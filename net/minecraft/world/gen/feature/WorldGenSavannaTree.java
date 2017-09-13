package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenSavannaTree extends WorldGenAbstractTree {
   private static final IBlockState TRUNK = Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.ACACIA);
   private static final IBlockState LEAF = Blocks.LEAVES2.getDefaultState().withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.ACACIA).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));

   public WorldGenSavannaTree(boolean var1) {
      super(var1);
   }

   public boolean generate(World var1, Random var2, BlockPos var3) {
      int var4 = var2.nextInt(3) + var2.nextInt(3) + 5;
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
            Block var20 = var1.getBlockState(var3.down()).getBlock();
            if ((var20 == Blocks.GRASS || var20 == Blocks.DIRT) && var3.getY() < 256 - var4 - 1) {
               this.setDirtAt(var1, var3.down());
               EnumFacing var21 = EnumFacing.Plane.HORIZONTAL.random(var2);
               int var22 = var4 - var2.nextInt(4) - 1;
               int var23 = 3 - var2.nextInt(3);
               int var24 = var3.getX();
               int var11 = var3.getZ();
               int var12 = 0;

               for(int var13 = 0; var13 < var4; ++var13) {
                  int var14 = var3.getY() + var13;
                  if (var13 >= var22 && var23 > 0) {
                     var24 += var21.getFrontOffsetX();
                     var11 += var21.getFrontOffsetZ();
                     --var23;
                  }

                  BlockPos var15 = new BlockPos(var24, var14, var11);
                  Material var16 = var1.getBlockState(var15).getMaterial();
                  if (var16 == Material.AIR || var16 == Material.LEAVES) {
                     this.placeLogAt(var1, var15);
                     var12 = var14;
                  }
               }

               BlockPos var28 = new BlockPos(var24, var12, var11);

               for(int var31 = -3; var31 <= 3; ++var31) {
                  for(int var34 = -3; var34 <= 3; ++var34) {
                     if (Math.abs(var31) != 3 || Math.abs(var34) != 3) {
                        this.placeLeafAt(var1, var28.add(var31, 0, var34));
                     }
                  }
               }

               var28 = var28.up();

               for(int var32 = -1; var32 <= 1; ++var32) {
                  for(int var35 = -1; var35 <= 1; ++var35) {
                     this.placeLeafAt(var1, var28.add(var32, 0, var35));
                  }
               }

               this.placeLeafAt(var1, var28.east(2));
               this.placeLeafAt(var1, var28.west(2));
               this.placeLeafAt(var1, var28.south(2));
               this.placeLeafAt(var1, var28.north(2));
               var24 = var3.getX();
               var11 = var3.getZ();
               EnumFacing var30 = EnumFacing.Plane.HORIZONTAL.random(var2);
               if (var30 != var21) {
                  int var33 = var22 - var2.nextInt(2) - 1;
                  int var36 = 1 + var2.nextInt(3);
                  var12 = 0;

                  for(int var37 = var33; var37 < var4 && var36 > 0; --var36) {
                     if (var37 >= 1) {
                        int var17 = var3.getY() + var37;
                        var24 += var30.getFrontOffsetX();
                        var11 += var30.getFrontOffsetZ();
                        BlockPos var18 = new BlockPos(var24, var17, var11);
                        Material var19 = var1.getBlockState(var18).getMaterial();
                        if (var19 == Material.AIR || var19 == Material.LEAVES) {
                           this.placeLogAt(var1, var18);
                           var12 = var17;
                        }
                     }

                     ++var37;
                  }

                  if (var12 > 0) {
                     BlockPos var38 = new BlockPos(var24, var12, var11);

                     for(int var40 = -2; var40 <= 2; ++var40) {
                        for(int var42 = -2; var42 <= 2; ++var42) {
                           if (Math.abs(var40) != 2 || Math.abs(var42) != 2) {
                              this.placeLeafAt(var1, var38.add(var40, 0, var42));
                           }
                        }
                     }

                     var38 = var38.up();

                     for(int var41 = -1; var41 <= 1; ++var41) {
                        for(int var43 = -1; var43 <= 1; ++var43) {
                           this.placeLeafAt(var1, var38.add(var41, 0, var43));
                        }
                     }
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

   private void placeLogAt(World var1, BlockPos var2) {
      this.setBlockAndNotifyAdequately(var1, var2, TRUNK);
   }

   private void placeLeafAt(World var1, BlockPos var2) {
      Material var3 = var1.getBlockState(var2).getMaterial();
      if (var3 == Material.AIR || var3 == Material.LEAVES) {
         this.setBlockAndNotifyAdequately(var1, var2, LEAF);
      }

   }
}
