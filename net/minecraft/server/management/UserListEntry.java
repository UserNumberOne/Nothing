package net.minecraft.server.management;

import com.google.gson.JsonObject;

public class UserListEntry {
   private final Object value;

   public UserListEntry(Object var1) {
      this.value = valueIn;
   }

   protected UserListEntry(Object var1, JsonObject var2) {
      this.value = valueIn;
   }

   Object getValue() {
      return this.value;
   }

   boolean hasBanExpired() {
      return false;
   }

   protected void onSerialization(JsonObject var1) {
   }
}
