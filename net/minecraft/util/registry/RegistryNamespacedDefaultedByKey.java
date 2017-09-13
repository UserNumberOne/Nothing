package net.minecraft.util.registry;

import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.Validate;

public class RegistryNamespacedDefaultedByKey extends RegistryNamespaced {
   private final Object defaultValueKey;
   private Object defaultValue;

   public RegistryNamespacedDefaultedByKey(Object var1) {
      this.defaultValueKey = var1;
   }

   public void register(int var1, Object var2, Object var3) {
      if (this.defaultValueKey.equals(var2)) {
         this.defaultValue = var3;
      }

      super.register(var1, var2, var3);
   }

   public void validateKey() {
      Validate.notNull(this.defaultValue, "Missing default of DefaultedMappedRegistry: " + this.defaultValueKey, new Object[0]);
   }

   public int getIDForObject(Object var1) {
      int var2 = super.getIDForObject(var1);
      return var2 == -1 ? super.getIDForObject(this.defaultValue) : var2;
   }

   @Nonnull
   public Object getNameForObject(Object var1) {
      Object var2 = super.getNameForObject(var1);
      return var2 == null ? this.defaultValueKey : var2;
   }

   @Nonnull
   public Object getObject(@Nullable Object var1) {
      Object var2 = super.getObject(var1);
      return var2 == null ? this.defaultValue : var2;
   }

   @Nonnull
   public Object getObjectById(int var1) {
      Object var2 = super.getObjectById(var1);
      return var2 == null ? this.defaultValue : var2;
   }

   @Nonnull
   public Object getRandomObject(Random var1) {
      Object var2 = super.getRandomObject(var1);
      return var2 == null ? this.defaultValue : var2;
   }
}
