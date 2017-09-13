package net.minecraft.world;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketWorldBorder;
import net.minecraft.pathfinding.PathWorldListener;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.src.MinecraftServer;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.loot.LootTableManager;
import org.bukkit.Bukkit;
import org.bukkit.World.Environment;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_10_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.generator.ChunkGenerator;

public abstract class World implements IBlockAccess {
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
   protected float prevRainingStrength;
   protected float rainingStrength;
   protected float prevThunderingStrength;
   protected float thunderingStrength;
   private int lastLightningBolt;
   public final Random rand = new Random();
   public WorldProvider provider;
   protected PathWorldListener pathListener = new PathWorldListener();
   protected List eventListeners;
   protected IChunkProvider chunkProvider;
   protected final ISaveHandler saveHandler;
   public WorldInfo worldInfo;
   protected boolean findingSpawnPoint;
   public MapStorage mapStorage;
   protected VillageCollection villageCollectionObj;
   protected LootTableManager lootTable;
   public final Profiler theProfiler;
   private final Calendar theCalendar;
   public Scoreboard worldScoreboard;
   public final boolean isRemote;
   public boolean spawnHostileMobs;
   public boolean spawnPeacefulMobs;
   private boolean processingLoadedTiles;
   private final WorldBorder worldBorder;
   int[] lightUpdateBlockList;
   private final CraftWorld world;
   public boolean pvpMode;
   public boolean keepSpawnInMemory = true;
   public ChunkGenerator generator;
   public boolean captureBlockStates = false;
   public boolean captureTreeGeneration = false;
   public ArrayList capturedBlockStates = new ArrayList() {
      public boolean add(BlockState blockState) {
         for(BlockState blockState1 : this) {
            if (blockState1.getLocation().equals(blockState.getLocation())) {
               return false;
            }
         }

         return super.add(blockState);
      }
   };
   public long ticksPerAnimalSpawns;
   public long ticksPerMonsterSpawns;
   public boolean populating;
   private int tickPosition;
   public Map capturedTileEntities = Maps.newHashMap();

   public CraftWorld getWorld() {
      return this.world;
   }

   public CraftServer getServer() {
      return (CraftServer)Bukkit.getServer();
   }

   public Chunk getChunkIfLoaded(int x, int z) {
      return ((ChunkProviderServer)this.chunkProvider).getChunkIfLoaded(x, z);
   }

   protected World(ISaveHandler idatamanager, WorldInfo worlddata, WorldProvider worldprovider, Profiler methodprofiler, boolean flag, ChunkGenerator gen, Environment env) {
      this.generator = gen;
      this.world = new CraftWorld((WorldServer)this, gen, env);
      this.ticksPerAnimalSpawns = (long)this.getServer().getTicksPerAnimalSpawns();
      this.ticksPerMonsterSpawns = (long)this.getServer().getTicksPerMonsterSpawns();
      this.eventListeners = Lists.newArrayList(new IWorldEventListener[]{this.pathListener});
      this.theCalendar = Calendar.getInstance();
      this.worldScoreboard = new Scoreboard();
      this.spawnHostileMobs = true;
      this.spawnPeacefulMobs = true;
      this.lightUpdateBlockList = new int['耀'];
      this.saveHandler = idatamanager;
      this.theProfiler = methodprofiler;
      this.worldInfo = worlddata;
      this.provider = worldprovider;
      this.isRemote = flag;
      this.worldBorder = worldprovider.createWorldBorder();
      this.getWorldBorder().world = (WorldServer)this;
      this.getWorldBorder().addListener(new IBorderListener() {
         public void onSizeChanged(WorldBorder worldborder, double d0) {
            World.this.getServer().getHandle().sendAll(new SPacketWorldBorder(worldborder, SPacketWorldBorder.Action.SET_SIZE), worldborder.world);
         }

         public void onTransitionStarted(WorldBorder worldborder, double d0, double d1, long i) {
            World.this.getServer().getHandle().sendAll(new SPacketWorldBorder(worldborder, SPacketWorldBorder.Action.LERP_SIZE), worldborder.world);
         }

         public void onCenterChanged(WorldBorder worldborder, double d0, double d1) {
            World.this.getServer().getHandle().sendAll(new SPacketWorldBorder(worldborder, SPacketWorldBorder.Action.SET_CENTER), worldborder.world);
         }

         public void onWarningTimeChanged(WorldBorder worldborder, int i) {
            World.this.getServer().getHandle().sendAll(new SPacketWorldBorder(worldborder, SPacketWorldBorder.Action.SET_WARNING_TIME), worldborder.world);
         }

         public void onWarningDistanceChanged(WorldBorder worldborder, int i) {
            World.this.getServer().getHandle().sendAll(new SPacketWorldBorder(worldborder, SPacketWorldBorder.Action.SET_WARNING_BLOCKS), worldborder.world);
         }

         public void onDamageAmountChanged(WorldBorder worldborder, double d0) {
         }

         public void onDamageBufferChanged(WorldBorder worldborder, double d0) {
         }
      });
      this.getServer().addWorld(this.world);
   }

   public World init() {
      return this;
   }

   public Biome getBiome(final BlockPos blockposition) {
      if (this.isBlockLoaded(blockposition)) {
         Chunk chunk = this.getChunkFromBlockCoords(blockposition);

         try {
            return chunk.getBiome(blockposition, this.provider.getBiomeProvider());
         } catch (Throwable var6) {
            CrashReport crashreport = CrashReport.makeCrashReport(var6, "Getting biome");
            CrashReportCategory crashreportsystemdetails = crashreport.makeCategory("Coordinates of biome request");
            crashreportsystemdetails.setDetail("Location", new ICrashReportDetail() {
               public String call() throws Exception {
                  return CrashReportCategory.getCoordinateInfo(blockposition);
               }

               public Object call() throws Exception {
                  return this.call();
               }
            });
            throw new ReportedException(crashreport);
         }
      } else {
         return this.provider.getBiomeProvider().getBiome(blockposition, Biomes.PLAINS);
      }
   }

   public BiomeProvider getBiomeProvider() {
      return this.provider.getBiomeProvider();
   }

   protected abstract IChunkProvider createChunkProvider();

   public void initialize(WorldSettings worldsettings) {
      this.worldInfo.setServerInitialized(true);
   }

   @Nullable
   public MinecraftServer getMinecraftServer() {
      return null;
   }

   public IBlockState getGroundAboveSeaLevel(BlockPos blockposition) {
      BlockPos blockposition1;
      for(blockposition1 = new BlockPos(blockposition.getX(), this.getSeaLevel(), blockposition.getZ()); !this.isAirBlock(blockposition1.up()); blockposition1 = blockposition1.up()) {
         ;
      }

      return this.getBlockState(blockposition1);
   }

   private boolean isValid(BlockPos blockposition) {
      return !this.isOutsideBuildHeight(blockposition) && blockposition.getX() >= -30000000 && blockposition.getZ() >= -30000000 && blockposition.getX() < 30000000 && blockposition.getZ() < 30000000;
   }

   private boolean isOutsideBuildHeight(BlockPos blockposition) {
      return blockposition.getY() < 0 || blockposition.getY() >= 256;
   }

   public boolean isAirBlock(BlockPos blockposition) {
      return this.getBlockState(blockposition).getMaterial() == Material.AIR;
   }

   public boolean isBlockLoaded(BlockPos blockposition) {
      return this.isBlockLoaded(blockposition, true);
   }

   public boolean isBlockLoaded(BlockPos blockposition, boolean flag) {
      return this.isChunkLoaded(blockposition.getX() >> 4, blockposition.getZ() >> 4, flag);
   }

   public boolean isAreaLoaded(BlockPos blockposition, int i) {
      return this.isAreaLoaded(blockposition, i, true);
   }

   public boolean isAreaLoaded(BlockPos blockposition, int i, boolean flag) {
      return this.isAreaLoaded(blockposition.getX() - i, blockposition.getY() - i, blockposition.getZ() - i, blockposition.getX() + i, blockposition.getY() + i, blockposition.getZ() + i, flag);
   }

   public boolean isAreaLoaded(BlockPos blockposition, BlockPos blockposition1) {
      return this.isAreaLoaded(blockposition, blockposition1, true);
   }

   public boolean isAreaLoaded(BlockPos blockposition, BlockPos blockposition1, boolean flag) {
      return this.isAreaLoaded(blockposition.getX(), blockposition.getY(), blockposition.getZ(), blockposition1.getX(), blockposition1.getY(), blockposition1.getZ(), flag);
   }

   public boolean isAreaLoaded(StructureBoundingBox structureboundingbox) {
      return this.isAreaLoaded(structureboundingbox, true);
   }

   public boolean isAreaLoaded(StructureBoundingBox structureboundingbox, boolean flag) {
      return this.isAreaLoaded(structureboundingbox.minX, structureboundingbox.minY, structureboundingbox.minZ, structureboundingbox.maxX, structureboundingbox.maxY, structureboundingbox.maxZ, flag);
   }

