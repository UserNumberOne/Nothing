package net.minecraft.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockWall;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Mirror;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.TravelAgent;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.craftbukkit.v1_10_R1.CraftTravelAgent;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.projectiles.ProjectileSource;

public abstract class Entity implements ICommandSender {
   private static final int CURRENT_LEVEL = 2;
   protected CraftEntity bukkitEntity;
   private static final Logger LOGGER = LogManager.getLogger();
   private static final AxisAlignedBB ZERO_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
   private static double renderDistanceWeight = 1.0D;
   private static int nextEntityID;
   private int entityId;
   public boolean preventEntitySpawning;
   public final List riddenByEntities;
   protected int rideCooldown;
   private Entity ridingEntity;
   public boolean forceSpawn;
   public World world;
   public double prevPosX;
   public double prevPosY;
   public double prevPosZ;
   public double posX;
   public double posY;
   public double posZ;
   public double motionX;
   public double motionY;
   public double motionZ;
   public float rotationYaw;
   public float rotationPitch;
   public float prevRotationYaw;
   public float prevRotationPitch;
   private AxisAlignedBB boundingBox;
   public boolean onGround;
   public boolean isCollidedHorizontally;
   public boolean isCollidedVertically;
   public boolean isCollided;
   public boolean velocityChanged;
   protected boolean isInWeb;
   private boolean isOutsideBorder;
   public boolean isDead;
   public float width;
   public float height;
   public float prevDistanceWalkedModified;
   public float distanceWalkedModified;
   public float distanceWalkedOnStepModified;
   public float fallDistance;
   private int nextStepDistance;
   public double lastTickPosX;
   public double lastTickPosY;
   public double lastTickPosZ;
   public float stepHeight;
   public boolean noClip;
   public float entityCollisionReduction;
   protected Random rand;
   public int ticksExisted;
   public int fireResistance;
   public int fire;
   public boolean inWater;
   public int hurtResistantTime;
   protected boolean firstUpdate;
   protected boolean isImmuneToFire;
   protected EntityDataManager dataManager;
   protected static final DataParameter FLAGS = EntityDataManager.createKey(Entity.class, DataSerializers.BYTE);
   private static final DataParameter AIR = EntityDataManager.createKey(Entity.class, DataSerializers.VARINT);
   private static final DataParameter CUSTOM_NAME = EntityDataManager.createKey(Entity.class, DataSerializers.STRING);
   private static final DataParameter CUSTOM_NAME_VISIBLE = EntityDataManager.createKey(Entity.class, DataSerializers.BOOLEAN);
   private static final DataParameter SILENT = EntityDataManager.createKey(Entity.class, DataSerializers.BOOLEAN);
   private static final DataParameter NO_GRAVITY = EntityDataManager.createKey(Entity.class, DataSerializers.BOOLEAN);
   public boolean addedToChunk;
   public int chunkCoordX;
   public int chunkCoordY;
   public int chunkCoordZ;
   public boolean ignoreFrustumCheck;
   public boolean isAirBorne;
   public int timeUntilPortal;
   protected boolean inPortal;
   protected int portalCounter;
   public int dimension;
   protected BlockPos lastPortalPos;
   protected Vec3d lastPortalVec;
   protected EnumFacing teleportDirection;
   private boolean invulnerable;
   protected UUID entityUniqueID;
   protected String cachedUniqueIdString;
   private final CommandResultStats cmdResultStats;
   private final List emptyItemStackList;
   public boolean glowing;
   private final Set tags;
   private boolean isPositionDirty;
   public boolean valid;
   public ProjectileSource projectileSource;
   public boolean forceExplosionKnockback;

   static boolean isLevelAtLeast(NBTTagCompound var0, int var1) {
      return var0.hasKey("Bukkit.updateLevel") && var0.getInteger("Bukkit.updateLevel") >= var1;
   }

   public CraftEntity getBukkitEntity() {
      if (this.bukkitEntity == null) {
         this.bukkitEntity = CraftEntity.getEntity(this.world.getServer(), this);
      }

      return this.bukkitEntity;
   }

   public Entity(World var1) {
      this.entityId = nextEntityID++;
      this.riddenByEntities = Lists.newArrayList();
      this.boundingBox = ZERO_AABB;
      this.width = 0.6F;
      this.height = 1.8F;
      this.nextStepDistance = 1;
      this.rand = new Random();
      this.fireResistance = 1;
      this.firstUpdate = true;
      this.entityUniqueID = MathHelper.getRandomUUID(this.rand);
      this.cachedUniqueIdString = this.entityUniqueID.toString();
      this.cmdResultStats = new CommandResultStats();
      this.emptyItemStackList = Lists.newArrayList();
      this.tags = Sets.newHashSet();
      this.world = var1;
      this.setPosition(0.0D, 0.0D, 0.0D);
      if (var1 != null) {
         this.dimension = var1.provider.getDimensionType().getId();
      }

      this.dataManager = new EntityDataManager(this);
      this.dataManager.register(FLAGS, Byte.valueOf((byte)0));
      this.dataManager.register(AIR, Integer.valueOf(300));
      this.dataManager.register(CUSTOM_NAME_VISIBLE, Boolean.valueOf(false));
      this.dataManager.register(CUSTOM_NAME, "");
      this.dataManager.register(SILENT, Boolean.valueOf(false));
      this.dataManager.register(NO_GRAVITY, Boolean.valueOf(false));
      this.entityInit();
   }

   public int getEntityId() {
      return this.entityId;
   }

   public void setEntityId(int var1) {
      this.entityId = var1;
   }

   public Set getTags() {
      return this.tags;
   }

   public boolean addTag(String var1) {
      if (this.tags.size() >= 1024) {
         return false;
      } else {
         this.tags.add(var1);
         return true;
      }
   }

   public boolean removeTag(String var1) {
      return this.tags.remove(var1);
   }

   public void onKillCommand() {
      this.setDead();
   }

   protected abstract void entityInit();

   public EntityDataManager getDataManager() {
      return this.dataManager;
   }

   public boolean equals(Object var1) {
      return var1 instanceof Entity ? ((Entity)var1).entityId == this.entityId : false;
   }

   public int hashCode() {
      return this.entityId;
   }

   public void setDead() {
      this.isDead = true;
   }

   public void setDropItemsWhenDead(boolean var1) {
   }

   public void setSize(float var1, float var2) {
      if (var1 != this.width || var2 != this.height) {
         float var3 = this.width;
         this.width = var1;
         this.height = var2;
         AxisAlignedBB var4 = this.getEntityBoundingBox();
         this.setEntityBoundingBox(new AxisAlignedBB(var4.minX, var4.minY, var4.minZ, var4.minX + (double)this.width, var4.minY + (double)this.height, var4.minZ + (double)this.width));
         if (this.width > var3 && !this.firstUpdate && !this.world.isRemote) {
            this.move((double)(var3 - this.width), 0.0D, (double)(var3 - this.width));
         }
      }

   }

   protected void setRotation(float var1, float var2) {
      if (Float.isNaN(var1)) {
         var1 = 0.0F;
      }

      if (var1 == Float.POSITIVE_INFINITY || var1 == Float.NEGATIVE_INFINITY) {
         if (this instanceof EntityPlayerMP) {
            this.world.getServer().getLogger().warning(this.getName() + " was caught trying to crash the server with an invalid yaw");
            ((CraftPlayer)this.getBukkitEntity()).kickPlayer("Nope");
         }

         var1 = 0.0F;
      }

      if (Float.isNaN(var2)) {
         var2 = 0.0F;
      }

      if (var2 == Float.POSITIVE_INFINITY || var2 == Float.NEGATIVE_INFINITY) {
         if (this instanceof EntityPlayerMP) {
            this.world.getServer().getLogger().warning(this.getName() + " was caught trying to crash the server with an invalid pitch");
            ((CraftPlayer)this.getBukkitEntity()).kickPlayer("Nope");
         }

         var2 = 0.0F;
      }

      this.rotationYaw = var1 % 360.0F;
      this.rotationPitch = var2 % 360.0F;
   }

   public void setPosition(double var1, double var3, double var5) {
      this.posX = var1;
      this.posY = var3;
      this.posZ = var5;
      float var7 = this.width / 2.0F;
      float var8 = this.height;
      this.setEntityBoundingBox(new AxisAlignedBB(var1 - (double)var7, var3, var5 - (double)var7, var1 + (double)var7, var3 + (double)var8, var5 + (double)var7));
   }

   public void onUpdate() {
      if (!this.world.isRemote) {
         this.setFlag(6, this.isGlowing());
      }

      this.onEntityUpdate();
   }

