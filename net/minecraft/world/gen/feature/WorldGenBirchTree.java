package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenBirchTree extends WorldGenAbstractTree {
   private static final IBlockState LOG = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.BIRCH);
   private static final IBlockState LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.BIRCH).withProperty(BlockOldLeaf.CHECK_DECAY, Boolean.valueOf(false));
   private final boolean useExtraRandomHeight;

   public WorldGenBirchTree(boolean var1, boolean var2) {
      super(var1);
      this.useExtraRandomHeight = var2;
   }

   public boolean generate(World var1, Random var2, BlockPos var3) {
      int var4 = var2.nextInt(3) + 5;
      if (this.useExtraRandomHeight) {
         var4 += var2.nextInt(7);
      }

      boolean var5 = true;
      if (var3.getY() >= 1 && var3.getY() + var4 + 1 <= 256) {
         for(int var6 = var3.getY(); var6 <= var3.getY() + 1 + var4; ++var6) {
            byte var7 = 1;
            if (var6 == var3.getY()) {
               var7 = 0;
            }

            if (var6 >= var3.getY() + 1 + var4 - 2) {
               var7 = 2;
            }

            BlockPos.MutableBlockPos var8 = new BlockPos.MutableBlockPos();

            for(int var9 = var3.getX() - var7; var9 <= var3.getX() + var7 && var5; ++var9) {
               for(int var10 = var3.getZ() - var7; var10 <= var3.getZ() + var7 && var5; ++var10) {
                  if (var6 >= 0 && var6 < var1.getHeight()) {
                     if (!this.isReplaceable(var1, var8.setPos(var9, var6, var10))) {
                        var5 = false;
                     }
                  } else {
                     var5 = false;
                  }
               }
            }
         }

         if (!var5) {
            return false;
         } else {
            BlockPos var18 = var3.down();
            IBlockState var19 = var1.getBlockState(var18);
            boolean var20 = var19.getBlock().canSustainPlant(var19, var1, var18, EnumFacing.UP, (BlockSapling)Blocks.SAPLING);
            if (var20 && var3.getY() < var1.getHeight() - var4 - 1) {
               var19.getBlock().onPlantGrow(var19, var1, var18, var3);

               for(int var21 = var3.getY() - 3 + var4; var21 <= var3.getY() + var4; ++var21) {
                  int var23 = var21 - (var3.getY() + var4);
                  int var11 = 1 - var23 / 2;

                  for(int var12 = var3.getX() - var11; var12 <= var3.getX() + var11; ++var12) {
                     int var13 = var12 - var3.getX();

                     for(int var14 = var3.getZ() - var11; var14 <= var3.getZ() + var11; ++var14) {
                        int var15 = var14 - var3.getZ();
                        if (Math.abs(var13) != var11 || Math.abs(var15) != var11 || var2.nextInt(2) != 0 && var23 != 0) {
                           BlockPos var16 = new BlockPos(var12, var21, var14);
                           IBlockState var17 = var1.getBlockState(var16);
                           if (var17.getBlock().isAir(var17, var1, var16) || var17.getBlock().isAir(var17, var1, var16)) {
                              this.setBlockAndNotifyAdequately(var1, var16, LEAF);
                           }
                        }
                     }
                  }
               }

               for(int var22 = 0; var22 < var4; ++var22) {
                  BlockPos var24 = var3.up(var22);
                  IBlockState var25 = var1.getBlockState(var24);
                  if (var25.getBlock().isAir(var25, var1, var24) || var25.getBlock().isLeaves(var25, var1, var24)) {
                     this.setBlockAndNotifyAdequately(var1, var3.up(var22), LOG);
                  }
               }

               return true;
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }
}
