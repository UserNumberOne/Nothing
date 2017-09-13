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
      super(var1);
      this.setTickRandomly(false);
      if (var1 == Material.LAVA) {
         this.setTickRandomly(true);
      }

   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!this.checkForMixing(var2, var3, var1)) {
         this.updateLiquid(var2, var3, var1);
      }

   }

   private void updateLiquid(World var1, BlockPos var2, IBlockState var3) {
      BlockDynamicLiquid var4 = getFlowingBlock(this.blockMaterial);
      var1.setBlockState(var2, var4.getDefaultState().withProperty(LEVEL, var3.getValue(LEVEL)), 2);
      var1.scheduleUpdate(var2, var4, this.tickRate(var1));
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (this.blockMaterial == Material.LAVA && var1.getGameRules().getBoolean("doFireTick")) {
         int var5 = var4.nextInt(3);
         if (var5 > 0) {
            BlockPos var6 = var2;

            for(int var7 = 0; var7 < var5; ++var7) {
               var6 = var6.add(var4.nextInt(3) - 1, 1, var4.nextInt(3) - 1);
               if (var6.getY() >= 0 && var6.getY() < var1.getHeight() && !var1.isBlockLoaded(var6)) {
                  return;
               }

               Block var8 = var1.getBlockState(var6).getBlock();
               if (var8.blockMaterial == Material.AIR) {
                  if (this.isSurroundingBlockFlammable(var1, var6)) {
                     var1.setBlockState(var6, Blocks.FIRE.getDefaultState());
                     return;
                  }
               } else if (var8.blockMaterial.blocksMovement()) {
                  return;
               }
            }
         } else {
            for(int var9 = 0; var9 < 3; ++var9) {
               BlockPos var10 = var2.add(var4.nextInt(3) - 1, 0, var4.nextInt(3) - 1);
               if (var10.getY() >= 0 && var10.getY() < 256 && !var1.isBlockLoaded(var10)) {
                  return;
               }

               if (var1.isAirBlock(var10.up()) && this.getCanBlockBurn(var1, var10)) {
                  var1.setBlockState(var10.up(), Blocks.FIRE.getDefaultState());
               }
            }
         }
      }

   }

   protected boolean isSurroundingBlockFlammable(World var1, BlockPos var2) {
      for(EnumFacing var6 : EnumFacing.values()) {
         if (this.getCanBlockBurn(var1, var2.offset(var6))) {
            return true;
         }
      }

      return false;
   }

   private boolean getCanBlockBurn(World var1, BlockPos var2) {
      return var2.getY() >= 0 && var2.getY() < 256 && !var1.isBlockLoaded(var2) ? false : var1.getBlockState(var2).getMaterial().getCanBurn();
   }
}
