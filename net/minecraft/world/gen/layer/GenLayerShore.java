package net.minecraft.world.gen.layer;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeJungle;
import net.minecraft.world.biome.BiomeMesa;

public class GenLayerShore extends GenLayer {
   public GenLayerShore(long var1, GenLayer var3) {
      super(var1);
      this.parent = var3;
   }

   public int[] getInts(int var1, int var2, int var3, int var4) {
      int[] var5 = this.parent.getInts(var1 - 1, var2 - 1, var3 + 2, var4 + 2);
      int[] var6 = IntCache.getIntCache(var3 * var4);

      for(int var7 = 0; var7 < var4; ++var7) {
         for(int var8 = 0; var8 < var3; ++var8) {
            this.initChunkSeed((long)(var8 + var1), (long)(var7 + var2));
            int var9 = var5[var8 + 1 + (var7 + 1) * (var3 + 2)];
            Biome var10 = Biome.getBiome(var9);
            if (var9 == Biome.getIdForBiome(Biomes.MUSHROOM_ISLAND)) {
               int var17 = var5[var8 + 1 + (var7 + 1 - 1) * (var3 + 2)];
               int var20 = var5[var8 + 1 + 1 + (var7 + 1) * (var3 + 2)];
               int var23 = var5[var8 + 1 - 1 + (var7 + 1) * (var3 + 2)];
               int var26 = var5[var8 + 1 + (var7 + 1 + 1) * (var3 + 2)];
               if (var17 != Biome.getIdForBiome(Biomes.OCEAN) && var20 != Biome.getIdForBiome(Biomes.OCEAN) && var23 != Biome.getIdForBiome(Biomes.OCEAN) && var26 != Biome.getIdForBiome(Biomes.OCEAN)) {
                  var6[var8 + var7 * var3] = var9;
               } else {
                  var6[var8 + var7 * var3] = Biome.getIdForBiome(Biomes.MUSHROOM_ISLAND_SHORE);
               }
            } else if (var10 != null && var10.getBiomeClass() == BiomeJungle.class) {
               int var16 = var5[var8 + 1 + (var7 + 1 - 1) * (var3 + 2)];
               int var19 = var5[var8 + 1 + 1 + (var7 + 1) * (var3 + 2)];
               int var22 = var5[var8 + 1 - 1 + (var7 + 1) * (var3 + 2)];
               int var25 = var5[var8 + 1 + (var7 + 1 + 1) * (var3 + 2)];
               if (this.isJungleCompatible(var16) && this.isJungleCompatible(var19) && this.isJungleCompatible(var22) && this.isJungleCompatible(var25)) {
                  if (!isBiomeOceanic(var16) && !isBiomeOceanic(var19) && !isBiomeOceanic(var22) && !isBiomeOceanic(var25)) {
                     var6[var8 + var7 * var3] = var9;
                  } else {
                     var6[var8 + var7 * var3] = Biome.getIdForBiome(Biomes.BEACH);
                  }
               } else {
                  var6[var8 + var7 * var3] = Biome.getIdForBiome(Biomes.JUNGLE_EDGE);
               }
            } else if (var9 != Biome.getIdForBiome(Biomes.EXTREME_HILLS) && var9 != Biome.getIdForBiome(Biomes.EXTREME_HILLS_WITH_TREES) && var9 != Biome.getIdForBiome(Biomes.EXTREME_HILLS_EDGE)) {
               if (var10 != null && var10.isSnowyBiome()) {
                  this.replaceIfNeighborOcean(var5, var6, var8, var7, var3, var9, Biome.getIdForBiome(Biomes.COLD_BEACH));
               } else if (var9 != Biome.getIdForBiome(Biomes.MESA) && var9 != Biome.getIdForBiome(Biomes.MESA_ROCK)) {
                  if (var9 != Biome.getIdForBiome(Biomes.OCEAN) && var9 != Biome.getIdForBiome(Biomes.DEEP_OCEAN) && var9 != Biome.getIdForBiome(Biomes.RIVER) && var9 != Biome.getIdForBiome(Biomes.SWAMPLAND)) {
                     int var15 = var5[var8 + 1 + (var7 + 1 - 1) * (var3 + 2)];
                     int var18 = var5[var8 + 1 + 1 + (var7 + 1) * (var3 + 2)];
                     int var21 = var5[var8 + 1 - 1 + (var7 + 1) * (var3 + 2)];
                     int var24 = var5[var8 + 1 + (var7 + 1 + 1) * (var3 + 2)];
                     if (!isBiomeOceanic(var15) && !isBiomeOceanic(var18) && !isBiomeOceanic(var21) && !isBiomeOceanic(var24)) {
                        var6[var8 + var7 * var3] = var9;
                     } else {
                        var6[var8 + var7 * var3] = Biome.getIdForBiome(Biomes.BEACH);
                     }
                  } else {
                     var6[var8 + var7 * var3] = var9;
                  }
               } else {
                  int var11 = var5[var8 + 1 + (var7 + 1 - 1) * (var3 + 2)];
                  int var12 = var5[var8 + 1 + 1 + (var7 + 1) * (var3 + 2)];
                  int var13 = var5[var8 + 1 - 1 + (var7 + 1) * (var3 + 2)];
                  int var14 = var5[var8 + 1 + (var7 + 1 + 1) * (var3 + 2)];
                  if (!isBiomeOceanic(var11) && !isBiomeOceanic(var12) && !isBiomeOceanic(var13) && !isBiomeOceanic(var14)) {
                     if (this.isMesa(var11) && this.isMesa(var12) && this.isMesa(var13) && this.isMesa(var14)) {
                        var6[var8 + var7 * var3] = var9;
                     } else {
                        var6[var8 + var7 * var3] = Biome.getIdForBiome(Biomes.DESERT);
                     }
                  } else {
                     var6[var8 + var7 * var3] = var9;
                  }
               }
            } else {
               this.replaceIfNeighborOcean(var5, var6, var8, var7, var3, var9, Biome.getIdForBiome(Biomes.STONE_BEACH));
            }
         }
      }

      return var6;
   }

