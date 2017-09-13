package net.minecraft.entity.projectile;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class EntityArrow extends Entity implements IProjectile {
   private static final Predicate ARROW_TARGETS = Predicates.and(new Predicate[]{EntitySelectors.NOT_SPECTATING, EntitySelectors.IS_ALIVE, new Predicate() {
      public boolean apply(@Nullable Entity var1) {
         return var1.canBeCollidedWith();
      }
   }});
   private static final DataParameter CRITICAL = EntityDataManager.createKey(EntityArrow.class, DataSerializers.BYTE);
   private int xTile;
   private int yTile;
   private int zTile;
   private Block inTile;
   private int inData;
   protected boolean inGround;
   protected int timeInGround;
   public EntityArrow.PickupStatus pickupStatus;
   public int arrowShake;
   public Entity shootingEntity;
   private int ticksInGround;
   private int ticksInAir;
   private double damage;
   private int knockbackStrength;

   public EntityArrow(World var1) {
      super(var1);
      this.xTile = -1;
      this.yTile = -1;
      this.zTile = -1;
      this.pickupStatus = EntityArrow.PickupStatus.DISALLOWED;
      this.damage = 2.0D;
      this.setSize(0.5F, 0.5F);
   }

   public EntityArrow(World var1, double var2, double var4, double var6) {
      this(var1);
      this.setPosition(var2, var4, var6);
   }

   public EntityArrow(World var1, EntityLivingBase var2) {
      this(var1, var2.posX, var2.posY + (double)var2.getEyeHeight() - 0.10000000149011612D, var2.posZ);
      this.shootingEntity = var2;
      if (var2 instanceof EntityPlayer) {
         this.pickupStatus = EntityArrow.PickupStatus.ALLOWED;
      }

   }

   @SideOnly(Side.CLIENT)
   public boolean isInRangeToRenderDist(double var1) {
      double var3 = this.getEntityBoundingBox().getAverageEdgeLength() * 10.0D;
      if (Double.isNaN(var3)) {
         var3 = 1.0D;
      }

      var3 = var3 * 64.0D * getRenderDistanceWeight();
      return var1 < var3 * var3;
   }

   protected void entityInit() {
      this.dataManager.register(CRITICAL, Byte.valueOf((byte)0));
   }

   public void setAim(Entity var1, float var2, float var3, float var4, float var5, float var6) {
      float var7 = -MathHelper.sin(var3 * 0.017453292F) * MathHelper.cos(var2 * 0.017453292F);
      float var8 = -MathHelper.sin(var2 * 0.017453292F);
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
   public void setPositionAndRotationDirect(double var1, double var3, double var5, float var7, float var8, int var9, boolean var10) {
      this.setPosition(var1, var3, var5);
      this.setRotation(var7, var8);
   }

   @SideOnly(Side.CLIENT)
   public void setVelocity(double var1, double var3, double var5) {
      this.motionX = var1;
      this.motionY = var3;
      this.motionZ = var5;
      if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
         float var7 = MathHelper.sqrt(var1 * var1 + var5 * var5);
         this.rotationPitch = (float)(MathHelper.atan2(var3, (double)var7) * 57.29577951308232D);
         this.rotationYaw = (float)(MathHelper.atan2(var1, var5) * 57.29577951308232D);
         this.prevRotationPitch = this.rotationPitch;
         this.prevRotationYaw = this.rotationYaw;
         this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
         this.ticksInGround = 0;
      }

   }

   public void onUpdate() {
      super.onUpdate();
      if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
         float var1 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
         this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * 57.29577951308232D);
         this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)var1) * 57.29577951308232D);
         this.prevRotationYaw = this.rotationYaw;
         this.prevRotationPitch = this.rotationPitch;
      }

      BlockPos var13 = new BlockPos(this.xTile, this.yTile, this.zTile);
      IBlockState var2 = this.world.getBlockState(var13);
      Block var3 = var2.getBlock();
      if (var2.getMaterial() != Material.AIR) {
         AxisAlignedBB var4 = var2.getCollisionBoundingBox(this.world, var13);
         if (var4 != Block.NULL_AABB && var4.offset(var13).isVecInside(new Vec3d(this.posX, this.posY, this.posZ))) {
            this.inGround = true;
         }
      }

      if (this.arrowShake > 0) {
         --this.arrowShake;
      }

      if (this.inGround) {
         int var14 = var3.getMetaFromState(var2);
         if (var3 == this.inTile && var14 == this.inData) {
            ++this.ticksInGround;
            if (this.ticksInGround >= 1200) {
               this.setDead();
            }
         } else {
            this.inGround = false;
            this.motionX *= (double)(this.rand.nextFloat() * 0.2F);
            this.motionY *= (double)(this.rand.nextFloat() * 0.2F);
            this.motionZ *= (double)(this.rand.nextFloat() * 0.2F);
            this.ticksInGround = 0;
            this.ticksInAir = 0;
         }

         ++this.timeInGround;
      } else {
         this.timeInGround = 0;
         ++this.ticksInAir;
         Vec3d var15 = new Vec3d(this.posX, this.posY, this.posZ);
         Vec3d var5 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
         RayTraceResult var6 = this.world.rayTraceBlocks(var15, var5, false, true, false);
         var15 = new Vec3d(this.posX, this.posY, this.posZ);
         var5 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
         if (var6 != null) {
            var5 = new Vec3d(var6.hitVec.xCoord, var6.hitVec.yCoord, var6.hitVec.zCoord);
         }

         Entity var7 = this.findEntityOnPath(var15, var5);
         if (var7 != null) {
            var6 = new RayTraceResult(var7);
         }

         if (var6 != null && var6.entityHit != null && var6.entityHit instanceof EntityPlayer) {
            EntityPlayer var8 = (EntityPlayer)var6.entityHit;
            if (this.shootingEntity instanceof EntityPlayer && !((EntityPlayer)this.shootingEntity).canAttackPlayer(var8)) {
               var6 = null;
            }
         }

         if (var6 != null) {
            this.onHit(var6);
         }

         if (this.getIsCritical()) {
            for(int var18 = 0; var18 < 4; ++var18) {
               this.world.spawnParticle(EnumParticleTypes.CRIT, this.posX + this.motionX * (double)var18 / 4.0D, this.posY + this.motionY * (double)var18 / 4.0D, this.posZ + this.motionZ * (double)var18 / 4.0D, -this.motionX, -this.motionY + 0.2D, -this.motionZ);
            }
         }

         this.posX += this.motionX;
         this.posY += this.motionY;
         this.posZ += this.motionZ;
         float var19 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
         this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * 57.29577951308232D);

         for(this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)var19) * 57.29577951308232D); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
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
         float var9 = 0.99F;
         float var10 = 0.05F;
         if (this.isInWater()) {
            for(int var11 = 0; var11 < 4; ++var11) {
               float var12 = 0.25F;
               this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * 0.25D, this.posY - this.motionY * 0.25D, this.posZ - this.motionZ * 0.25D, this.motionX, this.motionY, this.motionZ);
            }

            var9 = 0.6F;
         }

         if (this.isWet()) {
            this.extinguish();
         }

         this.motionX *= (double)var9;
         this.motionY *= (double)var9;
         this.motionZ *= (double)var9;
         if (!this.hasNoGravity()) {
            this.motionY -= 0.05000000074505806D;
         }

         this.setPosition(this.posX, this.posY, this.posZ);
         this.doBlockCollisions();
      }

   }

   protected void onHit(RayTraceResult var1) {
      Entity var2 = var1.entityHit;
      if (var2 != null) {
         float var3 = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
         int var4 = MathHelper.ceil((double)var3 * this.damage);
         if (this.getIsCritical()) {
            var4 += this.rand.nextInt(var4 / 2 + 2);
         }

         DamageSource var5;
         if (this.shootingEntity == null) {
            var5 = DamageSource.causeArrowDamage(this, this);
         } else {
            var5 = DamageSource.causeArrowDamage(this, this.shootingEntity);
         }

         if (this.isBurning() && !(var2 instanceof EntityEnderman)) {
            var2.setFire(5);
         }

         if (var2.attackEntityFrom(var5, (float)var4)) {
            if (var2 instanceof EntityLivingBase) {
               EntityLivingBase var6 = (EntityLivingBase)var2;
               if (!this.world.isRemote) {
                  var6.setArrowCountInEntity(var6.getArrowCountInEntity() + 1);
               }

               if (this.knockbackStrength > 0) {
                  float var7 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
                  if (var7 > 0.0F) {
                     var6.addVelocity(this.motionX * (double)this.knockbackStrength * 0.6000000238418579D / (double)var7, 0.1D, this.motionZ * (double)this.knockbackStrength * 0.6000000238418579D / (double)var7);
                  }
               }

               if (this.shootingEntity instanceof EntityLivingBase) {
                  EnchantmentHelper.applyThornEnchantments(var6, this.shootingEntity);
                  EnchantmentHelper.applyArthropodEnchantments((EntityLivingBase)this.shootingEntity, var6);
               }

               this.arrowHit(var6);
               if (this.shootingEntity != null && var6 != this.shootingEntity && var6 instanceof EntityPlayer && this.shootingEntity instanceof EntityPlayerMP) {
                  ((EntityPlayerMP)this.shootingEntity).connection.sendPacket(new SPacketChangeGameState(6, 0.0F));
               }
            }

            this.playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
            if (!(var2 instanceof EntityEnderman)) {
               this.setDead();
            }
         } else {
            this.motionX *= -0.10000000149011612D;
            this.motionY *= -0.10000000149011612D;
            this.motionZ *= -0.10000000149011612D;
            this.rotationYaw += 180.0F;
            this.prevRotationYaw += 180.0F;
            this.ticksInAir = 0;
            if (!this.world.isRemote && this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ < 0.0010000000474974513D) {
               if (this.pickupStatus == EntityArrow.PickupStatus.ALLOWED) {
                  this.entityDropItem(this.getArrowStack(), 0.1F);
               }

               this.setDead();
            }
         }
      } else {
         BlockPos var8 = var1.getBlockPos();
         this.xTile = var8.getX();
         this.yTile = var8.getY();
         this.zTile = var8.getZ();
         IBlockState var9 = this.world.getBlockState(var8);
         this.inTile = var9.getBlock();
         this.inData = this.inTile.getMetaFromState(var9);
         this.motionX = (double)((float)(var1.hitVec.xCoord - this.posX));
         this.motionY = (double)((float)(var1.hitVec.yCoord - this.posY));
         this.motionZ = (double)((float)(var1.hitVec.zCoord - this.posZ));
         float var10 = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
         this.posX -= this.motionX / (double)var10 * 0.05000000074505806D;
         this.posY -= this.motionY / (double)var10 * 0.05000000074505806D;
         this.posZ -= this.motionZ / (double)var10 * 0.05000000074505806D;
         this.playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
         this.inGround = true;
         this.arrowShake = 7;
         this.setIsCritical(false);
         if (var9.getMaterial() != Material.AIR) {
            this.inTile.onEntityCollidedWithBlock(this.world, var8, var9, this);
         }
      }

   }

   protected void arrowHit(EntityLivingBase var1) {
   }

   @Nullable
   protected Entity findEntityOnPath(Vec3d var1, Vec3d var2) {
      Entity var3 = null;
      List var4 = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expandXyz(1.0D), ARROW_TARGETS);
      double var5 = 0.0D;

      for(int var7 = 0; var7 < var4.size(); ++var7) {
         Entity var8 = (Entity)var4.get(var7);
         if (var8 != this.shootingEntity || this.ticksInAir >= 5) {
            AxisAlignedBB var9 = var8.getEntityBoundingBox().expandXyz(0.30000001192092896D);
            RayTraceResult var10 = var9.calculateIntercept(var1, var2);
            if (var10 != null) {
               double var11 = var1.squareDistanceTo(var10.hitVec);
               if (var11 < var5 || var5 == 0.0D) {
                  var3 = var8;
                  var5 = var11;
               }
            }
         }
      }

      return var3;
   }

   public static void registerFixesArrow(DataFixer var0, String var1) {
   }

   public static void registerFixesArrow(DataFixer var0) {
      registerFixesArrow(var0, "Arrow");
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      var1.setInteger("xTile", this.xTile);
      var1.setInteger("yTile", this.yTile);
      var1.setInteger("zTile", this.zTile);
      var1.setShort("life", (short)this.ticksInGround);
      ResourceLocation var2 = (ResourceLocation)Block.REGISTRY.getNameForObject(this.inTile);
      var1.setString("inTile", var2 == null ? "" : var2.toString());
      var1.setByte("inData", (byte)this.inData);
      var1.setByte("shake", (byte)this.arrowShake);
      var1.setByte("inGround", (byte)(this.inGround ? 1 : 0));
      var1.setByte("pickup", (byte)this.pickupStatus.ordinal());
      var1.setDouble("damage", this.damage);
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      this.xTile = var1.getInteger("xTile");
      this.yTile = var1.getInteger("yTile");
      this.zTile = var1.getInteger("zTile");
      this.ticksInGround = var1.getShort("life");
      if (var1.hasKey("inTile", 8)) {
         this.inTile = Block.getBlockFromName(var1.getString("inTile"));
      } else {
         this.inTile = Block.getBlockById(var1.getByte("inTile") & 255);
      }

      this.inData = var1.getByte("inData") & 255;
      this.arrowShake = var1.getByte("shake") & 255;
      this.inGround = var1.getByte("inGround") == 1;
      if (var1.hasKey("damage", 99)) {
         this.damage = var1.getDouble("damage");
      }

      if (var1.hasKey("pickup", 99)) {
         this.pickupStatus = EntityArrow.PickupStatus.getByOrdinal(var1.getByte("pickup"));
      } else if (var1.hasKey("player", 99)) {
         this.pickupStatus = var1.getBoolean("player") ? EntityArrow.PickupStatus.ALLOWED : EntityArrow.PickupStatus.DISALLOWED;
      }

   }

   public void onCollideWithPlayer(EntityPlayer var1) {
      if (!this.world.isRemote && this.inGround && this.arrowShake <= 0) {
         boolean var2 = this.pickupStatus == EntityArrow.PickupStatus.ALLOWED || this.pickupStatus == EntityArrow.PickupStatus.CREATIVE_ONLY && var1.capabilities.isCreativeMode;
         if (this.pickupStatus == EntityArrow.PickupStatus.ALLOWED && !var1.inventory.addItemStackToInventory(this.getArrowStack())) {
            var2 = false;
         }

         if (var2) {
            this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 0.2F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            var1.onItemPickup(this, 1);
            this.setDead();
         }
      }

   }

   protected abstract ItemStack getArrowStack();

   protected boolean canTriggerWalking() {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public int getBrightnessForRender(float var1) {
      return 15728880;
   }

   public void setDamage(double var1) {
      this.damage = var1;
   }

   public double getDamage() {
      return this.damage;
   }

   public void setKnockbackStrength(int var1) {
      this.knockbackStrength = var1;
   }

   public boolean canBeAttackedWithItem() {
      return false;
   }

   public float getEyeHeight() {
      return 0.0F;
   }

   public void setIsCritical(boolean var1) {
      byte var2 = ((Byte)this.dataManager.get(CRITICAL)).byteValue();
      if (var1) {
         this.dataManager.set(CRITICAL, Byte.valueOf((byte)(var2 | 1)));
      } else {
         this.dataManager.set(CRITICAL, Byte.valueOf((byte)(var2 & -2)));
      }

   }

   public boolean getIsCritical() {
      byte var1 = ((Byte)this.dataManager.get(CRITICAL)).byteValue();
      return (var1 & 1) != 0;
   }

   public static enum PickupStatus {
      DISALLOWED,
      ALLOWED,
      CREATIVE_ONLY;

      public static EntityArrow.PickupStatus getByOrdinal(int var0) {
         if (var0 < 0 || var0 > values().length) {
            var0 = 0;
         }

         return values()[var0];
      }
   }
}
