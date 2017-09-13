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
   private static final Set ALL_KNOWN = Sets.newConcurrentHashSet();
   private final Map map = Maps.newHashMap();
   private final Set knownKeys = Sets.newIdentityHashSet();
   private final Class baseClass;
   private final List values = Lists.newArrayList();

   public ClassInheritanceMultiMap(Class oclass) {
      this.baseClass = oclass;
      this.knownKeys.add(oclass);
      this.map.put(oclass, this.values);

      for(Class oclass1 : ALL_KNOWN) {
         this.createLookup(oclass1);
      }

   }

   protected void createLookup(Class oclass) {
      ALL_KNOWN.add(oclass);

      for(Object object : this.values) {
         if (oclass.isAssignableFrom(object.getClass())) {
            this.addForClass(object, oclass);
         }
      }

      this.knownKeys.add(oclass);
   }

   protected Class initializeClassLookup(Class oclass) {
      if (this.baseClass.isAssignableFrom(oclass)) {
         if (!this.knownKeys.contains(oclass)) {
            this.createLookup(oclass);
         }

         return oclass;
      } else {
         throw new IllegalArgumentException("Don't know how to search for " + oclass);
      }
   }

   public boolean add(Object t0) {
      for(Class oclass : this.knownKeys) {
         if (oclass.isAssignableFrom(t0.getClass())) {
            this.addForClass(t0, oclass);
         }
      }

      return true;
   }

   private void addForClass(Object t0, Class oclass) {
      List list = (List)this.map.get(oclass);
      if (list == null) {
         this.map.put(oclass, Lists.newArrayList(new Object[]{t0}));
      } else {
         list.add(t0);
      }

   }

   public boolean remove(Object object) {
      Object object1 = object;
      boolean flag = false;

      for(Class oclass : this.knownKeys) {
         if (oclass.isAssignableFrom(object1.getClass())) {
            List list = (List)this.map.get(oclass);
            if (list != null && list.remove(object1)) {
               flag = true;
            }
         }
      }

      return flag;
   }

   public boolean contains(Object object) {
      return Iterators.contains(this.getByClass(object.getClass()).iterator(), object);
   }

   public Iterable getByClass(final Class oclass) {
      return new Iterable() {
         public Iterator iterator() {
            List list = (List)ClassInheritanceMultiMap.this.map.get(ClassInheritanceMultiMap.this.initializeClassLookup(oclass));
            if (list == null) {
               return Iterators.emptyIterator();
            } else {
               Iterator iterator = list.iterator();
               return Iterators.filter(iterator, oclass);
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
