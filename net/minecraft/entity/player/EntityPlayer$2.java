package net.minecraft.entity.player;

import net.minecraft.util.EnumFacing;

// $FF: synthetic class
class EntityPlayer$2 {
   // $FF: synthetic field
   static final int[] field_190166_a = new int[EnumFacing.values().length];

   static {
      try {
         field_190166_a[EnumFacing.SOUTH.ordinal()] = 1;
      } catch (NoSuchFieldError var4) {
         ;
      }

      try {
         field_190166_a[EnumFacing.NORTH.ordinal()] = 2;
      } catch (NoSuchFieldError var3) {
         ;
      }

      try {
         field_190166_a[EnumFacing.WEST.ordinal()] = 3;
      } catch (NoSuchFieldError var2) {
         ;
      }

      try {
         field_190166_a[EnumFacing.EAST.ordinal()] = 4;
      } catch (NoSuchFieldError var1) {
         ;
      }

   }
}
