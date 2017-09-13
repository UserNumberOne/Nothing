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
      this.properties = propertiesIn;
      this.target = targetIn;
   }

   public boolean testCondition(Random var1, LootContext var2) {
      Entity entity = context.getEntity(this.target);
      if (entity == null) {
         return false;
      } else {
         for(EntityProperty entityproperty : this.properties) {
            if (!entityproperty.testProperty(rand, entity)) {
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
         JsonObject jsonobject = new JsonObject();

         for(EntityProperty entityproperty : value.properties) {
            EntityProperty.Serializer serializer = EntityPropertyManager.getSerializerFor(entityproperty);
            jsonobject.add(serializer.getName().toString(), serializer.serialize(entityproperty, context));
         }

         json.add("properties", jsonobject);
         json.add("entity", context.serialize(value.target));
      }

      public EntityHasProperty deserialize(JsonObject var1, JsonDeserializationContext var2) {
         Set set = JsonUtils.getJsonObject(json, "properties").entrySet();
         EntityProperty[] aentityproperty = new EntityProperty[set.size()];
         int i = 0;

         for(Entry entry : set) {
            aentityproperty[i++] = EntityPropertyManager.getSerializerForName(new ResourceLocation((String)entry.getKey())).deserialize((JsonElement)entry.getValue(), context);
         }

         return new EntityHasProperty(aentityproperty, (LootContext.EntityTarget)JsonUtils.deserializeClass(json, "entity", context, LootContext.EntityTarget.class));
      }
   }
}
