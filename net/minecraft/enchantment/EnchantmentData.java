package net.minecraft.enchantment;

import net.minecraft.util.WeightedRandom;

public class EnchantmentData extends WeightedRandom.Item {
   public final Enchantment enchantmentobj;
   public final int enchantmentLevel;

   public EnchantmentData(Enchantment var1, int var2) {
      super(var1.getRarity().getWeight());
      this.enchantmentobj = var1;
      this.enchantmentLevel = var2;
   }
}
