package net.minecraft.entity.boss.dragon.phase;

import javax.annotation.Nullable;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public abstract class PhaseBase implements IPhase {
   protected final EntityDragon dragon;

   public PhaseBase(EntityDragon var1) {
      this.dragon = var1;
   }

   public boolean getIsStationary() {
      return false;
   }

   public void doClientRenderEffects() {
   }

   public void doLocalUpdate() {
   }

   public void onCrystalDestroyed(EntityEnderCrystal var1, BlockPos var2, DamageSource var3, @Nullable EntityPlayer var4) {
   }

   public void initPhase() {
   }

   public void removeAreaEffect() {
   }

   public float getMaxRiseOrFall() {
      return 0.6F;
   }

   @Nullable
   public Vec3d getTargetLocation() {
      return null;
   }

   public float getAdjustedDamage(EntityDragonPart var1, DamageSource var2, float var3) {
      return var3;
   }

   public float getYawFactor() {
      float var1 = MathHelper.sqrt(this.dragon.motionX * this.dragon.motionX + this.dragon.motionZ * this.dragon.motionZ) + 1.0F;
      float var2 = Math.min(var1, 40.0F);
      return 0.7F / var2 / var1;
   }
}
