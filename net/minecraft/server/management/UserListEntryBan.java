package net.minecraft.server.management;

import com.google.gson.JsonObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class UserListEntryBan extends UserListEntry {
   public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
   protected final Date banStartDate;
   protected final String bannedBy;
   protected final Date banEndDate;
   protected final String reason;

   public UserListEntryBan(Object var1, Date var2, String var3, Date var4, String var5) {
      super(var1);
      this.banStartDate = var2 == null ? new Date() : var2;
      this.bannedBy = var3 == null ? "(Unknown)" : var3;
      this.banEndDate = var4;
      this.reason = var5 == null ? "Banned by an operator." : var5;
   }

   protected UserListEntryBan(Object var1, JsonObject var2) {
      super(checkExpiry(var1, var2), var2);

      Date var3;
      try {
         var3 = var2.has("created") ? DATE_FORMAT.parse(var2.get("created").getAsString()) : new Date();
      } catch (ParseException var6) {
         var3 = new Date();
      }

      this.banStartDate = var3;
      this.bannedBy = var2.has("source") ? var2.get("source").getAsString() : "(Unknown)";

      Date var4;
      try {
         var4 = var2.has("expires") ? DATE_FORMAT.parse(var2.get("expires").getAsString()) : null;
      } catch (ParseException var5) {
         var4 = null;
      }

      this.banEndDate = var4;
      this.reason = var2.has("reason") ? var2.get("reason").getAsString() : "Banned by an operator.";
   }

   public Date getBanEndDate() {
      return this.banEndDate;
   }

   public String getBanReason() {
      return this.reason;
   }

   boolean hasBanExpired() {
      return this.banEndDate == null ? false : this.banEndDate.before(new Date());
   }

   protected void onSerialization(JsonObject var1) {
      var1.addProperty("created", DATE_FORMAT.format(this.banStartDate));
      var1.addProperty("source", this.bannedBy);
      var1.addProperty("expires", this.banEndDate == null ? "forever" : DATE_FORMAT.format(this.banEndDate));
      var1.addProperty("reason", this.reason);
   }

   public String getSource() {
      return this.bannedBy;
   }

   public Date getCreated() {
      return this.banStartDate;
   }

   private static Object checkExpiry(Object var0, JsonObject var1) {
      Date var2 = null;

      try {
         var2 = var1.has("expires") ? DATE_FORMAT.parse(var1.get("expires").getAsString()) : null;
      } catch (ParseException var3) {
         ;
      }

      return var2 != null && !var2.after(new Date()) ? null : var0;
   }
}
