package net.minecraft.world.storage.loot.conditions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.entity.Entity;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.properties.EntityProperty;
import net.minecraft.world.storage.loot.properties.EntityPropertyManager;

public class EntityHasProperty implements LootCondition {
   private final EntityProperty[] properties;
   private final LootContext.EntityTarget target;

   public EntityHasProperty(EntityProperty[] var1, LootContext.EntityTarget var2) {
      this.properties = var1;
      this.target = var2;
   }

   public boolean testCondition(Random var1, LootContext var2) {
      Entity var3 = var2.getEntity(this.target);
      if (var3 == null) {
         return false;
      } else {
         for(EntityProperty var7 : this.properties) {
            if (!var7.testProperty(var1, var3)) {
               return false;
            }
         }

         return true;
      }
   }

   public static class Serializer extends LootCondition.Serializer {
      protected Serializer() {
         super(new ResourceLocation("entity_properties"), EntityHasProperty.class);
      }

      public void serialize(JsonObject var1, EntityHasProperty var2, JsonSerializationContext var3) {
         JsonObject var4 = new JsonObject();

         for(EntityProperty var8 : var2.properties) {
            EntityProperty.Serializer var9 = EntityPropertyManager.getSerializerFor(var8);
            var4.add(var9.getName().toString(), var9.serialize(var8, var3));
         }

         var1.add("properties", var4);
         var1.add("entity", var3.serialize(var2.target));
      }

      public EntityHasProperty deserialize(JsonObject var1, JsonDeserializationContext var2) {
         Set var3 = JsonUtils.getJsonObject(var1, "properties").entrySet();
         EntityProperty[] var4 = new EntityProperty[var3.size()];
         int var5 = 0;

         for(Entry var7 : var3) {
            var4[var5++] = EntityPropertyManager.getSerializerForName(new ResourceLocation((String)var7.getKey())).deserialize((JsonElement)var7.getValue(), var2);
         }

         return new EntityHasProperty(var4, (LootContext.EntityTarget)JsonUtils.deserializeClass(var1, "entity", var2, LootContext.EntityTarget.class));
      }
   }
}
