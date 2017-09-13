package net.minecraft.util.math;

import java.util.Random;
import java.util.UUID;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MathHelper {
   public static final float SQRT_2 = sqrt(2.0F);
   private static final float[] SIN_TABLE = new float[65536];
   private static final Random RANDOM = new Random();
   private static final int[] MULTIPLY_DE_BRUIJN_BIT_POSITION;
   private static final double FRAC_BIAS;
   private static final double[] ASINE_TAB;
   private static final double[] COS_TAB;

   public static float sin(float var0) {
      return SIN_TABLE[(int)(var0 * 10430.378F) & '\uffff'];
   }

   public static float cos(float var0) {
      return SIN_TABLE[(int)(var0 * 10430.378F + 16384.0F) & '\uffff'];
   }

   public static float sqrt(float var0) {
      return (float)Math.sqrt((double)var0);
   }

   public static float sqrt(double var0) {
      return (float)Math.sqrt(var0);
   }

   public static int floor(float var0) {
      int var1 = (int)var0;
      return var0 < (float)var1 ? var1 - 1 : var1;
   }

   @SideOnly(Side.CLIENT)
   public static int fastFloor(double var0) {
      return (int)(var0 + 1024.0D) - 1024;
   }

   public static int floor(double var0) {
      int var2 = (int)var0;
      return var0 < (double)var2 ? var2 - 1 : var2;
   }

   public static long lfloor(double var0) {
      long var2 = (long)var0;
      return var0 < (double)var2 ? var2 - 1L : var2;
   }

   @SideOnly(Side.CLIENT)
   public static int absFloor(double var0) {
      return (int)(var0 >= 0.0D ? var0 : -var0 + 1.0D);
   }

   public static float abs(float var0) {
      return var0 >= 0.0F ? var0 : -var0;
   }

   public static int abs(int var0) {
      return var0 >= 0 ? var0 : -var0;
   }

   public static int ceil(float var0) {
      int var1 = (int)var0;
      return var0 > (float)var1 ? var1 + 1 : var1;
   }

   public static int ceil(double var0) {
      int var2 = (int)var0;
      return var0 > (double)var2 ? var2 + 1 : var2;
   }

   public static int clamp(int var0, int var1, int var2) {
      return var0 < var1 ? var1 : (var0 > var2 ? var2 : var0);
   }

   public static float clamp(float var0, float var1, float var2) {
      return var0 < var1 ? var1 : (var0 > var2 ? var2 : var0);
   }

   public static double clamp(double var0, double var2, double var4) {
      return var0 < var2 ? var2 : (var0 > var4 ? var4 : var0);
   }

   public static double clampedLerp(double var0, double var2, double var4) {
      return var4 < 0.0D ? var0 : (var4 > 1.0D ? var2 : var0 + (var2 - var0) * var4);
   }

   public static double absMax(double var0, double var2) {
      if (var0 < 0.0D) {
         var0 = -var0;
      }

      if (var2 < 0.0D) {
         var2 = -var2;
      }

      return var0 > var2 ? var0 : var2;
   }

   @SideOnly(Side.CLIENT)
   public static int intFloorDiv(int var0, int var1) {
      return var0 < 0 ? -((-var0 - 1) / var1) - 1 : var0 / var1;
   }

   public static int getInt(Random var0, int var1, int var2) {
      return var1 >= var2 ? var1 : var0.nextInt(var2 - var1 + 1) + var1;
   }

   public static float nextFloat(Random var0, float var1, float var2) {
      return var1 >= var2 ? var1 : var0.nextFloat() * (var2 - var1) + var1;
   }

   public static double nextDouble(Random var0, double var1, double var3) {
      return var1 >= var3 ? var1 : var0.nextDouble() * (var3 - var1) + var1;
   }

   public static double average(long[] var0) {
      long var1 = 0L;

      for(long var6 : var0) {
         var1 += var6;
      }

      return (double)var1 / (double)var0.length;
   }

   @SideOnly(Side.CLIENT)
   public static boolean epsilonEquals(float var0, float var1) {
      return abs(var1 - var0) < 1.0E-5F;
   }

   @SideOnly(Side.CLIENT)
   public static int normalizeAngle(int var0, int var1) {
      return (var0 % var1 + var1) % var1;
   }

   @SideOnly(Side.CLIENT)
   public static float positiveModulo(float var0, float var1) {
      return (var0 % var1 + var1) % var1;
   }

   public static float wrapDegrees(float var0) {
      var0 = var0 % 360.0F;
      if (var0 >= 180.0F) {
         var0 -= 360.0F;
      }

      if (var0 < -180.0F) {
         var0 += 360.0F;
      }

      return var0;
   }

   public static double wrapDegrees(double var0) {
      var0 = var0 % 360.0D;
      if (var0 >= 180.0D) {
         var0 -= 360.0D;
      }

      if (var0 < -180.0D) {
         var0 += 360.0D;
      }

      return var0;
   }

   public static int clampAngle(int var0) {
      var0 = var0 % 360;
      if (var0 >= 180) {
         var0 -= 360;
      }

      if (var0 < -180) {
         var0 += 360;
      }

      return var0;
   }

   public static int getInt(String var0, int var1) {
      try {
         return Integer.parseInt(var0);
      } catch (Throwable var3) {
         return var1;
      }
   }

   public static int getInt(String var0, int var1, int var2) {
      return Math.max(var2, getInt(var0, var1));
   }

   public static double getDouble(String var0, double var1) {
      try {
         return Double.parseDouble(var0);
      } catch (Throwable var4) {
         return var1;
      }
   }

   public static double getDouble(String var0, double var1, double var3) {
      return Math.max(var3, getDouble(var0, var1));
   }

   public static int smallestEncompassingPowerOfTwo(int var0) {
      int var1 = var0 - 1;
      var1 = var1 | var1 >> 1;
      var1 = var1 | var1 >> 2;
      var1 = var1 | var1 >> 4;
      var1 = var1 | var1 >> 8;
      var1 = var1 | var1 >> 16;
      return var1 + 1;
   }

   private static boolean isPowerOfTwo(int var0) {
      return var0 != 0 && (var0 & var0 - 1) == 0;
   }

   public static int log2DeBruijn(int var0) {
      var0 = isPowerOfTwo(var0) ? var0 : smallestEncompassingPowerOfTwo(var0);
      return MULTIPLY_DE_BRUIJN_BIT_POSITION[(int)((long)var0 * 125613361L >> 27) & 31];
   }

   public static int log2(int var0) {
      return log2DeBruijn(var0) - (isPowerOfTwo(var0) ? 0 : 1);
   }

   public static int roundUp(int var0, int var1) {
      if (var1 == 0) {
         return 0;
      } else if (var0 == 0) {
         return var1;
      } else {
         if (var0 < 0) {
            var1 *= -1;
         }

         int var2 = var0 % var1;
         return var2 == 0 ? var0 : var0 + var1 - var2;
      }
   }

   @SideOnly(Side.CLIENT)
   public static int rgb(float var0, float var1, float var2) {
      return rgb(floor(var0 * 255.0F), floor(var1 * 255.0F), floor(var2 * 255.0F));
   }

   @SideOnly(Side.CLIENT)
   public static int rgb(int var0, int var1, int var2) {
      int var3 = (var0 << 8) + var1;
      var3 = (var3 << 8) + var2;
      return var3;
   }

   @SideOnly(Side.CLIENT)
   public static int multiplyColor(int var0, int var1) {
      int var2 = (var0 & 16711680) >> 16;
      int var3 = (var1 & 16711680) >> 16;
      int var4 = (var0 & '\uff00') >> 8;
      int var5 = (var1 & '\uff00') >> 8;
      int var6 = (var0 & 255) >> 0;
      int var7 = (var1 & 255) >> 0;
      int var8 = (int)((float)var2 * (float)var3 / 255.0F);
      int var9 = (int)((float)var4 * (float)var5 / 255.0F);
      int var10 = (int)((float)var6 * (float)var7 / 255.0F);
      return var0 & -16777216 | var8 << 16 | var9 << 8 | var10;
   }

   @SideOnly(Side.CLIENT)
   public static double frac(double var0) {
      return var0 - Math.floor(var0);
   }

   @SideOnly(Side.CLIENT)
   public static long getPositionRandom(Vec3i var0) {
      return getCoordinateRandom(var0.getX(), var0.getY(), var0.getZ());
   }

   public static UUID getRandomUUID(Random var0) {
      long var1 = var0.nextLong() & -61441L | 16384L;
      long var3 = var0.nextLong() & 4611686018427387903L | Long.MIN_VALUE;
      return new UUID(var1, var3);
   }

   public static UUID getRandomUUID() {
      return getRandomUUID(RANDOM);
   }

   @SideOnly(Side.CLIENT)
   public static long getCoordinateRandom(int var0, int var1, int var2) {
      long var3 = (long)(var0 * 3129871) ^ (long)var2 * 116129781L ^ (long)var1;
      var3 = var3 * var3 * 42317861L + var3 * 11L;
      return var3;
   }

   public static double pct(double var0, double var2, double var4) {
      return (var0 - var2) / (var4 - var2);
   }

   public static double atan2(double var0, double var2) {
      double var4 = var2 * var2 + var0 * var0;
      if (Double.isNaN(var4)) {
         return Double.NaN;
      } else {
         boolean var6 = var0 < 0.0D;
         if (var6) {
            var0 = -var0;
         }

         boolean var7 = var2 < 0.0D;
         if (var7) {
            var2 = -var2;
         }

         boolean var8 = var0 > var2;
         if (var8) {
            double var9 = var2;
            var2 = var0;
            var0 = var9;
         }

         double var28 = fastInvSqrt(var4);
         var2 = var2 * var28;
         var0 = var0 * var28;
         double var11 = FRAC_BIAS + var0;
         int var13 = (int)Double.doubleToRawLongBits(var11);
         double var14 = ASINE_TAB[var13];
         double var16 = COS_TAB[var13];
         double var18 = var11 - FRAC_BIAS;
         double var20 = var0 * var16 - var2 * var18;
         double var22 = (6.0D + var20 * var20) * var20 * 0.16666666666666666D;
         double var24 = var14 + var22;
         if (var8) {
            var24 = 1.5707963267948966D - var24;
         }

         if (var7) {
            var24 = 3.141592653589793D - var24;
         }

         if (var6) {
            var24 = -var24;
         }

         return var24;
      }
   }

   public static double fastInvSqrt(double var0) {
      double var2 = 0.5D * var0;
      long var4 = Double.doubleToRawLongBits(var0);
      var4 = 6910469410427058090L - (var4 >> 1);
      var0 = Double.longBitsToDouble(var4);
      var0 = var0 * (1.5D - var2 * var0 * var0);
      return var0;
   }

   @SideOnly(Side.CLIENT)
   public static int hsvToRGB(float var0, float var1, float var2) {
      int var3 = (int)(var0 * 6.0F) % 6;
      float var4 = var0 * 6.0F - (float)var3;
      float var5 = var2 * (1.0F - var1);
      float var6 = var2 * (1.0F - var4 * var1);
      float var7 = var2 * (1.0F - (1.0F - var4) * var1);
      float var8;
      float var9;
      float var10;
      switch(var3) {
      case 0:
         var8 = var2;
         var9 = var7;
         var10 = var5;
         break;
      case 1:
         var8 = var6;
         var9 = var2;
         var10 = var5;
         break;
      case 2:
         var8 = var5;
         var9 = var2;
         var10 = var7;
         break;
      case 3:
         var8 = var5;
         var9 = var6;
         var10 = var2;
         break;
      case 4:
         var8 = var7;
         var9 = var5;
         var10 = var2;
         break;
      case 5:
         var8 = var2;
         var9 = var5;
         var10 = var6;
         break;
      default:
         throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + var0 + ", " + var1 + ", " + var2);
      }

      int var11 = clamp((int)(var8 * 255.0F), 0, 255);
      int var12 = clamp((int)(var9 * 255.0F), 0, 255);
      int var13 = clamp((int)(var10 * 255.0F), 0, 255);
      return var11 << 16 | var12 << 8 | var13;
   }

   public static int hash(int var0) {
      var0 = var0 ^ var0 >>> 16;
      var0 = var0 * -2048144789;
      var0 = var0 ^ var0 >>> 13;
      var0 = var0 * -1028477387;
      var0 = var0 ^ var0 >>> 16;
      return var0;
   }

   static {
      for(int var0 = 0; var0 < 65536; ++var0) {
         SIN_TABLE[var0] = (float)Math.sin((double)var0 * 3.141592653589793D * 2.0D / 65536.0D);
      }

      MULTIPLY_DE_BRUIJN_BIT_POSITION = new int[]{0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9};
      FRAC_BIAS = Double.longBitsToDouble(4805340802404319232L);
      ASINE_TAB = new double[257];
      COS_TAB = new double[257];

      for(int var5 = 0; var5 < 257; ++var5) {
         double var1 = (double)var5 / 256.0D;
         double var3 = Math.asin(var1);
         COS_TAB[var5] = Math.cos(var3);
         ASINE_TAB[var5] = var3;
      }

   }
}
