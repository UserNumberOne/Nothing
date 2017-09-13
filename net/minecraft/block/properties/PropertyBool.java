package net.minecraft.block.properties;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;

public class PropertyBool extends PropertyHelper {
   private final ImmutableSet allowedValues = ImmutableSet.of(Boolean.valueOf(true), Boolean.valueOf(false));

   protected PropertyBool(String var1) {
      super(name, Boolean.class);
   }

   public Collection getAllowedValues() {
      return this.allowedValues;
   }

   public static PropertyBool create(String var0) {
      return new PropertyBool(name);
   }

   public Optional parseValue(String var1) {
      return !"true".equals(value) && !"false".equals(value) ? Optional.absent() : Optional.of(Boolean.valueOf(value));
   }

   public String getName(Boolean var1) {
      return value.toString();
   }

   public boolean equals(Object var1) {
      if (this == p_equals_1_) {
         return true;
      } else if (p_equals_1_ instanceof PropertyBool && super.equals(p_equals_1_)) {
         PropertyBool propertybool = (PropertyBool)p_equals_1_;
         return this.allowedValues.equals(propertybool.allowedValues);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return 31 * super.hashCode() + this.allowedValues.hashCode();
   }
}
