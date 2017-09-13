package net.minecraft.util;

import javax.annotation.Nullable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.text.ITextComponent;

public class CombatEntry {
   private final DamageSource damageSrc;
   private final int time;
   private final float damage;
   private final float health;
   private final String fallSuffix;
   private final float fallDistance;

   public CombatEntry(DamageSource var1, int var2, float var3, float var4, String var5, float var6) {
      this.damageSrc = var1;
      this.time = var2;
      this.damage = var4;
      this.health = var3;
      this.fallSuffix = var5;
      this.fallDistance = var6;
   }

   public DamageSource getDamageSrc() {
      return this.damageSrc;
   }

   public float getDamage() {
      return this.damage;
   }

   public boolean isLivingDamageSrc() {
      return this.damageSrc.getEntity() instanceof EntityLivingBase;
   }

   @Nullable
   public String getFallSuffix() {
      return this.fallSuffix;
   }

   @Nullable
   public ITextComponent getDamageSrcDisplayName() {
      return this.getDamageSrc().getEntity() == null ? null : this.getDamageSrc().getEntity().getDisplayName();
   }

   public float getDamageAmount() {
      return this.damageSrc == DamageSource.outOfWorld ? Float.MAX_VALUE : this.fallDistance;
   }
}
