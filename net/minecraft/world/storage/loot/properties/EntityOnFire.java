package net.minecraft.world.storage.loot.properties;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

public class EntityOnFire implements EntityProperty {
   private final boolean onFire;

   public EntityOnFire(boolean var1) {
      this.onFire = var1;
   }

   public boolean testProperty(Random var1, Entity var2) {
      return var2.isBurning() == this.onFire;
   }

   public static class Serializer extends EntityProperty.Serializer {
      protected Serializer() {
         super(new ResourceLocation("on_fire"), EntityOnFire.class);
      }

      public JsonElement serialize(EntityOnFire var1, JsonSerializationContext var2) {
         return new JsonPrimitive(var1.onFire);
      }

      public EntityOnFire deserialize(JsonElement var1, JsonDeserializationContext var2) {
         return new EntityOnFire(JsonUtils.getBoolean(var1, "on_fire"));
      }

      // $FF: synthetic method
      public EntityProperty deserialize(JsonElement var1, JsonDeserializationContext var2) {
         return this.deserialize(var1, var2);
      }
   }
}
