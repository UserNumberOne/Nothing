package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenBlockBlob extends WorldGenerator {
   private final Block block;
   private final int startRadius;

   public WorldGenBlockBlob(Block var1, int var2) {
      super(false);
      this.block = var1;
      this.startRadius = var2;
   }

   public boolean generate(World var1, Random var2, BlockPos var3) {
      while(true) {
         label50: {
            if (var3.getY() > 3) {
               if (var1.isAirBlock(var3.down())) {
                  break label50;
               }

               Block var4 = var1.getBlockState(var3.down()).getBlock();
               if (var4 != Blocks.GRASS && var4 != Blocks.DIRT && var4 != Blocks.STONE) {
                  break label50;
               }
            }

            if (var3.getY() <= 3) {
               return false;
            }

            int var12 = this.startRadius;

            for(int var5 = 0; var12 >= 0 && var5 < 3; ++var5) {
               int var6 = var12 + var2.nextInt(2);
               int var7 = var12 + var2.nextInt(2);
               int var8 = var12 + var2.nextInt(2);
               float var9 = (float)(var6 + var7 + var8) * 0.333F + 0.5F;

               for(BlockPos var11 : BlockPos.getAllInBox(var3.add(-var6, -var7, -var8), var3.add(var6, var7, var8))) {
                  if (var11.distanceSq(var3) <= (double)(var9 * var9)) {
                     var1.setBlockState(var11, this.block.getDefaultState(), 4);
                  }
               }

               var3 = var3.add(-(var12 + 1) + var2.nextInt(2 + var12 * 2), 0 - var2.nextInt(2), -(var12 + 1) + var2.nextInt(2 + var12 * 2));
            }

            return true;
         }

         var3 = var3.down();
      }
   }
}
