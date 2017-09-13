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
      if (var3.getY() >= 1 && var3.getY() + var4 + 1 <= 256) {
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
                  if (var9 >= 0 && var9 < 256) {
                     Material var14 = var1.getBlockState(var11.setPos(var12, var9, var13)).getMaterial();
                     if (var14 != Material.AIR && var14 != Material.LEAVES) {
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
            Block var20 = var1.getBlockState(var3.down()).getBlock();
            if ((var20 == Blocks.GRASS || var20 == Blocks.DIRT || var20 == Blocks.FARMLAND) && var3.getY() < 256 - var4 - 1) {
               this.setDirtAt(var1, var3.down());
               int var21 = var2.nextInt(2);
               int var22 = 1;
               byte var23 = 0;

               for(int var24 = 0; var24 <= var6; ++var24) {
                  int var26 = var3.getY() + var4 - var24;

                  for(int var15 = var3.getX() - var21; var15 <= var3.getX() + var21; ++var15) {
                     int var16 = var15 - var3.getX();

                     for(int var17 = var3.getZ() - var21; var17 <= var3.getZ() + var21; ++var17) {
                        int var18 = var17 - var3.getZ();
                        if (Math.abs(var16) != var21 || Math.abs(var18) != var21 || var21 <= 0) {
                           BlockPos var19 = new BlockPos(var15, var26, var17);
                           if (!var1.getBlockState(var19).isFullBlock()) {
                              this.setBlockAndNotifyAdequately(var1, var19, LEAF);
                           }
                        }
                     }
                  }

                  if (var21 >= var22) {
                     var21 = var23;
                     var23 = 1;
                     ++var22;
                     if (var22 > var7) {
                        var22 = var7;
                     }
                  } else {
                     ++var21;
                  }
               }

               int var25 = var2.nextInt(3);

               for(int var27 = 0; var27 < var4 - var25; ++var27) {
                  Material var28 = var1.getBlockState(var3.up(var27)).getMaterial();
                  if (var28 == Material.AIR || var28 == Material.LEAVES) {
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
