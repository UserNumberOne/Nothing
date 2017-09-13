package net.minecraft.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;

public class PotionAbsorption extends Potion {
   protected PotionAbsorption(boolean var1, int var2) {
      super(isBadEffectIn, liquidColorIn);
   }

   public void removeAttributesModifiersFromEntity(EntityLivingBase var1, AbstractAttributeMap var2, int var3) {
      entityLivingBaseIn.setAbsorptionAmount(entityLivingBaseIn.getAbsorptionAmount() - (float)(4 * (amplifier + 1)));
      super.removeAttributesModifiersFromEntity(entityLivingBaseIn, attributeMapIn, amplifier);
   }

   public void applyAttributesModifiersToEntity(EntityLivingBase var1, AbstractAttributeMap var2, int var3) {
      entityLivingBaseIn.setAbsorptionAmount(entityLivingBaseIn.getAbsorptionAmount() + (float)(4 * (amplifier + 1)));
      super.applyAttributesModifiersToEntity(entityLivingBaseIn, attributeMapIn, amplifier);
   }
}
