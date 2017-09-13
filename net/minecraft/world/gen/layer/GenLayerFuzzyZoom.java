package net.minecraft.world.gen.layer;

public class GenLayerFuzzyZoom extends GenLayerZoom {
   public GenLayerFuzzyZoom(long var1, GenLayer var3) {
      super(p_i2123_1_, p_i2123_3_);
   }

   protected int selectModeOrRandom(int var1, int var2, int var3, int var4) {
      return this.selectRandom(new int[]{p_151617_1_, p_151617_2_, p_151617_3_, p_151617_4_});
   }
}
