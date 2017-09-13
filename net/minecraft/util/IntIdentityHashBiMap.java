package net.minecraft.util;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import java.util.Iterator;
import javax.annotation.Nullable;
import net.minecraft.util.math.MathHelper;

public class IntIdentityHashBiMap implements IObjectIntIterable, Iterable {
   private static final Object EMPTY = null;
   private Object[] values;
   private int[] intKeys;
   private Object[] byId;
   private int nextFreeIndex;
   private int mapSize;

   public IntIdentityHashBiMap(int var1) {
      var1 = (int)((float)var1 / 0.8F);
      this.values = new Object[var1];
      this.intKeys = new int[var1];
      this.byId = new Object[var1];
   }

   public int getId(Object var1) {
      return this.getValue(this.getIndex(var1, this.hashObject(var1)));
   }

   @Nullable
   public Object get(int var1) {
      return var1 >= 0 && var1 < this.byId.length ? this.byId[var1] : null;
   }

   private int getValue(int var1) {
      return var1 == -1 ? -1 : this.intKeys[var1];
   }

   public int add(Object var1) {
      int var2 = this.nextId();
      this.put(var1, var2);
      return var2;
   }

   private int nextId() {
      while(this.nextFreeIndex < this.byId.length && this.byId[this.nextFreeIndex] != null) {
         ++this.nextFreeIndex;
      }

      return this.nextFreeIndex;
   }

   private void grow(int var1) {
      Object[] var2 = this.values;
      int[] var3 = this.intKeys;
      this.values = new Object[var1];
      this.intKeys = new int[var1];
      this.byId = new Object[var1];
      this.nextFreeIndex = 0;
      this.mapSize = 0;

      for(int var4 = 0; var4 < var2.length; ++var4) {
         if (var2[var4] != null) {
            this.put(var2[var4], var3[var4]);
         }
      }

   }

   public void put(Object var1, int var2) {
      int var3 = Math.max(var2, this.mapSize + 1);
      if ((float)var3 >= (float)this.values.length * 0.8F) {
         int var4;
         for(var4 = this.values.length << 1; var4 < var2; var4 <<= 1) {
            ;
         }

         this.grow(var4);
      }

      int var5 = this.findEmpty(this.hashObject(var1));
      this.values[var5] = var1;
      this.intKeys[var5] = var2;
      this.byId[var2] = var1;
      ++this.mapSize;
      if (var2 == this.nextFreeIndex) {
         ++this.nextFreeIndex;
      }

   }

   private int hashObject(Object var1) {
      return (MathHelper.hash(System.identityHashCode(var1)) & Integer.MAX_VALUE) % this.values.length;
   }

   private int getIndex(Object var1, int var2) {
      for(int var3 = var2; var3 < this.values.length; ++var3) {
         if (this.values[var3] == var1) {
            return var3;
         }

         if (this.values[var3] == EMPTY) {
            return -1;
         }
      }

      for(int var4 = 0; var4 < var2; ++var4) {
         if (this.values[var4] == var1) {
            return var4;
         }

         if (this.values[var4] == EMPTY) {
            return -1;
         }
      }

      return -1;
   }

   private int findEmpty(int var1) {
      for(int var2 = var1; var2 < this.values.length; ++var2) {
         if (this.values[var2] == EMPTY) {
            return var2;
         }
      }

      for(int var3 = 0; var3 < var1; ++var3) {
         if (this.values[var3] == EMPTY) {
            return var3;
         }
      }

      throw new RuntimeException("Overflowed :(");
   }

   public Iterator iterator() {
      return Iterators.filter(Iterators.forArray(this.byId), Predicates.notNull());
   }

   public int size() {
      return this.mapSize;
   }
}
