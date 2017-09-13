package net.minecraft.entity;

import com.google.common.collect.Sets;
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
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

      if (this.trackedEntity instanceof EntityItemFrame && this.updateCounter % 10 == 0) {
         EntityItemFrame var3 = (EntityItemFrame)this.trackedEntity;
         ItemStack var4 = var3.getDisplayedItem();
         if (var4 != null && var4.getItem() instanceof ItemMap) {
            MapData var5 = Items.FILLED_MAP.getMapData(var4, this.trackedEntity.world);

            for(EntityPlayer var7 : var1) {
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
            int var32 = MathHelper.floor(this.trackedEntity.rotationYaw * 256.0F / 360.0F);
            int var34 = MathHelper.floor(this.trackedEntity.rotationPitch * 256.0F / 360.0F);
            boolean var36 = Math.abs(var32 - this.encodedRotationYaw) >= 1 || Math.abs(var34 - this.encodedRotationPitch) >= 1;
            if (var36) {
               this.sendPacketToTrackedPlayers(new SPacketEntity.S16PacketEntityLook(this.trackedEntity.getEntityId(), (byte)var32, (byte)var34, this.trackedEntity.onGround));
               this.encodedRotationYaw = var32;
               this.encodedRotationPitch = var34;
            }

            this.encodedPosX = EntityTracker.getPositionLong(this.trackedEntity.posX);
            this.encodedPosY = EntityTracker.getPositionLong(this.trackedEntity.posY);
            this.encodedPosZ = EntityTracker.getPositionLong(this.trackedEntity.posZ);
            this.sendMetadata();
            this.ridingEntity = true;
         } else {
            ++this.ticksSinceLastForcedTeleport;
            long var31 = EntityTracker.getPositionLong(this.trackedEntity.posX);
            long var35 = EntityTracker.getPositionLong(this.trackedEntity.posY);
            long var37 = EntityTracker.getPositionLong(this.trackedEntity.posZ);
            int var38 = MathHelper.floor(this.trackedEntity.rotationYaw * 256.0F / 360.0F);
            int var10 = MathHelper.floor(this.trackedEntity.rotationPitch * 256.0F / 360.0F);
            long var11 = var31 - this.encodedPosX;
            long var13 = var35 - this.encodedPosY;
            long var15 = var37 - this.encodedPosZ;
            Object var17 = null;
            boolean var18 = var11 * var11 + var13 * var13 + var15 * var15 >= 128L || this.updateCounter % 60 == 0;
            boolean var19 = Math.abs(var38 - this.encodedRotationYaw) >= 1 || Math.abs(var10 - this.encodedRotationPitch) >= 1;
            if (this.updateCounter > 0 || this.trackedEntity instanceof EntityArrow) {
               if (var11 >= -32768L && var11 < 32768L && var13 >= -32768L && var13 < 32768L && var15 >= -32768L && var15 < 32768L && this.ticksSinceLastForcedTeleport <= 400 && !this.ridingEntity && this.onGround == this.trackedEntity.onGround) {
                  if ((!var18 || !var19) && !(this.trackedEntity instanceof EntityArrow)) {
                     if (var18) {
                        var17 = new SPacketEntity.S15PacketEntityRelMove(this.trackedEntity.getEntityId(), var11, var13, var15, this.trackedEntity.onGround);
                     } else if (var19) {
                        var17 = new SPacketEntity.S16PacketEntityLook(this.trackedEntity.getEntityId(), (byte)var38, (byte)var10, this.trackedEntity.onGround);
                     }
                  } else {
                     var17 = new SPacketEntity.S17PacketEntityLookMove(this.trackedEntity.getEntityId(), var11, var13, var15, (byte)var38, (byte)var10, this.trackedEntity.onGround);
                  }
               } else {
                  this.onGround = this.trackedEntity.onGround;
                  this.ticksSinceLastForcedTeleport = 0;
                  this.resetPlayerVisibility();
                  var17 = new SPacketEntityTeleport(this.trackedEntity);
               }
            }

            boolean var20 = this.sendVelocityUpdates;
            if (this.trackedEntity instanceof EntityLivingBase && ((EntityLivingBase)this.trackedEntity).isElytraFlying()) {
               var20 = true;
            }

            if (var20) {
               double var21 = this.trackedEntity.motionX - this.lastTrackedEntityMotionX;
               double var23 = this.trackedEntity.motionY - this.lastTrackedEntityMotionY;
               double var25 = this.trackedEntity.motionZ - this.motionZ;
               double var27 = 0.02D;
               double var29 = var21 * var21 + var23 * var23 + var25 * var25;
               if (var29 > 4.0E-4D || var29 > 0.0D && this.trackedEntity.motionX == 0.0D && this.trackedEntity.motionY == 0.0D && this.trackedEntity.motionZ == 0.0D) {
                  this.lastTrackedEntityMotionX = this.trackedEntity.motionX;
                  this.lastTrackedEntityMotionY = this.trackedEntity.motionY;
                  this.motionZ = this.trackedEntity.motionZ;
                  this.sendPacketToTrackedPlayers(new SPacketEntityVelocity(this.trackedEntity.getEntityId(), this.lastTrackedEntityMotionX, this.lastTrackedEntityMotionY, this.motionZ));
               }
            }

            if (var17 != null) {
               this.sendPacketToTrackedPlayers((Packet)var17);
            }

            this.sendMetadata();
            if (var18) {
               this.encodedPosX = var31;
               this.encodedPosY = var35;
               this.encodedPosZ = var37;
            }

            if (var19) {
               this.encodedRotationYaw = var38;
               this.encodedRotationPitch = var10;
            }

            this.ridingEntity = false;
         }

         int var33 = MathHelper.floor(this.trackedEntity.getRotationYawHead() * 256.0F / 360.0F);
         if (Math.abs(var33 - this.lastHeadMotion) >= 1) {
            this.sendPacketToTrackedPlayers(new SPacketEntityHeadLook(this.trackedEntity, (byte)var33));
            this.lastHeadMotion = var33;
         }

         this.trackedEntity.isAirBorne = false;
      }

      ++this.updateCounter;
      if (this.trackedEntity.velocityChanged) {
         this.sendToTrackingAndSelf(new SPacketEntityVelocity(this.trackedEntity));
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
               this.trackingPlayers.add(var1);
               Packet var2 = this.createSpawnPacket();
               var1.connection.sendPacket(var2);
               if (!this.trackedEntity.getDataManager().isEmpty()) {
                  var1.connection.sendPacket(new SPacketEntityMetadata(this.trackedEntity.getEntityId(), this.trackedEntity.getDataManager(), true));
               }

               boolean var3 = this.sendVelocityUpdates;
               if (this.trackedEntity instanceof EntityLivingBase) {
                  AttributeMap var4 = (AttributeMap)((EntityLivingBase)this.trackedEntity).getAttributeMap();
                  Collection var5 = var4.getWatchedAttributes();
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
               if (var3 && !(var2 instanceof SPacketSpawnMob)) {
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
                  EntityPlayer var10 = (EntityPlayer)this.trackedEntity;
                  if (var10.isPlayerSleeping()) {
                     var1.connection.sendPacket(new SPacketUseBed(var10, new BlockPos(this.trackedEntity)));
                  }
               }

               if (this.trackedEntity instanceof EntityLivingBase) {
                  EntityLivingBase var11 = (EntityLivingBase)this.trackedEntity;

                  for(PotionEffect var14 : var11.getActivePotionEffects()) {
                     var1.connection.sendPacket(new SPacketEntityEffect(this.trackedEntity.getEntityId(), var14));
                  }
               }

               this.trackedEntity.addTrackingPlayer(var1);
               var1.addEntity(this.trackedEntity);
               ForgeEventFactory.onStartEntityTracking(this.trackedEntity, var1);
            }
         } else if (this.trackingPlayers.contains(var1)) {
            this.trackingPlayers.remove(var1);
            this.trackedEntity.removeTrackingPlayer(var1);
            var1.removeEntity(this.trackedEntity);
            ForgeEventFactory.onStopEntityTracking(this.trackedEntity, var1);
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
         LOGGER.warn("Fetching addPacket for removed entity");
      }

      Packet var1 = FMLNetworkHandler.getEntitySpawningPacket(this.trackedEntity);
      if (var1 != null) {
         return var1;
      } else if (this.trackedEntity instanceof EntityItem) {
         return new SPacketSpawnObject(this.trackedEntity, 2, 1);
      } else if (this.trackedEntity instanceof EntityPlayerMP) {
         return new SPacketSpawnPlayer((EntityPlayer)this.trackedEntity);
      } else if (this.trackedEntity instanceof EntityMinecart) {
         EntityMinecart var12 = (EntityMinecart)this.trackedEntity;
         return new SPacketSpawnObject(this.trackedEntity, 10, var12.getType().getId());
      } else if (this.trackedEntity instanceof EntityBoat) {
         return new SPacketSpawnObject(this.trackedEntity, 1);
      } else if (this.trackedEntity instanceof IAnimals) {
         this.lastHeadMotion = MathHelper.floor(this.trackedEntity.getRotationYawHead() * 256.0F / 360.0F);
         return new SPacketSpawnMob((EntityLivingBase)this.trackedEntity);
      } else if (this.trackedEntity instanceof EntityFishHook) {
         EntityPlayer var11 = ((EntityFishHook)this.trackedEntity).angler;
         return new SPacketSpawnObject(this.trackedEntity, 90, var11 != null ? var11.getEntityId() : this.trackedEntity.getEntityId());
      } else if (this.trackedEntity instanceof EntitySpectralArrow) {
         Entity var10 = ((EntitySpectralArrow)this.trackedEntity).shootingEntity;
         return new SPacketSpawnObject(this.trackedEntity, 91, 1 + (var10 != null ? var10.getEntityId() : this.trackedEntity.getEntityId()));
      } else if (this.trackedEntity instanceof EntityTippedArrow) {
         Entity var9 = ((EntityArrow)this.trackedEntity).shootingEntity;
         return new SPacketSpawnObject(this.trackedEntity, 60, 1 + (var9 != null ? var9.getEntityId() : this.trackedEntity.getEntityId()));
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
         EntityFireball var8 = (EntityFireball)this.trackedEntity;
         SPacketSpawnObject var3 = null;
         byte var4 = 63;
         if (this.trackedEntity instanceof EntitySmallFireball) {
            var4 = 64;
         } else if (this.trackedEntity instanceof EntityDragonFireball) {
            var4 = 93;
         } else if (this.trackedEntity instanceof EntityWitherSkull) {
            var4 = 66;
         }

         if (var8.shootingEntity != null) {
            var3 = new SPacketSpawnObject(this.trackedEntity, var4, ((EntityFireball)this.trackedEntity).shootingEntity.getEntityId());
         } else {
            var3 = new SPacketSpawnObject(this.trackedEntity, var4, 0);
         }

         var3.setSpeedX((int)(var8.accelerationX * 8000.0D));
         var3.setSpeedY((int)(var8.accelerationY * 8000.0D));
         var3.setSpeedZ((int)(var8.accelerationZ * 8000.0D));
         return var3;
      } else if (this.trackedEntity instanceof EntityShulkerBullet) {
         SPacketSpawnObject var7 = new SPacketSpawnObject(this.trackedEntity, 67, 0);
         var7.setSpeedX((int)(this.trackedEntity.motionX * 8000.0D));
         var7.setSpeedY((int)(this.trackedEntity.motionY * 8000.0D));
         var7.setSpeedZ((int)(this.trackedEntity.motionZ * 8000.0D));
         return var7;
      } else if (this.trackedEntity instanceof EntityEgg) {
         return new SPacketSpawnObject(this.trackedEntity, 62);
      } else if (this.trackedEntity instanceof EntityTNTPrimed) {
         return new SPacketSpawnObject(this.trackedEntity, 50);
      } else if (this.trackedEntity instanceof EntityEnderCrystal) {
         return new SPacketSpawnObject(this.trackedEntity, 51);
      } else if (this.trackedEntity instanceof EntityFallingBlock) {
         EntityFallingBlock var6 = (EntityFallingBlock)this.trackedEntity;
         return new SPacketSpawnObject(this.trackedEntity, 70, Block.getStateId(var6.getBlock()));
      } else if (this.trackedEntity instanceof EntityArmorStand) {
         return new SPacketSpawnObject(this.trackedEntity, 78);
      } else if (this.trackedEntity instanceof EntityPainting) {
         return new SPacketSpawnPainting((EntityPainting)this.trackedEntity);
      } else if (this.trackedEntity instanceof EntityItemFrame) {
         EntityItemFrame var5 = (EntityItemFrame)this.trackedEntity;
         return new SPacketSpawnObject(this.trackedEntity, 71, var5.facingDirection.getHorizontalIndex(), var5.getHangingPosition());
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
