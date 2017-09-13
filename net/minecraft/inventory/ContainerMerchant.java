package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryMerchant;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryView;

public class ContainerMerchant extends Container {
   private final IMerchant theMerchant;
   private final InventoryMerchant merchantInventory;
   private final World world;
   private CraftInventoryView bukkitEntity = null;
   private InventoryPlayer player;

   public CraftInventoryView getBukkitView() {
      if (this.bukkitEntity == null) {
         this.bukkitEntity = new CraftInventoryView(this.player.player.getBukkitEntity(), new CraftInventoryMerchant(this.merchantInventory), this);
      }

      return this.bukkitEntity;
   }

   public ContainerMerchant(InventoryPlayer playerinventory, IMerchant imerchant, World world) {
      this.theMerchant = imerchant;
      this.world = world;
      this.merchantInventory = new InventoryMerchant(playerinventory.player, imerchant);
      this.addSlotToContainer(new Slot(this.merchantInventory, 0, 36, 53));
      this.addSlotToContainer(new Slot(this.merchantInventory, 1, 62, 53));
      this.addSlotToContainer(new SlotMerchantResult(playerinventory.player, imerchant, this.merchantInventory, 2, 120, 53));
      this.player = playerinventory;

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 9; ++j) {
            this.addSlotToContainer(new Slot(playerinventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for(int var6 = 0; var6 < 9; ++var6) {
         this.addSlotToContainer(new Slot(playerinventory, var6, 8 + var6 * 18, 142));
      }

   }

   public InventoryMerchant getMerchantInventory() {
      return this.merchantInventory;
   }

   public void addListener(IContainerListener icrafting) {
      super.addListener(icrafting);
   }

   public void detectAndSendChanges() {
      super.detectAndSendChanges();
   }

   public void onCraftMatrixChanged(IInventory iinventory) {
      this.merchantInventory.resetRecipeAndSlots();
      super.onCraftMatrixChanged(iinventory);
   }

   public void setCurrentRecipeIndex(int i) {
      this.merchantInventory.setCurrentRecipeIndex(i);
   }

   public boolean canInteractWith(EntityPlayer entityhuman) {
      return this.theMerchant.getCustomer() == entityhuman;
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
         } else if (i != 0 && i != 1) {
            if (i >= 3 && i < 30) {
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

   public void onContainerClosed(EntityPlayer entityhuman) {
      super.onContainerClosed(entityhuman);
      this.theMerchant.setCustomer((EntityPlayer)null);
      super.onContainerClosed(entityhuman);
      if (!this.world.isRemote) {
         ItemStack itemstack = this.merchantInventory.removeStackFromSlot(0);
         if (itemstack != null) {
            entityhuman.dropItem(itemstack, false);
         }

         itemstack = this.merchantInventory.removeStackFromSlot(1);
         if (itemstack != null) {
            entityhuman.dropItem(itemstack, false);
         }
      }

   }
}
