package net.minecraft.util;

import net.minecraft.util.math.MathHelper;

public class CombatRules {
   public static float getDamageAfterAbsorb(float var0, float var1, float var2) {
      float f = 2.0F + toughnessAttribute / 4.0F;
      float f1 = MathHelper.clamp(totalArmor - damage / f, totalArmor * 0.2F, 20.0F);
      return damage * (1.0F - f1 / 25.0F);
   }

   public static float getDamageAfterMagicAbsorb(float var0, float var1) {
      float f = MathHelper.clamp(p_188401_1_, 0.0F, 20.0F);
      return p_188401_0_ * (1.0F - f / 25.0F);
   }
}
