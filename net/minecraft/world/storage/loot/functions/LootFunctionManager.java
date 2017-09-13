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
      ResourceLocation resourcelocation = p_186582_0_.getFunctionName();
      Class oclass = p_186582_0_.getFunctionClass();
      if (NAME_TO_SERIALIZER_MAP.containsKey(resourcelocation)) {
         throw new IllegalArgumentException("Can't re-register item function name " + resourcelocation);
      } else if (CLASS_TO_SERIALIZER_MAP.containsKey(oclass)) {
         throw new IllegalArgumentException("Can't re-register item function class " + oclass.getName());
      } else {
         NAME_TO_SERIALIZER_MAP.put(resourcelocation, p_186582_0_);
         CLASS_TO_SERIALIZER_MAP.put(oclass, p_186582_0_);
      }
   }

   public static LootFunction.Serializer getSerializerForName(ResourceLocation var0) {
      LootFunction.Serializer serializer = (LootFunction.Serializer)NAME_TO_SERIALIZER_MAP.get(location);
      if (serializer == null) {
         throw new IllegalArgumentException("Unknown loot item function '" + location + "'");
      } else {
         return serializer;
      }
   }

   public static LootFunction.Serializer getSerializerFor(LootFunction var0) {
      LootFunction.Serializer serializer = (LootFunction.Serializer)CLASS_TO_SERIALIZER_MAP.get(functionClass.getClass());
      if (serializer == null) {
         throw new IllegalArgumentException("Unknown loot item function " + functionClass);
      } else {
         return serializer;
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
         JsonObject jsonobject = JsonUtils.getJsonObject(p_deserialize_1_, "function");
         ResourceLocation resourcelocation = new ResourceLocation(JsonUtils.getString(jsonobject, "function"));

         LootFunction.Serializer serializer;
         try {
            serializer = LootFunctionManager.getSerializerForName(resourcelocation);
         } catch (IllegalArgumentException var8) {
            throw new JsonSyntaxException("Unknown function '" + resourcelocation + "'");
         }

         return serializer.deserialize(jsonobject, p_deserialize_3_, (LootCondition[])JsonUtils.deserializeClass(jsonobject, "conditions", new LootCondition[0], p_deserialize_3_, LootCondition[].class));
      }

      public JsonElement serialize(LootFunction var1, Type var2, JsonSerializationContext var3) {
         LootFunction.Serializer serializer = LootFunctionManager.getSerializerFor(p_serialize_1_);
         JsonObject jsonobject = new JsonObject();
         serializer.serialize(jsonobject, p_serialize_1_, p_serialize_3_);
         jsonobject.addProperty("function", serializer.getFunctionName().toString());
         if (p_serialize_1_.getConditions() != null && p_serialize_1_.getConditions().length > 0) {
            jsonobject.add("conditions", p_serialize_3_.serialize(p_serialize_1_.getConditions()));
         }

         return jsonobject;
      }
   }
}
