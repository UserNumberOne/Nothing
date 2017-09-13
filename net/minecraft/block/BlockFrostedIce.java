package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BlockFrostedIce extends BlockIce {
   public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 3);

   public BlockFrostedIce() {
      this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, Integer.valueOf(0)));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)state.getValue(AGE)).intValue();
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(AGE, Integer.valueOf(MathHelper.clamp(meta, 0, 3)));
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if ((rand.nextInt(3) == 0 || this.countNeighbors(worldIn, pos) < 4) && worldIn.getLightFromNeighbors(pos) > 11 - ((Integer)state.getValue(AGE)).intValue() - state.getLightOpacity()) {
         this.slightlyMelt(worldIn, pos, state, rand, true);
      } else {
         worldIn.scheduleUpdate(pos, this, MathHelper.getInt(rand, 20, 40));
      }

   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (blockIn == this) {
         int i = this.countNeighbors(worldIn, pos);
         if (i < 2) {
            this.turnIntoWater(worldIn, pos);
         }
      }

   }

   private int countNeighbors(World var1, BlockPos var2) {
      int i = 0;

      for(EnumFacing enumfacing : EnumFacing.values()) {
         if (p_185680_1_.getBlockState(p_185680_2_.offset(enumfacing)).getBlock() == this) {
            ++i;
            if (i >= 4) {
               return i;
            }
         }
      }

      return i;
   }

   protected void slightlyMelt(World var1, BlockPos var2, IBlockState var3, Random var4, boolean var5) {
      int i = ((Integer)p_185681_3_.getValue(AGE)).intValue();
      if (i < 3) {
         p_185681_1_.setBlockState(p_185681_2_, p_185681_3_.withProperty(AGE, Integer.valueOf(i + 1)), 2);
         p_185681_1_.scheduleUpdate(p_185681_2_, this, MathHelper.getInt(p_185681_4_, 20, 40));
      } else {
         this.turnIntoWater(p_185681_1_, p_185681_2_);
         if (p_185681_5_) {
            for(EnumFacing enumfacing : EnumFacing.values()) {
               BlockPos blockpos = p_185681_2_.offset(enumfacing);
               IBlockState iblockstate = p_185681_1_.getBlockState(blockpos);
               if (iblockstate.getBlock() == this) {
                  this.slightlyMelt(p_185681_1_, blockpos, iblockstate, p_185681_4_, false);
               }
            }
         }
      }

   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{AGE});
   }

   @Nullable
   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return null;
   }
}
