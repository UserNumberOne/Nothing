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

   public void onOpen(CraftHumanEntity var1) {
      this.transaction.add(var1);
   }

   public void onClose(CraftHumanEntity var1) {
      this.transaction.remove(var1);
   }

   public List getViewers() {
      return this.transaction;
   }

   public InventoryHolder getOwner() {
      return this.player;
   }

   public void setMaxStackSize(int var1) {
      this.maxStack = var1;
   }

   public int getInventoryStackLimit() {
      return this.maxStack;
   }

   public InventoryEnderChest() {
      super("container.enderchest", false, 27);
   }

   public void setChestTileEntity(TileEntityEnderChest var1) {
      this.associatedChest = var1;
   }

   public void loadInventoryFromNBT(NBTTagList var1) {
      for(int var2 = 0; var2 < this.getSizeInventory(); ++var2) {
         this.setInventorySlotContents(var2, (ItemStack)null);
      }

      for(int var5 = 0; var5 < var1.tagCount(); ++var5) {
         NBTTagCompound var3 = var1.getCompoundTagAt(var5);
         int var4 = var3.getByte("Slot") & 255;
         if (var4 >= 0 && var4 < this.getSizeInventory()) {
            this.setInventorySlotContents(var4, ItemStack.loadItemStackFromNBT(var3));
         }
      }

   }

   public NBTTagList saveInventoryToNBT() {
      NBTTagList var1 = new NBTTagList();

      for(int var2 = 0; var2 < this.getSizeInventory(); ++var2) {
         ItemStack var3 = this.getStackInSlot(var2);
         if (var3 != null) {
            NBTTagCompound var4 = new NBTTagCompound();
            var4.setByte("Slot", (byte)var2);
            var3.writeToNBT(var4);
            var1.appendTag(var4);
         }
      }

      return var1;
   }

   public boolean isUsableByPlayer(EntityPlayer var1) {
      return this.associatedChest != null && !this.associatedChest.canBeUsed(var1) ? false : super.isUsableByPlayer(var1);
   }

   public void openInventory(EntityPlayer var1) {
      if (this.associatedChest != null) {
         this.associatedChest.openChest();
      }

      super.openInventory(var1);
   }

   public void closeInventory(EntityPlayer var1) {
      if (this.associatedChest != null) {
         this.associatedChest.closeChest();
      }

      super.closeInventory(var1);
      this.associatedChest = null;
   }
}
