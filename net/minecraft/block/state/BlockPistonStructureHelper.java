package net.minecraft.block.state;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockPistonStructureHelper {
   private final World world;
   private final BlockPos pistonPos;
   private final BlockPos blockToMove;
   private final EnumFacing moveDirection;
   private final List toMove = Lists.newArrayList();
   private final List toDestroy = Lists.newArrayList();

   public BlockPistonStructureHelper(World var1, BlockPos var2, EnumFacing var3, boolean var4) {
      this.world = var1;
      this.pistonPos = var2;
      if (var4) {
         this.moveDirection = var3;
         this.blockToMove = var2.offset(var3);
      } else {
         this.moveDirection = var3.getOpposite();
         this.blockToMove = var2.offset(var3, 2);
      }

   }

   public boolean canMove() {
      this.toMove.clear();
      this.toDestroy.clear();
      IBlockState var1 = this.world.getBlockState(this.blockToMove);
      if (!BlockPistonBase.canPush(var1, this.world, this.blockToMove, this.moveDirection, false)) {
         if (var1.getMobilityFlag() == EnumPushReaction.DESTROY) {
            this.toDestroy.add(this.blockToMove);
            return true;
         } else {
            return false;
         }
      } else if (!this.addBlockLine(this.blockToMove)) {
         return false;
      } else {
         for(int var2 = 0; var2 < this.toMove.size(); ++var2) {
            BlockPos var3 = (BlockPos)this.toMove.get(var2);
            if (this.world.getBlockState(var3).getBlock() == Blocks.SLIME_BLOCK && !this.addBranchingBlocks(var3)) {
               return false;
            }
         }

         return true;
      }
   }

   private boolean addBlockLine(BlockPos var1) {
      IBlockState var2 = this.world.getBlockState(var1);
      Block var3 = var2.getBlock();
      if (var2.getBlock().isAir(var2, this.world, var1)) {
         return true;
      } else if (!BlockPistonBase.canPush(var2, this.world, var1, this.moveDirection, false)) {
         return true;
      } else if (var1.equals(this.pistonPos)) {
         return true;
      } else if (this.toMove.contains(var1)) {
         return true;
      } else {
         int var4 = 1;
         if (var4 + this.toMove.size() > 12) {
            return false;
         } else {
            while(var3 == Blocks.SLIME_BLOCK) {
               BlockPos var5 = var1.offset(this.moveDirection.getOpposite(), var4);
               var2 = this.world.getBlockState(var5);
               var3 = var2.getBlock();
               if (var2.getBlock().isAir(var2, this.world, var5) || !BlockPistonBase.canPush(var2, this.world, var5, this.moveDirection, false) || var5.equals(this.pistonPos)) {
                  break;
               }

               ++var4;
               if (var4 + this.toMove.size() > 12) {
                  return false;
               }
            }

            int var13 = 0;

            for(int var6 = var4 - 1; var6 >= 0; --var6) {
               this.toMove.add(var1.offset(this.moveDirection.getOpposite(), var6));
               ++var13;
            }

            int var14 = 1;

            while(true) {
               BlockPos var7 = var1.offset(this.moveDirection, var14);
               int var8 = this.toMove.indexOf(var7);
               if (var8 > -1) {
                  this.reorderListAtCollision(var13, var8);

                  for(int var9 = 0; var9 <= var8 + var13; ++var9) {
                     BlockPos var10 = (BlockPos)this.toMove.get(var9);
                     if (this.world.getBlockState(var10).getBlock() == Blocks.SLIME_BLOCK && !this.addBranchingBlocks(var10)) {
                        return false;
                     }
                  }

                  return true;
               }

               var2 = this.world.getBlockState(var7);
               if (var2.getBlock().isAir(var2, this.world, var7)) {
                  return true;
               }

               if (!BlockPistonBase.canPush(var2, this.world, var7, this.moveDirection, true) || var7.equals(this.pistonPos)) {
                  return false;
               }

               if (var2.getMobilityFlag() == EnumPushReaction.DESTROY) {
                  this.toDestroy.add(var7);
                  return true;
               }

               if (this.toMove.size() >= 12) {
                  return false;
               }

               this.toMove.add(var7);
               ++var13;
               ++var14;
            }
         }
      }
   }

   private void reorderListAtCollision(int var1, int var2) {
      ArrayList var3 = Lists.newArrayList();
      ArrayList var4 = Lists.newArrayList();
      ArrayList var5 = Lists.newArrayList();
      var3.addAll(this.toMove.subList(0, var2));
      var4.addAll(this.toMove.subList(this.toMove.size() - var1, this.toMove.size()));
      var5.addAll(this.toMove.subList(var2, this.toMove.size() - var1));
      this.toMove.clear();
      this.toMove.addAll(var3);
      this.toMove.addAll(var4);
      this.toMove.addAll(var5);
   }

   private boolean addBranchingBlocks(BlockPos var1) {
      for(EnumFacing var5 : EnumFacing.values()) {
         if (var5.getAxis() != this.moveDirection.getAxis() && !this.addBlockLine(var1.offset(var5))) {
            return false;
         }
      }

      return true;
   }

   public List getBlocksToMove() {
      return this.toMove;
   }

   public List getBlocksToDestroy() {
      return this.toDestroy;
   }
}
