package net.minecraft.potion;

public class PotionHealth extends Potion {
   public PotionHealth(boolean var1, int var2) {
      super(isBadEffectIn, liquidColorIn);
   }

   public boolean isInstant() {
      return true;
   }

   public boolean isReady(int var1, int var2) {
      return duration >= 1;
   }
}
