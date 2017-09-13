package net.minecraft.enchantment;

import net.minecraft.util.WeightedRandom;

public class EnchantmentData extends WeightedRandom.Item {
   public final Enchantment enchantmentobj;
   public final int enchantmentLevel;

   public EnchantmentData(Enchantment var1, int var2) {
      super(enchantmentObj.getRarity().getWeight());
      this.enchantmentobj = enchantmentObj;
      this.enchantmentLevel = enchLevel;
   }
}
