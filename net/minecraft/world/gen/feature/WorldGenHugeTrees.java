package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class WorldGenHugeTrees extends WorldGenAbstractTree {
   protected final int baseHeight;
   protected final IBlockState woodMetadata;
   protected final IBlockState leavesMetadata;
   protected int extraRandomHeight;

   public WorldGenHugeTrees(boolean var1, int var2, int var3, IBlockState var4, IBlockState var5) {
      super(var1);
      this.baseHeight = var2;
      this.extraRandomHeight = var3;
      this.woodMetadata = var4;
      this.leavesMetadata = var5;
   }

   protected int getHeight(Random var1) {
      int var2 = var1.nextInt(3) + this.baseHeight;
      if (this.extraRandomHeight > 1) {
         var2 += var1.nextInt(this.extraRandomHeight);
      }

      return var2;
   }

   private boolean isSpaceAt(World var1, BlockPos var2, int var3) {
      boolean var4 = true;
      if (var2.getY() >= 1 && var2.getY() + var3 + 1 <= 256) {
         for(int var5 = 0; var5 <= 1 + var3; ++var5) {
            byte var6 = 2;
            if (var5 == 0) {
               var6 = 1;
            } else if (var5 >= 1 + var3 - 2) {
               var6 = 2;
            }

            for(int var7 = -var6; var7 <= var6 && var4; ++var7) {
               for(int var8 = -var6; var8 <= var6 && var4; ++var8) {
                  if (var2.getY() + var5 < 0 || var2.getY() + var5 >= 256 || !this.canGrowInto(var1.getBlockState(var2.add(var7, var5, var8)).getBlock()) && var1.getBlockState(var2.add(var7, var5, var8)).getBlock() != Blocks.SAPLING) {
                     var4 = false;
                  }
               }
            }
         }

         return var4;
      } else {
         return false;
      }
   }

   private boolean ensureDirtsUnderneath(BlockPos var1, World var2) {
      BlockPos var3 = var1.down();
      Block var4 = var2.getBlockState(var3).getBlock();
      if ((var4 == Blocks.GRASS || var4 == Blocks.DIRT) && var1.getY() >= 2) {
         this.setDirtAt(var2, var3);
         this.setDirtAt(var2, var3.east());
         this.setDirtAt(var2, var3.south());
         this.setDirtAt(var2, var3.south().east());
         return true;
      } else {
         return false;
      }
   }

   protected boolean ensureGrowable(World var1, Random var2, BlockPos var3, int var4) {
      return this.isSpaceAt(var1, var3, var4) && this.ensureDirtsUnderneath(var3, var1);
   }

   protected void growLeavesLayerStrict(World var1, BlockPos var2, int var3) {
      int var4 = var3 * var3;

      for(int var5 = -var3; var5 <= var3 + 1; ++var5) {
         for(int var6 = -var3; var6 <= var3 + 1; ++var6) {
            int var7 = var5 - 1;
            int var8 = var6 - 1;
            if (var5 * var5 + var6 * var6 <= var4 || var7 * var7 + var8 * var8 <= var4 || var5 * var5 + var8 * var8 <= var4 || var7 * var7 + var6 * var6 <= var4) {
               BlockPos var9 = var2.add(var5, 0, var6);
               Material var10 = var1.getBlockState(var9).getMaterial();
               if (var10 == Material.AIR || var10 == Material.LEAVES) {
                  this.setBlockAndNotifyAdequately(var1, var9, this.leavesMetadata);
               }
            }
         }
      }

   }

   protected void growLeavesLayer(World var1, BlockPos var2, int var3) {
      int var4 = var3 * var3;

      for(int var5 = -var3; var5 <= var3; ++var5) {
         for(int var6 = -var3; var6 <= var3; ++var6) {
            if (var5 * var5 + var6 * var6 <= var4) {
               BlockPos var7 = var2.add(var5, 0, var6);
               Material var8 = var1.getBlockState(var7).getMaterial();
               if (var8 == Material.AIR || var8 == Material.LEAVES) {
                  this.setBlockAndNotifyAdequately(var1, var7, this.leavesMetadata);
               }
            }
         }
      }

   }
}
