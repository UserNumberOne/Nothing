package net.minecraft.world.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;

public class LootingEnchantBonus extends LootFunction {
   private final RandomValueRange count;
   private final int limit;

   public LootingEnchantBonus(LootCondition[] var1, RandomValueRange var2, int var3) {
      super(var1);
      this.count = var2;
      this.limit = var3;
   }

   public ItemStack apply(ItemStack var1, Random var2, LootContext var3) {
      Entity var4 = var3.getKiller();
      if (var4 instanceof EntityLivingBase) {
         int var5 = var3.getLootingModifier();
         if (var5 == 0) {
            return var1;
         }

         float var6 = (float)var5 * this.count.generateFloat(var2);
         var1.stackSize += Math.round(var6);
         if (this.limit != 0 && var1.stackSize > this.limit) {
            var1.stackSize = this.limit;
         }
      }

      return var1;
   }

   public static class Serializer extends LootFunction.Serializer {
      protected Serializer() {
         super(new ResourceLocation("looting_enchant"), LootingEnchantBonus.class);
      }

      public void serialize(JsonObject var1, LootingEnchantBonus var2, JsonSerializationContext var3) {
         var1.add("count", var3.serialize(var2.count));
         if (var2.limit > 0) {
            var1.add("limit", var3.serialize(Integer.valueOf(var2.limit)));
         }

      }

      public LootingEnchantBonus deserialize(JsonObject var1, JsonDeserializationContext var2, LootCondition[] var3) {
         int var4 = JsonUtils.getInt(var1, "limit", 0);
         return new LootingEnchantBonus(var3, (RandomValueRange)JsonUtils.deserializeClass(var1, "count", var2, RandomValueRange.class), var4);
      }
   }
}
