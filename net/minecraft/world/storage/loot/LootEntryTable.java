package net.minecraft.world.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Collection;
import java.util.Random;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraftforge.common.ForgeHooks;

public class LootEntryTable extends LootEntry {
   protected final ResourceLocation table;

   public LootEntryTable(ResourceLocation var1, int var2, int var3, LootCondition[] var4, String var5) {
      super(weightIn, qualityIn, conditionsIn, entryName);
      this.table = tableIn;
   }

   public void addLoot(Collection var1, Random var2, LootContext var3) {
      LootTable loottable = context.getLootTableManager().getLootTableFromLocation(this.table);
      Collection collection = loottable.generateLootForPools(rand, context);
      stacks.addAll(collection);
   }

   protected void serialize(JsonObject var1, JsonSerializationContext var2) {
      json.addProperty("name", this.table.toString());
   }

   public static LootEntryTable deserialize(JsonObject var0, JsonDeserializationContext var1, int var2, int var3, LootCondition[] var4) {
      String name = ForgeHooks.readLootEntryName(object, "loot_table");
      ResourceLocation resourcelocation = new ResourceLocation(JsonUtils.getString(object, "name"));
      return new LootEntryTable(resourcelocation, weightIn, qualityIn, conditionsIn, name);
   }
}
