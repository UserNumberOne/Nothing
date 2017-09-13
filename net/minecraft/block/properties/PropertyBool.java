package net.minecraft.block.properties;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;

public class PropertyBool extends PropertyHelper {
   private final ImmutableSet allowedValues = ImmutableSet.of(Boolean.valueOf(true), Boolean.valueOf(false));

   protected PropertyBool(String var1) {
      super(var1, Boolean.class);
   }

   public Collection getAllowedValues() {
      return this.allowedValues;
   }

   public static PropertyBool create(String var0) {
      return new PropertyBool(var0);
   }

   public Optional parseValue(String var1) {
      return !"true".equals(var1) && !"false".equals(var1) ? Optional.absent() : Optional.of(Boolean.valueOf(var1));
   }

   public String getName(Boolean var1) {
      return var1.toString();
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (var1 instanceof PropertyBool && super.equals(var1)) {
         PropertyBool var2 = (PropertyBool)var1;
         return this.allowedValues.equals(var2.allowedValues);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return 31 * super.hashCode() + this.allowedValues.hashCode();
   }
}
