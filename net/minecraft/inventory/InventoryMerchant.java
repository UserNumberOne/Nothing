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
      this.player = thePlayerIn;
      this.theMerchant = theMerchantIn;
   }

   public int getSizeInventory() {
      return this.theInventory.length;
   }

   @Nullable
   public ItemStack getStackInSlot(int var1) {
      return this.theInventory[index];
   }

   @Nullable
   public ItemStack decrStackSize(int var1, int var2) {
      if (index == 2 && this.theInventory[index] != null) {
         return ItemStackHelper.getAndSplit(this.theInventory, index, this.theInventory[index].stackSize);
      } else {
         ItemStack itemstack = ItemStackHelper.getAndSplit(this.theInventory, index, count);
         if (itemstack != null && this.inventoryResetNeededOnSlotChange(index)) {
            this.resetRecipeAndSlots();
         }

         return itemstack;
      }
   }

   private boolean inventoryResetNeededOnSlotChange(int var1) {
      return slotIn == 0 || slotIn == 1;
   }

   @Nullable
   public ItemStack removeStackFromSlot(int var1) {
      return ItemStackHelper.getAndRemove(this.theInventory, index);
   }

   public void setInventorySlotContents(int var1, @Nullable ItemStack var2) {
      this.theInventory[index] = stack;
      if (stack != null && stack.stackSize > this.getInventoryStackLimit()) {
         stack.stackSize = this.getInventoryStackLimit();
      }

      if (this.inventoryResetNeededOnSlotChange(index)) {
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
      return this.theMerchant.getCustomer() == player;
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
      ItemStack itemstack = this.theInventory[0];
      ItemStack itemstack1 = this.theInventory[1];
      if (itemstack == null) {
         itemstack = itemstack1;
         itemstack1 = null;
      }

      if (itemstack == null) {
         this.setInventorySlotContents(2, (ItemStack)null);
      } else {
         MerchantRecipeList merchantrecipelist = this.theMerchant.getRecipes(this.player);
         if (merchantrecipelist != null) {
            MerchantRecipe merchantrecipe = merchantrecipelist.canRecipeBeUsed(itemstack, itemstack1, this.currentRecipeIndex);
            if (merchantrecipe != null && !merchantrecipe.isRecipeDisabled()) {
               this.currentRecipe = merchantrecipe;
               this.setInventorySlotContents(2, merchantrecipe.getItemToSell().copy());
            } else if (itemstack1 != null) {
               merchantrecipe = merchantrecipelist.canRecipeBeUsed(itemstack1, itemstack, this.currentRecipeIndex);
               if (merchantrecipe != null && !merchantrecipe.isRecipeDisabled()) {
                  this.currentRecipe = merchantrecipe;
                  this.setInventorySlotContents(2, merchantrecipe.getItemToSell().copy());
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
      this.currentRecipeIndex = currentRecipeIndexIn;
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
      for(int i = 0; i < this.theInventory.length; ++i) {
         this.theInventory[i] = null;
      }

   }
}
