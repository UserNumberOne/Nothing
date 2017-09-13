package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDesert;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.biome.BiomeSavanna;
import net.minecraft.world.biome.BiomeTaiga;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.BiomeEvent.GetVillageBlockID;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;

public class StructureVillagePieces {
   public static void registerVillagePieces() {
      MapGenStructureIO.registerStructureComponent(StructureVillagePieces.House1.class, "ViBH");
      MapGenStructureIO.registerStructureComponent(StructureVillagePieces.Field1.class, "ViDF");
      MapGenStructureIO.registerStructureComponent(StructureVillagePieces.Field2.class, "ViF");
      MapGenStructureIO.registerStructureComponent(StructureVillagePieces.Torch.class, "ViL");
      MapGenStructureIO.registerStructureComponent(StructureVillagePieces.Hall.class, "ViPH");
      MapGenStructureIO.registerStructureComponent(StructureVillagePieces.House4Garden.class, "ViSH");
      MapGenStructureIO.registerStructureComponent(StructureVillagePieces.WoodHut.class, "ViSmH");
      MapGenStructureIO.registerStructureComponent(StructureVillagePieces.Church.class, "ViST");
      MapGenStructureIO.registerStructureComponent(StructureVillagePieces.House2.class, "ViS");
      MapGenStructureIO.registerStructureComponent(StructureVillagePieces.Start.class, "ViStart");
      MapGenStructureIO.registerStructureComponent(StructureVillagePieces.Path.class, "ViSR");
      MapGenStructureIO.registerStructureComponent(StructureVillagePieces.House3.class, "ViTRH");
      MapGenStructureIO.registerStructureComponent(StructureVillagePieces.Well.class, "ViW");
   }

   public static List getStructureVillageWeightedPieceList(Random var0, int var1) {
      ArrayList var2 = Lists.newArrayList();
      var2.add(new StructureVillagePieces.PieceWeight(StructureVillagePieces.House4Garden.class, 4, MathHelper.getInt(var0, 2 + var1, 4 + var1 * 2)));
      var2.add(new StructureVillagePieces.PieceWeight(StructureVillagePieces.Church.class, 20, MathHelper.getInt(var0, 0 + var1, 1 + var1)));
      var2.add(new StructureVillagePieces.PieceWeight(StructureVillagePieces.House1.class, 20, MathHelper.getInt(var0, 0 + var1, 2 + var1)));
      var2.add(new StructureVillagePieces.PieceWeight(StructureVillagePieces.WoodHut.class, 3, MathHelper.getInt(var0, 2 + var1, 5 + var1 * 3)));
      var2.add(new StructureVillagePieces.PieceWeight(StructureVillagePieces.Hall.class, 15, MathHelper.getInt(var0, 0 + var1, 2 + var1)));
      var2.add(new StructureVillagePieces.PieceWeight(StructureVillagePieces.Field1.class, 3, MathHelper.getInt(var0, 1 + var1, 4 + var1)));
      var2.add(new StructureVillagePieces.PieceWeight(StructureVillagePieces.Field2.class, 3, MathHelper.getInt(var0, 2 + var1, 4 + var1 * 2)));
      var2.add(new StructureVillagePieces.PieceWeight(StructureVillagePieces.House2.class, 15, MathHelper.getInt(var0, 0, 1 + var1)));
      var2.add(new StructureVillagePieces.PieceWeight(StructureVillagePieces.House3.class, 8, MathHelper.getInt(var0, 0 + var1, 3 + var1 * 2)));
      VillagerRegistry.addExtraVillageComponents(var2, var0, var1);
      Iterator var3 = var2.iterator();

      while(var3.hasNext()) {
         if (((StructureVillagePieces.PieceWeight)var3.next()).villagePiecesLimit == 0) {
            var3.remove();
         }
      }

      return var2;
   }

   private static int updatePieceWeight(List var0) {
      boolean var1 = false;
      int var2 = 0;

      for(StructureVillagePieces.PieceWeight var4 : var0) {
         if (var4.villagePiecesLimit > 0 && var4.villagePiecesSpawned < var4.villagePiecesLimit) {
            var1 = true;
         }

         var2 += var4.villagePieceWeight;
      }

      return var1 ? var2 : -1;
   }

   private static StructureVillagePieces.Village findAndCreateComponentFactory(StructureVillagePieces.Start var0, StructureVillagePieces.PieceWeight var1, List var2, Random var3, int var4, int var5, int var6, EnumFacing var7, int var8) {
      Class var9 = var1.villagePieceClass;
      Object var10 = null;
      if (var9 == StructureVillagePieces.House4Garden.class) {
         var10 = StructureVillagePieces.House4Garden.createPiece(var0, var2, var3, var4, var5, var6, var7, var8);
      } else if (var9 == StructureVillagePieces.Church.class) {
         var10 = StructureVillagePieces.Church.createPiece(var0, var2, var3, var4, var5, var6, var7, var8);
      } else if (var9 == StructureVillagePieces.House1.class) {
         var10 = StructureVillagePieces.House1.createPiece(var0, var2, var3, var4, var5, var6, var7, var8);
      } else if (var9 == StructureVillagePieces.WoodHut.class) {
         var10 = StructureVillagePieces.WoodHut.createPiece(var0, var2, var3, var4, var5, var6, var7, var8);
      } else if (var9 == StructureVillagePieces.Hall.class) {
         var10 = StructureVillagePieces.Hall.createPiece(var0, var2, var3, var4, var5, var6, var7, var8);
      } else if (var9 == StructureVillagePieces.Field1.class) {
         var10 = StructureVillagePieces.Field1.createPiece(var0, var2, var3, var4, var5, var6, var7, var8);
      } else if (var9 == StructureVillagePieces.Field2.class) {
         var10 = StructureVillagePieces.Field2.createPiece(var0, var2, var3, var4, var5, var6, var7, var8);
      } else if (var9 == StructureVillagePieces.House2.class) {
         var10 = StructureVillagePieces.House2.createPiece(var0, var2, var3, var4, var5, var6, var7, var8);
      } else if (var9 == StructureVillagePieces.House3.class) {
         var10 = StructureVillagePieces.House3.createPiece(var0, var2, var3, var4, var5, var6, var7, var8);
      } else {
         var10 = VillagerRegistry.getVillageComponent(var1, var0, var2, var3, var4, var5, var6, var7, var8);
      }

      return (StructureVillagePieces.Village)var10;
   }

   private static StructureVillagePieces.Village generateComponent(StructureVillagePieces.Start var0, List var1, Random var2, int var3, int var4, int var5, EnumFacing var6, int var7) {
      int var8 = updatePieceWeight(var0.structureVillageWeightedPieceList);
      if (var8 <= 0) {
         return null;
      } else {
         int var9 = 0;

         while(var9 < 5) {
            ++var9;
            int var10 = var2.nextInt(var8);

            for(StructureVillagePieces.PieceWeight var12 : var0.structureVillageWeightedPieceList) {
               var10 -= var12.villagePieceWeight;
               if (var10 < 0) {
                  if (!var12.canSpawnMoreVillagePiecesOfType(var7) || var12 == var0.structVillagePieceWeight && var0.structureVillageWeightedPieceList.size() > 1) {
                     break;
                  }

                  StructureVillagePieces.Village var13 = findAndCreateComponentFactory(var0, var12, var1, var2, var3, var4, var5, var6, var7);
                  if (var13 != null) {
                     ++var12.villagePiecesSpawned;
                     var0.structVillagePieceWeight = var12;
                     if (!var12.canSpawnMoreVillagePieces()) {
                        var0.structureVillageWeightedPieceList.remove(var12);
                     }

                     return var13;
                  }
               }
            }
         }

         StructureBoundingBox var14 = StructureVillagePieces.Torch.findPieceBox(var0, var1, var2, var3, var4, var5, var6);
         if (var14 != null) {
            return new StructureVillagePieces.Torch(var0, var7, var2, var14, var6);
         } else {
            return null;
         }
      }
   }

