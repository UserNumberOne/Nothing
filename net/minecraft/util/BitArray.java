package net.minecraft.util;

import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.Validate;

public class BitArray {
   private final long[] longArray;
   private final int bitsPerEntry;
   private final long maxEntryValue;
   private final int arraySize;

   public BitArray(int var1, int var2) {
      Validate.inclusiveBetween(1L, 32L, (long)var1);
      this.arraySize = var2;
      this.bitsPerEntry = var1;
      this.maxEntryValue = (1L << var1) - 1L;
      this.longArray = new long[MathHelper.roundUp(var2 * var1, 64) / 64];
   }

   public void setAt(int var1, int var2) {
      Validate.inclusiveBetween(0L, (long)(this.arraySize - 1), (long)var1);
      Validate.inclusiveBetween(0L, this.maxEntryValue, (long)var2);
      int var3 = var1 * this.bitsPerEntry;
      int var4 = var3 / 64;
      int var5 = ((var1 + 1) * this.bitsPerEntry - 1) / 64;
      int var6 = var3 % 64;
      this.longArray[var4] = this.longArray[var4] & ~(this.maxEntryValue << var6) | ((long)var2 & this.maxEntryValue) << var6;
      if (var4 != var5) {
         int var7 = 64 - var6;
         int var8 = this.bitsPerEntry - var7;
         this.longArray[var5] = this.longArray[var5] >>> var8 << var8 | ((long)var2 & this.maxEntryValue) >> var7;
      }

   }

   public int getAt(int var1) {
      Validate.inclusiveBetween(0L, (long)(this.arraySize - 1), (long)var1);
      int var2 = var1 * this.bitsPerEntry;
      int var3 = var2 / 64;
      int var4 = ((var1 + 1) * this.bitsPerEntry - 1) / 64;
      int var5 = var2 % 64;
      if (var3 == var4) {
         return (int)(this.longArray[var3] >>> var5 & this.maxEntryValue);
      } else {
         int var6 = 64 - var5;
         return (int)((this.longArray[var3] >>> var5 | this.longArray[var4] << var6) & this.maxEntryValue);
      }
   }

   public long[] getBackingLongArray() {
      return this.longArray;
   }

   public int size() {
      return this.arraySize;
   }
}
