package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.VanillaInventoryCodeHooks;

public class BlockDropper extends BlockDispenser {
   private final IBehaviorDispenseItem dropBehavior = new BehaviorDefaultDispenseItem();

   protected IBehaviorDispenseItem getBehavior(@Nullable ItemStack var1) {
      return this.dropBehavior;
   }

   public TileEntity createNewTileEntity(World var1, int var2) {
      return new TileEntityDropper();
   }

   protected void dispense(World var1, BlockPos var2) {
      BlockSourceImpl blocksourceimpl = new BlockSourceImpl(worldIn, pos);
      TileEntityDispenser tileentitydispenser = (TileEntityDispenser)blocksourceimpl.getBlockTileEntity();
      if (tileentitydispenser != null) {
         int i = tileentitydispenser.getDispenseSlot();
         if (i < 0) {
            worldIn.playEvent(1001, pos, 0);
         } else {
            ItemStack itemstack = tileentitydispenser.getStackInSlot(i);
            if (itemstack != null && VanillaInventoryCodeHooks.dropperInsertHook(worldIn, pos, tileentitydispenser, i, itemstack)) {
               EnumFacing enumfacing = (EnumFacing)worldIn.getBlockState(pos).getValue(FACING);
               BlockPos blockpos = pos.offset(enumfacing);
               IInventory iinventory = TileEntityHopper.getInventoryAtPosition(worldIn, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
               ItemStack itemstack1;
               if (iinventory == null) {
                  itemstack1 = this.dropBehavior.dispense(blocksourceimpl, itemstack);
                  if (itemstack1 != null && itemstack1.stackSize <= 0) {
                     itemstack1 = null;
                  }
               } else {
                  itemstack1 = TileEntityHopper.putStackInInventoryAllSlots(iinventory, itemstack.copy().splitStack(1), enumfacing.getOpposite());
                  if (itemstack1 == null) {
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
