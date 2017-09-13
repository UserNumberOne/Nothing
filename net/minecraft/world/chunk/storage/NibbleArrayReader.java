package net.minecraft.world.chunk.storage;

public class NibbleArrayReader {
   public final byte[] data;
   private final int depthBits;
   private final int depthBitsPlusFour;

   public NibbleArrayReader(byte[] var1, int var2) {
      this.data = dataIn;
      this.depthBits = depthBitsIn;
      this.depthBitsPlusFour = depthBitsIn + 4;
   }

   public int get(int var1, int var2, int var3) {
      int i = x << this.depthBitsPlusFour | z << this.depthBits | y;
      int j = i >> 1;
      int k = i & 1;
      return k == 0 ? this.data[j] & 15 : this.data[j] >> 4 & 15;
   }
}
