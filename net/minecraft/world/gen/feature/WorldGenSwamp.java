package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockVine;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
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
                     if (var11.getMaterial() != Material.AIR && var11.getMaterial() != Material.LEAVES) {
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
            Block var17 = var1.getBlockState(var3.down()).getBlock();
            if ((var17 == Blocks.GRASS || var17 == Blocks.DIRT) && var3.getY() < 256 - var4 - 1) {
               this.setDirtAt(var1, var3.down());

               for(int var18 = var3.getY() - 3 + var4; var18 <= var3.getY() + var4; ++var18) {
                  int var21 = var18 - (var3.getY() + var4);
                  int var24 = 2 - var21 / 2;

                  for(int var27 = var3.getX() - var24; var27 <= var3.getX() + var24; ++var27) {
                     int var29 = var27 - var3.getX();

                     for(int var31 = var3.getZ() - var24; var31 <= var3.getZ() + var24; ++var31) {
                        int var13 = var31 - var3.getZ();
                        if (Math.abs(var29) != var24 || Math.abs(var13) != var24 || var2.nextInt(2) != 0 && var21 != 0) {
                           BlockPos var14 = new BlockPos(var27, var18, var31);
                           if (!var1.getBlockState(var14).isFullBlock()) {
                              this.setBlockAndNotifyAdequately(var1, var14, LEAF);
                           }
                        }
                     }
                  }
               }

               for(int var19 = 0; var19 < var4; ++var19) {
                  IBlockState var22 = var1.getBlockState(var3.up(var19));
                  Block var25 = var22.getBlock();
                  if (var22.getMaterial() == Material.AIR || var22.getMaterial() == Material.LEAVES || var25 == Blocks.FLOWING_WATER || var25 == Blocks.WATER) {
                     this.setBlockAndNotifyAdequately(var1, var3.up(var19), TRUNK);
                  }
               }

               for(int var20 = var3.getY() - 3 + var4; var20 <= var3.getY() + var4; ++var20) {
                  int var23 = var20 - (var3.getY() + var4);
                  int var26 = 2 - var23 / 2;
                  BlockPos.MutableBlockPos var28 = new BlockPos.MutableBlockPos();

                  for(int var30 = var3.getX() - var26; var30 <= var3.getX() + var26; ++var30) {
                     for(int var32 = var3.getZ() - var26; var32 <= var3.getZ() + var26; ++var32) {
                        var28.setPos(var30, var20, var32);
                        if (var1.getBlockState(var28).getMaterial() == Material.LEAVES) {
                           BlockPos var33 = var28.west();
                           BlockPos var34 = var28.east();
                           BlockPos var15 = var28.north();
                           BlockPos var16 = var28.south();
                           if (var2.nextInt(4) == 0 && var1.getBlockState(var33).getMaterial() == Material.AIR) {
                              this.addVine(var1, var33, BlockVine.EAST);
                           }

                           if (var2.nextInt(4) == 0 && var1.getBlockState(var34).getMaterial() == Material.AIR) {
                              this.addVine(var1, var34, BlockVine.WEST);
                           }

                           if (var2.nextInt(4) == 0 && var1.getBlockState(var15).getMaterial() == Material.AIR) {
                              this.addVine(var1, var15, BlockVine.SOUTH);
                           }

                           if (var2.nextInt(4) == 0 && var1.getBlockState(var16).getMaterial() == Material.AIR) {
                              this.addVine(var1, var16, BlockVine.NORTH);
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

      for(BlockPos var6 = var2.down(); var1.getBlockState(var6).getMaterial() == Material.AIR && var5 > 0; --var5) {
         this.setBlockAndNotifyAdequately(var1, var6, var4);
         var6 = var6.down();
      }

   }
}
