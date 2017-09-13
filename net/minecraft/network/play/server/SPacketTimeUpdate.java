package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class SPacketTimeUpdate implements Packet {
   private long totalWorldTime;
   private long worldTime;

   public SPacketTimeUpdate() {
   }

   public SPacketTimeUpdate(long var1, long var3, boolean var5) {
      this.totalWorldTime = var1;
      this.worldTime = var3;
      if (!var5) {
         this.worldTime = -this.worldTime;
         if (this.worldTime == 0L) {
            this.worldTime = -1L;
         }
      }

   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.totalWorldTime = var1.readLong();
      this.worldTime = var1.readLong();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeLong(this.totalWorldTime);
      var1.writeLong(this.worldTime);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleTimeUpdate(this);
   }
}
