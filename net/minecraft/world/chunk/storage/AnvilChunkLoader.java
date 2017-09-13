package net.minecraft.world.chunk.storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.CompressedStreamTools;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkDataEvent.Save;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnvilChunkLoader implements IChunkLoader, IThreadedFileIO {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Map chunksToRemove = new ConcurrentHashMap();
   private final Set pendingAnvilChunksCoordinates = Collections.newSetFromMap(new ConcurrentHashMap());
   public final File chunkSaveLocation;
   private final DataFixer dataFixer;
   private boolean savingExtraData;

   public AnvilChunkLoader(File var1, DataFixer var2) {
      this.chunkSaveLocation = var1;
      this.dataFixer = var2;
   }

   public boolean chunkExists(World var1, int var2, int var3) {
      ChunkPos var4 = new ChunkPos(var2, var3);
      if (this.pendingAnvilChunksCoordinates.contains(var4)) {
         for(ChunkPos var6 : this.chunksToRemove.keySet()) {
            if (var6.equals(var4)) {
               return true;
            }
         }
      }

      return RegionFileCache.createOrLoadRegionFile(this.chunkSaveLocation, var2, var3).chunkExists(var2 & 31, var3 & 31);
   }

   @Nullable
   public Chunk loadChunk(World var1, int var2, int var3) throws IOException {
      Object[] var4 = this.loadChunk__Async(var1, var2, var3);
      if (var4 != null) {
         Chunk var5 = (Chunk)var4[0];
         NBTTagCompound var6 = (NBTTagCompound)var4[1];
         this.loadEntities(var1, var6.getCompoundTag("Level"), var5);
         return var5;
      } else {
         return null;
      }
   }

   public Object[] loadChunk__Async(World var1, int var2, int var3) throws IOException {
      ChunkPos var4 = new ChunkPos(var2, var3);
      NBTTagCompound var5 = (NBTTagCompound)this.chunksToRemove.get(var4);
      if (var5 == null) {
         DataInputStream var6 = RegionFileCache.getChunkInputStream(this.chunkSaveLocation, var2, var3);
         if (var6 == null) {
            return null;
         }

         var5 = this.dataFixer.process(FixTypes.CHUNK, CompressedStreamTools.read(var6));
      }

      return this.checkedReadChunkFromNBT__Async(var1, var2, var3, var5);
   }

   protected Chunk checkedReadChunkFromNBT(World var1, int var2, int var3, NBTTagCompound var4) {
      Object[] var5 = this.checkedReadChunkFromNBT__Async(var1, var2, var3, var4);
      return var5 != null ? (Chunk)var5[0] : null;
   }

   protected Object[] checkedReadChunkFromNBT__Async(World var1, int var2, int var3, NBTTagCompound var4) {
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
               NBTTagList var7 = var5.getTagList("TileEntities", 10);
               if (var7 != null) {
                  for(int var8 = 0; var8 < var7.tagCount(); ++var8) {
                     NBTTagCompound var9 = var7.getCompoundTagAt(var8);
                     var9.setInteger("x", var2 * 16 + (var9.getInteger("x") - var6.xPosition * 16));
                     var9.setInteger("z", var3 * 16 + (var9.getInteger("z") - var6.zPosition * 16));
                  }
               }

               var6 = this.readChunkFromNBT(var1, var5);
            }

            Object[] var10 = new Object[]{var6, var4};
            return var10;
         }
      }
   }

   public void saveChunk(World var1, Chunk var2) throws MinecraftException, IOException {
      var1.checkSessionLock();

      try {
         NBTTagCompound var3 = new NBTTagCompound();
         NBTTagCompound var4 = new NBTTagCompound();
         var3.setTag("Level", var4);
         var3.setInteger("DataVersion", 512);
         this.writeChunkToNBT(var2, var1, var4);
         MinecraftForge.EVENT_BUS.post(new Save(var2, var3));
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
      if (this.chunksToRemove.isEmpty()) {
         if (this.savingExtraData) {
            LOGGER.info("ThreadedAnvilChunkStorage ({}): All chunks are saved", new Object[]{this.chunkSaveLocation.getName()});
         }

         return false;
      } else {
         ChunkPos var1 = (ChunkPos)this.chunksToRemove.keySet().iterator().next();

         boolean var2;
         try {
            this.pendingAnvilChunksCoordinates.add(var1);
            NBTTagCompound var3 = (NBTTagCompound)this.chunksToRemove.remove(var1);
            if (var3 != null) {
               try {
                  this.writeChunkData(var1, var3);
               } catch (Exception var8) {
                  LOGGER.error("Failed to save chunk", var8);
               }
            }

            var2 = true;
         } finally {
            this.pendingAnvilChunksCoordinates.remove(var1);
         }

         return var2;
      }
   }

   private void writeChunkData(ChunkPos var1, NBTTagCompound var2) throws IOException {
      DataOutputStream var3 = RegionFileCache.getChunkOutputStream(this.chunkSaveLocation, var1.chunkXPos, var1.chunkZPos);
      CompressedStreamTools.write(var2, var3);
      var3.close();
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
      NBTTagList var19 = new NBTTagList();

      for(int var20 = 0; var20 < var1.getEntityLists().length; ++var20) {
         for(Entity var25 : var1.getEntityLists()[var20]) {
            NBTTagCompound var28 = new NBTTagCompound();
            if (var25.writeToNBTOptional(var28)) {
               try {
                  var1.setHasEntities(true);
                  var19.appendTag(var28);
               } catch (Exception var18) {
                  FMLLog.log(Level.ERROR, var18, "An Entity type %s has thrown an exception trying to write state. It will not persist. Report this to the mod author", new Object[]{var25.getClass().getName()});
               }
            }
         }
      }

      var3.setTag("Entities", var19);
      NBTTagList var21 = new NBTTagList();

      for(TileEntity var26 : var1.getTileEntityMap().values()) {
         try {
            NBTTagCompound var29 = var26.writeToNBT(new NBTTagCompound());
            var21.appendTag(var29);
         } catch (Exception var17) {
            FMLLog.log(Level.ERROR, var17, "A TileEntity type %s has throw an exception trying to write state. It will not persist. Report this to the mod author", new Object[]{var26.getClass().getName()});
         }
      }

      var3.setTag("TileEntities", var21);
      List var24 = var2.getPendingBlockUpdates(var1, false);
      if (var24 != null) {
         long var27 = var2.getTotalWorldTime();
         NBTTagList var30 = new NBTTagList();

         for(NextTickListEntry var32 : var24) {
            NBTTagCompound var15 = new NBTTagCompound();
            ResourceLocation var16 = (ResourceLocation)Block.REGISTRY.getNameForObject(var32.getBlock());
            var15.setString("i", var16 == null ? "" : var16.toString());
            var15.setInteger("x", var32.position.getX());
            var15.setInteger("y", var32.position.getY());
            var15.setInteger("z", var32.position.getZ());
            var15.setInteger("t", (int)(var32.scheduledTime - var27));
            var15.setInteger("p", var32.priority);
            var30.appendTag(var15);
         }

         var3.setTag("TileTicks", var30);
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
      boolean var7 = true;
      ExtendedBlockStorage[] var8 = new ExtendedBlockStorage[16];
      boolean var9 = !var1.provider.hasNoSky();

      for(int var10 = 0; var10 < var6.tagCount(); ++var10) {
         NBTTagCompound var11 = var6.getCompoundTagAt(var10);
         byte var12 = var11.getByte("Y");
         ExtendedBlockStorage var13 = new ExtendedBlockStorage(var12 << 4, var9);
         byte[] var14 = var11.getByteArray("Blocks");
         NibbleArray var15 = new NibbleArray(var11.getByteArray("Data"));
         NibbleArray var16 = var11.hasKey("Add", 7) ? new NibbleArray(var11.getByteArray("Add")) : null;
         var13.getData().setDataFromNBT(var14, var15, var16);
         var13.setBlocklightArray(new NibbleArray(var11.getByteArray("BlockLight")));
         if (var9) {
            var13.setSkylightArray(new NibbleArray(var11.getByteArray("SkyLight")));
         }

         var13.removeInvalidBlocks();
         var8[var12] = var13;
      }

      var5.setStorageArrays(var8);
      if (var2.hasKey("Biomes", 7)) {
         var5.setBiomeArray(var2.getByteArray("Biomes"));
      }

      return var5;
   }

   public void loadEntities(World var1, NBTTagCompound var2, Chunk var3) {
      NBTTagList var4 = var2.getTagList("Entities", 10);
      if (var4 != null) {
         for(int var5 = 0; var5 < var4.tagCount(); ++var5) {
            NBTTagCompound var6 = var4.getCompoundTagAt(var5);
            readChunkEntity(var6, var1, var3);
            var3.setHasEntities(true);
         }
      }

      NBTTagList var10 = var2.getTagList("TileEntities", 10);
      if (var10 != null) {
         for(int var11 = 0; var11 < var10.tagCount(); ++var11) {
            NBTTagCompound var7 = var10.getCompoundTagAt(var11);
            TileEntity var8 = TileEntity.create(var1, var7);
            if (var8 != null) {
               var3.addTileEntity(var8);
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

               var1.scheduleBlockUpdate(new BlockPos(var14.getInteger("x"), var14.getInteger("y"), var14.getInteger("z")), var9, var14.getInteger("t"), var14.getInteger("p"));
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
      Entity var9 = createEntityFromNBT(var0, var1);
      if (var9 == null) {
         return null;
      } else {
         var9.setLocationAndAngles(var2, var4, var6, var9.rotationYaw, var9.rotationPitch);
         if (var8 && !var1.spawnEntity(var9)) {
            return null;
         } else {
            if (var0.hasKey("Passengers", 9)) {
               NBTTagList var10 = var0.getTagList("Passengers", 10);

               for(int var11 = 0; var11 < var10.tagCount(); ++var11) {
                  Entity var12 = readWorldEntityPos(var10.getCompoundTagAt(var11), var1, var2, var4, var6, var8);
                  if (var12 != null) {
                     var12.startRiding(var9, true);
                  }
               }
            }

            return var9;
         }
      }
   }

   @Nullable
   protected static Entity createEntityFromNBT(NBTTagCompound var0, World var1) {
      try {
         return EntityList.createEntityFromNBT(var0, var1);
      } catch (RuntimeException var3) {
         return null;
      }
   }

   public static void spawnEntity(Entity var0, World var1) {
      if (var1.spawnEntity(var0) && var0.isBeingRidden()) {
         for(Entity var3 : var0.getPassengers()) {
            spawnEntity(var3, var1);
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
