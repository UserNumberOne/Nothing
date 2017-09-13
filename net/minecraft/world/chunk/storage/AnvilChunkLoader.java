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

   public AnvilChunkLoader(File var1, DataFixer var2) {
      this.chunkSaveLocation = var1;
      this.dataFixer = var2;
   }

   public boolean chunkExists(World var1, int var2, int var3) {
      ChunkPos var4 = new ChunkPos(var2, var3);
      return this.pendingAnvilChunksCoordinates.contains(var4) && this.chunksToRemove.containsKey(var4) ? true : RegionFileCache.createOrLoadRegionFile(this.chunkSaveLocation, var2, var3).chunkExists(var2 & 31, var3 & 31);
   }

   @Nullable
   public Chunk loadChunk(World var1, int var2, int var3) throws IOException {
      Object[] var4 = this.loadChunk(var1, var2, var3);
      if (var4 != null) {
         Chunk var5 = (Chunk)var4[0];
         NBTTagCompound var6 = (NBTTagCompound)var4[1];
         this.loadEntities(var5, var6.getCompoundTag("Level"), var1);
         return var5;
      } else {
         return null;
      }
   }

   public Object[] loadChunk(World var1, int var2, int var3) throws IOException {
      ChunkPos var4 = new ChunkPos(var2, var3);
      NBTTagCompound var5 = (NBTTagCompound)this.chunksToRemove.get(var4);
      if (var5 == null) {
         var5 = RegionFileCache.c(this.chunkSaveLocation, var2, var3);
         if (var5 == null) {
            return null;
         }

         var5 = this.dataFixer.process(FixTypes.CHUNK, var5);
      }

      return this.a(var1, var2, var3, var5);
   }

   protected Object[] a(World var1, int var2, int var3, NBTTagCompound var4) {
      if (!var4.hasKey("Level", 10)) {
         LOGGER.error("Chunk file at {},{} is missing level data, skipping", new Object[]{var2, var3});
         return null;
      } else {
         NBTTagCompound var5 = var4.getCompoundTag("Level");
         if (!var5.hasKey("Sections", 9)) {
            LOGGER.error("Chunk file at {},{} is missing block data, skipping", new Object[]{var2, var3});
            return null;
         } else {
            Chunk var6 = this.readChunkFromNBT(var1, var5);
            if (!var6.isAtLocation(var2, var3)) {
               LOGGER.error("Chunk file at {},{} is in the wrong location; relocating. (Expected {}, {}, got {}, {})", new Object[]{var2, var3, var2, var3, var6.xPosition, var6.zPosition});
               var5.setInteger("xPos", var2);
               var5.setInteger("zPos", var3);
               NBTTagList var7 = var4.getCompoundTag("Level").getTagList("TileEntities", 10);
               if (var7 != null) {
                  for(int var8 = 0; var8 < var7.tagCount(); ++var8) {
                     NBTTagCompound var9 = var7.getCompoundTagAt(var8);
                     int var10 = var9.getInteger("x") - var6.xPosition * 16;
                     int var11 = var9.getInteger("z") - var6.zPosition * 16;
                     var9.setInteger("x", var2 * 16 + var10);
                     var9.setInteger("z", var3 * 16 + var11);
                  }
               }

               var6 = this.readChunkFromNBT(var1, var5);
            }

            Object[] var12 = new Object[]{var6, var4};
            return var12;
         }
      }
   }

   public void saveChunk(World var1, Chunk var2) throws IOException, MinecraftException {
      var1.checkSessionLock();

      try {
         NBTTagCompound var3 = new NBTTagCompound();
         NBTTagCompound var4 = new NBTTagCompound();
         var3.setTag("Level", var4);
         var3.setInteger("DataVersion", 512);
         this.writeChunkToNBT(var2, var1, var4);
         this.addChunkToPending(var2.getChunkCoordIntPair(), var3);
      } catch (Exception var5) {
         LOGGER.error("Failed to save chunk", var5);
      }

   }

   protected void addChunkToPending(ChunkPos var1, NBTTagCompound var2) {
      if (!this.pendingAnvilChunksCoordinates.contains(var1)) {
         this.chunksToRemove.put(var1, var2);
      }

      ThreadedFileIOBase.getThreadedIOInstance().queueIO(this);
   }

   public boolean writeNextIO() {
      Iterator var1 = this.chunksToRemove.entrySet().iterator();
      if (!var1.hasNext()) {
         if (this.savingExtraData) {
            LOGGER.info("ThreadedAnvilChunkStorage ({}): All chunks are saved", new Object[]{this.chunkSaveLocation.getName()});
         }

         return false;
      } else {
         Entry var2 = (Entry)var1.next();
         var1.remove();
         ChunkPos var3 = (ChunkPos)var2.getKey();

         boolean var6;
         try {
            this.pendingAnvilChunksCoordinates.add(var3);
            NBTTagCompound var4 = (NBTTagCompound)var2.getValue();
            if (var4 != null) {
               try {
                  this.writeChunkData(var3, var4);
               } catch (Exception var10) {
                  LOGGER.error("Failed to save chunk", var10);
               }
            }

            var6 = true;
         } finally {
            this.pendingAnvilChunksCoordinates.remove(var3);
         }

         return var6;
      }
   }

   private void writeChunkData(ChunkPos var1, NBTTagCompound var2) throws IOException {
      RegionFileCache.d(this.chunkSaveLocation, var1.chunkXPos, var1.chunkZPos, var2);
   }

   public void saveExtraChunkData(World var1, Chunk var2) throws IOException {
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

   public static void registerFixes(DataFixer var0) {
      var0.registerWalker(FixTypes.CHUNK, new IDataWalker() {
         public NBTTagCompound process(IDataFixer var1, NBTTagCompound var2, int var3) {
            if (var2.hasKey("Level", 10)) {
               NBTTagCompound var4 = var2.getCompoundTag("Level");
               if (var4.hasKey("Entities", 9)) {
                  NBTTagList var5 = var4.getTagList("Entities", 10);

                  for(int var6 = 0; var6 < var5.tagCount(); ++var6) {
                     var5.set(var6, var1.process(FixTypes.ENTITY, (NBTTagCompound)var5.get(var6), var3));
                  }
               }

               if (var4.hasKey("TileEntities", 9)) {
                  NBTTagList var7 = var4.getTagList("TileEntities", 10);

                  for(int var8 = 0; var8 < var7.tagCount(); ++var8) {
                     var7.set(var8, var1.process(FixTypes.BLOCK_ENTITY, (NBTTagCompound)var7.get(var8), var3));
                  }
               }
            }

            return var2;
         }
      });
   }

   private void writeChunkToNBT(Chunk var1, World var2, NBTTagCompound var3) {
      var3.setInteger("xPos", var1.xPosition);
      var3.setInteger("zPos", var1.zPosition);
      var3.setLong("LastUpdate", var2.getTotalWorldTime());
      var3.setIntArray("HeightMap", var1.getHeightMap());
      var3.setBoolean("TerrainPopulated", var1.isTerrainPopulated());
      var3.setBoolean("LightPopulated", var1.isLightPopulated());
      var3.setLong("InhabitedTime", var1.getInhabitedTime());
      ExtendedBlockStorage[] var4 = var1.getBlockStorageArray();
      NBTTagList var5 = new NBTTagList();
      boolean var6 = !var2.provider.hasNoSky();

      for(ExtendedBlockStorage var10 : var4) {
         if (var10 != Chunk.NULL_BLOCK_STORAGE) {
            NBTTagCompound var11 = new NBTTagCompound();
            var11.setByte("Y", (byte)(var10.getYLocation() >> 4 & 255));
            byte[] var12 = new byte[4096];
            NibbleArray var13 = new NibbleArray();
            NibbleArray var14 = var10.getData().getDataForNBT(var12, var13);
            var11.setByteArray("Blocks", var12);
            var11.setByteArray("Data", var13.getData());
            if (var14 != null) {
               var11.setByteArray("Add", var14.getData());
            }

            var11.setByteArray("BlockLight", var10.getBlocklightArray().getData());
            if (var6) {
               var11.setByteArray("SkyLight", var10.getSkylightArray().getData());
            } else {
               var11.setByteArray("SkyLight", new byte[var10.getBlocklightArray().getData().length]);
            }

            var5.appendTag(var11);
         }
      }

      var3.setTag("Sections", var5);
      var3.setByteArray("Biomes", var1.getBiomeArray());
      var1.setHasEntities(false);
      NBTTagList var23 = new NBTTagList();

      for(int var22 = 0; var22 < var1.getEntityLists().length; ++var22) {
         for(Entity var28 : var1.getEntityLists()[var22]) {
            NBTTagCompound var26 = new NBTTagCompound();
            if (var28.writeToNBTOptional(var26)) {
               var1.setHasEntities(true);
               var23.appendTag(var26);
            }
         }
      }

      var3.setTag("Entities", var23);
      NBTTagList var29 = new NBTTagList();

      for(TileEntity var30 : var1.getTileEntityMap().values()) {
         NBTTagCompound var27 = var30.writeToNBT(new NBTTagCompound());
         var29.appendTag(var27);
      }

      var3.setTag("TileEntities", var29);
      List var31 = var2.getPendingBlockUpdates(var1, false);
      if (var31 != null) {
         long var15 = var2.getTotalWorldTime();
         NBTTagList var17 = new NBTTagList();

         for(NextTickListEntry var19 : var31) {
            NBTTagCompound var20 = new NBTTagCompound();
            ResourceLocation var21 = (ResourceLocation)Block.REGISTRY.getNameForObject(var19.getBlock());
            var20.setString("i", var21 == null ? "" : var21.toString());
            var20.setInteger("x", var19.position.getX());
            var20.setInteger("y", var19.position.getY());
            var20.setInteger("z", var19.position.getZ());
            var20.setInteger("t", (int)(var19.scheduledTime - var15));
            var20.setInteger("p", var19.priority);
            var17.appendTag(var20);
         }

         var3.setTag("TileTicks", var17);
      }

   }

   private Chunk readChunkFromNBT(World var1, NBTTagCompound var2) {
      int var3 = var2.getInteger("xPos");
      int var4 = var2.getInteger("zPos");
      Chunk var5 = new Chunk(var1, var3, var4);
      var5.setHeightMap(var2.getIntArray("HeightMap"));
      var5.setTerrainPopulated(var2.getBoolean("TerrainPopulated"));
      var5.setLightPopulated(var2.getBoolean("LightPopulated"));
      var5.setInhabitedTime(var2.getLong("InhabitedTime"));
      NBTTagList var6 = var2.getTagList("Sections", 10);
      ExtendedBlockStorage[] var7 = new ExtendedBlockStorage[16];
      boolean var8 = !var1.provider.hasNoSky();

      for(int var9 = 0; var9 < var6.tagCount(); ++var9) {
         NBTTagCompound var10 = var6.getCompoundTagAt(var9);
         byte var11 = var10.getByte("Y");
         ExtendedBlockStorage var12 = new ExtendedBlockStorage(var11 << 4, var8);
         byte[] var13 = var10.getByteArray("Blocks");
         NibbleArray var14 = new NibbleArray(var10.getByteArray("Data"));
         NibbleArray var15 = var10.hasKey("Add", 7) ? new NibbleArray(var10.getByteArray("Add")) : null;
         var12.getData().setDataFromNBT(var13, var14, var15);
         var12.setBlocklightArray(new NibbleArray(var10.getByteArray("BlockLight")));
         if (var8) {
            var12.setSkylightArray(new NibbleArray(var10.getByteArray("SkyLight")));
         }

         var12.removeInvalidBlocks();
         var7[var11] = var12;
      }

      var5.setStorageArrays(var7);
      if (var2.hasKey("Biomes", 7)) {
         var5.setBiomeArray(var2.getByteArray("Biomes"));
      }

      return var5;
   }

   public void loadEntities(Chunk var1, NBTTagCompound var2, World var3) {
      NBTTagList var4 = var2.getTagList("Entities", 10);
      if (var4 != null) {
         for(int var5 = 0; var5 < var4.tagCount(); ++var5) {
            NBTTagCompound var6 = var4.getCompoundTagAt(var5);
            readChunkEntity(var6, var3, var1);
            var1.setHasEntities(true);
         }
      }

      NBTTagList var10 = var2.getTagList("TileEntities", 10);
      if (var10 != null) {
         for(int var11 = 0; var11 < var10.tagCount(); ++var11) {
            NBTTagCompound var7 = var10.getCompoundTagAt(var11);
            TileEntity var8 = TileEntity.create(var3, var7);
            if (var8 != null) {
               var1.addTileEntity(var8);
            }
         }
      }

      if (var2.hasKey("TileTicks", 9)) {
         NBTTagList var12 = var2.getTagList("TileTicks", 10);
         if (var12 != null) {
            for(int var13 = 0; var13 < var12.tagCount(); ++var13) {
               NBTTagCompound var14 = var12.getCompoundTagAt(var13);
               Block var9;
               if (var14.hasKey("i", 8)) {
                  var9 = Block.getBlockFromName(var14.getString("i"));
               } else {
                  var9 = Block.getBlockById(var14.getInteger("i"));
               }

               var3.scheduleBlockUpdate(new BlockPos(var14.getInteger("x"), var14.getInteger("y"), var14.getInteger("z")), var9, var14.getInteger("t"), var14.getInteger("p"));
            }
         }
      }

   }

   @Nullable
   public static Entity readChunkEntity(NBTTagCompound var0, World var1, Chunk var2) {
      Entity var3 = createEntityFromNBT(var0, var1);
      if (var3 == null) {
         return null;
      } else {
         var2.addEntity(var3);
         if (var0.hasKey("Passengers", 9)) {
            NBTTagList var4 = var0.getTagList("Passengers", 10);

            for(int var5 = 0; var5 < var4.tagCount(); ++var5) {
               Entity var6 = readChunkEntity(var4.getCompoundTagAt(var5), var1, var2);
               if (var6 != null) {
                  var6.startRiding(var3, true);
               }
            }
         }

         return var3;
      }
   }

   @Nullable
   public static Entity readWorldEntityPos(NBTTagCompound var0, World var1, double var2, double var4, double var6, boolean var8) {
      return spawnEntity(var0, var1, var2, var4, var6, var8, SpawnReason.DEFAULT);
   }

   public static Entity spawnEntity(NBTTagCompound var0, World var1, double var2, double var4, double var6, boolean var8, SpawnReason var9) {
      Entity var10 = createEntityFromNBT(var0, var1);
      if (var10 == null) {
         return null;
      } else {
         var10.setLocationAndAngles(var2, var4, var6, var10.rotationYaw, var10.rotationPitch);
         if (var8 && !var1.addEntity(var10, var9)) {
            return null;
         } else {
            if (var0.hasKey("Passengers", 9)) {
               NBTTagList var11 = var0.getTagList("Passengers", 10);

               for(int var12 = 0; var12 < var11.tagCount(); ++var12) {
                  Entity var13 = readWorldEntityPos(var11.getCompoundTagAt(var12), var1, var2, var4, var6, var8);
                  if (var13 != null) {
                     var13.startRiding(var10, true);
                  }
               }
            }

            return var10;
         }
      }
   }

   @Nullable
   protected static Entity createEntityFromNBT(NBTTagCompound var0, World var1) {
      try {
         return EntityList.createEntityFromNBT(var0, var1);
      } catch (RuntimeException var2) {
         return null;
      }
   }

   public static void spawnEntity(Entity var0, World var1) {
      a(var0, var1, SpawnReason.DEFAULT);
   }

   public static void a(Entity var0, World var1, SpawnReason var2) {
      if (var1.addEntity(var0, var2) && var0.isBeingRidden()) {
         for(Entity var4 : var0.getPassengers()) {
            spawnEntity(var4, var1);
         }
      }

   }

   @Nullable
   public static Entity readWorldEntity(NBTTagCompound var0, World var1, boolean var2) {
      Entity var3 = createEntityFromNBT(var0, var1);
      if (var3 == null) {
         return null;
      } else if (var2 && !var1.spawnEntity(var3)) {
         return null;
      } else {
         if (var0.hasKey("Passengers", 9)) {
            NBTTagList var4 = var0.getTagList("Passengers", 10);

            for(int var5 = 0; var5 < var4.tagCount(); ++var5) {
               Entity var6 = readWorldEntity(var4.getCompoundTagAt(var5), var1, var2);
               if (var6 != null) {
                  var6.startRiding(var3, true);
               }
            }
         }

         return var3;
      }
   }
}
