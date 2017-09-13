package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityBeacon;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryBeacon;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryView;

public class ContainerBeacon extends Container {
   private final IInventory tileBeacon;
   private final ContainerBeacon.BeaconSlot beaconSlot;
   private CraftInventoryView bukkitEntity = null;
   private InventoryPlayer player;

   public ContainerBeacon(IInventory iinventory, IInventory iinventory1) {
      this.player = (InventoryPlayer)iinventory;
      this.tileBeacon = iinventory1;
      this.beaconSlot = new ContainerBeacon.BeaconSlot(iinventory1, 0, 136, 110);
      this.addSlotToContainer(this.beaconSlot);

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 9; ++j) {
            this.addSlotToContainer(new Slot(iinventory, j + i * 9 + 9, 36 + j * 18, 137 + i * 18));
         }
      }

      for(int var5 = 0; var5 < 9; ++var5) {
         this.addSlotToContainer(new Slot(iinventory, var5, 36 + var5 * 18, 195));
      }

   }

   public void addListener(IContainerListener icrafting) {
      super.addListener(icrafting);
      icrafting.sendAllWindowProperties(this, this.tileBeacon);
   }

   public IInventory getTileEntity() {
      return this.tileBeacon;
   }

   public void onContainerClosed(EntityPlayer entityhuman) {
      super.onContainerClosed(entityhuman);
      if (entityhuman != null && !entityhuman.world.isRemote) {
         ItemStack itemstack = this.beaconSlot.decrStackSize(this.beaconSlot.getSlotStackLimit());
         if (itemstack != null) {
            entityhuman.dropItem(itemstack, false);
         }
      }

   }

   public boolean canInteractWith(EntityPlayer entityhuman) {
      return !this.checkReachable ? true : this.tileBeacon.isUsableByPlayer(entityhuman);
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer entityhuman, int i) {
      ItemStack itemstack = null;
      Slot slot = (Slot)this.inventorySlots.get(i);
      if (slot != null && slot.getHasStack()) {
         ItemStack itemstack1 = slot.getStack();
         itemstack = itemstack1.copy();
         if (i == 0) {
            if (!this.mergeItemStack(itemstack1, 1, 37, true)) {
               return null;
            }

            slot.onSlotChange(itemstack1, itemstack);
         } else if (!this.beaconSlot.getHasStack() && this.beaconSlot.isItemValid(itemstack1) && itemstack1.stackSize == 1) {
            if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
               return null;
            }
         } else if (i >= 1 && i < 28) {
            if (!this.mergeItemStack(itemstack1, 28, 37, false)) {
               return null;
            }
         } else if (i >= 28 && i < 37) {
            if (!this.mergeItemStack(itemstack1, 1, 28, false)) {
               return null;
            }
         } else if (!this.mergeItemStack(itemstack1, 1, 37, false)) {
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
         CraftInventory inventory = new CraftInventoryBeacon((TileEntityBeacon)this.tileBeacon);
         this.bukkitEntity = new CraftInventoryView(this.player.player.getBukkitEntity(), inventory, this);
         return this.bukkitEntity;
      }
   }

   class BeaconSlot extends Slot {
      public BeaconSlot(IInventory iinventory, int i, int j, int k) {
         super(iinventory, i, j, k);
      }

      public boolean isItemValid(@Nullable ItemStack itemstack) {
         return itemstack == null ? false : itemstack.getItem() == Items.EMERALD || itemstack.getItem() == Items.DIAMOND || itemstack.getItem() == Items.GOLD_INGOT || itemstack.getItem() == Items.IRON_INGOT;
      }

      public int getSlotStackLimit() {
         return 1;
      }
   }
}
