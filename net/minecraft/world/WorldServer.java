package net.minecraft.world;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.HorseType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketBlockAction;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.network.play.server.SPacketSpawnGlobalEntity;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.ScoreboardSaveData;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.src.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.tileentity.TileEntitySign;
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
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.World.Environment;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_10_R1.CraftTravelAgent;
import org.bukkit.craftbukkit.v1_10_R1.generator.CustomChunkGenerator;
import org.bukkit.craftbukkit.v1_10_R1.generator.NetherChunkGenerator;
import org.bukkit.craftbukkit.v1_10_R1.generator.NormalChunkGenerator;
import org.bukkit.craftbukkit.v1_10_R1.generator.SkyLandsChunkGenerator;
import org.bukkit.craftbukkit.v1_10_R1.util.HashTreeSet;
import org.bukkit.entity.LightningStrike;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.generator.ChunkGenerator;

public class WorldServer extends World implements IThreadListener {
   private static final Logger LOGGER = LogManager.getLogger();
   private final MinecraftServer mcServer;
   public EntityTracker theEntityTracker;
   private final PlayerChunkMap playerChunkMap;
   private final HashTreeSet pendingTickListEntriesTreeSet = new HashTreeSet();
   private final Map entitiesByUuid = Maps.newHashMap();
   public boolean disableLevelSaving;
   private boolean allPlayersSleeping;
   private int updateEntityTick;
   private final Teleporter worldTeleporter;
   private final WorldEntitySpawner entitySpawner = new WorldEntitySpawner();
   protected final VillageSiege villageSiege = new VillageSiege(this);
   private final WorldServer.ServerBlockEventList[] blockEventQueue = new WorldServer.ServerBlockEventList[]{new WorldServer.ServerBlockEventList((Object)null), new WorldServer.ServerBlockEventList((Object)null)};
   private int blockEventCacheIndex;
   private final List pendingTickListEntriesThisTick = Lists.newArrayList();
   public final int dimension;

   public WorldServer(MinecraftServer var1, ISaveHandler var2, WorldInfo var3, int var4, Profiler var5, Environment var6, ChunkGenerator var7) {
      super(var2, var3, DimensionType.getById(var6.getId()).createDimension(), var5, false, var7, var6);
      this.dimension = var4;
      this.pvpMode = var1.getPVP();
      var3.world = this;
      this.mcServer = var1;
      this.theEntityTracker = new EntityTracker(this);
      this.playerChunkMap = new PlayerChunkMap(this);
      this.provider.registerWorld(this);
      this.chunkProvider = this.createChunkProvider();
      this.worldTeleporter = new CraftTravelAgent(this);
      this.calculateInitialSkylight();
      this.calculateInitialWeather();
      this.getWorldBorder().setSize(var1.aD());
   }

   public World init() {
      this.mapStorage = new MapStorage(this.saveHandler);
      String var1 = VillageCollection.fileNameForProvider(this.provider);
      VillageCollection var2 = (VillageCollection)this.mapStorage.getOrLoadData(VillageCollection.class, var1);
      if (var2 == null) {
         this.villageCollectionObj = new VillageCollection(this);
         this.mapStorage.setData(var1, this.villageCollectionObj);
      } else {
         this.villageCollectionObj = var2;
         this.villageCollectionObj.setWorldsForAll(this);
      }

      if (this.getServer().getScoreboardManager() == null) {
         this.worldScoreboard = new ServerScoreboard(this.mcServer);
         ScoreboardSaveData var3 = (ScoreboardSaveData)this.mapStorage.getOrLoadData(ScoreboardSaveData.class, "scoreboard");
         if (var3 == null) {
            var3 = new ScoreboardSaveData();
            this.mapStorage.setData("scoreboard", var3);
         }

         var3.setScoreboard(this.worldScoreboard);
         ((ServerScoreboard)this.worldScoreboard).addDirtyRunnable(new WorldSavedDataCallableSave(var3));
      } else {
         this.worldScoreboard = this.getServer().getScoreboardManager().getMainScoreboard().getHandle();
      }

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

      if (this.generator != null) {
         this.getWorld().getPopulators().addAll(this.generator.getDefaultPopulators(this.getWorld()));
      }

      return this;
   }

