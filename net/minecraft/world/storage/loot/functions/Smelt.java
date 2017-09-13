package net.minecraft.world.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Smelt extends LootFunction {
   private static final Logger LOGGER = LogManager.getLogger();

   public Smelt(LootCondition[] var1) {
      super(var1);
   }

   public ItemStack apply(ItemStack var1, Random var2, LootContext var3) {
      ItemStack var4 = FurnaceRecipes.instance().getSmeltingResult(var1);
      if (var4 == null) {
         LOGGER.warn("Couldn't smelt {} because there is no smelting recipe", new Object[]{var1});
         return var1;
      } else {
         ItemStack var5 = var4.copy();
         var5.stackSize = var1.stackSize;
         return var5;
      }
   }

   public static class Serializer extends LootFunction.Serializer {
      protected Serializer() {
         super(new ResourceLocation("furnace_smelt"), Smelt.class);
      }

      public void serialize(JsonObject var1, Smelt var2, JsonSerializationContext var3) {
      }

      public Smelt deserialize(JsonObject var1, JsonDeserializationContext var2, LootCondition[] var3) {
         return new Smelt(var3);
      }

      // $FF: synthetic method
      public LootFunction deserialize(JsonObject var1, JsonDeserializationContext var2, LootCondition[] var3) {
         return this.deserialize(var1, var2, var3);
      }
   }
}
