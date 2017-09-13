package net.minecraft.block;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockRail extends BlockRailBase {
   public static final PropertyEnum SHAPE = PropertyEnum.create("shape", BlockRailBase.EnumRailDirection.class);

   protected BlockRail() {
      super(false);
      this.setDefaultState(this.blockState.getBaseState().withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH));
   }

   protected void updateState(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (var4.getDefaultState().canProvidePower() && (new BlockRailBase.Rail(var2, var3, var1)).countAdjacentRails() == 3) {
         this.updateDir(var2, var3, var1, false);
      }

   }

   public IProperty getShapeProperty() {
      return SHAPE;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(SHAPE, BlockRailBase.EnumRailDirection.byMetadata(var1));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((BlockRailBase.EnumRailDirection)var1.getValue(SHAPE)).getMetadata();
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      switch(var2) {
      case CLOCKWISE_180:
         switch((BlockRailBase.EnumRailDirection)var1.getValue(SHAPE)) {
         case ASCENDING_EAST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);
         case ASCENDING_WEST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);
         case ASCENDING_NORTH:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);
         case ASCENDING_SOUTH:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);
         case SOUTH_EAST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         case SOUTH_WEST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case NORTH_WEST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         case NORTH_EAST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         }
      case COUNTERCLOCKWISE_90:
         switch((BlockRailBase.EnumRailDirection)var1.getValue(SHAPE)) {
         case ASCENDING_EAST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);
         case ASCENDING_WEST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);
         case ASCENDING_NORTH:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);
         case ASCENDING_SOUTH:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);
         case SOUTH_EAST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case SOUTH_WEST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         case NORTH_WEST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         case NORTH_EAST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         case NORTH_SOUTH:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.EAST_WEST);
         case EAST_WEST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH);
         }
      case CLOCKWISE_90:
         switch((BlockRailBase.EnumRailDirection)var1.getValue(SHAPE)) {
         case ASCENDING_EAST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);
         case ASCENDING_WEST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);
         case ASCENDING_NORTH:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);
         case ASCENDING_SOUTH:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);
         case SOUTH_EAST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         case SOUTH_WEST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         case NORTH_WEST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case NORTH_EAST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         case NORTH_SOUTH:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.EAST_WEST);
         case EAST_WEST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH);
         }
      default:
         return var1;
      }
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      BlockRailBase.EnumRailDirection var3 = (BlockRailBase.EnumRailDirection)var1.getValue(SHAPE);
      switch(var2) {
      case LEFT_RIGHT:
         switch(var3) {
         case ASCENDING_NORTH:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);
         case ASCENDING_SOUTH:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);
         case SOUTH_EAST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case SOUTH_WEST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         case NORTH_WEST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         case NORTH_EAST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         default:
            return super.withMirror(var1, var2);
         }
      case FRONT_BACK:
         switch(var3) {
         case ASCENDING_EAST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);
         case ASCENDING_WEST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);
         case ASCENDING_NORTH:
         case ASCENDING_SOUTH:
         default:
            break;
         case SOUTH_EAST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         case SOUTH_WEST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         case NORTH_WEST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case NORTH_EAST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         }
      default:
         return super.withMirror(var1, var2);
      }
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{SHAPE});
   }
}
