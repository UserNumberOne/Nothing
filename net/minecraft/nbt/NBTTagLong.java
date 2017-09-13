package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagLong extends NBTPrimitive {
   private long data;

   NBTTagLong() {
   }

   public NBTTagLong(long var1) {
      this.data = data;
   }

   void write(DataOutput var1) throws IOException {
      output.writeLong(this.data);
   }

   void read(DataInput var1, int var2, NBTSizeTracker var3) throws IOException {
      sizeTracker.read(128L);
      this.data = input.readLong();
   }

   public byte getId() {
      return 4;
   }

   public String toString() {
      return "" + this.data + "L";
   }

   public NBTTagLong copy() {
      return new NBTTagLong(this.data);
   }

   public boolean equals(Object var1) {
      if (super.equals(p_equals_1_)) {
         NBTTagLong nbttaglong = (NBTTagLong)p_equals_1_;
         return this.data == nbttaglong.data;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return super.hashCode() ^ (int)(this.data ^ this.data >>> 32);
   }

   public long getLong() {
      return this.data;
   }

   public int getInt() {
      return (int)(this.data & -1L);
   }

   public short getShort() {
      return (short)((int)(this.data & 65535L));
   }

   public byte getByte() {
      return (byte)((int)(this.data & 255L));
   }

   public double getDouble() {
      return (double)this.data;
   }

   public float getFloat() {
      return (float)this.data;
   }
}
