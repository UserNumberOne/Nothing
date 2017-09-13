package net.minecraft.util;

import java.util.List;
import java.util.Random;

public class WeightedRandom {
   public static int getTotalWeight(List var0) {
      int i = 0;
      int j = 0;

      for(int k = collection.size(); j < k; ++j) {
         WeightedRandom.Item weightedrandom$item = (WeightedRandom.Item)collection.get(j);
         i += weightedrandom$item.itemWeight;
      }

      return i;
   }

   public static WeightedRandom.Item getRandomItem(Random var0, List var1, int var2) {
      if (totalWeight <= 0) {
         throw new IllegalArgumentException();
      } else {
         int i = random.nextInt(totalWeight);
         return getRandomItem(collection, i);
      }
   }

   public static WeightedRandom.Item getRandomItem(List var0, int var1) {
      int i = 0;

      for(int j = collection.size(); i < j; ++i) {
         WeightedRandom.Item t = (T)((WeightedRandom.Item)collection.get(i));
         weight -= t.itemWeight;
         if (weight < 0) {
            return t;
         }
      }

      return (WeightedRandom.Item)null;
   }

   public static WeightedRandom.Item getRandomItem(Random var0, List var1) {
      return getRandomItem(random, collection, getTotalWeight(collection));
   }

   public static class Item {
      public int itemWeight;

      public Item(int var1) {
         this.itemWeight = itemWeightIn;
      }
   }
}
