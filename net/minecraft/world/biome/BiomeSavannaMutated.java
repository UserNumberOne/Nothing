package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.block.BlockDirt;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public class BiomeSavannaMutated extends BiomeSavanna {
   public BiomeSavannaMutated(Biome.BiomeProperties var1) {
      super(properties);
      this.theBiomeDecorator.treesPerChunk = 2;
      this.theBiomeDecorator.flowersPerChunk = 2;
      this.theBiomeDecorator.grassPerChunk = 5;
   }

   public void genTerrainBlocks(World var1, Random var2, ChunkPrimer var3, int var4, int var5, double var6) {
      this.topBlock = Blocks.GRASS.getDefaultState();
      this.fillerBlock = Blocks.DIRT.getDefaultState();
      if (noiseVal > 1.75D) {
         this.topBlock = Blocks.STONE.getDefaultState();
         this.fillerBlock = Blocks.STONE.getDefaultState();
      } else if (noiseVal > -0.5D) {
         this.topBlock = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
      }

      this.generateBiomeTerrain(worldIn, rand, chunkPrimerIn, x, z, noiseVal);
   }

   public void decorate(World var1, Random var2, BlockPos var3) {
      this.theBiomeDecorator.decorate(worldIn, rand, this, pos);
   }
}
