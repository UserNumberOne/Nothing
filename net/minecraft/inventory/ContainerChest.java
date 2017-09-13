package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryDoubleChest;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryPlayer;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryView;

public class ContainerChest extends Container {
   private final IInventory lowerChestInventory;
   private final int numRows;
   private CraftInventoryView bukkitEntity = null;
   private InventoryPlayer player;

   public CraftInventoryView getBukkitView() {
      if (this.bukkitEntity != null) {
         return this.bukkitEntity;
      } else {
         CraftInventory inventory;
         if (this.lowerChestInventory instanceof InventoryPlayer) {
            inventory = new CraftInventoryPlayer((InventoryPlayer)this.lowerChestInventory);
         } else if (this.lowerChestInventory instanceof InventoryLargeChest) {
            inventory = new CraftInventoryDoubleChest((InventoryLargeChest)this.lowerChestInventory);
         } else {
            inventory = new CraftInventory(this.lowerChestInventory);
         }

         this.bukkitEntity = new CraftInventoryView(this.player.player.getBukkitEntity(), inventory, this);
         return this.bukkitEntity;
      }
   }

   public ContainerChest(IInventory iinventory, IInventory iinventory1, EntityPlayer entityhuman) {
      this.lowerChestInventory = iinventory1;
      this.numRows = iinventory1.getSizeInventory() / 9;
      iinventory1.openInventory(entityhuman);
      int i = (this.numRows - 4) * 18;
      this.player = (InventoryPlayer)iinventory;

      for(int j = 0; j < this.numRows; ++j) {
         for(int k = 0; k < 9; ++k) {
            this.addSlotToContainer(new Slot(iinventory1, k + j * 9, 8 + k * 18, 18 + j * 18));
         }
      }

      for(int var7 = 0; var7 < 3; ++var7) {
         for(int k = 0; k < 9; ++k) {
            this.addSlotToContainer(new Slot(iinventory, k + var7 * 9 + 9, 8 + k * 18, 103 + var7 * 18 + i));
         }
      }

      for(int var8 = 0; var8 < 9; ++var8) {
         this.addSlotToContainer(new Slot(iinventory, var8, 8 + var8 * 18, 161 + i));
      }

   }

   public boolean canInteractWith(EntityPlayer entityhuman) {
      return !this.checkReachable ? true : this.lowerChestInventory.isUsableByPlayer(entityhuman);
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer entityhuman, int i) {
      ItemStack itemstack = null;
      Slot slot = (Slot)this.inventorySlots.get(i);
      if (slot != null && slot.getHasStack()) {
         ItemStack itemstack1 = slot.getStack();
         itemstack = itemstack1.copy();
         if (i < this.numRows * 9) {
            if (!this.mergeItemStack(itemstack1, this.numRows * 9, this.inventorySlots.size(), true)) {
               return null;
            }
         } else if (!this.mergeItemStack(itemstack1, 0, this.numRows * 9, false)) {
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
      this.lowerChestInventory.closeInventory(entityhuman);
   }

   public IInventory getLowerChestInventory() {
      return this.lowerChestInventory;
   }
}
