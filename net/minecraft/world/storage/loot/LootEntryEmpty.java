package net.minecraft.world.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Collection;
import java.util.Random;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraftforge.common.ForgeHooks;

public class LootEntryEmpty extends LootEntry {
   public LootEntryEmpty(int var1, int var2, LootCondition[] var3, String var4) {
      super(weightIn, qualityIn, conditionsIn, entryName);
   }

   public void addLoot(Collection var1, Random var2, LootContext var3) {
   }

   protected void serialize(JsonObject var1, JsonSerializationContext var2) {
   }

   public static LootEntryEmpty deserialize(JsonObject var0, JsonDeserializationContext var1, int var2, int var3, LootCondition[] var4) {
      return new LootEntryEmpty(weightIn, qualityIn, conditionsIn, ForgeHooks.readLootEntryName(object, "empty"));
   }
}
