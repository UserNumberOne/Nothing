package net.minecraft.network;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.util.concurrent.Futures;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.Entity;
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
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketKeepAlive;
import net.minecraft.network.play.server.SPacketMoveVehicle;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.network.play.server.SPacketTabComplete;
import net.minecraft.server.MinecraftServer;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetHandlerPlayServer implements INetHandlerPlayServer, ITickable {
   private static final Logger LOGGER = LogManager.getLogger();
   public final NetworkManager netManager;
   private final MinecraftServer serverController;
   public EntityPlayerMP playerEntity;
   private int networkTickCount;
   private int keepAliveId;
   private long lastPingTime;
   private long lastSentPingPacket;
   private int chatSpamThresholdCount;
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

   public NetHandlerPlayServer(MinecraftServer var1, NetworkManager var2, EntityPlayerMP var3) {
      this.serverController = var1;
      this.netManager = var2;
      var2.setNetHandler(this);
      this.playerEntity = var3;
      var3.connection = this;
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

      this.serverController.theProfiler.startSection("keepAlive");
      if ((long)this.networkTickCount - this.lastSentPingPacket > 40L) {
         this.lastSentPingPacket = (long)this.networkTickCount;
         this.lastPingTime = this.currentTimeMillis();
         this.keepAliveId = (int)this.lastPingTime;
         this.sendPacket(new SPacketKeepAlive(this.keepAliveId));
      }

      this.serverController.theProfiler.endSection();
      if (this.chatSpamThresholdCount > 0) {
         --this.chatSpamThresholdCount;
      }

      if (this.itemDropThreshold > 0) {
         --this.itemDropThreshold;
      }

      if (this.playerEntity.getLastActiveTime() > 0L && this.serverController.getMaxPlayerIdleMinutes() > 0 && MinecraftServer.getCurrentTimeMillis() - this.playerEntity.getLastActiveTime() > (long)(this.serverController.getMaxPlayerIdleMinutes() * 1000 * 60)) {
         this.disconnect("You have been idle for too long!");
      }

   }

   private void captureCurrentPosition() {
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
      final TextComponentString var2 = new TextComponentString(var1);
      this.netManager.sendPacket(new SPacketDisconnect(var2), new GenericFutureListener() {
         public void operationComplete(Future var1) throws Exception {
            NetHandlerPlayServer.this.netManager.closeChannel(var2);
         }
      });
      this.netManager.disableAutoRead();
      Futures.getUnchecked(this.serverController.addScheduledTask(new Runnable() {
         public void run() {
            NetHandlerPlayServer.this.netManager.checkDisconnected();
         }
      }));
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
            if (var26 - var24 > 100.0D && (!this.serverController.isSinglePlayer() || !this.serverController.getServerOwner().equals(var2.getName()))) {
               LOGGER.warn("{} (vehicle of {}) moved too quickly! {},{},{}", new Object[]{var2.getName(), this.playerEntity.getName(), var18, var20, var22});
               this.netManager.sendPacket(new SPacketMoveVehicle(var2));
               return;
            }

            boolean var28 = var3.getCollisionBoxes(var2, var2.getEntityBoundingBox().contract(0.0625D)).isEmpty();
            var18 = var10 - this.lowestRiddenX1;
            var20 = var12 - this.lowestRiddenY1 - 1.0E-6D;
            var22 = var14 - this.lowestRiddenZ1;
            var2.move(var18, var20, var22);
            double var29 = var20;
            var18 = var10 - var2.posX;
            var20 = var12 - var2.posY;
            if (var20 > -0.5D || var20 < 0.5D) {
               var20 = 0.0D;
            }

            var22 = var14 - var2.posZ;
            var26 = var18 * var18 + var20 * var20 + var22 * var22;
            boolean var31 = false;
            if (var26 > 0.0625D) {
               var31 = true;
               LOGGER.warn("{} moved wrongly!", new Object[]{var2.getName()});
            }

            var2.setPositionAndRotation(var10, var12, var14, var16, var17);
            boolean var32 = var3.getCollisionBoxes(var2, var2.getEntityBoundingBox().contract(0.0625D)).isEmpty();
            if (var28 && (var31 || !var32)) {
               var2.setPositionAndRotation(var4, var6, var8, var16, var17);
               this.netManager.sendPacket(new SPacketMoveVehicle(var2));
               return;
            }

            this.serverController.getPlayerList().serverUpdateMovingPlayer(this.playerEntity);
            this.playerEntity.addMovementStat(this.playerEntity.posX - var4, this.playerEntity.posY - var6, this.playerEntity.posZ - var8);
            this.vehicleFloating = var29 >= -0.03125D && !this.serverController.isFlightAllowed() && !var3.checkBlockCollision(var2.getEntityBoundingBox().expandXyz(0.0625D).addCoord(0.0D, -0.55D, 0.0D));
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
         WorldServer var2 = this.serverController.worldServerForDimension(this.playerEntity.dimension);
         if (!this.playerEntity.playerConqueredTheEnd) {
            if (this.networkTickCount == 0) {
               this.captureCurrentPosition();
            }

            if (this.targetPos != null) {
               if (this.networkTickCount - this.lastPositionUpdate > 20) {
                  this.lastPositionUpdate = this.networkTickCount;
                  this.setPlayerLocation(this.targetPos.xCoord, this.targetPos.yCoord, this.targetPos.zCoord, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
               }
            } else {
               this.lastPositionUpdate = this.networkTickCount;
               if (this.playerEntity.isRiding()) {
                  this.playerEntity.setPositionAndRotation(this.playerEntity.posX, this.playerEntity.posY, this.playerEntity.posZ, var1.getYaw(this.playerEntity.rotationYaw), var1.getPitch(this.playerEntity.rotationPitch));
                  this.serverController.getPlayerList().serverUpdateMovingPlayer(this.playerEntity);
               } else {
                  double var3 = this.playerEntity.posX;
                  double var5 = this.playerEntity.posY;
                  double var7 = this.playerEntity.posZ;
                  double var9 = this.playerEntity.posY;
                  double var11 = var1.getX(this.playerEntity.posX);
                  double var13 = var1.getY(this.playerEntity.posY);
                  double var15 = var1.getZ(this.playerEntity.posZ);
                  float var17 = var1.getYaw(this.playerEntity.rotationYaw);
                  float var18 = var1.getPitch(this.playerEntity.rotationPitch);
                  double var19 = var11 - this.firstGoodX;
                  double var21 = var13 - this.firstGoodY;
                  double var23 = var15 - this.firstGoodZ;
                  double var25 = this.playerEntity.motionX * this.playerEntity.motionX + this.playerEntity.motionY * this.playerEntity.motionY + this.playerEntity.motionZ * this.playerEntity.motionZ;
                  double var27 = var19 * var19 + var21 * var21 + var23 * var23;
                  ++this.movePacketCounter;
                  int var29 = this.movePacketCounter - this.lastMovePacketCounter;
                  if (var29 > 5) {
                     LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", new Object[]{this.playerEntity.getName(), var29});
                     var29 = 1;
                  }

                  if (!this.playerEntity.isInvulnerableDimensionChange() && (!this.playerEntity.getServerWorld().getGameRules().getBoolean("disableElytraMovementCheck") || !this.playerEntity.isElytraFlying())) {
                     float var30 = this.playerEntity.isElytraFlying() ? 300.0F : 100.0F;
                     if (var27 - var25 > (double)(var30 * (float)var29) && (!this.serverController.isSinglePlayer() || !this.serverController.getServerOwner().equals(this.playerEntity.getName()))) {
                        LOGGER.warn("{} moved too quickly! {},{},{}", new Object[]{this.playerEntity.getName(), var19, var21, var23});
                        this.setPlayerLocation(this.playerEntity.posX, this.playerEntity.posY, this.playerEntity.posZ, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
                        return;
                     }
                  }

                  boolean var42 = var2.getCollisionBoxes(this.playerEntity, this.playerEntity.getEntityBoundingBox().contract(0.0625D)).isEmpty();
                  var19 = var11 - this.lastGoodX;
                  var21 = var13 - this.lastGoodY;
                  var23 = var15 - this.lastGoodZ;
                  if (this.playerEntity.onGround && !var1.isOnGround() && var21 > 0.0D) {
                     this.playerEntity.jump();
                  }

                  this.playerEntity.move(var19, var21, var23);
                  this.playerEntity.onGround = var1.isOnGround();
                  double var31 = var21;
                  var19 = var11 - this.playerEntity.posX;
                  var21 = var13 - this.playerEntity.posY;
                  if (var21 > -0.5D || var21 < 0.5D) {
                     var21 = 0.0D;
                  }

                  var23 = var15 - this.playerEntity.posZ;
                  var27 = var19 * var19 + var21 * var21 + var23 * var23;
                  boolean var33 = false;
                  if (!this.playerEntity.isInvulnerableDimensionChange() && var27 > 0.0625D && !this.playerEntity.isPlayerSleeping() && !this.playerEntity.interactionManager.isCreative() && this.playerEntity.interactionManager.getGameType() != GameType.SPECTATOR) {
                     var33 = true;
                     LOGGER.warn("{} moved wrongly!", new Object[]{this.playerEntity.getName()});
                  }

                  this.playerEntity.setPositionAndRotation(var11, var13, var15, var17, var18);
                  this.playerEntity.addMovementStat(this.playerEntity.posX - var3, this.playerEntity.posY - var5, this.playerEntity.posZ - var7);
                  if (!this.playerEntity.noClip && !this.playerEntity.isPlayerSleeping()) {
                     boolean var34 = var2.getCollisionBoxes(this.playerEntity, this.playerEntity.getEntityBoundingBox().contract(0.0625D)).isEmpty();
                     if (var42 && (var33 || !var34)) {
                        this.setPlayerLocation(var3, var5, var7, var17, var18);
                        return;
                     }
                  }

                  this.floating = var31 >= -0.03125D;
                  this.floating &= !this.serverController.isFlightAllowed() && !this.playerEntity.capabilities.allowFlying;
                  this.floating &= !this.playerEntity.isPotionActive(MobEffects.LEVITATION) && !this.playerEntity.isElytraFlying() && !var2.checkBlockCollision(this.playerEntity.getEntityBoundingBox().expandXyz(0.0625D).addCoord(0.0D, -0.55D, 0.0D));
                  this.playerEntity.onGround = var1.isOnGround();
                  this.serverController.getPlayerList().serverUpdateMovingPlayer(this.playerEntity);
                  this.playerEntity.handleFalling(this.playerEntity.posY - var9, var1.isOnGround());
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
      double var10 = var9.contains(SPacketPlayerPosLook.EnumFlags.X) ? this.playerEntity.posX : 0.0D;
      double var12 = var9.contains(SPacketPlayerPosLook.EnumFlags.Y) ? this.playerEntity.posY : 0.0D;
      double var14 = var9.contains(SPacketPlayerPosLook.EnumFlags.Z) ? this.playerEntity.posZ : 0.0D;
      this.targetPos = new Vec3d(var1 + var10, var3 + var12, var5 + var14);
      float var16 = var7;
      float var17 = var8;
      if (var9.contains(SPacketPlayerPosLook.EnumFlags.Y_ROT)) {
         var16 = var7 + this.playerEntity.rotationYaw;
      }

      if (var9.contains(SPacketPlayerPosLook.EnumFlags.X_ROT)) {
         var17 = var8 + this.playerEntity.rotationPitch;
      }

      if (++this.teleportId == Integer.MAX_VALUE) {
         this.teleportId = 0;
      }

      this.lastPositionUpdate = this.networkTickCount;
      this.playerEntity.setPositionAndRotation(this.targetPos.xCoord, this.targetPos.yCoord, this.targetPos.zCoord, var16, var17);
      this.playerEntity.connection.sendPacket(new SPacketPlayerPosLook(var1, var3, var5, var7, var8, var9, this.teleportId));
   }

   public void processPlayerDigging(CPacketPlayerDigging var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      WorldServer var2 = this.serverController.worldServerForDimension(this.playerEntity.dimension);
      BlockPos var3 = var1.getPosition();
      this.playerEntity.markPlayerActive();
      switch(var1.getAction()) {
      case SWAP_HELD_ITEMS:
         if (!this.playerEntity.isSpectator()) {
            ItemStack var15 = this.playerEntity.getHeldItem(EnumHand.OFF_HAND);
            this.playerEntity.setHeldItem(EnumHand.OFF_HAND, this.playerEntity.getHeldItem(EnumHand.MAIN_HAND));
            this.playerEntity.setHeldItem(EnumHand.MAIN_HAND, var15);
         }

         return;
      case DROP_ITEM:
         if (!this.playerEntity.isSpectator()) {
            this.playerEntity.dropItem(false);
         }

         return;
      case DROP_ALL_ITEMS:
         if (!this.playerEntity.isSpectator()) {
            this.playerEntity.dropItem(true);
         }

         return;
      case RELEASE_USE_ITEM:
         this.playerEntity.stopActiveHand();
         ItemStack var4 = this.playerEntity.getHeldItemMainhand();
         if (var4 != null && var4.stackSize == 0) {
            this.playerEntity.setHeldItem(EnumHand.MAIN_HAND, (ItemStack)null);
         }

         return;
      case START_DESTROY_BLOCK:
      case ABORT_DESTROY_BLOCK:
      case STOP_DESTROY_BLOCK:
         double var5 = this.playerEntity.posX - ((double)var3.getX() + 0.5D);
         double var7 = this.playerEntity.posY - ((double)var3.getY() + 0.5D) + 1.5D;
         double var9 = this.playerEntity.posZ - ((double)var3.getZ() + 0.5D);
         double var11 = var5 * var5 + var7 * var7 + var9 * var9;
         double var13 = this.playerEntity.interactionManager.getBlockReachDistance() + 1.0D;
         var13 = var13 * var13;
         if (var11 > var13) {
            return;
         } else if (var3.getY() >= this.serverController.getBuildLimit()) {
            return;
         } else {
            if (var1.getAction() == CPacketPlayerDigging.Action.START_DESTROY_BLOCK) {
               if (!this.serverController.isBlockProtected(var2, var3, this.playerEntity) && var2.getWorldBorder().contains(var3)) {
                  this.playerEntity.interactionManager.onBlockClicked(var3, var1.getFacing());
               } else {
                  this.playerEntity.connection.sendPacket(new SPacketBlockChange(var2, var3));
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

   public void processTryUseItemOnBlock(CPacketPlayerTryUseItemOnBlock var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      WorldServer var2 = this.serverController.worldServerForDimension(this.playerEntity.dimension);
      EnumHand var3 = var1.getHand();
      ItemStack var4 = this.playerEntity.getHeldItem(var3);
      BlockPos var5 = var1.getPos();
      EnumFacing var6 = var1.getDirection();
      this.playerEntity.markPlayerActive();
      if (var5.getY() < this.serverController.getBuildLimit() - 1 || var6 != EnumFacing.UP && var5.getY() < this.serverController.getBuildLimit()) {
         double var10 = this.playerEntity.interactionManager.getBlockReachDistance() + 3.0D;
         var10 = var10 * var10;
         if (this.targetPos == null && this.playerEntity.getDistanceSq((double)var5.getX() + 0.5D, (double)var5.getY() + 0.5D, (double)var5.getZ() + 0.5D) < var10 && !this.serverController.isBlockProtected(var2, var5, this.playerEntity) && var2.getWorldBorder().contains(var5)) {
            this.playerEntity.interactionManager.processRightClickBlock(this.playerEntity, var2, var4, var3, var5, var6, var1.getFacingX(), var1.getFacingY(), var1.getFacingZ());
         }
      } else {
         TextComponentTranslation var7 = new TextComponentTranslation("build.tooHigh", new Object[]{this.serverController.getBuildLimit()});
         var7.getStyle().setColor(TextFormatting.RED);
         this.playerEntity.connection.sendPacket(new SPacketChat(var7));
      }

      this.playerEntity.connection.sendPacket(new SPacketBlockChange(var2, var5));
      this.playerEntity.connection.sendPacket(new SPacketBlockChange(var2, var5.offset(var6)));
      var4 = this.playerEntity.getHeldItem(var3);
      if (var4 != null && var4.stackSize == 0) {
         this.playerEntity.setHeldItem(var3, (ItemStack)null);
         ForgeEventFactory.onPlayerDestroyItem(this.playerEntity, var4, var3);
      }

   }

   public void processTryUseItem(CPacketPlayerTryUseItem var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      WorldServer var2 = this.serverController.worldServerForDimension(this.playerEntity.dimension);
      EnumHand var3 = var1.getHand();
      ItemStack var4 = this.playerEntity.getHeldItem(var3);
      this.playerEntity.markPlayerActive();
      if (var4 != null) {
         this.playerEntity.interactionManager.processRightClick(this.playerEntity, var2, var4, var3);
         var4 = this.playerEntity.getHeldItem(var3);
         if (var4 != null && var4.stackSize == 0) {
            this.playerEntity.setHeldItem(var3, (ItemStack)null);
            ForgeEventFactory.onPlayerDestroyItem(this.playerEntity, var4, var3);
            var4 = null;
         }
      }

   }

   public void handleSpectate(CPacketSpectate var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      if (this.playerEntity.isSpectator()) {
         Entity var2 = null;

         for(WorldServer var6 : this.serverController.worlds) {
            if (var6 != null) {
               var2 = var1.getEntity(var6);
               if (var2 != null) {
                  break;
               }
            }
         }

         if (var2 != null) {
            this.playerEntity.setSpectatingEntity(this.playerEntity);
            this.playerEntity.dismountRidingEntity();
            if (var2.world == this.playerEntity.world) {
               this.playerEntity.setPositionAndUpdate(var2.posX, var2.posY, var2.posZ);
            } else {
               WorldServer var7 = this.playerEntity.getServerWorld();
               WorldServer var8 = (WorldServer)var2.world;
               this.playerEntity.dimension = var2.dimension;
               this.sendPacket(new SPacketRespawn(this.playerEntity.dimension, var7.getDifficulty(), var7.getWorldInfo().getTerrainType(), this.playerEntity.interactionManager.getGameType()));
               this.serverController.getPlayerList().updatePermissionLevel(this.playerEntity);
               var7.removeEntityDangerously(this.playerEntity);
               this.playerEntity.isDead = false;
               this.playerEntity.setLocationAndAngles(var2.posX, var2.posY, var2.posZ, var2.rotationYaw, var2.rotationPitch);
               if (this.playerEntity.isEntityAlive()) {
                  var7.updateEntityWithOptionalForce(this.playerEntity, false);
                  var8.spawnEntity(this.playerEntity);
                  var8.updateEntityWithOptionalForce(this.playerEntity, false);
               }

               this.playerEntity.setWorld(var8);
               this.serverController.getPlayerList().preparePlayer(this.playerEntity, var7);
               this.playerEntity.setPositionAndUpdate(var2.posX, var2.posY, var2.posZ);
               this.playerEntity.interactionManager.setWorld(var8);
               this.serverController.getPlayerList().updateTimeAndWeatherForPlayer(this.playerEntity, var8);
               this.serverController.getPlayerList().syncPlayerInventory(this.playerEntity);
            }
         }
      }

   }

   public void handleResourcePackStatus(CPacketResourcePackStatus var1) {
   }

   public void processSteerBoat(CPacketSteerBoat var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      Entity var2 = this.playerEntity.getRidingEntity();
      if (var2 instanceof EntityBoat) {
         ((EntityBoat)var2).setPaddleState(var1.getLeft(), var1.getRight());
      }

   }

   public void onDisconnect(ITextComponent var1) {
      LOGGER.info("{} lost connection: {}", new Object[]{this.playerEntity.getName(), var1});
      this.serverController.refreshStatusNextTick();
      TextComponentTranslation var2 = new TextComponentTranslation("multiplayer.player.left", new Object[]{this.playerEntity.getDisplayName()});
      var2.getStyle().setColor(TextFormatting.YELLOW);
      this.serverController.getPlayerList().sendChatMsg(var2);
      this.playerEntity.mountEntityAndWakeUp();
      this.serverController.getPlayerList().playerLoggedOut(this.playerEntity);
      if (this.serverController.isSinglePlayer() && this.playerEntity.getName().equals(this.serverController.getServerOwner())) {
         LOGGER.info("Stopping singleplayer server as player logged out");
         this.serverController.initiateShutdown();
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

      try {
         this.netManager.sendPacket(var1);
      } catch (Throwable var5) {
         CrashReport var6 = CrashReport.makeCrashReport(var5, "Sending packet");
         CrashReportCategory var4 = var6.makeCategory("Packet being sent");
         var4.setDetail("Packet class", new ICrashReportDetail() {
            public String call() throws Exception {
               return var1.getClass().getCanonicalName();
            }
         });
         throw new ReportedException(var6);
      }
   }

   public void processHeldItemChange(CPacketHeldItemChange var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      if (var1.getSlotId() >= 0 && var1.getSlotId() < InventoryPlayer.getHotbarSize()) {
         this.playerEntity.inventory.currentItem = var1.getSlotId();
         this.playerEntity.markPlayerActive();
      } else {
         LOGGER.warn("{} tried to set an invalid carried item", new Object[]{this.playerEntity.getName()});
      }

   }

   public void processChatMessage(CPacketChatMessage var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      if (this.playerEntity.getChatVisibility() == EntityPlayer.EnumChatVisibility.HIDDEN) {
         TextComponentTranslation var2 = new TextComponentTranslation("chat.cannotSend", new Object[0]);
         var2.getStyle().setColor(TextFormatting.RED);
         this.sendPacket(new SPacketChat(var2));
      } else {
         this.playerEntity.markPlayerActive();
         String var4 = var1.getMessage();
         var4 = StringUtils.normalizeSpace(var4);

         for(int var3 = 0; var3 < var4.length(); ++var3) {
            if (!ChatAllowedCharacters.isAllowedCharacter(var4.charAt(var3))) {
               this.disconnect("Illegal characters in chat");
               return;
            }
         }

         if (var4.startsWith("/")) {
            this.handleSlashCommand(var4);
         } else {
            TextComponentTranslation var6 = new TextComponentTranslation("chat.type.text", new Object[]{this.playerEntity.getDisplayName(), ForgeHooks.newChatWithLinks(var4)});
            ITextComponent var7 = ForgeHooks.onServerChatEvent(this, var4, var6);
            if (var7 == null) {
               return;
            }

            this.serverController.getPlayerList().sendChatMsgImpl(var7, false);
         }

         this.chatSpamThresholdCount += 20;
         if (this.chatSpamThresholdCount > 200 && !this.serverController.getPlayerList().canSendCommands(this.playerEntity.getGameProfile())) {
            this.disconnect("disconnect.spam");
         }
      }

   }

   private void handleSlashCommand(String var1) {
      this.serverController.getCommandManager().executeCommand(this.playerEntity, var1);
   }

   public void handleAnimation(CPacketAnimation var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      this.playerEntity.markPlayerActive();
      this.playerEntity.swingArm(var1.getHand());
   }

   public void processEntityAction(CPacketEntityAction var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      this.playerEntity.markPlayerActive();
      switch(var1.getAction()) {
      case START_SNEAKING:
         this.playerEntity.setSneaking(true);
         break;
      case STOP_SNEAKING:
         this.playerEntity.setSneaking(false);
         break;
      case START_SPRINTING:
         this.playerEntity.setSprinting(true);
         break;
      case STOP_SPRINTING:
         this.playerEntity.setSprinting(false);
         break;
      case STOP_SLEEPING:
         this.playerEntity.wakeUpPlayer(false, true, true);
         this.targetPos = new Vec3d(this.playerEntity.posX, this.playerEntity.posY, this.playerEntity.posZ);
         break;
      case START_RIDING_JUMP:
         if (this.playerEntity.getRidingEntity() instanceof IJumpingMount) {
            IJumpingMount var5 = (IJumpingMount)this.playerEntity.getRidingEntity();
            int var3 = var1.getAuxData();
            if (var5.canJump() && var3 > 0) {
               var5.handleStartJump(var3);
            }
         }
         break;
      case STOP_RIDING_JUMP:
         if (this.playerEntity.getRidingEntity() instanceof IJumpingMount) {
            IJumpingMount var4 = (IJumpingMount)this.playerEntity.getRidingEntity();
            var4.handleStopJump();
         }
         break;
      case OPEN_INVENTORY:
         if (this.playerEntity.getRidingEntity() instanceof EntityHorse) {
            ((EntityHorse)this.playerEntity.getRidingEntity()).openGUI(this.playerEntity);
         }
         break;
      case START_FALL_FLYING:
         if (!this.playerEntity.onGround && this.playerEntity.motionY < 0.0D && !this.playerEntity.isElytraFlying() && !this.playerEntity.isInWater()) {
            ItemStack var2 = this.playerEntity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (var2 != null && var2.getItem() == Items.ELYTRA && ItemElytra.isBroken(var2)) {
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

   public void processUseEntity(CPacketUseEntity var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      WorldServer var2 = this.serverController.worldServerForDimension(this.playerEntity.dimension);
      Entity var3 = var1.getEntityFromWorld(var2);
      this.playerEntity.markPlayerActive();
      if (var3 != null) {
         boolean var4 = this.playerEntity.canEntityBeSeen(var3);
         double var5 = 36.0D;
         if (!var4) {
            var5 = 9.0D;
         }

         if (this.playerEntity.getDistanceSqToEntity(var3) < var5) {
            if (var1.getAction() == CPacketUseEntity.Action.INTERACT) {
               EnumHand var7 = var1.getHand();
               ItemStack var8 = this.playerEntity.getHeldItem(var7);
               this.playerEntity.interact(var3, var8, var7);
            } else if (var1.getAction() == CPacketUseEntity.Action.INTERACT_AT) {
               EnumHand var9 = var1.getHand();
               ItemStack var10 = this.playerEntity.getHeldItem(var9);
               if (ForgeHooks.onInteractEntityAt(this.playerEntity, var3, var1.getHitVec(), var10, var9)) {
                  return;
               }

               var3.applyPlayerInteraction(this.playerEntity, var1.getHitVec(), var10, var9);
            } else if (var1.getAction() == CPacketUseEntity.Action.ATTACK) {
               if (var3 instanceof EntityItem || var3 instanceof EntityXPOrb || var3 instanceof EntityArrow || var3 == this.playerEntity) {
                  this.disconnect("Attempting to attack an invalid entity");
                  this.serverController.logWarning("Player " + this.playerEntity.getName() + " tried to attack an invalid entity");
                  return;
               }

               this.playerEntity.attackTargetEntityWithCurrentItem(var3);
            }
         }
      }

   }

   public void processClientStatus(CPacketClientStatus var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      this.playerEntity.markPlayerActive();
      CPacketClientStatus.State var2 = var1.getStatus();
      switch(var2) {
      case PERFORM_RESPAWN:
         if (this.playerEntity.playerConqueredTheEnd) {
            this.playerEntity.playerConqueredTheEnd = false;
            this.playerEntity = this.serverController.getPlayerList().recreatePlayerEntity(this.playerEntity, 0, true);
         } else {
            if (this.playerEntity.getHealth() > 0.0F) {
               return;
            }

            this.playerEntity = this.serverController.getPlayerList().recreatePlayerEntity(this.playerEntity, this.playerEntity.dimension, false);
            if (this.serverController.isHardcore()) {
               this.playerEntity.setGameType(GameType.SPECTATOR);
               this.playerEntity.getServerWorld().getGameRules().setOrCreateGameRule("spectatorsGenerateChunks", "false");
            }
         }
         break;
      case REQUEST_STATS:
         this.playerEntity.getStatFile().sendStats(this.playerEntity);
         break;
      case OPEN_INVENTORY_ACHIEVEMENT:
         this.playerEntity.addStat(AchievementList.OPEN_INVENTORY);
      }

   }

   public void processCloseWindow(CPacketCloseWindow var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      this.playerEntity.closeContainer();
   }

   public void processClickWindow(CPacketClickWindow var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      this.playerEntity.markPlayerActive();
      if (this.playerEntity.openContainer.windowId == var1.getWindowId() && this.playerEntity.openContainer.getCanCraft(this.playerEntity)) {
         if (this.playerEntity.isSpectator()) {
            ArrayList var2 = Lists.newArrayList();

            for(int var3 = 0; var3 < this.playerEntity.openContainer.inventorySlots.size(); ++var3) {
               var2.add(((Slot)this.playerEntity.openContainer.inventorySlots.get(var3)).getStack());
            }

            this.playerEntity.updateCraftingInventory(this.playerEntity.openContainer, var2);
         } else {
            ItemStack var7 = this.playerEntity.openContainer.slotClick(var1.getSlotId(), var1.getUsedButton(), var1.getClickType(), this.playerEntity);
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
               ArrayList var8 = Lists.newArrayList();

               for(int var4 = 0; var4 < this.playerEntity.openContainer.inventorySlots.size(); ++var4) {
                  ItemStack var5 = ((Slot)this.playerEntity.openContainer.inventorySlots.get(var4)).getStack();
                  ItemStack var6 = var5 != null && var5.stackSize > 0 ? var5 : null;
                  var8.add(var6);
               }

               this.playerEntity.updateCraftingInventory(this.playerEntity.openContainer, var8);
            }
         }
      }

   }

   public void processEnchantItem(CPacketEnchantItem var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      this.playerEntity.markPlayerActive();
      if (this.playerEntity.openContainer.windowId == var1.getWindowId() && this.playerEntity.openContainer.getCanCraft(this.playerEntity) && !this.playerEntity.isSpectator()) {
         this.playerEntity.openContainer.enchantItem(this.playerEntity, var1.getButton());
         this.playerEntity.openContainer.detectAndSendChanges();
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

         boolean var8 = var1.getSlotId() >= 1 && var1.getSlotId() <= 45;
         boolean var9 = var3 == null || var3.getItem() != null;
         boolean var10 = var3 == null || var3.getMetadata() >= 0 && var3.stackSize <= 64 && var3.stackSize > 0;
         if (var8 && var9 && var10) {
            if (var3 == null) {
               this.playerEntity.inventoryContainer.putStackInSlot(var1.getSlotId(), (ItemStack)null);
            } else {
               this.playerEntity.inventoryContainer.putStackInSlot(var1.getSlotId(), var3);
            }

            this.playerEntity.inventoryContainer.setCanCraft(this.playerEntity, true);
         } else if (var2 && var9 && var10 && this.itemDropThreshold < 200) {
            this.itemDropThreshold += 20;
            EntityItem var11 = this.playerEntity.dropItem(var3, true);
            if (var11 != null) {
               var11.setAgeToCreativeDespawnTime();
            }
         }
      }

   }

   public void processConfirmTransaction(CPacketConfirmTransaction var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      Short var2 = (Short)this.pendingTransactions.lookup(this.playerEntity.openContainer.windowId);
      if (var2 != null && var1.getUid() == var2.shortValue() && this.playerEntity.openContainer.windowId == var1.getWindowId() && !this.playerEntity.openContainer.getCanCraft(this.playerEntity) && !this.playerEntity.isSpectator()) {
         this.playerEntity.openContainer.setCanCraft(this.playerEntity, true);
      }

   }

   public void processUpdateSign(CPacketUpdateSign var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      this.playerEntity.markPlayerActive();
      WorldServer var2 = this.serverController.worldServerForDimension(this.playerEntity.dimension);
      BlockPos var3 = var1.getPosition();
      if (var2.isBlockLoaded(var3)) {
         IBlockState var4 = var2.getBlockState(var3);
         TileEntity var5 = var2.getTileEntity(var3);
         if (!(var5 instanceof TileEntitySign)) {
            return;
         }

         TileEntitySign var6 = (TileEntitySign)var5;
         if (!var6.getIsEditable() || var6.getPlayer() != this.playerEntity) {
            this.serverController.logWarning("Player " + this.playerEntity.getName() + " just tried to change non-editable sign");
            return;
         }

         String[] var7 = var1.getLines();

         for(int var8 = 0; var8 < var7.length; ++var8) {
            var6.signText[var8] = new TextComponentString(TextFormatting.getTextWithoutFormattingCodes(var7[var8]));
         }

         var6.markDirty();
         var2.notifyBlockUpdate(var3, var4, var4, 3);
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
      this.playerEntity.capabilities.isFlying = var1.isFlying() && this.playerEntity.capabilities.allowFlying;
   }

   public void processTabComplete(CPacketTabComplete var1) {
      PacketThreadUtil.checkThreadAndEnqueue(var1, this, this.playerEntity.getServerWorld());
      ArrayList var2 = Lists.newArrayList();

      for(String var4 : this.serverController.getTabCompletions(this.playerEntity, var1.getMessage(), var1.getTargetBlock(), var1.hasTargetBlock())) {
         var2.add(var4);
      }

      this.playerEntity.connection.sendPacket(new SPacketTabComplete((String[])var2.toArray(new String[var2.size()])));
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
               var5.setTagInfo("pages", var4.getTagCompound().getTagList("pages", 8));
            }
         } catch (Exception var25) {
            LOGGER.error("Couldn't handle book info", var25);
         }
      } else if ("MC|BSign".equals(var2)) {
         PacketBuffer var27 = var1.getBufferData();

         try {
            ItemStack var35 = var27.readItemStack();
            if (var35 == null) {
               return;
            }

            if (!ItemWrittenBook.validBookTagContents(var35.getTagCompound())) {
               throw new IOException("Invalid book tag!");
            }

            ItemStack var43 = this.playerEntity.getHeldItemMainhand();
            if (var43 == null) {
               return;
            }

            if (var35.getItem() == Items.WRITABLE_BOOK && var43.getItem() == Items.WRITABLE_BOOK) {
               var43.setTagInfo("author", new NBTTagString(this.playerEntity.getName()));
               var43.setTagInfo("title", new NBTTagString(var35.getTagCompound().getString("title")));
               NBTTagList var6 = var35.getTagCompound().getTagList("pages", 8);

               for(int var7 = 0; var7 < var6.tagCount(); ++var7) {
                  String var8 = var6.getStringTagAt(var7);
                  TextComponentString var9 = new TextComponentString(var8);
                  var8 = ITextComponent.Serializer.componentToJson(var9);
                  var6.set(var7, new NBTTagString(var8));
               }

               var43.setTagInfo("pages", var6);
               var43.setItem(Items.WRITTEN_BOOK);
            }
         } catch (Exception var26) {
            LOGGER.error("Couldn't sign book", var26);
         }
      } else if ("MC|TrSel".equals(var2)) {
         try {
            int var28 = var1.getBufferData().readInt();
            Container var36 = this.playerEntity.openContainer;
            if (var36 instanceof ContainerMerchant) {
               ((ContainerMerchant)var36).setCurrentRecipeIndex(var28);
            }
         } catch (Exception var24) {
            LOGGER.error("Couldn't select trade", var24);
         }
      } else if ("MC|AdvCmd".equals(var2)) {
         if (!this.serverController.isCommandBlockEnabled()) {
            this.playerEntity.sendMessage(new TextComponentTranslation("advMode.notEnabled", new Object[0]));
            return;
         }

         if (!this.playerEntity.canUseCommandBlock()) {
            this.playerEntity.sendMessage(new TextComponentTranslation("advMode.notAllowed", new Object[0]));
            return;
         }

         PacketBuffer var29 = var1.getBufferData();

         try {
            byte var37 = var29.readByte();
            CommandBlockBaseLogic var44 = null;
            if (var37 == 0) {
               TileEntity var48 = this.playerEntity.world.getTileEntity(new BlockPos(var29.readInt(), var29.readInt(), var29.readInt()));
               if (var48 instanceof TileEntityCommandBlock) {
                  var44 = ((TileEntityCommandBlock)var48).getCommandBlockLogic();
               }
            } else if (var37 == 1) {
               Entity var49 = this.playerEntity.world.getEntityByID(var29.readInt());
               if (var49 instanceof EntityMinecartCommandBlock) {
                  var44 = ((EntityMinecartCommandBlock)var49).getCommandBlockLogic();
               }
            }

            String var50 = var29.readString(var29.readableBytes());
            boolean var54 = var29.readBoolean();
            if (var44 != null) {
               var44.setCommand(var50);
               var44.setTrackOutput(var54);
               if (!var54) {
                  var44.setLastOutput((ITextComponent)null);
               }

               var44.updateCommand();
               this.playerEntity.sendMessage(new TextComponentTranslation("advMode.setCommand.success", new Object[]{var50}));
            }
         } catch (Exception var23) {
            LOGGER.error("Couldn't set command block", var23);
         }
      } else if ("MC|AutoCmd".equals(var2)) {
         if (!this.serverController.isCommandBlockEnabled()) {
            this.playerEntity.sendMessage(new TextComponentTranslation("advMode.notEnabled", new Object[0]));
            return;
         }

         if (!this.playerEntity.canUseCommandBlock()) {
            this.playerEntity.sendMessage(new TextComponentTranslation("advMode.notAllowed", new Object[0]));
            return;
         }

         PacketBuffer var30 = var1.getBufferData();

         try {
            CommandBlockBaseLogic var38 = null;
            TileEntityCommandBlock var45 = null;
            BlockPos var51 = new BlockPos(var30.readInt(), var30.readInt(), var30.readInt());
            TileEntity var55 = this.playerEntity.world.getTileEntity(var51);
            if (var55 instanceof TileEntityCommandBlock) {
               var45 = (TileEntityCommandBlock)var55;
               var38 = var45.getCommandBlockLogic();
            }

            String var59 = var30.readString(var30.readableBytes());
            boolean var62 = var30.readBoolean();
            TileEntityCommandBlock.Mode var10 = TileEntityCommandBlock.Mode.valueOf(var30.readString(16));
            boolean var11 = var30.readBoolean();
            boolean var12 = var30.readBoolean();
            if (var38 != null) {
               EnumFacing var13 = (EnumFacing)this.playerEntity.world.getBlockState(var51).getValue(BlockCommandBlock.FACING);
               switch(var10) {
               case SEQUENCE:
                  IBlockState var14 = Blocks.CHAIN_COMMAND_BLOCK.getDefaultState();
                  this.playerEntity.world.setBlockState(var51, var14.withProperty(BlockCommandBlock.FACING, var13).withProperty(BlockCommandBlock.CONDITIONAL, Boolean.valueOf(var11)), 2);
                  break;
               case AUTO:
                  IBlockState var15 = Blocks.REPEATING_COMMAND_BLOCK.getDefaultState();
                  this.playerEntity.world.setBlockState(var51, var15.withProperty(BlockCommandBlock.FACING, var13).withProperty(BlockCommandBlock.CONDITIONAL, Boolean.valueOf(var11)), 2);
                  break;
               case REDSTONE:
                  IBlockState var16 = Blocks.COMMAND_BLOCK.getDefaultState();
                  this.playerEntity.world.setBlockState(var51, var16.withProperty(BlockCommandBlock.FACING, var13).withProperty(BlockCommandBlock.CONDITIONAL, Boolean.valueOf(var11)), 2);
               }

               var55.validate();
               this.playerEntity.world.setTileEntity(var51, var55);
               var38.setCommand(var59);
               var38.setTrackOutput(var62);
               if (!var62) {
                  var38.setLastOutput((ITextComponent)null);
               }

               var45.setAuto(var12);
               var38.updateCommand();
               if (!net.minecraft.util.StringUtils.isNullOrEmpty(var59)) {
                  this.playerEntity.sendMessage(new TextComponentTranslation("advMode.setCommand.success", new Object[]{var59}));
               }
            }
         } catch (Exception var22) {
            LOGGER.error("Couldn't set command block", var22);
         }
      } else if ("MC|Beacon".equals(var2)) {
         if (this.playerEntity.openContainer instanceof ContainerBeacon) {
            try {
               PacketBuffer var31 = var1.getBufferData();
               int var39 = var31.readInt();
               int var46 = var31.readInt();
               ContainerBeacon var52 = (ContainerBeacon)this.playerEntity.openContainer;
               Slot var56 = var52.getSlot(0);
               if (var56.getHasStack()) {
                  var56.decrStackSize(1);
                  IInventory var60 = var52.getTileEntity();
                  var60.setField(1, var39);
                  var60.setField(2, var46);
                  var60.markDirty();
               }
            } catch (Exception var21) {
               LOGGER.error("Couldn't set beacon", var21);
            }
         }
      } else if ("MC|ItemName".equals(var2)) {
         if (this.playerEntity.openContainer instanceof ContainerRepair) {
            ContainerRepair var32 = (ContainerRepair)this.playerEntity.openContainer;
            if (var1.getBufferData() != null && var1.getBufferData().readableBytes() >= 1) {
               String var40 = ChatAllowedCharacters.filterAllowedCharacters(var1.getBufferData().readString(32767));
               if (var40.length() <= 30) {
                  var32.updateItemName(var40);
               }
            } else {
               var32.updateItemName("");
            }
         }
      } else if ("MC|Struct".equals(var2)) {
         if (!this.playerEntity.canUseCommandBlock()) {
            return;
         }

         PacketBuffer var33 = var1.getBufferData();

         try {
            BlockPos var41 = new BlockPos(var33.readInt(), var33.readInt(), var33.readInt());
            IBlockState var47 = this.playerEntity.world.getBlockState(var41);
            TileEntity var53 = this.playerEntity.world.getTileEntity(var41);
            if (var53 instanceof TileEntityStructure) {
               TileEntityStructure var57 = (TileEntityStructure)var53;
               byte var61 = var33.readByte();
               String var63 = var33.readString(32);
               var57.setMode(TileEntityStructure.Mode.valueOf(var63));
               var57.setName(var33.readString(64));
               int var64 = MathHelper.clamp(var33.readInt(), -32, 32);
               int var65 = MathHelper.clamp(var33.readInt(), -32, 32);
               int var66 = MathHelper.clamp(var33.readInt(), -32, 32);
               var57.setPosition(new BlockPos(var64, var65, var66));
               int var67 = MathHelper.clamp(var33.readInt(), 0, 32);
               int var68 = MathHelper.clamp(var33.readInt(), 0, 32);
               int var69 = MathHelper.clamp(var33.readInt(), 0, 32);
               var57.setSize(new BlockPos(var67, var68, var69));
               String var70 = var33.readString(32);
               var57.setMirror(Mirror.valueOf(var70));
               String var17 = var33.readString(32);
               var57.setRotation(Rotation.valueOf(var17));
               var57.setMetadata(var33.readString(128));
               var57.setIgnoresEntities(var33.readBoolean());
               var57.setShowAir(var33.readBoolean());
               var57.setShowBoundingBox(var33.readBoolean());
               var57.setIntegrity(MathHelper.clamp(var33.readFloat(), 0.0F, 1.0F));
               var57.setSeed(var33.readVarLong());
               String var18 = var57.getName();
               if (var61 == 2) {
                  if (var57.save()) {
                     this.playerEntity.sendStatusMessage(new TextComponentTranslation("structure_block.save_success", new Object[]{var18}));
                  } else {
                     this.playerEntity.sendStatusMessage(new TextComponentTranslation("structure_block.save_failure", new Object[]{var18}));
                  }
               } else if (var61 == 3) {
                  if (!var57.isStructureLoadable()) {
                     this.playerEntity.sendStatusMessage(new TextComponentTranslation("structure_block.load_not_found", new Object[]{var18}));
                  } else if (var57.load()) {
                     this.playerEntity.sendStatusMessage(new TextComponentTranslation("structure_block.load_success", new Object[]{var18}));
                  } else {
                     this.playerEntity.sendStatusMessage(new TextComponentTranslation("structure_block.load_prepare", new Object[]{var18}));
                  }
               } else if (var61 == 4) {
                  if (var57.detectSize()) {
                     this.playerEntity.sendStatusMessage(new TextComponentTranslation("structure_block.size_success", new Object[]{var18}));
                  } else {
                     this.playerEntity.sendStatusMessage(new TextComponentTranslation("structure_block.size_failure", new Object[0]));
                  }
               }

               var57.markDirty();
               this.playerEntity.world.notifyBlockUpdate(var41, var47, var47, 3);
            }
         } catch (Exception var20) {
            LOGGER.error("Couldn't set structure block", var20);
         }
      } else if ("MC|PickItem".equals(var2)) {
         PacketBuffer var34 = var1.getBufferData();

         try {
            int var42 = var34.readVarInt();
            this.playerEntity.inventory.pickItem(var42);
            this.playerEntity.connection.sendPacket(new SPacketSetSlot(-2, this.playerEntity.inventory.currentItem, this.playerEntity.inventory.getStackInSlot(this.playerEntity.inventory.currentItem)));
            this.playerEntity.connection.sendPacket(new SPacketSetSlot(-2, var42, this.playerEntity.inventory.getStackInSlot(var42)));
            this.playerEntity.connection.sendPacket(new SPacketHeldItemChange(this.playerEntity.inventory.currentItem));
         } catch (Exception var19) {
            LOGGER.error("Couldn't pick item", var19);
         }
      }

   }
}
