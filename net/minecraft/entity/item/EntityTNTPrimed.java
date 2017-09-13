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

   public EntityTNTPrimed(World world) {
      super(world);
      this.yield = 4.0F;
      this.isIncendiary = false;
      this.fuse = 80;
      this.preventEntitySpawning = true;
      this.setSize(0.98F, 0.98F);
   }

   public EntityTNTPrimed(World world, double d0, double d1, double d2, EntityLivingBase entityliving) {
      this(world);
      this.setPosition(d0, d1, d2);
      float f = (float)(Math.random() * 6.2831854820251465D);
      this.motionX = (double)(-((float)Math.sin((double)f)) * 0.02F);
      this.motionY = 0.20000000298023224D;
      this.motionZ = (double)(-((float)Math.cos((double)f)) * 0.02F);
      this.setFuse(80);
      this.prevPosX = d0;
      this.prevPosY = d1;
      this.prevPosZ = d2;
      this.tntPlacedBy = entityliving;
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
      CraftServer server = this.world.getServer();
      ExplosionPrimeEvent event = new ExplosionPrimeEvent((Explosive)CraftEntity.getEntity(server, this));
      server.getPluginManager().callEvent(event);
      if (!event.isCancelled()) {
         this.world.newExplosion(this, this.posX, this.posY + (double)(this.height / 16.0F), this.posZ, event.getRadius(), event.getFire(), true);
      }

   }

   protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setShort("Fuse", (short)this.getFuse());
   }

   protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      this.setFuse(nbttagcompound.getShort("Fuse"));
   }

   public EntityLivingBase getTntPlacedBy() {
      return this.tntPlacedBy;
   }

   public float getEyeHeight() {
      return 0.0F;
   }

   public void setFuse(int i) {
      this.dataManager.set(FUSE, Integer.valueOf(i));
      this.fuse = i;
   }

   public void notifyDataManagerChange(DataParameter datawatcherobject) {
      if (FUSE.equals(datawatcherobject)) {
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
