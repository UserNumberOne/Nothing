package net.minecraft.world.chunk;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockSand;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderDebug;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_10_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_10_R1.util.CraftMagicNumbers;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.generator.BlockPopulator;

public class Chunk {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final ExtendedBlockStorage NULL_BLOCK_STORAGE = null;
   private final ExtendedBlockStorage[] storageArrays;
   private final byte[] blockBiomeArray;
   private final int[] precipitationHeightMap;
   private final boolean[] updateSkylightColumns;
   private boolean isChunkLoaded;
   public final World world;
   public final int[] heightMap;
   public final int xPosition;
   public final int zPosition;
   private boolean isGapLightingUpdated;
   public final Map chunkTileEntityMap;
   public final ClassInheritanceMultiMap[] entityLists;
   private boolean isTerrainPopulated;
   private boolean isLightPopulated;
   private boolean chunkTicked;
   private boolean isModified;
   private boolean hasEntities;
   private long lastSaveTime;
   private int heightMapMinimum;
   private long inhabitedTime;
   private int queuedLightChecks;
   private ConcurrentLinkedQueue tileEntityPosQueue;
   public boolean unloaded;
   private int neighbors;
   public long chunkKey;
   public org.bukkit.Chunk bukkitChunk;
   public boolean mustSave;

   public boolean areNeighborsLoaded(int radius) {
      switch(radius) {
      case 1:
         if ((this.neighbors & 473536) == 473536) {
            return true;
         }

         return false;
      case 2:
         if (this.neighbors == 33554431) {
            return true;
         }

         return false;
      default:
         throw new UnsupportedOperationException(String.valueOf(radius));
      }
   }

   public void setNeighborLoaded(int x, int z) {
      this.neighbors |= 1 << x * 5 + 12 + z;
   }

   public void setNeighborUnloaded(int x, int z) {
      this.neighbors &= ~(1 << x * 5 + 12 + z);
   }

   public Chunk(World world, int i, int j) {
      this.neighbors = 4096;
      this.storageArrays = new ExtendedBlockStorage[16];
      this.blockBiomeArray = new byte[256];
      this.precipitationHeightMap = new int[256];
      this.updateSkylightColumns = new boolean[256];
      this.chunkTileEntityMap = Maps.newHashMap();
      this.queuedLightChecks = 4096;
      this.tileEntityPosQueue = Queues.newConcurrentLinkedQueue();
      this.entityLists = new ClassInheritanceMultiMap[16];
      this.world = world;
      this.xPosition = i;
      this.zPosition = j;
      this.heightMap = new int[256];

      for(int k = 0; k < this.entityLists.length; ++k) {
         this.entityLists[k] = new ClassInheritanceMultiMap(Entity.class);
      }

      Arrays.fill(this.precipitationHeightMap, -999);
      Arrays.fill(this.blockBiomeArray, (byte)-1);
      this.bukkitChunk = new CraftChunk(this);
      this.chunkKey = ChunkPos.asLong(this.xPosition, this.zPosition);
   }

   public Chunk(World world, ChunkPrimer chunksnapshot, int i, int j) {
      this(world, i, j);
      boolean flag1 = !world.provider.hasNoSky();

      for(int k = 0; k < 16; ++k) {
         for(int l = 0; l < 16; ++l) {
            for(int i1 = 0; i1 < 256; ++i1) {
               IBlockState iblockdata = chunksnapshot.getBlockState(k, i1, l);
               if (iblockdata.getMaterial() != Material.AIR) {
                  int j1 = i1 >> 4;
                  if (this.storageArrays[j1] == NULL_BLOCK_STORAGE) {
                     this.storageArrays[j1] = new ExtendedBlockStorage(j1 << 4, flag1);
                  }

                  this.storageArrays[j1].set(k, i1 & 15, l, iblockdata);
               }
            }
         }
      }

   }

   public boolean isAtLocation(int i, int j) {
      return i == this.xPosition && j == this.zPosition;
   }

   public int getHeight(BlockPos blockposition) {
      return this.getHeightValue(blockposition.getX() & 15, blockposition.getZ() & 15);
   }

   public int getHeightValue(int i, int j) {
      return this.heightMap[j << 4 | i];
   }

   @Nullable
   private ExtendedBlockStorage getLastExtendedBlockStorage() {
      for(int i = this.storageArrays.length - 1; i >= 0; --i) {
         if (this.storageArrays[i] != NULL_BLOCK_STORAGE) {
            return this.storageArrays[i];
         }
      }

      return null;
   }

   public int getTopFilledSegment() {
      ExtendedBlockStorage chunksection = this.getLastExtendedBlockStorage();
      return chunksection == null ? 0 : chunksection.getYLocation();
   }

   public ExtendedBlockStorage[] getBlockStorageArray() {
      return this.storageArrays;
   }

