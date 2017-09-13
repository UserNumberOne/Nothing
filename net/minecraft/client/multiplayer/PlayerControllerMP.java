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
      this.mc = mcIn;
      this.connection = netHandler;
   }

   public static void clickBlockCreative(Minecraft var0, PlayerControllerMP var1, BlockPos var2, EnumFacing var3) {
      if (!mcIn.world.extinguishFire(mcIn.player, pos, facing)) {
         playerController.onPlayerDestroyBlock(pos);
      }

   }

   public void setPlayerCapabilities(EntityPlayer var1) {
      this.currentGameType.configurePlayerCapabilities(player.capabilities);
   }

   public boolean isSpectator() {
      return this.currentGameType == GameType.SPECTATOR;
   }

   public void setGameType(GameType var1) {
      this.currentGameType = type;
      this.currentGameType.configurePlayerCapabilities(this.mc.player.capabilities);
   }

   public void flipPlayer(EntityPlayer var1) {
      playerIn.rotationYaw = -180.0F;
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
            ItemStack itemstack = this.mc.player.getHeldItemMainhand();
            if (itemstack == null) {
               return false;
            }

            if (!itemstack.canDestroy(this.mc.world.getBlockState(pos).getBlock())) {
               return false;
            }
         }
      }

      ItemStack stack = this.mc.player.getHeldItemMainhand();
      if (stack != null && stack.getItem() != null && stack.getItem().onBlockStartBreak(stack, pos, this.mc.player)) {
         return false;
      } else if (this.currentGameType.isCreative() && this.mc.player.getHeldItemMainhand() != null && this.mc.player.getHeldItemMainhand().getItem() instanceof ItemSword) {
         return false;
      } else {
         World world = this.mc.world;
         IBlockState iblockstate = world.getBlockState(pos);
         Block block = iblockstate.getBlock();
         if ((block instanceof BlockCommandBlock || block instanceof BlockStructure) && !this.mc.player.canUseCommandBlock()) {
            return false;
         } else if (iblockstate.getMaterial() == Material.AIR) {
            return false;
         } else {
            world.playEvent(2001, pos, Block.getStateId(iblockstate));
            this.currentBlock = new BlockPos(this.currentBlock.getX(), -1, this.currentBlock.getZ());
            if (!this.currentGameType.isCreative()) {
               ItemStack itemstack1 = this.mc.player.getHeldItemMainhand();
               if (itemstack1 != null) {
                  itemstack1.onBlockDestroyed(world, iblockstate, pos, this.mc.player);
                  if (itemstack1.stackSize <= 0) {
                     ForgeEventFactory.onPlayerDestroyItem(this.mc.player, itemstack1, EnumHand.MAIN_HAND);
                     this.mc.player.setHeldItem(EnumHand.MAIN_HAND, (ItemStack)null);
                  }
               }
            }

            boolean flag = block.removedByPlayer(iblockstate, world, pos, this.mc.player, false);
            if (flag) {
               block.onBlockDestroyedByPlayer(world, pos, iblockstate);
            }

            return flag;
         }
      }
   }

   public boolean clickBlock(BlockPos var1, EnumFacing var2) {
      if (this.currentGameType.isAdventure()) {
         if (this.currentGameType == GameType.SPECTATOR) {
            return false;
         }

         if (!this.mc.player.isAllowEdit()) {
            ItemStack itemstack = this.mc.player.getHeldItemMainhand();
            if (itemstack == null) {
               return false;
            }

            if (!itemstack.canDestroy(this.mc.world.getBlockState(loc).getBlock())) {
               return false;
            }
         }
      }

      if (!this.mc.world.getWorldBorder().contains(loc)) {
         return false;
      } else {
         if (this.currentGameType.isCreative()) {
            this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, loc, face));
            if (!ForgeHooks.onLeftClickBlock(this.mc.player, loc, face, ForgeHooks.rayTraceEyeHitVec(this.mc.player, (double)(this.getBlockReachDistance() + 1.0F))).isCanceled()) {
               clickBlockCreative(this.mc, this, loc, face);
            }

            this.blockHitDelay = 5;
         } else if (!this.isHittingBlock || !this.isHittingPosition(loc)) {
            if (this.isHittingBlock) {
               this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.currentBlock, face));
            }

            this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, loc, face));
            LeftClickBlock event = ForgeHooks.onLeftClickBlock(this.mc.player, loc, face, ForgeHooks.rayTraceEyeHitVec(this.mc.player, (double)(this.getBlockReachDistance() + 1.0F)));
            IBlockState iblockstate = this.mc.world.getBlockState(loc);
            boolean flag = iblockstate.getMaterial() != Material.AIR;
            if (flag && this.curBlockDamageMP == 0.0F && event.getUseBlock() != Result.DENY) {
               iblockstate.getBlock().onBlockClicked(this.mc.world, loc, this.mc.player);
            }

            if (event.getUseItem() == Result.DENY) {
               return true;
            }

            if (flag && iblockstate.getPlayerRelativeBlockHardness(this.mc.player, this.mc.player.world, loc) >= 1.0F) {
               this.onPlayerDestroyBlock(loc);
            } else {
               this.isHittingBlock = true;
               this.currentBlock = loc;
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
      } else if (this.currentGameType.isCreative() && this.mc.world.getWorldBorder().contains(posBlock)) {
         this.blockHitDelay = 5;
         this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, posBlock, directionFacing));
         clickBlockCreative(this.mc, this, posBlock, directionFacing);
         return true;
      } else if (this.isHittingPosition(posBlock)) {
         IBlockState iblockstate = this.mc.world.getBlockState(posBlock);
         Block block = iblockstate.getBlock();
         if (iblockstate.getMaterial() == Material.AIR) {
            this.isHittingBlock = false;
            return false;
         } else {
            this.curBlockDamageMP += iblockstate.getPlayerRelativeBlockHardness(this.mc.player, this.mc.player.world, posBlock);
            if (this.stepSoundTickCounter % 4.0F == 0.0F) {
               SoundType soundtype = block.getSoundType(iblockstate, this.mc.world, posBlock, this.mc.player);
               this.mc.getSoundHandler().playSound(new PositionedSoundRecord(soundtype.getHitSound(), SoundCategory.NEUTRAL, (soundtype.getVolume() + 1.0F) / 8.0F, soundtype.getPitch() * 0.5F, posBlock));
            }

            ++this.stepSoundTickCounter;
            if (this.curBlockDamageMP >= 1.0F) {
               this.isHittingBlock = false;
               this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, posBlock, directionFacing));
               this.onPlayerDestroyBlock(posBlock);
               this.curBlockDamageMP = 0.0F;
               this.stepSoundTickCounter = 0.0F;
               this.blockHitDelay = 5;
            }

            this.mc.world.sendBlockBreakProgress(this.mc.player.getEntityId(), this.currentBlock, (int)(this.curBlockDamageMP * 10.0F) - 1);
            return true;
         }
      } else {
         return this.clickBlock(posBlock, directionFacing);
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
      ItemStack itemstack = this.mc.player.getHeldItemMainhand();
      boolean flag = this.currentItemHittingBlock == null && itemstack == null;
      if (this.currentItemHittingBlock != null && itemstack != null) {
         flag = !ForgeHooksClient.shouldCauseBlockBreakReset(this.currentItemHittingBlock, itemstack);
      }

      return pos.equals(this.currentBlock) && flag;
   }

   private void syncCurrentPlayItem() {
      int i = this.mc.player.inventory.currentItem;
      if (i != this.currentPlayerItem) {
         this.currentPlayerItem = i;
         this.connection.sendPacket(new CPacketHeldItemChange(this.currentPlayerItem));
      }

   }

   public EnumActionResult processRightClickBlock(EntityPlayerSP var1, WorldClient var2, @Nullable ItemStack var3, BlockPos var4, EnumFacing var5, Vec3d var6, EnumHand var7) {
      this.syncCurrentPlayItem();
      float f = (float)(vec.xCoord - (double)pos.getX());
      float f1 = (float)(vec.yCoord - (double)pos.getY());
      float f2 = (float)(vec.zCoord - (double)pos.getZ());
      boolean flag = false;
      if (!this.mc.world.getWorldBorder().contains(pos)) {
         return EnumActionResult.FAIL;
      } else {
         RightClickBlock event = ForgeHooks.onRightClickBlock(player, hand, stack, pos, facing, ForgeHooks.rayTraceEyeHitVec(player, (double)(this.getBlockReachDistance() + 1.0F)));
         if (event.isCanceled()) {
            this.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, facing, hand, f, f1, f2));
            return EnumActionResult.PASS;
         } else {
            EnumActionResult result = EnumActionResult.PASS;
            if (this.currentGameType != GameType.SPECTATOR) {
               Item item = stack == null ? null : stack.getItem();
               EnumActionResult ret = item == null ? EnumActionResult.PASS : item.onItemUseFirst(stack, player, worldIn, pos, facing, f, f1, f2, hand);
               if (ret != EnumActionResult.PASS) {
                  return ret;
               }

               IBlockState iblockstate = worldIn.getBlockState(pos);
               boolean bypass = true;

               for(ItemStack s : new ItemStack[]{player.getHeldItemMainhand(), player.getHeldItemOffhand()}) {
                  bypass = bypass && (s == null || s.getItem().doesSneakBypassUse(s, worldIn, pos, player));
               }

               if (!player.isSneaking() || bypass || event.getUseBlock() == Result.ALLOW) {
                  if (event.getUseBlock() != Result.DENY) {
                     flag = iblockstate.getBlock().onBlockActivated(worldIn, pos, iblockstate, player, hand, stack, facing, f, f1, f2);
                  }

                  if (flag) {
                     result = EnumActionResult.SUCCESS;
                  }
               }

               if (!flag && stack != null && stack.getItem() instanceof ItemBlock) {
                  ItemBlock itemblock = (ItemBlock)stack.getItem();
                  if (!itemblock.canPlaceBlockOnSide(worldIn, pos, facing, player, stack)) {
                     return EnumActionResult.FAIL;
                  }
               }
            }

            this.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, facing, hand, f, f1, f2));
            if ((flag || this.currentGameType == GameType.SPECTATOR) && event.getUseItem() != Result.ALLOW) {
               return EnumActionResult.SUCCESS;
            } else if (stack == null) {
               return EnumActionResult.PASS;
            } else if (player.getCooldownTracker().hasCooldown(stack.getItem())) {
               return EnumActionResult.PASS;
            } else {
               if (stack.getItem() instanceof ItemBlock && !player.canUseCommandBlock()) {
                  Block block = ((ItemBlock)stack.getItem()).getBlock();
                  if (block instanceof BlockCommandBlock || block instanceof BlockStructure) {
                     return EnumActionResult.FAIL;
                  }
               }

               if (this.currentGameType.isCreative()) {
                  int i = stack.getMetadata();
                  int j = stack.stackSize;
                  if (event.getUseItem() != Result.DENY) {
                     EnumActionResult enumactionresult = stack.onItemUse(player, worldIn, pos, hand, facing, f, f1, f2);
                     stack.setItemDamage(i);
                     stack.stackSize = j;
                     return enumactionresult;
                  } else {
                     return result;
                  }
               } else {
                  if (event.getUseItem() != Result.DENY) {
                     result = stack.onItemUse(player, worldIn, pos, hand, facing, f, f1, f2);
                  }

                  if (stack.stackSize <= 0) {
                     ForgeEventFactory.onPlayerDestroyItem(player, stack, hand);
                  }

                  return result;
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
         this.connection.sendPacket(new CPacketPlayerTryUseItem(hand));
         if (player.getCooldownTracker().hasCooldown(stack.getItem())) {
            return EnumActionResult.PASS;
         } else if (ForgeHooks.onItemRightClick(player, hand, stack)) {
            return EnumActionResult.PASS;
         } else {
            int i = stack.stackSize;
            ActionResult actionresult = stack.useItemRightClick(worldIn, player, hand);
            ItemStack itemstack = (ItemStack)actionresult.getResult();
            if (itemstack != stack || itemstack.stackSize != i) {
               player.setHeldItem(hand, itemstack);
               if (itemstack.stackSize <= 0) {
                  player.setHeldItem(hand, (ItemStack)null);
                  ForgeEventFactory.onPlayerDestroyItem(player, itemstack, hand);
               }
            }

            return actionresult.getType();
         }
      }
   }

   public EntityPlayerSP createClientPlayer(World var1, StatisticsManager var2) {
      return new EntityPlayerSP(this.mc, worldIn, this.connection, statWriter);
   }

   public void attackEntity(EntityPlayer var1, Entity var2) {
      this.syncCurrentPlayItem();
      this.connection.sendPacket(new CPacketUseEntity(targetEntity));
      if (this.currentGameType != GameType.SPECTATOR) {
         playerIn.attackTargetEntityWithCurrentItem(targetEntity);
         playerIn.resetCooldown();
      }

   }

   public EnumActionResult interactWithEntity(EntityPlayer var1, Entity var2, @Nullable ItemStack var3, EnumHand var4) {
      this.syncCurrentPlayItem();
      this.connection.sendPacket(new CPacketUseEntity(target, hand));
      return this.currentGameType == GameType.SPECTATOR ? EnumActionResult.PASS : player.interact(target, heldItem, hand);
   }

   public EnumActionResult interactWithEntity(EntityPlayer var1, Entity var2, RayTraceResult var3, @Nullable ItemStack var4, EnumHand var5) {
      this.syncCurrentPlayItem();
      Vec3d vec3d = new Vec3d(raytrace.hitVec.xCoord - target.posX, raytrace.hitVec.yCoord - target.posY, raytrace.hitVec.zCoord - target.posZ);
      this.connection.sendPacket(new CPacketUseEntity(target, hand, vec3d));
      if (ForgeHooks.onInteractEntityAt(player, target, raytrace, player.getHeldItem(hand), hand)) {
         return EnumActionResult.PASS;
      } else {
         return this.currentGameType == GameType.SPECTATOR ? EnumActionResult.PASS : target.applyPlayerInteraction(player, vec3d, heldItem, hand);
      }
   }

   public ItemStack windowClick(int var1, int var2, int var3, ClickType var4, EntityPlayer var5) {
      short short1 = player.openContainer.getNextTransactionID(player.inventory);
      ItemStack itemstack = player.openContainer.slotClick(slotId, mouseButton, type, player);
      this.connection.sendPacket(new CPacketClickWindow(windowId, slotId, mouseButton, type, itemstack, short1));
      return itemstack;
   }

   public void sendEnchantPacket(int var1, int var2) {
      this.connection.sendPacket(new CPacketEnchantItem(windowID, button));
   }

   public void sendSlotPacket(ItemStack var1, int var2) {
      if (this.currentGameType.isCreative()) {
         this.connection.sendPacket(new CPacketCreativeInventoryAction(slotId, itemStackIn));
      }

   }

   public void sendPacketDropItem(ItemStack var1) {
      if (this.currentGameType.isCreative() && itemStackIn != null) {
         this.connection.sendPacket(new CPacketCreativeInventoryAction(-1, itemStackIn));
      }

   }

   public void onStoppedUsingItem(EntityPlayer var1) {
      this.syncCurrentPlayItem();
      this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
      playerIn.stopActiveHand();
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
      this.connection.sendPacket(new CPacketCustomPayload("MC|PickItem", (new PacketBuffer(Unpooled.buffer())).writeVarInt(index)));
   }
}
