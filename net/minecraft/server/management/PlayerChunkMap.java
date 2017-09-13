package net.minecraft.server.management;

import com.google.common.base.Predicate;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

public class PlayerChunkMap {
   private static final Predicate NOT_SPECTATOR = new Predicate() {
      public boolean apply(@Nullable EntityPlayerMP entityplayer) {
         return entityplayer != null && !entityplayer.isSpectator();
      }

      public boolean apply(Object object) {
         return this.apply((EntityPlayerMP)object);
      }
   };
   private static final Predicate CAN_GENERATE_CHUNKS = new Predicate() {
      public boolean apply(@Nullable EntityPlayerMP entityplayer) {
         return entityplayer != null && (!entityplayer.isSpectator() || entityplayer.getServerWorld().getGameRules().getBoolean("spectatorsGenerateChunks"));
      }

      public boolean apply(Object object) {
         return this.apply((EntityPlayerMP)object);
      }
   };
   private final WorldServer world;
   private final List players = Lists.newArrayList();
   private final Long2ObjectMap entryMap = new Long2ObjectOpenHashMap(4096);
   private final Set dirtyEntries = Sets.newHashSet();
   private final List pendingSendToPlayers = Lists.newLinkedList();
   private final List entriesWithoutChunks = Lists.newLinkedList();
   private final List entries = Lists.newArrayList();
   private int playerViewRadius;
   private long previousTotalWorldTime;
   private boolean sortMissingChunks = true;
   private boolean sortSendToPlayers = true;
   private boolean wasNotEmpty;

   public PlayerChunkMap(WorldServer worldserver) {
      this.world = worldserver;
      this.setPlayerViewRadius(worldserver.getMinecraftServer().getPlayerList().getViewDistance());
   }

   public WorldServer getWorldServer() {
      return this.world;
   }

   public Iterator getChunkIterator() {
      final Iterator iterator = this.entries.iterator();
      return new AbstractIterator() {
         protected Chunk computeNext() {
            while(true) {
               if (iterator.hasNext()) {
                  PlayerChunkMapEntry playerchunk = (PlayerChunkMapEntry)iterator.next();
                  Chunk chunk = playerchunk.getChunk();
                  if (chunk == null) {
                     continue;
                  }

                  if (!chunk.isLightPopulated() && chunk.isTerrainPopulated()) {
                     return chunk;
                  }

                  if (!chunk.isChunkTicked()) {
                     return chunk;
                  }

                  if (!playerchunk.hasPlayerMatchingInRange(128.0D, PlayerChunkMap.NOT_SPECTATOR)) {
                     continue;
                  }

                  return chunk;
               }

               return (Chunk)this.endOfData();
            }
         }

         protected Object computeNext() {
            return this.computeNext();
         }
      };
   }

   public void tick() {
      long i = this.world.getTotalWorldTime();
      if (i - this.previousTotalWorldTime > 8000L) {
         this.previousTotalWorldTime = i;

         for(int j = 0; j < this.entries.size(); ++j) {
            PlayerChunkMapEntry playerchunk = (PlayerChunkMapEntry)this.entries.get(j);
            playerchunk.update();
            playerchunk.updateChunkInhabitedTime();
         }
      }

      if (!this.dirtyEntries.isEmpty()) {
         for(PlayerChunkMapEntry playerchunk : this.dirtyEntries) {
            playerchunk.update();
         }

         this.dirtyEntries.clear();
      }

      if (this.sortMissingChunks && i % 4L == 0L) {
         this.sortMissingChunks = false;
         Collections.sort(this.entriesWithoutChunks, new Comparator() {
            public int compare(PlayerChunkMapEntry playerchunk, PlayerChunkMapEntry playerchunk1) {
               return ComparisonChain.start().compare(playerchunk.getClosestPlayerDistance(), playerchunk1.getClosestPlayerDistance()).result();
            }

            public int compare(Object object, Object object1) {
               return this.compare((PlayerChunkMapEntry)object, (PlayerChunkMapEntry)object1);
            }
         });
      }

      if (this.sortSendToPlayers && i % 4L == 2L) {
         this.sortSendToPlayers = false;
         Collections.sort(this.pendingSendToPlayers, new Comparator() {
            public int compare(PlayerChunkMapEntry playerchunk, PlayerChunkMapEntry playerchunk1) {
               return ComparisonChain.start().compare(playerchunk.getClosestPlayerDistance(), playerchunk1.getClosestPlayerDistance()).result();
            }

            public int compare(Object object, Object object1) {
               return this.compare((PlayerChunkMapEntry)object, (PlayerChunkMapEntry)object1);
            }
         });
      }

      if (!this.entriesWithoutChunks.isEmpty()) {
         long k = System.nanoTime() + 50000000L;
         int l = 49;
         Iterator iterator1 = this.entriesWithoutChunks.iterator();

         while(iterator1.hasNext()) {
            PlayerChunkMapEntry playerchunk1 = (PlayerChunkMapEntry)iterator1.next();
            if (playerchunk1.getChunk() == null) {
               boolean flag = playerchunk1.hasPlayerMatching(CAN_GENERATE_CHUNKS);
               if (playerchunk1.providePlayerChunk(flag)) {
                  iterator1.remove();
                  if (playerchunk1.sendToPlayers()) {
                     this.pendingSendToPlayers.remove(playerchunk1);
                  }

                  --l;
                  if (l < 0 || System.nanoTime() > k) {
                     break;
                  }
               }
            }
         }
      }

      if (!this.pendingSendToPlayers.isEmpty()) {
         int j = 81;
         Iterator iterator2 = this.pendingSendToPlayers.iterator();

         while(iterator2.hasNext()) {
            PlayerChunkMapEntry playerchunk2 = (PlayerChunkMapEntry)iterator2.next();
            if (playerchunk2.sendToPlayers()) {
               iterator2.remove();
               --j;
               if (j < 0) {
                  break;
               }
            }
         }
      }

      if (this.players.isEmpty()) {
         WorldProvider worldprovider = this.world.provider;
         if (!worldprovider.canRespawnHere()) {
            this.world.getChunkProvider().unloadAllChunks();
         }
      }

   }

