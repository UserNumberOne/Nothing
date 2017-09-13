package net.minecraft.world.storage.loot.conditions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;

public class RandomChance implements LootCondition {
   private final float chance;

   public RandomChance(float var1) {
      this.chance = chanceIn;
   }

   public boolean testCondition(Random var1, LootContext var2) {
      return rand.nextFloat() < this.chance;
   }

   public static class Serializer extends LootCondition.Serializer {
      protected Serializer() {
         super(new ResourceLocation("random_chance"), RandomChance.class);
      }

      public void serialize(JsonObject var1, RandomChance var2, JsonSerializationContext var3) {
         json.addProperty("chance", Float.valueOf(value.chance));
      }

      public RandomChance deserialize(JsonObject var1, JsonDeserializationContext var2) {
         return new RandomChance(JsonUtils.getFloat(json, "chance"));
      }
   }
}
