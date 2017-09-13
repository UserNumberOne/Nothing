package net.minecraft.block.state;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;

public abstract class BlockStateBase implements IBlockState {
   private static final Joiner COMMA_JOINER = Joiner.on(',');
   private static final Function MAP_ENTRY_TO_STRING = new Function() {
      @Nullable
      public String apply(@Nullable Entry var1) {
         if (p_apply_1_ == null) {
            return "<NULL>";
         } else {
            IProperty iproperty = (IProperty)p_apply_1_.getKey();
            return iproperty.getName() + "=" + this.getPropertyName(iproperty, (Comparable)p_apply_1_.getValue());
         }
      }

      private String getPropertyName(IProperty var1, Comparable var2) {
         return property.getName(entry);
      }
   };

   public IBlockState cycleProperty(IProperty var1) {
      return this.withProperty(property, (Comparable)cyclePropertyValue(property.getAllowedValues(), this.getValue(property)));
   }

   protected static Object cyclePropertyValue(Collection var0, Object var1) {
      Iterator iterator = values.iterator();

      while(iterator.hasNext()) {
         if (iterator.next().equals(currentValue)) {
            if (iterator.hasNext()) {
               return iterator.next();
            }

            return values.iterator().next();
         }
      }

      return iterator.next();
   }

   public String toString() {
      StringBuilder stringbuilder = new StringBuilder();
      stringbuilder.append(Block.REGISTRY.getNameForObject(this.getBlock()));
      if (!this.getProperties().isEmpty()) {
         stringbuilder.append("[");
         COMMA_JOINER.appendTo(stringbuilder, Iterables.transform(this.getProperties().entrySet(), MAP_ENTRY_TO_STRING));
         stringbuilder.append("]");
      }

      return stringbuilder.toString();
   }

   public ImmutableTable getPropertyValueTable() {
      return null;
   }
}
