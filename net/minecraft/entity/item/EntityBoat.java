package net.minecraft.entity.item;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.client.CPacketSteerBoat;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityBoat extends Entity {
   private static final DataParameter TIME_SINCE_HIT = EntityDataManager.createKey(EntityBoat.class, DataSerializers.VARINT);
   private static final DataParameter FORWARD_DIRECTION = EntityDataManager.createKey(EntityBoat.class, DataSerializers.VARINT);
   private static final DataParameter DAMAGE_TAKEN = EntityDataManager.createKey(EntityBoat.class, DataSerializers.FLOAT);
   private static final DataParameter BOAT_TYPE = EntityDataManager.createKey(EntityBoat.class, DataSerializers.VARINT);
   private static final DataParameter[] DATA_ID_PADDLE = new DataParameter[]{EntityDataManager.createKey(EntityBoat.class, DataSerializers.BOOLEAN), EntityDataManager.createKey(EntityBoat.class, DataSerializers.BOOLEAN)};
   private final float[] paddlePositions;
   private float momentum;
   private float outOfControlTicks;
   private float deltaRotation;
   private int lerpSteps;
   private double boatPitch;
   private double lerpY;
   private double lerpZ;
   private double boatYaw;
   private double lerpXRot;
   private boolean leftInputDown;
   private boolean rightInputDown;
   private boolean forwardInputDown;
   private boolean backInputDown;
   private double waterLevel;
   private float boatGlide;
   private EntityBoat.Status status;
   private EntityBoat.Status previousStatus;
   private double lastYd;

   public EntityBoat(World var1) {
      super(worldIn);
      this.paddlePositions = new float[2];
      this.preventEntitySpawning = true;
      this.setSize(1.375F, 0.5625F);
   }

   public EntityBoat(World var1, double var2, double var4, double var6) {
      this(worldIn);
      this.setPosition(x, y, z);
      this.motionX = 0.0D;
      this.motionY = 0.0D;
      this.motionZ = 0.0D;
      this.prevPosX = x;
      this.prevPosY = y;
      this.prevPosZ = z;
   }

   protected boolean canTriggerWalking() {
      return false;
   }

   protected void entityInit() {
      this.dataManager.register(TIME_SINCE_HIT, Integer.valueOf(0));
      this.dataManager.register(FORWARD_DIRECTION, Integer.valueOf(1));
      this.dataManager.register(DAMAGE_TAKEN, Float.valueOf(0.0F));
      this.dataManager.register(BOAT_TYPE, Integer.valueOf(EntityBoat.Type.OAK.ordinal()));

      for(DataParameter dataparameter : DATA_ID_PADDLE) {
         this.dataManager.register(dataparameter, Boolean.valueOf(false));
      }

   }

   @Nullable
   public AxisAlignedBB getCollisionBox(Entity var1) {
      return entityIn.getEntityBoundingBox();
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox() {
      return this.getEntityBoundingBox();
   }

   public boolean canBePushed() {
      return true;
   }

   public double getMountedYOffset() {
      return -0.1D;
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (this.isEntityInvulnerable(source)) {
         return false;
      } else if (!this.world.isRemote && !this.isDead) {
         if (source instanceof EntityDamageSourceIndirect && source.getEntity() != null && this.isPassenger(source.getEntity())) {
            return false;
         } else {
            this.setForwardDirection(-this.getForwardDirection());
            this.setTimeSinceHit(10);
            this.setDamageTaken(this.getDamageTaken() + amount * 10.0F);
            this.setBeenAttacked();
            boolean flag = source.getEntity() instanceof EntityPlayer && ((EntityPlayer)source.getEntity()).capabilities.isCreativeMode;
            if (flag || this.getDamageTaken() > 40.0F) {
               if (!flag && this.world.getGameRules().getBoolean("doEntityDrops")) {
                  this.dropItemWithOffset(this.getItemBoat(), 1, 0.0F);
               }

               this.setDead();
            }

            return true;
         }
      } else {
         return true;
      }
   }

   public void applyEntityCollision(Entity var1) {
      if (entityIn instanceof EntityBoat) {
         if (entityIn.getEntityBoundingBox().minY < this.getEntityBoundingBox().maxY) {
            super.applyEntityCollision(entityIn);
         }
      } else if (entityIn.getEntityBoundingBox().minY <= this.getEntityBoundingBox().minY) {
         super.applyEntityCollision(entityIn);
      }

   }

   public Item getItemBoat() {
      switch(this.getBoatType()) {
      case OAK:
      default:
         return Items.BOAT;
      case SPRUCE:
         return Items.SPRUCE_BOAT;
      case BIRCH:
         return Items.BIRCH_BOAT;
      case JUNGLE:
         return Items.JUNGLE_BOAT;
      case ACACIA:
         return Items.ACACIA_BOAT;
      case DARK_OAK:
         return Items.DARK_OAK_BOAT;
      }
   }

   @SideOnly(Side.CLIENT)
   public void performHurtAnimation() {
      this.setForwardDirection(-this.getForwardDirection());
      this.setTimeSinceHit(10);
      this.setDamageTaken(this.getDamageTaken() * 11.0F);
   }

   public boolean canBeCollidedWith() {
      return !this.isDead;
   }

   @SideOnly(Side.CLIENT)
   public void setPositionAndRotationDirect(double var1, double var3, double var5, float var7, float var8, int var9, boolean var10) {
      this.boatPitch = x;
      this.lerpY = y;
      this.lerpZ = z;
      this.boatYaw = (double)yaw;
      this.lerpXRot = (double)pitch;
      this.lerpSteps = 10;
   }

   public EnumFacing getAdjustedHorizontalFacing() {
      return this.getHorizontalFacing().rotateY();
   }

   public void onUpdate() {
      this.previousStatus = this.status;
      this.status = this.getBoatStatus();
      if (this.status != EntityBoat.Status.UNDER_WATER && this.status != EntityBoat.Status.UNDER_FLOWING_WATER) {
         this.outOfControlTicks = 0.0F;
      } else {
         ++this.outOfControlTicks;
      }

      if (!this.world.isRemote && this.outOfControlTicks >= 60.0F) {
         this.removePassengers();
      }

      if (this.getTimeSinceHit() > 0) {
         this.setTimeSinceHit(this.getTimeSinceHit() - 1);
      }

      if (this.getDamageTaken() > 0.0F) {
         this.setDamageTaken(this.getDamageTaken() - 1.0F);
      }

      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      super.onUpdate();
      this.tickLerp();
      if (this.canPassengerSteer()) {
         if (this.getPassengers().size() == 0 || !(this.getPassengers().get(0) instanceof EntityPlayer)) {
            this.setPaddleState(false, false);
         }

         this.updateMotion();
         if (this.world.isRemote) {
            this.controlBoat();
            this.world.sendPacketToServer(new CPacketSteerBoat(this.getPaddleState(0), this.getPaddleState(1)));
         }

         this.move(this.motionX, this.motionY, this.motionZ);
      } else {
         this.motionX = 0.0D;
         this.motionY = 0.0D;
         this.motionZ = 0.0D;
      }

      for(int i = 0; i <= 1; ++i) {
         if (this.getPaddleState(i)) {
            this.paddlePositions[i] = (float)((double)this.paddlePositions[i] + 0.01D);
         } else {
            this.paddlePositions[i] = 0.0F;
         }
      }

      this.doBlockCollisions();
      List list = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox().expand(0.20000000298023224D, -0.009999999776482582D, 0.20000000298023224D), EntitySelectors.getTeamCollisionPredicate(this));
      if (!list.isEmpty()) {
         boolean flag = !this.world.isRemote && !(this.getControllingPassenger() instanceof EntityPlayer);

         for(int j = 0; j < list.size(); ++j) {
            Entity entity = (Entity)list.get(j);
            if (!entity.isPassenger(this)) {
               if (flag && this.getPassengers().size() < 2 && !entity.isRiding() && entity.width < this.width && entity instanceof EntityLivingBase && !(entity instanceof EntityWaterMob) && !(entity instanceof EntityPlayer)) {
                  entity.startRiding(this);
               } else {
                  this.applyEntityCollision(entity);
               }
            }
         }
      }

   }

   private void tickLerp() {
      if (this.lerpSteps > 0 && !this.canPassengerSteer()) {
         double d0 = this.posX + (this.boatPitch - this.posX) / (double)this.lerpSteps;
         double d1 = this.posY + (this.lerpY - this.posY) / (double)this.lerpSteps;
         double d2 = this.posZ + (this.lerpZ - this.posZ) / (double)this.lerpSteps;
         double d3 = MathHelper.wrapDegrees(this.boatYaw - (double)this.rotationYaw);
         this.rotationYaw = (float)((double)this.rotationYaw + d3 / (double)this.lerpSteps);
         this.rotationPitch = (float)((double)this.rotationPitch + (this.lerpXRot - (double)this.rotationPitch) / (double)this.lerpSteps);
         --this.lerpSteps;
         this.setPosition(d0, d1, d2);
         this.setRotation(this.rotationYaw, this.rotationPitch);
      }

   }

   public void setPaddleState(boolean var1, boolean var2) {
      this.dataManager.set(DATA_ID_PADDLE[0], Boolean.valueOf(p_184445_1_));
      this.dataManager.set(DATA_ID_PADDLE[1], Boolean.valueOf(p_184445_2_));
   }

   @SideOnly(Side.CLIENT)
   public float getRowingTime(int var1, float var2) {
      return this.getPaddleState(p_184448_1_) ? (float)MathHelper.clampedLerp((double)this.paddlePositions[p_184448_1_] - 0.01D, (double)this.paddlePositions[p_184448_1_], (double)limbSwing) : 0.0F;
   }

   private EntityBoat.Status getBoatStatus() {
      EntityBoat.Status entityboat$status = this.getUnderwaterStatus();
      if (entityboat$status != null) {
         this.waterLevel = this.getEntityBoundingBox().maxY;
         return entityboat$status;
      } else if (this.checkInWater()) {
         return EntityBoat.Status.IN_WATER;
      } else {
         float f = this.getBoatGlide();
         if (f > 0.0F) {
            this.boatGlide = f;
            return EntityBoat.Status.ON_LAND;
         } else {
            return EntityBoat.Status.IN_AIR;
         }
      }
   }

   public float getWaterLevelAbove() {
      AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
      int i = MathHelper.floor(axisalignedbb.minX);
      int j = MathHelper.ceil(axisalignedbb.maxX);
      int k = MathHelper.floor(axisalignedbb.maxY);
      int l = MathHelper.ceil(axisalignedbb.maxY - this.lastYd);
      int i1 = MathHelper.floor(axisalignedbb.minZ);
      int j1 = MathHelper.ceil(axisalignedbb.maxZ);
      BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

      try {
         label107:
         for(int k1 = k; k1 < l; ++k1) {
            float f = 0.0F;
            int l1 = i;

            while(true) {
               if (l1 >= j) {
                  if (f < 1.0F) {
                     float f2 = (float)blockpos$pooledmutableblockpos.getY() + f;
                     float var20 = f2;
                     return var20;
                  }
                  break;
               }

               for(int i2 = i1; i2 < j1; ++i2) {
                  blockpos$pooledmutableblockpos.setPos(l1, k1, i2);
                  IBlockState iblockstate = this.world.getBlockState(blockpos$pooledmutableblockpos);
                  if (iblockstate.getMaterial() == Material.WATER) {
                     f = Math.max(f, getBlockLiquidHeight(iblockstate, this.world, blockpos$pooledmutableblockpos));
                  }

                  if (f >= 1.0F) {
                     continue label107;
                  }
               }

               ++l1;
            }
         }

         float f1 = (float)(l + 1);
         float var18 = f1;
         return var18;
      } finally {
         blockpos$pooledmutableblockpos.release();
      }
   }

   public float getBoatGlide() {
      AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
      AxisAlignedBB axisalignedbb1 = new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY - 0.001D, axisalignedbb.minZ, axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ);
      int i = MathHelper.floor(axisalignedbb1.minX) - 1;
      int j = MathHelper.ceil(axisalignedbb1.maxX) + 1;
      int k = MathHelper.floor(axisalignedbb1.minY) - 1;
      int l = MathHelper.ceil(axisalignedbb1.maxY) + 1;
      int i1 = MathHelper.floor(axisalignedbb1.minZ) - 1;
      int j1 = MathHelper.ceil(axisalignedbb1.maxZ) + 1;
      List list = Lists.newArrayList();
      float f = 0.0F;
      int k1 = 0;
      BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

      try {
         for(int l1 = i; l1 < j; ++l1) {
            for(int i2 = i1; i2 < j1; ++i2) {
               int j2 = (l1 != i && l1 != j - 1 ? 0 : 1) + (i2 != i1 && i2 != j1 - 1 ? 0 : 1);
               if (j2 != 2) {
                  for(int k2 = k; k2 < l; ++k2) {
                     if (j2 <= 0 || k2 != k && k2 != l - 1) {
                        blockpos$pooledmutableblockpos.setPos(l1, k2, i2);
                        IBlockState iblockstate = this.world.getBlockState(blockpos$pooledmutableblockpos);
                        iblockstate.addCollisionBoxToList(this.world, blockpos$pooledmutableblockpos, axisalignedbb1, list, this);
                        if (!list.isEmpty()) {
                           f += iblockstate.getBlock().slipperiness;
                           ++k1;
                        }

                        list.clear();
                     }
                  }
               }
            }
         }
      } finally {
         blockpos$pooledmutableblockpos.release();
      }

      return f / (float)k1;
   }

   private boolean checkInWater() {
      AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
      int i = MathHelper.floor(axisalignedbb.minX);
      int j = MathHelper.ceil(axisalignedbb.maxX);
      int k = MathHelper.floor(axisalignedbb.minY);
      int l = MathHelper.ceil(axisalignedbb.minY + 0.001D);
      int i1 = MathHelper.floor(axisalignedbb.minZ);
      int j1 = MathHelper.ceil(axisalignedbb.maxZ);
      boolean flag = false;
      this.waterLevel = Double.MIN_VALUE;
      BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

      try {
         for(int k1 = i; k1 < j; ++k1) {
            for(int l1 = k; l1 < l; ++l1) {
               for(int i2 = i1; i2 < j1; ++i2) {
                  blockpos$pooledmutableblockpos.setPos(k1, l1, i2);
                  IBlockState iblockstate = this.world.getBlockState(blockpos$pooledmutableblockpos);
                  if (iblockstate.getMaterial() == Material.WATER) {
                     float f = getLiquidHeight(iblockstate, this.world, blockpos$pooledmutableblockpos);
                     this.waterLevel = Math.max((double)f, this.waterLevel);
                     flag |= axisalignedbb.minY < (double)f;
                  }
               }
            }
         }
      } finally {
         blockpos$pooledmutableblockpos.release();
      }

      return flag;
   }

   @Nullable
   private EntityBoat.Status getUnderwaterStatus() {
      AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
      double d0 = axisalignedbb.maxY + 0.001D;
      int i = MathHelper.floor(axisalignedbb.minX);
      int j = MathHelper.ceil(axisalignedbb.maxX);
      int k = MathHelper.floor(axisalignedbb.maxY);
      int l = MathHelper.ceil(d0);
      int i1 = MathHelper.floor(axisalignedbb.minZ);
      int j1 = MathHelper.ceil(axisalignedbb.maxZ);
      boolean flag = false;
      BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

      try {
         for(int k1 = i; k1 < j; ++k1) {
            for(int l1 = k; l1 < l; ++l1) {
               for(int i2 = i1; i2 < j1; ++i2) {
                  blockpos$pooledmutableblockpos.setPos(k1, l1, i2);
                  IBlockState iblockstate = this.world.getBlockState(blockpos$pooledmutableblockpos);
                  if (iblockstate.getMaterial() == Material.WATER && d0 < (double)getLiquidHeight(iblockstate, this.world, blockpos$pooledmutableblockpos)) {
                     if (((Integer)iblockstate.getValue(BlockLiquid.LEVEL)).intValue() != 0) {
                        EntityBoat.Status entityboat$status = EntityBoat.Status.UNDER_FLOWING_WATER;
                        EntityBoat.Status var17 = entityboat$status;
                        return var17;
                     }

                     flag = true;
                  }
               }
            }
         }
      } finally {
         blockpos$pooledmutableblockpos.release();
      }

      return flag ? EntityBoat.Status.UNDER_WATER : null;
   }

   public static float getBlockLiquidHeight(IBlockState var0, IBlockAccess var1, BlockPos var2) {
      int i = ((Integer)p_184456_0_.getValue(BlockLiquid.LEVEL)).intValue();
      return (i & 7) == 0 && p_184456_1_.getBlockState(p_184456_2_.up()).getMaterial() == Material.WATER ? 1.0F : 1.0F - BlockLiquid.getLiquidHeightPercent(i);
   }

   public static float getLiquidHeight(IBlockState var0, IBlockAccess var1, BlockPos var2) {
      return (float)p_184452_2_.getY() + getBlockLiquidHeight(p_184452_0_, p_184452_1_, p_184452_2_);
   }

   private void updateMotion() {
      double d0 = -0.03999999910593033D;
      double d1 = this.hasNoGravity() ? 0.0D : -0.03999999910593033D;
      double d2 = 0.0D;
      this.momentum = 0.05F;
      if (this.previousStatus == EntityBoat.Status.IN_AIR && this.status != EntityBoat.Status.IN_AIR && this.status != EntityBoat.Status.ON_LAND) {
         this.waterLevel = this.getEntityBoundingBox().minY + (double)this.height;
         this.setPosition(this.posX, (double)(this.getWaterLevelAbove() - this.height) + 0.101D, this.posZ);
         this.motionY = 0.0D;
         this.lastYd = 0.0D;
         this.status = EntityBoat.Status.IN_WATER;
      } else {
         if (this.status == EntityBoat.Status.IN_WATER) {
            d2 = (this.waterLevel - this.getEntityBoundingBox().minY) / (double)this.height;
            this.momentum = 0.9F;
         } else if (this.status == EntityBoat.Status.UNDER_FLOWING_WATER) {
            d1 = -7.0E-4D;
            this.momentum = 0.9F;
         } else if (this.status == EntityBoat.Status.UNDER_WATER) {
            d2 = 0.009999999776482582D;
            this.momentum = 0.45F;
         } else if (this.status == EntityBoat.Status.IN_AIR) {
            this.momentum = 0.9F;
         } else if (this.status == EntityBoat.Status.ON_LAND) {
            this.momentum = this.boatGlide;
            if (this.getControllingPassenger() instanceof EntityPlayer) {
               this.boatGlide /= 2.0F;
            }
         }

         this.motionX *= (double)this.momentum;
         this.motionZ *= (double)this.momentum;
         this.deltaRotation *= this.momentum;
         this.motionY += d1;
         if (d2 > 0.0D) {
            double d3 = 0.65D;
            this.motionY += d2 * 0.06153846016296973D;
            double d4 = 0.75D;
            this.motionY *= 0.75D;
         }
      }

   }

   private void controlBoat() {
      if (this.isBeingRidden()) {
         float f = 0.0F;
         if (this.leftInputDown) {
            this.deltaRotation += -1.0F;
         }

         if (this.rightInputDown) {
            ++this.deltaRotation;
         }

         if (this.rightInputDown != this.leftInputDown && !this.forwardInputDown && !this.backInputDown) {
            f += 0.005F;
         }

         this.rotationYaw += this.deltaRotation;
         if (this.forwardInputDown) {
            f += 0.04F;
         }

         if (this.backInputDown) {
            f -= 0.005F;
         }

         this.motionX += (double)(MathHelper.sin(-this.rotationYaw * 0.017453292F) * f);
         this.motionZ += (double)(MathHelper.cos(this.rotationYaw * 0.017453292F) * f);
         this.setPaddleState(this.rightInputDown || this.forwardInputDown, this.leftInputDown || this.forwardInputDown);
      }

   }

   public void updatePassenger(Entity var1) {
      if (this.isPassenger(passenger)) {
         float f = 0.0F;
         float f1 = (float)((this.isDead ? 0.009999999776482582D : this.getMountedYOffset()) + passenger.getYOffset());
         if (this.getPassengers().size() > 1) {
            int i = this.getPassengers().indexOf(passenger);
            if (i == 0) {
               f = 0.2F;
            } else {
               f = -0.6F;
            }

            if (passenger instanceof EntityAnimal) {
               f = (float)((double)f + 0.2D);
            }
         }

         Vec3d vec3d = (new Vec3d((double)f, 0.0D, 0.0D)).rotateYaw(-this.rotationYaw * 0.017453292F - 1.5707964F);
         passenger.setPosition(this.posX + vec3d.xCoord, this.posY + (double)f1, this.posZ + vec3d.zCoord);
         passenger.rotationYaw += this.deltaRotation;
         passenger.setRotationYawHead(passenger.getRotationYawHead() + this.deltaRotation);
         this.applyYawToEntity(passenger);
         if (passenger instanceof EntityAnimal && this.getPassengers().size() > 1) {
            int j = passenger.getEntityId() % 2 == 0 ? 90 : 270;
            passenger.setRenderYawOffset(((EntityAnimal)passenger).renderYawOffset + (float)j);
            passenger.setRotationYawHead(passenger.getRotationYawHead() + (float)j);
         }
      }

   }

   protected void applyYawToEntity(Entity var1) {
      entityToUpdate.setRenderYawOffset(this.rotationYaw);
      float f = MathHelper.wrapDegrees(entityToUpdate.rotationYaw - this.rotationYaw);
      float f1 = MathHelper.clamp(f, -105.0F, 105.0F);
      entityToUpdate.prevRotationYaw += f1 - f;
      entityToUpdate.rotationYaw += f1 - f;
      entityToUpdate.setRotationYawHead(entityToUpdate.rotationYaw);
   }

   @SideOnly(Side.CLIENT)
   public void applyOrientationToEntity(Entity var1) {
      this.applyYawToEntity(entityToUpdate);
   }

   protected void writeEntityToNBT(NBTTagCompound var1) {
      compound.setString("Type", this.getBoatType().getName());
   }

   protected void readEntityFromNBT(NBTTagCompound var1) {
      if (compound.hasKey("Type", 8)) {
         this.setBoatType(EntityBoat.Type.getTypeFromString(compound.getString("Type")));
      }

   }

   public boolean processInitialInteract(EntityPlayer var1, @Nullable ItemStack var2, EnumHand var3) {
      if (!this.world.isRemote && !player.isSneaking() && this.outOfControlTicks < 60.0F) {
         player.startRiding(this);
      }

      return true;
   }

   protected void updateFallState(double var1, boolean var3, IBlockState var4, BlockPos var5) {
      this.lastYd = this.motionY;
      if (!this.isRiding()) {
         if (onGroundIn) {
            if (this.fallDistance > 3.0F) {
               if (this.status != EntityBoat.Status.ON_LAND) {
                  this.fallDistance = 0.0F;
                  return;
               }

               this.fall(this.fallDistance, 1.0F);
               if (!this.world.isRemote && !this.isDead) {
                  this.setDead();
                  if (this.world.getGameRules().getBoolean("doEntityDrops")) {
                     for(int i = 0; i < 3; ++i) {
                        this.entityDropItem(new ItemStack(Item.getItemFromBlock(Blocks.PLANKS), 1, this.getBoatType().getMetadata()), 0.0F);
                     }

                     for(int j = 0; j < 2; ++j) {
                        this.dropItemWithOffset(Items.STICK, 1, 0.0F);
                     }
                  }
               }
            }

            this.fallDistance = 0.0F;
         } else if (this.world.getBlockState((new BlockPos(this)).down()).getMaterial() != Material.WATER && y < 0.0D) {
            this.fallDistance = (float)((double)this.fallDistance - y);
         }
      }

   }

   public boolean getPaddleState(int var1) {
      return ((Boolean)this.dataManager.get(DATA_ID_PADDLE[p_184457_1_])).booleanValue() && this.getControllingPassenger() != null;
   }

   public void setDamageTaken(float var1) {
      this.dataManager.set(DAMAGE_TAKEN, Float.valueOf(damageTaken));
   }

   public float getDamageTaken() {
      return ((Float)this.dataManager.get(DAMAGE_TAKEN)).floatValue();
   }

   public void setTimeSinceHit(int var1) {
      this.dataManager.set(TIME_SINCE_HIT, Integer.valueOf(timeSinceHit));
   }

   public int getTimeSinceHit() {
      return ((Integer)this.dataManager.get(TIME_SINCE_HIT)).intValue();
   }

   public void setForwardDirection(int var1) {
      this.dataManager.set(FORWARD_DIRECTION, Integer.valueOf(forwardDirection));
   }

   public int getForwardDirection() {
      return ((Integer)this.dataManager.get(FORWARD_DIRECTION)).intValue();
   }

   public void setBoatType(EntityBoat.Type var1) {
      this.dataManager.set(BOAT_TYPE, Integer.valueOf(boatType.ordinal()));
   }

   public EntityBoat.Type getBoatType() {
      return EntityBoat.Type.byId(((Integer)this.dataManager.get(BOAT_TYPE)).intValue());
   }

   protected boolean canFitPassenger(Entity var1) {
      return this.getPassengers().size() < 2;
   }

   @Nullable
   public Entity getControllingPassenger() {
      List list = this.getPassengers();
      return list.isEmpty() ? null : (Entity)list.get(0);
   }

   @SideOnly(Side.CLIENT)
   public void updateInputs(boolean var1, boolean var2, boolean var3, boolean var4) {
      this.leftInputDown = p_184442_1_;
      this.rightInputDown = p_184442_2_;
      this.forwardInputDown = p_184442_3_;
      this.backInputDown = p_184442_4_;
   }

   public static enum Status {
      IN_WATER,
      UNDER_WATER,
      UNDER_FLOWING_WATER,
      ON_LAND,
      IN_AIR;
   }

   public static enum Type {
      OAK(BlockPlanks.EnumType.OAK.getMetadata(), "oak"),
      SPRUCE(BlockPlanks.EnumType.SPRUCE.getMetadata(), "spruce"),
      BIRCH(BlockPlanks.EnumType.BIRCH.getMetadata(), "birch"),
      JUNGLE(BlockPlanks.EnumType.JUNGLE.getMetadata(), "jungle"),
      ACACIA(BlockPlanks.EnumType.ACACIA.getMetadata(), "acacia"),
      DARK_OAK(BlockPlanks.EnumType.DARK_OAK.getMetadata(), "dark_oak");

      private final String name;
      private final int metadata;

      private Type(int var3, String var4) {
         this.name = nameIn;
         this.metadata = metadataIn;
      }

      public String getName() {
         return this.name;
      }

      public int getMetadata() {
         return this.metadata;
      }

      public String toString() {
         return this.name;
      }

      public static EntityBoat.Type byId(int var0) {
         if (id < 0 || id >= values().length) {
            id = 0;
         }

         return values()[id];
      }

      public static EntityBoat.Type getTypeFromString(String var0) {
         for(int i = 0; i < values().length; ++i) {
            if (values()[i].getName().equals(nameIn)) {
               return values()[i];
            }
         }

         return values()[0];
      }
   }
}
