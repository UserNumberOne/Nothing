package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockVine;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenTrees extends WorldGenAbstractTree {
   private static final IBlockState DEFAULT_TRUNK = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK);
   private static final IBlockState DEFAULT_LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));
   private final int minTreeHeight;
   private final boolean vinesGrow;
   private final IBlockState metaWood;
   private final IBlockState metaLeaves;

   public WorldGenTrees(boolean var1) {
      this(var1, 4, DEFAULT_TRUNK, DEFAULT_LEAF, false);
   }

   public WorldGenTrees(boolean var1, int var2, IBlockState var3, IBlockState var4, boolean var5) {
      super(var1);
      this.minTreeHeight = var2;
      this.metaWood = var3;
      this.metaLeaves = var4;
      this.vinesGrow = var5;
   }

   public boolean generate(World var1, Random var2, BlockPos var3) {
      int var4 = var2.nextInt(3) + this.minTreeHeight;
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
            Block var19 = var1.getBlockState(var3.down()).getBlock();
            if ((var19 == Blocks.GRASS || var19 == Blocks.DIRT || var19 == Blocks.FARMLAND) && var3.getY() < 256 - var4 - 1) {
               this.setDirtAt(var1, var3.down());
               boolean var20 = true;
               boolean var21 = false;

               for(int var22 = var3.getY() - 3 + var4; var22 <= var3.getY() + var4; ++var22) {
                  int var26 = var22 - (var3.getY() + var4);
                  int var11 = 1 - var26 / 2;

                  for(int var12 = var3.getX() - var11; var12 <= var3.getX() + var11; ++var12) {
                     int var13 = var12 - var3.getX();

                     for(int var14 = var3.getZ() - var11; var14 <= var3.getZ() + var11; ++var14) {
                        int var15 = var14 - var3.getZ();
                        if (Math.abs(var13) != var11 || Math.abs(var15) != var11 || var2.nextInt(2) != 0 && var26 != 0) {
                           BlockPos var16 = new BlockPos(var12, var22, var14);
                           Material var17 = var1.getBlockState(var16).getMaterial();
                           if (var17 == Material.AIR || var17 == Material.LEAVES || var17 == Material.VINE) {
                              this.setBlockAndNotifyAdequately(var1, var16, this.metaLeaves);
                           }
                        }
                     }
                  }
               }

               for(int var23 = 0; var23 < var4; ++var23) {
                  Material var27 = var1.getBlockState(var3.up(var23)).getMaterial();
                  if (var27 == Material.AIR || var27 == Material.LEAVES || var27 == Material.VINE) {
                     this.setBlockAndNotifyAdequately(var1, var3.up(var23), this.metaWood);
                     if (this.vinesGrow && var23 > 0) {
                        if (var2.nextInt(3) > 0 && var1.isAirBlock(var3.add(-1, var23, 0))) {
                           this.addVine(var1, var3.add(-1, var23, 0), BlockVine.EAST);
                        }

                        if (var2.nextInt(3) > 0 && var1.isAirBlock(var3.add(1, var23, 0))) {
                           this.addVine(var1, var3.add(1, var23, 0), BlockVine.WEST);
                        }

                        if (var2.nextInt(3) > 0 && var1.isAirBlock(var3.add(0, var23, -1))) {
                           this.addVine(var1, var3.add(0, var23, -1), BlockVine.SOUTH);
                        }

                        if (var2.nextInt(3) > 0 && var1.isAirBlock(var3.add(0, var23, 1))) {
                           this.addVine(var1, var3.add(0, var23, 1), BlockVine.NORTH);
                        }
                     }
                  }
               }

               if (this.vinesGrow) {
                  for(int var24 = var3.getY() - 3 + var4; var24 <= var3.getY() + var4; ++var24) {
                     int var28 = var24 - (var3.getY() + var4);
                     int var30 = 2 - var28 / 2;
                     BlockPos.MutableBlockPos var32 = new BlockPos.MutableBlockPos();

                     for(int var34 = var3.getX() - var30; var34 <= var3.getX() + var30; ++var34) {
                        for(int var35 = var3.getZ() - var30; var35 <= var3.getZ() + var30; ++var35) {
                           var32.setPos(var34, var24, var35);
                           if (var1.getBlockState(var32).getMaterial() == Material.LEAVES) {
                              BlockPos var36 = var32.west();
                              BlockPos var37 = var32.east();
                              BlockPos var38 = var32.north();
                              BlockPos var18 = var32.south();
                              if (var2.nextInt(4) == 0 && var1.getBlockState(var36).getMaterial() == Material.AIR) {
                                 this.addHangingVine(var1, var36, BlockVine.EAST);
                              }

                              if (var2.nextInt(4) == 0 && var1.getBlockState(var37).getMaterial() == Material.AIR) {
                                 this.addHangingVine(var1, var37, BlockVine.WEST);
                              }

                              if (var2.nextInt(4) == 0 && var1.getBlockState(var38).getMaterial() == Material.AIR) {
                                 this.addHangingVine(var1, var38, BlockVine.SOUTH);
                              }

                              if (var2.nextInt(4) == 0 && var1.getBlockState(var18).getMaterial() == Material.AIR) {
                                 this.addHangingVine(var1, var18, BlockVine.NORTH);
                              }
                           }
                        }
                     }
                  }

                  if (var2.nextInt(5) == 0 && var4 > 5) {
                     for(int var25 = 0; var25 < 2; ++var25) {
                        for(EnumFacing var31 : EnumFacing.Plane.HORIZONTAL) {
                           if (var2.nextInt(4 - var25) == 0) {
                              EnumFacing var33 = var31.getOpposite();
                              this.placeCocoa(var1, var2.nextInt(3), var3.add(var33.getFrontOffsetX(), var4 - 5 + var25, var33.getFrontOffsetZ()), var31);
                           }
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

   private void placeCocoa(World var1, int var2, BlockPos var3, EnumFacing var4) {
      this.setBlockAndNotifyAdequately(var1, var3, Blocks.COCOA.getDefaultState().withProperty(BlockCocoa.AGE, Integer.valueOf(var2)).withProperty(BlockCocoa.FACING, var4));
   }

   private void addVine(World var1, BlockPos var2, PropertyBool var3) {
      this.setBlockAndNotifyAdequately(var1, var2, Blocks.VINE.getDefaultState().withProperty(var3, Boolean.valueOf(true)));
   }

   private void addHangingVine(World var1, BlockPos var2, PropertyBool var3) {
      this.addVine(var1, var2, var3);
      int var4 = 4;

      for(BlockPos var5 = var2.down(); var1.getBlockState(var5).getMaterial() == Material.AIR && var4 > 0; --var4) {
         this.addVine(var1, var5, var3);
         var5 = var5.down();
      }

   }
}
