package net.minecraft.entity.projectile;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.projectiles.ProjectileSource;

public abstract class EntityFireball extends Entity {
   private int xTile = -1;
   private int yTile = -1;
   private int zTile = -1;
   private Block inTile;
   private boolean inGround;
   public EntityLivingBase shootingEntity;
   private int ticksAlive;
   private int ticksInAir;
   public double accelerationX;
   public double accelerationY;
   public double accelerationZ;
   public float bukkitYield = 1.0F;
   public boolean isIncendiary = true;

   public EntityFireball(World world) {
      super(world);
      this.setSize(1.0F, 1.0F);
   }

   protected void entityInit() {
   }

   public EntityFireball(World world, double d0, double d1, double d2, double d3, double d4, double d5) {
      super(world);
      this.setSize(1.0F, 1.0F);
      this.setLocationAndAngles(d0, d1, d2, this.rotationYaw, this.rotationPitch);
      this.setPosition(d0, d1, d2);
      double d6 = (double)MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
      this.accelerationX = d3 / d6 * 0.1D;
      this.accelerationY = d4 / d6 * 0.1D;
      this.accelerationZ = d5 / d6 * 0.1D;
   }

   public EntityFireball(World world, EntityLivingBase entityliving, double d0, double d1, double d2) {
      super(world);
      this.shootingEntity = entityliving;
      this.projectileSource = (LivingEntity)entityliving.getBukkitEntity();
      this.setSize(1.0F, 1.0F);
      this.setLocationAndAngles(entityliving.posX, entityliving.posY, entityliving.posZ, entityliving.rotationYaw, entityliving.rotationPitch);
      this.setPosition(this.posX, this.posY, this.posZ);
      this.motionX = 0.0D;
      this.motionY = 0.0D;
      this.motionZ = 0.0D;
      this.setDirection(d0, d1, d2);
   }

   public void setDirection(double d0, double d1, double d2) {
      d0 = d0 + this.rand.nextGaussian() * 0.4D;
      d1 = d1 + this.rand.nextGaussian() * 0.4D;
      d2 = d2 + this.rand.nextGaussian() * 0.4D;
      double d3 = (double)MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
      this.accelerationX = d0 / d3 * 0.1D;
      this.accelerationY = d1 / d3 * 0.1D;
      this.accelerationZ = d2 / d3 * 0.1D;
   }

