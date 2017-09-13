package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenMelon extends WorldGenerator {
   public boolean generate(World var1, Random var2, BlockPos var3) {
      for(int var4 = 0; var4 < 64; ++var4) {
         BlockPos var5 = var3.add(var2.nextInt(8) - var2.nextInt(8), var2.nextInt(4) - var2.nextInt(4), var2.nextInt(8) - var2.nextInt(8));
         if (Blocks.MELON_BLOCK.canPlaceBlockAt(var1, var5) && var1.getBlockState(var5.down()).getBlock() == Blocks.GRASS) {
            var1.setBlockState(var5, Blocks.MELON_BLOCK.getDefaultState(), 2);
         }
      }

      return true;
   }
}
