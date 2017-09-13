package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.DamageSource;

public abstract class PhaseSittingBase extends PhaseBase {
   public PhaseSittingBase(EntityDragon var1) {
      super(var1);
   }

   public boolean getIsStationary() {
      return true;
   }

   public float getAdjustedDamage(EntityDragonPart var1, DamageSource var2, float var3) {
      if (var2.getSourceOfDamage() instanceof EntityArrow) {
         var2.getSourceOfDamage().setFire(1);
         return 0.0F;
      } else {
         return super.getAdjustedDamage(var1, var2, var3);
      }
   }
}
