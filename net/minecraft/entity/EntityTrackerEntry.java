package net.minecraft.entity;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.attributes.AttributeMap;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityDragonFireball;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketEntity;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketEntityEquipment;
import net.minecraft.network.play.server.SPacketEntityHeadLook;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraft.network.play.server.SPacketEntityProperties;
import net.minecraft.network.play.server.SPacketEntityTeleport;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.network.play.server.SPacketSpawnExperienceOrb;
import net.minecraft.network.play.server.SPacketSpawnMob;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.network.play.server.SPacketSpawnPainting;
import net.minecraft.network.play.server.SPacketSpawnPlayer;
import net.minecraft.network.play.server.SPacketUseBed;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.storage.MapData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

public class EntityTrackerEntry {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Entity trackedEntity;
   private final int range;
   private int maxRange;
   private final int updateFrequency;
   private long encodedPosX;
   private long encodedPosY;
   private long encodedPosZ;
   private int encodedRotationYaw;
   private int encodedRotationPitch;
   private int lastHeadMotion;
   private double lastTrackedEntityMotionX;
   private double lastTrackedEntityMotionY;
   private double motionZ;
   public int updateCounter;
   private double lastTrackedEntityPosX;
   private double lastTrackedEntityPosY;
   private double lastTrackedEntityPosZ;
   private boolean updatedPlayerVisibility;
   private final boolean sendVelocityUpdates;
   private int ticksSinceLastForcedTeleport;
   private List passengers = Collections.emptyList();
   private boolean ridingEntity;
   private boolean onGround;
   public boolean playerEntitiesUpdated;
   public final Set trackingPlayers = Sets.newHashSet();

   public EntityTrackerEntry(Entity entity, int i, int j, int k, boolean flag) {
      this.trackedEntity = entity;
      this.range = i;
      this.maxRange = j;
      this.updateFrequency = k;
      this.sendVelocityUpdates = flag;
      this.encodedPosX = EntityTracker.getPositionLong(entity.posX);
      this.encodedPosY = EntityTracker.getPositionLong(entity.posY);
      this.encodedPosZ = EntityTracker.getPositionLong(entity.posZ);
      this.encodedRotationYaw = MathHelper.floor(entity.rotationYaw * 256.0F / 360.0F);
      this.encodedRotationPitch = MathHelper.floor(entity.rotationPitch * 256.0F / 360.0F);
      this.lastHeadMotion = MathHelper.floor(entity.getRotationYawHead() * 256.0F / 360.0F);
      this.onGround = entity.onGround;
   }

   public boolean equals(Object object) {
      return object instanceof EntityTrackerEntry ? ((EntityTrackerEntry)object).trackedEntity.getEntityId() == this.trackedEntity.getEntityId() : false;
   }

   public int hashCode() {
      return this.trackedEntity.getEntityId();
   }

