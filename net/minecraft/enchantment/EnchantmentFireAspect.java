package net.minecraft.enchantment;

import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentFireAspect extends Enchantment {
   protected EnchantmentFireAspect(Enchantment.Rarity var1, EntityEquipmentSlot... var2) {
      super(rarityIn, EnumEnchantmentType.WEAPON, slots);
      this.setName("fire");
   }

   public int getMinEnchantability(int var1) {
      return 10 + 20 * (enchantmentLevel - 1);
   }

   public int getMaxEnchantability(int var1) {
      return super.getMinEnchantability(enchantmentLevel) + 50;
   }

   public int getMaxLevel() {
      return 2;
   }
}
