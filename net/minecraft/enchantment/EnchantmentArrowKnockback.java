package net.minecraft.enchantment;

import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentArrowKnockback extends Enchantment {
   public EnchantmentArrowKnockback(Enchantment.Rarity var1, EntityEquipmentSlot... var2) {
      super(var1, EnumEnchantmentType.BOW, var2);
      this.setName("arrowKnockback");
   }

   public int getMinEnchantability(int var1) {
      return 12 + (var1 - 1) * 20;
   }

   public int getMaxEnchantability(int var1) {
      return this.getMinEnchantability(var1) + 25;
   }

   public int getMaxLevel() {
      return 2;
   }
}
