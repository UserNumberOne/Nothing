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
      if (p_189541_4_.getDefaultState().canProvidePower() && (new BlockRailBase.Rail(p_189541_2_, p_189541_3_, p_189541_1_)).countAdjacentRails() == 3) {
         this.updateDir(p_189541_2_, p_189541_3_, p_189541_1_, false);
      }

   }

   public IProperty getShapeProperty() {
      return SHAPE;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(SHAPE, BlockRailBase.EnumRailDirection.byMetadata(meta));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((BlockRailBase.EnumRailDirection)state.getValue(SHAPE)).getMetadata();
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      switch(rot) {
      case CLOCKWISE_180:
         switch((BlockRailBase.EnumRailDirection)state.getValue(SHAPE)) {
         case ASCENDING_EAST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);
         case ASCENDING_WEST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);
         case ASCENDING_NORTH:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);
         case ASCENDING_SOUTH:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);
         case SOUTH_EAST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         case SOUTH_WEST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case NORTH_WEST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         case NORTH_EAST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         }
      case COUNTERCLOCKWISE_90:
         switch((BlockRailBase.EnumRailDirection)state.getValue(SHAPE)) {
         case ASCENDING_EAST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);
         case ASCENDING_WEST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);
         case ASCENDING_NORTH:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);
         case ASCENDING_SOUTH:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);
         case SOUTH_EAST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case SOUTH_WEST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         case NORTH_WEST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         case NORTH_EAST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         case NORTH_SOUTH:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.EAST_WEST);
         case EAST_WEST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH);
         }
      case CLOCKWISE_90:
         switch((BlockRailBase.EnumRailDirection)state.getValue(SHAPE)) {
         case ASCENDING_EAST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);
         case ASCENDING_WEST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);
         case ASCENDING_NORTH:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);
         case ASCENDING_SOUTH:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);
         case SOUTH_EAST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         case SOUTH_WEST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         case NORTH_WEST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case NORTH_EAST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         case NORTH_SOUTH:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.EAST_WEST);
         case EAST_WEST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH);
         }
      default:
         return state;
      }
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      BlockRailBase.EnumRailDirection blockrailbase$enumraildirection = (BlockRailBase.EnumRailDirection)state.getValue(SHAPE);
      switch(mirrorIn) {
      case LEFT_RIGHT:
         switch(blockrailbase$enumraildirection) {
         case ASCENDING_NORTH:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);
         case ASCENDING_SOUTH:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);
         case SOUTH_EAST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case SOUTH_WEST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         case NORTH_WEST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         case NORTH_EAST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         default:
            return super.withMirror(state, mirrorIn);
         }
      case FRONT_BACK:
         switch(blockrailbase$enumraildirection) {
         case ASCENDING_EAST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);
         case ASCENDING_WEST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);
         case ASCENDING_NORTH:
         case ASCENDING_SOUTH:
         default:
            break;
         case SOUTH_EAST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         case SOUTH_WEST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         case NORTH_WEST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case NORTH_EAST:
            return state.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         }
      default:
         return super.withMirror(state, mirrorIn);
      }
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{SHAPE});
   }
}
