package net.minecraft.entity.projectile;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntitySmallFireball extends EntityFireball {
   public EntitySmallFireball(World var1) {
      super(worldIn);
      this.setSize(0.3125F, 0.3125F);
   }

   public EntitySmallFireball(World var1, EntityLivingBase var2, double var3, double var5, double var7) {
      super(worldIn, shooter, accelX, accelY, accelZ);
      this.setSize(0.3125F, 0.3125F);
   }

   public EntitySmallFireball(World var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      super(worldIn, x, y, z, accelX, accelY, accelZ);
      this.setSize(0.3125F, 0.3125F);
   }

   public static void registerFixesSmallFireball(DataFixer var0) {
      EntityFireball.registerFixesFireball(fixer, "SmallFireball");
   }

   protected void onImpact(RayTraceResult var1) {
      if (!this.world.isRemote) {
         if (result.entityHit != null) {
            if (!result.entityHit.isImmuneToFire()) {
               boolean flag = result.entityHit.attackEntityFrom(DamageSource.causeFireballDamage(this, this.shootingEntity), 5.0F);
               if (flag) {
                  this.applyEnchantments(this.shootingEntity, result.entityHit);
                  result.entityHit.setFire(5);
               }
            }
         } else {
            boolean flag1 = true;
            if (this.shootingEntity != null && this.shootingEntity instanceof EntityLiving) {
               flag1 = this.world.getGameRules().getBoolean("mobGriefing");
            }

            if (flag1) {
               BlockPos blockpos = result.getBlockPos().offset(result.sideHit);
               if (this.world.isAirBlock(blockpos)) {
                  this.world.setBlockState(blockpos, Blocks.FIRE.getDefaultState());
               }
            }
         }

         this.setDead();
      }

   }

   public boolean canBeCollidedWith() {
      return false;
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      return false;
   }
}
