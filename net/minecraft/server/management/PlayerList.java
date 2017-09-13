package net.minecraft.server.management;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketJoinGame;
import net.minecraft.network.play.server.SPacketPlayerAbilities;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketServerDifficulty;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.network.play.server.SPacketTeams;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.network.play.server.SPacketWorldBorder;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.NetHandlerLoginServer;
import net.minecraft.src.MinecraftServer;
import net.minecraft.stats.StatList;
import net.minecraft.stats.StatisticsManagerServer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.craftbukkit.v1_10_R1.CraftTravelAgent;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.chunkio.ChunkIOExecutor;
import org.bukkit.craftbukkit.v1_10_R1.command.ColouredConsoleSender;
import org.bukkit.craftbukkit.v1_10_R1.command.ConsoleCommandCompleter;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_10_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

public abstract class PlayerList {
   public static final File FILE_PLAYERBANS = new File("banned-players.json");
   public static final File FILE_IPBANS = new File("banned-ips.json");
   public static final File FILE_OPS = new File("ops.json");
   public static final File FILE_WHITELIST = new File("whitelist.json");
   private static final Logger LOG = LogManager.getLogger();
   private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
   private final MinecraftServer mcServer;
   public final List playerEntityList = new CopyOnWriteArrayList();
   private final Map uuidToPlayerMap = Maps.newHashMap();
   private final UserListBans bannedPlayers;
   private final UserListIPBans bannedIPs;
   private final UserListOps ops;
   private final UserListWhitelist whiteListedPlayers;
   private final Map playerStatFiles;
   public IPlayerFileData playerNBTManagerObj;
   private boolean whiteListEnforced;
   protected int maxPlayers;
   private int viewDistance;
   private GameType gameType;
   private boolean commandsAllowedForAll;
   private int playerPingIndex;
   private CraftServer cserver;

   public PlayerList(MinecraftServer var1) {
      this.cserver = var1.server = new CraftServer(var1, this);
      var1.console = ColouredConsoleSender.getInstance();
      var1.reader.addCompleter(new ConsoleCommandCompleter(var1.server));
      this.bannedPlayers = new UserListBans(FILE_PLAYERBANS);
      this.bannedIPs = new UserListIPBans(FILE_IPBANS);
      this.ops = new UserListOps(FILE_OPS);
      this.whiteListedPlayers = new UserListWhitelist(FILE_WHITELIST);
      this.playerStatFiles = Maps.newHashMap();
      this.mcServer = var1;
      this.bannedPlayers.setLanServer(false);
      this.bannedIPs.setLanServer(false);
      this.maxPlayers = 8;
   }

   public void initializeConnectionToPlayer(NetworkManager var1, EntityPlayerMP var2) {
      GameProfile var3 = var2.getGameProfile();
      PlayerProfileCache var4 = this.mcServer.getUserCache();
      GameProfile var5 = var4.getProfileByUUID(var3.getId());
      String var6 = var5 == null ? var3.getName() : var5.getName();
      var4.addEntry(var3);
      NBTTagCompound var7 = this.readPlayerDataFromFile(var2);
      if (var7 != null && var7.hasKey("bukkit")) {
         NBTTagCompound var8 = var7.getCompoundTag("bukkit");
         var6 = var8.hasKey("lastKnownName", 8) ? var8.getString("lastKnownName") : var6;
      }

      var2.setWorld(this.mcServer.getWorldServer(var2.dimension));
      var2.interactionManager.setWorld((WorldServer)var2.world);
      String var20 = "local";
      if (var1.getRemoteAddress() != null) {
         var20 = var1.getRemoteAddress().toString();
      }

      WorldServer var9 = this.mcServer.getWorldServer(var2.dimension);
      WorldInfo var10 = var9.getWorldInfo();
      BlockPos var11 = var9.getSpawnPoint();
      this.setPlayerGameTypeBasedOnOther(var2, (EntityPlayerMP)null, var9);
      NetHandlerPlayServer var12 = new NetHandlerPlayServer(this.mcServer, var1, var2);
      var12.sendPacket(new SPacketJoinGame(var2.getEntityId(), var2.interactionManager.getGameType(), var10.isHardcoreModeEnabled(), var9.provider.getDimensionType().getId(), var9.getDifficulty(), this.getMaxPlayers(), var10.getTerrainType(), var9.getGameRules().getBoolean("reducedDebugInfo")));
      var2.getBukkitEntity().sendSupportedChannels();
      var12.sendPacket(new SPacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString(this.getServer().getServerModName())));
      var12.sendPacket(new SPacketServerDifficulty(var10.getDifficulty(), var10.isDifficultyLocked()));
      var12.sendPacket(new SPacketSpawnPosition(var11));
      var12.sendPacket(new SPacketPlayerAbilities(var2.capabilities));
      var12.sendPacket(new SPacketHeldItemChange(var2.inventory.currentItem));
      this.updatePermissionLevel(var2);
      var2.getStatFile().markAllDirty();
      var2.getStatFile().sendAchievements(var2);
      this.sendScoreboard((ServerScoreboard)var9.getScoreboard(), var2);
      this.mcServer.aC();
      String var13;
      if (var2.getName().equalsIgnoreCase(var6)) {
         var13 = "§e" + I18n.translateToLocalFormatted("multiplayer.player.joined", var2.getName());
      } else {
         var13 = "§e" + I18n.translateToLocalFormatted("multiplayer.player.joined.renamed", var2.getName(), var6);
      }

