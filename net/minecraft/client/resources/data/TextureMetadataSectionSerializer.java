package net.minecraft.client.resources.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TextureMetadataSectionSerializer extends BaseMetadataSectionSerializer {
   public TextureMetadataSection deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
      JsonObject var4 = var1.getAsJsonObject();
      boolean var5 = JsonUtils.getBoolean(var4, "blur", false);
      boolean var6 = JsonUtils.getBoolean(var4, "clamp", false);
      return new TextureMetadataSection(var5, var6);
   }

   public String getSectionName() {
      return "texture";
   }
}
