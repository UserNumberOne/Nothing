package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BiomeVoidDecorator extends BiomeDecorator {
   public void decorate(World var1, Random var2, Biome var3, BlockPos var4) {
      BlockPos var5 = var1.getSpawnPoint();
      boolean var6 = true;
      double var7 = var5.distanceSq(var4.add(8, var5.getY(), 8));
      if (var7 <= 1024.0D) {
         BlockPos var9 = new BlockPos(var5.getX() - 16, var5.getY() - 1, var5.getZ() - 16);
         BlockPos var10 = new BlockPos(var5.getX() + 16, var5.getY() - 1, var5.getZ() + 16);
         BlockPos.MutableBlockPos var11 = new BlockPos.MutableBlockPos(var9);

         for(int var12 = var4.getZ(); var12 < var4.getZ() + 16; ++var12) {
            for(int var13 = var4.getX(); var13 < var4.getX() + 16; ++var13) {
               if (var12 >= var9.getZ() && var12 <= var10.getZ() && var13 >= var9.getX() && var13 <= var10.getX()) {
                  var11.setPos(var13, var11.getY(), var12);
                  if (var5.getX() == var13 && var5.getZ() == var12) {
                     var1.setBlockState(var11, Blocks.COBBLESTONE.getDefaultState(), 2);
                  } else {
                     var1.setBlockState(var11, Blocks.STONE.getDefaultState(), 2);
                  }
               }
            }
         }

      }
   }
}
