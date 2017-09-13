package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockPrismarine;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StructureOceanMonumentPieces {
   public static void registerOceanMonumentPieces() {
      MapGenStructureIO.registerStructureComponent(StructureOceanMonumentPieces.MonumentBuilding.class, "OMB");
      MapGenStructureIO.registerStructureComponent(StructureOceanMonumentPieces.MonumentCoreRoom.class, "OMCR");
      MapGenStructureIO.registerStructureComponent(StructureOceanMonumentPieces.DoubleXRoom.class, "OMDXR");
      MapGenStructureIO.registerStructureComponent(StructureOceanMonumentPieces.DoubleXYRoom.class, "OMDXYR");
      MapGenStructureIO.registerStructureComponent(StructureOceanMonumentPieces.DoubleYRoom.class, "OMDYR");
      MapGenStructureIO.registerStructureComponent(StructureOceanMonumentPieces.DoubleYZRoom.class, "OMDYZR");
      MapGenStructureIO.registerStructureComponent(StructureOceanMonumentPieces.DoubleZRoom.class, "OMDZR");
      MapGenStructureIO.registerStructureComponent(StructureOceanMonumentPieces.EntryRoom.class, "OMEntry");
      MapGenStructureIO.registerStructureComponent(StructureOceanMonumentPieces.Penthouse.class, "OMPenthouse");
      MapGenStructureIO.registerStructureComponent(StructureOceanMonumentPieces.SimpleRoom.class, "OMSimple");
      MapGenStructureIO.registerStructureComponent(StructureOceanMonumentPieces.SimpleTopRoom.class, "OMSimpleT");
   }

   public static class DoubleXRoom extends StructureOceanMonumentPieces.Piece {
      public DoubleXRoom() {
      }

      public DoubleXRoom(EnumFacing var1, StructureOceanMonumentPieces.RoomDefinition var2, Random var3) {
         super(1, var1, var2, 2, 1, 1);
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         StructureOceanMonumentPieces.RoomDefinition var4 = this.roomDefinition.connections[EnumFacing.EAST.getIndex()];
         StructureOceanMonumentPieces.RoomDefinition var5 = this.roomDefinition;
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(var1, var3, 8, 0, var4.hasOpening[EnumFacing.DOWN.getIndex()]);
            this.generateDefaultFloor(var1, var3, 0, 0, var5.hasOpening[EnumFacing.DOWN.getIndex()]);
         }

         if (var5.connections[EnumFacing.UP.getIndex()] == null) {
            this.generateBoxOnFillOnly(var1, var3, 1, 4, 1, 7, 4, 6, ROUGH_PRISMARINE);
         }

         if (var4.connections[EnumFacing.UP.getIndex()] == null) {
            this.generateBoxOnFillOnly(var1, var3, 8, 4, 1, 14, 4, 6, ROUGH_PRISMARINE);
         }

         this.fillWithBlocks(var1, var3, 0, 3, 0, 0, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 15, 3, 0, 15, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 3, 0, 15, 3, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 3, 7, 14, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 0, 2, 0, 0, 2, 7, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 15, 2, 0, 15, 2, 7, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 2, 0, 15, 2, 0, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 2, 7, 14, 2, 7, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 0, 1, 0, 0, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 15, 1, 0, 15, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 1, 0, 15, 1, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 1, 7, 14, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 5, 1, 0, 10, 1, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 6, 2, 0, 9, 2, 3, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 5, 3, 0, 10, 3, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.setBlockState(var1, SEA_LANTERN, 6, 2, 3, var3);
         this.setBlockState(var1, SEA_LANTERN, 9, 2, 3, var3);
         if (var5.hasOpening[EnumFacing.SOUTH.getIndex()]) {
            this.generateWaterBox(var1, var3, 3, 1, 0, 4, 2, 0, false);
         }

         if (var5.hasOpening[EnumFacing.NORTH.getIndex()]) {
            this.generateWaterBox(var1, var3, 3, 1, 7, 4, 2, 7, false);
         }

         if (var5.hasOpening[EnumFacing.WEST.getIndex()]) {
            this.generateWaterBox(var1, var3, 0, 1, 3, 0, 2, 4, false);
         }

         if (var4.hasOpening[EnumFacing.SOUTH.getIndex()]) {
            this.generateWaterBox(var1, var3, 11, 1, 0, 12, 2, 0, false);
         }

         if (var4.hasOpening[EnumFacing.NORTH.getIndex()]) {
            this.generateWaterBox(var1, var3, 11, 1, 7, 12, 2, 7, false);
         }

         if (var4.hasOpening[EnumFacing.EAST.getIndex()]) {
            this.generateWaterBox(var1, var3, 15, 1, 3, 15, 2, 4, false);
         }

         return true;
      }
   }

   public static class DoubleXYRoom extends StructureOceanMonumentPieces.Piece {
      public DoubleXYRoom() {
      }

      public DoubleXYRoom(EnumFacing var1, StructureOceanMonumentPieces.RoomDefinition var2, Random var3) {
         super(1, var1, var2, 2, 2, 1);
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         StructureOceanMonumentPieces.RoomDefinition var4 = this.roomDefinition.connections[EnumFacing.EAST.getIndex()];
         StructureOceanMonumentPieces.RoomDefinition var5 = this.roomDefinition;
         StructureOceanMonumentPieces.RoomDefinition var6 = var5.connections[EnumFacing.UP.getIndex()];
         StructureOceanMonumentPieces.RoomDefinition var7 = var4.connections[EnumFacing.UP.getIndex()];
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(var1, var3, 8, 0, var4.hasOpening[EnumFacing.DOWN.getIndex()]);
            this.generateDefaultFloor(var1, var3, 0, 0, var5.hasOpening[EnumFacing.DOWN.getIndex()]);
         }

         if (var6.connections[EnumFacing.UP.getIndex()] == null) {
            this.generateBoxOnFillOnly(var1, var3, 1, 8, 1, 7, 8, 6, ROUGH_PRISMARINE);
         }

         if (var7.connections[EnumFacing.UP.getIndex()] == null) {
            this.generateBoxOnFillOnly(var1, var3, 8, 8, 1, 14, 8, 6, ROUGH_PRISMARINE);
         }

         for(int var8 = 1; var8 <= 7; ++var8) {
            IBlockState var9 = BRICKS_PRISMARINE;
            if (var8 == 2 || var8 == 6) {
               var9 = ROUGH_PRISMARINE;
            }

            this.fillWithBlocks(var1, var3, 0, var8, 0, 0, var8, 7, var9, var9, false);
            this.fillWithBlocks(var1, var3, 15, var8, 0, 15, var8, 7, var9, var9, false);
            this.fillWithBlocks(var1, var3, 1, var8, 0, 15, var8, 0, var9, var9, false);
            this.fillWithBlocks(var1, var3, 1, var8, 7, 14, var8, 7, var9, var9, false);
         }

         this.fillWithBlocks(var1, var3, 2, 1, 3, 2, 7, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 3, 1, 2, 4, 7, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 3, 1, 5, 4, 7, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 13, 1, 3, 13, 7, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 11, 1, 2, 12, 7, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 11, 1, 5, 12, 7, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 5, 1, 3, 5, 3, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 10, 1, 3, 10, 3, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 5, 7, 2, 10, 7, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 5, 5, 2, 5, 7, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 10, 5, 2, 10, 7, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 5, 5, 5, 5, 7, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 10, 5, 5, 10, 7, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.setBlockState(var1, BRICKS_PRISMARINE, 6, 6, 2, var3);
         this.setBlockState(var1, BRICKS_PRISMARINE, 9, 6, 2, var3);
         this.setBlockState(var1, BRICKS_PRISMARINE, 6, 6, 5, var3);
         this.setBlockState(var1, BRICKS_PRISMARINE, 9, 6, 5, var3);
         this.fillWithBlocks(var1, var3, 5, 4, 3, 6, 4, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 9, 4, 3, 10, 4, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.setBlockState(var1, SEA_LANTERN, 5, 4, 2, var3);
         this.setBlockState(var1, SEA_LANTERN, 5, 4, 5, var3);
         this.setBlockState(var1, SEA_LANTERN, 10, 4, 2, var3);
         this.setBlockState(var1, SEA_LANTERN, 10, 4, 5, var3);
         if (var5.hasOpening[EnumFacing.SOUTH.getIndex()]) {
            this.generateWaterBox(var1, var3, 3, 1, 0, 4, 2, 0, false);
         }

         if (var5.hasOpening[EnumFacing.NORTH.getIndex()]) {
            this.generateWaterBox(var1, var3, 3, 1, 7, 4, 2, 7, false);
         }

         if (var5.hasOpening[EnumFacing.WEST.getIndex()]) {
            this.generateWaterBox(var1, var3, 0, 1, 3, 0, 2, 4, false);
         }

         if (var4.hasOpening[EnumFacing.SOUTH.getIndex()]) {
            this.generateWaterBox(var1, var3, 11, 1, 0, 12, 2, 0, false);
         }

         if (var4.hasOpening[EnumFacing.NORTH.getIndex()]) {
            this.generateWaterBox(var1, var3, 11, 1, 7, 12, 2, 7, false);
         }

         if (var4.hasOpening[EnumFacing.EAST.getIndex()]) {
            this.generateWaterBox(var1, var3, 15, 1, 3, 15, 2, 4, false);
         }

         if (var6.hasOpening[EnumFacing.SOUTH.getIndex()]) {
            this.generateWaterBox(var1, var3, 3, 5, 0, 4, 6, 0, false);
         }

         if (var6.hasOpening[EnumFacing.NORTH.getIndex()]) {
            this.generateWaterBox(var1, var3, 3, 5, 7, 4, 6, 7, false);
         }

         if (var6.hasOpening[EnumFacing.WEST.getIndex()]) {
            this.generateWaterBox(var1, var3, 0, 5, 3, 0, 6, 4, false);
         }

         if (var7.hasOpening[EnumFacing.SOUTH.getIndex()]) {
            this.generateWaterBox(var1, var3, 11, 5, 0, 12, 6, 0, false);
         }

         if (var7.hasOpening[EnumFacing.NORTH.getIndex()]) {
            this.generateWaterBox(var1, var3, 11, 5, 7, 12, 6, 7, false);
         }

         if (var7.hasOpening[EnumFacing.EAST.getIndex()]) {
            this.generateWaterBox(var1, var3, 15, 5, 3, 15, 6, 4, false);
         }

         return true;
      }
   }

   public static class DoubleYRoom extends StructureOceanMonumentPieces.Piece {
      public DoubleYRoom() {
      }

      public DoubleYRoom(EnumFacing var1, StructureOceanMonumentPieces.RoomDefinition var2, Random var3) {
         super(1, var1, var2, 1, 2, 1);
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(var1, var3, 0, 0, this.roomDefinition.hasOpening[EnumFacing.DOWN.getIndex()]);
         }

         StructureOceanMonumentPieces.RoomDefinition var4 = this.roomDefinition.connections[EnumFacing.UP.getIndex()];
         if (var4.connections[EnumFacing.UP.getIndex()] == null) {
            this.generateBoxOnFillOnly(var1, var3, 1, 8, 1, 6, 8, 6, ROUGH_PRISMARINE);
         }

         this.fillWithBlocks(var1, var3, 0, 4, 0, 0, 4, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 7, 4, 0, 7, 4, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 4, 0, 6, 4, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 4, 7, 6, 4, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 2, 4, 1, 2, 4, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 4, 2, 1, 4, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 5, 4, 1, 5, 4, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 6, 4, 2, 6, 4, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 2, 4, 5, 2, 4, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 4, 5, 1, 4, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 5, 4, 5, 5, 4, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 6, 4, 5, 6, 4, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         StructureOceanMonumentPieces.RoomDefinition var5 = this.roomDefinition;

         for(int var6 = 1; var6 <= 5; var6 += 4) {
            byte var7 = 0;
            if (var5.hasOpening[EnumFacing.SOUTH.getIndex()]) {
               this.fillWithBlocks(var1, var3, 2, var6, var7, 2, var6 + 2, var7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 5, var6, var7, 5, var6 + 2, var7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 3, var6 + 2, var7, 4, var6 + 2, var7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            } else {
               this.fillWithBlocks(var1, var3, 0, var6, var7, 7, var6 + 2, var7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 0, var6 + 1, var7, 7, var6 + 1, var7, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            }

            var7 = 7;
            if (var5.hasOpening[EnumFacing.NORTH.getIndex()]) {
               this.fillWithBlocks(var1, var3, 2, var6, var7, 2, var6 + 2, var7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 5, var6, var7, 5, var6 + 2, var7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 3, var6 + 2, var7, 4, var6 + 2, var7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            } else {
               this.fillWithBlocks(var1, var3, 0, var6, var7, 7, var6 + 2, var7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 0, var6 + 1, var7, 7, var6 + 1, var7, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            }

            byte var8 = 0;
            if (var5.hasOpening[EnumFacing.WEST.getIndex()]) {
               this.fillWithBlocks(var1, var3, var8, var6, 2, var8, var6 + 2, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, var8, var6, 5, var8, var6 + 2, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, var8, var6 + 2, 3, var8, var6 + 2, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            } else {
               this.fillWithBlocks(var1, var3, var8, var6, 0, var8, var6 + 2, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, var8, var6 + 1, 0, var8, var6 + 1, 7, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            }

            var8 = 7;
            if (var5.hasOpening[EnumFacing.EAST.getIndex()]) {
               this.fillWithBlocks(var1, var3, var8, var6, 2, var8, var6 + 2, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, var8, var6, 5, var8, var6 + 2, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, var8, var6 + 2, 3, var8, var6 + 2, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            } else {
               this.fillWithBlocks(var1, var3, var8, var6, 0, var8, var6 + 2, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, var8, var6 + 1, 0, var8, var6 + 1, 7, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            }

            var5 = var4;
         }

         return true;
      }
   }

   public static class DoubleYZRoom extends StructureOceanMonumentPieces.Piece {
      public DoubleYZRoom() {
      }

      public DoubleYZRoom(EnumFacing var1, StructureOceanMonumentPieces.RoomDefinition var2, Random var3) {
         super(1, var1, var2, 1, 2, 2);
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         StructureOceanMonumentPieces.RoomDefinition var4 = this.roomDefinition.connections[EnumFacing.NORTH.getIndex()];
         StructureOceanMonumentPieces.RoomDefinition var5 = this.roomDefinition;
         StructureOceanMonumentPieces.RoomDefinition var6 = var4.connections[EnumFacing.UP.getIndex()];
         StructureOceanMonumentPieces.RoomDefinition var7 = var5.connections[EnumFacing.UP.getIndex()];
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(var1, var3, 0, 8, var4.hasOpening[EnumFacing.DOWN.getIndex()]);
            this.generateDefaultFloor(var1, var3, 0, 0, var5.hasOpening[EnumFacing.DOWN.getIndex()]);
         }

         if (var7.connections[EnumFacing.UP.getIndex()] == null) {
            this.generateBoxOnFillOnly(var1, var3, 1, 8, 1, 6, 8, 7, ROUGH_PRISMARINE);
         }

         if (var6.connections[EnumFacing.UP.getIndex()] == null) {
            this.generateBoxOnFillOnly(var1, var3, 1, 8, 8, 6, 8, 14, ROUGH_PRISMARINE);
         }

         for(int var8 = 1; var8 <= 7; ++var8) {
            IBlockState var9 = BRICKS_PRISMARINE;
            if (var8 == 2 || var8 == 6) {
               var9 = ROUGH_PRISMARINE;
            }

            this.fillWithBlocks(var1, var3, 0, var8, 0, 0, var8, 15, var9, var9, false);
            this.fillWithBlocks(var1, var3, 7, var8, 0, 7, var8, 15, var9, var9, false);
            this.fillWithBlocks(var1, var3, 1, var8, 0, 6, var8, 0, var9, var9, false);
            this.fillWithBlocks(var1, var3, 1, var8, 15, 6, var8, 15, var9, var9, false);
         }

         for(int var10 = 1; var10 <= 7; ++var10) {
            IBlockState var11 = DARK_PRISMARINE;
            if (var10 == 2 || var10 == 6) {
               var11 = SEA_LANTERN;
            }

            this.fillWithBlocks(var1, var3, 3, var10, 7, 4, var10, 8, var11, var11, false);
         }

         if (var5.hasOpening[EnumFacing.SOUTH.getIndex()]) {
            this.generateWaterBox(var1, var3, 3, 1, 0, 4, 2, 0, false);
         }

         if (var5.hasOpening[EnumFacing.EAST.getIndex()]) {
            this.generateWaterBox(var1, var3, 7, 1, 3, 7, 2, 4, false);
         }

         if (var5.hasOpening[EnumFacing.WEST.getIndex()]) {
            this.generateWaterBox(var1, var3, 0, 1, 3, 0, 2, 4, false);
         }

         if (var4.hasOpening[EnumFacing.NORTH.getIndex()]) {
            this.generateWaterBox(var1, var3, 3, 1, 15, 4, 2, 15, false);
         }

         if (var4.hasOpening[EnumFacing.WEST.getIndex()]) {
            this.generateWaterBox(var1, var3, 0, 1, 11, 0, 2, 12, false);
         }

         if (var4.hasOpening[EnumFacing.EAST.getIndex()]) {
            this.generateWaterBox(var1, var3, 7, 1, 11, 7, 2, 12, false);
         }

         if (var7.hasOpening[EnumFacing.SOUTH.getIndex()]) {
            this.generateWaterBox(var1, var3, 3, 5, 0, 4, 6, 0, false);
         }

         if (var7.hasOpening[EnumFacing.EAST.getIndex()]) {
            this.generateWaterBox(var1, var3, 7, 5, 3, 7, 6, 4, false);
            this.fillWithBlocks(var1, var3, 5, 4, 2, 6, 4, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 6, 1, 2, 6, 3, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 6, 1, 5, 6, 3, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         }

         if (var7.hasOpening[EnumFacing.WEST.getIndex()]) {
            this.generateWaterBox(var1, var3, 0, 5, 3, 0, 6, 4, false);
            this.fillWithBlocks(var1, var3, 1, 4, 2, 2, 4, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 1, 1, 2, 1, 3, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 1, 1, 5, 1, 3, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         }

         if (var6.hasOpening[EnumFacing.NORTH.getIndex()]) {
            this.generateWaterBox(var1, var3, 3, 5, 15, 4, 6, 15, false);
         }

         if (var6.hasOpening[EnumFacing.WEST.getIndex()]) {
            this.generateWaterBox(var1, var3, 0, 5, 11, 0, 6, 12, false);
            this.fillWithBlocks(var1, var3, 1, 4, 10, 2, 4, 13, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 1, 1, 10, 1, 3, 10, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 1, 1, 13, 1, 3, 13, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         }

         if (var6.hasOpening[EnumFacing.EAST.getIndex()]) {
            this.generateWaterBox(var1, var3, 7, 5, 11, 7, 6, 12, false);
            this.fillWithBlocks(var1, var3, 5, 4, 10, 6, 4, 13, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 6, 1, 10, 6, 3, 10, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 6, 1, 13, 6, 3, 13, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         }

         return true;
      }
   }

   public static class DoubleZRoom extends StructureOceanMonumentPieces.Piece {
      public DoubleZRoom() {
      }

      public DoubleZRoom(EnumFacing var1, StructureOceanMonumentPieces.RoomDefinition var2, Random var3) {
         super(1, var1, var2, 1, 1, 2);
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         StructureOceanMonumentPieces.RoomDefinition var4 = this.roomDefinition.connections[EnumFacing.NORTH.getIndex()];
         StructureOceanMonumentPieces.RoomDefinition var5 = this.roomDefinition;
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(var1, var3, 0, 8, var4.hasOpening[EnumFacing.DOWN.getIndex()]);
            this.generateDefaultFloor(var1, var3, 0, 0, var5.hasOpening[EnumFacing.DOWN.getIndex()]);
         }

         if (var5.connections[EnumFacing.UP.getIndex()] == null) {
            this.generateBoxOnFillOnly(var1, var3, 1, 4, 1, 6, 4, 7, ROUGH_PRISMARINE);
         }

         if (var4.connections[EnumFacing.UP.getIndex()] == null) {
            this.generateBoxOnFillOnly(var1, var3, 1, 4, 8, 6, 4, 14, ROUGH_PRISMARINE);
         }

         this.fillWithBlocks(var1, var3, 0, 3, 0, 0, 3, 15, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 7, 3, 0, 7, 3, 15, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 3, 0, 7, 3, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 3, 15, 6, 3, 15, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 0, 2, 0, 0, 2, 15, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 7, 2, 0, 7, 2, 15, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 2, 0, 7, 2, 0, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 2, 15, 6, 2, 15, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 0, 1, 0, 0, 1, 15, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 7, 1, 0, 7, 1, 15, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 1, 0, 7, 1, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 1, 15, 6, 1, 15, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 1, 1, 1, 1, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 6, 1, 1, 6, 1, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 3, 1, 1, 3, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 6, 3, 1, 6, 3, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 1, 13, 1, 1, 14, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 6, 1, 13, 6, 1, 14, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 3, 13, 1, 3, 14, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 6, 3, 13, 6, 3, 14, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 2, 1, 6, 2, 3, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 5, 1, 6, 5, 3, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 2, 1, 9, 2, 3, 9, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 5, 1, 9, 5, 3, 9, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 3, 2, 6, 4, 2, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 3, 2, 9, 4, 2, 9, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 2, 2, 7, 2, 2, 8, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 5, 2, 7, 5, 2, 8, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.setBlockState(var1, SEA_LANTERN, 2, 2, 5, var3);
         this.setBlockState(var1, SEA_LANTERN, 5, 2, 5, var3);
         this.setBlockState(var1, SEA_LANTERN, 2, 2, 10, var3);
         this.setBlockState(var1, SEA_LANTERN, 5, 2, 10, var3);
         this.setBlockState(var1, BRICKS_PRISMARINE, 2, 3, 5, var3);
         this.setBlockState(var1, BRICKS_PRISMARINE, 5, 3, 5, var3);
         this.setBlockState(var1, BRICKS_PRISMARINE, 2, 3, 10, var3);
         this.setBlockState(var1, BRICKS_PRISMARINE, 5, 3, 10, var3);
         if (var5.hasOpening[EnumFacing.SOUTH.getIndex()]) {
            this.generateWaterBox(var1, var3, 3, 1, 0, 4, 2, 0, false);
         }

         if (var5.hasOpening[EnumFacing.EAST.getIndex()]) {
            this.generateWaterBox(var1, var3, 7, 1, 3, 7, 2, 4, false);
         }

         if (var5.hasOpening[EnumFacing.WEST.getIndex()]) {
            this.generateWaterBox(var1, var3, 0, 1, 3, 0, 2, 4, false);
         }

         if (var4.hasOpening[EnumFacing.NORTH.getIndex()]) {
            this.generateWaterBox(var1, var3, 3, 1, 15, 4, 2, 15, false);
         }

         if (var4.hasOpening[EnumFacing.WEST.getIndex()]) {
            this.generateWaterBox(var1, var3, 0, 1, 11, 0, 2, 12, false);
         }

         if (var4.hasOpening[EnumFacing.EAST.getIndex()]) {
            this.generateWaterBox(var1, var3, 7, 1, 11, 7, 2, 12, false);
         }

         return true;
      }
   }

   public static class EntryRoom extends StructureOceanMonumentPieces.Piece {
      public EntryRoom() {
      }

      public EntryRoom(EnumFacing var1, StructureOceanMonumentPieces.RoomDefinition var2) {
         super(1, var1, var2, 1, 1, 1);
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         this.fillWithBlocks(var1, var3, 0, 3, 0, 2, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 5, 3, 0, 7, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 0, 2, 0, 1, 2, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 6, 2, 0, 7, 2, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 0, 1, 0, 0, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 7, 1, 0, 7, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 0, 1, 7, 7, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 1, 0, 2, 3, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 5, 1, 0, 6, 3, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         if (this.roomDefinition.hasOpening[EnumFacing.NORTH.getIndex()]) {
            this.generateWaterBox(var1, var3, 3, 1, 7, 4, 2, 7, false);
         }

         if (this.roomDefinition.hasOpening[EnumFacing.WEST.getIndex()]) {
            this.generateWaterBox(var1, var3, 0, 1, 3, 1, 2, 4, false);
         }

         if (this.roomDefinition.hasOpening[EnumFacing.EAST.getIndex()]) {
            this.generateWaterBox(var1, var3, 6, 1, 3, 7, 2, 4, false);
         }

         return true;
      }
   }

   static class FitSimpleRoomHelper implements StructureOceanMonumentPieces.MonumentRoomFitHelper {
      private FitSimpleRoomHelper() {
      }

      public boolean fits(StructureOceanMonumentPieces.RoomDefinition var1) {
         return true;
      }

      public StructureOceanMonumentPieces.Piece create(EnumFacing var1, StructureOceanMonumentPieces.RoomDefinition var2, Random var3) {
         var2.claimed = true;
         return new StructureOceanMonumentPieces.SimpleRoom(var1, var2, var3);
      }
   }

   static class FitSimpleRoomTopHelper implements StructureOceanMonumentPieces.MonumentRoomFitHelper {
      private FitSimpleRoomTopHelper() {
      }

      public boolean fits(StructureOceanMonumentPieces.RoomDefinition var1) {
         return !var1.hasOpening[EnumFacing.WEST.getIndex()] && !var1.hasOpening[EnumFacing.EAST.getIndex()] && !var1.hasOpening[EnumFacing.NORTH.getIndex()] && !var1.hasOpening[EnumFacing.SOUTH.getIndex()] && !var1.hasOpening[EnumFacing.UP.getIndex()];
      }

      public StructureOceanMonumentPieces.Piece create(EnumFacing var1, StructureOceanMonumentPieces.RoomDefinition var2, Random var3) {
         var2.claimed = true;
         return new StructureOceanMonumentPieces.SimpleTopRoom(var1, var2, var3);
      }
   }

   public static class MonumentBuilding extends StructureOceanMonumentPieces.Piece {
      private StructureOceanMonumentPieces.RoomDefinition sourceRoom;
      private StructureOceanMonumentPieces.RoomDefinition coreRoom;
      private final List childPieces = Lists.newArrayList();

      public MonumentBuilding() {
      }

      public MonumentBuilding(Random var1, int var2, int var3, EnumFacing var4) {
         super(0);
         this.setCoordBaseMode(var4);
         EnumFacing var5 = this.getCoordBaseMode();
         if (var5.getAxis() == EnumFacing.Axis.Z) {
            this.boundingBox = new StructureBoundingBox(var2, 39, var3, var2 + 58 - 1, 61, var3 + 58 - 1);
         } else {
            this.boundingBox = new StructureBoundingBox(var2, 39, var3, var2 + 58 - 1, 61, var3 + 58 - 1);
         }

         List var6 = this.generateRoomGraph(var1);
         this.sourceRoom.claimed = true;
         this.childPieces.add(new StructureOceanMonumentPieces.EntryRoom(var5, this.sourceRoom));
         this.childPieces.add(new StructureOceanMonumentPieces.MonumentCoreRoom(var5, this.coreRoom, var1));
         ArrayList var7 = Lists.newArrayList();
         var7.add(new StructureOceanMonumentPieces.XYDoubleRoomFitHelper());
         var7.add(new StructureOceanMonumentPieces.YZDoubleRoomFitHelper());
         var7.add(new StructureOceanMonumentPieces.ZDoubleRoomFitHelper());
         var7.add(new StructureOceanMonumentPieces.XDoubleRoomFitHelper());
         var7.add(new StructureOceanMonumentPieces.YDoubleRoomFitHelper());
         var7.add(new StructureOceanMonumentPieces.FitSimpleRoomTopHelper());
         var7.add(new StructureOceanMonumentPieces.FitSimpleRoomHelper());

         label47:
         for(StructureOceanMonumentPieces.RoomDefinition var9 : var6) {
            if (!var9.claimed && !var9.isSpecial()) {
               Iterator var10 = var7.iterator();

               StructureOceanMonumentPieces.MonumentRoomFitHelper var11;
               while(true) {
                  if (!var10.hasNext()) {
                     continue label47;
                  }

                  var11 = (StructureOceanMonumentPieces.MonumentRoomFitHelper)var10.next();
                  if (var11.fits(var9)) {
                     break;
                  }
               }

               this.childPieces.add(var11.create(var5, var9, var1));
            }
         }

         int var15 = this.boundingBox.minY;
         int var16 = this.getXWithOffset(9, 22);
         int var17 = this.getZWithOffset(9, 22);

         for(StructureOceanMonumentPieces.Piece var12 : this.childPieces) {
            var12.getBoundingBox().offset(var16, var15, var17);
         }

         StructureBoundingBox var19 = StructureBoundingBox.createProper(this.getXWithOffset(1, 1), this.getYWithOffset(1), this.getZWithOffset(1, 1), this.getXWithOffset(23, 21), this.getYWithOffset(8), this.getZWithOffset(23, 21));
         StructureBoundingBox var20 = StructureBoundingBox.createProper(this.getXWithOffset(34, 1), this.getYWithOffset(1), this.getZWithOffset(34, 1), this.getXWithOffset(56, 21), this.getYWithOffset(8), this.getZWithOffset(56, 21));
         StructureBoundingBox var13 = StructureBoundingBox.createProper(this.getXWithOffset(22, 22), this.getYWithOffset(13), this.getZWithOffset(22, 22), this.getXWithOffset(35, 35), this.getYWithOffset(17), this.getZWithOffset(35, 35));
         int var14 = var1.nextInt();
         this.childPieces.add(new StructureOceanMonumentPieces.WingRoom(var5, var19, var14++));
         this.childPieces.add(new StructureOceanMonumentPieces.WingRoom(var5, var20, var14++));
         this.childPieces.add(new StructureOceanMonumentPieces.Penthouse(var5, var13));
      }

      private List generateRoomGraph(Random var1) {
         StructureOceanMonumentPieces.RoomDefinition[] var2 = new StructureOceanMonumentPieces.RoomDefinition[75];

         for(int var3 = 0; var3 < 5; ++var3) {
            for(int var4 = 0; var4 < 4; ++var4) {
               boolean var5 = false;
               int var6 = getRoomIndex(var3, 0, var4);
               var2[var6] = new StructureOceanMonumentPieces.RoomDefinition(var6);
            }
         }

         for(int var15 = 0; var15 < 5; ++var15) {
            for(int var19 = 0; var19 < 4; ++var19) {
               boolean var23 = true;
               int var27 = getRoomIndex(var15, 1, var19);
               var2[var27] = new StructureOceanMonumentPieces.RoomDefinition(var27);
            }
         }

         for(int var16 = 1; var16 < 4; ++var16) {
            for(int var20 = 0; var20 < 2; ++var20) {
               boolean var24 = true;
               int var28 = getRoomIndex(var16, 2, var20);
               var2[var28] = new StructureOceanMonumentPieces.RoomDefinition(var28);
            }
         }

         this.sourceRoom = var2[GRIDROOM_SOURCE_INDEX];

         for(int var17 = 0; var17 < 5; ++var17) {
            for(int var21 = 0; var21 < 5; ++var21) {
               for(int var25 = 0; var25 < 3; ++var25) {
                  int var29 = getRoomIndex(var17, var25, var21);
                  if (var2[var29] != null) {
                     for(EnumFacing var10 : EnumFacing.values()) {
                        int var11 = var17 + var10.getFrontOffsetX();
                        int var12 = var25 + var10.getFrontOffsetY();
                        int var13 = var21 + var10.getFrontOffsetZ();
                        if (var11 >= 0 && var11 < 5 && var13 >= 0 && var13 < 5 && var12 >= 0 && var12 < 3) {
                           int var14 = getRoomIndex(var11, var12, var13);
                           if (var2[var14] != null) {
                              if (var13 == var21) {
                                 var2[var29].setConnection(var10, var2[var14]);
                              } else {
                                 var2[var29].setConnection(var10.getOpposite(), var2[var14]);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         StructureOceanMonumentPieces.RoomDefinition var18 = new StructureOceanMonumentPieces.RoomDefinition(1003);
         StructureOceanMonumentPieces.RoomDefinition var22 = new StructureOceanMonumentPieces.RoomDefinition(1001);
         StructureOceanMonumentPieces.RoomDefinition var26 = new StructureOceanMonumentPieces.RoomDefinition(1002);
         var2[GRIDROOM_TOP_CONNECT_INDEX].setConnection(EnumFacing.UP, var18);
         var2[GRIDROOM_LEFTWING_CONNECT_INDEX].setConnection(EnumFacing.SOUTH, var22);
         var2[GRIDROOM_RIGHTWING_CONNECT_INDEX].setConnection(EnumFacing.SOUTH, var26);
         var18.claimed = true;
         var22.claimed = true;
         var26.claimed = true;
         this.sourceRoom.isSource = true;
         this.coreRoom = var2[getRoomIndex(var1.nextInt(4), 0, 2)];
         this.coreRoom.claimed = true;
         this.coreRoom.connections[EnumFacing.EAST.getIndex()].claimed = true;
         this.coreRoom.connections[EnumFacing.NORTH.getIndex()].claimed = true;
         this.coreRoom.connections[EnumFacing.EAST.getIndex()].connections[EnumFacing.NORTH.getIndex()].claimed = true;
         this.coreRoom.connections[EnumFacing.UP.getIndex()].claimed = true;
         this.coreRoom.connections[EnumFacing.EAST.getIndex()].connections[EnumFacing.UP.getIndex()].claimed = true;
         this.coreRoom.connections[EnumFacing.NORTH.getIndex()].connections[EnumFacing.UP.getIndex()].claimed = true;
         this.coreRoom.connections[EnumFacing.EAST.getIndex()].connections[EnumFacing.NORTH.getIndex()].connections[EnumFacing.UP.getIndex()].claimed = true;
         ArrayList var30 = Lists.newArrayList();

         for(StructureOceanMonumentPieces.RoomDefinition var37 : var2) {
            if (var37 != null) {
               var37.updateOpenings();
               var30.add(var37);
            }
         }

         var18.updateOpenings();
         Collections.shuffle(var30, var1);
         int var32 = 1;

         for(StructureOceanMonumentPieces.RoomDefinition var36 : var30) {
            int var38 = 0;
            int var39 = 0;

            while(var38 < 2 && var39 < 5) {
               ++var39;
               int var40 = var1.nextInt(6);
               if (var36.hasOpening[var40]) {
                  int var41 = EnumFacing.getFront(var40).getOpposite().getIndex();
                  var36.hasOpening[var40] = false;
                  var36.connections[var40].hasOpening[var41] = false;
                  if (var36.findSource(var32++) && var36.connections[var40].findSource(var32++)) {
                     ++var38;
                  } else {
                     var36.hasOpening[var40] = true;
                     var36.connections[var40].hasOpening[var41] = true;
                  }
               }
            }
         }

         var30.add(var18);
         var30.add(var22);
         var30.add(var26);
         return var30;
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         int var4 = Math.max(var1.getSeaLevel(), 64) - this.boundingBox.minY;
         this.generateWaterBox(var1, var3, 0, 0, 0, 58, var4, 58, false);
         this.generateWing(false, 0, var1, var2, var3);
         this.generateWing(true, 33, var1, var2, var3);
         this.generateEntranceArchs(var1, var2, var3);
         this.generateEntranceWall(var1, var2, var3);
         this.generateRoofPiece(var1, var2, var3);
         this.generateLowerWall(var1, var2, var3);
         this.generateMiddleWall(var1, var2, var3);
         this.generateUpperWall(var1, var2, var3);

         for(int var5 = 0; var5 < 7; ++var5) {
            int var6 = 0;

            while(var6 < 7) {
               if (var6 == 0 && var5 == 3) {
                  var6 = 6;
               }

               int var7 = var5 * 9;
               int var8 = var6 * 9;

               for(int var9 = 0; var9 < 4; ++var9) {
                  for(int var10 = 0; var10 < 4; ++var10) {
                     this.setBlockState(var1, BRICKS_PRISMARINE, var7 + var9, 0, var8 + var10, var3);
                     this.replaceAirAndLiquidDownwards(var1, BRICKS_PRISMARINE, var7 + var9, -1, var8 + var10, var3);
                  }
               }

               if (var5 != 0 && var5 != 6) {
                  var6 += 6;
               } else {
                  ++var6;
               }
            }
         }

         for(int var11 = 0; var11 < 5; ++var11) {
            this.generateWaterBox(var1, var3, -1 - var11, 0 + var11 * 2, -1 - var11, -1 - var11, 23, 58 + var11, false);
            this.generateWaterBox(var1, var3, 58 + var11, 0 + var11 * 2, -1 - var11, 58 + var11, 23, 58 + var11, false);
            this.generateWaterBox(var1, var3, 0 - var11, 0 + var11 * 2, -1 - var11, 57 + var11, 23, -1 - var11, false);
            this.generateWaterBox(var1, var3, 0 - var11, 0 + var11 * 2, 58 + var11, 57 + var11, 23, 58 + var11, false);
         }

         for(StructureOceanMonumentPieces.Piece var13 : this.childPieces) {
            if (var13.getBoundingBox().intersectsWith(var3)) {
               var13.addComponentParts(var1, var2, var3);
            }
         }

         return true;
      }

      private void generateWing(boolean var1, int var2, World var3, Random var4, StructureBoundingBox var5) {
         boolean var6 = true;
         if (this.doesChunkIntersect(var5, var2, 0, var2 + 23, 20)) {
            this.fillWithBlocks(var3, var5, var2 + 0, 0, 0, var2 + 24, 0, 20, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.generateWaterBox(var3, var5, var2 + 0, 1, 0, var2 + 24, 10, 20, false);

            for(int var7 = 0; var7 < 4; ++var7) {
               this.fillWithBlocks(var3, var5, var2 + var7, var7 + 1, var7, var2 + var7, var7 + 1, 20, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var3, var5, var2 + var7 + 7, var7 + 5, var7 + 7, var2 + var7 + 7, var7 + 5, 20, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var3, var5, var2 + 17 - var7, var7 + 5, var7 + 7, var2 + 17 - var7, var7 + 5, 20, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var3, var5, var2 + 24 - var7, var7 + 1, var7, var2 + 24 - var7, var7 + 1, 20, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var3, var5, var2 + var7 + 1, var7 + 1, var7, var2 + 23 - var7, var7 + 1, var7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var3, var5, var2 + var7 + 8, var7 + 5, var7 + 7, var2 + 16 - var7, var7 + 5, var7 + 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            this.fillWithBlocks(var3, var5, var2 + 4, 4, 4, var2 + 6, 4, 20, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var3, var5, var2 + 7, 4, 4, var2 + 17, 4, 6, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var3, var5, var2 + 18, 4, 4, var2 + 20, 4, 20, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var3, var5, var2 + 11, 8, 11, var2 + 13, 8, 20, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.setBlockState(var3, DOT_DECO_DATA, var2 + 12, 9, 12, var5);
            this.setBlockState(var3, DOT_DECO_DATA, var2 + 12, 9, 15, var5);
            this.setBlockState(var3, DOT_DECO_DATA, var2 + 12, 9, 18, var5);
            int var11 = var2 + (var1 ? 19 : 5);
            int var8 = var2 + (var1 ? 5 : 19);

            for(int var9 = 20; var9 >= 5; var9 -= 3) {
               this.setBlockState(var3, DOT_DECO_DATA, var11, 5, var9, var5);
            }

            for(int var12 = 19; var12 >= 7; var12 -= 3) {
               this.setBlockState(var3, DOT_DECO_DATA, var8, 5, var12, var5);
            }

            for(int var13 = 0; var13 < 4; ++var13) {
               int var10 = var1 ? var2 + (24 - (17 - var13 * 3)) : var2 + 17 - var13 * 3;
               this.setBlockState(var3, DOT_DECO_DATA, var10, 5, 5, var5);
            }

            this.setBlockState(var3, DOT_DECO_DATA, var8, 5, 5, var5);
            this.fillWithBlocks(var3, var5, var2 + 11, 1, 12, var2 + 13, 7, 12, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var3, var5, var2 + 12, 1, 11, var2 + 12, 7, 13, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         }

      }

      private void generateEntranceArchs(World var1, Random var2, StructureBoundingBox var3) {
         if (this.doesChunkIntersect(var3, 22, 5, 35, 17)) {
            this.generateWaterBox(var1, var3, 25, 0, 0, 32, 8, 20, false);

            for(int var4 = 0; var4 < 4; ++var4) {
               this.fillWithBlocks(var1, var3, 24, 2, 5 + var4 * 4, 24, 4, 5 + var4 * 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 22, 4, 5 + var4 * 4, 23, 4, 5 + var4 * 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.setBlockState(var1, BRICKS_PRISMARINE, 25, 5, 5 + var4 * 4, var3);
               this.setBlockState(var1, BRICKS_PRISMARINE, 26, 6, 5 + var4 * 4, var3);
               this.setBlockState(var1, SEA_LANTERN, 26, 5, 5 + var4 * 4, var3);
               this.fillWithBlocks(var1, var3, 33, 2, 5 + var4 * 4, 33, 4, 5 + var4 * 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 34, 4, 5 + var4 * 4, 35, 4, 5 + var4 * 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.setBlockState(var1, BRICKS_PRISMARINE, 32, 5, 5 + var4 * 4, var3);
               this.setBlockState(var1, BRICKS_PRISMARINE, 31, 6, 5 + var4 * 4, var3);
               this.setBlockState(var1, SEA_LANTERN, 31, 5, 5 + var4 * 4, var3);
               this.fillWithBlocks(var1, var3, 27, 6, 5 + var4 * 4, 30, 6, 5 + var4 * 4, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            }
         }

      }

      private void generateEntranceWall(World var1, Random var2, StructureBoundingBox var3) {
         if (this.doesChunkIntersect(var3, 15, 20, 42, 21)) {
            this.fillWithBlocks(var1, var3, 15, 0, 21, 42, 0, 21, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.generateWaterBox(var1, var3, 26, 1, 21, 31, 3, 21, false);
            this.fillWithBlocks(var1, var3, 21, 12, 21, 36, 12, 21, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 17, 11, 21, 40, 11, 21, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 16, 10, 21, 41, 10, 21, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 15, 7, 21, 42, 9, 21, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 16, 6, 21, 41, 6, 21, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 17, 5, 21, 40, 5, 21, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 21, 4, 21, 36, 4, 21, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 22, 3, 21, 26, 3, 21, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 31, 3, 21, 35, 3, 21, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 23, 2, 21, 25, 2, 21, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 32, 2, 21, 34, 2, 21, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 28, 4, 20, 29, 4, 21, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.setBlockState(var1, BRICKS_PRISMARINE, 27, 3, 21, var3);
            this.setBlockState(var1, BRICKS_PRISMARINE, 30, 3, 21, var3);
            this.setBlockState(var1, BRICKS_PRISMARINE, 26, 2, 21, var3);
            this.setBlockState(var1, BRICKS_PRISMARINE, 31, 2, 21, var3);
            this.setBlockState(var1, BRICKS_PRISMARINE, 25, 1, 21, var3);
            this.setBlockState(var1, BRICKS_PRISMARINE, 32, 1, 21, var3);

            for(int var4 = 0; var4 < 7; ++var4) {
               this.setBlockState(var1, DARK_PRISMARINE, 28 - var4, 6 + var4, 21, var3);
               this.setBlockState(var1, DARK_PRISMARINE, 29 + var4, 6 + var4, 21, var3);
            }

            for(int var5 = 0; var5 < 4; ++var5) {
               this.setBlockState(var1, DARK_PRISMARINE, 28 - var5, 9 + var5, 21, var3);
               this.setBlockState(var1, DARK_PRISMARINE, 29 + var5, 9 + var5, 21, var3);
            }

            this.setBlockState(var1, DARK_PRISMARINE, 28, 12, 21, var3);
            this.setBlockState(var1, DARK_PRISMARINE, 29, 12, 21, var3);

            for(int var6 = 0; var6 < 3; ++var6) {
               this.setBlockState(var1, DARK_PRISMARINE, 22 - var6 * 2, 8, 21, var3);
               this.setBlockState(var1, DARK_PRISMARINE, 22 - var6 * 2, 9, 21, var3);
               this.setBlockState(var1, DARK_PRISMARINE, 35 + var6 * 2, 8, 21, var3);
               this.setBlockState(var1, DARK_PRISMARINE, 35 + var6 * 2, 9, 21, var3);
            }

            this.generateWaterBox(var1, var3, 15, 13, 21, 42, 15, 21, false);
            this.generateWaterBox(var1, var3, 15, 1, 21, 15, 6, 21, false);
            this.generateWaterBox(var1, var3, 16, 1, 21, 16, 5, 21, false);
            this.generateWaterBox(var1, var3, 17, 1, 21, 20, 4, 21, false);
            this.generateWaterBox(var1, var3, 21, 1, 21, 21, 3, 21, false);
            this.generateWaterBox(var1, var3, 22, 1, 21, 22, 2, 21, false);
            this.generateWaterBox(var1, var3, 23, 1, 21, 24, 1, 21, false);
            this.generateWaterBox(var1, var3, 42, 1, 21, 42, 6, 21, false);
            this.generateWaterBox(var1, var3, 41, 1, 21, 41, 5, 21, false);
            this.generateWaterBox(var1, var3, 37, 1, 21, 40, 4, 21, false);
            this.generateWaterBox(var1, var3, 36, 1, 21, 36, 3, 21, false);
            this.generateWaterBox(var1, var3, 33, 1, 21, 34, 1, 21, false);
            this.generateWaterBox(var1, var3, 35, 1, 21, 35, 2, 21, false);
         }

      }

      private void generateRoofPiece(World var1, Random var2, StructureBoundingBox var3) {
         if (this.doesChunkIntersect(var3, 21, 21, 36, 36)) {
            this.fillWithBlocks(var1, var3, 21, 0, 22, 36, 0, 36, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.generateWaterBox(var1, var3, 21, 1, 22, 36, 23, 36, false);

            for(int var4 = 0; var4 < 4; ++var4) {
               this.fillWithBlocks(var1, var3, 21 + var4, 13 + var4, 21 + var4, 36 - var4, 13 + var4, 21 + var4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 21 + var4, 13 + var4, 36 - var4, 36 - var4, 13 + var4, 36 - var4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 21 + var4, 13 + var4, 22 + var4, 21 + var4, 13 + var4, 35 - var4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 36 - var4, 13 + var4, 22 + var4, 36 - var4, 13 + var4, 35 - var4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            this.fillWithBlocks(var1, var3, 25, 16, 25, 32, 16, 32, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 25, 17, 25, 25, 19, 25, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 32, 17, 25, 32, 19, 25, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 25, 17, 32, 25, 19, 32, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 32, 17, 32, 32, 19, 32, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.setBlockState(var1, BRICKS_PRISMARINE, 26, 20, 26, var3);
            this.setBlockState(var1, BRICKS_PRISMARINE, 27, 21, 27, var3);
            this.setBlockState(var1, SEA_LANTERN, 27, 20, 27, var3);
            this.setBlockState(var1, BRICKS_PRISMARINE, 26, 20, 31, var3);
            this.setBlockState(var1, BRICKS_PRISMARINE, 27, 21, 30, var3);
            this.setBlockState(var1, SEA_LANTERN, 27, 20, 30, var3);
            this.setBlockState(var1, BRICKS_PRISMARINE, 31, 20, 31, var3);
            this.setBlockState(var1, BRICKS_PRISMARINE, 30, 21, 30, var3);
            this.setBlockState(var1, SEA_LANTERN, 30, 20, 30, var3);
            this.setBlockState(var1, BRICKS_PRISMARINE, 31, 20, 26, var3);
            this.setBlockState(var1, BRICKS_PRISMARINE, 30, 21, 27, var3);
            this.setBlockState(var1, SEA_LANTERN, 30, 20, 27, var3);
            this.fillWithBlocks(var1, var3, 28, 21, 27, 29, 21, 27, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 27, 21, 28, 27, 21, 29, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 28, 21, 30, 29, 21, 30, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 30, 21, 28, 30, 21, 29, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         }

      }

      private void generateLowerWall(World var1, Random var2, StructureBoundingBox var3) {
         if (this.doesChunkIntersect(var3, 0, 21, 6, 58)) {
            this.fillWithBlocks(var1, var3, 0, 0, 21, 6, 0, 57, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.generateWaterBox(var1, var3, 0, 1, 21, 6, 7, 57, false);
            this.fillWithBlocks(var1, var3, 4, 4, 21, 6, 4, 53, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);

            for(int var4 = 0; var4 < 4; ++var4) {
               this.fillWithBlocks(var1, var3, var4, var4 + 1, 21, var4, var4 + 1, 57 - var4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            for(int var5 = 23; var5 < 53; var5 += 3) {
               this.setBlockState(var1, DOT_DECO_DATA, 5, 5, var5, var3);
            }

            this.setBlockState(var1, DOT_DECO_DATA, 5, 5, 52, var3);

            for(int var6 = 0; var6 < 4; ++var6) {
               this.fillWithBlocks(var1, var3, var6, var6 + 1, 21, var6, var6 + 1, 57 - var6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            this.fillWithBlocks(var1, var3, 4, 1, 52, 6, 3, 52, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 5, 1, 51, 5, 3, 53, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         }

         if (this.doesChunkIntersect(var3, 51, 21, 58, 58)) {
            this.fillWithBlocks(var1, var3, 51, 0, 21, 57, 0, 57, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.generateWaterBox(var1, var3, 51, 1, 21, 57, 7, 57, false);
            this.fillWithBlocks(var1, var3, 51, 4, 21, 53, 4, 53, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);

            for(int var7 = 0; var7 < 4; ++var7) {
               this.fillWithBlocks(var1, var3, 57 - var7, var7 + 1, 21, 57 - var7, var7 + 1, 57 - var7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            for(int var8 = 23; var8 < 53; var8 += 3) {
               this.setBlockState(var1, DOT_DECO_DATA, 52, 5, var8, var3);
            }

            this.setBlockState(var1, DOT_DECO_DATA, 52, 5, 52, var3);
            this.fillWithBlocks(var1, var3, 51, 1, 52, 53, 3, 52, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 52, 1, 51, 52, 3, 53, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         }

         if (this.doesChunkIntersect(var3, 0, 51, 57, 57)) {
            this.fillWithBlocks(var1, var3, 7, 0, 51, 50, 0, 57, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.generateWaterBox(var1, var3, 7, 1, 51, 50, 10, 57, false);

            for(int var9 = 0; var9 < 4; ++var9) {
               this.fillWithBlocks(var1, var3, var9 + 1, var9 + 1, 57 - var9, 56 - var9, var9 + 1, 57 - var9, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }
         }

      }

      private void generateMiddleWall(World var1, Random var2, StructureBoundingBox var3) {
         if (this.doesChunkIntersect(var3, 7, 21, 13, 50)) {
            this.fillWithBlocks(var1, var3, 7, 0, 21, 13, 0, 50, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.generateWaterBox(var1, var3, 7, 1, 21, 13, 10, 50, false);
            this.fillWithBlocks(var1, var3, 11, 8, 21, 13, 8, 53, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);

            for(int var4 = 0; var4 < 4; ++var4) {
               this.fillWithBlocks(var1, var3, var4 + 7, var4 + 5, 21, var4 + 7, var4 + 5, 54, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            for(int var5 = 21; var5 <= 45; var5 += 3) {
               this.setBlockState(var1, DOT_DECO_DATA, 12, 9, var5, var3);
            }
         }

         if (this.doesChunkIntersect(var3, 44, 21, 50, 54)) {
            this.fillWithBlocks(var1, var3, 44, 0, 21, 50, 0, 50, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.generateWaterBox(var1, var3, 44, 1, 21, 50, 10, 50, false);
            this.fillWithBlocks(var1, var3, 44, 8, 21, 46, 8, 53, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);

            for(int var6 = 0; var6 < 4; ++var6) {
               this.fillWithBlocks(var1, var3, 50 - var6, var6 + 5, 21, 50 - var6, var6 + 5, 54, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            for(int var7 = 21; var7 <= 45; var7 += 3) {
               this.setBlockState(var1, DOT_DECO_DATA, 45, 9, var7, var3);
            }
         }

         if (this.doesChunkIntersect(var3, 8, 44, 49, 54)) {
            this.fillWithBlocks(var1, var3, 14, 0, 44, 43, 0, 50, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.generateWaterBox(var1, var3, 14, 1, 44, 43, 10, 50, false);

            for(int var8 = 12; var8 <= 45; var8 += 3) {
               this.setBlockState(var1, DOT_DECO_DATA, var8, 9, 45, var3);
               this.setBlockState(var1, DOT_DECO_DATA, var8, 9, 52, var3);
               if (var8 == 12 || var8 == 18 || var8 == 24 || var8 == 33 || var8 == 39 || var8 == 45) {
                  this.setBlockState(var1, DOT_DECO_DATA, var8, 9, 47, var3);
                  this.setBlockState(var1, DOT_DECO_DATA, var8, 9, 50, var3);
                  this.setBlockState(var1, DOT_DECO_DATA, var8, 10, 45, var3);
                  this.setBlockState(var1, DOT_DECO_DATA, var8, 10, 46, var3);
                  this.setBlockState(var1, DOT_DECO_DATA, var8, 10, 51, var3);
                  this.setBlockState(var1, DOT_DECO_DATA, var8, 10, 52, var3);
                  this.setBlockState(var1, DOT_DECO_DATA, var8, 11, 47, var3);
                  this.setBlockState(var1, DOT_DECO_DATA, var8, 11, 50, var3);
                  this.setBlockState(var1, DOT_DECO_DATA, var8, 12, 48, var3);
                  this.setBlockState(var1, DOT_DECO_DATA, var8, 12, 49, var3);
               }
            }

            for(int var9 = 0; var9 < 3; ++var9) {
               this.fillWithBlocks(var1, var3, 8 + var9, 5 + var9, 54, 49 - var9, 5 + var9, 54, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            }

            this.fillWithBlocks(var1, var3, 11, 8, 54, 46, 8, 54, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 14, 8, 44, 43, 8, 53, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         }

      }

      private void generateUpperWall(World var1, Random var2, StructureBoundingBox var3) {
         if (this.doesChunkIntersect(var3, 14, 21, 20, 43)) {
            this.fillWithBlocks(var1, var3, 14, 0, 21, 20, 0, 43, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.generateWaterBox(var1, var3, 14, 1, 22, 20, 14, 43, false);
            this.fillWithBlocks(var1, var3, 18, 12, 22, 20, 12, 39, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 18, 12, 21, 20, 12, 21, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);

            for(int var4 = 0; var4 < 4; ++var4) {
               this.fillWithBlocks(var1, var3, var4 + 14, var4 + 9, 21, var4 + 14, var4 + 9, 43 - var4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            for(int var5 = 23; var5 <= 39; var5 += 3) {
               this.setBlockState(var1, DOT_DECO_DATA, 19, 13, var5, var3);
            }
         }

         if (this.doesChunkIntersect(var3, 37, 21, 43, 43)) {
            this.fillWithBlocks(var1, var3, 37, 0, 21, 43, 0, 43, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.generateWaterBox(var1, var3, 37, 1, 22, 43, 14, 43, false);
            this.fillWithBlocks(var1, var3, 37, 12, 22, 39, 12, 39, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 37, 12, 21, 39, 12, 21, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);

            for(int var6 = 0; var6 < 4; ++var6) {
               this.fillWithBlocks(var1, var3, 43 - var6, var6 + 9, 21, 43 - var6, var6 + 9, 43 - var6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            for(int var7 = 23; var7 <= 39; var7 += 3) {
               this.setBlockState(var1, DOT_DECO_DATA, 38, 13, var7, var3);
            }
         }

         if (this.doesChunkIntersect(var3, 15, 37, 42, 43)) {
            this.fillWithBlocks(var1, var3, 21, 0, 37, 36, 0, 43, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.generateWaterBox(var1, var3, 21, 1, 37, 36, 14, 43, false);
            this.fillWithBlocks(var1, var3, 21, 12, 37, 36, 12, 39, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);

            for(int var8 = 0; var8 < 4; ++var8) {
               this.fillWithBlocks(var1, var3, 15 + var8, var8 + 9, 43 - var8, 42 - var8, var8 + 9, 43 - var8, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            for(int var9 = 21; var9 <= 36; var9 += 3) {
               this.setBlockState(var1, DOT_DECO_DATA, var9, 13, 38, var3);
            }
         }

      }
   }

   public static class MonumentCoreRoom extends StructureOceanMonumentPieces.Piece {
      public MonumentCoreRoom() {
      }

      public MonumentCoreRoom(EnumFacing var1, StructureOceanMonumentPieces.RoomDefinition var2, Random var3) {
         super(1, var1, var2, 2, 2, 2);
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         this.generateBoxOnFillOnly(var1, var3, 1, 8, 0, 14, 8, 14, ROUGH_PRISMARINE);
         boolean var4 = true;
         IBlockState var5 = BRICKS_PRISMARINE;
         this.fillWithBlocks(var1, var3, 0, 7, 0, 0, 7, 15, var5, var5, false);
         this.fillWithBlocks(var1, var3, 15, 7, 0, 15, 7, 15, var5, var5, false);
         this.fillWithBlocks(var1, var3, 1, 7, 0, 15, 7, 0, var5, var5, false);
         this.fillWithBlocks(var1, var3, 1, 7, 15, 14, 7, 15, var5, var5, false);

         for(int var7 = 1; var7 <= 6; ++var7) {
            var5 = BRICKS_PRISMARINE;
            if (var7 == 2 || var7 == 6) {
               var5 = ROUGH_PRISMARINE;
            }

            for(int var6 = 0; var6 <= 15; var6 += 15) {
               this.fillWithBlocks(var1, var3, var6, var7, 0, var6, var7, 1, var5, var5, false);
               this.fillWithBlocks(var1, var3, var6, var7, 6, var6, var7, 9, var5, var5, false);
               this.fillWithBlocks(var1, var3, var6, var7, 14, var6, var7, 15, var5, var5, false);
            }

            this.fillWithBlocks(var1, var3, 1, var7, 0, 1, var7, 0, var5, var5, false);
            this.fillWithBlocks(var1, var3, 6, var7, 0, 9, var7, 0, var5, var5, false);
            this.fillWithBlocks(var1, var3, 14, var7, 0, 14, var7, 0, var5, var5, false);
            this.fillWithBlocks(var1, var3, 1, var7, 15, 14, var7, 15, var5, var5, false);
         }

         this.fillWithBlocks(var1, var3, 6, 3, 6, 9, 6, 9, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 7, 4, 7, 8, 5, 8, Blocks.GOLD_BLOCK.getDefaultState(), Blocks.GOLD_BLOCK.getDefaultState(), false);

         for(int var8 = 3; var8 <= 6; var8 += 3) {
            for(int var10 = 6; var10 <= 9; var10 += 3) {
               this.setBlockState(var1, SEA_LANTERN, var10, var8, 6, var3);
               this.setBlockState(var1, SEA_LANTERN, var10, var8, 9, var3);
            }
         }

         this.fillWithBlocks(var1, var3, 5, 1, 6, 5, 2, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 5, 1, 9, 5, 2, 9, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 10, 1, 6, 10, 2, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 10, 1, 9, 10, 2, 9, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 6, 1, 5, 6, 2, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 9, 1, 5, 9, 2, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 6, 1, 10, 6, 2, 10, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 9, 1, 10, 9, 2, 10, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 5, 2, 5, 5, 6, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 5, 2, 10, 5, 6, 10, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 10, 2, 5, 10, 6, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 10, 2, 10, 10, 6, 10, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 5, 7, 1, 5, 7, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 10, 7, 1, 10, 7, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 5, 7, 9, 5, 7, 14, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 10, 7, 9, 10, 7, 14, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 7, 5, 6, 7, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 7, 10, 6, 7, 10, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 9, 7, 5, 14, 7, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 9, 7, 10, 14, 7, 10, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 2, 1, 2, 2, 1, 3, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 3, 1, 2, 3, 1, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 13, 1, 2, 13, 1, 3, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 12, 1, 2, 12, 1, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 2, 1, 12, 2, 1, 13, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 3, 1, 13, 3, 1, 13, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 13, 1, 12, 13, 1, 13, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 12, 1, 13, 12, 1, 13, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         return true;
      }
   }

   interface MonumentRoomFitHelper {
      boolean fits(StructureOceanMonumentPieces.RoomDefinition var1);

      StructureOceanMonumentPieces.Piece create(EnumFacing var1, StructureOceanMonumentPieces.RoomDefinition var2, Random var3);
   }

   public static class Penthouse extends StructureOceanMonumentPieces.Piece {
      public Penthouse() {
      }

      public Penthouse(EnumFacing var1, StructureBoundingBox var2) {
         super(var1, var2);
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         this.fillWithBlocks(var1, var3, 2, -1, 2, 11, -1, 11, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 0, -1, 0, 1, -1, 11, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 12, -1, 0, 13, -1, 11, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 2, -1, 0, 11, -1, 1, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 2, -1, 12, 11, -1, 13, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 0, 0, 0, 0, 0, 13, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 13, 0, 0, 13, 0, 13, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 0, 0, 12, 0, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 0, 13, 12, 0, 13, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);

         for(int var4 = 2; var4 <= 11; var4 += 3) {
            this.setBlockState(var1, SEA_LANTERN, 0, 0, var4, var3);
            this.setBlockState(var1, SEA_LANTERN, 13, 0, var4, var3);
            this.setBlockState(var1, SEA_LANTERN, var4, 0, 0, var3);
         }

         this.fillWithBlocks(var1, var3, 2, 0, 3, 4, 0, 9, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 9, 0, 3, 11, 0, 9, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 4, 0, 9, 9, 0, 11, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.setBlockState(var1, BRICKS_PRISMARINE, 5, 0, 8, var3);
         this.setBlockState(var1, BRICKS_PRISMARINE, 8, 0, 8, var3);
         this.setBlockState(var1, BRICKS_PRISMARINE, 10, 0, 10, var3);
         this.setBlockState(var1, BRICKS_PRISMARINE, 3, 0, 10, var3);
         this.fillWithBlocks(var1, var3, 3, 0, 3, 3, 0, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 10, 0, 3, 10, 0, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 6, 0, 10, 7, 0, 10, DARK_PRISMARINE, DARK_PRISMARINE, false);
         byte var7 = 3;

         for(int var5 = 0; var5 < 2; ++var5) {
            for(int var6 = 2; var6 <= 8; var6 += 3) {
               this.fillWithBlocks(var1, var3, var7, 0, var6, var7, 2, var6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            var7 = 10;
         }

         this.fillWithBlocks(var1, var3, 5, 0, 10, 5, 2, 10, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 8, 0, 10, 8, 2, 10, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 6, -1, 7, 7, -1, 8, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.generateWaterBox(var1, var3, 6, -1, 3, 7, -1, 4, false);
         this.spawnElder(var1, var3, 6, 1, 6);
         return true;
      }
   }

   public abstract static class Piece extends StructureComponent {
      protected static final IBlockState ROUGH_PRISMARINE = Blocks.PRISMARINE.getStateFromMeta(BlockPrismarine.ROUGH_META);
      protected static final IBlockState BRICKS_PRISMARINE = Blocks.PRISMARINE.getStateFromMeta(BlockPrismarine.BRICKS_META);
      protected static final IBlockState DARK_PRISMARINE = Blocks.PRISMARINE.getStateFromMeta(BlockPrismarine.DARK_META);
      protected static final IBlockState DOT_DECO_DATA = BRICKS_PRISMARINE;
      protected static final IBlockState SEA_LANTERN = Blocks.SEA_LANTERN.getDefaultState();
      protected static final IBlockState WATER = Blocks.WATER.getDefaultState();
      protected static final int GRIDROOM_SOURCE_INDEX = getRoomIndex(2, 0, 0);
      protected static final int GRIDROOM_TOP_CONNECT_INDEX = getRoomIndex(2, 2, 0);
      protected static final int GRIDROOM_LEFTWING_CONNECT_INDEX = getRoomIndex(0, 1, 0);
      protected static final int GRIDROOM_RIGHTWING_CONNECT_INDEX = getRoomIndex(4, 1, 0);
      protected StructureOceanMonumentPieces.RoomDefinition roomDefinition;

      protected static final int getRoomIndex(int var0, int var1, int var2) {
         return var1 * 25 + var2 * 5 + var0;
      }

      public Piece() {
         super(0);
      }

      public Piece(int var1) {
         super(var1);
      }

      public Piece(EnumFacing var1, StructureBoundingBox var2) {
         super(1);
         this.setCoordBaseMode(var1);
         this.boundingBox = var2;
      }

      protected Piece(int var1, EnumFacing var2, StructureOceanMonumentPieces.RoomDefinition var3, int var4, int var5, int var6) {
         super(var1);
         this.setCoordBaseMode(var2);
         this.roomDefinition = var3;
         int var7 = var3.index;
         int var8 = var7 % 5;
         int var9 = var7 / 5 % 5;
         int var10 = var7 / 25;
         if (var2 != EnumFacing.NORTH && var2 != EnumFacing.SOUTH) {
            this.boundingBox = new StructureBoundingBox(0, 0, 0, var6 * 8 - 1, var5 * 4 - 1, var4 * 8 - 1);
         } else {
            this.boundingBox = new StructureBoundingBox(0, 0, 0, var4 * 8 - 1, var5 * 4 - 1, var6 * 8 - 1);
         }

         switch(var2) {
         case NORTH:
            this.boundingBox.offset(var8 * 8, var10 * 4, -(var9 + var6) * 8 + 1);
            break;
         case SOUTH:
            this.boundingBox.offset(var8 * 8, var10 * 4, var9 * 8);
            break;
         case WEST:
            this.boundingBox.offset(-(var9 + var6) * 8 + 1, var10 * 4, var8 * 8);
            break;
         default:
            this.boundingBox.offset(var9 * 8, var10 * 4, var8 * 8);
         }

      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
      }

      protected void generateWaterBox(World var1, StructureBoundingBox var2, int var3, int var4, int var5, int var6, int var7, int var8, boolean var9) {
         for(int var10 = var4; var10 <= var7; ++var10) {
            for(int var11 = var3; var11 <= var6; ++var11) {
               for(int var12 = var5; var12 <= var8; ++var12) {
                  if (!var9 || this.getBlockStateFromPos(var1, var11, var10, var12, var2).getMaterial() != Material.AIR) {
                     if (this.getYWithOffset(var10) >= var1.getSeaLevel()) {
                        this.setBlockState(var1, Blocks.AIR.getDefaultState(), var11, var10, var12, var2);
                     } else {
                        this.setBlockState(var1, WATER, var11, var10, var12, var2);
                     }
                  }
               }
            }
         }

      }

      protected void generateDefaultFloor(World var1, StructureBoundingBox var2, int var3, int var4, boolean var5) {
         if (var5) {
            this.fillWithBlocks(var1, var2, var3 + 0, 0, var4 + 0, var3 + 2, 0, var4 + 8 - 1, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var2, var3 + 5, 0, var4 + 0, var3 + 8 - 1, 0, var4 + 8 - 1, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var2, var3 + 3, 0, var4 + 0, var3 + 4, 0, var4 + 2, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var2, var3 + 3, 0, var4 + 5, var3 + 4, 0, var4 + 8 - 1, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var2, var3 + 3, 0, var4 + 2, var3 + 4, 0, var4 + 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var2, var3 + 3, 0, var4 + 5, var3 + 4, 0, var4 + 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var2, var3 + 2, 0, var4 + 3, var3 + 2, 0, var4 + 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var2, var3 + 5, 0, var4 + 3, var3 + 5, 0, var4 + 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         } else {
            this.fillWithBlocks(var1, var2, var3 + 0, 0, var4 + 0, var3 + 8 - 1, 0, var4 + 8 - 1, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         }

      }

      protected void generateBoxOnFillOnly(World var1, StructureBoundingBox var2, int var3, int var4, int var5, int var6, int var7, int var8, IBlockState var9) {
         for(int var10 = var4; var10 <= var7; ++var10) {
            for(int var11 = var3; var11 <= var6; ++var11) {
               for(int var12 = var5; var12 <= var8; ++var12) {
                  if (this.getBlockStateFromPos(var1, var11, var10, var12, var2) == WATER) {
                     this.setBlockState(var1, var9, var11, var10, var12, var2);
                  }
               }
            }
         }

      }

      protected boolean doesChunkIntersect(StructureBoundingBox var1, int var2, int var3, int var4, int var5) {
         int var6 = this.getXWithOffset(var2, var3);
         int var7 = this.getZWithOffset(var2, var3);
         int var8 = this.getXWithOffset(var4, var5);
         int var9 = this.getZWithOffset(var4, var5);
         return var1.intersectsWith(Math.min(var6, var8), Math.min(var7, var9), Math.max(var6, var8), Math.max(var7, var9));
      }

      protected boolean spawnElder(World var1, StructureBoundingBox var2, int var3, int var4, int var5) {
         int var6 = this.getXWithOffset(var3, var5);
         int var7 = this.getYWithOffset(var4);
         int var8 = this.getZWithOffset(var3, var5);
         if (var2.isVecInside(new BlockPos(var6, var7, var8))) {
            EntityGuardian var9 = new EntityGuardian(var1);
            var9.setElder(true);
            var9.heal(var9.getMaxHealth());
            var9.setLocationAndAngles((double)var6 + 0.5D, (double)var7, (double)var8 + 0.5D, 0.0F, 0.0F);
            var9.onInitialSpawn(var1.getDifficultyForLocation(new BlockPos(var9)), (IEntityLivingData)null);
            var1.spawnEntity(var9);
            return true;
         } else {
            return false;
         }
      }
   }

   static class RoomDefinition {
      int index;
      StructureOceanMonumentPieces.RoomDefinition[] connections = new StructureOceanMonumentPieces.RoomDefinition[6];
      boolean[] hasOpening = new boolean[6];
      boolean claimed;
      boolean isSource;
      int scanIndex;

      public RoomDefinition(int var1) {
         this.index = var1;
      }

      public void setConnection(EnumFacing var1, StructureOceanMonumentPieces.RoomDefinition var2) {
         this.connections[var1.getIndex()] = var2;
         var2.connections[var1.getOpposite().getIndex()] = this;
      }

      public void updateOpenings() {
         for(int var1 = 0; var1 < 6; ++var1) {
            this.hasOpening[var1] = this.connections[var1] != null;
         }

      }

      public boolean findSource(int var1) {
         if (this.isSource) {
            return true;
         } else {
            this.scanIndex = var1;

            for(int var2 = 0; var2 < 6; ++var2) {
               if (this.connections[var2] != null && this.hasOpening[var2] && this.connections[var2].scanIndex != var1 && this.connections[var2].findSource(var1)) {
                  return true;
               }
            }

            return false;
         }
      }

      public boolean isSpecial() {
         return this.index >= 75;
      }

      public int countOpenings() {
         int var1 = 0;

         for(int var2 = 0; var2 < 6; ++var2) {
            if (this.hasOpening[var2]) {
               ++var1;
            }
         }

         return var1;
      }
   }

   public static class SimpleRoom extends StructureOceanMonumentPieces.Piece {
      private int mainDesign;

      public SimpleRoom() {
      }

      public SimpleRoom(EnumFacing var1, StructureOceanMonumentPieces.RoomDefinition var2, Random var3) {
         super(1, var1, var2, 1, 1, 1);
         this.mainDesign = var3.nextInt(3);
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(var1, var3, 0, 0, this.roomDefinition.hasOpening[EnumFacing.DOWN.getIndex()]);
         }

         if (this.roomDefinition.connections[EnumFacing.UP.getIndex()] == null) {
            this.generateBoxOnFillOnly(var1, var3, 1, 4, 1, 6, 4, 6, ROUGH_PRISMARINE);
         }

         boolean var4 = this.mainDesign != 0 && var2.nextBoolean() && !this.roomDefinition.hasOpening[EnumFacing.DOWN.getIndex()] && !this.roomDefinition.hasOpening[EnumFacing.UP.getIndex()] && this.roomDefinition.countOpenings() > 1;
         if (this.mainDesign == 0) {
            this.fillWithBlocks(var1, var3, 0, 1, 0, 2, 1, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 0, 3, 0, 2, 3, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 0, 2, 0, 0, 2, 2, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 1, 2, 0, 2, 2, 0, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.setBlockState(var1, SEA_LANTERN, 1, 2, 1, var3);
            this.fillWithBlocks(var1, var3, 5, 1, 0, 7, 1, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 5, 3, 0, 7, 3, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 7, 2, 0, 7, 2, 2, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 5, 2, 0, 6, 2, 0, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.setBlockState(var1, SEA_LANTERN, 6, 2, 1, var3);
            this.fillWithBlocks(var1, var3, 0, 1, 5, 2, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 0, 3, 5, 2, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 0, 2, 5, 0, 2, 7, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 1, 2, 7, 2, 2, 7, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.setBlockState(var1, SEA_LANTERN, 1, 2, 6, var3);
            this.fillWithBlocks(var1, var3, 5, 1, 5, 7, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 5, 3, 5, 7, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 7, 2, 5, 7, 2, 7, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 5, 2, 7, 6, 2, 7, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.setBlockState(var1, SEA_LANTERN, 6, 2, 6, var3);
            if (this.roomDefinition.hasOpening[EnumFacing.SOUTH.getIndex()]) {
               this.fillWithBlocks(var1, var3, 3, 3, 0, 4, 3, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            } else {
               this.fillWithBlocks(var1, var3, 3, 3, 0, 4, 3, 1, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 3, 2, 0, 4, 2, 0, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 3, 1, 0, 4, 1, 1, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            if (this.roomDefinition.hasOpening[EnumFacing.NORTH.getIndex()]) {
               this.fillWithBlocks(var1, var3, 3, 3, 7, 4, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            } else {
               this.fillWithBlocks(var1, var3, 3, 3, 6, 4, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 3, 2, 7, 4, 2, 7, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 3, 1, 6, 4, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            if (this.roomDefinition.hasOpening[EnumFacing.WEST.getIndex()]) {
               this.fillWithBlocks(var1, var3, 0, 3, 3, 0, 3, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            } else {
               this.fillWithBlocks(var1, var3, 0, 3, 3, 1, 3, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 0, 2, 3, 0, 2, 4, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 0, 1, 3, 1, 1, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            if (this.roomDefinition.hasOpening[EnumFacing.EAST.getIndex()]) {
               this.fillWithBlocks(var1, var3, 7, 3, 3, 7, 3, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            } else {
               this.fillWithBlocks(var1, var3, 6, 3, 3, 7, 3, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 7, 2, 3, 7, 2, 4, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 6, 1, 3, 7, 1, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }
         } else if (this.mainDesign == 1) {
            this.fillWithBlocks(var1, var3, 2, 1, 2, 2, 3, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 2, 1, 5, 2, 3, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 5, 1, 5, 5, 3, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 5, 1, 2, 5, 3, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.setBlockState(var1, SEA_LANTERN, 2, 2, 2, var3);
            this.setBlockState(var1, SEA_LANTERN, 2, 2, 5, var3);
            this.setBlockState(var1, SEA_LANTERN, 5, 2, 5, var3);
            this.setBlockState(var1, SEA_LANTERN, 5, 2, 2, var3);
            this.fillWithBlocks(var1, var3, 0, 1, 0, 1, 3, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 0, 1, 1, 0, 3, 1, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 0, 1, 7, 1, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 0, 1, 6, 0, 3, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 6, 1, 7, 7, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 7, 1, 6, 7, 3, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 6, 1, 0, 7, 3, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 7, 1, 1, 7, 3, 1, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.setBlockState(var1, ROUGH_PRISMARINE, 1, 2, 0, var3);
            this.setBlockState(var1, ROUGH_PRISMARINE, 0, 2, 1, var3);
            this.setBlockState(var1, ROUGH_PRISMARINE, 1, 2, 7, var3);
            this.setBlockState(var1, ROUGH_PRISMARINE, 0, 2, 6, var3);
            this.setBlockState(var1, ROUGH_PRISMARINE, 6, 2, 7, var3);
            this.setBlockState(var1, ROUGH_PRISMARINE, 7, 2, 6, var3);
            this.setBlockState(var1, ROUGH_PRISMARINE, 6, 2, 0, var3);
            this.setBlockState(var1, ROUGH_PRISMARINE, 7, 2, 1, var3);
            if (!this.roomDefinition.hasOpening[EnumFacing.SOUTH.getIndex()]) {
               this.fillWithBlocks(var1, var3, 1, 3, 0, 6, 3, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 1, 2, 0, 6, 2, 0, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 1, 1, 0, 6, 1, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            if (!this.roomDefinition.hasOpening[EnumFacing.NORTH.getIndex()]) {
               this.fillWithBlocks(var1, var3, 1, 3, 7, 6, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 1, 2, 7, 6, 2, 7, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 1, 1, 7, 6, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            if (!this.roomDefinition.hasOpening[EnumFacing.WEST.getIndex()]) {
               this.fillWithBlocks(var1, var3, 0, 3, 1, 0, 3, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 0, 2, 1, 0, 2, 6, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 0, 1, 1, 0, 1, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            if (!this.roomDefinition.hasOpening[EnumFacing.EAST.getIndex()]) {
               this.fillWithBlocks(var1, var3, 7, 3, 1, 7, 3, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 7, 2, 1, 7, 2, 6, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, 7, 1, 1, 7, 1, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }
         } else if (this.mainDesign == 2) {
            this.fillWithBlocks(var1, var3, 0, 1, 0, 0, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 7, 1, 0, 7, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 1, 1, 0, 6, 1, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 1, 1, 7, 6, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 0, 2, 0, 0, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 7, 2, 0, 7, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 1, 2, 0, 6, 2, 0, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 1, 2, 7, 6, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 0, 3, 0, 0, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 7, 3, 0, 7, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 1, 3, 0, 6, 3, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 1, 3, 7, 6, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 0, 1, 3, 0, 2, 4, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 7, 1, 3, 7, 2, 4, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 3, 1, 0, 4, 2, 0, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 3, 1, 7, 4, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            if (this.roomDefinition.hasOpening[EnumFacing.SOUTH.getIndex()]) {
               this.generateWaterBox(var1, var3, 3, 1, 0, 4, 2, 0, false);
            }

            if (this.roomDefinition.hasOpening[EnumFacing.NORTH.getIndex()]) {
               this.generateWaterBox(var1, var3, 3, 1, 7, 4, 2, 7, false);
            }

            if (this.roomDefinition.hasOpening[EnumFacing.WEST.getIndex()]) {
               this.generateWaterBox(var1, var3, 0, 1, 3, 0, 2, 4, false);
            }

            if (this.roomDefinition.hasOpening[EnumFacing.EAST.getIndex()]) {
               this.generateWaterBox(var1, var3, 7, 1, 3, 7, 2, 4, false);
            }
         }

         if (var4) {
            this.fillWithBlocks(var1, var3, 3, 1, 3, 4, 1, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 3, 2, 3, 4, 2, 4, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 3, 3, 3, 4, 3, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         }

         return true;
      }
   }

   public static class SimpleTopRoom extends StructureOceanMonumentPieces.Piece {
      public SimpleTopRoom() {
      }

      public SimpleTopRoom(EnumFacing var1, StructureOceanMonumentPieces.RoomDefinition var2, Random var3) {
         super(1, var1, var2, 1, 1, 1);
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(var1, var3, 0, 0, this.roomDefinition.hasOpening[EnumFacing.DOWN.getIndex()]);
         }

         if (this.roomDefinition.connections[EnumFacing.UP.getIndex()] == null) {
            this.generateBoxOnFillOnly(var1, var3, 1, 4, 1, 6, 4, 6, ROUGH_PRISMARINE);
         }

         for(int var4 = 1; var4 <= 6; ++var4) {
            for(int var5 = 1; var5 <= 6; ++var5) {
               if (var2.nextInt(3) != 0) {
                  int var6 = 2 + (var2.nextInt(4) == 0 ? 0 : 1);
                  this.fillWithBlocks(var1, var3, var4, var6, var5, var4, 3, var5, Blocks.SPONGE.getStateFromMeta(1), Blocks.SPONGE.getStateFromMeta(1), false);
               }
            }
         }

         this.fillWithBlocks(var1, var3, 0, 1, 0, 0, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 7, 1, 0, 7, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 1, 0, 6, 1, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 1, 7, 6, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 0, 2, 0, 0, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 7, 2, 0, 7, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 2, 0, 6, 2, 0, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 2, 7, 6, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 0, 3, 0, 0, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 7, 3, 0, 7, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 3, 0, 6, 3, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 1, 3, 7, 6, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 0, 1, 3, 0, 2, 4, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 7, 1, 3, 7, 2, 4, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 3, 1, 0, 4, 2, 0, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithBlocks(var1, var3, 3, 1, 7, 4, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
         if (this.roomDefinition.hasOpening[EnumFacing.SOUTH.getIndex()]) {
            this.generateWaterBox(var1, var3, 3, 1, 0, 4, 2, 0, false);
         }

         return true;
      }
   }

   public static class WingRoom extends StructureOceanMonumentPieces.Piece {
      private int mainDesign;

      public WingRoom() {
      }

      public WingRoom(EnumFacing var1, StructureBoundingBox var2, int var3) {
         super(var1, var2);
         this.mainDesign = var3 & 1;
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.mainDesign == 0) {
            for(int var4 = 0; var4 < 4; ++var4) {
               this.fillWithBlocks(var1, var3, 10 - var4, 3 - var4, 20 - var4, 12 + var4, 3 - var4, 20, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            this.fillWithBlocks(var1, var3, 7, 0, 6, 15, 0, 16, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 6, 0, 6, 6, 3, 20, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 16, 0, 6, 16, 3, 20, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 7, 1, 7, 7, 1, 20, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 15, 1, 7, 15, 1, 20, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 7, 1, 6, 9, 3, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 13, 1, 6, 15, 3, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 8, 1, 7, 9, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 13, 1, 7, 14, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 9, 0, 5, 13, 0, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 10, 0, 7, 12, 0, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 8, 0, 10, 8, 0, 12, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 14, 0, 10, 14, 0, 12, DARK_PRISMARINE, DARK_PRISMARINE, false);

            for(int var8 = 18; var8 >= 7; var8 -= 3) {
               this.setBlockState(var1, SEA_LANTERN, 6, 3, var8, var3);
               this.setBlockState(var1, SEA_LANTERN, 16, 3, var8, var3);
            }

            this.setBlockState(var1, SEA_LANTERN, 10, 0, 10, var3);
            this.setBlockState(var1, SEA_LANTERN, 12, 0, 10, var3);
            this.setBlockState(var1, SEA_LANTERN, 10, 0, 12, var3);
            this.setBlockState(var1, SEA_LANTERN, 12, 0, 12, var3);
            this.setBlockState(var1, SEA_LANTERN, 8, 3, 6, var3);
            this.setBlockState(var1, SEA_LANTERN, 14, 3, 6, var3);
            this.setBlockState(var1, BRICKS_PRISMARINE, 4, 2, 4, var3);
            this.setBlockState(var1, SEA_LANTERN, 4, 1, 4, var3);
            this.setBlockState(var1, BRICKS_PRISMARINE, 4, 0, 4, var3);
            this.setBlockState(var1, BRICKS_PRISMARINE, 18, 2, 4, var3);
            this.setBlockState(var1, SEA_LANTERN, 18, 1, 4, var3);
            this.setBlockState(var1, BRICKS_PRISMARINE, 18, 0, 4, var3);
            this.setBlockState(var1, BRICKS_PRISMARINE, 4, 2, 18, var3);
            this.setBlockState(var1, SEA_LANTERN, 4, 1, 18, var3);
            this.setBlockState(var1, BRICKS_PRISMARINE, 4, 0, 18, var3);
            this.setBlockState(var1, BRICKS_PRISMARINE, 18, 2, 18, var3);
            this.setBlockState(var1, SEA_LANTERN, 18, 1, 18, var3);
            this.setBlockState(var1, BRICKS_PRISMARINE, 18, 0, 18, var3);
            this.setBlockState(var1, BRICKS_PRISMARINE, 9, 7, 20, var3);
            this.setBlockState(var1, BRICKS_PRISMARINE, 13, 7, 20, var3);
            this.fillWithBlocks(var1, var3, 6, 0, 21, 7, 4, 21, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 15, 0, 21, 16, 4, 21, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.spawnElder(var1, var3, 11, 2, 16);
         } else if (this.mainDesign == 1) {
            this.fillWithBlocks(var1, var3, 9, 3, 18, 13, 3, 20, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 9, 0, 18, 9, 2, 18, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 13, 0, 18, 13, 2, 18, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            byte var9 = 9;
            boolean var5 = true;
            boolean var6 = true;

            for(int var7 = 0; var7 < 2; ++var7) {
               this.setBlockState(var1, BRICKS_PRISMARINE, var9, 6, 20, var3);
               this.setBlockState(var1, SEA_LANTERN, var9, 5, 20, var3);
               this.setBlockState(var1, BRICKS_PRISMARINE, var9, 4, 20, var3);
               var9 = 13;
            }

            this.fillWithBlocks(var1, var3, 7, 3, 7, 15, 3, 14, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            var9 = 10;

            for(int var12 = 0; var12 < 2; ++var12) {
               this.fillWithBlocks(var1, var3, var9, 0, 10, var9, 6, 10, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, var9, 0, 12, var9, 6, 12, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.setBlockState(var1, SEA_LANTERN, var9, 0, 10, var3);
               this.setBlockState(var1, SEA_LANTERN, var9, 0, 12, var3);
               this.setBlockState(var1, SEA_LANTERN, var9, 4, 10, var3);
               this.setBlockState(var1, SEA_LANTERN, var9, 4, 12, var3);
               var9 = 12;
            }

            var9 = 8;

            for(int var13 = 0; var13 < 2; ++var13) {
               this.fillWithBlocks(var1, var3, var9, 0, 7, var9, 2, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(var1, var3, var9, 0, 14, var9, 2, 14, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               var9 = 14;
            }

            this.fillWithBlocks(var1, var3, 8, 3, 8, 8, 3, 13, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(var1, var3, 14, 3, 8, 14, 3, 13, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.spawnElder(var1, var3, 11, 5, 13);
         }

         return true;
      }
   }

   static class XDoubleRoomFitHelper implements StructureOceanMonumentPieces.MonumentRoomFitHelper {
      private XDoubleRoomFitHelper() {
      }

      public boolean fits(StructureOceanMonumentPieces.RoomDefinition var1) {
         return var1.hasOpening[EnumFacing.EAST.getIndex()] && !var1.connections[EnumFacing.EAST.getIndex()].claimed;
      }

      public StructureOceanMonumentPieces.Piece create(EnumFacing var1, StructureOceanMonumentPieces.RoomDefinition var2, Random var3) {
         var2.claimed = true;
         var2.connections[EnumFacing.EAST.getIndex()].claimed = true;
         return new StructureOceanMonumentPieces.DoubleXRoom(var1, var2, var3);
      }
   }

   static class XYDoubleRoomFitHelper implements StructureOceanMonumentPieces.MonumentRoomFitHelper {
      private XYDoubleRoomFitHelper() {
      }

      public boolean fits(StructureOceanMonumentPieces.RoomDefinition var1) {
         if (var1.hasOpening[EnumFacing.EAST.getIndex()] && !var1.connections[EnumFacing.EAST.getIndex()].claimed && var1.hasOpening[EnumFacing.UP.getIndex()] && !var1.connections[EnumFacing.UP.getIndex()].claimed) {
            StructureOceanMonumentPieces.RoomDefinition var2 = var1.connections[EnumFacing.EAST.getIndex()];
            return var2.hasOpening[EnumFacing.UP.getIndex()] && !var2.connections[EnumFacing.UP.getIndex()].claimed;
         } else {
            return false;
         }
      }

      public StructureOceanMonumentPieces.Piece create(EnumFacing var1, StructureOceanMonumentPieces.RoomDefinition var2, Random var3) {
         var2.claimed = true;
         var2.connections[EnumFacing.EAST.getIndex()].claimed = true;
         var2.connections[EnumFacing.UP.getIndex()].claimed = true;
         var2.connections[EnumFacing.EAST.getIndex()].connections[EnumFacing.UP.getIndex()].claimed = true;
         return new StructureOceanMonumentPieces.DoubleXYRoom(var1, var2, var3);
      }
   }

   static class YDoubleRoomFitHelper implements StructureOceanMonumentPieces.MonumentRoomFitHelper {
      private YDoubleRoomFitHelper() {
      }

      public boolean fits(StructureOceanMonumentPieces.RoomDefinition var1) {
         return var1.hasOpening[EnumFacing.UP.getIndex()] && !var1.connections[EnumFacing.UP.getIndex()].claimed;
      }

      public StructureOceanMonumentPieces.Piece create(EnumFacing var1, StructureOceanMonumentPieces.RoomDefinition var2, Random var3) {
         var2.claimed = true;
         var2.connections[EnumFacing.UP.getIndex()].claimed = true;
         return new StructureOceanMonumentPieces.DoubleYRoom(var1, var2, var3);
      }
   }

   static class YZDoubleRoomFitHelper implements StructureOceanMonumentPieces.MonumentRoomFitHelper {
      private YZDoubleRoomFitHelper() {
      }

      public boolean fits(StructureOceanMonumentPieces.RoomDefinition var1) {
         if (var1.hasOpening[EnumFacing.NORTH.getIndex()] && !var1.connections[EnumFacing.NORTH.getIndex()].claimed && var1.hasOpening[EnumFacing.UP.getIndex()] && !var1.connections[EnumFacing.UP.getIndex()].claimed) {
            StructureOceanMonumentPieces.RoomDefinition var2 = var1.connections[EnumFacing.NORTH.getIndex()];
            return var2.hasOpening[EnumFacing.UP.getIndex()] && !var2.connections[EnumFacing.UP.getIndex()].claimed;
         } else {
            return false;
         }
      }

      public StructureOceanMonumentPieces.Piece create(EnumFacing var1, StructureOceanMonumentPieces.RoomDefinition var2, Random var3) {
         var2.claimed = true;
         var2.connections[EnumFacing.NORTH.getIndex()].claimed = true;
         var2.connections[EnumFacing.UP.getIndex()].claimed = true;
         var2.connections[EnumFacing.NORTH.getIndex()].connections[EnumFacing.UP.getIndex()].claimed = true;
         return new StructureOceanMonumentPieces.DoubleYZRoom(var1, var2, var3);
      }
   }

   static class ZDoubleRoomFitHelper implements StructureOceanMonumentPieces.MonumentRoomFitHelper {
      private ZDoubleRoomFitHelper() {
      }

      public boolean fits(StructureOceanMonumentPieces.RoomDefinition var1) {
         return var1.hasOpening[EnumFacing.NORTH.getIndex()] && !var1.connections[EnumFacing.NORTH.getIndex()].claimed;
      }

      public StructureOceanMonumentPieces.Piece create(EnumFacing var1, StructureOceanMonumentPieces.RoomDefinition var2, Random var3) {
         StructureOceanMonumentPieces.RoomDefinition var4 = var2;
         if (!var2.hasOpening[EnumFacing.NORTH.getIndex()] || var2.connections[EnumFacing.NORTH.getIndex()].claimed) {
            var4 = var2.connections[EnumFacing.SOUTH.getIndex()];
         }

         var4.claimed = true;
         var4.connections[EnumFacing.NORTH.getIndex()].claimed = true;
         return new StructureOceanMonumentPieces.DoubleZRoom(var1, var4, var3);
      }
   }
}
