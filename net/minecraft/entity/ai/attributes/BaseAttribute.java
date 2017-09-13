package net.minecraft.entity.ai.attributes;

import javax.annotation.Nullable;

public abstract class BaseAttribute implements IAttribute {
   private final IAttribute parent;
   private final String unlocalizedName;
   private final double defaultValue;
   private boolean shouldWatch;

   protected BaseAttribute(@Nullable IAttribute var1, String var2, double var3) {
      this.parent = parentIn;
      this.unlocalizedName = unlocalizedNameIn;
      this.defaultValue = defaultValueIn;
      if (unlocalizedNameIn == null) {
         throw new IllegalArgumentException("Name cannot be null!");
      }
   }

   public String getName() {
      return this.unlocalizedName;
   }

   public double getDefaultValue() {
      return this.defaultValue;
   }

   public boolean getShouldWatch() {
      return this.shouldWatch;
   }

   public BaseAttribute setShouldWatch(boolean var1) {
      this.shouldWatch = shouldWatchIn;
      return this;
   }

   @Nullable
   public IAttribute getParent() {
      return this.parent;
   }

   public int hashCode() {
      return this.unlocalizedName.hashCode();
   }

   public boolean equals(Object var1) {
      return p_equals_1_ instanceof IAttribute && this.unlocalizedName.equals(((IAttribute)p_equals_1_).getName());
   }
}
