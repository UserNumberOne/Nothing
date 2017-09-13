package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;

public class BlockStaticLiquid extends BlockLiquid {
   protected BlockStaticLiquid(Material material) {
      super(material);
      this.setTickRandomly(false);
      if (material == Material.LAVA) {
         this.setTickRandomly(true);
      }

   }

   public void neighborChanged(IBlockState iblockdata, World world, BlockPos blockposition, Block block) {
      if (!this.checkForMixing(world, blockposition, iblockdata)) {
         this.updateLiquid(world, blockposition, iblockdata);
      }

   }

   private void updateLiquid(World world, BlockPos blockposition, IBlockState iblockdata) {
      BlockDynamicLiquid blockflowing = getFlowingBlock(this.blockMaterial);
      world.setBlockState(blockposition, blockflowing.getDefaultState().withProperty(LEVEL, (Integer)iblockdata.getValue(LEVEL)), 2);
      world.scheduleUpdate(blockposition, blockflowing, this.tickRate(world));
   }

   public void updateTick(World world, BlockPos blockposition, IBlockState iblockdata, Random random) {
      if (this.blockMaterial == Material.LAVA && world.getGameRules().getBoolean("doFireTick")) {
         int i = random.nextInt(3);
         if (i > 0) {
            BlockPos blockposition1 = blockposition;

            for(int j = 0; j < i; ++j) {
               blockposition1 = blockposition1.add(random.nextInt(3) - 1, 1, random.nextInt(3) - 1);
               if (blockposition1.getY() >= 0 && blockposition1.getY() < 256 && !world.isBlockLoaded(blockposition1)) {
                  return;
               }

               Block block = world.getBlockState(blockposition1).getBlock();
               if (block.blockMaterial == Material.AIR) {
                  if (this.isSurroundingBlockFlammable(world, blockposition1) && (world.getBlockState(blockposition1) == Blocks.FIRE || !CraftEventFactory.callBlockIgniteEvent(world, blockposition1.getX(), blockposition1.getY(), blockposition1.getZ(), blockposition.getX(), blockposition.getY(), blockposition.getZ()).isCancelled())) {
                     world.setBlockState(blockposition1, Blocks.FIRE.getDefaultState());
                     return;
                  }
               } else if (block.blockMaterial.blocksMovement()) {
                  return;
               }
            }
         } else {
            for(int k = 0; k < 3; ++k) {
               BlockPos blockposition2 = blockposition.add(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
               if (blockposition2.getY() >= 0 && blockposition2.getY() < 256 && !world.isBlockLoaded(blockposition2)) {
                  return;
               }

               if (world.isAirBlock(blockposition2.up()) && this.getCanBlockBurn(world, blockposition2)) {
                  BlockPos up = blockposition2.up();
                  if (world.getBlockState(up) == Blocks.FIRE || !CraftEventFactory.callBlockIgniteEvent(world, up.getX(), up.getY(), up.getZ(), blockposition.getX(), blockposition.getY(), blockposition.getZ()).isCancelled()) {
                     world.setBlockState(blockposition2.up(), Blocks.FIRE.getDefaultState());
                  }
               }
            }
         }
      }

   }

   protected boolean isSurroundingBlockFlammable(World world, BlockPos blockposition) {
      for(EnumFacing enumdirection : EnumFacing.values()) {
         if (this.getCanBlockBurn(world, blockposition.offset(enumdirection))) {
            return true;
         }
      }

      return false;
   }

   private boolean getCanBlockBurn(World world, BlockPos blockposition) {
      return blockposition.getY() >= 0 && blockposition.getY() < 256 && !world.isBlockLoaded(blockposition) ? false : world.getBlockState(blockposition).getMaterial().getCanBurn();
   }
}
