package net.minecraft.enchantment;

import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentUntouching extends Enchantment {
   protected EnchantmentUntouching(Enchantment.Rarity var1, EntityEquipmentSlot... var2) {
      super(rarityIn, EnumEnchantmentType.DIGGER, slots);
      this.setName("untouching");
   }

   public int getMinEnchantability(int var1) {
      return 15;
   }

   public int getMaxEnchantability(int var1) {
      return super.getMinEnchantability(enchantmentLevel) + 50;
   }

   public int getMaxLevel() {
      return 1;
   }

   public boolean canApplyTogether(Enchantment var1) {
      return super.canApplyTogether(ench) && ench != Enchantments.FORTUNE;
   }
}
