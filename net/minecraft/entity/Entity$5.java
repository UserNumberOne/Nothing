package net.minecraft.entity;

import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

// $FF: synthetic class
class Entity$5 {
   // $FF: synthetic field
   static final int[] field_188427_a;
   // $FF: synthetic field
   static final int[] field_188428_b = new int[Mirror.values().length];

   static {
      try {
         field_188428_b[Mirror.LEFT_RIGHT.ordinal()] = 1;
      } catch (NoSuchFieldError var5) {
         ;
      }

      try {
         field_188428_b[Mirror.FRONT_BACK.ordinal()] = 2;
      } catch (NoSuchFieldError var4) {
         ;
      }

      field_188427_a = new int[Rotation.values().length];

      try {
         field_188427_a[Rotation.CLOCKWISE_180.ordinal()] = 1;
      } catch (NoSuchFieldError var3) {
         ;
      }

      try {
         field_188427_a[Rotation.COUNTERCLOCKWISE_90.ordinal()] = 2;
      } catch (NoSuchFieldError var2) {
         ;
      }

      try {
         field_188427_a[Rotation.CLOCKWISE_90.ordinal()] = 3;
      } catch (NoSuchFieldError var1) {
         ;
      }

   }
}
