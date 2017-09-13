package net.minecraft.client.resources.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.Validate;

@SideOnly(Side.CLIENT)
public class FontMetadataSectionSerializer extends BaseMetadataSectionSerializer {
   public FontMetadataSection deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
      JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
      float[] afloat = new float[256];
      float[] afloat1 = new float[256];
      float[] afloat2 = new float[256];
      float f = 1.0F;
      float f1 = 0.0F;
      float f2 = 0.0F;
      if (jsonobject.has("characters")) {
         if (!jsonobject.get("characters").isJsonObject()) {
            throw new JsonParseException("Invalid font->characters: expected object, was " + jsonobject.get("characters"));
         }

         JsonObject jsonobject1 = jsonobject.getAsJsonObject("characters");
         if (jsonobject1.has("default")) {
            if (!jsonobject1.get("default").isJsonObject()) {
               throw new JsonParseException("Invalid font->characters->default: expected object, was " + jsonobject1.get("default"));
            }

            JsonObject jsonobject2 = jsonobject1.getAsJsonObject("default");
            f = JsonUtils.getFloat(jsonobject2, "width", f);
            Validate.inclusiveBetween(0.0D, 3.4028234663852886E38D, (double)f, "Invalid default width");
            f1 = JsonUtils.getFloat(jsonobject2, "spacing", f1);
            Validate.inclusiveBetween(0.0D, 3.4028234663852886E38D, (double)f1, "Invalid default spacing");
            f2 = JsonUtils.getFloat(jsonobject2, "left", f1);
            Validate.inclusiveBetween(0.0D, 3.4028234663852886E38D, (double)f2, "Invalid default left");
         }

         for(int i = 0; i < 256; ++i) {
            JsonElement jsonelement = jsonobject1.get(Integer.toString(i));
            float f3 = f;
            float f4 = f1;
            float f5 = f2;
            if (jsonelement != null) {
               JsonObject jsonobject3 = JsonUtils.getJsonObject(jsonelement, "characters[" + i + "]");
               f3 = JsonUtils.getFloat(jsonobject3, "width", f);
               Validate.inclusiveBetween(0.0D, 3.4028234663852886E38D, (double)f3, "Invalid width");
               f4 = JsonUtils.getFloat(jsonobject3, "spacing", f1);
               Validate.inclusiveBetween(0.0D, 3.4028234663852886E38D, (double)f4, "Invalid spacing");
               f5 = JsonUtils.getFloat(jsonobject3, "left", f2);
               Validate.inclusiveBetween(0.0D, 3.4028234663852886E38D, (double)f5, "Invalid left");
            }

            afloat[i] = f3;
            afloat1[i] = f4;
            afloat2[i] = f5;
         }
      }

      return new FontMetadataSection(afloat, afloat2, afloat1);
   }

   public String getSectionName() {
      return "font";
   }
}
