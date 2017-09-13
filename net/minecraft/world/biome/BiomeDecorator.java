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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Post;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Pre;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType;

public class BiomeDecorator {
   public boolean decorating;
   public BlockPos chunkPos;
   public ChunkProviderSettings chunkProviderSettings;
   public WorldGenerator clayGen = new WorldGenClay(4);
   public WorldGenerator sandGen = new WorldGenSand(Blocks.SAND, 7);
   public WorldGenerator gravelAsSandGen = new WorldGenSand(Blocks.GRAVEL, 6);
   public WorldGenerator dirtGen;
   public WorldGenerator gravelGen;
   public WorldGenerator graniteGen;
   public WorldGenerator dioriteGen;
   public WorldGenerator andesiteGen;
   public WorldGenerator coalGen;
   public WorldGenerator ironGen;
   public WorldGenerator goldGen;
   public WorldGenerator redstoneGen;
   public WorldGenerator diamondGen;
   public WorldGenerator lapisGen;
   public WorldGenFlowers yellowFlowerGen = new WorldGenFlowers(Blocks.YELLOW_FLOWER, BlockFlower.EnumFlowerType.DANDELION);
   public WorldGenerator mushroomBrownGen = new WorldGenBush(Blocks.BROWN_MUSHROOM);
   public WorldGenerator mushroomRedGen = new WorldGenBush(Blocks.RED_MUSHROOM);
   public WorldGenerator bigMushroomGen = new WorldGenBigMushroom();
   public WorldGenerator reedGen = new WorldGenReed();
   public WorldGenerator cactusGen = new WorldGenCactus();
   public WorldGenerator waterlilyGen = new WorldGenWaterlily();
   public int waterlilyPerChunk;
   public int treesPerChunk;
   public float extraTreeChance = 0.1F;
   public int flowersPerChunk = 2;
   public int grassPerChunk = 1;
   public int deadBushPerChunk;
   public int mushroomsPerChunk;
   public int reedsPerChunk;
   public int cactiPerChunk;
   public int sandPerChunk = 1;
   public int sandPerChunk2 = 3;
   public int clayPerChunk = 1;
   public int bigMushroomsPerChunk;
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
      MinecraftForge.EVENT_BUS.post(new Pre(var2, var3, this.chunkPos));
      this.generateOres(var2, var3);
      if (TerrainGen.decorate(var2, var3, this.chunkPos, EventType.SAND)) {
         for(int var4 = 0; var4 < this.sandPerChunk2; ++var4) {
            int var5 = var3.nextInt(16) + 8;
            int var6 = var3.nextInt(16) + 8;
            this.sandGen.generate(var2, var3, var2.getTopSolidOrLiquidBlock(this.chunkPos.add(var5, 0, var6)));
         }
      }

      if (TerrainGen.decorate(var2, var3, this.chunkPos, EventType.CLAY)) {
         for(int var13 = 0; var13 < this.clayPerChunk; ++var13) {
            int var16 = var3.nextInt(16) + 8;
            int var33 = var3.nextInt(16) + 8;
            this.clayGen.generate(var2, var3, var2.getTopSolidOrLiquidBlock(this.chunkPos.add(var16, 0, var33)));
         }
      }

      if (TerrainGen.decorate(var2, var3, this.chunkPos, EventType.SAND_PASS2)) {
         for(int var14 = 0; var14 < this.sandPerChunk; ++var14) {
            int var17 = var3.nextInt(16) + 8;
            int var34 = var3.nextInt(16) + 8;
            this.gravelAsSandGen.generate(var2, var3, var2.getTopSolidOrLiquidBlock(this.chunkPos.add(var17, 0, var34)));
         }
      }

      int var15 = this.treesPerChunk;
      if (var3.nextFloat() < this.extraTreeChance) {
         ++var15;
      }

      if (TerrainGen.decorate(var2, var3, this.chunkPos, EventType.TREE)) {
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
      }