   public void generateSkylightMap() {
      int i = this.getTopFilledSegment();
      this.heightMapMinimum = Integer.MAX_VALUE;

      for(int j = 0; j < 16; ++j) {
         for(int k = 0; k < 16; ++k) {
            this.precipitationHeightMap[j + (k << 4)] = -999;

            for(int l = i + 16; l > 0; --l) {
               if (this.getBlockLightOpacity(j, l - 1, k) != 0) {
                  this.heightMap[k << 4 | j] = l;
                  if (l < this.heightMapMinimum) {
                     this.heightMapMinimum = l;
                  }
                  break;
               }
            }

            if (!this.world.provider.hasNoSky()) {
               int var8 = 15;
               int i1 = i + 16 - 1;

               while(true) {
                  int j1 = this.getBlockLightOpacity(j, i1, k);
                  if (j1 == 0 && var8 != 15) {
                     j1 = 1;
                  }

                  var8 -= j1;
                  if (var8 > 0) {
                     ExtendedBlockStorage chunksection = this.storageArrays[i1 >> 4];
                     if (chunksection != NULL_BLOCK_STORAGE) {
                        chunksection.setExtSkylightValue(j, i1 & 15, k, var8);
                        this.world.notifyLightSet(new BlockPos((this.xPosition << 4) + j, i1, (this.zPosition << 4) + k));
                     }
                  }

                  --i1;
                  if (i1 <= 0 || var8 <= 0) {
                     break;
                  }
               }
            }
         }
      }

      this.isModified = true;
   }

   private void propagateSkylightOcclusion(int i, int j) {
      this.updateSkylightColumns[i + j * 16] = true;
      this.isGapLightingUpdated = true;
   }

   private void recheckGaps(boolean flag) {
      this.world.theProfiler.startSection("recheckGaps");
      if (this.world.isAreaLoaded(new BlockPos(this.xPosition * 16 + 8, 0, this.zPosition * 16 + 8), 16)) {
         for(int i = 0; i < 16; ++i) {
            for(int j = 0; j < 16; ++j) {
               if (this.updateSkylightColumns[i + j * 16]) {
                  this.updateSkylightColumns[i + j * 16] = false;
                  int k = this.getHeightValue(i, j);
                  int l = this.xPosition * 16 + i;
                  int i1 = this.zPosition * 16 + j;
                  int j1 = Integer.MAX_VALUE;

                  for(EnumFacing enumdirection : EnumFacing.Plane.HORIZONTAL) {
                     j1 = Math.min(j1, this.world.getChunksLowestHorizon(l + enumdirection.getFrontOffsetX(), i1 + enumdirection.getFrontOffsetZ()));
                  }

                  this.checkSkylightNeighborHeight(l, i1, j1);

                  for(EnumFacing enumdirection : EnumFacing.Plane.HORIZONTAL) {
                     this.checkSkylightNeighborHeight(l + enumdirection.getFrontOffsetX(), i1 + enumdirection.getFrontOffsetZ(), k);
                  }

                  if (flag) {
                     this.world.theProfiler.endSection();
                     return;
                  }
               }
            }
         }

         this.isGapLightingUpdated = false;
      }

      this.world.theProfiler.endSection();
   }

   private void checkSkylightNeighborHeight(int i, int j, int k) {
      int l = this.world.getHeight(new BlockPos(i, 0, j)).getY();
      if (l > k) {
         this.updateSkylightNeighborHeight(i, j, k, l + 1);
      } else if (l < k) {
         this.updateSkylightNeighborHeight(i, j, l, k + 1);
      }

   }

   private void updateSkylightNeighborHeight(int i, int j, int k, int l) {
      if (l > k && this.world.isAreaLoaded(new BlockPos(i, 0, j), 16)) {
         for(int i1 = k; i1 < l; ++i1) {
            this.world.checkLightFor(EnumSkyBlock.SKY, new BlockPos(i, i1, j));
         }

         this.isModified = true;
      }

   }

   private void relightBlock(int i, int j, int k) {
      int l = this.heightMap[k << 4 | i] & 255;
      int i1 = l;
      if (j > l) {
         i1 = j;
      }

      while(i1 > 0 && this.getBlockLightOpacity(i, i1 - 1, k) == 0) {
         --i1;
      }

      if (i1 != l) {
         this.world.markBlocksDirtyVertical(i + this.xPosition * 16, k + this.zPosition * 16, i1, l);
         this.heightMap[k << 4 | i] = i1;
         int j1 = this.xPosition * 16 + i;
         int k1 = this.zPosition * 16 + k;
         if (!this.world.provider.hasNoSky()) {
            if (i1 < l) {
               for(int l1 = i1; l1 < l; ++l1) {
                  ExtendedBlockStorage chunksection = this.storageArrays[l1 >> 4];
                  if (chunksection != NULL_BLOCK_STORAGE) {
                     chunksection.setExtSkylightValue(i, l1 & 15, k, 15);
                     this.world.notifyLightSet(new BlockPos((this.xPosition << 4) + i, l1, (this.zPosition << 4) + k));
                  }
               }
            } else {
               for(int l1 = l; l1 < i1; ++l1) {
                  ExtendedBlockStorage chunksection = this.storageArrays[l1 >> 4];
                  if (chunksection != NULL_BLOCK_STORAGE) {
                     chunksection.setExtSkylightValue(i, l1 & 15, k, 0);
                     this.world.notifyLightSet(new BlockPos((this.xPosition << 4) + i, l1, (this.zPosition << 4) + k));
                  }
               }
            }

            int var14 = 15;

            while(i1 > 0 && var14 > 0) {
               --i1;
               int i2 = this.getBlockLightOpacity(i, i1, k);
               if (i2 == 0) {
                  i2 = 1;
               }

               var14 -= i2;
               if (var14 < 0) {
                  var14 = 0;
               }

               ExtendedBlockStorage chunksection1 = this.storageArrays[i1 >> 4];
               if (chunksection1 != NULL_BLOCK_STORAGE) {
                  chunksection1.setExtSkylightValue(i, i1 & 15, k, var14);
               }
            }
         }

         int l1 = this.heightMap[k << 4 | i];
         int i2 = l;
         int j2 = l1;
         if (l1 < l) {
            i2 = l1;
            j2 = l;
         }

         if (l1 < this.heightMapMinimum) {
            this.heightMapMinimum = l1;
         }

         if (!this.world.provider.hasNoSky()) {
            for(EnumFacing enumdirection : EnumFacing.Plane.HORIZONTAL) {
               this.updateSkylightNeighborHeight(j1 + enumdirection.getFrontOffsetX(), k1 + enumdirection.getFrontOffsetZ(), i2, j2);
            }

            this.updateSkylightNeighborHeight(j1, k1, i2, j2);
         }

         this.isModified = true;
      }

   }

