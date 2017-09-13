package net.minecraft.client.network;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.GuardianSound;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiCommandBlock;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.GuiScreenDemo;
import net.minecraft.client.gui.GuiScreenRealmsProxy;
import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.gui.IProgressMeter;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.ParticleItemPickup;
import net.minecraft.client.player.inventory.ContainerLocalMenu;
import net.minecraft.client.player.inventory.LocalBlockIntercommunication;
import net.minecraft.client.renderer.debug.DebugRendererPathfinding;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.NpcMerchant;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityDragonFireball;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.AnimalChest;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketResourcePackStatus;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.network.play.server.SPacketBlockAction;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketCamera;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketCloseWindow;
import net.minecraft.network.play.server.SPacketCollectItem;
import net.minecraft.network.play.server.SPacketCombatEvent;
import net.minecraft.network.play.server.SPacketConfirmTransaction;
import net.minecraft.network.play.server.SPacketCooldown;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.network.play.server.SPacketCustomSound;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.network.play.server.SPacketDisplayObjective;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketEntity;
import net.minecraft.network.play.server.SPacketEntityAttach;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketEntityEquipment;
import net.minecraft.network.play.server.SPacketEntityHeadLook;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraft.network.play.server.SPacketEntityProperties;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityTeleport;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketJoinGame;
import net.minecraft.network.play.server.SPacketKeepAlive;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.network.play.server.SPacketMoveVehicle;
import net.minecraft.network.play.server.SPacketMultiBlockChange;
import net.minecraft.network.play.server.SPacketOpenWindow;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.network.play.server.SPacketPlayerAbilities;
import net.minecraft.network.play.server.SPacketPlayerListHeaderFooter;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketRemoveEntityEffect;
import net.minecraft.network.play.server.SPacketResourcePackSend;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketScoreboardObjective;
import net.minecraft.network.play.server.SPacketServerDifficulty;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.network.play.server.SPacketSignEditorOpen;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnExperienceOrb;
import net.minecraft.network.play.server.SPacketSpawnGlobalEntity;
import net.minecraft.network.play.server.SPacketSpawnMob;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.network.play.server.SPacketSpawnPainting;
import net.minecraft.network.play.server.SPacketSpawnPlayer;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.network.play.server.SPacketStatistics;
import net.minecraft.network.play.server.SPacketTabComplete;
import net.minecraft.network.play.server.SPacketTeams;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.network.play.server.SPacketUnloadChunk;
import net.minecraft.network.play.server.SPacketUpdateBossInfo;
import net.minecraft.network.play.server.SPacketUpdateHealth;
import net.minecraft.network.play.server.SPacketUpdateScore;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.network.play.server.SPacketUseBed;
import net.minecraft.network.play.server.SPacketWindowItems;
import net.minecraft.network.play.server.SPacketWindowProperty;
import net.minecraft.network.play.server.SPacketWorldBorder;
import net.minecraft.pathfinding.Path;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntityEndGateway;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITabCompleter;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class NetHandlerPlayClient implements INetHandlerPlayClient {
   private static final Logger LOGGER = LogManager.getLogger();
   private final NetworkManager netManager;
   private final GameProfile profile;
   private final GuiScreen guiScreenServer;
   private Minecraft gameController;
   private WorldClient clientWorldController;
   private boolean doneLoadingTerrain;
   private final Map playerInfoMap = Maps.newHashMap();
   public int currentServerMaxPlayers = 20;
   private boolean hasStatistics;
   private final Random avRandomizer = new Random();

   public NetHandlerPlayClient(Minecraft var1, GuiScreen var2, NetworkManager var3, GameProfile var4) {
      this.gameController = var1;
      this.guiScreenServer = var2;
      this.netManager = var3;
      this.profile = var4;
   }

   public void cleanup() {
      this.clientWorldController = null;
   }

   public void handleJoinGame(SPacketJoinGame var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      this.gameController.playerController = new PlayerControllerMP(this.gameController, this);
      this.clientWorldController = new WorldClient(this, new WorldSettings(0L, var1.getGameType(), false, var1.isHardcoreMode(), var1.getWorldType()), NetworkDispatcher.get(this.getNetworkManager()).getOverrideDimension(var1), var1.getDifficulty(), this.gameController.mcProfiler);
      this.gameController.gameSettings.difficulty = var1.getDifficulty();
      this.gameController.loadWorld(this.clientWorldController);
      this.gameController.player.dimension = var1.getDimension();
      this.gameController.displayGuiScreen(new GuiDownloadTerrain(this));
      this.gameController.player.setEntityId(var1.getPlayerId());
      this.currentServerMaxPlayers = var1.getMaxPlayers();
      this.gameController.player.setReducedDebug(var1.isReducedDebugInfo());
      this.gameController.playerController.setGameType(var1.getGameType());
      this.gameController.gameSettings.sendSettingsToServer();
      this.netManager.sendPacket(new CPacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString(ClientBrandRetriever.getClientModName())));
   }

   public void handleSpawnObject(SPacketSpawnObject var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      double var2 = var1.getX();
      double var4 = var1.getY();
      double var6 = var1.getZ();
      Object var8 = null;
      if (var1.getType() == 10) {
         var8 = EntityMinecart.create(this.clientWorldController, var2, var4, var6, EntityMinecart.Type.getById(var1.getData()));
      } else if (var1.getType() == 90) {
         Entity var9 = this.clientWorldController.getEntityByID(var1.getData());
         if (var9 instanceof EntityPlayer) {
            var8 = new EntityFishHook(this.clientWorldController, var2, var4, var6, (EntityPlayer)var9);
         }

         var1.setData(0);
      } else if (var1.getType() == 60) {
         var8 = new EntityTippedArrow(this.clientWorldController, var2, var4, var6);
      } else if (var1.getType() == 91) {
         var8 = new EntitySpectralArrow(this.clientWorldController, var2, var4, var6);
      } else if (var1.getType() == 61) {
         var8 = new EntitySnowball(this.clientWorldController, var2, var4, var6);
      } else if (var1.getType() == 71) {
         var8 = new EntityItemFrame(this.clientWorldController, new BlockPos(var2, var4, var6), EnumFacing.getHorizontal(var1.getData()));
         var1.setData(0);
      } else if (var1.getType() == 77) {
         var8 = new EntityLeashKnot(this.clientWorldController, new BlockPos(MathHelper.floor(var2), MathHelper.floor(var4), MathHelper.floor(var6)));
         var1.setData(0);
      } else if (var1.getType() == 65) {
         var8 = new EntityEnderPearl(this.clientWorldController, var2, var4, var6);
      } else if (var1.getType() == 72) {
         var8 = new EntityEnderEye(this.clientWorldController, var2, var4, var6);
      } else if (var1.getType() == 76) {
         var8 = new EntityFireworkRocket(this.clientWorldController, var2, var4, var6, (ItemStack)null);
      } else if (var1.getType() == 63) {
         var8 = new EntityLargeFireball(this.clientWorldController, var2, var4, var6, (double)var1.getSpeedX() / 8000.0D, (double)var1.getSpeedY() / 8000.0D, (double)var1.getSpeedZ() / 8000.0D);
         var1.setData(0);
      } else if (var1.getType() == 93) {
         var8 = new EntityDragonFireball(this.clientWorldController, var2, var4, var6, (double)var1.getSpeedX() / 8000.0D, (double)var1.getSpeedY() / 8000.0D, (double)var1.getSpeedZ() / 8000.0D);
         var1.setData(0);
      } else if (var1.getType() == 64) {
         var8 = new EntitySmallFireball(this.clientWorldController, var2, var4, var6, (double)var1.getSpeedX() / 8000.0D, (double)var1.getSpeedY() / 8000.0D, (double)var1.getSpeedZ() / 8000.0D);
         var1.setData(0);
      } else if (var1.getType() == 66) {
         var8 = new EntityWitherSkull(this.clientWorldController, var2, var4, var6, (double)var1.getSpeedX() / 8000.0D, (double)var1.getSpeedY() / 8000.0D, (double)var1.getSpeedZ() / 8000.0D);
         var1.setData(0);
      } else if (var1.getType() == 67) {
         var8 = new EntityShulkerBullet(this.clientWorldController, var2, var4, var6, (double)var1.getSpeedX() / 8000.0D, (double)var1.getSpeedY() / 8000.0D, (double)var1.getSpeedZ() / 8000.0D);
         var1.setData(0);
      } else if (var1.getType() == 62) {
         var8 = new EntityEgg(this.clientWorldController, var2, var4, var6);
      } else if (var1.getType() == 73) {
         var8 = new EntityPotion(this.clientWorldController, var2, var4, var6, (ItemStack)null);
         var1.setData(0);
      } else if (var1.getType() == 75) {
         var8 = new EntityExpBottle(this.clientWorldController, var2, var4, var6);
         var1.setData(0);
      } else if (var1.getType() == 1) {
         var8 = new EntityBoat(this.clientWorldController, var2, var4, var6);
      } else if (var1.getType() == 50) {
         var8 = new EntityTNTPrimed(this.clientWorldController, var2, var4, var6, (EntityLivingBase)null);
      } else if (var1.getType() == 78) {
         var8 = new EntityArmorStand(this.clientWorldController, var2, var4, var6);
      } else if (var1.getType() == 51) {
         var8 = new EntityEnderCrystal(this.clientWorldController, var2, var4, var6);
      } else if (var1.getType() == 2) {
         var8 = new EntityItem(this.clientWorldController, var2, var4, var6);
      } else if (var1.getType() == 70) {
         var8 = new EntityFallingBlock(this.clientWorldController, var2, var4, var6, Block.getStateById(var1.getData() & '\uffff'));
         var1.setData(0);
      } else if (var1.getType() == 3) {
         var8 = new EntityAreaEffectCloud(this.clientWorldController, var2, var4, var6);
      }

      if (var8 != null) {
         EntityTracker.updateServerPosition((Entity)var8, var2, var4, var6);
         ((Entity)var8).rotationPitch = (float)(var1.getPitch() * 360) / 256.0F;
         ((Entity)var8).rotationYaw = (float)(var1.getYaw() * 360) / 256.0F;
         Entity[] var15 = ((Entity)var8).getParts();
         if (var15 != null) {
            int var10 = var1.getEntityID() - ((Entity)var8).getEntityId();

            for(Entity var14 : var15) {
               var14.setEntityId(var14.getEntityId() + var10);
            }
         }

         ((Entity)var8).setEntityId(var1.getEntityID());
         ((Entity)var8).setUniqueId(var1.getUniqueId());
         this.clientWorldController.addEntityToWorld(var1.getEntityID(), (Entity)var8);
         if (var1.getData() > 0) {
            if (var1.getType() == 60 || var1.getType() == 91) {
               Entity var16 = this.clientWorldController.getEntityByID(var1.getData() - 1);
               if (var16 instanceof EntityLivingBase && var8 instanceof EntityArrow) {
                  ((EntityArrow)var8).shootingEntity = var16;
               }
            }

            ((Entity)var8).setVelocity((double)var1.getSpeedX() / 8000.0D, (double)var1.getSpeedY() / 8000.0D, (double)var1.getSpeedZ() / 8000.0D);
         }
      }

   }

   public void handleSpawnExperienceOrb(SPacketSpawnExperienceOrb var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      double var2 = var1.getX();
      double var4 = var1.getY();
      double var6 = var1.getZ();
      EntityXPOrb var8 = new EntityXPOrb(this.clientWorldController, var2, var4, var6, var1.getXPValue());
      EntityTracker.updateServerPosition(var8, var2, var4, var6);
      var8.rotationYaw = 0.0F;
      var8.rotationPitch = 0.0F;
      var8.setEntityId(var1.getEntityID());
      this.clientWorldController.addEntityToWorld(var1.getEntityID(), var8);
   }

   public void handleSpawnGlobalEntity(SPacketSpawnGlobalEntity var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      double var2 = var1.getX();
      double var4 = var1.getY();
      double var6 = var1.getZ();
      EntityLightningBolt var8 = null;
      if (var1.getType() == 1) {
         var8 = new EntityLightningBolt(this.clientWorldController, var2, var4, var6, false);
      }

      if (var8 != null) {
         EntityTracker.updateServerPosition(var8, var2, var4, var6);
         var8.rotationYaw = 0.0F;
         var8.rotationPitch = 0.0F;
         var8.setEntityId(var1.getEntityId());
         this.clientWorldController.addWeatherEffect(var8);
      }

   }

   public void handleSpawnPainting(SPacketSpawnPainting var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      EntityPainting var2 = new EntityPainting(this.clientWorldController, var1.getPosition(), var1.getFacing(), var1.getTitle());
      var2.setUniqueId(var1.getUniqueId());
      this.clientWorldController.addEntityToWorld(var1.getEntityID(), var2);
   }

   public void handleEntityVelocity(SPacketEntityVelocity var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      Entity var2 = this.clientWorldController.getEntityByID(var1.getEntityID());
      if (var2 != null) {
         var2.setVelocity((double)var1.getMotionX() / 8000.0D, (double)var1.getMotionY() / 8000.0D, (double)var1.getMotionZ() / 8000.0D);
      }

   }

   public void handleEntityMetadata(SPacketEntityMetadata var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      Entity var2 = this.clientWorldController.getEntityByID(var1.getEntityId());
      if (var2 != null && var1.getDataManagerEntries() != null) {
         var2.getDataManager().setEntryValues(var1.getDataManagerEntries());
      }

   }

   public void handleSpawnPlayer(SPacketSpawnPlayer var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      double var2 = var1.getX();
      double var4 = var1.getY();
      double var6 = var1.getZ();
      float var8 = (float)(var1.getYaw() * 360) / 256.0F;
      float var9 = (float)(var1.getPitch() * 360) / 256.0F;
      EntityOtherPlayerMP var10 = new EntityOtherPlayerMP(this.gameController.world, this.getPlayerInfo(var1.getUniqueId()).getGameProfile());
      var10.prevPosX = var2;
      var10.lastTickPosX = var2;
      var10.prevPosY = var4;
      var10.lastTickPosY = var4;
      var10.prevPosZ = var6;
      var10.lastTickPosZ = var6;
      EntityTracker.updateServerPosition(var10, var2, var4, var6);
      var10.setPositionAndRotation(var2, var4, var6, var8, var9);
      this.clientWorldController.addEntityToWorld(var1.getEntityID(), var10);
      List var11 = var1.getDataManagerEntries();
      if (var11 != null) {
         var10.getDataManager().setEntryValues(var11);
      }

   }

   public void handleEntityTeleport(SPacketEntityTeleport var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      Entity var2 = this.clientWorldController.getEntityByID(var1.getEntityId());
      if (var2 != null) {
         double var3 = var1.getX();
         double var5 = var1.getY();
         double var7 = var1.getZ();
         EntityTracker.updateServerPosition(var2, var3, var5, var7);
         if (!var2.canPassengerSteer()) {
            float var9 = (float)(var1.getYaw() * 360) / 256.0F;
            float var10 = (float)(var1.getPitch() * 360) / 256.0F;
            if (Math.abs(var2.posX - var3) < 0.03125D && Math.abs(var2.posY - var5) < 0.015625D && Math.abs(var2.posZ - var7) < 0.03125D) {
               var2.setPositionAndRotationDirect(var2.posX, var2.posY, var2.posZ, var9, var10, 0, true);
            } else {
               var2.setPositionAndRotationDirect(var3, var5, var7, var9, var10, 3, true);
            }

            var2.onGround = var1.getOnGround();
         }
      }

   }

   public void handleHeldItemChange(SPacketHeldItemChange var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      if (InventoryPlayer.isHotbar(var1.getHeldItemHotbarIndex())) {
         this.gameController.player.inventory.currentItem = var1.getHeldItemHotbarIndex();
      }

   }

   public void handleEntityMovement(SPacketEntity var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      Entity var2 = var1.getEntity(this.clientWorldController);
      if (var2 != null) {
         var2.serverPosX += (long)var1.getX();
         var2.serverPosY += (long)var1.getY();
         var2.serverPosZ += (long)var1.getZ();
         double var3 = (double)var2.serverPosX / 4096.0D;
         double var5 = (double)var2.serverPosY / 4096.0D;
         double var7 = (double)var2.serverPosZ / 4096.0D;
         if (!var2.canPassengerSteer()) {
            float var9 = var1.isRotating() ? (float)(var1.getYaw() * 360) / 256.0F : var2.rotationYaw;
            float var10 = var1.isRotating() ? (float)(var1.getPitch() * 360) / 256.0F : var2.rotationPitch;
            var2.setPositionAndRotationDirect(var3, var5, var7, var9, var10, 3, false);
            var2.onGround = var1.getOnGround();
         }
      }

   }

   public void handleEntityHeadLook(SPacketEntityHeadLook var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      Entity var2 = var1.getEntity(this.clientWorldController);
      if (var2 != null) {
         float var3 = (float)(var1.getYaw() * 360) / 256.0F;
         var2.setRotationYawHead(var3);
      }

   }

   public void handleDestroyEntities(SPacketDestroyEntities var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);

      for(int var2 = 0; var2 < var1.getEntityIDs().length; ++var2) {
         this.clientWorldController.removeEntityFromWorld(var1.getEntityIDs()[var2]);
      }

   }

   public void handlePlayerPosLook(SPacketPlayerPosLook var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      EntityPlayerSP var2 = this.gameController.player;
      double var3 = var1.getX();
      double var5 = var1.getY();
      double var7 = var1.getZ();
      float var9 = var1.getYaw();
      float var10 = var1.getPitch();
      if (var1.getFlags().contains(SPacketPlayerPosLook.EnumFlags.X)) {
         var3 += var2.posX;
      } else {
         var2.motionX = 0.0D;
      }

      if (var1.getFlags().contains(SPacketPlayerPosLook.EnumFlags.Y)) {
         var5 += var2.posY;
      } else {
         var2.motionY = 0.0D;
      }

      if (var1.getFlags().contains(SPacketPlayerPosLook.EnumFlags.Z)) {
         var7 += var2.posZ;
      } else {
         var2.motionZ = 0.0D;
      }

      if (var1.getFlags().contains(SPacketPlayerPosLook.EnumFlags.X_ROT)) {
         var10 += var2.rotationPitch;
      }

      if (var1.getFlags().contains(SPacketPlayerPosLook.EnumFlags.Y_ROT)) {
         var9 += var2.rotationYaw;
      }

      var2.setPositionAndRotation(var3, var5, var7, var9, var10);
      this.netManager.sendPacket(new CPacketConfirmTeleport(var1.getTeleportId()));
      this.netManager.sendPacket(new CPacketPlayer.PositionRotation(var2.posX, var2.getEntityBoundingBox().minY, var2.posZ, var2.rotationYaw, var2.rotationPitch, false));
      if (!this.doneLoadingTerrain) {
         this.gameController.player.prevPosX = this.gameController.player.posX;
         this.gameController.player.prevPosY = this.gameController.player.posY;
         this.gameController.player.prevPosZ = this.gameController.player.posZ;
         this.doneLoadingTerrain = true;
         this.gameController.displayGuiScreen((GuiScreen)null);
      }

   }

   public void handleMultiBlockChange(SPacketMultiBlockChange var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);

      for(SPacketMultiBlockChange.BlockUpdateData var5 : var1.getChangedBlocks()) {
         this.clientWorldController.invalidateRegionAndSetBlock(var5.getPos(), var5.getBlockState());
      }

   }

   public void handleChunkData(SPacketChunkData var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      if (var1.doChunkLoad()) {
         this.clientWorldController.doPreChunk(var1.getChunkX(), var1.getChunkZ(), true);
      }

      this.clientWorldController.invalidateBlockReceiveRegion(var1.getChunkX() << 4, 0, var1.getChunkZ() << 4, (var1.getChunkX() << 4) + 15, 256, (var1.getChunkZ() << 4) + 15);
      Chunk var2 = this.clientWorldController.getChunkFromChunkCoords(var1.getChunkX(), var1.getChunkZ());
      var2.fillChunk(var1.getReadBuffer(), var1.getExtractedSize(), var1.doChunkLoad());
      this.clientWorldController.markBlockRangeForRenderUpdate(var1.getChunkX() << 4, 0, var1.getChunkZ() << 4, (var1.getChunkX() << 4) + 15, 256, (var1.getChunkZ() << 4) + 15);
      if (!var1.doChunkLoad() || !(this.clientWorldController.provider instanceof WorldProviderSurface)) {
         var2.resetRelightChecks();
      }

      for(NBTTagCompound var4 : var1.getTileEntityTags()) {
         BlockPos var5 = new BlockPos(var4.getInteger("x"), var4.getInteger("y"), var4.getInteger("z"));
         TileEntity var6 = this.clientWorldController.getTileEntity(var5);
         if (var6 != null) {
            var6.handleUpdateTag(var4);
         }
      }

   }

   public void processChunkUnload(SPacketUnloadChunk var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      this.clientWorldController.doPreChunk(var1.getX(), var1.getZ(), false);
   }

   public void handleBlockChange(SPacketBlockChange var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      this.clientWorldController.invalidateRegionAndSetBlock(var1.getBlockPosition(), var1.getBlockState());
   }

   public void handleDisconnect(SPacketDisconnect var1) {
      this.netManager.closeChannel(var1.getReason());
   }

   public void onDisconnect(ITextComponent var1) {
      this.gameController.loadWorld((WorldClient)null);
      if (this.guiScreenServer != null) {
         if (this.guiScreenServer instanceof GuiScreenRealmsProxy) {
            this.gameController.displayGuiScreen((new DisconnectedRealmsScreen(((GuiScreenRealmsProxy)this.guiScreenServer).getProxy(), "disconnect.lost", var1)).getProxy());
         } else {
            this.gameController.displayGuiScreen(new GuiDisconnected(this.guiScreenServer, "disconnect.lost", var1));
         }
      } else {
         this.gameController.displayGuiScreen(new GuiDisconnected(new GuiMultiplayer(new GuiMainMenu()), "disconnect.lost", var1));
      }

   }

   public void sendPacket(Packet var1) {
      this.netManager.sendPacket(var1);
   }

   public void handleCollectItem(SPacketCollectItem var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      Entity var2 = this.clientWorldController.getEntityByID(var1.getCollectedItemEntityID());
      Object var3 = (EntityLivingBase)this.clientWorldController.getEntityByID(var1.getEntityID());
      if (var3 == null) {
         var3 = this.gameController.player;
      }

      if (var2 != null) {
         if (var2 instanceof EntityXPOrb) {
            this.clientWorldController.playSound(var2.posX, var2.posY, var2.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.2F, ((this.avRandomizer.nextFloat() - this.avRandomizer.nextFloat()) * 0.7F + 1.0F) * 2.0F, false);
         } else {
            this.clientWorldController.playSound(var2.posX, var2.posY, var2.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((this.avRandomizer.nextFloat() - this.avRandomizer.nextFloat()) * 0.7F + 1.0F) * 2.0F, false);
         }

         this.gameController.effectRenderer.addEffect(new ParticleItemPickup(this.clientWorldController, var2, (Entity)var3, 0.5F));
         this.clientWorldController.removeEntityFromWorld(var1.getCollectedItemEntityID());
      }

   }

   public void handleChat(SPacketChat var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      ITextComponent var2 = ForgeEventFactory.onClientChat(var1.getType(), var1.getChatComponent());
      if (var2 != null) {
         if (var1.getType() == 2) {
            this.gameController.ingameGUI.setOverlayMessage(var2, false);
         } else {
            this.gameController.ingameGUI.getChatGUI().printChatMessage(var2);
         }

      }
   }

   public void handleAnimation(SPacketAnimation var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      Entity var2 = this.clientWorldController.getEntityByID(var1.getEntityID());
      if (var2 != null) {
         if (var1.getAnimationType() == 0) {
            EntityLivingBase var3 = (EntityLivingBase)var2;
            var3.swingArm(EnumHand.MAIN_HAND);
         } else if (var1.getAnimationType() == 3) {
            EntityLivingBase var4 = (EntityLivingBase)var2;
            var4.swingArm(EnumHand.OFF_HAND);
         } else if (var1.getAnimationType() == 1) {
            var2.performHurtAnimation();
         } else if (var1.getAnimationType() == 2) {
            EntityPlayer var5 = (EntityPlayer)var2;
            var5.wakeUpPlayer(false, false, false);
         } else if (var1.getAnimationType() == 4) {
            this.gameController.effectRenderer.emitParticleAtEntity(var2, EnumParticleTypes.CRIT);
         } else if (var1.getAnimationType() == 5) {
            this.gameController.effectRenderer.emitParticleAtEntity(var2, EnumParticleTypes.CRIT_MAGIC);
         }
      }

   }

   public void handleUseBed(SPacketUseBed var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      var1.getPlayer(this.clientWorldController).trySleep(var1.getBedPosition());
   }

   public void handleSpawnMob(SPacketSpawnMob var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      double var2 = var1.getX();
      double var4 = var1.getY();
      double var6 = var1.getZ();
      float var8 = (float)(var1.getYaw() * 360) / 256.0F;
      float var9 = (float)(var1.getPitch() * 360) / 256.0F;
      EntityLivingBase var10 = (EntityLivingBase)EntityList.createEntityByID(var1.getEntityType(), this.gameController.world);
      if (var10 == null) {
         FMLLog.info("Server attempted to spawn an unknown entity using ID: {0} at ({1}, {2}, {3}) Skipping!", new Object[]{var1.getEntityType(), var2, var4, var6});
      } else {
         EntityTracker.updateServerPosition(var10, var2, var4, var6);
         var10.renderYawOffset = (float)(var1.getHeadPitch() * 360) / 256.0F;
         var10.rotationYawHead = (float)(var1.getHeadPitch() * 360) / 256.0F;
         Entity[] var11 = var10.getParts();
         if (var11 != null) {
            int var12 = var1.getEntityID() - var10.getEntityId();

            for(Entity var16 : var11) {
               var16.setEntityId(var16.getEntityId() + var12);
            }
         }

         var10.setEntityId(var1.getEntityID());
         var10.setUniqueId(var1.getUniqueId());
         var10.setPositionAndRotation(var2, var4, var6, var8, var9);
         var10.motionX = (double)((float)var1.getVelocityX() / 8000.0F);
         var10.motionY = (double)((float)var1.getVelocityY() / 8000.0F);
         var10.motionZ = (double)((float)var1.getVelocityZ() / 8000.0F);
         this.clientWorldController.addEntityToWorld(var1.getEntityID(), var10);
         List var17 = var1.getDataManagerEntries();
         if (var17 != null) {
            var10.getDataManager().setEntryValues(var17);
         }

      }
   }

   public void handleTimeUpdate(SPacketTimeUpdate var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      this.gameController.world.setTotalWorldTime(var1.getTotalWorldTime());
      this.gameController.world.setWorldTime(var1.getWorldTime());
   }

   public void handleSpawnPosition(SPacketSpawnPosition var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      this.gameController.player.setSpawnPoint(var1.getSpawnPos(), true);
      this.gameController.world.getWorldInfo().setSpawn(var1.getSpawnPos());
   }

   public void handleSetPassengers(SPacketSetPassengers var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      Entity var2 = this.clientWorldController.getEntityByID(var1.getEntityId());
      if (var2 == null) {
         LOGGER.warn("Received passengers for unknown entity");
      } else {
         boolean var3 = var2.isRidingOrBeingRiddenBy(this.gameController.player);
         var2.removePassengers();

         for(int var7 : var1.getPassengerIds()) {
            Entity var8 = this.clientWorldController.getEntityByID(var7);
            if (var8 == null) {
               LOGGER.warn("Received unknown passenger for {}", new Object[]{var2});
            } else {
               var8.startRiding(var2, true);
               if (var8 == this.gameController.player && !var3) {
                  this.gameController.ingameGUI.setOverlayMessage(I18n.format("mount.onboard", this.gameController.gameSettings.keyBindSneak.getDisplayName()), false);
               }
            }
         }
      }

   }

   public void handleEntityAttach(SPacketEntityAttach var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      Entity var2 = this.clientWorldController.getEntityByID(var1.getEntityId());
      Entity var3 = this.clientWorldController.getEntityByID(var1.getVehicleEntityId());
      if (var2 instanceof EntityLiving) {
         if (var3 != null) {
            ((EntityLiving)var2).setLeashedToEntity(var3, false);
         } else {
            ((EntityLiving)var2).clearLeashed(false, false);
         }
      }

   }

   public void handleEntityStatus(SPacketEntityStatus var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      Entity var2 = var1.getEntity(this.clientWorldController);
      if (var2 != null) {
         if (var1.getOpCode() == 21) {
            this.gameController.getSoundHandler().playSound(new GuardianSound((EntityGuardian)var2));
         } else {
            var2.handleStatusUpdate(var1.getOpCode());
         }
      }

   }

   public void handleUpdateHealth(SPacketUpdateHealth var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      this.gameController.player.setPlayerSPHealth(var1.getHealth());
      this.gameController.player.getFoodStats().setFoodLevel(var1.getFoodLevel());
      this.gameController.player.getFoodStats().setFoodSaturationLevel(var1.getSaturationLevel());
   }

   public void handleSetExperience(SPacketSetExperience var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      this.gameController.player.setXPStats(var1.getExperienceBar(), var1.getTotalExperience(), var1.getLevel());
   }

   public void handleRespawn(SPacketRespawn var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      if (var1.getDimensionID() != this.gameController.player.dimension) {
         this.doneLoadingTerrain = false;
         Scoreboard var2 = this.clientWorldController.getScoreboard();
         this.clientWorldController = new WorldClient(this, new WorldSettings(0L, var1.getGameType(), false, this.gameController.world.getWorldInfo().isHardcoreModeEnabled(), var1.getWorldType()), var1.getDimensionID(), var1.getDifficulty(), this.gameController.mcProfiler);
         this.clientWorldController.setWorldScoreboard(var2);
         this.gameController.loadWorld(this.clientWorldController);
         this.gameController.player.dimension = var1.getDimensionID();
         this.gameController.displayGuiScreen(new GuiDownloadTerrain(this));
      }

      this.gameController.setDimensionAndSpawnPlayer(var1.getDimensionID());
      this.gameController.playerController.setGameType(var1.getGameType());
   }

   public void handleExplosion(SPacketExplosion var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      Explosion var2 = new Explosion(this.gameController.world, (Entity)null, var1.getX(), var1.getY(), var1.getZ(), var1.getStrength(), var1.getAffectedBlockPositions());
      var2.doExplosionB(true);
      this.gameController.player.motionX += (double)var1.getMotionX();
      this.gameController.player.motionY += (double)var1.getMotionY();
      this.gameController.player.motionZ += (double)var1.getMotionZ();
   }

   public void handleOpenWindow(SPacketOpenWindow var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      EntityPlayerSP var2 = this.gameController.player;
      if ("minecraft:container".equals(var1.getGuiId())) {
         var2.displayGUIChest(new InventoryBasic(var1.getWindowTitle(), var1.getSlotCount()));
         var2.openContainer.windowId = var1.getWindowId();
      } else if ("minecraft:villager".equals(var1.getGuiId())) {
         var2.displayVillagerTradeGui(new NpcMerchant(var2, var1.getWindowTitle()));
         var2.openContainer.windowId = var1.getWindowId();
      } else if ("EntityHorse".equals(var1.getGuiId())) {
         Entity var3 = this.clientWorldController.getEntityByID(var1.getEntityId());
         if (var3 instanceof EntityHorse) {
            var2.openGuiHorseInventory((EntityHorse)var3, new AnimalChest(var1.getWindowTitle(), var1.getSlotCount()));
            var2.openContainer.windowId = var1.getWindowId();
         }
      } else if (!var1.hasSlots()) {
         var2.displayGui(new LocalBlockIntercommunication(var1.getGuiId(), var1.getWindowTitle()));
         var2.openContainer.windowId = var1.getWindowId();
      } else {
         ContainerLocalMenu var4 = new ContainerLocalMenu(var1.getGuiId(), var1.getWindowTitle(), var1.getSlotCount());
         var2.displayGUIChest(var4);
         var2.openContainer.windowId = var1.getWindowId();
      }

   }

   public void handleSetSlot(SPacketSetSlot var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      EntityPlayerSP var2 = this.gameController.player;
      if (var1.getWindowId() == -1) {
         var2.inventory.setItemStack(var1.getStack());
      } else if (var1.getWindowId() == -2) {
         var2.inventory.setInventorySlotContents(var1.getSlot(), var1.getStack());
      } else {
         boolean var3 = false;
         if (this.gameController.currentScreen instanceof GuiContainerCreative) {
            GuiContainerCreative var4 = (GuiContainerCreative)this.gameController.currentScreen;
            var3 = var4.getSelectedTabIndex() != CreativeTabs.INVENTORY.getTabIndex();
         }

         if (var1.getWindowId() == 0 && var1.getSlot() >= 36 && var1.getSlot() < 45) {
            ItemStack var5 = var2.inventoryContainer.getSlot(var1.getSlot()).getStack();
            if (var1.getStack() != null && (var5 == null || var5.stackSize < var1.getStack().stackSize)) {
               var1.getStack().animationsToGo = 5;
            }

            var2.inventoryContainer.putStackInSlot(var1.getSlot(), var1.getStack());
         } else if (var1.getWindowId() == var2.openContainer.windowId && (var1.getWindowId() != 0 || !var3)) {
            var2.openContainer.putStackInSlot(var1.getSlot(), var1.getStack());
         }
      }

   }

   public void handleConfirmTransaction(SPacketConfirmTransaction var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      Container var2 = null;
      EntityPlayerSP var3 = this.gameController.player;
      if (var1.getWindowId() == 0) {
         var2 = var3.inventoryContainer;
      } else if (var1.getWindowId() == var3.openContainer.windowId) {
         var2 = var3.openContainer;
      }

      if (var2 != null && !var1.wasAccepted()) {
         this.sendPacket(new CPacketConfirmTransaction(var1.getWindowId(), var1.getActionNumber(), true));
      }

   }

   public void handleWindowItems(SPacketWindowItems var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      EntityPlayerSP var2 = this.gameController.player;
      if (var1.getWindowId() == 0) {
         var2.inventoryContainer.putStacksInSlots(var1.getItemStacks());
      } else if (var1.getWindowId() == var2.openContainer.windowId) {
         var2.openContainer.putStacksInSlots(var1.getItemStacks());
      }

   }

   public void handleSignEditorOpen(SPacketSignEditorOpen var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      Object var2 = this.clientWorldController.getTileEntity(var1.getSignPosition());
      if (!(var2 instanceof TileEntitySign)) {
         var2 = new TileEntitySign();
         ((TileEntity)var2).setWorld(this.clientWorldController);
         ((TileEntity)var2).setPos(var1.getSignPosition());
      }

      this.gameController.player.openEditSign((TileEntitySign)var2);
   }

   public void handleUpdateTileEntity(SPacketUpdateTileEntity var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      if (this.gameController.world.isBlockLoaded(var1.getPos())) {
         TileEntity var2 = this.gameController.world.getTileEntity(var1.getPos());
         int var3 = var1.getTileEntityType();
         boolean var4 = var3 == 2 && var2 instanceof TileEntityCommandBlock;
         if ((var3 != 1 || !(var2 instanceof TileEntityMobSpawner)) && !var4 && (var3 != 3 || !(var2 instanceof TileEntityBeacon)) && (var3 != 4 || !(var2 instanceof TileEntitySkull)) && (var3 != 5 || !(var2 instanceof TileEntityFlowerPot)) && (var3 != 6 || !(var2 instanceof TileEntityBanner)) && (var3 != 7 || !(var2 instanceof TileEntityStructure)) && (var3 != 8 || !(var2 instanceof TileEntityEndGateway)) && (var3 != 9 || !(var2 instanceof TileEntitySign))) {
            if (var2 == null) {
               LOGGER.error("Received invalid update packet for null tile entity at {} with data: {}", new Object[]{var1.getPos(), var1.getNbtCompound()});
               return;
            }

            var2.onDataPacket(this.netManager, var1);
         } else {
            var2.readFromNBT(var1.getNbtCompound());
         }

         if (var4 && this.gameController.currentScreen instanceof GuiCommandBlock) {
            ((GuiCommandBlock)this.gameController.currentScreen).updateGui();
         }
      }

   }

   public void handleWindowProperty(SPacketWindowProperty var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      EntityPlayerSP var2 = this.gameController.player;
      if (var2.openContainer != null && var2.openContainer.windowId == var1.getWindowId()) {
         var2.openContainer.updateProgressBar(var1.getProperty(), var1.getValue());
      }

   }

   public void handleEntityEquipment(SPacketEntityEquipment var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      Entity var2 = this.clientWorldController.getEntityByID(var1.getEntityID());
      if (var2 != null) {
         var2.setItemStackToSlot(var1.getEquipmentSlot(), var1.getItemStack());
      }

   }

   public void handleCloseWindow(SPacketCloseWindow var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      this.gameController.player.closeScreenAndDropStack();
   }

   public void handleBlockAction(SPacketBlockAction var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      this.gameController.world.addBlockEvent(var1.getBlockPosition(), var1.getBlockType(), var1.getData1(), var1.getData2());
   }

   public void handleBlockBreakAnim(SPacketBlockBreakAnim var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      this.gameController.world.sendBlockBreakProgress(var1.getBreakerId(), var1.getPosition(), var1.getProgress());
   }

   public void handleChangeGameState(SPacketChangeGameState var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      EntityPlayerSP var2 = this.gameController.player;
      int var3 = var1.getGameState();
      float var4 = var1.getValue();
      int var5 = MathHelper.floor(var4 + 0.5F);
      if (var3 >= 0 && var3 < SPacketChangeGameState.MESSAGE_NAMES.length && SPacketChangeGameState.MESSAGE_NAMES[var3] != null) {
         var2.sendStatusMessage(new TextComponentTranslation(SPacketChangeGameState.MESSAGE_NAMES[var3], new Object[0]));
      }

      if (var3 == 1) {
         this.clientWorldController.getWorldInfo().setRaining(true);
         this.clientWorldController.setRainStrength(0.0F);
      } else if (var3 == 2) {
         this.clientWorldController.getWorldInfo().setRaining(false);
         this.clientWorldController.setRainStrength(1.0F);
      } else if (var3 == 3) {
         this.gameController.playerController.setGameType(GameType.getByID(var5));
      } else if (var3 == 4) {
         if (var5 == 0) {
            this.gameController.player.connection.sendPacket(new CPacketClientStatus(CPacketClientStatus.State.PERFORM_RESPAWN));
            this.gameController.displayGuiScreen(new GuiDownloadTerrain(this));
         } else if (var5 == 1) {
            this.gameController.displayGuiScreen(new GuiWinGame());
         }
      } else if (var3 == 5) {
         GameSettings var6 = this.gameController.gameSettings;
         if (var4 == 0.0F) {
            this.gameController.displayGuiScreen(new GuiScreenDemo());
         } else if (var4 == 101.0F) {
            this.gameController.ingameGUI.getChatGUI().printChatMessage(new TextComponentTranslation("demo.help.movement", new Object[]{var6.keyBindForward.getDisplayName(), var6.keyBindLeft.getDisplayName(), var6.keyBindBack.getDisplayName(), var6.keyBindRight.getDisplayName()}));
         } else if (var4 == 102.0F) {
            this.gameController.ingameGUI.getChatGUI().printChatMessage(new TextComponentTranslation("demo.help.jump", new Object[]{var6.keyBindJump.getDisplayName()}));
         } else if (var4 == 103.0F) {
            this.gameController.ingameGUI.getChatGUI().printChatMessage(new TextComponentTranslation("demo.help.inventory", new Object[]{var6.keyBindInventory.getDisplayName()}));
         }
      } else if (var3 == 6) {
         this.clientWorldController.playSound(var2, var2.posX, var2.posY + (double)var2.getEyeHeight(), var2.posZ, SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 0.18F, 0.45F);
      } else if (var3 == 7) {
         this.clientWorldController.setRainStrength(var4);
      } else if (var3 == 8) {
         this.clientWorldController.setThunderStrength(var4);
      } else if (var3 == 10) {
         this.clientWorldController.spawnParticle(EnumParticleTypes.MOB_APPEARANCE, var2.posX, var2.posY, var2.posZ, 0.0D, 0.0D, 0.0D, new int[0]);
         this.clientWorldController.playSound(var2, var2.posX, var2.posY, var2.posZ, SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.HOSTILE, 1.0F, 1.0F);
      }

   }

   public void handleMaps(SPacketMaps var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      MapData var2 = ItemMap.loadMapData(var1.getMapId(), this.gameController.world);
      var1.setMapdataTo(var2);
      this.gameController.entityRenderer.getMapItemRenderer().updateMapTexture(var2);
   }

   public void handleEffect(SPacketEffect var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      if (var1.isSoundServerwide()) {
         this.gameController.world.playBroadcastSound(var1.getSoundType(), var1.getSoundPos(), var1.getSoundData());
      } else {
         this.gameController.world.playEvent(var1.getSoundType(), var1.getSoundPos(), var1.getSoundData());
      }

   }

   public void handleStatistics(SPacketStatistics var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      boolean var2 = false;

      for(Entry var4 : var1.getStatisticMap().entrySet()) {
         StatBase var5 = (StatBase)var4.getKey();
         int var6 = ((Integer)var4.getValue()).intValue();
         if (var5.isAchievement() && var6 > 0) {
            if (this.hasStatistics && this.gameController.player.getStatFileWriter().readStat(var5) == 0) {
               Achievement var7 = (Achievement)var5;
               this.gameController.guiAchievement.displayAchievement(var7);
               if (var5 == AchievementList.OPEN_INVENTORY) {
                  this.gameController.gameSettings.showInventoryAchievementHint = false;
                  this.gameController.gameSettings.saveOptions();
               }
            }

            var2 = true;
         }

         this.gameController.player.getStatFileWriter().unlockAchievement(this.gameController.player, var5, var6);
      }

      if (!this.hasStatistics && !var2 && this.gameController.gameSettings.showInventoryAchievementHint) {
         this.gameController.guiAchievement.displayUnformattedAchievement(AchievementList.OPEN_INVENTORY);
      }

      this.hasStatistics = true;
      if (this.gameController.currentScreen instanceof IProgressMeter) {
         ((IProgressMeter)this.gameController.currentScreen).doneLoading();
      }

   }

   public void handleEntityEffect(SPacketEntityEffect var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      Entity var2 = this.clientWorldController.getEntityByID(var1.getEntityId());
      if (var2 instanceof EntityLivingBase) {
         Potion var3 = Potion.getPotionById(var1.getEffectId() & 255);
         if (var3 != null) {
            PotionEffect var4 = new PotionEffect(var3, var1.getDuration(), var1.getAmplifier(), var1.getIsAmbient(), var1.doesShowParticles());
            var4.setPotionDurationMax(var1.isMaxDuration());
            ((EntityLivingBase)var2).addPotionEffect(var4);
         }
      }

   }

   public void handleCombatEvent(SPacketCombatEvent var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      if (var1.eventType == SPacketCombatEvent.Event.ENTITY_DIED) {
         Entity var2 = this.clientWorldController.getEntityByID(var1.playerId);
         if (var2 == this.gameController.player) {
            this.gameController.displayGuiScreen(new GuiGameOver(var1.deathMessage));
         }
      }

   }

   public void handleServerDifficulty(SPacketServerDifficulty var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      this.gameController.world.getWorldInfo().setDifficulty(var1.getDifficulty());
      this.gameController.world.getWorldInfo().setDifficultyLocked(var1.isDifficultyLocked());
   }

   public void handleCamera(SPacketCamera var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      Entity var2 = var1.getEntity(this.clientWorldController);
      if (var2 != null) {
         this.gameController.setRenderViewEntity(var2);
      }

   }

   public void handleWorldBorder(SPacketWorldBorder var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      var1.apply(this.clientWorldController.getWorldBorder());
   }

   public void handleTitle(SPacketTitle var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      SPacketTitle.Type var2 = var1.getType();
      String var3 = null;
      String var4 = null;
      String var5 = var1.getMessage() != null ? var1.getMessage().getFormattedText() : "";
      switch(var2) {
      case TITLE:
         var3 = var5;
         break;
      case SUBTITLE:
         var4 = var5;
         break;
      case RESET:
         this.gameController.ingameGUI.displayTitle("", "", -1, -1, -1);
         this.gameController.ingameGUI.setDefaultTitlesTimes();
         return;
      }

      this.gameController.ingameGUI.displayTitle(var3, var4, var1.getFadeInTime(), var1.getDisplayTime(), var1.getFadeOutTime());
   }

   public void handlePlayerListHeaderFooter(SPacketPlayerListHeaderFooter var1) {
      this.gameController.ingameGUI.getTabList().setHeader(var1.getHeader().getFormattedText().isEmpty() ? null : var1.getHeader());
      this.gameController.ingameGUI.getTabList().setFooter(var1.getFooter().getFormattedText().isEmpty() ? null : var1.getFooter());
   }

   public void handleRemoveEntityEffect(SPacketRemoveEntityEffect var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      Entity var2 = var1.getEntity(this.clientWorldController);
      if (var2 instanceof EntityLivingBase) {
         ((EntityLivingBase)var2).removeActivePotionEffect(var1.getPotion());
      }

   }

   public void handlePlayerListItem(SPacketPlayerListItem var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);

      for(SPacketPlayerListItem.AddPlayerData var3 : var1.getEntries()) {
         if (var1.getAction() == SPacketPlayerListItem.Action.REMOVE_PLAYER) {
            this.playerInfoMap.remove(var3.getProfile().getId());
         } else {
            NetworkPlayerInfo var4 = (NetworkPlayerInfo)this.playerInfoMap.get(var3.getProfile().getId());
            if (var1.getAction() == SPacketPlayerListItem.Action.ADD_PLAYER) {
               var4 = new NetworkPlayerInfo(var3);
               this.playerInfoMap.put(var4.getGameProfile().getId(), var4);
            }

            if (var4 != null) {
               switch(var1.getAction()) {
               case ADD_PLAYER:
                  var4.setGameType(var3.getGameMode());
                  var4.setResponseTime(var3.getPing());
                  break;
               case UPDATE_GAME_MODE:
                  var4.setGameType(var3.getGameMode());
                  break;
               case UPDATE_LATENCY:
                  var4.setResponseTime(var3.getPing());
                  break;
               case UPDATE_DISPLAY_NAME:
                  var4.setDisplayName(var3.getDisplayName());
               }
            }
         }
      }

   }

   public void handleKeepAlive(SPacketKeepAlive var1) {
      this.sendPacket(new CPacketKeepAlive(var1.getId()));
   }

   public void handlePlayerAbilities(SPacketPlayerAbilities var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      EntityPlayerSP var2 = this.gameController.player;
      var2.capabilities.isFlying = var1.isFlying();
      var2.capabilities.isCreativeMode = var1.isCreativeMode();
      var2.capabilities.disableDamage = var1.isInvulnerable();
      var2.capabilities.allowFlying = var1.isAllowFlying();
      var2.capabilities.setFlySpeed(var1.getFlySpeed());
      var2.capabilities.setPlayerWalkSpeed(var1.getWalkSpeed());
   }

   public void handleTabComplete(SPacketTabComplete var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      String[] var2 = var1.getMatches();
      if (this.gameController.currentScreen instanceof ITabCompleter) {
         ((ITabCompleter)this.gameController.currentScreen).setCompletions(var2);
      }

   }

   public void handleSoundEffect(SPacketSoundEffect var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      this.gameController.world.playSound(this.gameController.player, var1.getX(), var1.getY(), var1.getZ(), var1.getSound(), var1.getCategory(), var1.getVolume(), var1.getPitch());
   }

   public void handleCustomSound(SPacketCustomSound var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      this.gameController.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(var1.getSoundName()), var1.getCategory(), var1.getVolume(), var1.getPitch(), false, 0, ISound.AttenuationType.LINEAR, (float)var1.getX(), (float)var1.getY(), (float)var1.getZ()));
   }

   public void handleResourcePack(SPacketResourcePackSend var1) {
      final String var2 = var1.getURL();
      final String var3 = var1.getHash();
      if (this.validateResourcePackUrl(var2)) {
         if (var2.startsWith("level://")) {
            String var4 = var2.substring("level://".length());
            File var5 = new File(this.gameController.mcDataDir, "saves");
            File var6 = new File(var5, var4);
            if (var6.isFile()) {
               this.netManager.sendPacket(new CPacketResourcePackStatus(CPacketResourcePackStatus.Action.ACCEPTED));
               Futures.addCallback(this.gameController.getResourcePackRepository().setResourcePackInstance(var6), this.createDownloadCallback());
            } else {
               this.netManager.sendPacket(new CPacketResourcePackStatus(CPacketResourcePackStatus.Action.FAILED_DOWNLOAD));
            }
         } else {
            ServerData var7 = this.gameController.getCurrentServerData();
            if (var7 != null && var7.getResourceMode() == ServerData.ServerResourceMode.ENABLED) {
               this.netManager.sendPacket(new CPacketResourcePackStatus(CPacketResourcePackStatus.Action.ACCEPTED));
               Futures.addCallback(this.gameController.getResourcePackRepository().downloadResourcePack(var2, var3), this.createDownloadCallback());
            } else if (var7 != null && var7.getResourceMode() != ServerData.ServerResourceMode.PROMPT) {
               this.netManager.sendPacket(new CPacketResourcePackStatus(CPacketResourcePackStatus.Action.DECLINED));
            } else {
               this.gameController.addScheduledTask(new Runnable() {
                  public void run() {
                     NetHandlerPlayClient.this.gameController.displayGuiScreen(new GuiYesNo(new GuiYesNoCallback() {
                        public void confirmClicked(boolean var1, int var2x) {
                           NetHandlerPlayClient.this.gameController = Minecraft.getMinecraft();
                           ServerData var3x = NetHandlerPlayClient.this.gameController.getCurrentServerData();
                           if (var1) {
                              if (var3x != null) {
                                 var3x.setResourceMode(ServerData.ServerResourceMode.ENABLED);
                              }

                              NetHandlerPlayClient.this.netManager.sendPacket(new CPacketResourcePackStatus(CPacketResourcePackStatus.Action.ACCEPTED));
                              Futures.addCallback(NetHandlerPlayClient.this.gameController.getResourcePackRepository().downloadResourcePack(var2, var3), NetHandlerPlayClient.this.createDownloadCallback());
                           } else {
                              if (var3x != null) {
                                 var3x.setResourceMode(ServerData.ServerResourceMode.DISABLED);
                              }

                              NetHandlerPlayClient.this.netManager.sendPacket(new CPacketResourcePackStatus(CPacketResourcePackStatus.Action.DECLINED));
                           }

                           ServerList.saveSingleServer(var3x);
                           NetHandlerPlayClient.this.gameController.displayGuiScreen((GuiScreen)null);
                        }
                     }, I18n.format("multiplayer.texturePrompt.line1"), I18n.format("multiplayer.texturePrompt.line2"), 0));
                  }
               });
            }
         }
      }

   }

   private boolean validateResourcePackUrl(String var1) {
      try {
         URI var2 = new URI(var1.replace(' ', '+'));
         String var3 = var2.getScheme();
         boolean var4 = "level".equals(var3);
         if (!"http".equals(var3) && !"https".equals(var3) && !var4) {
            throw new URISyntaxException(var1, "Wrong protocol");
         } else if (var4 && (var1.contains("..") || !var1.endsWith("/resources.zip"))) {
            throw new URISyntaxException(var1, "Invalid levelstorage resourcepack path");
         } else {
            return true;
         }
      } catch (URISyntaxException var5) {
         this.netManager.sendPacket(new CPacketResourcePackStatus(CPacketResourcePackStatus.Action.FAILED_DOWNLOAD));
         return false;
      }
   }

   private FutureCallback createDownloadCallback() {
      return new FutureCallback() {
         public void onSuccess(@Nullable Object var1) {
            NetHandlerPlayClient.this.netManager.sendPacket(new CPacketResourcePackStatus(CPacketResourcePackStatus.Action.SUCCESSFULLY_LOADED));
         }

         public void onFailure(Throwable var1) {
            NetHandlerPlayClient.this.netManager.sendPacket(new CPacketResourcePackStatus(CPacketResourcePackStatus.Action.FAILED_DOWNLOAD));
         }
      };
   }

   public void handleUpdateBossInfo(SPacketUpdateBossInfo var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      this.gameController.ingameGUI.getBossOverlay().read(var1);
   }

   public void handleCooldown(SPacketCooldown var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      if (var1.getTicks() == 0) {
         this.gameController.player.getCooldownTracker().removeCooldown(var1.getItem());
      } else {
         this.gameController.player.getCooldownTracker().setCooldown(var1.getItem(), var1.getTicks());
      }

   }

   public void handleMoveVehicle(SPacketMoveVehicle var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      Entity var2 = this.gameController.player.getLowestRidingEntity();
      if (var2 != this.gameController.player && var2.canPassengerSteer()) {
         var2.setPositionAndRotation(var1.getX(), var1.getY(), var1.getZ(), var1.getYaw(), var1.getPitch());
         this.netManager.sendPacket(new CPacketVehicleMove(var2));
      }

   }

   public void handleCustomPayload(SPacketCustomPayload var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      if ("MC|TrList".equals(var1.getChannelName())) {
         PacketBuffer var2 = var1.getBufferData();

         try {
            int var3 = var2.readInt();
            GuiScreen var4 = this.gameController.currentScreen;
            if (var4 != null && var4 instanceof GuiMerchant && var3 == this.gameController.player.openContainer.windowId) {
               IMerchant var5 = ((GuiMerchant)var4).getMerchant();
               MerchantRecipeList var6 = MerchantRecipeList.readFromBuf(var2);
               var5.setRecipes(var6);
            }
         } catch (IOException var10) {
            LOGGER.error("Couldn't load trade info", var10);
         } finally {
            var2.release();
         }
      } else if ("MC|Brand".equals(var1.getChannelName())) {
         this.gameController.player.setServerBrand(var1.getBufferData().readString(32767));
      } else if ("MC|BOpen".equals(var1.getChannelName())) {
         EnumHand var12 = (EnumHand)var1.getBufferData().readEnumValue(EnumHand.class);
         ItemStack var15 = var12 == EnumHand.OFF_HAND ? this.gameController.player.getHeldItemOffhand() : this.gameController.player.getHeldItemMainhand();
         if (var15 != null && var15.getItem() == Items.WRITTEN_BOOK) {
            this.gameController.displayGuiScreen(new GuiScreenBook(this.gameController.player, var15, false));
         }
      } else if ("MC|DebugPath".equals(var1.getChannelName())) {
         PacketBuffer var13 = var1.getBufferData();
         int var16 = var13.readInt();
         float var18 = var13.readFloat();
         Path var20 = Path.read(var13);
         ((DebugRendererPathfinding)this.gameController.debugRenderer.debugRendererPathfinding).addPath(var16, var20, var18);
      } else if ("MC|StopSound".equals(var1.getChannelName())) {
         PacketBuffer var14 = var1.getBufferData();
         String var17 = var14.readString(32767);
         String var19 = var14.readString(256);
         this.gameController.getSoundHandler().stop(var19, SoundCategory.getByName(var17));
      }

   }

   public void handleScoreboardObjective(SPacketScoreboardObjective var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      Scoreboard var2 = this.clientWorldController.getScoreboard();
      if (var1.getAction() == 0) {
         ScoreObjective var3 = var2.addScoreObjective(var1.getObjectiveName(), IScoreCriteria.DUMMY);
         var3.setDisplayName(var1.getObjectiveValue());
         var3.setRenderType(var1.getRenderType());
      } else {
         ScoreObjective var4 = var2.getObjective(var1.getObjectiveName());
         if (var1.getAction() == 1) {
            var2.removeObjective(var4);
         } else if (var1.getAction() == 2) {
            var4.setDisplayName(var1.getObjectiveValue());
            var4.setRenderType(var1.getRenderType());
         }
      }

   }

   public void handleUpdateScore(SPacketUpdateScore var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      Scoreboard var2 = this.clientWorldController.getScoreboard();
      ScoreObjective var3 = var2.getObjective(var1.getObjectiveName());
      if (var1.getScoreAction() == SPacketUpdateScore.Action.CHANGE) {
         Score var4 = var2.getOrCreateScore(var1.getPlayerName(), var3);
         var4.setScorePoints(var1.getScoreValue());
      } else if (var1.getScoreAction() == SPacketUpdateScore.Action.REMOVE) {
         if (StringUtils.isNullOrEmpty(var1.getObjectiveName())) {
            var2.removeObjectiveFromEntity(var1.getPlayerName(), (ScoreObjective)null);
         } else if (var3 != null) {
            var2.removeObjectiveFromEntity(var1.getPlayerName(), var3);
         }
      }

   }

   public void handleDisplayObjective(SPacketDisplayObjective var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      Scoreboard var2 = this.clientWorldController.getScoreboard();
      if (var1.getName().isEmpty()) {
         var2.setObjectiveInDisplaySlot(var1.getPosition(), (ScoreObjective)null);
      } else {
         ScoreObjective var3 = var2.getObjective(var1.getName());
         var2.setObjectiveInDisplaySlot(var1.getPosition(), var3);
      }

   }

   public void handleTeams(SPacketTeams var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      Scoreboard var2 = this.clientWorldController.getScoreboard();
      ScorePlayerTeam var3;
      if (var1.getAction() == 0) {
         var3 = var2.createTeam(var1.getName());
      } else {
         var3 = var2.getTeam(var1.getName());
      }

      if (var1.getAction() == 0 || var1.getAction() == 2) {
         var3.setTeamName(var1.getDisplayName());
         var3.setNamePrefix(var1.getPrefix());
         var3.setNameSuffix(var1.getSuffix());
         var3.setChatFormat(TextFormatting.fromColorIndex(var1.getColor()));
         var3.setFriendlyFlags(var1.getFriendlyFlags());
         Team.EnumVisible var4 = Team.EnumVisible.getByName(var1.getNameTagVisibility());
         if (var4 != null) {
            var3.setNameTagVisibility(var4);
         }

         Team.CollisionRule var5 = Team.CollisionRule.getByName(var1.getCollisionRule());
         if (var5 != null) {
            var3.setCollisionRule(var5);
         }
      }

      if (var1.getAction() == 0 || var1.getAction() == 3) {
         for(String var8 : var1.getPlayers()) {
            var2.addPlayerToTeam(var8, var1.getName());
         }
      }

      if (var1.getAction() == 4) {
         for(String var9 : var1.getPlayers()) {
            var2.removePlayerFromTeam(var9, var3);
         }
      }

      if (var1.getAction() == 1) {
         var2.removeTeam(var3);
      }

   }

   public void handleParticles(SPacketParticles var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      if (var1.getParticleCount() == 0) {
         double var2 = (double)(var1.getParticleSpeed() * var1.getXOffset());
         double var4 = (double)(var1.getParticleSpeed() * var1.getYOffset());
         double var6 = (double)(var1.getParticleSpeed() * var1.getZOffset());

         try {
            this.clientWorldController.spawnParticle(var1.getParticleType(), var1.isLongDistance(), var1.getXCoordinate(), var1.getYCoordinate(), var1.getZCoordinate(), var2, var4, var6, var1.getParticleArgs());
         } catch (Throwable var17) {
            LOGGER.warn("Could not spawn particle effect {}", new Object[]{var1.getParticleType()});
         }
      } else {
         for(int var18 = 0; var18 < var1.getParticleCount(); ++var18) {
            double var3 = this.avRandomizer.nextGaussian() * (double)var1.getXOffset();
            double var5 = this.avRandomizer.nextGaussian() * (double)var1.getYOffset();
            double var7 = this.avRandomizer.nextGaussian() * (double)var1.getZOffset();
            double var9 = this.avRandomizer.nextGaussian() * (double)var1.getParticleSpeed();
            double var11 = this.avRandomizer.nextGaussian() * (double)var1.getParticleSpeed();
            double var13 = this.avRandomizer.nextGaussian() * (double)var1.getParticleSpeed();

            try {
               this.clientWorldController.spawnParticle(var1.getParticleType(), var1.isLongDistance(), var1.getXCoordinate() + var3, var1.getYCoordinate() + var5, var1.getZCoordinate() + var7, var9, var11, var13, var1.getParticleArgs());
            } catch (Throwable var16) {
               LOGGER.warn("Could not spawn particle effect {}", new Object[]{var1.getParticleType()});
               return;
            }
         }
      }

   }

   public void handleEntityProperties(SPacketEntityProperties var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.gameController);
      Entity var2 = this.clientWorldController.getEntityByID(var1.getEntityId());
      if (var2 != null) {
         if (!(var2 instanceof EntityLivingBase)) {
            throw new IllegalStateException("Server tried to update attributes of a non-living entity (actually: " + var2 + ")");
         }

         AbstractAttributeMap var3 = ((EntityLivingBase)var2).getAttributeMap();

         for(SPacketEntityProperties.Snapshot var5 : var1.getSnapshots()) {
            IAttributeInstance var6 = var3.getAttributeInstanceByName(var5.getName());
            if (var6 == null) {
               var6 = var3.registerAttribute(new RangedAttribute((IAttribute)null, var5.getName(), 0.0D, 2.2250738585072014E-308D, Double.MAX_VALUE));
            }

            var6.setBaseValue(var5.getBaseValue());
            var6.removeAllModifiers();

            for(AttributeModifier var8 : var5.getModifiers()) {
               var6.applyModifier(var8);
            }
         }
      }

   }

   public NetworkManager getNetworkManager() {
      return this.netManager;
   }

   public Collection getPlayerInfoMap() {
      return this.playerInfoMap.values();
   }

   public NetworkPlayerInfo getPlayerInfo(UUID var1) {
      return (NetworkPlayerInfo)this.playerInfoMap.get(var1);
   }

   @Nullable
   public NetworkPlayerInfo getPlayerInfo(String var1) {
      for(NetworkPlayerInfo var3 : this.playerInfoMap.values()) {
         if (var3.getGameProfile().getName().equals(var1)) {
            return var3;
         }
      }

      return null;
   }

   public GameProfile getGameProfile() {
      return this.profile;
   }
}
