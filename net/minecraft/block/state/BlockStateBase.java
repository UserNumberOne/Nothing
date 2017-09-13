package net.minecraft.block.state;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
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
         if (var1 == null) {
            return "<NULL>";
         } else {
            IProperty var2 = (IProperty)var1.getKey();
            return var2.getName() + "=" + this.getPropertyName(var2, (Comparable)var1.getValue());
         }
      }

      private String getPropertyName(IProperty var1, Comparable var2) {
         return var1.getName(var2);
      }

      // $FF: synthetic method
      public Object apply(Object var1) {
         return this.apply((Entry)var1);
      }
   };

   public IBlockState cycleProperty(IProperty var1) {
      return this.withProperty(var1, (Comparable)cyclePropertyValue(var1.getAllowedValues(), this.getValue(var1)));
   }

   protected static Object cyclePropertyValue(Collection var0, Object var1) {
      Iterator var2 = var0.iterator();

      while(var2.hasNext()) {
         if (var2.next().equals(var1)) {
            if (var2.hasNext()) {
               return var2.next();
            }

            return var0.iterator().next();
         }
      }

      return var2.next();
   }

   public String toString() {
      StringBuilder var1 = new StringBuilder();
      var1.append(Block.REGISTRY.getNameForObject(this.getBlock()));
      if (!this.getProperties().isEmpty()) {
         var1.append("[");
         COMMA_JOINER.appendTo(var1, Iterables.transform(this.getProperties().entrySet(), MAP_ENTRY_TO_STRING));
         var1.append("]");
      }

      return var1.toString();
   }
}
