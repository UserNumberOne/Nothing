package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockBush;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenBush extends WorldGenerator {
   private final BlockBush block;

   public WorldGenBush(BlockBush var1) {
      this.block = var1;
   }

   public boolean generate(World var1, Random var2, BlockPos var3) {
      for(int var4 = 0; var4 < 64; ++var4) {
         BlockPos var5 = var3.add(var2.nextInt(8) - var2.nextInt(8), var2.nextInt(4) - var2.nextInt(4), var2.nextInt(8) - var2.nextInt(8));
         if (var1.isAirBlock(var5) && (!var1.provider.hasNoSky() || var5.getY() < var1.getHeight() - 1) && this.block.canBlockStay(var1, var5, this.block.getDefaultState())) {
            var1.setBlockState(var5, this.block.getDefaultState(), 2);
         }
      }

      return true;
   }
}
