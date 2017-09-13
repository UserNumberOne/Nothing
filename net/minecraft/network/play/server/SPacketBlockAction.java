package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.block.Block;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketBlockAction implements Packet {
   private BlockPos blockPosition;
   private int instrument;
   private int pitch;
   private Block block;

   public SPacketBlockAction() {
   }

   public SPacketBlockAction(BlockPos var1, Block var2, int var3, int var4) {
      this.blockPosition = var1;
      this.instrument = var3;
      this.pitch = var4;
      this.block = var2;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.blockPosition = var1.readBlockPos();
      this.instrument = var1.readUnsignedByte();
      this.pitch = var1.readUnsignedByte();
      this.block = Block.getBlockById(var1.readVarInt() & 4095);
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeBlockPos(this.blockPosition);
      var1.writeByte(this.instrument);
      var1.writeByte(this.pitch);
      var1.writeVarInt(Block.getIdFromBlock(this.block) & 4095);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleBlockAction(this);
   }

   @SideOnly(Side.CLIENT)
   public BlockPos getBlockPosition() {
      return this.blockPosition;
   }

   @SideOnly(Side.CLIENT)
   public int getData1() {
      return this.instrument;
   }

   @SideOnly(Side.CLIENT)
   public int getData2() {
      return this.pitch;
   }

   @SideOnly(Side.CLIENT)
   public Block getBlockType() {
      return this.block;
   }
}
