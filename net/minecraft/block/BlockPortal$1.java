package net.minecraft.block;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;

// $FF: synthetic class
class BlockPortal$1 {
   // $FF: synthetic field
   static final int[] field_185810_a;
   // $FF: synthetic field
   static final int[] field_185811_b = new int[Rotation.values().length];

   static {
      try {
         field_185811_b[Rotation.COUNTERCLOCKWISE_90.ordinal()] = 1;
      } catch (NoSuchFieldError var5) {
         ;
      }

      try {
         field_185811_b[Rotation.CLOCKWISE_90.ordinal()] = 2;
      } catch (NoSuchFieldError var4) {
         ;
      }

      field_185810_a = new int[EnumFacing.Axis.values().length];

      try {
         field_185810_a[EnumFacing.Axis.X.ordinal()] = 1;
      } catch (NoSuchFieldError var3) {
         ;
      }

      try {
         field_185810_a[EnumFacing.Axis.Y.ordinal()] = 2;
      } catch (NoSuchFieldError var2) {
         ;
      }

      try {
         field_185810_a[EnumFacing.Axis.Z.ordinal()] = 3;
      } catch (NoSuchFieldError var1) {
         ;
      }

   }
}
