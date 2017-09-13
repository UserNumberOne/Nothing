package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;

public class ItemStackHelper {
   @Nullable
   public static ItemStack getAndSplit(ItemStack[] var0, int var1, int var2) {
      if (var1 >= 0 && var1 < var0.length && var0[var1] != null && var2 > 0) {
         ItemStack var3 = var0[var1].splitStack(var2);
         if (var0[var1].stackSize == 0) {
            var0[var1] = null;
         }

         return var3;
      } else {
         return null;
      }
   }

   @Nullable
   public static ItemStack getAndRemove(ItemStack[] var0, int var1) {
      if (var1 >= 0 && var1 < var0.length) {
         ItemStack var2 = var0[var1];
         var0[var1] = null;
         return var2;
      } else {
         return null;
      }
   }
}
