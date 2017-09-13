package net.minecraft.src;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.io.Files;
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
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
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
import net.minecraft.server.dedicated.PropertyManager;
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
import net.minecraft.world.chunk.storage.AnvilSaveHandler;
import net.minecraft.world.demo.DemoWorldServer;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World.Environment;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.craftbukkit.Main;
import org.bukkit.craftbukkit.libs.jline.console.ConsoleReader;
import org.bukkit.craftbukkit.libs.joptsimple.OptionSet;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.craftbukkit.v1_10_R1.chunkio.ChunkIOExecutor;
import org.bukkit.craftbukkit.v1_10_R1.scoreboard.CraftScoreboardManager;
import org.bukkit.craftbukkit.v1_10_R1.util.ServerShutdownThread;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginLoadOrder;

public abstract class MinecraftServer implements Runnable, ICommandSender, IThreadListener, ISnooperInfo {
   public static final Logger LOGGER = LogManager.getLogger();
   public static final File a = new File("usercache.json");
   public ISaveFormat convertable;
   private final Snooper m = new Snooper("server", this, av());
   public File universe;
   private final List o = Lists.newArrayList();
   public final ICommandManager b;
   public final Profiler methodProfiler = new Profiler();
   private final NetworkSystem p;
   private final ServerStatusResponse q = new ServerStatusResponse();
   private final Random r = new Random();
   private final DataFixer dataConverterManager;
   private String serverIp;
   private int u = -1;
   public WorldServer[] worldServer;
   private PlayerList v;
   private boolean isRunning = true;
   private boolean isStopped;
   private int ticks;
   protected final Proxy e;
   public String f;
   public int g;
   private boolean onlineMode;
   private boolean spawnAnimals;
   private boolean spawnNPCs;
   private boolean pvpMode;
   private boolean allowFlight;
   private String motd;
   private int F;
   private int G;
   public final long[] h = new long[100];
   public long[][] i;
   private KeyPair H;
   private String I;
   private String J;
   private boolean demoMode;
   private boolean M;
   private String N = "";
   private String O = "";
   private boolean P;
   private long Q;
   private String R;
   private boolean S;
   private boolean T;
   private final YggdrasilAuthenticationService U;
   private final MinecraftSessionService V;
   private final GameProfileRepository W;
   private final PlayerProfileCache X;
   private long Y;
   protected final Queue j = Queues.newArrayDeque();
   private Thread serverThread;
   private long aa = av();
   public List worlds = new ArrayList();
   public CraftServer server;
   public OptionSet options;
   public ConsoleCommandSender console;
   public RemoteConsoleCommandSender remoteConsole;
   public ConsoleReader reader;
   public static int currentTick = (int)(System.currentTimeMillis() / 50L);
   public final Thread primaryThread;
   public Queue processQueue = new ConcurrentLinkedQueue();
   public int autosavePeriod;
   private boolean hasStopped = false;
   private final Object stopLock = new Object();

   public MinecraftServer(OptionSet options, Proxy proxy, DataFixer dataconvertermanager, YggdrasilAuthenticationService yggdrasilauthenticationservice, MinecraftSessionService minecraftsessionservice, GameProfileRepository gameprofilerepository, PlayerProfileCache usercache) {
      this.e = proxy;
      this.U = yggdrasilauthenticationservice;
      this.V = minecraftsessionservice;
      this.W = gameprofilerepository;
      this.X = usercache;
      this.p = new NetworkSystem(this);
      this.b = this.i();
      this.dataConverterManager = dataconvertermanager;
      this.options = options;
      if (System.console() == null && System.getProperty("org.bukkit.craftbukkit.libs.jline.terminal") == null) {
         System.setProperty("org.bukkit.craftbukkit.libs.jline.terminal", "org.bukkit.craftbukkit.libs.jline.UnsupportedTerminal");
         Main.useJline = false;
      }

      try {
         this.reader = new ConsoleReader(System.in, System.out);
         this.reader.setExpandEvents(false);
      } catch (Throwable var10) {
         try {
            System.setProperty("org.bukkit.craftbukkit.libs.jline.terminal", "org.bukkit.craftbukkit.libs.jline.UnsupportedTerminal");
            System.setProperty("user.language", "en");
            Main.useJline = false;
            this.reader = new ConsoleReader(System.in, System.out);
            this.reader.setExpandEvents(false);
         } catch (IOException var9) {
            LOGGER.warn((String)null, var9);
         }
      }

      Runtime.getRuntime().addShutdownHook(new ServerShutdownThread(this));
      this.serverThread = this.primaryThread = new Thread(this, "Server thread");
   }

