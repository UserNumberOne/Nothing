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
      public boolean apply(@Nullable EntityPlayerMP var1) {
         return var1 != null && !var1.isSpectator();
      }

      public boolean apply(Object var1) {
         return this.apply((EntityPlayerMP)var1);
      }
   };
   private static final Predicate CAN_GENERATE_CHUNKS = new Predicate() {
      public boolean apply(@Nullable EntityPlayerMP var1) {
         return var1 != null && (!var1.isSpectator() || var1.getServerWorld().getGameRules().getBoolean("spectatorsGenerateChunks"));
      }

      public boolean apply(Object var1) {
         return this.apply((EntityPlayerMP)var1);
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

   public PlayerChunkMap(WorldServer var1) {
      this.world = var1;
      this.setPlayerViewRadius(var1.getMinecraftServer().getPlayerList().getViewDistance());
   }

   public WorldServer getWorldServer() {
      return this.world;
   }

   public Iterator getChunkIterator() {
      final Iterator var1 = this.entries.iterator();
      return new AbstractIterator() {
         protected Chunk computeNext() {
            while(true) {
               if (var1.hasNext()) {
                  PlayerChunkMapEntry var1x = (PlayerChunkMapEntry)var1.next();
                  Chunk var2 = var1x.getChunk();
                  if (var2 == null) {
                     continue;
                  }

                  if (!var2.isLightPopulated() && var2.isTerrainPopulated()) {
                     return var2;
                  }

                  if (!var2.isChunkTicked()) {
                     return var2;
                  }

                  if (!var1x.hasPlayerMatchingInRange(128.0D, PlayerChunkMap.NOT_SPECTATOR)) {
                     continue;
                  }

                  return var2;
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
      long var1 = this.world.getTotalWorldTime();
      if (var1 - this.previousTotalWorldTime > 8000L) {
         this.previousTotalWorldTime = var1;

         for(int var3 = 0; var3 < this.entries.size(); ++var3) {
            PlayerChunkMapEntry var4 = (PlayerChunkMapEntry)this.entries.get(var3);
            var4.update();
            var4.updateChunkInhabitedTime();
         }
      }

      if (!this.dirtyEntries.isEmpty()) {
         for(PlayerChunkMapEntry var14 : this.dirtyEntries) {
            var14.update();
         }

         this.dirtyEntries.clear();
      }

      if (this.sortMissingChunks && var1 % 4L == 0L) {
         this.sortMissingChunks = false;
         Collections.sort(this.entriesWithoutChunks, new Comparator() {
            public int compare(PlayerChunkMapEntry var1, PlayerChunkMapEntry var2) {
               return ComparisonChain.start().compare(var1.getClosestPlayerDistance(), var2.getClosestPlayerDistance()).result();
            }

            public int compare(Object var1, Object var2) {
               return this.compare((PlayerChunkMapEntry)var1, (PlayerChunkMapEntry)var2);
            }
         });
      }

      if (this.sortSendToPlayers && var1 % 4L == 2L) {
         this.sortSendToPlayers = false;
         Collections.sort(this.pendingSendToPlayers, new Comparator() {
            public int compare(PlayerChunkMapEntry var1, PlayerChunkMapEntry var2) {
               return ComparisonChain.start().compare(var1.getClosestPlayerDistance(), var2.getClosestPlayerDistance()).result();
            }

            public int compare(Object var1, Object var2) {
               return this.compare((PlayerChunkMapEntry)var1, (PlayerChunkMapEntry)var2);
            }
         });
      }

      if (!this.entriesWithoutChunks.isEmpty()) {
         long var6 = System.nanoTime() + 50000000L;
         int var8 = 49;
         Iterator var9 = this.entriesWithoutChunks.iterator();

         while(var9.hasNext()) {
            PlayerChunkMapEntry var10 = (PlayerChunkMapEntry)var9.next();
            if (var10.getChunk() == null) {
               boolean var11 = var10.hasPlayerMatching(CAN_GENERATE_CHUNKS);
               if (var10.providePlayerChunk(var11)) {
                  var9.remove();
                  if (var10.sendToPlayers()) {
                     this.pendingSendToPlayers.remove(var10);
                  }

                  --var8;
                  if (var8 < 0 || System.nanoTime() > var6) {
                     break;
                  }
               }
            }
         }
      }

      if (!this.pendingSendToPlayers.isEmpty()) {
         int var13 = 81;
         Iterator var15 = this.pendingSendToPlayers.iterator();

         while(var15.hasNext()) {
            PlayerChunkMapEntry var12 = (PlayerChunkMapEntry)var15.next();
            if (var12.sendToPlayers()) {
               var15.remove();
               --var13;
               if (var13 < 0) {
                  break;
               }
            }
         }
      }

      if (this.players.isEmpty()) {
         WorldProvider var16 = this.world.provider;
         if (!var16.canRespawnHere()) {
            this.world.getChunkProvider().unloadAllChunks();
         }
      }

   }

   public boolean contains(int var1, int var2) {
      long var3 = getIndex(var1, var2);
      return this.entryMap.get(var3) != null;
   }

   @Nullable
   public PlayerChunkMapEntry getEntry(int var1, int var2) {
      return (PlayerChunkMapEntry)this.entryMap.get(getIndex(var1, var2));
   }

   private PlayerChunkMapEntry getOrCreateEntry(int var1, int var2) {
      long var3 = getIndex(var1, var2);
      PlayerChunkMapEntry var5 = (PlayerChunkMapEntry)this.entryMap.get(var3);
      if (var5 == null) {
         var5 = new PlayerChunkMapEntry(this, var1, var2);
         this.entryMap.put(var3, var5);
         this.entries.add(var5);
         if (var5.getChunk() == null) {
            this.entriesWithoutChunks.add(var5);
         }

         if (!var5.sendToPlayers()) {
            this.pendingSendToPlayers.add(var5);
         }
      }

      return var5;
   }

   public final boolean isChunkInUse(int var1, int var2) {
      PlayerChunkMapEntry var3 = this.getEntry(var1, var2);
      if (var3 != null) {
         return var3.players.size() > 0;
      } else {
         return false;
      }
   }

   public void markBlockForUpdate(BlockPos var1) {
      int var2 = var1.getX() >> 4;
      int var3 = var1.getZ() >> 4;
      PlayerChunkMapEntry var4 = this.getEntry(var2, var3);
      if (var4 != null) {
         var4.blockChanged(var1.getX() & 15, var1.getY(), var1.getZ() & 15);
      }

   }

   public void addPlayer(EntityPlayerMP var1) {
      int var2 = (int)var1.posX >> 4;
      int var3 = (int)var1.posZ >> 4;
      var1.managedPosX = var1.posX;
      var1.managedPosZ = var1.posZ;
      LinkedList var4 = new LinkedList();

      for(int var5 = var2 - this.playerViewRadius; var5 <= var2 + this.playerViewRadius; ++var5) {
         for(int var6 = var3 - this.playerViewRadius; var6 <= var3 + this.playerViewRadius; ++var6) {
            var4.add(new ChunkPos(var5, var6));
         }
      }

      Collections.sort(var4, new PlayerChunkMap.ChunkCoordComparator(var1));

      for(ChunkPos var7 : var4) {
         this.getOrCreateEntry(var7.chunkXPos, var7.chunkZPos).addPlayer(var1);
      }

      this.players.add(var1);
      this.markSortPending();
   }

   public void removePlayer(EntityPlayerMP var1) {
      int var2 = (int)var1.managedPosX >> 4;
      int var3 = (int)var1.managedPosZ >> 4;

      for(int var4 = var2 - this.playerViewRadius; var4 <= var2 + this.playerViewRadius; ++var4) {
         for(int var5 = var3 - this.playerViewRadius; var5 <= var3 + this.playerViewRadius; ++var5) {
            PlayerChunkMapEntry var6 = this.getEntry(var4, var5);
            if (var6 != null) {
               var6.removePlayer(var1);
            }
         }
      }

      this.players.remove(var1);
      this.markSortPending();
   }

   private boolean overlaps(int var1, int var2, int var3, int var4, int var5) {
      int var6 = var1 - var3;
      int var7 = var2 - var4;
      return var6 >= -var5 && var6 <= var5 ? var7 >= -var5 && var7 <= var5 : false;
   }

   public void updateMovingPlayer(EntityPlayerMP var1) {
      int var2 = (int)var1.posX >> 4;
      int var3 = (int)var1.posZ >> 4;
      double var4 = var1.managedPosX - var1.posX;
      double var6 = var1.managedPosZ - var1.posZ;
      double var8 = var4 * var4 + var6 * var6;
      if (var8 >= 64.0D) {
         int var10 = (int)var1.managedPosX >> 4;
         int var11 = (int)var1.managedPosZ >> 4;
         int var12 = this.playerViewRadius;
         int var13 = var2 - var10;
         int var14 = var3 - var11;
         LinkedList var15 = new LinkedList();
         if (var13 != 0 || var14 != 0) {
            for(int var16 = var2 - var12; var16 <= var2 + var12; ++var16) {
               for(int var17 = var3 - var12; var17 <= var3 + var12; ++var17) {
                  if (!this.overlaps(var16, var17, var10, var11, var12)) {
                     var15.add(new ChunkPos(var16, var17));
                  }

                  if (!this.overlaps(var16 - var13, var17 - var14, var2, var3, var12)) {
                     PlayerChunkMapEntry var18 = this.getEntry(var16 - var13, var17 - var14);
                     if (var18 != null) {
                        var18.removePlayer(var1);
                     }
                  }
               }
            }

            var1.managedPosX = var1.posX;
            var1.managedPosZ = var1.posZ;
            this.markSortPending();
            Collections.sort(var15, new PlayerChunkMap.ChunkCoordComparator(var1));

            for(ChunkPos var19 : var15) {
               this.getOrCreateEntry(var19.chunkXPos, var19.chunkZPos).addPlayer(var1);
            }
         }
      }

   }

   public boolean isPlayerWatchingChunk(EntityPlayerMP var1, int var2, int var3) {
      PlayerChunkMapEntry var4 = this.getEntry(var2, var3);
      return var4 != null && var4.containsPlayer(var1) && var4.isSentToPlayers();
   }

   public void setPlayerViewRadius(int var1) {
      var1 = MathHelper.clamp(var1, 3, 32);
      if (var1 != this.playerViewRadius) {
         int var2 = var1 - this.playerViewRadius;

         for(EntityPlayerMP var5 : Lists.newArrayList(this.players)) {
            int var6 = (int)var5.posX >> 4;
            int var7 = (int)var5.posZ >> 4;
            if (var2 > 0) {
               for(int var12 = var6 - var1; var12 <= var6 + var1; ++var12) {
                  for(int var13 = var7 - var1; var13 <= var7 + var1; ++var13) {
                     PlayerChunkMapEntry var10 = this.getOrCreateEntry(var12, var13);
                     if (!var10.containsPlayer(var5)) {
                        var10.addPlayer(var5);
                     }
                  }
               }
            } else {
               for(int var8 = var6 - this.playerViewRadius; var8 <= var6 + this.playerViewRadius; ++var8) {
                  for(int var9 = var7 - this.playerViewRadius; var9 <= var7 + this.playerViewRadius; ++var9) {
                     if (!this.overlaps(var8, var9, var6, var7, var1)) {
                        this.getOrCreateEntry(var8, var9).removePlayer(var5);
                     }
                  }
               }
            }
         }

         this.playerViewRadius = var1;
         this.markSortPending();
      }

   }

   private void markSortPending() {
      this.sortMissingChunks = true;
      this.sortSendToPlayers = true;
   }

   public static int getFurthestViewableBlock(int var0) {
      return var0 * 16 - 16;
   }

   private static long getIndex(int var0, int var1) {
      return (long)var0 + 2147483647L | (long)var1 + 2147483647L << 32;
   }

   public void entryChanged(PlayerChunkMapEntry var1) {
      this.dirtyEntries.add(var1);
   }

   public void removeEntry(PlayerChunkMapEntry var1) {
      ChunkPos var2 = var1.getPos();
      long var3 = getIndex(var2.chunkXPos, var2.chunkZPos);
      var1.updateChunkInhabitedTime();
      this.entryMap.remove(var3);
      this.entries.remove(var1);
      this.dirtyEntries.remove(var1);
      this.pendingSendToPlayers.remove(var1);
      this.entriesWithoutChunks.remove(var1);
      Chunk var5 = var1.getChunk();
      if (var5 != null) {
         this.getWorldServer().getChunkProvider().unload(var5);
      }

   }

   private static class ChunkCoordComparator implements Comparator {
      private int x;
      private int z;

      public ChunkCoordComparator(EntityPlayerMP var1) {
         this.x = (int)var1.posX >> 4;
         this.z = (int)var1.posZ >> 4;
      }

      public int compare(ChunkPos var1, ChunkPos var2) {
         if (var1.equals(var2)) {
            return 0;
         } else {
            int var3 = var1.chunkXPos - this.x;
            int var4 = var1.chunkZPos - this.z;
            int var5 = var2.chunkXPos - this.x;
            int var6 = var2.chunkZPos - this.z;
            int var7 = (var3 - var5) * (var3 + var5) + (var4 - var6) * (var4 + var6);
            if (var7 != 0) {
               return var7;
            } else if (var3 < 0) {
               return var5 < 0 ? var6 - var4 : -1;
            } else {
               return var5 < 0 ? 1 : var4 - var6;
            }
         }
      }
   }
}
