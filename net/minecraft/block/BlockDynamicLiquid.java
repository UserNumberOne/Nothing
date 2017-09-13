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
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.block.CraftBlock;
import org.bukkit.event.block.BlockFromToEvent;

public class BlockDynamicLiquid extends BlockLiquid {
   int adjacentSourceBlocks;

   protected BlockDynamicLiquid(Material var1) {
      super(var1);
   }

   private void placeStaticBlock(World var1, BlockPos var2, IBlockState var3) {
      var1.setBlockState(var2, getStaticBlock(this.blockMaterial).getDefaultState().withProperty(LEVEL, (Integer)var3.getValue(LEVEL)), 2);
   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      CraftWorld var5 = var1.getWorld();
      CraftServer var6 = var1.getServer();
      org.bukkit.block.Block var7 = var5 == null ? null : var5.getBlockAt(var2.getX(), var2.getY(), var2.getZ());
      int var8 = ((Integer)var3.getValue(LEVEL)).intValue();
      byte var9 = 1;
      if (this.blockMaterial == Material.LAVA && !var1.provider.doesWaterVaporize()) {
         var9 = 2;
      }

      int var10 = this.tickRate(var1);
      if (var8 > 0) {
         int var11 = -100;
         this.adjacentSourceBlocks = 0;

         for(EnumFacing var13 : EnumFacing.Plane.HORIZONTAL) {
            var11 = this.checkAdjacentBlock(var1, var2.offset(var13), var11);
         }

         int var19 = var11 + var9;
         if (var19 >= 8 || var11 < 0) {
            var19 = -1;
         }

         int var14 = this.getDepth(var1.getBlockState(var2.up()));
         if (var14 >= 0) {
            if (var14 >= 8) {
               var19 = var14;
            } else {
               var19 = var14 + 8;
            }
         }

         if (this.adjacentSourceBlocks >= 2 && this.blockMaterial == Material.WATER) {
            IBlockState var15 = var1.getBlockState(var2.down());
            if (var15.getMaterial().isSolid()) {
               var19 = 0;
            } else if (var15.getMaterial() == this.blockMaterial && ((Integer)var15.getValue(LEVEL)).intValue() == 0) {
               var19 = 0;
            }
         }

         if (this.blockMaterial == Material.LAVA && var8 < 8 && var19 < 8 && var19 > var8 && var4.nextInt(4) != 0) {
            var10 *= 4;
         }

         if (var19 == var8) {
            this.placeStaticBlock(var1, var2, var3);
         } else {
            var8 = var19;
            if (var19 < 0) {
               var1.setBlockToAir(var2);
            } else {
               var3 = var3.withProperty(LEVEL, Integer.valueOf(var19));
               var1.setBlockState(var2, var3, 2);
               var1.scheduleUpdate(var2, this, var10);
               var1.notifyNeighborsOfStateChange(var2, this);
            }
         }
      } else {
         this.placeStaticBlock(var1, var2, var3);
      }

      IBlockState var18 = var1.getBlockState(var2.down());
      if (this.canFlowInto(var1, var2.down(), var18)) {
         BlockFromToEvent var21 = new BlockFromToEvent(var7, BlockFace.DOWN);
         if (var6 != null) {
            var6.getPluginManager().callEvent(var21);
         }

         if (!var21.isCancelled()) {
            if (this.blockMaterial == Material.LAVA && var1.getBlockState(var2.down()).getMaterial() == Material.WATER) {
               var1.setBlockState(var2.down(), Blocks.STONE.getDefaultState());
               this.triggerMixEffects(var1, var2.down());
               return;
            }

            if (var8 >= 8) {
               this.tryFlowInto(var1, var2.down(), var18, var8);
            } else {
               this.tryFlowInto(var1, var2.down(), var18, var8 + 8);
            }
         }
      } else if (var8 >= 0 && (var8 == 0 || this.isBlocked(var1, var2.down(), var18))) {
         Set var22 = this.getPossibleFlowDirections(var1, var2);
         int var23 = var8 + var9;
         if (var8 >= 8) {
            var23 = 1;
         }

         if (var23 >= 8) {
            return;
         }

         for(EnumFacing var24 : var22) {
            BlockFromToEvent var16 = new BlockFromToEvent(var7, CraftBlock.notchToBlockFace(var24));
            if (var6 != null) {
               var6.getPluginManager().callEvent(var16);
            }

            if (!var16.isCancelled()) {
               this.tryFlowInto(var1, var2.offset(var24), var1.getBlockState(var2.offset(var24)), var23);
            }
         }
      }

   }

   private void tryFlowInto(World var1, BlockPos var2, IBlockState var3, int var4) {
      if (var1.isBlockLoaded(var2) && this.canFlowInto(var1, var2, var3)) {
         if (var3.getMaterial() != Material.AIR) {
            if (this.blockMaterial == Material.LAVA) {
               this.triggerMixEffects(var1, var2);
            } else {
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
