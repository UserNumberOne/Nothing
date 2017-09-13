package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.util.math.MathHelper;

public class NBTTagFloat extends NBTPrimitive {
   private float data;

   NBTTagFloat() {
   }

   public NBTTagFloat(float var1) {
      this.data = data;
   }

   void write(DataOutput var1) throws IOException {
      output.writeFloat(this.data);
   }

   void read(DataInput var1, int var2, NBTSizeTracker var3) throws IOException {
      sizeTracker.read(96L);
      this.data = input.readFloat();
   }

   public byte getId() {
      return 5;
   }

   public String toString() {
      return "" + this.data + "f";
   }

   public NBTTagFloat copy() {
      return new NBTTagFloat(this.data);
   }

   public boolean equals(Object var1) {
      if (super.equals(p_equals_1_)) {
         NBTTagFloat nbttagfloat = (NBTTagFloat)p_equals_1_;
         return this.data == nbttagfloat.data;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return super.hashCode() ^ Float.floatToIntBits(this.data);
   }

   public long getLong() {
      return (long)this.data;
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
      return (double)this.data;
   }

   public float getFloat() {
      return this.data;
   }
}
