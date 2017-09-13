package net.minecraft.world.storage.loot;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import org.apache.commons.lang3.ArrayUtils;

public class LootPool {
   private final LootEntry[] lootEntries;
   private final LootCondition[] poolConditions;
   private final RandomValueRange rolls;
   private final RandomValueRange bonusRolls;

   public LootPool(LootEntry[] var1, LootCondition[] var2, RandomValueRange var3, RandomValueRange var4) {
      this.lootEntries = var1;
      this.poolConditions = var2;
      this.rolls = var3;
      this.bonusRolls = var4;
   }

   protected void createLootRoll(Collection var1, Random var2, LootContext var3) {
      ArrayList var4 = Lists.newArrayList();
      int var5 = 0;

      for(LootEntry var9 : this.lootEntries) {
         if (LootConditionManager.testAllConditions(var9.conditions, var2, var3)) {
            int var10 = var9.getEffectiveWeight(var3.getLuck());
            if (var10 > 0) {
               var4.add(var9);
               var5 += var10;
            }
         }
      }

      if (var5 != 0 && !var4.isEmpty()) {
         int var11 = var2.nextInt(var5);

         for(LootEntry var13 : var4) {
            var11 -= var13.getEffectiveWeight(var3.getLuck());
            if (var11 < 0) {
               var13.addLoot(var1, var2, var3);
               return;
            }
         }

      }
   }

   public void generateLoot(Collection var1, Random var2, LootContext var3) {
      if (LootConditionManager.testAllConditions(this.poolConditions, var2, var3)) {
         int var4 = this.rolls.generateInt(var2) + MathHelper.floor(this.bonusRolls.generateFloat(var2) * var3.getLuck());

         for(int var5 = 0; var5 < var4; ++var5) {
            this.createLootRoll(var1, var2, var3);
         }

      }
   }

   public static class Serializer implements JsonDeserializer, JsonSerializer {
      public LootPool deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         JsonObject var4 = JsonUtils.getJsonObject(var1, "loot pool");
         LootEntry[] var5 = (LootEntry[])JsonUtils.deserializeClass(var4, "entries", var3, LootEntry[].class);
         LootCondition[] var6 = (LootCondition[])JsonUtils.deserializeClass(var4, "conditions", new LootCondition[0], var3, LootCondition[].class);
         RandomValueRange var7 = (RandomValueRange)JsonUtils.deserializeClass(var4, "rolls", var3, RandomValueRange.class);
         RandomValueRange var8 = (RandomValueRange)JsonUtils.deserializeClass(var4, "bonus_rolls", new RandomValueRange(0.0F, 0.0F), var3, RandomValueRange.class);
         return new LootPool(var5, var6, var7, var8);
      }

      public JsonElement serialize(LootPool var1, Type var2, JsonSerializationContext var3) {
         JsonObject var4 = new JsonObject();
         var4.add("entries", var3.serialize(var1.lootEntries));
         var4.add("rolls", var3.serialize(var1.rolls));
         if (var1.bonusRolls.getMin() != 0.0F && var1.bonusRolls.getMax() != 0.0F) {
            var4.add("bonus_rolls", var3.serialize(var1.bonusRolls));
         }

         if (!ArrayUtils.isEmpty(var1.poolConditions)) {
            var4.add("conditions", var3.serialize(var1.poolConditions));
         }

         return var4;
      }

      // $FF: synthetic method
      public JsonElement serialize(Object var1, Type var2, JsonSerializationContext var3) {
         return this.serialize((LootPool)var1, var2, var3);
      }

      // $FF: synthetic method
      public Object deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         return this.deserialize(var1, var2, var3);
      }
   }
}
