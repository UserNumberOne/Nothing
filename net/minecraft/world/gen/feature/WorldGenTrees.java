package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSapling;
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
      if (var3.getY() >= 1 && var3.getY() + var4 + 1 <= var1.getHeight()) {
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
            IBlockState var19 = var1.getBlockState(var3.down());
            if (var19.getBlock().canSustainPlant(var19, var1, var3.down(), EnumFacing.UP, (BlockSapling)Blocks.SAPLING) && var3.getY() < var1.getHeight() - var4 - 1) {
               this.setDirtAt(var1, var3.down());
               boolean var23 = true;
               boolean var24 = false;

               for(int var25 = var3.getY() - 3 + var4; var25 <= var3.getY() + var4; ++var25) {
                  int var29 = var25 - (var3.getY() + var4);
                  int var11 = 1 - var29 / 2;

                  for(int var12 = var3.getX() - var11; var12 <= var3.getX() + var11; ++var12) {
                     int var13 = var12 - var3.getX();

                     for(int var14 = var3.getZ() - var11; var14 <= var3.getZ() + var11; ++var14) {
                        int var15 = var14 - var3.getZ();
                        if (Math.abs(var13) != var11 || Math.abs(var15) != var11 || var2.nextInt(2) != 0 && var29 != 0) {
                           BlockPos var16 = new BlockPos(var12, var25, var14);
                           var19 = var1.getBlockState(var16);
                           if (var19.getBlock().isAir(var19, var1, var16) || var19.getBlock().isLeaves(var19, var1, var16) || var19.getMaterial() == Material.VINE) {
                              this.setBlockAndNotifyAdequately(var1, var16, this.metaLeaves);
                           }
                        }
                     }
                  }
               }

               for(int var26 = 0; var26 < var4; ++var26) {
                  BlockPos var30 = var3.up(var26);
                  var19 = var1.getBlockState(var30);
                  if (var19.getBlock().isAir(var19, var1, var30) || var19.getBlock().isLeaves(var19, var1, var30) || var19.getMaterial() == Material.VINE) {
                     this.setBlockAndNotifyAdequately(var1, var3.up(var26), this.metaWood);
                     if (this.vinesGrow && var26 > 0) {
                        if (var2.nextInt(3) > 0 && var1.isAirBlock(var3.add(-1, var26, 0))) {
                           this.addVine(var1, var3.add(-1, var26, 0), BlockVine.EAST);
                        }

                        if (var2.nextInt(3) > 0 && var1.isAirBlock(var3.add(1, var26, 0))) {
                           this.addVine(var1, var3.add(1, var26, 0), BlockVine.WEST);
                        }

                        if (var2.nextInt(3) > 0 && var1.isAirBlock(var3.add(0, var26, -1))) {
                           this.addVine(var1, var3.add(0, var26, -1), BlockVine.SOUTH);
                        }

                        if (var2.nextInt(3) > 0 && var1.isAirBlock(var3.add(0, var26, 1))) {
                           this.addVine(var1, var3.add(0, var26, 1), BlockVine.NORTH);
                        }
                     }
                  }
               }

               if (this.vinesGrow) {
                  for(int var27 = var3.getY() - 3 + var4; var27 <= var3.getY() + var4; ++var27) {
                     int var31 = var27 - (var3.getY() + var4);
                     int var33 = 2 - var31 / 2;
                     BlockPos.MutableBlockPos var35 = new BlockPos.MutableBlockPos();

                     for(int var37 = var3.getX() - var33; var37 <= var3.getX() + var33; ++var37) {
                        for(int var38 = var3.getZ() - var33; var38 <= var3.getZ() + var33; ++var38) {
                           var35.setPos(var37, var27, var38);
                           var19 = var1.getBlockState(var35);
                           if (var19.getBlock().isLeaves(var19, var1, var35)) {
                              BlockPos var39 = var35.west();
                              BlockPos var40 = var35.east();
                              BlockPos var17 = var35.north();
                              BlockPos var18 = var35.south();
                              if (var2.nextInt(4) == 0 && var1.isAirBlock(var39)) {
                                 this.addHangingVine(var1, var39, BlockVine.EAST);
                              }

                              if (var2.nextInt(4) == 0 && var1.isAirBlock(var40)) {
                                 this.addHangingVine(var1, var40, BlockVine.WEST);
                              }

                              if (var2.nextInt(4) == 0 && var1.isAirBlock(var17)) {
                                 this.addHangingVine(var1, var17, BlockVine.SOUTH);
                              }

                              if (var2.nextInt(4) == 0 && var1.isAirBlock(var18)) {
                                 this.addHangingVine(var1, var18, BlockVine.NORTH);
                              }
                           }
                        }
                     }
                  }

                  if (var2.nextInt(5) == 0 && var4 > 5) {
                     for(int var28 = 0; var28 < 2; ++var28) {
                        for(EnumFacing var34 : EnumFacing.Plane.HORIZONTAL) {
                           if (var2.nextInt(4 - var28) == 0) {
                              EnumFacing var36 = var34.getOpposite();
                              this.placeCocoa(var1, var2.nextInt(3), var3.add(var36.getFrontOffsetX(), var4 - 5 + var28, var36.getFrontOffsetZ()), var34);
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

      for(BlockPos var5 = var2.down(); var1.isAirBlock(var5) && var4 > 0; --var4) {
         this.addVine(var1, var5, var3);
         var5 = var5.down();
      }

   }
}
