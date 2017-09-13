package net.minecraft.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import javax.annotation.Nullable;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class JsonUtils {
   public static boolean isString(JsonObject var0, String var1) {
      return !isJsonPrimitive(var0, var1) ? false : var0.getAsJsonPrimitive(var1).isString();
   }

   @SideOnly(Side.CLIENT)
   public static boolean isString(JsonElement var0) {
      return !var0.isJsonPrimitive() ? false : var0.getAsJsonPrimitive().isString();
   }

   public static boolean isNumber(JsonElement var0) {
      return !var0.isJsonPrimitive() ? false : var0.getAsJsonPrimitive().isNumber();
   }

   @SideOnly(Side.CLIENT)
   public static boolean isBoolean(JsonObject var0, String var1) {
      return !isJsonPrimitive(var0, var1) ? false : var0.getAsJsonPrimitive(var1).isBoolean();
   }

   public static boolean isJsonArray(JsonObject var0, String var1) {
      return !hasField(var0, var1) ? false : var0.get(var1).isJsonArray();
   }

   public static boolean isJsonPrimitive(JsonObject var0, String var1) {
      return !hasField(var0, var1) ? false : var0.get(var1).isJsonPrimitive();
   }

   public static boolean hasField(JsonObject var0, String var1) {
      return var0 == null ? false : var0.get(var1) != null;
   }

   public static String getString(JsonElement var0, String var1) {
      if (var0.isJsonPrimitive()) {
         return var0.getAsString();
      } else {
         throw new JsonSyntaxException("Expected " + var1 + " to be a string, was " + toString(var0));
      }
   }

   public static String getString(JsonObject var0, String var1) {
      if (var0.has(var1)) {
         return getString(var0.get(var1), var1);
      } else {
         throw new JsonSyntaxException("Missing " + var1 + ", expected to find a string");
      }
   }

   @SideOnly(Side.CLIENT)
   public static String getString(JsonObject var0, String var1, String var2) {
      return var0.has(var1) ? getString(var0.get(var1), var1) : var2;
   }

   public static Item getItem(JsonElement var0, String var1) {
      if (var0.isJsonPrimitive()) {
         String var2 = var0.getAsString();
         Item var3 = Item.getByNameOrId(var2);
         if (var3 == null) {
            throw new JsonSyntaxException("Expected " + var1 + " to be an item, was unknown string '" + var2 + "'");
         } else {
            return var3;
         }
      } else {
         throw new JsonSyntaxException("Expected " + var1 + " to be an item, was " + toString(var0));
      }
   }

   public static Item getItem(JsonObject var0, String var1) {
      if (var0.has(var1)) {
         return getItem(var0.get(var1), var1);
      } else {
         throw new JsonSyntaxException("Missing " + var1 + ", expected to find an item");
      }
   }

   public static boolean getBoolean(JsonElement var0, String var1) {
      if (var0.isJsonPrimitive()) {
         return var0.getAsBoolean();
      } else {
         throw new JsonSyntaxException("Expected " + var1 + " to be a Boolean, was " + toString(var0));
      }
   }

   @SideOnly(Side.CLIENT)
   public static boolean getBoolean(JsonObject var0, String var1) {
      if (var0.has(var1)) {
         return getBoolean(var0.get(var1), var1);
      } else {
         throw new JsonSyntaxException("Missing " + var1 + ", expected to find a Boolean");
      }
   }

   public static boolean getBoolean(JsonObject var0, String var1, boolean var2) {
      return var0.has(var1) ? getBoolean(var0.get(var1), var1) : var2;
   }

   public static float getFloat(JsonElement var0, String var1) {
      if (var0.isJsonPrimitive() && var0.getAsJsonPrimitive().isNumber()) {
         return var0.getAsFloat();
      } else {
         throw new JsonSyntaxException("Expected " + var1 + " to be a Float, was " + toString(var0));
      }
   }

   public static float getFloat(JsonObject var0, String var1) {
      if (var0.has(var1)) {
         return getFloat(var0.get(var1), var1);
      } else {
         throw new JsonSyntaxException("Missing " + var1 + ", expected to find a Float");
      }
   }

   public static float getFloat(JsonObject var0, String var1, float var2) {
      return var0.has(var1) ? getFloat(var0.get(var1), var1) : var2;
   }

   public static int getInt(JsonElement var0, String var1) {
      if (var0.isJsonPrimitive() && var0.getAsJsonPrimitive().isNumber()) {
         return var0.getAsInt();
      } else {
         throw new JsonSyntaxException("Expected " + var1 + " to be a Int, was " + toString(var0));
      }
   }

   public static int getInt(JsonObject var0, String var1) {
      if (var0.has(var1)) {
         return getInt(var0.get(var1), var1);
      } else {
         throw new JsonSyntaxException("Missing " + var1 + ", expected to find a Int");
      }
   }

   public static int getInt(JsonObject var0, String var1, int var2) {
      return var0.has(var1) ? getInt(var0.get(var1), var1) : var2;
   }

   public static JsonObject getJsonObject(JsonElement var0, String var1) {
      if (var0.isJsonObject()) {
         return var0.getAsJsonObject();
      } else {
         throw new JsonSyntaxException("Expected " + var1 + " to be a JsonObject, was " + toString(var0));
      }
   }

   public static JsonObject getJsonObject(JsonObject var0, String var1) {
      if (var0.has(var1)) {
         return getJsonObject(var0.get(var1), var1);
      } else {
         throw new JsonSyntaxException("Missing " + var1 + ", expected to find a JsonObject");
      }
   }

   @SideOnly(Side.CLIENT)
   public static JsonObject getJsonObject(JsonObject var0, String var1, JsonObject var2) {
      return var0.has(var1) ? getJsonObject(var0.get(var1), var1) : var2;
   }

   public static JsonArray getJsonArray(JsonElement var0, String var1) {
      if (var0.isJsonArray()) {
         return var0.getAsJsonArray();
      } else {
         throw new JsonSyntaxException("Expected " + var1 + " to be a JsonArray, was " + toString(var0));
      }
   }

   public static JsonArray getJsonArray(JsonObject var0, String var1) {
      if (var0.has(var1)) {
         return getJsonArray(var0.get(var1), var1);
      } else {
         throw new JsonSyntaxException("Missing " + var1 + ", expected to find a JsonArray");
      }
   }

   @SideOnly(Side.CLIENT)
   public static JsonArray getJsonArray(JsonObject var0, String var1, @Nullable JsonArray var2) {
      return var0.has(var1) ? getJsonArray(var0.get(var1), var1) : var2;
   }

   public static Object deserializeClass(@Nullable JsonElement var0, String var1, JsonDeserializationContext var2, Class var3) {
      if (var0 != null) {
         return var2.deserialize(var0, var3);
      } else {
         throw new JsonSyntaxException("Missing " + var1);
      }
   }

   public static Object deserializeClass(JsonObject var0, String var1, JsonDeserializationContext var2, Class var3) {
      if (var0.has(var1)) {
         return deserializeClass(var0.get(var1), var1, var2, var3);
      } else {
         throw new JsonSyntaxException("Missing " + var1);
      }
   }

   public static Object deserializeClass(JsonObject var0, String var1, Object var2, JsonDeserializationContext var3, Class var4) {
      return var0.has(var1) ? deserializeClass(var0.get(var1), var1, var3, var4) : var2;
   }

   public static String toString(JsonElement var0) {
      String var1 = org.apache.commons.lang3.StringUtils.abbreviateMiddle(String.valueOf(var0), "...", 10);
      if (var0 == null) {
         return "null (missing)";
      } else if (var0.isJsonNull()) {
         return "null (json)";
      } else if (var0.isJsonArray()) {
         return "an array (" + var1 + ")";
      } else if (var0.isJsonObject()) {
         return "an object (" + var1 + ")";
      } else {
         if (var0.isJsonPrimitive()) {
            JsonPrimitive var2 = var0.getAsJsonPrimitive();
            if (var2.isNumber()) {
               return "a number (" + var1 + ")";
            }

            if (var2.isBoolean()) {
               return "a boolean (" + var1 + ")";
            }
         }

         return var1;
      }
   }

   public static Object gsonDeserialize(Gson var0, Reader var1, Class var2, boolean var3) {
      try {
         JsonReader var4 = new JsonReader(var1);
         var4.setLenient(var3);
         return var0.getAdapter(var2).read(var4);
      } catch (IOException var5) {
         throw new JsonParseException(var5);
      }
   }

   public static Object gsonDeserialize(Gson var0, String var1, Class var2) {
      return gsonDeserialize(var0, var1, var2, false);
   }

   public static Object gsonDeserialize(Gson var0, String var1, Class var2, boolean var3) {
      return gsonDeserialize(var0, new StringReader(var1), var2, var3);
   }
}
