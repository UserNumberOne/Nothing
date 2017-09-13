package net.minecraft.server.management;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
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
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.chunkio.ChunkIOExecutor;
import net.minecraftforge.event.world.ChunkWatchEvent.UnWatch;
import net.minecraftforge.event.world.ChunkWatchEvent.Watch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlayerChunkMapEntry {
   private static final Logger LOGGER = LogManager.getLogger();
   private final PlayerChunkMap playerChunkMap;
   private final List players = Lists.newArrayList();
   private final ChunkPos pos;
   private short[] changedBlocks = new short[64];
   @Nullable
   private Chunk chunk;
   private int changes;
   private int changedSectionFilter;
   private long lastUpdateInhabitedTime;
   private boolean sentToPlayers;
   private Runnable loadedRunnable = new Runnable() {
      public void run() {
         PlayerChunkMapEntry.this.chunk = PlayerChunkMapEntry.this.playerChunkMap.getWorldServer().getChunkProvider().loadChunk(PlayerChunkMapEntry.this.pos.chunkXPos, PlayerChunkMapEntry.this.pos.chunkZPos);
         PlayerChunkMapEntry.this.loading = false;
      }
   };
   private boolean loading = true;

   public PlayerChunkMapEntry(PlayerChunkMap mapIn, int chunkX, int chunkZ) {
      this.playerChunkMap = mapIn;
      this.pos = new ChunkPos(chunkX, chunkZ);
      mapIn.getWorldServer().getChunkProvider().loadChunk(chunkX, chunkZ, this.loadedRunnable);
   }

   public ChunkPos getPos() {
      return this.pos;
   }

   public void addPlayer(EntityPlayerMP player) {
      if (this.players.contains(player)) {
         LOGGER.debug("Failed to add player. {} already is in chunk {}, {}", new Object[]{player, this.pos.chunkXPos, this.pos.chunkZPos});
      } else {
         if (this.players.isEmpty()) {
            this.lastUpdateInhabitedTime = this.playerChunkMap.getWorldServer().getTotalWorldTime();
         }

         this.players.add(player);
         if (this.sentToPlayers) {
            this.sendNearbySpecialEntities(player);
            MinecraftForge.EVENT_BUS.post(new Watch(this.pos, player));
         }
      }

   }

   public void removePlayer(EntityPlayerMP player) {
      if (this.players.contains(player)) {
         if (this.chunk == null) {
            this.players.remove(player);
            if (this.players.isEmpty()) {
               if (this.loading) {
                  ChunkIOExecutor.dropQueuedChunkLoad(this.playerChunkMap.getWorldServer(), this.pos.chunkXPos, this.pos.chunkZPos, this.loadedRunnable);
               }

               this.playerChunkMap.removeEntry(this);
            }

            return;
         }

         if (this.sentToPlayers) {
            player.connection.sendPacket(new SPacketUnloadChunk(this.pos.chunkXPos, this.pos.chunkZPos));
         }

         this.players.remove(player);
         MinecraftForge.EVENT_BUS.post(new UnWatch(this.pos, player));
         if (this.players.isEmpty()) {
            this.playerChunkMap.removeEntry(this);
         }
      }

   }

   public boolean providePlayerChunk(boolean canGenerate) {
      if (this.loading) {
         return false;
      } else if (this.chunk != null) {
         return true;
      } else {
         if (canGenerate) {
            this.chunk = this.playerChunkMap.getWorldServer().getChunkProvider().provideChunk(this.pos.chunkXPos, this.pos.chunkZPos);
         } else {
            this.chunk = this.playerChunkMap.getWorldServer().getChunkProvider().loadChunk(this.pos.chunkXPos, this.pos.chunkZPos);
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
         Packet packet = new SPacketChunkData(this.chunk, 65535);

         for(EntityPlayerMP entityplayermp : this.players) {
            entityplayermp.connection.sendPacket(packet);
            this.playerChunkMap.getWorldServer().getEntityTracker().sendLeashedEntitiesInChunk(entityplayermp, this.chunk);
            MinecraftForge.EVENT_BUS.post(new Watch(this.pos, entityplayermp));
         }

         return true;
      }
   }

   public void sendNearbySpecialEntities(EntityPlayerMP player) {
      if (this.sentToPlayers) {
         player.connection.sendPacket(new SPacketChunkData(this.chunk, 65535));
         this.playerChunkMap.getWorldServer().getEntityTracker().sendLeashedEntitiesInChunk(player, this.chunk);
      }

   }

   public void updateChunkInhabitedTime() {
      long i = this.playerChunkMap.getWorldServer().getTotalWorldTime();
      if (this.chunk != null) {
         this.chunk.setInhabitedTime(this.chunk.getInhabitedTime() + i - this.lastUpdateInhabitedTime);
      }

      this.lastUpdateInhabitedTime = i;
   }

   public void blockChanged(int x, int y, int z) {
      if (this.sentToPlayers) {
         if (this.changes == 0) {
            this.playerChunkMap.entryChanged(this);
         }

         this.changedSectionFilter |= 1 << (y >> 4);
         short short1 = (short)(x << 12 | z << 8 | y);

         for(int i = 0; i < this.changes; ++i) {
            if (this.changedBlocks[i] == short1) {
               return;
            }
         }

         if (this.changes == this.changedBlocks.length) {
            this.changedBlocks = Arrays.copyOf(this.changedBlocks, this.changedBlocks.length << 1);
         }

         this.changedBlocks[this.changes++] = short1;
      }

   }

   public void sendPacket(Packet packetIn) {
      if (this.sentToPlayers) {
         for(int i = 0; i < this.players.size(); ++i) {
            ((EntityPlayerMP)this.players.get(i)).connection.sendPacket(packetIn);
         }
      }

   }

   public void update() {
      if (this.sentToPlayers && this.chunk != null && this.changes != 0) {
         if (this.changes == 1) {
            int i = (this.changedBlocks[0] >> 12 & 15) + this.pos.chunkXPos * 16;
            int j = this.changedBlocks[0] & 255;
            int k = (this.changedBlocks[0] >> 8 & 15) + this.pos.chunkZPos * 16;
            BlockPos blockpos = new BlockPos(i, j, k);
            this.sendPacket(new SPacketBlockChange(this.playerChunkMap.getWorldServer(), blockpos));
            IBlockState state = this.playerChunkMap.getWorldServer().getBlockState(blockpos);
            if (state.getBlock().hasTileEntity(state)) {
               this.sendBlockEntity(this.playerChunkMap.getWorldServer().getTileEntity(blockpos));
            }
         } else if (this.changes >= ForgeModContainer.clumpingThreshold) {
            this.sendPacket(new SPacketChunkData(this.chunk, this.changedSectionFilter));
         } else {
            this.sendPacket(new SPacketMultiBlockChange(this.changes, this.changedBlocks, this.chunk));

            for(int l = 0; l < this.changes; ++l) {
               int i1 = (this.changedBlocks[l] >> 12 & 15) + this.pos.chunkXPos * 16;
               int j1 = this.changedBlocks[l] & 255;
               int k1 = (this.changedBlocks[l] >> 8 & 15) + this.pos.chunkZPos * 16;
               BlockPos blockpos1 = new BlockPos(i1, j1, k1);
               IBlockState state = this.playerChunkMap.getWorldServer().getBlockState(blockpos1);
               if (state.getBlock().hasTileEntity(state)) {
                  this.sendBlockEntity(this.playerChunkMap.getWorldServer().getTileEntity(blockpos1));
               }
            }
         }

         this.changes = 0;
         this.changedSectionFilter = 0;
      }

   }

   private void sendBlockEntity(@Nullable TileEntity be) {
      if (be != null) {
         SPacketUpdateTileEntity spacketupdatetileentity = be.getUpdatePacket();
         if (spacketupdatetileentity != null) {
            this.sendPacket(spacketupdatetileentity);
         }
      }

   }

   public boolean containsPlayer(EntityPlayerMP player) {
      return this.players.contains(player);
   }

   public boolean hasPlayerMatching(Predicate predicate) {
      return Iterables.tryFind(this.players, predicate).isPresent();
   }

   public boolean hasPlayerMatchingInRange(double range, Predicate predicate) {
      int i = 0;

      for(int j = this.players.size(); i < j; ++i) {
         EntityPlayerMP entityplayermp = (EntityPlayerMP)this.players.get(i);
         if (predicate.apply(entityplayermp) && this.pos.getDistanceSq(entityplayermp) < range * range) {
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

      for(EntityPlayerMP entityplayermp : this.players) {
         double d1 = this.pos.getDistanceSq(entityplayermp);
         if (d1 < d0) {
            d0 = d1;
         }
      }

      return d0;
   }
}
