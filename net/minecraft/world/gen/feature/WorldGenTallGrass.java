package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenTallGrass extends WorldGenerator {
   private final IBlockState tallGrassState;

   public WorldGenTallGrass(BlockTallGrass.EnumType var1) {
      this.tallGrassState = Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, var1);
   }

   public boolean generate(World var1, Random var2, BlockPos var3) {
      for(IBlockState var4 = var1.getBlockState(var3); (var4.getMaterial() == Material.AIR || var4.getMaterial() == Material.LEAVES) && var3.getY() > 0; var4 = var1.getBlockState(var3)) {
         var3 = var3.down();
      }

      for(int var5 = 0; var5 < 128; ++var5) {
         BlockPos var6 = var3.add(var2.nextInt(8) - var2.nextInt(8), var2.nextInt(4) - var2.nextInt(4), var2.nextInt(8) - var2.nextInt(8));
         if (var1.isAirBlock(var6) && Blocks.TALLGRASS.canBlockStay(var1, var6, this.tallGrassState)) {
            var1.setBlockState(var6, this.tallGrassState, 2);
         }
      }

      return true;
   }
}
