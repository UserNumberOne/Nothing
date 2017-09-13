package net.minecraft.world.storage.loot.functions;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import java.util.Map;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.conditions.LootCondition;

public class LootFunctionManager {
   private static final Map NAME_TO_SERIALIZER_MAP = Maps.newHashMap();
   private static final Map CLASS_TO_SERIALIZER_MAP = Maps.newHashMap();

   public static void registerFunction(LootFunction.Serializer var0) {
      ResourceLocation var1 = var0.getFunctionName();
      Class var2 = var0.getFunctionClass();
      if (NAME_TO_SERIALIZER_MAP.containsKey(var1)) {
         throw new IllegalArgumentException("Can't re-register item function name " + var1);
      } else if (CLASS_TO_SERIALIZER_MAP.containsKey(var2)) {
         throw new IllegalArgumentException("Can't re-register item function class " + var2.getName());
      } else {
         NAME_TO_SERIALIZER_MAP.put(var1, var0);
         CLASS_TO_SERIALIZER_MAP.put(var2, var0);
      }
   }

   public static LootFunction.Serializer getSerializerForName(ResourceLocation var0) {
      LootFunction.Serializer var1 = (LootFunction.Serializer)NAME_TO_SERIALIZER_MAP.get(var0);
      if (var1 == null) {
         throw new IllegalArgumentException("Unknown loot item function '" + var0 + "'");
      } else {
         return var1;
      }
   }

   public static LootFunction.Serializer getSerializerFor(LootFunction var0) {
      LootFunction.Serializer var1 = (LootFunction.Serializer)CLASS_TO_SERIALIZER_MAP.get(var0.getClass());
      if (var1 == null) {
         throw new IllegalArgumentException("Unknown loot item function " + var0);
      } else {
         return var1;
      }
   }

   static {
      registerFunction(new SetCount.Serializer());
      registerFunction(new SetMetadata.Serializer());
      registerFunction(new EnchantWithLevels.Serializer());
      registerFunction(new EnchantRandomly.Serializer());
      registerFunction(new SetNBT.Serializer());
      registerFunction(new Smelt.Serializer());
      registerFunction(new LootingEnchantBonus.Serializer());
      registerFunction(new SetDamage.Serializer());
      registerFunction(new SetAttributes.Serializer());
   }

   public static class Serializer implements JsonDeserializer, JsonSerializer {
      public LootFunction deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         JsonObject var4 = JsonUtils.getJsonObject(var1, "function");
         ResourceLocation var5 = new ResourceLocation(JsonUtils.getString(var4, "function"));

         LootFunction.Serializer var6;
         try {
            var6 = LootFunctionManager.getSerializerForName(var5);
         } catch (IllegalArgumentException var8) {
            throw new JsonSyntaxException("Unknown function '" + var5 + "'");
         }

         return var6.deserialize(var4, var3, (LootCondition[])JsonUtils.deserializeClass(var4, "conditions", new LootCondition[0], var3, LootCondition[].class));
      }

      public JsonElement serialize(LootFunction var1, Type var2, JsonSerializationContext var3) {
         LootFunction.Serializer var4 = LootFunctionManager.getSerializerFor(var1);
         JsonObject var5 = new JsonObject();
         var4.serialize(var5, var1, var3);
         var5.addProperty("function", var4.getFunctionName().toString());
         if (var1.getConditions() != null && var1.getConditions().length > 0) {
            var5.add("conditions", var3.serialize(var1.getConditions()));
         }

         return var5;
      }
   }
}