   public int getBlockLightOpacity(BlockPos blockposition) {
      return this.getBlockState(blockposition).getLightOpacity();
   }

   private int getBlockLightOpacity(int i, int j, int k) {
      return this.getBlockState(i, j, k).getLightOpacity();
   }

   public IBlockState getBlockState(BlockPos blockposition) {
      return this.getBlockState(blockposition.getX(), blockposition.getY(), blockposition.getZ());
   }

   public IBlockState getBlockState(final int i, final int j, final int k) {
      if (this.world.getWorldType() == WorldType.DEBUG_WORLD) {
         IBlockState iblockdata = null;
         if (j == 60) {
            iblockdata = Blocks.BARRIER.getDefaultState();
         }

         if (j == 70) {
            iblockdata = ChunkProviderDebug.getBlockStateFor(i, k);
         }

         return iblockdata == null ? Blocks.AIR.getDefaultState() : iblockdata;
      } else {
         try {
            if (j >= 0 && j >> 4 < this.storageArrays.length) {
               ExtendedBlockStorage chunksection = this.storageArrays[j >> 4];
               if (chunksection != NULL_BLOCK_STORAGE) {
                  return chunksection.get(i & 15, j & 15, k & 15);
               }
            }

            return Blocks.AIR.getDefaultState();
         } catch (Throwable var7) {
            CrashReport crashreport = CrashReport.makeCrashReport(var7, "Getting block state");
            CrashReportCategory crashreportsystemdetails = crashreport.makeCategory("Block being got");
            crashreportsystemdetails.setDetail("Location", new ICrashReportDetail() {
               public String call() throws Exception {
                  return CrashReportCategory.getCoordinateInfo(i, j, k);
               }

               public Object call() throws Exception {
                  return this.call();
               }
            });
            throw new ReportedException(crashreport);
         }
      }
   }

   @Nullable
   public IBlockState setBlockState(BlockPos blockposition, IBlockState iblockdata) {
      int i = blockposition.getX() & 15;
      int j = blockposition.getY();
      int k = blockposition.getZ() & 15;
      int l = k << 4 | i;
      if (j >= this.precipitationHeightMap[l] - 1) {
         this.precipitationHeightMap[l] = -999;
      }

      int i1 = this.heightMap[l];
      IBlockState iblockdata1 = this.getBlockState(blockposition);
      if (iblockdata1 == iblockdata) {
         return null;
      } else {
         Block block = iblockdata.getBlock();
         Block block1 = iblockdata1.getBlock();
         ExtendedBlockStorage chunksection = this.storageArrays[j >> 4];
         boolean flag = false;
         if (chunksection == NULL_BLOCK_STORAGE) {
            if (block == Blocks.AIR) {
               return null;
            }

            chunksection = new ExtendedBlockStorage(j >> 4 << 4, !this.world.provider.hasNoSky());
            this.storageArrays[j >> 4] = chunksection;
            flag = j >= i1;
         }

         chunksection.set(i, j & 15, k, iblockdata);
         if (block1 != block) {
            if (!this.world.isRemote) {
               block1.breakBlock(this.world, blockposition, iblockdata1);
            } else if (block1 instanceof ITileEntityProvider) {
               this.world.removeTileEntity(blockposition);
            }
         }

         if (chunksection.get(i, j & 15, k).getBlock() != block) {
            return null;
         } else {
            if (flag) {
               this.generateSkylightMap();
            } else {
               int j1 = iblockdata.getLightOpacity();
               int k1 = iblockdata1.getLightOpacity();
               if (j1 > 0) {
                  if (j >= i1) {
                     this.relightBlock(i, j + 1, k);
                  }
               } else if (j == i1 - 1) {
                  this.relightBlock(i, j, k);
               }

               if (j1 != k1 && (j1 < k1 || this.getLightFor(EnumSkyBlock.SKY, blockposition) > 0 || this.getLightFor(EnumSkyBlock.BLOCK, blockposition) > 0)) {
                  this.propagateSkylightOcclusion(i, k);
               }
            }

            if (block1 instanceof ITileEntityProvider) {
               TileEntity tileentity = this.getTileEntity(blockposition, Chunk.EnumCreateEntityType.CHECK);
               if (tileentity != null) {
                  tileentity.updateContainingBlockInfo();
               }
            }

            if (!this.world.isRemote && block1 != block && (!this.world.captureBlockStates || block instanceof BlockContainer)) {
               block.onBlockAdded(this.world, blockposition, iblockdata);
            }

            if (block instanceof ITileEntityProvider) {
               TileEntity tileentity = this.getTileEntity(blockposition, Chunk.EnumCreateEntityType.CHECK);
               if (tileentity == null) {
                  tileentity = ((ITileEntityProvider)block).createNewTileEntity(this.world, block.getMetaFromState(iblockdata));
                  this.world.setTileEntity(blockposition, tileentity);
               }

               if (tileentity != null) {
                  tileentity.updateContainingBlockInfo();
               }
            }

            this.isModified = true;
            return iblockdata1;
         }
      }
   }

