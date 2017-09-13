package net.minecraft.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityEgg extends EntityThrowable {
   public EntityEgg(World var1) {
      super(worldIn);
   }

   public EntityEgg(World var1, EntityLivingBase var2) {
      super(worldIn, throwerIn);
   }

   public EntityEgg(World var1, double var2, double var4, double var6) {
      super(worldIn, x, y, z);
   }

   public static void registerFixesEgg(DataFixer var0) {
      EntityThrowable.registerFixesThrowable(fixer, "ThrownEgg");
   }

   protected void onImpact(RayTraceResult var1) {
      if (result.entityHit != null) {
         result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), 0.0F);
      }

      if (!this.world.isRemote && this.rand.nextInt(8) == 0) {
         int i = 1;
         if (this.rand.nextInt(32) == 0) {
            i = 4;
         }

         for(int j = 0; j < i; ++j) {
            EntityChicken entitychicken = new EntityChicken(this.world);
            entitychicken.setGrowingAge(-24000);
            entitychicken.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
            this.world.spawnEntity(entitychicken);
         }
      }

      double d0 = 0.08D;

      for(int k = 0; k < 8; ++k) {
         this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, this.posX, this.posY, this.posZ, ((double)this.rand.nextFloat() - 0.5D) * 0.08D, ((double)this.rand.nextFloat() - 0.5D) * 0.08D, ((double)this.rand.nextFloat() - 0.5D) * 0.08D, Item.getIdFromItem(Items.EGG));
      }

      if (!this.world.isRemote) {
         this.setDead();
      }

   }
}
