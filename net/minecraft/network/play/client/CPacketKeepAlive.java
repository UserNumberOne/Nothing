package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CPacketKeepAlive implements Packet {
   private int key;

   public CPacketKeepAlive() {
   }

   @SideOnly(Side.CLIENT)
   public CPacketKeepAlive(int var1) {
      this.key = var1;
   }

   public void processPacket(INetHandlerPlayServer var1) {
      var1.processKeepAlive(this);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.key = var1.readVarInt();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.key);
   }

   public int getKey() {
      return this.key;
   }
}
