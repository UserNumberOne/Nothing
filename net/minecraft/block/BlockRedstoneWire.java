package net.minecraft.block;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.event.block.BlockRedstoneEvent;

public class BlockRedstoneWire extends Block {
   public static final PropertyEnum NORTH = PropertyEnum.create("north", BlockRedstoneWire.EnumAttachPosition.class);
   public static final PropertyEnum EAST = PropertyEnum.create("east", BlockRedstoneWire.EnumAttachPosition.class);
   public static final PropertyEnum SOUTH = PropertyEnum.create("south", BlockRedstoneWire.EnumAttachPosition.class);
   public static final PropertyEnum WEST = PropertyEnum.create("west", BlockRedstoneWire.EnumAttachPosition.class);
   public static final PropertyInteger POWER = PropertyInteger.create("power", 0, 15);
   protected static final AxisAlignedBB[] REDSTONE_WIRE_AABB = new AxisAlignedBB[]{new AxisAlignedBB(0.1875D, 0.0D, 0.1875D, 0.8125D, 0.0625D, 0.8125D), new AxisAlignedBB(0.1875D, 0.0D, 0.1875D, 0.8125D, 0.0625D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.1875D, 0.8125D, 0.0625D, 0.8125D), new AxisAlignedBB(0.0D, 0.0D, 0.1875D, 0.8125D, 0.0625D, 1.0D), new AxisAlignedBB(0.1875D, 0.0D, 0.0D, 0.8125D, 0.0625D, 0.8125D), new AxisAlignedBB(0.1875D, 0.0D, 0.0D, 0.8125D, 0.0625D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.8125D, 0.0625D, 0.8125D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.8125D, 0.0625D, 1.0D), new AxisAlignedBB(0.1875D, 0.0D, 0.1875D, 1.0D, 0.0625D, 0.8125D), new AxisAlignedBB(0.1875D, 0.0D, 0.1875D, 1.0D, 0.0625D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.1875D, 1.0D, 0.0625D, 0.8125D), new AxisAlignedBB(0.0D, 0.0D, 0.1875D, 1.0D, 0.0625D, 1.0D), new AxisAlignedBB(0.1875D, 0.0D, 0.0D, 1.0D, 0.0625D, 0.8125D), new AxisAlignedBB(0.1875D, 0.0D, 0.0D, 1.0D, 0.0625D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.0625D, 0.8125D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.0625D, 1.0D)};
   private boolean canProvidePower = true;
   private final Set blocksNeedingUpdate = Sets.newHashSet();

   public BlockRedstoneWire() {
      super(Material.CIRCUITS);
      this.setDefaultState(this.blockState.getBaseState().withProperty(NORTH, BlockRedstoneWire.EnumAttachPosition.NONE).withProperty(EAST, BlockRedstoneWire.EnumAttachPosition.NONE).withProperty(SOUTH, BlockRedstoneWire.EnumAttachPosition.NONE).withProperty(WEST, BlockRedstoneWire.EnumAttachPosition.NONE).withProperty(POWER, Integer.valueOf(0)));
   }

   public AxisAlignedBB getBoundingBox(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      return REDSTONE_WIRE_AABB[getAABBIndex(iblockdata.getActualState(iblockaccess, blockposition))];
   }

   private static int getAABBIndex(IBlockState iblockdata) {
      int i = 0;
      boolean flag = iblockdata.getValue(NORTH) != BlockRedstoneWire.EnumAttachPosition.NONE;
      boolean flag1 = iblockdata.getValue(EAST) != BlockRedstoneWire.EnumAttachPosition.NONE;
      boolean flag2 = iblockdata.getValue(SOUTH) != BlockRedstoneWire.EnumAttachPosition.NONE;
      boolean flag3 = iblockdata.getValue(WEST) != BlockRedstoneWire.EnumAttachPosition.NONE;
      if (flag || flag2 && !flag && !flag1 && !flag3) {
         i |= 1 << EnumFacing.NORTH.getHorizontalIndex();
      }

      if (flag1 || flag3 && !flag && !flag1 && !flag2) {
         i |= 1 << EnumFacing.EAST.getHorizontalIndex();
      }

      if (flag2 || flag && !flag1 && !flag2 && !flag3) {
         i |= 1 << EnumFacing.SOUTH.getHorizontalIndex();
      }

      if (flag3 || flag1 && !flag && !flag2 && !flag3) {
         i |= 1 << EnumFacing.WEST.getHorizontalIndex();
      }

      return i;
   }

