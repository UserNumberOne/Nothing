package net.minecraft.network;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.logging.Level;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecartCommandBlock;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.item.ItemWrittenBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketClientSettings;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.client.CPacketEnchantItem;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerAbilities;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketResourcePackStatus;
import net.minecraft.network.play.client.CPacketSpectate;
import net.minecraft.network.play.client.CPacketSteerBoat;
import net.minecraft.network.play.client.CPacketTabComplete;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketConfirmTransaction;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.network.play.server.SPacketEntityAttach;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketKeepAlive;
import net.minecraft.network.play.server.SPacketMoveVehicle;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.network.play.server.SPacketTabComplete;
import net.minecraft.src.MinecraftServer;
import net.minecraft.stats.AchievementList;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.Mirror;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandException;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.craftbukkit.v1_10_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_10_R1.block.CraftSign;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_10_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_10_R1.util.LazyPlayerSet;
import org.bukkit.craftbukkit.v1_10_R1.util.Waitable;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.Recipe;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

public class NetHandlerPlayServer implements INetHandlerPlayServer, ITickable {
   private static final Logger LOGGER = LogManager.getLogger();
   public final NetworkManager netManager;
   private final MinecraftServer serverController;
   public EntityPlayerMP playerEntity;
   private int networkTickCount;
   private int keepAliveId;
   private long lastPingTime;
   private long lastSentPingPacket;
   private volatile int chatSpamThresholdCount;
   private static final AtomicIntegerFieldUpdater chatSpamField = AtomicIntegerFieldUpdater.newUpdater(NetHandlerPlayServer.class, "chatThrottle");
   private int itemDropThreshold;
   private final IntHashMap pendingTransactions = new IntHashMap();
   private double firstGoodX;
   private double firstGoodY;
   private double firstGoodZ;
   private double lastGoodX;
   private double lastGoodY;
   private double lastGoodZ;
   private Entity lowestRiddenEnt;
   private double lowestRiddenX;
   private double lowestRiddenY;
   private double lowestRiddenZ;
   private double lowestRiddenX1;
   private double lowestRiddenY1;
   private double lowestRiddenZ1;
   private Vec3d targetPos;
   private int teleportId;
   private int lastPositionUpdate;
   private boolean floating;
   private int floatingTickCount;
   private boolean vehicleFloating;
   private int vehicleFloatingTickCount;
   private int movePacketCounter;
   private int lastMovePacketCounter;
   private boolean processedDisconnect;
   private final CraftServer server;
   private int lastTick = MinecraftServer.currentTick;
   private int allowedPlayerTicks = 1;
   private int lastDropTick = MinecraftServer.currentTick;
   private int dropCount = 0;
   private static final int SURVIVAL_PLACE_DISTANCE_SQUARED = 36;
   private static final int CREATIVE_PLACE_DISTANCE_SQUARED = 49;
   private double lastPosX = Double.MAX_VALUE;
   private double lastPosY = Double.MAX_VALUE;
   private double lastPosZ = Double.MAX_VALUE;
   private float lastPitch = Float.MAX_VALUE;
   private float lastYaw = Float.MAX_VALUE;
   private boolean justTeleported = false;
   private static final HashSet invalidItems = new HashSet(Arrays.asList(Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(10), Integer.valueOf(11), Integer.valueOf(26), Integer.valueOf(34), Integer.valueOf(36), Integer.valueOf(43), Integer.valueOf(51), Integer.valueOf(52), Integer.valueOf(55), Integer.valueOf(59), Integer.valueOf(60), Integer.valueOf(62), Integer.valueOf(63), Integer.valueOf(64), Integer.valueOf(68), Integer.valueOf(71), Integer.valueOf(74), Integer.valueOf(75), Integer.valueOf(83), Integer.valueOf(90), Integer.valueOf(92), Integer.valueOf(93), Integer.valueOf(94), Integer.valueOf(104), Integer.valueOf(105), Integer.valueOf(115), Integer.valueOf(117), Integer.valueOf(118), Integer.valueOf(119), Integer.valueOf(125), Integer.valueOf(127), Integer.valueOf(132), Integer.valueOf(140), Integer.valueOf(141), Integer.valueOf(142), Integer.valueOf(144)));
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$net$minecraft$server$PacketPlayInEntityAction$EnumPlayerAction;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$net$minecraft$server$InventoryClickType;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$org$bukkit$event$inventory$InventoryAction;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$org$bukkit$event$Event$Result;

   public NetHandlerPlayServer(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayerMP entityplayer) {
      this.serverController = minecraftserver;
      this.netManager = networkmanager;
      networkmanager.setNetHandler(this);
      this.playerEntity = entityplayer;
      entityplayer.connection = this;
      this.server = minecraftserver.server;
   }

   public CraftPlayer getPlayer() {
      return this.playerEntity == null ? null : this.playerEntity.getBukkitEntity();
   }

