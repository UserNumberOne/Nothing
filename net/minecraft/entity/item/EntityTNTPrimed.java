package net.minecraft.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class EntityTNTPrimed extends Entity {
   private static final DataParameter FUSE = EntityDataManager.createKey(EntityTNTPrimed.class, DataSerializers.VARINT);
   private EntityLivingBase tntPlacedBy;
   private int fuse;

   public EntityTNTPrimed(World var1) {
      super(worldIn);
      this.fuse = 80;
      this.preventEntitySpawning = true;
      this.setSize(0.98F, 0.98F);
   }

   public EntityTNTPrimed(World var1, double var2, double var4, double var6, EntityLivingBase var8) {
      this(worldIn);
      this.setPosition(x, y, z);
      float f = (float)(Math.random() * 6.283185307179586D);
      this.motionX = (double)(-((float)Math.sin((double)f)) * 0.02F);
      this.motionY = 0.20000000298023224D;
      this.motionZ = (double)(-((float)Math.cos((double)f)) * 0.02F);
      this.setFuse(80);
      this.prevPosX = x;
      this.prevPosY = y;
      this.prevPosZ = z;
      this.tntPlacedBy = igniter;
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
         this.setDead();
         if (!this.world.isRemote) {
            this.explode();
         }
      } else {
         this.handleWaterMovement();
         this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D);
      }

   }

   private void explode() {
      float f = 4.0F;
      this.world.createExplosion(this, this.posX, this.posY + (double)(this.height / 16.0F), this.posZ, 4.0F, true);
   }

   protected void writeEntityToNBT(NBTTagCompound var1) {
      compound.setShort("Fuse", (short)this.getFuse());
   }

   protected void readEntityFromNBT(NBTTagCompound var1) {
      this.setFuse(compound.getShort("Fuse"));
   }

   public EntityLivingBase getTntPlacedBy() {
      return this.tntPlacedBy;
   }

   public float getEyeHeight() {
      return 0.0F;
   }

   public void setFuse(int var1) {
      this.dataManager.set(FUSE, Integer.valueOf(fuseIn));
      this.fuse = fuseIn;
   }

   public void notifyDataManagerChange(DataParameter var1) {
      if (FUSE.equals(key)) {
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
