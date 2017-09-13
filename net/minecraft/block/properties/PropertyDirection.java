package net.minecraft.block.properties;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import java.util.Collection;
import net.minecraft.util.EnumFacing;

public class PropertyDirection extends PropertyEnum {
   protected PropertyDirection(String var1, Collection var2) {
      super(name, EnumFacing.class, values);
   }

   public static PropertyDirection create(String var0) {
      return create(name, Predicates.alwaysTrue());
   }

   public static PropertyDirection create(String var0, Predicate var1) {
      return create(name, Collections2.filter(Lists.newArrayList(EnumFacing.values()), filter));
   }

   public static PropertyDirection create(String var0, Collection var1) {
      return new PropertyDirection(name, values);
   }
}
