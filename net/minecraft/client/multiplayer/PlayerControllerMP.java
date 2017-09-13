package net.minecraft.client.multiplayer;

import io.netty.buffer.Unpooled;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketCreativeInventoryAction;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.client.CPacketEnchantItem;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PlayerControllerMP {
   private final Minecraft mc;
   private final NetHandlerPlayClient connection;
   private BlockPos currentBlock = new BlockPos(-1, -1, -1);
   private ItemStack currentItemHittingBlock;
   private float curBlockDamageMP;
   private float stepSoundTickCounter;
   private int blockHitDelay;
   private boolean isHittingBlock;
   private GameType currentGameType = GameType.SURVIVAL;
   private int currentPlayerItem;

   public PlayerControllerMP(Minecraft var1, NetHandlerPlayClient var2) {
      this.mc = var1;
      this.connection = var2;
   }

   public static void clickBlockCreative(Minecraft var0, PlayerControllerMP var1, BlockPos var2, EnumFacing var3) {
      if (!var0.world.extinguishFire(var0.player, var2, var3)) {
         var1.onPlayerDestroyBlock(var2);
      }

   }

   public void setPlayerCapabilities(EntityPlayer var1) {
      this.currentGameType.configurePlayerCapabilities(var1.capabilities);
   }

   public boolean isSpectator() {
      return this.currentGameType == GameType.SPECTATOR;
   }

   public void setGameType(GameType var1) {
      this.currentGameType = var1;
      this.currentGameType.configurePlayerCapabilities(this.mc.player.capabilities);
   }

   public void flipPlayer(EntityPlayer var1) {
      var1.rotationYaw = -180.0F;
   }

   public boolean shouldDrawHUD() {
      return this.currentGameType.isSurvivalOrAdventure();
   }

   public boolean onPlayerDestroyBlock(BlockPos var1) {
      if (this.currentGameType.isAdventure()) {
         if (this.currentGameType == GameType.SPECTATOR) {
            return false;
         }

         if (!this.mc.player.isAllowEdit()) {
            ItemStack var2 = this.mc.player.getHeldItemMainhand();
            if (var2 == null) {
               return false;
            }

            if (!var2.canDestroy(this.mc.world.getBlockState(var1).getBlock())) {
               return false;
            }
         }
      }

      ItemStack var7 = this.mc.player.getHeldItemMainhand();
      if (var7 != null && var7.getItem() != null && var7.getItem().onBlockStartBreak(var7, var1, this.mc.player)) {
         return false;
      } else if (this.currentGameType.isCreative() && this.mc.player.getHeldItemMainhand() != null && this.mc.player.getHeldItemMainhand().getItem() instanceof ItemSword) {
         return false;
      } else {
         WorldClient var3 = this.mc.world;
         IBlockState var4 = var3.getBlockState(var1);
         Block var5 = var4.getBlock();
         if ((var5 instanceof BlockCommandBlock || var5 instanceof BlockStructure) && !this.mc.player.canUseCommandBlock()) {
            return false;
         } else if (var4.getMaterial() == Material.AIR) {
            return false;
         } else {
            var3.playEvent(2001, var1, Block.getStateId(var4));
            this.currentBlock = new BlockPos(this.currentBlock.getX(), -1, this.currentBlock.getZ());
            if (!this.currentGameType.isCreative()) {
               ItemStack var6 = this.mc.player.getHeldItemMainhand();
               if (var6 != null) {
                  var6.onBlockDestroyed(var3, var4, var1, this.mc.player);
                  if (var6.stackSize <= 0) {
                     ForgeEventFactory.onPlayerDestroyItem(this.mc.player, var6, EnumHand.MAIN_HAND);
                     this.mc.player.setHeldItem(EnumHand.MAIN_HAND, (ItemStack)null);
                  }
               }
            }

            boolean var8 = var5.removedByPlayer(var4, var3, var1, this.mc.player, false);
            if (var8) {
               var5.onBlockDestroyedByPlayer(var3, var1, var4);
            }

            return var8;
         }
      }
   }

   public boolean clickBlock(BlockPos var1, EnumFacing var2) {
      if (this.currentGameType.isAdventure()) {
         if (this.currentGameType == GameType.SPECTATOR) {
            return false;
         }

         if (!this.mc.player.isAllowEdit()) {
            ItemStack var3 = this.mc.player.getHeldItemMainhand();
            if (var3 == null) {
               return false;
            }

            if (!var3.canDestroy(this.mc.world.getBlockState(var1).getBlock())) {
               return false;
            }
         }
      }

      if (!this.mc.world.getWorldBorder().contains(var1)) {
         return false;
      } else {
         if (this.currentGameType.isCreative()) {
            this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, var1, var2));
            if (!ForgeHooks.onLeftClickBlock(this.mc.player, var1, var2, ForgeHooks.rayTraceEyeHitVec(this.mc.player, (double)(this.getBlockReachDistance() + 1.0F))).isCanceled()) {
               clickBlockCreative(this.mc, this, var1, var2);
            }

            this.blockHitDelay = 5;
         } else if (!this.isHittingBlock || !this.isHittingPosition(var1)) {
            if (this.isHittingBlock) {
               this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.currentBlock, var2));
            }

            this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, var1, var2));
            LeftClickBlock var6 = ForgeHooks.onLeftClickBlock(this.mc.player, var1, var2, ForgeHooks.rayTraceEyeHitVec(this.mc.player, (double)(this.getBlockReachDistance() + 1.0F)));
            IBlockState var4 = this.mc.world.getBlockState(var1);
            boolean var5 = var4.getMaterial() != Material.AIR;
            if (var5 && this.curBlockDamageMP == 0.0F && var6.getUseBlock() != Result.DENY) {
               var4.getBlock().onBlockClicked(this.mc.world, var1, this.mc.player);
            }

            if (var6.getUseItem() == Result.DENY) {
               return true;
            }

            if (var5 && var4.getPlayerRelativeBlockHardness(this.mc.player, this.mc.player.world, var1) >= 1.0F) {
               this.onPlayerDestroyBlock(var1);
            } else {
               this.isHittingBlock = true;
               this.currentBlock = var1;
               this.currentItemHittingBlock = this.mc.player.getHeldItemMainhand();
               this.curBlockDamageMP = 0.0F;
               this.stepSoundTickCounter = 0.0F;
               this.mc.world.sendBlockBreakProgress(this.mc.player.getEntityId(), this.currentBlock, (int)(this.curBlockDamageMP * 10.0F) - 1);
            }
         }

         return true;
      }
   }

   public void resetBlockRemoving() {
      if (this.isHittingBlock) {
         this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.currentBlock, EnumFacing.DOWN));
         this.isHittingBlock = false;
         this.curBlockDamageMP = 0.0F;
         this.mc.world.sendBlockBreakProgress(this.mc.player.getEntityId(), this.currentBlock, -1);
         this.mc.player.resetCooldown();
      }

   }

   public boolean onPlayerDamageBlock(BlockPos var1, EnumFacing var2) {
      this.syncCurrentPlayItem();
      if (this.blockHitDelay > 0) {
         --this.blockHitDelay;
         return true;
      } else if (this.currentGameType.isCreative() && this.mc.world.getWorldBorder().contains(var1)) {
         this.blockHitDelay = 5;
         this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, var1, var2));
         clickBlockCreative(this.mc, this, var1, var2);
         return true;
      } else if (this.isHittingPosition(var1)) {
         IBlockState var3 = this.mc.world.getBlockState(var1);
         Block var4 = var3.getBlock();
         if (var3.getMaterial() == Material.AIR) {
            this.isHittingBlock = false;
            return false;
         } else {
            this.curBlockDamageMP += var3.getPlayerRelativeBlockHardness(this.mc.player, this.mc.player.world, var1);
            if (this.stepSoundTickCounter % 4.0F == 0.0F) {
               SoundType var5 = var4.getSoundType(var3, this.mc.world, var1, this.mc.player);
               this.mc.getSoundHandler().playSound(new PositionedSoundRecord(var5.getHitSound(), SoundCategory.NEUTRAL, (var5.getVolume() + 1.0F) / 8.0F, var5.getPitch() * 0.5F, var1));
            }

            ++this.stepSoundTickCounter;
            if (this.curBlockDamageMP >= 1.0F) {
               this.isHittingBlock = false;
               this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, var1, var2));
               this.onPlayerDestroyBlock(var1);
               this.curBlockDamageMP = 0.0F;
               this.stepSoundTickCounter = 0.0F;
               this.blockHitDelay = 5;
            }

            this.mc.world.sendBlockBreakProgress(this.mc.player.getEntityId(), this.currentBlock, (int)(this.curBlockDamageMP * 10.0F) - 1);
            return true;
         }
      } else {
         return this.clickBlock(var1, var2);
      }
   }

   public float getBlockReachDistance() {
      return this.currentGameType.isCreative() ? 5.0F : 4.5F;
   }

   public void updateController() {
      this.syncCurrentPlayItem();
      if (this.connection.getNetworkManager().isChannelOpen()) {
         this.connection.getNetworkManager().processReceivedPackets();
      } else {
         this.connection.getNetworkManager().checkDisconnected();
      }

   }

   private boolean isHittingPosition(BlockPos var1) {
      ItemStack var2 = this.mc.player.getHeldItemMainhand();
      boolean var3 = this.currentItemHittingBlock == null && var2 == null;
      if (this.currentItemHittingBlock != null && var2 != null) {
         var3 = !ForgeHooksClient.shouldCauseBlockBreakReset(this.currentItemHittingBlock, var2);
      }

      return var1.equals(this.currentBlock) && var3;
   }

   private void syncCurrentPlayItem() {
      int var1 = this.mc.player.inventory.currentItem;
      if (var1 != this.currentPlayerItem) {
         this.currentPlayerItem = var1;
         this.connection.sendPacket(new CPacketHeldItemChange(this.currentPlayerItem));
      }

   }

   public EnumActionResult processRightClickBlock(EntityPlayerSP var1, WorldClient var2, @Nullable ItemStack var3, BlockPos var4, EnumFacing var5, Vec3d var6, EnumHand var7) {
      this.syncCurrentPlayItem();
      float var8 = (float)(var6.xCoord - (double)var4.getX());
      float var9 = (float)(var6.yCoord - (double)var4.getY());
      float var10 = (float)(var6.zCoord - (double)var4.getZ());
      boolean var11 = false;
      if (!this.mc.world.getWorldBorder().contains(var4)) {
         return EnumActionResult.FAIL;
      } else {
         RightClickBlock var12 = ForgeHooks.onRightClickBlock(var1, var7, var3, var4, var5, ForgeHooks.rayTraceEyeHitVec(var1, (double)(this.getBlockReachDistance() + 1.0F)));
         if (var12.isCanceled()) {
            this.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(var4, var5, var7, var8, var9, var10));
            return EnumActionResult.PASS;
         } else {
            EnumActionResult var13 = EnumActionResult.PASS;
            if (this.currentGameType != GameType.SPECTATOR) {
               Item var14 = var3 == null ? null : var3.getItem();
               EnumActionResult var15 = var14 == null ? EnumActionResult.PASS : var14.onItemUseFirst(var3, var1, var2, var4, var5, var8, var9, var10, var7);
               if (var15 != EnumActionResult.PASS) {
                  return var15;
               }

               IBlockState var16 = var2.getBlockState(var4);
               boolean var17 = true;

               for(ItemStack var21 : new ItemStack[]{var1.getHeldItemMainhand(), var1.getHeldItemOffhand()}) {
                  var17 = var17 && (var21 == null || var21.getItem().doesSneakBypassUse(var21, var2, var4, var1));
               }

               if (!var1.isSneaking() || var17 || var12.getUseBlock() == Result.ALLOW) {
                  if (var12.getUseBlock() != Result.DENY) {
                     var11 = var16.getBlock().onBlockActivated(var2, var4, var16, var1, var7, var3, var5, var8, var9, var10);
                  }

                  if (var11) {
                     var13 = EnumActionResult.SUCCESS;
                  }
               }

               if (!var11 && var3 != null && var3.getItem() instanceof ItemBlock) {
                  ItemBlock var26 = (ItemBlock)var3.getItem();
                  if (!var26.canPlaceBlockOnSide(var2, var4, var5, var1, var3)) {
                     return EnumActionResult.FAIL;
                  }
               }
            }

            this.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(var4, var5, var7, var8, var9, var10));
            if ((var11 || this.currentGameType == GameType.SPECTATOR) && var12.getUseItem() != Result.ALLOW) {
               return EnumActionResult.SUCCESS;
            } else if (var3 == null) {
               return EnumActionResult.PASS;
            } else if (var1.getCooldownTracker().hasCooldown(var3.getItem())) {
               return EnumActionResult.PASS;
            } else {
               if (var3.getItem() instanceof ItemBlock && !var1.canUseCommandBlock()) {
                  Block var22 = ((ItemBlock)var3.getItem()).getBlock();
                  if (var22 instanceof BlockCommandBlock || var22 instanceof BlockStructure) {
                     return EnumActionResult.FAIL;
                  }
               }

               if (this.currentGameType.isCreative()) {
                  int var23 = var3.getMetadata();
                  int var24 = var3.stackSize;
                  if (var12.getUseItem() != Result.DENY) {
                     EnumActionResult var25 = var3.onItemUse(var1, var2, var4, var7, var5, var8, var9, var10);
                     var3.setItemDamage(var23);
                     var3.stackSize = var24;
                     return var25;
                  } else {
                     return var13;
                  }
               } else {
                  if (var12.getUseItem() != Result.DENY) {
                     var13 = var3.onItemUse(var1, var2, var4, var7, var5, var8, var9, var10);
                  }

                  if (var3.stackSize <= 0) {
                     ForgeEventFactory.onPlayerDestroyItem(var1, var3, var7);
                  }

                  return var13;
               }
            }
         }
      }
   }

   public EnumActionResult processRightClick(EntityPlayer var1, World var2, ItemStack var3, EnumHand var4) {
      if (this.currentGameType == GameType.SPECTATOR) {
         return EnumActionResult.PASS;
      } else {
         this.syncCurrentPlayItem();
         this.connection.sendPacket(new CPacketPlayerTryUseItem(var4));
         if (var1.getCooldownTracker().hasCooldown(var3.getItem())) {
            return EnumActionResult.PASS;
         } else if (ForgeHooks.onItemRightClick(var1, var4, var3)) {
            return EnumActionResult.PASS;
         } else {
            int var5 = var3.stackSize;
            ActionResult var6 = var3.useItemRightClick(var2, var1, var4);
            ItemStack var7 = (ItemStack)var6.getResult();
            if (var7 != var3 || var7.stackSize != var5) {
               var1.setHeldItem(var4, var7);
               if (var7.stackSize <= 0) {
                  var1.setHeldItem(var4, (ItemStack)null);
                  ForgeEventFactory.onPlayerDestroyItem(var1, var7, var4);
               }
            }

            return var6.getType();
         }
      }
   }

   public EntityPlayerSP createClientPlayer(World var1, StatisticsManager var2) {
      return new EntityPlayerSP(this.mc, var1, this.connection, var2);
   }

   public void attackEntity(EntityPlayer var1, Entity var2) {
      this.syncCurrentPlayItem();
      this.connection.sendPacket(new CPacketUseEntity(var2));
      if (this.currentGameType != GameType.SPECTATOR) {
         var1.attackTargetEntityWithCurrentItem(var2);
         var1.resetCooldown();
      }

   }

   public EnumActionResult interactWithEntity(EntityPlayer var1, Entity var2, @Nullable ItemStack var3, EnumHand var4) {
      this.syncCurrentPlayItem();
      this.connection.sendPacket(new CPacketUseEntity(var2, var4));
      return this.currentGameType == GameType.SPECTATOR ? EnumActionResult.PASS : var1.interact(var2, var3, var4);
   }

   public EnumActionResult interactWithEntity(EntityPlayer var1, Entity var2, RayTraceResult var3, @Nullable ItemStack var4, EnumHand var5) {
      this.syncCurrentPlayItem();
      Vec3d var6 = new Vec3d(var3.hitVec.xCoord - var2.posX, var3.hitVec.yCoord - var2.posY, var3.hitVec.zCoord - var2.posZ);
      this.connection.sendPacket(new CPacketUseEntity(var2, var5, var6));
      if (ForgeHooks.onInteractEntityAt(var1, var2, var3, var1.getHeldItem(var5), var5)) {
         return EnumActionResult.PASS;
      } else {
         return this.currentGameType == GameType.SPECTATOR ? EnumActionResult.PASS : var2.applyPlayerInteraction(var1, var6, var4, var5);
      }
   }

   public ItemStack windowClick(int var1, int var2, int var3, ClickType var4, EntityPlayer var5) {
      short var6 = var5.openContainer.getNextTransactionID(var5.inventory);
      ItemStack var7 = var5.openContainer.slotClick(var2, var3, var4, var5);
      this.connection.sendPacket(new CPacketClickWindow(var1, var2, var3, var4, var7, var6));
      return var7;
   }

   public void sendEnchantPacket(int var1, int var2) {
      this.connection.sendPacket(new CPacketEnchantItem(var1, var2));
   }

   public void sendSlotPacket(ItemStack var1, int var2) {
      if (this.currentGameType.isCreative()) {
         this.connection.sendPacket(new CPacketCreativeInventoryAction(var2, var1));
      }

   }

   public void sendPacketDropItem(ItemStack var1) {
      if (this.currentGameType.isCreative() && var1 != null) {
         this.connection.sendPacket(new CPacketCreativeInventoryAction(-1, var1));
      }

   }

   public void onStoppedUsingItem(EntityPlayer var1) {
      this.syncCurrentPlayItem();
      this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
      var1.stopActiveHand();
   }

   public boolean gameIsSurvivalOrAdventure() {
      return this.currentGameType.isSurvivalOrAdventure();
   }

   public boolean isNotCreative() {
      return !this.currentGameType.isCreative();
   }

   public boolean isInCreativeMode() {
      return this.currentGameType.isCreative();
   }

   public boolean extendedReach() {
      return this.currentGameType.isCreative();
   }

   public boolean isRidingHorse() {
      return this.mc.player.isRiding() && this.mc.player.getRidingEntity() instanceof EntityHorse;
   }

   public boolean isSpectatorMode() {
      return this.currentGameType == GameType.SPECTATOR;
   }

   public GameType getCurrentGameType() {
      return this.currentGameType;
   }

   public boolean getIsHittingBlock() {
      return this.isHittingBlock;
   }

   public void pickItem(int var1) {
      this.connection.sendPacket(new CPacketCustomPayload("MC|PickItem", (new PacketBuffer(Unpooled.buffer())).writeVarInt(var1)));
   }
}
