package net.minecraft.world.gen;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
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

      boolean var67 = false;
      if (var15 == -1) {
         var15 = var16 / 2;
         var67 = true;
      }

      float var27 = 1.0F;

      for(int var28 = 0; var28 < 256; ++var28) {
         if (var28 == 0 || var19.nextInt(3) == 0) {
            var27 = 1.0F + var19.nextFloat() * var19.nextFloat();
         }

         this.rs[var28] = var27 * var27;
      }

      for(; var15 < var16; ++var15) {
         double var29 = 1.5D + (double)(MathHelper.sin((float)var15 * 3.1415927F / (float)var16) * var12);
         double var31 = var29 * var17;
         var29 = var29 * ((double)var19.nextFloat() * 0.25D + 0.75D);
         var31 = var31 * ((double)var19.nextFloat() * 0.25D + 0.75D);
         float var33 = MathHelper.cos(var14);
         float var34 = MathHelper.sin(var14);
         var6 += (double)(MathHelper.cos(var13) * var33);
         var8 += (double)var34;
         var10 += (double)(MathHelper.sin(var13) * var33);
         var14 = var14 * 0.7F;
         var14 = var14 + var25 * 0.05F;
         var13 += var24 * 0.05F;
         var25 = var25 * 0.8F;
         var24 = var24 * 0.5F;
         var25 = var25 + (var19.nextFloat() - var19.nextFloat()) * var19.nextFloat() * 2.0F;
         var24 = var24 + (var19.nextFloat() - var19.nextFloat()) * var19.nextFloat() * 4.0F;
         if (var67 || var19.nextInt(4) != 0) {
            double var35 = var6 - var20;
            double var37 = var10 - var22;
            double var39 = (double)(var16 - var15);
            double var41 = (double)(var12 + 2.0F + 16.0F);
            if (var35 * var35 + var37 * var37 - var39 * var39 > var41 * var41) {
               return;
            }

            if (var6 >= var20 - 16.0D - var29 * 2.0D && var10 >= var22 - 16.0D - var29 * 2.0D && var6 <= var20 + 16.0D + var29 * 2.0D && var10 <= var22 + 16.0D + var29 * 2.0D) {
               int var43 = MathHelper.floor(var6 - var29) - var3 * 16 - 1;
               int var44 = MathHelper.floor(var6 + var29) - var3 * 16 + 1;
               int var45 = MathHelper.floor(var8 - var31) - 1;
               int var46 = MathHelper.floor(var8 + var31) + 1;
               int var47 = MathHelper.floor(var10 - var29) - var4 * 16 - 1;
               int var48 = MathHelper.floor(var10 + var29) - var4 * 16 + 1;
               if (var43 < 0) {
                  var43 = 0;
               }

               if (var44 > 16) {
                  var44 = 16;
               }

               if (var45 < 1) {
                  var45 = 1;
               }

               if (var46 > 248) {
                  var46 = 248;
               }

               if (var47 < 0) {
                  var47 = 0;
               }

               if (var48 > 16) {
                  var48 = 16;
               }

               boolean var49 = false;

               for(int var50 = var43; !var49 && var50 < var44; ++var50) {
                  for(int var51 = var47; !var49 && var51 < var48; ++var51) {
                     for(int var52 = var46 + 1; !var49 && var52 >= var45 - 1; --var52) {
                        if (var52 >= 0 && var52 < 256) {
                           IBlockState var53 = var5.getBlockState(var50, var52, var51);
                           if (var53.getBlock() == Blocks.FLOWING_WATER || var53.getBlock() == Blocks.WATER) {
                              var49 = true;
                           }

                           if (var52 != var45 - 1 && var50 != var43 && var50 != var44 - 1 && var51 != var47 && var51 != var48 - 1) {
                              var52 = var45;
                           }
                        }
                     }
                  }
               }

               if (!var49) {
                  BlockPos.MutableBlockPos var70 = new BlockPos.MutableBlockPos();

                  for(int var71 = var43; var71 < var44; ++var71) {
                     double var54 = ((double)(var71 + var3 * 16) + 0.5D - var6) / var29;

                     for(int var56 = var47; var56 < var48; ++var56) {
                        double var57 = ((double)(var56 + var4 * 16) + 0.5D - var10) / var29;
                        boolean var59 = false;
                        if (var54 * var54 + var57 * var57 < 1.0D) {
                           for(int var60 = var46; var60 > var45; --var60) {
                              double var61 = ((double)(var60 - 1) + 0.5D - var8) / var31;
                              if ((var54 * var54 + var57 * var57) * (double)this.rs[var60 - 1] + var61 * var61 / 6.0D < 1.0D) {
                                 IBlockState var63 = var5.getBlockState(var71, var60, var56);
                                 if (var63.getBlock() == Blocks.GRASS) {
                                    var59 = true;
                                 }

                                 if (var63.getBlock() == Blocks.STONE || var63.getBlock() == Blocks.DIRT || var63.getBlock() == Blocks.GRASS) {
                                    if (var60 - 1 < 10) {
                                       var5.setBlockState(var71, var60, var56, FLOWING_LAVA);
                                    } else {
                                       var5.setBlockState(var71, var60, var56, AIR);
                                       if (var59 && var5.getBlockState(var71, var60 - 1, var56).getBlock() == Blocks.DIRT) {
                                          var70.setPos(var71 + var3 * 16, 0, var56 + var4 * 16);
                                          var5.setBlockState(var71, var60 - 1, var56, this.world.getBiome(var70).topBlock);
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (var67) {
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
}
