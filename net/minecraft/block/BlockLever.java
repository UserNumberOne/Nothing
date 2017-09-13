package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.event.block.BlockRedstoneEvent;

public class BlockLever extends Block {
   public static final PropertyEnum FACING = PropertyEnum.create("facing", BlockLever.EnumOrientation.class);
   public static final PropertyBool POWERED = PropertyBool.create("powered");
   protected static final AxisAlignedBB LEVER_NORTH_AABB = new AxisAlignedBB(0.3125D, 0.20000000298023224D, 0.625D, 0.6875D, 0.800000011920929D, 1.0D);
   protected static final AxisAlignedBB LEVER_SOUTH_AABB = new AxisAlignedBB(0.3125D, 0.20000000298023224D, 0.0D, 0.6875D, 0.800000011920929D, 0.375D);
   protected static final AxisAlignedBB LEVER_WEST_AABB = new AxisAlignedBB(0.625D, 0.20000000298023224D, 0.3125D, 1.0D, 0.800000011920929D, 0.6875D);
   protected static final AxisAlignedBB LEVER_EAST_AABB = new AxisAlignedBB(0.0D, 0.20000000298023224D, 0.3125D, 0.375D, 0.800000011920929D, 0.6875D);
   protected static final AxisAlignedBB LEVER_UP_AABB = new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 0.6000000238418579D, 0.75D);
   protected static final AxisAlignedBB LEVER_DOWN_AABB = new AxisAlignedBB(0.25D, 0.4000000059604645D, 0.25D, 0.75D, 1.0D, 0.75D);

   protected BlockLever() {
      super(Material.CIRCUITS);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, BlockLever.EnumOrientation.NORTH).withProperty(POWERED, Boolean.valueOf(false)));
      this.setCreativeTab(CreativeTabs.REDSTONE);
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

   public boolean canPlaceBlockOnSide(World var1, BlockPos var2, EnumFacing var3) {
      return canAttachTo(var1, var2, var3.getOpposite());
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      for(EnumFacing var6 : EnumFacing.values()) {
         if (canAttachTo(var1, var2, var6)) {
            return true;
         }
      }

      return false;
   }

   protected static boolean canAttachTo(World var0, BlockPos var1, EnumFacing var2) {
      return BlockButton.canPlaceBlock(var0, var1, var2);
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      IBlockState var9 = this.getDefaultState().withProperty(POWERED, Boolean.valueOf(false));
      if (canAttachTo(var1, var2, var3.getOpposite())) {
         return var9.withProperty(FACING, BlockLever.EnumOrientation.forFacings(var3, var8.getHorizontalFacing()));
      } else {
         for(EnumFacing var11 : EnumFacing.Plane.HORIZONTAL) {
            if (var11 != var3 && canAttachTo(var1, var2, var11.getOpposite())) {
               return var9.withProperty(FACING, BlockLever.EnumOrientation.forFacings(var11, var8.getHorizontalFacing()));
            }
         }

         if (var1.getBlockState(var2.down()).isFullyOpaque()) {
            return var9.withProperty(FACING, BlockLever.EnumOrientation.forFacings(EnumFacing.UP, var8.getHorizontalFacing()));
         } else {
            return var9;
         }
      }
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (this.checkCanSurvive(var2, var3, var1) && !canAttachTo(var2, var3, ((BlockLever.EnumOrientation)var1.getValue(FACING)).getFacing().getOpposite())) {
         this.dropBlockAsItem(var2, var3, var1, 0);
         var2.setBlockToAir(var3);
      }

   }

   private boolean checkCanSurvive(World var1, BlockPos var2, IBlockState var3) {
      if (this.canPlaceBlockAt(var1, var2)) {
         return true;
      } else {
         this.dropBlockAsItem(var1, var2, var3, 0);
         var1.setBlockToAir(var2);
         return false;
      }
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      switch(BlockLever.SyntheticClass_1.b[((BlockLever.EnumOrientation)var1.getValue(FACING)).ordinal()]) {
      case 1:
      default:
         return LEVER_EAST_AABB;
      case 2:
         return LEVER_WEST_AABB;
      case 3:
         return LEVER_SOUTH_AABB;
      case 4:
         return LEVER_NORTH_AABB;
      case 5:
      case 6:
         return LEVER_UP_AABB;
      case 7:
      case 8:
         return LEVER_DOWN_AABB;
      }
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (var1.isRemote) {
         return true;
      } else {
         boolean var11 = ((Boolean)var3.getValue(POWERED)).booleanValue();
         org.bukkit.block.Block var12 = var1.getWorld().getBlockAt(var2.getX(), var2.getY(), var2.getZ());
         int var13 = var11 ? 15 : 0;
         int var14 = !var11 ? 15 : 0;
         BlockRedstoneEvent var15 = new BlockRedstoneEvent(var12, var13, var14);
         var1.getServer().getPluginManager().callEvent(var15);
         if (var15.getNewCurrent() > 0 != !var11) {
            return true;
         } else {
            var3 = var3.cycleProperty(POWERED);
            var1.setBlockState(var2, var3, 3);
            float var16 = ((Boolean)var3.getValue(POWERED)).booleanValue() ? 0.6F : 0.5F;
            var1.playSound((EntityPlayer)null, var2, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, var16);
            var1.notifyNeighborsOfStateChange(var2, this);
            EnumFacing var17 = ((BlockLever.EnumOrientation)var3.getValue(FACING)).getFacing();
            var1.notifyNeighborsOfStateChange(var2.offset(var17.getOpposite()), this);
            return true;
         }
      }
   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      if (((Boolean)var3.getValue(POWERED)).booleanValue()) {
         var1.notifyNeighborsOfStateChange(var2, this);
         EnumFacing var4 = ((BlockLever.EnumOrientation)var3.getValue(FACING)).getFacing();
         var1.notifyNeighborsOfStateChange(var2.offset(var4.getOpposite()), this);
      }

      super.breakBlock(var1, var2, var3);
   }

   public int getWeakPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return ((Boolean)var1.getValue(POWERED)).booleanValue() ? 15 : 0;
   }

   public int getStrongPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return !((Boolean)var1.getValue(POWERED)).booleanValue() ? 0 : (((BlockLever.EnumOrientation)var1.getValue(FACING)).getFacing() == var4 ? 15 : 0);
   }

   public boolean canProvidePower(IBlockState var1) {
      return true;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(FACING, BlockLever.EnumOrientation.byMetadata(var1 & 7)).withProperty(POWERED, Boolean.valueOf((var1 & 8) > 0));
   }

   public int getMetaFromState(IBlockState var1) {
      byte var2 = 0;
      int var3 = var2 | ((BlockLever.EnumOrientation)var1.getValue(FACING)).getMetadata();
      if (((Boolean)var1.getValue(POWERED)).booleanValue()) {
         var3 |= 8;
      }

      return var3;
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      switch(BlockLever.SyntheticClass_1.c[var2.ordinal()]) {
      case 1:
         switch(BlockLever.SyntheticClass_1.b[((BlockLever.EnumOrientation)var1.getValue(FACING)).ordinal()]) {
         case 1:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.WEST);
         case 2:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.EAST);
         case 3:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.NORTH);
         case 4:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.SOUTH);
         default:
            return var1;
         }
      case 2:
         switch(BlockLever.SyntheticClass_1.b[((BlockLever.EnumOrientation)var1.getValue(FACING)).ordinal()]) {
         case 1:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.NORTH);
         case 2:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.SOUTH);
         case 3:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.EAST);
         case 4:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.WEST);
         case 5:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.UP_X);
         case 6:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.UP_Z);
         case 7:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.DOWN_Z);
         case 8:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.DOWN_X);
         }
      case 3:
         switch(BlockLever.SyntheticClass_1.b[((BlockLever.EnumOrientation)var1.getValue(FACING)).ordinal()]) {
         case 1:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.SOUTH);
         case 2:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.NORTH);
         case 3:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.WEST);
         case 4:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.EAST);
         case 5:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.UP_X);
         case 6:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.UP_Z);
         case 7:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.DOWN_Z);
         case 8:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.DOWN_X);
         }
      default:
         return var1;
      }
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return var1.withRotation(var2.toRotation(((BlockLever.EnumOrientation)var1.getValue(FACING)).getFacing()));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, POWERED});
   }

   public static enum EnumOrientation implements IStringSerializable {
      DOWN_X(0, "down_x", EnumFacing.DOWN),
      EAST(1, "east", EnumFacing.EAST),
      WEST(2, "west", EnumFacing.WEST),
      SOUTH(3, "south", EnumFacing.SOUTH),
      NORTH(4, "north", EnumFacing.NORTH),
      UP_Z(5, "up_z", EnumFacing.UP),
      UP_X(6, "up_x", EnumFacing.UP),
      DOWN_Z(7, "down_z", EnumFacing.DOWN);

      private static final BlockLever.EnumOrientation[] META_LOOKUP = new BlockLever.EnumOrientation[values().length];
      private final int meta;
      private final String name;
      private final EnumFacing facing;

      static {
         for(BlockLever.EnumOrientation var3 : values()) {
            META_LOOKUP[var3.getMetadata()] = var3;
         }

      }

      private EnumOrientation(int var3, String var4, EnumFacing var5) {
         this.meta = var3;
         this.name = var4;
         this.facing = var5;
      }

      public int getMetadata() {
         return this.meta;
      }

      public EnumFacing getFacing() {
         return this.facing;
      }

      public String toString() {
         return this.name;
      }

      public static BlockLever.EnumOrientation byMetadata(int var0) {
         if (var0 < 0 || var0 >= META_LOOKUP.length) {
            var0 = 0;
         }

         return META_LOOKUP[var0];
      }

      public static BlockLever.EnumOrientation forFacings(EnumFacing var0, EnumFacing var1) {
         switch(BlockLever.SyntheticClass_1.a[var0.ordinal()]) {
         case 1:
            switch(BlockLever.SyntheticClass_1.d[var1.getAxis().ordinal()]) {
            case 1:
               return DOWN_X;
            case 2:
               return DOWN_Z;
            default:
               throw new IllegalArgumentException("Invalid entityFacing " + var1 + " for facing " + var0);
            }
         case 2:
            switch(BlockLever.SyntheticClass_1.d[var1.getAxis().ordinal()]) {
            case 1:
               return UP_X;
            case 2:
               return UP_Z;
            default:
               throw new IllegalArgumentException("Invalid entityFacing " + var1 + " for facing " + var0);
            }
         case 3:
            return NORTH;
         case 4:
            return SOUTH;
         case 5:
            return WEST;
         case 6:
            return EAST;
         default:
            throw new IllegalArgumentException("Invalid facing: " + var0);
         }
      }

      public String getName() {
         return this.name;
      }
   }

   static class SyntheticClass_1 {
      static final int[] a;
      static final int[] b;
      static final int[] c;
      static final int[] d = new int[EnumFacing.Axis.values().length];

      static {
         try {
            d[EnumFacing.Axis.X.ordinal()] = 1;
         } catch (NoSuchFieldError var18) {
            ;
         }

         try {
            d[EnumFacing.Axis.Z.ordinal()] = 2;
         } catch (NoSuchFieldError var17) {
            ;
         }

         c = new int[Rotation.values().length];

         try {
            c[Rotation.CLOCKWISE_180.ordinal()] = 1;
         } catch (NoSuchFieldError var16) {
            ;
         }

         try {
            c[Rotation.COUNTERCLOCKWISE_90.ordinal()] = 2;
         } catch (NoSuchFieldError var15) {
            ;
         }

         try {
            c[Rotation.CLOCKWISE_90.ordinal()] = 3;
         } catch (NoSuchFieldError var14) {
            ;
         }

         b = new int[BlockLever.EnumOrientation.values().length];

         try {
            b[BlockLever.EnumOrientation.EAST.ordinal()] = 1;
         } catch (NoSuchFieldError var13) {
            ;
         }

         try {
            b[BlockLever.EnumOrientation.WEST.ordinal()] = 2;
         } catch (NoSuchFieldError var12) {
            ;
         }

         try {
            b[BlockLever.EnumOrientation.SOUTH.ordinal()] = 3;
         } catch (NoSuchFieldError var11) {
            ;
         }

         try {
            b[BlockLever.EnumOrientation.NORTH.ordinal()] = 4;
         } catch (NoSuchFieldError var10) {
            ;
         }

         try {
            b[BlockLever.EnumOrientation.UP_Z.ordinal()] = 5;
         } catch (NoSuchFieldError var9) {
            ;
         }

         try {
            b[BlockLever.EnumOrientation.UP_X.ordinal()] = 6;
         } catch (NoSuchFieldError var8) {
            ;
         }

         try {
            b[BlockLever.EnumOrientation.DOWN_X.ordinal()] = 7;
         } catch (NoSuchFieldError var7) {
            ;
         }

         try {
            b[BlockLever.EnumOrientation.DOWN_Z.ordinal()] = 8;
         } catch (NoSuchFieldError var6) {
            ;
         }

         a = new int[EnumFacing.values().length];

         try {
            a[EnumFacing.DOWN.ordinal()] = 1;
         } catch (NoSuchFieldError var5) {
            ;
         }

         try {
            a[EnumFacing.UP.ordinal()] = 2;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            a[EnumFacing.NORTH.ordinal()] = 3;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            a[EnumFacing.SOUTH.ordinal()] = 4;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            a[EnumFacing.WEST.ordinal()] = 5;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[EnumFacing.EAST.ordinal()] = 6;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
