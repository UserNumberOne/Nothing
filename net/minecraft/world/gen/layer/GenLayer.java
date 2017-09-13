package net.minecraft.world.gen.layer;

import net.minecraft.init.Biomes;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkProviderSettings;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.WorldTypeEvent.BiomeSize;

public abstract class GenLayer {
   private long worldGenSeed;
   protected GenLayer parent;
   private long chunkSeed;
   protected long baseSeed;

   public static GenLayer[] initializeAllBiomeGenerators(long var0, WorldType var2, String var3) {
      GenLayer genlayer = new GenLayerIsland(1L);
      genlayer = new GenLayerFuzzyZoom(2000L, genlayer);
      GenLayerAddIsland genlayeraddisland = new GenLayerAddIsland(1L, genlayer);
      GenLayerZoom genlayerzoom = new GenLayerZoom(2001L, genlayeraddisland);
      GenLayerAddIsland genlayeraddisland1 = new GenLayerAddIsland(2L, genlayerzoom);
      genlayeraddisland1 = new GenLayerAddIsland(50L, genlayeraddisland1);
      genlayeraddisland1 = new GenLayerAddIsland(70L, genlayeraddisland1);
      GenLayerRemoveTooMuchOcean genlayerremovetoomuchocean = new GenLayerRemoveTooMuchOcean(2L, genlayeraddisland1);
      GenLayerAddSnow genlayeraddsnow = new GenLayerAddSnow(2L, genlayerremovetoomuchocean);
      GenLayerAddIsland genlayeraddisland2 = new GenLayerAddIsland(3L, genlayeraddsnow);
      GenLayerEdge genlayeredge = new GenLayerEdge(2L, genlayeraddisland2, GenLayerEdge.Mode.COOL_WARM);
      genlayeredge = new GenLayerEdge(2L, genlayeredge, GenLayerEdge.Mode.HEAT_ICE);
      genlayeredge = new GenLayerEdge(3L, genlayeredge, GenLayerEdge.Mode.SPECIAL);
      GenLayerZoom genlayerzoom1 = new GenLayerZoom(2002L, genlayeredge);
      genlayerzoom1 = new GenLayerZoom(2003L, genlayerzoom1);
      GenLayerAddIsland genlayeraddisland3 = new GenLayerAddIsland(4L, genlayerzoom1);
      GenLayerAddMushroomIsland genlayeraddmushroomisland = new GenLayerAddMushroomIsland(5L, genlayeraddisland3);
      GenLayerDeepOcean genlayerdeepocean = new GenLayerDeepOcean(4L, genlayeraddmushroomisland);
      GenLayer genlayer4 = GenLayerZoom.magnify(1000L, genlayerdeepocean, 0);
      int i = 4;
      int j = i;
      if (p_180781_2_ == WorldType.CUSTOMIZED && !p_180781_3_.isEmpty()) {
         ChunkProviderSettings chunkprovidersettings = ChunkProviderSettings.Factory.jsonToFactory(p_180781_3_).build();
         i = chunkprovidersettings.biomeSize;
         j = chunkprovidersettings.riverSize;
      }

      if (p_180781_2_ == WorldType.LARGE_BIOMES) {
         i = 6;
      }

      i = getModdedBiomeSize(p_180781_2_, i);
      GenLayer lvt_8_1_ = GenLayerZoom.magnify(1000L, genlayer4, 0);
      GenLayerRiverInit genlayerriverinit = new GenLayerRiverInit(100L, lvt_8_1_);
      GenLayer lvt_10_1_ = GenLayerZoom.magnify(1000L, genlayerriverinit, 2);
      GenLayer genlayerbiomeedge = p_180781_2_.getBiomeLayer(seed, genlayer4, p_180781_3_);
      GenLayer genlayerhills = new GenLayerHills(1000L, genlayerbiomeedge, lvt_10_1_);
      GenLayer genlayer5 = GenLayerZoom.magnify(1000L, genlayerriverinit, 2);
      genlayer5 = GenLayerZoom.magnify(1000L, genlayer5, j);
      GenLayerRiver genlayerriver = new GenLayerRiver(1L, genlayer5);
      GenLayerSmooth genlayersmooth = new GenLayerSmooth(1000L, genlayerriver);
      genlayerhills = new GenLayerRareBiome(1001L, genlayerhills);

      for(int k = 0; k < i; ++k) {
         genlayerhills = new GenLayerZoom((long)(1000 + k), genlayerhills);
         if (k == 0) {
            genlayerhills = new GenLayerAddIsland(3L, genlayerhills);
         }

         if (k == 1 || i == 1) {
            genlayerhills = new GenLayerShore(1000L, genlayerhills);
         }
      }

      GenLayerSmooth genlayersmooth1 = new GenLayerSmooth(1000L, genlayerhills);
      GenLayerRiverMix genlayerrivermix = new GenLayerRiverMix(100L, genlayersmooth1, genlayersmooth);
      GenLayer genlayer3 = new GenLayerVoronoiZoom(10L, genlayerrivermix);
      genlayerrivermix.initWorldGenSeed(seed);
      genlayer3.initWorldGenSeed(seed);
      return new GenLayer[]{genlayerrivermix, genlayer3, genlayerrivermix};
   }

