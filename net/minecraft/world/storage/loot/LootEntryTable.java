package net.minecraft.world.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraftforge.common.ForgeHooks;

public class LootEntryTable extends LootEntry {
   protected final ResourceLocation table;

   public LootEntryTable(ResourceLocation var1, int var2, int var3, LootCondition[] var4, String var5) {
      super(var2, var3, var4, var5);
      this.table = var1;
   }

   public void addLoot(Collection var1, Random var2, LootContext var3) {
      LootTable var4 = var3.getLootTableManager().getLootTableFromLocation(this.table);
      List var5 = var4.generateLootForPools(var2, var3);
      var1.addAll(var5);
   }

   protected void serialize(JsonObject var1, JsonSerializationContext var2) {
      var1.addProperty("name", this.table.toString());
   }

   public static LootEntryTable deserialize(JsonObject var0, JsonDeserializationContext var1, int var2, int var3, LootCondition[] var4) {
      String var5 = ForgeHooks.readLootEntryName(var0, "loot_table");
      ResourceLocation var6 = new ResourceLocation(JsonUtils.getString(var0, "name"));
      return new LootEntryTable(var6, var2, var3, var4, var5);
   }
}
