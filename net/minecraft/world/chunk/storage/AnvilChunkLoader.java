package net.minecraft.world.chunk.storage;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.storage.IThreadedFileIO;
import net.minecraft.world.storage.ThreadedFileIOBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class AnvilChunkLoader implements IChunkLoader, IThreadedFileIO {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Map chunksToRemove = new ConcurrentHashMap();
   private final Set pendingAnvilChunksCoordinates = Collections.newSetFromMap(new ConcurrentHashMap());
   private final File chunkSaveLocation;
   private final DataFixer dataFixer;
   private boolean savingExtraData;

   public AnvilChunkLoader(File file, DataFixer dataconvertermanager) {
      this.chunkSaveLocation = file;
      this.dataFixer = dataconvertermanager;
   }

   public boolean chunkExists(World world, int i, int j) {
      ChunkPos chunkcoordintpair = new ChunkPos(i, j);
      return this.pendingAnvilChunksCoordinates.contains(chunkcoordintpair) && this.chunksToRemove.containsKey(chunkcoordintpair) ? true : RegionFileCache.createOrLoadRegionFile(this.chunkSaveLocation, i, j).chunkExists(i & 31, j & 31);
   }

   @Nullable
   public Chunk loadChunk(World world, int i, int j) throws IOException {
      Object[] data = this.loadChunk(world, i, j);
      if (data != null) {
         Chunk chunk = (Chunk)data[0];
         NBTTagCompound nbttagcompound = (NBTTagCompound)data[1];
         this.loadEntities(chunk, nbttagcompound.getCompoundTag("Level"), world);
         return chunk;
      } else {
         return null;
      }
   }

   public Object[] loadChunk(World world, int i, int j) throws IOException {
      ChunkPos chunkcoordintpair = new ChunkPos(i, j);
      NBTTagCompound nbttagcompound = (NBTTagCompound)this.chunksToRemove.get(chunkcoordintpair);
      if (nbttagcompound == null) {
         nbttagcompound = RegionFileCache.c(this.chunkSaveLocation, i, j);
         if (nbttagcompound == null) {
            return null;
         }

         nbttagcompound = this.dataFixer.process(FixTypes.CHUNK, nbttagcompound);
      }

      return this.a(world, i, j, nbttagcompound);
   }

   protected Object[] a(World world, int i, int j, NBTTagCompound nbttagcompound) {
      if (!nbttagcompound.hasKey("Level", 10)) {
         LOGGER.error("Chunk file at {},{} is missing level data, skipping", new Object[]{i, j});
         return null;
      } else {
         NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("Level");
         if (!nbttagcompound1.hasKey("Sections", 9)) {
            LOGGER.error("Chunk file at {},{} is missing block data, skipping", new Object[]{i, j});
            return null;
         } else {
            Chunk chunk = this.readChunkFromNBT(world, nbttagcompound1);
            if (!chunk.isAtLocation(i, j)) {
               LOGGER.error("Chunk file at {},{} is in the wrong location; relocating. (Expected {}, {}, got {}, {})", new Object[]{i, j, i, j, chunk.xPosition, chunk.zPosition});
               nbttagcompound1.setInteger("xPos", i);
               nbttagcompound1.setInteger("zPos", j);
               NBTTagList tileEntities = nbttagcompound.getCompoundTag("Level").getTagList("TileEntities", 10);
               if (tileEntities != null) {
                  for(int te = 0; te < tileEntities.tagCount(); ++te) {
                     NBTTagCompound tileEntity = tileEntities.getCompoundTagAt(te);
                     int x = tileEntity.getInteger("x") - chunk.xPosition * 16;
                     int z = tileEntity.getInteger("z") - chunk.zPosition * 16;
                     tileEntity.setInteger("x", i * 16 + x);
                     tileEntity.setInteger("z", j * 16 + z);
                  }
               }

               chunk = this.readChunkFromNBT(world, nbttagcompound1);
            }

            Object[] data = new Object[2];
            data[0] = chunk;
            data[1] = nbttagcompound;
            return data;
         }
      }
   }

   public void saveChunk(World world, Chunk chunk) throws IOException, MinecraftException {
      world.checkSessionLock();

      try {
         NBTTagCompound nbttagcompound = new NBTTagCompound();
         NBTTagCompound nbttagcompound1 = new NBTTagCompound();
         nbttagcompound.setTag("Level", nbttagcompound1);
         nbttagcompound.setInteger("DataVersion", 512);
         this.writeChunkToNBT(chunk, world, nbttagcompound1);
         this.addChunkToPending(chunk.getChunkCoordIntPair(), nbttagcompound);
      } catch (Exception var5) {
         LOGGER.error("Failed to save chunk", var5);
      }

   }

   protected void addChunkToPending(ChunkPos chunkcoordintpair, NBTTagCompound nbttagcompound) {
      if (!this.pendingAnvilChunksCoordinates.contains(chunkcoordintpair)) {
         this.chunksToRemove.put(chunkcoordintpair, nbttagcompound);
      }

      ThreadedFileIOBase.getThreadedIOInstance().queueIO(this);
   }

   public boolean writeNextIO() {
      Iterator iter = this.chunksToRemove.entrySet().iterator();
      if (!iter.hasNext()) {
         if (this.savingExtraData) {
            LOGGER.info("ThreadedAnvilChunkStorage ({}): All chunks are saved", new Object[]{this.chunkSaveLocation.getName()});
         }

         return false;
      } else {
         Entry entry = (Entry)iter.next();
         iter.remove();
         ChunkPos chunkcoordintpair = (ChunkPos)entry.getKey();

         boolean flag;
         try {
            this.pendingAnvilChunksCoordinates.add(chunkcoordintpair);
            NBTTagCompound nbttagcompound = (NBTTagCompound)entry.getValue();
            if (nbttagcompound != null) {
               try {
                  this.writeChunkData(chunkcoordintpair, nbttagcompound);
               } catch (Exception var10) {
                  LOGGER.error("Failed to save chunk", var10);
               }
            }

            flag = true;
         } finally {
            this.pendingAnvilChunksCoordinates.remove(chunkcoordintpair);
         }

         return flag;
      }
   }

   private void writeChunkData(ChunkPos chunkcoordintpair, NBTTagCompound nbttagcompound) throws IOException {
      RegionFileCache.d(this.chunkSaveLocation, chunkcoordintpair.chunkXPos, chunkcoordintpair.chunkZPos, nbttagcompound);
   }

   public void saveExtraChunkData(World world, Chunk chunk) throws IOException {
   }

   public void chunkTick() {
   }

   public void saveExtraData() {
      try {
         this.savingExtraData = true;

         while(true) {
            if (this.writeNextIO()) {
               continue;
            }
         }
      } finally {
         this.savingExtraData = false;
      }

   }

   public static void registerFixes(DataFixer dataconvertermanager) {
      dataconvertermanager.registerWalker(FixTypes.CHUNK, new IDataWalker() {
         public NBTTagCompound process(IDataFixer dataconverter, NBTTagCompound nbttagcompound, int i) {
            if (nbttagcompound.hasKey("Level", 10)) {
               NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("Level");
               if (nbttagcompound1.hasKey("Entities", 9)) {
                  NBTTagList nbttaglist = nbttagcompound1.getTagList("Entities", 10);

                  for(int j = 0; j < nbttaglist.tagCount(); ++j) {
                     nbttaglist.set(j, dataconverter.process(FixTypes.ENTITY, (NBTTagCompound)nbttaglist.get(j), i));
                  }
               }

               if (nbttagcompound1.hasKey("TileEntities", 9)) {
                  NBTTagList nbttaglist = nbttagcompound1.getTagList("TileEntities", 10);

                  for(int j = 0; j < nbttaglist.tagCount(); ++j) {
                     nbttaglist.set(j, dataconverter.process(FixTypes.BLOCK_ENTITY, (NBTTagCompound)nbttaglist.get(j), i));
                  }
               }
            }

            return nbttagcompound;
         }
      });
   }

   private void writeChunkToNBT(Chunk chunk, World world, NBTTagCompound nbttagcompound) {
      nbttagcompound.setInteger("xPos", chunk.xPosition);
      nbttagcompound.setInteger("zPos", chunk.zPosition);
      nbttagcompound.setLong("LastUpdate", world.getTotalWorldTime());
      nbttagcompound.setIntArray("HeightMap", chunk.getHeightMap());
      nbttagcompound.setBoolean("TerrainPopulated", chunk.isTerrainPopulated());
      nbttagcompound.setBoolean("LightPopulated", chunk.isLightPopulated());
      nbttagcompound.setLong("InhabitedTime", chunk.getInhabitedTime());
      ExtendedBlockStorage[] achunksection = chunk.getBlockStorageArray();
      NBTTagList nbttaglist = new NBTTagList();
      boolean flag = !world.provider.hasNoSky();

      for(ExtendedBlockStorage chunksection : achunksection) {
         if (chunksection != Chunk.NULL_BLOCK_STORAGE) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            nbttagcompound1.setByte("Y", (byte)(chunksection.getYLocation() >> 4 & 255));
            byte[] abyte = new byte[4096];
            NibbleArray nibblearray = new NibbleArray();
            NibbleArray nibblearray1 = chunksection.getData().getDataForNBT(abyte, nibblearray);
            nbttagcompound1.setByteArray("Blocks", abyte);
            nbttagcompound1.setByteArray("Data", nibblearray.getData());
            if (nibblearray1 != null) {
               nbttagcompound1.setByteArray("Add", nibblearray1.getData());
            }

            nbttagcompound1.setByteArray("BlockLight", chunksection.getBlocklightArray().getData());
            if (flag) {
               nbttagcompound1.setByteArray("SkyLight", chunksection.getSkylightArray().getData());
            } else {
               nbttagcompound1.setByteArray("SkyLight", new byte[chunksection.getBlocklightArray().getData().length]);
            }

            nbttaglist.appendTag(nbttagcompound1);
         }
      }

      nbttagcompound.setTag("Sections", nbttaglist);
      nbttagcompound.setByteArray("Biomes", chunk.getBiomeArray());
      chunk.setHasEntities(false);
      NBTTagList nbttaglist1 = new NBTTagList();

      for(int var22 = 0; var22 < chunk.getEntityLists().length; ++var22) {
         for(Entity entity : chunk.getEntityLists()[var22]) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            if (entity.writeToNBTOptional(nbttagcompound1)) {
               chunk.setHasEntities(true);
               nbttaglist1.appendTag(nbttagcompound1);
            }
         }
      }

      nbttagcompound.setTag("Entities", nbttaglist1);
      NBTTagList nbttaglist2 = new NBTTagList();

      for(TileEntity tileentity : chunk.getTileEntityMap().values()) {
         NBTTagCompound nbttagcompound1 = tileentity.writeToNBT(new NBTTagCompound());
         nbttaglist2.appendTag(nbttagcompound1);
      }

      nbttagcompound.setTag("TileEntities", nbttaglist2);
      List list = world.getPendingBlockUpdates(chunk, false);
      if (list != null) {
         long k = world.getTotalWorldTime();
         NBTTagList nbttaglist3 = new NBTTagList();

         for(NextTickListEntry nextticklistentry : list) {
            NBTTagCompound nbttagcompound2 = new NBTTagCompound();
            ResourceLocation minecraftkey = (ResourceLocation)Block.REGISTRY.getNameForObject(nextticklistentry.getBlock());
            nbttagcompound2.setString("i", minecraftkey == null ? "" : minecraftkey.toString());
            nbttagcompound2.setInteger("x", nextticklistentry.position.getX());
            nbttagcompound2.setInteger("y", nextticklistentry.position.getY());
            nbttagcompound2.setInteger("z", nextticklistentry.position.getZ());
            nbttagcompound2.setInteger("t", (int)(nextticklistentry.scheduledTime - k));
            nbttagcompound2.setInteger("p", nextticklistentry.priority);
            nbttaglist3.appendTag(nbttagcompound2);
         }

         nbttagcompound.setTag("TileTicks", nbttaglist3);
      }

   }

   private Chunk readChunkFromNBT(World world, NBTTagCompound nbttagcompound) {
      int i = nbttagcompound.getInteger("xPos");
      int j = nbttagcompound.getInteger("zPos");
      Chunk chunk = new Chunk(world, i, j);
      chunk.setHeightMap(nbttagcompound.getIntArray("HeightMap"));
      chunk.setTerrainPopulated(nbttagcompound.getBoolean("TerrainPopulated"));
      chunk.setLightPopulated(nbttagcompound.getBoolean("LightPopulated"));
      chunk.setInhabitedTime(nbttagcompound.getLong("InhabitedTime"));
      NBTTagList nbttaglist = nbttagcompound.getTagList("Sections", 10);
      ExtendedBlockStorage[] achunksection = new ExtendedBlockStorage[16];
      boolean flag1 = !world.provider.hasNoSky();

      for(int k = 0; k < nbttaglist.tagCount(); ++k) {
         NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(k);
         byte b0 = nbttagcompound1.getByte("Y");
         ExtendedBlockStorage chunksection = new ExtendedBlockStorage(b0 << 4, flag1);
         byte[] abyte = nbttagcompound1.getByteArray("Blocks");
         NibbleArray nibblearray = new NibbleArray(nbttagcompound1.getByteArray("Data"));
         NibbleArray nibblearray1 = nbttagcompound1.hasKey("Add", 7) ? new NibbleArray(nbttagcompound1.getByteArray("Add")) : null;
         chunksection.getData().setDataFromNBT(abyte, nibblearray, nibblearray1);
         chunksection.setBlocklightArray(new NibbleArray(nbttagcompound1.getByteArray("BlockLight")));
         if (flag1) {
            chunksection.setSkylightArray(new NibbleArray(nbttagcompound1.getByteArray("SkyLight")));
         }

         chunksection.removeInvalidBlocks();
         achunksection[b0] = chunksection;
      }

      chunk.setStorageArrays(achunksection);
      if (nbttagcompound.hasKey("Biomes", 7)) {
         chunk.setBiomeArray(nbttagcompound.getByteArray("Biomes"));
      }

      return chunk;
   }

   public void loadEntities(Chunk chunk, NBTTagCompound nbttagcompound, World world) {
      NBTTagList nbttaglist1 = nbttagcompound.getTagList("Entities", 10);
      if (nbttaglist1 != null) {
         for(int l = 0; l < nbttaglist1.tagCount(); ++l) {
            NBTTagCompound nbttagcompound2 = nbttaglist1.getCompoundTagAt(l);
            readChunkEntity(nbttagcompound2, world, chunk);
            chunk.setHasEntities(true);
         }
      }

      NBTTagList nbttaglist2 = nbttagcompound.getTagList("TileEntities", 10);
      if (nbttaglist2 != null) {
         for(int i1 = 0; i1 < nbttaglist2.tagCount(); ++i1) {
            NBTTagCompound nbttagcompound3 = nbttaglist2.getCompoundTagAt(i1);
            TileEntity tileentity = TileEntity.create(world, nbttagcompound3);
            if (tileentity != null) {
               chunk.addTileEntity(tileentity);
            }
         }
      }

      if (nbttagcompound.hasKey("TileTicks", 9)) {
         NBTTagList nbttaglist3 = nbttagcompound.getTagList("TileTicks", 10);
         if (nbttaglist3 != null) {
            for(int j1 = 0; j1 < nbttaglist3.tagCount(); ++j1) {
               NBTTagCompound nbttagcompound4 = nbttaglist3.getCompoundTagAt(j1);
               Block block;
               if (nbttagcompound4.hasKey("i", 8)) {
                  block = Block.getBlockFromName(nbttagcompound4.getString("i"));
               } else {
                  block = Block.getBlockById(nbttagcompound4.getInteger("i"));
               }

               world.scheduleBlockUpdate(new BlockPos(nbttagcompound4.getInteger("x"), nbttagcompound4.getInteger("y"), nbttagcompound4.getInteger("z")), block, nbttagcompound4.getInteger("t"), nbttagcompound4.getInteger("p"));
            }
         }
      }

   }

   @Nullable
   public static Entity readChunkEntity(NBTTagCompound nbttagcompound, World world, Chunk chunk) {
      Entity entity = createEntityFromNBT(nbttagcompound, world);
      if (entity == null) {
         return null;
      } else {
         chunk.addEntity(entity);
         if (nbttagcompound.hasKey("Passengers", 9)) {
            NBTTagList nbttaglist = nbttagcompound.getTagList("Passengers", 10);

            for(int i = 0; i < nbttaglist.tagCount(); ++i) {
               Entity entity1 = readChunkEntity(nbttaglist.getCompoundTagAt(i), world, chunk);
               if (entity1 != null) {
                  entity1.startRiding(entity, true);
               }
            }
         }

         return entity;
      }
   }

   @Nullable
   public static Entity readWorldEntityPos(NBTTagCompound nbttagcompound, World world, double d0, double d1, double d2, boolean flag) {
      return spawnEntity(nbttagcompound, world, d0, d1, d2, flag, SpawnReason.DEFAULT);
   }

   public static Entity spawnEntity(NBTTagCompound nbttagcompound, World world, double d0, double d1, double d2, boolean flag, SpawnReason spawnReason) {
      Entity entity = createEntityFromNBT(nbttagcompound, world);
      if (entity == null) {
         return null;
      } else {
         entity.setLocationAndAngles(d0, d1, d2, entity.rotationYaw, entity.rotationPitch);
         if (flag && !world.addEntity(entity, spawnReason)) {
            return null;
         } else {
            if (nbttagcompound.hasKey("Passengers", 9)) {
               NBTTagList nbttaglist = nbttagcompound.getTagList("Passengers", 10);

               for(int i = 0; i < nbttaglist.tagCount(); ++i) {
                  Entity entity1 = readWorldEntityPos(nbttaglist.getCompoundTagAt(i), world, d0, d1, d2, flag);
                  if (entity1 != null) {
                     entity1.startRiding(entity, true);
                  }
               }
            }

            return entity;
         }
      }
   }

   @Nullable
   protected static Entity createEntityFromNBT(NBTTagCompound nbttagcompound, World world) {
      try {
         return EntityList.createEntityFromNBT(nbttagcompound, world);
      } catch (RuntimeException var2) {
         return null;
      }
   }

   public static void spawnEntity(Entity entity, World world) {
      a(entity, world, SpawnReason.DEFAULT);
   }

   public static void a(Entity entity, World world, SpawnReason reason) {
      if (world.addEntity(entity, reason) && entity.isBeingRidden()) {
         for(Entity entity1 : entity.getPassengers()) {
            spawnEntity(entity1, world);
         }
      }

   }

   @Nullable
   public static Entity readWorldEntity(NBTTagCompound nbttagcompound, World world, boolean flag) {
      Entity entity = createEntityFromNBT(nbttagcompound, world);
      if (entity == null) {
         return null;
      } else if (flag && !world.spawnEntity(entity)) {
         return null;
      } else {
         if (nbttagcompound.hasKey("Passengers", 9)) {
            NBTTagList nbttaglist = nbttagcompound.getTagList("Passengers", 10);

            for(int i = 0; i < nbttaglist.tagCount(); ++i) {
               Entity entity1 = readWorldEntity(nbttaglist.getCompoundTagAt(i), world, flag);
               if (entity1 != null) {
                  entity1.startRiding(entity, true);
               }
            }
         }

         return entity;
      }
   }
}
