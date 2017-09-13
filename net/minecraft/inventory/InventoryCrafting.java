package net.minecraft.inventory;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftHumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;

public class InventoryCrafting implements IInventory {
   private final ItemStack[] stackList;
   private final int inventoryWidth;
   private final int inventoryHeight;
   private final Container eventHandler;
   public List transaction;
   public IRecipe currentRecipe;
   public IInventory resultInventory;
   private EntityPlayer owner;
   private int maxStack;

   public ItemStack[] getContents() {
      return this.stackList;
   }

   public void onOpen(CraftHumanEntity var1) {
      this.transaction.add(var1);
   }

   public InventoryType getInvType() {
      return this.stackList.length == 4 ? InventoryType.CRAFTING : InventoryType.WORKBENCH;
   }

   public void onClose(CraftHumanEntity var1) {
      this.transaction.remove(var1);
   }

   public List getViewers() {
      return this.transaction;
   }

   public InventoryHolder getOwner() {
      return this.owner == null ? null : this.owner.getBukkitEntity();
   }

   public void setMaxStackSize(int var1) {
      this.maxStack = var1;
      this.resultInventory.setMaxStackSize(var1);
   }

   public Location getLocation() {
      return this.owner.getBukkitEntity().getLocation();
   }

   public InventoryCrafting(Container var1, int var2, int var3, EntityPlayer var4) {
      this(var1, var2, var3);
      this.owner = var4;
   }

   public InventoryCrafting(Container var1, int var2, int var3) {
      this.transaction = new ArrayList();
      this.maxStack = 64;
      int var4 = var2 * var3;
      this.stackList = new ItemStack[var4];
      this.eventHandler = var1;
      this.inventoryWidth = var2;
      this.inventoryHeight = var3;
   }

   public int getSizeInventory() {
      return this.stackList.length;
   }

   @Nullable
   public ItemStack getStackInSlot(int var1) {
      return var1 >= this.getSizeInventory() ? null : this.stackList[var1];
   }

   @Nullable
   public ItemStack getStackInRowAndColumn(int var1, int var2) {
      return var1 >= 0 && var1 < this.inventoryWidth && var2 >= 0 && var2 <= this.inventoryHeight ? this.getStackInSlot(var1 + var2 * this.inventoryWidth) : null;
   }

   public String getName() {
      return "container.crafting";
   }

   public boolean hasCustomName() {
      return false;
   }

   public ITextComponent getDisplayName() {
      return (ITextComponent)(this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName(), new Object[0]));
   }

   @Nullable
   public ItemStack removeStackFromSlot(int var1) {
      return ItemStackHelper.getAndRemove(this.stackList, var1);
   }

   @Nullable
   public ItemStack decrStackSize(int var1, int var2) {
      ItemStack var3 = ItemStackHelper.getAndSplit(this.stackList, var1, var2);
      if (var3 != null) {
         this.eventHandler.onCraftMatrixChanged(this);
      }

      return var3;
   }

   public void setInventorySlotContents(int var1, @Nullable ItemStack var2) {
      this.stackList[var1] = var2;
      this.eventHandler.onCraftMatrixChanged(this);
   }

   public int getInventoryStackLimit() {
      return 64;
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
      for(int var1 = 0; var1 < this.stackList.length; ++var1) {
         this.stackList[var1] = null;
      }

   }

   public int getHeight() {
      return this.inventoryHeight;
   }

   public int getWidth() {
      return this.inventoryWidth;
   }
}
