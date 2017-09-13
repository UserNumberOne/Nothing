package net.minecraft.client.util;

import com.google.common.collect.Lists;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;

@SideOnly(Side.CLIENT)
public class JsonException extends IOException {
   private final List entries = Lists.newArrayList();
   private final String message;

   public JsonException(String var1) {
      this.entries.add(new JsonException.Entry());
      this.message = var1;
   }

   public JsonException(String var1, Throwable var2) {
      super(var2);
      this.entries.add(new JsonException.Entry());
      this.message = var1;
   }

   public void prependJsonKey(String var1) {
      ((JsonException.Entry)this.entries.get(0)).addJsonKey(var1);
   }

   public void setFilenameAndFlush(String var1) {
      ((JsonException.Entry)this.entries.get(0)).filename = var1;
      this.entries.add(0, new JsonException.Entry());
   }

   public String getMessage() {
      return "Invalid " + this.entries.get(this.entries.size() - 1) + ": " + this.message;
   }

   public static JsonException forException(Exception var0) {
      if (var0 instanceof JsonException) {
         return (JsonException)var0;
      } else {
         String var1 = var0.getMessage();
         if (var0 instanceof FileNotFoundException) {
            var1 = "File not found";
         }

         return new JsonException(var1, var0);
      }
   }

   @SideOnly(Side.CLIENT)
   public static class Entry {
      private String filename;
      private final List jsonKeys;

      private Entry() {
         this.jsonKeys = Lists.newArrayList();
      }

      private void addJsonKey(String var1) {
         this.jsonKeys.add(0, var1);
      }

      public String getJsonKeys() {
         return StringUtils.join(this.jsonKeys, "->");
      }

      public String toString() {
         return this.filename != null ? (this.jsonKeys.isEmpty() ? this.filename : this.filename + " " + this.getJsonKeys()) : (this.jsonKeys.isEmpty() ? "(Unknown file)" : "(Unknown file) " + this.getJsonKeys());
      }
   }
}
