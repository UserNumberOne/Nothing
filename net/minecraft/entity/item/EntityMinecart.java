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
import net.minecraft.src.MinecraftServer;
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
import org.bukkit.Location;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.util.Vector;

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
   public boolean slowWhenEmpty;
   private double derailedX;
   private double derailedY;
   private double derailedZ;
   private double flyingX;
   private double flyingY;
   private double flyingZ;
   public double maxSpeed;

   public EntityMinecart(World world) {
      super(world);
      this.slowWhenEmpty = true;
      this.derailedX = 0.5D;
      this.derailedY = 0.5D;
      this.derailedZ = 0.5D;
      this.flyingX = 0.95D;
      this.flyingY = 0.95D;
      this.flyingZ = 0.95D;
      this.maxSpeed = 0.4D;
      this.preventEntitySpawning = true;
      this.setSize(0.98F, 0.7F);
   }

   public static EntityMinecart create(World world, double d0, double d1, double d2, EntityMinecart.Type entityminecartabstract_enumminecarttype) {
      switch(EntityMinecart.SyntheticClass_1.a[entityminecartabstract_enumminecarttype.ordinal()]) {
      case 1:
         return new EntityMinecartChest(world, d0, d1, d2);
      case 2:
         return new EntityMinecartFurnace(world, d0, d1, d2);
      case 3:
         return new EntityMinecartTNT(world, d0, d1, d2);
      case 4:
         return new EntityMinecartMobSpawner(world, d0, d1, d2);
      case 5:
         return new EntityMinecartHopper(world, d0, d1, d2);
      case 6:
         return new EntityMinecartCommandBlock(world, d0, d1, d2);
      default:
         return new EntityMinecartEmpty(world, d0, d1, d2);
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
   public AxisAlignedBB getCollisionBox(Entity entity) {
      return entity.canBePushed() ? entity.getEntityBoundingBox() : null;
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox() {
      return null;
   }

   public boolean canBePushed() {
      return true;
   }

   public EntityMinecart(World world, double d0, double d1, double d2) {
      this(world);
      this.setPosition(d0, d1, d2);
      this.motionX = 0.0D;
      this.motionY = 0.0D;
      this.motionZ = 0.0D;
      this.prevPosX = d0;
      this.prevPosY = d1;
      this.prevPosZ = d2;
      this.world.getServer().getPluginManager().callEvent(new VehicleCreateEvent((Vehicle)this.getBukkitEntity()));
   }

   public double getMountedYOffset() {
      return 0.0D;
   }

   public boolean attackEntityFrom(DamageSource damagesource, float f) {
      if (!this.world.isRemote && !this.isDead) {
         if (this.isEntityInvulnerable(damagesource)) {
            return false;
         } else {
            Vehicle vehicle = (Vehicle)this.getBukkitEntity();
            org.bukkit.entity.Entity passenger = damagesource.getEntity() == null ? null : damagesource.getEntity().getBukkitEntity();
            VehicleDamageEvent event = new VehicleDamageEvent(vehicle, passenger, (double)f);
            this.world.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
               return true;
            } else {
               f = (float)event.getDamage();
               this.setRollingDirection(-this.getRollingDirection());
               this.setRollingAmplitude(10);
               this.setBeenAttacked();
               this.setDamage(this.getDamage() + f * 10.0F);
               boolean flag = damagesource.getEntity() instanceof EntityPlayer && ((EntityPlayer)damagesource.getEntity()).capabilities.isCreativeMode;
               if (flag || this.getDamage() > 40.0F) {
                  VehicleDestroyEvent destroyEvent = new VehicleDestroyEvent(vehicle, passenger);
                  this.world.getServer().getPluginManager().callEvent(destroyEvent);
                  if (destroyEvent.isCancelled()) {
                     this.setDamage(40.0F);
                     return true;
                  }

                  this.removePassengers();
                  if (flag && !this.hasCustomName()) {
                     this.setDead();
                  } else {
                     this.killMinecart(damagesource);
                  }
               }

               return true;
            }
         }
      } else {
         return true;
      }
   }

   public void killMinecart(DamageSource damagesource) {
      this.setDead();
      if (this.world.getGameRules().getBoolean("doEntityDrops")) {
         ItemStack itemstack = new ItemStack(Items.MINECART, 1);
         if (this.getName() != null) {
            itemstack.setStackDisplayName(this.getName());
         }

         this.entityDropItem(itemstack, 0.0F);
      }

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
      double prevX = this.posX;
      double prevY = this.posY;
      double prevZ = this.posZ;
      float prevYaw = this.rotationYaw;
      float prevPitch = this.rotationPitch;
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
         MinecraftServer minecraftserver = this.world.getMinecraftServer();
         int i = this.getMaxInPortalTime();
         if (this.inPortal) {
            if (!this.isRiding() && this.portalCounter++ >= i) {
               this.portalCounter = i;
               this.timeUntilPortal = this.getPortalCooldown();
               byte b0;
               if (this.world.provider.getDimensionType().getId() == -1) {
                  b0 = 0;
               } else {
                  b0 = -1;
               }

               this.changeDimension(b0);
            }

            this.inPortal = false;
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
            double d0 = this.posX + (this.minecartX - this.posX) / (double)this.turnProgress;
            double d1 = this.posY + (this.minecartY - this.posY) / (double)this.turnProgress;
            double d2 = this.posZ + (this.minecartZ - this.posZ) / (double)this.turnProgress;
            double d3 = MathHelper.wrapDegrees(this.minecartYaw - (double)this.rotationYaw);
            this.rotationYaw = (float)((double)this.rotationYaw + d3 / (double)this.turnProgress);
            this.rotationPitch = (float)((double)this.rotationPitch + (this.minecartPitch - (double)this.rotationPitch) / (double)this.turnProgress);
            --this.turnProgress;
            this.setPosition(d0, d1, d2);
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

         int j = MathHelper.floor(this.posX);
         int i = MathHelper.floor(this.posY);
         int k = MathHelper.floor(this.posZ);
         if (BlockRailBase.isRailBlock(this.world, new BlockPos(j, i - 1, k))) {
            --i;
         }

         BlockPos blockposition = new BlockPos(j, i, k);
         IBlockState iblockdata = this.world.getBlockState(blockposition);
         if (BlockRailBase.isRailBlock(iblockdata)) {
            this.moveAlongTrack(blockposition, iblockdata);
            if (iblockdata.getBlock() == Blocks.ACTIVATOR_RAIL) {
               this.onActivatorRailPass(j, i, k, ((Boolean)iblockdata.getValue(BlockRailPowered.POWERED)).booleanValue());
            }
         } else {
            this.moveDerailedMinecart();
         }

         this.doBlockCollisions();
         this.rotationPitch = 0.0F;
         double d4 = this.prevPosX - this.posX;
         double d5 = this.prevPosZ - this.posZ;
         if (d4 * d4 + d5 * d5 > 0.001D) {
            this.rotationYaw = (float)(MathHelper.atan2(d5, d4) * 180.0D / 3.141592653589793D);
            if (this.isInReverse) {
               this.rotationYaw += 180.0F;
            }
         }

         double d6 = (double)MathHelper.wrapDegrees(this.rotationYaw - this.prevRotationYaw);
         if (d6 < -170.0D || d6 >= 170.0D) {
            this.rotationYaw += 180.0F;
            this.isInReverse = !this.isInReverse;
         }

         this.setRotation(this.rotationYaw, this.rotationPitch);
         org.bukkit.World bworld = this.world.getWorld();
         Location from = new Location(bworld, prevX, prevY, prevZ, prevYaw, prevPitch);
         Location to = new Location(bworld, this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
         Vehicle vehicle = (Vehicle)this.getBukkitEntity();
         this.world.getServer().getPluginManager().callEvent(new VehicleUpdateEvent(vehicle));
         if (!from.equals(to)) {
            this.world.getServer().getPluginManager().callEvent(new VehicleMoveEvent(vehicle, from, to));
         }

         if (this.getType() == EntityMinecart.Type.RIDEABLE && this.motionX * this.motionX + this.motionZ * this.motionZ > 0.01D) {
            List list = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox().expand(0.20000000298023224D, 0.0D, 0.20000000298023224D), EntitySelectors.getTeamCollisionPredicate(this));
            if (!list.isEmpty()) {
               for(int l = 0; l < list.size(); ++l) {
                  Entity entity = (Entity)list.get(l);
                  if (!(entity instanceof EntityPlayer) && !(entity instanceof EntityIronGolem) && !(entity instanceof EntityMinecart) && !this.isBeingRidden() && !entity.isRiding()) {
                     VehicleEntityCollisionEvent collisionEvent = new VehicleEntityCollisionEvent(vehicle, entity.getBukkitEntity());
                     this.world.getServer().getPluginManager().callEvent(collisionEvent);
                     if (!collisionEvent.isCancelled()) {
                        entity.startRiding(this);
                     }
                  } else {
                     VehicleEntityCollisionEvent collisionEvent = new VehicleEntityCollisionEvent(vehicle, entity.getBukkitEntity());
                     this.world.getServer().getPluginManager().callEvent(collisionEvent);
                     if (!collisionEvent.isCancelled()) {
                        entity.applyEntityCollision(this);
                     }
                  }
               }
            }
         } else {
            for(Entity entity1 : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(0.20000000298023224D, 0.0D, 0.20000000298023224D))) {
               if (!this.isPassenger(entity1) && entity1.canBePushed() && entity1 instanceof EntityMinecart) {
                  VehicleEntityCollisionEvent collisionEvent = new VehicleEntityCollisionEvent(vehicle, entity1.getBukkitEntity());
                  this.world.getServer().getPluginManager().callEvent(collisionEvent);
                  if (!collisionEvent.isCancelled()) {
                     entity1.applyEntityCollision(this);
                  }
               }
            }
         }

         this.handleWaterMovement();
      }

   }

   protected double getMaximumSpeed() {
      return this.maxSpeed;
   }

   public void onActivatorRailPass(int i, int j, int k, boolean flag) {
   }

   protected void moveDerailedMinecart() {
      double d0 = this.getMaximumSpeed();
      this.motionX = MathHelper.clamp(this.motionX, -d0, d0);
      this.motionZ = MathHelper.clamp(this.motionZ, -d0, d0);
      if (this.onGround) {
         this.motionX *= this.derailedX;
         this.motionY *= this.derailedY;
         this.motionZ *= this.derailedZ;
      }

      this.move(this.motionX, this.motionY, this.motionZ);
      if (!this.onGround) {
         this.motionX *= this.flyingX;
         this.motionY *= this.flyingY;
         this.motionZ *= this.flyingZ;
      }

   }

   protected void moveAlongTrack(BlockPos blockposition, IBlockState iblockdata) {
      this.fallDistance = 0.0F;
      Vec3d vec3d = this.getPos(this.posX, this.posY, this.posZ);
      this.posY = (double)blockposition.getY();
      boolean flag = false;
      boolean flag1 = false;
      BlockRailBase blockminecarttrackabstract = (BlockRailBase)iblockdata.getBlock();
      if (blockminecarttrackabstract == Blocks.GOLDEN_RAIL) {
         flag = ((Boolean)iblockdata.getValue(BlockRailPowered.POWERED)).booleanValue();
         flag1 = !flag;
      }

      BlockRailBase.EnumRailDirection blockminecarttrackabstract_enumtrackposition = (BlockRailBase.EnumRailDirection)iblockdata.getValue(blockminecarttrackabstract.getShapeProperty());
      switch(EntityMinecart.SyntheticClass_1.b[blockminecarttrackabstract_enumtrackposition.ordinal()]) {
      case 1:
         this.motionX -= 0.0078125D;
         ++this.posY;
         break;
      case 2:
         this.motionX += 0.0078125D;
         ++this.posY;
         break;
      case 3:
         this.motionZ += 0.0078125D;
         ++this.posY;
         break;
      case 4:
         this.motionZ -= 0.0078125D;
         ++this.posY;
      }

      int[][] aint = MATRIX[blockminecarttrackabstract_enumtrackposition.getMetadata()];
      double d1 = (double)(aint[1][0] - aint[0][0]);
      double d2 = (double)(aint[1][2] - aint[0][2]);
      double d3 = Math.sqrt(d1 * d1 + d2 * d2);
      double d4 = this.motionX * d1 + this.motionZ * d2;
      if (d4 < 0.0D) {
         d1 = -d1;
         d2 = -d2;
      }

      double d5 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      if (d5 > 2.0D) {
         d5 = 2.0D;
      }

      this.motionX = d5 * d1 / d3;
      this.motionZ = d5 * d2 / d3;
      Entity entity = this.getPassengers().isEmpty() ? null : (Entity)this.getPassengers().get(0);
      if (entity instanceof EntityLivingBase) {
         double d6 = (double)((EntityLivingBase)entity).moveForward;
         if (d6 > 0.0D) {
            double d7 = -Math.sin((double)(entity.rotationYaw * 0.017453292F));
            double d8 = Math.cos((double)(entity.rotationYaw * 0.017453292F));
            double d9 = this.motionX * this.motionX + this.motionZ * this.motionZ;
            if (d9 < 0.01D) {
               this.motionX += d7 * 0.1D;
               this.motionZ += d8 * 0.1D;
               flag1 = false;
            }
         }
      }

      if (flag1) {
         double d6 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
         if (d6 < 0.03D) {
            this.motionX *= 0.0D;
            this.motionY *= 0.0D;
            this.motionZ *= 0.0D;
         } else {
            this.motionX *= 0.5D;
            this.motionY *= 0.0D;
            this.motionZ *= 0.5D;
         }
      }

      double d6 = (double)blockposition.getX() + 0.5D + (double)aint[0][0] * 0.5D;
      double d7 = (double)blockposition.getZ() + 0.5D + (double)aint[0][2] * 0.5D;
      double d8 = (double)blockposition.getX() + 0.5D + (double)aint[1][0] * 0.5D;
      double d9 = (double)blockposition.getZ() + 0.5D + (double)aint[1][2] * 0.5D;
      d1 = d8 - d6;
      d2 = d9 - d7;
      double d10;
      if (d1 == 0.0D) {
         this.posX = (double)blockposition.getX() + 0.5D;
         d10 = this.posZ - (double)blockposition.getZ();
      } else if (d2 == 0.0D) {
         this.posZ = (double)blockposition.getZ() + 0.5D;
         d10 = this.posX - (double)blockposition.getX();
      } else {
         double d11 = this.posX - d6;
         double d12 = this.posZ - d7;
         d10 = (d11 * d1 + d12 * d2) * 2.0D;
      }

      this.posX = d6 + d1 * d10;
      this.posZ = d7 + d2 * d10;
      this.setPosition(this.posX, this.posY, this.posZ);
      double d11 = this.motionX;
      double d12 = this.motionZ;
      if (this.isBeingRidden()) {
         d11 *= 0.75D;
         d12 *= 0.75D;
      }

      double d13 = this.getMaximumSpeed();
      d11 = MathHelper.clamp(d11, -d13, d13);
      d12 = MathHelper.clamp(d12, -d13, d13);
      this.move(d11, 0.0D, d12);
      if (aint[0][1] != 0 && MathHelper.floor(this.posX) - blockposition.getX() == aint[0][0] && MathHelper.floor(this.posZ) - blockposition.getZ() == aint[0][2]) {
         this.setPosition(this.posX, this.posY + (double)aint[0][1], this.posZ);
      } else if (aint[1][1] != 0 && MathHelper.floor(this.posX) - blockposition.getX() == aint[1][0] && MathHelper.floor(this.posZ) - blockposition.getZ() == aint[1][2]) {
         this.setPosition(this.posX, this.posY + (double)aint[1][1], this.posZ);
      }

      this.applyDrag();
      Vec3d vec3d1 = this.getPos(this.posX, this.posY, this.posZ);
      if (vec3d1 != null && vec3d != null) {
         double d14 = (vec3d.yCoord - vec3d1.yCoord) * 0.05D;
         d5 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
         if (d5 > 0.0D) {
            this.motionX = this.motionX / d5 * (d5 + d14);
            this.motionZ = this.motionZ / d5 * (d5 + d14);
         }

         this.setPosition(this.posX, vec3d1.yCoord, this.posZ);
      }

      int i = MathHelper.floor(this.posX);
      int j = MathHelper.floor(this.posZ);
      if (i != blockposition.getX() || j != blockposition.getZ()) {
         d5 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
         this.motionX = d5 * (double)(i - blockposition.getX());
         this.motionZ = d5 * (double)(j - blockposition.getZ());
      }

      if (flag) {
         double d15 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
         if (d15 > 0.01D) {
            this.motionX += this.motionX / d15 * 0.06D;
            this.motionZ += this.motionZ / d15 * 0.06D;
         } else if (blockminecarttrackabstract_enumtrackposition == BlockRailBase.EnumRailDirection.EAST_WEST) {
            if (this.world.getBlockState(blockposition.west()).isNormalCube()) {
               this.motionX = 0.02D;
            } else if (this.world.getBlockState(blockposition.east()).isNormalCube()) {
               this.motionX = -0.02D;
            }
         } else if (blockminecarttrackabstract_enumtrackposition == BlockRailBase.EnumRailDirection.NORTH_SOUTH) {
            if (this.world.getBlockState(blockposition.north()).isNormalCube()) {
               this.motionZ = 0.02D;
            } else if (this.world.getBlockState(blockposition.south()).isNormalCube()) {
               this.motionZ = -0.02D;
            }
         }
      }

   }

   protected void applyDrag() {
      if (!this.isBeingRidden() && this.slowWhenEmpty) {
         this.motionX *= 0.9599999785423279D;
         this.motionY *= 0.0D;
         this.motionZ *= 0.9599999785423279D;
      } else {
         this.motionX *= 0.996999979019165D;
         this.motionY *= 0.0D;
         this.motionZ *= 0.996999979019165D;
      }

   }

   public void setPosition(double d0, double d1, double d2) {
      this.posX = d0;
      this.posY = d1;
      this.posZ = d2;
      float f = this.width / 2.0F;
      float f1 = this.height;
      this.setEntityBoundingBox(new AxisAlignedBB(d0 - (double)f, d1, d2 - (double)f, d0 + (double)f, d1 + (double)f1, d2 + (double)f));
   }

   public Vec3d getPos(double d0, double d1, double d2) {
      int i = MathHelper.floor(d0);
      int j = MathHelper.floor(d1);
      int k = MathHelper.floor(d2);
      if (BlockRailBase.isRailBlock(this.world, new BlockPos(i, j - 1, k))) {
         --j;
      }

      IBlockState iblockdata = this.world.getBlockState(new BlockPos(i, j, k));
      if (BlockRailBase.isRailBlock(iblockdata)) {
         BlockRailBase.EnumRailDirection blockminecarttrackabstract_enumtrackposition = (BlockRailBase.EnumRailDirection)iblockdata.getValue(((BlockRailBase)iblockdata.getBlock()).getShapeProperty());
         int[][] aint = MATRIX[blockminecarttrackabstract_enumtrackposition.getMetadata()];
         double d3 = (double)i + 0.5D + (double)aint[0][0] * 0.5D;
         double d4 = (double)j + 0.0625D + (double)aint[0][1] * 0.5D;
         double d5 = (double)k + 0.5D + (double)aint[0][2] * 0.5D;
         double d6 = (double)i + 0.5D + (double)aint[1][0] * 0.5D;
         double d7 = (double)j + 0.0625D + (double)aint[1][1] * 0.5D;
         double d8 = (double)k + 0.5D + (double)aint[1][2] * 0.5D;
         double d9 = d6 - d3;
         double d10 = (d7 - d4) * 2.0D;
         double d11 = d8 - d5;
         double d12;
         if (d9 == 0.0D) {
            d12 = d2 - (double)k;
         } else if (d11 == 0.0D) {
            d12 = d0 - (double)i;
         } else {
            double d13 = d0 - d3;
            double d14 = d2 - d5;
            d12 = (d13 * d9 + d14 * d11) * 2.0D;
         }

         d0 = d3 + d9 * d12;
         d1 = d4 + d10 * d12;
         d2 = d5 + d11 * d12;
         if (d10 < 0.0D) {
            ++d1;
         }

         if (d10 > 0.0D) {
            d1 += 0.5D;
         }

         return new Vec3d(d0, d1, d2);
      } else {
         return null;
      }
   }

   public static void registerFixesMinecart(DataFixer dataconvertermanager, String s) {
   }

   protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      if (nbttagcompound.getBoolean("CustomDisplayTile")) {
         Block block;
         if (nbttagcompound.hasKey("DisplayTile", 8)) {
            block = Block.getBlockFromName(nbttagcompound.getString("DisplayTile"));
         } else {
            block = Block.getBlockById(nbttagcompound.getInteger("DisplayTile"));
         }

         int i = nbttagcompound.getInteger("DisplayData");
         this.setDisplayTile(block == null ? Blocks.AIR.getDefaultState() : block.getStateFromMeta(i));
         this.setDisplayTileOffset(nbttagcompound.getInteger("DisplayOffset"));
      }

   }

   protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      if (this.hasDisplayTile()) {
         nbttagcompound.setBoolean("CustomDisplayTile", true);
         IBlockState iblockdata = this.getDisplayTile();
         ResourceLocation minecraftkey = (ResourceLocation)Block.REGISTRY.getNameForObject(iblockdata.getBlock());
         nbttagcompound.setString("DisplayTile", minecraftkey == null ? "" : minecraftkey.toString());
         nbttagcompound.setInteger("DisplayData", iblockdata.getBlock().getMetaFromState(iblockdata));
         nbttagcompound.setInteger("DisplayOffset", this.getDisplayTileOffset());
      }

   }

   public void applyEntityCollision(Entity entity) {
      if (!this.world.isRemote && !entity.noClip && !this.noClip && !this.isPassenger(entity)) {
         double d0 = entity.posX - this.posX;
         double d1 = entity.posZ - this.posZ;
         double d2 = d0 * d0 + d1 * d1;
         if (d2 >= 9.999999747378752E-5D) {
            d2 = (double)MathHelper.sqrt(d2);
            d0 = d0 / d2;
            d1 = d1 / d2;
            double d3 = 1.0D / d2;
            if (d3 > 1.0D) {
               d3 = 1.0D;
            }

            d0 = d0 * d3;
            d1 = d1 * d3;
            d0 = d0 * 0.10000000149011612D;
            d1 = d1 * 0.10000000149011612D;
            d0 = d0 * (double)(1.0F - this.entityCollisionReduction);
            d1 = d1 * (double)(1.0F - this.entityCollisionReduction);
            d0 = d0 * 0.5D;
            d1 = d1 * 0.5D;
            if (entity instanceof EntityMinecart) {
               double d4 = entity.posX - this.posX;
               double d5 = entity.posZ - this.posZ;
               Vec3d vec3d = (new Vec3d(d4, 0.0D, d5)).normalize();
               Vec3d vec3d1 = (new Vec3d((double)MathHelper.cos(this.rotationYaw * 0.017453292F), 0.0D, (double)MathHelper.sin(this.rotationYaw * 0.017453292F))).normalize();
               double d6 = Math.abs(vec3d.dotProduct(vec3d1));
               if (d6 < 0.800000011920929D) {
                  return;
               }

               double d7 = entity.motionX + this.motionX;
               double d8 = entity.motionZ + this.motionZ;
               if (((EntityMinecart)entity).getType() == EntityMinecart.Type.FURNACE && this.getType() != EntityMinecart.Type.FURNACE) {
                  this.motionX *= 0.20000000298023224D;
                  this.motionZ *= 0.20000000298023224D;
                  this.addVelocity(entity.motionX - d0, 0.0D, entity.motionZ - d1);
                  entity.motionX *= 0.949999988079071D;
                  entity.motionZ *= 0.949999988079071D;
               } else if (((EntityMinecart)entity).getType() != EntityMinecart.Type.FURNACE && this.getType() == EntityMinecart.Type.FURNACE) {
                  entity.motionX *= 0.20000000298023224D;
                  entity.motionZ *= 0.20000000298023224D;
                  entity.addVelocity(this.motionX + d0, 0.0D, this.motionZ + d1);
                  this.motionX *= 0.949999988079071D;
                  this.motionZ *= 0.949999988079071D;
               } else {
                  d7 = d7 / 2.0D;
                  d8 = d8 / 2.0D;
                  this.motionX *= 0.20000000298023224D;
                  this.motionZ *= 0.20000000298023224D;
                  this.addVelocity(d7 - d0, 0.0D, d8 - d1);
                  entity.motionX *= 0.20000000298023224D;
                  entity.motionZ *= 0.20000000298023224D;
                  entity.addVelocity(d7 + d0, 0.0D, d8 + d1);
               }
            } else {
               this.addVelocity(-d0, 0.0D, -d1);
               entity.addVelocity(d0 / 4.0D, 0.0D, d1 / 4.0D);
            }
         }
      }

   }

   public void setDamage(float f) {
      this.dataManager.set(DAMAGE, Float.valueOf(f));
   }

   public float getDamage() {
      return ((Float)this.dataManager.get(DAMAGE)).floatValue();
   }

   public void setRollingAmplitude(int i) {
      this.dataManager.set(ROLLING_AMPLITUDE, Integer.valueOf(i));
   }

   public int getRollingAmplitude() {
      return ((Integer)this.dataManager.get(ROLLING_AMPLITUDE)).intValue();
   }

   public void setRollingDirection(int i) {
      this.dataManager.set(ROLLING_DIRECTION, Integer.valueOf(i));
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

   public void setDisplayTile(IBlockState iblockdata) {
      this.getDataManager().set(DISPLAY_TILE, Integer.valueOf(Block.getStateId(iblockdata)));
      this.setHasDisplayTile(true);
   }

   public void setDisplayTileOffset(int i) {
      this.getDataManager().set(DISPLAY_TILE_OFFSET, Integer.valueOf(i));
      this.setHasDisplayTile(true);
   }

   public boolean hasDisplayTile() {
      return ((Boolean)this.getDataManager().get(SHOW_BLOCK)).booleanValue();
   }

   public void setHasDisplayTile(boolean flag) {
      this.getDataManager().set(SHOW_BLOCK, Boolean.valueOf(flag));
   }

   public Vector getFlyingVelocityMod() {
      return new Vector(this.flyingX, this.flyingY, this.flyingZ);
   }

   public void setFlyingVelocityMod(Vector flying) {
      this.flyingX = flying.getX();
      this.flyingY = flying.getY();
      this.flyingZ = flying.getZ();
   }

   public Vector getDerailedVelocityMod() {
      return new Vector(this.derailedX, this.derailedY, this.derailedZ);
   }

   public void setDerailedVelocityMod(Vector derailed) {
      this.derailedX = derailed.getX();
      this.derailedY = derailed.getY();
      this.derailedZ = derailed.getZ();
   }

   static class SyntheticClass_1 {
      static final int[] a;
      static final int[] b = new int[BlockRailBase.EnumRailDirection.values().length];

      static {
         try {
            b[BlockRailBase.EnumRailDirection.ASCENDING_EAST.ordinal()] = 1;
         } catch (NoSuchFieldError var9) {
            ;
         }

         try {
            b[BlockRailBase.EnumRailDirection.ASCENDING_WEST.ordinal()] = 2;
         } catch (NoSuchFieldError var8) {
            ;
         }

         try {
            b[BlockRailBase.EnumRailDirection.ASCENDING_NORTH.ordinal()] = 3;
         } catch (NoSuchFieldError var7) {
            ;
         }

         try {
            b[BlockRailBase.EnumRailDirection.ASCENDING_SOUTH.ordinal()] = 4;
         } catch (NoSuchFieldError var6) {
            ;
         }

         a = new int[EntityMinecart.Type.values().length];

         try {
            a[EntityMinecart.Type.CHEST.ordinal()] = 1;
         } catch (NoSuchFieldError var5) {
            ;
         }

         try {
            a[EntityMinecart.Type.FURNACE.ordinal()] = 2;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            a[EntityMinecart.Type.TNT.ordinal()] = 3;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            a[EntityMinecart.Type.SPAWNER.ordinal()] = 4;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            a[EntityMinecart.Type.HOPPER.ordinal()] = 5;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[EntityMinecart.Type.COMMAND_BLOCK.ordinal()] = 6;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
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

      static {
         for(EntityMinecart.Type entityminecartabstract_enumminecarttype : values()) {
            BY_ID.put(Integer.valueOf(entityminecartabstract_enumminecarttype.getId()), entityminecartabstract_enumminecarttype);
         }

      }

      private Type(int i, String s) {
         this.id = i;
         this.name = s;
      }

      public int getId() {
         return this.id;
      }

      public String getName() {
         return this.name;
      }
   }
}
