package net.minecraft.dispenser;

import net.minecraft.item.ItemStack;

public interface IBehaviorDispenseItem {
   IBehaviorDispenseItem DEFAULT_BEHAVIOR = new IBehaviorDispenseItem() {
      public ItemStack dispense(IBlockSource var1, ItemStack var2) {
         return stack;
      }
   };

   ItemStack dispense(IBlockSource var1, ItemStack var2);
}
