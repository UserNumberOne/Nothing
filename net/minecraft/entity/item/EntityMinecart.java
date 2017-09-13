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
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEntity;
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

   public EntityMinecart(World var1) {
      super(var1);
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

   public static EntityMinecart create(World var0, double var1, double var3, double var5, EntityMinecart.Type var7) {
      switch(EntityMinecart.SyntheticClass_1.a[var7.ordinal()]) {
      case 1:
         return new EntityMinecartChest(var0, var1, var3, var5);
      case 2:
         return new EntityMinecartFurnace(var0, var1, var3, var5);
      case 3:
         return new EntityMinecartTNT(var0, var1, var3, var5);
      case 4:
         return new EntityMinecartMobSpawner(var0, var1, var3, var5);
      case 5:
         return new EntityMinecartHopper(var0, var1, var3, var5);
      case 6:
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
      return var1.canBePushed() ? var1.getEntityBoundingBox() : null;
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox() {
      return null;
   }

   public boolean canBePushed() {
      return true;
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
      this.world.getServer().getPluginManager().callEvent(new VehicleCreateEvent((Vehicle)this.getBukkitEntity()));
   }

   public double getMountedYOffset() {
      return 0.0D;
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (!this.world.isRemote && !this.isDead) {
         if (this.isEntityInvulnerable(var1)) {
            return false;
         } else {
            Vehicle var3 = (Vehicle)this.getBukkitEntity();
            CraftEntity var4 = var1.getEntity() == null ? null : var1.getEntity().getBukkitEntity();
            VehicleDamageEvent var5 = new VehicleDamageEvent(var3, var4, (double)var2);
            this.world.getServer().getPluginManager().callEvent(var5);
            if (var5.isCancelled()) {
               return true;
            } else {
               var2 = (float)var5.getDamage();
               this.setRollingDirection(-this.getRollingDirection());
               this.setRollingAmplitude(10);
               this.setBeenAttacked();
               this.setDamage(this.getDamage() + var2 * 10.0F);
               boolean var6 = var1.getEntity() instanceof EntityPlayer && ((EntityPlayer)var1.getEntity()).capabilities.isCreativeMode;
               if (var6 || this.getDamage() > 40.0F) {
                  VehicleDestroyEvent var7 = new VehicleDestroyEvent(var3, var4);
                  this.world.getServer().getPluginManager().callEvent(var7);
                  if (var7.isCancelled()) {
                     this.setDamage(40.0F);
                     return true;
                  }

                  this.removePassengers();
                  if (var6 && !this.hasCustomName()) {
                     this.setDead();
                  } else {
                     this.killMinecart(var1);
                  }
               }

               return true;
            }
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
      double var1 = this.posX;
      double var3 = this.posY;
      double var5 = this.posZ;
      float var7 = this.rotationYaw;
      float var8 = this.rotationPitch;
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
         MinecraftServer var9 = this.world.getMinecraftServer();
         int var10 = this.getMaxInPortalTime();
         if (this.inPortal) {
            if (!this.isRiding() && this.portalCounter++ >= var10) {
               this.portalCounter = var10;
               this.timeUntilPortal = this.getPortalCooldown();
               byte var11;
               if (this.world.provider.getDimensionType().getId() == -1) {
                  var11 = 0;
               } else {
                  var11 = -1;
               }

               this.changeDimension(var11);
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
            double var12 = this.posX + (this.minecartX - this.posX) / (double)this.turnProgress;
            double var14 = this.posY + (this.minecartY - this.posY) / (double)this.turnProgress;
            double var16 = this.posZ + (this.minecartZ - this.posZ) / (double)this.turnProgress;
            double var18 = MathHelper.wrapDegrees(this.minecartYaw - (double)this.rotationYaw);
            this.rotationYaw = (float)((double)this.rotationYaw + var18 / (double)this.turnProgress);
            this.rotationPitch = (float)((double)this.rotationPitch + (this.minecartPitch - (double)this.rotationPitch) / (double)this.turnProgress);
            --this.turnProgress;
            this.setPosition(var12, var14, var16);
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

         int var32 = MathHelper.floor(this.posX);
         int var33 = MathHelper.floor(this.posY);
         int var34 = MathHelper.floor(this.posZ);
         if (BlockRailBase.isRailBlock(this.world, new BlockPos(var32, var33 - 1, var34))) {
            --var33;
         }

         BlockPos var20 = new BlockPos(var32, var33, var34);
         IBlockState var21 = this.world.getBlockState(var20);
         if (BlockRailBase.isRailBlock(var21)) {
            this.moveAlongTrack(var20, var21);
            if (var21.getBlock() == Blocks.ACTIVATOR_RAIL) {
               this.onActivatorRailPass(var32, var33, var34, ((Boolean)var21.getValue(BlockRailPowered.POWERED)).booleanValue());
            }
         } else {
            this.moveDerailedMinecart();
         }

         this.doBlockCollisions();
         this.rotationPitch = 0.0F;
         double var35 = this.prevPosX - this.posX;
         double var36 = this.prevPosZ - this.posZ;
         if (var35 * var35 + var36 * var36 > 0.001D) {
            this.rotationYaw = (float)(MathHelper.atan2(var36, var35) * 180.0D / 3.141592653589793D);
            if (this.isInReverse) {
               this.rotationYaw += 180.0F;
            }
         }

         double var22 = (double)MathHelper.wrapDegrees(this.rotationYaw - this.prevRotationYaw);
         if (var22 < -170.0D || var22 >= 170.0D) {
            this.rotationYaw += 180.0F;
            this.isInReverse = !this.isInReverse;
         }

         this.setRotation(this.rotationYaw, this.rotationPitch);
         CraftWorld var24 = this.world.getWorld();
         Location var25 = new Location(var24, var1, var3, var5, var7, var8);
         Location var26 = new Location(var24, this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
         Vehicle var27 = (Vehicle)this.getBukkitEntity();
         this.world.getServer().getPluginManager().callEvent(new VehicleUpdateEvent(var27));
         if (!var25.equals(var26)) {
            this.world.getServer().getPluginManager().callEvent(new VehicleMoveEvent(var27, var25, var26));
         }

         if (this.getType() == EntityMinecart.Type.RIDEABLE && this.motionX * this.motionX + this.motionZ * this.motionZ > 0.01D) {
            List var37 = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox().expand(0.20000000298023224D, 0.0D, 0.20000000298023224D), EntitySelectors.getTeamCollisionPredicate(this));
            if (!var37.isEmpty()) {
               for(int var38 = 0; var38 < var37.size(); ++var38) {
                  Entity var39 = (Entity)var37.get(var38);
                  if (!(var39 instanceof EntityPlayer) && !(var39 instanceof EntityIronGolem) && !(var39 instanceof EntityMinecart) && !this.isBeingRidden() && !var39.isRiding()) {
                     VehicleEntityCollisionEvent var40 = new VehicleEntityCollisionEvent(var27, var39.getBukkitEntity());
                     this.world.getServer().getPluginManager().callEvent(var40);
                     if (!var40.isCancelled()) {
                        var39.startRiding(this);
                     }
                  } else {
                     VehicleEntityCollisionEvent var31 = new VehicleEntityCollisionEvent(var27, var39.getBukkitEntity());
                     this.world.getServer().getPluginManager().callEvent(var31);
                     if (!var31.isCancelled()) {
                        var39.applyEntityCollision(this);
                     }
                  }
               }
            }
         } else {
            for(Entity var29 : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(0.20000000298023224D, 0.0D, 0.20000000298023224D))) {
               if (!this.isPassenger(var29) && var29.canBePushed() && var29 instanceof EntityMinecart) {
                  VehicleEntityCollisionEvent var30 = new VehicleEntityCollisionEvent(var27, var29.getBukkitEntity());
                  this.world.getServer().getPluginManager().callEvent(var30);
                  if (!var30.isCancelled()) {
                     var29.applyEntityCollision(this);
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

   public void onActivatorRailPass(int var1, int var2, int var3, boolean var4) {
   }

   protected void moveDerailedMinecart() {
      double var1 = this.getMaximumSpeed();
      this.motionX = MathHelper.clamp(this.motionX, -var1, var1);
      this.motionZ = MathHelper.clamp(this.motionZ, -var1, var1);
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

      BlockRailBase.EnumRailDirection var7 = (BlockRailBase.EnumRailDirection)var2.getValue(var6.getShapeProperty());
      switch(EntityMinecart.SyntheticClass_1.b[var7.ordinal()]) {
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

      int[][] var8 = MATRIX[var7.getMetadata()];
      double var9 = (double)(var8[1][0] - var8[0][0]);
      double var11 = (double)(var8[1][2] - var8[0][2]);
      double var13 = Math.sqrt(var9 * var9 + var11 * var11);
      double var15 = this.motionX * var9 + this.motionZ * var11;
      if (var15 < 0.0D) {
         var9 = -var9;
         var11 = -var11;
      }

      double var17 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      if (var17 > 2.0D) {
         var17 = 2.0D;
      }

      this.motionX = var17 * var9 / var13;
      this.motionZ = var17 * var11 / var13;
      Entity var19 = this.getPassengers().isEmpty() ? null : (Entity)this.getPassengers().get(0);
      if (var19 instanceof EntityLivingBase) {
         double var20 = (double)((EntityLivingBase)var19).moveForward;
         if (var20 > 0.0D) {
            double var22 = -Math.sin((double)(var19.rotationYaw * 0.017453292F));
            double var24 = Math.cos((double)(var19.rotationYaw * 0.017453292F));
            double var26 = this.motionX * this.motionX + this.motionZ * this.motionZ;
            if (var26 < 0.01D) {
               this.motionX += var22 * 0.1D;
               this.motionZ += var24 * 0.1D;
               var5 = false;
            }
         }
      }

      if (var5) {
         double var47 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
         if (var47 < 0.03D) {
            this.motionX *= 0.0D;
            this.motionY *= 0.0D;
            this.motionZ *= 0.0D;
         } else {
            this.motionX *= 0.5D;
            this.motionY *= 0.0D;
            this.motionZ *= 0.5D;
         }
      }

      double var48 = (double)var1.getX() + 0.5D + (double)var8[0][0] * 0.5D;
      double var49 = (double)var1.getZ() + 0.5D + (double)var8[0][2] * 0.5D;
      double var50 = (double)var1.getX() + 0.5D + (double)var8[1][0] * 0.5D;
      double var51 = (double)var1.getZ() + 0.5D + (double)var8[1][2] * 0.5D;
      var9 = var50 - var48;
      var11 = var51 - var49;
      double var28;
      if (var9 == 0.0D) {
         this.posX = (double)var1.getX() + 0.5D;
         var28 = this.posZ - (double)var1.getZ();
      } else if (var11 == 0.0D) {
         this.posZ = (double)var1.getZ() + 0.5D;
         var28 = this.posX - (double)var1.getX();
      } else {
         double var30 = this.posX - var48;
         double var32 = this.posZ - var49;
         var28 = (var30 * var9 + var32 * var11) * 2.0D;
      }

      this.posX = var48 + var9 * var28;
      this.posZ = var49 + var11 * var28;
      this.setPosition(this.posX, this.posY, this.posZ);
      double var52 = this.motionX;
      double var54 = this.motionZ;
      if (this.isBeingRidden()) {
         var52 *= 0.75D;
         var54 *= 0.75D;
      }

      double var34 = this.getMaximumSpeed();
      var52 = MathHelper.clamp(var52, -var34, var34);
      var54 = MathHelper.clamp(var54, -var34, var34);
      this.move(var52, 0.0D, var54);
      if (var8[0][1] != 0 && MathHelper.floor(this.posX) - var1.getX() == var8[0][0] && MathHelper.floor(this.posZ) - var1.getZ() == var8[0][2]) {
         this.setPosition(this.posX, this.posY + (double)var8[0][1], this.posZ);
      } else if (var8[1][1] != 0 && MathHelper.floor(this.posX) - var1.getX() == var8[1][0] && MathHelper.floor(this.posZ) - var1.getZ() == var8[1][2]) {
         this.setPosition(this.posX, this.posY + (double)var8[1][1], this.posZ);
      }

      this.applyDrag();
      Vec3d var36 = this.getPos(this.posX, this.posY, this.posZ);
      if (var36 != null && var3 != null) {
         double var37 = (var3.yCoord - var36.yCoord) * 0.05D;
         var17 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
         if (var17 > 0.0D) {
            this.motionX = this.motionX / var17 * (var17 + var37);
            this.motionZ = this.motionZ / var17 * (var17 + var37);
         }

         this.setPosition(this.posX, var36.yCoord, this.posZ);
      }

      int var39 = MathHelper.floor(this.posX);
      int var40 = MathHelper.floor(this.posZ);
      if (var39 != var1.getX() || var40 != var1.getZ()) {
         var17 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
         this.motionX = var17 * (double)(var39 - var1.getX());
         this.motionZ = var17 * (double)(var40 - var1.getZ());
      }

      if (var4) {
         double var41 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
         if (var41 > 0.01D) {
            this.motionX += this.motionX / var41 * 0.06D;
            this.motionZ += this.motionZ / var41 * 0.06D;
         } else if (var7 == BlockRailBase.EnumRailDirection.EAST_WEST) {
            if (this.world.getBlockState(var1.west()).isNormalCube()) {
               this.motionX = 0.02D;
            } else if (this.world.getBlockState(var1.east()).isNormalCube()) {
               this.motionX = -0.02D;
            }
         } else if (var7 == BlockRailBase.EnumRailDirection.NORTH_SOUTH) {
            if (this.world.getBlockState(var1.north()).isNormalCube()) {
               this.motionZ = 0.02D;
            } else if (this.world.getBlockState(var1.south()).isNormalCube()) {
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

   public void setPosition(double var1, double var3, double var5) {
      this.posX = var1;
      this.posY = var3;
      this.posZ = var5;
      float var7 = this.width / 2.0F;
      float var8 = this.height;
      this.setEntityBoundingBox(new AxisAlignedBB(var1 - (double)var7, var3, var5 - (double)var7, var1 + (double)var7, var3 + (double)var8, var5 + (double)var7));
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
               if (((EntityMinecart)var1).getType() == EntityMinecart.Type.FURNACE && this.getType() != EntityMinecart.Type.FURNACE) {
                  this.motionX *= 0.20000000298023224D;
                  this.motionZ *= 0.20000000298023224D;
                  this.addVelocity(var1.motionX - var2, 0.0D, var1.motionZ - var4);
                  var1.motionX *= 0.949999988079071D;
                  var1.motionZ *= 0.949999988079071D;
               } else if (((EntityMinecart)var1).getType() != EntityMinecart.Type.FURNACE && this.getType() == EntityMinecart.Type.FURNACE) {
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

   public void setDamage(float var1) {
      this.dataManager.set(DAMAGE, Float.valueOf(var1));
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

   public Vector getFlyingVelocityMod() {
      return new Vector(this.flyingX, this.flyingY, this.flyingZ);
   }

   public void setFlyingVelocityMod(Vector var1) {
      this.flyingX = var1.getX();
      this.flyingY = var1.getY();
      this.flyingZ = var1.getZ();
   }

   public Vector getDerailedVelocityMod() {
      return new Vector(this.derailedX, this.derailedY, this.derailedZ);
   }

   public void setDerailedVelocityMod(Vector var1) {
      this.derailedX = var1.getX();
      this.derailedY = var1.getY();
      this.derailedZ = var1.getZ();
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
         for(EntityMinecart.Type var3 : values()) {
            BY_ID.put(Integer.valueOf(var3.getId()), var3);
         }

      }

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
   }
}
