package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class WorldGenIceSpike extends WorldGenerator {
   public boolean generate(World var1, Random var2, BlockPos var3) {
      while(var1.isAirBlock(var3) && var3.getY() > 2) {
         var3 = var3.down();
      }

      if (var1.getBlockState(var3).getBlock() != Blocks.SNOW) {
         return false;
      } else {
         var3 = var3.up(var2.nextInt(4));
         int var4 = var2.nextInt(4) + 7;
         int var5 = var4 / 4 + var2.nextInt(2);
         if (var5 > 1 && var2.nextInt(60) == 0) {
            var3 = var3.up(10 + var2.nextInt(30));
         }

         for(int var6 = 0; var6 < var4; ++var6) {
            float var7 = (1.0F - (float)var6 / (float)var4) * (float)var5;
            int var8 = MathHelper.ceil(var7);

            for(int var9 = -var8; var9 <= var8; ++var9) {
               float var10 = (float)MathHelper.abs(var9) - 0.25F;

               for(int var11 = -var8; var11 <= var8; ++var11) {
                  float var12 = (float)MathHelper.abs(var11) - 0.25F;
                  if ((var9 == 0 && var11 == 0 || var10 * var10 + var12 * var12 <= var7 * var7) && (var9 != -var8 && var9 != var8 && var11 != -var8 && var11 != var8 || var2.nextFloat() <= 0.75F)) {
                     IBlockState var13 = var1.getBlockState(var3.add(var9, var6, var11));
                     Block var14 = var13.getBlock();
                     if (var13.getMaterial() == Material.AIR || var14 == Blocks.DIRT || var14 == Blocks.SNOW || var14 == Blocks.ICE) {
                        this.setBlockAndNotifyAdequately(var1, var3.add(var9, var6, var11), Blocks.PACKED_ICE.getDefaultState());
                     }

                     if (var6 != 0 && var8 > 1) {
                        var13 = var1.getBlockState(var3.add(var9, -var6, var11));
                        var14 = var13.getBlock();
                        if (var13.getMaterial() == Material.AIR || var14 == Blocks.DIRT || var14 == Blocks.SNOW || var14 == Blocks.ICE) {
                           this.setBlockAndNotifyAdequately(var1, var3.add(var9, -var6, var11), Blocks.PACKED_ICE.getDefaultState());
                        }
                     }
                  }
               }
            }
         }

         int var16 = var5 - 1;
         if (var16 < 0) {
            var16 = 0;
         } else if (var16 > 1) {
            var16 = 1;
         }

         for(int var17 = -var16; var17 <= var16; ++var17) {
            for(int var18 = -var16; var18 <= var16; ++var18) {
               BlockPos var19 = var3.add(var17, -1, var18);
               int var20 = 50;
               if (Math.abs(var17) == 1 && Math.abs(var18) == 1) {
                  var20 = var2.nextInt(5);
               }

               while(var19.getY() > 50) {
                  IBlockState var21 = var1.getBlockState(var19);
                  Block var22 = var21.getBlock();
                  if (var21.getMaterial() != Material.AIR && var22 != Blocks.DIRT && var22 != Blocks.SNOW && var22 != Blocks.ICE && var22 != Blocks.PACKED_ICE) {
                     break;
                  }

                  this.setBlockAndNotifyAdequately(var1, var19, Blocks.PACKED_ICE.getDefaultState());
                  var19 = var19.down();
                  --var20;
                  if (var20 <= 0) {
                     var19 = var19.down(var2.nextInt(5) + 1);
                     var20 = var2.nextInt(5);
                  }
               }
            }
         }

         return true;
      }
   }
}
