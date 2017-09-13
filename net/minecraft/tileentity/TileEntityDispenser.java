package net.minecraft.tileentity;

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

public class TileEntityDispenser extends TileEntityLockableLoot implements IInventory {
   private static final Random RNG = new Random();
   private ItemStack[] stacks = new ItemStack[9];
   protected String customName;

   public int getSizeInventory() {
      return 9;
   }

   @Nullable
   public ItemStack getStackInSlot(int var1) {
      this.fillWithLoot((EntityPlayer)null);
      return this.stacks[index];
   }

   @Nullable
   public ItemStack decrStackSize(int var1, int var2) {
      this.fillWithLoot((EntityPlayer)null);
      ItemStack itemstack = ItemStackHelper.getAndSplit(this.stacks, index, count);
      if (itemstack != null) {
         this.markDirty();
      }

      return itemstack;
   }

   @Nullable
   public ItemStack removeStackFromSlot(int var1) {
      this.fillWithLoot((EntityPlayer)null);
      return ItemStackHelper.getAndRemove(this.stacks, index);
   }

   public int getDispenseSlot() {
      this.fillWithLoot((EntityPlayer)null);
      int i = -1;
      int j = 1;

      for(int k = 0; k < this.stacks.length; ++k) {
         if (this.stacks[k] != null && RNG.nextInt(j++) == 0) {
            i = k;
         }
      }

      return i;
   }

   public void setInventorySlotContents(int var1, @Nullable ItemStack var2) {
      this.fillWithLoot((EntityPlayer)null);
      this.stacks[index] = stack;
      if (stack != null && stack.stackSize > this.getInventoryStackLimit()) {
         stack.stackSize = this.getInventoryStackLimit();
      }

      this.markDirty();
   }

   public int addItemStack(ItemStack var1) {
      for(int i = 0; i < this.stacks.length; ++i) {
         if (this.stacks[i] == null || this.stacks[i].getItem() == null) {
            this.setInventorySlotContents(i, stack);
            return i;
         }
      }

      return -1;
   }

   public String getName() {
      return this.hasCustomName() ? this.customName : "container.dispenser";
   }

   public void setCustomName(String var1) {
      this.customName = customName;
   }

   public boolean hasCustomName() {
      return this.customName != null;
   }

   public static void registerFixes(DataFixer var0) {
      fixer.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists("Trap", new String[]{"Items"}));
   }

   public void readFromNBT(NBTTagCompound var1) {
      super.readFromNBT(compound);
      if (!this.checkLootAndRead(compound)) {
         NBTTagList nbttaglist = compound.getTagList("Items", 10);
         this.stacks = new ItemStack[this.getSizeInventory()];

         for(int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 255;
            if (j >= 0 && j < this.stacks.length) {
               this.stacks[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
            }
         }
      }

      if (compound.hasKey("CustomName", 8)) {
         this.customName = compound.getString("CustomName");
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      super.writeToNBT(compound);
      if (!this.checkLootAndWrite(compound)) {
         NBTTagList nbttaglist = new NBTTagList();

         for(int i = 0; i < this.stacks.length; ++i) {
            if (this.stacks[i] != null) {
               NBTTagCompound nbttagcompound = new NBTTagCompound();
               nbttagcompound.setByte("Slot", (byte)i);
               this.stacks[i].writeToNBT(nbttagcompound);
               nbttaglist.appendTag(nbttagcompound);
            }
         }

         compound.setTag("Items", nbttaglist);
      }

      if (this.hasCustomName()) {
         compound.setString("CustomName", this.customName);
      }

      return compound;
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public boolean isUsableByPlayer(EntityPlayer var1) {
      return this.world.getTileEntity(this.pos) != this ? false : player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
   }

   public void openInventory(EntityPlayer var1) {
   }

   public void closeInventory(EntityPlayer var1) {
   }

   public boolean isItemValidForSlot(int var1, ItemStack var2) {
      return true;
   }

   public String getGuiID() {
      return "minecraft:dispenser";
   }

   public Container createContainer(InventoryPlayer var1, EntityPlayer var2) {
      this.fillWithLoot(playerIn);
      return new ContainerDispenser(playerInventory, this);
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
      this.fillWithLoot((EntityPlayer)null);

      for(int i = 0; i < this.stacks.length; ++i) {
         this.stacks[i] = null;
      }

   }
}
