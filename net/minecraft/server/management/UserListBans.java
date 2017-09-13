package net.minecraft.server.management;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;

public class UserListBans extends UserList {
   public UserListBans(File var1) {
      super(bansFile);
   }

   protected UserListEntry createEntry(JsonObject var1) {
      return new UserListBansEntry(entryData);
   }

   public boolean isBanned(GameProfile var1) {
      return this.hasEntry(profile);
   }

   public String[] getKeys() {
      String[] astring = new String[this.getValues().size()];
      int i = 0;

      for(UserListBansEntry userlistbansentry : this.getValues().values()) {
         astring[i++] = ((GameProfile)userlistbansentry.getValue()).getName();
      }

      return astring;
   }

   protected String getObjectKey(GameProfile var1) {
      return obj.getId().toString();
   }

   public GameProfile getBannedProfile(String var1) {
      for(UserListBansEntry userlistbansentry : this.getValues().values()) {
         if (username.equalsIgnoreCase(((GameProfile)userlistbansentry.getValue()).getName())) {
            return (GameProfile)userlistbansentry.getValue();
         }
      }

      return null;
   }
}
