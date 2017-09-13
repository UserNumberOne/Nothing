package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryCrafting;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryView;

public class ContainerWorkbench extends Container {
   public InventoryCrafting craftMatrix;
   public IInventory craftResult = new InventoryCraftResult();
   private final World world;
   private final BlockPos pos;
   private CraftInventoryView bukkitEntity = null;
   private InventoryPlayer player;

   public ContainerWorkbench(InventoryPlayer playerinventory, World world, BlockPos blockposition) {
      this.craftMatrix = new InventoryCrafting(this, 3, 3, playerinventory.player);
      this.craftMatrix.resultInventory = this.craftResult;
      this.player = playerinventory;
      this.world = world;
      this.pos = blockposition;
      this.addSlotToContainer(new SlotCrafting(playerinventory.player, this.craftMatrix, this.craftResult, 0, 124, 35));

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 3; ++j) {
            this.addSlotToContainer(new Slot(this.craftMatrix, j + i * 3, 30 + j * 18, 17 + i * 18));
         }
      }

      for(int var6 = 0; var6 < 3; ++var6) {
         for(int j = 0; j < 9; ++j) {
            this.addSlotToContainer(new Slot(playerinventory, j + var6 * 9 + 9, 8 + j * 18, 84 + var6 * 18));
         }
      }

      for(int var7 = 0; var7 < 9; ++var7) {
         this.addSlotToContainer(new Slot(playerinventory, var7, 8 + var7 * 18, 142));
      }

      this.onCraftMatrixChanged(this.craftMatrix);
   }

   public void onCraftMatrixChanged(IInventory iinventory) {
      CraftingManager.getInstance().lastCraftView = this.getBukkitView();
      ItemStack craftResult = CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.world);
      this.craftResult.setInventorySlotContents(0, craftResult);
      if (super.listeners.size() >= 1) {
         if (craftResult == null || craftResult.getItem() != Items.FILLED_MAP) {
            EntityPlayerMP player = (EntityPlayerMP)super.listeners.get(0);
            player.connection.sendPacket(new SPacketSetSlot(player.openContainer.windowId, 0, craftResult));
         }
      }
   }

   public void onContainerClosed(EntityPlayer entityhuman) {
      super.onContainerClosed(entityhuman);
      if (!this.world.isRemote) {
         for(int i = 0; i < 9; ++i) {
            ItemStack itemstack = this.craftMatrix.removeStackFromSlot(i);
            if (itemstack != null) {
               entityhuman.dropItem(itemstack, false);
            }
         }
      }

   }

   public boolean canInteractWith(EntityPlayer entityhuman) {
      if (!this.checkReachable) {
         return true;
      } else {
         return this.world.getBlockState(this.pos).getBlock() != Blocks.CRAFTING_TABLE ? false : entityhuman.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
      }
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer entityhuman, int i) {
      ItemStack itemstack = null;
      Slot slot = (Slot)this.inventorySlots.get(i);
      if (slot != null && slot.getHasStack()) {
         ItemStack itemstack1 = slot.getStack();
         itemstack = itemstack1.copy();
         if (i == 0) {
            if (!this.mergeItemStack(itemstack1, 10, 46, true)) {
               return null;
            }

            slot.onSlotChange(itemstack1, itemstack);
         } else if (i >= 10 && i < 37) {
            if (!this.mergeItemStack(itemstack1, 37, 46, false)) {
               return null;
            }
         } else if (i >= 37 && i < 46) {
            if (!this.mergeItemStack(itemstack1, 10, 37, false)) {
               return null;
            }
         } else if (!this.mergeItemStack(itemstack1, 10, 46, false)) {
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
