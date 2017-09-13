package net.minecraft.server.management;

import com.google.gson.JsonObject;
import java.io.File;
import java.net.SocketAddress;

public class UserListIPBans extends UserList {
   public UserListIPBans(File var1) {
      super(bansFile);
   }

   protected UserListEntry createEntry(JsonObject var1) {
      return new UserListIPBansEntry(entryData);
   }

   public boolean isBanned(SocketAddress var1) {
      String s = this.addressToString(address);
      return this.hasEntry(s);
   }

   public UserListIPBansEntry getBanEntry(SocketAddress var1) {
      String s = this.addressToString(address);
      return (UserListIPBansEntry)this.getEntry(s);
   }

   private String addressToString(SocketAddress var1) {
      String s = address.toString();
      if (s.contains("/")) {
         s = s.substring(s.indexOf(47) + 1);
      }

      if (s.contains(":")) {
         s = s.substring(0, s.indexOf(58));
      }

      return s;
   }
}
