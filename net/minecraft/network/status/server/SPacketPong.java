package net.minecraft.network.status.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.status.INetHandlerStatusClient;

public class SPacketPong implements Packet {
   private long clientTime;

   public SPacketPong() {
   }

   public SPacketPong(long var1) {
      this.clientTime = var1;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.clientTime = var1.readLong();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeLong(this.clientTime);
   }

   public void processPacket(INetHandlerStatusClient var1) {
      var1.handlePong(this);
   }
}
