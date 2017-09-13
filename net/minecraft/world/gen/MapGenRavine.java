package net.minecraft.world.gen;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

public class MapGenRavine extends MapGenBase {
   protected static final IBlockState FLOWING_LAVA = Blocks.FLOWING_LAVA.getDefaultState();
   protected static final IBlockState AIR = Blocks.AIR.getDefaultState();
   private final float[] rs = new float[1024];

   protected void addTunnel(long var1, int var3, int var4, ChunkPrimer var5, double var6, double var8, double var10, float var12, float var13, float var14, int var15, int var16, double var17) {
      Random var19 = new Random(var1);
      double var20 = (double)(var3 * 16 + 8);
      double var22 = (double)(var4 * 16 + 8);
      float var24 = 0.0F;
      float var25 = 0.0F;
      if (var16 <= 0) {
         int var26 = this.range * 16 - 16;
         var16 = var26 - var19.nextInt(var26 / 4);
      }

      boolean var62 = false;
      if (var15 == -1) {
         var15 = var16 / 2;
         var62 = true;
      }

      float var27 = 1.0F;

      for(int var28 = 0; var28 < 256; ++var28) {
         if (var28 == 0 || var19.nextInt(3) == 0) {
            var27 = 1.0F + var19.nextFloat() * var19.nextFloat();
         }

         this.rs[var28] = var27 * var27;
      }

      for(; var15 < var16; ++var15) {
         double var63 = 1.5D + (double)(MathHelper.sin((float)var15 * 3.1415927F / (float)var16) * var12);
         double var30 = var63 * var17;
         var63 = var63 * ((double)var19.nextFloat() * 0.25D + 0.75D);
         var30 = var30 * ((double)var19.nextFloat() * 0.25D + 0.75D);
         float var32 = MathHelper.cos(var14);
         float var33 = MathHelper.sin(var14);
         var6 += (double)(MathHelper.cos(var13) * var32);
         var8 += (double)var33;
         var10 += (double)(MathHelper.sin(var13) * var32);
         var14 = var14 * 0.7F;
         var14 = var14 + var25 * 0.05F;
         var13 += var24 * 0.05F;
         var25 = var25 * 0.8F;
         var24 = var24 * 0.5F;
         var25 = var25 + (var19.nextFloat() - var19.nextFloat()) * var19.nextFloat() * 2.0F;
         var24 = var24 + (var19.nextFloat() - var19.nextFloat()) * var19.nextFloat() * 4.0F;
         if (var62 || var19.nextInt(4) != 0) {
            double var34 = var6 - var20;
            double var36 = var10 - var22;
            double var38 = (double)(var16 - var15);
            double var40 = (double)(var12 + 2.0F + 16.0F);
            if (var34 * var34 + var36 * var36 - var38 * var38 > var40 * var40) {
               return;
            }

            if (var6 >= var20 - 16.0D - var63 * 2.0D && var10 >= var22 - 16.0D - var63 * 2.0D && var6 <= var20 + 16.0D + var63 * 2.0D && var10 <= var22 + 16.0D + var63 * 2.0D) {
               int var42 = MathHelper.floor(var6 - var63) - var3 * 16 - 1;
               int var43 = MathHelper.floor(var6 + var63) - var3 * 16 + 1;
               int var44 = MathHelper.floor(var8 - var30) - 1;
               int var45 = MathHelper.floor(var8 + var30) + 1;
               int var46 = MathHelper.floor(var10 - var63) - var4 * 16 - 1;
               int var47 = MathHelper.floor(var10 + var63) - var4 * 16 + 1;
               if (var42 < 0) {
                  var42 = 0;
               }

               if (var43 > 16) {
                  var43 = 16;
               }

               if (var44 < 1) {
                  var44 = 1;
               }

               if (var45 > 248) {
                  var45 = 248;
               }

               if (var46 < 0) {
                  var46 = 0;
               }

               if (var47 > 16) {
                  var47 = 16;
               }

               boolean var48 = false;

               for(int var49 = var42; !var48 && var49 < var43; ++var49) {
                  for(int var50 = var46; !var48 && var50 < var47; ++var50) {
                     for(int var51 = var45 + 1; !var48 && var51 >= var44 - 1; --var51) {
                        if (var51 >= 0 && var51 < 256) {
                           if (this.isOceanBlock(var5, var49, var51, var50, var3, var4)) {
                              var48 = true;
                           }

                           if (var51 != var44 - 1 && var49 != var42 && var49 != var43 - 1 && var50 != var46 && var50 != var47 - 1) {
                              var51 = var44;
                           }
                        }
                     }
                  }
               }

               if (!var48) {
                  for(int var66 = var42; var66 < var43; ++var66) {
                     double var67 = ((double)(var66 + var3 * 16) + 0.5D - var6) / var63;

                     for(int var52 = var46; var52 < var47; ++var52) {
                        double var53 = ((double)(var52 + var4 * 16) + 0.5D - var10) / var63;
                        boolean var55 = false;
                        if (var67 * var67 + var53 * var53 < 1.0D) {
                           for(int var56 = var45; var56 > var44; --var56) {
                              double var57 = ((double)(var56 - 1) + 0.5D - var8) / var30;
                              if ((var67 * var67 + var53 * var53) * (double)this.rs[var56 - 1] + var57 * var57 / 6.0D < 1.0D) {
                                 if (this.isTopBlock(var5, var66, var56, var52, var3, var4)) {
                                    var55 = true;
                                 }

                                 this.digBlock(var5, var66, var56, var52, var3, var4, var55);
                              }
                           }
                        }
                     }
                  }

                  if (var62) {
                     break;
                  }
               }
            }
         }
      }

   }