   public void onEntityUpdate() {
      this.world.theProfiler.startSection("entityBaseTick");
      if (this.isRiding() && this.getRidingEntity().isDead) {
         this.dismountRidingEntity();
      }

      if (this.rideCooldown > 0) {
         --this.rideCooldown;
      }

      this.prevDistanceWalkedModified = this.distanceWalkedModified;
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      this.prevRotationPitch = this.rotationPitch;
      this.prevRotationYaw = this.rotationYaw;
      if (!this.world.isRemote && this.world instanceof WorldServer) {
         this.world.theProfiler.startSection("portal");
         if (this.inPortal) {
            MinecraftServer var1 = this.world.getMinecraftServer();
            if (!this.isRiding()) {
               int var2 = this.getMaxInPortalTime();
               if (this.portalCounter++ >= var2) {
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

         this.decrementTimeUntilPortal();
         this.world.theProfiler.endSection();
      }

      this.spawnRunningParticles();
      this.handleWaterMovement();
      if (this.world.isRemote) {
         this.fire = 0;
      } else if (this.fire > 0) {
         if (this.isImmuneToFire) {
            this.fire -= 4;
            if (this.fire < 0) {
               this.fire = 0;
            }
         } else {
            if (this.fire % 20 == 0) {
               this.attackEntityFrom(DamageSource.onFire, 1.0F);
            }

            --this.fire;
         }
      }

      if (this.isInLava()) {
         this.setOnFireFromLava();
         this.fallDistance *= 0.5F;
      }

      if (this.posY < -64.0D) {
         this.kill();
      }

      if (!this.world.isRemote) {
         this.setFlag(0, this.fire > 0);
      }

      this.firstUpdate = false;
      this.world.theProfiler.endSection();
   }

   protected void decrementTimeUntilPortal() {
      if (this.timeUntilPortal > 0) {
         --this.timeUntilPortal;
      }

   }

   public int getMaxInPortalTime() {
      return 1;
   }

   protected void setOnFireFromLava() {
      if (!this.isImmuneToFire) {
         this.attackEntityFrom(DamageSource.lava, 4.0F);
         if (this instanceof EntityLivingBase) {
            if (this.fire <= 0) {
               Object var1 = null;
               CraftEntity var2 = this.getBukkitEntity();
               EntityCombustByBlockEvent var3 = new EntityCombustByBlockEvent((Block)var1, var2, 15);
               this.world.getServer().getPluginManager().callEvent(var3);
               if (!var3.isCancelled()) {
                  this.setFire(var3.getDuration());
               }
            } else {
               this.setFire(15);
            }

            return;
         }

         this.setFire(15);
      }

   }

   public void setFire(int var1) {
      int var2 = var1 * 20;
      if (this instanceof EntityLivingBase) {
         var2 = EnchantmentProtection.getFireTimeForEntity((EntityLivingBase)this, var2);
      }

      if (this.fire < var2) {
         this.fire = var2;
      }

   }

   public void extinguish() {
      this.fire = 0;
   }

   protected void kill() {
      this.setDead();
   }

   public boolean isOffsetPositionInLiquid(double var1, double var3, double var5) {
      AxisAlignedBB var7 = this.getEntityBoundingBox().offset(var1, var3, var5);
      return this.isLiquidPresentInAABB(var7);
   }

   private boolean isLiquidPresentInAABB(AxisAlignedBB var1) {
      return this.world.getCollisionBoxes(this, var1).isEmpty() && !this.world.containsAnyLiquid(var1);
   }

   public void move(double var1, double var3, double var5) {
      if (this.noClip) {
         this.setEntityBoundingBox(this.getEntityBoundingBox().offset(var1, var3, var5));
         this.resetPositionToBB();
      } else {
         try {
            this.doBlockCollisions();
         } catch (Throwable var77) {
            CrashReport var8 = CrashReport.makeCrashReport(var77, "Checking entity block collision");
            CrashReportCategory var9 = var8.makeCategory("Entity being checked for collision");
            this.addEntityCrashInfo(var9);
            throw new ReportedException(var8);
         }

         if (var1 == 0.0D && var3 == 0.0D && var5 == 0.0D && this.isBeingRidden() && this.isRiding()) {
            return;
         }

         this.world.theProfiler.startSection("move");
         double var10 = this.posX;
         double var12 = this.posY;
         double var14 = this.posZ;
         if (this.isInWeb) {
            this.isInWeb = false;
            var1 *= 0.25D;
            var3 *= 0.05000000074505806D;
            var5 *= 0.25D;
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
         }

         double var16 = var1;
         double var18 = var3;
         double var20 = var5;
         boolean var22 = this.onGround && this.isSneaking() && this instanceof EntityPlayer;
         if (var22) {
            for(; var1 != 0.0D && this.world.getCollisionBoxes(this, this.getEntityBoundingBox().offset(var1, -1.0D, 0.0D)).isEmpty(); var16 = var1) {
               if (var1 < 0.05D && var1 >= -0.05D) {
                  var1 = 0.0D;
               } else if (var1 > 0.0D) {
                  var1 -= 0.05D;
               } else {
                  var1 += 0.05D;
               }
            }

            for(; var5 != 0.0D && this.world.getCollisionBoxes(this, this.getEntityBoundingBox().offset(0.0D, -1.0D, var5)).isEmpty(); var20 = var5) {
               if (var5 < 0.05D && var5 >= -0.05D) {
                  var5 = 0.0D;
               } else if (var5 > 0.0D) {
                  var5 -= 0.05D;
               } else {
                  var5 += 0.05D;
               }
            }

            for(; var1 != 0.0D && var5 != 0.0D && this.world.getCollisionBoxes(this, this.getEntityBoundingBox().offset(var1, -1.0D, var5)).isEmpty(); var20 = var5) {
               if (var1 < 0.05D && var1 >= -0.05D) {
                  var1 = 0.0D;
               } else if (var1 > 0.0D) {
                  var1 -= 0.05D;
               } else {
                  var1 += 0.05D;
               }

               var16 = var1;
               if (var5 < 0.05D && var5 >= -0.05D) {
                  var5 = 0.0D;
               } else if (var5 > 0.0D) {
                  var5 -= 0.05D;
               } else {
                  var5 += 0.05D;
               }
            }
         }

         List var23 = this.world.getCollisionBoxes(this, this.getEntityBoundingBox().addCoord(var1, var3, var5));
         AxisAlignedBB var24 = this.getEntityBoundingBox();
         int var25 = 0;

         for(int var26 = var23.size(); var25 < var26; ++var25) {
            var3 = ((AxisAlignedBB)var23.get(var25)).calculateYOffset(this.getEntityBoundingBox(), var3);
         }

         this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, var3, 0.0D));
         boolean var27 = this.onGround || var18 != var3 && var18 < 0.0D;
         int var79 = 0;

         for(int var28 = var23.size(); var79 < var28; ++var79) {
            var1 = ((AxisAlignedBB)var23.get(var79)).calculateXOffset(this.getEntityBoundingBox(), var1);
         }

         this.setEntityBoundingBox(this.getEntityBoundingBox().offset(var1, 0.0D, 0.0D));
         var79 = 0;

         for(int var82 = var23.size(); var79 < var82; ++var79) {
            var5 = ((AxisAlignedBB)var23.get(var79)).calculateZOffset(this.getEntityBoundingBox(), var5);
         }

         this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, 0.0D, var5));
         if (this.stepHeight > 0.0F && var27 && (var16 != var1 || var20 != var5)) {
            double var29 = var1;
            double var31 = var3;
            double var33 = var5;
            AxisAlignedBB var35 = this.getEntityBoundingBox();
            this.setEntityBoundingBox(var24);
            var3 = (double)this.stepHeight;
            List var36 = this.world.getCollisionBoxes(this, this.getEntityBoundingBox().addCoord(var16, var3, var20));
            AxisAlignedBB var37 = this.getEntityBoundingBox();
            AxisAlignedBB var38 = var37.addCoord(var16, 0.0D, var20);
            double var39 = var3;
            int var41 = 0;

            for(int var42 = var36.size(); var41 < var42; ++var41) {
               var39 = ((AxisAlignedBB)var36.get(var41)).calculateYOffset(var38, var39);
            }

            var37 = var37.offset(0.0D, var39, 0.0D);
            double var43 = var16;
            int var45 = 0;

            for(int var46 = var36.size(); var45 < var46; ++var45) {
               var43 = ((AxisAlignedBB)var36.get(var45)).calculateXOffset(var37, var43);
            }

            var37 = var37.offset(var43, 0.0D, 0.0D);
            double var47 = var20;
            int var49 = 0;

            for(int var50 = var36.size(); var49 < var50; ++var49) {
               var47 = ((AxisAlignedBB)var36.get(var49)).calculateZOffset(var37, var47);
            }

            var37 = var37.offset(0.0D, 0.0D, var47);
            AxisAlignedBB var91 = this.getEntityBoundingBox();
            double var51 = var3;
            int var53 = 0;

            for(int var54 = var36.size(); var53 < var54; ++var53) {
               var51 = ((AxisAlignedBB)var36.get(var53)).calculateYOffset(var91, var51);
            }

            var91 = var91.offset(0.0D, var51, 0.0D);
            double var55 = var16;
            int var57 = 0;

            for(int var58 = var36.size(); var57 < var58; ++var57) {
               var55 = ((AxisAlignedBB)var36.get(var57)).calculateXOffset(var91, var55);
            }

            var91 = var91.offset(var55, 0.0D, 0.0D);
            double var59 = var20;
            int var61 = 0;

            for(int var62 = var36.size(); var61 < var62; ++var61) {
               var59 = ((AxisAlignedBB)var36.get(var61)).calculateZOffset(var91, var59);
            }

            var91 = var91.offset(0.0D, 0.0D, var59);
            double var63 = var43 * var43 + var47 * var47;
            double var65 = var55 * var55 + var59 * var59;
            if (var63 > var65) {
               var1 = var43;
               var5 = var47;
               var3 = -var39;
               this.setEntityBoundingBox(var37);
            } else {
               var1 = var55;
               var5 = var59;
               var3 = -var51;
               this.setEntityBoundingBox(var91);
            }

            int var67 = 0;

            for(int var68 = var36.size(); var67 < var68; ++var67) {
               var3 = ((AxisAlignedBB)var36.get(var67)).calculateYOffset(this.getEntityBoundingBox(), var3);
            }

            this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, var3, 0.0D));
            if (var29 * var29 + var33 * var33 >= var1 * var1 + var5 * var5) {
               var1 = var29;
               var3 = var31;
               var5 = var33;
               this.setEntityBoundingBox(var35);
            }
         }

         this.world.theProfiler.endSection();
         this.world.theProfiler.startSection("rest");
         this.resetPositionToBB();
         this.isCollidedHorizontally = var16 != var1 || var20 != var5;
         this.isCollidedVertically = var18 != var3;
         this.onGround = this.isCollidedVertically && var18 < 0.0D;
         this.isCollided = this.isCollidedHorizontally || this.isCollidedVertically;
         var79 = MathHelper.floor(this.posX);
         int var83 = MathHelper.floor(this.posY - 0.20000000298023224D);
         int var69 = MathHelper.floor(this.posZ);
         BlockPos var70 = new BlockPos(var79, var83, var69);
         IBlockState var71 = this.world.getBlockState(var70);
         if (var71.getMaterial() == Material.AIR) {
            BlockPos var72 = var70.down();
            IBlockState var73 = this.world.getBlockState(var72);
            net.minecraft.block.Block var74 = var73.getBlock();
            if (var74 instanceof BlockFence || var74 instanceof BlockWall || var74 instanceof BlockFenceGate) {
               var71 = var73;
               var70 = var72;
            }
         }

         this.updateFallState(var3, this.onGround, var71, var70);
         if (var16 != var1) {
            this.motionX = 0.0D;
         }

         if (var20 != var5) {
            this.motionZ = 0.0D;
         }

         net.minecraft.block.Block var95 = var71.getBlock();
         if (var18 != var3) {
            var95.onLanded(this.world, this);
         }

         if (this.isCollidedHorizontally && this.getBukkitEntity() instanceof Vehicle) {
            Vehicle var96 = (Vehicle)this.getBukkitEntity();
            Block var98 = this.world.getWorld().getBlockAt(MathHelper.floor(this.posX), MathHelper.floor(this.posY), MathHelper.floor(this.posZ));
            if (var16 > var1) {
               var98 = var98.getRelative(BlockFace.EAST);
            } else if (var16 < var1) {
               var98 = var98.getRelative(BlockFace.WEST);
            } else if (var20 > var5) {
               var98 = var98.getRelative(BlockFace.SOUTH);
            } else if (var20 < var5) {
               var98 = var98.getRelative(BlockFace.NORTH);
            }

            if (var98.getType() != org.bukkit.Material.AIR) {
               VehicleBlockCollisionEvent var85 = new VehicleBlockCollisionEvent(var96, var98);
               this.world.getServer().getPluginManager().callEvent(var85);
            }
         }

         if (this.canTriggerWalking() && !var22 && !this.isRiding()) {
            double var84 = this.posX - var10;
            double var75 = this.posY - var12;
            double var90 = this.posZ - var14;
            if (var95 != Blocks.LADDER) {
               var75 = 0.0D;
            }

            if (var95 != null && this.onGround) {
               var95.onEntityWalk(this.world, var70, this);
            }

            this.distanceWalkedModified = (float)((double)this.distanceWalkedModified + (double)MathHelper.sqrt(var84 * var84 + var90 * var90) * 0.6D);
            this.distanceWalkedOnStepModified = (float)((double)this.distanceWalkedOnStepModified + (double)MathHelper.sqrt(var84 * var84 + var75 * var75 + var90 * var90) * 0.6D);
            if (this.distanceWalkedOnStepModified > (float)this.nextStepDistance && var71.getMaterial() != Material.AIR) {
               this.nextStepDistance = (int)this.distanceWalkedOnStepModified + 1;
               if (this.isInWater()) {
                  float var89 = MathHelper.sqrt(this.motionX * this.motionX * 0.20000000298023224D + this.motionY * this.motionY + this.motionZ * this.motionZ * 0.20000000298023224D) * 0.35F;
                  if (var89 > 1.0F) {
                     var89 = 1.0F;
                  }

                  this.playSound(this.getSwimSound(), var89, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
               }

               this.playStepSound(var70, var95);
            }
         }

         boolean var97 = this.isWet();
         if (this.world.isFlammableWithin(this.getEntityBoundingBox().contract(0.001D))) {
            this.burn(1.0F);
            if (!var97) {
               ++this.fire;
               if (this.fire == 0) {
                  EntityCombustByBlockEvent var99 = new EntityCombustByBlockEvent((Block)null, this.getBukkitEntity(), 8);
                  this.world.getServer().getPluginManager().callEvent(var99);
                  if (!var99.isCancelled()) {
                     this.setFire(var99.getDuration());
                  }
               }
            }
         } else if (this.fire <= 0) {
            this.fire = -this.fireResistance;
         }

         if (var97 && this.fire > 0) {
            this.playSound(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
            this.fire = -this.fireResistance;
         }

         this.world.theProfiler.endSection();
      }

   }

   public void resetPositionToBB() {
      AxisAlignedBB var1 = this.getEntityBoundingBox();
      this.posX = (var1.minX + var1.maxX) / 2.0D;
      this.posY = var1.minY;
      this.posZ = (var1.minZ + var1.maxZ) / 2.0D;
   }

   protected SoundEvent getSwimSound() {
      return SoundEvents.ENTITY_GENERIC_SWIM;
   }

   protected SoundEvent getSplashSound() {
      return SoundEvents.ENTITY_GENERIC_SPLASH;
   }

   protected void doBlockCollisions() {
      AxisAlignedBB var1 = this.getEntityBoundingBox();
      BlockPos.PooledMutableBlockPos var2 = BlockPos.PooledMutableBlockPos.retain(var1.minX + 0.001D, var1.minY + 0.001D, var1.minZ + 0.001D);
      BlockPos.PooledMutableBlockPos var3 = BlockPos.PooledMutableBlockPos.retain(var1.maxX - 0.001D, var1.maxY - 0.001D, var1.maxZ - 0.001D);
      BlockPos.PooledMutableBlockPos var4 = BlockPos.PooledMutableBlockPos.retain();
      if (this.world.isAreaLoaded(var2, var3)) {
         for(int var5 = var2.getX(); var5 <= var3.getX(); ++var5) {
            for(int var6 = var2.getY(); var6 <= var3.getY(); ++var6) {
               for(int var7 = var2.getZ(); var7 <= var3.getZ(); ++var7) {
                  var4.setPos(var5, var6, var7);
                  IBlockState var8 = this.world.getBlockState(var4);

                  try {
                     var8.getBlock().onEntityCollidedWithBlock(this.world, var4, var8, this);
                  } catch (Throwable var12) {
                     CrashReport var10 = CrashReport.makeCrashReport(var12, "Colliding entity with block");
                     CrashReportCategory var11 = var10.makeCategory("Block being collided with");
                     CrashReportCategory.addBlockInfo(var11, var4, var8);
                     throw new ReportedException(var10);
                  }
               }
            }
         }
      }

      var2.release();
      var3.release();
      var4.release();
   }

   protected void playStepSound(BlockPos var1, net.minecraft.block.Block var2) {
      SoundType var3 = var2.getSoundType();
      if (this.world.getBlockState(var1.up()).getBlock() == Blocks.SNOW_LAYER) {
         var3 = Blocks.SNOW_LAYER.getSoundType();
         this.playSound(var3.getStepSound(), var3.getVolume() * 0.15F, var3.getPitch());
      } else if (!var2.getDefaultState().getMaterial().isLiquid()) {
         this.playSound(var3.getStepSound(), var3.getVolume() * 0.15F, var3.getPitch());
      }

   }

   public void playSound(SoundEvent var1, float var2, float var3) {
      if (!this.isSilent()) {
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, var1, this.getSoundCategory(), var2, var3);
      }

   }

   public boolean isSilent() {
      return ((Boolean)this.dataManager.get(SILENT)).booleanValue();
   }

   public void setSilent(boolean var1) {
      this.dataManager.set(SILENT, Boolean.valueOf(var1));
   }

   public boolean hasNoGravity() {
      return ((Boolean)this.dataManager.get(NO_GRAVITY)).booleanValue();
   }

   public void setNoGravity(boolean var1) {
      this.dataManager.set(NO_GRAVITY, Boolean.valueOf(var1));
   }

   protected boolean canTriggerWalking() {
      return true;
   }

   protected void updateFallState(double var1, boolean var3, IBlockState var4, BlockPos var5) {
      if (var3) {
         if (this.fallDistance > 0.0F) {
            var4.getBlock().onFallenUpon(this.world, var5, this, this.fallDistance);
         }

         this.fallDistance = 0.0F;
      } else if (var1 < 0.0D) {
         this.fallDistance = (float)((double)this.fallDistance - var1);
      }

   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox() {
      return null;
   }

   protected void burn(float var1) {
      if (!this.isImmuneToFire) {
         this.attackEntityFrom(DamageSource.inFire, var1);
      }

   }

   public final boolean isImmuneToFire() {
      return this.isImmuneToFire;
   }

   public void fall(float var1, float var2) {
      if (this.isBeingRidden()) {
         for(Entity var4 : this.getPassengers()) {
            var4.fall(var1, var2);
         }
      }

   }

   public boolean isWet() {
      if (this.inWater) {
         return true;
      } else {
         BlockPos.PooledMutableBlockPos var1 = BlockPos.PooledMutableBlockPos.retain(this.posX, this.posY, this.posZ);
         if (!this.world.isRainingAt(var1) && !this.world.isRainingAt(var1.setPos(this.posX, this.posY + (double)this.height, this.posZ))) {
            var1.release();
            return false;
         } else {
            var1.release();
            return true;
         }
      }
   }

   public boolean isInWater() {
      return this.inWater;
   }

   public boolean handleWaterMovement() {
      if (this.getRidingEntity() instanceof EntityBoat) {
         this.inWater = false;
      } else if (this.world.handleMaterialAcceleration(this.getEntityBoundingBox().expand(0.0D, -0.4000000059604645D, 0.0D).contract(0.001D), Material.WATER, this)) {
         if (!this.inWater && !this.firstUpdate) {
            this.resetHeight();
         }

         this.fallDistance = 0.0F;
         this.inWater = true;
         this.fire = 0;
      } else {
         this.inWater = false;
      }

      return this.inWater;
   }

   protected void resetHeight() {
      float var1 = MathHelper.sqrt(this.motionX * this.motionX * 0.20000000298023224D + this.motionY * this.motionY + this.motionZ * this.motionZ * 0.20000000298023224D) * 0.2F;
      if (var1 > 1.0F) {
         var1 = 1.0F;
      }

      this.playSound(this.getSplashSound(), var1, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
      float var2 = (float)MathHelper.floor(this.getEntityBoundingBox().minY);

      for(int var3 = 0; (float)var3 < 1.0F + this.width * 20.0F; ++var3) {
         float var4 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
         float var5 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
         this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX + (double)var4, (double)(var2 + 1.0F), this.posZ + (double)var5, this.motionX, this.motionY - (double)(this.rand.nextFloat() * 0.2F), this.motionZ);
      }

      for(int var6 = 0; (float)var6 < 1.0F + this.width * 20.0F; ++var6) {
         float var7 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
         float var8 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
         this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX + (double)var7, (double)(var2 + 1.0F), this.posZ + (double)var8, this.motionX, this.motionY, this.motionZ);
      }

   }

   public void spawnRunningParticles() {
      if (this.isSprinting() && !this.isInWater()) {
         this.createRunningParticles();
      }

   }

   protected void createRunningParticles() {
      int var1 = MathHelper.floor(this.posX);
      int var2 = MathHelper.floor(this.posY - 0.20000000298023224D);
      int var3 = MathHelper.floor(this.posZ);
      BlockPos var4 = new BlockPos(var1, var2, var3);
      IBlockState var5 = this.world.getBlockState(var4);
      if (var5.getRenderType() != EnumBlockRenderType.INVISIBLE) {
         this.world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX + ((double)this.rand.nextFloat() - 0.5D) * (double)this.width, this.getEntityBoundingBox().minY + 0.1D, this.posZ + ((double)this.rand.nextFloat() - 0.5D) * (double)this.width, -this.motionX * 4.0D, 1.5D, -this.motionZ * 4.0D, net.minecraft.block.Block.getStateId(var5));
      }

   }

   public boolean isInsideOfMaterial(Material var1) {
      if (this.getRidingEntity() instanceof EntityBoat) {
         return false;
      } else {
         double var2 = this.posY + (double)this.getEyeHeight();
         BlockPos var4 = new BlockPos(this.posX, var2, this.posZ);
         IBlockState var5 = this.world.getBlockState(var4);
         if (var5.getMaterial() == var1) {
            float var6 = BlockLiquid.getLiquidHeightPercent(var5.getBlock().getMetaFromState(var5)) - 0.11111111F;
            float var7 = (float)(var4.getY() + 1) - var6;
            boolean var8 = var2 < (double)var7;
            return !var8 && this instanceof EntityPlayer ? false : var8;
         } else {
            return false;
         }
      }
   }

   public boolean isInLava() {
      return this.world.isMaterialInBB(this.getEntityBoundingBox().expand(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D), Material.LAVA);
   }

   public void moveRelative(float var1, float var2, float var3) {
      float var4 = var1 * var1 + var2 * var2;
      if (var4 >= 1.0E-4F) {
         var4 = MathHelper.sqrt(var4);
         if (var4 < 1.0F) {
            var4 = 1.0F;
         }

         var4 = var3 / var4;
         var1 = var1 * var4;
         var2 = var2 * var4;
         float var5 = MathHelper.sin(this.rotationYaw * 0.017453292F);
         float var6 = MathHelper.cos(this.rotationYaw * 0.017453292F);
         this.motionX += (double)(var1 * var6 - var2 * var5);
         this.motionZ += (double)(var2 * var6 + var1 * var5);
      }

   }

   public float getBrightness(float var1) {
      BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos(MathHelper.floor(this.posX), 0, MathHelper.floor(this.posZ));
      if (this.world.isBlockLoaded(var2)) {
         var2.setY(MathHelper.floor(this.posY + (double)this.getEyeHeight()));
         return this.world.getLightBrightness(var2);
      } else {
         return 0.0F;
      }
   }

   public void setWorld(World var1) {
      if (var1 == null) {
         this.setDead();
         this.world = ((CraftWorld)Bukkit.getServer().getWorlds().get(0)).getHandle();
      } else {
         this.world = var1;
      }
   }

   public void setPositionAndRotation(double var1, double var3, double var5, float var7, float var8) {
      this.posX = MathHelper.clamp(var1, -3.0E7D, 3.0E7D);
      this.posY = var3;
      this.posZ = MathHelper.clamp(var5, -3.0E7D, 3.0E7D);
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      var8 = MathHelper.clamp(var8, -90.0F, 90.0F);
      this.rotationYaw = var7;
      this.rotationPitch = var8;
      this.prevRotationYaw = this.rotationYaw;
      this.prevRotationPitch = this.rotationPitch;
      double var9 = (double)(this.prevRotationYaw - var7);
      if (var9 < -180.0D) {
         this.prevRotationYaw += 360.0F;
      }

      if (var9 >= 180.0D) {
         this.prevRotationYaw -= 360.0F;
      }

      this.setPosition(this.posX, this.posY, this.posZ);
      this.setRotation(var7, var8);
   }

   public void moveToBlockPosAndAngles(BlockPos var1, float var2, float var3) {
      this.setLocationAndAngles((double)var1.getX() + 0.5D, (double)var1.getY(), (double)var1.getZ() + 0.5D, var2, var3);
   }

   public void setLocationAndAngles(double var1, double var3, double var5, float var7, float var8) {
      this.posX = var1;
      this.posY = var3;
      this.posZ = var5;
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      this.lastTickPosX = this.posX;
      this.lastTickPosY = this.posY;
      this.lastTickPosZ = this.posZ;
      this.rotationYaw = var7;
      this.rotationPitch = var8;
      this.setPosition(this.posX, this.posY, this.posZ);
   }

   public float getDistanceToEntity(Entity var1) {
      float var2 = (float)(this.posX - var1.posX);
      float var3 = (float)(this.posY - var1.posY);
      float var4 = (float)(this.posZ - var1.posZ);
      return MathHelper.sqrt(var2 * var2 + var3 * var3 + var4 * var4);
   }

   public double getDistanceSq(double var1, double var3, double var5) {
      double var7 = this.posX - var1;
      double var9 = this.posY - var3;
      double var11 = this.posZ - var5;
      return var7 * var7 + var9 * var9 + var11 * var11;
   }

   public double getDistanceSq(BlockPos var1) {
      return var1.distanceSq(this.posX, this.posY, this.posZ);
   }

   public double getDistanceSqToCenter(BlockPos var1) {
      return var1.distanceSqToCenter(this.posX, this.posY, this.posZ);
   }

   public double getDistance(double var1, double var3, double var5) {
      double var7 = this.posX - var1;
      double var9 = this.posY - var3;
      double var11 = this.posZ - var5;
      return (double)MathHelper.sqrt(var7 * var7 + var9 * var9 + var11 * var11);
   }

   public double getDistanceSqToEntity(Entity var1) {
      double var2 = this.posX - var1.posX;
      double var4 = this.posY - var1.posY;
      double var6 = this.posZ - var1.posZ;
      return var2 * var2 + var4 * var4 + var6 * var6;
   }

   public void onCollideWithPlayer(EntityPlayer var1) {
   }

   public void applyEntityCollision(Entity var1) {
      if (!this.isRidingSameEntity(var1) && !var1.noClip && !this.noClip) {
         double var2 = var1.posX - this.posX;
         double var4 = var1.posZ - this.posZ;
         double var6 = MathHelper.absMax(var2, var4);
         if (var6 >= 0.009999999776482582D) {
            var6 = (double)MathHelper.sqrt(var6);
            var2 = var2 / var6;
            var4 = var4 / var6;
            double var8 = 1.0D / var6;
            if (var8 > 1.0D) {
               var8 = 1.0D;
            }

            var2 = var2 * var8;
            var4 = var4 * var8;
            var2 = var2 * 0.05000000074505806D;
            var4 = var4 * 0.05000000074505806D;
            var2 = var2 * (double)(1.0F - this.entityCollisionReduction);
            var4 = var4 * (double)(1.0F - this.entityCollisionReduction);
            if (!this.isBeingRidden()) {
               this.addVelocity(-var2, 0.0D, -var4);
            }

            if (!var1.isBeingRidden()) {
               var1.addVelocity(var2, 0.0D, var4);
            }
         }
      }

   }

   public void addVelocity(double var1, double var3, double var5) {
      this.motionX += var1;
      this.motionY += var3;
      this.motionZ += var5;
      this.isAirBorne = true;
   }

   protected void setBeenAttacked() {
      this.velocityChanged = true;
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (this.isEntityInvulnerable(var1)) {
         return false;
      } else {
         this.setBeenAttacked();
         return false;
      }
   }

   public Vec3d getLook(float var1) {
      if (var1 == 1.0F) {
         return this.getVectorForRotation(this.rotationPitch, this.rotationYaw);
      } else {
         float var2 = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * var1;
         float var3 = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * var1;
         return this.getVectorForRotation(var2, var3);
      }
   }

   protected final Vec3d getVectorForRotation(float var1, float var2) {
      float var3 = MathHelper.cos(-var2 * 0.017453292F - 3.1415927F);
      float var4 = MathHelper.sin(-var2 * 0.017453292F - 3.1415927F);
      float var5 = -MathHelper.cos(-var1 * 0.017453292F);
      float var6 = MathHelper.sin(-var1 * 0.017453292F);
      return new Vec3d((double)(var4 * var5), (double)var6, (double)(var3 * var5));
   }

   public boolean canBeCollidedWith() {
      return false;
   }

   public boolean canBePushed() {
      return false;
   }

   public void addToPlayerScore(Entity var1, int var2) {
   }

   public boolean writeToNBTAtomically(NBTTagCompound var1) {
      String var2 = this.getEntityString();
      if (!this.isDead && var2 != null) {
         var1.setString("id", var2);
         this.writeToNBT(var1);
         return true;
      } else {
         return false;
      }
   }

   public boolean writeToNBTOptional(NBTTagCompound var1) {
      String var2 = this.getEntityString();
      if (!this.isDead && var2 != null && !this.isRiding()) {
         var1.setString("id", var2);
         this.writeToNBT(var1);
         return true;
      } else {
         return false;
      }
   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      try {
         var1.setTag("Pos", this.newDoubleNBTList(this.posX, this.posY, this.posZ));
         var1.setTag("Motion", this.newDoubleNBTList(this.motionX, this.motionY, this.motionZ));
         if (Float.isNaN(this.rotationYaw)) {
            this.rotationYaw = 0.0F;
         }

         if (Float.isNaN(this.rotationPitch)) {
            this.rotationPitch = 0.0F;
         }

         var1.setTag("Rotation", this.newFloatNBTList(this.rotationYaw, this.rotationPitch));
         var1.setFloat("FallDistance", this.fallDistance);
         var1.setShort("Fire", (short)this.fire);
         var1.setShort("Air", (short)this.getAir());
         var1.setBoolean("OnGround", this.onGround);
         var1.setInteger("Dimension", this.dimension);
         var1.setBoolean("Invulnerable", this.invulnerable);
         var1.setInteger("PortalCooldown", this.timeUntilPortal);
         var1.setUniqueId("UUID", this.getUniqueID());
         var1.setLong("WorldUUIDLeast", this.world.getSaveHandler().getUUID().getLeastSignificantBits());
         var1.setLong("WorldUUIDMost", this.world.getSaveHandler().getUUID().getMostSignificantBits());
         var1.setInteger("Bukkit.updateLevel", 2);
         if (this.getCustomNameTag() != null && !this.getCustomNameTag().isEmpty()) {
            var1.setString("CustomName", this.getCustomNameTag());
         }

         if (this.getAlwaysRenderNameTag()) {
            var1.setBoolean("CustomNameVisible", this.getAlwaysRenderNameTag());
         }

         this.cmdResultStats.writeStatsToNBT(var1);
         if (this.isSilent()) {
            var1.setBoolean("Silent", this.isSilent());
         }

         if (this.hasNoGravity()) {
            var1.setBoolean("NoGravity", this.hasNoGravity());
         }

         if (this.glowing) {
            var1.setBoolean("Glowing", this.glowing);
         }

         if (this.tags.size() > 0) {
            NBTTagList var2 = new NBTTagList();

            for(String var10 : this.tags) {
               var2.appendTag(new NBTTagString(var10));
            }

            var1.setTag("Tags", var2);
         }

         this.writeEntityToNBT(var1);
         if (this.isBeingRidden()) {
            NBTTagList var7 = new NBTTagList();

            for(Entity var11 : this.getPassengers()) {
               NBTTagCompound var5 = new NBTTagCompound();
               if (var11.writeToNBTAtomically(var5)) {
                  var7.appendTag(var5);
               }
            }

            if (!var7.hasNoTags()) {
               var1.setTag("Passengers", var7);
            }
         }

         return var1;
      } catch (Throwable var6) {
         CrashReport var3 = CrashReport.makeCrashReport(var6, "Saving entity NBT");
         CrashReportCategory var4 = var3.makeCategory("Entity being saved");
         this.addEntityCrashInfo(var4);
         throw new ReportedException(var3);
      }
   }

   public void readFromNBT(NBTTagCompound var1) {
      try {
         NBTTagList var2 = var1.getTagList("Pos", 6);
         NBTTagList var10 = var1.getTagList("Motion", 6);
         NBTTagList var11 = var1.getTagList("Rotation", 5);
         this.motionX = var10.getDoubleAt(0);
         this.motionY = var10.getDoubleAt(1);
         this.motionZ = var10.getDoubleAt(2);
         this.posX = var2.getDoubleAt(0);
         this.posY = var2.getDoubleAt(1);
         this.posZ = var2.getDoubleAt(2);
         this.lastTickPosX = this.posX;
         this.lastTickPosY = this.posY;
         this.lastTickPosZ = this.posZ;
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         this.rotationYaw = var11.getFloatAt(0);
         this.rotationPitch = var11.getFloatAt(1);
         this.prevRotationYaw = this.rotationYaw;
         this.prevRotationPitch = this.rotationPitch;
         this.setRotationYawHead(this.rotationYaw);
         this.setRenderYawOffset(this.rotationYaw);
         this.fallDistance = var1.getFloat("FallDistance");
         this.fire = var1.getShort("Fire");
         this.setAir(var1.getShort("Air"));
         this.onGround = var1.getBoolean("OnGround");
         if (var1.hasKey("Dimension")) {
            this.dimension = var1.getInteger("Dimension");
         }

         this.invulnerable = var1.getBoolean("Invulnerable");
         this.timeUntilPortal = var1.getInteger("PortalCooldown");
         if (var1.hasUniqueId("UUID")) {
            this.entityUniqueID = var1.getUniqueId("UUID");
            this.cachedUniqueIdString = this.entityUniqueID.toString();
         }

         this.setPosition(this.posX, this.posY, this.posZ);
         this.setRotation(this.rotationYaw, this.rotationPitch);
         if (var1.hasKey("CustomName", 8)) {
            this.setCustomNameTag(var1.getString("CustomName"));
         }

         this.setAlwaysRenderNameTag(var1.getBoolean("CustomNameVisible"));
         this.cmdResultStats.readStatsFromNBT(var1);
         this.setSilent(var1.getBoolean("Silent"));
         this.setNoGravity(var1.getBoolean("NoGravity"));
         this.setGlowing(var1.getBoolean("Glowing"));
         if (var1.hasKey("Tags", 9)) {
            this.tags.clear();
            NBTTagList var5 = var1.getTagList("Tags", 8);
            int var6 = Math.min(var5.tagCount(), 1024);

            for(int var7 = 0; var7 < var6; ++var7) {
               this.tags.add(var5.getStringTagAt(var7));
            }
         }

         this.readEntityFromNBT(var1);
         if (this.shouldSetPosAfterLoading()) {
            this.setPosition(this.posX, this.posY, this.posZ);
         }

         if (this instanceof EntityLivingBase) {
            EntityLivingBase var12 = (EntityLivingBase)this;
            if (var12 instanceof EntityTameable && !isLevelAtLeast(var1, 2) && !var1.getBoolean("PersistenceRequired")) {
               EntityLiving var14 = (EntityLiving)var12;
               var14.persistenceRequired = !var14.canDespawn();
            }
         }

         if (!(this.getBukkitEntity() instanceof Vehicle)) {
            if (Math.abs(this.motionX) > 10.0D) {
               this.motionX = 0.0D;
            }

            if (Math.abs(this.motionY) > 10.0D) {
               this.motionY = 0.0D;
            }

            if (Math.abs(this.motionZ) > 10.0D) {
               this.motionZ = 0.0D;
            }
         }

         if (this instanceof EntityPlayerMP) {
            Server var13 = Bukkit.getServer();
            Object var15 = null;
            String var17 = var1.getString("world");
            if (var1.hasKey("WorldUUIDMost") && var1.hasKey("WorldUUIDLeast")) {
               UUID var8 = new UUID(var1.getLong("WorldUUIDMost"), var1.getLong("WorldUUIDLeast"));
               var15 = var13.getWorld(var8);
            } else {
               var15 = var13.getWorld(var17);
            }

            if (var15 == null) {
               EntityPlayerMP var18 = (EntityPlayerMP)this;
               var15 = ((CraftServer)var13).getServer().getWorldServer(var18.dimension).getWorld();
            }

            this.setWorld(var15 == null ? null : ((CraftWorld)var15).getHandle());
         }

      } catch (Throwable var9) {
         CrashReport var3 = CrashReport.makeCrashReport(var9, "Loading entity NBT");
         CrashReportCategory var4 = var3.makeCategory("Entity being loaded");
         this.addEntityCrashInfo(var4);
         throw new ReportedException(var3);
      }
   }

   protected boolean shouldSetPosAfterLoading() {
      return true;
   }

   protected final String getEntityString() {
      return EntityList.getEntityString(this);
   }

   protected abstract void readEntityFromNBT(NBTTagCompound var1);

   protected abstract void writeEntityToNBT(NBTTagCompound var1);

   protected NBTTagList newDoubleNBTList(double... var1) {
      NBTTagList var2 = new NBTTagList();

      for(double var6 : var1) {
         var2.appendTag(new NBTTagDouble(var6));
      }

      return var2;
   }

   protected NBTTagList newFloatNBTList(float... var1) {
      NBTTagList var2 = new NBTTagList();

      for(float var6 : var1) {
         var2.appendTag(new NBTTagFloat(var6));
      }

      return var2;
   }

   public EntityItem dropItem(Item var1, int var2) {
      return this.dropItemWithOffset(var1, var2, 0.0F);
   }

   public EntityItem dropItemWithOffset(Item var1, int var2, float var3) {
      return this.entityDropItem(new ItemStack(var1, var2, 0), var3);
   }

   public EntityItem entityDropItem(ItemStack var1, float var2) {
      if (var1.stackSize != 0 && var1.getItem() != null) {
         if (this instanceof EntityLivingBase && !((EntityLivingBase)this).forceDrops) {
            ((EntityLivingBase)this).drops.add(CraftItemStack.asBukkitCopy(var1));
            return null;
         } else {
            EntityItem var3 = new EntityItem(this.world, this.posX, this.posY + (double)var2, this.posZ, var1);
            var3.setDefaultPickupDelay();
            this.world.spawnEntity(var3);
            return var3;
         }
      } else {
         return null;
      }
   }

   public boolean isEntityAlive() {
      return !this.isDead;
   }

   public boolean isEntityInsideOpaqueBlock() {
      if (this.noClip) {
         return false;
      } else {
         BlockPos.PooledMutableBlockPos var1 = BlockPos.PooledMutableBlockPos.retain();

         for(int var2 = 0; var2 < 8; ++var2) {
            int var3 = MathHelper.floor(this.posY + (double)(((float)((var2 >> 0) % 2) - 0.5F) * 0.1F) + (double)this.getEyeHeight());
            int var4 = MathHelper.floor(this.posX + (double)(((float)((var2 >> 1) % 2) - 0.5F) * this.width * 0.8F));
            int var5 = MathHelper.floor(this.posZ + (double)(((float)((var2 >> 2) % 2) - 0.5F) * this.width * 0.8F));
            if (var1.getX() != var4 || var1.getY() != var3 || var1.getZ() != var5) {
               var1.setPos(var4, var3, var5);
               if (this.world.getBlockState(var1).getBlock().causesSuffocation()) {
                  var1.release();
                  return true;
               }
            }
         }

         var1.release();
         return false;
      }
   }

   public boolean processInitialInteract(EntityPlayer var1, @Nullable ItemStack var2, EnumHand var3) {
      return false;
   }

   @Nullable
   public AxisAlignedBB getCollisionBox(Entity var1) {
      return null;
   }

   public void updateRidden() {
      Entity var1 = this.getRidingEntity();
      if (this.isRiding() && var1.isDead) {
         this.dismountRidingEntity();
      } else {
         this.motionX = 0.0D;
         this.motionY = 0.0D;
         this.motionZ = 0.0D;
         this.onUpdate();
         if (this.isRiding()) {
            var1.updatePassenger(this);
         }
      }

   }

   public void updatePassenger(Entity var1) {
      if (this.isPassenger(var1)) {
         var1.setPosition(this.posX, this.posY + this.getMountedYOffset() + var1.getYOffset(), this.posZ);
      }

   }

   public double getYOffset() {
      return 0.0D;
   }

   public double getMountedYOffset() {
      return (double)this.height * 0.75D;
   }

   public boolean startRiding(Entity var1) {
      return this.startRiding(var1, false);
   }

   public boolean startRiding(Entity var1, boolean var2) {
      if (var2 || this.canBeRidden(var1) && var1.canFitPassenger(this)) {
         if (this.isRiding()) {
            this.dismountRidingEntity();
         }

         this.ridingEntity = var1;
         this.ridingEntity.addPassenger(this);
         return true;
      } else {
         return false;
      }
   }

   protected boolean canBeRidden(Entity var1) {
      return this.rideCooldown <= 0;
   }

   public void removePassengers() {
      for(int var1 = this.riddenByEntities.size() - 1; var1 >= 0; --var1) {
         ((Entity)this.riddenByEntities.get(var1)).dismountRidingEntity();
      }

   }

   public void dismountRidingEntity() {
      if (this.ridingEntity != null) {
         Entity var1 = this.ridingEntity;
         this.ridingEntity = null;
         var1.removePassenger(this);
      }

   }

   protected void addPassenger(Entity var1) {
      if (var1.getRidingEntity() != this) {
         throw new IllegalStateException("Use x.startRiding(y), not y.addPassenger(x)");
      } else {
         Preconditions.checkState(!var1.riddenByEntities.contains(this), "Circular entity riding! %s %s", new Object[]{this, var1});
         CraftEntity var2 = (CraftEntity)var1.getBukkitEntity().getVehicle();
         Entity var3 = var2 == null ? null : var2.getHandle();
         if (this.getBukkitEntity() instanceof Vehicle && var1.getBukkitEntity() instanceof LivingEntity && var1.world.isChunkLoaded((int)var1.posX >> 4, (int)var1.posZ >> 4, false)) {
            VehicleEnterEvent var4 = new VehicleEnterEvent((Vehicle)this.getBukkitEntity(), var1.getBukkitEntity());
            Bukkit.getPluginManager().callEvent(var4);
            CraftEntity var5 = (CraftEntity)var1.getBukkitEntity().getVehicle();
            Entity var6 = var5 == null ? null : var5.getHandle();
            if (var4.isCancelled() || var6 != var3) {
               return;
            }
         }

         if (!this.world.isRemote && var1 instanceof EntityPlayer && !(this.getControllingPassenger() instanceof EntityPlayer)) {
            this.riddenByEntities.add(0, var1);
         } else {
            this.riddenByEntities.add(var1);
         }

      }
   }

   protected void removePassenger(Entity var1) {
      if (var1.getRidingEntity() == this) {
         throw new IllegalStateException("Use x.stopRiding(y), not y.removePassenger(x)");
      } else {
         CraftEntity var2 = (CraftEntity)var1.getBukkitEntity().getVehicle();
         Entity var3 = var2 == null ? null : var2.getHandle();
         if (this.getBukkitEntity() instanceof Vehicle && var1.getBukkitEntity() instanceof LivingEntity) {
            VehicleExitEvent var4 = new VehicleExitEvent((Vehicle)this.getBukkitEntity(), (LivingEntity)var1.getBukkitEntity());
            Bukkit.getPluginManager().callEvent(var4);
            CraftEntity var5 = (CraftEntity)var1.getBukkitEntity().getVehicle();
            Entity var6 = var5 == null ? null : var5.getHandle();
            if (var4.isCancelled() || var6 != var3) {
               return;
            }
         }

         this.riddenByEntities.remove(var1);
         var1.rideCooldown = 60;
      }
   }

   protected boolean canFitPassenger(Entity var1) {
      return this.getPassengers().size() < 1;
   }

   public float getCollisionBorderSize() {
      return 0.0F;
   }

   public Vec3d getLookVec() {
      return null;
   }

   public void setPortal(BlockPos var1) {
      if (this.timeUntilPortal > 0) {
         this.timeUntilPortal = this.getPortalCooldown();
      } else {
         if (!this.world.isRemote && !var1.equals(this.lastPortalPos)) {
            this.lastPortalPos = new BlockPos(var1);
            BlockPattern.PatternHelper var2 = Blocks.PORTAL.createPatternHelper(this.world, this.lastPortalPos);
            double var3 = var2.getForwards().getAxis() == EnumFacing.Axis.X ? (double)var2.getFrontTopLeft().getZ() : (double)var2.getFrontTopLeft().getX();
            double var5 = var2.getForwards().getAxis() == EnumFacing.Axis.X ? this.posZ : this.posX;
            var5 = Math.abs(MathHelper.pct(var5 - (double)(var2.getForwards().rotateY().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? 1 : 0), var3, var3 - (double)var2.getWidth()));
            double var7 = MathHelper.pct(this.posY - 1.0D, (double)var2.getFrontTopLeft().getY(), (double)(var2.getFrontTopLeft().getY() - var2.getHeight()));
            this.lastPortalVec = new Vec3d(var5, var7, 0.0D);
            this.teleportDirection = var2.getForwards();
         }

         this.inPortal = true;
      }

   }

   public int getPortalCooldown() {
      return 300;
   }

   public Iterable getHeldEquipment() {
      return this.emptyItemStackList;
   }

   public Iterable getArmorInventoryList() {
      return this.emptyItemStackList;
   }

   public Iterable getEquipmentAndArmor() {
      return Iterables.concat(this.getHeldEquipment(), this.getArmorInventoryList());
   }

   public void setItemStackToSlot(EntityEquipmentSlot var1, @Nullable ItemStack var2) {
   }

   public boolean isBurning() {
      boolean var1 = this.world != null && this.world.isRemote;
      return !this.isImmuneToFire && (this.fire > 0 || var1 && this.getFlag(0));
   }

   public boolean isRiding() {
      return this.getRidingEntity() != null;
   }

   public boolean isBeingRidden() {
      return !this.getPassengers().isEmpty();
   }

   public boolean isSneaking() {
      return this.getFlag(1);
   }

   public void setSneaking(boolean var1) {
      this.setFlag(1, var1);
   }

   public boolean isSprinting() {
      return this.getFlag(3);
   }

   public void setSprinting(boolean var1) {
      this.setFlag(3, var1);
   }

   public boolean isGlowing() {
      return this.glowing || this.world.isRemote && this.getFlag(6);
   }

   public void setGlowing(boolean var1) {
      this.glowing = var1;
      if (!this.world.isRemote) {
         this.setFlag(6, this.glowing);
      }

   }

   public boolean isInvisible() {
      return this.getFlag(5);
   }

   @Nullable
   public Team getTeam() {
      return this.world.getScoreboard().getPlayersTeam(this.getCachedUniqueIdString());
   }

   public boolean isOnSameTeam(Entity var1) {
      return this.isOnScoreboardTeam(var1.getTeam());
   }

   public boolean isOnScoreboardTeam(Team var1) {
      return this.getTeam() != null ? this.getTeam().isSameTeam(var1) : false;
   }

   public void setInvisible(boolean var1) {
      this.setFlag(5, var1);
   }

   public boolean getFlag(int var1) {
      return (((Byte)this.dataManager.get(FLAGS)).byteValue() & 1 << var1) != 0;
   }

   public void setFlag(int var1, boolean var2) {
      byte var3 = ((Byte)this.dataManager.get(FLAGS)).byteValue();
      if (var2) {
         this.dataManager.set(FLAGS, Byte.valueOf((byte)(var3 | 1 << var1)));
      } else {
         this.dataManager.set(FLAGS, Byte.valueOf((byte)(var3 & ~(1 << var1))));
      }

   }

   public int getAir() {
      return ((Integer)this.dataManager.get(AIR)).intValue();
   }

   public void setAir(int var1) {
      EntityAirChangeEvent var2 = new EntityAirChangeEvent(this.getBukkitEntity(), var1);
      if (!var2.isCancelled()) {
         this.dataManager.set(AIR, Integer.valueOf(var2.getAmount()));
      }
   }

   public void onStruckByLightning(EntityLightningBolt var1) {
      CraftEntity var2 = this.getBukkitEntity();
      CraftEntity var3 = var1.getBukkitEntity();
      PluginManager var4 = Bukkit.getPluginManager();
      if (var2 instanceof Hanging) {
         HangingBreakByEntityEvent var5 = new HangingBreakByEntityEvent((Hanging)var2, var3);
         var4.callEvent(var5);
         if (var5.isCancelled()) {
            return;
         }
      }

      if (!this.isImmuneToFire) {
         CraftEventFactory.entityDamage = var1;
         if (!this.attackEntityFrom(DamageSource.lightningBolt, 5.0F)) {
            CraftEventFactory.entityDamage = null;
         } else {
            ++this.fire;
            if (this.fire == 0) {
               EntityCombustByEntityEvent var6 = new EntityCombustByEntityEvent(var3, var2, 8);
               var4.callEvent(var6);
               if (!var6.isCancelled()) {
                  this.setFire(var6.getDuration());
               }
            }

         }
      }
   }

   public void onKillEntity(EntityLivingBase var1) {
   }

   protected boolean pushOutOfBlocks(double var1, double var3, double var5) {
      BlockPos var7 = new BlockPos(var1, var3, var5);
      double var8 = var1 - (double)var7.getX();
      double var10 = var3 - (double)var7.getY();
      double var12 = var5 - (double)var7.getZ();
      List var14 = this.world.getCollisionBoxes(this.getEntityBoundingBox());
      if (var14.isEmpty()) {
         return false;
      } else {
         EnumFacing var15 = EnumFacing.UP;
         double var16 = Double.MAX_VALUE;
         if (!this.world.isBlockFullCube(var7.west()) && var8 < var16) {
            var16 = var8;
            var15 = EnumFacing.WEST;
         }

         if (!this.world.isBlockFullCube(var7.east()) && 1.0D - var8 < var16) {
            var16 = 1.0D - var8;
            var15 = EnumFacing.EAST;
         }

         if (!this.world.isBlockFullCube(var7.north()) && var12 < var16) {
            var16 = var12;
            var15 = EnumFacing.NORTH;
         }

         if (!this.world.isBlockFullCube(var7.south()) && 1.0D - var12 < var16) {
            var16 = 1.0D - var12;
            var15 = EnumFacing.SOUTH;
         }

         if (!this.world.isBlockFullCube(var7.up()) && 1.0D - var10 < var16) {
            var16 = 1.0D - var10;
            var15 = EnumFacing.UP;
         }

         float var18 = this.rand.nextFloat() * 0.2F + 0.1F;
         float var19 = (float)var15.getAxisDirection().getOffset();
         if (var15.getAxis() == EnumFacing.Axis.X) {
            this.motionX += (double)(var19 * var18);
         } else if (var15.getAxis() == EnumFacing.Axis.Y) {
            this.motionY += (double)(var19 * var18);
         } else if (var15.getAxis() == EnumFacing.Axis.Z) {
            this.motionZ += (double)(var19 * var18);
         }

         return true;
      }
   }

   public void setInWeb() {
      this.isInWeb = true;
      this.fallDistance = 0.0F;
   }

   public String getName() {
      if (this.hasCustomName()) {
         return this.getCustomNameTag();
      } else {
         String var1 = EntityList.getEntityString(this);
         if (var1 == null) {
            var1 = "generic";
         }

         return I18n.translateToLocal("entity." + var1 + ".name");
      }
   }

   public Entity[] getParts() {
      return null;
   }

   public boolean isEntityEqual(Entity var1) {
      return this == var1;
   }

   public float getRotationYawHead() {
      return 0.0F;
   }

   public void setRotationYawHead(float var1) {
   }

   public void setRenderYawOffset(float var1) {
   }

   public boolean canBeAttackedWithItem() {
      return true;
   }

   public boolean hitByEntity(Entity var1) {
      return false;
   }

   public String toString() {
      return String.format("%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f]", this.getClass().getSimpleName(), this.getName(), this.entityId, this.world == null ? "~NULL~" : this.world.getWorldInfo().getWorldName(), this.posX, this.posY, this.posZ);
   }

   public boolean isEntityInvulnerable(DamageSource var1) {
      return this.invulnerable && var1 != DamageSource.outOfWorld && !var1.isCreativePlayer();
   }

   public void setEntityInvulnerable(boolean var1) {
      this.invulnerable = var1;
   }

   public void copyLocationAndAnglesFrom(Entity var1) {
      this.setLocationAndAngles(var1.posX, var1.posY, var1.posZ, var1.rotationYaw, var1.rotationPitch);
   }

   private void copyDataFromOld(Entity var1) {
      NBTTagCompound var2 = var1.writeToNBT(new NBTTagCompound());
      var2.removeTag("Dimension");
      this.readFromNBT(var2);
      this.timeUntilPortal = var1.timeUntilPortal;
      this.lastPortalPos = var1.lastPortalPos;
      this.lastPortalVec = var1.lastPortalVec;
      this.teleportDirection = var1.teleportDirection;
   }

   @Nullable
   public Entity changeDimension(int var1) {
      if (!this.world.isRemote && !this.isDead) {
         this.world.theProfiler.startSection("changeDimension");
         MinecraftServer var2 = this.h();
         WorldServer var3 = null;
         if (this.dimension < 10) {
            for(WorldServer var5 : var2.worlds) {
               if (var5.dimension == var1) {
                  var3 = var5;
               }
            }
         }

         Object var11 = null;
         Location var10 = this.getBukkitEntity().getLocation();
         Location var6;
         if (var3 != null) {
            if (var11 != null) {
               var6 = new Location(var3.getWorld(), (double)((BlockPos)var11).getX(), (double)((BlockPos)var11).getY(), (double)((BlockPos)var11).getZ());
            } else {
               var6 = var2.getPlayerList().calculateTarget(var10, var2.getWorldServer(var1));
            }
         } else {
            var6 = null;
         }

         boolean var7 = var3 != null && (this.dimension != 1 || var3.dimension != 1);
         TravelAgent var8 = var6 != null ? (TravelAgent)((CraftWorld)var6.getWorld()).getHandle().getDefaultTeleporter() : CraftTravelAgent.DEFAULT;
         EntityPortalEvent var9 = new EntityPortalEvent(this.getBukkitEntity(), var10, var6, var8);
         var9.useTravelAgent(var7);
         var9.getEntity().getServer().getPluginManager().callEvent(var9);
         if (!var9.isCancelled() && var9.getTo() != null && var9.getTo().getWorld() != null && this.isEntityAlive()) {
            var6 = var9.useTravelAgent() ? var9.getPortalTravelAgent().findOrCreate(var9.getTo()) : var9.getTo();
            return this.teleportTo(var6, true);
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public Entity teleportTo(Location var1, boolean var2) {
      WorldServer var3 = ((CraftWorld)this.getBukkitEntity().getLocation().getWorld()).getHandle();
      WorldServer var4 = ((CraftWorld)var1.getWorld()).getHandle();
      int var5 = var4.dimension;
      this.dimension = var5;
      this.world.removeEntity(this);
      this.isDead = false;
      this.world.theProfiler.startSection("reposition");
      var4.getMinecraftServer().getPlayerList().repositionEntity(this, var1, var2);
      this.world.theProfiler.endStartSection("reloading");
      Entity var6 = EntityList.createEntityByName(EntityList.getEntityString(this), var4);
      if (var6 != null) {
         var6.copyDataFromOld(this);
         boolean var7 = var6.forceSpawn;
         var6.forceSpawn = true;
         var4.spawnEntity(var6);
         var6.forceSpawn = var7;
         var4.updateEntityWithOptionalForce(var6, false);
         this.getBukkitEntity().setHandle(var6);
         var6.bukkitEntity = this.getBukkitEntity();
         if (this instanceof EntityLiving) {
            ((EntityLiving)this).clearLeashed(true, false);
         }
      }

      this.isDead = true;
      this.world.theProfiler.endSection();
      var3.resetUpdateEntityTick();
      var4.resetUpdateEntityTick();
      this.world.theProfiler.endSection();
      return var6;
   }

   public boolean isNonBoss() {
      return true;
   }

   public float getExplosionResistance(Explosion var1, World var2, BlockPos var3, IBlockState var4) {
      return var4.getBlock().getExplosionResistance(this);
   }

   public boolean verifyExplosion(Explosion var1, World var2, BlockPos var3, IBlockState var4, float var5) {
      return true;
   }

   public int getMaxFallHeight() {
      return 3;
   }

   public Vec3d getLastPortalVec() {
      return this.lastPortalVec;
   }

   public EnumFacing getTeleportDirection() {
      return this.teleportDirection;
   }

   public boolean doesEntityNotTriggerPressurePlate() {
      return false;
   }

   public void addEntityCrashInfo(CrashReportCategory var1) {
      var1.setDetail("Entity Type", new ICrashReportDetail() {
         public String call() throws Exception {
            return EntityList.getEntityString(Entity.this) + " (" + Entity.this.getClass().getCanonicalName() + ")";
         }

         public Object call() throws Exception {
            return this.call();
         }
      });
      var1.addCrashSection("Entity ID", Integer.valueOf(this.entityId));
      var1.setDetail("Entity Name", new ICrashReportDetail() {
         public String call() throws Exception {
            return Entity.this.getName();
         }

         public Object call() throws Exception {
            return this.call();
         }
      });
      var1.addCrashSection("Entity's Exact location", String.format("%.2f, %.2f, %.2f", this.posX, this.posY, this.posZ));
      var1.addCrashSection("Entity's Block location", CrashReportCategory.getCoordinateInfo(MathHelper.floor(this.posX), MathHelper.floor(this.posY), MathHelper.floor(this.posZ)));
      var1.addCrashSection("Entity's Momentum", String.format("%.2f, %.2f, %.2f", this.motionX, this.motionY, this.motionZ));
      var1.setDetail("Entity's Passengers", new ICrashReportDetail() {
         public String call() throws Exception {
            return Entity.this.getPassengers().toString();
         }

         public Object call() throws Exception {
            return this.call();
         }
      });
      var1.setDetail("Entity's Vehicle", new ICrashReportDetail() {
         public String call() throws Exception {
            return Entity.this.getRidingEntity().toString();
         }

         public Object call() throws Exception {
            return this.call();
         }
      });
   }

   public void setUniqueId(UUID var1) {
      this.entityUniqueID = var1;
      this.cachedUniqueIdString = this.entityUniqueID.toString();
   }

   public UUID getUniqueID() {
      return this.entityUniqueID;
   }

   public String getCachedUniqueIdString() {
      return this.cachedUniqueIdString;
   }

   public boolean isPushedByWater() {
      return true;
   }

   public ITextComponent getDisplayName() {
      TextComponentString var1 = new TextComponentString(ScorePlayerTeam.formatPlayerName(this.getTeam(), this.getName()));
      var1.getStyle().setHoverEvent(this.getHoverEvent());
      var1.getStyle().setInsertion(this.getCachedUniqueIdString());
      return var1;
   }

   public void setCustomNameTag(String var1) {
      if (var1.length() > 256) {
         var1 = var1.substring(0, 256);
      }

      this.dataManager.set(CUSTOM_NAME, var1);
   }

   public String getCustomNameTag() {
      return (String)this.dataManager.get(CUSTOM_NAME);
   }

   public boolean hasCustomName() {
      return !((String)this.dataManager.get(CUSTOM_NAME)).isEmpty();
   }

   public void setAlwaysRenderNameTag(boolean var1) {
      this.dataManager.set(CUSTOM_NAME_VISIBLE, Boolean.valueOf(var1));
   }

   public boolean getAlwaysRenderNameTag() {
      return ((Boolean)this.dataManager.get(CUSTOM_NAME_VISIBLE)).booleanValue();
   }

   public void setPositionAndUpdate(double var1, double var3, double var5) {
      this.isPositionDirty = true;
      this.setLocationAndAngles(var1, var3, var5, this.rotationYaw, this.rotationPitch);
      this.world.updateEntityWithOptionalForce(this, false);
   }

   public void notifyDataManagerChange(DataParameter var1) {
   }

   public EnumFacing getHorizontalFacing() {
      return EnumFacing.getHorizontal(MathHelper.floor((double)(this.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3);
   }

   public EnumFacing getAdjustedHorizontalFacing() {
      return this.getHorizontalFacing();
   }

   protected HoverEvent getHoverEvent() {
      NBTTagCompound var1 = new NBTTagCompound();
      String var2 = EntityList.getEntityString(this);
      var1.setString("id", this.getCachedUniqueIdString());
      if (var2 != null) {
         var1.setString("type", var2);
      }

      var1.setString("name", this.getName());
      return new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new TextComponentString(var1.toString()));
   }

   public boolean isSpectatedByPlayer(EntityPlayerMP var1) {
      return true;
   }

   public AxisAlignedBB getEntityBoundingBox() {
      return this.boundingBox;
   }

   public void setEntityBoundingBox(AxisAlignedBB var1) {
      double var2 = var1.minX;
      double var4 = var1.minY;
      double var6 = var1.minZ;
      double var8 = var1.maxX;
      double var10 = var1.maxY;
      double var12 = var1.maxZ;
      double var14 = var1.maxX - var1.minX;
      if (var14 < 0.0D) {
         var8 = var2;
      }

      if (var14 > 64.0D) {
         var8 = var2 + 64.0D;
      }

      var14 = var1.maxY - var1.minY;
      if (var14 < 0.0D) {
         var10 = var4;
      }

      if (var14 > 64.0D) {
         var10 = var4 + 64.0D;
      }

      var14 = var1.maxZ - var1.minZ;
      if (var14 < 0.0D) {
         var12 = var6;
      }

      if (var14 > 64.0D) {
         var12 = var6 + 64.0D;
      }

      this.boundingBox = new AxisAlignedBB(var2, var4, var6, var8, var10, var12);
   }

   public float getEyeHeight() {
      return this.height * 0.85F;
   }

   public boolean isOutsideBorder() {
      return this.isOutsideBorder;
   }

   public void setOutsideBorder(boolean var1) {
      this.isOutsideBorder = var1;
   }

   public boolean replaceItemInInventory(int var1, ItemStack var2) {
      return false;
   }

   public void sendMessage(ITextComponent var1) {
   }

   public boolean canUseCommand(int var1, String var2) {
      return true;
   }

   public BlockPos getPosition() {
      return new BlockPos(this.posX, this.posY + 0.5D, this.posZ);
   }

   public Vec3d getPositionVector() {
      return new Vec3d(this.posX, this.posY, this.posZ);
   }

   public World getEntityWorld() {
      return this.world;
   }

   public Entity getCommandSenderEntity() {
      return this;
   }

   public boolean sendCommandFeedback() {
      return false;
   }

   public void setCommandStat(CommandResultStats.Type var1, int var2) {
      if (this.world != null && !this.world.isRemote) {
         this.cmdResultStats.a(this.world.getMinecraftServer(), this, var1, var2);
      }

   }

   @Nullable
   public MinecraftServer h() {
      return this.world.getMinecraftServer();
   }

   public CommandResultStats getCommandStats() {
      return this.cmdResultStats;
   }

   public void setCommandStats(Entity var1) {
      this.cmdResultStats.addAllStats(var1.getCommandStats());
   }

   public EnumActionResult applyPlayerInteraction(EntityPlayer var1, Vec3d var2, @Nullable ItemStack var3, EnumHand var4) {
      return EnumActionResult.PASS;
   }

   public boolean isImmuneToExplosions() {
      return false;
   }

   protected void applyEnchantments(EntityLivingBase var1, Entity var2) {
      if (var2 instanceof EntityLivingBase) {
         EnchantmentHelper.applyThornEnchantments((EntityLivingBase)var2, var1);
      }

      EnchantmentHelper.applyArthropodEnchantments(var1, var2);
   }

   public void addTrackingPlayer(EntityPlayerMP var1) {
   }

   public void removeTrackingPlayer(EntityPlayerMP var1) {
   }

   public float getRotatedYaw(Rotation var1) {
      float var2 = MathHelper.wrapDegrees(this.rotationYaw);
      switch(Entity.SyntheticClass_1.a[var1.ordinal()]) {
      case 1:
         return var2 + 180.0F;
      case 2:
         return var2 + 270.0F;
      case 3:
         return var2 + 90.0F;
      default:
         return var2;
      }
   }

   public float getMirroredYaw(Mirror var1) {
      float var2 = MathHelper.wrapDegrees(this.rotationYaw);
      switch(Entity.SyntheticClass_1.b[var1.ordinal()]) {
      case 1:
         return -var2;
      case 2:
         return 180.0F - var2;
      default:
         return var2;
      }
   }

   public boolean ignoreItemEntityData() {
      return false;
   }

   public boolean setPositionNonDirty() {
      boolean var1 = this.isPositionDirty;
      this.isPositionDirty = false;
      return var1;
   }

   @Nullable
   public Entity getControllingPassenger() {
      return null;
   }

   public List getPassengers() {
      return (List)(this.riddenByEntities.isEmpty() ? Collections.emptyList() : Lists.newArrayList(this.riddenByEntities));
   }

   public boolean isPassenger(Entity var1) {
      for(Entity var3 : this.getPassengers()) {
         if (var3.equals(var1)) {
            return true;
         }
      }

      return false;
   }

   public Collection getRecursivePassengers() {
      HashSet var1 = Sets.newHashSet();
      this.getRecursivePassengersByType(Entity.class, var1);
      return var1;
   }

   public Collection getRecursivePassengersByType(Class var1) {
      HashSet var2 = Sets.newHashSet();
      this.getRecursivePassengersByType(var1, var2);
      return var2;
   }

   private void getRecursivePassengersByType(Class var1, Set var2) {
      for(Entity var4 : this.getPassengers()) {
         if (var1.isAssignableFrom(var4.getClass())) {
            var2.add(var4);
         }

         var4.getRecursivePassengersByType(var1, var2);
      }

   }

   public Entity getLowestRidingEntity() {
      Entity var1;
      for(var1 = this; var1.isRiding(); var1 = var1.getRidingEntity()) {
         ;
      }

      return var1;
   }

   public boolean isRidingSameEntity(Entity var1) {
      return this.getLowestRidingEntity() == var1.getLowestRidingEntity();
   }

   public boolean isRidingOrBeingRiddenBy(Entity var1) {
      for(Entity var3 : this.getPassengers()) {
         if (var3.equals(var1)) {
            return true;
         }

         if (var3.isRidingOrBeingRiddenBy(var1)) {
            return true;
         }
      }

      return false;
   }

   public boolean canPassengerSteer() {
      Entity var1 = this.getControllingPassenger();
      return var1 instanceof EntityPlayer ? ((EntityPlayer)var1).isUser() : !this.world.isRemote;
   }

   @Nullable
   public Entity getRidingEntity() {
      return this.ridingEntity;
   }

   public EnumPushReaction getPushReaction() {
      return EnumPushReaction.NORMAL;
   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.NEUTRAL;
   }

   static class SyntheticClass_1 {
      static final int[] a;
      static final int[] b = new int[Mirror.values().length];

      static {
         try {
            b[Mirror.LEFT_RIGHT.ordinal()] = 1;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            b[Mirror.FRONT_BACK.ordinal()] = 2;
         } catch (NoSuchFieldError var3) {
            ;
         }

         a = new int[Rotation.values().length];

         try {
            a[Rotation.CLOCKWISE_180.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            a[Rotation.COUNTERCLOCKWISE_90.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[Rotation.CLOCKWISE_90.ordinal()] = 3;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
