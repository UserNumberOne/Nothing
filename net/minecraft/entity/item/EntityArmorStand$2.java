package net.minecraft.entity.item;

import net.minecraft.inventory.EntityEquipmentSlot;

// $FF: synthetic class
class EntityArmorStand$2 {
   // $FF: synthetic field
   static final int[] field_188765_a = new int[EntityEquipmentSlot.Type.values().length];

   static {
      try {
         field_188765_a[EntityEquipmentSlot.Type.HAND.ordinal()] = 1;
      } catch (NoSuchFieldError var2) {
         ;
      }

      try {
         field_188765_a[EntityEquipmentSlot.Type.ARMOR.ordinal()] = 2;
      } catch (NoSuchFieldError var1) {
         ;
      }

   }
}
