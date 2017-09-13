package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class WorldGenLakes extends WorldGenerator {
   private final Block block;

   public WorldGenLakes(Block var1) {
      this.block = var1;
   }

   public boolean generate(World var1, Random var2, BlockPos var3) {
      for(var3 = var3.add(-8, 0, -8); var3.getY() > 5 && var1.isAirBlock(var3); var3 = var3.down()) {
         ;
      }

      if (var3.getY() <= 4) {
         return false;
      } else {
         var3 = var3.down(4);
         boolean[] var4 = new boolean[2048];
         int var5 = var2.nextInt(4) + 4;

         for(int var6 = 0; var6 < var5; ++var6) {
            double var7 = var2.nextDouble() * 6.0D + 3.0D;
            double var9 = var2.nextDouble() * 4.0D + 2.0D;
            double var11 = var2.nextDouble() * 6.0D + 3.0D;
            double var13 = var2.nextDouble() * (16.0D - var7 - 2.0D) + 1.0D + var7 / 2.0D;
            double var15 = var2.nextDouble() * (8.0D - var9 - 4.0D) + 2.0D + var9 / 2.0D;
            double var17 = var2.nextDouble() * (16.0D - var11 - 2.0D) + 1.0D + var11 / 2.0D;

            for(int var19 = 1; var19 < 15; ++var19) {
               for(int var20 = 1; var20 < 15; ++var20) {
                  for(int var21 = 1; var21 < 7; ++var21) {
                     double var22 = ((double)var19 - var13) / (var7 / 2.0D);
                     double var24 = ((double)var21 - var15) / (var9 / 2.0D);
                     double var26 = ((double)var20 - var17) / (var11 / 2.0D);
                     double var28 = var22 * var22 + var24 * var24 + var26 * var26;
                     if (var28 < 1.0D) {
                        var4[(var19 * 16 + var20) * 8 + var21] = true;
                     }
                  }
               }
            }
         }

         for(int var36 = 0; var36 < 16; ++var36) {
            for(int var30 = 0; var30 < 16; ++var30) {
               for(int var31 = 0; var31 < 8; ++var31) {
                  boolean var32 = !var4[(var36 * 16 + var30) * 8 + var31] && (var36 < 15 && var4[((var36 + 1) * 16 + var30) * 8 + var31] || var36 > 0 && var4[((var36 - 1) * 16 + var30) * 8 + var31] || var30 < 15 && var4[(var36 * 16 + var30 + 1) * 8 + var31] || var30 > 0 && var4[(var36 * 16 + (var30 - 1)) * 8 + var31] || var31 < 7 && var4[(var36 * 16 + var30) * 8 + var31 + 1] || var31 > 0 && var4[(var36 * 16 + var30) * 8 + (var31 - 1)]);
                  if (var32) {
                     Material var33 = var1.getBlockState(var3.add(var36, var31, var30)).getMaterial();
                     if (var31 >= 4 && var33.isLiquid()) {
                        return false;
                     }

                     if (var31 < 4 && !var33.isSolid() && var1.getBlockState(var3.add(var36, var31, var30)).getBlock() != this.block) {
                        return false;
                     }
                  }
               }
            }
         }

         for(int var37 = 0; var37 < 16; ++var37) {
            for(int var41 = 0; var41 < 16; ++var41) {
               for(int var45 = 0; var45 < 8; ++var45) {
                  if (var4[(var37 * 16 + var41) * 8 + var45]) {
                     var1.setBlockState(var3.add(var37, var45, var41), var45 >= 4 ? Blocks.AIR.getDefaultState() : this.block.getDefaultState(), 2);
                  }
               }
            }
         }

         for(int var38 = 0; var38 < 16; ++var38) {
            for(int var42 = 0; var42 < 16; ++var42) {
               for(int var46 = 4; var46 < 8; ++var46) {
                  if (var4[(var38 * 16 + var42) * 8 + var46]) {
                     BlockPos var49 = var3.add(var38, var46 - 1, var42);
                     if (var1.getBlockState(var49).getBlock() == Blocks.DIRT && var1.getLightFor(EnumSkyBlock.SKY, var3.add(var38, var46, var42)) > 0) {
                        Biome var51 = var1.getBiome(var49);
                        if (var51.topBlock.getBlock() == Blocks.MYCELIUM) {
                           var1.setBlockState(var49, Blocks.MYCELIUM.getDefaultState(), 2);
                        } else {
                           var1.setBlockState(var49, Blocks.GRASS.getDefaultState(), 2);
                        }
                     }
                  }
               }
            }
         }

         if (this.block.getDefaultState().getMaterial() == Material.LAVA) {
            for(int var39 = 0; var39 < 16; ++var39) {
               for(int var43 = 0; var43 < 16; ++var43) {
                  for(int var47 = 0; var47 < 8; ++var47) {
                     boolean var50 = !var4[(var39 * 16 + var43) * 8 + var47] && (var39 < 15 && var4[((var39 + 1) * 16 + var43) * 8 + var47] || var39 > 0 && var4[((var39 - 1) * 16 + var43) * 8 + var47] || var43 < 15 && var4[(var39 * 16 + var43 + 1) * 8 + var47] || var43 > 0 && var4[(var39 * 16 + (var43 - 1)) * 8 + var47] || var47 < 7 && var4[(var39 * 16 + var43) * 8 + var47 + 1] || var47 > 0 && var4[(var39 * 16 + var43) * 8 + (var47 - 1)]);
                     if (var50 && (var47 < 4 || var2.nextInt(2) != 0) && var1.getBlockState(var3.add(var39, var47, var43)).getMaterial().isSolid()) {
                        var1.setBlockState(var3.add(var39, var47, var43), Blocks.STONE.getDefaultState(), 2);
                     }
                  }
               }
            }
         }

         if (this.block.getDefaultState().getMaterial() == Material.WATER) {
            for(int var40 = 0; var40 < 16; ++var40) {
               for(int var44 = 0; var44 < 16; ++var44) {
                  boolean var48 = true;
                  if (var1.canBlockFreezeWater(var3.add(var40, 4, var44))) {
                     var1.setBlockState(var3.add(var40, 4, var44), Blocks.ICE.getDefaultState(), 2);
                  }
               }
            }
         }

         return true;
      }
   }
}
