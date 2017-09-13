package net.minecraft.block;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

public class BlockDynamicLiquid extends BlockLiquid {
   int adjacentSourceBlocks;

   protected BlockDynamicLiquid(Material var1) {
      super(var1);
   }

   private void placeStaticBlock(World var1, BlockPos var2, IBlockState var3) {
      var1.setBlockState(var2, getStaticBlock(this.blockMaterial).getDefaultState().withProperty(LEVEL, var3.getValue(LEVEL)), 2);
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      int var5 = ((Integer)var3.getValue(LEVEL)).intValue();
      byte var6 = 1;
      if (this.blockMaterial == Material.LAVA && !var1.provider.doesWaterVaporize()) {
         var6 = 2;
      }

      int var7 = this.tickRate(var1);
      if (var5 > 0) {
         int var8 = -100;
         this.adjacentSourceBlocks = 0;

         for(EnumFacing var10 : EnumFacing.Plane.HORIZONTAL) {
            var8 = this.checkAdjacentBlock(var1, var2.offset(var10), var8);
         }

         int var15 = var8 + var6;
         if (var15 >= 8 || var8 < 0) {
            var15 = -1;
         }

         int var17 = this.getDepth(var1.getBlockState(var2.up()));
         if (var17 >= 0) {
            if (var17 >= 8) {
               var15 = var17;
            } else {
               var15 = var17 + 8;
            }
         }

         if (this.adjacentSourceBlocks >= 2 && ForgeEventFactory.canCreateFluidSource(var1, var2, var3, this.blockMaterial == Material.WATER)) {
            IBlockState var11 = var1.getBlockState(var2.down());
            if (var11.getMaterial().isSolid()) {
               var15 = 0;
            } else if (var11.getMaterial() == this.blockMaterial && ((Integer)var11.getValue(LEVEL)).intValue() == 0) {
               var15 = 0;
            }
         }

         if (this.blockMaterial == Material.LAVA && var5 < 8 && var15 < 8 && var15 > var5 && var4.nextInt(4) != 0) {
            var7 *= 4;
         }

         if (var15 == var5) {
            this.placeStaticBlock(var1, var2, var3);
         } else {
            var5 = var15;
            if (var15 < 0) {
               var1.setBlockToAir(var2);
            } else {
               var3 = var3.withProperty(LEVEL, Integer.valueOf(var15));
               var1.setBlockState(var2, var3, 2);
               var1.scheduleUpdate(var2, this, var7);
               var1.notifyNeighborsOfStateChange(var2, this);
            }
         }
      } else {
         this.placeStaticBlock(var1, var2, var3);
      }

      IBlockState var14 = var1.getBlockState(var2.down());
      if (this.canFlowInto(var1, var2.down(), var14)) {
         if (this.blockMaterial == Material.LAVA && var1.getBlockState(var2.down()).getMaterial() == Material.WATER) {
            var1.setBlockState(var2.down(), Blocks.STONE.getDefaultState());
            this.triggerMixEffects(var1, var2.down());
            return;
         }

         if (var5 >= 8) {
            this.tryFlowInto(var1, var2.down(), var14, var5);
         } else {
            this.tryFlowInto(var1, var2.down(), var14, var5 + 8);
         }
      } else if (var5 >= 0 && (var5 == 0 || this.isBlocked(var1, var2.down(), var14))) {
         Set var16 = this.getPossibleFlowDirections(var1, var2);
         int var18 = var5 + var6;
         if (var5 >= 8) {
            var18 = 1;
         }

         if (var18 >= 8) {
            return;
         }

         for(EnumFacing var12 : var16) {
            this.tryFlowInto(var1, var2.offset(var12), var1.getBlockState(var2.offset(var12)), var18);
         }
      }

   }

