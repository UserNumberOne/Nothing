package net.minecraft.entity.item;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailPowered;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IMinecartCollisionHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.minecart.MinecartCollisionEvent;
import net.minecraftforge.event.entity.minecart.MinecartUpdateEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class EntityMinecart extends Entity implements IWorldNameable {
   private static final DataParameter ROLLING_AMPLITUDE = EntityDataManager.createKey(EntityMinecart.class, DataSerializers.VARINT);
   private static final DataParameter ROLLING_DIRECTION = EntityDataManager.createKey(EntityMinecart.class, DataSerializers.VARINT);
   private static final DataParameter DAMAGE = EntityDataManager.createKey(EntityMinecart.class, DataSerializers.FLOAT);
   private static final DataParameter DISPLAY_TILE = EntityDataManager.createKey(EntityMinecart.class, DataSerializers.VARINT);
   private static final DataParameter DISPLAY_TILE_OFFSET = EntityDataManager.createKey(EntityMinecart.class, DataSerializers.VARINT);
   private static final DataParameter SHOW_BLOCK = EntityDataManager.createKey(EntityMinecart.class, DataSerializers.BOOLEAN);
   private boolean isInReverse;
   private static final int[][][] MATRIX = new int[][][]{{{0, 0, -1}, {0, 0, 1}}, {{-1, 0, 0}, {1, 0, 0}}, {{-1, -1, 0}, {1, 0, 0}}, {{-1, 0, 0}, {1, -1, 0}}, {{0, 0, -1}, {0, -1, 1}}, {{0, -1, -1}, {0, 0, 1}}, {{0, 0, 1}, {1, 0, 0}}, {{0, 0, 1}, {-1, 0, 0}}, {{0, 0, -1}, {-1, 0, 0}}, {{0, 0, -1}, {1, 0, 0}}};
   private int turnProgress;
   private double minecartX;
   private double minecartY;
   private double minecartZ;
   private double minecartYaw;
   private double minecartPitch;
   @SideOnly(Side.CLIENT)
   private double velocityX;
   @SideOnly(Side.CLIENT)
   private double velocityY;
   @SideOnly(Side.CLIENT)
   private double velocityZ;
   public static float defaultMaxSpeedAirLateral = 0.4F;
   public static float defaultMaxSpeedAirVertical = -1.0F;
   public static double defaultDragAir = 0.949999988079071D;
   protected boolean canUseRail;
   protected boolean canBePushed;
   private static IMinecartCollisionHandler collisionHandler = null;
   private float currentSpeedRail;
   protected float maxSpeedAirLateral;
   protected float maxSpeedAirVertical;
   protected double dragAir;

   public EntityMinecart(World var1) {
      super(var1);
      this.canUseRail = true;
      this.canBePushed = true;
      this.currentSpeedRail = this.getMaxCartSpeedOnRail();
      this.maxSpeedAirLateral = defaultMaxSpeedAirLateral;
      this.maxSpeedAirVertical = defaultMaxSpeedAirVertical;
      this.dragAir = defaultDragAir;
      this.preventEntitySpawning = true;
      this.setSize(0.98F, 0.7F);
   }

   public static EntityMinecart create(World var0, double var1, double var3, double var5, EntityMinecart.Type var7) {
      switch(var7) {
      case CHEST:
         return new EntityMinecartChest(var0, var1, var3, var5);
      case FURNACE:
         return new EntityMinecartFurnace(var0, var1, var3, var5);
      case TNT:
         return new EntityMinecartTNT(var0, var1, var3, var5);
      case SPAWNER:
         return new EntityMinecartMobSpawner(var0, var1, var3, var5);
      case HOPPER:
         return new EntityMinecartHopper(var0, var1, var3, var5);
      case COMMAND_BLOCK:
         return new EntityMinecartCommandBlock(var0, var1, var3, var5);
      default:
         return new EntityMinecartEmpty(var0, var1, var3, var5);
      }
   }

   protected boolean canTriggerWalking() {
      return false;
   }

   protected void entityInit() {
      this.dataManager.register(ROLLING_AMPLITUDE, Integer.valueOf(0));
      this.dataManager.register(ROLLING_DIRECTION, Integer.valueOf(1));
      this.dataManager.register(DAMAGE, Float.valueOf(0.0F));
      this.dataManager.register(DISPLAY_TILE, Integer.valueOf(0));
      this.dataManager.register(DISPLAY_TILE_OFFSET, Integer.valueOf(6));
      this.dataManager.register(SHOW_BLOCK, Boolean.valueOf(false));
   }

   @Nullable
   public AxisAlignedBB getCollisionBox(Entity var1) {
      if (getCollisionHandler() != null) {
         return getCollisionHandler().getCollisionBox(this, var1);
      } else {
         return var1.canBePushed() ? var1.getEntityBoundingBox() : null;
      }
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox() {
      return getCollisionHandler() != null ? getCollisionHandler().getBoundingBox(this) : null;
   }

   public boolean canBePushed() {
      return this.canBePushed;
   }

   public EntityMinecart(World var1, double var2, double var4, double var6) {
      this(var1);
      this.setPosition(var2, var4, var6);
      this.motionX = 0.0D;
      this.motionY = 0.0D;
      this.motionZ = 0.0D;
      this.prevPosX = var2;
      this.prevPosY = var4;
      this.prevPosZ = var6;
   }

   public double getMountedYOffset() {
      return 0.0D;
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (!this.world.isRemote && !this.isDead) {
         if (this.isEntityInvulnerable(var1)) {
            return false;
         } else {
            this.setRollingDirection(-this.getRollingDirection());
            this.setRollingAmplitude(10);
            this.setBeenAttacked();
            this.setDamage(this.getDamage() + var2 * 10.0F);
            boolean var3 = var1.getEntity() instanceof EntityPlayer && ((EntityPlayer)var1.getEntity()).capabilities.isCreativeMode;
            if (var3 || this.getDamage() > 40.0F) {
               this.removePassengers();
               if (var3 && !this.hasCustomName()) {
                  this.setDead();
               } else {
                  this.killMinecart(var1);
               }
            }

            return true;
         }
      } else {
         return true;
      }
   }

   public void killMinecart(DamageSource var1) {
      this.setDead();
      if (this.world.getGameRules().getBoolean("doEntityDrops")) {
         ItemStack var2 = new ItemStack(Items.MINECART, 1);
         if (this.getName() != null) {
            var2.setStackDisplayName(this.getName());
         }

         this.entityDropItem(var2, 0.0F);
      }

   }

   @SideOnly(Side.CLIENT)
   public void performHurtAnimation() {
      this.setRollingDirection(-this.getRollingDirection());
      this.setRollingAmplitude(10);
      this.setDamage(this.getDamage() + this.getDamage() * 10.0F);
   }

   public boolean canBeCollidedWith() {
      return !this.isDead;
   }

   public void setDead() {
      super.setDead();
   }

   public EnumFacing getAdjustedHorizontalFacing() {
      return this.isInReverse ? this.getHorizontalFacing().getOpposite().rotateY() : this.getHorizontalFacing().rotateY();
   }

   public void onUpdate() {
      if (this.getRollingAmplitude() > 0) {
         this.setRollingAmplitude(this.getRollingAmplitude() - 1);
      }

      if (this.getDamage() > 0.0F) {
         this.setDamage(this.getDamage() - 1.0F);
      }

      if (this.posY < -64.0D) {
         this.kill();
      }

      if (!this.world.isRemote && this.world instanceof WorldServer) {
         this.world.theProfiler.startSection("portal");
         MinecraftServer var1 = this.world.getMinecraftServer();
         int var2 = this.getMaxInPortalTime();
         if (this.inPortal) {
            if (var1.getAllowNether()) {
               if (!this.isRiding() && this.portalCounter++ >= var2) {
                  this.portalCounter = var2;
                  this.timeUntilPortal = this.getPortalCooldown();
                  byte var3;
                  if (this.world.provider.getDimensionType().getId() == -1) {
                     var3 = 0;
                  } else {
                     var3 = -1;
                  }

                  this.changeDimension(var3);
               }

               this.inPortal = false;
            }
         } else {
            if (this.portalCounter > 0) {
               this.portalCounter -= 4;
            }

            if (this.portalCounter < 0) {
               this.portalCounter = 0;
            }
         }

         if (this.timeUntilPortal > 0) {
            --this.timeUntilPortal;
         }

         this.world.theProfiler.endSection();
      }

      if (this.world.isRemote) {
         if (this.turnProgress > 0) {
            double var16 = this.posX + (this.minecartX - this.posX) / (double)this.turnProgress;
            double var19 = this.posY + (this.minecartY - this.posY) / (double)this.turnProgress;
            double var5 = this.posZ + (this.minecartZ - this.posZ) / (double)this.turnProgress;
            double var7 = MathHelper.wrapDegrees(this.minecartYaw - (double)this.rotationYaw);
            this.rotationYaw = (float)((double)this.rotationYaw + var7 / (double)this.turnProgress);
            this.rotationPitch = (float)((double)this.rotationPitch + (this.minecartPitch - (double)this.rotationPitch) / (double)this.turnProgress);
            --this.turnProgress;
            this.setPosition(var16, var19, var5);
            this.setRotation(this.rotationYaw, this.rotationPitch);
         } else {
            this.setPosition(this.posX, this.posY, this.posZ);
            this.setRotation(this.rotationYaw, this.rotationPitch);
         }
      } else {
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         if (!this.hasNoGravity()) {
            this.motionY -= 0.03999999910593033D;
         }

         int var17 = MathHelper.floor(this.posX);
         int var18 = MathHelper.floor(this.posY);
         int var20 = MathHelper.floor(this.posZ);
         if (BlockRailBase.isRailBlock(this.world, new BlockPos(var17, var18 - 1, var20))) {
            --var18;
         }

         BlockPos var4 = new BlockPos(var17, var18, var20);
         IBlockState var21 = this.world.getBlockState(var4);
         if (this.canUseRail() && BlockRailBase.isRailBlock(var21)) {
            this.moveAlongTrack(var4, var21);
            if (var21.getBlock() == Blocks.ACTIVATOR_RAIL) {
               this.onActivatorRailPass(var17, var18, var20, ((Boolean)var21.getValue(BlockRailPowered.POWERED)).booleanValue());
            }
         } else {
            this.moveDerailedMinecart();
         }

         this.doBlockCollisions();
         this.rotationPitch = 0.0F;
         double var6 = this.prevPosX - this.posX;
         double var8 = this.prevPosZ - this.posZ;
         if (var6 * var6 + var8 * var8 > 0.001D) {
            this.rotationYaw = (float)(MathHelper.atan2(var8, var6) * 180.0D / 3.141592653589793D);
            if (this.isInReverse) {
               this.rotationYaw += 180.0F;
            }
         }

         double var10 = (double)MathHelper.wrapDegrees(this.rotationYaw - this.prevRotationYaw);
         if (var10 < -170.0D || var10 >= 170.0D) {
            this.rotationYaw += 180.0F;
            this.isInReverse = !this.isInReverse;
         }

         this.setRotation(this.rotationYaw, this.rotationPitch);
         AxisAlignedBB var12;
         if (getCollisionHandler() != null) {
            var12 = getCollisionHandler().getMinecartCollisionBox(this);
         } else {
            var12 = this.getEntityBoundingBox().expand(0.20000000298023224D, 0.0D, 0.20000000298023224D);
         }

         if (this.canBeRidden() && this.motionX * this.motionX + this.motionZ * this.motionZ > 0.01D) {
            List var22 = this.world.getEntitiesInAABBexcluding(this, var12, EntitySelectors.getTeamCollisionPredicate(this));
            if (!var22.isEmpty()) {
               for(int var23 = 0; var23 < var22.size(); ++var23) {
                  Entity var15 = (Entity)var22.get(var23);
                  if (!(var15 instanceof EntityPlayer) && !(var15 instanceof EntityIronGolem) && !(var15 instanceof EntityMinecart) && !this.isBeingRidden() && !var15.isRiding()) {
                     var15.startRiding(this);
                  } else {
                     var15.applyEntityCollision(this);
                  }
               }
            }
         } else {
            for(Entity var14 : this.world.getEntitiesWithinAABBExcludingEntity(this, var12)) {
               if (!this.isPassenger(var14) && var14.canBePushed() && var14 instanceof EntityMinecart) {
                  var14.applyEntityCollision(this);
               }
            }
         }

         this.handleWaterMovement();
         MinecraftForge.EVENT_BUS.post(new MinecartUpdateEvent(this, this.getCurrentRailPosition()));
      }

   }

   protected double getMaximumSpeed() {
      return 0.4D;
   }

   public void onActivatorRailPass(int var1, int var2, int var3, boolean var4) {
   }

   protected void moveDerailedMinecart() {
      double var1 = this.onGround ? this.getMaximumSpeed() : (double)this.getMaxSpeedAirLateral();
      this.motionX = MathHelper.clamp(this.motionX, -var1, var1);
      this.motionZ = MathHelper.clamp(this.motionZ, -var1, var1);
      double var3 = this.motionY;
      if (this.getMaxSpeedAirVertical() > 0.0F && this.motionY > (double)this.getMaxSpeedAirVertical()) {
         var3 = (double)this.getMaxSpeedAirVertical();
         if (Math.abs(this.motionX) < 0.30000001192092896D && Math.abs(this.motionZ) < 0.30000001192092896D) {
            var3 = 0.15000000596046448D;
            this.motionY = var3;
         }
      }

      if (this.onGround) {
         this.motionX *= 0.5D;
         this.motionY *= 0.5D;
         this.motionZ *= 0.5D;
      }

      this.move(this.motionX, var3, this.motionZ);
      if (!this.onGround) {
         this.motionX *= this.getDragAir();
         this.motionY *= this.getDragAir();
         this.motionZ *= this.getDragAir();
      }

   }

   protected void moveAlongTrack(BlockPos var1, IBlockState var2) {
      this.fallDistance = 0.0F;
      Vec3d var3 = this.getPos(this.posX, this.posY, this.posZ);
      this.posY = (double)var1.getY();
      boolean var4 = false;
      boolean var5 = false;
      BlockRailBase var6 = (BlockRailBase)var2.getBlock();
      if (var6 == Blocks.GOLDEN_RAIL) {
         var4 = ((Boolean)var2.getValue(BlockRailPowered.POWERED)).booleanValue();
         var5 = !var4;
      }

      double var7 = this.getSlopeAdjustment();
      BlockRailBase.EnumRailDirection var9 = var6.getRailDirection(this.world, var1, var2, this);
      switch(var9) {
      case ASCENDING_EAST:
         this.motionX -= var7;
         ++this.posY;
         break;
      case ASCENDING_WEST:
         this.motionX += var7;
         ++this.posY;
         break;
      case ASCENDING_NORTH:
         this.motionZ += var7;
         ++this.posY;
         break;
      case ASCENDING_SOUTH:
         this.motionZ -= var7;
         ++this.posY;
      }

      int[][] var10 = MATRIX[var9.getMetadata()];
      double var11 = (double)(var10[1][0] - var10[0][0]);
      double var13 = (double)(var10[1][2] - var10[0][2]);
      double var15 = Math.sqrt(var11 * var11 + var13 * var13);
      double var17 = this.motionX * var11 + this.motionZ * var13;
      if (var17 < 0.0D) {
         var11 = -var11;
         var13 = -var13;
      }

      double var19 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      if (var19 > 2.0D) {
         var19 = 2.0D;
      }

      this.motionX = var19 * var11 / var15;
      this.motionZ = var19 * var13 / var15;
      Entity var21 = this.getPassengers().isEmpty() ? null : (Entity)this.getPassengers().get(0);
      if (var21 instanceof EntityLivingBase) {
         double var22 = (double)((EntityLivingBase)var21).moveForward;
         if (var22 > 0.0D) {
            double var24 = -Math.sin((double)(var21.rotationYaw * 0.017453292F));
            double var26 = Math.cos((double)(var21.rotationYaw * 0.017453292F));
            double var28 = this.motionX * this.motionX + this.motionZ * this.motionZ;
            if (var28 < 0.01D) {
               this.motionX += var24 * 0.1D;
               this.motionZ += var26 * 0.1D;
               var5 = false;
            }
         }
      }

      if (var5 && this.shouldDoRailFunctions()) {
         double var43 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
         if (var43 < 0.03D) {
            this.motionX *= 0.0D;
            this.motionY *= 0.0D;
            this.motionZ *= 0.0D;
         } else {
            this.motionX *= 0.5D;
            this.motionY *= 0.0D;
            this.motionZ *= 0.5D;
         }
      }

      double var44 = (double)var1.getX() + 0.5D + (double)var10[0][0] * 0.5D;
      double var45 = (double)var1.getZ() + 0.5D + (double)var10[0][2] * 0.5D;
      double var46 = (double)var1.getX() + 0.5D + (double)var10[1][0] * 0.5D;
      double var47 = (double)var1.getZ() + 0.5D + (double)var10[1][2] * 0.5D;
      var11 = var46 - var44;
      var13 = var47 - var45;
      double var30;
      if (var11 == 0.0D) {
         this.posX = (double)var1.getX() + 0.5D;
         var30 = this.posZ - (double)var1.getZ();
      } else if (var13 == 0.0D) {
         this.posZ = (double)var1.getZ() + 0.5D;
         var30 = this.posX - (double)var1.getX();
      } else {
         double var32 = this.posX - var44;
         double var34 = this.posZ - var45;
         var30 = (var32 * var11 + var34 * var13) * 2.0D;
      }

      this.posX = var44 + var11 * var30;
      this.posZ = var45 + var13 * var30;
      this.setPosition(this.posX, this.posY, this.posZ);
      this.moveMinecartOnRail(var1);
      if (var10[0][1] != 0 && MathHelper.floor(this.posX) - var1.getX() == var10[0][0] && MathHelper.floor(this.posZ) - var1.getZ() == var10[0][2]) {
         this.setPosition(this.posX, this.posY + (double)var10[0][1], this.posZ);
      } else if (var10[1][1] != 0 && MathHelper.floor(this.posX) - var1.getX() == var10[1][0] && MathHelper.floor(this.posZ) - var1.getZ() == var10[1][2]) {
         this.setPosition(this.posX, this.posY + (double)var10[1][1], this.posZ);
      }

      this.applyDrag();
      Vec3d var48 = this.getPos(this.posX, this.posY, this.posZ);
      if (var48 != null && var3 != null) {
         double var33 = (var3.yCoord - var48.yCoord) * 0.05D;
         var19 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
         if (var19 > 0.0D) {
            this.motionX = this.motionX / var19 * (var19 + var33);
            this.motionZ = this.motionZ / var19 * (var19 + var33);
         }

         this.setPosition(this.posX, var48.yCoord, this.posZ);
      }

      int var49 = MathHelper.floor(this.posX);
      int var50 = MathHelper.floor(this.posZ);
      if (var49 != var1.getX() || var50 != var1.getZ()) {
         var19 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
         this.motionX = var19 * (double)(var49 - var1.getX());
         this.motionZ = var19 * (double)(var50 - var1.getZ());
      }

      if (this.shouldDoRailFunctions()) {
         ((BlockRailBase)var2.getBlock()).onMinecartPass(this.world, this, var1);
      }

      if (var4 && this.shouldDoRailFunctions()) {
         double var35 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
         if (var35 > 0.01D) {
            double var37 = 0.06D;
            this.motionX += this.motionX / var35 * 0.06D;
            this.motionZ += this.motionZ / var35 * 0.06D;
         } else if (var9 == BlockRailBase.EnumRailDirection.EAST_WEST) {
            if (this.world.getBlockState(var1.west()).isNormalCube()) {
               this.motionX = 0.02D;
            } else if (this.world.getBlockState(var1.east()).isNormalCube()) {
               this.motionX = -0.02D;
            }
         } else if (var9 == BlockRailBase.EnumRailDirection.NORTH_SOUTH) {
            if (this.world.getBlockState(var1.north()).isNormalCube()) {
               this.motionZ = 0.02D;
            } else if (this.world.getBlockState(var1.south()).isNormalCube()) {
               this.motionZ = -0.02D;
            }
         }
      }

   }

   protected void applyDrag() {
      if (this.isBeingRidden()) {
         this.motionX *= 0.996999979019165D;
         this.motionY *= 0.0D;
         this.motionZ *= 0.996999979019165D;
      } else {
         this.motionX *= 0.9599999785423279D;
         this.motionY *= 0.0D;
         this.motionZ *= 0.9599999785423279D;
      }

   }

   public void setPosition(double var1, double var3, double var5) {
      this.posX = var1;
      this.posY = var3;
      this.posZ = var5;
      float var7 = this.width / 2.0F;
      float var8 = this.height;
      this.setEntityBoundingBox(new AxisAlignedBB(var1 - (double)var7, var3, var5 - (double)var7, var1 + (double)var7, var3 + (double)var8, var5 + (double)var7));
   }

   @SideOnly(Side.CLIENT)
   public Vec3d getPosOffset(double var1, double var3, double var5, double var7) {
      int var9 = MathHelper.floor(var1);
      int var10 = MathHelper.floor(var3);
      int var11 = MathHelper.floor(var5);
      if (BlockRailBase.isRailBlock(this.world, new BlockPos(var9, var10 - 1, var11))) {
         --var10;
      }

      IBlockState var12 = this.world.getBlockState(new BlockPos(var9, var10, var11));
      if (BlockRailBase.isRailBlock(var12)) {
         BlockRailBase.EnumRailDirection var13 = (BlockRailBase.EnumRailDirection)var12.getValue(((BlockRailBase)var12.getBlock()).getShapeProperty());
         var3 = (double)var10;
         if (var13.isAscending()) {
            var3 = (double)(var10 + 1);
         }

         int[][] var14 = MATRIX[var13.getMetadata()];
         double var15 = (double)(var14[1][0] - var14[0][0]);
         double var17 = (double)(var14[1][2] - var14[0][2]);
         double var19 = Math.sqrt(var15 * var15 + var17 * var17);
         var15 = var15 / var19;
         var17 = var17 / var19;
         var1 = var1 + var15 * var7;
         var5 = var5 + var17 * var7;
         if (var14[0][1] != 0 && MathHelper.floor(var1) - var9 == var14[0][0] && MathHelper.floor(var5) - var11 == var14[0][2]) {
            var3 += (double)var14[0][1];
         } else if (var14[1][1] != 0 && MathHelper.floor(var1) - var9 == var14[1][0] && MathHelper.floor(var5) - var11 == var14[1][2]) {
            var3 += (double)var14[1][1];
         }

         return this.getPos(var1, var3, var5);
      } else {
         return null;
      }
   }

   public Vec3d getPos(double var1, double var3, double var5) {
      int var7 = MathHelper.floor(var1);
      int var8 = MathHelper.floor(var3);
      int var9 = MathHelper.floor(var5);
      if (BlockRailBase.isRailBlock(this.world, new BlockPos(var7, var8 - 1, var9))) {
         --var8;
      }

      IBlockState var10 = this.world.getBlockState(new BlockPos(var7, var8, var9));
      if (BlockRailBase.isRailBlock(var10)) {
         BlockRailBase.EnumRailDirection var11 = (BlockRailBase.EnumRailDirection)var10.getValue(((BlockRailBase)var10.getBlock()).getShapeProperty());
         int[][] var12 = MATRIX[var11.getMetadata()];
         double var13 = (double)var7 + 0.5D + (double)var12[0][0] * 0.5D;
         double var15 = (double)var8 + 0.0625D + (double)var12[0][1] * 0.5D;
         double var17 = (double)var9 + 0.5D + (double)var12[0][2] * 0.5D;
         double var19 = (double)var7 + 0.5D + (double)var12[1][0] * 0.5D;
         double var21 = (double)var8 + 0.0625D + (double)var12[1][1] * 0.5D;
         double var23 = (double)var9 + 0.5D + (double)var12[1][2] * 0.5D;
         double var25 = var19 - var13;
         double var27 = (var21 - var15) * 2.0D;
         double var29 = var23 - var17;
         double var31;
         if (var25 == 0.0D) {
            var31 = var5 - (double)var9;
         } else if (var29 == 0.0D) {
            var31 = var1 - (double)var7;
         } else {
            double var33 = var1 - var13;
            double var35 = var5 - var17;
            var31 = (var33 * var25 + var35 * var29) * 2.0D;
         }

         var1 = var13 + var25 * var31;
         var3 = var15 + var27 * var31;
         var5 = var17 + var29 * var31;
         if (var27 < 0.0D) {
            ++var3;
         }

         if (var27 > 0.0D) {
            var3 += 0.5D;
         }

         return new Vec3d(var1, var3, var5);
      } else {
         return null;
      }
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getRenderBoundingBox() {
      AxisAlignedBB var1 = this.getEntityBoundingBox();
      return this.hasDisplayTile() ? var1.expandXyz((double)Math.abs(this.getDisplayTileOffset()) / 16.0D) : var1;
   }

   public static void registerFixesMinecart(DataFixer var0, String var1) {
   }

   protected void readEntityFromNBT(NBTTagCompound var1) {
      if (var1.getBoolean("CustomDisplayTile")) {
         Block var2;
         if (var1.hasKey("DisplayTile", 8)) {
            var2 = Block.getBlockFromName(var1.getString("DisplayTile"));
         } else {
            var2 = Block.getBlockById(var1.getInteger("DisplayTile"));
         }

         int var3 = var1.getInteger("DisplayData");
         this.setDisplayTile(var2 == null ? Blocks.AIR.getDefaultState() : var2.getStateFromMeta(var3));
         this.setDisplayTileOffset(var1.getInteger("DisplayOffset"));
      }

   }

   protected void writeEntityToNBT(NBTTagCompound var1) {
      if (this.hasDisplayTile()) {
         var1.setBoolean("CustomDisplayTile", true);
         IBlockState var2 = this.getDisplayTile();
         ResourceLocation var3 = (ResourceLocation)Block.REGISTRY.getNameForObject(var2.getBlock());
         var1.setString("DisplayTile", var3 == null ? "" : var3.toString());
         var1.setInteger("DisplayData", var2.getBlock().getMetaFromState(var2));
         var1.setInteger("DisplayOffset", this.getDisplayTileOffset());
      }

   }

   public void applyEntityCollision(Entity var1) {
      MinecraftForge.EVENT_BUS.post(new MinecartCollisionEvent(this, var1));
      if (getCollisionHandler() != null) {
         getCollisionHandler().onEntityCollision(this, var1);
      } else {
         if (!this.world.isRemote && !var1.noClip && !this.noClip && !this.isPassenger(var1)) {
            double var2 = var1.posX - this.posX;
            double var4 = var1.posZ - this.posZ;
            double var6 = var2 * var2 + var4 * var4;
            if (var6 >= 9.999999747378752E-5D) {
               var6 = (double)MathHelper.sqrt(var6);
               var2 = var2 / var6;
               var4 = var4 / var6;
               double var8 = 1.0D / var6;
               if (var8 > 1.0D) {
                  var8 = 1.0D;
               }

               var2 = var2 * var8;
               var4 = var4 * var8;
               var2 = var2 * 0.10000000149011612D;
               var4 = var4 * 0.10000000149011612D;
               var2 = var2 * (double)(1.0F - this.entityCollisionReduction);
               var4 = var4 * (double)(1.0F - this.entityCollisionReduction);
               var2 = var2 * 0.5D;
               var4 = var4 * 0.5D;
               if (var1 instanceof EntityMinecart) {
                  double var10 = var1.posX - this.posX;
                  double var12 = var1.posZ - this.posZ;
                  Vec3d var14 = (new Vec3d(var10, 0.0D, var12)).normalize();
                  Vec3d var15 = (new Vec3d((double)MathHelper.cos(this.rotationYaw * 0.017453292F), 0.0D, (double)MathHelper.sin(this.rotationYaw * 0.017453292F))).normalize();
                  double var16 = Math.abs(var14.dotProduct(var15));
                  if (var16 < 0.800000011920929D) {
                     return;
                  }

                  double var18 = var1.motionX + this.motionX;
                  double var20 = var1.motionZ + this.motionZ;
                  if (((EntityMinecart)var1).isPoweredCart() && !this.isPoweredCart()) {
                     this.motionX *= 0.20000000298023224D;
                     this.motionZ *= 0.20000000298023224D;
                     this.addVelocity(var1.motionX - var2, 0.0D, var1.motionZ - var4);
                     var1.motionX *= 0.949999988079071D;
                     var1.motionZ *= 0.949999988079071D;
                  } else if (!((EntityMinecart)var1).isPoweredCart() && this.isPoweredCart()) {
                     var1.motionX *= 0.20000000298023224D;
                     var1.motionZ *= 0.20000000298023224D;
                     var1.addVelocity(this.motionX + var2, 0.0D, this.motionZ + var4);
                     this.motionX *= 0.949999988079071D;
                     this.motionZ *= 0.949999988079071D;
                  } else {
                     var18 = var18 / 2.0D;
                     var20 = var20 / 2.0D;
                     this.motionX *= 0.20000000298023224D;
                     this.motionZ *= 0.20000000298023224D;
                     this.addVelocity(var18 - var2, 0.0D, var20 - var4);
                     var1.motionX *= 0.20000000298023224D;
                     var1.motionZ *= 0.20000000298023224D;
                     var1.addVelocity(var18 + var2, 0.0D, var20 + var4);
                  }
               } else {
                  this.addVelocity(-var2, 0.0D, -var4);
                  var1.addVelocity(var2 / 4.0D, 0.0D, var4 / 4.0D);
               }
            }
         }

      }
   }

   @SideOnly(Side.CLIENT)
   public void setPositionAndRotationDirect(double var1, double var3, double var5, float var7, float var8, int var9, boolean var10) {
      this.minecartX = var1;
      this.minecartY = var3;
      this.minecartZ = var5;
      this.minecartYaw = (double)var7;
      this.minecartPitch = (double)var8;
      this.turnProgress = var9 + 2;
      this.motionX = this.velocityX;
      this.motionY = this.velocityY;
      this.motionZ = this.velocityZ;
   }

   public void setDamage(float var1) {
      this.dataManager.set(DAMAGE, Float.valueOf(var1));
   }

   @SideOnly(Side.CLIENT)
   public void setVelocity(double var1, double var3, double var5) {
      this.motionX = var1;
      this.motionY = var3;
      this.motionZ = var5;
      this.velocityX = this.motionX;
      this.velocityY = this.motionY;
      this.velocityZ = this.motionZ;
   }

   public float getDamage() {
      return ((Float)this.dataManager.get(DAMAGE)).floatValue();
   }

   public void setRollingAmplitude(int var1) {
      this.dataManager.set(ROLLING_AMPLITUDE, Integer.valueOf(var1));
   }

   public int getRollingAmplitude() {
      return ((Integer)this.dataManager.get(ROLLING_AMPLITUDE)).intValue();
   }

   public void setRollingDirection(int var1) {
      this.dataManager.set(ROLLING_DIRECTION, Integer.valueOf(var1));
   }

   public int getRollingDirection() {
      return ((Integer)this.dataManager.get(ROLLING_DIRECTION)).intValue();
   }

   public abstract EntityMinecart.Type getType();

   public IBlockState getDisplayTile() {
      return !this.hasDisplayTile() ? this.getDefaultDisplayTile() : Block.getStateById(((Integer)this.getDataManager().get(DISPLAY_TILE)).intValue());
   }

   public IBlockState getDefaultDisplayTile() {
      return Blocks.AIR.getDefaultState();
   }

   public int getDisplayTileOffset() {
      return !this.hasDisplayTile() ? this.getDefaultDisplayTileOffset() : ((Integer)this.getDataManager().get(DISPLAY_TILE_OFFSET)).intValue();
   }

   public int getDefaultDisplayTileOffset() {
      return 6;
   }

   public void setDisplayTile(IBlockState var1) {
      this.getDataManager().set(DISPLAY_TILE, Integer.valueOf(Block.getStateId(var1)));
      this.setHasDisplayTile(true);
   }

   public void setDisplayTileOffset(int var1) {
      this.getDataManager().set(DISPLAY_TILE_OFFSET, Integer.valueOf(var1));
      this.setHasDisplayTile(true);
   }

   public boolean hasDisplayTile() {
      return ((Boolean)this.getDataManager().get(SHOW_BLOCK)).booleanValue();
   }

   public void setHasDisplayTile(boolean var1) {
      this.getDataManager().set(SHOW_BLOCK, Boolean.valueOf(var1));
   }

   private BlockPos getCurrentRailPosition() {
      int var1 = MathHelper.floor(this.posX);
      int var2 = MathHelper.floor(this.posY);
      int var3 = MathHelper.floor(this.posZ);
      if (BlockRailBase.isRailBlock(this.world, new BlockPos(var1, var2 - 1, var3))) {
         --var2;
      }

      return new BlockPos(var1, var2, var3);
   }

   protected double getMaxSpeed() {
      if (!this.canUseRail()) {
         return this.getMaximumSpeed();
      } else {
         BlockPos var1 = this.getCurrentRailPosition();
         IBlockState var2 = this.world.getBlockState(var1);
         if (!BlockRailBase.isRailBlock(var2)) {
            return this.getMaximumSpeed();
         } else {
            float var3 = ((BlockRailBase)var2.getBlock()).getRailMaxSpeed(this.world, this, var1);
            return (double)Math.min(var3, this.getCurrentCartSpeedCapOnRail());
         }
      }
   }

   public void moveMinecartOnRail(BlockPos var1) {
      double var2 = this.motionX;
      double var4 = this.motionZ;
      if (this.isBeingRidden()) {
         var2 *= 0.75D;
         var4 *= 0.75D;
      }

      double var6 = this.getMaxSpeed();
      var2 = MathHelper.clamp(var2, -var6, var6);
      var4 = MathHelper.clamp(var4, -var6, var6);
      this.move(var2, 0.0D, var4);
   }

   public static IMinecartCollisionHandler getCollisionHandler() {
      return collisionHandler;
   }

   public static void setCollisionHandler(IMinecartCollisionHandler var0) {
      collisionHandler = var0;
   }

   public ItemStack getCartItem() {
      if (this instanceof EntityMinecartFurnace) {
         return new ItemStack(Items.FURNACE_MINECART);
      } else if (this instanceof EntityMinecartChest) {
         return new ItemStack(Items.CHEST_MINECART);
      } else if (this instanceof EntityMinecartTNT) {
         return new ItemStack(Items.TNT_MINECART);
      } else if (this instanceof EntityMinecartHopper) {
         return new ItemStack(Items.HOPPER_MINECART);
      } else {
         return this instanceof EntityMinecartCommandBlock ? new ItemStack(Items.COMMAND_BLOCK_MINECART) : new ItemStack(Items.MINECART);
      }
   }

   public boolean canUseRail() {
      return this.canUseRail;
   }

   public void setCanUseRail(boolean var1) {
      this.canUseRail = var1;
   }

   public boolean shouldDoRailFunctions() {
      return true;
   }

   public boolean isPoweredCart() {
      return this.getType() == EntityMinecart.Type.FURNACE;
   }

   public boolean canBeRidden() {
      return this.getType() == EntityMinecart.Type.RIDEABLE;
   }

   public float getMaxCartSpeedOnRail() {
      return 1.2F;
   }

   public final float getCurrentCartSpeedCapOnRail() {
      return this.currentSpeedRail;
   }

   public final void setCurrentCartSpeedCapOnRail(float var1) {
      var1 = Math.min(var1, this.getMaxCartSpeedOnRail());
      this.currentSpeedRail = var1;
   }

   public float getMaxSpeedAirLateral() {
      return this.maxSpeedAirLateral;
   }

   public void setMaxSpeedAirLateral(float var1) {
      this.maxSpeedAirLateral = var1;
   }

   public float getMaxSpeedAirVertical() {
      return this.maxSpeedAirVertical;
   }

   public void setMaxSpeedAirVertical(float var1) {
      this.maxSpeedAirVertical = var1;
   }

   public double getDragAir() {
      return this.dragAir;
   }

   public void setDragAir(double var1) {
      this.dragAir = var1;
   }

   public double getSlopeAdjustment() {
      return 0.0078125D;
   }

   public int getComparatorLevel() {
      return -1;
   }

   public static enum Type {
      RIDEABLE(0, "MinecartRideable"),
      CHEST(1, "MinecartChest"),
      FURNACE(2, "MinecartFurnace"),
      TNT(3, "MinecartTNT"),
      SPAWNER(4, "MinecartSpawner"),
      HOPPER(5, "MinecartHopper"),
      COMMAND_BLOCK(6, "MinecartCommandBlock");

      private static final Map BY_ID = Maps.newHashMap();
      private final int id;
      private final String name;

      private Type(int var3, String var4) {
         this.id = var3;
         this.name = var4;
      }

      public int getId() {
         return this.id;
      }

      public String getName() {
         return this.name;
      }

      @SideOnly(Side.CLIENT)
      public static EntityMinecart.Type getById(int var0) {
         EntityMinecart.Type var1 = (EntityMinecart.Type)BY_ID.get(Integer.valueOf(var0));
         return var1 == null ? RIDEABLE : var1;
      }

      static {
         for(EntityMinecart.Type var3 : values()) {
            BY_ID.put(Integer.valueOf(var3.getId()), var3);
         }

      }
   }
}
