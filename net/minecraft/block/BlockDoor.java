package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.event.block.BlockRedstoneEvent;

public class BlockDoor extends Block {
   public static final PropertyDirection FACING = BlockHorizontal.FACING;
   public static final PropertyBool OPEN = PropertyBool.create("open");
   public static final PropertyEnum HINGE = PropertyEnum.create("hinge", BlockDoor.EnumHingePosition.class);
   public static final PropertyBool POWERED = PropertyBool.create("powered");
   public static final PropertyEnum HALF = PropertyEnum.create("half", BlockDoor.EnumDoorHalf.class);
   protected static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.1875D);
   protected static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.8125D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.8125D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.1875D, 1.0D, 1.0D);

   protected BlockDoor(Material material) {
      super(material);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(OPEN, Boolean.valueOf(false)).withProperty(HINGE, BlockDoor.EnumHingePosition.LEFT).withProperty(POWERED, Boolean.valueOf(false)).withProperty(HALF, BlockDoor.EnumDoorHalf.LOWER));
   }

   public AxisAlignedBB getBoundingBox(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      iblockdata = iblockdata.getActualState(iblockaccess, blockposition);
      EnumFacing enumdirection = (EnumFacing)iblockdata.getValue(FACING);
      boolean flag = !((Boolean)iblockdata.getValue(OPEN)).booleanValue();
      boolean flag1 = iblockdata.getValue(HINGE) == BlockDoor.EnumHingePosition.RIGHT;
      switch(BlockDoor.SyntheticClass_1.a[enumdirection.ordinal()]) {
      case 1:
      default:
         return flag ? EAST_AABB : (flag1 ? NORTH_AABB : SOUTH_AABB);
      case 2:
         return flag ? SOUTH_AABB : (flag1 ? EAST_AABB : WEST_AABB);
      case 3:
         return flag ? WEST_AABB : (flag1 ? SOUTH_AABB : NORTH_AABB);
      case 4:
         return flag ? NORTH_AABB : (flag1 ? WEST_AABB : EAST_AABB);
      }
   }

   public String getLocalizedName() {
      return I18n.translateToLocal((this.getUnlocalizedName() + ".name").replaceAll("tile", "item"));
   }

   public boolean isOpaqueCube(IBlockState iblockdata) {
      return false;
   }

   public boolean isPassable(IBlockAccess iblockaccess, BlockPos blockposition) {
      return isOpen(combineMetadata(iblockaccess, blockposition));
   }

   public boolean isFullCube(IBlockState iblockdata) {
      return false;
   }

   private int getCloseSound() {
      return this.blockMaterial == Material.IRON ? 1011 : 1012;
   }

   private int getOpenSound() {
      return this.blockMaterial == Material.IRON ? 1005 : 1006;
   }

   public MapColor getMapColor(IBlockState iblockdata) {
      return iblockdata.getBlock() == Blocks.IRON_DOOR ? MapColor.IRON : (iblockdata.getBlock() == Blocks.OAK_DOOR ? BlockPlanks.EnumType.OAK.getMapColor() : (iblockdata.getBlock() == Blocks.SPRUCE_DOOR ? BlockPlanks.EnumType.SPRUCE.getMapColor() : (iblockdata.getBlock() == Blocks.BIRCH_DOOR ? BlockPlanks.EnumType.BIRCH.getMapColor() : (iblockdata.getBlock() == Blocks.JUNGLE_DOOR ? BlockPlanks.EnumType.JUNGLE.getMapColor() : (iblockdata.getBlock() == Blocks.ACACIA_DOOR ? BlockPlanks.EnumType.ACACIA.getMapColor() : (iblockdata.getBlock() == Blocks.DARK_OAK_DOOR ? BlockPlanks.EnumType.DARK_OAK.getMapColor() : super.getMapColor(iblockdata)))))));
   }

   public boolean onBlockActivated(World world, BlockPos blockposition, IBlockState iblockdata, EntityPlayer entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack, EnumFacing enumdirection, float f, float f1, float f2) {
      if (this.blockMaterial == Material.IRON) {
         return true;
      } else {
         BlockPos blockposition1 = iblockdata.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER ? blockposition : blockposition.down();
         IBlockState iblockdata1 = blockposition.equals(blockposition1) ? iblockdata : world.getBlockState(blockposition1);
         if (iblockdata1.getBlock() != this) {
            return false;
         } else {
            iblockdata = iblockdata1.cycleProperty(OPEN);
            world.setBlockState(blockposition1, iblockdata, 10);
            world.markBlockRangeForRenderUpdate(blockposition1, blockposition);
            world.playEvent(entityhuman, ((Boolean)iblockdata.getValue(OPEN)).booleanValue() ? this.getOpenSound() : this.getCloseSound(), blockposition, 0);
            return true;
         }
      }
   }

   public void toggleDoor(World world, BlockPos blockposition, boolean flag) {
      IBlockState iblockdata = world.getBlockState(blockposition);
      if (iblockdata.getBlock() == this) {
         BlockPos blockposition1 = iblockdata.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER ? blockposition : blockposition.down();
         IBlockState iblockdata1 = blockposition == blockposition1 ? iblockdata : world.getBlockState(blockposition1);
         if (iblockdata1.getBlock() == this && ((Boolean)iblockdata1.getValue(OPEN)).booleanValue() != flag) {
            world.setBlockState(blockposition1, iblockdata1.withProperty(OPEN, Boolean.valueOf(flag)), 10);
            world.markBlockRangeForRenderUpdate(blockposition1, blockposition);
            world.playEvent((EntityPlayer)null, flag ? this.getOpenSound() : this.getCloseSound(), blockposition, 0);
         }
      }

   }

   public void neighborChanged(IBlockState iblockdata, World world, BlockPos blockposition, Block block) {
      if (iblockdata.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER) {
         BlockPos blockposition1 = blockposition.down();
         IBlockState iblockdata1 = world.getBlockState(blockposition1);
         if (iblockdata1.getBlock() != this) {
            world.setBlockToAir(blockposition);
         } else if (block != this) {
            iblockdata1.neighborChanged(world, blockposition1, block);
         }
      } else {
         boolean flag = false;
         BlockPos blockposition2 = blockposition.up();
         IBlockState iblockdata2 = world.getBlockState(blockposition2);
         if (iblockdata2.getBlock() != this) {
            world.setBlockToAir(blockposition);
            flag = true;
         }

         if (!world.getBlockState(blockposition.down()).isFullyOpaque()) {
            world.setBlockToAir(blockposition);
            flag = true;
            if (iblockdata2.getBlock() == this) {
               world.setBlockToAir(blockposition2);
            }
         }

         if (flag) {
            if (!world.isRemote) {
               this.dropBlockAsItem(world, blockposition, iblockdata, 0);
            }
         } else {
            org.bukkit.World bworld = world.getWorld();
            org.bukkit.block.Block bukkitBlock = bworld.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
            org.bukkit.block.Block blockTop = bworld.getBlockAt(blockposition2.getX(), blockposition2.getY(), blockposition2.getZ());
            int power = bukkitBlock.getBlockPower();
            int powerTop = blockTop.getBlockPower();
            if (powerTop > power) {
               power = powerTop;
            }

            int oldPower = ((Boolean)iblockdata2.getValue(POWERED)).booleanValue() ? 15 : 0;
            if (oldPower == 0 ^ power == 0) {
               BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(bukkitBlock, oldPower, power);
               world.getServer().getPluginManager().callEvent(eventRedstone);
               boolean flag1 = eventRedstone.getNewCurrent() > 0;
               world.setBlockState(blockposition2, iblockdata2.withProperty(POWERED, Boolean.valueOf(flag1)), 2);
               if (flag1 != ((Boolean)iblockdata.getValue(OPEN)).booleanValue()) {
                  world.setBlockState(blockposition, iblockdata.withProperty(OPEN, Boolean.valueOf(flag1)), 2);
                  world.markBlockRangeForRenderUpdate(blockposition, blockposition);
                  world.playEvent((EntityPlayer)null, flag1 ? this.getOpenSound() : this.getCloseSound(), blockposition, 0);
               }
            }
         }
      }

   }

   @Nullable
   public Item getItemDropped(IBlockState iblockdata, Random random, int i) {
      return iblockdata.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER ? null : this.getItem();
   }

   public boolean canPlaceBlockAt(World world, BlockPos blockposition) {
      return blockposition.getY() >= 255 ? false : world.getBlockState(blockposition.down()).isFullyOpaque() && super.canPlaceBlockAt(world, blockposition) && super.canPlaceBlockAt(world, blockposition.up());
   }

   public EnumPushReaction getMobilityFlag(IBlockState iblockdata) {
      return EnumPushReaction.DESTROY;
   }

   public static int combineMetadata(IBlockAccess iblockaccess, BlockPos blockposition) {
      IBlockState iblockdata = iblockaccess.getBlockState(blockposition);
      int i = iblockdata.getBlock().getMetaFromState(iblockdata);
      boolean flag = isTop(i);
      IBlockState iblockdata1 = iblockaccess.getBlockState(blockposition.down());
      int j = iblockdata1.getBlock().getMetaFromState(iblockdata1);
      int k = flag ? j : i;
      IBlockState iblockdata2 = iblockaccess.getBlockState(blockposition.up());
      int l = iblockdata2.getBlock().getMetaFromState(iblockdata2);
      int i1 = flag ? i : l;
      boolean flag1 = (i1 & 1) != 0;
      boolean flag2 = (i1 & 2) != 0;
      return removeHalfBit(k) | (flag ? 8 : 0) | (flag1 ? 16 : 0) | (flag2 ? 32 : 0);
   }

   public ItemStack getItem(World world, BlockPos blockposition, IBlockState iblockdata) {
      return new ItemStack(this.getItem());
   }

   private Item getItem() {
      return this == Blocks.IRON_DOOR ? Items.IRON_DOOR : (this == Blocks.SPRUCE_DOOR ? Items.SPRUCE_DOOR : (this == Blocks.BIRCH_DOOR ? Items.BIRCH_DOOR : (this == Blocks.JUNGLE_DOOR ? Items.JUNGLE_DOOR : (this == Blocks.ACACIA_DOOR ? Items.ACACIA_DOOR : (this == Blocks.DARK_OAK_DOOR ? Items.DARK_OAK_DOOR : Items.OAK_DOOR)))));
   }

   public void onBlockHarvested(World world, BlockPos blockposition, IBlockState iblockdata, EntityPlayer entityhuman) {
      BlockPos blockposition1 = blockposition.down();
      BlockPos blockposition2 = blockposition.up();
      if (entityhuman.capabilities.isCreativeMode && iblockdata.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER && world.getBlockState(blockposition1).getBlock() == this) {
         world.setBlockToAir(blockposition1);
      }

      if (iblockdata.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER && world.getBlockState(blockposition2).getBlock() == this) {
         if (entityhuman.capabilities.isCreativeMode) {
            world.setBlockToAir(blockposition);
         }

         world.setBlockToAir(blockposition2);
      }

   }

   public IBlockState getActualState(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      if (iblockdata.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER) {
         IBlockState iblockdata1 = iblockaccess.getBlockState(blockposition.up());
         if (iblockdata1.getBlock() == this) {
            iblockdata = iblockdata.withProperty(HINGE, (BlockDoor.EnumHingePosition)iblockdata1.getValue(HINGE)).withProperty(POWERED, (Boolean)iblockdata1.getValue(POWERED));
         }
      } else {
         IBlockState iblockdata1 = iblockaccess.getBlockState(blockposition.down());
         if (iblockdata1.getBlock() == this) {
            iblockdata = iblockdata.withProperty(FACING, (EnumFacing)iblockdata1.getValue(FACING)).withProperty(OPEN, (Boolean)iblockdata1.getValue(OPEN));
         }
      }

      return iblockdata;
   }

   public IBlockState withRotation(IBlockState iblockdata, Rotation enumblockrotation) {
      return iblockdata.getValue(HALF) != BlockDoor.EnumDoorHalf.LOWER ? iblockdata : iblockdata.withProperty(FACING, enumblockrotation.rotate((EnumFacing)iblockdata.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState iblockdata, Mirror enumblockmirror) {
      return enumblockmirror == Mirror.NONE ? iblockdata : iblockdata.withRotation(enumblockmirror.toRotation((EnumFacing)iblockdata.getValue(FACING))).cycleProperty(HINGE);
   }

   public IBlockState getStateFromMeta(int i) {
      return (i & 8) > 0 ? this.getDefaultState().withProperty(HALF, BlockDoor.EnumDoorHalf.UPPER).withProperty(HINGE, (i & 1) > 0 ? BlockDoor.EnumHingePosition.RIGHT : BlockDoor.EnumHingePosition.LEFT).withProperty(POWERED, Boolean.valueOf((i & 2) > 0)) : this.getDefaultState().withProperty(HALF, BlockDoor.EnumDoorHalf.LOWER).withProperty(FACING, EnumFacing.getHorizontal(i & 3).rotateYCCW()).withProperty(OPEN, Boolean.valueOf((i & 4) > 0));
   }

   public int getMetaFromState(IBlockState iblockdata) {
      byte b0 = 0;
      int i;
      if (iblockdata.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER) {
         i = b0 | 8;
         if (iblockdata.getValue(HINGE) == BlockDoor.EnumHingePosition.RIGHT) {
            i |= 1;
         }

         if (((Boolean)iblockdata.getValue(POWERED)).booleanValue()) {
            i |= 2;
         }
      } else {
         i = b0 | ((EnumFacing)iblockdata.getValue(FACING)).rotateY().getHorizontalIndex();
         if (((Boolean)iblockdata.getValue(OPEN)).booleanValue()) {
            i |= 4;
         }
      }

      return i;
   }

   protected static int removeHalfBit(int i) {
      return i & 7;
   }

   public static boolean isOpen(IBlockAccess iblockaccess, BlockPos blockposition) {
      return isOpen(combineMetadata(iblockaccess, blockposition));
   }

   public static EnumFacing getFacing(IBlockAccess iblockaccess, BlockPos blockposition) {
      return getFacing(combineMetadata(iblockaccess, blockposition));
   }

   public static EnumFacing getFacing(int i) {
      return EnumFacing.getHorizontal(i & 3).rotateYCCW();
   }

   protected static boolean isOpen(int i) {
      return (i & 4) != 0;
   }

   protected static boolean isTop(int i) {
      return (i & 8) != 0;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{HALF, FACING, OPEN, HINGE, POWERED});
   }

   public static enum EnumDoorHalf implements IStringSerializable {
      UPPER,
      LOWER;

      public String toString() {
         return this.getName();
      }

      public String getName() {
         return this == UPPER ? "upper" : "lower";
      }
   }

   public static enum EnumHingePosition implements IStringSerializable {
      LEFT,
      RIGHT;

      public String toString() {
         return this.getName();
      }

      public String getName() {
         return this == LEFT ? "left" : "right";
      }
   }

   static class SyntheticClass_1 {
      static final int[] a = new int[EnumFacing.values().length];

      static {
         try {
            a[EnumFacing.EAST.ordinal()] = 1;
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
            a[EnumFacing.NORTH.ordinal()] = 4;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
