package net.minecraft.world.biome;

import java.util.Random;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.event.terraingen.OreGenEvent.Post;
import net.minecraftforge.event.terraingen.OreGenEvent.Pre;
import net.minecraftforge.event.terraingen.OreGenEvent.GenerateMinable.EventType;

public class BiomeHills extends Biome {
   private final WorldGenerator theWorldGenerator = new WorldGenMinable(Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, BlockSilverfish.EnumType.STONE), 9);
   private final WorldGenTaiga2 spruceGenerator = new WorldGenTaiga2(false);
   private final BiomeHills.Type type;

   public BiomeHills(BiomeHills.Type p_i46710_1_, Biome.BiomeProperties properties) {
      super(properties);
      if (p_i46710_1_ == BiomeHills.Type.EXTRA_TREES) {
         this.theBiomeDecorator.treesPerChunk = 3;
      }

      this.type = p_i46710_1_;
   }

   public WorldGenAbstractTree genBigTreeChance(Random rand) {
      return (WorldGenAbstractTree)(rand.nextInt(3) > 0 ? this.spruceGenerator : super.genBigTreeChance(rand));
   }

   public void decorate(World worldIn, Random rand, BlockPos pos) {
      super.decorate(worldIn, rand, pos);
      MinecraftForge.ORE_GEN_BUS.post(new Pre(worldIn, rand, pos));
      WorldGenerator emeralds = new BiomeHills.EmeraldGenerator();
      if (TerrainGen.generateOre(worldIn, rand, emeralds, pos, EventType.EMERALD)) {
         emeralds.generate(worldIn, rand, pos);
      }

      for(int i = 0; i < 7; ++i) {
         int j1 = rand.nextInt(16);
         int k1 = rand.nextInt(64);
         int l1 = rand.nextInt(16);
         if (TerrainGen.generateOre(worldIn, rand, this.theWorldGenerator, pos.add(j1, k1, l1), EventType.SILVERFISH)) {
            this.theWorldGenerator.generate(worldIn, rand, pos.add(j1, k1, l1));
         }
      }

      MinecraftForge.ORE_GEN_BUS.post(new Post(worldIn, rand, pos));
   }

   public void genTerrainBlocks(World worldIn, Random rand, ChunkPrimer chunkPrimerIn, int x, int z, double noiseVal) {
      this.topBlock = Blocks.GRASS.getDefaultState();
      this.fillerBlock = Blocks.DIRT.getDefaultState();
      if ((noiseVal < -1.0D || noiseVal > 2.0D) && this.type == BiomeHills.Type.MUTATED) {
         this.topBlock = Blocks.GRAVEL.getDefaultState();
         this.fillerBlock = Blocks.GRAVEL.getDefaultState();
      } else if (noiseVal > 1.0D && this.type != BiomeHills.Type.EXTRA_TREES) {
         this.topBlock = Blocks.STONE.getDefaultState();
         this.fillerBlock = Blocks.STONE.getDefaultState();
      }

      this.generateBiomeTerrain(worldIn, rand, chunkPrimerIn, x, z, noiseVal);
   }

   private static class EmeraldGenerator extends WorldGenerator {
      private EmeraldGenerator() {
      }

      public boolean generate(World worldIn, Random rand, BlockPos pos) {
         int count = 3 + rand.nextInt(6);

         for(int i = 0; i < count; ++i) {
            BlockPos blockpos = pos.add(rand.nextInt(16), rand.nextInt(28) + 4, rand.nextInt(16));
            IBlockState state = worldIn.getBlockState(blockpos);
            if (state.getBlock().isReplaceableOreGen(state, worldIn, blockpos, BlockMatcher.forBlock(Blocks.STONE))) {
               worldIn.setBlockState(blockpos, Blocks.EMERALD_ORE.getDefaultState(), 2);
            }
         }

         return true;
      }
   }

   public static enum Type {
      NORMAL,
      EXTRA_TREES,
      MUTATED;
   }
}
