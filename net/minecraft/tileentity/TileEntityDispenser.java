package net.minecraft.tileentity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerDispenser;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftHumanEntity;

public class TileEntityDispenser extends TileEntityLockableLoot implements IInventory {
   private static final Random RNG = new Random();
   private ItemStack[] stacks = new ItemStack[9];
   protected String customName;
   public List transaction = new ArrayList();
   private int maxStack = 64;

   public ItemStack[] getContents() {
      return this.stacks;
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

   public void setMaxStackSize(int size) {
      this.maxStack = size;
   }

   public int getSizeInventory() {
      return 9;
   }

   @Nullable
   public ItemStack getStackInSlot(int i) {
      this.fillWithLoot((EntityPlayer)null);
      return this.stacks[i];
   }

   @Nullable
   public ItemStack decrStackSize(int i, int j) {
      this.fillWithLoot((EntityPlayer)null);
      ItemStack itemstack = ItemStackHelper.getAndSplit(this.stacks, i, j);
      if (itemstack != null) {
         this.markDirty();
      }

      return itemstack;
   }

   @Nullable
   public ItemStack removeStackFromSlot(int i) {
      this.fillWithLoot((EntityPlayer)null);
      return ItemStackHelper.getAndRemove(this.stacks, i);
   }

   public int getDispenseSlot() {
      this.fillWithLoot((EntityPlayer)null);
      int i = -1;
      int j = 1;

      for(int k = 0; k < this.stacks.length; ++k) {
         if (this.stacks[k] != null && RNG.nextInt(j++) == 0 && this.stacks[k].stackSize != 0) {
            i = k;
         }
      }

      return i;
   }

   public void setInventorySlotContents(int i, @Nullable ItemStack itemstack) {
      this.fillWithLoot((EntityPlayer)null);
      this.stacks[i] = itemstack;
      if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit()) {
         itemstack.stackSize = this.getInventoryStackLimit();
      }

      this.markDirty();
   }

   public int addItemStack(ItemStack itemstack) {
      for(int i = 0; i < this.stacks.length; ++i) {
         if (this.stacks[i] == null || this.stacks[i].getItem() == null) {
            this.setInventorySlotContents(i, itemstack);
            return i;
         }
      }

      return -1;
   }

   public String getName() {
      return this.hasCustomName() ? this.customName : "container.dispenser";
   }

   public void setCustomName(String s) {
      this.customName = s;
   }

   public boolean hasCustomName() {
      return this.customName != null;
   }

   public static void registerFixes(DataFixer dataconvertermanager) {
      dataconvertermanager.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists("Trap", new String[]{"Items"}));
   }

   public void readFromNBT(NBTTagCompound nbttagcompound) {
      super.readFromNBT(nbttagcompound);
      if (!this.checkLootAndRead(nbttagcompound)) {
         NBTTagList nbttaglist = nbttagcompound.getTagList("Items", 10);
         this.stacks = new ItemStack[this.getSizeInventory()];

         for(int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound1.getByte("Slot") & 255;
            if (j >= 0 && j < this.stacks.length) {
               this.stacks[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
            }
         }
      }

      if (nbttagcompound.hasKey("CustomName", 8)) {
         this.customName = nbttagcompound.getString("CustomName");
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
      super.writeToNBT(nbttagcompound);
      if (!this.checkLootAndWrite(nbttagcompound)) {
         NBTTagList nbttaglist = new NBTTagList();

         for(int i = 0; i < this.stacks.length; ++i) {
            if (this.stacks[i] != null) {
               NBTTagCompound nbttagcompound1 = new NBTTagCompound();
               nbttagcompound1.setByte("Slot", (byte)i);
               this.stacks[i].writeToNBT(nbttagcompound1);
               nbttaglist.appendTag(nbttagcompound1);
            }
         }

         nbttagcompound.setTag("Items", nbttaglist);
      }

      if (this.hasCustomName()) {
         nbttagcompound.setString("CustomName", this.customName);
      }

      return nbttagcompound;
   }

   public int getInventoryStackLimit() {
      return this.maxStack;
   }

   public boolean isUsableByPlayer(EntityPlayer entityhuman) {
      return this.world.getTileEntity(this.pos) != this ? false : entityhuman.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
   }

   public void openInventory(EntityPlayer entityhuman) {
   }

   public void closeInventory(EntityPlayer entityhuman) {
   }

   public boolean isItemValidForSlot(int i, ItemStack itemstack) {
      return true;
   }

   public String getGuiID() {
      return "minecraft:dispenser";
   }

   public Container createContainer(InventoryPlayer playerinventory, EntityPlayer entityhuman) {
      this.fillWithLoot(entityhuman);
      return new ContainerDispenser(playerinventory, this);
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
      this.fillWithLoot((EntityPlayer)null);

      for(int i = 0; i < this.stacks.length; ++i) {
         this.stacks[i] = null;
      }

   }
}
