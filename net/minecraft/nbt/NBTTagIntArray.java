package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class NBTTagIntArray extends NBTBase {
   private int[] intArray;

   NBTTagIntArray() {
   }

   public NBTTagIntArray(int[] var1) {
      this.intArray = p_i45132_1_;
   }

   void write(DataOutput var1) throws IOException {
      output.writeInt(this.intArray.length);

      for(int i : this.intArray) {
         output.writeInt(i);
      }

   }

   void read(DataInput var1, int var2, NBTSizeTracker var3) throws IOException {
      sizeTracker.read(192L);
      int i = input.readInt();
      sizeTracker.read((long)(32 * i));
      this.intArray = new int[i];

      for(int j = 0; j < i; ++j) {
         this.intArray[j] = input.readInt();
      }

   }

   public byte getId() {
      return 11;
   }

   public String toString() {
      String s = "[";

      for(int i : this.intArray) {
         s = s + i + ",";
      }

      return s + "]";
   }

   public NBTTagIntArray copy() {
      int[] aint = new int[this.intArray.length];
      System.arraycopy(this.intArray, 0, aint, 0, this.intArray.length);
      return new NBTTagIntArray(aint);
   }

   public boolean equals(Object var1) {
      return super.equals(p_equals_1_) ? Arrays.equals(this.intArray, ((NBTTagIntArray)p_equals_1_).intArray) : false;
   }

   public int hashCode() {
      return super.hashCode() ^ Arrays.hashCode(this.intArray);
   }

   public int[] getIntArray() {
      return this.intArray;
   }
}
