package net.minecraft.util.math;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.annotation.Nullable;

public class Cartesian {
   public static Iterable cartesianProduct(Class var0, Iterable var1) {
      return new Cartesian.Product(var0, (Iterable[])toArray(Iterable.class, var1));
   }

   public static Iterable cartesianProduct(Iterable var0) {
      return arraysAsLists(cartesianProduct(Object.class, var0));
   }

   private static Iterable arraysAsLists(Iterable var0) {
      return Iterables.transform(var0, new Cartesian.GetList());
   }

   private static Object[] toArray(Class var0, Iterable var1) {
      ArrayList var2 = Lists.newArrayList();

      for(Object var4 : var1) {
         var2.add(var4);
      }

      return var2.toArray(createArray(var0, var2.size()));
   }

   private static Object[] createArray(Class var0, int var1) {
      return Array.newInstance(var0, var1);
   }

   static class GetList implements Function {
      private GetList() {
      }

      public List apply(@Nullable Object[] var1) {
         return Arrays.asList(var1);
      }
   }

   static class Product implements Iterable {
      private final Class clazz;
      private final Iterable[] iterables;

      private Product(Class var1, Iterable[] var2) {
         this.clazz = var1;
         this.iterables = var2;
      }

      public Iterator iterator() {
         return (Iterator)(this.iterables.length <= 0 ? Collections.singletonList(Cartesian.createArray(this.clazz, 0)).iterator() : new Cartesian.Product.ProductIterator(this.clazz, this.iterables));
      }

      static class ProductIterator extends UnmodifiableIterator {
         private int index;
         private final Iterable[] iterables;
         private final Iterator[] iterators;
         private final Object[] results;

         private ProductIterator(Class var1, Iterable[] var2) {
            this.index = -2;
            this.iterables = var2;
            this.iterators = (Iterator[])Cartesian.createArray(Iterator.class, this.iterables.length);

            for(int var3 = 0; var3 < this.iterables.length; ++var3) {
               this.iterators[var3] = var2[var3].iterator();
            }

            this.results = Cartesian.createArray(var1, this.iterators.length);
         }

         private void endOfData() {
            this.index = -1;
            Arrays.fill(this.iterators, (Object)null);
            Arrays.fill(this.results, (Object)null);
         }

         public boolean hasNext() {
            if (this.index == -2) {
               this.index = 0;

               for(Iterator var4 : this.iterators) {
                  if (!var4.hasNext()) {
                     this.endOfData();
                     break;
                  }
               }

               return true;
            } else {
               if (this.index >= this.iterators.length) {
                  for(this.index = this.iterators.length - 1; this.index >= 0; --this.index) {
                     Iterator var1 = this.iterators[this.index];
                     if (var1.hasNext()) {
                        break;
                     }

                     if (this.index == 0) {
                        this.endOfData();
                        break;
                     }

                     var1 = this.iterables[this.index].iterator();
                     this.iterators[this.index] = var1;
                     if (!var1.hasNext()) {
                        this.endOfData();
                        break;
                     }
                  }
               }

               return this.index >= 0;
            }
         }

         public Object[] next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            } else {
               while(this.index < this.iterators.length) {
                  this.results[this.index] = this.iterators[this.index].next();
                  ++this.index;
               }

               return this.results.clone();
            }
         }
      }
   }
}
