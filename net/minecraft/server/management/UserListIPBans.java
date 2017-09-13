package net.minecraft.server.management;

import com.google.gson.JsonObject;
import java.io.File;
import java.net.SocketAddress;

public class UserListIPBans extends UserList {
   public UserListIPBans(File var1) {
      super(var1);
   }

   protected UserListEntry createEntry(JsonObject var1) {
      return new UserListIPBansEntry(var1);
   }

   public boolean isBanned(SocketAddress var1) {
      String var2 = this.addressToString(var1);
      return this.hasEntry(var2);
   }

   public UserListIPBansEntry getBanEntry(SocketAddress var1) {
      String var2 = this.addressToString(var1);
      return (UserListIPBansEntry)this.getEntry(var2);
   }

   private String addressToString(SocketAddress var1) {
      String var2 = var1.toString();
      if (var2.contains("/")) {
         var2 = var2.substring(var2.indexOf(47) + 1);
      }

      if (var2.contains(":")) {
         var2 = var2.substring(0, var2.indexOf(58));
      }

      return var2;
   }
}
