package net.minecraft.world.biome;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.util.math.BlockPos;

public class BiomeProviderSingle extends BiomeProvider {
   private final Biome biome;

   public BiomeProviderSingle(Biome var1) {
      this.biome = biomeIn;
   }

   public Biome getBiome(BlockPos var1) {
      return this.biome;
   }

   public Biome[] getBiomesForGeneration(Biome[] var1, int var2, int var3, int var4, int var5) {
      if (biomes == null || biomes.length < width * height) {
         biomes = new Biome[width * height];
      }

      Arrays.fill(biomes, 0, width * height, this.biome);
      return biomes;
   }

   public Biome[] getBiomes(@Nullable Biome[] var1, int var2, int var3, int var4, int var5) {
      if (oldBiomeList == null || oldBiomeList.length < width * depth) {
         oldBiomeList = new Biome[width * depth];
      }

      Arrays.fill(oldBiomeList, 0, width * depth, this.biome);
      return oldBiomeList;
   }

   public Biome[] getBiomes(@Nullable Biome[] var1, int var2, int var3, int var4, int var5, boolean var6) {
      return this.getBiomes(listToReuse, x, z, width, length);
   }

   @Nullable
   public BlockPos findBiomePosition(int var1, int var2, int var3, List var4, Random var5) {
      return biomes.contains(this.biome) ? new BlockPos(x - range + random.nextInt(range * 2 + 1), 0, z - range + random.nextInt(range * 2 + 1)) : null;
   }

   public boolean areBiomesViable(int var1, int var2, int var3, List var4) {
      return allowed.contains(this.biome);
   }
}
