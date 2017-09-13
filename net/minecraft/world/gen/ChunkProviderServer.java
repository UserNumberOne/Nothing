package net.minecraft.world.gen;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.IOException;
import java.util.ArrayList;
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
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.chunkio.ChunkIOExecutor;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkProviderServer implements IChunkProvider {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Set droppedChunksSet = Sets.newHashSet();
   public final IChunkGenerator chunkGenerator;
   public final IChunkLoader chunkLoader;
   public final Long2ObjectMap id2ChunkMap = new Long2ObjectOpenHashMap(8192);
   public final WorldServer world;
   private Set loadingChunks = Sets.newHashSet();

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
      return this.loadChunk(var1, var2, (Runnable)null);
   }

   @Nullable
   public Chunk loadChunk(int var1, int var2, Runnable var3) {
      Chunk var4 = this.getLoadedChunk(var1, var2);
      if (var4 == null) {
         long var5 = ChunkPos.asLong(var1, var2);
         var4 = ForgeChunkManager.fetchDormantChunk(var5, this.world);
         if (var4 == null && this.chunkLoader instanceof AnvilChunkLoader) {
            AnvilChunkLoader var7 = (AnvilChunkLoader)this.chunkLoader;
            if (var3 == null) {
               var4 = ChunkIOExecutor.syncChunkLoad(this.world, var7, this, var1, var2);
            } else if (var7.chunkExists(this.world, var1, var2)) {
               ChunkIOExecutor.queueChunkLoad(this.world, var7, this, var1, var2, var3);
               return null;
            }
         } else {
            if (!this.loadingChunks.add(Long.valueOf(var5))) {
               FMLLog.bigWarning("There is an attempt to load a chunk (%d,%d) in dimension %d that is already being loaded. This will cause weird chunk breakages.", new Object[]{var1, var2, this.world.provider.getDimension()});
            }

            if (var4 == null) {
               var4 = this.loadChunkFromFile(var1, var2);
            }

            if (var4 != null) {
               this.id2ChunkMap.put(ChunkPos.asLong(var1, var2), var4);
               var4.onChunkLoad();
               var4.populateChunk(this, this.chunkGenerator);
            }

            this.loadingChunks.remove(Long.valueOf(var5));
         }
      }

      if (var3 != null) {
         var3.run();
      }

      return var4;
   }

   public Chunk provideChunk(int var1, int var2) {
      Chunk var3 = this.loadChunk(var1, var2);
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
         var3.populateChunk(this, this.chunkGenerator);
      }

      return var3;
   }

   @Nullable
   private Chunk loadChunkFromFile(int var1, int var2) {
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

   private void saveChunkExtraData(Chunk var1) {
      try {
         this.chunkLoader.saveExtraChunkData(this.world, var1);
      } catch (Exception var3) {
         LOGGER.error("Couldn't save entities", var3);
      }

   }

   private void saveChunkData(Chunk var1) {
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
      ArrayList var3 = Lists.newArrayList(this.id2ChunkMap.values());

      for(int var4 = 0; var4 < var3.size(); ++var4) {
         Chunk var5 = (Chunk)var3.get(var4);
         if (var1) {
            this.saveChunkExtraData(var5);
         }

         if (var5.needsSaving(var1)) {
            this.saveChunkData(var5);
            var5.setModified(false);
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
            UnmodifiableIterator var1 = this.world.getPersistentChunks().keySet().iterator();

            while(var1.hasNext()) {
               ChunkPos var2 = (ChunkPos)var1.next();
               this.droppedChunksSet.remove(Long.valueOf(ChunkPos.asLong(var2.chunkXPos, var2.chunkZPos)));
            }

            Iterator var5 = this.droppedChunksSet.iterator();

            for(int var6 = 0; var6 < 100 && var5.hasNext(); var5.remove()) {
               Long var3 = (Long)var5.next();
               Chunk var4 = (Chunk)this.id2ChunkMap.get(var3);
               if (var4 != null && var4.unloaded) {
                  var4.onChunkUnload();
                  this.saveChunkData(var4);
                  this.saveChunkExtraData(var4);
                  this.id2ChunkMap.remove(var3);
                  ++var6;
                  ForgeChunkManager.putDormantChunk(ChunkPos.asLong(var4.xPosition, var4.zPosition), var4);
                  if (this.id2ChunkMap.size() == 0 && ForgeChunkManager.getPersistentChunksFor(this.world).size() == 0 && !this.world.provider.getDimensionType().shouldLoadSpawn()) {
                     DimensionManager.unloadWorld(this.world.provider.getDimension());
                     break;
                  }
               }
            }
         }

         this.chunkLoader.chunkTick();
      }

      return false;
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
