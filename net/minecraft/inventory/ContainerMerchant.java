package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerMerchant extends Container {
   private final IMerchant theMerchant;
   private final InventoryMerchant merchantInventory;
   private final World world;

   public ContainerMerchant(InventoryPlayer var1, IMerchant var2, World var3) {
      this.theMerchant = merchant;
      this.world = worldIn;
      this.merchantInventory = new InventoryMerchant(playerInventory.player, merchant);
      this.addSlotToContainer(new Slot(this.merchantInventory, 0, 36, 53));
      this.addSlotToContainer(new Slot(this.merchantInventory, 1, 62, 53));
      this.addSlotToContainer(new SlotMerchantResult(playerInventory.player, merchant, this.merchantInventory, 2, 120, 53));

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 9; ++j) {
            this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for(int k = 0; k < 9; ++k) {
         this.addSlotToContainer(new Slot(playerInventory, k, 8 + k * 18, 142));
      }

   }

   public InventoryMerchant getMerchantInventory() {
      return this.merchantInventory;
   }

   public void addListener(IContainerListener var1) {
      super.addListener(listener);
   }

   public void detectAndSendChanges() {
      super.detectAndSendChanges();
   }

   public void onCraftMatrixChanged(IInventory var1) {
      this.merchantInventory.resetRecipeAndSlots();
      super.onCraftMatrixChanged(inventoryIn);
   }

   public void setCurrentRecipeIndex(int var1) {
      this.merchantInventory.setCurrentRecipeIndex(currentRecipeIndex);
   }

   @SideOnly(Side.CLIENT)
   public void updateProgressBar(int var1, int var2) {
   }

   public boolean canInteractWith(EntityPlayer var1) {
      return this.theMerchant.getCustomer() == playerIn;
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer var1, int var2) {
      ItemStack itemstack = null;
      Slot slot = (Slot)this.inventorySlots.get(index);
      if (slot != null && slot.getHasStack()) {
         ItemStack itemstack1 = slot.getStack();
         itemstack = itemstack1.copy();
         if (index == 2) {
            if (!this.mergeItemStack(itemstack1, 3, 39, true)) {
               return null;
            }

            slot.onSlotChange(itemstack1, itemstack);
         } else if (index != 0 && index != 1) {
            if (index >= 3 && index < 30) {
               if (!this.mergeItemStack(itemstack1, 30, 39, false)) {
                  return null;
               }
            } else if (index >= 30 && index < 39 && !this.mergeItemStack(itemstack1, 3, 30, false)) {
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

         slot.onPickupFromSlot(playerIn, itemstack1);
      }

      return itemstack;
   }

   public void onContainerClosed(EntityPlayer var1) {
      super.onContainerClosed(playerIn);
      this.theMerchant.setCustomer((EntityPlayer)null);
      super.onContainerClosed(playerIn);
      if (!this.world.isRemote) {
         ItemStack itemstack = this.merchantInventory.removeStackFromSlot(0);
         if (itemstack != null) {
            playerIn.dropItem(itemstack, false);
         }

         itemstack = this.merchantInventory.removeStackFromSlot(1);
         if (itemstack != null) {
            playerIn.dropItem(itemstack, false);
         }
      }

   }
}
