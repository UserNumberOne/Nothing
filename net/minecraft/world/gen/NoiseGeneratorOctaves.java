package net.minecraft.world.gen;

import java.util.Random;
import net.minecraft.util.math.MathHelper;

public class NoiseGeneratorOctaves extends NoiseGenerator {
   private final NoiseGeneratorImproved[] generatorCollection;
   private final int octaves;

   public NoiseGeneratorOctaves(Random var1, int var2) {
      this.octaves = octavesIn;
      this.generatorCollection = new NoiseGeneratorImproved[octavesIn];

      for(int i = 0; i < octavesIn; ++i) {
         this.generatorCollection[i] = new NoiseGeneratorImproved(seed);
      }

   }

   public double[] generateNoiseOctaves(double[] var1, int var2, int var3, int var4, int var5, int var6, int var7, double var8, double var10, double var12) {
      if (noiseArray == null) {
         noiseArray = new double[xSize * ySize * zSize];
      } else {
         for(int i = 0; i < noiseArray.length; ++i) {
            noiseArray[i] = 0.0D;
         }
      }

      double d3 = 1.0D;

      for(int j = 0; j < this.octaves; ++j) {
         double d0 = (double)xOffset * d3 * xScale;
         double d1 = (double)yOffset * d3 * yScale;
         double d2 = (double)zOffset * d3 * zScale;
         long k = MathHelper.lfloor(d0);
         long l = MathHelper.lfloor(d2);
         d0 = d0 - (double)k;
         d2 = d2 - (double)l;
         k = k % 16777216L;
         l = l % 16777216L;
         d0 = d0 + (double)k;
         d2 = d2 + (double)l;
         this.generatorCollection[j].populateNoiseArray(noiseArray, d0, d1, d2, xSize, ySize, zSize, xScale * d3, yScale * d3, zScale * d3, d3);
         d3 /= 2.0D;
      }

      return noiseArray;
   }

   public double[] generateNoiseOctaves(double[] var1, int var2, int var3, int var4, int var5, double var6, double var8, double var10) {
      return this.generateNoiseOctaves(noiseArray, xOffset, 10, zOffset, xSize, 1, zSize, xScale, 1.0D, zScale);
   }
}
