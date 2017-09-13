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
      super(var2, var3, DimensionManager.createProviderFor(var4), var5, false);
      this.mcServer = var1;
      this.theEntityTracker = new EntityTracker(this);
      this.playerChunkMap = new PlayerChunkMap(this);
      int var6 = this.provider.getDimension();
      this.provider.registerWorld(this);
      this.provider.setDimension(var6);
      this.chunkProvider = this.createChunkProvider();
      this.perWorldStorage = new MapStorage(new WorldSpecificSaveHandler(this, var2));
      this.worldTeleporter = new Teleporter(this);
      this.calculateInitialSkylight();
      this.calculateInitialWeather();
      this.getWorldBorder().setSize(var1.getMaxWorldSize());
      DimensionManager.setWorld(var4, this, this.mcServer);
   }

   public World init() {
      this.mapStorage = new MapStorage(this.saveHandler);
      String var1 = VillageCollection.fileNameForProvider(this.provider);
      VillageCollection var2 = (VillageCollection)this.perWorldStorage.getOrLoadData(VillageCollection.class, var1);
      if (var2 == null) {
         this.villageCollectionObj = new VillageCollection(this);
         this.perWorldStorage.setData(var1, this.villageCollectionObj);
      } else {
         this.villageCollectionObj = var2;
         this.villageCollectionObj.setWorldsForAll(this);
      }

      this.worldScoreboard = new ServerScoreboard(this.mcServer);
      ScoreboardSaveData var3 = (ScoreboardSaveData)this.mapStorage.getOrLoadData(ScoreboardSaveData.class, "scoreboard");
      if (var3 == null) {
         var3 = new ScoreboardSaveData();
         this.mapStorage.setData("scoreboard", var3);
      }

      var3.setScoreboard(this.worldScoreboard);
      ((ServerScoreboard)this.worldScoreboard).addDirtyRunnable(new WorldSavedDataCallableSave(var3));
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
            long var1 = this.worldInfo.getWorldTime() + 24000L;
            this.worldInfo.setWorldTime(var1 - var1 % 24000L);
         }

         this.wakeAllPlayers();
      }

      this.theProfiler.startSection("mobSpawner");
      if (this.getGameRules().getBoolean("doMobSpawning") && this.worldInfo.getTerrainType() != WorldType.DEBUG_WORLD) {
         this.entitySpawner.findChunksForSpawning(this, this.spawnHostileMobs, this.spawnPeacefulMobs, this.worldInfo.getWorldTotalTime() % 400L == 0L);
      }

      this.theProfiler.endStartSection("chunkSource");
      this.chunkProvider.tick();
      int var4 = this.calculateSkylightSubtracted(1.0F);
      if (var4 != this.getSkylightSubtracted()) {
         this.setSkylightSubtracted(var4);
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

      for(Teleporter var3 : this.customTeleporters) {
         var3.removeStalePortalLocations(this.getTotalWorldTime());
      }

      this.theProfiler.endSection();
      this.sendQueuedBlockEvents();
   }

   @Nullable
   public Biome.SpawnListEntry getSpawnListEntryForTypeAt(EnumCreatureType var1, BlockPos var2) {
      List var3 = this.getChunkProvider().getPossibleCreatures(var1, var2);
      var3 = ForgeEventFactory.getPotentialSpawns(this, var1, var2, var3);
      return var3 != null && !var3.isEmpty() ? (Biome.SpawnListEntry)WeightedRandom.getRandomItem(this.rand, var3) : null;
   }

   public boolean canCreatureTypeSpawnHere(EnumCreatureType var1, Biome.SpawnListEntry var2, BlockPos var3) {
      List var4 = this.getChunkProvider().getPossibleCreatures(var1, var3);
      var4 = ForgeEventFactory.getPotentialSpawns(this, var1, var3, var4);
      return var4 != null && !var4.isEmpty() ? var4.contains(var2) : false;
   }

   public void updateAllPlayersSleepingFlag() {
      this.allPlayersSleeping = false;
      if (!this.playerEntities.isEmpty()) {
         int var1 = 0;
         int var2 = 0;

         for(EntityPlayer var4 : this.playerEntities) {
            if (var4.isSpectator()) {
               ++var1;
            } else if (var4.isPlayerSleeping()) {
               ++var2;
            }
         }

         this.allPlayersSleeping = var2 > 0 && var2 >= this.playerEntities.size() - var1;
      }

   }

   protected void wakeAllPlayers() {
      this.allPlayersSleeping = false;

      for(EntityPlayer var2 : this.playerEntities) {
         if (var2.isPlayerSleeping()) {
            var2.wakeUpPlayer(false, false, true);
         }
      }

      this.resetRainAndThunder();
   }

   private void resetRainAndThunder() {
      this.provider.resetRainAndThunder();
   }

   public boolean areAllPlayersAsleep() {
      if (this.allPlayersSleeping && !this.isRemote) {
         for(EntityPlayer var2 : this.playerEntities) {
            if (!var2.isSpectator() && !var2.isPlayerFullyAsleep()) {
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

      int var1 = this.worldInfo.getSpawnX();
      int var2 = this.worldInfo.getSpawnZ();
      int var3 = 0;

      while(this.getGroundAboveSeaLevel(new BlockPos(var1, 0, var2)).getMaterial() == Material.AIR) {
         var1 += this.rand.nextInt(8) - this.rand.nextInt(8);
         var2 += this.rand.nextInt(8) - this.rand.nextInt(8);
         ++var3;
         if (var3 == 10000) {
            break;
         }
      }

      this.worldInfo.setSpawnX(var1);
      this.worldInfo.setSpawnZ(var2);
   }

   protected boolean isChunkLoaded(int var1, int var2, boolean var3) {
      return this.getChunkProvider().chunkExists(var1, var2);
   }

   protected void playerCheckLight() {
      this.theProfiler.startSection("playerCheckLight");
      if (!this.playerEntities.isEmpty()) {
         int var1 = this.rand.nextInt(this.playerEntities.size());
         EntityPlayer var2 = (EntityPlayer)this.playerEntities.get(var1);
         int var3 = MathHelper.floor(var2.posX) + this.rand.nextInt(11) - 5;
         int var4 = MathHelper.floor(var2.posY) + this.rand.nextInt(11) - 5;
         int var5 = MathHelper.floor(var2.posZ) + this.rand.nextInt(11) - 5;
         this.checkLight(new BlockPos(var3, var4, var5));
      }

      this.theProfiler.endSection();
   }

   protected void updateBlocks() {
      this.playerCheckLight();
      if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) {
         Iterator var1 = this.playerChunkMap.getChunkIterator();

         while(var1.hasNext()) {
            ((Chunk)var1.next()).onTick(false);
         }
      } else {
         int var19 = this.getGameRules().getInt("randomTickSpeed");
         boolean var2 = this.isRaining();
         boolean var3 = this.isThundering();
         this.theProfiler.startSection("pollingChunks");

         for(Iterator var4 = this.getPersistentChunkIterable(this.playerChunkMap.getChunkIterator()); var4.hasNext(); this.theProfiler.endSection()) {
            this.theProfiler.startSection("getChunk");
            Chunk var5 = (Chunk)var4.next();
            int var6 = var5.xPosition * 16;
            int var7 = var5.zPosition * 16;
            this.theProfiler.endStartSection("checkNextLight");
            var5.enqueueRelightChecks();
            this.theProfiler.endStartSection("tickChunk");
            var5.onTick(false);
            this.theProfiler.endStartSection("thunder");
            if (this.provider.canDoLightning(var5) && var2 && var3 && this.rand.nextInt(100000) == 0) {
               this.updateLCG = this.updateLCG * 3 + 1013904223;
               int var8 = this.updateLCG >> 2;
               BlockPos var9 = this.adjustPosToNearbyEntity(new BlockPos(var6 + (var8 & 15), 0, var7 + (var8 >> 8 & 15)));
               if (this.isRainingAt(var9)) {
                  DifficultyInstance var10 = this.getDifficultyForLocation(var9);
                  if (this.rand.nextDouble() < (double)var10.getAdditionalDifficulty() * 0.05D) {
                     EntityHorse var11 = new EntityHorse(this);
                     var11.setType(HorseType.SKELETON);
                     var11.setSkeletonTrap(true);
                     var11.setGrowingAge(0);
                     var11.setPosition((double)var9.getX(), (double)var9.getY(), (double)var9.getZ());
                     this.spawnEntity(var11);
                     this.addWeatherEffect(new EntityLightningBolt(this, (double)var9.getX(), (double)var9.getY(), (double)var9.getZ(), true));
                  } else {
                     this.addWeatherEffect(new EntityLightningBolt(this, (double)var9.getX(), (double)var9.getY(), (double)var9.getZ(), false));
                  }
               }
            }

            this.theProfiler.endStartSection("iceandsnow");
            if (this.provider.canDoRainSnowIce(var5) && this.rand.nextInt(16) == 0) {
               this.updateLCG = this.updateLCG * 3 + 1013904223;
               int var20 = this.updateLCG >> 2;
               BlockPos var22 = this.getPrecipitationHeight(new BlockPos(var6 + (var20 & 15), 0, var7 + (var20 >> 8 & 15)));
               BlockPos var24 = var22.down();
               if (this.canBlockFreezeNoWater(var24)) {
                  this.setBlockState(var24, Blocks.ICE.getDefaultState());
               }

               if (var2 && this.canSnowAt(var22, true)) {
                  this.setBlockState(var22, Blocks.SNOW_LAYER.getDefaultState());
               }

               if (var2 && this.getBiome(var24).canRain()) {
                  this.getBlockState(var24).getBlock().fillWithRain(this, var24);
               }
            }

            this.theProfiler.endStartSection("tickBlocks");
            if (var19 > 0) {
               for(ExtendedBlockStorage var26 : var5.getBlockStorageArray()) {
                  if (var26 != Chunk.NULL_BLOCK_STORAGE && var26.getNeedsRandomTick()) {
                     for(int var12 = 0; var12 < var19; ++var12) {
                        this.updateLCG = this.updateLCG * 3 + 1013904223;
                        int var13 = this.updateLCG >> 2;
                        int var14 = var13 & 15;
                        int var15 = var13 >> 8 & 15;
                        int var16 = var13 >> 16 & 15;
                        IBlockState var17 = var26.get(var14, var16, var15);
                        Block var18 = var17.getBlock();
                        this.theProfiler.startSection("randomTick");
                        if (var18.getTickRandomly()) {
                           var18.randomTick(this, new BlockPos(var14 + var6, var16 + var26.getYLocation(), var15 + var7), var17, this.rand);
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
      BlockPos var2 = this.getPrecipitationHeight(var1);
      AxisAlignedBB var3 = (new AxisAlignedBB(var2, new BlockPos(var2.getX(), this.getHeight(), var2.getZ()))).expandXyz(3.0D);
      List var4 = this.getEntitiesWithinAABB(EntityLivingBase.class, var3, new Predicate() {
         public boolean apply(@Nullable EntityLivingBase var1) {
            return var1 != null && var1.isEntityAlive() && WorldServer.this.canSeeSky(var1.getPosition());
         }
      });
      if (!var4.isEmpty()) {
         return ((EntityLivingBase)var4.get(this.rand.nextInt(var4.size()))).getPosition();
      } else {
         if (var2.getY() == -1) {
            var2 = var2.up(2);
         }

         return var2;
      }
   }

   public boolean isBlockTickPending(BlockPos var1, Block var2) {
      NextTickListEntry var3 = new NextTickListEntry(var1, var2);
      return this.pendingTickListEntriesThisTick.contains(var3);
   }

   public boolean isUpdateScheduled(BlockPos var1, Block var2) {
      NextTickListEntry var3 = new NextTickListEntry(var1, var2);
      return this.pendingTickListEntriesHashSet.contains(var3);
   }

   public void scheduleUpdate(BlockPos var1, Block var2, int var3) {
      this.updateBlockTick(var1, var2, var3, 0);
   }

   public void updateBlockTick(BlockPos var1, Block var2, int var3, int var4) {
      if (var1 instanceof BlockPos.MutableBlockPos || var1 instanceof BlockPos.PooledMutableBlockPos) {
         var1 = new BlockPos(var1);
         LogManager.getLogger().warn("Tried to assign a mutable BlockPos to tick data...", new Error(var1.getClass().toString()));
      }

      Material var5 = var2.getDefaultState().getMaterial();
      if (this.scheduledUpdatesAreImmediate && var5 != Material.AIR) {
         if (var2.requiresUpdates()) {
            boolean var9 = this.getPersistentChunks().containsKey(new ChunkPos(var1));
            int var7 = var9 ? 0 : 8;
            if (this.isAreaLoaded(var1.add(-var7, -var7, -var7), var1.add(var7, var7, var7))) {
               IBlockState var8 = this.getBlockState(var1);
               if (var8.getMaterial() != Material.AIR && var8.getBlock() == var2) {
                  var8.getBlock().updateTick(this, var1, var8, this.rand);
               }
            }

            return;
         }

         var3 = 1;
      }

      NextTickListEntry var6 = new NextTickListEntry(var1, var2);
      if (this.isBlockLoaded(var1)) {
         if (var5 != Material.AIR) {
            var6.setScheduledTime((long)var3 + this.worldInfo.getWorldTotalTime());
            var6.setPriority(var4);
         }

         if (!this.pendingTickListEntriesHashSet.contains(var6)) {
            this.pendingTickListEntriesHashSet.add(var6);
            this.pendingTickListEntriesTreeSet.add(var6);
         }
      }

   }

   public void scheduleBlockUpdate(BlockPos var1, Block var2, int var3, int var4) {
      if (var2 != null) {
         if (var1 instanceof BlockPos.MutableBlockPos || var1 instanceof BlockPos.PooledMutableBlockPos) {
            var1 = new BlockPos(var1);
            LogManager.getLogger().warn("Tried to assign a mutable BlockPos to tick data...", new Error(var1.getClass().toString()));
         }

         NextTickListEntry var5 = new NextTickListEntry(var1, var2);
         var5.setPriority(var4);
         Material var6 = var2.getDefaultState().getMaterial();
         if (var6 != Material.AIR) {
            var5.setScheduledTime((long)var3 + this.worldInfo.getWorldTotalTime());
         }

         if (!this.pendingTickListEntriesHashSet.contains(var5)) {
            this.pendingTickListEntriesHashSet.add(var5);
            this.pendingTickListEntriesTreeSet.add(var5);
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

      for(int var1 = 0; var1 < this.playerEntities.size(); ++var1) {
         Entity var2 = (Entity)this.playerEntities.get(var1);
         Entity var3 = var2.getRidingEntity();
         if (var3 != null) {
            if (!var3.isDead && var3.isPassenger(var2)) {
               continue;
            }

            var2.dismountRidingEntity();
         }

         this.theProfiler.startSection("tick");
         if (!var2.isDead) {
            try {
               this.updateEntity(var2);
            } catch (Throwable var7) {
               CrashReport var5 = CrashReport.makeCrashReport(var7, "Ticking player");
               CrashReportCategory var6 = var5.makeCategory("Player being ticked");
               var2.addEntityCrashInfo(var6);
               throw new ReportedException(var5);
            }
         }

         this.theProfiler.endSection();
         this.theProfiler.startSection("remove");
         if (var2.isDead) {
            int var4 = var2.chunkCoordX;
            int var8 = var2.chunkCoordZ;
            if (var2.addedToChunk && this.isChunkLoaded(var4, var8, true)) {
               this.getChunkFromChunkCoords(var4, var8).removeEntity(var2);
            }

            this.loadedEntityList.remove(var2);
            this.onEntityRemoved(var2);
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
         int var2 = this.pendingTickListEntriesTreeSet.size();
         if (var2 != this.pendingTickListEntriesHashSet.size()) {
            throw new IllegalStateException("TickNextTick list out of synch");
         } else {
            if (var2 > 65536) {
               var2 = 65536;
            }

            this.theProfiler.startSection("cleaning");

            for(int var3 = 0; var3 < var2; ++var3) {
               NextTickListEntry var4 = (NextTickListEntry)this.pendingTickListEntriesTreeSet.first();
               if (!var1 && var4.scheduledTime > this.worldInfo.getWorldTotalTime()) {
                  break;
               }

               this.pendingTickListEntriesTreeSet.remove(var4);
               this.pendingTickListEntriesHashSet.remove(var4);
               this.pendingTickListEntriesThisTick.add(var4);
            }

            this.theProfiler.endSection();
            this.theProfiler.startSection("ticking");
            Iterator var11 = this.pendingTickListEntriesThisTick.iterator();

            while(var11.hasNext()) {
               NextTickListEntry var12 = (NextTickListEntry)var11.next();
               var11.remove();
               boolean var5 = false;
               if (this.isAreaLoaded(var12.position.add(0, 0, 0), var12.position.add(0, 0, 0))) {
                  IBlockState var6 = this.getBlockState(var12.position);
                  if (var6.getMaterial() != Material.AIR && Block.isEqualTo(var6.getBlock(), var12.getBlock())) {
                     try {
                        var6.getBlock().updateTick(this, var12.position, var6, this.rand);
                     } catch (Throwable var10) {
                        CrashReport var8 = CrashReport.makeCrashReport(var10, "Exception while ticking a block");
                        CrashReportCategory var9 = var8.makeCategory("Block being ticked");
                        CrashReportCategory.addBlockInfo(var9, var12.position, var6);
                        throw new ReportedException(var8);
                     }
                  }
               } else {
                  this.scheduleUpdate(var12.position, var12.getBlock(), 0);
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
      ChunkPos var3 = var1.getChunkCoordIntPair();
      int var4 = (var3.chunkXPos << 4) - 2;
      int var5 = var4 + 16 + 2;
      int var6 = (var3.chunkZPos << 4) - 2;
      int var7 = var6 + 16 + 2;
      return this.getPendingBlockUpdates(new StructureBoundingBox(var4, 0, var6, var5, 256, var7), var2);
   }

   @Nullable
   public List getPendingBlockUpdates(StructureBoundingBox var1, boolean var2) {
      ArrayList var3 = null;

      for(int var4 = 0; var4 < 2; ++var4) {
         Iterator var5;
         if (var4 == 0) {
            var5 = this.pendingTickListEntriesTreeSet.iterator();
         } else {
            var5 = this.pendingTickListEntriesThisTick.iterator();
         }

         while(var5.hasNext()) {
            NextTickListEntry var6 = (NextTickListEntry)var5.next();
            BlockPos var7 = var6.position;
            if (var7.getX() >= var1.minX && var7.getX() < var1.maxX && var7.getZ() >= var1.minZ && var7.getZ() < var1.maxZ) {
               if (var2) {
                  if (var4 == 0) {
                     this.pendingTickListEntriesHashSet.remove(var6);
                  }

                  var5.remove();
               }

               if (var3 == null) {
                  var3 = Lists.newArrayList();
               }

               var3.add(var6);
            }
         }
      }

      return var3;
   }

   public void updateEntityWithOptionalForce(Entity var1, boolean var2) {
      if (!this.canSpawnAnimals() && (var1 instanceof EntityAnimal || var1 instanceof EntityWaterMob)) {
         var1.setDead();
      }

      if (!this.canSpawnNPCs() && var1 instanceof INpc) {
         var1.setDead();
      }

      super.updateEntityWithOptionalForce(var1, var2);
   }

   private boolean canSpawnNPCs() {
      return this.mcServer.getCanSpawnNPCs();
   }

   private boolean canSpawnAnimals() {
      return this.mcServer.getCanSpawnAnimals();
   }

   protected IChunkProvider createChunkProvider() {
      IChunkLoader var1 = this.saveHandler.getChunkLoader(this.provider);
      return new ChunkProviderServer(this, var1, this.provider.createChunkGenerator());
   }

   public boolean isBlockModifiable(EntityPlayer var1, BlockPos var2) {
      return super.isBlockModifiable(var1, var2);
   }

   public boolean canMineBlockBody(EntityPlayer var1, BlockPos var2) {
      return !this.mcServer.isBlockProtected(this, var2, var1) && this.getWorldBorder().contains(var2);
   }

   public void initialize(WorldSettings var1) {
      if (!this.worldInfo.isInitialized()) {
         try {
            this.createSpawnPosition(var1);
            if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) {
               this.setDebugWorldSettings();
            }

            super.initialize(var1);
         } catch (Throwable var6) {
            CrashReport var3 = CrashReport.makeCrashReport(var6, "Exception initializing level");

            try {
               this.addWorldInfoToCrashReport(var3);
            } catch (Throwable var5) {
               ;
            }

            throw new ReportedException(var3);
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
         if (ForgeEventFactory.onCreateWorldSpawn(this, var1)) {
            return;
         }

         this.findingSpawnPoint = true;
         BiomeProvider var2 = this.provider.getBiomeProvider();
         List var3 = var2.getBiomesToSpawnIn();
         Random var4 = new Random(this.getSeed());
         BlockPos var5 = var2.findBiomePosition(0, 0, 256, var3, var4);
         int var6 = 8;
         int var7 = this.provider.getAverageGroundLevel();
         int var8 = 8;
         if (var5 != null) {
            var6 = var5.getX();
            var8 = var5.getZ();
         } else {
            LOGGER.warn("Unable to find spawn biome");
         }

         int var9 = 0;

         while(!this.provider.canCoordinateBeSpawn(var6, var8)) {
            var6 += var4.nextInt(64) - var4.nextInt(64);
            var8 += var4.nextInt(64) - var4.nextInt(64);
            ++var9;
            if (var9 == 1000) {
               break;
            }
         }

         this.worldInfo.setSpawn(new BlockPos(var6, var7, var8));
         this.findingSpawnPoint = false;
         if (var1.isBonusChestEnabled()) {
            this.createBonusChest();
         }
      }

   }

   protected void createBonusChest() {
      WorldGeneratorBonusChest var1 = new WorldGeneratorBonusChest();

      for(int var2 = 0; var2 < 10; ++var2) {
         int var3 = this.worldInfo.getSpawnX() + this.rand.nextInt(6) - this.rand.nextInt(6);
         int var4 = this.worldInfo.getSpawnZ() + this.rand.nextInt(6) - this.rand.nextInt(6);
         BlockPos var5 = this.getTopSolidOrLiquidBlock(new BlockPos(var3, 0, var4)).up();
         if (var1.generate(this, this.rand, var5)) {
            break;
         }
      }

   }

   public BlockPos getSpawnCoordinate() {
      return this.provider.getSpawnCoordinate();
   }

   public void saveAllChunks(boolean var1, @Nullable IProgressUpdate var2) throws MinecraftException {
      ChunkProviderServer var3 = this.getChunkProvider();
      if (var3.canSave()) {
         if (var2 != null) {
            var2.displaySavingString("Saving level");
         }

         this.saveLevel();
         if (var2 != null) {
            var2.displayLoadingString("Saving chunks");
         }

         var3.saveChunks(var1);
         MinecraftForge.EVENT_BUS.post(new Save(this));

         for(Chunk var5 : Lists.newArrayList(var3.getLoadedChunks())) {
            if (var5 != null && !this.playerChunkMap.contains(var5.xPosition, var5.zPosition)) {
               var3.unload(var5);
            }
         }
      }

   }

   public void saveChunkData() {
      ChunkProviderServer var1 = this.getChunkProvider();
      if (var1.canSave()) {
         var1.saveExtraData();
      }

   }

   protected void saveLevel() throws MinecraftException {
      this.checkSessionLock();

      for(WorldServer var4 : this.mcServer.worlds) {
         if (var4 instanceof WorldServerMulti) {
            ((WorldServerMulti)var4).saveAdditionalData();
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
      return this.canAddEntity(var1) ? super.spawnEntity(var1) : false;
   }

   public void loadEntities(Collection var1) {
      for(Entity var3 : Lists.newArrayList(var1)) {
         if (this.canAddEntity(var3) && !MinecraftForge.EVENT_BUS.post(new EntityJoinWorldEvent(var3, this))) {
            this.loadedEntityList.add(var3);
            this.onEntityAdded(var3);
         }
      }

   }

   private boolean canAddEntity(Entity var1) {
      if (var1.isDead) {
         LOGGER.warn("Tried to add entity {} but it was marked as removed already", new Object[]{EntityList.getEntityString(var1)});
         return false;
      } else {
         UUID var2 = var1.getUniqueID();
         if (this.entitiesByUuid.containsKey(var2)) {
            Entity var3 = (Entity)this.entitiesByUuid.get(var2);
            if (this.unloadedEntityList.contains(var3)) {
               this.unloadedEntityList.remove(var3);
            } else {
               if (!(var1 instanceof EntityPlayer)) {
                  LOGGER.warn("Keeping entity {} that already exists with UUID {}", new Object[]{EntityList.getEntityString(var3), var2.toString()});
                  return false;
               }

               LOGGER.warn("Force-added player with duplicate UUID {}", new Object[]{var2.toString()});
            }

            this.removeEntityDangerously(var3);
         }

         return true;
      }
   }

   public void onEntityAdded(Entity var1) {
      super.onEntityAdded(var1);
      this.entitiesById.addKey(var1.getEntityId(), var1);
      this.entitiesByUuid.put(var1.getUniqueID(), var1);
      Entity[] var2 = var1.getParts();
      if (var2 != null) {
         for(Entity var6 : var2) {
            this.entitiesById.addKey(var6.getEntityId(), var6);
         }
      }

   }

   public void onEntityRemoved(Entity var1) {
      super.onEntityRemoved(var1);
      this.entitiesById.removeObject(var1.getEntityId());
      this.entitiesByUuid.remove(var1.getUniqueID());
      Entity[] var2 = var1.getParts();
      if (var2 != null) {
         for(Entity var6 : var2) {
            this.entitiesById.removeObject(var6.getEntityId());
         }
      }

   }

   public boolean addWeatherEffect(Entity var1) {
      if (super.addWeatherEffect(var1)) {
         this.mcServer.getPlayerList().sendToAllNearExcept((EntityPlayer)null, var1.posX, var1.posY, var1.posZ, 512.0D, this.provider.getDimension(), new SPacketSpawnGlobalEntity(var1));
         return true;
      } else {
         return false;
      }
   }

   public void setEntityState(Entity var1, byte var2) {
      this.getEntityTracker().sendToTrackingAndSelf(var1, new SPacketEntityStatus(var1, var2));
   }

   public ChunkProviderServer getChunkProvider() {
      return (ChunkProviderServer)super.getChunkProvider();
   }

   public Explosion newExplosion(@Nullable Entity var1, double var2, double var4, double var6, float var8, boolean var9, boolean var10) {
      Explosion var11 = new Explosion(this, var1, var2, var4, var6, var8, var9, var10);
      if (ForgeEventFactory.onExplosionStart(this, var11)) {
         return var11;
      } else {
         var11.doExplosionA();
         var11.doExplosionB(false);
         if (!var10) {
            var11.clearAffectedBlockPositions();
         }

         for(EntityPlayer var13 : this.playerEntities) {
            if (var13.getDistanceSq(var2, var4, var6) < 4096.0D) {
               ((EntityPlayerMP)var13).connection.sendPacket(new SPacketExplosion(var2, var4, var6, var8, var11.getAffectedBlockPositions(), (Vec3d)var11.getPlayerKnockbackMap().get(var13)));
            }
         }

         return var11;
      }
   }

   public void addBlockEvent(BlockPos var1, Block var2, int var3, int var4) {
      BlockEventData var5 = new BlockEventData(var1, var2, var3, var4);

      for(BlockEventData var7 : this.blockEventQueue[this.blockEventCacheIndex]) {
         if (var7.equals(var5)) {
            return;
         }
      }

      this.blockEventQueue[this.blockEventCacheIndex].add(var5);
   }

   private void sendQueuedBlockEvents() {
      while(!this.blockEventQueue[this.blockEventCacheIndex].isEmpty()) {
         int var1 = this.blockEventCacheIndex;
         this.blockEventCacheIndex ^= 1;

         for(BlockEventData var3 : this.blockEventQueue[var1]) {
            if (this.fireBlockEvent(var3)) {
               this.mcServer.getPlayerList().sendToAllNearExcept((EntityPlayer)null, (double)var3.getPosition().getX(), (double)var3.getPosition().getY(), (double)var3.getPosition().getZ(), 64.0D, this.provider.getDimension(), new SPacketBlockAction(var3.getPosition(), var3.getBlock(), var3.getEventID(), var3.getEventParameter()));
            }
         }

         this.blockEventQueue[var1].clear();
      }

   }

   private boolean fireBlockEvent(BlockEventData var1) {
      IBlockState var2 = this.getBlockState(var1.getPosition());
      return var2.getBlock() == var1.getBlock() ? var2.onBlockEventReceived(this, var1.getPosition(), var1.getEventID(), var1.getEventParameter()) : false;
   }

   public void flush() {
      this.saveHandler.flush();
   }

   protected void updateWeather() {
      boolean var1 = this.isRaining();
      super.updateWeather();
      if (this.prevRainingStrength != this.rainingStrength) {
         this.mcServer.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketChangeGameState(7, this.rainingStrength), this.provider.getDimension());
      }

      if (this.prevThunderingStrength != this.thunderingStrength) {
         this.mcServer.getPlayerList().sendPacketToAllPlayersInDimension(new SPacketChangeGameState(8, this.thunderingStrength), this.provider.getDimension());
      }

      if (var1 != this.isRaining()) {
         if (var1) {
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
      this.spawnParticle(var1, false, var2, var4, var6, var8, var9, var11, var13, var15, var17);
   }

   public void spawnParticle(EnumParticleTypes var1, boolean var2, double var3, double var5, double var7, int var9, double var10, double var12, double var14, double var16, int... var18) {
      SPacketParticles var19 = new SPacketParticles(var1, var2, (float)var3, (float)var5, (float)var7, (float)var10, (float)var12, (float)var14, (float)var16, var9, var18);

      for(int var20 = 0; var20 < this.playerEntities.size(); ++var20) {
         EntityPlayerMP var21 = (EntityPlayerMP)this.playerEntities.get(var20);
         this.sendPacketWithinDistance(var21, var2, var3, var5, var7, var19);
      }

   }

   public void spawnParticle(EntityPlayerMP var1, EnumParticleTypes var2, boolean var3, double var4, double var6, double var8, int var10, double var11, double var13, double var15, double var17, int... var19) {
      SPacketParticles var20 = new SPacketParticles(var2, var3, (float)var4, (float)var6, (float)var8, (float)var11, (float)var13, (float)var15, (float)var17, var10, var19);
      this.sendPacketWithinDistance(var1, var3, var4, var6, var8, var20);
   }

   private void sendPacketWithinDistance(EntityPlayerMP var1, boolean var2, double var3, double var5, double var7, Packet var9) {
      BlockPos var10 = var1.getPosition();
      double var11 = var10.distanceSq(var3, var5, var7);
      if (var11 <= 1024.0D || var2 && var11 <= 262144.0D) {
         var1.connection.sendPacket(var9);
      }

   }

   @Nullable
   public Entity getEntityFromUuid(UUID var1) {
      return (Entity)this.entitiesByUuid.get(var1);
   }

   public ListenableFuture addScheduledTask(Runnable var1) {
      return this.mcServer.addScheduledTask(var1);
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
