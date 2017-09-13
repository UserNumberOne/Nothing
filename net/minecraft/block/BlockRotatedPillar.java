package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockRotatedPillar extends Block {
   public static final PropertyEnum AXIS = PropertyEnum.create("axis", EnumFacing.Axis.class);

   protected BlockRotatedPillar(Material var1) {
      super(var1, var1.getMaterialMapColor());
   }

   protected BlockRotatedPillar(Material var1, MapColor var2) {
      super(var1, var2);
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      switch(var2) {
      case COUNTERCLOCKWISE_90:
      case CLOCKWISE_90:
         switch((EnumFacing.Axis)var1.getValue(AXIS)) {
         case X:
            return var1.withProperty(AXIS, EnumFacing.Axis.Z);
         case Z:
            return var1.withProperty(AXIS, EnumFacing.Axis.X);
         default:
            return var1;
         }
      default:
         return var1;
      }
   }

   public IBlockState getStateFromMeta(int var1) {
      EnumFacing.Axis var2 = EnumFacing.Axis.Y;
      int var3 = var1 & 12;
      if (var3 == 4) {
         var2 = EnumFacing.Axis.X;
      } else if (var3 == 8) {
         var2 = EnumFacing.Axis.Z;
      }

      return this.getDefaultState().withProperty(AXIS, var2);
   }

   public int getMetaFromState(IBlockState var1) {
      int var2 = 0;
      EnumFacing.Axis var3 = (EnumFacing.Axis)var1.getValue(AXIS);
      if (var3 == EnumFacing.Axis.X) {
         var2 |= 4;
      } else if (var3 == EnumFacing.Axis.Z) {
         var2 |= 8;
      }

      return var2;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{AXIS});
   }

   protected ItemStack getSilkTouchDrop(IBlockState var1) {
      return new ItemStack(Item.getItemFromBlock(this));
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      return super.getStateForPlacement(var1, var2, var3, var4, var5, var6, var7, var8).withProperty(AXIS, var3.getAxis());
   }
}
