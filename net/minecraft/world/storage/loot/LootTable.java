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
      this.pools = Lists.newArrayList(poolsIn);
   }

   public List generateLootForPools(Random var1, LootContext var2) {
      List list = Lists.newArrayList();
      if (context.addLootTable(this)) {
         for(LootPool lootpool : this.pools) {
            lootpool.generateLoot(list, rand, context);
         }

         context.removeLootTable(this);
      } else {
         LOGGER.warn("Detected infinite loop in loot tables");
      }

      return list;
   }

   public void fillInventory(IInventory var1, Random var2, LootContext var3) {
      List list = this.generateLootForPools(rand, context);
      List list1 = this.getEmptySlotsRandomized(inventory, rand);
      this.shuffleItems(list, list1.size(), rand);

      for(ItemStack itemstack : list) {
         if (list1.isEmpty()) {
            LOGGER.warn("Tried to over-fill a container");
            return;
         }

         if (itemstack == null) {
            inventory.setInventorySlotContents(((Integer)list1.remove(list1.size() - 1)).intValue(), (ItemStack)null);
         } else {
            inventory.setInventorySlotContents(((Integer)list1.remove(list1.size() - 1)).intValue(), itemstack);
         }
      }

   }

   private void shuffleItems(List var1, int var2, Random var3) {
      List list = Lists.newArrayList();
      Iterator iterator = stacks.iterator();

      while(iterator.hasNext()) {
         ItemStack itemstack = (ItemStack)iterator.next();
         if (itemstack.stackSize <= 0) {
            iterator.remove();
         } else if (itemstack.stackSize > 1) {
            list.add(itemstack);
            iterator.remove();
         }
      }

      p_186463_2_ = p_186463_2_ - stacks.size();

      while(p_186463_2_ > 0 && ((List)list).size() > 0) {
         ItemStack itemstack2 = (ItemStack)list.remove(MathHelper.getInt(rand, 0, list.size() - 1));
         int i = MathHelper.getInt(rand, 1, itemstack2.stackSize / 2);
         itemstack2.stackSize -= i;
         ItemStack itemstack1 = itemstack2.copy();
         itemstack1.stackSize = i;
         if (itemstack2.stackSize > 1 && rand.nextBoolean()) {
            list.add(itemstack2);
         } else {
            stacks.add(itemstack2);
         }

         if (itemstack1.stackSize > 1 && rand.nextBoolean()) {
            list.add(itemstack1);
         } else {
            stacks.add(itemstack1);
         }
      }

      stacks.addAll(list);
      Collections.shuffle(stacks, rand);
   }

   private List getEmptySlotsRandomized(IInventory var1, Random var2) {
      List list = Lists.newArrayList();

      for(int i = 0; i < inventory.getSizeInventory(); ++i) {
         if (inventory.getStackInSlot(i) == null) {
            list.add(Integer.valueOf(i));
         }
      }

      Collections.shuffle(list, rand);
      return list;
   }

   public void freeze() {
      this.isFrozen = true;

      for(LootPool pool : this.pools) {
         pool.freeze();
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
      for(LootPool pool : this.pools) {
         if (name.equals(pool.getName())) {
            return pool;
         }
      }

      return null;
   }

   public LootPool removePool(String var1) {
      this.checkFrozen();

      for(LootPool pool : this.pools) {
         if (name.equals(pool.getName())) {
            this.pools.remove(pool);
            return pool;
         }
      }

      return null;
   }

   public void addPool(LootPool var1) {
      this.checkFrozen();

      for(LootPool p : this.pools) {
         if (p == pool || p.getName().equals(pool.getName())) {
            throw new RuntimeException("Attempted to add a duplicate pool to loot table: " + pool.getName());
         }
      }

      this.pools.add(pool);
   }

   public static class Serializer implements JsonDeserializer, JsonSerializer {
      public LootTable deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         JsonObject jsonobject = JsonUtils.getJsonObject(p_deserialize_1_, "loot table");
         LootPool[] alootpool = (LootPool[])JsonUtils.deserializeClass(jsonobject, "pools", new LootPool[0], p_deserialize_3_, LootPool[].class);
         return new LootTable(alootpool);
      }

      public JsonElement serialize(LootTable var1, Type var2, JsonSerializationContext var3) {
         JsonObject jsonobject = new JsonObject();
         jsonobject.add("pools", p_serialize_3_.serialize(p_serialize_1_.pools));
         return jsonobject;
      }
   }
}
