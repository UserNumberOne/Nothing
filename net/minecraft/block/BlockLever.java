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
   public AxisAlignedBB getCollisionBoundingBox(IBlockState iblockdata, World world, BlockPos blockposition) {
      return NULL_AABB;
   }

   public boolean isOpaqueCube(IBlockState iblockdata) {
      return false;
   }

   public boolean isFullCube(IBlockState iblockdata) {
      return false;
   }

   public boolean canPlaceBlockOnSide(World world, BlockPos blockposition, EnumFacing enumdirection) {
      return canAttachTo(world, blockposition, enumdirection.getOpposite());
   }

   public boolean canPlaceBlockAt(World world, BlockPos blockposition) {
      for(EnumFacing enumdirection : EnumFacing.values()) {
         if (canAttachTo(world, blockposition, enumdirection)) {
            return true;
         }
      }

      return false;
   }

   protected static boolean canAttachTo(World world, BlockPos blockposition, EnumFacing enumdirection) {
      return BlockButton.canPlaceBlock(world, blockposition, enumdirection);
   }

   public IBlockState getStateForPlacement(World world, BlockPos blockposition, EnumFacing enumdirection, float f, float f1, float f2, int i, EntityLivingBase entityliving) {
      IBlockState iblockdata = this.getDefaultState().withProperty(POWERED, Boolean.valueOf(false));
      if (canAttachTo(world, blockposition, enumdirection.getOpposite())) {
         return iblockdata.withProperty(FACING, BlockLever.EnumOrientation.forFacings(enumdirection, entityliving.getHorizontalFacing()));
      } else {
         for(EnumFacing enumdirection1 : EnumFacing.Plane.HORIZONTAL) {
            if (enumdirection1 != enumdirection && canAttachTo(world, blockposition, enumdirection1.getOpposite())) {
               return iblockdata.withProperty(FACING, BlockLever.EnumOrientation.forFacings(enumdirection1, entityliving.getHorizontalFacing()));
            }
         }

         if (world.getBlockState(blockposition.down()).isFullyOpaque()) {
            return iblockdata.withProperty(FACING, BlockLever.EnumOrientation.forFacings(EnumFacing.UP, entityliving.getHorizontalFacing()));
         } else {
            return iblockdata;
         }
      }
   }

   public void neighborChanged(IBlockState iblockdata, World world, BlockPos blockposition, Block block) {
      if (this.checkCanSurvive(world, blockposition, iblockdata) && !canAttachTo(world, blockposition, ((BlockLever.EnumOrientation)iblockdata.getValue(FACING)).getFacing().getOpposite())) {
         this.dropBlockAsItem(world, blockposition, iblockdata, 0);
         world.setBlockToAir(blockposition);
      }

   }

   private boolean checkCanSurvive(World world, BlockPos blockposition, IBlockState iblockdata) {
      if (this.canPlaceBlockAt(world, blockposition)) {
         return true;
      } else {
         this.dropBlockAsItem(world, blockposition, iblockdata, 0);
         world.setBlockToAir(blockposition);
         return false;
      }
   }

   public AxisAlignedBB getBoundingBox(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      switch(BlockLever.SyntheticClass_1.b[((BlockLever.EnumOrientation)iblockdata.getValue(FACING)).ordinal()]) {
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

   public boolean onBlockActivated(World world, BlockPos blockposition, IBlockState iblockdata, EntityPlayer entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack, EnumFacing enumdirection, float f, float f1, float f2) {
      if (world.isRemote) {
         return true;
      } else {
         boolean powered = ((Boolean)iblockdata.getValue(POWERED)).booleanValue();
         org.bukkit.block.Block block = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
         int old = powered ? 15 : 0;
         int current = !powered ? 15 : 0;
         BlockRedstoneEvent eventRedstone = new BlockRedstoneEvent(block, old, current);
         world.getServer().getPluginManager().callEvent(eventRedstone);
         if (eventRedstone.getNewCurrent() > 0 != !powered) {
            return true;
         } else {
            iblockdata = iblockdata.cycleProperty(POWERED);
            world.setBlockState(blockposition, iblockdata, 3);
            float f3 = ((Boolean)iblockdata.getValue(POWERED)).booleanValue() ? 0.6F : 0.5F;
            world.playSound((EntityPlayer)null, blockposition, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, f3);
            world.notifyNeighborsOfStateChange(blockposition, this);
            EnumFacing enumdirection1 = ((BlockLever.EnumOrientation)iblockdata.getValue(FACING)).getFacing();
            world.notifyNeighborsOfStateChange(blockposition.offset(enumdirection1.getOpposite()), this);
            return true;
         }
      }
   }

   public void breakBlock(World world, BlockPos blockposition, IBlockState iblockdata) {
      if (((Boolean)iblockdata.getValue(POWERED)).booleanValue()) {
         world.notifyNeighborsOfStateChange(blockposition, this);
         EnumFacing enumdirection = ((BlockLever.EnumOrientation)iblockdata.getValue(FACING)).getFacing();
         world.notifyNeighborsOfStateChange(blockposition.offset(enumdirection.getOpposite()), this);
      }

      super.breakBlock(world, blockposition, iblockdata);
   }

   public int getWeakPower(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition, EnumFacing enumdirection) {
      return ((Boolean)iblockdata.getValue(POWERED)).booleanValue() ? 15 : 0;
   }

   public int getStrongPower(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition, EnumFacing enumdirection) {
      return !((Boolean)iblockdata.getValue(POWERED)).booleanValue() ? 0 : (((BlockLever.EnumOrientation)iblockdata.getValue(FACING)).getFacing() == enumdirection ? 15 : 0);
   }

   public boolean canProvidePower(IBlockState iblockdata) {
      return true;
   }

   public IBlockState getStateFromMeta(int i) {
      return this.getDefaultState().withProperty(FACING, BlockLever.EnumOrientation.byMetadata(i & 7)).withProperty(POWERED, Boolean.valueOf((i & 8) > 0));
   }

   public int getMetaFromState(IBlockState iblockdata) {
      byte b0 = 0;
      int i = b0 | ((BlockLever.EnumOrientation)iblockdata.getValue(FACING)).getMetadata();
      if (((Boolean)iblockdata.getValue(POWERED)).booleanValue()) {
         i |= 8;
      }

      return i;
   }

   public IBlockState withRotation(IBlockState iblockdata, Rotation enumblockrotation) {
      switch(BlockLever.SyntheticClass_1.c[enumblockrotation.ordinal()]) {
      case 1:
         switch(BlockLever.SyntheticClass_1.b[((BlockLever.EnumOrientation)iblockdata.getValue(FACING)).ordinal()]) {
         case 1:
            return iblockdata.withProperty(FACING, BlockLever.EnumOrientation.WEST);
         case 2:
            return iblockdata.withProperty(FACING, BlockLever.EnumOrientation.EAST);
         case 3:
            return iblockdata.withProperty(FACING, BlockLever.EnumOrientation.NORTH);
         case 4:
            return iblockdata.withProperty(FACING, BlockLever.EnumOrientation.SOUTH);
         default:
            return iblockdata;
         }
      case 2:
         switch(BlockLever.SyntheticClass_1.b[((BlockLever.EnumOrientation)iblockdata.getValue(FACING)).ordinal()]) {
         case 1:
            return iblockdata.withProperty(FACING, BlockLever.EnumOrientation.NORTH);
         case 2:
            return iblockdata.withProperty(FACING, BlockLever.EnumOrientation.SOUTH);
         case 3:
            return iblockdata.withProperty(FACING, BlockLever.EnumOrientation.EAST);
         case 4:
            return iblockdata.withProperty(FACING, BlockLever.EnumOrientation.WEST);
         case 5:
            return iblockdata.withProperty(FACING, BlockLever.EnumOrientation.UP_X);
         case 6:
            return iblockdata.withProperty(FACING, BlockLever.EnumOrientation.UP_Z);
         case 7:
            return iblockdata.withProperty(FACING, BlockLever.EnumOrientation.DOWN_Z);
         case 8:
            return iblockdata.withProperty(FACING, BlockLever.EnumOrientation.DOWN_X);
         }
      case 3:
         switch(BlockLever.SyntheticClass_1.b[((BlockLever.EnumOrientation)iblockdata.getValue(FACING)).ordinal()]) {
         case 1:
            return iblockdata.withProperty(FACING, BlockLever.EnumOrientation.SOUTH);
         case 2:
            return iblockdata.withProperty(FACING, BlockLever.EnumOrientation.NORTH);
         case 3:
            return iblockdata.withProperty(FACING, BlockLever.EnumOrientation.WEST);
         case 4:
            return iblockdata.withProperty(FACING, BlockLever.EnumOrientation.EAST);
         case 5:
            return iblockdata.withProperty(FACING, BlockLever.EnumOrientation.UP_X);
         case 6:
            return iblockdata.withProperty(FACING, BlockLever.EnumOrientation.UP_Z);
         case 7:
            return iblockdata.withProperty(FACING, BlockLever.EnumOrientation.DOWN_Z);
         case 8:
            return iblockdata.withProperty(FACING, BlockLever.EnumOrientation.DOWN_X);
         }
      default:
         return iblockdata;
      }
   }

   public IBlockState withMirror(IBlockState iblockdata, Mirror enumblockmirror) {
      return iblockdata.withRotation(enumblockmirror.toRotation(((BlockLever.EnumOrientation)iblockdata.getValue(FACING)).getFacing()));
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
         for(BlockLever.EnumOrientation blocklever_enumleverposition : values()) {
            META_LOOKUP[blocklever_enumleverposition.getMetadata()] = blocklever_enumleverposition;
         }

      }

      private EnumOrientation(int i, String s, EnumFacing enumdirection) {
         this.meta = i;
         this.name = s;
         this.facing = enumdirection;
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

      public static BlockLever.EnumOrientation byMetadata(int i) {
         if (i < 0 || i >= META_LOOKUP.length) {
            i = 0;
         }

         return META_LOOKUP[i];
      }

      public static BlockLever.EnumOrientation forFacings(EnumFacing enumdirection, EnumFacing enumdirection1) {
         switch(BlockLever.SyntheticClass_1.a[enumdirection.ordinal()]) {
         case 1:
            switch(BlockLever.SyntheticClass_1.d[enumdirection1.getAxis().ordinal()]) {
            case 1:
               return DOWN_X;
            case 2:
               return DOWN_Z;
            default:
               throw new IllegalArgumentException("Invalid entityFacing " + enumdirection1 + " for facing " + enumdirection);
            }
         case 2:
            switch(BlockLever.SyntheticClass_1.d[enumdirection1.getAxis().ordinal()]) {
            case 1:
               return UP_X;
            case 2:
               return UP_Z;
            default:
               throw new IllegalArgumentException("Invalid entityFacing " + enumdirection1 + " for facing " + enumdirection);
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
            throw new IllegalArgumentException("Invalid facing: " + enumdirection);
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
