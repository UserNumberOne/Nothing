package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryView;

public class ContainerHopper extends Container {
   private final IInventory hopperInventory;
   private CraftInventoryView bukkitEntity = null;
   private InventoryPlayer player;

   public CraftInventoryView getBukkitView() {
      if (this.bukkitEntity != null) {
         return this.bukkitEntity;
      } else {
         CraftInventory var1 = new CraftInventory(this.hopperInventory);
         this.bukkitEntity = new CraftInventoryView(this.player.player.getBukkitEntity(), var1, this);
         return this.bukkitEntity;
      }
   }

   public ContainerHopper(InventoryPlayer var1, IInventory var2, EntityPlayer var3) {
      this.hopperInventory = var2;
      this.player = var1;
      var2.openInventory(var3);

      for(int var4 = 0; var4 < var2.getSizeInventory(); ++var4) {
         this.addSlotToContainer(new Slot(var2, var4, 44 + var4 * 18, 20));
      }

      for(int var6 = 0; var6 < 3; ++var6) {
         for(int var5 = 0; var5 < 9; ++var5) {
            this.addSlotToContainer(new Slot(var1, var5 + var6 * 9 + 9, 8 + var5 * 18, var6 * 18 + 51));
         }
      }

      for(int var7 = 0; var7 < 9; ++var7) {
         this.addSlotToContainer(new Slot(var1, var7, 8 + var7 * 18, 109));
      }

   }

   public boolean canInteractWith(EntityPlayer var1) {
      return !this.checkReachable ? true : this.hopperInventory.isUsableByPlayer(var1);
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer var1, int var2) {
      ItemStack var3 = null;
      Slot var4 = (Slot)this.inventorySlots.get(var2);
      if (var4 != null && var4.getHasStack()) {
         ItemStack var5 = var4.getStack();
         var3 = var5.copy();
         if (var2 < this.hopperInventory.getSizeInventory()) {
            if (!this.mergeItemStack(var5, this.hopperInventory.getSizeInventory(), this.inventorySlots.size(), true)) {
               return null;
            }
         } else if (!this.mergeItemStack(var5, 0, this.hopperInventory.getSizeInventory(), false)) {
            return null;
         }

         if (var5.stackSize == 0) {
            var4.putStack((ItemStack)null);
         } else {
            var4.onSlotChanged();
         }
      }

      return var3;
   }

   public void onContainerClosed(EntityPlayer var1) {
      super.onContainerClosed(var1);
      this.hopperInventory.closeInventory(var1);
   }
}
