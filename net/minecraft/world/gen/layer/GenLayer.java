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
      GenLayerIsland var4 = new GenLayerIsland(1L);
      GenLayerFuzzyZoom var30 = new GenLayerFuzzyZoom(2000L, var4);
      GenLayerAddIsland var5 = new GenLayerAddIsland(1L, var30);
      GenLayerZoom var6 = new GenLayerZoom(2001L, var5);
      GenLayerAddIsland var7 = new GenLayerAddIsland(2L, var6);
      var7 = new GenLayerAddIsland(50L, var7);
      var7 = new GenLayerAddIsland(70L, var7);
      GenLayerRemoveTooMuchOcean var8 = new GenLayerRemoveTooMuchOcean(2L, var7);
      GenLayerAddSnow var9 = new GenLayerAddSnow(2L, var8);
      GenLayerAddIsland var10 = new GenLayerAddIsland(3L, var9);
      GenLayerEdge var11 = new GenLayerEdge(2L, var10, GenLayerEdge.Mode.COOL_WARM);
      var11 = new GenLayerEdge(2L, var11, GenLayerEdge.Mode.HEAT_ICE);
      var11 = new GenLayerEdge(3L, var11, GenLayerEdge.Mode.SPECIAL);
      GenLayerZoom var12 = new GenLayerZoom(2002L, var11);
      var12 = new GenLayerZoom(2003L, var12);
      GenLayerAddIsland var13 = new GenLayerAddIsland(4L, var12);
      GenLayerAddMushroomIsland var14 = new GenLayerAddMushroomIsland(5L, var13);
      GenLayerDeepOcean var15 = new GenLayerDeepOcean(4L, var14);
      GenLayer var16 = GenLayerZoom.magnify(1000L, var15, 0);
      int var17 = 4;
      int var18 = var17;
      if (var2 == WorldType.CUSTOMIZED && !var3.isEmpty()) {
         ChunkProviderSettings var19 = ChunkProviderSettings.Factory.jsonToFactory(var3).build();
         var17 = var19.biomeSize;
         var18 = var19.riverSize;
      }

      if (var2 == WorldType.LARGE_BIOMES) {
         var17 = 6;
      }

      var17 = getModdedBiomeSize(var2, var17);
      GenLayer var37 = GenLayerZoom.magnify(1000L, var16, 0);
      GenLayerRiverInit var20 = new GenLayerRiverInit(100L, var37);
      GenLayer var21 = GenLayerZoom.magnify(1000L, var20, 2);
      GenLayer var22 = var2.getBiomeLayer(var0, var16, var3);
      GenLayerHills var23 = new GenLayerHills(1000L, var22, var21);
      GenLayer var24 = GenLayerZoom.magnify(1000L, var20, 2);
      var24 = GenLayerZoom.magnify(1000L, var24, var18);
      GenLayerRiver var25 = new GenLayerRiver(1L, var24);
      GenLayerSmooth var26 = new GenLayerSmooth(1000L, var25);
      var23 = new GenLayerRareBiome(1001L, var23);

      for(int var27 = 0; var27 < var17; ++var27) {
         var23 = new GenLayerZoom((long)(1000 + var27), var23);
         if (var27 == 0) {
            var23 = new GenLayerAddIsland(3L, var23);
         }

         if (var27 == 1 || var17 == 1) {
            var23 = new GenLayerShore(1000L, var23);
         }
      }

      GenLayerSmooth var40 = new GenLayerSmooth(1000L, var23);
      GenLayerRiverMix var28 = new GenLayerRiverMix(100L, var40, var26);
      GenLayerVoronoiZoom var29 = new GenLayerVoronoiZoom(10L, var28);
      var28.initWorldGenSeed(var0);
      var29.initWorldGenSeed(var0);
      return new GenLayer[]{var28, var29, var28};
   }

   public GenLayer(long var1) {
      this.baseSeed = var1;
      this.baseSeed *= this.baseSeed * 6364136223846793005L + 1442695040888963407L;
      this.baseSeed += var1;
      this.baseSeed *= this.baseSeed * 6364136223846793005L + 1442695040888963407L;
      this.baseSeed += var1;
      this.baseSeed *= this.baseSeed * 6364136223846793005L + 1442695040888963407L;
      this.baseSeed += var1;
   }

   public void initWorldGenSeed(long var1) {
      this.worldGenSeed = var1;
      if (this.parent != null) {
         this.parent.initWorldGenSeed(var1);
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
      this.chunkSeed += var1;
      this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
      this.chunkSeed += var3;
      this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
      this.chunkSeed += var1;
      this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
      this.chunkSeed += var3;
   }

   protected int nextInt(int var1) {
      int var2 = (int)((this.chunkSeed >> 24) % (long)var1);
      if (var2 < 0) {
         var2 += var1;
      }

      this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
      this.chunkSeed += this.worldGenSeed;
      return var2;
   }

   public abstract int[] getInts(int var1, int var2, int var3, int var4);

   protected static boolean biomesEqualOrMesaPlateau(int var0, int var1) {
      if (var0 == var1) {
         return true;
      } else {
         Biome var2 = Biome.getBiome(var0);
         Biome var3 = Biome.getBiome(var1);
         return var2 != null && var3 != null ? (var2 != Biomes.MESA_ROCK && var2 != Biomes.MESA_CLEAR_ROCK ? var2 == var3 || var2.getBiomeClass() == var3.getBiomeClass() : var3 == Biomes.MESA_ROCK || var3 == Biomes.MESA_CLEAR_ROCK) : false;
      }
   }

   protected static boolean isBiomeOceanic(int var0) {
      return BiomeManager.oceanBiomes.contains(Biome.getBiome(var0));
   }

   protected int selectRandom(int... var1) {
      return var1[this.nextInt(var1.length)];
   }

   protected int selectModeOrRandom(int var1, int var2, int var3, int var4) {
      return var2 == var3 && var3 == var4 ? var2 : (var1 == var2 && var1 == var3 ? var1 : (var1 == var2 && var1 == var4 ? var1 : (var1 == var3 && var1 == var4 ? var1 : (var1 == var2 && var3 != var4 ? var1 : (var1 == var3 && var2 != var4 ? var1 : (var1 == var4 && var2 != var3 ? var1 : (var2 == var3 && var1 != var4 ? var2 : (var2 == var4 && var1 != var3 ? var2 : (var3 == var4 && var1 != var2 ? var3 : this.selectRandom(var1, var2, var3, var4))))))))));
   }

   protected long nextLong(long var1) {
      long var3 = (this.chunkSeed >> 24) % var1;
      if (var3 < 0L) {
         var3 += var1;
      }

      this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
      this.chunkSeed += this.worldGenSeed;
      return var3;
   }

   public static int getModdedBiomeSize(WorldType var0, int var1) {
      BiomeSize var2 = new BiomeSize(var0, var1);
      MinecraftForge.TERRAIN_GEN_BUS.post(var2);
      return var2.getNewSize();
   }
}
