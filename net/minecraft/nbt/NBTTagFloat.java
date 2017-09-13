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
      this.data = var1;
   }

   void write(DataOutput var1) throws IOException {
      var1.writeFloat(this.data);
   }

   void read(DataInput var1, int var2, NBTSizeTracker var3) throws IOException {
      var3.read(96L);
      this.data = var1.readFloat();
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
      if (super.equals(var1)) {
         NBTTagFloat var2 = (NBTTagFloat)var1;
         return this.data == var2.data;
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

   // $FF: synthetic method
   public NBTBase copy() {
      return this.copy();
   }
}
