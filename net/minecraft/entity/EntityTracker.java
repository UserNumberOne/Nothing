package net.minecraft.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Set;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketEntityAttach;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityTracker {
   private static final Logger LOGGER = LogManager.getLogger();
   private final WorldServer world;
   private final Set entries = Sets.newHashSet();
   public final IntHashMap trackedEntityHashTable = new IntHashMap();
   private int maxTrackingDistanceThreshold;

   public EntityTracker(WorldServer worldserver) {
      this.world = worldserver;
      this.maxTrackingDistanceThreshold = worldserver.getMinecraftServer().getPlayerList().getEntityViewDistance();
   }

   public static long getPositionLong(double d0) {
      return MathHelper.lfloor(d0 * 4096.0D);
   }

   public void track(Entity entity) {
      if (entity instanceof EntityPlayerMP) {
         this.track(entity, 512, 2);
         EntityPlayerMP entityplayer = (EntityPlayerMP)entity;

         for(EntityTrackerEntry entitytrackerentry : this.entries) {
            if (entitytrackerentry.getTrackedEntity() != entityplayer) {
               entitytrackerentry.updatePlayerEntity(entityplayer);
            }
         }
      } else if (entity instanceof EntityFishHook) {
         this.track(entity, 64, 5, true);
      } else if (entity instanceof EntityArrow) {
         this.track(entity, 64, 20, false);
      } else if (entity instanceof EntitySmallFireball) {
         this.track(entity, 64, 10, false);
      } else if (entity instanceof EntityFireball) {
         this.track(entity, 64, 10, false);
      } else if (entity instanceof EntitySnowball) {
         this.track(entity, 64, 10, true);
      } else if (entity instanceof EntityEnderPearl) {
         this.track(entity, 64, 10, true);
      } else if (entity instanceof EntityEnderEye) {
         this.track(entity, 64, 4, true);
      } else if (entity instanceof EntityEgg) {
         this.track(entity, 64, 10, true);
      } else if (entity instanceof EntityPotion) {
         this.track(entity, 64, 10, true);
      } else if (entity instanceof EntityExpBottle) {
         this.track(entity, 64, 10, true);
      } else if (entity instanceof EntityFireworkRocket) {
         this.track(entity, 64, 10, true);
      } else if (entity instanceof EntityItem) {
         this.track(entity, 64, 20, true);
      } else if (entity instanceof EntityMinecart) {
         this.track(entity, 80, 3, true);
      } else if (entity instanceof EntityBoat) {
         this.track(entity, 80, 3, true);
      } else if (entity instanceof EntitySquid) {
         this.track(entity, 64, 3, true);
      } else if (entity instanceof EntityWither) {
         this.track(entity, 80, 3, false);
      } else if (entity instanceof EntityShulkerBullet) {
         this.track(entity, 80, 3, true);
      } else if (entity instanceof EntityBat) {
         this.track(entity, 80, 3, false);
      } else if (entity instanceof EntityDragon) {
         this.track(entity, 160, 3, true);
      } else if (entity instanceof IAnimals) {
         this.track(entity, 80, 3, true);
      } else if (entity instanceof EntityTNTPrimed) {
         this.track(entity, 160, 10, true);
      } else if (entity instanceof EntityFallingBlock) {
         this.track(entity, 160, 20, true);
      } else if (entity instanceof EntityHanging) {
         this.track(entity, 160, Integer.MAX_VALUE, false);
      } else if (entity instanceof EntityArmorStand) {
         this.track(entity, 160, 3, true);
      } else if (entity instanceof EntityXPOrb) {
         this.track(entity, 160, 20, true);
      } else if (entity instanceof EntityAreaEffectCloud) {
         this.track(entity, 160, Integer.MAX_VALUE, true);
      } else if (entity instanceof EntityEnderCrystal) {
         this.track(entity, 256, Integer.MAX_VALUE, false);
      }

   }

   public void track(Entity entity, int i, int j) {
      this.track(entity, i, j, false);
   }

   public void track(Entity entity, final int i, int j, boolean flag) {
      try {
         if (this.trackedEntityHashTable.containsItem(entity.getEntityId())) {
            throw new IllegalStateException("Entity is already tracked!");
         }

         EntityTrackerEntry entitytrackerentry = new EntityTrackerEntry(entity, i, this.maxTrackingDistanceThreshold, j, flag);
         this.entries.add(entitytrackerentry);
         this.trackedEntityHashTable.addKey(entity.getEntityId(), entitytrackerentry);
         entitytrackerentry.updatePlayerEntities(this.world.playerEntities);
      } catch (Throwable var11) {
         CrashReport crashreport = CrashReport.makeCrashReport(var11, "Adding entity to track");
         CrashReportCategory crashreportsystemdetails = crashreport.makeCategory("Entity To Track");
         crashreportsystemdetails.addCrashSection("Tracking range", i + " blocks");
         crashreportsystemdetails.setDetail("Update interval", new ICrashReportDetail() {
            public String call() throws Exception {
               String s = "Once per " + i + " ticks";
               if (i == Integer.MAX_VALUE) {
                  s = "Maximum (" + s + ")";
               }

               return s;
            }

            public Object call() throws Exception {
               return this.call();
            }
         });
         entity.addEntityCrashInfo(crashreportsystemdetails);
         ((EntityTrackerEntry)this.trackedEntityHashTable.lookup(entity.getEntityId())).getTrackedEntity().addEntityCrashInfo(crashreport.makeCategory("Entity That Is Already Tracked"));

         try {
            throw new ReportedException(crashreport);
         } catch (ReportedException var10) {
            LOGGER.error("\"Silently\" catching entity tracking error.", var10);
         }
      }

   }

   public void untrack(Entity entity) {
      if (entity instanceof EntityPlayerMP) {
         EntityPlayerMP entityplayer = (EntityPlayerMP)entity;

         for(EntityTrackerEntry entitytrackerentry : this.entries) {
            entitytrackerentry.removeFromTrackedPlayers(entityplayer);
         }
      }

      EntityTrackerEntry entitytrackerentry1 = (EntityTrackerEntry)this.trackedEntityHashTable.removeObject(entity.getEntityId());
      if (entitytrackerentry1 != null) {
         this.entries.remove(entitytrackerentry1);
         entitytrackerentry1.sendDestroyEntityPacketToTrackedPlayers();
      }

   }

   public void tick() {
      ArrayList arraylist = Lists.newArrayList();

      for(EntityTrackerEntry entitytrackerentry : this.entries) {
         entitytrackerentry.updatePlayerList(this.world.playerEntities);
         if (entitytrackerentry.playerEntitiesUpdated) {
            Entity entity = entitytrackerentry.getTrackedEntity();
            if (entity instanceof EntityPlayerMP) {
               arraylist.add((EntityPlayerMP)entity);
            }
         }
      }

      for(int i = 0; i < arraylist.size(); ++i) {
         EntityPlayerMP entityplayer = (EntityPlayerMP)arraylist.get(i);

         for(EntityTrackerEntry entitytrackerentry1 : this.entries) {
            if (entitytrackerentry1.getTrackedEntity() != entityplayer) {
               entitytrackerentry1.updatePlayerEntity(entityplayer);
            }
         }
      }

   }

   public void updateVisibility(EntityPlayerMP entityplayer) {
      for(EntityTrackerEntry entitytrackerentry : this.entries) {
         if (entitytrackerentry.getTrackedEntity() == entityplayer) {
            entitytrackerentry.updatePlayerEntities(this.world.playerEntities);
         } else {
            entitytrackerentry.updatePlayerEntity(entityplayer);
         }
      }

   }

   public void sendToTracking(Entity entity, Packet packet) {
      EntityTrackerEntry entitytrackerentry = (EntityTrackerEntry)this.trackedEntityHashTable.lookup(entity.getEntityId());
      if (entitytrackerentry != null) {
         entitytrackerentry.sendPacketToTrackedPlayers(packet);
      }

   }

   public void sendToTrackingAndSelf(Entity entity, Packet packet) {
      EntityTrackerEntry entitytrackerentry = (EntityTrackerEntry)this.trackedEntityHashTable.lookup(entity.getEntityId());
      if (entitytrackerentry != null) {
         entitytrackerentry.sendToTrackingAndSelf(packet);
      }

   }

   public void removePlayerFromTrackers(EntityPlayerMP entityplayer) {
      for(EntityTrackerEntry entitytrackerentry : this.entries) {
         entitytrackerentry.removeTrackedPlayerSymmetric(entityplayer);
      }

   }

   public void sendLeashedEntitiesInChunk(EntityPlayerMP entityplayer, Chunk chunk) {
      ArrayList arraylist = Lists.newArrayList();
      ArrayList arraylist1 = Lists.newArrayList();

      for(EntityTrackerEntry entitytrackerentry : this.entries) {
         Entity entity = entitytrackerentry.getTrackedEntity();
         if (entity != entityplayer && entity.chunkCoordX == chunk.xPosition && entity.chunkCoordZ == chunk.zPosition) {
            entitytrackerentry.updatePlayerEntity(entityplayer);
            if (entity instanceof EntityLiving && ((EntityLiving)entity).getLeashedToEntity() != null) {
               arraylist.add(entity);
            }

            if (!entity.getPassengers().isEmpty()) {
               arraylist1.add(entity);
            }
         }
      }

      if (!arraylist.isEmpty()) {
         for(Entity entity1 : arraylist) {
            entityplayer.connection.sendPacket(new SPacketEntityAttach(entity1, ((EntityLiving)entity1).getLeashedToEntity()));
         }
      }

      if (!arraylist1.isEmpty()) {
         for(Entity entity1 : arraylist1) {
            entityplayer.connection.sendPacket(new SPacketSetPassengers(entity1));
         }
      }

   }

   public void setViewDistance(int i) {
      this.maxTrackingDistanceThreshold = (i - 1) * 16;

      for(EntityTrackerEntry entitytrackerentry : this.entries) {
         entitytrackerentry.setMaxRange(this.maxTrackingDistanceThreshold);
      }

   }
}
