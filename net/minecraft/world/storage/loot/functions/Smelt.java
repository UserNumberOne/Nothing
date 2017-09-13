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
      super(conditionsIn);
   }

   public ItemStack apply(ItemStack var1, Random var2, LootContext var3) {
      ItemStack itemstack = FurnaceRecipes.instance().getSmeltingResult(stack);
      if (itemstack == null) {
         LOGGER.warn("Couldn't smelt {} because there is no smelting recipe", new Object[]{stack});
         return stack;
      } else {
         ItemStack itemstack1 = itemstack.copy();
         itemstack1.stackSize = stack.stackSize;
         return itemstack1;
      }
   }

   public static class Serializer extends LootFunction.Serializer {
      protected Serializer() {
         super(new ResourceLocation("furnace_smelt"), Smelt.class);
      }

      public void serialize(JsonObject var1, Smelt var2, JsonSerializationContext var3) {
      }

      public Smelt deserialize(JsonObject var1, JsonDeserializationContext var2, LootCondition[] var3) {
         return new Smelt(conditionsIn);
      }
   }
}
