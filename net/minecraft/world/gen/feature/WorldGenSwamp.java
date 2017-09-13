package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
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

public class WorldGenSwamp extends WorldGenAbstractTree {
   private static final IBlockState TRUNK = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK);
   private static final IBlockState LEAF = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK).withProperty(BlockOldLeaf.CHECK_DECAY, Boolean.valueOf(false));

   public WorldGenSwamp() {
      super(false);
   }

   public boolean generate(World var1, Random var2, BlockPos var3) {
      int var4;
      for(var4 = var2.nextInt(4) + 5; var1.getBlockState(var3.down()).getMaterial() == Material.WATER; var3 = var3.down()) {
         ;
      }

      boolean var5 = true;
      if (var3.getY() >= 1 && var3.getY() + var4 + 1 <= 256) {
         for(int var6 = var3.getY(); var6 <= var3.getY() + 1 + var4; ++var6) {
            byte var7 = 1;
            if (var6 == var3.getY()) {
               var7 = 0;
            }

            if (var6 >= var3.getY() + 1 + var4 - 2) {
               var7 = 3;
            }

            BlockPos.MutableBlockPos var8 = new BlockPos.MutableBlockPos();

            for(int var9 = var3.getX() - var7; var9 <= var3.getX() + var7 && var5; ++var9) {
               for(int var10 = var3.getZ() - var7; var10 <= var3.getZ() + var7 && var5; ++var10) {
                  if (var6 >= 0 && var6 < 256) {
                     IBlockState var11 = var1.getBlockState(var8.setPos(var9, var6, var10));
                     Block var12 = var11.getBlock();
                     if (!var11.getBlock().isAir(var11, var1, var8.setPos(var9, var6, var10)) && !var11.getBlock().isLeaves(var11, var1, var8.setPos(var9, var6, var10))) {
                        if (var12 != Blocks.WATER && var12 != Blocks.FLOWING_WATER) {
                           var5 = false;
                        } else if (var6 > var3.getY()) {
                           var5 = false;
                        }
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
            BlockPos var19 = var3.down();
            IBlockState var20 = var1.getBlockState(var19);
            boolean var22 = var20.getBlock().canSustainPlant(var20, var1, var19, EnumFacing.UP, (BlockSapling)Blocks.SAPLING);
            if (var22 && var3.getY() < var1.getHeight() - var4 - 1) {
               var20.getBlock().onPlantGrow(var20, var1, var3.down(), var3);

               for(int var23 = var3.getY() - 3 + var4; var23 <= var3.getY() + var4; ++var23) {
                  int var26 = var23 - (var3.getY() + var4);
                  int var29 = 2 - var26 / 2;

                  for(int var32 = var3.getX() - var29; var32 <= var3.getX() + var29; ++var32) {
                     int var13 = var32 - var3.getX();

                     for(int var14 = var3.getZ() - var29; var14 <= var3.getZ() + var29; ++var14) {
                        int var15 = var14 - var3.getZ();
                        if (Math.abs(var13) != var29 || Math.abs(var15) != var29 || var2.nextInt(2) != 0 && var26 != 0) {
                           BlockPos var16 = new BlockPos(var32, var23, var14);
                           var20 = var1.getBlockState(var16);
                           if (var20.getBlock().canBeReplacedByLeaves(var20, var1, var16)) {
                              this.setBlockAndNotifyAdequately(var1, var16, LEAF);
                           }
                        }
                     }
                  }
               }

               for(int var24 = 0; var24 < var4; ++var24) {
                  BlockPos var27 = var3.up(var24);
                  IBlockState var30 = var1.getBlockState(var27);
                  Block var33 = var30.getBlock();
                  if (var33.isAir(var30, var1, var27) || var33.isLeaves(var30, var1, var27) || var33 == Blocks.FLOWING_WATER || var33 == Blocks.WATER) {
                     this.setBlockAndNotifyAdequately(var1, var3.up(var24), TRUNK);
                  }
               }

               for(int var25 = var3.getY() - 3 + var4; var25 <= var3.getY() + var4; ++var25) {
                  int var28 = var25 - (var3.getY() + var4);
                  int var31 = 2 - var28 / 2;
                  BlockPos.MutableBlockPos var34 = new BlockPos.MutableBlockPos();

                  for(int var35 = var3.getX() - var31; var35 <= var3.getX() + var31; ++var35) {
                     for(int var36 = var3.getZ() - var31; var36 <= var3.getZ() + var31; ++var36) {
                        var34.setPos(var35, var25, var36);
                        if (var1.getBlockState(var34).getMaterial() == Material.LEAVES) {
                           BlockPos var37 = var34.west();
                           BlockPos var38 = var34.east();
                           BlockPos var17 = var34.north();
                           BlockPos var18 = var34.south();
                           if (var2.nextInt(4) == 0 && this.isAir(var1, var37)) {
                              this.addVine(var1, var37, BlockVine.EAST);
                           }

                           if (var2.nextInt(4) == 0 && this.isAir(var1, var38)) {
                              this.addVine(var1, var38, BlockVine.WEST);
                           }

                           if (var2.nextInt(4) == 0 && this.isAir(var1, var17)) {
                              this.addVine(var1, var17, BlockVine.SOUTH);
                           }

                           if (var2.nextInt(4) == 0 && this.isAir(var1, var18)) {
                              this.addVine(var1, var18, BlockVine.NORTH);
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

   private void addVine(World var1, BlockPos var2, PropertyBool var3) {
      IBlockState var4 = Blocks.VINE.getDefaultState().withProperty(var3, Boolean.valueOf(true));
      this.setBlockAndNotifyAdequately(var1, var2, var4);
      int var5 = 4;

      for(BlockPos var6 = var2.down(); this.isAir(var1, var6) && var5 > 0; --var5) {
         this.setBlockAndNotifyAdequately(var1, var6, var4);
         var6 = var6.down();
      }

   }

   private boolean isAir(World var1, BlockPos var2) {
      IBlockState var3 = var1.getBlockState(var2);
      return var3.getBlock().isAir(var3, var1, var2);
   }
}
