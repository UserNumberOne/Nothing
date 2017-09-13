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

   public BlockSlab(Material var1) {
      super(var1);
      this.fullBlock = this.isDouble();
      this.setLightOpacity(255);
   }

   protected boolean canSilkHarvest() {
      return false;
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return this.isDouble() ? FULL_BLOCK_AABB : (var1.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP ? AABB_TOP_HALF : AABB_BOTTOM_HALF);
   }

   public boolean isFullyOpaque(IBlockState var1) {
      return ((BlockSlab)var1.getBlock()).isDouble() || var1.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return this.isDouble();
   }

   public boolean doesSideBlockRendering(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      if (ForgeModContainer.disableStairSlabCulling) {
         return super.doesSideBlockRendering(var1, var2, var3, var4);
      } else if (var1.isOpaqueCube()) {
         return true;
      } else {
         BlockSlab.EnumBlockHalf var5 = (BlockSlab.EnumBlockHalf)var1.getValue(HALF);
         return var5 == BlockSlab.EnumBlockHalf.TOP && var4 == EnumFacing.UP || var5 == BlockSlab.EnumBlockHalf.BOTTOM && var4 == EnumFacing.DOWN;
      }
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      IBlockState var9 = super.getStateForPlacement(var1, var2, var3, var4, var5, var6, var7, var8).withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM);
      return this.isDouble() ? var9 : (var3 == EnumFacing.DOWN || var3 != EnumFacing.UP && (double)var5 > 0.5D ? var9.withProperty(HALF, BlockSlab.EnumBlockHalf.TOP) : var9);
   }

   public int quantityDropped(Random var1) {
      return this.isDouble() ? 2 : 1;
   }

   public boolean isFullCube(IBlockState var1) {
      return this.isDouble();
   }

   @SideOnly(Side.CLIENT)
   public boolean shouldSideBeRendered(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      if (this.isDouble()) {
         return super.shouldSideBeRendered(var1, var2, var3, var4);
      } else {
         return var4 != EnumFacing.UP && var4 != EnumFacing.DOWN && !super.shouldSideBeRendered(var1, var2, var3, var4) ? false : super.shouldSideBeRendered(var1, var2, var3, var4);
      }
   }

   @SideOnly(Side.CLIENT)
   protected static boolean isHalfSlab(IBlockState var0) {
      Block var1 = var0.getBlock();
      return var1 == Blocks.STONE_SLAB || var1 == Blocks.WOODEN_SLAB || var1 == Blocks.STONE_SLAB2 || var1 == Blocks.PURPUR_SLAB;
   }

   public abstract String getUnlocalizedName(int var1);

   public abstract boolean isDouble();

   public abstract IProperty getVariantProperty();

   public abstract Comparable getTypeForItem(ItemStack var1);

   public static enum EnumBlockHalf implements IStringSerializable {
      TOP("top"),
      BOTTOM("bottom");

      private final String name;

      private EnumBlockHalf(String var3) {
         this.name = var3;
      }

      public String toString() {
         return this.name;
      }

      public String getName() {
         return this.name;
      }
   }
}