   public void update() {
      this.captureCurrentPosition();
      this.playerEntity.onUpdateEntity();
      this.playerEntity.setPositionAndRotation(this.firstGoodX, this.firstGoodY, this.firstGoodZ, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
      ++this.networkTickCount;
      this.lastMovePacketCounter = this.movePacketCounter;
      if (this.floating) {
         if (++this.floatingTickCount > 80) {
            LOGGER.warn("{} was kicked for floating too long!", new Object[]{this.playerEntity.getName()});
            this.disconnect("Flying is not enabled on this server");
            return;
         }
      } else {
         this.floating = false;
         this.floatingTickCount = 0;
      }

      this.lowestRiddenEnt = this.playerEntity.getLowestRidingEntity();
      if (this.lowestRiddenEnt != this.playerEntity && this.lowestRiddenEnt.getControllingPassenger() == this.playerEntity) {
         this.lowestRiddenX = this.lowestRiddenEnt.posX;
         this.lowestRiddenY = this.lowestRiddenEnt.posY;
         this.lowestRiddenZ = this.lowestRiddenEnt.posZ;
         this.lowestRiddenX1 = this.lowestRiddenEnt.posX;
         this.lowestRiddenY1 = this.lowestRiddenEnt.posY;
         this.lowestRiddenZ1 = this.lowestRiddenEnt.posZ;
         if (this.vehicleFloating && this.playerEntity.getLowestRidingEntity().getControllingPassenger() == this.playerEntity) {
            if (++this.vehicleFloatingTickCount > 80) {
               LOGGER.warn("{} was kicked for floating a vehicle too long!", new Object[]{this.playerEntity.getName()});
               this.disconnect("Flying is not enabled on this server");
               return;
            }
         } else {
            this.vehicleFloating = false;
            this.vehicleFloatingTickCount = 0;
         }
      } else {
         this.lowestRiddenEnt = null;
         this.vehicleFloating = false;
         this.vehicleFloatingTickCount = 0;
      }

      this.serverController.methodProfiler.startSection("keepAlive");
      if ((long)this.networkTickCount - this.lastSentPingPacket > 40L) {
         this.lastSentPingPacket = (long)this.networkTickCount;
         this.lastPingTime = this.currentTimeMillis();
         this.keepAliveId = (int)this.lastPingTime;
         this.sendPacket(new SPacketKeepAlive(this.keepAliveId));
      }

      this.serverController.methodProfiler.endSection();

      while(true) {
         int spam = this.chatSpamThresholdCount;
         if (this.chatSpamThresholdCount <= 0 || chatSpamField.compareAndSet(this, spam, spam - 1)) {
            break;
         }
      }

      if (this.itemDropThreshold > 0) {
         --this.itemDropThreshold;
      }

      if (this.playerEntity.getLastActiveTime() > 0L && this.serverController.getIdleTimeout() > 0 && MinecraftServer.av() - this.playerEntity.getLastActiveTime() > (long)(this.serverController.getIdleTimeout() * 1000 * 60)) {
         this.playerEntity.markPlayerActive();
         this.disconnect("You have been idle for too long!");
      }

   }

   public void captureCurrentPosition() {
      this.firstGoodX = this.playerEntity.posX;
      this.firstGoodY = this.playerEntity.posY;
      this.firstGoodZ = this.playerEntity.posZ;
      this.lastGoodX = this.playerEntity.posX;
      this.lastGoodY = this.playerEntity.posY;
      this.lastGoodZ = this.playerEntity.posZ;
   }

   public NetworkManager getNetworkManager() {
      return this.netManager;
   }

   public void disconnect(String s) {
      if (!this.processedDisconnect) {
         String leaveMessage = TextFormatting.YELLOW + this.playerEntity.getName() + " left the game.";
         PlayerKickEvent event = new PlayerKickEvent(this.server.getPlayer(this.playerEntity), s, leaveMessage);
         if (this.server.getServer().isRunning()) {
            this.server.getPluginManager().callEvent(event);
         }

         if (!event.isCancelled()) {
            s = event.getReason();
            final TextComponentString chatcomponenttext = new TextComponentString(s);
            this.netManager.sendPacket(new SPacketDisconnect(chatcomponenttext), new GenericFutureListener() {
               public void operationComplete(Future future) throws Exception {
                  NetHandlerPlayServer.this.netManager.closeChannel(chatcomponenttext);
               }
            });
            this.onDisconnect(chatcomponenttext);
            this.netManager.disableAutoRead();
            this.serverController.addScheduledTask(new Runnable() {
               public void run() {
                  NetHandlerPlayServer.this.netManager.checkDisconnected();
               }
            });
         }
      }
   }

   public void processInput(CPacketInput packetplayinsteervehicle) {
      PacketThreadUtil.checkThreadAndEnqueue(packetplayinsteervehicle, this, this.playerEntity.getServerWorld());
      this.playerEntity.setEntityActionState(packetplayinsteervehicle.getStrafeSpeed(), packetplayinsteervehicle.getForwardSpeed(), packetplayinsteervehicle.isJumping(), packetplayinsteervehicle.isSneaking());
   }

   private static boolean isMovePlayerPacketInvalid(CPacketPlayer packetplayinflying) {
      return Doubles.isFinite(packetplayinflying.getX(0.0D)) && Doubles.isFinite(packetplayinflying.getY(0.0D)) && Doubles.isFinite(packetplayinflying.getZ(0.0D)) && Floats.isFinite(packetplayinflying.getPitch(0.0F)) && Floats.isFinite(packetplayinflying.getYaw(0.0F)) ? false : Math.abs(packetplayinflying.getX(0.0D)) <= 3.0E7D && Math.abs(packetplayinflying.getX(0.0D)) <= 3.0E7D;
   }

   private static boolean isMoveVehiclePacketInvalid(CPacketVehicleMove packetplayinvehiclemove) {
      return !Doubles.isFinite(packetplayinvehiclemove.getX()) || !Doubles.isFinite(packetplayinvehiclemove.getY()) || !Doubles.isFinite(packetplayinvehiclemove.getZ()) || !Floats.isFinite(packetplayinvehiclemove.getPitch()) || !Floats.isFinite(packetplayinvehiclemove.getYaw());
   }

   public void processVehicleMove(CPacketVehicleMove packetplayinvehiclemove) {
      PacketThreadUtil.checkThreadAndEnqueue(packetplayinvehiclemove, this, this.playerEntity.getServerWorld());
      if (isMoveVehiclePacketInvalid(packetplayinvehiclemove)) {
         this.disconnect("Invalid move vehicle packet received");
      } else {
         Entity entity = this.playerEntity.getLowestRidingEntity();
         if (entity != this.playerEntity && entity.getControllingPassenger() == this.playerEntity && entity == this.lowestRiddenEnt) {
            WorldServer worldserver = this.playerEntity.getServerWorld();
            double d0 = entity.posX;
            double d1 = entity.posY;
            double d2 = entity.posZ;
            double d3 = packetplayinvehiclemove.getX();
            double d4 = packetplayinvehiclemove.getY();
            double d5 = packetplayinvehiclemove.getZ();
            float f = packetplayinvehiclemove.getYaw();
            float f1 = packetplayinvehiclemove.getPitch();
            double d6 = d3 - this.lowestRiddenX;
            double d7 = d4 - this.lowestRiddenY;
            double d8 = d5 - this.lowestRiddenZ;
            double d9 = entity.motionX * entity.motionX + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ;
            double d10 = d6 * d6 + d7 * d7 + d8 * d8;
            this.allowedPlayerTicks = (int)((long)this.allowedPlayerTicks + (System.currentTimeMillis() / 50L - (long)this.lastTick));
            this.allowedPlayerTicks = Math.max(this.allowedPlayerTicks, 1);
            this.lastTick = (int)(System.currentTimeMillis() / 50L);
            ++this.movePacketCounter;
            int i = this.movePacketCounter - this.lastMovePacketCounter;
            if (i > Math.max(this.allowedPlayerTicks, 5)) {
               LOGGER.debug(this.playerEntity.getName() + " is sending move packets too frequently (" + i + " packets since last tick)");
               i = 1;
            }

            if (d10 > 0.0D) {
               --this.allowedPlayerTicks;
            } else {
               this.allowedPlayerTicks = 20;
            }

            float speed;
            if (this.playerEntity.capabilities.isFlying) {
               speed = this.playerEntity.capabilities.flySpeed * 20.0F;
            } else {
               speed = this.playerEntity.capabilities.walkSpeed * 10.0F;
            }

            speed = speed * 2.0F;
            if (d10 - d9 > Math.max(100.0D, Math.pow((double)(10.0F * (float)i * speed), 2.0D)) && (!this.serverController.R() || !this.serverController.Q().equals(entity.getName()))) {
               LOGGER.warn("{} (vehicle of {}) moved too quickly! {},{},{}", new Object[]{entity.getName(), this.playerEntity.getName(), d6, d7, d8});
               this.netManager.sendPacket(new SPacketMoveVehicle(entity));
               return;
            }

            boolean flag = worldserver.getCollisionBoxes(entity, entity.getEntityBoundingBox().contract(0.0625D)).isEmpty();
            d6 = d3 - this.lowestRiddenX1;
            d7 = d4 - this.lowestRiddenY1 - 1.0E-6D;
            d8 = d5 - this.lowestRiddenZ1;
            entity.move(d6, d7, d8);
            d6 = d3 - entity.posX;
            d7 = d4 - entity.posY;
            if (d7 > -0.5D || d7 < 0.5D) {
               d7 = 0.0D;
            }

            d8 = d5 - entity.posZ;
            d10 = d6 * d6 + d7 * d7 + d8 * d8;
            boolean flag1 = false;
            if (d10 > 0.0625D) {
               flag1 = true;
               LOGGER.warn("{} moved wrongly!", new Object[]{entity.getName()});
            }

            entity.setPositionAndRotation(d3, d4, d5, f, f1);
            boolean flag2 = worldserver.getCollisionBoxes(entity, entity.getEntityBoundingBox().contract(0.0625D)).isEmpty();
            if (flag && (flag1 || !flag2)) {
               entity.setPositionAndRotation(d0, d1, d2, f, f1);
               this.netManager.sendPacket(new SPacketMoveVehicle(entity));
               return;
            }

            Player player = this.getPlayer();
            Location from = new Location(player.getWorld(), this.lastPosX, this.lastPosY, this.lastPosZ, this.lastYaw, this.lastPitch);
            Location to = player.getLocation().clone();
            to.setX(packetplayinvehiclemove.getX());
            to.setY(packetplayinvehiclemove.getY());
            to.setZ(packetplayinvehiclemove.getZ());
            to.setYaw(packetplayinvehiclemove.getYaw());
            to.setPitch(packetplayinvehiclemove.getPitch());
            double delta = Math.pow(this.lastPosX - to.getX(), 2.0D) + Math.pow(this.lastPosY - to.getY(), 2.0D) + Math.pow(this.lastPosZ - to.getZ(), 2.0D);
            float deltaAngle = Math.abs(this.lastYaw - to.getYaw()) + Math.abs(this.lastPitch - to.getPitch());
            if ((delta > 0.00390625D || deltaAngle > 10.0F) && !this.playerEntity.isMovementBlocked()) {
               this.lastPosX = to.getX();
               this.lastPosY = to.getY();
               this.lastPosZ = to.getZ();
               this.lastYaw = to.getYaw();
               this.lastPitch = to.getPitch();
               if (from.getX() != Double.MAX_VALUE) {
                  Location oldTo = to.clone();
                  PlayerMoveEvent event = new PlayerMoveEvent(player, from, to);
                  this.server.getPluginManager().callEvent(event);
                  if (event.isCancelled()) {
                     this.teleport(from);
                     return;
                  }

                  if (!oldTo.equals(event.getTo()) && !event.isCancelled()) {
                     this.playerEntity.getBukkitEntity().teleport(event.getTo(), TeleportCause.UNKNOWN);
                     return;
                  }

                  if (!from.equals(this.getPlayer().getLocation()) && this.justTeleported) {
                     this.justTeleported = false;
                     return;
                  }
               }
            }

            this.serverController.getPlayerList().serverUpdateMovingPlayer(this.playerEntity);
            this.playerEntity.addMovementStat(this.playerEntity.posX - d0, this.playerEntity.posY - d1, this.playerEntity.posZ - d2);
            this.vehicleFloating = d7 >= -0.03125D && !this.serverController.getAllowFlight() && !worldserver.checkBlockCollision(entity.getEntityBoundingBox().expandXyz(0.0625D).addCoord(0.0D, -0.55D, 0.0D));
            this.lowestRiddenX1 = entity.posX;
            this.lowestRiddenY1 = entity.posY;
            this.lowestRiddenZ1 = entity.posZ;
         }
      }

   }

   public void processConfirmTeleport(CPacketConfirmTeleport packetplayinteleportaccept) {
      PacketThreadUtil.checkThreadAndEnqueue(packetplayinteleportaccept, this, this.playerEntity.getServerWorld());
      if (packetplayinteleportaccept.getTeleportId() == this.teleportId) {
         this.playerEntity.setPositionAndRotation(this.targetPos.xCoord, this.targetPos.yCoord, this.targetPos.zCoord, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
         if (this.playerEntity.isInvulnerableDimensionChange()) {
            this.lastGoodX = this.targetPos.xCoord;
            this.lastGoodY = this.targetPos.yCoord;
            this.lastGoodZ = this.targetPos.zCoord;
            this.playerEntity.clearInvulnerableDimensionChange();
         }

         this.targetPos = null;
      }

   }

   public void processPlayer(CPacketPlayer packetplayinflying) {
      PacketThreadUtil.checkThreadAndEnqueue(packetplayinflying, this, this.playerEntity.getServerWorld());
      if (isMovePlayerPacketInvalid(packetplayinflying)) {
         this.disconnect("Invalid move player packet received");
      } else {
         WorldServer worldserver = this.serverController.getWorldServer(this.playerEntity.dimension);
         if (!this.playerEntity.playerConqueredTheEnd && !this.playerEntity.isMovementBlocked()) {
            if (this.networkTickCount == 0) {
               this.captureCurrentPosition();
            }

            if (this.targetPos != null) {
               if (this.networkTickCount - this.lastPositionUpdate > 20) {
                  this.lastPositionUpdate = this.networkTickCount;
                  this.setPlayerLocation(this.targetPos.xCoord, this.targetPos.yCoord, this.targetPos.zCoord, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
               }

               this.allowedPlayerTicks = 20;
            } else {
               this.lastPositionUpdate = this.networkTickCount;
               if (this.playerEntity.isRiding()) {
                  this.playerEntity.setPositionAndRotation(this.playerEntity.posX, this.playerEntity.posY, this.playerEntity.posZ, packetplayinflying.getYaw(this.playerEntity.rotationYaw), packetplayinflying.getPitch(this.playerEntity.rotationPitch));
                  this.serverController.getPlayerList().serverUpdateMovingPlayer(this.playerEntity);
                  this.allowedPlayerTicks = 20;
               } else {
                  double prevX = this.playerEntity.posX;
                  double prevY = this.playerEntity.posY;
                  double prevZ = this.playerEntity.posZ;
                  float prevYaw = this.playerEntity.rotationYaw;
                  float prevPitch = this.playerEntity.rotationPitch;
                  double d0 = this.playerEntity.posX;
                  double d1 = this.playerEntity.posY;
                  double d2 = this.playerEntity.posZ;
                  double d3 = this.playerEntity.posY;
                  double d4 = packetplayinflying.getX(this.playerEntity.posX);
                  double d5 = packetplayinflying.getY(this.playerEntity.posY);
                  double d6 = packetplayinflying.getZ(this.playerEntity.posZ);
                  float f = packetplayinflying.getYaw(this.playerEntity.rotationYaw);
                  float f1 = packetplayinflying.getPitch(this.playerEntity.rotationPitch);
                  double d7 = d4 - this.firstGoodX;
                  double d8 = d5 - this.firstGoodY;
                  double d9 = d6 - this.firstGoodZ;
                  double d10 = this.playerEntity.motionX * this.playerEntity.motionX + this.playerEntity.motionY * this.playerEntity.motionY + this.playerEntity.motionZ * this.playerEntity.motionZ;
                  double d11 = d7 * d7 + d8 * d8 + d9 * d9;
                  ++this.movePacketCounter;
                  int i = this.movePacketCounter - this.lastMovePacketCounter;
                  this.allowedPlayerTicks = (int)((long)this.allowedPlayerTicks + (System.currentTimeMillis() / 50L - (long)this.lastTick));
                  this.allowedPlayerTicks = Math.max(this.allowedPlayerTicks, 1);
                  this.lastTick = (int)(System.currentTimeMillis() / 50L);
                  if (i > Math.max(this.allowedPlayerTicks, 5)) {
                     LOGGER.debug(this.playerEntity.getName() + " is sending move packets too frequently (" + i + " packets since last tick)");
                     i = 1;
                  }

                  if (!packetplayinflying.rotating && d11 <= 0.0D) {
                     this.allowedPlayerTicks = 20;
                  } else {
                     --this.allowedPlayerTicks;
                  }

                  float speed;
                  if (this.playerEntity.capabilities.isFlying) {
                     speed = this.playerEntity.capabilities.flySpeed * 20.0F;
                  } else {
                     speed = this.playerEntity.capabilities.walkSpeed * 10.0F;
                  }

                  if (!this.playerEntity.isInvulnerableDimensionChange() && (!this.playerEntity.getServerWorld().getGameRules().getBoolean("disableElytraMovementCheck") || !this.playerEntity.isElytraFlying())) {
                     if (this.playerEntity.isElytraFlying()) {
                        ;
                     }

                     if (d11 - d10 > Math.max(100.0D, Math.pow((double)(10.0F * (float)i * speed), 2.0D)) && (!this.serverController.R() || !this.serverController.Q().equals(this.playerEntity.getName()))) {
                        LOGGER.warn("{} moved too quickly! {},{},{}", new Object[]{this.playerEntity.getName(), d7, d8, d9});
                        this.setPlayerLocation(this.playerEntity.posX, this.playerEntity.posY, this.playerEntity.posZ, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
                        return;
                     }
                  }

                  boolean flag = worldserver.getCollisionBoxes(this.playerEntity, this.playerEntity.getEntityBoundingBox().contract(0.0625D)).isEmpty();
                  d7 = d4 - this.lastGoodX;
                  d8 = d5 - this.lastGoodY;
                  d9 = d6 - this.lastGoodZ;
                  if (this.playerEntity.onGround && !packetplayinflying.isOnGround() && d8 > 0.0D) {
                     this.playerEntity.jump();
                  }

                  this.playerEntity.move(d7, d8, d9);
                  this.playerEntity.onGround = packetplayinflying.isOnGround();
                  d7 = d4 - this.playerEntity.posX;
                  d8 = d5 - this.playerEntity.posY;
                  if (d8 > -0.5D || d8 < 0.5D) {
                     d8 = 0.0D;
                  }

                  d9 = d6 - this.playerEntity.posZ;
                  d11 = d7 * d7 + d8 * d8 + d9 * d9;
                  boolean flag1 = false;
                  if (!this.playerEntity.isInvulnerableDimensionChange() && d11 > 0.0625D && !this.playerEntity.isPlayerSleeping() && !this.playerEntity.interactionManager.isCreative() && this.playerEntity.interactionManager.getGameType() != GameType.SPECTATOR) {
                     flag1 = true;
                     LOGGER.warn("{} moved wrongly!", new Object[]{this.playerEntity.getName()});
                  }

                  this.playerEntity.setPositionAndRotation(d4, d5, d6, f, f1);
                  this.playerEntity.addMovementStat(this.playerEntity.posX - d0, this.playerEntity.posY - d1, this.playerEntity.posZ - d2);
                  if (!this.playerEntity.noClip && !this.playerEntity.isPlayerSleeping()) {
                     boolean flag2 = worldserver.getCollisionBoxes(this.playerEntity, this.playerEntity.getEntityBoundingBox().contract(0.0625D)).isEmpty();
                     if (flag && (flag1 || !flag2)) {
                        this.setPlayerLocation(d0, d1, d2, f, f1);
                        return;
                     }
                  }

                  this.playerEntity.setPositionAndRotation(prevX, prevY, prevZ, prevYaw, prevPitch);
                  Player player = this.getPlayer();
                  Location from = new Location(player.getWorld(), this.lastPosX, this.lastPosY, this.lastPosZ, this.lastYaw, this.lastPitch);
                  Location to = player.getLocation().clone();
                  if (packetplayinflying.moving) {
                     to.setX(packetplayinflying.x);
                     to.setY(packetplayinflying.y);
                     to.setZ(packetplayinflying.z);
                  }

                  if (packetplayinflying.rotating) {
                     to.setYaw(packetplayinflying.yaw);
                     to.setPitch(packetplayinflying.pitch);
                  }

                  double delta = Math.pow(this.lastPosX - to.getX(), 2.0D) + Math.pow(this.lastPosY - to.getY(), 2.0D) + Math.pow(this.lastPosZ - to.getZ(), 2.0D);
                  float deltaAngle = Math.abs(this.lastYaw - to.getYaw()) + Math.abs(this.lastPitch - to.getPitch());
                  if ((delta > 0.00390625D || deltaAngle > 10.0F) && !this.playerEntity.isMovementBlocked()) {
                     this.lastPosX = to.getX();
                     this.lastPosY = to.getY();
                     this.lastPosZ = to.getZ();
                     this.lastYaw = to.getYaw();
                     this.lastPitch = to.getPitch();
                     if (from.getX() != Double.MAX_VALUE) {
                        Location oldTo = to.clone();
                        PlayerMoveEvent event = new PlayerMoveEvent(player, from, to);
                        this.server.getPluginManager().callEvent(event);
                        if (event.isCancelled()) {
                           this.teleport(from);
                           return;
                        }

                        if (!oldTo.equals(event.getTo()) && !event.isCancelled()) {
                           this.playerEntity.getBukkitEntity().teleport(event.getTo(), TeleportCause.UNKNOWN);
                           return;
                        }

                        if (!from.equals(this.getPlayer().getLocation()) && this.justTeleported) {
                           this.justTeleported = false;
                           return;
                        }
                     }
                  }

                  this.playerEntity.setPositionAndRotation(d4, d5, d6, f, f1);
                  this.floating = d8 >= -0.03125D;
                  this.floating &= !this.serverController.getAllowFlight() && !this.playerEntity.capabilities.allowFlying;
                  this.floating &= !this.playerEntity.isPotionActive(MobEffects.LEVITATION) && !this.playerEntity.isElytraFlying() && !worldserver.checkBlockCollision(this.playerEntity.getEntityBoundingBox().expandXyz(0.0625D).addCoord(0.0D, -0.55D, 0.0D));
                  this.playerEntity.onGround = packetplayinflying.isOnGround();
                  this.serverController.getPlayerList().serverUpdateMovingPlayer(this.playerEntity);
                  this.playerEntity.handleFalling(this.playerEntity.posY - d3, packetplayinflying.isOnGround());
                  this.lastGoodX = this.playerEntity.posX;
                  this.lastGoodY = this.playerEntity.posY;
                  this.lastGoodZ = this.playerEntity.posZ;
               }
            }
         }
      }

   }

   public void setPlayerLocation(double d0, double d1, double d2, float f, float f1) {
      this.setPlayerLocation(d0, d1, d2, f, f1, Collections.emptySet());
   }

   public void setPlayerLocation(double d0, double d1, double d2, float f, float f1, Set set) {
      Player player = this.getPlayer();
      Location from = player.getLocation();
      double x = d0;
      double y = d1;
      double z = d2;
      float yaw = f;
      float pitch = f1;
      if (set.contains(SPacketPlayerPosLook.EnumFlags.X)) {
         x = d0 + from.getX();
      }

      if (set.contains(SPacketPlayerPosLook.EnumFlags.Y)) {
         y = d1 + from.getY();
      }

      if (set.contains(SPacketPlayerPosLook.EnumFlags.Z)) {
         z = d2 + from.getZ();
      }

      if (set.contains(SPacketPlayerPosLook.EnumFlags.Y_ROT)) {
         yaw = f + from.getYaw();
      }

      if (set.contains(SPacketPlayerPosLook.EnumFlags.X_ROT)) {
         pitch = f1 + from.getPitch();
      }

      Location to = new Location(this.getPlayer().getWorld(), x, y, z, yaw, pitch);
      PlayerTeleportEvent event = new PlayerTeleportEvent(player, from.clone(), to.clone(), TeleportCause.UNKNOWN);
      this.server.getPluginManager().callEvent(event);
      if (event.isCancelled() || !to.equals(event.getTo())) {
         set.clear();
         to = event.isCancelled() ? event.getFrom() : event.getTo();
         d0 = to.getX();
         d1 = to.getY();
         d2 = to.getZ();
         f = to.getYaw();
         f1 = to.getPitch();
      }

      this.internalTeleport(d0, d1, d2, f, f1, set);
   }

   public void teleport(Location dest) {
      this.internalTeleport(dest.getX(), dest.getY(), dest.getZ(), dest.getYaw(), dest.getPitch(), Collections.emptySet());
   }

   private void internalTeleport(double d0, double d1, double d2, float f, float f1, Set set) {
      if (Float.isNaN(f)) {
         f = 0.0F;
      }

      if (Float.isNaN(f1)) {
         f1 = 0.0F;
      }

      this.justTeleported = true;
      this.targetPos = new Vec3d(d0, d1, d2);
      if (set.contains(SPacketPlayerPosLook.EnumFlags.X)) {
         this.targetPos = this.targetPos.addVector(this.playerEntity.posX, 0.0D, 0.0D);
      }

      if (set.contains(SPacketPlayerPosLook.EnumFlags.Y)) {
         this.targetPos = this.targetPos.addVector(0.0D, this.playerEntity.posY, 0.0D);
      }

      if (set.contains(SPacketPlayerPosLook.EnumFlags.Z)) {
         this.targetPos = this.targetPos.addVector(0.0D, 0.0D, this.playerEntity.posZ);
      }

      float f2 = f;
      float f3 = f1;
      if (set.contains(SPacketPlayerPosLook.EnumFlags.Y_ROT)) {
         f2 = f + this.playerEntity.rotationYaw;
      }

      if (set.contains(SPacketPlayerPosLook.EnumFlags.X_ROT)) {
         f3 = f1 + this.playerEntity.rotationPitch;
      }

      this.lastPosX = this.targetPos.xCoord;
      this.lastPosY = this.targetPos.yCoord;
      this.lastPosZ = this.targetPos.zCoord;
      this.lastYaw = f2;
      this.lastPitch = f3;
      if (++this.teleportId == Integer.MAX_VALUE) {
         this.teleportId = 0;
      }

      this.lastPositionUpdate = this.networkTickCount;
      this.playerEntity.setPositionAndRotation(this.targetPos.xCoord, this.targetPos.yCoord, this.targetPos.zCoord, f2, f3);
      this.playerEntity.connection.sendPacket(new SPacketPlayerPosLook(d0, d1, d2, f, f1, set, this.teleportId));
   }

   public void processPlayerDigging(CPacketPlayerDigging packetplayinblockdig) {
      PacketThreadUtil.checkThreadAndEnqueue(packetplayinblockdig, this, this.playerEntity.getServerWorld());
      if (!this.playerEntity.isMovementBlocked()) {
         WorldServer worldserver = this.serverController.getWorldServer(this.playerEntity.dimension);
         BlockPos blockposition = packetplayinblockdig.getPosition();
         this.playerEntity.markPlayerActive();
         switch(NetHandlerPlayServer.SyntheticClass_1.a[packetplayinblockdig.getAction().ordinal()]) {
         case 1:
            if (!this.playerEntity.isSpectator()) {
               PlayerSwapHandItemsEvent swapItemsEvent = new PlayerSwapHandItemsEvent(this.getPlayer(), CraftItemStack.asBukkitCopy(this.playerEntity.getHeldItem(EnumHand.OFF_HAND)), CraftItemStack.asBukkitCopy(this.playerEntity.getHeldItem(EnumHand.MAIN_HAND)));
               this.server.getPluginManager().callEvent(swapItemsEvent);
               if (swapItemsEvent.isCancelled()) {
                  return;
               }

               ItemStack itemstack = CraftItemStack.asNMSCopy(swapItemsEvent.getMainHandItem());
               this.playerEntity.setHeldItem(EnumHand.OFF_HAND, CraftItemStack.asNMSCopy(swapItemsEvent.getOffHandItem()));
               this.playerEntity.setHeldItem(EnumHand.MAIN_HAND, itemstack);
            }

            return;
         case 2:
            if (!this.playerEntity.isSpectator()) {
               if (this.lastDropTick != MinecraftServer.currentTick) {
                  this.dropCount = 0;
                  this.lastDropTick = MinecraftServer.currentTick;
               } else {
                  ++this.dropCount;
                  if (this.dropCount >= 20) {
                     LOGGER.warn(this.playerEntity.getName() + " dropped their items too quickly!");
                     this.disconnect("You dropped your items too quickly (Hacking?)");
                     return;
                  }
               }

               this.playerEntity.dropItem(false);
            }

            return;
         case 3:
            if (!this.playerEntity.isSpectator()) {
               this.playerEntity.dropItem(true);
            }

            return;
         case 4:
            this.playerEntity.stopActiveHand();
            ItemStack itemstack = this.playerEntity.getHeldItemMainhand();
            if (itemstack != null && itemstack.stackSize == 0) {
               this.playerEntity.setHeldItem(EnumHand.MAIN_HAND, (ItemStack)null);
            }

            return;
         case 5:
         case 6:
         case 7:
            double d0 = this.playerEntity.posX - ((double)blockposition.getX() + 0.5D);
            double d1 = this.playerEntity.posY - ((double)blockposition.getY() + 0.5D) + 1.5D;
            double d2 = this.playerEntity.posZ - ((double)blockposition.getZ() + 0.5D);
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;
            if (d3 > 36.0D) {
               return;
            } else if (blockposition.getY() >= this.serverController.getMaxBuildHeight()) {
               return;
            } else {
               if (packetplayinblockdig.getAction() == CPacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                  if (!this.serverController.a(worldserver, blockposition, this.playerEntity) && worldserver.getWorldBorder().contains(blockposition)) {
                     this.playerEntity.interactionManager.onBlockClicked(blockposition, packetplayinblockdig.getFacing());
                  } else {
                     CraftEventFactory.callPlayerInteractEvent(this.playerEntity, Action.LEFT_CLICK_BLOCK, blockposition, packetplayinblockdig.getFacing(), this.playerEntity.inventory.getCurrentItem(), EnumHand.MAIN_HAND);
                     this.playerEntity.connection.sendPacket(new SPacketBlockChange(worldserver, blockposition));
                     TileEntity tileentity = worldserver.getTileEntity(blockposition);
                     if (tileentity != null) {
                        this.playerEntity.connection.sendPacket(tileentity.getUpdatePacket());
                     }
                  }
               } else {
                  if (packetplayinblockdig.getAction() == CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
                     this.playerEntity.interactionManager.blockRemoving(blockposition);
                  } else if (packetplayinblockdig.getAction() == CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK) {
                     this.playerEntity.interactionManager.cancelDestroyingBlock();
                  }

                  if (worldserver.getBlockState(blockposition).getMaterial() != Material.AIR) {
                     this.playerEntity.connection.sendPacket(new SPacketBlockChange(worldserver, blockposition));
                  }
               }

               return;
            }
         default:
            throw new IllegalArgumentException("Invalid player action");
         }
      }
   }

   public void processTryUseItemOnBlock(CPacketPlayerTryUseItemOnBlock packetplayinuseitem) {
      PacketThreadUtil.checkThreadAndEnqueue(packetplayinuseitem, this, this.playerEntity.getServerWorld());
      if (!this.playerEntity.isMovementBlocked()) {
         WorldServer worldserver = this.serverController.getWorldServer(this.playerEntity.dimension);
         EnumHand enumhand = packetplayinuseitem.getHand();
         ItemStack itemstack = this.playerEntity.getHeldItem(enumhand);
         BlockPos blockposition = packetplayinuseitem.getPos();
         EnumFacing enumdirection = packetplayinuseitem.getDirection();
         this.playerEntity.markPlayerActive();
         if (blockposition.getY() < this.serverController.getMaxBuildHeight() - 1 || enumdirection != EnumFacing.UP && blockposition.getY() < this.serverController.getMaxBuildHeight()) {
            if (this.targetPos == null && this.playerEntity.getDistanceSq((double)blockposition.getX() + 0.5D, (double)blockposition.getY() + 0.5D, (double)blockposition.getZ() + 0.5D) < 64.0D && !this.serverController.a(worldserver, blockposition, this.playerEntity) && worldserver.getWorldBorder().contains(blockposition)) {
               Location eyeLoc = this.getPlayer().getEyeLocation();
               double reachDistance = NumberConversions.square(eyeLoc.getX() - (double)blockposition.getX()) + NumberConversions.square(eyeLoc.getY() - (double)blockposition.getY()) + NumberConversions.square(eyeLoc.getZ() - (double)blockposition.getZ());
               if (reachDistance > (double)(this.getPlayer().getGameMode() == GameMode.CREATIVE ? 49 : 36)) {
                  return;
               }

               this.playerEntity.interactionManager.processRightClickBlock(this.playerEntity, worldserver, itemstack, enumhand, blockposition, enumdirection, packetplayinuseitem.getFacingX(), packetplayinuseitem.getFacingY(), packetplayinuseitem.getFacingZ());
            }
         } else {
            TextComponentTranslation chatmessage = new TextComponentTranslation("build.tooHigh", new Object[]{this.serverController.getMaxBuildHeight()});
            chatmessage.getStyle().setColor(TextFormatting.RED);
            this.playerEntity.connection.sendPacket(new SPacketChat(chatmessage));
         }

         this.playerEntity.connection.sendPacket(new SPacketBlockChange(worldserver, blockposition));
         this.playerEntity.connection.sendPacket(new SPacketBlockChange(worldserver, blockposition.offset(enumdirection)));
         itemstack = this.playerEntity.getHeldItem(enumhand);
         if (itemstack != null && itemstack.stackSize == 0) {
            this.playerEntity.setHeldItem(enumhand, (ItemStack)null);
         }

      }
   }

   public void processTryUseItem(CPacketPlayerTryUseItem packetplayinblockplace) {
      PacketThreadUtil.checkThreadAndEnqueue(packetplayinblockplace, this, this.playerEntity.getServerWorld());
      if (!this.playerEntity.isMovementBlocked()) {
         WorldServer worldserver = this.serverController.getWorldServer(this.playerEntity.dimension);
         EnumHand enumhand = packetplayinblockplace.getHand();
         ItemStack itemstack = this.playerEntity.getHeldItem(enumhand);
         this.playerEntity.markPlayerActive();
         if (itemstack != null) {
            float f1 = this.playerEntity.rotationPitch;
            float f2 = this.playerEntity.rotationYaw;
            double d0 = this.playerEntity.posX;
            double d1 = this.playerEntity.posY + (double)this.playerEntity.getEyeHeight();
            double d2 = this.playerEntity.posZ;
            Vec3d vec3d = new Vec3d(d0, d1, d2);
            float f3 = MathHelper.cos(-f2 * 0.017453292F - 3.1415927F);
            float f4 = MathHelper.sin(-f2 * 0.017453292F - 3.1415927F);
            float f5 = -MathHelper.cos(-f1 * 0.017453292F);
            float f6 = MathHelper.sin(-f1 * 0.017453292F);
            float f7 = f4 * f5;
            float f8 = f3 * f5;
            double d3 = this.playerEntity.interactionManager.getGameType() == GameType.CREATIVE ? 5.0D : 4.5D;
            Vec3d vec3d1 = vec3d.addVector((double)f7 * d3, (double)f6 * d3, (double)f8 * d3);
            RayTraceResult movingobjectposition = this.playerEntity.world.rayTraceBlocks(vec3d, vec3d1, false);
            boolean cancelled = false;
            if (movingobjectposition != null && movingobjectposition.typeOfHit == RayTraceResult.Type.BLOCK) {
               if (this.playerEntity.interactionManager.firedInteract) {
                  this.playerEntity.interactionManager.firedInteract = false;
                  cancelled = this.playerEntity.interactionManager.interactResult;
               } else {
                  PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(this.playerEntity, Action.RIGHT_CLICK_BLOCK, movingobjectposition.getBlockPos(), movingobjectposition.sideHit, itemstack, true, enumhand);
                  cancelled = event.useItemInHand() == Result.DENY;
               }
            } else {
               PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(this.playerEntity, Action.RIGHT_CLICK_AIR, itemstack, enumhand);
               cancelled = event.useItemInHand() == Result.DENY;
            }

            if (!cancelled) {
               this.playerEntity.interactionManager.processRightClick(this.playerEntity, worldserver, itemstack, enumhand);
               itemstack = this.playerEntity.getHeldItem(enumhand);
               if (itemstack != null && itemstack.stackSize == 0) {
                  this.playerEntity.setHeldItem(enumhand, (ItemStack)null);
                  itemstack = null;
               }
            }
         }

      }
   }

   public void handleSpectate(CPacketSpectate packetplayinspectate) {
      PacketThreadUtil.checkThreadAndEnqueue(packetplayinspectate, this, this.playerEntity.getServerWorld());
      if (this.playerEntity.isSpectator()) {
         Entity entity = null;
         WorldServer[] aworldserver = this.serverController.worldServer;
         int var10000 = aworldserver.length;

         for(WorldServer worldserver : this.serverController.worlds) {
            if (worldserver != null) {
               entity = packetplayinspectate.getEntity(worldserver);
               if (entity != null) {
                  break;
               }
            }
         }

         if (entity != null) {
            this.playerEntity.setSpectatingEntity(this.playerEntity);
            this.playerEntity.dismountRidingEntity();
            this.playerEntity.getBukkitEntity().teleport(entity.getBukkitEntity(), TeleportCause.SPECTATE);
         }
      }

   }

   public void handleResourcePackStatus(CPacketResourcePackStatus packetplayinresourcepackstatus) {
      this.server.getPluginManager().callEvent(new PlayerResourcePackStatusEvent(this.getPlayer(), Status.values()[packetplayinresourcepackstatus.action.ordinal()]));
   }

   public void processSteerBoat(CPacketSteerBoat packetplayinboatmove) {
      PacketThreadUtil.checkThreadAndEnqueue(packetplayinboatmove, this, this.playerEntity.getServerWorld());
      Entity entity = this.playerEntity.getRidingEntity();
      if (entity instanceof EntityBoat) {
         ((EntityBoat)entity).setPaddleState(packetplayinboatmove.getLeft(), packetplayinboatmove.getRight());
      }

   }

   public void onDisconnect(ITextComponent ichatbasecomponent) {
      if (!this.processedDisconnect) {
         this.processedDisconnect = true;
         LOGGER.info("{} lost connection: {}", new Object[]{this.playerEntity.getName(), ichatbasecomponent.getUnformattedText()});
         this.playerEntity.mountEntityAndWakeUp();
         String quitMessage = this.serverController.getPlayerList().disconnect(this.playerEntity);
         if (quitMessage != null && quitMessage.length() > 0) {
            this.serverController.getPlayerList().sendMessage(CraftChatMessage.fromString(quitMessage));
         }

         if (this.serverController.R() && this.playerEntity.getName().equals(this.serverController.Q())) {
            LOGGER.info("Stopping singleplayer server as player logged out");
            this.serverController.safeShutdown();
         }

      }
   }

   public void sendPacket(final Packet packet) {
      if (packet instanceof SPacketChat) {
         SPacketChat packetplayoutchat = (SPacketChat)packet;
         EntityPlayer.EnumChatVisibility entityhuman_enumchatvisibility = this.playerEntity.getChatVisibility();
         if (entityhuman_enumchatvisibility == EntityPlayer.EnumChatVisibility.HIDDEN) {
            return;
         }

         if (entityhuman_enumchatvisibility == EntityPlayer.EnumChatVisibility.SYSTEM && !packetplayoutchat.isSystem()) {
            return;
         }
      }

      if (packet != null) {
         if (packet instanceof SPacketSpawnPosition) {
            SPacketSpawnPosition packet6 = (SPacketSpawnPosition)packet;
            this.playerEntity.compassTarget = new Location(this.getPlayer().getWorld(), (double)packet6.spawnBlockPos.getX(), (double)packet6.spawnBlockPos.getY(), (double)packet6.spawnBlockPos.getZ());
         }

         try {
            this.netManager.sendPacket(packet);
         } catch (Throwable var5) {
            CrashReport crashreport = CrashReport.makeCrashReport(var5, "Sending packet");
            CrashReportCategory crashreportsystemdetails = crashreport.makeCategory("Packet being sent");
            crashreportsystemdetails.setDetail("Packet class", new ICrashReportDetail() {
               public String call() throws Exception {
                  return packet.getClass().getCanonicalName();
               }

               public Object call() throws Exception {
                  return this.call();
               }
            });
            throw new ReportedException(crashreport);
         }
      }
   }

   public void processHeldItemChange(CPacketHeldItemChange packetplayinhelditemslot) {
      PacketThreadUtil.checkThreadAndEnqueue(packetplayinhelditemslot, this, this.playerEntity.getServerWorld());
      if (!this.playerEntity.isMovementBlocked()) {
         if (packetplayinhelditemslot.getSlotId() >= 0 && packetplayinhelditemslot.getSlotId() < InventoryPlayer.getHotbarSize()) {
            PlayerItemHeldEvent event = new PlayerItemHeldEvent(this.getPlayer(), this.playerEntity.inventory.currentItem, packetplayinhelditemslot.getSlotId());
            this.server.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
               this.sendPacket(new SPacketHeldItemChange(this.playerEntity.inventory.currentItem));
               this.playerEntity.markPlayerActive();
               return;
            }

            this.playerEntity.inventory.currentItem = packetplayinhelditemslot.getSlotId();
            this.playerEntity.markPlayerActive();
         } else {
            LOGGER.warn("{} tried to set an invalid carried item", new Object[]{this.playerEntity.getName()});
            this.disconnect("Nope!");
         }

      }
   }

   public void processChatMessage(CPacketChatMessage packetplayinchat) {
      boolean isSync = packetplayinchat.getMessage().startsWith("/");
      if (packetplayinchat.getMessage().startsWith("/")) {
         PacketThreadUtil.checkThreadAndEnqueue(packetplayinchat, this, this.playerEntity.getServerWorld());
      }

      if (!this.playerEntity.isDead && this.playerEntity.getChatVisibility() != EntityPlayer.EnumChatVisibility.HIDDEN) {
         this.playerEntity.markPlayerActive();
         String s = packetplayinchat.getMessage();
         s = StringUtils.normalizeSpace(s);

         for(int i = 0; i < s.length(); ++i) {
            if (!ChatAllowedCharacters.isAllowedCharacter(s.charAt(i))) {
               if (!isSync) {
                  Waitable waitable = new Waitable() {
                     protected Object evaluate() {
                        NetHandlerPlayServer.this.disconnect("Illegal characters in chat");
                        return null;
                     }
                  };
                  this.serverController.processQueue.add(waitable);

                  try {
                     waitable.get();
                  } catch (InterruptedException var12) {
                     Thread.currentThread().interrupt();
                  } catch (ExecutionException var13) {
                     throw new RuntimeException(var13);
                  }
               } else {
                  this.disconnect("Illegal characters in chat");
               }

               return;
            }
         }

         if (isSync) {
            try {
               this.serverController.server.playerCommandState = true;
               this.handleSlashCommand(s);
            } finally {
               this.serverController.server.playerCommandState = false;
            }
         } else if (s.isEmpty()) {
            LOGGER.warn(this.playerEntity.getName() + " tried to send an empty message");
         } else if (this.getPlayer().isConversing()) {
            this.getPlayer().acceptConversationInput(s);
         } else if (this.playerEntity.getChatVisibility() == EntityPlayer.EnumChatVisibility.SYSTEM) {
            TextComponentTranslation chatmessage = new TextComponentTranslation("chat.cannotSend", new Object[0]);
            chatmessage.getStyle().setColor(TextFormatting.RED);
            this.sendPacket(new SPacketChat(chatmessage));
         } else {
            this.chat(s, true);
         }

         if (chatSpamField.addAndGet(this, 20) > 200 && !this.serverController.getPlayerList().canSendCommands(this.playerEntity.getGameProfile())) {
            if (!isSync) {
               Waitable waitable = new Waitable() {
                  protected Object evaluate() {
                     NetHandlerPlayServer.this.disconnect("disconnect.spam");
                     return null;
                  }
               };
               this.serverController.processQueue.add(waitable);

               try {
                  waitable.get();
               } catch (InterruptedException var14) {
                  Thread.currentThread().interrupt();
               } catch (ExecutionException var15) {
                  throw new RuntimeException(var15);
               }
            } else {
               this.disconnect("disconnect.spam");
            }
         }
      } else {
         TextComponentTranslation chatmessage = new TextComponentTranslation("chat.cannotSend", new Object[0]);
         chatmessage.getStyle().setColor(TextFormatting.RED);
         this.sendPacket(new SPacketChat(chatmessage));
      }

   }

   public void chat(String s, boolean async) {
      if (!s.isEmpty() && this.playerEntity.getChatVisibility() != EntityPlayer.EnumChatVisibility.HIDDEN) {
         if (!async && s.startsWith("/")) {
            this.handleSlashCommand(s);
         } else if (this.playerEntity.getChatVisibility() != EntityPlayer.EnumChatVisibility.SYSTEM) {
            Player player = this.getPlayer();
            AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(async, player, s, new LazyPlayerSet(this.serverController));
            this.server.getPluginManager().callEvent(event);
            if (PlayerChatEvent.getHandlerList().getRegisteredListeners().length != 0) {
               final PlayerChatEvent queueEvent = new PlayerChatEvent(player, event.getMessage(), event.getFormat(), event.getRecipients());
               queueEvent.setCancelled(event.isCancelled());
               Waitable waitable = new Waitable() {
                  protected Object evaluate() {
                     Bukkit.getPluginManager().callEvent(queueEvent);
                     if (queueEvent.isCancelled()) {
                        return null;
                     } else {
                        String message = String.format(queueEvent.getFormat(), queueEvent.getPlayer().getDisplayName(), queueEvent.getMessage());
                        NetHandlerPlayServer.this.serverController.console.sendMessage(message);
                        if (((LazyPlayerSet)queueEvent.getRecipients()).isLazy()) {
                           for(Object player : NetHandlerPlayServer.this.serverController.getPlayerList().playerEntityList) {
                              ((EntityPlayerMP)player).sendMessage(CraftChatMessage.fromString(message));
                           }
                        } else {
                           for(Player player : queueEvent.getRecipients()) {
                              player.sendMessage(message);
                           }
                        }

                        return null;
                     }
                  }
               };
               if (async) {
                  this.serverController.processQueue.add(waitable);
               } else {
                  waitable.run();
               }

               try {
                  waitable.get();
               } catch (InterruptedException var8) {
                  Thread.currentThread().interrupt();
               } catch (ExecutionException var9) {
                  throw new RuntimeException("Exception processing chat event", var9.getCause());
               }
            } else {
               if (event.isCancelled()) {
                  return;
               }

               s = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
               this.serverController.console.sendMessage(s);
               if (((LazyPlayerSet)event.getRecipients()).isLazy()) {
                  for(Object recipient : this.serverController.getPlayerList().playerEntityList) {
                     ((EntityPlayerMP)recipient).sendMessage(CraftChatMessage.fromString(s));
                  }
               } else {
                  for(Player recipient : event.getRecipients()) {
                     recipient.sendMessage(s);
                  }
               }
            }
         }

      }
   }

   private void handleSlashCommand(String s) {
      LOGGER.info(this.playerEntity.getName() + " issued server command: " + s);
      CraftPlayer player = this.getPlayer();
      PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, s, new LazyPlayerSet(this.serverController));
      this.server.getPluginManager().callEvent(event);
      if (!event.isCancelled()) {
         try {
            if (!this.server.dispatchCommand(event.getPlayer(), event.getMessage().substring(1))) {
               ;
            }
         } catch (CommandException var5) {
            player.sendMessage(ChatColor.RED + "An internal error occurred while attempting to perform this command");
            java.util.logging.Logger.getLogger(NetHandlerPlayServer.class.getName()).log(Level.SEVERE, (String)null, var5);
         }
      }
   }

   public void handleAnimation(CPacketAnimation packetplayinarmanimation) {
      PacketThreadUtil.checkThreadAndEnqueue(packetplayinarmanimation, this, this.playerEntity.getServerWorld());
      if (!this.playerEntity.isMovementBlocked()) {
         this.playerEntity.markPlayerActive();
         float f1 = this.playerEntity.rotationPitch;
         float f2 = this.playerEntity.rotationYaw;
         double d0 = this.playerEntity.posX;
         double d1 = this.playerEntity.posY + (double)this.playerEntity.getEyeHeight();
         double d2 = this.playerEntity.posZ;
         Vec3d vec3d = new Vec3d(d0, d1, d2);
         float f3 = MathHelper.cos(-f2 * 0.017453292F - 3.1415927F);
         float f4 = MathHelper.sin(-f2 * 0.017453292F - 3.1415927F);
         float f5 = -MathHelper.cos(-f1 * 0.017453292F);
         float f6 = MathHelper.sin(-f1 * 0.017453292F);
         float f7 = f4 * f5;
         float f8 = f3 * f5;
         double d3 = this.playerEntity.interactionManager.getGameType() == GameType.CREATIVE ? 5.0D : 4.5D;
         Vec3d vec3d1 = vec3d.addVector((double)f7 * d3, (double)f6 * d3, (double)f8 * d3);
         RayTraceResult movingobjectposition = this.playerEntity.world.rayTraceBlocks(vec3d, vec3d1, false);
         if (movingobjectposition == null || movingobjectposition.typeOfHit != RayTraceResult.Type.BLOCK) {
            CraftEventFactory.callPlayerInteractEvent(this.playerEntity, Action.LEFT_CLICK_AIR, this.playerEntity.inventory.getCurrentItem(), EnumHand.MAIN_HAND);
         }

         PlayerAnimationEvent event = new PlayerAnimationEvent(this.getPlayer());
         this.server.getPluginManager().callEvent(event);
         if (!event.isCancelled()) {
            this.playerEntity.swingArm(packetplayinarmanimation.getHand());
         }
      }
   }

   public void processEntityAction(CPacketEntityAction packetplayinentityaction) {
      PacketThreadUtil.checkThreadAndEnqueue(packetplayinentityaction, this, this.playerEntity.getServerWorld());
      if (!this.playerEntity.isDead) {
         switch($SWITCH_TABLE$net$minecraft$server$PacketPlayInEntityAction$EnumPlayerAction()[packetplayinentityaction.getAction().ordinal()]) {
         case 1:
         case 2:
            PlayerToggleSneakEvent event = new PlayerToggleSneakEvent(this.getPlayer(), packetplayinentityaction.getAction() == CPacketEntityAction.Action.START_SNEAKING);
            this.server.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
               return;
            }
         case 3:
         default:
            break;
         case 4:
         case 5:
            PlayerToggleSprintEvent e2 = new PlayerToggleSprintEvent(this.getPlayer(), packetplayinentityaction.getAction() == CPacketEntityAction.Action.START_SPRINTING);
            this.server.getPluginManager().callEvent(e2);
            if (e2.isCancelled()) {
               return;
            }
         }

         this.playerEntity.markPlayerActive();
         switch(NetHandlerPlayServer.SyntheticClass_1.b[packetplayinentityaction.getAction().ordinal()]) {
         case 1:
            this.playerEntity.setSneaking(true);
            break;
         case 2:
            this.playerEntity.setSneaking(false);
            break;
         case 3:
            this.playerEntity.setSprinting(true);
            break;
         case 4:
            this.playerEntity.setSprinting(false);
            break;
         case 5:
            this.playerEntity.wakeUpPlayer(false, true, true);
            this.targetPos = new Vec3d(this.playerEntity.posX, this.playerEntity.posY, this.playerEntity.posZ);
            break;
         case 6:
            if (this.playerEntity.getRidingEntity() instanceof IJumpingMount) {
               IJumpingMount ijumpable = (IJumpingMount)this.playerEntity.getRidingEntity();
               int i = packetplayinentityaction.getAuxData();
               if (ijumpable.canJump() && i > 0) {
                  ijumpable.handleStartJump(i);
               }
            }
            break;
         case 7:
            if (this.playerEntity.getRidingEntity() instanceof IJumpingMount) {
               IJumpingMount ijumpable = (IJumpingMount)this.playerEntity.getRidingEntity();
               ijumpable.handleStopJump();
            }
            break;
         case 8:
            if (this.playerEntity.getRidingEntity() instanceof EntityHorse) {
               ((EntityHorse)this.playerEntity.getRidingEntity()).openGUI(this.playerEntity);
            }
            break;
         case 9:
            if (!this.playerEntity.onGround && this.playerEntity.motionY < 0.0D && !this.playerEntity.isElytraFlying() && !this.playerEntity.isInWater()) {
               ItemStack itemstack = this.playerEntity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
               if (itemstack != null && itemstack.getItem() == Items.ELYTRA && ItemElytra.isBroken(itemstack)) {
                  this.playerEntity.setElytraFlying();
               }
            } else {
               this.playerEntity.clearElytraFlying();
            }
            break;
         default:
            throw new IllegalArgumentException("Invalid client command!");
         }

      }
   }

   public void processUseEntity(CPacketUseEntity packetplayinuseentity) {
      PacketThreadUtil.checkThreadAndEnqueue(packetplayinuseentity, this, this.playerEntity.getServerWorld());
      if (!this.playerEntity.isMovementBlocked()) {
         WorldServer worldserver = this.serverController.getWorldServer(this.playerEntity.dimension);
         Entity entity = packetplayinuseentity.getEntityFromWorld(worldserver);
         this.playerEntity.markPlayerActive();
         if (entity != null) {
            boolean flag = this.playerEntity.canEntityBeSeen(entity);
            double d0 = 36.0D;
            if (!flag) {
               d0 = 9.0D;
            }

            if (this.playerEntity.getDistanceSqToEntity(entity) < d0) {
               ItemStack itemInHand = this.playerEntity.getHeldItem(packetplayinuseentity.getHand() == null ? EnumHand.MAIN_HAND : packetplayinuseentity.getHand());
               if (packetplayinuseentity.getAction() == CPacketUseEntity.Action.INTERACT || packetplayinuseentity.getAction() == CPacketUseEntity.Action.INTERACT_AT) {
                  boolean triggerLeashUpdate = itemInHand != null && itemInHand.getItem() == Items.LEAD && entity instanceof EntityLiving;
                  Item origItem = this.playerEntity.inventory.getCurrentItem() == null ? null : this.playerEntity.inventory.getCurrentItem().getItem();
                  PlayerInteractEntityEvent event;
                  if (packetplayinuseentity.getAction() == CPacketUseEntity.Action.INTERACT) {
                     event = new PlayerInteractEntityEvent(this.getPlayer(), entity.getBukkitEntity(), packetplayinuseentity.getHand() == EnumHand.OFF_HAND ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND);
                  } else {
                     Vec3d target = packetplayinuseentity.getHitVec();
                     event = new PlayerInteractAtEntityEvent(this.getPlayer(), entity.getBukkitEntity(), new Vector(target.xCoord, target.yCoord, target.zCoord), packetplayinuseentity.getHand() == EnumHand.OFF_HAND ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND);
                  }

                  this.server.getPluginManager().callEvent(event);
                  if (triggerLeashUpdate && (event.isCancelled() || this.playerEntity.inventory.getCurrentItem() == null || this.playerEntity.inventory.getCurrentItem().getItem() != Items.LEAD)) {
                     this.sendPacket(new SPacketEntityAttach(entity, ((EntityLiving)entity).getLeashedToEntity()));
                  }

                  if (event.isCancelled() || this.playerEntity.inventory.getCurrentItem() == null || this.playerEntity.inventory.getCurrentItem().getItem() != origItem) {
                     this.sendPacket(new SPacketEntityMetadata(entity.getEntityId(), entity.dataManager, true));
                  }

                  if (event.isCancelled()) {
                     return;
                  }
               }

               if (packetplayinuseentity.getAction() == CPacketUseEntity.Action.INTERACT) {
                  EnumHand enumhand = packetplayinuseentity.getHand();
                  ItemStack itemstack = this.playerEntity.getHeldItem(enumhand);
                  this.playerEntity.interact(entity, itemstack, enumhand);
                  if (itemInHand != null && itemInHand.stackSize <= -1) {
                     this.playerEntity.sendContainerToPlayer(this.playerEntity.openContainer);
                  }
               } else if (packetplayinuseentity.getAction() == CPacketUseEntity.Action.INTERACT_AT) {
                  EnumHand enumhand = packetplayinuseentity.getHand();
                  ItemStack itemstack = this.playerEntity.getHeldItem(enumhand);
                  entity.applyPlayerInteraction(this.playerEntity, packetplayinuseentity.getHitVec(), itemstack, enumhand);
                  if (itemInHand != null && itemInHand.stackSize <= -1) {
                     this.playerEntity.sendContainerToPlayer(this.playerEntity.openContainer);
                  }
               } else if (packetplayinuseentity.getAction() == CPacketUseEntity.Action.ATTACK) {
                  if (entity instanceof EntityItem || entity instanceof EntityXPOrb || entity instanceof EntityArrow || entity == this.playerEntity && !this.playerEntity.isSpectator()) {
                     this.disconnect("Attempting to attack an invalid entity");
                     this.serverController.warning("Player " + this.playerEntity.getName() + " tried to attack an invalid entity");
                     return;
                  }

                  this.playerEntity.attackTargetEntityWithCurrentItem(entity);
                  if (itemInHand != null && itemInHand.stackSize <= -1) {
                     this.playerEntity.sendContainerToPlayer(this.playerEntity.openContainer);
                  }
               }
            }
         }

      }
   }

   public void processClientStatus(CPacketClientStatus packetplayinclientcommand) {
      PacketThreadUtil.checkThreadAndEnqueue(packetplayinclientcommand, this, this.playerEntity.getServerWorld());
      this.playerEntity.markPlayerActive();
      CPacketClientStatus.State packetplayinclientcommand_enumclientcommand = packetplayinclientcommand.getStatus();
      switch(NetHandlerPlayServer.SyntheticClass_1.c[packetplayinclientcommand_enumclientcommand.ordinal()]) {
      case 1:
         if (this.playerEntity.playerConqueredTheEnd) {
            this.playerEntity.playerConqueredTheEnd = false;
            this.serverController.getPlayerList().changeDimension(this.playerEntity, 0, TeleportCause.END_PORTAL);
         } else {
            if (this.playerEntity.getHealth() > 0.0F) {
               return;
            }

            this.playerEntity = this.serverController.getPlayerList().recreatePlayerEntity(this.playerEntity, 0, false);
            if (this.serverController.isHardcore()) {
               this.playerEntity.setGameType(GameType.SPECTATOR);
               this.playerEntity.getServerWorld().getGameRules().setOrCreateGameRule("spectatorsGenerateChunks", "false");
            }
         }
         break;
      case 2:
         this.playerEntity.getStatFile().sendStats(this.playerEntity);
         break;
      case 3:
         this.playerEntity.addStat(AchievementList.OPEN_INVENTORY);
      }

   }

   public void processCloseWindow(CPacketCloseWindow packetplayinclosewindow) {
      PacketThreadUtil.checkThreadAndEnqueue(packetplayinclosewindow, this, this.playerEntity.getServerWorld());
      if (!this.playerEntity.isMovementBlocked()) {
         CraftEventFactory.handleInventoryCloseEvent(this.playerEntity);
         this.playerEntity.closeContainer();
      }
   }

   public void processClickWindow(CPacketClickWindow packetplayinwindowclick) {
      PacketThreadUtil.checkThreadAndEnqueue(packetplayinwindowclick, this, this.playerEntity.getServerWorld());
      if (!this.playerEntity.isMovementBlocked()) {
         this.playerEntity.markPlayerActive();
         if (this.playerEntity.openContainer.windowId == packetplayinwindowclick.getWindowId() && this.playerEntity.openContainer.getCanCraft(this.playerEntity)) {
            boolean cancelled = this.playerEntity.isSpectator();
            if (packetplayinwindowclick.getSlotId() < -1 && packetplayinwindowclick.getSlotId() != -999) {
               return;
            }

            InventoryView inventory = this.playerEntity.openContainer.getBukkitView();
            SlotType type = CraftInventoryView.getSlotType(inventory, packetplayinwindowclick.getSlotId());
            ClickType click = ClickType.UNKNOWN;
            InventoryAction action = InventoryAction.UNKNOWN;
            ItemStack itemstack = null;
            switch($SWITCH_TABLE$net$minecraft$server$InventoryClickType()[packetplayinwindowclick.getClickType().ordinal()]) {
            case 1:
               if (packetplayinwindowclick.getUsedButton() == 0) {
                  click = ClickType.LEFT;
               } else if (packetplayinwindowclick.getUsedButton() == 1) {
                  click = ClickType.RIGHT;
               }

               if (packetplayinwindowclick.getUsedButton() != 0 && packetplayinwindowclick.getUsedButton() != 1) {
                  break;
               }

               action = InventoryAction.NOTHING;
               if (packetplayinwindowclick.getSlotId() == -999) {
                  if (this.playerEntity.inventory.getItemStack() != null) {
                     action = packetplayinwindowclick.getUsedButton() == 0 ? InventoryAction.DROP_ALL_CURSOR : InventoryAction.DROP_ONE_CURSOR;
                  }
               } else if (packetplayinwindowclick.getSlotId() < 0) {
                  action = InventoryAction.NOTHING;
               } else {
                  Slot slot = this.playerEntity.openContainer.getSlot(packetplayinwindowclick.getSlotId());
                  if (slot == null) {
                     break;
                  }

                  ItemStack clickedItem = slot.getStack();
                  ItemStack cursor = this.playerEntity.inventory.getItemStack();
                  if (clickedItem == null) {
                     if (cursor != null) {
                        action = packetplayinwindowclick.getUsedButton() == 0 ? InventoryAction.PLACE_ALL : InventoryAction.PLACE_ONE;
                     }
                  } else {
                     if (!slot.canTakeStack(this.playerEntity)) {
                        break;
                     }

                     if (cursor == null) {
                        action = packetplayinwindowclick.getUsedButton() == 0 ? InventoryAction.PICKUP_ALL : InventoryAction.PICKUP_HALF;
                     } else if (slot.isItemValid(cursor)) {
                        if (clickedItem.isItemEqual(cursor) && ItemStack.areItemStackTagsEqual(clickedItem, cursor)) {
                           int toPlace = packetplayinwindowclick.getUsedButton() == 0 ? cursor.stackSize : 1;
                           toPlace = Math.min(toPlace, clickedItem.getMaxStackSize() - clickedItem.stackSize);
                           toPlace = Math.min(toPlace, slot.inventory.getInventoryStackLimit() - clickedItem.stackSize);
                           if (toPlace == 1) {
                              action = InventoryAction.PLACE_ONE;
                           } else if (toPlace == cursor.stackSize) {
                              action = InventoryAction.PLACE_ALL;
                           } else if (toPlace < 0) {
                              action = toPlace != -1 ? InventoryAction.PICKUP_SOME : InventoryAction.PICKUP_ONE;
                           } else if (toPlace != 0) {
                              action = InventoryAction.PLACE_SOME;
                           }
                        } else if (cursor.stackSize <= slot.getSlotStackLimit()) {
                           action = InventoryAction.SWAP_WITH_CURSOR;
                        }
                     } else if (cursor.getItem() == clickedItem.getItem() && (!cursor.getHasSubtypes() || cursor.getMetadata() == clickedItem.getMetadata()) && ItemStack.areItemStackTagsEqual(cursor, clickedItem) && clickedItem.stackSize >= 0 && clickedItem.stackSize + cursor.stackSize <= cursor.getMaxStackSize()) {
                        action = InventoryAction.PICKUP_ALL;
                     }
                  }
               }
               break;
            case 2:
               if (packetplayinwindowclick.getUsedButton() == 0) {
                  click = ClickType.SHIFT_LEFT;
               } else if (packetplayinwindowclick.getUsedButton() == 1) {
                  click = ClickType.SHIFT_RIGHT;
               }

               if (packetplayinwindowclick.getUsedButton() != 0 && packetplayinwindowclick.getUsedButton() != 1) {
                  break;
               }

               if (packetplayinwindowclick.getSlotId() < 0) {
                  action = InventoryAction.NOTHING;
               } else {
                  Slot slot = this.playerEntity.openContainer.getSlot(packetplayinwindowclick.getSlotId());
                  if (slot != null && slot.canTakeStack(this.playerEntity) && slot.getHasStack()) {
                     action = InventoryAction.MOVE_TO_OTHER_INVENTORY;
                     break;
                  }

                  action = InventoryAction.NOTHING;
               }
               break;
            case 3:
               if (packetplayinwindowclick.getUsedButton() < 0 || packetplayinwindowclick.getUsedButton() >= 9) {
                  break;
               }

               click = ClickType.NUMBER_KEY;
               Slot clickedSlot = this.playerEntity.openContainer.getSlot(packetplayinwindowclick.getSlotId());
               if (!clickedSlot.canTakeStack(this.playerEntity)) {
                  action = InventoryAction.NOTHING;
               } else {
                  ItemStack hotbar = this.playerEntity.inventory.getStackInSlot(packetplayinwindowclick.getUsedButton());
                  boolean canCleanSwap = hotbar == null || clickedSlot.inventory == this.playerEntity.inventory && clickedSlot.isItemValid(hotbar);
                  if (clickedSlot.getHasStack()) {
                     if (canCleanSwap) {
                        action = InventoryAction.HOTBAR_SWAP;
                     } else {
                        int firstEmptySlot = this.playerEntity.inventory.getFirstEmptyStack();
                        if (firstEmptySlot > -1) {
                           action = InventoryAction.HOTBAR_MOVE_AND_READD;
                        } else {
                           action = InventoryAction.NOTHING;
                        }
                     }
                  } else {
                     if (!clickedSlot.getHasStack() && hotbar != null && clickedSlot.isItemValid(hotbar)) {
                        action = InventoryAction.HOTBAR_SWAP;
                        break;
                     }

                     action = InventoryAction.NOTHING;
                  }
               }
               break;
            case 4:
               if (packetplayinwindowclick.getUsedButton() == 2) {
                  click = ClickType.MIDDLE;
                  if (packetplayinwindowclick.getSlotId() == -999) {
                     action = InventoryAction.NOTHING;
                     break;
                  }

                  Slot slot = this.playerEntity.openContainer.getSlot(packetplayinwindowclick.getSlotId());
                  if (slot != null && slot.getHasStack() && this.playerEntity.capabilities.isCreativeMode && this.playerEntity.inventory.getItemStack() == null) {
                     action = InventoryAction.CLONE_STACK;
                     break;
                  }

                  action = InventoryAction.NOTHING;
                  break;
               }

               click = ClickType.UNKNOWN;
               action = InventoryAction.UNKNOWN;
               break;
            case 5:
               if (packetplayinwindowclick.getSlotId() >= 0) {
                  if (packetplayinwindowclick.getUsedButton() == 0) {
                     click = ClickType.DROP;
                     Slot slot = this.playerEntity.openContainer.getSlot(packetplayinwindowclick.getSlotId());
                     if (slot != null && slot.getHasStack() && slot.canTakeStack(this.playerEntity) && slot.getStack() != null && slot.getStack().getItem() != Item.getItemFromBlock(Blocks.AIR)) {
                        action = InventoryAction.DROP_ONE_SLOT;
                        break;
                     }

                     action = InventoryAction.NOTHING;
                     break;
                  }

                  if (packetplayinwindowclick.getUsedButton() != 1) {
                     break;
                  }

                  click = ClickType.CONTROL_DROP;
                  Slot slot = this.playerEntity.openContainer.getSlot(packetplayinwindowclick.getSlotId());
                  if (slot != null && slot.getHasStack() && slot.canTakeStack(this.playerEntity) && slot.getStack() != null && slot.getStack().getItem() != Item.getItemFromBlock(Blocks.AIR)) {
                     action = InventoryAction.DROP_ALL_SLOT;
                     break;
                  }

                  action = InventoryAction.NOTHING;
                  break;
               }

               click = ClickType.LEFT;
               if (packetplayinwindowclick.getUsedButton() == 1) {
                  click = ClickType.RIGHT;
               }

               action = InventoryAction.NOTHING;
               break;
            case 6:
               itemstack = this.playerEntity.openContainer.slotClick(packetplayinwindowclick.getSlotId(), packetplayinwindowclick.getUsedButton(), packetplayinwindowclick.getClickType(), this.playerEntity);
               break;
            case 7:
               click = ClickType.DOUBLE_CLICK;
               action = InventoryAction.NOTHING;
               if (packetplayinwindowclick.getSlotId() >= 0 && this.playerEntity.inventory.getItemStack() != null) {
                  ItemStack cursor = this.playerEntity.inventory.getItemStack();
                  action = InventoryAction.NOTHING;
                  if (inventory.getTopInventory().contains(org.bukkit.Material.getMaterial(Item.getIdFromItem(cursor.getItem()))) || inventory.getBottomInventory().contains(org.bukkit.Material.getMaterial(Item.getIdFromItem(cursor.getItem())))) {
                     action = InventoryAction.COLLECT_TO_CURSOR;
                  }
               }
            }

            if (packetplayinwindowclick.getClickType() != net.minecraft.inventory.ClickType.QUICK_CRAFT) {
               InventoryClickEvent event;
               if (click == ClickType.NUMBER_KEY) {
                  event = new InventoryClickEvent(inventory, type, packetplayinwindowclick.getSlotId(), click, action, packetplayinwindowclick.getUsedButton());
               } else {
                  event = new InventoryClickEvent(inventory, type, packetplayinwindowclick.getSlotId(), click, action);
               }

               Inventory top = inventory.getTopInventory();
               if (packetplayinwindowclick.getSlotId() == 0 && top instanceof CraftingInventory) {
                  Recipe recipe = ((CraftingInventory)top).getRecipe();
                  if (recipe != null) {
                     if (click == ClickType.NUMBER_KEY) {
                        event = new CraftItemEvent(recipe, inventory, type, packetplayinwindowclick.getSlotId(), click, action, packetplayinwindowclick.getUsedButton());
                     } else {
                        event = new CraftItemEvent(recipe, inventory, type, packetplayinwindowclick.getSlotId(), click, action);
                     }
                  }
               }

               event.setCancelled(cancelled);
               Container oldContainer = this.playerEntity.openContainer;
               this.server.getPluginManager().callEvent(event);
               if (this.playerEntity.openContainer != oldContainer) {
                  return;
               }

               switch($SWITCH_TABLE$org$bukkit$event$Event$Result()[event.getResult().ordinal()]) {
               case 1:
                  switch($SWITCH_TABLE$org$bukkit$event$inventory$InventoryAction()[action.ordinal()]) {
                  case 1:
                  default:
                     break;
                  case 2:
                  case 14:
                  case 15:
                  case 16:
                  case 18:
                  case 19:
                     this.playerEntity.sendContainerToPlayer(this.playerEntity.openContainer);
                     break;
                  case 3:
                  case 4:
                  case 5:
                  case 6:
                  case 7:
                  case 8:
                  case 9:
                     this.playerEntity.connection.sendPacket(new SPacketSetSlot(-1, -1, this.playerEntity.inventory.getItemStack()));
                     this.playerEntity.connection.sendPacket(new SPacketSetSlot(this.playerEntity.openContainer.windowId, packetplayinwindowclick.getSlotId(), this.playerEntity.openContainer.getSlot(packetplayinwindowclick.getSlotId()).getStack()));
                     break;
                  case 10:
                  case 11:
                  case 17:
                     this.playerEntity.connection.sendPacket(new SPacketSetSlot(-1, -1, this.playerEntity.inventory.getItemStack()));
                     break;
                  case 12:
                  case 13:
                     this.playerEntity.connection.sendPacket(new SPacketSetSlot(this.playerEntity.openContainer.windowId, packetplayinwindowclick.getSlotId(), this.playerEntity.openContainer.getSlot(packetplayinwindowclick.getSlotId()).getStack()));
                  }

                  return;
               case 2:
               case 3:
                  itemstack = this.playerEntity.openContainer.slotClick(packetplayinwindowclick.getSlotId(), packetplayinwindowclick.getUsedButton(), packetplayinwindowclick.getClickType(), this.playerEntity);
               default:
                  if (event instanceof CraftItemEvent) {
                     this.playerEntity.sendContainerToPlayer(this.playerEntity.openContainer);
                  }
               }
            }

            if (ItemStack.areItemStacksEqual(packetplayinwindowclick.getClickedItem(), itemstack)) {
               this.playerEntity.connection.sendPacket(new SPacketConfirmTransaction(packetplayinwindowclick.getWindowId(), packetplayinwindowclick.getActionNumber(), true));
               this.playerEntity.isChangingQuantityOnly = true;
               this.playerEntity.openContainer.detectAndSendChanges();
               this.playerEntity.updateHeldItem();
               this.playerEntity.isChangingQuantityOnly = false;
            } else {
               this.pendingTransactions.addKey(this.playerEntity.openContainer.windowId, Short.valueOf(packetplayinwindowclick.getActionNumber()));
               this.playerEntity.connection.sendPacket(new SPacketConfirmTransaction(packetplayinwindowclick.getWindowId(), packetplayinwindowclick.getActionNumber(), false));
               this.playerEntity.openContainer.setCanCraft(this.playerEntity, false);
               ArrayList arraylist1 = Lists.newArrayList();

               for(int j = 0; j < this.playerEntity.openContainer.inventorySlots.size(); ++j) {
                  ItemStack itemstack1 = ((Slot)this.playerEntity.openContainer.inventorySlots.get(j)).getStack();
                  ItemStack itemstack2 = itemstack1 != null && itemstack1.stackSize > 0 ? itemstack1 : null;
                  arraylist1.add(itemstack2);
               }

               this.playerEntity.updateCraftingInventory(this.playerEntity.openContainer, arraylist1);
            }
         }

      }
   }

   public void processEnchantItem(CPacketEnchantItem packetplayinenchantitem) {
      PacketThreadUtil.checkThreadAndEnqueue(packetplayinenchantitem, this, this.playerEntity.getServerWorld());
      if (!this.playerEntity.isMovementBlocked()) {
         this.playerEntity.markPlayerActive();
         if (this.playerEntity.openContainer.windowId == packetplayinenchantitem.getWindowId() && this.playerEntity.openContainer.getCanCraft(this.playerEntity) && !this.playerEntity.isSpectator()) {
            this.playerEntity.openContainer.enchantItem(this.playerEntity, packetplayinenchantitem.getButton());
            this.playerEntity.openContainer.detectAndSendChanges();
         }

      }
   }

   public void processCreativeInventoryAction(CPacketCreativeInventoryAction packetplayinsetcreativeslot) {
      PacketThreadUtil.checkThreadAndEnqueue(packetplayinsetcreativeslot, this, this.playerEntity.getServerWorld());
      if (this.playerEntity.interactionManager.isCreative()) {
         boolean flag = packetplayinsetcreativeslot.getSlotId() < 0;
         ItemStack itemstack = packetplayinsetcreativeslot.getStack();
         if (itemstack != null && itemstack.hasTagCompound() && itemstack.getTagCompound().hasKey("BlockEntityTag", 10)) {
            NBTTagCompound nbttagcompound = itemstack.getTagCompound().getCompoundTag("BlockEntityTag");
            if (nbttagcompound.hasKey("x") && nbttagcompound.hasKey("y") && nbttagcompound.hasKey("z")) {
               BlockPos blockposition = new BlockPos(nbttagcompound.getInteger("x"), nbttagcompound.getInteger("y"), nbttagcompound.getInteger("z"));
               TileEntity tileentity = this.playerEntity.world.getTileEntity(blockposition);
               if (tileentity != null) {
                  NBTTagCompound nbttagcompound1 = tileentity.writeToNBT(new NBTTagCompound());
                  nbttagcompound1.removeTag("x");
                  nbttagcompound1.removeTag("y");
                  nbttagcompound1.removeTag("z");
                  itemstack.setTagInfo("BlockEntityTag", nbttagcompound1);
               }
            }
         }

         boolean flag1 = packetplayinsetcreativeslot.getSlotId() >= 1 && packetplayinsetcreativeslot.getSlotId() <= 45;
         boolean flag2 = itemstack == null || itemstack.getItem() != null && !invalidItems.contains(Integer.valueOf(Item.getIdFromItem(itemstack.getItem())));
         boolean flag3 = itemstack == null || itemstack.getMetadata() >= 0 && itemstack.stackSize <= 64 && itemstack.stackSize > 0;
         if (flag || flag1 && !ItemStack.areItemStacksEqual(this.playerEntity.inventoryContainer.getSlot(packetplayinsetcreativeslot.getSlotId()).getStack(), packetplayinsetcreativeslot.getStack())) {
            HumanEntity player = this.playerEntity.getBukkitEntity();
            InventoryView inventory = new CraftInventoryView(player, player.getInventory(), this.playerEntity.inventoryContainer);
            org.bukkit.inventory.ItemStack item = CraftItemStack.asBukkitCopy(packetplayinsetcreativeslot.getStack());
            SlotType type = SlotType.QUICKBAR;
            if (flag) {
               type = SlotType.OUTSIDE;
            } else if (packetplayinsetcreativeslot.getSlotId() < 36) {
               if (packetplayinsetcreativeslot.getSlotId() >= 5 && packetplayinsetcreativeslot.getSlotId() < 9) {
                  type = SlotType.ARMOR;
               } else {
                  type = SlotType.CONTAINER;
               }
            }

            InventoryCreativeEvent event = new InventoryCreativeEvent(inventory, type, flag ? -999 : packetplayinsetcreativeslot.getSlotId(), item);
            this.server.getPluginManager().callEvent(event);
            itemstack = CraftItemStack.asNMSCopy(event.getCursor());
            switch($SWITCH_TABLE$org$bukkit$event$Event$Result()[event.getResult().ordinal()]) {
            case 1:
               if (packetplayinsetcreativeslot.getSlotId() >= 0) {
                  this.playerEntity.connection.sendPacket(new SPacketSetSlot(this.playerEntity.inventoryContainer.windowId, packetplayinsetcreativeslot.getSlotId(), this.playerEntity.inventoryContainer.getSlot(packetplayinsetcreativeslot.getSlotId()).getStack()));
                  this.playerEntity.connection.sendPacket(new SPacketSetSlot(-1, -1, (ItemStack)null));
               }

               return;
            case 2:
            default:
               break;
            case 3:
               flag3 = true;
               flag2 = true;
            }
         }

         if (flag1 && flag2 && flag3) {
            if (itemstack == null) {
               this.playerEntity.inventoryContainer.putStackInSlot(packetplayinsetcreativeslot.getSlotId(), (ItemStack)null);
            } else {
               this.playerEntity.inventoryContainer.putStackInSlot(packetplayinsetcreativeslot.getSlotId(), itemstack);
            }

            this.playerEntity.inventoryContainer.setCanCraft(this.playerEntity, true);
         } else if (flag && flag2 && flag3 && this.itemDropThreshold < 200) {
            this.itemDropThreshold += 20;
            EntityItem entityitem = this.playerEntity.dropItem(itemstack, true);
            if (entityitem != null) {
               entityitem.setAgeToCreativeDespawnTime();
            }
         }
      }

   }

   public void processConfirmTransaction(CPacketConfirmTransaction packetplayintransaction) {
      PacketThreadUtil.checkThreadAndEnqueue(packetplayintransaction, this, this.playerEntity.getServerWorld());
      if (!this.playerEntity.isMovementBlocked()) {
         Short oshort = (Short)this.pendingTransactions.lookup(this.playerEntity.openContainer.windowId);
         if (oshort != null && packetplayintransaction.getUid() == oshort.shortValue() && this.playerEntity.openContainer.windowId == packetplayintransaction.getWindowId() && !this.playerEntity.openContainer.getCanCraft(this.playerEntity) && !this.playerEntity.isSpectator()) {
            this.playerEntity.openContainer.setCanCraft(this.playerEntity, true);
         }

      }
   }

   public void processUpdateSign(CPacketUpdateSign packetplayinupdatesign) {
      PacketThreadUtil.checkThreadAndEnqueue(packetplayinupdatesign, this, this.playerEntity.getServerWorld());
      if (!this.playerEntity.isMovementBlocked()) {
         this.playerEntity.markPlayerActive();
         WorldServer worldserver = this.serverController.getWorldServer(this.playerEntity.dimension);
         BlockPos blockposition = packetplayinupdatesign.getPosition();
         if (worldserver.isBlockLoaded(blockposition)) {
            IBlockState iblockdata = worldserver.getBlockState(blockposition);
            TileEntity tileentity = worldserver.getTileEntity(blockposition);
            if (!(tileentity instanceof TileEntitySign)) {
               return;
            }

            TileEntitySign tileentitysign = (TileEntitySign)tileentity;
            if (!tileentitysign.getIsEditable() || tileentitysign.getPlayer() != this.playerEntity) {
               this.serverController.warning("Player " + this.playerEntity.getName() + " just tried to change non-editable sign");
               this.sendPacket(tileentity.getUpdatePacket());
               return;
            }

            String[] astring = packetplayinupdatesign.getLines();
            Player player = this.server.getPlayer(this.playerEntity);
            int x = packetplayinupdatesign.getPosition().getX();
            int y = packetplayinupdatesign.getPosition().getY();
            int z = packetplayinupdatesign.getPosition().getZ();
            String[] lines = new String[4];

            for(int i = 0; i < astring.length; ++i) {
               lines[i] = TextFormatting.getTextWithoutFormattingCodes((new TextComponentString(TextFormatting.getTextWithoutFormattingCodes(astring[i]))).getUnformattedText());
            }

            SignChangeEvent event = new SignChangeEvent((CraftBlock)player.getWorld().getBlockAt(x, y, z), this.server.getPlayer(this.playerEntity), lines);
            this.server.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
               System.arraycopy(CraftSign.sanitizeLines(event.getLines()), 0, tileentitysign.signText, 0, 4);
               tileentitysign.isEditable = false;
            }

            tileentitysign.markDirty();
            worldserver.notifyBlockUpdate(blockposition, iblockdata, iblockdata, 3);
         }

      }
   }

