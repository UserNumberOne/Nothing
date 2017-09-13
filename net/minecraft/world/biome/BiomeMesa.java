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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

   public BiomeDecorator createBiomeDecorator() {
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
      double var23 = 0.0D;
      if (this.brycePillars) {
         int var10 = (var4 & -16) + (var5 & 15);
         int var11 = (var5 & -16) + (var4 & 15);
         double var12 = Math.min(Math.abs(var6), this.pillarNoise.getValue((double)var10 * 0.25D, (double)var11 * 0.25D));
         if (var12 > 0.0D) {
            double var14 = 0.001953125D;
            double var16 = Math.abs(this.pillarRoofNoise.getValue((double)var10 * 0.001953125D, (double)var11 * 0.001953125D));
            var23 = var12 * var12 * 2.5D;
            double var18 = Math.ceil(var16 * 50.0D) + 14.0D;
            if (var23 > var18) {
               var23 = var18;
            }

            var23 = var23 + 64.0D;
         }
      }

      int var25 = var4 & 15;
      int var26 = var5 & 15;
      int var27 = var1.getSeaLevel();
      IBlockState var13 = STAINED_HARDENED_CLAY;
      IBlockState var28 = this.fillerBlock;
      int var15 = (int)(var6 / 3.0D + 3.0D + var2.nextDouble() * 0.25D);
      boolean var29 = Math.cos(var6 / 3.0D * 3.141592653589793D) > 0.0D;
      int var17 = -1;
      boolean var30 = false;
      int var19 = 0;

      for(int var20 = 255; var20 >= 0; --var20) {
         if (var3.getBlockState(var26, var20, var25).getMaterial() == Material.AIR && var20 < (int)var23) {
            var3.setBlockState(var26, var20, var25, STONE);
         }

         if (var20 <= var2.nextInt(5)) {
            var3.setBlockState(var26, var20, var25, BEDROCK);
         } else if (var19 < 15) {
            IBlockState var21 = var3.getBlockState(var26, var20, var25);
            if (var21.getMaterial() == Material.AIR) {
               var17 = -1;
            } else if (var21.getBlock() == Blocks.STONE) {
               if (var17 == -1) {
                  var30 = false;
                  if (var15 <= 0) {
                     var13 = AIR;
                     var28 = STONE;
                  } else if (var20 >= var27 - 4 && var20 <= var27 + 1) {
                     var13 = STAINED_HARDENED_CLAY;
                     var28 = this.fillerBlock;
                  }

                  if (var20 < var27 && (var13 == null || var13.getMaterial() == Material.AIR)) {
                     var13 = WATER;
                  }

                  var17 = var15 + Math.max(0, var20 - var27);
                  if (var20 >= var27 - 1) {
                     if (this.hasForest && var20 > 86 + var15 * 2) {
                        if (var29) {
                           var3.setBlockState(var26, var20, var25, COARSE_DIRT);
                        } else {
                           var3.setBlockState(var26, var20, var25, GRASS);
                        }
                     } else if (var20 > var27 + 3 + var15) {
                        IBlockState var22;
                        if (var20 >= 64 && var20 <= 127) {
                           if (var29) {
                              var22 = HARDENED_CLAY;
                           } else {
                              var22 = this.getBand(var4, var20, var5);
                           }
                        } else {
                           var22 = ORANGE_STAINED_HARDENED_CLAY;
                        }

                        var3.setBlockState(var26, var20, var25, var22);
                     } else {
                        var3.setBlockState(var26, var20, var25, this.topBlock);
                        var30 = true;
                     }
                  } else {
                     var3.setBlockState(var26, var20, var25, var28);
                     if (var28.getBlock() == Blocks.STAINED_HARDENED_CLAY) {
                        var3.setBlockState(var26, var20, var25, ORANGE_STAINED_HARDENED_CLAY);
                     }
                  }
               } else if (var17 > 0) {
                  --var17;
                  if (var30) {
                     var3.setBlockState(var26, var20, var25, ORANGE_STAINED_HARDENED_CLAY);
                  } else {
                     var3.setBlockState(var26, var20, var25, this.getBand(var4, var20, var5));
                  }
               }

               ++var19;
            }
         }
      }

   }

   public void generateBands(long var1) {
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

   public IBlockState getBand(int var1, int var2, int var3) {
      int var4 = (int)Math.round(this.clayBandsOffsetNoise.getValue((double)var1 / 512.0D, (double)var1 / 512.0D) * 2.0D);
      return this.clayBands[(var2 + var4 + 64) % 64];
   }

   @SideOnly(Side.CLIENT)
   public int getFoliageColorAtPos(BlockPos var1) {
      return 10387789;
   }

   @SideOnly(Side.CLIENT)
   public int getGrassColorAtPos(BlockPos var1) {
      return 9470285;
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