   public void updatePlayerList(List list) {
      this.playerEntitiesUpdated = false;
      if (!this.updatedPlayerVisibility || this.trackedEntity.getDistanceSq(this.lastTrackedEntityPosX, this.lastTrackedEntityPosY, this.lastTrackedEntityPosZ) > 16.0D) {
         this.lastTrackedEntityPosX = this.trackedEntity.posX;
         this.lastTrackedEntityPosY = this.trackedEntity.posY;
         this.lastTrackedEntityPosZ = this.trackedEntity.posZ;
         this.updatedPlayerVisibility = true;
         this.playerEntitiesUpdated = true;
         this.updatePlayerEntities(list);
      }

      List list1 = this.trackedEntity.getPassengers();
      if (!list1.equals(this.passengers)) {
         this.passengers = list1;
         this.sendPacketToTrackedPlayers(new SPacketSetPassengers(this.trackedEntity));
      }

      if (this.trackedEntity instanceof EntityItemFrame) {
         EntityItemFrame entityitemframe = (EntityItemFrame)this.trackedEntity;
         ItemStack itemstack = entityitemframe.getDisplayedItem();
         if (this.updateCounter % 10 == 0 && itemstack != null && itemstack.getItem() instanceof ItemMap) {
            MapData worldmap = Items.FILLED_MAP.getMapData(itemstack, this.trackedEntity.world);

            for(EntityPlayer entityhuman : this.trackingPlayers) {
               EntityPlayerMP entityplayer = (EntityPlayerMP)entityhuman;
               worldmap.updateVisiblePlayers(entityplayer, itemstack);
               Packet packet = Items.FILLED_MAP.createMapDataPacket(itemstack, this.trackedEntity.world, entityplayer);
               if (packet != null) {
                  entityplayer.connection.sendPacket(packet);
               }
            }
         }

         this.sendMetadata();
      }

      if (this.updateCounter % this.updateFrequency == 0 || this.trackedEntity.isAirBorne || this.trackedEntity.getDataManager().isDirty()) {
         if (this.trackedEntity.isRiding()) {
            int i = MathHelper.floor(this.trackedEntity.rotationYaw * 256.0F / 360.0F);
            int j = MathHelper.floor(this.trackedEntity.rotationPitch * 256.0F / 360.0F);
            boolean flag = Math.abs(i - this.encodedRotationYaw) >= 1 || Math.abs(j - this.encodedRotationPitch) >= 1;
            if (flag) {
               this.sendPacketToTrackedPlayers(new SPacketEntity.S16PacketEntityLook(this.trackedEntity.getEntityId(), (byte)i, (byte)j, this.trackedEntity.onGround));
               this.encodedRotationYaw = i;
               this.encodedRotationPitch = j;
            }

            this.encodedPosX = EntityTracker.getPositionLong(this.trackedEntity.posX);
            this.encodedPosY = EntityTracker.getPositionLong(this.trackedEntity.posY);
            this.encodedPosZ = EntityTracker.getPositionLong(this.trackedEntity.posZ);
            this.sendMetadata();
            this.ridingEntity = true;
         } else {
            ++this.ticksSinceLastForcedTeleport;
            long k = EntityTracker.getPositionLong(this.trackedEntity.posX);
            long l = EntityTracker.getPositionLong(this.trackedEntity.posY);
            long i1 = EntityTracker.getPositionLong(this.trackedEntity.posZ);
            int j1 = MathHelper.floor(this.trackedEntity.rotationYaw * 256.0F / 360.0F);
            int k1 = MathHelper.floor(this.trackedEntity.rotationPitch * 256.0F / 360.0F);
            long l1 = k - this.encodedPosX;
            long i2 = l - this.encodedPosY;
            long j2 = i1 - this.encodedPosZ;
            Object object = null;
            boolean flag1 = l1 * l1 + i2 * i2 + j2 * j2 >= 128L || this.updateCounter % 60 == 0;
            boolean flag2 = Math.abs(j1 - this.encodedRotationYaw) >= 1 || Math.abs(k1 - this.encodedRotationPitch) >= 1;
            if (flag1) {
               this.encodedPosX = k;
               this.encodedPosY = l;
               this.encodedPosZ = i1;
            }

            if (flag2) {
               this.encodedRotationYaw = j1;
               this.encodedRotationPitch = k1;
            }

            if (this.updateCounter > 0 || this.trackedEntity instanceof EntityArrow) {
               if (l1 >= -32768L && l1 < 32768L && i2 >= -32768L && i2 < 32768L && j2 >= -32768L && j2 < 32768L && this.ticksSinceLastForcedTeleport <= 400 && !this.ridingEntity && this.onGround == this.trackedEntity.onGround) {
                  if ((!flag1 || !flag2) && !(this.trackedEntity instanceof EntityArrow)) {
                     if (flag1) {
                        object = new SPacketEntity.S15PacketEntityRelMove(this.trackedEntity.getEntityId(), l1, i2, j2, this.trackedEntity.onGround);
                     } else if (flag2) {
                        object = new SPacketEntity.S16PacketEntityLook(this.trackedEntity.getEntityId(), (byte)j1, (byte)k1, this.trackedEntity.onGround);
                     }
                  } else {
                     object = new SPacketEntity.S17PacketEntityLookMove(this.trackedEntity.getEntityId(), l1, i2, j2, (byte)j1, (byte)k1, this.trackedEntity.onGround);
                  }
               } else {
                  this.onGround = this.trackedEntity.onGround;
                  this.ticksSinceLastForcedTeleport = 0;
                  if (this.trackedEntity instanceof EntityPlayerMP) {
                     this.updatePlayerEntities(new ArrayList(this.trackingPlayers));
                  }

                  this.resetPlayerVisibility();
                  object = new SPacketEntityTeleport(this.trackedEntity);
               }
            }

            boolean flag3 = this.sendVelocityUpdates;
            if (this.trackedEntity instanceof EntityLivingBase && ((EntityLivingBase)this.trackedEntity).isElytraFlying()) {
               flag3 = true;
            }

            if (flag3) {
               double d0 = this.trackedEntity.motionX - this.lastTrackedEntityMotionX;
               double d1 = this.trackedEntity.motionY - this.lastTrackedEntityMotionY;
               double d2 = this.trackedEntity.motionZ - this.motionZ;
               double d4 = d0 * d0 + d1 * d1 + d2 * d2;
               if (d4 > 4.0E-4D || d4 > 0.0D && this.trackedEntity.motionX == 0.0D && this.trackedEntity.motionY == 0.0D && this.trackedEntity.motionZ == 0.0D) {
                  this.lastTrackedEntityMotionX = this.trackedEntity.motionX;
                  this.lastTrackedEntityMotionY = this.trackedEntity.motionY;
                  this.motionZ = this.trackedEntity.motionZ;
                  this.sendPacketToTrackedPlayers(new SPacketEntityVelocity(this.trackedEntity.getEntityId(), this.lastTrackedEntityMotionX, this.lastTrackedEntityMotionY, this.motionZ));
               }
            }

            if (object != null) {
               this.sendPacketToTrackedPlayers((Packet)object);
            }

            this.sendMetadata();
            this.ridingEntity = false;
         }

         int i = MathHelper.floor(this.trackedEntity.getRotationYawHead() * 256.0F / 360.0F);
         if (Math.abs(i - this.lastHeadMotion) >= 1) {
            this.sendPacketToTrackedPlayers(new SPacketEntityHeadLook(this.trackedEntity, (byte)i));
            this.lastHeadMotion = i;
         }

         this.trackedEntity.isAirBorne = false;
      }

      ++this.updateCounter;
      if (this.trackedEntity.velocityChanged) {
         boolean cancelled = false;
         if (this.trackedEntity instanceof EntityPlayerMP) {
            Player player = (Player)this.trackedEntity.getBukkitEntity();
            Vector velocity = player.getVelocity();
            PlayerVelocityEvent event = new PlayerVelocityEvent(player, velocity.clone());
            this.trackedEntity.world.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
               cancelled = true;
            } else if (!velocity.equals(event.getVelocity())) {
               player.setVelocity(event.getVelocity());
            }
         }

         if (!cancelled) {
            this.sendToTrackingAndSelf(new SPacketEntityVelocity(this.trackedEntity));
         }

         this.trackedEntity.velocityChanged = false;
      }

   }

   private void sendMetadata() {
      EntityDataManager datawatcher = this.trackedEntity.getDataManager();
      if (datawatcher.isDirty()) {
         this.sendToTrackingAndSelf(new SPacketEntityMetadata(this.trackedEntity.getEntityId(), datawatcher, false));
      }

      if (this.trackedEntity instanceof EntityLivingBase) {
         AttributeMap attributemapserver = (AttributeMap)((EntityLivingBase)this.trackedEntity).getAttributeMap();
         Set set = attributemapserver.getAttributeInstanceSet();
         if (!set.isEmpty()) {
            if (this.trackedEntity instanceof EntityPlayerMP) {
               ((EntityPlayerMP)this.trackedEntity).getBukkitEntity().injectScaledMaxHealth(set, false);
            }

            this.sendToTrackingAndSelf(new SPacketEntityProperties(this.trackedEntity.getEntityId(), set));
         }

         set.clear();
      }

   }

   public void sendPacketToTrackedPlayers(Packet packet) {
      for(EntityPlayerMP entityplayer : this.trackingPlayers) {
         entityplayer.connection.sendPacket(packet);
      }

   }

   public void sendToTrackingAndSelf(Packet packet) {
      this.sendPacketToTrackedPlayers(packet);
      if (this.trackedEntity instanceof EntityPlayerMP) {
         ((EntityPlayerMP)this.trackedEntity).connection.sendPacket(packet);
      }

   }

   public void sendDestroyEntityPacketToTrackedPlayers() {
      for(EntityPlayerMP entityplayer : this.trackingPlayers) {
         this.trackedEntity.removeTrackingPlayer(entityplayer);
         entityplayer.removeEntity(this.trackedEntity);
      }

   }

   public void removeFromTrackedPlayers(EntityPlayerMP entityplayer) {
      if (this.trackingPlayers.contains(entityplayer)) {
         this.trackedEntity.removeTrackingPlayer(entityplayer);
         entityplayer.removeEntity(this.trackedEntity);
         this.trackingPlayers.remove(entityplayer);
      }

   }

   public void updatePlayerEntity(EntityPlayerMP entityplayer) {
      if (entityplayer != this.trackedEntity) {
         if (this.isVisibleTo(entityplayer)) {
            if (!this.trackingPlayers.contains(entityplayer) && (this.isPlayerWatchingThisChunk(entityplayer) || this.trackedEntity.forceSpawn)) {
               if (this.trackedEntity instanceof EntityPlayerMP) {
                  Player player = ((EntityPlayerMP)this.trackedEntity).getBukkitEntity();
                  if (!entityplayer.getBukkitEntity().canSee(player)) {
                     return;
                  }
               }

               entityplayer.entityRemoveQueue.remove(Integer.valueOf(this.trackedEntity.getEntityId()));
               this.trackingPlayers.add(entityplayer);
               Packet packet = this.createSpawnPacket();
               entityplayer.connection.sendPacket(packet);
               if (!this.trackedEntity.getDataManager().isEmpty()) {
                  entityplayer.connection.sendPacket(new SPacketEntityMetadata(this.trackedEntity.getEntityId(), this.trackedEntity.getDataManager(), true));
               }

               boolean flag = this.sendVelocityUpdates;
               if (this.trackedEntity instanceof EntityLivingBase) {
                  AttributeMap attributemapserver = (AttributeMap)((EntityLivingBase)this.trackedEntity).getAttributeMap();
                  Collection collection = attributemapserver.getWatchedAttributes();
                  if (this.trackedEntity.getEntityId() == entityplayer.getEntityId()) {
                     ((EntityPlayerMP)this.trackedEntity).getBukkitEntity().injectScaledMaxHealth(collection, false);
                  }

                  if (!collection.isEmpty()) {
                     entityplayer.connection.sendPacket(new SPacketEntityProperties(this.trackedEntity.getEntityId(), collection));
                  }

                  if (((EntityLivingBase)this.trackedEntity).isElytraFlying()) {
                     flag = true;
                  }
               }

               this.lastTrackedEntityMotionX = this.trackedEntity.motionX;
               this.lastTrackedEntityMotionY = this.trackedEntity.motionY;
               this.motionZ = this.trackedEntity.motionZ;
               if (flag && !(packet instanceof SPacketSpawnMob)) {
                  entityplayer.connection.sendPacket(new SPacketEntityVelocity(this.trackedEntity.getEntityId(), this.trackedEntity.motionX, this.trackedEntity.motionY, this.trackedEntity.motionZ));
               }

               if (this.trackedEntity instanceof EntityLivingBase) {
                  for(EntityEquipmentSlot enumitemslot : EntityEquipmentSlot.values()) {
                     ItemStack itemstack = ((EntityLivingBase)this.trackedEntity).getItemStackFromSlot(enumitemslot);
                     if (itemstack != null) {
                        entityplayer.connection.sendPacket(new SPacketEntityEquipment(this.trackedEntity.getEntityId(), enumitemslot, itemstack));
                     }
                  }
               }

               if (this.trackedEntity instanceof EntityPlayer) {
                  EntityPlayer entityhuman = (EntityPlayer)this.trackedEntity;
                  if (entityhuman.isPlayerSleeping()) {
                     entityplayer.connection.sendPacket(new SPacketUseBed(entityhuman, new BlockPos(this.trackedEntity)));
                  }
               }

               this.lastHeadMotion = MathHelper.floor(this.trackedEntity.getRotationYawHead() * 256.0F / 360.0F);
               this.sendPacketToTrackedPlayers(new SPacketEntityHeadLook(this.trackedEntity, (byte)this.lastHeadMotion));
               if (this.trackedEntity instanceof EntityLivingBase) {
                  EntityLivingBase entityliving = (EntityLivingBase)this.trackedEntity;

                  for(PotionEffect mobeffect : entityliving.getActivePotionEffects()) {
                     entityplayer.connection.sendPacket(new SPacketEntityEffect(this.trackedEntity.getEntityId(), mobeffect));
                  }
               }

               this.trackedEntity.addTrackingPlayer(entityplayer);
               entityplayer.addEntity(this.trackedEntity);
            }
         } else if (this.trackingPlayers.contains(entityplayer)) {
            this.trackingPlayers.remove(entityplayer);
            this.trackedEntity.removeTrackingPlayer(entityplayer);
            entityplayer.removeEntity(this.trackedEntity);
         }
      }

   }

   public boolean isVisibleTo(EntityPlayerMP entityplayer) {
      double d0 = entityplayer.posX - (double)this.encodedPosX / 4096.0D;
      double d1 = entityplayer.posZ - (double)this.encodedPosZ / 4096.0D;
      int i = Math.min(this.range, this.maxRange);
      return d0 >= (double)(-i) && d0 <= (double)i && d1 >= (double)(-i) && d1 <= (double)i && this.trackedEntity.isSpectatedByPlayer(entityplayer);
   }

   private boolean isPlayerWatchingThisChunk(EntityPlayerMP entityplayer) {
      return entityplayer.getServerWorld().getPlayerChunkMap().isPlayerWatchingChunk(entityplayer, this.trackedEntity.chunkCoordX, this.trackedEntity.chunkCoordZ);
   }

   public void updatePlayerEntities(List list) {
      for(int i = 0; i < list.size(); ++i) {
         this.updatePlayerEntity((EntityPlayerMP)list.get(i));
      }

   }

   private Packet createSpawnPacket() {
      if (this.trackedEntity.isDead) {
         return null;
      } else if (this.trackedEntity instanceof EntityItem) {
         return new SPacketSpawnObject(this.trackedEntity, 2, 1);
      } else if (this.trackedEntity instanceof EntityPlayerMP) {
         return new SPacketSpawnPlayer((EntityPlayer)this.trackedEntity);
      } else if (this.trackedEntity instanceof EntityMinecart) {
         EntityMinecart entityminecartabstract = (EntityMinecart)this.trackedEntity;
         return new SPacketSpawnObject(this.trackedEntity, 10, entityminecartabstract.getType().getId());
      } else if (this.trackedEntity instanceof EntityBoat) {
         return new SPacketSpawnObject(this.trackedEntity, 1);
      } else if (this.trackedEntity instanceof IAnimals) {
         this.lastHeadMotion = MathHelper.floor(this.trackedEntity.getRotationYawHead() * 256.0F / 360.0F);
         return new SPacketSpawnMob((EntityLivingBase)this.trackedEntity);
      } else if (this.trackedEntity instanceof EntityFishHook) {
         EntityPlayer entityhuman = ((EntityFishHook)this.trackedEntity).angler;
         return new SPacketSpawnObject(this.trackedEntity, 90, entityhuman != null ? entityhuman.getEntityId() : this.trackedEntity.getEntityId());
      } else if (this.trackedEntity instanceof EntitySpectralArrow) {
         Entity entity = ((EntitySpectralArrow)this.trackedEntity).shootingEntity;
         return new SPacketSpawnObject(this.trackedEntity, 91, 1 + (entity != null ? entity.getEntityId() : this.trackedEntity.getEntityId()));
      } else if (this.trackedEntity instanceof EntityTippedArrow) {
         Entity entity = ((EntityArrow)this.trackedEntity).shootingEntity;
         return new SPacketSpawnObject(this.trackedEntity, 60, 1 + (entity != null ? entity.getEntityId() : this.trackedEntity.getEntityId()));
      } else if (this.trackedEntity instanceof EntitySnowball) {
         return new SPacketSpawnObject(this.trackedEntity, 61);
      } else if (this.trackedEntity instanceof EntityPotion) {
         return new SPacketSpawnObject(this.trackedEntity, 73);
      } else if (this.trackedEntity instanceof EntityExpBottle) {
         return new SPacketSpawnObject(this.trackedEntity, 75);
      } else if (this.trackedEntity instanceof EntityEnderPearl) {
         return new SPacketSpawnObject(this.trackedEntity, 65);
      } else if (this.trackedEntity instanceof EntityEnderEye) {
         return new SPacketSpawnObject(this.trackedEntity, 72);
      } else if (this.trackedEntity instanceof EntityFireworkRocket) {
         return new SPacketSpawnObject(this.trackedEntity, 76);
      } else if (this.trackedEntity instanceof EntityFireball) {
         EntityFireball entityfireball = (EntityFireball)this.trackedEntity;
         SPacketSpawnObject packetplayoutspawnentity = null;
         byte b0 = 63;
         if (this.trackedEntity instanceof EntitySmallFireball) {
            b0 = 64;
         } else if (this.trackedEntity instanceof EntityDragonFireball) {
            b0 = 93;
         } else if (this.trackedEntity instanceof EntityWitherSkull) {
            b0 = 66;
         }

         if (entityfireball.shootingEntity != null) {
            packetplayoutspawnentity = new SPacketSpawnObject(this.trackedEntity, b0, ((EntityFireball)this.trackedEntity).shootingEntity.getEntityId());
         } else {
            packetplayoutspawnentity = new SPacketSpawnObject(this.trackedEntity, b0, 0);
         }

         packetplayoutspawnentity.setSpeedX((int)(entityfireball.accelerationX * 8000.0D));
         packetplayoutspawnentity.setSpeedY((int)(entityfireball.accelerationY * 8000.0D));
         packetplayoutspawnentity.setSpeedZ((int)(entityfireball.accelerationZ * 8000.0D));
         return packetplayoutspawnentity;
      } else if (this.trackedEntity instanceof EntityShulkerBullet) {
         SPacketSpawnObject packetplayoutspawnentity1 = new SPacketSpawnObject(this.trackedEntity, 67, 0);
         packetplayoutspawnentity1.setSpeedX((int)(this.trackedEntity.motionX * 8000.0D));
         packetplayoutspawnentity1.setSpeedY((int)(this.trackedEntity.motionY * 8000.0D));
         packetplayoutspawnentity1.setSpeedZ((int)(this.trackedEntity.motionZ * 8000.0D));
         return packetplayoutspawnentity1;
      } else if (this.trackedEntity instanceof EntityEgg) {
         return new SPacketSpawnObject(this.trackedEntity, 62);
      } else if (this.trackedEntity instanceof EntityTNTPrimed) {
         return new SPacketSpawnObject(this.trackedEntity, 50);
      } else if (this.trackedEntity instanceof EntityEnderCrystal) {
         return new SPacketSpawnObject(this.trackedEntity, 51);
      } else if (this.trackedEntity instanceof EntityFallingBlock) {
         EntityFallingBlock entityfallingblock = (EntityFallingBlock)this.trackedEntity;
         return new SPacketSpawnObject(this.trackedEntity, 70, Block.getStateId(entityfallingblock.getBlock()));
      } else if (this.trackedEntity instanceof EntityArmorStand) {
         return new SPacketSpawnObject(this.trackedEntity, 78);
      } else if (this.trackedEntity instanceof EntityPainting) {
         return new SPacketSpawnPainting((EntityPainting)this.trackedEntity);
      } else if (this.trackedEntity instanceof EntityItemFrame) {
         EntityItemFrame entityitemframe = (EntityItemFrame)this.trackedEntity;
         return new SPacketSpawnObject(this.trackedEntity, 71, entityitemframe.facingDirection.getHorizontalIndex(), entityitemframe.getHangingPosition());
      } else if (this.trackedEntity instanceof EntityLeashKnot) {
         EntityLeashKnot entityleash = (EntityLeashKnot)this.trackedEntity;
         return new SPacketSpawnObject(this.trackedEntity, 77, 0, entityleash.getHangingPosition());
      } else if (this.trackedEntity instanceof EntityXPOrb) {
         return new SPacketSpawnExperienceOrb((EntityXPOrb)this.trackedEntity);
      } else if (this.trackedEntity instanceof EntityAreaEffectCloud) {
         return new SPacketSpawnObject(this.trackedEntity, 3);
      } else {
         throw new IllegalArgumentException("Don't know how to add " + this.trackedEntity.getClass() + "!");
      }
   }

   public void removeTrackedPlayerSymmetric(EntityPlayerMP entityplayer) {
      if (this.trackingPlayers.contains(entityplayer)) {
         this.trackingPlayers.remove(entityplayer);
         this.trackedEntity.removeTrackingPlayer(entityplayer);
         entityplayer.removeEntity(this.trackedEntity);
      }

   }

   public Entity getTrackedEntity() {
      return this.trackedEntity;
   }

   public void setMaxRange(int i) {
      this.maxRange = i;
   }

   public void resetPlayerVisibility() {
      this.updatedPlayerVisibility = false;
   }
}
