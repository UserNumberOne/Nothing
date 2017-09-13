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
      this.baseClass = baseClassIn;
      this.knownKeys.add(baseClassIn);
      this.map.put(baseClassIn, this.values);

      for(Class oclass : ALL_KNOWN) {
         this.createLookup(oclass);
      }

   }

   protected void createLookup(Class var1) {
      ALL_KNOWN.add(clazz);

      for(Object t : this.values) {
         if (clazz.isAssignableFrom(t.getClass())) {
            this.addForClass(t, clazz);
         }
      }

      this.knownKeys.add(clazz);
   }

   protected Class initializeClassLookup(Class var1) {
      if (this.baseClass.isAssignableFrom(clazz)) {
         if (!this.knownKeys.contains(clazz)) {
            this.createLookup(clazz);
         }

         return clazz;
      } else {
         throw new IllegalArgumentException("Don't know how to search for " + clazz);
      }
   }

   public boolean add(Object var1) {
      for(Class oclass : this.knownKeys) {
         if (oclass.isAssignableFrom(p_add_1_.getClass())) {
            this.addForClass(p_add_1_, oclass);
         }
      }

      return true;
   }

   private void addForClass(Object var1, Class var2) {
      List list = (List)this.map.get(parentClass);
      if (list == null) {
         this.map.put(parentClass, Lists.newArrayList(new Object[]{value}));
      } else {
         list.add(value);
      }

   }

   public boolean remove(Object var1) {
      Object t = (T)p_remove_1_;
      boolean flag = false;

      for(Class oclass : this.knownKeys) {
         if (oclass.isAssignableFrom(t.getClass())) {
            List list = (List)this.map.get(oclass);
            if (list != null && list.remove(t)) {
               flag = true;
            }
         }
      }

      return flag;
   }

   public boolean contains(Object var1) {
      return Iterators.contains(this.getByClass(p_contains_1_.getClass()).iterator(), p_contains_1_);
   }

   public Iterable getByClass(final Class var1) {
      return new Iterable() {
         public Iterator iterator() {
            List list = (List)ClassInheritanceMultiMap.this.map.get(ClassInheritanceMultiMap.this.initializeClassLookup(clazz));
            if (list == null) {
               return Iterators.emptyIterator();
            } else {
               Iterator iterator = list.iterator();
               return Iterators.filter(iterator, clazz);
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
