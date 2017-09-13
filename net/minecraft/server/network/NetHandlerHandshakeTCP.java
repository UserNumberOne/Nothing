package net.minecraft.server.network;

import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.INetHandlerHandshakeServer;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.server.SPacketDisconnect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class NetHandlerHandshakeTCP implements INetHandlerHandshakeServer {
   private final MinecraftServer server;
   private final NetworkManager networkManager;

   public NetHandlerHandshakeTCP(MinecraftServer var1, NetworkManager var2) {
      this.server = var1;
      this.networkManager = var2;
   }

   public void processHandshake(C00Handshake var1) {
      if (FMLCommonHandler.instance().handleServerHandshake(var1, this.networkManager)) {
         switch(var1.getRequestedState()) {
         case LOGIN:
            this.networkManager.setConnectionState(EnumConnectionState.LOGIN);
            if (var1.getProtocolVersion() > 210) {
               TextComponentString var2 = new TextComponentString("Outdated server! I'm still on 1.10.2");
               this.networkManager.sendPacket(new SPacketDisconnect(var2));
               this.networkManager.closeChannel(var2);
            } else if (var1.getProtocolVersion() < 210) {
               TextComponentString var3 = new TextComponentString("Outdated client! Please use 1.10.2");
               this.networkManager.sendPacket(new SPacketDisconnect(var3));
               this.networkManager.closeChannel(var3);
            } else {
               this.networkManager.setNetHandler(new NetHandlerLoginServer(this.server, this.networkManager));
            }
            break;
         case STATUS:
            this.networkManager.setConnectionState(EnumConnectionState.STATUS);
            this.networkManager.setNetHandler(new NetHandlerStatusServer(this.server, this.networkManager));
            break;
         default:
            throw new UnsupportedOperationException("Invalid intention " + var1.getRequestedState());
         }

      }
   }

   public void onDisconnect(ITextComponent var1) {
   }
}
