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
import org.bukkit.event.block.BlockRedstoneEvent;

public class BlockRailDetector extends BlockRailBase {
   public static final PropertyEnum SHAPE = PropertyEnum.create("shape", BlockRailBase.EnumRailDirection.class, new Predicate() {
      public boolean apply(@Nullable BlockRailBase.EnumRailDirection blockminecarttrackabstract_enumtrackposition) {
         return blockminecarttrackabstract_enumtrackposition != BlockRailBase.EnumRailDirection.NORTH_EAST && blockminecarttrackabstract_enumtrackposition != BlockRailBase.EnumRailDirection.NORTH_WEST && blockminecarttrackabstract_enumtrackposition != BlockRailBase.EnumRailDirection.SOUTH_EAST && blockminecarttrackabstract_enumtrackposition != BlockRailBase.EnumRailDirection.SOUTH_WEST;
      }

      public boolean apply(Object object) {
         return this.apply((BlockRailBase.EnumRailDirection)object);
      }
   });
   public static final PropertyBool POWERED = PropertyBool.create("powered");

   public BlockRailDetector() {
      super(true);
      this.setDefaultState(this.blockState.getBaseState().withProperty(POWERED, Boolean.valueOf(false)).withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH));
      this.setTickRandomly(true);
   }

   public int tickRate(World world) {
      return 20;
   }

   public boolean canProvidePower(IBlockState iblockdata) {
      return true;
   }

   public void onEntityCollidedWithBlock(World world, BlockPos blockposition, IBlockState iblockdata, Entity entity) {
      if (!world.isRemote && !((Boolean)iblockdata.getValue(POWERED)).booleanValue()) {
         this.updatePoweredState(world, blockposition, iblockdata);
      }

   }

   public void randomTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
   }

   public void updateTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
      if (!world.isRemote && ((Boolean)iblockdata.getValue(POWERED)).booleanValue()) {
         this.updatePoweredState(world, blockposition, iblockdata);
      }

   }

   public int getWeakPower(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition, EnumFacing enumdirection) {
      return ((Boolean)iblockdata.getValue(POWERED)).booleanValue() ? 15 : 0;
   }

   public int getStrongPower(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition, EnumFacing enumdirection) {
      return !((Boolean)iblockdata.getValue(POWERED)).booleanValue() ? 0 : (enumdirection == EnumFacing.UP ? 15 : 0);
   }

   private void updatePoweredState(World world, BlockPos blockposition, IBlockState iblockdata) {
      boolean flag = ((Boolean)iblockdata.getValue(POWERED)).booleanValue();
      boolean flag1 = false;
      List list = this.findMinecarts(world, blockposition, EntityMinecart.class);
      if (!list.isEmpty()) {
         flag1 = true;
      }

      if (flag != flag1) {
         org.bukkit.block.Block block = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
         BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(block, flag ? 15 : 0, flag1 ? 15 : 0);
         world.getServer().getPluginManager().callEvent(eventRedstone);
         flag1 = eventRedstone.getNewCurrent() > 0;
      }

      if (flag1 && !flag) {
         world.setBlockState(blockposition, iblockdata.withProperty(POWERED, Boolean.valueOf(true)), 3);
         this.updateConnectedRails(world, blockposition, iblockdata, true);
         world.notifyNeighborsOfStateChange(blockposition, this);
         world.notifyNeighborsOfStateChange(blockposition.down(), this);
         world.markBlockRangeForRenderUpdate(blockposition, blockposition);
      }

      if (!flag1 && flag) {
         world.setBlockState(blockposition, iblockdata.withProperty(POWERED, Boolean.valueOf(false)), 3);
         this.updateConnectedRails(world, blockposition, iblockdata, false);
         world.notifyNeighborsOfStateChange(blockposition, this);
         world.notifyNeighborsOfStateChange(blockposition.down(), this);
         world.markBlockRangeForRenderUpdate(blockposition, blockposition);
      }

      if (flag1) {
         world.scheduleUpdate(new BlockPos(blockposition), this, this.tickRate(world));
      }

      world.updateComparatorOutputLevel(blockposition, this);
   }

   protected void updateConnectedRails(World world, BlockPos blockposition, IBlockState iblockdata, boolean flag) {
      BlockRailBase.Rail blockminecarttrackabstract_minecarttracklogic = new BlockRailBase.Rail(world, blockposition, iblockdata);

      for(BlockPos blockposition1 : blockminecarttrackabstract_minecarttracklogic.getConnectedRails()) {
         IBlockState iblockdata1 = world.getBlockState(blockposition1);
         if (iblockdata1 != null) {
            iblockdata1.neighborChanged(world, blockposition1, iblockdata1.getBlock());
         }
      }

   }

   public void onBlockAdded(World world, BlockPos blockposition, IBlockState iblockdata) {
      super.onBlockAdded(world, blockposition, iblockdata);
      this.updatePoweredState(world, blockposition, iblockdata);
   }

   public IProperty getShapeProperty() {
      return SHAPE;
   }

   public boolean hasComparatorInputOverride(IBlockState iblockdata) {
      return true;
   }

   public int getComparatorInputOverride(IBlockState iblockdata, World world, BlockPos blockposition) {
      if (((Boolean)iblockdata.getValue(POWERED)).booleanValue()) {
         List list = this.findMinecarts(world, blockposition, EntityMinecartCommandBlock.class);
         if (!list.isEmpty()) {
            return ((EntityMinecartCommandBlock)list.get(0)).getCommandBlockLogic().getSuccessCount();
         }

         List list1 = this.findMinecarts(world, blockposition, EntityMinecart.class, EntitySelectors.HAS_INVENTORY);
         if (!list1.isEmpty()) {
            return Container.calcRedstoneFromInventory((IInventory)list1.get(0));
         }
      }

      return 0;
   }

   protected List findMinecarts(World world, BlockPos blockposition, Class oclass, Predicate... apredicate) {
      AxisAlignedBB axisalignedbb = this.getDectectionBox(blockposition);
      return apredicate.length != 1 ? world.getEntitiesWithinAABB(oclass, axisalignedbb) : world.getEntitiesWithinAABB(oclass, axisalignedbb, apredicate[0]);
   }

   private AxisAlignedBB getDectectionBox(BlockPos blockposition) {
      return new AxisAlignedBB((double)((float)blockposition.getX() + 0.2F), (double)blockposition.getY(), (double)((float)blockposition.getZ() + 0.2F), (double)((float)(blockposition.getX() + 1) - 0.2F), (double)((float)(blockposition.getY() + 1) - 0.2F), (double)((float)(blockposition.getZ() + 1) - 0.2F));
   }

   public IBlockState getStateFromMeta(int i) {
      return this.getDefaultState().withProperty(SHAPE, BlockRailBase.EnumRailDirection.byMetadata(i & 7)).withProperty(POWERED, Boolean.valueOf((i & 8) > 0));
   }

   public int getMetaFromState(IBlockState iblockdata) {
      byte b0 = 0;
      int i = b0 | ((BlockRailBase.EnumRailDirection)iblockdata.getValue(SHAPE)).getMetadata();
      if (((Boolean)iblockdata.getValue(POWERED)).booleanValue()) {
         i |= 8;
      }

      return i;
   }

   public IBlockState withRotation(IBlockState iblockdata, Rotation enumblockrotation) {
      switch(BlockRailDetector.SyntheticClass_1.b[enumblockrotation.ordinal()]) {
      case 1:
         switch(BlockRailDetector.SyntheticClass_1.a[((BlockRailBase.EnumRailDirection)iblockdata.getValue(SHAPE)).ordinal()]) {
         case 1:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);
         case 2:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);
         case 3:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);
         case 4:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);
         case 5:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         case 6:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case 7:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         case 8:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         }
      case 2:
         switch(BlockRailDetector.SyntheticClass_1.a[((BlockRailBase.EnumRailDirection)iblockdata.getValue(SHAPE)).ordinal()]) {
         case 1:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);
         case 2:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);
         case 3:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);
         case 4:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);
         case 5:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case 6:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         case 7:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         case 8:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         case 9:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.EAST_WEST);
         case 10:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH);
         }
      case 3:
         switch(BlockRailDetector.SyntheticClass_1.a[((BlockRailBase.EnumRailDirection)iblockdata.getValue(SHAPE)).ordinal()]) {
         case 1:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);
         case 2:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);
         case 3:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);
         case 4:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);
         case 5:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         case 6:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         case 7:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case 8:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         case 9:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.EAST_WEST);
         case 10:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH);
         }
      default:
         return iblockdata;
      }
   }

   public IBlockState withMirror(IBlockState iblockdata, Mirror enumblockmirror) {
      BlockRailBase.EnumRailDirection blockminecarttrackabstract_enumtrackposition = (BlockRailBase.EnumRailDirection)iblockdata.getValue(SHAPE);
      switch(BlockRailDetector.SyntheticClass_1.c[enumblockmirror.ordinal()]) {
      case 1:
         switch(BlockRailDetector.SyntheticClass_1.a[blockminecarttrackabstract_enumtrackposition.ordinal()]) {
         case 3:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);
         case 4:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);
         case 5:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case 6:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         case 7:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         case 8:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         default:
            return super.withMirror(iblockdata, enumblockmirror);
         }
      case 2:
         switch(BlockRailDetector.SyntheticClass_1.a[blockminecarttrackabstract_enumtrackposition.ordinal()]) {
         case 1:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);
         case 2:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);
         case 3:
         case 4:
         default:
            break;
         case 5:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         case 6:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         case 7:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case 8:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         }
      default:
         return super.withMirror(iblockdata, enumblockmirror);
      }
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{SHAPE, POWERED});
   }

   static class SyntheticClass_1 {
      static final int[] a;
      static final int[] b;
      static final int[] c = new int[Mirror.values().length];

      static {
         try {
            c[Mirror.LEFT_RIGHT.ordinal()] = 1;
         } catch (NoSuchFieldError var14) {
            ;
         }

         try {
            c[Mirror.FRONT_BACK.ordinal()] = 2;
         } catch (NoSuchFieldError var13) {
            ;
         }

         b = new int[Rotation.values().length];

         try {
            b[Rotation.CLOCKWISE_180.ordinal()] = 1;
         } catch (NoSuchFieldError var12) {
            ;
         }

         try {
            b[Rotation.COUNTERCLOCKWISE_90.ordinal()] = 2;
         } catch (NoSuchFieldError var11) {
            ;
         }

         try {
            b[Rotation.CLOCKWISE_90.ordinal()] = 3;
         } catch (NoSuchFieldError var10) {
            ;
         }

         a = new int[BlockRailBase.EnumRailDirection.values().length];

         try {
            a[BlockRailBase.EnumRailDirection.ASCENDING_EAST.ordinal()] = 1;
         } catch (NoSuchFieldError var9) {
            ;
         }

         try {
            a[BlockRailBase.EnumRailDirection.ASCENDING_WEST.ordinal()] = 2;
         } catch (NoSuchFieldError var8) {
            ;
         }

         try {
            a[BlockRailBase.EnumRailDirection.ASCENDING_NORTH.ordinal()] = 3;
         } catch (NoSuchFieldError var7) {
            ;
         }

         try {
            a[BlockRailBase.EnumRailDirection.ASCENDING_SOUTH.ordinal()] = 4;
         } catch (NoSuchFieldError var6) {
            ;
         }

         try {
            a[BlockRailBase.EnumRailDirection.SOUTH_EAST.ordinal()] = 5;
         } catch (NoSuchFieldError var5) {
            ;
         }

         try {
            a[BlockRailBase.EnumRailDirection.SOUTH_WEST.ordinal()] = 6;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            a[BlockRailBase.EnumRailDirection.NORTH_WEST.ordinal()] = 7;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            a[BlockRailBase.EnumRailDirection.NORTH_EAST.ordinal()] = 8;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            a[BlockRailBase.EnumRailDirection.NORTH_SOUTH.ordinal()] = 9;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[BlockRailBase.EnumRailDirection.EAST_WEST.ordinal()] = 10;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
