package net.minecraft.util;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;

public class ObjectIntIdentityMap implements IObjectIntIterable {
   private final IdentityHashMap identityMap;
   private final List objectList;

   public ObjectIntIdentityMap() {
      this(512);
   }

   public ObjectIntIdentityMap(int var1) {
      this.objectList = Lists.newArrayListWithExpectedSize(var1);
      this.identityMap = new IdentityHashMap(var1);
   }

   public void put(Object var1, int var2) {
      this.identityMap.put(var1, Integer.valueOf(var2));

      while(this.objectList.size() <= var2) {
         this.objectList.add((Object)null);
      }

      this.objectList.set(var2, var1);
   }

   public int get(Object var1) {
      Integer var2 = (Integer)this.identityMap.get(var1);
      return var2 == null ? -1 : var2.intValue();
   }

   @Nullable
   public final Object getByValue(int var1) {
      return var1 >= 0 && var1 < this.objectList.size() ? this.objectList.get(var1) : null;
   }

   public Iterator iterator() {
      return Iterators.filter(this.objectList.iterator(), Predicates.notNull());
   }

   public int size() {
      return this.identityMap.size();
   }
}
