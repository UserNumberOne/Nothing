package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.stats.AchievementList;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerBrewingStand extends Container {
   private final IInventory tileBrewingStand;
   private final Slot theSlot;
   private int prevBrewTime;
   private int prevFuel;

   public ContainerBrewingStand(InventoryPlayer var1, IInventory var2) {
      this.tileBrewingStand = tileBrewingStandIn;
      this.addSlotToContainer(new ContainerBrewingStand.Potion(playerInventory.player, tileBrewingStandIn, 0, 56, 51));
      this.addSlotToContainer(new ContainerBrewingStand.Potion(playerInventory.player, tileBrewingStandIn, 1, 79, 58));
      this.addSlotToContainer(new ContainerBrewingStand.Potion(playerInventory.player, tileBrewingStandIn, 2, 102, 51));
      this.theSlot = this.addSlotToContainer(new ContainerBrewingStand.Ingredient(tileBrewingStandIn, 3, 79, 17));
      this.addSlotToContainer(new ContainerBrewingStand.Fuel(tileBrewingStandIn, 4, 17, 17));

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 9; ++j) {
            this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for(int k = 0; k < 9; ++k) {
         this.addSlotToContainer(new Slot(playerInventory, k, 8 + k * 18, 142));
      }

   }

   public void addListener(IContainerListener var1) {
      super.addListener(listener);
      listener.sendAllWindowProperties(this, this.tileBrewingStand);
   }

   public void detectAndSendChanges() {
      super.detectAndSendChanges();

      for(int i = 0; i < this.listeners.size(); ++i) {
         IContainerListener icontainerlistener = (IContainerListener)this.listeners.get(i);
         if (this.prevBrewTime != this.tileBrewingStand.getField(0)) {
            icontainerlistener.sendProgressBarUpdate(this, 0, this.tileBrewingStand.getField(0));
         }

         if (this.prevFuel != this.tileBrewingStand.getField(1)) {
            icontainerlistener.sendProgressBarUpdate(this, 1, this.tileBrewingStand.getField(1));
         }
      }

      this.prevBrewTime = this.tileBrewingStand.getField(0);
      this.prevFuel = this.tileBrewingStand.getField(1);
   }

   @SideOnly(Side.CLIENT)
   public void updateProgressBar(int var1, int var2) {
      this.tileBrewingStand.setField(id, data);
   }

   public boolean canInteractWith(EntityPlayer var1) {
      return this.tileBrewingStand.isUsableByPlayer(playerIn);
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer var1, int var2) {
      ItemStack itemstack = null;
      Slot slot = (Slot)this.inventorySlots.get(index);
      if (slot != null && slot.getHasStack()) {
         ItemStack itemstack1 = slot.getStack();
         itemstack = itemstack1.copy();
         if ((index < 0 || index > 2) && index != 3 && index != 4) {
            if (!this.theSlot.getHasStack() && this.theSlot.isItemValid(itemstack1)) {
               if (!this.mergeItemStack(itemstack1, 3, 4, false)) {
                  return null;
               }
            } else if (ContainerBrewingStand.Potion.canHoldPotion(itemstack)) {
               if (!this.mergeItemStack(itemstack1, 0, 3, false)) {
                  return null;
               }
            } else if (ContainerBrewingStand.Fuel.isValidBrewingFuel(itemstack)) {
               if (!this.mergeItemStack(itemstack1, 4, 5, false)) {
                  return null;
               }
            } else if (index >= 5 && index < 32) {
               if (!this.mergeItemStack(itemstack1, 32, 41, false)) {
                  return null;
               }
            } else if (index >= 32 && index < 41) {
               if (!this.mergeItemStack(itemstack1, 5, 32, false)) {
                  return null;
               }
            } else if (!this.mergeItemStack(itemstack1, 5, 41, false)) {
               return null;
            }
         } else {
            if (!this.mergeItemStack(itemstack1, 5, 41, true)) {
               return null;
            }

            slot.onSlotChange(itemstack1, itemstack);
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

   static class Fuel extends Slot {
      public Fuel(IInventory var1, int var2, int var3, int var4) {
         super(iInventoryIn, index, xPosition, yPosition);
      }

      public boolean isItemValid(@Nullable ItemStack var1) {
         return isValidBrewingFuel(stack);
      }

      public static boolean isValidBrewingFuel(@Nullable ItemStack var0) {
         return itemStackIn != null && itemStackIn.getItem() == Items.BLAZE_POWDER;
      }

      public int getSlotStackLimit() {
         return 64;
      }
   }

   static class Ingredient extends Slot {
      public Ingredient(IInventory var1, int var2, int var3, int var4) {
         super(iInventoryIn, index, xPosition, yPosition);
      }

      public boolean isItemValid(@Nullable ItemStack var1) {
         return stack != null && BrewingRecipeRegistry.isValidIngredient(stack);
      }

      public int getSlotStackLimit() {
         return 64;
      }
   }

   static class Potion extends Slot {
      private final EntityPlayer player;

      public Potion(EntityPlayer var1, IInventory var2, int var3, int var4, int var5) {
         super(inventoryIn, index, xPosition, yPosition);
         this.player = playerIn;
      }

      public boolean isItemValid(@Nullable ItemStack var1) {
         return canHoldPotion(stack);
      }

      public int getSlotStackLimit() {
         return 1;
      }

      public void onPickupFromSlot(EntityPlayer var1, ItemStack var2) {
         if (PotionUtils.getPotionFromItem(stack) != PotionTypes.WATER) {
            ForgeEventFactory.onPlayerBrewedPotion(playerIn, stack);
            this.player.addStat(AchievementList.POTION);
         }

         super.onPickupFromSlot(playerIn, stack);
      }

      public static boolean canHoldPotion(@Nullable ItemStack var0) {
         return stack == null ? false : BrewingRecipeRegistry.isValidInput(stack);
      }
   }
}
