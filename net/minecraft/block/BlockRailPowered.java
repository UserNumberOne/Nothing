package net.minecraft.block;

import com.google.common.base.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;

public class BlockRailPowered extends BlockRailBase {
   public static final PropertyEnum SHAPE = PropertyEnum.create("shape", BlockRailBase.EnumRailDirection.class, new Predicate() {
      public boolean apply(@Nullable BlockRailBase.EnumRailDirection blockminecarttrackabstract_enumtrackposition) {
         return blockminecarttrackabstract_enumtrackposition != BlockRailBase.EnumRailDirection.NORTH_EAST && blockminecarttrackabstract_enumtrackposition != BlockRailBase.EnumRailDirection.NORTH_WEST && blockminecarttrackabstract_enumtrackposition != BlockRailBase.EnumRailDirection.SOUTH_EAST && blockminecarttrackabstract_enumtrackposition != BlockRailBase.EnumRailDirection.SOUTH_WEST;
      }

      public boolean apply(Object object) {
         return this.apply((BlockRailBase.EnumRailDirection)object);
      }
   });
   public static final PropertyBool POWERED = PropertyBool.create("powered");

   protected BlockRailPowered() {
      super(true);
      this.setDefaultState(this.blockState.getBaseState().withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH).withProperty(POWERED, Boolean.valueOf(false)));
   }

   protected boolean findPoweredRailSignal(World world, BlockPos blockposition, IBlockState iblockdata, boolean flag, int i) {
      if (i >= 8) {
         return false;
      } else {
         int j = blockposition.getX();
         int k = blockposition.getY();
         int l = blockposition.getZ();
         boolean flag1 = true;
         BlockRailBase.EnumRailDirection blockminecarttrackabstract_enumtrackposition = (BlockRailBase.EnumRailDirection)iblockdata.getValue(SHAPE);
         switch(BlockRailPowered.SyntheticClass_1.a[blockminecarttrackabstract_enumtrackposition.ordinal()]) {
         case 1:
            if (flag) {
               ++l;
            } else {
               --l;
            }
            break;
         case 2:
            if (flag) {
               --j;
            } else {
               ++j;
            }
            break;
         case 3:
            if (flag) {
               --j;
            } else {
               ++j;
               ++k;
               flag1 = false;
            }

            blockminecarttrackabstract_enumtrackposition = BlockRailBase.EnumRailDirection.EAST_WEST;
            break;
         case 4:
            if (flag) {
               --j;
               ++k;
               flag1 = false;
            } else {
               ++j;
            }

            blockminecarttrackabstract_enumtrackposition = BlockRailBase.EnumRailDirection.EAST_WEST;
            break;
         case 5:
            if (flag) {
               ++l;
            } else {
               --l;
               ++k;
               flag1 = false;
            }

            blockminecarttrackabstract_enumtrackposition = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
            break;
         case 6:
            if (flag) {
               ++l;
               ++k;
               flag1 = false;
            } else {
               --l;
            }

            blockminecarttrackabstract_enumtrackposition = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
         }

         return this.isSameRailWithPower(world, new BlockPos(j, k, l), flag, i, blockminecarttrackabstract_enumtrackposition) ? true : flag1 && this.isSameRailWithPower(world, new BlockPos(j, k - 1, l), flag, i, blockminecarttrackabstract_enumtrackposition);
      }
   }

   protected boolean isSameRailWithPower(World world, BlockPos blockposition, boolean flag, int i, BlockRailBase.EnumRailDirection blockminecarttrackabstract_enumtrackposition) {
      IBlockState iblockdata = world.getBlockState(blockposition);
      if (iblockdata.getBlock() != this) {
         return false;
      } else {
         BlockRailBase.EnumRailDirection blockminecarttrackabstract_enumtrackposition1 = (BlockRailBase.EnumRailDirection)iblockdata.getValue(SHAPE);
         return blockminecarttrackabstract_enumtrackposition != BlockRailBase.EnumRailDirection.EAST_WEST || blockminecarttrackabstract_enumtrackposition1 != BlockRailBase.EnumRailDirection.NORTH_SOUTH && blockminecarttrackabstract_enumtrackposition1 != BlockRailBase.EnumRailDirection.ASCENDING_NORTH && blockminecarttrackabstract_enumtrackposition1 != BlockRailBase.EnumRailDirection.ASCENDING_SOUTH ? (blockminecarttrackabstract_enumtrackposition != BlockRailBase.EnumRailDirection.NORTH_SOUTH || blockminecarttrackabstract_enumtrackposition1 != BlockRailBase.EnumRailDirection.EAST_WEST && blockminecarttrackabstract_enumtrackposition1 != BlockRailBase.EnumRailDirection.ASCENDING_EAST && blockminecarttrackabstract_enumtrackposition1 != BlockRailBase.EnumRailDirection.ASCENDING_WEST ? (((Boolean)iblockdata.getValue(POWERED)).booleanValue() ? (world.isBlockPowered(blockposition) ? true : this.findPoweredRailSignal(world, blockposition, iblockdata, flag, i + 1)) : false) : false) : false;
      }
   }

   protected void updateState(IBlockState iblockdata, World world, BlockPos blockposition, Block block) {
      boolean flag = ((Boolean)iblockdata.getValue(POWERED)).booleanValue();
      boolean flag1 = world.isBlockPowered(blockposition) || this.findPoweredRailSignal(world, blockposition, iblockdata, true, 0) || this.findPoweredRailSignal(world, blockposition, iblockdata, false, 0);
      if (flag1 != flag) {
         int power = ((Boolean)iblockdata.getValue(POWERED)).booleanValue() ? 15 : 0;
         int newPower = CraftEventFactory.callRedstoneChange(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), power, 15 - power).getNewCurrent();
         if (newPower == power) {
            return;
         }

         world.setBlockState(blockposition, iblockdata.withProperty(POWERED, Boolean.valueOf(flag1)), 3);
         world.notifyNeighborsOfStateChange(blockposition.down(), this);
         if (((BlockRailBase.EnumRailDirection)iblockdata.getValue(SHAPE)).isAscending()) {
            world.notifyNeighborsOfStateChange(blockposition.up(), this);
         }
      }

   }

   public IProperty getShapeProperty() {
      return SHAPE;
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
      switch(BlockRailPowered.SyntheticClass_1.b[enumblockrotation.ordinal()]) {
      case 1:
         switch(BlockRailPowered.SyntheticClass_1.a[((BlockRailBase.EnumRailDirection)iblockdata.getValue(SHAPE)).ordinal()]) {
         case 3:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);
         case 4:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);
         case 5:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);
         case 6:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);
         case 7:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         case 8:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case 9:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         case 10:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         }
      case 2:
         switch(BlockRailPowered.SyntheticClass_1.a[((BlockRailBase.EnumRailDirection)iblockdata.getValue(SHAPE)).ordinal()]) {
         case 1:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.EAST_WEST);
         case 2:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH);
         case 3:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);
         case 4:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);
         case 5:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);
         case 6:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);
         case 7:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case 8:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         case 9:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         case 10:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         }
      case 3:
         switch(BlockRailPowered.SyntheticClass_1.a[((BlockRailBase.EnumRailDirection)iblockdata.getValue(SHAPE)).ordinal()]) {
         case 1:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.EAST_WEST);
         case 2:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH);
         case 3:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);
         case 4:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);
         case 5:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);
         case 6:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);
         case 7:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         case 8:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         case 9:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case 10:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         }
      default:
         return iblockdata;
      }
   }

   public IBlockState withMirror(IBlockState iblockdata, Mirror enumblockmirror) {
      BlockRailBase.EnumRailDirection blockminecarttrackabstract_enumtrackposition = (BlockRailBase.EnumRailDirection)iblockdata.getValue(SHAPE);
      switch(BlockRailPowered.SyntheticClass_1.c[enumblockmirror.ordinal()]) {
      case 1:
         switch(BlockRailPowered.SyntheticClass_1.a[blockminecarttrackabstract_enumtrackposition.ordinal()]) {
         case 5:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);
         case 6:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);
         case 7:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case 8:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         case 9:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         case 10:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         default:
            return super.withMirror(iblockdata, enumblockmirror);
         }
      case 2:
         switch(BlockRailPowered.SyntheticClass_1.a[blockminecarttrackabstract_enumtrackposition.ordinal()]) {
         case 3:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);
         case 4:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);
         case 5:
         case 6:
         default:
            break;
         case 7:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         case 8:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         case 9:
            return iblockdata.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case 10:
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
            a[BlockRailBase.EnumRailDirection.NORTH_SOUTH.ordinal()] = 1;
         } catch (NoSuchFieldError var9) {
            ;
         }

         try {
            a[BlockRailBase.EnumRailDirection.EAST_WEST.ordinal()] = 2;
         } catch (NoSuchFieldError var8) {
            ;
         }

         try {
            a[BlockRailBase.EnumRailDirection.ASCENDING_EAST.ordinal()] = 3;
         } catch (NoSuchFieldError var7) {
            ;
         }

         try {
            a[BlockRailBase.EnumRailDirection.ASCENDING_WEST.ordinal()] = 4;
         } catch (NoSuchFieldError var6) {
            ;
         }

         try {
            a[BlockRailBase.EnumRailDirection.ASCENDING_NORTH.ordinal()] = 5;
         } catch (NoSuchFieldError var5) {
            ;
         }

         try {
            a[BlockRailBase.EnumRailDirection.ASCENDING_SOUTH.ordinal()] = 6;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            a[BlockRailBase.EnumRailDirection.SOUTH_EAST.ordinal()] = 7;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            a[BlockRailBase.EnumRailDirection.SOUTH_WEST.ordinal()] = 8;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            a[BlockRailBase.EnumRailDirection.NORTH_WEST.ordinal()] = 9;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[BlockRailBase.EnumRailDirection.NORTH_EAST.ordinal()] = 10;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