   public int getLightFor(EnumSkyBlock enumskyblock, BlockPos blockposition) {
      int i = blockposition.getX() & 15;
      int j = blockposition.getY();
      int k = blockposition.getZ() & 15;
      ExtendedBlockStorage chunksection = this.storageArrays[j >> 4];
      return chunksection == NULL_BLOCK_STORAGE ? (this.canSeeSky(blockposition) ? enumskyblock.defaultLightValue : 0) : (enumskyblock == EnumSkyBlock.SKY ? (this.world.provider.hasNoSky() ? 0 : chunksection.getExtSkylightValue(i, j & 15, k)) : (enumskyblock == EnumSkyBlock.BLOCK ? chunksection.getExtBlocklightValue(i, j & 15, k) : enumskyblock.defaultLightValue));
   }

   public void setLightFor(EnumSkyBlock enumskyblock, BlockPos blockposition, int i) {
      int j = blockposition.getX() & 15;
      int k = blockposition.getY();
      int l = blockposition.getZ() & 15;
      ExtendedBlockStorage chunksection = this.storageArrays[k >> 4];
      if (chunksection == NULL_BLOCK_STORAGE) {
         chunksection = new ExtendedBlockStorage(k >> 4 << 4, !this.world.provider.hasNoSky());
         this.storageArrays[k >> 4] = chunksection;
         this.generateSkylightMap();
      }

      this.isModified = true;
      if (enumskyblock == EnumSkyBlock.SKY) {
         if (!this.world.provider.hasNoSky()) {
            chunksection.setExtSkylightValue(j, k & 15, l, i);
         }
      } else if (enumskyblock == EnumSkyBlock.BLOCK) {
         chunksection.setExtBlocklightValue(j, k & 15, l, i);
      }

   }

   public int getLightSubtracted(BlockPos blockposition, int i) {
      int j = blockposition.getX() & 15;
      int k = blockposition.getY();
      int l = blockposition.getZ() & 15;
      ExtendedBlockStorage chunksection = this.storageArrays[k >> 4];
      if (chunksection != NULL_BLOCK_STORAGE) {
         int i1 = this.world.provider.hasNoSky() ? 0 : chunksection.getExtSkylightValue(j, k & 15, l);
         i1 = i1 - i;
         int j1 = chunksection.getExtBlocklightValue(j, k & 15, l);
         if (j1 > i1) {
            i1 = j1;
         }

         return i1;
      } else {
         return !this.world.provider.hasNoSky() && i < EnumSkyBlock.SKY.defaultLightValue ? EnumSkyBlock.SKY.defaultLightValue - i : 0;
      }
   }

   public void addEntity(Entity entity) {
      this.hasEntities = true;
      int i = MathHelper.floor(entity.posX / 16.0D);
      int j = MathHelper.floor(entity.posZ / 16.0D);
      if (i != this.xPosition || j != this.zPosition) {
         LOGGER.warn("Wrong location! ({}, {}) should be ({}, {}), {}", new Object[]{i, j, this.xPosition, this.zPosition, entity, entity});
         entity.setDead();
      }

      int k = MathHelper.floor(entity.posY / 16.0D);
      if (k < 0) {
         k = 0;
      }

      if (k >= this.entityLists.length) {
         k = this.entityLists.length - 1;
      }

      entity.addedToChunk = true;
      entity.chunkCoordX = this.xPosition;
      entity.chunkCoordY = k;
      entity.chunkCoordZ = this.zPosition;
      this.entityLists[k].add(entity);
   }

   public void removeEntity(Entity entity) {
      this.removeEntityAtIndex(entity, entity.chunkCoordY);
   }

   public void removeEntityAtIndex(Entity entity, int i) {
      if (i < 0) {
         i = 0;
      }

      if (i >= this.entityLists.length) {
         i = this.entityLists.length - 1;
      }

      this.entityLists[i].remove(entity);
   }

   public boolean canSeeSky(BlockPos blockposition) {
      int i = blockposition.getX() & 15;
      int j = blockposition.getY();
      int k = blockposition.getZ() & 15;
      return j >= this.heightMap[k << 4 | i];
   }