   public IBlockState getActualState(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      iblockdata = iblockdata.withProperty(WEST, this.getAttachPosition(iblockaccess, blockposition, EnumFacing.WEST));
      iblockdata = iblockdata.withProperty(EAST, this.getAttachPosition(iblockaccess, blockposition, EnumFacing.EAST));
      iblockdata = iblockdata.withProperty(NORTH, this.getAttachPosition(iblockaccess, blockposition, EnumFacing.NORTH));
      iblockdata = iblockdata.withProperty(SOUTH, this.getAttachPosition(iblockaccess, blockposition, EnumFacing.SOUTH));
      return iblockdata;
   }

   private BlockRedstoneWire.EnumAttachPosition getAttachPosition(IBlockAccess iblockaccess, BlockPos blockposition, EnumFacing enumdirection) {
      BlockPos blockposition1 = blockposition.offset(enumdirection);
      IBlockState iblockdata = iblockaccess.getBlockState(blockposition.offset(enumdirection));
      if (!canConnectTo(iblockaccess.getBlockState(blockposition1), enumdirection) && (iblockdata.isNormalCube() || !canConnectUpwardsTo(iblockaccess.getBlockState(blockposition1.down())))) {
         IBlockState iblockdata1 = iblockaccess.getBlockState(blockposition.up());
         if (!iblockdata1.isNormalCube()) {
            boolean flag = iblockaccess.getBlockState(blockposition1).isFullyOpaque() || iblockaccess.getBlockState(blockposition1).getBlock() == Blocks.GLOWSTONE;
            if (flag && canConnectUpwardsTo(iblockaccess.getBlockState(blockposition1.up()))) {
               if (iblockdata.isBlockNormalCube()) {
                  return BlockRedstoneWire.EnumAttachPosition.UP;
               }

               return BlockRedstoneWire.EnumAttachPosition.SIDE;
            }
         }

         return BlockRedstoneWire.EnumAttachPosition.NONE;
      } else {
         return BlockRedstoneWire.EnumAttachPosition.SIDE;
      }
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState iblockdata, World world, BlockPos blockposition) {
      return NULL_AABB;
   }

   public boolean isOpaqueCube(IBlockState iblockdata) {
      return false;
   }

   public boolean isFullCube(IBlockState iblockdata) {
      return false;
   }

   public boolean canPlaceBlockAt(World world, BlockPos blockposition) {
      return world.getBlockState(blockposition.down()).isFullyOpaque() || world.getBlockState(blockposition.down()).getBlock() == Blocks.GLOWSTONE;
   }

   private IBlockState updateSurroundingRedstone(World world, BlockPos blockposition, IBlockState iblockdata) {
      iblockdata = this.calculateCurrentChanges(world, blockposition, blockposition, iblockdata);
      ArrayList arraylist = Lists.newArrayList(this.blocksNeedingUpdate);
      this.blocksNeedingUpdate.clear();

      for(BlockPos blockposition1 : arraylist) {
         world.notifyNeighborsOfStateChange(blockposition1, this);
      }

      return iblockdata;
   }

