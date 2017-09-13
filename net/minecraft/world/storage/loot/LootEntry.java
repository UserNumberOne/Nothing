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
import net.minecraftforge.common.ForgeHooks;

public abstract class LootEntry {
   protected final String entryName;
   protected final int weight;
   protected final int quality;
   protected final LootCondition[] conditions;

   protected LootEntry(int var1, int var2, LootCondition[] var3, String var4) {
      this.weight = weightIn;
      this.quality = qualityIn;
      this.conditions = conditionsIn;
      this.entryName = entryName;
   }

   public int getEffectiveWeight(float var1) {
      return Math.max(MathHelper.floor((float)this.weight + (float)this.quality * luck), 0);
   }

   public String getEntryName() {
      return this.entryName;
   }

   public abstract void addLoot(Collection var1, Random var2, LootContext var3);

   protected abstract void serialize(JsonObject var1, JsonSerializationContext var2);

   public static class Serializer implements JsonDeserializer, JsonSerializer {
      public LootEntry deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         JsonObject jsonobject = JsonUtils.getJsonObject(p_deserialize_1_, "loot item");
         String s = JsonUtils.getString(jsonobject, "type");
         int i = JsonUtils.getInt(jsonobject, "weight", 1);
         int j = JsonUtils.getInt(jsonobject, "quality", 0);
         LootCondition[] alootcondition;
         if (jsonobject.has("conditions")) {
            alootcondition = (LootCondition[])JsonUtils.deserializeClass(jsonobject, "conditions", p_deserialize_3_, LootCondition[].class);
         } else {
            alootcondition = new LootCondition[0];
         }

         LootEntry ret = ForgeHooks.deserializeJsonLootEntry(s, jsonobject, i, j, alootcondition);
         if (ret != null) {
            return ret;
         } else if ("item".equals(s)) {
            return LootEntryItem.deserialize(jsonobject, p_deserialize_3_, i, j, alootcondition);
         } else if ("loot_table".equals(s)) {
            return LootEntryTable.deserialize(jsonobject, p_deserialize_3_, i, j, alootcondition);
         } else if ("empty".equals(s)) {
            return LootEntryEmpty.deserialize(jsonobject, p_deserialize_3_, i, j, alootcondition);
         } else {
            throw new JsonSyntaxException("Unknown loot entry type '" + s + "'");
         }
      }

      public JsonElement serialize(LootEntry var1, Type var2, JsonSerializationContext var3) {
         JsonObject jsonobject = new JsonObject();
         if (p_serialize_1_.entryName != null && !p_serialize_1_.entryName.startsWith("custom#")) {
            jsonobject.addProperty("entryName", p_serialize_1_.entryName);
         }

         jsonobject.addProperty("weight", Integer.valueOf(p_serialize_1_.weight));
         jsonobject.addProperty("quality", Integer.valueOf(p_serialize_1_.quality));
         if (p_serialize_1_.conditions.length > 0) {
            jsonobject.add("conditions", p_serialize_3_.serialize(p_serialize_1_.conditions));
         }

         String type = ForgeHooks.getLootEntryType(p_serialize_1_);
         if (type != null) {
            jsonobject.addProperty("type", type);
         } else if (p_serialize_1_ instanceof LootEntryItem) {
            jsonobject.addProperty("type", "item");
         } else if (p_serialize_1_ instanceof LootEntryTable) {
            jsonobject.addProperty("type", "item");
         } else {
            if (!(p_serialize_1_ instanceof LootEntryEmpty)) {
               throw new IllegalArgumentException("Don't know how to serialize " + p_serialize_1_);
            }

            jsonobject.addProperty("type", "empty");
         }

         p_serialize_1_.serialize(jsonobject, p_serialize_3_);
         return jsonobject;
      }
   }
}
