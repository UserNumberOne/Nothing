package net.minecraft.util;

import javax.annotation.Nullable;

public class IntHashMap {
   private transient IntHashMap.Entry[] slots = new IntHashMap.Entry[16];
   private transient int count;
   private int threshold = 12;
   private final float growFactor = 0.75F;

   private static int computeHash(int var0) {
      integer = integer ^ integer >>> 20 ^ integer >>> 12;
      return integer ^ integer >>> 7 ^ integer >>> 4;
   }

   private static int getSlotIndex(int var0, int var1) {
      return hash & slotCount - 1;
   }

   @Nullable
   public Object lookup(int var1) {
      int i = computeHash(hashEntry);

      for(IntHashMap.Entry entry = this.slots[getSlotIndex(i, this.slots.length)]; entry != null; entry = entry.nextEntry) {
         if (entry.hashEntry == hashEntry) {
            return entry.valueEntry;
         }
      }

      return null;
   }

   public boolean containsItem(int var1) {
      return this.lookupEntry(hashEntry) != null;
   }

   @Nullable
   final IntHashMap.Entry lookupEntry(int var1) {
      int i = computeHash(hashEntry);

      for(IntHashMap.Entry entry = this.slots[getSlotIndex(i, this.slots.length)]; entry != null; entry = entry.nextEntry) {
         if (entry.hashEntry == hashEntry) {
            return entry;
         }
      }

      return null;
   }

   public void addKey(int var1, Object var2) {
      int i = computeHash(hashEntry);
      int j = getSlotIndex(i, this.slots.length);

      for(IntHashMap.Entry entry = this.slots[j]; entry != null; entry = entry.nextEntry) {
         if (entry.hashEntry == hashEntry) {
            entry.valueEntry = valueEntry;
            return;
         }
      }

      this.insert(i, hashEntry, valueEntry, j);
   }

   private void grow(int var1) {
      IntHashMap.Entry[] entry = this.slots;
      int i = entry.length;
      if (i == 1073741824) {
         this.threshold = Integer.MAX_VALUE;
      } else {
         IntHashMap.Entry[] entry1 = new IntHashMap.Entry[p_76047_1_];
         this.copyTo(entry1);
         this.slots = entry1;
         float var10001 = (float)p_76047_1_;
         this.getClass();
         this.threshold = (int)(var10001 * 0.75F);
      }

   }

   private void copyTo(IntHashMap.Entry[] var1) {
      IntHashMap.Entry[] entry = this.slots;
      int i = p_76048_1_.length;

      for(int j = 0; j < entry.length; ++j) {
         IntHashMap.Entry entry1 = entry[j];
         if (entry1 != null) {
            entry[j] = null;

            while(true) {
               IntHashMap.Entry entry2 = entry1.nextEntry;
               int k = getSlotIndex(entry1.slotHash, i);
               entry1.nextEntry = p_76048_1_[k];
               p_76048_1_[k] = entry1;
               entry1 = entry2;
               if (entry2 == null) {
                  break;
               }
            }
         }
      }

   }

   @Nullable
   public Object removeObject(int var1) {
      IntHashMap.Entry entry = this.removeEntry(p_76049_1_);
      return entry == null ? null : entry.valueEntry;
   }

   @Nullable
   final IntHashMap.Entry removeEntry(int var1) {
      int i = computeHash(p_76036_1_);
      int j = getSlotIndex(i, this.slots.length);
      IntHashMap.Entry entry = this.slots[j];

      IntHashMap.Entry entry1;
      IntHashMap.Entry entry2;
      for(entry1 = entry; entry1 != null; entry1 = entry2) {
         entry2 = entry1.nextEntry;
         if (entry1.hashEntry == p_76036_1_) {
            --this.count;
            if (entry == entry1) {
               this.slots[j] = entry2;
            } else {
               entry.nextEntry = entry2;
            }

            return entry1;
         }

         entry = entry1;
      }

      return entry1;
   }

   public void clearMap() {
      IntHashMap.Entry[] entry = this.slots;

      for(int i = 0; i < entry.length; ++i) {
         entry[i] = null;
      }

      this.count = 0;
   }

   private void insert(int var1, int var2, Object var3, int var4) {
      IntHashMap.Entry entry = this.slots[p_76040_4_];
      this.slots[p_76040_4_] = new IntHashMap.Entry(p_76040_1_, p_76040_2_, p_76040_3_, entry);
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
         this.valueEntry = p_i1552_3_;
         this.nextEntry = p_i1552_4_;
         this.hashEntry = p_i1552_2_;
         this.slotHash = p_i1552_1_;
      }

      public final int getHash() {
         return this.hashEntry;
      }

      public final Object getValue() {
         return this.valueEntry;
      }

      public final boolean equals(Object var1) {
         if (!(p_equals_1_ instanceof IntHashMap.Entry)) {
            return false;
         } else {
            IntHashMap.Entry entry = (IntHashMap.Entry)p_equals_1_;
            if (this.hashEntry == entry.hashEntry) {
               Object object = this.getValue();
               Object object1 = entry.getValue();
               if (object == object1 || object != null && object.equals(object1)) {
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
