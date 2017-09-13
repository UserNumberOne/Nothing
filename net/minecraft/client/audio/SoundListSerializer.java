package net.minecraft.client.audio;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.Validate;

@SideOnly(Side.CLIENT)
public class SoundListSerializer implements JsonDeserializer {
   public SoundList deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
      JsonObject var4 = JsonUtils.getJsonObject(var1, "entry");
      boolean var5 = JsonUtils.getBoolean(var4, "replace", false);
      String var6 = JsonUtils.getString(var4, "subtitle", (String)null);
      List var7 = this.deserializeSounds(var4);
      return new SoundList(var7, var5, var6);
   }

   private List deserializeSounds(JsonObject var1) {
      ArrayList var2 = Lists.newArrayList();
      if (var1.has("sounds")) {
         JsonArray var3 = JsonUtils.getJsonArray(var1, "sounds");

         for(int var4 = 0; var4 < var3.size(); ++var4) {
            JsonElement var5 = var3.get(var4);
            if (JsonUtils.isString(var5)) {
               String var6 = JsonUtils.getString(var5, "sound");
               var2.add(new Sound(var6, 1.0F, 1.0F, 1, Sound.Type.FILE, false));
            } else {
               var2.add(this.deserializeSound(JsonUtils.getJsonObject(var5, "sound")));
            }
         }
      }

      return var2;
   }

   private Sound deserializeSound(JsonObject var1) {
      String var2 = JsonUtils.getString(var1, "name");
      Sound.Type var3 = this.deserializeType(var1, Sound.Type.FILE);
      float var4 = JsonUtils.getFloat(var1, "volume", 1.0F);
      Validate.isTrue(var4 > 0.0F, "Invalid volume", new Object[0]);
      float var5 = JsonUtils.getFloat(var1, "pitch", 1.0F);
      Validate.isTrue(var5 > 0.0F, "Invalid pitch", new Object[0]);
      int var6 = JsonUtils.getInt(var1, "weight", 1);
      Validate.isTrue(var6 > 0, "Invalid weight", new Object[0]);
      boolean var7 = JsonUtils.getBoolean(var1, "stream", false);
      return new Sound(var2, var4, var5, var6, var3, var7);
   }

   private Sound.Type deserializeType(JsonObject var1, Sound.Type var2) {
      Sound.Type var3 = var2;
      if (var1.has("type")) {
         var3 = Sound.Type.getByName(JsonUtils.getString(var1, "type"));
         Validate.notNull(var3, "Invalid type", new Object[0]);
      }

      return var3;
   }
}
