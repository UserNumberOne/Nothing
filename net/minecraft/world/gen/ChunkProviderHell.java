package net.minecraft.world.gen;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
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
import net.minecraft.world.gen.feature.WorldGenBush;
import net.minecraft.world.gen.feature.WorldGenFire;
import net.minecraft.world.gen.feature.WorldGenGlowStone1;
import net.minecraft.world.gen.feature.WorldGenGlowStone2;
import net.minecraft.world.gen.feature.WorldGenHellLava;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.gen.structure.MapGenNetherBridge;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.event.terraingen.ChunkGeneratorEvent.InitNoiseField;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Post;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Pre;
import net.minecraftforge.event.terraingen.InitMapGenEvent.EventType;
import net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextHell;
import net.minecraftforge.fml.common.eventhandler.Event.Result;

public class ChunkProviderHell implements IChunkGenerator {
   protected static final IBlockState AIR = Blocks.AIR.getDefaultState();
   protected static final IBlockState NETHERRACK = Blocks.NETHERRACK.getDefaultState();
   protected static final IBlockState BEDROCK = Blocks.BEDROCK.getDefaultState();
   protected static final IBlockState LAVA = Blocks.LAVA.getDefaultState();
   protected static final IBlockState GRAVEL = Blocks.GRAVEL.getDefaultState();
   protected static final IBlockState SOUL_SAND = Blocks.SOUL_SAND.getDefaultState();
   private final World world;
   private final boolean generateStructures;
   private final Random rand;
   private double[] slowsandNoise = new double[256];
   private double[] gravelNoise = new double[256];
   private double[] depthBuffer = new double[256];
   private double[] buffer;
   private NoiseGeneratorOctaves lperlinNoise1;
   private NoiseGeneratorOctaves lperlinNoise2;
   private NoiseGeneratorOctaves perlinNoise1;
   private NoiseGeneratorOctaves slowsandGravelNoiseGen;
   private NoiseGeneratorOctaves netherrackExculsivityNoiseGen;
   public NoiseGeneratorOctaves scaleNoise;
   public NoiseGeneratorOctaves depthNoise;
   private final WorldGenFire fireFeature = new WorldGenFire();
   private final WorldGenGlowStone1 lightGemGen = new WorldGenGlowStone1();
   private final WorldGenGlowStone2 hellPortalGen = new WorldGenGlowStone2();
   private final WorldGenerator quartzGen = new WorldGenMinable(Blocks.QUARTZ_ORE.getDefaultState(), 14, BlockMatcher.forBlock(Blocks.NETHERRACK));
   private final WorldGenerator magmaGen = new WorldGenMinable(Blocks.MAGMA.getDefaultState(), 33, BlockMatcher.forBlock(Blocks.NETHERRACK));
   private final WorldGenHellLava lavaTrapGen = new WorldGenHellLava(Blocks.FLOWING_LAVA, true);
   private final WorldGenHellLava hellSpringGen = new WorldGenHellLava(Blocks.FLOWING_LAVA, false);
   private final WorldGenBush brownMushroomFeature = new WorldGenBush(Blocks.BROWN_MUSHROOM);
   private final WorldGenBush redMushroomFeature = new WorldGenBush(Blocks.RED_MUSHROOM);
   private MapGenNetherBridge genNetherBridge = new MapGenNetherBridge();
   private MapGenBase genNetherCaves = new MapGenCavesHell();
   double[] pnr;
   double[] ar;
   double[] br;
   double[] noiseData4;
   double[] dr;

