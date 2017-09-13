package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityBeacon;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryBeacon;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryView;

public class ContainerBeacon extends Container {
   private final IInventory tileBeacon;
   private final ContainerBeacon.BeaconSlot beaconSlot;
   private CraftInventoryView bukkitEntity = null;
   private InventoryPlayer player;

   public ContainerBeacon(IInventory var1, IInventory var2) {
      this.player = (InventoryPlayer)var1;
      this.tileBeacon = var2;
      this.beaconSlot = new ContainerBeacon.BeaconSlot(var2, 0, 136, 110);
      this.addSlotToContainer(this.beaconSlot);

      for(int var3 = 0; var3 < 3; ++var3) {
         for(int var4 = 0; var4 < 9; ++var4) {
            this.addSlotToContainer(new Slot(var1, var4 + var3 * 9 + 9, 36 + var4 * 18, 137 + var3 * 18));
         }
      }

      for(int var5 = 0; var5 < 9; ++var5) {
         this.addSlotToContainer(new Slot(var1, var5, 36 + var5 * 18, 195));
      }

   }

   public void addListener(IContainerListener var1) {
      super.addListener(var1);
      var1.sendAllWindowProperties(this, this.tileBeacon);
   }

   public IInventory getTileEntity() {
      return this.tileBeacon;
   }

   public void onContainerClosed(EntityPlayer var1) {
      super.onContainerClosed(var1);
      if (var1 != null && !var1.world.isRemote) {
         ItemStack var2 = this.beaconSlot.decrStackSize(this.beaconSlot.getSlotStackLimit());
         if (var2 != null) {
            var1.dropItem(var2, false);
         }
      }

   }

   public boolean canInteractWith(EntityPlayer var1) {
      return !this.checkReachable ? true : this.tileBeacon.isUsableByPlayer(var1);
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer var1, int var2) {
      ItemStack var3 = null;
      Slot var4 = (Slot)this.inventorySlots.get(var2);
      if (var4 != null && var4.getHasStack()) {
         ItemStack var5 = var4.getStack();
         var3 = var5.copy();
         if (var2 == 0) {
            if (!this.mergeItemStack(var5, 1, 37, true)) {
               return null;
            }

            var4.onSlotChange(var5, var3);
         } else if (!this.beaconSlot.getHasStack() && this.beaconSlot.isItemValid(var5) && var5.stackSize == 1) {
            if (!this.mergeItemStack(var5, 0, 1, false)) {
               return null;
            }
         } else if (var2 >= 1 && var2 < 28) {
            if (!this.mergeItemStack(var5, 28, 37, false)) {
               return null;
            }
         } else if (var2 >= 28 && var2 < 37) {
            if (!this.mergeItemStack(var5, 1, 28, false)) {
               return null;
            }
         } else if (!this.mergeItemStack(var5, 1, 37, false)) {
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
         CraftInventoryBeacon var1 = new CraftInventoryBeacon((TileEntityBeacon)this.tileBeacon);
         this.bukkitEntity = new CraftInventoryView(this.player.player.getBukkitEntity(), var1, this);
         return this.bukkitEntity;
      }
   }

   class BeaconSlot extends Slot {
      public BeaconSlot(IInventory var2, int var3, int var4, int var5) {
         super(var2, var3, var4, var5);
      }

      public boolean isItemValid(@Nullable ItemStack var1) {
         return var1 == null ? false : var1.getItem() == Items.EMERALD || var1.getItem() == Items.DIAMOND || var1.getItem() == Items.GOLD_INGOT || var1.getItem() == Items.IRON_INGOT;
      }

      public int getSlotStackLimit() {
         return 1;
      }
   }
}
