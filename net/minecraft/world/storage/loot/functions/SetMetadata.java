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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SetMetadata extends LootFunction {
   private static final Logger LOGGER = LogManager.getLogger();
   private final RandomValueRange metaRange;

   public SetMetadata(LootCondition[] var1, RandomValueRange var2) {
      super(var1);
      this.metaRange = var2;
   }

   public ItemStack apply(ItemStack var1, Random var2, LootContext var3) {
      if (var1.isItemStackDamageable()) {
         LOGGER.warn("Couldn't set data of loot item {}", new Object[]{var1});
      } else {
         var1.setItemDamage(this.metaRange.generateInt(var2));
      }

      return var1;
   }

   public static class Serializer extends LootFunction.Serializer {
      protected Serializer() {
         super(new ResourceLocation("set_data"), SetMetadata.class);
      }

      public void serialize(JsonObject var1, SetMetadata var2, JsonSerializationContext var3) {
         var1.add("data", var3.serialize(var2.metaRange));
      }

      public SetMetadata deserialize(JsonObject var1, JsonDeserializationContext var2, LootCondition[] var3) {
         return new SetMetadata(var3, (RandomValueRange)JsonUtils.deserializeClass(var1, "data", var2, RandomValueRange.class));
      }

      // $FF: synthetic method
      public LootFunction deserialize(JsonObject var1, JsonDeserializationContext var2, LootCondition[] var3) {
         return this.deserialize(var1, var2, var3);
      }
   }
}