      if (TerrainGen.decorate(var2, var3, this.chunkPos, EventType.BIG_SHROOM)) {
         for(int var19 = 0; var19 < this.bigMushroomsPerChunk; ++var19) {
            int var36 = var3.nextInt(16) + 8;
            int var51 = var3.nextInt(16) + 8;
            this.bigMushroomGen.generate(var2, var3, var2.getHeight(this.chunkPos.add(var36, 0, var51)));
         }
      }

      if (TerrainGen.decorate(var2, var3, this.chunkPos, EventType.FLOWERS)) {
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
      }

      if (TerrainGen.decorate(var2, var3, this.chunkPos, EventType.GRASS)) {
         for(int var21 = 0; var21 < this.grassPerChunk; ++var21) {
            int var38 = var3.nextInt(16) + 8;
            int var53 = var3.nextInt(16) + 8;
            int var67 = var2.getHeight(this.chunkPos.add(var38, 0, var53)).getY() * 2;
            if (var67 > 0) {
               int var81 = var3.nextInt(var67);
               var1.getRandomWorldGenForGrass(var3).generate(var2, var3, this.chunkPos.add(var38, var81, var53));
            }
         }
      }

      if (TerrainGen.decorate(var2, var3, this.chunkPos, EventType.DEAD_BUSH)) {
         for(int var22 = 0; var22 < this.deadBushPerChunk; ++var22) {
            int var39 = var3.nextInt(16) + 8;
            int var54 = var3.nextInt(16) + 8;
            int var68 = var2.getHeight(this.chunkPos.add(var39, 0, var54)).getY() * 2;
            if (var68 > 0) {
               int var82 = var3.nextInt(var68);
               (new WorldGenDeadBush()).generate(var2, var3, this.chunkPos.add(var39, var82, var54));
            }
         }
      }

      if (TerrainGen.decorate(var2, var3, this.chunkPos, EventType.LILYPAD)) {
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
      }

      if (TerrainGen.decorate(var2, var3, this.chunkPos, EventType.SHROOM)) {
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
      }

      if (TerrainGen.decorate(var2, var3, this.chunkPos, EventType.REED)) {
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
      }

      if (TerrainGen.decorate(var2, var3, this.chunkPos, EventType.PUMPKIN) && var3.nextInt(32) == 0) {
         int var29 = var3.nextInt(16) + 8;
         int var47 = var3.nextInt(16) + 8;
         int var62 = var2.getHeight(this.chunkPos.add(var29, 0, var47)).getY() * 2;
         if (var62 > 0) {
            int var76 = var3.nextInt(var62);
            (new WorldGenPumpkin()).generate(var2, var3, this.chunkPos.add(var29, var76, var47));
         }
      }

      if (TerrainGen.decorate(var2, var3, this.chunkPos, EventType.CACTUS)) {
         for(int var30 = 0; var30 < this.cactiPerChunk; ++var30) {
            int var48 = var3.nextInt(16) + 8;
            int var63 = var3.nextInt(16) + 8;
            int var77 = var2.getHeight(this.chunkPos.add(var48, 0, var63)).getY() * 2;
            if (var77 > 0) {
               int var87 = var3.nextInt(var77);
               this.cactusGen.generate(var2, var3, this.chunkPos.add(var48, var87, var63));
            }
         }
      }

      if (this.generateLakes) {
         if (TerrainGen.decorate(var2, var3, this.chunkPos, EventType.LAKE_WATER)) {
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
         }

         if (TerrainGen.decorate(var2, var3, this.chunkPos, EventType.LAKE_LAVA)) {
            for(int var32 = 0; var32 < 20; ++var32) {
               int var50 = var3.nextInt(16) + 8;
               int var65 = var3.nextInt(16) + 8;
               int var79 = var3.nextInt(var3.nextInt(var3.nextInt(240) + 8) + 8);
               BlockPos var89 = this.chunkPos.add(var50, var79, var65);
               (new WorldGenLiquids(Blocks.FLOWING_LAVA)).generate(var2, var3, var89);
            }
         }
      }

