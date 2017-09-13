package net.minecraft.util;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class IntegerCache {
   private static final Integer[] CACHE = new Integer['\uffff'];

   public static Integer getInteger(int var0) {
      return var0 > 0 && var0 < CACHE.length ? CACHE[var0] : var0;
   }

   static {
      int var0 = 0;

      for(int var1 = CACHE.length; var0 < var1; ++var0) {
         CACHE[var0] = var0;
      }

   }
}
