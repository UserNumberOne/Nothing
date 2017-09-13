package net.minecraft.util.datafix.fixes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StringUtils;
import net.minecraft.util.datafix.IFixableData;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class SignStrictJSON implements IFixableData {
   public static final Gson GSON_INSTANCE = (new GsonBuilder()).registerTypeAdapter(ITextComponent.class, new JsonDeserializer() {
      public ITextComponent deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         if (var1.isJsonPrimitive()) {
            return new TextComponentString(var1.getAsString());
         } else if (var1.isJsonArray()) {
            JsonArray var4 = var1.getAsJsonArray();
            ITextComponent var5 = null;

            for(JsonElement var7 : var4) {
               ITextComponent var8 = this.deserialize(var7, var7.getClass(), var3);
               if (var5 == null) {
                  var5 = var8;
               } else {
                  var5.appendSibling(var8);
               }
            }

            return var5;
         } else {
            throw new JsonParseException("Don't know how to turn " + var1 + " into a Component");
         }
      }

      // $FF: synthetic method
      public Object deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         return this.deserialize(var1, var2, var3);
      }
   }).create();

   public int getFixVersion() {
      return 101;
   }

   public NBTTagCompound fixTagCompound(NBTTagCompound var1) {
      if ("Sign".equals(var1.getString("id"))) {
         this.updateLine(var1, "Text1");
         this.updateLine(var1, "Text2");
         this.updateLine(var1, "Text3");
         this.updateLine(var1, "Text4");
      }

      return var1;
   }

   private void updateLine(NBTTagCompound var1, String var2) {
      String var3 = var1.getString(var2);
      Object var4 = null;
      if (!"null".equals(var3) && !StringUtils.isNullOrEmpty(var3)) {
         if (var3.charAt(0) == '"' && var3.charAt(var3.length() - 1) == '"' || var3.charAt(0) == '{' && var3.charAt(var3.length() - 1) == '}') {
            try {
               var4 = (ITextComponent)GSON_INSTANCE.fromJson(var3, ITextComponent.class);
               if (var4 == null) {
                  var4 = new TextComponentString("");
               }
            } catch (JsonParseException var8) {
               ;
            }

            if (var4 == null) {
               try {
                  var4 = ITextComponent.Serializer.jsonToComponent(var3);
               } catch (JsonParseException var7) {
                  ;
               }
            }

            if (var4 == null) {
               try {
                  var4 = ITextComponent.Serializer.fromJsonLenient(var3);
               } catch (JsonParseException var6) {
                  ;
               }
            }

            if (var4 == null) {
               var4 = new TextComponentString(var3);
            }
         } else {
            var4 = new TextComponentString(var3);
         }
      } else {
         var4 = new TextComponentString("");
      }

      var1.setString(var2, ITextComponent.Serializer.componentToJson((ITextComponent)var4));
   }
}
