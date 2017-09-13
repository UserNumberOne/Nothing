package net.minecraft.world.storage.loot.conditions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;

public class RandomChanceWithLooting implements LootCondition {
   private final float chance;
   private final float lootingMultiplier;

   public RandomChanceWithLooting(float var1, float var2) {
      this.chance = var1;
      this.lootingMultiplier = var2;
   }

   public boolean testCondition(Random var1, LootContext var2) {
      int var3 = 0;
      if (var2.getKiller() instanceof EntityLivingBase) {
         var3 = EnchantmentHelper.getLootingModifier((EntityLivingBase)var2.getKiller());
      }

      return var1.nextFloat() < this.chance + (float)var3 * this.lootingMultiplier;
   }

   public static class Serializer extends LootCondition.Serializer {
      protected Serializer() {
         super(new ResourceLocation("random_chance_with_looting"), RandomChanceWithLooting.class);
      }

      public void serialize(JsonObject var1, RandomChanceWithLooting var2, JsonSerializationContext var3) {
         var1.addProperty("chance", Float.valueOf(var2.chance));
         var1.addProperty("looting_multiplier", Float.valueOf(var2.lootingMultiplier));
      }

      public RandomChanceWithLooting deserialize(JsonObject var1, JsonDeserializationContext var2) {
         return new RandomChanceWithLooting(JsonUtils.getFloat(var1, "chance"), JsonUtils.getFloat(var1, "looting_multiplier"));
      }

      // $FF: synthetic method
      public LootCondition deserialize(JsonObject var1, JsonDeserializationContext var2) {
         return this.deserialize(var1, var2);
      }
   }
}
