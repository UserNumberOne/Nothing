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

   public DedicatedServer(OptionSet var1, DataFixer var2, YggdrasilAuthenticationService var3, MinecraftSessionService var4, GameProfileRepository var5, PlayerProfileCache var6) {
      super(var1, Proxy.NO_PROXY, var2, var3, var4, var5, var6);
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
      Thread var1 = new Thread("Server console handler") {
         public void run() {
            if (Main.useConsole) {
               ConsoleReader var1 = DedicatedServer.this.reader;

               try {
                  while(!DedicatedServer.this.isStopped() && DedicatedServer.this.isRunning()) {
                     String var2;
                     if (Main.useJline) {
                        var2 = var1.readLine(">", (Character)null);
                     } else {
                        var2 = var1.readLine();
                     }

                     if (var2 != null && var2.trim().length() > 0) {
                        DedicatedServer.this.addPendingCommand(var2, DedicatedServer.this);
                     }
                  }
               } catch (IOException var4) {
                  DedicatedServer.LOGGER.error("Exception handling console input", var4);
               }

            }
         }
      };
      java.util.logging.Logger var2 = java.util.logging.Logger.getLogger("");
      var2.setUseParentHandlers(false);

      Handler[] var3;
      for(Handler var6 : var3 = var2.getHandlers()) {
         var2.removeHandler(var6);
      }

      var2.addHandler(new ForwardLogHandler());
      org.apache.logging.log4j.core.Logger var27 = (org.apache.logging.log4j.core.Logger)LogManager.getRootLogger();

      for(Appender var25 : var27.getAppenders().values()) {
         if (var25 instanceof ConsoleAppender) {
            var27.removeAppender(var25);
         }
      }

      (new Thread(new TerminalConsoleWriterThread(System.out, this.reader))).start();
      System.setOut(new PrintStream(new LoggerOutputStream(var27, Level.INFO), true));
      System.setErr(new PrintStream(new LoggerOutputStream(var27, Level.WARN), true));
      var1.setDaemon(true);
      var1.start();
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
         int var26 = this.settings.getIntProperty("gamemode", GameType.SURVIVAL.getID());
         this.gameType = WorldSettings.getGameTypeById(var26);
         LOGGER.info("Default game type: {}", new Object[]{this.gameType});
         InetAddress var24 = null;
         if (!this.getServerIp().isEmpty()) {
            var24 = InetAddress.getByName(this.getServerIp());
         }

         if (this.P() < 0) {
            this.setPort(this.settings.getIntProperty("server-port", 25565));
         }

         LOGGER.info("Generating keypair");
         this.a(CryptManager.generateKeyPair());
         LOGGER.info("Starting Minecraft server on {}:{}", new Object[]{this.getServerIp().isEmpty() ? "*" : this.getServerIp(), this.P()});

         try {
            this.am().addLanEndpoint(var24, this.P());
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
            long var7 = System.nanoTime();
            if (this.getFolderName() == null) {
               this.setWorld(this.settings.getStringProperty("level-name", "world"));
            }

            String var9 = this.settings.getStringProperty("level-seed", "");
            String var10 = this.settings.getStringProperty("level-type", "DEFAULT");
            String var11 = this.settings.getStringProperty("generator-settings", "");
            long var12 = (new Random()).nextLong();
            if (!var9.isEmpty()) {
               try {
                  long var14 = Long.parseLong(var9);
                  if (var14 != 0L) {
                     var12 = var14;
                  }
               } catch (NumberFormatException var21) {
                  var12 = (long)var9.hashCode();
               }
            }

            WorldType var16 = WorldType.parseWorldType(var10);
            if (var16 == null) {
               var16 = WorldType.DEFAULT;
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
            this.a(this.getFolderName(), this.getFolderName(), var12, var16, var11);
            long var17 = System.nanoTime() - var7;
            String var19 = String.format("%.3fs", (double)var17 / 1.0E9D);
            LOGGER.info("Done ({})! For help, type \"help\" or \"?\"", new Object[]{var19});
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
               Thread var20 = new Thread(new ServerHangWatchdog(this));
               var20.setName("Server Watchdog");
               var20.setDaemon(true);
               var20.start();
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

      String var1 = this.settings.getStringProperty("resource-pack-sha1", "");
      if (!var1.isEmpty() && !RESOURCE_PACK_SHA1_PATTERN.matcher(var1).matches()) {
         LOGGER.warn("Invalid sha1 for ressource-pack-sha1");
      }

      if (!this.settings.getStringProperty("resource-pack", "").isEmpty() && var1.isEmpty()) {
         LOGGER.warn("You specified a resource pack without providing a sha1 hash. Pack will be updated on the client only if you change the name of the pack.");
      }

      return var1;
   }

   public void setGamemode(GameType var1) {
      super.setGamemode(var1);
      this.gameType = var1;
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

   public void finalTick(CrashReport var1) {
   }

   public CrashReport addServerInfoToCrashReport(CrashReport var1) {
      var1 = super.b(var1);
      var1.getCategory().setDetail("Is Modded", new ICrashReportDetail() {
         public String call() throws Exception {
            String var1 = DedicatedServer.this.getServerModName();
            return !"vanilla".equals(var1) ? "Definitely; Server brand changed to '" + var1 + "'" : "Unknown (can't tell)";
         }

         public Object call() throws Exception {
            return this.call();
         }
      });
      var1.getCategory().setDetail("Type", new ICrashReportDetail() {
         public String call() throws Exception {
            return "Dedicated Server (map_server.txt)";
         }

         public Object call() throws Exception {
            return this.call();
         }
      });
      return var1;
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

   public void addServerStatsToSnooper(Snooper var1) {
      var1.addClientStat("whitelist_enabled", Boolean.valueOf(this.getPlayerList().isWhiteListEnabled()));
      var1.addClientStat("whitelist_count", Integer.valueOf(this.getPlayerList().getWhitelistedPlayerNames().length));
      super.addServerStatsToSnooper(var1);
   }

   public boolean isSnooperEnabled() {
      return this.settings.getBooleanProperty("snooper-enabled", true);
   }

   public void addPendingCommand(String var1, ICommandSender var2) {
      this.pendingCommandList.add(new PendingCommand(var1, var2));
   }

   public void executePendingCommands() {
      while(!this.pendingCommandList.isEmpty()) {
         PendingCommand var1 = (PendingCommand)this.pendingCommandList.remove(0);
         ServerCommandEvent var2 = new ServerCommandEvent(this.console, var1.command);
         this.server.getPluginManager().callEvent(var2);
         if (!var2.isCancelled()) {
            var1 = new PendingCommand(var2.getCommand(), var1.sender);
            this.server.dispatchServerCommand(this.console, var1);
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

   public int getIntProperty(String var1, int var2) {
      return this.settings.getIntProperty(var1, var2);
   }

   public String getStringProperty(String var1, String var2) {
      return this.settings.getStringProperty(var1, var2);
   }

   public boolean getBooleanProperty(String var1, boolean var2) {
      return this.settings.getBooleanProperty(var1, var2);
   }

   public void setProperty(String var1, Object var2) {
      this.settings.setProperty(var1, var2);
   }

   public void saveProperties() {
      this.settings.saveProperties();
   }

   public String getSettingsFilename() {
      File var1 = this.settings.getPropertiesFile();
      return var1 != null ? var1.getAbsolutePath() : "No settings file";
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

   public String shareToLAN(GameType var1, boolean var2) {
      return "";
   }

   public boolean getEnableCommandBlock() {
      return this.settings.getBooleanProperty("enable-command-block", false);
   }

   public int getSpawnProtection() {
      return this.settings.getIntProperty("spawn-protection", super.getSpawnProtection());
   }

   public boolean isBlockProtected(World var1, BlockPos var2, EntityPlayer var3) {
      if (var1.provider.getDimensionType().getId() != 0) {
         return false;
      } else if (this.getPlayerList().getOppedPlayers().isEmpty()) {
         return false;
      } else if (this.getPlayerList().canSendCommands(var3.getGameProfile())) {
         return false;
      } else if (this.getSpawnProtection() <= 0) {
         return false;
      } else {
         BlockPos var4 = var1.getSpawnPoint();
         int var5 = MathHelper.abs(var2.getX() - var4.getX());
         int var6 = MathHelper.abs(var2.getZ() - var4.getZ());
         int var7 = Math.max(var5, var6);
         return var7 <= this.getSpawnProtection();
      }
   }

   public int getOpPermissionLevel() {
      return this.settings.getIntProperty("op-permission-level", 4);
   }

   public void setIdleTimeout(int var1) {
      super.setIdleTimeout(var1);
      this.settings.setProperty("player-idle-timeout", Integer.valueOf(var1));
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
      int var1 = this.settings.getIntProperty("max-world-size", super.aD());
      if (var1 < 1) {
         var1 = 1;
      } else if (var1 > super.aD()) {
         var1 = super.aD();
      }

      return var1;
   }

   public int getNetworkCompressionThreshold() {
      return this.settings.getIntProperty("network-compression-threshold", super.aF());
   }

   protected boolean convertFiles() {
      boolean var1 = false;

      for(int var2 = 0; !var1 && var2 <= 2; ++var2) {
         if (var2 > 0) {
            LOGGER.warn("Encountered a problem while converting the user banlist, retrying in a few seconds");
            this.sleepFiveSeconds();
         }

         var1 = PreYggdrasilConverter.a(this);
      }

      boolean var3 = false;

      for(int var7 = 0; !var3 && var7 <= 2; ++var7) {
         if (var7 > 0) {
            LOGGER.warn("Encountered a problem while converting the ip banlist, retrying in a few seconds");
            this.sleepFiveSeconds();
         }

         var3 = PreYggdrasilConverter.b(this);
      }

      boolean var4 = false;

      for(int var8 = 0; !var4 && var8 <= 2; ++var8) {
         if (var8 > 0) {
            LOGGER.warn("Encountered a problem while converting the op list, retrying in a few seconds");
            this.sleepFiveSeconds();
         }

         var4 = PreYggdrasilConverter.c(this);
      }

      boolean var5 = false;

      for(int var9 = 0; !var5 && var9 <= 2; ++var9) {
         if (var9 > 0) {
            LOGGER.warn("Encountered a problem while converting the whitelist, retrying in a few seconds");
            this.sleepFiveSeconds();
         }

         var5 = PreYggdrasilConverter.d(this);
      }

      boolean var6 = false;

      for(int var10 = 0; !var6 && var10 <= 2; ++var10) {
         if (var10 > 0) {
            LOGGER.warn("Encountered a problem while converting the player save files, retrying in a few seconds");
            this.sleepFiveSeconds();
         }

         var6 = PreYggdrasilConverter.convertSaveFiles(this, this.settings);
      }

      return var1 || var3 || var4 || var5 || var6;
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
      StringBuilder var1 = new StringBuilder();
      Plugin[] var2 = this.server.getPluginManager().getPlugins();
      var1.append(this.server.getName());
      var1.append(" on Bukkit ");
      var1.append(this.server.getBukkitVersion());
      if (var2.length > 0 && this.server.getQueryPlugins()) {
         var1.append(": ");

         for(int var3 = 0; var3 < var2.length; ++var3) {
            if (var3 > 0) {
               var1.append("; ");
            }

            var1.append(var2[var3].getDescription().getName());
            var1.append(" ");
            var1.append(var2[var3].getDescription().getVersion().replaceAll(";", ","));
         }
      }

      return var1.toString();
   }

   public String handleRConCommand(final String var1) {
      Waitable var2 = new Waitable() {
         protected String evaluate() {
            DedicatedServer.this.rconConsoleSource.resetLog();
            RemoteServerCommandEvent var1x = new RemoteServerCommandEvent(DedicatedServer.this.remoteConsole, var1);
            DedicatedServer.this.server.getPluginManager().callEvent(var1x);
            if (var1x.isCancelled()) {
               return "";
            } else {
               PendingCommand var2 = new PendingCommand(var1x.getCommand(), DedicatedServer.this.rconConsoleSource);
               DedicatedServer.this.server.dispatchServerCommand(DedicatedServer.this.remoteConsole, var2);
               return DedicatedServer.this.rconConsoleSource.getLogContents();
            }
         }
      };
      this.processQueue.add(var2);

      try {
         return (String)var2.get();
      } catch (ExecutionException var4) {
         throw new RuntimeException("Exception processing rcon command " + var1, var4.getCause());
      } catch (InterruptedException var5) {
         Thread.currentThread().interrupt();
         throw new RuntimeException("Interrupted processing rcon command " + var1, var5);
      }
   }

   public PlayerList getPlayerList() {
      return this.getPlayerList();
   }

   public PropertyManager getPropertyManager() {
      return this.settings;
   }
}
