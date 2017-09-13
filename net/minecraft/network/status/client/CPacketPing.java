package net.minecraft.network.status.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.status.INetHandlerStatusServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CPacketPing implements Packet {
   private long clientTime;

   public CPacketPing() {
   }

   @SideOnly(Side.CLIENT)
   public CPacketPing(long var1) {
      this.clientTime = var1;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.clientTime = var1.readLong();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeLong(this.clientTime);
   }

   public void processPacket(INetHandlerStatusServer var1) {
      var1.processPing(this);
   }

   public long getClientTime() {
      return this.clientTime;
   }
}
