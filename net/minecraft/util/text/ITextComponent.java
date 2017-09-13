package net.minecraft.util.text;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.util.EnumTypeAdapterFactory;
import net.minecraft.util.JsonUtils;

public interface ITextComponent extends Iterable {
   ITextComponent setStyle(Style var1);

   Style getStyle();

   ITextComponent appendText(String var1);

   ITextComponent appendSibling(ITextComponent var1);

   String getUnformattedComponentText();

   String getUnformattedText();

   List getSiblings();

   ITextComponent createCopy();

   public static class Serializer implements JsonDeserializer, JsonSerializer {
      private static final Gson GSON;

      public ITextComponent deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         if (var1.isJsonPrimitive()) {
            return new TextComponentString(var1.getAsString());
         } else if (!var1.isJsonObject()) {
            if (var1.isJsonArray()) {
               JsonArray var11 = var1.getAsJsonArray();
               ITextComponent var12 = null;

               for(JsonElement var17 : var11) {
                  ITextComponent var18 = this.deserialize(var17, var17.getClass(), var3);
                  if (var12 == null) {
                     var12 = var18;
                  } else {
                     var12.appendSibling(var18);
                  }
               }

               return var12;
            } else {
               throw new JsonParseException("Don't know how to turn " + var1 + " into a Component");
            }
         } else {
            JsonObject var4 = var1.getAsJsonObject();
            Object var5;
            if (var4.has("text")) {
               var5 = new TextComponentString(var4.get("text").getAsString());
            } else if (var4.has("translate")) {
               String var6 = var4.get("translate").getAsString();
               if (var4.has("with")) {
                  JsonArray var7 = var4.getAsJsonArray("with");
                  Object[] var8 = new Object[var7.size()];

                  for(int var9 = 0; var9 < var8.length; ++var9) {
                     var8[var9] = this.deserialize(var7.get(var9), var2, var3);
                     if (var8[var9] instanceof TextComponentString) {
                        TextComponentString var10 = (TextComponentString)var8[var9];
                        if (var10.getStyle().isEmpty() && var10.getSiblings().isEmpty()) {
                           var8[var9] = var10.getText();
                        }
                     }
                  }

                  var5 = new TextComponentTranslation(var6, var8);
               } else {
                  var5 = new TextComponentTranslation(var6, new Object[0]);
               }
            } else if (var4.has("score")) {
               JsonObject var13 = var4.getAsJsonObject("score");
               if (!var13.has("name") || !var13.has("objective")) {
                  throw new JsonParseException("A score component needs a least a name and an objective");
               }

               var5 = new TextComponentScore(JsonUtils.getString(var13, "name"), JsonUtils.getString(var13, "objective"));
               if (var13.has("value")) {
                  ((TextComponentScore)var5).setValue(JsonUtils.getString(var13, "value"));
               }
            } else {
               if (!var4.has("selector")) {
                  throw new JsonParseException("Don't know how to turn " + var1 + " into a Component");
               }

               var5 = new TextComponentSelector(JsonUtils.getString(var4, "selector"));
            }

            if (var4.has("extra")) {
               JsonArray var14 = var4.getAsJsonArray("extra");
               if (var14.size() <= 0) {
                  throw new JsonParseException("Unexpected empty array of components");
               }

               for(int var16 = 0; var16 < var14.size(); ++var16) {
                  ((ITextComponent)var5).appendSibling(this.deserialize(var14.get(var16), var2, var3));
               }
            }

            ((ITextComponent)var5).setStyle((Style)var3.deserialize(var1, Style.class));
            return (ITextComponent)var5;
         }
      }

      private void serializeChatStyle(Style var1, JsonObject var2, JsonSerializationContext var3) {
         JsonElement var4 = var3.serialize(var1);
         if (var4.isJsonObject()) {
            JsonObject var5 = (JsonObject)var4;

            for(Entry var7 : var5.entrySet()) {
               var2.add((String)var7.getKey(), (JsonElement)var7.getValue());
            }
         }

      }

      public JsonElement serialize(ITextComponent var1, Type var2, JsonSerializationContext var3) {
         JsonObject var4 = new JsonObject();
         if (!var1.getStyle().isEmpty()) {
            this.serializeChatStyle(var1.getStyle(), var4, var3);
         }

         if (!var1.getSiblings().isEmpty()) {
            JsonArray var5 = new JsonArray();

            for(ITextComponent var7 : var1.getSiblings()) {
               var5.add(this.serialize(var7, var7.getClass(), var3));
            }

            var4.add("extra", var5);
         }

         if (var1 instanceof TextComponentString) {
            var4.addProperty("text", ((TextComponentString)var1).getText());
         } else if (var1 instanceof TextComponentTranslation) {
            TextComponentTranslation var11 = (TextComponentTranslation)var1;
            var4.addProperty("translate", var11.getKey());
            if (var11.getFormatArgs() != null && var11.getFormatArgs().length > 0) {
               JsonArray var14 = new JsonArray();

               for(Object var10 : var11.getFormatArgs()) {
                  if (var10 instanceof ITextComponent) {
                     var14.add(this.serialize((ITextComponent)var10, var10.getClass(), var3));
                  } else {
                     var14.add(new JsonPrimitive(String.valueOf(var10)));
                  }
               }

               var4.add("with", var14);
            }
         } else if (var1 instanceof TextComponentScore) {
            TextComponentScore var12 = (TextComponentScore)var1;
            JsonObject var15 = new JsonObject();
            var15.addProperty("name", var12.getName());
            var15.addProperty("objective", var12.getObjective());
            var15.addProperty("value", var12.getUnformattedComponentText());
            var4.add("score", var15);
         } else {
            if (!(var1 instanceof TextComponentSelector)) {
               throw new IllegalArgumentException("Don't know how to serialize " + var1 + " as a Component");
            }

            TextComponentSelector var13 = (TextComponentSelector)var1;
            var4.addProperty("selector", var13.getSelector());
         }

         return var4;
      }

      public static String componentToJson(ITextComponent var0) {
         return GSON.toJson(var0);
      }

      public static ITextComponent jsonToComponent(String var0) {
         return (ITextComponent)JsonUtils.gsonDeserialize(GSON, var0, ITextComponent.class, false);
      }

      public static ITextComponent fromJsonLenient(String var0) {
         return (ITextComponent)JsonUtils.gsonDeserialize(GSON, var0, ITextComponent.class, true);
      }

      // $FF: synthetic method
      public JsonElement serialize(Object var1, Type var2, JsonSerializationContext var3) {
         return this.serialize((ITextComponent)var1, var2, var3);
      }

      // $FF: synthetic method
      public Object deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         return this.deserialize(var1, var2, var3);
      }

      static {
         GsonBuilder var0 = new GsonBuilder();
         var0.registerTypeHierarchyAdapter(ITextComponent.class, new ITextComponent.Serializer());
         var0.registerTypeHierarchyAdapter(Style.class, new Style.Serializer());
         var0.registerTypeAdapterFactory(new EnumTypeAdapterFactory());
         GSON = var0.create();
      }
   }
}
