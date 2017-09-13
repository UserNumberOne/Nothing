package net.minecraft.inventory;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftHumanEntity;
import org.bukkit.inventory.InventoryHolder;

public class InventoryLargeChest implements ILockableContainer {
   private final String name;
   public final ILockableContainer upperChest;
   public final ILockableContainer lowerChest;
   public List transaction = new ArrayList();

   public ItemStack[] getContents() {
      ItemStack[] var1 = new ItemStack[this.getSizeInventory()];

      for(int var2 = 0; var2 < var1.length; ++var2) {
         var1[var2] = this.getStackInSlot(var2);
      }

      return var1;
   }

   public void onOpen(CraftHumanEntity var1) {
      this.upperChest.onOpen(var1);
      this.lowerChest.onOpen(var1);
      this.transaction.add(var1);
   }

   public void onClose(CraftHumanEntity var1) {
      this.upperChest.onClose(var1);
      this.lowerChest.onClose(var1);
      this.transaction.remove(var1);
   }

   public List getViewers() {
      return this.transaction;
   }

   public InventoryHolder getOwner() {
      return null;
   }

   public void setMaxStackSize(int var1) {
      this.upperChest.setMaxStackSize(var1);
      this.lowerChest.setMaxStackSize(var1);
   }

   public Location getLocation() {
      return this.upperChest.getLocation();
   }

   public InventoryLargeChest(String var1, ILockableContainer var2, ILockableContainer var3) {
      this.name = var1;
      if (var2 == null) {
         var2 = var3;
      }

      if (var3 == null) {
         var3 = var2;
      }

      this.upperChest = var2;
      this.lowerChest = var3;
      if (var2.isLocked()) {
         var3.setLockCode(var2.getLockCode());
      } else if (var3.isLocked()) {
         var2.setLockCode(var3.getLockCode());
      }

   }

   public int getSizeInventory() {
      return this.upperChest.getSizeInventory() + this.lowerChest.getSizeInventory();
   }

   public boolean isPartOfLargeChest(IInventory var1) {
      return this.upperChest == var1 || this.lowerChest == var1;
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
      return var1 >= this.upperChest.getSizeInventory() ? this.lowerChest.getStackInSlot(var1 - this.upperChest.getSizeInventory()) : this.upperChest.getStackInSlot(var1);
   }

   @Nullable
   public ItemStack decrStackSize(int var1, int var2) {
      return var1 >= this.upperChest.getSizeInventory() ? this.lowerChest.decrStackSize(var1 - this.upperChest.getSizeInventory(), var2) : this.upperChest.decrStackSize(var1, var2);
   }

   @Nullable
   public ItemStack removeStackFromSlot(int var1) {
      return var1 >= this.upperChest.getSizeInventory() ? this.lowerChest.removeStackFromSlot(var1 - this.upperChest.getSizeInventory()) : this.upperChest.removeStackFromSlot(var1);
   }

   public void setInventorySlotContents(int var1, @Nullable ItemStack var2) {
      if (var1 >= this.upperChest.getSizeInventory()) {
         this.lowerChest.setInventorySlotContents(var1 - this.upperChest.getSizeInventory(), var2);
      } else {
         this.upperChest.setInventorySlotContents(var1, var2);
      }

   }

   public int getInventoryStackLimit() {
      return Math.min(this.upperChest.getInventoryStackLimit(), this.lowerChest.getInventoryStackLimit());
   }

   public void markDirty() {
      this.upperChest.markDirty();
      this.lowerChest.markDirty();
   }

   public boolean isUsableByPlayer(EntityPlayer var1) {
      return this.upperChest.isUsableByPlayer(var1) && this.lowerChest.isUsableByPlayer(var1);
   }

   public void openInventory(EntityPlayer var1) {
      this.upperChest.openInventory(var1);
      this.lowerChest.openInventory(var1);
   }

   public void closeInventory(EntityPlayer var1) {
      this.upperChest.closeInventory(var1);
      this.lowerChest.closeInventory(var1);
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
      this.upperChest.setLockCode(var1);
      this.lowerChest.setLockCode(var1);
   }

   public LockCode getLockCode() {
      return this.upperChest.getLockCode();
   }

   public String getGuiID() {
      return this.upperChest.getGuiID();
   }

   public Container createContainer(InventoryPlayer var1, EntityPlayer var2) {
      return new ContainerChest(var1, this, var2);
   }

   public void clear() {
      this.upperChest.clear();
      this.lowerChest.clear();
   }
}
