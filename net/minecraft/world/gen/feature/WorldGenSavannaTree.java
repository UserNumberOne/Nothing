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
            BlockPos var22 = var3.down();
            IBlockState var23 = var1.getBlockState(var22);
            boolean var26 = var23.getBlock().canSustainPlant(var23, var1, var22, EnumFacing.UP, (BlockSapling)Blocks.SAPLING);
            if (var26 && var3.getY() < var1.getHeight() - var4 - 1) {
               var23.getBlock().onPlantGrow(var23, var1, var22, var3);
               EnumFacing var27 = EnumFacing.Plane.HORIZONTAL.random(var2);
               int var28 = var4 - var2.nextInt(4) - 1;
               int var11 = 3 - var2.nextInt(3);
               int var12 = var3.getX();
               int var13 = var3.getZ();
               int var14 = 0;

               for(int var15 = 0; var15 < var4; ++var15) {
                  int var16 = var3.getY() + var15;
                  if (var15 >= var28 && var11 > 0) {
                     var12 += var27.getFrontOffsetX();
                     var13 += var27.getFrontOffsetZ();
                     --var11;
                  }

                  BlockPos var17 = new BlockPos(var12, var16, var13);
                  var23 = var1.getBlockState(var17);
                  if (var23.getBlock().isAir(var23, var1, var17) || var23.getBlock().isLeaves(var23, var1, var17)) {
                     this.placeLogAt(var1, var17);
                     var14 = var16;
                  }
               }

               BlockPos var32 = new BlockPos(var12, var14, var13);

               for(int var34 = -3; var34 <= 3; ++var34) {
                  for(int var37 = -3; var37 <= 3; ++var37) {
                     if (Math.abs(var34) != 3 || Math.abs(var37) != 3) {
                        this.placeLeafAt(var1, var32.add(var34, 0, var37));
                     }
                  }
               }

               var32 = var32.up();

               for(int var35 = -1; var35 <= 1; ++var35) {
                  for(int var38 = -1; var38 <= 1; ++var38) {
                     this.placeLeafAt(var1, var32.add(var35, 0, var38));
                  }
               }

               this.placeLeafAt(var1, var32.east(2));
               this.placeLeafAt(var1, var32.west(2));
               this.placeLeafAt(var1, var32.south(2));
               this.placeLeafAt(var1, var32.north(2));
               var12 = var3.getX();
               var13 = var3.getZ();
               EnumFacing var36 = EnumFacing.Plane.HORIZONTAL.random(var2);
               if (var36 != var27) {
                  int var39 = var28 - var2.nextInt(2) - 1;
                  int var18 = 1 + var2.nextInt(3);
                  var14 = 0;

                  for(int var19 = var39; var19 < var4 && var18 > 0; --var18) {
                     if (var19 >= 1) {
                        int var20 = var3.getY() + var19;
                        var12 += var36.getFrontOffsetX();
                        var13 += var36.getFrontOffsetZ();
                        BlockPos var21 = new BlockPos(var12, var20, var13);
                        var23 = var1.getBlockState(var21);
                        if (var23.getBlock().isAir(var23, var1, var21) || var23.getBlock().isLeaves(var23, var1, var21)) {
                           this.placeLogAt(var1, var21);
                           var14 = var20;
                        }
                     }

                     ++var19;
                  }

                  if (var14 > 0) {
                     BlockPos var40 = new BlockPos(var12, var14, var13);

                     for(int var42 = -2; var42 <= 2; ++var42) {
                        for(int var44 = -2; var44 <= 2; ++var44) {
                           if (Math.abs(var42) != 2 || Math.abs(var44) != 2) {
                              this.placeLeafAt(var1, var40.add(var42, 0, var44));
                           }
                        }
                     }

                     var40 = var40.up();

                     for(int var43 = -1; var43 <= 1; ++var43) {
                        for(int var45 = -1; var45 <= 1; ++var45) {
                           this.placeLeafAt(var1, var40.add(var43, 0, var45));
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
      IBlockState var3 = var1.getBlockState(var2);
      if (var3.getBlock().isAir(var3, var1, var2) || var3.getBlock().isLeaves(var3, var1, var2)) {
         this.setBlockAndNotifyAdequately(var1, var2, LEAF);
      }

   }
}