   private boolean isAreaLoaded(int i, int j, int k, int l, int i1, int j1, boolean flag) {
      if (i1 >= 0 && j < 256) {
         i = i >> 4;
         k = k >> 4;
         l = l >> 4;
         j1 = j1 >> 4;

         for(int k1 = i; k1 <= l; ++k1) {
            for(int l1 = k; l1 <= j1; ++l1) {
               if (!this.isChunkLoaded(k1, l1, flag)) {
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

   public Chunk getChunkFromBlockCoords(BlockPos blockposition) {
      return this.getChunkFromChunkCoords(blockposition.getX() >> 4, blockposition.getZ() >> 4);
   }

   public Chunk getChunkFromChunkCoords(int i, int j) {
      return this.chunkProvider.provideChunk(i, j);
   }

   public boolean setBlockState(BlockPos blockposition, IBlockState iblockdata, int i) {
      if (this.captureTreeGeneration) {
         BlockState blockstate = null;
         Iterator it = this.capturedBlockStates.iterator();

         while(it.hasNext()) {
            BlockState previous = (BlockState)it.next();
            if (previous.getX() == blockposition.getX() && previous.getY() == blockposition.getY() && previous.getZ() == blockposition.getZ()) {
               blockstate = previous;
               it.remove();
               break;
            }
         }

         if (blockstate == null) {
            blockstate = CraftBlockState.getBlockState(this, blockposition.getX(), blockposition.getY(), blockposition.getZ(), i);
         }

         blockstate.setTypeId(CraftMagicNumbers.getId(iblockdata.getBlock()));
         blockstate.setRawData((byte)iblockdata.getBlock().getMetaFromState(iblockdata));
         this.capturedBlockStates.add(blockstate);
         return true;
      } else if (this.isOutsideBuildHeight(blockposition)) {
         return false;
      } else if (!this.isRemote && this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) {
         return false;
      } else {
         Chunk chunk = this.getChunkFromBlockCoords(blockposition);
         iblockdata.getBlock();
         BlockState blockstate = null;
         if (this.captureBlockStates) {
            blockstate = CraftBlockState.getBlockState(this, blockposition.getX(), blockposition.getY(), blockposition.getZ(), i);
            this.capturedBlockStates.add(blockstate);
         }

         IBlockState iblockdata1 = chunk.setBlockState(blockposition, iblockdata);
         if (iblockdata1 == null) {
            if (this.captureBlockStates) {
               this.capturedBlockStates.remove(blockstate);
            }

            return false;
         } else {
            if (iblockdata.getLightOpacity() != iblockdata1.getLightOpacity() || iblockdata.getLightValue() != iblockdata1.getLightValue()) {
               this.theProfiler.startSection("checkLight");
               this.checkLight(blockposition);
               this.theProfiler.endSection();
            }

            if (!this.captureBlockStates) {
               this.notifyAndUpdatePhysics(blockposition, chunk, iblockdata1, iblockdata, i);
            }

            return true;
         }
      }
   }

   public void notifyAndUpdatePhysics(BlockPos blockposition, Chunk chunk, IBlockState oldBlock, IBlockState newBlock, int flag) {
      if ((flag & 2) != 0 && (chunk == null || chunk.isPopulated())) {
         this.notifyBlockUpdate(blockposition, oldBlock, newBlock, flag);
      }

      if (!this.isRemote && (flag & 1) != 0) {
         this.notifyNeighborsRespectDebug(blockposition, oldBlock.getBlock());
         if (newBlock.hasComparatorInputOverride()) {
            this.updateComparatorOutputLevel(blockposition, newBlock.getBlock());
         }
      }

   }

   public boolean setBlockToAir(BlockPos blockposition) {
      return this.setBlockState(blockposition, Blocks.AIR.getDefaultState(), 3);
   }

   public boolean destroyBlock(BlockPos blockposition, boolean flag) {
      IBlockState iblockdata = this.getBlockState(blockposition);
      Block block = iblockdata.getBlock();
      if (iblockdata.getMaterial() == Material.AIR) {
         return false;
      } else {
         this.playEvent(2001, blockposition, Block.getStateId(iblockdata));
         if (flag) {
            block.dropBlockAsItem(this, blockposition, iblockdata, 0);
         }

         return this.setBlockState(blockposition, Blocks.AIR.getDefaultState(), 3);
      }
   }

   public boolean setBlockState(BlockPos blockposition, IBlockState iblockdata) {
      return this.setBlockState(blockposition, iblockdata, 3);
   }

   public void notifyBlockUpdate(BlockPos blockposition, IBlockState iblockdata, IBlockState iblockdata1, int i) {
      for(int j = 0; j < this.eventListeners.size(); ++j) {
         ((IWorldEventListener)this.eventListeners.get(j)).notifyBlockUpdate(this, blockposition, iblockdata, iblockdata1, i);
      }

   }

   public void notifyNeighborsRespectDebug(BlockPos blockposition, Block block) {
      if (this.worldInfo.getTerrainType() != WorldType.DEBUG_WORLD) {
         if (this.populating) {
            return;
         }

         this.notifyNeighborsOfStateChange(blockposition, block);
      }

   }

   public void markBlocksDirtyVertical(int i, int j, int k, int l) {
      if (k > l) {
         int i1 = l;
         l = k;
         k = i1;
      }

      if (!this.provider.hasNoSky()) {
         for(int i1 = k; i1 <= l; ++i1) {
            this.checkLightFor(EnumSkyBlock.SKY, new BlockPos(i, i1, j));
         }
      }

      this.markBlockRangeForRenderUpdate(i, k, j, i, l, j);
   }

   public void markBlockRangeForRenderUpdate(BlockPos blockposition, BlockPos blockposition1) {
      this.markBlockRangeForRenderUpdate(blockposition.getX(), blockposition.getY(), blockposition.getZ(), blockposition1.getX(), blockposition1.getY(), blockposition1.getZ());
   }

   public void markBlockRangeForRenderUpdate(int i, int j, int k, int l, int i1, int j1) {
      for(int k1 = 0; k1 < this.eventListeners.size(); ++k1) {
         ((IWorldEventListener)this.eventListeners.get(k1)).markBlockRangeForRenderUpdate(i, j, k, l, i1, j1);
      }

   }

   public void notifyNeighborsOfStateChange(BlockPos blockposition, Block block) {
      this.notifyBlockOfStateChange(blockposition.west(), block);
      this.notifyBlockOfStateChange(blockposition.east(), block);
      this.notifyBlockOfStateChange(blockposition.down(), block);
      this.notifyBlockOfStateChange(blockposition.up(), block);
      this.notifyBlockOfStateChange(blockposition.north(), block);
      this.notifyBlockOfStateChange(blockposition.south(), block);
   }

   public void notifyNeighborsOfStateExcept(BlockPos blockposition, Block block, EnumFacing enumdirection) {
      if (enumdirection != EnumFacing.WEST) {
         this.notifyBlockOfStateChange(blockposition.west(), block);
      }

      if (enumdirection != EnumFacing.EAST) {
         this.notifyBlockOfStateChange(blockposition.east(), block);
      }

      if (enumdirection != EnumFacing.DOWN) {
         this.notifyBlockOfStateChange(blockposition.down(), block);
      }

      if (enumdirection != EnumFacing.UP) {
         this.notifyBlockOfStateChange(blockposition.up(), block);
      }

      if (enumdirection != EnumFacing.NORTH) {
         this.notifyBlockOfStateChange(blockposition.north(), block);
      }

      if (enumdirection != EnumFacing.SOUTH) {
         this.notifyBlockOfStateChange(blockposition.south(), block);
      }

   }

   public void notifyBlockOfStateChange(BlockPos blockposition, final Block block) {
      if (!this.isRemote) {
         IBlockState iblockdata = this.getBlockState(blockposition);

         try {
            CraftWorld world = ((WorldServer)this).getWorld();
            if (world != null) {
               BlockPhysicsEvent event = new BlockPhysicsEvent(world.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()), CraftMagicNumbers.getId(block));
               this.getServer().getPluginManager().callEvent(event);
               if (event.isCancelled()) {
                  return;
               }
            }

            iblockdata.neighborChanged(this, blockposition, block);
         } catch (Throwable var7) {
            CrashReport crashreport = CrashReport.makeCrashReport(var7, "Exception while updating neighbours");
            CrashReportCategory crashreportsystemdetails = crashreport.makeCategory("Block being updated");
            crashreportsystemdetails.setDetail("Source block type", new ICrashReportDetail() {
               public String call() throws Exception {
                  try {
                     return String.format("ID #%d (%s // %s)", Block.getIdFromBlock(block), block.getUnlocalizedName(), block.getClass().getCanonicalName());
                  } catch (Throwable var1) {
                     return "ID #" + Block.getIdFromBlock(block);
                  }
               }

               public Object call() throws Exception {
                  return this.call();
               }
            });
            CrashReportCategory.addBlockInfo(crashreportsystemdetails, blockposition, iblockdata);
            throw new ReportedException(crashreport);
         }
      }

   }

   public boolean isBlockTickPending(BlockPos blockposition, Block block) {
      return false;
   }

   public boolean canSeeSky(BlockPos blockposition) {
      return this.getChunkFromBlockCoords(blockposition).canSeeSky(blockposition);
   }

   public boolean canBlockSeeSky(BlockPos blockposition) {
      if (blockposition.getY() >= this.getSeaLevel()) {
         return this.canSeeSky(blockposition);
      } else {
         BlockPos blockposition1 = new BlockPos(blockposition.getX(), this.getSeaLevel(), blockposition.getZ());
         if (!this.canSeeSky(blockposition1)) {
            return false;
         } else {
            for(BlockPos var4 = blockposition1.down(); var4.getY() > blockposition.getY(); var4 = var4.down()) {
               IBlockState iblockdata = this.getBlockState(var4);
               if (iblockdata.getLightOpacity() > 0 && !iblockdata.getMaterial().isLiquid()) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public int getLight(BlockPos blockposition) {
      if (blockposition.getY() < 0) {
         return 0;
      } else {
         if (blockposition.getY() >= 256) {
            blockposition = new BlockPos(blockposition.getX(), 255, blockposition.getZ());
         }

         return this.getChunkFromBlockCoords(blockposition).getLightSubtracted(blockposition, 0);
      }
   }

   public int getLightFromNeighbors(BlockPos blockposition) {
      return this.getLight(blockposition, true);
   }

   public int getLight(BlockPos blockposition, boolean flag) {
      if (blockposition.getX() >= -30000000 && blockposition.getZ() >= -30000000 && blockposition.getX() < 30000000 && blockposition.getZ() < 30000000) {
         if (flag && this.getBlockState(blockposition).useNeighborBrightness()) {
            int i = this.getLight(blockposition.up(), false);
            int j = this.getLight(blockposition.east(), false);
            int k = this.getLight(blockposition.west(), false);
            int l = this.getLight(blockposition.south(), false);
            int i1 = this.getLight(blockposition.north(), false);
            if (j > i) {
               i = j;
            }

            if (k > i) {
               i = k;
            }

            if (l > i) {
               i = l;
            }

            if (i1 > i) {
               i = i1;
            }

            return i;
         } else if (blockposition.getY() < 0) {
            return 0;
         } else {
            if (blockposition.getY() >= 256) {
               blockposition = new BlockPos(blockposition.getX(), 255, blockposition.getZ());
            }

            Chunk chunk = this.getChunkFromBlockCoords(blockposition);
            return chunk.getLightSubtracted(blockposition, this.skylightSubtracted);
         }
      } else {
         return 15;
      }
   }

   public BlockPos getHeight(BlockPos blockposition) {
      return new BlockPos(blockposition.getX(), this.getHeight(blockposition.getX(), blockposition.getZ()), blockposition.getZ());
   }

   public int getHeight(int i, int j) {
      int k;
      if (i >= -30000000 && j >= -30000000 && i < 30000000 && j < 30000000) {
         if (this.isChunkLoaded(i >> 4, j >> 4, true)) {
            k = this.getChunkFromChunkCoords(i >> 4, j >> 4).getHeightValue(i & 15, j & 15);
         } else {
            k = 0;
         }
      } else {
         k = this.getSeaLevel() + 1;
      }

      return k;
   }

   /** @deprecated */
   @Deprecated
   public int getChunksLowestHorizon(int i, int j) {
      if (i >= -30000000 && j >= -30000000 && i < 30000000 && j < 30000000) {
         if (!this.isChunkLoaded(i >> 4, j >> 4, true)) {
            return 0;
         } else {
            Chunk chunk = this.getChunkFromChunkCoords(i >> 4, j >> 4);
            return chunk.getLowestHeight();
         }
      } else {
         return this.getSeaLevel() + 1;
      }
   }

   public int getLightFor(EnumSkyBlock enumskyblock, BlockPos blockposition) {
      if (blockposition.getY() < 0) {
         blockposition = new BlockPos(blockposition.getX(), 0, blockposition.getZ());
      }

      if (!this.isValid(blockposition)) {
         return enumskyblock.defaultLightValue;
      } else if (!this.isBlockLoaded(blockposition)) {
         return enumskyblock.defaultLightValue;
      } else {
         Chunk chunk = this.getChunkFromBlockCoords(blockposition);
         return chunk.getLightFor(enumskyblock, blockposition);
      }
   }

   public void setLightFor(EnumSkyBlock enumskyblock, BlockPos blockposition, int i) {
      if (this.isValid(blockposition) && this.isBlockLoaded(blockposition)) {
         Chunk chunk = this.getChunkFromBlockCoords(blockposition);
         chunk.setLightFor(enumskyblock, blockposition, i);
         this.notifyLightSet(blockposition);
      }

   }

   public void notifyLightSet(BlockPos blockposition) {
      for(int i = 0; i < this.eventListeners.size(); ++i) {
         ((IWorldEventListener)this.eventListeners.get(i)).notifyLightSet(blockposition);
      }

   }

   public float getLightBrightness(BlockPos blockposition) {
      return this.provider.getLightBrightnessTable()[this.getLightFromNeighbors(blockposition)];
   }

   public IBlockState getBlockState(BlockPos blockposition) {
      if (this.captureTreeGeneration) {
         for(BlockState previous : this.capturedBlockStates) {
            if (previous.getX() == blockposition.getX() && previous.getY() == blockposition.getY() && previous.getZ() == blockposition.getZ()) {
               return CraftMagicNumbers.getBlock(previous.getTypeId()).getStateFromMeta(previous.getRawData());
            }
         }
      }

      if (this.isOutsideBuildHeight(blockposition)) {
         return Blocks.AIR.getDefaultState();
      } else {
         Chunk chunk = this.getChunkFromBlockCoords(blockposition);
         return chunk.getBlockState(blockposition);
      }
   }

   public boolean isDaytime() {
      return this.skylightSubtracted < 4;
   }

   @Nullable
   public RayTraceResult rayTraceBlocks(Vec3d vec3d, Vec3d vec3d1) {
      return this.rayTraceBlocks(vec3d, vec3d1, false, false, false);
   }

   @Nullable
   public RayTraceResult rayTraceBlocks(Vec3d vec3d, Vec3d vec3d1, boolean flag) {
      return this.rayTraceBlocks(vec3d, vec3d1, flag, false, false);
   }

   @Nullable
   public RayTraceResult rayTraceBlocks(Vec3d vec3d, Vec3d vec3d1, boolean flag, boolean flag1, boolean flag2) {
      if (!Double.isNaN(vec3d.xCoord) && !Double.isNaN(vec3d.yCoord) && !Double.isNaN(vec3d.zCoord)) {
         if (!Double.isNaN(vec3d1.xCoord) && !Double.isNaN(vec3d1.yCoord) && !Double.isNaN(vec3d1.zCoord)) {
            int i = MathHelper.floor(vec3d1.xCoord);
            int j = MathHelper.floor(vec3d1.yCoord);
            int k = MathHelper.floor(vec3d1.zCoord);
            int l = MathHelper.floor(vec3d.xCoord);
            int i1 = MathHelper.floor(vec3d.yCoord);
            int j1 = MathHelper.floor(vec3d.zCoord);
            BlockPos blockposition = new BlockPos(l, i1, j1);
            IBlockState iblockdata = this.getBlockState(blockposition);
            Block block = iblockdata.getBlock();
            if ((!flag1 || iblockdata.getCollisionBoundingBox(this, blockposition) != Block.NULL_AABB) && block.canCollideCheck(iblockdata, flag)) {
               RayTraceResult movingobjectposition = iblockdata.collisionRayTrace(this, blockposition, vec3d, vec3d1);
               if (movingobjectposition != null) {
                  return movingobjectposition;
               }
            }

            RayTraceResult movingobjectposition1 = null;
            int k1 = 200;

            while(k1-- >= 0) {
               if (Double.isNaN(vec3d.xCoord) || Double.isNaN(vec3d.yCoord) || Double.isNaN(vec3d.zCoord)) {
                  return null;
               }

               if (l == i && i1 == j && j1 == k) {
                  return flag2 ? movingobjectposition1 : null;
               }

               boolean flag3 = true;
               boolean flag4 = true;
               boolean flag5 = true;
               double d0 = 999.0D;
               double d1 = 999.0D;
               double d2 = 999.0D;
               if (i > l) {
                  d0 = (double)l + 1.0D;
               } else if (i < l) {
                  d0 = (double)l + 0.0D;
               } else {
                  flag3 = false;
               }

               if (j > i1) {
                  d1 = (double)i1 + 1.0D;
               } else if (j < i1) {
                  d1 = (double)i1 + 0.0D;
               } else {
                  flag4 = false;
               }

               if (k > j1) {
                  d2 = (double)j1 + 1.0D;
               } else if (k < j1) {
                  d2 = (double)j1 + 0.0D;
               } else {
                  flag5 = false;
               }

               double d3 = 999.0D;
               double d4 = 999.0D;
               double d5 = 999.0D;
               double d6 = vec3d1.xCoord - vec3d.xCoord;
               double d7 = vec3d1.yCoord - vec3d.yCoord;
               double d8 = vec3d1.zCoord - vec3d.zCoord;
               if (flag3) {
                  d3 = (d0 - vec3d.xCoord) / d6;
               }

               if (flag4) {
                  d4 = (d1 - vec3d.yCoord) / d7;
               }

               if (flag5) {
                  d5 = (d2 - vec3d.zCoord) / d8;
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

               EnumFacing enumdirection;
               if (d3 < d4 && d3 < d5) {
                  enumdirection = i > l ? EnumFacing.WEST : EnumFacing.EAST;
                  vec3d = new Vec3d(d0, vec3d.yCoord + d7 * d3, vec3d.zCoord + d8 * d3);
               } else if (d4 < d5) {
                  enumdirection = j > i1 ? EnumFacing.DOWN : EnumFacing.UP;
                  vec3d = new Vec3d(vec3d.xCoord + d6 * d4, d1, vec3d.zCoord + d8 * d4);
               } else {
                  enumdirection = k > j1 ? EnumFacing.NORTH : EnumFacing.SOUTH;
                  vec3d = new Vec3d(vec3d.xCoord + d6 * d5, vec3d.yCoord + d7 * d5, d2);
               }

               l = MathHelper.floor(vec3d.xCoord) - (enumdirection == EnumFacing.EAST ? 1 : 0);
               i1 = MathHelper.floor(vec3d.yCoord) - (enumdirection == EnumFacing.UP ? 1 : 0);
               j1 = MathHelper.floor(vec3d.zCoord) - (enumdirection == EnumFacing.SOUTH ? 1 : 0);
               blockposition = new BlockPos(l, i1, j1);
               IBlockState iblockdata1 = this.getBlockState(blockposition);
               Block block1 = iblockdata1.getBlock();
               if (!flag1 || iblockdata1.getMaterial() == Material.PORTAL || iblockdata1.getCollisionBoundingBox(this, blockposition) != Block.NULL_AABB) {
                  if (block1.canCollideCheck(iblockdata1, flag)) {
                     RayTraceResult movingobjectposition2 = iblockdata1.collisionRayTrace(this, blockposition, vec3d, vec3d1);
                     if (movingobjectposition2 != null) {
                        return movingobjectposition2;
                     }
                  } else {
                     movingobjectposition1 = new RayTraceResult(RayTraceResult.Type.MISS, vec3d, enumdirection, blockposition);
                  }
               }
            }

            return flag2 ? movingobjectposition1 : null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public void playSound(@Nullable EntityPlayer entityhuman, BlockPos blockposition, SoundEvent soundeffect, SoundCategory soundcategory, float f, float f1) {
      this.playSound(entityhuman, (double)blockposition.getX() + 0.5D, (double)blockposition.getY() + 0.5D, (double)blockposition.getZ() + 0.5D, soundeffect, soundcategory, f, f1);
   }

   public void playSound(@Nullable EntityPlayer entityhuman, double d0, double d1, double d2, SoundEvent soundeffect, SoundCategory soundcategory, float f, float f1) {
      for(int i = 0; i < this.eventListeners.size(); ++i) {
         ((IWorldEventListener)this.eventListeners.get(i)).playSoundToAllNearExcept(entityhuman, soundeffect, soundcategory, d0, d1, d2, f, f1);
      }

   }

   public void playSound(double d0, double d1, double d2, SoundEvent soundeffect, SoundCategory soundcategory, float f, float f1, boolean flag) {
   }

   public void playRecord(BlockPos blockposition, @Nullable SoundEvent soundeffect) {
      for(int i = 0; i < this.eventListeners.size(); ++i) {
         ((IWorldEventListener)this.eventListeners.get(i)).playRecord(soundeffect, blockposition);
      }

   }

   public void spawnParticle(EnumParticleTypes enumparticle, double d0, double d1, double d2, double d3, double d4, double d5, int... aint) {
      this.spawnParticle(enumparticle.getParticleID(), enumparticle.getShouldIgnoreRange(), d0, d1, d2, d3, d4, d5, aint);
   }

   private void spawnParticle(int i, boolean flag, double d0, double d1, double d2, double d3, double d4, double d5, int... aint) {
      for(int j = 0; j < this.eventListeners.size(); ++j) {
         ((IWorldEventListener)this.eventListeners.get(j)).spawnParticle(i, flag, d0, d1, d2, d3, d4, d5, aint);
      }

   }

   public boolean addWeatherEffect(Entity entity) {
      this.weatherEffects.add(entity);
      return true;
   }

   public boolean spawnEntity(Entity entity) {
      return this.addEntity(entity, SpawnReason.DEFAULT);
   }

   public boolean addEntity(Entity entity, SpawnReason spawnReason) {
      if (entity == null) {
         return false;
      } else {
         int i = MathHelper.floor(entity.posX / 16.0D);
         int j = MathHelper.floor(entity.posZ / 16.0D);
         boolean flag = entity.forceSpawn;
         if (entity instanceof EntityPlayer) {
            flag = true;
         }

         Cancellable event = null;
         if (entity instanceof EntityLivingBase && !(entity instanceof EntityPlayerMP)) {
            boolean isAnimal = entity instanceof EntityAnimal || entity instanceof EntityWaterMob || entity instanceof EntityGolem;
            boolean isMonster = entity instanceof EntityMob || entity instanceof EntityGhast || entity instanceof EntitySlime;
            if (spawnReason != SpawnReason.CUSTOM && (isAnimal && !this.spawnPeacefulMobs || isMonster && !this.spawnHostileMobs)) {
               entity.isDead = true;
               return false;
            }

            event = CraftEventFactory.callCreatureSpawnEvent((EntityLivingBase)entity, spawnReason);
         } else if (entity instanceof EntityItem) {
            event = CraftEventFactory.callItemSpawnEvent((EntityItem)entity);
         } else if (entity.getBukkitEntity() instanceof Projectile) {
            event = CraftEventFactory.callProjectileLaunchEvent(entity);
         }

         if (event == null || !event.isCancelled() && !entity.isDead) {
            if (!flag && !this.isChunkLoaded(i, j, false)) {
               return false;
            } else {
               if (entity instanceof EntityPlayer) {
                  EntityPlayer entityhuman = (EntityPlayer)entity;
                  this.playerEntities.add(entityhuman);
                  this.updateAllPlayersSleepingFlag();
               }

               this.getChunkFromChunkCoords(i, j).addEntity(entity);
               this.loadedEntityList.add(entity);
               this.onEntityAdded(entity);
               return true;
            }
         } else {
            entity.isDead = true;
            return false;
         }
      }
   }

   protected void onEntityAdded(Entity entity) {
      for(int i = 0; i < this.eventListeners.size(); ++i) {
         ((IWorldEventListener)this.eventListeners.get(i)).onEntityAdded(entity);
      }

      entity.valid = true;
   }

   protected void onEntityRemoved(Entity entity) {
      for(int i = 0; i < this.eventListeners.size(); ++i) {
         ((IWorldEventListener)this.eventListeners.get(i)).onEntityRemoved(entity);
      }

      entity.valid = false;
   }

   public void removeEntity(Entity entity) {
      if (entity.isBeingRidden()) {
         entity.removePassengers();
      }

      if (entity.isRiding()) {
         entity.dismountRidingEntity();
      }

      entity.setDead();
      if (entity instanceof EntityPlayer) {
         this.playerEntities.remove(entity);
         this.updateAllPlayersSleepingFlag();
         this.onEntityRemoved(entity);
      }

   }

   public void removeEntityDangerously(Entity entity) {
      entity.setDropItemsWhenDead(false);
      entity.setDead();
      if (entity instanceof EntityPlayer) {
         this.playerEntities.remove(entity);
         this.updateAllPlayersSleepingFlag();
      }

      int i = entity.chunkCoordX;
      int j = entity.chunkCoordZ;
      if (entity.addedToChunk && this.isChunkLoaded(i, j, true)) {
         this.getChunkFromChunkCoords(i, j).removeEntity(entity);
      }

      int index = this.loadedEntityList.indexOf(entity);
      if (index != -1) {
         if (index <= this.tickPosition) {
            --this.tickPosition;
         }

         this.loadedEntityList.remove(index);
      }

      this.onEntityRemoved(entity);
   }

   public void addEventListener(IWorldEventListener iworldaccess) {
      this.eventListeners.add(iworldaccess);
   }

   public List getCollisionBoxes(@Nullable Entity entity, AxisAlignedBB axisalignedbb) {
      ArrayList arraylist = Lists.newArrayList();
      int i = MathHelper.floor(axisalignedbb.minX) - 1;
      int j = MathHelper.ceil(axisalignedbb.maxX) + 1;
      int k = MathHelper.floor(axisalignedbb.minY) - 1;
      int l = MathHelper.ceil(axisalignedbb.maxY) + 1;
      int i1 = MathHelper.floor(axisalignedbb.minZ) - 1;
      int j1 = MathHelper.ceil(axisalignedbb.maxZ) + 1;
      WorldBorder worldborder = this.getWorldBorder();
      boolean flag = entity != null && entity.isOutsideBorder();
      boolean flag1 = entity != null && this.isInsideBorder(worldborder, entity);
      IBlockState iblockdata = Blocks.STONE.getDefaultState();
      BlockPos.PooledMutableBlockPos blockposition_pooledblockposition = BlockPos.PooledMutableBlockPos.retain();

      for(int l1 = i; l1 < j; ++l1) {
         for(int k1 = i1; k1 < j1; ++k1) {
            int i2 = (l1 != i && l1 != j - 1 ? 0 : 1) + (k1 != i1 && k1 != j1 - 1 ? 0 : 1);
            if (i2 != 2 && this.isBlockLoaded(blockposition_pooledblockposition.setPos(l1, 64, k1))) {
               for(int j2 = k; j2 < l; ++j2) {
                  if (i2 <= 0 || j2 != k && j2 != l - 1) {
                     blockposition_pooledblockposition.setPos(l1, j2, k1);
                     if (entity != null) {
                        if (flag && flag1) {
                           entity.setOutsideBorder(false);
                        } else if (!flag && !flag1) {
                           entity.setOutsideBorder(true);
                        }
                     }

                     IBlockState iblockdata1 = iblockdata;
                     if (worldborder.contains(blockposition_pooledblockposition) || !flag1) {
                        iblockdata1 = this.getBlockState(blockposition_pooledblockposition);
                     }

                     iblockdata1.addCollisionBoxToList(this, blockposition_pooledblockposition, axisalignedbb, arraylist, entity);
                  }
               }
            }
         }
      }

      blockposition_pooledblockposition.release();
      if (entity != null) {
         List list = this.getEntitiesWithinAABBExcludingEntity(entity, axisalignedbb.expandXyz(0.25D));

         for(int k1 = 0; k1 < list.size(); ++k1) {
            Entity entity1 = (Entity)list.get(k1);
            if (!entity.isRidingSameEntity(entity1)) {
               AxisAlignedBB axisalignedbb1 = entity1.getCollisionBoundingBox();
               if (axisalignedbb1 != null && axisalignedbb1.intersectsWith(axisalignedbb)) {
                  arraylist.add(axisalignedbb1);
               }

               axisalignedbb1 = entity.getCollisionBox(entity1);
               if (axisalignedbb1 != null && axisalignedbb1.intersectsWith(axisalignedbb)) {
                  arraylist.add(axisalignedbb1);
               }
            }
         }
      }

      return arraylist;
   }

   public boolean isInsideBorder(WorldBorder worldborder, Entity entity) {
      double d0 = worldborder.minX();
      double d1 = worldborder.minZ();
      double d2 = worldborder.maxX();
      double d3 = worldborder.maxZ();
      if (entity.isOutsideBorder()) {
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

      return entity.posX > d0 && entity.posX < d2 && entity.posZ > d1 && entity.posZ < d3;
   }

   public List getCollisionBoxes(AxisAlignedBB axisalignedbb) {
      ArrayList arraylist = Lists.newArrayList();
      int i = MathHelper.floor(axisalignedbb.minX) - 1;
      int j = MathHelper.ceil(axisalignedbb.maxX) + 1;
      int k = MathHelper.floor(axisalignedbb.minY) - 1;
      int l = MathHelper.ceil(axisalignedbb.maxY) + 1;
      int i1 = MathHelper.floor(axisalignedbb.minZ) - 1;
      int j1 = MathHelper.ceil(axisalignedbb.maxZ) + 1;
      BlockPos.PooledMutableBlockPos blockposition_pooledblockposition = BlockPos.PooledMutableBlockPos.retain();

      for(int k1 = i; k1 < j; ++k1) {
         for(int l1 = i1; l1 < j1; ++l1) {
            int i2 = (k1 != i && k1 != j - 1 ? 0 : 1) + (l1 != i1 && l1 != j1 - 1 ? 0 : 1);
            if (i2 != 2 && this.isBlockLoaded(blockposition_pooledblockposition.setPos(k1, 64, l1))) {
               for(int j2 = k; j2 < l; ++j2) {
                  if (i2 <= 0 || j2 != k && j2 != l - 1) {
                     blockposition_pooledblockposition.setPos(k1, j2, l1);
                     IBlockState iblockdata;
                     if (k1 >= -30000000 && k1 < 30000000 && l1 >= -30000000 && l1 < 30000000) {
                        iblockdata = this.getBlockState(blockposition_pooledblockposition);
                     } else {
                        iblockdata = Blocks.BEDROCK.getDefaultState();
                     }

                     iblockdata.addCollisionBoxToList(this, blockposition_pooledblockposition, axisalignedbb, arraylist, (Entity)null);
                  }
               }
            }
         }
      }

      blockposition_pooledblockposition.release();
      return arraylist;
   }

   public boolean collidesWithAnyBlock(AxisAlignedBB axisalignedbb) {
      ArrayList arraylist = Lists.newArrayList();
      int i = MathHelper.floor(axisalignedbb.minX) - 1;
      int j = MathHelper.ceil(axisalignedbb.maxX) + 1;
      int k = MathHelper.floor(axisalignedbb.minY) - 1;
      int l = MathHelper.ceil(axisalignedbb.maxY) + 1;
      int i1 = MathHelper.floor(axisalignedbb.minZ) - 1;
      int j1 = MathHelper.ceil(axisalignedbb.maxZ) + 1;
      BlockPos.PooledMutableBlockPos blockposition_pooledblockposition = BlockPos.PooledMutableBlockPos.retain();

      try {
         for(int k1 = i; k1 < j; ++k1) {
            for(int l1 = i1; l1 < j1; ++l1) {
               int i2 = (k1 != i && k1 != j - 1 ? 0 : 1) + (l1 != i1 && l1 != j1 - 1 ? 0 : 1);
               if (i2 != 2 && this.isBlockLoaded(blockposition_pooledblockposition.setPos(k1, 64, l1))) {
                  for(int j2 = k; j2 < l; ++j2) {
                     if (i2 <= 0 || j2 != k && j2 != l - 1) {
                        blockposition_pooledblockposition.setPos(k1, j2, l1);
                        if (k1 < -30000000 || k1 >= 30000000 || l1 < -30000000 || l1 >= 30000000) {
                           boolean flag = true;
                           boolean var21 = flag;
                           return var21;
                        }

                        IBlockState iblockdata = this.getBlockState(blockposition_pooledblockposition);
                        iblockdata.addCollisionBoxToList(this, blockposition_pooledblockposition, axisalignedbb, arraylist, (Entity)null);
                        if (!arraylist.isEmpty()) {
                           boolean flag1 = true;
                           boolean var15 = flag1;
                           return var15;
                        }
                     }
                  }
               }
            }
         }

         return false;
      } finally {
         blockposition_pooledblockposition.release();
      }
   }

   public int calculateSkylightSubtracted(float f) {
      float f1 = this.getCelestialAngle(f);
      float f2 = 1.0F - (MathHelper.cos(f1 * 6.2831855F) * 2.0F + 0.5F);
      f2 = MathHelper.clamp(f2, 0.0F, 1.0F);
      f2 = 1.0F - f2;
      f2 = (float)((double)f2 * (1.0D - (double)(this.getRainStrength(f) * 5.0F) / 16.0D));
      f2 = (float)((double)f2 * (1.0D - (double)(this.getThunderStrength(f) * 5.0F) / 16.0D));
      f2 = 1.0F - f2;
      return (int)(f2 * 11.0F);
   }

   public float getCelestialAngle(float f) {
      return this.provider.calculateCelestialAngle(this.worldInfo.getWorldTime(), f);
   }

   public float getCurrentMoonPhaseFactor() {
      return WorldProvider.MOON_PHASE_FACTORS[this.provider.getMoonPhase(this.worldInfo.getWorldTime())];
   }

   public float getCelestialAngleRadians(float f) {
      float f1 = this.getCelestialAngle(f);
      return f1 * 6.2831855F;
   }

   public BlockPos getPrecipitationHeight(BlockPos blockposition) {
      return this.getChunkFromBlockCoords(blockposition).getPrecipitationHeight(blockposition);
   }

   public BlockPos getTopSolidOrLiquidBlock(BlockPos blockposition) {
      Chunk chunk = this.getChunkFromBlockCoords(blockposition);

      BlockPos blockposition1;
      BlockPos blockposition2;
      for(blockposition1 = new BlockPos(blockposition.getX(), chunk.getTopFilledSegment() + 16, blockposition.getZ()); blockposition1.getY() >= 0; blockposition1 = blockposition2) {
         blockposition2 = blockposition1.down();
         Material material = chunk.getBlockState(blockposition2).getMaterial();
         if (material.blocksMovement() && material != Material.LEAVES) {
            break;
         }
      }

      return blockposition1;
   }

   public boolean isUpdateScheduled(BlockPos blockposition, Block block) {
      return true;
   }

   public void scheduleUpdate(BlockPos blockposition, Block block, int i) {
   }

   public void updateBlockTick(BlockPos blockposition, Block block, int i, int j) {
   }

   public void scheduleBlockUpdate(BlockPos blockposition, Block block, int i, int j) {
   }

   public void updateEntities() {
      this.theProfiler.startSection("entities");
      this.theProfiler.startSection("global");

      for(int i = 0; i < this.weatherEffects.size(); ++i) {
         Entity entity = (Entity)this.weatherEffects.get(i);
         if (entity != null) {
            try {
               ++entity.ticksExisted;
               entity.onUpdate();
            } catch (Throwable var13) {
               CrashReport crashreport = CrashReport.makeCrashReport(var13, "Ticking entity");
               CrashReportCategory crashreportsystemdetails = crashreport.makeCategory("Entity being ticked");
               if (entity == null) {
                  crashreportsystemdetails.addCrashSection("Entity", "~~NULL~~");
               } else {
                  entity.addEntityCrashInfo(crashreportsystemdetails);
               }

               throw new ReportedException(crashreport);
            }

            if (entity.isDead) {
               this.weatherEffects.remove(i--);
            }
         }
      }

      this.theProfiler.endStartSection("remove");
      this.loadedEntityList.removeAll(this.unloadedEntityList);

      for(int var14 = 0; var14 < this.unloadedEntityList.size(); ++var14) {
         Entity entity = (Entity)this.unloadedEntityList.get(var14);
         int k = entity.chunkCoordX;
         int j = entity.chunkCoordZ;
         if (entity.addedToChunk && this.isChunkLoaded(k, j, true)) {
            this.getChunkFromChunkCoords(k, j).removeEntity(entity);
         }
      }

      for(int var15 = 0; var15 < this.unloadedEntityList.size(); ++var15) {
         this.onEntityRemoved((Entity)this.unloadedEntityList.get(var15));
      }

      this.unloadedEntityList.clear();
      this.tickPlayers();
      this.theProfiler.endStartSection("regular");

      for(this.tickPosition = 0; this.tickPosition < this.loadedEntityList.size(); ++this.tickPosition) {
         Entity entity = (Entity)this.loadedEntityList.get(this.tickPosition);
         Entity entity1 = entity.getRidingEntity();
         if (entity1 != null) {
            if (!entity1.isDead && entity1.isPassenger(entity)) {
               continue;
            }

            entity.dismountRidingEntity();
         }

         this.theProfiler.startSection("tick");
         if (!entity.isDead && !(entity instanceof EntityPlayerMP)) {
            try {
               this.updateEntity(entity);
            } catch (Throwable var12) {
               CrashReport crashreport1 = CrashReport.makeCrashReport(var12, "Ticking entity");
               CrashReportCategory crashreportsystemdetails1 = crashreport1.makeCategory("Entity being ticked");
               entity.addEntityCrashInfo(crashreportsystemdetails1);
               throw new ReportedException(crashreport1);
            }
         }

         this.theProfiler.endSection();
         this.theProfiler.startSection("remove");
         if (entity.isDead) {
            int j = entity.chunkCoordX;
            int l = entity.chunkCoordZ;
            if (entity.addedToChunk && this.isChunkLoaded(j, l, true)) {
               this.getChunkFromChunkCoords(j, l).removeEntity(entity);
            }

            this.loadedEntityList.remove(this.tickPosition--);
            this.onEntityRemoved(entity);
         }

         this.theProfiler.endSection();
      }

      this.theProfiler.endStartSection("blockEntities");
      this.processingLoadedTiles = true;
      if (!this.tileEntitiesToBeRemoved.isEmpty()) {
         this.tickableTileEntities.removeAll(this.tileEntitiesToBeRemoved);
         this.loadedTileEntityList.removeAll(this.tileEntitiesToBeRemoved);
         this.tileEntitiesToBeRemoved.clear();
      }

      Iterator iterator = this.tickableTileEntities.iterator();

      while(iterator.hasNext()) {
         TileEntity tileentity = (TileEntity)iterator.next();
         if (!tileentity.isInvalid() && tileentity.hasWorld()) {
            BlockPos blockposition = tileentity.getPos();
            if (this.isBlockLoaded(blockposition) && this.worldBorder.contains(blockposition)) {
               try {
                  this.theProfiler.startSection("");
                  ((ITickable)tileentity).update();
                  this.theProfiler.endSection();
               } catch (Throwable var11) {
                  CrashReport crashreport1 = CrashReport.makeCrashReport(var11, "Ticking block entity");
                  CrashReportCategory crashreportsystemdetails1 = crashreport1.makeCategory("Block entity being ticked");
                  tileentity.addInfoToCrashReport(crashreportsystemdetails1);
                  throw new ReportedException(crashreport1);
               }
            }
         }

         if (tileentity.isInvalid()) {
            iterator.remove();
            this.loadedTileEntityList.remove(tileentity);
            if (this.isBlockLoaded(tileentity.getPos())) {
               this.getChunkFromBlockCoords(tileentity.getPos()).removeTileEntity(tileentity.getPos());
            }
         }
      }

      this.processingLoadedTiles = false;
      this.theProfiler.endStartSection("pendingBlockEntities");
      if (!this.addedTileEntityList.isEmpty()) {
         for(int i1 = 0; i1 < this.addedTileEntityList.size(); ++i1) {
            TileEntity tileentity1 = (TileEntity)this.addedTileEntityList.get(i1);
            if (!tileentity1.isInvalid() && this.isBlockLoaded(tileentity1.getPos())) {
               Chunk chunk = this.getChunkFromBlockCoords(tileentity1.getPos());
               IBlockState iblockdata = chunk.getBlockState(tileentity1.getPos());
               chunk.addTileEntity(tileentity1.getPos(), tileentity1);
               this.notifyBlockUpdate(tileentity1.getPos(), iblockdata, iblockdata, 3);
               if (!this.loadedTileEntityList.contains(tileentity1)) {
                  this.addTileEntity(tileentity1);
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

   public boolean addTileEntity(TileEntity tileentity) {
      boolean flag = this.loadedTileEntityList.add(tileentity);
      if (flag && tileentity instanceof ITickable) {
         this.tickableTileEntities.add(tileentity);
      }

      if (this.isRemote) {
         BlockPos blockposition = tileentity.getPos();
         IBlockState iblockdata = this.getBlockState(blockposition);
         this.notifyBlockUpdate(blockposition, iblockdata, iblockdata, 2);
      }

      return flag;
   }

   public void addTileEntities(Collection collection) {
      if (this.processingLoadedTiles) {
         this.addedTileEntityList.addAll(collection);
      } else {
         for(TileEntity tileentity : collection) {
            this.addTileEntity(tileentity);
         }
      }

   }

   public void updateEntity(Entity entity) {
      this.updateEntityWithOptionalForce(entity, true);
   }

   public void updateEntityWithOptionalForce(Entity entity, boolean flag) {
      int i = MathHelper.floor(entity.posX);
      int j = MathHelper.floor(entity.posZ);
      Chunk startingChunk = this.getChunkIfLoaded(i >> 4, j >> 4);
      if (!flag || startingChunk != null && startingChunk.areNeighborsLoaded(2)) {
         entity.lastTickPosX = entity.posX;
         entity.lastTickPosY = entity.posY;
         entity.lastTickPosZ = entity.posZ;
         entity.prevRotationYaw = entity.rotationYaw;
         entity.prevRotationPitch = entity.rotationPitch;
         if (flag && entity.addedToChunk) {
            ++entity.ticksExisted;
            if (entity.isRiding()) {
               entity.updateRidden();
            } else {
               entity.onUpdate();
            }
         }

         this.theProfiler.startSection("chunkCheck");
         if (Double.isNaN(entity.posX) || Double.isInfinite(entity.posX)) {
            entity.posX = entity.lastTickPosX;
         }

         if (Double.isNaN(entity.posY) || Double.isInfinite(entity.posY)) {
            entity.posY = entity.lastTickPosY;
         }

         if (Double.isNaN(entity.posZ) || Double.isInfinite(entity.posZ)) {
            entity.posZ = entity.lastTickPosZ;
         }

         if (Double.isNaN((double)entity.rotationPitch) || Double.isInfinite((double)entity.rotationPitch)) {
            entity.rotationPitch = entity.prevRotationPitch;
         }

         if (Double.isNaN((double)entity.rotationYaw) || Double.isInfinite((double)entity.rotationYaw)) {
            entity.rotationYaw = entity.prevRotationYaw;
         }

         int k = MathHelper.floor(entity.posX / 16.0D);
         int l = MathHelper.floor(entity.posY / 16.0D);
         int i1 = MathHelper.floor(entity.posZ / 16.0D);
         if (!entity.addedToChunk || entity.chunkCoordX != k || entity.chunkCoordY != l || entity.chunkCoordZ != i1) {
            if (entity.addedToChunk && this.isChunkLoaded(entity.chunkCoordX, entity.chunkCoordZ, true)) {
               this.getChunkFromChunkCoords(entity.chunkCoordX, entity.chunkCoordZ).removeEntityAtIndex(entity, entity.chunkCoordY);
            }

            if (!entity.setPositionNonDirty() && !this.isChunkLoaded(k, i1, true)) {
               entity.addedToChunk = false;
            } else {
               this.getChunkFromChunkCoords(k, i1).addEntity(entity);
            }
         }

         this.theProfiler.endSection();
         if (flag && entity.addedToChunk) {
            for(Entity entity1 : entity.getPassengers()) {
               if (!entity1.isDead && entity1.getRidingEntity() == entity) {
                  this.updateEntity(entity1);
               } else {
                  entity1.dismountRidingEntity();
               }
            }
         }
      }

   }

   public boolean checkNoEntityCollision(AxisAlignedBB axisalignedbb) {
      return this.checkNoEntityCollision(axisalignedbb, (Entity)null);
   }

   public boolean checkNoEntityCollision(AxisAlignedBB axisalignedbb, @Nullable Entity entity) {
      List list = this.getEntitiesWithinAABBExcludingEntity((Entity)null, axisalignedbb);

      for(int i = 0; i < list.size(); ++i) {
         Entity entity1 = (Entity)list.get(i);
         if (!entity1.isDead && entity1.preventEntitySpawning && entity1 != entity && (entity == null || entity1.isRidingSameEntity(entity))) {
            return false;
         }
      }

      return true;
   }

   public boolean checkBlockCollision(AxisAlignedBB axisalignedbb) {
      int i = MathHelper.floor(axisalignedbb.minX);
      int j = MathHelper.ceil(axisalignedbb.maxX);
      int k = MathHelper.floor(axisalignedbb.minY);
      int l = MathHelper.ceil(axisalignedbb.maxY);
      int i1 = MathHelper.floor(axisalignedbb.minZ);
      int j1 = MathHelper.ceil(axisalignedbb.maxZ);
      BlockPos.PooledMutableBlockPos blockposition_pooledblockposition = BlockPos.PooledMutableBlockPos.retain();

      for(int k1 = i; k1 < j; ++k1) {
         for(int l1 = k; l1 < l; ++l1) {
            for(int i2 = i1; i2 < j1; ++i2) {
               IBlockState iblockdata = this.getBlockState(blockposition_pooledblockposition.setPos(k1, l1, i2));
               if (iblockdata.getMaterial() != Material.AIR) {
                  blockposition_pooledblockposition.release();
                  return true;
               }
            }
         }
      }

      blockposition_pooledblockposition.release();
      return false;
   }

   public boolean containsAnyLiquid(AxisAlignedBB axisalignedbb) {
      int i = MathHelper.floor(axisalignedbb.minX);
      int j = MathHelper.ceil(axisalignedbb.maxX);
      int k = MathHelper.floor(axisalignedbb.minY);
      int l = MathHelper.ceil(axisalignedbb.maxY);
      int i1 = MathHelper.floor(axisalignedbb.minZ);
      int j1 = MathHelper.ceil(axisalignedbb.maxZ);
      BlockPos.PooledMutableBlockPos blockposition_pooledblockposition = BlockPos.PooledMutableBlockPos.retain();

      for(int k1 = i; k1 < j; ++k1) {
         for(int l1 = k; l1 < l; ++l1) {
            for(int i2 = i1; i2 < j1; ++i2) {
               IBlockState iblockdata = this.getBlockState(blockposition_pooledblockposition.setPos(k1, l1, i2));
               if (iblockdata.getMaterial().isLiquid()) {
                  blockposition_pooledblockposition.release();
                  return true;
               }
            }
         }
      }

      blockposition_pooledblockposition.release();
      return false;
   }

   public boolean isFlammableWithin(AxisAlignedBB axisalignedbb) {
      int i = MathHelper.floor(axisalignedbb.minX);
      int j = MathHelper.ceil(axisalignedbb.maxX);
      int k = MathHelper.floor(axisalignedbb.minY);
      int l = MathHelper.ceil(axisalignedbb.maxY);
      int i1 = MathHelper.floor(axisalignedbb.minZ);
      int j1 = MathHelper.ceil(axisalignedbb.maxZ);
      if (this.isAreaLoaded(i, k, i1, j, l, j1, true)) {
         BlockPos.PooledMutableBlockPos blockposition_pooledblockposition = BlockPos.PooledMutableBlockPos.retain();

         for(int k1 = i; k1 < j; ++k1) {
            for(int l1 = k; l1 < l; ++l1) {
               for(int i2 = i1; i2 < j1; ++i2) {
                  Block block = this.getBlockState(blockposition_pooledblockposition.setPos(k1, l1, i2)).getBlock();
                  if (block == Blocks.FIRE || block == Blocks.FLOWING_LAVA || block == Blocks.LAVA) {
                     blockposition_pooledblockposition.release();
                     return true;
                  }
               }
            }
         }

         blockposition_pooledblockposition.release();
      }

      return false;
   }

   public boolean handleMaterialAcceleration(AxisAlignedBB axisalignedbb, Material material, Entity entity) {
      int i = MathHelper.floor(axisalignedbb.minX);
      int j = MathHelper.ceil(axisalignedbb.maxX);
      int k = MathHelper.floor(axisalignedbb.minY);
      int l = MathHelper.ceil(axisalignedbb.maxY);
      int i1 = MathHelper.floor(axisalignedbb.minZ);
      int j1 = MathHelper.ceil(axisalignedbb.maxZ);
      if (!this.isAreaLoaded(i, k, i1, j, l, j1, true)) {
         return false;
      } else {
         boolean flag = false;
         Vec3d vec3d = Vec3d.ZERO;
         BlockPos.PooledMutableBlockPos blockposition_pooledblockposition = BlockPos.PooledMutableBlockPos.retain();

         for(int k1 = i; k1 < j; ++k1) {
            for(int l1 = k; l1 < l; ++l1) {
               for(int i2 = i1; i2 < j1; ++i2) {
                  blockposition_pooledblockposition.setPos(k1, l1, i2);
                  IBlockState iblockdata = this.getBlockState(blockposition_pooledblockposition);
                  Block block = iblockdata.getBlock();
                  if (iblockdata.getMaterial() == material) {
                     double d0 = (double)((float)(l1 + 1) - BlockLiquid.getLiquidHeightPercent(((Integer)iblockdata.getValue(BlockLiquid.LEVEL)).intValue()));
                     if ((double)l >= d0) {
                        flag = true;
                        vec3d = block.modifyAcceleration(this, blockposition_pooledblockposition, entity, vec3d);
                     }
                  }
               }
            }
         }

         blockposition_pooledblockposition.release();
         if (vec3d.lengthVector() > 0.0D && entity.isPushedByWater()) {
            vec3d = vec3d.normalize();
            entity.motionX += vec3d.xCoord * 0.014D;
            entity.motionY += vec3d.yCoord * 0.014D;
            entity.motionZ += vec3d.zCoord * 0.014D;
         }

         return flag;
      }
   }

   public boolean isMaterialInBB(AxisAlignedBB axisalignedbb, Material material) {
      int i = MathHelper.floor(axisalignedbb.minX);
      int j = MathHelper.ceil(axisalignedbb.maxX);
      int k = MathHelper.floor(axisalignedbb.minY);
      int l = MathHelper.ceil(axisalignedbb.maxY);
      int i1 = MathHelper.floor(axisalignedbb.minZ);
      int j1 = MathHelper.ceil(axisalignedbb.maxZ);
      BlockPos.PooledMutableBlockPos blockposition_pooledblockposition = BlockPos.PooledMutableBlockPos.retain();

      for(int k1 = i; k1 < j; ++k1) {
         for(int l1 = k; l1 < l; ++l1) {
            for(int i2 = i1; i2 < j1; ++i2) {
               if (this.getBlockState(blockposition_pooledblockposition.setPos(k1, l1, i2)).getMaterial() == material) {
                  blockposition_pooledblockposition.release();
                  return true;
               }
            }
         }
      }

      blockposition_pooledblockposition.release();
      return false;
   }

   public boolean isAABBInMaterial(AxisAlignedBB axisalignedbb, Material material) {
      int i = MathHelper.floor(axisalignedbb.minX);
      int j = MathHelper.ceil(axisalignedbb.maxX);
      int k = MathHelper.floor(axisalignedbb.minY);
      int l = MathHelper.ceil(axisalignedbb.maxY);
      int i1 = MathHelper.floor(axisalignedbb.minZ);
      int j1 = MathHelper.ceil(axisalignedbb.maxZ);
      BlockPos.PooledMutableBlockPos blockposition_pooledblockposition = BlockPos.PooledMutableBlockPos.retain();

      for(int k1 = i; k1 < j; ++k1) {
         for(int l1 = k; l1 < l; ++l1) {
            for(int i2 = i1; i2 < j1; ++i2) {
               IBlockState iblockdata = this.getBlockState(blockposition_pooledblockposition.setPos(k1, l1, i2));
               if (iblockdata.getMaterial() == material) {
                  int j2 = ((Integer)iblockdata.getValue(BlockLiquid.LEVEL)).intValue();
                  double d0 = (double)(l1 + 1);
                  if (j2 < 8) {
                     d0 = (double)(l1 + 1) - (double)j2 / 8.0D;
                  }

                  if (d0 >= axisalignedbb.minY) {
                     blockposition_pooledblockposition.release();
                     return true;
                  }
               }
            }
         }
      }

      blockposition_pooledblockposition.release();
      return false;
   }

   public Explosion createExplosion(@Nullable Entity entity, double d0, double d1, double d2, float f, boolean flag) {
      return this.newExplosion(entity, d0, d1, d2, f, false, flag);
   }

   public Explosion newExplosion(@Nullable Entity entity, double d0, double d1, double d2, float f, boolean flag, boolean flag1) {
      Explosion explosion = new Explosion(this, entity, d0, d1, d2, f, flag, flag1);
      explosion.doExplosionA();
      explosion.doExplosionB(true);
      return explosion;
   }

   public float getBlockDensity(Vec3d vec3d, AxisAlignedBB axisalignedbb) {
      double d0 = 1.0D / ((axisalignedbb.maxX - axisalignedbb.minX) * 2.0D + 1.0D);
      double d1 = 1.0D / ((axisalignedbb.maxY - axisalignedbb.minY) * 2.0D + 1.0D);
      double d2 = 1.0D / ((axisalignedbb.maxZ - axisalignedbb.minZ) * 2.0D + 1.0D);
      double d3 = (1.0D - Math.floor(1.0D / d0) * d0) / 2.0D;
      double d4 = (1.0D - Math.floor(1.0D / d2) * d2) / 2.0D;
      if (d0 >= 0.0D && d1 >= 0.0D && d2 >= 0.0D) {
         int i = 0;
         int j = 0;

         for(float f = 0.0F; f <= 1.0F; f = (float)((double)f + d0)) {
            for(float f1 = 0.0F; f1 <= 1.0F; f1 = (float)((double)f1 + d1)) {
               for(float f2 = 0.0F; f2 <= 1.0F; f2 = (float)((double)f2 + d2)) {
                  double d5 = axisalignedbb.minX + (axisalignedbb.maxX - axisalignedbb.minX) * (double)f;
                  double d6 = axisalignedbb.minY + (axisalignedbb.maxY - axisalignedbb.minY) * (double)f1;
                  double d7 = axisalignedbb.minZ + (axisalignedbb.maxZ - axisalignedbb.minZ) * (double)f2;
                  if (this.rayTraceBlocks(new Vec3d(d5 + d3, d6, d7 + d4), vec3d) == null) {
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

   public boolean extinguishFire(@Nullable EntityPlayer entityhuman, BlockPos blockposition, EnumFacing enumdirection) {
      blockposition = blockposition.offset(enumdirection);
      if (this.getBlockState(blockposition).getBlock() == Blocks.FIRE) {
         this.playEvent(entityhuman, 1009, blockposition, 0);
         this.setBlockToAir(blockposition);
         return true;
      } else {
         return false;
      }
   }

   @Nullable
   public TileEntity getTileEntity(BlockPos blockposition) {
      if (this.isOutsideBuildHeight(blockposition)) {
         return null;
      } else if (this.capturedTileEntities.containsKey(blockposition)) {
         return (TileEntity)this.capturedTileEntities.get(blockposition);
      } else {
         TileEntity tileentity = null;
         if (this.processingLoadedTiles) {
            tileentity = this.getPendingTileEntityAt(blockposition);
         }

         if (tileentity == null) {
            tileentity = this.getChunkFromBlockCoords(blockposition).getTileEntity(blockposition, Chunk.EnumCreateEntityType.IMMEDIATE);
         }

         if (tileentity == null) {
            tileentity = this.getPendingTileEntityAt(blockposition);
         }

         return tileentity;
      }
   }

   @Nullable
   private TileEntity getPendingTileEntityAt(BlockPos blockposition) {
      for(int i = 0; i < this.addedTileEntityList.size(); ++i) {
         TileEntity tileentity = (TileEntity)this.addedTileEntityList.get(i);
         if (!tileentity.isInvalid() && tileentity.getPos().equals(blockposition)) {
            return tileentity;
         }
      }

      return null;
   }

   public void setTileEntity(BlockPos blockposition, @Nullable TileEntity tileentity) {
      if (!this.isOutsideBuildHeight(blockposition) && tileentity != null && !tileentity.isInvalid()) {
         if (this.captureBlockStates) {
            tileentity.setWorld(this);
            tileentity.setPos(blockposition);
            this.capturedTileEntities.put(blockposition, tileentity);
            return;
         }

         if (this.processingLoadedTiles) {
            tileentity.setPos(blockposition);
            Iterator iterator = this.addedTileEntityList.iterator();

            while(iterator.hasNext()) {
               TileEntity tileentity1 = (TileEntity)iterator.next();
               if (tileentity1.getPos().equals(blockposition)) {
                  tileentity1.invalidate();
                  iterator.remove();
               }
            }

            this.addedTileEntityList.add(tileentity);
         } else {
            this.addTileEntity(tileentity);
            this.getChunkFromBlockCoords(blockposition).addTileEntity(blockposition, tileentity);
         }
      }

   }

   public void removeTileEntity(BlockPos blockposition) {
      TileEntity tileentity = this.getTileEntity(blockposition);
      if (tileentity != null && this.processingLoadedTiles) {
         tileentity.invalidate();
         this.addedTileEntityList.remove(tileentity);
      } else {
         if (tileentity != null) {
            this.addedTileEntityList.remove(tileentity);
            this.loadedTileEntityList.remove(tileentity);
            this.tickableTileEntities.remove(tileentity);
         }

         this.getChunkFromBlockCoords(blockposition).removeTileEntity(blockposition);
      }

   }

   public void markTileEntityForRemoval(TileEntity tileentity) {
      this.tileEntitiesToBeRemoved.add(tileentity);
   }

   public boolean isBlockFullCube(BlockPos blockposition) {
      AxisAlignedBB axisalignedbb = this.getBlockState(blockposition).getCollisionBoundingBox(this, blockposition);
      return axisalignedbb != Block.NULL_AABB && axisalignedbb.getAverageEdgeLength() >= 1.0D;
   }

   public boolean isBlockNormalCube(BlockPos blockposition, boolean flag) {
      if (this.isOutsideBuildHeight(blockposition)) {
         return false;
      } else {
         Chunk chunk = this.chunkProvider.getLoadedChunk(blockposition.getX() >> 4, blockposition.getZ() >> 4);
         if (chunk != null && !chunk.isEmpty()) {
            IBlockState iblockdata = this.getBlockState(blockposition);
            return iblockdata.getMaterial().isOpaque() && iblockdata.isFullCube();
         } else {
            return flag;
         }
      }
   }

   public void calculateInitialSkylight() {
      int i = this.calculateSkylightSubtracted(1.0F);
      if (i != this.skylightSubtracted) {
         this.skylightSubtracted = i;
      }

   }

   public void setAllowedSpawnTypes(boolean flag, boolean flag1) {
      this.spawnHostileMobs = flag;
      this.spawnPeacefulMobs = flag1;
   }

   public void tick() {
      this.updateWeather();
   }

   protected void calculateInitialWeather() {
      if (this.worldInfo.isRaining()) {
         this.rainingStrength = 1.0F;
         if (this.worldInfo.isThundering()) {
            this.thunderingStrength = 1.0F;
         }
      }

   }

   protected void updateWeather() {
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

         for(int idx = 0; idx < this.playerEntities.size(); ++idx) {
            if (((EntityPlayerMP)this.playerEntities.get(idx)).world == this) {
               ((EntityPlayerMP)this.playerEntities.get(idx)).tickWeather();
            }
         }
      }

   }

   protected void updateBlocks() {
   }

   public void immediateBlockTick(BlockPos blockposition, IBlockState iblockdata, Random random) {
      this.scheduledUpdatesAreImmediate = true;
      iblockdata.getBlock().updateTick(this, blockposition, iblockdata, random);
      this.scheduledUpdatesAreImmediate = false;
   }

   public boolean canBlockFreezeWater(BlockPos blockposition) {
      return this.canBlockFreeze(blockposition, false);
   }

   public boolean canBlockFreezeNoWater(BlockPos blockposition) {
      return this.canBlockFreeze(blockposition, true);
   }

   public boolean canBlockFreeze(BlockPos blockposition, boolean flag) {
      Biome biomebase = this.getBiome(blockposition);
      float f = biomebase.getFloatTemperature(blockposition);
      if (f > 0.15F) {
         return false;
      } else {
         if (blockposition.getY() >= 0 && blockposition.getY() < 256 && this.getLightFor(EnumSkyBlock.BLOCK, blockposition) < 10) {
            IBlockState iblockdata = this.getBlockState(blockposition);
            Block block = iblockdata.getBlock();
            if ((block == Blocks.WATER || block == Blocks.FLOWING_WATER) && ((Integer)iblockdata.getValue(BlockLiquid.LEVEL)).intValue() == 0) {
               if (!flag) {
                  return true;
               }

               boolean flag1 = this.isWater(blockposition.west()) && this.isWater(blockposition.east()) && this.isWater(blockposition.north()) && this.isWater(blockposition.south());
               if (!flag1) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private boolean isWater(BlockPos blockposition) {
      return this.getBlockState(blockposition).getMaterial() == Material.WATER;
   }

   public boolean canSnowAt(BlockPos blockposition, boolean flag) {
      Biome biomebase = this.getBiome(blockposition);
      float f = biomebase.getFloatTemperature(blockposition);
      if (f > 0.15F) {
         return false;
      } else if (!flag) {
         return true;
      } else {
         if (blockposition.getY() >= 0 && blockposition.getY() < 256 && this.getLightFor(EnumSkyBlock.BLOCK, blockposition) < 10) {
            IBlockState iblockdata = this.getBlockState(blockposition);
            if (iblockdata.getMaterial() == Material.AIR && Blocks.SNOW_LAYER.canPlaceBlockAt(this, blockposition)) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean checkLight(BlockPos blockposition) {
      boolean flag = false;
      if (!this.provider.hasNoSky()) {
         flag |= this.checkLightFor(EnumSkyBlock.SKY, blockposition);
      }

      flag = flag | this.checkLightFor(EnumSkyBlock.BLOCK, blockposition);
      return flag;
   }

   private int getRawLight(BlockPos blockposition, EnumSkyBlock enumskyblock) {
      if (enumskyblock == EnumSkyBlock.SKY && this.canSeeSky(blockposition)) {
         return 15;
      } else {
         IBlockState iblockdata = this.getBlockState(blockposition);
         int i = enumskyblock == EnumSkyBlock.SKY ? 0 : iblockdata.getLightValue();
         int j = iblockdata.getLightOpacity();
         if (j >= 15 && iblockdata.getLightValue() > 0) {
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
            BlockPos.PooledMutableBlockPos blockposition_pooledblockposition = BlockPos.PooledMutableBlockPos.retain();

            for(EnumFacing enumdirection : EnumFacing.values()) {
               blockposition_pooledblockposition.setPos(blockposition).move(enumdirection);
               int i1 = this.getLightFor(enumskyblock, blockposition_pooledblockposition) - j;
               if (i1 > i) {
                  i = i1;
               }

               if (i >= 14) {
                  return i;
               }
            }

            blockposition_pooledblockposition.release();
            return i;
         }
      }
   }

   public boolean checkLightFor(EnumSkyBlock enumskyblock, BlockPos blockposition) {
      Chunk chunk = this.getChunkIfLoaded(blockposition.getX() >> 4, blockposition.getZ() >> 4);
      if (chunk != null && chunk.areNeighborsLoaded(1)) {
         int i = 0;
         int j = 0;
         this.theProfiler.startSection("getBrightness");
         int k = this.getLightFor(enumskyblock, blockposition);
         int l = this.getRawLight(blockposition, enumskyblock);
         int i1 = blockposition.getX();
         int j1 = blockposition.getY();
         int k1 = blockposition.getZ();
         if (l > k) {
            this.lightUpdateBlockList[j++] = 133152;
         } else if (l < k) {
            this.lightUpdateBlockList[j++] = 133152 | k << 18;

            while(i < j) {
               int l1 = this.lightUpdateBlockList[i++];
               int i2 = (l1 & 63) - 32 + i1;
               int j2 = (l1 >> 6 & 63) - 32 + j1;
               int k2 = (l1 >> 12 & 63) - 32 + k1;
               int l3 = l1 >> 18 & 15;
               BlockPos blockposition1 = new BlockPos(i2, j2, k2);
               int l2 = this.getLightFor(enumskyblock, blockposition1);
               if (l2 == l3) {
                  this.setLightFor(enumskyblock, blockposition1, 0);
                  if (l3 > 0) {
                     int i3 = MathHelper.abs(i2 - i1);
                     int j3 = MathHelper.abs(j2 - j1);
                     int k3 = MathHelper.abs(k2 - k1);
                     if (i3 + j3 + k3 < 17) {
                        BlockPos.PooledMutableBlockPos blockposition_pooledblockposition = BlockPos.PooledMutableBlockPos.retain();

                        for(EnumFacing enumdirection : EnumFacing.values()) {
                           int k4 = i2 + enumdirection.getFrontOffsetX();
                           int l4 = j2 + enumdirection.getFrontOffsetY();
                           int i5 = k2 + enumdirection.getFrontOffsetZ();
                           blockposition_pooledblockposition.setPos(k4, l4, i5);
                           int j5 = Math.max(1, this.getBlockState(blockposition_pooledblockposition).getLightOpacity());
                           l2 = this.getLightFor(enumskyblock, blockposition_pooledblockposition);
                           if (l2 == l3 - j5 && j < this.lightUpdateBlockList.length) {
                              this.lightUpdateBlockList[j++] = k4 - i1 + 32 | l4 - j1 + 32 << 6 | i5 - k1 + 32 << 12 | l3 - j5 << 18;
                           }
                        }

                        blockposition_pooledblockposition.release();
                     }
                  }
               }
            }

            i = 0;
         }

         this.theProfiler.endSection();
         this.theProfiler.startSection("checkedPosition < toCheckCount");

         while(i < j) {
            int l1 = this.lightUpdateBlockList[i++];
            int i2 = (l1 & 63) - 32 + i1;
            int j2 = (l1 >> 6 & 63) - 32 + j1;
            int k2 = (l1 >> 12 & 63) - 32 + k1;
            BlockPos blockposition2 = new BlockPos(i2, j2, k2);
            int k5 = this.getLightFor(enumskyblock, blockposition2);
            int l2 = this.getRawLight(blockposition2, enumskyblock);
            if (l2 != k5) {
               this.setLightFor(enumskyblock, blockposition2, l2);
               if (l2 > k5) {
                  int i3 = Math.abs(i2 - i1);
                  int j3 = Math.abs(j2 - j1);
                  int k3 = Math.abs(k2 - k1);
                  boolean flag = j < this.lightUpdateBlockList.length - 6;
                  if (i3 + j3 + k3 < 17 && flag) {
                     if (this.getLightFor(enumskyblock, blockposition2.west()) < l2) {
                        this.lightUpdateBlockList[j++] = i2 - 1 - i1 + 32 + (j2 - j1 + 32 << 6) + (k2 - k1 + 32 << 12);
                     }

                     if (this.getLightFor(enumskyblock, blockposition2.east()) < l2) {
                        this.lightUpdateBlockList[j++] = i2 + 1 - i1 + 32 + (j2 - j1 + 32 << 6) + (k2 - k1 + 32 << 12);
                     }

                     if (this.getLightFor(enumskyblock, blockposition2.down()) < l2) {
                        this.lightUpdateBlockList[j++] = i2 - i1 + 32 + (j2 - 1 - j1 + 32 << 6) + (k2 - k1 + 32 << 12);
                     }

                     if (this.getLightFor(enumskyblock, blockposition2.up()) < l2) {
                        this.lightUpdateBlockList[j++] = i2 - i1 + 32 + (j2 + 1 - j1 + 32 << 6) + (k2 - k1 + 32 << 12);
                     }

                     if (this.getLightFor(enumskyblock, blockposition2.north()) < l2) {
                        this.lightUpdateBlockList[j++] = i2 - i1 + 32 + (j2 - j1 + 32 << 6) + (k2 - 1 - k1 + 32 << 12);
                     }

                     if (this.getLightFor(enumskyblock, blockposition2.south()) < l2) {
                        this.lightUpdateBlockList[j++] = i2 - i1 + 32 + (j2 - j1 + 32 << 6) + (k2 + 1 - k1 + 32 << 12);
                     }
                  }
               }
            }
         }

         this.theProfiler.endSection();
         return true;
      } else {
         return false;
      }
   }

   public boolean tickUpdates(boolean flag) {
      return false;
   }

   @Nullable
   public List getPendingBlockUpdates(Chunk chunk, boolean flag) {
      return null;
   }

   @Nullable
   public List getPendingBlockUpdates(StructureBoundingBox structureboundingbox, boolean flag) {
      return null;
   }

   public List getEntitiesWithinAABBExcludingEntity(@Nullable Entity entity, AxisAlignedBB axisalignedbb) {
      return this.getEntitiesInAABBexcluding(entity, axisalignedbb, EntitySelectors.NOT_SPECTATING);
   }

   public List getEntitiesInAABBexcluding(@Nullable Entity entity, AxisAlignedBB axisalignedbb, @Nullable Predicate predicate) {
      ArrayList arraylist = Lists.newArrayList();
      int i = MathHelper.floor((axisalignedbb.minX - 2.0D) / 16.0D);
      int j = MathHelper.floor((axisalignedbb.maxX + 2.0D) / 16.0D);
      int k = MathHelper.floor((axisalignedbb.minZ - 2.0D) / 16.0D);
      int l = MathHelper.floor((axisalignedbb.maxZ + 2.0D) / 16.0D);

      for(int i1 = i; i1 <= j; ++i1) {
         for(int j1 = k; j1 <= l; ++j1) {
            if (this.isChunkLoaded(i1, j1, true)) {
               this.getChunkFromChunkCoords(i1, j1).getEntitiesWithinAABBForEntity(entity, axisalignedbb, arraylist, predicate);
            }
         }
      }

      return arraylist;
   }

   public List getEntities(Class oclass, Predicate predicate) {
      ArrayList arraylist = Lists.newArrayList();

      for(Entity entity : this.loadedEntityList) {
         if (oclass.isAssignableFrom(entity.getClass()) && predicate.apply(entity)) {
            arraylist.add(entity);
         }
      }

      return arraylist;
   }

   public List getPlayers(Class oclass, Predicate predicate) {
      ArrayList arraylist = Lists.newArrayList();

      for(Entity entity : this.playerEntities) {
         if (oclass.isAssignableFrom(entity.getClass()) && predicate.apply(entity)) {
            arraylist.add(entity);
         }
      }

      return arraylist;
   }

   public List getEntitiesWithinAABB(Class oclass, AxisAlignedBB axisalignedbb) {
      return this.getEntitiesWithinAABB(oclass, axisalignedbb, EntitySelectors.NOT_SPECTATING);
   }

   public List getEntitiesWithinAABB(Class oclass, AxisAlignedBB axisalignedbb, @Nullable Predicate predicate) {
      int i = MathHelper.floor((axisalignedbb.minX - 2.0D) / 16.0D);
      int j = MathHelper.ceil((axisalignedbb.maxX + 2.0D) / 16.0D);
      int k = MathHelper.floor((axisalignedbb.minZ - 2.0D) / 16.0D);
      int l = MathHelper.ceil((axisalignedbb.maxZ + 2.0D) / 16.0D);
      ArrayList arraylist = Lists.newArrayList();

      for(int i1 = i; i1 < j; ++i1) {
         for(int j1 = k; j1 < l; ++j1) {
            if (this.isChunkLoaded(i1, j1, true)) {
               this.getChunkFromChunkCoords(i1, j1).getEntitiesOfTypeWithinAAAB(oclass, axisalignedbb, arraylist, predicate);
            }
         }
      }

      return arraylist;
   }

   @Nullable
   public Entity findNearestEntityWithinAABB(Class oclass, AxisAlignedBB axisalignedbb, Entity t0) {
      List list = this.getEntitiesWithinAABB(oclass, axisalignedbb);
      Entity entity = null;
      double d0 = Double.MAX_VALUE;

      for(int i = 0; i < list.size(); ++i) {
         Entity entity1 = (Entity)list.get(i);
         if (entity1 != t0 && EntitySelectors.NOT_SPECTATING.apply(entity1)) {
            double d1 = t0.getDistanceSqToEntity(entity1);
            if (d1 <= d0) {
               entity = entity1;
               d0 = d1;
            }
         }
      }

      return entity;
   }

   @Nullable
   public Entity getEntityByID(int i) {
      return (Entity)this.entitiesById.lookup(i);
   }

   public void markChunkDirty(BlockPos blockposition, TileEntity tileentity) {
      if (this.isBlockLoaded(blockposition)) {
         this.getChunkFromBlockCoords(blockposition).setChunkModified();
      }

   }

   public int countEntities(Class oclass) {
      int i = 0;
      Iterator iterator = this.loadedEntityList.iterator();

      while(true) {
         Entity entity;
         while(true) {
            if (!iterator.hasNext()) {
               return i;
            }

            entity = (Entity)iterator.next();
            if (!(entity instanceof EntityLiving)) {
               break;
            }

            EntityLiving entityinsentient = (EntityLiving)entity;
            if (!entityinsentient.canDespawn() || !entityinsentient.isNoDespawnRequired()) {
               break;
            }
         }

         if (oclass.isAssignableFrom(entity.getClass())) {
            ++i;
         }
      }
   }

   public void loadEntities(Collection collection) {
      for(Entity entity : collection) {
         if (entity != null) {
            this.loadedEntityList.add(entity);
            this.onEntityAdded(entity);
         }
      }

   }

   public void unloadEntities(Collection collection) {
      this.unloadedEntityList.addAll(collection);
   }

   public boolean canBlockBePlaced(Block block, BlockPos blockposition, boolean flag, EnumFacing enumdirection, @Nullable Entity entity, @Nullable ItemStack itemstack) {
      IBlockState iblockdata = this.getBlockState(blockposition);
      AxisAlignedBB axisalignedbb = flag ? null : block.getDefaultState().getCollisionBoundingBox(this, blockposition);
      boolean defaultReturn = axisalignedbb != Block.NULL_AABB && !this.checkNoEntityCollision(axisalignedbb.offset(blockposition), entity) ? false : (iblockdata.getMaterial() == Material.CIRCUITS && block == Blocks.ANVIL ? true : iblockdata.getMaterial().isReplaceable() && block.canReplace(this, blockposition, enumdirection, itemstack));
      BlockCanBuildEvent event = new BlockCanBuildEvent(this.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()), CraftMagicNumbers.getId(block), defaultReturn);
      this.getServer().getPluginManager().callEvent(event);
      return event.isBuildable();
   }

   public int getSeaLevel() {
      return this.seaLevel;
   }

   public void setSeaLevel(int i) {
      this.seaLevel = i;
   }

   public int getStrongPower(BlockPos blockposition, EnumFacing enumdirection) {
      return this.getBlockState(blockposition).getStrongPower(this, blockposition, enumdirection);
   }

   public WorldType getWorldType() {
      return this.worldInfo.getTerrainType();
   }

   public int getStrongPower(BlockPos blockposition) {
      byte b0 = 0;
      int i = Math.max(b0, this.getStrongPower(blockposition.down(), EnumFacing.DOWN));
      if (i >= 15) {
         return i;
      } else {
         i = Math.max(i, this.getStrongPower(blockposition.up(), EnumFacing.UP));
         if (i >= 15) {
            return i;
         } else {
            i = Math.max(i, this.getStrongPower(blockposition.north(), EnumFacing.NORTH));
            if (i >= 15) {
               return i;
            } else {
               i = Math.max(i, this.getStrongPower(blockposition.south(), EnumFacing.SOUTH));
               if (i >= 15) {
                  return i;
               } else {
                  i = Math.max(i, this.getStrongPower(blockposition.west(), EnumFacing.WEST));
                  if (i >= 15) {
                     return i;
                  } else {
                     i = Math.max(i, this.getStrongPower(blockposition.east(), EnumFacing.EAST));
                     return i >= 15 ? i : i;
                  }
               }
            }
         }
      }
   }

   public boolean isSidePowered(BlockPos blockposition, EnumFacing enumdirection) {
      return this.getRedstonePower(blockposition, enumdirection) > 0;
   }

   public int getRedstonePower(BlockPos blockposition, EnumFacing enumdirection) {
      IBlockState iblockdata = this.getBlockState(blockposition);
      return iblockdata.isNormalCube() ? this.getStrongPower(blockposition) : iblockdata.getWeakPower(this, blockposition, enumdirection);
   }

   public boolean isBlockPowered(BlockPos blockposition) {
      return this.getRedstonePower(blockposition.down(), EnumFacing.DOWN) > 0 ? true : (this.getRedstonePower(blockposition.up(), EnumFacing.UP) > 0 ? true : (this.getRedstonePower(blockposition.north(), EnumFacing.NORTH) > 0 ? true : (this.getRedstonePower(blockposition.south(), EnumFacing.SOUTH) > 0 ? true : (this.getRedstonePower(blockposition.west(), EnumFacing.WEST) > 0 ? true : this.getRedstonePower(blockposition.east(), EnumFacing.EAST) > 0))));
   }

   public int isBlockIndirectlyGettingPowered(BlockPos blockposition) {
      int i = 0;

      for(EnumFacing enumdirection : EnumFacing.values()) {
         int l = this.getRedstonePower(blockposition.offset(enumdirection), enumdirection);
         if (l >= 15) {
            return 15;
         }

         if (l > i) {
            i = l;
         }
      }

      return i;
   }

   @Nullable
   public EntityPlayer getClosestPlayerToEntity(Entity entity, double d0) {
      return this.getClosestPlayer(entity.posX, entity.posY, entity.posZ, d0, false);
   }

   @Nullable
   public EntityPlayer getNearestPlayerNotCreative(Entity entity, double d0) {
      return this.getClosestPlayer(entity.posX, entity.posY, entity.posZ, d0, true);
   }

   @Nullable
   public EntityPlayer getClosestPlayer(double d0, double d1, double d2, double d3, boolean flag) {
      double d4 = -1.0D;
      EntityPlayer entityhuman = null;

      for(int i = 0; i < this.playerEntities.size(); ++i) {
         EntityPlayer entityhuman1 = (EntityPlayer)this.playerEntities.get(i);
         if (entityhuman1 != null && !entityhuman1.isDead && (EntitySelectors.CAN_AI_TARGET.apply(entityhuman1) || !flag) && (EntitySelectors.NOT_SPECTATING.apply(entityhuman1) || flag)) {
            double d5 = entityhuman1.getDistanceSq(d0, d1, d2);
            if ((d3 < 0.0D || d5 < d3 * d3) && (d4 == -1.0D || d5 < d4)) {
               d4 = d5;
               entityhuman = entityhuman1;
            }
         }
      }

      return entityhuman;
   }

   public boolean isAnyPlayerWithinRangeAt(double d0, double d1, double d2, double d3) {
      for(int i = 0; i < this.playerEntities.size(); ++i) {
         EntityPlayer entityhuman = (EntityPlayer)this.playerEntities.get(i);
         if (EntitySelectors.NOT_SPECTATING.apply(entityhuman)) {
            double d4 = entityhuman.getDistanceSq(d0, d1, d2);
            if (d3 < 0.0D || d4 < d3 * d3) {
               return true;
            }
         }
      }

      return false;
   }

   @Nullable
   public EntityPlayer getNearestAttackablePlayer(Entity entity, double d0, double d1) {
      return this.getNearestAttackablePlayer(entity.posX, entity.posY, entity.posZ, d0, d1, (Function)null, (Predicate)null);
   }

   @Nullable
   public EntityPlayer getNearestAttackablePlayer(BlockPos blockposition, double d0, double d1) {
      return this.getNearestAttackablePlayer((double)((float)blockposition.getX() + 0.5F), (double)((float)blockposition.getY() + 0.5F), (double)((float)blockposition.getZ() + 0.5F), d0, d1, (Function)null, (Predicate)null);
   }

   @Nullable
   public EntityPlayer getNearestAttackablePlayer(double d0, double d1, double d2, double d3, double d4, @Nullable Function function, @Nullable Predicate predicate) {
      double d5 = -1.0D;
      EntityPlayer entityhuman = null;

      for(int i = 0; i < this.playerEntities.size(); ++i) {
         EntityPlayer entityhuman1 = (EntityPlayer)this.playerEntities.get(i);
         if (!entityhuman1.capabilities.disableDamage && entityhuman1.isEntityAlive() && !entityhuman1.isSpectator() && (predicate == null || predicate.apply(entityhuman1))) {
            double d6 = entityhuman1.getDistanceSq(d0, entityhuman1.posY, d2);
            double d7 = d3;
            if (entityhuman1.isSneaking()) {
               d7 = d3 * 0.800000011920929D;
            }

            if (entityhuman1.isInvisible()) {
               float f = entityhuman1.getArmorVisibility();
               if (f < 0.1F) {
                  f = 0.1F;
               }

               d7 *= (double)(0.7F * f);
            }

            if (function != null) {
               d7 *= ((Double)Objects.firstNonNull((Double)function.apply(entityhuman1), Double.valueOf(1.0D))).doubleValue();
            }

            if ((d4 < 0.0D || Math.abs(entityhuman1.posY - d1) < d4 * d4) && (d3 < 0.0D || d6 < d7 * d7) && (d5 == -1.0D || d6 < d5)) {
               d5 = d6;
               entityhuman = entityhuman1;
            }
         }
      }

      return entityhuman;
   }

   @Nullable
   public EntityPlayer getPlayerEntityByName(String s) {
      for(int i = 0; i < this.playerEntities.size(); ++i) {
         EntityPlayer entityhuman = (EntityPlayer)this.playerEntities.get(i);
         if (s.equals(entityhuman.getName())) {
            return entityhuman;
         }
      }

      return null;
   }

   @Nullable
   public EntityPlayer getPlayerEntityByUUID(UUID uuid) {
      for(int i = 0; i < this.playerEntities.size(); ++i) {
         EntityPlayer entityhuman = (EntityPlayer)this.playerEntities.get(i);
         if (uuid.equals(entityhuman.getUniqueID())) {
            return entityhuman;
         }
      }

      return null;
   }

   public void checkSessionLock() throws MinecraftException {
      this.saveHandler.checkSessionLock();
   }

   public long getSeed() {
      return this.worldInfo.getSeed();
   }

   public long getTotalWorldTime() {
      return this.worldInfo.getWorldTotalTime();
   }

   public long getWorldTime() {
      return this.worldInfo.getWorldTime();
   }

   public void setWorldTime(long i) {
      this.worldInfo.setWorldTime(i);
   }

   public BlockPos getSpawnPoint() {
      BlockPos blockposition = new BlockPos(this.worldInfo.getSpawnX(), this.worldInfo.getSpawnY(), this.worldInfo.getSpawnZ());
      if (!this.getWorldBorder().contains(blockposition)) {
         blockposition = this.getHeight(new BlockPos(this.getWorldBorder().getCenterX(), 0.0D, this.getWorldBorder().getCenterZ()));
      }

      return blockposition;
   }

   public void setSpawnPoint(BlockPos blockposition) {
      this.worldInfo.setSpawn(blockposition);
   }

   public boolean isBlockModifiable(EntityPlayer entityhuman, BlockPos blockposition) {
      return true;
   }

   public void setEntityState(Entity entity, byte b0) {
   }

   public IChunkProvider getChunkProvider() {
      return this.chunkProvider;
   }

   public void addBlockEvent(BlockPos blockposition, Block block, int i, int j) {
      this.getBlockState(blockposition).onBlockEventReceived(this, blockposition, i, j);
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

   public void checkSleepStatus() {
      if (!this.isRemote) {
         this.updateAllPlayersSleepingFlag();
      }

   }

   public float getThunderStrength(float f) {
      return (this.prevThunderingStrength + (this.thunderingStrength - this.prevThunderingStrength) * f) * this.getRainStrength(f);
   }

   public float getRainStrength(float f) {
      return this.prevRainingStrength + (this.rainingStrength - this.prevRainingStrength) * f;
   }

   public boolean isThundering() {
      return (double)this.getThunderStrength(1.0F) > 0.9D;
   }

   public boolean isRaining() {
      return (double)this.getRainStrength(1.0F) > 0.2D;
   }

   public boolean isRainingAt(BlockPos blockposition) {
      if (!this.isRaining()) {
         return false;
      } else if (!this.canSeeSky(blockposition)) {
         return false;
      } else if (this.getPrecipitationHeight(blockposition).getY() > blockposition.getY()) {
         return false;
      } else {
         Biome biomebase = this.getBiome(blockposition);
         return biomebase.getEnableSnow() ? false : (this.canSnowAt(blockposition, false) ? false : biomebase.canRain());
      }
   }

   public boolean isBlockinHighHumidity(BlockPos blockposition) {
      Biome biomebase = this.getBiome(blockposition);
      return biomebase.isHighHumidity();
   }

   @Nullable
   public MapStorage getMapStorage() {
      return this.mapStorage;
   }

   public void setData(String s, WorldSavedData persistentbase) {
      this.mapStorage.setData(s, persistentbase);
   }

   @Nullable
   public WorldSavedData loadData(Class oclass, String s) {
      return this.mapStorage.getOrLoadData(oclass, s);
   }

   public int getUniqueDataId(String s) {
      return this.mapStorage.getUniqueDataId(s);
   }

   public void playBroadcastSound(int i, BlockPos blockposition, int j) {
      for(int k = 0; k < this.eventListeners.size(); ++k) {
         ((IWorldEventListener)this.eventListeners.get(k)).broadcastSound(i, blockposition, j);
      }

   }

   public void playEvent(int i, BlockPos blockposition, int j) {
      this.playEvent((EntityPlayer)null, i, blockposition, j);
   }

   public void playEvent(@Nullable EntityPlayer entityhuman, int i, BlockPos blockposition, int j) {
      try {
         for(int k = 0; k < this.eventListeners.size(); ++k) {
            ((IWorldEventListener)this.eventListeners.get(k)).playEvent(entityhuman, i, blockposition, j);
         }

      } catch (Throwable var8) {
         CrashReport crashreport = CrashReport.makeCrashReport(var8, "Playing level event");
         CrashReportCategory crashreportsystemdetails = crashreport.makeCategory("Level event being played");
         crashreportsystemdetails.addCrashSection("Block coordinates", CrashReportCategory.getCoordinateInfo(blockposition));
         crashreportsystemdetails.addCrashSection("Event source", entityhuman);
         crashreportsystemdetails.addCrashSection("Event type", Integer.valueOf(i));
         crashreportsystemdetails.addCrashSection("Event data", Integer.valueOf(j));
         throw new ReportedException(crashreport);
      }
   }

   public int getHeight() {
      return 256;
   }

   public int getActualHeight() {
      return this.provider.hasNoSky() ? 128 : 256;
   }

   public Random setRandomSeed(int i, int j, int k) {
      long l = (long)i * 341873128712L + (long)j * 132897987541L + this.getWorldInfo().getSeed() + (long)k;
      this.rand.setSeed(l);
      return this.rand;
   }

   public CrashReportCategory addWorldInfoToCrashReport(CrashReport crashreport) {
      CrashReportCategory crashreportsystemdetails = crashreport.makeCategoryDepth("Affected level", 1);
      crashreportsystemdetails.addCrashSection("Level name", this.worldInfo == null ? "????" : this.worldInfo.getWorldName());
      crashreportsystemdetails.setDetail("All players", new ICrashReportDetail() {
         public String a() {
            return World.this.playerEntities.size() + " total; " + World.this.playerEntities;
         }

         public Object call() throws Exception {
            return this.a();
         }
      });
      crashreportsystemdetails.setDetail("Chunk stats", new ICrashReportDetail() {
         public String a() {
            return World.this.chunkProvider.makeString();
         }

         public Object call() throws Exception {
            return this.a();
         }
      });

      try {
         this.worldInfo.addToCrashReport(crashreportsystemdetails);
      } catch (Throwable var4) {
         crashreportsystemdetails.addCrashSectionThrowable("Level Data Unobtainable", var4);
      }

      return crashreportsystemdetails;
   }

   public void sendBlockBreakProgress(int i, BlockPos blockposition, int j) {
      for(int k = 0; k < this.eventListeners.size(); ++k) {
         IWorldEventListener iworldaccess = (IWorldEventListener)this.eventListeners.get(k);
         iworldaccess.sendBlockBreakProgress(i, blockposition, j);
      }

   }

   public Calendar getCurrentDate() {
      if (this.getTotalWorldTime() % 600L == 0L) {
         this.theCalendar.setTimeInMillis(MinecraftServer.av());
      }

      return this.theCalendar;
   }

   public Scoreboard getScoreboard() {
      return this.worldScoreboard;
   }

   public void updateComparatorOutputLevel(BlockPos blockposition, Block block) {
      for(EnumFacing enumdirection : EnumFacing.Plane.HORIZONTAL) {
         BlockPos blockposition1 = blockposition.offset(enumdirection);
         if (this.isBlockLoaded(blockposition1)) {
            IBlockState iblockdata = this.getBlockState(blockposition1);
            if (Blocks.UNPOWERED_COMPARATOR.isSameDiode(iblockdata)) {
               iblockdata.neighborChanged(this, blockposition1, block);
            } else if (iblockdata.isNormalCube()) {
               blockposition1 = blockposition1.offset(enumdirection);
               iblockdata = this.getBlockState(blockposition1);
               if (Blocks.UNPOWERED_COMPARATOR.isSameDiode(iblockdata)) {
                  iblockdata.neighborChanged(this, blockposition1, block);
               }
            }
         }
      }

   }

   public DifficultyInstance getDifficultyForLocation(BlockPos blockposition) {
      long i = 0L;
      float f = 0.0F;
      if (this.isBlockLoaded(blockposition)) {
         f = this.getCurrentMoonPhaseFactor();
         i = this.getChunkFromBlockCoords(blockposition).getInhabitedTime();
      }

      return new DifficultyInstance(this.getDifficulty(), this.getWorldTime(), i, f);
   }

   public EnumDifficulty getDifficulty() {
      return this.getWorldInfo().getDifficulty();
   }

   public int getSkylightSubtracted() {
      return this.skylightSubtracted;
   }

   public void setSkylightSubtracted(int i) {
      this.skylightSubtracted = i;
   }

   public void setLastLightningBolt(int i) {
      this.lastLightningBolt = i;
   }

   public VillageCollection getVillageCollection() {
      return this.villageCollectionObj;
   }

   public WorldBorder getWorldBorder() {
      return this.worldBorder;
   }

   public boolean isSpawnChunk(int i, int j) {
      BlockPos blockposition = this.getSpawnPoint();
      int k = i * 16 + 8 - blockposition.getX();
      int l = j * 16 + 8 - blockposition.getZ();
      return k >= -128 && k <= 128 && l >= -128 && l <= 128 && this.keepSpawnInMemory;
   }

   public void sendPacketToServer(Packet packet) {
      throw new UnsupportedOperationException("Can't send packets to server unless you're on the client.");
   }

   public LootTableManager getLootTableManager() {
      return this.lootTable;
   }
}
