package net.minecraft.world.gen;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.IChunkLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.v1_10_R1.chunkio.ChunkIOExecutor;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkProviderServer implements IChunkProvider {
   private static final Logger LOGGER = LogManager.getLogger();
   public final Set droppedChunksSet = Sets.newHashSet();
   public final IChunkGenerator chunkGenerator;
   private final IChunkLoader chunkLoader;
   public final Long2ObjectMap id2ChunkMap = new Long2ObjectOpenHashMap(8192);
   public final WorldServer world;

   public ChunkProviderServer(WorldServer var1, IChunkLoader var2, IChunkGenerator var3) {
      this.world = var1;
      this.chunkLoader = var2;
      this.chunkGenerator = var3;
   }

   public Collection getLoadedChunks() {
      return this.id2ChunkMap.values();
   }

   public void unload(Chunk var1) {
      if (this.world.provider.canDropChunk(var1.xPosition, var1.zPosition)) {
         this.droppedChunksSet.add(Long.valueOf(ChunkPos.asLong(var1.xPosition, var1.zPosition)));
         var1.unloaded = true;
      }

   }

   public void unloadAllChunks() {
      ObjectIterator var1 = this.id2ChunkMap.values().iterator();

      while(var1.hasNext()) {
         Chunk var2 = (Chunk)var1.next();
         this.unload(var2);
      }

   }

   @Nullable
   public Chunk getLoadedChunk(int var1, int var2) {
      long var3 = ChunkPos.asLong(var1, var2);
      Chunk var5 = (Chunk)this.id2ChunkMap.get(var3);
      if (var5 != null) {
         var5.unloaded = false;
      }

      return var5;
   }

   @Nullable
   public Chunk loadChunk(int var1, int var2) {
      Chunk var3 = this.getLoadedChunk(var1, var2);
      if (var3 == null) {
         AnvilChunkLoader var4 = null;
         if (this.chunkLoader instanceof AnvilChunkLoader) {
            var4 = (AnvilChunkLoader)this.chunkLoader;
         }

         if (var4 != null && var4.chunkExists(this.world, var1, var2)) {
            var3 = ChunkIOExecutor.syncChunkLoad(this.world, var4, this, var1, var2);
         }
      }

      return var3;
   }

   @Nullable
   public Chunk originalGetOrLoadChunkAt(int var1, int var2) {
      Chunk var3 = this.getLoadedChunk(var1, var2);
      if (var3 == null) {
         var3 = this.loadChunkFromFile(var1, var2);
         if (var3 != null) {
            this.id2ChunkMap.put(ChunkPos.asLong(var1, var2), var3);
            var3.onChunkLoad();
            var3.loadNearby(this, this.chunkGenerator, false);
         }
      }

      return var3;
   }

   public Chunk getChunkIfLoaded(int var1, int var2) {
      return (Chunk)this.id2ChunkMap.get(ChunkPos.asLong(var1, var2));
   }

   public Chunk provideChunk(int var1, int var2) {
      return this.getChunkAt(var1, var2, (Runnable)null);
   }

   public Chunk getChunkAt(int var1, int var2, Runnable var3) {
      return this.getChunkAt(var1, var2, var3, true);
   }

   public Chunk getChunkAt(int var1, int var2, Runnable var3, boolean var4) {
      Chunk var5 = this.getChunkIfLoaded(var1, var2);
      AnvilChunkLoader var6 = null;
      if (this.chunkLoader instanceof AnvilChunkLoader) {
         var6 = (AnvilChunkLoader)this.chunkLoader;
      }

      if (var5 == null && var6 != null && var6.chunkExists(this.world, var1, var2)) {
         if (var3 != null) {
            ChunkIOExecutor.queueChunkLoad(this.world, var6, this, var1, var2, var3);
            return null;
         }

         var5 = ChunkIOExecutor.syncChunkLoad(this.world, var6, this, var1, var2);
      } else if (var5 == null && var4) {
         var5 = this.originalGetChunkAt(var1, var2);
      }

      if (var3 != null) {
         var3.run();
      }

      return var5;
   }

   public Chunk originalGetChunkAt(int var1, int var2) {
      Chunk var3 = this.originalGetOrLoadChunkAt(var1, var2);
      if (var3 == null) {
         long var4 = ChunkPos.asLong(var1, var2);

         try {
            var3 = this.chunkGenerator.provideChunk(var1, var2);
         } catch (Throwable var9) {
            CrashReport var7 = CrashReport.makeCrashReport(var9, "Exception generating new chunk");
            CrashReportCategory var8 = var7.makeCategory("Chunk to be generated");
            var8.addCrashSection("Location", String.format("%d,%d", var1, var2));
            var8.addCrashSection("Position hash", Long.valueOf(var4));
            var8.addCrashSection("Generator", this.chunkGenerator);
            throw new ReportedException(var7);
         }

         this.id2ChunkMap.put(var4, var3);
         var3.onChunkLoad();
         var3.loadNearby(this, this.chunkGenerator, true);
      }

      return var3;
   }

   @Nullable
   public Chunk loadChunkFromFile(int var1, int var2) {
      try {
         Chunk var3 = this.chunkLoader.loadChunk(this.world, var1, var2);
         if (var3 != null) {
            var3.setLastSaveTime(this.world.getTotalWorldTime());
            this.chunkGenerator.recreateStructures(var3, var1, var2);
         }

         return var3;
      } catch (Exception var4) {
         LOGGER.error("Couldn't load chunk", var4);
         return null;
      }
   }

   public void saveChunkExtraData(Chunk var1) {
      try {
         this.chunkLoader.saveExtraChunkData(this.world, var1);
      } catch (Exception var3) {
         LOGGER.error("Couldn't save entities", var3);
      }

   }

   public void saveChunkData(Chunk var1) {
      try {
         var1.setLastSaveTime(this.world.getTotalWorldTime());
         this.chunkLoader.saveChunk(this.world, var1);
      } catch (IOException var3) {
         LOGGER.error("Couldn't save chunk", var3);
      } catch (MinecraftException var4) {
         LOGGER.error("Couldn't save chunk; already in use by another instance of Minecraft?", var4);
      }

   }

   public boolean saveChunks(boolean var1) {
      int var2 = 0;
      ObjectIterator var3 = this.id2ChunkMap.values().iterator();

      while(var3.hasNext()) {
         Chunk var4 = (Chunk)var3.next();
         if (var1) {
            this.saveChunkExtraData(var4);
         }

         if (var4.needsSaving(var1)) {
            this.saveChunkData(var4);
            var4.setModified(false);
            ++var2;
            if (var2 == 24 && !var1) {
               return false;
            }
         }
      }

      return true;
   }

   public void saveExtraData() {
      this.chunkLoader.saveExtraData();
   }

   public boolean tick() {
      if (!this.world.disableLevelSaving) {
         if (!this.droppedChunksSet.isEmpty()) {
            Iterator var1 = this.droppedChunksSet.iterator();

            for(int var2 = 0; var2 < 100 && var1.hasNext(); var1.remove()) {
               Long var3 = (Long)var1.next();
               Chunk var4 = (Chunk)this.id2ChunkMap.get(var3);
               if (var4 != null && var4.unloaded && this.unloadChunk(var4, true)) {
                  ++var2;
               }
            }
         }

         this.chunkLoader.chunkTick();
      }

      return false;
   }

   public boolean unloadChunk(Chunk var1, boolean var2) {
      ChunkUnloadEvent var3 = new ChunkUnloadEvent(var1.bukkitChunk, var2);
      this.world.getServer().getPluginManager().callEvent(var3);
      if (var3.isCancelled()) {
         return false;
      } else {
         var2 = var3.isSaveChunk();

         for(int var4 = -2; var4 < 3; ++var4) {
            for(int var5 = -2; var5 < 3; ++var5) {
               if (var4 != 0 || var5 != 0) {
                  Chunk var6 = this.getChunkIfLoaded(var1.xPosition + var4, var1.zPosition + var5);
                  if (var6 != null) {
                     var6.setNeighborUnloaded(-var4, -var5);
                     var1.setNeighborUnloaded(var4, var5);
                  }
               }
            }
         }

         var1.onChunkUnload();
         if (var2) {
            this.saveChunkData(var1);
            this.saveChunkExtraData(var1);
         }

         this.id2ChunkMap.remove(var1.chunkKey);
         return true;
      }
   }

   public boolean canSave() {
      return !this.world.disableLevelSaving;
   }

   public String makeString() {
      return "ServerChunkCache: " + this.id2ChunkMap.size() + " Drop: " + this.droppedChunksSet.size();
   }

   public List getPossibleCreatures(EnumCreatureType var1, BlockPos var2) {
      return this.chunkGenerator.getPossibleCreatures(var1, var2);
   }

   @Nullable
   public BlockPos getStrongholdGen(World var1, String var2, BlockPos var3) {
      return this.chunkGenerator.getStrongholdGen(var1, var2, var3);
   }

   public int getLoadedChunkCount() {
      return this.id2ChunkMap.size();
   }

   public boolean chunkExists(int var1, int var2) {
      return this.id2ChunkMap.containsKey(ChunkPos.asLong(var1, var2));
   }
}
