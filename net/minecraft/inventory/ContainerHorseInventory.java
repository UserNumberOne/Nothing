package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.HorseArmorType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryHorse;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryView;
import org.bukkit.inventory.InventoryView;

public class ContainerHorseInventory extends Container {
   private final IInventory horseInventory;
   private final EntityHorse theHorse;
   CraftInventoryView bukkitEntity;
   InventoryPlayer player;

   public InventoryView getBukkitView() {
      if (this.bukkitEntity != null) {
         return this.bukkitEntity;
      } else {
         CraftInventory inventory = new CraftInventoryHorse(this.horseInventory);
         return this.bukkitEntity = new CraftInventoryView(this.player.player.getBukkitEntity(), inventory, this);
      }
   }

   public ContainerHorseInventory(IInventory iinventory, IInventory iinventory1, final EntityHorse entityhorse, EntityPlayer entityhuman) {
      this.player = (InventoryPlayer)iinventory;
      this.horseInventory = iinventory1;
      this.theHorse = entityhorse;
      iinventory1.openInventory(entityhuman);
      this.addSlotToContainer(new Slot(iinventory1, 0, 8, 18) {
         public boolean isItemValid(@Nullable ItemStack itemstack) {
            return super.isItemValid(itemstack) && itemstack.getItem() == Items.SADDLE && !this.getHasStack();
         }
      });
      this.addSlotToContainer(new Slot(iinventory1, 1, 8, 36) {
         public boolean isItemValid(@Nullable ItemStack itemstack) {
            return super.isItemValid(itemstack) && entityhorse.getType().isHorse() && HorseArmorType.isHorseArmor(itemstack.getItem());
         }
      });
      if (entityhorse.isChested()) {
         for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 5; ++j) {
               this.addSlotToContainer(new Slot(iinventory1, 2 + j + i * 5, 80 + j * 18, 18 + i * 18));
            }
         }
      }

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 9; ++j) {
            this.addSlotToContainer(new Slot(iinventory, j + i * 9 + 9, 8 + j * 18, 102 + i * 18 + -18));
         }
      }

      for(int var8 = 0; var8 < 9; ++var8) {
         this.addSlotToContainer(new Slot(iinventory, var8, 8 + var8 * 18, 142));
      }

   }

   public boolean canInteractWith(EntityPlayer entityhuman) {
      return this.horseInventory.isUsableByPlayer(entityhuman) && this.theHorse.isEntityAlive() && this.theHorse.getDistanceToEntity(entityhuman) < 8.0F;
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer entityhuman, int i) {
      ItemStack itemstack = null;
      Slot slot = (Slot)this.inventorySlots.get(i);
      if (slot != null && slot.getHasStack()) {
         ItemStack itemstack1 = slot.getStack();
         itemstack = itemstack1.copy();
         if (i < this.horseInventory.getSizeInventory()) {
            if (!this.mergeItemStack(itemstack1, this.horseInventory.getSizeInventory(), this.inventorySlots.size(), true)) {
               return null;
            }
         } else if (this.getSlot(1).isItemValid(itemstack1) && !this.getSlot(1).getHasStack()) {
            if (!this.mergeItemStack(itemstack1, 1, 2, false)) {
               return null;
            }
         } else if (this.getSlot(0).isItemValid(itemstack1)) {
            if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
               return null;
            }
         } else if (this.horseInventory.getSizeInventory() <= 2 || !this.mergeItemStack(itemstack1, 2, this.horseInventory.getSizeInventory(), false)) {
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
      this.horseInventory.closeInventory(entityhuman);
   }
}
