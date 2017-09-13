package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class WorldGenerator {
   private final boolean doBlockNotify;

   public WorldGenerator() {
      this(false);
   }

   public WorldGenerator(boolean var1) {
      this.doBlockNotify = var1;
   }

   public abstract boolean generate(World var1, Random var2, BlockPos var3);

   public void setDecorationDefaults() {
   }

   protected void setBlockAndNotifyAdequately(World var1, BlockPos var2, IBlockState var3) {
      if (this.doBlockNotify) {
         var1.setBlockState(var2, var3, 3);
      } else {
         var1.setBlockState(var2, var3, 2);
      }

   }
}