   public abstract PropertyManager getPropertyManager();

   protected ServerCommandManager i() {
      return new ServerCommandManager(this);
   }

   public abstract boolean init() throws IOException;

   protected void a(String s) {
      if (this.getConvertable().isOldMapFormat(s)) {
         LOGGER.info("Converting map!");
         this.b("menu.convertingLevel");
         this.getConvertable().convertMapFormat(s, new IProgressUpdate() {
            private long b = System.currentTimeMillis();

            public void displaySavingString(String s) {
            }

            public void setLoadingProgress(int i) {
               if (System.currentTimeMillis() - this.b >= 1000L) {
                  this.b = System.currentTimeMillis();
                  MinecraftServer.LOGGER.info("Converting... {}%", new Object[]{i});
               }

            }

            public void displayLoadingString(String s) {
            }
         });
      }

   }

   protected synchronized void b(String s) {
      this.R = s;
   }

   public void a(String s, String s1, long i, WorldType worldtype, String s2) {
      this.a(s);
      this.b("menu.loadingLevel");
      this.worldServer = new WorldServer[3];
      int worldCount = 3;

      for(int j = 0; j < worldCount; ++j) {
         byte dimension = 0;
         if (j == 1) {
            if (!this.getAllowNether()) {
               continue;
            }

            dimension = -1;
         }

         if (j == 2) {
            if (!this.server.getAllowEnd()) {
               continue;
            }

            dimension = 1;
         }

         String worldType = Environment.getEnvironment(dimension).toString().toLowerCase();
         String name = dimension == 0 ? s : s + "_" + worldType;
         ChunkGenerator gen = this.server.getGenerator(name);
         WorldSettings worldsettings = new WorldSettings(i, this.getGamemode(), this.getGenerateStructures(), this.isHardcore(), worldtype);
         worldsettings.setGeneratorOptions(s2);
         WorldServer world;
         if (j == 0) {
            ISaveHandler idatamanager = new AnvilSaveHandler(this.server.getWorldContainer(), s1, true, this.dataConverterManager);
            WorldInfo worlddata = idatamanager.loadWorldInfo();
            if (worlddata == null) {
               worlddata = new WorldInfo(worldsettings, s1);
            }

            worlddata.checkName(s1);
            if (this.V()) {
               world = (WorldServer)(new DemoWorldServer(this, idatamanager, worlddata, dimension, this.methodProfiler)).init();
            } else {
               world = (WorldServer)(new WorldServer(this, idatamanager, worlddata, dimension, this.methodProfiler, Environment.getEnvironment(dimension), gen)).init();
            }

            world.initialize(worldsettings);
            this.server.scoreboardManager = new CraftScoreboardManager(this, world.getScoreboard());
         } else {
            String dim = "DIM" + dimension;
            File newWorld = new File(new File(name), dim);
            File oldWorld = new File(new File(s), dim);
            if (!newWorld.isDirectory() && oldWorld.isDirectory()) {
               LOGGER.info("---- Migration of old " + worldType + " folder required ----");
               LOGGER.info("Unfortunately due to the way that Minecraft implemented multiworld support in 1.6, Bukkit requires that you move your " + worldType + " folder to a new location in order to operate correctly.");
               LOGGER.info("We will move this folder for you, but it will mean that you need to move it back should you wish to stop using Bukkit in the future.");
               LOGGER.info("Attempting to move " + oldWorld + " to " + newWorld + "...");
               if (newWorld.exists()) {
                  LOGGER.warn("A file or folder already exists at " + newWorld + "!");
                  LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
               } else if (newWorld.getParentFile().mkdirs()) {
                  if (oldWorld.renameTo(newWorld)) {
                     LOGGER.info("Success! To restore " + worldType + " in the future, simply move " + newWorld + " to " + oldWorld);

                     try {
                        Files.copy(new File(new File(s), "level.dat"), new File(new File(name), "level.dat"));
                        FileUtils.copyDirectory(new File(new File(s), "data"), new File(new File(name), "data"));
                     } catch (IOException var20) {
                        LOGGER.warn("Unable to migrate world data.");
                     }

                     LOGGER.info("---- Migration of old " + worldType + " folder complete ----");
                  } else {
                     LOGGER.warn("Could not move folder " + oldWorld + " to " + newWorld + "!");
                     LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
                  }
               } else {
                  LOGGER.warn("Could not create path for " + newWorld + "!");
                  LOGGER.info("---- Migration of old " + worldType + " folder failed ----");
               }
            }

            ISaveHandler idatamanager = new AnvilSaveHandler(this.server.getWorldContainer(), name, true, this.dataConverterManager);
            WorldInfo worlddata = idatamanager.loadWorldInfo();
            if (worlddata == null) {
               worlddata = new WorldInfo(worldsettings, name);
            }

            worlddata.checkName(name);
            world = (WorldServer)(new WorldServerMulti(this, idatamanager, dimension, (WorldServer)this.worlds.get(0), this.methodProfiler, worlddata, Environment.getEnvironment(dimension), gen)).init();
         }

         this.server.getPluginManager().callEvent(new WorldInitEvent(world.getWorld()));
         world.addEventListener(new ServerWorldEventHandler(this, world));
         if (!this.R()) {
            world.getWorldInfo().setGameType(this.getGamemode());
         }

         this.worlds.add(world);
         this.getPlayerList().setPlayerManager((WorldServer[])this.worlds.toArray(new WorldServer[this.worlds.size()]));
      }

      this.v.setPlayerManager(this.worldServer);
      this.a(this.getDifficulty());
      this.l();
   }

