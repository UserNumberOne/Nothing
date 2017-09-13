package net.minecraft.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityLargeFireball extends EntityFireball {
   public int explosionPower = 1;

   public EntityLargeFireball(World var1) {
      super(var1);
   }

   @SideOnly(Side.CLIENT)
   public EntityLargeFireball(World var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      super(var1, var2, var4, var6, var8, var10, var12);
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
         this.world.newExplosion((Entity)null, this.posX, this.posY, this.posZ, (float)this.explosionPower, var2, var2);
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
         this.explosionPower = var1.getInteger("ExplosionPower");
      }

   }
}
