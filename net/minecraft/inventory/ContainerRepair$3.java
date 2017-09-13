package net.minecraft.inventory;

import net.minecraft.enchantment.Enchantment;

// $FF: synthetic class
class ContainerRepair$3 {
   // $FF: synthetic field
   static final int[] field_185007_a = new int[Enchantment.Rarity.values().length];

   static {
      try {
         field_185007_a[Enchantment.Rarity.COMMON.ordinal()] = 1;
      } catch (NoSuchFieldError var4) {
         ;
      }

      try {
         field_185007_a[Enchantment.Rarity.UNCOMMON.ordinal()] = 2;
      } catch (NoSuchFieldError var3) {
         ;
      }

      try {
         field_185007_a[Enchantment.Rarity.RARE.ordinal()] = 3;
      } catch (NoSuchFieldError var2) {
         ;
      }

      try {
         field_185007_a[Enchantment.Rarity.VERY_RARE.ordinal()] = 4;
      } catch (NoSuchFieldError var1) {
         ;
      }

   }
}
