package net.minecraft.util;

import java.util.List;
import java.util.Random;

public class WeightedRandom {
   public static int getTotalWeight(List var0) {
      int var1 = 0;
      int var2 = 0;

      for(int var3 = var0.size(); var2 < var3; ++var2) {
         WeightedRandom.Item var4 = (WeightedRandom.Item)var0.get(var2);
         var1 += var4.itemWeight;
      }

      return var1;
   }

   public static WeightedRandom.Item getRandomItem(Random var0, List var1, int var2) {
      if (var2 <= 0) {
         throw new IllegalArgumentException();
      } else {
         int var3 = var0.nextInt(var2);
         return getRandomItem(var1, var3);
      }
   }

   public static WeightedRandom.Item getRandomItem(List var0, int var1) {
      int var2 = 0;

      for(int var3 = var0.size(); var2 < var3; ++var2) {
         WeightedRandom.Item var4 = (WeightedRandom.Item)var0.get(var2);
         var1 -= var4.itemWeight;
         if (var1 < 0) {
            return var4;
         }
      }

      return (WeightedRandom.Item)null;
   }

   public static WeightedRandom.Item getRandomItem(Random var0, List var1) {
      return getRandomItem(var0, var1, getTotalWeight(var1));
   }

   public static class Item {
      public int itemWeight;

      public Item(int var1) {
         this.itemWeight = var1;
      }
   }
}
