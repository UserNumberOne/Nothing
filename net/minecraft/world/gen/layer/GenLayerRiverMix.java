package net.minecraft.world.gen.layer;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

public class GenLayerRiverMix extends GenLayer {
   private final GenLayer biomePatternGeneratorChain;
   private final GenLayer riverPatternGeneratorChain;

   public GenLayerRiverMix(long var1, GenLayer var3, GenLayer var4) {
      super(p_i2129_1_);
      this.biomePatternGeneratorChain = p_i2129_3_;
      this.riverPatternGeneratorChain = p_i2129_4_;
   }

   public void initWorldGenSeed(long var1) {
      this.biomePatternGeneratorChain.initWorldGenSeed(seed);
      this.riverPatternGeneratorChain.initWorldGenSeed(seed);
      super.initWorldGenSeed(seed);
   }

   public int[] getInts(int var1, int var2, int var3, int var4) {
      int[] aint = this.biomePatternGeneratorChain.getInts(areaX, areaY, areaWidth, areaHeight);
      int[] aint1 = this.riverPatternGeneratorChain.getInts(areaX, areaY, areaWidth, areaHeight);
      int[] aint2 = IntCache.getIntCache(areaWidth * areaHeight);

      for(int i = 0; i < areaWidth * areaHeight; ++i) {
         if (aint[i] != Biome.getIdForBiome(Biomes.OCEAN) && aint[i] != Biome.getIdForBiome(Biomes.DEEP_OCEAN)) {
            if (aint1[i] == Biome.getIdForBiome(Biomes.RIVER)) {
               if (aint[i] == Biome.getIdForBiome(Biomes.ICE_PLAINS)) {
                  aint2[i] = Biome.getIdForBiome(Biomes.FROZEN_RIVER);
               } else if (aint[i] != Biome.getIdForBiome(Biomes.MUSHROOM_ISLAND) && aint[i] != Biome.getIdForBiome(Biomes.MUSHROOM_ISLAND_SHORE)) {
                  aint2[i] = aint1[i] & 255;
               } else {
                  aint2[i] = Biome.getIdForBiome(Biomes.MUSHROOM_ISLAND_SHORE);
               }
            } else {
               aint2[i] = aint[i];
            }
         } else {
            aint2[i] = aint[i];
         }
      }

      return aint2;
   }
}
