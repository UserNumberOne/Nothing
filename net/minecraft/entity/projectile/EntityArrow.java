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
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
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
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;

public abstract class EntityArrow extends Entity implements IProjectile {
   private static final Predicate ARROW_TARGETS = Predicates.and(new Predicate[]{EntitySelectors.NOT_SPECTATING, EntitySelectors.IS_ALIVE, new Predicate() {
      public boolean apply(@Nullable Entity entity) {
         return entity.canBeCollidedWith();
      }

      public boolean apply(Object object) {
         return this.apply((Entity)object);
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
   public int knockbackStrength;

   public EntityArrow(World world) {
      super(world);
      this.xTile = -1;
      this.yTile = -1;
      this.zTile = -1;
      this.pickupStatus = EntityArrow.PickupStatus.DISALLOWED;
      this.damage = 2.0D;
      this.setSize(0.5F, 0.5F);
   }

   public EntityArrow(World world, double d0, double d1, double d2) {
      this(world);
      this.setPosition(d0, d1, d2);
   }

   public EntityArrow(World world, EntityLivingBase entityliving) {
      this(world, entityliving.posX, entityliving.posY + (double)entityliving.getEyeHeight() - 0.10000000149011612D, entityliving.posZ);
      this.shootingEntity = entityliving;
      this.projectileSource = (LivingEntity)entityliving.getBukkitEntity();
      if (entityliving instanceof EntityPlayer) {
         this.pickupStatus = EntityArrow.PickupStatus.ALLOWED;
      }

   }

   protected void entityInit() {
      this.dataManager.register(CRITICAL, Byte.valueOf((byte)0));
   }

   public void setAim(Entity entity, float f, float f1, float f2, float f3, float f4) {
      float f5 = -MathHelper.sin(f1 * 0.017453292F) * MathHelper.cos(f * 0.017453292F);
      float f6 = -MathHelper.sin(f * 0.017453292F);
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
      super.onUpdate();
      if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
         float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
         this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * 57.2957763671875D);
         this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)f) * 57.2957763671875D);
         this.prevRotationYaw = this.rotationYaw;
         this.prevRotationPitch = this.rotationPitch;
      }

      BlockPos blockposition = new BlockPos(this.xTile, this.yTile, this.zTile);
      IBlockState iblockdata = this.world.getBlockState(blockposition);
      Block block = iblockdata.getBlock();
      if (iblockdata.getMaterial() != Material.AIR) {
         AxisAlignedBB axisalignedbb = iblockdata.getCollisionBoundingBox(this.world, blockposition);
         if (axisalignedbb != Block.NULL_AABB && axisalignedbb.offset(blockposition).isVecInside(new Vec3d(this.posX, this.posY, this.posZ))) {
            this.inGround = true;
         }
      }

      if (this.arrowShake > 0) {
         --this.arrowShake;
      }

      if (this.inGround) {
         int i = block.getMetaFromState(iblockdata);
         if (block == this.inTile && i == this.inData) {
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
         Vec3d vec3d = new Vec3d(this.posX, this.posY, this.posZ);
         Vec3d vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
         RayTraceResult movingobjectposition = this.world.rayTraceBlocks(vec3d, vec3d1, false, true, false);
         vec3d = new Vec3d(this.posX, this.posY, this.posZ);
         vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
         if (movingobjectposition != null) {
            vec3d1 = new Vec3d(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
         }

         Entity entity = this.findEntityOnPath(vec3d, vec3d1);
         if (entity != null) {
            movingobjectposition = new RayTraceResult(entity);
         }

         if (movingobjectposition != null && movingobjectposition.entityHit != null && movingobjectposition.entityHit instanceof EntityPlayer) {
            EntityPlayer entityhuman = (EntityPlayer)movingobjectposition.entityHit;
            if (this.shootingEntity instanceof EntityPlayer && !((EntityPlayer)this.shootingEntity).canAttackPlayer(entityhuman)) {
               movingobjectposition = null;
            }
         }

         if (movingobjectposition != null) {
            this.onHit(movingobjectposition);
         }

         if (this.getIsCritical()) {
            for(int j = 0; j < 4; ++j) {
               this.world.spawnParticle(EnumParticleTypes.CRIT, this.posX + this.motionX * (double)j / 4.0D, this.posY + this.motionY * (double)j / 4.0D, this.posZ + this.motionZ * (double)j / 4.0D, -this.motionX, -this.motionY + 0.2D, -this.motionZ);
            }
         }

         this.posX += this.motionX;
         this.posY += this.motionY;
         this.posZ += this.motionZ;
         float f1 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
         this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * 57.2957763671875D);

         for(this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)f1) * 57.2957763671875D); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
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
         float f2 = 0.99F;
         if (this.isInWater()) {
            for(int k = 0; k < 4; ++k) {
               this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * 0.25D, this.posY - this.motionY * 0.25D, this.posZ - this.motionZ * 0.25D, this.motionX, this.motionY, this.motionZ);
            }

