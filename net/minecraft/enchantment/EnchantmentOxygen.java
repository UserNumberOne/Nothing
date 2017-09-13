package net.minecraft.enchantment;

import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentOxygen extends Enchantment {
   public EnchantmentOxygen(Enchantment.Rarity var1, EntityEquipmentSlot... var2) {
      super(var1, EnumEnchantmentType.ARMOR_HEAD, var2);
      this.setName("oxygen");
   }

   public int getMinEnchantability(int var1) {
      return 10 * var1;
   }

   public int getMaxEnchantability(int var1) {
      return this.getMinEnchantability(var1) + 30;
   }

   public int getMaxLevel() {
      return 3;
   }
}
