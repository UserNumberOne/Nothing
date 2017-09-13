package net.minecraft.server.management;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.Date;
import java.util.UUID;

public class UserListBansEntry extends UserListEntryBan {
   public UserListBansEntry(GameProfile var1) {
      this(profile, (Date)null, (String)null, (Date)null, (String)null);
   }

   public UserListBansEntry(GameProfile var1, Date var2, String var3, Date var4, String var5) {
      super(profile, endDate, banner, endDate, banReason);
   }

   public UserListBansEntry(JsonObject var1) {
      super(toGameProfile(json), json);
   }

   protected void onSerialization(JsonObject var1) {
      if (this.getValue() != null) {
         data.addProperty("uuid", ((GameProfile)this.getValue()).getId() == null ? "" : ((GameProfile)this.getValue()).getId().toString());
         data.addProperty("name", ((GameProfile)this.getValue()).getName());
         super.onSerialization(data);
      }

   }

   private static GameProfile toGameProfile(JsonObject var0) {
      if (json.has("uuid") && json.has("name")) {
         String s = json.get("uuid").getAsString();

         UUID uuid;
         try {
            uuid = UUID.fromString(s);
         } catch (Throwable var4) {
            return null;
         }

         return new GameProfile(uuid, json.get("name").getAsString());
      } else {
         return null;
      }
   }
}
