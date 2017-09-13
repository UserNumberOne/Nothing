package net.minecraft.enchantment;

import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentWaterWalker extends Enchantment {
   public EnchantmentWaterWalker(Enchantment.Rarity var1, EntityEquipmentSlot... var2) {
      super(var1, EnumEnchantmentType.ARMOR_FEET, var2);
      this.setName("waterWalker");
   }

   public int getMinEnchantability(int var1) {
      return var1 * 10;
   }

   public int getMaxEnchantability(int var1) {
      return this.getMinEnchantability(var1) + 15;
   }

   public int getMaxLevel() {
      return 3;
   }

   public boolean canApplyTogether(Enchantment var1) {
      return super.canApplyTogether(var1) && var1 != Enchantments.FROST_WALKER;
   }
}
