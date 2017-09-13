package net.minecraft.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;

public class PotionHealthBoost extends Potion {
   public PotionHealthBoost(boolean var1, int var2) {
      super(var1, var2);
   }

   public void removeAttributesModifiersFromEntity(EntityLivingBase var1, AbstractAttributeMap var2, int var3) {
      super.removeAttributesModifiersFromEntity(var1, var2, var3);
      if (var1.getHealth() > var1.getMaxHealth()) {
         var1.setHealth(var1.getMaxHealth());
      }

   }
}
