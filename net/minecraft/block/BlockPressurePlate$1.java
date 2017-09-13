package net.minecraft.block;

// $FF: synthetic class
class BlockPressurePlate$1 {
   // $FF: synthetic field
   static final int[] field_180127_a = new int[BlockPressurePlate.Sensitivity.values().length];

   static {
      try {
         field_180127_a[BlockPressurePlate.Sensitivity.EVERYTHING.ordinal()] = 1;
      } catch (NoSuchFieldError var2) {
         ;
      }

      try {
         field_180127_a[BlockPressurePlate.Sensitivity.MOBS.ordinal()] = 2;
      } catch (NoSuchFieldError var1) {
         ;
      }

   }
}
