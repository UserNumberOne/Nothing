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
import org.bukkit.craftbukkit.v1_10_R1.generator.InternalChunkGenerator;
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

   public WorldServer(MinecraftServer minecraftserver, ISaveHandler idatamanager, WorldInfo worlddata, int i, Profiler methodprofiler, Environment env, ChunkGenerator gen) {
      super(idatamanager, worlddata, DimensionType.getById(env.getId()).createDimension(), methodprofiler, false, gen, env);
      this.dimension = i;
      this.pvpMode = minecraftserver.getPVP();
      worlddata.world = this;
      this.mcServer = minecraftserver;
      this.theEntityTracker = new EntityTracker(this);
      this.playerChunkMap = new PlayerChunkMap(this);
      this.provider.registerWorld(this);
      this.chunkProvider = this.createChunkProvider();
      this.worldTeleporter = new CraftTravelAgent(this);
      this.calculateInitialSkylight();
      this.calculateInitialWeather();
      this.getWorldBorder().setSize(minecraftserver.aD());
   }

   public World init() {
      this.mapStorage = new MapStorage(this.saveHandler);
      String s = VillageCollection.fileNameForProvider(this.provider);
      VillageCollection persistentvillage = (VillageCollection)this.mapStorage.getOrLoadData(VillageCollection.class, s);
      if (persistentvillage == null) {
         this.villageCollectionObj = new VillageCollection(this);
         this.mapStorage.setData(s, this.villageCollectionObj);
      } else {
         this.villageCollectionObj = persistentvillage;
         this.villageCollectionObj.setWorldsForAll(this);
      }

      if (this.getServer().getScoreboardManager() == null) {
         this.worldScoreboard = new ServerScoreboard(this.mcServer);
         ScoreboardSaveData persistentscoreboard = (ScoreboardSaveData)this.mapStorage.getOrLoadData(ScoreboardSaveData.class, "scoreboard");
         if (persistentscoreboard == null) {
            persistentscoreboard = new ScoreboardSaveData();
            this.mapStorage.setData("scoreboard", persistentscoreboard);
         }

         persistentscoreboard.setScoreboard(this.worldScoreboard);
         ((ServerScoreboard)this.worldScoreboard).addDirtyRunnable(new WorldSavedDataCallableSave(persistentscoreboard));
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

   public TileEntity getTileEntity(BlockPos pos) {
      TileEntity result = super.getTileEntity(pos);
      Block type = this.getBlockState(pos).getBlock();
      if (type == Blocks.CHEST) {
         if (!(result instanceof TileEntityChest)) {
            result = this.fixTileEntity(pos, type, result);
         }
      } else if (type == Blocks.FURNACE) {
         if (!(result instanceof TileEntityFurnace)) {
            result = this.fixTileEntity(pos, type, result);
         }
      } else if (type == Blocks.DROPPER) {
         if (!(result instanceof TileEntityDropper)) {
            result = this.fixTileEntity(pos, type, result);
         }
      } else if (type == Blocks.DISPENSER) {
         if (!(result instanceof TileEntityDispenser)) {
            result = this.fixTileEntity(pos, type, result);
         }
      } else if (type == Blocks.JUKEBOX) {
         if (!(result instanceof BlockJukebox.TileEntityJukebox)) {
            result = this.fixTileEntity(pos, type, result);
         }
      } else if (type == Blocks.NOTEBLOCK) {
         if (!(result instanceof TileEntityNote)) {
            result = this.fixTileEntity(pos, type, result);
         }
      } else if (type == Blocks.MOB_SPAWNER) {
         if (!(result instanceof TileEntityMobSpawner)) {
            result = this.fixTileEntity(pos, type, result);
         }
      } else if (type != Blocks.STANDING_SIGN && type != Blocks.WALL_SIGN) {
         if (type == Blocks.ENDER_CHEST) {
            if (!(result instanceof TileEntityEnderChest)) {
               result = this.fixTileEntity(pos, type, result);
            }
         } else if (type == Blocks.BREWING_STAND) {
            if (!(result instanceof TileEntityBrewingStand)) {
               result = this.fixTileEntity(pos, type, result);
            }
         } else if (type == Blocks.BEACON) {
            if (!(result instanceof TileEntityBeacon)) {
               result = this.fixTileEntity(pos, type, result);
            }
         } else if (type == Blocks.HOPPER && !(result instanceof TileEntityHopper)) {
            result = this.fixTileEntity(pos, type, result);
         }
      } else if (!(result instanceof TileEntitySign)) {
         result = this.fixTileEntity(pos, type, result);
      }

      return result;
   }

   private TileEntity fixTileEntity(BlockPos pos, Block type, TileEntity found) {
      this.getServer().getLogger().log(Level.SEVERE, "Block at {0},{1},{2} is {3} but has {4}. Bukkit will attempt to fix this, but there may be additional damage that we cannot recover.", new Object[]{pos.getX(), pos.getY(), pos.getZ(), Material.getMaterial(Block.getIdFromBlock(type)).toString(), found});
      if (type instanceof ITileEntityProvider) {
         TileEntity replacement = ((ITileEntityProvider)type).createNewTileEntity(this, type.getMetaFromState(this.getBlockState(pos)));
         replacement.world = this;
         this.setTileEntity(pos, replacement);
         return replacement;
      } else {
         this.getServer().getLogger().severe("Don't know how to fix for this type... Can't do anything! :(");
         return found;
      }
   }

   private boolean canSpawn(int x, int z) {
      return this.generator != null ? this.generator.canSpawn(this.getWorld(), x, z) : this.provider.canCoordinateBeSpawn(x, z);
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

      long time = this.worldInfo.getWorldTotalTime();
      if (this.getGameRules().getBoolean("doMobSpawning") && this.worldInfo.getTerrainType() != WorldType.DEBUG_WORLD && (this.spawnHostileMobs || this.spawnPeacefulMobs) && this instanceof WorldServer && this.playerEntities.size() > 0) {
         this.entitySpawner.findChunksForSpawning(this, this.spawnHostileMobs && this.ticksPerMonsterSpawns != 0L && time % this.ticksPerMonsterSpawns == 0L, this.spawnPeacefulMobs && this.ticksPerAnimalSpawns != 0L && time % this.ticksPerAnimalSpawns == 0L, this.worldInfo.getWorldTotalTime() % 400L == 0L);
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
      this.theProfiler.endSection();
      this.sendQueuedBlockEvents();
      this.getWorld().processChunkGC();
   }

   @Nullable
   public Biome.SpawnListEntry getSpawnListEntryForTypeAt(EnumCreatureType enumcreaturetype, BlockPos blockposition) {
      List list = this.getChunkProvider().getPossibleCreatures(enumcreaturetype, blockposition);
      return list != null && !list.isEmpty() ? (Biome.SpawnListEntry)WeightedRandom.getRandomItem(this.rand, list) : null;
   }

   public boolean canCreatureTypeSpawnHere(EnumCreatureType enumcreaturetype, Biome.SpawnListEntry biomebase_biomemeta, BlockPos blockposition) {
      List list = this.getChunkProvider().getPossibleCreatures(enumcreaturetype, blockposition);
      return list != null && !list.isEmpty() ? list.contains(biomebase_biomemeta) : false;
   }

   public void updateAllPlayersSleepingFlag() {
      this.allPlayersSleeping = false;
      if (!this.playerEntities.isEmpty()) {
         int i = 0;
         int j = 0;

         for(EntityPlayer entityhuman : this.playerEntities) {
            if (entityhuman.isSpectator()) {
               ++i;
            } else if (entityhuman.isPlayerSleeping() || entityhuman.fauxSleeping) {
               ++j;
            }
         }

         this.allPlayersSleeping = j > 0 && j >= this.playerEntities.size() - i;
      }

   }

   protected void wakeAllPlayers() {
      this.allPlayersSleeping = false;

      for(EntityPlayer entityhuman : this.playerEntities) {
         if (entityhuman.isPlayerSleeping()) {
            entityhuman.wakeUpPlayer(false, false, true);
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
         Iterator iterator = this.playerEntities.iterator();
         boolean foundActualSleepers = false;

         while(iterator.hasNext()) {
            EntityPlayer entityhuman = (EntityPlayer)iterator.next();
            if (entityhuman.isPlayerFullyAsleep()) {
               foundActualSleepers = true;
            }

            if (entityhuman.isSpectator() && !entityhuman.isPlayerFullyAsleep() && !entityhuman.fauxSleeping) {
               return false;
            }
         }

         return foundActualSleepers;
      } else {
         return false;
      }
   }

   protected boolean isChunkLoaded(int i, int j, boolean flag) {
      return this.getChunkProvider().chunkExists(i, j);
   }

   protected void playerCheckLight() {
      this.theProfiler.startSection("playerCheckLight");
      if (!this.playerEntities.isEmpty()) {
         int i = this.rand.nextInt(this.playerEntities.size());
         EntityPlayer entityhuman = (EntityPlayer)this.playerEntities.get(i);
         int j = MathHelper.floor(entityhuman.posX) + this.rand.nextInt(11) - 5;
         int k = MathHelper.floor(entityhuman.posY) + this.rand.nextInt(11) - 5;
         int l = MathHelper.floor(entityhuman.posZ) + this.rand.nextInt(11) - 5;
         this.checkLight(new BlockPos(j, k, l));
      }

      this.theProfiler.endSection();
   }

   protected void updateBlocks() {
      this.playerCheckLight();
      if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) {
         Iterator iterator = this.playerChunkMap.getChunkIterator();

         while(iterator.hasNext()) {
            ((Chunk)iterator.next()).onTick(false);
         }
      } else {
         int i = this.getGameRules().getInt("randomTickSpeed");
         boolean flag = this.isRaining();
         boolean flag1 = this.isThundering();
         this.theProfiler.startSection("pollingChunks");

         for(Iterator iterator1 = this.playerChunkMap.getChunkIterator(); iterator1.hasNext(); this.theProfiler.endSection()) {
            this.theProfiler.startSection("getChunk");
            Chunk chunk = (Chunk)iterator1.next();
            int j = chunk.xPosition * 16;
            int k = chunk.zPosition * 16;
            this.theProfiler.endStartSection("checkNextLight");
            chunk.enqueueRelightChecks();
            this.theProfiler.endStartSection("tickChunk");
            chunk.onTick(false);
            this.theProfiler.endStartSection("thunder");
            if (flag && flag1 && this.rand.nextInt(100000) == 0) {
               this.updateLCG = this.updateLCG * 3 + 1013904223;
               int l = this.updateLCG >> 2;
               BlockPos blockposition = this.adjustPosToNearbyEntity(new BlockPos(j + (l & 15), 0, k + (l >> 8 & 15)));
               if (this.isRainingAt(blockposition)) {
                  DifficultyInstance difficultydamagescaler = this.getDifficultyForLocation(blockposition);
                  if (this.rand.nextDouble() < (double)difficultydamagescaler.getAdditionalDifficulty() * 0.05D) {
                     EntityHorse entityhorse = new EntityHorse(this);
                     entityhorse.setType(HorseType.SKELETON);
                     entityhorse.setSkeletonTrap(true);
                     entityhorse.setGrowingAge(0);
                     entityhorse.setPosition((double)blockposition.getX(), (double)blockposition.getY(), (double)blockposition.getZ());
                     this.addEntity(entityhorse, SpawnReason.LIGHTNING);
                     this.addWeatherEffect(new EntityLightningBolt(this, (double)blockposition.getX(), (double)blockposition.getY(), (double)blockposition.getZ(), true));
                  } else {
                     this.addWeatherEffect(new EntityLightningBolt(this, (double)blockposition.getX(), (double)blockposition.getY(), (double)blockposition.getZ(), false));
                  }
               }
            }

            this.theProfiler.endStartSection("iceandsnow");
            if (this.rand.nextInt(16) == 0) {
               this.updateLCG = this.updateLCG * 3 + 1013904223;
               int l = this.updateLCG >> 2;
               BlockPos blockposition = this.getPrecipitationHeight(new BlockPos(j + (l & 15), 0, k + (l >> 8 & 15)));
               BlockPos blockposition1 = blockposition.down();
               if (this.canBlockFreezeNoWater(blockposition1)) {
                  BlockState blockState = this.getWorld().getBlockAt(blockposition1.getX(), blockposition1.getY(), blockposition1.getZ()).getState();
                  blockState.setTypeId(Block.getIdFromBlock(Blocks.ICE));
                  BlockFormEvent iceBlockForm = new BlockFormEvent(blockState.getBlock(), blockState);
                  this.getServer().getPluginManager().callEvent(iceBlockForm);
                  if (!iceBlockForm.isCancelled()) {
                     blockState.update(true);
                  }
               }

               if (flag && this.canSnowAt(blockposition, true)) {
                  BlockState blockState = this.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()).getState();
                  blockState.setTypeId(Block.getIdFromBlock(Blocks.SNOW_LAYER));
                  BlockFormEvent snow = new BlockFormEvent(blockState.getBlock(), blockState);
                  this.getServer().getPluginManager().callEvent(snow);
                  if (!snow.isCancelled()) {
                     blockState.update(true);
                  }
               }

               if (flag && this.getBiome(blockposition1).canRain()) {
                  this.getBlockState(blockposition1).getBlock().fillWithRain(this, blockposition1);
               }
            }

            this.theProfiler.endStartSection("tickBlocks");
            if (i > 0) {
               for(ExtendedBlockStorage chunksection : chunk.getBlockStorageArray()) {
                  if (chunksection != Chunk.NULL_BLOCK_STORAGE && chunksection.getNeedsRandomTick()) {
                     for(int k1 = 0; k1 < i; ++k1) {
                        this.updateLCG = this.updateLCG * 3 + 1013904223;
                        int l1 = this.updateLCG >> 2;
                        int i2 = l1 & 15;
                        int j2 = l1 >> 8 & 15;
                        int k2 = l1 >> 16 & 15;
                        IBlockState iblockdata = chunksection.get(i2, k2, j2);
                        Block block = iblockdata.getBlock();
                        this.theProfiler.startSection("randomTick");
                        if (block.getTickRandomly()) {
                           block.randomTick(this, new BlockPos(i2 + j, k2 + chunksection.getYLocation(), j2 + k), iblockdata, this.rand);
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

   protected BlockPos adjustPosToNearbyEntity(BlockPos blockposition) {
      BlockPos blockposition1 = this.getPrecipitationHeight(blockposition);
      AxisAlignedBB axisalignedbb = (new AxisAlignedBB(blockposition1, new BlockPos(blockposition1.getX(), this.getHeight(), blockposition1.getZ()))).expandXyz(3.0D);
      List list = this.getEntitiesWithinAABB(EntityLivingBase.class, axisalignedbb, new Predicate() {
         public boolean apply(@Nullable EntityLivingBase entityliving) {
            return entityliving != null && entityliving.isEntityAlive() && WorldServer.this.canSeeSky(entityliving.getPosition());
         }

         public boolean apply(Object object) {
            return this.apply((EntityLivingBase)object);
         }
      });
      if (!list.isEmpty()) {
         return ((EntityLivingBase)list.get(this.rand.nextInt(list.size()))).getPosition();
      } else {
         if (blockposition1.getY() == -1) {
            blockposition1 = blockposition1.up(2);
         }

         return blockposition1;
      }
   }

   public boolean isBlockTickPending(BlockPos blockposition, Block block) {
      NextTickListEntry nextticklistentry = new NextTickListEntry(blockposition, block);
      return this.pendingTickListEntriesThisTick.contains(nextticklistentry);
   }

   public boolean isUpdateScheduled(BlockPos blockposition, Block block) {
      NextTickListEntry nextticklistentry = new NextTickListEntry(blockposition, block);
      return this.pendingTickListEntriesTreeSet.contains(nextticklistentry);
   }

   public void scheduleUpdate(BlockPos blockposition, Block block, int i) {
      this.updateBlockTick(blockposition, block, i, 0);
   }

   public void updateBlockTick(BlockPos blockposition, Block block, int i, int j) {
      if (blockposition instanceof BlockPos.MutableBlockPos || blockposition instanceof BlockPos.PooledMutableBlockPos) {
         blockposition = new BlockPos(blockposition);
         LogManager.getLogger().warn("Tried to assign a mutable BlockPos to tick data...", new Error(blockposition.getClass().toString()));
      }

      net.minecraft.block.material.Material material = block.getDefaultState().getMaterial();
      if (this.scheduledUpdatesAreImmediate && material != net.minecraft.block.material.Material.AIR) {
         if (block.requiresUpdates()) {
            if (this.isAreaLoaded(blockposition.add(-8, -8, -8), blockposition.add(8, 8, 8))) {
               IBlockState iblockdata = this.getBlockState(blockposition);
               if (iblockdata.getMaterial() != net.minecraft.block.material.Material.AIR && iblockdata.getBlock() == block) {
                  iblockdata.getBlock().updateTick(this, blockposition, iblockdata, this.rand);
               }
            }

            return;
         }

         i = 1;
      }

      NextTickListEntry nextticklistentry = new NextTickListEntry(blockposition, block);
      if (this.isBlockLoaded(blockposition)) {
         if (material != net.minecraft.block.material.Material.AIR) {
            nextticklistentry.setScheduledTime((long)i + this.worldInfo.getWorldTotalTime());
            nextticklistentry.setPriority(j);
         }

         if (!this.pendingTickListEntriesTreeSet.contains(nextticklistentry)) {
            this.pendingTickListEntriesTreeSet.add(nextticklistentry);
         }
      }

   }

   public void scheduleBlockUpdate(BlockPos blockposition, Block block, int i, int j) {
      if (blockposition instanceof BlockPos.MutableBlockPos || blockposition instanceof BlockPos.PooledMutableBlockPos) {
         blockposition = new BlockPos(blockposition);
         LogManager.getLogger().warn("Tried to assign a mutable BlockPos to tick data...", new Error(blockposition.getClass().toString()));
      }

      NextTickListEntry nextticklistentry = new NextTickListEntry(blockposition, block);
      nextticklistentry.setPriority(j);
      net.minecraft.block.material.Material material = block.getDefaultState().getMaterial();
      if (material != net.minecraft.block.material.Material.AIR) {
         nextticklistentry.setScheduledTime((long)i + this.worldInfo.getWorldTotalTime());
      }

      if (!this.pendingTickListEntriesTreeSet.contains(nextticklistentry)) {
         this.pendingTickListEntriesTreeSet.add(nextticklistentry);
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
               CrashReportCategory crashreportsystemdetails = crashreport.makeCategory("Player being ticked");
               entity.addEntityCrashInfo(crashreportsystemdetails);
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

   public boolean tickUpdates(boolean flag) {
      if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) {
         return false;
      } else {
         int i = this.pendingTickListEntriesTreeSet.size();
         if (i > 65536) {
            if (i > 1310720) {
               i /= 20;
            } else {
               i = 65536;
            }
         }

         this.theProfiler.startSection("cleaning");

         for(int j = 0; j < i; ++j) {
            NextTickListEntry nextticklistentry = (NextTickListEntry)this.pendingTickListEntriesTreeSet.first();
            if (!flag && nextticklistentry.scheduledTime > this.worldInfo.getWorldTotalTime()) {
               break;
            }

            this.pendingTickListEntriesTreeSet.remove(nextticklistentry);
            this.pendingTickListEntriesThisTick.add(nextticklistentry);
         }

         this.theProfiler.endSection();
         this.theProfiler.startSection("ticking");
         Iterator iterator = this.pendingTickListEntriesThisTick.iterator();

         while(iterator.hasNext()) {
            NextTickListEntry nextticklistentry = (NextTickListEntry)iterator.next();
            iterator.remove();
            if (this.isAreaLoaded(nextticklistentry.position.add(0, 0, 0), nextticklistentry.position.add(0, 0, 0))) {
               IBlockState iblockdata = this.getBlockState(nextticklistentry.position);
               if (iblockdata.getMaterial() != net.minecraft.block.material.Material.AIR && Block.isEqualTo(iblockdata.getBlock(), nextticklistentry.getBlock())) {
                  try {
                     iblockdata.getBlock().updateTick(this, nextticklistentry.position, iblockdata, this.rand);
                  } catch (Throwable var9) {
                     CrashReport crashreport = CrashReport.makeCrashReport(var9, "Exception while ticking a block");
                     CrashReportCategory crashreportsystemdetails = crashreport.makeCategory("Block being ticked");
                     CrashReportCategory.addBlockInfo(crashreportsystemdetails, nextticklistentry.position, iblockdata);
                     throw new ReportedException(crashreport);
                  }
               }
            } else {
               this.scheduleUpdate(nextticklistentry.position, nextticklistentry.getBlock(), 0);
            }
         }

         this.theProfiler.endSection();
         this.pendingTickListEntriesThisTick.clear();
         return !this.pendingTickListEntriesTreeSet.isEmpty();
      }
   }

   @Nullable
   public List getPendingBlockUpdates(Chunk chunk, boolean flag) {
      ChunkPos chunkcoordintpair = chunk.getChunkCoordIntPair();
      int i = (chunkcoordintpair.chunkXPos << 4) - 2;
      int j = i + 16 + 2;
      int k = (chunkcoordintpair.chunkZPos << 4) - 2;
      int l = k + 16 + 2;
      return this.getPendingBlockUpdates(new StructureBoundingBox(i, 0, k, j, 256, l), flag);
   }

   @Nullable
   public List getPendingBlockUpdates(StructureBoundingBox structureboundingbox, boolean flag) {
      ArrayList arraylist = null;

      for(int i = 0; i < 2; ++i) {
         Iterator iterator;
         if (i == 0) {
            iterator = this.pendingTickListEntriesTreeSet.iterator();
         } else {
            iterator = this.pendingTickListEntriesThisTick.iterator();
         }

         while(iterator.hasNext()) {
            NextTickListEntry nextticklistentry = (NextTickListEntry)iterator.next();
            BlockPos blockposition = nextticklistentry.position;
            if (blockposition.getX() >= structureboundingbox.minX && blockposition.getX() < structureboundingbox.maxX && blockposition.getZ() >= structureboundingbox.minZ && blockposition.getZ() < structureboundingbox.maxZ) {
               if (flag) {
                  iterator.remove();
               }

               if (arraylist == null) {
                  arraylist = Lists.newArrayList();
               }

               arraylist.add(nextticklistentry);
            }
         }
      }

      return arraylist;
   }

   private boolean canSpawnNPCs() {
      return this.mcServer.getSpawnNPCs();
   }

   private boolean canSpawnAnimals() {
      return this.mcServer.getSpawnAnimals();
   }

   protected IChunkProvider createChunkProvider() {
      IChunkLoader ichunkloader = this.saveHandler.getChunkLoader(this.provider);
      InternalChunkGenerator gen;
      if (this.generator != null) {
         gen = new CustomChunkGenerator(this, this.getSeed(), this.generator);
      } else if (this.provider instanceof WorldProviderHell) {
         gen = new NetherChunkGenerator(this, this.getSeed());
      } else if (this.provider instanceof WorldProviderEnd) {
         gen = new SkyLandsChunkGenerator(this, this.getSeed());
      } else {
         gen = new NormalChunkGenerator(this, this.getSeed());
      }

      return new ChunkProviderServer(this, ichunkloader, gen);
   }

   public List a(int i, int j, int k, int l, int i1, int j1) {
      ArrayList arraylist = Lists.newArrayList();

      for(int chunkX = i >> 4; chunkX <= l - 1 >> 4; ++chunkX) {
         for(int chunkZ = k >> 4; chunkZ <= j1 - 1 >> 4; ++chunkZ) {
            Chunk chunk = this.getChunkFromChunkCoords(chunkX, chunkZ);
            if (chunk != null) {
               for(Object te : chunk.chunkTileEntityMap.values()) {
                  TileEntity tileentity = (TileEntity)te;
                  if (tileentity.pos.getX() >= i && tileentity.pos.getY() >= j && tileentity.pos.getZ() >= k && tileentity.pos.getX() < l && tileentity.pos.getY() < i1 && tileentity.pos.getZ() < j1) {
                     arraylist.add(tileentity);
                  }
               }
            }
         }
      }

      return arraylist;
   }

   public boolean isBlockModifiable(EntityPlayer entityhuman, BlockPos blockposition) {
      return !this.mcServer.a(this, blockposition, entityhuman) && this.getWorldBorder().contains(blockposition);
   }

   public void initialize(WorldSettings worldsettings) {
      if (!this.worldInfo.isInitialized()) {
         try {
            this.createSpawnPosition(worldsettings);
            if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) {
               this.setDebugWorldSettings();
            }

            super.initialize(worldsettings);
         } catch (Throwable var5) {
            CrashReport crashreport = CrashReport.makeCrashReport(var5, "Exception initializing level");

            try {
               this.addWorldInfoToCrashReport(crashreport);
            } catch (Throwable var4) {
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

   private void createSpawnPosition(WorldSettings worldsettings) {
      if (!this.provider.canRespawnHere()) {
         this.worldInfo.setSpawn(BlockPos.ORIGIN.up(this.provider.getAverageGroundLevel()));
      } else if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) {
         this.worldInfo.setSpawn(BlockPos.ORIGIN.up());
      } else {
         this.findingSpawnPoint = true;
         BiomeProvider worldchunkmanager = this.provider.getBiomeProvider();
         List list = worldchunkmanager.getBiomesToSpawnIn();
         Random random = new Random(this.getSeed());
         BlockPos blockposition = worldchunkmanager.findBiomePosition(0, 0, 256, list, random);
         int i = 8;
         int j = this.provider.getAverageGroundLevel();
         int k = 8;
         if (this.generator != null) {
            Random rand = new Random(this.getSeed());
            Location spawn = this.generator.getFixedSpawnLocation(this.getWorld(), rand);
            if (spawn != null) {
               if (spawn.getWorld() != this.getWorld()) {
                  throw new IllegalStateException("Cannot set spawn point for " + this.worldInfo.getWorldName() + " to be in another world (" + spawn.getWorld().getName() + ")");
               }

               this.worldInfo.setSpawn(new BlockPos(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ()));
               this.findingSpawnPoint = false;
               return;
            }
         }

         if (blockposition != null) {
            i = blockposition.getX();
            k = blockposition.getZ();
         } else {
            LOGGER.warn("Unable to find spawn biome");
         }

         int l = 0;

         while(!this.canSpawn(i, k)) {
            i += random.nextInt(64) - random.nextInt(64);
            k += random.nextInt(64) - random.nextInt(64);
            ++l;
            if (l == 1000) {
               break;
            }
         }

         this.worldInfo.setSpawn(new BlockPos(i, j, k));
         this.findingSpawnPoint = false;
         if (worldsettings.isBonusChestEnabled()) {
            this.createBonusChest();
         }
      }

   }

   protected void createBonusChest() {
      WorldGeneratorBonusChest worldgenbonuschest = new WorldGeneratorBonusChest();

      for(int i = 0; i < 10; ++i) {
         int j = this.worldInfo.getSpawnX() + this.rand.nextInt(6) - this.rand.nextInt(6);
         int k = this.worldInfo.getSpawnZ() + this.rand.nextInt(6) - this.rand.nextInt(6);
         BlockPos blockposition = this.getTopSolidOrLiquidBlock(new BlockPos(j, 0, k)).up();
         if (worldgenbonuschest.generate(this, this.rand, blockposition)) {
            break;
         }
      }

   }

   public BlockPos getSpawnCoordinate() {
      return this.provider.getSpawnCoordinate();
   }

   public void saveAllChunks(boolean flag, @Nullable IProgressUpdate iprogressupdate) throws MinecraftException {
      ChunkProviderServer chunkproviderserver = this.getChunkProvider();
      if (chunkproviderserver.canSave()) {
         Bukkit.getPluginManager().callEvent(new WorldSaveEvent(this.getWorld()));
         if (iprogressupdate != null) {
            iprogressupdate.displaySavingString("Saving level");
         }

         this.saveLevel();
         if (iprogressupdate != null) {
            iprogressupdate.displayLoadingString("Saving chunks");
         }

         chunkproviderserver.saveChunks(flag);

         for(Chunk chunk : chunkproviderserver.getLoadedChunks()) {
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

      for(WorldServer worldserver : this.mcServer.worldServer) {
         if (worldserver instanceof WorldServerMulti) {
            ((WorldServerMulti)worldserver).saveAdditionalData();
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

   public boolean addEntity(Entity entity, SpawnReason spawnReason) {
      return this.canAddEntity(entity) ? super.addEntity(entity, spawnReason) : false;
   }

   public void loadEntities(Collection collection) {
      for(Entity entity : Lists.newArrayList(collection)) {
         if (this.canAddEntity(entity)) {
            this.loadedEntityList.add(entity);
            this.onEntityAdded(entity);
         }
      }

   }

   private boolean canAddEntity(Entity entity) {
      if (entity.isDead) {
         return false;
      } else {
         UUID uuid = entity.getUniqueID();
         if (this.entitiesByUuid.containsKey(uuid)) {
            Entity entity1 = (Entity)this.entitiesByUuid.get(uuid);
            if (this.unloadedEntityList.contains(entity1)) {
               this.unloadedEntityList.remove(entity1);
            } else {
               if (!(entity instanceof EntityPlayer)) {
                  return false;
               }

               LOGGER.warn("Force-added player with duplicate UUID {}", new Object[]{uuid.toString()});
            }

            this.removeEntityDangerously(entity1);
         }

         return true;
      }
   }

   protected void onEntityAdded(Entity entity) {
      super.onEntityAdded(entity);
      this.entitiesById.addKey(entity.getEntityId(), entity);
      this.entitiesByUuid.put(entity.getUniqueID(), entity);
      Entity[] aentity = entity.getParts();
      if (aentity != null) {
         for(Entity entity1 : aentity) {
            this.entitiesById.addKey(entity1.getEntityId(), entity1);
         }
      }

   }

   protected void onEntityRemoved(Entity entity) {
      super.onEntityRemoved(entity);
      this.entitiesById.removeObject(entity.getEntityId());
      this.entitiesByUuid.remove(entity.getUniqueID());
      Entity[] aentity = entity.getParts();
      if (aentity != null) {
         for(Entity entity1 : aentity) {
            this.entitiesById.removeObject(entity1.getEntityId());
         }
      }

   }

   public boolean addWeatherEffect(Entity entity) {
      LightningStrikeEvent lightning = new LightningStrikeEvent(this.getWorld(), (LightningStrike)entity.getBukkitEntity());
      this.getServer().getPluginManager().callEvent(lightning);
      if (lightning.isCancelled()) {
         return false;
      } else if (super.addWeatherEffect(entity)) {
         this.mcServer.getPlayerList().sendToAllNearExcept((EntityPlayer)null, entity.posX, entity.posY, entity.posZ, 512.0D, this.dimension, new SPacketSpawnGlobalEntity(entity));
         return true;
      } else {
         return false;
      }
   }

   public void setEntityState(Entity entity, byte b0) {
      this.getEntityTracker().sendToTrackingAndSelf(entity, new SPacketEntityStatus(entity, b0));
   }

   public ChunkProviderServer getChunkProvider() {
      return (ChunkProviderServer)super.getChunkProvider();
   }

   public Explosion newExplosion(@Nullable Entity entity, double d0, double d1, double d2, float f, boolean flag, boolean flag1) {
      Explosion explosion = super.newExplosion(entity, d0, d1, d2, f, flag, flag1);
      if (explosion.wasCanceled) {
         return explosion;
      } else {
         if (!flag1) {
            explosion.clearAffectedBlockPositions();
         }

         for(EntityPlayer entityhuman : this.playerEntities) {
            if (entityhuman.getDistanceSq(d0, d1, d2) < 4096.0D) {
               ((EntityPlayerMP)entityhuman).connection.sendPacket(new SPacketExplosion(d0, d1, d2, f, explosion.getAffectedBlockPositions(), (Vec3d)explosion.getPlayerKnockbackMap().get(entityhuman)));
            }
         }

         return explosion;
      }
   }

   public void addBlockEvent(BlockPos blockposition, Block block, int i, int j) {
      BlockEventData blockactiondata = new BlockEventData(blockposition, block, i, j);

      for(BlockEventData blockactiondata1 : this.blockEventQueue[this.blockEventCacheIndex]) {
         if (blockactiondata1.equals(blockactiondata)) {
            return;
         }
      }

      this.blockEventQueue[this.blockEventCacheIndex].add(blockactiondata);
   }

   private void sendQueuedBlockEvents() {
      while(!this.blockEventQueue[this.blockEventCacheIndex].isEmpty()) {
         int i = this.blockEventCacheIndex;
         this.blockEventCacheIndex ^= 1;

         for(BlockEventData blockactiondata : this.blockEventQueue[i]) {
            if (this.fireBlockEvent(blockactiondata)) {
               this.mcServer.getPlayerList().sendToAllNearExcept((EntityPlayer)null, (double)blockactiondata.getPosition().getX(), (double)blockactiondata.getPosition().getY(), (double)blockactiondata.getPosition().getZ(), 64.0D, this.dimension, new SPacketBlockAction(blockactiondata.getPosition(), blockactiondata.getBlock(), blockactiondata.getEventID(), blockactiondata.getEventParameter()));
            }
         }

         this.blockEventQueue[i].clear();
      }

   }

   private boolean fireBlockEvent(BlockEventData blockactiondata) {
      IBlockState iblockdata = this.getBlockState(blockactiondata.getPosition());
      return iblockdata.getBlock() == blockactiondata.getBlock() ? iblockdata.onBlockEventReceived(this, blockactiondata.getPosition(), blockactiondata.getEventID(), blockactiondata.getEventParameter()) : false;
   }

   public void flush() {
      this.saveHandler.flush();
   }

   protected void updateWeather() {
      boolean flag = this.isRaining();
      super.updateWeather();
      if (flag != this.isRaining()) {
         for(int i = 0; i < this.playerEntities.size(); ++i) {
            if (((EntityPlayerMP)this.playerEntities.get(i)).world == this) {
               ((EntityPlayerMP)this.playerEntities.get(i)).setPlayerWeather(!flag ? WeatherType.DOWNFALL : WeatherType.CLEAR, false);
            }
         }
      }

      for(int i = 0; i < this.playerEntities.size(); ++i) {
         if (((EntityPlayerMP)this.playerEntities.get(i)).world == this) {
            ((EntityPlayerMP)this.playerEntities.get(i)).updateWeather(this.prevRainingStrength, this.rainingStrength, this.prevThunderingStrength, this.thunderingStrength);
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

   public void spawnParticle(EnumParticleTypes enumparticle, double d0, double d1, double d2, int i, double d3, double d4, double d5, double d6, int... aint) {
      this.spawnParticle(enumparticle, false, d0, d1, d2, i, d3, d4, d5, d6, aint);
   }

   public void spawnParticle(EnumParticleTypes enumparticle, boolean flag, double d0, double d1, double d2, int i, double d3, double d4, double d5, double d6, int... aint) {
      this.sendParticles((EntityPlayerMP)null, enumparticle, flag, d0, d1, d2, i, d3, d4, d5, d6, aint);
   }

   public void sendParticles(EntityPlayerMP sender, EnumParticleTypes enumparticle, boolean flag, double d0, double d1, double d2, int i, double d3, double d4, double d5, double d6, int... aint) {
      SPacketParticles packetplayoutworldparticles = new SPacketParticles(enumparticle, flag, (float)d0, (float)d1, (float)d2, (float)d3, (float)d4, (float)d5, (float)d6, i, aint);

      for(int j = 0; j < this.playerEntities.size(); ++j) {
         EntityPlayerMP entityplayer = (EntityPlayerMP)this.playerEntities.get(j);
         if (sender == null || entityplayer.getBukkitEntity().canSee(sender.getBukkitEntity())) {
            BlockPos blockposition = entityplayer.getPosition();
            blockposition.distanceSq(d0, d1, d2);
            this.sendPacketWithinDistance(entityplayer, flag, d0, d1, d2, packetplayoutworldparticles);
         }
      }

   }

   public void spawnParticle(EntityPlayerMP entityplayer, EnumParticleTypes enumparticle, boolean flag, double d0, double d1, double d2, int i, double d3, double d4, double d5, double d6, int... aint) {
      SPacketParticles packetplayoutworldparticles = new SPacketParticles(enumparticle, flag, (float)d0, (float)d1, (float)d2, (float)d3, (float)d4, (float)d5, (float)d6, i, aint);
      this.sendPacketWithinDistance(entityplayer, flag, d0, d1, d2, packetplayoutworldparticles);
   }

   private void sendPacketWithinDistance(EntityPlayerMP entityplayer, boolean flag, double d0, double d1, double d2, Packet packet) {
      BlockPos blockposition = entityplayer.getPosition();
      double d3 = blockposition.distanceSq(d0, d1, d2);
      if (d3 <= 1024.0D || flag && d3 <= 262144.0D) {
         entityplayer.connection.sendPacket(packet);
      }

   }

   @Nullable
   public Entity getEntityFromUuid(UUID uuid) {
      return (Entity)this.entitiesByUuid.get(uuid);
   }

   public ListenableFuture addScheduledTask(Runnable runnable) {
      return this.mcServer.addScheduledTask(runnable);
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

      ServerBlockEventList(Object object) {
         this();
      }
   }
}
