package net.minecraft.world.storage.loot.conditions;

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
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;

public class LootConditionManager {
   private static final Map NAME_TO_SERIALIZER_MAP = Maps.newHashMap();
   private static final Map CLASS_TO_SERIALIZER_MAP = Maps.newHashMap();

   public static void registerCondition(LootCondition.Serializer var0) {
      ResourceLocation var1 = var0.getLootTableLocation();
      Class var2 = var0.getConditionClass();
      if (NAME_TO_SERIALIZER_MAP.containsKey(var1)) {
         throw new IllegalArgumentException("Can't re-register item condition name " + var1);
      } else if (CLASS_TO_SERIALIZER_MAP.containsKey(var2)) {
         throw new IllegalArgumentException("Can't re-register item condition class " + var2.getName());
      } else {
         NAME_TO_SERIALIZER_MAP.put(var1, var0);
         CLASS_TO_SERIALIZER_MAP.put(var2, var0);
      }
   }

   public static boolean testAllConditions(@Nullable LootCondition[] var0, Random var1, LootContext var2) {
      if (var0 == null) {
         return true;
      } else {
         for(LootCondition var6 : var0) {
            if (!var6.testCondition(var1, var2)) {
               return false;
            }
         }

         return true;
      }
   }

   public static LootCondition.Serializer getSerializerForName(ResourceLocation var0) {
      LootCondition.Serializer var1 = (LootCondition.Serializer)NAME_TO_SERIALIZER_MAP.get(var0);
      if (var1 == null) {
         throw new IllegalArgumentException("Unknown loot item condition '" + var0 + "'");
      } else {
         return var1;
      }
   }

   public static LootCondition.Serializer getSerializerFor(LootCondition var0) {
      LootCondition.Serializer var1 = (LootCondition.Serializer)CLASS_TO_SERIALIZER_MAP.get(var0.getClass());
      if (var1 == null) {
         throw new IllegalArgumentException("Unknown loot item condition " + var0);
      } else {
         return var1;
      }
   }

   static {
      registerCondition(new RandomChance.Serializer());
      registerCondition(new RandomChanceWithLooting.Serializer());
      registerCondition(new EntityHasProperty.Serializer());
      registerCondition(new KilledByPlayer.Serializer());
      registerCondition(new EntityHasScore.Serializer());
   }

   public static class Serializer implements JsonDeserializer, JsonSerializer {
      public LootCondition deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         JsonObject var4 = JsonUtils.getJsonObject(var1, "condition");
         ResourceLocation var5 = new ResourceLocation(JsonUtils.getString(var4, "condition"));

         LootCondition.Serializer var6;
         try {
            var6 = LootConditionManager.getSerializerForName(var5);
         } catch (IllegalArgumentException var8) {
            throw new JsonSyntaxException("Unknown condition '" + var5 + "'");
         }

         return var6.deserialize(var4, var3);
      }

      public JsonElement serialize(LootCondition var1, Type var2, JsonSerializationContext var3) {
         LootCondition.Serializer var4 = LootConditionManager.getSerializerFor(var1);
         JsonObject var5 = new JsonObject();
         var4.serialize(var5, var1, var3);
         var5.addProperty("condition", var4.getLootTableLocation().toString());
         return var5;
      }

      // $FF: synthetic method
      public JsonElement serialize(Object var1, Type var2, JsonSerializationContext var3) {
         return this.serialize((LootCondition)var1, var2, var3);
      }

      // $FF: synthetic method
      public Object deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         return this.deserialize(var1, var2, var3);
      }
   }
}
