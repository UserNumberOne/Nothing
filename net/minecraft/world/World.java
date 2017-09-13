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
      public boolean add(BlockState var1) {
         for(BlockState var3 : this) {
            if (var3.getLocation().equals(var1.getLocation())) {
               return false;
            }
         }

         return super.add(var1);
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

   public Chunk getChunkIfLoaded(int var1, int var2) {
      return ((ChunkProviderServer)this.chunkProvider).getChunkIfLoaded(var1, var2);
   }

   protected World(ISaveHandler var1, WorldInfo var2, WorldProvider var3, Profiler var4, boolean var5, ChunkGenerator var6, Environment var7) {
      this.generator = var6;
      this.world = new CraftWorld((WorldServer)this, var6, var7);
      this.ticksPerAnimalSpawns = (long)this.getServer().getTicksPerAnimalSpawns();
      this.ticksPerMonsterSpawns = (long)this.getServer().getTicksPerMonsterSpawns();
      this.eventListeners = Lists.newArrayList(new IWorldEventListener[]{this.pathListener});
      this.theCalendar = Calendar.getInstance();
      this.worldScoreboard = new Scoreboard();
      this.spawnHostileMobs = true;
      this.spawnPeacefulMobs = true;
      this.lightUpdateBlockList = new int['耀'];
      this.saveHandler = var1;
      this.theProfiler = var4;
      this.worldInfo = var2;
      this.provider = var3;
      this.isRemote = var5;
      this.worldBorder = var3.createWorldBorder();
      this.getWorldBorder().world = (WorldServer)this;
      this.getWorldBorder().addListener(new IBorderListener() {
         public void onSizeChanged(WorldBorder var1, double var2) {
            World.this.getServer().getHandle().sendAll(new SPacketWorldBorder(var1, SPacketWorldBorder.Action.SET_SIZE), var1.world);
         }

         public void onTransitionStarted(WorldBorder var1, double var2, double var4, long var6) {
            World.this.getServer().getHandle().sendAll(new SPacketWorldBorder(var1, SPacketWorldBorder.Action.LERP_SIZE), var1.world);
         }

         public void onCenterChanged(WorldBorder var1, double var2, double var4) {
            World.this.getServer().getHandle().sendAll(new SPacketWorldBorder(var1, SPacketWorldBorder.Action.SET_CENTER), var1.world);
         }

         public void onWarningTimeChanged(WorldBorder var1, int var2) {
            World.this.getServer().getHandle().sendAll(new SPacketWorldBorder(var1, SPacketWorldBorder.Action.SET_WARNING_TIME), var1.world);
         }

         public void onWarningDistanceChanged(WorldBorder var1, int var2) {
            World.this.getServer().getHandle().sendAll(new SPacketWorldBorder(var1, SPacketWorldBorder.Action.SET_WARNING_BLOCKS), var1.world);
         }

         public void onDamageAmountChanged(WorldBorder var1, double var2) {
         }

         public void onDamageBufferChanged(WorldBorder var1, double var2) {
         }
      });
      this.getServer().addWorld(this.world);
   }

   public World init() {
      return this;
   }

   public Biome getBiome(final BlockPos var1) {
      if (this.isBlockLoaded(var1)) {
         Chunk var2 = this.getChunkFromBlockCoords(var1);

         try {
            return var2.getBiome(var1, this.provider.getBiomeProvider());
         } catch (Throwable var6) {
            CrashReport var4 = CrashReport.makeCrashReport(var6, "Getting biome");
            CrashReportCategory var5 = var4.makeCategory("Coordinates of biome request");
            var5.setDetail("Location", new ICrashReportDetail() {
               public String call() throws Exception {
                  return CrashReportCategory.getCoordinateInfo(var1);
               }

               public Object call() throws Exception {
                  return this.call();
               }
            });
            throw new ReportedException(var4);
         }
      } else {
         return this.provider.getBiomeProvider().getBiome(var1, Biomes.PLAINS);
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

   public IBlockState getGroundAboveSeaLevel(BlockPos var1) {
      BlockPos var2;
      for(var2 = new BlockPos(var1.getX(), this.getSeaLevel(), var1.getZ()); !this.isAirBlock(var2.up()); var2 = var2.up()) {
         ;
      }

      return this.getBlockState(var2);
   }

   private boolean isValid(BlockPos var1) {
      return !this.isOutsideBuildHeight(var1) && var1.getX() >= -30000000 && var1.getZ() >= -30000000 && var1.getX() < 30000000 && var1.getZ() < 30000000;
   }

   private boolean isOutsideBuildHeight(BlockPos var1) {
      return var1.getY() < 0 || var1.getY() >= 256;
   }

   public boolean isAirBlock(BlockPos var1) {
      return this.getBlockState(var1).getMaterial() == Material.AIR;
   }

   public boolean isBlockLoaded(BlockPos var1) {
      return this.isBlockLoaded(var1, true);
   }

   public boolean isBlockLoaded(BlockPos var1, boolean var2) {
      return this.isChunkLoaded(var1.getX() >> 4, var1.getZ() >> 4, var2);
   }

   public boolean isAreaLoaded(BlockPos var1, int var2) {
      return this.isAreaLoaded(var1, var2, true);
   }

   public boolean isAreaLoaded(BlockPos var1, int var2, boolean var3) {
      return this.isAreaLoaded(var1.getX() - var2, var1.getY() - var2, var1.getZ() - var2, var1.getX() + var2, var1.getY() + var2, var1.getZ() + var2, var3);
   }

   public boolean isAreaLoaded(BlockPos var1, BlockPos var2) {
      return this.isAreaLoaded(var1, var2, true);
   }

   public boolean isAreaLoaded(BlockPos var1, BlockPos var2, boolean var3) {
      return this.isAreaLoaded(var1.getX(), var1.getY(), var1.getZ(), var2.getX(), var2.getY(), var2.getZ(), var3);
   }

   public boolean isAreaLoaded(StructureBoundingBox var1) {
      return this.isAreaLoaded(var1, true);
   }

   public boolean isAreaLoaded(StructureBoundingBox var1, boolean var2) {
      return this.isAreaLoaded(var1.minX, var1.minY, var1.minZ, var1.maxX, var1.maxY, var1.maxZ, var2);
   }

   private boolean isAreaLoaded(int var1, int var2, int var3, int var4, int var5, int var6, boolean var7) {
      if (var5 >= 0 && var2 < 256) {
         var1 = var1 >> 4;
         var3 = var3 >> 4;
         var4 = var4 >> 4;
         var6 = var6 >> 4;

         for(int var8 = var1; var8 <= var4; ++var8) {
            for(int var9 = var3; var9 <= var6; ++var9) {
               if (!this.isChunkLoaded(var8, var9, var7)) {
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
      return this.getChunkFromChunkCoords(var1.getX() >> 4, var1.getZ() >> 4);
   }

   public Chunk getChunkFromChunkCoords(int var1, int var2) {
      return this.chunkProvider.provideChunk(var1, var2);
   }

   public boolean setBlockState(BlockPos var1, IBlockState var2, int var3) {
      if (this.captureTreeGeneration) {
         Object var7 = null;
         Iterator var8 = this.capturedBlockStates.iterator();

         while(var8.hasNext()) {
            BlockState var9 = (BlockState)var8.next();
            if (var9.getX() == var1.getX() && var9.getY() == var1.getY() && var9.getZ() == var1.getZ()) {
               var7 = var9;
               var8.remove();
               break;
            }
         }

         if (var7 == null) {
            var7 = CraftBlockState.getBlockState(this, var1.getX(), var1.getY(), var1.getZ(), var3);
         }

         ((BlockState)var7).setTypeId(CraftMagicNumbers.getId(var2.getBlock()));
         ((BlockState)var7).setRawData((byte)var2.getBlock().getMetaFromState(var2));
         this.capturedBlockStates.add(var7);
         return true;
      } else if (this.isOutsideBuildHeight(var1)) {
         return false;
      } else if (!this.isRemote && this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) {
         return false;
      } else {
         Chunk var4 = this.getChunkFromBlockCoords(var1);
         var2.getBlock();
         CraftBlockState var5 = null;
         if (this.captureBlockStates) {
            var5 = CraftBlockState.getBlockState(this, var1.getX(), var1.getY(), var1.getZ(), var3);
            this.capturedBlockStates.add(var5);
         }

         IBlockState var6 = var4.setBlockState(var1, var2);
         if (var6 == null) {
            if (this.captureBlockStates) {
               this.capturedBlockStates.remove(var5);
            }

            return false;
         } else {
            if (var2.getLightOpacity() != var6.getLightOpacity() || var2.getLightValue() != var6.getLightValue()) {
               this.theProfiler.startSection("checkLight");
               this.checkLight(var1);
               this.theProfiler.endSection();
            }

            if (!this.captureBlockStates) {
               this.notifyAndUpdatePhysics(var1, var4, var6, var2, var3);
            }

            return true;
         }
      }
   }

   public void notifyAndUpdatePhysics(BlockPos var1, Chunk var2, IBlockState var3, IBlockState var4, int var5) {
      if ((var5 & 2) != 0 && (var2 == null || var2.isPopulated())) {
         this.notifyBlockUpdate(var1, var3, var4, var5);
      }

      if (!this.isRemote && (var5 & 1) != 0) {
         this.notifyNeighborsRespectDebug(var1, var3.getBlock());
         if (var4.hasComparatorInputOverride()) {
            this.updateComparatorOutputLevel(var1, var4.getBlock());
         }
      }

   }

   public boolean setBlockToAir(BlockPos var1) {
      return this.setBlockState(var1, Blocks.AIR.getDefaultState(), 3);
   }

   public boolean destroyBlock(BlockPos var1, boolean var2) {
      IBlockState var3 = this.getBlockState(var1);
      Block var4 = var3.getBlock();
      if (var3.getMaterial() == Material.AIR) {
         return false;
      } else {
         this.playEvent(2001, var1, Block.getStateId(var3));
         if (var2) {
            var4.dropBlockAsItem(this, var1, var3, 0);
         }

         return this.setBlockState(var1, Blocks.AIR.getDefaultState(), 3);
      }
   }

   public boolean setBlockState(BlockPos var1, IBlockState var2) {
      return this.setBlockState(var1, var2, 3);
   }

   public void notifyBlockUpdate(BlockPos var1, IBlockState var2, IBlockState var3, int var4) {
      for(int var5 = 0; var5 < this.eventListeners.size(); ++var5) {
         ((IWorldEventListener)this.eventListeners.get(var5)).notifyBlockUpdate(this, var1, var2, var3, var4);
      }

   }

   public void notifyNeighborsRespectDebug(BlockPos var1, Block var2) {
      if (this.worldInfo.getTerrainType() != WorldType.DEBUG_WORLD) {
         if (this.populating) {
            return;
         }

         this.notifyNeighborsOfStateChange(var1, var2);
      }

   }

   public void markBlocksDirtyVertical(int var1, int var2, int var3, int var4) {
      if (var3 > var4) {
         int var5 = var4;
         var4 = var3;
         var3 = var5;
      }

      if (!this.provider.hasNoSky()) {
         for(int var6 = var3; var6 <= var4; ++var6) {
            this.checkLightFor(EnumSkyBlock.SKY, new BlockPos(var1, var6, var2));
         }
      }

      this.markBlockRangeForRenderUpdate(var1, var3, var2, var1, var4, var2);
   }

   public void markBlockRangeForRenderUpdate(BlockPos var1, BlockPos var2) {
      this.markBlockRangeForRenderUpdate(var1.getX(), var1.getY(), var1.getZ(), var2.getX(), var2.getY(), var2.getZ());
   }

   public void markBlockRangeForRenderUpdate(int var1, int var2, int var3, int var4, int var5, int var6) {
      for(int var7 = 0; var7 < this.eventListeners.size(); ++var7) {
         ((IWorldEventListener)this.eventListeners.get(var7)).markBlockRangeForRenderUpdate(var1, var2, var3, var4, var5, var6);
      }

   }

   public void notifyNeighborsOfStateChange(BlockPos var1, Block var2) {
      this.notifyBlockOfStateChange(var1.west(), var2);
      this.notifyBlockOfStateChange(var1.east(), var2);
      this.notifyBlockOfStateChange(var1.down(), var2);
      this.notifyBlockOfStateChange(var1.up(), var2);
      this.notifyBlockOfStateChange(var1.north(), var2);
      this.notifyBlockOfStateChange(var1.south(), var2);
   }

   public void notifyNeighborsOfStateExcept(BlockPos var1, Block var2, EnumFacing var3) {
      if (var3 != EnumFacing.WEST) {
         this.notifyBlockOfStateChange(var1.west(), var2);
      }

      if (var3 != EnumFacing.EAST) {
         this.notifyBlockOfStateChange(var1.east(), var2);
      }

      if (var3 != EnumFacing.DOWN) {
         this.notifyBlockOfStateChange(var1.down(), var2);
      }

      if (var3 != EnumFacing.UP) {
         this.notifyBlockOfStateChange(var1.up(), var2);
      }

      if (var3 != EnumFacing.NORTH) {
         this.notifyBlockOfStateChange(var1.north(), var2);
      }

      if (var3 != EnumFacing.SOUTH) {
         this.notifyBlockOfStateChange(var1.south(), var2);
      }

   }

   public void notifyBlockOfStateChange(BlockPos var1, final Block var2) {
      if (!this.isRemote) {
         IBlockState var3 = this.getBlockState(var1);

         try {
            CraftWorld var4 = ((WorldServer)this).getWorld();
            if (var4 != null) {
               BlockPhysicsEvent var8 = new BlockPhysicsEvent(var4.getBlockAt(var1.getX(), var1.getY(), var1.getZ()), CraftMagicNumbers.getId(var2));
               this.getServer().getPluginManager().callEvent(var8);
               if (var8.isCancelled()) {
                  return;
               }
            }

            var3.neighborChanged(this, var1, var2);
         } catch (Throwable var7) {
            CrashReport var5 = CrashReport.makeCrashReport(var7, "Exception while updating neighbours");
            CrashReportCategory var6 = var5.makeCategory("Block being updated");
            var6.setDetail("Source block type", new ICrashReportDetail() {
               public String call() throws Exception {
                  try {
                     return String.format("ID #%d (%s // %s)", Block.getIdFromBlock(var2), var2.getUnlocalizedName(), var2.getClass().getCanonicalName());
                  } catch (Throwable var1) {
                     return "ID #" + Block.getIdFromBlock(var2);
                  }
               }

               public Object call() throws Exception {
                  return this.call();
               }
            });
            CrashReportCategory.addBlockInfo(var6, var1, var3);
            throw new ReportedException(var5);
         }
      }

   }

   public boolean isBlockTickPending(BlockPos var1, Block var2) {
      return false;
   }

   public boolean canSeeSky(BlockPos var1) {
      return this.getChunkFromBlockCoords(var1).canSeeSky(var1);
   }

   public boolean canBlockSeeSky(BlockPos var1) {
      if (var1.getY() >= this.getSeaLevel()) {
         return this.canSeeSky(var1);
      } else {
         BlockPos var2 = new BlockPos(var1.getX(), this.getSeaLevel(), var1.getZ());
         if (!this.canSeeSky(var2)) {
            return false;
         } else {
            for(BlockPos var4 = var2.down(); var4.getY() > var1.getY(); var4 = var4.down()) {
               IBlockState var3 = this.getBlockState(var4);
               if (var3.getLightOpacity() > 0 && !var3.getMaterial().isLiquid()) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public int getLight(BlockPos var1) {
      if (var1.getY() < 0) {
         return 0;
      } else {
         if (var1.getY() >= 256) {
            var1 = new BlockPos(var1.getX(), 255, var1.getZ());
         }

         return this.getChunkFromBlockCoords(var1).getLightSubtracted(var1, 0);
      }
   }

   public int getLightFromNeighbors(BlockPos var1) {
      return this.getLight(var1, true);
   }

   public int getLight(BlockPos var1, boolean var2) {
      if (var1.getX() >= -30000000 && var1.getZ() >= -30000000 && var1.getX() < 30000000 && var1.getZ() < 30000000) {
         if (var2 && this.getBlockState(var1).useNeighborBrightness()) {
            int var8 = this.getLight(var1.up(), false);
            int var4 = this.getLight(var1.east(), false);
            int var5 = this.getLight(var1.west(), false);
            int var6 = this.getLight(var1.south(), false);
            int var7 = this.getLight(var1.north(), false);
            if (var4 > var8) {
               var8 = var4;
            }

            if (var5 > var8) {
               var8 = var5;
            }

            if (var6 > var8) {
               var8 = var6;
            }

            if (var7 > var8) {
               var8 = var7;
            }

            return var8;
         } else if (var1.getY() < 0) {
            return 0;
         } else {
            if (var1.getY() >= 256) {
               var1 = new BlockPos(var1.getX(), 255, var1.getZ());
            }

            Chunk var3 = this.getChunkFromBlockCoords(var1);
            return var3.getLightSubtracted(var1, this.skylightSubtracted);
         }
      } else {
         return 15;
      }
   }

   public BlockPos getHeight(BlockPos var1) {
      return new BlockPos(var1.getX(), this.getHeight(var1.getX(), var1.getZ()), var1.getZ());
   }

   public int getHeight(int var1, int var2) {
      int var3;
      if (var1 >= -30000000 && var2 >= -30000000 && var1 < 30000000 && var2 < 30000000) {
         if (this.isChunkLoaded(var1 >> 4, var2 >> 4, true)) {
            var3 = this.getChunkFromChunkCoords(var1 >> 4, var2 >> 4).getHeightValue(var1 & 15, var2 & 15);
         } else {
            var3 = 0;
         }
      } else {
         var3 = this.getSeaLevel() + 1;
      }

      return var3;
   }

   /** @deprecated */
   @Deprecated
   public int getChunksLowestHorizon(int var1, int var2) {
      if (var1 >= -30000000 && var2 >= -30000000 && var1 < 30000000 && var2 < 30000000) {
         if (!this.isChunkLoaded(var1 >> 4, var2 >> 4, true)) {
            return 0;
         } else {
            Chunk var3 = this.getChunkFromChunkCoords(var1 >> 4, var2 >> 4);
            return var3.getLowestHeight();
         }
      } else {
         return this.getSeaLevel() + 1;
      }
   }

   public int getLightFor(EnumSkyBlock var1, BlockPos var2) {
      if (var2.getY() < 0) {
         var2 = new BlockPos(var2.getX(), 0, var2.getZ());
      }

      if (!this.isValid(var2)) {
         return var1.defaultLightValue;
      } else if (!this.isBlockLoaded(var2)) {
         return var1.defaultLightValue;
      } else {
         Chunk var3 = this.getChunkFromBlockCoords(var2);
         return var3.getLightFor(var1, var2);
      }
   }

   public void setLightFor(EnumSkyBlock var1, BlockPos var2, int var3) {
      if (this.isValid(var2) && this.isBlockLoaded(var2)) {
         Chunk var4 = this.getChunkFromBlockCoords(var2);
         var4.setLightFor(var1, var2, var3);
         this.notifyLightSet(var2);
      }

   }

   public void notifyLightSet(BlockPos var1) {
      for(int var2 = 0; var2 < this.eventListeners.size(); ++var2) {
         ((IWorldEventListener)this.eventListeners.get(var2)).notifyLightSet(var1);
      }

   }

   public float getLightBrightness(BlockPos var1) {
      return this.provider.getLightBrightnessTable()[this.getLightFromNeighbors(var1)];
   }

   public IBlockState getBlockState(BlockPos var1) {
      if (this.captureTreeGeneration) {
         for(BlockState var3 : this.capturedBlockStates) {
            if (var3.getX() == var1.getX() && var3.getY() == var1.getY() && var3.getZ() == var1.getZ()) {
               return CraftMagicNumbers.getBlock(var3.getTypeId()).getStateFromMeta(var3.getRawData());
            }
         }
      }

      if (this.isOutsideBuildHeight(var1)) {
         return Blocks.AIR.getDefaultState();
      } else {
         Chunk var4 = this.getChunkFromBlockCoords(var1);
         return var4.getBlockState(var1);
      }
   }

   public boolean isDaytime() {
      return this.skylightSubtracted < 4;
   }

   @Nullable
   public RayTraceResult rayTraceBlocks(Vec3d var1, Vec3d var2) {
      return this.rayTraceBlocks(var1, var2, false, false, false);
   }

   @Nullable
   public RayTraceResult rayTraceBlocks(Vec3d var1, Vec3d var2, boolean var3) {
      return this.rayTraceBlocks(var1, var2, var3, false, false);
   }

   @Nullable
   public RayTraceResult rayTraceBlocks(Vec3d var1, Vec3d var2, boolean var3, boolean var4, boolean var5) {
      if (!Double.isNaN(var1.xCoord) && !Double.isNaN(var1.yCoord) && !Double.isNaN(var1.zCoord)) {
         if (!Double.isNaN(var2.xCoord) && !Double.isNaN(var2.yCoord) && !Double.isNaN(var2.zCoord)) {
            int var6 = MathHelper.floor(var2.xCoord);
            int var7 = MathHelper.floor(var2.yCoord);
            int var8 = MathHelper.floor(var2.zCoord);
            int var9 = MathHelper.floor(var1.xCoord);
            int var10 = MathHelper.floor(var1.yCoord);
            int var11 = MathHelper.floor(var1.zCoord);
            BlockPos var12 = new BlockPos(var9, var10, var11);
            IBlockState var13 = this.getBlockState(var12);
            Block var14 = var13.getBlock();
            if ((!var4 || var13.getCollisionBoundingBox(this, var12) != Block.NULL_AABB) && var14.canCollideCheck(var13, var3)) {
               RayTraceResult var15 = var13.collisionRayTrace(this, var12, var1, var2);
               if (var15 != null) {
                  return var15;
               }
            }

            RayTraceResult var43 = null;
            int var16 = 200;

            while(var16-- >= 0) {
               if (Double.isNaN(var1.xCoord) || Double.isNaN(var1.yCoord) || Double.isNaN(var1.zCoord)) {
                  return null;
               }

               if (var9 == var6 && var10 == var7 && var11 == var8) {
                  return var5 ? var43 : null;
               }

               boolean var17 = true;
               boolean var18 = true;
               boolean var19 = true;
               double var20 = 999.0D;
               double var22 = 999.0D;
               double var24 = 999.0D;
               if (var6 > var9) {
                  var20 = (double)var9 + 1.0D;
               } else if (var6 < var9) {
                  var20 = (double)var9 + 0.0D;
               } else {
                  var17 = false;
               }

               if (var7 > var10) {
                  var22 = (double)var10 + 1.0D;
               } else if (var7 < var10) {
                  var22 = (double)var10 + 0.0D;
               } else {
                  var18 = false;
               }

               if (var8 > var11) {
                  var24 = (double)var11 + 1.0D;
               } else if (var8 < var11) {
                  var24 = (double)var11 + 0.0D;
               } else {
                  var19 = false;
               }

               double var26 = 999.0D;
               double var28 = 999.0D;
               double var30 = 999.0D;
               double var32 = var2.xCoord - var1.xCoord;
               double var34 = var2.yCoord - var1.yCoord;
               double var36 = var2.zCoord - var1.zCoord;
               if (var17) {
                  var26 = (var20 - var1.xCoord) / var32;
               }

               if (var18) {
                  var28 = (var22 - var1.yCoord) / var34;
               }

               if (var19) {
                  var30 = (var24 - var1.zCoord) / var36;
               }

               if (var26 == -0.0D) {
                  var26 = -1.0E-4D;
               }

               if (var28 == -0.0D) {
                  var28 = -1.0E-4D;
               }

               if (var30 == -0.0D) {
                  var30 = -1.0E-4D;
               }

               EnumFacing var38;
               if (var26 < var28 && var26 < var30) {
                  var38 = var6 > var9 ? EnumFacing.WEST : EnumFacing.EAST;
                  var1 = new Vec3d(var20, var1.yCoord + var34 * var26, var1.zCoord + var36 * var26);
               } else if (var28 < var30) {
                  var38 = var7 > var10 ? EnumFacing.DOWN : EnumFacing.UP;
                  var1 = new Vec3d(var1.xCoord + var32 * var28, var22, var1.zCoord + var36 * var28);
               } else {
                  var38 = var8 > var11 ? EnumFacing.NORTH : EnumFacing.SOUTH;
                  var1 = new Vec3d(var1.xCoord + var32 * var30, var1.yCoord + var34 * var30, var24);
               }

               var9 = MathHelper.floor(var1.xCoord) - (var38 == EnumFacing.EAST ? 1 : 0);
               var10 = MathHelper.floor(var1.yCoord) - (var38 == EnumFacing.UP ? 1 : 0);
               var11 = MathHelper.floor(var1.zCoord) - (var38 == EnumFacing.SOUTH ? 1 : 0);
               var12 = new BlockPos(var9, var10, var11);
               IBlockState var39 = this.getBlockState(var12);
               Block var40 = var39.getBlock();
               if (!var4 || var39.getMaterial() == Material.PORTAL || var39.getCollisionBoundingBox(this, var12) != Block.NULL_AABB) {
                  if (var40.canCollideCheck(var39, var3)) {
                     RayTraceResult var41 = var39.collisionRayTrace(this, var12, var1, var2);
                     if (var41 != null) {
                        return var41;
                     }
                  } else {
                     var43 = new RayTraceResult(RayTraceResult.Type.MISS, var1, var38, var12);
                  }
               }
            }

            return var5 ? var43 : null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public void playSound(@Nullable EntityPlayer var1, BlockPos var2, SoundEvent var3, SoundCategory var4, float var5, float var6) {
      this.playSound(var1, (double)var2.getX() + 0.5D, (double)var2.getY() + 0.5D, (double)var2.getZ() + 0.5D, var3, var4, var5, var6);
   }

   public void playSound(@Nullable EntityPlayer var1, double var2, double var4, double var6, SoundEvent var8, SoundCategory var9, float var10, float var11) {
      for(int var12 = 0; var12 < this.eventListeners.size(); ++var12) {
         ((IWorldEventListener)this.eventListeners.get(var12)).playSoundToAllNearExcept(var1, var8, var9, var2, var4, var6, var10, var11);
      }

   }

   public void playSound(double var1, double var3, double var5, SoundEvent var7, SoundCategory var8, float var9, float var10, boolean var11) {
   }

   public void playRecord(BlockPos var1, @Nullable SoundEvent var2) {
      for(int var3 = 0; var3 < this.eventListeners.size(); ++var3) {
         ((IWorldEventListener)this.eventListeners.get(var3)).playRecord(var2, var1);
      }

   }

   public void spawnParticle(EnumParticleTypes var1, double var2, double var4, double var6, double var8, double var10, double var12, int... var14) {
      this.spawnParticle(var1.getParticleID(), var1.getShouldIgnoreRange(), var2, var4, var6, var8, var10, var12, var14);
   }

   private void spawnParticle(int var1, boolean var2, double var3, double var5, double var7, double var9, double var11, double var13, int... var15) {
      for(int var16 = 0; var16 < this.eventListeners.size(); ++var16) {
         ((IWorldEventListener)this.eventListeners.get(var16)).spawnParticle(var1, var2, var3, var5, var7, var9, var11, var13, var15);
      }

   }

   public boolean addWeatherEffect(Entity var1) {
      this.weatherEffects.add(var1);
      return true;
   }

   public boolean spawnEntity(Entity var1) {
      return this.addEntity(var1, SpawnReason.DEFAULT);
   }

   public boolean addEntity(Entity var1, SpawnReason var2) {
      if (var1 == null) {
         return false;
      } else {
         int var3 = MathHelper.floor(var1.posX / 16.0D);
         int var4 = MathHelper.floor(var1.posZ / 16.0D);
         boolean var5 = var1.forceSpawn;
         if (var1 instanceof EntityPlayer) {
            var5 = true;
         }

         Object var6 = null;
         if (var1 instanceof EntityLivingBase && !(var1 instanceof EntityPlayerMP)) {
            boolean var7 = var1 instanceof EntityAnimal || var1 instanceof EntityWaterMob || var1 instanceof EntityGolem;
            boolean var8 = var1 instanceof EntityMob || var1 instanceof EntityGhast || var1 instanceof EntitySlime;
            if (var2 != SpawnReason.CUSTOM && (var7 && !this.spawnPeacefulMobs || var8 && !this.spawnHostileMobs)) {
               var1.isDead = true;
               return false;
            }

            var6 = CraftEventFactory.callCreatureSpawnEvent((EntityLivingBase)var1, var2);
         } else if (var1 instanceof EntityItem) {
            var6 = CraftEventFactory.callItemSpawnEvent((EntityItem)var1);
         } else if (var1.getBukkitEntity() instanceof Projectile) {
            var6 = CraftEventFactory.callProjectileLaunchEvent(var1);
         }

         if (var6 == null || !((Cancellable)var6).isCancelled() && !var1.isDead) {
            if (!var5 && !this.isChunkLoaded(var3, var4, false)) {
               return false;
            } else {
               if (var1 instanceof EntityPlayer) {
                  EntityPlayer var9 = (EntityPlayer)var1;
                  this.playerEntities.add(var9);
                  this.updateAllPlayersSleepingFlag();
               }

               this.getChunkFromChunkCoords(var3, var4).addEntity(var1);
               this.loadedEntityList.add(var1);
               this.onEntityAdded(var1);
               return true;
            }
         } else {
            var1.isDead = true;
            return false;
         }
      }
   }

   protected void onEntityAdded(Entity var1) {
      for(int var2 = 0; var2 < this.eventListeners.size(); ++var2) {
         ((IWorldEventListener)this.eventListeners.get(var2)).onEntityAdded(var1);
      }

      var1.valid = true;
   }

   protected void onEntityRemoved(Entity var1) {
      for(int var2 = 0; var2 < this.eventListeners.size(); ++var2) {
         ((IWorldEventListener)this.eventListeners.get(var2)).onEntityRemoved(var1);
      }

      var1.valid = false;
   }

   public void removeEntity(Entity var1) {
      if (var1.isBeingRidden()) {
         var1.removePassengers();
      }

      if (var1.isRiding()) {
         var1.dismountRidingEntity();
      }

      var1.setDead();
      if (var1 instanceof EntityPlayer) {
         this.playerEntities.remove(var1);
         this.updateAllPlayersSleepingFlag();
         this.onEntityRemoved(var1);
      }

   }

   public void removeEntityDangerously(Entity var1) {
      var1.setDropItemsWhenDead(false);
      var1.setDead();
      if (var1 instanceof EntityPlayer) {
         this.playerEntities.remove(var1);
         this.updateAllPlayersSleepingFlag();
      }

      int var2 = var1.chunkCoordX;
      int var3 = var1.chunkCoordZ;
      if (var1.addedToChunk && this.isChunkLoaded(var2, var3, true)) {
         this.getChunkFromChunkCoords(var2, var3).removeEntity(var1);
      }

      int var4 = this.loadedEntityList.indexOf(var1);
      if (var4 != -1) {
         if (var4 <= this.tickPosition) {
            --this.tickPosition;
         }

         this.loadedEntityList.remove(var4);
      }

      this.onEntityRemoved(var1);
   }

   public void addEventListener(IWorldEventListener var1) {
      this.eventListeners.add(var1);
   }

   public List getCollisionBoxes(@Nullable Entity var1, AxisAlignedBB var2) {
      ArrayList var3 = Lists.newArrayList();
      int var4 = MathHelper.floor(var2.minX) - 1;
      int var5 = MathHelper.ceil(var2.maxX) + 1;
      int var6 = MathHelper.floor(var2.minY) - 1;
      int var7 = MathHelper.ceil(var2.maxY) + 1;
      int var8 = MathHelper.floor(var2.minZ) - 1;
      int var9 = MathHelper.ceil(var2.maxZ) + 1;
      WorldBorder var10 = this.getWorldBorder();
      boolean var11 = var1 != null && var1.isOutsideBorder();
      boolean var12 = var1 != null && this.isInsideBorder(var10, var1);
      IBlockState var13 = Blocks.STONE.getDefaultState();
      BlockPos.PooledMutableBlockPos var14 = BlockPos.PooledMutableBlockPos.retain();

      for(int var15 = var4; var15 < var5; ++var15) {
         for(int var16 = var8; var16 < var9; ++var16) {
            int var17 = (var15 != var4 && var15 != var5 - 1 ? 0 : 1) + (var16 != var8 && var16 != var9 - 1 ? 0 : 1);
            if (var17 != 2 && this.isBlockLoaded(var14.setPos(var15, 64, var16))) {
               for(int var18 = var6; var18 < var7; ++var18) {
                  if (var17 <= 0 || var18 != var6 && var18 != var7 - 1) {
                     var14.setPos(var15, var18, var16);
                     if (var1 != null) {
                        if (var11 && var12) {
                           var1.setOutsideBorder(false);
                        } else if (!var11 && !var12) {
                           var1.setOutsideBorder(true);
                        }
                     }

                     IBlockState var19 = var13;
                     if (var10.contains(var14) || !var12) {
                        var19 = this.getBlockState(var14);
                     }

                     var19.addCollisionBoxToList(this, var14, var2, var3, var1);
                  }
               }
            }
         }
      }

      var14.release();
      if (var1 != null) {
         List var20 = this.getEntitiesWithinAABBExcludingEntity(var1, var2.expandXyz(0.25D));

         for(int var21 = 0; var21 < var20.size(); ++var21) {
            Entity var22 = (Entity)var20.get(var21);
            if (!var1.isRidingSameEntity(var22)) {
               AxisAlignedBB var23 = var22.getCollisionBoundingBox();
               if (var23 != null && var23.intersectsWith(var2)) {
                  var3.add(var23);
               }

               var23 = var1.getCollisionBox(var22);
               if (var23 != null && var23.intersectsWith(var2)) {
                  var3.add(var23);
               }
            }
         }
      }

      return var3;
   }

   public boolean isInsideBorder(WorldBorder var1, Entity var2) {
      double var3 = var1.minX();
      double var5 = var1.minZ();
      double var7 = var1.maxX();
      double var9 = var1.maxZ();
      if (var2.isOutsideBorder()) {
         ++var3;
         ++var5;
         --var7;
         --var9;
      } else {
         --var3;
         --var5;
         ++var7;
         ++var9;
      }

      return var2.posX > var3 && var2.posX < var7 && var2.posZ > var5 && var2.posZ < var9;
   }

   public List getCollisionBoxes(AxisAlignedBB var1) {
      ArrayList var2 = Lists.newArrayList();
      int var3 = MathHelper.floor(var1.minX) - 1;
      int var4 = MathHelper.ceil(var1.maxX) + 1;
      int var5 = MathHelper.floor(var1.minY) - 1;
      int var6 = MathHelper.ceil(var1.maxY) + 1;
      int var7 = MathHelper.floor(var1.minZ) - 1;
      int var8 = MathHelper.ceil(var1.maxZ) + 1;
      BlockPos.PooledMutableBlockPos var9 = BlockPos.PooledMutableBlockPos.retain();

      for(int var10 = var3; var10 < var4; ++var10) {
         for(int var11 = var7; var11 < var8; ++var11) {
            int var12 = (var10 != var3 && var10 != var4 - 1 ? 0 : 1) + (var11 != var7 && var11 != var8 - 1 ? 0 : 1);
            if (var12 != 2 && this.isBlockLoaded(var9.setPos(var10, 64, var11))) {
               for(int var13 = var5; var13 < var6; ++var13) {
                  if (var12 <= 0 || var13 != var5 && var13 != var6 - 1) {
                     var9.setPos(var10, var13, var11);
                     IBlockState var14;
                     if (var10 >= -30000000 && var10 < 30000000 && var11 >= -30000000 && var11 < 30000000) {
                        var14 = this.getBlockState(var9);
                     } else {
                        var14 = Blocks.BEDROCK.getDefaultState();
                     }

                     var14.addCollisionBoxToList(this, var9, var1, var2, (Entity)null);
                  }
               }
            }
         }
      }

      var9.release();
      return var2;
   }

   public boolean collidesWithAnyBlock(AxisAlignedBB var1) {
      ArrayList var2 = Lists.newArrayList();
      int var3 = MathHelper.floor(var1.minX) - 1;
      int var4 = MathHelper.ceil(var1.maxX) + 1;
      int var5 = MathHelper.floor(var1.minY) - 1;
      int var6 = MathHelper.ceil(var1.maxY) + 1;
      int var7 = MathHelper.floor(var1.minZ) - 1;
      int var8 = MathHelper.ceil(var1.maxZ) + 1;
      BlockPos.PooledMutableBlockPos var9 = BlockPos.PooledMutableBlockPos.retain();

      try {
         for(int var10 = var3; var10 < var4; ++var10) {
            for(int var11 = var7; var11 < var8; ++var11) {
               int var12 = (var10 != var3 && var10 != var4 - 1 ? 0 : 1) + (var11 != var7 && var11 != var8 - 1 ? 0 : 1);
               if (var12 != 2 && this.isBlockLoaded(var9.setPos(var10, 64, var11))) {
                  for(int var13 = var5; var13 < var6; ++var13) {
                     if (var12 <= 0 || var13 != var5 && var13 != var6 - 1) {
                        var9.setPos(var10, var13, var11);
                        if (var10 < -30000000 || var10 >= 30000000 || var11 < -30000000 || var11 >= 30000000) {
                           boolean var20 = true;
                           boolean var21 = var20;
                           return var21;
                        }

                        IBlockState var14 = this.getBlockState(var9);
                        var14.addCollisionBoxToList(this, var9, var1, var2, (Entity)null);
                        if (!var2.isEmpty()) {
                           boolean var16 = true;
                           boolean var15 = var16;
                           return var15;
                        }
                     }
                  }
               }
            }
         }

         return false;
      } finally {
         var9.release();
      }
   }

   public int calculateSkylightSubtracted(float var1) {
      float var2 = this.getCelestialAngle(var1);
      float var3 = 1.0F - (MathHelper.cos(var2 * 6.2831855F) * 2.0F + 0.5F);
      var3 = MathHelper.clamp(var3, 0.0F, 1.0F);
      var3 = 1.0F - var3;
      var3 = (float)((double)var3 * (1.0D - (double)(this.getRainStrength(var1) * 5.0F) / 16.0D));
      var3 = (float)((double)var3 * (1.0D - (double)(this.getThunderStrength(var1) * 5.0F) / 16.0D));
      var3 = 1.0F - var3;
      return (int)(var3 * 11.0F);
   }

   public float getCelestialAngle(float var1) {
      return this.provider.calculateCelestialAngle(this.worldInfo.getWorldTime(), var1);
   }

   public float getCurrentMoonPhaseFactor() {
      return WorldProvider.MOON_PHASE_FACTORS[this.provider.getMoonPhase(this.worldInfo.getWorldTime())];
   }

   public float getCelestialAngleRadians(float var1) {
      float var2 = this.getCelestialAngle(var1);
      return var2 * 6.2831855F;
   }

   public BlockPos getPrecipitationHeight(BlockPos var1) {
      return this.getChunkFromBlockCoords(var1).getPrecipitationHeight(var1);
   }

   public BlockPos getTopSolidOrLiquidBlock(BlockPos var1) {
      Chunk var2 = this.getChunkFromBlockCoords(var1);

      BlockPos var3;
      BlockPos var4;
      for(var3 = new BlockPos(var1.getX(), var2.getTopFilledSegment() + 16, var1.getZ()); var3.getY() >= 0; var3 = var4) {
         var4 = var3.down();
         Material var5 = var2.getBlockState(var4).getMaterial();
         if (var5.blocksMovement() && var5 != Material.LEAVES) {
            break;
         }
      }

      return var3;
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

      for(int var1 = 0; var1 < this.weatherEffects.size(); ++var1) {
         Entity var2 = (Entity)this.weatherEffects.get(var1);
         if (var2 != null) {
            try {
               ++var2.ticksExisted;
               var2.onUpdate();
            } catch (Throwable var13) {
               CrashReport var4 = CrashReport.makeCrashReport(var13, "Ticking entity");
               CrashReportCategory var5 = var4.makeCategory("Entity being ticked");
               if (var2 == null) {
                  var5.addCrashSection("Entity", "~~NULL~~");
               } else {
                  var2.addEntityCrashInfo(var5);
               }

               throw new ReportedException(var4);
            }

            if (var2.isDead) {
               this.weatherEffects.remove(var1--);
            }
         }
      }

      this.theProfiler.endStartSection("remove");
      this.loadedEntityList.removeAll(this.unloadedEntityList);

      for(int var14 = 0; var14 < this.unloadedEntityList.size(); ++var14) {
         Entity var16 = (Entity)this.unloadedEntityList.get(var14);
         int var19 = var16.chunkCoordX;
         int var3 = var16.chunkCoordZ;
         if (var16.addedToChunk && this.isChunkLoaded(var19, var3, true)) {
            this.getChunkFromChunkCoords(var19, var3).removeEntity(var16);
         }
      }

      for(int var15 = 0; var15 < this.unloadedEntityList.size(); ++var15) {
         this.onEntityRemoved((Entity)this.unloadedEntityList.get(var15));
      }

      this.unloadedEntityList.clear();
      this.tickPlayers();
      this.theProfiler.endStartSection("regular");

      for(this.tickPosition = 0; this.tickPosition < this.loadedEntityList.size(); ++this.tickPosition) {
         Entity var17 = (Entity)this.loadedEntityList.get(this.tickPosition);
         Entity var6 = var17.getRidingEntity();
         if (var6 != null) {
            if (!var6.isDead && var6.isPassenger(var17)) {
               continue;
            }

            var17.dismountRidingEntity();
         }

         this.theProfiler.startSection("tick");
         if (!var17.isDead && !(var17 instanceof EntityPlayerMP)) {
            try {
               this.updateEntity(var17);
            } catch (Throwable var12) {
               CrashReport var22 = CrashReport.makeCrashReport(var12, "Ticking entity");
               CrashReportCategory var20 = var22.makeCategory("Entity being ticked");
               var17.addEntityCrashInfo(var20);
               throw new ReportedException(var22);
            }
         }

         this.theProfiler.endSection();
         this.theProfiler.startSection("remove");
         if (var17.isDead) {
            int var18 = var17.chunkCoordX;
            int var7 = var17.chunkCoordZ;
            if (var17.addedToChunk && this.isChunkLoaded(var18, var7, true)) {
               this.getChunkFromChunkCoords(var18, var7).removeEntity(var17);
            }

            this.loadedEntityList.remove(this.tickPosition--);
            this.onEntityRemoved(var17);
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

      Iterator var24 = this.tickableTileEntities.iterator();

      while(var24.hasNext()) {
         TileEntity var25 = (TileEntity)var24.next();
         if (!var25.isInvalid() && var25.hasWorld()) {
            BlockPos var8 = var25.getPos();
            if (this.isBlockLoaded(var8) && this.worldBorder.contains(var8)) {
               try {
                  this.theProfiler.startSection("");
                  ((ITickable)var25).update();
                  this.theProfiler.endSection();
               } catch (Throwable var11) {
                  CrashReport var23 = CrashReport.makeCrashReport(var11, "Ticking block entity");
                  CrashReportCategory var21 = var23.makeCategory("Block entity being ticked");
                  var25.addInfoToCrashReport(var21);
                  throw new ReportedException(var23);
               }
            }
         }

         if (var25.isInvalid()) {
            var24.remove();
            this.loadedTileEntityList.remove(var25);
            if (this.isBlockLoaded(var25.getPos())) {
               this.getChunkFromBlockCoords(var25.getPos()).removeTileEntity(var25.getPos());
            }
         }
      }

      this.processingLoadedTiles = false;
      this.theProfiler.endStartSection("pendingBlockEntities");
      if (!this.addedTileEntityList.isEmpty()) {
         for(int var26 = 0; var26 < this.addedTileEntityList.size(); ++var26) {
            TileEntity var27 = (TileEntity)this.addedTileEntityList.get(var26);
            if (!var27.isInvalid() && this.isBlockLoaded(var27.getPos())) {
               Chunk var9 = this.getChunkFromBlockCoords(var27.getPos());
               IBlockState var10 = var9.getBlockState(var27.getPos());
               var9.addTileEntity(var27.getPos(), var27);
               this.notifyBlockUpdate(var27.getPos(), var10, var10, 3);
               if (!this.loadedTileEntityList.contains(var27)) {
                  this.addTileEntity(var27);
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
      boolean var2 = this.loadedTileEntityList.add(var1);
      if (var2 && var1 instanceof ITickable) {
         this.tickableTileEntities.add(var1);
      }

      if (this.isRemote) {
         BlockPos var3 = var1.getPos();
         IBlockState var4 = this.getBlockState(var3);
         this.notifyBlockUpdate(var3, var4, var4, 2);
      }

      return var2;
   }

   public void addTileEntities(Collection var1) {
      if (this.processingLoadedTiles) {
         this.addedTileEntityList.addAll(var1);
      } else {
         for(TileEntity var3 : var1) {
            this.addTileEntity(var3);
         }
      }

   }

   public void updateEntity(Entity var1) {
      this.updateEntityWithOptionalForce(var1, true);
   }

   public void updateEntityWithOptionalForce(Entity var1, boolean var2) {
      int var3 = MathHelper.floor(var1.posX);
      int var4 = MathHelper.floor(var1.posZ);
      Chunk var5 = this.getChunkIfLoaded(var3 >> 4, var4 >> 4);
      if (!var2 || var5 != null && var5.areNeighborsLoaded(2)) {
         var1.lastTickPosX = var1.posX;
         var1.lastTickPosY = var1.posY;
         var1.lastTickPosZ = var1.posZ;
         var1.prevRotationYaw = var1.rotationYaw;
         var1.prevRotationPitch = var1.rotationPitch;
         if (var2 && var1.addedToChunk) {
            ++var1.ticksExisted;
            if (var1.isRiding()) {
               var1.updateRidden();
            } else {
               var1.onUpdate();
            }
         }

         this.theProfiler.startSection("chunkCheck");
         if (Double.isNaN(var1.posX) || Double.isInfinite(var1.posX)) {
            var1.posX = var1.lastTickPosX;
         }

         if (Double.isNaN(var1.posY) || Double.isInfinite(var1.posY)) {
            var1.posY = var1.lastTickPosY;
         }

         if (Double.isNaN(var1.posZ) || Double.isInfinite(var1.posZ)) {
            var1.posZ = var1.lastTickPosZ;
         }

         if (Double.isNaN((double)var1.rotationPitch) || Double.isInfinite((double)var1.rotationPitch)) {
            var1.rotationPitch = var1.prevRotationPitch;
         }

         if (Double.isNaN((double)var1.rotationYaw) || Double.isInfinite((double)var1.rotationYaw)) {
            var1.rotationYaw = var1.prevRotationYaw;
         }

         int var6 = MathHelper.floor(var1.posX / 16.0D);
         int var7 = MathHelper.floor(var1.posY / 16.0D);
         int var8 = MathHelper.floor(var1.posZ / 16.0D);
         if (!var1.addedToChunk || var1.chunkCoordX != var6 || var1.chunkCoordY != var7 || var1.chunkCoordZ != var8) {
            if (var1.addedToChunk && this.isChunkLoaded(var1.chunkCoordX, var1.chunkCoordZ, true)) {
               this.getChunkFromChunkCoords(var1.chunkCoordX, var1.chunkCoordZ).removeEntityAtIndex(var1, var1.chunkCoordY);
            }

            if (!var1.setPositionNonDirty() && !this.isChunkLoaded(var6, var8, true)) {
               var1.addedToChunk = false;
            } else {
               this.getChunkFromChunkCoords(var6, var8).addEntity(var1);
            }
         }

         this.theProfiler.endSection();
         if (var2 && var1.addedToChunk) {
            for(Entity var10 : var1.getPassengers()) {
               if (!var10.isDead && var10.getRidingEntity() == var1) {
                  this.updateEntity(var10);
               } else {
                  var10.dismountRidingEntity();
               }
            }
         }
      }

   }

   public boolean checkNoEntityCollision(AxisAlignedBB var1) {
      return this.checkNoEntityCollision(var1, (Entity)null);
   }

   public boolean checkNoEntityCollision(AxisAlignedBB var1, @Nullable Entity var2) {
      List var3 = this.getEntitiesWithinAABBExcludingEntity((Entity)null, var1);

      for(int var4 = 0; var4 < var3.size(); ++var4) {
         Entity var5 = (Entity)var3.get(var4);
         if (!var5.isDead && var5.preventEntitySpawning && var5 != var2 && (var2 == null || var5.isRidingSameEntity(var2))) {
            return false;
         }
      }

      return true;
   }

   public boolean checkBlockCollision(AxisAlignedBB var1) {
      int var2 = MathHelper.floor(var1.minX);
      int var3 = MathHelper.ceil(var1.maxX);
      int var4 = MathHelper.floor(var1.minY);
      int var5 = MathHelper.ceil(var1.maxY);
      int var6 = MathHelper.floor(var1.minZ);
      int var7 = MathHelper.ceil(var1.maxZ);
      BlockPos.PooledMutableBlockPos var8 = BlockPos.PooledMutableBlockPos.retain();

      for(int var9 = var2; var9 < var3; ++var9) {
         for(int var10 = var4; var10 < var5; ++var10) {
            for(int var11 = var6; var11 < var7; ++var11) {
               IBlockState var12 = this.getBlockState(var8.setPos(var9, var10, var11));
               if (var12.getMaterial() != Material.AIR) {
                  var8.release();
                  return true;
               }
            }
         }
      }

      var8.release();
      return false;
   }

   public boolean containsAnyLiquid(AxisAlignedBB var1) {
      int var2 = MathHelper.floor(var1.minX);
      int var3 = MathHelper.ceil(var1.maxX);
      int var4 = MathHelper.floor(var1.minY);
      int var5 = MathHelper.ceil(var1.maxY);
      int var6 = MathHelper.floor(var1.minZ);
      int var7 = MathHelper.ceil(var1.maxZ);
      BlockPos.PooledMutableBlockPos var8 = BlockPos.PooledMutableBlockPos.retain();

      for(int var9 = var2; var9 < var3; ++var9) {
         for(int var10 = var4; var10 < var5; ++var10) {
            for(int var11 = var6; var11 < var7; ++var11) {
               IBlockState var12 = this.getBlockState(var8.setPos(var9, var10, var11));
               if (var12.getMaterial().isLiquid()) {
                  var8.release();
                  return true;
               }
            }
         }
      }

      var8.release();
      return false;
   }

   public boolean isFlammableWithin(AxisAlignedBB var1) {
      int var2 = MathHelper.floor(var1.minX);
      int var3 = MathHelper.ceil(var1.maxX);
      int var4 = MathHelper.floor(var1.minY);
      int var5 = MathHelper.ceil(var1.maxY);
      int var6 = MathHelper.floor(var1.minZ);
      int var7 = MathHelper.ceil(var1.maxZ);
      if (this.isAreaLoaded(var2, var4, var6, var3, var5, var7, true)) {
         BlockPos.PooledMutableBlockPos var8 = BlockPos.PooledMutableBlockPos.retain();

         for(int var9 = var2; var9 < var3; ++var9) {
            for(int var10 = var4; var10 < var5; ++var10) {
               for(int var11 = var6; var11 < var7; ++var11) {
                  Block var12 = this.getBlockState(var8.setPos(var9, var10, var11)).getBlock();
                  if (var12 == Blocks.FIRE || var12 == Blocks.FLOWING_LAVA || var12 == Blocks.LAVA) {
                     var8.release();
                     return true;
                  }
               }
            }
         }

         var8.release();
      }

      return false;
   }

   public boolean handleMaterialAcceleration(AxisAlignedBB var1, Material var2, Entity var3) {
      int var4 = MathHelper.floor(var1.minX);
      int var5 = MathHelper.ceil(var1.maxX);
      int var6 = MathHelper.floor(var1.minY);
      int var7 = MathHelper.ceil(var1.maxY);
      int var8 = MathHelper.floor(var1.minZ);
      int var9 = MathHelper.ceil(var1.maxZ);
      if (!this.isAreaLoaded(var4, var6, var8, var5, var7, var9, true)) {
         return false;
      } else {
         boolean var10 = false;
         Vec3d var11 = Vec3d.ZERO;
         BlockPos.PooledMutableBlockPos var12 = BlockPos.PooledMutableBlockPos.retain();

         for(int var13 = var4; var13 < var5; ++var13) {
            for(int var14 = var6; var14 < var7; ++var14) {
               for(int var15 = var8; var15 < var9; ++var15) {
                  var12.setPos(var13, var14, var15);
                  IBlockState var16 = this.getBlockState(var12);
                  Block var17 = var16.getBlock();
                  if (var16.getMaterial() == var2) {
                     double var18 = (double)((float)(var14 + 1) - BlockLiquid.getLiquidHeightPercent(((Integer)var16.getValue(BlockLiquid.LEVEL)).intValue()));
                     if ((double)var7 >= var18) {
                        var10 = true;
                        var11 = var17.modifyAcceleration(this, var12, var3, var11);
                     }
                  }
               }
            }
         }

         var12.release();
         if (var11.lengthVector() > 0.0D && var3.isPushedByWater()) {
            var11 = var11.normalize();
            var3.motionX += var11.xCoord * 0.014D;
            var3.motionY += var11.yCoord * 0.014D;
            var3.motionZ += var11.zCoord * 0.014D;
         }

         return var10;
      }
   }

   public boolean isMaterialInBB(AxisAlignedBB var1, Material var2) {
      int var3 = MathHelper.floor(var1.minX);
      int var4 = MathHelper.ceil(var1.maxX);
      int var5 = MathHelper.floor(var1.minY);
      int var6 = MathHelper.ceil(var1.maxY);
      int var7 = MathHelper.floor(var1.minZ);
      int var8 = MathHelper.ceil(var1.maxZ);
      BlockPos.PooledMutableBlockPos var9 = BlockPos.PooledMutableBlockPos.retain();

      for(int var10 = var3; var10 < var4; ++var10) {
         for(int var11 = var5; var11 < var6; ++var11) {
            for(int var12 = var7; var12 < var8; ++var12) {
               if (this.getBlockState(var9.setPos(var10, var11, var12)).getMaterial() == var2) {
                  var9.release();
                  return true;
               }
            }
         }
      }

      var9.release();
      return false;
   }

   public boolean isAABBInMaterial(AxisAlignedBB var1, Material var2) {
      int var3 = MathHelper.floor(var1.minX);
      int var4 = MathHelper.ceil(var1.maxX);
      int var5 = MathHelper.floor(var1.minY);
      int var6 = MathHelper.ceil(var1.maxY);
      int var7 = MathHelper.floor(var1.minZ);
      int var8 = MathHelper.ceil(var1.maxZ);
      BlockPos.PooledMutableBlockPos var9 = BlockPos.PooledMutableBlockPos.retain();

      for(int var10 = var3; var10 < var4; ++var10) {
         for(int var11 = var5; var11 < var6; ++var11) {
            for(int var12 = var7; var12 < var8; ++var12) {
               IBlockState var13 = this.getBlockState(var9.setPos(var10, var11, var12));
               if (var13.getMaterial() == var2) {
                  int var14 = ((Integer)var13.getValue(BlockLiquid.LEVEL)).intValue();
                  double var15 = (double)(var11 + 1);
                  if (var14 < 8) {
                     var15 = (double)(var11 + 1) - (double)var14 / 8.0D;
                  }

                  if (var15 >= var1.minY) {
                     var9.release();
                     return true;
                  }
               }
            }
         }
      }

      var9.release();
      return false;
   }

   public Explosion createExplosion(@Nullable Entity var1, double var2, double var4, double var6, float var8, boolean var9) {
      return this.newExplosion(var1, var2, var4, var6, var8, false, var9);
   }

   public Explosion newExplosion(@Nullable Entity var1, double var2, double var4, double var6, float var8, boolean var9, boolean var10) {
      Explosion var11 = new Explosion(this, var1, var2, var4, var6, var8, var9, var10);
      var11.doExplosionA();
      var11.doExplosionB(true);
      return var11;
   }

   public float getBlockDensity(Vec3d var1, AxisAlignedBB var2) {
      double var3 = 1.0D / ((var2.maxX - var2.minX) * 2.0D + 1.0D);
      double var5 = 1.0D / ((var2.maxY - var2.minY) * 2.0D + 1.0D);
      double var7 = 1.0D / ((var2.maxZ - var2.minZ) * 2.0D + 1.0D);
      double var9 = (1.0D - Math.floor(1.0D / var3) * var3) / 2.0D;
      double var11 = (1.0D - Math.floor(1.0D / var7) * var7) / 2.0D;
      if (var3 >= 0.0D && var5 >= 0.0D && var7 >= 0.0D) {
         int var13 = 0;
         int var14 = 0;

         for(float var15 = 0.0F; var15 <= 1.0F; var15 = (float)((double)var15 + var3)) {
            for(float var16 = 0.0F; var16 <= 1.0F; var16 = (float)((double)var16 + var5)) {
               for(float var17 = 0.0F; var17 <= 1.0F; var17 = (float)((double)var17 + var7)) {
                  double var18 = var2.minX + (var2.maxX - var2.minX) * (double)var15;
                  double var20 = var2.minY + (var2.maxY - var2.minY) * (double)var16;
                  double var22 = var2.minZ + (var2.maxZ - var2.minZ) * (double)var17;
                  if (this.rayTraceBlocks(new Vec3d(var18 + var9, var20, var22 + var11), var1) == null) {
                     ++var13;
                  }

                  ++var14;
               }
            }
         }

         return (float)var13 / (float)var14;
      } else {
         return 0.0F;
      }
   }

   public boolean extinguishFire(@Nullable EntityPlayer var1, BlockPos var2, EnumFacing var3) {
      var2 = var2.offset(var3);
      if (this.getBlockState(var2).getBlock() == Blocks.FIRE) {
         this.playEvent(var1, 1009, var2, 0);
         this.setBlockToAir(var2);
         return true;
      } else {
         return false;
      }
   }

   @Nullable
   public TileEntity getTileEntity(BlockPos var1) {
      if (this.isOutsideBuildHeight(var1)) {
         return null;
      } else if (this.capturedTileEntities.containsKey(var1)) {
         return (TileEntity)this.capturedTileEntities.get(var1);
      } else {
         TileEntity var2 = null;
         if (this.processingLoadedTiles) {
            var2 = this.getPendingTileEntityAt(var1);
         }

         if (var2 == null) {
            var2 = this.getChunkFromBlockCoords(var1).getTileEntity(var1, Chunk.EnumCreateEntityType.IMMEDIATE);
         }

         if (var2 == null) {
            var2 = this.getPendingTileEntityAt(var1);
         }

         return var2;
      }
   }

   @Nullable
   private TileEntity getPendingTileEntityAt(BlockPos var1) {
      for(int var2 = 0; var2 < this.addedTileEntityList.size(); ++var2) {
         TileEntity var3 = (TileEntity)this.addedTileEntityList.get(var2);
         if (!var3.isInvalid() && var3.getPos().equals(var1)) {
            return var3;
         }
      }

      return null;
   }

   public void setTileEntity(BlockPos var1, @Nullable TileEntity var2) {
      if (!this.isOutsideBuildHeight(var1) && var2 != null && !var2.isInvalid()) {
         if (this.captureBlockStates) {
            var2.setWorld(this);
            var2.setPos(var1);
            this.capturedTileEntities.put(var1, var2);
            return;
         }

         if (this.processingLoadedTiles) {
            var2.setPos(var1);
            Iterator var3 = this.addedTileEntityList.iterator();

            while(var3.hasNext()) {
               TileEntity var4 = (TileEntity)var3.next();
               if (var4.getPos().equals(var1)) {
                  var4.invalidate();
                  var3.remove();
               }
            }

            this.addedTileEntityList.add(var2);
         } else {
            this.addTileEntity(var2);
            this.getChunkFromBlockCoords(var1).addTileEntity(var1, var2);
         }
      }

   }

   public void removeTileEntity(BlockPos var1) {
      TileEntity var2 = this.getTileEntity(var1);
      if (var2 != null && this.processingLoadedTiles) {
         var2.invalidate();
         this.addedTileEntityList.remove(var2);
      } else {
         if (var2 != null) {
            this.addedTileEntityList.remove(var2);
            this.loadedTileEntityList.remove(var2);
            this.tickableTileEntities.remove(var2);
         }

         this.getChunkFromBlockCoords(var1).removeTileEntity(var1);
      }

   }

   public void markTileEntityForRemoval(TileEntity var1) {
      this.tileEntitiesToBeRemoved.add(var1);
   }

   public boolean isBlockFullCube(BlockPos var1) {
      AxisAlignedBB var2 = this.getBlockState(var1).getCollisionBoundingBox(this, var1);
      return var2 != Block.NULL_AABB && var2.getAverageEdgeLength() >= 1.0D;
   }

   public boolean isBlockNormalCube(BlockPos var1, boolean var2) {
      if (this.isOutsideBuildHeight(var1)) {
         return false;
      } else {
         Chunk var3 = this.chunkProvider.getLoadedChunk(var1.getX() >> 4, var1.getZ() >> 4);
         if (var3 != null && !var3.isEmpty()) {
            IBlockState var4 = this.getBlockState(var1);
            return var4.getMaterial().isOpaque() && var4.isFullCube();
         } else {
            return var2;
         }
      }
   }

   public void calculateInitialSkylight() {
      int var1 = this.calculateSkylightSubtracted(1.0F);
      if (var1 != this.skylightSubtracted) {
         this.skylightSubtracted = var1;
      }

   }

   public void setAllowedSpawnTypes(boolean var1, boolean var2) {
      this.spawnHostileMobs = var1;
      this.spawnPeacefulMobs = var2;
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
         int var1 = this.worldInfo.getCleanWeatherTime();
         if (var1 > 0) {
            --var1;
            this.worldInfo.setCleanWeatherTime(var1);
            this.worldInfo.setThunderTime(this.worldInfo.isThundering() ? 1 : 2);
            this.worldInfo.setRainTime(this.worldInfo.isRaining() ? 1 : 2);
         }

         int var2 = this.worldInfo.getThunderTime();
         if (var2 <= 0) {
            if (this.worldInfo.isThundering()) {
               this.worldInfo.setThunderTime(this.rand.nextInt(12000) + 3600);
            } else {
               this.worldInfo.setThunderTime(this.rand.nextInt(168000) + 12000);
            }
         } else {
            --var2;
            this.worldInfo.setThunderTime(var2);
            if (var2 <= 0) {
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
         int var3 = this.worldInfo.getRainTime();
         if (var3 <= 0) {
            if (this.worldInfo.isRaining()) {
               this.worldInfo.setRainTime(this.rand.nextInt(12000) + 12000);
            } else {
               this.worldInfo.setRainTime(this.rand.nextInt(168000) + 12000);
            }
         } else {
            --var3;
            this.worldInfo.setRainTime(var3);
            if (var3 <= 0) {
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

         for(int var4 = 0; var4 < this.playerEntities.size(); ++var4) {
            if (((EntityPlayerMP)this.playerEntities.get(var4)).world == this) {
               ((EntityPlayerMP)this.playerEntities.get(var4)).tickWeather();
            }
         }
      }

   }

   protected void updateBlocks() {
   }

   public void immediateBlockTick(BlockPos var1, IBlockState var2, Random var3) {
      this.scheduledUpdatesAreImmediate = true;
      var2.getBlock().updateTick(this, var1, var2, var3);
      this.scheduledUpdatesAreImmediate = false;
   }

   public boolean canBlockFreezeWater(BlockPos var1) {
      return this.canBlockFreeze(var1, false);
   }

   public boolean canBlockFreezeNoWater(BlockPos var1) {
      return this.canBlockFreeze(var1, true);
   }

   public boolean canBlockFreeze(BlockPos var1, boolean var2) {
      Biome var3 = this.getBiome(var1);
      float var4 = var3.getFloatTemperature(var1);
      if (var4 > 0.15F) {
         return false;
      } else {
         if (var1.getY() >= 0 && var1.getY() < 256 && this.getLightFor(EnumSkyBlock.BLOCK, var1) < 10) {
            IBlockState var5 = this.getBlockState(var1);
            Block var6 = var5.getBlock();
            if ((var6 == Blocks.WATER || var6 == Blocks.FLOWING_WATER) && ((Integer)var5.getValue(BlockLiquid.LEVEL)).intValue() == 0) {
               if (!var2) {
                  return true;
               }

               boolean var7 = this.isWater(var1.west()) && this.isWater(var1.east()) && this.isWater(var1.north()) && this.isWater(var1.south());
               if (!var7) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private boolean isWater(BlockPos var1) {
      return this.getBlockState(var1).getMaterial() == Material.WATER;
   }

   public boolean canSnowAt(BlockPos var1, boolean var2) {
      Biome var3 = this.getBiome(var1);
      float var4 = var3.getFloatTemperature(var1);
      if (var4 > 0.15F) {
         return false;
      } else if (!var2) {
         return true;
      } else {
         if (var1.getY() >= 0 && var1.getY() < 256 && this.getLightFor(EnumSkyBlock.BLOCK, var1) < 10) {
            IBlockState var5 = this.getBlockState(var1);
            if (var5.getMaterial() == Material.AIR && Blocks.SNOW_LAYER.canPlaceBlockAt(this, var1)) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean checkLight(BlockPos var1) {
      boolean var2 = false;
      if (!this.provider.hasNoSky()) {
         var2 |= this.checkLightFor(EnumSkyBlock.SKY, var1);
      }

      var2 = var2 | this.checkLightFor(EnumSkyBlock.BLOCK, var1);
      return var2;
   }

   private int getRawLight(BlockPos var1, EnumSkyBlock var2) {
      if (var2 == EnumSkyBlock.SKY && this.canSeeSky(var1)) {
         return 15;
      } else {
         IBlockState var3 = this.getBlockState(var1);
         int var4 = var2 == EnumSkyBlock.SKY ? 0 : var3.getLightValue();
         int var5 = var3.getLightOpacity();
         if (var5 >= 15 && var3.getLightValue() > 0) {
            var5 = 1;
         }

         if (var5 < 1) {
            var5 = 1;
         }

         if (var5 >= 15) {
            return 0;
         } else if (var4 >= 14) {
            return var4;
         } else {
            BlockPos.PooledMutableBlockPos var6 = BlockPos.PooledMutableBlockPos.retain();

            for(EnumFacing var10 : EnumFacing.values()) {
               var6.setPos(var1).move(var10);
               int var11 = this.getLightFor(var2, var6) - var5;
               if (var11 > var4) {
                  var4 = var11;
               }

               if (var4 >= 14) {
                  return var4;
               }
            }

            var6.release();
            return var4;
         }
      }
   }

   public boolean checkLightFor(EnumSkyBlock var1, BlockPos var2) {
      Chunk var3 = this.getChunkIfLoaded(var2.getX() >> 4, var2.getZ() >> 4);
      if (var3 != null && var3.areNeighborsLoaded(1)) {
         int var4 = 0;
         int var5 = 0;
         this.theProfiler.startSection("getBrightness");
         int var6 = this.getLightFor(var1, var2);
         int var7 = this.getRawLight(var2, var1);
         int var8 = var2.getX();
         int var9 = var2.getY();
         int var10 = var2.getZ();
         if (var7 > var6) {
            this.lightUpdateBlockList[var5++] = 133152;
         } else if (var7 < var6) {
            this.lightUpdateBlockList[var5++] = 133152 | var6 << 18;

            while(var4 < var5) {
               int var11 = this.lightUpdateBlockList[var4++];
               int var12 = (var11 & 63) - 32 + var8;
               int var13 = (var11 >> 6 & 63) - 32 + var9;
               int var14 = (var11 >> 12 & 63) - 32 + var10;
               int var15 = var11 >> 18 & 15;
               BlockPos var16 = new BlockPos(var12, var13, var14);
               int var17 = this.getLightFor(var1, var16);
               if (var17 == var15) {
                  this.setLightFor(var1, var16, 0);
                  if (var15 > 0) {
                     int var18 = MathHelper.abs(var12 - var8);
                     int var19 = MathHelper.abs(var13 - var9);
                     int var20 = MathHelper.abs(var14 - var10);
                     if (var18 + var19 + var20 < 17) {
                        BlockPos.PooledMutableBlockPos var21 = BlockPos.PooledMutableBlockPos.retain();

                        for(EnumFacing var25 : EnumFacing.values()) {
                           int var26 = var12 + var25.getFrontOffsetX();
                           int var27 = var13 + var25.getFrontOffsetY();
                           int var28 = var14 + var25.getFrontOffsetZ();
                           var21.setPos(var26, var27, var28);
                           int var29 = Math.max(1, this.getBlockState(var21).getLightOpacity());
                           var17 = this.getLightFor(var1, var21);
                           if (var17 == var15 - var29 && var5 < this.lightUpdateBlockList.length) {
                              this.lightUpdateBlockList[var5++] = var26 - var8 + 32 | var27 - var9 + 32 << 6 | var28 - var10 + 32 << 12 | var15 - var29 << 18;
                           }
                        }

                        var21.release();
                     }
                  }
               }
            }

            var4 = 0;
         }

         this.theProfiler.endSection();
         this.theProfiler.startSection("checkedPosition < toCheckCount");

         while(var4 < var5) {
            int var30 = this.lightUpdateBlockList[var4++];
            int var31 = (var30 & 63) - 32 + var8;
            int var32 = (var30 >> 6 & 63) - 32 + var9;
            int var33 = (var30 >> 12 & 63) - 32 + var10;
            BlockPos var34 = new BlockPos(var31, var32, var33);
            int var35 = this.getLightFor(var1, var34);
            int var37 = this.getRawLight(var34, var1);
            if (var37 != var35) {
               this.setLightFor(var1, var34, var37);
               if (var37 > var35) {
                  int var38 = Math.abs(var31 - var8);
                  int var39 = Math.abs(var32 - var9);
                  int var40 = Math.abs(var33 - var10);
                  boolean var41 = var5 < this.lightUpdateBlockList.length - 6;
                  if (var38 + var39 + var40 < 17 && var41) {
                     if (this.getLightFor(var1, var34.west()) < var37) {
                        this.lightUpdateBlockList[var5++] = var31 - 1 - var8 + 32 + (var32 - var9 + 32 << 6) + (var33 - var10 + 32 << 12);
                     }

                     if (this.getLightFor(var1, var34.east()) < var37) {
                        this.lightUpdateBlockList[var5++] = var31 + 1 - var8 + 32 + (var32 - var9 + 32 << 6) + (var33 - var10 + 32 << 12);
                     }

                     if (this.getLightFor(var1, var34.down()) < var37) {
                        this.lightUpdateBlockList[var5++] = var31 - var8 + 32 + (var32 - 1 - var9 + 32 << 6) + (var33 - var10 + 32 << 12);
                     }

                     if (this.getLightFor(var1, var34.up()) < var37) {
                        this.lightUpdateBlockList[var5++] = var31 - var8 + 32 + (var32 + 1 - var9 + 32 << 6) + (var33 - var10 + 32 << 12);
                     }

                     if (this.getLightFor(var1, var34.north()) < var37) {
                        this.lightUpdateBlockList[var5++] = var31 - var8 + 32 + (var32 - var9 + 32 << 6) + (var33 - 1 - var10 + 32 << 12);
                     }

                     if (this.getLightFor(var1, var34.south()) < var37) {
                        this.lightUpdateBlockList[var5++] = var31 - var8 + 32 + (var32 - var9 + 32 << 6) + (var33 + 1 - var10 + 32 << 12);
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
      return this.getEntitiesInAABBexcluding(var1, var2, EntitySelectors.NOT_SPECTATING);
   }

   public List getEntitiesInAABBexcluding(@Nullable Entity var1, AxisAlignedBB var2, @Nullable Predicate var3) {
      ArrayList var4 = Lists.newArrayList();
      int var5 = MathHelper.floor((var2.minX - 2.0D) / 16.0D);
      int var6 = MathHelper.floor((var2.maxX + 2.0D) / 16.0D);
      int var7 = MathHelper.floor((var2.minZ - 2.0D) / 16.0D);
      int var8 = MathHelper.floor((var2.maxZ + 2.0D) / 16.0D);

      for(int var9 = var5; var9 <= var6; ++var9) {
         for(int var10 = var7; var10 <= var8; ++var10) {
            if (this.isChunkLoaded(var9, var10, true)) {
               this.getChunkFromChunkCoords(var9, var10).getEntitiesWithinAABBForEntity(var1, var2, var4, var3);
            }
         }
      }

      return var4;
   }

   public List getEntities(Class var1, Predicate var2) {
      ArrayList var3 = Lists.newArrayList();

      for(Entity var5 : this.loadedEntityList) {
         if (var1.isAssignableFrom(var5.getClass()) && var2.apply(var5)) {
            var3.add(var5);
         }
      }

      return var3;
   }

   public List getPlayers(Class var1, Predicate var2) {
      ArrayList var3 = Lists.newArrayList();

      for(Entity var5 : this.playerEntities) {
         if (var1.isAssignableFrom(var5.getClass()) && var2.apply(var5)) {
            var3.add(var5);
         }
      }

      return var3;
   }

   public List getEntitiesWithinAABB(Class var1, AxisAlignedBB var2) {
      return this.getEntitiesWithinAABB(var1, var2, EntitySelectors.NOT_SPECTATING);
   }

   public List getEntitiesWithinAABB(Class var1, AxisAlignedBB var2, @Nullable Predicate var3) {
      int var4 = MathHelper.floor((var2.minX - 2.0D) / 16.0D);
      int var5 = MathHelper.ceil((var2.maxX + 2.0D) / 16.0D);
      int var6 = MathHelper.floor((var2.minZ - 2.0D) / 16.0D);
      int var7 = MathHelper.ceil((var2.maxZ + 2.0D) / 16.0D);
      ArrayList var8 = Lists.newArrayList();

      for(int var9 = var4; var9 < var5; ++var9) {
         for(int var10 = var6; var10 < var7; ++var10) {
            if (this.isChunkLoaded(var9, var10, true)) {
               this.getChunkFromChunkCoords(var9, var10).getEntitiesOfTypeWithinAAAB(var1, var2, var8, var3);
            }
         }
      }

      return var8;
   }

   @Nullable
   public Entity findNearestEntityWithinAABB(Class var1, AxisAlignedBB var2, Entity var3) {
      List var4 = this.getEntitiesWithinAABB(var1, var2);
      Entity var5 = null;
      double var6 = Double.MAX_VALUE;

      for(int var8 = 0; var8 < var4.size(); ++var8) {
         Entity var9 = (Entity)var4.get(var8);
         if (var9 != var3 && EntitySelectors.NOT_SPECTATING.apply(var9)) {
            double var10 = var3.getDistanceSqToEntity(var9);
            if (var10 <= var6) {
               var5 = var9;
               var6 = var10;
            }
         }
      }

      return var5;
   }

   @Nullable
   public Entity getEntityByID(int var1) {
      return (Entity)this.entitiesById.lookup(var1);
   }

   public void markChunkDirty(BlockPos var1, TileEntity var2) {
      if (this.isBlockLoaded(var1)) {
         this.getChunkFromBlockCoords(var1).setChunkModified();
      }

   }

   public int countEntities(Class var1) {
      int var2 = 0;
      Iterator var3 = this.loadedEntityList.iterator();

      while(true) {
         Entity var4;
         while(true) {
            if (!var3.hasNext()) {
               return var2;
            }

            var4 = (Entity)var3.next();
            if (!(var4 instanceof EntityLiving)) {
               break;
            }

            EntityLiving var5 = (EntityLiving)var4;
            if (!var5.canDespawn() || !var5.isNoDespawnRequired()) {
               break;
            }
         }

         if (var1.isAssignableFrom(var4.getClass())) {
            ++var2;
         }
      }
   }

   public void loadEntities(Collection var1) {
      for(Entity var3 : var1) {
         if (var3 != null) {
            this.loadedEntityList.add(var3);
            this.onEntityAdded(var3);
         }
      }

   }

   public void unloadEntities(Collection var1) {
      this.unloadedEntityList.addAll(var1);
   }

   public boolean canBlockBePlaced(Block var1, BlockPos var2, boolean var3, EnumFacing var4, @Nullable Entity var5, @Nullable ItemStack var6) {
      IBlockState var7 = this.getBlockState(var2);
      AxisAlignedBB var8 = var3 ? null : var1.getDefaultState().getCollisionBoundingBox(this, var2);
      boolean var9 = var8 != Block.NULL_AABB && !this.checkNoEntityCollision(var8.offset(var2), var5) ? false : (var7.getMaterial() == Material.CIRCUITS && var1 == Blocks.ANVIL ? true : var7.getMaterial().isReplaceable() && var1.canReplace(this, var2, var4, var6));
      BlockCanBuildEvent var10 = new BlockCanBuildEvent(this.getWorld().getBlockAt(var2.getX(), var2.getY(), var2.getZ()), CraftMagicNumbers.getId(var1), var9);
      this.getServer().getPluginManager().callEvent(var10);
      return var10.isBuildable();
   }

   public int getSeaLevel() {
      return this.seaLevel;
   }

   public void setSeaLevel(int var1) {
      this.seaLevel = var1;
   }

   public int getStrongPower(BlockPos var1, EnumFacing var2) {
      return this.getBlockState(var1).getStrongPower(this, var1, var2);
   }

   public WorldType getWorldType() {
      return this.worldInfo.getTerrainType();
   }

   public int getStrongPower(BlockPos var1) {
      byte var2 = 0;
      int var3 = Math.max(var2, this.getStrongPower(var1.down(), EnumFacing.DOWN));
      if (var3 >= 15) {
         return var3;
      } else {
         var3 = Math.max(var3, this.getStrongPower(var1.up(), EnumFacing.UP));
         if (var3 >= 15) {
            return var3;
         } else {
            var3 = Math.max(var3, this.getStrongPower(var1.north(), EnumFacing.NORTH));
            if (var3 >= 15) {
               return var3;
            } else {
               var3 = Math.max(var3, this.getStrongPower(var1.south(), EnumFacing.SOUTH));
               if (var3 >= 15) {
                  return var3;
               } else {
                  var3 = Math.max(var3, this.getStrongPower(var1.west(), EnumFacing.WEST));
                  if (var3 >= 15) {
                     return var3;
                  } else {
                     var3 = Math.max(var3, this.getStrongPower(var1.east(), EnumFacing.EAST));
                     return var3 >= 15 ? var3 : var3;
                  }
               }
            }
         }
      }
   }

   public boolean isSidePowered(BlockPos var1, EnumFacing var2) {
      return this.getRedstonePower(var1, var2) > 0;
   }

   public int getRedstonePower(BlockPos var1, EnumFacing var2) {
      IBlockState var3 = this.getBlockState(var1);
      return var3.isNormalCube() ? this.getStrongPower(var1) : var3.getWeakPower(this, var1, var2);
   }

   public boolean isBlockPowered(BlockPos var1) {
      return this.getRedstonePower(var1.down(), EnumFacing.DOWN) > 0 ? true : (this.getRedstonePower(var1.up(), EnumFacing.UP) > 0 ? true : (this.getRedstonePower(var1.north(), EnumFacing.NORTH) > 0 ? true : (this.getRedstonePower(var1.south(), EnumFacing.SOUTH) > 0 ? true : (this.getRedstonePower(var1.west(), EnumFacing.WEST) > 0 ? true : this.getRedstonePower(var1.east(), EnumFacing.EAST) > 0))));
   }

   public int isBlockIndirectlyGettingPowered(BlockPos var1) {
      int var2 = 0;

      for(EnumFacing var6 : EnumFacing.values()) {
         int var7 = this.getRedstonePower(var1.offset(var6), var6);
         if (var7 >= 15) {
            return 15;
         }

         if (var7 > var2) {
            var2 = var7;
         }
      }

      return var2;
   }

   @Nullable
   public EntityPlayer getClosestPlayerToEntity(Entity var1, double var2) {
      return this.getClosestPlayer(var1.posX, var1.posY, var1.posZ, var2, false);
   }

   @Nullable
   public EntityPlayer getNearestPlayerNotCreative(Entity var1, double var2) {
      return this.getClosestPlayer(var1.posX, var1.posY, var1.posZ, var2, true);
   }

   @Nullable
   public EntityPlayer getClosestPlayer(double var1, double var3, double var5, double var7, boolean var9) {
      double var10 = -1.0D;
      EntityPlayer var12 = null;

      for(int var13 = 0; var13 < this.playerEntities.size(); ++var13) {
         EntityPlayer var14 = (EntityPlayer)this.playerEntities.get(var13);
         if (var14 != null && !var14.isDead && (EntitySelectors.CAN_AI_TARGET.apply(var14) || !var9) && (EntitySelectors.NOT_SPECTATING.apply(var14) || var9)) {
            double var15 = var14.getDistanceSq(var1, var3, var5);
            if ((var7 < 0.0D || var15 < var7 * var7) && (var10 == -1.0D || var15 < var10)) {
               var10 = var15;
               var12 = var14;
            }
         }
      }

      return var12;
   }

   public boolean isAnyPlayerWithinRangeAt(double var1, double var3, double var5, double var7) {
      for(int var9 = 0; var9 < this.playerEntities.size(); ++var9) {
         EntityPlayer var10 = (EntityPlayer)this.playerEntities.get(var9);
         if (EntitySelectors.NOT_SPECTATING.apply(var10)) {
            double var11 = var10.getDistanceSq(var1, var3, var5);
            if (var7 < 0.0D || var11 < var7 * var7) {
               return true;
            }
         }
      }

      return false;
   }

   @Nullable
   public EntityPlayer getNearestAttackablePlayer(Entity var1, double var2, double var4) {
      return this.getNearestAttackablePlayer(var1.posX, var1.posY, var1.posZ, var2, var4, (Function)null, (Predicate)null);
   }

   @Nullable
   public EntityPlayer getNearestAttackablePlayer(BlockPos var1, double var2, double var4) {
      return this.getNearestAttackablePlayer((double)((float)var1.getX() + 0.5F), (double)((float)var1.getY() + 0.5F), (double)((float)var1.getZ() + 0.5F), var2, var4, (Function)null, (Predicate)null);
   }

   @Nullable
   public EntityPlayer getNearestAttackablePlayer(double var1, double var3, double var5, double var7, double var9, @Nullable Function var11, @Nullable Predicate var12) {
      double var13 = -1.0D;
      EntityPlayer var15 = null;

      for(int var16 = 0; var16 < this.playerEntities.size(); ++var16) {
         EntityPlayer var17 = (EntityPlayer)this.playerEntities.get(var16);
         if (!var17.capabilities.disableDamage && var17.isEntityAlive() && !var17.isSpectator() && (var12 == null || var12.apply(var17))) {
            double var18 = var17.getDistanceSq(var1, var17.posY, var5);
            double var20 = var7;
            if (var17.isSneaking()) {
               var20 = var7 * 0.800000011920929D;
            }

            if (var17.isInvisible()) {
               float var22 = var17.getArmorVisibility();
               if (var22 < 0.1F) {
                  var22 = 0.1F;
               }

               var20 *= (double)(0.7F * var22);
            }

            if (var11 != null) {
               var20 *= ((Double)Objects.firstNonNull((Double)var11.apply(var17), Double.valueOf(1.0D))).doubleValue();
            }

            if ((var9 < 0.0D || Math.abs(var17.posY - var3) < var9 * var9) && (var7 < 0.0D || var18 < var20 * var20) && (var13 == -1.0D || var18 < var13)) {
               var13 = var18;
               var15 = var17;
            }
         }
      }

      return var15;
   }

   @Nullable
   public EntityPlayer getPlayerEntityByName(String var1) {
      for(int var2 = 0; var2 < this.playerEntities.size(); ++var2) {
         EntityPlayer var3 = (EntityPlayer)this.playerEntities.get(var2);
         if (var1.equals(var3.getName())) {
            return var3;
         }
      }

      return null;
   }

   @Nullable
   public EntityPlayer getPlayerEntityByUUID(UUID var1) {
      for(int var2 = 0; var2 < this.playerEntities.size(); ++var2) {
         EntityPlayer var3 = (EntityPlayer)this.playerEntities.get(var2);
         if (var1.equals(var3.getUniqueID())) {
            return var3;
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

   public void setWorldTime(long var1) {
      this.worldInfo.setWorldTime(var1);
   }

   public BlockPos getSpawnPoint() {
      BlockPos var1 = new BlockPos(this.worldInfo.getSpawnX(), this.worldInfo.getSpawnY(), this.worldInfo.getSpawnZ());
      if (!this.getWorldBorder().contains(var1)) {
         var1 = this.getHeight(new BlockPos(this.getWorldBorder().getCenterX(), 0.0D, this.getWorldBorder().getCenterZ()));
      }

      return var1;
   }

   public void setSpawnPoint(BlockPos var1) {
      this.worldInfo.setSpawn(var1);
   }

   public boolean isBlockModifiable(EntityPlayer var1, BlockPos var2) {
      return true;
   }

   public void setEntityState(Entity var1, byte var2) {
   }

   public IChunkProvider getChunkProvider() {
      return this.chunkProvider;
   }

   public void addBlockEvent(BlockPos var1, Block var2, int var3, int var4) {
      this.getBlockState(var1).onBlockEventReceived(this, var1, var3, var4);
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

   public float getThunderStrength(float var1) {
      return (this.prevThunderingStrength + (this.thunderingStrength - this.prevThunderingStrength) * var1) * this.getRainStrength(var1);
   }

   public float getRainStrength(float var1) {
      return this.prevRainingStrength + (this.rainingStrength - this.prevRainingStrength) * var1;
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
      } else if (!this.canSeeSky(var1)) {
         return false;
      } else if (this.getPrecipitationHeight(var1).getY() > var1.getY()) {
         return false;
      } else {
         Biome var2 = this.getBiome(var1);
         return var2.getEnableSnow() ? false : (this.canSnowAt(var1, false) ? false : var2.canRain());
      }
   }

   public boolean isBlockinHighHumidity(BlockPos var1) {
      Biome var2 = this.getBiome(var1);
      return var2.isHighHumidity();
   }

   @Nullable
   public MapStorage getMapStorage() {
      return this.mapStorage;
   }

   public void setData(String var1, WorldSavedData var2) {
      this.mapStorage.setData(var1, var2);
   }

   @Nullable
   public WorldSavedData loadData(Class var1, String var2) {
      return this.mapStorage.getOrLoadData(var1, var2);
   }

   public int getUniqueDataId(String var1) {
      return this.mapStorage.getUniqueDataId(var1);
   }

   public void playBroadcastSound(int var1, BlockPos var2, int var3) {
      for(int var4 = 0; var4 < this.eventListeners.size(); ++var4) {
         ((IWorldEventListener)this.eventListeners.get(var4)).broadcastSound(var1, var2, var3);
      }

   }

   public void playEvent(int var1, BlockPos var2, int var3) {
      this.playEvent((EntityPlayer)null, var1, var2, var3);
   }

   public void playEvent(@Nullable EntityPlayer var1, int var2, BlockPos var3, int var4) {
      try {
         for(int var5 = 0; var5 < this.eventListeners.size(); ++var5) {
            ((IWorldEventListener)this.eventListeners.get(var5)).playEvent(var1, var2, var3, var4);
         }

      } catch (Throwable var8) {
         CrashReport var6 = CrashReport.makeCrashReport(var8, "Playing level event");
         CrashReportCategory var7 = var6.makeCategory("Level event being played");
         var7.addCrashSection("Block coordinates", CrashReportCategory.getCoordinateInfo(var3));
         var7.addCrashSection("Event source", var1);
         var7.addCrashSection("Event type", Integer.valueOf(var2));
         var7.addCrashSection("Event data", Integer.valueOf(var4));
         throw new ReportedException(var6);
      }
   }

   public int getHeight() {
      return 256;
   }

   public int getActualHeight() {
      return this.provider.hasNoSky() ? 128 : 256;
   }

   public Random setRandomSeed(int var1, int var2, int var3) {
      long var4 = (long)var1 * 341873128712L + (long)var2 * 132897987541L + this.getWorldInfo().getSeed() + (long)var3;
      this.rand.setSeed(var4);
      return this.rand;
   }

   public CrashReportCategory addWorldInfoToCrashReport(CrashReport var1) {
      CrashReportCategory var2 = var1.makeCategoryDepth("Affected level", 1);
      var2.addCrashSection("Level name", this.worldInfo == null ? "????" : this.worldInfo.getWorldName());
      var2.setDetail("All players", new ICrashReportDetail() {
         public String a() {
            return World.this.playerEntities.size() + " total; " + World.this.playerEntities;
         }

         public Object call() throws Exception {
            return this.a();
         }
      });
      var2.setDetail("Chunk stats", new ICrashReportDetail() {
         public String a() {
            return World.this.chunkProvider.makeString();
         }

         public Object call() throws Exception {
            return this.a();
         }
      });

      try {
         this.worldInfo.addToCrashReport(var2);
      } catch (Throwable var4) {
         var2.addCrashSectionThrowable("Level Data Unobtainable", var4);
      }

      return var2;
   }

   public void sendBlockBreakProgress(int var1, BlockPos var2, int var3) {
      for(int var4 = 0; var4 < this.eventListeners.size(); ++var4) {
         IWorldEventListener var5 = (IWorldEventListener)this.eventListeners.get(var4);
         var5.sendBlockBreakProgress(var1, var2, var3);
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

   public void updateComparatorOutputLevel(BlockPos var1, Block var2) {
      for(EnumFacing var4 : EnumFacing.Plane.HORIZONTAL) {
         BlockPos var5 = var1.offset(var4);
         if (this.isBlockLoaded(var5)) {
            IBlockState var6 = this.getBlockState(var5);
            if (Blocks.UNPOWERED_COMPARATOR.isSameDiode(var6)) {
               var6.neighborChanged(this, var5, var2);
            } else if (var6.isNormalCube()) {
               var5 = var5.offset(var4);
               var6 = this.getBlockState(var5);
               if (Blocks.UNPOWERED_COMPARATOR.isSameDiode(var6)) {
                  var6.neighborChanged(this, var5, var2);
               }
            }
         }
      }

   }

   public DifficultyInstance getDifficultyForLocation(BlockPos var1) {
      long var2 = 0L;
      float var4 = 0.0F;
      if (this.isBlockLoaded(var1)) {
         var4 = this.getCurrentMoonPhaseFactor();
         var2 = this.getChunkFromBlockCoords(var1).getInhabitedTime();
      }

      return new DifficultyInstance(this.getDifficulty(), this.getWorldTime(), var2, var4);
   }

   public EnumDifficulty getDifficulty() {
      return this.getWorldInfo().getDifficulty();
   }

   public int getSkylightSubtracted() {
      return this.skylightSubtracted;
   }

   public void setSkylightSubtracted(int var1) {
      this.skylightSubtracted = var1;
   }

   public void setLastLightningBolt(int var1) {
      this.lastLightningBolt = var1;
   }

   public VillageCollection getVillageCollection() {
      return this.villageCollectionObj;
   }

   public WorldBorder getWorldBorder() {
      return this.worldBorder;
   }

   public boolean isSpawnChunk(int var1, int var2) {
      BlockPos var3 = this.getSpawnPoint();
      int var4 = var1 * 16 + 8 - var3.getX();
      int var5 = var2 * 16 + 8 - var3.getZ();
      return var4 >= -128 && var4 <= 128 && var5 >= -128 && var5 <= 128 && this.keepSpawnInMemory;
   }

   public void sendPacketToServer(Packet var1) {
      throw new UnsupportedOperationException("Can't send packets to server unless you're on the client.");
   }

   public LootTableManager getLootTableManager() {
      return this.lootTable;
   }
}
