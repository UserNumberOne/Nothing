package net.minecraft.world.storage.loot.functions;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SetAttributes extends LootFunction {
   private static final Logger LOGGER = LogManager.getLogger();
   private final SetAttributes.Modifier[] modifiers;

   public SetAttributes(LootCondition[] var1, SetAttributes.Modifier[] var2) {
      super(var1);
      this.modifiers = var2;
   }

   public ItemStack apply(ItemStack var1, Random var2, LootContext var3) {
      for(SetAttributes.Modifier var7 : this.modifiers) {
         UUID var8 = var7.uuid;
         if (var8 == null) {
            var8 = UUID.randomUUID();
         }

         EntityEquipmentSlot var9 = var7.slots[var2.nextInt(var7.slots.length)];
         var1.addAttributeModifier(var7.attributeName, new AttributeModifier(var8, var7.modifierName, (double)var7.amount.generateFloat(var2), var7.operation), var9);
      }

      return var1;
   }

   static class Modifier {
      private final String modifierName;
      private final String attributeName;
      private final int operation;
      private final RandomValueRange amount;
      @Nullable
      private final UUID uuid;
      private final EntityEquipmentSlot[] slots;

      private Modifier(String var1, String var2, int var3, RandomValueRange var4, EntityEquipmentSlot[] var5, @Nullable UUID var6) {
         this.modifierName = var1;
         this.attributeName = var2;
         this.operation = var3;
         this.amount = var4;
         this.uuid = var6;
         this.slots = var5;
      }

      public JsonObject serialize(JsonSerializationContext var1) {
         JsonObject var2 = new JsonObject();
         var2.addProperty("name", this.modifierName);
         var2.addProperty("attribute", this.attributeName);
         var2.addProperty("operation", getOperationFromStr(this.operation));
         var2.add("amount", var1.serialize(this.amount));
         if (this.uuid != null) {
            var2.addProperty("id", this.uuid.toString());
         }

         if (this.slots.length == 1) {
            var2.addProperty("slot", this.slots[0].getName());
         } else {
            JsonArray var3 = new JsonArray();

            for(EntityEquipmentSlot var7 : this.slots) {
               var3.add(new JsonPrimitive(var7.getName()));
            }

            var2.add("slot", var3);
         }

         return var2;
      }

      public static SetAttributes.Modifier deserialize(JsonObject var0, JsonDeserializationContext var1) {
         String var2 = JsonUtils.getString(var0, "name");
         String var3 = JsonUtils.getString(var0, "attribute");
         int var4 = getOperationFromInt(JsonUtils.getString(var0, "operation"));
         RandomValueRange var5 = (RandomValueRange)JsonUtils.deserializeClass(var0, "amount", var1, RandomValueRange.class);
         UUID var6 = null;
         EntityEquipmentSlot[] var7;
         if (JsonUtils.isString(var0, "slot")) {
            var7 = new EntityEquipmentSlot[]{EntityEquipmentSlot.fromString(JsonUtils.getString(var0, "slot"))};
         } else {
            if (!JsonUtils.isJsonArray(var0, "slot")) {
               throw new JsonSyntaxException("Invalid or missing attribute modifier slot; must be either string or array of strings.");
            }

            JsonArray var8 = JsonUtils.getJsonArray(var0, "slot");
            var7 = new EntityEquipmentSlot[var8.size()];
            int var9 = 0;

            for(JsonElement var11 : var8) {
               var7[var9++] = EntityEquipmentSlot.fromString(JsonUtils.getString(var11, "slot"));
            }

            if (var7.length == 0) {
               throw new JsonSyntaxException("Invalid attribute modifier slot; must contain at least one entry.");
            }
         }

         if (var0.has("id")) {
            String var13 = JsonUtils.getString(var0, "id");

            try {
               var6 = UUID.fromString(var13);
            } catch (IllegalArgumentException var12) {
               throw new JsonSyntaxException("Invalid attribute modifier id '" + var13 + "' (must be UUID format, with dashes)");
            }
         }

         return new SetAttributes.Modifier(var2, var3, var4, var5, var7, var6);
      }

      private static String getOperationFromStr(int var0) {
         switch(var0) {
         case 0:
            return "addition";
         case 1:
            return "multiply_base";
         case 2:
            return "multiply_total";
         default:
            throw new IllegalArgumentException("Unknown operation " + var0);
         }
      }

      private static int getOperationFromInt(String var0) {
         if ("addition".equals(var0)) {
            return 0;
         } else if ("multiply_base".equals(var0)) {
            return 1;
         } else if ("multiply_total".equals(var0)) {
            return 2;
         } else {
            throw new JsonSyntaxException("Unknown attribute modifier operation " + var0);
         }
      }
   }

   public static class Serializer extends LootFunction.Serializer {
      public Serializer() {
         super(new ResourceLocation("set_attributes"), SetAttributes.class);
      }

      public void serialize(JsonObject var1, SetAttributes var2, JsonSerializationContext var3) {
         JsonArray var4 = new JsonArray();

         for(SetAttributes.Modifier var8 : var2.modifiers) {
            var4.add(var8.serialize(var3));
         }

         var1.add("modifiers", var4);
      }

      public SetAttributes deserialize(JsonObject var1, JsonDeserializationContext var2, LootCondition[] var3) {
         JsonArray var4 = JsonUtils.getJsonArray(var1, "modifiers");
         SetAttributes.Modifier[] var5 = new SetAttributes.Modifier[var4.size()];
         int var6 = 0;

         for(JsonElement var8 : var4) {
            var5[var6++] = SetAttributes.Modifier.deserialize(JsonUtils.getJsonObject(var8, "modifier"), var2);
         }

         if (var5.length == 0) {
            throw new JsonSyntaxException("Invalid attribute modifiers array; cannot be empty");
         } else {
            return new SetAttributes(var3, var5);
         }
      }
   }
}
