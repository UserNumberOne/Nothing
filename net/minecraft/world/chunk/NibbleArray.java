package net.minecraft.world.chunk;

public class NibbleArray {
   private final byte[] data;

   public NibbleArray() {
      this.data = new byte[2048];
   }

   public NibbleArray(byte[] var1) {
      this.data = storageArray;
      if (storageArray.length != 2048) {
         throw new IllegalArgumentException("ChunkNibbleArrays should be 2048 bytes not: " + storageArray.length);
      }
   }

   public int get(int var1, int var2, int var3) {
      return this.getFromIndex(this.getCoordinateIndex(x, y, z));
   }

   public void set(int var1, int var2, int var3, int var4) {
      this.setIndex(this.getCoordinateIndex(x, y, z), value);
   }

   private int getCoordinateIndex(int var1, int var2, int var3) {
      return y << 8 | z << 4 | x;
   }

   public int getFromIndex(int var1) {
      int i = this.getNibbleIndex(index);
      return this.isLowerNibble(index) ? this.data[i] & 15 : this.data[i] >> 4 & 15;
   }

   public void setIndex(int var1, int var2) {
      int i = this.getNibbleIndex(index);
      if (this.isLowerNibble(index)) {
         this.data[i] = (byte)(this.data[i] & 240 | value & 15);
      } else {
         this.data[i] = (byte)(this.data[i] & 15 | (value & 15) << 4);
      }

   }

   private boolean isLowerNibble(int var1) {
      return (index & 1) == 0;
   }

   private int getNibbleIndex(int var1) {
      return index >> 1;
   }

   public byte[] getData() {
      return this.data;
   }
}
