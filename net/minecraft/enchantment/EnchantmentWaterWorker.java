package net.minecraft.enchantment;

import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentWaterWorker extends Enchantment {
   public EnchantmentWaterWorker(Enchantment.Rarity var1, EntityEquipmentSlot... var2) {
      super(rarityIn, EnumEnchantmentType.ARMOR_HEAD, slots);
      this.setName("waterWorker");
   }

   public int getMinEnchantability(int var1) {
      return 1;
   }

   public int getMaxEnchantability(int var1) {
      return this.getMinEnchantability(enchantmentLevel) + 40;
   }

   public int getMaxLevel() {
      return 1;
   }
}
