package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryView;

public class ContainerHopper extends Container {
   private final IInventory hopperInventory;
   private CraftInventoryView bukkitEntity = null;
   private InventoryPlayer player;

   public CraftInventoryView getBukkitView() {
      if (this.bukkitEntity != null) {
         return this.bukkitEntity;
      } else {
         CraftInventory inventory = new CraftInventory(this.hopperInventory);
         this.bukkitEntity = new CraftInventoryView(this.player.player.getBukkitEntity(), inventory, this);
         return this.bukkitEntity;
      }
   }

   public ContainerHopper(InventoryPlayer playerinventory, IInventory iinventory, EntityPlayer entityhuman) {
      this.hopperInventory = iinventory;
      this.player = playerinventory;
      iinventory.openInventory(entityhuman);

      for(int i = 0; i < iinventory.getSizeInventory(); ++i) {
         this.addSlotToContainer(new Slot(iinventory, i, 44 + i * 18, 20));
      }

      for(int var6 = 0; var6 < 3; ++var6) {
         for(int j = 0; j < 9; ++j) {
            this.addSlotToContainer(new Slot(playerinventory, j + var6 * 9 + 9, 8 + j * 18, var6 * 18 + 51));
         }
      }

      for(int var7 = 0; var7 < 9; ++var7) {
         this.addSlotToContainer(new Slot(playerinventory, var7, 8 + var7 * 18, 109));
      }

   }

   public boolean canInteractWith(EntityPlayer entityhuman) {
      return !this.checkReachable ? true : this.hopperInventory.isUsableByPlayer(entityhuman);
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer entityhuman, int i) {
      ItemStack itemstack = null;
      Slot slot = (Slot)this.inventorySlots.get(i);
      if (slot != null && slot.getHasStack()) {
         ItemStack itemstack1 = slot.getStack();
         itemstack = itemstack1.copy();
         if (i < this.hopperInventory.getSizeInventory()) {
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

   public void onContainerClosed(EntityPlayer entityhuman) {
      super.onContainerClosed(entityhuman);
      this.hopperInventory.closeInventory(entityhuman);
   }
}