   public TileEntity getTileEntity(BlockPos var1) {
      TileEntity var2 = super.getTileEntity(var1);
      Block var3 = this.getBlockState(var1).getBlock();
      if (var3 == Blocks.CHEST) {
         if (!(var2 instanceof TileEntityChest)) {
            var2 = this.fixTileEntity(var1, var3, var2);
         }
      } else if (var3 == Blocks.FURNACE) {
         if (!(var2 instanceof TileEntityFurnace)) {
            var2 = this.fixTileEntity(var1, var3, var2);
         }
      } else if (var3 == Blocks.DROPPER) {
         if (!(var2 instanceof TileEntityDropper)) {
            var2 = this.fixTileEntity(var1, var3, var2);
         }
      } else if (var3 == Blocks.DISPENSER) {
         if (!(var2 instanceof TileEntityDispenser)) {
            var2 = this.fixTileEntity(var1, var3, var2);
         }
      } else if (var3 == Blocks.JUKEBOX) {
         if (!(var2 instanceof BlockJukebox.TileEntityJukebox)) {
            var2 = this.fixTileEntity(var1, var3, var2);
         }
      } else if (var3 == Blocks.NOTEBLOCK) {
         if (!(var2 instanceof TileEntityNote)) {
            var2 = this.fixTileEntity(var1, var3, var2);
         }
      } else if (var3 == Blocks.MOB_SPAWNER) {
         if (!(var2 instanceof TileEntityMobSpawner)) {
            var2 = this.fixTileEntity(var1, var3, var2);
         }
      } else if (var3 != Blocks.STANDING_SIGN && var3 != Blocks.WALL_SIGN) {
         if (var3 == Blocks.ENDER_CHEST) {
            if (!(var2 instanceof TileEntityEnderChest)) {
               var2 = this.fixTileEntity(var1, var3, var2);
            }
         } else if (var3 == Blocks.BREWING_STAND) {
            if (!(var2 instanceof TileEntityBrewingStand)) {
               var2 = this.fixTileEntity(var1, var3, var2);
            }
         } else if (var3 == Blocks.BEACON) {
            if (!(var2 instanceof TileEntityBeacon)) {
               var2 = this.fixTileEntity(var1, var3, var2);
            }
         } else if (var3 == Blocks.HOPPER && !(var2 instanceof TileEntityHopper)) {
            var2 = this.fixTileEntity(var1, var3, var2);
         }
      } else if (!(var2 instanceof TileEntitySign)) {
         var2 = this.fixTileEntity(var1, var3, var2);
      }

      return var2;
   }

   private TileEntity fixTileEntity(BlockPos var1, Block var2, TileEntity var3) {
      this.getServer().getLogger().log(Level.SEVERE, "Block at {0},{1},{2} is {3} but has {4}. Bukkit will attempt to fix this, but there may be additional damage that we cannot recover.", new Object[]{var1.getX(), var1.getY(), var1.getZ(), Material.getMaterial(Block.getIdFromBlock(var2)).toString(), var3});
      if (var2 instanceof ITileEntityProvider) {
         TileEntity var4 = ((ITileEntityProvider)var2).createNewTileEntity(this, var2.getMetaFromState(this.getBlockState(var1)));
         var4.world = this;
         this.setTileEntity(var1, var4);
         return var4;
      } else {
         this.getServer().getLogger().severe("Don't know how to fix for this type... Can't do anything! :(");
         return var3;
      }
   }

