package net.minecraft.world.storage.loot.functions;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EnchantRandomly extends LootFunction {
   private static final Logger LOGGER = LogManager.getLogger();
   @Nullable
   private final List enchantments;

   public EnchantRandomly(LootCondition[] var1, @Nullable List var2) {
      super(var1);
      this.enchantments = var2;
   }

   public ItemStack apply(ItemStack var1, Random var2, LootContext var3) {
      Enchantment var7;
      if (this.enchantments != null && !this.enchantments.isEmpty()) {
         var7 = (Enchantment)this.enchantments.get(var2.nextInt(this.enchantments.size()));
      } else {
         ArrayList var4 = Lists.newArrayList();

         for(Enchantment var6 : Enchantment.REGISTRY) {
            if (var1.getItem() == Items.BOOK || var6.canApply(var1)) {
               var4.add(var6);
            }
         }

         if (var4.isEmpty()) {
            LOGGER.warn("Couldn't find a compatible enchantment for {}", new Object[]{var1});
            return var1;
         }

         var7 = (Enchantment)var4.get(var2.nextInt(var4.size()));
      }

      int var8 = MathHelper.getInt(var2, var7.getMinLevel(), var7.getMaxLevel());
      if (var1.getItem() == Items.BOOK) {
         var1.setItem(Items.ENCHANTED_BOOK);
         Items.ENCHANTED_BOOK.addEnchantment(var1, new EnchantmentData(var7, var8));
      } else {
         var1.addEnchantment(var7, var8);
      }

      return var1;
   }

   public static class Serializer extends LootFunction.Serializer {
      public Serializer() {
         super(new ResourceLocation("enchant_randomly"), EnchantRandomly.class);
      }

      public void serialize(JsonObject var1, EnchantRandomly var2, JsonSerializationContext var3) {
         if (var2.enchantments != null && !var2.enchantments.isEmpty()) {
            JsonArray var4 = new JsonArray();

            for(Enchantment var6 : var2.enchantments) {
               ResourceLocation var7 = (ResourceLocation)Enchantment.REGISTRY.getNameForObject(var6);
               if (var7 == null) {
                  throw new IllegalArgumentException("Don't know how to serialize enchantment " + var6);
               }

               var4.add(new JsonPrimitive(var7.toString()));
            }

            var1.add("enchantments", var4);
         }

      }

      public EnchantRandomly deserialize(JsonObject var1, JsonDeserializationContext var2, LootCondition[] var3) {
         ArrayList var4 = null;
         if (var1.has("enchantments")) {
            var4 = Lists.newArrayList();

            for(JsonElement var7 : JsonUtils.getJsonArray(var1, "enchantments")) {
               String var8 = JsonUtils.getString(var7, "enchantment");
               Enchantment var9 = (Enchantment)Enchantment.REGISTRY.getObject(new ResourceLocation(var8));
               if (var9 == null) {
                  throw new JsonSyntaxException("Unknown enchantment '" + var8 + "'");
               }

               var4.add(var9);
            }
         }

         return new EnchantRandomly(var3, var4);
      }

      // $FF: synthetic method
      public LootFunction deserialize(JsonObject var1, JsonDeserializationContext var2, LootCondition[] var3) {
         return this.deserialize(var1, var2, var3);
      }
   }
}