   public void processKeepAlive(CPacketKeepAlive packetplayinkeepalive) {
      if (packetplayinkeepalive.getKey() == this.keepAliveId) {
         int i = (int)(this.currentTimeMillis() - this.lastPingTime);
         this.playerEntity.ping = (this.playerEntity.ping * 3 + i) / 4;
      }

   }

   private long currentTimeMillis() {
      return System.nanoTime() / 1000000L;
   }

   public void processPlayerAbilities(CPacketPlayerAbilities packetplayinabilities) {
      PacketThreadUtil.checkThreadAndEnqueue(packetplayinabilities, this, this.playerEntity.getServerWorld());
      if (this.playerEntity.capabilities.allowFlying && this.playerEntity.capabilities.isFlying != packetplayinabilities.isFlying()) {
         PlayerToggleFlightEvent event = new PlayerToggleFlightEvent(this.server.getPlayer(this.playerEntity), packetplayinabilities.isFlying());
         this.server.getPluginManager().callEvent(event);
         if (!event.isCancelled()) {
            this.playerEntity.capabilities.isFlying = packetplayinabilities.isFlying();
         } else {
            this.playerEntity.sendPlayerAbilities();
         }
      }

   }

   public void processTabComplete(CPacketTabComplete packetplayintabcomplete) {
      PacketThreadUtil.checkThreadAndEnqueue(packetplayintabcomplete, this, this.playerEntity.getServerWorld());
      if (chatSpamField.addAndGet(this, 10) > 500 && !this.serverController.getPlayerList().canSendCommands(this.playerEntity.getGameProfile())) {
         this.disconnect("disconnect.spam");
      } else {
         ArrayList arraylist = Lists.newArrayList();

         for(String s : this.serverController.tabCompleteCommand(this.playerEntity, packetplayintabcomplete.getMessage(), packetplayintabcomplete.getTargetBlock(), packetplayintabcomplete.hasTargetBlock())) {
            arraylist.add(s);
         }

         this.playerEntity.connection.sendPacket(new SPacketTabComplete((String[])arraylist.toArray(new String[arraylist.size()])));
      }
   }

