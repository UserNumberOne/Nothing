package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenShrub extends WorldGenTrees {
   private final IBlockState leavesMetadata;
   private final IBlockState woodMetadata;

   public WorldGenShrub(IBlockState var1, IBlockState var2) {
      super(false);
      this.woodMetadata = p_i46450_1_;
      this.leavesMetadata = p_i46450_2_;
   }

   public boolean generate(World var1, Random var2, BlockPos var3) {
      for(IBlockState iblockstate = worldIn.getBlockState(position); (iblockstate.getBlock().isAir(iblockstate, worldIn, position) || iblockstate.getBlock().isLeaves(iblockstate, worldIn, position)) && position.getY() > 0; iblockstate = worldIn.getBlockState(position)) {
         position = position.down();
      }

      IBlockState state = worldIn.getBlockState(position);
      if (state.getBlock().canSustainPlant(state, worldIn, position, EnumFacing.UP, (BlockSapling)Blocks.SAPLING)) {
         position = position.up();
         this.setBlockAndNotifyAdequately(worldIn, position, this.woodMetadata);

         for(int i = position.getY(); i <= position.getY() + 2; ++i) {
            int j = i - position.getY();
            int k = 2 - j;

            for(int l = position.getX() - k; l <= position.getX() + k; ++l) {
               int i1 = l - position.getX();

               for(int j1 = position.getZ() - k; j1 <= position.getZ() + k; ++j1) {
                  int k1 = j1 - position.getZ();
                  if (Math.abs(i1) != k || Math.abs(k1) != k || rand.nextInt(2) != 0) {
                     BlockPos blockpos = new BlockPos(l, i, j1);
                     state = worldIn.getBlockState(blockpos);
                     if (state.getBlock().canBeReplacedByLeaves(state, worldIn, blockpos)) {
                        this.setBlockAndNotifyAdequately(worldIn, blockpos, this.leavesMetadata);
                     }
                  }
               }
            }
         }
      }

      return true;
   }
}
