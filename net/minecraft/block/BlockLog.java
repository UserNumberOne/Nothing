package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockLog extends BlockRotatedPillar {
   public static final PropertyEnum LOG_AXIS = PropertyEnum.create("axis", BlockLog.EnumAxis.class);

   public BlockLog() {
      super(Material.WOOD);
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
      this.setHardness(2.0F);
      this.setSoundType(SoundType.WOOD);
   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      int i = 4;
      int j = 5;
      if (worldIn.isAreaLoaded(pos.add(-5, -5, -5), pos.add(5, 5, 5))) {
         for(BlockPos blockpos : BlockPos.getAllInBox(pos.add(-4, -4, -4), pos.add(4, 4, 4))) {
            IBlockState iblockstate = worldIn.getBlockState(blockpos);
            if (iblockstate.getBlock().isLeaves(iblockstate, worldIn, blockpos)) {
               iblockstate.getBlock().beginLeavesDecay(iblockstate, worldIn, blockpos);
            }
         }
      }

   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      return this.getStateFromMeta(meta).withProperty(LOG_AXIS, BlockLog.EnumAxis.fromFacingAxis(facing.getAxis()));
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      switch(rot) {
      case COUNTERCLOCKWISE_90:
      case CLOCKWISE_90:
         switch((BlockLog.EnumAxis)state.getValue(LOG_AXIS)) {
         case X:
            return state.withProperty(LOG_AXIS, BlockLog.EnumAxis.Z);
         case Z:
            return state.withProperty(LOG_AXIS, BlockLog.EnumAxis.X);
         default:
            return state;
         }
      default:
         return state;
      }
   }

   public boolean canSustainLeaves(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return true;
   }

   public boolean isWood(IBlockAccess var1, BlockPos var2) {
      return true;
   }

   public static enum EnumAxis implements IStringSerializable {
      X("x"),
      Y("y"),
      Z("z"),
      NONE("none");

      private final String name;

      private EnumAxis(String var3) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }

      public static BlockLog.EnumAxis fromFacingAxis(EnumFacing.Axis var0) {
         switch(axis) {
         case X:
            return X;
         case Y:
            return Y;
         case Z:
            return Z;
         default:
            return NONE;
         }
      }

      public String getName() {
         return this.name;
      }
   }
}
