package net.minecraft.world.gen;

import java.util.Random;

public class NoiseGeneratorPerlin extends NoiseGenerator {
   private final NoiseGeneratorSimplex[] noiseLevels;
   private final int levels;

   public NoiseGeneratorPerlin(Random var1, int var2) {
      this.levels = p_i45470_2_;
      this.noiseLevels = new NoiseGeneratorSimplex[p_i45470_2_];

      for(int i = 0; i < p_i45470_2_; ++i) {
         this.noiseLevels[i] = new NoiseGeneratorSimplex(p_i45470_1_);
      }

   }

   public double getValue(double var1, double var3) {
      double d0 = 0.0D;
      double d1 = 1.0D;

      for(int i = 0; i < this.levels; ++i) {
         d0 += this.noiseLevels[i].getValue(p_151601_1_ * d1, p_151601_3_ * d1) / d1;
         d1 /= 2.0D;
      }

      return d0;
   }

   public double[] getRegion(double[] var1, double var2, double var4, int var6, int var7, double var8, double var10, double var12) {
      return this.getRegion(p_151599_1_, p_151599_2_, p_151599_4_, p_151599_6_, p_151599_7_, p_151599_8_, p_151599_10_, p_151599_12_, 0.5D);
   }

   public double[] getRegion(double[] var1, double var2, double var4, int var6, int var7, double var8, double var10, double var12, double var14) {
      if (p_151600_1_ != null && p_151600_1_.length >= p_151600_6_ * p_151600_7_) {
         for(int i = 0; i < p_151600_1_.length; ++i) {
            p_151600_1_[i] = 0.0D;
         }
      } else {
         p_151600_1_ = new double[p_151600_6_ * p_151600_7_];
      }

      double d1 = 1.0D;
      double d0 = 1.0D;

      for(int j = 0; j < this.levels; ++j) {
         this.noiseLevels[j].add(p_151600_1_, p_151600_2_, p_151600_4_, p_151600_6_, p_151600_7_, p_151600_8_ * d0 * d1, p_151600_10_ * d0 * d1, 0.55D / d1);
         d0 *= p_151600_12_;
         d1 *= p_151600_14_;
      }

      return p_151600_1_;
   }
}
