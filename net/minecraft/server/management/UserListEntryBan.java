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

   public UserListEntryBan(Object t0, Date date, String s, Date date1, String s1) {
      super(t0);
      this.banStartDate = date == null ? new Date() : date;
      this.bannedBy = s == null ? "(Unknown)" : s;
      this.banEndDate = date1;
      this.reason = s1 == null ? "Banned by an operator." : s1;
   }

   protected UserListEntryBan(Object t0, JsonObject jsonobject) {
      super(checkExpiry(t0, jsonobject), jsonobject);

      Date date;
      try {
         date = jsonobject.has("created") ? DATE_FORMAT.parse(jsonobject.get("created").getAsString()) : new Date();
      } catch (ParseException var6) {
         date = new Date();
      }

      this.banStartDate = date;
      this.bannedBy = jsonobject.has("source") ? jsonobject.get("source").getAsString() : "(Unknown)";

      Date date1;
      try {
         date1 = jsonobject.has("expires") ? DATE_FORMAT.parse(jsonobject.get("expires").getAsString()) : null;
      } catch (ParseException var5) {
         date1 = null;
      }

      this.banEndDate = date1;
      this.reason = jsonobject.has("reason") ? jsonobject.get("reason").getAsString() : "Banned by an operator.";
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

   protected void onSerialization(JsonObject jsonobject) {
      jsonobject.addProperty("created", DATE_FORMAT.format(this.banStartDate));
      jsonobject.addProperty("source", this.bannedBy);
      jsonobject.addProperty("expires", this.banEndDate == null ? "forever" : DATE_FORMAT.format(this.banEndDate));
      jsonobject.addProperty("reason", this.reason);
   }

   public String getSource() {
      return this.bannedBy;
   }

   public Date getCreated() {
      return this.banStartDate;
   }

   private static Object checkExpiry(Object object, JsonObject jsonobject) {
      Date expires = null;

      try {
         expires = jsonobject.has("expires") ? DATE_FORMAT.parse(jsonobject.get("expires").getAsString()) : null;
      } catch (ParseException var3) {
         ;
      }

      return expires != null && !expires.after(new Date()) ? null : object;
   }
}
