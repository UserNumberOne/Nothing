package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketUnloadChunk implements Packet {
   private int x;
   private int z;

   public SPacketUnloadChunk() {
   }

   public SPacketUnloadChunk(int var1, int var2) {
      this.x = var1;
      this.z = var2;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.x = var1.readInt();
      this.z = var1.readInt();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeInt(this.x);
      var1.writeInt(this.z);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.processChunkUnload(this);
   }

   @SideOnly(Side.CLIENT)
   public int getX() {
      return this.x;
   }

   @SideOnly(Side.CLIENT)
   public int getZ() {
      return this.z;
   }
}
