package net.minecraft.world.chunk.storage;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.NibbleArray;

public class ExtendedBlockStorage {
   private final int yBase;
   private int blockRefCount;
   private int tickRefCount;
   private final BlockStateContainer data;
   private NibbleArray blocklightArray;
   private NibbleArray skylightArray;

   public ExtendedBlockStorage(int i, boolean flag) {
      this.yBase = i;
      this.data = new BlockStateContainer();
      this.blocklightArray = new NibbleArray();
      if (flag) {
         this.skylightArray = new NibbleArray();
      }

   }

   public ExtendedBlockStorage(int y, boolean flag, char[] blockIds) {
      this.yBase = y;
      this.data = new BlockStateContainer();

      for(int i = 0; i < blockIds.length; ++i) {
         int xx = i & 15;
         int yy = i >> 8 & 15;
         int zz = i >> 4 & 15;
         this.data.set(xx, yy, zz, (IBlockState)Block.BLOCK_STATE_IDS.getByValue(blockIds[i]));
      }

      this.blocklightArray = new NibbleArray();
      if (flag) {
         this.skylightArray = new NibbleArray();
      }

      this.removeInvalidBlocks();
   }

   public IBlockState get(int i, int j, int k) {
      return this.data.get(i, j, k);
   }

   public void set(int i, int j, int k, IBlockState iblockdata) {
      IBlockState iblockdata1 = this.get(i, j, k);
      Block block = iblockdata1.getBlock();
      Block block1 = iblockdata.getBlock();
      if (block != Blocks.AIR) {
         --this.blockRefCount;
         if (block.getTickRandomly()) {
            --this.tickRefCount;
         }
      }

      if (block1 != Blocks.AIR) {
         ++this.blockRefCount;
         if (block1.getTickRandomly()) {
            ++this.tickRefCount;
         }
      }

      this.data.set(i, j, k, iblockdata);
   }

   public boolean isEmpty() {
      return false;
   }

   public boolean getNeedsRandomTick() {
      return this.tickRefCount > 0;
   }

   public int getYLocation() {
      return this.yBase;
   }

   public void setExtSkylightValue(int i, int j, int k, int l) {
      this.skylightArray.set(i, j, k, l);
   }

   public int getExtSkylightValue(int i, int j, int k) {
      return this.skylightArray.get(i, j, k);
   }

   public void setExtBlocklightValue(int i, int j, int k, int l) {
      this.blocklightArray.set(i, j, k, l);
   }

   public int getExtBlocklightValue(int i, int j, int k) {
      return this.blocklightArray.get(i, j, k);
   }

   public void removeInvalidBlocks() {
      this.blockRefCount = 0;
      this.tickRefCount = 0;

      for(int i = 0; i < 16; ++i) {
         for(int j = 0; j < 16; ++j) {
            for(int k = 0; k < 16; ++k) {
               Block block = this.get(i, j, k).getBlock();
               if (block != Blocks.AIR) {
                  ++this.blockRefCount;
                  if (block.getTickRandomly()) {
                     ++this.tickRefCount;
                  }
               }
            }
         }
      }

   }

   public BlockStateContainer getData() {
      return this.data;
   }

   public NibbleArray getBlocklightArray() {
      return this.blocklightArray;
   }

   public NibbleArray getSkylightArray() {
      return this.skylightArray;
   }

   public void setBlocklightArray(NibbleArray nibblearray) {
      this.blocklightArray = nibblearray;
   }

   public void setSkylightArray(NibbleArray nibblearray) {
      this.skylightArray = nibblearray;
   }
}
