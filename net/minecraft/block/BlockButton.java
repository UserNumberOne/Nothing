package net.minecraft.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockButton extends BlockDirectional {
   public static final PropertyBool POWERED = PropertyBool.create("powered");
   protected static final AxisAlignedBB AABB_DOWN_OFF = new AxisAlignedBB(0.3125D, 0.875D, 0.375D, 0.6875D, 1.0D, 0.625D);
   protected static final AxisAlignedBB AABB_UP_OFF = new AxisAlignedBB(0.3125D, 0.0D, 0.375D, 0.6875D, 0.125D, 0.625D);
   protected static final AxisAlignedBB AABB_NORTH_OFF = new AxisAlignedBB(0.3125D, 0.375D, 0.875D, 0.6875D, 0.625D, 1.0D);
   protected static final AxisAlignedBB AABB_SOUTH_OFF = new AxisAlignedBB(0.3125D, 0.375D, 0.0D, 0.6875D, 0.625D, 0.125D);
   protected static final AxisAlignedBB AABB_WEST_OFF = new AxisAlignedBB(0.875D, 0.375D, 0.3125D, 1.0D, 0.625D, 0.6875D);
   protected static final AxisAlignedBB AABB_EAST_OFF = new AxisAlignedBB(0.0D, 0.375D, 0.3125D, 0.125D, 0.625D, 0.6875D);
   protected static final AxisAlignedBB AABB_DOWN_ON = new AxisAlignedBB(0.3125D, 0.9375D, 0.375D, 0.6875D, 1.0D, 0.625D);
   protected static final AxisAlignedBB AABB_UP_ON = new AxisAlignedBB(0.3125D, 0.0D, 0.375D, 0.6875D, 0.0625D, 0.625D);
   protected static final AxisAlignedBB AABB_NORTH_ON = new AxisAlignedBB(0.3125D, 0.375D, 0.9375D, 0.6875D, 0.625D, 1.0D);
   protected static final AxisAlignedBB AABB_SOUTH_ON = new AxisAlignedBB(0.3125D, 0.375D, 0.0D, 0.6875D, 0.625D, 0.0625D);
   protected static final AxisAlignedBB AABB_WEST_ON = new AxisAlignedBB(0.9375D, 0.375D, 0.3125D, 1.0D, 0.625D, 0.6875D);
   protected static final AxisAlignedBB AABB_EAST_ON = new AxisAlignedBB(0.0D, 0.375D, 0.3125D, 0.0625D, 0.625D, 0.6875D);
   private final boolean wooden;

   protected BlockButton(boolean var1) {
      super(Material.CIRCUITS);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(POWERED, Boolean.valueOf(false)));
      this.setTickRandomly(true);
      this.setCreativeTab(CreativeTabs.REDSTONE);
      this.wooden = wooden;
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState var1, World var2, BlockPos var3) {
      return NULL_AABB;
   }

   public int tickRate(World var1) {
      return this.wooden ? 30 : 20;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean canPlaceBlockOnSide(World var1, BlockPos var2, EnumFacing var3) {
      return canPlaceBlock(worldIn, pos, side.getOpposite());
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      for(EnumFacing enumfacing : EnumFacing.values()) {
         if (canPlaceBlock(worldIn, pos, enumfacing)) {
            return true;
         }
      }

      return false;
   }

   protected static boolean canPlaceBlock(World var0, BlockPos var1, EnumFacing var2) {
      BlockPos blockpos = pos.offset(direction);
      return worldIn.getBlockState(blockpos).isSideSolid(worldIn, blockpos, direction.getOpposite());
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      return canPlaceBlock(worldIn, pos, facing.getOpposite()) ? this.getDefaultState().withProperty(FACING, facing).withProperty(POWERED, Boolean.valueOf(false)) : this.getDefaultState().withProperty(FACING, EnumFacing.DOWN).withProperty(POWERED, Boolean.valueOf(false));
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (this.checkForDrop(worldIn, pos, state) && !canPlaceBlock(worldIn, pos, ((EnumFacing)state.getValue(FACING)).getOpposite())) {
         this.dropBlockAsItem(worldIn, pos, state, 0);
         worldIn.setBlockToAir(pos);
      }

   }

   private boolean checkForDrop(World var1, BlockPos var2, IBlockState var3) {
      if (this.canPlaceBlockAt(worldIn, pos)) {
         return true;
      } else {
         this.dropBlockAsItem(worldIn, pos, state, 0);
         worldIn.setBlockToAir(pos);
         return false;
      }
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      EnumFacing enumfacing = (EnumFacing)state.getValue(FACING);
      boolean flag = ((Boolean)state.getValue(POWERED)).booleanValue();
      switch(enumfacing) {
      case EAST:
         return flag ? AABB_EAST_ON : AABB_EAST_OFF;
      case WEST:
         return flag ? AABB_WEST_ON : AABB_WEST_OFF;
      case SOUTH:
         return flag ? AABB_SOUTH_ON : AABB_SOUTH_OFF;
      case NORTH:
      default:
         return flag ? AABB_NORTH_ON : AABB_NORTH_OFF;
      case UP:
         return flag ? AABB_UP_ON : AABB_UP_OFF;
      case DOWN:
         return flag ? AABB_DOWN_ON : AABB_DOWN_OFF;
      }
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (((Boolean)state.getValue(POWERED)).booleanValue()) {
         return true;
      } else {
         worldIn.setBlockState(pos, state.withProperty(POWERED, Boolean.valueOf(true)), 3);
         worldIn.markBlockRangeForRenderUpdate(pos, pos);
         this.playClickSound(playerIn, worldIn, pos);
         this.notifyNeighbors(worldIn, pos, (EnumFacing)state.getValue(FACING));
         worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
         return true;
      }
   }

   protected abstract void playClickSound(@Nullable EntityPlayer var1, World var2, BlockPos var3);

   protected abstract void playReleaseSound(World var1, BlockPos var2);

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      if (((Boolean)state.getValue(POWERED)).booleanValue()) {
         this.notifyNeighbors(worldIn, pos, (EnumFacing)state.getValue(FACING));
      }

      super.breakBlock(worldIn, pos, state);
   }

   public int getWeakPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return ((Boolean)blockState.getValue(POWERED)).booleanValue() ? 15 : 0;
   }

   public int getStrongPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return !((Boolean)blockState.getValue(POWERED)).booleanValue() ? 0 : (blockState.getValue(FACING) == side ? 15 : 0);
   }

   public boolean canProvidePower(IBlockState var1) {
      return true;
   }

   public void randomTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!worldIn.isRemote && ((Boolean)state.getValue(POWERED)).booleanValue()) {
         if (this.wooden) {
            this.checkPressed(state, worldIn, pos);
         } else {
            worldIn.setBlockState(pos, state.withProperty(POWERED, Boolean.valueOf(false)));
            this.notifyNeighbors(worldIn, pos, (EnumFacing)state.getValue(FACING));
            this.playReleaseSound(worldIn, pos);
            worldIn.markBlockRangeForRenderUpdate(pos, pos);
         }
      }

   }

   public void onEntityCollidedWithBlock(World var1, BlockPos var2, IBlockState var3, Entity var4) {
      if (!worldIn.isRemote && this.wooden && !((Boolean)state.getValue(POWERED)).booleanValue()) {
         this.checkPressed(state, worldIn, pos);
      }

   }

   private void checkPressed(IBlockState var1, World var2, BlockPos var3) {
      List list = p_185616_2_.getEntitiesWithinAABB(EntityArrow.class, p_185616_1_.getBoundingBox(p_185616_2_, p_185616_3_).offset(p_185616_3_));
      boolean flag = !list.isEmpty();
      boolean flag1 = ((Boolean)p_185616_1_.getValue(POWERED)).booleanValue();
      if (flag && !flag1) {
         p_185616_2_.setBlockState(p_185616_3_, p_185616_1_.withProperty(POWERED, Boolean.valueOf(true)));
         this.notifyNeighbors(p_185616_2_, p_185616_3_, (EnumFacing)p_185616_1_.getValue(FACING));
         p_185616_2_.markBlockRangeForRenderUpdate(p_185616_3_, p_185616_3_);
         this.playClickSound((EntityPlayer)null, p_185616_2_, p_185616_3_);
      }

      if (!flag && flag1) {
         p_185616_2_.setBlockState(p_185616_3_, p_185616_1_.withProperty(POWERED, Boolean.valueOf(false)));
         this.notifyNeighbors(p_185616_2_, p_185616_3_, (EnumFacing)p_185616_1_.getValue(FACING));
         p_185616_2_.markBlockRangeForRenderUpdate(p_185616_3_, p_185616_3_);
         this.playReleaseSound(p_185616_2_, p_185616_3_);
      }

      if (flag) {
         p_185616_2_.scheduleUpdate(new BlockPos(p_185616_3_), this, this.tickRate(p_185616_2_));
      }

   }

   private void notifyNeighbors(World var1, BlockPos var2, EnumFacing var3) {
      worldIn.notifyNeighborsOfStateChange(pos, this);
      worldIn.notifyNeighborsOfStateChange(pos.offset(facing.getOpposite()), this);
   }

   public IBlockState getStateFromMeta(int var1) {
      EnumFacing enumfacing;
      switch(meta & 7) {
      case 0:
         enumfacing = EnumFacing.DOWN;
         break;
      case 1:
         enumfacing = EnumFacing.EAST;
         break;
      case 2:
         enumfacing = EnumFacing.WEST;
         break;
      case 3:
         enumfacing = EnumFacing.SOUTH;
         break;
      case 4:
         enumfacing = EnumFacing.NORTH;
         break;
      case 5:
      default:
         enumfacing = EnumFacing.UP;
      }

      return this.getDefaultState().withProperty(FACING, enumfacing).withProperty(POWERED, Boolean.valueOf((meta & 8) > 0));
   }

   public int getMetaFromState(IBlockState var1) {
      int i;
      switch((EnumFacing)state.getValue(FACING)) {
      case EAST:
         i = 1;
         break;
      case WEST:
         i = 2;
         break;
      case SOUTH:
         i = 3;
         break;
      case NORTH:
         i = 4;
         break;
      case UP:
      default:
         i = 5;
         break;
      case DOWN:
         i = 0;
      }

      if (((Boolean)state.getValue(POWERED)).booleanValue()) {
         i |= 8;
      }

      return i;
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, POWERED});
   }
}
