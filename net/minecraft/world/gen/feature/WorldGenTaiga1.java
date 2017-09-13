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

public class WorldGenTaiga1 extends WorldGenAbstractTree {
   private static final IBlockState TRUNK = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE);
   private static final IBlockState LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.SPRUCE).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));

   public WorldGenTaiga1() {
      super(false);
   }

   public boolean generate(World var1, Random var2, BlockPos var3) {
      int var4 = var2.nextInt(5) + 7;
      int var5 = var4 - var2.nextInt(2) - 3;
      int var6 = var4 - var5;
      int var7 = 1 + var2.nextInt(var6 + 1);
      if (var3.getY() >= 1 && var3.getY() + var4 + 1 <= 256) {
         boolean var8 = true;

         for(int var9 = var3.getY(); var9 <= var3.getY() + 1 + var4 && var8; ++var9) {
            int var10 = 1;
            if (var9 - var3.getY() < var5) {
               var10 = 0;
            } else {
               var10 = var7;
            }

            BlockPos.MutableBlockPos var11 = new BlockPos.MutableBlockPos();

            for(int var12 = var3.getX() - var10; var12 <= var3.getX() + var10 && var8; ++var12) {
               for(int var13 = var3.getZ() - var10; var13 <= var3.getZ() + var10 && var8; ++var13) {
                  if (var9 >= 0 && var9 < 256) {
                     if (!this.isReplaceable(var1, var11.setPos(var12, var9, var13))) {
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
            BlockPos var19 = var3.down();
            IBlockState var21 = var1.getBlockState(var19);
            boolean var24 = var21.getBlock().canSustainPlant(var21, var1, var19, EnumFacing.UP, (BlockSapling)Blocks.SAPLING);
            if (var24 && var3.getY() < 256 - var4 - 1) {
               var21.getBlock().onPlantGrow(var21, var1, var19, var3);
               int var25 = 0;

               for(int var26 = var3.getY() + var4; var26 >= var3.getY() + var5; --var26) {
                  for(int var14 = var3.getX() - var25; var14 <= var3.getX() + var25; ++var14) {
                     int var15 = var14 - var3.getX();

                     for(int var16 = var3.getZ() - var25; var16 <= var3.getZ() + var25; ++var16) {
                        int var17 = var16 - var3.getZ();
                        if (Math.abs(var15) != var25 || Math.abs(var17) != var25 || var25 <= 0) {
                           BlockPos var18 = new BlockPos(var14, var26, var16);
                           var21 = var1.getBlockState(var18);
                           if (var21.getBlock().canBeReplacedByLeaves(var21, var1, var18)) {
                              this.setBlockAndNotifyAdequately(var1, var18, LEAF);
                           }
                        }
                     }
                  }

                  if (var25 >= 1 && var26 == var3.getY() + var5 + 1) {
                     --var25;
                  } else if (var25 < var7) {
                     ++var25;
                  }
               }

               for(int var27 = 0; var27 < var4 - 1; ++var27) {
                  BlockPos var28 = var3.up(var27);
                  var21 = var1.getBlockState(var28);
                  if (var21.getBlock().isAir(var21, var1, var28) || var21.getBlock().isLeaves(var21, var1, var28)) {
                     this.setBlockAndNotifyAdequately(var1, var3.up(var27), TRUNK);
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
