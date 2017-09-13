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

   public ExtendedBlockStorage(int var1, boolean var2) {
      this.yBase = var1;
      this.data = new BlockStateContainer();
      this.blocklightArray = new NibbleArray();
      if (var2) {
         this.skylightArray = new NibbleArray();
      }

   }

   public ExtendedBlockStorage(int var1, boolean var2, char[] var3) {
      this.yBase = var1;
      this.data = new BlockStateContainer();

      for(int var4 = 0; var4 < var3.length; ++var4) {
         int var5 = var4 & 15;
         int var6 = var4 >> 8 & 15;
         int var7 = var4 >> 4 & 15;
         this.data.set(var5, var6, var7, (IBlockState)Block.BLOCK_STATE_IDS.getByValue(var3[var4]));
      }

      this.blocklightArray = new NibbleArray();
      if (var2) {
         this.skylightArray = new NibbleArray();
      }

      this.removeInvalidBlocks();
   }

   public IBlockState get(int var1, int var2, int var3) {
      return this.data.get(var1, var2, var3);
   }

   public void set(int var1, int var2, int var3, IBlockState var4) {
      IBlockState var5 = this.get(var1, var2, var3);
      Block var6 = var5.getBlock();
      Block var7 = var4.getBlock();
      if (var6 != Blocks.AIR) {
         --this.blockRefCount;
         if (var6.getTickRandomly()) {
            --this.tickRefCount;
         }
      }

      if (var7 != Blocks.AIR) {
         ++this.blockRefCount;
         if (var7.getTickRandomly()) {
            ++this.tickRefCount;
         }
      }

      this.data.set(var1, var2, var3, var4);
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

   public void setExtSkylightValue(int var1, int var2, int var3, int var4) {
      this.skylightArray.set(var1, var2, var3, var4);
   }

   public int getExtSkylightValue(int var1, int var2, int var3) {
      return this.skylightArray.get(var1, var2, var3);
   }

   public void setExtBlocklightValue(int var1, int var2, int var3, int var4) {
      this.blocklightArray.set(var1, var2, var3, var4);
   }

   public int getExtBlocklightValue(int var1, int var2, int var3) {
      return this.blocklightArray.get(var1, var2, var3);
   }

   public void removeInvalidBlocks() {
      this.blockRefCount = 0;
      this.tickRefCount = 0;

      for(int var1 = 0; var1 < 16; ++var1) {
         for(int var2 = 0; var2 < 16; ++var2) {
            for(int var3 = 0; var3 < 16; ++var3) {
               Block var4 = this.get(var1, var2, var3).getBlock();
               if (var4 != Blocks.AIR) {
                  ++this.blockRefCount;
                  if (var4.getTickRandomly()) {
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

   public void setBlocklightArray(NibbleArray var1) {
      this.blocklightArray = var1;
   }

   public void setSkylightArray(NibbleArray var1) {
      this.skylightArray = var1;
   }
}
