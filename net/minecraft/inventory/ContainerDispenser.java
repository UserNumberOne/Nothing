package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryView;

public class ContainerDispenser extends Container {
   public final IInventory dispenserInventory;
   private CraftInventoryView bukkitEntity = null;
   private InventoryPlayer player;

   public ContainerDispenser(IInventory iinventory, IInventory iinventory1) {
      this.dispenserInventory = iinventory1;
      this.player = (InventoryPlayer)iinventory;

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 3; ++j) {
            this.addSlotToContainer(new Slot(iinventory1, j + i * 3, 62 + j * 18, 17 + i * 18));
         }
      }

      for(int var5 = 0; var5 < 3; ++var5) {
         for(int j = 0; j < 9; ++j) {
            this.addSlotToContainer(new Slot(iinventory, j + var5 * 9 + 9, 8 + j * 18, 84 + var5 * 18));
         }
      }

      for(int var6 = 0; var6 < 9; ++var6) {
         this.addSlotToContainer(new Slot(iinventory, var6, 8 + var6 * 18, 142));
      }

   }

   public boolean canInteractWith(EntityPlayer entityhuman) {
      return !this.checkReachable ? true : this.dispenserInventory.isUsableByPlayer(entityhuman);
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer entityhuman, int i) {
      ItemStack itemstack = null;
      Slot slot = (Slot)this.inventorySlots.get(i);
      if (slot != null && slot.getHasStack()) {
         ItemStack itemstack1 = slot.getStack();
         itemstack = itemstack1.copy();
         if (i < 9) {
            if (!this.mergeItemStack(itemstack1, 9, 45, true)) {
               return null;
            }
         } else if (!this.mergeItemStack(itemstack1, 0, 9, false)) {
            return null;
         }

         if (itemstack1.stackSize == 0) {
            slot.putStack((ItemStack)null);
         } else {
            slot.onSlotChanged();
         }

         if (itemstack1.stackSize == itemstack.stackSize) {
            return null;
         }

         slot.onPickupFromSlot(entityhuman, itemstack1);
      }

      return itemstack;
   }

   public CraftInventoryView getBukkitView() {
      if (this.bukkitEntity != null) {
         return this.bukkitEntity;
      } else {
         CraftInventory inventory = new CraftInventory(this.dispenserInventory);
         this.bukkitEntity = new CraftInventoryView(this.player.player.getBukkitEntity(), inventory, this);
         return this.bukkitEntity;
      }
   }
}
