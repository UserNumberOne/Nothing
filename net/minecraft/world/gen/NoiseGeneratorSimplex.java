package net.minecraft.world.gen;

import java.util.Random;

public class NoiseGeneratorSimplex {
   private static final int[][] grad3 = new int[][]{{1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0}, {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1}, {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}};
   public static final double SQRT_3 = Math.sqrt(3.0D);
   private final int[] p;
   public double xo;
   public double yo;
   public double zo;
   private static final double F2 = 0.5D * (SQRT_3 - 1.0D);
   private static final double G2 = (3.0D - SQRT_3) / 6.0D;

   public NoiseGeneratorSimplex() {
      this(new Random());
   }

   public NoiseGeneratorSimplex(Random var1) {
      this.p = new int[512];
      this.xo = var1.nextDouble() * 256.0D;
      this.yo = var1.nextDouble() * 256.0D;
      this.zo = var1.nextDouble() * 256.0D;

      for(int var2 = 0; var2 < 256; this.p[var2] = var2++) {
         ;
      }

      for(int var5 = 0; var5 < 256; ++var5) {
         int var3 = var1.nextInt(256 - var5) + var5;
         int var4 = this.p[var5];
         this.p[var5] = this.p[var3];
         this.p[var3] = var4;
         this.p[var5 + 256] = this.p[var5];
      }

   }

   private static int fastFloor(double var0) {
      return var0 > 0.0D ? (int)var0 : (int)var0 - 1;
   }

   private static double dot(int[] var0, double var1, double var3) {
      return (double)var0[0] * var1 + (double)var0[1] * var3;
   }

   public double getValue(double var1, double var3) {
      double var5 = 0.5D * (SQRT_3 - 1.0D);
      double var7 = (var1 + var3) * var5;
      int var9 = fastFloor(var1 + var7);
      int var10 = fastFloor(var3 + var7);
      double var11 = (3.0D - SQRT_3) / 6.0D;
      double var13 = (double)(var9 + var10) * var11;
      double var15 = (double)var9 - var13;
      double var17 = (double)var10 - var13;
      double var19 = var1 - var15;
      double var21 = var3 - var17;
      byte var23;
      byte var24;
      if (var19 > var21) {
         var23 = 1;
         var24 = 0;
      } else {
         var23 = 0;
         var24 = 1;
      }

      double var25 = var19 - (double)var23 + var11;
      double var27 = var21 - (double)var24 + var11;
      double var29 = var19 - 1.0D + 2.0D * var11;
      double var31 = var21 - 1.0D + 2.0D * var11;
      int var33 = var9 & 255;
      int var34 = var10 & 255;
      int var35 = this.p[var33 + this.p[var34]] % 12;
      int var36 = this.p[var33 + var23 + this.p[var34 + var24]] % 12;
      int var37 = this.p[var33 + 1 + this.p[var34 + 1]] % 12;
      double var38 = 0.5D - var19 * var19 - var21 * var21;
      double var40;
      if (var38 < 0.0D) {
         var40 = 0.0D;
      } else {
         var38 = var38 * var38;
         var40 = var38 * var38 * dot(grad3[var35], var19, var21);
      }

      double var42 = 0.5D - var25 * var25 - var27 * var27;
      double var44;
      if (var42 < 0.0D) {
         var44 = 0.0D;
      } else {
         var42 = var42 * var42;
         var44 = var42 * var42 * dot(grad3[var36], var25, var27);
      }

      double var46 = 0.5D - var29 * var29 - var31 * var31;
      double var48;
      if (var46 < 0.0D) {
         var48 = 0.0D;
      } else {
         var46 = var46 * var46;
         var48 = var46 * var46 * dot(grad3[var37], var29, var31);
      }

      return 70.0D * (var40 + var44 + var48);
   }

   public void add(double[] var1, double var2, double var4, int var6, int var7, double var8, double var10, double var12) {
      int var14 = 0;

      for(int var15 = 0; var15 < var7; ++var15) {
         double var16 = (var4 + (double)var15) * var10 + this.yo;

         for(int var18 = 0; var18 < var6; ++var18) {
            double var19 = (var2 + (double)var18) * var8 + this.xo;
            double var21 = (var19 + var16) * F2;
            int var23 = fastFloor(var19 + var21);
            int var24 = fastFloor(var16 + var21);
            double var25 = (double)(var23 + var24) * G2;
            double var27 = (double)var23 - var25;
            double var29 = (double)var24 - var25;
            double var31 = var19 - var27;
            double var33 = var16 - var29;
            byte var35;
            byte var36;
            if (var31 > var33) {
               var35 = 1;
               var36 = 0;
            } else {
               var35 = 0;
               var36 = 1;
            }

            double var37 = var31 - (double)var35 + G2;
            double var39 = var33 - (double)var36 + G2;
            double var41 = var31 - 1.0D + 2.0D * G2;
            double var43 = var33 - 1.0D + 2.0D * G2;
            int var45 = var23 & 255;
            int var46 = var24 & 255;
            int var47 = this.p[var45 + this.p[var46]] % 12;
            int var48 = this.p[var45 + var35 + this.p[var46 + var36]] % 12;
            int var49 = this.p[var45 + 1 + this.p[var46 + 1]] % 12;
            double var50 = 0.5D - var31 * var31 - var33 * var33;
            double var52;
            if (var50 < 0.0D) {
               var52 = 0.0D;
            } else {
               var50 = var50 * var50;
               var52 = var50 * var50 * dot(grad3[var47], var31, var33);
            }

            double var54 = 0.5D - var37 * var37 - var39 * var39;
            double var56;
            if (var54 < 0.0D) {
               var56 = 0.0D;
            } else {
               var54 = var54 * var54;
               var56 = var54 * var54 * dot(grad3[var48], var37, var39);
            }

            double var58 = 0.5D - var41 * var41 - var43 * var43;
            double var60;
            if (var58 < 0.0D) {
               var60 = 0.0D;
            } else {
               var58 = var58 * var58;
               var60 = var58 * var58 * dot(grad3[var49], var41, var43);
            }

            int var10001 = var14++;
            var1[var10001] += 70.0D * (var52 + var56 + var60) * var12;
         }
      }

   }
}
