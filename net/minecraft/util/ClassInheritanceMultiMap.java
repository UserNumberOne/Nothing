package net.minecraft.util;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassInheritanceMultiMap extends AbstractSet {
   private static final Set ALL_KNOWN = Sets.newHashSet();
   private final Map map = Maps.newHashMap();
   private final Set knownKeys = Sets.newIdentityHashSet();
   private final Class baseClass;
   private final List values = Lists.newArrayList();

   public ClassInheritanceMultiMap(Class var1) {
      this.baseClass = var1;
      this.knownKeys.add(var1);
      this.map.put(var1, this.values);

      for(Class var3 : ALL_KNOWN) {
         this.createLookup(var3);
      }

   }

   protected void createLookup(Class var1) {
      ALL_KNOWN.add(var1);

      for(Object var3 : this.values) {
         if (var1.isAssignableFrom(var3.getClass())) {
            this.addForClass(var3, var1);
         }
      }

      this.knownKeys.add(var1);
   }

   protected Class initializeClassLookup(Class var1) {
      if (this.baseClass.isAssignableFrom(var1)) {
         if (!this.knownKeys.contains(var1)) {
            this.createLookup(var1);
         }

         return var1;
      } else {
         throw new IllegalArgumentException("Don't know how to search for " + var1);
      }
   }

   public boolean add(Object var1) {
      for(Class var3 : this.knownKeys) {
         if (var3.isAssignableFrom(var1.getClass())) {
            this.addForClass(var1, var3);
         }
      }

      return true;
   }

   private void addForClass(Object var1, Class var2) {
      List var3 = (List)this.map.get(var2);
      if (var3 == null) {
         this.map.put(var2, Lists.newArrayList(new Object[]{var1}));
      } else {
         var3.add(var1);
      }

   }

   public boolean remove(Object var1) {
      Object var2 = var1;
      boolean var3 = false;

      for(Class var5 : this.knownKeys) {
         if (var5.isAssignableFrom(var2.getClass())) {
            List var6 = (List)this.map.get(var5);
            if (var6 != null && var6.remove(var2)) {
               var3 = true;
            }
         }
      }

      return var3;
   }

   public boolean contains(Object var1) {
      return Iterators.contains(this.getByClass(var1.getClass()).iterator(), var1);
   }

   public Iterable getByClass(final Class var1) {
      return new Iterable() {
         public Iterator iterator() {
            List var1x = (List)ClassInheritanceMultiMap.this.map.get(ClassInheritanceMultiMap.this.initializeClassLookup(var1));
            if (var1x == null) {
               return Iterators.emptyIterator();
            } else {
               Iterator var2 = var1x.iterator();
               return Iterators.filter(var2, var1);
            }
         }
      };
   }

   public Iterator iterator() {
      return this.values.isEmpty() ? Iterators.emptyIterator() : Iterators.unmodifiableIterator(this.values.iterator());
   }

   public int size() {
      return this.values.size();
   }
}
