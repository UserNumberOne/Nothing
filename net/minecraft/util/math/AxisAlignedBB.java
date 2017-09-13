package net.minecraft.util.math;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;
import net.minecraft.util.EnumFacing;

public class AxisAlignedBB {
   public final double minX;
   public final double minY;
   public final double minZ;
   public final double maxX;
   public final double maxY;
   public final double maxZ;

   public AxisAlignedBB(double var1, double var3, double var5, double var7, double var9, double var11) {
      this.minX = Math.min(var1, var7);
      this.minY = Math.min(var3, var9);
      this.minZ = Math.min(var5, var11);
      this.maxX = Math.max(var1, var7);
      this.maxY = Math.max(var3, var9);
      this.maxZ = Math.max(var5, var11);
   }

   public AxisAlignedBB(BlockPos var1) {
      this((double)var1.getX(), (double)var1.getY(), (double)var1.getZ(), (double)(var1.getX() + 1), (double)(var1.getY() + 1), (double)(var1.getZ() + 1));
   }

   public AxisAlignedBB(BlockPos var1, BlockPos var2) {
      this((double)var1.getX(), (double)var1.getY(), (double)var1.getZ(), (double)var2.getX(), (double)var2.getY(), (double)var2.getZ());
   }

   public AxisAlignedBB setMaxY(double var1) {
      return new AxisAlignedBB(this.minX, this.minY, this.minZ, this.maxX, var1, this.maxZ);
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof AxisAlignedBB)) {
         return false;
      } else {
         AxisAlignedBB var2 = (AxisAlignedBB)var1;
         if (Double.compare(var2.minX, this.minX) != 0) {
            return false;
         } else if (Double.compare(var2.minY, this.minY) != 0) {
            return false;
         } else if (Double.compare(var2.minZ, this.minZ) != 0) {
            return false;
         } else if (Double.compare(var2.maxX, this.maxX) != 0) {
            return false;
         } else if (Double.compare(var2.maxY, this.maxY) != 0) {
            return false;
         } else {
            return Double.compare(var2.maxZ, this.maxZ) == 0;
         }
      }
   }

   public int hashCode() {
      long var1 = Double.doubleToLongBits(this.minX);
      int var3 = (int)(var1 ^ var1 >>> 32);
      var1 = Double.doubleToLongBits(this.minY);
      var3 = 31 * var3 + (int)(var1 ^ var1 >>> 32);
      var1 = Double.doubleToLongBits(this.minZ);
      var3 = 31 * var3 + (int)(var1 ^ var1 >>> 32);
      var1 = Double.doubleToLongBits(this.maxX);
      var3 = 31 * var3 + (int)(var1 ^ var1 >>> 32);
      var1 = Double.doubleToLongBits(this.maxY);
      var3 = 31 * var3 + (int)(var1 ^ var1 >>> 32);
      var1 = Double.doubleToLongBits(this.maxZ);
      var3 = 31 * var3 + (int)(var1 ^ var1 >>> 32);
      return var3;
   }

   public AxisAlignedBB addCoord(double var1, double var3, double var5) {
      double var7 = this.minX;
      double var9 = this.minY;
      double var11 = this.minZ;
      double var13 = this.maxX;
      double var15 = this.maxY;
      double var17 = this.maxZ;
      if (var1 < 0.0D) {
         var7 += var1;
      } else if (var1 > 0.0D) {
         var13 += var1;
      }

      if (var3 < 0.0D) {
         var9 += var3;
      } else if (var3 > 0.0D) {
         var15 += var3;
      }

      if (var5 < 0.0D) {
         var11 += var5;
      } else if (var5 > 0.0D) {
         var17 += var5;
      }

      return new AxisAlignedBB(var7, var9, var11, var13, var15, var17);
   }

   public AxisAlignedBB expand(double var1, double var3, double var5) {
      double var7 = this.minX - var1;
      double var9 = this.minY - var3;
      double var11 = this.minZ - var5;
      double var13 = this.maxX + var1;
      double var15 = this.maxY + var3;
      double var17 = this.maxZ + var5;
      return new AxisAlignedBB(var7, var9, var11, var13, var15, var17);
   }

   public AxisAlignedBB expandXyz(double var1) {
      return this.expand(var1, var1, var1);
   }

   public AxisAlignedBB union(AxisAlignedBB var1) {
      double var2 = Math.min(this.minX, var1.minX);
      double var4 = Math.min(this.minY, var1.minY);
      double var6 = Math.min(this.minZ, var1.minZ);
      double var8 = Math.max(this.maxX, var1.maxX);
      double var10 = Math.max(this.maxY, var1.maxY);
      double var12 = Math.max(this.maxZ, var1.maxZ);
      return new AxisAlignedBB(var2, var4, var6, var8, var10, var12);
   }

   public AxisAlignedBB offset(double var1, double var3, double var5) {
      return new AxisAlignedBB(this.minX + var1, this.minY + var3, this.minZ + var5, this.maxX + var1, this.maxY + var3, this.maxZ + var5);
   }

   public AxisAlignedBB offset(BlockPos var1) {
      return new AxisAlignedBB(this.minX + (double)var1.getX(), this.minY + (double)var1.getY(), this.minZ + (double)var1.getZ(), this.maxX + (double)var1.getX(), this.maxY + (double)var1.getY(), this.maxZ + (double)var1.getZ());
   }

   public double calculateXOffset(AxisAlignedBB var1, double var2) {
      if (var1.maxY > this.minY && var1.minY < this.maxY && var1.maxZ > this.minZ && var1.minZ < this.maxZ) {
         if (var2 > 0.0D && var1.maxX <= this.minX) {
            double var6 = this.minX - var1.maxX;
            if (var6 < var2) {
               var2 = var6;
            }
         } else if (var2 < 0.0D && var1.minX >= this.maxX) {
            double var4 = this.maxX - var1.minX;
            if (var4 > var2) {
               var2 = var4;
            }
         }

         return var2;
      } else {
         return var2;
      }
   }

   public double calculateYOffset(AxisAlignedBB var1, double var2) {
      if (var1.maxX > this.minX && var1.minX < this.maxX && var1.maxZ > this.minZ && var1.minZ < this.maxZ) {
         if (var2 > 0.0D && var1.maxY <= this.minY) {
            double var6 = this.minY - var1.maxY;
            if (var6 < var2) {
               var2 = var6;
            }
         } else if (var2 < 0.0D && var1.minY >= this.maxY) {
            double var4 = this.maxY - var1.minY;
            if (var4 > var2) {
               var2 = var4;
            }
         }

         return var2;
      } else {
         return var2;
      }
   }

   public double calculateZOffset(AxisAlignedBB var1, double var2) {
      if (var1.maxX > this.minX && var1.minX < this.maxX && var1.maxY > this.minY && var1.minY < this.maxY) {
         if (var2 > 0.0D && var1.maxZ <= this.minZ) {
            double var6 = this.minZ - var1.maxZ;
            if (var6 < var2) {
               var2 = var6;
            }
         } else if (var2 < 0.0D && var1.minZ >= this.maxZ) {
            double var4 = this.maxZ - var1.minZ;
            if (var4 > var2) {
               var2 = var4;
            }
         }

         return var2;
      } else {
         return var2;
      }
   }

   public boolean intersectsWith(AxisAlignedBB var1) {
      return this.intersects(var1.minX, var1.minY, var1.minZ, var1.maxX, var1.maxY, var1.maxZ);
   }

   public boolean intersects(double var1, double var3, double var5, double var7, double var9, double var11) {
      return this.minX < var7 && this.maxX > var1 && this.minY < var9 && this.maxY > var3 && this.minZ < var11 && this.maxZ > var5;
   }

   public boolean isVecInside(Vec3d var1) {
      if (var1.xCoord > this.minX && var1.xCoord < this.maxX) {
         if (var1.yCoord > this.minY && var1.yCoord < this.maxY) {
            return var1.zCoord > this.minZ && var1.zCoord < this.maxZ;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public double getAverageEdgeLength() {
      double var1 = this.maxX - this.minX;
      double var3 = this.maxY - this.minY;
      double var5 = this.maxZ - this.minZ;
      return (var1 + var3 + var5) / 3.0D;
   }

   public AxisAlignedBB contract(double var1) {
      return this.expandXyz(-var1);
   }

   @Nullable
   public RayTraceResult calculateIntercept(Vec3d var1, Vec3d var2) {
      Vec3d var3 = this.collideWithXPlane(this.minX, var1, var2);
      EnumFacing var4 = EnumFacing.WEST;
      Vec3d var5 = this.collideWithXPlane(this.maxX, var1, var2);
      if (var5 != null && this.isClosest(var1, var3, var5)) {
         var3 = var5;
         var4 = EnumFacing.EAST;
      }

      var5 = this.collideWithYPlane(this.minY, var1, var2);
      if (var5 != null && this.isClosest(var1, var3, var5)) {
         var3 = var5;
         var4 = EnumFacing.DOWN;
      }

      var5 = this.collideWithYPlane(this.maxY, var1, var2);
      if (var5 != null && this.isClosest(var1, var3, var5)) {
         var3 = var5;
         var4 = EnumFacing.UP;
      }

      var5 = this.collideWithZPlane(this.minZ, var1, var2);
      if (var5 != null && this.isClosest(var1, var3, var5)) {
         var3 = var5;
         var4 = EnumFacing.NORTH;
      }

      var5 = this.collideWithZPlane(this.maxZ, var1, var2);
      if (var5 != null && this.isClosest(var1, var3, var5)) {
         var3 = var5;
         var4 = EnumFacing.SOUTH;
      }

      return var3 == null ? null : new RayTraceResult(var3, var4);
   }

   @VisibleForTesting
   boolean isClosest(Vec3d var1, @Nullable Vec3d var2, Vec3d var3) {
      return var2 == null || var1.squareDistanceTo(var3) < var1.squareDistanceTo(var2);
   }

   @Nullable
   @VisibleForTesting
   Vec3d collideWithXPlane(double var1, Vec3d var3, Vec3d var4) {
      Vec3d var5 = var3.getIntermediateWithXValue(var4, var1);
      return var5 != null && this.intersectsWithYZ(var5) ? var5 : null;
   }

   @Nullable
   @VisibleForTesting
   Vec3d collideWithYPlane(double var1, Vec3d var3, Vec3d var4) {
      Vec3d var5 = var3.getIntermediateWithYValue(var4, var1);
      return var5 != null && this.intersectsWithXZ(var5) ? var5 : null;
   }

   @Nullable
   @VisibleForTesting
   Vec3d collideWithZPlane(double var1, Vec3d var3, Vec3d var4) {
      Vec3d var5 = var3.getIntermediateWithZValue(var4, var1);
      return var5 != null && this.intersectsWithXY(var5) ? var5 : null;
   }

   @VisibleForTesting
   public boolean intersectsWithYZ(Vec3d var1) {
      return var1.yCoord >= this.minY && var1.yCoord <= this.maxY && var1.zCoord >= this.minZ && var1.zCoord <= this.maxZ;
   }

   @VisibleForTesting
   public boolean intersectsWithXZ(Vec3d var1) {
      return var1.xCoord >= this.minX && var1.xCoord <= this.maxX && var1.zCoord >= this.minZ && var1.zCoord <= this.maxZ;
   }

   @VisibleForTesting
   public boolean intersectsWithXY(Vec3d var1) {
      return var1.xCoord >= this.minX && var1.xCoord <= this.maxX && var1.yCoord >= this.minY && var1.yCoord <= this.maxY;
   }

   public String toString() {
      return "box[" + this.minX + ", " + this.minY + ", " + this.minZ + " -> " + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
   }
}
