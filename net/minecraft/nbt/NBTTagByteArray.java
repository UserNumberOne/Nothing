package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class NBTTagByteArray extends NBTBase {
   private byte[] data;

   NBTTagByteArray() {
   }

   public NBTTagByteArray(byte[] var1) {
      this.data = data;
   }

   void write(DataOutput var1) throws IOException {
      output.writeInt(this.data.length);
      output.write(this.data);
   }

   void read(DataInput var1, int var2, NBTSizeTracker var3) throws IOException {
      sizeTracker.read(192L);
      int i = input.readInt();
      sizeTracker.read((long)(8 * i));
      this.data = new byte[i];
      input.readFully(this.data);
   }

   public byte getId() {
      return 7;
   }

   public String toString() {
      return "[" + this.data.length + " bytes]";
   }

   public NBTBase copy() {
      byte[] abyte = new byte[this.data.length];
      System.arraycopy(this.data, 0, abyte, 0, this.data.length);
      return new NBTTagByteArray(abyte);
   }

   public boolean equals(Object var1) {
      return super.equals(p_equals_1_) ? Arrays.equals(this.data, ((NBTTagByteArray)p_equals_1_).data) : false;
   }

   public int hashCode() {
      return super.hashCode() ^ Arrays.hashCode(this.data);
   }

   public byte[] getByteArray() {
      return this.data;
   }
}
