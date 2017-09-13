package net.minecraft.init;

import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

class Bootstrap$7$1 extends BehaviorProjectileDispense {
   // $FF: synthetic field
   final ItemStack field_185022_b;
   // $FF: synthetic field
   final <undefinedtype> field_185023_c;

   Bootstrap$7$1(Object var1, ItemStack var2) {
      this.field_185023_c = var1;
      this.field_185022_b = var2;
   }

   protected IProjectile getProjectileEntity(World var1, IPosition var2, ItemStack var3) {
      return new EntityPotion(var1, var2.getX(), var2.getY(), var2.getZ(), this.field_185022_b.copy());
   }

   protected float getProjectileInaccuracy() {
      return super.getProjectileInaccuracy() * 0.5F;
   }

   protected float getProjectileVelocity() {
      return super.getProjectileVelocity() * 1.25F;
   }
}
