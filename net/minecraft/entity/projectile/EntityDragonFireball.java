package net.minecraft.entity.projectile;

import java.util.List;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityDragonFireball extends EntityFireball {
   public EntityDragonFireball(World var1) {
      super(worldIn);
      this.setSize(0.3125F, 0.3125F);
   }

   @SideOnly(Side.CLIENT)
   public EntityDragonFireball(World var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      super(worldIn, x, y, z, accelX, accelY, accelZ);
      this.setSize(0.3125F, 0.3125F);
   }

   public EntityDragonFireball(World var1, EntityLivingBase var2, double var3, double var5, double var7) {
      super(worldIn, shooter, accelX, accelY, accelZ);
      this.setSize(0.3125F, 0.3125F);
   }

   public static void registerFixesDragonFireball(DataFixer var0) {
      EntityFireball.registerFixesFireball(fixer, "DragonFireball");
   }

   protected void onImpact(RayTraceResult var1) {
      if (!this.world.isRemote) {
         List list = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().expand(4.0D, 2.0D, 4.0D));
         EntityAreaEffectCloud entityareaeffectcloud = new EntityAreaEffectCloud(this.world, this.posX, this.posY, this.posZ);
         entityareaeffectcloud.setOwner(this.shootingEntity);
         entityareaeffectcloud.setParticle(EnumParticleTypes.DRAGON_BREATH);
         entityareaeffectcloud.setRadius(3.0F);
         entityareaeffectcloud.setDuration(2400);
         entityareaeffectcloud.setRadiusPerTick((7.0F - entityareaeffectcloud.getRadius()) / (float)entityareaeffectcloud.getDuration());
         entityareaeffectcloud.addEffect(new PotionEffect(MobEffects.INSTANT_DAMAGE, 1, 1));
         if (!list.isEmpty()) {
            for(EntityLivingBase entitylivingbase : list) {
               double d0 = this.getDistanceSqToEntity(entitylivingbase);
               if (d0 < 16.0D) {
                  entityareaeffectcloud.setPosition(entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ);
                  break;
               }
            }
         }

         this.world.playEvent(2006, new BlockPos(this.posX, this.posY, this.posZ), 0);
         this.world.spawnEntity(entityareaeffectcloud);
         this.setDead();
      }

   }

   public boolean canBeCollidedWith() {
      return false;
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      return false;
   }

   protected EnumParticleTypes getParticleType() {
      return EnumParticleTypes.DRAGON_BREATH;
   }

   protected boolean isFireballFiery() {
      return false;
   }
}
