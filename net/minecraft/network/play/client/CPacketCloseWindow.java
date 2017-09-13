package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CPacketCloseWindow implements Packet {
   private int windowId;

   public CPacketCloseWindow() {
   }

   @SideOnly(Side.CLIENT)
   public CPacketCloseWindow(int var1) {
      this.windowId = var1;
   }

   public void processPacket(INetHandlerPlayServer var1) {
      var1.processCloseWindow(this);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.windowId = var1.readByte();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeByte(this.windowId);
   }
}
