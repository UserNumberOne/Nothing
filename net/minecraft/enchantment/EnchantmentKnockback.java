package net.minecraft.enchantment;

import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentKnockback extends Enchantment {
   protected EnchantmentKnockback(Enchantment.Rarity var1, EntityEquipmentSlot... var2) {
      super(var1, EnumEnchantmentType.WEAPON, var2);
      this.setName("knockback");
   }

   public int getMinEnchantability(int var1) {
      return 5 + 20 * (var1 - 1);
   }

   public int getMaxEnchantability(int var1) {
      return super.getMinEnchantability(var1) + 50;
   }

   public int getMaxLevel() {
      return 2;
   }
}
