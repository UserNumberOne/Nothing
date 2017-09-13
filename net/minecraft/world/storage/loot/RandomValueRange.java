package net.minecraft.world.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Random;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.math.MathHelper;

public class RandomValueRange {
   private final float min;
   private final float max;

   public RandomValueRange(float var1, float var2) {
      this.min = minIn;
      this.max = maxIn;
   }

   public RandomValueRange(float var1) {
      this.min = value;
      this.max = value;
   }

   public float getMin() {
      return this.min;
   }

   public float getMax() {
      return this.max;
   }

   public int generateInt(Random var1) {
      return MathHelper.getInt(rand, MathHelper.floor(this.min), MathHelper.floor(this.max));
   }

   public float generateFloat(Random var1) {
      return MathHelper.nextFloat(rand, this.min, this.max);
   }

   public boolean isInRange(int var1) {
      return (float)value <= this.max && (float)value >= this.min;
   }

   public static class Serializer implements JsonDeserializer, JsonSerializer {
      public RandomValueRange deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         if (JsonUtils.isNumber(p_deserialize_1_)) {
            return new RandomValueRange(JsonUtils.getFloat(p_deserialize_1_, "value"));
         } else {
            JsonObject jsonobject = JsonUtils.getJsonObject(p_deserialize_1_, "value");
            float f = JsonUtils.getFloat(jsonobject, "min");
            float f1 = JsonUtils.getFloat(jsonobject, "max");
            return new RandomValueRange(f, f1);
         }
      }

      public JsonElement serialize(RandomValueRange var1, Type var2, JsonSerializationContext var3) {
         if (p_serialize_1_.min == p_serialize_1_.max) {
            return new JsonPrimitive(p_serialize_1_.min);
         } else {
            JsonObject jsonobject = new JsonObject();
            jsonobject.addProperty("min", Float.valueOf(p_serialize_1_.min));
            jsonobject.addProperty("max", Float.valueOf(p_serialize_1_.max));
            return jsonobject;
         }
      }
   }
}
