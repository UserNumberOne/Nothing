package net.minecraft.util.registry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RegistryDefaulted extends RegistrySimple {
   private final Object defaultObject;

   public RegistryDefaulted(Object var1) {
      this.defaultObject = defaultObjectIn;
   }

   @Nonnull
   public Object getObject(@Nullable Object var1) {
      Object v = (V)super.getObject(name);
      return v == null ? this.defaultObject : v;
   }
}
