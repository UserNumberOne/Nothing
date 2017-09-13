package net.minecraft.util.math;

import javax.annotation.Nullable;

public class Vec3d {
   public static final Vec3d ZERO = new Vec3d(0.0D, 0.0D, 0.0D);
   public final double xCoord;
   public final double yCoord;
   public final double zCoord;

   public Vec3d(double var1, double var3, double var5) {
      if (var1 == -0.0D) {
         var1 = 0.0D;
      }

      if (var3 == -0.0D) {
         var3 = 0.0D;
      }

      if (var5 == -0.0D) {
         var5 = 0.0D;
      }

      this.xCoord = var1;
      this.yCoord = var3;
      this.zCoord = var5;
   }

   public Vec3d(Vec3i var1) {
      this((double)var1.getX(), (double)var1.getY(), (double)var1.getZ());
   }

   public Vec3d subtractReverse(Vec3d var1) {
      return new Vec3d(var1.xCoord - this.xCoord, var1.yCoord - this.yCoord, var1.zCoord - this.zCoord);
   }

   public Vec3d normalize() {
      double var1 = (double)MathHelper.sqrt(this.xCoord * this.xCoord + this.yCoord * this.yCoord + this.zCoord * this.zCoord);
      return var1 < 1.0E-4D ? ZERO : new Vec3d(this.xCoord / var1, this.yCoord / var1, this.zCoord / var1);
   }

   public double dotProduct(Vec3d var1) {
      return this.xCoord * var1.xCoord + this.yCoord * var1.yCoord + this.zCoord * var1.zCoord;
   }

   public Vec3d crossProduct(Vec3d var1) {
      return new Vec3d(this.yCoord * var1.zCoord - this.zCoord * var1.yCoord, this.zCoord * var1.xCoord - this.xCoord * var1.zCoord, this.xCoord * var1.yCoord - this.yCoord * var1.xCoord);
   }

   public Vec3d subtract(Vec3d var1) {
      return this.subtract(var1.xCoord, var1.yCoord, var1.zCoord);
   }

   public Vec3d subtract(double var1, double var3, double var5) {
      return this.addVector(-var1, -var3, -var5);
   }

   public Vec3d add(Vec3d var1) {
      return this.addVector(var1.xCoord, var1.yCoord, var1.zCoord);
   }

   public Vec3d addVector(double var1, double var3, double var5) {
      return new Vec3d(this.xCoord + var1, this.yCoord + var3, this.zCoord + var5);
   }

   public double distanceTo(Vec3d var1) {
      double var2 = var1.xCoord - this.xCoord;
      double var4 = var1.yCoord - this.yCoord;
      double var6 = var1.zCoord - this.zCoord;
      return (double)MathHelper.sqrt(var2 * var2 + var4 * var4 + var6 * var6);
   }

   public double squareDistanceTo(Vec3d var1) {
      double var2 = var1.xCoord - this.xCoord;
      double var4 = var1.yCoord - this.yCoord;
      double var6 = var1.zCoord - this.zCoord;
      return var2 * var2 + var4 * var4 + var6 * var6;
   }

   public double squareDistanceTo(double var1, double var3, double var5) {
      double var7 = var1 - this.xCoord;
      double var9 = var3 - this.yCoord;
      double var11 = var5 - this.zCoord;
      return var7 * var7 + var9 * var9 + var11 * var11;
   }

   public Vec3d scale(double var1) {
      return new Vec3d(this.xCoord * var1, this.yCoord * var1, this.zCoord * var1);
   }

   public double lengthVector() {
      return (double)MathHelper.sqrt(this.xCoord * this.xCoord + this.yCoord * this.yCoord + this.zCoord * this.zCoord);
   }

   public double lengthSquared() {
      return this.xCoord * this.xCoord + this.yCoord * this.yCoord + this.zCoord * this.zCoord;
   }

