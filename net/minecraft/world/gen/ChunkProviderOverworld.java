package net.minecraft.world.gen;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureOceanMonument;

public class ChunkProviderOverworld implements IChunkGenerator {
   protected static final IBlockState STONE = Blocks.STONE.getDefaultState();
   private final Random rand;
   private final NoiseGeneratorOctaves minLimitPerlinNoise;
   private final NoiseGeneratorOctaves maxLimitPerlinNoise;
   private final NoiseGeneratorOctaves mainPerlinNoise;
   private final NoiseGeneratorPerlin surfaceNoise;
   public NoiseGeneratorOctaves scaleNoise;
   public NoiseGeneratorOctaves depthNoise;
   public NoiseGeneratorOctaves forestNoise;
   private final World world;
   private final boolean mapFeaturesEnabled;
   private final WorldType terrainType;
   private final double[] heightMap;
   private final float[] biomeWeights;
   private ChunkProviderSettings settings;
   private IBlockState oceanBlock = Blocks.WATER.getDefaultState();
   private double[] depthBuffer = new double[256];
   private final MapGenBase caveGenerator = new MapGenCaves();
   private final MapGenStronghold strongholdGenerator = new MapGenStronghold();
   private final MapGenVillage villageGenerator = new MapGenVillage();
   private final MapGenMineshaft mineshaftGenerator = new MapGenMineshaft();
   private final MapGenScatteredFeature scatteredFeatureGenerator = new MapGenScatteredFeature();
   private final MapGenBase ravineGenerator = new MapGenRavine();
   private final StructureOceanMonument oceanMonumentGenerator = new StructureOceanMonument();
   private Biome[] biomesForGeneration;
   double[] mainNoiseRegion;
   double[] minLimitRegion;
   double[] maxLimitRegion;
   double[] depthRegion;

   public ChunkProviderOverworld(World var1, long var2, boolean var4, String var5) {
      this.world = var1;
      this.mapFeaturesEnabled = var4;
      this.terrainType = var1.getWorldInfo().getTerrainType();
      this.rand = new Random(var2);
      this.minLimitPerlinNoise = new NoiseGeneratorOctaves(this.rand, 16);
      this.maxLimitPerlinNoise = new NoiseGeneratorOctaves(this.rand, 16);
      this.mainPerlinNoise = new NoiseGeneratorOctaves(this.rand, 8);
      this.surfaceNoise = new NoiseGeneratorPerlin(this.rand, 4);
      this.scaleNoise = new NoiseGeneratorOctaves(this.rand, 10);
      this.depthNoise = new NoiseGeneratorOctaves(this.rand, 16);
      this.forestNoise = new NoiseGeneratorOctaves(this.rand, 8);
      this.heightMap = new double[825];
      this.biomeWeights = new float[25];

      for(int var6 = -2; var6 <= 2; ++var6) {
         for(int var7 = -2; var7 <= 2; ++var7) {
            float var8 = 10.0F / MathHelper.sqrt((float)(var6 * var6 + var7 * var7) + 0.2F);
            this.biomeWeights[var6 + 2 + (var7 + 2) * 5] = var8;
         }
      }

      if (var5 != null) {
         this.settings = ChunkProviderSettings.Factory.jsonToFactory(var5).build();
         this.oceanBlock = this.settings.useLavaOceans ? Blocks.LAVA.getDefaultState() : Blocks.WATER.getDefaultState();
         var1.setSeaLevel(this.settings.seaLevel);
      }

   }

