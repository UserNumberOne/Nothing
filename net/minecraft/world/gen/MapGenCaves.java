package net.minecraft.world.gen;

import com.google.common.base.Objects;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

public class MapGenCaves extends MapGenBase {
   protected static final IBlockState BLK_LAVA = Blocks.LAVA.getDefaultState();
   protected static final IBlockState BLK_AIR = Blocks.AIR.getDefaultState();
   protected static final IBlockState BLK_SANDSTONE = Blocks.SANDSTONE.getDefaultState();
   protected static final IBlockState BLK_RED_SANDSTONE = Blocks.RED_SANDSTONE.getDefaultState();

   protected void addRoom(long var1, int var3, int var4, ChunkPrimer var5, double var6, double var8, double var10) {
      this.addTunnel(var1, var3, var4, var5, var6, var8, var10, 1.0F + this.rand.nextFloat() * 6.0F, 0.0F, 0.0F, -1, -1, 0.5D);
   }

   protected void addTunnel(long var1, int var3, int var4, ChunkPrimer var5, double var6, double var8, double var10, float var12, float var13, float var14, int var15, int var16, double var17) {
      double var19 = (double)(var3 * 16 + 8);
      double var21 = (double)(var4 * 16 + 8);
      float var23 = 0.0F;
      float var24 = 0.0F;
      Random var25 = new Random(var1);
      if (var16 <= 0) {
         int var26 = this.range * 16 - 16;
         var16 = var26 - var25.nextInt(var26 / 4);
      }

      boolean var66 = false;
      if (var15 == -1) {
         var15 = var16 / 2;
         var66 = true;
      }

      int var27 = var25.nextInt(var16 / 2) + var16 / 4;

      for(boolean var28 = var25.nextInt(6) == 0; var15 < var16; ++var15) {
         double var29 = 1.5D + (double)(MathHelper.sin((float)var15 * 3.1415927F / (float)var16) * var12);
         double var31 = var29 * var17;
         float var33 = MathHelper.cos(var14);
         float var34 = MathHelper.sin(var14);
         var6 += (double)(MathHelper.cos(var13) * var33);
         var8 += (double)var34;
         var10 += (double)(MathHelper.sin(var13) * var33);
         if (var28) {
            var14 = var14 * 0.92F;
         } else {
            var14 = var14 * 0.7F;
         }

         var14 = var14 + var24 * 0.1F;
         var13 += var23 * 0.1F;
         var24 = var24 * 0.9F;
         var23 = var23 * 0.75F;
         var24 = var24 + (var25.nextFloat() - var25.nextFloat()) * var25.nextFloat() * 2.0F;
         var23 = var23 + (var25.nextFloat() - var25.nextFloat()) * var25.nextFloat() * 4.0F;
         if (!var66 && var15 == var27 && var12 > 1.0F && var16 > 0) {
            this.addTunnel(var25.nextLong(), var3, var4, var5, var6, var8, var10, var25.nextFloat() * 0.5F + 0.5F, var13 - 1.5707964F, var14 / 3.0F, var15, var16, 1.0D);
            this.addTunnel(var25.nextLong(), var3, var4, var5, var6, var8, var10, var25.nextFloat() * 0.5F + 0.5F, var13 + 1.5707964F, var14 / 3.0F, var15, var16, 1.0D);
            return;
         }

         if (var66 || var25.nextInt(4) != 0) {
            double var35 = var6 - var19;
            double var37 = var10 - var21;
            double var39 = (double)(var16 - var15);
            double var41 = (double)(var12 + 2.0F + 16.0F);
            if (var35 * var35 + var37 * var37 - var39 * var39 > var41 * var41) {
               return;
            }

            if (var6 >= var19 - 16.0D - var29 * 2.0D && var10 >= var21 - 16.0D - var29 * 2.0D && var6 <= var19 + 16.0D + var29 * 2.0D && var10 <= var21 + 16.0D + var29 * 2.0D) {
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
                           if (this.isOceanBlock(var5, var50, var52, var51, var3, var4)) {
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
                  new BlockPos.MutableBlockPos();

                  for(int var67 = var43; var67 < var44; ++var67) {
                     double var68 = ((double)(var67 + var3 * 16) + 0.5D - var6) / var29;

                     for(int var54 = var47; var54 < var48; ++var54) {
                        double var55 = ((double)(var54 + var4 * 16) + 0.5D - var10) / var29;
                        boolean var57 = false;
                        if (var68 * var68 + var55 * var55 < 1.0D) {
                           for(int var58 = var46; var58 > var45; --var58) {
                              double var59 = ((double)(var58 - 1) + 0.5D - var8) / var31;
                              if (var59 > -0.7D && var68 * var68 + var59 * var59 + var55 * var55 < 1.0D) {
                                 IBlockState var61 = var5.getBlockState(var67, var58, var54);
                                 IBlockState var62 = (IBlockState)Objects.firstNonNull(var5.getBlockState(var67, var58 + 1, var54), BLK_AIR);
                                 if (this.isTopBlock(var5, var67, var58, var54, var3, var4)) {
                                    var57 = true;
                                 }

                                 this.digBlock(var5, var67, var58, var54, var3, var4, var57, var61, var62);
                              }
                           }
                        }
                     }
                  }

                  if (var66) {
                     break;
                  }
               }
            }
         }
      }

   }

