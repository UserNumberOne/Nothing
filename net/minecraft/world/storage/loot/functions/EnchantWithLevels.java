package net.minecraft.world.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;

public class EnchantWithLevels extends LootFunction {
   private final RandomValueRange randomLevel;
   private final boolean isTreasure;

   public EnchantWithLevels(LootCondition[] var1, RandomValueRange var2, boolean var3) {
      super(var1);
      this.randomLevel = var2;
      this.isTreasure = var3;
   }

   public ItemStack apply(ItemStack var1, Random var2, LootContext var3) {
      EnchantmentHelper.addRandomEnchantment(var2, var1, this.randomLevel.generateInt(var2), this.isTreasure);
      return var1;
   }

   public static class Serializer extends LootFunction.Serializer {
      public Serializer() {
         super(new ResourceLocation("enchant_with_levels"), EnchantWithLevels.class);
      }

      public void serialize(JsonObject var1, EnchantWithLevels var2, JsonSerializationContext var3) {
         var1.add("levels", var3.serialize(var2.randomLevel));
         var1.addProperty("treasure", Boolean.valueOf(var2.isTreasure));
      }

      public EnchantWithLevels deserialize(JsonObject var1, JsonDeserializationContext var2, LootCondition[] var3) {
         RandomValueRange var4 = (RandomValueRange)JsonUtils.deserializeClass(var1, "levels", var2, RandomValueRange.class);
         boolean var5 = JsonUtils.getBoolean(var1, "treasure", false);
         return new EnchantWithLevels(var3, var4, var5);
      }
   }
}