   public void setBlocksInChunk(int var1, int var2, ChunkPrimer var3) {
      this.biomesForGeneration = this.world.getBiomeProvider().getBiomesForGeneration(this.biomesForGeneration, var1 * 4 - 2, var2 * 4 - 2, 10, 10);
      this.generateHeightmap(var1 * 4, 0, var2 * 4);

      for(int var4 = 0; var4 < 4; ++var4) {
         int var5 = var4 * 5;
         int var6 = (var4 + 1) * 5;

         for(int var7 = 0; var7 < 4; ++var7) {
            int var8 = (var5 + var7) * 33;
            int var9 = (var5 + var7 + 1) * 33;
            int var10 = (var6 + var7) * 33;
            int var11 = (var6 + var7 + 1) * 33;

            for(int var12 = 0; var12 < 32; ++var12) {
               double var13 = this.heightMap[var8 + var12];
               double var15 = this.heightMap[var9 + var12];
               double var17 = this.heightMap[var10 + var12];
               double var19 = this.heightMap[var11 + var12];
               double var21 = (this.heightMap[var8 + var12 + 1] - var13) * 0.125D;
               double var23 = (this.heightMap[var9 + var12 + 1] - var15) * 0.125D;
               double var25 = (this.heightMap[var10 + var12 + 1] - var17) * 0.125D;
               double var27 = (this.heightMap[var11 + var12 + 1] - var19) * 0.125D;

               for(int var29 = 0; var29 < 8; ++var29) {
                  double var30 = var13;
                  double var32 = var15;
                  double var34 = (var17 - var13) * 0.25D;
                  double var36 = (var19 - var15) * 0.25D;

                  for(int var38 = 0; var38 < 4; ++var38) {
                     double var39 = (var32 - var30) * 0.25D;
                     double var41 = var30 - var39;

                     for(int var43 = 0; var43 < 4; ++var43) {
                        if ((var41 += var39) > 0.0D) {
                           var3.setBlockState(var4 * 4 + var38, var12 * 8 + var29, var7 * 4 + var43, STONE);
                        } else if (var12 * 8 + var29 < this.settings.seaLevel) {
                           var3.setBlockState(var4 * 4 + var38, var12 * 8 + var29, var7 * 4 + var43, this.oceanBlock);
                        }
                     }

                     var30 += var34;
                     var32 += var36;
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

   public void replaceBiomeBlocks(int var1, int var2, ChunkPrimer var3, Biome[] var4) {
      this.depthBuffer = this.surfaceNoise.getRegion(this.depthBuffer, (double)(var1 * 16), (double)(var2 * 16), 16, 16, 0.0625D, 0.0625D, 1.0D);

      for(int var5 = 0; var5 < 16; ++var5) {
         for(int var6 = 0; var6 < 16; ++var6) {
            Biome var7 = var4[var6 + var5 * 16];
            var7.genTerrainBlocks(this.world, this.rand, var3, var1 * 16 + var5, var2 * 16 + var6, this.depthBuffer[var6 + var5 * 16]);
         }
      }

   }

   public Chunk provideChunk(int var1, int var2) {
      this.rand.setSeed((long)var1 * 341873128712L + (long)var2 * 132897987541L);
      ChunkPrimer var3 = new ChunkPrimer();
      this.setBlocksInChunk(var1, var2, var3);
      this.biomesForGeneration = this.world.getBiomeProvider().getBiomes(this.biomesForGeneration, var1 * 16, var2 * 16, 16, 16);
      this.replaceBiomeBlocks(var1, var2, var3, this.biomesForGeneration);
      if (this.settings.useCaves) {
         this.caveGenerator.generate(this.world, var1, var2, var3);
      }

      if (this.settings.useRavines) {
         this.ravineGenerator.generate(this.world, var1, var2, var3);
      }

      if (this.mapFeaturesEnabled) {
         if (this.settings.useMineShafts) {
            this.mineshaftGenerator.generate(this.world, var1, var2, var3);
         }

         if (this.settings.useVillages) {
            this.villageGenerator.generate(this.world, var1, var2, var3);
         }

         if (this.settings.useStrongholds) {
            this.strongholdGenerator.generate(this.world, var1, var2, var3);
         }

         if (this.settings.useTemples) {
            this.scatteredFeatureGenerator.generate(this.world, var1, var2, var3);
         }

         if (this.settings.useMonuments) {
            this.oceanMonumentGenerator.generate(this.world, var1, var2, var3);
         }
      }

      Chunk var4 = new Chunk(this.world, var3, var1, var2);
      byte[] var5 = var4.getBiomeArray();

      for(int var6 = 0; var6 < var5.length; ++var6) {
         var5[var6] = (byte)Biome.getIdForBiome(this.biomesForGeneration[var6]);
      }

      var4.generateSkylightMap();
      return var4;
   }

   private void generateHeightmap(int var1, int var2, int var3) {
      this.depthRegion = this.depthNoise.generateNoiseOctaves(this.depthRegion, var1, var3, 5, 5, (double)this.settings.depthNoiseScaleX, (double)this.settings.depthNoiseScaleZ, (double)this.settings.depthNoiseScaleExponent);
      float var4 = this.settings.coordinateScale;
      float var5 = this.settings.heightScale;
      this.mainNoiseRegion = this.mainPerlinNoise.generateNoiseOctaves(this.mainNoiseRegion, var1, var2, var3, 5, 33, 5, (double)(var4 / this.settings.mainNoiseScaleX), (double)(var5 / this.settings.mainNoiseScaleY), (double)(var4 / this.settings.mainNoiseScaleZ));
      this.minLimitRegion = this.minLimitPerlinNoise.generateNoiseOctaves(this.minLimitRegion, var1, var2, var3, 5, 33, 5, (double)var4, (double)var5, (double)var4);
      this.maxLimitRegion = this.maxLimitPerlinNoise.generateNoiseOctaves(this.maxLimitRegion, var1, var2, var3, 5, 33, 5, (double)var4, (double)var5, (double)var4);
      int var6 = 0;
      int var7 = 0;

      for(int var8 = 0; var8 < 5; ++var8) {
         for(int var9 = 0; var9 < 5; ++var9) {
            float var10 = 0.0F;
            float var11 = 0.0F;
            float var12 = 0.0F;
            Biome var13 = this.biomesForGeneration[var8 + 2 + (var9 + 2) * 10];

            for(int var14 = -2; var14 <= 2; ++var14) {
               for(int var15 = -2; var15 <= 2; ++var15) {
                  Biome var16 = this.biomesForGeneration[var8 + var14 + 2 + (var9 + var15 + 2) * 10];
                  float var17 = this.settings.biomeDepthOffSet + var16.getBaseHeight() * this.settings.biomeDepthWeight;
                  float var18 = this.settings.biomeScaleOffset + var16.getHeightVariation() * this.settings.biomeScaleWeight;
                  if (this.terrainType == WorldType.AMPLIFIED && var17 > 0.0F) {
                     var17 = 1.0F + var17 * 2.0F;
                     var18 = 1.0F + var18 * 4.0F;
                  }

                  if (var17 < -1.8F) {
                     var17 = -1.8F;
                  }

                  float var19 = this.biomeWeights[var14 + 2 + (var15 + 2) * 5] / (var17 + 2.0F);
                  if (var16.getBaseHeight() > var13.getBaseHeight()) {
                     var19 /= 2.0F;
                  }

                  var10 += var18 * var19;
                  var11 += var17 * var19;
                  var12 += var19;
               }
            }

            var10 = var10 / var12;
            var11 = var11 / var12;
            var10 = var10 * 0.9F + 0.1F;
            var11 = (var11 * 4.0F - 1.0F) / 8.0F;
            double var20 = this.depthRegion[var7] / 8000.0D;
            if (var20 < 0.0D) {
               var20 = -var20 * 0.3D;
            }

            var20 = var20 * 3.0D - 2.0D;
            if (var20 < 0.0D) {
               var20 = var20 / 2.0D;
               if (var20 < -1.0D) {
                  var20 = -1.0D;
               }

               var20 = var20 / 1.4D;
               var20 = var20 / 2.0D;
            } else {
               if (var20 > 1.0D) {
                  var20 = 1.0D;
               }

               var20 = var20 / 8.0D;
            }

            ++var7;
            double var22 = (double)var11;
            double var24 = (double)var10;
            var22 = var22 + var20 * 0.2D;
            var22 = var22 * (double)this.settings.baseSize / 8.0D;
            double var26 = (double)this.settings.baseSize + var22 * 4.0D;

            for(int var28 = 0; var28 < 33; ++var28) {
               double var29 = ((double)var28 - var26) * (double)this.settings.stretchY * 128.0D / 256.0D / var24;
               if (var29 < 0.0D) {
                  var29 *= 4.0D;
               }

               double var31 = this.minLimitRegion[var6] / (double)this.settings.lowerLimitScale;
               double var33 = this.maxLimitRegion[var6] / (double)this.settings.upperLimitScale;
               double var35 = (this.mainNoiseRegion[var6] / 10.0D + 1.0D) / 2.0D;
               double var37 = MathHelper.clampedLerp(var31, var33, var35) - var29;
               if (var28 > 29) {
                  double var39 = (double)((float)(var28 - 29) / 3.0F);
                  var37 = var37 * (1.0D - var39) + -10.0D * var39;
               }

               this.heightMap[var6] = var37;
               ++var6;
            }
         }
      }

   }

   public void populate(int var1, int var2) {
      BlockFalling.fallInstantly = true;
      int var3 = var1 * 16;
      int var4 = var2 * 16;
      BlockPos var5 = new BlockPos(var3, 0, var4);
      Biome var6 = this.world.getBiome(var5.add(16, 0, 16));
      this.rand.setSeed(this.world.getSeed());
      long var7 = this.rand.nextLong() / 2L * 2L + 1L;
      long var9 = this.rand.nextLong() / 2L * 2L + 1L;
      this.rand.setSeed((long)var1 * var7 + (long)var2 * var9 ^ this.world.getSeed());
      boolean var11 = false;
      ChunkPos var12 = new ChunkPos(var1, var2);
      if (this.mapFeaturesEnabled) {
         if (this.settings.useMineShafts) {
            this.mineshaftGenerator.generateStructure(this.world, this.rand, var12);
         }

         if (this.settings.useVillages) {
            var11 = this.villageGenerator.generateStructure(this.world, this.rand, var12);
         }

         if (this.settings.useStrongholds) {
            this.strongholdGenerator.generateStructure(this.world, this.rand, var12);
         }

         if (this.settings.useTemples) {
            this.scatteredFeatureGenerator.generateStructure(this.world, this.rand, var12);
         }

         if (this.settings.useMonuments) {
            this.oceanMonumentGenerator.generateStructure(this.world, this.rand, var12);
         }
      }

      if (var6 != Biomes.DESERT && var6 != Biomes.DESERT_HILLS && this.settings.useWaterLakes && !var11 && this.rand.nextInt(this.settings.waterLakeChance) == 0) {
         int var13 = this.rand.nextInt(16) + 8;
         int var14 = this.rand.nextInt(256);
         int var15 = this.rand.nextInt(16) + 8;
         (new WorldGenLakes(Blocks.WATER)).generate(this.world, this.rand, var5.add(var13, var14, var15));
      }

      if (!var11 && this.rand.nextInt(this.settings.lavaLakeChance / 10) == 0 && this.settings.useLavaLakes) {
         int var19 = this.rand.nextInt(16) + 8;
         int var22 = this.rand.nextInt(this.rand.nextInt(248) + 8);
         int var25 = this.rand.nextInt(16) + 8;
         if (var22 < this.world.getSeaLevel() || this.rand.nextInt(this.settings.lavaLakeChance / 8) == 0) {
            (new WorldGenLakes(Blocks.LAVA)).generate(this.world, this.rand, var5.add(var19, var22, var25));
         }
      }

      if (this.settings.useDungeons) {
         for(int var20 = 0; var20 < this.settings.dungeonChance; ++var20) {
            int var23 = this.rand.nextInt(16) + 8;
            int var26 = this.rand.nextInt(256);
            int var16 = this.rand.nextInt(16) + 8;
            (new WorldGenDungeons()).generate(this.world, this.rand, var5.add(var23, var26, var16));
         }
      }

      var6.decorate(this.world, this.rand, new BlockPos(var3, 0, var4));
      WorldEntitySpawner.performWorldGenSpawning(this.world, var6, var3 + 8, var4 + 8, 16, 16, this.rand);
      var5 = var5.add(8, 0, 8);

      for(int var21 = 0; var21 < 16; ++var21) {
         for(int var24 = 0; var24 < 16; ++var24) {
            BlockPos var27 = this.world.getPrecipitationHeight(var5.add(var21, 0, var24));
            BlockPos var17 = var27.down();
            if (this.world.canBlockFreezeWater(var17)) {
               this.world.setBlockState(var17, Blocks.ICE.getDefaultState(), 2);
            }

            if (this.world.canSnowAt(var27, true)) {
               this.world.setBlockState(var27, Blocks.SNOW_LAYER.getDefaultState(), 2);
            }
         }
      }

      BlockFalling.fallInstantly = false;
   }

   public boolean generateStructures(Chunk var1, int var2, int var3) {
      boolean var4 = false;
      if (this.settings.useMonuments && this.mapFeaturesEnabled && var1.getInhabitedTime() < 3600L) {
         var4 |= this.oceanMonumentGenerator.generateStructure(this.world, this.rand, new ChunkPos(var2, var3));
      }

      return var4;
   }

   public List getPossibleCreatures(EnumCreatureType var1, BlockPos var2) {
      Biome var3 = this.world.getBiome(var2);
      if (this.mapFeaturesEnabled) {
         if (var1 == EnumCreatureType.MONSTER && this.scatteredFeatureGenerator.isSwampHut(var2)) {
            return this.scatteredFeatureGenerator.getScatteredFeatureSpawnList();
         }

         if (var1 == EnumCreatureType.MONSTER && this.settings.useMonuments && this.oceanMonumentGenerator.isPositionInStructure(this.world, var2)) {
            return this.oceanMonumentGenerator.getScatteredFeatureSpawnList();
         }
      }

      return var3.getSpawnableList(var1);
   }

   @Nullable
   public BlockPos getStrongholdGen(World var1, String var2, BlockPos var3) {
      return "Stronghold".equals(var2) && this.strongholdGenerator != null ? this.strongholdGenerator.getClosestStrongholdPos(var1, var3) : null;
   }

   public void recreateStructures(Chunk var1, int var2, int var3) {
      if (this.mapFeaturesEnabled) {
         if (this.settings.useMineShafts) {
            this.mineshaftGenerator.generate(this.world, var2, var3, (ChunkPrimer)null);
         }

         if (this.settings.useVillages) {
            this.villageGenerator.generate(this.world, var2, var3, (ChunkPrimer)null);
         }

         if (this.settings.useStrongholds) {
            this.strongholdGenerator.generate(this.world, var2, var3, (ChunkPrimer)null);
         }

         if (this.settings.useTemples) {
            this.scatteredFeatureGenerator.generate(this.world, var2, var3, (ChunkPrimer)null);
         }

         if (this.settings.useMonuments) {
            this.oceanMonumentGenerator.generate(this.world, var2, var3, (ChunkPrimer)null);
         }
      }

   }
}
