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
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
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

   protected BlockTrapDoor(Material var1) {
      super(var1);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(OPEN, Boolean.valueOf(false)).withProperty(HALF, BlockTrapDoor.DoorHalf.BOTTOM));
      this.setCreativeTab(CreativeTabs.REDSTONE);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      AxisAlignedBB var4;
      if (((Boolean)var1.getValue(OPEN)).booleanValue()) {
         switch(BlockTrapDoor.SyntheticClass_1.a[((EnumFacing)var1.getValue(FACING)).ordinal()]) {
         case 1:
         default:
            var4 = NORTH_OPEN_AABB;
            break;
         case 2:
            var4 = SOUTH_OPEN_AABB;
            break;
         case 3:
            var4 = WEST_OPEN_AABB;
            break;
         case 4:
            var4 = EAST_OPEN_AABB;
         }
      } else if (var1.getValue(HALF) == BlockTrapDoor.DoorHalf.TOP) {
         var4 = TOP_AABB;
      } else {
         var4 = BOTTOM_AABB;
      }

      return var4;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean isPassable(IBlockAccess var1, BlockPos var2) {
      return !((Boolean)var1.getBlockState(var2).getValue(OPEN)).booleanValue();
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (this.blockMaterial == Material.IRON) {
         return true;
      } else {
         var3 = var3.cycleProperty(OPEN);
         var1.setBlockState(var2, var3, 2);
         this.playSound(var4, var1, var2, ((Boolean)var3.getValue(OPEN)).booleanValue());
         return true;
      }
   }

   protected void playSound(@Nullable EntityPlayer var1, World var2, BlockPos var3, boolean var4) {
      if (var4) {
         int var5 = this.blockMaterial == Material.IRON ? 1037 : 1007;
         var2.playEvent(var1, var5, var3, 0);
      } else {
         int var6 = this.blockMaterial == Material.IRON ? 1036 : 1013;
         var2.playEvent(var1, var6, var3, 0);
      }

   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!var2.isRemote) {
         boolean var5 = var2.isBlockPowered(var3);
         if (var5 || var4.getDefaultState().canProvidePower()) {
            CraftWorld var6 = var2.getWorld();
            org.bukkit.block.Block var7 = var6.getBlockAt(var3.getX(), var3.getY(), var3.getZ());
            int var8 = var7.getBlockPower();
            int var9 = ((Boolean)var1.getValue(OPEN)).booleanValue() ? 15 : 0;
            if (var9 == 0 ^ var8 == 0 || var4.getDefaultState().hasComparatorInputOverride()) {
               BlockRedstoneEvent var10 = new BlockRedstoneEvent(var7, var9, var8);
               var2.getServer().getPluginManager().callEvent(var10);
               var5 = var10.getNewCurrent() > 0;
            }

            boolean var11 = ((Boolean)var1.getValue(OPEN)).booleanValue();
            if (var11 != var5) {
               var2.setBlockState(var3, var1.withProperty(OPEN, Boolean.valueOf(var5)), 2);
               this.playSound((EntityPlayer)null, var2, var3, var5);
            }
         }
      }

   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      IBlockState var9 = this.getDefaultState();
      if (var3.getAxis().isHorizontal()) {
         var9 = var9.withProperty(FACING, var3).withProperty(OPEN, Boolean.valueOf(false));
         var9 = var9.withProperty(HALF, var5 > 0.5F ? BlockTrapDoor.DoorHalf.TOP : BlockTrapDoor.DoorHalf.BOTTOM);
      } else {
         var9 = var9.withProperty(FACING, var8.getHorizontalFacing().getOpposite()).withProperty(OPEN, Boolean.valueOf(false));
         var9 = var9.withProperty(HALF, var3 == EnumFacing.UP ? BlockTrapDoor.DoorHalf.BOTTOM : BlockTrapDoor.DoorHalf.TOP);
      }

      return var9;
   }

   public boolean canPlaceBlockOnSide(World var1, BlockPos var2, EnumFacing var3) {
      return true;
   }

   protected static EnumFacing getFacing(int var0) {
      switch(var0 & 3) {
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

   protected static int getMetaForFacing(EnumFacing var0) {
      switch(BlockTrapDoor.SyntheticClass_1.a[var0.ordinal()]) {
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

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(FACING, getFacing(var1)).withProperty(OPEN, Boolean.valueOf((var1 & 4) != 0)).withProperty(HALF, (var1 & 8) == 0 ? BlockTrapDoor.DoorHalf.BOTTOM : BlockTrapDoor.DoorHalf.TOP);
   }

   public int getMetaFromState(IBlockState var1) {
      byte var2 = 0;
      int var3 = var2 | getMetaForFacing((EnumFacing)var1.getValue(FACING));
      if (((Boolean)var1.getValue(OPEN)).booleanValue()) {
         var3 |= 4;
      }

      if (var1.getValue(HALF) == BlockTrapDoor.DoorHalf.TOP) {
         var3 |= 8;
      }

      return var3;
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return var1.withProperty(FACING, var2.rotate((EnumFacing)var1.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return var1.withRotation(var2.toRotation((EnumFacing)var1.getValue(FACING)));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, OPEN, HALF});
   }

   public static enum DoorHalf implements IStringSerializable {
      TOP("top"),
      BOTTOM("bottom");

      private final String name;

      private DoorHalf(String var3) {
         this.name = var3;
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
