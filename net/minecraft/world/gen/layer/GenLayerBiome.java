package net.minecraft.world.gen.layer;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.init.Biomes;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkProviderSettings;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.BiomeManager.BiomeEntry;
import net.minecraftforge.common.BiomeManager.BiomeType;

public class GenLayerBiome extends GenLayer {
   private List[] biomes = new ArrayList[BiomeType.values().length];
   private final ChunkProviderSettings settings;

   public GenLayerBiome(long var1, GenLayer var3, WorldType var4, String var5) {
      super(var1);
      this.parent = var3;

      for(BiomeType var9 : BiomeType.values()) {
         ImmutableList var10 = BiomeManager.getBiomes(var9);
         int var11 = var9.ordinal();
         if (this.biomes[var11] == null) {
            this.biomes[var11] = new ArrayList();
         }

         if (var10 != null) {
            this.biomes[var11].addAll(var10);
         }
      }

      int var12 = BiomeType.DESERT.ordinal();
      this.biomes[var12].add(new BiomeEntry(Biomes.DESERT, 30));
      this.biomes[var12].add(new BiomeEntry(Biomes.SAVANNA, 20));
      this.biomes[var12].add(new BiomeEntry(Biomes.PLAINS, 10));
      if (var4 == WorldType.DEFAULT_1_1) {
         this.biomes[var12].clear();
         this.biomes[var12].add(new BiomeEntry(Biomes.DESERT, 10));
         this.biomes[var12].add(new BiomeEntry(Biomes.FOREST, 10));
         this.biomes[var12].add(new BiomeEntry(Biomes.EXTREME_HILLS, 10));
         this.biomes[var12].add(new BiomeEntry(Biomes.SWAMPLAND, 10));
         this.biomes[var12].add(new BiomeEntry(Biomes.PLAINS, 10));
         this.biomes[var12].add(new BiomeEntry(Biomes.TAIGA, 10));
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
                  var6[var8 + var7 * var3] = Biome.getIdForBiome(this.getWeightedBiomeEntry(BiomeType.DESERT).biome);
               }
            } else if (var9 == 2) {
               if (var10 > 0) {
                  var6[var8 + var7 * var3] = Biome.getIdForBiome(Biomes.JUNGLE);
               } else {
                  var6[var8 + var7 * var3] = Biome.getIdForBiome(this.getWeightedBiomeEntry(BiomeType.WARM).biome);
               }
            } else if (var9 == 3) {
               if (var10 > 0) {
                  var6[var8 + var7 * var3] = Biome.getIdForBiome(Biomes.REDWOOD_TAIGA);
               } else {
                  var6[var8 + var7 * var3] = Biome.getIdForBiome(this.getWeightedBiomeEntry(BiomeType.COOL).biome);
               }
            } else if (var9 == 4) {
               var6[var8 + var7 * var3] = Biome.getIdForBiome(this.getWeightedBiomeEntry(BiomeType.ICY).biome);
            } else {
               var6[var8 + var7 * var3] = Biome.getIdForBiome(Biomes.MUSHROOM_ISLAND);
            }
         }
      }

      return var6;
   }

   protected BiomeEntry getWeightedBiomeEntry(BiomeType var1) {
      List var2 = this.biomes[var1.ordinal()];
      int var3 = WeightedRandom.getTotalWeight(var2);
      int var4 = BiomeManager.isTypeListModded(var1) ? this.nextInt(var3) : this.nextInt(var3 / 10) * 10;
      return (BiomeEntry)WeightedRandom.getRandomItem(var2, var4);
   }
}
