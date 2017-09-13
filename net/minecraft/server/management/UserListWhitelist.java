package net.minecraft.server.management;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;

public class UserListWhitelist extends UserList {
   public UserListWhitelist(File var1) {
      super(var1);
   }

   protected UserListEntry createEntry(JsonObject var1) {
      return new UserListWhitelistEntry(var1);
   }

   public boolean isWhitelisted(GameProfile var1) {
      return this.hasEntry(var1);
   }

   public String[] getKeys() {
      String[] var1 = new String[this.getValues().size()];
      int var2 = 0;

      for(UserListWhitelistEntry var4 : this.getValues().values()) {
         var1[var2++] = ((GameProfile)var4.getValue()).getName();
      }

      return var1;
   }

   protected String getObjectKey(GameProfile var1) {
      return var1.getId().toString();
   }

   public GameProfile getByName(String var1) {
      for(UserListWhitelistEntry var3 : this.getValues().values()) {
         if (var1.equalsIgnoreCase(((GameProfile)var3.getValue()).getName())) {
            return (GameProfile)var3.getValue();
         }
      }

      return null;
   }

   // $FF: synthetic method
   protected String getObjectKey(Object var1) {
      return this.getObjectKey((GameProfile)var1);
   }
}
