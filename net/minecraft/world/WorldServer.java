package net.minecraft.world;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.INpc;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.passive.HorseType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketBlockAction;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.network.play.server.SPacketSpawnGlobalEntity;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.ScoreboardSaveData;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ReportedException;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.VillageCollection;
import net.minecraft.village.VillageSiege;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.feature.WorldGeneratorBonusChest;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.WorldSpecificSaveHandler;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldServer extends World implements IThreadListener {
   private static final Logger LOGGER = LogManager.getLogger();
   private final MinecraftServer mcServer;
   private final EntityTracker theEntityTracker;
   private final PlayerChunkMap playerChunkMap;
   private final Set pendingTickListEntriesHashSet = Sets.newHashSet();
   private final TreeSet pendingTickListEntriesTreeSet = new TreeSet();
   private final Map entitiesByUuid = Maps.newHashMap();
   public boolean disableLevelSaving;
   private boolean allPlayersSleeping;
   private int updateEntityTick;
   private final Teleporter worldTeleporter;
   private final WorldEntitySpawner entitySpawner = new WorldEntitySpawner();
   protected final VillageSiege villageSiege = new VillageSiege(this);
   private final WorldServer.ServerBlockEventList[] blockEventQueue = new WorldServer.ServerBlockEventList[]{new WorldServer.ServerBlockEventList(), new WorldServer.ServerBlockEventList()};
   private int blockEventCacheIndex;
   private final List pendingTickListEntriesThisTick = Lists.newArrayList();
   protected Set doneChunks = new HashSet();
   public List customTeleporters = new ArrayList();

   public WorldServer(MinecraftServer var1, ISaveHandler var2, WorldInfo var3, int var4, Profiler var5) {
      super(saveHandlerIn, info, DimensionManager.createProviderFor(dimensionId), profilerIn, false);
      this.mcServer = server;
      this.theEntityTracker = new EntityTracker(this);
      this.playerChunkMap = new PlayerChunkMap(this);
      int providerDim = this.provider.getDimension();
      this.provider.registerWorld(this);
      this.provider.setDimension(providerDim);
      this.chunkProvider = this.createChunkProvider();
      this.perWorldStorage = new MapStorage(new WorldSpecificSaveHandler(this, saveHandlerIn));
      this.worldTeleporter = new Teleporter(this);
      this.calculateInitialSkylight();
      this.calculateInitialWeather();
      this.getWorldBorder().setSize(server.getMaxWorldSize());
      DimensionManager.setWorld(dimensionId, this, this.mcServer);
   }

   public World init() {
      this.mapStorage = new MapStorage(this.saveHandler);
      String s = VillageCollection.fileNameForProvider(this.provider);
      VillageCollection villagecollection = (VillageCollection)this.perWorldStorage.getOrLoadData(VillageCollection.class, s);
      if (villagecollection == null) {
         this.villageCollectionObj = new VillageCollection(this);
         this.perWorldStorage.setData(s, this.villageCollectionObj);
      } else {
         this.villageCollectionObj = villagecollection;
         this.villageCollectionObj.setWorldsForAll(this);
      }

      this.worldScoreboard = new ServerScoreboard(this.mcServer);
      ScoreboardSaveData scoreboardsavedata = (ScoreboardSaveData)this.mapStorage.getOrLoadData(ScoreboardSaveData.class, "scoreboard");
      if (scoreboardsavedata == null) {
         scoreboardsavedata = new ScoreboardSaveData();
         this.mapStorage.setData("scoreboard", scoreboardsavedata);
      }

      scoreboardsavedata.setScoreboard(this.worldScoreboard);
      ((ServerScoreboard)this.worldScoreboard).addDirtyRunnable(new WorldSavedDataCallableSave(scoreboardsavedata));
      this.lootTable = new LootTableManager(new File(new File(this.saveHandler.getWorldDirectory(), "data"), "loot_tables"));
      this.getWorldBorder().setCenter(this.worldInfo.getBorderCenterX(), this.worldInfo.getBorderCenterZ());
      this.getWorldBorder().setDamageAmount(this.worldInfo.getBorderDamagePerBlock());
      this.getWorldBorder().setDamageBuffer(this.worldInfo.getBorderSafeZone());
      this.getWorldBorder().setWarningDistance(this.worldInfo.getBorderWarningDistance());
      this.getWorldBorder().setWarningTime(this.worldInfo.getBorderWarningTime());
      if (this.worldInfo.getBorderLerpTime() > 0L) {
         this.getWorldBorder().setTransition(this.worldInfo.getBorderSize(), this.worldInfo.getBorderLerpTarget(), this.worldInfo.getBorderLerpTime());
      } else {
         this.getWorldBorder().setTransition(this.worldInfo.getBorderSize());
      }

      this.initCapabilities();
      return this;
   }

   public void tick() {
      super.tick();
      if (this.getWorldInfo().isHardcoreModeEnabled() && this.getDifficulty() != EnumDifficulty.HARD) {
         this.getWorldInfo().setDifficulty(EnumDifficulty.HARD);
      }

      this.provider.getBiomeProvider().cleanupCache();
      if (this.areAllPlayersAsleep()) {
         if (this.getGameRules().getBoolean("doDaylightCycle")) {
            long i = this.worldInfo.getWorldTime() + 24000L;
            this.worldInfo.setWorldTime(i - i % 24000L);
         }

         this.wakeAllPlayers();
      }

      this.theProfiler.startSection("mobSpawner");
      if (this.getGameRules().getBoolean("doMobSpawning") && this.worldInfo.getTerrainType() != WorldType.DEBUG_WORLD) {
         this.entitySpawner.findChunksForSpawning(this, this.spawnHostileMobs, this.spawnPeacefulMobs, this.worldInfo.getWorldTotalTime() % 400L == 0L);
      }

      this.theProfiler.endStartSection("chunkSource");
      this.chunkProvider.tick();
      int j = this.calculateSkylightSubtracted(1.0F);
      if (j != this.getSkylightSubtracted()) {
         this.setSkylightSubtracted(j);
      }

      this.worldInfo.setWorldTotalTime(this.worldInfo.getWorldTotalTime() + 1L);
      if (this.getGameRules().getBoolean("doDaylightCycle")) {
         this.worldInfo.setWorldTime(this.worldInfo.getWorldTime() + 1L);
      }

      this.theProfiler.endStartSection("tickPending");
      this.tickUpdates(false);
      this.theProfiler.endStartSection("tickBlocks");
      this.updateBlocks();
      this.theProfiler.endStartSection("chunkMap");
      this.playerChunkMap.tick();
      this.theProfiler.endStartSection("village");
      this.villageCollectionObj.tick();
      this.villageSiege.tick();
      this.theProfiler.endStartSection("portalForcer");
      this.worldTeleporter.removeStalePortalLocations(this.getTotalWorldTime());

      for(Teleporter tele : this.customTeleporters) {
         tele.removeStalePortalLocations(this.getTotalWorldTime());
      }

      this.theProfiler.endSection();
      this.sendQueuedBlockEvents();
   }

   @Nullable
   public Biome.SpawnListEntry getSpawnListEntryForTypeAt(EnumCreatureType var1, BlockPos var2) {
      List list = this.getChunkProvider().getPossibleCreatures(creatureType, pos);
      list = ForgeEventFactory.getPotentialSpawns(this, creatureType, pos, list);
      return list != null && !list.isEmpty() ? (Biome.SpawnListEntry)WeightedRandom.getRandomItem(this.rand, list) : null;
   }

   public boolean canCreatureTypeSpawnHere(EnumCreatureType var1, Biome.SpawnListEntry var2, BlockPos var3) {
      List list = this.getChunkProvider().getPossibleCreatures(creatureType, pos);
      list = ForgeEventFactory.getPotentialSpawns(this, creatureType, pos, list);
      return list != null && !list.isEmpty() ? list.contains(spawnListEntry) : false;
   }

   public void updateAllPlayersSleepingFlag() {
      this.allPlayersSleeping = false;
      if (!this.playerEntities.isEmpty()) {
         int i = 0;
         int j = 0;

         for(EntityPlayer entityplayer : this.playerEntities) {
            if (entityplayer.isSpectator()) {
               ++i;
            } else if (entityplayer.isPlayerSleeping()) {
               ++j;
            }
         }

         this.allPlayersSleeping = j > 0 && j >= this.playerEntities.size() - i;
      }

   }

   protected void wakeAllPlayers() {
      this.allPlayersSleeping = false;

      for(EntityPlayer entityplayer : this.playerEntities) {
         if (entityplayer.isPlayerSleeping()) {
            entityplayer.wakeUpPlayer(false, false, true);
         }
      }

      this.resetRainAndThunder();
   }

   private void resetRainAndThunder() {
      this.provider.resetRainAndThunder();
   }

   public boolean areAllPlayersAsleep() {
      if (this.allPlayersSleeping && !this.isRemote) {
         for(EntityPlayer entityplayer : this.playerEntities) {
            if (!entityplayer.isSpectator() && !entityplayer.isPlayerFullyAsleep()) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @SideOnly(Side.CLIENT)
   public void setInitialSpawnLocation() {
      if (this.worldInfo.getSpawnY() <= 0) {
         this.worldInfo.setSpawnY(this.getSeaLevel() + 1);
      }

      int i = this.worldInfo.getSpawnX();
      int j = this.worldInfo.getSpawnZ();
      int k = 0;

      while(this.getGroundAboveSeaLevel(new BlockPos(i, 0, j)).getMaterial() == Material.AIR) {
         i += this.rand.nextInt(8) - this.rand.nextInt(8);
         j += this.rand.nextInt(8) - this.rand.nextInt(8);
         ++k;
         if (k == 10000) {
            break;
         }
      }

      this.worldInfo.setSpawnX(i);
      this.worldInfo.setSpawnZ(j);
   }

   protected boolean isChunkLoaded(int var1, int var2, boolean var3) {
      return this.getChunkProvider().chunkExists(x, z);
   }

   protected void playerCheckLight() {
      this.theProfiler.startSection("playerCheckLight");
      if (!this.playerEntities.isEmpty()) {
         int i = this.rand.nextInt(this.playerEntities.size());
         EntityPlayer entityplayer = (EntityPlayer)this.playerEntities.get(i);
         int j = MathHelper.floor(entityplayer.posX) + this.rand.nextInt(11) - 5;
         int k = MathHelper.floor(entityplayer.posY) + this.rand.nextInt(11) - 5;
         int l = MathHelper.floor(entityplayer.posZ) + this.rand.nextInt(11) - 5;
         this.checkLight(new BlockPos(j, k, l));
      }

      this.theProfiler.endSection();
   }

   protected void updateBlocks() {
      this.playerCheckLight();
      if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) {
         Iterator iterator1 = this.playerChunkMap.getChunkIterator();

         while(iterator1.hasNext()) {
            ((Chunk)iterator1.next()).onTick(false);
         }
      } else {
         int i = this.getGameRules().getInt("randomTickSpeed");
         boolean flag = this.isRaining();
         boolean flag1 = this.isThundering();
         this.theProfiler.startSection("pollingChunks");

         for(Iterator iterator = this.getPersistentChunkIterable(this.playerChunkMap.getChunkIterator()); iterator.hasNext(); this.theProfiler.endSection()) {
            this.theProfiler.startSection("getChunk");
            Chunk chunk = (Chunk)iterator.next();
            int j = chunk.xPosition * 16;
            int k = chunk.zPosition * 16;
            this.theProfiler.endStartSection("checkNextLight");
            chunk.enqueueRelightChecks();
            this.theProfiler.endStartSection("tickChunk");
            chunk.onTick(false);
            this.theProfiler.endStartSection("thunder");
            if (this.provider.canDoLightning(chunk) && flag && flag1 && this.rand.nextInt(100000) == 0) {
               this.updateLCG = this.updateLCG * 3 + 1013904223;
               int l = this.updateLCG >> 2;
               BlockPos blockpos = this.adjustPosToNearbyEntity(new BlockPos(j + (l & 15), 0, k + (l >> 8 & 15)));
               if (this.isRainingAt(blockpos)) {
                  DifficultyInstance difficultyinstance = this.getDifficultyForLocation(blockpos);
                  if (this.rand.nextDouble() < (double)difficultyinstance.getAdditionalDifficulty() * 0.05D) {
                     EntityHorse entityhorse = new EntityHorse(this);
                     entityhorse.setType(HorseType.SKELETON);
                     entityhorse.setSkeletonTrap(true);
                     entityhorse.setGrowingAge(0);
                     entityhorse.setPosition((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
                     this.spawnEntity(entityhorse);
                     this.addWeatherEffect(new EntityLightningBolt(this, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), true));
                  } else {
                     this.addWeatherEffect(new EntityLightningBolt(this, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), false));
                  }
               }
            }

            this.theProfiler.endStartSection("iceandsnow");
            if (this.provider.canDoRainSnowIce(chunk) && this.rand.nextInt(16) == 0) {
               this.updateLCG = this.updateLCG * 3 + 1013904223;
               int j2 = this.updateLCG >> 2;
               BlockPos blockpos1 = this.getPrecipitationHeight(new BlockPos(j + (j2 & 15), 0, k + (j2 >> 8 & 15)));
               BlockPos blockpos2 = blockpos1.down();
               if (this.canBlockFreezeNoWater(blockpos2)) {
                  this.setBlockState(blockpos2, Blocks.ICE.getDefaultState());
               }

               if (flag && this.canSnowAt(blockpos1, true)) {
                  this.setBlockState(blockpos1, Blocks.SNOW_LAYER.getDefaultState());
               }

               if (flag && this.getBiome(blockpos2).canRain()) {
                  this.getBlockState(blockpos2).getBlock().fillWithRain(this, blockpos2);
               }
            }

            this.theProfiler.endStartSection("tickBlocks");
            if (i > 0) {
               for(ExtendedBlockStorage extendedblockstorage : chunk.getBlockStorageArray()) {
                  if (extendedblockstorage != Chunk.NULL_BLOCK_STORAGE && extendedblockstorage.getNeedsRandomTick()) {
                     for(int i1 = 0; i1 < i; ++i1) {
                        this.updateLCG = this.updateLCG * 3 + 1013904223;
                        int j1 = this.updateLCG >> 2;
                        int k1 = j1 & 15;
                        int l1 = j1 >> 8 & 15;
                        int i2 = j1 >> 16 & 15;
                        IBlockState iblockstate = extendedblockstorage.get(k1, i2, l1);
                        Block block = iblockstate.getBlock();
                        this.theProfiler.startSection("randomTick");
                        if (block.getTickRandomly()) {
                           block.randomTick(this, new BlockPos(k1 + j, i2 + extendedblockstorage.getYLocation(), l1 + k), iblockstate, this.rand);
                        }

                        this.theProfiler.endSection();
                     }
                  }
               }
            }
         }

         this.theProfiler.endSection();
      }

   }

   protected BlockPos adjustPosToNearbyEntity(BlockPos var1) {
      BlockPos blockpos = this.getPrecipitationHeight(pos);
      AxisAlignedBB axisalignedbb = (new AxisAlignedBB(blockpos, new BlockPos(blockpos.getX(), this.getHeight(), blockpos.getZ()))).expandXyz(3.0D);
      List list = this.getEntitiesWithinAABB(EntityLivingBase.class, axisalignedbb, new Predicate() {
         public boolean apply(@Nullable EntityLivingBase var1) {
            return p_apply_1_ != null && p_apply_1_.isEntityAlive() && WorldServer.this.canSeeSky(p_apply_1_.getPosition());
         }
      });
      if (!list.isEmpty()) {
         return ((EntityLivingBase)list.get(this.rand.nextInt(list.size()))).getPosition();
      } else {
         if (blockpos.getY() == -1) {
            blockpos = blockpos.up(2);
         }

         return blockpos;
      }
   }

   public boolean isBlockTickPending(BlockPos var1, Block var2) {
      NextTickListEntry nextticklistentry = new NextTickListEntry(pos, blockType);
      return this.pendingTickListEntriesThisTick.contains(nextticklistentry);
   }

   public boolean isUpdateScheduled(BlockPos var1, Block var2) {
      NextTickListEntry nextticklistentry = new NextTickListEntry(pos, blk);
      return this.pendingTickListEntriesHashSet.contains(nextticklistentry);
   }

   public void scheduleUpdate(BlockPos var1, Block var2, int var3) {
      this.updateBlockTick(pos, blockIn, delay, 0);
   }

   public void updateBlockTick(BlockPos var1, Block var2, int var3, int var4) {
      if (pos instanceof BlockPos.MutableBlockPos || pos instanceof BlockPos.PooledMutableBlockPos) {
         pos = new BlockPos(pos);
         LogManager.getLogger().warn("Tried to assign a mutable BlockPos to tick data...", new Error(pos.getClass().toString()));
      }

      Material material = blockIn.getDefaultState().getMaterial();
      if (this.scheduledUpdatesAreImmediate && material != Material.AIR) {
         if (blockIn.requiresUpdates()) {
            boolean isForced = this.getPersistentChunks().containsKey(new ChunkPos(pos));
            int range = isForced ? 0 : 8;
            if (this.isAreaLoaded(pos.add(-range, -range, -range), pos.add(range, range, range))) {
               IBlockState iblockstate = this.getBlockState(pos);
               if (iblockstate.getMaterial() != Material.AIR && iblockstate.getBlock() == blockIn) {
                  iblockstate.getBlock().updateTick(this, pos, iblockstate, this.rand);
               }
            }

            return;
         }

         delay = 1;
      }

      NextTickListEntry nextticklistentry = new NextTickListEntry(pos, blockIn);
      if (this.isBlockLoaded(pos)) {
         if (material != Material.AIR) {
            nextticklistentry.setScheduledTime((long)delay + this.worldInfo.getWorldTotalTime());
            nextticklistentry.setPriority(priority);
         }

         if (!this.pendingTickListEntriesHashSet.contains(nextticklistentry)) {
            this.pendingTickListEntriesHashSet.add(nextticklistentry);
            this.pendingTickListEntriesTreeSet.add(nextticklistentry);
         }
      }

   }

   public void scheduleBlockUpdate(BlockPos var1, Block var2, int var3, int var4) {
      if (blockIn != null) {
         if (pos instanceof BlockPos.MutableBlockPos || pos instanceof BlockPos.PooledMutableBlockPos) {
            pos = new BlockPos(pos);
            LogManager.getLogger().warn("Tried to assign a mutable BlockPos to tick data...", new Error(pos.getClass().toString()));
         }

         NextTickListEntry nextticklistentry = new NextTickListEntry(pos, blockIn);
         nextticklistentry.setPriority(priority);
         Material material = blockIn.getDefaultState().getMaterial();
         if (material != Material.AIR) {
            nextticklistentry.setScheduledTime((long)delay + this.worldInfo.getWorldTotalTime());
         }

         if (!this.pendingTickListEntriesHashSet.contains(nextticklistentry)) {
            this.pendingTickListEntriesHashSet.add(nextticklistentry);
            this.pendingTickListEntriesTreeSet.add(nextticklistentry);
         }

      }
   }

   public void updateEntities() {
      if (this.playerEntities.isEmpty() && this.getPersistentChunks().isEmpty()) {
         if (this.updateEntityTick++ >= 300) {
            return;
         }
      } else {
         this.resetUpdateEntityTick();
      }

      this.provider.onWorldUpdateEntities();
      super.updateEntities();
   }

   protected void tickPlayers() {
      super.tickPlayers();
      this.theProfiler.endStartSection("players");

      for(int i = 0; i < this.playerEntities.size(); ++i) {
         Entity entity = (Entity)this.playerEntities.get(i);
         Entity entity1 = entity.getRidingEntity();
         if (entity1 != null) {
            if (!entity1.isDead && entity1.isPassenger(entity)) {
               continue;
            }

            entity.dismountRidingEntity();
         }

         this.theProfiler.startSection("tick");
         if (!entity.isDead) {
            try {
               this.updateEntity(entity);
            } catch (Throwable var7) {
               CrashReport crashreport = CrashReport.makeCrashReport(var7, "Ticking player");
               CrashReportCategory crashreportcategory = crashreport.makeCategory("Player being ticked");
               entity.addEntityCrashInfo(crashreportcategory);
               throw new ReportedException(crashreport);
            }
         }

         this.theProfiler.endSection();
         this.theProfiler.startSection("remove");
         if (entity.isDead) {
            int j = entity.chunkCoordX;
            int k = entity.chunkCoordZ;
            if (entity.addedToChunk && this.isChunkLoaded(j, k, true)) {
               this.getChunkFromChunkCoords(j, k).removeEntity(entity);
            }

            this.loadedEntityList.remove(entity);
            this.onEntityRemoved(entity);
         }

         this.theProfiler.endSection();
      }

   }

   public void resetUpdateEntityTick() {
      this.updateEntityTick = 0;
   }

   public boolean tickUpdates(boolean var1) {
      if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) {
         return false;
      } else {
         int i = this.pendingTickListEntriesTreeSet.size();
         if (i != this.pendingTickListEntriesHashSet.size()) {
            throw new IllegalStateException("TickNextTick list out of synch");
         } else {
            if (i > 65536) {
               i = 65536;
            }

            this.theProfiler.startSection("cleaning");

            for(int j = 0; j < i; ++j) {
               NextTickListEntry nextticklistentry = (NextTickListEntry)this.pendingTickListEntriesTreeSet.first();
               if (!p_72955_1_ && nextticklistentry.scheduledTime > this.worldInfo.getWorldTotalTime()) {
                  break;
               }

               this.pendingTickListEntriesTreeSet.remove(nextticklistentry);
               this.pendingTickListEntriesHashSet.remove(nextticklistentry);
               this.pendingTickListEntriesThisTick.add(nextticklistentry);
            }

            this.theProfiler.endSection();
            this.theProfiler.startSection("ticking");
            Iterator iterator = this.pendingTickListEntriesThisTick.iterator();

            while(iterator.hasNext()) {
               NextTickListEntry nextticklistentry1 = (NextTickListEntry)iterator.next();
               iterator.remove();
               int k = 0;
               if (this.isAreaLoaded(nextticklistentry1.position.add(0, 0, 0), nextticklistentry1.position.add(0, 0, 0))) {
                  IBlockState iblockstate = this.getBlockState(nextticklistentry1.position);
                  if (iblockstate.getMaterial() != Material.AIR && Block.isEqualTo(iblockstate.getBlock(), nextticklistentry1.getBlock())) {
                     try {
                        iblockstate.getBlock().updateTick(this, nextticklistentry1.position, iblockstate, this.rand);
                     } catch (Throwable var10) {
                        CrashReport crashreport = CrashReport.makeCrashReport(var10, "Exception while ticking a block");
                        CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being ticked");
                        CrashReportCategory.addBlockInfo(crashreportcategory, nextticklistentry1.position, iblockstate);
                        throw new ReportedException(crashreport);
                     }
                  }
               } else {
                  this.scheduleUpdate(nextticklistentry1.position, nextticklistentry1.getBlock(), 0);
               }
            }

            this.theProfiler.endSection();
            this.pendingTickListEntriesThisTick.clear();
            return !this.pendingTickListEntriesTreeSet.isEmpty();
         }
      }
   }

   @Nullable
   public List getPendingBlockUpdates(Chunk var1, boolean var2) {
      ChunkPos chunkpos = chunkIn.getChunkCoordIntPair();
      int i = (chunkpos.chunkXPos << 4) - 2;
      int j = i + 16 + 2;
      int k = (chunkpos.chunkZPos << 4) - 2;
      int l = k + 16 + 2;
      return this.getPendingBlockUpdates(new StructureBoundingBox(i, 0, k, j, 256, l), p_72920_2_);
   }

   @Nullable
   public List getPendingBlockUpdates(StructureBoundingBox var1, boolean var2) {
      List list = null;

      for(int i = 0; i < 2; ++i) {
         Iterator iterator;
         if (i == 0) {
            iterator = this.pendingTickListEntriesTreeSet.iterator();
         } else {
            iterator = this.pendingTickListEntriesThisTick.iterator();
         }

         while(iterator.hasNext()) {
            NextTickListEntry nextticklistentry = (NextTickListEntry)iterator.next();
            BlockPos blockpos = nextticklistentry.position;
            if (blockpos.getX() >= structureBB.minX && blockpos.getX() < structureBB.maxX && blockpos.getZ() >= structureBB.minZ && blockpos.getZ() < structureBB.maxZ) {
               if (p_175712_2_) {
                  if (i == 0) {
                     this.pendingTickListEntriesHashSet.remove(nextticklistentry);
                  }

                  iterator.remove();
               }

               if (list == null) {
                  list = Lists.newArrayList();
               }

               list.add(nextticklistentry);
            }
         }
      }

      return list;
   }

   public void updateEntityWithOptionalForce(Entity var1, boolean var2) {
      if (!this.canSpawnAnimals() && (entityIn instanceof EntityAnimal || entityIn instanceof EntityWaterMob)) {
         entityIn.setDead();
      }

      if (!this.canSpawnNPCs() && entityIn instanceof INpc) {
         entityIn.setDead();
      }

      super.updateEntityWithOptionalForce(entityIn, forceUpdate);
   }

   private boolean canSpawnNPCs() {
      return this.mcServer.getCanSpawnNPCs();
   }

   private boolean canSpawnAnimals() {
      return this.mcServer.getCanSpawnAnimals();
   }

   protected IChunkProvider createChunkProvider() {
      IChunkLoader ichunkloader = this.saveHandler.getChunkLoader(this.provider);
      return new ChunkProviderServer(this, ichunkloader, this.provider.createChunkGenerator());
   }

   public boolean isBlockModifiable(EntityPlayer var1, BlockPos var2) {
      return super.isBlockModifiable(player, pos);
   }

   public boolean canMineBlockBody(EntityPlayer var1, BlockPos var2) {
      return !this.mcServer.isBlockProtected(this, pos, player) && this.getWorldBorder().contains(pos);
   }

   public void initialize(WorldSettings var1) {
      if (!this.worldInfo.isInitialized()) {
         try {
            this.createSpawnPosition(settings);
            if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) {
               this.setDebugWorldSettings();
            }

            super.initialize(settings);
         } catch (Throwable var6) {
            CrashReport crashreport = CrashReport.makeCrashReport(var6, "Exception initializing level");

            try {
               this.addWorldInfoToCrashReport(crashreport);
            } catch (Throwable var5) {
               ;
            }

            throw new ReportedException(crashreport);
         }

         this.worldInfo.setServerInitialized(true);
      }

   }

   private void setDebugWorldSettings() {
      this.worldInfo.setMapFeaturesEnabled(false);
      this.worldInfo.setAllowCommands(true);
      this.worldInfo.setRaining(false);
      this.worldInfo.setThundering(false);
      this.worldInfo.setCleanWeatherTime(1000000000);
      this.worldInfo.setWorldTime(6000L);
      this.worldInfo.setGameType(GameType.SPECTATOR);
      this.worldInfo.setHardcore(false);
      this.worldInfo.setDifficulty(EnumDifficulty.PEACEFUL);
      this.worldInfo.setDifficultyLocked(true);
      this.getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
   }

   private void createSpawnPosition(WorldSettings var1) {
      if (!this.provider.canRespawnHere()) {
         this.worldInfo.setSpawn(BlockPos.ORIGIN.up(this.provider.getAverageGroundLevel()));
      } else if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) {
         this.worldInfo.setSpawn(BlockPos.ORIGIN.up());
      } else {
         if (ForgeEventFactory.onCreateWorldSpawn(this, settings)) {
            return;
         }

         this.findingSpawnPoint = true;
         BiomeProvider biomeprovider = this.provider.getBiomeProvider();
         List list = biomeprovider.getBiomesToSpawnIn();
         Random random = new Random(this.getSeed());
         BlockPos blockpos = biomeprovider.findBiomePosition(0, 0, 256, list, random);
         int i = 8;
         int j = this.provider.getAverageGroundLevel();
         int k = 8;
         if (blockpos != null) {
            i = blockpos.getX();
            k = blockpos.getZ();
         } else {
            LOGGER.warn("Unable to find spawn biome");
         }

         int l = 0;

         while(!this.provider.canCoordinateBeSpawn(i, k)) {
            i += random.nextInt(64) - random.nextInt(64);
            k += random.nextInt(64) - random.nextInt(64);
            ++l;
            if (l == 1000) {
               break;
            }
         }

         this.worldInfo.setSpawn(new BlockPos(i, j, k));
         this.findingSpawnPoint = false;
         if (settings.isBonusChestEnabled()) {
            this.createBonusChest();
         }
      }

   }

   protected void createBonusChest() {
      WorldGeneratorBonusChest worldgeneratorbonuschest = new WorldGeneratorBonusChest();

      for(int i = 0; i < 10; ++i) {
         int j = this.worldInfo.getSpawnX() + this.rand.nextInt(6) - this.rand.nextInt(6);
         int k = this.worldInfo.getSpawnZ() + this.rand.nextInt(6) - this.rand.nextInt(6);
         BlockPos blockpos = this.getTopSolidOrLiquidBlock(new BlockPos(j, 0, k)).up();
         if (worldgeneratorbonuschest.generate(this, this.rand, blockpos)) {
            break;
         }
      }

   }

   public BlockPos getSpawnCoordinate() {
      return this.provider.getSpawnCoordinate();
   }

   public void saveAllChunks(boolean var1, @Nullable IProgressUpdate var2) throws MinecraftException {
      ChunkProviderServer chunkproviderserver = this.getChunkProvider();
      if (chunkproviderserver.canSave()) {
         if (progressCallback != null) {
            progressCallback.displaySavingString("Saving level");
         }

         this.saveLevel();
         if (progressCallback != null) {
            progressCallback.displayLoadingString("Saving chunks");
         }

         chunkproviderserver.saveChunks(p_73044_1_);
         MinecraftForge.EVENT_BUS.post(new Save(this));

         for(Chunk chunk : Lists.newArrayList(chunkproviderserver.getLoadedChunks())) {
            if (chunk != null && !this.playerChunkMap.contains(chunk.xPosition, chunk.zPosition)) {
               chunkproviderserver.unload(chunk);
            }
         }
      }

   }

   public void saveChunkData() {
      ChunkProviderServer chunkproviderserver = this.getChunkProvider();
      if (chunkproviderserver.canSave()) {
         chunkproviderserver.saveExtraData();
      }

   }

   protected void saveLevel() throws MinecraftException {
      this.checkSessionLock();

      for(WorldServer worldserver : this.mcServer.worlds) {
         if (worldserver instanceof WorldServerMulti) {
            ((WorldServerMulti)worldserver).saveAdditionalData();
         }
      }

      this.worldInfo.setBorderSize(this.getWorldBorder().getDiameter());
      this.worldInfo.getBorderCenterX(this.getWorldBorder().getCenterX());
      this.worldInfo.getBorderCenterZ(this.getWorldBorder().getCenterZ());
      this.worldInfo.setBorderSafeZone(this.getWorldBorder().getDamageBuffer());
      this.worldInfo.setBorderDamagePerBlock(this.getWorldBorder().getDamageAmount());
      this.worldInfo.setBorderWarningDistance(this.getWorldBorder().getWarningDistance());
      this.worldInfo.setBorderWarningTime(this.getWorldBorder().getWarningTime());
      this.worldInfo.setBorderLerpTarget(this.getWorldBorder().getTargetSize());
      this.worldInfo.setBorderLerpTime(this.getWorldBorder().getTimeUntilTarget());
      this.saveHandler.saveWorldInfoWithPlayer(this.worldInfo, this.mcServer.getPlayerList().getHostPlayerData());
      this.mapStorage.saveAllData();
      this.perWorldStorage.saveAllData();
   }

   public boolean spawnEntity(Entity var1) {
      return this.canAddEntity(entityIn) ? super.spawnEntity(entityIn) : false;
   }

   public void loadEntities(Collection var1) {
      for(Entity entity : Lists.newArrayList(entityCollection)) {
         if (this.canAddEntity(entity) && !MinecraftForge.EVENT_BUS.post(new EntityJoinWorldEvent(entity, this))) {
            this.loadedEntityList.add(entity);
            this.onEntityAdded(entity);
         }
      }

   }

   private boolean canAddEntity(Entity var1) {
      if (entityIn.isDead) {
         LOGGER.warn("Tried to add entity {} but it was marked as removed already", new Object[]{EntityList.getEntityString(entityIn)});
         return false;
      } else {
         UUID uuid = entityIn.getUniqueID();
         if (this.entitiesByUuid.containsKey(uuid)) {
            Entity entity = (Entity)this.entitiesByUuid.get(uuid);
            if (this.unloadedEntityList.contains(entity)) {
               this.unloadedEntityList.remove(entity);
            } else {
               if (!(entityIn instanceof EntityPlayer)) {
                  LOGGER.warn("Keeping entity {} that already exists with UUID {}", new Object[]{EntityList.getEntityString(entity), uuid.toString()});
                  return false;
               }

               LOGGER.warn("Force-added player with duplicate UUID {}", new Object[]{uuid.toString()});
            }

            this.removeEntityDangerously(entity);
         }

         return true;
      }
   }

   public void onEntityAdded(Entity var1) {
      super.onEntityAdded(entityIn);
      this.entitiesById.addKey(entityIn.getEntityId(), entityIn);
      this.entitiesByUuid.put(entityIn.getUniqueID(), entityIn);
      Entity[] aentity = entityIn.getParts();
      if (aentity != null) {
         for(Entity entity : aentity) {
            this.entitiesById.addKey(entity.getEntityId(), entity);
         }
      }

   }

   public void onEntityRemoved(Entity var1) {
      super.onEntityRemoved(entityIn);
      this.entitiesById.removeObject(entityIn.getEntityId());
      this.entitiesByUuid.remove(entityIn.getUniqueID());
      Entity[] aentity = entityIn.getParts();
      if (aentity != null) {
         for(Entity entity : aentity) {
            this.entitiesById.removeObject(entity.getEntityId());
         }
      }

   }

   public boolean addWeatherEffect(Entity var1) {
      if (super.addWeatherEffect(entityIn)) {
         this.mcServer.getPlayerList().sendToAllNearExcept((EntityPlayer)null, entityIn.posX, entityIn.posY, entityIn.posZ, 512.0D, this.provider.getDimension(), new SPacketSpawnGlobalEntity(entityIn));
         return true;
      } else {
         return false;
      }
   }

   public void setEntityState(Entity var1, byte var2) {
      this.getEntityTracker().sendToTrackingAndSelf(entityIn, new SPacketEntityStatus(entityIn, state));
   }

   public ChunkProviderServer getChunkProvider() {
      return (ChunkProviderServer)super.getChunkProvider();
   }

   public Explosion newExplosion(@Nullable Entity var1, double var2, double var4, double var6, float var8, boolean var9, boolean var10) {
      Explosion explosion = new Explosion(this, entityIn, x, y, z, strength, isFlaming, isSmoking);
      if (ForgeEventFactory.onExplosionStart(this, explosion)) {
         return explosion;
      } else {
         explosion.doExplosionA();
         explosion.doExplosionB(false);
         if (!isSmoking) {
            explosion.clearAffectedBlockPositions();
         }

         for(EntityPlayer entityplayer : this.playerEntities) {
            if (entityplayer.getDistanceSq(x, y, z) < 4096.0D) {
               ((EntityPlayerMP)entityplayer).connection.sendPacket(new SPacketExplosion(x, y, z, strength, explosion.getAffectedBlockPositions(), (Vec3d)explosion.getPlayerKnockbackMap().get(entityplayer)));
            }
         }

         return explosion;
      }
   }

   public void addBlockEvent(BlockPos var1, Block var2, int var3, int var4) {
      BlockEventData blockeventdata = new BlockEventData(pos, blockIn, eventID, eventParam);

      for(BlockEventData blockeventdata1 : this.blockEventQueue[this.blockEventCacheIndex]) {
         if (blockeventdata1.equals(blockeventdata)) {
            return;
         }
      }

      this.blockEventQueue[this.blockEventCacheIndex].add(blockeventdata);
   }

   private void sendQueuedBlockEvents() {
      while(!this.blockEventQueue[this.blockEventCacheIndex].isEmpty()) {
         int i = this.blockEventCacheIndex;
         this.blockEventCacheIndex ^= 1;

         for(BlockEventData blockeventdata : this.blockEventQueue[i]) {
            if (this.fireBlockEvent(blockeventdata)) {
               this.mcServer.getPlayerList().sendToAllNearExcept((EntityPlayer)null, (double)blockeventdata.getPosition().getX(), (double)blockeventdata.getPosition().getY(), (double)blockeventdata.getPosition().getZ(), 64.0D, this.provider.getDimension(), new SPacketBlockAction(blockeventdata.getPosition(), blockeventdata.getBlock(), blockeventdata.getEventID(), blockeventdata.getEventParameter()));
            }
         }

         this.blockEventQueue[i].clear();
      }

   }

   private boolean fireBlockEvent(BlockEventData var1) {
      IBlockState iblockstate = this.getBlockState(event.getPosition());
      return iblockstate.getBlock() == event.getBlock() ? iblockstate.onBlockEventReceived(this, event.getPosition(), event.getEventID(), event.getEventParameter()) : false;
   }

   public void flush() {
      this.saveHandler.flush();
   }

   protected void updateWeather() {
      boolean flag = this.isRaining();
      super.updateWeather();
      if (this.prevRainingStrength != this.rainingStrength) {
         this.mcServer.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketChangeGameState(7, this.rainingStrength), this.provider.getDimension());
      }

      if (this.prevThunderingStrength != this.thunderingStrength) {
         this.mcServer.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketChangeGameState(8, this.thunderingStrength), this.provider.getDimension());
      }

      if (flag != this.isRaining()) {
         if (flag) {
            this.mcServer.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketChangeGameState(2, 0.0F), this.provider.getDimension());
         } else {
            this.mcServer.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketChangeGameState(1, 0.0F), this.provider.getDimension());
         }

         this.mcServer.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketChangeGameState(7, this.rainingStrength), this.provider.getDimension());
         this.mcServer.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketChangeGameState(8, this.thunderingStrength), this.provider.getDimension());
      }

   }

   @Nullable
   public MinecraftServer getMinecraftServer() {
      return this.mcServer;
   }

   public EntityTracker getEntityTracker() {
      return this.theEntityTracker;
   }

   public PlayerChunkMap getPlayerChunkMap() {
      return this.playerChunkMap;
   }

   public Teleporter getDefaultTeleporter() {
      return this.worldTeleporter;
   }

   public TemplateManager getStructureTemplateManager() {
      return this.saveHandler.getStructureTemplateManager();
   }

   public void spawnParticle(EnumParticleTypes var1, double var2, double var4, double var6, int var8, double var9, double var11, double var13, double var15, int... var17) {
      this.spawnParticle(particleType, false, xCoord, yCoord, zCoord, numberOfParticles, xOffset, yOffset, zOffset, particleSpeed, particleArguments);
   }

   public void spawnParticle(EnumParticleTypes var1, boolean var2, double var3, double var5, double var7, int var9, double var10, double var12, double var14, double var16, int... var18) {
      SPacketParticles spacketparticles = new SPacketParticles(particleType, longDistance, (float)xCoord, (float)yCoord, (float)zCoord, (float)xOffset, (float)yOffset, (float)zOffset, (float)particleSpeed, numberOfParticles, particleArguments);

      for(int i = 0; i < this.playerEntities.size(); ++i) {
         EntityPlayerMP entityplayermp = (EntityPlayerMP)this.playerEntities.get(i);
         this.sendPacketWithinDistance(entityplayermp, longDistance, xCoord, yCoord, zCoord, spacketparticles);
      }

   }

   public void spawnParticle(EntityPlayerMP var1, EnumParticleTypes var2, boolean var3, double var4, double var6, double var8, int var10, double var11, double var13, double var15, double var17, int... var19) {
      Packet packet = new SPacketParticles(particle, longDistance, (float)x, (float)y, (float)z, (float)xOffset, (float)yOffset, (float)zOffset, (float)speed, count, arguments);
      this.sendPacketWithinDistance(player, longDistance, x, y, z, packet);
   }

   private void sendPacketWithinDistance(EntityPlayerMP var1, boolean var2, double var3, double var5, double var7, Packet var9) {
      BlockPos blockpos = player.getPosition();
      double d0 = blockpos.distanceSq(x, y, z);
      if (d0 <= 1024.0D || longDistance && d0 <= 262144.0D) {
         player.connection.sendPacket(packetIn);
      }

   }

   @Nullable
   public Entity getEntityFromUuid(UUID var1) {
      return (Entity)this.entitiesByUuid.get(uuid);
   }

   public ListenableFuture addScheduledTask(Runnable var1) {
      return this.mcServer.addScheduledTask(runnableToSchedule);
   }

   public boolean isCallingFromMinecraftThread() {
      return this.mcServer.isCallingFromMinecraftThread();
   }

   public File getChunkSaveLocation() {
      return ((AnvilChunkLoader)this.getChunkProvider().chunkLoader).chunkSaveLocation;
   }

   static class ServerBlockEventList extends ArrayList {
      private ServerBlockEventList() {
      }
   }
}
