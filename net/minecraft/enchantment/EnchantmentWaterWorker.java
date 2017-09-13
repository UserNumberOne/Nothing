package net.minecraft.enchantment;

import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentWaterWorker extends Enchantment {
   public EnchantmentWaterWorker(Enchantment.Rarity var1, EntityEquipmentSlot... var2) {
      super(var1, EnumEnchantmentType.ARMOR_HEAD, var2);
      this.setName("waterWorker");
   }

   public int getMinEnchantability(int var1) {
      return 1;
   }

   public int getMaxEnchantability(int var1) {
      return this.getMinEnchantability(var1) + 40;
   }

   public int getMaxLevel() {
      return 1;
   }
}
