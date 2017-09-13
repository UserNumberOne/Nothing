package net.minecraft.util.registry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RegistryDefaulted extends RegistrySimple {
   private final Object defaultObject;

   public RegistryDefaulted(Object var1) {
      this.defaultObject = var1;
   }

   @Nonnull
   public Object getObject(@Nullable Object var1) {
      Object var2 = super.getObject(var1);
      return var2 == null ? this.defaultObject : var2;
   }
}
