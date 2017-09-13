package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockStaticLiquid extends BlockLiquid {
   protected BlockStaticLiquid(Material var1) {
      super(materialIn);
      this.setTickRandomly(false);
      if (materialIn == Material.LAVA) {
         this.setTickRandomly(true);
      }

   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!this.checkForMixing(worldIn, pos, state)) {
         this.updateLiquid(worldIn, pos, state);
      }

   }

   private void updateLiquid(World var1, BlockPos var2, IBlockState var3) {
      BlockDynamicLiquid blockdynamicliquid = getFlowingBlock(this.blockMaterial);
      worldIn.setBlockState(pos, blockdynamicliquid.getDefaultState().withProperty(LEVEL, state.getValue(LEVEL)), 2);
      worldIn.scheduleUpdate(pos, blockdynamicliquid, this.tickRate(worldIn));
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (this.blockMaterial == Material.LAVA && worldIn.getGameRules().getBoolean("doFireTick")) {
         int i = rand.nextInt(3);
         if (i > 0) {
            BlockPos blockpos = pos;

            for(int j = 0; j < i; ++j) {
               blockpos = blockpos.add(rand.nextInt(3) - 1, 1, rand.nextInt(3) - 1);
               if (blockpos.getY() >= 0 && blockpos.getY() < worldIn.getHeight() && !worldIn.isBlockLoaded(blockpos)) {
                  return;
               }

               Block block = worldIn.getBlockState(blockpos).getBlock();
               if (block.blockMaterial == Material.AIR) {
                  if (this.isSurroundingBlockFlammable(worldIn, blockpos)) {
                     worldIn.setBlockState(blockpos, Blocks.FIRE.getDefaultState());
                     return;
                  }
               } else if (block.blockMaterial.blocksMovement()) {
                  return;
               }
            }
         } else {
            for(int k = 0; k < 3; ++k) {
               BlockPos blockpos1 = pos.add(rand.nextInt(3) - 1, 0, rand.nextInt(3) - 1);
               if (blockpos1.getY() >= 0 && blockpos1.getY() < 256 && !worldIn.isBlockLoaded(blockpos1)) {
                  return;
               }

               if (worldIn.isAirBlock(blockpos1.up()) && this.getCanBlockBurn(worldIn, blockpos1)) {
                  worldIn.setBlockState(blockpos1.up(), Blocks.FIRE.getDefaultState());
               }
            }
         }
      }

   }

   protected boolean isSurroundingBlockFlammable(World var1, BlockPos var2) {
      for(EnumFacing enumfacing : EnumFacing.values()) {
         if (this.getCanBlockBurn(worldIn, pos.offset(enumfacing))) {
            return true;
         }
      }

      return false;
   }

   private boolean getCanBlockBurn(World var1, BlockPos var2) {
      return pos.getY() >= 0 && pos.getY() < 256 && !worldIn.isBlockLoaded(pos) ? false : worldIn.getBlockState(pos).getMaterial().getCanBurn();
   }
}
