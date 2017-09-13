package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenReed extends WorldGenerator {
   public boolean generate(World var1, Random var2, BlockPos var3) {
      for(int var4 = 0; var4 < 20; ++var4) {
         BlockPos var5 = var3.add(var2.nextInt(4) - var2.nextInt(4), 0, var2.nextInt(4) - var2.nextInt(4));
         if (var1.isAirBlock(var5)) {
            BlockPos var6 = var5.down();
            if (var1.getBlockState(var6.west()).getMaterial() == Material.WATER || var1.getBlockState(var6.east()).getMaterial() == Material.WATER || var1.getBlockState(var6.north()).getMaterial() == Material.WATER || var1.getBlockState(var6.south()).getMaterial() == Material.WATER) {
               int var7 = 2 + var2.nextInt(var2.nextInt(3) + 1);

               for(int var8 = 0; var8 < var7; ++var8) {
                  if (Blocks.REEDS.canBlockStay(var1, var5)) {
                     var1.setBlockState(var5.up(var8), Blocks.REEDS.getDefaultState(), 2);
                  }
               }
            }
         }
      }

      return true;
   }
}
