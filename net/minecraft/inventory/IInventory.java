package net.minecraft.inventory;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IWorldNameable;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftHumanEntity;
import org.bukkit.inventory.InventoryHolder;

public interface IInventory extends IWorldNameable {
   int MAX_STACK = 64;

   int getSizeInventory();

   @Nullable
   ItemStack getStackInSlot(int var1);

   @Nullable
   ItemStack decrStackSize(int var1, int var2);

   @Nullable
   ItemStack removeStackFromSlot(int var1);

   void setInventorySlotContents(int var1, @Nullable ItemStack var2);

   int getInventoryStackLimit();

   void markDirty();

   boolean isUsableByPlayer(EntityPlayer var1);

   void openInventory(EntityPlayer var1);

   void closeInventory(EntityPlayer var1);

   boolean isItemValidForSlot(int var1, ItemStack var2);

   int getField(int var1);

   void setField(int var1, int var2);

   int getFieldCount();

   void clear();

   ItemStack[] getContents();

   void onOpen(CraftHumanEntity var1);

   void onClose(CraftHumanEntity var1);

   List getViewers();

   InventoryHolder getOwner();

   void setMaxStackSize(int var1);

   Location getLocation();
}
