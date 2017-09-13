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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

   public EntityFireball(World var1) {
      super(var1);
      this.setSize(1.0F, 1.0F);
   }

   protected void entityInit() {
   }

   @SideOnly(Side.CLIENT)
   public boolean isInRangeToRenderDist(double var1) {
      double var3 = this.getEntityBoundingBox().getAverageEdgeLength() * 4.0D;
      if (Double.isNaN(var3)) {
         var3 = 4.0D;
      }

      var3 = var3 * 64.0D;
      return var1 < var3 * var3;
   }

   public EntityFireball(World var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      super(var1);
      this.setSize(1.0F, 1.0F);
      this.setLocationAndAngles(var2, var4, var6, this.rotationYaw, this.rotationPitch);
      this.setPosition(var2, var4, var6);
      double var14 = (double)MathHelper.sqrt(var8 * var8 + var10 * var10 + var12 * var12);
      this.accelerationX = var8 / var14 * 0.1D;
      this.accelerationY = var10 / var14 * 0.1D;
      this.accelerationZ = var12 / var14 * 0.1D;
   }

   public EntityFireball(World var1, EntityLivingBase var2, double var3, double var5, double var7) {
      super(var1);
      this.shootingEntity = var2;
      this.setSize(1.0F, 1.0F);
      this.setLocationAndAngles(var2.posX, var2.posY, var2.posZ, var2.rotationYaw, var2.rotationPitch);
      this.setPosition(this.posX, this.posY, this.posZ);
      this.motionX = 0.0D;
      this.motionY = 0.0D;
      this.motionZ = 0.0D;
      var3 = var3 + this.rand.nextGaussian() * 0.4D;
      var5 = var5 + this.rand.nextGaussian() * 0.4D;
      var7 = var7 + this.rand.nextGaussian() * 0.4D;
      double var9 = (double)MathHelper.sqrt(var3 * var3 + var5 * var5 + var7 * var7);
      this.accelerationX = var3 / var9 * 0.1D;
      this.accelerationY = var5 / var9 * 0.1D;
      this.accelerationZ = var7 / var9 * 0.1D;
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

         RayTraceResult var1 = ProjectileHelper.forwardsRaycast(this, true, this.ticksInAir >= 25, this.shootingEntity);
         if (var1 != null) {
            this.onImpact(var1);
         }

         this.posX += this.motionX;
         this.posY += this.motionY;
         this.posZ += this.motionZ;
         ProjectileHelper.rotateTowardsMovement(this, 0.2F);
         float var2 = this.getMotionFactor();
         if (this.isInWater()) {
            for(int var3 = 0; var3 < 4; ++var3) {
               float var4 = 0.25F;
               this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * 0.25D, this.posY - this.motionY * 0.25D, this.posZ - this.motionZ * 0.25D, this.motionX, this.motionY, this.motionZ);
            }

            var2 = 0.8F;
         }

         this.motionX += this.accelerationX;
         this.motionY += this.accelerationY;
         this.motionZ += this.accelerationZ;
         this.motionX *= (double)var2;
         this.motionY *= (double)var2;
         this.motionZ *= (double)var2;
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

   public static void registerFixesFireball(DataFixer var0, String var1) {
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      var1.setInteger("xTile", this.xTile);
      var1.setInteger("yTile", this.yTile);
      var1.setInteger("zTile", this.zTile);
      ResourceLocation var2 = (ResourceLocation)Block.REGISTRY.getNameForObject(this.inTile);
      var1.setString("inTile", var2 == null ? "" : var2.toString());
      var1.setByte("inGround", (byte)(this.inGround ? 1 : 0));
      var1.setTag("direction", this.newDoubleNBTList(new double[]{this.motionX, this.motionY, this.motionZ}));
      var1.setTag("power", this.newDoubleNBTList(new double[]{this.accelerationX, this.accelerationY, this.accelerationZ}));
      var1.setInteger("life", this.ticksAlive);
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      this.xTile = var1.getInteger("xTile");
      this.yTile = var1.getInteger("yTile");
      this.zTile = var1.getInteger("zTile");
      if (var1.hasKey("inTile", 8)) {
         this.inTile = Block.getBlockFromName(var1.getString("inTile"));
      } else {
         this.inTile = Block.getBlockById(var1.getByte("inTile") & 255);
      }

      this.inGround = var1.getByte("inGround") == 1;
      if (var1.hasKey("power", 9)) {
         NBTTagList var2 = var1.getTagList("power", 6);
         if (var2.tagCount() == 3) {
            this.accelerationX = var2.getDoubleAt(0);
            this.accelerationY = var2.getDoubleAt(1);
            this.accelerationZ = var2.getDoubleAt(2);
         }
      }

      this.ticksAlive = var1.getInteger("life");
      if (var1.hasKey("direction", 9) && var1.getTagList("direction", 6).tagCount() == 3) {
         NBTTagList var3 = var1.getTagList("direction", 6);
         this.motionX = var3.getDoubleAt(0);
         this.motionY = var3.getDoubleAt(1);
         this.motionZ = var3.getDoubleAt(2);
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

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (this.isEntityInvulnerable(var1)) {
         return false;
      } else {
         this.setBeenAttacked();
         if (var1.getEntity() != null) {
            Vec3d var3 = var1.getEntity().getLookVec();
            if (var3 != null) {
               this.motionX = var3.xCoord;
               this.motionY = var3.yCoord;
               this.motionZ = var3.zCoord;
               this.accelerationX = this.motionX * 0.1D;
               this.accelerationY = this.motionY * 0.1D;
               this.accelerationZ = this.motionZ * 0.1D;
            }

            if (var1.getEntity() instanceof EntityLivingBase) {
               this.shootingEntity = (EntityLivingBase)var1.getEntity();
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public float getBrightness(float var1) {
      return 1.0F;
   }

   @SideOnly(Side.CLIENT)
   public int getBrightnessForRender(float var1) {
      return 15728880;
   }
}
