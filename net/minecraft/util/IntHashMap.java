package net.minecraft.util;

import javax.annotation.Nullable;

public class IntHashMap {
   private transient IntHashMap.Entry[] slots = new IntHashMap.Entry[16];
   private transient int count;
   private int threshold = 12;
   private final float growFactor = 0.75F;

   private static int computeHash(int var0) {
      var0 = var0 ^ var0 >>> 20 ^ var0 >>> 12;
      return var0 ^ var0 >>> 7 ^ var0 >>> 4;
   }

   private static int getSlotIndex(int var0, int var1) {
      return var0 & var1 - 1;
   }

   @Nullable
   public Object lookup(int var1) {
      int var2 = computeHash(var1);

      for(IntHashMap.Entry var3 = this.slots[getSlotIndex(var2, this.slots.length)]; var3 != null; var3 = var3.nextEntry) {
         if (var3.hashEntry == var1) {
            return var3.valueEntry;
         }
      }

      return null;
   }

   public boolean containsItem(int var1) {
      return this.lookupEntry(var1) != null;
   }

   @Nullable
   final IntHashMap.Entry lookupEntry(int var1) {
      int var2 = computeHash(var1);

      for(IntHashMap.Entry var3 = this.slots[getSlotIndex(var2, this.slots.length)]; var3 != null; var3 = var3.nextEntry) {
         if (var3.hashEntry == var1) {
            return var3;
         }
      }

      return null;
   }

   public void addKey(int var1, Object var2) {
      int var3 = computeHash(var1);
      int var4 = getSlotIndex(var3, this.slots.length);

      for(IntHashMap.Entry var5 = this.slots[var4]; var5 != null; var5 = var5.nextEntry) {
         if (var5.hashEntry == var1) {
            var5.valueEntry = var2;
            return;
         }
      }

      this.insert(var3, var1, var2, var4);
   }

   private void grow(int var1) {
      IntHashMap.Entry[] var2 = this.slots;
      int var3 = var2.length;
      if (var3 == 1073741824) {
         this.threshold = Integer.MAX_VALUE;
      } else {
         IntHashMap.Entry[] var4 = new IntHashMap.Entry[var1];
         this.copyTo(var4);
         this.slots = var4;
         float var10001 = (float)var1;
         this.getClass();
         this.threshold = (int)(var10001 * 0.75F);
      }

   }

   private void copyTo(IntHashMap.Entry[] var1) {
      IntHashMap.Entry[] var2 = this.slots;
      int var3 = var1.length;

      for(int var4 = 0; var4 < var2.length; ++var4) {
         IntHashMap.Entry var5 = var2[var4];
         if (var5 != null) {
            var2[var4] = null;

            while(true) {
               IntHashMap.Entry var6 = var5.nextEntry;
               int var7 = getSlotIndex(var5.slotHash, var3);
               var5.nextEntry = var1[var7];
               var1[var7] = var5;
               var5 = var6;
               if (var6 == null) {
                  break;
               }
            }
         }
      }

   }

   @Nullable
   public Object removeObject(int var1) {
      IntHashMap.Entry var2 = this.removeEntry(var1);
      return var2 == null ? null : var2.valueEntry;
   }

   @Nullable
   final IntHashMap.Entry removeEntry(int var1) {
      int var2 = computeHash(var1);
      int var3 = getSlotIndex(var2, this.slots.length);
      IntHashMap.Entry var4 = this.slots[var3];

      IntHashMap.Entry var5;
      IntHashMap.Entry var6;
      for(var5 = var4; var5 != null; var5 = var6) {
         var6 = var5.nextEntry;
         if (var5.hashEntry == var1) {
            --this.count;
            if (var4 == var5) {
               this.slots[var3] = var6;
            } else {
               var4.nextEntry = var6;
            }

            return var5;
         }

         var4 = var5;
      }

      return var5;
   }

   public void clearMap() {
      IntHashMap.Entry[] var1 = this.slots;

      for(int var2 = 0; var2 < var1.length; ++var2) {
         var1[var2] = null;
      }

      this.count = 0;
   }

   private void insert(int var1, int var2, Object var3, int var4) {
      IntHashMap.Entry var5 = this.slots[var4];
      this.slots[var4] = new IntHashMap.Entry(var1, var2, var3, var5);
      if (this.count++ >= this.threshold) {
         this.grow(2 * this.slots.length);
      }

   }

   static class Entry {
      final int hashEntry;
      Object valueEntry;
      IntHashMap.Entry nextEntry;
      final int slotHash;

      Entry(int var1, int var2, Object var3, IntHashMap.Entry var4) {
         this.valueEntry = var3;
         this.nextEntry = var4;
         this.hashEntry = var2;
         this.slotHash = var1;
      }

      public final int getHash() {
         return this.hashEntry;
      }

      public final Object getValue() {
         return this.valueEntry;
      }

      public final boolean equals(Object var1) {
         if (!(var1 instanceof IntHashMap.Entry)) {
            return false;
         } else {
            IntHashMap.Entry var2 = (IntHashMap.Entry)var1;
            if (this.hashEntry == var2.hashEntry) {
               Object var3 = this.getValue();
               Object var4 = var2.getValue();
               if (var3 == var4 || var3 != null && var3.equals(var4)) {
                  return true;
               }
            }

            return false;
         }
      }

      public final int hashCode() {
         return IntHashMap.computeHash(this.hashEntry);
      }

      public final String toString() {
         return this.getHash() + "=" + this.getValue();
      }
   }
}
