package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

public class InventoryMerchant implements IInventory {
   private final IMerchant theMerchant;
   private final ItemStack[] theInventory = new ItemStack[3];
   private final EntityPlayer player;
   private MerchantRecipe currentRecipe;
   private int currentRecipeIndex;

   public InventoryMerchant(EntityPlayer var1, IMerchant var2) {
      this.player = var1;
      this.theMerchant = var2;
   }

   public int getSizeInventory() {
      return this.theInventory.length;
   }

   @Nullable
   public ItemStack getStackInSlot(int var1) {
      return this.theInventory[var1];
   }

   @Nullable
   public ItemStack decrStackSize(int var1, int var2) {
      if (var1 == 2 && this.theInventory[var1] != null) {
         return ItemStackHelper.getAndSplit(this.theInventory, var1, this.theInventory[var1].stackSize);
      } else {
         ItemStack var3 = ItemStackHelper.getAndSplit(this.theInventory, var1, var2);
         if (var3 != null && this.inventoryResetNeededOnSlotChange(var1)) {
            this.resetRecipeAndSlots();
         }

         return var3;
      }
   }

   private boolean inventoryResetNeededOnSlotChange(int var1) {
      return var1 == 0 || var1 == 1;
   }

   @Nullable
   public ItemStack removeStackFromSlot(int var1) {
      return ItemStackHelper.getAndRemove(this.theInventory, var1);
   }

   public void setInventorySlotContents(int var1, @Nullable ItemStack var2) {
      this.theInventory[var1] = var2;
      if (var2 != null && var2.stackSize > this.getInventoryStackLimit()) {
         var2.stackSize = this.getInventoryStackLimit();
      }

      if (this.inventoryResetNeededOnSlotChange(var1)) {
         this.resetRecipeAndSlots();
      }

   }

   public String getName() {
      return "mob.villager";
   }

   public boolean hasCustomName() {
      return false;
   }

   public ITextComponent getDisplayName() {
      return (ITextComponent)(this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName(), new Object[0]));
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public boolean isUsableByPlayer(EntityPlayer var1) {
      return this.theMerchant.getCustomer() == var1;
   }

   public void openInventory(EntityPlayer var1) {
   }

   public void closeInventory(EntityPlayer var1) {
   }

   public boolean isItemValidForSlot(int var1, ItemStack var2) {
      return true;
   }

   public void markDirty() {
      this.resetRecipeAndSlots();
   }

   public void resetRecipeAndSlots() {
      this.currentRecipe = null;
      ItemStack var1 = this.theInventory[0];
      ItemStack var2 = this.theInventory[1];
      if (var1 == null) {
         var1 = var2;
         var2 = null;
      }

      if (var1 == null) {
         this.setInventorySlotContents(2, (ItemStack)null);
      } else {
         MerchantRecipeList var3 = this.theMerchant.getRecipes(this.player);
         if (var3 != null) {
            MerchantRecipe var4 = var3.canRecipeBeUsed(var1, var2, this.currentRecipeIndex);
            if (var4 != null && !var4.isRecipeDisabled()) {
               this.currentRecipe = var4;
               this.setInventorySlotContents(2, var4.getItemToSell().copy());
            } else if (var2 != null) {
               var4 = var3.canRecipeBeUsed(var2, var1, this.currentRecipeIndex);
               if (var4 != null && !var4.isRecipeDisabled()) {
                  this.currentRecipe = var4;
                  this.setInventorySlotContents(2, var4.getItemToSell().copy());
               } else {
                  this.setInventorySlotContents(2, (ItemStack)null);
               }
            } else {
               this.setInventorySlotContents(2, (ItemStack)null);
            }
         }
      }

      this.theMerchant.verifySellingItem(this.getStackInSlot(2));
   }

   public MerchantRecipe getCurrentRecipe() {
      return this.currentRecipe;
   }

   public void setCurrentRecipeIndex(int var1) {
      this.currentRecipeIndex = var1;
      this.resetRecipeAndSlots();
   }

   public int getField(int var1) {
      return 0;
   }

   public void setField(int var1, int var2) {
   }

   public int getFieldCount() {
      return 0;
   }

   public void clear() {
      for(int var1 = 0; var1 < this.theInventory.length; ++var1) {
         this.theInventory[var1] = null;
      }

   }
}
