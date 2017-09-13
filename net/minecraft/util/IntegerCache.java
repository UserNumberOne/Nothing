package net.minecraft.util;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class IntegerCache {
   private static final Integer[] CACHE = new Integer['\uffff'];

   public static Integer getInteger(int var0) {
      return value > 0 && value < CACHE.length ? CACHE[value] : value;
   }

   static {
      int i = 0;

      for(int j = CACHE.length; i < j; ++i) {
         CACHE[i] = i;
      }

   }
}
