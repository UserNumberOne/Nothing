package net.minecraft.dispenser;

import net.minecraft.block.BlockDispenser;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BehaviorDefaultDispenseItem implements IBehaviorDispenseItem {
   public final ItemStack dispense(IBlockSource var1, ItemStack var2) {
      ItemStack itemstack = this.dispenseStack(source, stack);
      this.playDispenseSound(source);
      this.spawnDispenseParticles(source, (EnumFacing)source.getBlockState().getValue(BlockDispenser.FACING));
      return itemstack;
   }

   protected ItemStack dispenseStack(IBlockSource var1, ItemStack var2) {
      EnumFacing enumfacing = (EnumFacing)source.getBlockState().getValue(BlockDispenser.FACING);
      IPosition iposition = BlockDispenser.getDispensePosition(source);
      ItemStack itemstack = stack.splitStack(1);
      doDispense(source.getWorld(), itemstack, 6, enumfacing, iposition);
      return stack;
   }

   public static void doDispense(World var0, ItemStack var1, int var2, EnumFacing var3, IPosition var4) {
      double d0 = position.getX();
      double d1 = position.getY();
      double d2 = position.getZ();
      if (facing.getAxis() == EnumFacing.Axis.Y) {
         d1 = d1 - 0.125D;
      } else {
         d1 = d1 - 0.15625D;
      }

      EntityItem entityitem = new EntityItem(worldIn, d0, d1, d2, stack);
      double d3 = worldIn.rand.nextDouble() * 0.1D + 0.2D;
      entityitem.motionX = (double)facing.getFrontOffsetX() * d3;
      entityitem.motionY = 0.20000000298023224D;
      entityitem.motionZ = (double)facing.getFrontOffsetZ() * d3;
      entityitem.motionX += worldIn.rand.nextGaussian() * 0.007499999832361937D * (double)speed;
      entityitem.motionY += worldIn.rand.nextGaussian() * 0.007499999832361937D * (double)speed;
      entityitem.motionZ += worldIn.rand.nextGaussian() * 0.007499999832361937D * (double)speed;
      worldIn.spawnEntity(entityitem);
   }

   protected void playDispenseSound(IBlockSource var1) {
      source.getWorld().playEvent(1000, source.getBlockPos(), 0);
   }

   protected void spawnDispenseParticles(IBlockSource var1, EnumFacing var2) {
      source.getWorld().playEvent(2000, source.getBlockPos(), this.getWorldEventDataFrom(facingIn));
   }

   private int getWorldEventDataFrom(EnumFacing var1) {
      return facingIn.getFrontOffsetX() + 1 + (facingIn.getFrontOffsetZ() + 1) * 3;
   }
}
