package net.minecraft.util;

import net.minecraft.util.math.MathHelper;

public class CombatRules {
   public static float getDamageAfterAbsorb(float var0, float var1, float var2) {
      float var3 = 2.0F + var2 / 4.0F;
      float var4 = MathHelper.clamp(var1 - var0 / var3, var1 * 0.2F, 20.0F);
      return var0 * (1.0F - var4 / 25.0F);
   }

   public static float getDamageAfterMagicAbsorb(float var0, float var1) {
      float var2 = MathHelper.clamp(var1, 0.0F, 20.0F);
      return var0 * (1.0F - var2 / 25.0F);
   }
}
