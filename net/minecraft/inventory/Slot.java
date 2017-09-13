package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class Slot {
   public final int slotIndex;
   public final IInventory inventory;
   public int slotNumber;
   public int xPos;
   public int yPos;

   public Slot(IInventory iinventory, int i, int j, int k) {
      this.inventory = iinventory;
      this.slotIndex = i;
      this.xPos = j;
      this.yPos = k;
   }

   public void onSlotChange(ItemStack itemstack, ItemStack itemstack1) {
      if (itemstack != null && itemstack1 != null && itemstack.getItem() == itemstack1.getItem()) {
         int i = itemstack1.stackSize - itemstack.stackSize;
         if (i > 0) {
            this.onCrafting(itemstack, i);
         }
      }

   }

   protected void onCrafting(ItemStack itemstack, int i) {
   }

   protected void onCrafting(ItemStack itemstack) {
   }

   public void onPickupFromSlot(EntityPlayer entityhuman, ItemStack itemstack) {
      this.onSlotChanged();
   }

   public boolean isItemValid(@Nullable ItemStack itemstack) {
      return true;
   }

   @Nullable
   public ItemStack getStack() {
      return this.inventory.getStackInSlot(this.slotIndex);
   }

   public boolean getHasStack() {
      if (this.getStack() != null && this.getStack().stackSize == 0) {
         this.putStack((ItemStack)null);
      }

      return this.getStack() != null;
   }

   public void putStack(@Nullable ItemStack itemstack) {
      this.inventory.setInventorySlotContents(this.slotIndex, itemstack);
      this.onSlotChanged();
   }

   public void onSlotChanged() {
      this.inventory.markDirty();
   }

   public int getSlotStackLimit() {
      return this.inventory.getInventoryStackLimit();
   }

   public int getItemStackLimit(ItemStack itemstack) {
      return this.getSlotStackLimit();
   }

   public ItemStack decrStackSize(int i) {
      return this.inventory.decrStackSize(this.slotIndex, i);
   }

   public boolean isHere(IInventory iinventory, int i) {
      return iinventory == this.inventory && i == this.slotIndex;
   }

   public boolean canTakeStack(EntityPlayer entityhuman) {
      return true;
   }
}
