package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PhaseSittingScanning extends PhaseSittingBase {
   private int scanningTime;

   public PhaseSittingScanning(EntityDragon var1) {
      super(var1);
   }

   public void doLocalUpdate() {
      ++this.scanningTime;
      EntityPlayer var1 = this.dragon.world.getNearestAttackablePlayer(this.dragon, 20.0D, 10.0D);
      if (var1 != null) {
         if (this.scanningTime > 25) {
            this.dragon.getPhaseManager().setPhase(PhaseList.SITTING_ATTACKING);
         } else {
            Vec3d var2 = (new Vec3d(var1.posX - this.dragon.posX, 0.0D, var1.posZ - this.dragon.posZ)).normalize();
            Vec3d var3 = (new Vec3d((double)MathHelper.sin(this.dragon.rotationYaw * 0.017453292F), 0.0D, (double)(-MathHelper.cos(this.dragon.rotationYaw * 0.017453292F)))).normalize();
            float var4 = (float)var3.dotProduct(var2);
            float var5 = (float)(Math.acos((double)var4) * 57.2957763671875D) + 0.5F;
            if (var5 < 0.0F || var5 > 10.0F) {
               double var6 = var1.posX - this.dragon.dragonPartHead.posX;
               double var8 = var1.posZ - this.dragon.dragonPartHead.posZ;
               double var10 = MathHelper.clamp(MathHelper.wrapDegrees(180.0D - MathHelper.atan2(var6, var8) * 57.2957763671875D - (double)this.dragon.rotationYaw), -100.0D, 100.0D);
               this.dragon.randomYawVelocity *= 0.8F;
               float var12 = MathHelper.sqrt(var6 * var6 + var8 * var8) + 1.0F;
               float var13 = var12;
               if (var12 > 40.0F) {
                  var12 = 40.0F;
               }

               this.dragon.randomYawVelocity = (float)((double)this.dragon.randomYawVelocity + var10 * (double)(0.7F / var12 / var13));
               this.dragon.rotationYaw += this.dragon.randomYawVelocity;
            }
         }
      } else if (this.scanningTime >= 100) {
         var1 = this.dragon.world.getNearestAttackablePlayer(this.dragon, 150.0D, 150.0D);
         this.dragon.getPhaseManager().setPhase(PhaseList.TAKEOFF);
         if (var1 != null) {
            this.dragon.getPhaseManager().setPhase(PhaseList.CHARGING_PLAYER);
            ((PhaseChargingPlayer)this.dragon.getPhaseManager().getPhase(PhaseList.CHARGING_PLAYER)).setTarget(new Vec3d(var1.posX, var1.posY, var1.posZ));
         }
      }

   }

   public void initPhase() {
      this.scanningTime = 0;
   }

   public PhaseList getPhaseList() {
      return PhaseList.SITTING_SCANNING;
   }
}
