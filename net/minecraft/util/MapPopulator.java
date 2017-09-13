package net.minecraft.util;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class MapPopulator {
   public static Map createMap(Iterable var0, Iterable var1) {
      return populateMap(keys, values, Maps.newLinkedHashMap());
   }

   public static Map populateMap(Iterable var0, Iterable var1, Map var2) {
      Iterator iterator = values.iterator();

      for(Object k : keys) {
         map.put(k, iterator.next());
      }

      if (iterator.hasNext()) {
         throw new NoSuchElementException();
      } else {
         return map;
      }
   }
}
