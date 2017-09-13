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
      this.tileBrewingStand = var2;
      this.addSlotToContainer(new ContainerBrewingStand.Potion(var1.player, var2, 0, 56, 51));
      this.addSlotToContainer(new ContainerBrewingStand.Potion(var1.player, var2, 1, 79, 58));
      this.addSlotToContainer(new ContainerBrewingStand.Potion(var1.player, var2, 2, 102, 51));
      this.theSlot = this.addSlotToContainer(new ContainerBrewingStand.Ingredient(var2, 3, 79, 17));
      this.addSlotToContainer(new ContainerBrewingStand.Fuel(var2, 4, 17, 17));

      for(int var3 = 0; var3 < 3; ++var3) {
         for(int var4 = 0; var4 < 9; ++var4) {
            this.addSlotToContainer(new Slot(var1, var4 + var3 * 9 + 9, 8 + var4 * 18, 84 + var3 * 18));
         }
      }

      for(int var5 = 0; var5 < 9; ++var5) {
         this.addSlotToContainer(new Slot(var1, var5, 8 + var5 * 18, 142));
      }

   }

   public void addListener(IContainerListener var1) {
      super.addListener(var1);
      var1.sendAllWindowProperties(this, this.tileBrewingStand);
   }

   public void detectAndSendChanges() {
      super.detectAndSendChanges();

      for(int var1 = 0; var1 < this.listeners.size(); ++var1) {
         IContainerListener var2 = (IContainerListener)this.listeners.get(var1);
         if (this.prevBrewTime != this.tileBrewingStand.getField(0)) {
            var2.sendProgressBarUpdate(this, 0, this.tileBrewingStand.getField(0));
         }

         if (this.prevFuel != this.tileBrewingStand.getField(1)) {
            var2.sendProgressBarUpdate(this, 1, this.tileBrewingStand.getField(1));
         }
      }

      this.prevBrewTime = this.tileBrewingStand.getField(0);
      this.prevFuel = this.tileBrewingStand.getField(1);
   }

   @SideOnly(Side.CLIENT)
   public void updateProgressBar(int var1, int var2) {
      this.tileBrewingStand.setField(var1, var2);
   }

   public boolean canInteractWith(EntityPlayer var1) {
      return this.tileBrewingStand.isUsableByPlayer(var1);
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer var1, int var2) {
      ItemStack var3 = null;
      Slot var4 = (Slot)this.inventorySlots.get(var2);
      if (var4 != null && var4.getHasStack()) {
         ItemStack var5 = var4.getStack();
         var3 = var5.copy();
         if ((var2 < 0 || var2 > 2) && var2 != 3 && var2 != 4) {
            if (!this.theSlot.getHasStack() && this.theSlot.isItemValid(var5)) {
               if (!this.mergeItemStack(var5, 3, 4, false)) {
                  return null;
               }
            } else if (ContainerBrewingStand.Potion.canHoldPotion(var3)) {
               if (!this.mergeItemStack(var5, 0, 3, false)) {
                  return null;
               }
            } else if (ContainerBrewingStand.Fuel.isValidBrewingFuel(var3)) {
               if (!this.mergeItemStack(var5, 4, 5, false)) {
                  return null;
               }
            } else if (var2 >= 5 && var2 < 32) {
               if (!this.mergeItemStack(var5, 32, 41, false)) {
                  return null;
               }
            } else if (var2 >= 32 && var2 < 41) {
               if (!this.mergeItemStack(var5, 5, 32, false)) {
                  return null;
               }
            } else if (!this.mergeItemStack(var5, 5, 41, false)) {
               return null;
            }
         } else {
            if (!this.mergeItemStack(var5, 5, 41, true)) {
               return null;
            }

            var4.onSlotChange(var5, var3);
         }

         if (var5.stackSize == 0) {
            var4.putStack((ItemStack)null);
         } else {
            var4.onSlotChanged();
         }

         if (var5.stackSize == var3.stackSize) {
            return null;
         }

         var4.onPickupFromSlot(var1, var5);
      }

      return var3;
   }

   static class Fuel extends Slot {
      public Fuel(IInventory var1, int var2, int var3, int var4) {
         super(var1, var2, var3, var4);
      }

      public boolean isItemValid(@Nullable ItemStack var1) {
         return isValidBrewingFuel(var1);
      }

      public static boolean isValidBrewingFuel(@Nullable ItemStack var0) {
         return var0 != null && var0.getItem() == Items.BLAZE_POWDER;
      }

      public int getSlotStackLimit() {
         return 64;
      }
   }

   static class Ingredient extends Slot {
      public Ingredient(IInventory var1, int var2, int var3, int var4) {
         super(var1, var2, var3, var4);
      }

      public boolean isItemValid(@Nullable ItemStack var1) {
         return var1 != null && BrewingRecipeRegistry.isValidIngredient(var1);
      }

      public int getSlotStackLimit() {
         return 64;
      }
   }

   static class Potion extends Slot {
      private final EntityPlayer player;

      public Potion(EntityPlayer var1, IInventory var2, int var3, int var4, int var5) {
         super(var2, var3, var4, var5);
         this.player = var1;
      }

      public boolean isItemValid(@Nullable ItemStack var1) {
         return canHoldPotion(var1);
      }

      public int getSlotStackLimit() {
         return 1;
      }

      public void onPickupFromSlot(EntityPlayer var1, ItemStack var2) {
         if (PotionUtils.getPotionFromItem(var2) != PotionTypes.WATER) {
            ForgeEventFactory.onPlayerBrewedPotion(var1, var2);
            this.player.addStat(AchievementList.POTION);
         }

         super.onPickupFromSlot(var1, var2);
      }

      public static boolean canHoldPotion(@Nullable ItemStack var0) {
         return var0 == null ? false : BrewingRecipeRegistry.isValidInput(var0);
      }
   }
}
