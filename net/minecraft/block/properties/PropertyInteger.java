package net.minecraft.block.properties;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;

public class PropertyInteger extends PropertyHelper {
   private final ImmutableSet allowedValues;

   protected PropertyInteger(String var1, int var2, int var3) {
      super(name, Integer.class);
      if (min < 0) {
         throw new IllegalArgumentException("Min value of " + name + " must be 0 or greater");
      } else if (max <= min) {
         throw new IllegalArgumentException("Max value of " + name + " must be greater than min (" + min + ")");
      } else {
         Set set = Sets.newHashSet();

         for(int i = min; i <= max; ++i) {
            set.add(Integer.valueOf(i));
         }

         this.allowedValues = ImmutableSet.copyOf(set);
      }
   }

   public Collection getAllowedValues() {
      return this.allowedValues;
   }

   public boolean equals(Object var1) {
      if (this == p_equals_1_) {
         return true;
      } else if (p_equals_1_ instanceof PropertyInteger && super.equals(p_equals_1_)) {
         PropertyInteger propertyinteger = (PropertyInteger)p_equals_1_;
         return this.allowedValues.equals(propertyinteger.allowedValues);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return 31 * super.hashCode() + this.allowedValues.hashCode();
   }

   public static PropertyInteger create(String var0, int var1, int var2) {
      return new PropertyInteger(name, min, max);
   }

   public Optional parseValue(String var1) {
      try {
         Integer integer = Integer.valueOf(value);
         return this.allowedValues.contains(integer) ? Optional.of(integer) : Optional.absent();
      } catch (NumberFormatException var3) {
         return Optional.absent();
      }
   }

   public String getName(Integer var1) {
      return value.toString();
   }
}
