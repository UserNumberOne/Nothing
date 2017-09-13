package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketBlockBreakAnim implements Packet {
   private int breakerId;
   private BlockPos position;
   private int progress;

   public SPacketBlockBreakAnim() {
   }

   public SPacketBlockBreakAnim(int var1, BlockPos var2, int var3) {
      this.breakerId = var1;
      this.position = var2;
      this.progress = var3;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.breakerId = var1.readVarInt();
      this.position = var1.readBlockPos();
      this.progress = var1.readUnsignedByte();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.breakerId);
      var1.writeBlockPos(this.position);
      var1.writeByte(this.progress);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleBlockBreakAnim(this);
   }

   @SideOnly(Side.CLIENT)
   public int getBreakerId() {
      return this.breakerId;
   }

   @SideOnly(Side.CLIENT)
   public BlockPos getPosition() {
      return this.position;
   }

   @SideOnly(Side.CLIENT)
   public int getProgress() {
      return this.progress;
   }
}
