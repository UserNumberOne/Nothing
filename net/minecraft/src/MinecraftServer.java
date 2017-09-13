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

   public MinecraftServer(OptionSet var1, Proxy var2, DataFixer var3, YggdrasilAuthenticationService var4, MinecraftSessionService var5, GameProfileRepository var6, PlayerProfileCache var7) {
      this.e = var2;
      this.U = var4;
      this.V = var5;
      this.W = var6;
      this.X = var7;
      this.p = new NetworkSystem(this);
      this.b = this.i();
      this.dataConverterManager = var3;
      this.options = var1;
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

   protected void a(String var1) {
      if (this.getConvertable().isOldMapFormat(var1)) {
         LOGGER.info("Converting map!");
         this.b("menu.convertingLevel");
         this.getConvertable().convertMapFormat(var1, new IProgressUpdate() {
            private long b = System.currentTimeMillis();

            public void displaySavingString(String var1) {
            }

            public void setLoadingProgress(int var1) {
               if (System.currentTimeMillis() - this.b >= 1000L) {
                  this.b = System.currentTimeMillis();
                  MinecraftServer.LOGGER.info("Converting... {}%", new Object[]{var1});
               }

            }

            public void displayLoadingString(String var1) {
            }
         });
      }

   }

   protected synchronized void b(String var1) {
      this.R = var1;
   }

   public void a(String var1, String var2, long var3, WorldType var5, String var6) {
      this.a(var1);
      this.b("menu.loadingLevel");
      this.worldServer = new WorldServer[3];
      byte var7 = 3;

      for(int var8 = 0; var8 < var7; ++var8) {
         byte var9 = 0;
         if (var8 == 1) {
            if (!this.getAllowNether()) {
               continue;
            }

            var9 = -1;
         }

         if (var8 == 2) {
            if (!this.server.getAllowEnd()) {
               continue;
            }

            var9 = 1;
         }

         String var10 = Environment.getEnvironment(var9).toString().toLowerCase();
         String var11 = var9 == 0 ? var1 : var1 + "_" + var10;
         ChunkGenerator var12 = this.server.getGenerator(var11);
         WorldSettings var13 = new WorldSettings(var3, this.getGamemode(), this.getGenerateStructures(), this.isHardcore(), var5);
         var13.setGeneratorOptions(var6);
         WorldServer var16;
         if (var8 == 0) {
            AnvilSaveHandler var14 = new AnvilSaveHandler(this.server.getWorldContainer(), var2, true, this.dataConverterManager);
            WorldInfo var15 = var14.loadWorldInfo();
            if (var15 == null) {
               var15 = new WorldInfo(var13, var2);
            }

            var15.checkName(var2);
            if (this.V()) {
               var16 = (WorldServer)(new DemoWorldServer(this, var14, var15, var9, this.methodProfiler)).init();
            } else {
               var16 = (WorldServer)(new WorldServer(this, var14, var15, var9, this.methodProfiler, Environment.getEnvironment(var9), var12)).init();
            }

            var16.initialize(var13);
            this.server.scoreboardManager = new CraftScoreboardManager(this, var16.getScoreboard());
         } else {
            String var21 = "DIM" + var9;
            File var22 = new File(new File(var11), var21);
            File var17 = new File(new File(var1), var21);
            if (!var22.isDirectory() && var17.isDirectory()) {
               LOGGER.info("---- Migration of old " + var10 + " folder required ----");
               LOGGER.info("Unfortunately due to the way that Minecraft implemented multiworld support in 1.6, Bukkit requires that you move your " + var10 + " folder to a new location in order to operate correctly.");
               LOGGER.info("We will move this folder for you, but it will mean that you need to move it back should you wish to stop using Bukkit in the future.");
               LOGGER.info("Attempting to move " + var17 + " to " + var22 + "...");
               if (var22.exists()) {
                  LOGGER.warn("A file or folder already exists at " + var22 + "!");
                  LOGGER.info("---- Migration of old " + var10 + " folder failed ----");
               } else if (var22.getParentFile().mkdirs()) {
                  if (var17.renameTo(var22)) {
                     LOGGER.info("Success! To restore " + var10 + " in the future, simply move " + var22 + " to " + var17);

                     try {
                        Files.copy(new File(new File(var1), "level.dat"), new File(new File(var11), "level.dat"));
                        FileUtils.copyDirectory(new File(new File(var1), "data"), new File(new File(var11), "data"));
                     } catch (IOException var20) {
                        LOGGER.warn("Unable to migrate world data.");
                     }

                     LOGGER.info("---- Migration of old " + var10 + " folder complete ----");
                  } else {
                     LOGGER.warn("Could not move folder " + var17 + " to " + var22 + "!");
                     LOGGER.info("---- Migration of old " + var10 + " folder failed ----");
                  }
               } else {
                  LOGGER.warn("Could not create path for " + var22 + "!");
                  LOGGER.info("---- Migration of old " + var10 + " folder failed ----");
               }
            }

            AnvilSaveHandler var18 = new AnvilSaveHandler(this.server.getWorldContainer(), var11, true, this.dataConverterManager);
            WorldInfo var19 = var18.loadWorldInfo();
            if (var19 == null) {
               var19 = new WorldInfo(var13, var11);
            }

            var19.checkName(var11);
            var16 = (WorldServer)(new WorldServerMulti(this, var18, var9, (WorldServer)this.worlds.get(0), this.methodProfiler, var19, Environment.getEnvironment(var9), var12)).init();
         }

         this.server.getPluginManager().callEvent(new WorldInitEvent(var16.getWorld()));
         var16.addEventListener(new ServerWorldEventHandler(this, var16));
         if (!this.R()) {
            var16.getWorldInfo().setGameType(this.getGamemode());
         }

         this.worlds.add(var16);
         this.getPlayerList().setPlayerManager((WorldServer[])this.worlds.toArray(new WorldServer[this.worlds.size()]));
      }

      this.v.setPlayerManager(this.worldServer);
      this.a(this.getDifficulty());
      this.l();
   }

   protected void l() {
      int var1 = 0;
      this.b("menu.generatingTerrain");

      for(int var2 = 0; var2 < this.worlds.size(); ++var2) {
         WorldServer var3 = (WorldServer)this.worlds.get(var2);
         LOGGER.info("Preparing start region for level " + var2 + " (Seed: " + var3.getSeed() + ")");
         if (var3.getWorld().getKeepSpawnInMemory()) {
            BlockPos var4 = var3.getSpawnPoint();
            long var5 = av();
            var1 = 0;

            for(int var7 = -192; var7 <= 192 && this.isRunning(); var7 += 16) {
               for(int var8 = -192; var8 <= 192 && this.isRunning(); var8 += 16) {
                  long var9 = av();
                  if (var9 - var5 > 1000L) {
                     this.a_("Preparing spawn area", var1 * 100 / 625);
                     var5 = var9;
                  }

                  ++var1;
                  var3.getChunkProvider().provideChunk(var4.getX() + var7 >> 4, var4.getZ() + var8 >> 4);
               }
            }
         }
      }

      for(WorldServer var12 : this.worlds) {
         this.server.getPluginManager().callEvent(new WorldLoadEvent(var12.getWorld()));
      }

      this.t();
   }

   protected void a(String var1, ISaveHandler var2) {
      File var3 = new File(var2.getWorldDirectory(), "resources.zip");
      if (var3.isFile()) {
         this.setResourcePack("level://" + var1 + "/" + "resources.zip", "");
      }

   }

   public abstract boolean getGenerateStructures();

   public abstract GameType getGamemode();

   public abstract EnumDifficulty getDifficulty();

   public abstract boolean isHardcore();

   public abstract int q();

   public abstract boolean r();

   public abstract boolean s();

   protected void a_(String var1, int var2) {
      this.f = var1;
      this.g = var2;
      LOGGER.info("{}: {}%", new Object[]{var1, var2});
   }

   protected void t() {
      this.f = null;
      this.g = 0;
      this.server.enablePlugins(PluginLoadOrder.POSTWORLD);
   }

   protected void saveChunks(boolean var1) {
      WorldServer[] var2 = this.worldServer;
      int var10000 = var2.length;

      for(int var3 = 0; var3 < this.worlds.size(); ++var3) {
         WorldServer var4 = (WorldServer)this.worlds.get(var3);
         if (var4 != null) {
            if (!var1) {
               LOGGER.info("Saving chunks for level '{}'/{}", new Object[]{var4.getWorldInfo().getWorldName(), var4.provider.getDimensionType().getName()});
            }

            try {
               var4.saveAllChunks(true, (IProgressUpdate)null);
               var4.flush();
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

         for(WorldServer var4 : this.worldServer) {
            if (var4 != null) {
               var4.disableLevelSaving = false;
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

   public void c(String var1) {
      this.serverIp = var1;
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
            long var1 = 0L;
            this.q.setServerDescription(new TextComponentString(this.motd));
            this.q.setVersion(new ServerStatusResponse.Version("1.10.2", 210));
            this.a(this.q);

            while(this.isRunning) {
               long var3 = av();
               long var5 = var3 - this.aa;
               if (var5 > 2000L && this.aa - this.Q >= 15000L) {
                  if (this.server.getWarnOnOverload()) {
                     LOGGER.warn("Can't keep up! Did the system time change, or is the server overloaded? Running {}ms behind, skipping {} tick(s)", new Object[]{var5, var5 / 50L});
                  }

                  var5 = 2000L;
                  this.Q = this.aa;
               }

               if (var5 < 0L) {
                  LOGGER.warn("Time ran backwards! Did the system time change?");
                  var5 = 0L;
               }

               var1 += var5;
               this.aa = var3;
               if (((WorldServer)this.worlds.get(0)).areAllPlayersAsleep()) {
                  this.C();
                  var1 = 0L;
               } else {
                  while(var1 > 50L) {
                     currentTick = (int)(System.currentTimeMillis() / 50L);
                     var1 -= 50L;
                     this.C();
                  }
               }

               Thread.sleep(Math.max(1L, 50L - var1));
               this.P = true;
            }
         } else {
            this.a((CrashReport)null);
         }
      } catch (Throwable var72) {
         LOGGER.error("Encountered an unexpected exception", var72);
         CrashReport var8 = null;
         if (var72 instanceof ReportedException) {
            var8 = this.b(((ReportedException)var72).getCrashReport());
         } else {
            var8 = this.b(new CrashReport("Exception in server tick loop", var72));
         }

         File var9 = new File(new File(this.A(), "crash-reports"), "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-server.txt");
         if (var8.saveToFile(var9)) {
            LOGGER.error("This crash report has been saved to: {}", new Object[]{var9.getAbsolutePath()});
         } else {
            LOGGER.error("We were unable to save this crash report to disk.");
         }

         this.a(var8);
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

   public void a(ServerStatusResponse var1) {
      File var2 = this.d("server-icon.png");
      if (!var2.exists()) {
         var2 = this.getConvertable().getFile(this.S(), "icon.png");
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
            LOGGER.error("Couldn't load server icon", var9);
         } finally {
            var3.release();
         }
      }

   }

   public File A() {
      return new File(".");
   }

   public void a(CrashReport var1) {
   }

   public void B() {
   }

   protected void C() throws MinecraftException {
      long var1 = System.nanoTime();
      ++this.ticks;
      if (this.S) {
         this.S = false;
         this.methodProfiler.profilingEnabled = true;
         this.methodProfiler.clearProfiling();
      }

      this.methodProfiler.startSection("root");
      this.D();
      if (var1 - this.Y >= 5000000000L) {
         this.Y = var1;
         this.q.setPlayers(new ServerStatusResponse.Players(this.I(), this.H()));
         GameProfile[] var3 = new GameProfile[Math.min(this.H(), 12)];
         int var4 = MathHelper.getInt(this.r, 0, this.H() - var3.length);

         for(int var5 = 0; var5 < var3.length; ++var5) {
            var3[var5] = ((EntityPlayerMP)this.v.getPlayers().get(var4 + var5)).getGameProfile();
         }

         Collections.shuffle(Arrays.asList(var3));
         this.q.getPlayers().setPlayers(var3);
      }

      if (this.autosavePeriod > 0 && this.ticks % this.autosavePeriod == 0) {
         this.methodProfiler.startSection("save");
         this.v.saveAllPlayerData();
         this.saveChunks(true);
         this.methodProfiler.endSection();
      }

      this.methodProfiler.startSection("tallying");
      this.h[this.ticks % 100] = System.nanoTime() - var1;
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
         for(int var8 = 0; var8 < this.getPlayerList().playerEntityList.size(); ++var8) {
            EntityPlayerMP var2 = (EntityPlayerMP)this.getPlayerList().playerEntityList.get(var8);
            var2.connection.sendPacket(new SPacketTimeUpdate(var2.world.getTotalWorldTime(), var2.getPlayerTime(), var2.world.getGameRules().getBoolean("doDaylightCycle")));
         }
      }

      for(int var9 = 0; var9 < this.worlds.size(); ++var9) {
         System.nanoTime();
         WorldServer var11 = (WorldServer)this.worlds.get(var9);
         this.methodProfiler.startSection(var11.getWorldInfo().getWorldName());
         this.methodProfiler.startSection("tick");

         try {
            var11.tick();
         } catch (Throwable var6) {
            CrashReport var4 = CrashReport.makeCrashReport(var6, "Exception ticking world");
            var11.addWorldInfoToCrashReport(var4);
            throw new ReportedException(var4);
         }

         try {
            var11.updateEntities();
         } catch (Throwable var5) {
            CrashReport var12 = CrashReport.makeCrashReport(var5, "Exception ticking world entities");
            var11.addWorldInfoToCrashReport(var12);
            throw new ReportedException(var12);
         }

         this.methodProfiler.endSection();
         this.methodProfiler.startSection("tracker");
         var11.getEntityTracker().tick();
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

   public void a(ITickable var1) {
      this.o.add(var1);
   }

   public static void main(OptionSet var0) {
      Bootstrap.register();

      try {
         String var1 = ".";
         YggdrasilAuthenticationService var2 = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());
         MinecraftSessionService var3 = var2.createMinecraftSessionService();
         GameProfileRepository var4 = var2.createProfileRepository();
         PlayerProfileCache var5 = new PlayerProfileCache(var4, new File(var1, a.getName()));
         DedicatedServer var6 = new DedicatedServer(var0, DataFixesManager.createFixer(), var2, var3, var4, var5);
         if (var0.has("port")) {
            int var7 = ((Integer)var0.valueOf("port")).intValue();
            if (var7 > 0) {
               var6.setPort(var7);
            }
         }

         if (var0.has("universe")) {
            var6.universe = (File)var0.valueOf("universe");
         }

         if (var0.has("world")) {
            var6.setWorld((String)var0.valueOf("world"));
         }

         var6.primaryThread.start();
      } catch (Exception var8) {
         LOGGER.fatal("Failed to start the minecraft server", var8);
      }

   }

   public void F() {
   }

   public File d(String var1) {
      return new File(this.A(), var1);
   }

   public void info(String var1) {
      LOGGER.info(var1);
   }

   public void warning(String var1) {
      LOGGER.warn(var1);
   }

   public WorldServer getWorldServer(int var1) {
      for(WorldServer var3 : this.worlds) {
         if (var3.dimension == var1) {
            return var3;
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

   public void g(String var1) {
      LOGGER.error(var1);
   }

   public void h(String var1) {
      if (this.isDebugging()) {
         LOGGER.info(var1);
      }

   }

   public String getServerModName() {
      return this.server.getName();
   }

   public CrashReport b(CrashReport var1) {
      var1.getCategory().setDetail("Profiler Position", new ICrashReportDetail() {
         public String a() throws Exception {
            return MinecraftServer.this.methodProfiler.profilingEnabled ? MinecraftServer.this.methodProfiler.getNameOfLastSection() : "N/A (disabled)";
         }

         public Object call() throws Exception {
            return this.a();
         }
      });
      if (this.v != null) {
         var1.getCategory().setDetail("Player Count", new ICrashReportDetail() {
            public String a() {
               return MinecraftServer.this.v.getCurrentPlayerCount() + " / " + MinecraftServer.this.v.getMaxPlayers() + "; " + MinecraftServer.this.v.getPlayers();
            }

            public Object call() throws Exception {
               return this.a();
            }
         });
      }

      return var1;
   }

   public List tabCompleteCommand(ICommandSender var1, String var2, @Nullable BlockPos var3, boolean var4) {
      return this.server.tabComplete(var1, var2, var3);
   }

   public boolean M() {
      return true;
   }

   public String getName() {
      return "Server";
   }

   public void sendMessage(ITextComponent var1) {
      LOGGER.info(var1.getUnformattedText());
   }

   public boolean canUseCommand(int var1, String var2) {
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

   public void setPort(int var1) {
      this.u = var1;
   }

   public String Q() {
      return this.I;
   }

   public void i(String var1) {
      this.I = var1;
   }

   public boolean R() {
      return this.I != null;
   }

   public String S() {
      return this.J;
   }

   public void setWorld(String var1) {
      this.J = var1;
   }

   public void a(KeyPair var1) {
      this.H = var1;
   }

   public void a(EnumDifficulty var1) {
      int var2 = this.worlds.size();

      for(int var3 = 0; var3 < var2; ++var3) {
         WorldServer var4 = (WorldServer)this.worlds.get(var3);
         if (var4 != null) {
            if (var4.getWorldInfo().isHardcoreModeEnabled()) {
               var4.getWorldInfo().setDifficulty(EnumDifficulty.HARD);
               var4.setAllowedSpawnTypes(true, true);
            } else if (this.R()) {
               var4.getWorldInfo().setDifficulty(var1);
               var4.setAllowedSpawnTypes(var4.getDifficulty() != EnumDifficulty.PEACEFUL, true);
            } else {
               var4.getWorldInfo().setDifficulty(var1);
               var4.setAllowedSpawnTypes(this.getSpawnMonsters(), this.spawnAnimals);
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

   public void b(boolean var1) {
      this.demoMode = var1;
   }

   public void c(boolean var1) {
      this.M = var1;
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

   public void setResourcePack(String var1, String var2) {
      this.N = var1;
      this.O = var2;
   }

   public void addServerStatsToSnooper(Snooper var1) {
      var1.addClientStat("whitelist_enabled", Boolean.valueOf(false));
      var1.addClientStat("whitelist_count", Integer.valueOf(0));
      if (this.v != null) {
         var1.addClientStat("players_current", Integer.valueOf(this.H()));
         var1.addClientStat("players_max", Integer.valueOf(this.I()));
         var1.addClientStat("players_seen", Integer.valueOf(this.v.getAvailablePlayerDat().length));
      }

      var1.addClientStat("uses_auth", Boolean.valueOf(this.onlineMode));
      var1.addClientStat("gui_state", this.ao() ? "enabled" : "disabled");
      var1.addClientStat("run_time", Long.valueOf((av() - var1.getMinecraftStartTimeMillis()) / 60L * 1000L));
      var1.addClientStat("avg_tick_ms", Integer.valueOf((int)(MathHelper.average(this.h) * 1.0E-6D)));
      int var2 = 0;
      if (this.worldServer != null) {
         for(int var3 = 0; var3 < this.worlds.size(); ++var3) {
            WorldServer var4 = (WorldServer)this.worlds.get(var3);
            if (var4 != null) {
               WorldInfo var5 = var4.getWorldInfo();
               var1.addClientStat("world[" + var2 + "][dimension]", Integer.valueOf(var4.provider.getDimensionType().getId()));
               var1.addClientStat("world[" + var2 + "][mode]", var5.getGameType());
               var1.addClientStat("world[" + var2 + "][difficulty]", var4.getDifficulty());
               var1.addClientStat("world[" + var2 + "][hardcore]", Boolean.valueOf(var5.isHardcoreModeEnabled()));
               var1.addClientStat("world[" + var2 + "][generator_name]", var5.getTerrainType().getName());
               var1.addClientStat("world[" + var2 + "][generator_version]", Integer.valueOf(var5.getTerrainType().getGeneratorVersion()));
               var1.addClientStat("world[" + var2 + "][height]", Integer.valueOf(this.F));
               var1.addClientStat("world[" + var2 + "][chunks_loaded]", Integer.valueOf(var4.getChunkProvider().getLoadedChunkCount()));
               ++var2;
            }
         }
      }

      var1.addClientStat("worlds", Integer.valueOf(var2));
   }

   public void addServerTypeToSnooper(Snooper var1) {
      var1.addStatToSnooper("singleplayer", Boolean.valueOf(this.R()));
      var1.addStatToSnooper("server_brand", this.getServerModName());
      var1.addStatToSnooper("gui_supported", GraphicsEnvironment.isHeadless() ? "headless" : "supported");
      var1.addStatToSnooper("dedicated", Boolean.valueOf(this.aa()));
   }

   public boolean isSnooperEnabled() {
      return true;
   }

   public abstract boolean aa();

   public boolean getOnlineMode() {
      return this.server.getOnlineMode();
   }

   public void setOnlineMode(boolean var1) {
      this.onlineMode = var1;
   }

   public boolean getSpawnAnimals() {
      return this.spawnAnimals;
   }

   public void setSpawnAnimals(boolean var1) {
      this.spawnAnimals = var1;
   }

   public boolean getSpawnNPCs() {
      return this.spawnNPCs;
   }

   public abstract boolean ae();

   public void setSpawnNPCs(boolean var1) {
      this.spawnNPCs = var1;
   }

   public boolean getPVP() {
      return this.pvpMode;
   }

   public void setPVP(boolean var1) {
      this.pvpMode = var1;
   }

   public boolean getAllowFlight() {
      return this.allowFlight;
   }

   public void setAllowFlight(boolean var1) {
      this.allowFlight = var1;
   }

   public abstract boolean getEnableCommandBlock();

   public String getMotd() {
      return this.motd;
   }

   public void setMotd(String var1) {
      this.motd = var1;
   }

   public int getMaxBuildHeight() {
      return this.F;
   }

   public void c(int var1) {
      this.F = var1;
   }

   public boolean isStopped() {
      return this.isStopped;
   }

   public PlayerList getPlayerList() {
      return this.v;
   }

   public void a(PlayerList var1) {
      this.v = var1;
   }

   public void setGamemode(GameType var1) {
      for(int var2 = 0; var2 < this.worlds.size(); ++var2) {
         ((WorldServer)this.worlds.get(var2)).getWorldInfo().setGameType(var1);
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

   public boolean a(World var1, BlockPos var2, EntityPlayer var3) {
      return false;
   }

   public void setForceGamemode(boolean var1) {
      this.T = var1;
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

   public void setIdleTimeout(int var1) {
      this.G = var1;
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
   public Entity a(UUID var1) {
      WorldServer[] var2 = this.worldServer;
      int var10000 = var2.length;

      for(int var3 = 0; var3 < this.worlds.size(); ++var3) {
         WorldServer var4 = (WorldServer)this.worlds.get(var3);
         if (var4 != null) {
            Entity var5 = var4.getEntityFromUuid(var1);
            if (var5 != null) {
               return var5;
            }
         }
      }

      return null;
   }

   public boolean sendCommandFeedback() {
      return ((WorldServer)this.worlds.get(0)).getGameRules().getBoolean("sendCommandFeedback");
   }

   public void setCommandStat(CommandResultStats.Type var1, int var2) {
   }

   public MinecraftServer h() {
      return this;
   }

   public int aD() {
      return 29999984;
   }

   public ListenableFuture a(Callable var1) {
      Validate.notNull(var1);
      if (!this.isCallingFromMinecraftThread()) {
         ListenableFutureTask var2 = ListenableFutureTask.create(var1);
         synchronized(this.j) {
            this.j.add(var2);
            return var2;
         }
      } else {
         try {
            return Futures.immediateFuture(var1.call());
         } catch (Exception var5) {
            return Futures.immediateFailedCheckedFuture(var5);
         }
      }
   }

   public ListenableFuture addScheduledTask(Runnable var1) {
      Validate.notNull(var1);
      return this.a(Executors.callable(var1));
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

   public int a(@Nullable WorldServer var1) {
      return var1 != null ? var1.getGameRules().getInt("spawnRadius") : 10;
   }

   /** @deprecated */
   @Deprecated
   public static MinecraftServer getServer() {
      return Bukkit.getServer() instanceof CraftServer ? ((CraftServer)Bukkit.getServer()).getServer() : null;
   }
}
