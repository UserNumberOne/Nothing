package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.HorseArmorType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerHorseInventory extends Container {
   private final IInventory horseInventory;
   private final EntityHorse theHorse;

   public ContainerHorseInventory(IInventory var1, IInventory var2, final EntityHorse var3, EntityPlayer var4) {
      this.horseInventory = var2;
      this.theHorse = var3;
      boolean var5 = true;
      var2.openInventory(var4);
      boolean var6 = true;
      this.addSlotToContainer(new Slot(var2, 0, 8, 18) {
         public boolean isItemValid(@Nullable ItemStack var1) {
            return super.isItemValid(var1) && var1.getItem() == Items.SADDLE && !this.getHasStack();
         }
      });
      this.addSlotToContainer(new Slot(var2, 1, 8, 36) {
         public boolean isItemValid(@Nullable ItemStack var1) {
            return super.isItemValid(var1) && var3.getType().isHorse() && HorseArmorType.isHorseArmor(var1.getItem());
         }

         @SideOnly(Side.CLIENT)
         public boolean canBeHovered() {
            return var3.getType().isHorse();
         }
      });
      if (var3.isChested()) {
         for(int var7 = 0; var7 < 3; ++var7) {
            for(int var8 = 0; var8 < 5; ++var8) {
               this.addSlotToContainer(new Slot(var2, 2 + var8 + var7 * 5, 80 + var8 * 18, 18 + var7 * 18));
            }
         }
      }

      for(int var9 = 0; var9 < 3; ++var9) {
         for(int var11 = 0; var11 < 9; ++var11) {
            this.addSlotToContainer(new Slot(var1, var11 + var9 * 9 + 9, 8 + var11 * 18, 102 + var9 * 18 + -18));
         }
      }

      for(int var10 = 0; var10 < 9; ++var10) {
         this.addSlotToContainer(new Slot(var1, var10, 8 + var10 * 18, 142));
      }

   }

   public boolean canInteractWith(EntityPlayer var1) {
      return this.horseInventory.isUsableByPlayer(var1) && this.theHorse.isEntityAlive() && this.theHorse.getDistanceToEntity(var1) < 8.0F;
   }

   @Nullable
   public ItemStack transferStackInSlot(EntityPlayer var1, int var2) {
      ItemStack var3 = null;
      Slot var4 = (Slot)this.inventorySlots.get(var2);
      if (var4 != null && var4.getHasStack()) {
         ItemStack var5 = var4.getStack();
         var3 = var5.copy();
         if (var2 < this.horseInventory.getSizeInventory()) {
            if (!this.mergeItemStack(var5, this.horseInventory.getSizeInventory(), this.inventorySlots.size(), true)) {
               return null;
            }
         } else if (this.getSlot(1).isItemValid(var5) && !this.getSlot(1).getHasStack()) {
            if (!this.mergeItemStack(var5, 1, 2, false)) {
               return null;
            }
         } else if (this.getSlot(0).isItemValid(var5)) {
            if (!this.mergeItemStack(var5, 0, 1, false)) {
               return null;
            }
         } else if (this.horseInventory.getSizeInventory() <= 2 || !this.mergeItemStack(var5, 2, this.horseInventory.getSizeInventory(), false)) {
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
      this.horseInventory.closeInventory(var1);
   }
}
