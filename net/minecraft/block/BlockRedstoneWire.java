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

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return REDSTONE_WIRE_AABB[getAABBIndex(var1.getActualState(var2, var3))];
   }

   private static int getAABBIndex(IBlockState var0) {
      int var1 = 0;
      boolean var2 = var0.getValue(NORTH) != BlockRedstoneWire.EnumAttachPosition.NONE;
      boolean var3 = var0.getValue(EAST) != BlockRedstoneWire.EnumAttachPosition.NONE;
      boolean var4 = var0.getValue(SOUTH) != BlockRedstoneWire.EnumAttachPosition.NONE;
      boolean var5 = var0.getValue(WEST) != BlockRedstoneWire.EnumAttachPosition.NONE;
      if (var2 || var4 && !var2 && !var3 && !var5) {
         var1 |= 1 << EnumFacing.NORTH.getHorizontalIndex();
      }

      if (var3 || var5 && !var2 && !var3 && !var4) {
         var1 |= 1 << EnumFacing.EAST.getHorizontalIndex();
      }

      if (var4 || var2 && !var3 && !var4 && !var5) {
         var1 |= 1 << EnumFacing.SOUTH.getHorizontalIndex();
      }

      if (var5 || var3 && !var2 && !var4 && !var5) {
         var1 |= 1 << EnumFacing.WEST.getHorizontalIndex();
      }

      return var1;
   }

   public IBlockState getActualState(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      var1 = var1.withProperty(WEST, this.getAttachPosition(var2, var3, EnumFacing.WEST));
      var1 = var1.withProperty(EAST, this.getAttachPosition(var2, var3, EnumFacing.EAST));
      var1 = var1.withProperty(NORTH, this.getAttachPosition(var2, var3, EnumFacing.NORTH));
      var1 = var1.withProperty(SOUTH, this.getAttachPosition(var2, var3, EnumFacing.SOUTH));
      return var1;
   }

   private BlockRedstoneWire.EnumAttachPosition getAttachPosition(IBlockAccess var1, BlockPos var2, EnumFacing var3) {
      BlockPos var4 = var2.offset(var3);
      IBlockState var5 = var1.getBlockState(var2.offset(var3));
      if (!canConnectTo(var1.getBlockState(var4), var3) && (var5.isNormalCube() || !canConnectUpwardsTo(var1.getBlockState(var4.down())))) {
         IBlockState var6 = var1.getBlockState(var2.up());
         if (!var6.isNormalCube()) {
            boolean var7 = var1.getBlockState(var4).isFullyOpaque() || var1.getBlockState(var4).getBlock() == Blocks.GLOWSTONE;
            if (var7 && canConnectUpwardsTo(var1.getBlockState(var4.up()))) {
               if (var5.isBlockNormalCube()) {
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
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return NULL_AABB;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return var1.getBlockState(var2.down()).isFullyOpaque() || var1.getBlockState(var2.down()).getBlock() == Blocks.GLOWSTONE;
   }

   private IBlockState updateSurroundingRedstone(World var1, BlockPos var2, IBlockState var3) {
      var3 = this.calculateCurrentChanges(var1, var2, var2, var3);
      ArrayList var4 = Lists.newArrayList(this.blocksNeedingUpdate);
      this.blocksNeedingUpdate.clear();

      for(BlockPos var6 : var4) {
         var1.notifyNeighborsOfStateChange(var6, this);
      }

      return var3;
   }

   private IBlockState calculateCurrentChanges(World var1, BlockPos var2, BlockPos var3, IBlockState var4) {
      IBlockState var5 = var4;
      int var6 = ((Integer)var4.getValue(POWER)).intValue();
      byte var7 = 0;
      int var8 = this.getMaxCurrentStrength(var1, var3, var7);
      this.canProvidePower = false;
      int var9 = var1.isBlockIndirectlyGettingPowered(var2);
      this.canProvidePower = true;
      if (var9 > 0 && var9 > var8 - 1) {
         var8 = var9;
      }

      int var10 = 0;

      for(EnumFacing var12 : EnumFacing.Plane.HORIZONTAL) {
         BlockPos var13 = var2.offset(var12);
         boolean var14 = var13.getX() != var3.getX() || var13.getZ() != var3.getZ();
         if (var14) {
            var10 = this.getMaxCurrentStrength(var1, var13, var10);
         }

         if (var1.getBlockState(var13).isNormalCube() && !var1.getBlockState(var2.up()).isNormalCube()) {
            if (var14 && var2.getY() >= var3.getY()) {
               var10 = this.getMaxCurrentStrength(var1, var13.up(), var10);
            }
         } else if (!var1.getBlockState(var13).isNormalCube() && var14 && var2.getY() <= var3.getY()) {
            var10 = this.getMaxCurrentStrength(var1, var13.down(), var10);
         }
      }

      if (var10 > var8) {
         var8 = var10 - 1;
      } else if (var8 > 0) {
         --var8;
      } else {
         var8 = 0;
      }

      if (var9 > var8 - 1) {
         var8 = var9;
      }

      if (var6 != var8) {
         BlockRedstoneEvent var17 = new BlockRedstoneEvent(var1.getWorld().getBlockAt(var2.getX(), var2.getY(), var2.getZ()), var6, var8);
         var1.getServer().getPluginManager().callEvent(var17);
         var8 = var17.getNewCurrent();
      }

      if (var6 != var8) {
         var4 = var4.withProperty(POWER, Integer.valueOf(var8));
         if (var1.getBlockState(var2) == var5) {
            var1.setBlockState(var2, var4, 2);
         }

         this.blocksNeedingUpdate.add(var2);

         for(EnumFacing var15 : EnumFacing.values()) {
            this.blocksNeedingUpdate.add(var2.offset(var15));
         }
      }

      return var4;
   }

   private void notifyWireNeighborsOfStateChange(World var1, BlockPos var2) {
      if (var1.getBlockState(var2).getBlock() == this) {
         var1.notifyNeighborsOfStateChange(var2, this);

         for(EnumFacing var6 : EnumFacing.values()) {
            var1.notifyNeighborsOfStateChange(var2.offset(var6), this);
         }
      }

   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      if (!var1.isRemote) {
         this.updateSurroundingRedstone(var1, var2, var3);

         for(EnumFacing var5 : EnumFacing.Plane.VERTICAL) {
            var1.notifyNeighborsOfStateChange(var2.offset(var5), this);
         }

         for(EnumFacing var9 : EnumFacing.Plane.HORIZONTAL) {
            this.notifyWireNeighborsOfStateChange(var1, var2.offset(var9));
         }

         for(EnumFacing var10 : EnumFacing.Plane.HORIZONTAL) {
            BlockPos var6 = var2.offset(var10);
            if (var1.getBlockState(var6).isNormalCube()) {
               this.notifyWireNeighborsOfStateChange(var1, var6.up());
            } else {
               this.notifyWireNeighborsOfStateChange(var1, var6.down());
            }
         }
      }

   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      super.breakBlock(var1, var2, var3);
      if (!var1.isRemote) {
         for(EnumFacing var7 : EnumFacing.values()) {
            var1.notifyNeighborsOfStateChange(var2.offset(var7), this);
         }

         this.updateSurroundingRedstone(var1, var2, var3);

         for(EnumFacing var11 : EnumFacing.Plane.HORIZONTAL) {
            this.notifyWireNeighborsOfStateChange(var1, var2.offset(var11));
         }

         for(EnumFacing var12 : EnumFacing.Plane.HORIZONTAL) {
            BlockPos var8 = var2.offset(var12);
            if (var1.getBlockState(var8).isNormalCube()) {
               this.notifyWireNeighborsOfStateChange(var1, var8.up());
            } else {
               this.notifyWireNeighborsOfStateChange(var1, var8.down());
            }
         }
      }

   }

   public int getMaxCurrentStrength(World var1, BlockPos var2, int var3) {
      if (var1.getBlockState(var2).getBlock() != this) {
         return var3;
      } else {
         int var4 = ((Integer)var1.getBlockState(var2).getValue(POWER)).intValue();
         return var4 > var3 ? var4 : var3;
      }
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!var2.isRemote) {
         if (this.canPlaceBlockAt(var2, var3)) {
            this.updateSurroundingRedstone(var2, var3, var1);
         } else {
            this.dropBlockAsItem(var2, var3, var1, 0);
            var2.setBlockToAir(var3);
         }
      }

   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Items.REDSTONE;
   }

   public int getStrongPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return !this.canProvidePower ? 0 : var1.getWeakPower(var2, var3, var4);
   }

   public int getWeakPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      if (!this.canProvidePower) {
         return 0;
      } else {
         int var5 = ((Integer)var1.getValue(POWER)).intValue();
         if (var5 == 0) {
            return 0;
         } else if (var4 == EnumFacing.UP) {
            return var5;
         } else {
            EnumSet var6 = EnumSet.noneOf(EnumFacing.class);

            for(EnumFacing var8 : EnumFacing.Plane.HORIZONTAL) {
               if (this.isPowerSourceAt(var2, var3, var8)) {
                  var6.add(var8);
               }
            }

            if (var4.getAxis().isHorizontal() && var6.isEmpty()) {
               return var5;
            } else if (var6.contains(var4) && !var6.contains(var4.rotateYCCW()) && !var6.contains(var4.rotateY())) {
               return var5;
            } else {
               return 0;
            }
         }
      }
   }

   private boolean isPowerSourceAt(IBlockAccess var1, BlockPos var2, EnumFacing var3) {
      BlockPos var4 = var2.offset(var3);
      IBlockState var5 = var1.getBlockState(var4);
      boolean var6 = var5.isNormalCube();
      boolean var7 = var1.getBlockState(var2.up()).isNormalCube();
      return !var7 && var6 && canConnectUpwardsTo(var1, var4.up()) ? true : (canConnectTo(var5, var3) ? true : (var5.getBlock() == Blocks.POWERED_REPEATER && var5.getValue(BlockRedstoneDiode.FACING) == var3 ? true : !var6 && canConnectUpwardsTo(var1, var4.down())));
   }

   protected static boolean canConnectUpwardsTo(IBlockAccess var0, BlockPos var1) {
      return canConnectUpwardsTo(var0.getBlockState(var1));
   }

   protected static boolean canConnectUpwardsTo(IBlockState var0) {
      return canConnectTo(var0, (EnumFacing)null);
   }

   protected static boolean canConnectTo(IBlockState var0, @Nullable EnumFacing var1) {
      Block var2 = var0.getBlock();
      if (var2 == Blocks.REDSTONE_WIRE) {
         return true;
      } else if (Blocks.UNPOWERED_REPEATER.isSameDiode(var0)) {
         EnumFacing var3 = (EnumFacing)var0.getValue(BlockRedstoneRepeater.FACING);
         return var3 == var1 || var3.getOpposite() == var1;
      } else {
         return var0.canProvidePower() && var1 != null;
      }
   }

   public boolean canProvidePower(IBlockState var1) {
      return this.canProvidePower;
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Items.REDSTONE);
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(POWER, Integer.valueOf(var1));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)var1.getValue(POWER)).intValue();
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      switch(BlockRedstoneWire.SyntheticClass_1.a[var2.ordinal()]) {
      case 1:
         return var1.withProperty(NORTH, (BlockRedstoneWire.EnumAttachPosition)var1.getValue(SOUTH)).withProperty(EAST, (BlockRedstoneWire.EnumAttachPosition)var1.getValue(WEST)).withProperty(SOUTH, (BlockRedstoneWire.EnumAttachPosition)var1.getValue(NORTH)).withProperty(WEST, (BlockRedstoneWire.EnumAttachPosition)var1.getValue(EAST));
      case 2:
         return var1.withProperty(NORTH, (BlockRedstoneWire.EnumAttachPosition)var1.getValue(EAST)).withProperty(EAST, (BlockRedstoneWire.EnumAttachPosition)var1.getValue(SOUTH)).withProperty(SOUTH, (BlockRedstoneWire.EnumAttachPosition)var1.getValue(WEST)).withProperty(WEST, (BlockRedstoneWire.EnumAttachPosition)var1.getValue(NORTH));
      case 3:
         return var1.withProperty(NORTH, (BlockRedstoneWire.EnumAttachPosition)var1.getValue(WEST)).withProperty(EAST, (BlockRedstoneWire.EnumAttachPosition)var1.getValue(NORTH)).withProperty(SOUTH, (BlockRedstoneWire.EnumAttachPosition)var1.getValue(EAST)).withProperty(WEST, (BlockRedstoneWire.EnumAttachPosition)var1.getValue(SOUTH));
      default:
         return var1;
      }
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      switch(BlockRedstoneWire.SyntheticClass_1.b[var2.ordinal()]) {
      case 1:
         return var1.withProperty(NORTH, (BlockRedstoneWire.EnumAttachPosition)var1.getValue(SOUTH)).withProperty(SOUTH, (BlockRedstoneWire.EnumAttachPosition)var1.getValue(NORTH));
      case 2:
         return var1.withProperty(EAST, (BlockRedstoneWire.EnumAttachPosition)var1.getValue(WEST)).withProperty(WEST, (BlockRedstoneWire.EnumAttachPosition)var1.getValue(EAST));
      default:
         return super.withMirror(var1, var2);
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

      private EnumAttachPosition(String var3) {
         this.name = var3;
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
