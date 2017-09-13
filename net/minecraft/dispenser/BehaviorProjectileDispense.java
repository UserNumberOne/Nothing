package net.minecraft.dispenser;

import net.minecraft.block.BlockDispenser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public abstract class BehaviorProjectileDispense extends BehaviorDefaultDispenseItem {
   public ItemStack dispenseStack(IBlockSource var1, ItemStack var2) {
      World var3 = var1.getWorld();
      IPosition var4 = BlockDispenser.getDispensePosition(var1);
      EnumFacing var5 = (EnumFacing)var1.getBlockState().getValue(BlockDispenser.FACING);
      IProjectile var6 = this.getProjectileEntity(var3, var4, var2);
      var6.setThrowableHeading((double)var5.getFrontOffsetX(), (double)((float)var5.getFrontOffsetY() + 0.1F), (double)var5.getFrontOffsetZ(), this.getProjectileVelocity(), this.getProjectileInaccuracy());
      var3.spawnEntity((Entity)var6);
      var2.splitStack(1);
      return var2;
   }

   protected void playDispenseSound(IBlockSource var1) {
      var1.getWorld().playEvent(1002, var1.getBlockPos(), 0);
   }

   protected abstract IProjectile getProjectileEntity(World var1, IPosition var2, ItemStack var3);

   protected float getProjectileInaccuracy() {
      return 6.0F;
   }

   protected float getProjectileVelocity() {
      return 1.1F;
   }
}
