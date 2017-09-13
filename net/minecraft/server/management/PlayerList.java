package net.minecraft.server.management;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.StatList;
import net.minecraft.stats.StatisticsManagerServer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.border.IBorderListener;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.demo.DemoWorldManager;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.chunkio.ChunkIOExecutor;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class PlayerList {
   public static final File FILE_PLAYERBANS = new File("banned-players.json");
   public static final File FILE_IPBANS = new File("banned-ips.json");
   public static final File FILE_OPS = new File("ops.json");
   public static final File FILE_WHITELIST = new File("whitelist.json");
   private static final Logger LOG = LogManager.getLogger();
   private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
   private final MinecraftServer mcServer;
   private final List playerEntityList = Lists.newArrayList();
   private final Map uuidToPlayerMap = Maps.newHashMap();
   private final UserListBans bannedPlayers;
   private final UserListIPBans bannedIPs;
   private final UserListOps ops;
   private final UserListWhitelist whiteListedPlayers;
   private final Map playerStatFiles;
   private IPlayerFileData playerNBTManagerObj;
   private boolean whiteListEnforced;
   protected int maxPlayers;
   private int viewDistance;
   private GameType gameType;
   private boolean commandsAllowedForAll;
   private int playerPingIndex;

   public PlayerList(MinecraftServer var1) {
      this.bannedPlayers = new UserListBans(FILE_PLAYERBANS);
      this.bannedIPs = new UserListIPBans(FILE_IPBANS);
      this.ops = new UserListOps(FILE_OPS);
      this.whiteListedPlayers = new UserListWhitelist(FILE_WHITELIST);
      this.playerStatFiles = Maps.newHashMap();
      this.mcServer = server;
      this.bannedPlayers.setLanServer(false);
      this.bannedIPs.setLanServer(false);
      this.maxPlayers = 8;
   }

   public void initializeConnectionToPlayer(NetworkManager var1, EntityPlayerMP var2, NetHandlerPlayServer var3) {
      GameProfile gameprofile = playerIn.getGameProfile();
      PlayerProfileCache playerprofilecache = this.mcServer.getPlayerProfileCache();
      GameProfile gameprofile1 = playerprofilecache.getProfileByUUID(gameprofile.getId());
      String s = gameprofile1 == null ? gameprofile.getName() : gameprofile1.getName();
      playerprofilecache.addEntry(gameprofile);
      NBTTagCompound nbttagcompound = this.readPlayerDataFromFile(playerIn);
      playerIn.setWorld(this.mcServer.worldServerForDimension(playerIn.dimension));
      World playerWorld = this.mcServer.worldServerForDimension(playerIn.dimension);
      if (playerWorld == null) {
         playerIn.dimension = 0;
         playerWorld = this.mcServer.worldServerForDimension(0);
         BlockPos spawnPoint = playerWorld.provider.getRandomizedSpawnPoint();
         playerIn.setPosition((double)spawnPoint.getX(), (double)spawnPoint.getY(), (double)spawnPoint.getZ());
      }

      playerIn.setWorld(playerWorld);
      playerIn.interactionManager.setWorld((WorldServer)playerIn.world);
      String s1 = "local";
      if (netManager.getRemoteAddress() != null) {
         s1 = netManager.getRemoteAddress().toString();
      }

      LOG.info("{}[{}] logged in with entity id {} at ({}, {}, {})", new Object[]{playerIn.getName(), s1, playerIn.getEntityId(), playerIn.posX, playerIn.posY, playerIn.posZ});
      WorldServer worldserver = this.mcServer.worldServerForDimension(playerIn.dimension);
      WorldInfo worldinfo = worldserver.getWorldInfo();
      BlockPos blockpos = worldserver.getSpawnPoint();
      this.setPlayerGameTypeBasedOnOther(playerIn, (EntityPlayerMP)null, worldserver);
      playerIn.connection = nethandlerplayserver;
      nethandlerplayserver.sendPacket(new SPacketJoinGame(playerIn.getEntityId(), playerIn.interactionManager.getGameType(), worldinfo.isHardcoreModeEnabled(), worldserver.provider.getDimension(), worldserver.getDifficulty(), this.getMaxPlayers(), worldinfo.getTerrainType(), worldserver.getGameRules().getBoolean("reducedDebugInfo")));
      nethandlerplayserver.sendPacket(new SPacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString(this.getServerInstance().getServerModName())));
      nethandlerplayserver.sendPacket(new SPacketServerDifficulty(worldinfo.getDifficulty(), worldinfo.isDifficultyLocked()));
      nethandlerplayserver.sendPacket(new SPacketSpawnPosition(blockpos));
      nethandlerplayserver.sendPacket(new SPacketPlayerAbilities(playerIn.capabilities));
      nethandlerplayserver.sendPacket(new SPacketHeldItemChange(playerIn.inventory.currentItem));
      this.updatePermissionLevel(playerIn);
      playerIn.getStatFile().markAllDirty();
      playerIn.getStatFile().sendAchievements(playerIn);
      this.sendScoreboard((ServerScoreboard)worldserver.getScoreboard(), playerIn);
      this.mcServer.refreshStatusNextTick();
      TextComponentTranslation textcomponenttranslation;
      if (playerIn.getName().equalsIgnoreCase(s)) {
         textcomponenttranslation = new TextComponentTranslation("multiplayer.player.joined", new Object[]{playerIn.getDisplayName()});
      } else {
         textcomponenttranslation = new TextComponentTranslation("multiplayer.player.joined.renamed", new Object[]{playerIn.getDisplayName(), s});
      }

      textcomponenttranslation.getStyle().setColor(TextFormatting.YELLOW);
      this.sendChatMsg(textcomponenttranslation);
      this.playerLoggedIn(playerIn);
      nethandlerplayserver.setPlayerLocation(playerIn.posX, playerIn.posY, playerIn.posZ, playerIn.rotationYaw, playerIn.rotationPitch);
      this.updateTimeAndWeatherForPlayer(playerIn, worldserver);
      if (!this.mcServer.getResourcePackUrl().isEmpty()) {
         playerIn.loadResourcePack(this.mcServer.getResourcePackUrl(), this.mcServer.getResourcePackHash());
      }

      for(PotionEffect potioneffect : playerIn.getActivePotionEffects()) {
         nethandlerplayserver.sendPacket(new SPacketEntityEffect(playerIn.getEntityId(), potioneffect));
      }

      if (nbttagcompound != null) {
         if (nbttagcompound.hasKey("RootVehicle", 10)) {
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("RootVehicle");
            Entity entity2 = AnvilChunkLoader.readWorldEntity(nbttagcompound1.getCompoundTag("Entity"), worldserver, true);
            if (entity2 != null) {
               UUID uuid = nbttagcompound1.getUniqueId("Attach");
               if (entity2.getUniqueID().equals(uuid)) {
                  playerIn.startRiding(entity2, true);
               } else {
                  for(Entity entity : entity2.getRecursivePassengers()) {
                     if (entity.getUniqueID().equals(uuid)) {
                        playerIn.startRiding(entity, true);
                        break;
                     }
                  }
               }

               if (!playerIn.isRiding()) {
                  LOG.warn("Couldn't reattach entity to player");
                  worldserver.removeEntityDangerously(entity2);

                  for(Entity entity3 : entity2.getRecursivePassengers()) {
                     worldserver.removeEntityDangerously(entity3);
                  }
               }
            }
         } else if (nbttagcompound.hasKey("Riding", 10)) {
            Entity entity1 = AnvilChunkLoader.readWorldEntity(nbttagcompound.getCompoundTag("Riding"), worldserver, true);
            if (entity1 != null) {
               playerIn.startRiding(entity1, true);
            }
         }
      }

      playerIn.addSelfToInternalCraftingInventory();
      FMLCommonHandler.instance().firePlayerLoggedIn(playerIn);
   }

   protected void sendScoreboard(ServerScoreboard var1, EntityPlayerMP var2) {
      Set set = Sets.newHashSet();

      for(ScorePlayerTeam scoreplayerteam : scoreboardIn.getTeams()) {
         playerIn.connection.sendPacket(new SPacketTeams(scoreplayerteam, 0));
      }

      for(int i = 0; i < 19; ++i) {
         ScoreObjective scoreobjective = scoreboardIn.getObjectiveInDisplaySlot(i);
         if (scoreobjective != null && !set.contains(scoreobjective)) {
            for(Packet packet : scoreboardIn.getCreatePackets(scoreobjective)) {
               playerIn.connection.sendPacket(packet);
            }

            set.add(scoreobjective);
         }
      }

   }

   public void setPlayerManager(WorldServer[] var1) {
      this.playerNBTManagerObj = worldServers[0].getSaveHandler().getPlayerNBTManager();
      worldServers[0].getWorldBorder().addListener(new IBorderListener() {
         public void onSizeChanged(WorldBorder var1, double var2) {
            PlayerList.this.sendPacketToAllPlayers(new SPacketWorldBorder(border, SPacketWorldBorder.Action.SET_SIZE));
         }

         public void onTransitionStarted(WorldBorder var1, double var2, double var4, long var6) {
            PlayerList.this.sendPacketToAllPlayers(new SPacketWorldBorder(border, SPacketWorldBorder.Action.LERP_SIZE));
         }

         public void onCenterChanged(WorldBorder var1, double var2, double var4) {
            PlayerList.this.sendPacketToAllPlayers(new SPacketWorldBorder(border, SPacketWorldBorder.Action.SET_CENTER));
         }

         public void onWarningTimeChanged(WorldBorder var1, int var2) {
            PlayerList.this.sendPacketToAllPlayers(new SPacketWorldBorder(border, SPacketWorldBorder.Action.SET_WARNING_TIME));
         }

         public void onWarningDistanceChanged(WorldBorder var1, int var2) {
            PlayerList.this.sendPacketToAllPlayers(new SPacketWorldBorder(border, SPacketWorldBorder.Action.SET_WARNING_BLOCKS));
         }

         public void onDamageAmountChanged(WorldBorder var1, double var2) {
         }

         public void onDamageBufferChanged(WorldBorder var1, double var2) {
         }
      });
   }

   public void preparePlayer(EntityPlayerMP var1, WorldServer var2) {
      WorldServer worldserver = playerIn.getServerWorld();
      if (worldIn != null) {
         worldIn.getPlayerChunkMap().removePlayer(playerIn);
      }

      worldserver.getPlayerChunkMap().addPlayer(playerIn);
      worldserver.getChunkProvider().provideChunk((int)playerIn.posX >> 4, (int)playerIn.posZ >> 4);
   }

   public int getEntityViewDistance() {
      return PlayerChunkMap.getFurthestViewableBlock(this.getViewDistance());
   }

   public NBTTagCompound readPlayerDataFromFile(EntityPlayerMP var1) {
      NBTTagCompound nbttagcompound = this.mcServer.worlds[0].getWorldInfo().getPlayerNBTTagCompound();
      NBTTagCompound nbttagcompound1;
      if (playerIn.getName().equals(this.mcServer.getServerOwner()) && nbttagcompound != null) {
         nbttagcompound1 = this.mcServer.getDataFixer().process(FixTypes.PLAYER, nbttagcompound);
         playerIn.readFromNBT(nbttagcompound1);
         LOG.debug("loading single player");
         ForgeEventFactory.firePlayerLoadingEvent(playerIn, this.playerNBTManagerObj, playerIn.getUniqueID().toString());
      } else {
         nbttagcompound1 = this.playerNBTManagerObj.readPlayerData(playerIn);
      }

      return nbttagcompound1;
   }

   public NBTTagCompound getPlayerNBT(EntityPlayerMP var1) {
      NBTTagCompound nbttagcompound = this.mcServer.worlds[0].getWorldInfo().getPlayerNBTTagCompound();
      return player.getName().equals(this.mcServer.getServerOwner()) && nbttagcompound != null ? nbttagcompound : ((SaveHandler)this.playerNBTManagerObj).getPlayerNBT(player);
   }

   protected void writePlayerData(EntityPlayerMP var1) {
      if (playerIn.connection != null) {
         this.playerNBTManagerObj.writePlayerData(playerIn);
         StatisticsManagerServer statisticsmanagerserver = (StatisticsManagerServer)this.playerStatFiles.get(playerIn.getUniqueID());
         if (statisticsmanagerserver != null) {
            statisticsmanagerserver.saveStatFile();
         }

      }
   }

   public void playerLoggedIn(EntityPlayerMP var1) {
      this.playerEntityList.add(playerIn);
      this.uuidToPlayerMap.put(playerIn.getUniqueID(), playerIn);
      this.sendPacketToAllPlayers(new SPacketPlayerListItem(SPacketPlayerListItem.Action.ADD_PLAYER, new EntityPlayerMP[]{playerIn}));
      WorldServer worldserver = this.mcServer.worldServerForDimension(playerIn.dimension);

      for(int i = 0; i < this.playerEntityList.size(); ++i) {
         playerIn.connection.sendPacket(new SPacketPlayerListItem(SPacketPlayerListItem.Action.ADD_PLAYER, new EntityPlayerMP[]{(EntityPlayerMP)this.playerEntityList.get(i)}));
      }

      ChunkIOExecutor.adjustPoolSize(this.getCurrentPlayerCount());
      worldserver.spawnEntity(playerIn);
      this.preparePlayer(playerIn, (WorldServer)null);
   }

   public void serverUpdateMovingPlayer(EntityPlayerMP var1) {
      playerIn.getServerWorld().getPlayerChunkMap().updateMovingPlayer(playerIn);
   }

   public void playerLoggedOut(EntityPlayerMP var1) {
      FMLCommonHandler.instance().firePlayerLoggedOut(playerIn);
      WorldServer worldserver = playerIn.getServerWorld();
      playerIn.addStat(StatList.LEAVE_GAME);
      this.writePlayerData(playerIn);
      if (playerIn.isRiding()) {
         Entity entity = playerIn.getLowestRidingEntity();
         if (entity.getRecursivePassengersByType(EntityPlayerMP.class).size() == 1) {
            LOG.debug("Removing player mount");
            playerIn.dismountRidingEntity();
            worldserver.removeEntityDangerously(entity);

            for(Entity entity1 : entity.getRecursivePassengers()) {
               worldserver.removeEntityDangerously(entity1);
            }

            worldserver.getChunkFromChunkCoords(playerIn.chunkCoordX, playerIn.chunkCoordZ).setChunkModified();
         }
      }

      worldserver.removeEntity(playerIn);
      worldserver.getPlayerChunkMap().removePlayer(playerIn);
      this.playerEntityList.remove(playerIn);
      UUID uuid = playerIn.getUniqueID();
      EntityPlayerMP entityplayermp = (EntityPlayerMP)this.uuidToPlayerMap.get(uuid);
      if (entityplayermp == playerIn) {
         this.uuidToPlayerMap.remove(uuid);
         this.playerStatFiles.remove(uuid);
      }

      ChunkIOExecutor.adjustPoolSize(this.getCurrentPlayerCount());
      this.sendPacketToAllPlayers(new SPacketPlayerListItem(SPacketPlayerListItem.Action.REMOVE_PLAYER, new EntityPlayerMP[]{playerIn}));
   }

   public String allowUserToConnect(SocketAddress var1, GameProfile var2) {
      if (this.bannedPlayers.isBanned(profile)) {
         UserListBansEntry userlistbansentry = (UserListBansEntry)this.bannedPlayers.getEntry(profile);
         String s1 = "You are banned from this server!\nReason: " + userlistbansentry.getBanReason();
         if (userlistbansentry.getBanEndDate() != null) {
            s1 = s1 + "\nYour ban will be removed on " + DATE_FORMAT.format(userlistbansentry.getBanEndDate());
         }

         return s1;
      } else if (!this.canJoin(profile)) {
         return "You are not white-listed on this server!";
      } else if (this.bannedIPs.isBanned(address)) {
         UserListIPBansEntry userlistipbansentry = this.bannedIPs.getBanEntry(address);
         String s = "Your IP address is banned from this server!\nReason: " + userlistipbansentry.getBanReason();
         if (userlistipbansentry.getBanEndDate() != null) {
            s = s + "\nYour ban will be removed on " + DATE_FORMAT.format(userlistipbansentry.getBanEndDate());
         }

         return s;
      } else {
         return this.playerEntityList.size() >= this.maxPlayers && !this.bypassesPlayerLimit(profile) ? "The server is full!" : null;
      }
   }

   public EntityPlayerMP createPlayerForUser(GameProfile var1) {
      UUID uuid = EntityPlayer.getUUID(profile);
      List list = Lists.newArrayList();

      for(int i = 0; i < this.playerEntityList.size(); ++i) {
         EntityPlayerMP entityplayermp = (EntityPlayerMP)this.playerEntityList.get(i);
         if (entityplayermp.getUniqueID().equals(uuid)) {
            list.add(entityplayermp);
         }
      }

      EntityPlayerMP entityplayermp2 = (EntityPlayerMP)this.uuidToPlayerMap.get(profile.getId());
      if (entityplayermp2 != null && !list.contains(entityplayermp2)) {
         list.add(entityplayermp2);
      }

      for(EntityPlayerMP entityplayermp1 : list) {
         entityplayermp1.connection.disconnect("You logged in from another location");
      }

      Object playerinteractionmanager;
      if (this.mcServer.isDemo()) {
         playerinteractionmanager = new DemoWorldManager(this.mcServer.worldServerForDimension(0));
      } else {
         playerinteractionmanager = new PlayerInteractionManager(this.mcServer.worldServerForDimension(0));
      }

      return new EntityPlayerMP(this.mcServer, this.mcServer.worldServerForDimension(0), profile, playerinteractionmanager);
   }

   public EntityPlayerMP recreatePlayerEntity(EntityPlayerMP var1, int var2, boolean var3) {
      World world = this.mcServer.worldServerForDimension(dimension);
      if (world == null) {
         dimension = 0;
      } else if (!world.provider.canRespawnHere()) {
         dimension = world.provider.getRespawnDimension(playerIn);
      }

      playerIn.getServerWorld().getEntityTracker().removePlayerFromTrackers(playerIn);
      playerIn.getServerWorld().getEntityTracker().untrack(playerIn);
      playerIn.getServerWorld().getPlayerChunkMap().removePlayer(playerIn);
      this.playerEntityList.remove(playerIn);
      this.mcServer.worldServerForDimension(playerIn.dimension).removeEntityDangerously(playerIn);
      BlockPos blockpos = playerIn.getBedLocation(dimension);
      boolean flag = playerIn.isSpawnForced(dimension);
      playerIn.dimension = dimension;
      Object playerinteractionmanager;
      if (this.mcServer.isDemo()) {
         playerinteractionmanager = new DemoWorldManager(this.mcServer.worldServerForDimension(playerIn.dimension));
      } else {
         playerinteractionmanager = new PlayerInteractionManager(this.mcServer.worldServerForDimension(playerIn.dimension));
      }

      EntityPlayerMP entityplayermp = new EntityPlayerMP(this.mcServer, this.mcServer.worldServerForDimension(playerIn.dimension), playerIn.getGameProfile(), playerinteractionmanager);
      entityplayermp.connection = playerIn.connection;
      entityplayermp.clonePlayer(playerIn, conqueredEnd);
      entityplayermp.dimension = dimension;
      entityplayermp.setEntityId(playerIn.getEntityId());
      entityplayermp.setCommandStats(playerIn);
      entityplayermp.setPrimaryHand(playerIn.getPrimaryHand());

      for(String s : playerIn.getTags()) {
         entityplayermp.addTag(s);
      }

      WorldServer worldserver = this.mcServer.worldServerForDimension(playerIn.dimension);
      this.setPlayerGameTypeBasedOnOther(entityplayermp, playerIn, worldserver);
      if (blockpos != null) {
         BlockPos blockpos1 = EntityPlayer.getBedSpawnLocation(this.mcServer.worldServerForDimension(playerIn.dimension), blockpos, flag);
         if (blockpos1 != null) {
            entityplayermp.setLocationAndAngles((double)((float)blockpos1.getX() + 0.5F), (double)((float)blockpos1.getY() + 0.1F), (double)((float)blockpos1.getZ() + 0.5F), 0.0F, 0.0F);
            entityplayermp.setSpawnPoint(blockpos, flag);
         } else {
            entityplayermp.connection.sendPacket(new SPacketChangeGameState(0, 0.0F));
         }
      }

      worldserver.getChunkProvider().provideChunk((int)entityplayermp.posX >> 4, (int)entityplayermp.posZ >> 4);

      while(!worldserver.getCollisionBoxes(entityplayermp, entityplayermp.getEntityBoundingBox()).isEmpty() && entityplayermp.posY < 256.0D) {
         entityplayermp.setPosition(entityplayermp.posX, entityplayermp.posY + 1.0D, entityplayermp.posZ);
      }

      entityplayermp.connection.sendPacket(new SPacketRespawn(entityplayermp.dimension, entityplayermp.world.getDifficulty(), entityplayermp.world.getWorldInfo().getTerrainType(), entityplayermp.interactionManager.getGameType()));
      BlockPos blockpos2 = worldserver.getSpawnPoint();
      entityplayermp.connection.setPlayerLocation(entityplayermp.posX, entityplayermp.posY, entityplayermp.posZ, entityplayermp.rotationYaw, entityplayermp.rotationPitch);
      entityplayermp.connection.sendPacket(new SPacketSpawnPosition(blockpos2));
      entityplayermp.connection.sendPacket(new SPacketSetExperience(entityplayermp.experience, entityplayermp.experienceTotal, entityplayermp.experienceLevel));
      this.updateTimeAndWeatherForPlayer(entityplayermp, worldserver);
      this.updatePermissionLevel(entityplayermp);
      worldserver.getPlayerChunkMap().addPlayer(entityplayermp);
      worldserver.spawnEntity(entityplayermp);
      this.playerEntityList.add(entityplayermp);
      this.uuidToPlayerMap.put(entityplayermp.getUniqueID(), entityplayermp);
      entityplayermp.addSelfToInternalCraftingInventory();
      entityplayermp.setHealth(entityplayermp.getHealth());
      FMLCommonHandler.instance().firePlayerRespawnEvent(entityplayermp);
      return entityplayermp;
   }

   public void updatePermissionLevel(EntityPlayerMP var1) {
      GameProfile gameprofile = player.getGameProfile();
      int i = this.canSendCommands(gameprofile) ? this.ops.getPermissionLevel(gameprofile) : 0;
      i = this.mcServer.isSinglePlayer() && this.mcServer.worlds[0].getWorldInfo().areCommandsAllowed() ? 4 : i;
      i = this.commandsAllowedForAll ? 4 : i;
      this.sendPlayerPermissionLevel(player, i);
   }

   public void changePlayerDimension(EntityPlayerMP var1, int var2) {
      this.transferPlayerToDimension(player, dimensionIn, this.mcServer.worldServerForDimension(dimensionIn).getDefaultTeleporter());
   }

   public void transferPlayerToDimension(EntityPlayerMP var1, int var2, Teleporter var3) {
      int i = player.dimension;
      WorldServer worldserver = this.mcServer.worldServerForDimension(player.dimension);
      player.dimension = dimensionIn;
      WorldServer worldserver1 = this.mcServer.worldServerForDimension(player.dimension);
      player.connection.sendPacket(new SPacketRespawn(player.dimension, worldserver1.getDifficulty(), worldserver1.getWorldInfo().getTerrainType(), player.interactionManager.getGameType()));
      this.updatePermissionLevel(player);
      worldserver.removeEntityDangerously(player);
      player.isDead = false;
      this.transferEntityToWorld(player, i, worldserver, worldserver1, teleporter);
      this.preparePlayer(player, worldserver);
      player.connection.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
      player.interactionManager.setWorld(worldserver1);
      player.connection.sendPacket(new SPacketPlayerAbilities(player.capabilities));
      this.updateTimeAndWeatherForPlayer(player, worldserver1);
      this.syncPlayerInventory(player);

      for(PotionEffect potioneffect : player.getActivePotionEffects()) {
         player.connection.sendPacket(new SPacketEntityEffect(player.getEntityId(), potioneffect));
      }

      FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, i, dimensionIn);
   }

   public void transferEntityToWorld(Entity var1, int var2, WorldServer var3, WorldServer var4) {
      this.transferEntityToWorld(entityIn, lastDimension, oldWorldIn, toWorldIn, toWorldIn.getDefaultTeleporter());
   }

   public void transferEntityToWorld(Entity var1, int var2, WorldServer var3, WorldServer var4, Teleporter var5) {
      WorldProvider pOld = oldWorldIn.provider;
      WorldProvider pNew = toWorldIn.provider;
      double moveFactor = pOld.getMovementFactor() / pNew.getMovementFactor();
      double d0 = entityIn.posX * moveFactor;
      double d1 = entityIn.posZ * moveFactor;
      double d2 = 8.0D;
      float f = entityIn.rotationYaw;
      oldWorldIn.theProfiler.startSection("moving");
      if (entityIn.dimension == 1) {
         BlockPos blockpos;
         if (lastDimension == 1) {
            blockpos = toWorldIn.getSpawnPoint();
         } else {
            blockpos = toWorldIn.getSpawnCoordinate();
         }

         d0 = (double)blockpos.getX();
         entityIn.posY = (double)blockpos.getY();
         d1 = (double)blockpos.getZ();
         entityIn.setLocationAndAngles(d0, entityIn.posY, d1, 90.0F, 0.0F);
         if (entityIn.isEntityAlive()) {
            oldWorldIn.updateEntityWithOptionalForce(entityIn, false);
         }
      }

      oldWorldIn.theProfiler.endSection();
      if (lastDimension != 1) {
         oldWorldIn.theProfiler.startSection("placing");
         d0 = (double)MathHelper.clamp((int)d0, -29999872, 29999872);
         d1 = (double)MathHelper.clamp((int)d1, -29999872, 29999872);
         if (entityIn.isEntityAlive()) {
            entityIn.setLocationAndAngles(d0, entityIn.posY, d1, entityIn.rotationYaw, entityIn.rotationPitch);
            teleporter.placeInPortal(entityIn, f);
            toWorldIn.spawnEntity(entityIn);
            toWorldIn.updateEntityWithOptionalForce(entityIn, false);
         }

         oldWorldIn.theProfiler.endSection();
      }

      entityIn.setWorld(toWorldIn);
   }

   public void onTick() {
      if (++this.playerPingIndex > 600) {
         this.sendPacketToAllPlayers(new SPacketPlayerListItem(SPacketPlayerListItem.Action.UPDATE_LATENCY, this.playerEntityList));
         this.playerPingIndex = 0;
      }

   }

   public void sendPacketToAllPlayers(Packet var1) {
      for(int i = 0; i < this.playerEntityList.size(); ++i) {
         ((EntityPlayerMP)this.playerEntityList.get(i)).connection.sendPacket(packetIn);
      }

   }

   public void sendPacketToAllPlayersInDimension(Packet var1, int var2) {
      for(int i = 0; i < this.playerEntityList.size(); ++i) {
         EntityPlayerMP entityplayermp = (EntityPlayerMP)this.playerEntityList.get(i);
         if (entityplayermp.dimension == dimension) {
            entityplayermp.connection.sendPacket(packetIn);
         }
      }

   }

   public void sendMessageToAllTeamMembers(EntityPlayer var1, ITextComponent var2) {
      Team team = player.getTeam();
      if (team != null) {
         for(String s : team.getMembershipCollection()) {
            EntityPlayerMP entityplayermp = this.getPlayerByUsername(s);
            if (entityplayermp != null && entityplayermp != player) {
               entityplayermp.sendMessage(message);
            }
         }
      }

   }

   public void sendMessageToTeamOrAllPlayers(EntityPlayer var1, ITextComponent var2) {
      Team team = player.getTeam();
      if (team == null) {
         this.sendChatMsg(message);
      } else {
         for(int i = 0; i < this.playerEntityList.size(); ++i) {
            EntityPlayerMP entityplayermp = (EntityPlayerMP)this.playerEntityList.get(i);
            if (entityplayermp.getTeam() != team) {
               entityplayermp.sendMessage(message);
            }
         }
      }

   }

   public String getFormattedListOfPlayers(boolean var1) {
      String s = "";
      List list = Lists.newArrayList(this.playerEntityList);

      for(int i = 0; i < ((List)list).size(); ++i) {
         if (i > 0) {
            s = s + ", ";
         }

         s = s + ((EntityPlayerMP)list.get(i)).getName();
         if (includeUUIDs) {
            s = s + " (" + ((EntityPlayerMP)list.get(i)).getCachedUniqueIdString() + ")";
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

   public void addOp(GameProfile var1) {
      int i = this.mcServer.getOpPermissionLevel();
      this.ops.addEntry(new UserListOpsEntry(profile, this.mcServer.getOpPermissionLevel(), this.ops.bypassesPlayerLimit(profile)));
      this.sendPlayerPermissionLevel(this.getPlayerByUUID(profile.getId()), i);
   }

   public void removeOp(GameProfile var1) {
      this.ops.removeEntry(profile);
      this.sendPlayerPermissionLevel(this.getPlayerByUUID(profile.getId()), 0);
   }

   private void sendPlayerPermissionLevel(EntityPlayerMP var1, int var2) {
      if (player != null && player.connection != null) {
         byte b0;
         if (permLevel <= 0) {
            b0 = 24;
         } else if (permLevel >= 4) {
            b0 = 28;
         } else {
            b0 = (byte)(24 + permLevel);
         }

         player.connection.sendPacket(new SPacketEntityStatus(player, b0));
      }

   }

   public boolean canJoin(GameProfile var1) {
      return !this.whiteListEnforced || this.ops.hasEntry(profile) || this.whiteListedPlayers.hasEntry(profile);
   }

   public boolean canSendCommands(GameProfile var1) {
      return this.ops.hasEntry(profile) || this.mcServer.isSinglePlayer() && this.mcServer.worlds[0].getWorldInfo().areCommandsAllowed() && this.mcServer.getServerOwner().equalsIgnoreCase(profile.getName()) || this.commandsAllowedForAll;
   }

   @Nullable
   public EntityPlayerMP getPlayerByUsername(String var1) {
      for(EntityPlayerMP entityplayermp : this.playerEntityList) {
         if (entityplayermp.getName().equalsIgnoreCase(username)) {
            return entityplayermp;
         }
      }

      return null;
   }

   public void sendToAllNearExcept(@Nullable EntityPlayer var1, double var2, double var4, double var6, double var8, int var10, Packet var11) {
      for(int i = 0; i < this.playerEntityList.size(); ++i) {
         EntityPlayerMP entityplayermp = (EntityPlayerMP)this.playerEntityList.get(i);
         if (entityplayermp != except && entityplayermp.dimension == dimension) {
            double d0 = x - entityplayermp.posX;
            double d1 = y - entityplayermp.posY;
            double d2 = z - entityplayermp.posZ;
            if (d0 * d0 + d1 * d1 + d2 * d2 < radius * radius) {
               entityplayermp.connection.sendPacket(packetIn);
            }
         }
      }

   }

   public void saveAllPlayerData() {
      for(int i = 0; i < this.playerEntityList.size(); ++i) {
         this.writePlayerData((EntityPlayerMP)this.playerEntityList.get(i));
      }

   }

   public void addWhitelistedPlayer(GameProfile var1) {
      this.whiteListedPlayers.addEntry(new UserListWhitelistEntry(profile));
   }

   public void removePlayerFromWhitelist(GameProfile var1) {
      this.whiteListedPlayers.removeEntry(profile);
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
      WorldBorder worldborder = this.mcServer.worlds[0].getWorldBorder();
      playerIn.connection.sendPacket(new SPacketWorldBorder(worldborder, SPacketWorldBorder.Action.INITIALIZE));
      playerIn.connection.sendPacket(new SPacketTimeUpdate(worldIn.getTotalWorldTime(), worldIn.getWorldTime(), worldIn.getGameRules().getBoolean("doDaylightCycle")));
      if (worldIn.isRaining()) {
         playerIn.connection.sendPacket(new SPacketChangeGameState(1, 0.0F));
         playerIn.connection.sendPacket(new SPacketChangeGameState(7, worldIn.getRainStrength(1.0F)));
         playerIn.connection.sendPacket(new SPacketChangeGameState(8, worldIn.getThunderStrength(1.0F)));
      }

   }

   public void syncPlayerInventory(EntityPlayerMP var1) {
      playerIn.sendContainerToPlayer(playerIn.inventoryContainer);
      playerIn.setPlayerHealthUpdated();
      playerIn.connection.sendPacket(new SPacketHeldItemChange(playerIn.inventory.currentItem));
   }

   public int getCurrentPlayerCount() {
      return this.playerEntityList.size();
   }

   public int getMaxPlayers() {
      return this.maxPlayers;
   }

   public String[] getAvailablePlayerDat() {
      return this.mcServer.worlds[0].getSaveHandler().getPlayerNBTManager().getAvailablePlayerDat();
   }

   public void setWhiteListEnabled(boolean var1) {
      this.whiteListEnforced = whitelistEnabled;
   }

   public List getPlayersMatchingAddress(String var1) {
      List list = Lists.newArrayList();

      for(EntityPlayerMP entityplayermp : this.playerEntityList) {
         if (entityplayermp.getPlayerIP().equals(address)) {
            list.add(entityplayermp);
         }
      }

      return list;
   }

   public int getViewDistance() {
      return this.viewDistance;
   }

   public MinecraftServer getServerInstance() {
      return this.mcServer;
   }

   public NBTTagCompound getHostPlayerData() {
      return null;
   }

   @SideOnly(Side.CLIENT)
   public void setGameType(GameType var1) {
      this.gameType = gameModeIn;
   }

   private void setPlayerGameTypeBasedOnOther(EntityPlayerMP var1, EntityPlayerMP var2, World var3) {
      if (source != null) {
         target.interactionManager.setGameType(source.interactionManager.getGameType());
      } else if (this.gameType != null) {
         target.interactionManager.setGameType(this.gameType);
      }

      target.interactionManager.initializeGameType(worldIn.getWorldInfo().getGameType());
   }

   @SideOnly(Side.CLIENT)
   public void setCommandsAllowedForAll(boolean var1) {
      this.commandsAllowedForAll = p_72387_1_;
   }

   public void removeAllPlayers() {
      for(int i = 0; i < this.playerEntityList.size(); ++i) {
         ((EntityPlayerMP)this.playerEntityList.get(i)).connection.disconnect("Server closed");
      }

   }

   public void sendChatMsgImpl(ITextComponent var1, boolean var2) {
      this.mcServer.sendMessage(component);
      byte b0 = (byte)(isSystem ? 1 : 0);
      this.sendPacketToAllPlayers(new SPacketChat(component, b0));
   }

   public void sendChatMsg(ITextComponent var1) {
      this.sendChatMsgImpl(component, true);
   }

   public StatisticsManagerServer getPlayerStatsFile(EntityPlayer var1) {
      UUID uuid = playerIn.getUniqueID();
      StatisticsManagerServer statisticsmanagerserver = uuid == null ? null : (StatisticsManagerServer)this.playerStatFiles.get(uuid);
      if (statisticsmanagerserver == null) {
         File file1 = new File(this.mcServer.worldServerForDimension(0).getSaveHandler().getWorldDirectory(), "stats");
         File file2 = new File(file1, uuid + ".json");
         if (!file2.exists()) {
            File file3 = new File(file1, playerIn.getName() + ".json");
            if (file3.exists() && file3.isFile()) {
               file3.renameTo(file2);
            }
         }

         statisticsmanagerserver = new StatisticsManagerServer(this.mcServer, file2);
         statisticsmanagerserver.readStatFile();
         this.playerStatFiles.put(uuid, statisticsmanagerserver);
      }

      return statisticsmanagerserver;
   }

   public void setViewDistance(int var1) {
      this.viewDistance = distance;
      if (this.mcServer.worlds != null) {
         for(WorldServer worldserver : this.mcServer.worlds) {
            if (worldserver != null) {
               worldserver.getPlayerChunkMap().setPlayerViewRadius(distance);
               worldserver.getEntityTracker().setViewDistance(distance);
            }
         }
      }

   }

   public List getPlayers() {
      return this.playerEntityList;
   }

   public EntityPlayerMP getPlayerByUUID(UUID var1) {
      return (EntityPlayerMP)this.uuidToPlayerMap.get(playerUUID);
   }

   public boolean bypassesPlayerLimit(GameProfile var1) {
      return false;
   }

   @SideOnly(Side.SERVER)
   public boolean isWhiteListEnabled() {
      return this.whiteListEnforced;
   }
}
