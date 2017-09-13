package net.minecraft.server.network;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.INetHandlerHandshakeServer;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.server.SPacketDisconnect;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.apache.logging.log4j.LogManager;

public class NetHandlerHandshakeTCP implements INetHandlerHandshakeServer {
   private static final HashMap throttleTracker = new HashMap();
   private static int throttleCounter = 0;
   private final MinecraftServer server;
   private final NetworkManager networkManager;

   public NetHandlerHandshakeTCP(MinecraftServer minecraftserver, NetworkManager networkmanager) {
      this.server = minecraftserver;
      this.networkManager = networkmanager;
   }

   public void processHandshake(C00Handshake packethandshakinginsetprotocol) {
      switch(NetHandlerHandshakeTCP.SyntheticClass_1.a[packethandshakinginsetprotocol.getRequestedState().ordinal()]) {
      case 1:
         this.networkManager.setConnectionState(EnumConnectionState.LOGIN);

         try {
            long currentTime = System.currentTimeMillis();
            long connectionThrottle = MinecraftServer.getServer().server.getConnectionThrottle();
            InetAddress address = ((InetSocketAddress)this.networkManager.getRemoteAddress()).getAddress();
            synchronized(throttleTracker) {
               if (throttleTracker.containsKey(address) && !"127.0.0.1".equals(address.getHostAddress()) && currentTime - ((Long)throttleTracker.get(address)).longValue() < connectionThrottle) {
                  throttleTracker.put(address, Long.valueOf(currentTime));
                  TextComponentString chatcomponenttext = new TextComponentString("Connection throttled! Please wait before reconnecting.");
                  this.networkManager.sendPacket(new SPacketDisconnect(chatcomponenttext));
                  this.networkManager.closeChannel(chatcomponenttext);
                  return;
               }

               throttleTracker.put(address, Long.valueOf(currentTime));
               ++throttleCounter;
               if (throttleCounter > 200) {
                  throttleCounter = 0;
                  Iterator iter = throttleTracker.entrySet().iterator();

                  while(iter.hasNext()) {
                     Entry entry = (Entry)iter.next();
                     if (((Long)entry.getValue()).longValue() > connectionThrottle) {
                        iter.remove();
                     }
                  }
               }
            }
         } catch (Throwable var13) {
            LogManager.getLogger().debug("Failed to check connection throttle", var13);
         }

         if (packethandshakinginsetprotocol.getProtocolVersion() > 210) {
            TextComponentString chatcomponenttext = new TextComponentString("Outdated server! I'm still on 1.10.2");
            this.networkManager.sendPacket(new SPacketDisconnect(chatcomponenttext));
            this.networkManager.closeChannel(chatcomponenttext);
         } else if (packethandshakinginsetprotocol.getProtocolVersion() < 210) {
            TextComponentString chatcomponenttext = new TextComponentString("Outdated client! Please use 1.10.2");
            this.networkManager.sendPacket(new SPacketDisconnect(chatcomponenttext));
            this.networkManager.closeChannel(chatcomponenttext);
         } else {
            this.networkManager.setNetHandler(new NetHandlerLoginServer(this.server, this.networkManager));
            ((NetHandlerLoginServer)this.networkManager.getNetHandler()).hostname = packethandshakinginsetprotocol.ip + ":" + packethandshakinginsetprotocol.port;
         }
         break;
      case 2:
         this.networkManager.setConnectionState(EnumConnectionState.STATUS);
         this.networkManager.setNetHandler(new NetHandlerStatusServer(this.server, this.networkManager));
         break;
      default:
         throw new UnsupportedOperationException("Invalid intention " + packethandshakinginsetprotocol.getRequestedState());
      }

   }

   public void onDisconnect(ITextComponent ichatbasecomponent) {
   }

   static class SyntheticClass_1 {
      static final int[] a = new int[EnumConnectionState.values().length];

      static {
         try {
            a[EnumConnectionState.LOGIN.ordinal()] = 1;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[EnumConnectionState.STATUS.ordinal()] = 2;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