      this.onPlayerJoin(var2, var13);
      var9 = this.mcServer.getWorldServer(var2.dimension);
      var12.setPlayerLocation(var2.posX, var2.posY, var2.posZ, var2.rotationYaw, var2.rotationPitch);
      this.updateTimeAndWeatherForPlayer(var2, var9);
      if (!this.mcServer.getResourcePack().isEmpty()) {
         var2.loadResourcePack(this.mcServer.getResourcePack(), this.mcServer.getResourcePackHash());
      }

      for(PotionEffect var15 : var2.getActivePotionEffects()) {
         var12.sendPacket(new SPacketEntityEffect(var2.getEntityId(), var15));
      }

      if (var7 != null) {
         if (var7.hasKey("RootVehicle", 10)) {
            NBTTagCompound var22 = var7.getCompoundTag("RootVehicle");
            Entity var16 = AnvilChunkLoader.readWorldEntity(var22.getCompoundTag("Entity"), var9, true);
            if (var16 != null) {
               UUID var17 = var22.getUniqueId("Attach");
               if (var16.getUniqueID().equals(var17)) {
                  var2.startRiding(var16, true);
               } else {
                  for(Entity var19 : var16.getRecursivePassengers()) {
                     if (var19.getUniqueID().equals(var17)) {
                        var2.startRiding(var19, true);
                        break;
                     }
                  }
               }

               if (!var2.isRiding()) {
                  LOG.warn("Couldn't reattach entity to player");
                  var9.removeEntityDangerously(var16);

                  for(Entity var25 : var16.getRecursivePassengers()) {
                     var9.removeEntityDangerously(var25);
                  }
               }
            }
         } else if (var7.hasKey("Riding", 10)) {
            Entity var23 = AnvilChunkLoader.readWorldEntity(var7.getCompoundTag("Riding"), var9, true);
            if (var23 != null) {
               var2.startRiding(var23, true);
            }
         }
      }

