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
      super(worldIn);
   }

   @SideOnly(Side.CLIENT)
   public EntityLargeFireball(World var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      super(worldIn, x, y, z, accelX, accelY, accelZ);
   }

   public EntityLargeFireball(World var1, EntityLivingBase var2, double var3, double var5, double var7) {
      super(worldIn, shooter, accelX, accelY, accelZ);
   }

   protected void onImpact(RayTraceResult var1) {
      if (!this.world.isRemote) {
         if (result.entityHit != null) {
            result.entityHit.attackEntityFrom(DamageSource.causeFireballDamage(this, this.shootingEntity), 6.0F);
            this.applyEnchantments(this.shootingEntity, result.entityHit);
         }

         boolean flag = this.world.getGameRules().getBoolean("mobGriefing");
         this.world.newExplosion((Entity)null, this.posX, this.posY, this.posZ, (float)this.explosionPower, flag, flag);
         this.setDead();
      }

   }

   public static void registerFixesLargeFireball(DataFixer var0) {
      EntityFireball.registerFixesFireball(fixer, "Fireball");
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(compound);
      compound.setInteger("ExplosionPower", this.explosionPower);
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(compound);
      if (compound.hasKey("ExplosionPower", 99)) {
         this.explosionPower = compound.getInteger("ExplosionPower");
      }

   }
}
