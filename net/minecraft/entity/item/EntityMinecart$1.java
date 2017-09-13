package net.minecraft.entity.item;

import net.minecraft.block.BlockRailBase;

// $FF: synthetic class
class EntityMinecart$1 {
   // $FF: synthetic field
   static final int[] field_184969_a;
   // $FF: synthetic field
   static final int[] field_184970_b = new int[BlockRailBase.EnumRailDirection.values().length];

   static {
      try {
         field_184970_b[BlockRailBase.EnumRailDirection.ASCENDING_EAST.ordinal()] = 1;
      } catch (NoSuchFieldError var10) {
         ;
      }

      try {
         field_184970_b[BlockRailBase.EnumRailDirection.ASCENDING_WEST.ordinal()] = 2;
      } catch (NoSuchFieldError var9) {
         ;
      }

      try {
         field_184970_b[BlockRailBase.EnumRailDirection.ASCENDING_NORTH.ordinal()] = 3;
      } catch (NoSuchFieldError var8) {
         ;
      }

      try {
         field_184970_b[BlockRailBase.EnumRailDirection.ASCENDING_SOUTH.ordinal()] = 4;
      } catch (NoSuchFieldError var7) {
         ;
      }

      field_184969_a = new int[EntityMinecart.Type.values().length];

      try {
         field_184969_a[EntityMinecart.Type.CHEST.ordinal()] = 1;
      } catch (NoSuchFieldError var6) {
         ;
      }

      try {
         field_184969_a[EntityMinecart.Type.FURNACE.ordinal()] = 2;
      } catch (NoSuchFieldError var5) {
         ;
      }

      try {
         field_184969_a[EntityMinecart.Type.TNT.ordinal()] = 3;
      } catch (NoSuchFieldError var4) {
         ;
      }

      try {
         field_184969_a[EntityMinecart.Type.SPAWNER.ordinal()] = 4;
      } catch (NoSuchFieldError var3) {
         ;
      }

      try {
         field_184969_a[EntityMinecart.Type.HOPPER.ordinal()] = 5;
      } catch (NoSuchFieldError var2) {
         ;
      }

      try {
         field_184969_a[EntityMinecart.Type.COMMAND_BLOCK.ordinal()] = 6;
      } catch (NoSuchFieldError var1) {
         ;
      }

   }
}
