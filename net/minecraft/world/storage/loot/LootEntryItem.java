package net.minecraft.world.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Collection;
import java.util.Random;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.common.ForgeHooks;

public class LootEntryItem extends LootEntry {
   protected final Item item;
   protected final LootFunction[] functions;

   public LootEntryItem(Item var1, int var2, int var3, LootFunction[] var4, LootCondition[] var5, String var6) {
      super(var2, var3, var5, var6);
      this.item = var1;
      this.functions = var4;
   }

   public void addLoot(Collection var1, Random var2, LootContext var3) {
      ItemStack var4 = new ItemStack(this.item);

      for(LootFunction var8 : this.functions) {
         if (LootConditionManager.testAllConditions(var8.getConditions(), var2, var3)) {
            var4 = var8.apply(var4, var2, var3);
         }
      }

      if (var4.stackSize > 0) {
         if (var4.stackSize < this.item.getItemStackLimit(var4)) {
            var1.add(var4);
         } else {
            int var9 = var4.stackSize;

            while(var9 > 0) {
               ItemStack var10 = var4.copy();
               var10.stackSize = Math.min(var4.getMaxStackSize(), var9);
               var9 -= var10.stackSize;
               var1.add(var10);
            }
         }
      }

   }

   protected void serialize(JsonObject var1, JsonSerializationContext var2) {
      if (this.functions != null && this.functions.length > 0) {
         var1.add("functions", var2.serialize(this.functions));
      }

      ResourceLocation var3 = (ResourceLocation)Item.REGISTRY.getNameForObject(this.item);
      if (var3 == null) {
         throw new IllegalArgumentException("Can't serialize unknown item " + this.item);
      } else {
         var1.addProperty("name", var3.toString());
      }
   }

   public static LootEntryItem deserialize(JsonObject var0, JsonDeserializationContext var1, int var2, int var3, LootCondition[] var4) {
      String var5 = ForgeHooks.readLootEntryName(var0, "item");
      Item var6 = JsonUtils.getItem(var0, "name");
      LootFunction[] var7;
      if (var0.has("functions")) {
         var7 = (LootFunction[])JsonUtils.deserializeClass(var0, "functions", var1, LootFunction[].class);
      } else {
         var7 = new LootFunction[0];
      }

      return new LootEntryItem(var6, var2, var3, var7, var4, var5);
   }
}
