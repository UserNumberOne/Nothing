package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BlockSlab extends Block {
   public static final PropertyEnum HALF = PropertyEnum.create("half", BlockSlab.EnumBlockHalf.class);
   protected static final AxisAlignedBB AABB_BOTTOM_HALF = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
   protected static final AxisAlignedBB AABB_TOP_HALF = new AxisAlignedBB(0.0D, 0.5D, 0.0D, 1.0D, 1.0D, 1.0D);

   public BlockSlab(Material materialIn) {
      super(materialIn);
      this.fullBlock = this.isDouble();
      this.setLightOpacity(255);
   }

   protected boolean canSilkHarvest() {
      return false;
   }

   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
      return this.isDouble() ? FULL_BLOCK_AABB : (state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP ? AABB_TOP_HALF : AABB_BOTTOM_HALF);
   }

   public boolean isFullyOpaque(IBlockState state) {
      return ((BlockSlab)state.getBlock()).isDouble() || state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP;
   }

   public boolean isOpaqueCube(IBlockState state) {
      return this.isDouble();
   }

   public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
      if (ForgeModContainer.disableStairSlabCulling) {
         return super.doesSideBlockRendering(state, world, pos, face);
      } else if (state.isOpaqueCube()) {
         return true;
      } else {
         BlockSlab.EnumBlockHalf side = (BlockSlab.EnumBlockHalf)state.getValue(HALF);
         return side == BlockSlab.EnumBlockHalf.TOP && face == EnumFacing.UP || side == BlockSlab.EnumBlockHalf.BOTTOM && face == EnumFacing.DOWN;
      }
   }

   public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
      IBlockState iblockstate = super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM);
      return this.isDouble() ? iblockstate : (facing == EnumFacing.DOWN || facing != EnumFacing.UP && (double)hitY > 0.5D ? iblockstate.withProperty(HALF, BlockSlab.EnumBlockHalf.TOP) : iblockstate);
   }

   public int quantityDropped(Random random) {
      return this.isDouble() ? 2 : 1;
   }

   public boolean isFullCube(IBlockState state) {
      return this.isDouble();
   }

   @SideOnly(Side.CLIENT)
   public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
      if (this.isDouble()) {
         return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
      } else {
         return side != EnumFacing.UP && side != EnumFacing.DOWN && !super.shouldSideBeRendered(blockState, blockAccess, pos, side) ? false : super.shouldSideBeRendered(blockState, blockAccess, pos, side);
      }
   }

   @SideOnly(Side.CLIENT)
   protected static boolean isHalfSlab(IBlockState state) {
      Block block = state.getBlock();
      return block == Blocks.STONE_SLAB || block == Blocks.WOODEN_SLAB || block == Blocks.STONE_SLAB2 || block == Blocks.PURPUR_SLAB;
   }

   public abstract String getUnlocalizedName(int var1);

   public abstract boolean isDouble();

   public abstract IProperty getVariantProperty();

   public abstract Comparable getTypeForItem(ItemStack var1);

   public static enum EnumBlockHalf implements IStringSerializable {
      TOP("top"),
      BOTTOM("bottom");

      private final String name;

      private EnumBlockHalf(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }

      public String getName() {
         return this.name;
      }
   }
}
