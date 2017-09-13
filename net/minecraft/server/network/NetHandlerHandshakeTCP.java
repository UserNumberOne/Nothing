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

   public NetHandlerHandshakeTCP(MinecraftServer var1, NetworkManager var2) {
      this.server = var1;
      this.networkManager = var2;
   }

   public void processHandshake(C00Handshake var1) {
      switch(NetHandlerHandshakeTCP.SyntheticClass_1.a[var1.getRequestedState().ordinal()]) {
      case 1:
         this.networkManager.setConnectionState(EnumConnectionState.LOGIN);

         try {
            long var2 = System.currentTimeMillis();
            long var4 = MinecraftServer.getServer().server.getConnectionThrottle();
            InetAddress var6 = ((InetSocketAddress)this.networkManager.getRemoteAddress()).getAddress();
            synchronized(throttleTracker) {
               if (throttleTracker.containsKey(var6) && !"127.0.0.1".equals(var6.getHostAddress()) && var2 - ((Long)throttleTracker.get(var6)).longValue() < var4) {
                  throttleTracker.put(var6, Long.valueOf(var2));
                  TextComponentString var15 = new TextComponentString("Connection throttled! Please wait before reconnecting.");
                  this.networkManager.sendPacket(new SPacketDisconnect(var15));
                  this.networkManager.closeChannel(var15);
                  return;
               }

               throttleTracker.put(var6, Long.valueOf(var2));
               ++throttleCounter;
               if (throttleCounter > 200) {
                  throttleCounter = 0;
                  Iterator var9 = throttleTracker.entrySet().iterator();

                  while(var9.hasNext()) {
                     Entry var10 = (Entry)var9.next();
                     if (((Long)var10.getValue()).longValue() > var4) {
                        var9.remove();
                     }
                  }
               }
            }
         } catch (Throwable var13) {
            LogManager.getLogger().debug("Failed to check connection throttle", var13);
         }

         if (var1.getProtocolVersion() > 210) {
            TextComponentString var8 = new TextComponentString("Outdated server! I'm still on 1.10.2");
            this.networkManager.sendPacket(new SPacketDisconnect(var8));
            this.networkManager.closeChannel(var8);
         } else if (var1.getProtocolVersion() < 210) {
            TextComponentString var14 = new TextComponentString("Outdated client! Please use 1.10.2");
            this.networkManager.sendPacket(new SPacketDisconnect(var14));
            this.networkManager.closeChannel(var14);
         } else {
            this.networkManager.setNetHandler(new NetHandlerLoginServer(this.server, this.networkManager));
            ((NetHandlerLoginServer)this.networkManager.getNetHandler()).hostname = var1.ip + ":" + var1.port;
         }
         break;
      case 2:
         this.networkManager.setConnectionState(EnumConnectionState.STATUS);
         this.networkManager.setNetHandler(new NetHandlerStatusServer(this.server, this.networkManager));
         break;
      default:
         throw new UnsupportedOperationException("Invalid intention " + var1.getRequestedState());
      }

   }

   public void onDisconnect(ITextComponent var1) {
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
