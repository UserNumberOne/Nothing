package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;

public abstract class BlockRedstoneDiode extends BlockHorizontal {
   protected static final AxisAlignedBB REDSTONE_DIODE_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D);
   protected final boolean isRepeaterPowered;

   protected BlockRedstoneDiode(boolean var1) {
      super(Material.CIRCUITS);
      this.isRepeaterPowered = var1;
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return REDSTONE_DIODE_AABB;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return var1.getBlockState(var2.down()).isFullyOpaque() ? super.canPlaceBlockAt(var1, var2) : false;
   }

   public boolean canBlockStay(World var1, BlockPos var2) {
      return var1.getBlockState(var2.down()).isFullyOpaque();
   }

   public void randomTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!this.isLocked(var1, var2, var3)) {
         boolean var5 = this.shouldBePowered(var1, var2, var3);
         if (this.isRepeaterPowered && !var5) {
            if (CraftEventFactory.callRedstoneChange(var1, var2.getX(), var2.getY(), var2.getZ(), 15, 0).getNewCurrent() != 0) {
               return;
            }

            var1.setBlockState(var2, this.getUnpoweredState(var3), 2);
         } else if (!this.isRepeaterPowered) {
            if (CraftEventFactory.callRedstoneChange(var1, var2.getX(), var2.getY(), var2.getZ(), 0, 15).getNewCurrent() != 15) {
               return;
            }

            var1.setBlockState(var2, this.getPoweredState(var3), 2);
            if (!var5) {
               var1.updateBlockTick(var2, this.getPoweredState(var3).getBlock(), this.getTickDelay(var3), -1);
            }
         }
      }

   }

   protected boolean isPowered(IBlockState var1) {
      return this.isRepeaterPowered;
   }

   public int getStrongPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return var1.getWeakPower(var2, var3, var4);
   }

   public int getWeakPower(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return !this.isPowered(var1) ? 0 : (var1.getValue(FACING) == var4 ? this.getActiveSignal(var2, var3, var1) : 0);
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (this.canBlockStay(var2, var3)) {
         this.updateState(var2, var3, var1);
      } else {
         this.dropBlockAsItem(var2, var3, var1, 0);
         var2.setBlockToAir(var3);

         for(EnumFacing var8 : EnumFacing.values()) {
            var2.notifyNeighborsOfStateChange(var3.offset(var8), this);
         }
      }

   }

   protected void updateState(World var1, BlockPos var2, IBlockState var3) {
      if (!this.isLocked(var1, var2, var3)) {
         boolean var4 = this.shouldBePowered(var1, var2, var3);
         if ((this.isRepeaterPowered && !var4 || !this.isRepeaterPowered && var4) && !var1.isBlockTickPending(var2, this)) {
            byte var5 = -1;
            if (this.isFacingTowardsRepeater(var1, var2, var3)) {
               var5 = -3;
            } else if (this.isRepeaterPowered) {
               var5 = -2;
            }

            var1.updateBlockTick(var2, this, this.getDelay(var3), var5);
         }
      }

   }

   public boolean isLocked(IBlockAccess var1, BlockPos var2, IBlockState var3) {
      return false;
   }

   protected boolean shouldBePowered(World var1, BlockPos var2, IBlockState var3) {
      return this.calculateInputStrength(var1, var2, var3) > 0;
   }

   protected int calculateInputStrength(World var1, BlockPos var2, IBlockState var3) {
      EnumFacing var4 = (EnumFacing)var3.getValue(FACING);
      BlockPos var5 = var2.offset(var4);
      int var6 = var1.getRedstonePower(var5, var4);
      if (var6 >= 15) {
         return var6;
      } else {
         IBlockState var7 = var1.getBlockState(var5);
         return Math.max(var6, var7.getBlock() == Blocks.REDSTONE_WIRE ? ((Integer)var7.getValue(BlockRedstoneWire.POWER)).intValue() : 0);
      }
   }

   protected int getPowerOnSides(IBlockAccess var1, BlockPos var2, IBlockState var3) {
      EnumFacing var4 = (EnumFacing)var3.getValue(FACING);
      EnumFacing var5 = var4.rotateY();
      EnumFacing var6 = var4.rotateYCCW();
      return Math.max(this.getPowerOnSide(var1, var2.offset(var5), var5), this.getPowerOnSide(var1, var2.offset(var6), var6));
   }

   protected int getPowerOnSide(IBlockAccess var1, BlockPos var2, EnumFacing var3) {
      IBlockState var4 = var1.getBlockState(var2);
      Block var5 = var4.getBlock();
      return this.isAlternateInput(var4) ? (var5 == Blocks.REDSTONE_BLOCK ? 15 : (var5 == Blocks.REDSTONE_WIRE ? ((Integer)var4.getValue(BlockRedstoneWire.POWER)).intValue() : var1.getStrongPower(var2, var3))) : 0;
   }

   public boolean canProvidePower(IBlockState var1) {
      return true;
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      return this.getDefaultState().withProperty(FACING, var8.getHorizontalFacing().getOpposite());
   }

   public void onBlockPlacedBy(World var1, BlockPos var2, IBlockState var3, EntityLivingBase var4, ItemStack var5) {
      if (this.shouldBePowered(var1, var2, var3)) {
         var1.scheduleUpdate(var2, this, 1);
      }

   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      this.notifyNeighbors(var1, var2, var3);
   }

   protected void notifyNeighbors(World var1, BlockPos var2, IBlockState var3) {
      EnumFacing var4 = (EnumFacing)var3.getValue(FACING);
      BlockPos var5 = var2.offset(var4.getOpposite());
      var1.notifyBlockOfStateChange(var5, this);
      var1.notifyNeighborsOfStateExcept(var5, this, var4);
   }

   public void onBlockDestroyedByPlayer(World var1, BlockPos var2, IBlockState var3) {
      if (this.isRepeaterPowered) {
         for(EnumFacing var7 : EnumFacing.values()) {
            var1.notifyNeighborsOfStateChange(var2.offset(var7), this);
         }
      }

      super.onBlockDestroyedByPlayer(var1, var2, var3);
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   protected boolean isAlternateInput(IBlockState var1) {
      return var1.canProvidePower();
   }

   protected int getActiveSignal(IBlockAccess var1, BlockPos var2, IBlockState var3) {
      return 15;
   }

   public static boolean isDiode(IBlockState var0) {
      return Blocks.UNPOWERED_REPEATER.isSameDiode(var0) || Blocks.UNPOWERED_COMPARATOR.isSameDiode(var0);
   }

   public boolean isSameDiode(IBlockState var1) {
      Block var2 = var1.getBlock();
      return var2 == this.getPoweredState(this.getDefaultState()).getBlock() || var2 == this.getUnpoweredState(this.getDefaultState()).getBlock();
   }

   public boolean isFacingTowardsRepeater(World var1, BlockPos var2, IBlockState var3) {
      EnumFacing var4 = ((EnumFacing)var3.getValue(FACING)).getOpposite();
      BlockPos var5 = var2.offset(var4);
      return isDiode(var1.getBlockState(var5)) ? var1.getBlockState(var5).getValue(FACING) != var4 : false;
   }

   protected int getTickDelay(IBlockState var1) {
      return this.getDelay(var1);
   }

   protected abstract int getDelay(IBlockState var1);

   protected abstract IBlockState getPoweredState(IBlockState var1);

   protected abstract IBlockState getUnpoweredState(IBlockState var1);

   public boolean isAssociatedBlock(Block var1) {
      return this.isSameDiode(var1.getDefaultState());
   }
}
