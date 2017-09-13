package net.minecraft.client.resources.data;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.resources.Language;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LanguageMetadataSectionSerializer extends BaseMetadataSectionSerializer {
   public LanguageMetadataSection deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
      JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
      Set set = Sets.newHashSet();

      for(Entry entry : jsonobject.entrySet()) {
         String s = (String)entry.getKey();
         JsonObject jsonobject1 = JsonUtils.getJsonObject((JsonElement)entry.getValue(), "language");
         String s1 = JsonUtils.getString(jsonobject1, "region");
         String s2 = JsonUtils.getString(jsonobject1, "name");
         boolean flag = JsonUtils.getBoolean(jsonobject1, "bidirectional", false);
         if (s1.isEmpty()) {
            throw new JsonParseException("Invalid language->'" + s + "'->region: empty value");
         }

         if (s2.isEmpty()) {
            throw new JsonParseException("Invalid language->'" + s + "'->name: empty value");
         }

         if (!set.add(new Language(s, s1, s2, flag))) {
            throw new JsonParseException("Duplicate language->'" + s + "' defined");
         }
      }

      return new LanguageMetadataSection(set);
   }

   public String getSectionName() {
      return "language";
   }
}
