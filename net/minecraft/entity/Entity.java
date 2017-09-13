package net.minecraft.entity;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
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
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMonsterPlacer;
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
import net.minecraft.server.MinecraftServer;
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
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.Explosion;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Entity implements ICommandSender, ICapabilitySerializable {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final AxisAlignedBB ZERO_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
   private static double renderDistanceWeight = 1.0D;
   private static int nextEntityID;
   private int entityId;
   public boolean preventEntitySpawning;
   private final List riddenByEntities;
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
   private int fire;
   protected boolean inWater;
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
   @SideOnly(Side.CLIENT)
   public long serverPosX;
   @SideOnly(Side.CLIENT)
   public long serverPosY;
   @SideOnly(Side.CLIENT)
   public long serverPosZ;
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
   protected boolean glowing;
   private final Set tags;
   private boolean isPositionDirty;
   private NBTTagCompound customEntityData;
   public boolean captureDrops = false;
   public ArrayList capturedDrops = new ArrayList();
   private CapabilityDispatcher capabilities;

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
         this.dimension = var1.provider.getDimension();
      }

      this.dataManager = new EntityDataManager(this);
      this.dataManager.register(FLAGS, Byte.valueOf((byte)0));
      this.dataManager.register(AIR, Integer.valueOf(300));
      this.dataManager.register(CUSTOM_NAME_VISIBLE, Boolean.valueOf(false));
      this.dataManager.register(CUSTOM_NAME, "");
      this.dataManager.register(SILENT, Boolean.valueOf(false));
      this.dataManager.register(NO_GRAVITY, Boolean.valueOf(false));
      this.entityInit();
      MinecraftForge.EVENT_BUS.post(new EntityConstructing(this));
      this.capabilities = ForgeEventFactory.gatherCapabilities(this);
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

   @SideOnly(Side.CLIENT)
   protected void preparePlayerToSpawn() {
      if (this.world != null) {
         while(true) {
            if (this.posY > 0.0D && this.posY < 256.0D) {
               this.setPosition(this.posX, this.posY, this.posZ);
               if (!this.world.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty()) {
                  ++this.posY;
                  continue;
               }
            }

            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
            this.rotationPitch = 0.0F;
            break;
         }
      }

   }

   public void setDead() {
      this.isDead = true;
   }

   public void setDropItemsWhenDead(boolean var1) {
   }

   protected void setSize(float var1, float var2) {
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

   @SideOnly(Side.CLIENT)
   public void turn(float var1, float var2) {
      float var3 = this.rotationPitch;
      float var4 = this.rotationYaw;
      this.rotationYaw = (float)((double)this.rotationYaw + (double)var1 * 0.15D);
      this.rotationPitch = (float)((double)this.rotationPitch - (double)var2 * 0.15D);
      this.rotationPitch = MathHelper.clamp(this.rotationPitch, -90.0F, 90.0F);
      this.prevRotationPitch += this.rotationPitch - var3;
      this.prevRotationYaw += this.rotationYaw - var4;
      if (this.ridingEntity != null) {
         this.ridingEntity.applyOrientationToEntity(this);
      }

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
            if (var1.getAllowNether()) {
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
            }
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
         this.world.theProfiler.startSection("move");
         double var7 = this.posX;
         double var9 = this.posY;
         double var11 = this.posZ;
         if (this.isInWeb) {
            this.isInWeb = false;
            var1 *= 0.25D;
            var3 *= 0.05000000074505806D;
            var5 *= 0.25D;
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
         }

         double var13 = var1;
         double var15 = var3;
         double var17 = var5;
         boolean var19 = this.onGround && this.isSneaking() && this instanceof EntityPlayer;
         if (var19) {
            for(double var20 = 0.05D; var1 != 0.0D && this.world.getCollisionBoxes(this, this.getEntityBoundingBox().offset(var1, -1.0D, 0.0D)).isEmpty(); var13 = var1) {
               if (var1 < 0.05D && var1 >= -0.05D) {
                  var1 = 0.0D;
               } else if (var1 > 0.0D) {
                  var1 -= 0.05D;
               } else {
                  var1 += 0.05D;
               }
            }

            for(; var5 != 0.0D && this.world.getCollisionBoxes(this, this.getEntityBoundingBox().offset(0.0D, -1.0D, var5)).isEmpty(); var17 = var5) {
               if (var5 < 0.05D && var5 >= -0.05D) {
                  var5 = 0.0D;
               } else if (var5 > 0.0D) {
                  var5 -= 0.05D;
               } else {
                  var5 += 0.05D;
               }
            }

            for(; var1 != 0.0D && var5 != 0.0D && this.world.getCollisionBoxes(this, this.getEntityBoundingBox().offset(var1, -1.0D, var5)).isEmpty(); var17 = var5) {
               if (var1 < 0.05D && var1 >= -0.05D) {
                  var1 = 0.0D;
               } else if (var1 > 0.0D) {
                  var1 -= 0.05D;
               } else {
                  var1 += 0.05D;
               }

               var13 = var1;
               if (var5 < 0.05D && var5 >= -0.05D) {
                  var5 = 0.0D;
               } else if (var5 > 0.0D) {
                  var5 -= 0.05D;
               } else {
                  var5 += 0.05D;
               }
            }
         }

         List var62 = this.world.getCollisionBoxes(this, this.getEntityBoundingBox().addCoord(var1, var3, var5));
         AxisAlignedBB var21 = this.getEntityBoundingBox();
         int var22 = 0;

         for(int var23 = var62.size(); var22 < var23; ++var22) {
            var3 = ((AxisAlignedBB)var62.get(var22)).calculateYOffset(this.getEntityBoundingBox(), var3);
         }

         this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, var3, 0.0D));
         boolean var63 = this.onGround || var15 != var3 && var15 < 0.0D;
         int var24 = 0;

         for(int var25 = var62.size(); var24 < var25; ++var24) {
            var1 = ((AxisAlignedBB)var62.get(var24)).calculateXOffset(this.getEntityBoundingBox(), var1);
         }

         this.setEntityBoundingBox(this.getEntityBoundingBox().offset(var1, 0.0D, 0.0D));
         var24 = 0;

         for(int var66 = var62.size(); var24 < var66; ++var24) {
            var5 = ((AxisAlignedBB)var62.get(var24)).calculateZOffset(this.getEntityBoundingBox(), var5);
         }

         this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, 0.0D, var5));
         if (this.stepHeight > 0.0F && var63 && (var13 != var1 || var17 != var5)) {
            double var67 = var1;
            double var27 = var3;
            double var29 = var5;
            AxisAlignedBB var31 = this.getEntityBoundingBox();
            this.setEntityBoundingBox(var21);
            var3 = (double)this.stepHeight;
            List var32 = this.world.getCollisionBoxes(this, this.getEntityBoundingBox().addCoord(var13, var3, var17));
            AxisAlignedBB var33 = this.getEntityBoundingBox();
            AxisAlignedBB var34 = var33.addCoord(var13, 0.0D, var17);
            double var35 = var3;
            int var37 = 0;

            for(int var38 = var32.size(); var37 < var38; ++var37) {
               var35 = ((AxisAlignedBB)var32.get(var37)).calculateYOffset(var34, var35);
            }

            var33 = var33.offset(0.0D, var35, 0.0D);
            double var82 = var13;
            int var40 = 0;

            for(int var41 = var32.size(); var40 < var41; ++var40) {
               var82 = ((AxisAlignedBB)var32.get(var40)).calculateXOffset(var33, var82);
            }

            var33 = var33.offset(var82, 0.0D, 0.0D);
            double var83 = var17;
            int var43 = 0;

            for(int var44 = var32.size(); var43 < var44; ++var43) {
               var83 = ((AxisAlignedBB)var32.get(var43)).calculateZOffset(var33, var83);
            }

            var33 = var33.offset(0.0D, 0.0D, var83);
            AxisAlignedBB var84 = this.getEntityBoundingBox();
            double var45 = var3;
            int var47 = 0;

            for(int var48 = var32.size(); var47 < var48; ++var47) {
               var45 = ((AxisAlignedBB)var32.get(var47)).calculateYOffset(var84, var45);
            }

            var84 = var84.offset(0.0D, var45, 0.0D);
            double var88 = var13;
            int var50 = 0;

            for(int var51 = var32.size(); var50 < var51; ++var50) {
               var88 = ((AxisAlignedBB)var32.get(var50)).calculateXOffset(var84, var88);
            }

            var84 = var84.offset(var88, 0.0D, 0.0D);
            double var89 = var17;
            int var53 = 0;

            for(int var54 = var32.size(); var53 < var54; ++var53) {
               var89 = ((AxisAlignedBB)var32.get(var53)).calculateZOffset(var84, var89);
            }

            var84 = var84.offset(0.0D, 0.0D, var89);
            double var90 = var82 * var82 + var83 * var83;
            double var56 = var88 * var88 + var89 * var89;
            if (var90 > var56) {
               var1 = var82;
               var5 = var83;
               var3 = -var35;
               this.setEntityBoundingBox(var33);
            } else {
               var1 = var88;
               var5 = var89;
               var3 = -var45;
               this.setEntityBoundingBox(var84);
            }

            int var58 = 0;

            for(int var59 = var32.size(); var58 < var59; ++var58) {
               var3 = ((AxisAlignedBB)var32.get(var58)).calculateYOffset(this.getEntityBoundingBox(), var3);
            }

            this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, var3, 0.0D));
            if (var67 * var67 + var29 * var29 >= var1 * var1 + var5 * var5) {
               var1 = var67;
               var3 = var27;
               var5 = var29;
               this.setEntityBoundingBox(var31);
            }
         }

         this.world.theProfiler.endSection();
         this.world.theProfiler.startSection("rest");
         this.resetPositionToBB();
         this.isCollidedHorizontally = var13 != var1 || var17 != var5;
         this.isCollidedVertically = var15 != var3;
         this.onGround = this.isCollidedVertically && var15 < 0.0D;
         this.isCollided = this.isCollidedHorizontally || this.isCollidedVertically;
         var24 = MathHelper.floor(this.posX);
         int var68 = MathHelper.floor(this.posY - 0.20000000298023224D);
         int var26 = MathHelper.floor(this.posZ);
         BlockPos var69 = new BlockPos(var24, var68, var26);
         IBlockState var28 = this.world.getBlockState(var69);
         if (var28.getMaterial() == Material.AIR) {
            BlockPos var70 = var69.down();
            IBlockState var30 = this.world.getBlockState(var70);
            Block var74 = var30.getBlock();
            if (var74 instanceof BlockFence || var74 instanceof BlockWall || var74 instanceof BlockFenceGate) {
               var28 = var30;
               var69 = var70;
            }
         }

         this.updateFallState(var3, this.onGround, var28, var69);
         if (var13 != var1) {
            this.motionX = 0.0D;
         }

         if (var17 != var5) {
            this.motionZ = 0.0D;
         }

         Block var71 = var28.getBlock();
         if (var15 != var3) {
            var71.onLanded(this.world, this);
         }

         if (this.canTriggerWalking() && !var19 && !this.isRiding()) {
            double var72 = this.posX - var7;
            double var76 = this.posY - var9;
            double var81 = this.posZ - var11;
            if (var71 != Blocks.LADDER) {
               var76 = 0.0D;
            }

            if (var71 != null && this.onGround) {
               var71.onEntityWalk(this.world, var69, this);
            }

            this.distanceWalkedModified = (float)((double)this.distanceWalkedModified + (double)MathHelper.sqrt(var72 * var72 + var81 * var81) * 0.6D);
            this.distanceWalkedOnStepModified = (float)((double)this.distanceWalkedOnStepModified + (double)MathHelper.sqrt(var72 * var72 + var76 * var76 + var81 * var81) * 0.6D);
            if (this.distanceWalkedOnStepModified > (float)this.nextStepDistance && var28.getMaterial() != Material.AIR) {
               this.nextStepDistance = (int)this.distanceWalkedOnStepModified + 1;
               if (this.isInWater()) {
                  float var36 = MathHelper.sqrt(this.motionX * this.motionX * 0.20000000298023224D + this.motionY * this.motionY + this.motionZ * this.motionZ * 0.20000000298023224D) * 0.35F;
                  if (var36 > 1.0F) {
                     var36 = 1.0F;
                  }

                  this.playSound(this.getSwimSound(), var36, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
               }

               this.playStepSound(var69, var71);
            }
         }

         try {
            this.doBlockCollisions();
         } catch (Throwable var60) {
            CrashReport var75 = CrashReport.makeCrashReport(var60, "Checking entity block collision");
            CrashReportCategory var77 = var75.makeCategory("Entity being checked for collision");
            this.addEntityCrashInfo(var77);
            throw new ReportedException(var75);
         }

         boolean var73 = this.isWet();
         if (this.world.isFlammableWithin(this.getEntityBoundingBox().contract(0.001D))) {
            this.dealFireDamage(1);
            if (!var73) {
               ++this.fire;
               if (this.fire == 0) {
                  this.setFire(8);
               }
            }
         } else if (this.fire <= 0) {
            this.fire = -this.fireResistance;
         }

         if (var73 && this.fire > 0) {
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

   protected void playStepSound(BlockPos var1, Block var2) {
      SoundType var3 = var2.getSoundType(this.world.getBlockState(var1), this.world, var1, this);
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

   protected void dealFireDamage(int var1) {
      if (!this.isImmuneToFire) {
         this.attackEntityFrom(DamageSource.inFire, (float)var1);
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
         this.world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX + ((double)this.rand.nextFloat() - 0.5D) * (double)this.width, this.getEntityBoundingBox().minY + 0.1D, this.posZ + ((double)this.rand.nextFloat() - 0.5D) * (double)this.width, -this.motionX * 4.0D, 1.5D, -this.motionZ * 4.0D, Block.getStateId(var5));
      }

   }

   public boolean isInsideOfMaterial(Material var1) {
      if (this.getRidingEntity() instanceof EntityBoat) {
         return false;
      } else {
         double var2 = this.posY + (double)this.getEyeHeight();
         BlockPos var4 = new BlockPos(this.posX, var2, this.posZ);
         IBlockState var5 = this.world.getBlockState(var4);
         Boolean var6 = var5.getBlock().isEntityInsideMaterial(this.world, var4, var5, this, var2, var1, true);
         if (var6 != null) {
            return var6.booleanValue();
         } else {
            return var5.getMaterial() == var1 ? ForgeHooks.isInsideOfMaterial(var1, this, var4) : false;
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

   @SideOnly(Side.CLIENT)
   public int getBrightnessForRender(float var1) {
      BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos(MathHelper.floor(this.posX), 0, MathHelper.floor(this.posZ));
      if (this.world.isBlockLoaded(var2)) {
         var2.setY(MathHelper.floor(this.posY + (double)this.getEyeHeight()));
         return this.world.getCombinedLight(var2, 0);
      } else {
         return 0;
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
      this.world = var1;
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

   @SideOnly(Side.CLIENT)
   public Vec3d getPositionEyes(float var1) {
      if (var1 == 1.0F) {
         return new Vec3d(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ);
      } else {
         double var2 = this.prevPosX + (this.posX - this.prevPosX) * (double)var1;
         double var4 = this.prevPosY + (this.posY - this.prevPosY) * (double)var1 + (double)this.getEyeHeight();
         double var6 = this.prevPosZ + (this.posZ - this.prevPosZ) * (double)var1;
         return new Vec3d(var2, var4, var6);
      }
   }

   @Nullable
   @SideOnly(Side.CLIENT)
   public RayTraceResult rayTrace(double var1, float var3) {
      Vec3d var4 = this.getPositionEyes(var3);
      Vec3d var5 = this.getLook(var3);
      Vec3d var6 = var4.addVector(var5.xCoord * var1, var5.yCoord * var1, var5.zCoord * var1);
      return this.world.rayTraceBlocks(var4, var6, false, false, true);
   }

   public boolean canBeCollidedWith() {
      return false;
   }

   public boolean canBePushed() {
      return false;
   }

   public void addToPlayerScore(Entity var1, int var2) {
   }

   @SideOnly(Side.CLIENT)
   public boolean isInRangeToRender3d(double var1, double var3, double var5) {
      double var7 = this.posX - var1;
      double var9 = this.posY - var3;
      double var11 = this.posZ - var5;
      double var13 = var7 * var7 + var9 * var9 + var11 * var11;
      return this.isInRangeToRenderDist(var13);
   }

   @SideOnly(Side.CLIENT)
   public boolean isInRangeToRenderDist(double var1) {
      double var3 = this.getEntityBoundingBox().getAverageEdgeLength();
      if (Double.isNaN(var3)) {
         var3 = 1.0D;
      }

      var3 = var3 * 64.0D * renderDistanceWeight;
      return var1 < var3 * var3;
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
         var1.setTag("Rotation", this.newFloatNBTList(this.rotationYaw, this.rotationPitch));
         var1.setFloat("FallDistance", this.fallDistance);
         var1.setShort("Fire", (short)this.fire);
         var1.setShort("Air", (short)this.getAir());
         var1.setBoolean("OnGround", this.onGround);
         var1.setInteger("Dimension", this.dimension);
         var1.setBoolean("Invulnerable", this.invulnerable);
         var1.setInteger("PortalCooldown", this.timeUntilPortal);
         var1.setUniqueId("UUID", this.getUniqueID());
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

         if (this.customEntityData != null) {
            var1.setTag("ForgeData", this.customEntityData);
         }

         if (this.capabilities != null) {
            var1.setTag("ForgeCaps", this.capabilities.serializeNBT());
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
         NBTTagList var9 = var1.getTagList("Motion", 6);
         NBTTagList var10 = var1.getTagList("Rotation", 5);
         this.motionX = var9.getDoubleAt(0);
         this.motionY = var9.getDoubleAt(1);
         this.motionZ = var9.getDoubleAt(2);
         if (Math.abs(this.motionX) > 10.0D) {
            this.motionX = 0.0D;
         }

         if (Math.abs(this.motionY) > 10.0D) {
            this.motionY = 0.0D;
         }

         if (Math.abs(this.motionZ) > 10.0D) {
            this.motionZ = 0.0D;
         }

         this.posX = var2.getDoubleAt(0);
         this.posY = var2.getDoubleAt(1);
         this.posZ = var2.getDoubleAt(2);
         this.lastTickPosX = this.posX;
         this.lastTickPosY = this.posY;
         this.lastTickPosZ = this.posZ;
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         this.rotationYaw = var10.getFloatAt(0);
         this.rotationPitch = var10.getFloatAt(1);
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
         if (var1.hasKey("ForgeData")) {
            this.customEntityData = var1.getCompoundTag("ForgeData");
         }

         if (this.capabilities != null && var1.hasKey("ForgeCaps")) {
            this.capabilities.deserializeNBT(var1.getCompoundTag("ForgeCaps"));
         }

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

      } catch (Throwable var8) {
         CrashReport var3 = CrashReport.makeCrashReport(var8, "Loading entity NBT");
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
         EntityItem var3 = new EntityItem(this.world, this.posX, this.posY + (double)var2, this.posZ, var1);
         var3.setDefaultPickupDelay();
         if (this.captureDrops) {
            this.capturedDrops.add(var3);
         } else {
            this.world.spawnEntity(var3);
         }

         return var3;
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

   @SideOnly(Side.CLIENT)
   public void applyOrientationToEntity(Entity var1) {
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
      if (!ForgeEventFactory.canMountEntity(this, var1, true)) {
         return false;
      } else if (var2 || this.canBeRidden(var1) && var1.canFitPassenger(this)) {
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
         if (!ForgeEventFactory.canMountEntity(this, var1, false)) {
            return;
         }

         this.ridingEntity = null;
         var1.removePassenger(this);
      }

   }

   protected void addPassenger(Entity var1) {
      if (var1.getRidingEntity() != this) {
         throw new IllegalStateException("Use x.startRiding(y), not y.addPassenger(x)");
      } else {
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
         this.riddenByEntities.remove(var1);
         var1.rideCooldown = 60;
      }
   }

   protected boolean canFitPassenger(Entity var1) {
      return this.getPassengers().size() < 1;
   }

   @SideOnly(Side.CLIENT)
   public void setPositionAndRotationDirect(double var1, double var3, double var5, float var7, float var8, int var9, boolean var10) {
      this.setPosition(var1, var3, var5);
      this.setRotation(var7, var8);
   }

   public float getCollisionBorderSize() {
      return 0.0F;
   }

   public Vec3d getLookVec() {
      return null;
   }

   @SideOnly(Side.CLIENT)
   public Vec2f getPitchYaw() {
      Vec2f var1 = new Vec2f(this.rotationPitch, this.rotationYaw);
      return var1;
   }

   @SideOnly(Side.CLIENT)
   public Vec3d getForward() {
      return Vec3d.fromPitchYawVector(this.getPitchYaw());
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

   @SideOnly(Side.CLIENT)
   public void setVelocity(double var1, double var3, double var5) {
      this.motionX = var1;
      this.motionY = var3;
      this.motionZ = var5;
   }

   @SideOnly(Side.CLIENT)
   public void handleStatusUpdate(byte var1) {
   }

   @SideOnly(Side.CLIENT)
   public void performHurtAnimation() {
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

   @SideOnly(Side.CLIENT)
   public boolean isInvisibleToPlayer(EntityPlayer var1) {
      if (var1.isSpectator()) {
         return false;
      } else {
         Team var2 = this.getTeam();
         return var2 != null && var1 != null && var1.getTeam() == var2 && var2.getSeeFriendlyInvisiblesEnabled() ? false : this.isInvisible();
      }
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

   protected boolean getFlag(int var1) {
      return (((Byte)this.dataManager.get(FLAGS)).byteValue() & 1 << var1) != 0;
   }

   protected void setFlag(int var1, boolean var2) {
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
      this.dataManager.set(AIR, Integer.valueOf(var1));
   }

   public void onStruckByLightning(EntityLightningBolt var1) {
      this.attackEntityFrom(DamageSource.lightningBolt, 5.0F);
      ++this.fire;
      if (this.fire == 0) {
         this.setFire(8);
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
      MinecraftForge.EVENT_BUS.post(new GetCollisionBoxesEvent(this.world, this, this.getEntityBoundingBox(), var14));
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
         if (!ForgeHooks.onTravelToDimension(this, var1)) {
            return null;
         } else {
            this.world.theProfiler.startSection("changeDimension");
            MinecraftServer var2 = this.getServer();
            int var3 = this.dimension;
            WorldServer var4 = var2.worldServerForDimension(var3);
            WorldServer var5 = var2.worldServerForDimension(var1);
            this.dimension = var1;
            if (var3 == 1 && var1 == 1) {
               var5 = var2.worldServerForDimension(0);
               this.dimension = 0;
            }

            this.world.removeEntity(this);
            this.isDead = false;
            this.world.theProfiler.startSection("reposition");
            BlockPos var6;
            if (var1 == 1) {
               var6 = var5.getSpawnCoordinate();
            } else {
               double var7 = this.posX;
               double var9 = this.posZ;
               double var11 = 8.0D;
               if (var1 == -1) {
                  var7 = MathHelper.clamp(var7 / 8.0D, var5.getWorldBorder().minX() + 16.0D, var5.getWorldBorder().maxX() - 16.0D);
                  var9 = MathHelper.clamp(var9 / 8.0D, var5.getWorldBorder().minZ() + 16.0D, var5.getWorldBorder().maxZ() - 16.0D);
               } else if (var1 == 0) {
                  var7 = MathHelper.clamp(var7 * 8.0D, var5.getWorldBorder().minX() + 16.0D, var5.getWorldBorder().maxX() - 16.0D);
                  var9 = MathHelper.clamp(var9 * 8.0D, var5.getWorldBorder().minZ() + 16.0D, var5.getWorldBorder().maxZ() - 16.0D);
               }

               var7 = (double)MathHelper.clamp((int)var7, -29999872, 29999872);
               var9 = (double)MathHelper.clamp((int)var9, -29999872, 29999872);
               float var13 = this.rotationYaw;
               this.setLocationAndAngles(var7, this.posY, var9, 90.0F, 0.0F);
               Teleporter var14 = var5.getDefaultTeleporter();
               var14.placeInExistingPortal(this, var13);
               var6 = new BlockPos(this);
            }

            var4.updateEntityWithOptionalForce(this, false);
            this.world.theProfiler.endStartSection("reloading");
            Entity var16 = EntityList.createEntityByName(EntityList.getEntityString(this), var5);
            if (var16 != null) {
               var16.copyDataFromOld(this);
               if (var3 == 1 && var1 == 1) {
                  BlockPos var8 = var5.getTopSolidOrLiquidBlock(var5.getSpawnPoint());
                  var16.moveToBlockPosAndAngles(var8, var16.rotationYaw, var16.rotationPitch);
               } else {
                  var16.moveToBlockPosAndAngles(var6, var16.rotationYaw, var16.rotationPitch);
               }

               boolean var17 = var16.forceSpawn;
               var16.forceSpawn = true;
               var5.spawnEntity(var16);
               var16.forceSpawn = var17;
               var5.updateEntityWithOptionalForce(var16, false);
            }

            this.isDead = true;
            this.world.theProfiler.endSection();
            var4.resetUpdateEntityTick();
            var5.resetUpdateEntityTick();
            this.world.theProfiler.endSection();
            return var16;
         }
      } else {
         return null;
      }
   }

   public boolean isNonBoss() {
      return true;
   }

   public float getExplosionResistance(Explosion var1, World var2, BlockPos var3, IBlockState var4) {
      return var4.getBlock().getExplosionResistance(var2, var3, this, var1);
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
      });
      var1.addCrashSection("Entity ID", Integer.valueOf(this.entityId));
      var1.setDetail("Entity Name", new ICrashReportDetail() {
         public String call() throws Exception {
            return Entity.this.getName();
         }
      });
      var1.addCrashSection("Entity's Exact location", String.format("%.2f, %.2f, %.2f", this.posX, this.posY, this.posZ));
      var1.addCrashSection("Entity's Block location", CrashReportCategory.getCoordinateInfo(MathHelper.floor(this.posX), MathHelper.floor(this.posY), MathHelper.floor(this.posZ)));
      var1.addCrashSection("Entity's Momentum", String.format("%.2f, %.2f, %.2f", this.motionX, this.motionY, this.motionZ));
      var1.setDetail("Entity's Passengers", new ICrashReportDetail() {
         public String call() throws Exception {
            return Entity.this.getPassengers().toString();
         }
      });
      var1.setDetail("Entity's Vehicle", new ICrashReportDetail() {
         public String call() throws Exception {
            return Entity.this.getRidingEntity().toString();
         }
      });
   }

   public void setUniqueId(UUID var1) {
      this.entityUniqueID = var1;
      this.cachedUniqueIdString = this.entityUniqueID.toString();
   }

   @SideOnly(Side.CLIENT)
   public boolean canRenderOnFire() {
      return this.isBurning();
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

   @SideOnly(Side.CLIENT)
   public static double getRenderDistanceWeight() {
      return renderDistanceWeight;
   }

   @SideOnly(Side.CLIENT)
   public static void setRenderDistanceWeight(double var0) {
      renderDistanceWeight = var0;
   }

   public ITextComponent getDisplayName() {
      TextComponentString var1 = new TextComponentString(ScorePlayerTeam.formatPlayerName(this.getTeam(), this.getName()));
      var1.getStyle().setHoverEvent(this.getHoverEvent());
      var1.getStyle().setInsertion(this.getCachedUniqueIdString());
      return var1;
   }

   public void setCustomNameTag(String var1) {
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

   @SideOnly(Side.CLIENT)
   public boolean getAlwaysRenderNameTagForRender() {
      return this.getAlwaysRenderNameTag();
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

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getRenderBoundingBox() {
      return this.getEntityBoundingBox();
   }

   public void setEntityBoundingBox(AxisAlignedBB var1) {
      this.boundingBox = var1;
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
         this.cmdResultStats.setCommandStatForSender(this.world.getMinecraftServer(), this, var1, var2);
      }

   }

   @Nullable
   public MinecraftServer getServer() {
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

   public NBTTagCompound getEntityData() {
      if (this.customEntityData == null) {
         this.customEntityData = new NBTTagCompound();
      }

      return this.customEntityData;
   }

   public boolean shouldRiderSit() {
      return true;
   }

   public ItemStack getPickedResult(RayTraceResult var1) {
      if (this instanceof EntityPainting) {
         return new ItemStack(Items.PAINTING);
      } else if (this instanceof EntityLeashKnot) {
         return new ItemStack(Items.LEAD);
      } else if (this instanceof EntityItemFrame) {
         ItemStack var4 = ((EntityItemFrame)this).getDisplayedItem();
         return var4 == null ? new ItemStack(Items.ITEM_FRAME) : var4.copy();
      } else if (this instanceof EntityMinecart) {
         return ((EntityMinecart)this).getCartItem();
      } else if (this instanceof EntityBoat) {
         return new ItemStack(((EntityBoat)this).getItemBoat());
      } else if (this instanceof EntityArmorStand) {
         return new ItemStack(Items.ARMOR_STAND);
      } else if (this instanceof EntityEnderCrystal) {
         return new ItemStack(Items.END_CRYSTAL);
      } else {
         String var2 = EntityList.getEntityString(this);
         if (EntityList.ENTITY_EGGS.containsKey(var2)) {
            ItemStack var3 = new ItemStack(Items.SPAWN_EGG);
            ItemMonsterPlacer.applyEntityIdToItemStack(var3, var2);
            return var3;
         } else {
            return null;
         }
      }
   }

   public UUID getPersistentID() {
      return this.entityUniqueID;
   }

   public final void resetEntityId() {
      this.entityId = nextEntityID++;
   }

   public boolean shouldRenderInPass(int var1) {
      return var1 == 0;
   }

   public boolean isCreatureType(EnumCreatureType var1, boolean var2) {
      return var2 && this instanceof EntityLiving && ((EntityLiving)this).isNoDespawnRequired() ? false : var1.getCreatureClass().isAssignableFrom(this.getClass());
   }

   public boolean canRiderInteract() {
      return false;
   }

   public boolean shouldDismountInWater(Entity var1) {
      return this instanceof EntityLivingBase;
   }

   public boolean hasCapability(Capability var1, EnumFacing var2) {
      if (this.getCapability(var1, var2) != null) {
         return true;
      } else {
         return this.capabilities == null ? false : this.capabilities.hasCapability(var1, var2);
      }
   }

   public Object getCapability(Capability var1, EnumFacing var2) {
      return this.capabilities == null ? null : this.capabilities.getCapability(var1, var2);
   }

   public void deserializeNBT(NBTTagCompound var1) {
      this.readFromNBT(var1);
   }

   public NBTTagCompound serializeNBT() {
      NBTTagCompound var1 = new NBTTagCompound();
      var1.setString("id", this.getEntityString());
      return this.writeToNBT(var1);
   }

   public void addTrackingPlayer(EntityPlayerMP var1) {
   }

   public void removeTrackingPlayer(EntityPlayerMP var1) {
   }

   public float getRotatedYaw(Rotation var1) {
      float var2 = MathHelper.wrapDegrees(this.rotationYaw);
      switch(var1) {
      case CLOCKWISE_180:
         return var2 + 180.0F;
      case COUNTERCLOCKWISE_90:
         return var2 + 270.0F;
      case CLOCKWISE_90:
         return var2 + 90.0F;
      default:
         return var2;
      }
   }

   public float getMirroredYaw(Mirror var1) {
      float var2 = MathHelper.wrapDegrees(this.rotationYaw);
      switch(var1) {
      case LEFT_RIGHT:
         return -var2;
      case FRONT_BACK:
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
}
