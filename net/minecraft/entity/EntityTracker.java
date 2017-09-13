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

   public EntityTracker(WorldServer var1) {
      this.world = var1;
      this.maxTrackingDistanceThreshold = var1.getMinecraftServer().getPlayerList().getEntityViewDistance();
   }

   public static long getPositionLong(double var0) {
      return MathHelper.lfloor(var0 * 4096.0D);
   }

   public void track(Entity var1) {
      if (var1 instanceof EntityPlayerMP) {
         this.track(var1, 512, 2);
         EntityPlayerMP var2 = (EntityPlayerMP)var1;

         for(EntityTrackerEntry var4 : this.entries) {
            if (var4.getTrackedEntity() != var2) {
               var4.updatePlayerEntity(var2);
            }
         }
      } else if (var1 instanceof EntityFishHook) {
         this.track(var1, 64, 5, true);
      } else if (var1 instanceof EntityArrow) {
         this.track(var1, 64, 20, false);
      } else if (var1 instanceof EntitySmallFireball) {
         this.track(var1, 64, 10, false);
      } else if (var1 instanceof EntityFireball) {
         this.track(var1, 64, 10, false);
      } else if (var1 instanceof EntitySnowball) {
         this.track(var1, 64, 10, true);
      } else if (var1 instanceof EntityEnderPearl) {
         this.track(var1, 64, 10, true);
      } else if (var1 instanceof EntityEnderEye) {
         this.track(var1, 64, 4, true);
      } else if (var1 instanceof EntityEgg) {
         this.track(var1, 64, 10, true);
      } else if (var1 instanceof EntityPotion) {
         this.track(var1, 64, 10, true);
      } else if (var1 instanceof EntityExpBottle) {
         this.track(var1, 64, 10, true);
      } else if (var1 instanceof EntityFireworkRocket) {
         this.track(var1, 64, 10, true);
      } else if (var1 instanceof EntityItem) {
         this.track(var1, 64, 20, true);
      } else if (var1 instanceof EntityMinecart) {
         this.track(var1, 80, 3, true);
      } else if (var1 instanceof EntityBoat) {
         this.track(var1, 80, 3, true);
      } else if (var1 instanceof EntitySquid) {
         this.track(var1, 64, 3, true);
      } else if (var1 instanceof EntityWither) {
         this.track(var1, 80, 3, false);
      } else if (var1 instanceof EntityShulkerBullet) {
         this.track(var1, 80, 3, true);
      } else if (var1 instanceof EntityBat) {
         this.track(var1, 80, 3, false);
      } else if (var1 instanceof EntityDragon) {
         this.track(var1, 160, 3, true);
      } else if (var1 instanceof IAnimals) {
         this.track(var1, 80, 3, true);
      } else if (var1 instanceof EntityTNTPrimed) {
         this.track(var1, 160, 10, true);
      } else if (var1 instanceof EntityFallingBlock) {
         this.track(var1, 160, 20, true);
      } else if (var1 instanceof EntityHanging) {
         this.track(var1, 160, Integer.MAX_VALUE, false);
      } else if (var1 instanceof EntityArmorStand) {
         this.track(var1, 160, 3, true);
      } else if (var1 instanceof EntityXPOrb) {
         this.track(var1, 160, 20, true);
      } else if (var1 instanceof EntityAreaEffectCloud) {
         this.track(var1, 160, Integer.MAX_VALUE, true);
      } else if (var1 instanceof EntityEnderCrystal) {
         this.track(var1, 256, Integer.MAX_VALUE, false);
      }

   }

   public void track(Entity var1, int var2, int var3) {
      this.track(var1, var2, var3, false);
   }

   public void track(Entity var1, final int var2, int var3, boolean var4) {
      try {
         if (this.trackedEntityHashTable.containsItem(var1.getEntityId())) {
            throw new IllegalStateException("Entity is already tracked!");
         }

         EntityTrackerEntry var5 = new EntityTrackerEntry(var1, var2, this.maxTrackingDistanceThreshold, var3, var4);
         this.entries.add(var5);
         this.trackedEntityHashTable.addKey(var1.getEntityId(), var5);
         var5.updatePlayerEntities(this.world.playerEntities);
      } catch (Throwable var11) {
         CrashReport var6 = CrashReport.makeCrashReport(var11, "Adding entity to track");
         CrashReportCategory var7 = var6.makeCategory("Entity To Track");
         var7.addCrashSection("Tracking range", var2 + " blocks");
         var7.setDetail("Update interval", new ICrashReportDetail() {
            public String call() throws Exception {
               String var1 = "Once per " + var2 + " ticks";
               if (var2 == Integer.MAX_VALUE) {
                  var1 = "Maximum (" + var1 + ")";
               }

               return var1;
            }

            public Object call() throws Exception {
               return this.call();
            }
         });
         var1.addEntityCrashInfo(var7);
         ((EntityTrackerEntry)this.trackedEntityHashTable.lookup(var1.getEntityId())).getTrackedEntity().addEntityCrashInfo(var6.makeCategory("Entity That Is Already Tracked"));

         try {
            throw new ReportedException(var6);
         } catch (ReportedException var10) {
            LOGGER.error("\"Silently\" catching entity tracking error.", var10);
         }
      }

   }

   public void untrack(Entity var1) {
      if (var1 instanceof EntityPlayerMP) {
         EntityPlayerMP var2 = (EntityPlayerMP)var1;

         for(EntityTrackerEntry var4 : this.entries) {
            var4.removeFromTrackedPlayers(var2);
         }
      }

      EntityTrackerEntry var5 = (EntityTrackerEntry)this.trackedEntityHashTable.removeObject(var1.getEntityId());
      if (var5 != null) {
         this.entries.remove(var5);
         var5.sendDestroyEntityPacketToTrackedPlayers();
      }

   }

   public void tick() {
      ArrayList var1 = Lists.newArrayList();

      for(EntityTrackerEntry var3 : this.entries) {
         var3.updatePlayerList(this.world.playerEntities);
         if (var3.playerEntitiesUpdated) {
            Entity var4 = var3.getTrackedEntity();
            if (var4 instanceof EntityPlayerMP) {
               var1.add((EntityPlayerMP)var4);
            }
         }
      }

      for(int var7 = 0; var7 < var1.size(); ++var7) {
         EntityPlayerMP var8 = (EntityPlayerMP)var1.get(var7);

         for(EntityTrackerEntry var6 : this.entries) {
            if (var6.getTrackedEntity() != var8) {
               var6.updatePlayerEntity(var8);
            }
         }
      }

   }

   public void updateVisibility(EntityPlayerMP var1) {
      for(EntityTrackerEntry var3 : this.entries) {
         if (var3.getTrackedEntity() == var1) {
            var3.updatePlayerEntities(this.world.playerEntities);
         } else {
            var3.updatePlayerEntity(var1);
         }
      }

   }

   public void sendToTracking(Entity var1, Packet var2) {
      EntityTrackerEntry var3 = (EntityTrackerEntry)this.trackedEntityHashTable.lookup(var1.getEntityId());
      if (var3 != null) {
         var3.sendPacketToTrackedPlayers(var2);
      }

   }

   public void sendToTrackingAndSelf(Entity var1, Packet var2) {
      EntityTrackerEntry var3 = (EntityTrackerEntry)this.trackedEntityHashTable.lookup(var1.getEntityId());
      if (var3 != null) {
         var3.sendToTrackingAndSelf(var2);
      }

   }

   public void removePlayerFromTrackers(EntityPlayerMP var1) {
      for(EntityTrackerEntry var3 : this.entries) {
         var3.removeTrackedPlayerSymmetric(var1);
      }

   }

   public void sendLeashedEntitiesInChunk(EntityPlayerMP var1, Chunk var2) {
      ArrayList var3 = Lists.newArrayList();
      ArrayList var4 = Lists.newArrayList();

      for(EntityTrackerEntry var6 : this.entries) {
         Entity var7 = var6.getTrackedEntity();
         if (var7 != var1 && var7.chunkCoordX == var2.xPosition && var7.chunkCoordZ == var2.zPosition) {
            var6.updatePlayerEntity(var1);
            if (var7 instanceof EntityLiving && ((EntityLiving)var7).getLeashedToEntity() != null) {
               var3.add(var7);
            }

            if (!var7.getPassengers().isEmpty()) {
               var4.add(var7);
            }
         }
      }

      if (!var3.isEmpty()) {
         for(Entity var10 : var3) {
            var1.connection.sendPacket(new SPacketEntityAttach(var10, ((EntityLiving)var10).getLeashedToEntity()));
         }
      }

      if (!var4.isEmpty()) {
         for(Entity var11 : var4) {
            var1.connection.sendPacket(new SPacketSetPassengers(var11));
         }
      }

   }

   public void setViewDistance(int var1) {
      this.maxTrackingDistanceThreshold = (var1 - 1) * 16;

      for(EntityTrackerEntry var3 : this.entries) {
         var3.setMaxRange(this.maxTrackingDistanceThreshold);
      }

   }
}
