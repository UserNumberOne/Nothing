package net.minecraft.enchantment;

import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentArrowFire extends Enchantment {
   public EnchantmentArrowFire(Enchantment.Rarity var1, EntityEquipmentSlot... var2) {
      super(rarityIn, EnumEnchantmentType.BOW, slots);
      this.setName("arrowFire");
   }

   public int getMinEnchantability(int var1) {
      return 20;
   }

   public int getMaxEnchantability(int var1) {
      return 50;
   }

   public int getMaxLevel() {
      return 1;
   }
}