   public boolean contains(int i, int j) {
      long k = getIndex(i, j);
      return this.entryMap.get(k) != null;
   }

   @Nullable
   public PlayerChunkMapEntry getEntry(int i, int j) {
      return (PlayerChunkMapEntry)this.entryMap.get(getIndex(i, j));
   }

   private PlayerChunkMapEntry getOrCreateEntry(int i, int j) {
      long k = getIndex(i, j);
      PlayerChunkMapEntry playerchunk = (PlayerChunkMapEntry)this.entryMap.get(k);
      if (playerchunk == null) {
         playerchunk = new PlayerChunkMapEntry(this, i, j);
         this.entryMap.put(k, playerchunk);
         this.entries.add(playerchunk);
         if (playerchunk.getChunk() == null) {
            this.entriesWithoutChunks.add(playerchunk);
         }

         if (!playerchunk.sendToPlayers()) {
            this.pendingSendToPlayers.add(playerchunk);
         }
      }

      return playerchunk;
   }

   public final boolean isChunkInUse(int x, int z) {
      PlayerChunkMapEntry pi = this.getEntry(x, z);
      if (pi != null) {
         return pi.players.size() > 0;
      } else {
         return false;
      }
   }

   public void markBlockForUpdate(BlockPos blockposition) {
      int i = blockposition.getX() >> 4;
      int j = blockposition.getZ() >> 4;
      PlayerChunkMapEntry playerchunk = this.getEntry(i, j);
      if (playerchunk != null) {
         playerchunk.blockChanged(blockposition.getX() & 15, blockposition.getY(), blockposition.getZ() & 15);
      }

   }

   public void addPlayer(EntityPlayerMP entityplayer) {
      int i = (int)entityplayer.posX >> 4;
      int j = (int)entityplayer.posZ >> 4;
      entityplayer.managedPosX = entityplayer.posX;
      entityplayer.managedPosZ = entityplayer.posZ;
      List chunkList = new LinkedList();

      for(int k = i - this.playerViewRadius; k <= i + this.playerViewRadius; ++k) {
         for(int l = j - this.playerViewRadius; l <= j + this.playerViewRadius; ++l) {
            chunkList.add(new ChunkPos(k, l));
         }
      }

      Collections.sort(chunkList, new PlayerChunkMap.ChunkCoordComparator(entityplayer));

      for(ChunkPos pair : chunkList) {
         this.getOrCreateEntry(pair.chunkXPos, pair.chunkZPos).addPlayer(entityplayer);
      }

      this.players.add(entityplayer);
      this.markSortPending();
   }

   public void removePlayer(EntityPlayerMP entityplayer) {
      int i = (int)entityplayer.managedPosX >> 4;
      int j = (int)entityplayer.managedPosZ >> 4;

      for(int k = i - this.playerViewRadius; k <= i + this.playerViewRadius; ++k) {
         for(int l = j - this.playerViewRadius; l <= j + this.playerViewRadius; ++l) {
            PlayerChunkMapEntry playerchunk = this.getEntry(k, l);
            if (playerchunk != null) {
               playerchunk.removePlayer(entityplayer);
            }
         }
      }

      this.players.remove(entityplayer);
      this.markSortPending();
   }

   private boolean overlaps(int i, int j, int k, int l, int i1) {
      int j1 = i - k;
      int k1 = j - l;
      return j1 >= -i1 && j1 <= i1 ? k1 >= -i1 && k1 <= i1 : false;
   }

