package net.minecraft.world.gen.structure;

import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockRedstoneRepeater;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.BlockTripWire;
import net.minecraft.block.BlockTripWireHook;
import net.minecraft.block.BlockVine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.src.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.loot.LootTableList;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class ComponentScatteredFeaturePieces {
   public static void registerScatteredFeaturePieces() {
      MapGenStructureIO.registerStructureComponent(ComponentScatteredFeaturePieces.DesertPyramid.class, "TeDP");
      MapGenStructureIO.registerStructureComponent(ComponentScatteredFeaturePieces.JunglePyramid.class, "TeJP");
      MapGenStructureIO.registerStructureComponent(ComponentScatteredFeaturePieces.SwampHut.class, "TeSH");
      MapGenStructureIO.registerStructureComponent(ComponentScatteredFeaturePieces.Igloo.class, "Iglu");
   }

   public static class DesertPyramid extends ComponentScatteredFeaturePieces.Feature {
      private final boolean[] hasPlacedChest = new boolean[4];

      public DesertPyramid() {
      }

      public DesertPyramid(Random var1, int var2, int var3) {
         super(var1, var2, 64, var3, 21, 15, 21);
      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         super.writeStructureToNBT(var1);
         var1.setBoolean("hasPlacedChest0", this.hasPlacedChest[0]);
         var1.setBoolean("hasPlacedChest1", this.hasPlacedChest[1]);
         var1.setBoolean("hasPlacedChest2", this.hasPlacedChest[2]);
         var1.setBoolean("hasPlacedChest3", this.hasPlacedChest[3]);
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         super.readStructureFromNBT(var1);
         this.hasPlacedChest[0] = var1.getBoolean("hasPlacedChest0");
         this.hasPlacedChest[1] = var1.getBoolean("hasPlacedChest1");
         this.hasPlacedChest[2] = var1.getBoolean("hasPlacedChest2");
         this.hasPlacedChest[3] = var1.getBoolean("hasPlacedChest3");
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         this.fillWithBlocks(var1, var3, 0, -4, 0, this.scatteredFeatureSizeX - 1, 0, this.scatteredFeatureSizeZ - 1, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);

         for(int var4 = 1; var4 <= 9; ++var4) {
            this.fillWithBlocks(var1, var3, var4, var4, var4, this.scatteredFeatureSizeX - 1 - var4, var4, this.scatteredFeatureSizeZ - 1 - var4, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
            this.fillWithBlocks(var1, var3, var4 + 1, var4, var4 + 1, this.scatteredFeatureSizeX - 2 - var4, var4, this.scatteredFeatureSizeZ - 2 - var4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         }

         for(int var16 = 0; var16 < this.scatteredFeatureSizeX; ++var16) {
            for(int var5 = 0; var5 < this.scatteredFeatureSizeZ; ++var5) {
               this.replaceAirAndLiquidDownwards(var1, Blocks.SANDSTONE.getDefaultState(), var16, -5, var5, var3);
            }
         }

         IBlockState var17 = Blocks.SANDSTONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH);
         IBlockState var6 = Blocks.SANDSTONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH);
         IBlockState var7 = Blocks.SANDSTONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST);
         IBlockState var8 = Blocks.SANDSTONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.WEST);
         int var9 = ~EnumDyeColor.ORANGE.getDyeDamage() & 15;
         int var10 = ~EnumDyeColor.BLUE.getDyeDamage() & 15;
         this.fillWithBlocks(var1, var3, 0, 0, 0, 4, 9, 4, Blocks.SANDSTONE.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 1, 10, 1, 3, 10, 3, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
         this.setBlockState(var1, var17, 2, 10, 0, var3);
         this.setBlockState(var1, var6, 2, 10, 4, var3);
         this.setBlockState(var1, var7, 0, 10, 2, var3);
         this.setBlockState(var1, var8, 4, 10, 2, var3);
         this.fillWithBlocks(var1, var3, this.scatteredFeatureSizeX - 5, 0, 0, this.scatteredFeatureSizeX - 1, 9, 4, Blocks.SANDSTONE.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, this.scatteredFeatureSizeX - 4, 10, 1, this.scatteredFeatureSizeX - 2, 10, 3, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
         this.setBlockState(var1, var17, this.scatteredFeatureSizeX - 3, 10, 0, var3);
         this.setBlockState(var1, var6, this.scatteredFeatureSizeX - 3, 10, 4, var3);
         this.setBlockState(var1, var7, this.scatteredFeatureSizeX - 5, 10, 2, var3);
         this.setBlockState(var1, var8, this.scatteredFeatureSizeX - 1, 10, 2, var3);
         this.fillWithBlocks(var1, var3, 8, 0, 0, 12, 4, 4, Blocks.SANDSTONE.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 9, 1, 0, 11, 3, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 9, 1, 1, var3);
         this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 9, 2, 1, var3);
         this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 9, 3, 1, var3);
         this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 10, 3, 1, var3);
         this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 11, 3, 1, var3);
         this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 11, 2, 1, var3);
         this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 11, 1, 1, var3);
         this.fillWithBlocks(var1, var3, 4, 1, 1, 8, 3, 3, Blocks.SANDSTONE.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 4, 1, 2, 8, 2, 2, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 12, 1, 1, 16, 3, 3, Blocks.SANDSTONE.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 12, 1, 2, 16, 2, 2, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 5, 4, 5, this.scatteredFeatureSizeX - 6, 4, this.scatteredFeatureSizeZ - 6, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 9, 4, 9, 11, 4, 11, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 8, 1, 8, 8, 3, 8, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
         this.fillWithBlocks(var1, var3, 12, 1, 8, 12, 3, 8, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
         this.fillWithBlocks(var1, var3, 8, 1, 12, 8, 3, 12, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
         this.fillWithBlocks(var1, var3, 12, 1, 12, 12, 3, 12, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
         this.fillWithBlocks(var1, var3, 1, 1, 5, 4, 4, 11, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, this.scatteredFeatureSizeX - 5, 1, 5, this.scatteredFeatureSizeX - 2, 4, 11, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 6, 7, 9, 6, 7, 11, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, this.scatteredFeatureSizeX - 7, 7, 9, this.scatteredFeatureSizeX - 7, 7, 11, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 5, 5, 9, 5, 7, 11, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
         this.fillWithBlocks(var1, var3, this.scatteredFeatureSizeX - 6, 5, 9, this.scatteredFeatureSizeX - 6, 7, 11, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 5, 5, 10, var3);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 5, 6, 10, var3);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 6, 6, 10, var3);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), this.scatteredFeatureSizeX - 6, 5, 10, var3);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), this.scatteredFeatureSizeX - 6, 6, 10, var3);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), this.scatteredFeatureSizeX - 7, 6, 10, var3);
         this.fillWithBlocks(var1, var3, 2, 4, 4, 2, 6, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, this.scatteredFeatureSizeX - 3, 4, 4, this.scatteredFeatureSizeX - 3, 6, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.setBlockState(var1, var17, 2, 4, 5, var3);
         this.setBlockState(var1, var17, 2, 3, 4, var3);
         this.setBlockState(var1, var17, this.scatteredFeatureSizeX - 3, 4, 5, var3);
         this.setBlockState(var1, var17, this.scatteredFeatureSizeX - 3, 3, 4, var3);
         this.fillWithBlocks(var1, var3, 1, 1, 3, 2, 2, 3, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, this.scatteredFeatureSizeX - 3, 1, 3, this.scatteredFeatureSizeX - 2, 2, 3, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
         this.setBlockState(var1, Blocks.SANDSTONE.getDefaultState(), 1, 1, 2, var3);
         this.setBlockState(var1, Blocks.SANDSTONE.getDefaultState(), this.scatteredFeatureSizeX - 2, 1, 2, var3);
         this.setBlockState(var1, Blocks.STONE_SLAB.getStateFromMeta(BlockStoneSlab.EnumType.SAND.getMetadata()), 1, 2, 2, var3);
         this.setBlockState(var1, Blocks.STONE_SLAB.getStateFromMeta(BlockStoneSlab.EnumType.SAND.getMetadata()), this.scatteredFeatureSizeX - 2, 2, 2, var3);
         this.setBlockState(var1, var8, 2, 1, 2, var3);
         this.setBlockState(var1, var7, this.scatteredFeatureSizeX - 3, 1, 2, var3);
         this.fillWithBlocks(var1, var3, 4, 3, 5, 4, 3, 18, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, this.scatteredFeatureSizeX - 5, 3, 5, this.scatteredFeatureSizeX - 5, 3, 17, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 3, 1, 5, 4, 2, 16, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, this.scatteredFeatureSizeX - 6, 1, 5, this.scatteredFeatureSizeX - 5, 2, 16, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);

         for(int var11 = 5; var11 <= 17; var11 += 2) {
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 4, 1, var11, var3);
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), 4, 2, var11, var3);
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), this.scatteredFeatureSizeX - 5, 1, var11, var3);
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), this.scatteredFeatureSizeX - 5, 2, var11, var3);
         }

         this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), 10, 0, 7, var3);
         this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), 10, 0, 8, var3);
         this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), 9, 0, 9, var3);
         this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), 11, 0, 9, var3);
         this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), 8, 0, 10, var3);
         this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), 12, 0, 10, var3);
         this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), 7, 0, 10, var3);
         this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), 13, 0, 10, var3);
         this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), 9, 0, 11, var3);
         this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), 11, 0, 11, var3);
         this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), 10, 0, 12, var3);
         this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), 10, 0, 13, var3);
         this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var10), 10, 0, 10, var3);

         for(int var18 = 0; var18 <= this.scatteredFeatureSizeX - 1; var18 += this.scatteredFeatureSizeX - 1) {
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var18, 2, 1, var3);
            this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), var18, 2, 2, var3);
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var18, 2, 3, var3);
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var18, 3, 1, var3);
            this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), var18, 3, 2, var3);
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var18, 3, 3, var3);
            this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), var18, 4, 1, var3);
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), var18, 4, 2, var3);
            this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), var18, 4, 3, var3);
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var18, 5, 1, var3);
            this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), var18, 5, 2, var3);
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var18, 5, 3, var3);
            this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), var18, 6, 1, var3);
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), var18, 6, 2, var3);
            this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), var18, 6, 3, var3);
            this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), var18, 7, 1, var3);
            this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), var18, 7, 2, var3);
            this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), var18, 7, 3, var3);
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var18, 8, 1, var3);
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var18, 8, 2, var3);
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var18, 8, 3, var3);
         }

         for(int var19 = 2; var19 <= this.scatteredFeatureSizeX - 3; var19 += this.scatteredFeatureSizeX - 3 - 2) {
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var19 - 1, 2, 0, var3);
            this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), var19, 2, 0, var3);
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var19 + 1, 2, 0, var3);
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var19 - 1, 3, 0, var3);
            this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), var19, 3, 0, var3);
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var19 + 1, 3, 0, var3);
            this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), var19 - 1, 4, 0, var3);
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), var19, 4, 0, var3);
            this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), var19 + 1, 4, 0, var3);
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var19 - 1, 5, 0, var3);
            this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), var19, 5, 0, var3);
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var19 + 1, 5, 0, var3);
            this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), var19 - 1, 6, 0, var3);
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), var19, 6, 0, var3);
            this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), var19 + 1, 6, 0, var3);
            this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), var19 - 1, 7, 0, var3);
            this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), var19, 7, 0, var3);
            this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), var19 + 1, 7, 0, var3);
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var19 - 1, 8, 0, var3);
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var19, 8, 0, var3);
            this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var19 + 1, 8, 0, var3);
         }

         this.fillWithBlocks(var1, var3, 8, 4, 0, 12, 6, 0, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 8, 6, 0, var3);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 12, 6, 0, var3);
         this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), 9, 5, 0, var3);
         this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), 10, 5, 0, var3);
         this.setBlockState(var1, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(var9), 11, 5, 0, var3);
         this.fillWithBlocks(var1, var3, 8, -14, 8, 12, -11, 12, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
         this.fillWithBlocks(var1, var3, 8, -10, 8, 12, -10, 12, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), false);
         this.fillWithBlocks(var1, var3, 8, -9, 8, 12, -9, 12, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
         this.fillWithBlocks(var1, var3, 8, -8, 8, 12, -1, 12, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
         this.fillWithBlocks(var1, var3, 9, -11, 9, 11, -1, 11, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.setBlockState(var1, Blocks.STONE_PRESSURE_PLATE.getDefaultState(), 10, -11, 10, var3);
         this.fillWithBlocks(var1, var3, 9, -13, 9, 11, -13, 11, Blocks.TNT.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 8, -11, 10, var3);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 8, -10, 10, var3);
         this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), 7, -10, 10, var3);
         this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 7, -11, 10, var3);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 12, -11, 10, var3);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 12, -10, 10, var3);
         this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), 13, -10, 10, var3);
         this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 13, -11, 10, var3);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 10, -11, 8, var3);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 10, -10, 8, var3);
         this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), 10, -10, 7, var3);
         this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 10, -11, 7, var3);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 10, -11, 12, var3);
         this.setBlockState(var1, Blocks.AIR.getDefaultState(), 10, -10, 12, var3);
         this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), 10, -10, 13, var3);
         this.setBlockState(var1, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 10, -11, 13, var3);

         for(EnumFacing var13 : EnumFacing.Plane.HORIZONTAL) {
            if (!this.hasPlacedChest[var13.getHorizontalIndex()]) {
               int var14 = var13.getFrontOffsetX() * 2;
               int var15 = var13.getFrontOffsetZ() * 2;
               this.hasPlacedChest[var13.getHorizontalIndex()] = this.generateChest(var1, var3, var2, 10 + var14, -11, 10 + var15, LootTableList.CHESTS_DESERT_PYRAMID);
            }
         }

         return true;
      }
   }

   abstract static class Feature extends StructureComponent {
      protected int scatteredFeatureSizeX;
      protected int scatteredFeatureSizeY;
      protected int scatteredFeatureSizeZ;
      protected int horizontalPos = -1;

      public Feature() {
      }

      protected Feature(Random var1, int var2, int var3, int var4, int var5, int var6, int var7) {
         super(0);
         this.scatteredFeatureSizeX = var5;
         this.scatteredFeatureSizeY = var6;
         this.scatteredFeatureSizeZ = var7;
         this.setCoordBaseMode(EnumFacing.Plane.HORIZONTAL.random(var1));
         if (this.getCoordBaseMode().getAxis() == EnumFacing.Axis.Z) {
            this.boundingBox = new StructureBoundingBox(var2, var3, var4, var2 + var5 - 1, var3 + var6 - 1, var4 + var7 - 1);
         } else {
            this.boundingBox = new StructureBoundingBox(var2, var3, var4, var2 + var7 - 1, var3 + var6 - 1, var4 + var5 - 1);
         }

      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         var1.setInteger("Width", this.scatteredFeatureSizeX);
         var1.setInteger("Height", this.scatteredFeatureSizeY);
         var1.setInteger("Depth", this.scatteredFeatureSizeZ);
         var1.setInteger("HPos", this.horizontalPos);
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         this.scatteredFeatureSizeX = var1.getInteger("Width");
         this.scatteredFeatureSizeY = var1.getInteger("Height");
         this.scatteredFeatureSizeZ = var1.getInteger("Depth");
         this.horizontalPos = var1.getInteger("HPos");
      }

      protected boolean offsetToAverageGroundLevel(World var1, StructureBoundingBox var2, int var3) {
         if (this.horizontalPos >= 0) {
            return true;
         } else {
            int var4 = 0;
            int var5 = 0;
            BlockPos.MutableBlockPos var6 = new BlockPos.MutableBlockPos();

            for(int var7 = this.boundingBox.minZ; var7 <= this.boundingBox.maxZ; ++var7) {
               for(int var8 = this.boundingBox.minX; var8 <= this.boundingBox.maxX; ++var8) {
                  var6.setPos(var8, 64, var7);
                  if (var2.isVecInside(var6)) {
                     var4 += Math.max(var1.getTopSolidOrLiquidBlock(var6).getY(), var1.provider.getAverageGroundLevel());
                     ++var5;
                  }
               }
            }

            if (var5 == 0) {
               return false;
            } else {
               this.horizontalPos = var4 / var5;
               this.boundingBox.offset(0, this.horizontalPos - this.boundingBox.minY + var3, 0);
               return true;
            }
         }
      }
   }

   public static class Igloo extends ComponentScatteredFeaturePieces.Feature {
      private static final ResourceLocation IGLOO_TOP_ID = new ResourceLocation("igloo/igloo_top");
      private static final ResourceLocation IGLOO_MIDDLE_ID = new ResourceLocation("igloo/igloo_middle");
      private static final ResourceLocation IGLOO_BOTTOM_ID = new ResourceLocation("igloo/igloo_bottom");

      public Igloo() {
      }

      public Igloo(Random var1, int var2, int var3) {
         super(var1, var2, 64, var3, 7, 5, 8);
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (!this.offsetToAverageGroundLevel(var1, var3, -1)) {
            return false;
         } else {
            StructureBoundingBox var4 = this.getBoundingBox();
            BlockPos var5 = new BlockPos(var4.minX, var4.minY, var4.minZ);
            Rotation[] var6 = Rotation.values();
            MinecraftServer var7 = var1.getMinecraftServer();
            TemplateManager var8 = var1.getSaveHandler().getStructureTemplateManager();
            PlacementSettings var9 = (new PlacementSettings()).setRotation(var6[var2.nextInt(var6.length)]).setReplacedBlock(Blocks.STRUCTURE_VOID).setBoundingBox(var4);
            Template var10 = var8.a(var7, IGLOO_TOP_ID);
            var10.addBlocksToWorldChunk(var1, var5, var9);
            if (var2.nextDouble() < 0.5D) {
               Template var11 = var8.a(var7, IGLOO_MIDDLE_ID);
               Template var12 = var8.a(var7, IGLOO_BOTTOM_ID);
               int var13 = var2.nextInt(8) + 4;

               for(int var14 = 0; var14 < var13; ++var14) {
                  BlockPos var15 = var10.calculateConnectedPos(var9, new BlockPos(3, -1 - var14 * 3, 5), var9, new BlockPos(1, 2, 1));
                  var11.addBlocksToWorldChunk(var1, var5.add(var15), var9);
               }

               BlockPos var21 = var5.add(var10.calculateConnectedPos(var9, new BlockPos(3, -1 - var13 * 3, 5), var9, new BlockPos(3, 5, 7)));
               var12.addBlocksToWorldChunk(var1, var21, var9);
               Map var22 = var12.getDataBlocks(var21, var9);

               for(Entry var17 : var22.entrySet()) {
                  if ("chest".equals(var17.getValue())) {
                     BlockPos var18 = (BlockPos)var17.getKey();
                     var1.setBlockState(var18, Blocks.AIR.getDefaultState(), 3);
                     TileEntity var19 = var1.getTileEntity(var18.down());
                     if (var19 instanceof TileEntityChest) {
                        ((TileEntityChest)var19).setLootTable(LootTableList.CHESTS_IGLOO_CHEST, var2.nextLong());
                     }
                  }
               }
            } else {
               BlockPos var20 = Template.transformedBlockPos(var9, new BlockPos(3, 0, 5));
               var1.setBlockState(var5.add(var20), Blocks.SNOW.getDefaultState(), 3);
            }

            return true;
         }
      }
   }

   public static class JunglePyramid extends ComponentScatteredFeaturePieces.Feature {
      private boolean placedMainChest;
      private boolean placedHiddenChest;
      private boolean placedTrap1;
      private boolean placedTrap2;
      private static final ComponentScatteredFeaturePieces.JunglePyramid.WorldGenJungleTemplePiece junglePyramidsRandomScatteredStones = new ComponentScatteredFeaturePieces.JunglePyramid.WorldGenJungleTemplePiece((ComponentScatteredFeaturePieces.SyntheticClass_1)null);

      public JunglePyramid() {
      }

      public JunglePyramid(Random var1, int var2, int var3) {
         super(var1, var2, 64, var3, 12, 10, 15);
      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         super.writeStructureToNBT(var1);
         var1.setBoolean("placedMainChest", this.placedMainChest);
         var1.setBoolean("placedHiddenChest", this.placedHiddenChest);
         var1.setBoolean("placedTrap1", this.placedTrap1);
         var1.setBoolean("placedTrap2", this.placedTrap2);
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         super.readStructureFromNBT(var1);
         this.placedMainChest = var1.getBoolean("placedMainChest");
         this.placedHiddenChest = var1.getBoolean("placedHiddenChest");
         this.placedTrap1 = var1.getBoolean("placedTrap1");
         this.placedTrap2 = var1.getBoolean("placedTrap2");
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (!this.offsetToAverageGroundLevel(var1, var3, 0)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(var1, var3, 0, -4, 0, this.scatteredFeatureSizeX - 1, 0, this.scatteredFeatureSizeZ - 1, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 2, 1, 2, 9, 2, 2, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 2, 1, 12, 9, 2, 12, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 2, 1, 3, 2, 2, 11, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 9, 1, 3, 9, 2, 11, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 1, 3, 1, 10, 6, 1, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 1, 3, 13, 10, 6, 13, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 1, 3, 2, 1, 6, 12, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 10, 3, 2, 10, 6, 12, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 2, 3, 2, 9, 3, 12, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 2, 6, 2, 9, 6, 12, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 3, 7, 3, 8, 7, 11, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 4, 8, 4, 7, 8, 10, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithAir(var1, var3, 3, 1, 3, 8, 2, 11);
            this.fillWithAir(var1, var3, 4, 3, 6, 7, 3, 9);
            this.fillWithAir(var1, var3, 2, 4, 2, 9, 5, 12);
            this.fillWithAir(var1, var3, 4, 6, 5, 7, 6, 9);
            this.fillWithAir(var1, var3, 5, 7, 6, 6, 7, 8);
            this.fillWithAir(var1, var3, 5, 1, 2, 6, 2, 2);
            this.fillWithAir(var1, var3, 5, 2, 12, 6, 2, 12);
            this.fillWithAir(var1, var3, 5, 5, 1, 6, 5, 1);
            this.fillWithAir(var1, var3, 5, 5, 13, 6, 5, 13);
            this.setBlockState(var1, Blocks.AIR.getDefaultState(), 1, 5, 5, var3);
            this.setBlockState(var1, Blocks.AIR.getDefaultState(), 10, 5, 5, var3);
            this.setBlockState(var1, Blocks.AIR.getDefaultState(), 1, 5, 9, var3);
            this.setBlockState(var1, Blocks.AIR.getDefaultState(), 10, 5, 9, var3);

            for(int var4 = 0; var4 <= 14; var4 += 14) {
               this.fillWithRandomizedBlocks(var1, var3, 2, 4, var4, 2, 5, var4, false, var2, junglePyramidsRandomScatteredStones);
               this.fillWithRandomizedBlocks(var1, var3, 4, 4, var4, 4, 5, var4, false, var2, junglePyramidsRandomScatteredStones);
               this.fillWithRandomizedBlocks(var1, var3, 7, 4, var4, 7, 5, var4, false, var2, junglePyramidsRandomScatteredStones);
               this.fillWithRandomizedBlocks(var1, var3, 9, 4, var4, 9, 5, var4, false, var2, junglePyramidsRandomScatteredStones);
            }

            this.fillWithRandomizedBlocks(var1, var3, 5, 6, 0, 6, 6, 0, false, var2, junglePyramidsRandomScatteredStones);

            for(int var11 = 0; var11 <= 11; var11 += 11) {
               for(int var5 = 2; var5 <= 12; var5 += 2) {
                  this.fillWithRandomizedBlocks(var1, var3, var11, 4, var5, var11, 5, var5, false, var2, junglePyramidsRandomScatteredStones);
               }

               this.fillWithRandomizedBlocks(var1, var3, var11, 6, 5, var11, 6, 5, false, var2, junglePyramidsRandomScatteredStones);
               this.fillWithRandomizedBlocks(var1, var3, var11, 6, 9, var11, 6, 9, false, var2, junglePyramidsRandomScatteredStones);
            }

            this.fillWithRandomizedBlocks(var1, var3, 2, 7, 2, 2, 9, 2, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 9, 7, 2, 9, 9, 2, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 2, 7, 12, 2, 9, 12, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 9, 7, 12, 9, 9, 12, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 4, 9, 4, 4, 9, 4, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 7, 9, 4, 7, 9, 4, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 4, 9, 10, 4, 9, 10, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 7, 9, 10, 7, 9, 10, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 5, 9, 7, 6, 9, 7, false, var2, junglePyramidsRandomScatteredStones);
            IBlockState var12 = Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST);
            IBlockState var6 = Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.WEST);
            IBlockState var7 = Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH);
            IBlockState var8 = Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH);
            this.setBlockState(var1, var8, 5, 9, 6, var3);
            this.setBlockState(var1, var8, 6, 9, 6, var3);
            this.setBlockState(var1, var7, 5, 9, 8, var3);
            this.setBlockState(var1, var7, 6, 9, 8, var3);
            this.setBlockState(var1, var8, 4, 0, 0, var3);
            this.setBlockState(var1, var8, 5, 0, 0, var3);
            this.setBlockState(var1, var8, 6, 0, 0, var3);
            this.setBlockState(var1, var8, 7, 0, 0, var3);
            this.setBlockState(var1, var8, 4, 1, 8, var3);
            this.setBlockState(var1, var8, 4, 2, 9, var3);
            this.setBlockState(var1, var8, 4, 3, 10, var3);
            this.setBlockState(var1, var8, 7, 1, 8, var3);
            this.setBlockState(var1, var8, 7, 2, 9, var3);
            this.setBlockState(var1, var8, 7, 3, 10, var3);
            this.fillWithRandomizedBlocks(var1, var3, 4, 1, 9, 4, 1, 9, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 7, 1, 9, 7, 1, 9, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 4, 1, 10, 7, 2, 10, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 5, 4, 5, 6, 4, 5, false, var2, junglePyramidsRandomScatteredStones);
            this.setBlockState(var1, var12, 4, 4, 5, var3);
            this.setBlockState(var1, var6, 7, 4, 5, var3);

            for(int var9 = 0; var9 < 4; ++var9) {
               this.setBlockState(var1, var7, 5, 0 - var9, 6 + var9, var3);
               this.setBlockState(var1, var7, 6, 0 - var9, 6 + var9, var3);
               this.fillWithAir(var1, var3, 5, 0 - var9, 7 + var9, 6, 0 - var9, 9 + var9);
            }

            this.fillWithAir(var1, var3, 1, -3, 12, 10, -1, 13);
            this.fillWithAir(var1, var3, 1, -3, 1, 3, -1, 13);
            this.fillWithAir(var1, var3, 1, -3, 1, 9, -1, 5);

            for(int var13 = 1; var13 <= 13; var13 += 2) {
               this.fillWithRandomizedBlocks(var1, var3, 1, -3, var13, 1, -2, var13, false, var2, junglePyramidsRandomScatteredStones);
            }

            for(int var14 = 2; var14 <= 12; var14 += 2) {
               this.fillWithRandomizedBlocks(var1, var3, 1, -1, var14, 3, -1, var14, false, var2, junglePyramidsRandomScatteredStones);
            }

            this.fillWithRandomizedBlocks(var1, var3, 2, -2, 1, 5, -2, 1, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 7, -2, 1, 9, -2, 1, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 6, -3, 1, 6, -3, 1, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 6, -1, 1, 6, -1, 1, false, var2, junglePyramidsRandomScatteredStones);
            this.setBlockState(var1, Blocks.TRIPWIRE_HOOK.getDefaultState().withProperty(BlockTripWireHook.FACING, EnumFacing.EAST).withProperty(BlockTripWireHook.ATTACHED, Boolean.valueOf(true)), 1, -3, 8, var3);
            this.setBlockState(var1, Blocks.TRIPWIRE_HOOK.getDefaultState().withProperty(BlockTripWireHook.FACING, EnumFacing.WEST).withProperty(BlockTripWireHook.ATTACHED, Boolean.valueOf(true)), 4, -3, 8, var3);
            this.setBlockState(var1, Blocks.TRIPWIRE.getDefaultState().withProperty(BlockTripWire.ATTACHED, Boolean.valueOf(true)), 2, -3, 8, var3);
            this.setBlockState(var1, Blocks.TRIPWIRE.getDefaultState().withProperty(BlockTripWire.ATTACHED, Boolean.valueOf(true)), 3, -3, 8, var3);
            this.setBlockState(var1, Blocks.REDSTONE_WIRE.getDefaultState(), 5, -3, 7, var3);
            this.setBlockState(var1, Blocks.REDSTONE_WIRE.getDefaultState(), 5, -3, 6, var3);
            this.setBlockState(var1, Blocks.REDSTONE_WIRE.getDefaultState(), 5, -3, 5, var3);
            this.setBlockState(var1, Blocks.REDSTONE_WIRE.getDefaultState(), 5, -3, 4, var3);
            this.setBlockState(var1, Blocks.REDSTONE_WIRE.getDefaultState(), 5, -3, 3, var3);
            this.setBlockState(var1, Blocks.REDSTONE_WIRE.getDefaultState(), 5, -3, 2, var3);
            this.setBlockState(var1, Blocks.REDSTONE_WIRE.getDefaultState(), 5, -3, 1, var3);
            this.setBlockState(var1, Blocks.REDSTONE_WIRE.getDefaultState(), 4, -3, 1, var3);
            this.setBlockState(var1, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 3, -3, 1, var3);
            if (!this.placedTrap1) {
               this.placedTrap1 = this.createDispenser(var1, var3, var2, 3, -2, 1, EnumFacing.NORTH, LootTableList.CHESTS_JUNGLE_TEMPLE_DISPENSER);
            }

            this.setBlockState(var1, Blocks.VINE.getDefaultState().withProperty(BlockVine.SOUTH, Boolean.valueOf(true)), 3, -2, 2, var3);
            this.setBlockState(var1, Blocks.TRIPWIRE_HOOK.getDefaultState().withProperty(BlockTripWireHook.FACING, EnumFacing.NORTH).withProperty(BlockTripWireHook.ATTACHED, Boolean.valueOf(true)), 7, -3, 1, var3);
            this.setBlockState(var1, Blocks.TRIPWIRE_HOOK.getDefaultState().withProperty(BlockTripWireHook.FACING, EnumFacing.SOUTH).withProperty(BlockTripWireHook.ATTACHED, Boolean.valueOf(true)), 7, -3, 5, var3);
            this.setBlockState(var1, Blocks.TRIPWIRE.getDefaultState().withProperty(BlockTripWire.ATTACHED, Boolean.valueOf(true)), 7, -3, 2, var3);
            this.setBlockState(var1, Blocks.TRIPWIRE.getDefaultState().withProperty(BlockTripWire.ATTACHED, Boolean.valueOf(true)), 7, -3, 3, var3);
            this.setBlockState(var1, Blocks.TRIPWIRE.getDefaultState().withProperty(BlockTripWire.ATTACHED, Boolean.valueOf(true)), 7, -3, 4, var3);
            this.setBlockState(var1, Blocks.REDSTONE_WIRE.getDefaultState(), 8, -3, 6, var3);
            this.setBlockState(var1, Blocks.REDSTONE_WIRE.getDefaultState(), 9, -3, 6, var3);
            this.setBlockState(var1, Blocks.REDSTONE_WIRE.getDefaultState(), 9, -3, 5, var3);
            this.setBlockState(var1, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 9, -3, 4, var3);
            this.setBlockState(var1, Blocks.REDSTONE_WIRE.getDefaultState(), 9, -2, 4, var3);
            if (!this.placedTrap2) {
               this.placedTrap2 = this.createDispenser(var1, var3, var2, 9, -2, 3, EnumFacing.WEST, LootTableList.CHESTS_JUNGLE_TEMPLE_DISPENSER);
            }

            this.setBlockState(var1, Blocks.VINE.getDefaultState().withProperty(BlockVine.EAST, Boolean.valueOf(true)), 8, -1, 3, var3);
            this.setBlockState(var1, Blocks.VINE.getDefaultState().withProperty(BlockVine.EAST, Boolean.valueOf(true)), 8, -2, 3, var3);
            if (!this.placedMainChest) {
               this.placedMainChest = this.generateChest(var1, var3, var2, 8, -3, 3, LootTableList.CHESTS_JUNGLE_TEMPLE);
            }

            this.setBlockState(var1, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 9, -3, 2, var3);
            this.setBlockState(var1, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 8, -3, 1, var3);
            this.setBlockState(var1, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 4, -3, 5, var3);
            this.setBlockState(var1, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 5, -2, 5, var3);
            this.setBlockState(var1, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 5, -1, 5, var3);
            this.setBlockState(var1, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 6, -3, 5, var3);
            this.setBlockState(var1, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 7, -2, 5, var3);
            this.setBlockState(var1, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 7, -1, 5, var3);
            this.setBlockState(var1, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 8, -3, 5, var3);
            this.fillWithRandomizedBlocks(var1, var3, 9, -1, 1, 9, -1, 5, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithAir(var1, var3, 8, -3, 8, 10, -1, 10);
            this.setBlockState(var1, Blocks.STONEBRICK.getStateFromMeta(BlockStoneBrick.CHISELED_META), 8, -2, 11, var3);
            this.setBlockState(var1, Blocks.STONEBRICK.getStateFromMeta(BlockStoneBrick.CHISELED_META), 9, -2, 11, var3);
            this.setBlockState(var1, Blocks.STONEBRICK.getStateFromMeta(BlockStoneBrick.CHISELED_META), 10, -2, 11, var3);
            IBlockState var10 = Blocks.LEVER.getDefaultState().withProperty(BlockLever.FACING, BlockLever.EnumOrientation.NORTH);
            this.setBlockState(var1, var10, 8, -2, 12, var3);
            this.setBlockState(var1, var10, 9, -2, 12, var3);
            this.setBlockState(var1, var10, 10, -2, 12, var3);
            this.fillWithRandomizedBlocks(var1, var3, 8, -3, 8, 8, -3, 10, false, var2, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(var1, var3, 10, -3, 8, 10, -3, 10, false, var2, junglePyramidsRandomScatteredStones);
            this.setBlockState(var1, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 10, -2, 9, var3);
            this.setBlockState(var1, Blocks.REDSTONE_WIRE.getDefaultState(), 8, -2, 9, var3);
            this.setBlockState(var1, Blocks.REDSTONE_WIRE.getDefaultState(), 8, -2, 10, var3);
            this.setBlockState(var1, Blocks.REDSTONE_WIRE.getDefaultState(), 10, -1, 9, var3);
            this.setBlockState(var1, Blocks.STICKY_PISTON.getDefaultState().withProperty(BlockPistonBase.FACING, EnumFacing.UP), 9, -2, 8, var3);
            this.setBlockState(var1, Blocks.STICKY_PISTON.getDefaultState().withProperty(BlockPistonBase.FACING, EnumFacing.WEST), 10, -2, 8, var3);
            this.setBlockState(var1, Blocks.STICKY_PISTON.getDefaultState().withProperty(BlockPistonBase.FACING, EnumFacing.WEST), 10, -1, 8, var3);
            this.setBlockState(var1, Blocks.UNPOWERED_REPEATER.getDefaultState().withProperty(BlockRedstoneRepeater.FACING, EnumFacing.NORTH), 10, -2, 10, var3);
            if (!this.placedHiddenChest) {
               this.placedHiddenChest = this.generateChest(var1, var3, var2, 9, -3, 10, LootTableList.CHESTS_JUNGLE_TEMPLE);
            }

            return true;
         }
      }

      static class WorldGenJungleTemplePiece extends StructureComponent.BlockSelector {
         private WorldGenJungleTemplePiece() {
         }

         public void selectBlocks(Random var1, int var2, int var3, int var4, boolean var5) {
            if (var1.nextFloat() < 0.4F) {
               this.blockstate = Blocks.COBBLESTONE.getDefaultState();
            } else {
               this.blockstate = Blocks.MOSSY_COBBLESTONE.getDefaultState();
            }

         }

         WorldGenJungleTemplePiece(ComponentScatteredFeaturePieces.SyntheticClass_1 var1) {
            this();
         }
      }
   }

   public static class SwampHut extends ComponentScatteredFeaturePieces.Feature {
      private boolean hasWitch;

      public SwampHut() {
      }

      public SwampHut(Random var1, int var2, int var3) {
         super(var1, var2, 64, var3, 7, 7, 9);
      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         super.writeStructureToNBT(var1);
         var1.setBoolean("Witch", this.hasWitch);
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         super.readStructureFromNBT(var1);
         this.hasWitch = var1.getBoolean("Witch");
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (!this.offsetToAverageGroundLevel(var1, var3, 0)) {
            return false;
         } else {
            this.fillWithBlocks(var1, var3, 1, 1, 1, 5, 1, 7, Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), false);
            this.fillWithBlocks(var1, var3, 1, 4, 2, 5, 4, 7, Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), false);
            this.fillWithBlocks(var1, var3, 2, 1, 0, 4, 1, 0, Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), false);
            this.fillWithBlocks(var1, var3, 2, 2, 2, 3, 3, 2, Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), false);
            this.fillWithBlocks(var1, var3, 1, 2, 3, 1, 3, 6, Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), false);
            this.fillWithBlocks(var1, var3, 5, 2, 3, 5, 3, 6, Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), false);
            this.fillWithBlocks(var1, var3, 2, 2, 7, 4, 3, 7, Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), false);
            this.fillWithBlocks(var1, var3, 1, 0, 2, 1, 3, 2, Blocks.LOG.getDefaultState(), Blocks.LOG.getDefaultState(), false);
            this.fillWithBlocks(var1, var3, 5, 0, 2, 5, 3, 2, Blocks.LOG.getDefaultState(), Blocks.LOG.getDefaultState(), false);
            this.fillWithBlocks(var1, var3, 1, 0, 7, 1, 3, 7, Blocks.LOG.getDefaultState(), Blocks.LOG.getDefaultState(), false);
            this.fillWithBlocks(var1, var3, 5, 0, 7, 5, 3, 7, Blocks.LOG.getDefaultState(), Blocks.LOG.getDefaultState(), false);
            this.setBlockState(var1, Blocks.OAK_FENCE.getDefaultState(), 2, 3, 2, var3);
            this.setBlockState(var1, Blocks.OAK_FENCE.getDefaultState(), 3, 3, 7, var3);
            this.setBlockState(var1, Blocks.AIR.getDefaultState(), 1, 3, 4, var3);
            this.setBlockState(var1, Blocks.AIR.getDefaultState(), 5, 3, 4, var3);
            this.setBlockState(var1, Blocks.AIR.getDefaultState(), 5, 3, 5, var3);
            this.setBlockState(var1, Blocks.FLOWER_POT.getDefaultState().withProperty(BlockFlowerPot.CONTENTS, BlockFlowerPot.EnumFlowerType.MUSHROOM_RED), 1, 3, 5, var3);
            this.setBlockState(var1, Blocks.CRAFTING_TABLE.getDefaultState(), 3, 2, 6, var3);
            this.setBlockState(var1, Blocks.CAULDRON.getDefaultState(), 4, 2, 6, var3);
            this.setBlockState(var1, Blocks.OAK_FENCE.getDefaultState(), 1, 2, 1, var3);
            this.setBlockState(var1, Blocks.OAK_FENCE.getDefaultState(), 5, 2, 1, var3);
            IBlockState var4 = Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH);
            IBlockState var5 = Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST);
            IBlockState var6 = Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.WEST);
            IBlockState var7 = Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH);
            this.fillWithBlocks(var1, var3, 0, 4, 1, 6, 4, 1, var4, var4, false);
            this.fillWithBlocks(var1, var3, 0, 4, 2, 0, 4, 7, var5, var5, false);
            this.fillWithBlocks(var1, var3, 6, 4, 2, 6, 4, 7, var6, var6, false);
            this.fillWithBlocks(var1, var3, 0, 4, 8, 6, 4, 8, var7, var7, false);

            for(int var8 = 2; var8 <= 7; var8 += 5) {
               for(int var9 = 1; var9 <= 5; var9 += 4) {
                  this.replaceAirAndLiquidDownwards(var1, Blocks.LOG.getDefaultState(), var9, -1, var8, var3);
               }
            }

            if (!this.hasWitch) {
               int var12 = this.getXWithOffset(2, 5);
               int var13 = this.getYWithOffset(2);
               int var10 = this.getZWithOffset(2, 5);
               if (var3.isVecInside(new BlockPos(var12, var13, var10))) {
                  this.hasWitch = true;
                  EntityWitch var11 = new EntityWitch(var1);
                  var11.setLocationAndAngles((double)var12 + 0.5D, (double)var13, (double)var10 + 0.5D, 0.0F, 0.0F);
                  var11.onInitialSpawn(var1.getDifficultyForLocation(new BlockPos(var12, var13, var10)), (IEntityLivingData)null);
                  var1.addEntity(var11, SpawnReason.CHUNK_GEN);
               }
            }

            return true;
         }
      }
   }

   static class SyntheticClass_1 {
   }
}
