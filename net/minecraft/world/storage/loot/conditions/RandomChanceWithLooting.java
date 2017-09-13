package net.minecraft.world.storage.loot.conditions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;

public class RandomChanceWithLooting implements LootCondition {
   private final float chance;
   private final float lootingMultiplier;

   public RandomChanceWithLooting(float var1, float var2) {
      this.chance = chanceIn;
      this.lootingMultiplier = lootingMultiplierIn;
   }

   public boolean testCondition(Random var1, LootContext var2) {
      int i = context.getLootingModifier();
      return rand.nextFloat() < this.chance + (float)i * this.lootingMultiplier;
   }

   public static class Serializer extends LootCondition.Serializer {
      protected Serializer() {
         super(new ResourceLocation("random_chance_with_looting"), RandomChanceWithLooting.class);
      }

      public void serialize(JsonObject var1, RandomChanceWithLooting var2, JsonSerializationContext var3) {
         json.addProperty("chance", Float.valueOf(value.chance));
         json.addProperty("looting_multiplier", Float.valueOf(value.lootingMultiplier));
      }

      public RandomChanceWithLooting deserialize(JsonObject var1, JsonDeserializationContext var2) {
         return new RandomChanceWithLooting(JsonUtils.getFloat(json, "chance"), JsonUtils.getFloat(json, "looting_multiplier"));
      }
   }
}
