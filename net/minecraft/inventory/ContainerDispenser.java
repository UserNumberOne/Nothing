package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryView;

public class ContainerDispenser extends Container {
   public final IInventory dispenserInventory;
   private CraftInventoryView bukkitEntity = null;
   private InventoryPlayer player;

   public ContainerDispenser(IInventory var1, IInventory var2) {
      this.dispenserInventory = var2;
      this.player = (InventoryPlayer)var1;

      for(int var3 = 0; var3 < 3; ++var3) {
         for(int var4 = 0; var4 < 3; ++var4) {
            this.addSlotToContainer(new Slot(var2, var4 + var3 * 3, 62 + var4 * 18, 17 + var3 * 18));
         }
      }

      for(int var5 = 0; var5 < 3; ++var5) {
         for(int var7 = 0; var7 < 9; ++var7) {
            this.addSlotToContainer(new Slot(var1, var7 + var5 * 9 + 9, 8 + var7 * 18, 84 + var5 * 18));
         }
      }

      for(int var6 = 0; var6 < 9; ++var6) {
         this.addSlotToContainer(new Slot(var1, var6, 8 + var6 * 18, 142));
      }

   }

   public boolean canInteractWith(EntityPlayer var1) {
      return !this.checkReachable ? true : this.dispenserInventory.isUsableByPlayer(var1);
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer var1, int var2) {
      ItemStack var3 = null;
      Slot var4 = (Slot)this.inventorySlots.get(var2);
      if (var4 != null && var4.getHasStack()) {
         ItemStack var5 = var4.getStack();
         var3 = var5.copy();
         if (var2 < 9) {
            if (!this.mergeItemStack(var5, 9, 45, true)) {
               return null;
            }
         } else if (!this.mergeItemStack(var5, 0, 9, false)) {
            return null;
         }

         if (var5.stackSize == 0) {
            var4.putStack((ItemStack)null);
         } else {
            var4.onSlotChanged();
         }

         if (var5.stackSize == var3.stackSize) {
            return null;
         }

         var4.onPickupFromSlot(var1, var5);
      }

      return var3;
   }

   public CraftInventoryView getBukkitView() {
      if (this.bukkitEntity != null) {
         return this.bukkitEntity;
      } else {
         CraftInventory var1 = new CraftInventory(this.dispenserInventory);
         this.bukkitEntity = new CraftInventoryView(this.player.player.getBukkitEntity(), var1, this);
         return this.bukkitEntity;
      }
   }
}