   @Nullable
   private TileEntity createNewTileEntity(BlockPos blockposition) {
      IBlockState iblockdata = this.getBlockState(blockposition);
      Block block = iblockdata.getBlock();
      return !block.hasTileEntity() ? null : ((ITileEntityProvider)block).createNewTileEntity(this.world, iblockdata.getBlock().getMetaFromState(iblockdata));
   }

   @Nullable
   public TileEntity getTileEntity(BlockPos blockposition, Chunk.EnumCreateEntityType chunk_enumtileentitystate) {
      TileEntity tileentity = null;
      if (this.world.captureBlockStates) {
         tileentity = (TileEntity)this.world.capturedTileEntities.get(blockposition);
      }

      if (tileentity == null) {
         tileentity = (TileEntity)this.chunkTileEntityMap.get(blockposition);
      }

      if (tileentity == null) {
         if (chunk_enumtileentitystate == Chunk.EnumCreateEntityType.IMMEDIATE) {
            tileentity = this.createNewTileEntity(blockposition);
            this.world.setTileEntity(blockposition, tileentity);
         } else if (chunk_enumtileentitystate == Chunk.EnumCreateEntityType.QUEUED) {
            this.tileEntityPosQueue.add(blockposition);
         }
      } else if (tileentity.isInvalid()) {
         this.chunkTileEntityMap.remove(blockposition);
         return null;
      }

      return tileentity;
   }

   public void addTileEntity(TileEntity tileentity) {
      this.addTileEntity(tileentity.getPos(), tileentity);
      if (this.isChunkLoaded) {
         this.world.addTileEntity(tileentity);
      }

   }

   public void addTileEntity(BlockPos blockposition, TileEntity tileentity) {
      tileentity.setWorld(this.world);
      tileentity.setPos(blockposition);
      if (this.getBlockState(blockposition).getBlock() instanceof ITileEntityProvider) {
         if (this.chunkTileEntityMap.containsKey(blockposition)) {
            ((TileEntity)this.chunkTileEntityMap.get(blockposition)).invalidate();
         }

         tileentity.validate();
         this.chunkTileEntityMap.put(blockposition, tileentity);
      } else {
         System.out.println("Attempted to place a tile entity (" + tileentity + ") at " + tileentity.pos.getX() + "," + tileentity.pos.getY() + "," + tileentity.pos.getZ() + " (" + CraftMagicNumbers.getMaterial(this.getBlockState(blockposition).getBlock()) + ") where there was no entity tile!");
         System.out.println("Chunk coordinates: " + this.xPosition * 16 + "," + this.zPosition * 16);
         (new Exception()).printStackTrace();
      }

   }

   public void removeTileEntity(BlockPos blockposition) {
      if (this.isChunkLoaded) {
         TileEntity tileentity = (TileEntity)this.chunkTileEntityMap.remove(blockposition);
         if (tileentity != null) {
            tileentity.invalidate();
         }
      }

   }

   public void onChunkLoad() {
      this.isChunkLoaded = true;
      this.world.addTileEntities(this.chunkTileEntityMap.values());

      for(ClassInheritanceMultiMap entityslice : this.entityLists) {
         this.world.loadEntities(entityslice);
      }

   }

   public void onChunkUnload() {
      this.isChunkLoaded = false;

      for(TileEntity tileentity : this.chunkTileEntityMap.values()) {
         this.world.markTileEntityForRemoval(tileentity);
      }

      ClassInheritanceMultiMap[] aentityslice = this.entityLists;
      int i = aentityslice.length;

      for(int j = 0; j < i; ++j) {
         List newList = Lists.newArrayList(aentityslice[j]);
         Iterator iter = newList.iterator();

         while(iter.hasNext()) {
            Entity entity = (Entity)iter.next();
            if (entity instanceof EntityPlayerMP) {
               iter.remove();
            }
         }

         this.world.unloadEntities(newList);
      }

   }

   public void setChunkModified() {
      this.isModified = true;
   }

   public void getEntitiesWithinAABBForEntity(@Nullable Entity entity, AxisAlignedBB axisalignedbb, List list, Predicate predicate) {
      int i = MathHelper.floor((axisalignedbb.minY - 2.0D) / 16.0D);
      int j = MathHelper.floor((axisalignedbb.maxY + 2.0D) / 16.0D);
      i = MathHelper.clamp(i, 0, this.entityLists.length - 1);
      j = MathHelper.clamp(j, 0, this.entityLists.length - 1);

      for(int k = i; k <= j; ++k) {
         if (!this.entityLists[k].isEmpty()) {
            for(Entity entity1 : this.entityLists[k]) {
               if (entity1.getEntityBoundingBox().intersectsWith(axisalignedbb) && entity1 != entity) {
                  if (predicate == null || predicate.apply(entity1)) {
                     list.add(entity1);
                  }

                  Entity[] aentity = entity1.getParts();
                  if (aentity != null) {
                     for(Entity entity2 : aentity) {
                        if (entity2 != entity && entity2.getEntityBoundingBox().intersectsWith(axisalignedbb) && (predicate == null || predicate.apply(entity2))) {
                           list.add(entity2);
                        }
                     }
                  }
               }
            }
         }
      }

   }

