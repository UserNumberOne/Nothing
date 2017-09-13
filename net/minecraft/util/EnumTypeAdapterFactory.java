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
import java.util.Locale;
import java.util.Map;

public class EnumTypeAdapterFactory implements TypeAdapterFactory {
   public TypeAdapter create(Gson var1, TypeToken var2) {
      Class oclass = p_create_2_.getRawType();
      if (!oclass.isEnum()) {
         return null;
      } else {
         final Map map = Maps.newHashMap();

         for(Object t : oclass.getEnumConstants()) {
            map.put(this.getName(t), t);
         }

         return new TypeAdapter() {
            public void write(JsonWriter var1, Object var2) throws IOException {
               if (p_write_2_ == null) {
                  p_write_1_.nullValue();
               } else {
                  p_write_1_.value(EnumTypeAdapterFactory.this.getName(p_write_2_));
               }

            }

            public Object read(JsonReader var1) throws IOException {
               if (p_read_1_.peek() == JsonToken.NULL) {
                  p_read_1_.nextNull();
                  return null;
               } else {
                  return map.get(p_read_1_.nextString());
               }
            }
         };
      }
   }

   private String getName(Object var1) {
      return objectIn instanceof Enum ? ((Enum)objectIn).name().toLowerCase(Locale.US) : objectIn.toString().toLowerCase(Locale.US);
   }
}
