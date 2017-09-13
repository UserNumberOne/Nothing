package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

public class SlotFurnaceFuel extends Slot {
   public SlotFurnaceFuel(IInventory var1, int var2, int var3, int var4) {
      super(inventoryIn, slotIndex, xPosition, yPosition);
   }

   public boolean isItemValid(@Nullable ItemStack var1) {
      return TileEntityFurnace.isItemFuel(stack) || isBucket(stack);
   }

   public int getItemStackLimit(ItemStack var1) {
      return isBucket(stack) ? 1 : super.getItemStackLimit(stack);
   }

   public static boolean isBucket(ItemStack var0) {
      return stack != null && stack.getItem() != null && stack.getItem() == Items.BUCKET;
   }
}
