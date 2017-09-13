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
      var1.setBlockState(var2, var4.getDefaultState().withProperty(LEVEL, (Integer)var3.getValue(LEVEL)), 2);
      var1.scheduleUpdate(var2, var4, this.tickRate(var1));
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (this.blockMaterial == Material.LAVA && var1.getGameRules().getBoolean("doFireTick")) {
         int var5 = var4.nextInt(3);
         if (var5 > 0) {
            BlockPos var9 = var2;

            for(int var10 = 0; var10 < var5; ++var10) {
               var9 = var9.add(var4.nextInt(3) - 1, 1, var4.nextInt(3) - 1);
               if (var9.getY() >= 0 && var9.getY() < 256 && !var1.isBlockLoaded(var9)) {
                  return;
               }

               Block var11 = var1.getBlockState(var9).getBlock();
               if (var11.blockMaterial == Material.AIR) {
                  if (this.isSurroundingBlockFlammable(var1, var9) && (var1.getBlockState(var9) == Blocks.FIRE || !CraftEventFactory.callBlockIgniteEvent(var1, var9.getX(), var9.getY(), var9.getZ(), var2.getX(), var2.getY(), var2.getZ()).isCancelled())) {
                     var1.setBlockState(var9, Blocks.FIRE.getDefaultState());
                     return;
                  }
               } else if (var11.blockMaterial.blocksMovement()) {
                  return;
               }
            }
         } else {
            for(int var6 = 0; var6 < 3; ++var6) {
               BlockPos var7 = var2.add(var4.nextInt(3) - 1, 0, var4.nextInt(3) - 1);
               if (var7.getY() >= 0 && var7.getY() < 256 && !var1.isBlockLoaded(var7)) {
                  return;
               }

               if (var1.isAirBlock(var7.up()) && this.getCanBlockBurn(var1, var7)) {
                  BlockPos var8 = var7.up();
                  if (var1.getBlockState(var8) == Blocks.FIRE || !CraftEventFactory.callBlockIgniteEvent(var1, var8.getX(), var8.getY(), var8.getZ(), var2.getX(), var2.getY(), var2.getZ()).isCancelled()) {
                     var1.setBlockState(var7.up(), Blocks.FIRE.getDefaultState());
                  }
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