   public void getEntitiesOfTypeWithinAAAB(Class oclass, AxisAlignedBB axisalignedbb, List list, Predicate predicate) {
      int i = MathHelper.floor((axisalignedbb.minY - 2.0D) / 16.0D);
      int j = MathHelper.floor((axisalignedbb.maxY + 2.0D) / 16.0D);
      i = MathHelper.clamp(i, 0, this.entityLists.length - 1);
      j = MathHelper.clamp(j, 0, this.entityLists.length - 1);

      for(int k = i; k <= j; ++k) {
         for(Entity entity : this.entityLists[k].getByClass(oclass)) {
            if (entity.getEntityBoundingBox().intersectsWith(axisalignedbb) && (predicate == null || predicate.apply(entity))) {
               list.add(entity);
            }
         }
      }

   }

   public boolean needsSaving(boolean flag) {
      if (flag) {
         if (this.hasEntities && this.world.getTotalWorldTime() != this.lastSaveTime || this.isModified) {
            return true;
         }
      } else if (this.hasEntities && this.world.getTotalWorldTime() >= this.lastSaveTime + 600L) {
         return true;
      }

      return this.isModified;
   }

   public Random getRandomWithSeed(long i) {
      return new Random(this.world.getSeed() + (long)(this.xPosition * this.xPosition * 4987142) + (long)(this.xPosition * 5947611) + (long)(this.zPosition * this.zPosition) * 4392871L + (long)(this.zPosition * 389711) ^ i);
   }

   public boolean isEmpty() {
      return false;
   }

   public void loadNearby(IChunkProvider ichunkprovider, IChunkGenerator chunkgenerator, boolean newChunk) {
      Server server = this.world.getServer();
      if (server != null) {
         server.getPluginManager().callEvent(new ChunkLoadEvent(this.bukkitChunk, newChunk));
      }

      for(int x = -2; x < 3; ++x) {
         for(int z = -2; z < 3; ++z) {
            if (x != 0 || z != 0) {
               Chunk neighbor = this.getWorld().getChunkIfLoaded(this.xPosition + x, this.zPosition + z);
               if (neighbor != null) {
                  neighbor.setNeighborLoaded(-x, -z);
                  this.setNeighborLoaded(x, z);
               }
            }
         }
      }

      Chunk chunk = ichunkprovider.getLoadedChunk(this.xPosition, this.zPosition - 1);
      Chunk chunk1 = ichunkprovider.getLoadedChunk(this.xPosition + 1, this.zPosition);
      Chunk chunk2 = ichunkprovider.getLoadedChunk(this.xPosition, this.zPosition + 1);
      Chunk chunk3 = ichunkprovider.getLoadedChunk(this.xPosition - 1, this.zPosition);
      if (chunk1 != null && chunk2 != null && ichunkprovider.getLoadedChunk(this.xPosition + 1, this.zPosition + 1) != null) {
         this.populateChunk(chunkgenerator);
      }

      if (chunk3 != null && chunk2 != null && ichunkprovider.getLoadedChunk(this.xPosition - 1, this.zPosition + 1) != null) {
         chunk3.populateChunk(chunkgenerator);
      }

      if (chunk != null && chunk1 != null && ichunkprovider.getLoadedChunk(this.xPosition + 1, this.zPosition - 1) != null) {
         chunk.populateChunk(chunkgenerator);
      }

      if (chunk != null && chunk3 != null) {
         Chunk chunk4 = ichunkprovider.getLoadedChunk(this.xPosition - 1, this.zPosition - 1);
         if (chunk4 != null) {
            chunk4.populateChunk(chunkgenerator);
         }
      }

   }

   protected void populateChunk(IChunkGenerator chunkgenerator) {
      if (this.isTerrainPopulated()) {
         if (chunkgenerator.generateStructures(this, this.xPosition, this.zPosition)) {
            this.setChunkModified();
         }
      } else {
         this.checkLight();
         chunkgenerator.populate(this.xPosition, this.zPosition);
         BlockSand.fallInstantly = true;
         Random random = new Random();
         random.setSeed(this.world.getSeed());
         long xRand = random.nextLong() / 2L * 2L + 1L;
         long zRand = random.nextLong() / 2L * 2L + 1L;
         random.setSeed((long)this.xPosition * xRand + (long)this.zPosition * zRand ^ this.world.getSeed());
         org.bukkit.World world = this.world.getWorld();
         if (world != null) {
            this.world.populating = true;

            try {
               for(BlockPopulator populator : world.getPopulators()) {
                  populator.populate(world, random, this.bukkitChunk);
               }
            } finally {
               this.world.populating = false;
            }
         }

         BlockSand.fallInstantly = false;
         this.world.getServer().getPluginManager().callEvent(new ChunkPopulateEvent(this.bukkitChunk));
         this.setChunkModified();
      }

   }

