package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class WorldGenEndIsland extends WorldGenerator {
   public boolean generate(World var1, Random var2, BlockPos var3) {
      float var4 = (float)(var2.nextInt(3) + 4);

      for(int var5 = 0; var4 > 0.5F; --var5) {
         for(int var6 = MathHelper.floor(-var4); var6 <= MathHelper.ceil(var4); ++var6) {
            for(int var7 = MathHelper.floor(-var4); var7 <= MathHelper.ceil(var4); ++var7) {
               if ((float)(var6 * var6 + var7 * var7) <= (var4 + 1.0F) * (var4 + 1.0F)) {
                  this.setBlockAndNotifyAdequately(var1, var3.add(var6, var5, var7), Blocks.END_STONE.getDefaultState());
               }
            }
         }

         var4 = (float)((double)var4 - ((double)var2.nextInt(2) + 0.5D));
      }

      return true;
   }
}
