package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class ContainerHopper extends Container {
   private final IInventory hopperInventory;

   public ContainerHopper(InventoryPlayer var1, IInventory var2, EntityPlayer var3) {
      this.hopperInventory = hopperInventoryIn;
      hopperInventoryIn.openInventory(player);
      int i = 51;

      for(int j = 0; j < hopperInventoryIn.getSizeInventory(); ++j) {
         this.addSlotToContainer(new Slot(hopperInventoryIn, j, 44 + j * 18, 20));
      }

      for(int l = 0; l < 3; ++l) {
         for(int k = 0; k < 9; ++k) {
            this.addSlotToContainer(new Slot(playerInventory, k + l * 9 + 9, 8 + k * 18, l * 18 + 51));
         }
      }

      for(int i1 = 0; i1 < 9; ++i1) {
         this.addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 109));
      }

   }

   public boolean canInteractWith(EntityPlayer var1) {
      return this.hopperInventory.isUsableByPlayer(playerIn);
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer var1, int var2) {
      ItemStack itemstack = null;
      Slot slot = (Slot)this.inventorySlots.get(index);
      if (slot != null && slot.getHasStack()) {
         ItemStack itemstack1 = slot.getStack();
         itemstack = itemstack1.copy();
         if (index < this.hopperInventory.getSizeInventory()) {
            if (!this.mergeItemStack(itemstack1, this.hopperInventory.getSizeInventory(), this.inventorySlots.size(), true)) {
               return null;
            }
         } else if (!this.mergeItemStack(itemstack1, 0, this.hopperInventory.getSizeInventory(), false)) {
            return null;
         }

         if (itemstack1.stackSize == 0) {
            slot.putStack((ItemStack)null);
         } else {
            slot.onSlotChanged();
         }
      }

      return itemstack;
   }

   public void onContainerClosed(EntityPlayer var1) {
      super.onContainerClosed(playerIn);
      this.hopperInventory.closeInventory(playerIn);
   }
}
