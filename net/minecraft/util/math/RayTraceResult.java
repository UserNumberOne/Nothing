package net.minecraft.util.math;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;

public class RayTraceResult {
   private BlockPos blockPos;
   public RayTraceResult.Type typeOfHit;
   public EnumFacing sideHit;
   public Vec3d hitVec;
   public Entity entityHit;

   public RayTraceResult(Vec3d var1, EnumFacing var2, BlockPos var3) {
      this(RayTraceResult.Type.BLOCK, var1, var2, var3);
   }

   public RayTraceResult(Vec3d var1, EnumFacing var2) {
      this(RayTraceResult.Type.BLOCK, var1, var2, BlockPos.ORIGIN);
   }

   public RayTraceResult(Entity var1) {
      this(var1, new Vec3d(var1.posX, var1.posY, var1.posZ));
   }

   public RayTraceResult(RayTraceResult.Type var1, Vec3d var2, EnumFacing var3, BlockPos var4) {
      this.typeOfHit = var1;
      this.blockPos = var4;
      this.sideHit = var3;
      this.hitVec = new Vec3d(var2.xCoord, var2.yCoord, var2.zCoord);
   }

   public RayTraceResult(Entity var1, Vec3d var2) {
      this.typeOfHit = RayTraceResult.Type.ENTITY;
      this.entityHit = var1;
      this.hitVec = var2;
   }

   public BlockPos getBlockPos() {
      return this.blockPos;
   }

   public String toString() {
      return "HitResult{type=" + this.typeOfHit + ", blockpos=" + this.blockPos + ", f=" + this.sideHit + ", pos=" + this.hitVec + ", entity=" + this.entityHit + '}';
   }

   public static enum Type {
      MISS,
      BLOCK,
      ENTITY;
   }
}
