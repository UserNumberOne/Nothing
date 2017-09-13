package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PhaseSittingFlaming extends PhaseSittingBase {
   private int flameTicks;
   private int flameCount;
   private EntityAreaEffectCloud areaEffectCloud;

   public PhaseSittingFlaming(EntityDragon var1) {
      super(var1);
   }

   public void doClientRenderEffects() {
      ++this.flameTicks;
      if (this.flameTicks % 2 == 0 && this.flameTicks < 10) {
         Vec3d var1 = this.dragon.getHeadLookVec(1.0F).normalize();
         var1.rotateYaw(-0.7853982F);
         double var2 = this.dragon.dragonPartHead.posX;
         double var4 = this.dragon.dragonPartHead.posY + (double)(this.dragon.dragonPartHead.height / 2.0F);
         double var6 = this.dragon.dragonPartHead.posZ;

         for(int var8 = 0; var8 < 8; ++var8) {
            double var9 = var2 + this.dragon.getRNG().nextGaussian() / 2.0D;
            double var11 = var4 + this.dragon.getRNG().nextGaussian() / 2.0D;
            double var13 = var6 + this.dragon.getRNG().nextGaussian() / 2.0D;

            for(int var15 = 0; var15 < 6; ++var15) {
               this.dragon.world.spawnParticle(EnumParticleTypes.DRAGON_BREATH, var9, var11, var13, -var1.xCoord * 0.07999999821186066D * (double)var15, -var1.yCoord * 0.6000000238418579D, -var1.zCoord * 0.07999999821186066D * (double)var15);
            }

            var1.rotateYaw(0.19634955F);
         }
      }

   }

   public void doLocalUpdate() {
      ++this.flameTicks;
      if (this.flameTicks >= 200) {
         if (this.flameCount >= 4) {
            this.dragon.getPhaseManager().setPhase(PhaseList.TAKEOFF);
         } else {
            this.dragon.getPhaseManager().setPhase(PhaseList.SITTING_SCANNING);
         }
      } else if (this.flameTicks == 10) {
         Vec3d var1 = (new Vec3d(this.dragon.dragonPartHead.posX - this.dragon.posX, 0.0D, this.dragon.dragonPartHead.posZ - this.dragon.posZ)).normalize();
         float var2 = 5.0F;
         double var3 = this.dragon.dragonPartHead.posX + var1.xCoord * 5.0D / 2.0D;
         double var5 = this.dragon.dragonPartHead.posZ + var1.zCoord * 5.0D / 2.0D;
         double var7 = this.dragon.dragonPartHead.posY + (double)(this.dragon.dragonPartHead.height / 2.0F);
         BlockPos.MutableBlockPos var9 = new BlockPos.MutableBlockPos(MathHelper.floor(var3), MathHelper.floor(var7), MathHelper.floor(var5));

         while(this.dragon.world.isAirBlock(var9)) {
            --var7;
            var9.setPos(MathHelper.floor(var3), MathHelper.floor(var7), MathHelper.floor(var5));
         }

         var7 = (double)(MathHelper.floor(var7) + 1);
         this.areaEffectCloud = new EntityAreaEffectCloud(this.dragon.world, var3, var7, var5);
         this.areaEffectCloud.setOwner(this.dragon);
         this.areaEffectCloud.setRadius(5.0F);
         this.areaEffectCloud.setDuration(200);
         this.areaEffectCloud.setParticle(EnumParticleTypes.DRAGON_BREATH);
         this.areaEffectCloud.addEffect(new PotionEffect(MobEffects.INSTANT_DAMAGE));
         this.dragon.world.spawnEntity(this.areaEffectCloud);
      }

   }

   public void initPhase() {
      this.flameTicks = 0;
      ++this.flameCount;
   }

   public void removeAreaEffect() {
      if (this.areaEffectCloud != null) {
         this.areaEffectCloud.setDead();
         this.areaEffectCloud = null;
      }

   }

   public PhaseList getPhaseList() {
      return PhaseList.SITTING_FLAMING;
   }

   public void resetFlameCount() {
      this.flameCount = 0;
   }
}
