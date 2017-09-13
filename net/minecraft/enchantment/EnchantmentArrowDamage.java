package net.minecraft.enchantment;

import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentArrowDamage extends Enchantment {
   public EnchantmentArrowDamage(Enchantment.Rarity var1, EntityEquipmentSlot... var2) {
      super(rarityIn, EnumEnchantmentType.BOW, slots);
      this.setName("arrowDamage");
   }

   public int getMinEnchantability(int var1) {
      return 1 + (enchantmentLevel - 1) * 10;
   }

   public int getMaxEnchantability(int var1) {
      return this.getMinEnchantability(enchantmentLevel) + 15;
   }

   public int getMaxLevel() {
      return 5;
   }
}
