package net.minecraft.enchantment;

import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentArrowInfinite extends Enchantment {
   public EnchantmentArrowInfinite(Enchantment.Rarity var1, EntityEquipmentSlot... var2) {
      super(rarityIn, EnumEnchantmentType.BOW, slots);
      this.setName("arrowInfinite");
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
