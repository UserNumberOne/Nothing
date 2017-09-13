package net.minecraft.util;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

public class EnumTypeAdapterFactory implements TypeAdapterFactory {
   public TypeAdapter create(Gson var1, TypeToken var2) {
      Class var3 = var2.getRawType();
      if (!var3.isEnum()) {
         return null;
      } else {
         final HashMap var4 = Maps.newHashMap();

         for(Object var8 : var3.getEnumConstants()) {
            var4.put(this.getName(var8), var8);
         }

         return new TypeAdapter() {
            public void write(JsonWriter var1, Object var2) throws IOException {
               if (var2 == null) {
                  var1.nullValue();
               } else {
                  var1.value(EnumTypeAdapterFactory.this.getName(var2));
               }

            }

            public Object read(JsonReader var1) throws IOException {
               if (var1.peek() == JsonToken.NULL) {
                  var1.nextNull();
                  return null;
               } else {
                  return var4.get(var1.nextString());
               }
            }
         };
      }
   }

   private String getName(Object var1) {
      return var1 instanceof Enum ? ((Enum)var1).name().toLowerCase(Locale.US) : var1.toString().toLowerCase(Locale.US);
   }
}
