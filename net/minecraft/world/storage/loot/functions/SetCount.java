package net.minecraft.world.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;

public class SetCount extends LootFunction {
   private final RandomValueRange countRange;

   public SetCount(LootCondition[] var1, RandomValueRange var2) {
      super(conditionsIn);
      this.countRange = countRangeIn;
   }

   public ItemStack apply(ItemStack var1, Random var2, LootContext var3) {
      stack.stackSize = this.countRange.generateInt(rand);
      return stack;
   }

   public static class Serializer extends LootFunction.Serializer {
      protected Serializer() {
         super(new ResourceLocation("set_count"), SetCount.class);
      }

      public void serialize(JsonObject var1, SetCount var2, JsonSerializationContext var3) {
         object.add("count", serializationContext.serialize(functionClazz.countRange));
      }

      public SetCount deserialize(JsonObject var1, JsonDeserializationContext var2, LootCondition[] var3) {
         return new SetCount(conditionsIn, (RandomValueRange)JsonUtils.deserializeClass(object, "count", deserializationContext, RandomValueRange.class));
      }
   }
}