   public BlockPos getPrecipitationHeight(BlockPos blockposition) {
      int i = blockposition.getX() & 15;
      int j = blockposition.getZ() & 15;
      int k = i | j << 4;
      BlockPos blockposition1 = new BlockPos(blockposition.getX(), this.precipitationHeightMap[k], blockposition.getZ());
      if (blockposition1.getY() == -999) {
         int l = this.getTopFilledSegment() + 15;
         blockposition1 = new BlockPos(blockposition.getX(), l, blockposition.getZ());
         int i1 = -1;

         while(blockposition1.getY() > 0 && i1 == -1) {
            IBlockState iblockdata = this.getBlockState(blockposition1);
            Material material = iblockdata.getMaterial();
            if (!material.blocksMovement() && !material.isLiquid()) {
               blockposition1 = blockposition1.down();
            } else {
               i1 = blockposition1.getY() + 1;
            }
         }

         this.precipitationHeightMap[k] = i1;
      }

      return new BlockPos(blockposition.getX(), this.precipitationHeightMap[k], blockposition.getZ());
   }

   public void onTick(boolean flag) {
      if (this.isGapLightingUpdated && !this.world.provider.hasNoSky() && !flag) {
         this.recheckGaps(this.world.isRemote);
      }

      this.chunkTicked = true;
      if (!this.isLightPopulated && this.isTerrainPopulated) {
         this.checkLight();
      }

      while(!this.tileEntityPosQueue.isEmpty()) {
         BlockPos blockposition = (BlockPos)this.tileEntityPosQueue.poll();
         if (this.getTileEntity(blockposition, Chunk.EnumCreateEntityType.CHECK) == null && this.getBlockState(blockposition).getBlock().hasTileEntity()) {
            TileEntity tileentity = this.createNewTileEntity(blockposition);
            this.world.setTileEntity(blockposition, tileentity);
            this.world.markBlockRangeForRenderUpdate(blockposition, blockposition);
         }
      }

   }

   public boolean isPopulated() {
      return this.chunkTicked && this.isTerrainPopulated && this.isLightPopulated;
   }

   public boolean isChunkTicked() {
      return this.chunkTicked;
   }

   public ChunkPos getChunkCoordIntPair() {
      return new ChunkPos(this.xPosition, this.zPosition);
   }

