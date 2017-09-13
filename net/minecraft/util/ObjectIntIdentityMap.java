package net.minecraft.util;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;

public class ObjectIntIdentityMap implements IObjectIntIterable {
   protected final IdentityHashMap identityMap;
   protected final List objectList;

   public ObjectIntIdentityMap() {
      this(512);
   }

   public ObjectIntIdentityMap(int var1) {
      this.objectList = Lists.newArrayListWithExpectedSize(expectedSize);
      this.identityMap = new IdentityHashMap(expectedSize);
   }

   public void put(Object var1, int var2) {
      this.identityMap.put(key, Integer.valueOf(value));

      while(this.objectList.size() <= value) {
         this.objectList.add((Object)null);
      }

      this.objectList.set(value, key);
   }

   public int get(Object var1) {
      Integer integer = (Integer)this.identityMap.get(key);
      return integer == null ? -1 : integer.intValue();
   }

   @Nullable
   public final Object getByValue(int var1) {
      return value >= 0 && value < this.objectList.size() ? this.objectList.get(value) : null;
   }

   public Iterator iterator() {
      return Iterators.filter(this.objectList.iterator(), Predicates.notNull());
   }

   public int size() {
      return this.identityMap.size();
   }
}
