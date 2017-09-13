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

   public Chunk(World var1, int var2, int var3) {
      this.storageArrays = new ExtendedBlockStorage[16];
      this.blockBiomeArray = new byte[256];
      this.precipitationHeightMap = new int[256];
      this.updateSkylightColumns = new boolean[256];
      this.chunkTileEntityMap = Maps.newHashMap();
      this.queuedLightChecks = 4096;
      this.tileEntityPosQueue = Queues.newConcurrentLinkedQueue();
      this.entityLists = new ClassInheritanceMultiMap[16];
      this.world = var1;
      this.xPosition = var2;
      this.zPosition = var3;
      this.heightMap = new int[256];

      for(int var4 = 0; var4 < this.entityLists.length; ++var4) {
         this.entityLists[var4] = new ClassInheritanceMultiMap(Entity.class);
      }

      Arrays.fill(this.precipitationHeightMap, -999);
      Arrays.fill(this.blockBiomeArray, (byte)-1);
   }

   public Chunk(World var1, ChunkPrimer var2, int var3, int var4) {
      this(var1, var3, var4);
      boolean var5 = true;
      boolean var6 = !var1.provider.hasNoSky();

      for(int var7 = 0; var7 < 16; ++var7) {
         for(int var8 = 0; var8 < 16; ++var8) {
            for(int var9 = 0; var9 < 256; ++var9) {
               IBlockState var10 = var2.getBlockState(var7, var9, var8);
               if (var10.getMaterial() != Material.AIR) {
                  int var11 = var9 >> 4;
                  if (this.storageArrays[var11] == NULL_BLOCK_STORAGE) {
                     this.storageArrays[var11] = new ExtendedBlockStorage(var11 << 4, var6);
                  }

                  this.storageArrays[var11].set(var7, var9 & 15, var8, var10);
               }
            }
         }
      }

   }

   public boolean isAtLocation(int var1, int var2) {
      return var1 == this.xPosition && var2 == this.zPosition;
   }

   public int getHeight(BlockPos var1) {
      return this.getHeightValue(var1.getX() & 15, var1.getZ() & 15);
   }

   public int getHeightValue(int var1, int var2) {
      return this.heightMap[var2 << 4 | var1];
   }

   @Nullable
   private ExtendedBlockStorage getLastExtendedBlockStorage() {
      for(int var1 = this.storageArrays.length - 1; var1 >= 0; --var1) {
         if (this.storageArrays[var1] != NULL_BLOCK_STORAGE) {
            return this.storageArrays[var1];
         }
      }

      return null;
   }

   public int getTopFilledSegment() {
      ExtendedBlockStorage var1 = this.getLastExtendedBlockStorage();
      return var1 == null ? 0 : var1.getYLocation();
   }

   public ExtendedBlockStorage[] getBlockStorageArray() {
      return this.storageArrays;
   }

   @SideOnly(Side.CLIENT)
   protected void generateHeightMap() {
      int var1 = this.getTopFilledSegment();
      this.heightMapMinimum = Integer.MAX_VALUE;

      for(int var2 = 0; var2 < 16; ++var2) {
         for(int var3 = 0; var3 < 16; ++var3) {
            this.precipitationHeightMap[var2 + (var3 << 4)] = -999;

            for(int var4 = var1 + 16; var4 > 0; --var4) {
               IBlockState var5 = this.getBlockState(var2, var4 - 1, var3);
               if (var5.getLightOpacity(this.world, new BlockPos(var2, var4 - 1, var3)) != 0) {
                  this.heightMap[var3 << 4 | var2] = var4;
                  if (var4 < this.heightMapMinimum) {
                     this.heightMapMinimum = var4;
                  }
                  break;
               }
            }
         }
      }

      this.isModified = true;
   }

   public void generateSkylightMap() {
      int var1 = this.getTopFilledSegment();
      this.heightMapMinimum = Integer.MAX_VALUE;

      for(int var2 = 0; var2 < 16; ++var2) {
         for(int var3 = 0; var3 < 16; ++var3) {
            this.precipitationHeightMap[var2 + (var3 << 4)] = -999;

            for(int var4 = var1 + 16; var4 > 0; --var4) {
               if (this.getBlockLightOpacity(var2, var4 - 1, var3) != 0) {
                  this.heightMap[var3 << 4 | var2] = var4;
                  if (var4 < this.heightMapMinimum) {
                     this.heightMapMinimum = var4;
                  }
                  break;
               }
            }

            if (!this.world.provider.hasNoSky()) {
               int var8 = 15;
               int var5 = var1 + 16 - 1;

               while(true) {
                  int var6 = this.getBlockLightOpacity(var2, var5, var3);
                  if (var6 == 0 && var8 != 15) {
                     var6 = 1;
                  }

                  var8 -= var6;
                  if (var8 > 0) {
                     ExtendedBlockStorage var7 = this.storageArrays[var5 >> 4];
                     if (var7 != NULL_BLOCK_STORAGE) {
                        var7.setExtSkylightValue(var2, var5 & 15, var3, var8);
                        this.world.notifyLightSet(new BlockPos((this.xPosition << 4) + var2, var5, (this.zPosition << 4) + var3));
                     }
                  }

                  --var5;
                  if (var5 <= 0 || var8 <= 0) {
                     break;
                  }
               }
            }
         }
      }

      this.isModified = true;
   }

   private void propagateSkylightOcclusion(int var1, int var2) {
      this.updateSkylightColumns[var1 + var2 * 16] = true;
      this.isGapLightingUpdated = true;
   }

   private void recheckGaps(boolean var1) {
      this.world.theProfiler.startSection("recheckGaps");
      if (this.world.isAreaLoaded(new BlockPos(this.xPosition * 16 + 8, 0, this.zPosition * 16 + 8), 16)) {
         for(int var2 = 0; var2 < 16; ++var2) {
            for(int var3 = 0; var3 < 16; ++var3) {
               if (this.updateSkylightColumns[var2 + var3 * 16]) {
                  this.updateSkylightColumns[var2 + var3 * 16] = false;
                  int var4 = this.getHeightValue(var2, var3);
                  int var5 = this.xPosition * 16 + var2;
                  int var6 = this.zPosition * 16 + var3;
                  int var7 = Integer.MAX_VALUE;

                  for(EnumFacing var9 : EnumFacing.Plane.HORIZONTAL) {
                     var7 = Math.min(var7, this.world.getChunksLowestHorizon(var5 + var9.getFrontOffsetX(), var6 + var9.getFrontOffsetZ()));
                  }

                  this.checkSkylightNeighborHeight(var5, var6, var7);

                  for(EnumFacing var11 : EnumFacing.Plane.HORIZONTAL) {
                     this.checkSkylightNeighborHeight(var5 + var11.getFrontOffsetX(), var6 + var11.getFrontOffsetZ(), var4);
                  }

                  if (var1) {
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

   private void checkSkylightNeighborHeight(int var1, int var2, int var3) {
      int var4 = this.world.getHeight(new BlockPos(var1, 0, var2)).getY();
      if (var4 > var3) {
         this.updateSkylightNeighborHeight(var1, var2, var3, var4 + 1);
      } else if (var4 < var3) {
         this.updateSkylightNeighborHeight(var1, var2, var4, var3 + 1);
      }

   }

   private void updateSkylightNeighborHeight(int var1, int var2, int var3, int var4) {
      if (var4 > var3 && this.world.isAreaLoaded(new BlockPos(var1, 0, var2), 16)) {
         for(int var5 = var3; var5 < var4; ++var5) {
            this.world.checkLightFor(EnumSkyBlock.SKY, new BlockPos(var1, var5, var2));
         }

         this.isModified = true;
      }

   }

   private void relightBlock(int var1, int var2, int var3) {
      int var4 = this.heightMap[var3 << 4 | var1] & 255;
      int var5 = var4;
      if (var2 > var4) {
         var5 = var2;
      }

      while(var5 > 0 && this.getBlockLightOpacity(var1, var5 - 1, var3) == 0) {
         --var5;
      }

      if (var5 != var4) {
         this.world.markBlocksDirtyVertical(var1 + this.xPosition * 16, var3 + this.zPosition * 16, var5, var4);
         this.heightMap[var3 << 4 | var1] = var5;
         int var6 = this.xPosition * 16 + var1;
         int var7 = this.zPosition * 16 + var3;
         if (!this.world.provider.hasNoSky()) {
            if (var5 < var4) {
               for(int var8 = var5; var8 < var4; ++var8) {
                  ExtendedBlockStorage var9 = this.storageArrays[var8 >> 4];
                  if (var9 != NULL_BLOCK_STORAGE) {
                     var9.setExtSkylightValue(var1, var8 & 15, var3, 15);
                     this.world.notifyLightSet(new BlockPos((this.xPosition << 4) + var1, var8, (this.zPosition << 4) + var3));
                  }
               }
            } else {
               for(int var13 = var4; var13 < var5; ++var13) {
                  ExtendedBlockStorage var16 = this.storageArrays[var13 >> 4];
                  if (var16 != NULL_BLOCK_STORAGE) {
                     var16.setExtSkylightValue(var1, var13 & 15, var3, 0);
                     this.world.notifyLightSet(new BlockPos((this.xPosition << 4) + var1, var13, (this.zPosition << 4) + var3));
                  }
               }
            }

            int var14 = 15;

            while(var5 > 0 && var14 > 0) {
               --var5;
               int var17 = this.getBlockLightOpacity(var1, var5, var3);
               if (var17 == 0) {
                  var17 = 1;
               }

               var14 -= var17;
               if (var14 < 0) {
                  var14 = 0;
               }

               ExtendedBlockStorage var10 = this.storageArrays[var5 >> 4];
               if (var10 != NULL_BLOCK_STORAGE) {
                  var10.setExtSkylightValue(var1, var5 & 15, var3, var14);
               }
            }
         }

         int var15 = this.heightMap[var3 << 4 | var1];
         int var18 = var4;
         int var19 = var15;
         if (var15 < var4) {
            var18 = var15;
            var19 = var4;
         }

         if (var15 < this.heightMapMinimum) {
            this.heightMapMinimum = var15;
         }

         if (!this.world.provider.hasNoSky()) {
            for(EnumFacing var12 : EnumFacing.Plane.HORIZONTAL) {
               this.updateSkylightNeighborHeight(var6 + var12.getFrontOffsetX(), var7 + var12.getFrontOffsetZ(), var18, var19);
            }

            this.updateSkylightNeighborHeight(var6, var7, var18, var19);
         }

         this.isModified = true;
      }

   }

   public int getBlockLightOpacity(BlockPos var1) {
      return this.getBlockState(var1).getLightOpacity(this.world, var1);
   }

   private int getBlockLightOpacity(int var1, int var2, int var3) {
      IBlockState var4 = this.getBlockState(var1, var2, var3);
      return this.unloaded ? var4.getLightOpacity() : var4.getLightOpacity(this.world, new BlockPos(var1, var2, var3));
   }

   public IBlockState getBlockState(BlockPos var1) {
      return this.getBlockState(var1.getX(), var1.getY(), var1.getZ());
   }

   public IBlockState getBlockState(final int var1, final int var2, final int var3) {
      if (this.world.getWorldType() == WorldType.DEBUG_WORLD) {
         IBlockState var8 = null;
         if (var2 == 60) {
            var8 = Blocks.BARRIER.getDefaultState();
         }

         if (var2 == 70) {
            var8 = ChunkProviderDebug.getBlockStateFor(var1, var3);
         }

         return var8 == null ? Blocks.AIR.getDefaultState() : var8;
      } else {
         try {
            if (var2 >= 0 && var2 >> 4 < this.storageArrays.length) {
               ExtendedBlockStorage var4 = this.storageArrays[var2 >> 4];
               if (var4 != NULL_BLOCK_STORAGE) {
                  return var4.get(var1 & 15, var2 & 15, var3 & 15);
               }
            }

            return Blocks.AIR.getDefaultState();
         } catch (Throwable var7) {
            CrashReport var5 = CrashReport.makeCrashReport(var7, "Getting block state");
            CrashReportCategory var6 = var5.makeCategory("Block being got");
            var6.setDetail("Location", new ICrashReportDetail() {
               public String call() throws Exception {
                  return CrashReportCategory.getCoordinateInfo(var1, var2, var3);
               }
            });
            throw new ReportedException(var5);
         }
      }
   }

   @Nullable
   public IBlockState setBlockState(BlockPos var1, IBlockState var2) {
      int var3 = var1.getX() & 15;
      int var4 = var1.getY();
      int var5 = var1.getZ() & 15;
      int var6 = var5 << 4 | var3;
      if (var4 >= this.precipitationHeightMap[var6] - 1) {
         this.precipitationHeightMap[var6] = -999;
      }

      int var7 = this.heightMap[var6];
      IBlockState var8 = this.getBlockState(var1);
      if (var8 == var2) {
         return null;
      } else {
         Block var9 = var2.getBlock();
         Block var10 = var8.getBlock();
         int var11 = var8.getLightOpacity(this.world, var1);
         ExtendedBlockStorage var12 = this.storageArrays[var4 >> 4];
         boolean var13 = false;
         if (var12 == NULL_BLOCK_STORAGE) {
            if (var9 == Blocks.AIR) {
               return null;
            }

            var12 = new ExtendedBlockStorage(var4 >> 4 << 4, !this.world.provider.hasNoSky());
            this.storageArrays[var4 >> 4] = var12;
            var13 = var4 >= var7;
         }

         var12.set(var3, var4 & 15, var5, var2);
         if (!this.world.isRemote) {
            if (var10 != var9) {
               var10.breakBlock(this.world, var1, var8);
            }

            TileEntity var14 = this.getTileEntity(var1, Chunk.EnumCreateEntityType.CHECK);
            if (var14 != null && var14.shouldRefresh(this.world, var1, var8, var2)) {
               this.world.removeTileEntity(var1);
            }
         } else if (var10.hasTileEntity(var8)) {
            TileEntity var15 = this.getTileEntity(var1, Chunk.EnumCreateEntityType.CHECK);
            if (var15 != null && var15.shouldRefresh(this.world, var1, var8, var2)) {
               this.world.removeTileEntity(var1);
            }
         }

         if (var12.get(var3, var4 & 15, var5).getBlock() != var9) {
            return null;
         } else {
            if (var13) {
               this.generateSkylightMap();
            } else {
               int var16 = var2.getLightOpacity(this.world, var1);
               if (var16 > 0) {
                  if (var4 >= var7) {
                     this.relightBlock(var3, var4 + 1, var5);
                  }
               } else if (var4 == var7 - 1) {
                  this.relightBlock(var3, var4, var5);
               }

               if (var16 != var11 && (var16 < var11 || this.getLightFor(EnumSkyBlock.SKY, var1) > 0 || this.getLightFor(EnumSkyBlock.BLOCK, var1) > 0)) {
                  this.propagateSkylightOcclusion(var3, var5);
               }
            }

            if (!this.world.isRemote && var10 != var9 && (!this.world.captureBlockSnapshots || var9.hasTileEntity(var2))) {
               var9.onBlockAdded(this.world, var1, var2);
            }

            if (var9.hasTileEntity(var2)) {
               TileEntity var17 = this.getTileEntity(var1, Chunk.EnumCreateEntityType.CHECK);
               if (var17 == null) {
                  var17 = var9.createTileEntity(this.world, var2);
                  this.world.setTileEntity(var1, var17);
               }

               if (var17 != null) {
                  var17.updateContainingBlockInfo();
               }
            }

            this.isModified = true;
            return var8;
         }
      }
   }

   public int getLightFor(EnumSkyBlock var1, BlockPos var2) {
      int var3 = var2.getX() & 15;
      int var4 = var2.getY();
      int var5 = var2.getZ() & 15;
      ExtendedBlockStorage var6 = this.storageArrays[var4 >> 4];
      return var6 == NULL_BLOCK_STORAGE ? (this.canSeeSky(var2) ? var1.defaultLightValue : 0) : (var1 == EnumSkyBlock.SKY ? (this.world.provider.hasNoSky() ? 0 : var6.getExtSkylightValue(var3, var4 & 15, var5)) : (var1 == EnumSkyBlock.BLOCK ? var6.getExtBlocklightValue(var3, var4 & 15, var5) : var1.defaultLightValue));
   }

   public void setLightFor(EnumSkyBlock var1, BlockPos var2, int var3) {
      int var4 = var2.getX() & 15;
      int var5 = var2.getY();
      int var6 = var2.getZ() & 15;
      ExtendedBlockStorage var7 = this.storageArrays[var5 >> 4];
      if (var7 == NULL_BLOCK_STORAGE) {
         var7 = new ExtendedBlockStorage(var5 >> 4 << 4, !this.world.provider.hasNoSky());
         this.storageArrays[var5 >> 4] = var7;
         this.generateSkylightMap();
      }

      this.isModified = true;
      if (var1 == EnumSkyBlock.SKY) {
         if (!this.world.provider.hasNoSky()) {
            var7.setExtSkylightValue(var4, var5 & 15, var6, var3);
         }
      } else if (var1 == EnumSkyBlock.BLOCK) {
         var7.setExtBlocklightValue(var4, var5 & 15, var6, var3);
      }

   }

   public int getLightSubtracted(BlockPos var1, int var2) {
      int var3 = var1.getX() & 15;
      int var4 = var1.getY();
      int var5 = var1.getZ() & 15;
      ExtendedBlockStorage var6 = this.storageArrays[var4 >> 4];
      if (var6 != NULL_BLOCK_STORAGE) {
         int var7 = this.world.provider.hasNoSky() ? 0 : var6.getExtSkylightValue(var3, var4 & 15, var5);
         var7 = var7 - var2;
         int var8 = var6.getExtBlocklightValue(var3, var4 & 15, var5);
         if (var8 > var7) {
            var7 = var8;
         }

         return var7;
      } else {
         return !this.world.provider.hasNoSky() && var2 < EnumSkyBlock.SKY.defaultLightValue ? EnumSkyBlock.SKY.defaultLightValue - var2 : 0;
      }
   }

   public void addEntity(Entity var1) {
      this.hasEntities = true;
      int var2 = MathHelper.floor(var1.posX / 16.0D);
      int var3 = MathHelper.floor(var1.posZ / 16.0D);
      if (var2 != this.xPosition || var3 != this.zPosition) {
         LOGGER.warn("Wrong location! ({}, {}) should be ({}, {}), {}", new Object[]{var2, var3, this.xPosition, this.zPosition, var1, var1});
         var1.setDead();
      }

      int var4 = MathHelper.floor(var1.posY / 16.0D);
      if (var4 < 0) {
         var4 = 0;
      }

      if (var4 >= this.entityLists.length) {
         var4 = this.entityLists.length - 1;
      }

      MinecraftForge.EVENT_BUS.post(new EnteringChunk(var1, this.xPosition, this.zPosition, var1.chunkCoordX, var1.chunkCoordZ));
      var1.addedToChunk = true;
      var1.chunkCoordX = this.xPosition;
      var1.chunkCoordY = var4;
      var1.chunkCoordZ = this.zPosition;
      this.entityLists[var4].add(var1);
   }

   public void removeEntity(Entity var1) {
      this.removeEntityAtIndex(var1, var1.chunkCoordY);
   }

   public void removeEntityAtIndex(Entity var1, int var2) {
      if (var2 < 0) {
         var2 = 0;
      }

      if (var2 >= this.entityLists.length) {
         var2 = this.entityLists.length - 1;
      }

      this.entityLists[var2].remove(var1);
   }

   public boolean canSeeSky(BlockPos var1) {
      int var2 = var1.getX() & 15;
      int var3 = var1.getY();
      int var4 = var1.getZ() & 15;
      return var3 >= this.heightMap[var4 << 4 | var2];
   }

   @Nullable
   private TileEntity createNewTileEntity(BlockPos var1) {
      IBlockState var2 = this.getBlockState(var1);
      Block var3 = var2.getBlock();
      return !var3.hasTileEntity(var2) ? null : var3.createTileEntity(this.world, var2);
   }

   @Nullable
   public TileEntity getTileEntity(BlockPos var1, Chunk.EnumCreateEntityType var2) {
      TileEntity var3 = (TileEntity)this.chunkTileEntityMap.get(var1);
      if (var3 != null && var3.isInvalid()) {
         this.chunkTileEntityMap.remove(var1);
         var3 = null;
      }

      if (var3 == null) {
         if (var2 == Chunk.EnumCreateEntityType.IMMEDIATE) {
            var3 = this.createNewTileEntity(var1);
            this.world.setTileEntity(var1, var3);
         } else if (var2 == Chunk.EnumCreateEntityType.QUEUED) {
            this.tileEntityPosQueue.add(var1.toImmutable());
         }
      }

      return var3;
   }

   public void addTileEntity(TileEntity var1) {
      this.addTileEntity(var1.getPos(), var1);
      if (this.isChunkLoaded) {
         this.world.addTileEntity(var1);
      }

   }

   public void addTileEntity(BlockPos var1, TileEntity var2) {
      if (var2.getWorld() != this.world) {
         var2.setWorld(this.world);
      }

      var2.setPos(var1);
      if (this.getBlockState(var1).getBlock().hasTileEntity(this.getBlockState(var1))) {
         if (this.chunkTileEntityMap.containsKey(var1)) {
            ((TileEntity)this.chunkTileEntityMap.get(var1)).invalidate();
         }

         var2.validate();
         this.chunkTileEntityMap.put(var1, var2);
         var2.onLoad();
      }

   }

   public void removeTileEntity(BlockPos var1) {
      if (this.isChunkLoaded) {
         TileEntity var2 = (TileEntity)this.chunkTileEntityMap.remove(var1);
         if (var2 != null) {
            var2.invalidate();
         }
      }

   }

   public void onChunkLoad() {
      this.isChunkLoaded = true;
      this.world.addTileEntities(this.chunkTileEntityMap.values());

      for(ClassInheritanceMultiMap var4 : this.entityLists) {
         this.world.loadEntities(ImmutableList.copyOf(var4));
      }

      MinecraftForge.EVENT_BUS.post(new Load(this));
   }

   public void onChunkUnload() {
      this.isChunkLoaded = false;

      for(TileEntity var2 : this.chunkTileEntityMap.values()) {
         this.world.markTileEntityForRemoval(var2);
      }

      for(ClassInheritanceMultiMap var4 : this.entityLists) {
         this.world.unloadEntities(var4);
      }

      MinecraftForge.EVENT_BUS.post(new Unload(this));
   }

   public void setChunkModified() {
      this.isModified = true;
   }

   public void getEntitiesWithinAABBForEntity(@Nullable Entity var1, AxisAlignedBB var2, List var3, Predicate var4) {
      int var5 = MathHelper.floor((var2.minY - World.MAX_ENTITY_RADIUS) / 16.0D);
      int var6 = MathHelper.floor((var2.maxY + World.MAX_ENTITY_RADIUS) / 16.0D);
      var5 = MathHelper.clamp(var5, 0, this.entityLists.length - 1);
      var6 = MathHelper.clamp(var6, 0, this.entityLists.length - 1);

      for(int var7 = var5; var7 <= var6; ++var7) {
         if (!this.entityLists[var7].isEmpty()) {
            for(Entity var9 : this.entityLists[var7]) {
               if (var9.getEntityBoundingBox().intersectsWith(var2) && var9 != var1) {
                  if (var4 == null || var4.apply(var9)) {
                     var3.add(var9);
                  }

                  Entity[] var10 = var9.getParts();
                  if (var10 != null) {
                     for(Entity var14 : var10) {
                        if (var14 != var1 && var14.getEntityBoundingBox().intersectsWith(var2) && (var4 == null || var4.apply(var14))) {
                           var3.add(var14);
                        }
                     }
                  }
               }
            }
         }
      }

   }

   public void getEntitiesOfTypeWithinAAAB(Class var1, AxisAlignedBB var2, List var3, Predicate var4) {
      int var5 = MathHelper.floor((var2.minY - World.MAX_ENTITY_RADIUS) / 16.0D);
      int var6 = MathHelper.floor((var2.maxY + World.MAX_ENTITY_RADIUS) / 16.0D);
      var5 = MathHelper.clamp(var5, 0, this.entityLists.length - 1);
      var6 = MathHelper.clamp(var6, 0, this.entityLists.length - 1);

      for(int var7 = var5; var7 <= var6; ++var7) {
         for(Entity var9 : this.entityLists[var7].getByClass(var1)) {
            if (var9.getEntityBoundingBox().intersectsWith(var2) && (var4 == null || var4.apply(var9))) {
               var3.add(var9);
            }
         }
      }

   }

   public boolean needsSaving(boolean var1) {
      if (var1) {
         if (this.hasEntities && this.world.getTotalWorldTime() != this.lastSaveTime || this.isModified) {
            return true;
         }
      } else if (this.hasEntities && this.world.getTotalWorldTime() >= this.lastSaveTime + 600L) {
         return true;
      }

      return this.isModified;
   }

   public Random getRandomWithSeed(long var1) {
      return new Random(this.world.getSeed() + (long)(this.xPosition * this.xPosition * 4987142) + (long)(this.xPosition * 5947611) + (long)(this.zPosition * this.zPosition) * 4392871L + (long)(this.zPosition * 389711) ^ var1);
   }

   public boolean isEmpty() {
      return false;
   }

   public void populateChunk(IChunkProvider var1, IChunkGenerator var2) {
      Chunk var3 = var1.getLoadedChunk(this.xPosition, this.zPosition - 1);
      Chunk var4 = var1.getLoadedChunk(this.xPosition + 1, this.zPosition);
      Chunk var5 = var1.getLoadedChunk(this.xPosition, this.zPosition + 1);
      Chunk var6 = var1.getLoadedChunk(this.xPosition - 1, this.zPosition);
      if (var4 != null && var5 != null && var1.getLoadedChunk(this.xPosition + 1, this.zPosition + 1) != null) {
         this.populateChunk(var2);
      }

      if (var6 != null && var5 != null && var1.getLoadedChunk(this.xPosition - 1, this.zPosition + 1) != null) {
         var6.populateChunk(var2);
      }

      if (var3 != null && var4 != null && var1.getLoadedChunk(this.xPosition + 1, this.zPosition - 1) != null) {
         var3.populateChunk(var2);
      }

      if (var3 != null && var6 != null) {
         Chunk var7 = var1.getLoadedChunk(this.xPosition - 1, this.zPosition - 1);
         if (var7 != null) {
            var7.populateChunk(var2);
         }
      }

   }

   protected void populateChunk(IChunkGenerator var1) {
      if (this.isTerrainPopulated()) {
         if (var1.generateStructures(this, this.xPosition, this.zPosition)) {
            this.setChunkModified();
         }
      } else {
         this.checkLight();
         var1.populate(this.xPosition, this.zPosition);
         GameRegistry.generateWorld(this.xPosition, this.zPosition, this.world, var1, this.world.getChunkProvider());
         this.setChunkModified();
      }

   }

   public BlockPos getPrecipitationHeight(BlockPos var1) {
      int var2 = var1.getX() & 15;
      int var3 = var1.getZ() & 15;
      int var4 = var2 | var3 << 4;
      BlockPos var5 = new BlockPos(var1.getX(), this.precipitationHeightMap[var4], var1.getZ());
      if (var5.getY() == -999) {
         int var6 = this.getTopFilledSegment() + 15;
         var5 = new BlockPos(var1.getX(), var6, var1.getZ());
         int var7 = -1;

         while(var5.getY() > 0 && var7 == -1) {
            IBlockState var8 = this.getBlockState(var5);
            Material var9 = var8.getMaterial();
            if (!var9.blocksMovement() && !var9.isLiquid()) {
               var5 = var5.down();
            } else {
               var7 = var5.getY() + 1;
            }
         }

         this.precipitationHeightMap[var4] = var7;
      }

      return new BlockPos(var1.getX(), this.precipitationHeightMap[var4], var1.getZ());
   }

   public void onTick(boolean var1) {
      if (this.isGapLightingUpdated && !this.world.provider.hasNoSky() && !var1) {
         this.recheckGaps(this.world.isRemote);
      }

      this.chunkTicked = true;
      if (!this.isLightPopulated && this.isTerrainPopulated) {
         this.checkLight();
      }

      while(!this.tileEntityPosQueue.isEmpty()) {
         BlockPos var2 = (BlockPos)this.tileEntityPosQueue.poll();
         if (this.getTileEntity(var2, Chunk.EnumCreateEntityType.CHECK) == null && this.getBlockState(var2).getBlock().hasTileEntity(this.getBlockState(var2))) {
            TileEntity var3 = this.createNewTileEntity(var2);
            this.world.setTileEntity(var2, var3);
            this.world.markBlockRangeForRenderUpdate(var2, var2);
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

   public boolean getAreLevelsEmpty(int var1, int var2) {
      if (var1 < 0) {
         var1 = 0;
      }

      if (var2 >= 256) {
         var2 = 255;
      }

      for(int var3 = var1; var3 <= var2; var3 += 16) {
         ExtendedBlockStorage var4 = this.storageArrays[var3 >> 4];
         if (var4 != NULL_BLOCK_STORAGE && !var4.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public void setStorageArrays(ExtendedBlockStorage[] var1) {
      if (this.storageArrays.length != var1.length) {
         LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", new Object[]{var1.length, this.storageArrays.length});
      } else {
         System.arraycopy(var1, 0, this.storageArrays, 0, this.storageArrays.length);
      }

   }

   @SideOnly(Side.CLIENT)
   public void fillChunk(PacketBuffer var1, int var2, boolean var3) {
      for(TileEntity var5 : this.chunkTileEntityMap.values()) {
         var5.updateContainingBlockInfo();
         var5.getBlockMetadata();
         var5.getBlockType();
      }

      boolean var8 = !this.world.provider.hasNoSky();

      for(int var9 = 0; var9 < this.storageArrays.length; ++var9) {
         ExtendedBlockStorage var6 = this.storageArrays[var9];
         if ((var2 & 1 << var9) == 0) {
            if (var3 && var6 != NULL_BLOCK_STORAGE) {
               this.storageArrays[var9] = NULL_BLOCK_STORAGE;
            }
         } else {
            if (var6 == NULL_BLOCK_STORAGE) {
               var6 = new ExtendedBlockStorage(var9 << 4, var8);
               this.storageArrays[var9] = var6;
            }

            var6.getData().read(var1);
            var1.readBytes(var6.getBlocklightArray().getData());
            if (var8) {
               var1.readBytes(var6.getSkylightArray().getData());
            }
         }
      }

      if (var3) {
         var1.readBytes(this.blockBiomeArray);
      }

      for(int var10 = 0; var10 < this.storageArrays.length; ++var10) {
         if (this.storageArrays[var10] != NULL_BLOCK_STORAGE && (var2 & 1 << var10) != 0) {
            this.storageArrays[var10].removeInvalidBlocks();
         }
      }

      this.isLightPopulated = true;
      this.isTerrainPopulated = true;
      this.generateHeightMap();
      ArrayList var11 = new ArrayList();

      for(TileEntity var7 : this.chunkTileEntityMap.values()) {
         if (var7.shouldRefresh(this.world, var7.getPos(), var7.getBlockType().getStateFromMeta(var7.getBlockMetadata()), this.getBlockState(var7.getPos()))) {
            var11.add(var7);
         }

         var7.updateContainingBlockInfo();
      }

      for(TileEntity var14 : var11) {
         var14.invalidate();
      }

   }

   public Biome getBiome(BlockPos var1, BiomeProvider var2) {
      int var3 = var1.getX() & 15;
      int var4 = var1.getZ() & 15;
      int var5 = this.blockBiomeArray[var4 << 4 | var3] & 255;
      if (var5 == 255) {
         Biome var6 = var2.getBiome(var1, Biomes.PLAINS);
         var5 = Biome.getIdForBiome(var6);
         this.blockBiomeArray[var4 << 4 | var3] = (byte)(var5 & 255);
      }

      Biome var7 = Biome.getBiome(var5);
      return var7 == null ? Biomes.PLAINS : var7;
   }

   public byte[] getBiomeArray() {
      return this.blockBiomeArray;
   }

   public void setBiomeArray(byte[] var1) {
      if (this.blockBiomeArray.length != var1.length) {
         LOGGER.warn("Could not set level chunk biomes, array length is {} instead of {}", new Object[]{var1.length, this.blockBiomeArray.length});
      } else {
         System.arraycopy(var1, 0, this.blockBiomeArray, 0, this.blockBiomeArray.length);
      }

   }

   public void resetRelightChecks() {
      this.queuedLightChecks = 0;
   }

   public void enqueueRelightChecks() {
      if (this.queuedLightChecks < 4096) {
         BlockPos var1 = new BlockPos(this.xPosition << 4, 0, this.zPosition << 4);

         for(int var2 = 0; var2 < 8; ++var2) {
            if (this.queuedLightChecks >= 4096) {
               return;
            }

            int var3 = this.queuedLightChecks % 16;
            int var4 = this.queuedLightChecks / 16 % 16;
            int var5 = this.queuedLightChecks / 256;
            ++this.queuedLightChecks;

            for(int var6 = 0; var6 < 16; ++var6) {
               BlockPos var7 = var1.add(var4, (var3 << 4) + var6, var5);
               boolean var8 = var6 == 0 || var6 == 15 || var4 == 0 || var4 == 15 || var5 == 0 || var5 == 15;
               if (this.storageArrays[var3] == NULL_BLOCK_STORAGE && var8 || this.storageArrays[var3] != NULL_BLOCK_STORAGE && this.storageArrays[var3].get(var4, var6, var5).getBlock().isAir(this.storageArrays[var3].get(var4, var6, var5), this.world, var7)) {
                  for(EnumFacing var12 : EnumFacing.values()) {
                     BlockPos var13 = var7.offset(var12);
                     if (this.world.getBlockState(var13).getLightValue(this.world, var13) > 0) {
                        this.world.checkLight(var13);
                     }
                  }

                  this.world.checkLight(var7);
               }
            }
         }
      }

   }

   public void checkLight() {
      this.isTerrainPopulated = true;
      this.isLightPopulated = true;
      BlockPos var1 = new BlockPos(this.xPosition << 4, 0, this.zPosition << 4);
      if (!this.world.provider.hasNoSky()) {
         if (this.world.isAreaLoaded(var1.add(-1, 0, -1), var1.add(16, this.world.getSeaLevel(), 16))) {
            label44:
            for(int var2 = 0; var2 < 16; ++var2) {
               for(int var3 = 0; var3 < 16; ++var3) {
                  if (!this.checkLight(var2, var3)) {
                     this.isLightPopulated = false;
                     break label44;
                  }
               }
            }

            if (this.isLightPopulated) {
               for(EnumFacing var6 : EnumFacing.Plane.HORIZONTAL) {
                  int var4 = var6.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? 16 : 1;
                  this.world.getChunkFromBlockCoords(var1.offset(var6, var4)).checkLightSide(var6.getOpposite());
               }

               this.setSkylightUpdated();
            }
         } else {
            this.isLightPopulated = false;
         }
      }

   }

   private void setSkylightUpdated() {
      for(int var1 = 0; var1 < this.updateSkylightColumns.length; ++var1) {
         this.updateSkylightColumns[var1] = true;
      }

      this.recheckGaps(false);
   }

   private void checkLightSide(EnumFacing var1) {
      if (this.isTerrainPopulated) {
         if (var1 == EnumFacing.EAST) {
            for(int var2 = 0; var2 < 16; ++var2) {
               this.checkLight(15, var2);
            }
         } else if (var1 == EnumFacing.WEST) {
            for(int var3 = 0; var3 < 16; ++var3) {
               this.checkLight(0, var3);
            }
         } else if (var1 == EnumFacing.SOUTH) {
            for(int var4 = 0; var4 < 16; ++var4) {
               this.checkLight(var4, 15);
            }
         } else if (var1 == EnumFacing.NORTH) {
            for(int var5 = 0; var5 < 16; ++var5) {
               this.checkLight(var5, 0);
            }
         }
      }

   }

   private boolean checkLight(int var1, int var2) {
      int var3 = this.getTopFilledSegment();
      boolean var4 = false;
      boolean var5 = false;
      BlockPos.MutableBlockPos var6 = new BlockPos.MutableBlockPos((this.xPosition << 4) + var1, 0, (this.zPosition << 4) + var2);

      for(int var7 = var3 + 16 - 1; var7 > this.world.getSeaLevel() || var7 > 0 && !var5; --var7) {
         var6.setPos(var6.getX(), var7, var6.getZ());
         int var8 = this.getBlockLightOpacity(var6);
         if (var8 == 255 && var6.getY() < this.world.getSeaLevel()) {
            var5 = true;
         }

         if (!var4 && var8 > 0) {
            var4 = true;
         } else if (var4 && var8 == 0 && !this.world.checkLight(var6)) {
            return false;
         }
      }

      for(int var9 = var6.getY(); var9 > 0; --var9) {
         var6.setPos(var6.getX(), var9, var6.getZ());
         if (this.getBlockState(var6).getLightValue(this.world, var6) > 0) {
            this.world.checkLight(var6);
         }
      }

      return true;
   }

   public boolean isLoaded() {
      return this.isChunkLoaded;
   }

   @SideOnly(Side.CLIENT)
   public void setChunkLoaded(boolean var1) {
      this.isChunkLoaded = var1;
   }

   public World getWorld() {
      return this.world;
   }

   public int[] getHeightMap() {
      return this.heightMap;
   }

   public void setHeightMap(int[] var1) {
      if (this.heightMap.length != var1.length) {
         LOGGER.warn("Could not set level chunk heightmap, array length is {} instead of {}", new Object[]{var1.length, this.heightMap.length});
      } else {
         System.arraycopy(var1, 0, this.heightMap, 0, this.heightMap.length);
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

   public void setTerrainPopulated(boolean var1) {
      this.isTerrainPopulated = var1;
   }

   public boolean isLightPopulated() {
      return this.isLightPopulated;
   }

   public void setLightPopulated(boolean var1) {
      this.isLightPopulated = var1;
   }

   public void setModified(boolean var1) {
      this.isModified = var1;
   }

   public void setHasEntities(boolean var1) {
      this.hasEntities = var1;
   }

   public void setLastSaveTime(long var1) {
      this.lastSaveTime = var1;
   }

   public int getLowestHeight() {
      return this.heightMapMinimum;
   }

   public long getInhabitedTime() {
      return this.inhabitedTime;
   }

   public void setInhabitedTime(long var1) {
      this.inhabitedTime = var1;
   }

   public void removeInvalidTileEntity(BlockPos var1) {
      if (this.isChunkLoaded) {
         TileEntity var2 = (TileEntity)this.chunkTileEntityMap.get(var1);
         if (var2 != null && var2.isInvalid()) {
            this.chunkTileEntityMap.remove(var1);
         }
      }

   }

   public static enum EnumCreateEntityType {
      IMMEDIATE,
      QUEUED,
      CHECK;
   }
}
