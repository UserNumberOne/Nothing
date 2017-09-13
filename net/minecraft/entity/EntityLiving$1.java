package net.minecraft.entity;

import net.minecraft.inventory.EntityEquipmentSlot;

// $FF: synthetic class
class EntityLiving$1 {
   // $FF: synthetic field
   static final int[] field_188474_a;
   // $FF: synthetic field
   static final int[] field_188475_b = new int[EntityEquipmentSlot.values().length];

   static {
      try {
         field_188475_b[EntityEquipmentSlot.HEAD.ordinal()] = 1;
      } catch (NoSuchFieldError var6) {
         ;
      }

      try {
         field_188475_b[EntityEquipmentSlot.CHEST.ordinal()] = 2;
      } catch (NoSuchFieldError var5) {
         ;
      }

      try {
         field_188475_b[EntityEquipmentSlot.LEGS.ordinal()] = 3;
      } catch (NoSuchFieldError var4) {
         ;
      }

      try {
         field_188475_b[EntityEquipmentSlot.FEET.ordinal()] = 4;
      } catch (NoSuchFieldError var3) {
         ;
      }

      field_188474_a = new int[EntityEquipmentSlot.Type.values().length];

      try {
         field_188474_a[EntityEquipmentSlot.Type.HAND.ordinal()] = 1;
      } catch (NoSuchFieldError var2) {
         ;
      }

      try {
         field_188474_a[EntityEquipmentSlot.Type.ARMOR.ordinal()] = 2;
      } catch (NoSuchFieldError var1) {
         ;
      }

   }
}
