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

   public Slot(IInventory var1, int var2, int var3, int var4) {
      this.inventory = var1;
      this.slotIndex = var2;
      this.xPos = var3;
      this.yPos = var4;
   }

   public void onSlotChange(ItemStack var1, ItemStack var2) {
      if (var1 != null && var2 != null && var1.getItem() == var2.getItem()) {
         int var3 = var2.stackSize - var1.stackSize;
         if (var3 > 0) {
            this.onCrafting(var1, var3);
         }
      }

   }

   protected void onCrafting(ItemStack var1, int var2) {
   }

   protected void onCrafting(ItemStack var1) {
   }

   public void onPickupFromSlot(EntityPlayer var1, ItemStack var2) {
      this.onSlotChanged();
   }

   public boolean isItemValid(@Nullable ItemStack var1) {
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

   public void putStack(@Nullable ItemStack var1) {
      this.inventory.setInventorySlotContents(this.slotIndex, var1);
      this.onSlotChanged();
   }

   public void onSlotChanged() {
      this.inventory.markDirty();
   }

   public int getSlotStackLimit() {
      return this.inventory.getInventoryStackLimit();
   }

   public int getItemStackLimit(ItemStack var1) {
      return this.getSlotStackLimit();
   }

   public ItemStack decrStackSize(int var1) {
      return this.inventory.decrStackSize(this.slotIndex, var1);
   }

   public boolean isHere(IInventory var1, int var2) {
      return var1 == this.inventory && var2 == this.slotIndex;
   }

   public boolean canTakeStack(EntityPlayer var1) {
      return true;
   }
}
