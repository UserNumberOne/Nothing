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
      Enchantment var4;
      if (this.enchantments != null && !this.enchantments.isEmpty()) {
         var4 = (Enchantment)this.enchantments.get(var2.nextInt(this.enchantments.size()));
      } else {
         ArrayList var5 = Lists.newArrayList();

         for(Enchantment var7 : Enchantment.REGISTRY) {
            if (var1.getItem() == Items.BOOK || var7.canApply(var1)) {
               var5.add(var7);
            }
         }

         if (var5.isEmpty()) {
            LOGGER.warn("Couldn't find a compatible enchantment for {}", new Object[]{var1});
            return var1;
         }

         var4 = (Enchantment)var5.get(var2.nextInt(var5.size()));
      }

      int var8 = MathHelper.getInt(var2, var4.getMinLevel(), var4.getMaxLevel());
      if (var1.getItem() == Items.BOOK) {
         var1.setItem(Items.ENCHANTED_BOOK);
         Items.ENCHANTED_BOOK.addEnchantment(var1, new EnchantmentData(var4, var8));
      } else {
         var1.addEnchantment(var4, var8);
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

            for(JsonElement var6 : JsonUtils.getJsonArray(var1, "enchantments")) {
               String var7 = JsonUtils.getString(var6, "enchantment");
               Enchantment var8 = (Enchantment)Enchantment.REGISTRY.getObject(new ResourceLocation(var7));
               if (var8 == null) {
                  throw new JsonSyntaxException("Unknown enchantment '" + var7 + "'");
               }

               var4.add(var8);
            }
         }

         return new EnchantRandomly(var3, var4);
      }
   }
}
