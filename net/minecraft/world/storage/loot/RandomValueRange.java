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
      this.min = var1;
      this.max = var2;
   }

   public RandomValueRange(float var1) {
      this.min = var1;
      this.max = var1;
   }

   public float getMin() {
      return this.min;
   }

   public float getMax() {
      return this.max;
   }

   public int generateInt(Random var1) {
      return MathHelper.getInt(var1, MathHelper.floor(this.min), MathHelper.floor(this.max));
   }

   public float generateFloat(Random var1) {
      return MathHelper.nextFloat(var1, this.min, this.max);
   }

   public boolean isInRange(int var1) {
      return (float)var1 <= this.max && (float)var1 >= this.min;
   }

   public static class Serializer implements JsonDeserializer, JsonSerializer {
      public RandomValueRange deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         if (JsonUtils.isNumber(var1)) {
            return new RandomValueRange(JsonUtils.getFloat(var1, "value"));
         } else {
            JsonObject var4 = JsonUtils.getJsonObject(var1, "value");
            float var5 = JsonUtils.getFloat(var4, "min");
            float var6 = JsonUtils.getFloat(var4, "max");
            return new RandomValueRange(var5, var6);
         }
      }

      public JsonElement serialize(RandomValueRange var1, Type var2, JsonSerializationContext var3) {
         if (var1.min == var1.max) {
            return new JsonPrimitive(var1.min);
         } else {
            JsonObject var4 = new JsonObject();
            var4.addProperty("min", Float.valueOf(var1.min));
            var4.addProperty("max", Float.valueOf(var1.max));
            return var4;
         }
      }
   }
}
