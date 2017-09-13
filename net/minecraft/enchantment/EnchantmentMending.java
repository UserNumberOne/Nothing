package net.minecraft.enchantment;

import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentMending extends Enchantment {
   public EnchantmentMending(Enchantment.Rarity var1, EntityEquipmentSlot... var2) {
      super(var1, EnumEnchantmentType.BREAKABLE, var2);
      this.setName("mending");
   }

   public int getMinEnchantability(int var1) {
      return var1 * 25;
   }

   public int getMaxEnchantability(int var1) {
      return this.getMinEnchantability(var1) + 50;
   }

   public boolean isTreasureEnchantment() {
      return true;
   }

   public int getMaxLevel() {
      return 1;
   }
}
