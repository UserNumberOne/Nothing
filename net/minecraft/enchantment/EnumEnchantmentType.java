package net.minecraft.enchantment;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;

public enum EnumEnchantmentType {
   ALL,
   ARMOR,
   ARMOR_FEET,
   ARMOR_LEGS,
   ARMOR_CHEST,
   ARMOR_HEAD,
   WEAPON,
   DIGGER,
   FISHING_ROD,
   BREAKABLE,
   BOW;

   public boolean canEnchantItem(Item var1) {
      if (this == ALL) {
         return true;
      } else if (this == BREAKABLE && var1.isDamageable()) {
         return true;
      } else if (var1 instanceof ItemArmor) {
         if (this == ARMOR) {
            return true;
         } else {
            ItemArmor var2 = (ItemArmor)var1;
            if (var2.armorType == EntityEquipmentSlot.HEAD) {
               return this == ARMOR_HEAD;
            } else if (var2.armorType == EntityEquipmentSlot.LEGS) {
               return this == ARMOR_LEGS;
            } else if (var2.armorType == EntityEquipmentSlot.CHEST) {
               return this == ARMOR_CHEST;
            } else if (var2.armorType == EntityEquipmentSlot.FEET) {
               return this == ARMOR_FEET;
            } else {
               return false;
            }
         }
      } else if (var1 instanceof ItemSword) {
         return this == WEAPON;
      } else if (var1 instanceof ItemTool) {
         return this == DIGGER;
      } else if (var1 instanceof ItemBow) {
         return this == BOW;
      } else if (var1 instanceof ItemFishingRod) {
         return this == FISHING_ROD;
      } else {
         return false;
      }
   }
}