   public boolean getAreLevelsEmpty(int i, int j) {
      if (i < 0) {
         i = 0;
      }

      if (j >= 256) {
         j = 255;
      }

      for(int k = i; k <= j; k += 16) {
         ExtendedBlockStorage chunksection = this.storageArrays[k >> 4];
         if (chunksection != NULL_BLOCK_STORAGE && !chunksection.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public void setStorageArrays(ExtendedBlockStorage[] achunksection) {
      if (this.storageArrays.length != achunksection.length) {
         LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", new Object[]{achunksection.length, this.storageArrays.length});
      } else {
         System.arraycopy(achunksection, 0, this.storageArrays, 0, this.storageArrays.length);
      }

   }

   public Biome getBiome(BlockPos blockposition, BiomeProvider worldchunkmanager) {
      int i = blockposition.getX() & 15;
      int j = blockposition.getZ() & 15;
      int k = this.blockBiomeArray[j << 4 | i] & 255;
      if (k == 255) {
         Biome biomebase = worldchunkmanager.getBiome(blockposition, Biomes.PLAINS);
         k = Biome.getIdForBiome(biomebase);
         this.blockBiomeArray[j << 4 | i] = (byte)(k & 255);
      }

      Biome biomebase = Biome.getBiome(k);
      return biomebase == null ? Biomes.PLAINS : biomebase;
   }

   public byte[] getBiomeArray() {
      return this.blockBiomeArray;
   }

   public void setBiomeArray(byte[] abyte) {
      if (this.blockBiomeArray.length != abyte.length) {
         LOGGER.warn("Could not set level chunk biomes, array length is {} instead of {}", new Object[]{abyte.length, this.blockBiomeArray.length});
      } else {
         System.arraycopy(abyte, 0, this.blockBiomeArray, 0, this.blockBiomeArray.length);
      }

   }

   public void resetRelightChecks() {
      this.queuedLightChecks = 0;
   }

   public void enqueueRelightChecks() {
      if (this.queuedLightChecks < 4096) {
         BlockPos blockposition = new BlockPos(this.xPosition << 4, 0, this.zPosition << 4);

         for(int i = 0; i < 8; ++i) {
            if (this.queuedLightChecks >= 4096) {
               return;
            }

            int j = this.queuedLightChecks % 16;
            int k = this.queuedLightChecks / 16 % 16;
            int l = this.queuedLightChecks / 256;
            ++this.queuedLightChecks;

            for(int i1 = 0; i1 < 16; ++i1) {
               BlockPos blockposition1 = blockposition.add(k, (j << 4) + i1, l);
               boolean flag = i1 == 0 || i1 == 15 || k == 0 || k == 15 || l == 0 || l == 15;
               if (this.storageArrays[j] == NULL_BLOCK_STORAGE && flag || this.storageArrays[j] != NULL_BLOCK_STORAGE && this.storageArrays[j].get(k, i1, l).getMaterial() == Material.AIR) {
                  for(EnumFacing enumdirection : EnumFacing.values()) {
                     BlockPos blockposition2 = blockposition1.offset(enumdirection);
                     if (this.world.getBlockState(blockposition2).getLightValue() > 0) {
                        this.world.checkLight(blockposition2);
                     }
                  }

                  this.world.checkLight(blockposition1);
               }
            }
         }
      }

   }

   public void checkLight() {
      this.isTerrainPopulated = true;
      this.isLightPopulated = true;
      BlockPos blockposition = new BlockPos(this.xPosition << 4, 0, this.zPosition << 4);
      if (!this.world.provider.hasNoSky()) {
         if (this.world.isAreaLoaded(blockposition.add(-1, 0, -1), blockposition.add(16, this.world.getSeaLevel(), 16))) {
            label44:
            for(int i = 0; i < 16; ++i) {
               for(int j = 0; j < 16; ++j) {
                  if (!this.checkLight(i, j)) {
                     this.isLightPopulated = false;
                     break label44;
                  }
               }
            }

            if (this.isLightPopulated) {
               for(EnumFacing enumdirection : EnumFacing.Plane.HORIZONTAL) {
                  int k = enumdirection.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? 16 : 1;
                  this.world.getChunkFromBlockCoords(blockposition.offset(enumdirection, k)).checkLightSide(enumdirection.getOpposite());
               }

               this.setSkylightUpdated();
            }
         } else {
            this.isLightPopulated = false;
         }
      }

   }

   private void setSkylightUpdated() {
      for(int i = 0; i < this.updateSkylightColumns.length; ++i) {
         this.updateSkylightColumns[i] = true;
      }

      this.recheckGaps(false);
   }

   private void checkLightSide(EnumFacing enumdirection) {
      if (this.isTerrainPopulated) {
         if (enumdirection == EnumFacing.EAST) {
            for(int i = 0; i < 16; ++i) {
               this.checkLight(15, i);
            }
         } else if (enumdirection == EnumFacing.WEST) {
            for(int i = 0; i < 16; ++i) {
               this.checkLight(0, i);
            }
         } else if (enumdirection == EnumFacing.SOUTH) {
            for(int i = 0; i < 16; ++i) {
               this.checkLight(i, 15);
            }
         } else if (enumdirection == EnumFacing.NORTH) {
            for(int i = 0; i < 16; ++i) {
               this.checkLight(i, 0);
            }
         }
      }

   }

   private boolean checkLight(int i, int j) {
      int k = this.getTopFilledSegment();
      boolean flag = false;
      boolean flag1 = false;
      BlockPos.MutableBlockPos blockposition_mutableblockposition = new BlockPos.MutableBlockPos((this.xPosition << 4) + i, 0, (this.zPosition << 4) + j);

      for(int l = k + 16 - 1; l > this.world.getSeaLevel() || l > 0 && !flag1; --l) {
         blockposition_mutableblockposition.setPos(blockposition_mutableblockposition.getX(), l, blockposition_mutableblockposition.getZ());
         int i1 = this.getBlockLightOpacity(blockposition_mutableblockposition);
         if (i1 == 255 && blockposition_mutableblockposition.getY() < this.world.getSeaLevel()) {
            flag1 = true;
         }

         if (!flag && i1 > 0) {
            flag = true;
         } else if (flag && i1 == 0 && !this.world.checkLight(blockposition_mutableblockposition)) {
            return false;
         }
      }

      for(int var9 = blockposition_mutableblockposition.getY(); var9 > 0; --var9) {
         blockposition_mutableblockposition.setPos(blockposition_mutableblockposition.getX(), var9, blockposition_mutableblockposition.getZ());
         if (this.getBlockState(blockposition_mutableblockposition).getLightValue() > 0) {
            this.world.checkLight(blockposition_mutableblockposition);
         }
      }

      return true;
   }

   public boolean isLoaded() {
      return this.isChunkLoaded;
   }

   public World getWorld() {
      return this.world;
   }

   public int[] getHeightMap() {
      return this.heightMap;
   }

   public void setHeightMap(int[] aint) {
      if (this.heightMap.length != aint.length) {
         LOGGER.warn("Could not set level chunk heightmap, array length is {} instead of {}", new Object[]{aint.length, this.heightMap.length});
      } else {
         System.arraycopy(aint, 0, this.heightMap, 0, this.heightMap.length);
      }

   }

   public Map getTileEntityMap() {
      return this.chunkTileEntityMap;
   }

   public ClassInheritanceMultiMap[] getEntityLists() {
      return this.entityLists;
   }

   public boolean isTerrainPopulated() {
      return this.isTerrainPopulated;
   }

   public void setTerrainPopulated(boolean flag) {
      this.isTerrainPopulated = flag;
   }

   public boolean isLightPopulated() {
      return this.isLightPopulated;
   }

   public void setLightPopulated(boolean flag) {
      this.isLightPopulated = flag;
   }

   public void setModified(boolean flag) {
      this.isModified = flag;
   }

   public void setHasEntities(boolean flag) {
      this.hasEntities = flag;
   }

   public void setLastSaveTime(long i) {
      this.lastSaveTime = i;
   }

   public int getLowestHeight() {
      return this.heightMapMinimum;
   }

   public long getInhabitedTime() {
      return this.inhabitedTime;
   }

   public void setInhabitedTime(long i) {
      this.inhabitedTime = i;
   }

   public static enum EnumCreateEntityType {
      IMMEDIATE,
      QUEUED,
      CHECK;
   }
}
