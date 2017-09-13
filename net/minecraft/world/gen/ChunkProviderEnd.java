package net.minecraft.world.gen;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockChorusFlower;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenEndIsland;
import net.minecraft.world.gen.structure.MapGenEndCity;

public class ChunkProviderEnd implements IChunkGenerator {
   private final Random rand;
   protected static final IBlockState END_STONE = Blocks.END_STONE.getDefaultState();
   protected static final IBlockState AIR = Blocks.AIR.getDefaultState();
   private final NoiseGeneratorOctaves lperlinNoise1;
   private final NoiseGeneratorOctaves lperlinNoise2;
   private final NoiseGeneratorOctaves perlinNoise1;
   public NoiseGeneratorOctaves noiseGen5;
   public NoiseGeneratorOctaves noiseGen6;
   private final World world;
   private final boolean mapFeaturesEnabled;
   private final MapGenEndCity endCityGen = new MapGenEndCity(this);
   private final NoiseGeneratorSimplex islandNoise;
   private double[] buffer;
   private Biome[] biomesForGeneration;
   double[] pnr;
   double[] ar;
   double[] br;
   private final WorldGenEndIsland endIslands = new WorldGenEndIsland();

   public ChunkProviderEnd(World var1, boolean var2, long var3) {
      this.world = var1;
      this.mapFeaturesEnabled = var2;
      this.rand = new Random(var3);
      this.lperlinNoise1 = new NoiseGeneratorOctaves(this.rand, 16);
      this.lperlinNoise2 = new NoiseGeneratorOctaves(this.rand, 16);
      this.perlinNoise1 = new NoiseGeneratorOctaves(this.rand, 8);
      this.noiseGen5 = new NoiseGeneratorOctaves(this.rand, 10);
      this.noiseGen6 = new NoiseGeneratorOctaves(this.rand, 16);
      this.islandNoise = new NoiseGeneratorSimplex(this.rand);
   }

   public void setBlocksInChunk(int var1, int var2, ChunkPrimer var3) {
      boolean var4 = true;
      boolean var5 = true;
      boolean var6 = true;
      boolean var7 = true;
      this.buffer = this.getHeights(this.buffer, var1 * 2, 0, var2 * 2, 3, 33, 3);

      for(int var8 = 0; var8 < 2; ++var8) {
         for(int var9 = 0; var9 < 2; ++var9) {
            for(int var10 = 0; var10 < 32; ++var10) {
               double var11 = 0.25D;
               double var13 = this.buffer[((var8 + 0) * 3 + var9 + 0) * 33 + var10 + 0];
               double var15 = this.buffer[((var8 + 0) * 3 + var9 + 1) * 33 + var10 + 0];
               double var17 = this.buffer[((var8 + 1) * 3 + var9 + 0) * 33 + var10 + 0];
               double var19 = this.buffer[((var8 + 1) * 3 + var9 + 1) * 33 + var10 + 0];
               double var21 = (this.buffer[((var8 + 0) * 3 + var9 + 0) * 33 + var10 + 1] - var13) * 0.25D;
               double var23 = (this.buffer[((var8 + 0) * 3 + var9 + 1) * 33 + var10 + 1] - var15) * 0.25D;
               double var25 = (this.buffer[((var8 + 1) * 3 + var9 + 0) * 33 + var10 + 1] - var17) * 0.25D;
               double var27 = (this.buffer[((var8 + 1) * 3 + var9 + 1) * 33 + var10 + 1] - var19) * 0.25D;

               for(int var29 = 0; var29 < 4; ++var29) {
                  double var30 = 0.125D;
                  double var32 = var13;
                  double var34 = var15;
                  double var36 = (var17 - var13) * 0.125D;
                  double var38 = (var19 - var15) * 0.125D;

                  for(int var40 = 0; var40 < 8; ++var40) {
                     double var41 = 0.125D;
                     double var43 = var32;
                     double var45 = (var34 - var32) * 0.125D;

                     for(int var47 = 0; var47 < 8; ++var47) {
                        IBlockState var48 = AIR;
                        if (var43 > 0.0D) {
                           var48 = END_STONE;
                        }

                        int var49 = var40 + var8 * 8;
                        int var50 = var29 + var10 * 4;
                        int var51 = var47 + var9 * 8;
                        var3.setBlockState(var49, var50, var51, var48);
                        var43 += var45;
                     }

                     var32 += var36;
                     var34 += var38;
                  }

                  var13 += var21;
                  var15 += var23;
                  var17 += var25;
                  var19 += var27;
               }
            }
         }
      }

   }

