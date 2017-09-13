package net.minecraft.world.biome;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.util.math.BlockPos;

public class BiomeProviderSingle extends BiomeProvider {
   private final Biome biome;

   public BiomeProviderSingle(Biome var1) {
      this.biome = var1;
   }

   public Biome getBiome(BlockPos var1) {
      return this.biome;
   }

   public Biome[] getBiomesForGeneration(Biome[] var1, int var2, int var3, int var4, int var5) {
      if (var1 == null || var1.length < var4 * var5) {
         var1 = new Biome[var4 * var5];
      }

      Arrays.fill(var1, 0, var4 * var5, this.biome);
      return var1;
   }

   public Biome[] getBiomes(@Nullable Biome[] var1, int var2, int var3, int var4, int var5) {
      if (var1 == null || var1.length < var4 * var5) {
         var1 = new Biome[var4 * var5];
      }

      Arrays.fill(var1, 0, var4 * var5, this.biome);
      return var1;
   }

   public Biome[] getBiomes(@Nullable Biome[] var1, int var2, int var3, int var4, int var5, boolean var6) {
      return this.getBiomes(var1, var2, var3, var4, var5);
   }

   @Nullable
   public BlockPos findBiomePosition(int var1, int var2, int var3, List var4, Random var5) {
      return var4.contains(this.biome) ? new BlockPos(var1 - var3 + var5.nextInt(var3 * 2 + 1), 0, var2 - var3 + var5.nextInt(var3 * 2 + 1)) : null;
   }

   public boolean areBiomesViable(int var1, int var2, int var3, List var4) {
      return var4.contains(this.biome);
   }
}
