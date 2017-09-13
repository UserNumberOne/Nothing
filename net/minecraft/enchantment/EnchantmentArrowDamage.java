package net.minecraft.enchantment;

import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentArrowDamage extends Enchantment {
   public EnchantmentArrowDamage(Enchantment.Rarity var1, EntityEquipmentSlot... var2) {
      super(var1, EnumEnchantmentType.BOW, var2);
      this.setName("arrowDamage");
   }

   public int getMinEnchantability(int var1) {
      return 1 + (var1 - 1) * 10;
   }

   public int getMaxEnchantability(int var1) {
      return this.getMinEnchantability(var1) + 15;
   }

   public int getMaxLevel() {
      return 5;
   }
}
