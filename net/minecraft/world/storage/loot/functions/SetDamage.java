package net.minecraft.world.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SetDamage extends LootFunction {
   private static final Logger LOGGER = LogManager.getLogger();
   private final RandomValueRange damageRange;

   public SetDamage(LootCondition[] var1, RandomValueRange var2) {
      super(var1);
      this.damageRange = var2;
   }

   public ItemStack apply(ItemStack var1, Random var2, LootContext var3) {
      if (var1.isItemStackDamageable()) {
         float var4 = 1.0F - this.damageRange.generateFloat(var2);
         var1.setItemDamage(MathHelper.floor(var4 * (float)var1.getMaxDamage()));
      } else {
         LOGGER.warn("Couldn't set damage of loot item {}", new Object[]{var1});
      }

      return var1;
   }

   public static class Serializer extends LootFunction.Serializer {
      protected Serializer() {
         super(new ResourceLocation("set_damage"), SetDamage.class);
      }

      public void serialize(JsonObject var1, SetDamage var2, JsonSerializationContext var3) {
         var1.add("damage", var3.serialize(var2.damageRange));
      }

      public SetDamage deserialize(JsonObject var1, JsonDeserializationContext var2, LootCondition[] var3) {
         return new SetDamage(var3, (RandomValueRange)JsonUtils.deserializeClass(var1, "damage", var2, RandomValueRange.class));
      }
   }
}
