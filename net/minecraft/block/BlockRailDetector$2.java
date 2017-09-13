package net.minecraft.block;

import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

// $FF: synthetic class
class BlockRailDetector$2 {
   // $FF: synthetic field
   static final int[] field_185786_a;
   // $FF: synthetic field
   static final int[] field_185787_b;
   // $FF: synthetic field
   static final int[] field_185788_c = new int[Mirror.values().length];

   static {
      try {
         field_185788_c[Mirror.LEFT_RIGHT.ordinal()] = 1;
      } catch (NoSuchFieldError var15) {
         ;
      }

      try {
         field_185788_c[Mirror.FRONT_BACK.ordinal()] = 2;
      } catch (NoSuchFieldError var14) {
         ;
      }

      field_185787_b = new int[Rotation.values().length];

      try {
         field_185787_b[Rotation.CLOCKWISE_180.ordinal()] = 1;
      } catch (NoSuchFieldError var13) {
         ;
      }

      try {
         field_185787_b[Rotation.COUNTERCLOCKWISE_90.ordinal()] = 2;
      } catch (NoSuchFieldError var12) {
         ;
      }

      try {
         field_185787_b[Rotation.CLOCKWISE_90.ordinal()] = 3;
      } catch (NoSuchFieldError var11) {
         ;
      }

      field_185786_a = new int[BlockRailBase.EnumRailDirection.values().length];

      try {
         field_185786_a[BlockRailBase.EnumRailDirection.ASCENDING_EAST.ordinal()] = 1;
      } catch (NoSuchFieldError var10) {
         ;
      }

      try {
         field_185786_a[BlockRailBase.EnumRailDirection.ASCENDING_WEST.ordinal()] = 2;
      } catch (NoSuchFieldError var9) {
         ;
      }

      try {
         field_185786_a[BlockRailBase.EnumRailDirection.ASCENDING_NORTH.ordinal()] = 3;
      } catch (NoSuchFieldError var8) {
         ;
      }

      try {
         field_185786_a[BlockRailBase.EnumRailDirection.ASCENDING_SOUTH.ordinal()] = 4;
      } catch (NoSuchFieldError var7) {
         ;
      }

      try {
         field_185786_a[BlockRailBase.EnumRailDirection.SOUTH_EAST.ordinal()] = 5;
      } catch (NoSuchFieldError var6) {
         ;
      }

      try {
         field_185786_a[BlockRailBase.EnumRailDirection.SOUTH_WEST.ordinal()] = 6;
      } catch (NoSuchFieldError var5) {
         ;
      }

      try {
         field_185786_a[BlockRailBase.EnumRailDirection.NORTH_WEST.ordinal()] = 7;
      } catch (NoSuchFieldError var4) {
         ;
      }

      try {
         field_185786_a[BlockRailBase.EnumRailDirection.NORTH_EAST.ordinal()] = 8;
      } catch (NoSuchFieldError var3) {
         ;
      }

      try {
         field_185786_a[BlockRailBase.EnumRailDirection.NORTH_SOUTH.ordinal()] = 9;
      } catch (NoSuchFieldError var2) {
         ;
      }

      try {
         field_185786_a[BlockRailBase.EnumRailDirection.EAST_WEST.ordinal()] = 10;
      } catch (NoSuchFieldError var1) {
         ;
      }

   }
}
