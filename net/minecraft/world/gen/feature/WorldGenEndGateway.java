package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenEndGateway extends WorldGenerator {
   public boolean generate(World var1, Random var2, BlockPos var3) {
      for(BlockPos.MutableBlockPos var5 : BlockPos.getAllInBoxMutable(var3.add(-1, -2, -1), var3.add(1, 2, 1))) {
         boolean var6 = var5.getX() == var3.getX();
         boolean var7 = var5.getY() == var3.getY();
         boolean var8 = var5.getZ() == var3.getZ();
         boolean var9 = Math.abs(var5.getY() - var3.getY()) == 2;
         if (var6 && var7 && var8) {
            this.setBlockAndNotifyAdequately(var1, new BlockPos(var5), Blocks.END_GATEWAY.getDefaultState());
         } else if (var7) {
            this.setBlockAndNotifyAdequately(var1, var5, Blocks.AIR.getDefaultState());
         } else if (var9 && var6 && var8) {
            this.setBlockAndNotifyAdequately(var1, var5, Blocks.BEDROCK.getDefaultState());
         } else if ((var6 || var8) && !var9) {
            this.setBlockAndNotifyAdequately(var1, var5, Blocks.BEDROCK.getDefaultState());
         } else {
            this.setBlockAndNotifyAdequately(var1, var5, Blocks.AIR.getDefaultState());
         }
      }

      return true;
   }
}