   public GenLayer(long var1) {
      this.baseSeed = p_i2125_1_;
      this.baseSeed *= this.baseSeed * 6364136223846793005L + 1442695040888963407L;
      this.baseSeed += p_i2125_1_;
      this.baseSeed *= this.baseSeed * 6364136223846793005L + 1442695040888963407L;
      this.baseSeed += p_i2125_1_;
      this.baseSeed *= this.baseSeed * 6364136223846793005L + 1442695040888963407L;
      this.baseSeed += p_i2125_1_;
   }

   public void initWorldGenSeed(long var1) {
      this.worldGenSeed = seed;
      if (this.parent != null) {
         this.parent.initWorldGenSeed(seed);
      }

      this.worldGenSeed *= this.worldGenSeed * 6364136223846793005L + 1442695040888963407L;
      this.worldGenSeed += this.baseSeed;
      this.worldGenSeed *= this.worldGenSeed * 6364136223846793005L + 1442695040888963407L;
      this.worldGenSeed += this.baseSeed;
      this.worldGenSeed *= this.worldGenSeed * 6364136223846793005L + 1442695040888963407L;
      this.worldGenSeed += this.baseSeed;
   }

   public void initChunkSeed(long var1, long var3) {
      this.chunkSeed = this.worldGenSeed;
      this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
      this.chunkSeed += p_75903_1_;
      this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
      this.chunkSeed += p_75903_3_;
      this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
      this.chunkSeed += p_75903_1_;
      this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
      this.chunkSeed += p_75903_3_;
   }

   protected int nextInt(int var1) {
      int i = (int)((this.chunkSeed >> 24) % (long)p_75902_1_);
      if (i < 0) {
         i += p_75902_1_;
      }

      this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
      this.chunkSeed += this.worldGenSeed;
      return i;
   }

   public abstract int[] getInts(int var1, int var2, int var3, int var4);

   protected static boolean biomesEqualOrMesaPlateau(int var0, int var1) {
      if (biomeIDA == biomeIDB) {
         return true;
      } else {
         Biome biome = Biome.getBiome(biomeIDA);
         Biome biome1 = Biome.getBiome(biomeIDB);
         return biome != null && biome1 != null ? (biome != Biomes.MESA_ROCK && biome != Biomes.MESA_CLEAR_ROCK ? biome == biome1 || biome.getBiomeClass() == biome1.getBiomeClass() : biome1 == Biomes.MESA_ROCK || biome1 == Biomes.MESA_CLEAR_ROCK) : false;
      }
   }

   protected static boolean isBiomeOceanic(int var0) {
      return BiomeManager.oceanBiomes.contains(Biome.getBiome(p_151618_0_));
   }

   protected int selectRandom(int... var1) {
      return p_151619_1_[this.nextInt(p_151619_1_.length)];
   }

   protected int selectModeOrRandom(int var1, int var2, int var3, int var4) {
      return p_151617_2_ == p_151617_3_ && p_151617_3_ == p_151617_4_ ? p_151617_2_ : (p_151617_1_ == p_151617_2_ && p_151617_1_ == p_151617_3_ ? p_151617_1_ : (p_151617_1_ == p_151617_2_ && p_151617_1_ == p_151617_4_ ? p_151617_1_ : (p_151617_1_ == p_151617_3_ && p_151617_1_ == p_151617_4_ ? p_151617_1_ : (p_151617_1_ == p_151617_2_ && p_151617_3_ != p_151617_4_ ? p_151617_1_ : (p_151617_1_ == p_151617_3_ && p_151617_2_ != p_151617_4_ ? p_151617_1_ : (p_151617_1_ == p_151617_4_ && p_151617_2_ != p_151617_3_ ? p_151617_1_ : (p_151617_2_ == p_151617_3_ && p_151617_1_ != p_151617_4_ ? p_151617_2_ : (p_151617_2_ == p_151617_4_ && p_151617_1_ != p_151617_3_ ? p_151617_2_ : (p_151617_3_ == p_151617_4_ && p_151617_1_ != p_151617_2_ ? p_151617_3_ : this.selectRandom(p_151617_1_, p_151617_2_, p_151617_3_, p_151617_4_))))))))));
   }

   protected long nextLong(long var1) {
      long j = (this.chunkSeed >> 24) % par1;
      if (j < 0L) {
         j += par1;
      }

      this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
      this.chunkSeed += this.worldGenSeed;
      return j;
   }

   public static int getModdedBiomeSize(WorldType var0, int var1) {
      BiomeSize event = new BiomeSize(worldType, original);
      MinecraftForge.TERRAIN_GEN_BUS.post(event);
      return event.getNewSize();
   }
}