            f2 = 0.6F;
         }

         if (this.isWet()) {
            this.extinguish();
         }

         this.motionX *= (double)f2;
         this.motionY *= (double)f2;
         this.motionZ *= (double)f2;
         if (!this.hasNoGravity()) {
            this.motionY -= 0.05000000074505806D;
         }

         this.setPosition(this.posX, this.posY, this.posZ);
         this.doBlockCollisions();
      }

   }

   protected void onHit(RayTraceResult movingobjectposition) {
      Entity entity = movingobjectposition.entityHit;
      CraftEventFactory.callProjectileHitEvent(this);
      if (entity != null) {
         float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
         int i = MathHelper.ceil((double)f * this.damage);
         if (this.getIsCritical()) {
            i += this.rand.nextInt(i / 2 + 2);
         }

         DamageSource damagesource;
         if (this.shootingEntity == null) {
            damagesource = DamageSource.causeArrowDamage(this, this);
         } else {
            damagesource = DamageSource.causeArrowDamage(this, this.shootingEntity);
         }

         if (this.isBurning() && !(entity instanceof EntityEnderman)) {
            EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(this.getBukkitEntity(), entity.getBukkitEntity(), 5);
            Bukkit.getPluginManager().callEvent(combustEvent);
            if (!combustEvent.isCancelled()) {
               entity.setFire(combustEvent.getDuration());
            }
         }

         if (entity.attackEntityFrom(damagesource, (float)i)) {
            if (entity instanceof EntityLivingBase) {
               EntityLivingBase entityliving = (EntityLivingBase)entity;
               if (!this.world.isRemote) {
                  entityliving.setArrowCountInEntity(entityliving.getArrowCountInEntity() + 1);
               }

               if (this.knockbackStrength > 0) {
                  float f1 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
                  if (f1 > 0.0F) {
                     entityliving.addVelocity(this.motionX * (double)this.knockbackStrength * 0.6000000238418579D / (double)f1, 0.1D, this.motionZ * (double)this.knockbackStrength * 0.6000000238418579D / (double)f1);
                  }
               }

               if (this.shootingEntity instanceof EntityLivingBase) {
                  EnchantmentHelper.applyThornEnchantments(entityliving, this.shootingEntity);
                  EnchantmentHelper.applyArthropodEnchantments((EntityLivingBase)this.shootingEntity, entityliving);
               }

               this.arrowHit(entityliving);
               if (this.shootingEntity != null && entityliving != this.shootingEntity && entityliving instanceof EntityPlayer && this.shootingEntity instanceof EntityPlayerMP) {
                  ((EntityPlayerMP)this.shootingEntity).connection.sendPacket(new SPacketChangeGameState(6, 0.0F));
               }
            }

            this.playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
            if (!(entity instanceof EntityEnderman)) {
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
         BlockPos blockposition = movingobjectposition.getBlockPos();
         this.xTile = blockposition.getX();
         this.yTile = blockposition.getY();
         this.zTile = blockposition.getZ();
         IBlockState iblockdata = this.world.getBlockState(blockposition);
         this.inTile = iblockdata.getBlock();
         this.inData = this.inTile.getMetaFromState(iblockdata);
         this.motionX = (double)((float)(movingobjectposition.hitVec.xCoord - this.posX));
         this.motionY = (double)((float)(movingobjectposition.hitVec.yCoord - this.posY));
         this.motionZ = (double)((float)(movingobjectposition.hitVec.zCoord - this.posZ));
         float f2 = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
         this.posX -= this.motionX / (double)f2 * 0.05000000074505806D;
         this.posY -= this.motionY / (double)f2 * 0.05000000074505806D;
         this.posZ -= this.motionZ / (double)f2 * 0.05000000074505806D;
         this.playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
         this.inGround = true;
         this.arrowShake = 7;
         this.setIsCritical(false);
         if (iblockdata.getMaterial() != Material.AIR) {
            this.inTile.onEntityCollidedWithBlock(this.world, blockposition, iblockdata, this);
         }
      }

   }

   protected void arrowHit(EntityLivingBase entityliving) {
   }

   @Nullable
   protected Entity findEntityOnPath(Vec3d vec3d, Vec3d vec3d1) {
      Entity entity = null;
      List list = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expandXyz(1.0D), ARROW_TARGETS);
      double d0 = 0.0D;

      for(int i = 0; i < list.size(); ++i) {
         Entity entity1 = (Entity)list.get(i);
         if (entity1 != this.shootingEntity || this.ticksInAir >= 5) {
            AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expandXyz(0.30000001192092896D);
            RayTraceResult movingobjectposition = axisalignedbb.calculateIntercept(vec3d, vec3d1);
            if (movingobjectposition != null) {
               double d1 = vec3d.squareDistanceTo(movingobjectposition.hitVec);
               if (d1 < d0 || d0 == 0.0D) {
                  entity = entity1;
                  d0 = d1;
               }
            }
         }
      }

      return entity;
   }

   public static void registerFixesArrow(DataFixer dataconvertermanager, String s) {
   }

   public static void registerFixesArrow(DataFixer dataconvertermanager) {
      registerFixesArrow(dataconvertermanager, "Arrow");
   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setInteger("xTile", this.xTile);
      nbttagcompound.setInteger("yTile", this.yTile);
      nbttagcompound.setInteger("zTile", this.zTile);
      nbttagcompound.setShort("life", (short)this.ticksInGround);
      ResourceLocation minecraftkey = (ResourceLocation)Block.REGISTRY.getNameForObject(this.inTile);
      nbttagcompound.setString("inTile", minecraftkey == null ? "" : minecraftkey.toString());
      nbttagcompound.setByte("inData", (byte)this.inData);
      nbttagcompound.setByte("shake", (byte)this.arrowShake);
      nbttagcompound.setByte("inGround", (byte)(this.inGround ? 1 : 0));
      nbttagcompound.setByte("pickup", (byte)this.pickupStatus.ordinal());
      nbttagcompound.setDouble("damage", this.damage);
   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      this.xTile = nbttagcompound.getInteger("xTile");
      this.yTile = nbttagcompound.getInteger("yTile");
      this.zTile = nbttagcompound.getInteger("zTile");
      this.ticksInGround = nbttagcompound.getShort("life");
      if (nbttagcompound.hasKey("inTile", 8)) {
         this.inTile = Block.getBlockFromName(nbttagcompound.getString("inTile"));
      } else {
         this.inTile = Block.getBlockById(nbttagcompound.getByte("inTile") & 255);
      }

      this.inData = nbttagcompound.getByte("inData") & 255;
      this.arrowShake = nbttagcompound.getByte("shake") & 255;
      this.inGround = nbttagcompound.getByte("inGround") == 1;
      if (nbttagcompound.hasKey("damage", 99)) {
         this.damage = nbttagcompound.getDouble("damage");
      }

      if (nbttagcompound.hasKey("pickup", 99)) {
         this.pickupStatus = EntityArrow.PickupStatus.getByOrdinal(nbttagcompound.getByte("pickup"));
      } else if (nbttagcompound.hasKey("player", 99)) {
         this.pickupStatus = nbttagcompound.getBoolean("player") ? EntityArrow.PickupStatus.ALLOWED : EntityArrow.PickupStatus.DISALLOWED;
      }

   }

   public void onCollideWithPlayer(EntityPlayer entityhuman) {
      if (!this.world.isRemote && this.inGround && this.arrowShake <= 0) {
         ItemStack itemstack = new ItemStack(Items.ARROW);
         if (this.pickupStatus == EntityArrow.PickupStatus.ALLOWED && entityhuman.inventory.canHold(itemstack) > 0) {
            EntityItem item = new EntityItem(this.world, this.posX, this.posY, this.posZ, itemstack);
            PlayerPickupArrowEvent event = new PlayerPickupArrowEvent((Player)entityhuman.getBukkitEntity(), new CraftItem(this.world.getServer(), this, item), (Arrow)this.getBukkitEntity());
            this.world.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
               return;
            }
         }

         boolean flag = this.pickupStatus == EntityArrow.PickupStatus.ALLOWED || this.pickupStatus == EntityArrow.PickupStatus.CREATIVE_ONLY && entityhuman.capabilities.isCreativeMode;
         if (this.pickupStatus == EntityArrow.PickupStatus.ALLOWED && !entityhuman.inventory.addItemStackToInventory(this.getArrowStack())) {
            flag = false;
         }

         if (flag) {
            this.playSound(SoundEvents.ENTITY_ITEM_PICKUP, 0.2F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            entityhuman.onItemPickup(this, 1);
            this.setDead();
         }
      }

   }

   protected abstract ItemStack getArrowStack();

   protected boolean canTriggerWalking() {
      return false;
   }

   public void setDamage(double d0) {
      this.damage = d0;
   }

   public double getDamage() {
      return this.damage;
   }

   public void setKnockbackStrength(int i) {
      this.knockbackStrength = i;
   }

   public boolean canBeAttackedWithItem() {
      return false;
   }

   public float getEyeHeight() {
      return 0.0F;
   }

   public void setIsCritical(boolean flag) {
      byte b0 = ((Byte)this.dataManager.get(CRITICAL)).byteValue();
      if (flag) {
         this.dataManager.set(CRITICAL, Byte.valueOf((byte)(b0 | 1)));
      } else {
         this.dataManager.set(CRITICAL, Byte.valueOf((byte)(b0 & -2)));
      }

   }

   public boolean getIsCritical() {
      byte b0 = ((Byte)this.dataManager.get(CRITICAL)).byteValue();
      return (b0 & 1) != 0;
   }

   public boolean isInGround() {
      return this.inGround;
   }

   public static enum PickupStatus {
      DISALLOWED,
      ALLOWED,
      CREATIVE_ONLY;

      public static EntityArrow.PickupStatus getByOrdinal(int i) {
         if (i < 0 || i > values().length) {
            i = 0;
         }

         return values()[i];
      }
   }
}