   public void processClientSettings(CPacketClientSettings packetplayinsettings) {
      PacketThreadUtil.checkThreadAndEnqueue(packetplayinsettings, this, this.playerEntity.getServerWorld());
      this.playerEntity.handleClientSettings(packetplayinsettings);
   }

   public void processCustomPayload(CPacketCustomPayload packetplayincustompayload) {
      PacketThreadUtil.checkThreadAndEnqueue(packetplayincustompayload, this, this.playerEntity.getServerWorld());
      String s = packetplayincustompayload.getChannelName();
      if ("MC|BEdit".equals(s)) {
         PacketBuffer packetdataserializer = packetplayincustompayload.getBufferData();

         try {
            ItemStack itemstack = packetdataserializer.readItemStack();
            if (itemstack == null) {
               return;
            }

            if (!ItemWritableBook.isNBTValid(itemstack.getTagCompound())) {
               throw new IOException("Invalid book tag!");
            }

            ItemStack itemstack1 = this.playerEntity.getHeldItemMainhand();
            if (itemstack1 == null) {
               return;
            }

            if (itemstack.getItem() == Items.WRITABLE_BOOK && itemstack.getItem() == itemstack1.getItem()) {
               itemstack1 = new ItemStack(Items.WRITABLE_BOOK);
               itemstack1.setTagInfo("pages", itemstack.getTagCompound().getTagList("pages", 8));
               CraftEventFactory.handleEditBookEvent(this.playerEntity, itemstack1);
            }
         } catch (Exception var29) {
            LOGGER.error("Couldn't handle book info", var29);
            this.disconnect("Invalid book data!");
         }
      } else if ("MC|BSign".equals(s)) {
         PacketBuffer packetdataserializer = packetplayincustompayload.getBufferData();

         try {
            ItemStack itemstack = packetdataserializer.readItemStack();
            if (itemstack == null) {
               return;
            }

            if (!ItemWrittenBook.validBookTagContents(itemstack.getTagCompound())) {
               throw new IOException("Invalid book tag!");
            }

            ItemStack itemstack1 = this.playerEntity.getHeldItemMainhand();
            if (itemstack1 == null) {
               return;
            }

            if (itemstack.getItem() == Items.WRITABLE_BOOK && itemstack1.getItem() == Items.WRITABLE_BOOK) {
               itemstack1 = new ItemStack(Items.WRITABLE_BOOK);
               itemstack1.setTagInfo("author", new NBTTagString(this.playerEntity.getName()));
               itemstack1.setTagInfo("title", new NBTTagString(itemstack.getTagCompound().getString("title")));
               NBTTagList nbttaglist = itemstack.getTagCompound().getTagList("pages", 8);

               for(int i = 0; i < nbttaglist.tagCount(); ++i) {
                  String s1 = nbttaglist.getStringTagAt(i);
                  TextComponentString chatcomponenttext = new TextComponentString(s1);
                  s1 = ITextComponent.Serializer.componentToJson(chatcomponenttext);
                  nbttaglist.set(i, new NBTTagString(s1));
               }

               itemstack1.setTagInfo("pages", nbttaglist);
               itemstack1.setItem(Items.WRITTEN_BOOK);
               CraftEventFactory.handleEditBookEvent(this.playerEntity, itemstack1);
            }
         } catch (Exception var30) {
            LOGGER.error("Couldn't sign book", var30);
            this.disconnect("Invalid book data!");
         }
      } else if ("MC|TrSel".equals(s)) {
         try {
            int j = packetplayincustompayload.getBufferData().readInt();
            Container container = this.playerEntity.openContainer;
            if (container instanceof ContainerMerchant) {
               ((ContainerMerchant)container).setCurrentRecipeIndex(j);
            }
         } catch (Exception var28) {
            LOGGER.error("Couldn't select trade", var28);
            this.disconnect("Invalid trade data!");
         }
      } else if ("MC|AdvCmd".equals(s)) {
         if (!this.serverController.getEnableCommandBlock()) {
            this.playerEntity.sendMessage((ITextComponent)(new TextComponentTranslation("advMode.notEnabled", new Object[0])));
            return;
         }

         if (!this.playerEntity.canUseCommandBlock()) {
            this.playerEntity.sendMessage((ITextComponent)(new TextComponentTranslation("advMode.notAllowed", new Object[0])));
            return;
         }

         PacketBuffer packetdataserializer = packetplayincustompayload.getBufferData();

         try {
            byte b0 = packetdataserializer.readByte();
            CommandBlockBaseLogic commandblocklistenerabstract = null;
            if (b0 == 0) {
               TileEntity tileentity = this.playerEntity.world.getTileEntity(new BlockPos(packetdataserializer.readInt(), packetdataserializer.readInt(), packetdataserializer.readInt()));
               if (tileentity instanceof TileEntityCommandBlock) {
                  commandblocklistenerabstract = ((TileEntityCommandBlock)tileentity).getCommandBlockLogic();
               }
            } else if (b0 == 1) {
               Entity entity = this.playerEntity.world.getEntityByID(packetdataserializer.readInt());
               if (entity instanceof EntityMinecartCommandBlock) {
                  commandblocklistenerabstract = ((EntityMinecartCommandBlock)entity).getCommandBlockLogic();
               }
            }

            String s2 = packetdataserializer.readString(packetdataserializer.readableBytes());
            boolean flag = packetdataserializer.readBoolean();
            if (commandblocklistenerabstract != null) {
               commandblocklistenerabstract.setCommand(s2);
               commandblocklistenerabstract.setTrackOutput(flag);
               if (!flag) {
                  commandblocklistenerabstract.setLastOutput((ITextComponent)null);
               }

               commandblocklistenerabstract.updateCommand();
               this.playerEntity.sendMessage((ITextComponent)(new TextComponentTranslation("advMode.setCommand.success", new Object[]{s2})));
            }
         } catch (Exception var27) {
            LOGGER.error("Couldn't set command block", var27);
            this.disconnect("Invalid command data!");
         }
      } else if ("MC|AutoCmd".equals(s)) {
         if (!this.serverController.getEnableCommandBlock()) {
            this.playerEntity.sendMessage((ITextComponent)(new TextComponentTranslation("advMode.notEnabled", new Object[0])));
            return;
         }

         if (!this.playerEntity.canUseCommandBlock()) {
            this.playerEntity.sendMessage((ITextComponent)(new TextComponentTranslation("advMode.notAllowed", new Object[0])));
            return;
         }

         PacketBuffer packetdataserializer = packetplayincustompayload.getBufferData();

         try {
            CommandBlockBaseLogic commandblocklistenerabstract1 = null;
            TileEntityCommandBlock tileentitycommand = null;
            BlockPos blockposition = new BlockPos(packetdataserializer.readInt(), packetdataserializer.readInt(), packetdataserializer.readInt());
            TileEntity tileentity1 = this.playerEntity.world.getTileEntity(blockposition);
            if (tileentity1 instanceof TileEntityCommandBlock) {
               tileentitycommand = (TileEntityCommandBlock)tileentity1;
               commandblocklistenerabstract1 = tileentitycommand.getCommandBlockLogic();
            }

            String s1 = packetdataserializer.readString(packetdataserializer.readableBytes());
            boolean flag1 = packetdataserializer.readBoolean();
            TileEntityCommandBlock.Mode tileentitycommand_type = TileEntityCommandBlock.Mode.valueOf(packetdataserializer.readString(16));
            boolean flag2 = packetdataserializer.readBoolean();
            boolean flag3 = packetdataserializer.readBoolean();
            if (commandblocklistenerabstract1 != null) {
               EnumFacing enumdirection = (EnumFacing)this.playerEntity.world.getBlockState(blockposition).getValue(BlockCommandBlock.FACING);
               switch(NetHandlerPlayServer.SyntheticClass_1.d[tileentitycommand_type.ordinal()]) {
               case 1:
                  IBlockState iblockdata = Blocks.CHAIN_COMMAND_BLOCK.getDefaultState();
                  this.playerEntity.world.setBlockState(blockposition, iblockdata.withProperty(BlockCommandBlock.FACING, enumdirection).withProperty(BlockCommandBlock.CONDITIONAL, Boolean.valueOf(flag2)), 2);
                  break;
               case 2:
                  IBlockState iblockdata = Blocks.REPEATING_COMMAND_BLOCK.getDefaultState();
                  this.playerEntity.world.setBlockState(blockposition, iblockdata.withProperty(BlockCommandBlock.FACING, enumdirection).withProperty(BlockCommandBlock.CONDITIONAL, Boolean.valueOf(flag2)), 2);
                  break;
               case 3:
                  IBlockState iblockdata = Blocks.COMMAND_BLOCK.getDefaultState();
                  this.playerEntity.world.setBlockState(blockposition, iblockdata.withProperty(BlockCommandBlock.FACING, enumdirection).withProperty(BlockCommandBlock.CONDITIONAL, Boolean.valueOf(flag2)), 2);
               }

               tileentity1.validate();
               this.playerEntity.world.setTileEntity(blockposition, tileentity1);
               commandblocklistenerabstract1.setCommand(s1);
               commandblocklistenerabstract1.setTrackOutput(flag1);
               if (!flag1) {
                  commandblocklistenerabstract1.setLastOutput((ITextComponent)null);
               }

               tileentitycommand.setAuto(flag3);
               commandblocklistenerabstract1.updateCommand();
               if (!net.minecraft.util.StringUtils.isNullOrEmpty(s1)) {
                  this.playerEntity.sendMessage((ITextComponent)(new TextComponentTranslation("advMode.setCommand.success", new Object[]{s1})));
               }
            }
         } catch (Exception var26) {
            LOGGER.error("Couldn't set command block", var26);
            this.disconnect("Invalid command data!");
         }
      } else if ("MC|Beacon".equals(s)) {
         if (this.playerEntity.openContainer instanceof ContainerBeacon) {
            try {
               PacketBuffer packetdataserializer = packetplayincustompayload.getBufferData();
               int k = packetdataserializer.readInt();
               int l = packetdataserializer.readInt();
               ContainerBeacon containerbeacon = (ContainerBeacon)this.playerEntity.openContainer;
               Slot slot = containerbeacon.getSlot(0);
               if (slot.getHasStack()) {
                  slot.decrStackSize(1);
                  IInventory iinventory = containerbeacon.getTileEntity();
                  iinventory.setField(1, k);
                  iinventory.setField(2, l);
                  iinventory.markDirty();
               }
            } catch (Exception var25) {
               LOGGER.error("Couldn't set beacon", var25);
               this.disconnect("Invalid beacon data!");
            }
         }
      } else if ("MC|ItemName".equals(s)) {
         if (this.playerEntity.openContainer instanceof ContainerRepair) {
            ContainerRepair containeranvil = (ContainerRepair)this.playerEntity.openContainer;
            if (packetplayincustompayload.getBufferData() != null && packetplayincustompayload.getBufferData().readableBytes() >= 1) {
               String s3 = ChatAllowedCharacters.filterAllowedCharacters(packetplayincustompayload.getBufferData().readString(32767));
               if (s3.length() <= 30) {
                  containeranvil.updateItemName(s3);
               }
            } else {
               containeranvil.updateItemName("");
            }
         }
      } else if ("MC|Struct".equals(s)) {
         if (!this.playerEntity.canUseCommandBlock()) {
            return;
         }

         PacketBuffer packetdataserializer = packetplayincustompayload.getBufferData();

         try {
            BlockPos blockposition1 = new BlockPos(packetdataserializer.readInt(), packetdataserializer.readInt(), packetdataserializer.readInt());
            IBlockState iblockdata1 = this.playerEntity.world.getBlockState(blockposition1);
            TileEntity tileentity = this.playerEntity.world.getTileEntity(blockposition1);
            if (tileentity instanceof TileEntityStructure) {
               TileEntityStructure tileentitystructure = (TileEntityStructure)tileentity;
               byte b1 = packetdataserializer.readByte();
               String s4 = packetdataserializer.readString(32);
               tileentitystructure.setMode(TileEntityStructure.Mode.valueOf(s4));
               tileentitystructure.setName(packetdataserializer.readString(64));
               int i1 = MathHelper.clamp(packetdataserializer.readInt(), -32, 32);
               int j1 = MathHelper.clamp(packetdataserializer.readInt(), -32, 32);
               int k1 = MathHelper.clamp(packetdataserializer.readInt(), -32, 32);
               tileentitystructure.setPosition(new BlockPos(i1, j1, k1));
               int l1 = MathHelper.clamp(packetdataserializer.readInt(), 0, 32);
               int i2 = MathHelper.clamp(packetdataserializer.readInt(), 0, 32);
               int j2 = MathHelper.clamp(packetdataserializer.readInt(), 0, 32);
               tileentitystructure.setSize(new BlockPos(l1, i2, j2));
               String s5 = packetdataserializer.readString(32);
               tileentitystructure.setMirror(Mirror.valueOf(s5));
               String s6 = packetdataserializer.readString(32);
               tileentitystructure.setRotation(Rotation.valueOf(s6));
               tileentitystructure.setMetadata(packetdataserializer.readString(128));
               tileentitystructure.setIgnoresEntities(packetdataserializer.readBoolean());
               tileentitystructure.setShowAir(packetdataserializer.readBoolean());
               tileentitystructure.setShowBoundingBox(packetdataserializer.readBoolean());
               tileentitystructure.setIntegrity(MathHelper.clamp(packetdataserializer.readFloat(), 0.0F, 1.0F));
               tileentitystructure.setSeed(packetdataserializer.readVarLong());
               String s7 = tileentitystructure.getName();
               if (b1 == 2) {
                  if (tileentitystructure.save()) {
                     this.playerEntity.sendStatusMessage(new TextComponentTranslation("structure_block.save_success", new Object[]{s7}));
                  } else {
                     this.playerEntity.sendStatusMessage(new TextComponentTranslation("structure_block.save_failure", new Object[]{s7}));
                  }
               } else if (b1 == 3) {
                  if (!tileentitystructure.isStructureLoadable()) {
                     this.playerEntity.sendStatusMessage(new TextComponentTranslation("structure_block.load_not_found", new Object[]{s7}));
                  } else if (tileentitystructure.load()) {
                     this.playerEntity.sendStatusMessage(new TextComponentTranslation("structure_block.load_success", new Object[]{s7}));
                  } else {
                     this.playerEntity.sendStatusMessage(new TextComponentTranslation("structure_block.load_prepare", new Object[]{s7}));
                  }
               } else if (b1 == 4) {
                  if (tileentitystructure.detectSize()) {
                     this.playerEntity.sendStatusMessage(new TextComponentTranslation("structure_block.size_success", new Object[]{s7}));
                  } else {
                     this.playerEntity.sendStatusMessage(new TextComponentTranslation("structure_block.size_failure", new Object[0]));
                  }
               }

               tileentitystructure.markDirty();
               this.playerEntity.world.notifyBlockUpdate(blockposition1, iblockdata1, iblockdata1, 3);
            }
         } catch (Exception var24) {
            LOGGER.error("Couldn't set structure block", var24);
            this.disconnect("Invalid structure data!");
         }
      } else if ("MC|PickItem".equals(s)) {
         PacketBuffer packetdataserializer = packetplayincustompayload.getBufferData();

         try {
            int k = packetdataserializer.readVarInt();
            this.playerEntity.inventory.pickItem(k);
            this.playerEntity.connection.sendPacket(new SPacketSetSlot(-2, this.playerEntity.inventory.currentItem, this.playerEntity.inventory.getStackInSlot(this.playerEntity.inventory.currentItem)));
            this.playerEntity.connection.sendPacket(new SPacketSetSlot(-2, k, this.playerEntity.inventory.getStackInSlot(k)));
            this.playerEntity.connection.sendPacket(new SPacketHeldItemChange(this.playerEntity.inventory.currentItem));
         } catch (Exception var23) {
            LOGGER.error("Couldn't pick item", var23);
            this.disconnect("Invalid item data!");
         }
      } else if (packetplayincustompayload.getChannelName().equals("REGISTER")) {
         String channels = packetplayincustompayload.getBufferData().toString(Charsets.UTF_8);

         String[] var76;
         for(String channel : var76 = channels.split("\u0000")) {
            this.getPlayer().addChannel(channel);
         }
      } else if (packetplayincustompayload.getChannelName().equals("UNREGISTER")) {
         String channels = packetplayincustompayload.getBufferData().toString(Charsets.UTF_8);

         String[] var77;
         for(String channel : var77 = channels.split("\u0000")) {
            this.getPlayer().removeChannel(channel);
         }
      } else {
         byte[] data = new byte[packetplayincustompayload.getBufferData().readableBytes()];
         packetplayincustompayload.getBufferData().readBytes(data);
         this.server.getMessenger().dispatchIncomingMessage(this.playerEntity.getBukkitEntity(), packetplayincustompayload.getChannelName(), data);
      }

   }

