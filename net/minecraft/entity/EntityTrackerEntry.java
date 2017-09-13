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
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
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

   public EntityTrackerEntry(Entity var1, int var2, int var3, int var4, boolean var5) {
      this.trackedEntity = var1;
      this.range = var2;
      this.maxRange = var3;
      this.updateFrequency = var4;
      this.sendVelocityUpdates = var5;
      this.encodedPosX = EntityTracker.getPositionLong(var1.posX);
      this.encodedPosY = EntityTracker.getPositionLong(var1.posY);
      this.encodedPosZ = EntityTracker.getPositionLong(var1.posZ);
      this.encodedRotationYaw = MathHelper.floor(var1.rotationYaw * 256.0F / 360.0F);
      this.encodedRotationPitch = MathHelper.floor(var1.rotationPitch * 256.0F / 360.0F);
      this.lastHeadMotion = MathHelper.floor(var1.getRotationYawHead() * 256.0F / 360.0F);
      this.onGround = var1.onGround;
   }

   public boolean equals(Object var1) {
      return var1 instanceof EntityTrackerEntry ? ((EntityTrackerEntry)var1).trackedEntity.getEntityId() == this.trackedEntity.getEntityId() : false;
   }

   public int hashCode() {
      return this.trackedEntity.getEntityId();
   }

   public void updatePlayerList(List var1) {
      this.playerEntitiesUpdated = false;
      if (!this.updatedPlayerVisibility || this.trackedEntity.getDistanceSq(this.lastTrackedEntityPosX, this.lastTrackedEntityPosY, this.lastTrackedEntityPosZ) > 16.0D) {
         this.lastTrackedEntityPosX = this.trackedEntity.posX;
         this.lastTrackedEntityPosY = this.trackedEntity.posY;
         this.lastTrackedEntityPosZ = this.trackedEntity.posZ;
         this.updatedPlayerVisibility = true;
         this.playerEntitiesUpdated = true;
         this.updatePlayerEntities(var1);
      }

      List var2 = this.trackedEntity.getPassengers();
      if (!var2.equals(this.passengers)) {
         this.passengers = var2;
         this.sendPacketToTrackedPlayers(new SPacketSetPassengers(this.trackedEntity));
      }

      if (this.trackedEntity instanceof EntityItemFrame) {
         EntityItemFrame var3 = (EntityItemFrame)this.trackedEntity;
         ItemStack var4 = var3.getDisplayedItem();
         if (this.updateCounter % 10 == 0 && var4 != null && var4.getItem() instanceof ItemMap) {
            MapData var5 = Items.FILLED_MAP.getMapData(var4, this.trackedEntity.world);

            for(EntityPlayer var7 : this.trackingPlayers) {
               EntityPlayerMP var8 = (EntityPlayerMP)var7;
               var5.updateVisiblePlayers(var8, var4);
               Packet var9 = Items.FILLED_MAP.createMapDataPacket(var4, this.trackedEntity.world, var8);
               if (var9 != null) {
                  var8.connection.sendPacket(var9);
               }
            }
         }

         this.sendMetadata();
      }

      if (this.updateCounter % this.updateFrequency == 0 || this.trackedEntity.isAirBorne || this.trackedEntity.getDataManager().isDirty()) {
         if (this.trackedEntity.isRiding()) {
            int var36 = MathHelper.floor(this.trackedEntity.rotationYaw * 256.0F / 360.0F);
            int var39 = MathHelper.floor(this.trackedEntity.rotationPitch * 256.0F / 360.0F);
            boolean var41 = Math.abs(var36 - this.encodedRotationYaw) >= 1 || Math.abs(var39 - this.encodedRotationPitch) >= 1;
            if (var41) {
               this.sendPacketToTrackedPlayers(new SPacketEntity.S16PacketEntityLook(this.trackedEntity.getEntityId(), (byte)var36, (byte)var39, this.trackedEntity.onGround));
               this.encodedRotationYaw = var36;
               this.encodedRotationPitch = var39;
            }

            this.encodedPosX = EntityTracker.getPositionLong(this.trackedEntity.posX);
            this.encodedPosY = EntityTracker.getPositionLong(this.trackedEntity.posY);
            this.encodedPosZ = EntityTracker.getPositionLong(this.trackedEntity.posZ);
            this.sendMetadata();
            this.ridingEntity = true;
         } else {
            ++this.ticksSinceLastForcedTeleport;
            long var10 = EntityTracker.getPositionLong(this.trackedEntity.posX);
            long var12 = EntityTracker.getPositionLong(this.trackedEntity.posY);
            long var14 = EntityTracker.getPositionLong(this.trackedEntity.posZ);
            int var16 = MathHelper.floor(this.trackedEntity.rotationYaw * 256.0F / 360.0F);
            int var17 = MathHelper.floor(this.trackedEntity.rotationPitch * 256.0F / 360.0F);
            long var18 = var10 - this.encodedPosX;
            long var20 = var12 - this.encodedPosY;
            long var22 = var14 - this.encodedPosZ;
            Object var24 = null;
            boolean var25 = var18 * var18 + var20 * var20 + var22 * var22 >= 128L || this.updateCounter % 60 == 0;
            boolean var26 = Math.abs(var16 - this.encodedRotationYaw) >= 1 || Math.abs(var17 - this.encodedRotationPitch) >= 1;
            if (var25) {
               this.encodedPosX = var10;
               this.encodedPosY = var12;
               this.encodedPosZ = var14;
            }

            if (var26) {
               this.encodedRotationYaw = var16;
               this.encodedRotationPitch = var17;
            }

            if (this.updateCounter > 0 || this.trackedEntity instanceof EntityArrow) {
               if (var18 >= -32768L && var18 < 32768L && var20 >= -32768L && var20 < 32768L && var22 >= -32768L && var22 < 32768L && this.ticksSinceLastForcedTeleport <= 400 && !this.ridingEntity && this.onGround == this.trackedEntity.onGround) {
                  if ((!var25 || !var26) && !(this.trackedEntity instanceof EntityArrow)) {
                     if (var25) {
                        var24 = new SPacketEntity.S15PacketEntityRelMove(this.trackedEntity.getEntityId(), var18, var20, var22, this.trackedEntity.onGround);
                     } else if (var26) {
                        var24 = new SPacketEntity.S16PacketEntityLook(this.trackedEntity.getEntityId(), (byte)var16, (byte)var17, this.trackedEntity.onGround);
                     }
                  } else {
                     var24 = new SPacketEntity.S17PacketEntityLookMove(this.trackedEntity.getEntityId(), var18, var20, var22, (byte)var16, (byte)var17, this.trackedEntity.onGround);
                  }
               } else {
                  this.onGround = this.trackedEntity.onGround;
                  this.ticksSinceLastForcedTeleport = 0;
                  if (this.trackedEntity instanceof EntityPlayerMP) {
                     this.updatePlayerEntities(new ArrayList(this.trackingPlayers));
                  }

                  this.resetPlayerVisibility();
                  var24 = new SPacketEntityTeleport(this.trackedEntity);
               }
            }

            boolean var27 = this.sendVelocityUpdates;
            if (this.trackedEntity instanceof EntityLivingBase && ((EntityLivingBase)this.trackedEntity).isElytraFlying()) {
               var27 = true;
            }

            if (var27) {
               double var28 = this.trackedEntity.motionX - this.lastTrackedEntityMotionX;
               double var30 = this.trackedEntity.motionY - this.lastTrackedEntityMotionY;
               double var32 = this.trackedEntity.motionZ - this.motionZ;
               double var34 = var28 * var28 + var30 * var30 + var32 * var32;
               if (var34 > 4.0E-4D || var34 > 0.0D && this.trackedEntity.motionX == 0.0D && this.trackedEntity.motionY == 0.0D && this.trackedEntity.motionZ == 0.0D) {
                  this.lastTrackedEntityMotionX = this.trackedEntity.motionX;
                  this.lastTrackedEntityMotionY = this.trackedEntity.motionY;
                  this.motionZ = this.trackedEntity.motionZ;
                  this.sendPacketToTrackedPlayers(new SPacketEntityVelocity(this.trackedEntity.getEntityId(), this.lastTrackedEntityMotionX, this.lastTrackedEntityMotionY, this.motionZ));
               }
            }

            if (var24 != null) {
               this.sendPacketToTrackedPlayers((Packet)var24);
            }

            this.sendMetadata();
            this.ridingEntity = false;
         }

         int var37 = MathHelper.floor(this.trackedEntity.getRotationYawHead() * 256.0F / 360.0F);
         if (Math.abs(var37 - this.lastHeadMotion) >= 1) {
            this.sendPacketToTrackedPlayers(new SPacketEntityHeadLook(this.trackedEntity, (byte)var37));
            this.lastHeadMotion = var37;
         }

         this.trackedEntity.isAirBorne = false;
      }

      ++this.updateCounter;
      if (this.trackedEntity.velocityChanged) {
         boolean var38 = false;
         if (this.trackedEntity instanceof EntityPlayerMP) {
            Player var40 = (Player)this.trackedEntity.getBukkitEntity();
            Vector var42 = var40.getVelocity();
            PlayerVelocityEvent var43 = new PlayerVelocityEvent(var40, var42.clone());
            this.trackedEntity.world.getServer().getPluginManager().callEvent(var43);
            if (var43.isCancelled()) {
               var38 = true;
            } else if (!var42.equals(var43.getVelocity())) {
               var40.setVelocity(var43.getVelocity());
            }
         }

         if (!var38) {
            this.sendToTrackingAndSelf(new SPacketEntityVelocity(this.trackedEntity));
         }

         this.trackedEntity.velocityChanged = false;
      }

   }

   private void sendMetadata() {
      EntityDataManager var1 = this.trackedEntity.getDataManager();
      if (var1.isDirty()) {
         this.sendToTrackingAndSelf(new SPacketEntityMetadata(this.trackedEntity.getEntityId(), var1, false));
      }

      if (this.trackedEntity instanceof EntityLivingBase) {
         AttributeMap var2 = (AttributeMap)((EntityLivingBase)this.trackedEntity).getAttributeMap();
         Set var3 = var2.getAttributeInstanceSet();
         if (!var3.isEmpty()) {
            if (this.trackedEntity instanceof EntityPlayerMP) {
               ((EntityPlayerMP)this.trackedEntity).getBukkitEntity().injectScaledMaxHealth(var3, false);
            }

            this.sendToTrackingAndSelf(new SPacketEntityProperties(this.trackedEntity.getEntityId(), var3));
         }

         var3.clear();
      }

   }

   public void sendPacketToTrackedPlayers(Packet var1) {
      for(EntityPlayerMP var3 : this.trackingPlayers) {
         var3.connection.sendPacket(var1);
      }

   }

   public void sendToTrackingAndSelf(Packet var1) {
      this.sendPacketToTrackedPlayers(var1);
      if (this.trackedEntity instanceof EntityPlayerMP) {
         ((EntityPlayerMP)this.trackedEntity).connection.sendPacket(var1);
      }

   }

   public void sendDestroyEntityPacketToTrackedPlayers() {
      for(EntityPlayerMP var2 : this.trackingPlayers) {
         this.trackedEntity.removeTrackingPlayer(var2);
         var2.removeEntity(this.trackedEntity);
      }

   }

   public void removeFromTrackedPlayers(EntityPlayerMP var1) {
      if (this.trackingPlayers.contains(var1)) {
         this.trackedEntity.removeTrackingPlayer(var1);
         var1.removeEntity(this.trackedEntity);
         this.trackingPlayers.remove(var1);
      }

   }

   public void updatePlayerEntity(EntityPlayerMP var1) {
      if (var1 != this.trackedEntity) {
         if (this.isVisibleTo(var1)) {
            if (!this.trackingPlayers.contains(var1) && (this.isPlayerWatchingThisChunk(var1) || this.trackedEntity.forceSpawn)) {
               if (this.trackedEntity instanceof EntityPlayerMP) {
                  CraftPlayer var2 = ((EntityPlayerMP)this.trackedEntity).getBukkitEntity();
                  if (!var1.getBukkitEntity().canSee(var2)) {
                     return;
                  }
               }

               var1.entityRemoveQueue.remove(Integer.valueOf(this.trackedEntity.getEntityId()));
               this.trackingPlayers.add(var1);
               Packet var9 = this.createSpawnPacket();
               var1.connection.sendPacket(var9);
               if (!this.trackedEntity.getDataManager().isEmpty()) {
                  var1.connection.sendPacket(new SPacketEntityMetadata(this.trackedEntity.getEntityId(), this.trackedEntity.getDataManager(), true));
               }

               boolean var3 = this.sendVelocityUpdates;
               if (this.trackedEntity instanceof EntityLivingBase) {
                  AttributeMap var4 = (AttributeMap)((EntityLivingBase)this.trackedEntity).getAttributeMap();
                  Collection var5 = var4.getWatchedAttributes();
                  if (this.trackedEntity.getEntityId() == var1.getEntityId()) {
                     ((EntityPlayerMP)this.trackedEntity).getBukkitEntity().injectScaledMaxHealth(var5, false);
                  }

                  if (!var5.isEmpty()) {
                     var1.connection.sendPacket(new SPacketEntityProperties(this.trackedEntity.getEntityId(), var5));
                  }

                  if (((EntityLivingBase)this.trackedEntity).isElytraFlying()) {
                     var3 = true;
                  }
               }

               this.lastTrackedEntityMotionX = this.trackedEntity.motionX;
               this.lastTrackedEntityMotionY = this.trackedEntity.motionY;
               this.motionZ = this.trackedEntity.motionZ;
               if (var3 && !(var9 instanceof SPacketSpawnMob)) {
                  var1.connection.sendPacket(new SPacketEntityVelocity(this.trackedEntity.getEntityId(), this.trackedEntity.motionX, this.trackedEntity.motionY, this.trackedEntity.motionZ));
               }

               if (this.trackedEntity instanceof EntityLivingBase) {
                  for(EntityEquipmentSlot var7 : EntityEquipmentSlot.values()) {
                     ItemStack var8 = ((EntityLivingBase)this.trackedEntity).getItemStackFromSlot(var7);
                     if (var8 != null) {
                        var1.connection.sendPacket(new SPacketEntityEquipment(this.trackedEntity.getEntityId(), var7, var8));
                     }
                  }
               }

               if (this.trackedEntity instanceof EntityPlayer) {
                  EntityPlayer var11 = (EntityPlayer)this.trackedEntity;
                  if (var11.isPlayerSleeping()) {
                     var1.connection.sendPacket(new SPacketUseBed(var11, new BlockPos(this.trackedEntity)));
                  }
               }

               this.lastHeadMotion = MathHelper.floor(this.trackedEntity.getRotationYawHead() * 256.0F / 360.0F);
               this.sendPacketToTrackedPlayers(new SPacketEntityHeadLook(this.trackedEntity, (byte)this.lastHeadMotion));
               if (this.trackedEntity instanceof EntityLivingBase) {
                  EntityLivingBase var12 = (EntityLivingBase)this.trackedEntity;

                  for(PotionEffect var15 : var12.getActivePotionEffects()) {
                     var1.connection.sendPacket(new SPacketEntityEffect(this.trackedEntity.getEntityId(), var15));
                  }
               }

               this.trackedEntity.addTrackingPlayer(var1);
               var1.addEntity(this.trackedEntity);
            }
         } else if (this.trackingPlayers.contains(var1)) {
            this.trackingPlayers.remove(var1);
            this.trackedEntity.removeTrackingPlayer(var1);
            var1.removeEntity(this.trackedEntity);
         }
      }

   }

   public boolean isVisibleTo(EntityPlayerMP var1) {
      double var2 = var1.posX - (double)this.encodedPosX / 4096.0D;
      double var4 = var1.posZ - (double)this.encodedPosZ / 4096.0D;
      int var6 = Math.min(this.range, this.maxRange);
      return var2 >= (double)(-var6) && var2 <= (double)var6 && var4 >= (double)(-var6) && var4 <= (double)var6 && this.trackedEntity.isSpectatedByPlayer(var1);
   }

   private boolean isPlayerWatchingThisChunk(EntityPlayerMP var1) {
      return var1.getServerWorld().getPlayerChunkMap().isPlayerWatchingChunk(var1, this.trackedEntity.chunkCoordX, this.trackedEntity.chunkCoordZ);
   }

   public void updatePlayerEntities(List var1) {
      for(int var2 = 0; var2 < var1.size(); ++var2) {
         this.updatePlayerEntity((EntityPlayerMP)var1.get(var2));
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
         EntityMinecart var7 = (EntityMinecart)this.trackedEntity;
         return new SPacketSpawnObject(this.trackedEntity, 10, var7.getType().getId());
      } else if (this.trackedEntity instanceof EntityBoat) {
         return new SPacketSpawnObject(this.trackedEntity, 1);
      } else if (this.trackedEntity instanceof IAnimals) {
         this.lastHeadMotion = MathHelper.floor(this.trackedEntity.getRotationYawHead() * 256.0F / 360.0F);
         return new SPacketSpawnMob((EntityLivingBase)this.trackedEntity);
      } else if (this.trackedEntity instanceof EntityFishHook) {
         EntityPlayer var6 = ((EntityFishHook)this.trackedEntity).angler;
         return new SPacketSpawnObject(this.trackedEntity, 90, var6 != null ? var6.getEntityId() : this.trackedEntity.getEntityId());
      } else if (this.trackedEntity instanceof EntitySpectralArrow) {
         Entity var5 = ((EntitySpectralArrow)this.trackedEntity).shootingEntity;
         return new SPacketSpawnObject(this.trackedEntity, 91, 1 + (var5 != null ? var5.getEntityId() : this.trackedEntity.getEntityId()));
      } else if (this.trackedEntity instanceof EntityTippedArrow) {
         Entity var1 = ((EntityArrow)this.trackedEntity).shootingEntity;
         return new SPacketSpawnObject(this.trackedEntity, 60, 1 + (var1 != null ? var1.getEntityId() : this.trackedEntity.getEntityId()));
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
         EntityFireball var11 = (EntityFireball)this.trackedEntity;
         SPacketSpawnObject var3 = null;
         byte var4 = 63;
         if (this.trackedEntity instanceof EntitySmallFireball) {
            var4 = 64;
         } else if (this.trackedEntity instanceof EntityDragonFireball) {
            var4 = 93;
         } else if (this.trackedEntity instanceof EntityWitherSkull) {
            var4 = 66;
         }

         if (var11.shootingEntity != null) {
            var3 = new SPacketSpawnObject(this.trackedEntity, var4, ((EntityFireball)this.trackedEntity).shootingEntity.getEntityId());
         } else {
            var3 = new SPacketSpawnObject(this.trackedEntity, var4, 0);
         }

         var3.setSpeedX((int)(var11.accelerationX * 8000.0D));
         var3.setSpeedY((int)(var11.accelerationY * 8000.0D));
         var3.setSpeedZ((int)(var11.accelerationZ * 8000.0D));
         return var3;
      } else if (this.trackedEntity instanceof EntityShulkerBullet) {
         SPacketSpawnObject var10 = new SPacketSpawnObject(this.trackedEntity, 67, 0);
         var10.setSpeedX((int)(this.trackedEntity.motionX * 8000.0D));
         var10.setSpeedY((int)(this.trackedEntity.motionY * 8000.0D));
         var10.setSpeedZ((int)(this.trackedEntity.motionZ * 8000.0D));
         return var10;
      } else if (this.trackedEntity instanceof EntityEgg) {
         return new SPacketSpawnObject(this.trackedEntity, 62);
      } else if (this.trackedEntity instanceof EntityTNTPrimed) {
         return new SPacketSpawnObject(this.trackedEntity, 50);
      } else if (this.trackedEntity instanceof EntityEnderCrystal) {
         return new SPacketSpawnObject(this.trackedEntity, 51);
      } else if (this.trackedEntity instanceof EntityFallingBlock) {
         EntityFallingBlock var9 = (EntityFallingBlock)this.trackedEntity;
         return new SPacketSpawnObject(this.trackedEntity, 70, Block.getStateId(var9.getBlock()));
      } else if (this.trackedEntity instanceof EntityArmorStand) {
         return new SPacketSpawnObject(this.trackedEntity, 78);
      } else if (this.trackedEntity instanceof EntityPainting) {
         return new SPacketSpawnPainting((EntityPainting)this.trackedEntity);
      } else if (this.trackedEntity instanceof EntityItemFrame) {
         EntityItemFrame var8 = (EntityItemFrame)this.trackedEntity;
         return new SPacketSpawnObject(this.trackedEntity, 71, var8.facingDirection.getHorizontalIndex(), var8.getHangingPosition());
      } else if (this.trackedEntity instanceof EntityLeashKnot) {
         EntityLeashKnot var2 = (EntityLeashKnot)this.trackedEntity;
         return new SPacketSpawnObject(this.trackedEntity, 77, 0, var2.getHangingPosition());
      } else if (this.trackedEntity instanceof EntityXPOrb) {
         return new SPacketSpawnExperienceOrb((EntityXPOrb)this.trackedEntity);
      } else if (this.trackedEntity instanceof EntityAreaEffectCloud) {
         return new SPacketSpawnObject(this.trackedEntity, 3);
      } else {
         throw new IllegalArgumentException("Don't know how to add " + this.trackedEntity.getClass() + "!");
      }
   }

   public void removeTrackedPlayerSymmetric(EntityPlayerMP var1) {
      if (this.trackingPlayers.contains(var1)) {
         this.trackingPlayers.remove(var1);
         this.trackedEntity.removeTrackingPlayer(var1);
         var1.removeEntity(this.trackedEntity);
      }

   }

   public Entity getTrackedEntity() {
      return this.trackedEntity;
   }

   public void setMaxRange(int var1) {
      this.maxRange = var1;
   }

   public void resetPlayerVisibility() {
      this.updatedPlayerVisibility = false;
   }
}
