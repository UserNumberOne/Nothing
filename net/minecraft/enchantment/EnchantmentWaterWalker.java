package net.minecraft.enchantment;

import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentWaterWalker extends Enchantment {
   public EnchantmentWaterWalker(Enchantment.Rarity var1, EntityEquipmentSlot... var2) {
      super(rarityIn, EnumEnchantmentType.ARMOR_FEET, slots);
      this.setName("waterWalker");
   }

   public int getMinEnchantability(int var1) {
      return enchantmentLevel * 10;
   }

   public int getMaxEnchantability(int var1) {
      return this.getMinEnchantability(enchantmentLevel) + 15;
   }

   public int getMaxLevel() {
      return 3;
   }

   public boolean canApplyTogether(Enchantment var1) {
      return super.canApplyTogether(ench) && ench != Enchantments.FROST_WALKER;
   }
}
