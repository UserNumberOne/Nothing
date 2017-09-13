package net.minecraft.world.gen.layer;

import net.minecraft.init.Biomes;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkProviderSettings;

public abstract class GenLayer {
   private long worldGenSeed;
   protected GenLayer parent;
   private long chunkSeed;
   protected long baseSeed;

   public static GenLayer[] initializeAllBiomeGenerators(long var0, WorldType var2, String var3) {
      GenLayerIsland var4 = new GenLayerIsland(1L);
      GenLayerFuzzyZoom var13 = new GenLayerFuzzyZoom(2000L, var4);
      GenLayerAddIsland var14 = new GenLayerAddIsland(1L, var13);
      GenLayerZoom var15 = new GenLayerZoom(2001L, var14);
      GenLayerAddIsland var16 = new GenLayerAddIsland(2L, var15);
      var16 = new GenLayerAddIsland(50L, var16);
      var16 = new GenLayerAddIsland(70L, var16);
      GenLayerRemoveTooMuchOcean var19 = new GenLayerRemoveTooMuchOcean(2L, var16);
      GenLayerAddSnow var20 = new GenLayerAddSnow(2L, var19);
      GenLayerAddIsland var21 = new GenLayerAddIsland(3L, var20);
      GenLayerEdge var22 = new GenLayerEdge(2L, var21, GenLayerEdge.Mode.COOL_WARM);
      var22 = new GenLayerEdge(2L, var22, GenLayerEdge.Mode.HEAT_ICE);
      var22 = new GenLayerEdge(3L, var22, GenLayerEdge.Mode.SPECIAL);
      GenLayerZoom var25 = new GenLayerZoom(2002L, var22);
      var25 = new GenLayerZoom(2003L, var25);
      GenLayerAddIsland var27 = new GenLayerAddIsland(4L, var25);
      GenLayerAddMushroomIsland var28 = new GenLayerAddMushroomIsland(5L, var27);
      GenLayerDeepOcean var29 = new GenLayerDeepOcean(4L, var28);
      GenLayer var30 = GenLayerZoom.magnify(1000L, var29, 0);
      int var5 = 4;
      int var6 = var5;
      if (var2 == WorldType.CUSTOMIZED && !var3.isEmpty()) {
         ChunkProviderSettings var7 = ChunkProviderSettings.Factory.jsonToFactory(var3).build();
         var5 = var7.biomeSize;
         var6 = var7.riverSize;
      }

      if (var2 == WorldType.LARGE_BIOMES) {
         var5 = 6;
      }

      GenLayer var8 = GenLayerZoom.magnify(1000L, var30, 0);
      GenLayerRiverInit var31 = new GenLayerRiverInit(100L, var8);
      GenLayerBiome var9 = new GenLayerBiome(200L, var30, var2, var3);
      GenLayer var36 = GenLayerZoom.magnify(1000L, var9, 2);
      GenLayerBiomeEdge var37 = new GenLayerBiomeEdge(1000L, var36);
      GenLayer var10 = GenLayerZoom.magnify(1000L, var31, 2);
      GenLayerHills var38 = new GenLayerHills(1000L, var37, var10);
      GenLayer var32 = GenLayerZoom.magnify(1000L, var31, 2);
      var32 = GenLayerZoom.magnify(1000L, var32, var6);
      GenLayerRiver var34 = new GenLayerRiver(1L, var32);
      GenLayerSmooth var35 = new GenLayerSmooth(1000L, var34);
      var38 = new GenLayerRareBiome(1001L, var38);

      for(int var11 = 0; var11 < var5; ++var11) {
         var38 = new GenLayerZoom((long)(1000 + var11), var38);
         if (var11 == 0) {
            var38 = new GenLayerAddIsland(3L, var38);
         }

         if (var11 == 1 || var5 == 1) {
            var38 = new GenLayerShore(1000L, var38);
         }
      }

      GenLayerSmooth var40 = new GenLayerSmooth(1000L, var38);
      GenLayerRiverMix var41 = new GenLayerRiverMix(100L, var40, var35);
      GenLayerVoronoiZoom var12 = new GenLayerVoronoiZoom(10L, var41);
      var41.initWorldGenSeed(var0);
      var12.initWorldGenSeed(var0);
      return new GenLayer[]{var41, var12, var41};
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
         if (var2 != null && var3 != null) {
            if (var2 != Biomes.MESA_ROCK && var2 != Biomes.MESA_CLEAR_ROCK) {
               return var2 == var3 || var2.getBiomeClass() == var3.getBiomeClass();
            } else {
               return var3 == Biomes.MESA_ROCK || var3 == Biomes.MESA_CLEAR_ROCK;
            }
         } else {
            return false;
         }
      }
   }

   protected static boolean isBiomeOceanic(int var0) {
      Biome var1 = Biome.getBiome(var0);
      return var1 == Biomes.OCEAN || var1 == Biomes.DEEP_OCEAN || var1 == Biomes.FROZEN_OCEAN;
   }

   protected int selectRandom(int... var1) {
      return var1[this.nextInt(var1.length)];
   }

   protected int selectModeOrRandom(int var1, int var2, int var3, int var4) {
      if (var2 == var3 && var3 == var4) {
         return var2;
      } else if (var1 == var2 && var1 == var3) {
         return var1;
      } else if (var1 == var2 && var1 == var4) {
         return var1;
      } else if (var1 == var3 && var1 == var4) {
         return var1;
      } else if (var1 == var2 && var3 != var4) {
         return var1;
      } else if (var1 == var3 && var2 != var4) {
         return var1;
      } else if (var1 == var4 && var2 != var3) {
         return var1;
      } else if (var2 == var3 && var1 != var4) {
         return var2;
      } else if (var2 == var4 && var1 != var3) {
         return var2;
      } else {
         return var3 == var4 && var1 != var2 ? var3 : this.selectRandom(var1, var2, var3, var4);
      }
   }
}
