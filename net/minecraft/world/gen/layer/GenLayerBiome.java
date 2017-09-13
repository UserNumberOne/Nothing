package net.minecraft.world.gen.layer;

import net.minecraft.init.Biomes;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkProviderSettings;

public class GenLayerBiome extends GenLayer {
   private Biome[] warmBiomes = new Biome[]{Biomes.DESERT, Biomes.DESERT, Biomes.DESERT, Biomes.SAVANNA, Biomes.SAVANNA, Biomes.PLAINS};
   private final Biome[] mediumBiomes = new Biome[]{Biomes.FOREST, Biomes.ROOFED_FOREST, Biomes.EXTREME_HILLS, Biomes.PLAINS, Biomes.BIRCH_FOREST, Biomes.SWAMPLAND};
   private final Biome[] coldBiomes = new Biome[]{Biomes.FOREST, Biomes.EXTREME_HILLS, Biomes.TAIGA, Biomes.PLAINS};
   private final Biome[] iceBiomes = new Biome[]{Biomes.ICE_PLAINS, Biomes.ICE_PLAINS, Biomes.ICE_PLAINS, Biomes.COLD_TAIGA};
   private final ChunkProviderSettings settings;

   public GenLayerBiome(long var1, GenLayer var3, WorldType var4, String var5) {
      super(var1);
      this.parent = var3;
      if (var4 == WorldType.DEFAULT_1_1) {
         this.warmBiomes = new Biome[]{Biomes.DESERT, Biomes.FOREST, Biomes.EXTREME_HILLS, Biomes.SWAMPLAND, Biomes.PLAINS, Biomes.TAIGA};
         this.settings = null;
      } else if (var4 == WorldType.CUSTOMIZED) {
         this.settings = ChunkProviderSettings.Factory.jsonToFactory(var5).build();
      } else {
         this.settings = null;
      }

   }

   public int[] getInts(int var1, int var2, int var3, int var4) {
      int[] var5 = this.parent.getInts(var1, var2, var3, var4);
      int[] var6 = IntCache.getIntCache(var3 * var4);

      for(int var7 = 0; var7 < var4; ++var7) {
         for(int var8 = 0; var8 < var3; ++var8) {
            this.initChunkSeed((long)(var8 + var1), (long)(var7 + var2));
            int var9 = var5[var8 + var7 * var3];
            int var10 = (var9 & 3840) >> 8;
            var9 = var9 & -3841;
            if (this.settings != null && this.settings.fixedBiome >= 0) {
               var6[var8 + var7 * var3] = this.settings.fixedBiome;
            } else if (isBiomeOceanic(var9)) {
               var6[var8 + var7 * var3] = var9;
            } else if (var9 == Biome.getIdForBiome(Biomes.MUSHROOM_ISLAND)) {
               var6[var8 + var7 * var3] = var9;
            } else if (var9 == 1) {
               if (var10 > 0) {
                  if (this.nextInt(3) == 0) {
                     var6[var8 + var7 * var3] = Biome.getIdForBiome(Biomes.MESA_CLEAR_ROCK);
                  } else {
                     var6[var8 + var7 * var3] = Biome.getIdForBiome(Biomes.MESA_ROCK);
                  }
               } else {
                  var6[var8 + var7 * var3] = Biome.getIdForBiome(this.warmBiomes[this.nextInt(this.warmBiomes.length)]);
               }
            } else if (var9 == 2) {
               if (var10 > 0) {
                  var6[var8 + var7 * var3] = Biome.getIdForBiome(Biomes.JUNGLE);
               } else {
                  var6[var8 + var7 * var3] = Biome.getIdForBiome(this.mediumBiomes[this.nextInt(this.mediumBiomes.length)]);
               }
            } else if (var9 == 3) {
               if (var10 > 0) {
                  var6[var8 + var7 * var3] = Biome.getIdForBiome(Biomes.REDWOOD_TAIGA);
               } else {
                  var6[var8 + var7 * var3] = Biome.getIdForBiome(this.coldBiomes[this.nextInt(this.coldBiomes.length)]);
               }
            } else if (var9 == 4) {
               var6[var8 + var7 * var3] = Biome.getIdForBiome(this.iceBiomes[this.nextInt(this.iceBiomes.length)]);
            } else {
               var6[var8 + var7 * var3] = Biome.getIdForBiome(Biomes.MUSHROOM_ISLAND);
            }
         }
      }

      return var6;
   }
}
