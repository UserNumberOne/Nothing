package net.minecraft.block.properties;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashSet;

public class PropertyInteger extends PropertyHelper {
   private final ImmutableSet allowedValues;

   protected PropertyInteger(String var1, int var2, int var3) {
      super(var1, Integer.class);
      if (var2 < 0) {
         throw new IllegalArgumentException("Min value of " + var1 + " must be 0 or greater");
      } else if (var3 <= var2) {
         throw new IllegalArgumentException("Max value of " + var1 + " must be greater than min (" + var2 + ")");
      } else {
         HashSet var4 = Sets.newHashSet();

         for(int var5 = var2; var5 <= var3; ++var5) {
            var4.add(Integer.valueOf(var5));
         }

         this.allowedValues = ImmutableSet.copyOf(var4);
      }
   }

   public Collection getAllowedValues() {
      return this.allowedValues;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (var1 instanceof PropertyInteger && super.equals(var1)) {
         PropertyInteger var2 = (PropertyInteger)var1;
         return this.allowedValues.equals(var2.allowedValues);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return 31 * super.hashCode() + this.allowedValues.hashCode();
   }

   public static PropertyInteger create(String var0, int var1, int var2) {
      return new PropertyInteger(var0, var1, var2);
   }

   public Optional parseValue(String var1) {
      try {
         Integer var2 = Integer.valueOf(var1);
         return this.allowedValues.contains(var2) ? Optional.of(var2) : Optional.absent();
      } catch (NumberFormatException var3) {
         return Optional.absent();
      }
   }

   public String getName(Integer var1) {
      return var1.toString();
   }
}
