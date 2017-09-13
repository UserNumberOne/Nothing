package net.minecraft.entity.projectile;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.init.MobEffects;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

public class EntityWitherSkull extends EntityFireball {
   private static final DataParameter INVULNERABLE = EntityDataManager.createKey(EntityWitherSkull.class, DataSerializers.BOOLEAN);

   public EntityWitherSkull(World var1) {
      super(var1);
      this.setSize(0.3125F, 0.3125F);
   }

   public EntityWitherSkull(World var1, EntityLivingBase var2, double var3, double var5, double var7) {
      super(var1, var2, var3, var5, var7);
      this.setSize(0.3125F, 0.3125F);
   }

   public static void registerFixesWitherSkull(DataFixer var0) {
      EntityFireball.registerFixesFireball(var0, "WitherSkull");
   }

   protected float getMotionFactor() {
      return this.isInvulnerable() ? 0.73F : super.getMotionFactor();
   }

   public boolean isBurning() {
      return false;
   }

   public float getExplosionResistance(Explosion var1, World var2, BlockPos var3, IBlockState var4) {
      float var5 = super.getExplosionResistance(var1, var2, var3, var4);
      Block var6 = var4.getBlock();
      if (this.isInvulnerable() && EntityWither.canDestroyBlock(var6)) {
         var5 = Math.min(0.8F, var5);
      }

      return var5;
   }

   protected void onImpact(RayTraceResult var1) {
      if (!this.world.isRemote) {
         if (var1.entityHit != null) {
            if (this.shootingEntity != null) {
               if (var1.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.shootingEntity), 8.0F)) {
                  if (var1.entityHit.isEntityAlive()) {
                     this.applyEnchantments(this.shootingEntity, var1.entityHit);
                  } else {
                     this.shootingEntity.heal(5.0F, RegainReason.WITHER);
                  }
               }
            } else {
               var1.entityHit.attackEntityFrom(DamageSource.magic, 5.0F);
            }

            if (var1.entityHit instanceof EntityLivingBase) {
               byte var2 = 0;
               if (this.world.getDifficulty() == EnumDifficulty.NORMAL) {
                  var2 = 10;
               } else if (this.world.getDifficulty() == EnumDifficulty.HARD) {
                  var2 = 40;
               }

               if (var2 > 0) {
                  ((EntityLivingBase)var1.entityHit).addPotionEffect(new PotionEffect(MobEffects.WITHER, 20 * var2, 1));
               }
            }
         }

         ExplosionPrimeEvent var3 = new ExplosionPrimeEvent(this.getBukkitEntity(), 1.0F, false);
         this.world.getServer().getPluginManager().callEvent(var3);
         if (!var3.isCancelled()) {
            this.world.newExplosion(this, this.posX, this.posY, this.posZ, var3.getRadius(), var3.getFire(), this.world.getGameRules().getBoolean("mobGriefing"));
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

   protected void entityInit() {
      this.dataManager.register(INVULNERABLE, Boolean.valueOf(false));
   }

   public boolean isInvulnerable() {
      return ((Boolean)this.dataManager.get(INVULNERABLE)).booleanValue();
   }

   public void setInvulnerable(boolean var1) {
      this.dataManager.set(INVULNERABLE, Boolean.valueOf(var1));
   }

   protected boolean isFireballFiery() {
      return false;
   }
}