   private static StructureComponent generateAndAddComponent(StructureVillagePieces.Start var0, List var1, Random var2, int var3, int var4, int var5, EnumFacing var6, int var7) {
      if (var7 > 50) {
         return null;
      } else if (Math.abs(var3 - var0.getBoundingBox().minX) <= 112 && Math.abs(var5 - var0.getBoundingBox().minZ) <= 112) {
         StructureVillagePieces.Village var8 = generateComponent(var0, var1, var2, var3, var4, var5, var6, var7 + 1);
         if (var8 != null) {
            var1.add(var8);
            var0.pendingHouses.add(var8);
            return var8;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static StructureComponent generateAndAddRoadPiece(StructureVillagePieces.Start var0, List var1, Random var2, int var3, int var4, int var5, EnumFacing var6, int var7) {
      if (var7 > 3 + var0.terrainType) {
         return null;
      } else if (Math.abs(var3 - var0.getBoundingBox().minX) <= 112 && Math.abs(var5 - var0.getBoundingBox().minZ) <= 112) {
         StructureBoundingBox var8 = StructureVillagePieces.Path.findPieceBox(var0, var1, var2, var3, var4, var5, var6);
         if (var8 != null && var8.minY > 10) {
            StructureVillagePieces.Path var9 = new StructureVillagePieces.Path(var0, var7, var2, var8, var6);
            var1.add(var9);
            var0.pendingRoads.add(var9);
            return var9;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static class Church extends StructureVillagePieces.Village {
      public Church() {
      }

      public Church(StructureVillagePieces.Start var1, int var2, Random var3, StructureBoundingBox var4, EnumFacing var5) {
         super(var1, var2);
         this.setCoordBaseMode(var5);
         this.boundingBox = var4;
      }

      public static StructureVillagePieces.Church createPiece(StructureVillagePieces.Start var0, List var1, Random var2, int var3, int var4, int var5, EnumFacing var6, int var7) {
         StructureBoundingBox var8 = StructureBoundingBox.getComponentToAddBoundingBox(var3, var4, var5, 0, 0, 0, 5, 12, 9, var6);
         return canVillageGoDeeper(var8) && StructureComponent.findIntersecting(var1, var8) == null ? new StructureVillagePieces.Church(var0, var7, var2, var8, var6) : null;
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(var1, var3);
            if (this.averageGroundLvl < 0) {
               return true;
            }

            this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 12 - 1, 0);
         }

         IBlockState var4 = Blocks.COBBLESTONE.getDefaultState();
         IBlockState var5 = this.getBiomeSpecificBlockState(Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH));
         IBlockState var6 = this.getBiomeSpecificBlockState(Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.WEST));
         IBlockState var7 = this.getBiomeSpecificBlockState(Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST));
         this.fillWithBlocks(var1, var3, 1, 1, 1, 3, 3, 7, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 1, 5, 1, 3, 9, 3, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 1, 0, 0, 3, 0, 8, var4, var4, false);
         this.fillWithBlocks(var1, var3, 1, 1, 0, 3, 10, 0, var4, var4, false);
         this.fillWithBlocks(var1, var3, 0, 1, 1, 0, 10, 3, var4, var4, false);
         this.fillWithBlocks(var1, var3, 4, 1, 1, 4, 10, 3, var4, var4, false);
         this.fillWithBlocks(var1, var3, 0, 0, 4, 0, 4, 7, var4, var4, false);
         this.fillWithBlocks(var1, var3, 4, 0, 4, 4, 4, 7, var4, var4, false);
         this.fillWithBlocks(var1, var3, 1, 1, 8, 3, 4, 8, var4, var4, false);
         this.fillWithBlocks(var1, var3, 1, 5, 4, 3, 10, 4, var4, var4, false);
         this.fillWithBlocks(var1, var3, 1, 5, 5, 3, 5, 7, var4, var4, false);
         this.fillWithBlocks(var1, var3, 0, 9, 0, 4, 9, 4, var4, var4, false);
         this.fillWithBlocks(var1, var3, 0, 4, 0, 4, 4, 4, var4, var4, false);
         this.setBlockState(var1, var4, 0, 11, 2, var3);
         this.setBlockState(var1, var4, 4, 11, 2, var3);
         this.setBlockState(var1, var4, 2, 11, 0, var3);
         this.setBlockState(var1, var4, 2, 11, 4, var3);
         this.setBlockState(var1, var4, 1, 1, 6, var3);
         this.setBlockState(var1, var4, 1, 1, 7, var3);
         this.setBlockState(var1, var4, 2, 1, 7, var3);
         this.setBlockState(var1, var4, 3, 1, 6, var3);
         this.setBlockState(var1, var4, 3, 1, 7, var3);
         this.setBlockState(var1, var5, 1, 1, 5, var3);
         this.setBlockState(var1, var5, 2, 1, 6, var3);
         this.setBlockState(var1, var5, 3, 1, 5, var3);
         this.setBlockState(var1, var6, 1, 2, 7, var3);
         this.setBlockState(var1, var7, 3, 2, 7, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 0, 2, 2, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 0, 3, 2, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 4, 2, 2, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 4, 3, 2, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 0, 6, 2, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 0, 7, 2, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 4, 6, 2, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 4, 7, 2, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 2, 6, 0, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 2, 7, 0, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 2, 6, 4, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 2, 7, 4, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 0, 3, 6, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 4, 3, 6, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 2, 3, 8, var3);
         this.func_189926_a(var1, EnumFacing.SOUTH, 2, 4, 7, var3);
         this.func_189926_a(var1, EnumFacing.EAST, 1, 4, 6, var3);
         this.func_189926_a(var1, EnumFacing.WEST, 3, 4, 6, var3);
         this.func_189926_a(var1, EnumFacing.NORTH, 2, 4, 5, var3);
         IBlockState var8 = Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.WEST);

         for(int var9 = 1; var9 <= 9; ++var9) {
            this.setBlockState(var1, var8, 3, var9, 3, var3);
         }

         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 2, 1, 0, var3);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 2, 2, 0, var3);
         this.func_189927_a(var1, var3, var2, 2, 1, 0, EnumFacing.NORTH);
         if (this.getBlockStateFromPos(var1, 2, 0, -1, var3).getMaterial() == Material.AIR && this.getBlockStateFromPos(var1, 2, -1, -1, var3).getMaterial() != Material.AIR) {
            this.setBlockState(var1, var5, 2, 0, -1, var3);
            if (this.getBlockStateFromPos(var1, 2, -1, -1, var3).getBlock() == Blocks.GRASS_PATH) {
               this.setBlockState(var1, Blocks.GRASS.getDefaultState(), 2, -1, -1, var3);
            }
         }

         for(int var11 = 0; var11 < 9; ++var11) {
            for(int var10 = 0; var10 < 5; ++var10) {
               this.clearCurrentPositionBlocksUpwards(var1, var10, 12, var11, var3);
               this.replaceAirAndLiquidDownwards(var1, var4, var10, -1, var11, var3);
            }
         }

