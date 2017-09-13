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
      public boolean apply(@Nullable BlockRailBase.EnumRailDirection var1) {
         return var1 != BlockRailBase.EnumRailDirection.NORTH_EAST && var1 != BlockRailBase.EnumRailDirection.NORTH_WEST && var1 != BlockRailBase.EnumRailDirection.SOUTH_EAST && var1 != BlockRailBase.EnumRailDirection.SOUTH_WEST;
      }

      public boolean apply(Object var1) {
         return this.apply((BlockRailBase.EnumRailDirection)var1);
      }
   });
   public static final PropertyBool POWERED = PropertyBool.create("powered");

   protected BlockRailPowered() {
      super(true);
      this.setDefaultState(this.blockState.getBaseState().withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH).withProperty(POWERED, Boolean.valueOf(false)));
   }

   protected boolean findPoweredRailSignal(World var1, BlockPos var2, IBlockState var3, boolean var4, int var5) {
      if (var5 >= 8) {
         return false;
      } else {
         int var6 = var2.getX();
         int var7 = var2.getY();
         int var8 = var2.getZ();
         boolean var9 = true;
         BlockRailBase.EnumRailDirection var10 = (BlockRailBase.EnumRailDirection)var3.getValue(SHAPE);
         switch(BlockRailPowered.SyntheticClass_1.a[var10.ordinal()]) {
         case 1:
            if (var4) {
               ++var8;
            } else {
               --var8;
            }
            break;
         case 2:
            if (var4) {
               --var6;
            } else {
               ++var6;
            }
            break;
         case 3:
            if (var4) {
               --var6;
            } else {
               ++var6;
               ++var7;
               var9 = false;
            }

            var10 = BlockRailBase.EnumRailDirection.EAST_WEST;
            break;
         case 4:
            if (var4) {
               --var6;
               ++var7;
               var9 = false;
            } else {
               ++var6;
            }

            var10 = BlockRailBase.EnumRailDirection.EAST_WEST;
            break;
         case 5:
            if (var4) {
               ++var8;
            } else {
               --var8;
               ++var7;
               var9 = false;
            }

            var10 = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
            break;
         case 6:
            if (var4) {
               ++var8;
               ++var7;
               var9 = false;
            } else {
               --var8;
            }

            var10 = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
         }

         return this.isSameRailWithPower(var1, new BlockPos(var6, var7, var8), var4, var5, var10) ? true : var9 && this.isSameRailWithPower(var1, new BlockPos(var6, var7 - 1, var8), var4, var5, var10);
      }
   }

   protected boolean isSameRailWithPower(World var1, BlockPos var2, boolean var3, int var4, BlockRailBase.EnumRailDirection var5) {
      IBlockState var6 = var1.getBlockState(var2);
      if (var6.getBlock() != this) {
         return false;
      } else {
         BlockRailBase.EnumRailDirection var7 = (BlockRailBase.EnumRailDirection)var6.getValue(SHAPE);
         return var5 != BlockRailBase.EnumRailDirection.EAST_WEST || var7 != BlockRailBase.EnumRailDirection.NORTH_SOUTH && var7 != BlockRailBase.EnumRailDirection.ASCENDING_NORTH && var7 != BlockRailBase.EnumRailDirection.ASCENDING_SOUTH ? (var5 != BlockRailBase.EnumRailDirection.NORTH_SOUTH || var7 != BlockRailBase.EnumRailDirection.EAST_WEST && var7 != BlockRailBase.EnumRailDirection.ASCENDING_EAST && var7 != BlockRailBase.EnumRailDirection.ASCENDING_WEST ? (((Boolean)var6.getValue(POWERED)).booleanValue() ? (var1.isBlockPowered(var2) ? true : this.findPoweredRailSignal(var1, var2, var6, var3, var4 + 1)) : false) : false) : false;
      }
   }

   protected void updateState(IBlockState var1, World var2, BlockPos var3, Block var4) {
      boolean var5 = ((Boolean)var1.getValue(POWERED)).booleanValue();
      boolean var6 = var2.isBlockPowered(var3) || this.findPoweredRailSignal(var2, var3, var1, true, 0) || this.findPoweredRailSignal(var2, var3, var1, false, 0);
      if (var6 != var5) {
         int var7 = ((Boolean)var1.getValue(POWERED)).booleanValue() ? 15 : 0;
         int var8 = CraftEventFactory.callRedstoneChange(var2, var3.getX(), var3.getY(), var3.getZ(), var7, 15 - var7).getNewCurrent();
         if (var8 == var7) {
            return;
         }

         var2.setBlockState(var3, var1.withProperty(POWERED, Boolean.valueOf(var6)), 3);
         var2.notifyNeighborsOfStateChange(var3.down(), this);
         if (((BlockRailBase.EnumRailDirection)var1.getValue(SHAPE)).isAscending()) {
            var2.notifyNeighborsOfStateChange(var3.up(), this);
         }
      }

   }

   public IProperty getShapeProperty() {
      return SHAPE;
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
      switch(BlockRailPowered.SyntheticClass_1.b[var2.ordinal()]) {
      case 1:
         switch(BlockRailPowered.SyntheticClass_1.a[((BlockRailBase.EnumRailDirection)var1.getValue(SHAPE)).ordinal()]) {
         case 3:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);
         case 4:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);
         case 5:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);
         case 6:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);
         case 7:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         case 8:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case 9:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         case 10:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         }
      case 2:
         switch(BlockRailPowered.SyntheticClass_1.a[((BlockRailBase.EnumRailDirection)var1.getValue(SHAPE)).ordinal()]) {
         case 1:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.EAST_WEST);
         case 2:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH);
         case 3:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);
         case 4:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);
         case 5:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);
         case 6:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);
         case 7:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case 8:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         case 9:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         case 10:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         }
      case 3:
         switch(BlockRailPowered.SyntheticClass_1.a[((BlockRailBase.EnumRailDirection)var1.getValue(SHAPE)).ordinal()]) {
         case 1:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.EAST_WEST);
         case 2:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH);
         case 3:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);
         case 4:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);
         case 5:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);
         case 6:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);
         case 7:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         case 8:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         case 9:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case 10:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         }
      default:
         return var1;
      }
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      BlockRailBase.EnumRailDirection var3 = (BlockRailBase.EnumRailDirection)var1.getValue(SHAPE);
      switch(BlockRailPowered.SyntheticClass_1.c[var2.ordinal()]) {
      case 1:
         switch(BlockRailPowered.SyntheticClass_1.a[var3.ordinal()]) {
         case 5:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_SOUTH);
         case 6:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_NORTH);
         case 7:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case 8:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_WEST);
         case 9:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         case 10:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         default:
            return super.withMirror(var1, var2);
         }
      case 2:
         switch(BlockRailPowered.SyntheticClass_1.a[var3.ordinal()]) {
         case 3:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_WEST);
         case 4:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.ASCENDING_EAST);
         case 5:
         case 6:
         default:
            break;
         case 7:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_WEST);
         case 8:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.SOUTH_EAST);
         case 9:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_EAST);
         case 10:
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
