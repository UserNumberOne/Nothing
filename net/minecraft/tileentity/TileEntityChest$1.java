package net.minecraft.tileentity;

import net.minecraft.util.EnumFacing;

// $FF: synthetic class
class TileEntityChest$1 {
   // $FF: synthetic field
   static final int[] field_177366_a = new int[EnumFacing.values().length];

   static {
      try {
         field_177366_a[EnumFacing.NORTH.ordinal()] = 1;
      } catch (NoSuchFieldError var4) {
         ;
      }

      try {
         field_177366_a[EnumFacing.SOUTH.ordinal()] = 2;
      } catch (NoSuchFieldError var3) {
         ;
      }

      try {
         field_177366_a[EnumFacing.EAST.ordinal()] = 3;
      } catch (NoSuchFieldError var2) {
         ;
      }

      try {
         field_177366_a[EnumFacing.WEST.ordinal()] = 4;
      } catch (NoSuchFieldError var1) {
         ;
      }

   }
}
