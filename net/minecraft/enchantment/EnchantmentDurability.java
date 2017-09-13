package net.minecraft.enchantment;

import java.util.Random;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class EnchantmentDurability extends Enchantment {
   protected EnchantmentDurability(Enchantment.Rarity var1, EntityEquipmentSlot... var2) {
      super(rarityIn, EnumEnchantmentType.BREAKABLE, slots);
      this.setName("durability");
   }

   public int getMinEnchantability(int var1) {
      return 5 + (enchantmentLevel - 1) * 8;
   }

   public int getMaxEnchantability(int var1) {
      return super.getMinEnchantability(enchantmentLevel) + 50;
   }

   public int getMaxLevel() {
      return 3;
   }

   public boolean canApply(ItemStack var1) {
      return stack.isItemStackDamageable() ? true : super.canApply(stack);
   }

   public static boolean negateDamage(ItemStack var0, int var1, Random var2) {
      return p_92097_0_.getItem() instanceof ItemArmor && p_92097_2_.nextFloat() < 0.6F ? false : p_92097_2_.nextInt(p_92097_1_ + 1) > 0;
   }
}
