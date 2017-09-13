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

   public void onOpen(CraftHumanEntity who) {
      this.transaction.add(who);
   }

   public InventoryType getInvType() {
      return this.stackList.length == 4 ? InventoryType.CRAFTING : InventoryType.WORKBENCH;
   }

   public void onClose(CraftHumanEntity who) {
      this.transaction.remove(who);
   }

   public List getViewers() {
      return this.transaction;
   }

   public InventoryHolder getOwner() {
      return this.owner == null ? null : this.owner.getBukkitEntity();
   }

   public void setMaxStackSize(int size) {
      this.maxStack = size;
      this.resultInventory.setMaxStackSize(size);
   }

   public Location getLocation() {
      return this.owner.getBukkitEntity().getLocation();
   }

   public InventoryCrafting(Container container, int i, int j, EntityPlayer player) {
      this(container, i, j);
      this.owner = player;
   }

   public InventoryCrafting(Container container, int i, int j) {
      this.transaction = new ArrayList();
      this.maxStack = 64;
      int k = i * j;
      this.stackList = new ItemStack[k];
      this.eventHandler = container;
      this.inventoryWidth = i;
      this.inventoryHeight = j;
   }

   public int getSizeInventory() {
      return this.stackList.length;
   }

   @Nullable
   public ItemStack getStackInSlot(int i) {
      return i >= this.getSizeInventory() ? null : this.stackList[i];
   }

   @Nullable
   public ItemStack getStackInRowAndColumn(int i, int j) {
      return i >= 0 && i < this.inventoryWidth && j >= 0 && j <= this.inventoryHeight ? this.getStackInSlot(i + j * this.inventoryWidth) : null;
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
   public ItemStack removeStackFromSlot(int i) {
      return ItemStackHelper.getAndRemove(this.stackList, i);
   }

   @Nullable
   public ItemStack decrStackSize(int i, int j) {
      ItemStack itemstack = ItemStackHelper.getAndSplit(this.stackList, i, j);
      if (itemstack != null) {
         this.eventHandler.onCraftMatrixChanged(this);
      }

      return itemstack;
   }

   public void setInventorySlotContents(int i, @Nullable ItemStack itemstack) {
      this.stackList[i] = itemstack;
      this.eventHandler.onCraftMatrixChanged(this);
   }

   public int getInventoryStackLimit() {
      return 64;
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
      for(int i = 0; i < this.stackList.length; ++i) {
         this.stackList[i] = null;
      }

   }

   public int getHeight() {
      return this.inventoryHeight;
   }

   public int getWidth() {
      return this.inventoryWidth;
   }
}
