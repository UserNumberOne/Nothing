package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class WorldGenAbstractTree extends WorldGenerator {
   public WorldGenAbstractTree(boolean var1) {
      super(var1);
   }

   protected boolean canGrowInto(Block var1) {
      Material var2 = var1.getDefaultState().getMaterial();
      return var2 == Material.AIR || var2 == Material.LEAVES || var1 == Blocks.GRASS || var1 == Blocks.DIRT || var1 == Blocks.LOG || var1 == Blocks.LOG2 || var1 == Blocks.SAPLING || var1 == Blocks.VINE;
   }

   public void generateSaplings(World var1, Random var2, BlockPos var3) {
   }

   protected void setDirtAt(World var1, BlockPos var2) {
      if (var1.getBlockState(var2).getBlock() != Blocks.DIRT) {
         this.setBlockAndNotifyAdequately(var1, var2, Blocks.DIRT.getDefaultState());
      }

   }
}