   public void buildSurfaces(ChunkPrimer var1) {
      for(int var2 = 0; var2 < 16; ++var2) {
         for(int var3 = 0; var3 < 16; ++var3) {
            boolean var4 = true;
            int var5 = -1;
            IBlockState var6 = END_STONE;
            IBlockState var7 = END_STONE;

            for(int var8 = 127; var8 >= 0; --var8) {
               IBlockState var9 = var1.getBlockState(var2, var8, var3);
               if (var9.getMaterial() == Material.AIR) {
                  var5 = -1;
               } else if (var9.getBlock() == Blocks.STONE) {
                  if (var5 == -1) {
                     var5 = 1;
                     if (var8 >= 0) {
                        var1.setBlockState(var2, var8, var3, var6);
                     } else {
                        var1.setBlockState(var2, var8, var3, var7);
                     }
                  } else if (var5 > 0) {
                     --var5;
                     var1.setBlockState(var2, var8, var3, var7);
                  }
               }
            }
         }
      }

   }

   public Chunk provideChunk(int var1, int var2) {
      this.rand.setSeed((long)var1 * 341873128712L + (long)var2 * 132897987541L);
      ChunkPrimer var3 = new ChunkPrimer();
      this.biomesForGeneration = this.world.getBiomeProvider().getBiomes(this.biomesForGeneration, var1 * 16, var2 * 16, 16, 16);
      this.setBlocksInChunk(var1, var2, var3);
      this.buildSurfaces(var3);
      if (this.mapFeaturesEnabled) {
         this.endCityGen.generate(this.world, var1, var2, var3);
      }

      Chunk var4 = new Chunk(this.world, var3, var1, var2);
      byte[] var5 = var4.getBiomeArray();

      for(int var6 = 0; var6 < var5.length; ++var6) {
         var5[var6] = (byte)Biome.getIdForBiome(this.biomesForGeneration[var6]);
      }

      var4.generateSkylightMap();
      return var4;
   }

   private float getIslandHeightValue(int var1, int var2, int var3, int var4) {
      float var5 = (float)(var1 * 2 + var3);
      float var6 = (float)(var2 * 2 + var4);
      float var7 = 100.0F - MathHelper.sqrt(var5 * var5 + var6 * var6) * 8.0F;
      if (var7 > 80.0F) {
         var7 = 80.0F;
      }

      if (var7 < -100.0F) {
         var7 = -100.0F;
      }

      for(int var8 = -12; var8 <= 12; ++var8) {
         for(int var9 = -12; var9 <= 12; ++var9) {
            long var10 = (long)(var1 + var8);
            long var12 = (long)(var2 + var9);
            if (var10 * var10 + var12 * var12 > 4096L && this.islandNoise.getValue((double)var10, (double)var12) < -0.8999999761581421D) {
               float var14 = (MathHelper.abs((float)var10) * 3439.0F + MathHelper.abs((float)var12) * 147.0F) % 13.0F + 9.0F;
               var5 = (float)(var3 - var8 * 2);
               var6 = (float)(var4 - var9 * 2);
               float var15 = 100.0F - MathHelper.sqrt(var5 * var5 + var6 * var6) * var14;
               if (var15 > 80.0F) {
                  var15 = 80.0F;
               }

               if (var15 < -100.0F) {
                  var15 = -100.0F;
               }

               if (var15 > var7) {
                  var7 = var15;
               }
            }
         }
      }

      return var7;
   }

   public boolean isIslandChunk(int var1, int var2) {
      return (long)var1 * (long)var1 + (long)var2 * (long)var2 > 4096L && this.getIslandHeightValue(var1, var2, 1, 1) >= 0.0F;
   }

