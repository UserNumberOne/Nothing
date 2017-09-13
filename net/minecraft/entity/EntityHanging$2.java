package net.minecraft.entity;

import net.minecraft.util.Rotation;

// $FF: synthetic class
class EntityHanging$2 {
   // $FF: synthetic field
   static final int[] field_188767_a = new int[Rotation.values().length];

   static {
      try {
         field_188767_a[Rotation.CLOCKWISE_180.ordinal()] = 1;
      } catch (NoSuchFieldError var3) {
         ;
      }

      try {
         field_188767_a[Rotation.COUNTERCLOCKWISE_90.ordinal()] = 2;
      } catch (NoSuchFieldError var2) {
         ;
      }

      try {
         field_188767_a[Rotation.CLOCKWISE_90.ordinal()] = 3;
      } catch (NoSuchFieldError var1) {
         ;
      }

   }
}
