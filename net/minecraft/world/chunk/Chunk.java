package net.minecraft.world.chunk;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.Entity;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.network.PacketBuffer;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent.EnteringChunk;
import net.minecraftforge.event.world.ChunkEvent.Load;
import net.minecraftforge.event.world.ChunkEvent.Unload;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Chunk {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final ExtendedBlockStorage NULL_BLOCK_STORAGE = null;
   private final ExtendedBlockStorage[] storageArrays;
   private final byte[] blockBiomeArray;
   private final int[] precipitationHeightMap;
   private final boolean[] updateSkylightColumns;
   private boolean isChunkLoaded;
   private final World world;
   private final int[] heightMap;
   public final int xPosition;
   public final int zPosition;
   private boolean isGapLightingUpdated;
   private final Map chunkTileEntityMap;
   private final ClassInheritanceMultiMap[] entityLists;
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

   public Chunk(World worldIn, int x, int z) {
      this.storageArrays = new ExtendedBlockStorage[16];
      this.blockBiomeArray = new byte[256];
      this.precipitationHeightMap = new int[256];
      this.updateSkylightColumns = new boolean[256];
      this.chunkTileEntityMap = Maps.newHashMap();
      this.queuedLightChecks = 4096;
      this.tileEntityPosQueue = Queues.newConcurrentLinkedQueue();
      this.entityLists = new ClassInheritanceMultiMap[16];
      this.world = worldIn;
      this.xPosition = x;
      this.zPosition = z;
      this.heightMap = new int[256];

      for(int i = 0; i < this.entityLists.length; ++i) {
         this.entityLists[i] = new ClassInheritanceMultiMap(Entity.class);
      }

      Arrays.fill(this.precipitationHeightMap, -999);
      Arrays.fill(this.blockBiomeArray, (byte)-1);
   }

   public Chunk(World worldIn, ChunkPrimer primer, int x, int z) {
      this(worldIn, x, z);
      int i = 256;
      boolean flag = !worldIn.provider.hasNoSky();

      for(int j = 0; j < 16; ++j) {
         for(int k = 0; k < 16; ++k) {
            for(int l = 0; l < 256; ++l) {
               IBlockState iblockstate = primer.getBlockState(j, l, k);
               if (iblockstate.getMaterial() != Material.AIR) {
                  int i1 = l >> 4;
                  if (this.storageArrays[i1] == NULL_BLOCK_STORAGE) {
                     this.storageArrays[i1] = new ExtendedBlockStorage(i1 << 4, flag);
                  }

                  this.storageArrays[i1].set(j, l & 15, k, iblockstate);
               }
            }
         }
      }

   }

   public boolean isAtLocation(int x, int z) {
      return x == this.xPosition && z == this.zPosition;
   }

   public int getHeight(BlockPos pos) {
      return this.getHeightValue(pos.getX() & 15, pos.getZ() & 15);
   }

   public int getHeightValue(int x, int z) {
      return this.heightMap[z << 4 | x];
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
      ExtendedBlockStorage extendedblockstorage = this.getLastExtendedBlockStorage();
      return extendedblockstorage == null ? 0 : extendedblockstorage.getYLocation();
   }

   public ExtendedBlockStorage[] getBlockStorageArray() {
      return this.storageArrays;
   }

   @SideOnly(Side.CLIENT)
   protected void generateHeightMap() {
      int i = this.getTopFilledSegment();
      this.heightMapMinimum = Integer.MAX_VALUE;

      for(int j = 0; j < 16; ++j) {
         for(int k = 0; k < 16; ++k) {
            this.precipitationHeightMap[j + (k << 4)] = -999;

            for(int l = i + 16; l > 0; --l) {
               IBlockState iblockstate = this.getBlockState(j, l - 1, k);
               if (iblockstate.getLightOpacity(this.world, new BlockPos(j, l - 1, k)) != 0) {
                  this.heightMap[k << 4 | j] = l;
                  if (l < this.heightMapMinimum) {
                     this.heightMapMinimum = l;
                  }
                  break;
               }
            }
         }
      }

      this.isModified = true;
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
               int k1 = 15;
               int i1 = i + 16 - 1;

               while(true) {
                  int j1 = this.getBlockLightOpacity(j, i1, k);
                  if (j1 == 0 && k1 != 15) {
                     j1 = 1;
                  }

                  k1 -= j1;
                  if (k1 > 0) {
                     ExtendedBlockStorage extendedblockstorage = this.storageArrays[i1 >> 4];
                     if (extendedblockstorage != NULL_BLOCK_STORAGE) {
                        extendedblockstorage.setExtSkylightValue(j, i1 & 15, k, k1);
                        this.world.notifyLightSet(new BlockPos((this.xPosition << 4) + j, i1, (this.zPosition << 4) + k));
                     }
                  }

                  --i1;
                  if (i1 <= 0 || k1 <= 0) {
                     break;
                  }
               }
            }
         }
      }

      this.isModified = true;
   }

   private void propagateSkylightOcclusion(int x, int z) {
      this.updateSkylightColumns[x + z * 16] = true;
      this.isGapLightingUpdated = true;
   }

   private void recheckGaps(boolean p_150803_1_) {
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

                  for(EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
                     j1 = Math.min(j1, this.world.getChunksLowestHorizon(l + enumfacing.getFrontOffsetX(), i1 + enumfacing.getFrontOffsetZ()));
                  }

                  this.checkSkylightNeighborHeight(l, i1, j1);

                  for(EnumFacing enumfacing1 : EnumFacing.Plane.HORIZONTAL) {
                     this.checkSkylightNeighborHeight(l + enumfacing1.getFrontOffsetX(), i1 + enumfacing1.getFrontOffsetZ(), k);
                  }

                  if (p_150803_1_) {
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

   private void checkSkylightNeighborHeight(int x, int z, int maxValue) {
      int i = this.world.getHeight(new BlockPos(x, 0, z)).getY();
      if (i > maxValue) {
         this.updateSkylightNeighborHeight(x, z, maxValue, i + 1);
      } else if (i < maxValue) {
         this.updateSkylightNeighborHeight(x, z, i, maxValue + 1);
      }

   }

   private void updateSkylightNeighborHeight(int x, int z, int startY, int endY) {
      if (endY > startY && this.world.isAreaLoaded(new BlockPos(x, 0, z), 16)) {
         for(int i = startY; i < endY; ++i) {
            this.world.checkLightFor(EnumSkyBlock.SKY, new BlockPos(x, i, z));
         }

         this.isModified = true;
      }

   }

   private void relightBlock(int x, int y, int z) {
      int i = this.heightMap[z << 4 | x] & 255;
      int j = i;
      if (y > i) {
         j = y;
      }

      while(j > 0 && this.getBlockLightOpacity(x, j - 1, z) == 0) {
         --j;
      }

      if (j != i) {
         this.world.markBlocksDirtyVertical(x + this.xPosition * 16, z + this.zPosition * 16, j, i);
         this.heightMap[z << 4 | x] = j;
         int k = this.xPosition * 16 + x;
         int l = this.zPosition * 16 + z;
         if (!this.world.provider.hasNoSky()) {
            if (j < i) {
               for(int j1 = j; j1 < i; ++j1) {
                  ExtendedBlockStorage extendedblockstorage2 = this.storageArrays[j1 >> 4];
                  if (extendedblockstorage2 != NULL_BLOCK_STORAGE) {
                     extendedblockstorage2.setExtSkylightValue(x, j1 & 15, z, 15);
                     this.world.notifyLightSet(new BlockPos((this.xPosition << 4) + x, j1, (this.zPosition << 4) + z));
                  }
               }
            } else {
               for(int i1 = i; i1 < j; ++i1) {
                  ExtendedBlockStorage extendedblockstorage = this.storageArrays[i1 >> 4];
                  if (extendedblockstorage != NULL_BLOCK_STORAGE) {
                     extendedblockstorage.setExtSkylightValue(x, i1 & 15, z, 0);
                     this.world.notifyLightSet(new BlockPos((this.xPosition << 4) + x, i1, (this.zPosition << 4) + z));
                  }
               }
            }

            int k1 = 15;

            while(j > 0 && k1 > 0) {
               --j;
               int i2 = this.getBlockLightOpacity(x, j, z);
               if (i2 == 0) {
                  i2 = 1;
               }

               k1 -= i2;
               if (k1 < 0) {
                  k1 = 0;
               }

               ExtendedBlockStorage extendedblockstorage1 = this.storageArrays[j >> 4];
               if (extendedblockstorage1 != NULL_BLOCK_STORAGE) {
                  extendedblockstorage1.setExtSkylightValue(x, j & 15, z, k1);
               }
            }
         }

         int l1 = this.heightMap[z << 4 | x];
         int j2 = i;
         int k2 = l1;
         if (l1 < i) {
            j2 = l1;
            k2 = i;
         }

         if (l1 < this.heightMapMinimum) {
            this.heightMapMinimum = l1;
         }

         if (!this.world.provider.hasNoSky()) {
            for(EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
               this.updateSkylightNeighborHeight(k + enumfacing.getFrontOffsetX(), l + enumfacing.getFrontOffsetZ(), j2, k2);
            }

            this.updateSkylightNeighborHeight(k, l, j2, k2);
         }

         this.isModified = true;
      }

   }

   public int getBlockLightOpacity(BlockPos pos) {
      return this.getBlockState(pos).getLightOpacity(this.world, pos);
   }

   private int getBlockLightOpacity(int x, int y, int z) {
      IBlockState state = this.getBlockState(x, y, z);
      return this.unloaded ? state.getLightOpacity() : state.getLightOpacity(this.world, new BlockPos(x, y, z));
   }

   public IBlockState getBlockState(BlockPos pos) {
      return this.getBlockState(pos.getX(), pos.getY(), pos.getZ());
   }

   public IBlockState getBlockState(final int x, final int y, final int z) {
      if (this.world.getWorldType() == WorldType.DEBUG_WORLD) {
         IBlockState iblockstate = null;
         if (y == 60) {
            iblockstate = Blocks.BARRIER.getDefaultState();
         }

         if (y == 70) {
            iblockstate = ChunkProviderDebug.getBlockStateFor(x, z);
         }

         return iblockstate == null ? Blocks.AIR.getDefaultState() : iblockstate;
      } else {
         try {
            if (y >= 0 && y >> 4 < this.storageArrays.length) {
               ExtendedBlockStorage extendedblockstorage = this.storageArrays[y >> 4];
               if (extendedblockstorage != NULL_BLOCK_STORAGE) {
                  return extendedblockstorage.get(x & 15, y & 15, z & 15);
               }
            }

            return Blocks.AIR.getDefaultState();
         } catch (Throwable var7) {
            CrashReport crashreport = CrashReport.makeCrashReport(var7, "Getting block state");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being got");
            crashreportcategory.setDetail("Location", new ICrashReportDetail() {
               public String call() throws Exception {
                  return CrashReportCategory.getCoordinateInfo(x, y, z);
               }
            });
            throw new ReportedException(crashreport);
         }
      }
   }

   @Nullable
   public IBlockState setBlockState(BlockPos pos, IBlockState state) {
      int i = pos.getX() & 15;
      int j = pos.getY();
      int k = pos.getZ() & 15;
      int l = k << 4 | i;
      if (j >= this.precipitationHeightMap[l] - 1) {
         this.precipitationHeightMap[l] = -999;
      }

      int i1 = this.heightMap[l];
      IBlockState iblockstate = this.getBlockState(pos);
      if (iblockstate == state) {
         return null;
      } else {
         Block block = state.getBlock();
         Block block1 = iblockstate.getBlock();
         int k1 = iblockstate.getLightOpacity(this.world, pos);
         ExtendedBlockStorage extendedblockstorage = this.storageArrays[j >> 4];
         boolean flag = false;
         if (extendedblockstorage == NULL_BLOCK_STORAGE) {
            if (block == Blocks.AIR) {
               return null;
            }

            extendedblockstorage = new ExtendedBlockStorage(j >> 4 << 4, !this.world.provider.hasNoSky());
            this.storageArrays[j >> 4] = extendedblockstorage;
            flag = j >= i1;
         }

         extendedblockstorage.set(i, j & 15, k, state);
         if (!this.world.isRemote) {
            if (block1 != block) {
               block1.breakBlock(this.world, pos, iblockstate);
            }

            TileEntity te = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
            if (te != null && te.shouldRefresh(this.world, pos, iblockstate, state)) {
               this.world.removeTileEntity(pos);
            }
         } else if (block1.hasTileEntity(iblockstate)) {
            TileEntity te = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
            if (te != null && te.shouldRefresh(this.world, pos, iblockstate, state)) {
               this.world.removeTileEntity(pos);
            }
         }

         if (extendedblockstorage.get(i, j & 15, k).getBlock() != block) {
            return null;
         } else {
            if (flag) {
               this.generateSkylightMap();
            } else {
               int j1 = state.getLightOpacity(this.world, pos);
               if (j1 > 0) {
                  if (j >= i1) {
                     this.relightBlock(i, j + 1, k);
                  }
               } else if (j == i1 - 1) {
                  this.relightBlock(i, j, k);
               }

               if (j1 != k1 && (j1 < k1 || this.getLightFor(EnumSkyBlock.SKY, pos) > 0 || this.getLightFor(EnumSkyBlock.BLOCK, pos) > 0)) {
                  this.propagateSkylightOcclusion(i, k);
               }
            }

            if (!this.world.isRemote && block1 != block && (!this.world.captureBlockSnapshots || block.hasTileEntity(state))) {
               block.onBlockAdded(this.world, pos, state);
            }

            if (block.hasTileEntity(state)) {
               TileEntity tileentity1 = this.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
               if (tileentity1 == null) {
                  tileentity1 = block.createTileEntity(this.world, state);
                  this.world.setTileEntity(pos, tileentity1);
               }

               if (tileentity1 != null) {
                  tileentity1.updateContainingBlockInfo();
               }
            }

            this.isModified = true;
            return iblockstate;
         }
      }
   }

   public int getLightFor(EnumSkyBlock p_177413_1_, BlockPos pos) {
      int i = pos.getX() & 15;
      int j = pos.getY();
      int k = pos.getZ() & 15;
      ExtendedBlockStorage extendedblockstorage = this.storageArrays[j >> 4];
      return extendedblockstorage == NULL_BLOCK_STORAGE ? (this.canSeeSky(pos) ? p_177413_1_.defaultLightValue : 0) : (p_177413_1_ == EnumSkyBlock.SKY ? (this.world.provider.hasNoSky() ? 0 : extendedblockstorage.getExtSkylightValue(i, j & 15, k)) : (p_177413_1_ == EnumSkyBlock.BLOCK ? extendedblockstorage.getExtBlocklightValue(i, j & 15, k) : p_177413_1_.defaultLightValue));
   }

   public void setLightFor(EnumSkyBlock p_177431_1_, BlockPos pos, int value) {
      int i = pos.getX() & 15;
      int j = pos.getY();
      int k = pos.getZ() & 15;
      ExtendedBlockStorage extendedblockstorage = this.storageArrays[j >> 4];
      if (extendedblockstorage == NULL_BLOCK_STORAGE) {
         extendedblockstorage = new ExtendedBlockStorage(j >> 4 << 4, !this.world.provider.hasNoSky());
         this.storageArrays[j >> 4] = extendedblockstorage;
         this.generateSkylightMap();
      }

      this.isModified = true;
      if (p_177431_1_ == EnumSkyBlock.SKY) {
         if (!this.world.provider.hasNoSky()) {
            extendedblockstorage.setExtSkylightValue(i, j & 15, k, value);
         }
      } else if (p_177431_1_ == EnumSkyBlock.BLOCK) {
         extendedblockstorage.setExtBlocklightValue(i, j & 15, k, value);
      }

   }

   public int getLightSubtracted(BlockPos pos, int amount) {
      int i = pos.getX() & 15;
      int j = pos.getY();
      int k = pos.getZ() & 15;
      ExtendedBlockStorage extendedblockstorage = this.storageArrays[j >> 4];
      if (extendedblockstorage != NULL_BLOCK_STORAGE) {
         int l = this.world.provider.hasNoSky() ? 0 : extendedblockstorage.getExtSkylightValue(i, j & 15, k);
         l = l - amount;
         int i1 = extendedblockstorage.getExtBlocklightValue(i, j & 15, k);
         if (i1 > l) {
            l = i1;
         }

         return l;
      } else {
         return !this.world.provider.hasNoSky() && amount < EnumSkyBlock.SKY.defaultLightValue ? EnumSkyBlock.SKY.defaultLightValue - amount : 0;
      }
   }

   public void addEntity(Entity entityIn) {
      this.hasEntities = true;
      int i = MathHelper.floor(entityIn.posX / 16.0D);
      int j = MathHelper.floor(entityIn.posZ / 16.0D);
      if (i != this.xPosition || j != this.zPosition) {
         LOGGER.warn("Wrong location! ({}, {}) should be ({}, {}), {}", new Object[]{i, j, this.xPosition, this.zPosition, entityIn, entityIn});
         entityIn.setDead();
      }

      int k = MathHelper.floor(entityIn.posY / 16.0D);
      if (k < 0) {
         k = 0;
      }

      if (k >= this.entityLists.length) {
         k = this.entityLists.length - 1;
      }

      MinecraftForge.EVENT_BUS.post(new EnteringChunk(entityIn, this.xPosition, this.zPosition, entityIn.chunkCoordX, entityIn.chunkCoordZ));
      entityIn.addedToChunk = true;
      entityIn.chunkCoordX = this.xPosition;
      entityIn.chunkCoordY = k;
      entityIn.chunkCoordZ = this.zPosition;
      this.entityLists[k].add(entityIn);
   }

   public void removeEntity(Entity entityIn) {
      this.removeEntityAtIndex(entityIn, entityIn.chunkCoordY);
   }

   public void removeEntityAtIndex(Entity entityIn, int index) {
      if (index < 0) {
         index = 0;
      }

      if (index >= this.entityLists.length) {
         index = this.entityLists.length - 1;
      }

      this.entityLists[index].remove(entityIn);
   }

   public boolean canSeeSky(BlockPos pos) {
      int i = pos.getX() & 15;
      int j = pos.getY();
      int k = pos.getZ() & 15;
      return j >= this.heightMap[k << 4 | i];
   }

   @Nullable
   private TileEntity createNewTileEntity(BlockPos pos) {
      IBlockState iblockstate = this.getBlockState(pos);
      Block block = iblockstate.getBlock();
      return !block.hasTileEntity(iblockstate) ? null : block.createTileEntity(this.world, iblockstate);
   }

   @Nullable
   public TileEntity getTileEntity(BlockPos pos, Chunk.EnumCreateEntityType p_177424_2_) {
      TileEntity tileentity = (TileEntity)this.chunkTileEntityMap.get(pos);
      if (tileentity != null && tileentity.isInvalid()) {
         this.chunkTileEntityMap.remove(pos);
         tileentity = null;
      }

      if (tileentity == null) {
         if (p_177424_2_ == Chunk.EnumCreateEntityType.IMMEDIATE) {
            tileentity = this.createNewTileEntity(pos);
            this.world.setTileEntity(pos, tileentity);
         } else if (p_177424_2_ == Chunk.EnumCreateEntityType.QUEUED) {
            this.tileEntityPosQueue.add(pos.toImmutable());
         }
      }

      return tileentity;
   }

   public void addTileEntity(TileEntity tileEntityIn) {
      this.addTileEntity(tileEntityIn.getPos(), tileEntityIn);
      if (this.isChunkLoaded) {
         this.world.addTileEntity(tileEntityIn);
      }

   }

   public void addTileEntity(BlockPos pos, TileEntity tileEntityIn) {
      if (tileEntityIn.getWorld() != this.world) {
         tileEntityIn.setWorld(this.world);
      }

      tileEntityIn.setPos(pos);
      if (this.getBlockState(pos).getBlock().hasTileEntity(this.getBlockState(pos))) {
         if (this.chunkTileEntityMap.containsKey(pos)) {
            ((TileEntity)this.chunkTileEntityMap.get(pos)).invalidate();
         }

         tileEntityIn.validate();
         this.chunkTileEntityMap.put(pos, tileEntityIn);
         tileEntityIn.onLoad();
      }

   }

   public void removeTileEntity(BlockPos pos) {
      if (this.isChunkLoaded) {
         TileEntity tileentity = (TileEntity)this.chunkTileEntityMap.remove(pos);
         if (tileentity != null) {
            tileentity.invalidate();
         }
      }

   }

   public void onChunkLoad() {
      this.isChunkLoaded = true;
      this.world.addTileEntities(this.chunkTileEntityMap.values());

      for(ClassInheritanceMultiMap classinheritancemultimap : this.entityLists) {
         this.world.loadEntities(ImmutableList.copyOf(classinheritancemultimap));
      }

      MinecraftForge.EVENT_BUS.post(new Load(this));
   }

   public void onChunkUnload() {
      this.isChunkLoaded = false;

      for(TileEntity tileentity : this.chunkTileEntityMap.values()) {
         this.world.markTileEntityForRemoval(tileentity);
      }

      for(ClassInheritanceMultiMap classinheritancemultimap : this.entityLists) {
         this.world.unloadEntities(classinheritancemultimap);
      }

      MinecraftForge.EVENT_BUS.post(new Unload(this));
   }

   public void setChunkModified() {
      this.isModified = true;
   }

   public void getEntitiesWithinAABBForEntity(@Nullable Entity entityIn, AxisAlignedBB aabb, List listToFill, Predicate p_177414_4_) {
      int i = MathHelper.floor((aabb.minY - World.MAX_ENTITY_RADIUS) / 16.0D);
      int j = MathHelper.floor((aabb.maxY + World.MAX_ENTITY_RADIUS) / 16.0D);
      i = MathHelper.clamp(i, 0, this.entityLists.length - 1);
      j = MathHelper.clamp(j, 0, this.entityLists.length - 1);

      for(int k = i; k <= j; ++k) {
         if (!this.entityLists[k].isEmpty()) {
            for(Entity entity : this.entityLists[k]) {
               if (entity.getEntityBoundingBox().intersectsWith(aabb) && entity != entityIn) {
                  if (p_177414_4_ == null || p_177414_4_.apply(entity)) {
                     listToFill.add(entity);
                  }

                  Entity[] aentity = entity.getParts();
                  if (aentity != null) {
                     for(Entity entity1 : aentity) {
                        if (entity1 != entityIn && entity1.getEntityBoundingBox().intersectsWith(aabb) && (p_177414_4_ == null || p_177414_4_.apply(entity1))) {
                           listToFill.add(entity1);
                        }
                     }
                  }
               }
            }
         }
      }

   }

   public void getEntitiesOfTypeWithinAAAB(Class entityClass, AxisAlignedBB aabb, List listToFill, Predicate filter) {
      int i = MathHelper.floor((aabb.minY - World.MAX_ENTITY_RADIUS) / 16.0D);
      int j = MathHelper.floor((aabb.maxY + World.MAX_ENTITY_RADIUS) / 16.0D);
      i = MathHelper.clamp(i, 0, this.entityLists.length - 1);
      j = MathHelper.clamp(j, 0, this.entityLists.length - 1);

      for(int k = i; k <= j; ++k) {
         for(Entity t : this.entityLists[k].getByClass(entityClass)) {
            if (t.getEntityBoundingBox().intersectsWith(aabb) && (filter == null || filter.apply(t))) {
               listToFill.add(t);
            }
         }
      }

   }

   public boolean needsSaving(boolean p_76601_1_) {
      if (p_76601_1_) {
         if (this.hasEntities && this.world.getTotalWorldTime() != this.lastSaveTime || this.isModified) {
            return true;
         }
      } else if (this.hasEntities && this.world.getTotalWorldTime() >= this.lastSaveTime + 600L) {
         return true;
      }

      return this.isModified;
   }

   public Random getRandomWithSeed(long seed) {
      return new Random(this.world.getSeed() + (long)(this.xPosition * this.xPosition * 4987142) + (long)(this.xPosition * 5947611) + (long)(this.zPosition * this.zPosition) * 4392871L + (long)(this.zPosition * 389711) ^ seed);
   }

   public boolean isEmpty() {
      return false;
   }

   public void populateChunk(IChunkProvider chunkProvider, IChunkGenerator chunkGenrator) {
      Chunk chunk = chunkProvider.getLoadedChunk(this.xPosition, this.zPosition - 1);
      Chunk chunk1 = chunkProvider.getLoadedChunk(this.xPosition + 1, this.zPosition);
      Chunk chunk2 = chunkProvider.getLoadedChunk(this.xPosition, this.zPosition + 1);
      Chunk chunk3 = chunkProvider.getLoadedChunk(this.xPosition - 1, this.zPosition);
      if (chunk1 != null && chunk2 != null && chunkProvider.getLoadedChunk(this.xPosition + 1, this.zPosition + 1) != null) {
         this.populateChunk(chunkGenrator);
      }

      if (chunk3 != null && chunk2 != null && chunkProvider.getLoadedChunk(this.xPosition - 1, this.zPosition + 1) != null) {
         chunk3.populateChunk(chunkGenrator);
      }

      if (chunk != null && chunk1 != null && chunkProvider.getLoadedChunk(this.xPosition + 1, this.zPosition - 1) != null) {
         chunk.populateChunk(chunkGenrator);
      }

      if (chunk != null && chunk3 != null) {
         Chunk chunk4 = chunkProvider.getLoadedChunk(this.xPosition - 1, this.zPosition - 1);
         if (chunk4 != null) {
            chunk4.populateChunk(chunkGenrator);
         }
      }

   }

   protected void populateChunk(IChunkGenerator generator) {
      if (this.isTerrainPopulated()) {
         if (generator.generateStructures(this, this.xPosition, this.zPosition)) {
            this.setChunkModified();
         }
      } else {
         this.checkLight();
         generator.populate(this.xPosition, this.zPosition);
         GameRegistry.generateWorld(this.xPosition, this.zPosition, this.world, generator, this.world.getChunkProvider());
         this.setChunkModified();
      }

   }

   public BlockPos getPrecipitationHeight(BlockPos pos) {
      int i = pos.getX() & 15;
      int j = pos.getZ() & 15;
      int k = i | j << 4;
      BlockPos blockpos = new BlockPos(pos.getX(), this.precipitationHeightMap[k], pos.getZ());
      if (blockpos.getY() == -999) {
         int l = this.getTopFilledSegment() + 15;
         blockpos = new BlockPos(pos.getX(), l, pos.getZ());
         int i1 = -1;

         while(blockpos.getY() > 0 && i1 == -1) {
            IBlockState iblockstate = this.getBlockState(blockpos);
            Material material = iblockstate.getMaterial();
            if (!material.blocksMovement() && !material.isLiquid()) {
               blockpos = blockpos.down();
            } else {
               i1 = blockpos.getY() + 1;
            }
         }

         this.precipitationHeightMap[k] = i1;
      }

      return new BlockPos(pos.getX(), this.precipitationHeightMap[k], pos.getZ());
   }

   public void onTick(boolean p_150804_1_) {
      if (this.isGapLightingUpdated && !this.world.provider.hasNoSky() && !p_150804_1_) {
         this.recheckGaps(this.world.isRemote);
      }

      this.chunkTicked = true;
      if (!this.isLightPopulated && this.isTerrainPopulated) {
         this.checkLight();
      }

      while(!this.tileEntityPosQueue.isEmpty()) {
         BlockPos blockpos = (BlockPos)this.tileEntityPosQueue.poll();
         if (this.getTileEntity(blockpos, Chunk.EnumCreateEntityType.CHECK) == null && this.getBlockState(blockpos).getBlock().hasTileEntity(this.getBlockState(blockpos))) {
            TileEntity tileentity = this.createNewTileEntity(blockpos);
            this.world.setTileEntity(blockpos, tileentity);
            this.world.markBlockRangeForRenderUpdate(blockpos, blockpos);
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

   public boolean getAreLevelsEmpty(int startY, int endY) {
      if (startY < 0) {
         startY = 0;
      }

      if (endY >= 256) {
         endY = 255;
      }

      for(int i = startY; i <= endY; i += 16) {
         ExtendedBlockStorage extendedblockstorage = this.storageArrays[i >> 4];
         if (extendedblockstorage != NULL_BLOCK_STORAGE && !extendedblockstorage.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public void setStorageArrays(ExtendedBlockStorage[] newStorageArrays) {
      if (this.storageArrays.length != newStorageArrays.length) {
         LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", new Object[]{newStorageArrays.length, this.storageArrays.length});
      } else {
         System.arraycopy(newStorageArrays, 0, this.storageArrays, 0, this.storageArrays.length);
      }

   }

   @SideOnly(Side.CLIENT)
   public void fillChunk(PacketBuffer buf, int p_186033_2_, boolean p_186033_3_) {
      for(TileEntity tileEntity : this.chunkTileEntityMap.values()) {
         tileEntity.updateContainingBlockInfo();
         tileEntity.getBlockMetadata();
         tileEntity.getBlockType();
      }

      boolean flag = !this.world.provider.hasNoSky();

      for(int i = 0; i < this.storageArrays.length; ++i) {
         ExtendedBlockStorage extendedblockstorage = this.storageArrays[i];
         if ((p_186033_2_ & 1 << i) == 0) {
            if (p_186033_3_ && extendedblockstorage != NULL_BLOCK_STORAGE) {
               this.storageArrays[i] = NULL_BLOCK_STORAGE;
            }
         } else {
            if (extendedblockstorage == NULL_BLOCK_STORAGE) {
               extendedblockstorage = new ExtendedBlockStorage(i << 4, flag);
               this.storageArrays[i] = extendedblockstorage;
            }

            extendedblockstorage.getData().read(buf);
            buf.readBytes(extendedblockstorage.getBlocklightArray().getData());
            if (flag) {
               buf.readBytes(extendedblockstorage.getSkylightArray().getData());
            }
         }
      }

      if (p_186033_3_) {
         buf.readBytes(this.blockBiomeArray);
      }

      for(int j = 0; j < this.storageArrays.length; ++j) {
         if (this.storageArrays[j] != NULL_BLOCK_STORAGE && (p_186033_2_ & 1 << j) != 0) {
            this.storageArrays[j].removeInvalidBlocks();
         }
      }

      this.isLightPopulated = true;
      this.isTerrainPopulated = true;
      this.generateHeightMap();
      List invalidList = new ArrayList();

      for(TileEntity tileentity : this.chunkTileEntityMap.values()) {
         if (tileentity.shouldRefresh(this.world, tileentity.getPos(), tileentity.getBlockType().getStateFromMeta(tileentity.getBlockMetadata()), this.getBlockState(tileentity.getPos()))) {
            invalidList.add(tileentity);
         }

         tileentity.updateContainingBlockInfo();
      }

      for(TileEntity te : invalidList) {
         te.invalidate();
      }

   }

   public Biome getBiome(BlockPos pos, BiomeProvider provider) {
      int i = pos.getX() & 15;
      int j = pos.getZ() & 15;
      int k = this.blockBiomeArray[j << 4 | i] & 255;
      if (k == 255) {
         Biome biome = provider.getBiome(pos, Biomes.PLAINS);
         k = Biome.getIdForBiome(biome);
         this.blockBiomeArray[j << 4 | i] = (byte)(k & 255);
      }

      Biome biome1 = Biome.getBiome(k);
      return biome1 == null ? Biomes.PLAINS : biome1;
   }

   public byte[] getBiomeArray() {
      return this.blockBiomeArray;
   }

   public void setBiomeArray(byte[] biomeArray) {
      if (this.blockBiomeArray.length != biomeArray.length) {
         LOGGER.warn("Could not set level chunk biomes, array length is {} instead of {}", new Object[]{biomeArray.length, this.blockBiomeArray.length});
      } else {
         System.arraycopy(biomeArray, 0, this.blockBiomeArray, 0, this.blockBiomeArray.length);
      }

   }

   public void resetRelightChecks() {
      this.queuedLightChecks = 0;
   }

   public void enqueueRelightChecks() {
      if (this.queuedLightChecks < 4096) {
         BlockPos blockpos = new BlockPos(this.xPosition << 4, 0, this.zPosition << 4);

         for(int i = 0; i < 8; ++i) {
            if (this.queuedLightChecks >= 4096) {
               return;
            }

            int j = this.queuedLightChecks % 16;
            int k = this.queuedLightChecks / 16 % 16;
            int l = this.queuedLightChecks / 256;
            ++this.queuedLightChecks;

            for(int i1 = 0; i1 < 16; ++i1) {
               BlockPos blockpos1 = blockpos.add(k, (j << 4) + i1, l);
               boolean flag = i1 == 0 || i1 == 15 || k == 0 || k == 15 || l == 0 || l == 15;
               if (this.storageArrays[j] == NULL_BLOCK_STORAGE && flag || this.storageArrays[j] != NULL_BLOCK_STORAGE && this.storageArrays[j].get(k, i1, l).getBlock().isAir(this.storageArrays[j].get(k, i1, l), this.world, blockpos1)) {
                  for(EnumFacing enumfacing : EnumFacing.values()) {
                     BlockPos blockpos2 = blockpos1.offset(enumfacing);
                     if (this.world.getBlockState(blockpos2).getLightValue(this.world, blockpos2) > 0) {
                        this.world.checkLight(blockpos2);
                     }
                  }

                  this.world.checkLight(blockpos1);
               }
            }
         }
      }

   }

   public void checkLight() {
      this.isTerrainPopulated = true;
      this.isLightPopulated = true;
      BlockPos blockpos = new BlockPos(this.xPosition << 4, 0, this.zPosition << 4);
      if (!this.world.provider.hasNoSky()) {
         if (this.world.isAreaLoaded(blockpos.add(-1, 0, -1), blockpos.add(16, this.world.getSeaLevel(), 16))) {
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
               for(EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
                  int k = enumfacing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? 16 : 1;
                  this.world.getChunkFromBlockCoords(blockpos.offset(enumfacing, k)).checkLightSide(enumfacing.getOpposite());
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

   private void checkLightSide(EnumFacing facing) {
      if (this.isTerrainPopulated) {
         if (facing == EnumFacing.EAST) {
            for(int i = 0; i < 16; ++i) {
               this.checkLight(15, i);
            }
         } else if (facing == EnumFacing.WEST) {
            for(int j = 0; j < 16; ++j) {
               this.checkLight(0, j);
            }
         } else if (facing == EnumFacing.SOUTH) {
            for(int k = 0; k < 16; ++k) {
               this.checkLight(k, 15);
            }
         } else if (facing == EnumFacing.NORTH) {
            for(int l = 0; l < 16; ++l) {
               this.checkLight(l, 0);
            }
         }
      }

   }

   private boolean checkLight(int x, int z) {
      int i = this.getTopFilledSegment();
      boolean flag = false;
      boolean flag1 = false;
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos((this.xPosition << 4) + x, 0, (this.zPosition << 4) + z);

      for(int j = i + 16 - 1; j > this.world.getSeaLevel() || j > 0 && !flag1; --j) {
         blockpos$mutableblockpos.setPos(blockpos$mutableblockpos.getX(), j, blockpos$mutableblockpos.getZ());
         int k = this.getBlockLightOpacity(blockpos$mutableblockpos);
         if (k == 255 && blockpos$mutableblockpos.getY() < this.world.getSeaLevel()) {
            flag1 = true;
         }

         if (!flag && k > 0) {
            flag = true;
         } else if (flag && k == 0 && !this.world.checkLight(blockpos$mutableblockpos)) {
            return false;
         }
      }

      for(int l = blockpos$mutableblockpos.getY(); l > 0; --l) {
         blockpos$mutableblockpos.setPos(blockpos$mutableblockpos.getX(), l, blockpos$mutableblockpos.getZ());
         if (this.getBlockState(blockpos$mutableblockpos).getLightValue(this.world, blockpos$mutableblockpos) > 0) {
            this.world.checkLight(blockpos$mutableblockpos);
         }
      }

      return true;
   }

   public boolean isLoaded() {
      return this.isChunkLoaded;
   }

   @SideOnly(Side.CLIENT)
   public void setChunkLoaded(boolean loaded) {
      this.isChunkLoaded = loaded;
   }

   public World getWorld() {
      return this.world;
   }

   public int[] getHeightMap() {
      return this.heightMap;
   }

   public void setHeightMap(int[] newHeightMap) {
      if (this.heightMap.length != newHeightMap.length) {
         LOGGER.warn("Could not set level chunk heightmap, array length is {} instead of {}", new Object[]{newHeightMap.length, this.heightMap.length});
      } else {
         System.arraycopy(newHeightMap, 0, this.heightMap, 0, this.heightMap.length);
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

   public void setTerrainPopulated(boolean terrainPopulated) {
      this.isTerrainPopulated = terrainPopulated;
   }

   public boolean isLightPopulated() {
      return this.isLightPopulated;
   }

   public void setLightPopulated(boolean lightPopulated) {
      this.isLightPopulated = lightPopulated;
   }

   public void setModified(boolean modified) {
      this.isModified = modified;
   }

   public void setHasEntities(boolean hasEntitiesIn) {
      this.hasEntities = hasEntitiesIn;
   }

   public void setLastSaveTime(long saveTime) {
      this.lastSaveTime = saveTime;
   }

   public int getLowestHeight() {
      return this.heightMapMinimum;
   }

   public long getInhabitedTime() {
      return this.inhabitedTime;
   }

   public void setInhabitedTime(long newInhabitedTime) {
      this.inhabitedTime = newInhabitedTime;
   }

   public void removeInvalidTileEntity(BlockPos pos) {
      if (this.isChunkLoaded) {
         TileEntity entity = (TileEntity)this.chunkTileEntityMap.get(pos);
         if (entity != null && entity.isInvalid()) {
            this.chunkTileEntityMap.remove(pos);
         }
      }

   }

   public static enum EnumCreateEntityType {
      IMMEDIATE,
      QUEUED,
      CHECK;
   }
}
