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
      public boolean apply(@Nullable BlockRailBase.EnumRailDirection var1) {
         return var1 != BlockRailBase.EnumRailDirection.NORTH_EAST && var1 != BlockRailBase.EnumRailDirection.NORTH_WEST && var1 != BlockRailBase.EnumRailDirection.SOUTH_EAST && var1 != BlockRailBase.EnumRailDirection.SOUTH_WEST;
      }

      public boolean apply(Object var1) {
         return this.apply((BlockRailBase.EnumRailDirection)var1);
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

      if (var4 != var5) {
         org.bukkit.block.Block var7 = var1.getWorld().getBlockAt(var2.getX(), var2.getY(), var2.getZ());
         BlockRedstoneEvent var8 = new BlockRedstoneEvent(var7, var4 ? 15 : 0, var5 ? 15 : 0);
         var1.getServer().getPluginManager().callEvent(var8);
         var5 = var8.getNewCurrent() > 0;
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

      for(BlockPos var8 : var5.getConnectedRails()) {
         IBlockState var9 = var1.getBlockState(var8);
         if (var9 != null) {
            var9.neighborChanged(var1, var8, var9.getBlock());
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
         List var4 = this.findMinecarts(var2, var3, EntityMinecartCommandBlock.class);
         if (!var4.isEmpty()) {
            return ((EntityMinecartCommandBlock)var4.get(0)).getCommandBlockLogic().getSuccessCount();
         }

         List var5 = this.findMinecarts(var2, var3, EntityMinecart.class, EntitySelectors.HAS_INVENTORY);
         if (!var5.isEmpty()) {
            return Container.calcRedstoneFromInventory((IInventory)var5.get(0));
         }
      }

      return 0;
   }

   protected List findMinecarts(World var1, BlockPos var2, Class var3, Predicate... var4) {
      AxisAlignedBB var5 = this.getDectectionBox(var2);
      return var4.length != 1 ? var1.getEntitiesWithinAABB(var3, var5) : var1.getEntitiesWithinAABB(var3, var5, var4[0]);
   }

   private AxisAlignedBB getDectectionBox(BlockPos var1) {
      return new AxisAlignedBB((double)((float)var1.getX() + 0.2F), (double)var1.getY(), (double)((float)var1.getZ() + 0.2F), (double)((float)(var1.getX() + 1) - 0.2F), (double)((float)(var1.getY() + 1) - 0.2F), (double)((float)(var1.getZ() + 1) - 0.2F));
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(SHAPE, BlockRailBase.EnumRailDirection.byMetadata(var1 & 7)).withProperty(POWERED, Boolean.valueOf((var1 & 8) > 0));
   }

   public int getMetaFromState(IBlockState var1) {
      byte var2 = 0;
      int var3 = var2 | ((BlockRailBase.EnumRailDirection)var1.getValue(SHAPE)).getMetadata();
      if (((Boolean)var1.getValue(POWERED)).booleanValue()) {
         var3 |= 8;
      }

      return var3;
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      switch(BlockRailDetector.SyntheticClass_1.b[var2.ordinal()]) {
      case 1:
         switch(BlockRailDetector.SyntheticClass_1.a[((BlockRailBase.EnumRailDirection)var1.getValue(SHAPE)).ordinal()]) {
         case 1:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);
         case 2:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);
         case 3:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);
         case 4:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);
         case 5:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         case 6:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case 7:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         case 8:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         }
      case 2:
         switch(BlockRailDetector.SyntheticClass_1.a[((BlockRailBase.EnumRailDirection)var1.getValue(SHAPE)).ordinal()]) {
         case 1:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);
         case 2:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);
         case 3:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);
         case 4:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);
         case 5:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case 6:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         case 7:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         case 8:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         case 9:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.EAST_WEST);
         case 10:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH);
         }
      case 3:
         switch(BlockRailDetector.SyntheticClass_1.a[((BlockRailBase.EnumRailDirection)var1.getValue(SHAPE)).ordinal()]) {
         case 1:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);
         case 2:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);
         case 3:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);
         case 4:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);
         case 5:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         case 6:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         case 7:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case 8:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         case 9:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.EAST_WEST);
         case 10:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH);
         }
      default:
         return var1;
      }
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      BlockRailBase.EnumRailDirection var3 = (BlockRailBase.EnumRailDirection)var1.getValue(SHAPE);
      switch(BlockRailDetector.SyntheticClass_1.c[var2.ordinal()]) {
      case 1:
         switch(BlockRailDetector.SyntheticClass_1.a[var3.ordinal()]) {
         case 3:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);
         case 4:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);
         case 5:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case 6:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         case 7:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         case 8:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         default:
            return super.withMirror(var1, var2);
         }
      case 2:
         switch(BlockRailDetector.SyntheticClass_1.a[var3.ordinal()]) {
         case 1:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);
         case 2:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);
         case 3:
         case 4:
         default:
            break;
         case 5:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         case 6:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         case 7:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case 8:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         }
      default:
         return super.withMirror(var1, var2);
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
