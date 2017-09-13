package net.minecraft.entity.item;

import com.google.common.collect.Lists;
import java.util.ArrayList;
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
      super(var1);
      this.paddlePositions = new float[2];
      this.preventEntitySpawning = true;
      this.setSize(1.375F, 0.5625F);
   }

   public EntityBoat(World var1, double var2, double var4, double var6) {
      this(var1);
      this.setPosition(var2, var4, var6);
      this.motionX = 0.0D;
      this.motionY = 0.0D;
      this.motionZ = 0.0D;
      this.prevPosX = var2;
      this.prevPosY = var4;
      this.prevPosZ = var6;
   }

   protected boolean canTriggerWalking() {
      return false;
   }

   protected void entityInit() {
      this.dataManager.register(TIME_SINCE_HIT, Integer.valueOf(0));
      this.dataManager.register(FORWARD_DIRECTION, Integer.valueOf(1));
      this.dataManager.register(DAMAGE_TAKEN, Float.valueOf(0.0F));
      this.dataManager.register(BOAT_TYPE, Integer.valueOf(EntityBoat.Type.OAK.ordinal()));

      for(DataParameter var4 : DATA_ID_PADDLE) {
         this.dataManager.register(var4, Boolean.valueOf(false));
      }

   }

   @Nullable
   public AxisAlignedBB getCollisionBox(Entity var1) {
      return var1.getEntityBoundingBox();
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
      if (this.isEntityInvulnerable(var1)) {
         return false;
      } else if (!this.world.isRemote && !this.isDead) {
         if (var1 instanceof EntityDamageSourceIndirect && var1.getEntity() != null && this.isPassenger(var1.getEntity())) {
            return false;
         } else {
            this.setForwardDirection(-this.getForwardDirection());
            this.setTimeSinceHit(10);
            this.setDamageTaken(this.getDamageTaken() + var2 * 10.0F);
            this.setBeenAttacked();
            boolean var3 = var1.getEntity() instanceof EntityPlayer && ((EntityPlayer)var1.getEntity()).capabilities.isCreativeMode;
            if (var3 || this.getDamageTaken() > 40.0F) {
               if (!var3 && this.world.getGameRules().getBoolean("doEntityDrops")) {
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
      if (var1 instanceof EntityBoat) {
         if (var1.getEntityBoundingBox().minY < this.getEntityBoundingBox().maxY) {
            super.applyEntityCollision(var1);
         }
      } else if (var1.getEntityBoundingBox().minY <= this.getEntityBoundingBox().minY) {
         super.applyEntityCollision(var1);
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
      this.boatPitch = var1;
      this.lerpY = var3;
      this.lerpZ = var5;
      this.boatYaw = (double)var7;
      this.lerpXRot = (double)var8;
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

      for(int var1 = 0; var1 <= 1; ++var1) {
         if (this.getPaddleState(var1)) {
            this.paddlePositions[var1] = (float)((double)this.paddlePositions[var1] + 0.01D);
         } else {
            this.paddlePositions[var1] = 0.0F;
         }
      }

      this.doBlockCollisions();
      List var5 = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox().expand(0.20000000298023224D, -0.009999999776482582D, 0.20000000298023224D), EntitySelectors.getTeamCollisionPredicate(this));
      if (!var5.isEmpty()) {
         boolean var2 = !this.world.isRemote && !(this.getControllingPassenger() instanceof EntityPlayer);

         for(int var3 = 0; var3 < var5.size(); ++var3) {
            Entity var4 = (Entity)var5.get(var3);
            if (!var4.isPassenger(this)) {
               if (var2 && this.getPassengers().size() < 2 && !var4.isRiding() && var4.width < this.width && var4 instanceof EntityLivingBase && !(var4 instanceof EntityWaterMob) && !(var4 instanceof EntityPlayer)) {
                  var4.startRiding(this);
               } else {
                  this.applyEntityCollision(var4);
               }
            }
         }
      }

   }

   private void tickLerp() {
      if (this.lerpSteps > 0 && !this.canPassengerSteer()) {
         double var1 = this.posX + (this.boatPitch - this.posX) / (double)this.lerpSteps;
         double var3 = this.posY + (this.lerpY - this.posY) / (double)this.lerpSteps;
         double var5 = this.posZ + (this.lerpZ - this.posZ) / (double)this.lerpSteps;
         double var7 = MathHelper.wrapDegrees(this.boatYaw - (double)this.rotationYaw);
         this.rotationYaw = (float)((double)this.rotationYaw + var7 / (double)this.lerpSteps);
         this.rotationPitch = (float)((double)this.rotationPitch + (this.lerpXRot - (double)this.rotationPitch) / (double)this.lerpSteps);
         --this.lerpSteps;
         this.setPosition(var1, var3, var5);
         this.setRotation(this.rotationYaw, this.rotationPitch);
      }

   }

   public void setPaddleState(boolean var1, boolean var2) {
      this.dataManager.set(DATA_ID_PADDLE[0], Boolean.valueOf(var1));
      this.dataManager.set(DATA_ID_PADDLE[1], Boolean.valueOf(var2));
   }

   @SideOnly(Side.CLIENT)
   public float getRowingTime(int var1, float var2) {
      return this.getPaddleState(var1) ? (float)MathHelper.clampedLerp((double)this.paddlePositions[var1] - 0.01D, (double)this.paddlePositions[var1], (double)var2) : 0.0F;
   }

   private EntityBoat.Status getBoatStatus() {
      EntityBoat.Status var1 = this.getUnderwaterStatus();
      if (var1 != null) {
         this.waterLevel = this.getEntityBoundingBox().maxY;
         return var1;
      } else if (this.checkInWater()) {
         return EntityBoat.Status.IN_WATER;
      } else {
         float var2 = this.getBoatGlide();
         if (var2 > 0.0F) {
            this.boatGlide = var2;
            return EntityBoat.Status.ON_LAND;
         } else {
            return EntityBoat.Status.IN_AIR;
         }
      }
   }

   public float getWaterLevelAbove() {
      AxisAlignedBB var1 = this.getEntityBoundingBox();
      int var2 = MathHelper.floor(var1.minX);
      int var3 = MathHelper.ceil(var1.maxX);
      int var4 = MathHelper.floor(var1.maxY);
      int var5 = MathHelper.ceil(var1.maxY - this.lastYd);
      int var6 = MathHelper.floor(var1.minZ);
      int var7 = MathHelper.ceil(var1.maxZ);
      BlockPos.PooledMutableBlockPos var8 = BlockPos.PooledMutableBlockPos.retain();

      try {
         label107:
         for(int var9 = var4; var9 < var5; ++var9) {
            float var10 = 0.0F;
            int var11 = var2;

            while(true) {
               if (var11 >= var3) {
                  if (var10 < 1.0F) {
                     float var19 = (float)var8.getY() + var10;
                     float var20 = var19;
                     return var20;
                  }
                  break;
               }

               for(int var12 = var6; var12 < var7; ++var12) {
                  var8.setPos(var11, var9, var12);
                  IBlockState var13 = this.world.getBlockState(var8);
                  if (var13.getMaterial() == Material.WATER) {
                     var10 = Math.max(var10, getBlockLiquidHeight(var13, this.world, var8));
                  }

                  if (var10 >= 1.0F) {
                     continue label107;
                  }
               }

               ++var11;
            }
         }

         float var17 = (float)(var5 + 1);
         float var18 = var17;
         return var18;
      } finally {
         var8.release();
      }
   }

   public float getBoatGlide() {
      AxisAlignedBB var1 = this.getEntityBoundingBox();
      AxisAlignedBB var2 = new AxisAlignedBB(var1.minX, var1.minY - 0.001D, var1.minZ, var1.maxX, var1.minY, var1.maxZ);
      int var3 = MathHelper.floor(var2.minX) - 1;
      int var4 = MathHelper.ceil(var2.maxX) + 1;
      int var5 = MathHelper.floor(var2.minY) - 1;
      int var6 = MathHelper.ceil(var2.maxY) + 1;
      int var7 = MathHelper.floor(var2.minZ) - 1;
      int var8 = MathHelper.ceil(var2.maxZ) + 1;
      ArrayList var9 = Lists.newArrayList();
      float var10 = 0.0F;
      int var11 = 0;
      BlockPos.PooledMutableBlockPos var12 = BlockPos.PooledMutableBlockPos.retain();

      try {
         for(int var13 = var3; var13 < var4; ++var13) {
            for(int var14 = var7; var14 < var8; ++var14) {
               int var15 = (var13 != var3 && var13 != var4 - 1 ? 0 : 1) + (var14 != var7 && var14 != var8 - 1 ? 0 : 1);
               if (var15 != 2) {
                  for(int var16 = var5; var16 < var6; ++var16) {
                     if (var15 <= 0 || var16 != var5 && var16 != var6 - 1) {
                        var12.setPos(var13, var16, var14);
                        IBlockState var17 = this.world.getBlockState(var12);
                        var17.addCollisionBoxToList(this.world, var12, var2, var9, this);
                        if (!var9.isEmpty()) {
                           var10 += var17.getBlock().slipperiness;
                           ++var11;
                        }

                        var9.clear();
                     }
                  }
               }
            }
         }
      } finally {
         var12.release();
      }

      return var10 / (float)var11;
   }

   private boolean checkInWater() {
      AxisAlignedBB var1 = this.getEntityBoundingBox();
      int var2 = MathHelper.floor(var1.minX);
      int var3 = MathHelper.ceil(var1.maxX);
      int var4 = MathHelper.floor(var1.minY);
      int var5 = MathHelper.ceil(var1.minY + 0.001D);
      int var6 = MathHelper.floor(var1.minZ);
      int var7 = MathHelper.ceil(var1.maxZ);
      boolean var8 = false;
      this.waterLevel = Double.MIN_VALUE;
      BlockPos.PooledMutableBlockPos var9 = BlockPos.PooledMutableBlockPos.retain();

      try {
         for(int var10 = var2; var10 < var3; ++var10) {
            for(int var11 = var4; var11 < var5; ++var11) {
               for(int var12 = var6; var12 < var7; ++var12) {
                  var9.setPos(var10, var11, var12);
                  IBlockState var13 = this.world.getBlockState(var9);
                  if (var13.getMaterial() == Material.WATER) {
                     float var14 = getLiquidHeight(var13, this.world, var9);
                     this.waterLevel = Math.max((double)var14, this.waterLevel);
                     var8 |= var1.minY < (double)var14;
                  }
               }
            }
         }
      } finally {
         var9.release();
      }

      return var8;
   }

   @Nullable
   private EntityBoat.Status getUnderwaterStatus() {
      AxisAlignedBB var1 = this.getEntityBoundingBox();
      double var2 = var1.maxY + 0.001D;
      int var4 = MathHelper.floor(var1.minX);
      int var5 = MathHelper.ceil(var1.maxX);
      int var6 = MathHelper.floor(var1.maxY);
      int var7 = MathHelper.ceil(var2);
      int var8 = MathHelper.floor(var1.minZ);
      int var9 = MathHelper.ceil(var1.maxZ);
      boolean var10 = false;
      BlockPos.PooledMutableBlockPos var11 = BlockPos.PooledMutableBlockPos.retain();

      try {
         for(int var12 = var4; var12 < var5; ++var12) {
            for(int var13 = var6; var13 < var7; ++var13) {
               for(int var14 = var8; var14 < var9; ++var14) {
                  var11.setPos(var12, var13, var14);
                  IBlockState var15 = this.world.getBlockState(var11);
                  if (var15.getMaterial() == Material.WATER && var2 < (double)getLiquidHeight(var15, this.world, var11)) {
                     if (((Integer)var15.getValue(BlockLiquid.LEVEL)).intValue() != 0) {
                        EntityBoat.Status var16 = EntityBoat.Status.UNDER_FLOWING_WATER;
                        EntityBoat.Status var17 = var16;
                        return var17;
                     }

                     var10 = true;
                  }
               }
            }
         }
      } finally {
         var11.release();
      }

      return var10 ? EntityBoat.Status.UNDER_WATER : null;
   }

   public static float getBlockLiquidHeight(IBlockState var0, IBlockAccess var1, BlockPos var2) {
      int var3 = ((Integer)var0.getValue(BlockLiquid.LEVEL)).intValue();
      return (var3 & 7) == 0 && var1.getBlockState(var2.up()).getMaterial() == Material.WATER ? 1.0F : 1.0F - BlockLiquid.getLiquidHeightPercent(var3);
   }

   public static float getLiquidHeight(IBlockState var0, IBlockAccess var1, BlockPos var2) {
      return (float)var2.getY() + getBlockLiquidHeight(var0, var1, var2);
   }

   private void updateMotion() {
      double var1 = -0.03999999910593033D;
      double var3 = this.hasNoGravity() ? 0.0D : -0.03999999910593033D;
      double var5 = 0.0D;
      this.momentum = 0.05F;
      if (this.previousStatus == EntityBoat.Status.IN_AIR && this.status != EntityBoat.Status.IN_AIR && this.status != EntityBoat.Status.ON_LAND) {
         this.waterLevel = this.getEntityBoundingBox().minY + (double)this.height;
         this.setPosition(this.posX, (double)(this.getWaterLevelAbove() - this.height) + 0.101D, this.posZ);
         this.motionY = 0.0D;
         this.lastYd = 0.0D;
         this.status = EntityBoat.Status.IN_WATER;
      } else {
         if (this.status == EntityBoat.Status.IN_WATER) {
            var5 = (this.waterLevel - this.getEntityBoundingBox().minY) / (double)this.height;
            this.momentum = 0.9F;
         } else if (this.status == EntityBoat.Status.UNDER_FLOWING_WATER) {
            var3 = -7.0E-4D;
            this.momentum = 0.9F;
         } else if (this.status == EntityBoat.Status.UNDER_WATER) {
            var5 = 0.009999999776482582D;
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
         this.motionY += var3;
         if (var5 > 0.0D) {
            double var7 = 0.65D;
            this.motionY += var5 * 0.06153846016296973D;
            double var9 = 0.75D;
            this.motionY *= 0.75D;
         }
      }

   }

   private void controlBoat() {
      if (this.isBeingRidden()) {
         float var1 = 0.0F;
         if (this.leftInputDown) {
            this.deltaRotation += -1.0F;
         }

         if (this.rightInputDown) {
            ++this.deltaRotation;
         }

         if (this.rightInputDown != this.leftInputDown && !this.forwardInputDown && !this.backInputDown) {
            var1 += 0.005F;
         }

         this.rotationYaw += this.deltaRotation;
         if (this.forwardInputDown) {
            var1 += 0.04F;
         }

         if (this.backInputDown) {
            var1 -= 0.005F;
         }

         this.motionX += (double)(MathHelper.sin(-this.rotationYaw * 0.017453292F) * var1);
         this.motionZ += (double)(MathHelper.cos(this.rotationYaw * 0.017453292F) * var1);
         this.setPaddleState(this.rightInputDown || this.forwardInputDown, this.leftInputDown || this.forwardInputDown);
      }

   }

   public void updatePassenger(Entity var1) {
      if (this.isPassenger(var1)) {
         float var2 = 0.0F;
         float var3 = (float)((this.isDead ? 0.009999999776482582D : this.getMountedYOffset()) + var1.getYOffset());
         if (this.getPassengers().size() > 1) {
            int var4 = this.getPassengers().indexOf(var1);
            if (var4 == 0) {
               var2 = 0.2F;
            } else {
               var2 = -0.6F;
            }

            if (var1 instanceof EntityAnimal) {
               var2 = (float)((double)var2 + 0.2D);
            }
         }

         Vec3d var6 = (new Vec3d((double)var2, 0.0D, 0.0D)).rotateYaw(-this.rotationYaw * 0.017453292F - 1.5707964F);
         var1.setPosition(this.posX + var6.xCoord, this.posY + (double)var3, this.posZ + var6.zCoord);
         var1.rotationYaw += this.deltaRotation;
         var1.setRotationYawHead(var1.getRotationYawHead() + this.deltaRotation);
         this.applyYawToEntity(var1);
         if (var1 instanceof EntityAnimal && this.getPassengers().size() > 1) {
            int var5 = var1.getEntityId() % 2 == 0 ? 90 : 270;
            var1.setRenderYawOffset(((EntityAnimal)var1).renderYawOffset + (float)var5);
            var1.setRotationYawHead(var1.getRotationYawHead() + (float)var5);
         }
      }

   }

   protected void applyYawToEntity(Entity var1) {
      var1.setRenderYawOffset(this.rotationYaw);
      float var2 = MathHelper.wrapDegrees(var1.rotationYaw - this.rotationYaw);
      float var3 = MathHelper.clamp(var2, -105.0F, 105.0F);
      var1.prevRotationYaw += var3 - var2;
      var1.rotationYaw += var3 - var2;
      var1.setRotationYawHead(var1.rotationYaw);
   }

   @SideOnly(Side.CLIENT)
   public void applyOrientationToEntity(Entity var1) {
      this.applyYawToEntity(var1);
   }

   protected void writeEntityToNBT(NBTTagCompound var1) {
      var1.setString("Type", this.getBoatType().getName());
   }

   protected void readEntityFromNBT(NBTTagCompound var1) {
      if (var1.hasKey("Type", 8)) {
         this.setBoatType(EntityBoat.Type.getTypeFromString(var1.getString("Type")));
      }

   }

   public boolean processInitialInteract(EntityPlayer var1, @Nullable ItemStack var2, EnumHand var3) {
      if (!this.world.isRemote && !var1.isSneaking() && this.outOfControlTicks < 60.0F) {
         var1.startRiding(this);
      }

      return true;
   }

   protected void updateFallState(double var1, boolean var3, IBlockState var4, BlockPos var5) {
      this.lastYd = this.motionY;
      if (!this.isRiding()) {
         if (var3) {
            if (this.fallDistance > 3.0F) {
               if (this.status != EntityBoat.Status.ON_LAND) {
                  this.fallDistance = 0.0F;
                  return;
               }

               this.fall(this.fallDistance, 1.0F);
               if (!this.world.isRemote && !this.isDead) {
                  this.setDead();
                  if (this.world.getGameRules().getBoolean("doEntityDrops")) {
                     for(int var6 = 0; var6 < 3; ++var6) {
                        this.entityDropItem(new ItemStack(Item.getItemFromBlock(Blocks.PLANKS), 1, this.getBoatType().getMetadata()), 0.0F);
                     }

                     for(int var7 = 0; var7 < 2; ++var7) {
                        this.dropItemWithOffset(Items.STICK, 1, 0.0F);
                     }
                  }
               }
            }

            this.fallDistance = 0.0F;
         } else if (this.world.getBlockState((new BlockPos(this)).down()).getMaterial() != Material.WATER && var1 < 0.0D) {
            this.fallDistance = (float)((double)this.fallDistance - var1);
         }
      }

   }

   public boolean getPaddleState(int var1) {
      return ((Boolean)this.dataManager.get(DATA_ID_PADDLE[var1])).booleanValue() && this.getControllingPassenger() != null;
   }

   public void setDamageTaken(float var1) {
      this.dataManager.set(DAMAGE_TAKEN, Float.valueOf(var1));
   }

   public float getDamageTaken() {
      return ((Float)this.dataManager.get(DAMAGE_TAKEN)).floatValue();
   }

   public void setTimeSinceHit(int var1) {
      this.dataManager.set(TIME_SINCE_HIT, Integer.valueOf(var1));
   }

   public int getTimeSinceHit() {
      return ((Integer)this.dataManager.get(TIME_SINCE_HIT)).intValue();
   }

   public void setForwardDirection(int var1) {
      this.dataManager.set(FORWARD_DIRECTION, Integer.valueOf(var1));
   }

   public int getForwardDirection() {
      return ((Integer)this.dataManager.get(FORWARD_DIRECTION)).intValue();
   }

   public void setBoatType(EntityBoat.Type var1) {
      this.dataManager.set(BOAT_TYPE, Integer.valueOf(var1.ordinal()));
   }

   public EntityBoat.Type getBoatType() {
      return EntityBoat.Type.byId(((Integer)this.dataManager.get(BOAT_TYPE)).intValue());
   }

   protected boolean canFitPassenger(Entity var1) {
      return this.getPassengers().size() < 2;
   }

   @Nullable
   public Entity getControllingPassenger() {
      List var1 = this.getPassengers();
      return var1.isEmpty() ? null : (Entity)var1.get(0);
   }

   @SideOnly(Side.CLIENT)
   public void updateInputs(boolean var1, boolean var2, boolean var3, boolean var4) {
      this.leftInputDown = var1;
      this.rightInputDown = var2;
      this.forwardInputDown = var3;
      this.backInputDown = var4;
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
         this.name = var4;
         this.metadata = var3;
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
         if (var0 < 0 || var0 >= values().length) {
            var0 = 0;
         }

         return values()[var0];
      }

      public static EntityBoat.Type getTypeFromString(String var0) {
         for(int var1 = 0; var1 < values().length; ++var1) {
            if (values()[var1].getName().equals(var0)) {
               return values()[var1];
            }
         }

         return values()[0];
      }
   }
}
