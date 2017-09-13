package net.minecraft.world.gen.layer;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GenLayerHills extends GenLayer {
   private static final Logger LOGGER = LogManager.getLogger();
   private final GenLayer riverLayer;

   public GenLayerHills(long var1, GenLayer var3, GenLayer var4) {
      super(var1);
      this.parent = var3;
      this.riverLayer = var4;
   }

   public int[] getInts(int var1, int var2, int var3, int var4) {
      int[] var5 = this.parent.getInts(var1 - 1, var2 - 1, var3 + 2, var4 + 2);
      int[] var6 = this.riverLayer.getInts(var1 - 1, var2 - 1, var3 + 2, var4 + 2);
      int[] var7 = IntCache.getIntCache(var3 * var4);

      for(int var8 = 0; var8 < var4; ++var8) {
         for(int var9 = 0; var9 < var3; ++var9) {
            this.initChunkSeed((long)(var9 + var1), (long)(var8 + var2));
            int var10 = var5[var9 + 1 + (var8 + 1) * (var3 + 2)];
            int var11 = var6[var9 + 1 + (var8 + 1) * (var3 + 2)];
            boolean var12 = (var11 - 2) % 29 == 0;
            if (var10 > 255) {
               LOGGER.debug("old! {}", new Object[]{var10});
            }

            Biome var13 = Biome.getBiomeForId(var10);
            boolean var14 = var13 != null && var13.isMutation();
            if (var10 != 0 && var11 >= 2 && (var11 - 2) % 29 == 1 && !var14) {
               Biome var22 = Biome.getMutationForBiome(var13);
               var7[var9 + var8 * var3] = var22 == null ? var10 : Biome.getIdForBiome(var22);
            } else if (this.nextInt(3) != 0 && !var12) {
               var7[var9 + var8 * var3] = var10;
            } else {
               Biome var15 = var13;
               if (var13 == Biomes.DESERT) {
                  var15 = Biomes.DESERT_HILLS;
               } else if (var13 == Biomes.FOREST) {
                  var15 = Biomes.FOREST_HILLS;
               } else if (var13 == Biomes.BIRCH_FOREST) {
                  var15 = Biomes.BIRCH_FOREST_HILLS;
               } else if (var13 == Biomes.ROOFED_FOREST) {
                  var15 = Biomes.PLAINS;
               } else if (var13 == Biomes.TAIGA) {
                  var15 = Biomes.TAIGA_HILLS;
               } else if (var13 == Biomes.REDWOOD_TAIGA) {
                  var15 = Biomes.REDWOOD_TAIGA_HILLS;
               } else if (var13 == Biomes.COLD_TAIGA) {
                  var15 = Biomes.COLD_TAIGA_HILLS;
               } else if (var13 == Biomes.PLAINS) {
                  if (this.nextInt(3) == 0) {
                     var15 = Biomes.FOREST_HILLS;
                  } else {
                     var15 = Biomes.FOREST;
                  }
               } else if (var13 == Biomes.ICE_PLAINS) {
                  var15 = Biomes.ICE_MOUNTAINS;
               } else if (var13 == Biomes.JUNGLE) {
                  var15 = Biomes.JUNGLE_HILLS;
               } else if (var13 == Biomes.OCEAN) {
                  var15 = Biomes.DEEP_OCEAN;
               } else if (var13 == Biomes.EXTREME_HILLS) {
                  var15 = Biomes.EXTREME_HILLS_WITH_TREES;
               } else if (var13 == Biomes.SAVANNA) {
                  var15 = Biomes.SAVANNA_PLATEAU;
               } else if (biomesEqualOrMesaPlateau(var10, Biome.getIdForBiome(Biomes.MESA_ROCK))) {
                  var15 = Biomes.MESA;
               } else if (var13 == Biomes.DEEP_OCEAN && this.nextInt(3) == 0) {
                  int var16 = this.nextInt(2);
                  if (var16 == 0) {
                     var15 = Biomes.PLAINS;
                  } else {
                     var15 = Biomes.FOREST;
                  }
               }

               int var23 = Biome.getIdForBiome(var15);
               if (var12 && var23 != var10) {
                  Biome var17 = Biome.getMutationForBiome(var15);
                  var23 = var17 == null ? var10 : Biome.getIdForBiome(var17);
               }

               if (var23 == var10) {
                  var7[var9 + var8 * var3] = var10;
               } else {
                  int var24 = var5[var9 + 1 + (var8 + 0) * (var3 + 2)];
                  int var18 = var5[var9 + 2 + (var8 + 1) * (var3 + 2)];
                  int var19 = var5[var9 + 0 + (var8 + 1) * (var3 + 2)];
                  int var20 = var5[var9 + 1 + (var8 + 2) * (var3 + 2)];
                  int var21 = 0;
                  if (biomesEqualOrMesaPlateau(var24, var10)) {
                     ++var21;
                  }

                  if (biomesEqualOrMesaPlateau(var18, var10)) {
                     ++var21;
                  }

                  if (biomesEqualOrMesaPlateau(var19, var10)) {
                     ++var21;
                  }

                  if (biomesEqualOrMesaPlateau(var20, var10)) {
                     ++var21;
                  }

                  if (var21 >= 3) {
                     var7[var9 + var8 * var3] = var23;
                  } else {
                     var7[var9 + var8 * var3] = var10;
                  }
               }
            }
         }
      }

      return var7;
   }
}
