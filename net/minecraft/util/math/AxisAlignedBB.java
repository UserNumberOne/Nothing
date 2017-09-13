package net.minecraft.util.math;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AxisAlignedBB {
   public final double minX;
   public final double minY;
   public final double minZ;
   public final double maxX;
   public final double maxY;
   public final double maxZ;

   public AxisAlignedBB(double var1, double var3, double var5, double var7, double var9, double var11) {
      this.minX = Math.min(x1, x2);
      this.minY = Math.min(y1, y2);
      this.minZ = Math.min(z1, z2);
      this.maxX = Math.max(x1, x2);
      this.maxY = Math.max(y1, y2);
      this.maxZ = Math.max(z1, z2);
   }

   public AxisAlignedBB(BlockPos var1) {
      this((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)(pos.getX() + 1), (double)(pos.getY() + 1), (double)(pos.getZ() + 1));
   }

   public AxisAlignedBB(BlockPos var1, BlockPos var2) {
      this((double)pos1.getX(), (double)pos1.getY(), (double)pos1.getZ(), (double)pos2.getX(), (double)pos2.getY(), (double)pos2.getZ());
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB(Vec3d var1, Vec3d var2) {
      this(min.xCoord, min.yCoord, min.zCoord, max.xCoord, max.yCoord, max.zCoord);
   }

   public AxisAlignedBB setMaxY(double var1) {
      return new AxisAlignedBB(this.minX, this.minY, this.minZ, this.maxX, y2, this.maxZ);
   }

   public boolean equals(Object var1) {
      if (this == p_equals_1_) {
         return true;
      } else if (!(p_equals_1_ instanceof AxisAlignedBB)) {
         return false;
      } else {
         AxisAlignedBB axisalignedbb = (AxisAlignedBB)p_equals_1_;
         return Double.compare(axisalignedbb.minX, this.minX) != 0 ? false : (Double.compare(axisalignedbb.minY, this.minY) != 0 ? false : (Double.compare(axisalignedbb.minZ, this.minZ) != 0 ? false : (Double.compare(axisalignedbb.maxX, this.maxX) != 0 ? false : (Double.compare(axisalignedbb.maxY, this.maxY) != 0 ? false : Double.compare(axisalignedbb.maxZ, this.maxZ) == 0))));
      }
   }

   public int hashCode() {
      long i = Double.doubleToLongBits(this.minX);
      int j = (int)(i ^ i >>> 32);
      i = Double.doubleToLongBits(this.minY);
      j = 31 * j + (int)(i ^ i >>> 32);
      i = Double.doubleToLongBits(this.minZ);
      j = 31 * j + (int)(i ^ i >>> 32);
      i = Double.doubleToLongBits(this.maxX);
      j = 31 * j + (int)(i ^ i >>> 32);
      i = Double.doubleToLongBits(this.maxY);
      j = 31 * j + (int)(i ^ i >>> 32);
      i = Double.doubleToLongBits(this.maxZ);
      j = 31 * j + (int)(i ^ i >>> 32);
      return j;
   }

   public AxisAlignedBB addCoord(double var1, double var3, double var5) {
      double d0 = this.minX;
      double d1 = this.minY;
      double d2 = this.minZ;
      double d3 = this.maxX;
      double d4 = this.maxY;
      double d5 = this.maxZ;
      if (x < 0.0D) {
         d0 += x;
      } else if (x > 0.0D) {
         d3 += x;
      }

      if (y < 0.0D) {
         d1 += y;
      } else if (y > 0.0D) {
         d4 += y;
      }

      if (z < 0.0D) {
         d2 += z;
      } else if (z > 0.0D) {
         d5 += z;
      }

      return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
   }

   public AxisAlignedBB expand(double var1, double var3, double var5) {
      double d0 = this.minX - x;
      double d1 = this.minY - y;
      double d2 = this.minZ - z;
      double d3 = this.maxX + x;
      double d4 = this.maxY + y;
      double d5 = this.maxZ + z;
      return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
   }

   public AxisAlignedBB expandXyz(double var1) {
      return this.expand(value, value, value);
   }

   public AxisAlignedBB union(AxisAlignedBB var1) {
      double d0 = Math.min(this.minX, other.minX);
      double d1 = Math.min(this.minY, other.minY);
      double d2 = Math.min(this.minZ, other.minZ);
      double d3 = Math.max(this.maxX, other.maxX);
      double d4 = Math.max(this.maxY, other.maxY);
      double d5 = Math.max(this.maxZ, other.maxZ);
      return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
   }

   public AxisAlignedBB offset(double var1, double var3, double var5) {
      return new AxisAlignedBB(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
   }

   public AxisAlignedBB offset(BlockPos var1) {
      return new AxisAlignedBB(this.minX + (double)pos.getX(), this.minY + (double)pos.getY(), this.minZ + (double)pos.getZ(), this.maxX + (double)pos.getX(), this.maxY + (double)pos.getY(), this.maxZ + (double)pos.getZ());
   }

   public double calculateXOffset(AxisAlignedBB var1, double var2) {
      if (other.maxY > this.minY && other.minY < this.maxY && other.maxZ > this.minZ && other.minZ < this.maxZ) {
         if (offsetX > 0.0D && other.maxX <= this.minX) {
            double d1 = this.minX - other.maxX;
            if (d1 < offsetX) {
               offsetX = d1;
            }
         } else if (offsetX < 0.0D && other.minX >= this.maxX) {
            double d0 = this.maxX - other.minX;
            if (d0 > offsetX) {
               offsetX = d0;
            }
         }

         return offsetX;
      } else {
         return offsetX;
      }
   }

   public double calculateYOffset(AxisAlignedBB var1, double var2) {
      if (other.maxX > this.minX && other.minX < this.maxX && other.maxZ > this.minZ && other.minZ < this.maxZ) {
         if (offsetY > 0.0D && other.maxY <= this.minY) {
            double d1 = this.minY - other.maxY;
            if (d1 < offsetY) {
               offsetY = d1;
            }
         } else if (offsetY < 0.0D && other.minY >= this.maxY) {
            double d0 = this.maxY - other.minY;
            if (d0 > offsetY) {
               offsetY = d0;
            }
         }

         return offsetY;
      } else {
         return offsetY;
      }
   }

   public double calculateZOffset(AxisAlignedBB var1, double var2) {
      if (other.maxX > this.minX && other.minX < this.maxX && other.maxY > this.minY && other.minY < this.maxY) {
         if (offsetZ > 0.0D && other.maxZ <= this.minZ) {
            double d1 = this.minZ - other.maxZ;
            if (d1 < offsetZ) {
               offsetZ = d1;
            }
         } else if (offsetZ < 0.0D && other.minZ >= this.maxZ) {
            double d0 = this.maxZ - other.minZ;
            if (d0 > offsetZ) {
               offsetZ = d0;
            }
         }

         return offsetZ;
      } else {
         return offsetZ;
      }
   }

   public boolean intersectsWith(AxisAlignedBB var1) {
      return this.intersects(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
   }

   public boolean intersects(double var1, double var3, double var5, double var7, double var9, double var11) {
      return this.minX < x2 && this.maxX > x1 && this.minY < y2 && this.maxY > y1 && this.minZ < z2 && this.maxZ > z1;
   }

   @SideOnly(Side.CLIENT)
   public boolean intersects(Vec3d var1, Vec3d var2) {
      return this.intersects(Math.min(min.xCoord, max.xCoord), Math.min(min.yCoord, max.yCoord), Math.min(min.zCoord, max.zCoord), Math.max(min.xCoord, max.xCoord), Math.max(min.yCoord, max.yCoord), Math.max(min.zCoord, max.zCoord));
   }

   public boolean isVecInside(Vec3d var1) {
      return vec.xCoord > this.minX && vec.xCoord < this.maxX ? (vec.yCoord > this.minY && vec.yCoord < this.maxY ? vec.zCoord > this.minZ && vec.zCoord < this.maxZ : false) : false;
   }

   public double getAverageEdgeLength() {
      double d0 = this.maxX - this.minX;
      double d1 = this.maxY - this.minY;
      double d2 = this.maxZ - this.minZ;
      return (d0 + d1 + d2) / 3.0D;
   }

   public AxisAlignedBB contract(double var1) {
      return this.expandXyz(-value);
   }

   @Nullable
   public RayTraceResult calculateIntercept(Vec3d var1, Vec3d var2) {
      Vec3d vec3d = this.collideWithXPlane(this.minX, vecA, vecB);
      EnumFacing enumfacing = EnumFacing.WEST;
      Vec3d vec3d1 = this.collideWithXPlane(this.maxX, vecA, vecB);
      if (vec3d1 != null && this.isClosest(vecA, vec3d, vec3d1)) {
         vec3d = vec3d1;
         enumfacing = EnumFacing.EAST;
      }

      vec3d1 = this.collideWithYPlane(this.minY, vecA, vecB);
      if (vec3d1 != null && this.isClosest(vecA, vec3d, vec3d1)) {
         vec3d = vec3d1;
         enumfacing = EnumFacing.DOWN;
      }

      vec3d1 = this.collideWithYPlane(this.maxY, vecA, vecB);
      if (vec3d1 != null && this.isClosest(vecA, vec3d, vec3d1)) {
         vec3d = vec3d1;
         enumfacing = EnumFacing.UP;
      }

      vec3d1 = this.collideWithZPlane(this.minZ, vecA, vecB);
      if (vec3d1 != null && this.isClosest(vecA, vec3d, vec3d1)) {
         vec3d = vec3d1;
         enumfacing = EnumFacing.NORTH;
      }

      vec3d1 = this.collideWithZPlane(this.maxZ, vecA, vecB);
      if (vec3d1 != null && this.isClosest(vecA, vec3d, vec3d1)) {
         vec3d = vec3d1;
         enumfacing = EnumFacing.SOUTH;
      }

      return vec3d == null ? null : new RayTraceResult(vec3d, enumfacing);
   }

   @VisibleForTesting
   boolean isClosest(Vec3d var1, @Nullable Vec3d var2, Vec3d var3) {
      return p_186661_2_ == null || p_186661_1_.squareDistanceTo(p_186661_3_) < p_186661_1_.squareDistanceTo(p_186661_2_);
   }

   @Nullable
   @VisibleForTesting
   Vec3d collideWithXPlane(double var1, Vec3d var3, Vec3d var4) {
      Vec3d vec3d = p_186671_3_.getIntermediateWithXValue(p_186671_4_, p_186671_1_);
      return vec3d != null && this.intersectsWithYZ(vec3d) ? vec3d : null;
   }

   @Nullable
   @VisibleForTesting
   Vec3d collideWithYPlane(double var1, Vec3d var3, Vec3d var4) {
      Vec3d vec3d = p_186663_3_.getIntermediateWithYValue(p_186663_4_, p_186663_1_);
      return vec3d != null && this.intersectsWithXZ(vec3d) ? vec3d : null;
   }

   @Nullable
   @VisibleForTesting
   Vec3d collideWithZPlane(double var1, Vec3d var3, Vec3d var4) {
      Vec3d vec3d = p_186665_3_.getIntermediateWithZValue(p_186665_4_, p_186665_1_);
      return vec3d != null && this.intersectsWithXY(vec3d) ? vec3d : null;
   }

   @VisibleForTesting
   public boolean intersectsWithYZ(Vec3d var1) {
      return vec.yCoord >= this.minY && vec.yCoord <= this.maxY && vec.zCoord >= this.minZ && vec.zCoord <= this.maxZ;
   }

   @VisibleForTesting
   public boolean intersectsWithXZ(Vec3d var1) {
      return vec.xCoord >= this.minX && vec.xCoord <= this.maxX && vec.zCoord >= this.minZ && vec.zCoord <= this.maxZ;
   }

   @VisibleForTesting
   public boolean intersectsWithXY(Vec3d var1) {
      return vec.xCoord >= this.minX && vec.xCoord <= this.maxX && vec.yCoord >= this.minY && vec.yCoord <= this.maxY;
   }

   public String toString() {
      return "box[" + this.minX + ", " + this.minY + ", " + this.minZ + " -> " + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
   }

   @SideOnly(Side.CLIENT)
   public boolean hasNaN() {
      return Double.isNaN(this.minX) || Double.isNaN(this.minY) || Double.isNaN(this.minZ) || Double.isNaN(this.maxX) || Double.isNaN(this.maxY) || Double.isNaN(this.maxZ);
   }

   @SideOnly(Side.CLIENT)
   public Vec3d getCenter() {
      return new Vec3d(this.minX + (this.maxX - this.minX) * 0.5D, this.minY + (this.maxY - this.minY) * 0.5D, this.minZ + (this.maxZ - this.minZ) * 0.5D);
   }
}
