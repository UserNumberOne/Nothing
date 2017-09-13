package net.minecraft.world.gen.layer;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

public class GenLayerBiomeEdge extends GenLayer {
   public GenLayerBiomeEdge(long var1, GenLayer var3) {
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
            if (!this.replaceBiomeEdgeIfNecessary(var5, var6, var8, var7, var3, var9, Biome.getIdForBiome(Biomes.EXTREME_HILLS), Biome.getIdForBiome(Biomes.EXTREME_HILLS_EDGE)) && !this.replaceBiomeEdge(var5, var6, var8, var7, var3, var9, Biome.getIdForBiome(Biomes.MESA_ROCK), Biome.getIdForBiome(Biomes.MESA)) && !this.replaceBiomeEdge(var5, var6, var8, var7, var3, var9, Biome.getIdForBiome(Biomes.MESA_CLEAR_ROCK), Biome.getIdForBiome(Biomes.MESA)) && !this.replaceBiomeEdge(var5, var6, var8, var7, var3, var9, Biome.getIdForBiome(Biomes.REDWOOD_TAIGA), Biome.getIdForBiome(Biomes.TAIGA))) {
               if (var9 == Biome.getIdForBiome(Biomes.DESERT)) {
                  int var14 = var5[var8 + 1 + (var7 + 1 - 1) * (var3 + 2)];
                  int var15 = var5[var8 + 1 + 1 + (var7 + 1) * (var3 + 2)];
                  int var16 = var5[var8 + 1 - 1 + (var7 + 1) * (var3 + 2)];
                  int var17 = var5[var8 + 1 + (var7 + 1 + 1) * (var3 + 2)];
                  if (var14 != Biome.getIdForBiome(Biomes.ICE_PLAINS) && var15 != Biome.getIdForBiome(Biomes.ICE_PLAINS) && var16 != Biome.getIdForBiome(Biomes.ICE_PLAINS) && var17 != Biome.getIdForBiome(Biomes.ICE_PLAINS)) {
                     var6[var8 + var7 * var3] = var9;
                  } else {
                     var6[var8 + var7 * var3] = Biome.getIdForBiome(Biomes.EXTREME_HILLS_WITH_TREES);
                  }
               } else if (var9 == Biome.getIdForBiome(Biomes.SWAMPLAND)) {
                  int var10 = var5[var8 + 1 + (var7 + 1 - 1) * (var3 + 2)];
                  int var11 = var5[var8 + 1 + 1 + (var7 + 1) * (var3 + 2)];
                  int var12 = var5[var8 + 1 - 1 + (var7 + 1) * (var3 + 2)];
                  int var13 = var5[var8 + 1 + (var7 + 1 + 1) * (var3 + 2)];
                  if (var10 != Biome.getIdForBiome(Biomes.DESERT) && var11 != Biome.getIdForBiome(Biomes.DESERT) && var12 != Biome.getIdForBiome(Biomes.DESERT) && var13 != Biome.getIdForBiome(Biomes.DESERT) && var10 != Biome.getIdForBiome(Biomes.COLD_TAIGA) && var11 != Biome.getIdForBiome(Biomes.COLD_TAIGA) && var12 != Biome.getIdForBiome(Biomes.COLD_TAIGA) && var13 != Biome.getIdForBiome(Biomes.COLD_TAIGA) && var10 != Biome.getIdForBiome(Biomes.ICE_PLAINS) && var11 != Biome.getIdForBiome(Biomes.ICE_PLAINS) && var12 != Biome.getIdForBiome(Biomes.ICE_PLAINS) && var13 != Biome.getIdForBiome(Biomes.ICE_PLAINS)) {
                     if (var10 != Biome.getIdForBiome(Biomes.JUNGLE) && var13 != Biome.getIdForBiome(Biomes.JUNGLE) && var11 != Biome.getIdForBiome(Biomes.JUNGLE) && var12 != Biome.getIdForBiome(Biomes.JUNGLE)) {
                        var6[var8 + var7 * var3] = var9;
                     } else {
                        var6[var8 + var7 * var3] = Biome.getIdForBiome(Biomes.JUNGLE_EDGE);
                     }
                  } else {
                     var6[var8 + var7 * var3] = Biome.getIdForBiome(Biomes.PLAINS);
                  }
               } else {
                  var6[var8 + var7 * var3] = var9;
               }
            }
         }
      }

      return var6;
   }

   private boolean replaceBiomeEdgeIfNecessary(int[] var1, int[] var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      if (!biomesEqualOrMesaPlateau(var6, var7)) {
         return false;
      } else {
         int var9 = var1[var3 + 1 + (var4 + 1 - 1) * (var5 + 2)];
         int var10 = var1[var3 + 1 + 1 + (var4 + 1) * (var5 + 2)];
         int var11 = var1[var3 + 1 - 1 + (var4 + 1) * (var5 + 2)];
         int var12 = var1[var3 + 1 + (var4 + 1 + 1) * (var5 + 2)];
         if (this.canBiomesBeNeighbors(var9, var7) && this.canBiomesBeNeighbors(var10, var7) && this.canBiomesBeNeighbors(var11, var7) && this.canBiomesBeNeighbors(var12, var7)) {
            var2[var3 + var4 * var5] = var6;
         } else {
            var2[var3 + var4 * var5] = var8;
         }

         return true;
      }
   }

   private boolean replaceBiomeEdge(int[] var1, int[] var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      if (var6 != var7) {
         return false;
      } else {
         int var9 = var1[var3 + 1 + (var4 + 1 - 1) * (var5 + 2)];
         int var10 = var1[var3 + 1 + 1 + (var4 + 1) * (var5 + 2)];
         int var11 = var1[var3 + 1 - 1 + (var4 + 1) * (var5 + 2)];
         int var12 = var1[var3 + 1 + (var4 + 1 + 1) * (var5 + 2)];
         if (biomesEqualOrMesaPlateau(var9, var7) && biomesEqualOrMesaPlateau(var10, var7) && biomesEqualOrMesaPlateau(var11, var7) && biomesEqualOrMesaPlateau(var12, var7)) {
            var2[var3 + var4 * var5] = var6;
         } else {
            var2[var3 + var4 * var5] = var8;
         }

         return true;
      }
   }

   private boolean canBiomesBeNeighbors(int var1, int var2) {
      if (biomesEqualOrMesaPlateau(var1, var2)) {
         return true;
      } else {
         Biome var3 = Biome.getBiome(var1);
         Biome var4 = Biome.getBiome(var2);
         if (var3 != null && var4 != null) {
            Biome.TempCategory var5 = var3.getTempCategory();
            Biome.TempCategory var6 = var4.getTempCategory();
            return var5 == var6 || var5 == Biome.TempCategory.MEDIUM || var6 == Biome.TempCategory.MEDIUM;
         } else {
            return false;
         }
      }
   }
}
