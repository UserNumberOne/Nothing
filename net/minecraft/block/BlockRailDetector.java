package net.minecraft.block;

import com.google.common.base.Predicate;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartCommandBlock;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRailDetector extends BlockRailBase {
   public static final PropertyEnum SHAPE = PropertyEnum.create("shape", BlockRailBase.EnumRailDirection.class, new Predicate() {
      public boolean apply(@Nullable BlockRailBase.EnumRailDirection var1) {
         return p_apply_1_ != BlockRailBase.EnumRailDirection.NORTH_EAST && p_apply_1_ != BlockRailBase.EnumRailDirection.NORTH_WEST && p_apply_1_ != BlockRailBase.EnumRailDirection.SOUTH_EAST && p_apply_1_ != BlockRailBase.EnumRailDirection.SOUTH_WEST;
      }
   });
   public static final PropertyBool POWERED = PropertyBool.create("powered");

   public BlockRailDetector() {
      super(true);
      this.setDefaultState(this.blockState.getBaseState().withProperty(POWERED, Boolean.valueOf(false)).withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH));
      this.setTickRandomly(true);
   }

   public int tickRate(World var1) {
      return 20;
   }

   public boolean canProvidePower(IBlockState var1) {
      return true;
   }

   public void onEntityCollidedWithBlock(World var1, BlockPos var2, IBlockState var3, Entity var4) {
      if (!worldIn.isRemote && !((Boolean)state.getValue(POWERED)).booleanValue()) {
         this.updatePoweredState(worldIn, pos, state);
      }

   }

   public void randomTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!worldIn.isRemote && ((Boolean)state.getValue(POWERED)).booleanValue()) {
         this.updatePoweredState(worldIn, pos, state);
      }

   }

   public int getWeakPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return ((Boolean)blockState.getValue(POWERED)).booleanValue() ? 15 : 0;
   }

   public int getStrongPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return !((Boolean)blockState.getValue(POWERED)).booleanValue() ? 0 : (side == EnumFacing.UP ? 15 : 0);
   }

   private void updatePoweredState(World var1, BlockPos var2, IBlockState var3) {
      boolean flag = ((Boolean)state.getValue(POWERED)).booleanValue();
      boolean flag1 = false;
      List list = this.findMinecarts(worldIn, pos, EntityMinecart.class);
      if (!list.isEmpty()) {
         flag1 = true;
      }

      if (flag1 && !flag) {
         worldIn.setBlockState(pos, state.withProperty(POWERED, Boolean.valueOf(true)), 3);
         this.updateConnectedRails(worldIn, pos, state, true);
         worldIn.notifyNeighborsOfStateChange(pos, this);
         worldIn.notifyNeighborsOfStateChange(pos.down(), this);
         worldIn.markBlockRangeForRenderUpdate(pos, pos);
      }

      if (!flag1 && flag) {
         worldIn.setBlockState(pos, state.withProperty(POWERED, Boolean.valueOf(false)), 3);
         this.updateConnectedRails(worldIn, pos, state, false);
         worldIn.notifyNeighborsOfStateChange(pos, this);
         worldIn.notifyNeighborsOfStateChange(pos.down(), this);
         worldIn.markBlockRangeForRenderUpdate(pos, pos);
      }

      if (flag1) {
         worldIn.scheduleUpdate(new BlockPos(pos), this, this.tickRate(worldIn));
      }

      worldIn.updateComparatorOutputLevel(pos, this);
   }

   protected void updateConnectedRails(World var1, BlockPos var2, IBlockState var3, boolean var4) {
      BlockRailBase.Rail blockrailbase$rail = new BlockRailBase.Rail(worldIn, pos, state);

      for(BlockPos blockpos : blockrailbase$rail.getConnectedRails()) {
         IBlockState iblockstate = worldIn.getBlockState(blockpos);
         if (iblockstate != null) {
            iblockstate.neighborChanged(worldIn, blockpos, iblockstate.getBlock());
         }
      }

   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      super.onBlockAdded(worldIn, pos, state);
      this.updatePoweredState(worldIn, pos, state);
   }

   public IProperty getShapeProperty() {
      return SHAPE;
   }

   public boolean hasComparatorInputOverride(IBlockState var1) {
      return true;
   }

   public int getComparatorInputOverride(IBlockState var1, World var2, BlockPos var3) {
      if (((Boolean)blockState.getValue(POWERED)).booleanValue()) {
         List carts = this.findMinecarts(worldIn, pos, EntityMinecart.class);
         if (!carts.isEmpty() && ((EntityMinecart)carts.get(0)).getComparatorLevel() > -1) {
            return ((EntityMinecart)carts.get(0)).getComparatorLevel();
         }

         List list = this.findMinecarts(worldIn, pos, EntityMinecartCommandBlock.class);
         if (!list.isEmpty()) {
            return ((EntityMinecartCommandBlock)list.get(0)).getCommandBlockLogic().getSuccessCount();
         }

         List list1 = this.findMinecarts(worldIn, pos, EntityMinecart.class, EntitySelectors.HAS_INVENTORY);
         if (!list1.isEmpty()) {
            return Container.calcRedstoneFromInventory((IInventory)list1.get(0));
         }
      }

      return 0;
   }

   protected List findMinecarts(World var1, BlockPos var2, Class var3, Predicate... var4) {
      AxisAlignedBB axisalignedbb = this.getDectectionBox(pos);
      return filter.length != 1 ? worldIn.getEntitiesWithinAABB(clazz, axisalignedbb) : worldIn.getEntitiesWithinAABB(clazz, axisalignedbb, filter[0]);
   }

   private AxisAlignedBB getDectectionBox(BlockPos var1) {
      float f = 0.2F;
      return new AxisAlignedBB((double)((float)pos.getX() + 0.2F), (double)pos.getY(), (double)((float)pos.getZ() + 0.2F), (double)((float)(pos.getX() + 1) - 0.2F), (double)((float)(pos.getY() + 1) - 0.2F), (double)((float)(pos.getZ() + 1) - 0.2F));
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(SHAPE, BlockRailBase.EnumRailDirection.byMetadata(meta & 7)).withProperty(POWERED, Boolean.valueOf((meta & 8) > 0));
   }

   public int getMetaFromState(IBlockState var1) {
      int i = 0;
      i = i | ((BlockRailBase.EnumRailDirection)state.getValue(SHAPE)).getMetadata();
      if (((Boolean)state.getValue(POWERED)).booleanValue()) {
         i |= 8;
      }

      return i;
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
      return new BlockStateContainer(this, new IProperty[]{SHAPE, POWERED});
   }
}
