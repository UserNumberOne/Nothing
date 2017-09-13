package net.minecraft.block;

import com.google.common.base.Objects;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockTripWireHook extends Block {
   public static final PropertyDirection FACING = BlockHorizontal.FACING;
   public static final PropertyBool POWERED = PropertyBool.create("powered");
   public static final PropertyBool ATTACHED = PropertyBool.create("attached");
   protected static final AxisAlignedBB HOOK_NORTH_AABB = new AxisAlignedBB(0.3125D, 0.0D, 0.625D, 0.6875D, 0.625D, 1.0D);
   protected static final AxisAlignedBB HOOK_SOUTH_AABB = new AxisAlignedBB(0.3125D, 0.0D, 0.0D, 0.6875D, 0.625D, 0.375D);
   protected static final AxisAlignedBB HOOK_WEST_AABB = new AxisAlignedBB(0.625D, 0.0D, 0.3125D, 1.0D, 0.625D, 0.6875D);
   protected static final AxisAlignedBB HOOK_EAST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.3125D, 0.375D, 0.625D, 0.6875D);

   public BlockTripWireHook() {
      super(Material.CIRCUITS);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(POWERED, Boolean.valueOf(false)).withProperty(ATTACHED, Boolean.valueOf(false)));
      this.setCreativeTab(CreativeTabs.REDSTONE);
      this.setTickRandomly(true);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      switch((EnumFacing)state.getValue(FACING)) {
      case EAST:
      default:
         return HOOK_EAST_AABB;
      case WEST:
         return HOOK_WEST_AABB;
      case SOUTH:
         return HOOK_SOUTH_AABB;
      case NORTH:
         return HOOK_NORTH_AABB;
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

   public boolean canPlaceBlockOnSide(World var1, BlockPos var2, EnumFacing var3) {
      return side.getAxis().isHorizontal() && worldIn.getBlockState(pos.offset(side.getOpposite())).isSideSolid(worldIn, pos.offset(side.getOpposite()), side);
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      for(EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
         if (worldIn.getBlockState(pos.offset(enumfacing)).isSideSolid(worldIn, pos.offset(enumfacing), enumfacing.getOpposite())) {
            return true;
         }
      }

      return false;
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      IBlockState iblockstate = this.getDefaultState().withProperty(POWERED, Boolean.valueOf(false)).withProperty(ATTACHED, Boolean.valueOf(false));
      if (facing.getAxis().isHorizontal()) {
         iblockstate = iblockstate.withProperty(FACING, facing);
      }

      return iblockstate;
   }

   public void onBlockPlacedBy(World var1, BlockPos var2, IBlockState var3, EntityLivingBase var4, ItemStack var5) {
      this.calculateState(worldIn, pos, state, false, false, -1, (IBlockState)null);
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (blockIn != this && this.checkForDrop(worldIn, pos, state)) {
         EnumFacing enumfacing = (EnumFacing)state.getValue(FACING);
         if (!worldIn.getBlockState(pos.offset(enumfacing.getOpposite())).isSideSolid(worldIn, pos.offset(enumfacing.getOpposite()), enumfacing)) {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
         }
      }

   }

   public void calculateState(World var1, BlockPos var2, IBlockState var3, boolean var4, boolean var5, int var6, @Nullable IBlockState var7) {
      EnumFacing enumfacing = (EnumFacing)hookState.getValue(FACING);
      boolean flag = ((Boolean)hookState.getValue(ATTACHED)).booleanValue();
      boolean flag1 = ((Boolean)hookState.getValue(POWERED)).booleanValue();
      boolean flag2 = !p_176260_4_;
      boolean flag3 = false;
      int i = 0;
      IBlockState[] aiblockstate = new IBlockState[42];

      for(int j = 1; j < 42; ++j) {
         BlockPos blockpos = pos.offset(enumfacing, j);
         IBlockState iblockstate = worldIn.getBlockState(blockpos);
         if (iblockstate.getBlock() == Blocks.TRIPWIRE_HOOK) {
            if (iblockstate.getValue(FACING) == enumfacing.getOpposite()) {
               i = j;
            }
            break;
         }

         if (iblockstate.getBlock() != Blocks.TRIPWIRE && j != p_176260_6_) {
            aiblockstate[j] = null;
            flag2 = false;
         } else {
            if (j == p_176260_6_) {
               iblockstate = (IBlockState)Objects.firstNonNull(p_176260_7_, iblockstate);
            }

            boolean flag4 = !((Boolean)iblockstate.getValue(BlockTripWire.DISARMED)).booleanValue();
            boolean flag5 = ((Boolean)iblockstate.getValue(BlockTripWire.POWERED)).booleanValue();
            flag3 |= flag4 && flag5;
            aiblockstate[j] = iblockstate;
            if (j == p_176260_6_) {
               worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
               flag2 &= flag4;
            }
         }
      }

      flag2 = flag2 & i > 1;
      flag3 = flag3 & flag2;
      IBlockState iblockstate1 = this.getDefaultState().withProperty(ATTACHED, Boolean.valueOf(flag2)).withProperty(POWERED, Boolean.valueOf(flag3));
      if (i > 0) {
         BlockPos blockpos1 = pos.offset(enumfacing, i);
         EnumFacing enumfacing1 = enumfacing.getOpposite();
         worldIn.setBlockState(blockpos1, iblockstate1.withProperty(FACING, enumfacing1), 3);
         this.notifyNeighbors(worldIn, blockpos1, enumfacing1);
         this.playSound(worldIn, blockpos1, flag2, flag3, flag, flag1);
      }

      this.playSound(worldIn, pos, flag2, flag3, flag, flag1);
      if (!p_176260_4_) {
         worldIn.setBlockState(pos, iblockstate1.withProperty(FACING, enumfacing), 3);
         if (p_176260_5_) {
            this.notifyNeighbors(worldIn, pos, enumfacing);
         }
      }

      if (flag != flag2) {
         for(int k = 1; k < i; ++k) {
            BlockPos blockpos2 = pos.offset(enumfacing, k);
            IBlockState iblockstate2 = aiblockstate[k];
            if (iblockstate2 != null && worldIn.getBlockState(blockpos2).getMaterial() != Material.AIR) {
               worldIn.setBlockState(blockpos2, iblockstate2.withProperty(ATTACHED, Boolean.valueOf(flag2)), 3);
            }
         }
      }

   }

   public void randomTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      this.calculateState(worldIn, pos, state, false, true, -1, (IBlockState)null);
   }

   private void playSound(World var1, BlockPos var2, boolean var3, boolean var4, boolean var5, boolean var6) {
      if (p_180694_4_ && !p_180694_6_) {
         worldIn.playSound((EntityPlayer)null, pos, SoundEvents.BLOCK_TRIPWIRE_CLICK_ON, SoundCategory.BLOCKS, 0.4F, 0.6F);
      } else if (!p_180694_4_ && p_180694_6_) {
         worldIn.playSound((EntityPlayer)null, pos, SoundEvents.BLOCK_TRIPWIRE_CLICK_OFF, SoundCategory.BLOCKS, 0.4F, 0.5F);
      } else if (p_180694_3_ && !p_180694_5_) {
         worldIn.playSound((EntityPlayer)null, pos, SoundEvents.BLOCK_TRIPWIRE_ATTACH, SoundCategory.BLOCKS, 0.4F, 0.7F);
      } else if (!p_180694_3_ && p_180694_5_) {
         worldIn.playSound((EntityPlayer)null, pos, SoundEvents.BLOCK_TRIPWIRE_DETACH, SoundCategory.BLOCKS, 0.4F, 1.2F / (worldIn.rand.nextFloat() * 0.2F + 0.9F));
      }

   }

   private void notifyNeighbors(World var1, BlockPos var2, EnumFacing var3) {
      worldIn.notifyNeighborsOfStateChange(pos, this);
      worldIn.notifyNeighborsOfStateChange(pos.offset(side.getOpposite()), this);
   }

   private boolean checkForDrop(World var1, BlockPos var2, IBlockState var3) {
      if (!this.canPlaceBlockAt(worldIn, pos)) {
         this.dropBlockAsItem(worldIn, pos, state, 0);
         worldIn.setBlockToAir(pos);
         return false;
      } else {
         return true;
      }
   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      boolean flag = ((Boolean)state.getValue(ATTACHED)).booleanValue();
      boolean flag1 = ((Boolean)state.getValue(POWERED)).booleanValue();
      if (flag || flag1) {
         this.calculateState(worldIn, pos, state, true, false, -1, (IBlockState)null);
      }

      if (flag1) {
         worldIn.notifyNeighborsOfStateChange(pos, this);
         worldIn.notifyNeighborsOfStateChange(pos.offset(((EnumFacing)state.getValue(FACING)).getOpposite()), this);
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

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT_MIPPED;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta & 3)).withProperty(POWERED, Boolean.valueOf((meta & 8) > 0)).withProperty(ATTACHED, Boolean.valueOf((meta & 4) > 0));
   }

   public int getMetaFromState(IBlockState var1) {
      int i = 0;
      i = i | ((EnumFacing)state.getValue(FACING)).getHorizontalIndex();
      if (((Boolean)state.getValue(POWERED)).booleanValue()) {
         i |= 8;
      }

      if (((Boolean)state.getValue(ATTACHED)).booleanValue()) {
         i |= 4;
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
      return new BlockStateContainer(this, new IProperty[]{FACING, POWERED, ATTACHED});
   }
}
