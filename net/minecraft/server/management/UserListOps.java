package net.minecraft.server.management;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;

public class UserListOps extends UserList {
   public UserListOps(File var1) {
      super(saveFile);
   }

   protected UserListEntry createEntry(JsonObject var1) {
      return new UserListOpsEntry(entryData);
   }

   public String[] getKeys() {
      String[] astring = new String[this.getValues().size()];
      int i = 0;

      for(UserListOpsEntry userlistopsentry : this.getValues().values()) {
         astring[i++] = ((GameProfile)userlistopsentry.getValue()).getName();
      }

      return astring;
   }

   public int getPermissionLevel(GameProfile var1) {
      UserListOpsEntry userlistopsentry = (UserListOpsEntry)this.getEntry(profile);
      return userlistopsentry != null ? userlistopsentry.getPermissionLevel() : 0;
   }

   public boolean bypassesPlayerLimit(GameProfile var1) {
      UserListOpsEntry userlistopsentry = (UserListOpsEntry)this.getEntry(profile);
      return userlistopsentry != null ? userlistopsentry.bypassesPlayerLimit() : false;
   }

   protected String getObjectKey(GameProfile var1) {
      return obj.getId().toString();
   }

   public GameProfile getGameProfileFromName(String var1) {
      for(UserListOpsEntry userlistopsentry : this.getValues().values()) {
         if (username.equalsIgnoreCase(((GameProfile)userlistopsentry.getValue()).getName())) {
            return (GameProfile)userlistopsentry.getValue();
         }
      }

      return null;
   }
}
