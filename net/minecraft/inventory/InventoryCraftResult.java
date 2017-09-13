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

   public void onOpen(CraftHumanEntity var1) {
   }

   public void onClose(CraftHumanEntity var1) {
   }

   public List getViewers() {
      return new ArrayList();
   }

   public void setMaxStackSize(int var1) {
      this.maxStack = var1;
   }

   public Location getLocation() {
      return null;
   }

   public int getSizeInventory() {
      return 1;
   }

   @Nullable
   public ItemStack getStackInSlot(int var1) {
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
   public ItemStack decrStackSize(int var1, int var2) {
      return ItemStackHelper.getAndRemove(this.stackResult, 0);
   }

   @Nullable
   public ItemStack removeStackFromSlot(int var1) {
      return ItemStackHelper.getAndRemove(this.stackResult, 0);
   }

   public void setInventorySlotContents(int var1, @Nullable ItemStack var2) {
      this.stackResult[0] = var2;
   }

   public int getInventoryStackLimit() {
      return this.maxStack;
   }

   public void markDirty() {
   }

   public boolean isUsableByPlayer(EntityPlayer var1) {
      return true;
   }

   public void openInventory(EntityPlayer var1) {
   }

   public void closeInventory(EntityPlayer var1) {
   }

   public boolean isItemValidForSlot(int var1, ItemStack var2) {
      return true;
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
      for(int var1 = 0; var1 < this.stackResult.length; ++var1) {
         this.stackResult[var1] = null;
      }

   }
}
