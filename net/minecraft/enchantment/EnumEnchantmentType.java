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
            return var2.armorType == EntityEquipmentSlot.HEAD ? this == ARMOR_HEAD : (var2.armorType == EntityEquipmentSlot.LEGS ? this == ARMOR_LEGS : (var2.armorType == EntityEquipmentSlot.CHEST ? this == ARMOR_CHEST : (var2.armorType == EntityEquipmentSlot.FEET ? this == ARMOR_FEET : false)));
         }
      } else {
         return var1 instanceof ItemSword ? this == WEAPON : (var1 instanceof ItemTool ? this == DIGGER : (var1 instanceof ItemBow ? this == BOW : (var1 instanceof ItemFishingRod ? this == FISHING_ROD : false)));
      }
   }
}