   public final boolean isDisconnected() {
      return !this.playerEntity.joining && !this.netManager.isChannelOpen();
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$net$minecraft$server$PacketPlayInEntityAction$EnumPlayerAction() {
      int[] var10000 = $SWITCH_TABLE$net$minecraft$server$PacketPlayInEntityAction$EnumPlayerAction;
      if ($SWITCH_TABLE$net$minecraft$server$PacketPlayInEntityAction$EnumPlayerAction != null) {
         return var10000;
      } else {
         int[] var0 = new int[CPacketEntityAction.Action.values().length];

         try {
            var0[CPacketEntityAction.Action.OPEN_INVENTORY.ordinal()] = 8;
         } catch (NoSuchFieldError var9) {
            ;
         }

         try {
            var0[CPacketEntityAction.Action.START_FALL_FLYING.ordinal()] = 9;
         } catch (NoSuchFieldError var8) {
            ;
         }

         try {
            var0[CPacketEntityAction.Action.START_RIDING_JUMP.ordinal()] = 6;
         } catch (NoSuchFieldError var7) {
            ;
         }

         try {
            var0[CPacketEntityAction.Action.START_SNEAKING.ordinal()] = 1;
         } catch (NoSuchFieldError var6) {
            ;
         }

         try {
            var0[CPacketEntityAction.Action.START_SPRINTING.ordinal()] = 4;
         } catch (NoSuchFieldError var5) {
            ;
         }

         try {
            var0[CPacketEntityAction.Action.STOP_RIDING_JUMP.ordinal()] = 7;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            var0[CPacketEntityAction.Action.STOP_SLEEPING.ordinal()] = 3;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            var0[CPacketEntityAction.Action.STOP_SNEAKING.ordinal()] = 2;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            var0[CPacketEntityAction.Action.STOP_SPRINTING.ordinal()] = 5;
         } catch (NoSuchFieldError var1) {
            ;
         }

         $SWITCH_TABLE$net$minecraft$server$PacketPlayInEntityAction$EnumPlayerAction = var0;
         return var0;
      }
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$net$minecraft$server$InventoryClickType() {
      int[] var10000 = $SWITCH_TABLE$net$minecraft$server$InventoryClickType;
      if ($SWITCH_TABLE$net$minecraft$server$InventoryClickType != null) {
         return var10000;
      } else {
         int[] var0 = new int[net.minecraft.inventory.ClickType.values().length];

         try {
            var0[net.minecraft.inventory.ClickType.CLONE.ordinal()] = 4;
         } catch (NoSuchFieldError var7) {
            ;
         }

         try {
            var0[net.minecraft.inventory.ClickType.PICKUP.ordinal()] = 1;
         } catch (NoSuchFieldError var6) {
            ;
         }

         try {
            var0[net.minecraft.inventory.ClickType.PICKUP_ALL.ordinal()] = 7;
         } catch (NoSuchFieldError var5) {
            ;
         }

         try {
            var0[net.minecraft.inventory.ClickType.QUICK_CRAFT.ordinal()] = 6;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            var0[net.minecraft.inventory.ClickType.QUICK_MOVE.ordinal()] = 2;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            var0[net.minecraft.inventory.ClickType.SWAP.ordinal()] = 3;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            var0[net.minecraft.inventory.ClickType.THROW.ordinal()] = 5;
         } catch (NoSuchFieldError var1) {
            ;
         }

         $SWITCH_TABLE$net$minecraft$server$InventoryClickType = var0;
         return var0;
      }
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$org$bukkit$event$inventory$InventoryAction() {
      int[] var10000 = $SWITCH_TABLE$org$bukkit$event$inventory$InventoryAction;
      if ($SWITCH_TABLE$org$bukkit$event$inventory$InventoryAction != null) {
         return var10000;
      } else {
         int[] var0 = new int[InventoryAction.values().length];

         try {
            var0[InventoryAction.CLONE_STACK.ordinal()] = 17;
         } catch (NoSuchFieldError var19) {
            ;
         }

         try {
            var0[InventoryAction.COLLECT_TO_CURSOR.ordinal()] = 18;
         } catch (NoSuchFieldError var18) {
            ;
         }

         try {
            var0[InventoryAction.DROP_ALL_CURSOR.ordinal()] = 10;
         } catch (NoSuchFieldError var17) {
            ;
         }

         try {
            var0[InventoryAction.DROP_ALL_SLOT.ordinal()] = 12;
         } catch (NoSuchFieldError var16) {
            ;
         }

         try {
            var0[InventoryAction.DROP_ONE_CURSOR.ordinal()] = 11;
         } catch (NoSuchFieldError var15) {
            ;
         }

         try {
            var0[InventoryAction.DROP_ONE_SLOT.ordinal()] = 13;
         } catch (NoSuchFieldError var14) {
            ;
         }

         try {
            var0[InventoryAction.HOTBAR_MOVE_AND_READD.ordinal()] = 15;
         } catch (NoSuchFieldError var13) {
            ;
         }

         try {
            var0[InventoryAction.HOTBAR_SWAP.ordinal()] = 16;
         } catch (NoSuchFieldError var12) {
            ;
         }

         try {
            var0[InventoryAction.MOVE_TO_OTHER_INVENTORY.ordinal()] = 14;
         } catch (NoSuchFieldError var11) {
            ;
         }

         try {
            var0[InventoryAction.NOTHING.ordinal()] = 1;
         } catch (NoSuchFieldError var10) {
            ;
         }

         try {
            var0[InventoryAction.PICKUP_ALL.ordinal()] = 2;
         } catch (NoSuchFieldError var9) {
            ;
         }

         try {
            var0[InventoryAction.PICKUP_HALF.ordinal()] = 4;
         } catch (NoSuchFieldError var8) {
            ;
         }

         try {
            var0[InventoryAction.PICKUP_ONE.ordinal()] = 5;
         } catch (NoSuchFieldError var7) {
            ;
         }

         try {
            var0[InventoryAction.PICKUP_SOME.ordinal()] = 3;
         } catch (NoSuchFieldError var6) {
            ;
         }

         try {
            var0[InventoryAction.PLACE_ALL.ordinal()] = 6;
         } catch (NoSuchFieldError var5) {
            ;
         }

         try {
            var0[InventoryAction.PLACE_ONE.ordinal()] = 8;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            var0[InventoryAction.PLACE_SOME.ordinal()] = 7;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            var0[InventoryAction.SWAP_WITH_CURSOR.ordinal()] = 9;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            var0[InventoryAction.UNKNOWN.ordinal()] = 19;
         } catch (NoSuchFieldError var1) {
            ;
         }

         $SWITCH_TABLE$org$bukkit$event$inventory$InventoryAction = var0;
         return var0;
      }
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$org$bukkit$event$Event$Result() {
      int[] var10000 = $SWITCH_TABLE$org$bukkit$event$Event$Result;
      if ($SWITCH_TABLE$org$bukkit$event$Event$Result != null) {
         return var10000;
      } else {
         int[] var0 = new int[Result.values().length];

         try {
            var0[Result.ALLOW.ordinal()] = 3;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            var0[Result.DEFAULT.ordinal()] = 2;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            var0[Result.DENY.ordinal()] = 1;
         } catch (NoSuchFieldError var1) {
            ;
         }

         $SWITCH_TABLE$org$bukkit$event$Event$Result = var0;
         return var0;
      }
   }

   static class SyntheticClass_1 {
      static final int[] a;
      static final int[] b;
      static final int[] c;
      static final int[] d = new int[TileEntityCommandBlock.Mode.values().length];

      static {
         try {
            d[TileEntityCommandBlock.Mode.SEQUENCE.ordinal()] = 1;
         } catch (NoSuchFieldError var21) {
            ;
         }

         try {
            d[TileEntityCommandBlock.Mode.AUTO.ordinal()] = 2;
         } catch (NoSuchFieldError var20) {
            ;
         }

         try {
            d[TileEntityCommandBlock.Mode.REDSTONE.ordinal()] = 3;
         } catch (NoSuchFieldError var19) {
            ;
         }

         c = new int[CPacketClientStatus.State.values().length];

         try {
            c[CPacketClientStatus.State.PERFORM_RESPAWN.ordinal()] = 1;
         } catch (NoSuchFieldError var18) {
            ;
         }

         try {
            c[CPacketClientStatus.State.REQUEST_STATS.ordinal()] = 2;
         } catch (NoSuchFieldError var17) {
            ;
         }

         try {
            c[CPacketClientStatus.State.OPEN_INVENTORY_ACHIEVEMENT.ordinal()] = 3;
         } catch (NoSuchFieldError var16) {
            ;
         }

         b = new int[CPacketEntityAction.Action.values().length];

         try {
            b[CPacketEntityAction.Action.START_SNEAKING.ordinal()] = 1;
         } catch (NoSuchFieldError var15) {
            ;
         }

         try {
            b[CPacketEntityAction.Action.STOP_SNEAKING.ordinal()] = 2;
         } catch (NoSuchFieldError var14) {
            ;
         }

         try {
            b[CPacketEntityAction.Action.START_SPRINTING.ordinal()] = 3;
         } catch (NoSuchFieldError var13) {
            ;
         }

         try {
            b[CPacketEntityAction.Action.STOP_SPRINTING.ordinal()] = 4;
         } catch (NoSuchFieldError var12) {
            ;
         }

         try {
            b[CPacketEntityAction.Action.STOP_SLEEPING.ordinal()] = 5;
         } catch (NoSuchFieldError var11) {
            ;
         }

         try {
            b[CPacketEntityAction.Action.START_RIDING_JUMP.ordinal()] = 6;
         } catch (NoSuchFieldError var10) {
            ;
         }

         try {
            b[CPacketEntityAction.Action.STOP_RIDING_JUMP.ordinal()] = 7;
         } catch (NoSuchFieldError var9) {
            ;
         }

         try {
            b[CPacketEntityAction.Action.OPEN_INVENTORY.ordinal()] = 8;
         } catch (NoSuchFieldError var8) {
            ;
         }

         try {
            b[CPacketEntityAction.Action.START_FALL_FLYING.ordinal()] = 9;
         } catch (NoSuchFieldError var7) {
            ;
         }

         a = new int[CPacketPlayerDigging.Action.values().length];

         try {
            a[CPacketPlayerDigging.Action.SWAP_HELD_ITEMS.ordinal()] = 1;
         } catch (NoSuchFieldError var6) {
            ;
         }

         try {
            a[CPacketPlayerDigging.Action.DROP_ITEM.ordinal()] = 2;
         } catch (NoSuchFieldError var5) {
            ;
         }

         try {
            a[CPacketPlayerDigging.Action.DROP_ALL_ITEMS.ordinal()] = 3;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            a[CPacketPlayerDigging.Action.RELEASE_USE_ITEM.ordinal()] = 4;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            a[CPacketPlayerDigging.Action.START_DESTROY_BLOCK.ordinal()] = 5;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            a[CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK.ordinal()] = 6;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK.ordinal()] = 7;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
