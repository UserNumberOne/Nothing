package net.minecraft.server.integrated;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.FutureTask;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ThreadLanServerPing;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.profiler.Snooper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.CryptManager;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.Util;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.demo.DemoWorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class IntegratedServer extends MinecraftServer {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Minecraft mc;
   private final WorldSettings theWorldSettings;
   private boolean isGamePaused;
   private boolean isPublic;
   private ThreadLanServerPing lanServerPing;

   public IntegratedServer(Minecraft var1, String var2, String var3, WorldSettings var4, YggdrasilAuthenticationService var5, MinecraftSessionService var6, GameProfileRepository var7, PlayerProfileCache var8) {
      super(new File(var1.mcDataDir, "saves"), var1.getProxy(), var1.getDataFixer(), var5, var6, var7, var8);
      this.setServerOwner(var1.getSession().getUsername());
      this.setFolderName(var2);
      this.setWorldName(var3);
      this.setDemo(var1.isDemo());
      this.canCreateBonusChest(var4.isBonusChestEnabled());
      this.setBuildLimit(256);
      this.setPlayerList(new IntegratedPlayerList(this));
      this.mc = var1;
      this.theWorldSettings = this.isDemo() ? DemoWorldServer.DEMO_WORLD_SETTINGS : var4;
   }

   public ServerCommandManager createCommandManager() {
      return new IntegratedServerCommandManager(this);
   }

   public void loadAllWorlds(String var1, String var2, long var3, WorldType var5, String var6) {
      this.convertMapIfNeeded(var1);
      ISaveHandler var7 = this.getActiveAnvilConverter().getSaveLoader(var1, true);
      this.setResourcePackFromWorld(this.getFolderName(), var7);
      WorldInfo var8 = var7.loadWorldInfo();
      if (var8 == null) {
         var8 = new WorldInfo(this.theWorldSettings, var2);
      } else {
         var8.setWorldName(var2);
      }

      WorldServer var9 = this.isDemo() ? (WorldServer)(new DemoWorldServer(this, var7, var8, 0, this.theProfiler)).init() : (WorldServer)(new WorldServer(this, var7, var8, 0, this.theProfiler)).init();
      var9.initialize(this.theWorldSettings);
      Integer[] var10 = DimensionManager.getStaticDimensionIDs();
      int var11 = var10.length;

      for(int var12 = 0; var12 < var11; ++var12) {
         int var13 = var10[var12].intValue();
         WorldServer var14 = var13 == 0 ? var9 : (WorldServer)(new WorldServerMulti(this, var7, var13, var9, this.theProfiler)).init();
         var14.addEventListener(new ServerWorldEventHandler(this, var14));
         if (!this.isSinglePlayer()) {
            var14.getWorldInfo().setGameType(this.getGameType());
         }

         MinecraftForge.EVENT_BUS.post(new Load(var14));
      }

      this.getPlayerList().setPlayerManager(new WorldServer[]{var9});
      if (var9.getWorldInfo().getDifficulty() == null) {
         this.setDifficultyForAllWorlds(this.mc.gameSettings.difficulty);
      }

      this.initialWorldChunkLoad();
   }

   public boolean init() throws IOException {
      LOGGER.info("Starting integrated minecraft server version 1.10.2");
      this.setOnlineMode(true);
      this.setCanSpawnAnimals(true);
      this.setCanSpawnNPCs(true);
      this.setAllowPvp(true);
      this.setAllowFlight(true);
      LOGGER.info("Generating keypair");
      this.setKeyPair(CryptManager.generateKeyPair());
      if (!FMLCommonHandler.instance().handleServerAboutToStart(this)) {
         return false;
      } else {
         this.loadAllWorlds(this.getFolderName(), this.getWorldName(), this.theWorldSettings.getSeed(), this.theWorldSettings.getTerrainType(), this.theWorldSettings.getGeneratorOptions());
         this.setMOTD(this.getServerOwner() + " - " + this.worlds[0].getWorldInfo().getWorldName());
         return FMLCommonHandler.instance().handleServerStarting(this);
      }
   }

   public void tick() {
      boolean var1 = this.isGamePaused;
      this.isGamePaused = Minecraft.getMinecraft().getConnection() != null && Minecraft.getMinecraft().isGamePaused();
      if (!var1 && this.isGamePaused) {
         LOGGER.info("Saving and pausing game...");
         this.getPlayerList().saveAllPlayerData();
         this.saveAllWorlds(false);
      }

      if (this.isGamePaused) {
         synchronized(this.futureTaskQueue) {
            while(!this.futureTaskQueue.isEmpty()) {
               Util.runTask((FutureTask)this.futureTaskQueue.poll(), LOGGER);
            }
         }
      } else {
         super.tick();
         if (this.mc.gameSettings.renderDistanceChunks != this.getPlayerList().getViewDistance()) {
            LOGGER.info("Changing view distance to {}, from {}", new Object[]{this.mc.gameSettings.renderDistanceChunks, this.getPlayerList().getViewDistance()});
            this.getPlayerList().setViewDistance(this.mc.gameSettings.renderDistanceChunks);
         }

         if (this.mc.world != null) {
            WorldInfo var9 = this.worlds[0].getWorldInfo();
            WorldInfo var3 = this.mc.world.getWorldInfo();
            if (!var9.isDifficultyLocked() && var3.getDifficulty() != var9.getDifficulty()) {
               LOGGER.info("Changing difficulty to {}, from {}", new Object[]{var3.getDifficulty(), var9.getDifficulty()});
               this.setDifficultyForAllWorlds(var3.getDifficulty());
            } else if (var3.isDifficultyLocked() && !var9.isDifficultyLocked()) {
               LOGGER.info("Locking difficulty to {}", new Object[]{var3.getDifficulty()});

               for(WorldServer var7 : this.worlds) {
                  if (var7 != null) {
                     var7.getWorldInfo().setDifficultyLocked(true);
                  }
               }
            }
         }
      }

   }

   public boolean canStructuresSpawn() {
      return false;
   }

   public GameType getGameType() {
      return this.theWorldSettings.getGameType();
   }

   public EnumDifficulty getDifficulty() {
      return this.mc.world == null ? this.mc.gameSettings.difficulty : this.mc.world.getWorldInfo().getDifficulty();
   }

   public boolean isHardcore() {
      return this.theWorldSettings.getHardcoreEnabled();
   }

   public boolean shouldBroadcastRconToOps() {
      return true;
   }

   public boolean shouldBroadcastConsoleToOps() {
      return true;
   }

   public void saveAllWorlds(boolean var1) {
      super.saveAllWorlds(var1);
   }

   public File getDataDirectory() {
      return this.mc.mcDataDir;
   }

   public boolean isDedicatedServer() {
      return false;
   }

   public boolean shouldUseNativeTransport() {
      return false;
   }

   public void finalTick(CrashReport var1) {
      this.mc.crashed(var1);
   }

   public CrashReport addServerInfoToCrashReport(CrashReport var1) {
      var1 = super.addServerInfoToCrashReport(var1);
      var1.getCategory().setDetail("Type", new ICrashReportDetail() {
         public String call() throws Exception {
            return "Integrated Server (map_client.txt)";
         }
      });
      var1.getCategory().setDetail("Is Modded", new ICrashReportDetail() {
         public String call() throws Exception {
            String var1 = ClientBrandRetriever.getClientModName();
            if (!var1.equals("vanilla")) {
               return "Definitely; Client brand changed to '" + var1 + "'";
            } else {
               var1 = IntegratedServer.this.getServerModName();
               return !"vanilla".equals(var1) ? "Definitely; Server brand changed to '" + var1 + "'" : (Minecraft.class.getSigners() == null ? "Very likely; Jar signature invalidated" : "Probably not. Jar signature remains and both client + server brands are untouched.");
            }
         }
      });
      return var1;
   }

   public void setDifficultyForAllWorlds(EnumDifficulty var1) {
      super.setDifficultyForAllWorlds(var1);
      if (this.mc.world != null) {
         this.mc.world.getWorldInfo().setDifficulty(var1);
      }

   }

   public void addServerStatsToSnooper(Snooper var1) {
      super.addServerStatsToSnooper(var1);
      var1.addClientStat("snooper_partner", this.mc.getPlayerUsageSnooper().getUniqueID());
   }

   public boolean isSnooperEnabled() {
      return Minecraft.getMinecraft().isSnooperEnabled();
   }

   public String shareToLAN(GameType var1, boolean var2) {
      try {
         int var3 = -1;

         try {
            var3 = HttpUtil.getSuitableLanPort();
         } catch (IOException var5) {
            ;
         }

         if (var3 <= 0) {
            var3 = 25564;
         }

         this.getNetworkSystem().addLanEndpoint((InetAddress)null, var3);
         LOGGER.info("Started on {}", new Object[]{var3});
         this.isPublic = true;
         this.lanServerPing = new ThreadLanServerPing(this.getMOTD(), var3 + "");
         this.lanServerPing.start();
         this.getPlayerList().setGameType(var1);
         this.getPlayerList().setCommandsAllowedForAll(var2);
         this.mc.player.setPermissionLevel(var2 ? 4 : 0);
         return var3 + "";
      } catch (IOException var6) {
         return null;
      }
   }

   public void stopServer() {
      super.stopServer();
      if (this.lanServerPing != null) {
         this.lanServerPing.interrupt();
         this.lanServerPing = null;
      }

   }

   public void initiateShutdown() {
      if (this.isServerRunning()) {
         Futures.getUnchecked(this.addScheduledTask(new Runnable() {
            public void run() {
               for(EntityPlayerMP var2 : Lists.newArrayList(IntegratedServer.this.getPlayerList().getPlayers())) {
                  IntegratedServer.this.getPlayerList().playerLoggedOut(var2);
               }

            }
         }));
      }

      super.initiateShutdown();
      if (this.lanServerPing != null) {
         this.lanServerPing.interrupt();
         this.lanServerPing = null;
      }

   }

   public boolean getPublic() {
      return this.isPublic;
   }

   public void setGameType(GameType var1) {
      super.setGameType(var1);
      this.getPlayerList().setGameType(var1);
   }

   public boolean isCommandBlockEnabled() {
      return true;
   }

   public int getOpPermissionLevel() {
      return 4;
   }

   public void reloadLootTables() {
      if (this.isCallingFromMinecraftThread()) {
         this.worlds[0].getLootTableManager().reloadLootTables();
      } else {
         this.addScheduledTask(new Runnable() {
            public void run() {
               IntegratedServer.this.reloadLootTables();
            }
         });
      }

   }
}
