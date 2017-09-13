package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.event.block.BlockRedstoneEvent;

public class BlockTrapDoor extends Block {
   public static final PropertyDirection FACING = BlockHorizontal.FACING;
   public static final PropertyBool OPEN = PropertyBool.create("open");
   public static final PropertyEnum HALF = PropertyEnum.create("half", BlockTrapDoor.DoorHalf.class);
   protected static final AxisAlignedBB EAST_OPEN_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.1875D, 1.0D, 1.0D);
   protected static final AxisAlignedBB WEST_OPEN_AABB = new AxisAlignedBB(0.8125D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB SOUTH_OPEN_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.1875D);
   protected static final AxisAlignedBB NORTH_OPEN_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.8125D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB BOTTOM_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.1875D, 1.0D);
   protected static final AxisAlignedBB TOP_AABB = new AxisAlignedBB(0.0D, 0.8125D, 0.0D, 1.0D, 1.0D, 1.0D);

   protected BlockTrapDoor(Material material) {
      super(material);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(OPEN, Boolean.valueOf(false)).withProperty(HALF, BlockTrapDoor.DoorHalf.BOTTOM));
      this.setCreativeTab(CreativeTabs.REDSTONE);
   }

   public AxisAlignedBB getBoundingBox(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      AxisAlignedBB axisalignedbb;
      if (((Boolean)iblockdata.getValue(OPEN)).booleanValue()) {
         switch(BlockTrapDoor.SyntheticClass_1.a[((EnumFacing)iblockdata.getValue(FACING)).ordinal()]) {
         case 1:
         default:
            axisalignedbb = NORTH_OPEN_AABB;
            break;
         case 2:
            axisalignedbb = SOUTH_OPEN_AABB;
            break;
         case 3:
            axisalignedbb = WEST_OPEN_AABB;
            break;
         case 4:
            axisalignedbb = EAST_OPEN_AABB;
         }
      } else if (iblockdata.getValue(HALF) == BlockTrapDoor.DoorHalf.TOP) {
         axisalignedbb = TOP_AABB;
      } else {
         axisalignedbb = BOTTOM_AABB;
      }

      return axisalignedbb;
   }

   public boolean isOpaqueCube(IBlockState iblockdata) {
      return false;
   }

   public boolean isFullCube(IBlockState iblockdata) {
      return false;
   }

   public boolean isPassable(IBlockAccess iblockaccess, BlockPos blockposition) {
      return !((Boolean)iblockaccess.getBlockState(blockposition).getValue(OPEN)).booleanValue();
   }

   public boolean onBlockActivated(World world, BlockPos blockposition, IBlockState iblockdata, EntityPlayer entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack, EnumFacing enumdirection, float f, float f1, float f2) {
      if (this.blockMaterial == Material.IRON) {
         return true;
      } else {
         iblockdata = iblockdata.cycleProperty(OPEN);
         world.setBlockState(blockposition, iblockdata, 2);
         this.playSound(entityhuman, world, blockposition, ((Boolean)iblockdata.getValue(OPEN)).booleanValue());
         return true;
      }
   }

   protected void playSound(@Nullable EntityPlayer entityhuman, World world, BlockPos blockposition, boolean flag) {
      if (flag) {
         int i = this.blockMaterial == Material.IRON ? 1037 : 1007;
         world.playEvent(entityhuman, i, blockposition, 0);
      } else {
         int i = this.blockMaterial == Material.IRON ? 1036 : 1013;
         world.playEvent(entityhuman, i, blockposition, 0);
      }

   }

   public void neighborChanged(IBlockState iblockdata, World world, BlockPos blockposition, Block block) {
      if (!world.isRemote) {
         boolean flag = world.isBlockPowered(blockposition);
         if (flag || block.getDefaultState().canProvidePower()) {
            org.bukkit.World bworld = world.getWorld();
            org.bukkit.block.Block bblock = bworld.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
            int power = bblock.getBlockPower();
            int oldPower = ((Boolean)iblockdata.getValue(OPEN)).booleanValue() ? 15 : 0;
            if (oldPower == 0 ^ power == 0 || block.getDefaultState().hasComparatorInputOverride()) {
               BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(bblock, oldPower, power);
               world.getServer().getPluginManager().callEvent(eventRedstone);
               flag = eventRedstone.getNewCurrent() > 0;
            }

            boolean flag1 = ((Boolean)iblockdata.getValue(OPEN)).booleanValue();
            if (flag1 != flag) {
               world.setBlockState(blockposition, iblockdata.withProperty(OPEN, Boolean.valueOf(flag)), 2);
               this.playSound((EntityPlayer)null, world, blockposition, flag);
            }
         }
      }

   }

   public IBlockState getStateForPlacement(World world, BlockPos blockposition, EnumFacing enumdirection, float f, float f1, float f2, int i, EntityLivingBase entityliving) {
      IBlockState iblockdata = this.getDefaultState();
      if (enumdirection.getAxis().isHorizontal()) {
         iblockdata = iblockdata.withProperty(FACING, enumdirection).withProperty(OPEN, Boolean.valueOf(false));
         iblockdata = iblockdata.withProperty(HALF, f1 > 0.5F ? BlockTrapDoor.DoorHalf.TOP : BlockTrapDoor.DoorHalf.BOTTOM);
      } else {
         iblockdata = iblockdata.withProperty(FACING, entityliving.getHorizontalFacing().getOpposite()).withProperty(OPEN, Boolean.valueOf(false));
         iblockdata = iblockdata.withProperty(HALF, enumdirection == EnumFacing.UP ? BlockTrapDoor.DoorHalf.BOTTOM : BlockTrapDoor.DoorHalf.TOP);
      }

      return iblockdata;
   }

   public boolean canPlaceBlockOnSide(World world, BlockPos blockposition, EnumFacing enumdirection) {
      return true;
   }

   protected static EnumFacing getFacing(int i) {
      switch(i & 3) {
      case 0:
         return EnumFacing.NORTH;
      case 1:
         return EnumFacing.SOUTH;
      case 2:
         return EnumFacing.WEST;
      case 3:
      default:
         return EnumFacing.EAST;
      }
   }

   protected static int getMetaForFacing(EnumFacing enumdirection) {
      switch(BlockTrapDoor.SyntheticClass_1.a[enumdirection.ordinal()]) {
      case 1:
         return 0;
      case 2:
         return 1;
      case 3:
         return 2;
      case 4:
      default:
         return 3;
      }
   }

   public IBlockState getStateFromMeta(int i) {
      return this.getDefaultState().withProperty(FACING, getFacing(i)).withProperty(OPEN, Boolean.valueOf((i & 4) != 0)).withProperty(HALF, (i & 8) == 0 ? BlockTrapDoor.DoorHalf.BOTTOM : BlockTrapDoor.DoorHalf.TOP);
   }

   public int getMetaFromState(IBlockState iblockdata) {
      byte b0 = 0;
      int i = b0 | getMetaForFacing((EnumFacing)iblockdata.getValue(FACING));
      if (((Boolean)iblockdata.getValue(OPEN)).booleanValue()) {
         i |= 4;
      }

      if (iblockdata.getValue(HALF) == BlockTrapDoor.DoorHalf.TOP) {
         i |= 8;
      }

      return i;
   }

   public IBlockState withRotation(IBlockState iblockdata, Rotation enumblockrotation) {
      return iblockdata.withProperty(FACING, enumblockrotation.rotate((EnumFacing)iblockdata.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState iblockdata, Mirror enumblockmirror) {
      return iblockdata.withRotation(enumblockmirror.toRotation((EnumFacing)iblockdata.getValue(FACING)));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, OPEN, HALF});
   }

   public static enum DoorHalf implements IStringSerializable {
      TOP("top"),
      BOTTOM("bottom");

      private final String name;

      private DoorHalf(String s) {
         this.name = s;
      }

      public String toString() {
         return this.name;
      }

      public String getName() {
         return this.name;
      }
   }

   static class SyntheticClass_1 {
      static final int[] a = new int[EnumFacing.values().length];

      static {
         try {
            a[EnumFacing.NORTH.ordinal()] = 1;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            a[EnumFacing.SOUTH.ordinal()] = 2;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            a[EnumFacing.WEST.ordinal()] = 3;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[EnumFacing.EAST.ordinal()] = 4;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
