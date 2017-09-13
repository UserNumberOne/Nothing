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

   public ContainerWorkbench(InventoryPlayer var1, World var2, BlockPos var3) {
      this.craftMatrix = new InventoryCrafting(this, 3, 3, var1.player);
      this.craftMatrix.resultInventory = this.craftResult;
      this.player = var1;
      this.world = var2;
      this.pos = var3;
      this.addSlotToContainer(new SlotCrafting(var1.player, this.craftMatrix, this.craftResult, 0, 124, 35));

      for(int var4 = 0; var4 < 3; ++var4) {
         for(int var5 = 0; var5 < 3; ++var5) {
            this.addSlotToContainer(new Slot(this.craftMatrix, var5 + var4 * 3, 30 + var5 * 18, 17 + var4 * 18));
         }
      }

      for(int var6 = 0; var6 < 3; ++var6) {
         for(int var8 = 0; var8 < 9; ++var8) {
            this.addSlotToContainer(new Slot(var1, var8 + var6 * 9 + 9, 8 + var8 * 18, 84 + var6 * 18));
         }
      }

      for(int var7 = 0; var7 < 9; ++var7) {
         this.addSlotToContainer(new Slot(var1, var7, 8 + var7 * 18, 142));
      }

      this.onCraftMatrixChanged(this.craftMatrix);
   }

   public void onCraftMatrixChanged(IInventory var1) {
      CraftingManager.getInstance().lastCraftView = this.getBukkitView();
      ItemStack var2 = CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.world);
      this.craftResult.setInventorySlotContents(0, var2);
      if (super.listeners.size() >= 1) {
         if (var2 == null || var2.getItem() != Items.FILLED_MAP) {
            EntityPlayerMP var3 = (EntityPlayerMP)super.listeners.get(0);
            var3.connection.sendPacket(new SPacketSetSlot(var3.openContainer.windowId, 0, var2));
         }
      }
   }

   public void onContainerClosed(EntityPlayer var1) {
      super.onContainerClosed(var1);
      if (!this.world.isRemote) {
         for(int var2 = 0; var2 < 9; ++var2) {
            ItemStack var3 = this.craftMatrix.removeStackFromSlot(var2);
            if (var3 != null) {
               var1.dropItem(var3, false);
            }
         }
      }

   }

   public boolean canInteractWith(EntityPlayer var1) {
      if (!this.checkReachable) {
         return true;
      } else {
         return this.world.getBlockState(this.pos).getBlock() != Blocks.CRAFTING_TABLE ? false : var1.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
      }
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer var1, int var2) {
      ItemStack var3 = null;
      Slot var4 = (Slot)this.inventorySlots.get(var2);
      if (var4 != null && var4.getHasStack()) {
         ItemStack var5 = var4.getStack();
         var3 = var5.copy();
         if (var2 == 0) {
            if (!this.mergeItemStack(var5, 10, 46, true)) {
               return null;
            }

            var4.onSlotChange(var5, var3);
         } else if (var2 >= 10 && var2 < 37) {
            if (!this.mergeItemStack(var5, 37, 46, false)) {
               return null;
            }
         } else if (var2 >= 37 && var2 < 46) {
            if (!this.mergeItemStack(var5, 10, 37, false)) {
               return null;
            }
         } else if (!this.mergeItemStack(var5, 10, 46, false)) {
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
