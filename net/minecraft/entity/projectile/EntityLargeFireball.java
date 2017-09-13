package net.minecraft.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEntity;
import org.bukkit.entity.Explosive;
import org.bukkit.event.entity.ExplosionPrimeEvent;

public class EntityLargeFireball extends EntityFireball {
   public int explosionPower = 1;

   public EntityLargeFireball(World var1) {
      super(var1);
   }

   public EntityLargeFireball(World var1, EntityLivingBase var2, double var3, double var5, double var7) {
      super(var1, var2, var3, var5, var7);
   }

   protected void onImpact(RayTraceResult var1) {
      if (!this.world.isRemote) {
         if (var1.entityHit != null) {
            var1.entityHit.attackEntityFrom(DamageSource.causeFireballDamage(this, this.shootingEntity), 6.0F);
            this.applyEnchantments(this.shootingEntity, var1.entityHit);
         }

         boolean var2 = this.world.getGameRules().getBoolean("mobGriefing");
         ExplosionPrimeEvent var3 = new ExplosionPrimeEvent((Explosive)CraftEntity.getEntity(this.world.getServer(), this));
         this.world.getServer().getPluginManager().callEvent(var3);
         if (!var3.isCancelled()) {
            this.world.newExplosion(this, this.posX, this.posY, this.posZ, var3.getRadius(), var3.getFire(), var2);
         }

         this.setDead();
      }

   }

   public static void registerFixesLargeFireball(DataFixer var0) {
      EntityFireball.registerFixesFireball(var0, "Fireball");
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setInteger("ExplosionPower", this.explosionPower);
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      if (var1.hasKey("ExplosionPower", 99)) {
         this.bukkitYield = (float)(this.explosionPower = var1.getInteger("ExplosionPower"));
      }

   }
}
