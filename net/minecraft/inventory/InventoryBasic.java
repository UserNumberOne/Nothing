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

   public void onOpen(CraftHumanEntity var1) {
      this.transaction.add(var1);
   }

   public void onClose(CraftHumanEntity var1) {
      this.transaction.remove(var1);
   }

   public List getViewers() {
      return this.transaction;
   }

   public void setMaxStackSize(int var1) {
      this.maxStack = var1;
   }

   public InventoryHolder getOwner() {
      return this.bukkitOwner;
   }

   public Location getLocation() {
      return null;
   }

   public InventoryBasic(String var1, boolean var2, int var3) {
      this(var1, var2, var3, (InventoryHolder)null);
   }

   public InventoryBasic(String var1, boolean var2, int var3, InventoryHolder var4) {
      this.transaction = new ArrayList();
      this.maxStack = 64;
      this.bukkitOwner = var4;
      this.inventoryTitle = var1;
      this.hasCustomName = var2;
      this.slotsCount = var3;
      this.inventoryContents = new ItemStack[var3];
   }

   public void addInventoryChangeListener(IInventoryChangedListener var1) {
      if (this.changeListeners == null) {
         this.changeListeners = Lists.newArrayList();
      }

      this.changeListeners.add(var1);
   }

   public void removeInventoryChangeListener(IInventoryChangedListener var1) {
      this.changeListeners.remove(var1);
   }

   @Nullable
   public ItemStack getStackInSlot(int var1) {
      return var1 >= 0 && var1 < this.inventoryContents.length ? this.inventoryContents[var1] : null;
   }

   @Nullable
   public ItemStack decrStackSize(int var1, int var2) {
      ItemStack var3 = ItemStackHelper.getAndSplit(this.inventoryContents, var1, var2);
      if (var3 != null) {
         this.markDirty();
      }

      return var3;
   }

   @Nullable
   public ItemStack addItem(ItemStack var1) {
      ItemStack var2 = var1.copy();

      for(int var3 = 0; var3 < this.slotsCount; ++var3) {
         ItemStack var4 = this.getStackInSlot(var3);
         if (var4 == null) {
            this.setInventorySlotContents(var3, var2);
            this.markDirty();
            return null;
         }

         if (ItemStack.areItemsEqual(var4, var2)) {
            int var5 = Math.min(this.getInventoryStackLimit(), var4.getMaxStackSize());
            int var6 = Math.min(var2.stackSize, var5 - var4.stackSize);
            if (var6 > 0) {
               var4.stackSize += var6;
               var2.stackSize -= var6;
               if (var2.stackSize <= 0) {
                  this.markDirty();
                  return null;
               }
            }
         }
      }

      if (var2.stackSize != var1.stackSize) {
         this.markDirty();
      }

      return var2;
   }

   @Nullable
   public ItemStack removeStackFromSlot(int var1) {
      if (this.inventoryContents[var1] != null) {
         ItemStack var2 = this.inventoryContents[var1];
         this.inventoryContents[var1] = null;
         return var2;
      } else {
         return null;
      }
   }

   public void setInventorySlotContents(int var1, @Nullable ItemStack var2) {
      this.inventoryContents[var1] = var2;
      if (var2 != null && var2.stackSize > this.getInventoryStackLimit()) {
         var2.stackSize = this.getInventoryStackLimit();
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

   public void setCustomName(String var1) {
      this.hasCustomName = true;
      this.inventoryTitle = var1;
   }

   public ITextComponent getDisplayName() {
      return (ITextComponent)(this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName(), new Object[0]));
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public void markDirty() {
      if (this.changeListeners != null) {
         for(int var1 = 0; var1 < this.changeListeners.size(); ++var1) {
            ((IInventoryChangedListener)this.changeListeners.get(var1)).onInventoryChanged(this);
         }
      }

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
      for(int var1 = 0; var1 < this.inventoryContents.length; ++var1) {
         this.inventoryContents[var1] = null;
      }

   }
}
