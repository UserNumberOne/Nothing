package net.minecraft.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityEnderEye extends Entity {
   private double targetX;
   private double targetY;
   private double targetZ;
   private int despawnTimer;
   private boolean shatterOrDrop;

   public EntityEnderEye(World var1) {
      super(var1);
      this.setSize(0.25F, 0.25F);
   }

   protected void entityInit() {
   }

   public EntityEnderEye(World var1, double var2, double var4, double var6) {
      super(var1);
      this.despawnTimer = 0;
      this.setSize(0.25F, 0.25F);
      this.setPosition(var2, var4, var6);
   }

   public void moveTowards(BlockPos var1) {
      double var2 = (double)var1.getX();
      int var4 = var1.getY();
      double var5 = (double)var1.getZ();
      double var7 = var2 - this.posX;
      double var9 = var5 - this.posZ;
      float var11 = MathHelper.sqrt(var7 * var7 + var9 * var9);
      if (var11 > 12.0F) {
         this.targetX = this.posX + var7 / (double)var11 * 12.0D;
         this.targetZ = this.posZ + var9 / (double)var11 * 12.0D;
         this.targetY = this.posY + 8.0D;
      } else {
         this.targetX = var2;
         this.targetY = (double)var4;
         this.targetZ = var5;
      }

      this.despawnTimer = 0;
      this.shatterOrDrop = this.rand.nextInt(5) > 0;
   }

   public void onUpdate() {
      this.lastTickPosX = this.posX;
      this.lastTickPosY = this.posY;
      this.lastTickPosZ = this.posZ;
      super.onUpdate();
      this.posX += this.motionX;
      this.posY += this.motionY;
      this.posZ += this.motionZ;
      float var1 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * 57.2957763671875D);

      for(this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)var1) * 57.2957763671875D); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
         ;
      }

      while(this.rotationPitch - this.prevRotationPitch >= 180.0F) {
         this.prevRotationPitch += 360.0F;
      }

      while(this.rotationYaw - this.prevRotationYaw < -180.0F) {
         this.prevRotationYaw -= 360.0F;
      }

      while(this.rotationYaw - this.prevRotationYaw >= 180.0F) {
         this.prevRotationYaw += 360.0F;
      }

      this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
      this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
      if (!this.world.isRemote) {
         double var2 = this.targetX - this.posX;
         double var4 = this.targetZ - this.posZ;
         float var6 = (float)Math.sqrt(var2 * var2 + var4 * var4);
         float var7 = (float)MathHelper.atan2(var4, var2);
         double var8 = (double)var1 + (double)(var6 - var1) * 0.0025D;
         if (var6 < 1.0F) {
            var8 *= 0.8D;
            this.motionY *= 0.8D;
         }

         this.motionX = Math.cos((double)var7) * var8;
         this.motionZ = Math.sin((double)var7) * var8;
         if (this.posY < this.targetY) {
            this.motionY += (1.0D - this.motionY) * 0.014999999664723873D;
         } else {
            this.motionY += (-1.0D - this.motionY) * 0.014999999664723873D;
         }
      }

      float var10 = 0.25F;
      if (this.isInWater()) {
         for(int var11 = 0; var11 < 4; ++var11) {
            this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * 0.25D, this.posY - this.motionY * 0.25D, this.posZ - this.motionZ * 0.25D, this.motionX, this.motionY, this.motionZ);
         }
      } else {
         this.world.spawnParticle(EnumParticleTypes.PORTAL, this.posX - this.motionX * 0.25D + this.rand.nextDouble() * 0.6D - 0.3D, this.posY - this.motionY * 0.25D - 0.5D, this.posZ - this.motionZ * 0.25D + this.rand.nextDouble() * 0.6D - 0.3D, this.motionX, this.motionY, this.motionZ);
      }

      if (!this.world.isRemote) {
         this.setPosition(this.posX, this.posY, this.posZ);
         ++this.despawnTimer;
         if (this.despawnTimer > 80 && !this.world.isRemote) {
            this.setDead();
            if (this.shatterOrDrop) {
               this.world.spawnEntity(new EntityItem(this.world, this.posX, this.posY, this.posZ, new ItemStack(Items.ENDER_EYE)));
            } else {
               this.world.playEvent(2003, new BlockPos(this), 0);
            }
         }
      }

   }

   public void writeEntityToNBT(NBTTagCompound var1) {
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
   }

   public float getBrightness(float var1) {
      return 1.0F;
   }

   public boolean canBeAttackedWithItem() {
      return false;
   }
}
