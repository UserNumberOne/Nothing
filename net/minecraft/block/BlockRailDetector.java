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
         return var1 != BlockRailBase.EnumRailDirection.NORTH_EAST && var1 != BlockRailBase.EnumRailDirection.NORTH_WEST && var1 != BlockRailBase.EnumRailDirection.SOUTH_EAST && var1 != BlockRailBase.EnumRailDirection.SOUTH_WEST;
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
      if (!var1.isRemote && !((Boolean)var3.getValue(POWERED)).booleanValue()) {
         this.updatePoweredState(var1, var2, var3);
      }

   }

   public void randomTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!var1.isRemote && ((Boolean)var3.getValue(POWERED)).booleanValue()) {
         this.updatePoweredState(var1, var2, var3);
      }

   }

   public int getWeakPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return ((Boolean)var1.getValue(POWERED)).booleanValue() ? 15 : 0;
   }

   public int getStrongPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return !((Boolean)var1.getValue(POWERED)).booleanValue() ? 0 : (var4 == EnumFacing.UP ? 15 : 0);
   }

   private void updatePoweredState(World var1, BlockPos var2, IBlockState var3) {
      boolean var4 = ((Boolean)var3.getValue(POWERED)).booleanValue();
      boolean var5 = false;
      List var6 = this.findMinecarts(var1, var2, EntityMinecart.class);
      if (!var6.isEmpty()) {
         var5 = true;
      }

      if (var5 && !var4) {
         var1.setBlockState(var2, var3.withProperty(POWERED, Boolean.valueOf(true)), 3);
         this.updateConnectedRails(var1, var2, var3, true);
         var1.notifyNeighborsOfStateChange(var2, this);
         var1.notifyNeighborsOfStateChange(var2.down(), this);
         var1.markBlockRangeForRenderUpdate(var2, var2);
      }

      if (!var5 && var4) {
         var1.setBlockState(var2, var3.withProperty(POWERED, Boolean.valueOf(false)), 3);
         this.updateConnectedRails(var1, var2, var3, false);
         var1.notifyNeighborsOfStateChange(var2, this);
         var1.notifyNeighborsOfStateChange(var2.down(), this);
         var1.markBlockRangeForRenderUpdate(var2, var2);
      }

      if (var5) {
         var1.scheduleUpdate(new BlockPos(var2), this, this.tickRate(var1));
      }

      var1.updateComparatorOutputLevel(var2, this);
   }

   protected void updateConnectedRails(World var1, BlockPos var2, IBlockState var3, boolean var4) {
      BlockRailBase.Rail var5 = new BlockRailBase.Rail(var1, var2, var3);

      for(BlockPos var7 : var5.getConnectedRails()) {
         IBlockState var8 = var1.getBlockState(var7);
         if (var8 != null) {
            var8.neighborChanged(var1, var7, var8.getBlock());
         }
      }

   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      super.onBlockAdded(var1, var2, var3);
      this.updatePoweredState(var1, var2, var3);
   }

   public IProperty getShapeProperty() {
      return SHAPE;
   }

   public boolean hasComparatorInputOverride(IBlockState var1) {
      return true;
   }

   public int getComparatorInputOverride(IBlockState var1, World var2, BlockPos var3) {
      if (((Boolean)var1.getValue(POWERED)).booleanValue()) {
         List var4 = this.findMinecarts(var2, var3, EntityMinecart.class);
         if (!var4.isEmpty() && ((EntityMinecart)var4.get(0)).getComparatorLevel() > -1) {
            return ((EntityMinecart)var4.get(0)).getComparatorLevel();
         }

         List var5 = this.findMinecarts(var2, var3, EntityMinecartCommandBlock.class);
         if (!var5.isEmpty()) {
            return ((EntityMinecartCommandBlock)var5.get(0)).getCommandBlockLogic().getSuccessCount();
         }

         List var6 = this.findMinecarts(var2, var3, EntityMinecart.class, EntitySelectors.HAS_INVENTORY);
         if (!var6.isEmpty()) {
            return Container.calcRedstoneFromInventory((IInventory)var6.get(0));
         }
      }

      return 0;
   }

   protected List findMinecarts(World var1, BlockPos var2, Class var3, Predicate... var4) {
      AxisAlignedBB var5 = this.getDectectionBox(var2);
      return var4.length != 1 ? var1.getEntitiesWithinAABB(var3, var5) : var1.getEntitiesWithinAABB(var3, var5, var4[0]);
   }

   private AxisAlignedBB getDectectionBox(BlockPos var1) {
      float var2 = 0.2F;
      return new AxisAlignedBB((double)((float)var1.getX() + 0.2F), (double)var1.getY(), (double)((float)var1.getZ() + 0.2F), (double)((float)(var1.getX() + 1) - 0.2F), (double)((float)(var1.getY() + 1) - 0.2F), (double)((float)(var1.getZ() + 1) - 0.2F));
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(SHAPE, BlockRailBase.EnumRailDirection.byMetadata(var1 & 7)).withProperty(POWERED, Boolean.valueOf((var1 & 8) > 0));
   }

   public int getMetaFromState(IBlockState var1) {
      int var2 = 0;
      var2 = var2 | ((BlockRailBase.EnumRailDirection)var1.getValue(SHAPE)).getMetadata();
      if (((Boolean)var1.getValue(POWERED)).booleanValue()) {
         var2 |= 8;
      }

      return var2;
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
      return new BlockStateContainer(this, new IProperty[]{SHAPE, POWERED});
   }
}
