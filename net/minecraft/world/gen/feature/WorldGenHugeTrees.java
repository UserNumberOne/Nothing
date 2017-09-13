package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class WorldGenHugeTrees extends WorldGenAbstractTree {
   protected final int baseHeight;
   protected final IBlockState woodMetadata;
   protected final IBlockState leavesMetadata;
   protected int extraRandomHeight;

   public WorldGenHugeTrees(boolean var1, int var2, int var3, IBlockState var4, IBlockState var5) {
      super(notify);
      this.baseHeight = baseHeightIn;
      this.extraRandomHeight = extraRandomHeightIn;
      this.woodMetadata = woodMetadataIn;
      this.leavesMetadata = leavesMetadataIn;
   }

   protected int getHeight(Random var1) {
      int i = rand.nextInt(3) + this.baseHeight;
      if (this.extraRandomHeight > 1) {
         i += rand.nextInt(this.extraRandomHeight);
      }

      return i;
   }

   private boolean isSpaceAt(World var1, BlockPos var2, int var3) {
      boolean flag = true;
      if (leavesPos.getY() >= 1 && leavesPos.getY() + height + 1 <= 256) {
         for(int i = 0; i <= 1 + height; ++i) {
            int j = 2;
            if (i == 0) {
               j = 1;
            } else if (i >= 1 + height - 2) {
               j = 2;
            }

            for(int k = -j; k <= j && flag; ++k) {
               for(int l = -j; l <= j && flag; ++l) {
                  if (leavesPos.getY() + i < 0 || leavesPos.getY() + i >= 256 || !this.isReplaceable(worldIn, leavesPos.add(k, i, l))) {
                     flag = false;
                  }
               }
            }
         }

         return flag;
      } else {
         return false;
      }
   }

   private boolean ensureDirtsUnderneath(BlockPos var1, World var2) {
      BlockPos blockpos = pos.down();
      IBlockState state = worldIn.getBlockState(blockpos);
      boolean isSoil = state.getBlock().canSustainPlant(state, worldIn, blockpos, EnumFacing.UP, (BlockSapling)Blocks.SAPLING);
      if (isSoil && pos.getY() >= 2) {
         this.onPlantGrow(worldIn, blockpos, pos);
         this.onPlantGrow(worldIn, blockpos.east(), pos);
         this.onPlantGrow(worldIn, blockpos.south(), pos);
         this.onPlantGrow(worldIn, blockpos.south().east(), pos);
         return true;
      } else {
         return false;
      }
   }

   protected boolean ensureGrowable(World var1, Random var2, BlockPos var3, int var4) {
      return this.isSpaceAt(worldIn, treePos, p_175929_4_) && this.ensureDirtsUnderneath(treePos, worldIn);
   }

   protected void growLeavesLayerStrict(World var1, BlockPos var2, int var3) {
      int i = width * width;

      for(int j = -width; j <= width + 1; ++j) {
         for(int k = -width; k <= width + 1; ++k) {
            int l = j - 1;
            int i1 = k - 1;
            if (j * j + k * k <= i || l * l + i1 * i1 <= i || j * j + i1 * i1 <= i || l * l + k * k <= i) {
               BlockPos blockpos = layerCenter.add(j, 0, k);
               IBlockState state = worldIn.getBlockState(blockpos);
               if (state.getBlock().isAir(state, worldIn, blockpos) || state.getBlock().isLeaves(state, worldIn, blockpos)) {
                  this.setBlockAndNotifyAdequately(worldIn, blockpos, this.leavesMetadata);
               }
            }
         }
      }

   }

   protected void growLeavesLayer(World var1, BlockPos var2, int var3) {
      int i = width * width;

      for(int j = -width; j <= width; ++j) {
         for(int k = -width; k <= width; ++k) {
            if (j * j + k * k <= i) {
               BlockPos blockpos = layerCenter.add(j, 0, k);
               IBlockState state = worldIn.getBlockState(blockpos);
               if (state.getBlock().isAir(state, worldIn, blockpos) || state.getBlock().isLeaves(state, worldIn, blockpos)) {
                  this.setBlockAndNotifyAdequately(worldIn, blockpos, this.leavesMetadata);
               }
            }
         }
      }

   }

   private void onPlantGrow(World var1, BlockPos var2, BlockPos var3) {
      IBlockState state = world.getBlockState(pos);
      state.getBlock().onPlantGrow(state, world, pos, source);
   }
}
