package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketMultiBlockChange implements Packet {
   private ChunkPos chunkPos;
   private SPacketMultiBlockChange.BlockUpdateData[] changedBlocks;

   public SPacketMultiBlockChange() {
   }

   public SPacketMultiBlockChange(int var1, short[] var2, Chunk var3) {
      this.chunkPos = new ChunkPos(var3.xPosition, var3.zPosition);
      this.changedBlocks = new SPacketMultiBlockChange.BlockUpdateData[var1];

      for(int var4 = 0; var4 < this.changedBlocks.length; ++var4) {
         this.changedBlocks[var4] = new SPacketMultiBlockChange.BlockUpdateData(var2[var4], var3);
      }

   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.chunkPos = new ChunkPos(var1.readInt(), var1.readInt());
      this.changedBlocks = new SPacketMultiBlockChange.BlockUpdateData[var1.readVarInt()];

      for(int var2 = 0; var2 < this.changedBlocks.length; ++var2) {
         this.changedBlocks[var2] = new SPacketMultiBlockChange.BlockUpdateData(var1.readShort(), (IBlockState)Block.BLOCK_STATE_IDS.getByValue(var1.readVarInt()));
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeInt(this.chunkPos.chunkXPos);
      var1.writeInt(this.chunkPos.chunkZPos);
      var1.writeVarInt(this.changedBlocks.length);

      for(SPacketMultiBlockChange.BlockUpdateData var5 : this.changedBlocks) {
         var1.writeShort(var5.getOffset());
         var1.writeVarInt(Block.BLOCK_STATE_IDS.get(var5.getBlockState()));
      }

   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleMultiBlockChange(this);
   }

   @SideOnly(Side.CLIENT)
   public SPacketMultiBlockChange.BlockUpdateData[] getChangedBlocks() {
      return this.changedBlocks;
   }

   public class BlockUpdateData {
      private final short offset;
      private final IBlockState blockState;

      public BlockUpdateData(short var2, IBlockState var3) {
         this.offset = var2;
         this.blockState = var3;
      }

      public BlockUpdateData(short var2, Chunk var3) {
         this.offset = var2;
         this.blockState = var3.getBlockState(this.getPos());
      }

      public BlockPos getPos() {
         return new BlockPos(SPacketMultiBlockChange.this.chunkPos.getBlock(this.offset >> 12 & 15, this.offset & 255, this.offset >> 8 & 15));
      }

      public short getOffset() {
         return this.offset;
      }

      public IBlockState getBlockState() {
         return this.blockState;
      }
   }
}
