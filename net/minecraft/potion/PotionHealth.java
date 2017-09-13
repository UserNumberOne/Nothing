package net.minecraft.potion;

public class PotionHealth extends Potion {
   public PotionHealth(boolean var1, int var2) {
      super(var1, var2);
   }

   public boolean isInstant() {
      return true;
   }

   public boolean isReady(int var1, int var2) {
      return var1 >= 1;
   }
}