   @Nullable
   public Vec3d getIntermediateWithXValue(Vec3d var1, double var2) {
      double var4 = var1.xCoord - this.xCoord;
      double var6 = var1.yCoord - this.yCoord;
      double var8 = var1.zCoord - this.zCoord;
      if (var4 * var4 < 1.0000000116860974E-7D) {
         return null;
      } else {
         double var10 = (var2 - this.xCoord) / var4;
         return var10 >= 0.0D && var10 <= 1.0D ? new Vec3d(this.xCoord + var4 * var10, this.yCoord + var6 * var10, this.zCoord + var8 * var10) : null;
      }
   }

   @Nullable
   public Vec3d getIntermediateWithYValue(Vec3d var1, double var2) {
      double var4 = var1.xCoord - this.xCoord;
      double var6 = var1.yCoord - this.yCoord;
      double var8 = var1.zCoord - this.zCoord;
      if (var6 * var6 < 1.0000000116860974E-7D) {
         return null;
      } else {
         double var10 = (var2 - this.yCoord) / var6;
         return var10 >= 0.0D && var10 <= 1.0D ? new Vec3d(this.xCoord + var4 * var10, this.yCoord + var6 * var10, this.zCoord + var8 * var10) : null;
      }
   }

   @Nullable
   public Vec3d getIntermediateWithZValue(Vec3d var1, double var2) {
      double var4 = var1.xCoord - this.xCoord;
      double var6 = var1.yCoord - this.yCoord;
      double var8 = var1.zCoord - this.zCoord;
      if (var8 * var8 < 1.0000000116860974E-7D) {
         return null;
      } else {
         double var10 = (var2 - this.zCoord) / var8;
         return var10 >= 0.0D && var10 <= 1.0D ? new Vec3d(this.xCoord + var4 * var10, this.yCoord + var6 * var10, this.zCoord + var8 * var10) : null;
      }
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof Vec3d)) {
         return false;
      } else {
         Vec3d var2 = (Vec3d)var1;
         return Double.compare(var2.xCoord, this.xCoord) != 0 ? false : (Double.compare(var2.yCoord, this.yCoord) != 0 ? false : Double.compare(var2.zCoord, this.zCoord) == 0);
      }
   }

   public int hashCode() {
      long var1 = Double.doubleToLongBits(this.xCoord);
      int var3 = (int)(var1 ^ var1 >>> 32);
      var1 = Double.doubleToLongBits(this.yCoord);
      var3 = 31 * var3 + (int)(var1 ^ var1 >>> 32);
      var1 = Double.doubleToLongBits(this.zCoord);
      var3 = 31 * var3 + (int)(var1 ^ var1 >>> 32);
      return var3;
   }

   public String toString() {
      return "(" + this.xCoord + ", " + this.yCoord + ", " + this.zCoord + ")";
   }

   public Vec3d rotatePitch(float var1) {
      float var2 = MathHelper.cos(var1);
      float var3 = MathHelper.sin(var1);
      double var4 = this.xCoord;
      double var6 = this.yCoord * (double)var2 + this.zCoord * (double)var3;
      double var8 = this.zCoord * (double)var2 - this.yCoord * (double)var3;
      return new Vec3d(var4, var6, var8);
   }

   public Vec3d rotateYaw(float var1) {
      float var2 = MathHelper.cos(var1);
      float var3 = MathHelper.sin(var1);
      double var4 = this.xCoord * (double)var2 + this.zCoord * (double)var3;
      double var6 = this.yCoord;
      double var8 = this.zCoord * (double)var2 - this.xCoord * (double)var3;
      return new Vec3d(var4, var6, var8);
   }

   public static Vec3d fromPitchYawVector(Vec2f var0) {
      return fromPitchYaw(var0.x, var0.y);
   }

   public static Vec3d fromPitchYaw(float var0, float var1) {
      float var2 = MathHelper.cos(-var1 * 0.017453292F - 3.1415927F);
      float var3 = MathHelper.sin(-var1 * 0.017453292F - 3.1415927F);
      float var4 = -MathHelper.cos(-var0 * 0.017453292F);
      float var5 = MathHelper.sin(-var0 * 0.017453292F);
      return new Vec3d((double)(var3 * var4), (double)var5, (double)(var2 * var4));
   }
}
