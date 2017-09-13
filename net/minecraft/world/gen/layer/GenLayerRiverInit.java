package net.minecraft.world.gen.layer;

public class GenLayerRiverInit extends GenLayer {
   public GenLayerRiverInit(long var1, GenLayer var3) {
      super(p_i2127_1_);
      this.parent = p_i2127_3_;
   }

   public int[] getInts(int var1, int var2, int var3, int var4) {
      int[] aint = this.parent.getInts(areaX, areaY, areaWidth, areaHeight);
      int[] aint1 = IntCache.getIntCache(areaWidth * areaHeight);

      for(int i = 0; i < areaHeight; ++i) {
         for(int j = 0; j < areaWidth; ++j) {
            this.initChunkSeed((long)(j + areaX), (long)(i + areaY));
            aint1[j + i * areaWidth] = aint[j + i * areaWidth] > 0 ? this.nextInt(299999) + 2 : 0;
         }
      }

      return aint1;
   }
}
