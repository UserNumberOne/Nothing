package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryFurnace;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryView;

public class ContainerFurnace extends Container {
   private final IInventory tileFurnace;
   private int cookTime;
   private int totalCookTime;
   private int furnaceBurnTime;
   private int currentItemBurnTime;
   private CraftInventoryView bukkitEntity = null;
   private InventoryPlayer player;

   public CraftInventoryView getBukkitView() {
      if (this.bukkitEntity != null) {
         return this.bukkitEntity;
      } else {
         CraftInventoryFurnace inventory = new CraftInventoryFurnace((TileEntityFurnace)this.tileFurnace);
         this.bukkitEntity = new CraftInventoryView(this.player.player.getBukkitEntity(), inventory, this);
         return this.bukkitEntity;
      }
   }

   public ContainerFurnace(InventoryPlayer playerinventory, IInventory iinventory) {
      this.tileFurnace = iinventory;
      this.addSlotToContainer(new Slot(iinventory, 0, 56, 17));
      this.addSlotToContainer(new SlotFurnaceFuel(iinventory, 1, 56, 53));
      this.addSlotToContainer(new SlotFurnaceOutput(playerinventory.player, iinventory, 2, 116, 35));
      this.player = playerinventory;

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 9; ++j) {
            this.addSlotToContainer(new Slot(playerinventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for(int var5 = 0; var5 < 9; ++var5) {
         this.addSlotToContainer(new Slot(playerinventory, var5, 8 + var5 * 18, 142));
      }

   }

   public void addListener(IContainerListener icrafting) {
      super.addListener(icrafting);
      icrafting.sendAllWindowProperties(this, this.tileFurnace);
   }

   public void detectAndSendChanges() {
      super.detectAndSendChanges();

      for(int i = 0; i < this.listeners.size(); ++i) {
         IContainerListener icrafting = (IContainerListener)this.listeners.get(i);
         if (this.cookTime != this.tileFurnace.getField(2)) {
            icrafting.sendProgressBarUpdate(this, 2, this.tileFurnace.getField(2));
         }

         if (this.furnaceBurnTime != this.tileFurnace.getField(0)) {
            icrafting.sendProgressBarUpdate(this, 0, this.tileFurnace.getField(0));
         }

         if (this.currentItemBurnTime != this.tileFurnace.getField(1)) {
            icrafting.sendProgressBarUpdate(this, 1, this.tileFurnace.getField(1));
         }

         if (this.totalCookTime != this.tileFurnace.getField(3)) {
            icrafting.sendProgressBarUpdate(this, 3, this.tileFurnace.getField(3));
         }
      }

      this.cookTime = this.tileFurnace.getField(2);
      this.furnaceBurnTime = this.tileFurnace.getField(0);
      this.currentItemBurnTime = this.tileFurnace.getField(1);
      this.totalCookTime = this.tileFurnace.getField(3);
   }

   public boolean canInteractWith(EntityPlayer entityhuman) {
      return !this.checkReachable ? true : this.tileFurnace.isUsableByPlayer(entityhuman);
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer entityhuman, int i) {
      ItemStack itemstack = null;
      Slot slot = (Slot)this.inventorySlots.get(i);
      if (slot != null && slot.getHasStack()) {
         ItemStack itemstack1 = slot.getStack();
         itemstack = itemstack1.copy();
         if (i == 2) {
            if (!this.mergeItemStack(itemstack1, 3, 39, true)) {
               return null;
            }

            slot.onSlotChange(itemstack1, itemstack);
         } else if (i != 1 && i != 0) {
            if (FurnaceRecipes.instance().getSmeltingResult(itemstack1) != null) {
               if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
                  return null;
               }
            } else if (TileEntityFurnace.isItemFuel(itemstack1)) {
               if (!this.mergeItemStack(itemstack1, 1, 2, false)) {
                  return null;
               }
            } else if (i >= 3 && i < 30) {
               if (!this.mergeItemStack(itemstack1, 30, 39, false)) {
                  return null;
               }
            } else if (i >= 30 && i < 39 && !this.mergeItemStack(itemstack1, 3, 30, false)) {
               return null;
            }
         } else if (!this.mergeItemStack(itemstack1, 3, 39, false)) {
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
}
