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
   public CPacketKeepAlive(int idIn) {
      this.key = idIn;
   }

   public void processPacket(INetHandlerPlayServer handler) {
      handler.processKeepAlive(this);
   }

   public void readPacketData(PacketBuffer buf) throws IOException {
      this.key = buf.readVarInt();
   }

   public void writePacketData(PacketBuffer buf) throws IOException {
      buf.writeVarInt(this.key);
   }

   public int getKey() {
      return this.key;
   }
}
