package net.minecraft.server.dedicated;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.regex.Pattern;
import net.minecraft.command.ICommandSender;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.rcon.IServer;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.network.rcon.RConThreadMain;
import net.minecraft.network.rcon.RConThreadQuery;
import net.minecraft.profiler.Snooper;
import net.minecraft.server.ServerEula;
import net.minecraft.server.gui.MinecraftServerGui;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.src.MinecraftServer;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.CryptManager;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.bukkit.craftbukkit.Main;
import org.bukkit.craftbukkit.libs.jline.console.ConsoleReader;
import org.bukkit.craftbukkit.libs.joptsimple.OptionSet;
import org.bukkit.craftbukkit.v1_10_R1.LoggerOutputStream;
import org.bukkit.craftbukkit.v1_10_R1.command.CraftRemoteConsoleCommandSender;
import org.bukkit.craftbukkit.v1_10_R1.util.ForwardLogHandler;
import org.bukkit.craftbukkit.v1_10_R1.util.TerminalConsoleWriterThread;
import org.bukkit.craftbukkit.v1_10_R1.util.Waitable;
import org.bukkit.event.server.RemoteServerCommandEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.Plugin;

public class DedicatedServer extends MinecraftServer implements IServer {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Pattern RESOURCE_PACK_SHA1_PATTERN = Pattern.compile("^[a-fA-F0-9]{40}$");
   private final List pendingCommandList = Collections.synchronizedList(Lists.newArrayList());
   private RConThreadQuery theRConThreadQuery;
   public final RConConsoleSource rconConsoleSource = new RConConsoleSource(this);
   private RConThreadMain theRConThreadMain;
   public PropertyManager settings;
   private ServerEula eula;
   private boolean canSpawnStructures;
   private GameType gameType;
   private boolean guiIsEnabled;

   public DedicatedServer(OptionSet options, DataFixer dataconvertermanager, YggdrasilAuthenticationService yggdrasilauthenticationservice, MinecraftSessionService minecraftsessionservice, GameProfileRepository gameprofilerepository, PlayerProfileCache usercache) {
      super(options, Proxy.NO_PROXY, dataconvertermanager, yggdrasilauthenticationservice, minecraftsessionservice, gameprofilerepository, usercache);
      Thread var10000 = new Thread("Server Infinisleeper") {
         {
            this.setDaemon(true);
            this.start();
         }

         public void run() {
            while(true) {
               try {
                  Thread.sleep(2147483647L);
               } catch (InterruptedException var1) {
                  ;
               }
            }
         }
      };
   }

   public boolean init() throws IOException {
      Thread thread = new Thread("Server console handler") {
         public void run() {
            if (Main.useConsole) {
               ConsoleReader bufferedreader = DedicatedServer.this.reader;

               try {
                  while(!DedicatedServer.this.isStopped() && DedicatedServer.this.isRunning()) {
                     String s;
                     if (Main.useJline) {
                        s = bufferedreader.readLine(">", (Character)null);
                     } else {
                        s = bufferedreader.readLine();
                     }

                     if (s != null && s.trim().length() > 0) {
                        DedicatedServer.this.addPendingCommand(s, DedicatedServer.this);
                     }
                  }
               } catch (IOException var4) {
                  DedicatedServer.LOGGER.error("Exception handling console input", var4);
               }

            }
         }
      };
      java.util.logging.Logger global = java.util.logging.Logger.getLogger("");
      global.setUseParentHandlers(false);

      Handler[] ioexception;
      for(Handler handler : ioexception = global.getHandlers()) {
         global.removeHandler(handler);
      }

      global.addHandler(new ForwardLogHandler());
      org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger)LogManager.getRootLogger();

      for(Appender appender : logger.getAppenders().values()) {
         if (appender instanceof ConsoleAppender) {
            logger.removeAppender(appender);
         }
      }

      (new Thread(new TerminalConsoleWriterThread(System.out, this.reader))).start();
      System.setOut(new PrintStream(new LoggerOutputStream(logger, Level.INFO), true));
      System.setErr(new PrintStream(new LoggerOutputStream(logger, Level.WARN), true));
      thread.setDaemon(true);
      thread.start();
      LOGGER.info("Starting minecraft server version 1.10.2");
      if (Runtime.getRuntime().maxMemory() / 1024L / 1024L < 512L) {
         LOGGER.warn("To start the server with more ram, launch it as \"java -Xmx1024M -Xms1024M -jar minecraft_server.jar\"");
      }

