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
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class EntityThrowable extends Entity implements IProjectile {
   private int xTile;
   private int yTile;
   private int zTile;
   private Block inTile;
   protected boolean inGround;
   public int throwableShake;
   private EntityLivingBase thrower;
   private String throwerName;
   private int ticksInGround;
   private int ticksInAir;
   public Entity ignoreEntity;
   private int ignoreTime;

   public EntityThrowable(World var1) {
      super(var1);
      this.xTile = -1;
      this.yTile = -1;
      this.zTile = -1;
      this.setSize(0.25F, 0.25F);
   }

   public EntityThrowable(World var1, double var2, double var4, double var6) {
      this(var1);
      this.setPosition(var2, var4, var6);
   }

   public EntityThrowable(World var1, EntityLivingBase var2) {
      this(var1, var2.posX, var2.posY + (double)var2.getEyeHeight() - 0.10000000149011612D, var2.posZ);
      this.thrower = var2;
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

   public void setHeadingFromThrower(Entity var1, float var2, float var3, float var4, float var5, float var6) {
      float var7 = -MathHelper.sin(var3 * 0.017453292F) * MathHelper.cos(var2 * 0.017453292F);
      float var8 = -MathHelper.sin((var2 + var4) * 0.017453292F);
      float var9 = MathHelper.cos(var3 * 0.017453292F) * MathHelper.cos(var2 * 0.017453292F);
      this.setThrowableHeading((double)var7, (double)var8, (double)var9, var5, var6);
      this.motionX += var1.motionX;
      this.motionZ += var1.motionZ;
      if (!var1.onGround) {
         this.motionY += var1.motionY;
      }

   }

   public void setThrowableHeading(double var1, double var3, double var5, float var7, float var8) {
      float var9 = MathHelper.sqrt(var1 * var1 + var3 * var3 + var5 * var5);
      var1 = var1 / (double)var9;
      var3 = var3 / (double)var9;
      var5 = var5 / (double)var9;
      var1 = var1 + this.rand.nextGaussian() * 0.007499999832361937D * (double)var8;
      var3 = var3 + this.rand.nextGaussian() * 0.007499999832361937D * (double)var8;
      var5 = var5 + this.rand.nextGaussian() * 0.007499999832361937D * (double)var8;
      var1 = var1 * (double)var7;
      var3 = var3 * (double)var7;
      var5 = var5 * (double)var7;
      this.motionX = var1;
      this.motionY = var3;
      this.motionZ = var5;
      float var10 = MathHelper.sqrt(var1 * var1 + var5 * var5);
      this.rotationYaw = (float)(MathHelper.atan2(var1, var5) * 57.29577951308232D);
      this.rotationPitch = (float)(MathHelper.atan2(var3, (double)var10) * 57.29577951308232D);
      this.prevRotationYaw = this.rotationYaw;
      this.prevRotationPitch = this.rotationPitch;
      this.ticksInGround = 0;
   }

   @SideOnly(Side.CLIENT)
   public void setVelocity(double var1, double var3, double var5) {
      this.motionX = var1;
      this.motionY = var3;
      this.motionZ = var5;
      if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
         float var7 = MathHelper.sqrt(var1 * var1 + var5 * var5);
         this.rotationYaw = (float)(MathHelper.atan2(var1, var5) * 57.29577951308232D);
         this.rotationPitch = (float)(MathHelper.atan2(var3, (double)var7) * 57.29577951308232D);
         this.prevRotationYaw = this.rotationYaw;
         this.prevRotationPitch = this.rotationPitch;
      }

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

      Vec3d var1 = new Vec3d(this.posX, this.posY, this.posZ);
      Vec3d var2 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
      RayTraceResult var3 = this.world.rayTraceBlocks(var1, var2);
      var1 = new Vec3d(this.posX, this.posY, this.posZ);
      var2 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
      if (var3 != null) {
         var2 = new Vec3d(var3.hitVec.xCoord, var3.hitVec.yCoord, var3.hitVec.zCoord);
      }

      Entity var4 = null;
      List var5 = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expandXyz(1.0D));
      double var6 = 0.0D;
      boolean var8 = false;

      for(int var9 = 0; var9 < var5.size(); ++var9) {
         Entity var10 = (Entity)var5.get(var9);
         if (var10.canBeCollidedWith()) {
            if (var10 == this.ignoreEntity) {
               var8 = true;
            } else if (this.ticksExisted < 2 && this.ignoreEntity == null) {
               this.ignoreEntity = var10;
               var8 = true;
            } else {
               var8 = false;
               AxisAlignedBB var11 = var10.getEntityBoundingBox().expandXyz(0.30000001192092896D);
               RayTraceResult var12 = var11.calculateIntercept(var1, var2);
               if (var12 != null) {
                  double var13 = var1.squareDistanceTo(var12.hitVec);
                  if (var13 < var6 || var6 == 0.0D) {
                     var4 = var10;
                     var6 = var13;
                  }
               }
            }
         }
      }

      if (this.ignoreEntity != null) {
         if (var8) {
            this.ignoreTime = 2;
         } else if (this.ignoreTime-- <= 0) {
            this.ignoreEntity = null;
         }
      }

      if (var4 != null) {
         var3 = new RayTraceResult(var4);
      }

      if (var3 != null) {
         if (var3.typeOfHit == RayTraceResult.Type.BLOCK && this.world.getBlockState(var3.getBlockPos()).getBlock() == Blocks.PORTAL) {
            this.setPortal(var3.getBlockPos());
         } else if (!ForgeHooks.onThrowableImpact(this, var3)) {
            this.onImpact(var3);
         }
      }

      this.posX += this.motionX;
      this.posY += this.motionY;
      this.posZ += this.motionZ;
      float var17 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * 57.29577951308232D);

      for(this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)var17) * 57.29577951308232D); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
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
      float var18 = 0.99F;
      float var19 = this.getGravityVelocity();
      if (this.isInWater()) {
         for(int var20 = 0; var20 < 4; ++var20) {
            float var21 = 0.25F;
            this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * 0.25D, this.posY - this.motionY * 0.25D, this.posZ - this.motionZ * 0.25D, this.motionX, this.motionY, this.motionZ);
         }

         var18 = 0.8F;
      }

      this.motionX *= (double)var18;
      this.motionY *= (double)var18;
      this.motionZ *= (double)var18;
      if (!this.hasNoGravity()) {
         this.motionY -= (double)var19;
      }

      this.setPosition(this.posX, this.posY, this.posZ);
   }

   protected float getGravityVelocity() {
      return 0.03F;
   }

   protected abstract void onImpact(RayTraceResult var1);

   public static void registerFixesThrowable(DataFixer var0, String var1) {
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      var1.setInteger("xTile", this.xTile);
      var1.setInteger("yTile", this.yTile);
      var1.setInteger("zTile", this.zTile);
      ResourceLocation var2 = (ResourceLocation)Block.REGISTRY.getNameForObject(this.inTile);
      var1.setString("inTile", var2 == null ? "" : var2.toString());
      var1.setByte("shake", (byte)this.throwableShake);
      var1.setByte("inGround", (byte)(this.inGround ? 1 : 0));
      if ((this.throwerName == null || this.throwerName.isEmpty()) && this.thrower instanceof EntityPlayer) {
         this.throwerName = this.thrower.getName();
      }

      var1.setString("ownerName", this.throwerName == null ? "" : this.throwerName);
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

      this.throwableShake = var1.getByte("shake") & 255;
      this.inGround = var1.getByte("inGround") == 1;
      this.thrower = null;
      this.throwerName = var1.getString("ownerName");
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
               Entity var1 = ((WorldServer)this.world).getEntityFromUuid(UUID.fromString(this.throwerName));
               if (var1 instanceof EntityLivingBase) {
                  this.thrower = (EntityLivingBase)var1;
               }
            } catch (Throwable var2) {
               this.thrower = null;
            }
         }
      }

      return this.thrower;
   }
}
