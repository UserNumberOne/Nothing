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
      switch((BlockLever.EnumOrientation)var1.getValue(FACING)) {
      case EAST:
      default:
         return LEVER_EAST_AABB;
      case WEST:
         return LEVER_WEST_AABB;
      case SOUTH:
         return LEVER_SOUTH_AABB;
      case NORTH:
         return LEVER_NORTH_AABB;
      case UP_Z:
      case UP_X:
         return LEVER_UP_AABB;
      case DOWN_X:
      case DOWN_Z:
         return LEVER_DOWN_AABB;
      }
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (var1.isRemote) {
         return true;
      } else {
         var3 = var3.cycleProperty(POWERED);
         var1.setBlockState(var2, var3, 3);
         float var11 = ((Boolean)var3.getValue(POWERED)).booleanValue() ? 0.6F : 0.5F;
         var1.playSound((EntityPlayer)null, var2, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, var11);
         var1.notifyNeighborsOfStateChange(var2, this);
         EnumFacing var12 = ((BlockLever.EnumOrientation)var3.getValue(FACING)).getFacing();
         var1.notifyNeighborsOfStateChange(var2.offset(var12.getOpposite()), this);
         return true;
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
      int var2 = 0;
      var2 = var2 | ((BlockLever.EnumOrientation)var1.getValue(FACING)).getMetadata();
      if (((Boolean)var1.getValue(POWERED)).booleanValue()) {
         var2 |= 8;
      }

      return var2;
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      switch(var2) {
      case CLOCKWISE_180:
         switch((BlockLever.EnumOrientation)var1.getValue(FACING)) {
         case EAST:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.WEST);
         case WEST:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.EAST);
         case SOUTH:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.NORTH);
         case NORTH:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.SOUTH);
         default:
            return var1;
         }
      case COUNTERCLOCKWISE_90:
         switch((BlockLever.EnumOrientation)var1.getValue(FACING)) {
         case EAST:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.NORTH);
         case WEST:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.SOUTH);
         case SOUTH:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.EAST);
         case NORTH:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.WEST);
         case UP_Z:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.UP_X);
         case UP_X:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.UP_Z);
         case DOWN_X:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.DOWN_Z);
         case DOWN_Z:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.DOWN_X);
         }
      case CLOCKWISE_90:
         switch((BlockLever.EnumOrientation)var1.getValue(FACING)) {
         case EAST:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.SOUTH);
         case WEST:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.NORTH);
         case SOUTH:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.WEST);
         case NORTH:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.EAST);
         case UP_Z:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.UP_X);
         case UP_X:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.UP_Z);
         case DOWN_X:
            return var1.withProperty(FACING, BlockLever.EnumOrientation.DOWN_Z);
         case DOWN_Z:
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

   private boolean canAttach(World var1, BlockPos var2, EnumFacing var3) {
      return var1.isSideSolid(var2, var3);
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
         switch(var0) {
         case DOWN:
            switch(var1.getAxis()) {
            case X:
               return DOWN_X;
            case Z:
               return DOWN_Z;
            default:
               throw new IllegalArgumentException("Invalid entityFacing " + var1 + " for facing " + var0);
            }
         case UP:
            switch(var1.getAxis()) {
            case X:
               return UP_X;
            case Z:
               return UP_Z;
            default:
               throw new IllegalArgumentException("Invalid entityFacing " + var1 + " for facing " + var0);
            }
         case NORTH:
            return NORTH;
         case SOUTH:
            return SOUTH;
         case WEST:
            return WEST;
         case EAST:
            return EAST;
         default:
            throw new IllegalArgumentException("Invalid facing: " + var0);
         }
      }

      public String getName() {
         return this.name;
      }

      static {
         for(BlockLever.EnumOrientation var3 : values()) {
            META_LOOKUP[var3.getMetadata()] = var3;
         }

      }
   }
}