   protected void l() {
      int i = 0;
      this.b("menu.generatingTerrain");

      for(int m = 0; m < this.worlds.size(); ++m) {
         WorldServer worldserver = (WorldServer)this.worlds.get(m);
         LOGGER.info("Preparing start region for level " + m + " (Seed: " + worldserver.getSeed() + ")");
         if (worldserver.getWorld().getKeepSpawnInMemory()) {
            BlockPos blockposition = worldserver.getSpawnPoint();
            long j = av();
            i = 0;

            for(int k = -192; k <= 192 && this.isRunning(); k += 16) {
               for(int l = -192; l <= 192 && this.isRunning(); l += 16) {
                  long i1 = av();
                  if (i1 - j > 1000L) {
                     this.a_("Preparing spawn area", i * 100 / 625);
                     j = i1;
                  }

                  ++i;
                  worldserver.getChunkProvider().provideChunk(blockposition.getX() + k >> 4, blockposition.getZ() + l >> 4);
               }
            }
         }
      }

      for(WorldServer world : this.worlds) {
         this.server.getPluginManager().callEvent(new WorldLoadEvent(world.getWorld()));
      }

      this.t();
   }

   protected void a(String s, ISaveHandler idatamanager) {
      File file = new File(idatamanager.getWorldDirectory(), "resources.zip");
      if (file.isFile()) {
         this.setResourcePack("level://" + s + "/" + "resources.zip", "");
      }

   }

   public abstract boolean getGenerateStructures();

   public abstract GameType getGamemode();

   public abstract EnumDifficulty getDifficulty();

   public abstract boolean isHardcore();

   public abstract int q();

   public abstract boolean r();

   public abstract boolean s();

   protected void a_(String s, int i) {
      this.f = s;
      this.g = i;
      LOGGER.info("{}: {}%", new Object[]{s, i});
   }

   protected void t() {
      this.f = null;
      this.g = 0;
      this.server.enablePlugins(PluginLoadOrder.POSTWORLD);
   }

   protected void saveChunks(boolean flag) {
      WorldServer[] aworldserver = this.worldServer;
      int var10000 = aworldserver.length;

      for(int j = 0; j < this.worlds.size(); ++j) {
         WorldServer worldserver = (WorldServer)this.worlds.get(j);
         if (worldserver != null) {
            if (!flag) {
               LOGGER.info("Saving chunks for level '{}'/{}", new Object[]{worldserver.getWorldInfo().getWorldName(), worldserver.provider.getDimensionType().getName()});
            }

            try {
               worldserver.saveAllChunks(true, (IProgressUpdate)null);
               worldserver.flush();
            } catch (MinecraftException var6) {
               LOGGER.warn(var6.getMessage());
            }
         }
      }

   }

   public void stop() throws MinecraftException {
      synchronized(this.stopLock) {
         if (this.hasStopped) {
            return;
         }

         this.hasStopped = true;
      }

      LOGGER.info("Stopping server");
      if (this.server != null) {
         this.server.disablePlugins();
      }

      if (this.am() != null) {
         this.am().terminateEndpoints();
      }

      if (this.v != null) {
         LOGGER.info("Saving players");
         this.v.saveAllPlayerData();
         this.v.removeAllPlayers();

         try {
            Thread.sleep(100L);
         } catch (InterruptedException var5) {
            ;
         }
      }

      if (this.worldServer != null) {
         LOGGER.info("Saving worlds");

         for(WorldServer worldserver : this.worldServer) {
            if (worldserver != null) {
               worldserver.disableLevelSaving = false;
            }
         }

         this.saveChunks(false);
         WorldServer[] var8 = this.worldServer;
         int var9 = var8.length;
      }

      if (this.m.isSnooperRunning()) {
         this.m.stopSnooper();
      }

   }

