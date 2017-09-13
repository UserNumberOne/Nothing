package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionUtils;
import net.minecraft.stats.AchievementList;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryBrewer;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryView;

public class ContainerBrewingStand extends Container {
   private final IInventory tileBrewingStand;
   private final Slot theSlot;
   private int prevBrewTime;
   private int prevFuel;
   private CraftInventoryView bukkitEntity = null;
   private InventoryPlayer player;

   public ContainerBrewingStand(InventoryPlayer playerinventory, IInventory iinventory) {
      this.player = playerinventory;
      this.tileBrewingStand = iinventory;
      this.addSlotToContainer(new ContainerBrewingStand.Potion(playerinventory.player, iinventory, 0, 56, 51));
      this.addSlotToContainer(new ContainerBrewingStand.Potion(playerinventory.player, iinventory, 1, 79, 58));
      this.addSlotToContainer(new ContainerBrewingStand.Potion(playerinventory.player, iinventory, 2, 102, 51));
      this.theSlot = this.addSlotToContainer(new ContainerBrewingStand.Ingredient(iinventory, 3, 79, 17));
      this.addSlotToContainer(new ContainerBrewingStand.Fuel(iinventory, 4, 17, 17));

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
      icrafting.sendAllWindowProperties(this, this.tileBrewingStand);
   }

   public void detectAndSendChanges() {
      super.detectAndSendChanges();

      for(int i = 0; i < this.listeners.size(); ++i) {
         IContainerListener icrafting = (IContainerListener)this.listeners.get(i);
         if (this.prevBrewTime != this.tileBrewingStand.getField(0)) {
            icrafting.sendProgressBarUpdate(this, 0, this.tileBrewingStand.getField(0));
         }

         if (this.prevFuel != this.tileBrewingStand.getField(1)) {
            icrafting.sendProgressBarUpdate(this, 1, this.tileBrewingStand.getField(1));
         }
      }

      this.prevBrewTime = this.tileBrewingStand.getField(0);
      this.prevFuel = this.tileBrewingStand.getField(1);
   }

   public boolean canInteractWith(EntityPlayer entityhuman) {
      return !this.checkReachable ? true : this.tileBrewingStand.isUsableByPlayer(entityhuman);
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer entityhuman, int i) {
      ItemStack itemstack = null;
      Slot slot = (Slot)this.inventorySlots.get(i);
      if (slot != null && slot.getHasStack()) {
         ItemStack itemstack1 = slot.getStack();
         itemstack = itemstack1.copy();
         if ((i < 0 || i > 2) && i != 3 && i != 4) {
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
            } else if (i >= 5 && i < 32) {
               if (!this.mergeItemStack(itemstack1, 32, 41, false)) {
                  return null;
               }
            } else if (i >= 32 && i < 41) {
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

         slot.onPickupFromSlot(entityhuman, itemstack1);
      }

      return itemstack;
   }

   public CraftInventoryView getBukkitView() {
      if (this.bukkitEntity != null) {
         return this.bukkitEntity;
      } else {
         CraftInventoryBrewer inventory = new CraftInventoryBrewer(this.tileBrewingStand);
         this.bukkitEntity = new CraftInventoryView(this.player.player.getBukkitEntity(), inventory, this);
         return this.bukkitEntity;
      }
   }

   static class Fuel extends Slot {
      public Fuel(IInventory iinventory, int i, int j, int k) {
         super(iinventory, i, j, k);
      }

      public boolean isItemValid(@Nullable ItemStack itemstack) {
         return isValidBrewingFuel(itemstack);
      }

      public static boolean isValidBrewingFuel(@Nullable ItemStack itemstack) {
         return itemstack != null && itemstack.getItem() == Items.BLAZE_POWDER;
      }

      public int getSlotStackLimit() {
         return 64;
      }
   }

   static class Ingredient extends Slot {
      public Ingredient(IInventory iinventory, int i, int j, int k) {
         super(iinventory, i, j, k);
      }

      public boolean isItemValid(@Nullable ItemStack itemstack) {
         return itemstack != null && PotionHelper.isReagent(itemstack);
      }

      public int getSlotStackLimit() {
         return 64;
      }
   }

   static class Potion extends Slot {
      private final EntityPlayer player;

      public Potion(EntityPlayer entityhuman, IInventory iinventory, int i, int j, int k) {
         super(iinventory, i, j, k);
         this.player = entityhuman;
      }

      public boolean isItemValid(@Nullable ItemStack itemstack) {
         return canHoldPotion(itemstack);
      }

      public int getSlotStackLimit() {
         return 1;
      }

      public void onPickupFromSlot(EntityPlayer entityhuman, ItemStack itemstack) {
         if (PotionUtils.getPotionFromItem(itemstack) != PotionTypes.WATER) {
            this.player.addStat(AchievementList.POTION);
         }

         super.onPickupFromSlot(entityhuman, itemstack);
      }

      public static boolean canHoldPotion(@Nullable ItemStack itemstack) {
         if (itemstack == null) {
            return false;
         } else {
            Item item = itemstack.getItem();
            return item == Items.POTIONITEM || item == Items.GLASS_BOTTLE || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION;
         }
      }
   }
}
