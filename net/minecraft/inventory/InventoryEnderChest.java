package net.minecraft.inventory;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityEnderChest;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftHumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

public class InventoryEnderChest extends InventoryBasic {
   private TileEntityEnderChest associatedChest;
   public List transaction = new ArrayList();
   public Player player;
   private int maxStack = 64;

   public ItemStack[] getContents() {
      return this.inventoryContents;
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

   public InventoryHolder getOwner() {
      return this.player;
   }

   public void setMaxStackSize(int size) {
      this.maxStack = size;
   }

   public int getInventoryStackLimit() {
      return this.maxStack;
   }

   public InventoryEnderChest() {
      super("container.enderchest", false, 27);
   }

   public void setChestTileEntity(TileEntityEnderChest tileentityenderchest) {
      this.associatedChest = tileentityenderchest;
   }

   public void loadInventoryFromNBT(NBTTagList nbttaglist) {
      for(int i = 0; i < this.getSizeInventory(); ++i) {
         this.setInventorySlotContents(i, (ItemStack)null);
      }

      for(int var5 = 0; var5 < nbttaglist.tagCount(); ++var5) {
         NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(var5);
         int j = nbttagcompound.getByte("Slot") & 255;
         if (j >= 0 && j < this.getSizeInventory()) {
            this.setInventorySlotContents(j, ItemStack.loadItemStackFromNBT(nbttagcompound));
         }
      }

   }

   public NBTTagList saveInventoryToNBT() {
      NBTTagList nbttaglist = new NBTTagList();

      for(int i = 0; i < this.getSizeInventory(); ++i) {
         ItemStack itemstack = this.getStackInSlot(i);
         if (itemstack != null) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setByte("Slot", (byte)i);
            itemstack.writeToNBT(nbttagcompound);
            nbttaglist.appendTag(nbttagcompound);
         }
      }

      return nbttaglist;
   }

   public boolean isUsableByPlayer(EntityPlayer entityhuman) {
      return this.associatedChest != null && !this.associatedChest.canBeUsed(entityhuman) ? false : super.isUsableByPlayer(entityhuman);
   }

   public void openInventory(EntityPlayer entityhuman) {
      if (this.associatedChest != null) {
         this.associatedChest.openChest();
      }

      super.openInventory(entityhuman);
   }

   public void closeInventory(EntityPlayer entityhuman) {
      if (this.associatedChest != null) {
         this.associatedChest.closeChest();
      }

      super.closeInventory(entityhuman);
      this.associatedChest = null;
   }
}
