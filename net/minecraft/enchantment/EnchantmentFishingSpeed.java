package net.minecraft.enchantment;

import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentFishingSpeed extends Enchantment {
   protected EnchantmentFishingSpeed(Enchantment.Rarity var1, EnumEnchantmentType var2, EntityEquipmentSlot... var3) {
      super(rarityIn, typeIn, slots);
      this.setName("fishingSpeed");
   }

   public int getMinEnchantability(int var1) {
      return 15 + (enchantmentLevel - 1) * 9;
   }

   public int getMaxEnchantability(int var1) {
      return super.getMinEnchantability(enchantmentLevel) + 50;
   }

   public int getMaxLevel() {
      return 3;
   }
}