   private IBlockState calculateCurrentChanges(World world, BlockPos blockposition, BlockPos blockposition1, IBlockState iblockdata) {
      IBlockState iblockdata1 = iblockdata;
      int i = ((Integer)iblockdata.getValue(POWER)).intValue();
      byte b0 = 0;
      int j = this.getMaxCurrentStrength(world, blockposition1, b0);
      this.canProvidePower = false;
      int k = world.isBlockIndirectlyGettingPowered(blockposition);
      this.canProvidePower = true;
      if (k > 0 && k > j - 1) {
         j = k;
      }

      int l = 0;

      for(EnumFacing enumdirection : EnumFacing.Plane.HORIZONTAL) {
         BlockPos blockposition2 = blockposition.offset(enumdirection);
         boolean flag = blockposition2.getX() != blockposition1.getX() || blockposition2.getZ() != blockposition1.getZ();
         if (flag) {
            l = this.getMaxCurrentStrength(world, blockposition2, l);
         }

         if (world.getBlockState(blockposition2).isNormalCube() && !world.getBlockState(blockposition.up()).isNormalCube()) {
            if (flag && blockposition.getY() >= blockposition1.getY()) {
               l = this.getMaxCurrentStrength(world, blockposition2.up(), l);
            }
         } else if (!world.getBlockState(blockposition2).isNormalCube() && flag && blockposition.getY() <= blockposition1.getY()) {
            l = this.getMaxCurrentStrength(world, blockposition2.down(), l);
         }
      }

      if (l > j) {
         j = l - 1;
      } else if (j > 0) {
         --j;
      } else {
         j = 0;
      }

      if (k > j - 1) {
         j = k;
      }

      if (i != j) {
         BlockRedstoneEvent event = new BlockRedstoneEvent(world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()), i, j);
         world.getServer().getPluginManager().callEvent(event);
         j = event.getNewCurrent();
      }

      if (i != j) {
         iblockdata = iblockdata.withProperty(POWER, Integer.valueOf(j));
         if (world.getBlockState(blockposition) == iblockdata1) {
            world.setBlockState(blockposition, iblockdata, 2);
         }

         this.blocksNeedingUpdate.add(blockposition);

         for(EnumFacing enumdirection1 : EnumFacing.values()) {
            this.blocksNeedingUpdate.add(blockposition.offset(enumdirection1));
         }
      }

