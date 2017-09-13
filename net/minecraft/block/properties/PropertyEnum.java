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
      super(name, valueClass);
      this.allowedValues = ImmutableSet.copyOf(allowedValues);

      for(Enum t : allowedValues) {
         String s = ((IStringSerializable)t).getName();
         if (this.nameToValue.containsKey(s)) {
            throw new IllegalArgumentException("Multiple values have the same name '" + s + "'");
         }

         this.nameToValue.put(s, t);
      }

   }

   public Collection getAllowedValues() {
      return this.allowedValues;
   }

   public Optional parseValue(String var1) {
      return Optional.fromNullable(this.nameToValue.get(value));
   }

   public String getName(Enum var1) {
      return ((IStringSerializable)value).getName();
   }

   public boolean equals(Object var1) {
      if (this == p_equals_1_) {
         return true;
      } else if (p_equals_1_ instanceof PropertyEnum && super.equals(p_equals_1_)) {
         PropertyEnum propertyenum = (PropertyEnum)p_equals_1_;
         return this.allowedValues.equals(propertyenum.allowedValues) && this.nameToValue.equals(propertyenum.nameToValue);
      } else {
         return false;
      }
   }

   public int hashCode() {
      int i = super.hashCode();
      i = 31 * i + this.allowedValues.hashCode();
      i = 31 * i + this.nameToValue.hashCode();
      return i;
   }

   public static PropertyEnum create(String var0, Class var1) {
      return create(name, clazz, Predicates.alwaysTrue());
   }

   public static PropertyEnum create(String var0, Class var1, Predicate var2) {
      return create(name, clazz, Collections2.filter(Lists.newArrayList(clazz.getEnumConstants()), filter));
   }

   public static PropertyEnum create(String var0, Class var1, Enum... var2) {
      return create(name, clazz, Lists.newArrayList(values));
   }

   public static PropertyEnum create(String var0, Class var1, Collection var2) {
      return new PropertyEnum(name, clazz, values);
   }
}
