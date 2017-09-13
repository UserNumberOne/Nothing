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
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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

   public NetHandlerPlayServer(MinecraftServer var1, NetworkManager var2, EntityPlayerMP var3) {
      this.serverController = var1;
      this.netManager = var2;
      var2.setNetHandler(this);
      this.playerEntity = var3;
      var3.connection = this;
      this.server = var1.server;
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
         int var1 = this.chatSpamThresholdCount;
         if (this.chatSpamThresholdCount <= 0 || chatSpamField.compareAndSet(this, var1, var1 - 1)) {
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

   public void disconnect(String var1) {
      if (!this.processedDisconnect) {
         String var2 = TextFormatting.YELLOW + this.playerEntity.getName() + " left the game.";
         PlayerKickEvent var3 = new PlayerKickEvent(this.server.getPlayer(this.playerEntity), var1, var2);
         if (this.server.getServer().isRunning()) {
            this.server.getPluginManager().callEvent(var3);
         }

         if (!var3.isCancelled()) {
            var1 = var3.getReason();
            final TextComponentString var4 = new TextComponentString(var1);
            this.netManager.sendPacket(new SPacketDisconnect(var4), new GenericFutureListener() {
               public void operationComplete(Future var1) throws Exception {
                  NetHandlerPlayServer.this.netManager.closeChannel(var4);
               }
            });
            this.onDisconnect(var4);
            this.netManager.disableAutoRead();
            this.serverController.addScheduledTask(new Runnable() {
               public void run() {
                  NetHandlerPlayServer.this.netManager.checkDisconnected();
               }
            });
         }
      }
   }

   public void processInput(CPacketInput var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      this.playerEntity.setEntityActionState(var1.getStrafeSpeed(), var1.getForwardSpeed(), var1.isJumping(), var1.isSneaking());
   }

   private static boolean isMovePlayerPacketInvalid(CPacketPlayer var0) {
      return Doubles.isFinite(var0.getX(0.0D)) && Doubles.isFinite(var0.getY(0.0D)) && Doubles.isFinite(var0.getZ(0.0D)) && Floats.isFinite(var0.getPitch(0.0F)) && Floats.isFinite(var0.getYaw(0.0F)) ? false : Math.abs(var0.getX(0.0D)) <= 3.0E7D && Math.abs(var0.getX(0.0D)) <= 3.0E7D;
   }

   private static boolean isMoveVehiclePacketInvalid(CPacketVehicleMove var0) {
      return !Doubles.isFinite(var0.getX()) || !Doubles.isFinite(var0.getY()) || !Doubles.isFinite(var0.getZ()) || !Floats.isFinite(var0.getPitch()) || !Floats.isFinite(var0.getYaw());
   }

   public void processVehicleMove(CPacketVehicleMove var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      if (isMoveVehiclePacketInvalid(var1)) {
         this.disconnect("Invalid move vehicle packet received");
      } else {
         Entity var2 = this.playerEntity.getLowestRidingEntity();
         if (var2 != this.playerEntity && var2.getControllingPassenger() == this.playerEntity && var2 == this.lowestRiddenEnt) {
            WorldServer var3 = this.playerEntity.getServerWorld();
            double var4 = var2.posX;
            double var6 = var2.posY;
            double var8 = var2.posZ;
            double var10 = var1.getX();
            double var12 = var1.getY();
            double var14 = var1.getZ();
            float var16 = var1.getYaw();
            float var17 = var1.getPitch();
            double var18 = var10 - this.lowestRiddenX;
            double var20 = var12 - this.lowestRiddenY;
            double var22 = var14 - this.lowestRiddenZ;
            double var24 = var2.motionX * var2.motionX + var2.motionY * var2.motionY + var2.motionZ * var2.motionZ;
            double var26 = var18 * var18 + var20 * var20 + var22 * var22;
            this.allowedPlayerTicks = (int)((long)this.allowedPlayerTicks + (System.currentTimeMillis() / 50L - (long)this.lastTick));
            this.allowedPlayerTicks = Math.max(this.allowedPlayerTicks, 1);
            this.lastTick = (int)(System.currentTimeMillis() / 50L);
            ++this.movePacketCounter;
            int var28 = this.movePacketCounter - this.lastMovePacketCounter;
            if (var28 > Math.max(this.allowedPlayerTicks, 5)) {
               LOGGER.debug(this.playerEntity.getName() + " is sending move packets too frequently (" + var28 + " packets since last tick)");
               var28 = 1;
            }

            if (var26 > 0.0D) {
               --this.allowedPlayerTicks;
            } else {
               this.allowedPlayerTicks = 20;
            }

            float var29;
            if (this.playerEntity.capabilities.isFlying) {
               var29 = this.playerEntity.capabilities.flySpeed * 20.0F;
            } else {
               var29 = this.playerEntity.capabilities.walkSpeed * 10.0F;
            }

            var29 = var29 * 2.0F;
            if (var26 - var24 > Math.max(100.0D, Math.pow((double)(10.0F * (float)var28 * var29), 2.0D)) && (!this.serverController.R() || !this.serverController.Q().equals(var2.getName()))) {
               LOGGER.warn("{} (vehicle of {}) moved too quickly! {},{},{}", new Object[]{var2.getName(), this.playerEntity.getName(), var18, var20, var22});
               this.netManager.sendPacket(new SPacketMoveVehicle(var2));
               return;
            }

            boolean var30 = var3.getCollisionBoxes(var2, var2.getEntityBoundingBox().contract(0.0625D)).isEmpty();
            var18 = var10 - this.lowestRiddenX1;
            var20 = var12 - this.lowestRiddenY1 - 1.0E-6D;
            var22 = var14 - this.lowestRiddenZ1;
            var2.move(var18, var20, var22);
            var18 = var10 - var2.posX;
            var20 = var12 - var2.posY;
            if (var20 > -0.5D || var20 < 0.5D) {
               var20 = 0.0D;
            }

            var22 = var14 - var2.posZ;
            var26 = var18 * var18 + var20 * var20 + var22 * var22;
            boolean var33 = false;
            if (var26 > 0.0625D) {
               var33 = true;
               LOGGER.warn("{} moved wrongly!", new Object[]{var2.getName()});
            }

            var2.setPositionAndRotation(var10, var12, var14, var16, var17);
            boolean var34 = var3.getCollisionBoxes(var2, var2.getEntityBoundingBox().contract(0.0625D)).isEmpty();
            if (var30 && (var33 || !var34)) {
               var2.setPositionAndRotation(var4, var6, var8, var16, var17);
               this.netManager.sendPacket(new SPacketMoveVehicle(var2));
               return;
            }

            CraftPlayer var35 = this.getPlayer();
            Location var36 = new Location(var35.getWorld(), this.lastPosX, this.lastPosY, this.lastPosZ, this.lastYaw, this.lastPitch);
            Location var37 = var35.getLocation().clone();
            var37.setX(var1.getX());
            var37.setY(var1.getY());
            var37.setZ(var1.getZ());
            var37.setYaw(var1.getYaw());
            var37.setPitch(var1.getPitch());
            double var38 = Math.pow(this.lastPosX - var37.getX(), 2.0D) + Math.pow(this.lastPosY - var37.getY(), 2.0D) + Math.pow(this.lastPosZ - var37.getZ(), 2.0D);
            float var40 = Math.abs(this.lastYaw - var37.getYaw()) + Math.abs(this.lastPitch - var37.getPitch());
            if ((var38 > 0.00390625D || var40 > 10.0F) && !this.playerEntity.isMovementBlocked()) {
               this.lastPosX = var37.getX();
               this.lastPosY = var37.getY();
               this.lastPosZ = var37.getZ();
               this.lastYaw = var37.getYaw();
               this.lastPitch = var37.getPitch();
               if (var36.getX() != Double.MAX_VALUE) {
                  Location var41 = var37.clone();
                  PlayerMoveEvent var42 = new PlayerMoveEvent(var35, var36, var37);
                  this.server.getPluginManager().callEvent(var42);
                  if (var42.isCancelled()) {
                     this.teleport(var36);
                     return;
                  }

                  if (!var41.equals(var42.getTo()) && !var42.isCancelled()) {
                     this.playerEntity.getBukkitEntity().teleport(var42.getTo(), TeleportCause.UNKNOWN);
                     return;
                  }

                  if (!var36.equals(this.getPlayer().getLocation()) && this.justTeleported) {
                     this.justTeleported = false;
                     return;
                  }
               }
            }

            this.serverController.getPlayerList().serverUpdateMovingPlayer(this.playerEntity);
            this.playerEntity.addMovementStat(this.playerEntity.posX - var4, this.playerEntity.posY - var6, this.playerEntity.posZ - var8);
            this.vehicleFloating = var20 >= -0.03125D && !this.serverController.getAllowFlight() && !var3.checkBlockCollision(var2.getEntityBoundingBox().expandXyz(0.0625D).addCoord(0.0D, -0.55D, 0.0D));
            this.lowestRiddenX1 = var2.posX;
            this.lowestRiddenY1 = var2.posY;
            this.lowestRiddenZ1 = var2.posZ;
         }
      }

   }

   public void processConfirmTeleport(CPacketConfirmTeleport var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      if (var1.getTeleportId() == this.teleportId) {
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

   public void processPlayer(CPacketPlayer var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      if (isMovePlayerPacketInvalid(var1)) {
         this.disconnect("Invalid move player packet received");
      } else {
         WorldServer var2 = this.serverController.getWorldServer(this.playerEntity.dimension);
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
                  this.playerEntity.setPositionAndRotation(this.playerEntity.posX, this.playerEntity.posY, this.playerEntity.posZ, var1.getYaw(this.playerEntity.rotationYaw), var1.getPitch(this.playerEntity.rotationPitch));
                  this.serverController.getPlayerList().serverUpdateMovingPlayer(this.playerEntity);
                  this.allowedPlayerTicks = 20;
               } else {
                  double var3 = this.playerEntity.posX;
                  double var5 = this.playerEntity.posY;
                  double var7 = this.playerEntity.posZ;
                  float var9 = this.playerEntity.rotationYaw;
                  float var10 = this.playerEntity.rotationPitch;
                  double var11 = this.playerEntity.posX;
                  double var13 = this.playerEntity.posY;
                  double var15 = this.playerEntity.posZ;
                  double var17 = this.playerEntity.posY;
                  double var19 = var1.getX(this.playerEntity.posX);
                  double var21 = var1.getY(this.playerEntity.posY);
                  double var23 = var1.getZ(this.playerEntity.posZ);
                  float var25 = var1.getYaw(this.playerEntity.rotationYaw);
                  float var26 = var1.getPitch(this.playerEntity.rotationPitch);
                  double var27 = var19 - this.firstGoodX;
                  double var29 = var21 - this.firstGoodY;
                  double var31 = var23 - this.firstGoodZ;
                  double var33 = this.playerEntity.motionX * this.playerEntity.motionX + this.playerEntity.motionY * this.playerEntity.motionY + this.playerEntity.motionZ * this.playerEntity.motionZ;
                  double var35 = var27 * var27 + var29 * var29 + var31 * var31;
                  ++this.movePacketCounter;
                  int var37 = this.movePacketCounter - this.lastMovePacketCounter;
                  this.allowedPlayerTicks = (int)((long)this.allowedPlayerTicks + (System.currentTimeMillis() / 50L - (long)this.lastTick));
                  this.allowedPlayerTicks = Math.max(this.allowedPlayerTicks, 1);
                  this.lastTick = (int)(System.currentTimeMillis() / 50L);
                  if (var37 > Math.max(this.allowedPlayerTicks, 5)) {
                     LOGGER.debug(this.playerEntity.getName() + " is sending move packets too frequently (" + var37 + " packets since last tick)");
                     var37 = 1;
                  }

                  if (!var1.rotating && var35 <= 0.0D) {
                     this.allowedPlayerTicks = 20;
                  } else {
                     --this.allowedPlayerTicks;
                  }

                  float var38;
                  if (this.playerEntity.capabilities.isFlying) {
                     var38 = this.playerEntity.capabilities.flySpeed * 20.0F;
                  } else {
                     var38 = this.playerEntity.capabilities.walkSpeed * 10.0F;
                  }

                  if (!this.playerEntity.isInvulnerableDimensionChange() && (!this.playerEntity.getServerWorld().getGameRules().getBoolean("disableElytraMovementCheck") || !this.playerEntity.isElytraFlying())) {
                     if (this.playerEntity.isElytraFlying()) {
                        ;
                     }

                     if (var35 - var33 > Math.max(100.0D, Math.pow((double)(10.0F * (float)var37 * var38), 2.0D)) && (!this.serverController.R() || !this.serverController.Q().equals(this.playerEntity.getName()))) {
                        LOGGER.warn("{} moved too quickly! {},{},{}", new Object[]{this.playerEntity.getName(), var27, var29, var31});
                        this.setPlayerLocation(this.playerEntity.posX, this.playerEntity.posY, this.playerEntity.posZ, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
                        return;
                     }
                  }

                  boolean var39 = var2.getCollisionBoxes(this.playerEntity, this.playerEntity.getEntityBoundingBox().contract(0.0625D)).isEmpty();
                  var27 = var19 - this.lastGoodX;
                  var29 = var21 - this.lastGoodY;
                  var31 = var23 - this.lastGoodZ;
                  if (this.playerEntity.onGround && !var1.isOnGround() && var29 > 0.0D) {
                     this.playerEntity.jump();
                  }

                  this.playerEntity.move(var27, var29, var31);
                  this.playerEntity.onGround = var1.isOnGround();
                  var27 = var19 - this.playerEntity.posX;
                  var29 = var21 - this.playerEntity.posY;
                  if (var29 > -0.5D || var29 < 0.5D) {
                     var29 = 0.0D;
                  }

                  var31 = var23 - this.playerEntity.posZ;
                  var35 = var27 * var27 + var29 * var29 + var31 * var31;
                  boolean var42 = false;
                  if (!this.playerEntity.isInvulnerableDimensionChange() && var35 > 0.0625D && !this.playerEntity.isPlayerSleeping() && !this.playerEntity.interactionManager.isCreative() && this.playerEntity.interactionManager.getGameType() != GameType.SPECTATOR) {
                     var42 = true;
                     LOGGER.warn("{} moved wrongly!", new Object[]{this.playerEntity.getName()});
                  }

                  this.playerEntity.setPositionAndRotation(var19, var21, var23, var25, var26);
                  this.playerEntity.addMovementStat(this.playerEntity.posX - var11, this.playerEntity.posY - var13, this.playerEntity.posZ - var15);
                  if (!this.playerEntity.noClip && !this.playerEntity.isPlayerSleeping()) {
                     boolean var43 = var2.getCollisionBoxes(this.playerEntity, this.playerEntity.getEntityBoundingBox().contract(0.0625D)).isEmpty();
                     if (var39 && (var42 || !var43)) {
                        this.setPlayerLocation(var11, var13, var15, var25, var26);
                        return;
                     }
                  }

                  this.playerEntity.setPositionAndRotation(var3, var5, var7, var9, var10);
                  CraftPlayer var58 = this.getPlayer();
                  Location var44 = new Location(var58.getWorld(), this.lastPosX, this.lastPosY, this.lastPosZ, this.lastYaw, this.lastPitch);
                  Location var45 = var58.getLocation().clone();
                  if (var1.moving) {
                     var45.setX(var1.x);
                     var45.setY(var1.y);
                     var45.setZ(var1.z);
                  }

                  if (var1.rotating) {
                     var45.setYaw(var1.yaw);
                     var45.setPitch(var1.pitch);
                  }

                  double var46 = Math.pow(this.lastPosX - var45.getX(), 2.0D) + Math.pow(this.lastPosY - var45.getY(), 2.0D) + Math.pow(this.lastPosZ - var45.getZ(), 2.0D);
                  float var48 = Math.abs(this.lastYaw - var45.getYaw()) + Math.abs(this.lastPitch - var45.getPitch());
                  if ((var46 > 0.00390625D || var48 > 10.0F) && !this.playerEntity.isMovementBlocked()) {
                     this.lastPosX = var45.getX();
                     this.lastPosY = var45.getY();
                     this.lastPosZ = var45.getZ();
                     this.lastYaw = var45.getYaw();
                     this.lastPitch = var45.getPitch();
                     if (var44.getX() != Double.MAX_VALUE) {
                        Location var49 = var45.clone();
                        PlayerMoveEvent var50 = new PlayerMoveEvent(var58, var44, var45);
                        this.server.getPluginManager().callEvent(var50);
                        if (var50.isCancelled()) {
                           this.teleport(var44);
                           return;
                        }

                        if (!var49.equals(var50.getTo()) && !var50.isCancelled()) {
                           this.playerEntity.getBukkitEntity().teleport(var50.getTo(), TeleportCause.UNKNOWN);
                           return;
                        }

                        if (!var44.equals(this.getPlayer().getLocation()) && this.justTeleported) {
                           this.justTeleported = false;
                           return;
                        }
                     }
                  }

                  this.playerEntity.setPositionAndRotation(var19, var21, var23, var25, var26);
                  this.floating = var29 >= -0.03125D;
                  this.floating &= !this.serverController.getAllowFlight() && !this.playerEntity.capabilities.allowFlying;
                  this.floating &= !this.playerEntity.isPotionActive(MobEffects.LEVITATION) && !this.playerEntity.isElytraFlying() && !var2.checkBlockCollision(this.playerEntity.getEntityBoundingBox().expandXyz(0.0625D).addCoord(0.0D, -0.55D, 0.0D));
                  this.playerEntity.onGround = var1.isOnGround();
                  this.serverController.getPlayerList().serverUpdateMovingPlayer(this.playerEntity);
                  this.playerEntity.handleFalling(this.playerEntity.posY - var17, var1.isOnGround());
                  this.lastGoodX = this.playerEntity.posX;
                  this.lastGoodY = this.playerEntity.posY;
                  this.lastGoodZ = this.playerEntity.posZ;
               }
            }
         }
      }

   }

   public void setPlayerLocation(double var1, double var3, double var5, float var7, float var8) {
      this.setPlayerLocation(var1, var3, var5, var7, var8, Collections.emptySet());
   }

   public void setPlayerLocation(double var1, double var3, double var5, float var7, float var8, Set var9) {
      CraftPlayer var10 = this.getPlayer();
      Location var11 = var10.getLocation();
      double var12 = var1;
      double var14 = var3;
      double var16 = var5;
      float var18 = var7;
      float var19 = var8;
      if (var9.contains(SPacketPlayerPosLook.EnumFlags.X)) {
         var12 = var1 + var11.getX();
      }

      if (var9.contains(SPacketPlayerPosLook.EnumFlags.Y)) {
         var14 = var3 + var11.getY();
      }

      if (var9.contains(SPacketPlayerPosLook.EnumFlags.Z)) {
         var16 = var5 + var11.getZ();
      }

      if (var9.contains(SPacketPlayerPosLook.EnumFlags.Y_ROT)) {
         var18 = var7 + var11.getYaw();
      }

      if (var9.contains(SPacketPlayerPosLook.EnumFlags.X_ROT)) {
         var19 = var8 + var11.getPitch();
      }

      Location var20 = new Location(this.getPlayer().getWorld(), var12, var14, var16, var18, var19);
      PlayerTeleportEvent var21 = new PlayerTeleportEvent(var10, var11.clone(), var20.clone(), TeleportCause.UNKNOWN);
      this.server.getPluginManager().callEvent(var21);
      if (var21.isCancelled() || !var20.equals(var21.getTo())) {
         var9.clear();
         var20 = var21.isCancelled() ? var21.getFrom() : var21.getTo();
         var1 = var20.getX();
         var3 = var20.getY();
         var5 = var20.getZ();
         var7 = var20.getYaw();
         var8 = var20.getPitch();
      }

      this.internalTeleport(var1, var3, var5, var7, var8, var9);
   }

   public void teleport(Location var1) {
      this.internalTeleport(var1.getX(), var1.getY(), var1.getZ(), var1.getYaw(), var1.getPitch(), Collections.emptySet());
   }

   private void internalTeleport(double var1, double var3, double var5, float var7, float var8, Set var9) {
      if (Float.isNaN(var7)) {
         var7 = 0.0F;
      }

      if (Float.isNaN(var8)) {
         var8 = 0.0F;
      }

      this.justTeleported = true;
      this.targetPos = new Vec3d(var1, var3, var5);
      if (var9.contains(SPacketPlayerPosLook.EnumFlags.X)) {
         this.targetPos = this.targetPos.addVector(this.playerEntity.posX, 0.0D, 0.0D);
      }

      if (var9.contains(SPacketPlayerPosLook.EnumFlags.Y)) {
         this.targetPos = this.targetPos.addVector(0.0D, this.playerEntity.posY, 0.0D);
      }

      if (var9.contains(SPacketPlayerPosLook.EnumFlags.Z)) {
         this.targetPos = this.targetPos.addVector(0.0D, 0.0D, this.playerEntity.posZ);
      }

      float var10 = var7;
      float var11 = var8;
      if (var9.contains(SPacketPlayerPosLook.EnumFlags.Y_ROT)) {
         var10 = var7 + this.playerEntity.rotationYaw;
      }

      if (var9.contains(SPacketPlayerPosLook.EnumFlags.X_ROT)) {
         var11 = var8 + this.playerEntity.rotationPitch;
      }

      this.lastPosX = this.targetPos.xCoord;
      this.lastPosY = this.targetPos.yCoord;
      this.lastPosZ = this.targetPos.zCoord;
      this.lastYaw = var10;
      this.lastPitch = var11;
      if (++this.teleportId == Integer.MAX_VALUE) {
         this.teleportId = 0;
      }

      this.lastPositionUpdate = this.networkTickCount;
      this.playerEntity.setPositionAndRotation(this.targetPos.xCoord, this.targetPos.yCoord, this.targetPos.zCoord, var10, var11);
      this.playerEntity.connection.sendPacket(new SPacketPlayerPosLook(var1, var3, var5, var7, var8, var9, this.teleportId));
   }

   public void processPlayerDigging(CPacketPlayerDigging var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      if (!this.playerEntity.isMovementBlocked()) {
         WorldServer var2 = this.serverController.getWorldServer(this.playerEntity.dimension);
         BlockPos var3 = var1.getPosition();
         this.playerEntity.markPlayerActive();
         switch(NetHandlerPlayServer.SyntheticClass_1.a[var1.getAction().ordinal()]) {
         case 1:
            if (!this.playerEntity.isSpectator()) {
               PlayerSwapHandItemsEvent var4 = new PlayerSwapHandItemsEvent(this.getPlayer(), CraftItemStack.asBukkitCopy(this.playerEntity.getHeldItem(EnumHand.OFF_HAND)), CraftItemStack.asBukkitCopy(this.playerEntity.getHeldItem(EnumHand.MAIN_HAND)));
               this.server.getPluginManager().callEvent(var4);
               if (var4.isCancelled()) {
                  return;
               }

               ItemStack var15 = CraftItemStack.asNMSCopy(var4.getMainHandItem());
               this.playerEntity.setHeldItem(EnumHand.OFF_HAND, CraftItemStack.asNMSCopy(var4.getOffHandItem()));
               this.playerEntity.setHeldItem(EnumHand.MAIN_HAND, var15);
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
            ItemStack var5 = this.playerEntity.getHeldItemMainhand();
            if (var5 != null && var5.stackSize == 0) {
               this.playerEntity.setHeldItem(EnumHand.MAIN_HAND, (ItemStack)null);
            }

            return;
         case 5:
         case 6:
         case 7:
            double var6 = this.playerEntity.posX - ((double)var3.getX() + 0.5D);
            double var8 = this.playerEntity.posY - ((double)var3.getY() + 0.5D) + 1.5D;
            double var10 = this.playerEntity.posZ - ((double)var3.getZ() + 0.5D);
            double var12 = var6 * var6 + var8 * var8 + var10 * var10;
            if (var12 > 36.0D) {
               return;
            } else if (var3.getY() >= this.serverController.getMaxBuildHeight()) {
               return;
            } else {
               if (var1.getAction() == CPacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                  if (!this.serverController.a(var2, var3, this.playerEntity) && var2.getWorldBorder().contains(var3)) {
                     this.playerEntity.interactionManager.onBlockClicked(var3, var1.getFacing());
                  } else {
                     CraftEventFactory.callPlayerInteractEvent(this.playerEntity, Action.LEFT_CLICK_BLOCK, var3, var1.getFacing(), this.playerEntity.inventory.getCurrentItem(), EnumHand.MAIN_HAND);
                     this.playerEntity.connection.sendPacket(new SPacketBlockChange(var2, var3));
                     TileEntity var14 = var2.getTileEntity(var3);
                     if (var14 != null) {
                        this.playerEntity.connection.sendPacket(var14.getUpdatePacket());
                     }
                  }
               } else {
                  if (var1.getAction() == CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
                     this.playerEntity.interactionManager.blockRemoving(var3);
                  } else if (var1.getAction() == CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK) {
                     this.playerEntity.interactionManager.cancelDestroyingBlock();
                  }

                  if (var2.getBlockState(var3).getMaterial() != Material.AIR) {
                     this.playerEntity.connection.sendPacket(new SPacketBlockChange(var2, var3));
                  }
               }

               return;
            }
         default:
            throw new IllegalArgumentException("Invalid player action");
         }
      }
   }

   public void processTryUseItemOnBlock(CPacketPlayerTryUseItemOnBlock var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      if (!this.playerEntity.isMovementBlocked()) {
         WorldServer var2 = this.serverController.getWorldServer(this.playerEntity.dimension);
         EnumHand var3 = var1.getHand();
         ItemStack var4 = this.playerEntity.getHeldItem(var3);
         BlockPos var5 = var1.getPos();
         EnumFacing var6 = var1.getDirection();
         this.playerEntity.markPlayerActive();
         if (var5.getY() < this.serverController.getMaxBuildHeight() - 1 || var6 != EnumFacing.UP && var5.getY() < this.serverController.getMaxBuildHeight()) {
            if (this.targetPos == null && this.playerEntity.getDistanceSq((double)var5.getX() + 0.5D, (double)var5.getY() + 0.5D, (double)var5.getZ() + 0.5D) < 64.0D && !this.serverController.a(var2, var5, this.playerEntity) && var2.getWorldBorder().contains(var5)) {
               Location var11 = this.getPlayer().getEyeLocation();
               double var8 = NumberConversions.square(var11.getX() - (double)var5.getX()) + NumberConversions.square(var11.getY() - (double)var5.getY()) + NumberConversions.square(var11.getZ() - (double)var5.getZ());
               if (var8 > (double)(this.getPlayer().getGameMode() == GameMode.CREATIVE ? 49 : 36)) {
                  return;
               }

               this.playerEntity.interactionManager.processRightClickBlock(this.playerEntity, var2, var4, var3, var5, var6, var1.getFacingX(), var1.getFacingY(), var1.getFacingZ());
            }
         } else {
            TextComponentTranslation var7 = new TextComponentTranslation("build.tooHigh", new Object[]{this.serverController.getMaxBuildHeight()});
            var7.getStyle().setColor(TextFormatting.RED);
            this.playerEntity.connection.sendPacket(new SPacketChat(var7));
         }

         this.playerEntity.connection.sendPacket(new SPacketBlockChange(var2, var5));
         this.playerEntity.connection.sendPacket(new SPacketBlockChange(var2, var5.offset(var6)));
         var4 = this.playerEntity.getHeldItem(var3);
         if (var4 != null && var4.stackSize == 0) {
            this.playerEntity.setHeldItem(var3, (ItemStack)null);
         }

      }
   }

   public void processTryUseItem(CPacketPlayerTryUseItem var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      if (!this.playerEntity.isMovementBlocked()) {
         WorldServer var2 = this.serverController.getWorldServer(this.playerEntity.dimension);
         EnumHand var3 = var1.getHand();
         ItemStack var4 = this.playerEntity.getHeldItem(var3);
         this.playerEntity.markPlayerActive();
         if (var4 != null) {
            float var5 = this.playerEntity.rotationPitch;
            float var6 = this.playerEntity.rotationYaw;
            double var7 = this.playerEntity.posX;
            double var9 = this.playerEntity.posY + (double)this.playerEntity.getEyeHeight();
            double var11 = this.playerEntity.posZ;
            Vec3d var13 = new Vec3d(var7, var9, var11);
            float var14 = MathHelper.cos(-var6 * 0.017453292F - 3.1415927F);
            float var15 = MathHelper.sin(-var6 * 0.017453292F - 3.1415927F);
            float var16 = -MathHelper.cos(-var5 * 0.017453292F);
            float var17 = MathHelper.sin(-var5 * 0.017453292F);
            float var18 = var15 * var16;
            float var19 = var14 * var16;
            double var20 = this.playerEntity.interactionManager.getGameType() == GameType.CREATIVE ? 5.0D : 4.5D;
            Vec3d var22 = var13.addVector((double)var18 * var20, (double)var17 * var20, (double)var19 * var20);
            RayTraceResult var23 = this.playerEntity.world.rayTraceBlocks(var13, var22, false);
            boolean var24 = false;
            if (var23 != null && var23.typeOfHit == RayTraceResult.Type.BLOCK) {
               if (this.playerEntity.interactionManager.firedInteract) {
                  this.playerEntity.interactionManager.firedInteract = false;
                  var24 = this.playerEntity.interactionManager.interactResult;
               } else {
                  PlayerInteractEvent var29 = CraftEventFactory.callPlayerInteractEvent(this.playerEntity, Action.RIGHT_CLICK_BLOCK, var23.getBlockPos(), var23.sideHit, var4, true, var3);
                  var24 = var29.useItemInHand() == Result.DENY;
               }
            } else {
               PlayerInteractEvent var25 = CraftEventFactory.callPlayerInteractEvent(this.playerEntity, Action.RIGHT_CLICK_AIR, var4, var3);
               var24 = var25.useItemInHand() == Result.DENY;
            }

            if (!var24) {
               this.playerEntity.interactionManager.processRightClick(this.playerEntity, var2, var4, var3);
               var4 = this.playerEntity.getHeldItem(var3);
               if (var4 != null && var4.stackSize == 0) {
                  this.playerEntity.setHeldItem(var3, (ItemStack)null);
                  var4 = null;
               }
            }
         }

      }
   }

   public void handleSpectate(CPacketSpectate var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      if (this.playerEntity.isSpectator()) {
         Entity var2 = null;
         WorldServer[] var3 = this.serverController.worldServer;
         int var10000 = var3.length;

         for(WorldServer var5 : this.serverController.worlds) {
            if (var5 != null) {
               var2 = var1.getEntity(var5);
               if (var2 != null) {
                  break;
               }
            }
         }

         if (var2 != null) {
            this.playerEntity.setSpectatingEntity(this.playerEntity);
            this.playerEntity.dismountRidingEntity();
            this.playerEntity.getBukkitEntity().teleport(var2.getBukkitEntity(), TeleportCause.SPECTATE);
         }
      }

   }

   public void handleResourcePackStatus(CPacketResourcePackStatus var1) {
      this.server.getPluginManager().callEvent(new PlayerResourcePackStatusEvent(this.getPlayer(), Status.values()[var1.action.ordinal()]));
   }

   public void processSteerBoat(CPacketSteerBoat var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      Entity var2 = this.playerEntity.getRidingEntity();
      if (var2 instanceof EntityBoat) {
         ((EntityBoat)var2).setPaddleState(var1.getLeft(), var1.getRight());
      }

   }

   public void onDisconnect(ITextComponent var1) {
      if (!this.processedDisconnect) {
         this.processedDisconnect = true;
         LOGGER.info("{} lost connection: {}", new Object[]{this.playerEntity.getName(), var1.getUnformattedText()});
         this.playerEntity.mountEntityAndWakeUp();
         String var2 = this.serverController.getPlayerList().disconnect(this.playerEntity);
         if (var2 != null && var2.length() > 0) {
            this.serverController.getPlayerList().sendMessage(CraftChatMessage.fromString(var2));
         }

         if (this.serverController.R() && this.playerEntity.getName().equals(this.serverController.Q())) {
            LOGGER.info("Stopping singleplayer server as player logged out");
            this.serverController.safeShutdown();
         }

      }
   }

   public void sendPacket(final Packet var1) {
      if (var1 instanceof SPacketChat) {
         SPacketChat var2 = (SPacketChat)var1;
         EntityPlayer.EnumChatVisibility var3 = this.playerEntity.getChatVisibility();
         if (var3 == EntityPlayer.EnumChatVisibility.HIDDEN) {
            return;
         }

         if (var3 == EntityPlayer.EnumChatVisibility.SYSTEM && !var2.isSystem()) {
            return;
         }
      }

      if (var1 != null) {
         if (var1 instanceof SPacketSpawnPosition) {
            SPacketSpawnPosition var6 = (SPacketSpawnPosition)var1;
            this.playerEntity.compassTarget = new Location(this.getPlayer().getWorld(), (double)var6.spawnBlockPos.getX(), (double)var6.spawnBlockPos.getY(), (double)var6.spawnBlockPos.getZ());
         }

         try {
            this.netManager.sendPacket(var1);
         } catch (Throwable var5) {
            CrashReport var7 = CrashReport.makeCrashReport(var5, "Sending packet");
            CrashReportCategory var4 = var7.makeCategory("Packet being sent");
            var4.setDetail("Packet class", new ICrashReportDetail() {
               public String call() throws Exception {
                  return var1.getClass().getCanonicalName();
               }

               public Object call() throws Exception {
                  return this.call();
               }
            });
            throw new ReportedException(var7);
         }
      }
   }

   public void processHeldItemChange(CPacketHeldItemChange var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      if (!this.playerEntity.isMovementBlocked()) {
         if (var1.getSlotId() >= 0 && var1.getSlotId() < InventoryPlayer.getHotbarSize()) {
            PlayerItemHeldEvent var2 = new PlayerItemHeldEvent(this.getPlayer(), this.playerEntity.inventory.currentItem, var1.getSlotId());
            this.server.getPluginManager().callEvent(var2);
            if (var2.isCancelled()) {
               this.sendPacket(new SPacketHeldItemChange(this.playerEntity.inventory.currentItem));
               this.playerEntity.markPlayerActive();
               return;
            }

            this.playerEntity.inventory.currentItem = var1.getSlotId();
            this.playerEntity.markPlayerActive();
         } else {
            LOGGER.warn("{} tried to set an invalid carried item", new Object[]{this.playerEntity.getName()});
            this.disconnect("Nope!");
         }

      }
   }

   public void processChatMessage(CPacketChatMessage var1) {
      boolean var2 = var1.getMessage().startsWith("/");
      if (var1.getMessage().startsWith("/")) {
         PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      }

      if (!this.playerEntity.isDead && this.playerEntity.getChatVisibility() != EntityPlayer.EnumChatVisibility.HIDDEN) {
         this.playerEntity.markPlayerActive();
         String var17 = var1.getMessage();
         var17 = StringUtils.normalizeSpace(var17);

         for(int var4 = 0; var4 < var17.length(); ++var4) {
            if (!ChatAllowedCharacters.isAllowedCharacter(var17.charAt(var4))) {
               if (!var2) {
                  Waitable var5 = new Waitable() {
                     protected Object evaluate() {
                        NetHandlerPlayServer.this.disconnect("Illegal characters in chat");
                        return null;
                     }
                  };
                  this.serverController.processQueue.add(var5);

                  try {
                     var5.get();
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

         if (var2) {
            try {
               this.serverController.server.playerCommandState = true;
               this.handleSlashCommand(var17);
            } finally {
               this.serverController.server.playerCommandState = false;
            }
         } else if (var17.isEmpty()) {
            LOGGER.warn(this.playerEntity.getName() + " tried to send an empty message");
         } else if (this.getPlayer().isConversing()) {
            this.getPlayer().acceptConversationInput(var17);
         } else if (this.playerEntity.getChatVisibility() == EntityPlayer.EnumChatVisibility.SYSTEM) {
            TextComponentTranslation var19 = new TextComponentTranslation("chat.cannotSend", new Object[0]);
            var19.getStyle().setColor(TextFormatting.RED);
            this.sendPacket(new SPacketChat(var19));
         } else {
            this.chat(var17, true);
         }

         if (chatSpamField.addAndGet(this, 20) > 200 && !this.serverController.getPlayerList().canSendCommands(this.playerEntity.getGameProfile())) {
            if (!var2) {
               Waitable var20 = new Waitable() {
                  protected Object evaluate() {
                     NetHandlerPlayServer.this.disconnect("disconnect.spam");
                     return null;
                  }
               };
               this.serverController.processQueue.add(var20);

               try {
                  var20.get();
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
         TextComponentTranslation var3 = new TextComponentTranslation("chat.cannotSend", new Object[0]);
         var3.getStyle().setColor(TextFormatting.RED);
         this.sendPacket(new SPacketChat(var3));
      }

   }

   public void chat(String var1, boolean var2) {
      if (!var1.isEmpty() && this.playerEntity.getChatVisibility() != EntityPlayer.EnumChatVisibility.HIDDEN) {
         if (!var2 && var1.startsWith("/")) {
            this.handleSlashCommand(var1);
         } else if (this.playerEntity.getChatVisibility() != EntityPlayer.EnumChatVisibility.SYSTEM) {
            CraftPlayer var3 = this.getPlayer();
            AsyncPlayerChatEvent var4 = new AsyncPlayerChatEvent(var2, var3, var1, new LazyPlayerSet(this.serverController));
            this.server.getPluginManager().callEvent(var4);
            if (PlayerChatEvent.getHandlerList().getRegisteredListeners().length != 0) {
               final PlayerChatEvent var5 = new PlayerChatEvent(var3, var4.getMessage(), var4.getFormat(), var4.getRecipients());
               var5.setCancelled(var4.isCancelled());
               Waitable var6 = new Waitable() {
                  protected Object evaluate() {
                     Bukkit.getPluginManager().callEvent(var5);
                     if (var5.isCancelled()) {
                        return null;
                     } else {
                        String var1 = String.format(var5.getFormat(), var5.getPlayer().getDisplayName(), var5.getMessage());
                        NetHandlerPlayServer.this.serverController.console.sendMessage(var1);
                        if (((LazyPlayerSet)var5.getRecipients()).isLazy()) {
                           for(Object var3 : NetHandlerPlayServer.this.serverController.getPlayerList().playerEntityList) {
                              ((EntityPlayerMP)var3).sendMessage(CraftChatMessage.fromString(var1));
                           }
                        } else {
                           for(Player var5x : var5.getRecipients()) {
                              var5x.sendMessage(var1);
                           }
                        }

                        return null;
                     }
                  }
               };
               if (var2) {
                  this.serverController.processQueue.add(var6);
               } else {
                  var6.run();
               }

               try {
                  var6.get();
               } catch (InterruptedException var8) {
                  Thread.currentThread().interrupt();
               } catch (ExecutionException var9) {
                  throw new RuntimeException("Exception processing chat event", var9.getCause());
               }
            } else {
               if (var4.isCancelled()) {
                  return;
               }

               var1 = String.format(var4.getFormat(), var4.getPlayer().getDisplayName(), var4.getMessage());
               this.serverController.console.sendMessage(var1);
               if (((LazyPlayerSet)var4.getRecipients()).isLazy()) {
                  for(Object var11 : this.serverController.getPlayerList().playerEntityList) {
                     ((EntityPlayerMP)var11).sendMessage(CraftChatMessage.fromString(var1));
                  }
               } else {
                  for(Player var12 : var4.getRecipients()) {
                     var12.sendMessage(var1);
                  }
               }
            }
         }

      }
   }

   private void handleSlashCommand(String var1) {
      LOGGER.info(this.playerEntity.getName() + " issued server command: " + var1);
      CraftPlayer var2 = this.getPlayer();
      PlayerCommandPreprocessEvent var3 = new PlayerCommandPreprocessEvent(var2, var1, new LazyPlayerSet(this.serverController));
      this.server.getPluginManager().callEvent(var3);
      if (!var3.isCancelled()) {
         try {
            if (!this.server.dispatchCommand(var3.getPlayer(), var3.getMessage().substring(1))) {
               ;
            }
         } catch (CommandException var5) {
            var2.sendMessage(ChatColor.RED + "An internal error occurred while attempting to perform this command");
            java.util.logging.Logger.getLogger(NetHandlerPlayServer.class.getName()).log(Level.SEVERE, (String)null, var5);
         }
      }
   }

   public void handleAnimation(CPacketAnimation var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      if (!this.playerEntity.isMovementBlocked()) {
         this.playerEntity.markPlayerActive();
         float var2 = this.playerEntity.rotationPitch;
         float var3 = this.playerEntity.rotationYaw;
         double var4 = this.playerEntity.posX;
         double var6 = this.playerEntity.posY + (double)this.playerEntity.getEyeHeight();
         double var8 = this.playerEntity.posZ;
         Vec3d var10 = new Vec3d(var4, var6, var8);
         float var11 = MathHelper.cos(-var3 * 0.017453292F - 3.1415927F);
         float var12 = MathHelper.sin(-var3 * 0.017453292F - 3.1415927F);
         float var13 = -MathHelper.cos(-var2 * 0.017453292F);
         float var14 = MathHelper.sin(-var2 * 0.017453292F);
         float var15 = var12 * var13;
         float var16 = var11 * var13;
         double var17 = this.playerEntity.interactionManager.getGameType() == GameType.CREATIVE ? 5.0D : 4.5D;
         Vec3d var19 = var10.addVector((double)var15 * var17, (double)var14 * var17, (double)var16 * var17);
         RayTraceResult var20 = this.playerEntity.world.rayTraceBlocks(var10, var19, false);
         if (var20 == null || var20.typeOfHit != RayTraceResult.Type.BLOCK) {
            CraftEventFactory.callPlayerInteractEvent(this.playerEntity, Action.LEFT_CLICK_AIR, this.playerEntity.inventory.getCurrentItem(), EnumHand.MAIN_HAND);
         }

         PlayerAnimationEvent var21 = new PlayerAnimationEvent(this.getPlayer());
         this.server.getPluginManager().callEvent(var21);
         if (!var21.isCancelled()) {
            this.playerEntity.swingArm(var1.getHand());
         }
      }
   }

   public void processEntityAction(CPacketEntityAction var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      if (!this.playerEntity.isDead) {
         switch($SWITCH_TABLE$net$minecraft$server$PacketPlayInEntityAction$EnumPlayerAction()[var1.getAction().ordinal()]) {
         case 1:
         case 2:
            PlayerToggleSneakEvent var2 = new PlayerToggleSneakEvent(this.getPlayer(), var1.getAction() == CPacketEntityAction.Action.START_SNEAKING);
            this.server.getPluginManager().callEvent(var2);
            if (var2.isCancelled()) {
               return;
            }
         case 3:
         default:
            break;
         case 4:
         case 5:
            PlayerToggleSprintEvent var3 = new PlayerToggleSprintEvent(this.getPlayer(), var1.getAction() == CPacketEntityAction.Action.START_SPRINTING);
            this.server.getPluginManager().callEvent(var3);
            if (var3.isCancelled()) {
               return;
            }
         }

         this.playerEntity.markPlayerActive();
         switch(NetHandlerPlayServer.SyntheticClass_1.b[var1.getAction().ordinal()]) {
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
               IJumpingMount var5 = (IJumpingMount)this.playerEntity.getRidingEntity();
               int var7 = var1.getAuxData();
               if (var5.canJump() && var7 > 0) {
                  var5.handleStartJump(var7);
               }
            }
            break;
         case 7:
            if (this.playerEntity.getRidingEntity() instanceof IJumpingMount) {
               IJumpingMount var4 = (IJumpingMount)this.playerEntity.getRidingEntity();
               var4.handleStopJump();
            }
            break;
         case 8:
            if (this.playerEntity.getRidingEntity() instanceof EntityHorse) {
               ((EntityHorse)this.playerEntity.getRidingEntity()).openGUI(this.playerEntity);
            }
            break;
         case 9:
            if (!this.playerEntity.onGround && this.playerEntity.motionY < 0.0D && !this.playerEntity.isElytraFlying() && !this.playerEntity.isInWater()) {
               ItemStack var6 = this.playerEntity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
               if (var6 != null && var6.getItem() == Items.ELYTRA && ItemElytra.isBroken(var6)) {
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

   public void processUseEntity(CPacketUseEntity var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      if (!this.playerEntity.isMovementBlocked()) {
         WorldServer var2 = this.serverController.getWorldServer(this.playerEntity.dimension);
         Entity var3 = var1.getEntityFromWorld(var2);
         this.playerEntity.markPlayerActive();
         if (var3 != null) {
            boolean var4 = this.playerEntity.canEntityBeSeen(var3);
            double var5 = 36.0D;
            if (!var4) {
               var5 = 9.0D;
            }

            if (this.playerEntity.getDistanceSqToEntity(var3) < var5) {
               ItemStack var7 = this.playerEntity.getHeldItem(var1.getHand() == null ? EnumHand.MAIN_HAND : var1.getHand());
               if (var1.getAction() == CPacketUseEntity.Action.INTERACT || var1.getAction() == CPacketUseEntity.Action.INTERACT_AT) {
                  boolean var8 = var7 != null && var7.getItem() == Items.LEAD && var3 instanceof EntityLiving;
                  Item var9 = this.playerEntity.inventory.getCurrentItem() == null ? null : this.playerEntity.inventory.getCurrentItem().getItem();
                  Object var10;
                  if (var1.getAction() == CPacketUseEntity.Action.INTERACT) {
                     var10 = new PlayerInteractEntityEvent(this.getPlayer(), var3.getBukkitEntity(), var1.getHand() == EnumHand.OFF_HAND ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND);
                  } else {
                     Vec3d var11 = var1.getHitVec();
                     var10 = new PlayerInteractAtEntityEvent(this.getPlayer(), var3.getBukkitEntity(), new Vector(var11.xCoord, var11.yCoord, var11.zCoord), var1.getHand() == EnumHand.OFF_HAND ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND);
                  }

                  this.server.getPluginManager().callEvent((Event)var10);
                  if (var8 && (((PlayerInteractEntityEvent)var10).isCancelled() || this.playerEntity.inventory.getCurrentItem() == null || this.playerEntity.inventory.getCurrentItem().getItem() != Items.LEAD)) {
                     this.sendPacket(new SPacketEntityAttach(var3, ((EntityLiving)var3).getLeashedToEntity()));
                  }

                  if (((PlayerInteractEntityEvent)var10).isCancelled() || this.playerEntity.inventory.getCurrentItem() == null || this.playerEntity.inventory.getCurrentItem().getItem() != var9) {
                     this.sendPacket(new SPacketEntityMetadata(var3.getEntityId(), var3.dataManager, true));
                  }

                  if (((PlayerInteractEntityEvent)var10).isCancelled()) {
                     return;
                  }
               }

               if (var1.getAction() == CPacketUseEntity.Action.INTERACT) {
                  EnumHand var12 = var1.getHand();
                  ItemStack var13 = this.playerEntity.getHeldItem(var12);
                  this.playerEntity.interact(var3, var13, var12);
                  if (var7 != null && var7.stackSize <= -1) {
                     this.playerEntity.sendContainerToPlayer(this.playerEntity.openContainer);
                  }
               } else if (var1.getAction() == CPacketUseEntity.Action.INTERACT_AT) {
                  EnumHand var14 = var1.getHand();
                  ItemStack var15 = this.playerEntity.getHeldItem(var14);
                  var3.applyPlayerInteraction(this.playerEntity, var1.getHitVec(), var15, var14);
                  if (var7 != null && var7.stackSize <= -1) {
                     this.playerEntity.sendContainerToPlayer(this.playerEntity.openContainer);
                  }
               } else if (var1.getAction() == CPacketUseEntity.Action.ATTACK) {
                  if (var3 instanceof EntityItem || var3 instanceof EntityXPOrb || var3 instanceof EntityArrow || var3 == this.playerEntity && !this.playerEntity.isSpectator()) {
                     this.disconnect("Attempting to attack an invalid entity");
                     this.serverController.warning("Player " + this.playerEntity.getName() + " tried to attack an invalid entity");
                     return;
                  }

                  this.playerEntity.attackTargetEntityWithCurrentItem(var3);
                  if (var7 != null && var7.stackSize <= -1) {
                     this.playerEntity.sendContainerToPlayer(this.playerEntity.openContainer);
                  }
               }
            }
         }

      }
   }

   public void processClientStatus(CPacketClientStatus var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      this.playerEntity.markPlayerActive();
      CPacketClientStatus.State var2 = var1.getStatus();
      switch(NetHandlerPlayServer.SyntheticClass_1.c[var2.ordinal()]) {
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

   public void processCloseWindow(CPacketCloseWindow var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      if (!this.playerEntity.isMovementBlocked()) {
         CraftEventFactory.handleInventoryCloseEvent(this.playerEntity);
         this.playerEntity.closeContainer();
      }
   }

   public void processClickWindow(CPacketClickWindow var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      if (!this.playerEntity.isMovementBlocked()) {
         this.playerEntity.markPlayerActive();
         if (this.playerEntity.openContainer.windowId == var1.getWindowId() && this.playerEntity.openContainer.getCanCraft(this.playerEntity)) {
            boolean var2 = this.playerEntity.isSpectator();
            if (var1.getSlotId() < -1 && var1.getSlotId() != -999) {
               return;
            }

            InventoryView var3 = this.playerEntity.openContainer.getBukkitView();
            SlotType var4 = CraftInventoryView.getSlotType(var3, var1.getSlotId());
            ClickType var5 = ClickType.UNKNOWN;
            InventoryAction var6 = InventoryAction.UNKNOWN;
            ItemStack var7 = null;
            switch($SWITCH_TABLE$net$minecraft$server$InventoryClickType()[var1.getClickType().ordinal()]) {
            case 1:
               if (var1.getUsedButton() == 0) {
                  var5 = ClickType.LEFT;
               } else if (var1.getUsedButton() == 1) {
                  var5 = ClickType.RIGHT;
               }

               if (var1.getUsedButton() != 0 && var1.getUsedButton() != 1) {
                  break;
               }

               var6 = InventoryAction.NOTHING;
               if (var1.getSlotId() == -999) {
                  if (this.playerEntity.inventory.getItemStack() != null) {
                     var6 = var1.getUsedButton() == 0 ? InventoryAction.DROP_ALL_CURSOR : InventoryAction.DROP_ONE_CURSOR;
                  }
               } else if (var1.getSlotId() < 0) {
                  var6 = InventoryAction.NOTHING;
               } else {
                  Slot var18 = this.playerEntity.openContainer.getSlot(var1.getSlotId());
                  if (var18 == null) {
                     break;
                  }

                  ItemStack var21 = var18.getStack();
                  ItemStack var25 = this.playerEntity.inventory.getItemStack();
                  if (var21 == null) {
                     if (var25 != null) {
                        var6 = var1.getUsedButton() == 0 ? InventoryAction.PLACE_ALL : InventoryAction.PLACE_ONE;
                     }
                  } else {
                     if (!var18.canTakeStack(this.playerEntity)) {
                        break;
                     }

                     if (var25 == null) {
                        var6 = var1.getUsedButton() == 0 ? InventoryAction.PICKUP_ALL : InventoryAction.PICKUP_HALF;
                     } else if (var18.isItemValid(var25)) {
                        if (var21.isItemEqual(var25) && ItemStack.areItemStackTagsEqual(var21, var25)) {
                           int var27 = var1.getUsedButton() == 0 ? var25.stackSize : 1;
                           var27 = Math.min(var27, var21.getMaxStackSize() - var21.stackSize);
                           var27 = Math.min(var27, var18.inventory.getInventoryStackLimit() - var21.stackSize);
                           if (var27 == 1) {
                              var6 = InventoryAction.PLACE_ONE;
                           } else if (var27 == var25.stackSize) {
                              var6 = InventoryAction.PLACE_ALL;
                           } else if (var27 < 0) {
                              var6 = var27 != -1 ? InventoryAction.PICKUP_SOME : InventoryAction.PICKUP_ONE;
                           } else if (var27 != 0) {
                              var6 = InventoryAction.PLACE_SOME;
                           }
                        } else if (var25.stackSize <= var18.getSlotStackLimit()) {
                           var6 = InventoryAction.SWAP_WITH_CURSOR;
                        }
                     } else if (var25.getItem() == var21.getItem() && (!var25.getHasSubtypes() || var25.getMetadata() == var21.getMetadata()) && ItemStack.areItemStackTagsEqual(var25, var21) && var21.stackSize >= 0 && var21.stackSize + var25.stackSize <= var25.getMaxStackSize()) {
                        var6 = InventoryAction.PICKUP_ALL;
                     }
                  }
               }
               break;
            case 2:
               if (var1.getUsedButton() == 0) {
                  var5 = ClickType.SHIFT_LEFT;
               } else if (var1.getUsedButton() == 1) {
                  var5 = ClickType.SHIFT_RIGHT;
               }

               if (var1.getUsedButton() != 0 && var1.getUsedButton() != 1) {
                  break;
               }

               if (var1.getSlotId() < 0) {
                  var6 = InventoryAction.NOTHING;
               } else {
                  Slot var17 = this.playerEntity.openContainer.getSlot(var1.getSlotId());
                  if (var17 != null && var17.canTakeStack(this.playerEntity) && var17.getHasStack()) {
                     var6 = InventoryAction.MOVE_TO_OTHER_INVENTORY;
                     break;
                  }

                  var6 = InventoryAction.NOTHING;
               }
               break;
            case 3:
               if (var1.getUsedButton() < 0 || var1.getUsedButton() >= 9) {
                  break;
               }

               var5 = ClickType.NUMBER_KEY;
               Slot var16 = this.playerEntity.openContainer.getSlot(var1.getSlotId());
               if (!var16.canTakeStack(this.playerEntity)) {
                  var6 = InventoryAction.NOTHING;
               } else {
                  ItemStack var9 = this.playerEntity.inventory.getStackInSlot(var1.getUsedButton());
                  boolean var10 = var9 == null || var16.inventory == this.playerEntity.inventory && var16.isItemValid(var9);
                  if (var16.getHasStack()) {
                     if (var10) {
                        var6 = InventoryAction.HOTBAR_SWAP;
                     } else {
                        int var11 = this.playerEntity.inventory.getFirstEmptyStack();
                        if (var11 > -1) {
                           var6 = InventoryAction.HOTBAR_MOVE_AND_READD;
                        } else {
                           var6 = InventoryAction.NOTHING;
                        }
                     }
                  } else {
                     if (!var16.getHasStack() && var9 != null && var16.isItemValid(var9)) {
                        var6 = InventoryAction.HOTBAR_SWAP;
                        break;
                     }

                     var6 = InventoryAction.NOTHING;
                  }
               }
               break;
            case 4:
               if (var1.getUsedButton() == 2) {
                  var5 = ClickType.MIDDLE;
                  if (var1.getSlotId() == -999) {
                     var6 = InventoryAction.NOTHING;
                     break;
                  }

                  Slot var15 = this.playerEntity.openContainer.getSlot(var1.getSlotId());
                  if (var15 != null && var15.getHasStack() && this.playerEntity.capabilities.isCreativeMode && this.playerEntity.inventory.getItemStack() == null) {
                     var6 = InventoryAction.CLONE_STACK;
                     break;
                  }

                  var6 = InventoryAction.NOTHING;
                  break;
               }

               var5 = ClickType.UNKNOWN;
               var6 = InventoryAction.UNKNOWN;
               break;
            case 5:
               if (var1.getSlotId() >= 0) {
                  if (var1.getUsedButton() == 0) {
                     var5 = ClickType.DROP;
                     Slot var14 = this.playerEntity.openContainer.getSlot(var1.getSlotId());
                     if (var14 != null && var14.getHasStack() && var14.canTakeStack(this.playerEntity) && var14.getStack() != null && var14.getStack().getItem() != Item.getItemFromBlock(Blocks.AIR)) {
                        var6 = InventoryAction.DROP_ONE_SLOT;
                        break;
                     }

                     var6 = InventoryAction.NOTHING;
                     break;
                  }

                  if (var1.getUsedButton() != 1) {
                     break;
                  }

                  var5 = ClickType.CONTROL_DROP;
                  Slot var13 = this.playerEntity.openContainer.getSlot(var1.getSlotId());
                  if (var13 != null && var13.getHasStack() && var13.canTakeStack(this.playerEntity) && var13.getStack() != null && var13.getStack().getItem() != Item.getItemFromBlock(Blocks.AIR)) {
                     var6 = InventoryAction.DROP_ALL_SLOT;
                     break;
                  }

                  var6 = InventoryAction.NOTHING;
                  break;
               }

               var5 = ClickType.LEFT;
               if (var1.getUsedButton() == 1) {
                  var5 = ClickType.RIGHT;
               }

               var6 = InventoryAction.NOTHING;
               break;
            case 6:
               var7 = this.playerEntity.openContainer.slotClick(var1.getSlotId(), var1.getUsedButton(), var1.getClickType(), this.playerEntity);
               break;
            case 7:
               var5 = ClickType.DOUBLE_CLICK;
               var6 = InventoryAction.NOTHING;
               if (var1.getSlotId() >= 0 && this.playerEntity.inventory.getItemStack() != null) {
                  ItemStack var8 = this.playerEntity.inventory.getItemStack();
                  var6 = InventoryAction.NOTHING;
                  if (var3.getTopInventory().contains(org.bukkit.Material.getMaterial(Item.getIdFromItem(var8.getItem()))) || var3.getBottomInventory().contains(org.bukkit.Material.getMaterial(Item.getIdFromItem(var8.getItem())))) {
                     var6 = InventoryAction.COLLECT_TO_CURSOR;
                  }
               }
            }

            if (var1.getClickType() != net.minecraft.inventory.ClickType.QUICK_CRAFT) {
               Object var12;
               if (var5 == ClickType.NUMBER_KEY) {
                  var12 = new InventoryClickEvent(var3, var4, var1.getSlotId(), var5, var6, var1.getUsedButton());
               } else {
                  var12 = new InventoryClickEvent(var3, var4, var1.getSlotId(), var5, var6);
               }

               Inventory var19 = var3.getTopInventory();
               if (var1.getSlotId() == 0 && var19 instanceof CraftingInventory) {
                  Recipe var22 = ((CraftingInventory)var19).getRecipe();
                  if (var22 != null) {
                     if (var5 == ClickType.NUMBER_KEY) {
                        var12 = new CraftItemEvent(var22, var3, var4, var1.getSlotId(), var5, var6, var1.getUsedButton());
                     } else {
                        var12 = new CraftItemEvent(var22, var3, var4, var1.getSlotId(), var5, var6);
                     }
                  }
               }

               ((InventoryClickEvent)var12).setCancelled(var2);
               Container var23 = this.playerEntity.openContainer;
               this.server.getPluginManager().callEvent((Event)var12);
               if (this.playerEntity.openContainer != var23) {
                  return;
               }

               switch($SWITCH_TABLE$org$bukkit$event$Event$Result()[((InventoryClickEvent)var12).getResult().ordinal()]) {
               case 1:
                  switch($SWITCH_TABLE$org$bukkit$event$inventory$InventoryAction()[var6.ordinal()]) {
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
                     this.playerEntity.connection.sendPacket(new SPacketSetSlot(this.playerEntity.openContainer.windowId, var1.getSlotId(), this.playerEntity.openContainer.getSlot(var1.getSlotId()).getStack()));
                     break;
                  case 10:
                  case 11:
                  case 17:
                     this.playerEntity.connection.sendPacket(new SPacketSetSlot(-1, -1, this.playerEntity.inventory.getItemStack()));
                     break;
                  case 12:
                  case 13:
                     this.playerEntity.connection.sendPacket(new SPacketSetSlot(this.playerEntity.openContainer.windowId, var1.getSlotId(), this.playerEntity.openContainer.getSlot(var1.getSlotId()).getStack()));
                  }

                  return;
               case 2:
               case 3:
                  var7 = this.playerEntity.openContainer.slotClick(var1.getSlotId(), var1.getUsedButton(), var1.getClickType(), this.playerEntity);
               default:
                  if (var12 instanceof CraftItemEvent) {
                     this.playerEntity.sendContainerToPlayer(this.playerEntity.openContainer);
                  }
               }
            }

            if (ItemStack.areItemStacksEqual(var1.getClickedItem(), var7)) {
               this.playerEntity.connection.sendPacket(new SPacketConfirmTransaction(var1.getWindowId(), var1.getActionNumber(), true));
               this.playerEntity.isChangingQuantityOnly = true;
               this.playerEntity.openContainer.detectAndSendChanges();
               this.playerEntity.updateHeldItem();
               this.playerEntity.isChangingQuantityOnly = false;
            } else {
               this.pendingTransactions.addKey(this.playerEntity.openContainer.windowId, Short.valueOf(var1.getActionNumber()));
               this.playerEntity.connection.sendPacket(new SPacketConfirmTransaction(var1.getWindowId(), var1.getActionNumber(), false));
               this.playerEntity.openContainer.setCanCraft(this.playerEntity, false);
               ArrayList var20 = Lists.newArrayList();

               for(int var24 = 0; var24 < this.playerEntity.openContainer.inventorySlots.size(); ++var24) {
                  ItemStack var26 = ((Slot)this.playerEntity.openContainer.inventorySlots.get(var24)).getStack();
                  ItemStack var30 = var26 != null && var26.stackSize > 0 ? var26 : null;
                  var20.add(var30);
               }

               this.playerEntity.updateCraftingInventory(this.playerEntity.openContainer, var20);
            }
         }

      }
   }

   public void processEnchantItem(CPacketEnchantItem var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      if (!this.playerEntity.isMovementBlocked()) {
         this.playerEntity.markPlayerActive();
         if (this.playerEntity.openContainer.windowId == var1.getWindowId() && this.playerEntity.openContainer.getCanCraft(this.playerEntity) && !this.playerEntity.isSpectator()) {
            this.playerEntity.openContainer.enchantItem(this.playerEntity, var1.getButton());
            this.playerEntity.openContainer.detectAndSendChanges();
         }

      }
   }

   public void processCreativeInventoryAction(CPacketCreativeInventoryAction var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      if (this.playerEntity.interactionManager.isCreative()) {
         boolean var2 = var1.getSlotId() < 0;
         ItemStack var3 = var1.getStack();
         if (var3 != null && var3.hasTagCompound() && var3.getTagCompound().hasKey("BlockEntityTag", 10)) {
            NBTTagCompound var4 = var3.getTagCompound().getCompoundTag("BlockEntityTag");
            if (var4.hasKey("x") && var4.hasKey("y") && var4.hasKey("z")) {
               BlockPos var5 = new BlockPos(var4.getInteger("x"), var4.getInteger("y"), var4.getInteger("z"));
               TileEntity var6 = this.playerEntity.world.getTileEntity(var5);
               if (var6 != null) {
                  NBTTagCompound var7 = var6.writeToNBT(new NBTTagCompound());
                  var7.removeTag("x");
                  var7.removeTag("y");
                  var7.removeTag("z");
                  var3.setTagInfo("BlockEntityTag", var7);
               }
            }
         }

         boolean var12 = var1.getSlotId() >= 1 && var1.getSlotId() <= 45;
         boolean var13 = var3 == null || var3.getItem() != null && !invalidItems.contains(Integer.valueOf(Item.getIdFromItem(var3.getItem())));
         boolean var14 = var3 == null || var3.getMetadata() >= 0 && var3.stackSize <= 64 && var3.stackSize > 0;
         if (var2 || var12 && !ItemStack.areItemStacksEqual(this.playerEntity.inventoryContainer.getSlot(var1.getSlotId()).getStack(), var1.getStack())) {
            CraftPlayer var15 = this.playerEntity.getBukkitEntity();
            CraftInventoryView var8 = new CraftInventoryView(var15, var15.getInventory(), this.playerEntity.inventoryContainer);
            org.bukkit.inventory.ItemStack var9 = CraftItemStack.asBukkitCopy(var1.getStack());
            SlotType var10 = SlotType.QUICKBAR;
            if (var2) {
               var10 = SlotType.OUTSIDE;
            } else if (var1.getSlotId() < 36) {
               if (var1.getSlotId() >= 5 && var1.getSlotId() < 9) {
                  var10 = SlotType.ARMOR;
               } else {
                  var10 = SlotType.CONTAINER;
               }
            }

            InventoryCreativeEvent var11 = new InventoryCreativeEvent(var8, var10, var2 ? -999 : var1.getSlotId(), var9);
            this.server.getPluginManager().callEvent(var11);
            var3 = CraftItemStack.asNMSCopy(var11.getCursor());
            switch($SWITCH_TABLE$org$bukkit$event$Event$Result()[var11.getResult().ordinal()]) {
            case 1:
               if (var1.getSlotId() >= 0) {
                  this.playerEntity.connection.sendPacket(new SPacketSetSlot(this.playerEntity.inventoryContainer.windowId, var1.getSlotId(), this.playerEntity.inventoryContainer.getSlot(var1.getSlotId()).getStack()));
                  this.playerEntity.connection.sendPacket(new SPacketSetSlot(-1, -1, (ItemStack)null));
               }

               return;
            case 2:
            default:
               break;
            case 3:
               var14 = true;
               var13 = true;
            }
         }

         if (var12 && var13 && var14) {
            if (var3 == null) {
               this.playerEntity.inventoryContainer.putStackInSlot(var1.getSlotId(), (ItemStack)null);
            } else {
               this.playerEntity.inventoryContainer.putStackInSlot(var1.getSlotId(), var3);
            }

            this.playerEntity.inventoryContainer.setCanCraft(this.playerEntity, true);
         } else if (var2 && var13 && var14 && this.itemDropThreshold < 200) {
            this.itemDropThreshold += 20;
            EntityItem var16 = this.playerEntity.dropItem(var3, true);
            if (var16 != null) {
               var16.setAgeToCreativeDespawnTime();
            }
         }
      }

   }

   public void processConfirmTransaction(CPacketConfirmTransaction var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      if (!this.playerEntity.isMovementBlocked()) {
         Short var2 = (Short)this.pendingTransactions.lookup(this.playerEntity.openContainer.windowId);
         if (var2 != null && var1.getUid() == var2.shortValue() && this.playerEntity.openContainer.windowId == var1.getWindowId() && !this.playerEntity.openContainer.getCanCraft(this.playerEntity) && !this.playerEntity.isSpectator()) {
            this.playerEntity.openContainer.setCanCraft(this.playerEntity, true);
         }

      }
   }

   public void processUpdateSign(CPacketUpdateSign var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      if (!this.playerEntity.isMovementBlocked()) {
         this.playerEntity.markPlayerActive();
         WorldServer var2 = this.serverController.getWorldServer(this.playerEntity.dimension);
         BlockPos var3 = var1.getPosition();
         if (var2.isBlockLoaded(var3)) {
            IBlockState var4 = var2.getBlockState(var3);
            TileEntity var5 = var2.getTileEntity(var3);
            if (!(var5 instanceof TileEntitySign)) {
               return;
            }

            TileEntitySign var6 = (TileEntitySign)var5;
            if (!var6.getIsEditable() || var6.getPlayer() != this.playerEntity) {
               this.serverController.warning("Player " + this.playerEntity.getName() + " just tried to change non-editable sign");
               this.sendPacket(var5.getUpdatePacket());
               return;
            }

            String[] var7 = var1.getLines();
            Player var8 = this.server.getPlayer(this.playerEntity);
            int var9 = var1.getPosition().getX();
            int var10 = var1.getPosition().getY();
            int var11 = var1.getPosition().getZ();
            String[] var12 = new String[4];

            for(int var13 = 0; var13 < var7.length; ++var13) {
               var12[var13] = TextFormatting.getTextWithoutFormattingCodes((new TextComponentString(TextFormatting.getTextWithoutFormattingCodes(var7[var13]))).getUnformattedText());
            }

            SignChangeEvent var14 = new SignChangeEvent((CraftBlock)var8.getWorld().getBlockAt(var9, var10, var11), this.server.getPlayer(this.playerEntity), var12);
            this.server.getPluginManager().callEvent(var14);
            if (!var14.isCancelled()) {
               System.arraycopy(CraftSign.sanitizeLines(var14.getLines()), 0, var6.signText, 0, 4);
               var6.isEditable = false;
            }

            var6.markDirty();
            var2.notifyBlockUpdate(var3, var4, var4, 3);
         }

      }
   }

   public void processKeepAlive(CPacketKeepAlive var1) {
      if (var1.getKey() == this.keepAliveId) {
         int var2 = (int)(this.currentTimeMillis() - this.lastPingTime);
         this.playerEntity.ping = (this.playerEntity.ping * 3 + var2) / 4;
      }

   }

   private long currentTimeMillis() {
      return System.nanoTime() / 1000000L;
   }

   public void processPlayerAbilities(CPacketPlayerAbilities var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      if (this.playerEntity.capabilities.allowFlying && this.playerEntity.capabilities.isFlying != var1.isFlying()) {
         PlayerToggleFlightEvent var2 = new PlayerToggleFlightEvent(this.server.getPlayer(this.playerEntity), var1.isFlying());
         this.server.getPluginManager().callEvent(var2);
         if (!var2.isCancelled()) {
            this.playerEntity.capabilities.isFlying = var1.isFlying();
         } else {
            this.playerEntity.sendPlayerAbilities();
         }
      }

   }

   public void processTabComplete(CPacketTabComplete var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      if (chatSpamField.addAndGet(this, 10) > 500 && !this.serverController.getPlayerList().canSendCommands(this.playerEntity.getGameProfile())) {
         this.disconnect("disconnect.spam");
      } else {
         ArrayList var2 = Lists.newArrayList();

         for(String var4 : this.serverController.tabCompleteCommand(this.playerEntity, var1.getMessage(), var1.getTargetBlock(), var1.hasTargetBlock())) {
            var2.add(var4);
         }

         this.playerEntity.connection.sendPacket(new SPacketTabComplete((String[])var2.toArray(new String[var2.size()])));
      }
   }

   public void processClientSettings(CPacketClientSettings var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      this.playerEntity.handleClientSettings(var1);
   }

   public void processCustomPayload(CPacketCustomPayload var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      String var2 = var1.getChannelName();
      if ("MC|BEdit".equals(var2)) {
         PacketBuffer var3 = var1.getBufferData();

         try {
            ItemStack var4 = var3.readItemStack();
            if (var4 == null) {
               return;
            }

            if (!ItemWritableBook.isNBTValid(var4.getTagCompound())) {
               throw new IOException("Invalid book tag!");
            }

            ItemStack var5 = this.playerEntity.getHeldItemMainhand();
            if (var5 == null) {
               return;
            }

            if (var4.getItem() == Items.WRITABLE_BOOK && var4.getItem() == var5.getItem()) {
               var5 = new ItemStack(Items.WRITABLE_BOOK);
               var5.setTagInfo("pages", var4.getTagCompound().getTagList("pages", 8));
               CraftEventFactory.handleEditBookEvent(this.playerEntity, var5);
            }
         } catch (Exception var29) {
            LOGGER.error("Couldn't handle book info", var29);
            this.disconnect("Invalid book data!");
         }
      } else if ("MC|BSign".equals(var2)) {
         PacketBuffer var31 = var1.getBufferData();

         try {
            ItemStack var37 = var31.readItemStack();
            if (var37 == null) {
               return;
            }

            if (!ItemWrittenBook.validBookTagContents(var37.getTagCompound())) {
               throw new IOException("Invalid book tag!");
            }

            ItemStack var39 = this.playerEntity.getHeldItemMainhand();
            if (var39 == null) {
               return;
            }

            if (var37.getItem() == Items.WRITABLE_BOOK && var39.getItem() == Items.WRITABLE_BOOK) {
               var39 = new ItemStack(Items.WRITABLE_BOOK);
               var39.setTagInfo("author", new NBTTagString(this.playerEntity.getName()));
               var39.setTagInfo("title", new NBTTagString(var37.getTagCompound().getString("title")));
               NBTTagList var7 = var37.getTagCompound().getTagList("pages", 8);

               for(int var8 = 0; var8 < var7.tagCount(); ++var8) {
                  String var6 = var7.getStringTagAt(var8);
                  TextComponentString var9 = new TextComponentString(var6);
                  var6 = ITextComponent.Serializer.componentToJson(var9);
                  var7.set(var8, new NBTTagString(var6));
               }

               var39.setTagInfo("pages", var7);
               var39.setItem(Items.WRITTEN_BOOK);
               CraftEventFactory.handleEditBookEvent(this.playerEntity, var39);
            }
         } catch (Exception var30) {
            LOGGER.error("Couldn't sign book", var30);
            this.disconnect("Invalid book data!");
         }
      } else if ("MC|TrSel".equals(var2)) {
         try {
            int var43 = var1.getBufferData().readInt();
            Container var46 = this.playerEntity.openContainer;
            if (var46 instanceof ContainerMerchant) {
               ((ContainerMerchant)var46).setCurrentRecipeIndex(var43);
            }
         } catch (Exception var28) {
            LOGGER.error("Couldn't select trade", var28);
            this.disconnect("Invalid trade data!");
         }
      } else if ("MC|AdvCmd".equals(var2)) {
         if (!this.serverController.getEnableCommandBlock()) {
            this.playerEntity.sendMessage((ITextComponent)(new TextComponentTranslation("advMode.notEnabled", new Object[0])));
            return;
         }

         if (!this.playerEntity.canUseCommandBlock()) {
            this.playerEntity.sendMessage((ITextComponent)(new TextComponentTranslation("advMode.notAllowed", new Object[0])));
            return;
         }

         PacketBuffer var32 = var1.getBufferData();

         try {
            byte var47 = var32.readByte();
            CommandBlockBaseLogic var51 = null;
            if (var47 == 0) {
               TileEntity var44 = this.playerEntity.world.getTileEntity(new BlockPos(var32.readInt(), var32.readInt(), var32.readInt()));
               if (var44 instanceof TileEntityCommandBlock) {
                  var51 = ((TileEntityCommandBlock)var44).getCommandBlockLogic();
               }
            } else if (var47 == 1) {
               Entity var10 = this.playerEntity.world.getEntityByID(var32.readInt());
               if (var10 instanceof EntityMinecartCommandBlock) {
                  var51 = ((EntityMinecartCommandBlock)var10).getCommandBlockLogic();
               }
            }

            String var59 = var32.readString(var32.readableBytes());
            boolean var11 = var32.readBoolean();
            if (var51 != null) {
               var51.setCommand(var59);
               var51.setTrackOutput(var11);
               if (!var11) {
                  var51.setLastOutput((ITextComponent)null);
               }

               var51.updateCommand();
               this.playerEntity.sendMessage((ITextComponent)(new TextComponentTranslation("advMode.setCommand.success", new Object[]{var59})));
            }
         } catch (Exception var27) {
            LOGGER.error("Couldn't set command block", var27);
            this.disconnect("Invalid command data!");
         }
      } else if ("MC|AutoCmd".equals(var2)) {
         if (!this.serverController.getEnableCommandBlock()) {
            this.playerEntity.sendMessage((ITextComponent)(new TextComponentTranslation("advMode.notEnabled", new Object[0])));
            return;
         }

         if (!this.playerEntity.canUseCommandBlock()) {
            this.playerEntity.sendMessage((ITextComponent)(new TextComponentTranslation("advMode.notAllowed", new Object[0])));
            return;
         }

         PacketBuffer var33 = var1.getBufferData();

         try {
            CommandBlockBaseLogic var48 = null;
            TileEntityCommandBlock var52 = null;
            BlockPos var60 = new BlockPos(var33.readInt(), var33.readInt(), var33.readInt());
            TileEntity var66 = this.playerEntity.world.getTileEntity(var60);
            if (var66 instanceof TileEntityCommandBlock) {
               var52 = (TileEntityCommandBlock)var66;
               var48 = var52.getCommandBlockLogic();
            }

            String var42 = var33.readString(var33.readableBytes());
            boolean var12 = var33.readBoolean();
            TileEntityCommandBlock.Mode var13 = TileEntityCommandBlock.Mode.valueOf(var33.readString(16));
            boolean var14 = var33.readBoolean();
            boolean var15 = var33.readBoolean();
            if (var48 != null) {
               EnumFacing var16 = (EnumFacing)this.playerEntity.world.getBlockState(var60).getValue(BlockCommandBlock.FACING);
               switch(NetHandlerPlayServer.SyntheticClass_1.d[var13.ordinal()]) {
               case 1:
                  IBlockState var82 = Blocks.CHAIN_COMMAND_BLOCK.getDefaultState();
                  this.playerEntity.world.setBlockState(var60, var82.withProperty(BlockCommandBlock.FACING, var16).withProperty(BlockCommandBlock.CONDITIONAL, Boolean.valueOf(var14)), 2);
                  break;
               case 2:
                  IBlockState var81 = Blocks.REPEATING_COMMAND_BLOCK.getDefaultState();
                  this.playerEntity.world.setBlockState(var60, var81.withProperty(BlockCommandBlock.FACING, var16).withProperty(BlockCommandBlock.CONDITIONAL, Boolean.valueOf(var14)), 2);
                  break;
               case 3:
                  IBlockState var17 = Blocks.COMMAND_BLOCK.getDefaultState();
                  this.playerEntity.world.setBlockState(var60, var17.withProperty(BlockCommandBlock.FACING, var16).withProperty(BlockCommandBlock.CONDITIONAL, Boolean.valueOf(var14)), 2);
               }

               var66.validate();
               this.playerEntity.world.setTileEntity(var60, var66);
               var48.setCommand(var42);
               var48.setTrackOutput(var12);
               if (!var12) {
                  var48.setLastOutput((ITextComponent)null);
               }

               var52.setAuto(var15);
               var48.updateCommand();
               if (!net.minecraft.util.StringUtils.isNullOrEmpty(var42)) {
                  this.playerEntity.sendMessage((ITextComponent)(new TextComponentTranslation("advMode.setCommand.success", new Object[]{var42})));
               }
            }
         } catch (Exception var26) {
            LOGGER.error("Couldn't set command block", var26);
            this.disconnect("Invalid command data!");
         }
      } else if ("MC|Beacon".equals(var2)) {
         if (this.playerEntity.openContainer instanceof ContainerBeacon) {
            try {
               PacketBuffer var34 = var1.getBufferData();
               int var49 = var34.readInt();
               int var53 = var34.readInt();
               ContainerBeacon var61 = (ContainerBeacon)this.playerEntity.openContainer;
               Slot var67 = var61.getSlot(0);
               if (var67.getHasStack()) {
                  var67.decrStackSize(1);
                  IInventory var71 = var61.getTileEntity();
                  var71.setField(1, var49);
                  var71.setField(2, var53);
                  var71.markDirty();
               }
            } catch (Exception var25) {
               LOGGER.error("Couldn't set beacon", var25);
               this.disconnect("Invalid beacon data!");
            }
         }
      } else if ("MC|ItemName".equals(var2)) {
         if (this.playerEntity.openContainer instanceof ContainerRepair) {
            ContainerRepair var54 = (ContainerRepair)this.playerEntity.openContainer;
            if (var1.getBufferData() != null && var1.getBufferData().readableBytes() >= 1) {
               String var62 = ChatAllowedCharacters.filterAllowedCharacters(var1.getBufferData().readString(32767));
               if (var62.length() <= 30) {
                  var54.updateItemName(var62);
               }
            } else {
               var54.updateItemName("");
            }
         }
      } else if ("MC|Struct".equals(var2)) {
         if (!this.playerEntity.canUseCommandBlock()) {
            return;
         }

         PacketBuffer var35 = var1.getBufferData();

         try {
            BlockPos var55 = new BlockPos(var35.readInt(), var35.readInt(), var35.readInt());
            IBlockState var63 = this.playerEntity.world.getBlockState(var55);
            TileEntity var45 = this.playerEntity.world.getTileEntity(var55);
            if (var45 instanceof TileEntityStructure) {
               TileEntityStructure var68 = (TileEntityStructure)var45;
               byte var72 = var35.readByte();
               String var75 = var35.readString(32);
               var68.setMode(TileEntityStructure.Mode.valueOf(var75));
               var68.setName(var35.readString(64));
               int var78 = MathHelper.clamp(var35.readInt(), -32, 32);
               int var79 = MathHelper.clamp(var35.readInt(), -32, 32);
               int var80 = MathHelper.clamp(var35.readInt(), -32, 32);
               var68.setPosition(new BlockPos(var78, var79, var80));
               int var83 = MathHelper.clamp(var35.readInt(), 0, 32);
               int var18 = MathHelper.clamp(var35.readInt(), 0, 32);
               int var19 = MathHelper.clamp(var35.readInt(), 0, 32);
               var68.setSize(new BlockPos(var83, var18, var19));
               String var20 = var35.readString(32);
               var68.setMirror(Mirror.valueOf(var20));
               String var21 = var35.readString(32);
               var68.setRotation(Rotation.valueOf(var21));
               var68.setMetadata(var35.readString(128));
               var68.setIgnoresEntities(var35.readBoolean());
               var68.setShowAir(var35.readBoolean());
               var68.setShowBoundingBox(var35.readBoolean());
               var68.setIntegrity(MathHelper.clamp(var35.readFloat(), 0.0F, 1.0F));
               var68.setSeed(var35.readVarLong());
               String var22 = var68.getName();
               if (var72 == 2) {
                  if (var68.save()) {
                     this.playerEntity.sendStatusMessage(new TextComponentTranslation("structure_block.save_success", new Object[]{var22}));
                  } else {
                     this.playerEntity.sendStatusMessage(new TextComponentTranslation("structure_block.save_failure", new Object[]{var22}));
                  }
               } else if (var72 == 3) {
                  if (!var68.isStructureLoadable()) {
                     this.playerEntity.sendStatusMessage(new TextComponentTranslation("structure_block.load_not_found", new Object[]{var22}));
                  } else if (var68.load()) {
                     this.playerEntity.sendStatusMessage(new TextComponentTranslation("structure_block.load_success", new Object[]{var22}));
                  } else {
                     this.playerEntity.sendStatusMessage(new TextComponentTranslation("structure_block.load_prepare", new Object[]{var22}));
                  }
               } else if (var72 == 4) {
                  if (var68.detectSize()) {
                     this.playerEntity.sendStatusMessage(new TextComponentTranslation("structure_block.size_success", new Object[]{var22}));
                  } else {
                     this.playerEntity.sendStatusMessage(new TextComponentTranslation("structure_block.size_failure", new Object[0]));
                  }
               }

               var68.markDirty();
               this.playerEntity.world.notifyBlockUpdate(var55, var63, var63, 3);
            }
         } catch (Exception var24) {
            LOGGER.error("Couldn't set structure block", var24);
            this.disconnect("Invalid structure data!");
         }
      } else if ("MC|PickItem".equals(var2)) {
         PacketBuffer var36 = var1.getBufferData();

         try {
            int var50 = var36.readVarInt();
            this.playerEntity.inventory.pickItem(var50);
            this.playerEntity.connection.sendPacket(new SPacketSetSlot(-2, this.playerEntity.inventory.currentItem, this.playerEntity.inventory.getStackInSlot(this.playerEntity.inventory.currentItem)));
            this.playerEntity.connection.sendPacket(new SPacketSetSlot(-2, var50, this.playerEntity.inventory.getStackInSlot(var50)));
            this.playerEntity.connection.sendPacket(new SPacketHeldItemChange(this.playerEntity.inventory.currentItem));
         } catch (Exception var23) {
            LOGGER.error("Couldn't pick item", var23);
            this.disconnect("Invalid item data!");
         }
      } else if (var1.getChannelName().equals("REGISTER")) {
         String var56 = var1.getBufferData().toString(Charsets.UTF_8);

         String[] var76;
         for(String var64 : var76 = var56.split("\u0000")) {
            this.getPlayer().addChannel(var64);
         }
      } else if (var1.getChannelName().equals("UNREGISTER")) {
         String var57 = var1.getBufferData().toString(Charsets.UTF_8);

         String[] var77;
         for(String var65 : var77 = var57.split("\u0000")) {
            this.getPlayer().removeChannel(var65);
         }
      } else {
         byte[] var58 = new byte[var1.getBufferData().readableBytes()];
         var1.getBufferData().readBytes(var58);
         this.server.getMessenger().dispatchIncomingMessage(this.playerEntity.getBukkitEntity(), var1.getChannelName(), var58);
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
