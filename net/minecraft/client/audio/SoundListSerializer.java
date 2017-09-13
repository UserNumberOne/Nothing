package net.minecraft.client.audio;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.List;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.Validate;

@SideOnly(Side.CLIENT)
public class SoundListSerializer implements JsonDeserializer {
   public SoundList deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
      JsonObject jsonobject = JsonUtils.getJsonObject(p_deserialize_1_, "entry");
      boolean flag = JsonUtils.getBoolean(jsonobject, "replace", false);
      String s = JsonUtils.getString(jsonobject, "subtitle", (String)null);
      List list = this.deserializeSounds(jsonobject);
      return new SoundList(list, flag, s);
   }

   private List deserializeSounds(JsonObject var1) {
      List list = Lists.newArrayList();
      if (object.has("sounds")) {
         JsonArray jsonarray = JsonUtils.getJsonArray(object, "sounds");

         for(int i = 0; i < jsonarray.size(); ++i) {
            JsonElement jsonelement = jsonarray.get(i);
            if (JsonUtils.isString(jsonelement)) {
               String s = JsonUtils.getString(jsonelement, "sound");
               list.add(new Sound(s, 1.0F, 1.0F, 1, Sound.Type.FILE, false));
            } else {
               list.add(this.deserializeSound(JsonUtils.getJsonObject(jsonelement, "sound")));
            }
         }
      }

      return list;
   }

   private Sound deserializeSound(JsonObject var1) {
      String s = JsonUtils.getString(object, "name");
      Sound.Type sound$type = this.deserializeType(object, Sound.Type.FILE);
      float f = JsonUtils.getFloat(object, "volume", 1.0F);
      Validate.isTrue(f > 0.0F, "Invalid volume", new Object[0]);
      float f1 = JsonUtils.getFloat(object, "pitch", 1.0F);
      Validate.isTrue(f1 > 0.0F, "Invalid pitch", new Object[0]);
      int i = JsonUtils.getInt(object, "weight", 1);
      Validate.isTrue(i > 0, "Invalid weight", new Object[0]);
      boolean flag = JsonUtils.getBoolean(object, "stream", false);
      return new Sound(s, f, f1, i, sound$type, flag);
   }

   private Sound.Type deserializeType(JsonObject var1, Sound.Type var2) {
      Sound.Type sound$type = defaultValue;
      if (object.has("type")) {
         sound$type = Sound.Type.getByName(JsonUtils.getString(object, "type"));
         Validate.notNull(sound$type, "Invalid type", new Object[0]);
      }

      return sound$type;
   }
}
