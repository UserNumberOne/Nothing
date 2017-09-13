package net.minecraft.block;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

// $FF: synthetic class
class BlockVine$1 {
   // $FF: synthetic field
   static final int[] field_177057_a;
   // $FF: synthetic field
   static final int[] field_185876_b;
   // $FF: synthetic field
   static final int[] field_185877_c = new int[Mirror.values().length];

   static {
      try {
         field_185877_c[Mirror.LEFT_RIGHT.ordinal()] = 1;
      } catch (NoSuchFieldError var10) {
         ;
      }

      try {
         field_185877_c[Mirror.FRONT_BACK.ordinal()] = 2;
      } catch (NoSuchFieldError var9) {
         ;
      }

      field_185876_b = new int[Rotation.values().length];

      try {
         field_185876_b[Rotation.CLOCKWISE_180.ordinal()] = 1;
      } catch (NoSuchFieldError var8) {
         ;
      }

      try {
         field_185876_b[Rotation.COUNTERCLOCKWISE_90.ordinal()] = 2;
      } catch (NoSuchFieldError var7) {
         ;
      }

      try {
         field_185876_b[Rotation.CLOCKWISE_90.ordinal()] = 3;
      } catch (NoSuchFieldError var6) {
         ;
      }

      field_177057_a = new int[EnumFacing.values().length];

      try {
         field_177057_a[EnumFacing.UP.ordinal()] = 1;
      } catch (NoSuchFieldError var5) {
         ;
      }

      try {
         field_177057_a[EnumFacing.NORTH.ordinal()] = 2;
      } catch (NoSuchFieldError var4) {
         ;
      }

      try {
         field_177057_a[EnumFacing.SOUTH.ordinal()] = 3;
      } catch (NoSuchFieldError var3) {
         ;
      }

      try {
         field_177057_a[EnumFacing.EAST.ordinal()] = 4;
      } catch (NoSuchFieldError var2) {
         ;
      }

      try {
         field_177057_a[EnumFacing.WEST.ordinal()] = 5;
      } catch (NoSuchFieldError var1) {
         ;
      }

   }
}
