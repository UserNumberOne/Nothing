package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockVine;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class WorldGenMegaJungle extends WorldGenHugeTrees {
   public WorldGenMegaJungle(boolean var1, int var2, int var3, IBlockState var4, IBlockState var5) {
      super(var1, var2, var3, var4, var5);
   }

   public boolean generate(World var1, Random var2, BlockPos var3) {
      int var4 = this.getHeight(var2);
      if (!this.ensureGrowable(var1, var2, var3, var4)) {
         return false;
      } else {
         this.createCrown(var1, var3.up(var4), 2);

         for(int var5 = var3.getY() + var4 - 2 - var2.nextInt(4); var5 > var3.getY() + var4 / 2; var5 -= 2 + var2.nextInt(4)) {
            float var6 = var2.nextFloat() * 6.2831855F;
            int var7 = var3.getX() + (int)(0.5F + MathHelper.cos(var6) * 4.0F);
            int var8 = var3.getZ() + (int)(0.5F + MathHelper.sin(var6) * 4.0F);

            for(int var9 = 0; var9 < 5; ++var9) {
               var7 = var3.getX() + (int)(1.5F + MathHelper.cos(var6) * (float)var9);
               var8 = var3.getZ() + (int)(1.5F + MathHelper.sin(var6) * (float)var9);
               this.setBlockAndNotifyAdequately(var1, new BlockPos(var7, var5 - 3 + var9 / 2, var8), this.woodMetadata);
            }

            int var16 = 1 + var2.nextInt(2);
            int var10 = var5;

            for(int var11 = var5 - var16; var11 <= var10; ++var11) {
               int var12 = var11 - var10;
               this.growLeavesLayer(var1, new BlockPos(var7, var11, var8), 1 - var12);
            }
         }

         for(int var13 = 0; var13 < var4; ++var13) {
            BlockPos var14 = var3.up(var13);
            if (this.canGrowInto(var1.getBlockState(var14).getBlock())) {
               this.setBlockAndNotifyAdequately(var1, var14, this.woodMetadata);
               if (var13 > 0) {
                  this.placeVine(var1, var2, var14.west(), BlockVine.EAST);
                  this.placeVine(var1, var2, var14.north(), BlockVine.SOUTH);
               }
            }

            if (var13 < var4 - 1) {
               BlockPos var15 = var14.east();
               if (this.canGrowInto(var1.getBlockState(var15).getBlock())) {
                  this.setBlockAndNotifyAdequately(var1, var15, this.woodMetadata);
                  if (var13 > 0) {
                     this.placeVine(var1, var2, var15.east(), BlockVine.WEST);
                     this.placeVine(var1, var2, var15.north(), BlockVine.SOUTH);
                  }
               }

               BlockPos var17 = var14.south().east();
               if (this.canGrowInto(var1.getBlockState(var17).getBlock())) {
                  this.setBlockAndNotifyAdequately(var1, var17, this.woodMetadata);
                  if (var13 > 0) {
                     this.placeVine(var1, var2, var17.east(), BlockVine.WEST);
                     this.placeVine(var1, var2, var17.south(), BlockVine.NORTH);
                  }
               }

               BlockPos var18 = var14.south();
               if (this.canGrowInto(var1.getBlockState(var18).getBlock())) {
                  this.setBlockAndNotifyAdequately(var1, var18, this.woodMetadata);
                  if (var13 > 0) {
                     this.placeVine(var1, var2, var18.west(), BlockVine.EAST);
                     this.placeVine(var1, var2, var18.south(), BlockVine.NORTH);
                  }
               }
            }
         }

         return true;
      }
   }

   private void placeVine(World var1, Random var2, BlockPos var3, PropertyBool var4) {
      if (var2.nextInt(3) > 0 && var1.isAirBlock(var3)) {
         this.setBlockAndNotifyAdequately(var1, var3, Blocks.VINE.getDefaultState().withProperty(var4, Boolean.valueOf(true)));
      }

   }

   private void createCrown(World var1, BlockPos var2, int var3) {
      boolean var4 = true;

      for(int var5 = -2; var5 <= 0; ++var5) {
         this.growLeavesLayerStrict(var1, var2.up(var5), var3 + 1 - var5);
      }

   }
}
