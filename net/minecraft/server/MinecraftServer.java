package net.minecraft.server;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Bootstrap;
import net.minecraft.network.NetworkSystem;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.profiler.ISnooperInfo;
import net.minecraft.profiler.Profiler;
import net.minecraft.profiler.Snooper;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ITickable;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.demo.DemoWorldServer;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.chunkio.ChunkIOExecutor;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.StartupQuery;
import net.minecraftforge.fml.common.StartupQuery.AbortedException;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class MinecraftServer implements Runnable, ICommandSender, IThreadListener, ISnooperInfo {
   private static final Logger LOG = LogManager.getLogger();
   public static final File USER_CACHE_FILE = new File("usercache.json");
   private final ISaveFormat anvilConverterForAnvilFile;
   private final Snooper usageSnooper = new Snooper("server", this, getCurrentTimeMillis());
   private final File anvilFile;
   private final List tickables = Lists.newArrayList();
   public final ICommandManager commandManager;
   public final Profiler theProfiler = new Profiler();
   private final NetworkSystem networkSystem;
   private final ServerStatusResponse statusResponse = new ServerStatusResponse();
   private final Random random = new Random();
   private final DataFixer dataFixer;
   @SideOnly(Side.SERVER)
   private String hostname;
   private int serverPort = -1;
   public WorldServer[] worlds = new WorldServer[0];
   private PlayerList playerList;
   private boolean serverRunning = true;
   private boolean serverStopped;
   private int tickCounter;
   protected final Proxy serverProxy;
   public String currentTask;
   public int percentDone;
   private boolean onlineMode;
   private boolean canSpawnAnimals;
   private boolean canSpawnNPCs;
   private boolean pvpEnabled;
   private boolean allowFlight;
   private String motd;
   private int buildLimit;
   private int maxPlayerIdleMinutes;
   public final long[] tickTimeArray = new long[100];
   public Hashtable worldTickTimes = new Hashtable();
   private KeyPair serverKeyPair;
   private String serverOwner;
   private String folderName;
   @SideOnly(Side.CLIENT)
   private String worldName;
   private boolean isDemo;
   private boolean enableBonusChest;
   private String resourcePackUrl = "";
   private String resourcePackHash = "";
   private boolean serverIsRunning;
   private long timeOfLastWarning;
   private String userMessage;
   private boolean startProfiling;
   private boolean isGamemodeForced;
   private final YggdrasilAuthenticationService authService;
   private final MinecraftSessionService sessionService;
   private final GameProfileRepository profileRepo;
   private final PlayerProfileCache profileCache;
   private long nanoTimeSinceStatusRefresh;
   public final Queue futureTaskQueue = Queues.newArrayDeque();
   private Thread serverThread;
   private long currentTime = getCurrentTimeMillis();
   @SideOnly(Side.CLIENT)
   private boolean worldIconSet;

   public MinecraftServer(File anvilFileIn, Proxy proxyIn, DataFixer dataFixerIn, YggdrasilAuthenticationService authServiceIn, MinecraftSessionService sessionServiceIn, GameProfileRepository profileRepoIn, PlayerProfileCache profileCacheIn) {
      this.serverProxy = proxyIn;
      this.authService = authServiceIn;
      this.sessionService = sessionServiceIn;
      this.profileRepo = profileRepoIn;
      this.profileCache = profileCacheIn;
      this.anvilFile = anvilFileIn;
      this.networkSystem = new NetworkSystem(this);
      this.commandManager = this.createCommandManager();
      this.anvilConverterForAnvilFile = new AnvilSaveConverter(anvilFileIn, dataFixerIn);
      this.dataFixer = dataFixerIn;
   }

   public ServerCommandManager createCommandManager() {
      return new ServerCommandManager(this);
   }

   public abstract boolean init() throws IOException;

   public void convertMapIfNeeded(String worldNameIn) {
      if (this.getActiveAnvilConverter().isOldMapFormat(worldNameIn)) {
         LOG.info("Converting map!");
         this.setUserMessage("menu.convertingLevel");
         this.getActiveAnvilConverter().convertMapFormat(worldNameIn, new IProgressUpdate() {
            private long startTime = System.currentTimeMillis();

            public void displaySavingString(String message) {
            }

            public void setLoadingProgress(int progress) {
               if (System.currentTimeMillis() - this.startTime >= 1000L) {
                  this.startTime = System.currentTimeMillis();
                  MinecraftServer.LOG.info("Converting... {}%", new Object[]{progress});
               }

            }

            @SideOnly(Side.CLIENT)
            public void resetProgressAndMessage(String message) {
            }

            @SideOnly(Side.CLIENT)
            public void setDoneWorking() {
            }

            public void displayLoadingString(String message) {
            }
         });
      }

   }

   protected synchronized void setUserMessage(String message) {
      this.userMessage = message;
   }

   @SideOnly(Side.CLIENT)
   public synchronized String getUserMessage() {
      return this.userMessage;
   }

   public void loadAllWorlds(String saveName, String worldNameIn, long seed, WorldType type, String generatorOptions) {
      this.convertMapIfNeeded(saveName);
      this.setUserMessage("menu.loadingLevel");
      ISaveHandler isavehandler = this.anvilConverterForAnvilFile.getSaveLoader(saveName, true);
      this.setResourcePackFromWorld(this.getFolderName(), isavehandler);
      WorldInfo worldinfo = isavehandler.loadWorldInfo();
      WorldSettings worldsettings;
      if (worldinfo == null) {
         if (this.isDemo()) {
            worldsettings = DemoWorldServer.DEMO_WORLD_SETTINGS;
         } else {
            worldsettings = new WorldSettings(seed, this.getGameType(), this.canStructuresSpawn(), this.isHardcore(), type);
            worldsettings.setGeneratorOptions(generatorOptions);
            if (this.enableBonusChest) {
               worldsettings.enableBonusChest();
            }
         }

         worldinfo = new WorldInfo(worldsettings, worldNameIn);
      } else {
         worldinfo.setWorldName(worldNameIn);
         worldsettings = new WorldSettings(worldinfo);
      }

      WorldServer overWorld = (WorldServer)(this.isDemo() ? (new DemoWorldServer(this, isavehandler, worldinfo, 0, this.theProfiler)).init() : (new WorldServer(this, isavehandler, worldinfo, 0, this.theProfiler)).init());
      overWorld.initialize(worldsettings);
      Integer[] var11 = DimensionManager.getStaticDimensionIDs();
      int var12 = var11.length;

      for(int var13 = 0; var13 < var12; ++var13) {
         int dim = var11[var13].intValue();
         WorldServer world = dim == 0 ? overWorld : (WorldServer)(new WorldServerMulti(this, isavehandler, dim, overWorld, this.theProfiler)).init();
         world.addEventListener(new ServerWorldEventHandler(this, world));
         if (!this.isSinglePlayer()) {
            world.getWorldInfo().setGameType(this.getGameType());
         }

         MinecraftForge.EVENT_BUS.post(new Load(world));
      }

      this.playerList.setPlayerManager(new WorldServer[]{overWorld});
      this.setDifficultyForAllWorlds(this.getDifficulty());
      this.initialWorldChunkLoad();
   }

   public void initialWorldChunkLoad() {
      int i = 16;
      int j = 4;
      int k = 192;
      int l = 625;
      int i1 = 0;
      this.setUserMessage("menu.generatingTerrain");
      int j1 = 0;
      LOG.info("Preparing start region for level 0");
      WorldServer worldserver = DimensionManager.getWorld(j1);
      BlockPos blockpos = worldserver.getSpawnPoint();
      long k1 = getCurrentTimeMillis();

      for(int l1 = -192; l1 <= 192 && this.isServerRunning(); l1 += 16) {
         for(int i2 = -192; i2 <= 192 && this.isServerRunning(); i2 += 16) {
            long j2 = getCurrentTimeMillis();
            if (j2 - k1 > 1000L) {
               this.outputPercentRemaining("Preparing spawn area", i1 * 100 / 625);
               k1 = j2;
            }

            ++i1;
            worldserver.getChunkProvider().provideChunk(blockpos.getX() + l1 >> 4, blockpos.getZ() + i2 >> 4);
         }
      }

      this.clearCurrentTask();
   }

   public void setResourcePackFromWorld(String worldNameIn, ISaveHandler saveHandlerIn) {
      File file1 = new File(saveHandlerIn.getWorldDirectory(), "resources.zip");
      if (file1.isFile()) {
         this.setResourcePack("level://" + worldNameIn + "/resources.zip", "");
      }

   }

   public abstract boolean canStructuresSpawn();

   public abstract GameType getGameType();

   public abstract EnumDifficulty getDifficulty();

   public abstract boolean isHardcore();

   public abstract int getOpPermissionLevel();

   public abstract boolean shouldBroadcastRconToOps();

   public abstract boolean shouldBroadcastConsoleToOps();

   protected void outputPercentRemaining(String message, int percent) {
      this.currentTask = message;
      this.percentDone = percent;
      LOG.info("{}: {}%", new Object[]{message, percent});
   }

   protected void clearCurrentTask() {
      this.currentTask = null;
      this.percentDone = 0;
   }

   public void saveAllWorlds(boolean isSilent) {
      for(WorldServer worldserver : this.worlds) {
         if (worldserver != null) {
            if (!isSilent) {
               LOG.info("Saving chunks for level '{}'/{}", new Object[]{worldserver.getWorldInfo().getWorldName(), worldserver.provider.getDimensionType().getName()});
            }

            try {
               worldserver.saveAllChunks(true, (IProgressUpdate)null);
            } catch (MinecraftException var7) {
               LOG.warn(var7.getMessage());
            }
         }
      }

   }

   public void stopServer() {
      LOG.info("Stopping server");
      if (this.getNetworkSystem() != null) {
         this.getNetworkSystem().terminateEndpoints();
      }

      if (this.playerList != null) {
         LOG.info("Saving players");
         this.playerList.saveAllPlayerData();
         this.playerList.removeAllPlayers();
      }

      if (this.worlds != null) {
         LOG.info("Saving worlds");

         for(WorldServer worldserver : this.worlds) {
            if (worldserver != null) {
               worldserver.disableLevelSaving = false;
            }
         }

         this.saveAllWorlds(false);

         for(WorldServer worldserver1 : this.worlds) {
            if (worldserver1 != null) {
               MinecraftForge.EVENT_BUS.post(new Unload(worldserver1));
               worldserver1.flush();
            }
         }

         WorldServer[] tmp = this.worlds;

         for(WorldServer world : tmp) {
            DimensionManager.setWorld(world.provider.getDimension(), (WorldServer)null, this);
         }
      }

      if (this.usageSnooper.isSnooperRunning()) {
         this.usageSnooper.stopSnooper();
      }

   }

   public boolean isServerRunning() {
      return this.serverRunning;
   }

   public void initiateShutdown() {
      this.serverRunning = false;
   }

   public void run() {
      try {
         if (this.init()) {
            FMLCommonHandler.instance().handleServerStarted();
            this.currentTime = getCurrentTimeMillis();
            long i = 0L;
            this.statusResponse.setServerDescription(new TextComponentString(this.motd));
            this.statusResponse.setVersion(new ServerStatusResponse.Version("1.10.2", 210));
            this.applyServerIconToResponse(this.statusResponse);

            while(this.serverRunning) {
               long k = getCurrentTimeMillis();
               long j = k - this.currentTime;
               if (j > 2000L && this.currentTime - this.timeOfLastWarning >= 15000L) {
                  LOG.warn("Can't keep up! Did the system time change, or is the server overloaded? Running {}ms behind, skipping {} tick(s)", new Object[]{j, j / 50L});
                  j = 2000L;
                  this.timeOfLastWarning = this.currentTime;
               }

               if (j < 0L) {
                  LOG.warn("Time ran backwards! Did the system time change?");
                  j = 0L;
               }

               i += j;
               this.currentTime = k;
               if (this.worlds[0].areAllPlayersAsleep()) {
                  this.tick();
                  i = 0L;
               } else {
                  while(i > 50L) {
                     i -= 50L;
                     this.tick();
                  }
               }

               Thread.sleep(Math.max(1L, 50L - i));
               this.serverIsRunning = true;
            }

            FMLCommonHandler.instance().handleServerStopping();
            FMLCommonHandler.instance().expectServerStopped();
         } else {
            FMLCommonHandler.instance().expectServerStopped();
            this.finalTick((CrashReport)null);
         }
      } catch (AbortedException var70) {
         FMLCommonHandler.instance().expectServerStopped();
      } catch (Throwable var71) {
         LOG.error("Encountered an unexpected exception", var71);
         CrashReport crashreport = null;
         if (var71 instanceof ReportedException) {
            crashreport = this.addServerInfoToCrashReport(((ReportedException)var71).getCrashReport());
         } else {
            crashreport = this.addServerInfoToCrashReport(new CrashReport("Exception in server tick loop", var71));
         }

         File file1 = new File(new File(this.getDataDirectory(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");
         if (crashreport.saveToFile(file1)) {
            LOG.error("This crash report has been saved to: {}", new Object[]{file1.getAbsolutePath()});
         } else {
            LOG.error("We were unable to save this crash report to disk.");
         }

         FMLCommonHandler.instance().expectServerStopped();
         this.finalTick(crashreport);
      } finally {
         try {
            this.stopServer();
            this.serverStopped = true;
         } catch (Throwable var68) {
            LOG.error("Exception stopping the server", var68);
         } finally {
            FMLCommonHandler.instance().handleServerStopped();
            this.serverStopped = true;
            this.systemExitNow();
         }

      }

   }

   public void applyServerIconToResponse(ServerStatusResponse response) {
      File file1 = this.getFile("server-icon.png");
      if (!file1.exists()) {
         file1 = this.getActiveAnvilConverter().getFile(this.getFolderName(), "icon.png");
      }

      if (file1.isFile()) {
         ByteBuf bytebuf = Unpooled.buffer();

         try {
            BufferedImage bufferedimage = ImageIO.read(file1);
            Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide", new Object[0]);
            Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high", new Object[0]);
            ImageIO.write(bufferedimage, "PNG", new ByteBufOutputStream(bytebuf));
            ByteBuf bytebuf1 = Base64.encode(bytebuf);
            response.setFavicon("data:image/png;base64," + bytebuf1.toString(Charsets.UTF_8));
         } catch (Exception var9) {
            LOG.error("Couldn't load server icon", var9);
         } finally {
            bytebuf.release();
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public boolean isWorldIconSet() {
      this.worldIconSet = this.worldIconSet || this.getWorldIconFile().isFile();
      return this.worldIconSet;
   }

   @SideOnly(Side.CLIENT)
   public File getWorldIconFile() {
      return this.getActiveAnvilConverter().getFile(this.getFolderName(), "icon.png");
   }

   public File getDataDirectory() {
      return new File(".");
   }

   public void finalTick(CrashReport report) {
   }

   public void systemExitNow() {
   }

   public void tick() {
      long i = System.nanoTime();
      FMLCommonHandler.instance().onPreServerTick();
      ++this.tickCounter;
      if (this.startProfiling) {
         this.startProfiling = false;
         this.theProfiler.profilingEnabled = true;
         this.theProfiler.clearProfiling();
      }

      this.theProfiler.startSection("root");
      this.updateTimeLightAndEntities();
      if (i - this.nanoTimeSinceStatusRefresh >= 5000000000L) {
         this.nanoTimeSinceStatusRefresh = i;
         this.statusResponse.setPlayers(new ServerStatusResponse.Players(this.getMaxPlayers(), this.getCurrentPlayerCount()));
         GameProfile[] agameprofile = new GameProfile[Math.min(this.getCurrentPlayerCount(), 12)];
         int j = MathHelper.getInt(this.random, 0, this.getCurrentPlayerCount() - agameprofile.length);

         for(int k = 0; k < agameprofile.length; ++k) {
            agameprofile[k] = ((EntityPlayerMP)this.playerList.getPlayers().get(j + k)).getGameProfile();
         }

         Collections.shuffle(Arrays.asList(agameprofile));
         this.statusResponse.getPlayers().setPlayers(agameprofile);
         this.statusResponse.invalidateJson();
      }

      if (this.tickCounter % 900 == 0) {
         this.theProfiler.startSection("save");
         this.playerList.saveAllPlayerData();
         this.saveAllWorlds(true);
         this.theProfiler.endSection();
      }

      this.theProfiler.startSection("tallying");
      this.tickTimeArray[this.tickCounter % 100] = System.nanoTime() - i;
      this.theProfiler.endSection();
      this.theProfiler.startSection("snooper");
      if (!this.usageSnooper.isSnooperRunning() && this.tickCounter > 100) {
         this.usageSnooper.startSnooper();
      }

      if (this.tickCounter % 6000 == 0) {
         this.usageSnooper.addMemoryStatsToSnooper();
      }

      this.theProfiler.endSection();
      this.theProfiler.endSection();
      FMLCommonHandler.instance().onPostServerTick();
   }

   public void updateTimeLightAndEntities() {
      this.theProfiler.startSection("jobs");
      synchronized(this.futureTaskQueue) {
         while(!this.futureTaskQueue.isEmpty()) {
            Util.runTask((FutureTask)this.futureTaskQueue.poll(), LOG);
         }
      }

      this.theProfiler.endStartSection("levels");
      ChunkIOExecutor.tick();
      Integer[] ids = DimensionManager.getIDs(this.tickCounter % 200 == 0);

      for(int x = 0; x < ids.length; ++x) {
         int id = ids[x].intValue();
         long i = System.nanoTime();
         if (id == 0 || this.getAllowNether()) {
            WorldServer worldserver = DimensionManager.getWorld(id);
            this.theProfiler.startSection(worldserver.getWorldInfo().getWorldName());
            if (this.tickCounter % 20 == 0) {
               this.theProfiler.startSection("timeSync");
               this.playerList.sendPacketToAllPlayersInDimension(new SPacketTimeUpdate(worldserver.getTotalWorldTime(), worldserver.getWorldTime(), worldserver.getGameRules().getBoolean("doDaylightCycle")), worldserver.provider.getDimension());
               this.theProfiler.endSection();
            }

            this.theProfiler.startSection("tick");
            FMLCommonHandler.instance().onPreWorldTick(worldserver);

            try {
               worldserver.tick();
            } catch (Throwable var10) {
               CrashReport crashreport = CrashReport.makeCrashReport(var10, "Exception ticking world");
               worldserver.addWorldInfoToCrashReport(crashreport);
               throw new ReportedException(crashreport);
            }

            try {
               worldserver.updateEntities();
            } catch (Throwable var9) {
               CrashReport crashreport1 = CrashReport.makeCrashReport(var9, "Exception ticking world entities");
               worldserver.addWorldInfoToCrashReport(crashreport1);
               throw new ReportedException(crashreport1);
            }

            FMLCommonHandler.instance().onPostWorldTick(worldserver);
            this.theProfiler.endSection();
            this.theProfiler.startSection("tracker");
            worldserver.getEntityTracker().tick();
            this.theProfiler.endSection();
            this.theProfiler.endSection();
         }

         ((long[])this.worldTickTimes.get(Integer.valueOf(id)))[this.tickCounter % 100] = System.nanoTime() - i;
      }

      this.theProfiler.endStartSection("dim_unloading");
      DimensionManager.unloadWorlds(this.worldTickTimes);
      this.theProfiler.endStartSection("connection");
      this.getNetworkSystem().networkTick();
      this.theProfiler.endStartSection("players");
      this.playerList.onTick();
      this.theProfiler.endStartSection("tickables");

      for(int k = 0; k < this.tickables.size(); ++k) {
         ((ITickable)this.tickables.get(k)).update();
      }

      this.theProfiler.endSection();
   }

   public boolean getAllowNether() {
      return true;
   }

   public void startServerThread() {
      StartupQuery.reset();
      this.serverThread = new Thread(this, "Server thread");
      this.serverThread.start();
   }

   public File getFile(String fileName) {
      return new File(this.getDataDirectory(), fileName);
   }

   public void logWarning(String msg) {
      LOG.warn(msg);
   }

   public WorldServer worldServerForDimension(int dimension) {
      WorldServer ret = DimensionManager.getWorld(dimension);
      if (ret == null) {
         DimensionManager.initDimension(dimension);
         ret = DimensionManager.getWorld(dimension);
      }

      return ret;
   }

   public String getMinecraftVersion() {
      return "1.10.2";
   }

   public int getCurrentPlayerCount() {
      return this.playerList.getCurrentPlayerCount();
   }

   public int getMaxPlayers() {
      return this.playerList.getMaxPlayers();
   }

   public String[] getOnlinePlayerNames() {
      return this.playerList.getOnlinePlayerNames();
   }

   public GameProfile[] getOnlinePlayerProfiles() {
      return this.playerList.getOnlinePlayerProfiles();
   }

   public String getServerModName() {
      return FMLCommonHandler.instance().getModName();
   }

   public CrashReport addServerInfoToCrashReport(CrashReport report) {
      report.getCategory().setDetail("Profiler Position", new ICrashReportDetail() {
         public String call() throws Exception {
            return MinecraftServer.this.theProfiler.profilingEnabled ? MinecraftServer.this.theProfiler.getNameOfLastSection() : "N/A (disabled)";
         }
      });
      if (this.playerList != null) {
         report.getCategory().setDetail("Player Count", new ICrashReportDetail() {
            public String call() {
               return MinecraftServer.this.playerList.getCurrentPlayerCount() + " / " + MinecraftServer.this.playerList.getMaxPlayers() + "; " + MinecraftServer.this.playerList.getPlayers();
            }
         });
      }

      return report;
   }

   public List getTabCompletions(ICommandSender sender, String input, @Nullable BlockPos pos, boolean hasTargetBlock) {
      List list = Lists.newArrayList();
      boolean flag = input.startsWith("/");
      if (flag) {
         input = input.substring(1);
      }

      if (!flag && !hasTargetBlock) {
         String[] astring = input.split(" ", -1);
         String s2 = astring[astring.length - 1];

         for(String s1 : this.playerList.getOnlinePlayerNames()) {
            if (CommandBase.doesStringStartWith(s2, s1)) {
               list.add(s1);
            }
         }

         return list;
      } else {
         boolean flag1 = !input.contains(" ");
         List list1 = this.commandManager.getTabCompletions(sender, input, pos);
         if (!list1.isEmpty()) {
            for(String s : list1) {
               if (flag1) {
                  list.add("/" + s);
               } else {
                  list.add(s);
               }
            }
         }

         return list;
      }
   }

   public boolean isAnvilFileSet() {
      return this.anvilFile != null;
   }

   public String getName() {
      return "Server";
   }

   public void sendMessage(ITextComponent component) {
      LOG.info(component.getUnformattedText());
   }

   public boolean canUseCommand(int permLevel, String commandName) {
      return true;
   }

   public ICommandManager getCommandManager() {
      return this.commandManager;
   }

   public KeyPair getKeyPair() {
      return this.serverKeyPair;
   }

   public String getServerOwner() {
      return this.serverOwner;
   }

   public void setServerOwner(String owner) {
      this.serverOwner = owner;
   }

   public boolean isSinglePlayer() {
      return this.serverOwner != null;
   }

   public String getFolderName() {
      return this.folderName;
   }

   public void setFolderName(String name) {
      this.folderName = name;
   }

   @SideOnly(Side.CLIENT)
   public void setWorldName(String worldNameIn) {
      this.worldName = worldNameIn;
   }

   @SideOnly(Side.CLIENT)
   public String getWorldName() {
      return this.worldName;
   }

   public void setKeyPair(KeyPair keyPair) {
      this.serverKeyPair = keyPair;
   }

   public void setDifficultyForAllWorlds(EnumDifficulty difficulty) {
      for(WorldServer worldserver : this.worlds) {
         if (worldserver != null) {
            if (worldserver.getWorldInfo().isHardcoreModeEnabled()) {
               worldserver.getWorldInfo().setDifficulty(EnumDifficulty.HARD);
               worldserver.setAllowedSpawnTypes(true, true);
            } else if (this.isSinglePlayer()) {
               worldserver.getWorldInfo().setDifficulty(difficulty);
               worldserver.setAllowedSpawnTypes(worldserver.getDifficulty() != EnumDifficulty.PEACEFUL, true);
            } else {
               worldserver.getWorldInfo().setDifficulty(difficulty);
               worldserver.setAllowedSpawnTypes(this.allowSpawnMonsters(), this.canSpawnAnimals);
            }
         }
      }

   }

   public boolean allowSpawnMonsters() {
      return true;
   }

   public boolean isDemo() {
      return this.isDemo;
   }

   public void setDemo(boolean demo) {
      this.isDemo = demo;
   }

   public void canCreateBonusChest(boolean enable) {
      this.enableBonusChest = enable;
   }

   public ISaveFormat getActiveAnvilConverter() {
      return this.anvilConverterForAnvilFile;
   }

   public String getResourcePackUrl() {
      return this.resourcePackUrl;
   }

   public String getResourcePackHash() {
      return this.resourcePackHash;
   }

   public void setResourcePack(String url, String hash) {
      this.resourcePackUrl = url;
      this.resourcePackHash = hash;
   }

   public void addServerStatsToSnooper(Snooper playerSnooper) {
      playerSnooper.addClientStat("whitelist_enabled", Boolean.valueOf(false));
      playerSnooper.addClientStat("whitelist_count", Integer.valueOf(0));
      if (this.playerList != null) {
         playerSnooper.addClientStat("players_current", Integer.valueOf(this.getCurrentPlayerCount()));
         playerSnooper.addClientStat("players_max", Integer.valueOf(this.getMaxPlayers()));
         playerSnooper.addClientStat("players_seen", Integer.valueOf(this.playerList.getAvailablePlayerDat().length));
      }

      playerSnooper.addClientStat("uses_auth", Boolean.valueOf(this.onlineMode));
      playerSnooper.addClientStat("gui_state", this.getGuiEnabled() ? "enabled" : "disabled");
      playerSnooper.addClientStat("run_time", Long.valueOf((getCurrentTimeMillis() - playerSnooper.getMinecraftStartTimeMillis()) / 60L * 1000L));
      playerSnooper.addClientStat("avg_tick_ms", Integer.valueOf((int)(MathHelper.average(this.tickTimeArray) * 1.0E-6D)));
      int i = 0;
      if (this.worlds != null) {
         for(WorldServer worldserver : this.worlds) {
            if (worldserver != null) {
               WorldInfo worldinfo = worldserver.getWorldInfo();
               playerSnooper.addClientStat("world[" + i + "][dimension]", Integer.valueOf(worldserver.provider.getDimensionType().getId()));
               playerSnooper.addClientStat("world[" + i + "][mode]", worldinfo.getGameType());
               playerSnooper.addClientStat("world[" + i + "][difficulty]", worldserver.getDifficulty());
               playerSnooper.addClientStat("world[" + i + "][hardcore]", Boolean.valueOf(worldinfo.isHardcoreModeEnabled()));
               playerSnooper.addClientStat("world[" + i + "][generator_name]", worldinfo.getTerrainType().getName());
               playerSnooper.addClientStat("world[" + i + "][generator_version]", Integer.valueOf(worldinfo.getTerrainType().getGeneratorVersion()));
               playerSnooper.addClientStat("world[" + i + "][height]", Integer.valueOf(this.buildLimit));
               playerSnooper.addClientStat("world[" + i + "][chunks_loaded]", Integer.valueOf(worldserver.getChunkProvider().getLoadedChunkCount()));
               ++i;
            }
         }
      }

      playerSnooper.addClientStat("worlds", Integer.valueOf(i));
   }

   public void addServerTypeToSnooper(Snooper playerSnooper) {
      playerSnooper.addStatToSnooper("singleplayer", Boolean.valueOf(this.isSinglePlayer()));
      playerSnooper.addStatToSnooper("server_brand", this.getServerModName());
      playerSnooper.addStatToSnooper("gui_supported", GraphicsEnvironment.isHeadless() ? "headless" : "supported");
      playerSnooper.addStatToSnooper("dedicated", Boolean.valueOf(this.isDedicatedServer()));
   }

   public boolean isSnooperEnabled() {
      return true;
   }

   public abstract boolean isDedicatedServer();

   public boolean isServerInOnlineMode() {
      return this.onlineMode;
   }

   public void setOnlineMode(boolean online) {
      this.onlineMode = online;
   }

   public boolean getCanSpawnAnimals() {
      return this.canSpawnAnimals;
   }

   public void setCanSpawnAnimals(boolean spawnAnimals) {
      this.canSpawnAnimals = spawnAnimals;
   }

   public boolean getCanSpawnNPCs() {
      return this.canSpawnNPCs;
   }

   public abstract boolean shouldUseNativeTransport();

   public void setCanSpawnNPCs(boolean spawnNpcs) {
      this.canSpawnNPCs = spawnNpcs;
   }

   public boolean isPVPEnabled() {
      return this.pvpEnabled;
   }

   public void setAllowPvp(boolean allowPvp) {
      this.pvpEnabled = allowPvp;
   }

   public boolean isFlightAllowed() {
      return this.allowFlight;
   }

   public void setAllowFlight(boolean allow) {
      this.allowFlight = allow;
   }

   public abstract boolean isCommandBlockEnabled();

   public String getMOTD() {
      return this.motd;
   }

   public void setMOTD(String motdIn) {
      this.motd = motdIn;
   }

   public int getBuildLimit() {
      return this.buildLimit;
   }

   public void setBuildLimit(int maxBuildHeight) {
      this.buildLimit = maxBuildHeight;
   }

   public boolean isServerStopped() {
      return this.serverStopped;
   }

   public PlayerList getPlayerList() {
      return this.playerList;
   }

   public void setPlayerList(PlayerList list) {
      this.playerList = list;
   }

   public void setGameType(GameType gameMode) {
      for(WorldServer worldserver : this.worlds) {
         worldserver.getWorldInfo().setGameType(gameMode);
      }

   }

   public NetworkSystem getNetworkSystem() {
      return this.networkSystem;
   }

   @SideOnly(Side.CLIENT)
   public boolean serverIsInRunLoop() {
      return this.serverIsRunning;
   }

   public boolean getGuiEnabled() {
      return false;
   }

   public abstract String shareToLAN(GameType var1, boolean var2);

   public int getTickCounter() {
      return this.tickCounter;
   }

   public void enableProfiling() {
      this.startProfiling = true;
   }

   @SideOnly(Side.CLIENT)
   public Snooper getPlayerUsageSnooper() {
      return this.usageSnooper;
   }

   public BlockPos getPosition() {
      return BlockPos.ORIGIN;
   }

   public Vec3d getPositionVector() {
      return Vec3d.ZERO;
   }

   public World getEntityWorld() {
      return this.worlds[0];
   }

   public Entity getCommandSenderEntity() {
      return null;
   }

   public boolean isBlockProtected(World worldIn, BlockPos pos, EntityPlayer playerIn) {
      return false;
   }

   public boolean getForceGamemode() {
      return this.isGamemodeForced;
   }

   public Proxy getServerProxy() {
      return this.serverProxy;
   }

   public static long getCurrentTimeMillis() {
      return System.currentTimeMillis();
   }

   public int getMaxPlayerIdleMinutes() {
      return this.maxPlayerIdleMinutes;
   }

   public void setPlayerIdleTimeout(int idleTimeout) {
      this.maxPlayerIdleMinutes = idleTimeout;
   }

   public ITextComponent getDisplayName() {
      return new TextComponentString(this.getName());
   }

   public boolean isAnnouncingPlayerAchievements() {
      return true;
   }

   public MinecraftSessionService getMinecraftSessionService() {
      return this.sessionService;
   }

   public GameProfileRepository getGameProfileRepository() {
      return this.profileRepo;
   }

   public PlayerProfileCache getPlayerProfileCache() {
      return this.profileCache;
   }

   public ServerStatusResponse getServerStatusResponse() {
      return this.statusResponse;
   }

   public void refreshStatusNextTick() {
      this.nanoTimeSinceStatusRefresh = 0L;
   }

   @Nullable
   public Entity getEntityFromUuid(UUID uuid) {
      for(WorldServer worldserver : this.worlds) {
         if (worldserver != null) {
            Entity entity = worldserver.getEntityFromUuid(uuid);
            if (entity != null) {
               return entity;
            }
         }
      }

      return null;
   }

   public boolean sendCommandFeedback() {
      return this.worlds[0].getGameRules().getBoolean("sendCommandFeedback");
   }

   public void setCommandStat(CommandResultStats.Type type, int amount) {
   }

   public MinecraftServer getServer() {
      return this;
   }

   public int getMaxWorldSize() {
      return 29999984;
   }

   public ListenableFuture callFromMainThread(Callable callable) {
      Validate.notNull(callable);
      if (!this.isCallingFromMinecraftThread() && !this.isServerStopped()) {
         ListenableFutureTask listenablefuturetask = ListenableFutureTask.create(callable);
         synchronized(this.futureTaskQueue) {
            this.futureTaskQueue.add(listenablefuturetask);
            return listenablefuturetask;
         }
      } else {
         try {
            return Futures.immediateFuture(callable.call());
         } catch (Exception var6) {
            return Futures.immediateFailedCheckedFuture(var6);
         }
      }
   }

   public ListenableFuture addScheduledTask(Runnable runnableToSchedule) {
      Validate.notNull(runnableToSchedule);
      return this.callFromMainThread(Executors.callable(runnableToSchedule));
   }

   public boolean isCallingFromMinecraftThread() {
      return Thread.currentThread() == this.serverThread;
   }

   public int getNetworkCompressionThreshold() {
      return 256;
   }

   public DataFixer getDataFixer() {
      return this.dataFixer;
   }

   public int getSpawnRadius(@Nullable WorldServer worldIn) {
      return worldIn != null ? worldIn.getGameRules().getInt("spawnRadius") : 10;
   }

   @SideOnly(Side.SERVER)
   public String getServerHostname() {
      return this.hostname;
   }

   @SideOnly(Side.SERVER)
   public void setHostname(String host) {
      this.hostname = host;
   }

   @SideOnly(Side.SERVER)
   public void registerTickable(ITickable tickable) {
      this.tickables.add(tickable);
   }

   @SideOnly(Side.SERVER)
   public static void main(String[] p_main_0_) {
      Bootstrap.register();

      try {
         boolean flag = true;
         String s = null;
         String s1 = ".";
         String s2 = null;
         boolean flag1 = false;
         boolean flag2 = false;
         int i = -1;

         for(int j = 0; j < p_main_0_.length; ++j) {
            String s3 = p_main_0_[j];
            String s4 = j == p_main_0_.length - 1 ? null : p_main_0_[j + 1];
            boolean flag3 = false;
            if (!"nogui".equals(s3) && !"--nogui".equals(s3)) {
               if ("--port".equals(s3) && s4 != null) {
                  flag3 = true;

                  try {
                     i = Integer.parseInt(s4);
                  } catch (NumberFormatException var13) {
                     ;
                  }
               } else if ("--singleplayer".equals(s3) && s4 != null) {
                  flag3 = true;
                  s = s4;
               } else if ("--universe".equals(s3) && s4 != null) {
                  flag3 = true;
                  s1 = s4;
               } else if ("--world".equals(s3) && s4 != null) {
                  flag3 = true;
                  s2 = s4;
               } else if ("--demo".equals(s3)) {
                  flag1 = true;
               } else if ("--bonusChest".equals(s3)) {
                  flag2 = true;
               }
            } else {
               flag = false;
            }

            if (flag3) {
               ++j;
            }
         }

         YggdrasilAuthenticationService yggdrasilauthenticationservice = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());
         MinecraftSessionService minecraftsessionservice = yggdrasilauthenticationservice.createMinecraftSessionService();
         GameProfileRepository gameprofilerepository = yggdrasilauthenticationservice.createProfileRepository();
         PlayerProfileCache playerprofilecache = new PlayerProfileCache(gameprofilerepository, new File(s1, USER_CACHE_FILE.getName()));
         final DedicatedServer dedicatedserver = new DedicatedServer(new File(s1), DataFixesManager.createFixer(), yggdrasilauthenticationservice, minecraftsessionservice, gameprofilerepository, playerprofilecache);
         if (s != null) {
            dedicatedserver.setServerOwner(s);
         }

         if (s2 != null) {
            dedicatedserver.setFolderName(s2);
         }

         if (i >= 0) {
            dedicatedserver.setServerPort(i);
         }

         if (flag1) {
            dedicatedserver.setDemo(true);
         }

         if (flag2) {
            dedicatedserver.canCreateBonusChest(true);
         }

         if (flag && !GraphicsEnvironment.isHeadless()) {
            dedicatedserver.setGuiEnabled();
         }

         dedicatedserver.startServerThread();
         Runtime.getRuntime().addShutdownHook(new Thread("Server Shutdown Thread") {
            public void run() {
               dedicatedserver.stopServer();
            }
         });
      } catch (Exception var14) {
         LOG.fatal("Failed to start the minecraft server", var14);
      }

   }

   @SideOnly(Side.SERVER)
   public void logInfo(String msg) {
      LOG.info(msg);
   }

   @SideOnly(Side.SERVER)
   public boolean isDebuggingEnabled() {
      return false;
   }

   @SideOnly(Side.SERVER)
   public void logSevere(String msg) {
      LOG.error(msg);
   }

   @SideOnly(Side.SERVER)
   public void logDebug(String msg) {
      if (this.isDebuggingEnabled()) {
         LOG.info(msg);
      }

   }

   @SideOnly(Side.SERVER)
   public int getServerPort() {
      return this.serverPort;
   }

   @SideOnly(Side.SERVER)
   public void setServerPort(int port) {
      this.serverPort = port;
   }

   @SideOnly(Side.SERVER)
   public int getSpawnProtectionSize() {
      return 16;
   }

   @SideOnly(Side.SERVER)
   public void setForceGamemode(boolean force) {
      this.isGamemodeForced = force;
   }

   @SideOnly(Side.SERVER)
   public long getCurrentTime() {
      return this.currentTime;
   }

   @SideOnly(Side.SERVER)
   public Thread getServerThread() {
      return this.serverThread;
   }
}
