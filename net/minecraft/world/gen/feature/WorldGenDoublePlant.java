package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenDoublePlant extends WorldGenerator {
   private BlockDoublePlant.EnumPlantType plantType;

   public void setPlantType(BlockDoublePlant.EnumPlantType var1) {
      this.plantType = plantTypeIn;
   }

   public boolean generate(World var1, Random var2, BlockPos var3) {
      boolean flag = false;

      for(int i = 0; i < 64; ++i) {
         BlockPos blockpos = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));
         if (worldIn.isAirBlock(blockpos) && (!worldIn.provider.hasNoSky() || blockpos.getY() < 254) && Blocks.DOUBLE_PLANT.canPlaceBlockAt(worldIn, blockpos)) {
            Blocks.DOUBLE_PLANT.placeAt(worldIn, blockpos, this.plantType, 2);
            flag = true;
         }
      }

      return flag;
   }
}