   protected void recursiveGenerate(World var1, int var2, int var3, int var4, int var5, ChunkPrimer var6) {
      if (this.rand.nextInt(50) == 0) {
         double var7 = (double)(var2 * 16 + this.rand.nextInt(16));
         double var9 = (double)(this.rand.nextInt(this.rand.nextInt(40) + 8) + 20);
         double var11 = (double)(var3 * 16 + this.rand.nextInt(16));
         boolean var13 = true;

         for(int var14 = 0; var14 < 1; ++var14) {
            float var15 = this.rand.nextFloat() * 6.2831855F;
            float var16 = (this.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
            float var17 = (this.rand.nextFloat() * 2.0F + this.rand.nextFloat()) * 2.0F;
            this.addTunnel(this.rand.nextLong(), var4, var5, var6, var7, var9, var11, var17, var15, var16, 0, 0, 3.0D);
         }
      }

   }

   protected boolean isOceanBlock(ChunkPrimer var1, int var2, int var3, int var4, int var5, int var6) {
      Block var7 = var1.getBlockState(var2, var3, var4).getBlock();
      return var7 == Blocks.FLOWING_WATER || var7 == Blocks.WATER;
   }

   private boolean isExceptionBiome(Biome var1) {
      if (var1 == Biomes.BEACH) {
         return true;
      } else if (var1 == Biomes.DESERT) {
         return true;
      } else if (var1 == Biomes.MUSHROOM_ISLAND) {
         return true;
      } else {
         return var1 == Biomes.MUSHROOM_ISLAND_SHORE;
      }
   }

   private boolean isTopBlock(ChunkPrimer var1, int var2, int var3, int var4, int var5, int var6) {
      Biome var7 = this.world.getBiome(new BlockPos(var2 + var5 * 16, 0, var4 + var6 * 16));
      IBlockState var8 = var1.getBlockState(var2, var3, var4);
      return this.isExceptionBiome(var7) ? var8.getBlock() == Blocks.GRASS : var8.getBlock() == var7.topBlock;
   }

   protected void digBlock(ChunkPrimer var1, int var2, int var3, int var4, int var5, int var6, boolean var7) {
      Biome var8 = this.world.getBiome(new BlockPos(var2 + var5 * 16, 0, var4 + var6 * 16));
      IBlockState var9 = var1.getBlockState(var2, var3, var4);
      IBlockState var10 = this.isExceptionBiome(var8) ? Blocks.GRASS.getDefaultState() : var8.topBlock;
      IBlockState var11 = this.isExceptionBiome(var8) ? Blocks.DIRT.getDefaultState() : var8.fillerBlock;
      if (var9.getBlock() == Blocks.STONE || var9.getBlock() == var10.getBlock() || var9.getBlock() == var11.getBlock()) {
         if (var3 - 1 < 10) {
            var1.setBlockState(var2, var3, var4, FLOWING_LAVA);
         } else {
            var1.setBlockState(var2, var3, var4, AIR);
            if (var7 && var1.getBlockState(var2, var3 - 1, var4).getBlock() == var11.getBlock()) {
               var1.setBlockState(var2, var3 - 1, var4, var10.getBlock().getDefaultState());
            }
         }
      }

   }
}
