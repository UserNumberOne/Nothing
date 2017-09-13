package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class InventoryCrafting implements IInventory {
   private final ItemStack[] stackList;
   private final int inventoryWidth;
   private final int inventoryHeight;
   private final Container eventHandler;

   public InventoryCrafting(Container var1, int var2, int var3) {
      int i = width * height;
      this.stackList = new ItemStack[i];
      this.eventHandler = eventHandlerIn;
      this.inventoryWidth = width;
      this.inventoryHeight = height;
   }

   public int getSizeInventory() {
      return this.stackList.length;
   }

   @Nullable
   public ItemStack getStackInSlot(int var1) {
      return index >= this.getSizeInventory() ? null : this.stackList[index];
   }

   @Nullable
   public ItemStack getStackInRowAndColumn(int var1, int var2) {
      return row >= 0 && row < this.inventoryWidth && column >= 0 && column <= this.inventoryHeight ? this.getStackInSlot(row + column * this.inventoryWidth) : null;
   }

   public String getName() {
      return "container.crafting";
   }

   public boolean hasCustomName() {
      return false;
   }

   public ITextComponent getDisplayName() {
      return (ITextComponent)(this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName(), new Object[0]));
   }

   @Nullable
   public ItemStack removeStackFromSlot(int var1) {
      return ItemStackHelper.getAndRemove(this.stackList, index);
   }

   @Nullable
   public ItemStack decrStackSize(int var1, int var2) {
      ItemStack itemstack = ItemStackHelper.getAndSplit(this.stackList, index, count);
      if (itemstack != null) {
         this.eventHandler.onCraftMatrixChanged(this);
      }

      return itemstack;
   }

   public void setInventorySlotContents(int var1, @Nullable ItemStack var2) {
      this.stackList[index] = stack;
      this.eventHandler.onCraftMatrixChanged(this);
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public void markDirty() {
   }

   public boolean isUsableByPlayer(EntityPlayer var1) {
      return true;
   }

   public void openInventory(EntityPlayer var1) {
   }

   public void closeInventory(EntityPlayer var1) {
   }

   public boolean isItemValidForSlot(int var1, ItemStack var2) {
      return true;
   }

   public int getField(int var1) {
      return 0;
   }

   public void setField(int var1, int var2) {
   }

   public int getFieldCount() {
      return 0;
   }

   public void clear() {
      for(int i = 0; i < this.stackList.length; ++i) {
         this.stackList[i] = null;
      }

   }

   public int getHeight() {
      return this.inventoryHeight;
   }

   public int getWidth() {
      return this.inventoryWidth;
   }
}
