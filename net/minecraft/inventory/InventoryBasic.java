package net.minecraft.inventory;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftHumanEntity;
import org.bukkit.inventory.InventoryHolder;

public class InventoryBasic implements IInventory {
   private String inventoryTitle;
   private final int slotsCount;
   public final ItemStack[] inventoryContents;
   private List changeListeners;
   private boolean hasCustomName;
   public List transaction;
   private int maxStack;
   protected InventoryHolder bukkitOwner;

   public ItemStack[] getContents() {
      return this.inventoryContents;
   }

   public void onOpen(CraftHumanEntity who) {
      this.transaction.add(who);
   }

   public void onClose(CraftHumanEntity who) {
      this.transaction.remove(who);
   }

   public List getViewers() {
      return this.transaction;
   }

   public void setMaxStackSize(int i) {
      this.maxStack = i;
   }

   public InventoryHolder getOwner() {
      return this.bukkitOwner;
   }

   public Location getLocation() {
      return null;
   }

   public InventoryBasic(String s, boolean flag, int i) {
      this(s, flag, i, (InventoryHolder)null);
   }

   public InventoryBasic(String s, boolean flag, int i, InventoryHolder owner) {
      this.transaction = new ArrayList();
      this.maxStack = 64;
      this.bukkitOwner = owner;
      this.inventoryTitle = s;
      this.hasCustomName = flag;
      this.slotsCount = i;
      this.inventoryContents = new ItemStack[i];
   }

   public void addInventoryChangeListener(IInventoryChangedListener iinventorylistener) {
      if (this.changeListeners == null) {
         this.changeListeners = Lists.newArrayList();
      }

      this.changeListeners.add(iinventorylistener);
   }

   public void removeInventoryChangeListener(IInventoryChangedListener iinventorylistener) {
      this.changeListeners.remove(iinventorylistener);
   }

   @Nullable
   public ItemStack getStackInSlot(int i) {
      return i >= 0 && i < this.inventoryContents.length ? this.inventoryContents[i] : null;
   }

   @Nullable
   public ItemStack decrStackSize(int i, int j) {
      ItemStack itemstack = ItemStackHelper.getAndSplit(this.inventoryContents, i, j);
      if (itemstack != null) {
         this.markDirty();
      }

      return itemstack;
   }

   @Nullable
   public ItemStack addItem(ItemStack itemstack) {
      ItemStack itemstack1 = itemstack.copy();

      for(int i = 0; i < this.slotsCount; ++i) {
         ItemStack itemstack2 = this.getStackInSlot(i);
         if (itemstack2 == null) {
            this.setInventorySlotContents(i, itemstack1);
            this.markDirty();
            return null;
         }

         if (ItemStack.areItemsEqual(itemstack2, itemstack1)) {
            int j = Math.min(this.getInventoryStackLimit(), itemstack2.getMaxStackSize());
            int k = Math.min(itemstack1.stackSize, j - itemstack2.stackSize);
            if (k > 0) {
               itemstack2.stackSize += k;
               itemstack1.stackSize -= k;
               if (itemstack1.stackSize <= 0) {
                  this.markDirty();
                  return null;
               }
            }
         }
      }

      if (itemstack1.stackSize != itemstack.stackSize) {
         this.markDirty();
      }

      return itemstack1;
   }

   @Nullable
   public ItemStack removeStackFromSlot(int i) {
      if (this.inventoryContents[i] != null) {
         ItemStack itemstack = this.inventoryContents[i];
         this.inventoryContents[i] = null;
         return itemstack;
      } else {
         return null;
      }
   }

   public void setInventorySlotContents(int i, @Nullable ItemStack itemstack) {
      this.inventoryContents[i] = itemstack;
      if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit()) {
         itemstack.stackSize = this.getInventoryStackLimit();
      }

      this.markDirty();
   }

   public int getSizeInventory() {
      return this.slotsCount;
   }

   public String getName() {
      return this.inventoryTitle;
   }

   public boolean hasCustomName() {
      return this.hasCustomName;
   }

   public void setCustomName(String s) {
      this.hasCustomName = true;
      this.inventoryTitle = s;
   }

   public ITextComponent getDisplayName() {
      return (ITextComponent)(this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName(), new Object[0]));
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public void markDirty() {
      if (this.changeListeners != null) {
         for(int i = 0; i < this.changeListeners.size(); ++i) {
            ((IInventoryChangedListener)this.changeListeners.get(i)).onInventoryChanged(this);
         }
      }

   }

   public boolean isUsableByPlayer(EntityPlayer entityhuman) {
      return true;
   }

   public void openInventory(EntityPlayer entityhuman) {
   }

   public void closeInventory(EntityPlayer entityhuman) {
   }

   public boolean isItemValidForSlot(int i, ItemStack itemstack) {
      return true;
   }

   public int getField(int i) {
      return 0;
   }

   public void setField(int i, int j) {
   }

   public int getFieldCount() {
      return 0;
   }

   public void clear() {
      for(int i = 0; i < this.inventoryContents.length; ++i) {
         this.inventoryContents[i] = null;
      }

   }
}
