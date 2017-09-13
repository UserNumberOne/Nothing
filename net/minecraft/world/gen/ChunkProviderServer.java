package net.minecraft.world.gen;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
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

   public ChunkProviderServer(WorldServer worldObjIn, IChunkLoader chunkLoaderIn, IChunkGenerator chunkGeneratorIn) {
      this.world = worldObjIn;
      this.chunkLoader = chunkLoaderIn;
      this.chunkGenerator = chunkGeneratorIn;
   }

   public Collection getLoadedChunks() {
      return this.id2ChunkMap.values();
   }

   public void unload(Chunk chunkIn) {
      if (this.world.provider.canDropChunk(chunkIn.xPosition, chunkIn.zPosition)) {
         this.droppedChunksSet.add(Long.valueOf(ChunkPos.asLong(chunkIn.xPosition, chunkIn.zPosition)));
         chunkIn.unloaded = true;
      }

   }

   public void unloadAllChunks() {
      ObjectIterator var1 = this.id2ChunkMap.values().iterator();

      while(var1.hasNext()) {
         Chunk chunk = (Chunk)var1.next();
         this.unload(chunk);
      }

   }

   @Nullable
   public Chunk getLoadedChunk(int x, int z) {
      long i = ChunkPos.asLong(x, z);
      Chunk chunk = (Chunk)this.id2ChunkMap.get(i);
      if (chunk != null) {
         chunk.unloaded = false;
      }

      return chunk;
   }

   @Nullable
   public Chunk loadChunk(int x, int z) {
      return this.loadChunk(x, z, (Runnable)null);
   }

   @Nullable
   public Chunk loadChunk(int x, int z, Runnable runnable) {
      Chunk chunk = this.getLoadedChunk(x, z);
      if (chunk == null) {
         long pos = ChunkPos.asLong(x, z);
         chunk = ForgeChunkManager.fetchDormantChunk(pos, this.world);
         if (chunk == null && this.chunkLoader instanceof AnvilChunkLoader) {
            AnvilChunkLoader loader = (AnvilChunkLoader)this.chunkLoader;
            if (runnable == null) {
               chunk = ChunkIOExecutor.syncChunkLoad(this.world, loader, this, x, z);
            } else if (loader.chunkExists(this.world, x, z)) {
               ChunkIOExecutor.queueChunkLoad(this.world, loader, this, x, z, runnable);
               return null;
            }
         } else {
            if (!this.loadingChunks.add(Long.valueOf(pos))) {
               FMLLog.bigWarning("There is an attempt to load a chunk (%d,%d) in dimension %d that is already being loaded. This will cause weird chunk breakages.", new Object[]{x, z, this.world.provider.getDimension()});
            }

            if (chunk == null) {
               chunk = this.loadChunkFromFile(x, z);
            }

            if (chunk != null) {
               this.id2ChunkMap.put(ChunkPos.asLong(x, z), chunk);
               chunk.onChunkLoad();
               chunk.populateChunk(this, this.chunkGenerator);
            }

            this.loadingChunks.remove(Long.valueOf(pos));
         }
      }

      if (runnable != null) {
         runnable.run();
      }

      return chunk;
   }

   public Chunk provideChunk(int x, int z) {
      Chunk chunk = this.loadChunk(x, z);
      if (chunk == null) {
         long i = ChunkPos.asLong(x, z);

         try {
            chunk = this.chunkGenerator.provideChunk(x, z);
         } catch (Throwable var9) {
            CrashReport crashreport = CrashReport.makeCrashReport(var9, "Exception generating new chunk");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Chunk to be generated");
            crashreportcategory.addCrashSection("Location", String.format("%d,%d", x, z));
            crashreportcategory.addCrashSection("Position hash", Long.valueOf(i));
            crashreportcategory.addCrashSection("Generator", this.chunkGenerator);
            throw new ReportedException(crashreport);
         }

         this.id2ChunkMap.put(i, chunk);
         chunk.onChunkLoad();
         chunk.populateChunk(this, this.chunkGenerator);
      }

      return chunk;
   }

   @Nullable
   private Chunk loadChunkFromFile(int x, int z) {
      try {
         Chunk chunk = this.chunkLoader.loadChunk(this.world, x, z);
         if (chunk != null) {
            chunk.setLastSaveTime(this.world.getTotalWorldTime());
            this.chunkGenerator.recreateStructures(chunk, x, z);
         }

         return chunk;
      } catch (Exception var4) {
         LOGGER.error("Couldn't load chunk", var4);
         return null;
      }
   }

   private void saveChunkExtraData(Chunk chunkIn) {
      try {
         this.chunkLoader.saveExtraChunkData(this.world, chunkIn);
      } catch (Exception var3) {
         LOGGER.error("Couldn't save entities", var3);
      }

   }

   private void saveChunkData(Chunk chunkIn) {
      try {
         chunkIn.setLastSaveTime(this.world.getTotalWorldTime());
         this.chunkLoader.saveChunk(this.world, chunkIn);
      } catch (IOException var3) {
         LOGGER.error("Couldn't save chunk", var3);
      } catch (MinecraftException var4) {
         LOGGER.error("Couldn't save chunk; already in use by another instance of Minecraft?", var4);
      }

   }

   public boolean saveChunks(boolean p_186027_1_) {
      int i = 0;
      List list = Lists.newArrayList(this.id2ChunkMap.values());

      for(int j = 0; j < list.size(); ++j) {
         Chunk chunk = (Chunk)list.get(j);
         if (p_186027_1_) {
            this.saveChunkExtraData(chunk);
         }

         if (chunk.needsSaving(p_186027_1_)) {
            this.saveChunkData(chunk);
            chunk.setModified(false);
            ++i;
            if (i == 24 && !p_186027_1_) {
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
            UnmodifiableIterator iterator = this.world.getPersistentChunks().keySet().iterator();

            while(iterator.hasNext()) {
               ChunkPos forced = (ChunkPos)iterator.next();
               this.droppedChunksSet.remove(Long.valueOf(ChunkPos.asLong(forced.chunkXPos, forced.chunkZPos)));
            }

            Iterator iterator = this.droppedChunksSet.iterator();

            for(int i = 0; i < 100 && iterator.hasNext(); iterator.remove()) {
               Long olong = (Long)iterator.next();
               Chunk chunk = (Chunk)this.id2ChunkMap.get(olong);
               if (chunk != null && chunk.unloaded) {
                  chunk.onChunkUnload();
                  this.saveChunkData(chunk);
                  this.saveChunkExtraData(chunk);
                  this.id2ChunkMap.remove(olong);
                  ++i;
                  ForgeChunkManager.putDormantChunk(ChunkPos.asLong(chunk.xPosition, chunk.zPosition), chunk);
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

   public List getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
      return this.chunkGenerator.getPossibleCreatures(creatureType, pos);
   }

   @Nullable
   public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position) {
      return this.chunkGenerator.getStrongholdGen(worldIn, structureName, position);
   }

   public int getLoadedChunkCount() {
      return this.id2ChunkMap.size();
   }

   public boolean chunkExists(int x, int z) {
      return this.id2ChunkMap.containsKey(ChunkPos.asLong(x, z));
   }
}
