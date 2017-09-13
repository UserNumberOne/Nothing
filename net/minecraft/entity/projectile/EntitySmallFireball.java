package net.minecraft.entity.projectile;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityCombustByEntityEvent;

public class EntitySmallFireball extends EntityFireball {
   public EntitySmallFireball(World world) {
      super(world);
      this.setSize(0.3125F, 0.3125F);
   }

   public EntitySmallFireball(World world, EntityLivingBase entityliving, double d0, double d1, double d2) {
      super(world, entityliving, d0, d1, d2);
      this.setSize(0.3125F, 0.3125F);
   }

   public EntitySmallFireball(World world, double d0, double d1, double d2, double d3, double d4, double d5) {
      super(world, d0, d1, d2, d3, d4, d5);
      this.setSize(0.3125F, 0.3125F);
   }

   public static void registerFixesSmallFireball(DataFixer dataconvertermanager) {
      EntityFireball.registerFixesFireball(dataconvertermanager, "SmallFireball");
   }

   protected void onImpact(RayTraceResult movingobjectposition) {
      if (!this.world.isRemote) {
         if (movingobjectposition.entityHit != null) {
            if (!movingobjectposition.entityHit.isImmuneToFire()) {
               boolean flag = movingobjectposition.entityHit.attackEntityFrom(DamageSource.causeFireballDamage(this, this.shootingEntity), 5.0F);
               if (flag) {
                  this.applyEnchantments(this.shootingEntity, movingobjectposition.entityHit);
                  EntityCombustByEntityEvent event = new EntityCombustByEntityEvent((Projectile)this.getBukkitEntity(), movingobjectposition.entityHit.getBukkitEntity(), 5);
                  movingobjectposition.entityHit.world.getServer().getPluginManager().callEvent(event);
                  if (!event.isCancelled()) {
                     movingobjectposition.entityHit.setFire(event.getDuration());
                  }
               }
            }
         } else {
            boolean flag = true;
            if (this.shootingEntity != null && this.shootingEntity instanceof EntityLiving) {
               flag = this.world.getGameRules().getBoolean("mobGriefing");
            }

            if (flag) {
               BlockPos blockposition = movingobjectposition.getBlockPos().offset(movingobjectposition.sideHit);
               if (this.world.isAirBlock(blockposition) && this.isIncendiary && !CraftEventFactory.callBlockIgniteEvent(this.world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), this).isCancelled()) {
                  this.world.setBlockState(blockposition, Blocks.FIRE.getDefaultState());
               }
            }
         }

         this.setDead();
      }

   }

   public boolean canBeCollidedWith() {
      return false;
   }

   public boolean attackEntityFrom(DamageSource damagesource, float f) {
      return false;
   }
}