   public ChunkProviderHell(World var1, boolean var2, long var3) {
      this.world = var1;
      this.generateStructures = var2;
      this.rand = new Random(var3);
      this.lperlinNoise1 = new NoiseGeneratorOctaves(this.rand, 16);
      this.lperlinNoise2 = new NoiseGeneratorOctaves(this.rand, 16);
      this.perlinNoise1 = new NoiseGeneratorOctaves(this.rand, 8);
      this.slowsandGravelNoiseGen = new NoiseGeneratorOctaves(this.rand, 4);
      this.netherrackExculsivityNoiseGen = new NoiseGeneratorOctaves(this.rand, 4);
      this.scaleNoise = new NoiseGeneratorOctaves(this.rand, 10);
      this.depthNoise = new NoiseGeneratorOctaves(this.rand, 16);
      var1.setSeaLevel(63);
      ContextHell var5 = new ContextHell(this.lperlinNoise1, this.lperlinNoise2, this.perlinNoise1, this.slowsandGravelNoiseGen, this.netherrackExculsivityNoiseGen, this.scaleNoise, this.depthNoise);
      var5 = (ContextHell)TerrainGen.getModdedNoiseGenerators(var1, this.rand, var5);
      this.lperlinNoise1 = var5.getLPerlin1();
      this.lperlinNoise2 = var5.getLPerlin2();
      this.perlinNoise1 = var5.getPerlin();
      this.slowsandGravelNoiseGen = var5.getPerlin2();
      this.netherrackExculsivityNoiseGen = var5.getPerlin3();
      this.scaleNoise = var5.getScale();
      this.depthNoise = var5.getDepth();
      this.genNetherBridge = (MapGenNetherBridge)TerrainGen.getModdedMapGen(this.genNetherBridge, EventType.NETHER_BRIDGE);
      this.genNetherCaves = TerrainGen.getModdedMapGen(this.genNetherCaves, EventType.NETHER_CAVE);
   }

   public void prepareHeights(int var1, int var2, ChunkPrimer var3) {
      boolean var4 = true;
      int var5 = this.world.getSeaLevel() / 2 + 1;
      boolean var6 = true;
      boolean var7 = true;
      boolean var8 = true;
      this.buffer = this.getHeights(this.buffer, var1 * 4, 0, var2 * 4, 5, 17, 5);

      for(int var9 = 0; var9 < 4; ++var9) {
         for(int var10 = 0; var10 < 4; ++var10) {
            for(int var11 = 0; var11 < 16; ++var11) {
               double var12 = 0.125D;
               double var14 = this.buffer[((var9 + 0) * 5 + var10 + 0) * 17 + var11 + 0];
               double var16 = this.buffer[((var9 + 0) * 5 + var10 + 1) * 17 + var11 + 0];
               double var18 = this.buffer[((var9 + 1) * 5 + var10 + 0) * 17 + var11 + 0];
               double var20 = this.buffer[((var9 + 1) * 5 + var10 + 1) * 17 + var11 + 0];
               double var22 = (this.buffer[((var9 + 0) * 5 + var10 + 0) * 17 + var11 + 1] - var14) * 0.125D;
               double var24 = (this.buffer[((var9 + 0) * 5 + var10 + 1) * 17 + var11 + 1] - var16) * 0.125D;
               double var26 = (this.buffer[((var9 + 1) * 5 + var10 + 0) * 17 + var11 + 1] - var18) * 0.125D;
               double var28 = (this.buffer[((var9 + 1) * 5 + var10 + 1) * 17 + var11 + 1] - var20) * 0.125D;

               for(int var30 = 0; var30 < 8; ++var30) {
                  double var31 = 0.25D;
                  double var33 = var14;
                  double var35 = var16;
                  double var37 = (var18 - var14) * 0.25D;
                  double var39 = (var20 - var16) * 0.25D;

                  for(int var41 = 0; var41 < 4; ++var41) {
                     double var42 = 0.25D;
                     double var44 = var33;
                     double var46 = (var35 - var33) * 0.25D;

                     for(int var48 = 0; var48 < 4; ++var48) {
                        IBlockState var49 = null;
                        if (var11 * 8 + var30 < var5) {
                           var49 = LAVA;
                        }

                        if (var44 > 0.0D) {
                           var49 = NETHERRACK;
                        }

                        int var50 = var41 + var9 * 4;
                        int var51 = var30 + var11 * 8;
                        int var52 = var48 + var10 * 4;
                        var3.setBlockState(var50, var51, var52, var49);
                        var44 += var46;
                     }

                     var33 += var37;
                     var35 += var39;
                  }

                  var14 += var22;
                  var16 += var24;
                  var18 += var26;
                  var20 += var28;
               }
            }
         }
      }

   }

