package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class WorldGenAbstractTree extends WorldGenerator {
   public WorldGenAbstractTree(boolean var1) {
      super(notify);
   }

   protected boolean canGrowInto(Block var1) {
      Material material = blockType.getDefaultState().getMaterial();
      return material == Material.AIR || material == Material.LEAVES || blockType == Blocks.GRASS || blockType == Blocks.DIRT || blockType == Blocks.LOG || blockType == Blocks.LOG2 || blockType == Blocks.SAPLING || blockType == Blocks.VINE;
   }

   public void generateSaplings(World var1, Random var2, BlockPos var3) {
   }

   protected void setDirtAt(World var1, BlockPos var2) {
      if (worldIn.getBlockState(pos).getBlock() != Blocks.DIRT) {
         this.setBlockAndNotifyAdequately(worldIn, pos, Blocks.DIRT.getDefaultState());
      }

   }

   public boolean isReplaceable(World var1, BlockPos var2) {
      IBlockState state = world.getBlockState(pos);
      return state.getBlock().isAir(state, world, pos) || state.getBlock().isLeaves(state, world, pos) || state.getBlock().isWood(world, pos) || this.canGrowInto(state.getBlock());
   }
}
