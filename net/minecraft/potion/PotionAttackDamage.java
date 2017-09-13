package net.minecraft.potion;

import net.minecraft.entity.ai.attributes.AttributeModifier;

public class PotionAttackDamage extends Potion {
   protected final double bonusPerLevel;

   protected PotionAttackDamage(boolean var1, int var2, double var3) {
      super(var1, var2);
      this.bonusPerLevel = var3;
   }

   public double getAttributeModifierAmount(int var1, AttributeModifier var2) {
      return this.bonusPerLevel * (double)(var1 + 1);
   }
}
