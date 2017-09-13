package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
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
                     if (!this.canGrowInto(var1.getBlockState(var11.setPos(var12, var9, var13)).getBlock())) {
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
            Block var17 = var1.getBlockState(var3.down()).getBlock();
            if ((var17 == Blocks.GRASS || var17 == Blocks.DIRT) && var3.getY() < 256 - var4 - 1) {
               this.setDirtAt(var1, var3.down());
               int var19 = 0;

               for(int var20 = var3.getY() + var4; var20 >= var3.getY() + var5; --var20) {
                  for(int var22 = var3.getX() - var19; var22 <= var3.getX() + var19; ++var22) {
                     int var24 = var22 - var3.getX();

                     for(int var14 = var3.getZ() - var19; var14 <= var3.getZ() + var19; ++var14) {
                        int var15 = var14 - var3.getZ();
                        if (Math.abs(var24) != var19 || Math.abs(var15) != var19 || var19 <= 0) {
                           BlockPos var16 = new BlockPos(var22, var20, var14);
                           if (!var1.getBlockState(var16).isFullBlock()) {
                              this.setBlockAndNotifyAdequately(var1, var16, LEAF);
                           }
                        }
                     }
                  }

                  if (var19 >= 1 && var20 == var3.getY() + var5 + 1) {
                     --var19;
                  } else if (var19 < var7) {
                     ++var19;
                  }
               }

               for(int var21 = 0; var21 < var4 - 1; ++var21) {
                  Material var23 = var1.getBlockState(var3.up(var21)).getMaterial();
                  if (var23 == Material.AIR || var23 == Material.LEAVES) {
                     this.setBlockAndNotifyAdequately(var1, var3.up(var21), TRUNK);
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
