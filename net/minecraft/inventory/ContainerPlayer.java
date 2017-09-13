package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.network.play.server.SPacketSetSlot;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryCrafting;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryView;

public class ContainerPlayer extends Container {
   private static final EntityEquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EntityEquipmentSlot[]{EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};
   public InventoryCrafting craftMatrix = new InventoryCrafting(this, 2, 2);
   public IInventory craftResult = new InventoryCraftResult();
   public boolean isLocalWorld;
   private final EntityPlayer player;
   private CraftInventoryView bukkitEntity = null;
   private InventoryPlayer player;

   public ContainerPlayer(InventoryPlayer var1, boolean var2, EntityPlayer var3) {
      this.isLocalWorld = var2;
      this.player = var3;
      this.craftResult = new InventoryCraftResult();
      this.craftMatrix = new InventoryCrafting(this, 2, 2, var1.player);
      this.craftMatrix.resultInventory = this.craftResult;
      this.player = var1;
      this.addSlotToContainer(new SlotCrafting(var1.player, this.craftMatrix, this.craftResult, 0, 154, 28));

      for(int var4 = 0; var4 < 2; ++var4) {
         for(int var5 = 0; var5 < 2; ++var5) {
            this.addSlotToContainer(new Slot(this.craftMatrix, var5 + var4 * 2, 98 + var5 * 18, 18 + var4 * 18));
         }
      }

      for(int var7 = 0; var7 < 4; ++var7) {
         final EntityEquipmentSlot var6 = VALID_EQUIPMENT_SLOTS[var7];
         this.addSlotToContainer(new Slot(var1, 36 + (3 - var7), 8, 8 + var7 * 18) {
            public int getSlotStackLimit() {
               return 1;
            }

            public boolean isItemValid(@Nullable ItemStack var1) {
               if (var1 == null) {
                  return false;
               } else {
                  EntityEquipmentSlot var2 = EntityLiving.getSlotForItemStack(var1);
                  return var2 == var6;
               }
            }
         });
      }

      for(int var8 = 0; var8 < 3; ++var8) {
         for(int var10 = 0; var10 < 9; ++var10) {
            this.addSlotToContainer(new Slot(var1, var10 + (var8 + 1) * 9, 8 + var10 * 18, 84 + var8 * 18));
         }
      }

      for(int var9 = 0; var9 < 9; ++var9) {
         this.addSlotToContainer(new Slot(var1, var9, 8 + var9 * 18, 142));
      }

      this.addSlotToContainer(new Slot(var1, 40, 77, 62) {
         public boolean isItemValid(@Nullable ItemStack var1) {
            return super.isItemValid(var1);
         }
      });
   }

   public void onCraftMatrixChanged(IInventory var1) {
      CraftingManager.getInstance().lastCraftView = this.getBukkitView();
      ItemStack var2 = CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.player.world);
      this.craftResult.setInventorySlotContents(0, var2);
      if (super.listeners.size() >= 1) {
         EntityPlayerMP var3 = (EntityPlayerMP)super.listeners.get(0);
         var3.connection.sendPacket(new SPacketSetSlot(var3.openContainer.windowId, 0, var2));
      }
   }

   public void onContainerClosed(EntityPlayer var1) {
      super.onContainerClosed(var1);

      for(int var2 = 0; var2 < 4; ++var2) {
         ItemStack var3 = this.craftMatrix.removeStackFromSlot(var2);
         if (var3 != null) {
            var1.dropItem(var3, false);
         }
      }

      this.craftResult.setInventorySlotContents(0, (ItemStack)null);
   }

   public boolean canInteractWith(EntityPlayer var1) {
      return true;
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer var1, int var2) {
      ItemStack var3 = null;
      Slot var4 = (Slot)this.inventorySlots.get(var2);
      if (var4 != null && var4.getHasStack()) {
         ItemStack var5 = var4.getStack();
         var3 = var5.copy();
         EntityEquipmentSlot var6 = EntityLiving.getSlotForItemStack(var3);
         if (var2 == 0) {
            if (!this.mergeItemStack(var5, 9, 45, true)) {
               return null;
            }

            var4.onSlotChange(var5, var3);
         } else if (var2 >= 1 && var2 < 5) {
            if (!this.mergeItemStack(var5, 9, 45, false)) {
               return null;
            }
         } else if (var2 >= 5 && var2 < 9) {
            if (!this.mergeItemStack(var5, 9, 45, false)) {
               return null;
            }
         } else if (var6.getSlotType() == EntityEquipmentSlot.Type.ARMOR && !((Slot)this.inventorySlots.get(8 - var6.getIndex())).getHasStack()) {
            int var7 = 8 - var6.getIndex();
            if (!this.mergeItemStack(var5, var7, var7 + 1, false)) {
               return null;
            }
         } else if (var6 == EntityEquipmentSlot.OFFHAND && !((Slot)this.inventorySlots.get(45)).getHasStack()) {
            if (!this.mergeItemStack(var5, 45, 46, false)) {
               return null;
            }
         } else if (var2 >= 9 && var2 < 36) {
            if (!this.mergeItemStack(var5, 36, 45, false)) {
               return null;
            }
         } else if (var2 >= 36 && var2 < 45) {
            if (!this.mergeItemStack(var5, 9, 36, false)) {
               return null;
            }
         } else if (!this.mergeItemStack(var5, 9, 45, false)) {
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

   public boolean canMergeSlot(ItemStack var1, Slot var2) {
      return var2.inventory != this.craftResult && super.canMergeSlot(var1, var2);
   }

   public CraftInventoryView getBukkitView() {
      if (this.bukkitEntity != null) {
         return this.bukkitEntity;
      } else {
         CraftInventoryCrafting var1 = new CraftInventoryCrafting(this.craftMatrix, this.craftResult);
         this.bukkitEntity = new CraftInventoryView(this.player.player.getBukkitEntity(), var1, this);
         return this.bukkitEntity;
      }
   }
}
