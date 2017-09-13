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

   public void onOpen(CraftHumanEntity var1) {
      this.transaction.add(var1);
   }

   public void onClose(CraftHumanEntity var1) {
      this.transaction.remove(var1);
   }

   public List getViewers() {
      return this.transaction;
   }

   public void setMaxStackSize(int var1) {
      this.maxStack = var1;
   }

   public int getSizeInventory() {
      return 9;
   }

   @Nullable
   public ItemStack getStackInSlot(int var1) {
      this.fillWithLoot((EntityPlayer)null);
      return this.stacks[var1];
   }

   @Nullable
   public ItemStack decrStackSize(int var1, int var2) {
      this.fillWithLoot((EntityPlayer)null);
      ItemStack var3 = ItemStackHelper.getAndSplit(this.stacks, var1, var2);
      if (var3 != null) {
         this.markDirty();
      }

      return var3;
   }

   @Nullable
   public ItemStack removeStackFromSlot(int var1) {
      this.fillWithLoot((EntityPlayer)null);
      return ItemStackHelper.getAndRemove(this.stacks, var1);
   }

   public int getDispenseSlot() {
      this.fillWithLoot((EntityPlayer)null);
      int var1 = -1;
      int var2 = 1;

      for(int var3 = 0; var3 < this.stacks.length; ++var3) {
         if (this.stacks[var3] != null && RNG.nextInt(var2++) == 0 && this.stacks[var3].stackSize != 0) {
            var1 = var3;
         }
      }

      return var1;
   }

   public void setInventorySlotContents(int var1, @Nullable ItemStack var2) {
      this.fillWithLoot((EntityPlayer)null);
      this.stacks[var1] = var2;
      if (var2 != null && var2.stackSize > this.getInventoryStackLimit()) {
         var2.stackSize = this.getInventoryStackLimit();
      }

      this.markDirty();
   }

   public int addItemStack(ItemStack var1) {
      for(int var2 = 0; var2 < this.stacks.length; ++var2) {
         if (this.stacks[var2] == null || this.stacks[var2].getItem() == null) {
            this.setInventorySlotContents(var2, var1);
            return var2;
         }
      }

      return -1;
   }

   public String getName() {
      return this.hasCustomName() ? this.customName : "container.dispenser";
   }

   public void setCustomName(String var1) {
      this.customName = var1;
   }

   public boolean hasCustomName() {
      return this.customName != null;
   }

   public static void registerFixes(DataFixer var0) {
      var0.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists("Trap", new String[]{"Items"}));
   }

   public void readFromNBT(NBTTagCompound var1) {
      super.readFromNBT(var1);
      if (!this.checkLootAndRead(var1)) {
         NBTTagList var2 = var1.getTagList("Items", 10);
         this.stacks = new ItemStack[this.getSizeInventory()];

         for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
            NBTTagCompound var4 = var2.getCompoundTagAt(var3);
            int var5 = var4.getByte("Slot") & 255;
            if (var5 >= 0 && var5 < this.stacks.length) {
               this.stacks[var5] = ItemStack.loadItemStackFromNBT(var4);
            }
         }
      }

      if (var1.hasKey("CustomName", 8)) {
         this.customName = var1.getString("CustomName");
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      super.writeToNBT(var1);
      if (!this.checkLootAndWrite(var1)) {
         NBTTagList var2 = new NBTTagList();

         for(int var3 = 0; var3 < this.stacks.length; ++var3) {
            if (this.stacks[var3] != null) {
               NBTTagCompound var4 = new NBTTagCompound();
               var4.setByte("Slot", (byte)var3);
               this.stacks[var3].writeToNBT(var4);
               var2.appendTag(var4);
            }
         }

         var1.setTag("Items", var2);
      }

      if (this.hasCustomName()) {
         var1.setString("CustomName", this.customName);
      }

      return var1;
   }

   public int getInventoryStackLimit() {
      return this.maxStack;
   }

   public boolean isUsableByPlayer(EntityPlayer var1) {
      return this.world.getTileEntity(this.pos) != this ? false : var1.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
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
      this.fillWithLoot(var2);
      return new ContainerDispenser(var1, this);
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

      for(int var1 = 0; var1 < this.stacks.length; ++var1) {
         this.stacks[var1] = null;
      }

   }
}
