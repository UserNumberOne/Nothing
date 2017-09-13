package net.minecraft.nbt;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Stack;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JsonToNBT {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Pattern INT_ARRAY_MATCHER = Pattern.compile("\\[[-+\\d|,\\s]+\\]");

   public static NBTTagCompound getTagFromJson(String var0) throws NBTException {
      var0 = var0.trim();
      if (!var0.startsWith("{")) {
         throw new NBTException("Invalid tag encountered, expected '{' as first char.");
      } else if (topTagsCount(var0) != 1) {
         throw new NBTException("Encountered multiple top tags, only one expected");
      } else {
         return (NBTTagCompound)nameValueToNBT("tag", var0).parse();
      }
   }

   static int topTagsCount(String var0) throws NBTException {
      int var1 = 0;
      boolean var2 = false;
      Stack var3 = new Stack();

      for(int var4 = 0; var4 < var0.length(); ++var4) {
         char var5 = var0.charAt(var4);
         if (var5 == '"') {
            if (isCharEscaped(var0, var4)) {
               if (!var2) {
                  throw new NBTException("Illegal use of \\\": " + var0);
               }
            } else {
               var2 = !var2;
            }
         } else if (!var2) {
            if (var5 != '{' && var5 != '[') {
               if (var5 == '}' && (var3.isEmpty() || ((Character)var3.pop()).charValue() != '{')) {
                  throw new NBTException("Unbalanced curly brackets {}: " + var0);
               }

               if (var5 == ']' && (var3.isEmpty() || ((Character)var3.pop()).charValue() != '[')) {
                  throw new NBTException("Unbalanced square brackets []: " + var0);
               }
            } else {
               if (var3.isEmpty()) {
                  ++var1;
               }

               var3.push(Character.valueOf(var5));
            }
         }
      }

      if (var2) {
         throw new NBTException("Unbalanced quotation: " + var0);
      } else if (!var3.isEmpty()) {
         throw new NBTException("Unbalanced brackets: " + var0);
      } else {
         if (var1 == 0 && !var0.isEmpty()) {
            var1 = 1;
         }

         return var1;
      }
   }

   static JsonToNBT.Any joinStrToNBT(String... var0) throws NBTException {
      return nameValueToNBT(var0[0], var0[1]);
   }

   static JsonToNBT.Any nameValueToNBT(String var0, String var1) throws NBTException {
      var1 = var1.trim();
      if (var1.startsWith("{")) {
         var1 = var1.substring(1, var1.length() - 1);

         JsonToNBT.Compound var8;
         String var9;
         for(var8 = new JsonToNBT.Compound(var0); var1.length() > 0; var1 = var1.substring(var9.length() + 1)) {
            var9 = nextNameValuePair(var1, true);
            if (var9.length() > 0) {
               boolean var11 = false;
               var8.tagList.add(getTagFromNameValue(var9, false));
            }

            if (var1.length() < var9.length() + 1) {
               break;
            }

            char var12 = var1.charAt(var9.length());
            if (var12 != ',' && var12 != '{' && var12 != '}' && var12 != '[' && var12 != ']') {
               throw new NBTException("Unexpected token '" + var12 + "' at: " + var1.substring(var9.length()));
            }
         }

         return var8;
      } else if (var1.startsWith("[") && !INT_ARRAY_MATCHER.matcher(var1).matches()) {
         var1 = var1.substring(1, var1.length() - 1);

         JsonToNBT.List var2;
         String var3;
         for(var2 = new JsonToNBT.List(var0); var1.length() > 0; var1 = var1.substring(var3.length() + 1)) {
            var3 = nextNameValuePair(var1, false);
            if (var3.length() > 0) {
               boolean var4 = true;
               var2.tagList.add(getTagFromNameValue(var3, true));
            }

            if (var1.length() < var3.length() + 1) {
               break;
            }

            char var10 = var1.charAt(var3.length());
            if (var10 != ',' && var10 != '{' && var10 != '}' && var10 != '[' && var10 != ']') {
               throw new NBTException("Unexpected token '" + var10 + "' at: " + var1.substring(var3.length()));
            }
         }

         return var2;
      } else {
         return new JsonToNBT.Primitive(var0, var1);
      }
   }

   private static JsonToNBT.Any getTagFromNameValue(String var0, boolean var1) throws NBTException {
      String var2 = locateName(var0, var1);
      String var3 = locateValue(var0, var1);
      return joinStrToNBT(var2, var3);
   }

   private static String nextNameValuePair(String var0, boolean var1) throws NBTException {
      int var2 = getNextCharIndex(var0, ':');
      int var3 = getNextCharIndex(var0, ',');
      if (var1) {
         if (var2 == -1) {
            throw new NBTException("Unable to locate name/value separator for string: " + var0);
         }

         if (var3 != -1 && var3 < var2) {
            throw new NBTException("Name error at: " + var0);
         }
      } else if (var2 == -1 || var2 > var3) {
         var2 = -1;
      }

      return locateValueAt(var0, var2);
   }

   private static String locateValueAt(String var0, int var1) throws NBTException {
      Stack var2 = new Stack();
      int var3 = var1 + 1;
      boolean var4 = false;
      boolean var5 = false;
      boolean var6 = false;

      for(int var7 = 0; var3 < var0.length(); ++var3) {
         char var8 = var0.charAt(var3);
         if (var8 == '"') {
            if (isCharEscaped(var0, var3)) {
               if (!var4) {
                  throw new NBTException("Illegal use of \\\": " + var0);
               }
            } else {
               var4 = !var4;
               if (var4 && !var6) {
                  var5 = true;
               }

               if (!var4) {
                  var7 = var3;
               }
            }
         } else if (!var4) {
            if (var8 != '{' && var8 != '[') {
               if (var8 == '}' && (var2.isEmpty() || ((Character)var2.pop()).charValue() != '{')) {
                  throw new NBTException("Unbalanced curly brackets {}: " + var0);
               }

               if (var8 == ']' && (var2.isEmpty() || ((Character)var2.pop()).charValue() != '[')) {
                  throw new NBTException("Unbalanced square brackets []: " + var0);
               }

               if (var8 == ',' && var2.isEmpty()) {
                  return var0.substring(0, var3);
               }
            } else {
               var2.push(Character.valueOf(var8));
            }
         }

         if (!Character.isWhitespace(var8)) {
            if (!var4 && var5 && var7 != var3) {
               return var0.substring(0, var7 + 1);
            }

            var6 = true;
         }
      }

      return var0.substring(0, var3);
   }

   private static String locateName(String var0, boolean var1) throws NBTException {
      if (var1) {
         var0 = var0.trim();
         if (var0.startsWith("{") || var0.startsWith("[")) {
            return "";
         }
      }

      int var2 = getNextCharIndex(var0, ':');
      if (var2 == -1) {
         if (var1) {
            return "";
         } else {
            throw new NBTException("Unable to locate name/value separator for string: " + var0);
         }
      } else {
         return var0.substring(0, var2).trim();
      }
   }

   private static String locateValue(String var0, boolean var1) throws NBTException {
      if (var1) {
         var0 = var0.trim();
         if (var0.startsWith("{") || var0.startsWith("[")) {
            return var0;
         }
      }

      int var2 = getNextCharIndex(var0, ':');
      if (var2 == -1) {
         if (var1) {
            return var0;
         } else {
            throw new NBTException("Unable to locate name/value separator for string: " + var0);
         }
      } else {
         return var0.substring(var2 + 1).trim();
      }
   }

   private static int getNextCharIndex(String var0, char var1) {
      int var2 = 0;

      for(boolean var3 = true; var2 < var0.length(); ++var2) {
         char var4 = var0.charAt(var2);
         if (var4 == '"') {
            if (!isCharEscaped(var0, var2)) {
               var3 = !var3;
            }
         } else if (var3) {
            if (var4 == var1) {
               return var2;
            }

            if (var4 == '{' || var4 == '[') {
               return -1;
            }
         }
      }

      return -1;
   }

   private static boolean isCharEscaped(String var0, int var1) {
      return var1 > 0 && var0.charAt(var1 - 1) == '\\' && !isCharEscaped(var0, var1 - 1);
   }

   abstract static class Any {
      protected String json;

      public abstract NBTBase parse() throws NBTException;
   }

   static class Compound extends JsonToNBT.Any {
      protected java.util.List tagList = Lists.newArrayList();

      public Compound(String var1) {
         this.json = var1;
      }

      public NBTBase parse() throws NBTException {
         NBTTagCompound var1 = new NBTTagCompound();

         for(JsonToNBT.Any var3 : this.tagList) {
            var1.setTag(var3.json, var3.parse());
         }

         return var1;
      }
   }

   static class List extends JsonToNBT.Any {
      protected java.util.List tagList = Lists.newArrayList();

      public List(String var1) {
         this.json = var1;
      }

      public NBTBase parse() throws NBTException {
         NBTTagList var1 = new NBTTagList();

         for(JsonToNBT.Any var3 : this.tagList) {
            var1.appendTag(var3.parse());
         }

         return var1;
      }
   }

   static class Primitive extends JsonToNBT.Any {
      private static final Pattern DOUBLE = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+[d|D]");
      private static final Pattern FLOAT = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+[f|F]");
      private static final Pattern BYTE = Pattern.compile("[-+]?[0-9]+[b|B]");
      private static final Pattern LONG = Pattern.compile("[-+]?[0-9]+[l|L]");
      private static final Pattern SHORT = Pattern.compile("[-+]?[0-9]+[s|S]");
      private static final Pattern INTEGER = Pattern.compile("[-+]?[0-9]+");
      private static final Pattern DOUBLE_UNTYPED = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+");
      private static final Splitter SPLITTER = Splitter.on(',').omitEmptyStrings();
      protected String jsonValue;

      public Primitive(String var1, String var2) {
         this.json = var1;
         this.jsonValue = var2;
      }

      public NBTBase parse() throws NBTException {
         try {
            if (DOUBLE.matcher(this.jsonValue).matches()) {
               return new NBTTagDouble(Double.parseDouble(this.jsonValue.substring(0, this.jsonValue.length() - 1)));
            }

            if (FLOAT.matcher(this.jsonValue).matches()) {
               return new NBTTagFloat(Float.parseFloat(this.jsonValue.substring(0, this.jsonValue.length() - 1)));
            }

            if (BYTE.matcher(this.jsonValue).matches()) {
               return new NBTTagByte(Byte.parseByte(this.jsonValue.substring(0, this.jsonValue.length() - 1)));
            }

            if (LONG.matcher(this.jsonValue).matches()) {
               return new NBTTagLong(Long.parseLong(this.jsonValue.substring(0, this.jsonValue.length() - 1)));
            }

            if (SHORT.matcher(this.jsonValue).matches()) {
               return new NBTTagShort(Short.parseShort(this.jsonValue.substring(0, this.jsonValue.length() - 1)));
            }

            if (INTEGER.matcher(this.jsonValue).matches()) {
               return new NBTTagInt(Integer.parseInt(this.jsonValue));
            }

            if (DOUBLE_UNTYPED.matcher(this.jsonValue).matches()) {
               return new NBTTagDouble(Double.parseDouble(this.jsonValue));
            }

            if ("true".equalsIgnoreCase(this.jsonValue) || "false".equalsIgnoreCase(this.jsonValue)) {
               return new NBTTagByte((byte)(Boolean.parseBoolean(this.jsonValue) ? 1 : 0));
            }
         } catch (NumberFormatException var6) {
            this.jsonValue = this.jsonValue.replaceAll("\\\\\"", "\"");
            return new NBTTagString(this.jsonValue);
         }

         if (this.jsonValue.startsWith("[") && this.jsonValue.endsWith("]")) {
            String var7 = this.jsonValue.substring(1, this.jsonValue.length() - 1);
            String[] var8 = (String[])Iterables.toArray(SPLITTER.split(var7), String.class);

            try {
               int[] var3 = new int[var8.length];

               for(int var4 = 0; var4 < var8.length; ++var4) {
                  var3[var4] = Integer.parseInt(var8[var4].trim());
               }

               return new NBTTagIntArray(var3);
            } catch (NumberFormatException var5) {
               return new NBTTagString(this.jsonValue);
            }
         } else {
            if (this.jsonValue.startsWith("\"") && this.jsonValue.endsWith("\"")) {
               this.jsonValue = this.jsonValue.substring(1, this.jsonValue.length() - 1);
            }

            this.jsonValue = this.jsonValue.replaceAll("\\\\\"", "\"");
            StringBuilder var1 = new StringBuilder();

            for(int var2 = 0; var2 < this.jsonValue.length(); ++var2) {
               if (var2 < this.jsonValue.length() - 1 && this.jsonValue.charAt(var2) == '\\' && this.jsonValue.charAt(var2 + 1) == '\\') {
                  var1.append('\\');
                  ++var2;
               } else {
                  var1.append(this.jsonValue.charAt(var2));
               }
            }

            return new NBTTagString(var1.toString());
         }
      }
   }
}
