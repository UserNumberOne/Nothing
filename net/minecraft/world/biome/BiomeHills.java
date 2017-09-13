package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenerator;

public class BiomeHills extends Biome {
   private final WorldGenerator theWorldGenerator = new WorldGenMinable(Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, BlockSilverfish.EnumType.STONE), 9);
   private final WorldGenTaiga2 spruceGenerator = new WorldGenTaiga2(false);
   private final BiomeHills.Type type;

   protected BiomeHills(BiomeHills.Type var1, Biome.BiomeProperties var2) {
      super(var2);
      if (var1 == BiomeHills.Type.EXTRA_TREES) {
         this.theBiomeDecorator.treesPerChunk = 3;
      }

      this.type = var1;
   }

   public WorldGenAbstractTree genBigTreeChance(Random var1) {
      return (WorldGenAbstractTree)(var1.nextInt(3) > 0 ? this.spruceGenerator : super.genBigTreeChance(var1));
   }

   public void decorate(World var1, Random var2, BlockPos var3) {
      super.decorate(var1, var2, var3);
      int var4 = 3 + var2.nextInt(6);

      for(int var5 = 0; var5 < var4; ++var5) {
         int var6 = var2.nextInt(16);
         int var7 = var2.nextInt(28) + 4;
         int var8 = var2.nextInt(16);
         BlockPos var9 = var3.add(var6, var7, var8);
         if (var1.getBlockState(var9).getBlock() == Blocks.STONE) {
            var1.setBlockState(var9, Blocks.EMERALD_ORE.getDefaultState(), 2);
         }
      }

      for(int var10 = 0; var10 < 7; ++var10) {
         int var11 = var2.nextInt(16);
         int var12 = var2.nextInt(64);
         int var13 = var2.nextInt(16);
         this.theWorldGenerator.generate(var1, var2, var3.add(var11, var12, var13));
      }

   }

   public void genTerrainBlocks(World var1, Random var2, ChunkPrimer var3, int var4, int var5, double var6) {
      this.topBlock = Blocks.GRASS.getDefaultState();
      this.fillerBlock = Blocks.DIRT.getDefaultState();
      if ((var6 < -1.0D || var6 > 2.0D) && this.type == BiomeHills.Type.MUTATED) {
         this.topBlock = Blocks.GRAVEL.getDefaultState();
         this.fillerBlock = Blocks.GRAVEL.getDefaultState();
      } else if (var6 > 1.0D && this.type != BiomeHills.Type.EXTRA_TREES) {
         this.topBlock = Blocks.STONE.getDefaultState();
         this.fillerBlock = Blocks.STONE.getDefaultState();
      }

      this.generateBiomeTerrain(var1, var2, var3, var4, var5, var6);
   }

   public static enum Type {
      NORMAL,
      EXTRA_TREES,
      MUTATED;
   }
}