      var2.addSelfToInternalCraftingInventory();
      LOG.info(var2.getName() + "[" + var20 + "] logged in with entity id " + var2.getEntityId() + " at ([" + var2.world.worldInfo.getWorldName() + "]" + var2.posX + ", " + var2.posY + ", " + var2.posZ + ")");
   }

   public void sendScoreboard(ServerScoreboard var1, EntityPlayerMP var2) {
      HashSet var3 = Sets.newHashSet();

      for(ScorePlayerTeam var5 : var1.getTeams()) {
         var2.connection.sendPacket(new SPacketTeams(var5, 0));
      }

      for(int var10 = 0; var10 < 19; ++var10) {
         ScoreObjective var6 = var1.getObjectiveInDisplaySlot(var10);
         if (var6 != null && !var3.contains(var6)) {
            for(Packet var9 : var1.getCreatePackets(var6)) {
               var2.connection.sendPacket(var9);
            }

            var3.add(var6);
         }
      }

   }

   public void setPlayerManager(WorldServer[] var1) {
      if (this.playerNBTManagerObj == null) {
         this.playerNBTManagerObj = var1[0].getSaveHandler().getPlayerNBTManager();
         var1[0].getWorldBorder().addListener(new IBorderListener() {
            public void onSizeChanged(WorldBorder var1, double var2) {
               PlayerList.this.sendAll(new SPacketWorldBorder(var1, SPacketWorldBorder.Action.SET_SIZE), var1.world);
            }

            public void onTransitionStarted(WorldBorder var1, double var2, double var4, long var6) {
               PlayerList.this.sendAll(new SPacketWorldBorder(var1, SPacketWorldBorder.Action.LERP_SIZE), var1.world);
            }

            public void onCenterChanged(WorldBorder var1, double var2, double var4) {
               PlayerList.this.sendAll(new SPacketWorldBorder(var1, SPacketWorldBorder.Action.SET_CENTER), var1.world);
            }

            public void onWarningTimeChanged(WorldBorder var1, int var2) {
               PlayerList.this.sendAll(new SPacketWorldBorder(var1, SPacketWorldBorder.Action.SET_WARNING_TIME), var1.world);
            }

            public void onWarningDistanceChanged(WorldBorder var1, int var2) {
               PlayerList.this.sendAll(new SPacketWorldBorder(var1, SPacketWorldBorder.Action.SET_WARNING_BLOCKS), var1.world);
            }

            public void onDamageAmountChanged(WorldBorder var1, double var2) {
            }

            public void onDamageBufferChanged(WorldBorder var1, double var2) {
            }
         });
      }
   }

   public void preparePlayer(EntityPlayerMP var1, WorldServer var2) {
      WorldServer var3 = var1.getServerWorld();
      if (var2 != null) {
         var2.getPlayerChunkMap().removePlayer(var1);
      }

      var3.getPlayerChunkMap().addPlayer(var1);
      var3.getChunkProvider().provideChunk((int)var1.posX >> 4, (int)var1.posZ >> 4);
   }

   public int getEntityViewDistance() {
      return PlayerChunkMap.getFurthestViewableBlock(this.getViewDistance());
   }

   public NBTTagCompound readPlayerDataFromFile(EntityPlayerMP var1) {
      NBTTagCompound var2 = ((WorldServer)this.mcServer.worlds.get(0)).getWorldInfo().getPlayerNBTTagCompound();
      NBTTagCompound var3;
      if (var1.getName().equals(this.mcServer.Q()) && var2 != null) {
         var3 = this.mcServer.getDataConverterManager().process(FixTypes.PLAYER, var2);
         var1.readFromNBT(var3);
         LOG.debug("loading single player");
      } else {
         var3 = this.playerNBTManagerObj.readPlayerData(var1);
      }

      return var3;
   }

   protected void writePlayerData(EntityPlayerMP var1) {
      this.playerNBTManagerObj.writePlayerData(var1);
      StatisticsManagerServer var2 = (StatisticsManagerServer)this.playerStatFiles.get(var1.getUniqueID());
      if (var2 != null) {
         var2.saveStatFile();
      }

   }

   public void onPlayerJoin(EntityPlayerMP var1, String var2) {
      this.playerEntityList.add(var1);
      this.uuidToPlayerMap.put(var1.getUniqueID(), var1);
      WorldServer var3 = this.mcServer.getWorldServer(var1.dimension);
      PlayerJoinEvent var4 = new PlayerJoinEvent(this.cserver.getPlayer(var1), var2);
      this.cserver.getPluginManager().callEvent(var4);
      var2 = var4.getJoinMessage();
      if (var2 != null && var2.length() > 0) {
         ITextComponent[] var5;
         for(ITextComponent var8 : var5 = CraftChatMessage.fromString(var2)) {
            this.mcServer.getPlayerList().sendPacketToAllPlayers(new SPacketChat(var8));
         }
      }

      ChunkIOExecutor.adjustPoolSize(this.getCurrentPlayerCount());
      SPacketPlayerListItem var12 = new SPacketPlayerListItem(SPacketPlayerListItem.Action.ADD_PLAYER, new EntityPlayerMP[]{var1});

      for(int var11 = 0; var11 < this.playerEntityList.size(); ++var11) {
         EntityPlayerMP var10 = (EntityPlayerMP)this.playerEntityList.get(var11);
         if (var10.getBukkitEntity().canSee(var1.getBukkitEntity())) {
            var10.connection.sendPacket(var12);
         }

         if (var1.getBukkitEntity().canSee(var10.getBukkitEntity())) {
            var1.connection.sendPacket(new SPacketPlayerListItem(SPacketPlayerListItem.Action.ADD_PLAYER, new EntityPlayerMP[]{var10}));
         }
      }

      if (var1.world == var3 && !var3.playerEntities.contains(var1)) {
         var3.spawnEntity(var1);
         this.preparePlayer(var1, (WorldServer)null);
      }

   }

   public void serverUpdateMovingPlayer(EntityPlayerMP var1) {
      var1.getServerWorld().getPlayerChunkMap().updateMovingPlayer(var1);
   }

   public String disconnect(EntityPlayerMP var1) {
      WorldServer var2 = var1.getServerWorld();
      var1.addStat(StatList.LEAVE_GAME);
      CraftEventFactory.handleInventoryCloseEvent(var1);
      PlayerQuitEvent var3 = new PlayerQuitEvent(this.cserver.getPlayer(var1), "§e" + var1.getName() + " left the game");
      this.cserver.getPluginManager().callEvent(var3);
      var1.getBukkitEntity().disconnect(var3.getQuitMessage());
      var1.onUpdateEntity();
      this.writePlayerData(var1);
      if (var1.isRiding()) {
         Entity var4 = var1.getLowestRidingEntity();
         if (var4.getRecursivePassengersByType(EntityPlayerMP.class).size() == 1) {
            LOG.debug("Removing player mount");
            var1.dismountRidingEntity();
            var2.removeEntityDangerously(var4);

            for(Entity var6 : var4.getRecursivePassengers()) {
               var2.removeEntityDangerously(var6);
            }

            var2.getChunkFromChunkCoords(var1.chunkCoordX, var1.chunkCoordZ).setChunkModified();
         }
      }

      var2.removeEntity(var1);
      var2.getPlayerChunkMap().removePlayer(var1);
      this.playerEntityList.remove(var1);
      UUID var9 = var1.getUniqueID();
      EntityPlayerMP var10 = (EntityPlayerMP)this.uuidToPlayerMap.get(var9);
      if (var10 == var1) {
         this.uuidToPlayerMap.remove(var9);
         this.playerStatFiles.remove(var9);
      }

      SPacketPlayerListItem var11 = new SPacketPlayerListItem(SPacketPlayerListItem.Action.REMOVE_PLAYER, new EntityPlayerMP[]{var1});

      for(int var7 = 0; var7 < this.playerEntityList.size(); ++var7) {
         EntityPlayerMP var8 = (EntityPlayerMP)this.playerEntityList.get(var7);
         if (var8.getBukkitEntity().canSee(var1.getBukkitEntity())) {
            var8.connection.sendPacket(var11);
         } else {
            var8.getBukkitEntity().removeDisconnectingPlayer(var1.getBukkitEntity());
         }
      }

      this.cserver.getScoreboardManager().removePlayer(var1.getBukkitEntity());
      ChunkIOExecutor.adjustPoolSize(this.getCurrentPlayerCount());
      return var3.getQuitMessage();
   }

   public EntityPlayerMP attemptLogin(NetHandlerLoginServer var1, GameProfile var2, String var3) {
      UUID var4 = EntityPlayer.getUUID(var2);
      ArrayList var5 = Lists.newArrayList();

      for(int var6 = 0; var6 < this.playerEntityList.size(); ++var6) {
         EntityPlayerMP var7 = (EntityPlayerMP)this.playerEntityList.get(var6);
         if (var7.getUniqueID().equals(var4)) {
            var5.add(var7);
         }
      }

      for(EntityPlayerMP var15 : var5) {
         this.writePlayerData(var15);
         var15.connection.disconnect("You logged in from another location");
      }

      SocketAddress var8 = var1.networkManager.getRemoteAddress();
      EntityPlayerMP var9 = new EntityPlayerMP(this.mcServer, this.mcServer.getWorldServer(0), var2, new PlayerInteractionManager(this.mcServer.getWorldServer(0)));
      CraftPlayer var10 = var9.getBukkitEntity();
      PlayerLoginEvent var11 = new PlayerLoginEvent(var10, var3, ((InetSocketAddress)var8).getAddress());
      if (this.getBannedPlayers().isBanned(var2) && !((UserListBansEntry)this.getBannedPlayers().getEntry(var2)).hasBanExpired()) {
         UserListBansEntry var16 = (UserListBansEntry)this.bannedPlayers.getEntry(var2);
         String var17 = "You are banned from this server!\nReason: " + var16.getBanReason();
         if (var16.getBanEndDate() != null) {
            var17 = var17 + "\nYour ban will be removed on " + DATE_FORMAT.format(var16.getBanEndDate());
         }

         var11.disallow(Result.KICK_BANNED, var17);
      } else if (!this.canJoin(var2)) {
         var11.disallow(Result.KICK_WHITELIST, "You are not white-listed on this server!");
      } else if (this.getBannedIPs().isBanned(var8) && !this.getBannedIPs().getBanEntry(var8).hasBanExpired()) {
         UserListIPBansEntry var12 = this.bannedIPs.getBanEntry(var8);
         String var13 = "Your IP address is banned from this server!\nReason: " + var12.getBanReason();
         if (var12.getBanEndDate() != null) {
            var13 = var13 + "\nYour ban will be removed on " + DATE_FORMAT.format(var12.getBanEndDate());
         }

         var11.disallow(Result.KICK_BANNED, var13);
      } else if (this.playerEntityList.size() >= this.maxPlayers && !this.bypassesPlayerLimit(var2)) {
         var11.disallow(Result.KICK_FULL, "The server is full");
      }

      this.cserver.getPluginManager().callEvent(var11);
      if (var11.getResult() != Result.ALLOWED) {
         var1.closeConnection(var11.getKickMessage());
         return null;
      } else {
         return var9;
      }
   }

   public EntityPlayerMP processLogin(GameProfile var1, EntityPlayerMP var2) {
      return var2;
   }

   public EntityPlayerMP recreatePlayerEntity(EntityPlayerMP var1, int var2, boolean var3) {
      return this.moveToWorld(var1, var2, var3, (Location)null, true);
   }

   public EntityPlayerMP moveToWorld(EntityPlayerMP var1, int var2, boolean var3, Location var4, boolean var5) {
      var1.getServerWorld().getEntityTracker().removePlayerFromTrackers(var1);
      var1.getServerWorld().getPlayerChunkMap().removePlayer(var1);
      this.playerEntityList.remove(var1);
      this.mcServer.getWorldServer(var1.dimension).removeEntityDangerously(var1);
      BlockPos var6 = var1.getBedLocation();
      boolean var7 = var1.isSpawnForced();
      EntityPlayerMP var8 = var1;
      World var9 = var1.getBukkitEntity().getWorld();
      var1.playerConqueredTheEnd = false;
      var1.connection = var1.connection;
      var1.clonePlayer(var1, var3);
      var1.setEntityId(var1.getEntityId());
      var1.setCommandStats(var1);
      var1.setPrimaryHand(var1.getPrimaryHand());

      for(String var11 : var1.getTags()) {
         var8.addTag(var11);
      }

      if (var4 == null) {
         boolean var12 = false;
         CraftWorld var13 = (CraftWorld)this.mcServer.server.getWorld(var1.spawnWorld);
         if (var13 != null && var6 != null) {
            BlockPos var18 = EntityPlayer.getBedSpawnLocation(var13.getHandle(), var6, var7);
            if (var18 != null) {
               var12 = true;
               var4 = new Location(var13, (double)((float)var18.getX() + 0.5F), (double)((float)var18.getY() + 0.1F), (double)((float)var18.getZ() + 0.5F));
            } else {
               var8.setSpawnPoint((BlockPos)null, true);
               var8.connection.sendPacket(new SPacketChangeGameState(0, 0.0F));
            }
         }

         if (var4 == null) {
            var13 = (CraftWorld)this.mcServer.server.getWorlds().get(0);
            var6 = var13.getHandle().getSpawnPoint();
            var4 = new Location(var13, (double)((float)var6.getX() + 0.5F), (double)((float)var6.getY() + 0.1F), (double)((float)var6.getZ() + 0.5F));
         }

         Player var14 = this.cserver.getPlayer(var8);
         PlayerRespawnEvent var15 = new PlayerRespawnEvent(var14, var4, var12);
         this.cserver.getPluginManager().callEvent(var15);
         var4 = var15.getRespawnLocation();
         var1.reset();
      } else {
         var4.setWorld(this.mcServer.getWorldServer(var2).getWorld());
      }

      WorldServer var20 = ((CraftWorld)var4.getWorld()).getHandle();
      var8.forceSetPositionRotation(var4.getX(), var4.getY(), var4.getZ(), var4.getYaw(), var4.getPitch());
      var20.getChunkProvider().provideChunk((int)var8.posX >> 4, (int)var8.posZ >> 4);

      while(var5 && !var20.getCollisionBoxes(var8, var8.getEntityBoundingBox()).isEmpty() && var8.posY < 256.0D) {
         var8.setPosition(var8.posX, var8.posY + 1.0D, var8.posZ);
      }

      byte var22 = (byte)var20.getWorld().getEnvironment().getId();
      if (var9.getEnvironment() == var20.getWorld().getEnvironment()) {
         var8.connection.sendPacket(new SPacketRespawn((byte)(var22 >= 0 ? -1 : 0), var20.getDifficulty(), var20.getWorldInfo().getTerrainType(), var1.interactionManager.getGameType()));
      }

      var8.connection.sendPacket(new SPacketRespawn(var22, var20.getDifficulty(), var20.getWorldInfo().getTerrainType(), var8.interactionManager.getGameType()));
      var8.setWorld(var20);
      var8.isDead = false;
      var8.connection.teleport(new Location(var20.getWorld(), var8.posX, var8.posY, var8.posZ, var8.rotationYaw, var8.rotationPitch));
      var8.setSneaking(false);
      BlockPos var19 = var20.getSpawnPoint();
      var8.connection.sendPacket(new SPacketSpawnPosition(var19));
      var8.connection.sendPacket(new SPacketSetExperience(var8.experience, var8.experienceTotal, var8.experienceLevel));
      this.updateTimeAndWeatherForPlayer(var8, var20);
      this.updatePermissionLevel(var8);
      if (!var1.connection.isDisconnected()) {
         var20.getPlayerChunkMap().addPlayer(var8);
         var20.spawnEntity(var8);
         this.playerEntityList.add(var8);
         this.uuidToPlayerMap.put(var8.getUniqueID(), var8);
      }

      var8.setHealth(var8.getHealth());
      this.syncPlayerInventory(var1);
      var1.sendPlayerAbilities();

      for(Object var23 : var1.getActivePotionEffects()) {
         PotionEffect var16 = (PotionEffect)var23;
         var1.connection.sendPacket(new SPacketEntityEffect(var1.getEntityId(), var16));
      }

      if (var9 != var4.getWorld()) {
         PlayerChangedWorldEvent var24 = new PlayerChangedWorldEvent(var1.getBukkitEntity(), var9);
         this.mcServer.server.getPluginManager().callEvent(var24);
      }

      if (var1.connection.isDisconnected()) {
         this.writePlayerData(var1);
      }

      return var8;
   }

   public void changeDimension(EntityPlayerMP var1, int var2, TeleportCause var3) {
      WorldServer var4 = null;
      if (var1.dimension < 10) {
         for(WorldServer var6 : this.mcServer.worlds) {
            if (var6.dimension == var2) {
               var4 = var6;
            }
         }
      }

      Location var15 = var1.getBukkitEntity().getLocation();
      Location var13 = null;
      boolean var7 = false;
      if (var4 != null) {
         if (var3 == TeleportCause.END_PORTAL && var2 == 0) {
            var13 = var1.getBukkitEntity().getBedSpawnLocation();
            if (var13 == null || ((CraftWorld)var13.getWorld()).getHandle().dimension != 0) {
               var13 = var4.getWorld().getSpawnLocation();
            }
         } else {
            var13 = this.calculateTarget(var15, var4);
            var7 = true;
         }
      }

      TravelAgent var8 = var13 != null ? (TravelAgent)((CraftWorld)var13.getWorld()).getHandle().getDefaultTeleporter() : CraftTravelAgent.DEFAULT;
      PlayerPortalEvent var9 = new PlayerPortalEvent(var1.getBukkitEntity(), var15, var13, var8, var3);
      var9.useTravelAgent(var7);
      Bukkit.getServer().getPluginManager().callEvent(var9);
      if (!var9.isCancelled() && var9.getTo() != null) {
         var13 = var9.useTravelAgent() ? var9.getPortalTravelAgent().findOrCreate(var9.getTo()) : var9.getTo();
         if (var13 != null) {
            var4 = ((CraftWorld)var13.getWorld()).getHandle();
            PlayerTeleportEvent var10 = new PlayerTeleportEvent(var1.getBukkitEntity(), var15, var13, var3);
            Bukkit.getServer().getPluginManager().callEvent(var10);
            if (!var10.isCancelled() && var10.getTo() != null) {
               Vector var11 = var1.getBukkitEntity().getVelocity();
               var4.getDefaultTeleporter().adjustExit(var1, var13, var11);
               var1.invulnerableDimensionChange = true;
               this.moveToWorld(var1, var4.dimension, true, var13, false);
               if (var1.motionX != var11.getX() || var1.motionY != var11.getY() || var1.motionZ != var11.getZ()) {
                  var1.getBukkitEntity().setVelocity(var11);
               }

            }
         }
      }
   }

   public void updatePermissionLevel(EntityPlayerMP var1) {
      GameProfile var2 = var1.getGameProfile();
      int var3 = this.canSendCommands(var2) ? this.ops.getPermissionLevel(var2) : 0;
      var3 = this.mcServer.R() && this.mcServer.worldServer[0].getWorldInfo().areCommandsAllowed() ? 4 : var3;
      var3 = this.commandsAllowedForAll ? 4 : var3;
      this.sendPlayerPermissionLevel(var1, var3);
   }

   public void changePlayerDimension(EntityPlayerMP var1, int var2) {
      int var3 = var1.dimension;
      WorldServer var4 = this.mcServer.getWorldServer(var1.dimension);
      var1.dimension = var2;
      WorldServer var5 = this.mcServer.getWorldServer(var1.dimension);
      var1.connection.sendPacket(new SPacketRespawn(var1.dimension, var1.world.getDifficulty(), var1.world.getWorldInfo().getTerrainType(), var1.interactionManager.getGameType()));
      this.updatePermissionLevel(var1);
      var4.removeEntityDangerously(var1);
      var1.isDead = false;
      this.transferEntityToWorld(var1, var3, var4, var5);
      this.preparePlayer(var1, var4);
      var1.connection.setPlayerLocation(var1.posX, var1.posY, var1.posZ, var1.rotationYaw, var1.rotationPitch);
      var1.interactionManager.setWorld(var5);
      var1.connection.sendPacket(new SPacketPlayerAbilities(var1.capabilities));
      this.updateTimeAndWeatherForPlayer(var1, var5);
      this.syncPlayerInventory(var1);

      for(PotionEffect var7 : var1.getActivePotionEffects()) {
         var1.connection.sendPacket(new SPacketEntityEffect(var1.getEntityId(), var7));
      }

   }

   public void transferEntityToWorld(Entity var1, int var2, WorldServer var3, WorldServer var4) {
      Location var5 = this.calculateTarget(var1.getBukkitEntity().getLocation(), var4);
      this.repositionEntity(var1, var5, true);
   }

   public Location calculateTarget(Location var1, net.minecraft.world.World var2) {
      WorldServer var3 = ((CraftWorld)var1.getWorld()).getHandle();
      WorldServer var4 = var2.getWorld().getHandle();
      int var5 = var3.dimension;
      double var6 = var1.getY();
      float var8 = var1.getYaw();
      float var9 = var1.getPitch();
      double var10 = var1.getX();
      double var12 = var1.getZ();
      double var14 = 8.0D;
      if (var4.dimension == -1) {
         var10 = MathHelper.clamp(var10 / var14, var4.getWorldBorder().minX() + 16.0D, var4.getWorldBorder().maxX() - 16.0D);
         var12 = MathHelper.clamp(var12 / var14, var4.getWorldBorder().minZ() + 16.0D, var4.getWorldBorder().maxZ() - 16.0D);
      } else if (var4.dimension == 0) {
         var10 = MathHelper.clamp(var10 * var14, var4.getWorldBorder().minX() + 16.0D, var4.getWorldBorder().maxX() - 16.0D);
         var12 = MathHelper.clamp(var12 * var14, var4.getWorldBorder().minZ() + 16.0D, var4.getWorldBorder().maxZ() - 16.0D);
      } else {
         BlockPos var16;
         if (var5 == 1) {
            var4 = (WorldServer)this.mcServer.worlds.get(0);
            var16 = var4.getSpawnPoint();
         } else {
            var16 = var4.getSpawnCoordinate();
         }

         var10 = (double)var16.getX();
         var6 = (double)var16.getY();
         var12 = (double)var16.getZ();
      }

      if (var5 != 1) {
         var3.theProfiler.startSection("placing");
         var10 = (double)MathHelper.clamp((int)var10, -29999872, 29999872);
         var12 = (double)MathHelper.clamp((int)var12, -29999872, 29999872);
      }

      return new Location(var4.getWorld(), var10, var6, var12, var8, var9);
   }

   public void repositionEntity(Entity var1, Location var2, boolean var3) {
      WorldServer var4 = (WorldServer)var1.world;
      WorldServer var5 = ((CraftWorld)var2.getWorld()).getHandle();
      int var6 = var4.dimension;
      var1.setLocationAndAngles(var2.getX(), var2.getY(), var2.getZ(), var2.getYaw(), var2.getPitch());
      if (var1.isEntityAlive()) {
         var4.updateEntityWithOptionalForce(var1, false);
      }

      var4.theProfiler.endSection();
      if (var6 != 1) {
         var4.theProfiler.startSection("placing");
         if (var1.isEntityAlive()) {
            if (var3) {
               Vector var7 = var1.getBukkitEntity().getVelocity();
               var5.getDefaultTeleporter().adjustExit(var1, var2, var7);
               var1.setLocationAndAngles(var2.getX(), var2.getY(), var2.getZ(), var2.getYaw(), var2.getPitch());
               if (var1.motionX != var7.getX() || var1.motionY != var7.getY() || var1.motionZ != var7.getZ()) {
                  var1.getBukkitEntity().setVelocity(var7);
               }
            }

            var5.updateEntityWithOptionalForce(var1, false);
         }

         var4.theProfiler.endSection();
      }

      var1.setWorld(var5);
   }

   public void onTick() {
      if (++this.playerPingIndex > 600) {
         this.sendPacketToAllPlayers(new SPacketPlayerListItem(SPacketPlayerListItem.Action.UPDATE_LATENCY, this.playerEntityList));
         this.playerPingIndex = 0;
      }

   }

   public void sendPacketToAllPlayers(Packet var1) {
      for(int var2 = 0; var2 < this.playerEntityList.size(); ++var2) {
         ((EntityPlayerMP)this.playerEntityList.get(var2)).connection.sendPacket(var1);
      }

   }

   public void sendAll(Packet var1, EntityPlayer var2) {
      for(int var3 = 0; var3 < this.playerEntityList.size(); ++var3) {
         EntityPlayerMP var4 = (EntityPlayerMP)this.playerEntityList.get(var3);
         if (var2 == null || !(var2 instanceof EntityPlayerMP) || var4.getBukkitEntity().canSee(((EntityPlayerMP)var2).getBukkitEntity())) {
            ((EntityPlayerMP)this.playerEntityList.get(var3)).connection.sendPacket(var1);
         }
      }

   }

   public void sendAll(Packet var1, net.minecraft.world.World var2) {
      for(int var3 = 0; var3 < var2.playerEntities.size(); ++var3) {
         ((EntityPlayerMP)var2.playerEntities.get(var3)).connection.sendPacket(var1);
      }

   }

   public void sendPacketToAllPlayersInDimension(Packet var1, int var2) {
      for(int var3 = 0; var3 < this.playerEntityList.size(); ++var3) {
         EntityPlayerMP var4 = (EntityPlayerMP)this.playerEntityList.get(var3);
         if (var4.dimension == var2) {
            var4.connection.sendPacket(var1);
         }
      }

   }

   public void sendMessageToAllTeamMembers(EntityPlayer var1, ITextComponent var2) {
      Team var3 = var1.getTeam();
      if (var3 != null) {
         for(String var6 : var3.getMembershipCollection()) {
            EntityPlayerMP var7 = this.getPlayerByUsername(var6);
            if (var7 != null && var7 != var1) {
               var7.sendMessage(var2);
            }
         }
      }

   }

   public void sendMessageToTeamOrAllPlayers(EntityPlayer var1, ITextComponent var2) {
      Team var3 = var1.getTeam();
      if (var3 == null) {
         this.sendChatMsg(var2);
      } else {
         for(int var4 = 0; var4 < this.playerEntityList.size(); ++var4) {
            EntityPlayerMP var5 = (EntityPlayerMP)this.playerEntityList.get(var4);
            if (var5.getTeam() != var3) {
               var5.sendMessage(var2);
            }
         }
      }

   }

   public String getFormattedListOfPlayers(boolean var1) {
      String var2 = "";
      ArrayList var3 = Lists.newArrayList(this.playerEntityList);

      for(int var4 = 0; var4 < var3.size(); ++var4) {
         if (var4 > 0) {
            var2 = var2 + ", ";
         }

         var2 = var2 + ((EntityPlayerMP)var3.get(var4)).getName();
         if (var1) {
            var2 = var2 + " (" + ((EntityPlayerMP)var3.get(var4)).getCachedUniqueIdString() + ")";
         }
      }

      return var2;
   }

   public String[] getOnlinePlayerNames() {
      String[] var1 = new String[this.playerEntityList.size()];

      for(int var2 = 0; var2 < this.playerEntityList.size(); ++var2) {
         var1[var2] = ((EntityPlayerMP)this.playerEntityList.get(var2)).getName();
      }

      return var1;
   }

   public GameProfile[] getOnlinePlayerProfiles() {
      GameProfile[] var1 = new GameProfile[this.playerEntityList.size()];

      for(int var2 = 0; var2 < this.playerEntityList.size(); ++var2) {
         var1[var2] = ((EntityPlayerMP)this.playerEntityList.get(var2)).getGameProfile();
      }

      return var1;
   }

   public UserListBans getBannedPlayers() {
      return this.bannedPlayers;
   }

   public UserListIPBans getBannedIPs() {
      return this.bannedIPs;
   }

   public void addOp(GameProfile var1) {
      int var2 = this.mcServer.q();
      this.ops.addEntry(new UserListOpsEntry(var1, this.mcServer.q(), this.ops.bypassesPlayerLimit(var1)));
      this.sendPlayerPermissionLevel(this.getPlayerByUUID(var1.getId()), var2);
      Player var3 = this.mcServer.server.getPlayer(var1.getId());
      if (var3 != null) {
         var3.recalculatePermissions();
      }

   }

   public void removeOp(GameProfile var1) {
      this.ops.removeEntry(var1);
      this.sendPlayerPermissionLevel(this.getPlayerByUUID(var1.getId()), 0);
      Player var2 = this.mcServer.server.getPlayer(var1.getId());
      if (var2 != null) {
         var2.recalculatePermissions();
      }

   }

   private void sendPlayerPermissionLevel(EntityPlayerMP var1, int var2) {
      if (var1 != null && var1.connection != null) {
         byte var3;
         if (var2 <= 0) {
            var3 = 24;
         } else if (var2 >= 4) {
            var3 = 28;
         } else {
            var3 = (byte)(24 + var2);
         }

         var1.connection.sendPacket(new SPacketEntityStatus(var1, var3));
      }

   }

   public boolean canJoin(GameProfile var1) {
      return !this.whiteListEnforced || this.ops.hasEntry(var1) || this.whiteListedPlayers.hasEntry(var1);
   }

   public boolean canSendCommands(GameProfile var1) {
      return this.ops.hasEntry(var1) || this.mcServer.R() && ((WorldServer)this.mcServer.worlds.get(0)).getWorldInfo().areCommandsAllowed() && this.mcServer.Q().equalsIgnoreCase(var1.getName()) || this.commandsAllowedForAll;
   }

   @Nullable
   public EntityPlayerMP getPlayerByUsername(String var1) {
      for(EntityPlayerMP var3 : this.playerEntityList) {
         if (var3.getName().equalsIgnoreCase(var1)) {
            return var3;
         }
      }

      return null;
   }

   public void sendToAllNearExcept(@Nullable EntityPlayer var1, double var2, double var4, double var6, double var8, int var10, Packet var11) {
      for(int var12 = 0; var12 < this.playerEntityList.size(); ++var12) {
         EntityPlayerMP var13 = (EntityPlayerMP)this.playerEntityList.get(var12);
         if ((var1 == null || !(var1 instanceof EntityPlayerMP) || var13.getBukkitEntity().canSee(((EntityPlayerMP)var1).getBukkitEntity())) && var13 != var1 && var13.dimension == var10) {
            double var14 = var2 - var13.posX;
            double var16 = var4 - var13.posY;
            double var18 = var6 - var13.posZ;
            if (var14 * var14 + var16 * var16 + var18 * var18 < var8 * var8) {
               var13.connection.sendPacket(var11);
            }
         }
      }

   }

   public void saveAllPlayerData() {
      for(int var1 = 0; var1 < this.playerEntityList.size(); ++var1) {
         this.writePlayerData((EntityPlayerMP)this.playerEntityList.get(var1));
      }

   }

   public void addWhitelistedPlayer(GameProfile var1) {
      this.whiteListedPlayers.addEntry(new UserListWhitelistEntry(var1));
   }

   public void removePlayerFromWhitelist(GameProfile var1) {
      this.whiteListedPlayers.removeEntry(var1);
   }

   public UserListWhitelist getWhitelistedPlayers() {
      return this.whiteListedPlayers;
   }

   public String[] getWhitelistedPlayerNames() {
      return this.whiteListedPlayers.getKeys();
   }

   public UserListOps getOppedPlayers() {
      return this.ops;
   }

   public String[] getOppedPlayerNames() {
      return this.ops.getKeys();
   }

   public void reloadWhitelist() {
   }

   public void updateTimeAndWeatherForPlayer(EntityPlayerMP var1, WorldServer var2) {
      WorldBorder var3 = var1.world.getWorldBorder();
      var1.connection.sendPacket(new SPacketWorldBorder(var3, SPacketWorldBorder.Action.INITIALIZE));
      var1.connection.sendPacket(new SPacketTimeUpdate(var2.getTotalWorldTime(), var2.getWorldTime(), var2.getGameRules().getBoolean("doDaylightCycle")));
      if (var2.isRaining()) {
         var1.setPlayerWeather(WeatherType.DOWNFALL, false);
         var1.updateWeather(-var2.rainingStrength, var2.rainingStrength, -var2.thunderingStrength, var2.thunderingStrength);
      }

   }

   public void syncPlayerInventory(EntityPlayerMP var1) {
      var1.sendContainerToPlayer(var1.inventoryContainer);
      var1.getBukkitEntity().updateScaledHealth();
      var1.connection.sendPacket(new SPacketHeldItemChange(var1.inventory.currentItem));
   }

   public int getCurrentPlayerCount() {
      return this.playerEntityList.size();
   }

   public int getMaxPlayers() {
      return this.maxPlayers;
   }

   public String[] getAvailablePlayerDat() {
      return ((WorldServer)this.mcServer.worlds.get(0)).getSaveHandler().getPlayerNBTManager().getAvailablePlayerDat();
   }

   public boolean isWhiteListEnabled() {
      return this.whiteListEnforced;
   }

   public void setWhiteListEnabled(boolean var1) {
      this.whiteListEnforced = var1;
   }

   public List getPlayersMatchingAddress(String var1) {
      ArrayList var2 = Lists.newArrayList();

      for(EntityPlayerMP var4 : this.playerEntityList) {
         if (var4.getPlayerIP().equals(var1)) {
            var2.add(var4);
         }
      }

      return var2;
   }

   public int getViewDistance() {
      return this.viewDistance;
   }

   public MinecraftServer getServer() {
      return this.mcServer;
   }

   public NBTTagCompound getHostPlayerData() {
      return null;
   }

   private void setPlayerGameTypeBasedOnOther(EntityPlayerMP var1, EntityPlayerMP var2, net.minecraft.world.World var3) {
      if (var2 != null) {
         var1.interactionManager.setGameType(var2.interactionManager.getGameType());
      } else if (this.gameType != null) {
         var1.interactionManager.setGameType(this.gameType);
      }

      var1.interactionManager.initializeGameType(var3.getWorldInfo().getGameType());
   }

   public void removeAllPlayers() {
      for(EntityPlayerMP var2 : this.playerEntityList) {
         var2.connection.disconnect(this.mcServer.server.getShutdownMessage());
      }

   }

   public void sendMessage(ITextComponent[] var1) {
      for(ITextComponent var5 : var1) {
         this.sendChatMsgImpl(var5, true);
      }

   }

   public void sendChatMsgImpl(ITextComponent var1, boolean var2) {
      this.mcServer.sendMessage(var1);
      int var3 = var2 ? 1 : 0;
      this.sendPacketToAllPlayers(new SPacketChat(CraftChatMessage.fixComponent(var1), (byte)var3));
   }

   public void sendChatMsg(ITextComponent var1) {
      this.sendChatMsgImpl(var1, true);
   }

   public StatisticsManagerServer getPlayerStatsFile(EntityPlayer var1) {
      UUID var2 = var1.getUniqueID();
      StatisticsManagerServer var3 = var2 == null ? null : (StatisticsManagerServer)this.playerStatFiles.get(var2);
      if (var3 == null) {
         File var4 = new File(this.mcServer.getWorldServer(0).getSaveHandler().getWorldDirectory(), "stats");
         File var5 = new File(var4, var2 + ".json");
         if (!var5.exists()) {
            File var6 = new File(var4, var1.getName() + ".json");
            if (var6.exists() && var6.isFile()) {
               var6.renameTo(var5);
            }
         }

         var3 = new StatisticsManagerServer(this.mcServer, var5);
         var3.readStatFile();
         this.playerStatFiles.put(var2, var3);
      }

      return var3;
   }

   public void setViewDistance(int var1) {
      this.viewDistance = var1;
      if (this.mcServer.worldServer != null) {
         WorldServer[] var2 = this.mcServer.worldServer;
         int var10000 = var2.length;

         for(int var3 = 0; var3 < this.mcServer.worlds.size(); ++var3) {
            WorldServer var4 = (WorldServer)this.mcServer.worlds.get(0);
            if (var4 != null) {
               var4.getPlayerChunkMap().setPlayerViewRadius(var1);
               var4.getEntityTracker().setViewDistance(var1);
            }
         }
      }

   }

   public List getPlayers() {
      return this.playerEntityList;
   }

   public EntityPlayerMP getPlayerByUUID(UUID var1) {
      return (EntityPlayerMP)this.uuidToPlayerMap.get(var1);
   }

   public boolean bypassesPlayerLimit(GameProfile var1) {
      return false;
   }
}
