package net.minecraft.block;

import java.util.EnumSet;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BlockRedstoneDiode extends BlockHorizontal {
   protected static final AxisAlignedBB REDSTONE_DIODE_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D);
   protected final boolean isRepeaterPowered;

   protected BlockRedstoneDiode(boolean var1) {
      super(Material.CIRCUITS);
      this.isRepeaterPowered = powered;
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return REDSTONE_DIODE_AABB;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return worldIn.getBlockState(pos.down()).isFullyOpaque() ? super.canPlaceBlockAt(worldIn, pos) : false;
   }

   public boolean canBlockStay(World var1, BlockPos var2) {
      return worldIn.getBlockState(pos.down()).isFullyOpaque();
   }

   public void randomTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!this.isLocked(worldIn, pos, state)) {
         boolean flag = this.shouldBePowered(worldIn, pos, state);
         if (this.isRepeaterPowered && !flag) {
            worldIn.setBlockState(pos, this.getUnpoweredState(state), 2);
         } else if (!this.isRepeaterPowered) {
            worldIn.setBlockState(pos, this.getPoweredState(state), 2);
            if (!flag) {
               worldIn.updateBlockTick(pos, this.getPoweredState(state).getBlock(), this.getTickDelay(state), -1);
            }
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public boolean shouldSideBeRendered(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return side.getAxis() != EnumFacing.Axis.Y;
   }

   protected boolean isPowered(IBlockState var1) {
      return this.isRepeaterPowered;
   }

   public int getStrongPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return blockState.getWeakPower(blockAccess, pos, side);
   }

   public int getWeakPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return !this.isPowered(blockState) ? 0 : (blockState.getValue(FACING) == side ? this.getActiveSignal(blockAccess, pos, blockState) : 0);
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (this.canBlockStay(worldIn, pos)) {
         this.updateState(worldIn, pos, state);
      } else {
         this.dropBlockAsItem(worldIn, pos, state, 0);
         worldIn.setBlockToAir(pos);

         for(EnumFacing enumfacing : EnumFacing.values()) {
            worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this);
         }
      }

   }

   protected void updateState(World var1, BlockPos var2, IBlockState var3) {
      if (!this.isLocked(worldIn, pos, state)) {
         boolean flag = this.shouldBePowered(worldIn, pos, state);
         if ((this.isRepeaterPowered && !flag || !this.isRepeaterPowered && flag) && !worldIn.isBlockTickPending(pos, this)) {
            int i = -1;
            if (this.isFacingTowardsRepeater(worldIn, pos, state)) {
               i = -3;
            } else if (this.isRepeaterPowered) {
               i = -2;
            }

            worldIn.updateBlockTick(pos, this, this.getDelay(state), i);
         }
      }

   }

   public boolean isLocked(IBlockAccess var1, BlockPos var2, IBlockState var3) {
      return false;
   }

   protected boolean shouldBePowered(World var1, BlockPos var2, IBlockState var3) {
      return this.calculateInputStrength(worldIn, pos, state) > 0;
   }

   protected int calculateInputStrength(World var1, BlockPos var2, IBlockState var3) {
      EnumFacing enumfacing = (EnumFacing)state.getValue(FACING);
      BlockPos blockpos = pos.offset(enumfacing);
      int i = worldIn.getRedstonePower(blockpos, enumfacing);
      if (i >= 15) {
         return i;
      } else {
         IBlockState iblockstate = worldIn.getBlockState(blockpos);
         return Math.max(i, iblockstate.getBlock() == Blocks.REDSTONE_WIRE ? ((Integer)iblockstate.getValue(BlockRedstoneWire.POWER)).intValue() : 0);
      }
   }

   protected int getPowerOnSides(IBlockAccess var1, BlockPos var2, IBlockState var3) {
      EnumFacing enumfacing = (EnumFacing)state.getValue(FACING);
      EnumFacing enumfacing1 = enumfacing.rotateY();
      EnumFacing enumfacing2 = enumfacing.rotateYCCW();
      return Math.max(this.getPowerOnSide(worldIn, pos.offset(enumfacing1), enumfacing1), this.getPowerOnSide(worldIn, pos.offset(enumfacing2), enumfacing2));
   }

   protected int getPowerOnSide(IBlockAccess var1, BlockPos var2, EnumFacing var3) {
      IBlockState iblockstate = worldIn.getBlockState(pos);
      Block block = iblockstate.getBlock();
      return this.isAlternateInput(iblockstate) ? (block == Blocks.REDSTONE_BLOCK ? 15 : (block == Blocks.REDSTONE_WIRE ? ((Integer)iblockstate.getValue(BlockRedstoneWire.POWER)).intValue() : worldIn.getStrongPower(pos, side))) : 0;
   }

   public boolean canProvidePower(IBlockState var1) {
      return true;
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
   }

   public void onBlockPlacedBy(World var1, BlockPos var2, IBlockState var3, EntityLivingBase var4, ItemStack var5) {
      if (this.shouldBePowered(worldIn, pos, state)) {
         worldIn.scheduleUpdate(pos, this, 1);
      }

   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      this.notifyNeighbors(worldIn, pos, state);
   }

   protected void notifyNeighbors(World var1, BlockPos var2, IBlockState var3) {
      EnumFacing enumfacing = (EnumFacing)state.getValue(FACING);
      BlockPos blockpos = pos.offset(enumfacing.getOpposite());
      if (!ForgeEventFactory.onNeighborNotify(worldIn, pos, worldIn.getBlockState(pos), EnumSet.of(enumfacing.getOpposite())).isCanceled()) {
         worldIn.notifyBlockOfStateChange(blockpos, this);
         worldIn.notifyNeighborsOfStateExcept(blockpos, this, enumfacing);
      }
   }

   public void onBlockDestroyedByPlayer(World var1, BlockPos var2, IBlockState var3) {
      if (this.isRepeaterPowered) {
         for(EnumFacing enumfacing : EnumFacing.values()) {
            worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this);
         }
      }

      super.onBlockDestroyedByPlayer(worldIn, pos, state);
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   protected boolean isAlternateInput(IBlockState var1) {
      return state.canProvidePower();
   }

   protected int getActiveSignal(IBlockAccess var1, BlockPos var2, IBlockState var3) {
      return 15;
   }

   public static boolean isDiode(IBlockState var0) {
      return Blocks.UNPOWERED_REPEATER.isSameDiode(state) || Blocks.UNPOWERED_COMPARATOR.isSameDiode(state);
   }

   public boolean isSameDiode(IBlockState var1) {
      Block block = state.getBlock();
      return block == this.getPoweredState(this.getDefaultState()).getBlock() || block == this.getUnpoweredState(this.getDefaultState()).getBlock();
   }

   public boolean isFacingTowardsRepeater(World var1, BlockPos var2, IBlockState var3) {
      EnumFacing enumfacing = ((EnumFacing)state.getValue(FACING)).getOpposite();
      BlockPos blockpos = pos.offset(enumfacing);
      return isDiode(worldIn.getBlockState(blockpos)) ? worldIn.getBlockState(blockpos).getValue(FACING) != enumfacing : false;
   }

   protected int getTickDelay(IBlockState var1) {
      return this.getDelay(state);
   }

   protected abstract int getDelay(IBlockState var1);

   protected abstract IBlockState getPoweredState(IBlockState var1);

   protected abstract IBlockState getUnpoweredState(IBlockState var1);

   public boolean isAssociatedBlock(Block var1) {
      return this.isSameDiode(other.getDefaultState());
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT;
   }
}
