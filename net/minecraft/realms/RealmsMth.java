package net.minecraft.realms;

import java.util.Random;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;

@SideOnly(Side.CLIENT)
public class RealmsMth {
   public static float sin(float var0) {
      return MathHelper.sin(var0);
   }

   public static double nextDouble(Random var0, double var1, double var3) {
      return MathHelper.nextDouble(var0, var1, var3);
   }

   public static int ceil(float var0) {
      return MathHelper.ceil(var0);
   }

   public static int floor(double var0) {
      return MathHelper.floor(var0);
   }

   public static int intFloorDiv(int var0, int var1) {
      return MathHelper.intFloorDiv(var0, var1);
   }

   public static float abs(float var0) {
      return MathHelper.abs(var0);
   }

   public static int clamp(int var0, int var1, int var2) {
      return MathHelper.clamp(var0, var1, var2);
   }

   public static double clampedLerp(double var0, double var2, double var4) {
      return MathHelper.clampedLerp(var0, var2, var4);
   }

   public static int ceil(double var0) {
      return MathHelper.ceil(var0);
   }

   public static boolean isEmpty(String var0) {
      return StringUtils.isEmpty(var0);
   }

   public static long lfloor(double var0) {
      return MathHelper.lfloor(var0);
   }

   public static float sqrt(double var0) {
      return MathHelper.sqrt(var0);
   }

   public static double clamp(double var0, double var2, double var4) {
      return MathHelper.clamp(var0, var2, var4);
   }

   public static int getInt(String var0, int var1) {
      return MathHelper.getInt(var0, var1);
   }

   public static double getDouble(String var0, double var1) {
      return MathHelper.getDouble(var0, var1);
   }

   public static int log2(int var0) {
      return MathHelper.log2(var0);
   }

   public static int absFloor(double var0) {
      return MathHelper.absFloor(var0);
   }

   public static int smallestEncompassingPowerOfTwo(int var0) {
      return MathHelper.smallestEncompassingPowerOfTwo(var0);
   }

   public static float sqrt(float var0) {
      return MathHelper.sqrt(var0);
   }

   public static float cos(float var0) {
      return MathHelper.cos(var0);
   }

   public static int getInt(String var0, int var1, int var2) {
      return MathHelper.getInt(var0, var1, var2);
   }

   public static int fastFloor(double var0) {
      return MathHelper.fastFloor(var0);
   }

   public static double absMax(double var0, double var2) {
      return MathHelper.absMax(var0, var2);
   }

   public static float nextFloat(Random var0, float var1, float var2) {
      return MathHelper.nextFloat(var0, var1, var2);
   }

   public static double wrapDegrees(double var0) {
      return MathHelper.wrapDegrees(var0);
   }

   public static float wrapDegrees(float var0) {
      return MathHelper.wrapDegrees(var0);
   }

   public static float clamp(float var0, float var1, float var2) {
      return MathHelper.clamp(var0, var1, var2);
   }

   public static double getDouble(String var0, double var1, double var3) {
      return MathHelper.getDouble(var0, var1, var3);
   }

   public static int roundUp(int var0, int var1) {
      return MathHelper.roundUp(var0, var1);
   }

   public static double average(long[] var0) {
      return MathHelper.average(var0);
   }

   public static int floor(float var0) {
      return MathHelper.floor(var0);
   }

   public static int abs(int var0) {
      return MathHelper.abs(var0);
   }

   public static int nextInt(Random var0, int var1, int var2) {
      return MathHelper.getInt(var0, var1, var2);
   }
}
