package net.minecraft.entity.boss.dragon.phase;

import javax.annotation.Nullable;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.feature.WorldGenEndPodium;

public class PhaseLanding extends PhaseBase {
   private Vec3d targetLocation;

   public PhaseLanding(EntityDragon var1) {
      super(var1);
   }

   public void doClientRenderEffects() {
      Vec3d var1 = this.dragon.getHeadLookVec(1.0F).normalize();
      var1.rotateYaw(-0.7853982F);
      double var2 = this.dragon.dragonPartHead.posX;
      double var4 = this.dragon.dragonPartHead.posY + (double)(this.dragon.dragonPartHead.height / 2.0F);
      double var6 = this.dragon.dragonPartHead.posZ;

      for(int var8 = 0; var8 < 8; ++var8) {
         double var9 = var2 + this.dragon.getRNG().nextGaussian() / 2.0D;
         double var11 = var4 + this.dragon.getRNG().nextGaussian() / 2.0D;
         double var13 = var6 + this.dragon.getRNG().nextGaussian() / 2.0D;
         this.dragon.world.spawnParticle(EnumParticleTypes.DRAGON_BREATH, var9, var11, var13, -var1.xCoord * 0.07999999821186066D + this.dragon.motionX, -var1.yCoord * 0.30000001192092896D + this.dragon.motionY, -var1.zCoord * 0.07999999821186066D + this.dragon.motionZ);
         var1.rotateYaw(0.19634955F);
      }

   }

   public void doLocalUpdate() {
      if (this.targetLocation == null) {
         this.targetLocation = new Vec3d(this.dragon.world.getTopSolidOrLiquidBlock(WorldGenEndPodium.END_PODIUM_LOCATION));
      }

      if (this.targetLocation.squareDistanceTo(this.dragon.posX, this.dragon.posY, this.dragon.posZ) < 1.0D) {
         ((PhaseSittingFlaming)this.dragon.getPhaseManager().getPhase(PhaseList.SITTING_FLAMING)).resetFlameCount();
         this.dragon.getPhaseManager().setPhase(PhaseList.SITTING_SCANNING);
      }

   }

   public float getMaxRiseOrFall() {
      return 1.5F;
   }

   public float getYawFactor() {
      float var1 = MathHelper.sqrt(this.dragon.motionX * this.dragon.motionX + this.dragon.motionZ * this.dragon.motionZ) + 1.0F;
      float var2 = Math.min(var1, 40.0F);
      return var2 / var1;
   }

   public void initPhase() {
      this.targetLocation = null;
   }

   @Nullable
   public Vec3d getTargetLocation() {
      return this.targetLocation;
   }

   public PhaseList getPhaseList() {
      return PhaseList.LANDING;
   }
}
