package net.minecraft.inventory;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftVillager;
import org.bukkit.inventory.InventoryHolder;

public class InventoryMerchant implements IInventory {
   private final IMerchant theMerchant;
   private final ItemStack[] theInventory = new ItemStack[3];
   private final EntityPlayer player;
   private MerchantRecipe currentRecipe;
   public int currentRecipeIndex;
   public List transaction = new ArrayList();
   private int maxStack = 64;

   public ItemStack[] getContents() {
      return this.theInventory;
   }

   public void onOpen(CraftHumanEntity who) {
      this.transaction.add(who);
   }

   public void onClose(CraftHumanEntity who) {
      this.transaction.remove(who);
   }

   public List getViewers() {
      return this.transaction;
   }

   public void setMaxStackSize(int i) {
      this.maxStack = i;
   }

   public InventoryHolder getOwner() {
      return (CraftVillager)((EntityVillager)this.theMerchant).getBukkitEntity();
   }

   public Location getLocation() {
      return ((EntityVillager)this.theMerchant).getBukkitEntity().getLocation();
   }

   public InventoryMerchant(EntityPlayer entityhuman, IMerchant imerchant) {
      this.player = entityhuman;
      this.theMerchant = imerchant;
   }

   public int getSizeInventory() {
      return this.theInventory.length;
   }

   @Nullable
   public ItemStack getStackInSlot(int i) {
      return this.theInventory[i];
   }

   @Nullable
   public ItemStack decrStackSize(int i, int j) {
      if (i == 2 && this.theInventory[i] != null) {
         return ItemStackHelper.getAndSplit(this.theInventory, i, this.theInventory[i].stackSize);
      } else {
         ItemStack itemstack = ItemStackHelper.getAndSplit(this.theInventory, i, j);
         if (itemstack != null && this.inventoryResetNeededOnSlotChange(i)) {
            this.resetRecipeAndSlots();
         }

         return itemstack;
      }
   }

   private boolean inventoryResetNeededOnSlotChange(int i) {
      return i == 0 || i == 1;
   }

   @Nullable
   public ItemStack removeStackFromSlot(int i) {
      return ItemStackHelper.getAndRemove(this.theInventory, i);
   }

   public void setInventorySlotContents(int i, @Nullable ItemStack itemstack) {
      this.theInventory[i] = itemstack;
      if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit()) {
         itemstack.stackSize = this.getInventoryStackLimit();
      }

      if (this.inventoryResetNeededOnSlotChange(i)) {
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
      return this.maxStack;
   }

   public boolean isUsableByPlayer(EntityPlayer entityhuman) {
      return this.theMerchant.getCustomer() == entityhuman;
   }

   public void openInventory(EntityPlayer entityhuman) {
   }

   public void closeInventory(EntityPlayer entityhuman) {
   }

   public boolean isItemValidForSlot(int i, ItemStack itemstack) {
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

   public void setCurrentRecipeIndex(int i) {
      this.currentRecipeIndex = i;
      this.resetRecipeAndSlots();
   }

   public int getField(int i) {
      return 0;
   }

   public void setField(int i, int j) {
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