   public void buildSurfaces(int var1, int var2, ChunkPrimer var3) {
      if (ForgeEventFactory.onReplaceBiomeBlocks(this, var1, var2, var3, this.world)) {
         int var4 = this.world.getSeaLevel() + 1;
         double var5 = 0.03125D;
         this.slowsandNoise = this.slowsandGravelNoiseGen.generateNoiseOctaves(this.slowsandNoise, var1 * 16, var2 * 16, 0, 16, 16, 1, 0.03125D, 0.03125D, 1.0D);
         this.gravelNoise = this.slowsandGravelNoiseGen.generateNoiseOctaves(this.gravelNoise, var1 * 16, 109, var2 * 16, 16, 1, 16, 0.03125D, 1.0D, 0.03125D);
         this.depthBuffer = this.netherrackExculsivityNoiseGen.generateNoiseOctaves(this.depthBuffer, var1 * 16, var2 * 16, 0, 16, 16, 1, 0.0625D, 0.0625D, 0.0625D);

         for(int var7 = 0; var7 < 16; ++var7) {
            for(int var8 = 0; var8 < 16; ++var8) {
               boolean var9 = this.slowsandNoise[var7 + var8 * 16] + this.rand.nextDouble() * 0.2D > 0.0D;
               boolean var10 = this.gravelNoise[var7 + var8 * 16] + this.rand.nextDouble() * 0.2D > 0.0D;
               int var11 = (int)(this.depthBuffer[var7 + var8 * 16] / 3.0D + 3.0D + this.rand.nextDouble() * 0.25D);
               int var12 = -1;
               IBlockState var13 = NETHERRACK;
               IBlockState var14 = NETHERRACK;

               for(int var15 = 127; var15 >= 0; --var15) {
                  if (var15 < 127 - this.rand.nextInt(5) && var15 > this.rand.nextInt(5)) {
                     IBlockState var16 = var3.getBlockState(var8, var15, var7);
                     if (var16.getBlock() != null && var16.getMaterial() != Material.AIR) {
                        if (var16.getBlock() == Blocks.NETHERRACK) {
                           if (var12 == -1) {
                              if (var11 <= 0) {
                                 var13 = AIR;
                                 var14 = NETHERRACK;
                              } else if (var15 >= var4 - 4 && var15 <= var4 + 1) {
                                 var13 = NETHERRACK;
                                 var14 = NETHERRACK;
                                 if (var10) {
                                    var13 = GRAVEL;
                                    var14 = NETHERRACK;
                                 }

                                 if (var9) {
                                    var13 = SOUL_SAND;
                                    var14 = SOUL_SAND;
                                 }
                              }

                              if (var15 < var4 && (var13 == null || var13.getMaterial() == Material.AIR)) {
                                 var13 = LAVA;
                              }

                              var12 = var11;
                              if (var15 >= var4 - 1) {
                                 var3.setBlockState(var8, var15, var7, var13);
                              } else {
                                 var3.setBlockState(var8, var15, var7, var14);
                              }
                           } else if (var12 > 0) {
                              --var12;
                              var3.setBlockState(var8, var15, var7, var14);
                           }
                        }
                     } else {
                        var12 = -1;
                     }
                  } else {
                     var3.setBlockState(var8, var15, var7, BEDROCK);
                  }
               }
            }
         }

      }
   }

   public Chunk provideChunk(int var1, int var2) {
      this.rand.setSeed((long)var1 * 341873128712L + (long)var2 * 132897987541L);
      ChunkPrimer var3 = new ChunkPrimer();
      this.prepareHeights(var1, var2, var3);
      this.buildSurfaces(var1, var2, var3);
      this.genNetherCaves.generate(this.world, var1, var2, var3);
      if (this.generateStructures) {
         this.genNetherBridge.generate(this.world, var1, var2, var3);
      }

      Chunk var4 = new Chunk(this.world, var3, var1, var2);
      Biome[] var5 = this.world.getBiomeProvider().getBiomes((Biome[])null, var1 * 16, var2 * 16, 16, 16);
      byte[] var6 = var4.getBiomeArray();

      for(int var7 = 0; var7 < var6.length; ++var7) {
         var6[var7] = (byte)Biome.getIdForBiome(var5[var7]);
      }

      var4.resetRelightChecks();
      return var4;
   }

