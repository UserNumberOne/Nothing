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
      this.blockPosition = pos;
      this.instrument = instrumentIn;
      this.pitch = pitchIn;
      this.block = blockIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.blockPosition = buf.readBlockPos();
      this.instrument = buf.readUnsignedByte();
      this.pitch = buf.readUnsignedByte();
      this.block = Block.getBlockById(buf.readVarInt() & 4095);
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeBlockPos(this.blockPosition);
      buf.writeByte(this.instrument);
      buf.writeByte(this.pitch);
      buf.writeVarInt(Block.getIdFromBlock(this.block) & 4095);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleBlockAction(this);
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
