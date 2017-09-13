package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenFire extends WorldGenerator {
   public boolean generate(World var1, Random var2, BlockPos var3) {
      for(int i = 0; i < 64; ++i) {
         BlockPos blockpos = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));
         if (worldIn.isAirBlock(blockpos) && worldIn.getBlockState(blockpos.down()).getBlock() == Blocks.NETHERRACK) {
            worldIn.setBlockState(blockpos, Blocks.FIRE.getDefaultState(), 2);
         }
      }

      return true;
   }
}