   protected boolean canReplaceBlock(IBlockState var1, IBlockState var2) {
      return var1.getBlock() == Blocks.STONE ? true : (var1.getBlock() == Blocks.DIRT ? true : (var1.getBlock() == Blocks.GRASS ? true : (var1.getBlock() == Blocks.HARDENED_CLAY ? true : (var1.getBlock() == Blocks.STAINED_HARDENED_CLAY ? true : (var1.getBlock() == Blocks.SANDSTONE ? true : (var1.getBlock() == Blocks.RED_SANDSTONE ? true : (var1.getBlock() == Blocks.MYCELIUM ? true : (var1.getBlock() == Blocks.SNOW_LAYER ? true : (var1.getBlock() == Blocks.SAND || var1.getBlock() == Blocks.GRAVEL) && var2.getMaterial() != Material.WATER))))))));
   }

   protected void recursiveGenerate(World var1, int var2, int var3, int var4, int var5, ChunkPrimer var6) {
      int var7 = this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(15) + 1) + 1);
      if (this.rand.nextInt(7) != 0) {
         var7 = 0;
      }

      for(int var8 = 0; var8 < var7; ++var8) {
         double var9 = (double)(var2 * 16 + this.rand.nextInt(16));
         double var11 = (double)this.rand.nextInt(this.rand.nextInt(120) + 8);
         double var13 = (double)(var3 * 16 + this.rand.nextInt(16));
         int var15 = 1;
         if (this.rand.nextInt(4) == 0) {
            this.addRoom(this.rand.nextLong(), var4, var5, var6, var9, var11, var13);
            var15 += this.rand.nextInt(4);
         }

         for(int var16 = 0; var16 < var15; ++var16) {
            float var17 = this.rand.nextFloat() * 6.2831855F;
            float var18 = (this.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
            float var19 = this.rand.nextFloat() * 2.0F + this.rand.nextFloat();
            if (this.rand.nextInt(10) == 0) {
               var19 *= this.rand.nextFloat() * this.rand.nextFloat() * 3.0F + 1.0F;
            }

            this.addTunnel(this.rand.nextLong(), var4, var5, var6, var9, var11, var13, var19, var17, var18, 0, 0, 1.0D);
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
      } else {
         return var1 == Biomes.DESERT;
      }
   }

   private boolean isTopBlock(ChunkPrimer var1, int var2, int var3, int var4, int var5, int var6) {
      Biome var7 = this.world.getBiome(new BlockPos(var2 + var5 * 16, 0, var4 + var6 * 16));
      IBlockState var8 = var1.getBlockState(var2, var3, var4);
      return this.isExceptionBiome(var7) ? var8.getBlock() == Blocks.GRASS : var8.getBlock() == var7.topBlock;
   }

   protected void digBlock(ChunkPrimer var1, int var2, int var3, int var4, int var5, int var6, boolean var7, IBlockState var8, IBlockState var9) {
      Biome var10 = this.world.getBiome(new BlockPos(var2 + var5 * 16, 0, var4 + var6 * 16));
      IBlockState var11 = var10.topBlock;
      IBlockState var12 = var10.fillerBlock;
      if (this.canReplaceBlock(var8, var9) || var8.getBlock() == var11.getBlock() || var8.getBlock() == var12.getBlock()) {
         if (var3 - 1 < 10) {
            var1.setBlockState(var2, var3, var4, BLK_LAVA);
         } else {
            var1.setBlockState(var2, var3, var4, BLK_AIR);
            if (var7 && var1.getBlockState(var2, var3 - 1, var4).getBlock() == var12.getBlock()) {
               var1.setBlockState(var2, var3 - 1, var4, var11.getBlock().getDefaultState());
            }
         }
      }

   }
}
