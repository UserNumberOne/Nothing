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

   public PlayerChunkMapEntry(PlayerChunkMap var1, int var2, int var3) {
      this.playerChunkMap = var1;
      this.pos = new ChunkPos(var2, var3);
      this.loadInProgress = true;
      this.chunk = var1.getWorldServer().getChunkProvider().getChunkAt(var2, var3, this.loadedRunnable, false);
   }

   public ChunkPos getPos() {
      return this.pos;
   }

   public void addPlayer(EntityPlayerMP var1) {
      if (this.players.contains(var1)) {
         LOGGER.debug("Failed to add player. {} already is in chunk {}, {}", new Object[]{var1, this.pos.chunkXPos, this.pos.chunkZPos});
      } else {
         if (this.players.isEmpty()) {
            this.lastUpdateInhabitedTime = this.playerChunkMap.getWorldServer().getTotalWorldTime();
         }

         this.players.add(var1);
         if (this.sentToPlayers) {
            this.sendNearbySpecialEntities(var1);
         }
      }

   }

   public void removePlayer(EntityPlayerMP var1) {
      if (this.players.contains(var1)) {
         if (!this.sentToPlayers) {
            this.players.remove(var1);
            if (this.players.isEmpty()) {
               ChunkIOExecutor.dropQueuedChunkLoad(this.playerChunkMap.getWorldServer(), this.pos.chunkXPos, this.pos.chunkZPos, this.loadedRunnable);
               this.playerChunkMap.removeEntry(this);
            }

            return;
         }

         if (this.sentToPlayers) {
            var1.connection.sendPacket(new SPacketUnloadChunk(this.pos.chunkXPos, this.pos.chunkZPos));
         }

         this.players.remove(var1);
         if (this.players.isEmpty()) {
            this.playerChunkMap.removeEntry(this);
         }
      }

   }

   public boolean providePlayerChunk(boolean var1) {
      if (this.chunk != null) {
         return true;
      } else {
         if (!this.loadInProgress) {
            this.loadInProgress = true;
            this.chunk = this.playerChunkMap.getWorldServer().getChunkProvider().getChunkAt(this.pos.chunkXPos, this.pos.chunkZPos, this.loadedRunnable, var1);
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
         SPacketChunkData var1 = new SPacketChunkData(this.chunk, 65535);

         for(EntityPlayerMP var3 : this.players) {
            var3.connection.sendPacket(var1);
            this.playerChunkMap.getWorldServer().getEntityTracker().sendLeashedEntitiesInChunk(var3, this.chunk);
         }

         return true;
      }
   }

   public void sendNearbySpecialEntities(EntityPlayerMP var1) {
      if (this.sentToPlayers) {
         var1.connection.sendPacket(new SPacketChunkData(this.chunk, 65535));
         this.playerChunkMap.getWorldServer().getEntityTracker().sendLeashedEntitiesInChunk(var1, this.chunk);
      }

   }

   public void updateChunkInhabitedTime() {
      long var1 = this.playerChunkMap.getWorldServer().getTotalWorldTime();
      if (this.chunk != null) {
         this.chunk.setInhabitedTime(this.chunk.getInhabitedTime() + var1 - this.lastUpdateInhabitedTime);
      }

      this.lastUpdateInhabitedTime = var1;
   }

   public void blockChanged(int var1, int var2, int var3) {
      if (this.sentToPlayers) {
         if (this.changes == 0) {
            this.playerChunkMap.entryChanged(this);
         }

         this.changedSectionFilter |= 1 << (var2 >> 4);
         if (this.changes < 64) {
            short var4 = (short)(var1 << 12 | var3 << 8 | var2);

            for(int var5 = 0; var5 < this.changes; ++var5) {
               if (this.changedBlocks[var5] == var4) {
                  return;
               }
            }

            this.changedBlocks[this.changes++] = var4;
         }
      }

   }

   public void sendPacket(Packet var1) {
      if (this.sentToPlayers) {
         for(int var2 = 0; var2 < this.players.size(); ++var2) {
            ((EntityPlayerMP)this.players.get(var2)).connection.sendPacket(var1);
         }
      }

   }

   public void update() {
      if (this.sentToPlayers && this.chunk != null && this.changes != 0) {
         if (this.changes == 1) {
            int var6 = (this.changedBlocks[0] >> 12 & 15) + this.pos.chunkXPos * 16;
            int var7 = this.changedBlocks[0] & 255;
            int var8 = (this.changedBlocks[0] >> 8 & 15) + this.pos.chunkZPos * 16;
            BlockPos var9 = new BlockPos(var6, var7, var8);
            this.sendPacket(new SPacketBlockChange(this.playerChunkMap.getWorldServer(), var9));
            if (this.playerChunkMap.getWorldServer().getBlockState(var9).getBlock().hasTileEntity()) {
               this.sendBlockEntity(this.playerChunkMap.getWorldServer().getTileEntity(var9));
            }
         } else if (this.changes == 64) {
            this.sendPacket(new SPacketChunkData(this.chunk, this.changedSectionFilter));
         } else {
            this.sendPacket(new SPacketMultiBlockChange(this.changes, this.changedBlocks, this.chunk));

            for(int var1 = 0; var1 < this.changes; ++var1) {
               int var2 = (this.changedBlocks[var1] >> 12 & 15) + this.pos.chunkXPos * 16;
               int var3 = this.changedBlocks[var1] & 255;
               int var4 = (this.changedBlocks[var1] >> 8 & 15) + this.pos.chunkZPos * 16;
               BlockPos var5 = new BlockPos(var2, var3, var4);
               if (this.playerChunkMap.getWorldServer().getBlockState(var5).getBlock().hasTileEntity()) {
                  this.sendBlockEntity(this.playerChunkMap.getWorldServer().getTileEntity(var5));
               }
            }
         }

         this.changes = 0;
         this.changedSectionFilter = 0;
      }

   }

   private void sendBlockEntity(@Nullable TileEntity var1) {
      if (var1 != null) {
         SPacketUpdateTileEntity var2 = var1.getUpdatePacket();
         if (var2 != null) {
            this.sendPacket(var2);
         }
      }

   }

   public boolean containsPlayer(EntityPlayerMP var1) {
      return this.players.contains(var1);
   }

   public boolean hasPlayerMatching(Predicate var1) {
      return Iterables.tryFind(this.players, var1).isPresent();
   }

   public boolean hasPlayerMatchingInRange(double var1, Predicate var3) {
      int var4 = 0;

      for(int var5 = this.players.size(); var4 < var5; ++var4) {
         EntityPlayerMP var6 = (EntityPlayerMP)this.players.get(var4);
         if (var3.apply(var6) && this.pos.getDistanceSq(var6) < var1 * var1) {
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
      double var1 = Double.MAX_VALUE;

      for(EntityPlayerMP var4 : this.players) {
         double var5 = this.pos.getDistanceSq(var4);
         if (var5 < var1) {
            var1 = var5;
         }
      }

      return var1;
   }
}
