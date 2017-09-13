package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;

public class InventoryLargeChest implements ILockableContainer {
   private final String name;
   private final ILockableContainer upperChest;
   private final ILockableContainer lowerChest;

   public InventoryLargeChest(String var1, ILockableContainer var2, ILockableContainer var3) {
      this.name = nameIn;
      if (upperChestIn == null) {
         upperChestIn = lowerChestIn;
      }

      if (lowerChestIn == null) {
         lowerChestIn = upperChestIn;
      }

      this.upperChest = upperChestIn;
      this.lowerChest = lowerChestIn;
      if (upperChestIn.isLocked()) {
         lowerChestIn.setLockCode(upperChestIn.getLockCode());
      } else if (lowerChestIn.isLocked()) {
         upperChestIn.setLockCode(lowerChestIn.getLockCode());
      }

   }

   public int getSizeInventory() {
      return this.upperChest.getSizeInventory() + this.lowerChest.getSizeInventory();
   }

   public boolean isPartOfLargeChest(IInventory var1) {
      return this.upperChest == inventoryIn || this.lowerChest == inventoryIn;
   }

   public String getName() {
      return this.upperChest.hasCustomName() ? this.upperChest.getName() : (this.lowerChest.hasCustomName() ? this.lowerChest.getName() : this.name);
   }

   public boolean hasCustomName() {
      return this.upperChest.hasCustomName() || this.lowerChest.hasCustomName();
   }

   public ITextComponent getDisplayName() {
      return (ITextComponent)(this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName(), new Object[0]));
   }

   @Nullable
   public ItemStack getStackInSlot(int var1) {
      return index >= this.upperChest.getSizeInventory() ? this.lowerChest.getStackInSlot(index - this.upperChest.getSizeInventory()) : this.upperChest.getStackInSlot(index);
   }

   @Nullable
   public ItemStack decrStackSize(int var1, int var2) {
      return index >= this.upperChest.getSizeInventory() ? this.lowerChest.decrStackSize(index - this.upperChest.getSizeInventory(), count) : this.upperChest.decrStackSize(index, count);
   }

   @Nullable
   public ItemStack removeStackFromSlot(int var1) {
      return index >= this.upperChest.getSizeInventory() ? this.lowerChest.removeStackFromSlot(index - this.upperChest.getSizeInventory()) : this.upperChest.removeStackFromSlot(index);
   }

   public void setInventorySlotContents(int var1, @Nullable ItemStack var2) {
      if (index >= this.upperChest.getSizeInventory()) {
         this.lowerChest.setInventorySlotContents(index - this.upperChest.getSizeInventory(), stack);
      } else {
         this.upperChest.setInventorySlotContents(index, stack);
      }

   }

   public int getInventoryStackLimit() {
      return this.upperChest.getInventoryStackLimit();
   }

   public void markDirty() {
      this.upperChest.markDirty();
      this.lowerChest.markDirty();
   }

   public boolean isUsableByPlayer(EntityPlayer var1) {
      return this.upperChest.isUsableByPlayer(player) && this.lowerChest.isUsableByPlayer(player);
   }

   public void openInventory(EntityPlayer var1) {
      this.upperChest.openInventory(player);
      this.lowerChest.openInventory(player);
   }

   public void closeInventory(EntityPlayer var1) {
      this.upperChest.closeInventory(player);
      this.lowerChest.closeInventory(player);
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

   public boolean isLocked() {
      return this.upperChest.isLocked() || this.lowerChest.isLocked();
   }

   public void setLockCode(LockCode var1) {
      this.upperChest.setLockCode(code);
      this.lowerChest.setLockCode(code);
   }

   public LockCode getLockCode() {
      return this.upperChest.getLockCode();
   }

   public String getGuiID() {
      return this.upperChest.getGuiID();
   }

   public Container createContainer(InventoryPlayer var1, EntityPlayer var2) {
      return new ContainerChest(playerInventory, this, playerIn);
   }

   public void clear() {
      this.upperChest.clear();
      this.lowerChest.clear();
   }
}