   public void onUpdate() {
      if (this.world.isRemote || (this.shootingEntity == null || !this.shootingEntity.isDead) && this.world.isBlockLoaded(new BlockPos(this))) {
         super.onUpdate();
         if (this.isFireballFiery()) {
            this.setFire(1);
         }

         if (this.inGround) {
            if (this.world.getBlockState(new BlockPos(this.xTile, this.yTile, this.zTile)).getBlock() == this.inTile) {
               ++this.ticksAlive;
               if (this.ticksAlive == 600) {
                  this.setDead();
               }

               return;
            }

            this.inGround = false;
            this.motionX *= (double)(this.rand.nextFloat() * 0.2F);
            this.motionY *= (double)(this.rand.nextFloat() * 0.2F);
            this.motionZ *= (double)(this.rand.nextFloat() * 0.2F);
            this.ticksAlive = 0;
            this.ticksInAir = 0;
         } else {
            ++this.ticksInAir;
         }

         RayTraceResult movingobjectposition = ProjectileHelper.forwardsRaycast(this, true, this.ticksInAir >= 25, this.shootingEntity);
         if (movingobjectposition != null) {
            this.onImpact(movingobjectposition);
            if (this.isDead) {
               CraftEventFactory.callProjectileHitEvent(this);
            }
         }

         this.posX += this.motionX;
         this.posY += this.motionY;
         this.posZ += this.motionZ;
         ProjectileHelper.rotateTowardsMovement(this, 0.2F);
         float f = this.getMotionFactor();
         if (this.isInWater()) {
            for(int i = 0; i < 4; ++i) {
               this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * 0.25D, this.posY - this.motionY * 0.25D, this.posZ - this.motionZ * 0.25D, this.motionX, this.motionY, this.motionZ);
            }

            f = 0.8F;
         }

         this.motionX += this.accelerationX;
         this.motionY += this.accelerationY;
         this.motionZ += this.accelerationZ;
         this.motionX *= (double)f;
         this.motionY *= (double)f;
         this.motionZ *= (double)f;
         this.world.spawnParticle(this.getParticleType(), this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D);
         this.setPosition(this.posX, this.posY, this.posZ);
      } else {
         this.setDead();
      }

   }

   protected boolean isFireballFiery() {
      return true;
   }

   protected EnumParticleTypes getParticleType() {
      return EnumParticleTypes.SMOKE_NORMAL;
   }

   protected float getMotionFactor() {
      return 0.95F;
   }

   protected abstract void onImpact(RayTraceResult var1);

   public static void registerFixesFireball(DataFixer dataconvertermanager, String s) {
   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setInteger("xTile", this.xTile);
      nbttagcompound.setInteger("yTile", this.yTile);
      nbttagcompound.setInteger("zTile", this.zTile);
      ResourceLocation minecraftkey = (ResourceLocation)Block.REGISTRY.getNameForObject(this.inTile);
      nbttagcompound.setString("inTile", minecraftkey == null ? "" : minecraftkey.toString());
      nbttagcompound.setByte("inGround", (byte)(this.inGround ? 1 : 0));
      nbttagcompound.setTag("direction", this.newDoubleNBTList(new double[]{this.motionX, this.motionY, this.motionZ}));
      nbttagcompound.setTag("power", this.newDoubleNBTList(new double[]{this.accelerationX, this.accelerationY, this.accelerationZ}));
      nbttagcompound.setInteger("life", this.ticksAlive);
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

      this.inGround = nbttagcompound.getByte("inGround") == 1;
      if (nbttagcompound.hasKey("power", 9)) {
         NBTTagList nbttaglist = nbttagcompound.getTagList("power", 6);
         if (nbttaglist.tagCount() == 3) {
            this.accelerationX = nbttaglist.getDoubleAt(0);
            this.accelerationY = nbttaglist.getDoubleAt(1);
            this.accelerationZ = nbttaglist.getDoubleAt(2);
         }
      }

      this.ticksAlive = nbttagcompound.getInteger("life");
      if (nbttagcompound.hasKey("direction", 9) && nbttagcompound.getTagList("direction", 6).tagCount() == 3) {
         NBTTagList nbttaglist = nbttagcompound.getTagList("direction", 6);
         this.motionX = nbttaglist.getDoubleAt(0);
         this.motionY = nbttaglist.getDoubleAt(1);
         this.motionZ = nbttaglist.getDoubleAt(2);
      } else {
         this.setDead();
      }

   }

   public boolean canBeCollidedWith() {
      return true;
   }

   public float getCollisionBorderSize() {
      return 1.0F;
   }

   public boolean attackEntityFrom(DamageSource damagesource, float f) {
      if (this.isEntityInvulnerable(damagesource)) {
         return false;
      } else {
         this.setBeenAttacked();
         if (damagesource.getEntity() != null) {
            if (CraftEventFactory.handleNonLivingEntityDamageEvent(this, damagesource, (double)f)) {
               return false;
            } else {
               Vec3d vec3d = damagesource.getEntity().getLookVec();
               if (vec3d != null) {
                  this.motionX = vec3d.xCoord;
                  this.motionY = vec3d.yCoord;
                  this.motionZ = vec3d.zCoord;
                  this.accelerationX = this.motionX * 0.1D;
                  this.accelerationY = this.motionY * 0.1D;
                  this.accelerationZ = this.motionZ * 0.1D;
               }

               if (damagesource.getEntity() instanceof EntityLivingBase) {
                  this.shootingEntity = (EntityLivingBase)damagesource.getEntity();
                  this.projectileSource = (ProjectileSource)this.shootingEntity.getBukkitEntity();
               }

               return true;
            }
         } else {
            return false;
         }
      }
   }

   public float getBrightness(float f) {
      return 1.0F;
   }
}
