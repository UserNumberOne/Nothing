package net.minecraft.world.storage.loot.properties;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.util.ResourceLocation;

public class EntityPropertyManager {
   private static final Map NAME_TO_SERIALIZER_MAP = Maps.newHashMap();
   private static final Map CLASS_TO_SERIALIZER_MAP = Maps.newHashMap();

   public static void registerProperty(EntityProperty.Serializer var0) {
      ResourceLocation var1 = var0.getName();
      Class var2 = var0.getPropertyClass();
      if (NAME_TO_SERIALIZER_MAP.containsKey(var1)) {
         throw new IllegalArgumentException("Can't re-register entity property name " + var1);
      } else if (CLASS_TO_SERIALIZER_MAP.containsKey(var2)) {
         throw new IllegalArgumentException("Can't re-register entity property class " + var2.getName());
      } else {
         NAME_TO_SERIALIZER_MAP.put(var1, var0);
         CLASS_TO_SERIALIZER_MAP.put(var2, var0);
      }
   }

   public static EntityProperty.Serializer getSerializerForName(ResourceLocation var0) {
      EntityProperty.Serializer var1 = (EntityProperty.Serializer)NAME_TO_SERIALIZER_MAP.get(var0);
      if (var1 == null) {
         throw new IllegalArgumentException("Unknown loot entity property '" + var0 + "'");
      } else {
         return var1;
      }
   }

   public static EntityProperty.Serializer getSerializerFor(EntityProperty var0) {
      EntityProperty.Serializer var1 = (EntityProperty.Serializer)CLASS_TO_SERIALIZER_MAP.get(var0.getClass());
      if (var1 == null) {
         throw new IllegalArgumentException("Unknown loot entity property " + var0);
      } else {
         return var1;
      }
   }

   static {
      registerProperty(new EntityOnFire.Serializer());
   }
}