   private double[] getHeights(double[] var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      if (var1 == null) {
         var1 = new double[var5 * var6 * var7];
      }

      InitNoiseField var8 = new InitNoiseField(this, var1, var2, var3, var4, var5, var6, var7);
      MinecraftForge.EVENT_BUS.post(var8);
      if (var8.getResult() == Result.DENY) {
         return var8.getNoisefield();
      } else {
         double var9 = 684.412D;
         double var11 = 2053.236D;
         this.noiseData4 = this.scaleNoise.generateNoiseOctaves(this.noiseData4, var2, var3, var4, var5, 1, var7, 1.0D, 0.0D, 1.0D);
         this.dr = this.depthNoise.generateNoiseOctaves(this.dr, var2, var3, var4, var5, 1, var7, 100.0D, 0.0D, 100.0D);
         this.pnr = this.perlinNoise1.generateNoiseOctaves(this.pnr, var2, var3, var4, var5, var6, var7, 8.555150000000001D, 34.2206D, 8.555150000000001D);
         this.ar = this.lperlinNoise1.generateNoiseOctaves(this.ar, var2, var3, var4, var5, var6, var7, 684.412D, 2053.236D, 684.412D);
         this.br = this.lperlinNoise2.generateNoiseOctaves(this.br, var2, var3, var4, var5, var6, var7, 684.412D, 2053.236D, 684.412D);
         int var13 = 0;
         double[] var14 = new double[var6];

         for(int var15 = 0; var15 < var6; ++var15) {
            var14[var15] = Math.cos((double)var15 * 3.141592653589793D * 6.0D / (double)var6) * 2.0D;
            double var16 = (double)var15;
            if (var15 > var6 / 2) {
               var16 = (double)(var6 - 1 - var15);
            }

            if (var16 < 4.0D) {
               var16 = 4.0D - var16;
               var14[var15] -= var16 * var16 * var16 * 10.0D;
            }
         }

         for(int var32 = 0; var32 < var5; ++var32) {
            for(int var34 = 0; var34 < var7; ++var34) {
               double var17 = 0.0D;

               for(int var19 = 0; var19 < var6; ++var19) {
                  double var20 = var14[var19];
                  double var22 = this.ar[var13] / 512.0D;
                  double var24 = this.br[var13] / 512.0D;
                  double var26 = (this.pnr[var13] / 10.0D + 1.0D) / 2.0D;
                  double var28;
                  if (var26 < 0.0D) {
                     var28 = var22;
                  } else if (var26 > 1.0D) {
                     var28 = var24;
                  } else {
                     var28 = var22 + (var24 - var22) * var26;
                  }

                  var28 = var28 - var20;
                  if (var19 > var6 - 4) {
                     double var30 = (double)((float)(var19 - (var6 - 4)) / 3.0F);
                     var28 = var28 * (1.0D - var30) + -10.0D * var30;
                  }

                  if ((double)var19 < 0.0D) {
                     double var36 = (0.0D - (double)var19) / 4.0D;
                     var36 = MathHelper.clamp(var36, 0.0D, 1.0D);
                     var28 = var28 * (1.0D - var36) + -10.0D * var36;
                  }

                  var1[var13] = var28;
                  ++var13;
               }
            }
         }

         return var1;
      }
   }