   private boolean canSpawn(int var1, int var2) {
      return this.generator != null ? this.generator.canSpawn(this.getWorld(), var1, var2) : this.provider.canCoordinateBeSpawn(var1, var2);
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

      long var4 = this.worldInfo.getWorldTotalTime();
      if (this.getGameRules().getBoolean("doMobSpawning") && this.worldInfo.getTerrainType() != WorldType.DEBUG_WORLD && (this.spawnHostileMobs || this.spawnPeacefulMobs) && this instanceof WorldServer && this.playerEntities.size() > 0) {
         this.entitySpawner.findChunksForSpawning(this, this.spawnHostileMobs && this.ticksPerMonsterSpawns != 0L && var4 % this.ticksPerMonsterSpawns == 0L, this.spawnPeacefulMobs && this.ticksPerAnimalSpawns != 0L && var4 % this.ticksPerAnimalSpawns == 0L, this.worldInfo.getWorldTotalTime() % 400L == 0L);
      }

      this.theProfiler.endStartSection("chunkSource");
      this.chunkProvider.tick();
      int var3 = this.calculateSkylightSubtracted(1.0F);
      if (var3 != this.getSkylightSubtracted()) {
         this.setSkylightSubtracted(var3);
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
      this.theProfiler.endSection();
      this.sendQueuedBlockEvents();
      this.getWorld().processChunkGC();
   }

   @Nullable
   public Biome.SpawnListEntry getSpawnListEntryForTypeAt(EnumCreatureType var1, BlockPos var2) {
      List var3 = this.getChunkProvider().getPossibleCreatures(var1, var2);
      return var3 != null && !var3.isEmpty() ? (Biome.SpawnListEntry)WeightedRandom.getRandomItem(this.rand, var3) : null;
   }

   public boolean canCreatureTypeSpawnHere(EnumCreatureType var1, Biome.SpawnListEntry var2, BlockPos var3) {
      List var4 = this.getChunkProvider().getPossibleCreatures(var1, var3);
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
            } else if (var4.isPlayerSleeping() || var4.fauxSleeping) {
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
      this.worldInfo.setRaining(false);
      if (!this.worldInfo.isRaining()) {
         this.worldInfo.setRainTime(0);
      }

      this.worldInfo.setThundering(false);
      if (!this.worldInfo.isThundering()) {
         this.worldInfo.setThunderTime(0);
      }

   }

   public boolean areAllPlayersAsleep() {
      if (this.allPlayersSleeping && !this.isRemote) {
         Iterator var1 = this.playerEntities.iterator();
         boolean var2 = false;

         while(var1.hasNext()) {
            EntityPlayer var3 = (EntityPlayer)var1.next();
            if (var3.isPlayerFullyAsleep()) {
               var2 = true;
            }

            if (var3.isSpectator() && !var3.isPlayerFullyAsleep() && !var3.fauxSleeping) {
               return false;
            }
         }

         return var2;
      } else {
         return false;
      }
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
         int var21 = this.getGameRules().getInt("randomTickSpeed");
         boolean var2 = this.isRaining();
         boolean var3 = this.isThundering();
         this.theProfiler.startSection("pollingChunks");

         for(Iterator var4 = this.playerChunkMap.getChunkIterator(); var4.hasNext(); this.theProfiler.endSection()) {
            this.theProfiler.startSection("getChunk");
            Chunk var5 = (Chunk)var4.next();
            int var6 = var5.xPosition * 16;
            int var7 = var5.zPosition * 16;
            this.theProfiler.endStartSection("checkNextLight");
            var5.enqueueRelightChecks();
            this.theProfiler.endStartSection("tickChunk");
            var5.onTick(false);
            this.theProfiler.endStartSection("thunder");
            if (var2 && var3 && this.rand.nextInt(100000) == 0) {
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
                     this.addEntity(var11, SpawnReason.LIGHTNING);
                     this.addWeatherEffect(new EntityLightningBolt(this, (double)var9.getX(), (double)var9.getY(), (double)var9.getZ(), true));
                  } else {
                     this.addWeatherEffect(new EntityLightningBolt(this, (double)var9.getX(), (double)var9.getY(), (double)var9.getZ(), false));
                  }
               }
            }

            this.theProfiler.endStartSection("iceandsnow");
            if (this.rand.nextInt(16) == 0) {
               this.updateLCG = this.updateLCG * 3 + 1013904223;
               int var22 = this.updateLCG >> 2;
               BlockPos var23 = this.getPrecipitationHeight(new BlockPos(var6 + (var22 & 15), 0, var7 + (var22 >> 8 & 15)));
               BlockPos var24 = var23.down();
               if (this.canBlockFreezeNoWater(var24)) {
                  BlockState var26 = this.getWorld().getBlockAt(var24.getX(), var24.getY(), var24.getZ()).getState();
                  var26.setTypeId(Block.getIdFromBlock(Blocks.ICE));
                  BlockFormEvent var12 = new BlockFormEvent(var26.getBlock(), var26);
                  this.getServer().getPluginManager().callEvent(var12);
                  if (!var12.isCancelled()) {
                     var26.update(true);
                  }
               }

               if (var2 && this.canSnowAt(var23, true)) {
                  BlockState var27 = this.getWorld().getBlockAt(var23.getX(), var23.getY(), var23.getZ()).getState();
                  var27.setTypeId(Block.getIdFromBlock(Blocks.SNOW_LAYER));
                  BlockFormEvent var29 = new BlockFormEvent(var27.getBlock(), var27);
                  this.getServer().getPluginManager().callEvent(var29);
                  if (!var29.isCancelled()) {
                     var27.update(true);
                  }
               }

               if (var2 && this.getBiome(var24).canRain()) {
                  this.getBlockState(var24).getBlock().fillWithRain(this, var24);
               }
            }

            this.theProfiler.endStartSection("tickBlocks");
            if (var21 > 0) {
               for(ExtendedBlockStorage var13 : var5.getBlockStorageArray()) {
                  if (var13 != Chunk.NULL_BLOCK_STORAGE && var13.getNeedsRandomTick()) {
                     for(int var14 = 0; var14 < var21; ++var14) {
                        this.updateLCG = this.updateLCG * 3 + 1013904223;
                        int var15 = this.updateLCG >> 2;
                        int var16 = var15 & 15;
                        int var17 = var15 >> 8 & 15;
                        int var18 = var15 >> 16 & 15;
                        IBlockState var19 = var13.get(var16, var18, var17);
                        Block var20 = var19.getBlock();
                        this.theProfiler.startSection("randomTick");
                        if (var20.getTickRandomly()) {
                           var20.randomTick(this, new BlockPos(var16 + var6, var18 + var13.getYLocation(), var17 + var7), var19, this.rand);
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

         public boolean apply(Object var1) {
            return this.apply((EntityLivingBase)var1);
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
      return this.pendingTickListEntriesTreeSet.contains(var3);
   }

   public void scheduleUpdate(BlockPos var1, Block var2, int var3) {
      this.updateBlockTick(var1, var2, var3, 0);
   }

   public void updateBlockTick(BlockPos var1, Block var2, int var3, int var4) {
      if (var1 instanceof BlockPos.MutableBlockPos || var1 instanceof BlockPos.PooledMutableBlockPos) {
         var1 = new BlockPos(var1);
         LogManager.getLogger().warn("Tried to assign a mutable BlockPos to tick data...", new Error(var1.getClass().toString()));
      }

      net.minecraft.block.material.Material var5 = var2.getDefaultState().getMaterial();
      if (this.scheduledUpdatesAreImmediate && var5 != net.minecraft.block.material.Material.AIR) {
         if (var2.requiresUpdates()) {
            if (this.isAreaLoaded(var1.add(-8, -8, -8), var1.add(8, 8, 8))) {
               IBlockState var7 = this.getBlockState(var1);
               if (var7.getMaterial() != net.minecraft.block.material.Material.AIR && var7.getBlock() == var2) {
                  var7.getBlock().updateTick(this, var1, var7, this.rand);
               }
            }

            return;
         }

         var3 = 1;
      }

      NextTickListEntry var6 = new NextTickListEntry(var1, var2);
      if (this.isBlockLoaded(var1)) {
         if (var5 != net.minecraft.block.material.Material.AIR) {
            var6.setScheduledTime((long)var3 + this.worldInfo.getWorldTotalTime());
            var6.setPriority(var4);
         }

         if (!this.pendingTickListEntriesTreeSet.contains(var6)) {
            this.pendingTickListEntriesTreeSet.add(var6);
         }
      }

   }

   public void scheduleBlockUpdate(BlockPos var1, Block var2, int var3, int var4) {
      if (var1 instanceof BlockPos.MutableBlockPos || var1 instanceof BlockPos.PooledMutableBlockPos) {
         var1 = new BlockPos(var1);
         LogManager.getLogger().warn("Tried to assign a mutable BlockPos to tick data...", new Error(var1.getClass().toString()));
      }

      NextTickListEntry var5 = new NextTickListEntry(var1, var2);
      var5.setPriority(var4);
      net.minecraft.block.material.Material var6 = var2.getDefaultState().getMaterial();
      if (var6 != net.minecraft.block.material.Material.AIR) {
         var5.setScheduledTime((long)var3 + this.worldInfo.getWorldTotalTime());
      }

      if (!this.pendingTickListEntriesTreeSet.contains(var5)) {
         this.pendingTickListEntriesTreeSet.add(var5);
      }

   }

   public void updateEntities() {
      this.resetUpdateEntityTick();
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
         if (var2 > 65536) {
            if (var2 > 1310720) {
               var2 /= 20;
            } else {
               var2 = 65536;
            }
         }

         this.theProfiler.startSection("cleaning");

         for(int var3 = 0; var3 < var2; ++var3) {
            NextTickListEntry var4 = (NextTickListEntry)this.pendingTickListEntriesTreeSet.first();
            if (!var1 && var4.scheduledTime > this.worldInfo.getWorldTotalTime()) {
               break;
            }

            this.pendingTickListEntriesTreeSet.remove(var4);
            this.pendingTickListEntriesThisTick.add(var4);
         }

         this.theProfiler.endSection();
         this.theProfiler.startSection("ticking");
         Iterator var10 = this.pendingTickListEntriesThisTick.iterator();

         while(var10.hasNext()) {
            NextTickListEntry var11 = (NextTickListEntry)var10.next();
            var10.remove();
            if (this.isAreaLoaded(var11.position.add(0, 0, 0), var11.position.add(0, 0, 0))) {
               IBlockState var5 = this.getBlockState(var11.position);
               if (var5.getMaterial() != net.minecraft.block.material.Material.AIR && Block.isEqualTo(var5.getBlock(), var11.getBlock())) {
                  try {
                     var5.getBlock().updateTick(this, var11.position, var5, this.rand);
                  } catch (Throwable var9) {
                     CrashReport var7 = CrashReport.makeCrashReport(var9, "Exception while ticking a block");
                     CrashReportCategory var8 = var7.makeCategory("Block being ticked");
                     CrashReportCategory.addBlockInfo(var8, var11.position, var5);
                     throw new ReportedException(var7);
                  }
               }
            } else {
               this.scheduleUpdate(var11.position, var11.getBlock(), 0);
            }
         }

         this.theProfiler.endSection();
         this.pendingTickListEntriesThisTick.clear();
         return !this.pendingTickListEntriesTreeSet.isEmpty();
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

   private boolean canSpawnNPCs() {
      return this.mcServer.getSpawnNPCs();
   }

   private boolean canSpawnAnimals() {
      return this.mcServer.getSpawnAnimals();
   }

   protected IChunkProvider createChunkProvider() {
      IChunkLoader var1 = this.saveHandler.getChunkLoader(this.provider);
      Object var2;
      if (this.generator != null) {
         var2 = new CustomChunkGenerator(this, this.getSeed(), this.generator);
      } else if (this.provider instanceof WorldProviderHell) {
         var2 = new NetherChunkGenerator(this, this.getSeed());
      } else if (this.provider instanceof WorldProviderEnd) {
         var2 = new SkyLandsChunkGenerator(this, this.getSeed());
      } else {
         var2 = new NormalChunkGenerator(this, this.getSeed());
      }

      return new ChunkProviderServer(this, var1, (IChunkGenerator)var2);
   }

   public List a(int var1, int var2, int var3, int var4, int var5, int var6) {
      ArrayList var7 = Lists.newArrayList();

      for(int var8 = var1 >> 4; var8 <= var4 - 1 >> 4; ++var8) {
         for(int var9 = var3 >> 4; var9 <= var6 - 1 >> 4; ++var9) {
            Chunk var10 = this.getChunkFromChunkCoords(var8, var9);
            if (var10 != null) {
               for(Object var12 : var10.chunkTileEntityMap.values()) {
                  TileEntity var13 = (TileEntity)var12;
                  if (var13.pos.getX() >= var1 && var13.pos.getY() >= var2 && var13.pos.getZ() >= var3 && var13.pos.getX() < var4 && var13.pos.getY() < var5 && var13.pos.getZ() < var6) {
                     var7.add(var13);
                  }
               }
            }
         }
      }

      return var7;
   }

   public boolean isBlockModifiable(EntityPlayer var1, BlockPos var2) {
      return !this.mcServer.a(this, var2, var1) && this.getWorldBorder().contains(var2);
   }

   public void initialize(WorldSettings var1) {
      if (!this.worldInfo.isInitialized()) {
         try {
            this.createSpawnPosition(var1);
            if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) {
               this.setDebugWorldSettings();
            }

            super.initialize(var1);
         } catch (Throwable var5) {
            CrashReport var3 = CrashReport.makeCrashReport(var5, "Exception initializing level");

            try {
               this.addWorldInfoToCrashReport(var3);
            } catch (Throwable var4) {
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
         this.findingSpawnPoint = true;
         BiomeProvider var2 = this.provider.getBiomeProvider();
         List var3 = var2.getBiomesToSpawnIn();
         Random var4 = new Random(this.getSeed());
         BlockPos var5 = var2.findBiomePosition(0, 0, 256, var3, var4);
         int var6 = 8;
         int var7 = this.provider.getAverageGroundLevel();
         int var8 = 8;
         if (this.generator != null) {
            Random var9 = new Random(this.getSeed());
            Location var10 = this.generator.getFixedSpawnLocation(this.getWorld(), var9);
            if (var10 != null) {
               if (var10.getWorld() != this.getWorld()) {
                  throw new IllegalStateException("Cannot set spawn point for " + this.worldInfo.getWorldName() + " to be in another world (" + var10.getWorld().getName() + ")");
               }

               this.worldInfo.setSpawn(new BlockPos(var10.getBlockX(), var10.getBlockY(), var10.getBlockZ()));
               this.findingSpawnPoint = false;
               return;
            }
         }

         if (var5 != null) {
            var6 = var5.getX();
            var8 = var5.getZ();
         } else {
            LOGGER.warn("Unable to find spawn biome");
         }

         int var11 = 0;

         while(!this.canSpawn(var6, var8)) {
            var6 += var4.nextInt(64) - var4.nextInt(64);
            var8 += var4.nextInt(64) - var4.nextInt(64);
            ++var11;
            if (var11 == 1000) {
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
         Bukkit.getPluginManager().callEvent(new WorldSaveEvent(this.getWorld()));
         if (var2 != null) {
            var2.displaySavingString("Saving level");
         }

         this.saveLevel();
         if (var2 != null) {
            var2.displayLoadingString("Saving chunks");
         }

         var3.saveChunks(var1);

         for(Chunk var6 : var3.getLoadedChunks()) {
            if (var6 != null && !this.playerChunkMap.contains(var6.xPosition, var6.zPosition)) {
               var3.unload(var6);
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

      for(WorldServer var4 : this.mcServer.worldServer) {
         if (var4 instanceof WorldServerMulti) {
            ((WorldServerMulti)var4).saveAdditionalData();
         }
      }

      if (this instanceof WorldServerMulti) {
         ((WorldServerMulti)this).saveAdditionalData();
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
   }

   public boolean addEntity(Entity var1, SpawnReason var2) {
      return this.canAddEntity(var1) ? super.addEntity(var1, var2) : false;
   }

   public void loadEntities(Collection var1) {
      for(Entity var4 : Lists.newArrayList(var1)) {
         if (this.canAddEntity(var4)) {
            this.loadedEntityList.add(var4);
            this.onEntityAdded(var4);
         }
      }

   }

   private boolean canAddEntity(Entity var1) {
      if (var1.isDead) {
         return false;
      } else {
         UUID var2 = var1.getUniqueID();
         if (this.entitiesByUuid.containsKey(var2)) {
            Entity var3 = (Entity)this.entitiesByUuid.get(var2);
            if (this.unloadedEntityList.contains(var3)) {
               this.unloadedEntityList.remove(var3);
            } else {
               if (!(var1 instanceof EntityPlayer)) {
                  return false;
               }

               LOGGER.warn("Force-added player with duplicate UUID {}", new Object[]{var2.toString()});
            }

            this.removeEntityDangerously(var3);
         }

         return true;
      }
   }

   protected void onEntityAdded(Entity var1) {
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

   protected void onEntityRemoved(Entity var1) {
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
      LightningStrikeEvent var2 = new LightningStrikeEvent(this.getWorld(), (LightningStrike)var1.getBukkitEntity());
      this.getServer().getPluginManager().callEvent(var2);
      if (var2.isCancelled()) {
         return false;
      } else if (super.addWeatherEffect(var1)) {
         this.mcServer.getPlayerList().sendToAllNearExcept((EntityPlayer)null, var1.posX, var1.posY, var1.posZ, 512.0D, this.dimension, new SPacketSpawnGlobalEntity(var1));
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
      Explosion var11 = super.newExplosion(var1, var2, var4, var6, var8, var9, var10);
      if (var11.wasCanceled) {
         return var11;
      } else {
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
               this.mcServer.getPlayerList().sendToAllNearExcept((EntityPlayer)null, (double)var3.getPosition().getX(), (double)var3.getPosition().getY(), (double)var3.getPosition().getZ(), 64.0D, this.dimension, new SPacketBlockAction(var3.getPosition(), var3.getBlock(), var3.getEventID(), var3.getEventParameter()));
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
      if (var1 != this.isRaining()) {
         for(int var2 = 0; var2 < this.playerEntities.size(); ++var2) {
            if (((EntityPlayerMP)this.playerEntities.get(var2)).world == this) {
               ((EntityPlayerMP)this.playerEntities.get(var2)).setPlayerWeather(!var1 ? WeatherType.DOWNFALL : WeatherType.CLEAR, false);
            }
         }
      }

      for(int var3 = 0; var3 < this.playerEntities.size(); ++var3) {
         if (((EntityPlayerMP)this.playerEntities.get(var3)).world == this) {
            ((EntityPlayerMP)this.playerEntities.get(var3)).updateWeather(this.prevRainingStrength, this.rainingStrength, this.prevThunderingStrength, this.thunderingStrength);
         }
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
      this.sendParticles((EntityPlayerMP)null, var1, var2, var3, var5, var7, var9, var10, var12, var14, var16, var18);
   }

   public void sendParticles(EntityPlayerMP var1, EnumParticleTypes var2, boolean var3, double var4, double var6, double var8, int var10, double var11, double var13, double var15, double var17, int... var19) {
      SPacketParticles var20 = new SPacketParticles(var2, var3, (float)var4, (float)var6, (float)var8, (float)var11, (float)var13, (float)var15, (float)var17, var10, var19);

      for(int var21 = 0; var21 < this.playerEntities.size(); ++var21) {
         EntityPlayerMP var22 = (EntityPlayerMP)this.playerEntities.get(var21);
         if (var1 == null || var22.getBukkitEntity().canSee(var1.getBukkitEntity())) {
            BlockPos var23 = var22.getPosition();
            var23.distanceSq(var4, var6, var8);
            this.sendPacketWithinDistance(var22, var3, var4, var6, var8, var20);
         }
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

   public IChunkProvider getChunkProvider() {
      return this.getChunkProvider();
   }

   static class ServerBlockEventList extends ArrayList {
      private ServerBlockEventList() {
      }

      ServerBlockEventList(Object var1) {
         this();
      }
   }
}
