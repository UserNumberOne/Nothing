package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkProviderSettings;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import net.minecraft.world.gen.feature.WorldGenBush;
import net.minecraft.world.gen.feature.WorldGenCactus;
import net.minecraft.world.gen.feature.WorldGenClay;
import net.minecraft.world.gen.feature.WorldGenDeadBush;
import net.minecraft.world.gen.feature.WorldGenFlowers;
import net.minecraft.world.gen.feature.WorldGenLiquids;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenPumpkin;
import net.minecraft.world.gen.feature.WorldGenReed;
import net.minecraft.world.gen.feature.WorldGenSand;
import net.minecraft.world.gen.feature.WorldGenWaterlily;
import net.minecraft.world.gen.feature.WorldGenerator;

public class BiomeDecorator {
   protected boolean decorating;
   protected BlockPos chunkPos;
   protected ChunkProviderSettings chunkProviderSettings;
   protected WorldGenerator clayGen = new WorldGenClay(4);
   protected WorldGenerator sandGen = new WorldGenSand(Blocks.SAND, 7);
   protected WorldGenerator gravelAsSandGen = new WorldGenSand(Blocks.GRAVEL, 6);
   protected WorldGenerator dirtGen;
   protected WorldGenerator gravelGen;
   protected WorldGenerator graniteGen;
   protected WorldGenerator dioriteGen;
   protected WorldGenerator andesiteGen;
   protected WorldGenerator coalGen;
   protected WorldGenerator ironGen;
   protected WorldGenerator goldGen;
   protected WorldGenerator redstoneGen;
   protected WorldGenerator diamondGen;
   protected WorldGenerator lapisGen;
   protected WorldGenFlowers yellowFlowerGen = new WorldGenFlowers(Blocks.YELLOW_FLOWER, BlockFlower.EnumFlowerType.DANDELION);
   protected WorldGenerator mushroomBrownGen = new WorldGenBush(Blocks.BROWN_MUSHROOM);
   protected WorldGenerator mushroomRedGen = new WorldGenBush(Blocks.RED_MUSHROOM);
   protected WorldGenerator bigMushroomGen = new WorldGenBigMushroom();
   protected WorldGenerator reedGen = new WorldGenReed();
   protected WorldGenerator cactusGen = new WorldGenCactus();
   protected WorldGenerator waterlilyGen = new WorldGenWaterlily();
   protected int waterlilyPerChunk;
   protected int treesPerChunk;
   protected float extraTreeChance = 0.1F;
   protected int flowersPerChunk = 2;
   protected int grassPerChunk = 1;
   protected int deadBushPerChunk;
   protected int mushroomsPerChunk;
   protected int reedsPerChunk;
   protected int cactiPerChunk;
   protected int sandPerChunk = 1;
   protected int sandPerChunk2 = 3;
   protected int clayPerChunk = 1;
   protected int bigMushroomsPerChunk;
   public boolean generateLakes = true;

