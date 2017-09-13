package net.minecraft.util.registry;

import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.Validate;

public class RegistryNamespacedDefaultedByKey extends RegistryNamespaced {
   private final Object defaultValueKey;
   private Object defaultValue;

   public RegistryNamespacedDefaultedByKey(Object var1) {
      this.defaultValueKey = defaultValueKeyIn;
   }

   public void register(int var1, Object var2, Object var3) {
      if (this.defaultValueKey.equals(key)) {
         this.defaultValue = value;
      }

      super.register(id, key, value);
   }

   public void validateKey() {
      Validate.notNull(this.defaultValue, "Missing default of DefaultedMappedRegistry: " + this.defaultValueKey, new Object[0]);
   }

   public int getIDForObject(Object var1) {
      int i = super.getIDForObject(value);
      return i == -1 ? super.getIDForObject(this.defaultValue) : i;
   }

   @Nonnull
   public Object getNameForObject(Object var1) {
      Object k = (K)super.getNameForObject(value);
      return k == null ? this.defaultValueKey : k;
   }

   @Nonnull
   public Object getObject(@Nullable Object var1) {
      Object v = (V)super.getObject(name);
      return v == null ? this.defaultValue : v;
   }

   @Nonnull
   public Object getObjectById(int var1) {
      Object v = (V)super.getObjectById(id);
      return v == null ? this.defaultValue : v;
   }

   @Nonnull
   public Object getRandomObject(Random var1) {
      Object v = (V)super.getRandomObject(random);
      return v == null ? this.defaultValue : v;
   }

   public int getIDForObjectBypass(Object var1) {
      return super.getIDForObject(bypass);
   }

   public Object getNameForObjectBypass(Object var1) {
      return super.getNameForObject(value);
   }

   public Object getObjectBypass(Object var1) {
      return super.getObject(name);
   }

   public Object getObjectByIdBypass(int var1) {
      return super.getObjectById(id);
   }
}