      LOGGER.info("Loading properties");
      this.settings = new PropertyManager(this.options);
      this.eula = new ServerEula(new File("eula.txt"));
      if (!this.eula.hasAcceptedEULA()) {
         LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
         this.eula.createEULAFile();
         return false;
      } else {
         if (this.R()) {
            this.c("127.0.0.1");
         } else {
            this.setOnlineMode(this.settings.getBooleanProperty("online-mode", true));
            this.c(this.settings.getStringProperty("server-ip", ""));
         }

         this.setSpawnAnimals(this.settings.getBooleanProperty("spawn-animals", true));
         this.setSpawnNPCs(this.settings.getBooleanProperty("spawn-npcs", true));
         this.setPVP(this.settings.getBooleanProperty("pvp", true));
         this.setAllowFlight(this.settings.getBooleanProperty("allow-flight", false));
         this.setResourcePack(this.settings.getStringProperty("resource-pack", ""), this.loadResourcePackSHA());
         this.setMotd(this.settings.getStringProperty("motd", "A Minecraft Server"));
         this.setForceGamemode(this.settings.getBooleanProperty("force-gamemode", false));
         this.setIdleTimeout(this.settings.getIntProperty("player-idle-timeout", 0));
         if (this.settings.getIntProperty("difficulty", 1) < 0) {
            this.settings.setProperty("difficulty", Integer.valueOf(0));
         } else if (this.settings.getIntProperty("difficulty", 1) > 3) {
            this.settings.setProperty("difficulty", Integer.valueOf(3));
         }

         this.canSpawnStructures = this.settings.getBooleanProperty("generate-structures", true);
         int i = this.settings.getIntProperty("gamemode", GameType.SURVIVAL.getID());
         this.gameType = WorldSettings.getGameTypeById(i);
         LOGGER.info("Default game type: {}", new Object[]{this.gameType});
         InetAddress inetaddress = null;
         if (!this.getServerIp().isEmpty()) {
            inetaddress = InetAddress.getByName(this.getServerIp());
         }

         if (this.P() < 0) {
            this.setPort(this.settings.getIntProperty("server-port", 25565));
         }

         LOGGER.info("Generating keypair");
         this.a(CryptManager.generateKeyPair());
         LOGGER.info("Starting Minecraft server on {}:{}", new Object[]{this.getServerIp().isEmpty() ? "*" : this.getServerIp(), this.P()});

         try {
            this.am().addLanEndpoint(inetaddress, this.P());
         } catch (IOException var22) {
            LOGGER.warn("**** FAILED TO BIND TO PORT!");
            LOGGER.warn("The exception was: {}", new Object[]{var22.toString()});
            LOGGER.warn("Perhaps a server is already running on that port?");
            return false;
         }

         this.a(new DedicatedPlayerList(this));
         if (!this.getOnlineMode()) {
            LOGGER.warn("**** SERVER IS RUNNING IN OFFLINE/INSECURE MODE!");
            LOGGER.warn("The server will make no attempt to authenticate usernames. Beware.");
            LOGGER.warn("While this makes the game possible to play without internet access, it also opens up the ability for hackers to connect with any username they choose.");
            LOGGER.warn("To change this, set \"online-mode\" to \"true\" in the server.properties file.");
         }

         if (this.convertFiles()) {
            this.getUserCache().save();
         }

         if (!PreYggdrasilConverter.tryConvert(this.settings)) {
            return false;
         } else {
            this.convertable = new AnvilSaveConverter(this.server.getWorldContainer(), this.getDataConverterManager());
            long j = System.nanoTime();
            if (this.getFolderName() == null) {
               this.setWorld(this.settings.getStringProperty("level-name", "world"));
            }

            String s = this.settings.getStringProperty("level-seed", "");
            String s1 = this.settings.getStringProperty("level-type", "DEFAULT");
            String s2 = this.settings.getStringProperty("generator-settings", "");
            long k = (new Random()).nextLong();
            if (!s.isEmpty()) {
               try {
                  long l = Long.parseLong(s);
                  if (l != 0L) {
                     k = l;
                  }
               } catch (NumberFormatException var21) {
                  k = (long)s.hashCode();
               }
            }

            WorldType worldtype = WorldType.parseWorldType(s1);
            if (worldtype == null) {
               worldtype = WorldType.DEFAULT;
            }

            this.isAnnouncingPlayerAchievements();
            this.getEnableCommandBlock();
            this.getOpPermissionLevel();
            this.isSnooperEnabled();
            this.getNetworkCompressionThreshold();
            this.c(this.settings.getIntProperty("max-build-height", 256));
            this.c((this.getMaxBuildHeight() + 8) / 16 * 16);
            this.c(MathHelper.clamp(this.getMaxBuildHeight(), 64, 256));
            this.settings.setProperty("max-build-height", Integer.valueOf(this.getMaxBuildHeight()));
            TileEntitySkull.setProfileCache(this.getUserCache());
            TileEntitySkull.setSessionService(this.ay());
            PlayerProfileCache.setOnlineMode(this.getOnlineMode());
            LOGGER.info("Preparing level \"{}\"", new Object[]{this.getFolderName()});
            this.a(this.getFolderName(), this.getFolderName(), k, worldtype, s2);
            long i1 = System.nanoTime() - j;
            String s3 = String.format("%.3fs", (double)i1 / 1.0E9D);
            LOGGER.info("Done ({})! For help, type \"help\" or \"?\"", new Object[]{s3});
            if (this.settings.getBooleanProperty("enable-query", false)) {
               LOGGER.info("Starting GS4 status listener");
               this.theRConThreadQuery = new RConThreadQuery(this);
               this.theRConThreadQuery.startThread();
            }

            if (this.settings.getBooleanProperty("enable-rcon", false)) {
               LOGGER.info("Starting remote control listener");
               this.theRConThreadMain = new RConThreadMain(this);
               this.theRConThreadMain.startThread();
               this.remoteConsole = new CraftRemoteConsoleCommandSender(this.rconConsoleSource);
            }

            if (this.server.getBukkitSpawnRadius() > -1) {
               LOGGER.info("'settings.spawn-radius' in bukkit.yml has been moved to 'spawn-protection' in server.properties. I will move your config for you.");
               this.settings.serverProperties.remove("spawn-protection");
               this.settings.getIntProperty("spawn-protection", this.server.getBukkitSpawnRadius());
               this.server.removeBukkitSpawnRadius();
               this.settings.saveProperties();
            }

            if (this.getMaxTickTime() > 0L) {
               Thread thread1 = new Thread(new ServerHangWatchdog(this));
               thread1.setName("Server Watchdog");
               thread1.setDaemon(true);
               thread1.start();
            }

            return true;
         }
      }
   }

   public String loadResourcePackSHA() {
      if (this.settings.hasProperty("resource-pack-hash")) {
         if (this.settings.hasProperty("resource-pack-sha1")) {
            LOGGER.warn("resource-pack-hash is deprecated and found along side resource-pack-sha1. resource-pack-hash will be ignored.");
         } else {
            LOGGER.warn("resource-pack-hash is deprecated. Please use resource-pack-sha1 instead.");
            this.settings.getStringProperty("resource-pack-sha1", this.settings.getStringProperty("resource-pack-hash", ""));
            this.settings.removeProperty("resource-pack-hash");
         }
      }

      String s = this.settings.getStringProperty("resource-pack-sha1", "");
      if (!s.isEmpty() && !RESOURCE_PACK_SHA1_PATTERN.matcher(s).matches()) {
         LOGGER.warn("Invalid sha1 for ressource-pack-sha1");
      }

      if (!this.settings.getStringProperty("resource-pack", "").isEmpty() && s.isEmpty()) {
         LOGGER.warn("You specified a resource pack without providing a sha1 hash. Pack will be updated on the client only if you change the name of the pack.");
      }

      return s;
   }

   public void setGamemode(GameType enumgamemode) {
      super.setGamemode(enumgamemode);
      this.gameType = enumgamemode;
   }

   public boolean getGenerateStructures() {
      return this.canSpawnStructures;
   }

   public GameType getGamemode() {
      return this.gameType;
   }

   public EnumDifficulty getDifficulty() {
      return EnumDifficulty.getDifficultyEnum(this.settings.getIntProperty("difficulty", EnumDifficulty.NORMAL.getDifficultyId()));
   }

   public boolean isHardcore() {
      return this.settings.getBooleanProperty("hardcore", false);
   }

   public void finalTick(CrashReport crashreport) {
   }

   public CrashReport addServerInfoToCrashReport(CrashReport crashreport) {
      crashreport = super.b(crashreport);
      crashreport.getCategory().setDetail("Is Modded", new ICrashReportDetail() {
         public String call() throws Exception {
            String s = DedicatedServer.this.getServerModName();
            return !"vanilla".equals(s) ? "Definitely; Server brand changed to '" + s + "'" : "Unknown (can't tell)";
         }

         public Object call() throws Exception {
            return this.call();
         }
      });
      crashreport.getCategory().setDetail("Type", new ICrashReportDetail() {
         public String call() throws Exception {
            return "Dedicated Server (map_server.txt)";
         }

         public Object call() throws Exception {
            return this.call();
         }
      });
      return crashreport;
   }

   public void systemExitNow() {
      System.exit(0);
   }

   public void updateTimeLightAndEntities() {
      super.D();
      this.executePendingCommands();
   }

   public boolean getAllowNether() {
      return this.settings.getBooleanProperty("allow-nether", true);
   }

   public boolean getSpawnMonsters() {
      return this.settings.getBooleanProperty("spawn-monsters", true);
   }

   public void addServerStatsToSnooper(Snooper mojangstatisticsgenerator) {
      mojangstatisticsgenerator.addClientStat("whitelist_enabled", Boolean.valueOf(this.getPlayerList().isWhiteListEnabled()));
      mojangstatisticsgenerator.addClientStat("whitelist_count", Integer.valueOf(this.getPlayerList().getWhitelistedPlayerNames().length));
      super.addServerStatsToSnooper(mojangstatisticsgenerator);
   }

   public boolean isSnooperEnabled() {
      return this.settings.getBooleanProperty("snooper-enabled", true);
   }

   public void addPendingCommand(String s, ICommandSender icommandlistener) {
      this.pendingCommandList.add(new PendingCommand(s, icommandlistener));
   }

   public void executePendingCommands() {
      while(!this.pendingCommandList.isEmpty()) {
         PendingCommand servercommand = (PendingCommand)this.pendingCommandList.remove(0);
         ServerCommandEvent event = new ServerCommandEvent(this.console, servercommand.command);
         this.server.getPluginManager().callEvent(event);
         if (!event.isCancelled()) {
            servercommand = new PendingCommand(event.getCommand(), servercommand.sender);
            this.server.dispatchServerCommand(this.console, servercommand);
         }
      }

   }

   public boolean isDedicatedServer() {
      return true;
   }

   public boolean shouldUseNativeTransport() {
      return this.settings.getBooleanProperty("use-native-transport", true);
   }

   public DedicatedPlayerList getPlayerList() {
      return (DedicatedPlayerList)super.getPlayerList();
   }

   public int getIntProperty(String s, int i) {
      return this.settings.getIntProperty(s, i);
   }

   public String getStringProperty(String s, String s1) {
      return this.settings.getStringProperty(s, s1);
   }

   public boolean getBooleanProperty(String s, boolean flag) {
      return this.settings.getBooleanProperty(s, flag);
   }

   public void setProperty(String s, Object object) {
      this.settings.setProperty(s, object);
   }

   public void saveProperties() {
      this.settings.saveProperties();
   }

   public String getSettingsFilename() {
      File file = this.settings.getPropertiesFile();
      return file != null ? file.getAbsolutePath() : "No settings file";
   }

   public String getHostname() {
      return this.getServerIp();
   }

   public int getPort() {
      return this.P();
   }

   public String getMotd() {
      return this.getMotd();
   }

   public void setGuiEnabled() {
      MinecraftServerGui.createServerGui(this);
      this.guiIsEnabled = true;
   }

   public boolean getGuiEnabled() {
      return this.guiIsEnabled;
   }

   public String shareToLAN(GameType enumgamemode, boolean flag) {
      return "";
   }

   public boolean getEnableCommandBlock() {
      return this.settings.getBooleanProperty("enable-command-block", false);
   }

   public int getSpawnProtection() {
      return this.settings.getIntProperty("spawn-protection", super.getSpawnProtection());
   }

   public boolean isBlockProtected(World world, BlockPos blockposition, EntityPlayer entityhuman) {
      if (world.provider.getDimensionType().getId() != 0) {
         return false;
      } else if (this.getPlayerList().getOppedPlayers().isEmpty()) {
         return false;
      } else if (this.getPlayerList().canSendCommands(entityhuman.getGameProfile())) {
         return false;
      } else if (this.getSpawnProtection() <= 0) {
         return false;
      } else {
         BlockPos blockposition1 = world.getSpawnPoint();
         int i = MathHelper.abs(blockposition.getX() - blockposition1.getX());
         int j = MathHelper.abs(blockposition.getZ() - blockposition1.getZ());
         int k = Math.max(i, j);
         return k <= this.getSpawnProtection();
      }
   }

   public int getOpPermissionLevel() {
      return this.settings.getIntProperty("op-permission-level", 4);
   }

   public void setIdleTimeout(int i) {
      super.setIdleTimeout(i);
      this.settings.setProperty("player-idle-timeout", Integer.valueOf(i));
      this.saveProperties();
   }

   public boolean shouldBroadcastRconToOps() {
      return this.settings.getBooleanProperty("broadcast-rcon-to-ops", true);
   }

   public boolean shouldBroadcastConsoleToOps() {
      return this.settings.getBooleanProperty("broadcast-console-to-ops", true);
   }

   public boolean isAnnouncingPlayerAchievements() {
      return this.settings.getBooleanProperty("announce-player-achievements", true);
   }

   public int getMaxWorldSize() {
      int i = this.settings.getIntProperty("max-world-size", super.aD());
      if (i < 1) {
         i = 1;
      } else if (i > super.aD()) {
         i = super.aD();
      }

      return i;
   }

   public int getNetworkCompressionThreshold() {
      return this.settings.getIntProperty("network-compression-threshold", super.aF());
   }

   protected boolean convertFiles() {
      boolean flag = false;

      for(int i = 0; !flag && i <= 2; ++i) {
         if (i > 0) {
            LOGGER.warn("Encountered a problem while converting the user banlist, retrying in a few seconds");
            this.sleepFiveSeconds();
         }

         flag = PreYggdrasilConverter.a(this);
      }

      boolean flag1 = false;

      for(int var7 = 0; !flag1 && var7 <= 2; ++var7) {
         if (var7 > 0) {
            LOGGER.warn("Encountered a problem while converting the ip banlist, retrying in a few seconds");
            this.sleepFiveSeconds();
         }

         flag1 = PreYggdrasilConverter.b(this);
      }

      boolean flag2 = false;

      for(int var8 = 0; !flag2 && var8 <= 2; ++var8) {
         if (var8 > 0) {
            LOGGER.warn("Encountered a problem while converting the op list, retrying in a few seconds");
            this.sleepFiveSeconds();
         }

         flag2 = PreYggdrasilConverter.c(this);
      }

      boolean flag3 = false;

      for(int var9 = 0; !flag3 && var9 <= 2; ++var9) {
         if (var9 > 0) {
            LOGGER.warn("Encountered a problem while converting the whitelist, retrying in a few seconds");
            this.sleepFiveSeconds();
         }

         flag3 = PreYggdrasilConverter.d(this);
      }

      boolean flag4 = false;

      for(int var10 = 0; !flag4 && var10 <= 2; ++var10) {
         if (var10 > 0) {
            LOGGER.warn("Encountered a problem while converting the player save files, retrying in a few seconds");
            this.sleepFiveSeconds();
         }

         flag4 = PreYggdrasilConverter.convertSaveFiles(this, this.settings);
      }

      return flag || flag1 || flag2 || flag3 || flag4;
   }

   private void sleepFiveSeconds() {
      try {
         Thread.sleep(5000L);
      } catch (InterruptedException var1) {
         ;
      }

   }

   public long getMaxTickTime() {
      return this.settings.getLongProperty("max-tick-time", TimeUnit.MINUTES.toMillis(1L));
   }

   public String getPlugins() {
      StringBuilder result = new StringBuilder();
      Plugin[] plugins = this.server.getPluginManager().getPlugins();
      result.append(this.server.getName());
      result.append(" on Bukkit ");
      result.append(this.server.getBukkitVersion());
      if (plugins.length > 0 && this.server.getQueryPlugins()) {
         result.append(": ");

         for(int i = 0; i < plugins.length; ++i) {
            if (i > 0) {
               result.append("; ");
            }

            result.append(plugins[i].getDescription().getName());
            result.append(" ");
            result.append(plugins[i].getDescription().getVersion().replaceAll(";", ","));
         }
      }

      return result.toString();
   }

   public String handleRConCommand(final String s) {
      Waitable waitable = new Waitable() {
         protected String evaluate() {
            DedicatedServer.this.rconConsoleSource.resetLog();
            RemoteServerCommandEvent event = new RemoteServerCommandEvent(DedicatedServer.this.remoteConsole, s);
            DedicatedServer.this.server.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
               return "";
            } else {
               PendingCommand serverCommand = new PendingCommand(event.getCommand(), DedicatedServer.this.rconConsoleSource);
               DedicatedServer.this.server.dispatchServerCommand(DedicatedServer.this.remoteConsole, serverCommand);
               return DedicatedServer.this.rconConsoleSource.getLogContents();
            }
         }
      };
      this.processQueue.add(waitable);

      try {
         return (String)waitable.get();
      } catch (ExecutionException var4) {
         throw new RuntimeException("Exception processing rcon command " + s, var4.getCause());
      } catch (InterruptedException var5) {
         Thread.currentThread().interrupt();
         throw new RuntimeException("Interrupted processing rcon command " + s, var5);
      }
   }

   public PlayerList getPlayerList() {
      return this.getPlayerList();
   }

   public PropertyManager getPropertyManager() {
      return this.settings;
   }
}
