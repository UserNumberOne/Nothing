package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.util.math.MathHelper;

public class NBTTagDouble extends NBTPrimitive {
   private double data;

   NBTTagDouble() {
   }

   public NBTTagDouble(double var1) {
      this.data = data;
   }

   void write(DataOutput var1) throws IOException {
      output.writeDouble(this.data);
   }

   void read(DataInput var1, int var2, NBTSizeTracker var3) throws IOException {
      sizeTracker.read(128L);
      this.data = input.readDouble();
   }

   public byte getId() {
      return 6;
   }

   public String toString() {
      return "" + this.data + "d";
   }

   public NBTTagDouble copy() {
      return new NBTTagDouble(this.data);
   }

   public boolean equals(Object var1) {
      if (super.equals(p_equals_1_)) {
         NBTTagDouble nbttagdouble = (NBTTagDouble)p_equals_1_;
         return this.data == nbttagdouble.data;
      } else {
         return false;
      }
   }

   public int hashCode() {
      long i = Double.doubleToLongBits(this.data);
      return super.hashCode() ^ (int)(i ^ i >>> 32);
   }

   public long getLong() {
      return (long)Math.floor(this.data);
   }

   public int getInt() {
      return MathHelper.floor(this.data);
   }

   public short getShort() {
      return (short)(MathHelper.floor(this.data) & '\uffff');
   }

   public byte getByte() {
      return (byte)(MathHelper.floor(this.data) & 255);
   }

   public double getDouble() {
      return this.data;
   }

   public float getFloat() {
      return (float)this.data;
   }
}
