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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LootTable {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final LootTable EMPTY_LOOT_TABLE = new LootTable(new LootPool[0]);
   private final LootPool[] pools;

   public LootTable(LootPool[] var1) {
      this.pools = var1;
   }

   public List generateLootForPools(Random var1, LootContext var2) {
      ArrayList var3 = Lists.newArrayList();
      if (var2.addLootTable(this)) {
         for(LootPool var7 : this.pools) {
            var7.generateLoot(var3, var1, var2);
         }

         var2.removeLootTable(this);
      } else {
         LOGGER.warn("Detected infinite loop in loot tables");
      }

      return var3;
   }

   public void fillInventory(IInventory var1, Random var2, LootContext var3) {
      List var4 = this.generateLootForPools(var2, var3);
      List var5 = this.getEmptySlotsRandomized(var1, var2);
      this.shuffleItems(var4, var5.size(), var2);

      for(ItemStack var7 : var4) {
         if (var5.isEmpty()) {
            LOGGER.warn("Tried to over-fill a container");
            return;
         }

         if (var7 == null) {
            var1.setInventorySlotContents(((Integer)var5.remove(var5.size() - 1)).intValue(), (ItemStack)null);
         } else {
            var1.setInventorySlotContents(((Integer)var5.remove(var5.size() - 1)).intValue(), var7);
         }
      }

   }

   private void shuffleItems(List var1, int var2, Random var3) {
      ArrayList var4 = Lists.newArrayList();
      Iterator var5 = var1.iterator();

      while(var5.hasNext()) {
         ItemStack var6 = (ItemStack)var5.next();
         if (var6.stackSize <= 0) {
            var5.remove();
         } else if (var6.stackSize > 1) {
            var4.add(var6);
            var5.remove();
         }
      }

      var2 = var2 - var1.size();

      while(var2 > 0 && var4.size() > 0) {
         ItemStack var9 = (ItemStack)var4.remove(MathHelper.getInt(var3, 0, var4.size() - 1));
         int var10 = MathHelper.getInt(var3, 1, var9.stackSize / 2);
         var9.stackSize -= var10;
         ItemStack var7 = var9.copy();
         var7.stackSize = var10;
         if (var9.stackSize > 1 && var3.nextBoolean()) {
            var4.add(var9);
         } else {
            var1.add(var9);
         }

         if (var7.stackSize > 1 && var3.nextBoolean()) {
            var4.add(var7);
         } else {
            var1.add(var7);
         }
      }

      var1.addAll(var4);
      Collections.shuffle(var1, var3);
   }

   private List getEmptySlotsRandomized(IInventory var1, Random var2) {
      ArrayList var3 = Lists.newArrayList();

      for(int var4 = 0; var4 < var1.getSizeInventory(); ++var4) {
         if (var1.getStackInSlot(var4) == null) {
            var3.add(Integer.valueOf(var4));
         }
      }

      Collections.shuffle(var3, var2);
      return var3;
   }

   public static class Serializer implements JsonDeserializer, JsonSerializer {
      public LootTable deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         JsonObject var4 = JsonUtils.getJsonObject(var1, "loot table");
         LootPool[] var5 = (LootPool[])JsonUtils.deserializeClass(var4, "pools", new LootPool[0], var3, LootPool[].class);
         return new LootTable(var5);
      }

      public JsonElement serialize(LootTable var1, Type var2, JsonSerializationContext var3) {
         JsonObject var4 = new JsonObject();
         var4.add("pools", var3.serialize(var1.pools));
         return var4;
      }

      // $FF: synthetic method
      public JsonElement serialize(Object var1, Type var2, JsonSerializationContext var3) {
         return this.serialize((LootTable)var1, var2, var3);
      }

      // $FF: synthetic method
      public Object deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         return this.deserialize(var1, var2, var3);
      }
   }
}
