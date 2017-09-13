package net.minecraft.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;

public class PotionHealthBoost extends Potion {
   public PotionHealthBoost(boolean var1, int var2) {
      super(isBadEffectIn, liquidColorIn);
   }

   public void removeAttributesModifiersFromEntity(EntityLivingBase var1, AbstractAttributeMap var2, int var3) {
      super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
      if (entityLivingBaseIn.getHealth() > entityLivingBaseIn.getMaxHealth()) {
         entityLivingBaseIn.setHealth(entityLivingBaseIn.getMaxHealth());
      }

   }
}
