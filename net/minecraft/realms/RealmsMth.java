package net.minecraft.realms;

import java.util.Random;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;

@SideOnly(Side.CLIENT)
public class RealmsMth {
   public static float sin(float var0) {
      return MathHelper.sin(p_sin_0_);
   }

   public static double nextDouble(Random var0, double var1, double var3) {
      return MathHelper.nextDouble(p_nextDouble_0_, p_nextDouble_1_, p_nextDouble_3_);
   }

   public static int ceil(float var0) {
      return MathHelper.ceil(p_ceil_0_);
   }

   public static int floor(double var0) {
      return MathHelper.floor(p_floor_0_);
   }

   public static int intFloorDiv(int var0, int var1) {
      return MathHelper.intFloorDiv(p_intFloorDiv_0_, p_intFloorDiv_1_);
   }

   public static float abs(float var0) {
      return MathHelper.abs(p_abs_0_);
   }

   public static int clamp(int var0, int var1, int var2) {
      return MathHelper.clamp(p_clamp_0_, p_clamp_1_, p_clamp_2_);
   }

   public static double clampedLerp(double var0, double var2, double var4) {
      return MathHelper.clampedLerp(p_clampedLerp_0_, p_clampedLerp_2_, p_clampedLerp_4_);
   }

   public static int ceil(double var0) {
      return MathHelper.ceil(p_ceil_0_);
   }

   public static boolean isEmpty(String var0) {
      return StringUtils.isEmpty(p_isEmpty_0_);
   }

   public static long lfloor(double var0) {
      return MathHelper.lfloor(p_lfloor_0_);
   }

   public static float sqrt(double var0) {
      return MathHelper.sqrt(p_sqrt_0_);
   }

   public static double clamp(double var0, double var2, double var4) {
      return MathHelper.clamp(p_clamp_0_, p_clamp_2_, p_clamp_4_);
   }

   public static int getInt(String var0, int var1) {
      return MathHelper.getInt(p_getInt_0_, p_getInt_1_);
   }

   public static double getDouble(String var0, double var1) {
      return MathHelper.getDouble(p_getDouble_0_, p_getDouble_1_);
   }

   public static int log2(int var0) {
      return MathHelper.log2(p_log2_0_);
   }

   public static int absFloor(double var0) {
      return MathHelper.absFloor(p_absFloor_0_);
   }

   public static int smallestEncompassingPowerOfTwo(int var0) {
      return MathHelper.smallestEncompassingPowerOfTwo(p_smallestEncompassingPowerOfTwo_0_);
   }

   public static float sqrt(float var0) {
      return MathHelper.sqrt(p_sqrt_0_);
   }

   public static float cos(float var0) {
      return MathHelper.cos(p_cos_0_);
   }

   public static int getInt(String var0, int var1, int var2) {
      return MathHelper.getInt(p_getInt_0_, p_getInt_1_, p_getInt_2_);
   }

   public static int fastFloor(double var0) {
      return MathHelper.fastFloor(p_fastFloor_0_);
   }

   public static double absMax(double var0, double var2) {
      return MathHelper.absMax(p_absMax_0_, p_absMax_2_);
   }

   public static float nextFloat(Random var0, float var1, float var2) {
      return MathHelper.nextFloat(p_nextFloat_0_, p_nextFloat_1_, p_nextFloat_2_);
   }

   public static double wrapDegrees(double var0) {
      return MathHelper.wrapDegrees(p_wrapDegrees_0_);
   }

   public static float wrapDegrees(float var0) {
      return MathHelper.wrapDegrees(p_wrapDegrees_0_);
   }

   public static float clamp(float var0, float var1, float var2) {
      return MathHelper.clamp(p_clamp_0_, p_clamp_1_, p_clamp_2_);
   }

   public static double getDouble(String var0, double var1, double var3) {
      return MathHelper.getDouble(p_getDouble_0_, p_getDouble_1_, p_getDouble_3_);
   }

   public static int roundUp(int var0, int var1) {
      return MathHelper.roundUp(p_roundUp_0_, p_roundUp_1_);
   }

   public static double average(long[] var0) {
      return MathHelper.average(p_average_0_);
   }

   public static int floor(float var0) {
      return MathHelper.floor(p_floor_0_);
   }

   public static int abs(int var0) {
      return MathHelper.abs(p_abs_0_);
   }

   public static int nextInt(Random var0, int var1, int var2) {
      return MathHelper.getInt(p_nextInt_0_, p_nextInt_1_, p_nextInt_2_);
   }
}
