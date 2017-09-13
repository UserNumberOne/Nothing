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
      return !isJsonPrimitive(json, memberName) ? false : json.getAsJsonPrimitive(memberName).isString();
   }

   @SideOnly(Side.CLIENT)
   public static boolean isString(JsonElement var0) {
      return !json.isJsonPrimitive() ? false : json.getAsJsonPrimitive().isString();
   }

   public static boolean isNumber(JsonElement var0) {
      return !json.isJsonPrimitive() ? false : json.getAsJsonPrimitive().isNumber();
   }

   @SideOnly(Side.CLIENT)
   public static boolean isBoolean(JsonObject var0, String var1) {
      return !isJsonPrimitive(json, memberName) ? false : json.getAsJsonPrimitive(memberName).isBoolean();
   }

   public static boolean isJsonArray(JsonObject var0, String var1) {
      return !hasField(json, memberName) ? false : json.get(memberName).isJsonArray();
   }

   public static boolean isJsonPrimitive(JsonObject var0, String var1) {
      return !hasField(json, memberName) ? false : json.get(memberName).isJsonPrimitive();
   }

   public static boolean hasField(JsonObject var0, String var1) {
      return json == null ? false : json.get(memberName) != null;
   }

   public static String getString(JsonElement var0, String var1) {
      if (json.isJsonPrimitive()) {
         return json.getAsString();
      } else {
         throw new JsonSyntaxException("Expected " + memberName + " to be a string, was " + toString(json));
      }
   }

   public static String getString(JsonObject var0, String var1) {
      if (json.has(memberName)) {
         return getString(json.get(memberName), memberName);
      } else {
         throw new JsonSyntaxException("Missing " + memberName + ", expected to find a string");
      }
   }

   @SideOnly(Side.CLIENT)
   public static String getString(JsonObject var0, String var1, String var2) {
      return json.has(memberName) ? getString(json.get(memberName), memberName) : fallback;
   }

   public static Item getItem(JsonElement var0, String var1) {
      if (json.isJsonPrimitive()) {
         String s = json.getAsString();
         Item item = Item.getByNameOrId(s);
         if (item == null) {
            throw new JsonSyntaxException("Expected " + memberName + " to be an item, was unknown string '" + s + "'");
         } else {
            return item;
         }
      } else {
         throw new JsonSyntaxException("Expected " + memberName + " to be an item, was " + toString(json));
      }
   }

   public static Item getItem(JsonObject var0, String var1) {
      if (json.has(memberName)) {
         return getItem(json.get(memberName), memberName);
      } else {
         throw new JsonSyntaxException("Missing " + memberName + ", expected to find an item");
      }
   }

   public static boolean getBoolean(JsonElement var0, String var1) {
      if (json.isJsonPrimitive()) {
         return json.getAsBoolean();
      } else {
         throw new JsonSyntaxException("Expected " + memberName + " to be a Boolean, was " + toString(json));
      }
   }

   @SideOnly(Side.CLIENT)
   public static boolean getBoolean(JsonObject var0, String var1) {
      if (json.has(memberName)) {
         return getBoolean(json.get(memberName), memberName);
      } else {
         throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Boolean");
      }
   }

   public static boolean getBoolean(JsonObject var0, String var1, boolean var2) {
      return json.has(memberName) ? getBoolean(json.get(memberName), memberName) : fallback;
   }

   public static float getFloat(JsonElement var0, String var1) {
      if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
         return json.getAsFloat();
      } else {
         throw new JsonSyntaxException("Expected " + memberName + " to be a Float, was " + toString(json));
      }
   }

   public static float getFloat(JsonObject var0, String var1) {
      if (json.has(memberName)) {
         return getFloat(json.get(memberName), memberName);
      } else {
         throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Float");
      }
   }

   public static float getFloat(JsonObject var0, String var1, float var2) {
      return json.has(memberName) ? getFloat(json.get(memberName), memberName) : fallback;
   }

   public static int getInt(JsonElement var0, String var1) {
      if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
         return json.getAsInt();
      } else {
         throw new JsonSyntaxException("Expected " + memberName + " to be a Int, was " + toString(json));
      }
   }

   public static int getInt(JsonObject var0, String var1) {
      if (json.has(memberName)) {
         return getInt(json.get(memberName), memberName);
      } else {
         throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Int");
      }
   }

   public static int getInt(JsonObject var0, String var1, int var2) {
      return json.has(memberName) ? getInt(json.get(memberName), memberName) : fallback;
   }

   public static JsonObject getJsonObject(JsonElement var0, String var1) {
      if (json.isJsonObject()) {
         return json.getAsJsonObject();
      } else {
         throw new JsonSyntaxException("Expected " + memberName + " to be a JsonObject, was " + toString(json));
      }
   }

   public static JsonObject getJsonObject(JsonObject var0, String var1) {
      if (json.has(memberName)) {
         return getJsonObject(json.get(memberName), memberName);
      } else {
         throw new JsonSyntaxException("Missing " + memberName + ", expected to find a JsonObject");
      }
   }

   @SideOnly(Side.CLIENT)
   public static JsonObject getJsonObject(JsonObject var0, String var1, JsonObject var2) {
      return json.has(memberName) ? getJsonObject(json.get(memberName), memberName) : fallback;
   }

   public static JsonArray getJsonArray(JsonElement var0, String var1) {
      if (json.isJsonArray()) {
         return json.getAsJsonArray();
      } else {
         throw new JsonSyntaxException("Expected " + memberName + " to be a JsonArray, was " + toString(json));
      }
   }

   public static JsonArray getJsonArray(JsonObject var0, String var1) {
      if (json.has(memberName)) {
         return getJsonArray(json.get(memberName), memberName);
      } else {
         throw new JsonSyntaxException("Missing " + memberName + ", expected to find a JsonArray");
      }
   }

   @SideOnly(Side.CLIENT)
   public static JsonArray getJsonArray(JsonObject var0, String var1, @Nullable JsonArray var2) {
      return json.has(memberName) ? getJsonArray(json.get(memberName), memberName) : fallback;
   }

   public static Object deserializeClass(@Nullable JsonElement var0, String var1, JsonDeserializationContext var2, Class var3) {
      if (json != null) {
         return context.deserialize(json, adapter);
      } else {
         throw new JsonSyntaxException("Missing " + memberName);
      }
   }

   public static Object deserializeClass(JsonObject var0, String var1, JsonDeserializationContext var2, Class var3) {
      if (json.has(memberName)) {
         return deserializeClass(json.get(memberName), memberName, context, adapter);
      } else {
         throw new JsonSyntaxException("Missing " + memberName);
      }
   }

   public static Object deserializeClass(JsonObject var0, String var1, Object var2, JsonDeserializationContext var3, Class var4) {
      return json.has(memberName) ? deserializeClass(json.get(memberName), memberName, context, adapter) : fallback;
   }

   public static String toString(JsonElement var0) {
      String s = org.apache.commons.lang3.StringUtils.abbreviateMiddle(String.valueOf(json), "...", 10);
      if (json == null) {
         return "null (missing)";
      } else if (json.isJsonNull()) {
         return "null (json)";
      } else if (json.isJsonArray()) {
         return "an array (" + s + ")";
      } else if (json.isJsonObject()) {
         return "an object (" + s + ")";
      } else {
         if (json.isJsonPrimitive()) {
            JsonPrimitive jsonprimitive = json.getAsJsonPrimitive();
            if (jsonprimitive.isNumber()) {
               return "a number (" + s + ")";
            }

            if (jsonprimitive.isBoolean()) {
               return "a boolean (" + s + ")";
            }
         }

         return s;
      }
   }

   public static Object gsonDeserialize(Gson var0, Reader var1, Class var2, boolean var3) {
      try {
         JsonReader jsonreader = new JsonReader(readerIn);
         jsonreader.setLenient(lenient);
         return gsonIn.getAdapter(adapter).read(jsonreader);
      } catch (IOException var5) {
         throw new JsonParseException(var5);
      }
   }

   public static Object gsonDeserialize(Gson var0, String var1, Class var2) {
      return gsonDeserialize(gsonIn, json, adapter, false);
   }

   public static Object gsonDeserialize(Gson var0, String var1, Class var2, boolean var3) {
      return gsonDeserialize(gsonIn, new StringReader(json), adapter, lenient);
   }
}
