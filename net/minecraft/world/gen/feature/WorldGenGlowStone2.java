package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenGlowStone2 extends WorldGenerator {
   public boolean generate(World var1, Random var2, BlockPos var3) {
      if (!var1.isAirBlock(var3)) {
         return false;
      } else if (var1.getBlockState(var3.up()).getBlock() != Blocks.NETHERRACK) {
         return false;
      } else {
         var1.setBlockState(var3, Blocks.GLOWSTONE.getDefaultState(), 2);

         for(int var4 = 0; var4 < 1500; ++var4) {
            BlockPos var5 = var3.add(var2.nextInt(8) - var2.nextInt(8), -var2.nextInt(12), var2.nextInt(8) - var2.nextInt(8));
            if (var1.getBlockState(var5).getMaterial() == Material.AIR) {
               int var6 = 0;

               for(EnumFacing var10 : EnumFacing.values()) {
                  if (var1.getBlockState(var5.offset(var10)).getBlock() == Blocks.GLOWSTONE) {
                     ++var6;
                  }

                  if (var6 > 1) {
                     break;
                  }
               }

               if (var6 == 1) {
                  var1.setBlockState(var5, Blocks.GLOWSTONE.getDefaultState(), 2);
               }
            }
         }

         return true;
      }
   }
}
