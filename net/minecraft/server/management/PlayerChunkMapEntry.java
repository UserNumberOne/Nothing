package net.minecraft.server.management;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketMultiBlockChange;
import net.minecraft.network.play.server.SPacketUnloadChunk;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.v1_10_R1.chunkio.ChunkIOExecutor;

public class PlayerChunkMapEntry {
   private static final Logger LOGGER = LogManager.getLogger();
   private final PlayerChunkMap playerChunkMap;
   public final List players = Lists.newArrayList();
   private final ChunkPos pos;
   private final short[] changedBlocks = new short[64];
   @Nullable
   public Chunk chunk;
   private int changes;
   private int changedSectionFilter;
   private long lastUpdateInhabitedTime;
   private boolean sentToPlayers;
   private boolean loadInProgress = false;
   private Runnable loadedRunnable = new Runnable() {
      public void run() {
         PlayerChunkMapEntry.this.loadInProgress = false;
         PlayerChunkMapEntry.this.chunk = PlayerChunkMapEntry.this.playerChunkMap.getWorldServer().getChunkProvider().loadChunk(PlayerChunkMapEntry.this.pos.chunkXPos, PlayerChunkMapEntry.this.pos.chunkZPos);
      }
   };

   public PlayerChunkMapEntry(PlayerChunkMap playerchunkmap, int i, int j) {
      this.playerChunkMap = playerchunkmap;
      this.pos = new ChunkPos(i, j);
      this.loadInProgress = true;
      this.chunk = playerchunkmap.getWorldServer().getChunkProvider().getChunkAt(i, j, this.loadedRunnable, false);
   }

   public ChunkPos getPos() {
      return this.pos;
   }

   public void addPlayer(EntityPlayerMP entityplayer) {
      if (this.players.contains(entityplayer)) {
         LOGGER.debug("Failed to add player. {} already is in chunk {}, {}", new Object[]{entityplayer, this.pos.chunkXPos, this.pos.chunkZPos});
      } else {
         if (this.players.isEmpty()) {
            this.lastUpdateInhabitedTime = this.playerChunkMap.getWorldServer().getTotalWorldTime();
         }

         this.players.add(entityplayer);
         if (this.sentToPlayers) {
            this.sendNearbySpecialEntities(entityplayer);
         }
      }

   }

   public void removePlayer(EntityPlayerMP entityplayer) {
      if (this.players.contains(entityplayer)) {
         if (!this.sentToPlayers) {
            this.players.remove(entityplayer);
            if (this.players.isEmpty()) {
               ChunkIOExecutor.dropQueuedChunkLoad(this.playerChunkMap.getWorldServer(), this.pos.chunkXPos, this.pos.chunkZPos, this.loadedRunnable);
               this.playerChunkMap.removeEntry(this);
            }

            return;
         }

         if (this.sentToPlayers) {
            entityplayer.connection.sendPacket(new SPacketUnloadChunk(this.pos.chunkXPos, this.pos.chunkZPos));
         }

         this.players.remove(entityplayer);
         if (this.players.isEmpty()) {
            this.playerChunkMap.removeEntry(this);
         }
      }

   }

   public boolean providePlayerChunk(boolean flag) {
      if (this.chunk != null) {
         return true;
      } else {
         if (!this.loadInProgress) {
            this.loadInProgress = true;
            this.chunk = this.playerChunkMap.getWorldServer().getChunkProvider().getChunkAt(this.pos.chunkXPos, this.pos.chunkZPos, this.loadedRunnable, flag);
         }

         return this.chunk != null;
      }
   }

   public boolean sendToPlayers() {
      if (this.sentToPlayers) {
         return true;
      } else if (this.chunk == null) {
         return false;
      } else if (!this.chunk.isPopulated()) {
         return false;
      } else {
         this.changes = 0;
         this.changedSectionFilter = 0;
         this.sentToPlayers = true;
         SPacketChunkData packetplayoutmapchunk = new SPacketChunkData(this.chunk, 65535);

         for(EntityPlayerMP entityplayer : this.players) {
            entityplayer.connection.sendPacket(packetplayoutmapchunk);
            this.playerChunkMap.getWorldServer().getEntityTracker().sendLeashedEntitiesInChunk(entityplayer, this.chunk);
         }

         return true;
      }
   }

   public void sendNearbySpecialEntities(EntityPlayerMP entityplayer) {
      if (this.sentToPlayers) {
         entityplayer.connection.sendPacket(new SPacketChunkData(this.chunk, 65535));
         this.playerChunkMap.getWorldServer().getEntityTracker().sendLeashedEntitiesInChunk(entityplayer, this.chunk);
      }

   }

