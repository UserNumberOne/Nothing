package net.minecraft.server.management;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
      this.mcServer = var1;
      this.bannedPlayers.setLanServer(false);
      this.bannedIPs.setLanServer(false);
      this.maxPlayers = 8;
   }

   public void initializeConnectionToPlayer(NetworkManager var1, EntityPlayerMP var2, NetHandlerPlayServer var3) {
      GameProfile var4 = var2.getGameProfile();
      PlayerProfileCache var5 = this.mcServer.getPlayerProfileCache();
      GameProfile var6 = var5.getProfileByUUID(var4.getId());
      String var7 = var6 == null ? var4.getName() : var6.getName();
      var5.addEntry(var4);
      NBTTagCompound var8 = this.readPlayerDataFromFile(var2);
      var2.setWorld(this.mcServer.worldServerForDimension(var2.dimension));
      WorldServer var9 = this.mcServer.worldServerForDimension(var2.dimension);
      if (var9 == null) {
         var2.dimension = 0;
         var9 = this.mcServer.worldServerForDimension(0);
         BlockPos var10 = var9.provider.getRandomizedSpawnPoint();
         var2.setPosition((double)var10.getX(), (double)var10.getY(), (double)var10.getZ());
      }

      var2.setWorld(var9);
      var2.interactionManager.setWorld((WorldServer)var2.world);
      String var20 = "local";
      if (var1.getRemoteAddress() != null) {
         var20 = var1.getRemoteAddress().toString();
      }

      LOG.info("{}[{}] logged in with entity id {} at ({}, {}, {})", new Object[]{var2.getName(), var20, var2.getEntityId(), var2.posX, var2.posY, var2.posZ});
      WorldServer var11 = this.mcServer.worldServerForDimension(var2.dimension);
      WorldInfo var12 = var11.getWorldInfo();
      BlockPos var13 = var11.getSpawnPoint();
      this.setPlayerGameTypeBasedOnOther(var2, (EntityPlayerMP)null, var11);
      var2.connection = var3;
      var3.sendPacket(new SPacketJoinGame(var2.getEntityId(), var2.interactionManager.getGameType(), var12.isHardcoreModeEnabled(), var11.provider.getDimension(), var11.getDifficulty(), this.getMaxPlayers(), var12.getTerrainType(), var11.getGameRules().getBoolean("reducedDebugInfo")));
      var3.sendPacket(new SPacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString(this.getServerInstance().getServerModName())));
      var3.sendPacket(new SPacketServerDifficulty(var12.getDifficulty(), var12.isDifficultyLocked()));
      var3.sendPacket(new SPacketSpawnPosition(var13));
      var3.sendPacket(new SPacketPlayerAbilities(var2.capabilities));
      var3.sendPacket(new SPacketHeldItemChange(var2.inventory.currentItem));
      this.updatePermissionLevel(var2);
      var2.getStatFile().markAllDirty();
      var2.getStatFile().sendAchievements(var2);
      this.sendScoreboard((ServerScoreboard)var11.getScoreboard(), var2);
      this.mcServer.refreshStatusNextTick();
      TextComponentTranslation var14;
      if (var2.getName().equalsIgnoreCase(var7)) {
         var14 = new TextComponentTranslation("multiplayer.player.joined", new Object[]{var2.getDisplayName()});
      } else {
         var14 = new TextComponentTranslation("multiplayer.player.joined.renamed", new Object[]{var2.getDisplayName(), var7});
      }

      var14.getStyle().setColor(TextFormatting.YELLOW);
      this.sendChatMsg(var14);
      this.playerLoggedIn(var2);
      var3.setPlayerLocation(var2.posX, var2.posY, var2.posZ, var2.rotationYaw, var2.rotationPitch);
      this.updateTimeAndWeatherForPlayer(var2, var11);
      if (!this.mcServer.getResourcePackUrl().isEmpty()) {
         var2.loadResourcePack(this.mcServer.getResourcePackUrl(), this.mcServer.getResourcePackHash());
      }

      for(PotionEffect var16 : var2.getActivePotionEffects()) {
         var3.sendPacket(new SPacketEntityEffect(var2.getEntityId(), var16));
      }

      if (var8 != null) {
         if (var8.hasKey("RootVehicle", 10)) {
            NBTTagCompound var21 = var8.getCompoundTag("RootVehicle");
            Entity var23 = AnvilChunkLoader.readWorldEntity(var21.getCompoundTag("Entity"), var11, true);
            if (var23 != null) {
               UUID var17 = var21.getUniqueId("Attach");
               if (var23.getUniqueID().equals(var17)) {
                  var2.startRiding(var23, true);
               } else {
                  for(Entity var19 : var23.getRecursivePassengers()) {
                     if (var19.getUniqueID().equals(var17)) {
                        var2.startRiding(var19, true);
                        break;
                     }
                  }
               }

               if (!var2.isRiding()) {
                  LOG.warn("Couldn't reattach entity to player");
                  var11.removeEntityDangerously(var23);

                  for(Entity var25 : var23.getRecursivePassengers()) {
                     var11.removeEntityDangerously(var25);
                  }
               }
            }
         } else if (var8.hasKey("Riding", 10)) {
            Entity var22 = AnvilChunkLoader.readWorldEntity(var8.getCompoundTag("Riding"), var11, true);
            if (var22 != null) {
               var2.startRiding(var22, true);
            }
         }
      }

      var2.addSelfToInternalCraftingInventory();
      FMLCommonHandler.instance().firePlayerLoggedIn(var2);
   }

   protected void sendScoreboard(ServerScoreboard var1, EntityPlayerMP var2) {
      HashSet var3 = Sets.newHashSet();

      for(ScorePlayerTeam var5 : var1.getTeams()) {
         var2.connection.sendPacket(new SPacketTeams(var5, 0));
      }

      for(int var8 = 0; var8 < 19; ++var8) {
         ScoreObjective var9 = var1.getObjectiveInDisplaySlot(var8);
         if (var9 != null && !var3.contains(var9)) {
            for(Packet var7 : var1.getCreatePackets(var9)) {
               var2.connection.sendPacket(var7);
            }

            var3.add(var9);
         }
      }

   }

   public void setPlayerManager(WorldServer[] var1) {
      this.playerNBTManagerObj = var1[0].getSaveHandler().getPlayerNBTManager();
      var1[0].getWorldBorder().addListener(new IBorderListener() {
         public void onSizeChanged(WorldBorder var1, double var2) {
            PlayerList.this.sendPacketToAllPlayers(new SPacketWorldBorder(var1, SPacketWorldBorder.Action.SET_SIZE));
         }

         public void onTransitionStarted(WorldBorder var1, double var2, double var4, long var6) {
            PlayerList.this.sendPacketToAllPlayers(new SPacketWorldBorder(var1, SPacketWorldBorder.Action.LERP_SIZE));
         }

         public void onCenterChanged(WorldBorder var1, double var2, double var4) {
            PlayerList.this.sendPacketToAllPlayers(new SPacketWorldBorder(var1, SPacketWorldBorder.Action.SET_CENTER));
         }

         public void onWarningTimeChanged(WorldBorder var1, int var2) {
            PlayerList.this.sendPacketToAllPlayers(new SPacketWorldBorder(var1, SPacketWorldBorder.Action.SET_WARNING_TIME));
         }

         public void onWarningDistanceChanged(WorldBorder var1, int var2) {
            PlayerList.this.sendPacketToAllPlayers(new SPacketWorldBorder(var1, SPacketWorldBorder.Action.SET_WARNING_BLOCKS));
         }

         public void onDamageAmountChanged(WorldBorder var1, double var2) {
         }

         public void onDamageBufferChanged(WorldBorder var1, double var2) {
         }
      });
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
      NBTTagCompound var2 = this.mcServer.worlds[0].getWorldInfo().getPlayerNBTTagCompound();
      NBTTagCompound var3;
      if (var1.getName().equals(this.mcServer.getServerOwner()) && var2 != null) {
         var3 = this.mcServer.getDataFixer().process(FixTypes.PLAYER, var2);
         var1.readFromNBT(var3);
         LOG.debug("loading single player");
         ForgeEventFactory.firePlayerLoadingEvent(var1, this.playerNBTManagerObj, var1.getUniqueID().toString());
      } else {
         var3 = this.playerNBTManagerObj.readPlayerData(var1);
      }

      return var3;
   }

   public NBTTagCompound getPlayerNBT(EntityPlayerMP var1) {
      NBTTagCompound var2 = this.mcServer.worlds[0].getWorldInfo().getPlayerNBTTagCompound();
      return var1.getName().equals(this.mcServer.getServerOwner()) && var2 != null ? var2 : ((SaveHandler)this.playerNBTManagerObj).getPlayerNBT(var1);
   }

   protected void writePlayerData(EntityPlayerMP var1) {
      if (var1.connection != null) {
         this.playerNBTManagerObj.writePlayerData(var1);
         StatisticsManagerServer var2 = (StatisticsManagerServer)this.playerStatFiles.get(var1.getUniqueID());
         if (var2 != null) {
            var2.saveStatFile();
         }

      }
   }

   public void playerLoggedIn(EntityPlayerMP var1) {
      this.playerEntityList.add(var1);
      this.uuidToPlayerMap.put(var1.getUniqueID(), var1);
      this.sendPacketToAllPlayers(new SPacketPlayerListItem(SPacketPlayerListItem.Action.ADD_PLAYER, new EntityPlayerMP[]{var1}));
      WorldServer var2 = this.mcServer.worldServerForDimension(var1.dimension);

      for(int var3 = 0; var3 < this.playerEntityList.size(); ++var3) {
         var1.connection.sendPacket(new SPacketPlayerListItem(SPacketPlayerListItem.Action.ADD_PLAYER, new EntityPlayerMP[]{(EntityPlayerMP)this.playerEntityList.get(var3)}));
      }

      ChunkIOExecutor.adjustPoolSize(this.getCurrentPlayerCount());
      var2.spawnEntity(var1);
      this.preparePlayer(var1, (WorldServer)null);
   }

   public void serverUpdateMovingPlayer(EntityPlayerMP var1) {
      var1.getServerWorld().getPlayerChunkMap().updateMovingPlayer(var1);
   }

   public void playerLoggedOut(EntityPlayerMP var1) {
      FMLCommonHandler.instance().firePlayerLoggedOut(var1);
      WorldServer var2 = var1.getServerWorld();
      var1.addStat(StatList.LEAVE_GAME);
      this.writePlayerData(var1);
      if (var1.isRiding()) {
         Entity var3 = var1.getLowestRidingEntity();
         if (var3.getRecursivePassengersByType(EntityPlayerMP.class).size() == 1) {
            LOG.debug("Removing player mount");
            var1.dismountRidingEntity();
            var2.removeEntityDangerously(var3);

            for(Entity var5 : var3.getRecursivePassengers()) {
               var2.removeEntityDangerously(var5);
            }

            var2.getChunkFromChunkCoords(var1.chunkCoordX, var1.chunkCoordZ).setChunkModified();
         }
      }

      var2.removeEntity(var1);
      var2.getPlayerChunkMap().removePlayer(var1);
      this.playerEntityList.remove(var1);
      UUID var6 = var1.getUniqueID();
      EntityPlayerMP var7 = (EntityPlayerMP)this.uuidToPlayerMap.get(var6);
      if (var7 == var1) {
         this.uuidToPlayerMap.remove(var6);
         this.playerStatFiles.remove(var6);
      }

      ChunkIOExecutor.adjustPoolSize(this.getCurrentPlayerCount());
      this.sendPacketToAllPlayers(new SPacketPlayerListItem(SPacketPlayerListItem.Action.REMOVE_PLAYER, new EntityPlayerMP[]{var1}));
   }

   public String allowUserToConnect(SocketAddress var1, GameProfile var2) {
      if (this.bannedPlayers.isBanned(var2)) {
         UserListBansEntry var5 = (UserListBansEntry)this.bannedPlayers.getEntry(var2);
         String var6 = "You are banned from this server!\nReason: " + var5.getBanReason();
         if (var5.getBanEndDate() != null) {
            var6 = var6 + "\nYour ban will be removed on " + DATE_FORMAT.format(var5.getBanEndDate());
         }

         return var6;
      } else if (!this.canJoin(var2)) {
         return "You are not white-listed on this server!";
      } else if (this.bannedIPs.isBanned(var1)) {
         UserListIPBansEntry var3 = this.bannedIPs.getBanEntry(var1);
         String var4 = "Your IP address is banned from this server!\nReason: " + var3.getBanReason();
         if (var3.getBanEndDate() != null) {
            var4 = var4 + "\nYour ban will be removed on " + DATE_FORMAT.format(var3.getBanEndDate());
         }

         return var4;
      } else {
         return this.playerEntityList.size() >= this.maxPlayers && !this.bypassesPlayerLimit(var2) ? "The server is full!" : null;
      }
   }

   public EntityPlayerMP createPlayerForUser(GameProfile var1) {
      UUID var2 = EntityPlayer.getUUID(var1);
      ArrayList var3 = Lists.newArrayList();

      for(int var4 = 0; var4 < this.playerEntityList.size(); ++var4) {
         EntityPlayerMP var5 = (EntityPlayerMP)this.playerEntityList.get(var4);
         if (var5.getUniqueID().equals(var2)) {
            var3.add(var5);
         }
      }

      EntityPlayerMP var7 = (EntityPlayerMP)this.uuidToPlayerMap.get(var1.getId());
      if (var7 != null && !var3.contains(var7)) {
         var3.add(var7);
      }

      for(EntityPlayerMP var6 : var3) {
         var6.connection.disconnect("You logged in from another location");
      }

      Object var9;
      if (this.mcServer.isDemo()) {
         var9 = new DemoWorldManager(this.mcServer.worldServerForDimension(0));
      } else {
         var9 = new PlayerInteractionManager(this.mcServer.worldServerForDimension(0));
      }

      return new EntityPlayerMP(this.mcServer, this.mcServer.worldServerForDimension(0), var1, (PlayerInteractionManager)var9);
   }

   public EntityPlayerMP recreatePlayerEntity(EntityPlayerMP var1, int var2, boolean var3) {
      WorldServer var4 = this.mcServer.worldServerForDimension(var2);
      if (var4 == null) {
         var2 = 0;
      } else if (!var4.provider.canRespawnHere()) {
         var2 = var4.provider.getRespawnDimension(var1);
      }

      var1.getServerWorld().getEntityTracker().removePlayerFromTrackers(var1);
      var1.getServerWorld().getEntityTracker().untrack(var1);
      var1.getServerWorld().getPlayerChunkMap().removePlayer(var1);
      this.playerEntityList.remove(var1);
      this.mcServer.worldServerForDimension(var1.dimension).removeEntityDangerously(var1);
      BlockPos var5 = var1.getBedLocation(var2);
      boolean var6 = var1.isSpawnForced(var2);
      var1.dimension = var2;
      Object var7;
      if (this.mcServer.isDemo()) {
         var7 = new DemoWorldManager(this.mcServer.worldServerForDimension(var1.dimension));
      } else {
         var7 = new PlayerInteractionManager(this.mcServer.worldServerForDimension(var1.dimension));
      }

      EntityPlayerMP var8 = new EntityPlayerMP(this.mcServer, this.mcServer.worldServerForDimension(var1.dimension), var1.getGameProfile(), (PlayerInteractionManager)var7);
      var8.connection = var1.connection;
      var8.clonePlayer(var1, var3);
      var8.dimension = var2;
      var8.setEntityId(var1.getEntityId());
      var8.setCommandStats(var1);
      var8.setPrimaryHand(var1.getPrimaryHand());

      for(String var10 : var1.getTags()) {
         var8.addTag(var10);
      }

      WorldServer var11 = this.mcServer.worldServerForDimension(var1.dimension);
      this.setPlayerGameTypeBasedOnOther(var8, var1, var11);
      if (var5 != null) {
         BlockPos var12 = EntityPlayer.getBedSpawnLocation(this.mcServer.worldServerForDimension(var1.dimension), var5, var6);
         if (var12 != null) {
            var8.setLocationAndAngles((double)((float)var12.getX() + 0.5F), (double)((float)var12.getY() + 0.1F), (double)((float)var12.getZ() + 0.5F), 0.0F, 0.0F);
            var8.setSpawnPoint(var5, var6);
         } else {
            var8.connection.sendPacket(new SPacketChangeGameState(0, 0.0F));
         }
      }

      var11.getChunkProvider().provideChunk((int)var8.posX >> 4, (int)var8.posZ >> 4);

      while(!var11.getCollisionBoxes(var8, var8.getEntityBoundingBox()).isEmpty() && var8.posY < 256.0D) {
         var8.setPosition(var8.posX, var8.posY + 1.0D, var8.posZ);
      }

      var8.connection.sendPacket(new SPacketRespawn(var8.dimension, var8.world.getDifficulty(), var8.world.getWorldInfo().getTerrainType(), var8.interactionManager.getGameType()));
      BlockPos var13 = var11.getSpawnPoint();
      var8.connection.setPlayerLocation(var8.posX, var8.posY, var8.posZ, var8.rotationYaw, var8.rotationPitch);
      var8.connection.sendPacket(new SPacketSpawnPosition(var13));
      var8.connection.sendPacket(new SPacketSetExperience(var8.experience, var8.experienceTotal, var8.experienceLevel));
      this.updateTimeAndWeatherForPlayer(var8, var11);
      this.updatePermissionLevel(var8);
      var11.getPlayerChunkMap().addPlayer(var8);
      var11.spawnEntity(var8);
      this.playerEntityList.add(var8);
      this.uuidToPlayerMap.put(var8.getUniqueID(), var8);
      var8.addSelfToInternalCraftingInventory();
      var8.setHealth(var8.getHealth());
      FMLCommonHandler.instance().firePlayerRespawnEvent(var8);
      return var8;
   }

   public void updatePermissionLevel(EntityPlayerMP var1) {
      GameProfile var2 = var1.getGameProfile();
      int var3 = this.canSendCommands(var2) ? this.ops.getPermissionLevel(var2) : 0;
      var3 = this.mcServer.isSinglePlayer() && this.mcServer.worlds[0].getWorldInfo().areCommandsAllowed() ? 4 : var3;
      var3 = this.commandsAllowedForAll ? 4 : var3;
      this.sendPlayerPermissionLevel(var1, var3);
   }

   public void changePlayerDimension(EntityPlayerMP var1, int var2) {
      this.transferPlayerToDimension(var1, var2, this.mcServer.worldServerForDimension(var2).getDefaultTeleporter());
   }

   public void transferPlayerToDimension(EntityPlayerMP var1, int var2, Teleporter var3) {
      int var4 = var1.dimension;
      WorldServer var5 = this.mcServer.worldServerForDimension(var1.dimension);
      var1.dimension = var2;
      WorldServer var6 = this.mcServer.worldServerForDimension(var1.dimension);
      var1.connection.sendPacket(new SPacketRespawn(var1.dimension, var6.getDifficulty(), var6.getWorldInfo().getTerrainType(), var1.interactionManager.getGameType()));
      this.updatePermissionLevel(var1);
      var5.removeEntityDangerously(var1);
      var1.isDead = false;
      this.transferEntityToWorld(var1, var4, var5, var6, var3);
      this.preparePlayer(var1, var5);
      var1.connection.setPlayerLocation(var1.posX, var1.posY, var1.posZ, var1.rotationYaw, var1.rotationPitch);
      var1.interactionManager.setWorld(var6);
      var1.connection.sendPacket(new SPacketPlayerAbilities(var1.capabilities));
      this.updateTimeAndWeatherForPlayer(var1, var6);
      this.syncPlayerInventory(var1);

      for(PotionEffect var8 : var1.getActivePotionEffects()) {
         var1.connection.sendPacket(new SPacketEntityEffect(var1.getEntityId(), var8));
      }

      FMLCommonHandler.instance().firePlayerChangedDimensionEvent(var1, var4, var2);
   }

   public void transferEntityToWorld(Entity var1, int var2, WorldServer var3, WorldServer var4) {
      this.transferEntityToWorld(var1, var2, var3, var4, var4.getDefaultTeleporter());
   }

   public void transferEntityToWorld(Entity var1, int var2, WorldServer var3, WorldServer var4, Teleporter var5) {
      WorldProvider var6 = var3.provider;
      WorldProvider var7 = var4.provider;
      double var8 = var6.getMovementFactor() / var7.getMovementFactor();
      double var10 = var1.posX * var8;
      double var12 = var1.posZ * var8;
      double var14 = 8.0D;
      float var16 = var1.rotationYaw;
      var3.theProfiler.startSection("moving");
      if (var1.dimension == 1) {
         BlockPos var17;
         if (var2 == 1) {
            var17 = var4.getSpawnPoint();
         } else {
            var17 = var4.getSpawnCoordinate();
         }

         var10 = (double)var17.getX();
         var1.posY = (double)var17.getY();
         var12 = (double)var17.getZ();
         var1.setLocationAndAngles(var10, var1.posY, var12, 90.0F, 0.0F);
         if (var1.isEntityAlive()) {
            var3.updateEntityWithOptionalForce(var1, false);
         }
      }

      var3.theProfiler.endSection();
      if (var2 != 1) {
         var3.theProfiler.startSection("placing");
         var10 = (double)MathHelper.clamp((int)var10, -29999872, 29999872);
         var12 = (double)MathHelper.clamp((int)var12, -29999872, 29999872);
         if (var1.isEntityAlive()) {
            var1.setLocationAndAngles(var10, var1.posY, var12, var1.rotationYaw, var1.rotationPitch);
            var5.placeInPortal(var1, var16);
            var4.spawnEntity(var1);
            var4.updateEntityWithOptionalForce(var1, false);
         }

         var3.theProfiler.endSection();
      }

      var1.setWorld(var4);
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
         for(String var5 : var3.getMembershipCollection()) {
            EntityPlayerMP var6 = this.getPlayerByUsername(var5);
            if (var6 != null && var6 != var1) {
               var6.sendMessage(var2);
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
      int var2 = this.mcServer.getOpPermissionLevel();
      this.ops.addEntry(new UserListOpsEntry(var1, this.mcServer.getOpPermissionLevel(), this.ops.bypassesPlayerLimit(var1)));
      this.sendPlayerPermissionLevel(this.getPlayerByUUID(var1.getId()), var2);
   }

   public void removeOp(GameProfile var1) {
      this.ops.removeEntry(var1);
      this.sendPlayerPermissionLevel(this.getPlayerByUUID(var1.getId()), 0);
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
      return this.ops.hasEntry(var1) || this.mcServer.isSinglePlayer() && this.mcServer.worlds[0].getWorldInfo().areCommandsAllowed() && this.mcServer.getServerOwner().equalsIgnoreCase(var1.getName()) || this.commandsAllowedForAll;
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
         if (var13 != var1 && var13.dimension == var10) {
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
      WorldBorder var3 = this.mcServer.worlds[0].getWorldBorder();
      var1.connection.sendPacket(new SPacketWorldBorder(var3, SPacketWorldBorder.Action.INITIALIZE));
      var1.connection.sendPacket(new SPacketTimeUpdate(var2.getTotalWorldTime(), var2.getWorldTime(), var2.getGameRules().getBoolean("doDaylightCycle")));
      if (var2.isRaining()) {
         var1.connection.sendPacket(new SPacketChangeGameState(1, 0.0F));
         var1.connection.sendPacket(new SPacketChangeGameState(7, var2.getRainStrength(1.0F)));
         var1.connection.sendPacket(new SPacketChangeGameState(8, var2.getThunderStrength(1.0F)));
      }

   }

   public void syncPlayerInventory(EntityPlayerMP var1) {
      var1.sendContainerToPlayer(var1.inventoryContainer);
      var1.setPlayerHealthUpdated();
      var1.connection.sendPacket(new SPacketHeldItemChange(var1.inventory.currentItem));
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

   public MinecraftServer getServerInstance() {
      return this.mcServer;
   }

   public NBTTagCompound getHostPlayerData() {
      return null;
   }

   @SideOnly(Side.CLIENT)
   public void setGameType(GameType var1) {
      this.gameType = var1;
   }

   private void setPlayerGameTypeBasedOnOther(EntityPlayerMP var1, EntityPlayerMP var2, World var3) {
      if (var2 != null) {
         var1.interactionManager.setGameType(var2.interactionManager.getGameType());
      } else if (this.gameType != null) {
         var1.interactionManager.setGameType(this.gameType);
      }

      var1.interactionManager.initializeGameType(var3.getWorldInfo().getGameType());
   }

   @SideOnly(Side.CLIENT)
   public void setCommandsAllowedForAll(boolean var1) {
      this.commandsAllowedForAll = var1;
   }

   public void removeAllPlayers() {
      for(int var1 = 0; var1 < this.playerEntityList.size(); ++var1) {
         ((EntityPlayerMP)this.playerEntityList.get(var1)).connection.disconnect("Server closed");
      }

   }

   public void sendChatMsgImpl(ITextComponent var1, boolean var2) {
      this.mcServer.sendMessage(var1);
      byte var3 = (byte)(var2 ? 1 : 0);
      this.sendPacketToAllPlayers(new SPacketChat(var1, var3));
   }

   public void sendChatMsg(ITextComponent var1) {
      this.sendChatMsgImpl(var1, true);
   }

   public StatisticsManagerServer getPlayerStatsFile(EntityPlayer var1) {
      UUID var2 = var1.getUniqueID();
      StatisticsManagerServer var3 = var2 == null ? null : (StatisticsManagerServer)this.playerStatFiles.get(var2);
      if (var3 == null) {
         File var4 = new File(this.mcServer.worldServerForDimension(0).getSaveHandler().getWorldDirectory(), "stats");
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
      if (this.mcServer.worlds != null) {
         for(WorldServer var5 : this.mcServer.worlds) {
            if (var5 != null) {
               var5.getPlayerChunkMap().setPlayerViewRadius(var1);
               var5.getEntityTracker().setViewDistance(var1);
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

   @SideOnly(Side.SERVER)
   public boolean isWhiteListEnabled() {
      return this.whiteListEnforced;
   }
}
