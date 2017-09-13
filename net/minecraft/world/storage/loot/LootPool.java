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
import java.util.List;
import java.util.Random;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraftforge.common.ForgeHooks;

public class LootPool {
   private final List lootEntries;
   private final List poolConditions;
   private RandomValueRange rolls;
   private RandomValueRange bonusRolls;
   private final String name;
   private boolean isFrozen = false;

   public LootPool(LootEntry[] var1, LootCondition[] var2, RandomValueRange var3, RandomValueRange var4, String var5) {
      this.lootEntries = Lists.newArrayList(var1);
      this.poolConditions = Lists.newArrayList(var2);
      this.rolls = var3;
      this.bonusRolls = var4;
      this.name = var5;
   }

   protected void createLootRoll(Collection var1, Random var2, LootContext var3) {
      ArrayList var4 = Lists.newArrayList();
      int var5 = 0;

      for(LootEntry var7 : this.lootEntries) {
         if (LootConditionManager.testAllConditions(var7.conditions, var2, var3)) {
            int var8 = var7.getEffectiveWeight(var3.getLuck());
            if (var8 > 0) {
               var4.add(var7);
               var5 += var8;
            }
         }
      }

      if (var5 != 0 && !var4.isEmpty()) {
         int var9 = var2.nextInt(var5);

         for(LootEntry var11 : var4) {
            var9 -= var11.getEffectiveWeight(var3.getLuck());
            if (var9 < 0) {
               var11.addLoot(var1, var2, var3);
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

   public void freeze() {
      this.isFrozen = true;
   }

   public boolean isFrozen() {
      return this.isFrozen;
   }

   private void checkFrozen() {
      if (this.isFrozen()) {
         throw new RuntimeException("Attempted to modify LootPool after being frozen!");
      }
   }

   public String getName() {
      return this.name;
   }

   public RandomValueRange getRolls() {
      return this.rolls;
   }

   public RandomValueRange getBonusRolls() {
      return this.bonusRolls;
   }

   public void setRolls(RandomValueRange var1) {
      this.checkFrozen();
      this.rolls = var1;
   }

   public void setBonusRolls(RandomValueRange var1) {
      this.checkFrozen();
      this.bonusRolls = var1;
   }

   public LootEntry getEntry(String var1) {
      for(LootEntry var3 : this.lootEntries) {
         if (var1.equals(var3.getEntryName())) {
            return var3;
         }
      }

      return null;
   }

   public LootEntry removeEntry(String var1) {
      this.checkFrozen();

      for(LootEntry var3 : this.lootEntries) {
         if (var1.equals(var3.getEntryName())) {
            this.lootEntries.remove(var3);
            return var3;
         }
      }

      return null;
   }

   public void addEntry(LootEntry var1) {
      this.checkFrozen();

      for(LootEntry var3 : this.lootEntries) {
         if (var3 == var1 || var3.getEntryName().equals(var1.getEntryName())) {
            throw new RuntimeException("Attempted to add a duplicate entry to pool: " + var3.getEntryName());
         }
      }

      this.lootEntries.add(var1);
   }

   public static class Serializer implements JsonDeserializer, JsonSerializer {
      public LootPool deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         JsonObject var4 = JsonUtils.getJsonObject(var1, "loot pool");
         String var5 = ForgeHooks.readPoolName(var4);
         LootEntry[] var6 = (LootEntry[])JsonUtils.deserializeClass(var4, "entries", var3, LootEntry[].class);
         LootCondition[] var7 = (LootCondition[])JsonUtils.deserializeClass(var4, "conditions", new LootCondition[0], var3, LootCondition[].class);
         RandomValueRange var8 = (RandomValueRange)JsonUtils.deserializeClass(var4, "rolls", var3, RandomValueRange.class);
         RandomValueRange var9 = (RandomValueRange)JsonUtils.deserializeClass(var4, "bonus_rolls", new RandomValueRange(0.0F, 0.0F), var3, RandomValueRange.class);
         return new LootPool(var6, var7, var8, var9, var5);
      }

      public JsonElement serialize(LootPool var1, Type var2, JsonSerializationContext var3) {
         JsonObject var4 = new JsonObject();
         if (var1.name != null && !var1.name.startsWith("custom#")) {
            var4.add("name", var3.serialize(var1.name));
         }

         var4.add("entries", var3.serialize(var1.lootEntries));
         var4.add("rolls", var3.serialize(var1.rolls));
         if (var1.bonusRolls.getMin() != 0.0F && var1.bonusRolls.getMax() != 0.0F) {
            var4.add("bonus_rolls", var3.serialize(var1.bonusRolls));
         }

         if (!var1.poolConditions.isEmpty()) {
            var4.add("conditions", var3.serialize(var1.poolConditions));
         }

         return var4;
      }
   }
}
