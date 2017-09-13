package net.minecraft.enchantment;

import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentMending extends Enchantment {
   public EnchantmentMending(Enchantment.Rarity var1, EntityEquipmentSlot... var2) {
      super(rarityIn, EnumEnchantmentType.BREAKABLE, slots);
      this.setName("mending");
   }

   public int getMinEnchantability(int var1) {
      return enchantmentLevel * 25;
   }

   public int getMaxEnchantability(int var1) {
      return this.getMinEnchantability(enchantmentLevel) + 50;
   }

   public boolean isTreasureEnchantment() {
      return true;
   }

   public int getMaxLevel() {
      return 1;
   }
}
