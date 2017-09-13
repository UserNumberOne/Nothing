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

public class BlockRailPowered extends BlockRailBase {
   public static final PropertyEnum SHAPE = PropertyEnum.create("shape", BlockRailBase.EnumRailDirection.class, new Predicate() {
      public boolean apply(@Nullable BlockRailBase.EnumRailDirection var1) {
         return var1 != BlockRailBase.EnumRailDirection.NORTH_EAST && var1 != BlockRailBase.EnumRailDirection.NORTH_WEST && var1 != BlockRailBase.EnumRailDirection.SOUTH_EAST && var1 != BlockRailBase.EnumRailDirection.SOUTH_WEST;
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
         switch(var10) {
         case NORTH_SOUTH:
            if (var4) {
               ++var8;
            } else {
               --var8;
            }
            break;
         case EAST_WEST:
            if (var4) {
               --var6;
            } else {
               ++var6;
            }
            break;
         case ASCENDING_EAST:
            if (var4) {
               --var6;
            } else {
               ++var6;
               ++var7;
               var9 = false;
            }

            var10 = BlockRailBase.EnumRailDirection.EAST_WEST;
            break;
         case ASCENDING_WEST:
            if (var4) {
               --var6;
               ++var7;
               var9 = false;
            } else {
               ++var6;
            }

            var10 = BlockRailBase.EnumRailDirection.EAST_WEST;
            break;
         case ASCENDING_NORTH:
            if (var4) {
               ++var8;
            } else {
               --var8;
               ++var7;
               var9 = false;
            }

            var10 = BlockRailBase.EnumRailDirection.NORTH_SOUTH;
            break;
         case ASCENDING_SOUTH:
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
         case NORTH_SOUTH:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.EAST_WEST);
         case EAST_WEST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH);
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
         }
      case CLOCKWISE_90:
         switch((BlockRailBase.EnumRailDirection)var1.getValue(SHAPE)) {
         case NORTH_SOUTH:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.EAST_WEST);
         case EAST_WEST:
            return var1.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH);
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
