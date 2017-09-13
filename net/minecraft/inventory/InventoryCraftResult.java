package net.minecraft.inventory;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftHumanEntity;
import org.bukkit.inventory.InventoryHolder;

public class InventoryCraftResult implements IInventory {
   private final ItemStack[] stackResult = new ItemStack[1];
   private int maxStack = 64;

   public ItemStack[] getContents() {
      return this.stackResult;
   }

   public InventoryHolder getOwner() {
      return null;
   }

   public void onOpen(CraftHumanEntity who) {
   }

   public void onClose(CraftHumanEntity who) {
   }

   public List getViewers() {
      return new ArrayList();
   }

   public void setMaxStackSize(int size) {
      this.maxStack = size;
   }

   public Location getLocation() {
      return null;
   }

   public int getSizeInventory() {
      return 1;
   }

   @Nullable
   public ItemStack getStackInSlot(int i) {
      return this.stackResult[0];
   }

   public String getName() {
      return "Result";
   }

   public boolean hasCustomName() {
      return false;
   }

   public ITextComponent getDisplayName() {
      return (ITextComponent)(this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName(), new Object[0]));
   }

   @Nullable
   public ItemStack decrStackSize(int i, int j) {
      return ItemStackHelper.getAndRemove(this.stackResult, 0);
   }

   @Nullable
   public ItemStack removeStackFromSlot(int i) {
      return ItemStackHelper.getAndRemove(this.stackResult, 0);
   }

   public void setInventorySlotContents(int i, @Nullable ItemStack itemstack) {
      this.stackResult[0] = itemstack;
   }

   public int getInventoryStackLimit() {
      return this.maxStack;
   }

   public void markDirty() {
   }

   public boolean isUsableByPlayer(EntityPlayer entityhuman) {
      return true;
   }

   public void openInventory(EntityPlayer entityhuman) {
   }

   public void closeInventory(EntityPlayer entityhuman) {
   }

   public boolean isItemValidForSlot(int i, ItemStack itemstack) {
      return true;
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
      for(int i = 0; i < this.stackResult.length; ++i) {
         this.stackResult[i] = null;
      }

   }
}
