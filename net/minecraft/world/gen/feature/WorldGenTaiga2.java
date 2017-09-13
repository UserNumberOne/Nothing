package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenTaiga2 extends WorldGenAbstractTree {
   private static final IBlockState TRUNK = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE);
   private static final IBlockState LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.SPRUCE).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));

   public WorldGenTaiga2(boolean var1) {
      super(var1);
   }

   public boolean generate(World var1, Random var2, BlockPos var3) {
      int var4 = var2.nextInt(4) + 6;
      int var5 = 1 + var2.nextInt(2);
      int var6 = var4 - var5;
      int var7 = 2 + var2.nextInt(2);
      boolean var8 = true;
      if (var3.getY() >= 1 && var3.getY() + var4 + 1 <= var1.getHeight()) {
         for(int var9 = var3.getY(); var9 <= var3.getY() + 1 + var4 && var8; ++var9) {
            int var10;
            if (var9 - var3.getY() < var5) {
               var10 = 0;
            } else {
               var10 = var7;
            }

            BlockPos.MutableBlockPos var11 = new BlockPos.MutableBlockPos();

            for(int var12 = var3.getX() - var10; var12 <= var3.getX() + var10 && var8; ++var12) {
               for(int var13 = var3.getZ() - var10; var13 <= var3.getZ() + var10 && var8; ++var13) {
                  if (var9 >= 0 && var9 < var1.getHeight()) {
                     IBlockState var14 = var1.getBlockState(var11.setPos(var12, var9, var13));
                     if (!var14.getBlock().isAir(var14, var1, var11.setPos(var12, var9, var13)) && !var14.getBlock().isLeaves(var14, var1, var11.setPos(var12, var9, var13))) {
                        var8 = false;
                     }
                  } else {
                     var8 = false;
                  }
               }
            }
         }

         if (!var8) {
            return false;
         } else {
            BlockPos var21 = var3.down();
            IBlockState var22 = var1.getBlockState(var21);
            if (var22.getBlock().canSustainPlant(var22, var1, var21, EnumFacing.UP, (BlockSapling)Blocks.SAPLING) && var3.getY() < var1.getHeight() - var4 - 1) {
               var22.getBlock().onPlantGrow(var22, var1, var21, var3);
               int var25 = var2.nextInt(2);
               int var26 = 1;
               byte var27 = 0;

               for(int var28 = 0; var28 <= var6; ++var28) {
                  int var15 = var3.getY() + var4 - var28;

                  for(int var16 = var3.getX() - var25; var16 <= var3.getX() + var25; ++var16) {
                     int var17 = var16 - var3.getX();

                     for(int var18 = var3.getZ() - var25; var18 <= var3.getZ() + var25; ++var18) {
                        int var19 = var18 - var3.getZ();
                        if (Math.abs(var17) != var25 || Math.abs(var19) != var25 || var25 <= 0) {
                           BlockPos var20 = new BlockPos(var16, var15, var18);
                           var22 = var1.getBlockState(var20);
                           if (var22.getBlock().canBeReplacedByLeaves(var22, var1, var20)) {
                              this.setBlockAndNotifyAdequately(var1, var20, LEAF);
                           }
                        }
                     }
                  }

                  if (var25 >= var26) {
                     var25 = var27;
                     var27 = 1;
                     ++var26;
                     if (var26 > var7) {
                        var26 = var7;
                     }
                  } else {
                     ++var25;
                  }
               }

               int var29 = var2.nextInt(3);

               for(int var30 = 0; var30 < var4 - var29; ++var30) {
                  BlockPos var31 = var3.up(var30);
                  var22 = var1.getBlockState(var31);
                  if (var22.getBlock().isAir(var22, var1, var31) || var22.getBlock().isLeaves(var22, var1, var31)) {
                     this.setBlockAndNotifyAdequately(var1, var3.up(var30), TRUNK);
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
