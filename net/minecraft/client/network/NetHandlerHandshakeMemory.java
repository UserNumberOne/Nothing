package net.minecraft.client.network;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.INetHandlerHandshakeServer;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.NetHandlerLoginServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class NetHandlerHandshakeMemory implements INetHandlerHandshakeServer {
   private final MinecraftServer mcServer;
   private final NetworkManager networkManager;

   public NetHandlerHandshakeMemory(MinecraftServer var1, NetworkManager var2) {
      this.mcServer = mcServerIn;
      this.networkManager = networkManagerIn;
   }

   public void processHandshake(C00Handshake var1) {
      if (FMLCommonHandler.instance().handleServerHandshake(packetIn, this.networkManager)) {
         this.networkManager.setConnectionState(packetIn.getRequestedState());
         this.networkManager.setNetHandler(new NetHandlerLoginServer(this.mcServer, this.networkManager));
      }
   }

   public void onDisconnect(ITextComponent var1) {
   }
}