   public String getServerIp() {
      return this.serverIp;
   }

   public void c(String s) {
      this.serverIp = s;
   }

   public boolean isRunning() {
      return this.isRunning;
   }

   public void safeShutdown() {
      this.isRunning = false;
   }

   public void run() {
      try {
         if (this.init()) {
            this.aa = av();
            long i = 0L;
            this.q.setServerDescription(new TextComponentString(this.motd));
            this.q.setVersion(new ServerStatusResponse.Version("1.10.2", 210));
            this.a(this.q);

            while(this.isRunning) {
               long j = av();
               long k = j - this.aa;
               if (k > 2000L && this.aa - this.Q >= 15000L) {
                  if (this.server.getWarnOnOverload()) {
                     LOGGER.warn("Can't keep up! Did the system time change, or is the server overloaded? Running {}ms behind, skipping {} tick(s)", new Object[]{k, k / 50L});
                  }

                  k = 2000L;
                  this.Q = this.aa;
               }

               if (k < 0L) {
                  LOGGER.warn("Time ran backwards! Did the system time change?");
                  k = 0L;
               }

               i += k;
               this.aa = j;
               if (((WorldServer)this.worlds.get(0)).areAllPlayersAsleep()) {
                  this.C();
                  i = 0L;
               } else {
                  while(i > 50L) {
                     currentTick = (int)(System.currentTimeMillis() / 50L);
                     i -= 50L;
                     this.C();
                  }
               }

               Thread.sleep(Math.max(1L, 50L - i));
               this.P = true;
            }
         } else {
            this.a((CrashReport)null);
         }
      } catch (Throwable var72) {
         LOGGER.error("Encountered an unexpected exception", var72);
         CrashReport crashreport = null;
         if (var72 instanceof ReportedException) {
            crashreport = this.b(((ReportedException)var72).getCrashReport());
         } else {
            crashreport = this.b(new CrashReport("Exception in server tick loop", var72));
         }

         File file = new File(new File(this.A(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");
         if (crashreport.saveToFile(file)) {
            LOGGER.error("This crash report has been saved to: {}", new Object[]{file.getAbsolutePath()});
         } else {
            LOGGER.error("We were unable to save this crash report to disk.");
         }

         this.a(crashreport);
      } finally {
         try {
            this.isStopped = true;
            this.stop();
         } catch (Throwable var70) {
            LOGGER.error("Exception stopping the server", var70);
         } finally {
            try {
               this.reader.getTerminal().restore();
            } catch (Exception var69) {
               ;
            }

            this.B();
         }

      }

   }

   public void a(ServerStatusResponse serverping) {
      File file = this.d("server-icon.png");
      if (!file.exists()) {
         file = this.getConvertable().getFile(this.S(), "icon.png");
      }

      if (file.isFile()) {
         ByteBuf bytebuf = Unpooled.buffer();

         try {
            BufferedImage bufferedimage = ImageIO.read(file);
            Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide", new Object[0]);
            Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high", new Object[0]);
            ImageIO.write(bufferedimage, "PNG", new ByteBufOutputStream(bytebuf));
            ByteBuf bytebuf1 = Base64.encode(bytebuf);
            serverping.setFavicon("data:image/png;base64," + bytebuf1.toString(Charsets.UTF_8));
         } catch (Exception var9) {
            LOGGER.error("Couldn't load server icon", var9);
         } finally {
            bytebuf.release();
         }
      }

   }

   public File A() {
      return new File(".");
   }

   public void a(CrashReport crashreport) {
   }

   public void B() {
   }

   protected void C() throws MinecraftException {
      long i = System.nanoTime();
      ++this.ticks;
      if (this.S) {
         this.S = false;
         this.methodProfiler.profilingEnabled = true;
         this.methodProfiler.clearProfiling();
      }

      this.methodProfiler.startSection("root");
      this.D();
      if (i - this.Y >= 5000000000L) {
         this.Y = i;
         this.q.setPlayers(new ServerStatusResponse.Players(this.I(), this.H()));
         GameProfile[] agameprofile = new GameProfile[Math.min(this.H(), 12)];
         int j = MathHelper.getInt(this.r, 0, this.H() - agameprofile.length);

         for(int k = 0; k < agameprofile.length; ++k) {
            agameprofile[k] = ((EntityPlayerMP)this.v.getPlayers().get(j + k)).getGameProfile();
         }

         Collections.shuffle(Arrays.asList(agameprofile));
         this.q.getPlayers().setPlayers(agameprofile);
      }

      if (this.autosavePeriod > 0 && this.ticks % this.autosavePeriod == 0) {
         this.methodProfiler.startSection("save");
         this.v.saveAllPlayerData();
         this.saveChunks(true);
         this.methodProfiler.endSection();
      }

      this.methodProfiler.startSection("tallying");
      this.h[this.ticks % 100] = System.nanoTime() - i;
      this.methodProfiler.endSection();
      this.methodProfiler.startSection("snooper");
      if (!this.m.isSnooperRunning() && this.ticks > 100) {
         this.m.startSnooper();
      }

      if (this.ticks % 6000 == 0) {
         this.m.addMemoryStatsToSnooper();
      }

      this.methodProfiler.endSection();
      this.methodProfiler.endSection();
   }

   public void D() {
      this.server.getScheduler().mainThreadHeartbeat(this.ticks);
      this.methodProfiler.startSection("jobs");
      synchronized(this.j) {
         while(!this.j.isEmpty()) {
            Util.runTask((FutureTask)this.j.poll(), LOGGER);
         }
      }

      this.methodProfiler.endStartSection("levels");

      while(!this.processQueue.isEmpty()) {
         ((Runnable)this.processQueue.remove()).run();
      }

      ChunkIOExecutor.tick();
      if (this.ticks % 20 == 0) {
         for(int i = 0; i < this.getPlayerList().playerEntityList.size(); ++i) {
            EntityPlayerMP entityplayer = (EntityPlayerMP)this.getPlayerList().playerEntityList.get(i);
            entityplayer.connection.sendPacket(new SPacketTimeUpdate(entityplayer.world.getTotalWorldTime(), entityplayer.getPlayerTime(), entityplayer.world.getGameRules().getBoolean("doDaylightCycle")));
         }
      }

      for(int i = 0; i < this.worlds.size(); ++i) {
         System.nanoTime();
         WorldServer worldserver = (WorldServer)this.worlds.get(i);
         this.methodProfiler.startSection(worldserver.getWorldInfo().getWorldName());
         this.methodProfiler.startSection("tick");

         try {
            worldserver.tick();
         } catch (Throwable var6) {
            CrashReport crashreport = CrashReport.makeCrashReport(var6, "Exception ticking world");
            worldserver.addWorldInfoToCrashReport(crashreport);
            throw new ReportedException(crashreport);
         }

         try {
            worldserver.updateEntities();
         } catch (Throwable var5) {
            CrashReport crashreport = CrashReport.makeCrashReport(var5, "Exception ticking world entities");
            worldserver.addWorldInfoToCrashReport(crashreport);
            throw new ReportedException(crashreport);
         }

         this.methodProfiler.endSection();
         this.methodProfiler.startSection("tracker");
         worldserver.getEntityTracker().tick();
         this.methodProfiler.endSection();
         this.methodProfiler.endSection();
      }

      this.methodProfiler.endStartSection("connection");
      this.am().networkTick();
      this.methodProfiler.endStartSection("players");
      this.v.onTick();
      this.methodProfiler.endStartSection("tickables");

      for(int var10 = 0; var10 < this.o.size(); ++var10) {
         ((ITickable)this.o.get(var10)).update();
      }

      this.methodProfiler.endSection();
   }

   public boolean getAllowNether() {
      return true;
   }

   public void a(ITickable itickable) {
      this.o.add(itickable);
   }

   public static void main(OptionSet options) {
      Bootstrap.register();

      try {
         String s1 = ".";
         YggdrasilAuthenticationService yggdrasilauthenticationservice = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());
         MinecraftSessionService minecraftsessionservice = yggdrasilauthenticationservice.createMinecraftSessionService();
         GameProfileRepository gameprofilerepository = yggdrasilauthenticationservice.createProfileRepository();
         PlayerProfileCache usercache = new PlayerProfileCache(gameprofilerepository, new File(s1, a.getName()));
         DedicatedServer dedicatedserver = new DedicatedServer(options, DataFixesManager.createFixer(), yggdrasilauthenticationservice, minecraftsessionservice, gameprofilerepository, usercache);
         if (options.has("port")) {
            int port = ((Integer)options.valueOf("port")).intValue();
            if (port > 0) {
               dedicatedserver.setPort(port);
            }
         }

         if (options.has("universe")) {
            dedicatedserver.universe = (File)options.valueOf("universe");
         }

         if (options.has("world")) {
            dedicatedserver.setWorld((String)options.valueOf("world"));
         }

         dedicatedserver.primaryThread.start();
      } catch (Exception var8) {
         LOGGER.fatal("Failed to start the minecraft server", var8);
      }

   }

   public void F() {
   }

   public File d(String s) {
      return new File(this.A(), s);
   }

   public void info(String s) {
      LOGGER.info(s);
   }

   public void warning(String s) {
      LOGGER.warn(s);
   }

   public WorldServer getWorldServer(int i) {
      for(WorldServer world : this.worlds) {
         if (world.dimension == i) {
            return world;
         }
      }

      return (WorldServer)this.worlds.get(0);
   }

   public String getVersion() {
      return "1.10.2";
   }

   public int H() {
      return this.v.getCurrentPlayerCount();
   }

   public int I() {
      return this.v.getMaxPlayers();
   }

   public String[] getPlayers() {
      return this.v.getOnlinePlayerNames();
   }

   public GameProfile[] K() {
      return this.v.getOnlinePlayerProfiles();
   }

   public boolean isDebugging() {
      return this.getPropertyManager().getBooleanProperty("debug", false);
   }

   public void g(String s) {
      LOGGER.error(s);
   }

   public void h(String s) {
      if (this.isDebugging()) {
         LOGGER.info(s);
      }

   }

   public String getServerModName() {
      return this.server.getName();
   }

   public CrashReport b(CrashReport crashreport) {
      crashreport.getCategory().setDetail("Profiler Position", new ICrashReportDetail() {
         public String a() throws Exception {
            return MinecraftServer.this.methodProfiler.profilingEnabled ? MinecraftServer.this.methodProfiler.getNameOfLastSection() : "N/A (disabled)";
         }

         public Object call() throws Exception {
            return this.a();
         }
      });
      if (this.v != null) {
         crashreport.getCategory().setDetail("Player Count", new ICrashReportDetail() {
            public String a() {
               return MinecraftServer.this.v.getCurrentPlayerCount() + " / " + MinecraftServer.this.v.getMaxPlayers() + "; " + MinecraftServer.this.v.getPlayers();
            }

            public Object call() throws Exception {
               return this.a();
            }
         });
      }

      return crashreport;
   }

   public List tabCompleteCommand(ICommandSender icommandlistener, String s, @Nullable BlockPos blockposition, boolean flag) {
      return this.server.tabComplete(icommandlistener, s, blockposition);
   }

   public boolean M() {
      return true;
   }

   public String getName() {
      return "Server";
   }

   public void sendMessage(ITextComponent ichatbasecomponent) {
      LOGGER.info(ichatbasecomponent.getUnformattedText());
   }

   public boolean canUseCommand(int i, String s) {
      return true;
   }

   public ICommandManager getCommandHandler() {
      return this.b;
   }

   public KeyPair O() {
      return this.H;
   }

   public int P() {
      return this.u;
   }

   public void setPort(int i) {
      this.u = i;
   }

   public String Q() {
      return this.I;
   }

   public void i(String s) {
      this.I = s;
   }

   public boolean R() {
      return this.I != null;
   }

   public String S() {
      return this.J;
   }

   public void setWorld(String s) {
      this.J = s;
   }

   public void a(KeyPair keypair) {
      this.H = keypair;
   }

   public void a(EnumDifficulty enumdifficulty) {
      int i = this.worlds.size();

      for(int j = 0; j < i; ++j) {
         WorldServer worldserver = (WorldServer)this.worlds.get(j);
         if (worldserver != null) {
            if (worldserver.getWorldInfo().isHardcoreModeEnabled()) {
               worldserver.getWorldInfo().setDifficulty(EnumDifficulty.HARD);
               worldserver.setAllowedSpawnTypes(true, true);
            } else if (this.R()) {
               worldserver.getWorldInfo().setDifficulty(enumdifficulty);
               worldserver.setAllowedSpawnTypes(worldserver.getDifficulty() != EnumDifficulty.PEACEFUL, true);
            } else {
               worldserver.getWorldInfo().setDifficulty(enumdifficulty);
               worldserver.setAllowedSpawnTypes(this.getSpawnMonsters(), this.spawnAnimals);
            }
         }
      }

   }

   public boolean getSpawnMonsters() {
      return true;
   }

   public boolean V() {
      return this.demoMode;
   }

   public void b(boolean flag) {
      this.demoMode = flag;
   }

   public void c(boolean flag) {
      this.M = flag;
   }

   public ISaveFormat getConvertable() {
      return this.convertable;
   }

   public String getResourcePack() {
      return this.N;
   }

   public String getResourcePackHash() {
      return this.O;
   }

   public void setResourcePack(String s, String s1) {
      this.N = s;
      this.O = s1;
   }

   public void addServerStatsToSnooper(Snooper mojangstatisticsgenerator) {
      mojangstatisticsgenerator.addClientStat("whitelist_enabled", Boolean.valueOf(false));
      mojangstatisticsgenerator.addClientStat("whitelist_count", Integer.valueOf(0));
      if (this.v != null) {
         mojangstatisticsgenerator.addClientStat("players_current", Integer.valueOf(this.H()));
         mojangstatisticsgenerator.addClientStat("players_max", Integer.valueOf(this.I()));
         mojangstatisticsgenerator.addClientStat("players_seen", Integer.valueOf(this.v.getAvailablePlayerDat().length));
      }

      mojangstatisticsgenerator.addClientStat("uses_auth", Boolean.valueOf(this.onlineMode));
      mojangstatisticsgenerator.addClientStat("gui_state", this.ao() ? "enabled" : "disabled");
      mojangstatisticsgenerator.addClientStat("run_time", Long.valueOf((av() - mojangstatisticsgenerator.getMinecraftStartTimeMillis()) / 60L * 1000L));
      mojangstatisticsgenerator.addClientStat("avg_tick_ms", Integer.valueOf((int)(MathHelper.average(this.h) * 1.0E-6D)));
      int i = 0;
      if (this.worldServer != null) {
         for(int j = 0; j < this.worlds.size(); ++j) {
            WorldServer worldserver = (WorldServer)this.worlds.get(j);
            if (worldserver != null) {
               WorldInfo worlddata = worldserver.getWorldInfo();
               mojangstatisticsgenerator.addClientStat("world[" + i + "][dimension]", Integer.valueOf(worldserver.provider.getDimensionType().getId()));
               mojangstatisticsgenerator.addClientStat("world[" + i + "][mode]", worlddata.getGameType());
               mojangstatisticsgenerator.addClientStat("world[" + i + "][difficulty]", worldserver.getDifficulty());
               mojangstatisticsgenerator.addClientStat("world[" + i + "][hardcore]", Boolean.valueOf(worlddata.isHardcoreModeEnabled()));
               mojangstatisticsgenerator.addClientStat("world[" + i + "][generator_name]", worlddata.getTerrainType().getName());
               mojangstatisticsgenerator.addClientStat("world[" + i + "][generator_version]", Integer.valueOf(worlddata.getTerrainType().getGeneratorVersion()));
               mojangstatisticsgenerator.addClientStat("world[" + i + "][height]", Integer.valueOf(this.F));
               mojangstatisticsgenerator.addClientStat("world[" + i + "][chunks_loaded]", Integer.valueOf(worldserver.getChunkProvider().getLoadedChunkCount()));
               ++i;
            }
         }
      }

      mojangstatisticsgenerator.addClientStat("worlds", Integer.valueOf(i));
   }

   public void addServerTypeToSnooper(Snooper mojangstatisticsgenerator) {
      mojangstatisticsgenerator.addStatToSnooper("singleplayer", Boolean.valueOf(this.R()));
      mojangstatisticsgenerator.addStatToSnooper("server_brand", this.getServerModName());
      mojangstatisticsgenerator.addStatToSnooper("gui_supported", GraphicsEnvironment.isHeadless() ? "headless" : "supported");
      mojangstatisticsgenerator.addStatToSnooper("dedicated", Boolean.valueOf(this.aa()));
   }

   public boolean isSnooperEnabled() {
      return true;
   }

   public abstract boolean aa();

   public boolean getOnlineMode() {
      return this.server.getOnlineMode();
   }

   public void setOnlineMode(boolean flag) {
      this.onlineMode = flag;
   }

   public boolean getSpawnAnimals() {
      return this.spawnAnimals;
   }

   public void setSpawnAnimals(boolean flag) {
      this.spawnAnimals = flag;
   }

   public boolean getSpawnNPCs() {
      return this.spawnNPCs;
   }

   public abstract boolean ae();

   public void setSpawnNPCs(boolean flag) {
      this.spawnNPCs = flag;
   }

   public boolean getPVP() {
      return this.pvpMode;
   }

   public void setPVP(boolean flag) {
      this.pvpMode = flag;
   }

   public boolean getAllowFlight() {
      return this.allowFlight;
   }

   public void setAllowFlight(boolean flag) {
      this.allowFlight = flag;
   }

   public abstract boolean getEnableCommandBlock();

   public String getMotd() {
      return this.motd;
   }

   public void setMotd(String s) {
      this.motd = s;
   }

   public int getMaxBuildHeight() {
      return this.F;
   }

   public void c(int i) {
      this.F = i;
   }

   public boolean isStopped() {
      return this.isStopped;
   }

   public PlayerList getPlayerList() {
      return this.v;
   }

   public void a(PlayerList playerlist) {
      this.v = playerlist;
   }

   public void setGamemode(GameType enumgamemode) {
      for(int i = 0; i < this.worlds.size(); ++i) {
         ((WorldServer)this.worlds.get(i)).getWorldInfo().setGameType(enumgamemode);
      }

   }

   public NetworkSystem am() {
      return this.p;
   }

   public boolean ao() {
      return false;
   }

   public abstract String a(GameType var1, boolean var2);

   public int ap() {
      return this.ticks;
   }

   public void aq() {
      this.S = true;
   }

   public BlockPos getPosition() {
      return BlockPos.ORIGIN;
   }

   public Vec3d getPositionVector() {
      return Vec3d.ZERO;
   }

   public World getEntityWorld() {
      return (World)this.worlds.get(0);
   }

   public Entity getCommandSenderEntity() {
      return null;
   }

   public int getSpawnProtection() {
      return 16;
   }

   public boolean a(World world, BlockPos blockposition, EntityPlayer entityhuman) {
      return false;
   }

   public void setForceGamemode(boolean flag) {
      this.T = flag;
   }

   public boolean getForceGamemode() {
      return this.T;
   }

   public Proxy au() {
      return this.e;
   }

   public static long av() {
      return System.currentTimeMillis();
   }

   public int getIdleTimeout() {
      return this.G;
   }

   public void setIdleTimeout(int i) {
      this.G = i;
   }

   public ITextComponent getDisplayName() {
      return new TextComponentString(this.getName());
   }

   public boolean ax() {
      return true;
   }

   public MinecraftSessionService ay() {
      return this.V;
   }

   public GameProfileRepository getGameProfileRepository() {
      return this.W;
   }

   public PlayerProfileCache getUserCache() {
      return this.X;
   }

   public ServerStatusResponse getServerPing() {
      return this.q;
   }

   public void aC() {
      this.Y = 0L;
   }

   @Nullable
   public Entity a(UUID uuid) {
      WorldServer[] aworldserver = this.worldServer;
      int var10000 = aworldserver.length;

      for(int j = 0; j < this.worlds.size(); ++j) {
         WorldServer worldserver = (WorldServer)this.worlds.get(j);
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
      return ((WorldServer)this.worlds.get(0)).getGameRules().getBoolean("sendCommandFeedback");
   }

   public void setCommandStat(CommandResultStats.Type commandobjectiveexecutor_enumcommandresult, int i) {
   }

   public MinecraftServer h() {
      return this;
   }

   public int aD() {
      return 29999984;
   }

   public ListenableFuture a(Callable callable) {
      Validate.notNull(callable);
      if (!this.isCallingFromMinecraftThread()) {
         ListenableFutureTask listenablefuturetask = ListenableFutureTask.create(callable);
         synchronized(this.j) {
            this.j.add(listenablefuturetask);
            return listenablefuturetask;
         }
      } else {
         try {
            return Futures.immediateFuture(callable.call());
         } catch (Exception var5) {
            return Futures.immediateFailedCheckedFuture(var5);
         }
      }
   }

   public ListenableFuture addScheduledTask(Runnable runnable) {
      Validate.notNull(runnable);
      return this.a(Executors.callable(runnable));
   }

   public boolean isCallingFromMinecraftThread() {
      return Thread.currentThread() == this.serverThread;
   }

   public int aF() {
      return 256;
   }

   public long aG() {
      return this.aa;
   }

   public Thread aH() {
      return this.serverThread;
   }

   public DataFixer getDataConverterManager() {
      return this.dataConverterManager;
   }

   public int a(@Nullable WorldServer worldserver) {
      return worldserver != null ? worldserver.getGameRules().getInt("spawnRadius") : 10;
   }

   /** @deprecated */
   @Deprecated
   public static MinecraftServer getServer() {
      return Bukkit.getServer() instanceof CraftServer ? ((CraftServer)Bukkit.getServer()).getServer() : null;
   }
}
