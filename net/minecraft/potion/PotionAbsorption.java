package net.minecraft.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;

public class PotionAbsorption extends Potion {
   protected PotionAbsorption(boolean var1, int var2) {
      super(var1, var2);
   }

   public void removeAttributesModifiersFromEntity(EntityLivingBase var1, AbstractAttributeMap var2, int var3) {
      var1.setAbsorptionAmount(var1.getAbsorptionAmount() - (float)(4 * (var3 + 1)));
      super.removeAttributesModifiersFromEntity(var1, var2, var3);
   }

   public void applyAttributesModifiersToEntity(EntityLivingBase var1, AbstractAttributeMap var2, int var3) {
      var1.setAbsorptionAmount(var1.getAbsorptionAmount() + (float)(4 * (var3 + 1)));
      super.applyAttributesModifiersToEntity(var1, var2, var3);
   }
}