      return iblockdata;
   }

   private void notifyWireNeighborsOfStateChange(World world, BlockPos blockposition) {
      if (world.getBlockState(blockposition).getBlock() == this) {
         world.notifyNeighborsOfStateChange(blockposition, this);

         for(EnumFacing enumdirection : EnumFacing.values()) {
            world.notifyNeighborsOfStateChange(blockposition.offset(enumdirection), this);
         }
      }

   }

   public void onBlockAdded(World world, BlockPos blockposition, IBlockState iblockdata) {
      if (!world.isRemote) {
         this.updateSurroundingRedstone(world, blockposition, iblockdata);

         for(EnumFacing enumdirection : EnumFacing.Plane.VERTICAL) {
            world.notifyNeighborsOfStateChange(blockposition.offset(enumdirection), this);
         }

         for(EnumFacing enumdirection : EnumFacing.Plane.HORIZONTAL) {
            this.notifyWireNeighborsOfStateChange(world, blockposition.offset(enumdirection));
         }

         for(EnumFacing enumdirection : EnumFacing.Plane.HORIZONTAL) {
            BlockPos blockposition1 = blockposition.offset(enumdirection);
            if (world.getBlockState(blockposition1).isNormalCube()) {
               this.notifyWireNeighborsOfStateChange(world, blockposition1.up());
            } else {
               this.notifyWireNeighborsOfStateChange(world, blockposition1.down());
            }
         }
      }

   }

   public void breakBlock(World world, BlockPos blockposition, IBlockState iblockdata) {
      super.breakBlock(world, blockposition, iblockdata);
      if (!world.isRemote) {
         for(EnumFacing enumdirection : EnumFacing.values()) {
            world.notifyNeighborsOfStateChange(blockposition.offset(enumdirection), this);
         }

         this.updateSurroundingRedstone(world, blockposition, iblockdata);

         for(EnumFacing enumdirection1 : EnumFacing.Plane.HORIZONTAL) {
            this.notifyWireNeighborsOfStateChange(world, blockposition.offset(enumdirection1));
         }

         for(EnumFacing enumdirection1 : EnumFacing.Plane.HORIZONTAL) {
            BlockPos blockposition1 = blockposition.offset(enumdirection1);
            if (world.getBlockState(blockposition1).isNormalCube()) {
               this.notifyWireNeighborsOfStateChange(world, blockposition1.up());
            } else {
               this.notifyWireNeighborsOfStateChange(world, blockposition1.down());
            }
         }
      }

   }

   public int getMaxCurrentStrength(World world, BlockPos blockposition, int i) {
      if (world.getBlockState(blockposition).getBlock() != this) {
         return i;
      } else {
         int j = ((Integer)world.getBlockState(blockposition).getValue(POWER)).intValue();
         return j > i ? j : i;
      }
   }

   public void neighborChanged(IBlockState iblockdata, World world, BlockPos blockposition, Block block) {
      if (!world.isRemote) {
         if (this.canPlaceBlockAt(world, blockposition)) {
            this.updateSurroundingRedstone(world, blockposition, iblockdata);
         } else {
            this.dropBlockAsItem(world, blockposition, iblockdata, 0);
            world.setBlockToAir(blockposition);
         }
      }

   }

   @Nullable
   public Item getItemDropped(IBlockState iblockdata, Random random, int i) {
      return Items.REDSTONE;
   }

   public int getStrongPower(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition, EnumFacing enumdirection) {
      return !this.canProvidePower ? 0 : iblockdata.getWeakPower(iblockaccess, blockposition, enumdirection);
   }

   public int getWeakPower(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition, EnumFacing enumdirection) {
      if (!this.canProvidePower) {
         return 0;
      } else {
         int i = ((Integer)iblockdata.getValue(POWER)).intValue();
         if (i == 0) {
            return 0;
         } else if (enumdirection == EnumFacing.UP) {
            return i;
         } else {
            EnumSet enumset = EnumSet.noneOf(EnumFacing.class);

            for(EnumFacing enumdirection1 : EnumFacing.Plane.HORIZONTAL) {
               if (this.isPowerSourceAt(iblockaccess, blockposition, enumdirection1)) {
                  enumset.add(enumdirection1);
               }
            }

            if (enumdirection.getAxis().isHorizontal() && enumset.isEmpty()) {
               return i;
            } else if (enumset.contains(enumdirection) && !enumset.contains(enumdirection.rotateYCCW()) && !enumset.contains(enumdirection.rotateY())) {
               return i;
            } else {
               return 0;
            }
         }
      }
   }

   private boolean isPowerSourceAt(IBlockAccess iblockaccess, BlockPos blockposition, EnumFacing enumdirection) {
      BlockPos blockposition1 = blockposition.offset(enumdirection);
      IBlockState iblockdata = iblockaccess.getBlockState(blockposition1);
      boolean flag = iblockdata.isNormalCube();
      boolean flag1 = iblockaccess.getBlockState(blockposition.up()).isNormalCube();
      return !flag1 && flag && canConnectUpwardsTo(iblockaccess, blockposition1.up()) ? true : (canConnectTo(iblockdata, enumdirection) ? true : (iblockdata.getBlock() == Blocks.POWERED_REPEATER && iblockdata.getValue(BlockRedstoneDiode.FACING) == enumdirection ? true : !flag && canConnectUpwardsTo(iblockaccess, blockposition1.down())));
   }

   protected static boolean canConnectUpwardsTo(IBlockAccess iblockaccess, BlockPos blockposition) {
      return canConnectUpwardsTo(iblockaccess.getBlockState(blockposition));
   }

   protected static boolean canConnectUpwardsTo(IBlockState iblockdata) {
      return canConnectTo(iblockdata, (EnumFacing)null);
   }

   protected static boolean canConnectTo(IBlockState iblockdata, @Nullable EnumFacing enumdirection) {
      Block block = iblockdata.getBlock();
      if (block == Blocks.REDSTONE_WIRE) {
         return true;
      } else if (Blocks.UNPOWERED_REPEATER.isSameDiode(iblockdata)) {
         EnumFacing enumdirection1 = (EnumFacing)iblockdata.getValue(BlockRedstoneRepeater.FACING);
         return enumdirection1 == enumdirection || enumdirection1.getOpposite() == enumdirection;
      } else {
         return iblockdata.canProvidePower() && enumdirection != null;
      }
   }

   public boolean canProvidePower(IBlockState iblockdata) {
      return this.canProvidePower;
   }

   public ItemStack getItem(World world, BlockPos blockposition, IBlockState iblockdata) {
      return new ItemStack(Items.REDSTONE);
   }

   public IBlockState getStateFromMeta(int i) {
      return this.getDefaultState().withProperty(POWER, Integer.valueOf(i));
   }

   public int getMetaFromState(IBlockState iblockdata) {
      return ((Integer)iblockdata.getValue(POWER)).intValue();
   }

   public IBlockState withRotation(IBlockState iblockdata, Rotation enumblockrotation) {
      switch(BlockRedstoneWire.SyntheticClass_1.a[enumblockrotation.ordinal()]) {
      case 1:
         return iblockdata.withProperty(NORTH, (BlockRedstoneWire.EnumAttachPosition)iblockdata.getValue(SOUTH)).withProperty(EAST, (BlockRedstoneWire.EnumAttachPosition)iblockdata.getValue(WEST)).withProperty(SOUTH, (BlockRedstoneWire.EnumAttachPosition)iblockdata.getValue(NORTH)).withProperty(WEST, (BlockRedstoneWire.EnumAttachPosition)iblockdata.getValue(EAST));
      case 2:
         return iblockdata.withProperty(NORTH, (BlockRedstoneWire.EnumAttachPosition)iblockdata.getValue(EAST)).withProperty(EAST, (BlockRedstoneWire.EnumAttachPosition)iblockdata.getValue(SOUTH)).withProperty(SOUTH, (BlockRedstoneWire.EnumAttachPosition)iblockdata.getValue(WEST)).withProperty(WEST, (BlockRedstoneWire.EnumAttachPosition)iblockdata.getValue(NORTH));
      case 3:
         return iblockdata.withProperty(NORTH, (BlockRedstoneWire.EnumAttachPosition)iblockdata.getValue(WEST)).withProperty(EAST, (BlockRedstoneWire.EnumAttachPosition)iblockdata.getValue(NORTH)).withProperty(SOUTH, (BlockRedstoneWire.EnumAttachPosition)iblockdata.getValue(EAST)).withProperty(WEST, (BlockRedstoneWire.EnumAttachPosition)iblockdata.getValue(SOUTH));
      default:
         return iblockdata;
      }
   }

   public IBlockState withMirror(IBlockState iblockdata, Mirror enumblockmirror) {
      switch(BlockRedstoneWire.SyntheticClass_1.b[enumblockmirror.ordinal()]) {
      case 1:
         return iblockdata.withProperty(NORTH, (BlockRedstoneWire.EnumAttachPosition)iblockdata.getValue(SOUTH)).withProperty(SOUTH, (BlockRedstoneWire.EnumAttachPosition)iblockdata.getValue(NORTH));
      case 2:
         return iblockdata.withProperty(EAST, (BlockRedstoneWire.EnumAttachPosition)iblockdata.getValue(WEST)).withProperty(WEST, (BlockRedstoneWire.EnumAttachPosition)iblockdata.getValue(EAST));
      default:
         return super.withMirror(iblockdata, enumblockmirror);
      }
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{NORTH, EAST, SOUTH, WEST, POWER});
   }

   static enum EnumAttachPosition implements IStringSerializable {
      UP("up"),
      SIDE("side"),
      NONE("none");

      private final String name;

      private EnumAttachPosition(String s) {
         this.name = s;
      }

      public String toString() {
         return this.getName();
      }

      public String getName() {
         return this.name;
      }
   }

   static class SyntheticClass_1 {
      static final int[] a;
      static final int[] b = new int[Mirror.values().length];

      static {
         try {
            b[Mirror.LEFT_RIGHT.ordinal()] = 1;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            b[Mirror.FRONT_BACK.ordinal()] = 2;
         } catch (NoSuchFieldError var3) {
            ;
         }

         a = new int[Rotation.values().length];

         try {
            a[Rotation.CLOCKWISE_180.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            a[Rotation.COUNTERCLOCKWISE_90.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[Rotation.CLOCKWISE_90.ordinal()] = 3;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
