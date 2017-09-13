package net.minecraft.block.properties;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import net.minecraft.util.IStringSerializable;

public class PropertyEnum extends PropertyHelper {
   private final ImmutableSet allowedValues;
   private final Map nameToValue = Maps.newHashMap();

   protected PropertyEnum(String var1, Class var2, Collection var3) {
      super(var1, var2);
      this.allowedValues = ImmutableSet.copyOf(var3);

      for(Enum var5 : var3) {
         String var6 = ((IStringSerializable)var5).getName();
         if (this.nameToValue.containsKey(var6)) {
            throw new IllegalArgumentException("Multiple values have the same name '" + var6 + "'");
         }

         this.nameToValue.put(var6, var5);
      }

   }

   public Collection getAllowedValues() {
      return this.allowedValues;
   }

   public Optional parseValue(String var1) {
      return Optional.fromNullable(this.nameToValue.get(var1));
   }

   public String getName(Enum var1) {
      return ((IStringSerializable)var1).getName();
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (var1 instanceof PropertyEnum && super.equals(var1)) {
         PropertyEnum var2 = (PropertyEnum)var1;
         return this.allowedValues.equals(var2.allowedValues) && this.nameToValue.equals(var2.nameToValue);
      } else {
         return false;
      }
   }

   public int hashCode() {
      int var1 = super.hashCode();
      var1 = 31 * var1 + this.allowedValues.hashCode();
      var1 = 31 * var1 + this.nameToValue.hashCode();
      return var1;
   }

   public static PropertyEnum create(String var0, Class var1) {
      return create(var0, var1, Predicates.alwaysTrue());
   }

   public static PropertyEnum create(String var0, Class var1, Predicate var2) {
      return create(var0, var1, Collections2.filter(Lists.newArrayList(var1.getEnumConstants()), var2));
   }

   public static PropertyEnum create(String var0, Class var1, Enum... var2) {
      return create(var0, var1, Lists.newArrayList(var2));
   }

   public static PropertyEnum create(String var0, Class var1, Collection var2) {
      return new PropertyEnum(var0, var1, var2);
   }
}
