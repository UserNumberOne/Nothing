package net.minecraft.enchantment;

import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentOxygen extends Enchantment {
   public EnchantmentOxygen(Enchantment.Rarity var1, EntityEquipmentSlot... var2) {
      super(rarityIn, EnumEnchantmentType.ARMOR_HEAD, slots);
      this.setName("oxygen");
   }

   public int getMinEnchantability(int var1) {
      return 10 * enchantmentLevel;
   }

   public int getMaxEnchantability(int var1) {
      return this.getMinEnchantability(enchantmentLevel) + 30;
   }

   public int getMaxLevel() {
      return 3;
   }
}
