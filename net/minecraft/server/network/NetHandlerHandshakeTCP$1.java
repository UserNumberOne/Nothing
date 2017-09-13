package net.minecraft.server.network;

import net.minecraft.network.EnumConnectionState;

// $FF: synthetic class
class NetHandlerHandshakeTCP$1 {
   // $FF: synthetic field
   static final int[] field_151291_a = new int[EnumConnectionState.values().length];

   static {
      try {
         field_151291_a[EnumConnectionState.LOGIN.ordinal()] = 1;
      } catch (NoSuchFieldError var2) {
         ;
      }

      try {
         field_151291_a[EnumConnectionState.STATUS.ordinal()] = 2;
      } catch (NoSuchFieldError var1) {
         ;
      }

   }
}
