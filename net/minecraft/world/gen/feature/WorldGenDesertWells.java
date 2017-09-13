package net.minecraft.world.gen.feature;

import com.google.common.base.Predicates;
import java.util.Random;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenDesertWells extends WorldGenerator {
   private static final BlockStateMatcher IS_SAND = BlockStateMatcher.forBlock(Blocks.SAND).where(BlockSand.VARIANT, Predicates.equalTo(BlockSand.EnumType.SAND));
   private final IBlockState sandSlab = Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.SAND).withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.BOTTOM);
   private final IBlockState sandstone = Blocks.SANDSTONE.getDefaultState();
   private final IBlockState water = Blocks.FLOWING_WATER.getDefaultState();

   public boolean generate(World var1, Random var2, BlockPos var3) {
      while(var1.isAirBlock(var3) && var3.getY() > 2) {
         var3 = var3.down();
      }

      if (!IS_SAND.apply(var1.getBlockState(var3))) {
         return false;
      } else {
         for(int var4 = -2; var4 <= 2; ++var4) {
            for(int var5 = -2; var5 <= 2; ++var5) {
               if (var1.isAirBlock(var3.add(var4, -1, var5)) && var1.isAirBlock(var3.add(var4, -2, var5))) {
                  return false;
               }
            }
         }

         for(int var7 = -1; var7 <= 0; ++var7) {
            for(int var12 = -2; var12 <= 2; ++var12) {
               for(int var6 = -2; var6 <= 2; ++var6) {
                  var1.setBlockState(var3.add(var12, var7, var6), this.sandstone, 2);
               }
            }
         }

         var1.setBlockState(var3, this.water, 2);

         for(EnumFacing var13 : EnumFacing.Plane.HORIZONTAL) {
            var1.setBlockState(var3.offset(var13), this.water, 2);
         }

         for(int var9 = -2; var9 <= 2; ++var9) {
            for(int var14 = -2; var14 <= 2; ++var14) {
               if (var9 == -2 || var9 == 2 || var14 == -2 || var14 == 2) {
                  var1.setBlockState(var3.add(var9, 1, var14), this.sandstone, 2);
               }
            }
         }

         var1.setBlockState(var3.add(2, 1, 0), this.sandSlab, 2);
         var1.setBlockState(var3.add(-2, 1, 0), this.sandSlab, 2);
         var1.setBlockState(var3.add(0, 1, 2), this.sandSlab, 2);
         var1.setBlockState(var3.add(0, 1, -2), this.sandSlab, 2);

         for(int var10 = -1; var10 <= 1; ++var10) {
            for(int var15 = -1; var15 <= 1; ++var15) {
               if (var10 == 0 && var15 == 0) {
                  var1.setBlockState(var3.add(var10, 4, var15), this.sandstone, 2);
               } else {
                  var1.setBlockState(var3.add(var10, 4, var15), this.sandSlab, 2);
               }
            }
         }

         for(int var11 = 1; var11 <= 3; ++var11) {
            var1.setBlockState(var3.add(-1, var11, -1), this.sandstone, 2);
            var1.setBlockState(var3.add(-1, var11, 1), this.sandstone, 2);
            var1.setBlockState(var3.add(1, var11, -1), this.sandstone, 2);
            var1.setBlockState(var3.add(1, var11, 1), this.sandstone, 2);
         }

         return true;
      }
   }
}