      MinecraftForge.EVENT_BUS.post(new Post(var2, var3, this.chunkPos));
   }

   protected void generateOres(World var1, Random var2) {
      MinecraftForge.ORE_GEN_BUS.post(new net.minecraftforge.event.terraingen.OreGenEvent.Pre(var1, var2, this.chunkPos));
      if (TerrainGen.generateOre(var1, var2, this.dirtGen, this.chunkPos, net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.DIRT)) {
         this.genStandardOre1(var1, var2, this.chunkProviderSettings.dirtCount, this.dirtGen, this.chunkProviderSettings.dirtMinHeight, this.chunkProviderSettings.dirtMaxHeight);
      }

      if (TerrainGen.generateOre(var1, var2, this.gravelGen, this.chunkPos, net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.GRAVEL)) {
         this.genStandardOre1(var1, var2, this.chunkProviderSettings.gravelCount, this.gravelGen, this.chunkProviderSettings.gravelMinHeight, this.chunkProviderSettings.gravelMaxHeight);
      }

      if (TerrainGen.generateOre(var1, var2, this.dioriteGen, this.chunkPos, net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.DIORITE)) {
         this.genStandardOre1(var1, var2, this.chunkProviderSettings.dioriteCount, this.dioriteGen, this.chunkProviderSettings.dioriteMinHeight, this.chunkProviderSettings.dioriteMaxHeight);
      }

      if (TerrainGen.generateOre(var1, var2, this.graniteGen, this.chunkPos, net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.GRANITE)) {
         this.genStandardOre1(var1, var2, this.chunkProviderSettings.graniteCount, this.graniteGen, this.chunkProviderSettings.graniteMinHeight, this.chunkProviderSettings.graniteMaxHeight);
      }

      if (TerrainGen.generateOre(var1, var2, this.andesiteGen, this.chunkPos, net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.ANDESITE)) {
         this.genStandardOre1(var1, var2, this.chunkProviderSettings.andesiteCount, this.andesiteGen, this.chunkProviderSettings.andesiteMinHeight, this.chunkProviderSettings.andesiteMaxHeight);
      }

      if (TerrainGen.generateOre(var1, var2, this.coalGen, this.chunkPos, net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.COAL)) {
         this.genStandardOre1(var1, var2, this.chunkProviderSettings.coalCount, this.coalGen, this.chunkProviderSettings.coalMinHeight, this.chunkProviderSettings.coalMaxHeight);
      }

      if (TerrainGen.generateOre(var1, var2, this.ironGen, this.chunkPos, net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.IRON)) {
         this.genStandardOre1(var1, var2, this.chunkProviderSettings.ironCount, this.ironGen, this.chunkProviderSettings.ironMinHeight, this.chunkProviderSettings.ironMaxHeight);
      }

      if (TerrainGen.generateOre(var1, var2, this.goldGen, this.chunkPos, net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.GOLD)) {
         this.genStandardOre1(var1, var2, this.chunkProviderSettings.goldCount, this.goldGen, this.chunkProviderSettings.goldMinHeight, this.chunkProviderSettings.goldMaxHeight);
      }

      if (TerrainGen.generateOre(var1, var2, this.redstoneGen, this.chunkPos, net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.REDSTONE)) {
         this.genStandardOre1(var1, var2, this.chunkProviderSettings.redstoneCount, this.redstoneGen, this.chunkProviderSettings.redstoneMinHeight, this.chunkProviderSettings.redstoneMaxHeight);
      }

      if (TerrainGen.generateOre(var1, var2, this.diamondGen, this.chunkPos, net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.DIAMOND)) {
         this.genStandardOre1(var1, var2, this.chunkProviderSettings.diamondCount, this.diamondGen, this.chunkProviderSettings.diamondMinHeight, this.chunkProviderSettings.diamondMaxHeight);
      }

      if (TerrainGen.generateOre(var1, var2, this.lapisGen, this.chunkPos, net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType.LAPIS)) {
         this.genStandardOre2(var1, var2, this.chunkProviderSettings.lapisCount, this.lapisGen, this.chunkProviderSettings.lapisCenterHeight, this.chunkProviderSettings.lapisSpread);
      }

      MinecraftForge.ORE_GEN_BUS.post(new net.minecraftforge.event.terraingen.OreGenEvent.Post(var1, var2, this.chunkPos));
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
