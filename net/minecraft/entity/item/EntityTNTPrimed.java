package net.minecraft.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEntity;
import org.bukkit.entity.Explosive;
import org.bukkit.event.entity.ExplosionPrimeEvent;

public class EntityTNTPrimed extends Entity {
   private static final DataParameter FUSE = EntityDataManager.createKey(EntityTNTPrimed.class, DataSerializers.VARINT);
   private EntityLivingBase tntPlacedBy;
   private int fuse;
   public float yield;
   public boolean isIncendiary;

   public EntityTNTPrimed(World var1) {
      super(var1);
      this.yield = 4.0F;
      this.isIncendiary = false;
      this.fuse = 80;
      this.preventEntitySpawning = true;
      this.setSize(0.98F, 0.98F);
   }

   public EntityTNTPrimed(World var1, double var2, double var4, double var6, EntityLivingBase var8) {
      this(var1);
      this.setPosition(var2, var4, var6);
      float var9 = (float)(Math.random() * 6.2831854820251465D);
      this.motionX = (double)(-((float)Math.sin((double)var9)) * 0.02F);
      this.motionY = 0.20000000298023224D;
      this.motionZ = (double)(-((float)Math.cos((double)var9)) * 0.02F);
      this.setFuse(80);
      this.prevPosX = var2;
      this.prevPosY = var4;
      this.prevPosZ = var6;
      this.tntPlacedBy = var8;
   }

   protected void entityInit() {
      this.dataManager.register(FUSE, Integer.valueOf(80));
   }

   protected boolean canTriggerWalking() {
      return false;
   }

   public boolean canBeCollidedWith() {
      return !this.isDead;
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      if (!this.hasNoGravity()) {
         this.motionY -= 0.03999999910593033D;
      }

      this.move(this.motionX, this.motionY, this.motionZ);
      this.motionX *= 0.9800000190734863D;
      this.motionY *= 0.9800000190734863D;
      this.motionZ *= 0.9800000190734863D;
      if (this.onGround) {
         this.motionX *= 0.699999988079071D;
         this.motionZ *= 0.699999988079071D;
         this.motionY *= -0.5D;
      }

      --this.fuse;
      if (this.fuse <= 0) {
         if (!this.world.isRemote) {
            this.explode();
         }

         this.setDead();
      } else {
         this.handleWaterMovement();
         this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D);
      }

   }

   private void explode() {
      CraftServer var1 = this.world.getServer();
      ExplosionPrimeEvent var2 = new ExplosionPrimeEvent((Explosive)CraftEntity.getEntity(var1, this));
      var1.getPluginManager().callEvent(var2);
      if (!var2.isCancelled()) {
         this.world.newExplosion(this, this.posX, this.posY + (double)(this.height / 16.0F), this.posZ, var2.getRadius(), var2.getFire(), true);
      }

   }

   protected void writeEntityToNBT(NBTTagCompound var1) {
      var1.setShort("Fuse", (short)this.getFuse());
   }

   protected void readEntityFromNBT(NBTTagCompound var1) {
      this.setFuse(var1.getShort("Fuse"));
   }

   public EntityLivingBase getTntPlacedBy() {
      return this.tntPlacedBy;
   }

   public float getEyeHeight() {
      return 0.0F;
   }

   public void setFuse(int var1) {
      this.dataManager.set(FUSE, Integer.valueOf(var1));
      this.fuse = var1;
   }

   public void notifyDataManagerChange(DataParameter var1) {
      if (FUSE.equals(var1)) {
         this.fuse = this.getFuseDataManager();
      }

   }

   public int getFuseDataManager() {
      return ((Integer)this.dataManager.get(FUSE)).intValue();
   }

   public int getFuse() {
      return this.fuse;
   }
}
