package net.minecraft.client.resources.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PackMetadataSectionSerializer extends BaseMetadataSectionSerializer implements JsonSerializer {
   public PackMetadataSection deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
      JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
      ITextComponent itextcomponent = (ITextComponent)p_deserialize_3_.deserialize(jsonobject.get("description"), ITextComponent.class);
      if (itextcomponent == null) {
         throw new JsonParseException("Invalid/missing description!");
      } else {
         int i = JsonUtils.getInt(jsonobject, "pack_format");
         return new PackMetadataSection(itextcomponent, i);
      }
   }

   public JsonElement serialize(PackMetadataSection var1, Type var2, JsonSerializationContext var3) {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("pack_format", Integer.valueOf(p_serialize_1_.getPackFormat()));
      jsonobject.add("description", p_serialize_3_.serialize(p_serialize_1_.getPackDescription()));
      return jsonobject;
   }

   public String getSectionName() {
      return "pack";
   }
}
