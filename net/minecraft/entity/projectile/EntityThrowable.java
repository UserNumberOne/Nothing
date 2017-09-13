package net.minecraft.entity.projectile;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.entity.LivingEntity;

public abstract class EntityThrowable extends Entity implements IProjectile {
   private int xTile;
   private int yTile;
   private int zTile;
   private Block inTile;
   protected boolean inGround;
   public int throwableShake;
   public EntityLivingBase thrower;
   public String throwerName;
   private int ticksInGround;
   private int ticksInAir;
   public Entity ignoreEntity;
   private int ignoreTime;

   public EntityThrowable(World world) {
      super(world);
      this.xTile = -1;
      this.yTile = -1;
      this.zTile = -1;
      this.setSize(0.25F, 0.25F);
   }

   public EntityThrowable(World world, double d0, double d1, double d2) {
      this(world);
      this.setPosition(d0, d1, d2);
   }

   public EntityThrowable(World world, EntityLivingBase entityliving) {
      this(world, entityliving.posX, entityliving.posY + (double)entityliving.getEyeHeight() - 0.10000000149011612D, entityliving.posZ);
      this.thrower = entityliving;
      this.projectileSource = (LivingEntity)entityliving.getBukkitEntity();
   }

   protected void entityInit() {
   }

   public void setHeadingFromThrower(Entity entity, float f, float f1, float f2, float f3, float f4) {
      float f5 = -MathHelper.sin(f1 * 0.017453292F) * MathHelper.cos(f * 0.017453292F);
      float f6 = -MathHelper.sin((f + f2) * 0.017453292F);
      float f7 = MathHelper.cos(f1 * 0.017453292F) * MathHelper.cos(f * 0.017453292F);
      this.setThrowableHeading((double)f5, (double)f6, (double)f7, f3, f4);
      this.motionX += entity.motionX;
      this.motionZ += entity.motionZ;
      if (!entity.onGround) {
         this.motionY += entity.motionY;
      }

   }

   public void setThrowableHeading(double d0, double d1, double d2, float f, float f1) {
      float f2 = MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
      d0 = d0 / (double)f2;
      d1 = d1 / (double)f2;
      d2 = d2 / (double)f2;
      d0 = d0 + this.rand.nextGaussian() * 0.007499999832361937D * (double)f1;
      d1 = d1 + this.rand.nextGaussian() * 0.007499999832361937D * (double)f1;
      d2 = d2 + this.rand.nextGaussian() * 0.007499999832361937D * (double)f1;
      d0 = d0 * (double)f;
      d1 = d1 * (double)f;
      d2 = d2 * (double)f;
      this.motionX = d0;
      this.motionY = d1;
      this.motionZ = d2;
      float f3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
      this.rotationYaw = (float)(MathHelper.atan2(d0, d2) * 57.2957763671875D);
      this.rotationPitch = (float)(MathHelper.atan2(d1, (double)f3) * 57.2957763671875D);
      this.prevRotationYaw = this.rotationYaw;
      this.prevRotationPitch = this.rotationPitch;
      this.ticksInGround = 0;
   }

   public void onUpdate() {
      this.lastTickPosX = this.posX;
      this.lastTickPosY = this.posY;
      this.lastTickPosZ = this.posZ;
      super.onUpdate();
      if (this.throwableShake > 0) {
         --this.throwableShake;
      }

      if (this.inGround) {
         if (this.world.getBlockState(new BlockPos(this.xTile, this.yTile, this.zTile)).getBlock() == this.inTile) {
            ++this.ticksInGround;
            if (this.ticksInGround == 1200) {
               this.setDead();
            }

            return;
         }

         this.inGround = false;
         this.motionX *= (double)(this.rand.nextFloat() * 0.2F);
         this.motionY *= (double)(this.rand.nextFloat() * 0.2F);
         this.motionZ *= (double)(this.rand.nextFloat() * 0.2F);
         this.ticksInGround = 0;
         this.ticksInAir = 0;
      } else {
         ++this.ticksInAir;
      }

      Vec3d vec3d = new Vec3d(this.posX, this.posY, this.posZ);
      Vec3d vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
      RayTraceResult movingobjectposition = this.world.rayTraceBlocks(vec3d, vec3d1);
      vec3d = new Vec3d(this.posX, this.posY, this.posZ);
      vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
      if (movingobjectposition != null) {
         vec3d1 = new Vec3d(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
      }

      Entity entity = null;
      List list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expandXyz(1.0D));
      double d0 = 0.0D;
      boolean flag = false;

      for(int i = 0; i < list.size(); ++i) {
         Entity entity1 = (Entity)list.get(i);
         if (entity1.canBeCollidedWith()) {
            if (entity1 == this.ignoreEntity) {
               flag = true;
            } else if (this.ticksExisted < 2 && this.ignoreEntity == null) {
               this.ignoreEntity = entity1;
               flag = true;
            } else {
               flag = false;
               AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expandXyz(0.30000001192092896D);
               RayTraceResult movingobjectposition1 = axisalignedbb.calculateIntercept(vec3d, vec3d1);
               if (movingobjectposition1 != null) {
                  double d1 = vec3d.squareDistanceTo(movingobjectposition1.hitVec);
                  if (d1 < d0 || d0 == 0.0D) {
                     entity = entity1;
                     d0 = d1;
                  }
               }
            }
         }
      }

      if (this.ignoreEntity != null) {
         if (flag) {
            this.ignoreTime = 2;
         } else if (this.ignoreTime-- <= 0) {
            this.ignoreEntity = null;
         }
      }

      if (entity != null) {
         movingobjectposition = new RayTraceResult(entity);
      }

      if (movingobjectposition != null) {
         if (movingobjectposition.typeOfHit == RayTraceResult.Type.BLOCK && this.world.getBlockState(movingobjectposition.getBlockPos()).getBlock() == Blocks.PORTAL) {
            this.setPortal(movingobjectposition.getBlockPos());
         } else {
            this.onImpact(movingobjectposition);
            if (this.isDead) {
               CraftEventFactory.callProjectileHitEvent(this);
            }
         }
      }

      this.posX += this.motionX;
      this.posY += this.motionY;
      this.posZ += this.motionZ;
      float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * 57.2957763671875D);

