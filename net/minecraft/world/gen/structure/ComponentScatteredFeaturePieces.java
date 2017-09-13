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

      public DesertPyramid(Random random, int i, int j) {
         super(random, i, 64, j, 21, 15, 21);
      }

      protected void writeStructureToNBT(NBTTagCompound nbttagcompound) {
         super.writeStructureToNBT(nbttagcompound);
         nbttagcompound.setBoolean("hasPlacedChest0", this.hasPlacedChest[0]);
         nbttagcompound.setBoolean("hasPlacedChest1", this.hasPlacedChest[1]);
         nbttagcompound.setBoolean("hasPlacedChest2", this.hasPlacedChest[2]);
         nbttagcompound.setBoolean("hasPlacedChest3", this.hasPlacedChest[3]);
      }

      protected void readStructureFromNBT(NBTTagCompound nbttagcompound) {
         super.readStructureFromNBT(nbttagcompound);
         this.hasPlacedChest[0] = nbttagcompound.getBoolean("hasPlacedChest0");
         this.hasPlacedChest[1] = nbttagcompound.getBoolean("hasPlacedChest1");
         this.hasPlacedChest[2] = nbttagcompound.getBoolean("hasPlacedChest2");
         this.hasPlacedChest[3] = nbttagcompound.getBoolean("hasPlacedChest3");
      }

      public boolean addComponentParts(World world, Random random, StructureBoundingBox structureboundingbox) {
         this.fillWithBlocks(world, structureboundingbox, 0, -4, 0, this.scatteredFeatureSizeX - 1, 0, this.scatteredFeatureSizeZ - 1, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);

         for(int i = 1; i <= 9; ++i) {
            this.fillWithBlocks(world, structureboundingbox, i, i, i, this.scatteredFeatureSizeX - 1 - i, i, this.scatteredFeatureSizeZ - 1 - i, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
            this.fillWithBlocks(world, structureboundingbox, i + 1, i, i + 1, this.scatteredFeatureSizeX - 2 - i, i, this.scatteredFeatureSizeZ - 2 - i, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         }

         for(int var16 = 0; var16 < this.scatteredFeatureSizeX; ++var16) {
            for(int j = 0; j < this.scatteredFeatureSizeZ; ++j) {
               this.replaceAirAndLiquidDownwards(world, Blocks.SANDSTONE.getDefaultState(), var16, -5, j, structureboundingbox);
            }
         }

         IBlockState iblockdata = Blocks.SANDSTONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH);
         IBlockState iblockdata1 = Blocks.SANDSTONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH);
         IBlockState iblockdata2 = Blocks.SANDSTONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST);
         IBlockState iblockdata3 = Blocks.SANDSTONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.WEST);
         int k = ~EnumDyeColor.ORANGE.getDyeDamage() & 15;
         int l = ~EnumDyeColor.BLUE.getDyeDamage() & 15;
         this.fillWithBlocks(world, structureboundingbox, 0, 0, 0, 4, 9, 4, Blocks.SANDSTONE.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(world, structureboundingbox, 1, 10, 1, 3, 10, 3, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
         this.setBlockState(world, iblockdata, 2, 10, 0, structureboundingbox);
         this.setBlockState(world, iblockdata1, 2, 10, 4, structureboundingbox);
         this.setBlockState(world, iblockdata2, 0, 10, 2, structureboundingbox);
         this.setBlockState(world, iblockdata3, 4, 10, 2, structureboundingbox);
         this.fillWithBlocks(world, structureboundingbox, this.scatteredFeatureSizeX - 5, 0, 0, this.scatteredFeatureSizeX - 1, 9, 4, Blocks.SANDSTONE.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(world, structureboundingbox, this.scatteredFeatureSizeX - 4, 10, 1, this.scatteredFeatureSizeX - 2, 10, 3, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
         this.setBlockState(world, iblockdata, this.scatteredFeatureSizeX - 3, 10, 0, structureboundingbox);
         this.setBlockState(world, iblockdata1, this.scatteredFeatureSizeX - 3, 10, 4, structureboundingbox);
         this.setBlockState(world, iblockdata2, this.scatteredFeatureSizeX - 5, 10, 2, structureboundingbox);
         this.setBlockState(world, iblockdata3, this.scatteredFeatureSizeX - 1, 10, 2, structureboundingbox);
         this.fillWithBlocks(world, structureboundingbox, 8, 0, 0, 12, 4, 4, Blocks.SANDSTONE.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(world, structureboundingbox, 9, 1, 0, 11, 3, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 9, 1, 1, structureboundingbox);
         this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 9, 2, 1, structureboundingbox);
         this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 9, 3, 1, structureboundingbox);
         this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 10, 3, 1, structureboundingbox);
         this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 11, 3, 1, structureboundingbox);
         this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 11, 2, 1, structureboundingbox);
         this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 11, 1, 1, structureboundingbox);
         this.fillWithBlocks(world, structureboundingbox, 4, 1, 1, 8, 3, 3, Blocks.SANDSTONE.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(world, structureboundingbox, 4, 1, 2, 8, 2, 2, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(world, structureboundingbox, 12, 1, 1, 16, 3, 3, Blocks.SANDSTONE.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(world, structureboundingbox, 12, 1, 2, 16, 2, 2, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(world, structureboundingbox, 5, 4, 5, this.scatteredFeatureSizeX - 6, 4, this.scatteredFeatureSizeZ - 6, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
         this.fillWithBlocks(world, structureboundingbox, 9, 4, 9, 11, 4, 11, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(world, structureboundingbox, 8, 1, 8, 8, 3, 8, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
         this.fillWithBlocks(world, structureboundingbox, 12, 1, 8, 12, 3, 8, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
         this.fillWithBlocks(world, structureboundingbox, 8, 1, 12, 8, 3, 12, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
         this.fillWithBlocks(world, structureboundingbox, 12, 1, 12, 12, 3, 12, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
         this.fillWithBlocks(world, structureboundingbox, 1, 1, 5, 4, 4, 11, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
         this.fillWithBlocks(world, structureboundingbox, this.scatteredFeatureSizeX - 5, 1, 5, this.scatteredFeatureSizeX - 2, 4, 11, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
         this.fillWithBlocks(world, structureboundingbox, 6, 7, 9, 6, 7, 11, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
         this.fillWithBlocks(world, structureboundingbox, this.scatteredFeatureSizeX - 7, 7, 9, this.scatteredFeatureSizeX - 7, 7, 11, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
         this.fillWithBlocks(world, structureboundingbox, 5, 5, 9, 5, 7, 11, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
         this.fillWithBlocks(world, structureboundingbox, this.scatteredFeatureSizeX - 6, 5, 9, this.scatteredFeatureSizeX - 6, 7, 11, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
         this.setBlockState(world, Blocks.AIR.getDefaultState(), 5, 5, 10, structureboundingbox);
         this.setBlockState(world, Blocks.AIR.getDefaultState(), 5, 6, 10, structureboundingbox);
         this.setBlockState(world, Blocks.AIR.getDefaultState(), 6, 6, 10, structureboundingbox);
         this.setBlockState(world, Blocks.AIR.getDefaultState(), this.scatteredFeatureSizeX - 6, 5, 10, structureboundingbox);
         this.setBlockState(world, Blocks.AIR.getDefaultState(), this.scatteredFeatureSizeX - 6, 6, 10, structureboundingbox);
         this.setBlockState(world, Blocks.AIR.getDefaultState(), this.scatteredFeatureSizeX - 7, 6, 10, structureboundingbox);
         this.fillWithBlocks(world, structureboundingbox, 2, 4, 4, 2, 6, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(world, structureboundingbox, this.scatteredFeatureSizeX - 3, 4, 4, this.scatteredFeatureSizeX - 3, 6, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.setBlockState(world, iblockdata, 2, 4, 5, structureboundingbox);
         this.setBlockState(world, iblockdata, 2, 3, 4, structureboundingbox);
         this.setBlockState(world, iblockdata, this.scatteredFeatureSizeX - 3, 4, 5, structureboundingbox);
         this.setBlockState(world, iblockdata, this.scatteredFeatureSizeX - 3, 3, 4, structureboundingbox);
         this.fillWithBlocks(world, structureboundingbox, 1, 1, 3, 2, 2, 3, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
         this.fillWithBlocks(world, structureboundingbox, this.scatteredFeatureSizeX - 3, 1, 3, this.scatteredFeatureSizeX - 2, 2, 3, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
         this.setBlockState(world, Blocks.SANDSTONE.getDefaultState(), 1, 1, 2, structureboundingbox);
         this.setBlockState(world, Blocks.SANDSTONE.getDefaultState(), this.scatteredFeatureSizeX - 2, 1, 2, structureboundingbox);
         this.setBlockState(world, Blocks.STONE_SLAB.getStateFromMeta(BlockStoneSlab.EnumType.SAND.getMetadata()), 1, 2, 2, structureboundingbox);
         this.setBlockState(world, Blocks.STONE_SLAB.getStateFromMeta(BlockStoneSlab.EnumType.SAND.getMetadata()), this.scatteredFeatureSizeX - 2, 2, 2, structureboundingbox);
         this.setBlockState(world, iblockdata3, 2, 1, 2, structureboundingbox);
         this.setBlockState(world, iblockdata2, this.scatteredFeatureSizeX - 3, 1, 2, structureboundingbox);
         this.fillWithBlocks(world, structureboundingbox, 4, 3, 5, 4, 3, 18, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
         this.fillWithBlocks(world, structureboundingbox, this.scatteredFeatureSizeX - 5, 3, 5, this.scatteredFeatureSizeX - 5, 3, 17, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
         this.fillWithBlocks(world, structureboundingbox, 3, 1, 5, 4, 2, 16, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.fillWithBlocks(world, structureboundingbox, this.scatteredFeatureSizeX - 6, 1, 5, this.scatteredFeatureSizeX - 5, 2, 16, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);

         for(int i1 = 5; i1 <= 17; i1 += 2) {
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 4, 1, i1, structureboundingbox);
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), 4, 2, i1, structureboundingbox);
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), this.scatteredFeatureSizeX - 5, 1, i1, structureboundingbox);
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), this.scatteredFeatureSizeX - 5, 2, i1, structureboundingbox);
         }

         this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), 10, 0, 7, structureboundingbox);
         this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), 10, 0, 8, structureboundingbox);
         this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), 9, 0, 9, structureboundingbox);
         this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), 11, 0, 9, structureboundingbox);
         this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), 8, 0, 10, structureboundingbox);
         this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), 12, 0, 10, structureboundingbox);
         this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), 7, 0, 10, structureboundingbox);
         this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), 13, 0, 10, structureboundingbox);
         this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), 9, 0, 11, structureboundingbox);
         this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), 11, 0, 11, structureboundingbox);
         this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), 10, 0, 12, structureboundingbox);
         this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), 10, 0, 13, structureboundingbox);
         this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(l), 10, 0, 10, structureboundingbox);

         for(int var18 = 0; var18 <= this.scatteredFeatureSizeX - 1; var18 += this.scatteredFeatureSizeX - 1) {
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var18, 2, 1, structureboundingbox);
            this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), var18, 2, 2, structureboundingbox);
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var18, 2, 3, structureboundingbox);
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var18, 3, 1, structureboundingbox);
            this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), var18, 3, 2, structureboundingbox);
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var18, 3, 3, structureboundingbox);
            this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), var18, 4, 1, structureboundingbox);
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), var18, 4, 2, structureboundingbox);
            this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), var18, 4, 3, structureboundingbox);
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var18, 5, 1, structureboundingbox);
            this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), var18, 5, 2, structureboundingbox);
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var18, 5, 3, structureboundingbox);
            this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), var18, 6, 1, structureboundingbox);
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), var18, 6, 2, structureboundingbox);
            this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), var18, 6, 3, structureboundingbox);
            this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), var18, 7, 1, structureboundingbox);
            this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), var18, 7, 2, structureboundingbox);
            this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), var18, 7, 3, structureboundingbox);
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var18, 8, 1, structureboundingbox);
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var18, 8, 2, structureboundingbox);
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var18, 8, 3, structureboundingbox);
         }

         for(int var19 = 2; var19 <= this.scatteredFeatureSizeX - 3; var19 += this.scatteredFeatureSizeX - 3 - 2) {
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var19 - 1, 2, 0, structureboundingbox);
            this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), var19, 2, 0, structureboundingbox);
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var19 + 1, 2, 0, structureboundingbox);
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var19 - 1, 3, 0, structureboundingbox);
            this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), var19, 3, 0, structureboundingbox);
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var19 + 1, 3, 0, structureboundingbox);
            this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), var19 - 1, 4, 0, structureboundingbox);
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), var19, 4, 0, structureboundingbox);
            this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), var19 + 1, 4, 0, structureboundingbox);
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var19 - 1, 5, 0, structureboundingbox);
            this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), var19, 5, 0, structureboundingbox);
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var19 + 1, 5, 0, structureboundingbox);
            this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), var19 - 1, 6, 0, structureboundingbox);
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), var19, 6, 0, structureboundingbox);
            this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), var19 + 1, 6, 0, structureboundingbox);
            this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), var19 - 1, 7, 0, structureboundingbox);
            this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), var19, 7, 0, structureboundingbox);
            this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), var19 + 1, 7, 0, structureboundingbox);
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var19 - 1, 8, 0, structureboundingbox);
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var19, 8, 0, structureboundingbox);
            this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), var19 + 1, 8, 0, structureboundingbox);
         }

         this.fillWithBlocks(world, structureboundingbox, 8, 4, 0, 12, 6, 0, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
         this.setBlockState(world, Blocks.AIR.getDefaultState(), 8, 6, 0, structureboundingbox);
         this.setBlockState(world, Blocks.AIR.getDefaultState(), 12, 6, 0, structureboundingbox);
         this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), 9, 5, 0, structureboundingbox);
         this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), 10, 5, 0, structureboundingbox);
         this.setBlockState(world, Blocks.STAINED_HARDENED_CLAY.getStateFromMeta(k), 11, 5, 0, structureboundingbox);
         this.fillWithBlocks(world, structureboundingbox, 8, -14, 8, 12, -11, 12, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
         this.fillWithBlocks(world, structureboundingbox, 8, -10, 8, 12, -10, 12, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), false);
         this.fillWithBlocks(world, structureboundingbox, 8, -9, 8, 12, -9, 12, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), false);
         this.fillWithBlocks(world, structureboundingbox, 8, -8, 8, 12, -1, 12, Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE.getDefaultState(), false);
         this.fillWithBlocks(world, structureboundingbox, 9, -11, 9, 11, -1, 11, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.setBlockState(world, Blocks.STONE_PRESSURE_PLATE.getDefaultState(), 10, -11, 10, structureboundingbox);
         this.fillWithBlocks(world, structureboundingbox, 9, -13, 9, 11, -13, 11, Blocks.TNT.getDefaultState(), Blocks.AIR.getDefaultState(), false);
         this.setBlockState(world, Blocks.AIR.getDefaultState(), 8, -11, 10, structureboundingbox);
         this.setBlockState(world, Blocks.AIR.getDefaultState(), 8, -10, 10, structureboundingbox);
         this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), 7, -10, 10, structureboundingbox);
         this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 7, -11, 10, structureboundingbox);
         this.setBlockState(world, Blocks.AIR.getDefaultState(), 12, -11, 10, structureboundingbox);
         this.setBlockState(world, Blocks.AIR.getDefaultState(), 12, -10, 10, structureboundingbox);
         this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), 13, -10, 10, structureboundingbox);
         this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 13, -11, 10, structureboundingbox);
         this.setBlockState(world, Blocks.AIR.getDefaultState(), 10, -11, 8, structureboundingbox);
         this.setBlockState(world, Blocks.AIR.getDefaultState(), 10, -10, 8, structureboundingbox);
         this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), 10, -10, 7, structureboundingbox);
         this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 10, -11, 7, structureboundingbox);
         this.setBlockState(world, Blocks.AIR.getDefaultState(), 10, -11, 12, structureboundingbox);
         this.setBlockState(world, Blocks.AIR.getDefaultState(), 10, -10, 12, structureboundingbox);
         this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.CHISELED.getMetadata()), 10, -10, 13, structureboundingbox);
         this.setBlockState(world, Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata()), 10, -11, 13, structureboundingbox);

         for(EnumFacing enumdirection : EnumFacing.Plane.HORIZONTAL) {
            if (!this.hasPlacedChest[enumdirection.getHorizontalIndex()]) {
               int j1 = enumdirection.getFrontOffsetX() * 2;
               int k1 = enumdirection.getFrontOffsetZ() * 2;
               this.hasPlacedChest[enumdirection.getHorizontalIndex()] = this.generateChest(world, structureboundingbox, random, 10 + j1, -11, 10 + k1, LootTableList.CHESTS_DESERT_PYRAMID);
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

      protected Feature(Random random, int i, int j, int k, int l, int i1, int j1) {
         super(0);
         this.scatteredFeatureSizeX = l;
         this.scatteredFeatureSizeY = i1;
         this.scatteredFeatureSizeZ = j1;
         this.setCoordBaseMode(EnumFacing.Plane.HORIZONTAL.random(random));
         if (this.getCoordBaseMode().getAxis() == EnumFacing.Axis.Z) {
            this.boundingBox = new StructureBoundingBox(i, j, k, i + l - 1, j + i1 - 1, k + j1 - 1);
         } else {
            this.boundingBox = new StructureBoundingBox(i, j, k, i + j1 - 1, j + i1 - 1, k + l - 1);
         }

      }

      protected void writeStructureToNBT(NBTTagCompound nbttagcompound) {
         nbttagcompound.setInteger("Width", this.scatteredFeatureSizeX);
         nbttagcompound.setInteger("Height", this.scatteredFeatureSizeY);
         nbttagcompound.setInteger("Depth", this.scatteredFeatureSizeZ);
         nbttagcompound.setInteger("HPos", this.horizontalPos);
      }

      protected void readStructureFromNBT(NBTTagCompound nbttagcompound) {
         this.scatteredFeatureSizeX = nbttagcompound.getInteger("Width");
         this.scatteredFeatureSizeY = nbttagcompound.getInteger("Height");
         this.scatteredFeatureSizeZ = nbttagcompound.getInteger("Depth");
         this.horizontalPos = nbttagcompound.getInteger("HPos");
      }

      protected boolean offsetToAverageGroundLevel(World world, StructureBoundingBox structureboundingbox, int i) {
         if (this.horizontalPos >= 0) {
            return true;
         } else {
            int j = 0;
            int k = 0;
            BlockPos.MutableBlockPos blockposition_mutableblockposition = new BlockPos.MutableBlockPos();

            for(int l = this.boundingBox.minZ; l <= this.boundingBox.maxZ; ++l) {
               for(int i1 = this.boundingBox.minX; i1 <= this.boundingBox.maxX; ++i1) {
                  blockposition_mutableblockposition.setPos(i1, 64, l);
                  if (structureboundingbox.isVecInside(blockposition_mutableblockposition)) {
                     j += Math.max(world.getTopSolidOrLiquidBlock(blockposition_mutableblockposition).getY(), world.provider.getAverageGroundLevel());
                     ++k;
                  }
               }
            }

            if (k == 0) {
               return false;
            } else {
               this.horizontalPos = j / k;
               this.boundingBox.offset(0, this.horizontalPos - this.boundingBox.minY + i, 0);
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

      public Igloo(Random random, int i, int j) {
         super(random, i, 64, j, 7, 5, 8);
      }

      public boolean addComponentParts(World world, Random random, StructureBoundingBox structureboundingbox) {
         if (!this.offsetToAverageGroundLevel(world, structureboundingbox, -1)) {
            return false;
         } else {
            StructureBoundingBox structureboundingbox1 = this.getBoundingBox();
            BlockPos blockposition = new BlockPos(structureboundingbox1.minX, structureboundingbox1.minY, structureboundingbox1.minZ);
            Rotation[] aenumblockrotation = Rotation.values();
            MinecraftServer minecraftserver = world.getMinecraftServer();
            TemplateManager definedstructuremanager = world.getSaveHandler().getStructureTemplateManager();
            PlacementSettings definedstructureinfo = (new PlacementSettings()).setRotation(aenumblockrotation[random.nextInt(aenumblockrotation.length)]).setReplacedBlock(Blocks.STRUCTURE_VOID).setBoundingBox(structureboundingbox1);
            Template definedstructure = definedstructuremanager.a(minecraftserver, IGLOO_TOP_ID);
            definedstructure.addBlocksToWorldChunk(world, blockposition, definedstructureinfo);
            if (random.nextDouble() < 0.5D) {
               Template definedstructure1 = definedstructuremanager.a(minecraftserver, IGLOO_MIDDLE_ID);
               Template definedstructure2 = definedstructuremanager.a(minecraftserver, IGLOO_BOTTOM_ID);
               int i = random.nextInt(8) + 4;

               for(int j = 0; j < i; ++j) {
                  BlockPos blockposition1 = definedstructure.calculateConnectedPos(definedstructureinfo, new BlockPos(3, -1 - j * 3, 5), definedstructureinfo, new BlockPos(1, 2, 1));
                  definedstructure1.addBlocksToWorldChunk(world, blockposition.add(blockposition1), definedstructureinfo);
               }

               BlockPos blockposition2 = blockposition.add(definedstructure.calculateConnectedPos(definedstructureinfo, new BlockPos(3, -1 - i * 3, 5), definedstructureinfo, new BlockPos(3, 5, 7)));
               definedstructure2.addBlocksToWorldChunk(world, blockposition2, definedstructureinfo);
               Map map = definedstructure2.getDataBlocks(blockposition2, definedstructureinfo);

               for(Entry entry : map.entrySet()) {
                  if ("chest".equals(entry.getValue())) {
                     BlockPos blockposition3 = (BlockPos)entry.getKey();
                     world.setBlockState(blockposition3, Blocks.AIR.getDefaultState(), 3);
                     TileEntity tileentity = world.getTileEntity(blockposition3.down());
                     if (tileentity instanceof TileEntityChest) {
                        ((TileEntityChest)tileentity).setLootTable(LootTableList.CHESTS_IGLOO_CHEST, random.nextLong());
                     }
                  }
               }
            } else {
               BlockPos blockposition4 = Template.transformedBlockPos(definedstructureinfo, new BlockPos(3, 0, 5));
               world.setBlockState(blockposition.add(blockposition4), Blocks.SNOW.getDefaultState(), 3);
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

      public JunglePyramid(Random random, int i, int j) {
         super(random, i, 64, j, 12, 10, 15);
      }

      protected void writeStructureToNBT(NBTTagCompound nbttagcompound) {
         super.writeStructureToNBT(nbttagcompound);
         nbttagcompound.setBoolean("placedMainChest", this.placedMainChest);
         nbttagcompound.setBoolean("placedHiddenChest", this.placedHiddenChest);
         nbttagcompound.setBoolean("placedTrap1", this.placedTrap1);
         nbttagcompound.setBoolean("placedTrap2", this.placedTrap2);
      }

      protected void readStructureFromNBT(NBTTagCompound nbttagcompound) {
         super.readStructureFromNBT(nbttagcompound);
         this.placedMainChest = nbttagcompound.getBoolean("placedMainChest");
         this.placedHiddenChest = nbttagcompound.getBoolean("placedHiddenChest");
         this.placedTrap1 = nbttagcompound.getBoolean("placedTrap1");
         this.placedTrap2 = nbttagcompound.getBoolean("placedTrap2");
      }

      public boolean addComponentParts(World world, Random random, StructureBoundingBox structureboundingbox) {
         if (!this.offsetToAverageGroundLevel(world, structureboundingbox, 0)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(world, structureboundingbox, 0, -4, 0, this.scatteredFeatureSizeX - 1, 0, this.scatteredFeatureSizeZ - 1, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 2, 1, 2, 9, 2, 2, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 2, 1, 12, 9, 2, 12, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 2, 1, 3, 2, 2, 11, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 9, 1, 3, 9, 2, 11, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 1, 3, 1, 10, 6, 1, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 1, 3, 13, 10, 6, 13, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 1, 3, 2, 1, 6, 12, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 10, 3, 2, 10, 6, 12, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 2, 3, 2, 9, 3, 12, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 2, 6, 2, 9, 6, 12, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 3, 7, 3, 8, 7, 11, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 4, 8, 4, 7, 8, 10, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithAir(world, structureboundingbox, 3, 1, 3, 8, 2, 11);
            this.fillWithAir(world, structureboundingbox, 4, 3, 6, 7, 3, 9);
            this.fillWithAir(world, structureboundingbox, 2, 4, 2, 9, 5, 12);
            this.fillWithAir(world, structureboundingbox, 4, 6, 5, 7, 6, 9);
            this.fillWithAir(world, structureboundingbox, 5, 7, 6, 6, 7, 8);
            this.fillWithAir(world, structureboundingbox, 5, 1, 2, 6, 2, 2);
            this.fillWithAir(world, structureboundingbox, 5, 2, 12, 6, 2, 12);
            this.fillWithAir(world, structureboundingbox, 5, 5, 1, 6, 5, 1);
            this.fillWithAir(world, structureboundingbox, 5, 5, 13, 6, 5, 13);
            this.setBlockState(world, Blocks.AIR.getDefaultState(), 1, 5, 5, structureboundingbox);
            this.setBlockState(world, Blocks.AIR.getDefaultState(), 10, 5, 5, structureboundingbox);
            this.setBlockState(world, Blocks.AIR.getDefaultState(), 1, 5, 9, structureboundingbox);
            this.setBlockState(world, Blocks.AIR.getDefaultState(), 10, 5, 9, structureboundingbox);

            for(int i = 0; i <= 14; i += 14) {
               this.fillWithRandomizedBlocks(world, structureboundingbox, 2, 4, i, 2, 5, i, false, random, junglePyramidsRandomScatteredStones);
               this.fillWithRandomizedBlocks(world, structureboundingbox, 4, 4, i, 4, 5, i, false, random, junglePyramidsRandomScatteredStones);
               this.fillWithRandomizedBlocks(world, structureboundingbox, 7, 4, i, 7, 5, i, false, random, junglePyramidsRandomScatteredStones);
               this.fillWithRandomizedBlocks(world, structureboundingbox, 9, 4, i, 9, 5, i, false, random, junglePyramidsRandomScatteredStones);
            }

            this.fillWithRandomizedBlocks(world, structureboundingbox, 5, 6, 0, 6, 6, 0, false, random, junglePyramidsRandomScatteredStones);

            for(int var11 = 0; var11 <= 11; var11 += 11) {
               for(int j = 2; j <= 12; j += 2) {
                  this.fillWithRandomizedBlocks(world, structureboundingbox, var11, 4, j, var11, 5, j, false, random, junglePyramidsRandomScatteredStones);
               }

               this.fillWithRandomizedBlocks(world, structureboundingbox, var11, 6, 5, var11, 6, 5, false, random, junglePyramidsRandomScatteredStones);
               this.fillWithRandomizedBlocks(world, structureboundingbox, var11, 6, 9, var11, 6, 9, false, random, junglePyramidsRandomScatteredStones);
            }

            this.fillWithRandomizedBlocks(world, structureboundingbox, 2, 7, 2, 2, 9, 2, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 9, 7, 2, 9, 9, 2, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 2, 7, 12, 2, 9, 12, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 9, 7, 12, 9, 9, 12, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 4, 9, 4, 4, 9, 4, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 7, 9, 4, 7, 9, 4, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 4, 9, 10, 4, 9, 10, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 7, 9, 10, 7, 9, 10, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 5, 9, 7, 6, 9, 7, false, random, junglePyramidsRandomScatteredStones);
            IBlockState iblockdata = Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST);
            IBlockState iblockdata1 = Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.WEST);
            IBlockState iblockdata2 = Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH);
            IBlockState iblockdata3 = Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH);
            this.setBlockState(world, iblockdata3, 5, 9, 6, structureboundingbox);
            this.setBlockState(world, iblockdata3, 6, 9, 6, structureboundingbox);
            this.setBlockState(world, iblockdata2, 5, 9, 8, structureboundingbox);
            this.setBlockState(world, iblockdata2, 6, 9, 8, structureboundingbox);
            this.setBlockState(world, iblockdata3, 4, 0, 0, structureboundingbox);
            this.setBlockState(world, iblockdata3, 5, 0, 0, structureboundingbox);
            this.setBlockState(world, iblockdata3, 6, 0, 0, structureboundingbox);
            this.setBlockState(world, iblockdata3, 7, 0, 0, structureboundingbox);
            this.setBlockState(world, iblockdata3, 4, 1, 8, structureboundingbox);
            this.setBlockState(world, iblockdata3, 4, 2, 9, structureboundingbox);
            this.setBlockState(world, iblockdata3, 4, 3, 10, structureboundingbox);
            this.setBlockState(world, iblockdata3, 7, 1, 8, structureboundingbox);
            this.setBlockState(world, iblockdata3, 7, 2, 9, structureboundingbox);
            this.setBlockState(world, iblockdata3, 7, 3, 10, structureboundingbox);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 4, 1, 9, 4, 1, 9, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 7, 1, 9, 7, 1, 9, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 4, 1, 10, 7, 2, 10, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 5, 4, 5, 6, 4, 5, false, random, junglePyramidsRandomScatteredStones);
            this.setBlockState(world, iblockdata, 4, 4, 5, structureboundingbox);
            this.setBlockState(world, iblockdata1, 7, 4, 5, structureboundingbox);

            for(int k = 0; k < 4; ++k) {
               this.setBlockState(world, iblockdata2, 5, 0 - k, 6 + k, structureboundingbox);
               this.setBlockState(world, iblockdata2, 6, 0 - k, 6 + k, structureboundingbox);
               this.fillWithAir(world, structureboundingbox, 5, 0 - k, 7 + k, 6, 0 - k, 9 + k);
            }

            this.fillWithAir(world, structureboundingbox, 1, -3, 12, 10, -1, 13);
            this.fillWithAir(world, structureboundingbox, 1, -3, 1, 3, -1, 13);
            this.fillWithAir(world, structureboundingbox, 1, -3, 1, 9, -1, 5);

            for(int var13 = 1; var13 <= 13; var13 += 2) {
               this.fillWithRandomizedBlocks(world, structureboundingbox, 1, -3, var13, 1, -2, var13, false, random, junglePyramidsRandomScatteredStones);
            }

            for(int var14 = 2; var14 <= 12; var14 += 2) {
               this.fillWithRandomizedBlocks(world, structureboundingbox, 1, -1, var14, 3, -1, var14, false, random, junglePyramidsRandomScatteredStones);
            }

            this.fillWithRandomizedBlocks(world, structureboundingbox, 2, -2, 1, 5, -2, 1, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 7, -2, 1, 9, -2, 1, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 6, -3, 1, 6, -3, 1, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 6, -1, 1, 6, -1, 1, false, random, junglePyramidsRandomScatteredStones);
            this.setBlockState(world, Blocks.TRIPWIRE_HOOK.getDefaultState().withProperty(BlockTripWireHook.FACING, EnumFacing.EAST).withProperty(BlockTripWireHook.ATTACHED, Boolean.valueOf(true)), 1, -3, 8, structureboundingbox);
            this.setBlockState(world, Blocks.TRIPWIRE_HOOK.getDefaultState().withProperty(BlockTripWireHook.FACING, EnumFacing.WEST).withProperty(BlockTripWireHook.ATTACHED, Boolean.valueOf(true)), 4, -3, 8, structureboundingbox);
            this.setBlockState(world, Blocks.TRIPWIRE.getDefaultState().withProperty(BlockTripWire.ATTACHED, Boolean.valueOf(true)), 2, -3, 8, structureboundingbox);
            this.setBlockState(world, Blocks.TRIPWIRE.getDefaultState().withProperty(BlockTripWire.ATTACHED, Boolean.valueOf(true)), 3, -3, 8, structureboundingbox);
            this.setBlockState(world, Blocks.REDSTONE_WIRE.getDefaultState(), 5, -3, 7, structureboundingbox);
            this.setBlockState(world, Blocks.REDSTONE_WIRE.getDefaultState(), 5, -3, 6, structureboundingbox);
            this.setBlockState(world, Blocks.REDSTONE_WIRE.getDefaultState(), 5, -3, 5, structureboundingbox);
            this.setBlockState(world, Blocks.REDSTONE_WIRE.getDefaultState(), 5, -3, 4, structureboundingbox);
            this.setBlockState(world, Blocks.REDSTONE_WIRE.getDefaultState(), 5, -3, 3, structureboundingbox);
            this.setBlockState(world, Blocks.REDSTONE_WIRE.getDefaultState(), 5, -3, 2, structureboundingbox);
            this.setBlockState(world, Blocks.REDSTONE_WIRE.getDefaultState(), 5, -3, 1, structureboundingbox);
            this.setBlockState(world, Blocks.REDSTONE_WIRE.getDefaultState(), 4, -3, 1, structureboundingbox);
            this.setBlockState(world, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 3, -3, 1, structureboundingbox);
            if (!this.placedTrap1) {
               this.placedTrap1 = this.createDispenser(world, structureboundingbox, random, 3, -2, 1, EnumFacing.NORTH, LootTableList.CHESTS_JUNGLE_TEMPLE_DISPENSER);
            }

            this.setBlockState(world, Blocks.VINE.getDefaultState().withProperty(BlockVine.SOUTH, Boolean.valueOf(true)), 3, -2, 2, structureboundingbox);
            this.setBlockState(world, Blocks.TRIPWIRE_HOOK.getDefaultState().withProperty(BlockTripWireHook.FACING, EnumFacing.NORTH).withProperty(BlockTripWireHook.ATTACHED, Boolean.valueOf(true)), 7, -3, 1, structureboundingbox);
            this.setBlockState(world, Blocks.TRIPWIRE_HOOK.getDefaultState().withProperty(BlockTripWireHook.FACING, EnumFacing.SOUTH).withProperty(BlockTripWireHook.ATTACHED, Boolean.valueOf(true)), 7, -3, 5, structureboundingbox);
            this.setBlockState(world, Blocks.TRIPWIRE.getDefaultState().withProperty(BlockTripWire.ATTACHED, Boolean.valueOf(true)), 7, -3, 2, structureboundingbox);
            this.setBlockState(world, Blocks.TRIPWIRE.getDefaultState().withProperty(BlockTripWire.ATTACHED, Boolean.valueOf(true)), 7, -3, 3, structureboundingbox);
            this.setBlockState(world, Blocks.TRIPWIRE.getDefaultState().withProperty(BlockTripWire.ATTACHED, Boolean.valueOf(true)), 7, -3, 4, structureboundingbox);
            this.setBlockState(world, Blocks.REDSTONE_WIRE.getDefaultState(), 8, -3, 6, structureboundingbox);
            this.setBlockState(world, Blocks.REDSTONE_WIRE.getDefaultState(), 9, -3, 6, structureboundingbox);
            this.setBlockState(world, Blocks.REDSTONE_WIRE.getDefaultState(), 9, -3, 5, structureboundingbox);
            this.setBlockState(world, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 9, -3, 4, structureboundingbox);
            this.setBlockState(world, Blocks.REDSTONE_WIRE.getDefaultState(), 9, -2, 4, structureboundingbox);
            if (!this.placedTrap2) {
               this.placedTrap2 = this.createDispenser(world, structureboundingbox, random, 9, -2, 3, EnumFacing.WEST, LootTableList.CHESTS_JUNGLE_TEMPLE_DISPENSER);
            }

            this.setBlockState(world, Blocks.VINE.getDefaultState().withProperty(BlockVine.EAST, Boolean.valueOf(true)), 8, -1, 3, structureboundingbox);
            this.setBlockState(world, Blocks.VINE.getDefaultState().withProperty(BlockVine.EAST, Boolean.valueOf(true)), 8, -2, 3, structureboundingbox);
            if (!this.placedMainChest) {
               this.placedMainChest = this.generateChest(world, structureboundingbox, random, 8, -3, 3, LootTableList.CHESTS_JUNGLE_TEMPLE);
            }

            this.setBlockState(world, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 9, -3, 2, structureboundingbox);
            this.setBlockState(world, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 8, -3, 1, structureboundingbox);
            this.setBlockState(world, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 4, -3, 5, structureboundingbox);
            this.setBlockState(world, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 5, -2, 5, structureboundingbox);
            this.setBlockState(world, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 5, -1, 5, structureboundingbox);
            this.setBlockState(world, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 6, -3, 5, structureboundingbox);
            this.setBlockState(world, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 7, -2, 5, structureboundingbox);
            this.setBlockState(world, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 7, -1, 5, structureboundingbox);
            this.setBlockState(world, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 8, -3, 5, structureboundingbox);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 9, -1, 1, 9, -1, 5, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithAir(world, structureboundingbox, 8, -3, 8, 10, -1, 10);
            this.setBlockState(world, Blocks.STONEBRICK.getStateFromMeta(BlockStoneBrick.CHISELED_META), 8, -2, 11, structureboundingbox);
            this.setBlockState(world, Blocks.STONEBRICK.getStateFromMeta(BlockStoneBrick.CHISELED_META), 9, -2, 11, structureboundingbox);
            this.setBlockState(world, Blocks.STONEBRICK.getStateFromMeta(BlockStoneBrick.CHISELED_META), 10, -2, 11, structureboundingbox);
            IBlockState iblockdata4 = Blocks.LEVER.getDefaultState().withProperty(BlockLever.FACING, BlockLever.EnumOrientation.NORTH);
            this.setBlockState(world, iblockdata4, 8, -2, 12, structureboundingbox);
            this.setBlockState(world, iblockdata4, 9, -2, 12, structureboundingbox);
            this.setBlockState(world, iblockdata4, 10, -2, 12, structureboundingbox);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 8, -3, 8, 8, -3, 10, false, random, junglePyramidsRandomScatteredStones);
            this.fillWithRandomizedBlocks(world, structureboundingbox, 10, -3, 8, 10, -3, 10, false, random, junglePyramidsRandomScatteredStones);
            this.setBlockState(world, Blocks.MOSSY_COBBLESTONE.getDefaultState(), 10, -2, 9, structureboundingbox);
            this.setBlockState(world, Blocks.REDSTONE_WIRE.getDefaultState(), 8, -2, 9, structureboundingbox);
            this.setBlockState(world, Blocks.REDSTONE_WIRE.getDefaultState(), 8, -2, 10, structureboundingbox);
            this.setBlockState(world, Blocks.REDSTONE_WIRE.getDefaultState(), 10, -1, 9, structureboundingbox);
            this.setBlockState(world, Blocks.STICKY_PISTON.getDefaultState().withProperty(BlockPistonBase.FACING, EnumFacing.UP), 9, -2, 8, structureboundingbox);
            this.setBlockState(world, Blocks.STICKY_PISTON.getDefaultState().withProperty(BlockPistonBase.FACING, EnumFacing.WEST), 10, -2, 8, structureboundingbox);
            this.setBlockState(world, Blocks.STICKY_PISTON.getDefaultState().withProperty(BlockPistonBase.FACING, EnumFacing.WEST), 10, -1, 8, structureboundingbox);
            this.setBlockState(world, Blocks.UNPOWERED_REPEATER.getDefaultState().withProperty(BlockRedstoneRepeater.FACING, EnumFacing.NORTH), 10, -2, 10, structureboundingbox);
            if (!this.placedHiddenChest) {
               this.placedHiddenChest = this.generateChest(world, structureboundingbox, random, 9, -3, 10, LootTableList.CHESTS_JUNGLE_TEMPLE);
            }

            return true;
         }
      }

      static class WorldGenJungleTemplePiece extends StructureComponent.BlockSelector {
         private WorldGenJungleTemplePiece() {
         }

         public void selectBlocks(Random random, int i, int j, int k, boolean flag) {
            if (random.nextFloat() < 0.4F) {
               this.blockstate = Blocks.COBBLESTONE.getDefaultState();
            } else {
               this.blockstate = Blocks.MOSSY_COBBLESTONE.getDefaultState();
            }

         }

         WorldGenJungleTemplePiece(ComponentScatteredFeaturePieces.SyntheticClass_1 worldgenregistration_syntheticclass_1) {
            this();
         }
      }
   }

   public static class SwampHut extends ComponentScatteredFeaturePieces.Feature {
      private boolean hasWitch;

      public SwampHut() {
      }

      public SwampHut(Random random, int i, int j) {
         super(random, i, 64, j, 7, 7, 9);
      }

      protected void writeStructureToNBT(NBTTagCompound nbttagcompound) {
         super.writeStructureToNBT(nbttagcompound);
         nbttagcompound.setBoolean("Witch", this.hasWitch);
      }

      protected void readStructureFromNBT(NBTTagCompound nbttagcompound) {
         super.readStructureFromNBT(nbttagcompound);
         this.hasWitch = nbttagcompound.getBoolean("Witch");
      }

      public boolean addComponentParts(World world, Random random, StructureBoundingBox structureboundingbox) {
         if (!this.offsetToAverageGroundLevel(world, structureboundingbox, 0)) {
            return false;
         } else {
            this.fillWithBlocks(world, structureboundingbox, 1, 1, 1, 5, 1, 7, Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), false);
            this.fillWithBlocks(world, structureboundingbox, 1, 4, 2, 5, 4, 7, Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), false);
            this.fillWithBlocks(world, structureboundingbox, 2, 1, 0, 4, 1, 0, Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), false);
            this.fillWithBlocks(world, structureboundingbox, 2, 2, 2, 3, 3, 2, Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), false);
            this.fillWithBlocks(world, structureboundingbox, 1, 2, 3, 1, 3, 6, Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), false);
            this.fillWithBlocks(world, structureboundingbox, 5, 2, 3, 5, 3, 6, Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), false);
            this.fillWithBlocks(world, structureboundingbox, 2, 2, 7, 4, 3, 7, Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), Blocks.PLANKS.getStateFromMeta(BlockPlanks.EnumType.SPRUCE.getMetadata()), false);
            this.fillWithBlocks(world, structureboundingbox, 1, 0, 2, 1, 3, 2, Blocks.LOG.getDefaultState(), Blocks.LOG.getDefaultState(), false);
            this.fillWithBlocks(world, structureboundingbox, 5, 0, 2, 5, 3, 2, Blocks.LOG.getDefaultState(), Blocks.LOG.getDefaultState(), false);
            this.fillWithBlocks(world, structureboundingbox, 1, 0, 7, 1, 3, 7, Blocks.LOG.getDefaultState(), Blocks.LOG.getDefaultState(), false);
            this.fillWithBlocks(world, structureboundingbox, 5, 0, 7, 5, 3, 7, Blocks.LOG.getDefaultState(), Blocks.LOG.getDefaultState(), false);
            this.setBlockState(world, Blocks.OAK_FENCE.getDefaultState(), 2, 3, 2, structureboundingbox);
            this.setBlockState(world, Blocks.OAK_FENCE.getDefaultState(), 3, 3, 7, structureboundingbox);
            this.setBlockState(world, Blocks.AIR.getDefaultState(), 1, 3, 4, structureboundingbox);
            this.setBlockState(world, Blocks.AIR.getDefaultState(), 5, 3, 4, structureboundingbox);
            this.setBlockState(world, Blocks.AIR.getDefaultState(), 5, 3, 5, structureboundingbox);
            this.setBlockState(world, Blocks.FLOWER_POT.getDefaultState().withProperty(BlockFlowerPot.CONTENTS, BlockFlowerPot.EnumFlowerType.MUSHROOM_RED), 1, 3, 5, structureboundingbox);
            this.setBlockState(world, Blocks.CRAFTING_TABLE.getDefaultState(), 3, 2, 6, structureboundingbox);
            this.setBlockState(world, Blocks.CAULDRON.getDefaultState(), 4, 2, 6, structureboundingbox);
            this.setBlockState(world, Blocks.OAK_FENCE.getDefaultState(), 1, 2, 1, structureboundingbox);
            this.setBlockState(world, Blocks.OAK_FENCE.getDefaultState(), 5, 2, 1, structureboundingbox);
            IBlockState iblockdata = Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH);
            IBlockState iblockdata1 = Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST);
            IBlockState iblockdata2 = Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.WEST);
            IBlockState iblockdata3 = Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH);
            this.fillWithBlocks(world, structureboundingbox, 0, 4, 1, 6, 4, 1, iblockdata, iblockdata, false);
            this.fillWithBlocks(world, structureboundingbox, 0, 4, 2, 0, 4, 7, iblockdata1, iblockdata1, false);
            this.fillWithBlocks(world, structureboundingbox, 6, 4, 2, 6, 4, 7, iblockdata2, iblockdata2, false);
            this.fillWithBlocks(world, structureboundingbox, 0, 4, 8, 6, 4, 8, iblockdata3, iblockdata3, false);

            for(int i = 2; i <= 7; i += 5) {
               for(int j = 1; j <= 5; j += 4) {
                  this.replaceAirAndLiquidDownwards(world, Blocks.LOG.getDefaultState(), j, -1, i, structureboundingbox);
               }
            }

            if (!this.hasWitch) {
               int var12 = this.getXWithOffset(2, 5);
               int j = this.getYWithOffset(2);
               int k = this.getZWithOffset(2, 5);
               if (structureboundingbox.isVecInside(new BlockPos(var12, j, k))) {
                  this.hasWitch = true;
                  EntityWitch entitywitch = new EntityWitch(world);
                  entitywitch.setLocationAndAngles((double)var12 + 0.5D, (double)j, (double)k + 0.5D, 0.0F, 0.0F);
                  entitywitch.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(var12, j, k)), (IEntityLivingData)null);
                  world.addEntity(entitywitch, SpawnReason.CHUNK_GEN);
               }
            }

            return true;
         }
      }
   }

   static class SyntheticClass_1 {
   }
}
