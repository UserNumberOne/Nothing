package net.minecraft.world.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Random;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.storage.loot.conditions.LootCondition;

public abstract class LootEntry {
   protected final int weight;
   protected final int quality;
   protected final LootCondition[] conditions;

   protected LootEntry(int var1, int var2, LootCondition[] var3) {
      this.weight = var1;
      this.quality = var2;
      this.conditions = var3;
   }

   public int getEffectiveWeight(float var1) {
      return Math.max(MathHelper.floor((float)this.weight + (float)this.quality * var1), 0);
   }

   public abstract void addLoot(Collection var1, Random var2, LootContext var3);

   protected abstract void serialize(JsonObject var1, JsonSerializationContext var2);

   public static class Serializer implements JsonDeserializer, JsonSerializer {
      public LootEntry deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         JsonObject var4 = JsonUtils.getJsonObject(var1, "loot item");
         String var5 = JsonUtils.getString(var4, "type");
         int var6 = JsonUtils.getInt(var4, "weight", 1);
         int var7 = JsonUtils.getInt(var4, "quality", 0);
         LootCondition[] var8;
         if (var4.has("conditions")) {
            var8 = (LootCondition[])JsonUtils.deserializeClass(var4, "conditions", var3, LootCondition[].class);
         } else {
            var8 = new LootCondition[0];
         }

         if ("item".equals(var5)) {
            return LootEntryItem.deserialize(var4, var3, var6, var7, var8);
         } else if ("loot_table".equals(var5)) {
            return LootEntryTable.deserialize(var4, var3, var6, var7, var8);
         } else if ("empty".equals(var5)) {
            return LootEntryEmpty.deserialize(var4, var3, var6, var7, var8);
         } else {
            throw new JsonSyntaxException("Unknown loot entry type '" + var5 + "'");
         }
      }

      public JsonElement serialize(LootEntry var1, Type var2, JsonSerializationContext var3) {
         JsonObject var4 = new JsonObject();
         var4.addProperty("weight", Integer.valueOf(var1.weight));
         var4.addProperty("quality", Integer.valueOf(var1.quality));
         if (var1.conditions.length > 0) {
            var4.add("conditions", var3.serialize(var1.conditions));
         }

         if (var1 instanceof LootEntryItem) {
            var4.addProperty("type", "item");
         } else if (var1 instanceof LootEntryTable) {
            var4.addProperty("type", "item");
         } else {
            if (!(var1 instanceof LootEntryEmpty)) {
               throw new IllegalArgumentException("Don't know how to serialize " + var1);
            }

            var4.addProperty("type", "empty");
         }

         var1.serialize(var4, var3);
         return var4;
      }

      // $FF: synthetic method
      public JsonElement serialize(Object var1, Type var2, JsonSerializationContext var3) {
         return this.serialize((LootEntry)var1, var2, var3);
      }

      // $FF: synthetic method
      public Object deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         return this.deserialize(var1, var2, var3);
      }
   }
}
