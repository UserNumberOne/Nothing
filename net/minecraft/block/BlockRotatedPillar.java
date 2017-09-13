package net.minecraft.block;

import com.google.common.collect.UnmodifiableIterator;
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
      super(materialIn, materialIn.getMaterialMapColor());
   }

   protected BlockRotatedPillar(Material var1, MapColor var2) {
      super(materialIn, color);
   }

   public boolean rotateBlock(World var1, BlockPos var2, EnumFacing var3) {
      IBlockState state = world.getBlockState(pos);
      UnmodifiableIterator var5 = state.getProperties().keySet().iterator();

      while(var5.hasNext()) {
         IProperty prop = (IProperty)var5.next();
         if (prop.getName().equals("axis")) {
            world.setBlockState(pos, state.cycleProperty(prop));
            return true;
         }
      }

      return false;
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      switch(rot) {
      case COUNTERCLOCKWISE_90:
      case CLOCKWISE_90:
         switch((EnumFacing.Axis)state.getValue(AXIS)) {
         case X:
            return state.withProperty(AXIS, EnumFacing.Axis.Z);
         case Z:
            return state.withProperty(AXIS, EnumFacing.Axis.X);
         default:
            return state;
         }
      default:
         return state;
      }
   }

   public IBlockState getStateFromMeta(int var1) {
      EnumFacing.Axis enumfacing$axis = EnumFacing.Axis.Y;
      int i = meta & 12;
      if (i == 4) {
         enumfacing$axis = EnumFacing.Axis.X;
      } else if (i == 8) {
         enumfacing$axis = EnumFacing.Axis.Z;
      }

      return this.getDefaultState().withProperty(AXIS, enumfacing$axis);
   }

   public int getMetaFromState(IBlockState var1) {
      int i = 0;
      EnumFacing.Axis enumfacing$axis = (EnumFacing.Axis)state.getValue(AXIS);
      if (enumfacing$axis == EnumFacing.Axis.X) {
         i |= 4;
      } else if (enumfacing$axis == EnumFacing.Axis.Z) {
         i |= 8;
      }

      return i;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{AXIS});
   }

   protected ItemStack getSilkTouchDrop(IBlockState var1) {
      return new ItemStack(Item.getItemFromBlock(this));
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(AXIS, facing.getAxis());
   }
}
