package net.minecraft.entity.projectile;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class ProjectileHelper {
   public static RayTraceResult forwardsRaycast(Entity var0, boolean var1, boolean var2, Entity var3) {
      double var4 = var0.posX;
      double var6 = var0.posY;
      double var8 = var0.posZ;
      double var10 = var0.motionX;
      double var12 = var0.motionY;
      double var14 = var0.motionZ;
      World var16 = var0.world;
      Vec3d var17 = new Vec3d(var4, var6, var8);
      Vec3d var18 = new Vec3d(var4 + var10, var6 + var12, var8 + var14);
      RayTraceResult var19 = var16.rayTraceBlocks(var17, var18, false, true, false);
      if (var1) {
         if (var19 != null) {
            var18 = new Vec3d(var19.hitVec.xCoord, var19.hitVec.yCoord, var19.hitVec.zCoord);
         }

         Entity var20 = null;
         List var21 = var16.getEntitiesWithinAABBExcludingEntity(var0, var0.getEntityBoundingBox().addCoord(var10, var12, var14).expandXyz(1.0D));
         double var22 = 0.0D;

         for(int var24 = 0; var24 < var21.size(); ++var24) {
            Entity var25 = (Entity)var21.get(var24);
            if (var25.canBeCollidedWith() && (var2 || !var25.isEntityEqual(var3)) && !var25.noClip) {
               AxisAlignedBB var26 = var25.getEntityBoundingBox().expandXyz(0.30000001192092896D);
               RayTraceResult var27 = var26.calculateIntercept(var17, var18);
               if (var27 != null) {
                  double var28 = var17.squareDistanceTo(var27.hitVec);
                  if (var28 < var22 || var22 == 0.0D) {
                     var20 = var25;
                     var22 = var28;
                  }
               }
            }
         }

         if (var20 != null) {
            var19 = new RayTraceResult(var20);
         }
      }

      return var19;
   }

   public static final void rotateTowardsMovement(Entity var0, float var1) {
      double var2 = var0.motionX;
      double var4 = var0.motionY;
      double var6 = var0.motionZ;
      float var8 = MathHelper.sqrt(var2 * var2 + var6 * var6);
      var0.rotationYaw = (float)(MathHelper.atan2(var6, var2) * 57.29577951308232D) + 90.0F;

      for(var0.rotationPitch = (float)(MathHelper.atan2((double)var8, var4) * 57.29577951308232D) - 90.0F; var0.rotationPitch - var0.prevRotationPitch < -180.0F; var0.prevRotationPitch -= 360.0F) {
         ;
      }

      while(var0.rotationPitch - var0.prevRotationPitch >= 180.0F) {
         var0.prevRotationPitch += 360.0F;
      }

      while(var0.rotationYaw - var0.prevRotationYaw < -180.0F) {
         var0.prevRotationYaw -= 360.0F;
      }

      while(var0.rotationYaw - var0.prevRotationYaw >= 180.0F) {
         var0.prevRotationYaw += 360.0F;
      }

      var0.rotationPitch = var0.prevRotationPitch + (var0.rotationPitch - var0.prevRotationPitch) * var1;
      var0.rotationYaw = var0.prevRotationYaw + (var0.rotationYaw - var0.prevRotationYaw) * var1;
   }
}