      for(this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)f) * 57.2957763671875D); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
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
      float f1 = 0.99F;
      float f2 = this.getGravityVelocity();
      if (this.isInWater()) {
         for(int j = 0; j < 4; ++j) {
            this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * 0.25D, this.posY - this.motionY * 0.25D, this.posZ - this.motionZ * 0.25D, this.motionX, this.motionY, this.motionZ);
         }

         f1 = 0.8F;
      }

      this.motionX *= (double)f1;
      this.motionY *= (double)f1;
      this.motionZ *= (double)f1;
      if (!this.hasNoGravity()) {
         this.motionY -= (double)f2;
      }

      this.setPosition(this.posX, this.posY, this.posZ);
   }

   protected float getGravityVelocity() {
      return 0.03F;
   }

   protected abstract void onImpact(RayTraceResult var1);

   public static void registerFixesThrowable(DataFixer dataconvertermanager, String s) {
   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setInteger("xTile", this.xTile);
      nbttagcompound.setInteger("yTile", this.yTile);
      nbttagcompound.setInteger("zTile", this.zTile);
      ResourceLocation minecraftkey = (ResourceLocation)Block.REGISTRY.getNameForObject(this.inTile);
      nbttagcompound.setString("inTile", minecraftkey == null ? "" : minecraftkey.toString());
      nbttagcompound.setByte("shake", (byte)this.throwableShake);
      nbttagcompound.setByte("inGround", (byte)(this.inGround ? 1 : 0));
      if ((this.throwerName == null || this.throwerName.isEmpty()) && this.thrower instanceof EntityPlayer) {
         this.throwerName = this.thrower.getName();
      }

      nbttagcompound.setString("ownerName", this.throwerName == null ? "" : this.throwerName);
   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      this.xTile = nbttagcompound.getInteger("xTile");
      this.yTile = nbttagcompound.getInteger("yTile");
      this.zTile = nbttagcompound.getInteger("zTile");
      if (nbttagcompound.hasKey("inTile", 8)) {
         this.inTile = Block.getBlockFromName(nbttagcompound.getString("inTile"));
      } else {
         this.inTile = Block.getBlockById(nbttagcompound.getByte("inTile") & 255);
      }

      this.throwableShake = nbttagcompound.getByte("shake") & 255;
      this.inGround = nbttagcompound.getByte("inGround") == 1;
      this.thrower = null;
      this.throwerName = nbttagcompound.getString("ownerName");
      if (this.throwerName != null && this.throwerName.isEmpty()) {
         this.throwerName = null;
      }

      this.thrower = this.getThrower();
   }

   @Nullable
   public EntityLivingBase getThrower() {
      if (this.thrower == null && this.throwerName != null && !this.throwerName.isEmpty()) {
         this.thrower = this.world.getPlayerEntityByName(this.throwerName);
         if (this.thrower == null && this.world instanceof WorldServer) {
            try {
               Entity entity = ((WorldServer)this.world).getEntityFromUuid(UUID.fromString(this.throwerName));
               if (entity instanceof EntityLivingBase) {
                  this.thrower = (EntityLivingBase)entity;
               }
            } catch (Throwable var2) {
               this.thrower = null;
            }
         }
      }

      return this.thrower;
   }
}
