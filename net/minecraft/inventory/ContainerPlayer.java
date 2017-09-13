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

   public ContainerPlayer(InventoryPlayer playerinventory, boolean flag, EntityPlayer entityhuman) {
      this.isLocalWorld = flag;
      this.player = entityhuman;
      this.craftResult = new InventoryCraftResult();
      this.craftMatrix = new InventoryCrafting(this, 2, 2, playerinventory.player);
      this.craftMatrix.resultInventory = this.craftResult;
      this.player = playerinventory;
      this.addSlotToContainer(new SlotCrafting(playerinventory.player, this.craftMatrix, this.craftResult, 0, 154, 28));

      for(int i = 0; i < 2; ++i) {
         for(int j = 0; j < 2; ++j) {
            this.addSlotToContainer(new Slot(this.craftMatrix, j + i * 2, 98 + j * 18, 18 + i * 18));
         }
      }

      for(int var7 = 0; var7 < 4; ++var7) {
         final EntityEquipmentSlot enumitemslot1 = VALID_EQUIPMENT_SLOTS[var7];
         this.addSlotToContainer(new Slot(playerinventory, 36 + (3 - var7), 8, 8 + var7 * 18) {
            public int getSlotStackLimit() {
               return 1;
            }

            public boolean isItemValid(@Nullable ItemStack itemstack) {
               if (itemstack == null) {
                  return false;
               } else {
                  EntityEquipmentSlot enumitemslot = EntityLiving.getSlotForItemStack(itemstack);
                  return enumitemslot == enumitemslot1;
               }
            }
         });
      }

      for(int var8 = 0; var8 < 3; ++var8) {
         for(int j = 0; j < 9; ++j) {
            this.addSlotToContainer(new Slot(playerinventory, j + (var8 + 1) * 9, 8 + j * 18, 84 + var8 * 18));
         }
      }

      for(int var9 = 0; var9 < 9; ++var9) {
         this.addSlotToContainer(new Slot(playerinventory, var9, 8 + var9 * 18, 142));
      }

      this.addSlotToContainer(new Slot(playerinventory, 40, 77, 62) {
         public boolean isItemValid(@Nullable ItemStack itemstack) {
            return super.isItemValid(itemstack);
         }
      });
   }

   public void onCraftMatrixChanged(IInventory iinventory) {
      CraftingManager.getInstance().lastCraftView = this.getBukkitView();
      ItemStack craftResult = CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.player.world);
      this.craftResult.setInventorySlotContents(0, craftResult);
      if (super.listeners.size() >= 1) {
         EntityPlayerMP player = (EntityPlayerMP)super.listeners.get(0);
         player.connection.sendPacket(new SPacketSetSlot(player.openContainer.windowId, 0, craftResult));
      }
   }

   public void onContainerClosed(EntityPlayer entityhuman) {
      super.onContainerClosed(entityhuman);

      for(int i = 0; i < 4; ++i) {
         ItemStack itemstack = this.craftMatrix.removeStackFromSlot(i);
         if (itemstack != null) {
            entityhuman.dropItem(itemstack, false);
         }
      }

      this.craftResult.setInventorySlotContents(0, (ItemStack)null);
   }

   public boolean canInteractWith(EntityPlayer entityhuman) {
      return true;
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer entityhuman, int i) {
      ItemStack itemstack = null;
      Slot slot = (Slot)this.inventorySlots.get(i);
      if (slot != null && slot.getHasStack()) {
         ItemStack itemstack1 = slot.getStack();
         itemstack = itemstack1.copy();
         EntityEquipmentSlot enumitemslot = EntityLiving.getSlotForItemStack(itemstack);
         if (i == 0) {
            if (!this.mergeItemStack(itemstack1, 9, 45, true)) {
               return null;
            }

            slot.onSlotChange(itemstack1, itemstack);
         } else if (i >= 1 && i < 5) {
            if (!this.mergeItemStack(itemstack1, 9, 45, false)) {
               return null;
            }
         } else if (i >= 5 && i < 9) {
            if (!this.mergeItemStack(itemstack1, 9, 45, false)) {
               return null;
            }
         } else if (enumitemslot.getSlotType() == EntityEquipmentSlot.Type.ARMOR && !((Slot)this.inventorySlots.get(8 - enumitemslot.getIndex())).getHasStack()) {
            int j = 8 - enumitemslot.getIndex();
            if (!this.mergeItemStack(itemstack1, j, j + 1, false)) {
               return null;
            }
         } else if (enumitemslot == EntityEquipmentSlot.OFFHAND && !((Slot)this.inventorySlots.get(45)).getHasStack()) {
            if (!this.mergeItemStack(itemstack1, 45, 46, false)) {
               return null;
            }
         } else if (i >= 9 && i < 36) {
            if (!this.mergeItemStack(itemstack1, 36, 45, false)) {
               return null;
            }
         } else if (i >= 36 && i < 45) {
            if (!this.mergeItemStack(itemstack1, 9, 36, false)) {
               return null;
            }
         } else if (!this.mergeItemStack(itemstack1, 9, 45, false)) {
            return null;
         }

         if (itemstack1.stackSize == 0) {
            slot.putStack((ItemStack)null);
         } else {
            slot.onSlotChanged();
         }

         if (itemstack1.stackSize == itemstack.stackSize) {
            return null;
         }

         slot.onPickupFromSlot(entityhuman, itemstack1);
      }

      return itemstack;
   }

   public boolean canMergeSlot(ItemStack itemstack, Slot slot) {
      return slot.inventory != this.craftResult && super.canMergeSlot(itemstack, slot);
   }

   public CraftInventoryView getBukkitView() {
      if (this.bukkitEntity != null) {
         return this.bukkitEntity;
      } else {
         CraftInventoryCrafting inventory = new CraftInventoryCrafting(this.craftMatrix, this.craftResult);
         this.bukkitEntity = new CraftInventoryView(this.player.player.getBukkitEntity(), inventory, this);
         return this.bukkitEntity;
      }
   }
}
