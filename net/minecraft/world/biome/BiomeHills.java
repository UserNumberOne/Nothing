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

   public BiomeHills(BiomeHills.Type var1, Biome.BiomeProperties var2) {
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
      MinecraftForge.ORE_GEN_BUS.post(new Pre(var1, var2, var3));
      BiomeHills.EmeraldGenerator var4 = new BiomeHills.EmeraldGenerator();
      if (TerrainGen.generateOre(var1, var2, var4, var3, EventType.EMERALD)) {
         var4.generate(var1, var2, var3);
      }

      for(int var5 = 0; var5 < 7; ++var5) {
         int var6 = var2.nextInt(16);
         int var7 = var2.nextInt(64);
         int var8 = var2.nextInt(16);
         if (TerrainGen.generateOre(var1, var2, this.theWorldGenerator, var3.add(var6, var7, var8), EventType.SILVERFISH)) {
            this.theWorldGenerator.generate(var1, var2, var3.add(var6, var7, var8));
         }
      }

      MinecraftForge.ORE_GEN_BUS.post(new Post(var1, var2, var3));
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

   private static class EmeraldGenerator extends WorldGenerator {
      private EmeraldGenerator() {
      }

      public boolean generate(World var1, Random var2, BlockPos var3) {
         int var4 = 3 + var2.nextInt(6);

         for(int var5 = 0; var5 < var4; ++var5) {
            BlockPos var6 = var3.add(var2.nextInt(16), var2.nextInt(28) + 4, var2.nextInt(16));
            IBlockState var7 = var1.getBlockState(var6);
            if (var7.getBlock().isReplaceableOreGen(var7, var1, var6, BlockMatcher.forBlock(Blocks.STONE))) {
               var1.setBlockState(var6, Blocks.EMERALD_ORE.getDefaultState(), 2);
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
