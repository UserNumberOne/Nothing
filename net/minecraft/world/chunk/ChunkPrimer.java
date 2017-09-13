package net.minecraft.world.chunk;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class ChunkPrimer {
   private static final IBlockState DEFAULT_STATE = Blocks.AIR.getDefaultState();
   private final char[] data = new char[65536];

   public IBlockState getBlockState(int var1, int var2, int var3) {
      IBlockState var4 = (IBlockState)Block.BLOCK_STATE_IDS.getByValue(this.data[getBlockIndex(var1, var2, var3)]);
      return var4 == null ? DEFAULT_STATE : var4;
   }

   public void setBlockState(int var1, int var2, int var3, IBlockState var4) {
      this.data[getBlockIndex(var1, var2, var3)] = (char)Block.BLOCK_STATE_IDS.get(var4);
   }

   private static int getBlockIndex(int var0, int var1, int var2) {
      return var0 << 12 | var2 << 8 | var1;
   }

   public int findGroundBlockIdx(int var1, int var2) {
      int var3 = (var1 << 12 | var2 << 8) + 256 - 1;

      for(int var4 = 255; var4 >= 0; --var4) {
         IBlockState var5 = (IBlockState)Block.BLOCK_STATE_IDS.getByValue(this.data[var3 + var4]);
         if (var5 != null && var5 != DEFAULT_STATE) {
            return var4;
         }
      }

      return 0;
   }
}
