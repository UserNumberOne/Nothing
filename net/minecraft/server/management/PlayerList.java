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

   public PlayerList(MinecraftServer minecraftserver) {
      this.cserver = minecraftserver.server = new CraftServer(minecraftserver, this);
      minecraftserver.console = ColouredConsoleSender.getInstance();
      minecraftserver.reader.addCompleter(new ConsoleCommandCompleter(minecraftserver.server));
      this.bannedPlayers = new UserListBans(FILE_PLAYERBANS);
      this.bannedIPs = new UserListIPBans(FILE_IPBANS);
      this.ops = new UserListOps(FILE_OPS);
      this.whiteListedPlayers = new UserListWhitelist(FILE_WHITELIST);
      this.playerStatFiles = Maps.newHashMap();
      this.mcServer = minecraftserver;
      this.bannedPlayers.setLanServer(false);
      this.bannedIPs.setLanServer(false);
      this.maxPlayers = 8;
   }

   public void initializeConnectionToPlayer(NetworkManager networkmanager, EntityPlayerMP entityplayer) {
      GameProfile gameprofile = entityplayer.getGameProfile();
      PlayerProfileCache usercache = this.mcServer.getUserCache();
      GameProfile gameprofile1 = usercache.getProfileByUUID(gameprofile.getId());
      String s = gameprofile1 == null ? gameprofile.getName() : gameprofile1.getName();
      usercache.addEntry(gameprofile);
      NBTTagCompound nbttagcompound = this.readPlayerDataFromFile(entityplayer);
      if (nbttagcompound != null && nbttagcompound.hasKey("bukkit")) {
         NBTTagCompound bukkit = nbttagcompound.getCompoundTag("bukkit");
         s = bukkit.hasKey("lastKnownName", 8) ? bukkit.getString("lastKnownName") : s;
      }

      entityplayer.setWorld(this.mcServer.getWorldServer(entityplayer.dimension));
      entityplayer.interactionManager.setWorld((WorldServer)entityplayer.world);
      String s1 = "local";
      if (networkmanager.getRemoteAddress() != null) {
         s1 = networkmanager.getRemoteAddress().toString();
      }

      WorldServer worldserver = this.mcServer.getWorldServer(entityplayer.dimension);
      WorldInfo worlddata = worldserver.getWorldInfo();
      BlockPos blockposition = worldserver.getSpawnPoint();
      this.setPlayerGameTypeBasedOnOther(entityplayer, (EntityPlayerMP)null, worldserver);
      NetHandlerPlayServer playerconnection = new NetHandlerPlayServer(this.mcServer, networkmanager, entityplayer);
      playerconnection.sendPacket(new SPacketJoinGame(entityplayer.getEntityId(), entityplayer.interactionManager.getGameType(), worlddata.isHardcoreModeEnabled(), worldserver.provider.getDimensionType().getId(), worldserver.getDifficulty(), this.getMaxPlayers(), worlddata.getTerrainType(), worldserver.getGameRules().getBoolean("reducedDebugInfo")));
      entityplayer.getBukkitEntity().sendSupportedChannels();
      playerconnection.sendPacket(new SPacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString(this.getServer().getServerModName())));
      playerconnection.sendPacket(new SPacketServerDifficulty(worlddata.getDifficulty(), worlddata.isDifficultyLocked()));
      playerconnection.sendPacket(new SPacketSpawnPosition(blockposition));
      playerconnection.sendPacket(new SPacketPlayerAbilities(entityplayer.capabilities));
      playerconnection.sendPacket(new SPacketHeldItemChange(entityplayer.inventory.currentItem));
      this.updatePermissionLevel(entityplayer);
      entityplayer.getStatFile().markAllDirty();
      entityplayer.getStatFile().sendAchievements(entityplayer);
      this.sendScoreboard((ServerScoreboard)worldserver.getScoreboard(), entityplayer);
      this.mcServer.aC();
      String joinMessage;
      if (entityplayer.getName().equalsIgnoreCase(s)) {
         joinMessage = "§e" + I18n.translateToLocalFormatted("multiplayer.player.joined", entityplayer.getName());
      } else {
         joinMessage = "§e" + I18n.translateToLocalFormatted("multiplayer.player.joined.renamed", entityplayer.getName(), s);
      }

      this.onPlayerJoin(entityplayer, joinMessage);
      worldserver = this.mcServer.getWorldServer(entityplayer.dimension);
      playerconnection.setPlayerLocation(entityplayer.posX, entityplayer.posY, entityplayer.posZ, entityplayer.rotationYaw, entityplayer.rotationPitch);
      this.updateTimeAndWeatherForPlayer(entityplayer, worldserver);
      if (!this.mcServer.getResourcePack().isEmpty()) {
         entityplayer.loadResourcePack(this.mcServer.getResourcePack(), this.mcServer.getResourcePackHash());
      }

      for(PotionEffect mobeffect : entityplayer.getActivePotionEffects()) {
         playerconnection.sendPacket(new SPacketEntityEffect(entityplayer.getEntityId(), mobeffect));
      }

      if (nbttagcompound != null) {
         if (nbttagcompound.hasKey("RootVehicle", 10)) {
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("RootVehicle");
            Entity entity = AnvilChunkLoader.readWorldEntity(nbttagcompound1.getCompoundTag("Entity"), worldserver, true);
            if (entity != null) {
               UUID uuid = nbttagcompound1.getUniqueId("Attach");
               if (entity.getUniqueID().equals(uuid)) {
                  entityplayer.startRiding(entity, true);
               } else {
                  for(Entity entity1 : entity.getRecursivePassengers()) {
                     if (entity1.getUniqueID().equals(uuid)) {
                        entityplayer.startRiding(entity1, true);
                        break;
                     }
                  }
               }

               if (!entityplayer.isRiding()) {
                  LOG.warn("Couldn't reattach entity to player");
                  worldserver.removeEntityDangerously(entity);

                  for(Entity entity1 : entity.getRecursivePassengers()) {
                     worldserver.removeEntityDangerously(entity1);
                  }
               }
            }
         } else if (nbttagcompound.hasKey("Riding", 10)) {
            Entity entity2 = AnvilChunkLoader.readWorldEntity(nbttagcompound.getCompoundTag("Riding"), worldserver, true);
            if (entity2 != null) {
               entityplayer.startRiding(entity2, true);
            }
         }
      }

      entityplayer.addSelfToInternalCraftingInventory();
      LOG.info(entityplayer.getName() + "[" + s1 + "] logged in with entity id " + entityplayer.getEntityId() + " at ([" + entityplayer.world.worldInfo.getWorldName() + "]" + entityplayer.posX + ", " + entityplayer.posY + ", " + entityplayer.posZ + ")");
   }

   public void sendScoreboard(ServerScoreboard scoreboardserver, EntityPlayerMP entityplayer) {
      HashSet hashset = Sets.newHashSet();

      for(ScorePlayerTeam scoreboardteam : scoreboardserver.getTeams()) {
         entityplayer.connection.sendPacket(new SPacketTeams(scoreboardteam, 0));
      }

      for(int i = 0; i < 19; ++i) {
         ScoreObjective scoreboardobjective = scoreboardserver.getObjectiveInDisplaySlot(i);
         if (scoreboardobjective != null && !hashset.contains(scoreboardobjective)) {
            for(Packet packet : scoreboardserver.getCreatePackets(scoreboardobjective)) {
               entityplayer.connection.sendPacket(packet);
            }

            hashset.add(scoreboardobjective);
         }
      }

   }

   public void setPlayerManager(WorldServer[] aworldserver) {
      if (this.playerNBTManagerObj == null) {
         this.playerNBTManagerObj = aworldserver[0].getSaveHandler().getPlayerNBTManager();
         aworldserver[0].getWorldBorder().addListener(new IBorderListener() {
            public void onSizeChanged(WorldBorder worldborder, double d0) {
               PlayerList.this.sendAll(new SPacketWorldBorder(worldborder, SPacketWorldBorder.Action.SET_SIZE), worldborder.world);
            }

            public void onTransitionStarted(WorldBorder worldborder, double d0, double d1, long i) {
               PlayerList.this.sendAll(new SPacketWorldBorder(worldborder, SPacketWorldBorder.Action.LERP_SIZE), worldborder.world);
            }

            public void onCenterChanged(WorldBorder worldborder, double d0, double d1) {
               PlayerList.this.sendAll(new SPacketWorldBorder(worldborder, SPacketWorldBorder.Action.SET_CENTER), worldborder.world);
            }

            public void onWarningTimeChanged(WorldBorder worldborder, int i) {
               PlayerList.this.sendAll(new SPacketWorldBorder(worldborder, SPacketWorldBorder.Action.SET_WARNING_TIME), worldborder.world);
            }

            public void onWarningDistanceChanged(WorldBorder worldborder, int i) {
               PlayerList.this.sendAll(new SPacketWorldBorder(worldborder, SPacketWorldBorder.Action.SET_WARNING_BLOCKS), worldborder.world);
            }

            public void onDamageAmountChanged(WorldBorder worldborder, double d0) {
            }

            public void onDamageBufferChanged(WorldBorder worldborder, double d0) {
            }
         });
      }
   }

   public void preparePlayer(EntityPlayerMP entityplayer, WorldServer worldserver) {
      WorldServer worldserver1 = entityplayer.getServerWorld();
      if (worldserver != null) {
         worldserver.getPlayerChunkMap().removePlayer(entityplayer);
      }

      worldserver1.getPlayerChunkMap().addPlayer(entityplayer);
      worldserver1.getChunkProvider().provideChunk((int)entityplayer.posX >> 4, (int)entityplayer.posZ >> 4);
   }

   public int getEntityViewDistance() {
      return PlayerChunkMap.getFurthestViewableBlock(this.getViewDistance());
   }

   public NBTTagCompound readPlayerDataFromFile(EntityPlayerMP entityplayer) {
      NBTTagCompound nbttagcompound = ((WorldServer)this.mcServer.worlds.get(0)).getWorldInfo().getPlayerNBTTagCompound();
      NBTTagCompound nbttagcompound1;
      if (entityplayer.getName().equals(this.mcServer.Q()) && nbttagcompound != null) {
         nbttagcompound1 = this.mcServer.getDataConverterManager().process(FixTypes.PLAYER, nbttagcompound);
         entityplayer.readFromNBT(nbttagcompound1);
         LOG.debug("loading single player");
      } else {
         nbttagcompound1 = this.playerNBTManagerObj.readPlayerData(entityplayer);
      }

      return nbttagcompound1;
   }

   protected void writePlayerData(EntityPlayerMP entityplayer) {
      this.playerNBTManagerObj.writePlayerData(entityplayer);
      StatisticsManagerServer serverstatisticmanager = (StatisticsManagerServer)this.playerStatFiles.get(entityplayer.getUniqueID());
      if (serverstatisticmanager != null) {
         serverstatisticmanager.saveStatFile();
      }

   }

   public void onPlayerJoin(EntityPlayerMP entityplayer, String joinMessage) {
      this.playerEntityList.add(entityplayer);
      this.uuidToPlayerMap.put(entityplayer.getUniqueID(), entityplayer);
      WorldServer worldserver = this.mcServer.getWorldServer(entityplayer.dimension);
      PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(this.cserver.getPlayer(entityplayer), joinMessage);
      this.cserver.getPluginManager().callEvent(playerJoinEvent);
      joinMessage = playerJoinEvent.getJoinMessage();
      if (joinMessage != null && joinMessage.length() > 0) {
         ITextComponent[] var5;
         for(ITextComponent line : var5 = CraftChatMessage.fromString(joinMessage)) {
            this.mcServer.getPlayerList().sendPacketToAllPlayers(new SPacketChat(line));
         }
      }

      ChunkIOExecutor.adjustPoolSize(this.getCurrentPlayerCount());
      SPacketPlayerListItem packet = new SPacketPlayerListItem(SPacketPlayerListItem.Action.ADD_PLAYER, new EntityPlayerMP[]{entityplayer});

      for(int i = 0; i < this.playerEntityList.size(); ++i) {
         EntityPlayerMP entityplayer1 = (EntityPlayerMP)this.playerEntityList.get(i);
         if (entityplayer1.getBukkitEntity().canSee(entityplayer.getBukkitEntity())) {
            entityplayer1.connection.sendPacket(packet);
         }

         if (entityplayer.getBukkitEntity().canSee(entityplayer1.getBukkitEntity())) {
            entityplayer.connection.sendPacket(new SPacketPlayerListItem(SPacketPlayerListItem.Action.ADD_PLAYER, new EntityPlayerMP[]{entityplayer1}));
         }
      }

      if (entityplayer.world == worldserver && !worldserver.playerEntities.contains(entityplayer)) {
         worldserver.spawnEntity(entityplayer);
         this.preparePlayer(entityplayer, (WorldServer)null);
      }

   }

   public void serverUpdateMovingPlayer(EntityPlayerMP entityplayer) {
      entityplayer.getServerWorld().getPlayerChunkMap().updateMovingPlayer(entityplayer);
   }

   public String disconnect(EntityPlayerMP entityplayer) {
      WorldServer worldserver = entityplayer.getServerWorld();
      entityplayer.addStat(StatList.LEAVE_GAME);
      CraftEventFactory.handleInventoryCloseEvent(entityplayer);
      PlayerQuitEvent playerQuitEvent = new PlayerQuitEvent(this.cserver.getPlayer(entityplayer), "§e" + entityplayer.getName() + " left the game");
      this.cserver.getPluginManager().callEvent(playerQuitEvent);
      entityplayer.getBukkitEntity().disconnect(playerQuitEvent.getQuitMessage());
      entityplayer.onUpdateEntity();
      this.writePlayerData(entityplayer);
      if (entityplayer.isRiding()) {
         Entity entity = entityplayer.getLowestRidingEntity();
         if (entity.getRecursivePassengersByType(EntityPlayerMP.class).size() == 1) {
            LOG.debug("Removing player mount");
            entityplayer.dismountRidingEntity();
            worldserver.removeEntityDangerously(entity);

            for(Entity entity1 : entity.getRecursivePassengers()) {
               worldserver.removeEntityDangerously(entity1);
            }

            worldserver.getChunkFromChunkCoords(entityplayer.chunkCoordX, entityplayer.chunkCoordZ).setChunkModified();
         }
      }

      worldserver.removeEntity(entityplayer);
      worldserver.getPlayerChunkMap().removePlayer(entityplayer);
      this.playerEntityList.remove(entityplayer);
      UUID uuid = entityplayer.getUniqueID();
      EntityPlayerMP entityplayer1 = (EntityPlayerMP)this.uuidToPlayerMap.get(uuid);
      if (entityplayer1 == entityplayer) {
         this.uuidToPlayerMap.remove(uuid);
         this.playerStatFiles.remove(uuid);
      }

      SPacketPlayerListItem packet = new SPacketPlayerListItem(SPacketPlayerListItem.Action.REMOVE_PLAYER, new EntityPlayerMP[]{entityplayer});

      for(int i = 0; i < this.playerEntityList.size(); ++i) {
         EntityPlayerMP entityplayer2 = (EntityPlayerMP)this.playerEntityList.get(i);
         if (entityplayer2.getBukkitEntity().canSee(entityplayer.getBukkitEntity())) {
            entityplayer2.connection.sendPacket(packet);
         } else {
            entityplayer2.getBukkitEntity().removeDisconnectingPlayer(entityplayer.getBukkitEntity());
         }
      }

      this.cserver.getScoreboardManager().removePlayer(entityplayer.getBukkitEntity());
      ChunkIOExecutor.adjustPoolSize(this.getCurrentPlayerCount());
      return playerQuitEvent.getQuitMessage();
   }

   public EntityPlayerMP attemptLogin(NetHandlerLoginServer loginlistener, GameProfile gameprofile, String hostname) {
      UUID uuid = EntityPlayer.getUUID(gameprofile);
      ArrayList arraylist = Lists.newArrayList();

      for(int i = 0; i < this.playerEntityList.size(); ++i) {
         EntityPlayerMP entityplayer = (EntityPlayerMP)this.playerEntityList.get(i);
         if (entityplayer.getUniqueID().equals(uuid)) {
            arraylist.add(entityplayer);
         }
      }

      for(EntityPlayerMP entityplayer : arraylist) {
         this.writePlayerData(entityplayer);
         entityplayer.connection.disconnect("You logged in from another location");
      }

      SocketAddress socketaddress = loginlistener.networkManager.getRemoteAddress();
      EntityPlayerMP entity = new EntityPlayerMP(this.mcServer, this.mcServer.getWorldServer(0), gameprofile, new PlayerInteractionManager(this.mcServer.getWorldServer(0)));
      Player player = entity.getBukkitEntity();
      PlayerLoginEvent event = new PlayerLoginEvent(player, hostname, ((InetSocketAddress)socketaddress).getAddress());
      if (this.getBannedPlayers().isBanned(gameprofile) && !((UserListBansEntry)this.getBannedPlayers().getEntry(gameprofile)).hasBanExpired()) {
         UserListBansEntry gameprofilebanentry = (UserListBansEntry)this.bannedPlayers.getEntry(gameprofile);
         String s = "You are banned from this server!\nReason: " + gameprofilebanentry.getBanReason();
         if (gameprofilebanentry.getBanEndDate() != null) {
            s = s + "\nYour ban will be removed on " + DATE_FORMAT.format(gameprofilebanentry.getBanEndDate());
         }

         event.disallow(Result.KICK_BANNED, s);
      } else if (!this.canJoin(gameprofile)) {
         event.disallow(Result.KICK_WHITELIST, "You are not white-listed on this server!");
      } else if (this.getBannedIPs().isBanned(socketaddress) && !this.getBannedIPs().getBanEntry(socketaddress).hasBanExpired()) {
         UserListIPBansEntry ipbanentry = this.bannedIPs.getBanEntry(socketaddress);
         String s = "Your IP address is banned from this server!\nReason: " + ipbanentry.getBanReason();
         if (ipbanentry.getBanEndDate() != null) {
            s = s + "\nYour ban will be removed on " + DATE_FORMAT.format(ipbanentry.getBanEndDate());
         }

         event.disallow(Result.KICK_BANNED, s);
      } else if (this.playerEntityList.size() >= this.maxPlayers && !this.bypassesPlayerLimit(gameprofile)) {
         event.disallow(Result.KICK_FULL, "The server is full");
      }

      this.cserver.getPluginManager().callEvent(event);
      if (event.getResult() != Result.ALLOWED) {
         loginlistener.closeConnection(event.getKickMessage());
         return null;
      } else {
         return entity;
      }
   }

   public EntityPlayerMP processLogin(GameProfile gameprofile, EntityPlayerMP player) {
      return player;
   }

   public EntityPlayerMP recreatePlayerEntity(EntityPlayerMP entityplayer, int i, boolean flag) {
      return this.moveToWorld(entityplayer, i, flag, (Location)null, true);
   }

   public EntityPlayerMP moveToWorld(EntityPlayerMP entityplayer, int i, boolean flag, Location location, boolean avoidSuffocation) {
      entityplayer.getServerWorld().getEntityTracker().removePlayerFromTrackers(entityplayer);
      entityplayer.getServerWorld().getPlayerChunkMap().removePlayer(entityplayer);
      this.playerEntityList.remove(entityplayer);
      this.mcServer.getWorldServer(entityplayer.dimension).removeEntityDangerously(entityplayer);
      BlockPos blockposition = entityplayer.getBedLocation();
      boolean flag1 = entityplayer.isSpawnForced();
      EntityPlayerMP entityplayer1 = entityplayer;
      World fromWorld = entityplayer.getBukkitEntity().getWorld();
      entityplayer.playerConqueredTheEnd = false;
      entityplayer.connection = entityplayer.connection;
      entityplayer.clonePlayer(entityplayer, flag);
      entityplayer.setEntityId(entityplayer.getEntityId());
      entityplayer.setCommandStats(entityplayer);
      entityplayer.setPrimaryHand(entityplayer.getPrimaryHand());

      for(String s : entityplayer.getTags()) {
         entityplayer1.addTag(s);
      }

      if (location == null) {
         boolean isBedSpawn = false;
         CraftWorld cworld = (CraftWorld)this.mcServer.server.getWorld(entityplayer.spawnWorld);
         if (cworld != null && blockposition != null) {
            BlockPos blockposition1 = EntityPlayer.getBedSpawnLocation(cworld.getHandle(), blockposition, flag1);
            if (blockposition1 != null) {
               isBedSpawn = true;
               location = new Location(cworld, (double)((float)blockposition1.getX() + 0.5F), (double)((float)blockposition1.getY() + 0.1F), (double)((float)blockposition1.getZ() + 0.5F));
            } else {
               entityplayer1.setSpawnPoint((BlockPos)null, true);
               entityplayer1.connection.sendPacket(new SPacketChangeGameState(0, 0.0F));
            }
         }

         if (location == null) {
            cworld = (CraftWorld)this.mcServer.server.getWorlds().get(0);
            blockposition = cworld.getHandle().getSpawnPoint();
            location = new Location(cworld, (double)((float)blockposition.getX() + 0.5F), (double)((float)blockposition.getY() + 0.1F), (double)((float)blockposition.getZ() + 0.5F));
         }

         Player respawnPlayer = this.cserver.getPlayer(entityplayer1);
         PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(respawnPlayer, location, isBedSpawn);
         this.cserver.getPluginManager().callEvent(respawnEvent);
         location = respawnEvent.getRespawnLocation();
         entityplayer.reset();
      } else {
         location.setWorld(this.mcServer.getWorldServer(i).getWorld());
      }

      WorldServer worldserver = ((CraftWorld)location.getWorld()).getHandle();
      entityplayer1.forceSetPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
      worldserver.getChunkProvider().provideChunk((int)entityplayer1.posX >> 4, (int)entityplayer1.posZ >> 4);

      while(avoidSuffocation && !worldserver.getCollisionBoxes(entityplayer1, entityplayer1.getEntityBoundingBox()).isEmpty() && entityplayer1.posY < 256.0D) {
         entityplayer1.setPosition(entityplayer1.posX, entityplayer1.posY + 1.0D, entityplayer1.posZ);
      }

      byte actualDimension = (byte)worldserver.getWorld().getEnvironment().getId();
      if (fromWorld.getEnvironment() == worldserver.getWorld().getEnvironment()) {
         entityplayer1.connection.sendPacket(new SPacketRespawn((byte)(actualDimension >= 0 ? -1 : 0), worldserver.getDifficulty(), worldserver.getWorldInfo().getTerrainType(), entityplayer.interactionManager.getGameType()));
      }

      entityplayer1.connection.sendPacket(new SPacketRespawn(actualDimension, worldserver.getDifficulty(), worldserver.getWorldInfo().getTerrainType(), entityplayer1.interactionManager.getGameType()));
      entityplayer1.setWorld(worldserver);
      entityplayer1.isDead = false;
      entityplayer1.connection.teleport(new Location(worldserver.getWorld(), entityplayer1.posX, entityplayer1.posY, entityplayer1.posZ, entityplayer1.rotationYaw, entityplayer1.rotationPitch));
      entityplayer1.setSneaking(false);
      BlockPos blockposition1 = worldserver.getSpawnPoint();
      entityplayer1.connection.sendPacket(new SPacketSpawnPosition(blockposition1));
      entityplayer1.connection.sendPacket(new SPacketSetExperience(entityplayer1.experience, entityplayer1.experienceTotal, entityplayer1.experienceLevel));
      this.updateTimeAndWeatherForPlayer(entityplayer1, worldserver);
      this.updatePermissionLevel(entityplayer1);
      if (!entityplayer.connection.isDisconnected()) {
         worldserver.getPlayerChunkMap().addPlayer(entityplayer1);
         worldserver.spawnEntity(entityplayer1);
         this.playerEntityList.add(entityplayer1);
         this.uuidToPlayerMap.put(entityplayer1.getUniqueID(), entityplayer1);
      }

      entityplayer1.setHealth(entityplayer1.getHealth());
      this.syncPlayerInventory(entityplayer);
      entityplayer.sendPlayerAbilities();

      for(Object o1 : entityplayer.getActivePotionEffects()) {
         PotionEffect mobEffect = (PotionEffect)o1;
         entityplayer.connection.sendPacket(new SPacketEntityEffect(entityplayer.getEntityId(), mobEffect));
      }

      if (fromWorld != location.getWorld()) {
         PlayerChangedWorldEvent event = new PlayerChangedWorldEvent(entityplayer.getBukkitEntity(), fromWorld);
         this.mcServer.server.getPluginManager().callEvent(event);
      }

      if (entityplayer.connection.isDisconnected()) {
         this.writePlayerData(entityplayer);
      }

      return entityplayer1;
   }

   public void changeDimension(EntityPlayerMP entityplayer, int i, TeleportCause cause) {
      WorldServer exitWorld = null;
      if (entityplayer.dimension < 10) {
         for(WorldServer world : this.mcServer.worlds) {
            if (world.dimension == i) {
               exitWorld = world;
            }
         }
      }

      Location enter = entityplayer.getBukkitEntity().getLocation();
      Location exit = null;
      boolean useTravelAgent = false;
      if (exitWorld != null) {
         if (cause == TeleportCause.END_PORTAL && i == 0) {
            exit = entityplayer.getBukkitEntity().getBedSpawnLocation();
            if (exit == null || ((CraftWorld)exit.getWorld()).getHandle().dimension != 0) {
               exit = exitWorld.getWorld().getSpawnLocation();
            }
         } else {
            exit = this.calculateTarget(enter, exitWorld);
            useTravelAgent = true;
         }
      }

      TravelAgent agent = exit != null ? (TravelAgent)((CraftWorld)exit.getWorld()).getHandle().getDefaultTeleporter() : CraftTravelAgent.DEFAULT;
      PlayerPortalEvent event = new PlayerPortalEvent(entityplayer.getBukkitEntity(), enter, exit, agent, cause);
      event.useTravelAgent(useTravelAgent);
      Bukkit.getServer().getPluginManager().callEvent(event);
      if (!event.isCancelled() && event.getTo() != null) {
         exit = event.useTravelAgent() ? event.getPortalTravelAgent().findOrCreate(event.getTo()) : event.getTo();
         if (exit != null) {
            exitWorld = ((CraftWorld)exit.getWorld()).getHandle();
            PlayerTeleportEvent tpEvent = new PlayerTeleportEvent(entityplayer.getBukkitEntity(), enter, exit, cause);
            Bukkit.getServer().getPluginManager().callEvent(tpEvent);
            if (!tpEvent.isCancelled() && tpEvent.getTo() != null) {
               Vector velocity = entityplayer.getBukkitEntity().getVelocity();
               exitWorld.getDefaultTeleporter().adjustExit(entityplayer, exit, velocity);
               entityplayer.invulnerableDimensionChange = true;
               this.moveToWorld(entityplayer, exitWorld.dimension, true, exit, false);
               if (entityplayer.motionX != velocity.getX() || entityplayer.motionY != velocity.getY() || entityplayer.motionZ != velocity.getZ()) {
                  entityplayer.getBukkitEntity().setVelocity(velocity);
               }

            }
         }
      }
   }

   public void updatePermissionLevel(EntityPlayerMP entityplayer) {
      GameProfile gameprofile = entityplayer.getGameProfile();
      int i = this.canSendCommands(gameprofile) ? this.ops.getPermissionLevel(gameprofile) : 0;
      i = this.mcServer.R() && this.mcServer.worldServer[0].getWorldInfo().areCommandsAllowed() ? 4 : i;
      i = this.commandsAllowedForAll ? 4 : i;
      this.sendPlayerPermissionLevel(entityplayer, i);
   }

   public void changePlayerDimension(EntityPlayerMP entityplayer, int i) {
      int j = entityplayer.dimension;
      WorldServer worldserver = this.mcServer.getWorldServer(entityplayer.dimension);
      entityplayer.dimension = i;
      WorldServer worldserver1 = this.mcServer.getWorldServer(entityplayer.dimension);
      entityplayer.connection.sendPacket(new SPacketRespawn(entityplayer.dimension, entityplayer.world.getDifficulty(), entityplayer.world.getWorldInfo().getTerrainType(), entityplayer.interactionManager.getGameType()));
      this.updatePermissionLevel(entityplayer);
      worldserver.removeEntityDangerously(entityplayer);
      entityplayer.isDead = false;
      this.transferEntityToWorld(entityplayer, j, worldserver, worldserver1);
      this.preparePlayer(entityplayer, worldserver);
      entityplayer.connection.setPlayerLocation(entityplayer.posX, entityplayer.posY, entityplayer.posZ, entityplayer.rotationYaw, entityplayer.rotationPitch);
      entityplayer.interactionManager.setWorld(worldserver1);
      entityplayer.connection.sendPacket(new SPacketPlayerAbilities(entityplayer.capabilities));
      this.updateTimeAndWeatherForPlayer(entityplayer, worldserver1);
      this.syncPlayerInventory(entityplayer);

      for(PotionEffect mobeffect : entityplayer.getActivePotionEffects()) {
         entityplayer.connection.sendPacket(new SPacketEntityEffect(entityplayer.getEntityId(), mobeffect));
      }

   }

   public void transferEntityToWorld(Entity entity, int i, WorldServer worldserver, WorldServer worldserver1) {
      Location exit = this.calculateTarget(entity.getBukkitEntity().getLocation(), worldserver1);
      this.repositionEntity(entity, exit, true);
   }

   public Location calculateTarget(Location enter, net.minecraft.world.World target) {
      WorldServer worldserver = ((CraftWorld)enter.getWorld()).getHandle();
      WorldServer worldserver1 = target.getWorld().getHandle();
      int i = worldserver.dimension;
      double y = enter.getY();
      float yaw = enter.getYaw();
      float pitch = enter.getPitch();
      double d0 = enter.getX();
      double d1 = enter.getZ();
      double d2 = 8.0D;
      if (worldserver1.dimension == -1) {
         d0 = MathHelper.clamp(d0 / d2, worldserver1.getWorldBorder().minX() + 16.0D, worldserver1.getWorldBorder().maxX() - 16.0D);
         d1 = MathHelper.clamp(d1 / d2, worldserver1.getWorldBorder().minZ() + 16.0D, worldserver1.getWorldBorder().maxZ() - 16.0D);
      } else if (worldserver1.dimension == 0) {
         d0 = MathHelper.clamp(d0 * d2, worldserver1.getWorldBorder().minX() + 16.0D, worldserver1.getWorldBorder().maxX() - 16.0D);
         d1 = MathHelper.clamp(d1 * d2, worldserver1.getWorldBorder().minZ() + 16.0D, worldserver1.getWorldBorder().maxZ() - 16.0D);
      } else {
         BlockPos blockposition;
         if (i == 1) {
            worldserver1 = (WorldServer)this.mcServer.worlds.get(0);
            blockposition = worldserver1.getSpawnPoint();
         } else {
            blockposition = worldserver1.getSpawnCoordinate();
         }

         d0 = (double)blockposition.getX();
         y = (double)blockposition.getY();
         d1 = (double)blockposition.getZ();
      }

      if (i != 1) {
         worldserver.theProfiler.startSection("placing");
         d0 = (double)MathHelper.clamp((int)d0, -29999872, 29999872);
         d1 = (double)MathHelper.clamp((int)d1, -29999872, 29999872);
      }

      return new Location(worldserver1.getWorld(), d0, y, d1, yaw, pitch);
   }

   public void repositionEntity(Entity entity, Location exit, boolean portal) {
      WorldServer worldserver = (WorldServer)entity.world;
      WorldServer worldserver1 = ((CraftWorld)exit.getWorld()).getHandle();
      int i = worldserver.dimension;
      entity.setLocationAndAngles(exit.getX(), exit.getY(), exit.getZ(), exit.getYaw(), exit.getPitch());
      if (entity.isEntityAlive()) {
         worldserver.updateEntityWithOptionalForce(entity, false);
      }

      worldserver.theProfiler.endSection();
      if (i != 1) {
         worldserver.theProfiler.startSection("placing");
         if (entity.isEntityAlive()) {
            if (portal) {
               Vector velocity = entity.getBukkitEntity().getVelocity();
               worldserver1.getDefaultTeleporter().adjustExit(entity, exit, velocity);
               entity.setLocationAndAngles(exit.getX(), exit.getY(), exit.getZ(), exit.getYaw(), exit.getPitch());
               if (entity.motionX != velocity.getX() || entity.motionY != velocity.getY() || entity.motionZ != velocity.getZ()) {
                  entity.getBukkitEntity().setVelocity(velocity);
               }
            }

            worldserver1.updateEntityWithOptionalForce(entity, false);
         }

         worldserver.theProfiler.endSection();
      }

      entity.setWorld(worldserver1);
   }

   public void onTick() {
      if (++this.playerPingIndex > 600) {
         this.sendPacketToAllPlayers(new SPacketPlayerListItem(SPacketPlayerListItem.Action.UPDATE_LATENCY, this.playerEntityList));
         this.playerPingIndex = 0;
      }

   }

   public void sendPacketToAllPlayers(Packet packet) {
      for(int i = 0; i < this.playerEntityList.size(); ++i) {
         ((EntityPlayerMP)this.playerEntityList.get(i)).connection.sendPacket(packet);
      }

   }

   public void sendAll(Packet packet, EntityPlayer entityhuman) {
      for(int i = 0; i < this.playerEntityList.size(); ++i) {
         EntityPlayerMP entityplayer = (EntityPlayerMP)this.playerEntityList.get(i);
         if (entityhuman == null || !(entityhuman instanceof EntityPlayerMP) || entityplayer.getBukkitEntity().canSee(((EntityPlayerMP)entityhuman).getBukkitEntity())) {
            ((EntityPlayerMP)this.playerEntityList.get(i)).connection.sendPacket(packet);
         }
      }

   }

   public void sendAll(Packet packet, net.minecraft.world.World world) {
      for(int i = 0; i < world.playerEntities.size(); ++i) {
         ((EntityPlayerMP)world.playerEntities.get(i)).connection.sendPacket(packet);
      }

   }

   public void sendPacketToAllPlayersInDimension(Packet packet, int i) {
      for(int j = 0; j < this.playerEntityList.size(); ++j) {
         EntityPlayerMP entityplayer = (EntityPlayerMP)this.playerEntityList.get(j);
         if (entityplayer.dimension == i) {
            entityplayer.connection.sendPacket(packet);
         }
      }

   }

   public void sendMessageToAllTeamMembers(EntityPlayer entityhuman, ITextComponent ichatbasecomponent) {
      Team scoreboardteambase = entityhuman.getTeam();
      if (scoreboardteambase != null) {
         for(String s : scoreboardteambase.getMembershipCollection()) {
            EntityPlayerMP entityplayer = this.getPlayerByUsername(s);
            if (entityplayer != null && entityplayer != entityhuman) {
               entityplayer.sendMessage(ichatbasecomponent);
            }
         }
      }

   }

   public void sendMessageToTeamOrAllPlayers(EntityPlayer entityhuman, ITextComponent ichatbasecomponent) {
      Team scoreboardteambase = entityhuman.getTeam();
      if (scoreboardteambase == null) {
         this.sendChatMsg(ichatbasecomponent);
      } else {
         for(int i = 0; i < this.playerEntityList.size(); ++i) {
            EntityPlayerMP entityplayer = (EntityPlayerMP)this.playerEntityList.get(i);
            if (entityplayer.getTeam() != scoreboardteambase) {
               entityplayer.sendMessage(ichatbasecomponent);
            }
         }
      }

   }

   public String getFormattedListOfPlayers(boolean flag) {
      String s = "";
      ArrayList arraylist = Lists.newArrayList(this.playerEntityList);

      for(int i = 0; i < arraylist.size(); ++i) {
         if (i > 0) {
            s = s + ", ";
         }

         s = s + ((EntityPlayerMP)arraylist.get(i)).getName();
         if (flag) {
            s = s + " (" + ((EntityPlayerMP)arraylist.get(i)).getCachedUniqueIdString() + ")";
         }
      }

      return s;
   }

   public String[] getOnlinePlayerNames() {
      String[] astring = new String[this.playerEntityList.size()];

      for(int i = 0; i < this.playerEntityList.size(); ++i) {
         astring[i] = ((EntityPlayerMP)this.playerEntityList.get(i)).getName();
      }

      return astring;
   }

   public GameProfile[] getOnlinePlayerProfiles() {
      GameProfile[] agameprofile = new GameProfile[this.playerEntityList.size()];

      for(int i = 0; i < this.playerEntityList.size(); ++i) {
         agameprofile[i] = ((EntityPlayerMP)this.playerEntityList.get(i)).getGameProfile();
      }

      return agameprofile;
   }

   public UserListBans getBannedPlayers() {
      return this.bannedPlayers;
   }

   public UserListIPBans getBannedIPs() {
      return this.bannedIPs;
   }

   public void addOp(GameProfile gameprofile) {
      int i = this.mcServer.q();
      this.ops.addEntry(new UserListOpsEntry(gameprofile, this.mcServer.q(), this.ops.bypassesPlayerLimit(gameprofile)));
      this.sendPlayerPermissionLevel(this.getPlayerByUUID(gameprofile.getId()), i);
      Player player = this.mcServer.server.getPlayer(gameprofile.getId());
      if (player != null) {
         player.recalculatePermissions();
      }

   }

   public void removeOp(GameProfile gameprofile) {
      this.ops.removeEntry(gameprofile);
      this.sendPlayerPermissionLevel(this.getPlayerByUUID(gameprofile.getId()), 0);
      Player player = this.mcServer.server.getPlayer(gameprofile.getId());
      if (player != null) {
         player.recalculatePermissions();
      }

   }

   private void sendPlayerPermissionLevel(EntityPlayerMP entityplayer, int i) {
      if (entityplayer != null && entityplayer.connection != null) {
         byte b0;
         if (i <= 0) {
            b0 = 24;
         } else if (i >= 4) {
            b0 = 28;
         } else {
            b0 = (byte)(24 + i);
         }

         entityplayer.connection.sendPacket(new SPacketEntityStatus(entityplayer, b0));
      }

   }

   public boolean canJoin(GameProfile gameprofile) {
      return !this.whiteListEnforced || this.ops.hasEntry(gameprofile) || this.whiteListedPlayers.hasEntry(gameprofile);
   }

   public boolean canSendCommands(GameProfile gameprofile) {
      return this.ops.hasEntry(gameprofile) || this.mcServer.R() && ((WorldServer)this.mcServer.worlds.get(0)).getWorldInfo().areCommandsAllowed() && this.mcServer.Q().equalsIgnoreCase(gameprofile.getName()) || this.commandsAllowedForAll;
   }

   @Nullable
   public EntityPlayerMP getPlayerByUsername(String s) {
      for(EntityPlayerMP entityplayer : this.playerEntityList) {
         if (entityplayer.getName().equalsIgnoreCase(s)) {
            return entityplayer;
         }
      }

      return null;
   }

   public void sendToAllNearExcept(@Nullable EntityPlayer entityhuman, double d0, double d1, double d2, double d3, int i, Packet packet) {
      for(int j = 0; j < this.playerEntityList.size(); ++j) {
         EntityPlayerMP entityplayer = (EntityPlayerMP)this.playerEntityList.get(j);
         if ((entityhuman == null || !(entityhuman instanceof EntityPlayerMP) || entityplayer.getBukkitEntity().canSee(((EntityPlayerMP)entityhuman).getBukkitEntity())) && entityplayer != entityhuman && entityplayer.dimension == i) {
            double d4 = d0 - entityplayer.posX;
            double d5 = d1 - entityplayer.posY;
            double d6 = d2 - entityplayer.posZ;
            if (d4 * d4 + d5 * d5 + d6 * d6 < d3 * d3) {
               entityplayer.connection.sendPacket(packet);
            }
         }
      }

   }

   public void saveAllPlayerData() {
      for(int i = 0; i < this.playerEntityList.size(); ++i) {
         this.writePlayerData((EntityPlayerMP)this.playerEntityList.get(i));
      }

   }

   public void addWhitelistedPlayer(GameProfile gameprofile) {
      this.whiteListedPlayers.addEntry(new UserListWhitelistEntry(gameprofile));
   }

   public void removePlayerFromWhitelist(GameProfile gameprofile) {
      this.whiteListedPlayers.removeEntry(gameprofile);
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

   public void updateTimeAndWeatherForPlayer(EntityPlayerMP entityplayer, WorldServer worldserver) {
      WorldBorder worldborder = entityplayer.world.getWorldBorder();
      entityplayer.connection.sendPacket(new SPacketWorldBorder(worldborder, SPacketWorldBorder.Action.INITIALIZE));
      entityplayer.connection.sendPacket(new SPacketTimeUpdate(worldserver.getTotalWorldTime(), worldserver.getWorldTime(), worldserver.getGameRules().getBoolean("doDaylightCycle")));
      if (worldserver.isRaining()) {
         entityplayer.setPlayerWeather(WeatherType.DOWNFALL, false);
         entityplayer.updateWeather(-worldserver.rainingStrength, worldserver.rainingStrength, -worldserver.thunderingStrength, worldserver.thunderingStrength);
      }

   }

   public void syncPlayerInventory(EntityPlayerMP entityplayer) {
      entityplayer.sendContainerToPlayer(entityplayer.inventoryContainer);
      entityplayer.getBukkitEntity().updateScaledHealth();
      entityplayer.connection.sendPacket(new SPacketHeldItemChange(entityplayer.inventory.currentItem));
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

   public void setWhiteListEnabled(boolean flag) {
      this.whiteListEnforced = flag;
   }

   public List getPlayersMatchingAddress(String s) {
      ArrayList arraylist = Lists.newArrayList();

      for(EntityPlayerMP entityplayer : this.playerEntityList) {
         if (entityplayer.getPlayerIP().equals(s)) {
            arraylist.add(entityplayer);
         }
      }

      return arraylist;
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

   private void setPlayerGameTypeBasedOnOther(EntityPlayerMP entityplayer, EntityPlayerMP entityplayer1, net.minecraft.world.World world) {
      if (entityplayer1 != null) {
         entityplayer.interactionManager.setGameType(entityplayer1.interactionManager.getGameType());
      } else if (this.gameType != null) {
         entityplayer.interactionManager.setGameType(this.gameType);
      }

      entityplayer.interactionManager.initializeGameType(world.getWorldInfo().getGameType());
   }

   public void removeAllPlayers() {
      for(EntityPlayerMP player : this.playerEntityList) {
         player.connection.disconnect(this.mcServer.server.getShutdownMessage());
      }

   }

   public void sendMessage(ITextComponent[] iChatBaseComponents) {
      for(ITextComponent component : iChatBaseComponents) {
         this.sendChatMsgImpl(component, true);
      }

   }

   public void sendChatMsgImpl(ITextComponent ichatbasecomponent, boolean flag) {
      this.mcServer.sendMessage(ichatbasecomponent);
      int i = flag ? 1 : 0;
      this.sendPacketToAllPlayers(new SPacketChat(CraftChatMessage.fixComponent(ichatbasecomponent), (byte)i));
   }

   public void sendChatMsg(ITextComponent ichatbasecomponent) {
      this.sendChatMsgImpl(ichatbasecomponent, true);
   }

   public StatisticsManagerServer getPlayerStatsFile(EntityPlayer entityhuman) {
      UUID uuid = entityhuman.getUniqueID();
      StatisticsManagerServer serverstatisticmanager = uuid == null ? null : (StatisticsManagerServer)this.playerStatFiles.get(uuid);
      if (serverstatisticmanager == null) {
         File file = new File(this.mcServer.getWorldServer(0).getSaveHandler().getWorldDirectory(), "stats");
         File file1 = new File(file, uuid + ".json");
         if (!file1.exists()) {
            File file2 = new File(file, entityhuman.getName() + ".json");
            if (file2.exists() && file2.isFile()) {
               file2.renameTo(file1);
            }
         }

         serverstatisticmanager = new StatisticsManagerServer(this.mcServer, file1);
         serverstatisticmanager.readStatFile();
         this.playerStatFiles.put(uuid, serverstatisticmanager);
      }

      return serverstatisticmanager;
   }

   public void setViewDistance(int i) {
      this.viewDistance = i;
      if (this.mcServer.worldServer != null) {
         WorldServer[] aworldserver = this.mcServer.worldServer;
         int var10000 = aworldserver.length;

         for(int k = 0; k < this.mcServer.worlds.size(); ++k) {
            WorldServer worldserver = (WorldServer)this.mcServer.worlds.get(0);
            if (worldserver != null) {
               worldserver.getPlayerChunkMap().setPlayerViewRadius(i);
               worldserver.getEntityTracker().setViewDistance(i);
            }
         }
      }

   }

   public List getPlayers() {
      return this.playerEntityList;
   }

   public EntityPlayerMP getPlayerByUUID(UUID uuid) {
      return (EntityPlayerMP)this.uuidToPlayerMap.get(uuid);
   }

   public boolean bypassesPlayerLimit(GameProfile gameprofile) {
      return false;
   }
}
