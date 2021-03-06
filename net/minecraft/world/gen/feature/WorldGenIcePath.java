package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenIcePath extends WorldGenerator {
   private final Block block = Blocks.PACKED_ICE;
   private final int basePathWidth;

   public WorldGenIcePath(int var1) {
      this.basePathWidth = var1;
   }

   public boolean generate(World var1, Random var2, BlockPos var3) {
      while(var1.isAirBlock(var3) && var3.getY() > 2) {
         var3 = var3.down();
      }

      if (var1.getBlockState(var3).getBlock() != Blocks.SNOW) {
         return false;
      } else {
         int var4 = var2.nextInt(this.basePathWidth - 2) + 2;
         boolean var5 = true;

         for(int var6 = var3.getX() - var4; var6 <= var3.getX() + var4; ++var6) {
            for(int var7 = var3.getZ() - var4; var7 <= var3.getZ() + var4; ++var7) {
               int var8 = var6 - var3.getX();
               int var9 = var7 - var3.getZ();
               if (var8 * var8 + var9 * var9 <= var4 * var4) {
                  for(int var10 = var3.getY() - 1; var10 <= var3.getY() + 1; ++var10) {
                     BlockPos var11 = new BlockPos(var6, var10, var7);
                     Block var12 = var1.getBlockState(var11).getBlock();
                     if (var12 == Blocks.DIRT || var12 == Blocks.SNOW || var12 == Blocks.ICE) {
                        var1.setBlockState(var11, this.block.getDefaultState(), 2);
                     }
                  }
               }
            }
         }

         return true;
      }
   }
}
