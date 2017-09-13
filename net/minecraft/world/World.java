package net.minecraft.world;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.pathfinding.PathWorldListener;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ReportedException;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.WorldCapabilityData;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class World implements IBlockAccess, ICapabilityProvider {
   public static double MAX_ENTITY_RADIUS = 2.0D;
   private int seaLevel = 63;
   protected boolean scheduledUpdatesAreImmediate;
   public final List loadedEntityList = Lists.newArrayList();
   protected final List unloadedEntityList = Lists.newArrayList();
   public final List loadedTileEntityList = Lists.newArrayList();
   public final List tickableTileEntities = Lists.newArrayList();
   private final List addedTileEntityList = Lists.newArrayList();
   private final List tileEntitiesToBeRemoved = Lists.newArrayList();
   public final List playerEntities = Lists.newArrayList();
   public final List weatherEffects = Lists.newArrayList();
   protected final IntHashMap entitiesById = new IntHashMap();
   private final long cloudColour = 16777215L;
   private int skylightSubtracted;
   protected int updateLCG = (new Random()).nextInt();
   protected final int DIST_HASH_MAGIC = 1013904223;
   public float prevRainingStrength;
   public float rainingStrength;
   public float prevThunderingStrength;
   public float thunderingStrength;
   private int lastLightningBolt;
   public final Random rand = new Random();
   public final WorldProvider provider;
   protected PathWorldListener pathListener = new PathWorldListener();
   protected List eventListeners;
   protected IChunkProvider chunkProvider;
   protected final ISaveHandler saveHandler;
   protected WorldInfo worldInfo;
   protected boolean findingSpawnPoint;
   protected MapStorage mapStorage;
   public VillageCollection villageCollectionObj;
   protected LootTableManager lootTable;
   public final Profiler theProfiler;
   private final Calendar theCalendar;
   protected Scoreboard worldScoreboard;
   public final boolean isRemote;
   protected boolean spawnHostileMobs;
   protected boolean spawnPeacefulMobs;
   private boolean processingLoadedTiles;
   private final WorldBorder worldBorder;
   int[] lightUpdateBlockList;
   public boolean restoringBlockSnapshots = false;
   public boolean captureBlockSnapshots = false;
   public ArrayList capturedBlockSnapshots = new ArrayList();
   private CapabilityDispatcher capabilities;
   private WorldCapabilityData capabilityData;
   protected MapStorage perWorldStorage;

   protected World(ISaveHandler var1, WorldInfo var2, WorldProvider var3, Profiler var4, boolean var5) {
      this.eventListeners = Lists.newArrayList(new IWorldEventListener[]{this.pathListener});
      this.theCalendar = Calendar.getInstance();
      this.worldScoreboard = new Scoreboard();
      this.spawnHostileMobs = true;
      this.spawnPeacefulMobs = true;
      this.lightUpdateBlockList = new int['耀'];
      this.saveHandler = saveHandlerIn;
      this.theProfiler = profilerIn;
      this.worldInfo = info;
      this.provider = providerIn;
      this.isRemote = client;
      this.worldBorder = providerIn.createWorldBorder();
      this.perWorldStorage = new MapStorage((ISaveHandler)null);
   }

   public World init() {
      return this;
   }

   public Biome getBiome(BlockPos var1) {
      return this.provider.getBiomeForCoords(pos);
   }

   public Biome getBiomeForCoordsBody(final BlockPos var1) {
      if (this.isBlockLoaded(pos)) {
         Chunk chunk = this.getChunkFromBlockCoords(pos);

         try {
            return chunk.getBiome(pos, this.provider.getBiomeProvider());
         } catch (Throwable var6) {
            CrashReport crashreport = CrashReport.makeCrashReport(var6, "Getting biome");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Coordinates of biome request");
            crashreportcategory.setDetail("Location", new ICrashReportDetail() {
               public String call() throws Exception {
                  return CrashReportCategory.getCoordinateInfo(pos);
               }
            });
            throw new ReportedException(crashreport);
         }
      } else {
         return this.provider.getBiomeProvider().getBiome(pos, Biomes.PLAINS);
      }
   }

   public BiomeProvider getBiomeProvider() {
      return this.provider.getBiomeProvider();
   }

   protected abstract IChunkProvider createChunkProvider();

   public void initialize(WorldSettings var1) {
      this.worldInfo.setServerInitialized(true);
   }

   @Nullable
   public MinecraftServer getMinecraftServer() {
      return null;
   }

   @SideOnly(Side.CLIENT)
   public void setInitialSpawnLocation() {
      this.setSpawnPoint(new BlockPos(8, 64, 8));
   }

   public IBlockState getGroundAboveSeaLevel(BlockPos var1) {
      BlockPos blockpos;
      for(blockpos = new BlockPos(pos.getX(), this.getSeaLevel(), pos.getZ()); !this.isAirBlock(blockpos.up()); blockpos = blockpos.up()) {
         ;
      }

      return this.getBlockState(blockpos);
   }

   private boolean isValid(BlockPos var1) {
      return !this.isOutsideBuildHeight(pos) && pos.getX() >= -30000000 && pos.getZ() >= -30000000 && pos.getX() < 30000000 && pos.getZ() < 30000000;
   }

   private boolean isOutsideBuildHeight(BlockPos var1) {
      return pos.getY() < 0 || pos.getY() >= 256;
   }

   public boolean isAirBlock(BlockPos var1) {
      return this.getBlockState(pos).getBlock().isAir(this.getBlockState(pos), this, pos);
   }

   public boolean isBlockLoaded(BlockPos var1) {
      return this.isBlockLoaded(pos, true);
   }

   public boolean isBlockLoaded(BlockPos var1, boolean var2) {
      return this.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4, allowEmpty);
   }

   public boolean isAreaLoaded(BlockPos var1, int var2) {
      return this.isAreaLoaded(center, radius, true);
   }

   public boolean isAreaLoaded(BlockPos var1, int var2, boolean var3) {
      return this.isAreaLoaded(center.getX() - radius, center.getY() - radius, center.getZ() - radius, center.getX() + radius, center.getY() + radius, center.getZ() + radius, allowEmpty);
   }

   public boolean isAreaLoaded(BlockPos var1, BlockPos var2) {
      return this.isAreaLoaded(from, to, true);
   }

   public boolean isAreaLoaded(BlockPos var1, BlockPos var2, boolean var3) {
      return this.isAreaLoaded(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ(), allowEmpty);
   }

   public boolean isAreaLoaded(StructureBoundingBox var1) {
      return this.isAreaLoaded(box, true);
   }

   public boolean isAreaLoaded(StructureBoundingBox var1, boolean var2) {
      return this.isAreaLoaded(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, allowEmpty);
   }

   private boolean isAreaLoaded(int var1, int var2, int var3, int var4, int var5, int var6, boolean var7) {
      if (yEnd >= 0 && yStart < 256) {
         xStart = xStart >> 4;
         zStart = zStart >> 4;
         xEnd = xEnd >> 4;
         zEnd = zEnd >> 4;

         for(int i = xStart; i <= xEnd; ++i) {
            for(int j = zStart; j <= zEnd; ++j) {
               if (!this.isChunkLoaded(i, j, allowEmpty)) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   protected abstract boolean isChunkLoaded(int var1, int var2, boolean var3);

   public Chunk getChunkFromBlockCoords(BlockPos var1) {
      return this.getChunkFromChunkCoords(pos.getX() >> 4, pos.getZ() >> 4);
   }

   public Chunk getChunkFromChunkCoords(int var1, int var2) {
      return this.chunkProvider.provideChunk(chunkX, chunkZ);
   }

   public boolean setBlockState(BlockPos var1, IBlockState var2, int var3) {
      if (this.isOutsideBuildHeight(pos)) {
         return false;
      } else if (!this.isRemote && this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) {
         return false;
      } else {
         Chunk chunk = this.getChunkFromBlockCoords(pos);
         Block block = newState.getBlock();
         BlockSnapshot blockSnapshot = null;
         if (this.captureBlockSnapshots && !this.isRemote) {
            blockSnapshot = BlockSnapshot.getBlockSnapshot(this, pos, flags);
            this.capturedBlockSnapshots.add(blockSnapshot);
         }

         IBlockState oldState = this.getBlockState(pos);
         int oldLight = oldState.getLightValue(this, pos);
         int oldOpacity = oldState.getLightOpacity(this, pos);
         IBlockState iblockstate = chunk.setBlockState(pos, newState);
         if (iblockstate == null) {
            if (blockSnapshot != null) {
               this.capturedBlockSnapshots.remove(blockSnapshot);
            }

            return false;
         } else {
            if (newState.getLightOpacity(this, pos) != oldOpacity || newState.getLightValue(this, pos) != oldLight) {
               this.theProfiler.startSection("checkLight");
               this.checkLight(pos);
               this.theProfiler.endSection();
            }

            if (blockSnapshot == null) {
               this.markAndNotifyBlock(pos, chunk, iblockstate, newState, flags);
            }

            return true;
         }
      }
   }

   public void markAndNotifyBlock(BlockPos var1, Chunk var2, IBlockState var3, IBlockState var4, int var5) {
      if ((flags & 2) != 0 && (!this.isRemote || (flags & 4) == 0) && (chunk == null || chunk.isPopulated())) {
         this.notifyBlockUpdate(pos, iblockstate, newState, flags);
      }

      if (!this.isRemote && (flags & 1) != 0) {
         this.notifyNeighborsRespectDebug(pos, iblockstate.getBlock());
         if (newState.hasComparatorInputOverride()) {
            this.updateComparatorOutputLevel(pos, newState.getBlock());
         }
      }

   }

   public boolean setBlockToAir(BlockPos var1) {
      return this.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
   }

   public boolean destroyBlock(BlockPos var1, boolean var2) {
      IBlockState iblockstate = this.getBlockState(pos);
      Block block = iblockstate.getBlock();
      if (block.isAir(iblockstate, this, pos)) {
         return false;
      } else {
         this.playEvent(2001, pos, Block.getStateId(iblockstate));
         if (dropBlock) {
            block.dropBlockAsItem(this, pos, iblockstate, 0);
         }

         return this.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
      }
   }

   public boolean setBlockState(BlockPos var1, IBlockState var2) {
      return this.setBlockState(pos, state, 3);
   }

   public void notifyBlockUpdate(BlockPos var1, IBlockState var2, IBlockState var3, int var4) {
      for(int i = 0; i < this.eventListeners.size(); ++i) {
         ((IWorldEventListener)this.eventListeners.get(i)).notifyBlockUpdate(this, pos, oldState, newState, flags);
      }

   }

   public void notifyNeighborsRespectDebug(BlockPos var1, Block var2) {
      if (this.worldInfo.getTerrainType() != WorldType.DEBUG_WORLD) {
         this.notifyNeighborsOfStateChange(pos, blockType);
      }

   }

   public void markBlocksDirtyVertical(int var1, int var2, int var3, int var4) {
      if (x2 > z2) {
         int i = z2;
         z2 = x2;
         x2 = i;
      }

      if (!this.provider.hasNoSky()) {
         for(int j = x2; j <= z2; ++j) {
            this.checkLightFor(EnumSkyBlock.SKY, new BlockPos(x1, j, z1));
         }
      }

      this.markBlockRangeForRenderUpdate(x1, x2, z1, x1, z2, z1);
   }

   public void markBlockRangeForRenderUpdate(BlockPos var1, BlockPos var2) {
      this.markBlockRangeForRenderUpdate(rangeMin.getX(), rangeMin.getY(), rangeMin.getZ(), rangeMax.getX(), rangeMax.getY(), rangeMax.getZ());
   }

   public void markBlockRangeForRenderUpdate(int var1, int var2, int var3, int var4, int var5, int var6) {
      for(int i = 0; i < this.eventListeners.size(); ++i) {
         ((IWorldEventListener)this.eventListeners.get(i)).markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
      }

   }

   public void notifyNeighborsOfStateChange(BlockPos var1, Block var2) {
      if (!ForgeEventFactory.onNeighborNotify(this, pos, this.getBlockState(pos), EnumSet.allOf(EnumFacing.class)).isCanceled()) {
         this.notifyBlockOfStateChange(pos.west(), blockType);
         this.notifyBlockOfStateChange(pos.east(), blockType);
         this.notifyBlockOfStateChange(pos.down(), blockType);
         this.notifyBlockOfStateChange(pos.up(), blockType);
         this.notifyBlockOfStateChange(pos.north(), blockType);
         this.notifyBlockOfStateChange(pos.south(), blockType);
      }
   }

   public void notifyNeighborsOfStateExcept(BlockPos var1, Block var2, EnumFacing var3) {
      EnumSet directions = EnumSet.allOf(EnumFacing.class);
      directions.remove(skipSide);
      if (!ForgeEventFactory.onNeighborNotify(this, pos, this.getBlockState(pos), directions).isCanceled()) {
         if (skipSide != EnumFacing.WEST) {
            this.notifyBlockOfStateChange(pos.west(), blockType);
         }

         if (skipSide != EnumFacing.EAST) {
            this.notifyBlockOfStateChange(pos.east(), blockType);
         }

         if (skipSide != EnumFacing.DOWN) {
            this.notifyBlockOfStateChange(pos.down(), blockType);
         }

         if (skipSide != EnumFacing.UP) {
            this.notifyBlockOfStateChange(pos.up(), blockType);
         }

         if (skipSide != EnumFacing.NORTH) {
            this.notifyBlockOfStateChange(pos.north(), blockType);
         }

         if (skipSide != EnumFacing.SOUTH) {
            this.notifyBlockOfStateChange(pos.south(), blockType);
         }

      }
   }

   public void notifyBlockOfStateChange(BlockPos var1, final Block var2) {
      if (!this.isRemote) {
         IBlockState iblockstate = this.getBlockState(pos);

         try {
            iblockstate.neighborChanged(this, pos, blockIn);
         } catch (Throwable var7) {
            CrashReport crashreport = CrashReport.makeCrashReport(var7, "Exception while updating neighbours");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being updated");
            crashreportcategory.setDetail("Source block type", new ICrashReportDetail() {
               public String call() throws Exception {
                  try {
                     return String.format("ID #%d (%s // %s)", Block.getIdFromBlock(blockIn), blockIn.getUnlocalizedName(), blockIn.getClass().getCanonicalName());
                  } catch (Throwable var2x) {
                     return "ID #" + Block.getIdFromBlock(blockIn);
                  }
               }
            });
            CrashReportCategory.addBlockInfo(crashreportcategory, pos, iblockstate);
            throw new ReportedException(crashreport);
         }
      }

   }

   public boolean isBlockTickPending(BlockPos var1, Block var2) {
      return false;
   }

   public boolean canSeeSky(BlockPos var1) {
      return this.getChunkFromBlockCoords(pos).canSeeSky(pos);
   }

   public boolean canBlockSeeSky(BlockPos var1) {
      if (pos.getY() >= this.getSeaLevel()) {
         return this.canSeeSky(pos);
      } else {
         BlockPos blockpos = new BlockPos(pos.getX(), this.getSeaLevel(), pos.getZ());
         if (!this.canSeeSky(blockpos)) {
            return false;
         } else {
            for(BlockPos var4 = blockpos.down(); var4.getY() > pos.getY(); var4 = var4.down()) {
               IBlockState iblockstate = this.getBlockState(var4);
               if (iblockstate.getBlock().getLightOpacity(iblockstate, this, var4) > 0 && !iblockstate.getMaterial().isLiquid()) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public int getLight(BlockPos var1) {
      if (pos.getY() < 0) {
         return 0;
      } else {
         if (pos.getY() >= 256) {
            pos = new BlockPos(pos.getX(), 255, pos.getZ());
         }

         return this.getChunkFromBlockCoords(pos).getLightSubtracted(pos, 0);
      }
   }

   public int getLightFromNeighbors(BlockPos var1) {
      return this.getLight(pos, true);
   }

   public int getLight(BlockPos var1, boolean var2) {
      if (pos.getX() >= -30000000 && pos.getZ() >= -30000000 && pos.getX() < 30000000 && pos.getZ() < 30000000) {
         if (checkNeighbors && this.getBlockState(pos).useNeighborBrightness()) {
            int i1 = this.getLight(pos.up(), false);
            int i = this.getLight(pos.east(), false);
            int j = this.getLight(pos.west(), false);
            int k = this.getLight(pos.south(), false);
            int l = this.getLight(pos.north(), false);
            if (i > i1) {
               i1 = i;
            }

            if (j > i1) {
               i1 = j;
            }

            if (k > i1) {
               i1 = k;
            }

            if (l > i1) {
               i1 = l;
            }

            return i1;
         } else if (pos.getY() < 0) {
            return 0;
         } else {
            if (pos.getY() >= 256) {
               pos = new BlockPos(pos.getX(), 255, pos.getZ());
            }

            Chunk chunk = this.getChunkFromBlockCoords(pos);
            return chunk.getLightSubtracted(pos, this.skylightSubtracted);
         }
      } else {
         return 15;
      }
   }

   public BlockPos getHeight(BlockPos var1) {
      return new BlockPos(pos.getX(), this.getHeight(pos.getX(), pos.getZ()), pos.getZ());
   }

   public int getHeight(int var1, int var2) {
      int i;
      if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) {
         if (this.isChunkLoaded(x >> 4, z >> 4, true)) {
            i = this.getChunkFromChunkCoords(x >> 4, z >> 4).getHeightValue(x & 15, z & 15);
         } else {
            i = 0;
         }
      } else {
         i = this.getSeaLevel() + 1;
      }

      return i;
   }

   /** @deprecated */
   @Deprecated
   public int getChunksLowestHorizon(int var1, int var2) {
      if (x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000) {
         if (!this.isChunkLoaded(x >> 4, z >> 4, true)) {
            return 0;
         } else {
            Chunk chunk = this.getChunkFromChunkCoords(x >> 4, z >> 4);
            return chunk.getLowestHeight();
         }
      } else {
         return this.getSeaLevel() + 1;
      }
   }

   @SideOnly(Side.CLIENT)
   public int getLightFromNeighborsFor(EnumSkyBlock var1, BlockPos var2) {
      if (this.provider.hasNoSky() && type == EnumSkyBlock.SKY) {
         return 0;
      } else {
         if (pos.getY() < 0) {
            pos = new BlockPos(pos.getX(), 0, pos.getZ());
         }

         if (!this.isValid(pos)) {
            return type.defaultLightValue;
         } else if (!this.isBlockLoaded(pos)) {
            return type.defaultLightValue;
         } else if (this.getBlockState(pos).useNeighborBrightness()) {
            int i1 = this.getLightFor(type, pos.up());
            int i = this.getLightFor(type, pos.east());
            int j = this.getLightFor(type, pos.west());
            int k = this.getLightFor(type, pos.south());
            int l = this.getLightFor(type, pos.north());
            if (i > i1) {
               i1 = i;
            }

            if (j > i1) {
               i1 = j;
            }

            if (k > i1) {
               i1 = k;
            }

            if (l > i1) {
               i1 = l;
            }

            return i1;
         } else {
            Chunk chunk = this.getChunkFromBlockCoords(pos);
            return chunk.getLightFor(type, pos);
         }
      }
   }

   public int getLightFor(EnumSkyBlock var1, BlockPos var2) {
      if (pos.getY() < 0) {
         pos = new BlockPos(pos.getX(), 0, pos.getZ());
      }

      if (!this.isValid(pos)) {
         return type.defaultLightValue;
      } else if (!this.isBlockLoaded(pos)) {
         return type.defaultLightValue;
      } else {
         Chunk chunk = this.getChunkFromBlockCoords(pos);
         return chunk.getLightFor(type, pos);
      }
   }

   public void setLightFor(EnumSkyBlock var1, BlockPos var2, int var3) {
      if (this.isValid(pos) && this.isBlockLoaded(pos)) {
         Chunk chunk = this.getChunkFromBlockCoords(pos);
         chunk.setLightFor(type, pos, lightValue);
         this.notifyLightSet(pos);
      }

   }

   public void notifyLightSet(BlockPos var1) {
      for(int i = 0; i < this.eventListeners.size(); ++i) {
         ((IWorldEventListener)this.eventListeners.get(i)).notifyLightSet(pos);
      }

   }

   @SideOnly(Side.CLIENT)
   public int getCombinedLight(BlockPos var1, int var2) {
      int i = this.getLightFromNeighborsFor(EnumSkyBlock.SKY, pos);
      int j = this.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, pos);
      if (j < lightValue) {
         j = lightValue;
      }

      return i << 20 | j << 4;
   }

   public float getLightBrightness(BlockPos var1) {
      return this.provider.getLightBrightnessTable()[this.getLightFromNeighbors(pos)];
   }

   public IBlockState getBlockState(BlockPos var1) {
      if (this.isOutsideBuildHeight(pos)) {
         return Blocks.AIR.getDefaultState();
      } else {
         Chunk chunk = this.getChunkFromBlockCoords(pos);
         return chunk.getBlockState(pos);
      }
   }

   public boolean isDaytime() {
      return this.provider.isDaytime();
   }

   @Nullable
   public RayTraceResult rayTraceBlocks(Vec3d var1, Vec3d var2) {
      return this.rayTraceBlocks(start, end, false, false, false);
   }

   @Nullable
   public RayTraceResult rayTraceBlocks(Vec3d var1, Vec3d var2, boolean var3) {
      return this.rayTraceBlocks(start, end, stopOnLiquid, false, false);
   }

   @Nullable
   public RayTraceResult rayTraceBlocks(Vec3d var1, Vec3d var2, boolean var3, boolean var4, boolean var5) {
      if (!Double.isNaN(vec31.xCoord) && !Double.isNaN(vec31.yCoord) && !Double.isNaN(vec31.zCoord)) {
         if (!Double.isNaN(vec32.xCoord) && !Double.isNaN(vec32.yCoord) && !Double.isNaN(vec32.zCoord)) {
            int i = MathHelper.floor(vec32.xCoord);
            int j = MathHelper.floor(vec32.yCoord);
            int k = MathHelper.floor(vec32.zCoord);
            int l = MathHelper.floor(vec31.xCoord);
            int i1 = MathHelper.floor(vec31.yCoord);
            int j1 = MathHelper.floor(vec31.zCoord);
            BlockPos blockpos = new BlockPos(l, i1, j1);
            IBlockState iblockstate = this.getBlockState(blockpos);
            Block block = iblockstate.getBlock();
            if ((!ignoreBlockWithoutBoundingBox || iblockstate.getCollisionBoundingBox(this, blockpos) != Block.NULL_AABB) && block.canCollideCheck(iblockstate, stopOnLiquid)) {
               RayTraceResult raytraceresult = iblockstate.collisionRayTrace(this, blockpos, vec31, vec32);
               if (raytraceresult != null) {
                  return raytraceresult;
               }
            }

            RayTraceResult raytraceresult2 = null;
            int k1 = 200;

            while(k1-- >= 0) {
               if (Double.isNaN(vec31.xCoord) || Double.isNaN(vec31.yCoord) || Double.isNaN(vec31.zCoord)) {
                  return null;
               }

               if (l == i && i1 == j && j1 == k) {
                  return returnLastUncollidableBlock ? raytraceresult2 : null;
               }

               boolean flag2 = true;
               boolean flag = true;
               boolean flag1 = true;
               double d0 = 999.0D;
               double d1 = 999.0D;
               double d2 = 999.0D;
               if (i > l) {
                  d0 = (double)l + 1.0D;
               } else if (i < l) {
                  d0 = (double)l + 0.0D;
               } else {
                  flag2 = false;
               }

               if (j > i1) {
                  d1 = (double)i1 + 1.0D;
               } else if (j < i1) {
                  d1 = (double)i1 + 0.0D;
               } else {
                  flag = false;
               }

               if (k > j1) {
                  d2 = (double)j1 + 1.0D;
               } else if (k < j1) {
                  d2 = (double)j1 + 0.0D;
               } else {
                  flag1 = false;
               }

               double d3 = 999.0D;
               double d4 = 999.0D;
               double d5 = 999.0D;
               double d6 = vec32.xCoord - vec31.xCoord;
               double d7 = vec32.yCoord - vec31.yCoord;
               double d8 = vec32.zCoord - vec31.zCoord;
               if (flag2) {
                  d3 = (d0 - vec31.xCoord) / d6;
               }

               if (flag) {
                  d4 = (d1 - vec31.yCoord) / d7;
               }

               if (flag1) {
                  d5 = (d2 - vec31.zCoord) / d8;
               }

               if (d3 == -0.0D) {
                  d3 = -1.0E-4D;
               }

               if (d4 == -0.0D) {
                  d4 = -1.0E-4D;
               }

               if (d5 == -0.0D) {
                  d5 = -1.0E-4D;
               }

               EnumFacing enumfacing;
               if (d3 < d4 && d3 < d5) {
                  enumfacing = i > l ? EnumFacing.WEST : EnumFacing.EAST;
                  vec31 = new Vec3d(d0, vec31.yCoord + d7 * d3, vec31.zCoord + d8 * d3);
               } else if (d4 < d5) {
                  enumfacing = j > i1 ? EnumFacing.DOWN : EnumFacing.UP;
                  vec31 = new Vec3d(vec31.xCoord + d6 * d4, d1, vec31.zCoord + d8 * d4);
               } else {
                  enumfacing = k > j1 ? EnumFacing.NORTH : EnumFacing.SOUTH;
                  vec31 = new Vec3d(vec31.xCoord + d6 * d5, vec31.yCoord + d7 * d5, d2);
               }

               l = MathHelper.floor(vec31.xCoord) - (enumfacing == EnumFacing.EAST ? 1 : 0);
               i1 = MathHelper.floor(vec31.yCoord) - (enumfacing == EnumFacing.UP ? 1 : 0);
               j1 = MathHelper.floor(vec31.zCoord) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
               blockpos = new BlockPos(l, i1, j1);
               IBlockState iblockstate1 = this.getBlockState(blockpos);
               Block block1 = iblockstate1.getBlock();
               if (!ignoreBlockWithoutBoundingBox || iblockstate1.getMaterial() == Material.PORTAL || iblockstate1.getCollisionBoundingBox(this, blockpos) != Block.NULL_AABB) {
                  if (block1.canCollideCheck(iblockstate1, stopOnLiquid)) {
                     RayTraceResult raytraceresult1 = iblockstate1.collisionRayTrace(this, blockpos, vec31, vec32);
                     if (raytraceresult1 != null) {
                        return raytraceresult1;
                     }
                  } else {
                     raytraceresult2 = new RayTraceResult(RayTraceResult.Type.MISS, vec31, enumfacing, blockpos);
                  }
               }
            }

            return returnLastUncollidableBlock ? raytraceresult2 : null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public void playSound(@Nullable EntityPlayer var1, BlockPos var2, SoundEvent var3, SoundCategory var4, float var5, float var6) {
      this.playSound(player, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, soundIn, category, volume, pitch);
   }

   public void playSound(@Nullable EntityPlayer var1, double var2, double var4, double var6, SoundEvent var8, SoundCategory var9, float var10, float var11) {
      PlaySoundAtEntityEvent event = ForgeEventFactory.onPlaySoundAtEntity(player, soundIn, category, volume, pitch);
      if (!event.isCanceled() && event.getSound() != null) {
         soundIn = event.getSound();
         category = event.getCategory();
         volume = event.getVolume();
         pitch = event.getPitch();

         for(int i = 0; i < this.eventListeners.size(); ++i) {
            ((IWorldEventListener)this.eventListeners.get(i)).playSoundToAllNearExcept(player, soundIn, category, x, y, z, volume, pitch);
         }

      }
   }

   public void playSound(double var1, double var3, double var5, SoundEvent var7, SoundCategory var8, float var9, float var10, boolean var11) {
   }

   public void playRecord(BlockPos var1, @Nullable SoundEvent var2) {
      for(int i = 0; i < this.eventListeners.size(); ++i) {
         ((IWorldEventListener)this.eventListeners.get(i)).playRecord(soundEventIn, blockPositionIn);
      }

   }

   public void spawnParticle(EnumParticleTypes var1, double var2, double var4, double var6, double var8, double var10, double var12, int... var14) {
      this.spawnParticle(particleType.getParticleID(), particleType.getShouldIgnoreRange(), xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
   }

   @SideOnly(Side.CLIENT)
   public void spawnParticle(EnumParticleTypes var1, boolean var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
      this.spawnParticle(particleType.getParticleID(), particleType.getShouldIgnoreRange() | ignoreRange, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
   }

   private void spawnParticle(int var1, boolean var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
      for(int i = 0; i < this.eventListeners.size(); ++i) {
         ((IWorldEventListener)this.eventListeners.get(i)).spawnParticle(particleID, ignoreRange, xCood, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
      }

   }

   public boolean addWeatherEffect(Entity var1) {
      this.weatherEffects.add(entityIn);
      return true;
   }

   public boolean spawnEntity(Entity var1) {
      if (this.isRemote || entityIn != null && (!(entityIn instanceof EntityItem) || !this.restoringBlockSnapshots)) {
         int i = MathHelper.floor(entityIn.posX / 16.0D);
         int j = MathHelper.floor(entityIn.posZ / 16.0D);
         boolean flag = entityIn.forceSpawn;
         if (entityIn instanceof EntityPlayer) {
            flag = true;
         }

         if (!flag && !this.isChunkLoaded(i, j, false)) {
            return false;
         } else {
            if (entityIn instanceof EntityPlayer) {
               EntityPlayer entityplayer = (EntityPlayer)entityIn;
               this.playerEntities.add(entityplayer);
               this.updateAllPlayersSleepingFlag();
            }

            if (MinecraftForge.EVENT_BUS.post(new EntityJoinWorldEvent(entityIn, this)) && !flag) {
               return false;
            } else {
               this.getChunkFromChunkCoords(i, j).addEntity(entityIn);
               this.loadedEntityList.add(entityIn);
               this.onEntityAdded(entityIn);
               return true;
            }
         }
      } else {
         return false;
      }
   }

   public void onEntityAdded(Entity var1) {
      for(int i = 0; i < this.eventListeners.size(); ++i) {
         ((IWorldEventListener)this.eventListeners.get(i)).onEntityAdded(entityIn);
      }

   }

   public void onEntityRemoved(Entity var1) {
      for(int i = 0; i < this.eventListeners.size(); ++i) {
         ((IWorldEventListener)this.eventListeners.get(i)).onEntityRemoved(entityIn);
      }

   }

   public void removeEntity(Entity var1) {
      if (entityIn.isBeingRidden()) {
         entityIn.removePassengers();
      }

      if (entityIn.isRiding()) {
         entityIn.dismountRidingEntity();
      }

      entityIn.setDead();
      if (entityIn instanceof EntityPlayer) {
         this.playerEntities.remove(entityIn);
         this.updateAllPlayersSleepingFlag();
         this.onEntityRemoved(entityIn);
      }

   }

   public void removeEntityDangerously(Entity var1) {
      entityIn.setDropItemsWhenDead(false);
      entityIn.setDead();
      if (entityIn instanceof EntityPlayer) {
         this.playerEntities.remove(entityIn);
         this.updateAllPlayersSleepingFlag();
      }

      int i = entityIn.chunkCoordX;
      int j = entityIn.chunkCoordZ;
      if (entityIn.addedToChunk && this.isChunkLoaded(i, j, true)) {
         this.getChunkFromChunkCoords(i, j).removeEntity(entityIn);
      }

      this.loadedEntityList.remove(entityIn);
      this.onEntityRemoved(entityIn);
   }

   public void addEventListener(IWorldEventListener var1) {
      this.eventListeners.add(listener);
   }

   public List getCollisionBoxes(@Nullable Entity var1, AxisAlignedBB var2) {
      List list = Lists.newArrayList();
      int i = MathHelper.floor(aabb.minX) - 1;
      int j = MathHelper.ceil(aabb.maxX) + 1;
      int k = MathHelper.floor(aabb.minY) - 1;
      int l = MathHelper.ceil(aabb.maxY) + 1;
      int i1 = MathHelper.floor(aabb.minZ) - 1;
      int j1 = MathHelper.ceil(aabb.maxZ) + 1;
      WorldBorder worldborder = this.getWorldBorder();
      boolean flag = entityIn != null && entityIn.isOutsideBorder();
      boolean flag1 = entityIn != null && this.isInsideBorder(worldborder, entityIn);
      IBlockState iblockstate = Blocks.STONE.getDefaultState();
      BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

      for(int k1 = i; k1 < j; ++k1) {
         for(int l1 = i1; l1 < j1; ++l1) {
            int i2 = (k1 != i && k1 != j - 1 ? 0 : 1) + (l1 != i1 && l1 != j1 - 1 ? 0 : 1);
            if (i2 != 2 && this.isBlockLoaded(blockpos$pooledmutableblockpos.setPos(k1, 64, l1))) {
               for(int j2 = k; j2 < l; ++j2) {
                  if (i2 <= 0 || j2 != k && j2 != l - 1) {
                     blockpos$pooledmutableblockpos.setPos(k1, j2, l1);
                     if (entityIn != null) {
                        if (flag && flag1) {
                           entityIn.setOutsideBorder(false);
                        } else if (!flag && !flag1) {
                           entityIn.setOutsideBorder(true);
                        }
                     }

                     IBlockState iblockstate1 = iblockstate;
                     if (worldborder.contains(blockpos$pooledmutableblockpos) || !flag1) {
                        iblockstate1 = this.getBlockState(blockpos$pooledmutableblockpos);
                     }

                     iblockstate1.addCollisionBoxToList(this, blockpos$pooledmutableblockpos, aabb, list, entityIn);
                  }
               }
            }
         }
      }

      blockpos$pooledmutableblockpos.release();
      if (entityIn != null) {
         List list1 = this.getEntitiesWithinAABBExcludingEntity(entityIn, aabb.expandXyz(0.25D));

         for(int k2 = 0; k2 < list1.size(); ++k2) {
            Entity entity = (Entity)list1.get(k2);
            if (!entityIn.isRidingSameEntity(entity)) {
               AxisAlignedBB axisalignedbb = entity.getCollisionBoundingBox();
               if (axisalignedbb != null && axisalignedbb.intersectsWith(aabb)) {
                  list.add(axisalignedbb);
               }

               axisalignedbb = entityIn.getCollisionBox(entity);
               if (axisalignedbb != null && axisalignedbb.intersectsWith(aabb)) {
                  list.add(axisalignedbb);
               }
            }
         }
      }

      MinecraftForge.EVENT_BUS.post(new GetCollisionBoxesEvent(this, entityIn, aabb, list));
      return list;
   }

   public boolean isInsideBorder(WorldBorder var1, Entity var2) {
      double d0 = worldBorderIn.minX();
      double d1 = worldBorderIn.minZ();
      double d2 = worldBorderIn.maxX();
      double d3 = worldBorderIn.maxZ();
      if (entityIn.isOutsideBorder()) {
         ++d0;
         ++d1;
         --d2;
         --d3;
      } else {
         --d0;
         --d1;
         ++d2;
         ++d3;
      }

      return entityIn.posX > d0 && entityIn.posX < d2 && entityIn.posZ > d1 && entityIn.posZ < d3;
   }

   public List getCollisionBoxes(AxisAlignedBB var1) {
      List list = Lists.newArrayList();
      int i = MathHelper.floor(bb.minX) - 1;
      int j = MathHelper.ceil(bb.maxX) + 1;
      int k = MathHelper.floor(bb.minY) - 1;
      int l = MathHelper.ceil(bb.maxY) + 1;
      int i1 = MathHelper.floor(bb.minZ) - 1;
      int j1 = MathHelper.ceil(bb.maxZ) + 1;
      BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

      for(int k1 = i; k1 < j; ++k1) {
         for(int l1 = i1; l1 < j1; ++l1) {
            int i2 = (k1 != i && k1 != j - 1 ? 0 : 1) + (l1 != i1 && l1 != j1 - 1 ? 0 : 1);
            if (i2 != 2 && this.isBlockLoaded(blockpos$pooledmutableblockpos.setPos(k1, 64, l1))) {
               for(int j2 = k; j2 < l; ++j2) {
                  if (i2 <= 0 || j2 != k && j2 != l - 1) {
                     blockpos$pooledmutableblockpos.setPos(k1, j2, l1);
                     IBlockState iblockstate;
                     if (k1 >= -30000000 && k1 < 30000000 && l1 >= -30000000 && l1 < 30000000) {
                        iblockstate = this.getBlockState(blockpos$pooledmutableblockpos);
                     } else {
                        iblockstate = Blocks.BEDROCK.getDefaultState();
                     }

                     iblockstate.addCollisionBoxToList(this, blockpos$pooledmutableblockpos, bb, list, (Entity)null);
                  }
               }
            }
         }
      }

      blockpos$pooledmutableblockpos.release();
      return list;
   }

   public void removeEventListener(IWorldEventListener var1) {
      this.eventListeners.remove(listener);
   }

   public boolean collidesWithAnyBlock(AxisAlignedBB var1) {
      List list = Lists.newArrayList();
      int i = MathHelper.floor(bbox.minX) - 1;
      int j = MathHelper.ceil(bbox.maxX) + 1;
      int k = MathHelper.floor(bbox.minY) - 1;
      int l = MathHelper.ceil(bbox.maxY) + 1;
      int i1 = MathHelper.floor(bbox.minZ) - 1;
      int j1 = MathHelper.ceil(bbox.maxZ) + 1;
      BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

      try {
         for(int k1 = i; k1 < j; ++k1) {
            for(int l1 = i1; l1 < j1; ++l1) {
               int i2 = (k1 != i && k1 != j - 1 ? 0 : 1) + (l1 != i1 && l1 != j1 - 1 ? 0 : 1);
               if (i2 != 2 && this.isBlockLoaded(blockpos$pooledmutableblockpos.setPos(k1, 64, l1))) {
                  for(int j2 = k; j2 < l; ++j2) {
                     if (i2 <= 0 || j2 != k && j2 != l - 1) {
                        blockpos$pooledmutableblockpos.setPos(k1, j2, l1);
                        if (k1 < -30000000 || k1 >= 30000000 || l1 < -30000000 || l1 >= 30000000) {
                           boolean flag1 = true;
                           boolean var22 = flag1;
                           return var22;
                        }

                        IBlockState iblockstate = this.getBlockState(blockpos$pooledmutableblockpos);
                        iblockstate.addCollisionBoxToList(this, blockpos$pooledmutableblockpos, bbox, list, (Entity)null);
                        if (!list.isEmpty()) {
                           boolean flag = true;
                           boolean var16 = flag;
                           return var16;
                        }
                     }
                  }
               }
            }
         }

         boolean var20 = false;
         return var20;
      } finally {
         blockpos$pooledmutableblockpos.release();
      }
   }

   public int calculateSkylightSubtracted(float var1) {
      float f = this.provider.getSunBrightnessFactor(partialTicks);
      f = 1.0F - f;
      return (int)(f * 11.0F);
   }

   public float getSunBrightnessFactor(float var1) {
      float f = this.getCelestialAngle(partialTicks);
      float f1 = 1.0F - (MathHelper.cos(f * 6.2831855F) * 2.0F + 0.5F);
      f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
      f1 = 1.0F - f1;
      f1 = (float)((double)f1 * (1.0D - (double)(this.getRainStrength(partialTicks) * 5.0F) / 16.0D));
      f1 = (float)((double)f1 * (1.0D - (double)(this.getThunderStrength(partialTicks) * 5.0F) / 16.0D));
      return f1;
   }

   @SideOnly(Side.CLIENT)
   public float getSunBrightness(float var1) {
      return this.provider.getSunBrightness(p_72971_1_);
   }

   @SideOnly(Side.CLIENT)
   public float getSunBrightnessBody(float var1) {
      float f = this.getCelestialAngle(p_72971_1_);
      float f1 = 1.0F - (MathHelper.cos(f * 6.2831855F) * 2.0F + 0.2F);
      f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
      f1 = 1.0F - f1;
      f1 = (float)((double)f1 * (1.0D - (double)(this.getRainStrength(p_72971_1_) * 5.0F) / 16.0D));
      f1 = (float)((double)f1 * (1.0D - (double)(this.getThunderStrength(p_72971_1_) * 5.0F) / 16.0D));
      return f1 * 0.8F + 0.2F;
   }

   @SideOnly(Side.CLIENT)
   public Vec3d getSkyColor(Entity var1, float var2) {
      return this.provider.getSkyColor(entityIn, partialTicks);
   }

   @SideOnly(Side.CLIENT)
   public Vec3d getSkyColorBody(Entity var1, float var2) {
      float f = this.getCelestialAngle(partialTicks);
      float f1 = MathHelper.cos(f * 6.2831855F) * 2.0F + 0.5F;
      f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
      int i = MathHelper.floor(entityIn.posX);
      int j = MathHelper.floor(entityIn.posY);
      int k = MathHelper.floor(entityIn.posZ);
      BlockPos blockpos = new BlockPos(i, j, k);
      int l = ForgeHooksClient.getSkyBlendColour(this, blockpos);
      float f3 = (float)(l >> 16 & 255) / 255.0F;
      float f4 = (float)(l >> 8 & 255) / 255.0F;
      float f5 = (float)(l & 255) / 255.0F;
      f3 = f3 * f1;
      f4 = f4 * f1;
      f5 = f5 * f1;
      float f6 = this.getRainStrength(partialTicks);
      if (f6 > 0.0F) {
         float f7 = (f3 * 0.3F + f4 * 0.59F + f5 * 0.11F) * 0.6F;
         float f8 = 1.0F - f6 * 0.75F;
         f3 = f3 * f8 + f7 * (1.0F - f8);
         f4 = f4 * f8 + f7 * (1.0F - f8);
         f5 = f5 * f8 + f7 * (1.0F - f8);
      }

      float f10 = this.getThunderStrength(partialTicks);
      if (f10 > 0.0F) {
         float f11 = (f3 * 0.3F + f4 * 0.59F + f5 * 0.11F) * 0.2F;
         float f9 = 1.0F - f10 * 0.75F;
         f3 = f3 * f9 + f11 * (1.0F - f9);
         f4 = f4 * f9 + f11 * (1.0F - f9);
         f5 = f5 * f9 + f11 * (1.0F - f9);
      }

      if (this.lastLightningBolt > 0) {
         float f12 = (float)this.lastLightningBolt - partialTicks;
         if (f12 > 1.0F) {
            f12 = 1.0F;
         }

         f12 = f12 * 0.45F;
         f3 = f3 * (1.0F - f12) + 0.8F * f12;
         f4 = f4 * (1.0F - f12) + 0.8F * f12;
         f5 = f5 * (1.0F - f12) + 1.0F * f12;
      }

      return new Vec3d((double)f3, (double)f4, (double)f5);
   }

   public float getCelestialAngle(float var1) {
      return this.provider.calculateCelestialAngle(this.worldInfo.getWorldTime(), partialTicks);
   }

   @SideOnly(Side.CLIENT)
   public int getMoonPhase() {
      return this.provider.getMoonPhase(this.worldInfo.getWorldTime());
   }

   public float getCurrentMoonPhaseFactor() {
      return this.provider.getCurrentMoonPhaseFactor();
   }

   public float getCurrentMoonPhaseFactorBody() {
      return WorldProvider.MOON_PHASE_FACTORS[this.provider.getMoonPhase(this.worldInfo.getWorldTime())];
   }

   public float getCelestialAngleRadians(float var1) {
      float f = this.getCelestialAngle(partialTicks);
      return f * 6.2831855F;
   }

   @SideOnly(Side.CLIENT)
   public Vec3d getCloudColour(float var1) {
      return this.provider.getCloudColor(partialTicks);
   }

   @SideOnly(Side.CLIENT)
   public Vec3d getCloudColorBody(float var1) {
      float f = this.getCelestialAngle(partialTicks);
      float f1 = MathHelper.cos(f * 6.2831855F) * 2.0F + 0.5F;
      f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
      float f2 = 1.0F;
      float f3 = 1.0F;
      float f4 = 1.0F;
      float f5 = this.getRainStrength(partialTicks);
      if (f5 > 0.0F) {
         float f6 = (f2 * 0.3F + f3 * 0.59F + f4 * 0.11F) * 0.6F;
         float f7 = 1.0F - f5 * 0.95F;
         f2 = f2 * f7 + f6 * (1.0F - f7);
         f3 = f3 * f7 + f6 * (1.0F - f7);
         f4 = f4 * f7 + f6 * (1.0F - f7);
      }

      f2 = f2 * (f1 * 0.9F + 0.1F);
      f3 = f3 * (f1 * 0.9F + 0.1F);
      f4 = f4 * (f1 * 0.85F + 0.15F);
      float f9 = this.getThunderStrength(partialTicks);
      if (f9 > 0.0F) {
         float f10 = (f2 * 0.3F + f3 * 0.59F + f4 * 0.11F) * 0.2F;
         float f8 = 1.0F - f9 * 0.95F;
         f2 = f2 * f8 + f10 * (1.0F - f8);
         f3 = f3 * f8 + f10 * (1.0F - f8);
         f4 = f4 * f8 + f10 * (1.0F - f8);
      }

      return new Vec3d((double)f2, (double)f3, (double)f4);
   }

   @SideOnly(Side.CLIENT)
   public Vec3d getFogColor(float var1) {
      float f = this.getCelestialAngle(partialTicks);
      return this.provider.getFogColor(f, partialTicks);
   }

   public BlockPos getPrecipitationHeight(BlockPos var1) {
      return this.getChunkFromBlockCoords(pos).getPrecipitationHeight(pos);
   }

   public BlockPos getTopSolidOrLiquidBlock(BlockPos var1) {
      Chunk chunk = this.getChunkFromBlockCoords(pos);

      BlockPos blockpos;
      BlockPos blockpos1;
      for(blockpos = new BlockPos(pos.getX(), chunk.getTopFilledSegment() + 16, pos.getZ()); blockpos.getY() >= 0; blockpos = blockpos1) {
         blockpos1 = blockpos.down();
         IBlockState state = chunk.getBlockState(blockpos1);
         if (state.getMaterial().blocksMovement() && !state.getBlock().isLeaves(state, this, blockpos1) && !state.getBlock().isFoliage(this, blockpos1)) {
            break;
         }
      }

      return blockpos;
   }

   @SideOnly(Side.CLIENT)
   public float getStarBrightness(float var1) {
      return this.provider.getStarBrightness(partialTicks);
   }

   @SideOnly(Side.CLIENT)
   public float getStarBrightnessBody(float var1) {
      float f = this.getCelestialAngle(partialTicks);
      float f1 = 1.0F - (MathHelper.cos(f * 6.2831855F) * 2.0F + 0.25F);
      f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
      return f1 * f1 * 0.5F;
   }

   public boolean isUpdateScheduled(BlockPos var1, Block var2) {
      return true;
   }

   public void scheduleUpdate(BlockPos var1, Block var2, int var3) {
   }

   public void updateBlockTick(BlockPos var1, Block var2, int var3, int var4) {
   }

   public void scheduleBlockUpdate(BlockPos var1, Block var2, int var3, int var4) {
   }

   public void updateEntities() {
      this.theProfiler.startSection("entities");
      this.theProfiler.startSection("global");

      for(int i = 0; i < this.weatherEffects.size(); ++i) {
         Entity entity = (Entity)this.weatherEffects.get(i);

         try {
            ++entity.ticksExisted;
            entity.onUpdate();
         } catch (Throwable var9) {
            CrashReport crashreport = CrashReport.makeCrashReport(var9, "Ticking entity");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being ticked");
            if (entity == null) {
               crashreportcategory.addCrashSection("Entity", "~~NULL~~");
            } else {
               entity.addEntityCrashInfo(crashreportcategory);
            }

            if (!ForgeModContainer.removeErroringEntities) {
               throw new ReportedException(crashreport);
            }

            FMLLog.severe(crashreport.getCompleteReport(), new Object[0]);
            this.removeEntity(entity);
         }

         if (entity.isDead) {
            this.weatherEffects.remove(i--);
         }
      }

      this.theProfiler.endStartSection("remove");
      this.loadedEntityList.removeAll(this.unloadedEntityList);

      for(int k = 0; k < this.unloadedEntityList.size(); ++k) {
         Entity entity1 = (Entity)this.unloadedEntityList.get(k);
         int j = entity1.chunkCoordX;
         int k1 = entity1.chunkCoordZ;
         if (entity1.addedToChunk && this.isChunkLoaded(j, k1, true)) {
            this.getChunkFromChunkCoords(j, k1).removeEntity(entity1);
         }
      }

      for(int l = 0; l < this.unloadedEntityList.size(); ++l) {
         this.onEntityRemoved((Entity)this.unloadedEntityList.get(l));
      }

      this.unloadedEntityList.clear();
      this.tickPlayers();
      this.theProfiler.endStartSection("regular");

      for(int i1 = 0; i1 < this.loadedEntityList.size(); ++i1) {
         Entity entity2 = (Entity)this.loadedEntityList.get(i1);
         Entity entity3 = entity2.getRidingEntity();
         if (entity3 != null) {
            if (!entity3.isDead && entity3.isPassenger(entity2)) {
               continue;
            }

            entity2.dismountRidingEntity();
         }

         this.theProfiler.startSection("tick");
         if (!entity2.isDead && !(entity2 instanceof EntityPlayerMP)) {
            try {
               this.updateEntity(entity2);
            } catch (Throwable var8) {
               CrashReport crashreport1 = CrashReport.makeCrashReport(var8, "Ticking entity");
               CrashReportCategory crashreportcategory1 = crashreport1.makeCategory("Entity being ticked");
               entity2.addEntityCrashInfo(crashreportcategory1);
               if (!ForgeModContainer.removeErroringEntities) {
                  throw new ReportedException(crashreport1);
               }

               FMLLog.severe(crashreport1.getCompleteReport(), new Object[0]);
               this.removeEntity(entity2);
            }
         }

         this.theProfiler.endSection();
         this.theProfiler.startSection("remove");
         if (entity2.isDead) {
            int l1 = entity2.chunkCoordX;
            int i2 = entity2.chunkCoordZ;
            if (entity2.addedToChunk && this.isChunkLoaded(l1, i2, true)) {
               this.getChunkFromChunkCoords(l1, i2).removeEntity(entity2);
            }

            this.loadedEntityList.remove(i1--);
            this.onEntityRemoved(entity2);
         }

         this.theProfiler.endSection();
      }

      this.theProfiler.endStartSection("blockEntities");
      this.processingLoadedTiles = true;
      Iterator iterator = this.tickableTileEntities.iterator();

      while(iterator.hasNext()) {
         TileEntity tileentity = (TileEntity)iterator.next();
         if (!tileentity.isInvalid() && tileentity.hasWorld()) {
            BlockPos blockpos = tileentity.getPos();
            if (this.isBlockLoaded(blockpos, false) && this.worldBorder.contains(blockpos)) {
               try {
                  this.theProfiler.startSection(tileentity.getClass().getSimpleName());
                  ((ITickable)tileentity).update();
                  this.theProfiler.endSection();
               } catch (Throwable var7) {
                  CrashReport crashreport2 = CrashReport.makeCrashReport(var7, "Ticking block entity");
                  CrashReportCategory crashreportcategory2 = crashreport2.makeCategory("Block entity being ticked");
                  tileentity.addInfoToCrashReport(crashreportcategory2);
                  if (!ForgeModContainer.removeErroringTileEntities) {
                     throw new ReportedException(crashreport2);
                  }

                  FMLLog.severe(crashreport2.getCompleteReport(), new Object[0]);
                  tileentity.invalidate();
                  this.removeTileEntity(tileentity.getPos());
               }
            }
         }

         if (tileentity.isInvalid()) {
            iterator.remove();
            this.loadedTileEntityList.remove(tileentity);
            if (this.isBlockLoaded(tileentity.getPos())) {
               Chunk chunk = this.getChunkFromBlockCoords(tileentity.getPos());
               if (chunk.getTileEntity(tileentity.getPos(), Chunk.EnumCreateEntityType.CHECK) == tileentity) {
                  chunk.removeTileEntity(tileentity.getPos());
               }
            }
         }
      }

      if (!this.tileEntitiesToBeRemoved.isEmpty()) {
         for(Object tile : this.tileEntitiesToBeRemoved) {
            ((TileEntity)tile).onChunkUnload();
         }

         this.tickableTileEntities.removeAll(this.tileEntitiesToBeRemoved);
         this.loadedTileEntityList.removeAll(this.tileEntitiesToBeRemoved);
         this.tileEntitiesToBeRemoved.clear();
      }

      this.processingLoadedTiles = false;
      this.theProfiler.endStartSection("pendingBlockEntities");
      if (!this.addedTileEntityList.isEmpty()) {
         for(int j1 = 0; j1 < this.addedTileEntityList.size(); ++j1) {
            TileEntity tileentity1 = (TileEntity)this.addedTileEntityList.get(j1);
            if (!tileentity1.isInvalid()) {
               if (!this.loadedTileEntityList.contains(tileentity1)) {
                  this.addTileEntity(tileentity1);
               }

               if (this.isBlockLoaded(tileentity1.getPos())) {
                  Chunk chunk = this.getChunkFromBlockCoords(tileentity1.getPos());
                  IBlockState iblockstate = chunk.getBlockState(tileentity1.getPos());
                  chunk.addTileEntity(tileentity1.getPos(), tileentity1);
                  this.notifyBlockUpdate(tileentity1.getPos(), iblockstate, iblockstate, 3);
               }
            }
         }

         this.addedTileEntityList.clear();
      }

      this.theProfiler.endSection();
      this.theProfiler.endSection();
   }

   protected void tickPlayers() {
   }

   public boolean addTileEntity(TileEntity var1) {
      if (tile.getWorld() != null) {
         tile.setWorld(this);
      }

      List dest = this.processingLoadedTiles ? this.addedTileEntityList : this.loadedTileEntityList;
      boolean flag = dest.add(tile);
      if (flag && tile instanceof ITickable) {
         this.tickableTileEntities.add(tile);
      }

      if (this.isRemote) {
         BlockPos blockpos = tile.getPos();
         IBlockState iblockstate = this.getBlockState(blockpos);
         this.notifyBlockUpdate(blockpos, iblockstate, iblockstate, 2);
      }

      return flag;
   }

   public void addTileEntities(Collection var1) {
      if (this.processingLoadedTiles) {
         for(TileEntity te : tileEntityCollection) {
            if (te.getWorld() != this) {
               te.setWorld(this);
            }
         }

         this.addedTileEntityList.addAll(tileEntityCollection);
      } else {
         for(TileEntity tileentity : tileEntityCollection) {
            this.addTileEntity(tileentity);
         }
      }

   }

   public void updateEntity(Entity var1) {
      this.updateEntityWithOptionalForce(ent, true);
   }

   public void updateEntityWithOptionalForce(Entity var1, boolean var2) {
      int i = MathHelper.floor(entityIn.posX);
      int j = MathHelper.floor(entityIn.posZ);
      boolean isForced = this.getPersistentChunks().containsKey(new ChunkPos(i >> 4, j >> 4));
      int k = isForced ? 0 : 32;
      boolean canUpdate = !forceUpdate || this.isAreaLoaded(i - k, 0, j - k, i + k, 0, j + k, true);
      if (!canUpdate) {
         canUpdate = ForgeEventFactory.canEntityUpdate(entityIn);
      }

      if (canUpdate) {
         entityIn.lastTickPosX = entityIn.posX;
         entityIn.lastTickPosY = entityIn.posY;
         entityIn.lastTickPosZ = entityIn.posZ;
         entityIn.prevRotationYaw = entityIn.rotationYaw;
         entityIn.prevRotationPitch = entityIn.rotationPitch;
         if (forceUpdate && entityIn.addedToChunk) {
            ++entityIn.ticksExisted;
            if (entityIn.isRiding()) {
               entityIn.updateRidden();
            } else {
               entityIn.onUpdate();
            }
         }

         this.theProfiler.startSection("chunkCheck");
         if (Double.isNaN(entityIn.posX) || Double.isInfinite(entityIn.posX)) {
            entityIn.posX = entityIn.lastTickPosX;
         }

         if (Double.isNaN(entityIn.posY) || Double.isInfinite(entityIn.posY)) {
            entityIn.posY = entityIn.lastTickPosY;
         }

         if (Double.isNaN(entityIn.posZ) || Double.isInfinite(entityIn.posZ)) {
            entityIn.posZ = entityIn.lastTickPosZ;
         }

         if (Double.isNaN((double)entityIn.rotationPitch) || Double.isInfinite((double)entityIn.rotationPitch)) {
            entityIn.rotationPitch = entityIn.prevRotationPitch;
         }

         if (Double.isNaN((double)entityIn.rotationYaw) || Double.isInfinite((double)entityIn.rotationYaw)) {
            entityIn.rotationYaw = entityIn.prevRotationYaw;
         }

         int l = MathHelper.floor(entityIn.posX / 16.0D);
         int i1 = MathHelper.floor(entityIn.posY / 16.0D);
         int j1 = MathHelper.floor(entityIn.posZ / 16.0D);
         if (!entityIn.addedToChunk || entityIn.chunkCoordX != l || entityIn.chunkCoordY != i1 || entityIn.chunkCoordZ != j1) {
            if (entityIn.addedToChunk && this.isChunkLoaded(entityIn.chunkCoordX, entityIn.chunkCoordZ, true)) {
               this.getChunkFromChunkCoords(entityIn.chunkCoordX, entityIn.chunkCoordZ).removeEntityAtIndex(entityIn, entityIn.chunkCoordY);
            }

            if (!entityIn.setPositionNonDirty() && !this.isChunkLoaded(l, j1, true)) {
               entityIn.addedToChunk = false;
            } else {
               this.getChunkFromChunkCoords(l, j1).addEntity(entityIn);
            }
         }

         this.theProfiler.endSection();
         if (forceUpdate && entityIn.addedToChunk) {
            for(Entity entity : entityIn.getPassengers()) {
               if (!entity.isDead && entity.getRidingEntity() == entityIn) {
                  this.updateEntity(entity);
               } else {
                  entity.dismountRidingEntity();
               }
            }
         }
      }

   }

   public boolean checkNoEntityCollision(AxisAlignedBB var1) {
      return this.checkNoEntityCollision(bb, (Entity)null);
   }

   public boolean checkNoEntityCollision(AxisAlignedBB var1, @Nullable Entity var2) {
      List list = this.getEntitiesWithinAABBExcludingEntity((Entity)null, bb);

      for(int i = 0; i < list.size(); ++i) {
         Entity entity = (Entity)list.get(i);
         if (!entity.isDead && entity.preventEntitySpawning && entity != entityIn && (entityIn == null || entity.isRidingSameEntity(entityIn))) {
            return false;
         }
      }

      return true;
   }

   public boolean checkBlockCollision(AxisAlignedBB var1) {
      int i = MathHelper.floor(bb.minX);
      int j = MathHelper.ceil(bb.maxX);
      int k = MathHelper.floor(bb.minY);
      int l = MathHelper.ceil(bb.maxY);
      int i1 = MathHelper.floor(bb.minZ);
      int j1 = MathHelper.ceil(bb.maxZ);
      BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

      for(int k1 = i; k1 < j; ++k1) {
         for(int l1 = k; l1 < l; ++l1) {
            for(int i2 = i1; i2 < j1; ++i2) {
               IBlockState iblockstate = this.getBlockState(blockpos$pooledmutableblockpos.setPos(k1, l1, i2));
               if (iblockstate.getMaterial() != Material.AIR) {
                  blockpos$pooledmutableblockpos.release();
                  return true;
               }
            }
         }
      }

      blockpos$pooledmutableblockpos.release();
      return false;
   }

   public boolean containsAnyLiquid(AxisAlignedBB var1) {
      int i = MathHelper.floor(bb.minX);
      int j = MathHelper.ceil(bb.maxX);
      int k = MathHelper.floor(bb.minY);
      int l = MathHelper.ceil(bb.maxY);
      int i1 = MathHelper.floor(bb.minZ);
      int j1 = MathHelper.ceil(bb.maxZ);
      BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

      for(int k1 = i; k1 < j; ++k1) {
         for(int l1 = k; l1 < l; ++l1) {
            for(int i2 = i1; i2 < j1; ++i2) {
               IBlockState iblockstate = this.getBlockState(blockpos$pooledmutableblockpos.setPos(k1, l1, i2));
               if (iblockstate.getMaterial().isLiquid()) {
                  blockpos$pooledmutableblockpos.release();
                  return true;
               }
            }
         }
      }

      blockpos$pooledmutableblockpos.release();
      return false;
   }

   public boolean isFlammableWithin(AxisAlignedBB var1) {
      int i = MathHelper.floor(bb.minX);
      int j = MathHelper.ceil(bb.maxX);
      int k = MathHelper.floor(bb.minY);
      int l = MathHelper.ceil(bb.maxY);
      int i1 = MathHelper.floor(bb.minZ);
      int j1 = MathHelper.ceil(bb.maxZ);
      if (this.isAreaLoaded(i, k, i1, j, l, j1, true)) {
         BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

         for(int k1 = i; k1 < j; ++k1) {
            for(int l1 = k; l1 < l; ++l1) {
               for(int i2 = i1; i2 < j1; ++i2) {
                  Block block = this.getBlockState(blockpos$pooledmutableblockpos.setPos(k1, l1, i2)).getBlock();
                  if (block == Blocks.FIRE || block == Blocks.FLOWING_LAVA || block == Blocks.LAVA) {
                     blockpos$pooledmutableblockpos.release();
                     return true;
                  }

                  if (block.isBurning(this, new BlockPos(k1, l1, i2))) {
                     return true;
                  }
               }
            }
         }

         blockpos$pooledmutableblockpos.release();
      }

      return false;
   }

   public boolean handleMaterialAcceleration(AxisAlignedBB var1, Material var2, Entity var3) {
      int i = MathHelper.floor(bb.minX);
      int j = MathHelper.ceil(bb.maxX);
      int k = MathHelper.floor(bb.minY);
      int l = MathHelper.ceil(bb.maxY);
      int i1 = MathHelper.floor(bb.minZ);
      int j1 = MathHelper.ceil(bb.maxZ);
      if (!this.isAreaLoaded(i, k, i1, j, l, j1, true)) {
         return false;
      } else {
         boolean flag = false;
         Vec3d vec3d = Vec3d.ZERO;
         BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

         for(int k1 = i; k1 < j; ++k1) {
            for(int l1 = k; l1 < l; ++l1) {
               for(int i2 = i1; i2 < j1; ++i2) {
                  blockpos$pooledmutableblockpos.setPos(k1, l1, i2);
                  IBlockState iblockstate = this.getBlockState(blockpos$pooledmutableblockpos);
                  Block block = iblockstate.getBlock();
                  Boolean result = block.isEntityInsideMaterial(this, blockpos$pooledmutableblockpos, iblockstate, entityIn, (double)l, materialIn, false);
                  if (result != null && result.booleanValue()) {
                     flag = true;
                     vec3d = block.modifyAcceleration(this, blockpos$pooledmutableblockpos, entityIn, vec3d);
                  } else if ((result == null || result.booleanValue()) && iblockstate.getMaterial() == materialIn) {
                     double d0 = (double)((float)(l1 + 1) - BlockLiquid.getLiquidHeightPercent(((Integer)iblockstate.getValue(BlockLiquid.LEVEL)).intValue()));
                     if ((double)l >= d0) {
                        flag = true;
                        vec3d = block.modifyAcceleration(this, blockpos$pooledmutableblockpos, entityIn, vec3d);
                     }
                  }
               }
            }
         }

         blockpos$pooledmutableblockpos.release();
         if (vec3d.lengthVector() > 0.0D && entityIn.isPushedByWater()) {
            vec3d = vec3d.normalize();
            double d1 = 0.014D;
            entityIn.motionX += vec3d.xCoord * 0.014D;
            entityIn.motionY += vec3d.yCoord * 0.014D;
            entityIn.motionZ += vec3d.zCoord * 0.014D;
         }

         return flag;
      }
   }

   public boolean isMaterialInBB(AxisAlignedBB var1, Material var2) {
      int i = MathHelper.floor(bb.minX);
      int j = MathHelper.ceil(bb.maxX);
      int k = MathHelper.floor(bb.minY);
      int l = MathHelper.ceil(bb.maxY);
      int i1 = MathHelper.floor(bb.minZ);
      int j1 = MathHelper.ceil(bb.maxZ);
      BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

      for(int k1 = i; k1 < j; ++k1) {
         for(int l1 = k; l1 < l; ++l1) {
            for(int i2 = i1; i2 < j1; ++i2) {
               if (this.getBlockState(blockpos$pooledmutableblockpos.setPos(k1, l1, i2)).getMaterial() == materialIn) {
                  blockpos$pooledmutableblockpos.release();
                  return true;
               }
            }
         }
      }

      blockpos$pooledmutableblockpos.release();
      return false;
   }

   public boolean isAABBInMaterial(AxisAlignedBB var1, Material var2) {
      int i = MathHelper.floor(bb.minX);
      int j = MathHelper.ceil(bb.maxX);
      int k = MathHelper.floor(bb.minY);
      int l = MathHelper.ceil(bb.maxY);
      int i1 = MathHelper.floor(bb.minZ);
      int j1 = MathHelper.ceil(bb.maxZ);
      BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

      for(int k1 = i; k1 < j; ++k1) {
         for(int l1 = k; l1 < l; ++l1) {
            for(int i2 = i1; i2 < j1; ++i2) {
               IBlockState iblockstate = this.getBlockState(blockpos$pooledmutableblockpos.setPos(k1, l1, i2));
               Boolean result = iblockstate.getBlock().isAABBInsideMaterial(this, blockpos$pooledmutableblockpos, bb, materialIn);
               if (result != null) {
                  return result.booleanValue();
               }

               if (iblockstate.getMaterial() == materialIn) {
                  int j2 = ((Integer)iblockstate.getValue(BlockLiquid.LEVEL)).intValue();
                  double d0 = (double)(l1 + 1);
                  if (j2 < 8) {
                     d0 = (double)(l1 + 1) - (double)j2 / 8.0D;
                  }

                  if (d0 >= bb.minY) {
                     blockpos$pooledmutableblockpos.release();
                     return true;
                  }
               }
            }
         }
      }

      blockpos$pooledmutableblockpos.release();
      return false;
   }

   public Explosion createExplosion(@Nullable Entity var1, double var2, double var4, double var6, float var8, boolean var9) {
      return this.newExplosion(entityIn, x, y, z, strength, false, isSmoking);
   }

   public Explosion newExplosion(@Nullable Entity var1, double var2, double var4, double var6, float var8, boolean var9, boolean var10) {
      Explosion explosion = new Explosion(this, entityIn, x, y, z, strength, isFlaming, isSmoking);
      if (ForgeEventFactory.onExplosionStart(this, explosion)) {
         return explosion;
      } else {
         explosion.doExplosionA();
         explosion.doExplosionB(true);
         return explosion;
      }
   }

   public float getBlockDensity(Vec3d var1, AxisAlignedBB var2) {
      double d0 = 1.0D / ((bb.maxX - bb.minX) * 2.0D + 1.0D);
      double d1 = 1.0D / ((bb.maxY - bb.minY) * 2.0D + 1.0D);
      double d2 = 1.0D / ((bb.maxZ - bb.minZ) * 2.0D + 1.0D);
      double d3 = (1.0D - Math.floor(1.0D / d0) * d0) / 2.0D;
      double d4 = (1.0D - Math.floor(1.0D / d2) * d2) / 2.0D;
      if (d0 >= 0.0D && d1 >= 0.0D && d2 >= 0.0D) {
         int i = 0;
         int j = 0;

         for(float f = 0.0F; f <= 1.0F; f = (float)((double)f + d0)) {
            for(float f1 = 0.0F; f1 <= 1.0F; f1 = (float)((double)f1 + d1)) {
               for(float f2 = 0.0F; f2 <= 1.0F; f2 = (float)((double)f2 + d2)) {
                  double d5 = bb.minX + (bb.maxX - bb.minX) * (double)f;
                  double d6 = bb.minY + (bb.maxY - bb.minY) * (double)f1;
                  double d7 = bb.minZ + (bb.maxZ - bb.minZ) * (double)f2;
                  if (this.rayTraceBlocks(new Vec3d(d5 + d3, d6, d7 + d4), vec) == null) {
                     ++i;
                  }

                  ++j;
               }
            }
         }

         return (float)i / (float)j;
      } else {
         return 0.0F;
      }
   }

   public boolean extinguishFire(@Nullable EntityPlayer var1, BlockPos var2, EnumFacing var3) {
      pos = pos.offset(side);
      if (this.getBlockState(pos).getBlock() == Blocks.FIRE) {
         this.playEvent(player, 1009, pos, 0);
         this.setBlockToAir(pos);
         return true;
      } else {
         return false;
      }
   }

   @SideOnly(Side.CLIENT)
   public String getDebugLoadedEntities() {
      return "All: " + this.loadedEntityList.size();
   }

   @SideOnly(Side.CLIENT)
   public String getProviderName() {
      return this.chunkProvider.makeString();
   }

   @Nullable
   public TileEntity getTileEntity(BlockPos var1) {
      if (this.isOutsideBuildHeight(pos)) {
         return null;
      } else {
         TileEntity tileentity = null;
         if (this.processingLoadedTiles) {
            tileentity = this.getPendingTileEntityAt(pos);
         }

         if (tileentity == null) {
            tileentity = this.getChunkFromBlockCoords(pos).getTileEntity(pos, Chunk.EnumCreateEntityType.IMMEDIATE);
         }

         if (tileentity == null) {
            tileentity = this.getPendingTileEntityAt(pos);
         }

         return tileentity;
      }
   }

   @Nullable
   private TileEntity getPendingTileEntityAt(BlockPos var1) {
      for(int i = 0; i < this.addedTileEntityList.size(); ++i) {
         TileEntity tileentity = (TileEntity)this.addedTileEntityList.get(i);
         if (!tileentity.isInvalid() && tileentity.getPos().equals(p_189508_1_)) {
            return tileentity;
         }
      }

      return null;
   }

   public void setTileEntity(BlockPos var1, @Nullable TileEntity var2) {
      pos = pos.toImmutable();
      if (!this.isOutsideBuildHeight(pos) && tileEntityIn != null && !tileEntityIn.isInvalid()) {
         if (!this.processingLoadedTiles) {
            this.addTileEntity(tileEntityIn);
            Chunk chunk = this.getChunkFromBlockCoords(pos);
            if (chunk != null) {
               chunk.addTileEntity(pos, tileEntityIn);
            }
         } else {
            tileEntityIn.setPos(pos);
            if (tileEntityIn.getWorld() != this) {
               tileEntityIn.setWorld(this);
            }

            Iterator iterator = this.addedTileEntityList.iterator();

            while(iterator.hasNext()) {
               TileEntity tileentity = (TileEntity)iterator.next();
               if (tileentity.getPos().equals(pos)) {
                  tileentity.invalidate();
                  iterator.remove();
               }
            }

            this.addedTileEntityList.add(tileEntityIn);
         }

         this.updateComparatorOutputLevel(pos, this.getBlockState(pos).getBlock());
      }

   }

   public void removeTileEntity(BlockPos var1) {
      TileEntity tileentity = this.getTileEntity(pos);
      if (tileentity != null && this.processingLoadedTiles) {
         tileentity.invalidate();
         this.addedTileEntityList.remove(tileentity);
         if (!(tileentity instanceof ITickable)) {
            this.loadedTileEntityList.remove(tileentity);
         }
      } else {
         if (tileentity != null) {
            this.addedTileEntityList.remove(tileentity);
            this.loadedTileEntityList.remove(tileentity);
            this.tickableTileEntities.remove(tileentity);
         }

         this.getChunkFromBlockCoords(pos).removeTileEntity(pos);
      }

      this.updateComparatorOutputLevel(pos, this.getBlockState(pos).getBlock());
   }

   public void markTileEntityForRemoval(TileEntity var1) {
      this.tileEntitiesToBeRemoved.add(tileEntityIn);
   }

   public boolean isBlockFullCube(BlockPos var1) {
      AxisAlignedBB axisalignedbb = this.getBlockState(pos).getCollisionBoundingBox(this, pos);
      return axisalignedbb != Block.NULL_AABB && axisalignedbb.getAverageEdgeLength() >= 1.0D;
   }

   public boolean isBlockNormalCube(BlockPos var1, boolean var2) {
      if (this.isOutsideBuildHeight(pos)) {
         return false;
      } else {
         Chunk chunk = this.chunkProvider.getLoadedChunk(pos.getX() >> 4, pos.getZ() >> 4);
         if (chunk != null && !chunk.isEmpty()) {
            IBlockState iblockstate = this.getBlockState(pos);
            return iblockstate.getBlock().isNormalCube(iblockstate, this, pos);
         } else {
            return _default;
         }
      }
   }

   public void calculateInitialSkylight() {
      int i = this.calculateSkylightSubtracted(1.0F);
      if (i != this.skylightSubtracted) {
         this.skylightSubtracted = i;
      }

   }

   public void setAllowedSpawnTypes(boolean var1, boolean var2) {
      this.spawnHostileMobs = hostile;
      this.spawnPeacefulMobs = peaceful;
      this.provider.setAllowedSpawnTypes(hostile, peaceful);
   }

   public void tick() {
      this.updateWeather();
   }

   protected void calculateInitialWeather() {
      this.provider.calculateInitialWeather();
   }

   public void calculateInitialWeatherBody() {
      if (this.worldInfo.isRaining()) {
         this.rainingStrength = 1.0F;
         if (this.worldInfo.isThundering()) {
            this.thunderingStrength = 1.0F;
         }
      }

   }

   protected void updateWeather() {
      this.provider.updateWeather();
   }

   public void updateWeatherBody() {
      if (!this.provider.hasNoSky() && !this.isRemote) {
         int i = this.worldInfo.getCleanWeatherTime();
         if (i > 0) {
            --i;
            this.worldInfo.setCleanWeatherTime(i);
            this.worldInfo.setThunderTime(this.worldInfo.isThundering() ? 1 : 2);
            this.worldInfo.setRainTime(this.worldInfo.isRaining() ? 1 : 2);
         }

         int j = this.worldInfo.getThunderTime();
         if (j <= 0) {
            if (this.worldInfo.isThundering()) {
               this.worldInfo.setThunderTime(this.rand.nextInt(12000) + 3600);
            } else {
               this.worldInfo.setThunderTime(this.rand.nextInt(168000) + 12000);
            }
         } else {
            --j;
            this.worldInfo.setThunderTime(j);
            if (j <= 0) {
               this.worldInfo.setThundering(!this.worldInfo.isThundering());
            }
         }

         this.prevThunderingStrength = this.thunderingStrength;
         if (this.worldInfo.isThundering()) {
            this.thunderingStrength = (float)((double)this.thunderingStrength + 0.01D);
         } else {
            this.thunderingStrength = (float)((double)this.thunderingStrength - 0.01D);
         }

         this.thunderingStrength = MathHelper.clamp(this.thunderingStrength, 0.0F, 1.0F);
         int k = this.worldInfo.getRainTime();
         if (k <= 0) {
            if (this.worldInfo.isRaining()) {
               this.worldInfo.setRainTime(this.rand.nextInt(12000) + 12000);
            } else {
               this.worldInfo.setRainTime(this.rand.nextInt(168000) + 12000);
            }
         } else {
            --k;
            this.worldInfo.setRainTime(k);
            if (k <= 0) {
               this.worldInfo.setRaining(!this.worldInfo.isRaining());
            }
         }

         this.prevRainingStrength = this.rainingStrength;
         if (this.worldInfo.isRaining()) {
            this.rainingStrength = (float)((double)this.rainingStrength + 0.01D);
         } else {
            this.rainingStrength = (float)((double)this.rainingStrength - 0.01D);
         }

         this.rainingStrength = MathHelper.clamp(this.rainingStrength, 0.0F, 1.0F);
      }

   }

   @SideOnly(Side.CLIENT)
   protected void playMoodSoundAndCheckLight(int var1, int var2, Chunk var3) {
      chunkIn.enqueueRelightChecks();
   }

   protected void updateBlocks() {
   }

   public void immediateBlockTick(BlockPos var1, IBlockState var2, Random var3) {
      this.scheduledUpdatesAreImmediate = true;
      state.getBlock().updateTick(this, pos, state, random);
      this.scheduledUpdatesAreImmediate = false;
   }

   public boolean canBlockFreezeWater(BlockPos var1) {
      return this.canBlockFreeze(pos, false);
   }

   public boolean canBlockFreezeNoWater(BlockPos var1) {
      return this.canBlockFreeze(pos, true);
   }

   public boolean canBlockFreeze(BlockPos var1, boolean var2) {
      return this.provider.canBlockFreeze(pos, noWaterAdj);
   }

   public boolean canBlockFreezeBody(BlockPos var1, boolean var2) {
      Biome biome = this.getBiome(pos);
      float f = biome.getFloatTemperature(pos);
      if (f > 0.15F) {
         return false;
      } else {
         if (pos.getY() >= 0 && pos.getY() < 256 && this.getLightFor(EnumSkyBlock.BLOCK, pos) < 10) {
            IBlockState iblockstate = this.getBlockState(pos);
            Block block = iblockstate.getBlock();
            if ((block == Blocks.WATER || block == Blocks.FLOWING_WATER) && ((Integer)iblockstate.getValue(BlockLiquid.LEVEL)).intValue() == 0) {
               if (!noWaterAdj) {
                  return true;
               }

               boolean flag = this.isWater(pos.west()) && this.isWater(pos.east()) && this.isWater(pos.north()) && this.isWater(pos.south());
               if (!flag) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private boolean isWater(BlockPos var1) {
      return this.getBlockState(pos).getMaterial() == Material.WATER;
   }

   public boolean canSnowAt(BlockPos var1, boolean var2) {
      return this.provider.canSnowAt(pos, checkLight);
   }

   public boolean canSnowAtBody(BlockPos var1, boolean var2) {
      Biome biome = this.getBiome(pos);
      float f = biome.getFloatTemperature(pos);
      if (f > 0.15F) {
         return false;
      } else if (!checkLight) {
         return true;
      } else {
         if (pos.getY() >= 0 && pos.getY() < 256 && this.getLightFor(EnumSkyBlock.BLOCK, pos) < 10) {
            IBlockState iblockstate = this.getBlockState(pos);
            if (iblockstate.getBlock().isAir(iblockstate, this, pos) && Blocks.SNOW_LAYER.canPlaceBlockAt(this, pos)) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean checkLight(BlockPos var1) {
      boolean flag = false;
      if (!this.provider.hasNoSky()) {
         flag |= this.checkLightFor(EnumSkyBlock.SKY, pos);
      }

      flag = flag | this.checkLightFor(EnumSkyBlock.BLOCK, pos);
      return flag;
   }

   private int getRawLight(BlockPos var1, EnumSkyBlock var2) {
      if (lightType == EnumSkyBlock.SKY && this.canSeeSky(pos)) {
         return 15;
      } else {
         IBlockState iblockstate = this.getBlockState(pos);
         int blockLight = iblockstate.getBlock().getLightValue(iblockstate, this, pos);
         int i = lightType == EnumSkyBlock.SKY ? 0 : blockLight;
         int j = iblockstate.getBlock().getLightOpacity(iblockstate, this, pos);
         if (j >= 15 && blockLight > 0) {
            j = 1;
         }

         if (j < 1) {
            j = 1;
         }

         if (j >= 15) {
            return 0;
         } else if (i >= 14) {
            return i;
         } else {
            BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

            for(EnumFacing enumfacing : EnumFacing.values()) {
               blockpos$pooledmutableblockpos.setPos(pos).move(enumfacing);
               int k = this.getLightFor(lightType, blockpos$pooledmutableblockpos) - j;
               if (k > i) {
                  i = k;
               }

               if (i >= 14) {
                  return i;
               }
            }

            blockpos$pooledmutableblockpos.release();
            return i;
         }
      }
   }

   public boolean checkLightFor(EnumSkyBlock var1, BlockPos var2) {
      if (!this.isAreaLoaded(pos, 17, false)) {
         return false;
      } else {
         int i = 0;
         int j = 0;
         this.theProfiler.startSection("getBrightness");
         int k = this.getLightFor(lightType, pos);
         int l = this.getRawLight(pos, lightType);
         int i1 = pos.getX();
         int j1 = pos.getY();
         int k1 = pos.getZ();
         if (l > k) {
            this.lightUpdateBlockList[j++] = 133152;
         } else if (l < k) {
            this.lightUpdateBlockList[j++] = 133152 | k << 18;

            while(i < j) {
               int l1 = this.lightUpdateBlockList[i++];
               int i2 = (l1 & 63) - 32 + i1;
               int j2 = (l1 >> 6 & 63) - 32 + j1;
               int k2 = (l1 >> 12 & 63) - 32 + k1;
               int l2 = l1 >> 18 & 15;
               BlockPos blockpos = new BlockPos(i2, j2, k2);
               int i3 = this.getLightFor(lightType, blockpos);
               if (i3 == l2) {
                  this.setLightFor(lightType, blockpos, 0);
                  if (l2 > 0) {
                     int j3 = MathHelper.abs(i2 - i1);
                     int k3 = MathHelper.abs(j2 - j1);
                     int l3 = MathHelper.abs(k2 - k1);
                     if (j3 + k3 + l3 < 17) {
                        BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

                        for(EnumFacing enumfacing : EnumFacing.values()) {
                           int i4 = i2 + enumfacing.getFrontOffsetX();
                           int j4 = j2 + enumfacing.getFrontOffsetY();
                           int k4 = k2 + enumfacing.getFrontOffsetZ();
                           blockpos$pooledmutableblockpos.setPos(i4, j4, k4);
                           int l4 = Math.max(1, this.getBlockState(blockpos$pooledmutableblockpos).getBlock().getLightOpacity(this.getBlockState(blockpos$pooledmutableblockpos), this, blockpos$pooledmutableblockpos));
                           i3 = this.getLightFor(lightType, blockpos$pooledmutableblockpos);
                           if (i3 == l2 - l4 && j < this.lightUpdateBlockList.length) {
                              this.lightUpdateBlockList[j++] = i4 - i1 + 32 | j4 - j1 + 32 << 6 | k4 - k1 + 32 << 12 | l2 - l4 << 18;
                           }
                        }

                        blockpos$pooledmutableblockpos.release();
                     }
                  }
               }
            }

            i = 0;
         }

         this.theProfiler.endSection();
         this.theProfiler.startSection("checkedPosition < toCheckCount");

         while(i < j) {
            int i5 = this.lightUpdateBlockList[i++];
            int j5 = (i5 & 63) - 32 + i1;
            int k5 = (i5 >> 6 & 63) - 32 + j1;
            int l5 = (i5 >> 12 & 63) - 32 + k1;
            BlockPos blockpos1 = new BlockPos(j5, k5, l5);
            int i6 = this.getLightFor(lightType, blockpos1);
            int j6 = this.getRawLight(blockpos1, lightType);
            if (j6 != i6) {
               this.setLightFor(lightType, blockpos1, j6);
               if (j6 > i6) {
                  int k6 = Math.abs(j5 - i1);
                  int l6 = Math.abs(k5 - j1);
                  int i7 = Math.abs(l5 - k1);
                  boolean flag = j < this.lightUpdateBlockList.length - 6;
                  if (k6 + l6 + i7 < 17 && flag) {
                     if (this.getLightFor(lightType, blockpos1.west()) < j6) {
                        this.lightUpdateBlockList[j++] = j5 - 1 - i1 + 32 + (k5 - j1 + 32 << 6) + (l5 - k1 + 32 << 12);
                     }

                     if (this.getLightFor(lightType, blockpos1.east()) < j6) {
                        this.lightUpdateBlockList[j++] = j5 + 1 - i1 + 32 + (k5 - j1 + 32 << 6) + (l5 - k1 + 32 << 12);
                     }

                     if (this.getLightFor(lightType, blockpos1.down()) < j6) {
                        this.lightUpdateBlockList[j++] = j5 - i1 + 32 + (k5 - 1 - j1 + 32 << 6) + (l5 - k1 + 32 << 12);
                     }

                     if (this.getLightFor(lightType, blockpos1.up()) < j6) {
                        this.lightUpdateBlockList[j++] = j5 - i1 + 32 + (k5 + 1 - j1 + 32 << 6) + (l5 - k1 + 32 << 12);
                     }

                     if (this.getLightFor(lightType, blockpos1.north()) < j6) {
                        this.lightUpdateBlockList[j++] = j5 - i1 + 32 + (k5 - j1 + 32 << 6) + (l5 - 1 - k1 + 32 << 12);
                     }

                     if (this.getLightFor(lightType, blockpos1.south()) < j6) {
                        this.lightUpdateBlockList[j++] = j5 - i1 + 32 + (k5 - j1 + 32 << 6) + (l5 + 1 - k1 + 32 << 12);
                     }
                  }
               }
            }
         }

         this.theProfiler.endSection();
         return true;
      }
   }

   public boolean tickUpdates(boolean var1) {
      return false;
   }

   @Nullable
   public List getPendingBlockUpdates(Chunk var1, boolean var2) {
      return null;
   }

   @Nullable
   public List getPendingBlockUpdates(StructureBoundingBox var1, boolean var2) {
      return null;
   }

   public List getEntitiesWithinAABBExcludingEntity(@Nullable Entity var1, AxisAlignedBB var2) {
      return this.getEntitiesInAABBexcluding(entityIn, bb, EntitySelectors.NOT_SPECTATING);
   }

   public List getEntitiesInAABBexcluding(@Nullable Entity var1, AxisAlignedBB var2, @Nullable Predicate var3) {
      List list = Lists.newArrayList();
      int i = MathHelper.floor((boundingBox.minX - MAX_ENTITY_RADIUS) / 16.0D);
      int j = MathHelper.floor((boundingBox.maxX + MAX_ENTITY_RADIUS) / 16.0D);
      int k = MathHelper.floor((boundingBox.minZ - MAX_ENTITY_RADIUS) / 16.0D);
      int l = MathHelper.floor((boundingBox.maxZ + MAX_ENTITY_RADIUS) / 16.0D);

      for(int i1 = i; i1 <= j; ++i1) {
         for(int j1 = k; j1 <= l; ++j1) {
            if (this.isChunkLoaded(i1, j1, true)) {
               this.getChunkFromChunkCoords(i1, j1).getEntitiesWithinAABBForEntity(entityIn, boundingBox, list, predicate);
            }
         }
      }

      return list;
   }

   public List getEntities(Class var1, Predicate var2) {
      List list = Lists.newArrayList();

      for(Entity entity : this.loadedEntityList) {
         if (entityType.isAssignableFrom(entity.getClass()) && filter.apply(entity)) {
            list.add(entity);
         }
      }

      return list;
   }

   public List getPlayers(Class var1, Predicate var2) {
      List list = Lists.newArrayList();

      for(Entity entity : this.playerEntities) {
         if (playerType.isAssignableFrom(entity.getClass()) && filter.apply(entity)) {
            list.add(entity);
         }
      }

      return list;
   }

   public List getEntitiesWithinAABB(Class var1, AxisAlignedBB var2) {
      return this.getEntitiesWithinAABB(classEntity, bb, EntitySelectors.NOT_SPECTATING);
   }

   public List getEntitiesWithinAABB(Class var1, AxisAlignedBB var2, @Nullable Predicate var3) {
      int i = MathHelper.floor((aabb.minX - MAX_ENTITY_RADIUS) / 16.0D);
      int j = MathHelper.ceil((aabb.maxX + MAX_ENTITY_RADIUS) / 16.0D);
      int k = MathHelper.floor((aabb.minZ - MAX_ENTITY_RADIUS) / 16.0D);
      int l = MathHelper.ceil((aabb.maxZ + MAX_ENTITY_RADIUS) / 16.0D);
      List list = Lists.newArrayList();

      for(int i1 = i; i1 < j; ++i1) {
         for(int j1 = k; j1 < l; ++j1) {
            if (this.isChunkLoaded(i1, j1, true)) {
               this.getChunkFromChunkCoords(i1, j1).getEntitiesOfTypeWithinAAAB(clazz, aabb, list, filter);
            }
         }
      }

      return list;
   }

   @Nullable
   public Entity findNearestEntityWithinAABB(Class var1, AxisAlignedBB var2, Entity var3) {
      List list = this.getEntitiesWithinAABB(entityType, aabb);
      Entity t = (T)null;
      double d0 = Double.MAX_VALUE;

      for(int i = 0; i < list.size(); ++i) {
         Entity t1 = (T)((Entity)list.get(i));
         if (t1 != closestTo && EntitySelectors.NOT_SPECTATING.apply(t1)) {
            double d1 = closestTo.getDistanceSqToEntity(t1);
            if (d1 <= d0) {
               t = t1;
               d0 = d1;
            }
         }
      }

      return t;
   }

   @Nullable
   public Entity getEntityByID(int var1) {
      return (Entity)this.entitiesById.lookup(id);
   }

   @SideOnly(Side.CLIENT)
   public List getLoadedEntityList() {
      return this.loadedEntityList;
   }

   public void markChunkDirty(BlockPos var1, TileEntity var2) {
      if (this.isBlockLoaded(pos)) {
         this.getChunkFromBlockCoords(pos).setChunkModified();
      }

   }

   public int countEntities(Class var1) {
      int i = 0;

      for(Entity entity : this.loadedEntityList) {
         if ((!(entity instanceof EntityLiving) || !((EntityLiving)entity).isNoDespawnRequired()) && entityType.isAssignableFrom(entity.getClass())) {
            ++i;
         }
      }

      return i;
   }

   public void loadEntities(Collection var1) {
      for(Entity entity : entityCollection) {
         if (!MinecraftForge.EVENT_BUS.post(new EntityJoinWorldEvent(entity, this))) {
            this.loadedEntityList.add(entity);
            this.onEntityAdded(entity);
         }
      }

   }

   public void unloadEntities(Collection var1) {
      this.unloadedEntityList.addAll(entityCollection);
   }

   public boolean canBlockBePlaced(Block var1, BlockPos var2, boolean var3, EnumFacing var4, @Nullable Entity var5, @Nullable ItemStack var6) {
      IBlockState iblockstate = this.getBlockState(pos);
      AxisAlignedBB axisalignedbb = p_175716_3_ ? null : blockIn.getDefaultState().getCollisionBoundingBox(this, pos);
      return axisalignedbb != Block.NULL_AABB && !this.checkNoEntityCollision(axisalignedbb.offset(pos), entityIn) ? false : (iblockstate.getMaterial() == Material.CIRCUITS && blockIn == Blocks.ANVIL ? true : iblockstate.getBlock().isReplaceable(this, pos) && blockIn.canReplace(this, pos, side, itemStackIn));
   }

   public int getSeaLevel() {
      return this.seaLevel;
   }

   public void setSeaLevel(int var1) {
      this.seaLevel = seaLevelIn;
   }

   public int getStrongPower(BlockPos var1, EnumFacing var2) {
      return this.getBlockState(pos).getStrongPower(this, pos, direction);
   }

   public WorldType getWorldType() {
      return this.worldInfo.getTerrainType();
   }

   public int getStrongPower(BlockPos var1) {
      int i = 0;
      i = Math.max(i, this.getStrongPower(pos.down(), EnumFacing.DOWN));
      if (i >= 15) {
         return i;
      } else {
         i = Math.max(i, this.getStrongPower(pos.up(), EnumFacing.UP));
         if (i >= 15) {
            return i;
         } else {
            i = Math.max(i, this.getStrongPower(pos.north(), EnumFacing.NORTH));
            if (i >= 15) {
               return i;
            } else {
               i = Math.max(i, this.getStrongPower(pos.south(), EnumFacing.SOUTH));
               if (i >= 15) {
                  return i;
               } else {
                  i = Math.max(i, this.getStrongPower(pos.west(), EnumFacing.WEST));
                  if (i >= 15) {
                     return i;
                  } else {
                     i = Math.max(i, this.getStrongPower(pos.east(), EnumFacing.EAST));
                     return i >= 15 ? i : i;
                  }
               }
            }
         }
      }
   }

   public boolean isSidePowered(BlockPos var1, EnumFacing var2) {
      return this.getRedstonePower(pos, side) > 0;
   }

   public int getRedstonePower(BlockPos var1, EnumFacing var2) {
      IBlockState iblockstate = this.getBlockState(pos);
      return iblockstate.getBlock().shouldCheckWeakPower(iblockstate, this, pos, facing) ? this.getStrongPower(pos) : iblockstate.getWeakPower(this, pos, facing);
   }

   public boolean isBlockPowered(BlockPos var1) {
      return this.getRedstonePower(pos.down(), EnumFacing.DOWN) > 0 ? true : (this.getRedstonePower(pos.up(), EnumFacing.UP) > 0 ? true : (this.getRedstonePower(pos.north(), EnumFacing.NORTH) > 0 ? true : (this.getRedstonePower(pos.south(), EnumFacing.SOUTH) > 0 ? true : (this.getRedstonePower(pos.west(), EnumFacing.WEST) > 0 ? true : this.getRedstonePower(pos.east(), EnumFacing.EAST) > 0))));
   }

   public int isBlockIndirectlyGettingPowered(BlockPos var1) {
      int i = 0;

      for(EnumFacing enumfacing : EnumFacing.values()) {
         int j = this.getRedstonePower(pos.offset(enumfacing), enumfacing);
         if (j >= 15) {
            return 15;
         }

         if (j > i) {
            i = j;
         }
      }

      return i;
   }

   @Nullable
   public EntityPlayer getClosestPlayerToEntity(Entity var1, double var2) {
      return this.getClosestPlayer(entityIn.posX, entityIn.posY, entityIn.posZ, distance, false);
   }

   @Nullable
   public EntityPlayer getNearestPlayerNotCreative(Entity var1, double var2) {
      return this.getClosestPlayer(entityIn.posX, entityIn.posY, entityIn.posZ, distance, true);
   }

   @Nullable
   public EntityPlayer getClosestPlayer(double var1, double var3, double var5, double var7, boolean var9) {
      double d0 = -1.0D;
      EntityPlayer entityplayer = null;

      for(int i = 0; i < this.playerEntities.size(); ++i) {
         EntityPlayer entityplayer1 = (EntityPlayer)this.playerEntities.get(i);
         if ((EntitySelectors.CAN_AI_TARGET.apply(entityplayer1) || !spectator) && (EntitySelectors.NOT_SPECTATING.apply(entityplayer1) || spectator)) {
            double d1 = entityplayer1.getDistanceSq(posX, posY, posZ);
            if ((distance < 0.0D || d1 < distance * distance) && (d0 == -1.0D || d1 < d0)) {
               d0 = d1;
               entityplayer = entityplayer1;
            }
         }
      }

      return entityplayer;
   }

   public boolean isAnyPlayerWithinRangeAt(double var1, double var3, double var5, double var7) {
      for(int i = 0; i < this.playerEntities.size(); ++i) {
         EntityPlayer entityplayer = (EntityPlayer)this.playerEntities.get(i);
         if (EntitySelectors.NOT_SPECTATING.apply(entityplayer)) {
            double d0 = entityplayer.getDistanceSq(x, y, z);
            if (range < 0.0D || d0 < range * range) {
               return true;
            }
         }
      }

      return false;
   }

   @Nullable
   public EntityPlayer getNearestAttackablePlayer(Entity var1, double var2, double var4) {
      return this.getNearestAttackablePlayer(entityIn.posX, entityIn.posY, entityIn.posZ, maxXZDistance, maxYDistance, (Function)null, (Predicate)null);
   }

   @Nullable
   public EntityPlayer getNearestAttackablePlayer(BlockPos var1, double var2, double var4) {
      return this.getNearestAttackablePlayer((double)((float)pos.getX() + 0.5F), (double)((float)pos.getY() + 0.5F), (double)((float)pos.getZ() + 0.5F), maxXZDistance, maxYDistance, (Function)null, (Predicate)null);
   }

   @Nullable
   public EntityPlayer getNearestAttackablePlayer(double var1, double var3, double var5, double var7, double var9, @Nullable Function var11, @Nullable Predicate var12) {
      double d0 = -1.0D;
      EntityPlayer entityplayer = null;

      for(int i = 0; i < this.playerEntities.size(); ++i) {
         EntityPlayer entityplayer1 = (EntityPlayer)this.playerEntities.get(i);
         if (!entityplayer1.capabilities.disableDamage && entityplayer1.isEntityAlive() && !entityplayer1.isSpectator() && (p_184150_12_ == null || p_184150_12_.apply(entityplayer1))) {
            double d1 = entityplayer1.getDistanceSq(posX, entityplayer1.posY, posZ);
            double d2 = maxXZDistance;
            if (entityplayer1.isSneaking()) {
               d2 = maxXZDistance * 0.800000011920929D;
            }

            if (entityplayer1.isInvisible()) {
               float f = entityplayer1.getArmorVisibility();
               if (f < 0.1F) {
                  f = 0.1F;
               }

               d2 *= (double)(0.7F * f);
            }

            if (playerToDouble != null) {
               d2 *= ((Double)Objects.firstNonNull(playerToDouble.apply(entityplayer1), Double.valueOf(1.0D))).doubleValue();
            }

            d2 = ForgeHooks.getPlayerVisibilityDistance(entityplayer1, d2, maxXZDistance);
            if ((maxYDistance < 0.0D || Math.abs(entityplayer1.posY - posY) < maxYDistance * maxYDistance) && (maxXZDistance < 0.0D || d1 < d2 * d2) && (d0 == -1.0D || d1 < d0)) {
               d0 = d1;
               entityplayer = entityplayer1;
            }
         }
      }

      return entityplayer;
   }

   @Nullable
   public EntityPlayer getPlayerEntityByName(String var1) {
      for(int i = 0; i < this.playerEntities.size(); ++i) {
         EntityPlayer entityplayer = (EntityPlayer)this.playerEntities.get(i);
         if (name.equals(entityplayer.getName())) {
            return entityplayer;
         }
      }

      return null;
   }

   @Nullable
   public EntityPlayer getPlayerEntityByUUID(UUID var1) {
      for(int i = 0; i < this.playerEntities.size(); ++i) {
         EntityPlayer entityplayer = (EntityPlayer)this.playerEntities.get(i);
         if (uuid.equals(entityplayer.getUniqueID())) {
            return entityplayer;
         }
      }

      return null;
   }

   @SideOnly(Side.CLIENT)
   public void sendQuittingDisconnectingPacket() {
   }

   public void checkSessionLock() throws MinecraftException {
      this.saveHandler.checkSessionLock();
   }

   @SideOnly(Side.CLIENT)
   public void setTotalWorldTime(long var1) {
      this.worldInfo.setWorldTotalTime(worldTime);
   }

   public long getSeed() {
      return this.provider.getSeed();
   }

   public long getTotalWorldTime() {
      return this.worldInfo.getWorldTotalTime();
   }

   public long getWorldTime() {
      return this.provider.getWorldTime();
   }

   public void setWorldTime(long var1) {
      this.provider.setWorldTime(time);
   }

   public BlockPos getSpawnPoint() {
      BlockPos blockpos = this.provider.getSpawnPoint();
      if (!this.getWorldBorder().contains(blockpos)) {
         blockpos = this.getHeight(new BlockPos(this.getWorldBorder().getCenterX(), 0.0D, this.getWorldBorder().getCenterZ()));
      }

      return blockpos;
   }

   public void setSpawnPoint(BlockPos var1) {
      this.provider.setSpawnPoint(pos);
   }

   @SideOnly(Side.CLIENT)
   public void joinEntityInSurroundings(Entity var1) {
      int i = MathHelper.floor(entityIn.posX / 16.0D);
      int j = MathHelper.floor(entityIn.posZ / 16.0D);
      int k = 2;

      for(int l = -2; l <= 2; ++l) {
         for(int i1 = -2; i1 <= 2; ++i1) {
            this.getChunkFromChunkCoords(i + l, j + i1);
         }
      }

      if (!this.loadedEntityList.contains(entityIn) && !MinecraftForge.EVENT_BUS.post(new EntityJoinWorldEvent(entityIn, this))) {
         this.loadedEntityList.add(entityIn);
      }

   }

   public boolean isBlockModifiable(EntityPlayer var1, BlockPos var2) {
      return this.provider.canMineBlock(player, pos);
   }

   public boolean canMineBlockBody(EntityPlayer var1, BlockPos var2) {
      return true;
   }

   public void setEntityState(Entity var1, byte var2) {
   }

   public IChunkProvider getChunkProvider() {
      return this.chunkProvider;
   }

   public void addBlockEvent(BlockPos var1, Block var2, int var3, int var4) {
      this.getBlockState(pos).onBlockEventReceived(this, pos, eventID, eventParam);
   }

   public ISaveHandler getSaveHandler() {
      return this.saveHandler;
   }

   public WorldInfo getWorldInfo() {
      return this.worldInfo;
   }

   public GameRules getGameRules() {
      return this.worldInfo.getGameRulesInstance();
   }

   public void updateAllPlayersSleepingFlag() {
   }

   public float getThunderStrength(float var1) {
      return (this.prevThunderingStrength + (this.thunderingStrength - this.prevThunderingStrength) * delta) * this.getRainStrength(delta);
   }

   @SideOnly(Side.CLIENT)
   public void setThunderStrength(float var1) {
      this.prevThunderingStrength = strength;
      this.thunderingStrength = strength;
   }

   public float getRainStrength(float var1) {
      return this.prevRainingStrength + (this.rainingStrength - this.prevRainingStrength) * delta;
   }

   @SideOnly(Side.CLIENT)
   public void setRainStrength(float var1) {
      this.prevRainingStrength = strength;
      this.rainingStrength = strength;
   }

   public boolean isThundering() {
      return (double)this.getThunderStrength(1.0F) > 0.9D;
   }

   public boolean isRaining() {
      return (double)this.getRainStrength(1.0F) > 0.2D;
   }

   public boolean isRainingAt(BlockPos var1) {
      if (!this.isRaining()) {
         return false;
      } else if (!this.canSeeSky(strikePosition)) {
         return false;
      } else if (this.getPrecipitationHeight(strikePosition).getY() > strikePosition.getY()) {
         return false;
      } else {
         Biome biome = this.getBiome(strikePosition);
         return biome.getEnableSnow() ? false : (this.canSnowAt(strikePosition, false) ? false : biome.canRain());
      }
   }

   public boolean isBlockinHighHumidity(BlockPos var1) {
      return this.provider.isBlockHighHumidity(pos);
   }

   @Nullable
   public MapStorage getMapStorage() {
      return this.mapStorage;
   }

   public void setData(String var1, WorldSavedData var2) {
      this.mapStorage.setData(dataID, worldSavedDataIn);
   }

   @Nullable
   public WorldSavedData loadData(Class var1, String var2) {
      return this.mapStorage.getOrLoadData(clazz, dataID);
   }

   public int getUniqueDataId(String var1) {
      return this.mapStorage.getUniqueDataId(key);
   }

   public void playBroadcastSound(int var1, BlockPos var2, int var3) {
      for(int i = 0; i < this.eventListeners.size(); ++i) {
         ((IWorldEventListener)this.eventListeners.get(i)).broadcastSound(id, pos, data);
      }

   }

   public void playEvent(int var1, BlockPos var2, int var3) {
      this.playEvent((EntityPlayer)null, type, pos, data);
   }

   public void playEvent(@Nullable EntityPlayer var1, int var2, BlockPos var3, int var4) {
      try {
         for(int i = 0; i < this.eventListeners.size(); ++i) {
            ((IWorldEventListener)this.eventListeners.get(i)).playEvent(player, type, pos, data);
         }

      } catch (Throwable var8) {
         CrashReport crashreport = CrashReport.makeCrashReport(var8, "Playing level event");
         CrashReportCategory crashreportcategory = crashreport.makeCategory("Level event being played");
         crashreportcategory.addCrashSection("Block coordinates", CrashReportCategory.getCoordinateInfo(pos));
         crashreportcategory.addCrashSection("Event source", player);
         crashreportcategory.addCrashSection("Event type", Integer.valueOf(type));
         crashreportcategory.addCrashSection("Event data", Integer.valueOf(data));
         throw new ReportedException(crashreport);
      }
   }

   public int getHeight() {
      return this.provider.getHeight();
   }

   public int getActualHeight() {
      return this.provider.getActualHeight();
   }

   public Random setRandomSeed(int var1, int var2, int var3) {
      long i = (long)p_72843_1_ * 341873128712L + (long)p_72843_2_ * 132897987541L + this.getWorldInfo().getSeed() + (long)p_72843_3_;
      this.rand.setSeed(i);
      return this.rand;
   }

   public CrashReportCategory addWorldInfoToCrashReport(CrashReport var1) {
      CrashReportCategory crashreportcategory = report.makeCategoryDepth("Affected level", 1);
      crashreportcategory.addCrashSection("Level name", this.worldInfo == null ? "????" : this.worldInfo.getWorldName());
      crashreportcategory.setDetail("All players", new ICrashReportDetail() {
         public String call() {
            return World.this.playerEntities.size() + " total; " + World.this.playerEntities;
         }
      });
      crashreportcategory.setDetail("Chunk stats", new ICrashReportDetail() {
         public String call() {
            return World.this.chunkProvider.makeString();
         }
      });

      try {
         this.worldInfo.addToCrashReport(crashreportcategory);
      } catch (Throwable var4) {
         crashreportcategory.addCrashSectionThrowable("Level Data Unobtainable", var4);
      }

      return crashreportcategory;
   }

   @SideOnly(Side.CLIENT)
   public double getHorizon() {
      return this.provider.getHorizon();
   }

   public void sendBlockBreakProgress(int var1, BlockPos var2, int var3) {
      for(int i = 0; i < this.eventListeners.size(); ++i) {
         IWorldEventListener iworldeventlistener = (IWorldEventListener)this.eventListeners.get(i);
         iworldeventlistener.sendBlockBreakProgress(breakerId, pos, progress);
      }

   }

   public Calendar getCurrentDate() {
      if (this.getTotalWorldTime() % 600L == 0L) {
         this.theCalendar.setTimeInMillis(MinecraftServer.getCurrentTimeMillis());
      }

      return this.theCalendar;
   }

   @SideOnly(Side.CLIENT)
   public void makeFireworks(double var1, double var3, double var5, double var7, double var9, double var11, @Nullable NBTTagCompound var13) {
   }

   public Scoreboard getScoreboard() {
      return this.worldScoreboard;
   }

   public void updateComparatorOutputLevel(BlockPos var1, Block var2) {
      for(EnumFacing enumfacing : EnumFacing.VALUES) {
         BlockPos blockpos = pos.offset(enumfacing);
         if (this.isBlockLoaded(blockpos)) {
            IBlockState iblockstate = this.getBlockState(blockpos);
            iblockstate.getBlock().onNeighborChange(this, blockpos, pos);
            if (iblockstate.getBlock().isNormalCube(iblockstate, this, blockpos)) {
               blockpos = blockpos.offset(enumfacing);
               iblockstate = this.getBlockState(blockpos);
               if (iblockstate.getBlock().getWeakChanges(this, blockpos)) {
                  iblockstate.getBlock().onNeighborChange(this, blockpos, pos);
               }
            }
         }
      }

   }

   public DifficultyInstance getDifficultyForLocation(BlockPos var1) {
      long i = 0L;
      float f = 0.0F;
      if (this.isBlockLoaded(pos)) {
         f = this.getCurrentMoonPhaseFactor();
         i = this.getChunkFromBlockCoords(pos).getInhabitedTime();
      }

      return new DifficultyInstance(this.getDifficulty(), this.getWorldTime(), i, f);
   }

   public EnumDifficulty getDifficulty() {
      return this.getWorldInfo().getDifficulty();
   }

   public int getSkylightSubtracted() {
      return this.skylightSubtracted;
   }

   public void setSkylightSubtracted(int var1) {
      this.skylightSubtracted = newSkylightSubtracted;
   }

   @SideOnly(Side.CLIENT)
   public int getLastLightningBolt() {
      return this.lastLightningBolt;
   }

   public void setLastLightningBolt(int var1) {
      this.lastLightningBolt = lastLightningBoltIn;
   }

   public VillageCollection getVillageCollection() {
      return this.villageCollectionObj;
   }

   public WorldBorder getWorldBorder() {
      return this.worldBorder;
   }

   public boolean isSpawnChunk(int var1, int var2) {
      BlockPos blockpos = this.getSpawnPoint();
      int i = x * 16 + 8 - blockpos.getX();
      int j = z * 16 + 8 - blockpos.getZ();
      int k = 128;
      return i >= -128 && i <= 128 && j >= -128 && j <= 128;
   }

   public boolean isSideSolid(BlockPos var1, EnumFacing var2) {
      return this.isSideSolid(pos, side, false);
   }

   public boolean isSideSolid(BlockPos var1, EnumFacing var2, boolean var3) {
      if (!this.isValid(pos)) {
         return _default;
      } else {
         Chunk chunk = this.getChunkFromBlockCoords(pos);
         return chunk != null && !chunk.isEmpty() ? this.getBlockState(pos).isSideSolid(this, pos, side) : _default;
      }
   }

   public ImmutableSetMultimap getPersistentChunks() {
      return ForgeChunkManager.getPersistentChunksFor(this);
   }

   public Iterator getPersistentChunkIterable(Iterator var1) {
      return ForgeChunkManager.getPersistentChunksIterableFor(this, chunkIterator);
   }

   public int getBlockLightOpacity(BlockPos var1) {
      return !this.isValid(pos) ? 0 : this.getChunkFromBlockCoords(pos).getBlockLightOpacity(pos);
   }

   public int countEntities(EnumCreatureType var1, boolean var2) {
      int count = 0;

      for(int x = 0; x < this.loadedEntityList.size(); ++x) {
         if (((Entity)this.loadedEntityList.get(x)).isCreatureType(type, forSpawnCount)) {
            ++count;
         }
      }

      return count;
   }

   protected void initCapabilities() {
      ICapabilityProvider parent = this.provider.initCapabilities();
      this.capabilities = ForgeEventFactory.gatherCapabilities(this, parent);
      WorldCapabilityData data = (WorldCapabilityData)this.perWorldStorage.getOrLoadData(WorldCapabilityData.class, "capabilities");
      if (data == null) {
         this.capabilityData = new WorldCapabilityData(this.capabilities);
         this.perWorldStorage.setData(this.capabilityData.mapName, this.capabilityData);
      } else {
         this.capabilityData = data;
         this.capabilityData.setCapabilities(this.provider, this.capabilities);
      }

   }

   public boolean hasCapability(Capability var1, EnumFacing var2) {
      return this.capabilities == null ? false : this.capabilities.hasCapability(capability, facing);
   }

   public Object getCapability(Capability var1, EnumFacing var2) {
      return this.capabilities == null ? null : this.capabilities.getCapability(capability, facing);
   }

   public MapStorage getPerWorldStorage() {
      return this.perWorldStorage;
   }

   public void sendPacketToServer(Packet var1) {
      throw new UnsupportedOperationException("Can't send packets to server unless you're on the client.");
   }

   public LootTableManager getLootTableManager() {
      return this.lootTable;
   }
}
