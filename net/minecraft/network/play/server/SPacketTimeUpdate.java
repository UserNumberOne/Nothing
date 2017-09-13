package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketTimeUpdate implements Packet {
   private long totalWorldTime;
   private long worldTime;

   public SPacketTimeUpdate() {
   }

   public SPacketTimeUpdate(long var1, long var3, boolean var5) {
      this.totalWorldTime = totalWorldTimeIn;
      this.worldTime = worldTimeIn;
      if (!p_i46902_5_) {
         this.worldTime = -this.worldTime;
         if (this.worldTime == 0L) {
            this.worldTime = -1L;
         }
      }

   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.totalWorldTime = buf.readLong();
      this.worldTime = buf.readLong();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeLong(this.totalWorldTime);
      buf.writeLong(this.worldTime);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleTimeUpdate(this);
   }

   @SideOnly(Side.CLIENT)
   public long getTotalWorldTime() {
      return this.totalWorldTime;
   }

   @SideOnly(Side.CLIENT)
   public long getWorldTime() {
      return this.worldTime;
   }
}
