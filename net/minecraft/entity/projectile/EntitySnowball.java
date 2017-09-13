package net.minecraft.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntitySnowball extends EntityThrowable {
   public EntitySnowball(World var1) {
      super(worldIn);
   }

   public EntitySnowball(World var1, EntityLivingBase var2) {
      super(worldIn, throwerIn);
   }

   public EntitySnowball(World var1, double var2, double var4, double var6) {
      super(worldIn, x, y, z);
   }

   public static void registerFixesSnowball(DataFixer var0) {
      EntityThrowable.registerFixesThrowable(fixer, "Snowball");
   }

   protected void onImpact(RayTraceResult var1) {
      if (result.entityHit != null) {
         int i = 0;
         if (result.entityHit instanceof EntityBlaze) {
            i = 3;
         }

         result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), (float)i);
      }

      for(int j = 0; j < 8; ++j) {
         this.world.spawnParticle(EnumParticleTypes.SNOWBALL, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
      }

      if (!this.world.isRemote) {
         this.setDead();
      }

   }
}
