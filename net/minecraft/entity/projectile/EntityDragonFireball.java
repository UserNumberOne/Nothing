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
      super(var1);
      this.setSize(0.3125F, 0.3125F);
   }

   @SideOnly(Side.CLIENT)
   public EntityDragonFireball(World var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      super(var1, var2, var4, var6, var8, var10, var12);
      this.setSize(0.3125F, 0.3125F);
   }

   public EntityDragonFireball(World var1, EntityLivingBase var2, double var3, double var5, double var7) {
      super(var1, var2, var3, var5, var7);
      this.setSize(0.3125F, 0.3125F);
   }

   public static void registerFixesDragonFireball(DataFixer var0) {
      EntityFireball.registerFixesFireball(var0, "DragonFireball");
   }

   protected void onImpact(RayTraceResult var1) {
      if (!this.world.isRemote) {
         List var2 = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().expand(4.0D, 2.0D, 4.0D));
         EntityAreaEffectCloud var3 = new EntityAreaEffectCloud(this.world, this.posX, this.posY, this.posZ);
         var3.setOwner(this.shootingEntity);
         var3.setParticle(EnumParticleTypes.DRAGON_BREATH);
         var3.setRadius(3.0F);
         var3.setDuration(2400);
         var3.setRadiusPerTick((7.0F - var3.getRadius()) / (float)var3.getDuration());
         var3.addEffect(new PotionEffect(MobEffects.INSTANT_DAMAGE, 1, 1));
         if (!var2.isEmpty()) {
            for(EntityLivingBase var5 : var2) {
               double var6 = this.getDistanceSqToEntity(var5);
               if (var6 < 16.0D) {
                  var3.setPosition(var5.posX, var5.posY, var5.posZ);
                  break;
               }
            }
         }

         this.world.playEvent(2006, new BlockPos(this.posX, this.posY, this.posZ), 0);
         this.world.spawnEntity(var3);
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
