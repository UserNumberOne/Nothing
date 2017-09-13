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
   private final List pools;
   private boolean isFrozen = false;

   public LootTable(LootPool[] var1) {
      this.pools = Lists.newArrayList(var1);
   }

   public List generateLootForPools(Random var1, LootContext var2) {
      ArrayList var3 = Lists.newArrayList();
      if (var2.addLootTable(this)) {
         for(LootPool var5 : this.pools) {
            var5.generateLoot(var3, var1, var2);
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
         ItemStack var10 = (ItemStack)var4.remove(MathHelper.getInt(var3, 0, var4.size() - 1));
         int var7 = MathHelper.getInt(var3, 1, var10.stackSize / 2);
         var10.stackSize -= var7;
         ItemStack var8 = var10.copy();
         var8.stackSize = var7;
         if (var10.stackSize > 1 && var3.nextBoolean()) {
            var4.add(var10);
         } else {
            var1.add(var10);
         }

         if (var8.stackSize > 1 && var3.nextBoolean()) {
            var4.add(var8);
         } else {
            var1.add(var8);
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

   public void freeze() {
      this.isFrozen = true;

      for(LootPool var2 : this.pools) {
         var2.freeze();
      }

   }

   public boolean isFrozen() {
      return this.isFrozen;
   }

   private void checkFrozen() {
      if (this.isFrozen()) {
         throw new RuntimeException("Attempted to modify LootTable after being finalized!");
      }
   }

   public LootPool getPool(String var1) {
      for(LootPool var3 : this.pools) {
         if (var1.equals(var3.getName())) {
            return var3;
         }
      }

      return null;
   }

   public LootPool removePool(String var1) {
      this.checkFrozen();

      for(LootPool var3 : this.pools) {
         if (var1.equals(var3.getName())) {
            this.pools.remove(var3);
            return var3;
         }
      }

      return null;
   }

   public void addPool(LootPool var1) {
      this.checkFrozen();

      for(LootPool var3 : this.pools) {
         if (var3 == var1 || var3.getName().equals(var1.getName())) {
            throw new RuntimeException("Attempted to add a duplicate pool to loot table: " + var1.getName());
         }
      }

      this.pools.add(var1);
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
   }
}
