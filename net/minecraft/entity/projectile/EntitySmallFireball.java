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
   public EntitySmallFireball(World var1) {
      super(var1);
      this.setSize(0.3125F, 0.3125F);
   }

   public EntitySmallFireball(World var1, EntityLivingBase var2, double var3, double var5, double var7) {
      super(var1, var2, var3, var5, var7);
      this.setSize(0.3125F, 0.3125F);
   }

   public EntitySmallFireball(World var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      super(var1, var2, var4, var6, var8, var10, var12);
      this.setSize(0.3125F, 0.3125F);
   }

   public static void registerFixesSmallFireball(DataFixer var0) {
      EntityFireball.registerFixesFireball(var0, "SmallFireball");
   }

   protected void onImpact(RayTraceResult var1) {
      if (!this.world.isRemote) {
         if (var1.entityHit != null) {
            if (!var1.entityHit.isImmuneToFire()) {
               boolean var2 = var1.entityHit.attackEntityFrom(DamageSource.causeFireballDamage(this, this.shootingEntity), 5.0F);
               if (var2) {
                  this.applyEnchantments(this.shootingEntity, var1.entityHit);
                  EntityCombustByEntityEvent var3 = new EntityCombustByEntityEvent((Projectile)this.getBukkitEntity(), var1.entityHit.getBukkitEntity(), 5);
                  var1.entityHit.world.getServer().getPluginManager().callEvent(var3);
                  if (!var3.isCancelled()) {
                     var1.entityHit.setFire(var3.getDuration());
                  }
               }
            }
         } else {
            boolean var4 = true;
            if (this.shootingEntity != null && this.shootingEntity instanceof EntityLiving) {
               var4 = this.world.getGameRules().getBoolean("mobGriefing");
            }

            if (var4) {
               BlockPos var5 = var1.getBlockPos().offset(var1.sideHit);
               if (this.world.isAirBlock(var5) && this.isIncendiary && !CraftEventFactory.callBlockIgniteEvent(this.world, var5.getX(), var5.getY(), var5.getZ(), this).isCancelled()) {
                  this.world.setBlockState(var5, Blocks.FIRE.getDefaultState());
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
