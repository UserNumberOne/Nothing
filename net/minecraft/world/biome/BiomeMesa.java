package net.minecraft.world.biome;

import java.util.Arrays;
import java.util.Random;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockSand;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class BiomeMesa extends Biome {
   protected static final IBlockState COARSE_DIRT = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
   protected static final IBlockState GRASS = Blocks.GRASS.getDefaultState();
   protected static final IBlockState HARDENED_CLAY = Blocks.HARDENED_CLAY.getDefaultState();
   protected static final IBlockState STAINED_HARDENED_CLAY = Blocks.STAINED_HARDENED_CLAY.getDefaultState();
   protected static final IBlockState ORANGE_STAINED_HARDENED_CLAY = STAINED_HARDENED_CLAY.withProperty(BlockColored.COLOR, EnumDyeColor.ORANGE);
   protected static final IBlockState RED_SAND = Blocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.RED_SAND);
   private IBlockState[] clayBands;
   private long worldSeed;
   private NoiseGeneratorPerlin pillarNoise;
   private NoiseGeneratorPerlin pillarRoofNoise;
   private NoiseGeneratorPerlin clayBandsOffsetNoise;
   private final boolean brycePillars;
   private final boolean hasForest;

   public BiomeMesa(boolean var1, boolean var2, Biome.BiomeProperties var3) {
      super(var3);
      this.brycePillars = var1;
      this.hasForest = var2;
      this.spawnableCreatureList.clear();
      this.topBlock = RED_SAND;
      this.fillerBlock = STAINED_HARDENED_CLAY;
      this.theBiomeDecorator.treesPerChunk = -999;
      this.theBiomeDecorator.deadBushPerChunk = 20;
      this.theBiomeDecorator.reedsPerChunk = 3;
      this.theBiomeDecorator.cactiPerChunk = 5;
      this.theBiomeDecorator.flowersPerChunk = 0;
      this.spawnableCreatureList.clear();
      if (var2) {
         this.theBiomeDecorator.treesPerChunk = 5;
      }

   }

   protected BiomeDecorator createBiomeDecorator() {
      return new BiomeMesa.Decorator();
   }

   public WorldGenAbstractTree genBigTreeChance(Random var1) {
      return TREE_FEATURE;
   }

   public void decorate(World var1, Random var2, BlockPos var3) {
      super.decorate(var1, var2, var3);
   }

   public void genTerrainBlocks(World var1, Random var2, ChunkPrimer var3, int var4, int var5, double var6) {
      if (this.clayBands == null || this.worldSeed != var1.getSeed()) {
         this.generateBands(var1.getSeed());
      }

      if (this.pillarNoise == null || this.pillarRoofNoise == null || this.worldSeed != var1.getSeed()) {
         Random var8 = new Random(this.worldSeed);
         this.pillarNoise = new NoiseGeneratorPerlin(var8, 4);
         this.pillarRoofNoise = new NoiseGeneratorPerlin(var8, 1);
      }

      this.worldSeed = var1.getSeed();
      double var9 = 0.0D;
      if (this.brycePillars) {
         int var11 = (var4 & -16) + (var5 & 15);
         int var12 = (var5 & -16) + (var4 & 15);
         double var13 = Math.min(Math.abs(var6), this.pillarNoise.getValue((double)var11 * 0.25D, (double)var12 * 0.25D));
         if (var13 > 0.0D) {
            double var15 = 0.001953125D;
            double var17 = Math.abs(this.pillarRoofNoise.getValue((double)var11 * 0.001953125D, (double)var12 * 0.001953125D));
            var9 = var13 * var13 * 2.5D;
            double var19 = Math.ceil(var17 * 50.0D) + 14.0D;
            if (var9 > var19) {
               var9 = var19;
            }

            var9 = var9 + 64.0D;
         }
      }

      int var33 = var4 & 15;
      int var34 = var5 & 15;
      int var21 = var1.getSeaLevel();
      IBlockState var22 = STAINED_HARDENED_CLAY;
      IBlockState var23 = this.fillerBlock;
      int var24 = (int)(var6 / 3.0D + 3.0D + var2.nextDouble() * 0.25D);
      boolean var25 = Math.cos(var6 / 3.0D * 3.141592653589793D) > 0.0D;
      int var26 = -1;
      boolean var27 = false;
      int var28 = 0;

      for(int var29 = 255; var29 >= 0; --var29) {
         if (var3.getBlockState(var34, var29, var33).getMaterial() == Material.AIR && var29 < (int)var9) {
            var3.setBlockState(var34, var29, var33, STONE);
         }

         if (var29 <= var2.nextInt(5)) {
            var3.setBlockState(var34, var29, var33, BEDROCK);
         } else if (var28 < 15) {
            IBlockState var30 = var3.getBlockState(var34, var29, var33);
            if (var30.getMaterial() == Material.AIR) {
               var26 = -1;
            } else if (var30.getBlock() == Blocks.STONE) {
               if (var26 == -1) {
                  var27 = false;
                  if (var24 <= 0) {
                     var22 = AIR;
                     var23 = STONE;
                  } else if (var29 >= var21 - 4 && var29 <= var21 + 1) {
                     var22 = STAINED_HARDENED_CLAY;
                     var23 = this.fillerBlock;
                  }

                  if (var29 < var21 && (var22 == null || var22.getMaterial() == Material.AIR)) {
                     var22 = WATER;
                  }

                  var26 = var24 + Math.max(0, var29 - var21);
                  if (var29 >= var21 - 1) {
                     if (this.hasForest && var29 > 86 + var24 * 2) {
                        if (var25) {
                           var3.setBlockState(var34, var29, var33, COARSE_DIRT);
                        } else {
                           var3.setBlockState(var34, var29, var33, GRASS);
                        }
                     } else if (var29 > var21 + 3 + var24) {
                        IBlockState var31;
                        if (var29 >= 64 && var29 <= 127) {
                           if (var25) {
                              var31 = HARDENED_CLAY;
                           } else {
                              var31 = this.getBand(var4, var29, var5);
                           }
                        } else {
                           var31 = ORANGE_STAINED_HARDENED_CLAY;
                        }

                        var3.setBlockState(var34, var29, var33, var31);
                     } else {
                        var3.setBlockState(var34, var29, var33, this.topBlock);
                        var27 = true;
                     }
                  } else {
                     var3.setBlockState(var34, var29, var33, var23);
                     if (var23.getBlock() == Blocks.STAINED_HARDENED_CLAY) {
                        var3.setBlockState(var34, var29, var33, ORANGE_STAINED_HARDENED_CLAY);
                     }
                  }
               } else if (var26 > 0) {
                  --var26;
                  if (var27) {
                     var3.setBlockState(var34, var29, var33, ORANGE_STAINED_HARDENED_CLAY);
                  } else {
                     var3.setBlockState(var34, var29, var33, this.getBand(var4, var29, var5));
                  }
               }

               ++var28;
            }
         }
      }

   }

   private void generateBands(long var1) {
      this.clayBands = new IBlockState[64];
      Arrays.fill(this.clayBands, HARDENED_CLAY);
      Random var3 = new Random(var1);
      this.clayBandsOffsetNoise = new NoiseGeneratorPerlin(var3, 1);

      for(int var12 = 0; var12 < 64; ++var12) {
         var12 += var3.nextInt(5) + 1;
         if (var12 < 64) {
            this.clayBands[var12] = ORANGE_STAINED_HARDENED_CLAY;
         }
      }

      int var13 = var3.nextInt(4) + 2;

      for(int var5 = 0; var5 < var13; ++var5) {
         int var6 = var3.nextInt(3) + 1;
         int var7 = var3.nextInt(64);

         for(int var8 = 0; var7 + var8 < 64 && var8 < var6; ++var8) {
            this.clayBands[var7 + var8] = STAINED_HARDENED_CLAY.withProperty(BlockColored.COLOR, EnumDyeColor.YELLOW);
         }
      }

      int var14 = var3.nextInt(4) + 2;

      for(int var15 = 0; var15 < var14; ++var15) {
         int var17 = var3.nextInt(3) + 2;
         int var20 = var3.nextInt(64);

         for(int var9 = 0; var20 + var9 < 64 && var9 < var17; ++var9) {
            this.clayBands[var20 + var9] = STAINED_HARDENED_CLAY.withProperty(BlockColored.COLOR, EnumDyeColor.BROWN);
         }
      }

      int var16 = var3.nextInt(4) + 2;

      for(int var18 = 0; var18 < var16; ++var18) {
         int var21 = var3.nextInt(3) + 1;
         int var23 = var3.nextInt(64);

         for(int var10 = 0; var23 + var10 < 64 && var10 < var21; ++var10) {
            this.clayBands[var23 + var10] = STAINED_HARDENED_CLAY.withProperty(BlockColored.COLOR, EnumDyeColor.RED);
         }
      }

      int var19 = var3.nextInt(3) + 3;
      int var22 = 0;

      for(int var24 = 0; var24 < var19; ++var24) {
         boolean var25 = true;
         var22 += var3.nextInt(16) + 4;

         for(int var11 = 0; var22 + var11 < 64 && var11 < 1; ++var11) {
            this.clayBands[var22 + var11] = STAINED_HARDENED_CLAY.withProperty(BlockColored.COLOR, EnumDyeColor.WHITE);
            if (var22 + var11 > 1 && var3.nextBoolean()) {
               this.clayBands[var22 + var11 - 1] = STAINED_HARDENED_CLAY.withProperty(BlockColored.COLOR, EnumDyeColor.SILVER);
            }

            if (var22 + var11 < 63 && var3.nextBoolean()) {
               this.clayBands[var22 + var11 + 1] = STAINED_HARDENED_CLAY.withProperty(BlockColored.COLOR, EnumDyeColor.SILVER);
            }
         }
      }

   }

   private IBlockState getBand(int var1, int var2, int var3) {
      int var4 = (int)Math.round(this.clayBandsOffsetNoise.getValue((double)var1 / 512.0D, (double)var1 / 512.0D) * 2.0D);
      return this.clayBands[(var2 + var4 + 64) % 64];
   }

   class Decorator extends BiomeDecorator {
      private Decorator() {
      }

      protected void generateOres(World var1, Random var2) {
         super.generateOres(var1, var2);
         this.genStandardOre1(var1, var2, 20, this.goldGen, 32, 80);
      }
   }
}