   public void updateChunkInhabitedTime() {
      long i = this.playerChunkMap.getWorldServer().getTotalWorldTime();
      if (this.chunk != null) {
         this.chunk.setInhabitedTime(this.chunk.getInhabitedTime() + i - this.lastUpdateInhabitedTime);
      }

      this.lastUpdateInhabitedTime = i;
   }

   public void blockChanged(int i, int j, int k) {
      if (this.sentToPlayers) {
         if (this.changes == 0) {
            this.playerChunkMap.entryChanged(this);
         }

         this.changedSectionFilter |= 1 << (j >> 4);
         if (this.changes < 64) {
            short short0 = (short)(i << 12 | k << 8 | j);

            for(int l = 0; l < this.changes; ++l) {
               if (this.changedBlocks[l] == short0) {
                  return;
               }
            }

            this.changedBlocks[this.changes++] = short0;
         }
      }

   }

   public void sendPacket(Packet packet) {
      if (this.sentToPlayers) {
         for(int i = 0; i < this.players.size(); ++i) {
            ((EntityPlayerMP)this.players.get(i)).connection.sendPacket(packet);
         }
      }

   }

   public void update() {
      if (this.sentToPlayers && this.chunk != null && this.changes != 0) {
         if (this.changes == 1) {
            int i = (this.changedBlocks[0] >> 12 & 15) + this.pos.chunkXPos * 16;
            int j = this.changedBlocks[0] & 255;
            int k = (this.changedBlocks[0] >> 8 & 15) + this.pos.chunkZPos * 16;
            BlockPos blockposition = new BlockPos(i, j, k);
            this.sendPacket(new SPacketBlockChange(this.playerChunkMap.getWorldServer(), blockposition));
            if (this.playerChunkMap.getWorldServer().getBlockState(blockposition).getBlock().hasTileEntity()) {
               this.sendBlockEntity(this.playerChunkMap.getWorldServer().getTileEntity(blockposition));
            }
         } else if (this.changes == 64) {
            this.sendPacket(new SPacketChunkData(this.chunk, this.changedSectionFilter));
         } else {
            this.sendPacket(new SPacketMultiBlockChange(this.changes, this.changedBlocks, this.chunk));

            for(int i = 0; i < this.changes; ++i) {
               int j = (this.changedBlocks[i] >> 12 & 15) + this.pos.chunkXPos * 16;
               int k = this.changedBlocks[i] & 255;
               int l = (this.changedBlocks[i] >> 8 & 15) + this.pos.chunkZPos * 16;
               BlockPos blockposition1 = new BlockPos(j, k, l);
               if (this.playerChunkMap.getWorldServer().getBlockState(blockposition1).getBlock().hasTileEntity()) {
                  this.sendBlockEntity(this.playerChunkMap.getWorldServer().getTileEntity(blockposition1));
               }
            }
         }

         this.changes = 0;
         this.changedSectionFilter = 0;
      }

   }

   private void sendBlockEntity(@Nullable TileEntity tileentity) {
      if (tileentity != null) {
         SPacketUpdateTileEntity packetplayouttileentitydata = tileentity.getUpdatePacket();
         if (packetplayouttileentitydata != null) {
            this.sendPacket(packetplayouttileentitydata);
         }
      }

   }

   public boolean containsPlayer(EntityPlayerMP entityplayer) {
      return this.players.contains(entityplayer);
   }

   public boolean hasPlayerMatching(Predicate predicate) {
      return Iterables.tryFind(this.players, predicate).isPresent();
   }

   public boolean hasPlayerMatchingInRange(double d0, Predicate predicate) {
      int i = 0;

      for(int j = this.players.size(); i < j; ++i) {
         EntityPlayerMP entityplayer = (EntityPlayerMP)this.players.get(i);
         if (predicate.apply(entityplayer) && this.pos.getDistanceSq(entityplayer) < d0 * d0) {
            return true;
         }
      }

      return false;
   }

   public boolean isSentToPlayers() {
      return this.sentToPlayers;
   }

   @Nullable
   public Chunk getChunk() {
      return this.chunk;
   }

   public double getClosestPlayerDistance() {
      double d0 = Double.MAX_VALUE;

      for(EntityPlayerMP entityplayer : this.players) {
         double d1 = this.pos.getDistanceSq(entityplayer);
         if (d1 < d0) {
            d0 = d1;
         }
      }

      return d0;
   }
}
