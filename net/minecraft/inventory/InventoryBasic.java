package net.minecraft.inventory;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class InventoryBasic implements IInventory {
   private String inventoryTitle;
   private final int slotsCount;
   private final ItemStack[] inventoryContents;
   private List changeListeners;
   private boolean hasCustomName;

   public InventoryBasic(String var1, boolean var2, int var3) {
      this.inventoryTitle = title;
      this.hasCustomName = customName;
      this.slotsCount = slotCount;
      this.inventoryContents = new ItemStack[slotCount];
   }

   @SideOnly(Side.CLIENT)
   public InventoryBasic(ITextComponent var1, int var2) {
      this(title.getUnformattedText(), true, slotCount);
   }

   public void addInventoryChangeListener(IInventoryChangedListener var1) {
      if (this.changeListeners == null) {
         this.changeListeners = Lists.newArrayList();
      }

      this.changeListeners.add(listener);
   }

   public void removeInventoryChangeListener(IInventoryChangedListener var1) {
      this.changeListeners.remove(listener);
   }

   @Nullable
   public ItemStack getStackInSlot(int var1) {
      return index >= 0 && index < this.inventoryContents.length ? this.inventoryContents[index] : null;
   }

   @Nullable
   public ItemStack decrStackSize(int var1, int var2) {
      ItemStack itemstack = ItemStackHelper.getAndSplit(this.inventoryContents, index, count);
      if (itemstack != null) {
         this.markDirty();
      }

      return itemstack;
   }

   @Nullable
   public ItemStack addItem(ItemStack var1) {
      ItemStack itemstack = stack.copy();

      for(int i = 0; i < this.slotsCount; ++i) {
         ItemStack itemstack1 = this.getStackInSlot(i);
         if (itemstack1 == null) {
            this.setInventorySlotContents(i, itemstack);
            this.markDirty();
            return null;
         }

         if (ItemStack.areItemsEqual(itemstack1, itemstack)) {
            int j = Math.min(this.getInventoryStackLimit(), itemstack1.getMaxStackSize());
            int k = Math.min(itemstack.stackSize, j - itemstack1.stackSize);
            if (k > 0) {
               itemstack1.stackSize += k;
               itemstack.stackSize -= k;
               if (itemstack.stackSize <= 0) {
                  this.markDirty();
                  return null;
               }
            }
         }
      }

      if (itemstack.stackSize != stack.stackSize) {
         this.markDirty();
      }

      return itemstack;
   }

   @Nullable
   public ItemStack removeStackFromSlot(int var1) {
      if (this.inventoryContents[index] != null) {
         ItemStack itemstack = this.inventoryContents[index];
         this.inventoryContents[index] = null;
         return itemstack;
      } else {
         return null;
      }
   }

   public void setInventorySlotContents(int var1, @Nullable ItemStack var2) {
      this.inventoryContents[index] = stack;
      if (stack != null && stack.stackSize > this.getInventoryStackLimit()) {
         stack.stackSize = this.getInventoryStackLimit();
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
      this.inventoryTitle = inventoryTitleIn;
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
      for(int i = 0; i < this.inventoryContents.length; ++i) {
         this.inventoryContents[i] = null;
      }

   }
}