         this.spawnVillagers(var1, var3, 2, 1, 2, 1);
         return true;
      }

      protected int chooseProfession(int var1, int var2) {
         return 2;
      }
   }

   public static class Field1 extends StructureVillagePieces.Village {
      private Block cropTypeA;
      private Block cropTypeB;
      private Block cropTypeC;
      private Block cropTypeD;

      public Field1() {
      }

      public Field1(StructureVillagePieces.Start var1, int var2, Random var3, StructureBoundingBox var4, EnumFacing var5) {
         super(var1, var2);
         this.setCoordBaseMode(var5);
         this.boundingBox = var4;
         this.cropTypeA = this.getRandomCropType(var3);
         this.cropTypeB = this.getRandomCropType(var3);
         this.cropTypeC = this.getRandomCropType(var3);
         this.cropTypeD = this.getRandomCropType(var3);
      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         super.writeStructureToNBT(var1);
         var1.setInteger("CA", Block.REGISTRY.getIDForObject(this.cropTypeA));
         var1.setInteger("CB", Block.REGISTRY.getIDForObject(this.cropTypeB));
         var1.setInteger("CC", Block.REGISTRY.getIDForObject(this.cropTypeC));
         var1.setInteger("CD", Block.REGISTRY.getIDForObject(this.cropTypeD));
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         super.readStructureFromNBT(var1);
         this.cropTypeA = Block.getBlockById(var1.getInteger("CA"));
         this.cropTypeB = Block.getBlockById(var1.getInteger("CB"));
         this.cropTypeC = Block.getBlockById(var1.getInteger("CC"));
         this.cropTypeD = Block.getBlockById(var1.getInteger("CD"));
         if (!(this.cropTypeA instanceof BlockCrops)) {
            this.cropTypeA = Blocks.WHEAT;
         }

         if (!(this.cropTypeB instanceof BlockCrops)) {
            this.cropTypeB = Blocks.CARROTS;
         }

         if (!(this.cropTypeC instanceof BlockCrops)) {
            this.cropTypeC = Blocks.POTATOES;
         }

         if (!(this.cropTypeD instanceof BlockCrops)) {
            this.cropTypeD = Blocks.BEETROOTS;
         }

      }

      private Block getRandomCropType(Random var1) {
         switch(var1.nextInt(10)) {
         case 0:
         case 1:
            return Blocks.CARROTS;
         case 2:
         case 3:
            return Blocks.POTATOES;
         case 4:
            return Blocks.BEETROOTS;
         default:
            return Blocks.WHEAT;
         }
      }

      public static StructureVillagePieces.Field1 createPiece(StructureVillagePieces.Start var0, List var1, Random var2, int var3, int var4, int var5, EnumFacing var6, int var7) {
         StructureBoundingBox var8 = StructureBoundingBox.getComponentToAddBoundingBox(var3, var4, var5, 0, 0, 0, 13, 4, 9, var6);
         return canVillageGoDeeper(var8) && StructureComponent.findIntersecting(var1, var8) == null ? new StructureVillagePieces.Field1(var0, var7, var2, var8, var6) : null;
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(var1, var3);
            if (this.averageGroundLvl < 0) {
               return true;
            }

            this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 4 - 1, 0);
         }

         IBlockState var4 = this.getBiomeSpecificBlockState(Blocks.LOG.getDefaultState());
         this.fillWithBlocks(var1, var3, 0, 1, 0, 12, 4, 8, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 1, 0, 1, 2, 0, 7, Blocks.FARMLAND.getDefaultState(), Blocks.FARMLAND.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 4, 0, 1, 5, 0, 7, Blocks.FARMLAND.getDefaultState(), Blocks.FARMLAND.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 7, 0, 1, 8, 0, 7, Blocks.FARMLAND.getDefaultState(), Blocks.FARMLAND.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 10, 0, 1, 11, 0, 7, Blocks.FARMLAND.getDefaultState(), Blocks.FARMLAND.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 0, 0, 0, 0, 0, 8, var4, var4, false);
         this.fillWithBlocks(var1, var3, 6, 0, 0, 6, 0, 8, var4, var4, false);
         this.fillWithBlocks(var1, var3, 12, 0, 0, 12, 0, 8, var4, var4, false);
         this.fillWithBlocks(var1, var3, 1, 0, 0, 11, 0, 0, var4, var4, false);
         this.fillWithBlocks(var1, var3, 1, 0, 8, 11, 0, 8, var4, var4, false);
         this.fillWithBlocks(var1, var3, 3, 0, 1, 3, 0, 7, Blocks.WATER.getDefaultState(), Blocks.WATER.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 9, 0, 1, 9, 0, 7, Blocks.WATER.getDefaultState(), Blocks.WATER.getDefaultState(), false);

         for(int var5 = 1; var5 <= 7; ++var5) {
            int var6 = ((BlockCrops)this.cropTypeA).getMaxAge();
            int var7 = var6 / 3;
            this.setBlockState(var1, this.cropTypeA.getStateFromMeta(MathHelper.getInt(var2, var7, var6)), 1, 1, var5, var3);
            this.setBlockState(var1, this.cropTypeA.getStateFromMeta(MathHelper.getInt(var2, var7, var6)), 2, 1, var5, var3);
            int var8 = ((BlockCrops)this.cropTypeB).getMaxAge();
            int var9 = var8 / 3;
            this.setBlockState(var1, this.cropTypeB.getStateFromMeta(MathHelper.getInt(var2, var9, var8)), 4, 1, var5, var3);
            this.setBlockState(var1, this.cropTypeB.getStateFromMeta(MathHelper.getInt(var2, var9, var8)), 5, 1, var5, var3);
            int var10 = ((BlockCrops)this.cropTypeC).getMaxAge();
            int var11 = var10 / 3;
            this.setBlockState(var1, this.cropTypeC.getStateFromMeta(MathHelper.getInt(var2, var11, var10)), 7, 1, var5, var3);
            this.setBlockState(var1, this.cropTypeC.getStateFromMeta(MathHelper.getInt(var2, var11, var10)), 8, 1, var5, var3);
            int var12 = ((BlockCrops)this.cropTypeD).getMaxAge();
            int var13 = var12 / 3;
            this.setBlockState(var1, this.cropTypeD.getStateFromMeta(MathHelper.getInt(var2, var13, var12)), 10, 1, var5, var3);
            this.setBlockState(var1, this.cropTypeD.getStateFromMeta(MathHelper.getInt(var2, var13, var12)), 11, 1, var5, var3);
         }

         for(int var14 = 0; var14 < 9; ++var14) {
            for(int var15 = 0; var15 < 13; ++var15) {
               this.clearCurrentPositionBlocksUpwards(var1, var15, 4, var14, var3);
               this.replaceAirAndLiquidDownwards(var1, Blocks.DIRT.getDefaultState(), var15, -1, var14, var3);
            }
         }

         return true;
      }
   }

   public static class Field2 extends StructureVillagePieces.Village {
      private Block cropTypeA;
      private Block cropTypeB;

      public Field2() {
      }

      public Field2(StructureVillagePieces.Start var1, int var2, Random var3, StructureBoundingBox var4, EnumFacing var5) {
         super(var1, var2);
         this.setCoordBaseMode(var5);
         this.boundingBox = var4;
         this.cropTypeA = this.getRandomCropType(var3);
         this.cropTypeB = this.getRandomCropType(var3);
      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         super.writeStructureToNBT(var1);
         var1.setInteger("CA", Block.REGISTRY.getIDForObject(this.cropTypeA));
         var1.setInteger("CB", Block.REGISTRY.getIDForObject(this.cropTypeB));
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         super.readStructureFromNBT(var1);
         this.cropTypeA = Block.getBlockById(var1.getInteger("CA"));
         this.cropTypeB = Block.getBlockById(var1.getInteger("CB"));
      }

      private Block getRandomCropType(Random var1) {
         switch(var1.nextInt(10)) {
         case 0:
         case 1:
            return Blocks.CARROTS;
         case 2:
         case 3:
            return Blocks.POTATOES;
         case 4:
            return Blocks.BEETROOTS;
         default:
            return Blocks.WHEAT;
         }
      }

      public static StructureVillagePieces.Field2 createPiece(StructureVillagePieces.Start var0, List var1, Random var2, int var3, int var4, int var5, EnumFacing var6, int var7) {
         StructureBoundingBox var8 = StructureBoundingBox.getComponentToAddBoundingBox(var3, var4, var5, 0, 0, 0, 7, 4, 9, var6);
         return canVillageGoDeeper(var8) && StructureComponent.findIntersecting(var1, var8) == null ? new StructureVillagePieces.Field2(var0, var7, var2, var8, var6) : null;
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(var1, var3);
            if (this.averageGroundLvl < 0) {
               return true;
            }

            this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 4 - 1, 0);
         }

         IBlockState var4 = this.getBiomeSpecificBlockState(Blocks.LOG.getDefaultState());
         this.fillWithBlocks(var1, var3, 0, 1, 0, 6, 4, 8, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 1, 0, 1, 2, 0, 7, Blocks.FARMLAND.getDefaultState(), Blocks.FARMLAND.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 4, 0, 1, 5, 0, 7, Blocks.FARMLAND.getDefaultState(), Blocks.FARMLAND.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 0, 0, 0, 0, 0, 8, var4, var4, false);
         this.fillWithBlocks(var1, var3, 6, 0, 0, 6, 0, 8, var4, var4, false);
         this.fillWithBlocks(var1, var3, 1, 0, 0, 5, 0, 0, var4, var4, false);
         this.fillWithBlocks(var1, var3, 1, 0, 8, 5, 0, 8, var4, var4, false);
         this.fillWithBlocks(var1, var3, 3, 0, 1, 3, 0, 7, Blocks.WATER.getDefaultState(), Blocks.WATER.getDefaultState(), false);

         for(int var5 = 1; var5 <= 7; ++var5) {
            int var6 = ((BlockCrops)this.cropTypeA).getMaxAge();
            int var7 = var6 / 3;
            this.setBlockState(var1, this.cropTypeA.getStateFromMeta(MathHelper.getInt(var2, var7, var6)), 1, 1, var5, var3);
            this.setBlockState(var1, this.cropTypeA.getStateFromMeta(MathHelper.getInt(var2, var7, var6)), 2, 1, var5, var3);
            int var8 = ((BlockCrops)this.cropTypeB).getMaxAge();
            int var9 = var8 / 3;
            this.setBlockState(var1, this.cropTypeB.getStateFromMeta(MathHelper.getInt(var2, var9, var8)), 4, 1, var5, var3);
            this.setBlockState(var1, this.cropTypeB.getStateFromMeta(MathHelper.getInt(var2, var9, var8)), 5, 1, var5, var3);
         }

         for(int var10 = 0; var10 < 9; ++var10) {
            for(int var11 = 0; var11 < 7; ++var11) {
               this.clearCurrentPositionBlocksUpwards(var1, var11, 4, var10, var3);
               this.replaceAirAndLiquidDownwards(var1, Blocks.DIRT.getDefaultState(), var11, -1, var10, var3);
            }
         }

         return true;
      }
   }

   public static class Hall extends StructureVillagePieces.Village {
      public Hall() {
      }

      public Hall(StructureVillagePieces.Start var1, int var2, Random var3, StructureBoundingBox var4, EnumFacing var5) {
         super(var1, var2);
         this.setCoordBaseMode(var5);
         this.boundingBox = var4;
      }

      public static StructureVillagePieces.Hall createPiece(StructureVillagePieces.Start var0, List var1, Random var2, int var3, int var4, int var5, EnumFacing var6, int var7) {
         StructureBoundingBox var8 = StructureBoundingBox.getComponentToAddBoundingBox(var3, var4, var5, 0, 0, 0, 9, 7, 11, var6);
         return canVillageGoDeeper(var8) && StructureComponent.findIntersecting(var1, var8) == null ? new StructureVillagePieces.Hall(var0, var7, var2, var8, var6) : null;
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(var1, var3);
            if (this.averageGroundLvl < 0) {
               return true;
            }

            this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 7 - 1, 0);
         }

         IBlockState var4 = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE.getDefaultState());
         IBlockState var5 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH));
         IBlockState var6 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH));
         IBlockState var7 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.WEST));
         IBlockState var8 = this.getBiomeSpecificBlockState(Blocks.PLANKS.getDefaultState());
         IBlockState var9 = this.getBiomeSpecificBlockState(Blocks.LOG.getDefaultState());
         IBlockState var10 = this.getBiomeSpecificBlockState(Blocks.OAK_FENCE.getDefaultState());
         this.fillWithBlocks(var1, var3, 1, 1, 1, 7, 4, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 2, 1, 6, 8, 4, 10, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 2, 0, 6, 8, 0, 10, Blocks.DIRT.getDefaultState(), Blocks.DIRT.getDefaultState(), false);
         this.setBlockState(var1, var4, 6, 0, 6, var3);
         this.fillWithBlocks(var1, var3, 2, 1, 6, 2, 1, 10, var10, var10, false);
         this.fillWithBlocks(var1, var3, 8, 1, 6, 8, 1, 10, var10, var10, false);
         this.fillWithBlocks(var1, var3, 3, 1, 10, 7, 1, 10, var10, var10, false);
         this.fillWithBlocks(var1, var3, 1, 0, 1, 7, 0, 4, var8, var8, false);
         this.fillWithBlocks(var1, var3, 0, 0, 0, 0, 3, 5, var4, var4, false);
         this.fillWithBlocks(var1, var3, 8, 0, 0, 8, 3, 5, var4, var4, false);
         this.fillWithBlocks(var1, var3, 1, 0, 0, 7, 1, 0, var4, var4, false);
         this.fillWithBlocks(var1, var3, 1, 0, 5, 7, 1, 5, var4, var4, false);
         this.fillWithBlocks(var1, var3, 1, 2, 0, 7, 3, 0, var8, var8, false);
         this.fillWithBlocks(var1, var3, 1, 2, 5, 7, 3, 5, var8, var8, false);
         this.fillWithBlocks(var1, var3, 0, 4, 1, 8, 4, 1, var8, var8, false);
         this.fillWithBlocks(var1, var3, 0, 4, 4, 8, 4, 4, var8, var8, false);
         this.fillWithBlocks(var1, var3, 0, 5, 2, 8, 5, 3, var8, var8, false);
         this.setBlockState(var1, var8, 0, 4, 2, var3);
         this.setBlockState(var1, var8, 0, 4, 3, var3);
         this.setBlockState(var1, var8, 8, 4, 2, var3);
         this.setBlockState(var1, var8, 8, 4, 3, var3);
         IBlockState var11 = var5;
         IBlockState var12 = var6;

         for(int var13 = -1; var13 <= 2; ++var13) {
            for(int var14 = 0; var14 <= 8; ++var14) {
               this.setBlockState(var1, var11, var14, 4 + var13, var13, var3);
               this.setBlockState(var1, var12, var14, 4 + var13, 5 - var13, var3);
            }
         }

         this.setBlockState(var1, var9, 0, 2, 1, var3);
         this.setBlockState(var1, var9, 0, 2, 4, var3);
         this.setBlockState(var1, var9, 8, 2, 1, var3);
         this.setBlockState(var1, var9, 8, 2, 4, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 0, 2, 2, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 0, 2, 3, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 8, 2, 2, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 8, 2, 3, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 2, 2, 5, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 3, 2, 5, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 5, 2, 0, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 6, 2, 5, var3);
         this.setBlockState(var1, var10, 2, 1, 3, var3);
         this.setBlockState(var1, Blocks.WOODEN_PRESSURE_PLATE.getDefaultState(), 2, 2, 3, var3);
         this.setBlockState(var1, var8, 1, 1, 4, var3);
         this.setBlockState(var1, var11, 2, 1, 4, var3);
         this.setBlockState(var1, var7, 1, 1, 3, var3);
         this.fillWithBlocks(var1, var3, 5, 0, 1, 7, 0, 3, Blocks.DOUBLE_STONE_SLAB.getDefaultState(), Blocks.DOUBLE_STONE_SLAB.getDefaultState(), false);
         this.setBlockState(var1, Blocks.DOUBLE_STONE_SLAB.getDefaultState(), 6, 1, 1, var3);
         this.setBlockState(var1, Blocks.DOUBLE_STONE_SLAB.getDefaultState(), 6, 1, 2, var3);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 2, 1, 0, var3);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 2, 2, 0, var3);
         this.func_189926_a(var1, EnumFacing.NORTH, 2, 3, 1, var3);
         this.func_189927_a(var1, var3, var2, 2, 1, 0, EnumFacing.NORTH);
         if (this.getBlockStateFromPos(var1, 2, 0, -1, var3).getMaterial() == Material.AIR && this.getBlockStateFromPos(var1, 2, -1, -1, var3).getMaterial() != Material.AIR) {
            this.setBlockState(var1, var11, 2, 0, -1, var3);
            if (this.getBlockStateFromPos(var1, 2, -1, -1, var3).getBlock() == Blocks.GRASS_PATH) {
               this.setBlockState(var1, Blocks.GRASS.getDefaultState(), 2, -1, -1, var3);
            }
         }

         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 6, 1, 5, var3);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 6, 2, 5, var3);
         this.func_189926_a(var1, EnumFacing.SOUTH, 6, 3, 4, var3);
         this.func_189927_a(var1, var3, var2, 6, 1, 5, EnumFacing.SOUTH);

         for(int var15 = 0; var15 < 5; ++var15) {
            for(int var16 = 0; var16 < 9; ++var16) {
               this.clearCurrentPositionBlocksUpwards(var1, var16, 7, var15, var3);
               this.replaceAirAndLiquidDownwards(var1, var4, var16, -1, var15, var3);
            }
         }

         this.spawnVillagers(var1, var3, 4, 1, 2, 2);
         return true;
      }

      protected int chooseProfession(int var1, int var2) {
         return var1 == 0 ? 4 : super.chooseProfession(var1, var2);
      }
   }

   public static class House1 extends StructureVillagePieces.Village {
      public House1() {
      }

      public House1(StructureVillagePieces.Start var1, int var2, Random var3, StructureBoundingBox var4, EnumFacing var5) {
         super(var1, var2);
         this.setCoordBaseMode(var5);
         this.boundingBox = var4;
      }

      public static StructureVillagePieces.House1 createPiece(StructureVillagePieces.Start var0, List var1, Random var2, int var3, int var4, int var5, EnumFacing var6, int var7) {
         StructureBoundingBox var8 = StructureBoundingBox.getComponentToAddBoundingBox(var3, var4, var5, 0, 0, 0, 9, 9, 6, var6);
         return canVillageGoDeeper(var8) && StructureComponent.findIntersecting(var1, var8) == null ? new StructureVillagePieces.House1(var0, var7, var2, var8, var6) : null;
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(var1, var3);
            if (this.averageGroundLvl < 0) {
               return true;
            }

            this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 9 - 1, 0);
         }

         IBlockState var4 = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE.getDefaultState());
         IBlockState var5 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH));
         IBlockState var6 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH));
         IBlockState var7 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST));
         IBlockState var8 = this.getBiomeSpecificBlockState(Blocks.PLANKS.getDefaultState());
         IBlockState var9 = this.getBiomeSpecificBlockState(Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH));
         IBlockState var10 = this.getBiomeSpecificBlockState(Blocks.OAK_FENCE.getDefaultState());
         this.fillWithBlocks(var1, var3, 1, 1, 1, 7, 5, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 0, 0, 0, 8, 0, 5, var4, var4, false);
         this.fillWithBlocks(var1, var3, 0, 5, 0, 8, 5, 5, var4, var4, false);
         this.fillWithBlocks(var1, var3, 0, 6, 1, 8, 6, 4, var4, var4, false);
         this.fillWithBlocks(var1, var3, 0, 7, 2, 8, 7, 3, var4, var4, false);

         for(int var11 = -1; var11 <= 2; ++var11) {
            for(int var12 = 0; var12 <= 8; ++var12) {
               this.setBlockState(var1, var5, var12, 6 + var11, var11, var3);
               this.setBlockState(var1, var6, var12, 6 + var11, 5 - var11, var3);
            }
         }

         this.fillWithBlocks(var1, var3, 0, 1, 0, 0, 1, 5, var4, var4, false);
         this.fillWithBlocks(var1, var3, 1, 1, 5, 8, 1, 5, var4, var4, false);
         this.fillWithBlocks(var1, var3, 8, 1, 0, 8, 1, 4, var4, var4, false);
         this.fillWithBlocks(var1, var3, 2, 1, 0, 7, 1, 0, var4, var4, false);
         this.fillWithBlocks(var1, var3, 0, 2, 0, 0, 4, 0, var4, var4, false);
         this.fillWithBlocks(var1, var3, 0, 2, 5, 0, 4, 5, var4, var4, false);
         this.fillWithBlocks(var1, var3, 8, 2, 5, 8, 4, 5, var4, var4, false);
         this.fillWithBlocks(var1, var3, 8, 2, 0, 8, 4, 0, var4, var4, false);
         this.fillWithBlocks(var1, var3, 0, 2, 1, 0, 4, 4, var8, var8, false);
         this.fillWithBlocks(var1, var3, 1, 2, 5, 7, 4, 5, var8, var8, false);
         this.fillWithBlocks(var1, var3, 8, 2, 1, 8, 4, 4, var8, var8, false);
         this.fillWithBlocks(var1, var3, 1, 2, 0, 7, 4, 0, var8, var8, false);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 4, 2, 0, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 5, 2, 0, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 6, 2, 0, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 4, 3, 0, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 5, 3, 0, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 6, 3, 0, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 0, 2, 2, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 0, 2, 3, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 0, 3, 2, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 0, 3, 3, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 8, 2, 2, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 8, 2, 3, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 8, 3, 2, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 8, 3, 3, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 2, 2, 5, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 3, 2, 5, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 5, 2, 5, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 6, 2, 5, var3);
         this.fillWithBlocks(var1, var3, 1, 4, 1, 7, 4, 1, var8, var8, false);
         this.fillWithBlocks(var1, var3, 1, 4, 4, 7, 4, 4, var8, var8, false);
         this.fillWithBlocks(var1, var3, 1, 3, 4, 7, 3, 4, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
         this.setBlockState(var1, var8, 7, 1, 4, var3);
         this.setBlockState(var1, var7, 7, 1, 3, var3);
         this.setBlockState(var1, var5, 6, 1, 4, var3);
         this.setBlockState(var1, var5, 5, 1, 4, var3);
         this.setBlockState(var1, var5, 4, 1, 4, var3);
         this.setBlockState(var1, var5, 3, 1, 4, var3);
         this.setBlockState(var1, var10, 6, 1, 3, var3);
         this.setBlockState(var1, Blocks.WOODEN_PRESSURE_PLATE.getDefaultState(), 6, 2, 3, var3);
         this.setBlockState(var1, var10, 4, 1, 3, var3);
         this.setBlockState(var1, Blocks.WOODEN_PRESSURE_PLATE.getDefaultState(), 4, 2, 3, var3);
         this.setBlockState(var1, Blocks.CRAFTING_TABLE.getDefaultState(), 7, 1, 1, var3);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 1, 1, 0, var3);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 1, 2, 0, var3);
         this.func_189927_a(var1, var3, var2, 1, 1, 0, EnumFacing.NORTH);
         if (this.getBlockStateFromPos(var1, 1, 0, -1, var3).getMaterial() == Material.AIR && this.getBlockStateFromPos(var1, 1, -1, -1, var3).getMaterial() != Material.AIR) {
            this.setBlockState(var1, var9, 1, 0, -1, var3);
            if (this.getBlockStateFromPos(var1, 1, -1, -1, var3).getBlock() == Blocks.GRASS_PATH) {
               this.setBlockState(var1, Blocks.GRASS.getDefaultState(), 1, -1, -1, var3);
            }
         }

         for(int var13 = 0; var13 < 6; ++var13) {
            for(int var14 = 0; var14 < 9; ++var14) {
               this.clearCurrentPositionBlocksUpwards(var1, var14, 9, var13, var3);
               this.replaceAirAndLiquidDownwards(var1, var4, var14, -1, var13, var3);
            }
         }

         this.spawnVillagers(var1, var3, 2, 1, 2, 1);
         return true;
      }

      protected int chooseProfession(int var1, int var2) {
         return 1;
      }
   }

   public static class House2 extends StructureVillagePieces.Village {
      private boolean hasMadeChest;

      public House2() {
      }

      public House2(StructureVillagePieces.Start var1, int var2, Random var3, StructureBoundingBox var4, EnumFacing var5) {
         super(var1, var2);
         this.setCoordBaseMode(var5);
         this.boundingBox = var4;
      }

      public static StructureVillagePieces.House2 createPiece(StructureVillagePieces.Start var0, List var1, Random var2, int var3, int var4, int var5, EnumFacing var6, int var7) {
         StructureBoundingBox var8 = StructureBoundingBox.getComponentToAddBoundingBox(var3, var4, var5, 0, 0, 0, 10, 6, 7, var6);
         return canVillageGoDeeper(var8) && StructureComponent.findIntersecting(var1, var8) == null ? new StructureVillagePieces.House2(var0, var7, var2, var8, var6) : null;
      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         super.writeStructureToNBT(var1);
         var1.setBoolean("Chest", this.hasMadeChest);
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         super.readStructureFromNBT(var1);
         this.hasMadeChest = var1.getBoolean("Chest");
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(var1, var3);
            if (this.averageGroundLvl < 0) {
               return true;
            }

            this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 6 - 1, 0);
         }

         IBlockState var4 = Blocks.COBBLESTONE.getDefaultState();
         IBlockState var5 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH));
         IBlockState var6 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.WEST));
         IBlockState var7 = this.getBiomeSpecificBlockState(Blocks.PLANKS.getDefaultState());
         IBlockState var8 = this.getBiomeSpecificBlockState(Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH));
         IBlockState var9 = this.getBiomeSpecificBlockState(Blocks.LOG.getDefaultState());
         IBlockState var10 = this.getBiomeSpecificBlockState(Blocks.OAK_FENCE.getDefaultState());
         this.fillWithBlocks(var1, var3, 0, 1, 0, 9, 4, 6, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 0, 0, 0, 9, 0, 6, var4, var4, false);
         this.fillWithBlocks(var1, var3, 0, 4, 0, 9, 4, 6, var4, var4, false);
         this.fillWithBlocks(var1, var3, 0, 5, 0, 9, 5, 6, Blocks.STONE_SLAB.getDefaultState(), Blocks.STONE_SLAB.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 1, 5, 1, 8, 5, 5, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 1, 1, 0, 2, 3, 0, var7, var7, false);
         this.fillWithBlocks(var1, var3, 0, 1, 0, 0, 4, 0, var9, var9, false);
         this.fillWithBlocks(var1, var3, 3, 1, 0, 3, 4, 0, var9, var9, false);
         this.fillWithBlocks(var1, var3, 0, 1, 6, 0, 4, 6, var9, var9, false);
         this.setBlockState(var1, var7, 3, 3, 1, var3);
         this.fillWithBlocks(var1, var3, 3, 1, 2, 3, 3, 2, var7, var7, false);
         this.fillWithBlocks(var1, var3, 4, 1, 3, 5, 3, 3, var7, var7, false);
         this.fillWithBlocks(var1, var3, 0, 1, 1, 0, 3, 5, var7, var7, false);
         this.fillWithBlocks(var1, var3, 1, 1, 6, 5, 3, 6, var7, var7, false);
         this.fillWithBlocks(var1, var3, 5, 1, 0, 5, 3, 0, var10, var10, false);
         this.fillWithBlocks(var1, var3, 9, 1, 0, 9, 3, 0, var10, var10, false);
         this.fillWithBlocks(var1, var3, 6, 1, 4, 9, 4, 6, var4, var4, false);
         this.setBlockState(var1, Blocks.FLOWING_LAVA.getDefaultState(), 7, 1, 5, var3);
         this.setBlockState(var1, Blocks.FLOWING_LAVA.getDefaultState(), 8, 1, 5, var3);
         this.setBlockState(var1, Blocks.IRON_BARS.getDefaultState(), 9, 2, 5, var3);
         this.setBlockState(var1, Blocks.IRON_BARS.getDefaultState(), 9, 2, 4, var3);
         this.fillWithBlocks(var1, var3, 7, 2, 4, 8, 2, 5, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.setBlockState(var1, var4, 6, 1, 3, var3);
         this.setBlockState(var1, Blocks.FURNACE.getDefaultState(), 6, 2, 3, var3);
         this.setBlockState(var1, Blocks.FURNACE.getDefaultState(), 6, 3, 3, var3);
         this.setBlockState(var1, Blocks.DOUBLE_STONE_SLAB.getDefaultState(), 8, 1, 1, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 0, 2, 2, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 0, 2, 4, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 2, 2, 6, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 4, 2, 6, var3);
         this.setBlockState(var1, var10, 2, 1, 4, var3);
         this.setBlockState(var1, Blocks.WOODEN_PRESSURE_PLATE.getDefaultState(), 2, 2, 4, var3);
         this.setBlockState(var1, var7, 1, 1, 5, var3);
         this.setBlockState(var1, var5, 2, 1, 5, var3);
         this.setBlockState(var1, var6, 1, 1, 4, var3);
         if (!this.hasMadeChest && var3.isVecInside(new BlockPos(this.getXWithOffset(5, 5), this.getYWithOffset(1), this.getZWithOffset(5, 5)))) {
            this.hasMadeChest = true;
            this.generateChest(var1, var3, var2, 5, 1, 5, LootTableList.CHESTS_VILLAGE_BLACKSMITH);
         }

         for(int var11 = 6; var11 <= 8; ++var11) {
            if (this.getBlockStateFromPos(var1, var11, 0, -1, var3).getMaterial() == Material.AIR && this.getBlockStateFromPos(var1, var11, -1, -1, var3).getMaterial() != Material.AIR) {
               this.setBlockState(var1, var8, var11, 0, -1, var3);
               if (this.getBlockStateFromPos(var1, var11, -1, -1, var3).getBlock() == Blocks.GRASS_PATH) {
                  this.setBlockState(var1, Blocks.GRASS.getDefaultState(), var11, -1, -1, var3);
               }
            }
         }

         for(int var13 = 0; var13 < 7; ++var13) {
            for(int var12 = 0; var12 < 10; ++var12) {
               this.clearCurrentPositionBlocksUpwards(var1, var12, 6, var13, var3);
               this.replaceAirAndLiquidDownwards(var1, var4, var12, -1, var13, var3);
            }
         }

         this.spawnVillagers(var1, var3, 7, 1, 1, 1);
         return true;
      }

      protected int chooseProfession(int var1, int var2) {
         return 3;
      }
   }

   public static class House3 extends StructureVillagePieces.Village {
      public House3() {
      }

      public House3(StructureVillagePieces.Start var1, int var2, Random var3, StructureBoundingBox var4, EnumFacing var5) {
         super(var1, var2);
         this.setCoordBaseMode(var5);
         this.boundingBox = var4;
      }

      public static StructureVillagePieces.House3 createPiece(StructureVillagePieces.Start var0, List var1, Random var2, int var3, int var4, int var5, EnumFacing var6, int var7) {
         StructureBoundingBox var8 = StructureBoundingBox.getComponentToAddBoundingBox(var3, var4, var5, 0, 0, 0, 9, 7, 12, var6);
         return canVillageGoDeeper(var8) && StructureComponent.findIntersecting(var1, var8) == null ? new StructureVillagePieces.House3(var0, var7, var2, var8, var6) : null;
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(var1, var3);
            if (this.averageGroundLvl < 0) {
               return true;
            }

            this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 7 - 1, 0);
         }

         IBlockState var4 = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE.getDefaultState());
         IBlockState var5 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH));
         IBlockState var6 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH));
         IBlockState var7 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST));
         IBlockState var8 = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.WEST));
         IBlockState var9 = this.getBiomeSpecificBlockState(Blocks.PLANKS.getDefaultState());
         IBlockState var10 = this.getBiomeSpecificBlockState(Blocks.LOG.getDefaultState());
         this.fillWithBlocks(var1, var3, 1, 1, 1, 7, 4, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 2, 1, 6, 8, 4, 10, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 2, 0, 5, 8, 0, 10, var9, var9, false);
         this.fillWithBlocks(var1, var3, 1, 0, 1, 7, 0, 4, var9, var9, false);
         this.fillWithBlocks(var1, var3, 0, 0, 0, 0, 3, 5, var4, var4, false);
         this.fillWithBlocks(var1, var3, 8, 0, 0, 8, 3, 10, var4, var4, false);
         this.fillWithBlocks(var1, var3, 1, 0, 0, 7, 2, 0, var4, var4, false);
         this.fillWithBlocks(var1, var3, 1, 0, 5, 2, 1, 5, var4, var4, false);
         this.fillWithBlocks(var1, var3, 2, 0, 6, 2, 3, 10, var4, var4, false);
         this.fillWithBlocks(var1, var3, 3, 0, 10, 7, 3, 10, var4, var4, false);
         this.fillWithBlocks(var1, var3, 1, 2, 0, 7, 3, 0, var9, var9, false);
         this.fillWithBlocks(var1, var3, 1, 2, 5, 2, 3, 5, var9, var9, false);
         this.fillWithBlocks(var1, var3, 0, 4, 1, 8, 4, 1, var9, var9, false);
         this.fillWithBlocks(var1, var3, 0, 4, 4, 3, 4, 4, var9, var9, false);
         this.fillWithBlocks(var1, var3, 0, 5, 2, 8, 5, 3, var9, var9, false);
         this.setBlockState(var1, var9, 0, 4, 2, var3);
         this.setBlockState(var1, var9, 0, 4, 3, var3);
         this.setBlockState(var1, var9, 8, 4, 2, var3);
         this.setBlockState(var1, var9, 8, 4, 3, var3);
         this.setBlockState(var1, var9, 8, 4, 4, var3);
         IBlockState var11 = var5;
         IBlockState var12 = var6;
         IBlockState var13 = var8;
         IBlockState var14 = var7;

         for(int var15 = -1; var15 <= 2; ++var15) {
            for(int var16 = 0; var16 <= 8; ++var16) {
               this.setBlockState(var1, var11, var16, 4 + var15, var15, var3);
               if ((var15 > -1 || var16 <= 1) && (var15 > 0 || var16 <= 3) && (var15 > 1 || var16 <= 4 || var16 >= 6)) {
                  this.setBlockState(var1, var12, var16, 4 + var15, 5 - var15, var3);
               }
            }
         }

         this.fillWithBlocks(var1, var3, 3, 4, 5, 3, 4, 10, var9, var9, false);
         this.fillWithBlocks(var1, var3, 7, 4, 2, 7, 4, 10, var9, var9, false);
         this.fillWithBlocks(var1, var3, 4, 5, 4, 4, 5, 10, var9, var9, false);
         this.fillWithBlocks(var1, var3, 6, 5, 4, 6, 5, 10, var9, var9, false);
         this.fillWithBlocks(var1, var3, 5, 6, 3, 5, 6, 10, var9, var9, false);

         for(int var17 = 4; var17 >= 1; --var17) {
            this.setBlockState(var1, var9, var17, 2 + var17, 7 - var17, var3);

            for(int var21 = 8 - var17; var21 <= 10; ++var21) {
               this.setBlockState(var1, var14, var17, 2 + var17, var21, var3);
            }
         }

         this.setBlockState(var1, var9, 6, 6, 3, var3);
         this.setBlockState(var1, var9, 7, 5, 4, var3);
         this.setBlockState(var1, var8, 6, 6, 4, var3);

         for(int var18 = 6; var18 <= 8; ++var18) {
            for(int var22 = 5; var22 <= 10; ++var22) {
               this.setBlockState(var1, var13, var18, 12 - var18, var22, var3);
            }
         }

         this.setBlockState(var1, var10, 0, 2, 1, var3);
         this.setBlockState(var1, var10, 0, 2, 4, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 0, 2, 2, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 0, 2, 3, var3);
         this.setBlockState(var1, var10, 4, 2, 0, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 5, 2, 0, var3);
         this.setBlockState(var1, var10, 6, 2, 0, var3);
         this.setBlockState(var1, var10, 8, 2, 1, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 8, 2, 2, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 8, 2, 3, var3);
         this.setBlockState(var1, var10, 8, 2, 4, var3);
         this.setBlockState(var1, var9, 8, 2, 5, var3);
         this.setBlockState(var1, var10, 8, 2, 6, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 8, 2, 7, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 8, 2, 8, var3);
         this.setBlockState(var1, var10, 8, 2, 9, var3);
         this.setBlockState(var1, var10, 2, 2, 6, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 2, 2, 7, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 2, 2, 8, var3);
         this.setBlockState(var1, var10, 2, 2, 9, var3);
         this.setBlockState(var1, var10, 4, 4, 10, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 5, 4, 10, var3);
         this.setBlockState(var1, var10, 6, 4, 10, var3);
         this.setBlockState(var1, var9, 5, 5, 10, var3);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 2, 1, 0, var3);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 2, 2, 0, var3);
         this.func_189926_a(var1, EnumFacing.NORTH, 2, 3, 1, var3);
         this.func_189927_a(var1, var3, var2, 2, 1, 0, EnumFacing.NORTH);
         this.fillWithBlocks(var1, var3, 1, 0, -1, 3, 2, -1, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         if (this.getBlockStateFromPos(var1, 2, 0, -1, var3).getMaterial() == Material.AIR && this.getBlockStateFromPos(var1, 2, -1, -1, var3).getMaterial() != Material.AIR) {
            this.setBlockState(var1, var11, 2, 0, -1, var3);
            if (this.getBlockStateFromPos(var1, 2, -1, -1, var3).getBlock() == Blocks.GRASS_PATH) {
               this.setBlockState(var1, Blocks.GRASS.getDefaultState(), 2, -1, -1, var3);
            }
         }

         for(int var19 = 0; var19 < 5; ++var19) {
            for(int var23 = 0; var23 < 9; ++var23) {
               this.clearCurrentPositionBlocksUpwards(var1, var23, 7, var19, var3);
               this.replaceAirAndLiquidDownwards(var1, var4, var23, -1, var19, var3);
            }
         }

         for(int var20 = 5; var20 < 11; ++var20) {
            for(int var24 = 2; var24 < 9; ++var24) {
               this.clearCurrentPositionBlocksUpwards(var1, var24, 7, var20, var3);
               this.replaceAirAndLiquidDownwards(var1, var4, var24, -1, var20, var3);
            }
         }

         this.spawnVillagers(var1, var3, 4, 1, 2, 2);
         return true;
      }
   }

   public static class House4Garden extends StructureVillagePieces.Village {
      private boolean isRoofAccessible;

      public House4Garden() {
      }

      public House4Garden(StructureVillagePieces.Start var1, int var2, Random var3, StructureBoundingBox var4, EnumFacing var5) {
         super(var1, var2);
         this.setCoordBaseMode(var5);
         this.boundingBox = var4;
         this.isRoofAccessible = var3.nextBoolean();
      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         super.writeStructureToNBT(var1);
         var1.setBoolean("Terrace", this.isRoofAccessible);
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         super.readStructureFromNBT(var1);
         this.isRoofAccessible = var1.getBoolean("Terrace");
      }

      public static StructureVillagePieces.House4Garden createPiece(StructureVillagePieces.Start var0, List var1, Random var2, int var3, int var4, int var5, EnumFacing var6, int var7) {
         StructureBoundingBox var8 = StructureBoundingBox.getComponentToAddBoundingBox(var3, var4, var5, 0, 0, 0, 5, 6, 5, var6);
         return StructureComponent.findIntersecting(var1, var8) != null ? null : new StructureVillagePieces.House4Garden(var0, var7, var2, var8, var6);
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(var1, var3);
            if (this.averageGroundLvl < 0) {
               return true;
            }

            this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 6 - 1, 0);
         }

         IBlockState var4 = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE.getDefaultState());
         IBlockState var5 = this.getBiomeSpecificBlockState(Blocks.PLANKS.getDefaultState());
         IBlockState var6 = this.getBiomeSpecificBlockState(Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH));
         IBlockState var7 = this.getBiomeSpecificBlockState(Blocks.LOG.getDefaultState());
         IBlockState var8 = this.getBiomeSpecificBlockState(Blocks.OAK_FENCE.getDefaultState());
         this.fillWithBlocks(var1, var3, 0, 0, 0, 4, 0, 4, var4, var4, false);
         this.fillWithBlocks(var1, var3, 0, 4, 0, 4, 4, 4, var7, var7, false);
         this.fillWithBlocks(var1, var3, 1, 4, 1, 3, 4, 3, var5, var5, false);
         this.setBlockState(var1, var4, 0, 1, 0, var3);
         this.setBlockState(var1, var4, 0, 2, 0, var3);
         this.setBlockState(var1, var4, 0, 3, 0, var3);
         this.setBlockState(var1, var4, 4, 1, 0, var3);
         this.setBlockState(var1, var4, 4, 2, 0, var3);
         this.setBlockState(var1, var4, 4, 3, 0, var3);
         this.setBlockState(var1, var4, 0, 1, 4, var3);
         this.setBlockState(var1, var4, 0, 2, 4, var3);
         this.setBlockState(var1, var4, 0, 3, 4, var3);
         this.setBlockState(var1, var4, 4, 1, 4, var3);
         this.setBlockState(var1, var4, 4, 2, 4, var3);
         this.setBlockState(var1, var4, 4, 3, 4, var3);
         this.fillWithBlocks(var1, var3, 0, 1, 1, 0, 3, 3, var5, var5, false);
         this.fillWithBlocks(var1, var3, 4, 1, 1, 4, 3, 3, var5, var5, false);
         this.fillWithBlocks(var1, var3, 1, 1, 4, 3, 3, 4, var5, var5, false);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 0, 2, 2, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 2, 2, 4, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 4, 2, 2, var3);
         this.setBlockState(var1, var5, 1, 1, 0, var3);
         this.setBlockState(var1, var5, 1, 2, 0, var3);
         this.setBlockState(var1, var5, 1, 3, 0, var3);
         this.setBlockState(var1, var5, 2, 3, 0, var3);
         this.setBlockState(var1, var5, 3, 3, 0, var3);
         this.setBlockState(var1, var5, 3, 2, 0, var3);
         this.setBlockState(var1, var5, 3, 1, 0, var3);
         if (this.getBlockStateFromPos(var1, 2, 0, -1, var3).getMaterial() == Material.AIR && this.getBlockStateFromPos(var1, 2, -1, -1, var3).getMaterial() != Material.AIR) {
            this.setBlockState(var1, var6, 2, 0, -1, var3);
            if (this.getBlockStateFromPos(var1, 2, -1, -1, var3).getBlock() == Blocks.GRASS_PATH) {
               this.setBlockState(var1, Blocks.GRASS.getDefaultState(), 2, -1, -1, var3);
            }
         }

         this.fillWithBlocks(var1, var3, 1, 1, 1, 3, 3, 3, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         if (this.isRoofAccessible) {
            this.setBlockState(var1, var8, 0, 5, 0, var3);
            this.setBlockState(var1, var8, 1, 5, 0, var3);
            this.setBlockState(var1, var8, 2, 5, 0, var3);
            this.setBlockState(var1, var8, 3, 5, 0, var3);
            this.setBlockState(var1, var8, 4, 5, 0, var3);
            this.setBlockState(var1, var8, 0, 5, 4, var3);
            this.setBlockState(var1, var8, 1, 5, 4, var3);
            this.setBlockState(var1, var8, 2, 5, 4, var3);
            this.setBlockState(var1, var8, 3, 5, 4, var3);
            this.setBlockState(var1, var8, 4, 5, 4, var3);
            this.setBlockState(var1, var8, 4, 5, 1, var3);
            this.setBlockState(var1, var8, 4, 5, 2, var3);
            this.setBlockState(var1, var8, 4, 5, 3, var3);
            this.setBlockState(var1, var8, 0, 5, 1, var3);
            this.setBlockState(var1, var8, 0, 5, 2, var3);
            this.setBlockState(var1, var8, 0, 5, 3, var3);
         }

         if (this.isRoofAccessible) {
            IBlockState var9 = Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.SOUTH);
            this.setBlockState(var1, var9, 3, 1, 3, var3);
            this.setBlockState(var1, var9, 3, 2, 3, var3);
            this.setBlockState(var1, var9, 3, 3, 3, var3);
            this.setBlockState(var1, var9, 3, 4, 3, var3);
         }

         this.func_189926_a(var1, EnumFacing.NORTH, 2, 3, 1, var3);

         for(int var11 = 0; var11 < 5; ++var11) {
            for(int var10 = 0; var10 < 5; ++var10) {
               this.clearCurrentPositionBlocksUpwards(var1, var10, 6, var11, var3);
               this.replaceAirAndLiquidDownwards(var1, var4, var10, -1, var11, var3);
            }
         }

         this.spawnVillagers(var1, var3, 1, 1, 2, 1);
         return true;
      }
   }

   public static class Path extends StructureVillagePieces.Road {
      private int length;

      public Path() {
      }

      public Path(StructureVillagePieces.Start var1, int var2, Random var3, StructureBoundingBox var4, EnumFacing var5) {
         super(var1, var2);
         this.setCoordBaseMode(var5);
         this.boundingBox = var4;
         this.length = Math.max(var4.getXSize(), var4.getZSize());
      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         super.writeStructureToNBT(var1);
         var1.setInteger("Length", this.length);
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         super.readStructureFromNBT(var1);
         this.length = var1.getInteger("Length");
      }

      public void buildComponent(StructureComponent var1, List var2, Random var3) {
         boolean var4 = false;

         for(int var5 = var3.nextInt(5); var5 < this.length - 8; var5 += 2 + var3.nextInt(5)) {
            StructureComponent var6 = this.getNextComponentNN((StructureVillagePieces.Start)var1, var2, var3, 0, var5);
            if (var6 != null) {
               var5 += Math.max(var6.boundingBox.getXSize(), var6.boundingBox.getZSize());
               var4 = true;
            }
         }

         for(int var7 = var3.nextInt(5); var7 < this.length - 8; var7 += 2 + var3.nextInt(5)) {
            StructureComponent var9 = this.getNextComponentPP((StructureVillagePieces.Start)var1, var2, var3, 0, var7);
            if (var9 != null) {
               var7 += Math.max(var9.boundingBox.getXSize(), var9.boundingBox.getZSize());
               var4 = true;
            }
         }

         EnumFacing var8 = this.getCoordBaseMode();
         if (var4 && var3.nextInt(3) > 0 && var8 != null) {
            switch(var8) {
            case NORTH:
            default:
               StructureVillagePieces.generateAndAddRoadPiece((StructureVillagePieces.Start)var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ, EnumFacing.WEST, this.getComponentType());
               break;
            case SOUTH:
               StructureVillagePieces.generateAndAddRoadPiece((StructureVillagePieces.Start)var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.maxZ - 2, EnumFacing.WEST, this.getComponentType());
               break;
            case WEST:
               StructureVillagePieces.generateAndAddRoadPiece((StructureVillagePieces.Start)var1, var2, var3, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ - 1, EnumFacing.NORTH, this.getComponentType());
               break;
            case EAST:
               StructureVillagePieces.generateAndAddRoadPiece((StructureVillagePieces.Start)var1, var2, var3, this.boundingBox.maxX - 2, this.boundingBox.minY, this.boundingBox.minZ - 1, EnumFacing.NORTH, this.getComponentType());
            }
         }

         if (var4 && var3.nextInt(3) > 0 && var8 != null) {
            switch(var8) {
            case NORTH:
            default:
               StructureVillagePieces.generateAndAddRoadPiece((StructureVillagePieces.Start)var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ, EnumFacing.EAST, this.getComponentType());
               break;
            case SOUTH:
               StructureVillagePieces.generateAndAddRoadPiece((StructureVillagePieces.Start)var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.maxZ - 2, EnumFacing.EAST, this.getComponentType());
               break;
            case WEST:
               StructureVillagePieces.generateAndAddRoadPiece((StructureVillagePieces.Start)var1, var2, var3, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, this.getComponentType());
               break;
            case EAST:
               StructureVillagePieces.generateAndAddRoadPiece((StructureVillagePieces.Start)var1, var2, var3, this.boundingBox.maxX - 2, this.boundingBox.minY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, this.getComponentType());
            }
         }

      }

      public static StructureBoundingBox findPieceBox(StructureVillagePieces.Start var0, List var1, Random var2, int var3, int var4, int var5, EnumFacing var6) {
         for(int var7 = 7 * MathHelper.getInt(var2, 3, 5); var7 >= 7; var7 -= 7) {
            StructureBoundingBox var8 = StructureBoundingBox.getComponentToAddBoundingBox(var3, var4, var5, 0, 0, 0, 3, 3, var7, var6);
            if (StructureComponent.findIntersecting(var1, var8) == null) {
               return var8;
            }
         }

         return null;
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         IBlockState var4 = this.getBiomeSpecificBlockState(Blocks.GRASS_PATH.getDefaultState());
         IBlockState var5 = this.getBiomeSpecificBlockState(Blocks.PLANKS.getDefaultState());
         IBlockState var6 = this.getBiomeSpecificBlockState(Blocks.GRAVEL.getDefaultState());
         IBlockState var7 = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE.getDefaultState());

         for(int var8 = this.boundingBox.minX; var8 <= this.boundingBox.maxX; ++var8) {
            for(int var9 = this.boundingBox.minZ; var9 <= this.boundingBox.maxZ; ++var9) {
               BlockPos var10 = new BlockPos(var8, 64, var9);
               if (var3.isVecInside(var10)) {
                  var10 = var1.getTopSolidOrLiquidBlock(var10).down();
                  if (var10.getY() < var1.getSeaLevel()) {
                     var10 = new BlockPos(var10.getX(), var1.getSeaLevel() - 1, var10.getZ());
                  }

                  while(var10.getY() >= var1.getSeaLevel() - 1) {
                     IBlockState var11 = var1.getBlockState(var10);
                     if (var11.getBlock() == Blocks.GRASS && var1.isAirBlock(var10.up())) {
                        var1.setBlockState(var10, var4, 2);
                        break;
                     }

                     if (var11.getMaterial().isLiquid()) {
                        var1.setBlockState(var10, var5, 2);
                        break;
                     }

                     if (var11.getBlock() == Blocks.SAND || var11.getBlock() == Blocks.SANDSTONE || var11.getBlock() == Blocks.RED_SANDSTONE) {
                        var1.setBlockState(var10, var6, 2);
                        var1.setBlockState(var10.down(), var7, 2);
                        break;
                     }

                     var10 = var10.down();
                  }
               }
            }
         }

         return true;
      }
   }

   public static class PieceWeight {
      public Class villagePieceClass;
      public final int villagePieceWeight;
      public int villagePiecesSpawned;
      public int villagePiecesLimit;

      public PieceWeight(Class var1, int var2, int var3) {
         this.villagePieceClass = var1;
         this.villagePieceWeight = var2;
         this.villagePiecesLimit = var3;
      }

      public boolean canSpawnMoreVillagePiecesOfType(int var1) {
         return this.villagePiecesLimit == 0 || this.villagePiecesSpawned < this.villagePiecesLimit;
      }

      public boolean canSpawnMoreVillagePieces() {
         return this.villagePiecesLimit == 0 || this.villagePiecesSpawned < this.villagePiecesLimit;
      }
   }

   public abstract static class Road extends StructureVillagePieces.Village {
      public Road() {
      }

      protected Road(StructureVillagePieces.Start var1, int var2) {
         super(var1, var2);
      }
   }

   public static class Start extends StructureVillagePieces.Well {
      public BiomeProvider worldChunkMngr;
      public int terrainType;
      public StructureVillagePieces.PieceWeight structVillagePieceWeight;
      public List structureVillageWeightedPieceList;
      public List pendingHouses = Lists.newArrayList();
      public List pendingRoads = Lists.newArrayList();
      public Biome biome;

      public Start() {
      }

      public Start(BiomeProvider var1, int var2, Random var3, int var4, int var5, List var6, int var7) {
         super((StructureVillagePieces.Start)null, 0, var3, var4, var5);
         this.worldChunkMngr = var1;
         this.structureVillageWeightedPieceList = var6;
         this.terrainType = var7;
         Biome var8 = var1.getBiome(new BlockPos(var4, 0, var5), Biomes.DEFAULT);
         this.biome = var8;
         this.startPiece = this;
         if (var8 instanceof BiomeDesert) {
            this.structureType = 1;
         } else if (var8 instanceof BiomeSavanna) {
            this.structureType = 2;
         } else if (var8 instanceof BiomeTaiga) {
            this.structureType = 3;
         }

         this.func_189924_a(this.structureType);
         this.isZombieInfested = var3.nextInt(50) == 0;
      }
   }

   public static class Torch extends StructureVillagePieces.Village {
      public Torch() {
      }

      public Torch(StructureVillagePieces.Start var1, int var2, Random var3, StructureBoundingBox var4, EnumFacing var5) {
         super(var1, var2);
         this.setCoordBaseMode(var5);
         this.boundingBox = var4;
      }

      public static StructureBoundingBox findPieceBox(StructureVillagePieces.Start var0, List var1, Random var2, int var3, int var4, int var5, EnumFacing var6) {
         StructureBoundingBox var7 = StructureBoundingBox.getComponentToAddBoundingBox(var3, var4, var5, 0, 0, 0, 3, 4, 2, var6);
         return StructureComponent.findIntersecting(var1, var7) != null ? null : var7;
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(var1, var3);
            if (this.averageGroundLvl < 0) {
               return true;
            }

            this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 4 - 1, 0);
         }

         IBlockState var4 = this.getBiomeSpecificBlockState(Blocks.OAK_FENCE.getDefaultState());
         this.fillWithBlocks(var1, var3, 0, 0, 0, 2, 3, 1, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.setBlockState(var1, var4, 1, 0, 0, var3);
         this.setBlockState(var1, var4, 1, 1, 0, var3);
         this.setBlockState(var1, var4, 1, 2, 0, var3);
         this.setBlockState(var1, Blocks.WOOL.getStateFromMeta(EnumDyeColor.WHITE.getDyeDamage()), 1, 3, 0, var3);
         this.func_189926_a(var1, EnumFacing.EAST, 2, 3, 0, var3);
         this.func_189926_a(var1, EnumFacing.NORTH, 1, 3, 1, var3);
         this.func_189926_a(var1, EnumFacing.WEST, 0, 3, 0, var3);
         this.func_189926_a(var1, EnumFacing.SOUTH, 1, 3, -1, var3);
         return true;
      }
   }

   public abstract static class Village extends StructureComponent {
      protected int averageGroundLvl = -1;
      private int villagersSpawned;
      protected int structureType;
      protected boolean isZombieInfested;
      protected StructureVillagePieces.Start startPiece;

      public Village() {
      }

      protected Village(StructureVillagePieces.Start var1, int var2) {
         super(var2);
         if (var1 != null) {
            this.structureType = var1.structureType;
            this.isZombieInfested = var1.isZombieInfested;
            this.startPiece = var1;
         }

      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         var1.setInteger("HPos", this.averageGroundLvl);
         var1.setInteger("VCount", this.villagersSpawned);
         var1.setByte("Type", (byte)this.structureType);
         var1.setBoolean("Zombie", this.isZombieInfested);
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         this.averageGroundLvl = var1.getInteger("HPos");
         this.villagersSpawned = var1.getInteger("VCount");
         this.structureType = var1.getByte("Type");
         if (var1.getBoolean("Desert")) {
            this.structureType = 1;
         }

         this.isZombieInfested = var1.getBoolean("Zombie");
      }

      protected StructureComponent getNextComponentNN(StructureVillagePieces.Start var1, List var2, Random var3, int var4, int var5) {
         EnumFacing var6 = this.getCoordBaseMode();
         if (var6 != null) {
            switch(var6) {
            case NORTH:
            default:
               return StructureVillagePieces.generateAndAddComponent(var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.minY + var4, this.boundingBox.minZ + var5, EnumFacing.WEST, this.getComponentType());
            case SOUTH:
               return StructureVillagePieces.generateAndAddComponent(var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.minY + var4, this.boundingBox.minZ + var5, EnumFacing.WEST, this.getComponentType());
            case WEST:
               return StructureVillagePieces.generateAndAddComponent(var1, var2, var3, this.boundingBox.minX + var5, this.boundingBox.minY + var4, this.boundingBox.minZ - 1, EnumFacing.NORTH, this.getComponentType());
            case EAST:
               return StructureVillagePieces.generateAndAddComponent(var1, var2, var3, this.boundingBox.minX + var5, this.boundingBox.minY + var4, this.boundingBox.minZ - 1, EnumFacing.NORTH, this.getComponentType());
            }
         } else {
            return null;
         }
      }

      protected StructureComponent getNextComponentPP(StructureVillagePieces.Start var1, List var2, Random var3, int var4, int var5) {
         EnumFacing var6 = this.getCoordBaseMode();
         if (var6 != null) {
            switch(var6) {
            case NORTH:
            default:
               return StructureVillagePieces.generateAndAddComponent(var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.minY + var4, this.boundingBox.minZ + var5, EnumFacing.EAST, this.getComponentType());
            case SOUTH:
               return StructureVillagePieces.generateAndAddComponent(var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.minY + var4, this.boundingBox.minZ + var5, EnumFacing.EAST, this.getComponentType());
            case WEST:
               return StructureVillagePieces.generateAndAddComponent(var1, var2, var3, this.boundingBox.minX + var5, this.boundingBox.minY + var4, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, this.getComponentType());
            case EAST:
               return StructureVillagePieces.generateAndAddComponent(var1, var2, var3, this.boundingBox.minX + var5, this.boundingBox.minY + var4, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, this.getComponentType());
            }
         } else {
            return null;
         }
      }

      protected int getAverageGroundLevel(World var1, StructureBoundingBox var2) {
         int var3 = 0;
         int var4 = 0;
         BlockPos.MutableBlockPos var5 = new BlockPos.MutableBlockPos();

         for(int var6 = this.boundingBox.minZ; var6 <= this.boundingBox.maxZ; ++var6) {
            for(int var7 = this.boundingBox.minX; var7 <= this.boundingBox.maxX; ++var7) {
               var5.setPos(var7, 64, var6);
               if (var2.isVecInside(var5)) {
                  var3 += Math.max(var1.getTopSolidOrLiquidBlock(var5).getY(), var1.provider.getAverageGroundLevel() - 1);
                  ++var4;
               }
            }
         }

         if (var4 == 0) {
            return -1;
         } else {
            return var3 / var4;
         }
      }

      protected static boolean canVillageGoDeeper(StructureBoundingBox var0) {
         return var0 != null && var0.minY > 10;
      }

      protected void spawnVillagers(World var1, StructureBoundingBox var2, int var3, int var4, int var5, int var6) {
         if (this.villagersSpawned < var6) {
            for(int var7 = this.villagersSpawned; var7 < var6; ++var7) {
               int var8 = this.getXWithOffset(var3 + var7, var5);
               int var9 = this.getYWithOffset(var4);
               int var10 = this.getZWithOffset(var3 + var7, var5);
               if (!var2.isVecInside(new BlockPos(var8, var9, var10))) {
                  break;
               }

               ++this.villagersSpawned;
               if (this.isZombieInfested) {
                  EntityZombie var11 = new EntityZombie(var1);
                  var11.setLocationAndAngles((double)var8 + 0.5D, (double)var9, (double)var10 + 0.5D, 0.0F, 0.0F);
                  var11.onInitialSpawn(var1.getDifficultyForLocation(new BlockPos(var11)), (IEntityLivingData)null);
                  var11.setVillagerType(this.chooseForgeProfession(var7, (VillagerProfession)ForgeRegistries.VILLAGER_PROFESSIONS.getValue(new ResourceLocation("minecraft:farmer"))));
                  var11.enablePersistence();
                  var1.spawnEntity(var11);
               } else {
                  EntityVillager var12 = new EntityVillager(var1);
                  var12.setLocationAndAngles((double)var8 + 0.5D, (double)var9, (double)var10 + 0.5D, 0.0F, 0.0F);
                  var12.onInitialSpawn(var1.getDifficultyForLocation(new BlockPos(var12)), (IEntityLivingData)null);
                  var12.setProfession(this.chooseForgeProfession(var7, var12.getProfessionForge()));
                  var1.spawnEntity(var12);
               }
            }
         }

      }

      /** @deprecated */
      @Deprecated
      protected int chooseProfession(int var1, int var2) {
         return var2;
      }

      protected VillagerProfession chooseForgeProfession(int var1, VillagerProfession var2) {
         return VillagerRegistry.getById(this.chooseProfession(var1, VillagerRegistry.getId(var2)));
      }

      protected IBlockState getBiomeSpecificBlockState(IBlockState var1) {
         GetVillageBlockID var2 = new GetVillageBlockID(this.startPiece == null ? null : this.startPiece.biome, var1);
         MinecraftForge.TERRAIN_GEN_BUS.post(var2);
         if (var2.getResult() == Result.DENY) {
            return var2.getReplacement();
         } else {
            if (this.structureType == 1) {
               if (var1.getBlock() == Blocks.LOG || var1.getBlock() == Blocks.LOG2) {
                  return Blocks.SANDSTONE.getDefaultState();
               }

               if (var1.getBlock() == Blocks.COBBLESTONE) {
                  return Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.DEFAULT.getMetadata());
               }

               if (var1.getBlock() == Blocks.PLANKS) {
                  return Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata());
               }

               if (var1.getBlock() == Blocks.OAK_STAIRS) {
                  return Blocks.SANDSTONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, var1.getValue(BlockStairs.FACING));
               }

               if (var1.getBlock() == Blocks.STONE_STAIRS) {
                  return Blocks.SANDSTONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, var1.getValue(BlockStairs.FACING));
               }

               if (var1.getBlock() == Blocks.GRAVEL) {
                  return Blocks.SANDSTONE.getDefaultState();
               }
            } else if (this.structureType == 3) {
               if (var1.getBlock() == Blocks.LOG || var1.getBlock() == Blocks.LOG2) {
                  return Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE).withProperty(BlockLog.LOG_AXIS, var1.getValue(BlockLog.LOG_AXIS));
               }

               if (var1.getBlock() == Blocks.PLANKS) {
                  return Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.SPRUCE);
               }

               if (var1.getBlock() == Blocks.OAK_STAIRS) {
                  return Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, var1.getValue(BlockStairs.FACING));
               }

               if (var1.getBlock() == Blocks.OAK_FENCE) {
                  return Blocks.SPRUCE_FENCE.getDefaultState();
               }
            } else if (this.structureType == 2) {
               if (var1.getBlock() == Blocks.LOG || var1.getBlock() == Blocks.LOG2) {
                  return Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.ACACIA).withProperty(BlockLog.LOG_AXIS, var1.getValue(BlockLog.LOG_AXIS));
               }

               if (var1.getBlock() == Blocks.PLANKS) {
                  return Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.ACACIA);
               }

               if (var1.getBlock() == Blocks.OAK_STAIRS) {
                  return Blocks.ACACIA_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, var1.getValue(BlockStairs.FACING));
               }

               if (var1.getBlock() == Blocks.COBBLESTONE) {
                  return Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.ACACIA).withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Y);
               }

               if (var1.getBlock() == Blocks.OAK_FENCE) {
                  return Blocks.ACACIA_FENCE.getDefaultState();
               }
            }

            return var1;
         }
      }

      protected BlockDoor func_189925_i() {
         switch(this.structureType) {
         case 2:
            return Blocks.ACACIA_DOOR;
         case 3:
            return Blocks.SPRUCE_DOOR;
         default:
            return Blocks.OAK_DOOR;
         }
      }

      protected void func_189927_a(World var1, StructureBoundingBox var2, Random var3, int var4, int var5, int var6, EnumFacing var7) {
         if (!this.isZombieInfested) {
            this.func_189915_a(var1, var2, var3, var4, var5, var6, EnumFacing.NORTH, this.func_189925_i());
         }

      }

      protected void func_189926_a(World var1, EnumFacing var2, int var3, int var4, int var5, StructureBoundingBox var6) {
         if (!this.isZombieInfested) {
            this.setBlockState(var1, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, var2), var3, var4, var5, var6);
         }

      }

      protected void replaceAirAndLiquidDownwards(World var1, IBlockState var2, int var3, int var4, int var5, StructureBoundingBox var6) {
         IBlockState var7 = this.getBiomeSpecificBlockState(var2);
         super.replaceAirAndLiquidDownwards(var1, var7, var3, var4, var5, var6);
      }

      protected void func_189924_a(int var1) {
         this.structureType = var1;
      }
   }

   public static class Well extends StructureVillagePieces.Village {
      public Well() {
      }

      public Well(StructureVillagePieces.Start var1, int var2, Random var3, int var4, int var5) {
         super(var1, var2);
         this.setCoordBaseMode(EnumFacing.Plane.HORIZONTAL.random(var3));
         if (this.getCoordBaseMode().getAxis() == EnumFacing.Axis.Z) {
            this.boundingBox = new StructureBoundingBox(var4, 64, var5, var4 + 6 - 1, 78, var5 + 6 - 1);
         } else {
            this.boundingBox = new StructureBoundingBox(var4, 64, var5, var4 + 6 - 1, 78, var5 + 6 - 1);
         }

      }

      public void buildComponent(StructureComponent var1, List var2, Random var3) {
         StructureVillagePieces.generateAndAddRoadPiece((StructureVillagePieces.Start)var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.maxY - 4, this.boundingBox.minZ + 1, EnumFacing.WEST, this.getComponentType());
         StructureVillagePieces.generateAndAddRoadPiece((StructureVillagePieces.Start)var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.maxY - 4, this.boundingBox.minZ + 1, EnumFacing.EAST, this.getComponentType());
         StructureVillagePieces.generateAndAddRoadPiece((StructureVillagePieces.Start)var1, var2, var3, this.boundingBox.minX + 1, this.boundingBox.maxY - 4, this.boundingBox.minZ - 1, EnumFacing.NORTH, this.getComponentType());
         StructureVillagePieces.generateAndAddRoadPiece((StructureVillagePieces.Start)var1, var2, var3, this.boundingBox.minX + 1, this.boundingBox.maxY - 4, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, this.getComponentType());
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(var1, var3);
            if (this.averageGroundLvl < 0) {
               return true;
            }

            this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 3, 0);
         }

         IBlockState var4 = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE.getDefaultState());
         IBlockState var5 = this.getBiomeSpecificBlockState(Blocks.OAK_FENCE.getDefaultState());
         this.fillWithBlocks(var1, var3, 1, 0, 1, 4, 12, 4, var4, Blocks.FLOWING_WATER.getDefaultState(), false);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 2, 12, 2, var3);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 3, 12, 2, var3);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 2, 12, 3, var3);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 3, 12, 3, var3);
         this.setBlockState(var1, var5, 1, 13, 1, var3);
         this.setBlockState(var1, var5, 1, 14, 1, var3);
         this.setBlockState(var1, var5, 4, 13, 1, var3);
         this.setBlockState(var1, var5, 4, 14, 1, var3);
         this.setBlockState(var1, var5, 1, 13, 4, var3);
         this.setBlockState(var1, var5, 1, 14, 4, var3);
         this.setBlockState(var1, var5, 4, 13, 4, var3);
         this.setBlockState(var1, var5, 4, 14, 4, var3);
         this.fillWithBlocks(var1, var3, 1, 15, 1, 4, 15, 4, var4, var4, false);

         for(int var6 = 0; var6 <= 5; ++var6) {
            for(int var7 = 0; var7 <= 5; ++var7) {
               if (var7 == 0 || var7 == 5 || var6 == 0 || var6 == 5) {
                  this.setBlockState(var1, Blocks.COBBLESTONE.getDefaultState(), var7, 11, var6, var3);
                  this.clearCurrentPositionBlocksUpwards(var1, var7, 12, var6, var3);
               }
            }
         }

         return true;
      }
   }

   public static class WoodHut extends StructureVillagePieces.Village {
      private boolean isTallHouse;
      private int tablePosition;

      public WoodHut() {
      }

      public WoodHut(StructureVillagePieces.Start var1, int var2, Random var3, StructureBoundingBox var4, EnumFacing var5) {
         super(var1, var2);
         this.setCoordBaseMode(var5);
         this.boundingBox = var4;
         this.isTallHouse = var3.nextBoolean();
         this.tablePosition = var3.nextInt(3);
      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         super.writeStructureToNBT(var1);
         var1.setInteger("T", this.tablePosition);
         var1.setBoolean("C", this.isTallHouse);
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         super.readStructureFromNBT(var1);
         this.tablePosition = var1.getInteger("T");
         this.isTallHouse = var1.getBoolean("C");
      }

      public static StructureVillagePieces.WoodHut createPiece(StructureVillagePieces.Start var0, List var1, Random var2, int var3, int var4, int var5, EnumFacing var6, int var7) {
         StructureBoundingBox var8 = StructureBoundingBox.getComponentToAddBoundingBox(var3, var4, var5, 0, 0, 0, 4, 6, 5, var6);
         return canVillageGoDeeper(var8) && StructureComponent.findIntersecting(var1, var8) == null ? new StructureVillagePieces.WoodHut(var0, var7, var2, var8, var6) : null;
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(var1, var3);
            if (this.averageGroundLvl < 0) {
               return true;
            }

            this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 6 - 1, 0);
         }

         IBlockState var4 = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE.getDefaultState());
         IBlockState var5 = this.getBiomeSpecificBlockState(Blocks.PLANKS.getDefaultState());
         IBlockState var6 = this.getBiomeSpecificBlockState(Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH));
         IBlockState var7 = this.getBiomeSpecificBlockState(Blocks.LOG.getDefaultState());
         IBlockState var8 = this.getBiomeSpecificBlockState(Blocks.OAK_FENCE.getDefaultState());
         this.fillWithBlocks(var1, var3, 1, 1, 1, 3, 5, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 0, 0, 0, 3, 0, 4, var4, var4, false);
         this.fillWithBlocks(var1, var3, 1, 0, 1, 2, 0, 3, Blocks.DIRT.getDefaultState(), Blocks.DIRT.getDefaultState(), false);
         if (this.isTallHouse) {
            this.fillWithBlocks(var1, var3, 1, 4, 1, 2, 4, 3, var7, var7, false);
         } else {
            this.fillWithBlocks(var1, var3, 1, 5, 1, 2, 5, 3, var7, var7, false);
         }

         this.setBlockState(var1, var7, 1, 4, 0, var3);
         this.setBlockState(var1, var7, 2, 4, 0, var3);
         this.setBlockState(var1, var7, 1, 4, 4, var3);
         this.setBlockState(var1, var7, 2, 4, 4, var3);
         this.setBlockState(var1, var7, 0, 4, 1, var3);
         this.setBlockState(var1, var7, 0, 4, 2, var3);
         this.setBlockState(var1, var7, 0, 4, 3, var3);
         this.setBlockState(var1, var7, 3, 4, 1, var3);
         this.setBlockState(var1, var7, 3, 4, 2, var3);
         this.setBlockState(var1, var7, 3, 4, 3, var3);
         this.fillWithBlocks(var1, var3, 0, 1, 0, 0, 3, 0, var7, var7, false);
         this.fillWithBlocks(var1, var3, 3, 1, 0, 3, 3, 0, var7, var7, false);
         this.fillWithBlocks(var1, var3, 0, 1, 4, 0, 3, 4, var7, var7, false);
         this.fillWithBlocks(var1, var3, 3, 1, 4, 3, 3, 4, var7, var7, false);
         this.fillWithBlocks(var1, var3, 0, 1, 1, 0, 3, 3, var5, var5, false);
         this.fillWithBlocks(var1, var3, 3, 1, 1, 3, 3, 3, var5, var5, false);
         this.fillWithBlocks(var1, var3, 1, 1, 0, 2, 3, 0, var5, var5, false);
         this.fillWithBlocks(var1, var3, 1, 1, 4, 2, 3, 4, var5, var5, false);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 0, 2, 2, var3);
         this.setBlockState(var1, Blocks.GLASS_PANE.getDefaultState(), 3, 2, 2, var3);
         if (this.tablePosition > 0) {
            this.setBlockState(var1, var8, this.tablePosition, 1, 3, var3);
            this.setBlockState(var1, Blocks.WOODEN_PRESSURE_PLATE.getDefaultState(), this.tablePosition, 2, 3, var3);
         }

         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 1, 1, 0, var3);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 1, 2, 0, var3);
         this.func_189927_a(var1, var3, var2, 1, 1, 0, EnumFacing.NORTH);
         if (this.getBlockStateFromPos(var1, 1, 0, -1, var3).getMaterial() == Material.AIR && this.getBlockStateFromPos(var1, 1, -1, -1, var3).getMaterial() != Material.AIR) {
            this.setBlockState(var1, var6, 1, 0, -1, var3);
            if (this.getBlockStateFromPos(var1, 1, -1, -1, var3).getBlock() == Blocks.GRASS_PATH) {
               this.setBlockState(var1, Blocks.GRASS.getDefaultState(), 1, -1, -1, var3);
            }
         }

         for(int var9 = 0; var9 < 5; ++var9) {
            for(int var10 = 0; var10 < 4; ++var10) {
               this.clearCurrentPositionBlocksUpwards(var1, var10, 6, var9, var3);
               this.replaceAirAndLiquidDownwards(var1, var4, var10, -1, var9, var3);
            }
         }

         this.spawnVillagers(var1, var3, 1, 1, 2, 1);
         return true;
      }
   }
}
