package net.minecraft.enchantment;

import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentArrowKnockback extends Enchantment {
   public EnchantmentArrowKnockback(Enchantment.Rarity var1, EntityEquipmentSlot... var2) {
      super(rarityIn, EnumEnchantmentType.BOW, slots);
      this.setName("arrowKnockback");
   }

   public int getMinEnchantability(int var1) {
      return 12 + (enchantmentLevel - 1) * 20;
   }

   public int getMaxEnchantability(int var1) {
      return this.getMinEnchantability(enchantmentLevel) + 25;
   }

   public int getMaxLevel() {
      return 2;
   }
}