   private void tryFlowInto(World var1, BlockPos var2, IBlockState var3, int var4) {
      if (this.canFlowInto(var1, var2, var3)) {
         if (var3.getMaterial() != Material.AIR) {
            if (this.blockMaterial == Material.LAVA) {
               this.triggerMixEffects(var1, var2);
            } else if (var3.getBlock() != Blocks.SNOW_LAYER) {
               var3.getBlock().dropBlockAsItem(var1, var2, var3, 0);
            }
         }

         var1.setBlockState(var2, this.getDefaultState().withProperty(LEVEL, Integer.valueOf(var4)), 3);
      }

   }

   private int getSlopeDistance(World var1, BlockPos var2, int var3, EnumFacing var4) {
      int var5 = 1000;

      for(EnumFacing var7 : EnumFacing.Plane.HORIZONTAL) {
         if (var7 != var4) {
            BlockPos var8 = var2.offset(var7);
            IBlockState var9 = var1.getBlockState(var8);
            if (!this.isBlocked(var1, var8, var9) && (var9.getMaterial() != this.blockMaterial || ((Integer)var9.getValue(LEVEL)).intValue() > 0)) {
               if (!this.isBlocked(var1, var8.down(), var9)) {
                  return var3;
               }

               if (var3 < this.getSlopeFindDistance(var1)) {
                  int var10 = this.getSlopeDistance(var1, var8, var3 + 1, var7.getOpposite());
                  if (var10 < var5) {
                     var5 = var10;
                  }
               }
            }
         }
      }

      return var5;
   }

   private int getSlopeFindDistance(World var1) {
      return this.blockMaterial == Material.LAVA && !var1.provider.doesWaterVaporize() ? 2 : 4;
   }

   private Set getPossibleFlowDirections(World var1, BlockPos var2) {
      int var3 = 1000;
      EnumSet var4 = EnumSet.noneOf(EnumFacing.class);

      for(EnumFacing var6 : EnumFacing.Plane.HORIZONTAL) {
         BlockPos var7 = var2.offset(var6);
         IBlockState var8 = var1.getBlockState(var7);
         if (!this.isBlocked(var1, var7, var8) && (var8.getMaterial() != this.blockMaterial || ((Integer)var8.getValue(LEVEL)).intValue() > 0)) {
            int var9;
            if (this.isBlocked(var1, var7.down(), var1.getBlockState(var7.down()))) {
               var9 = this.getSlopeDistance(var1, var7, 1, var6.getOpposite());
            } else {
               var9 = 0;
            }

            if (var9 < var3) {
               var4.clear();
            }

            if (var9 <= var3) {
               var4.add(var6);
               var3 = var9;
            }
         }
      }

      return var4;
   }

   private boolean isBlocked(World var1, BlockPos var2, IBlockState var3) {
      Block var4 = var1.getBlockState(var2).getBlock();
      return !(var4 instanceof BlockDoor) && var4 != Blocks.STANDING_SIGN && var4 != Blocks.LADDER && var4 != Blocks.REEDS ? (var4.blockMaterial != Material.PORTAL && var4.blockMaterial != Material.STRUCTURE_VOID ? var4.blockMaterial.blocksMovement() : true) : true;
   }

   protected int checkAdjacentBlock(World var1, BlockPos var2, int var3) {
      int var4 = this.getDepth(var1.getBlockState(var2));
      if (var4 < 0) {
         return var3;
      } else {
         if (var4 == 0) {
            ++this.adjacentSourceBlocks;
         }

         if (var4 >= 8) {
            var4 = 0;
         }

         return var3 >= 0 && var4 >= var3 ? var3 : var4;
      }
   }

   private boolean canFlowInto(World var1, BlockPos var2, IBlockState var3) {
      Material var4 = var3.getMaterial();
      return var4 != this.blockMaterial && var4 != Material.LAVA && !this.isBlocked(var1, var2, var3);
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      if (!this.checkForMixing(var1, var2, var3)) {
         var1.scheduleUpdate(var2, this, this.tickRate(var1));
      }

   }
}
