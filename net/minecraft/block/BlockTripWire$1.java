package net.minecraft.block;

import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

// $FF: synthetic class
class BlockTripWire$1 {
   // $FF: synthetic field
   static final int[] field_185874_a;
   // $FF: synthetic field
   static final int[] field_185875_b = new int[Mirror.values().length];

   static {
      try {
         field_185875_b[Mirror.LEFT_RIGHT.ordinal()] = 1;
      } catch (NoSuchFieldError var5) {
         ;
      }

      try {
         field_185875_b[Mirror.FRONT_BACK.ordinal()] = 2;
      } catch (NoSuchFieldError var4) {
         ;
      }

      field_185874_a = new int[Rotation.values().length];

      try {
         field_185874_a[Rotation.CLOCKWISE_180.ordinal()] = 1;
      } catch (NoSuchFieldError var3) {
         ;
      }

      try {
         field_185874_a[Rotation.COUNTERCLOCKWISE_90.ordinal()] = 2;
      } catch (NoSuchFieldError var2) {
         ;
      }

      try {
         field_185874_a[Rotation.CLOCKWISE_90.ordinal()] = 3;
      } catch (NoSuchFieldError var1) {
         ;
      }

   }
}