   private void replaceIfNeighborOcean(int[] var1, int[] var2, int var3, int var4, int var5, int var6, int var7) {
      if (isBiomeOceanic(var6)) {
         var2[var3 + var4 * var5] = var6;
      } else {
         int var8 = var1[var3 + 1 + (var4 + 1 - 1) * (var5 + 2)];
         int var9 = var1[var3 + 1 + 1 + (var4 + 1) * (var5 + 2)];
         int var10 = var1[var3 + 1 - 1 + (var4 + 1) * (var5 + 2)];
         int var11 = var1[var3 + 1 + (var4 + 1 + 1) * (var5 + 2)];
         if (!isBiomeOceanic(var8) && !isBiomeOceanic(var9) && !isBiomeOceanic(var10) && !isBiomeOceanic(var11)) {
            var2[var3 + var4 * var5] = var6;
         } else {
            var2[var3 + var4 * var5] = var7;
         }
      }

   }

   private boolean isJungleCompatible(int var1) {
      return Biome.getBiome(var1) != null && Biome.getBiome(var1).getBiomeClass() == BiomeJungle.class ? true : var1 == Biome.getIdForBiome(Biomes.JUNGLE_EDGE) || var1 == Biome.getIdForBiome(Biomes.JUNGLE) || var1 == Biome.getIdForBiome(Biomes.JUNGLE_HILLS) || var1 == Biome.getIdForBiome(Biomes.FOREST) || var1 == Biome.getIdForBiome(Biomes.TAIGA) || isBiomeOceanic(var1);
   }

   private boolean isMesa(int var1) {
      return Biome.getBiome(var1) instanceof BiomeMesa;
   }
}
