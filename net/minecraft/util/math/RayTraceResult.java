package net.minecraft.util.math;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;

public class RayTraceResult {
   public int subHit;
   public Object hitInfo;
   private BlockPos blockPos;
   public RayTraceResult.Type typeOfHit;
   public EnumFacing sideHit;
   public Vec3d hitVec;
   public Entity entityHit;

   public RayTraceResult(Vec3d var1, EnumFacing var2, BlockPos var3) {
      this(RayTraceResult.Type.BLOCK, hitVecIn, sideHitIn, blockPosIn);
   }

   public RayTraceResult(Vec3d var1, EnumFacing var2) {
      this(RayTraceResult.Type.BLOCK, hitVecIn, sideHitIn, BlockPos.ORIGIN);
   }

   public RayTraceResult(Entity var1) {
      this(entityIn, new Vec3d(entityIn.posX, entityIn.posY, entityIn.posZ));
   }

   public RayTraceResult(RayTraceResult.Type var1, Vec3d var2, EnumFacing var3, BlockPos var4) {
      this.subHit = -1;
      this.hitInfo = null;
      this.typeOfHit = typeIn;
      this.blockPos = blockPosIn;
      this.sideHit = sideHitIn;
      this.hitVec = new Vec3d(hitVecIn.xCoord, hitVecIn.yCoord, hitVecIn.zCoord);
   }

   public RayTraceResult(Entity var1, Vec3d var2) {
      this.subHit = -1;
      this.hitInfo = null;
      this.typeOfHit = RayTraceResult.Type.ENTITY;
      this.entityHit = entityHitIn;
      this.hitVec = hitVecIn;
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