   public void decorate(World var1, Random var2, Biome var3, BlockPos var4) {
      if (this.decorating) {
         throw new RuntimeException("Already decorating");
      } else {
         this.chunkProviderSettings = ChunkProviderSettings.Factory.jsonToFactory(var1.getWorldInfo().getGeneratorOptions()).build();
         this.chunkPos = var4;
         this.dirtGen = new WorldGenMinable(Blocks.DIRT.getDefaultState(), this.chunkProviderSettings.dirtSize);
         this.gravelGen = new WorldGenMinable(Blocks.GRAVEL.getDefaultState(), this.chunkProviderSettings.gravelSize);
         this.graniteGen = new WorldGenMinable(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE), this.chunkProviderSettings.graniteSize);
         this.dioriteGen = new WorldGenMinable(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE), this.chunkProviderSettings.dioriteSize);
         this.andesiteGen = new WorldGenMinable(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE), this.chunkProviderSettings.andesiteSize);
         this.coalGen = new WorldGenMinable(Blocks.COAL_ORE.getDefaultState(), this.chunkProviderSettings.coalSize);
         this.ironGen = new WorldGenMinable(Blocks.IRON_ORE.getDefaultState(), this.chunkProviderSettings.ironSize);
         this.goldGen = new WorldGenMinable(Blocks.GOLD_ORE.getDefaultState(), this.chunkProviderSettings.goldSize);
         this.redstoneGen = new WorldGenMinable(Blocks.REDSTONE_ORE.getDefaultState(), this.chunkProviderSettings.redstoneSize);
         this.diamondGen = new WorldGenMinable(Blocks.DIAMOND_ORE.getDefaultState(), this.chunkProviderSettings.diamondSize);
         this.lapisGen = new WorldGenMinable(Blocks.LAPIS_ORE.getDefaultState(), this.chunkProviderSettings.lapisSize);
         this.genDecorations(var3, var1, var2);
         this.decorating = false;
      }
   }

   protected void genDecorations(Biome var1, World var2, Random var3) {
      this.generateOres(var2, var3);

      for(int var4 = 0; var4 < this.sandPerChunk2; ++var4) {
         int var5 = var3.nextInt(16) + 8;
         int var6 = var3.nextInt(16) + 8;
         this.sandGen.generate(var2, var3, var2.getTopSolidOrLiquidBlock(this.chunkPos.add(var5, 0, var6)));
      }

      for(int var13 = 0; var13 < this.clayPerChunk; ++var13) {
         int var16 = var3.nextInt(16) + 8;
         int var33 = var3.nextInt(16) + 8;
         this.clayGen.generate(var2, var3, var2.getTopSolidOrLiquidBlock(this.chunkPos.add(var16, 0, var33)));
      }

      for(int var14 = 0; var14 < this.sandPerChunk; ++var14) {
         int var17 = var3.nextInt(16) + 8;
         int var34 = var3.nextInt(16) + 8;
         this.gravelAsSandGen.generate(var2, var3, var2.getTopSolidOrLiquidBlock(this.chunkPos.add(var17, 0, var34)));
      }

      int var15 = this.treesPerChunk;
      if (var3.nextFloat() < this.extraTreeChance) {
         ++var15;
      }

      for(int var18 = 0; var18 < var15; ++var18) {
         int var35 = var3.nextInt(16) + 8;
         int var7 = var3.nextInt(16) + 8;
         WorldGenAbstractTree var8 = var1.genBigTreeChance(var3);
         var8.setDecorationDefaults();
         BlockPos var9 = var2.getHeight(this.chunkPos.add(var35, 0, var7));
         if (var8.generate(var2, var3, var9)) {
            var8.generateSaplings(var2, var3, var9);
         }
      }

      for(int var19 = 0; var19 < this.bigMushroomsPerChunk; ++var19) {
         int var36 = var3.nextInt(16) + 8;
         int var51 = var3.nextInt(16) + 8;
         this.bigMushroomGen.generate(var2, var3, var2.getHeight(this.chunkPos.add(var36, 0, var51)));
      }

      for(int var20 = 0; var20 < this.flowersPerChunk; ++var20) {
         int var37 = var3.nextInt(16) + 8;
         int var52 = var3.nextInt(16) + 8;
         int var66 = var2.getHeight(this.chunkPos.add(var37, 0, var52)).getY() + 32;
         if (var66 > 0) {
            int var80 = var3.nextInt(var66);
            BlockPos var10 = this.chunkPos.add(var37, var80, var52);
            BlockFlower.EnumFlowerType var11 = var1.pickRandomFlower(var3, var10);
            BlockFlower var12 = var11.getBlockType().getBlock();
            if (var12.getDefaultState().getMaterial() != Material.AIR) {
               this.yellowFlowerGen.setGeneratedBlock(var12, var11);
               this.yellowFlowerGen.generate(var2, var3, var10);
            }
         }
      }

      for(int var21 = 0; var21 < this.grassPerChunk; ++var21) {
         int var38 = var3.nextInt(16) + 8;
         int var53 = var3.nextInt(16) + 8;
         int var67 = var2.getHeight(this.chunkPos.add(var38, 0, var53)).getY() * 2;
         if (var67 > 0) {
            int var81 = var3.nextInt(var67);
            var1.getRandomWorldGenForGrass(var3).generate(var2, var3, this.chunkPos.add(var38, var81, var53));
         }
      }

      for(int var22 = 0; var22 < this.deadBushPerChunk; ++var22) {
         int var39 = var3.nextInt(16) + 8;
         int var54 = var3.nextInt(16) + 8;
         int var68 = var2.getHeight(this.chunkPos.add(var39, 0, var54)).getY() * 2;
         if (var68 > 0) {
            int var82 = var3.nextInt(var68);
            (new WorldGenDeadBush()).generate(var2, var3, this.chunkPos.add(var39, var82, var54));
         }
      }

      for(int var23 = 0; var23 < this.waterlilyPerChunk; ++var23) {
         int var40 = var3.nextInt(16) + 8;
         int var55 = var3.nextInt(16) + 8;
         int var69 = var2.getHeight(this.chunkPos.add(var40, 0, var55)).getY() * 2;
         if (var69 > 0) {
            int var83 = var3.nextInt(var69);

            BlockPos var90;
            BlockPos var93;
            for(var90 = this.chunkPos.add(var40, var83, var55); var90.getY() > 0; var90 = var93) {
               var93 = var90.down();
               if (!var2.isAirBlock(var93)) {
                  break;
               }
            }

            this.waterlilyGen.generate(var2, var3, var90);
         }
      }

      for(int var24 = 0; var24 < this.mushroomsPerChunk; ++var24) {
         if (var3.nextInt(4) == 0) {
            int var41 = var3.nextInt(16) + 8;
            int var56 = var3.nextInt(16) + 8;
            BlockPos var70 = var2.getHeight(this.chunkPos.add(var41, 0, var56));
            this.mushroomBrownGen.generate(var2, var3, var70);
         }

         if (var3.nextInt(8) == 0) {
            int var42 = var3.nextInt(16) + 8;
            int var57 = var3.nextInt(16) + 8;
            int var71 = var2.getHeight(this.chunkPos.add(var42, 0, var57)).getY() * 2;
            if (var71 > 0) {
               int var84 = var3.nextInt(var71);
               BlockPos var91 = this.chunkPos.add(var42, var84, var57);
               this.mushroomRedGen.generate(var2, var3, var91);
            }
         }
      }

      if (var3.nextInt(4) == 0) {
         int var25 = var3.nextInt(16) + 8;
         int var43 = var3.nextInt(16) + 8;
         int var58 = var2.getHeight(this.chunkPos.add(var25, 0, var43)).getY() * 2;
         if (var58 > 0) {
            int var72 = var3.nextInt(var58);
            this.mushroomBrownGen.generate(var2, var3, this.chunkPos.add(var25, var72, var43));
         }
      }

      if (var3.nextInt(8) == 0) {
         int var26 = var3.nextInt(16) + 8;
         int var44 = var3.nextInt(16) + 8;
         int var59 = var2.getHeight(this.chunkPos.add(var26, 0, var44)).getY() * 2;
         if (var59 > 0) {
            int var73 = var3.nextInt(var59);
            this.mushroomRedGen.generate(var2, var3, this.chunkPos.add(var26, var73, var44));
         }
      }

      for(int var27 = 0; var27 < this.reedsPerChunk; ++var27) {
         int var45 = var3.nextInt(16) + 8;
         int var60 = var3.nextInt(16) + 8;
         int var74 = var2.getHeight(this.chunkPos.add(var45, 0, var60)).getY() * 2;
         if (var74 > 0) {
            int var85 = var3.nextInt(var74);
            this.reedGen.generate(var2, var3, this.chunkPos.add(var45, var85, var60));
         }
      }

      for(int var28 = 0; var28 < 10; ++var28) {
         int var46 = var3.nextInt(16) + 8;
         int var61 = var3.nextInt(16) + 8;
         int var75 = var2.getHeight(this.chunkPos.add(var46, 0, var61)).getY() * 2;
         if (var75 > 0) {
            int var86 = var3.nextInt(var75);
            this.reedGen.generate(var2, var3, this.chunkPos.add(var46, var86, var61));
         }
      }

      if (var3.nextInt(32) == 0) {
         int var29 = var3.nextInt(16) + 8;
         int var47 = var3.nextInt(16) + 8;
         int var62 = var2.getHeight(this.chunkPos.add(var29, 0, var47)).getY() * 2;
         if (var62 > 0) {
            int var76 = var3.nextInt(var62);
            (new WorldGenPumpkin()).generate(var2, var3, this.chunkPos.add(var29, var76, var47));
         }
      }

      for(int var30 = 0; var30 < this.cactiPerChunk; ++var30) {
         int var48 = var3.nextInt(16) + 8;
         int var63 = var3.nextInt(16) + 8;
         int var77 = var2.getHeight(this.chunkPos.add(var48, 0, var63)).getY() * 2;
         if (var77 > 0) {
            int var87 = var3.nextInt(var77);
            this.cactusGen.generate(var2, var3, this.chunkPos.add(var48, var87, var63));
         }
      }

      if (this.generateLakes) {
         for(int var31 = 0; var31 < 50; ++var31) {
            int var49 = var3.nextInt(16) + 8;
            int var64 = var3.nextInt(16) + 8;
            int var78 = var3.nextInt(248) + 8;
            if (var78 > 0) {
               int var88 = var3.nextInt(var78);
               BlockPos var92 = this.chunkPos.add(var49, var88, var64);
               (new WorldGenLiquids(Blocks.FLOWING_WATER)).generate(var2, var3, var92);
            }
         }

         for(int var32 = 0; var32 < 20; ++var32) {
            int var50 = var3.nextInt(16) + 8;
            int var65 = var3.nextInt(16) + 8;
            int var79 = var3.nextInt(var3.nextInt(var3.nextInt(240) + 8) + 8);
            BlockPos var89 = this.chunkPos.add(var50, var79, var65);
            (new WorldGenLiquids(Blocks.FLOWING_LAVA)).generate(var2, var3, var89);
         }
      }

   }

   protected void generateOres(World var1, Random var2) {
      this.genStandardOre1(var1, var2, this.chunkProviderSettings.dirtCount, this.dirtGen, this.chunkProviderSettings.dirtMinHeight, this.chunkProviderSettings.dirtMaxHeight);
      this.genStandardOre1(var1, var2, this.chunkProviderSettings.gravelCount, this.gravelGen, this.chunkProviderSettings.gravelMinHeight, this.chunkProviderSettings.gravelMaxHeight);
      this.genStandardOre1(var1, var2, this.chunkProviderSettings.dioriteCount, this.dioriteGen, this.chunkProviderSettings.dioriteMinHeight, this.chunkProviderSettings.dioriteMaxHeight);
      this.genStandardOre1(var1, var2, this.chunkProviderSettings.graniteCount, this.graniteGen, this.chunkProviderSettings.graniteMinHeight, this.chunkProviderSettings.graniteMaxHeight);
      this.genStandardOre1(var1, var2, this.chunkProviderSettings.andesiteCount, this.andesiteGen, this.chunkProviderSettings.andesiteMinHeight, this.chunkProviderSettings.andesiteMaxHeight);
      this.genStandardOre1(var1, var2, this.chunkProviderSettings.coalCount, this.coalGen, this.chunkProviderSettings.coalMinHeight, this.chunkProviderSettings.coalMaxHeight);
      this.genStandardOre1(var1, var2, this.chunkProviderSettings.ironCount, this.ironGen, this.chunkProviderSettings.ironMinHeight, this.chunkProviderSettings.ironMaxHeight);
      this.genStandardOre1(var1, var2, this.chunkProviderSettings.goldCount, this.goldGen, this.chunkProviderSettings.goldMinHeight, this.chunkProviderSettings.goldMaxHeight);
      this.genStandardOre1(var1, var2, this.chunkProviderSettings.redstoneCount, this.redstoneGen, this.chunkProviderSettings.redstoneMinHeight, this.chunkProviderSettings.redstoneMaxHeight);
      this.genStandardOre1(var1, var2, this.chunkProviderSettings.diamondCount, this.diamondGen, this.chunkProviderSettings.diamondMinHeight, this.chunkProviderSettings.diamondMaxHeight);
      this.genStandardOre2(var1, var2, this.chunkProviderSettings.lapisCount, this.lapisGen, this.chunkProviderSettings.lapisCenterHeight, this.chunkProviderSettings.lapisSpread);
   }

   protected void genStandardOre1(World var1, Random var2, int var3, WorldGenerator var4, int var5, int var6) {
      if (var6 < var5) {
         int var7 = var5;
         var5 = var6;
         var6 = var7;
      } else if (var6 == var5) {
         if (var5 < 255) {
            ++var6;
         } else {
            --var5;
         }
      }

      for(int var9 = 0; var9 < var3; ++var9) {
         BlockPos var8 = this.chunkPos.add(var2.nextInt(16), var2.nextInt(var6 - var5) + var5, var2.nextInt(16));
         var4.generate(var1, var2, var8);
      }

   }

   protected void genStandardOre2(World var1, Random var2, int var3, WorldGenerator var4, int var5, int var6) {
      for(int var7 = 0; var7 < var3; ++var7) {
         BlockPos var8 = this.chunkPos.add(var2.nextInt(16), var2.nextInt(var6) + var2.nextInt(var6) + var5 - var6, var2.nextInt(16));
         var4.generate(var1, var2, var8);
      }

   }
}
