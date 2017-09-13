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
import org.bukkit.event.entity.EntityCombustEvent;
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

   static boolean isLevelAtLeast(NBTTagCompound tag, int level) {
      return tag.hasKey("Bukkit.updateLevel") && tag.getInteger("Bukkit.updateLevel") >= level;
   }

   public CraftEntity getBukkitEntity() {
      if (this.bukkitEntity == null) {
         this.bukkitEntity = CraftEntity.getEntity(this.world.getServer(), this);
      }

      return this.bukkitEntity;
   }

   public Entity(World world) {
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
      this.world = world;
      this.setPosition(0.0D, 0.0D, 0.0D);
      if (world != null) {
         this.dimension = world.provider.getDimensionType().getId();
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

   public void setEntityId(int i) {
      this.entityId = i;
   }

   public Set getTags() {
      return this.tags;
   }

   public boolean addTag(String s) {
      if (this.tags.size() >= 1024) {
         return false;
      } else {
         this.tags.add(s);
         return true;
      }
   }

   public boolean removeTag(String s) {
      return this.tags.remove(s);
   }

   public void onKillCommand() {
      this.setDead();
   }

   protected abstract void entityInit();

   public EntityDataManager getDataManager() {
      return this.dataManager;
   }

   public boolean equals(Object object) {
      return object instanceof Entity ? ((Entity)object).entityId == this.entityId : false;
   }

   public int hashCode() {
      return this.entityId;
   }

   public void setDead() {
      this.isDead = true;
   }

   public void setDropItemsWhenDead(boolean flag) {
   }

   public void setSize(float f, float f1) {
      if (f != this.width || f1 != this.height) {
         float f2 = this.width;
         this.width = f;
         this.height = f1;
         AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
         this.setEntityBoundingBox(new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.minX + (double)this.width, axisalignedbb.minY + (double)this.height, axisalignedbb.minZ + (double)this.width));
         if (this.width > f2 && !this.firstUpdate && !this.world.isRemote) {
            this.move((double)(f2 - this.width), 0.0D, (double)(f2 - this.width));
         }
      }

   }

   protected void setRotation(float f, float f1) {
      if (Float.isNaN(f)) {
         f = 0.0F;
      }

      if (f == Float.POSITIVE_INFINITY || f == Float.NEGATIVE_INFINITY) {
         if (this instanceof EntityPlayerMP) {
            this.world.getServer().getLogger().warning(this.getName() + " was caught trying to crash the server with an invalid yaw");
            ((CraftPlayer)this.getBukkitEntity()).kickPlayer("Nope");
         }

         f = 0.0F;
      }

      if (Float.isNaN(f1)) {
         f1 = 0.0F;
      }

      if (f1 == Float.POSITIVE_INFINITY || f1 == Float.NEGATIVE_INFINITY) {
         if (this instanceof EntityPlayerMP) {
            this.world.getServer().getLogger().warning(this.getName() + " was caught trying to crash the server with an invalid pitch");
            ((CraftPlayer)this.getBukkitEntity()).kickPlayer("Nope");
         }

         f1 = 0.0F;
      }

      this.rotationYaw = f % 360.0F;
      this.rotationPitch = f1 % 360.0F;
   }

   public void setPosition(double d0, double d1, double d2) {
      this.posX = d0;
      this.posY = d1;
      this.posZ = d2;
      float f = this.width / 2.0F;
      float f1 = this.height;
      this.setEntityBoundingBox(new AxisAlignedBB(d0 - (double)f, d1, d2 - (double)f, d0 + (double)f, d1 + (double)f1, d2 + (double)f));
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
            MinecraftServer minecraftserver = this.world.getMinecraftServer();
            if (!this.isRiding()) {
               int i = this.getMaxInPortalTime();
               if (this.portalCounter++ >= i) {
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
               Block damager = null;
               org.bukkit.entity.Entity damagee = this.getBukkitEntity();
               EntityCombustEvent combustEvent = new EntityCombustByBlockEvent(damager, damagee, 15);
               this.world.getServer().getPluginManager().callEvent(combustEvent);
               if (!combustEvent.isCancelled()) {
                  this.setFire(combustEvent.getDuration());
               }
            } else {
               this.setFire(15);
            }

            return;
         }

         this.setFire(15);
      }

   }

   public void setFire(int i) {
      int j = i * 20;
      if (this instanceof EntityLivingBase) {
         j = EnchantmentProtection.getFireTimeForEntity((EntityLivingBase)this, j);
      }

      if (this.fire < j) {
         this.fire = j;
      }

   }

   public void extinguish() {
      this.fire = 0;
   }

   protected void kill() {
      this.setDead();
   }

   public boolean isOffsetPositionInLiquid(double d0, double d1, double d2) {
      AxisAlignedBB axisalignedbb = this.getEntityBoundingBox().offset(d0, d1, d2);
      return this.isLiquidPresentInAABB(axisalignedbb);
   }

   private boolean isLiquidPresentInAABB(AxisAlignedBB axisalignedbb) {
      return this.world.getCollisionBoxes(this, axisalignedbb).isEmpty() && !this.world.containsAnyLiquid(axisalignedbb);
   }

   public void move(double d0, double d1, double d2) {
      if (this.noClip) {
         this.setEntityBoundingBox(this.getEntityBoundingBox().offset(d0, d1, d2));
         this.resetPositionToBB();
      } else {
         try {
            this.doBlockCollisions();
         } catch (Throwable var77) {
            CrashReport crashreport = CrashReport.makeCrashReport(var77, "Checking entity block collision");
            CrashReportCategory crashreportsystemdetails = crashreport.makeCategory("Entity being checked for collision");
            this.addEntityCrashInfo(crashreportsystemdetails);
            throw new ReportedException(crashreport);
         }

         if (d0 == 0.0D && d1 == 0.0D && d2 == 0.0D && this.isBeingRidden() && this.isRiding()) {
            return;
         }

         this.world.theProfiler.startSection("move");
         double d3 = this.posX;
         double d4 = this.posY;
         double d5 = this.posZ;
         if (this.isInWeb) {
            this.isInWeb = false;
            d0 *= 0.25D;
            d1 *= 0.05000000074505806D;
            d2 *= 0.25D;
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
         }

         double d6 = d0;
         double d7 = d1;
         double d8 = d2;
         boolean flag = this.onGround && this.isSneaking() && this instanceof EntityPlayer;
         if (flag) {
            for(; d0 != 0.0D && this.world.getCollisionBoxes(this, this.getEntityBoundingBox().offset(d0, -1.0D, 0.0D)).isEmpty(); d6 = d0) {
               if (d0 < 0.05D && d0 >= -0.05D) {
                  d0 = 0.0D;
               } else if (d0 > 0.0D) {
                  d0 -= 0.05D;
               } else {
                  d0 += 0.05D;
               }
            }

            for(; d2 != 0.0D && this.world.getCollisionBoxes(this, this.getEntityBoundingBox().offset(0.0D, -1.0D, d2)).isEmpty(); d8 = d2) {
               if (d2 < 0.05D && d2 >= -0.05D) {
                  d2 = 0.0D;
               } else if (d2 > 0.0D) {
                  d2 -= 0.05D;
               } else {
                  d2 += 0.05D;
               }
            }

            for(; d0 != 0.0D && d2 != 0.0D && this.world.getCollisionBoxes(this, this.getEntityBoundingBox().offset(d0, -1.0D, d2)).isEmpty(); d8 = d2) {
               if (d0 < 0.05D && d0 >= -0.05D) {
                  d0 = 0.0D;
               } else if (d0 > 0.0D) {
                  d0 -= 0.05D;
               } else {
                  d0 += 0.05D;
               }

               d6 = d0;
               if (d2 < 0.05D && d2 >= -0.05D) {
                  d2 = 0.0D;
               } else if (d2 > 0.0D) {
                  d2 -= 0.05D;
               } else {
                  d2 += 0.05D;
               }
            }
         }

         List list = this.world.getCollisionBoxes(this, this.getEntityBoundingBox().addCoord(d0, d1, d2));
         AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
         int i = 0;

         for(int j = list.size(); i < j; ++i) {
            d1 = ((AxisAlignedBB)list.get(i)).calculateYOffset(this.getEntityBoundingBox(), d1);
         }

         this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, d1, 0.0D));
         boolean flag1 = this.onGround || d7 != d1 && d7 < 0.0D;
         int var79 = 0;

         for(int k = list.size(); var79 < k; ++var79) {
            d0 = ((AxisAlignedBB)list.get(var79)).calculateXOffset(this.getEntityBoundingBox(), d0);
         }

         this.setEntityBoundingBox(this.getEntityBoundingBox().offset(d0, 0.0D, 0.0D));
         var79 = 0;

         for(int var82 = list.size(); var79 < var82; ++var79) {
            d2 = ((AxisAlignedBB)list.get(var79)).calculateZOffset(this.getEntityBoundingBox(), d2);
         }

         this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, 0.0D, d2));
         if (this.stepHeight > 0.0F && flag1 && (d6 != d0 || d8 != d2)) {
            double d11 = d0;
            double d12 = d1;
            double d13 = d2;
            AxisAlignedBB axisalignedbb1 = this.getEntityBoundingBox();
            this.setEntityBoundingBox(axisalignedbb);
            d1 = (double)this.stepHeight;
            List list1 = this.world.getCollisionBoxes(this, this.getEntityBoundingBox().addCoord(d6, d1, d8));
            AxisAlignedBB axisalignedbb2 = this.getEntityBoundingBox();
            AxisAlignedBB axisalignedbb3 = axisalignedbb2.addCoord(d6, 0.0D, d8);
            double d10 = d1;
            int l = 0;

            for(int i1 = list1.size(); l < i1; ++l) {
               d10 = ((AxisAlignedBB)list1.get(l)).calculateYOffset(axisalignedbb3, d10);
            }

            axisalignedbb2 = axisalignedbb2.offset(0.0D, d10, 0.0D);
            double d14 = d6;
            int j1 = 0;

            for(int k1 = list1.size(); j1 < k1; ++j1) {
               d14 = ((AxisAlignedBB)list1.get(j1)).calculateXOffset(axisalignedbb2, d14);
            }

            axisalignedbb2 = axisalignedbb2.offset(d14, 0.0D, 0.0D);
            double d15 = d8;
            int l1 = 0;

            for(int i2 = list1.size(); l1 < i2; ++l1) {
               d15 = ((AxisAlignedBB)list1.get(l1)).calculateZOffset(axisalignedbb2, d15);
            }

            axisalignedbb2 = axisalignedbb2.offset(0.0D, 0.0D, d15);
            AxisAlignedBB axisalignedbb4 = this.getEntityBoundingBox();
            double d16 = d1;
            int j2 = 0;

            for(int k2 = list1.size(); j2 < k2; ++j2) {
               d16 = ((AxisAlignedBB)list1.get(j2)).calculateYOffset(axisalignedbb4, d16);
            }

            axisalignedbb4 = axisalignedbb4.offset(0.0D, d16, 0.0D);
            double d17 = d6;
            int l2 = 0;

            for(int i3 = list1.size(); l2 < i3; ++l2) {
               d17 = ((AxisAlignedBB)list1.get(l2)).calculateXOffset(axisalignedbb4, d17);
            }

            axisalignedbb4 = axisalignedbb4.offset(d17, 0.0D, 0.0D);
            double d18 = d8;
            int j3 = 0;

            for(int k3 = list1.size(); j3 < k3; ++j3) {
               d18 = ((AxisAlignedBB)list1.get(j3)).calculateZOffset(axisalignedbb4, d18);
            }

            axisalignedbb4 = axisalignedbb4.offset(0.0D, 0.0D, d18);
            double d19 = d14 * d14 + d15 * d15;
            double d20 = d17 * d17 + d18 * d18;
            if (d19 > d20) {
               d0 = d14;
               d2 = d15;
               d1 = -d10;
               this.setEntityBoundingBox(axisalignedbb2);
            } else {
               d0 = d17;
               d2 = d18;
               d1 = -d16;
               this.setEntityBoundingBox(axisalignedbb4);
            }

            int l3 = 0;

            for(int i4 = list1.size(); l3 < i4; ++l3) {
               d1 = ((AxisAlignedBB)list1.get(l3)).calculateYOffset(this.getEntityBoundingBox(), d1);
            }

            this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, d1, 0.0D));
            if (d11 * d11 + d13 * d13 >= d0 * d0 + d2 * d2) {
               d0 = d11;
               d1 = d12;
               d2 = d13;
               this.setEntityBoundingBox(axisalignedbb1);
            }
         }

         this.world.theProfiler.endSection();
         this.world.theProfiler.startSection("rest");
         this.resetPositionToBB();
         this.isCollidedHorizontally = d6 != d0 || d8 != d2;
         this.isCollidedVertically = d7 != d1;
         this.onGround = this.isCollidedVertically && d7 < 0.0D;
         this.isCollided = this.isCollidedHorizontally || this.isCollidedVertically;
         var79 = MathHelper.floor(this.posX);
         int var83 = MathHelper.floor(this.posY - 0.20000000298023224D);
         int j4 = MathHelper.floor(this.posZ);
         BlockPos blockposition = new BlockPos(var79, var83, j4);
         IBlockState iblockdata = this.world.getBlockState(blockposition);
         if (iblockdata.getMaterial() == Material.AIR) {
            BlockPos blockposition1 = blockposition.down();
            IBlockState iblockdata1 = this.world.getBlockState(blockposition1);
            net.minecraft.block.Block block = iblockdata1.getBlock();
            if (block instanceof BlockFence || block instanceof BlockWall || block instanceof BlockFenceGate) {
               iblockdata = iblockdata1;
               blockposition = blockposition1;
            }
         }

         this.updateFallState(d1, this.onGround, iblockdata, blockposition);
         if (d6 != d0) {
            this.motionX = 0.0D;
         }

         if (d8 != d2) {
            this.motionZ = 0.0D;
         }

         net.minecraft.block.Block block1 = iblockdata.getBlock();
         if (d7 != d1) {
            block1.onLanded(this.world, this);
         }

         if (this.isCollidedHorizontally && this.getBukkitEntity() instanceof Vehicle) {
            Vehicle vehicle = (Vehicle)this.getBukkitEntity();
            Block bl = this.world.getWorld().getBlockAt(MathHelper.floor(this.posX), MathHelper.floor(this.posY), MathHelper.floor(this.posZ));
            if (d6 > d0) {
               bl = bl.getRelative(BlockFace.EAST);
            } else if (d6 < d0) {
               bl = bl.getRelative(BlockFace.WEST);
            } else if (d8 > d2) {
               bl = bl.getRelative(BlockFace.SOUTH);
            } else if (d8 < d2) {
               bl = bl.getRelative(BlockFace.NORTH);
            }

            if (bl.getType() != org.bukkit.Material.AIR) {
               VehicleBlockCollisionEvent event = new VehicleBlockCollisionEvent(vehicle, bl);
               this.world.getServer().getPluginManager().callEvent(event);
            }
         }

         if (this.canTriggerWalking() && !flag && !this.isRiding()) {
            double d21 = this.posX - d3;
            double d22 = this.posY - d4;
            double d10 = this.posZ - d5;
            if (block1 != Blocks.LADDER) {
               d22 = 0.0D;
            }

            if (block1 != null && this.onGround) {
               block1.onEntityWalk(this.world, blockposition, this);
            }

            this.distanceWalkedModified = (float)((double)this.distanceWalkedModified + (double)MathHelper.sqrt(d21 * d21 + d10 * d10) * 0.6D);
            this.distanceWalkedOnStepModified = (float)((double)this.distanceWalkedOnStepModified + (double)MathHelper.sqrt(d21 * d21 + d22 * d22 + d10 * d10) * 0.6D);
            if (this.distanceWalkedOnStepModified > (float)this.nextStepDistance && iblockdata.getMaterial() != Material.AIR) {
               this.nextStepDistance = (int)this.distanceWalkedOnStepModified + 1;
               if (this.isInWater()) {
                  float f = MathHelper.sqrt(this.motionX * this.motionX * 0.20000000298023224D + this.motionY * this.motionY + this.motionZ * this.motionZ * 0.20000000298023224D) * 0.35F;
                  if (f > 1.0F) {
                     f = 1.0F;
                  }

                  this.playSound(this.getSwimSound(), f, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
               }

               this.playStepSound(blockposition, block1);
            }
         }

         boolean flag2 = this.isWet();
         if (this.world.isFlammableWithin(this.getEntityBoundingBox().contract(0.001D))) {
            this.burn(1.0F);
            if (!flag2) {
               ++this.fire;
               if (this.fire == 0) {
                  EntityCombustEvent event = new EntityCombustByBlockEvent((Block)null, this.getBukkitEntity(), 8);
                  this.world.getServer().getPluginManager().callEvent(event);
                  if (!event.isCancelled()) {
                     this.setFire(event.getDuration());
                  }
               }
            }
         } else if (this.fire <= 0) {
            this.fire = -this.fireResistance;
         }

         if (flag2 && this.fire > 0) {
            this.playSound(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
            this.fire = -this.fireResistance;
         }

         this.world.theProfiler.endSection();
      }

   }

   public void resetPositionToBB() {
      AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
      this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
      this.posY = axisalignedbb.minY;
      this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
   }

   protected SoundEvent getSwimSound() {
      return SoundEvents.ENTITY_GENERIC_SWIM;
   }

   protected SoundEvent getSplashSound() {
      return SoundEvents.ENTITY_GENERIC_SPLASH;
   }

   protected void doBlockCollisions() {
      AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
      BlockPos.PooledMutableBlockPos blockposition_pooledblockposition = BlockPos.PooledMutableBlockPos.retain(axisalignedbb.minX + 0.001D, axisalignedbb.minY + 0.001D, axisalignedbb.minZ + 0.001D);
      BlockPos.PooledMutableBlockPos blockposition_pooledblockposition1 = BlockPos.PooledMutableBlockPos.retain(axisalignedbb.maxX - 0.001D, axisalignedbb.maxY - 0.001D, axisalignedbb.maxZ - 0.001D);
      BlockPos.PooledMutableBlockPos blockposition_pooledblockposition2 = BlockPos.PooledMutableBlockPos.retain();
      if (this.world.isAreaLoaded(blockposition_pooledblockposition, blockposition_pooledblockposition1)) {
         for(int i = blockposition_pooledblockposition.getX(); i <= blockposition_pooledblockposition1.getX(); ++i) {
            for(int j = blockposition_pooledblockposition.getY(); j <= blockposition_pooledblockposition1.getY(); ++j) {
               for(int k = blockposition_pooledblockposition.getZ(); k <= blockposition_pooledblockposition1.getZ(); ++k) {
                  blockposition_pooledblockposition2.setPos(i, j, k);
                  IBlockState iblockdata = this.world.getBlockState(blockposition_pooledblockposition2);

                  try {
                     iblockdata.getBlock().onEntityCollidedWithBlock(this.world, blockposition_pooledblockposition2, iblockdata, this);
                  } catch (Throwable var12) {
                     CrashReport crashreport = CrashReport.makeCrashReport(var12, "Colliding entity with block");
                     CrashReportCategory crashreportsystemdetails = crashreport.makeCategory("Block being collided with");
                     CrashReportCategory.addBlockInfo(crashreportsystemdetails, blockposition_pooledblockposition2, iblockdata);
                     throw new ReportedException(crashreport);
                  }
               }
            }
         }
      }

      blockposition_pooledblockposition.release();
      blockposition_pooledblockposition1.release();
      blockposition_pooledblockposition2.release();
   }

   protected void playStepSound(BlockPos blockposition, net.minecraft.block.Block block) {
      SoundType soundeffecttype = block.getSoundType();
      if (this.world.getBlockState(blockposition.up()).getBlock() == Blocks.SNOW_LAYER) {
         soundeffecttype = Blocks.SNOW_LAYER.getSoundType();
         this.playSound(soundeffecttype.getStepSound(), soundeffecttype.getVolume() * 0.15F, soundeffecttype.getPitch());
      } else if (!block.getDefaultState().getMaterial().isLiquid()) {
         this.playSound(soundeffecttype.getStepSound(), soundeffecttype.getVolume() * 0.15F, soundeffecttype.getPitch());
      }

   }

   public void playSound(SoundEvent soundeffect, float f, float f1) {
      if (!this.isSilent()) {
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, soundeffect, this.getSoundCategory(), f, f1);
      }

   }

   public boolean isSilent() {
      return ((Boolean)this.dataManager.get(SILENT)).booleanValue();
   }

   public void setSilent(boolean flag) {
      this.dataManager.set(SILENT, Boolean.valueOf(flag));
   }

   public boolean hasNoGravity() {
      return ((Boolean)this.dataManager.get(NO_GRAVITY)).booleanValue();
   }

   public void setNoGravity(boolean flag) {
      this.dataManager.set(NO_GRAVITY, Boolean.valueOf(flag));
   }

   protected boolean canTriggerWalking() {
      return true;
   }

   protected void updateFallState(double d0, boolean flag, IBlockState iblockdata, BlockPos blockposition) {
      if (flag) {
         if (this.fallDistance > 0.0F) {
            iblockdata.getBlock().onFallenUpon(this.world, blockposition, this, this.fallDistance);
         }

         this.fallDistance = 0.0F;
      } else if (d0 < 0.0D) {
         this.fallDistance = (float)((double)this.fallDistance - d0);
      }

   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox() {
      return null;
   }

   protected void burn(float i) {
      if (!this.isImmuneToFire) {
         this.attackEntityFrom(DamageSource.inFire, i);
      }

   }

   public final boolean isImmuneToFire() {
      return this.isImmuneToFire;
   }

   public void fall(float f, float f1) {
      if (this.isBeingRidden()) {
         for(Entity entity : this.getPassengers()) {
            entity.fall(f, f1);
         }
      }

   }

   public boolean isWet() {
      if (this.inWater) {
         return true;
      } else {
         BlockPos.PooledMutableBlockPos blockposition_pooledblockposition = BlockPos.PooledMutableBlockPos.retain(this.posX, this.posY, this.posZ);
         if (!this.world.isRainingAt(blockposition_pooledblockposition) && !this.world.isRainingAt(blockposition_pooledblockposition.setPos(this.posX, this.posY + (double)this.height, this.posZ))) {
            blockposition_pooledblockposition.release();
            return false;
         } else {
            blockposition_pooledblockposition.release();
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
      float f = MathHelper.sqrt(this.motionX * this.motionX * 0.20000000298023224D + this.motionY * this.motionY + this.motionZ * this.motionZ * 0.20000000298023224D) * 0.2F;
      if (f > 1.0F) {
         f = 1.0F;
      }

      this.playSound(this.getSplashSound(), f, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
      float f1 = (float)MathHelper.floor(this.getEntityBoundingBox().minY);

      for(int i = 0; (float)i < 1.0F + this.width * 20.0F; ++i) {
         float f2 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
         float f3 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
         this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX + (double)f2, (double)(f1 + 1.0F), this.posZ + (double)f3, this.motionX, this.motionY - (double)(this.rand.nextFloat() * 0.2F), this.motionZ);
      }

      for(int var6 = 0; (float)var6 < 1.0F + this.width * 20.0F; ++var6) {
         float f2 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
         float f3 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
         this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX + (double)f2, (double)(f1 + 1.0F), this.posZ + (double)f3, this.motionX, this.motionY, this.motionZ);
      }

   }

   public void spawnRunningParticles() {
      if (this.isSprinting() && !this.isInWater()) {
         this.createRunningParticles();
      }

   }

   protected void createRunningParticles() {
      int i = MathHelper.floor(this.posX);
      int j = MathHelper.floor(this.posY - 0.20000000298023224D);
      int k = MathHelper.floor(this.posZ);
      BlockPos blockposition = new BlockPos(i, j, k);
      IBlockState iblockdata = this.world.getBlockState(blockposition);
      if (iblockdata.getRenderType() != EnumBlockRenderType.INVISIBLE) {
         this.world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX + ((double)this.rand.nextFloat() - 0.5D) * (double)this.width, this.getEntityBoundingBox().minY + 0.1D, this.posZ + ((double)this.rand.nextFloat() - 0.5D) * (double)this.width, -this.motionX * 4.0D, 1.5D, -this.motionZ * 4.0D, net.minecraft.block.Block.getStateId(iblockdata));
      }

   }

   public boolean isInsideOfMaterial(Material material) {
      if (this.getRidingEntity() instanceof EntityBoat) {
         return false;
      } else {
         double d0 = this.posY + (double)this.getEyeHeight();
         BlockPos blockposition = new BlockPos(this.posX, d0, this.posZ);
         IBlockState iblockdata = this.world.getBlockState(blockposition);
         if (iblockdata.getMaterial() == material) {
            float f = BlockLiquid.getLiquidHeightPercent(iblockdata.getBlock().getMetaFromState(iblockdata)) - 0.11111111F;
            float f1 = (float)(blockposition.getY() + 1) - f;
            boolean flag = d0 < (double)f1;
            return !flag && this instanceof EntityPlayer ? false : flag;
         } else {
            return false;
         }
      }
   }

   public boolean isInLava() {
      return this.world.isMaterialInBB(this.getEntityBoundingBox().expand(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D), Material.LAVA);
   }

   public void moveRelative(float f, float f1, float f2) {
      float f3 = f * f + f1 * f1;
      if (f3 >= 1.0E-4F) {
         f3 = MathHelper.sqrt(f3);
         if (f3 < 1.0F) {
            f3 = 1.0F;
         }

         f3 = f2 / f3;
         f = f * f3;
         f1 = f1 * f3;
         float f4 = MathHelper.sin(this.rotationYaw * 0.017453292F);
         float f5 = MathHelper.cos(this.rotationYaw * 0.017453292F);
         this.motionX += (double)(f * f5 - f1 * f4);
         this.motionZ += (double)(f1 * f5 + f * f4);
      }

   }

   public float getBrightness(float f) {
      BlockPos.MutableBlockPos blockposition_mutableblockposition = new BlockPos.MutableBlockPos(MathHelper.floor(this.posX), 0, MathHelper.floor(this.posZ));
      if (this.world.isBlockLoaded(blockposition_mutableblockposition)) {
         blockposition_mutableblockposition.setY(MathHelper.floor(this.posY + (double)this.getEyeHeight()));
         return this.world.getLightBrightness(blockposition_mutableblockposition);
      } else {
         return 0.0F;
      }
   }

   public void setWorld(World world) {
      if (world == null) {
         this.setDead();
         this.world = ((CraftWorld)Bukkit.getServer().getWorlds().get(0)).getHandle();
      } else {
         this.world = world;
      }
   }

   public void setPositionAndRotation(double d0, double d1, double d2, float f, float f1) {
      this.posX = MathHelper.clamp(d0, -3.0E7D, 3.0E7D);
      this.posY = d1;
      this.posZ = MathHelper.clamp(d2, -3.0E7D, 3.0E7D);
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      f1 = MathHelper.clamp(f1, -90.0F, 90.0F);
      this.rotationYaw = f;
      this.rotationPitch = f1;
      this.prevRotationYaw = this.rotationYaw;
      this.prevRotationPitch = this.rotationPitch;
      double d3 = (double)(this.prevRotationYaw - f);
      if (d3 < -180.0D) {
         this.prevRotationYaw += 360.0F;
      }

      if (d3 >= 180.0D) {
         this.prevRotationYaw -= 360.0F;
      }

      this.setPosition(this.posX, this.posY, this.posZ);
      this.setRotation(f, f1);
   }

   public void moveToBlockPosAndAngles(BlockPos blockposition, float f, float f1) {
      this.setLocationAndAngles((double)blockposition.getX() + 0.5D, (double)blockposition.getY(), (double)blockposition.getZ() + 0.5D, f, f1);
   }

   public void setLocationAndAngles(double d0, double d1, double d2, float f, float f1) {
      this.posX = d0;
      this.posY = d1;
      this.posZ = d2;
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      this.lastTickPosX = this.posX;
      this.lastTickPosY = this.posY;
      this.lastTickPosZ = this.posZ;
      this.rotationYaw = f;
      this.rotationPitch = f1;
      this.setPosition(this.posX, this.posY, this.posZ);
   }

   public float getDistanceToEntity(Entity entity) {
      float f = (float)(this.posX - entity.posX);
      float f1 = (float)(this.posY - entity.posY);
      float f2 = (float)(this.posZ - entity.posZ);
      return MathHelper.sqrt(f * f + f1 * f1 + f2 * f2);
   }

   public double getDistanceSq(double d0, double d1, double d2) {
      double d3 = this.posX - d0;
      double d4 = this.posY - d1;
      double d5 = this.posZ - d2;
      return d3 * d3 + d4 * d4 + d5 * d5;
   }

   public double getDistanceSq(BlockPos blockposition) {
      return blockposition.distanceSq(this.posX, this.posY, this.posZ);
   }

   public double getDistanceSqToCenter(BlockPos blockposition) {
      return blockposition.distanceSqToCenter(this.posX, this.posY, this.posZ);
   }

   public double getDistance(double d0, double d1, double d2) {
      double d3 = this.posX - d0;
      double d4 = this.posY - d1;
      double d5 = this.posZ - d2;
      return (double)MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
   }

   public double getDistanceSqToEntity(Entity entity) {
      double d0 = this.posX - entity.posX;
      double d1 = this.posY - entity.posY;
      double d2 = this.posZ - entity.posZ;
      return d0 * d0 + d1 * d1 + d2 * d2;
   }

   public void onCollideWithPlayer(EntityPlayer entityhuman) {
   }

   public void applyEntityCollision(Entity entity) {
      if (!this.isRidingSameEntity(entity) && !entity.noClip && !this.noClip) {
         double d0 = entity.posX - this.posX;
         double d1 = entity.posZ - this.posZ;
         double d2 = MathHelper.absMax(d0, d1);
         if (d2 >= 0.009999999776482582D) {
            d2 = (double)MathHelper.sqrt(d2);
            d0 = d0 / d2;
            d1 = d1 / d2;
            double d3 = 1.0D / d2;
            if (d3 > 1.0D) {
               d3 = 1.0D;
            }

            d0 = d0 * d3;
            d1 = d1 * d3;
            d0 = d0 * 0.05000000074505806D;
            d1 = d1 * 0.05000000074505806D;
            d0 = d0 * (double)(1.0F - this.entityCollisionReduction);
            d1 = d1 * (double)(1.0F - this.entityCollisionReduction);
            if (!this.isBeingRidden()) {
               this.addVelocity(-d0, 0.0D, -d1);
            }

            if (!entity.isBeingRidden()) {
               entity.addVelocity(d0, 0.0D, d1);
            }
         }
      }

   }

   public void addVelocity(double d0, double d1, double d2) {
      this.motionX += d0;
      this.motionY += d1;
      this.motionZ += d2;
      this.isAirBorne = true;
   }

   protected void setBeenAttacked() {
      this.velocityChanged = true;
   }

   public boolean attackEntityFrom(DamageSource damagesource, float f) {
      if (this.isEntityInvulnerable(damagesource)) {
         return false;
      } else {
         this.setBeenAttacked();
         return false;
      }
   }

   public Vec3d getLook(float f) {
      if (f == 1.0F) {
         return this.getVectorForRotation(this.rotationPitch, this.rotationYaw);
      } else {
         float f1 = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * f;
         float f2 = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * f;
         return this.getVectorForRotation(f1, f2);
      }
   }

   protected final Vec3d getVectorForRotation(float f, float f1) {
      float f2 = MathHelper.cos(-f1 * 0.017453292F - 3.1415927F);
      float f3 = MathHelper.sin(-f1 * 0.017453292F - 3.1415927F);
      float f4 = -MathHelper.cos(-f * 0.017453292F);
      float f5 = MathHelper.sin(-f * 0.017453292F);
      return new Vec3d((double)(f3 * f4), (double)f5, (double)(f2 * f4));
   }

   public boolean canBeCollidedWith() {
      return false;
   }

   public boolean canBePushed() {
      return false;
   }

   public void addToPlayerScore(Entity entity, int i) {
   }

   public boolean writeToNBTAtomically(NBTTagCompound nbttagcompound) {
      String s = this.getEntityString();
      if (!this.isDead && s != null) {
         nbttagcompound.setString("id", s);
         this.writeToNBT(nbttagcompound);
         return true;
      } else {
         return false;
      }
   }

   public boolean writeToNBTOptional(NBTTagCompound nbttagcompound) {
      String s = this.getEntityString();
      if (!this.isDead && s != null && !this.isRiding()) {
         nbttagcompound.setString("id", s);
         this.writeToNBT(nbttagcompound);
         return true;
      } else {
         return false;
      }
   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
      try {
         nbttagcompound.setTag("Pos", this.newDoubleNBTList(this.posX, this.posY, this.posZ));
         nbttagcompound.setTag("Motion", this.newDoubleNBTList(this.motionX, this.motionY, this.motionZ));
         if (Float.isNaN(this.rotationYaw)) {
            this.rotationYaw = 0.0F;
         }

         if (Float.isNaN(this.rotationPitch)) {
            this.rotationPitch = 0.0F;
         }

         nbttagcompound.setTag("Rotation", this.newFloatNBTList(this.rotationYaw, this.rotationPitch));
         nbttagcompound.setFloat("FallDistance", this.fallDistance);
         nbttagcompound.setShort("Fire", (short)this.fire);
         nbttagcompound.setShort("Air", (short)this.getAir());
         nbttagcompound.setBoolean("OnGround", this.onGround);
         nbttagcompound.setInteger("Dimension", this.dimension);
         nbttagcompound.setBoolean("Invulnerable", this.invulnerable);
         nbttagcompound.setInteger("PortalCooldown", this.timeUntilPortal);
         nbttagcompound.setUniqueId("UUID", this.getUniqueID());
         nbttagcompound.setLong("WorldUUIDLeast", this.world.getSaveHandler().getUUID().getLeastSignificantBits());
         nbttagcompound.setLong("WorldUUIDMost", this.world.getSaveHandler().getUUID().getMostSignificantBits());
         nbttagcompound.setInteger("Bukkit.updateLevel", 2);
         if (this.getCustomNameTag() != null && !this.getCustomNameTag().isEmpty()) {
            nbttagcompound.setString("CustomName", this.getCustomNameTag());
         }

         if (this.getAlwaysRenderNameTag()) {
            nbttagcompound.setBoolean("CustomNameVisible", this.getAlwaysRenderNameTag());
         }

         this.cmdResultStats.writeStatsToNBT(nbttagcompound);
         if (this.isSilent()) {
            nbttagcompound.setBoolean("Silent", this.isSilent());
         }

         if (this.hasNoGravity()) {
            nbttagcompound.setBoolean("NoGravity", this.hasNoGravity());
         }

         if (this.glowing) {
            nbttagcompound.setBoolean("Glowing", this.glowing);
         }

         if (this.tags.size() > 0) {
            NBTTagList nbttaglist = new NBTTagList();

            for(String s : this.tags) {
               nbttaglist.appendTag(new NBTTagString(s));
            }

            nbttagcompound.setTag("Tags", nbttaglist);
         }

         this.writeEntityToNBT(nbttagcompound);
         if (this.isBeingRidden()) {
            NBTTagList nbttaglist = new NBTTagList();

            for(Entity entity : this.getPassengers()) {
               NBTTagCompound nbttagcompound1 = new NBTTagCompound();
               if (entity.writeToNBTAtomically(nbttagcompound1)) {
                  nbttaglist.appendTag(nbttagcompound1);
               }
            }

            if (!nbttaglist.hasNoTags()) {
               nbttagcompound.setTag("Passengers", nbttaglist);
            }
         }

         return nbttagcompound;
      } catch (Throwable var6) {
         CrashReport crashreport = CrashReport.makeCrashReport(var6, "Saving entity NBT");
         CrashReportCategory crashreportsystemdetails = crashreport.makeCategory("Entity being saved");
         this.addEntityCrashInfo(crashreportsystemdetails);
         throw new ReportedException(crashreport);
      }
   }

   public void readFromNBT(NBTTagCompound nbttagcompound) {
      try {
         NBTTagList nbttaglist = nbttagcompound.getTagList("Pos", 6);
         NBTTagList nbttaglist1 = nbttagcompound.getTagList("Motion", 6);
         NBTTagList nbttaglist2 = nbttagcompound.getTagList("Rotation", 5);
         this.motionX = nbttaglist1.getDoubleAt(0);
         this.motionY = nbttaglist1.getDoubleAt(1);
         this.motionZ = nbttaglist1.getDoubleAt(2);
         this.posX = nbttaglist.getDoubleAt(0);
         this.posY = nbttaglist.getDoubleAt(1);
         this.posZ = nbttaglist.getDoubleAt(2);
         this.lastTickPosX = this.posX;
         this.lastTickPosY = this.posY;
         this.lastTickPosZ = this.posZ;
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         this.rotationYaw = nbttaglist2.getFloatAt(0);
         this.rotationPitch = nbttaglist2.getFloatAt(1);
         this.prevRotationYaw = this.rotationYaw;
         this.prevRotationPitch = this.rotationPitch;
         this.setRotationYawHead(this.rotationYaw);
         this.setRenderYawOffset(this.rotationYaw);
         this.fallDistance = nbttagcompound.getFloat("FallDistance");
         this.fire = nbttagcompound.getShort("Fire");
         this.setAir(nbttagcompound.getShort("Air"));
         this.onGround = nbttagcompound.getBoolean("OnGround");
         if (nbttagcompound.hasKey("Dimension")) {
            this.dimension = nbttagcompound.getInteger("Dimension");
         }

         this.invulnerable = nbttagcompound.getBoolean("Invulnerable");
         this.timeUntilPortal = nbttagcompound.getInteger("PortalCooldown");
         if (nbttagcompound.hasUniqueId("UUID")) {
            this.entityUniqueID = nbttagcompound.getUniqueId("UUID");
            this.cachedUniqueIdString = this.entityUniqueID.toString();
         }

         this.setPosition(this.posX, this.posY, this.posZ);
         this.setRotation(this.rotationYaw, this.rotationPitch);
         if (nbttagcompound.hasKey("CustomName", 8)) {
            this.setCustomNameTag(nbttagcompound.getString("CustomName"));
         }

         this.setAlwaysRenderNameTag(nbttagcompound.getBoolean("CustomNameVisible"));
         this.cmdResultStats.readStatsFromNBT(nbttagcompound);
         this.setSilent(nbttagcompound.getBoolean("Silent"));
         this.setNoGravity(nbttagcompound.getBoolean("NoGravity"));
         this.setGlowing(nbttagcompound.getBoolean("Glowing"));
         if (nbttagcompound.hasKey("Tags", 9)) {
            this.tags.clear();
            NBTTagList nbttaglist3 = nbttagcompound.getTagList("Tags", 8);
            int i = Math.min(nbttaglist3.tagCount(), 1024);

            for(int j = 0; j < i; ++j) {
               this.tags.add(nbttaglist3.getStringTagAt(j));
            }
         }

         this.readEntityFromNBT(nbttagcompound);
         if (this.shouldSetPosAfterLoading()) {
            this.setPosition(this.posX, this.posY, this.posZ);
         }

         if (this instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase)this;
            if (entity instanceof EntityTameable && !isLevelAtLeast(nbttagcompound, 2) && !nbttagcompound.getBoolean("PersistenceRequired")) {
               EntityLiving entityinsentient = (EntityLiving)entity;
               entityinsentient.persistenceRequired = !entityinsentient.canDespawn();
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
            Server server = Bukkit.getServer();
            org.bukkit.World bworld = null;
            String worldName = nbttagcompound.getString("world");
            if (nbttagcompound.hasKey("WorldUUIDMost") && nbttagcompound.hasKey("WorldUUIDLeast")) {
               UUID uid = new UUID(nbttagcompound.getLong("WorldUUIDMost"), nbttagcompound.getLong("WorldUUIDLeast"));
               bworld = server.getWorld(uid);
            } else {
               bworld = server.getWorld(worldName);
            }

            if (bworld == null) {
               EntityPlayerMP entityPlayer = (EntityPlayerMP)this;
               bworld = ((CraftServer)server).getServer().getWorldServer(entityPlayer.dimension).getWorld();
            }

            this.setWorld(bworld == null ? null : ((CraftWorld)bworld).getHandle());
         }

      } catch (Throwable var9) {
         CrashReport crashreport = CrashReport.makeCrashReport(var9, "Loading entity NBT");
         CrashReportCategory crashreportsystemdetails = crashreport.makeCategory("Entity being loaded");
         this.addEntityCrashInfo(crashreportsystemdetails);
         throw new ReportedException(crashreport);
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

   protected NBTTagList newDoubleNBTList(double... adouble) {
      NBTTagList nbttaglist = new NBTTagList();

      for(double d0 : adouble) {
         nbttaglist.appendTag(new NBTTagDouble(d0));
      }

      return nbttaglist;
   }

   protected NBTTagList newFloatNBTList(float... afloat) {
      NBTTagList nbttaglist = new NBTTagList();

      for(float f : afloat) {
         nbttaglist.appendTag(new NBTTagFloat(f));
      }

      return nbttaglist;
   }

   public EntityItem dropItem(Item item, int i) {
      return this.dropItemWithOffset(item, i, 0.0F);
   }

   public EntityItem dropItemWithOffset(Item item, int i, float f) {
      return this.entityDropItem(new ItemStack(item, i, 0), f);
   }

   public EntityItem entityDropItem(ItemStack itemstack, float f) {
      if (itemstack.stackSize != 0 && itemstack.getItem() != null) {
         if (this instanceof EntityLivingBase && !((EntityLivingBase)this).forceDrops) {
            ((EntityLivingBase)this).drops.add(CraftItemStack.asBukkitCopy(itemstack));
            return null;
         } else {
            EntityItem entityitem = new EntityItem(this.world, this.posX, this.posY + (double)f, this.posZ, itemstack);
            entityitem.setDefaultPickupDelay();
            this.world.spawnEntity(entityitem);
            return entityitem;
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
         BlockPos.PooledMutableBlockPos blockposition_pooledblockposition = BlockPos.PooledMutableBlockPos.retain();

         for(int i = 0; i < 8; ++i) {
            int j = MathHelper.floor(this.posY + (double)(((float)((i >> 0) % 2) - 0.5F) * 0.1F) + (double)this.getEyeHeight());
            int k = MathHelper.floor(this.posX + (double)(((float)((i >> 1) % 2) - 0.5F) * this.width * 0.8F));
            int l = MathHelper.floor(this.posZ + (double)(((float)((i >> 2) % 2) - 0.5F) * this.width * 0.8F));
            if (blockposition_pooledblockposition.getX() != k || blockposition_pooledblockposition.getY() != j || blockposition_pooledblockposition.getZ() != l) {
               blockposition_pooledblockposition.setPos(k, j, l);
               if (this.world.getBlockState(blockposition_pooledblockposition).getBlock().causesSuffocation()) {
                  blockposition_pooledblockposition.release();
                  return true;
               }
            }
         }

         blockposition_pooledblockposition.release();
         return false;
      }
   }

   public boolean processInitialInteract(EntityPlayer entityhuman, @Nullable ItemStack itemstack, EnumHand enumhand) {
      return false;
   }

   @Nullable
   public AxisAlignedBB getCollisionBox(Entity entity) {
      return null;
   }

   public void updateRidden() {
      Entity entity = this.getRidingEntity();
      if (this.isRiding() && entity.isDead) {
         this.dismountRidingEntity();
      } else {
         this.motionX = 0.0D;
         this.motionY = 0.0D;
         this.motionZ = 0.0D;
         this.onUpdate();
         if (this.isRiding()) {
            entity.updatePassenger(this);
         }
      }

   }

   public void updatePassenger(Entity entity) {
      if (this.isPassenger(entity)) {
         entity.setPosition(this.posX, this.posY + this.getMountedYOffset() + entity.getYOffset(), this.posZ);
      }

   }

   public double getYOffset() {
      return 0.0D;
   }

   public double getMountedYOffset() {
      return (double)this.height * 0.75D;
   }

   public boolean startRiding(Entity entity) {
      return this.startRiding(entity, false);
   }

   public boolean startRiding(Entity entity, boolean flag) {
      if (flag || this.canBeRidden(entity) && entity.canFitPassenger(this)) {
         if (this.isRiding()) {
            this.dismountRidingEntity();
         }

         this.ridingEntity = entity;
         this.ridingEntity.addPassenger(this);
         return true;
      } else {
         return false;
      }
   }

   protected boolean canBeRidden(Entity entity) {
      return this.rideCooldown <= 0;
   }

   public void removePassengers() {
      for(int i = this.riddenByEntities.size() - 1; i >= 0; --i) {
         ((Entity)this.riddenByEntities.get(i)).dismountRidingEntity();
      }

   }

   public void dismountRidingEntity() {
      if (this.ridingEntity != null) {
         Entity entity = this.ridingEntity;
         this.ridingEntity = null;
         entity.removePassenger(this);
      }

   }

   protected void addPassenger(Entity entity) {
      if (entity.getRidingEntity() != this) {
         throw new IllegalStateException("Use x.startRiding(y), not y.addPassenger(x)");
      } else {
         Preconditions.checkState(!entity.riddenByEntities.contains(this), "Circular entity riding! %s %s", new Object[]{this, entity});
         CraftEntity craft = (CraftEntity)entity.getBukkitEntity().getVehicle();
         Entity orig = craft == null ? null : craft.getHandle();
         if (this.getBukkitEntity() instanceof Vehicle && entity.getBukkitEntity() instanceof LivingEntity && entity.world.isChunkLoaded((int)entity.posX >> 4, (int)entity.posZ >> 4, false)) {
            VehicleEnterEvent event = new VehicleEnterEvent((Vehicle)this.getBukkitEntity(), entity.getBukkitEntity());
            Bukkit.getPluginManager().callEvent(event);
            CraftEntity craftn = (CraftEntity)entity.getBukkitEntity().getVehicle();
            Entity n = craftn == null ? null : craftn.getHandle();
            if (event.isCancelled() || n != orig) {
               return;
            }
         }

         if (!this.world.isRemote && entity instanceof EntityPlayer && !(this.getControllingPassenger() instanceof EntityPlayer)) {
            this.riddenByEntities.add(0, entity);
         } else {
            this.riddenByEntities.add(entity);
         }

      }
   }

   protected void removePassenger(Entity entity) {
      if (entity.getRidingEntity() == this) {
         throw new IllegalStateException("Use x.stopRiding(y), not y.removePassenger(x)");
      } else {
         CraftEntity craft = (CraftEntity)entity.getBukkitEntity().getVehicle();
         Entity orig = craft == null ? null : craft.getHandle();
         if (this.getBukkitEntity() instanceof Vehicle && entity.getBukkitEntity() instanceof LivingEntity) {
            VehicleExitEvent event = new VehicleExitEvent((Vehicle)this.getBukkitEntity(), (LivingEntity)entity.getBukkitEntity());
            Bukkit.getPluginManager().callEvent(event);
            CraftEntity craftn = (CraftEntity)entity.getBukkitEntity().getVehicle();
            Entity n = craftn == null ? null : craftn.getHandle();
            if (event.isCancelled() || n != orig) {
               return;
            }
         }

         this.riddenByEntities.remove(entity);
         entity.rideCooldown = 60;
      }
   }

   protected boolean canFitPassenger(Entity entity) {
      return this.getPassengers().size() < 1;
   }

   public float getCollisionBorderSize() {
      return 0.0F;
   }

   public Vec3d getLookVec() {
      return null;
   }

   public void setPortal(BlockPos blockposition) {
      if (this.timeUntilPortal > 0) {
         this.timeUntilPortal = this.getPortalCooldown();
      } else {
         if (!this.world.isRemote && !blockposition.equals(this.lastPortalPos)) {
            this.lastPortalPos = new BlockPos(blockposition);
            BlockPattern.PatternHelper shapedetector_shapedetectorcollection = Blocks.PORTAL.createPatternHelper(this.world, this.lastPortalPos);
            double d0 = shapedetector_shapedetectorcollection.getForwards().getAxis() == EnumFacing.Axis.X ? (double)shapedetector_shapedetectorcollection.getFrontTopLeft().getZ() : (double)shapedetector_shapedetectorcollection.getFrontTopLeft().getX();
            double d1 = shapedetector_shapedetectorcollection.getForwards().getAxis() == EnumFacing.Axis.X ? this.posZ : this.posX;
            d1 = Math.abs(MathHelper.pct(d1 - (double)(shapedetector_shapedetectorcollection.getForwards().rotateY().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? 1 : 0), d0, d0 - (double)shapedetector_shapedetectorcollection.getWidth()));
            double d2 = MathHelper.pct(this.posY - 1.0D, (double)shapedetector_shapedetectorcollection.getFrontTopLeft().getY(), (double)(shapedetector_shapedetectorcollection.getFrontTopLeft().getY() - shapedetector_shapedetectorcollection.getHeight()));
            this.lastPortalVec = new Vec3d(d1, d2, 0.0D);
            this.teleportDirection = shapedetector_shapedetectorcollection.getForwards();
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

   public void setItemStackToSlot(EntityEquipmentSlot enumitemslot, @Nullable ItemStack itemstack) {
   }

   public boolean isBurning() {
      boolean flag = this.world != null && this.world.isRemote;
      return !this.isImmuneToFire && (this.fire > 0 || flag && this.getFlag(0));
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

   public void setSneaking(boolean flag) {
      this.setFlag(1, flag);
   }

   public boolean isSprinting() {
      return this.getFlag(3);
   }

   public void setSprinting(boolean flag) {
      this.setFlag(3, flag);
   }

   public boolean isGlowing() {
      return this.glowing || this.world.isRemote && this.getFlag(6);
   }

   public void setGlowing(boolean flag) {
      this.glowing = flag;
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

   public boolean isOnSameTeam(Entity entity) {
      return this.isOnScoreboardTeam(entity.getTeam());
   }

   public boolean isOnScoreboardTeam(Team scoreboardteambase) {
      return this.getTeam() != null ? this.getTeam().isSameTeam(scoreboardteambase) : false;
   }

   public void setInvisible(boolean flag) {
      this.setFlag(5, flag);
   }

   public boolean getFlag(int i) {
      return (((Byte)this.dataManager.get(FLAGS)).byteValue() & 1 << i) != 0;
   }

   public void setFlag(int i, boolean flag) {
      byte b0 = ((Byte)this.dataManager.get(FLAGS)).byteValue();
      if (flag) {
         this.dataManager.set(FLAGS, Byte.valueOf((byte)(b0 | 1 << i)));
      } else {
         this.dataManager.set(FLAGS, Byte.valueOf((byte)(b0 & ~(1 << i))));
      }

   }

   public int getAir() {
      return ((Integer)this.dataManager.get(AIR)).intValue();
   }

   public void setAir(int i) {
      EntityAirChangeEvent event = new EntityAirChangeEvent(this.getBukkitEntity(), i);
      if (!event.isCancelled()) {
         this.dataManager.set(AIR, Integer.valueOf(event.getAmount()));
      }
   }

   public void onStruckByLightning(EntityLightningBolt entitylightning) {
      org.bukkit.entity.Entity thisBukkitEntity = this.getBukkitEntity();
      org.bukkit.entity.Entity stormBukkitEntity = entitylightning.getBukkitEntity();
      PluginManager pluginManager = Bukkit.getPluginManager();
      if (thisBukkitEntity instanceof Hanging) {
         HangingBreakByEntityEvent hangingEvent = new HangingBreakByEntityEvent((Hanging)thisBukkitEntity, stormBukkitEntity);
         pluginManager.callEvent(hangingEvent);
         if (hangingEvent.isCancelled()) {
            return;
         }
      }

      if (!this.isImmuneToFire) {
         CraftEventFactory.entityDamage = entitylightning;
         if (!this.attackEntityFrom(DamageSource.lightningBolt, 5.0F)) {
            CraftEventFactory.entityDamage = null;
         } else {
            ++this.fire;
            if (this.fire == 0) {
               EntityCombustByEntityEvent entityCombustEvent = new EntityCombustByEntityEvent(stormBukkitEntity, thisBukkitEntity, 8);
               pluginManager.callEvent(entityCombustEvent);
               if (!entityCombustEvent.isCancelled()) {
                  this.setFire(entityCombustEvent.getDuration());
               }
            }

         }
      }
   }

   public void onKillEntity(EntityLivingBase entityliving) {
   }

   protected boolean pushOutOfBlocks(double d0, double d1, double d2) {
      BlockPos blockposition = new BlockPos(d0, d1, d2);
      double d3 = d0 - (double)blockposition.getX();
      double d4 = d1 - (double)blockposition.getY();
      double d5 = d2 - (double)blockposition.getZ();
      List list = this.world.getCollisionBoxes(this.getEntityBoundingBox());
      if (list.isEmpty()) {
         return false;
      } else {
         EnumFacing enumdirection = EnumFacing.UP;
         double d6 = Double.MAX_VALUE;
         if (!this.world.isBlockFullCube(blockposition.west()) && d3 < d6) {
            d6 = d3;
            enumdirection = EnumFacing.WEST;
         }

         if (!this.world.isBlockFullCube(blockposition.east()) && 1.0D - d3 < d6) {
            d6 = 1.0D - d3;
            enumdirection = EnumFacing.EAST;
         }

         if (!this.world.isBlockFullCube(blockposition.north()) && d5 < d6) {
            d6 = d5;
            enumdirection = EnumFacing.NORTH;
         }

         if (!this.world.isBlockFullCube(blockposition.south()) && 1.0D - d5 < d6) {
            d6 = 1.0D - d5;
            enumdirection = EnumFacing.SOUTH;
         }

         if (!this.world.isBlockFullCube(blockposition.up()) && 1.0D - d4 < d6) {
            d6 = 1.0D - d4;
            enumdirection = EnumFacing.UP;
         }

         float f = this.rand.nextFloat() * 0.2F + 0.1F;
         float f1 = (float)enumdirection.getAxisDirection().getOffset();
         if (enumdirection.getAxis() == EnumFacing.Axis.X) {
            this.motionX += (double)(f1 * f);
         } else if (enumdirection.getAxis() == EnumFacing.Axis.Y) {
            this.motionY += (double)(f1 * f);
         } else if (enumdirection.getAxis() == EnumFacing.Axis.Z) {
            this.motionZ += (double)(f1 * f);
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
         String s = EntityList.getEntityString(this);
         if (s == null) {
            s = "generic";
         }

         return I18n.translateToLocal("entity." + s + ".name");
      }
   }

   public Entity[] getParts() {
      return null;
   }

   public boolean isEntityEqual(Entity entity) {
      return this == entity;
   }

   public float getRotationYawHead() {
      return 0.0F;
   }

   public void setRotationYawHead(float f) {
   }

   public void setRenderYawOffset(float f) {
   }

   public boolean canBeAttackedWithItem() {
      return true;
   }

   public boolean hitByEntity(Entity entity) {
      return false;
   }

   public String toString() {
      return String.format("%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f]", this.getClass().getSimpleName(), this.getName(), this.entityId, this.world == null ? "~NULL~" : this.world.getWorldInfo().getWorldName(), this.posX, this.posY, this.posZ);
   }

   public boolean isEntityInvulnerable(DamageSource damagesource) {
      return this.invulnerable && damagesource != DamageSource.outOfWorld && !damagesource.isCreativePlayer();
   }

   public void setEntityInvulnerable(boolean flag) {
      this.invulnerable = flag;
   }

   public void copyLocationAndAnglesFrom(Entity entity) {
      this.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
   }

   private void copyDataFromOld(Entity entity) {
      NBTTagCompound nbttagcompound = entity.writeToNBT(new NBTTagCompound());
      nbttagcompound.removeTag("Dimension");
      this.readFromNBT(nbttagcompound);
      this.timeUntilPortal = entity.timeUntilPortal;
      this.lastPortalPos = entity.lastPortalPos;
      this.lastPortalVec = entity.lastPortalVec;
      this.teleportDirection = entity.teleportDirection;
   }

   @Nullable
   public Entity changeDimension(int i) {
      if (!this.world.isRemote && !this.isDead) {
         this.world.theProfiler.startSection("changeDimension");
         MinecraftServer minecraftserver = this.h();
         WorldServer exitWorld = null;
         if (this.dimension < 10) {
            for(WorldServer world : minecraftserver.worlds) {
               if (world.dimension == i) {
                  exitWorld = world;
               }
            }
         }

         BlockPos blockposition = null;
         Location enter = this.getBukkitEntity().getLocation();
         Location exit;
         if (exitWorld != null) {
            if (blockposition != null) {
               exit = new Location(exitWorld.getWorld(), (double)blockposition.getX(), (double)blockposition.getY(), (double)blockposition.getZ());
            } else {
               exit = minecraftserver.getPlayerList().calculateTarget(enter, minecraftserver.getWorldServer(i));
            }
         } else {
            exit = null;
         }

         boolean useTravelAgent = exitWorld != null && (this.dimension != 1 || exitWorld.dimension != 1);
         TravelAgent agent = exit != null ? (TravelAgent)((CraftWorld)exit.getWorld()).getHandle().getDefaultTeleporter() : CraftTravelAgent.DEFAULT;
         EntityPortalEvent event = new EntityPortalEvent(this.getBukkitEntity(), enter, exit, agent);
         event.useTravelAgent(useTravelAgent);
         event.getEntity().getServer().getPluginManager().callEvent(event);
         if (!event.isCancelled() && event.getTo() != null && event.getTo().getWorld() != null && this.isEntityAlive()) {
            exit = event.useTravelAgent() ? event.getPortalTravelAgent().findOrCreate(event.getTo()) : event.getTo();
            return this.teleportTo(exit, true);
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public Entity teleportTo(Location exit, boolean portal) {
      WorldServer worldserver = ((CraftWorld)this.getBukkitEntity().getLocation().getWorld()).getHandle();
      WorldServer worldserver1 = ((CraftWorld)exit.getWorld()).getHandle();
      int i = worldserver1.dimension;
      this.dimension = i;
      this.world.removeEntity(this);
      this.isDead = false;
      this.world.theProfiler.startSection("reposition");
      worldserver1.getMinecraftServer().getPlayerList().repositionEntity(this, exit, portal);
      this.world.theProfiler.endStartSection("reloading");
      Entity entity = EntityList.createEntityByName(EntityList.getEntityString(this), worldserver1);
      if (entity != null) {
         entity.copyDataFromOld(this);
         boolean flag = entity.forceSpawn;
         entity.forceSpawn = true;
         worldserver1.spawnEntity(entity);
         entity.forceSpawn = flag;
         worldserver1.updateEntityWithOptionalForce(entity, false);
         this.getBukkitEntity().setHandle(entity);
         entity.bukkitEntity = this.getBukkitEntity();
         if (this instanceof EntityLiving) {
            ((EntityLiving)this).clearLeashed(true, false);
         }
      }

      this.isDead = true;
      this.world.theProfiler.endSection();
      worldserver.resetUpdateEntityTick();
      worldserver1.resetUpdateEntityTick();
      this.world.theProfiler.endSection();
      return entity;
   }

   public boolean isNonBoss() {
      return true;
   }

   public float getExplosionResistance(Explosion explosion, World world, BlockPos blockposition, IBlockState iblockdata) {
      return iblockdata.getBlock().getExplosionResistance(this);
   }

   public boolean verifyExplosion(Explosion explosion, World world, BlockPos blockposition, IBlockState iblockdata, float f) {
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

   public void addEntityCrashInfo(CrashReportCategory crashreportsystemdetails) {
      crashreportsystemdetails.setDetail("Entity Type", new ICrashReportDetail() {
         public String call() throws Exception {
            return EntityList.getEntityString(Entity.this) + " (" + Entity.this.getClass().getCanonicalName() + ")";
         }

         public Object call() throws Exception {
            return this.call();
         }
      });
      crashreportsystemdetails.addCrashSection("Entity ID", Integer.valueOf(this.entityId));
      crashreportsystemdetails.setDetail("Entity Name", new ICrashReportDetail() {
         public String call() throws Exception {
            return Entity.this.getName();
         }

         public Object call() throws Exception {
            return this.call();
         }
      });
      crashreportsystemdetails.addCrashSection("Entity's Exact location", String.format("%.2f, %.2f, %.2f", this.posX, this.posY, this.posZ));
      crashreportsystemdetails.addCrashSection("Entity's Block location", CrashReportCategory.getCoordinateInfo(MathHelper.floor(this.posX), MathHelper.floor(this.posY), MathHelper.floor(this.posZ)));
      crashreportsystemdetails.addCrashSection("Entity's Momentum", String.format("%.2f, %.2f, %.2f", this.motionX, this.motionY, this.motionZ));
      crashreportsystemdetails.setDetail("Entity's Passengers", new ICrashReportDetail() {
         public String call() throws Exception {
            return Entity.this.getPassengers().toString();
         }

         public Object call() throws Exception {
            return this.call();
         }
      });
      crashreportsystemdetails.setDetail("Entity's Vehicle", new ICrashReportDetail() {
         public String call() throws Exception {
            return Entity.this.getRidingEntity().toString();
         }

         public Object call() throws Exception {
            return this.call();
         }
      });
   }

   public void setUniqueId(UUID uuid) {
      this.entityUniqueID = uuid;
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
      TextComponentString chatcomponenttext = new TextComponentString(ScorePlayerTeam.formatPlayerName(this.getTeam(), this.getName()));
      chatcomponenttext.getStyle().setHoverEvent(this.getHoverEvent());
      chatcomponenttext.getStyle().setInsertion(this.getCachedUniqueIdString());
      return chatcomponenttext;
   }

   public void setCustomNameTag(String s) {
      if (s.length() > 256) {
         s = s.substring(0, 256);
      }

      this.dataManager.set(CUSTOM_NAME, s);
   }

   public String getCustomNameTag() {
      return (String)this.dataManager.get(CUSTOM_NAME);
   }

   public boolean hasCustomName() {
      return !((String)this.dataManager.get(CUSTOM_NAME)).isEmpty();
   }

   public void setAlwaysRenderNameTag(boolean flag) {
      this.dataManager.set(CUSTOM_NAME_VISIBLE, Boolean.valueOf(flag));
   }

   public boolean getAlwaysRenderNameTag() {
      return ((Boolean)this.dataManager.get(CUSTOM_NAME_VISIBLE)).booleanValue();
   }

   public void setPositionAndUpdate(double d0, double d1, double d2) {
      this.isPositionDirty = true;
      this.setLocationAndAngles(d0, d1, d2, this.rotationYaw, this.rotationPitch);
      this.world.updateEntityWithOptionalForce(this, false);
   }

   public void notifyDataManagerChange(DataParameter datawatcherobject) {
   }

   public EnumFacing getHorizontalFacing() {
      return EnumFacing.getHorizontal(MathHelper.floor((double)(this.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3);
   }

   public EnumFacing getAdjustedHorizontalFacing() {
      return this.getHorizontalFacing();
   }

   protected HoverEvent getHoverEvent() {
      NBTTagCompound nbttagcompound = new NBTTagCompound();
      String s = EntityList.getEntityString(this);
      nbttagcompound.setString("id", this.getCachedUniqueIdString());
      if (s != null) {
         nbttagcompound.setString("type", s);
      }

      nbttagcompound.setString("name", this.getName());
      return new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new TextComponentString(nbttagcompound.toString()));
   }

   public boolean isSpectatedByPlayer(EntityPlayerMP entityplayer) {
      return true;
   }

   public AxisAlignedBB getEntityBoundingBox() {
      return this.boundingBox;
   }

   public void setEntityBoundingBox(AxisAlignedBB axisalignedbb) {
      double a = axisalignedbb.minX;
      double b = axisalignedbb.minY;
      double c = axisalignedbb.minZ;
      double d = axisalignedbb.maxX;
      double e = axisalignedbb.maxY;
      double f = axisalignedbb.maxZ;
      double len = axisalignedbb.maxX - axisalignedbb.minX;
      if (len < 0.0D) {
         d = a;
      }

      if (len > 64.0D) {
         d = a + 64.0D;
      }

      len = axisalignedbb.maxY - axisalignedbb.minY;
      if (len < 0.0D) {
         e = b;
      }

      if (len > 64.0D) {
         e = b + 64.0D;
      }

      len = axisalignedbb.maxZ - axisalignedbb.minZ;
      if (len < 0.0D) {
         f = c;
      }

      if (len > 64.0D) {
         f = c + 64.0D;
      }

      this.boundingBox = new AxisAlignedBB(a, b, c, d, e, f);
   }

   public float getEyeHeight() {
      return this.height * 0.85F;
   }

   public boolean isOutsideBorder() {
      return this.isOutsideBorder;
   }

   public void setOutsideBorder(boolean flag) {
      this.isOutsideBorder = flag;
   }

   public boolean replaceItemInInventory(int i, ItemStack itemstack) {
      return false;
   }

   public void sendMessage(ITextComponent ichatbasecomponent) {
   }

   public boolean canUseCommand(int i, String s) {
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

   public void setCommandStat(CommandResultStats.Type commandobjectiveexecutor_enumcommandresult, int i) {
      if (this.world != null && !this.world.isRemote) {
         this.cmdResultStats.a(this.world.getMinecraftServer(), this, commandobjectiveexecutor_enumcommandresult, i);
      }

   }

   @Nullable
   public MinecraftServer h() {
      return this.world.getMinecraftServer();
   }

   public CommandResultStats getCommandStats() {
      return this.cmdResultStats;
   }

   public void setCommandStats(Entity entity) {
      this.cmdResultStats.addAllStats(entity.getCommandStats());
   }

   public EnumActionResult applyPlayerInteraction(EntityPlayer entityhuman, Vec3d vec3d, @Nullable ItemStack itemstack, EnumHand enumhand) {
      return EnumActionResult.PASS;
   }

   public boolean isImmuneToExplosions() {
      return false;
   }

   protected void applyEnchantments(EntityLivingBase entityliving, Entity entity) {
      if (entity instanceof EntityLivingBase) {
         EnchantmentHelper.applyThornEnchantments((EntityLivingBase)entity, entityliving);
      }

      EnchantmentHelper.applyArthropodEnchantments(entityliving, entity);
   }

   public void addTrackingPlayer(EntityPlayerMP entityplayer) {
   }

   public void removeTrackingPlayer(EntityPlayerMP entityplayer) {
   }

   public float getRotatedYaw(Rotation enumblockrotation) {
      float f = MathHelper.wrapDegrees(this.rotationYaw);
      switch(Entity.SyntheticClass_1.a[enumblockrotation.ordinal()]) {
      case 1:
         return f + 180.0F;
      case 2:
         return f + 270.0F;
      case 3:
         return f + 90.0F;
      default:
         return f;
      }
   }

   public float getMirroredYaw(Mirror enumblockmirror) {
      float f = MathHelper.wrapDegrees(this.rotationYaw);
      switch(Entity.SyntheticClass_1.b[enumblockmirror.ordinal()]) {
      case 1:
         return -f;
      case 2:
         return 180.0F - f;
      default:
         return f;
      }
   }

   public boolean ignoreItemEntityData() {
      return false;
   }

   public boolean setPositionNonDirty() {
      boolean flag = this.isPositionDirty;
      this.isPositionDirty = false;
      return flag;
   }

   @Nullable
   public Entity getControllingPassenger() {
      return null;
   }

   public List getPassengers() {
      return (List)(this.riddenByEntities.isEmpty() ? Collections.emptyList() : Lists.newArrayList(this.riddenByEntities));
   }

   public boolean isPassenger(Entity entity) {
      for(Entity entity1 : this.getPassengers()) {
         if (entity1.equals(entity)) {
            return true;
         }
      }

      return false;
   }

   public Collection getRecursivePassengers() {
      HashSet hashset = Sets.newHashSet();
      this.getRecursivePassengersByType(Entity.class, hashset);
      return hashset;
   }

   public Collection getRecursivePassengersByType(Class oclass) {
      HashSet hashset = Sets.newHashSet();
      this.getRecursivePassengersByType(oclass, hashset);
      return hashset;
   }

   private void getRecursivePassengersByType(Class oclass, Set set) {
      for(Entity entity : this.getPassengers()) {
         if (oclass.isAssignableFrom(entity.getClass())) {
            set.add(entity);
         }

         entity.getRecursivePassengersByType(oclass, set);
      }

   }

   public Entity getLowestRidingEntity() {
      Entity entity;
      for(entity = this; entity.isRiding(); entity = entity.getRidingEntity()) {
         ;
      }

      return entity;
   }

   public boolean isRidingSameEntity(Entity entity) {
      return this.getLowestRidingEntity() == entity.getLowestRidingEntity();
   }

   public boolean isRidingOrBeingRiddenBy(Entity entity) {
      for(Entity entity1 : this.getPassengers()) {
         if (entity1.equals(entity)) {
            return true;
         }

         if (entity1.isRidingOrBeingRiddenBy(entity)) {
            return true;
         }
      }

      return false;
   }

   public boolean canPassengerSteer() {
      Entity entity = this.getControllingPassenger();
      return entity instanceof EntityPlayer ? ((EntityPlayer)entity).isUser() : !this.world.isRemote;
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
