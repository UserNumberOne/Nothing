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

   protected IBehaviorDispenseItem getBehavior(@Nullable ItemStack itemstack) {
      return this.dropBehavior;
   }

   public TileEntity createNewTileEntity(World world, int i) {
      return new TileEntityDropper();
   }

   public void dispense(World world, BlockPos blockposition) {
      BlockSourceImpl sourceblock = new BlockSourceImpl(world, blockposition);
      TileEntityDispenser tileentitydispenser = (TileEntityDispenser)sourceblock.getBlockTileEntity();
      if (tileentitydispenser != null) {
         int i = tileentitydispenser.getDispenseSlot();
         if (i < 0) {
            world.playEvent(1001, blockposition, 0);
         } else {
            ItemStack itemstack = tileentitydispenser.getStackInSlot(i);
            if (itemstack != null) {
               EnumFacing enumdirection = (EnumFacing)world.getBlockState(blockposition).getValue(FACING);
               BlockPos blockposition1 = blockposition.offset(enumdirection);
               IInventory iinventory = TileEntityHopper.getInventoryAtPosition(world, (double)blockposition1.getX(), (double)blockposition1.getY(), (double)blockposition1.getZ());
               ItemStack itemstack1;
               if (iinventory == null) {
                  itemstack1 = this.dropBehavior.dispense(sourceblock, itemstack);
                  if (itemstack1 != null && itemstack1.stackSize <= 0) {
                     itemstack1 = null;
                  }
               } else {
                  CraftItemStack oitemstack = CraftItemStack.asCraftMirror(itemstack.copy().splitStack(1));
                  Inventory destinationInventory;
                  if (iinventory instanceof InventoryLargeChest) {
                     destinationInventory = new CraftInventoryDoubleChest((InventoryLargeChest)iinventory);
                  } else {
                     destinationInventory = iinventory.getOwner().getInventory();
                  }

                  InventoryMoveItemEvent event = new InventoryMoveItemEvent(tileentitydispenser.getOwner().getInventory(), oitemstack.clone(), destinationInventory, true);
                  world.getServer().getPluginManager().callEvent(event);
                  if (event.isCancelled()) {
                     return;
                  }

                  itemstack1 = TileEntityHopper.putStackInInventoryAllSlots(iinventory, CraftItemStack.asNMSCopy(event.getItem()), enumdirection.getOpposite());
                  if (event.getItem().equals(oitemstack) && itemstack1 == null) {
                     itemstack1 = itemstack.copy();
                     if (--itemstack1.stackSize <= 0) {
                        itemstack1 = null;
                     }
                  } else {
                     itemstack1 = itemstack.copy();
                  }
               }

               tileentitydispenser.setInventorySlotContents(i, itemstack1);
            }
         }
      }

   }
}
