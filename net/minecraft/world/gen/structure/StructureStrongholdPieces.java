package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class StructureStrongholdPieces {
   private static final StructureStrongholdPieces.PieceWeight[] PIECE_WEIGHTS = new StructureStrongholdPieces.PieceWeight[]{new StructureStrongholdPieces.PieceWeight(StructureStrongholdPieces.Straight.class, 40, 0), new StructureStrongholdPieces.PieceWeight(StructureStrongholdPieces.Prison.class, 5, 5), new StructureStrongholdPieces.PieceWeight(StructureStrongholdPieces.LeftTurn.class, 20, 0), new StructureStrongholdPieces.PieceWeight(StructureStrongholdPieces.RightTurn.class, 20, 0), new StructureStrongholdPieces.PieceWeight(StructureStrongholdPieces.RoomCrossing.class, 10, 6), new StructureStrongholdPieces.PieceWeight(StructureStrongholdPieces.StairsStraight.class, 5, 5), new StructureStrongholdPieces.PieceWeight(StructureStrongholdPieces.Stairs.class, 5, 5), new StructureStrongholdPieces.PieceWeight(StructureStrongholdPieces.Crossing.class, 5, 4), new StructureStrongholdPieces.PieceWeight(StructureStrongholdPieces.ChestCorridor.class, 5, 4), new StructureStrongholdPieces.PieceWeight(StructureStrongholdPieces.Library.class, 10, 2) {
      public boolean canSpawnMoreStructuresOfType(int var1) {
         return super.canSpawnMoreStructuresOfType(var1) && var1 > 4;
      }
   }, new StructureStrongholdPieces.PieceWeight(StructureStrongholdPieces.PortalRoom.class, 20, 1) {
      public boolean canSpawnMoreStructuresOfType(int var1) {
         return super.canSpawnMoreStructuresOfType(var1) && var1 > 5;
      }
   }};
   private static List structurePieceList;
   private static Class strongComponentType;
   static int totalWeight;
   private static final StructureStrongholdPieces.Stones STRONGHOLD_STONES = new StructureStrongholdPieces.Stones();

   public static void registerStrongholdPieces() {
      MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.ChestCorridor.class, "SHCC");
      MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.Corridor.class, "SHFC");
      MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.Crossing.class, "SH5C");
      MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.LeftTurn.class, "SHLT");
      MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.Library.class, "SHLi");
      MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.PortalRoom.class, "SHPR");
      MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.Prison.class, "SHPH");
      MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.RightTurn.class, "SHRT");
      MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.RoomCrossing.class, "SHRC");
      MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.Stairs.class, "SHSD");
      MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.Stairs2.class, "SHStart");
      MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.Straight.class, "SHS");
      MapGenStructureIO.registerStructureComponent(StructureStrongholdPieces.StairsStraight.class, "SHSSD");
   }

   public static void prepareStructurePieces() {
      structurePieceList = Lists.newArrayList();

      for(StructureStrongholdPieces.PieceWeight var3 : PIECE_WEIGHTS) {
         var3.instancesSpawned = 0;
         structurePieceList.add(var3);
      }

      strongComponentType = null;
   }

   private static boolean canAddStructurePieces() {
      boolean var0 = false;
      totalWeight = 0;

      for(StructureStrongholdPieces.PieceWeight var2 : structurePieceList) {
         if (var2.instancesLimit > 0 && var2.instancesSpawned < var2.instancesLimit) {
            var0 = true;
         }

         totalWeight += var2.pieceWeight;
      }

      return var0;
   }

   private static StructureStrongholdPieces.Stronghold findAndCreatePieceFactory(Class var0, List var1, Random var2, int var3, int var4, int var5, @Nullable EnumFacing var6, int var7) {
      Object var8 = null;
      if (var0 == StructureStrongholdPieces.Straight.class) {
         var8 = StructureStrongholdPieces.Straight.createPiece(var1, var2, var3, var4, var5, var6, var7);
      } else if (var0 == StructureStrongholdPieces.Prison.class) {
         var8 = StructureStrongholdPieces.Prison.createPiece(var1, var2, var3, var4, var5, var6, var7);
      } else if (var0 == StructureStrongholdPieces.LeftTurn.class) {
         var8 = StructureStrongholdPieces.LeftTurn.createPiece(var1, var2, var3, var4, var5, var6, var7);
      } else if (var0 == StructureStrongholdPieces.RightTurn.class) {
         var8 = StructureStrongholdPieces.RightTurn.createPiece(var1, var2, var3, var4, var5, var6, var7);
      } else if (var0 == StructureStrongholdPieces.RoomCrossing.class) {
         var8 = StructureStrongholdPieces.RoomCrossing.createPiece(var1, var2, var3, var4, var5, var6, var7);
      } else if (var0 == StructureStrongholdPieces.StairsStraight.class) {
         var8 = StructureStrongholdPieces.StairsStraight.createPiece(var1, var2, var3, var4, var5, var6, var7);
      } else if (var0 == StructureStrongholdPieces.Stairs.class) {
         var8 = StructureStrongholdPieces.Stairs.createPiece(var1, var2, var3, var4, var5, var6, var7);
      } else if (var0 == StructureStrongholdPieces.Crossing.class) {
         var8 = StructureStrongholdPieces.Crossing.createPiece(var1, var2, var3, var4, var5, var6, var7);
      } else if (var0 == StructureStrongholdPieces.ChestCorridor.class) {
         var8 = StructureStrongholdPieces.ChestCorridor.createPiece(var1, var2, var3, var4, var5, var6, var7);
      } else if (var0 == StructureStrongholdPieces.Library.class) {
         var8 = StructureStrongholdPieces.Library.createPiece(var1, var2, var3, var4, var5, var6, var7);
      } else if (var0 == StructureStrongholdPieces.PortalRoom.class) {
         var8 = StructureStrongholdPieces.PortalRoom.createPiece(var1, var2, var3, var4, var5, var6, var7);
      }

      return (StructureStrongholdPieces.Stronghold)var8;
   }

   private static StructureStrongholdPieces.Stronghold generatePieceFromSmallDoor(StructureStrongholdPieces.Stairs2 var0, List var1, Random var2, int var3, int var4, int var5, EnumFacing var6, int var7) {
      if (!canAddStructurePieces()) {
         return null;
      } else {
         if (strongComponentType != null) {
            StructureStrongholdPieces.Stronghold var8 = findAndCreatePieceFactory(strongComponentType, var1, var2, var3, var4, var5, var6, var7);
            strongComponentType = null;
            if (var8 != null) {
               return var8;
            }
         }

         int var13 = 0;

         while(var13 < 5) {
            ++var13;
            int var9 = var2.nextInt(totalWeight);

            for(StructureStrongholdPieces.PieceWeight var11 : structurePieceList) {
               var9 -= var11.pieceWeight;
               if (var9 < 0) {
                  if (!var11.canSpawnMoreStructuresOfType(var7) || var11 == var0.strongholdPieceWeight) {
                     break;
                  }

                  StructureStrongholdPieces.Stronghold var12 = findAndCreatePieceFactory(var11.pieceClass, var1, var2, var3, var4, var5, var6, var7);
                  if (var12 != null) {
                     ++var11.instancesSpawned;
                     var0.strongholdPieceWeight = var11;
                     if (!var11.canSpawnMoreStructures()) {
                        structurePieceList.remove(var11);
                     }

                     return var12;
                  }
               }
            }
         }

         StructureBoundingBox var14 = StructureStrongholdPieces.Corridor.findPieceBox(var1, var2, var3, var4, var5, var6);
         if (var14 != null && var14.minY > 1) {
            return new StructureStrongholdPieces.Corridor(var7, var2, var14, var6);
         } else {
            return null;
         }
      }
   }

   private static StructureComponent generateAndAddPiece(StructureStrongholdPieces.Stairs2 var0, List var1, Random var2, int var3, int var4, int var5, @Nullable EnumFacing var6, int var7) {
      if (var7 > 50) {
         return null;
      } else if (Math.abs(var3 - var0.getBoundingBox().minX) <= 112 && Math.abs(var5 - var0.getBoundingBox().minZ) <= 112) {
         StructureStrongholdPieces.Stronghold var8 = generatePieceFromSmallDoor(var0, var1, var2, var3, var4, var5, var6, var7 + 1);
         if (var8 != null) {
            var1.add(var8);
            var0.pendingChildren.add(var8);
         }

         return var8;
      } else {
         return null;
      }
   }

   public static class ChestCorridor extends StructureStrongholdPieces.Stronghold {
      private boolean hasMadeChest;

      public ChestCorridor() {
      }

      public ChestCorridor(int var1, Random var2, StructureBoundingBox var3, EnumFacing var4) {
         super(var1);
         this.setCoordBaseMode(var4);
         this.entryDoor = this.getRandomDoor(var2);
         this.boundingBox = var3;
      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         super.writeStructureToNBT(var1);
         var1.setBoolean("Chest", this.hasMadeChest);
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         super.readStructureFromNBT(var1);
         this.hasMadeChest = var1.getBoolean("Chest");
      }

      public void buildComponent(StructureComponent var1, List var2, Random var3) {
         this.getNextComponentNormal((StructureStrongholdPieces.Stairs2)var1, var2, var3, 1, 1);
      }

      public static StructureStrongholdPieces.ChestCorridor createPiece(List var0, Random var1, int var2, int var3, int var4, EnumFacing var5, int var6) {
         StructureBoundingBox var7 = StructureBoundingBox.getComponentToAddBoundingBox(var2, var3, var4, -1, -1, 0, 5, 5, 7, var5);
         return canStrongholdGoDeeper(var7) && StructureComponent.findIntersecting(var0, var7) == null ? new StructureStrongholdPieces.ChestCorridor(var6, var1, var7, var5) : null;
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.isLiquidInStructureBoundingBox(var1, var3)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(var1, var3, 0, 0, 0, 4, 4, 6, true, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
            this.placeDoor(var1, var2, var3, this.entryDoor, 1, 1, 0);
            this.placeDoor(var1, var2, var3, StructureStrongholdPieces.Stronghold.Door.OPENING, 1, 1, 6);
            this.fillWithBlocks(var1, var3, 3, 1, 2, 3, 1, 4, Blocks.STONEBRICK.getDefaultState(), Blocks.STONEBRICK.getDefaultState(), false);
            this.setBlockState(var1, Blocks.STONE_SLAB.getStateFromMeta(BlockStoneSlab.EnumType.SMOOTHBRICK.getMetadata()), 3, 1, 1, var3);
            this.setBlockState(var1, Blocks.STONE_SLAB.getStateFromMeta(BlockStoneSlab.EnumType.SMOOTHBRICK.getMetadata()), 3, 1, 5, var3);
            this.setBlockState(var1, Blocks.STONE_SLAB.getStateFromMeta(BlockStoneSlab.EnumType.SMOOTHBRICK.getMetadata()), 3, 2, 2, var3);
            this.setBlockState(var1, Blocks.STONE_SLAB.getStateFromMeta(BlockStoneSlab.EnumType.SMOOTHBRICK.getMetadata()), 3, 2, 4, var3);

            for(int var4 = 2; var4 <= 4; ++var4) {
               this.setBlockState(var1, Blocks.STONE_SLAB.getStateFromMeta(BlockStoneSlab.EnumType.SMOOTHBRICK.getMetadata()), 2, 1, var4, var3);
            }

            if (!this.hasMadeChest && var3.isVecInside(new BlockPos(this.getXWithOffset(3, 3), this.getYWithOffset(2), this.getZWithOffset(3, 3)))) {
               this.hasMadeChest = true;
               this.generateChest(var1, var3, var2, 3, 2, 3, LootTableList.CHESTS_STRONGHOLD_CORRIDOR);
            }

            return true;
         }
      }
   }

   public static class Corridor extends StructureStrongholdPieces.Stronghold {
      private int steps;

      public Corridor() {
      }

      public Corridor(int var1, Random var2, StructureBoundingBox var3, EnumFacing var4) {
         super(var1);
         this.setCoordBaseMode(var4);
         this.boundingBox = var3;
         this.steps = var4 != EnumFacing.NORTH && var4 != EnumFacing.SOUTH ? var3.getXSize() : var3.getZSize();
      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         super.writeStructureToNBT(var1);
         var1.setInteger("Steps", this.steps);
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         super.readStructureFromNBT(var1);
         this.steps = var1.getInteger("Steps");
      }

      public static StructureBoundingBox findPieceBox(List var0, Random var1, int var2, int var3, int var4, EnumFacing var5) {
         boolean var6 = true;
         StructureBoundingBox var7 = StructureBoundingBox.getComponentToAddBoundingBox(var2, var3, var4, -1, -1, 0, 5, 5, 4, var5);
         StructureComponent var8 = StructureComponent.findIntersecting(var0, var7);
         if (var8 == null) {
            return null;
         } else {
            if (var8.getBoundingBox().minY == var7.minY) {
               for(int var9 = 3; var9 >= 1; --var9) {
                  var7 = StructureBoundingBox.getComponentToAddBoundingBox(var2, var3, var4, -1, -1, 0, 5, 5, var9 - 1, var5);
                  if (!var8.getBoundingBox().intersectsWith(var7)) {
                     return StructureBoundingBox.getComponentToAddBoundingBox(var2, var3, var4, -1, -1, 0, 5, 5, var9, var5);
                  }
               }
            }

            return null;
         }
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.isLiquidInStructureBoundingBox(var1, var3)) {
            return false;
         } else {
            for(int var4 = 0; var4 < this.steps; ++var4) {
               this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 0, 0, var4, var3);
               this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 1, 0, var4, var3);
               this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 2, 0, var4, var3);
               this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 3, 0, var4, var3);
               this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 4, 0, var4, var3);

               for(int var5 = 1; var5 <= 3; ++var5) {
                  this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 0, var5, var4, var3);
                  this.setBlockState(var1, Blocks.AIR.getDefaultState(), 1, var5, var4, var3);
                  this.setBlockState(var1, Blocks.AIR.getDefaultState(), 2, var5, var4, var3);
                  this.setBlockState(var1, Blocks.AIR.getDefaultState(), 3, var5, var4, var3);
                  this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 4, var5, var4, var3);
               }

               this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 0, 4, var4, var3);
               this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 1, 4, var4, var3);
               this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 2, 4, var4, var3);
               this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 3, 4, var4, var3);
               this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 4, 4, var4, var3);
            }

            return true;
         }
      }
   }

   public static class Crossing extends StructureStrongholdPieces.Stronghold {
      private boolean leftLow;
      private boolean leftHigh;
      private boolean rightLow;
      private boolean rightHigh;

      public Crossing() {
      }

      public Crossing(int var1, Random var2, StructureBoundingBox var3, EnumFacing var4) {
         super(var1);
         this.setCoordBaseMode(var4);
         this.entryDoor = this.getRandomDoor(var2);
         this.boundingBox = var3;
         this.leftLow = var2.nextBoolean();
         this.leftHigh = var2.nextBoolean();
         this.rightLow = var2.nextBoolean();
         this.rightHigh = var2.nextInt(3) > 0;
      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         super.writeStructureToNBT(var1);
         var1.setBoolean("leftLow", this.leftLow);
         var1.setBoolean("leftHigh", this.leftHigh);
         var1.setBoolean("rightLow", this.rightLow);
         var1.setBoolean("rightHigh", this.rightHigh);
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         super.readStructureFromNBT(var1);
         this.leftLow = var1.getBoolean("leftLow");
         this.leftHigh = var1.getBoolean("leftHigh");
         this.rightLow = var1.getBoolean("rightLow");
         this.rightHigh = var1.getBoolean("rightHigh");
      }

      public void buildComponent(StructureComponent var1, List var2, Random var3) {
         int var4 = 3;
         int var5 = 5;
         EnumFacing var6 = this.getCoordBaseMode();
         if (var6 == EnumFacing.WEST || var6 == EnumFacing.NORTH) {
            var4 = 8 - var4;
            var5 = 8 - var5;
         }

         this.getNextComponentNormal((StructureStrongholdPieces.Stairs2)var1, var2, var3, 5, 1);
         if (this.leftLow) {
            this.getNextComponentX((StructureStrongholdPieces.Stairs2)var1, var2, var3, var4, 1);
         }

         if (this.leftHigh) {
            this.getNextComponentX((StructureStrongholdPieces.Stairs2)var1, var2, var3, var5, 7);
         }

         if (this.rightLow) {
            this.getNextComponentZ((StructureStrongholdPieces.Stairs2)var1, var2, var3, var4, 1);
         }

         if (this.rightHigh) {
            this.getNextComponentZ((StructureStrongholdPieces.Stairs2)var1, var2, var3, var5, 7);
         }

      }

      public static StructureStrongholdPieces.Crossing createPiece(List var0, Random var1, int var2, int var3, int var4, EnumFacing var5, int var6) {
         StructureBoundingBox var7 = StructureBoundingBox.getComponentToAddBoundingBox(var2, var3, var4, -4, -3, 0, 10, 9, 11, var5);
         return canStrongholdGoDeeper(var7) && StructureComponent.findIntersecting(var0, var7) == null ? new StructureStrongholdPieces.Crossing(var6, var1, var7, var5) : null;
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.isLiquidInStructureBoundingBox(var1, var3)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(var1, var3, 0, 0, 0, 9, 8, 10, true, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
            this.placeDoor(var1, var2, var3, this.entryDoor, 4, 3, 0);
            if (this.leftLow) {
               this.fillWithBlocks(var1, var3, 0, 3, 1, 0, 5, 3, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            }

            if (this.rightLow) {
               this.fillWithBlocks(var1, var3, 9, 3, 1, 9, 5, 3, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            }

            if (this.leftHigh) {
               this.fillWithBlocks(var1, var3, 0, 5, 7, 0, 7, 9, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            }

            if (this.rightHigh) {
               this.fillWithBlocks(var1, var3, 9, 5, 7, 9, 7, 9, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            }

            this.fillWithBlocks(var1, var3, 5, 1, 10, 7, 3, 10, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            this.fillWithRandomizedBlocks(var1, var3, 1, 2, 1, 8, 2, 6, false, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
            this.fillWithRandomizedBlocks(var1, var3, 4, 1, 5, 4, 4, 9, false, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
            this.fillWithRandomizedBlocks(var1, var3, 8, 1, 5, 8, 4, 9, false, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
            this.fillWithRandomizedBlocks(var1, var3, 1, 4, 7, 3, 4, 9, false, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
            this.fillWithRandomizedBlocks(var1, var3, 1, 3, 5, 3, 3, 6, false, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
            this.fillWithBlocks(var1, var3, 1, 3, 4, 3, 3, 4, Blocks.STONE_SLAB.getDefaultState(), Blocks.STONE_SLAB.getDefaultState(), false);
            this.fillWithBlocks(var1, var3, 1, 4, 6, 3, 4, 6, Blocks.STONE_SLAB.getDefaultState(), Blocks.STONE_SLAB.getDefaultState(), false);
            this.fillWithRandomizedBlocks(var1, var3, 5, 1, 7, 7, 1, 8, false, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
            this.fillWithBlocks(var1, var3, 5, 1, 9, 7, 1, 9, Blocks.STONE_SLAB.getDefaultState(), Blocks.STONE_SLAB.getDefaultState(), false);
            this.fillWithBlocks(var1, var3, 5, 2, 7, 7, 2, 7, Blocks.STONE_SLAB.getDefaultState(), Blocks.STONE_SLAB.getDefaultState(), false);
            this.fillWithBlocks(var1, var3, 4, 5, 7, 4, 5, 9, Blocks.STONE_SLAB.getDefaultState(), Blocks.STONE_SLAB.getDefaultState(), false);
            this.fillWithBlocks(var1, var3, 8, 5, 7, 8, 5, 9, Blocks.STONE_SLAB.getDefaultState(), Blocks.STONE_SLAB.getDefaultState(), false);
            this.fillWithBlocks(var1, var3, 5, 5, 7, 7, 5, 9, Blocks.DOUBLE_STONE_SLAB.getDefaultState(), Blocks.DOUBLE_STONE_SLAB.getDefaultState(), false);
            this.setBlockState(var1, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.SOUTH), 6, 5, 6, var3);
            return true;
         }
      }
   }

   public static class LeftTurn extends StructureStrongholdPieces.Stronghold {
      public LeftTurn() {
      }

      public LeftTurn(int var1, Random var2, StructureBoundingBox var3, EnumFacing var4) {
         super(var1);
         this.setCoordBaseMode(var4);
         this.entryDoor = this.getRandomDoor(var2);
         this.boundingBox = var3;
      }

      public void buildComponent(StructureComponent var1, List var2, Random var3) {
         EnumFacing var4 = this.getCoordBaseMode();
         if (var4 != EnumFacing.NORTH && var4 != EnumFacing.EAST) {
            this.getNextComponentZ((StructureStrongholdPieces.Stairs2)var1, var2, var3, 1, 1);
         } else {
            this.getNextComponentX((StructureStrongholdPieces.Stairs2)var1, var2, var3, 1, 1);
         }

      }

      public static StructureStrongholdPieces.LeftTurn createPiece(List var0, Random var1, int var2, int var3, int var4, EnumFacing var5, int var6) {
         StructureBoundingBox var7 = StructureBoundingBox.getComponentToAddBoundingBox(var2, var3, var4, -1, -1, 0, 5, 5, 5, var5);
         return canStrongholdGoDeeper(var7) && StructureComponent.findIntersecting(var0, var7) == null ? new StructureStrongholdPieces.LeftTurn(var6, var1, var7, var5) : null;
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.isLiquidInStructureBoundingBox(var1, var3)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(var1, var3, 0, 0, 0, 4, 4, 4, true, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
            this.placeDoor(var1, var2, var3, this.entryDoor, 1, 1, 0);
            EnumFacing var4 = this.getCoordBaseMode();
            if (var4 != EnumFacing.NORTH && var4 != EnumFacing.EAST) {
               this.fillWithBlocks(var1, var3, 4, 1, 1, 4, 3, 3, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            } else {
               this.fillWithBlocks(var1, var3, 0, 1, 1, 0, 3, 3, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            }

            return true;
         }
      }
   }

   public static class Library extends StructureStrongholdPieces.Stronghold {
      private boolean isLargeRoom;

      public Library() {
      }

      public Library(int var1, Random var2, StructureBoundingBox var3, EnumFacing var4) {
         super(var1);
         this.setCoordBaseMode(var4);
         this.entryDoor = this.getRandomDoor(var2);
         this.boundingBox = var3;
         this.isLargeRoom = var3.getYSize() > 6;
      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         super.writeStructureToNBT(var1);
         var1.setBoolean("Tall", this.isLargeRoom);
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         super.readStructureFromNBT(var1);
         this.isLargeRoom = var1.getBoolean("Tall");
      }

      public static StructureStrongholdPieces.Library createPiece(List var0, Random var1, int var2, int var3, int var4, EnumFacing var5, int var6) {
         StructureBoundingBox var7 = StructureBoundingBox.getComponentToAddBoundingBox(var2, var3, var4, -4, -1, 0, 14, 11, 15, var5);
         if (!canStrongholdGoDeeper(var7) || StructureComponent.findIntersecting(var0, var7) != null) {
            var7 = StructureBoundingBox.getComponentToAddBoundingBox(var2, var3, var4, -4, -1, 0, 14, 6, 15, var5);
            if (!canStrongholdGoDeeper(var7) || StructureComponent.findIntersecting(var0, var7) != null) {
               return null;
            }
         }

         return new StructureStrongholdPieces.Library(var6, var1, var7, var5);
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.isLiquidInStructureBoundingBox(var1, var3)) {
            return false;
         } else {
            byte var4 = 11;
            if (!this.isLargeRoom) {
               var4 = 6;
            }

            this.fillWithRandomizedBlocks(var1, var3, 0, 0, 0, 13, var4 - 1, 14, true, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
            this.placeDoor(var1, var2, var3, this.entryDoor, 4, 1, 0);
            this.func_189914_a(var1, var3, var2, 0.07F, 2, 1, 1, 11, 4, 13, Blocks.WEB.getDefaultState(), Blocks.WEB.getDefaultState(), false, 0);
            boolean var5 = true;
            boolean var6 = true;

            for(int var7 = 1; var7 <= 13; ++var7) {
               if ((var7 - 1) % 4 == 0) {
                  this.fillWithBlocks(var1, var3, 1, 1, var7, 1, 4, var7, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(), false);
                  this.fillWithBlocks(var1, var3, 12, 1, var7, 12, 4, var7, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(), false);
                  this.setBlockState(var1, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.EAST), 2, 3, var7, var3);
                  this.setBlockState(var1, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.WEST), 11, 3, var7, var3);
                  if (this.isLargeRoom) {
                     this.fillWithBlocks(var1, var3, 1, 6, var7, 1, 9, var7, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(), false);
                     this.fillWithBlocks(var1, var3, 12, 6, var7, 12, 9, var7, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(), false);
                  }
               } else {
                  this.fillWithBlocks(var1, var3, 1, 1, var7, 1, 4, var7, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
                  this.fillWithBlocks(var1, var3, 12, 1, var7, 12, 4, var7, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
                  if (this.isLargeRoom) {
                     this.fillWithBlocks(var1, var3, 1, 6, var7, 1, 9, var7, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
                     this.fillWithBlocks(var1, var3, 12, 6, var7, 12, 9, var7, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
                  }
               }
            }

            for(int var11 = 3; var11 < 12; var11 += 2) {
               this.fillWithBlocks(var1, var3, 3, 1, var11, 4, 3, var11, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
               this.fillWithBlocks(var1, var3, 6, 1, var11, 7, 3, var11, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
               this.fillWithBlocks(var1, var3, 9, 1, var11, 10, 3, var11, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
            }

            if (this.isLargeRoom) {
               this.fillWithBlocks(var1, var3, 1, 5, 1, 3, 5, 13, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(), false);
               this.fillWithBlocks(var1, var3, 10, 5, 1, 12, 5, 13, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(), false);
               this.fillWithBlocks(var1, var3, 4, 5, 1, 9, 5, 2, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(), false);
               this.fillWithBlocks(var1, var3, 4, 5, 12, 9, 5, 13, Blocks.PLANKS.getDefaultState(), Blocks.PLANKS.getDefaultState(), false);
               this.setBlockState(var1, Blocks.PLANKS.getDefaultState(), 9, 5, 11, var3);
               this.setBlockState(var1, Blocks.PLANKS.getDefaultState(), 8, 5, 11, var3);
               this.setBlockState(var1, Blocks.PLANKS.getDefaultState(), 9, 5, 10, var3);
               this.fillWithBlocks(var1, var3, 3, 6, 2, 3, 6, 12, Blocks.OAK_FENCE.getDefaultState(), Blocks.OAK_FENCE.getDefaultState(), false);
               this.fillWithBlocks(var1, var3, 10, 6, 2, 10, 6, 10, Blocks.OAK_FENCE.getDefaultState(), Blocks.OAK_FENCE.getDefaultState(), false);
               this.fillWithBlocks(var1, var3, 4, 6, 2, 9, 6, 2, Blocks.OAK_FENCE.getDefaultState(), Blocks.OAK_FENCE.getDefaultState(), false);
               this.fillWithBlocks(var1, var3, 4, 6, 12, 8, 6, 12, Blocks.OAK_FENCE.getDefaultState(), Blocks.OAK_FENCE.getDefaultState(), false);
               this.setBlockState(var1, Blocks.OAK_FENCE.getDefaultState(), 9, 6, 11, var3);
               this.setBlockState(var1, Blocks.OAK_FENCE.getDefaultState(), 8, 6, 11, var3);
               this.setBlockState(var1, Blocks.OAK_FENCE.getDefaultState(), 9, 6, 10, var3);
               IBlockState var12 = Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.SOUTH);
               this.setBlockState(var1, var12, 10, 1, 13, var3);
               this.setBlockState(var1, var12, 10, 2, 13, var3);
               this.setBlockState(var1, var12, 10, 3, 13, var3);
               this.setBlockState(var1, var12, 10, 4, 13, var3);
               this.setBlockState(var1, var12, 10, 5, 13, var3);
               this.setBlockState(var1, var12, 10, 6, 13, var3);
               this.setBlockState(var1, var12, 10, 7, 13, var3);
               boolean var8 = true;
               boolean var9 = true;
               this.setBlockState(var1, Blocks.OAK_FENCE.getDefaultState(), 6, 9, 7, var3);
               this.setBlockState(var1, Blocks.OAK_FENCE.getDefaultState(), 7, 9, 7, var3);
               this.setBlockState(var1, Blocks.OAK_FENCE.getDefaultState(), 6, 8, 7, var3);
               this.setBlockState(var1, Blocks.OAK_FENCE.getDefaultState(), 7, 8, 7, var3);
               this.setBlockState(var1, Blocks.OAK_FENCE.getDefaultState(), 6, 7, 7, var3);
               this.setBlockState(var1, Blocks.OAK_FENCE.getDefaultState(), 7, 7, 7, var3);
               this.setBlockState(var1, Blocks.OAK_FENCE.getDefaultState(), 5, 7, 7, var3);
               this.setBlockState(var1, Blocks.OAK_FENCE.getDefaultState(), 8, 7, 7, var3);
               this.setBlockState(var1, Blocks.OAK_FENCE.getDefaultState(), 6, 7, 6, var3);
               this.setBlockState(var1, Blocks.OAK_FENCE.getDefaultState(), 6, 7, 8, var3);
               this.setBlockState(var1, Blocks.OAK_FENCE.getDefaultState(), 7, 7, 6, var3);
               this.setBlockState(var1, Blocks.OAK_FENCE.getDefaultState(), 7, 7, 8, var3);
               IBlockState var10 = Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.UP);
               this.setBlockState(var1, var10, 5, 8, 7, var3);
               this.setBlockState(var1, var10, 8, 8, 7, var3);
               this.setBlockState(var1, var10, 6, 8, 6, var3);
               this.setBlockState(var1, var10, 6, 8, 8, var3);
               this.setBlockState(var1, var10, 7, 8, 6, var3);
               this.setBlockState(var1, var10, 7, 8, 8, var3);
            }

            this.generateChest(var1, var3, var2, 3, 3, 5, LootTableList.CHESTS_STRONGHOLD_LIBRARY);
            if (this.isLargeRoom) {
               this.setBlockState(var1, Blocks.AIR.getDefaultState(), 12, 9, 1, var3);
               this.generateChest(var1, var3, var2, 12, 8, 1, LootTableList.CHESTS_STRONGHOLD_LIBRARY);
            }

            return true;
         }
      }
   }

   static class PieceWeight {
      public Class pieceClass;
      public final int pieceWeight;
      public int instancesSpawned;
      public int instancesLimit;

      public PieceWeight(Class var1, int var2, int var3) {
         this.pieceClass = var1;
         this.pieceWeight = var2;
         this.instancesLimit = var3;
      }

      public boolean canSpawnMoreStructuresOfType(int var1) {
         return this.instancesLimit == 0 || this.instancesSpawned < this.instancesLimit;
      }

      public boolean canSpawnMoreStructures() {
         return this.instancesLimit == 0 || this.instancesSpawned < this.instancesLimit;
      }
   }

   public static class PortalRoom extends StructureStrongholdPieces.Stronghold {
      private boolean hasSpawner;

      public PortalRoom() {
      }

      public PortalRoom(int var1, Random var2, StructureBoundingBox var3, EnumFacing var4) {
         super(var1);
         this.setCoordBaseMode(var4);
         this.boundingBox = var3;
      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         super.writeStructureToNBT(var1);
         var1.setBoolean("Mob", this.hasSpawner);
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         super.readStructureFromNBT(var1);
         this.hasSpawner = var1.getBoolean("Mob");
      }

      public void buildComponent(StructureComponent var1, List var2, Random var3) {
         if (var1 != null) {
            ((StructureStrongholdPieces.Stairs2)var1).strongholdPortalRoom = this;
         }

      }

      public static StructureStrongholdPieces.PortalRoom createPiece(List var0, Random var1, int var2, int var3, int var4, EnumFacing var5, int var6) {
         StructureBoundingBox var7 = StructureBoundingBox.getComponentToAddBoundingBox(var2, var3, var4, -4, -1, 0, 11, 8, 16, var5);
         return canStrongholdGoDeeper(var7) && StructureComponent.findIntersecting(var0, var7) == null ? new StructureStrongholdPieces.PortalRoom(var6, var1, var7, var5) : null;
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         this.fillWithRandomizedBlocks(var1, var3, 0, 0, 0, 10, 7, 15, false, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
         this.placeDoor(var1, var2, var3, StructureStrongholdPieces.Stronghold.Door.GRATES, 4, 1, 0);
         int var4 = 6;
         this.fillWithRandomizedBlocks(var1, var3, 1, var4, 1, 1, var4, 14, false, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
         this.fillWithRandomizedBlocks(var1, var3, 9, var4, 1, 9, var4, 14, false, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
         this.fillWithRandomizedBlocks(var1, var3, 2, var4, 1, 8, var4, 2, false, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
         this.fillWithRandomizedBlocks(var1, var3, 2, var4, 14, 8, var4, 14, false, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
         this.fillWithRandomizedBlocks(var1, var3, 1, 1, 1, 2, 1, 4, false, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
         this.fillWithRandomizedBlocks(var1, var3, 8, 1, 1, 9, 1, 4, false, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
         this.fillWithBlocks(var1, var3, 1, 1, 1, 1, 1, 3, Blocks.FLOWING_LAVA.getDefaultState(), Blocks.FLOWING_LAVA.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 9, 1, 1, 9, 1, 3, Blocks.FLOWING_LAVA.getDefaultState(), Blocks.FLOWING_LAVA.getDefaultState(), false);
         this.fillWithRandomizedBlocks(var1, var3, 3, 1, 8, 7, 1, 12, false, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
         this.fillWithBlocks(var1, var3, 4, 1, 9, 6, 1, 11, Blocks.FLOWING_LAVA.getDefaultState(), Blocks.FLOWING_LAVA.getDefaultState(), false);

         for(int var5 = 3; var5 < 14; var5 += 2) {
            this.fillWithBlocks(var1, var3, 0, 3, var5, 0, 4, var5, Blocks.IRON_BARS.getDefaultState(), Blocks.IRON_BARS.getDefaultState(), false);
            this.fillWithBlocks(var1, var3, 10, 3, var5, 10, 4, var5, Blocks.IRON_BARS.getDefaultState(), Blocks.IRON_BARS.getDefaultState(), false);
         }

         for(int var15 = 2; var15 < 9; var15 += 2) {
            this.fillWithBlocks(var1, var3, var15, 3, 15, var15, 4, 15, Blocks.IRON_BARS.getDefaultState(), Blocks.IRON_BARS.getDefaultState(), false);
         }

         IBlockState var16 = Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH);
         this.fillWithRandomizedBlocks(var1, var3, 4, 1, 5, 6, 1, 7, false, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
         this.fillWithRandomizedBlocks(var1, var3, 4, 2, 6, 6, 2, 7, false, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
         this.fillWithRandomizedBlocks(var1, var3, 4, 3, 7, 6, 3, 7, false, var2, StructureStrongholdPieces.STRONGHOLD_STONES);

         for(int var6 = 4; var6 <= 6; ++var6) {
            this.setBlockState(var1, var16, var6, 1, 4, var3);
            this.setBlockState(var1, var16, var6, 2, 5, var3);
            this.setBlockState(var1, var16, var6, 3, 6, var3);
         }

         IBlockState var17 = Blocks.END_PORTAL_FRAME.getDefaultState().withProperty(BlockEndPortalFrame.FACING, EnumFacing.NORTH);
         IBlockState var7 = Blocks.END_PORTAL_FRAME.getDefaultState().withProperty(BlockEndPortalFrame.FACING, EnumFacing.SOUTH);
         IBlockState var8 = Blocks.END_PORTAL_FRAME.getDefaultState().withProperty(BlockEndPortalFrame.FACING, EnumFacing.EAST);
         IBlockState var9 = Blocks.END_PORTAL_FRAME.getDefaultState().withProperty(BlockEndPortalFrame.FACING, EnumFacing.WEST);
         boolean var10 = true;
         boolean[] var11 = new boolean[12];

         for(int var12 = 0; var12 < var11.length; ++var12) {
            var11[var12] = var2.nextFloat() > 0.9F;
            var10 &= var11[var12];
         }

         this.setBlockState(var1, var17.withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(var11[0])), 4, 3, 8, var3);
         this.setBlockState(var1, var17.withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(var11[1])), 5, 3, 8, var3);
         this.setBlockState(var1, var17.withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(var11[2])), 6, 3, 8, var3);
         this.setBlockState(var1, var7.withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(var11[3])), 4, 3, 12, var3);
         this.setBlockState(var1, var7.withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(var11[4])), 5, 3, 12, var3);
         this.setBlockState(var1, var7.withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(var11[5])), 6, 3, 12, var3);
         this.setBlockState(var1, var8.withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(var11[6])), 3, 3, 9, var3);
         this.setBlockState(var1, var8.withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(var11[7])), 3, 3, 10, var3);
         this.setBlockState(var1, var8.withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(var11[8])), 3, 3, 11, var3);
         this.setBlockState(var1, var9.withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(var11[9])), 7, 3, 9, var3);
         this.setBlockState(var1, var9.withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(var11[10])), 7, 3, 10, var3);
         this.setBlockState(var1, var9.withProperty(BlockEndPortalFrame.EYE, Boolean.valueOf(var11[11])), 7, 3, 11, var3);
         if (var10) {
            IBlockState var18 = Blocks.END_PORTAL.getDefaultState();
            this.setBlockState(var1, var18, 4, 3, 9, var3);
            this.setBlockState(var1, var18, 5, 3, 9, var3);
            this.setBlockState(var1, var18, 6, 3, 9, var3);
            this.setBlockState(var1, var18, 4, 3, 10, var3);
            this.setBlockState(var1, var18, 5, 3, 10, var3);
            this.setBlockState(var1, var18, 6, 3, 10, var3);
            this.setBlockState(var1, var18, 4, 3, 11, var3);
            this.setBlockState(var1, var18, 5, 3, 11, var3);
            this.setBlockState(var1, var18, 6, 3, 11, var3);
         }

         if (!this.hasSpawner) {
            var4 = this.getYWithOffset(3);
            BlockPos var19 = new BlockPos(this.getXWithOffset(5, 6), var4, this.getZWithOffset(5, 6));
            if (var3.isVecInside(var19)) {
               this.hasSpawner = true;
               var1.setBlockState(var19, Blocks.MOB_SPAWNER.getDefaultState(), 2);
               TileEntity var13 = var1.getTileEntity(var19);
               if (var13 instanceof TileEntityMobSpawner) {
                  ((TileEntityMobSpawner)var13).getSpawnerBaseLogic().setEntityName("Silverfish");
               }
            }
         }

         return true;
      }
   }

   public static class Prison extends StructureStrongholdPieces.Stronghold {
      public Prison() {
      }

      public Prison(int var1, Random var2, StructureBoundingBox var3, EnumFacing var4) {
         super(var1);
         this.setCoordBaseMode(var4);
         this.entryDoor = this.getRandomDoor(var2);
         this.boundingBox = var3;
      }

      public void buildComponent(StructureComponent var1, List var2, Random var3) {
         this.getNextComponentNormal((StructureStrongholdPieces.Stairs2)var1, var2, var3, 1, 1);
      }

      public static StructureStrongholdPieces.Prison createPiece(List var0, Random var1, int var2, int var3, int var4, EnumFacing var5, int var6) {
         StructureBoundingBox var7 = StructureBoundingBox.getComponentToAddBoundingBox(var2, var3, var4, -1, -1, 0, 9, 5, 11, var5);
         return canStrongholdGoDeeper(var7) && StructureComponent.findIntersecting(var0, var7) == null ? new StructureStrongholdPieces.Prison(var6, var1, var7, var5) : null;
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.isLiquidInStructureBoundingBox(var1, var3)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(var1, var3, 0, 0, 0, 8, 4, 10, true, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
            this.placeDoor(var1, var2, var3, this.entryDoor, 1, 1, 0);
            this.fillWithBlocks(var1, var3, 1, 1, 10, 3, 3, 10, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            this.fillWithRandomizedBlocks(var1, var3, 4, 1, 1, 4, 3, 1, false, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
            this.fillWithRandomizedBlocks(var1, var3, 4, 1, 3, 4, 3, 3, false, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
            this.fillWithRandomizedBlocks(var1, var3, 4, 1, 7, 4, 3, 7, false, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
            this.fillWithRandomizedBlocks(var1, var3, 4, 1, 9, 4, 3, 9, false, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
            this.fillWithBlocks(var1, var3, 4, 1, 4, 4, 3, 6, Blocks.IRON_BARS.getDefaultState(), Blocks.IRON_BARS.getDefaultState(), false);
            this.fillWithBlocks(var1, var3, 5, 1, 5, 7, 3, 5, Blocks.IRON_BARS.getDefaultState(), Blocks.IRON_BARS.getDefaultState(), false);
            this.setBlockState(var1, Blocks.IRON_BARS.getDefaultState(), 4, 3, 2, var3);
            this.setBlockState(var1, Blocks.IRON_BARS.getDefaultState(), 4, 3, 8, var3);
            IBlockState var4 = Blocks.IRON_DOOR.getDefaultState().withProperty(BlockDoor.FACING, EnumFacing.WEST);
            IBlockState var5 = Blocks.IRON_DOOR.getDefaultState().withProperty(BlockDoor.FACING, EnumFacing.WEST).withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER);
            this.setBlockState(var1, var4, 4, 1, 2, var3);
            this.setBlockState(var1, var5, 4, 2, 2, var3);
            this.setBlockState(var1, var4, 4, 1, 8, var3);
            this.setBlockState(var1, var5, 4, 2, 8, var3);
            return true;
         }
      }
   }

   public static class RightTurn extends StructureStrongholdPieces.LeftTurn {
      public void buildComponent(StructureComponent var1, List var2, Random var3) {
         EnumFacing var4 = this.getCoordBaseMode();
         if (var4 != EnumFacing.NORTH && var4 != EnumFacing.EAST) {
            this.getNextComponentX((StructureStrongholdPieces.Stairs2)var1, var2, var3, 1, 1);
         } else {
            this.getNextComponentZ((StructureStrongholdPieces.Stairs2)var1, var2, var3, 1, 1);
         }

      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.isLiquidInStructureBoundingBox(var1, var3)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(var1, var3, 0, 0, 0, 4, 4, 4, true, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
            this.placeDoor(var1, var2, var3, this.entryDoor, 1, 1, 0);
            EnumFacing var4 = this.getCoordBaseMode();
            if (var4 != EnumFacing.NORTH && var4 != EnumFacing.EAST) {
               this.fillWithBlocks(var1, var3, 0, 1, 1, 0, 3, 3, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            } else {
               this.fillWithBlocks(var1, var3, 4, 1, 1, 4, 3, 3, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            }

            return true;
         }
      }
   }

   public static class RoomCrossing extends StructureStrongholdPieces.Stronghold {
      protected int roomType;

      public RoomCrossing() {
      }

      public RoomCrossing(int var1, Random var2, StructureBoundingBox var3, EnumFacing var4) {
         super(var1);
         this.setCoordBaseMode(var4);
         this.entryDoor = this.getRandomDoor(var2);
         this.boundingBox = var3;
         this.roomType = var2.nextInt(5);
      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         super.writeStructureToNBT(var1);
         var1.setInteger("Type", this.roomType);
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         super.readStructureFromNBT(var1);
         this.roomType = var1.getInteger("Type");
      }

      public void buildComponent(StructureComponent var1, List var2, Random var3) {
         this.getNextComponentNormal((StructureStrongholdPieces.Stairs2)var1, var2, var3, 4, 1);
         this.getNextComponentX((StructureStrongholdPieces.Stairs2)var1, var2, var3, 1, 4);
         this.getNextComponentZ((StructureStrongholdPieces.Stairs2)var1, var2, var3, 1, 4);
      }

      public static StructureStrongholdPieces.RoomCrossing createPiece(List var0, Random var1, int var2, int var3, int var4, EnumFacing var5, int var6) {
         StructureBoundingBox var7 = StructureBoundingBox.getComponentToAddBoundingBox(var2, var3, var4, -4, -1, 0, 11, 7, 11, var5);
         return canStrongholdGoDeeper(var7) && StructureComponent.findIntersecting(var0, var7) == null ? new StructureStrongholdPieces.RoomCrossing(var6, var1, var7, var5) : null;
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.isLiquidInStructureBoundingBox(var1, var3)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(var1, var3, 0, 0, 0, 10, 6, 10, true, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
            this.placeDoor(var1, var2, var3, this.entryDoor, 4, 1, 0);
            this.fillWithBlocks(var1, var3, 4, 1, 10, 6, 3, 10, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            this.fillWithBlocks(var1, var3, 0, 1, 4, 0, 3, 6, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            this.fillWithBlocks(var1, var3, 10, 1, 4, 10, 3, 6, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            switch(this.roomType) {
            case 0:
               this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 5, 1, 5, var3);
               this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 5, 2, 5, var3);
               this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 5, 3, 5, var3);
               this.setBlockState(var1, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.WEST), 4, 3, 5, var3);
               this.setBlockState(var1, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.EAST), 6, 3, 5, var3);
               this.setBlockState(var1, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.SOUTH), 5, 3, 4, var3);
               this.setBlockState(var1, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.NORTH), 5, 3, 6, var3);
               this.setBlockState(var1, Blocks.STONE_SLAB.getDefaultState(), 4, 1, 4, var3);
               this.setBlockState(var1, Blocks.STONE_SLAB.getDefaultState(), 4, 1, 5, var3);
               this.setBlockState(var1, Blocks.STONE_SLAB.getDefaultState(), 4, 1, 6, var3);
               this.setBlockState(var1, Blocks.STONE_SLAB.getDefaultState(), 6, 1, 4, var3);
               this.setBlockState(var1, Blocks.STONE_SLAB.getDefaultState(), 6, 1, 5, var3);
               this.setBlockState(var1, Blocks.STONE_SLAB.getDefaultState(), 6, 1, 6, var3);
               this.setBlockState(var1, Blocks.STONE_SLAB.getDefaultState(), 5, 1, 4, var3);
               this.setBlockState(var1, Blocks.STONE_SLAB.getDefaultState(), 5, 1, 6, var3);
               break;
            case 1:
               for(int var9 = 0; var9 < 5; ++var9) {
                  this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 3, 1, 3 + var9, var3);
                  this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 7, 1, 3 + var9, var3);
                  this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 3 + var9, 1, 3, var3);
                  this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 3 + var9, 1, 7, var3);
               }

               this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 5, 1, 5, var3);
               this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 5, 2, 5, var3);
               this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 5, 3, 5, var3);
               this.setBlockState(var1, Blocks.FLOWING_WATER.getDefaultState(), 5, 4, 5, var3);
               break;
            case 2:
               for(int var4 = 1; var4 <= 9; ++var4) {
                  this.setBlockState(var1, Blocks.COBBLESTONE.getDefaultState(), 1, 3, var4, var3);
                  this.setBlockState(var1, Blocks.COBBLESTONE.getDefaultState(), 9, 3, var4, var3);
               }

               for(int var5 = 1; var5 <= 9; ++var5) {
                  this.setBlockState(var1, Blocks.COBBLESTONE.getDefaultState(), var5, 3, 1, var3);
                  this.setBlockState(var1, Blocks.COBBLESTONE.getDefaultState(), var5, 3, 9, var3);
               }

               this.setBlockState(var1, Blocks.COBBLESTONE.getDefaultState(), 5, 1, 4, var3);
               this.setBlockState(var1, Blocks.COBBLESTONE.getDefaultState(), 5, 1, 6, var3);
               this.setBlockState(var1, Blocks.COBBLESTONE.getDefaultState(), 5, 3, 4, var3);
               this.setBlockState(var1, Blocks.COBBLESTONE.getDefaultState(), 5, 3, 6, var3);
               this.setBlockState(var1, Blocks.COBBLESTONE.getDefaultState(), 4, 1, 5, var3);
               this.setBlockState(var1, Blocks.COBBLESTONE.getDefaultState(), 6, 1, 5, var3);
               this.setBlockState(var1, Blocks.COBBLESTONE.getDefaultState(), 4, 3, 5, var3);
               this.setBlockState(var1, Blocks.COBBLESTONE.getDefaultState(), 6, 3, 5, var3);

               for(int var6 = 1; var6 <= 3; ++var6) {
                  this.setBlockState(var1, Blocks.COBBLESTONE.getDefaultState(), 4, var6, 4, var3);
                  this.setBlockState(var1, Blocks.COBBLESTONE.getDefaultState(), 6, var6, 4, var3);
                  this.setBlockState(var1, Blocks.COBBLESTONE.getDefaultState(), 4, var6, 6, var3);
                  this.setBlockState(var1, Blocks.COBBLESTONE.getDefaultState(), 6, var6, 6, var3);
               }

               this.setBlockState(var1, Blocks.TORCH.getDefaultState(), 5, 3, 5, var3);

               for(int var7 = 2; var7 <= 8; ++var7) {
                  this.setBlockState(var1, Blocks.PLANKS.getDefaultState(), 2, 3, var7, var3);
                  this.setBlockState(var1, Blocks.PLANKS.getDefaultState(), 3, 3, var7, var3);
                  if (var7 <= 3 || var7 >= 7) {
                     this.setBlockState(var1, Blocks.PLANKS.getDefaultState(), 4, 3, var7, var3);
                     this.setBlockState(var1, Blocks.PLANKS.getDefaultState(), 5, 3, var7, var3);
                     this.setBlockState(var1, Blocks.PLANKS.getDefaultState(), 6, 3, var7, var3);
                  }

                  this.setBlockState(var1, Blocks.PLANKS.getDefaultState(), 7, 3, var7, var3);
                  this.setBlockState(var1, Blocks.PLANKS.getDefaultState(), 8, 3, var7, var3);
               }

               IBlockState var8 = Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.WEST);
               this.setBlockState(var1, var8, 9, 1, 3, var3);
               this.setBlockState(var1, var8, 9, 2, 3, var3);
               this.setBlockState(var1, var8, 9, 3, 3, var3);
               this.generateChest(var1, var3, var2, 3, 4, 8, LootTableList.CHESTS_STRONGHOLD_CROSSING);
            }

            return true;
         }
      }
   }

   public static class Stairs extends StructureStrongholdPieces.Stronghold {
      private boolean source;

      public Stairs() {
      }

      public Stairs(int var1, Random var2, int var3, int var4) {
         super(var1);
         this.source = true;
         this.setCoordBaseMode(EnumFacing.Plane.HORIZONTAL.random(var2));
         this.entryDoor = StructureStrongholdPieces.Stronghold.Door.OPENING;
         if (this.getCoordBaseMode().getAxis() == EnumFacing.Axis.Z) {
            this.boundingBox = new StructureBoundingBox(var3, 64, var4, var3 + 5 - 1, 74, var4 + 5 - 1);
         } else {
            this.boundingBox = new StructureBoundingBox(var3, 64, var4, var3 + 5 - 1, 74, var4 + 5 - 1);
         }

      }

      public Stairs(int var1, Random var2, StructureBoundingBox var3, EnumFacing var4) {
         super(var1);
         this.source = false;
         this.setCoordBaseMode(var4);
         this.entryDoor = this.getRandomDoor(var2);
         this.boundingBox = var3;
      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         super.writeStructureToNBT(var1);
         var1.setBoolean("Source", this.source);
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         super.readStructureFromNBT(var1);
         this.source = var1.getBoolean("Source");
      }

      public void buildComponent(StructureComponent var1, List var2, Random var3) {
         if (this.source) {
            StructureStrongholdPieces.strongComponentType = StructureStrongholdPieces.Crossing.class;
         }

         this.getNextComponentNormal((StructureStrongholdPieces.Stairs2)var1, var2, var3, 1, 1);
      }

      public static StructureStrongholdPieces.Stairs createPiece(List var0, Random var1, int var2, int var3, int var4, EnumFacing var5, int var6) {
         StructureBoundingBox var7 = StructureBoundingBox.getComponentToAddBoundingBox(var2, var3, var4, -1, -7, 0, 5, 11, 5, var5);
         return canStrongholdGoDeeper(var7) && StructureComponent.findIntersecting(var0, var7) == null ? new StructureStrongholdPieces.Stairs(var6, var1, var7, var5) : null;
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.isLiquidInStructureBoundingBox(var1, var3)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(var1, var3, 0, 0, 0, 4, 10, 4, true, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
            this.placeDoor(var1, var2, var3, this.entryDoor, 1, 7, 0);
            this.placeDoor(var1, var2, var3, StructureStrongholdPieces.Stronghold.Door.OPENING, 1, 1, 4);
            this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 2, 6, 1, var3);
            this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 1, 5, 1, var3);
            this.setBlockState(var1, Blocks.STONE_SLAB.getStateFromMeta(BlockStoneSlab.EnumType.STONE.getMetadata()), 1, 6, 1, var3);
            this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 1, 5, 2, var3);
            this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 1, 4, 3, var3);
            this.setBlockState(var1, Blocks.STONE_SLAB.getStateFromMeta(BlockStoneSlab.EnumType.STONE.getMetadata()), 1, 5, 3, var3);
            this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 2, 4, 3, var3);
            this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 3, 3, 3, var3);
            this.setBlockState(var1, Blocks.STONE_SLAB.getStateFromMeta(BlockStoneSlab.EnumType.STONE.getMetadata()), 3, 4, 3, var3);
            this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 3, 3, 2, var3);
            this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 3, 2, 1, var3);
            this.setBlockState(var1, Blocks.STONE_SLAB.getStateFromMeta(BlockStoneSlab.EnumType.STONE.getMetadata()), 3, 3, 1, var3);
            this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 2, 2, 1, var3);
            this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 1, 1, 1, var3);
            this.setBlockState(var1, Blocks.STONE_SLAB.getStateFromMeta(BlockStoneSlab.EnumType.STONE.getMetadata()), 1, 2, 1, var3);
            this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 1, 1, 2, var3);
            this.setBlockState(var1, Blocks.STONE_SLAB.getStateFromMeta(BlockStoneSlab.EnumType.STONE.getMetadata()), 1, 1, 3, var3);
            return true;
         }
      }
   }

   public static class Stairs2 extends StructureStrongholdPieces.Stairs {
      public StructureStrongholdPieces.PieceWeight strongholdPieceWeight;
      public StructureStrongholdPieces.PortalRoom strongholdPortalRoom;
      public List pendingChildren = Lists.newArrayList();

      public Stairs2() {
      }

      public Stairs2(int var1, Random var2, int var3, int var4) {
         super(0, var2, var3, var4);
      }

      public BlockPos getBoundingBoxCenter() {
         return this.strongholdPortalRoom != null ? this.strongholdPortalRoom.getBoundingBoxCenter() : super.getBoundingBoxCenter();
      }
   }

   public static class StairsStraight extends StructureStrongholdPieces.Stronghold {
      public StairsStraight() {
      }

      public StairsStraight(int var1, Random var2, StructureBoundingBox var3, EnumFacing var4) {
         super(var1);
         this.setCoordBaseMode(var4);
         this.entryDoor = this.getRandomDoor(var2);
         this.boundingBox = var3;
      }

      public void buildComponent(StructureComponent var1, List var2, Random var3) {
         this.getNextComponentNormal((StructureStrongholdPieces.Stairs2)var1, var2, var3, 1, 1);
      }

      public static StructureStrongholdPieces.StairsStraight createPiece(List var0, Random var1, int var2, int var3, int var4, EnumFacing var5, int var6) {
         StructureBoundingBox var7 = StructureBoundingBox.getComponentToAddBoundingBox(var2, var3, var4, -1, -7, 0, 5, 11, 8, var5);
         return canStrongholdGoDeeper(var7) && StructureComponent.findIntersecting(var0, var7) == null ? new StructureStrongholdPieces.StairsStraight(var6, var1, var7, var5) : null;
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.isLiquidInStructureBoundingBox(var1, var3)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(var1, var3, 0, 0, 0, 4, 10, 7, true, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
            this.placeDoor(var1, var2, var3, this.entryDoor, 1, 7, 0);
            this.placeDoor(var1, var2, var3, StructureStrongholdPieces.Stronghold.Door.OPENING, 1, 1, 7);
            IBlockState var4 = Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH);

            for(int var5 = 0; var5 < 6; ++var5) {
               this.setBlockState(var1, var4, 1, 6 - var5, 1 + var5, var3);
               this.setBlockState(var1, var4, 2, 6 - var5, 1 + var5, var3);
               this.setBlockState(var1, var4, 3, 6 - var5, 1 + var5, var3);
               if (var5 < 5) {
                  this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 1, 5 - var5, 1 + var5, var3);
                  this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 2, 5 - var5, 1 + var5, var3);
                  this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), 3, 5 - var5, 1 + var5, var3);
               }
            }

            return true;
         }
      }
   }

   static class Stones extends StructureComponent.BlockSelector {
      private Stones() {
      }

      public void selectBlocks(Random var1, int var2, int var3, int var4, boolean var5) {
         if (var5) {
            float var6 = var1.nextFloat();
            if (var6 < 0.2F) {
               this.blockstate = Blocks.STONEBRICK.getStateFromMeta(BlockStoneBrick.CRACKED_META);
            } else if (var6 < 0.5F) {
               this.blockstate = Blocks.STONEBRICK.getStateFromMeta(BlockStoneBrick.MOSSY_META);
            } else if (var6 < 0.55F) {
               this.blockstate = Blocks.MONSTER_EGG.getStateFromMeta(BlockSilverfish.EnumType.STONEBRICK.getMetadata());
            } else {
               this.blockstate = Blocks.STONEBRICK.getDefaultState();
            }
         } else {
            this.blockstate = Blocks.AIR.getDefaultState();
         }

      }
   }

   public static class Straight extends StructureStrongholdPieces.Stronghold {
      private boolean expandsX;
      private boolean expandsZ;

      public Straight() {
      }

      public Straight(int var1, Random var2, StructureBoundingBox var3, EnumFacing var4) {
         super(var1);
         this.setCoordBaseMode(var4);
         this.entryDoor = this.getRandomDoor(var2);
         this.boundingBox = var3;
         this.expandsX = var2.nextInt(2) == 0;
         this.expandsZ = var2.nextInt(2) == 0;
      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         super.writeStructureToNBT(var1);
         var1.setBoolean("Left", this.expandsX);
         var1.setBoolean("Right", this.expandsZ);
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         super.readStructureFromNBT(var1);
         this.expandsX = var1.getBoolean("Left");
         this.expandsZ = var1.getBoolean("Right");
      }

      public void buildComponent(StructureComponent var1, List var2, Random var3) {
         this.getNextComponentNormal((StructureStrongholdPieces.Stairs2)var1, var2, var3, 1, 1);
         if (this.expandsX) {
            this.getNextComponentX((StructureStrongholdPieces.Stairs2)var1, var2, var3, 1, 2);
         }

         if (this.expandsZ) {
            this.getNextComponentZ((StructureStrongholdPieces.Stairs2)var1, var2, var3, 1, 2);
         }

      }

      public static StructureStrongholdPieces.Straight createPiece(List var0, Random var1, int var2, int var3, int var4, EnumFacing var5, int var6) {
         StructureBoundingBox var7 = StructureBoundingBox.getComponentToAddBoundingBox(var2, var3, var4, -1, -1, 0, 5, 5, 7, var5);
         return canStrongholdGoDeeper(var7) && StructureComponent.findIntersecting(var0, var7) == null ? new StructureStrongholdPieces.Straight(var6, var1, var7, var5) : null;
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.isLiquidInStructureBoundingBox(var1, var3)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(var1, var3, 0, 0, 0, 4, 4, 6, true, var2, StructureStrongholdPieces.STRONGHOLD_STONES);
            this.placeDoor(var1, var2, var3, this.entryDoor, 1, 1, 0);
            this.placeDoor(var1, var2, var3, StructureStrongholdPieces.Stronghold.Door.OPENING, 1, 1, 6);
            IBlockState var4 = Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.EAST);
            IBlockState var5 = Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.WEST);
            this.randomlyPlaceBlock(var1, var3, var2, 0.1F, 1, 2, 1, var4);
            this.randomlyPlaceBlock(var1, var3, var2, 0.1F, 3, 2, 1, var5);
            this.randomlyPlaceBlock(var1, var3, var2, 0.1F, 1, 2, 5, var4);
            this.randomlyPlaceBlock(var1, var3, var2, 0.1F, 3, 2, 5, var5);
            if (this.expandsX) {
               this.fillWithBlocks(var1, var3, 0, 1, 2, 0, 3, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            }

            if (this.expandsZ) {
               this.fillWithBlocks(var1, var3, 4, 1, 2, 4, 3, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            }

            return true;
         }
      }
   }

   public abstract static class Stronghold extends StructureComponent {
      protected StructureStrongholdPieces.Stronghold.Door entryDoor = StructureStrongholdPieces.Stronghold.Door.OPENING;

      public Stronghold() {
      }

      protected Stronghold(int var1) {
         super(var1);
      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         var1.setString("EntryDoor", this.entryDoor.name());
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         this.entryDoor = StructureStrongholdPieces.Stronghold.Door.valueOf(var1.getString("EntryDoor"));
      }

      protected void placeDoor(World var1, Random var2, StructureBoundingBox var3, StructureStrongholdPieces.Stronghold.Door var4, int var5, int var6, int var7) {
         switch(var4) {
         case OPENING:
            this.fillWithBlocks(var1, var3, var5, var6, var7, var5 + 3 - 1, var6 + 3 - 1, var7, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            break;
         case WOOD_DOOR:
            this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), var5, var6, var7, var3);
            this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), var5, var6 + 1, var7, var3);
            this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), var5, var6 + 2, var7, var3);
            this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), var5 + 1, var6 + 2, var7, var3);
            this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), var5 + 2, var6 + 2, var7, var3);
            this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), var5 + 2, var6 + 1, var7, var3);
            this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), var5 + 2, var6, var7, var3);
            this.setBlockState(var1, Blocks.OAK_DOOR.getDefaultState(), var5 + 1, var6, var7, var3);
            this.setBlockState(var1, Blocks.OAK_DOOR.getDefaultState().withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER), var5 + 1, var6 + 1, var7, var3);
            break;
         case GRATES:
            this.setBlockState(var1, Blocks.AIR.getDefaultState(), var5 + 1, var6, var7, var3);
            this.setBlockState(var1, Blocks.AIR.getDefaultState(), var5 + 1, var6 + 1, var7, var3);
            this.setBlockState(var1, Blocks.IRON_BARS.getDefaultState(), var5, var6, var7, var3);
            this.setBlockState(var1, Blocks.IRON_BARS.getDefaultState(), var5, var6 + 1, var7, var3);
            this.setBlockState(var1, Blocks.IRON_BARS.getDefaultState(), var5, var6 + 2, var7, var3);
            this.setBlockState(var1, Blocks.IRON_BARS.getDefaultState(), var5 + 1, var6 + 2, var7, var3);
            this.setBlockState(var1, Blocks.IRON_BARS.getDefaultState(), var5 + 2, var6 + 2, var7, var3);
            this.setBlockState(var1, Blocks.IRON_BARS.getDefaultState(), var5 + 2, var6 + 1, var7, var3);
            this.setBlockState(var1, Blocks.IRON_BARS.getDefaultState(), var5 + 2, var6, var7, var3);
            break;
         case IRON_DOOR:
            this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), var5, var6, var7, var3);
            this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), var5, var6 + 1, var7, var3);
            this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), var5, var6 + 2, var7, var3);
            this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), var5 + 1, var6 + 2, var7, var3);
            this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), var5 + 2, var6 + 2, var7, var3);
            this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), var5 + 2, var6 + 1, var7, var3);
            this.setBlockState(var1, Blocks.STONEBRICK.getDefaultState(), var5 + 2, var6, var7, var3);
            this.setBlockState(var1, Blocks.IRON_DOOR.getDefaultState(), var5 + 1, var6, var7, var3);
            this.setBlockState(var1, Blocks.IRON_DOOR.getDefaultState().withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER), var5 + 1, var6 + 1, var7, var3);
            this.setBlockState(var1, Blocks.STONE_BUTTON.getDefaultState().withProperty(BlockButton.FACING, EnumFacing.NORTH), var5 + 2, var6 + 1, var7 + 1, var3);
            this.setBlockState(var1, Blocks.STONE_BUTTON.getDefaultState().withProperty(BlockButton.FACING, EnumFacing.SOUTH), var5 + 2, var6 + 1, var7 - 1, var3);
         }

      }

      protected StructureStrongholdPieces.Stronghold.Door getRandomDoor(Random var1) {
         int var2 = var1.nextInt(5);
         switch(var2) {
         case 0:
         case 1:
         default:
            return StructureStrongholdPieces.Stronghold.Door.OPENING;
         case 2:
            return StructureStrongholdPieces.Stronghold.Door.WOOD_DOOR;
         case 3:
            return StructureStrongholdPieces.Stronghold.Door.GRATES;
         case 4:
            return StructureStrongholdPieces.Stronghold.Door.IRON_DOOR;
         }
      }

      protected StructureComponent getNextComponentNormal(StructureStrongholdPieces.Stairs2 var1, List var2, Random var3, int var4, int var5) {
         EnumFacing var6 = this.getCoordBaseMode();
         if (var6 != null) {
            switch(var6) {
            case NORTH:
               return StructureStrongholdPieces.generateAndAddPiece(var1, var2, var3, this.boundingBox.minX + var4, this.boundingBox.minY + var5, this.boundingBox.minZ - 1, var6, this.getComponentType());
            case SOUTH:
               return StructureStrongholdPieces.generateAndAddPiece(var1, var2, var3, this.boundingBox.minX + var4, this.boundingBox.minY + var5, this.boundingBox.maxZ + 1, var6, this.getComponentType());
            case WEST:
               return StructureStrongholdPieces.generateAndAddPiece(var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.minY + var5, this.boundingBox.minZ + var4, var6, this.getComponentType());
            case EAST:
               return StructureStrongholdPieces.generateAndAddPiece(var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.minY + var5, this.boundingBox.minZ + var4, var6, this.getComponentType());
            }
         }

         return null;
      }

      protected StructureComponent getNextComponentX(StructureStrongholdPieces.Stairs2 var1, List var2, Random var3, int var4, int var5) {
         EnumFacing var6 = this.getCoordBaseMode();
         if (var6 != null) {
            switch(var6) {
            case NORTH:
               return StructureStrongholdPieces.generateAndAddPiece(var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.minY + var4, this.boundingBox.minZ + var5, EnumFacing.WEST, this.getComponentType());
            case SOUTH:
               return StructureStrongholdPieces.generateAndAddPiece(var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.minY + var4, this.boundingBox.minZ + var5, EnumFacing.WEST, this.getComponentType());
            case WEST:
               return StructureStrongholdPieces.generateAndAddPiece(var1, var2, var3, this.boundingBox.minX + var5, this.boundingBox.minY + var4, this.boundingBox.minZ - 1, EnumFacing.NORTH, this.getComponentType());
            case EAST:
               return StructureStrongholdPieces.generateAndAddPiece(var1, var2, var3, this.boundingBox.minX + var5, this.boundingBox.minY + var4, this.boundingBox.minZ - 1, EnumFacing.NORTH, this.getComponentType());
            }
         }

         return null;
      }

      protected StructureComponent getNextComponentZ(StructureStrongholdPieces.Stairs2 var1, List var2, Random var3, int var4, int var5) {
         EnumFacing var6 = this.getCoordBaseMode();
         if (var6 != null) {
            switch(var6) {
            case NORTH:
               return StructureStrongholdPieces.generateAndAddPiece(var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.minY + var4, this.boundingBox.minZ + var5, EnumFacing.EAST, this.getComponentType());
            case SOUTH:
               return StructureStrongholdPieces.generateAndAddPiece(var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.minY + var4, this.boundingBox.minZ + var5, EnumFacing.EAST, this.getComponentType());
            case WEST:
               return StructureStrongholdPieces.generateAndAddPiece(var1, var2, var3, this.boundingBox.minX + var5, this.boundingBox.minY + var4, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, this.getComponentType());
            case EAST:
               return StructureStrongholdPieces.generateAndAddPiece(var1, var2, var3, this.boundingBox.minX + var5, this.boundingBox.minY + var4, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, this.getComponentType());
            }
         }

         return null;
      }

      protected static boolean canStrongholdGoDeeper(StructureBoundingBox var0) {
         return var0 != null && var0.minY > 10;
      }

      public static enum Door {
         OPENING,
         WOOD_DOOR,
         GRATES,
         IRON_DOOR;
      }
   }
}
