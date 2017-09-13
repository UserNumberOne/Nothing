package net.minecraft.entity;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
      this.world = worldIn;
      this.setPosition(0.0D, 0.0D, 0.0D);
      if (worldIn != null) {
         this.dimension = worldIn.provider.getDimension();
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
      this.entityId = id;
   }

   public Set getTags() {
      return this.tags;
   }

   public boolean addTag(String var1) {
      if (this.tags.size() >= 1024) {
         return false;
      } else {
         this.tags.add(tag);
         return true;
      }
   }

   public boolean removeTag(String var1) {
      return this.tags.remove(tag);
   }

   public void onKillCommand() {
      this.setDead();
   }

   protected abstract void entityInit();

   public EntityDataManager getDataManager() {
      return this.dataManager;
   }

   public boolean equals(Object var1) {
      return p_equals_1_ instanceof Entity ? ((Entity)p_equals_1_).entityId == this.entityId : false;
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
      if (width != this.width || height != this.height) {
         float f = this.width;
         this.width = width;
         this.height = height;
         AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
         this.setEntityBoundingBox(new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.minX + (double)this.width, axisalignedbb.minY + (double)this.height, axisalignedbb.minZ + (double)this.width));
         if (this.width > f && !this.firstUpdate && !this.world.isRemote) {
            this.move((double)(f - this.width), 0.0D, (double)(f - this.width));
         }
      }

   }

   protected void setRotation(float var1, float var2) {
      this.rotationYaw = yaw % 360.0F;
      this.rotationPitch = pitch % 360.0F;
   }

   public void setPosition(double var1, double var3, double var5) {
      this.posX = x;
      this.posY = y;
      this.posZ = z;
      float f = this.width / 2.0F;
      float f1 = this.height;
      this.setEntityBoundingBox(new AxisAlignedBB(x - (double)f, y, z - (double)f, x + (double)f, y + (double)f1, z + (double)f));
   }

   @SideOnly(Side.CLIENT)
   public void turn(float var1, float var2) {
      float f = this.rotationPitch;
      float f1 = this.rotationYaw;
      this.rotationYaw = (float)((double)this.rotationYaw + (double)yaw * 0.15D);
      this.rotationPitch = (float)((double)this.rotationPitch - (double)pitch * 0.15D);
      this.rotationPitch = MathHelper.clamp(this.rotationPitch, -90.0F, 90.0F);
      this.prevRotationPitch += this.rotationPitch - f;
      this.prevRotationYaw += this.rotationYaw - f1;
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
            MinecraftServer minecraftserver = this.world.getMinecraftServer();
            if (minecraftserver.getAllowNether()) {
               if (!this.isRiding()) {
                  int i = this.getMaxInPortalTime();
                  if (this.portalCounter++ >= i) {
                     this.portalCounter = i;
                     this.timeUntilPortal = this.getPortalCooldown();
                     int j;
                     if (this.world.provider.getDimensionType().getId() == -1) {
                        j = 0;
                     } else {
                        j = -1;
                     }

                     this.changeDimension(j);
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
      int i = seconds * 20;
      if (this instanceof EntityLivingBase) {
         i = EnchantmentProtection.getFireTimeForEntity((EntityLivingBase)this, i);
      }

      if (this.fire < i) {
         this.fire = i;
      }

   }

   public void extinguish() {
      this.fire = 0;
   }

   protected void kill() {
      this.setDead();
   }

   public boolean isOffsetPositionInLiquid(double var1, double var3, double var5) {
      AxisAlignedBB axisalignedbb = this.getEntityBoundingBox().offset(x, y, z);
      return this.isLiquidPresentInAABB(axisalignedbb);
   }

   private boolean isLiquidPresentInAABB(AxisAlignedBB var1) {
      return this.world.getCollisionBoxes(this, bb).isEmpty() && !this.world.containsAnyLiquid(bb);
   }

   public void move(double var1, double var3, double var5) {
      if (this.noClip) {
         this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, y, z));
         this.resetPositionToBB();
      } else {
         this.world.theProfiler.startSection("move");
         double d0 = this.posX;
         double d1 = this.posY;
         double d2 = this.posZ;
         if (this.isInWeb) {
            this.isInWeb = false;
            x *= 0.25D;
            y *= 0.05000000074505806D;
            z *= 0.25D;
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
         }

         double d3 = x;
         double d4 = y;
         double d5 = z;
         boolean flag = this.onGround && this.isSneaking() && this instanceof EntityPlayer;
         if (flag) {
            for(double d6 = 0.05D; x != 0.0D && this.world.getCollisionBoxes(this, this.getEntityBoundingBox().offset(x, -1.0D, 0.0D)).isEmpty(); d3 = x) {
               if (x < 0.05D && x >= -0.05D) {
                  x = 0.0D;
               } else if (x > 0.0D) {
                  x -= 0.05D;
               } else {
                  x += 0.05D;
               }
            }

            for(; z != 0.0D && this.world.getCollisionBoxes(this, this.getEntityBoundingBox().offset(0.0D, -1.0D, z)).isEmpty(); d5 = z) {
               if (z < 0.05D && z >= -0.05D) {
                  z = 0.0D;
               } else if (z > 0.0D) {
                  z -= 0.05D;
               } else {
                  z += 0.05D;
               }
            }

            for(; x != 0.0D && z != 0.0D && this.world.getCollisionBoxes(this, this.getEntityBoundingBox().offset(x, -1.0D, z)).isEmpty(); d5 = z) {
               if (x < 0.05D && x >= -0.05D) {
                  x = 0.0D;
               } else if (x > 0.0D) {
                  x -= 0.05D;
               } else {
                  x += 0.05D;
               }

               d3 = x;
               if (z < 0.05D && z >= -0.05D) {
                  z = 0.0D;
               } else if (z > 0.0D) {
                  z -= 0.05D;
               } else {
                  z += 0.05D;
               }
            }
         }

         List list1 = this.world.getCollisionBoxes(this, this.getEntityBoundingBox().addCoord(x, y, z));
         AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
         int i = 0;

         for(int j = list1.size(); i < j; ++i) {
            y = ((AxisAlignedBB)list1.get(i)).calculateYOffset(this.getEntityBoundingBox(), y);
         }

         this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, y, 0.0D));
         boolean i_ = this.onGround || d4 != y && d4 < 0.0D;
         int j4 = 0;

         for(int k = list1.size(); j4 < k; ++j4) {
            x = ((AxisAlignedBB)list1.get(j4)).calculateXOffset(this.getEntityBoundingBox(), x);
         }

         this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, 0.0D, 0.0D));
         j4 = 0;

         for(int k4 = list1.size(); j4 < k4; ++j4) {
            z = ((AxisAlignedBB)list1.get(j4)).calculateZOffset(this.getEntityBoundingBox(), z);
         }

         this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, 0.0D, z));
         if (this.stepHeight > 0.0F && i_ && (d3 != x || d5 != z)) {
            double d11 = x;
            double d7 = y;
            double d8 = z;
            AxisAlignedBB axisalignedbb1 = this.getEntityBoundingBox();
            this.setEntityBoundingBox(axisalignedbb);
            y = (double)this.stepHeight;
            List list = this.world.getCollisionBoxes(this, this.getEntityBoundingBox().addCoord(d3, y, d5));
            AxisAlignedBB axisalignedbb2 = this.getEntityBoundingBox();
            AxisAlignedBB axisalignedbb3 = axisalignedbb2.addCoord(d3, 0.0D, d5);
            double d9 = y;
            int l = 0;

            for(int i1 = list.size(); l < i1; ++l) {
               d9 = ((AxisAlignedBB)list.get(l)).calculateYOffset(axisalignedbb3, d9);
            }

            axisalignedbb2 = axisalignedbb2.offset(0.0D, d9, 0.0D);
            double d15 = d3;
            int j1 = 0;

            for(int k1 = list.size(); j1 < k1; ++j1) {
               d15 = ((AxisAlignedBB)list.get(j1)).calculateXOffset(axisalignedbb2, d15);
            }

            axisalignedbb2 = axisalignedbb2.offset(d15, 0.0D, 0.0D);
            double d16 = d5;
            int l1 = 0;

            for(int i2 = list.size(); l1 < i2; ++l1) {
               d16 = ((AxisAlignedBB)list.get(l1)).calculateZOffset(axisalignedbb2, d16);
            }

            axisalignedbb2 = axisalignedbb2.offset(0.0D, 0.0D, d16);
            AxisAlignedBB axisalignedbb4 = this.getEntityBoundingBox();
            double d17 = y;
            int j2 = 0;

            for(int k2 = list.size(); j2 < k2; ++j2) {
               d17 = ((AxisAlignedBB)list.get(j2)).calculateYOffset(axisalignedbb4, d17);
            }

            axisalignedbb4 = axisalignedbb4.offset(0.0D, d17, 0.0D);
            double d18 = d3;
            int l2 = 0;

            for(int i3 = list.size(); l2 < i3; ++l2) {
               d18 = ((AxisAlignedBB)list.get(l2)).calculateXOffset(axisalignedbb4, d18);
            }

            axisalignedbb4 = axisalignedbb4.offset(d18, 0.0D, 0.0D);
            double d19 = d5;
            int j3 = 0;

            for(int k3 = list.size(); j3 < k3; ++j3) {
               d19 = ((AxisAlignedBB)list.get(j3)).calculateZOffset(axisalignedbb4, d19);
            }

            axisalignedbb4 = axisalignedbb4.offset(0.0D, 0.0D, d19);
            double d20 = d15 * d15 + d16 * d16;
            double d10 = d18 * d18 + d19 * d19;
            if (d20 > d10) {
               x = d15;
               z = d16;
               y = -d9;
               this.setEntityBoundingBox(axisalignedbb2);
            } else {
               x = d18;
               z = d19;
               y = -d17;
               this.setEntityBoundingBox(axisalignedbb4);
            }

            int l3 = 0;

            for(int i4 = list.size(); l3 < i4; ++l3) {
               y = ((AxisAlignedBB)list.get(l3)).calculateYOffset(this.getEntityBoundingBox(), y);
            }

            this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, y, 0.0D));
            if (d11 * d11 + d8 * d8 >= x * x + z * z) {
               x = d11;
               y = d7;
               z = d8;
               this.setEntityBoundingBox(axisalignedbb1);
            }
         }

         this.world.theProfiler.endSection();
         this.world.theProfiler.startSection("rest");
         this.resetPositionToBB();
         this.isCollidedHorizontally = d3 != x || d5 != z;
         this.isCollidedVertically = d4 != y;
         this.onGround = this.isCollidedVertically && d4 < 0.0D;
         this.isCollided = this.isCollidedHorizontally || this.isCollidedVertically;
         j4 = MathHelper.floor(this.posX);
         int l4 = MathHelper.floor(this.posY - 0.20000000298023224D);
         int i5 = MathHelper.floor(this.posZ);
         BlockPos blockpos = new BlockPos(j4, l4, i5);
         IBlockState iblockstate = this.world.getBlockState(blockpos);
         if (iblockstate.getMaterial() == Material.AIR) {
            BlockPos blockpos1 = blockpos.down();
            IBlockState iblockstate1 = this.world.getBlockState(blockpos1);
            Block block1 = iblockstate1.getBlock();
            if (block1 instanceof BlockFence || block1 instanceof BlockWall || block1 instanceof BlockFenceGate) {
               iblockstate = iblockstate1;
               blockpos = blockpos1;
            }
         }

         this.updateFallState(y, this.onGround, iblockstate, blockpos);
         if (d3 != x) {
            this.motionX = 0.0D;
         }

         if (d5 != z) {
            this.motionZ = 0.0D;
         }

         Block block = iblockstate.getBlock();
         if (d4 != y) {
            block.onLanded(this.world, this);
         }

         if (this.canTriggerWalking() && !flag && !this.isRiding()) {
            double d12 = this.posX - d0;
            double d13 = this.posY - d1;
            double d14 = this.posZ - d2;
            if (block != Blocks.LADDER) {
               d13 = 0.0D;
            }

            if (block != null && this.onGround) {
               block.onEntityWalk(this.world, blockpos, this);
            }

            this.distanceWalkedModified = (float)((double)this.distanceWalkedModified + (double)MathHelper.sqrt(d12 * d12 + d14 * d14) * 0.6D);
            this.distanceWalkedOnStepModified = (float)((double)this.distanceWalkedOnStepModified + (double)MathHelper.sqrt(d12 * d12 + d13 * d13 + d14 * d14) * 0.6D);
            if (this.distanceWalkedOnStepModified > (float)this.nextStepDistance && iblockstate.getMaterial() != Material.AIR) {
               this.nextStepDistance = (int)this.distanceWalkedOnStepModified + 1;
               if (this.isInWater()) {
                  float f = MathHelper.sqrt(this.motionX * this.motionX * 0.20000000298023224D + this.motionY * this.motionY + this.motionZ * this.motionZ * 0.20000000298023224D) * 0.35F;
                  if (f > 1.0F) {
                     f = 1.0F;
                  }

                  this.playSound(this.getSwimSound(), f, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
               }

               this.playStepSound(blockpos, block);
            }
         }

         try {
            this.doBlockCollisions();
         } catch (Throwable var60) {
            CrashReport crashreport = CrashReport.makeCrashReport(var60, "Checking entity block collision");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being checked for collision");
            this.addEntityCrashInfo(crashreportcategory);
            throw new ReportedException(crashreport);
         }

         boolean flag1 = this.isWet();
         if (this.world.isFlammableWithin(this.getEntityBoundingBox().contract(0.001D))) {
            this.dealFireDamage(1);
            if (!flag1) {
               ++this.fire;
               if (this.fire == 0) {
                  this.setFire(8);
               }
            }
         } else if (this.fire <= 0) {
            this.fire = -this.fireResistance;
         }

         if (flag1 && this.fire > 0) {
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
      BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain(axisalignedbb.minX + 0.001D, axisalignedbb.minY + 0.001D, axisalignedbb.minZ + 0.001D);
      BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos1 = BlockPos.PooledMutableBlockPos.retain(axisalignedbb.maxX - 0.001D, axisalignedbb.maxY - 0.001D, axisalignedbb.maxZ - 0.001D);
      BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos2 = BlockPos.PooledMutableBlockPos.retain();
      if (this.world.isAreaLoaded(blockpos$pooledmutableblockpos, blockpos$pooledmutableblockpos1)) {
         for(int i = blockpos$pooledmutableblockpos.getX(); i <= blockpos$pooledmutableblockpos1.getX(); ++i) {
            for(int j = blockpos$pooledmutableblockpos.getY(); j <= blockpos$pooledmutableblockpos1.getY(); ++j) {
               for(int k = blockpos$pooledmutableblockpos.getZ(); k <= blockpos$pooledmutableblockpos1.getZ(); ++k) {
                  blockpos$pooledmutableblockpos2.setPos(i, j, k);
                  IBlockState iblockstate = this.world.getBlockState(blockpos$pooledmutableblockpos2);

                  try {
                     iblockstate.getBlock().onEntityCollidedWithBlock(this.world, blockpos$pooledmutableblockpos2, iblockstate, this);
                  } catch (Throwable var12) {
                     CrashReport crashreport = CrashReport.makeCrashReport(var12, "Colliding entity with block");
                     CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being collided with");
                     CrashReportCategory.addBlockInfo(crashreportcategory, blockpos$pooledmutableblockpos2, iblockstate);
                     throw new ReportedException(crashreport);
                  }
               }
            }
         }
      }

      blockpos$pooledmutableblockpos.release();
      blockpos$pooledmutableblockpos1.release();
      blockpos$pooledmutableblockpos2.release();
   }

   protected void playStepSound(BlockPos var1, Block var2) {
      SoundType soundtype = blockIn.getSoundType(this.world.getBlockState(pos), this.world, pos, this);
      if (this.world.getBlockState(pos.up()).getBlock() == Blocks.SNOW_LAYER) {
         soundtype = Blocks.SNOW_LAYER.getSoundType();
         this.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
      } else if (!blockIn.getDefaultState().getMaterial().isLiquid()) {
         this.playSound(soundtype.getStepSound(), soundtype.getVolume() * 0.15F, soundtype.getPitch());
      }

   }

   public void playSound(SoundEvent var1, float var2, float var3) {
      if (!this.isSilent()) {
         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, soundIn, this.getSoundCategory(), volume, pitch);
      }

   }

   public boolean isSilent() {
      return ((Boolean)this.dataManager.get(SILENT)).booleanValue();
   }

   public void setSilent(boolean var1) {
      this.dataManager.set(SILENT, Boolean.valueOf(isSilent));
   }

   public boolean hasNoGravity() {
      return ((Boolean)this.dataManager.get(NO_GRAVITY)).booleanValue();
   }

   public void setNoGravity(boolean var1) {
      this.dataManager.set(NO_GRAVITY, Boolean.valueOf(noGravity));
   }

   protected boolean canTriggerWalking() {
      return true;
   }

   protected void updateFallState(double var1, boolean var3, IBlockState var4, BlockPos var5) {
      if (onGroundIn) {
         if (this.fallDistance > 0.0F) {
            state.getBlock().onFallenUpon(this.world, pos, this, this.fallDistance);
         }

         this.fallDistance = 0.0F;
      } else if (y < 0.0D) {
         this.fallDistance = (float)((double)this.fallDistance - y);
      }

   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox() {
      return null;
   }

   protected void dealFireDamage(int var1) {
      if (!this.isImmuneToFire) {
         this.attackEntityFrom(DamageSource.inFire, (float)amount);
      }

   }

   public final boolean isImmuneToFire() {
      return this.isImmuneToFire;
   }

   public void fall(float var1, float var2) {
      if (this.isBeingRidden()) {
         for(Entity entity : this.getPassengers()) {
            entity.fall(distance, damageMultiplier);
         }
      }

   }

   public boolean isWet() {
      if (this.inWater) {
         return true;
      } else {
         BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain(this.posX, this.posY, this.posZ);
         if (!this.world.isRainingAt(blockpos$pooledmutableblockpos) && !this.world.isRainingAt(blockpos$pooledmutableblockpos.setPos(this.posX, this.posY + (double)this.height, this.posZ))) {
            blockpos$pooledmutableblockpos.release();
            return false;
         } else {
            blockpos$pooledmutableblockpos.release();
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

      for(int j = 0; (float)j < 1.0F + this.width * 20.0F; ++j) {
         float f4 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
         float f5 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width;
         this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX + (double)f4, (double)(f1 + 1.0F), this.posZ + (double)f5, this.motionX, this.motionY, this.motionZ);
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
      BlockPos blockpos = new BlockPos(i, j, k);
      IBlockState iblockstate = this.world.getBlockState(blockpos);
      if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE) {
         this.world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX + ((double)this.rand.nextFloat() - 0.5D) * (double)this.width, this.getEntityBoundingBox().minY + 0.1D, this.posZ + ((double)this.rand.nextFloat() - 0.5D) * (double)this.width, -this.motionX * 4.0D, 1.5D, -this.motionZ * 4.0D, Block.getStateId(iblockstate));
      }

   }

   public boolean isInsideOfMaterial(Material var1) {
      if (this.getRidingEntity() instanceof EntityBoat) {
         return false;
      } else {
         double d0 = this.posY + (double)this.getEyeHeight();
         BlockPos blockpos = new BlockPos(this.posX, d0, this.posZ);
         IBlockState iblockstate = this.world.getBlockState(blockpos);
         Boolean result = iblockstate.getBlock().isEntityInsideMaterial(this.world, blockpos, iblockstate, this, d0, materialIn, true);
         if (result != null) {
            return result.booleanValue();
         } else {
            return iblockstate.getMaterial() == materialIn ? ForgeHooks.isInsideOfMaterial(materialIn, this, blockpos) : false;
         }
      }
   }

   public boolean isInLava() {
      return this.world.isMaterialInBB(this.getEntityBoundingBox().expand(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D), Material.LAVA);
   }

   public void moveRelative(float var1, float var2, float var3) {
      float f = strafe * strafe + forward * forward;
      if (f >= 1.0E-4F) {
         f = MathHelper.sqrt(f);
         if (f < 1.0F) {
            f = 1.0F;
         }

         f = friction / f;
         strafe = strafe * f;
         forward = forward * f;
         float f1 = MathHelper.sin(this.rotationYaw * 0.017453292F);
         float f2 = MathHelper.cos(this.rotationYaw * 0.017453292F);
         this.motionX += (double)(strafe * f2 - forward * f1);
         this.motionZ += (double)(forward * f2 + strafe * f1);
      }

   }

   @SideOnly(Side.CLIENT)
   public int getBrightnessForRender(float var1) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(MathHelper.floor(this.posX), 0, MathHelper.floor(this.posZ));
      if (this.world.isBlockLoaded(blockpos$mutableblockpos)) {
         blockpos$mutableblockpos.setY(MathHelper.floor(this.posY + (double)this.getEyeHeight()));
         return this.world.getCombinedLight(blockpos$mutableblockpos, 0);
      } else {
         return 0;
      }
   }

   public float getBrightness(float var1) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(MathHelper.floor(this.posX), 0, MathHelper.floor(this.posZ));
      if (this.world.isBlockLoaded(blockpos$mutableblockpos)) {
         blockpos$mutableblockpos.setY(MathHelper.floor(this.posY + (double)this.getEyeHeight()));
         return this.world.getLightBrightness(blockpos$mutableblockpos);
      } else {
         return 0.0F;
      }
   }

   public void setWorld(World var1) {
      this.world = worldIn;
   }

   public void setPositionAndRotation(double var1, double var3, double var5, float var7, float var8) {
      this.posX = MathHelper.clamp(x, -3.0E7D, 3.0E7D);
      this.posY = y;
      this.posZ = MathHelper.clamp(z, -3.0E7D, 3.0E7D);
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      pitch = MathHelper.clamp(pitch, -90.0F, 90.0F);
      this.rotationYaw = yaw;
      this.rotationPitch = pitch;
      this.prevRotationYaw = this.rotationYaw;
      this.prevRotationPitch = this.rotationPitch;
      double d0 = (double)(this.prevRotationYaw - yaw);
      if (d0 < -180.0D) {
         this.prevRotationYaw += 360.0F;
      }

      if (d0 >= 180.0D) {
         this.prevRotationYaw -= 360.0F;
      }

      this.setPosition(this.posX, this.posY, this.posZ);
      this.setRotation(yaw, pitch);
   }

   public void moveToBlockPosAndAngles(BlockPos var1, float var2, float var3) {
      this.setLocationAndAngles((double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D, rotationYawIn, rotationPitchIn);
   }

   public void setLocationAndAngles(double var1, double var3, double var5, float var7, float var8) {
      this.posX = x;
      this.posY = y;
      this.posZ = z;
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      this.lastTickPosX = this.posX;
      this.lastTickPosY = this.posY;
      this.lastTickPosZ = this.posZ;
      this.rotationYaw = yaw;
      this.rotationPitch = pitch;
      this.setPosition(this.posX, this.posY, this.posZ);
   }

   public float getDistanceToEntity(Entity var1) {
      float f = (float)(this.posX - entityIn.posX);
      float f1 = (float)(this.posY - entityIn.posY);
      float f2 = (float)(this.posZ - entityIn.posZ);
      return MathHelper.sqrt(f * f + f1 * f1 + f2 * f2);
   }

   public double getDistanceSq(double var1, double var3, double var5) {
      double d0 = this.posX - x;
      double d1 = this.posY - y;
      double d2 = this.posZ - z;
      return d0 * d0 + d1 * d1 + d2 * d2;
   }

   public double getDistanceSq(BlockPos var1) {
      return pos.distanceSq(this.posX, this.posY, this.posZ);
   }

   public double getDistanceSqToCenter(BlockPos var1) {
      return pos.distanceSqToCenter(this.posX, this.posY, this.posZ);
   }

   public double getDistance(double var1, double var3, double var5) {
      double d0 = this.posX - x;
      double d1 = this.posY - y;
      double d2 = this.posZ - z;
      return (double)MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
   }

   public double getDistanceSqToEntity(Entity var1) {
      double d0 = this.posX - entityIn.posX;
      double d1 = this.posY - entityIn.posY;
      double d2 = this.posZ - entityIn.posZ;
      return d0 * d0 + d1 * d1 + d2 * d2;
   }

   public void onCollideWithPlayer(EntityPlayer var1) {
   }

   public void applyEntityCollision(Entity var1) {
      if (!this.isRidingSameEntity(entityIn) && !entityIn.noClip && !this.noClip) {
         double d0 = entityIn.posX - this.posX;
         double d1 = entityIn.posZ - this.posZ;
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

            if (!entityIn.isBeingRidden()) {
               entityIn.addVelocity(d0, 0.0D, d1);
            }
         }
      }

   }

   public void addVelocity(double var1, double var3, double var5) {
      this.motionX += x;
      this.motionY += y;
      this.motionZ += z;
      this.isAirBorne = true;
   }

   protected void setBeenAttacked() {
      this.velocityChanged = true;
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (this.isEntityInvulnerable(source)) {
         return false;
      } else {
         this.setBeenAttacked();
         return false;
      }
   }

   public Vec3d getLook(float var1) {
      if (partialTicks == 1.0F) {
         return this.getVectorForRotation(this.rotationPitch, this.rotationYaw);
      } else {
         float f = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * partialTicks;
         float f1 = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * partialTicks;
         return this.getVectorForRotation(f, f1);
      }
   }

   protected final Vec3d getVectorForRotation(float var1, float var2) {
      float f = MathHelper.cos(-yaw * 0.017453292F - 3.1415927F);
      float f1 = MathHelper.sin(-yaw * 0.017453292F - 3.1415927F);
      float f2 = -MathHelper.cos(-pitch * 0.017453292F);
      float f3 = MathHelper.sin(-pitch * 0.017453292F);
      return new Vec3d((double)(f1 * f2), (double)f3, (double)(f * f2));
   }

   @SideOnly(Side.CLIENT)
   public Vec3d getPositionEyes(float var1) {
      if (partialTicks == 1.0F) {
         return new Vec3d(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ);
      } else {
         double d0 = this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks;
         double d1 = this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks + (double)this.getEyeHeight();
         double d2 = this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks;
         return new Vec3d(d0, d1, d2);
      }
   }

   @Nullable
   @SideOnly(Side.CLIENT)
   public RayTraceResult rayTrace(double var1, float var3) {
      Vec3d vec3d = this.getPositionEyes(partialTicks);
      Vec3d vec3d1 = this.getLook(partialTicks);
      Vec3d vec3d2 = vec3d.addVector(vec3d1.xCoord * blockReachDistance, vec3d1.yCoord * blockReachDistance, vec3d1.zCoord * blockReachDistance);
      return this.world.rayTraceBlocks(vec3d, vec3d2, false, false, true);
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
      double d0 = this.posX - x;
      double d1 = this.posY - y;
      double d2 = this.posZ - z;
      double d3 = d0 * d0 + d1 * d1 + d2 * d2;
      return this.isInRangeToRenderDist(d3);
   }

   @SideOnly(Side.CLIENT)
   public boolean isInRangeToRenderDist(double var1) {
      double d0 = this.getEntityBoundingBox().getAverageEdgeLength();
      if (Double.isNaN(d0)) {
         d0 = 1.0D;
      }

      d0 = d0 * 64.0D * renderDistanceWeight;
      return distance < d0 * d0;
   }

   public boolean writeToNBTAtomically(NBTTagCompound var1) {
      String s = this.getEntityString();
      if (!this.isDead && s != null) {
         compound.setString("id", s);
         this.writeToNBT(compound);
         return true;
      } else {
         return false;
      }
   }

   public boolean writeToNBTOptional(NBTTagCompound var1) {
      String s = this.getEntityString();
      if (!this.isDead && s != null && !this.isRiding()) {
         compound.setString("id", s);
         this.writeToNBT(compound);
         return true;
      } else {
         return false;
      }
   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      try {
         compound.setTag("Pos", this.newDoubleNBTList(this.posX, this.posY, this.posZ));
         compound.setTag("Motion", this.newDoubleNBTList(this.motionX, this.motionY, this.motionZ));
         compound.setTag("Rotation", this.newFloatNBTList(this.rotationYaw, this.rotationPitch));
         compound.setFloat("FallDistance", this.fallDistance);
         compound.setShort("Fire", (short)this.fire);
         compound.setShort("Air", (short)this.getAir());
         compound.setBoolean("OnGround", this.onGround);
         compound.setInteger("Dimension", this.dimension);
         compound.setBoolean("Invulnerable", this.invulnerable);
         compound.setInteger("PortalCooldown", this.timeUntilPortal);
         compound.setUniqueId("UUID", this.getUniqueID());
         if (this.getCustomNameTag() != null && !this.getCustomNameTag().isEmpty()) {
            compound.setString("CustomName", this.getCustomNameTag());
         }

         if (this.getAlwaysRenderNameTag()) {
            compound.setBoolean("CustomNameVisible", this.getAlwaysRenderNameTag());
         }

         this.cmdResultStats.writeStatsToNBT(compound);
         if (this.isSilent()) {
            compound.setBoolean("Silent", this.isSilent());
         }

         if (this.hasNoGravity()) {
            compound.setBoolean("NoGravity", this.hasNoGravity());
         }

         if (this.glowing) {
            compound.setBoolean("Glowing", this.glowing);
         }

         if (this.tags.size() > 0) {
            NBTTagList nbttaglist = new NBTTagList();

            for(String s : this.tags) {
               nbttaglist.appendTag(new NBTTagString(s));
            }

            compound.setTag("Tags", nbttaglist);
         }

         if (this.customEntityData != null) {
            compound.setTag("ForgeData", this.customEntityData);
         }

         if (this.capabilities != null) {
            compound.setTag("ForgeCaps", this.capabilities.serializeNBT());
         }

         this.writeEntityToNBT(compound);
         if (this.isBeingRidden()) {
            NBTTagList nbttaglist1 = new NBTTagList();

            for(Entity entity : this.getPassengers()) {
               NBTTagCompound nbttagcompound = new NBTTagCompound();
               if (entity.writeToNBTAtomically(nbttagcompound)) {
                  nbttaglist1.appendTag(nbttagcompound);
               }
            }

            if (!nbttaglist1.hasNoTags()) {
               compound.setTag("Passengers", nbttaglist1);
            }
         }

         return compound;
      } catch (Throwable var6) {
         CrashReport crashreport = CrashReport.makeCrashReport(var6, "Saving entity NBT");
         CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being saved");
         this.addEntityCrashInfo(crashreportcategory);
         throw new ReportedException(crashreport);
      }
   }

   public void readFromNBT(NBTTagCompound var1) {
      try {
         NBTTagList nbttaglist = compound.getTagList("Pos", 6);
         NBTTagList nbttaglist2 = compound.getTagList("Motion", 6);
         NBTTagList nbttaglist3 = compound.getTagList("Rotation", 5);
         this.motionX = nbttaglist2.getDoubleAt(0);
         this.motionY = nbttaglist2.getDoubleAt(1);
         this.motionZ = nbttaglist2.getDoubleAt(2);
         if (Math.abs(this.motionX) > 10.0D) {
            this.motionX = 0.0D;
         }

         if (Math.abs(this.motionY) > 10.0D) {
            this.motionY = 0.0D;
         }

         if (Math.abs(this.motionZ) > 10.0D) {
            this.motionZ = 0.0D;
         }

         this.posX = nbttaglist.getDoubleAt(0);
         this.posY = nbttaglist.getDoubleAt(1);
         this.posZ = nbttaglist.getDoubleAt(2);
         this.lastTickPosX = this.posX;
         this.lastTickPosY = this.posY;
         this.lastTickPosZ = this.posZ;
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         this.rotationYaw = nbttaglist3.getFloatAt(0);
         this.rotationPitch = nbttaglist3.getFloatAt(1);
         this.prevRotationYaw = this.rotationYaw;
         this.prevRotationPitch = this.rotationPitch;
         this.setRotationYawHead(this.rotationYaw);
         this.setRenderYawOffset(this.rotationYaw);
         this.fallDistance = compound.getFloat("FallDistance");
         this.fire = compound.getShort("Fire");
         this.setAir(compound.getShort("Air"));
         this.onGround = compound.getBoolean("OnGround");
         if (compound.hasKey("Dimension")) {
            this.dimension = compound.getInteger("Dimension");
         }

         this.invulnerable = compound.getBoolean("Invulnerable");
         this.timeUntilPortal = compound.getInteger("PortalCooldown");
         if (compound.hasUniqueId("UUID")) {
            this.entityUniqueID = compound.getUniqueId("UUID");
            this.cachedUniqueIdString = this.entityUniqueID.toString();
         }

         this.setPosition(this.posX, this.posY, this.posZ);
         this.setRotation(this.rotationYaw, this.rotationPitch);
         if (compound.hasKey("CustomName", 8)) {
            this.setCustomNameTag(compound.getString("CustomName"));
         }

         this.setAlwaysRenderNameTag(compound.getBoolean("CustomNameVisible"));
         this.cmdResultStats.readStatsFromNBT(compound);
         this.setSilent(compound.getBoolean("Silent"));
         this.setNoGravity(compound.getBoolean("NoGravity"));
         this.setGlowing(compound.getBoolean("Glowing"));
         if (compound.hasKey("ForgeData")) {
            this.customEntityData = compound.getCompoundTag("ForgeData");
         }

         if (this.capabilities != null && compound.hasKey("ForgeCaps")) {
            this.capabilities.deserializeNBT(compound.getCompoundTag("ForgeCaps"));
         }

         if (compound.hasKey("Tags", 9)) {
            this.tags.clear();
            NBTTagList nbttaglist1 = compound.getTagList("Tags", 8);
            int i = Math.min(nbttaglist1.tagCount(), 1024);

            for(int j = 0; j < i; ++j) {
               this.tags.add(nbttaglist1.getStringTagAt(j));
            }
         }

         this.readEntityFromNBT(compound);
         if (this.shouldSetPosAfterLoading()) {
            this.setPosition(this.posX, this.posY, this.posZ);
         }

      } catch (Throwable var8) {
         CrashReport crashreport = CrashReport.makeCrashReport(var8, "Loading entity NBT");
         CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being loaded");
         this.addEntityCrashInfo(crashreportcategory);
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

   protected NBTTagList newDoubleNBTList(double... var1) {
      NBTTagList nbttaglist = new NBTTagList();

      for(double d0 : numbers) {
         nbttaglist.appendTag(new NBTTagDouble(d0));
      }

      return nbttaglist;
   }

   protected NBTTagList newFloatNBTList(float... var1) {
      NBTTagList nbttaglist = new NBTTagList();

      for(float f : numbers) {
         nbttaglist.appendTag(new NBTTagFloat(f));
      }

      return nbttaglist;
   }

   public EntityItem dropItem(Item var1, int var2) {
      return this.dropItemWithOffset(itemIn, size, 0.0F);
   }

   public EntityItem dropItemWithOffset(Item var1, int var2, float var3) {
      return this.entityDropItem(new ItemStack(itemIn, size, 0), offsetY);
   }

   public EntityItem entityDropItem(ItemStack var1, float var2) {
      if (stack.stackSize != 0 && stack.getItem() != null) {
         EntityItem entityitem = new EntityItem(this.world, this.posX, this.posY + (double)offsetY, this.posZ, stack);
         entityitem.setDefaultPickupDelay();
         if (this.captureDrops) {
            this.capturedDrops.add(entityitem);
         } else {
            this.world.spawnEntity(entityitem);
         }

         return entityitem;
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
         BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

         for(int i = 0; i < 8; ++i) {
            int j = MathHelper.floor(this.posY + (double)(((float)((i >> 0) % 2) - 0.5F) * 0.1F) + (double)this.getEyeHeight());
            int k = MathHelper.floor(this.posX + (double)(((float)((i >> 1) % 2) - 0.5F) * this.width * 0.8F));
            int l = MathHelper.floor(this.posZ + (double)(((float)((i >> 2) % 2) - 0.5F) * this.width * 0.8F));
            if (blockpos$pooledmutableblockpos.getX() != k || blockpos$pooledmutableblockpos.getY() != j || blockpos$pooledmutableblockpos.getZ() != l) {
               blockpos$pooledmutableblockpos.setPos(k, j, l);
               if (this.world.getBlockState(blockpos$pooledmutableblockpos).getBlock().causesSuffocation()) {
                  blockpos$pooledmutableblockpos.release();
                  return true;
               }
            }
         }

         blockpos$pooledmutableblockpos.release();
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

   public void updatePassenger(Entity var1) {
      if (this.isPassenger(passenger)) {
         passenger.setPosition(this.posX, this.posY + this.getMountedYOffset() + passenger.getYOffset(), this.posZ);
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
      return this.startRiding(entityIn, false);
   }

   public boolean startRiding(Entity var1, boolean var2) {
      if (!ForgeEventFactory.canMountEntity(this, entityIn, true)) {
         return false;
      } else if (force || this.canBeRidden(entityIn) && entityIn.canFitPassenger(this)) {
         if (this.isRiding()) {
            this.dismountRidingEntity();
         }

         this.ridingEntity = entityIn;
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
      for(int i = this.riddenByEntities.size() - 1; i >= 0; --i) {
         ((Entity)this.riddenByEntities.get(i)).dismountRidingEntity();
      }

   }

   public void dismountRidingEntity() {
      if (this.ridingEntity != null) {
         Entity entity = this.ridingEntity;
         if (!ForgeEventFactory.canMountEntity(this, entity, false)) {
            return;
         }

         this.ridingEntity = null;
         entity.removePassenger(this);
      }

   }

   protected void addPassenger(Entity var1) {
      if (passenger.getRidingEntity() != this) {
         throw new IllegalStateException("Use x.startRiding(y), not y.addPassenger(x)");
      } else {
         if (!this.world.isRemote && passenger instanceof EntityPlayer && !(this.getControllingPassenger() instanceof EntityPlayer)) {
            this.riddenByEntities.add(0, passenger);
         } else {
            this.riddenByEntities.add(passenger);
         }

      }
   }

   protected void removePassenger(Entity var1) {
      if (passenger.getRidingEntity() == this) {
         throw new IllegalStateException("Use x.stopRiding(y), not y.removePassenger(x)");
      } else {
         this.riddenByEntities.remove(passenger);
         passenger.rideCooldown = 60;
      }
   }

   protected boolean canFitPassenger(Entity var1) {
      return this.getPassengers().size() < 1;
   }

   @SideOnly(Side.CLIENT)
   public void setPositionAndRotationDirect(double var1, double var3, double var5, float var7, float var8, int var9, boolean var10) {
      this.setPosition(x, y, z);
      this.setRotation(yaw, pitch);
   }

   public float getCollisionBorderSize() {
      return 0.0F;
   }

   public Vec3d getLookVec() {
      return null;
   }

   @SideOnly(Side.CLIENT)
   public Vec2f getPitchYaw() {
      Vec2f vec2f = new Vec2f(this.rotationPitch, this.rotationYaw);
      return vec2f;
   }

   @SideOnly(Side.CLIENT)
   public Vec3d getForward() {
      return Vec3d.fromPitchYawVector(this.getPitchYaw());
   }

   public void setPortal(BlockPos var1) {
      if (this.timeUntilPortal > 0) {
         this.timeUntilPortal = this.getPortalCooldown();
      } else {
         if (!this.world.isRemote && !pos.equals(this.lastPortalPos)) {
            this.lastPortalPos = new BlockPos(pos);
            BlockPattern.PatternHelper blockpattern$patternhelper = Blocks.PORTAL.createPatternHelper(this.world, this.lastPortalPos);
            double d0 = blockpattern$patternhelper.getForwards().getAxis() == EnumFacing.Axis.X ? (double)blockpattern$patternhelper.getFrontTopLeft().getZ() : (double)blockpattern$patternhelper.getFrontTopLeft().getX();
            double d1 = blockpattern$patternhelper.getForwards().getAxis() == EnumFacing.Axis.X ? this.posZ : this.posX;
            d1 = Math.abs(MathHelper.pct(d1 - (double)(blockpattern$patternhelper.getForwards().rotateY().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? 1 : 0), d0, d0 - (double)blockpattern$patternhelper.getWidth()));
            double d2 = MathHelper.pct(this.posY - 1.0D, (double)blockpattern$patternhelper.getFrontTopLeft().getY(), (double)(blockpattern$patternhelper.getFrontTopLeft().getY() - blockpattern$patternhelper.getHeight()));
            this.lastPortalVec = new Vec3d(d1, d2, 0.0D);
            this.teleportDirection = blockpattern$patternhelper.getForwards();
         }

         this.inPortal = true;
      }

   }

   public int getPortalCooldown() {
      return 300;
   }

   @SideOnly(Side.CLIENT)
   public void setVelocity(double var1, double var3, double var5) {
      this.motionX = x;
      this.motionY = y;
      this.motionZ = z;
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

   public void setSneaking(boolean var1) {
      this.setFlag(1, sneaking);
   }

   public boolean isSprinting() {
      return this.getFlag(3);
   }

   public void setSprinting(boolean var1) {
      this.setFlag(3, sprinting);
   }

   public boolean isGlowing() {
      return this.glowing || this.world.isRemote && this.getFlag(6);
   }

   public void setGlowing(boolean var1) {
      this.glowing = glowingIn;
      if (!this.world.isRemote) {
         this.setFlag(6, this.glowing);
      }

   }

   public boolean isInvisible() {
      return this.getFlag(5);
   }

   @SideOnly(Side.CLIENT)
   public boolean isInvisibleToPlayer(EntityPlayer var1) {
      if (player.isSpectator()) {
         return false;
      } else {
         Team team = this.getTeam();
         return team != null && player != null && player.getTeam() == team && team.getSeeFriendlyInvisiblesEnabled() ? false : this.isInvisible();
      }
   }

   @Nullable
   public Team getTeam() {
      return this.world.getScoreboard().getPlayersTeam(this.getCachedUniqueIdString());
   }

   public boolean isOnSameTeam(Entity var1) {
      return this.isOnScoreboardTeam(entityIn.getTeam());
   }

   public boolean isOnScoreboardTeam(Team var1) {
      return this.getTeam() != null ? this.getTeam().isSameTeam(teamIn) : false;
   }

   public void setInvisible(boolean var1) {
      this.setFlag(5, invisible);
   }

   protected boolean getFlag(int var1) {
      return (((Byte)this.dataManager.get(FLAGS)).byteValue() & 1 << flag) != 0;
   }

   protected void setFlag(int var1, boolean var2) {
      byte b0 = ((Byte)this.dataManager.get(FLAGS)).byteValue();
      if (set) {
         this.dataManager.set(FLAGS, Byte.valueOf((byte)(b0 | 1 << flag)));
      } else {
         this.dataManager.set(FLAGS, Byte.valueOf((byte)(b0 & ~(1 << flag))));
      }

   }

   public int getAir() {
      return ((Integer)this.dataManager.get(AIR)).intValue();
   }

   public void setAir(int var1) {
      this.dataManager.set(AIR, Integer.valueOf(air));
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
      BlockPos blockpos = new BlockPos(x, y, z);
      double d0 = x - (double)blockpos.getX();
      double d1 = y - (double)blockpos.getY();
      double d2 = z - (double)blockpos.getZ();
      List list = this.world.getCollisionBoxes(this.getEntityBoundingBox());
      MinecraftForge.EVENT_BUS.post(new GetCollisionBoxesEvent(this.world, this, this.getEntityBoundingBox(), list));
      if (list.isEmpty()) {
         return false;
      } else {
         EnumFacing enumfacing = EnumFacing.UP;
         double d3 = Double.MAX_VALUE;
         if (!this.world.isBlockFullCube(blockpos.west()) && d0 < d3) {
            d3 = d0;
            enumfacing = EnumFacing.WEST;
         }

         if (!this.world.isBlockFullCube(blockpos.east()) && 1.0D - d0 < d3) {
            d3 = 1.0D - d0;
            enumfacing = EnumFacing.EAST;
         }

         if (!this.world.isBlockFullCube(blockpos.north()) && d2 < d3) {
            d3 = d2;
            enumfacing = EnumFacing.NORTH;
         }

         if (!this.world.isBlockFullCube(blockpos.south()) && 1.0D - d2 < d3) {
            d3 = 1.0D - d2;
            enumfacing = EnumFacing.SOUTH;
         }

         if (!this.world.isBlockFullCube(blockpos.up()) && 1.0D - d1 < d3) {
            d3 = 1.0D - d1;
            enumfacing = EnumFacing.UP;
         }

         float f = this.rand.nextFloat() * 0.2F + 0.1F;
         float f1 = (float)enumfacing.getAxisDirection().getOffset();
         if (enumfacing.getAxis() == EnumFacing.Axis.X) {
            this.motionX += (double)(f1 * f);
         } else if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
            this.motionY += (double)(f1 * f);
         } else if (enumfacing.getAxis() == EnumFacing.Axis.Z) {
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

   public boolean isEntityEqual(Entity var1) {
      return this == entityIn;
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
      return this.invulnerable && source != DamageSource.outOfWorld && !source.isCreativePlayer();
   }

   public void setEntityInvulnerable(boolean var1) {
      this.invulnerable = isInvulnerable;
   }

   public void copyLocationAndAnglesFrom(Entity var1) {
      this.setLocationAndAngles(entityIn.posX, entityIn.posY, entityIn.posZ, entityIn.rotationYaw, entityIn.rotationPitch);
   }

   private void copyDataFromOld(Entity var1) {
      NBTTagCompound nbttagcompound = entityIn.writeToNBT(new NBTTagCompound());
      nbttagcompound.removeTag("Dimension");
      this.readFromNBT(nbttagcompound);
      this.timeUntilPortal = entityIn.timeUntilPortal;
      this.lastPortalPos = entityIn.lastPortalPos;
      this.lastPortalVec = entityIn.lastPortalVec;
      this.teleportDirection = entityIn.teleportDirection;
   }

   @Nullable
   public Entity changeDimension(int var1) {
      if (!this.world.isRemote && !this.isDead) {
         if (!ForgeHooks.onTravelToDimension(this, dimensionIn)) {
            return null;
         } else {
            this.world.theProfiler.startSection("changeDimension");
            MinecraftServer minecraftserver = this.getServer();
            int i = this.dimension;
            WorldServer worldserver = minecraftserver.worldServerForDimension(i);
            WorldServer worldserver1 = minecraftserver.worldServerForDimension(dimensionIn);
            this.dimension = dimensionIn;
            if (i == 1 && dimensionIn == 1) {
               worldserver1 = minecraftserver.worldServerForDimension(0);
               this.dimension = 0;
            }

            this.world.removeEntity(this);
            this.isDead = false;
            this.world.theProfiler.startSection("reposition");
            BlockPos blockpos;
            if (dimensionIn == 1) {
               blockpos = worldserver1.getSpawnCoordinate();
            } else {
               double d0 = this.posX;
               double d1 = this.posZ;
               double d2 = 8.0D;
               if (dimensionIn == -1) {
                  d0 = MathHelper.clamp(d0 / 8.0D, worldserver1.getWorldBorder().minX() + 16.0D, worldserver1.getWorldBorder().maxX() - 16.0D);
                  d1 = MathHelper.clamp(d1 / 8.0D, worldserver1.getWorldBorder().minZ() + 16.0D, worldserver1.getWorldBorder().maxZ() - 16.0D);
               } else if (dimensionIn == 0) {
                  d0 = MathHelper.clamp(d0 * 8.0D, worldserver1.getWorldBorder().minX() + 16.0D, worldserver1.getWorldBorder().maxX() - 16.0D);
                  d1 = MathHelper.clamp(d1 * 8.0D, worldserver1.getWorldBorder().minZ() + 16.0D, worldserver1.getWorldBorder().maxZ() - 16.0D);
               }

               d0 = (double)MathHelper.clamp((int)d0, -29999872, 29999872);
               d1 = (double)MathHelper.clamp((int)d1, -29999872, 29999872);
               float f = this.rotationYaw;
               this.setLocationAndAngles(d0, this.posY, d1, 90.0F, 0.0F);
               Teleporter teleporter = worldserver1.getDefaultTeleporter();
               teleporter.placeInExistingPortal(this, f);
               blockpos = new BlockPos(this);
            }

            worldserver.updateEntityWithOptionalForce(this, false);
            this.world.theProfiler.endStartSection("reloading");
            Entity entity = EntityList.createEntityByName(EntityList.getEntityString(this), worldserver1);
            if (entity != null) {
               entity.copyDataFromOld(this);
               if (i == 1 && dimensionIn == 1) {
                  BlockPos blockpos1 = worldserver1.getTopSolidOrLiquidBlock(worldserver1.getSpawnPoint());
                  entity.moveToBlockPosAndAngles(blockpos1, entity.rotationYaw, entity.rotationPitch);
               } else {
                  entity.moveToBlockPosAndAngles(blockpos, entity.rotationYaw, entity.rotationPitch);
               }

               boolean flag = entity.forceSpawn;
               entity.forceSpawn = true;
               worldserver1.spawnEntity(entity);
               entity.forceSpawn = flag;
               worldserver1.updateEntityWithOptionalForce(entity, false);
            }

            this.isDead = true;
            this.world.theProfiler.endSection();
            worldserver.resetUpdateEntityTick();
            worldserver1.resetUpdateEntityTick();
            this.world.theProfiler.endSection();
            return entity;
         }
      } else {
         return null;
      }
   }

   public boolean isNonBoss() {
      return true;
   }

   public float getExplosionResistance(Explosion var1, World var2, BlockPos var3, IBlockState var4) {
      return blockStateIn.getBlock().getExplosionResistance(worldIn, pos, this, explosionIn);
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
      category.setDetail("Entity Type", new ICrashReportDetail() {
         public String call() throws Exception {
            return EntityList.getEntityString(Entity.this) + " (" + Entity.this.getClass().getCanonicalName() + ")";
         }
      });
      category.addCrashSection("Entity ID", Integer.valueOf(this.entityId));
      category.setDetail("Entity Name", new ICrashReportDetail() {
         public String call() throws Exception {
            return Entity.this.getName();
         }
      });
      category.addCrashSection("Entity's Exact location", String.format("%.2f, %.2f, %.2f", this.posX, this.posY, this.posZ));
      category.addCrashSection("Entity's Block location", CrashReportCategory.getCoordinateInfo(MathHelper.floor(this.posX), MathHelper.floor(this.posY), MathHelper.floor(this.posZ)));
      category.addCrashSection("Entity's Momentum", String.format("%.2f, %.2f, %.2f", this.motionX, this.motionY, this.motionZ));
      category.setDetail("Entity's Passengers", new ICrashReportDetail() {
         public String call() throws Exception {
            return Entity.this.getPassengers().toString();
         }
      });
      category.setDetail("Entity's Vehicle", new ICrashReportDetail() {
         public String call() throws Exception {
            return Entity.this.getRidingEntity().toString();
         }
      });
   }

   public void setUniqueId(UUID var1) {
      this.entityUniqueID = uniqueIdIn;
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
      renderDistanceWeight = renderDistWeight;
   }

   public ITextComponent getDisplayName() {
      TextComponentString textcomponentstring = new TextComponentString(ScorePlayerTeam.formatPlayerName(this.getTeam(), this.getName()));
      textcomponentstring.getStyle().setHoverEvent(this.getHoverEvent());
      textcomponentstring.getStyle().setInsertion(this.getCachedUniqueIdString());
      return textcomponentstring;
   }

   public void setCustomNameTag(String var1) {
      this.dataManager.set(CUSTOM_NAME, name);
   }

   public String getCustomNameTag() {
      return (String)this.dataManager.get(CUSTOM_NAME);
   }

   public boolean hasCustomName() {
      return !((String)this.dataManager.get(CUSTOM_NAME)).isEmpty();
   }

   public void setAlwaysRenderNameTag(boolean var1) {
      this.dataManager.set(CUSTOM_NAME_VISIBLE, Boolean.valueOf(alwaysRenderNameTag));
   }

   public boolean getAlwaysRenderNameTag() {
      return ((Boolean)this.dataManager.get(CUSTOM_NAME_VISIBLE)).booleanValue();
   }

   public void setPositionAndUpdate(double var1, double var3, double var5) {
      this.isPositionDirty = true;
      this.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
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
      NBTTagCompound nbttagcompound = new NBTTagCompound();
      String s = EntityList.getEntityString(this);
      nbttagcompound.setString("id", this.getCachedUniqueIdString());
      if (s != null) {
         nbttagcompound.setString("type", s);
      }

      nbttagcompound.setString("name", this.getName());
      return new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new TextComponentString(nbttagcompound.toString()));
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
      this.boundingBox = bb;
   }

   public float getEyeHeight() {
      return this.height * 0.85F;
   }

   public boolean isOutsideBorder() {
      return this.isOutsideBorder;
   }

   public void setOutsideBorder(boolean var1) {
      this.isOutsideBorder = outsideBorder;
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
         this.cmdResultStats.setCommandStatForSender(this.world.getMinecraftServer(), this, type, amount);
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
      this.cmdResultStats.addAllStats(entityIn.getCommandStats());
   }

   public EnumActionResult applyPlayerInteraction(EntityPlayer var1, Vec3d var2, @Nullable ItemStack var3, EnumHand var4) {
      return EnumActionResult.PASS;
   }

   public boolean isImmuneToExplosions() {
      return false;
   }

   protected void applyEnchantments(EntityLivingBase var1, Entity var2) {
      if (entityIn instanceof EntityLivingBase) {
         EnchantmentHelper.applyThornEnchantments((EntityLivingBase)entityIn, entityLivingBaseIn);
      }

      EnchantmentHelper.applyArthropodEnchantments(entityLivingBaseIn, entityIn);
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
         ItemStack held = ((EntityItemFrame)this).getDisplayedItem();
         return held == null ? new ItemStack(Items.ITEM_FRAME) : held.copy();
      } else if (this instanceof EntityMinecart) {
         return ((EntityMinecart)this).getCartItem();
      } else if (this instanceof EntityBoat) {
         return new ItemStack(((EntityBoat)this).getItemBoat());
      } else if (this instanceof EntityArmorStand) {
         return new ItemStack(Items.ARMOR_STAND);
      } else if (this instanceof EntityEnderCrystal) {
         return new ItemStack(Items.END_CRYSTAL);
      } else {
         String name = EntityList.getEntityString(this);
         if (EntityList.ENTITY_EGGS.containsKey(name)) {
            ItemStack stack = new ItemStack(Items.SPAWN_EGG);
            ItemMonsterPlacer.applyEntityIdToItemStack(stack, name);
            return stack;
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
      return pass == 0;
   }

   public boolean isCreatureType(EnumCreatureType var1, boolean var2) {
      return forSpawnCount && this instanceof EntityLiving && ((EntityLiving)this).isNoDespawnRequired() ? false : type.getCreatureClass().isAssignableFrom(this.getClass());
   }

   public boolean canRiderInteract() {
      return false;
   }

   public boolean shouldDismountInWater(Entity var1) {
      return this instanceof EntityLivingBase;
   }

   public boolean hasCapability(Capability var1, EnumFacing var2) {
      if (this.getCapability(capability, facing) != null) {
         return true;
      } else {
         return this.capabilities == null ? false : this.capabilities.hasCapability(capability, facing);
      }
   }

   public Object getCapability(Capability var1, EnumFacing var2) {
      return this.capabilities == null ? null : this.capabilities.getCapability(capability, facing);
   }

   public void deserializeNBT(NBTTagCompound var1) {
      this.readFromNBT(nbt);
   }

   public NBTTagCompound serializeNBT() {
      NBTTagCompound ret = new NBTTagCompound();
      ret.setString("id", this.getEntityString());
      return this.writeToNBT(ret);
   }

   public void addTrackingPlayer(EntityPlayerMP var1) {
   }

   public void removeTrackingPlayer(EntityPlayerMP var1) {
   }

   public float getRotatedYaw(Rotation var1) {
      float f = MathHelper.wrapDegrees(this.rotationYaw);
      switch(transformRotation) {
      case CLOCKWISE_180:
         return f + 180.0F;
      case COUNTERCLOCKWISE_90:
         return f + 270.0F;
      case CLOCKWISE_90:
         return f + 90.0F;
      default:
         return f;
      }
   }

   public float getMirroredYaw(Mirror var1) {
      float f = MathHelper.wrapDegrees(this.rotationYaw);
      switch(transformMirror) {
      case LEFT_RIGHT:
         return -f;
      case FRONT_BACK:
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

   public boolean isPassenger(Entity var1) {
      for(Entity entity : this.getPassengers()) {
         if (entity.equals(entityIn)) {
            return true;
         }
      }

      return false;
   }

   public Collection getRecursivePassengers() {
      Set set = Sets.newHashSet();
      this.getRecursivePassengersByType(Entity.class, set);
      return set;
   }

   public Collection getRecursivePassengersByType(Class var1) {
      Set set = Sets.newHashSet();
      this.getRecursivePassengersByType(entityClass, set);
      return set;
   }

   private void getRecursivePassengersByType(Class var1, Set var2) {
      for(Entity entity : this.getPassengers()) {
         if (entityClass.isAssignableFrom(entity.getClass())) {
            theSet.add(entity);
         }

         entity.getRecursivePassengersByType(entityClass, theSet);
      }

   }

   public Entity getLowestRidingEntity() {
      Entity entity;
      for(entity = this; entity.isRiding(); entity = entity.getRidingEntity()) {
         ;
      }

      return entity;
   }

   public boolean isRidingSameEntity(Entity var1) {
      return this.getLowestRidingEntity() == entityIn.getLowestRidingEntity();
   }

   public boolean isRidingOrBeingRiddenBy(Entity var1) {
      for(Entity entity : this.getPassengers()) {
         if (entity.equals(entityIn)) {
            return true;
         }

         if (entity.isRidingOrBeingRiddenBy(entityIn)) {
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
}