   private double[] getHeights(double[] var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      if (var1 == null) {
         var1 = new double[var5 * var6 * var7];
      }

      double var8 = 684.412D;
      double var10 = 684.412D;
      var8 = var8 * 2.0D;
      this.pnr = this.perlinNoise1.generateNoiseOctaves(this.pnr, var2, var3, var4, var5, var6, var7, var8 / 80.0D, 4.277575000000001D, var8 / 80.0D);
      this.ar = this.lperlinNoise1.generateNoiseOctaves(this.ar, var2, var3, var4, var5, var6, var7, var8, 684.412D, var8);
      this.br = this.lperlinNoise2.generateNoiseOctaves(this.br, var2, var3, var4, var5, var6, var7, var8, 684.412D, var8);
      int var12 = var2 / 2;
      int var13 = var4 / 2;
      int var14 = 0;

      for(int var15 = 0; var15 < var5; ++var15) {
         for(int var16 = 0; var16 < var7; ++var16) {
            float var17 = this.getIslandHeightValue(var12, var13, var15, var16);

            for(int var18 = 0; var18 < var6; ++var18) {
               double var19 = this.ar[var14] / 512.0D;
               double var21 = this.br[var14] / 512.0D;
               double var23 = (this.pnr[var14] / 10.0D + 1.0D) / 2.0D;
               double var25;
               if (var23 < 0.0D) {
                  var25 = var19;
               } else if (var23 > 1.0D) {
                  var25 = var21;
               } else {
                  var25 = var19 + (var21 - var19) * var23;
               }

               var25 = var25 - 8.0D;
               var25 = var25 + (double)var17;
               byte var27 = 2;
               if (var18 > var6 / 2 - var27) {
                  double var28 = (double)((float)(var18 - (var6 / 2 - var27)) / 64.0F);
                  var28 = MathHelper.clamp(var28, 0.0D, 1.0D);
                  var25 = var25 * (1.0D - var28) + -3000.0D * var28;
               }

               var27 = 8;
               if (var18 < var27) {
                  double var35 = (double)((float)(var27 - var18) / ((float)var27 - 1.0F));
                  var25 = var25 * (1.0D - var35) + -30.0D * var35;
               }

               var1[var14] = var25;
               ++var14;
            }
         }
      }

      return var1;
   }

   public void populate(int var1, int var2) {
      BlockFalling.fallInstantly = true;
      BlockPos var3 = new BlockPos(var1 * 16, 0, var2 * 16);
      if (this.mapFeaturesEnabled) {
         this.endCityGen.generateStructure(this.world, this.rand, new ChunkPos(var1, var2));
      }

      this.world.getBiome(var3.add(16, 0, 16)).decorate(this.world, this.world.rand, var3);
      long var4 = (long)var1 * (long)var1 + (long)var2 * (long)var2;
      if (var4 > 4096L) {
         float var6 = this.getIslandHeightValue(var1, var2, 1, 1);
         if (var6 < -20.0F && this.rand.nextInt(14) == 0) {
            this.endIslands.generate(this.world, this.rand, var3.add(this.rand.nextInt(16) + 8, 55 + this.rand.nextInt(16), this.rand.nextInt(16) + 8));
            if (this.rand.nextInt(4) == 0) {
               this.endIslands.generate(this.world, this.rand, var3.add(this.rand.nextInt(16) + 8, 55 + this.rand.nextInt(16), this.rand.nextInt(16) + 8));
            }
         }

         if (this.getIslandHeightValue(var1, var2, 1, 1) > 40.0F) {
            int var7 = this.rand.nextInt(5);

            for(int var8 = 0; var8 < var7; ++var8) {
               int var9 = this.rand.nextInt(16) + 8;
               int var10 = this.rand.nextInt(16) + 8;
               int var11 = this.world.getHeight(var3.add(var9, 0, var10)).getY();
               if (var11 > 0) {
                  int var12 = var11 - 1;
                  if (this.world.isAirBlock(var3.add(var9, var12 + 1, var10)) && this.world.getBlockState(var3.add(var9, var12, var10)).getBlock() == Blocks.END_STONE) {
                     BlockChorusFlower.generatePlant(this.world, var3.add(var9, var12 + 1, var10), this.rand, 8);
                  }
               }
            }
         }
      }

      BlockFalling.fallInstantly = false;
   }

   public boolean generateStructures(Chunk var1, int var2, int var3) {
      return false;
   }

   public List getPossibleCreatures(EnumCreatureType var1, BlockPos var2) {
      return this.world.getBiome(var2).getSpawnableList(var1);
   }

   @Nullable
   public BlockPos getStrongholdGen(World var1, String var2, BlockPos var3) {
      return null;
   }

   public void recreateStructures(Chunk var1, int var2, int var3) {
   }
}
