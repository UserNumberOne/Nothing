package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenCanopyTree extends WorldGenAbstractTree {
   private static final IBlockState DARK_OAK_LOG = Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.DARK_OAK);
   private static final IBlockState DARK_OAK_LEAVES = Blocks.LEAVES2.getDefaultState().withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.DARK_OAK).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));

   public WorldGenCanopyTree(boolean var1) {
      super(var1);
   }

   public boolean generate(World var1, Random var2, BlockPos var3) {
      int var4 = var2.nextInt(3) + var2.nextInt(2) + 6;
      int var5 = var3.getX();
      int var6 = var3.getY();
      int var7 = var3.getZ();
      if (var6 >= 1 && var6 + var4 + 1 < 256) {
         BlockPos var8 = var3.down();
         IBlockState var9 = var1.getBlockState(var8);
         boolean var10 = var9.getBlock().canSustainPlant(var9, var1, var8, EnumFacing.UP, (BlockSapling)Blocks.SAPLING);
         if (var10 && var3.getY() < var1.getHeight() - var4 - 1) {
            if (!this.placeTreeOfHeight(var1, var3, var4)) {
               return false;
            } else {
               this.onPlantGrow(var1, var8, var3);
               this.onPlantGrow(var1, var8.east(), var3);
               this.onPlantGrow(var1, var8.south(), var3);
               this.onPlantGrow(var1, var8.south().east(), var3);
               EnumFacing var11 = EnumFacing.Plane.HORIZONTAL.random(var2);
               int var12 = var4 - var2.nextInt(4);
               int var13 = 2 - var2.nextInt(3);
               int var14 = var5;
               int var15 = var7;
               int var16 = var6 + var4 - 1;

               for(int var17 = 0; var17 < var4; ++var17) {
                  if (var17 >= var12 && var13 > 0) {
                     var14 += var11.getFrontOffsetX();
                     var15 += var11.getFrontOffsetZ();
                     --var13;
                  }

                  int var18 = var6 + var17;
                  BlockPos var19 = new BlockPos(var14, var18, var15);
                  var9 = var1.getBlockState(var19);
                  if (var9.getBlock().isAir(var9, var1, var19) || var9.getBlock().isLeaves(var9, var1, var19)) {
                     this.placeLogAt(var1, var19);
                     this.placeLogAt(var1, var19.east());
                     this.placeLogAt(var1, var19.south());
                     this.placeLogAt(var1, var19.east().south());
                  }
               }

               for(int var23 = -2; var23 <= 0; ++var23) {
                  for(int var26 = -2; var26 <= 0; ++var26) {
                     byte var29 = -1;
                     this.placeLeafAt(var1, var14 + var23, var16 + var29, var15 + var26);
                     this.placeLeafAt(var1, 1 + var14 - var23, var16 + var29, var15 + var26);
                     this.placeLeafAt(var1, var14 + var23, var16 + var29, 1 + var15 - var26);
                     this.placeLeafAt(var1, 1 + var14 - var23, var16 + var29, 1 + var15 - var26);
                     if ((var23 > -2 || var26 > -1) && (var23 != -1 || var26 != -2)) {
                        var29 = 1;
                        this.placeLeafAt(var1, var14 + var23, var16 + var29, var15 + var26);
                        this.placeLeafAt(var1, 1 + var14 - var23, var16 + var29, var15 + var26);
                        this.placeLeafAt(var1, var14 + var23, var16 + var29, 1 + var15 - var26);
                        this.placeLeafAt(var1, 1 + var14 - var23, var16 + var29, 1 + var15 - var26);
                     }
                  }
               }

               if (var2.nextBoolean()) {
                  this.placeLeafAt(var1, var14, var16 + 2, var15);
                  this.placeLeafAt(var1, var14 + 1, var16 + 2, var15);
                  this.placeLeafAt(var1, var14 + 1, var16 + 2, var15 + 1);
                  this.placeLeafAt(var1, var14, var16 + 2, var15 + 1);
               }

               for(int var24 = -3; var24 <= 4; ++var24) {
                  for(int var27 = -3; var27 <= 4; ++var27) {
                     if ((var24 != -3 || var27 != -3) && (var24 != -3 || var27 != 4) && (var24 != 4 || var27 != -3) && (var24 != 4 || var27 != 4) && (Math.abs(var24) < 3 || Math.abs(var27) < 3)) {
                        this.placeLeafAt(var1, var14 + var24, var16, var15 + var27);
                     }
                  }
               }

               for(int var25 = -1; var25 <= 2; ++var25) {
                  for(int var28 = -1; var28 <= 2; ++var28) {
                     if ((var25 < 0 || var25 > 1 || var28 < 0 || var28 > 1) && var2.nextInt(3) <= 0) {
                        int var31 = var2.nextInt(3) + 2;

                        for(int var20 = 0; var20 < var31; ++var20) {
                           this.placeLogAt(var1, new BlockPos(var5 + var25, var16 - var20 - 1, var7 + var28));
                        }

                        for(int var32 = -1; var32 <= 1; ++var32) {
                           for(int var21 = -1; var21 <= 1; ++var21) {
                              this.placeLeafAt(var1, var14 + var25 + var32, var16, var15 + var28 + var21);
                           }
                        }

                        for(int var33 = -2; var33 <= 2; ++var33) {
                           for(int var34 = -2; var34 <= 2; ++var34) {
                              if (Math.abs(var33) != 2 || Math.abs(var34) != 2) {
                                 this.placeLeafAt(var1, var14 + var25 + var33, var16 - 1, var15 + var28 + var34);
                              }
                           }
                        }
                     }
                  }
               }

               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean placeTreeOfHeight(World var1, BlockPos var2, int var3) {
      int var4 = var2.getX();
      int var5 = var2.getY();
      int var6 = var2.getZ();
      BlockPos.MutableBlockPos var7 = new BlockPos.MutableBlockPos();

      for(int var8 = 0; var8 <= var3 + 1; ++var8) {
         byte var9 = 1;
         if (var8 == 0) {
            var9 = 0;
         }

         if (var8 >= var3 - 1) {
            var9 = 2;
         }

         for(int var10 = -var9; var10 <= var9; ++var10) {
            for(int var11 = -var9; var11 <= var9; ++var11) {
               if (!this.isReplaceable(var1, var7.setPos(var4 + var10, var5 + var8, var6 + var11))) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   private void placeLogAt(World var1, BlockPos var2) {
      if (this.canGrowInto(var1.getBlockState(var2).getBlock())) {
         this.setBlockAndNotifyAdequately(var1, var2, DARK_OAK_LOG);
      }

   }

   private void placeLeafAt(World var1, int var2, int var3, int var4) {
      BlockPos var5 = new BlockPos(var2, var3, var4);
      IBlockState var6 = var1.getBlockState(var5);
      if (var6.getBlock().isAir(var6, var1, var5)) {
         this.setBlockAndNotifyAdequately(var1, var5, DARK_OAK_LEAVES);
      }

   }

   private void onPlantGrow(World var1, BlockPos var2, BlockPos var3) {
      IBlockState var4 = var1.getBlockState(var2);
      var4.getBlock().onPlantGrow(var4, var1, var2, var3);
   }
}
