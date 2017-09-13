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
      boolean var4 = true;
      boolean var5 = true;
      if (var1.isAreaLoaded(var2.add(-5, -5, -5), var2.add(5, 5, 5))) {
         for(BlockPos var7 : BlockPos.getAllInBox(var2.add(-4, -4, -4), var2.add(4, 4, 4))) {
            IBlockState var8 = var1.getBlockState(var7);
            if (var8.getMaterial() == Material.LEAVES && !((Boolean)var8.getValue(BlockLeaves.CHECK_DECAY)).booleanValue()) {
               var1.setBlockState(var7, var8.withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(true)), 4);
            }
         }

      }
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      return this.getStateFromMeta(var7).withProperty(LOG_AXIS, BlockLog.EnumAxis.fromFacingAxis(var3.getAxis()));
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      switch(var2) {
      case COUNTERCLOCKWISE_90:
      case CLOCKWISE_90:
         switch((BlockLog.EnumAxis)var1.getValue(LOG_AXIS)) {
         case X:
            return var1.withProperty(LOG_AXIS, BlockLog.EnumAxis.Z);
         case Z:
            return var1.withProperty(LOG_AXIS, BlockLog.EnumAxis.X);
         default:
            return var1;
         }
      default:
         return var1;
      }
   }

   public static enum EnumAxis implements IStringSerializable {
      X("x"),
      Y("y"),
      Z("z"),
      NONE("none");

      private final String name;

      private EnumAxis(String var3) {
         this.name = var3;
      }

      public String toString() {
         return this.name;
      }

      public static BlockLog.EnumAxis fromFacingAxis(EnumFacing.Axis var0) {
         switch(var0) {
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
