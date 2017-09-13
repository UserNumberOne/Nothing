package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

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
      if (this.isDouble()) {
         return FULL_BLOCK_AABB;
      } else {
         return var1.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP ? AABB_TOP_HALF : AABB_BOTTOM_HALF;
      }
   }

   public boolean isFullyOpaque(IBlockState var1) {
      return ((BlockSlab)var1.getBlock()).isDouble() || var1.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return this.isDouble();
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      IBlockState var9 = super.getStateForPlacement(var1, var2, var3, var4, var5, var6, var7, var8).withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM);
      if (this.isDouble()) {
         return var9;
      } else {
         return var3 != EnumFacing.DOWN && (var3 == EnumFacing.UP || (double)var5 <= 0.5D) ? var9 : var9.withProperty(HALF, BlockSlab.EnumBlockHalf.TOP);
      }
   }

   public int quantityDropped(Random var1) {
      return this.isDouble() ? 2 : 1;
   }

   public boolean isFullCube(IBlockState var1) {
      return this.isDouble();
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
