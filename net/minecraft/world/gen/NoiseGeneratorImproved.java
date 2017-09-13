package net.minecraft.world.gen;

import java.util.Random;

public class NoiseGeneratorImproved extends NoiseGenerator {
   private final int[] permutations;
   public double xCoord;
   public double yCoord;
   public double zCoord;
   private static final double[] GRAD_X = new double[]{1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 0.0D, -1.0D, 0.0D};
   private static final double[] GRAD_Y = new double[]{1.0D, 1.0D, -1.0D, -1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D};
   private static final double[] GRAD_Z = new double[]{0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, -1.0D, -1.0D, 1.0D, 1.0D, -1.0D, -1.0D, 0.0D, 1.0D, 0.0D, -1.0D};
   private static final double[] GRAD_2X = new double[]{1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 0.0D, -1.0D, 0.0D};
   private static final double[] GRAD_2Z = new double[]{0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, -1.0D, -1.0D, 1.0D, 1.0D, -1.0D, -1.0D, 0.0D, 1.0D, 0.0D, -1.0D};

   public NoiseGeneratorImproved() {
      this(new Random());
   }

   public NoiseGeneratorImproved(Random var1) {
      this.permutations = new int[512];
      this.xCoord = var1.nextDouble() * 256.0D;
      this.yCoord = var1.nextDouble() * 256.0D;
      this.zCoord = var1.nextDouble() * 256.0D;

      for(int var2 = 0; var2 < 256; this.permutations[var2] = var2++) {
         ;
      }

      for(int var5 = 0; var5 < 256; ++var5) {
         int var3 = var1.nextInt(256 - var5) + var5;
         int var4 = this.permutations[var5];
         this.permutations[var5] = this.permutations[var3];
         this.permutations[var3] = var4;
         this.permutations[var5 + 256] = this.permutations[var5];
      }

   }

   public final double lerp(double var1, double var3, double var5) {
      return var3 + var1 * (var5 - var3);
   }

   public final double grad2(int var1, double var2, double var4) {
      int var6 = var1 & 15;
      return GRAD_2X[var6] * var2 + GRAD_2Z[var6] * var4;
   }

   public final double grad(int var1, double var2, double var4, double var6) {
      int var8 = var1 & 15;
      return GRAD_X[var8] * var2 + GRAD_Y[var8] * var4 + GRAD_Z[var8] * var6;
   }

   public void populateNoiseArray(double[] var1, double var2, double var4, double var6, int var8, int var9, int var10, double var11, double var13, double var15, double var17) {
      if (var9 == 1) {
         int var77 = 0;
         int var20 = 0;
         int var21 = 0;
         int var81 = 0;
         double var23 = 0.0D;
         double var25 = 0.0D;
         int var86 = 0;
         double var28 = 1.0D / var17;

         for(int var30 = 0; var30 < var8; ++var30) {
            double var87 = var2 + (double)var30 * var11 + this.xCoord;
            int var33 = (int)var87;
            if (var87 < (double)var33) {
               --var33;
            }

            int var34 = var33 & 255;
            var87 = var87 - (double)var33;
            double var89 = var87 * var87 * var87 * (var87 * (var87 * 6.0D - 15.0D) + 10.0D);

            for(int var90 = 0; var90 < var10; ++var90) {
               double var92 = var6 + (double)var90 * var15 + this.zCoord;
               int var94 = (int)var92;
               if (var92 < (double)var94) {
                  --var94;
               }

               int var95 = var94 & 255;
               var92 = var92 - (double)var94;
               double var96 = var92 * var92 * var92 * (var92 * (var92 * 6.0D - 15.0D) + 10.0D);
               var77 = this.permutations[var34] + 0;
               var20 = this.permutations[var77] + var95;
               var21 = this.permutations[var34 + 1] + 0;
               var81 = this.permutations[var21] + var95;
               var23 = this.lerp(var89, this.grad2(this.permutations[var20], var87, var92), this.grad(this.permutations[var81], var87 - 1.0D, 0.0D, var92));
               var25 = this.lerp(var89, this.grad(this.permutations[var20 + 1], var87, 0.0D, var92 - 1.0D), this.grad(this.permutations[var81 + 1], var87 - 1.0D, 0.0D, var92 - 1.0D));
               double var44 = this.lerp(var96, var23, var25);
               int var104 = var86++;
               var1[var104] += var44 * var28;
            }
         }

      } else {
         int var19 = 0;
         double var46 = 1.0D / var17;
         int var22 = -1;
         int var48 = 0;
         int var49 = 0;
         int var50 = 0;
         int var51 = 0;
         int var27 = 0;
         int var52 = 0;
         double var53 = 0.0D;
         double var31 = 0.0D;
         double var55 = 0.0D;
         double var35 = 0.0D;

         for(int var37 = 0; var37 < var8; ++var37) {
            double var38 = var2 + (double)var37 * var11 + this.xCoord;
            int var40 = (int)var38;
            if (var38 < (double)var40) {
               --var40;
            }

            int var41 = var40 & 255;
            var38 = var38 - (double)var40;
            double var42 = var38 * var38 * var38 * (var38 * (var38 * 6.0D - 15.0D) + 10.0D);

            for(int var57 = 0; var57 < var10; ++var57) {
               double var58 = var6 + (double)var57 * var15 + this.zCoord;
               int var60 = (int)var58;
               if (var58 < (double)var60) {
                  --var60;
               }

               int var61 = var60 & 255;
               var58 = var58 - (double)var60;
               double var62 = var58 * var58 * var58 * (var58 * (var58 * 6.0D - 15.0D) + 10.0D);

               for(int var64 = 0; var64 < var9; ++var64) {
                  double var65 = var4 + (double)var64 * var13 + this.yCoord;
                  int var67 = (int)var65;
                  if (var65 < (double)var67) {
                     --var67;
                  }

                  int var68 = var67 & 255;
                  var65 = var65 - (double)var67;
                  double var69 = var65 * var65 * var65 * (var65 * (var65 * 6.0D - 15.0D) + 10.0D);
                  if (var64 == 0 || var68 != var22) {
                     var22 = var68;
                     var48 = this.permutations[var41] + var68;
                     var49 = this.permutations[var48] + var61;
                     var50 = this.permutations[var48 + 1] + var61;
                     var51 = this.permutations[var41 + 1] + var68;
                     var27 = this.permutations[var51] + var61;
                     var52 = this.permutations[var51 + 1] + var61;
                     var53 = this.lerp(var42, this.grad(this.permutations[var49], var38, var65, var58), this.grad(this.permutations[var27], var38 - 1.0D, var65, var58));
                     var31 = this.lerp(var42, this.grad(this.permutations[var50], var38, var65 - 1.0D, var58), this.grad(this.permutations[var52], var38 - 1.0D, var65 - 1.0D, var58));
                     var55 = this.lerp(var42, this.grad(this.permutations[var49 + 1], var38, var65, var58 - 1.0D), this.grad(this.permutations[var27 + 1], var38 - 1.0D, var65, var58 - 1.0D));
                     var35 = this.lerp(var42, this.grad(this.permutations[var50 + 1], var38, var65 - 1.0D, var58 - 1.0D), this.grad(this.permutations[var52 + 1], var38 - 1.0D, var65 - 1.0D, var58 - 1.0D));
                  }

                  double var71 = this.lerp(var69, var53, var31);
                  double var73 = this.lerp(var69, var55, var35);
                  double var75 = this.lerp(var62, var71, var73);
                  int var10001 = var19++;
                  var1[var10001] += var75 * var46;
               }
            }
         }

      }
   }
}
