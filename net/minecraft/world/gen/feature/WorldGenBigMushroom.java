package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHugeMushroom;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenBigMushroom extends WorldGenerator {
   private final Block mushroomType;

   public WorldGenBigMushroom(Block var1) {
      super(true);
      this.mushroomType = var1;
   }

   public WorldGenBigMushroom() {
      super(false);
      this.mushroomType = null;
   }

   public boolean generate(World var1, Random var2, BlockPos var3) {
      Block var4 = this.mushroomType;
      if (var4 == null) {
         var4 = var2.nextBoolean() ? Blocks.BROWN_MUSHROOM_BLOCK : Blocks.RED_MUSHROOM_BLOCK;
      }

      int var5 = var2.nextInt(3) + 4;
      if (var2.nextInt(12) == 0) {
         var5 *= 2;
      }

      boolean var6 = true;
      if (var3.getY() >= 1 && var3.getY() + var5 + 1 < 256) {
         for(int var7 = var3.getY(); var7 <= var3.getY() + 1 + var5; ++var7) {
            byte var8 = 3;
            if (var7 <= var3.getY() + 3) {
               var8 = 0;
            }

            BlockPos.MutableBlockPos var9 = new BlockPos.MutableBlockPos();

            for(int var10 = var3.getX() - var8; var10 <= var3.getX() + var8 && var6; ++var10) {
               for(int var11 = var3.getZ() - var8; var11 <= var3.getZ() + var8 && var6; ++var11) {
                  if (var7 >= 0 && var7 < 256) {
                     Material var12 = var1.getBlockState(var9.setPos(var10, var7, var11)).getMaterial();
                     if (var12 != Material.AIR && var12 != Material.LEAVES) {
                        var6 = false;
                     }
                  } else {
                     var6 = false;
                  }
               }
            }
         }

         if (!var6) {
            return false;
         } else {
            Block var20 = var1.getBlockState(var3.down()).getBlock();
            if (var20 != Blocks.DIRT && var20 != Blocks.GRASS && var20 != Blocks.MYCELIUM) {
               return false;
            } else {
               int var21 = var3.getY() + var5;
               if (var4 == Blocks.RED_MUSHROOM_BLOCK) {
                  var21 = var3.getY() + var5 - 3;
               }

               for(int var22 = var21; var22 <= var3.getY() + var5; ++var22) {
                  int var24 = 1;
                  if (var22 < var3.getY() + var5) {
                     ++var24;
                  }

                  if (var4 == Blocks.BROWN_MUSHROOM_BLOCK) {
                     var24 = 3;
                  }

                  int var26 = var3.getX() - var24;
                  int var27 = var3.getX() + var24;
                  int var13 = var3.getZ() - var24;
                  int var14 = var3.getZ() + var24;

                  for(int var15 = var26; var15 <= var27; ++var15) {
                     for(int var16 = var13; var16 <= var14; ++var16) {
                        int var17 = 5;
                        if (var15 == var26) {
                           --var17;
                        } else if (var15 == var27) {
                           ++var17;
                        }

                        if (var16 == var13) {
                           var17 -= 3;
                        } else if (var16 == var14) {
                           var17 += 3;
                        }

                        BlockHugeMushroom.EnumType var18 = BlockHugeMushroom.EnumType.byMetadata(var17);
                        if (var4 == Blocks.BROWN_MUSHROOM_BLOCK || var22 < var3.getY() + var5) {
                           if ((var15 == var26 || var15 == var27) && (var16 == var13 || var16 == var14)) {
                              continue;
                           }

                           if (var15 == var3.getX() - (var24 - 1) && var16 == var13) {
                              var18 = BlockHugeMushroom.EnumType.NORTH_WEST;
                           }

                           if (var15 == var26 && var16 == var3.getZ() - (var24 - 1)) {
                              var18 = BlockHugeMushroom.EnumType.NORTH_WEST;
                           }

                           if (var15 == var3.getX() + (var24 - 1) && var16 == var13) {
                              var18 = BlockHugeMushroom.EnumType.NORTH_EAST;
                           }

                           if (var15 == var27 && var16 == var3.getZ() - (var24 - 1)) {
                              var18 = BlockHugeMushroom.EnumType.NORTH_EAST;
                           }

                           if (var15 == var3.getX() - (var24 - 1) && var16 == var14) {
                              var18 = BlockHugeMushroom.EnumType.SOUTH_WEST;
                           }

                           if (var15 == var26 && var16 == var3.getZ() + (var24 - 1)) {
                              var18 = BlockHugeMushroom.EnumType.SOUTH_WEST;
                           }

                           if (var15 == var3.getX() + (var24 - 1) && var16 == var14) {
                              var18 = BlockHugeMushroom.EnumType.SOUTH_EAST;
                           }

                           if (var15 == var27 && var16 == var3.getZ() + (var24 - 1)) {
                              var18 = BlockHugeMushroom.EnumType.SOUTH_EAST;
                           }
                        }

                        if (var18 == BlockHugeMushroom.EnumType.CENTER && var22 < var3.getY() + var5) {
                           var18 = BlockHugeMushroom.EnumType.ALL_INSIDE;
                        }

                        if (var3.getY() >= var3.getY() + var5 - 1 || var18 != BlockHugeMushroom.EnumType.ALL_INSIDE) {
                           BlockPos var19 = new BlockPos(var15, var22, var16);
                           if (!var1.getBlockState(var19).isFullBlock()) {
                              this.setBlockAndNotifyAdequately(var1, var19, var4.getDefaultState().withProperty(BlockHugeMushroom.VARIANT, var18));
                           }
                        }
                     }
                  }
               }

               for(int var23 = 0; var23 < var5; ++var23) {
                  IBlockState var25 = var1.getBlockState(var3.up(var23));
                  if (!var25.isFullBlock()) {
                     this.setBlockAndNotifyAdequately(var1, var3.up(var23), var4.getDefaultState().withProperty(BlockHugeMushroom.VARIANT, BlockHugeMushroom.EnumType.STEM));
                  }
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }
}
