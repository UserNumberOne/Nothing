package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockVine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenVines extends WorldGenerator {
   public boolean generate(World var1, Random var2, BlockPos var3) {
      for(; var3.getY() < 128; var3 = var3.up()) {
         if (var1.isAirBlock(var3)) {
            for(EnumFacing var7 : EnumFacing.Plane.HORIZONTAL.facings()) {
               if (Blocks.VINE.canPlaceBlockOnSide(var1, var3, var7)) {
                  IBlockState var8 = Blocks.VINE.getDefaultState().withProperty(BlockVine.NORTH, Boolean.valueOf(var7 == EnumFacing.NORTH)).withProperty(BlockVine.EAST, Boolean.valueOf(var7 == EnumFacing.EAST)).withProperty(BlockVine.SOUTH, Boolean.valueOf(var7 == EnumFacing.SOUTH)).withProperty(BlockVine.WEST, Boolean.valueOf(var7 == EnumFacing.WEST));
                  var1.setBlockState(var3, var8, 2);
                  break;
               }
            }
         } else {
            var3 = var3.add(var2.nextInt(4) - var2.nextInt(4), 0, var2.nextInt(4) - var2.nextInt(4));
         }
      }

      return true;
   }
}
