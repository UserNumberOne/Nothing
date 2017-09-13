package net.minecraft.server.dedicated;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerEula;
import net.minecraft.server.gui.MinecraftServerGui;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.CryptManager;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.server.console.TerminalHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.SERVER)
public class DedicatedServer extends MinecraftServer implements IServer {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Pattern RESOURCE_PACK_SHA1_PATTERN = Pattern.compile("^[a-fA-F0-9]{40}$");
   public final List pendingCommandList = Collections.synchronizedList(Lists.newArrayList());
   private RConThreadQuery theRConThreadQuery;
   private final RConConsoleSource rconConsoleSource = new RConConsoleSource(this);
   private RConThreadMain theRConThreadMain;
   private PropertyManager settings;
   private ServerEula eula;
   private boolean canSpawnStructures;
   private GameType gameType;
   private boolean guiIsEnabled;
   public static boolean allowPlayerLogins = false;

   public DedicatedServer(File var1, DataFixer var2, YggdrasilAuthenticationService var3, MinecraftSessionService var4, GameProfileRepository var5, PlayerProfileCache var6) {
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
               } catch (InterruptedException var2) {
                  ;
               }
            }
         }
      };
   }

   public boolean init() throws IOException {
      Thread var1 = new Thread("Server console handler") {
         public void run() {
            if (!TerminalHandler.handleCommands(DedicatedServer.this)) {
               BufferedReader var1 = new BufferedReader(new InputStreamReader(System.in));

               String var2;
               try {
                  while(!DedicatedServer.this.isServerStopped() && DedicatedServer.this.isServerRunning() && (var2 = var1.readLine()) != null) {
                     DedicatedServer.this.addPendingCommand(var2, DedicatedServer.this);
                  }
               } catch (IOException var4) {
                  DedicatedServer.LOGGER.error("Exception handling console input", var4);
               }

            }
         }
      };
      var1.setDaemon(true);
      var1.start();
      LOGGER.info("Starting minecraft server version 1.10.2");
      if (Runtime.getRuntime().maxMemory() / 1024L / 1024L < 512L) {
         LOGGER.warn("To start the server with more ram, launch it as \"java -Xmx1024M -Xms1024M -jar minecraft_server.jar\"");
      }

      FMLCommonHandler.instance().onServerStart(this);
      LOGGER.info("Loading properties");
      this.settings = new PropertyManager(new File("server.properties"));
      this.eula = new ServerEula(new File("eula.txt"));
      if (!this.eula.hasAcceptedEULA()) {
         LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
         this.eula.createEULAFile();
         return false;
      } else {
         if (this.isSinglePlayer()) {
            this.setHostname("127.0.0.1");
         } else {
            this.setOnlineMode(this.settings.getBooleanProperty("online-mode", true));
            this.setHostname(this.settings.getStringProperty("server-ip", ""));
         }

         this.setCanSpawnAnimals(this.settings.getBooleanProperty("spawn-animals", true));
         this.setCanSpawnNPCs(this.settings.getBooleanProperty("spawn-npcs", true));
         this.setAllowPvp(this.settings.getBooleanProperty("pvp", true));
         this.setAllowFlight(this.settings.getBooleanProperty("allow-flight", false));
         this.setResourcePack(this.settings.getStringProperty("resource-pack", ""), this.loadResourcePackSHA());
         this.setMOTD(this.settings.getStringProperty("motd", "A Minecraft Server"));
         this.setForceGamemode(this.settings.getBooleanProperty("force-gamemode", false));
         this.setPlayerIdleTimeout(this.settings.getIntProperty("player-idle-timeout", 0));
         if (this.settings.getIntProperty("difficulty", 1) < 0) {
            this.settings.setProperty("difficulty", Integer.valueOf(0));
         } else if (this.settings.getIntProperty("difficulty", 1) > 3) {
            this.settings.setProperty("difficulty", Integer.valueOf(3));
         }

         this.canSpawnStructures = this.settings.getBooleanProperty("generate-structures", true);
         int var2 = this.settings.getIntProperty("gamemode", GameType.SURVIVAL.getID());
         this.gameType = WorldSettings.getGameTypeById(var2);
         LOGGER.info("Default game type: {}", new Object[]{this.gameType});
         InetAddress var3 = null;
         if (!this.getServerHostname().isEmpty()) {
            var3 = InetAddress.getByName(this.getServerHostname());
         }

         if (this.getServerPort() < 0) {
            this.setServerPort(this.settings.getIntProperty("server-port", 25565));
         }

         LOGGER.info("Generating keypair");
         this.setKeyPair(CryptManager.generateKeyPair());
         LOGGER.info("Starting Minecraft server on {}:{}", new Object[]{this.getServerHostname().isEmpty() ? "*" : this.getServerHostname(), this.getServerPort()});

         try {
            this.getNetworkSystem().addLanEndpoint(var3, this.getServerPort());
         } catch (IOException var17) {
            LOGGER.warn("**** FAILED TO BIND TO PORT!");
            LOGGER.warn("The exception was: {}", new Object[]{var17.toString()});
            LOGGER.warn("Perhaps a server is already running on that port?");
            return false;
         }

         if (!this.isServerInOnlineMode()) {
            LOGGER.warn("**** SERVER IS RUNNING IN OFFLINE/INSECURE MODE!");
            LOGGER.warn("The server will make no attempt to authenticate usernames. Beware.");
            LOGGER.warn("While this makes the game possible to play without internet access, it also opens up the ability for hackers to connect with any username they choose.");
            LOGGER.warn("To change this, set \"online-mode\" to \"true\" in the server.properties file.");
         }

         if (this.convertFiles()) {
            this.getPlayerProfileCache().save();
         }

         if (!PreYggdrasilConverter.tryConvert(this.settings)) {
            return false;
         } else {
            FMLCommonHandler.instance().onServerStarted();
            this.setPlayerList(new DedicatedPlayerList(this));
            long var4 = System.nanoTime();
            if (this.getFolderName() == null) {
               this.setFolderName(this.settings.getStringProperty("level-name", "world"));
            }

            String var6 = this.settings.getStringProperty("level-seed", "");
            String var7 = this.settings.getStringProperty("level-type", "DEFAULT");
            String var8 = this.settings.getStringProperty("generator-settings", "");
            long var9 = (new Random()).nextLong();
            if (!var6.isEmpty()) {
               try {
                  long var11 = Long.parseLong(var6);
                  if (var11 != 0L) {
                     var9 = var11;
                  }
               } catch (NumberFormatException var16) {
                  var9 = (long)var6.hashCode();
               }
            }

            WorldType var18 = WorldType.parseWorldType(var7);
            if (var18 == null) {
               var18 = WorldType.DEFAULT;
            }

            this.isAnnouncingPlayerAchievements();
            this.isCommandBlockEnabled();
            this.getOpPermissionLevel();
            this.isSnooperEnabled();
            this.getNetworkCompressionThreshold();
            this.setBuildLimit(this.settings.getIntProperty("max-build-height", 256));
            this.setBuildLimit((this.getBuildLimit() + 8) / 16 * 16);
            this.setBuildLimit(MathHelper.clamp(this.getBuildLimit(), 64, 256));
            this.settings.setProperty("max-build-height", Integer.valueOf(this.getBuildLimit()));
            TileEntitySkull.setProfileCache(this.getPlayerProfileCache());
            TileEntitySkull.setSessionService(this.getMinecraftSessionService());
            PlayerProfileCache.setOnlineMode(this.isServerInOnlineMode());
            if (!FMLCommonHandler.instance().handleServerAboutToStart(this)) {
               return false;
            } else {
               LOGGER.info("Preparing level \"{}\"", new Object[]{this.getFolderName()});
               this.loadAllWorlds(this.getFolderName(), this.getFolderName(), var9, var18, var8);
               long var12 = System.nanoTime() - var4;
               String var14 = String.format("%.3fs", (double)var12 / 1.0E9D);
               LOGGER.info("Done ({})! For help, type \"help\" or \"?\"", new Object[]{var14});
               if (this.settings.getBooleanProperty("enable-query", false)) {
                  LOGGER.info("Starting GS4 status listener");
                  this.theRConThreadQuery = new RConThreadQuery(this);
                  this.theRConThreadQuery.startThread();
               }

               if (this.settings.getBooleanProperty("enable-rcon", false)) {
                  LOGGER.info("Starting remote control listener");
                  this.theRConThreadMain = new RConThreadMain(this);
                  this.theRConThreadMain.startThread();
               }

               if (this.getMaxTickTime() > 0L) {
                  Thread var15 = new Thread(new ServerHangWatchdog(this));
                  var15.setName("Server Watchdog");
                  var15.setDaemon(true);
                  var15.start();
               }

               return FMLCommonHandler.instance().handleServerStarting(this);
            }
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

   public void setGameType(GameType var1) {
      super.setGameType(var1);
      this.gameType = var1;
   }

   public boolean canStructuresSpawn() {
      return this.canSpawnStructures;
   }

   public GameType getGameType() {
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
      var1 = super.addServerInfoToCrashReport(var1);
      var1.getCategory().setDetail("Is Modded", new ICrashReportDetail() {
         public String call() throws Exception {
            String var1 = DedicatedServer.this.getServerModName();
            return !"vanilla".equals(var1) ? "Definitely; Server brand changed to '" + var1 + "'" : "Unknown (can't tell)";
         }
      });
      var1.getCategory().setDetail("Type", new ICrashReportDetail() {
         public String call() throws Exception {
            return "Dedicated Server (map_server.txt)";
         }
      });
      return var1;
   }

   public void systemExitNow() {
      System.exit(0);
   }

   public void updateTimeLightAndEntities() {
      super.updateTimeLightAndEntities();
      this.executePendingCommands();
   }

   public boolean getAllowNether() {
      return this.settings.getBooleanProperty("allow-nether", true);
   }

   public boolean allowSpawnMonsters() {
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
         this.getCommandManager().executeCommand(var1.sender, var1.command);
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
      return this.getServerHostname();
   }

   public int getPort() {
      return this.getServerPort();
   }

   public String getMotd() {
      return this.getMOTD();
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

   public boolean isCommandBlockEnabled() {
      return this.settings.getBooleanProperty("enable-command-block", false);
   }

   public int getSpawnProtectionSize() {
      return this.settings.getIntProperty("spawn-protection", super.getSpawnProtectionSize());
   }

   public boolean isBlockProtected(World var1, BlockPos var2, EntityPlayer var3) {
      if (var1.provider.getDimension() != 0) {
         return false;
      } else if (this.getPlayerList().getOppedPlayers().isEmpty()) {
         return false;
      } else if (this.getPlayerList().canSendCommands(var3.getGameProfile())) {
         return false;
      } else if (this.getSpawnProtectionSize() <= 0) {
         return false;
      } else {
         BlockPos var4 = var1.getSpawnPoint();
         int var5 = MathHelper.abs(var2.getX() - var4.getX());
         int var6 = MathHelper.abs(var2.getZ() - var4.getZ());
         int var7 = Math.max(var5, var6);
         return var7 <= this.getSpawnProtectionSize();
      }
   }

   public int getOpPermissionLevel() {
      return this.settings.getIntProperty("op-permission-level", 4);
   }

   public void setPlayerIdleTimeout(int var1) {
      super.setPlayerIdleTimeout(var1);
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
      int var1 = this.settings.getIntProperty("max-world-size", super.getMaxWorldSize());
      if (var1 < 1) {
         var1 = 1;
      } else if (var1 > super.getMaxWorldSize()) {
         var1 = super.getMaxWorldSize();
      }

      return var1;
   }

   public int getNetworkCompressionThreshold() {
      return this.settings.getIntProperty("network-compression-threshold", super.getNetworkCompressionThreshold());
   }

   public void sendMessage(ITextComponent var1) {
      LOGGER.info(var1.getFormattedText());
   }

   protected boolean convertFiles() throws IOException {
      boolean var1 = false;

      for(int var2 = 0; !var1 && var2 <= 2; ++var2) {
         if (var2 > 0) {
            LOGGER.warn("Encountered a problem while converting the user banlist, retrying in a few seconds");
            this.sleepFiveSeconds();
         }

         var1 = PreYggdrasilConverter.convertUserBanlist(this);
      }

      boolean var7 = false;

      for(int var3 = 0; !var7 && var3 <= 2; ++var3) {
         if (var3 > 0) {
            LOGGER.warn("Encountered a problem while converting the ip banlist, retrying in a few seconds");
            this.sleepFiveSeconds();
         }

         var7 = PreYggdrasilConverter.convertIpBanlist(this);
      }

      boolean var8 = false;

      for(int var4 = 0; !var8 && var4 <= 2; ++var4) {
         if (var4 > 0) {
            LOGGER.warn("Encountered a problem while converting the op list, retrying in a few seconds");
            this.sleepFiveSeconds();
         }

         var8 = PreYggdrasilConverter.convertOplist(this);
      }

      boolean var9 = false;

      for(int var5 = 0; !var9 && var5 <= 2; ++var5) {
         if (var5 > 0) {
            LOGGER.warn("Encountered a problem while converting the whitelist, retrying in a few seconds");
            this.sleepFiveSeconds();
         }

         var9 = PreYggdrasilConverter.convertWhitelist(this);
      }

      boolean var10 = false;

      for(int var6 = 0; !var10 && var6 <= 2; ++var6) {
         if (var6 > 0) {
            LOGGER.warn("Encountered a problem while converting the player save files, retrying in a few seconds");
            this.sleepFiveSeconds();
         }

         var10 = PreYggdrasilConverter.convertSaveFiles(this, this.settings);
      }

      return var1 || var7 || var8 || var9 || var10;
   }

   private void sleepFiveSeconds() {
      try {
         Thread.sleep(5000L);
      } catch (InterruptedException var2) {
         ;
      }

   }

   public long getMaxTickTime() {
      return this.settings.getLongProperty("max-tick-time", TimeUnit.MINUTES.toMillis(1L));
   }

   public String getPlugins() {
      return "";
   }

   public String handleRConCommand(String var1) {
      this.rconConsoleSource.resetLog();
      this.commandManager.executeCommand(this.rconConsoleSource, var1);
      return this.rconConsoleSource.getLogContents();
   }
}