   public void updateMovingPlayer(EntityPlayerMP entityplayer) {
      int i = (int)entityplayer.posX >> 4;
      int j = (int)entityplayer.posZ >> 4;
      double d0 = entityplayer.managedPosX - entityplayer.posX;
      double d1 = entityplayer.managedPosZ - entityplayer.posZ;
      double d2 = d0 * d0 + d1 * d1;
      if (d2 >= 64.0D) {
         int k = (int)entityplayer.managedPosX >> 4;
         int l = (int)entityplayer.managedPosZ >> 4;
         int i1 = this.playerViewRadius;
         int j1 = i - k;
         int k1 = j - l;
         List chunksToLoad = new LinkedList();
         if (j1 != 0 || k1 != 0) {
            for(int l1 = i - i1; l1 <= i + i1; ++l1) {
               for(int i2 = j - i1; i2 <= j + i1; ++i2) {
                  if (!this.overlaps(l1, i2, k, l, i1)) {
                     chunksToLoad.add(new ChunkPos(l1, i2));
                  }

                  if (!this.overlaps(l1 - j1, i2 - k1, i, j, i1)) {
                     PlayerChunkMapEntry playerchunk = this.getEntry(l1 - j1, i2 - k1);
                     if (playerchunk != null) {
                        playerchunk.removePlayer(entityplayer);
                     }
                  }
               }
            }

            entityplayer.managedPosX = entityplayer.posX;
            entityplayer.managedPosZ = entityplayer.posZ;
            this.markSortPending();
            Collections.sort(chunksToLoad, new PlayerChunkMap.ChunkCoordComparator(entityplayer));

            for(ChunkPos pair : chunksToLoad) {
               this.getOrCreateEntry(pair.chunkXPos, pair.chunkZPos).addPlayer(entityplayer);
            }
         }
      }

   }

   public boolean isPlayerWatchingChunk(EntityPlayerMP entityplayer, int i, int j) {
      PlayerChunkMapEntry playerchunk = this.getEntry(i, j);
      return playerchunk != null && playerchunk.containsPlayer(entityplayer) && playerchunk.isSentToPlayers();
   }

   public void setPlayerViewRadius(int i) {
      i = MathHelper.clamp(i, 3, 32);
      if (i != this.playerViewRadius) {
         int j = i - this.playerViewRadius;

         for(EntityPlayerMP entityplayer : Lists.newArrayList(this.players)) {
            int k = (int)entityplayer.posX >> 4;
            int l = (int)entityplayer.posZ >> 4;
            if (j > 0) {
               for(int i1 = k - i; i1 <= k + i; ++i1) {
                  for(int j1 = l - i; j1 <= l + i; ++j1) {
                     PlayerChunkMapEntry playerchunk = this.getOrCreateEntry(i1, j1);
                     if (!playerchunk.containsPlayer(entityplayer)) {
                        playerchunk.addPlayer(entityplayer);
                     }
                  }
               }
            } else {
               for(int i1 = k - this.playerViewRadius; i1 <= k + this.playerViewRadius; ++i1) {
                  for(int j1 = l - this.playerViewRadius; j1 <= l + this.playerViewRadius; ++j1) {
                     if (!this.overlaps(i1, j1, k, l, i)) {
                        this.getOrCreateEntry(i1, j1).removePlayer(entityplayer);
                     }
                  }
               }
            }
         }

         this.playerViewRadius = i;
         this.markSortPending();
      }

   }

   private void markSortPending() {
      this.sortMissingChunks = true;
      this.sortSendToPlayers = true;
   }

   public static int getFurthestViewableBlock(int i) {
      return i * 16 - 16;
   }

   private static long getIndex(int i, int j) {
      return (long)i + 2147483647L | (long)j + 2147483647L << 32;
   }

   public void entryChanged(PlayerChunkMapEntry playerchunk) {
      this.dirtyEntries.add(playerchunk);
   }

   public void removeEntry(PlayerChunkMapEntry playerchunk) {
      ChunkPos chunkcoordintpair = playerchunk.getPos();
      long i = getIndex(chunkcoordintpair.chunkXPos, chunkcoordintpair.chunkZPos);
      playerchunk.updateChunkInhabitedTime();
      this.entryMap.remove(i);
      this.entries.remove(playerchunk);
      this.dirtyEntries.remove(playerchunk);
      this.pendingSendToPlayers.remove(playerchunk);
      this.entriesWithoutChunks.remove(playerchunk);
      Chunk chunk = playerchunk.getChunk();
      if (chunk != null) {
         this.getWorldServer().getChunkProvider().unload(chunk);
      }

   }

   private static class ChunkCoordComparator implements Comparator {
      private int x;
      private int z;

      public ChunkCoordComparator(EntityPlayerMP entityplayer) {
         this.x = (int)entityplayer.posX >> 4;
         this.z = (int)entityplayer.posZ >> 4;
      }

      public int compare(ChunkPos a, ChunkPos b) {
         if (a.equals(b)) {
            return 0;
         } else {
            int ax = a.chunkXPos - this.x;
            int az = a.chunkZPos - this.z;
            int bx = b.chunkXPos - this.x;
            int bz = b.chunkZPos - this.z;
            int result = (ax - bx) * (ax + bx) + (az - bz) * (az + bz);
            if (result != 0) {
               return result;
            } else if (ax < 0) {
               return bx < 0 ? bz - az : -1;
            } else {
               return bx < 0 ? 1 : az - bz;
            }
         }
      }
   }
}
