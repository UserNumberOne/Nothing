package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
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
                  if (var6 >= 0 && var6 < 256) {
                     if (!this.canGrowInto(var1.getBlockState(var8.setPos(var9, var6, var10)).getBlock())) {
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
            Block var16 = var1.getBlockState(var3.down()).getBlock();
            if ((var16 == Blocks.GRASS || var16 == Blocks.DIRT || var16 == Blocks.FARMLAND) && var3.getY() < 256 - var4 - 1) {
               this.setDirtAt(var1, var3.down());

               for(int var17 = var3.getY() - 3 + var4; var17 <= var3.getY() + var4; ++var17) {
                  int var19 = var17 - (var3.getY() + var4);
                  int var21 = 1 - var19 / 2;

                  for(int var22 = var3.getX() - var21; var22 <= var3.getX() + var21; ++var22) {
                     int var11 = var22 - var3.getX();

                     for(int var12 = var3.getZ() - var21; var12 <= var3.getZ() + var21; ++var12) {
                        int var13 = var12 - var3.getZ();
                        if (Math.abs(var11) != var21 || Math.abs(var13) != var21 || var2.nextInt(2) != 0 && var19 != 0) {
                           BlockPos var14 = new BlockPos(var22, var17, var12);
                           Material var15 = var1.getBlockState(var14).getMaterial();
                           if (var15 == Material.AIR || var15 == Material.LEAVES) {
                              this.setBlockAndNotifyAdequately(var1, var14, LEAF);
                           }
                        }
                     }
                  }
               }

               for(int var18 = 0; var18 < var4; ++var18) {
                  Material var20 = var1.getBlockState(var3.up(var18)).getMaterial();
                  if (var20 == Material.AIR || var20 == Material.LEAVES) {
                     this.setBlockAndNotifyAdequately(var1, var3.up(var18), LOG);
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
