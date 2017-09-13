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
import java.util.ArrayList;
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

   public MinecraftServer(File var1, Proxy var2, DataFixer var3, YggdrasilAuthenticationService var4, MinecraftSessionService var5, GameProfileRepository var6, PlayerProfileCache var7) {
      this.serverProxy = var2;
      this.authService = var4;
      this.sessionService = var5;
      this.profileRepo = var6;
      this.profileCache = var7;
      this.anvilFile = var1;
      this.networkSystem = new NetworkSystem(this);
      this.commandManager = this.createCommandManager();
      this.anvilConverterForAnvilFile = new AnvilSaveConverter(var1, var3);
      this.dataFixer = var3;
   }

   public ServerCommandManager createCommandManager() {
      return new ServerCommandManager(this);
   }

   public abstract boolean init() throws IOException;

   public void convertMapIfNeeded(String var1) {
      if (this.getActiveAnvilConverter().isOldMapFormat(var1)) {
         LOG.info("Converting map!");
         this.setUserMessage("menu.convertingLevel");
         this.getActiveAnvilConverter().convertMapFormat(var1, new IProgressUpdate() {
            private long startTime = System.currentTimeMillis();

            public void displaySavingString(String var1) {
            }

            public void setLoadingProgress(int var1) {
               if (System.currentTimeMillis() - this.startTime >= 1000L) {
                  this.startTime = System.currentTimeMillis();
                  MinecraftServer.LOG.info("Converting... {}%", new Object[]{var1});
               }

            }

            @SideOnly(Side.CLIENT)
            public void resetProgressAndMessage(String var1) {
            }

            @SideOnly(Side.CLIENT)
            public void setDoneWorking() {
            }

            public void displayLoadingString(String var1) {
            }
         });
      }

   }

   protected synchronized void setUserMessage(String var1) {
      this.userMessage = var1;
   }

   @SideOnly(Side.CLIENT)
   public synchronized String getUserMessage() {
      return this.userMessage;
   }

   public void loadAllWorlds(String var1, String var2, long var3, WorldType var5, String var6) {
      this.convertMapIfNeeded(var1);
      this.setUserMessage("menu.loadingLevel");
      ISaveHandler var7 = this.anvilConverterForAnvilFile.getSaveLoader(var1, true);
      this.setResourcePackFromWorld(this.getFolderName(), var7);
      WorldInfo var8 = var7.loadWorldInfo();
      WorldSettings var9;
      if (var8 == null) {
         if (this.isDemo()) {
            var9 = DemoWorldServer.DEMO_WORLD_SETTINGS;
         } else {
            var9 = new WorldSettings(var3, this.getGameType(), this.canStructuresSpawn(), this.isHardcore(), var5);
            var9.setGeneratorOptions(var6);
            if (this.enableBonusChest) {
               var9.enableBonusChest();
            }
         }

         var8 = new WorldInfo(var9, var2);
      } else {
         var8.setWorldName(var2);
         var9 = new WorldSettings(var8);
      }

      WorldServer var10 = (WorldServer)(this.isDemo() ? (new DemoWorldServer(this, var7, var8, 0, this.theProfiler)).init() : (new WorldServer(this, var7, var8, 0, this.theProfiler)).init());
      var10.initialize(var9);
      Integer[] var11 = DimensionManager.getStaticDimensionIDs();
      int var12 = var11.length;

      for(int var13 = 0; var13 < var12; ++var13) {
         int var14 = var11[var13].intValue();
         WorldServer var15 = var14 == 0 ? var10 : (WorldServer)(new WorldServerMulti(this, var7, var14, var10, this.theProfiler)).init();
         var15.addEventListener(new ServerWorldEventHandler(this, var15));
         if (!this.isSinglePlayer()) {
            var15.getWorldInfo().setGameType(this.getGameType());
         }

         MinecraftForge.EVENT_BUS.post(new Load(var15));
      }

      this.playerList.setPlayerManager(new WorldServer[]{var10});
      this.setDifficultyForAllWorlds(this.getDifficulty());
      this.initialWorldChunkLoad();
   }

   public void initialWorldChunkLoad() {
      boolean var1 = true;
      boolean var2 = true;
      boolean var3 = true;
      boolean var4 = true;
      int var5 = 0;
      this.setUserMessage("menu.generatingTerrain");
      byte var6 = 0;
      LOG.info("Preparing start region for level 0");
      WorldServer var7 = DimensionManager.getWorld(var6);
      BlockPos var8 = var7.getSpawnPoint();
      long var9 = getCurrentTimeMillis();

      for(int var11 = -192; var11 <= 192 && this.isServerRunning(); var11 += 16) {
         for(int var12 = -192; var12 <= 192 && this.isServerRunning(); var12 += 16) {
            long var13 = getCurrentTimeMillis();
            if (var13 - var9 > 1000L) {
               this.outputPercentRemaining("Preparing spawn area", var5 * 100 / 625);
               var9 = var13;
            }

            ++var5;
            var7.getChunkProvider().provideChunk(var8.getX() + var11 >> 4, var8.getZ() + var12 >> 4);
         }
      }

      this.clearCurrentTask();
   }

   public void setResourcePackFromWorld(String var1, ISaveHandler var2) {
      File var3 = new File(var2.getWorldDirectory(), "resources.zip");
      if (var3.isFile()) {
         this.setResourcePack("level://" + var1 + "/resources.zip", "");
      }

   }

   public abstract boolean canStructuresSpawn();

   public abstract GameType getGameType();

   public abstract EnumDifficulty getDifficulty();

   public abstract boolean isHardcore();

   public abstract int getOpPermissionLevel();

   public abstract boolean shouldBroadcastRconToOps();

   public abstract boolean shouldBroadcastConsoleToOps();

   protected void outputPercentRemaining(String var1, int var2) {
      this.currentTask = var1;
      this.percentDone = var2;
      LOG.info("{}: {}%", new Object[]{var1, var2});
   }

   protected void clearCurrentTask() {
      this.currentTask = null;
      this.percentDone = 0;
   }

   public void saveAllWorlds(boolean var1) {
      for(WorldServer var5 : this.worlds) {
         if (var5 != null) {
            if (!var1) {
               LOG.info("Saving chunks for level '{}'/{}", new Object[]{var5.getWorldInfo().getWorldName(), var5.provider.getDimensionType().getName()});
            }

            try {
               var5.saveAllChunks(true, (IProgressUpdate)null);
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

         for(WorldServer var4 : this.worlds) {
            if (var4 != null) {
               var4.disableLevelSaving = false;
            }
         }

         this.saveAllWorlds(false);

         for(WorldServer var12 : this.worlds) {
            if (var12 != null) {
               MinecraftForge.EVENT_BUS.post(new Unload(var12));
               var12.flush();
            }
         }

         WorldServer[] var7 = this.worlds;

         for(WorldServer var5 : var7) {
            DimensionManager.setWorld(var5.provider.getDimension(), (WorldServer)null, this);
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
            long var1 = 0L;
            this.statusResponse.setServerDescription(new TextComponentString(this.motd));
            this.statusResponse.setVersion(new ServerStatusResponse.Version("1.10.2", 210));
            this.applyServerIconToResponse(this.statusResponse);

            while(this.serverRunning) {
               long var74 = getCurrentTimeMillis();
               long var5 = var74 - this.currentTime;
               if (var5 > 2000L && this.currentTime - this.timeOfLastWarning >= 15000L) {
                  LOG.warn("Can't keep up! Did the system time change, or is the server overloaded? Running {}ms behind, skipping {} tick(s)", new Object[]{var5, var5 / 50L});
                  var5 = 2000L;
                  this.timeOfLastWarning = this.currentTime;
               }

               if (var5 < 0L) {
                  LOG.warn("Time ran backwards! Did the system time change?");
                  var5 = 0L;
               }

               var1 += var5;
               this.currentTime = var74;
               if (this.worlds[0].areAllPlayersAsleep()) {
                  this.tick();
                  var1 = 0L;
               } else {
                  while(var1 > 50L) {
                     var1 -= 50L;
                     this.tick();
                  }
               }

               Thread.sleep(Math.max(1L, 50L - var1));
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
         CrashReport var2 = null;
         if (var71 instanceof ReportedException) {
            var2 = this.addServerInfoToCrashReport(((ReportedException)var71).getCrashReport());
         } else {
            var2 = this.addServerInfoToCrashReport(new CrashReport("Exception in server tick loop", var71));
         }

         File var3 = new File(new File(this.getDataDirectory(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");
         if (var2.saveToFile(var3)) {
            LOG.error("This crash report has been saved to: {}", new Object[]{var3.getAbsolutePath()});
         } else {
            LOG.error("We were unable to save this crash report to disk.");
         }

         FMLCommonHandler.instance().expectServerStopped();
         this.finalTick(var2);
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

   public void applyServerIconToResponse(ServerStatusResponse var1) {
      File var2 = this.getFile("server-icon.png");
      if (!var2.exists()) {
         var2 = this.getActiveAnvilConverter().getFile(this.getFolderName(), "icon.png");
      }

      if (var2.isFile()) {
         ByteBuf var3 = Unpooled.buffer();

         try {
            BufferedImage var4 = ImageIO.read(var2);
            Validate.validState(var4.getWidth() == 64, "Must be 64 pixels wide", new Object[0]);
            Validate.validState(var4.getHeight() == 64, "Must be 64 pixels high", new Object[0]);
            ImageIO.write(var4, "PNG", new ByteBufOutputStream(var3));
            ByteBuf var5 = Base64.encode(var3);
            var1.setFavicon("data:image/png;base64," + var5.toString(Charsets.UTF_8));
         } catch (Exception var9) {
            LOG.error("Couldn't load server icon", var9);
         } finally {
            var3.release();
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

   public void finalTick(CrashReport var1) {
   }

   public void systemExitNow() {
   }

   public void tick() {
      long var1 = System.nanoTime();
      FMLCommonHandler.instance().onPreServerTick();
      ++this.tickCounter;
      if (this.startProfiling) {
         this.startProfiling = false;
         this.theProfiler.profilingEnabled = true;
         this.theProfiler.clearProfiling();
      }

      this.theProfiler.startSection("root");
      this.updateTimeLightAndEntities();
      if (var1 - this.nanoTimeSinceStatusRefresh >= 5000000000L) {
         this.nanoTimeSinceStatusRefresh = var1;
         this.statusResponse.setPlayers(new ServerStatusResponse.Players(this.getMaxPlayers(), this.getCurrentPlayerCount()));
         GameProfile[] var3 = new GameProfile[Math.min(this.getCurrentPlayerCount(), 12)];
         int var4 = MathHelper.getInt(this.random, 0, this.getCurrentPlayerCount() - var3.length);

         for(int var5 = 0; var5 < var3.length; ++var5) {
            var3[var5] = ((EntityPlayerMP)this.playerList.getPlayers().get(var4 + var5)).getGameProfile();
         }

         Collections.shuffle(Arrays.asList(var3));
         this.statusResponse.getPlayers().setPlayers(var3);
         this.statusResponse.invalidateJson();
      }

      if (this.tickCounter % 900 == 0) {
         this.theProfiler.startSection("save");
         this.playerList.saveAllPlayerData();
         this.saveAllWorlds(true);
         this.theProfiler.endSection();
      }

      this.theProfiler.startSection("tallying");
      this.tickTimeArray[this.tickCounter % 100] = System.nanoTime() - var1;
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
      Integer[] var12 = DimensionManager.getIDs(this.tickCounter % 200 == 0);

      for(int var2 = 0; var2 < var12.length; ++var2) {
         int var3 = var12[var2].intValue();
         long var4 = System.nanoTime();
         if (var3 == 0 || this.getAllowNether()) {
            WorldServer var6 = DimensionManager.getWorld(var3);
            this.theProfiler.startSection(var6.getWorldInfo().getWorldName());
            if (this.tickCounter % 20 == 0) {
               this.theProfiler.startSection("timeSync");
               this.playerList.sendPacketToAllPlayersInDimension(new SPacketTimeUpdate(var6.getTotalWorldTime(), var6.getWorldTime(), var6.getGameRules().getBoolean("doDaylightCycle")), var6.provider.getDimension());
               this.theProfiler.endSection();
            }

            this.theProfiler.startSection("tick");
            FMLCommonHandler.instance().onPreWorldTick(var6);

            try {
               var6.tick();
            } catch (Throwable var10) {
               CrashReport var8 = CrashReport.makeCrashReport(var10, "Exception ticking world");
               var6.addWorldInfoToCrashReport(var8);
               throw new ReportedException(var8);
            }

            try {
               var6.updateEntities();
            } catch (Throwable var9) {
               CrashReport var14 = CrashReport.makeCrashReport(var9, "Exception ticking world entities");
               var6.addWorldInfoToCrashReport(var14);
               throw new ReportedException(var14);
            }

            FMLCommonHandler.instance().onPostWorldTick(var6);
            this.theProfiler.endSection();
            this.theProfiler.startSection("tracker");
            var6.getEntityTracker().tick();
            this.theProfiler.endSection();
            this.theProfiler.endSection();
         }

         ((long[])this.worldTickTimes.get(Integer.valueOf(var3)))[this.tickCounter % 100] = System.nanoTime() - var4;
      }

      this.theProfiler.endStartSection("dim_unloading");
      DimensionManager.unloadWorlds(this.worldTickTimes);
      this.theProfiler.endStartSection("connection");
      this.getNetworkSystem().networkTick();
      this.theProfiler.endStartSection("players");
      this.playerList.onTick();
      this.theProfiler.endStartSection("tickables");

      for(int var13 = 0; var13 < this.tickables.size(); ++var13) {
         ((ITickable)this.tickables.get(var13)).update();
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

   public File getFile(String var1) {
      return new File(this.getDataDirectory(), var1);
   }

   public void logWarning(String var1) {
      LOG.warn(var1);
   }

   public WorldServer worldServerForDimension(int var1) {
      WorldServer var2 = DimensionManager.getWorld(var1);
      if (var2 == null) {
         DimensionManager.initDimension(var1);
         var2 = DimensionManager.getWorld(var1);
      }

      return var2;
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

   public CrashReport addServerInfoToCrashReport(CrashReport var1) {
      var1.getCategory().setDetail("Profiler Position", new ICrashReportDetail() {
         public String call() throws Exception {
            return MinecraftServer.this.theProfiler.profilingEnabled ? MinecraftServer.this.theProfiler.getNameOfLastSection() : "N/A (disabled)";
         }
      });
      if (this.playerList != null) {
         var1.getCategory().setDetail("Player Count", new ICrashReportDetail() {
            public String call() {
               return MinecraftServer.this.playerList.getCurrentPlayerCount() + " / " + MinecraftServer.this.playerList.getMaxPlayers() + "; " + MinecraftServer.this.playerList.getPlayers();
            }
         });
      }

      return var1;
   }

   public List getTabCompletions(ICommandSender var1, String var2, @Nullable BlockPos var3, boolean var4) {
      ArrayList var5 = Lists.newArrayList();
      boolean var6 = var2.startsWith("/");
      if (var6) {
         var2 = var2.substring(1);
      }

      if (!var6 && !var4) {
         String[] var13 = var2.split(" ", -1);
         String var14 = var13[var13.length - 1];

         for(String var12 : this.playerList.getOnlinePlayerNames()) {
            if (CommandBase.doesStringStartWith(var14, var12)) {
               var5.add(var12);
            }
         }

         return var5;
      } else {
         boolean var7 = !var2.contains(" ");
         List var8 = this.commandManager.getTabCompletions(var1, var2, var3);
         if (!var8.isEmpty()) {
            for(String var10 : var8) {
               if (var7) {
                  var5.add("/" + var10);
               } else {
                  var5.add(var10);
               }
            }
         }

         return var5;
      }
   }

   public boolean isAnvilFileSet() {
      return this.anvilFile != null;
   }

   public String getName() {
      return "Server";
   }

   public void sendMessage(ITextComponent var1) {
      LOG.info(var1.getUnformattedText());
   }

   public boolean canUseCommand(int var1, String var2) {
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

   public void setServerOwner(String var1) {
      this.serverOwner = var1;
   }

   public boolean isSinglePlayer() {
      return this.serverOwner != null;
   }

   public String getFolderName() {
      return this.folderName;
   }

   public void setFolderName(String var1) {
      this.folderName = var1;
   }

   @SideOnly(Side.CLIENT)
   public void setWorldName(String var1) {
      this.worldName = var1;
   }

   @SideOnly(Side.CLIENT)
   public String getWorldName() {
      return this.worldName;
   }

   public void setKeyPair(KeyPair var1) {
      this.serverKeyPair = var1;
   }

   public void setDifficultyForAllWorlds(EnumDifficulty var1) {
      for(WorldServer var5 : this.worlds) {
         if (var5 != null) {
            if (var5.getWorldInfo().isHardcoreModeEnabled()) {
               var5.getWorldInfo().setDifficulty(EnumDifficulty.HARD);
               var5.setAllowedSpawnTypes(true, true);
            } else if (this.isSinglePlayer()) {
               var5.getWorldInfo().setDifficulty(var1);
               var5.setAllowedSpawnTypes(var5.getDifficulty() != EnumDifficulty.PEACEFUL, true);
            } else {
               var5.getWorldInfo().setDifficulty(var1);
               var5.setAllowedSpawnTypes(this.allowSpawnMonsters(), this.canSpawnAnimals);
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

   public void setDemo(boolean var1) {
      this.isDemo = var1;
   }

   public void canCreateBonusChest(boolean var1) {
      this.enableBonusChest = var1;
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

   public void setResourcePack(String var1, String var2) {
      this.resourcePackUrl = var1;
      this.resourcePackHash = var2;
   }

   public void addServerStatsToSnooper(Snooper var1) {
      var1.addClientStat("whitelist_enabled", Boolean.valueOf(false));
      var1.addClientStat("whitelist_count", Integer.valueOf(0));
      if (this.playerList != null) {
         var1.addClientStat("players_current", Integer.valueOf(this.getCurrentPlayerCount()));
         var1.addClientStat("players_max", Integer.valueOf(this.getMaxPlayers()));
         var1.addClientStat("players_seen", Integer.valueOf(this.playerList.getAvailablePlayerDat().length));
      }

      var1.addClientStat("uses_auth", Boolean.valueOf(this.onlineMode));
      var1.addClientStat("gui_state", this.getGuiEnabled() ? "enabled" : "disabled");
      var1.addClientStat("run_time", Long.valueOf((getCurrentTimeMillis() - var1.getMinecraftStartTimeMillis()) / 60L * 1000L));
      var1.addClientStat("avg_tick_ms", Integer.valueOf((int)(MathHelper.average(this.tickTimeArray) * 1.0E-6D)));
      int var2 = 0;
      if (this.worlds != null) {
         for(WorldServer var6 : this.worlds) {
            if (var6 != null) {
               WorldInfo var7 = var6.getWorldInfo();
               var1.addClientStat("world[" + var2 + "][dimension]", Integer.valueOf(var6.provider.getDimensionType().getId()));
               var1.addClientStat("world[" + var2 + "][mode]", var7.getGameType());
               var1.addClientStat("world[" + var2 + "][difficulty]", var6.getDifficulty());
               var1.addClientStat("world[" + var2 + "][hardcore]", Boolean.valueOf(var7.isHardcoreModeEnabled()));
               var1.addClientStat("world[" + var2 + "][generator_name]", var7.getTerrainType().getName());
               var1.addClientStat("world[" + var2 + "][generator_version]", Integer.valueOf(var7.getTerrainType().getGeneratorVersion()));
               var1.addClientStat("world[" + var2 + "][height]", Integer.valueOf(this.buildLimit));
               var1.addClientStat("world[" + var2 + "][chunks_loaded]", Integer.valueOf(var6.getChunkProvider().getLoadedChunkCount()));
               ++var2;
            }
         }
      }

      var1.addClientStat("worlds", Integer.valueOf(var2));
   }

   public void addServerTypeToSnooper(Snooper var1) {
      var1.addStatToSnooper("singleplayer", Boolean.valueOf(this.isSinglePlayer()));
      var1.addStatToSnooper("server_brand", this.getServerModName());
      var1.addStatToSnooper("gui_supported", GraphicsEnvironment.isHeadless() ? "headless" : "supported");
      var1.addStatToSnooper("dedicated", Boolean.valueOf(this.isDedicatedServer()));
   }

   public boolean isSnooperEnabled() {
      return true;
   }

   public abstract boolean isDedicatedServer();

   public boolean isServerInOnlineMode() {
      return this.onlineMode;
   }

   public void setOnlineMode(boolean var1) {
      this.onlineMode = var1;
   }

   public boolean getCanSpawnAnimals() {
      return this.canSpawnAnimals;
   }

   public void setCanSpawnAnimals(boolean var1) {
      this.canSpawnAnimals = var1;
   }

   public boolean getCanSpawnNPCs() {
      return this.canSpawnNPCs;
   }

   public abstract boolean shouldUseNativeTransport();

   public void setCanSpawnNPCs(boolean var1) {
      this.canSpawnNPCs = var1;
   }

   public boolean isPVPEnabled() {
      return this.pvpEnabled;
   }

   public void setAllowPvp(boolean var1) {
      this.pvpEnabled = var1;
   }

   public boolean isFlightAllowed() {
      return this.allowFlight;
   }

   public void setAllowFlight(boolean var1) {
      this.allowFlight = var1;
   }

   public abstract boolean isCommandBlockEnabled();

   public String getMOTD() {
      return this.motd;
   }

   public void setMOTD(String var1) {
      this.motd = var1;
   }

   public int getBuildLimit() {
      return this.buildLimit;
   }

   public void setBuildLimit(int var1) {
      this.buildLimit = var1;
   }

   public boolean isServerStopped() {
      return this.serverStopped;
   }

   public PlayerList getPlayerList() {
      return this.playerList;
   }

   public void setPlayerList(PlayerList var1) {
      this.playerList = var1;
   }

   public void setGameType(GameType var1) {
      for(WorldServer var5 : this.worlds) {
         var5.getWorldInfo().setGameType(var1);
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

   public boolean isBlockProtected(World var1, BlockPos var2, EntityPlayer var3) {
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

   public void setPlayerIdleTimeout(int var1) {
      this.maxPlayerIdleMinutes = var1;
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
   public Entity getEntityFromUuid(UUID var1) {
      for(WorldServer var5 : this.worlds) {
         if (var5 != null) {
            Entity var6 = var5.getEntityFromUuid(var1);
            if (var6 != null) {
               return var6;
            }
         }
      }

      return null;
   }

   public boolean sendCommandFeedback() {
      return this.worlds[0].getGameRules().getBoolean("sendCommandFeedback");
   }

   public void setCommandStat(CommandResultStats.Type var1, int var2) {
   }

   public MinecraftServer getServer() {
      return this;
   }

   public int getMaxWorldSize() {
      return 29999984;
   }

   public ListenableFuture callFromMainThread(Callable var1) {
      Validate.notNull(var1);
      if (!this.isCallingFromMinecraftThread() && !this.isServerStopped()) {
         ListenableFutureTask var2 = ListenableFutureTask.create(var1);
         synchronized(this.futureTaskQueue) {
            this.futureTaskQueue.add(var2);
            return var2;
         }
      } else {
         try {
            return Futures.immediateFuture(var1.call());
         } catch (Exception var6) {
            return Futures.immediateFailedCheckedFuture(var6);
         }
      }
   }

   public ListenableFuture addScheduledTask(Runnable var1) {
      Validate.notNull(var1);
      return this.callFromMainThread(Executors.callable(var1));
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

   public int getSpawnRadius(@Nullable WorldServer var1) {
      return var1 != null ? var1.getGameRules().getInt("spawnRadius") : 10;
   }

   @SideOnly(Side.SERVER)
   public String getServerHostname() {
      return this.hostname;
   }

   @SideOnly(Side.SERVER)
   public void setHostname(String var1) {
      this.hostname = var1;
   }

   @SideOnly(Side.SERVER)
   public void registerTickable(ITickable var1) {
      this.tickables.add(var1);
   }

   @SideOnly(Side.SERVER)
   public static void main(String[] var0) {
      Bootstrap.register();

      try {
         boolean var1 = true;
         String var2 = null;
         String var3 = ".";
         String var4 = null;
         boolean var5 = false;
         boolean var6 = false;
         int var7 = -1;

         for(int var8 = 0; var8 < var0.length; ++var8) {
            String var9 = var0[var8];
            String var10 = var8 == var0.length - 1 ? null : var0[var8 + 1];
            boolean var11 = false;
            if (!"nogui".equals(var9) && !"--nogui".equals(var9)) {
               if ("--port".equals(var9) && var10 != null) {
                  var11 = true;

                  try {
                     var7 = Integer.parseInt(var10);
                  } catch (NumberFormatException var13) {
                     ;
                  }
               } else if ("--singleplayer".equals(var9) && var10 != null) {
                  var11 = true;
                  var2 = var10;
               } else if ("--universe".equals(var9) && var10 != null) {
                  var11 = true;
                  var3 = var10;
               } else if ("--world".equals(var9) && var10 != null) {
                  var11 = true;
                  var4 = var10;
               } else if ("--demo".equals(var9)) {
                  var5 = true;
               } else if ("--bonusChest".equals(var9)) {
                  var6 = true;
               }
            } else {
               var1 = false;
            }

            if (var11) {
               ++var8;
            }
         }

         YggdrasilAuthenticationService var15 = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());
         MinecraftSessionService var16 = var15.createMinecraftSessionService();
         GameProfileRepository var17 = var15.createProfileRepository();
         PlayerProfileCache var18 = new PlayerProfileCache(var17, new File(var3, USER_CACHE_FILE.getName()));
         final DedicatedServer var12 = new DedicatedServer(new File(var3), DataFixesManager.createFixer(), var15, var16, var17, var18);
         if (var2 != null) {
            var12.setServerOwner(var2);
         }

         if (var4 != null) {
            var12.setFolderName(var4);
         }

         if (var7 >= 0) {
            var12.setServerPort(var7);
         }

         if (var5) {
            var12.setDemo(true);
         }

         if (var6) {
            var12.canCreateBonusChest(true);
         }

         if (var1 && !GraphicsEnvironment.isHeadless()) {
            var12.setGuiEnabled();
         }

         var12.startServerThread();
         Runtime.getRuntime().addShutdownHook(new Thread("Server Shutdown Thread") {
            public void run() {
               var12.stopServer();
            }
         });
      } catch (Exception var14) {
         LOG.fatal("Failed to start the minecraft server", var14);
      }

   }

   @SideOnly(Side.SERVER)
   public void logInfo(String var1) {
      LOG.info(var1);
   }

   @SideOnly(Side.SERVER)
   public boolean isDebuggingEnabled() {
      return false;
   }

   @SideOnly(Side.SERVER)
   public void logSevere(String var1) {
      LOG.error(var1);
   }

   @SideOnly(Side.SERVER)
   public void logDebug(String var1) {
      if (this.isDebuggingEnabled()) {
         LOG.info(var1);
      }

   }

   @SideOnly(Side.SERVER)
   public int getServerPort() {
      return this.serverPort;
   }

   @SideOnly(Side.SERVER)
   public void setServerPort(int var1) {
      this.serverPort = var1;
   }

   @SideOnly(Side.SERVER)
   public int getSpawnProtectionSize() {
      return 16;
   }

   @SideOnly(Side.SERVER)
   public void setForceGamemode(boolean var1) {
      this.isGamemodeForced = var1;
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
