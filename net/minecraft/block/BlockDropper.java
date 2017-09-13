package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryDoubleChest;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;

public class BlockDropper extends BlockDispenser {
   private final IBehaviorDispenseItem dropBehavior = new BehaviorDefaultDispenseItem();

   protected IBehaviorDispenseItem getBehavior(@Nullable ItemStack var1) {
      return this.dropBehavior;
   }

   public TileEntity createNewTileEntity(World var1, int var2) {
      return new TileEntityDropper();
   }

   public void dispense(World var1, BlockPos var2) {
      BlockSourceImpl var3 = new BlockSourceImpl(var1, var2);
      TileEntityDispenser var4 = (TileEntityDispenser)var3.getBlockTileEntity();
      if (var4 != null) {
         int var5 = var4.getDispenseSlot();
         if (var5 < 0) {
            var1.playEvent(1001, var2, 0);
         } else {
            ItemStack var6 = var4.getStackInSlot(var5);
            if (var6 != null) {
               EnumFacing var7 = (EnumFacing)var1.getBlockState(var2).getValue(FACING);
               BlockPos var8 = var2.offset(var7);
               IInventory var9 = TileEntityHopper.getInventoryAtPosition(var1, (double)var8.getX(), (double)var8.getY(), (double)var8.getZ());
               ItemStack var14;
               if (var9 == null) {
                  var14 = this.dropBehavior.dispense(var3, var6);
                  if (var14 != null && var14.stackSize <= 0) {
                     var14 = null;
                  }
               } else {
                  CraftItemStack var11 = CraftItemStack.asCraftMirror(var6.copy().splitStack(1));
                  Object var12;
                  if (var9 instanceof InventoryLargeChest) {
                     var12 = new CraftInventoryDoubleChest((InventoryLargeChest)var9);
                  } else {
                     var12 = var9.getOwner().getInventory();
                  }

                  InventoryMoveItemEvent var13 = new InventoryMoveItemEvent(var4.getOwner().getInventory(), var11.clone(), (Inventory)var12, true);
                  var1.getServer().getPluginManager().callEvent(var13);
                  if (var13.isCancelled()) {
                     return;
                  }

                  var14 = TileEntityHopper.putStackInInventoryAllSlots(var9, CraftItemStack.asNMSCopy(var13.getItem()), var7.getOpposite());
                  if (var13.getItem().equals(var11) && var14 == null) {
                     var14 = var6.copy();
                     if (--var14.stackSize <= 0) {
                        var14 = null;
                     }
                  } else {
                     var14 = var6.copy();
                  }
               }

               var4.setInventorySlotContents(var5, var14);
            }
         }
      }

   }
}