   public void populate(int var1, int var2) {
      BlockFalling.fallInstantly = true;
      ForgeEventFactory.onChunkPopulate(true, this, this.world, this.rand, var1, var2, false);
      int var3 = var1 * 16;
      int var4 = var2 * 16;
      BlockPos var5 = new BlockPos(var3, 0, var4);
      Biome var6 = this.world.getBiome(var5.add(16, 0, 16));
      ChunkPos var7 = new ChunkPos(var1, var2);
      this.genNetherBridge.generateStructure(this.world, this.rand, var7);
      if (TerrainGen.populate(this, this.world, this.rand, var1, var2, false, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.NETHER_LAVA)) {
         for(int var8 = 0; var8 < 8; ++var8) {
            this.hellSpringGen.generate(this.world, this.rand, var5.add(this.rand.nextInt(16) + 8, this.rand.nextInt(120) + 4, this.rand.nextInt(16) + 8));
         }
      }

      if (TerrainGen.populate(this, this.world, this.rand, var1, var2, false, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.FIRE)) {
         for(int var10 = 0; var10 < this.rand.nextInt(this.rand.nextInt(10) + 1) + 1; ++var10) {
            this.fireFeature.generate(this.world, this.rand, var5.add(this.rand.nextInt(16) + 8, this.rand.nextInt(120) + 4, this.rand.nextInt(16) + 8));
         }
      }

      if (TerrainGen.populate(this, this.world, this.rand, var1, var2, false, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.GLOWSTONE)) {
         for(int var11 = 0; var11 < this.rand.nextInt(this.rand.nextInt(10) + 1); ++var11) {
            this.lightGemGen.generate(this.world, this.rand, var5.add(this.rand.nextInt(16) + 8, this.rand.nextInt(120) + 4, this.rand.nextInt(16) + 8));
         }

         for(int var12 = 0; var12 < 10; ++var12) {
            this.hellPortalGen.generate(this.world, this.rand, var5.add(this.rand.nextInt(16) + 8, this.rand.nextInt(128), this.rand.nextInt(16) + 8));
         }
      }

      ForgeEventFactory.onChunkPopulate(false, this, this.world, this.rand, var1, var2, false);
      MinecraftForge.EVENT_BUS.post(new Pre(this.world, this.rand, var5));
      if (TerrainGen.decorate(this.world, this.rand, var5, net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType.SHROOM)) {
         if (this.rand.nextBoolean()) {
            this.brownMushroomFeature.generate(this.world, this.rand, var5.add(this.rand.nextInt(16) + 8, this.rand.nextInt(128), this.rand.nextInt(16) + 8));
         }

         if (this.rand.nextBoolean()) {
            this.redMushroomFeature.generate(this.world, this.rand, var5.add(this.rand.nextInt(16) + 8, this.rand.nextInt(128), this.rand.nextInt(16) + 8));
         }
      }

      if (TerrainGen.generateOre(this.world, this.rand, this.quartzGen, var5, net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.QUARTZ)) {
         for(int var13 = 0; var13 < 16; ++var13) {
            this.quartzGen.generate(this.world, this.rand, var5.add(this.rand.nextInt(16), this.rand.nextInt(108) + 10, this.rand.nextInt(16)));
         }
      }

      int var14 = this.world.getSeaLevel() / 2 + 1;
      if (TerrainGen.populate(this, this.world, this.rand, var1, var2, false, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.NETHER_MAGMA)) {
         for(int var9 = 0; var9 < 4; ++var9) {
            this.magmaGen.generate(this.world, this.rand, var5.add(this.rand.nextInt(16), var14 - 5 + this.rand.nextInt(10), this.rand.nextInt(16)));
         }
      }

      if (TerrainGen.populate(this, this.world, this.rand, var1, var2, false, net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.NETHER_LAVA2)) {
         for(int var15 = 0; var15 < 16; ++var15) {
            this.lavaTrapGen.generate(this.world, this.rand, var5.add(this.rand.nextInt(16), this.rand.nextInt(108) + 10, this.rand.nextInt(16)));
         }
      }

      var6.decorate(this.world, this.rand, new BlockPos(var3, 0, var4));
      MinecraftForge.EVENT_BUS.post(new Post(this.world, this.rand, var5));
      BlockFalling.fallInstantly = false;
   }

   public boolean generateStructures(Chunk var1, int var2, int var3) {
      return false;
   }

   public List getPossibleCreatures(EnumCreatureType var1, BlockPos var2) {
      if (var1 == EnumCreatureType.MONSTER) {
         if (this.genNetherBridge.isInsideStructure(var2)) {
            return this.genNetherBridge.getSpawnList();
         }

         if (this.genNetherBridge.isPositionInStructure(this.world, var2) && this.world.getBlockState(var2.down()).getBlock() == Blocks.NETHER_BRICK) {
            return this.genNetherBridge.getSpawnList();
         }
      }

      Biome var3 = this.world.getBiome(var2);
      return var3.getSpawnableList(var1);
   }

   @Nullable
   public BlockPos getStrongholdGen(World var1, String var2, BlockPos var3) {
      return null;
   }

   public void recreateStructures(Chunk var1, int var2, int var3) {
      this.genNetherBridge.generate(this.world, var2, var3, (ChunkPrimer)null);
   }
}
