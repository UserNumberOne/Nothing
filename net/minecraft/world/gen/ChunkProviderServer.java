package net.minecraft.world.gen;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
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

   public ChunkProviderServer(WorldServer worldserver, IChunkLoader ichunkloader, IChunkGenerator chunkgenerator) {
      this.world = worldserver;
      this.chunkLoader = ichunkloader;
      this.chunkGenerator = chunkgenerator;
   }

   public Collection getLoadedChunks() {
      return this.id2ChunkMap.values();
   }

   public void unload(Chunk chunk) {
      if (this.world.provider.canDropChunk(chunk.xPosition, chunk.zPosition)) {
         this.droppedChunksSet.add(Long.valueOf(ChunkPos.asLong(chunk.xPosition, chunk.zPosition)));
         chunk.unloaded = true;
      }

   }

   public void unloadAllChunks() {
      Iterator iterator = this.id2ChunkMap.values().iterator();

      while(iterator.hasNext()) {
         Chunk chunk = (Chunk)iterator.next();
         this.unload(chunk);
      }

   }

   @Nullable
   public Chunk getLoadedChunk(int i, int j) {
      long k = ChunkPos.asLong(i, j);
      Chunk chunk = (Chunk)this.id2ChunkMap.get(k);
      if (chunk != null) {
         chunk.unloaded = false;
      }

      return chunk;
   }

   @Nullable
   public Chunk loadChunk(int i, int j) {
      Chunk chunk = this.getLoadedChunk(i, j);
      if (chunk == null) {
         AnvilChunkLoader loader = null;
         if (this.chunkLoader instanceof AnvilChunkLoader) {
            loader = (AnvilChunkLoader)this.chunkLoader;
         }

         if (loader != null && loader.chunkExists(this.world, i, j)) {
            chunk = ChunkIOExecutor.syncChunkLoad(this.world, loader, this, i, j);
         }
      }

      return chunk;
   }

   @Nullable
   public Chunk originalGetOrLoadChunkAt(int i, int j) {
      Chunk chunk = this.getLoadedChunk(i, j);
      if (chunk == null) {
         chunk = this.loadChunkFromFile(i, j);
         if (chunk != null) {
            this.id2ChunkMap.put(ChunkPos.asLong(i, j), chunk);
            chunk.onChunkLoad();
            chunk.loadNearby(this, this.chunkGenerator, false);
         }
      }

      return chunk;
   }

   public Chunk getChunkIfLoaded(int x, int z) {
      return (Chunk)this.id2ChunkMap.get(ChunkPos.asLong(x, z));
   }

   public Chunk provideChunk(int i, int j) {
      return this.getChunkAt(i, j, (Runnable)null);
   }

   public Chunk getChunkAt(int i, int j, Runnable runnable) {
      return this.getChunkAt(i, j, runnable, true);
   }

   public Chunk getChunkAt(int i, int j, Runnable runnable, boolean generate) {
      Chunk chunk = this.getChunkIfLoaded(i, j);
      AnvilChunkLoader loader = null;
      if (this.chunkLoader instanceof AnvilChunkLoader) {
         loader = (AnvilChunkLoader)this.chunkLoader;
      }

      if (chunk == null && loader != null && loader.chunkExists(this.world, i, j)) {
         if (runnable != null) {
            ChunkIOExecutor.queueChunkLoad(this.world, loader, this, i, j, runnable);
            return null;
         }

         chunk = ChunkIOExecutor.syncChunkLoad(this.world, loader, this, i, j);
      } else if (chunk == null && generate) {
         chunk = this.originalGetChunkAt(i, j);
      }

      if (runnable != null) {
         runnable.run();
      }

      return chunk;
   }

   public Chunk originalGetChunkAt(int i, int j) {
      Chunk chunk = this.originalGetOrLoadChunkAt(i, j);
      if (chunk == null) {
         long k = ChunkPos.asLong(i, j);

         try {
            chunk = this.chunkGenerator.provideChunk(i, j);
         } catch (Throwable var9) {
            CrashReport crashreport = CrashReport.makeCrashReport(var9, "Exception generating new chunk");
            CrashReportCategory crashreportsystemdetails = crashreport.makeCategory("Chunk to be generated");
            crashreportsystemdetails.addCrashSection("Location", String.format("%d,%d", i, j));
            crashreportsystemdetails.addCrashSection("Position hash", Long.valueOf(k));
            crashreportsystemdetails.addCrashSection("Generator", this.chunkGenerator);
            throw new ReportedException(crashreport);
         }

         this.id2ChunkMap.put(k, chunk);
         chunk.onChunkLoad();
         chunk.loadNearby(this, this.chunkGenerator, true);
      }

      return chunk;
   }

   @Nullable
   public Chunk loadChunkFromFile(int i, int j) {
      try {
         Chunk chunk = this.chunkLoader.loadChunk(this.world, i, j);
         if (chunk != null) {
            chunk.setLastSaveTime(this.world.getTotalWorldTime());
            this.chunkGenerator.recreateStructures(chunk, i, j);
         }

         return chunk;
      } catch (Exception var4) {
         LOGGER.error("Couldn't load chunk", var4);
         return null;
      }
   }

   public void saveChunkExtraData(Chunk chunk) {
      try {
         this.chunkLoader.saveExtraChunkData(this.world, chunk);
      } catch (Exception var3) {
         LOGGER.error("Couldn't save entities", var3);
      }

   }

   public void saveChunkData(Chunk chunk) {
      try {
         chunk.setLastSaveTime(this.world.getTotalWorldTime());
         this.chunkLoader.saveChunk(this.world, chunk);
      } catch (IOException var3) {
         LOGGER.error("Couldn't save chunk", var3);
      } catch (MinecraftException var4) {
         LOGGER.error("Couldn't save chunk; already in use by another instance of Minecraft?", var4);
      }

   }

   public boolean saveChunks(boolean flag) {
      int i = 0;
      Iterator iterator = this.id2ChunkMap.values().iterator();

      while(iterator.hasNext()) {
         Chunk chunk = (Chunk)iterator.next();
         if (flag) {
            this.saveChunkExtraData(chunk);
         }

         if (chunk.needsSaving(flag)) {
            this.saveChunkData(chunk);
            chunk.setModified(false);
            ++i;
            if (i == 24 && !flag) {
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
            Iterator iterator = this.droppedChunksSet.iterator();

            for(int i = 0; i < 100 && iterator.hasNext(); iterator.remove()) {
               Long olong = (Long)iterator.next();
               Chunk chunk = (Chunk)this.id2ChunkMap.get(olong);
               if (chunk != null && chunk.unloaded && this.unloadChunk(chunk, true)) {
                  ++i;
               }
            }
         }

         this.chunkLoader.chunkTick();
      }

      return false;
   }

   public boolean unloadChunk(Chunk chunk, boolean save) {
      ChunkUnloadEvent event = new ChunkUnloadEvent(chunk.bukkitChunk, save);
      this.world.getServer().getPluginManager().callEvent(event);
      if (event.isCancelled()) {
         return false;
      } else {
         save = event.isSaveChunk();

         for(int x = -2; x < 3; ++x) {
            for(int z = -2; z < 3; ++z) {
               if (x != 0 || z != 0) {
                  Chunk neighbor = this.getChunkIfLoaded(chunk.xPosition + x, chunk.zPosition + z);
                  if (neighbor != null) {
                     neighbor.setNeighborUnloaded(-x, -z);
                     chunk.setNeighborUnloaded(x, z);
                  }
               }
            }
         }

         chunk.onChunkUnload();
         if (save) {
            this.saveChunkData(chunk);
            this.saveChunkExtraData(chunk);
         }

         this.id2ChunkMap.remove(chunk.chunkKey);
         return true;
      }
   }

   public boolean canSave() {
      return !this.world.disableLevelSaving;
   }

   public String makeString() {
      return "ServerChunkCache: " + this.id2ChunkMap.size() + " Drop: " + this.droppedChunksSet.size();
   }

   public List getPossibleCreatures(EnumCreatureType enumcreaturetype, BlockPos blockposition) {
      return this.chunkGenerator.getPossibleCreatures(enumcreaturetype, blockposition);
   }

   @Nullable
   public BlockPos getStrongholdGen(World world, String s, BlockPos blockposition) {
      return this.chunkGenerator.getStrongholdGen(world, s, blockposition);
   }

   public int getLoadedChunkCount() {
      return this.id2ChunkMap.size();
   }

   public boolean chunkExists(int i, int j) {
      return this.id2ChunkMap.containsKey(ChunkPos.asLong(i, j));
   }
}
