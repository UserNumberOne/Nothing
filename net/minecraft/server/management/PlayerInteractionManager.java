package net.minecraft.server.management;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.Event.Result;

public class PlayerInteractionManager {
   private double blockReachDistance = 5.0D;
   public World world;
   public EntityPlayerMP player;
   private GameType gameType = GameType.NOT_SET;
   private boolean isDestroyingBlock;
   private int initialDamage;
   private BlockPos destroyPos = BlockPos.ORIGIN;
   private int curblockDamage;
   private boolean receivedFinishDiggingPacket;
   private BlockPos delayedDestroyPos = BlockPos.ORIGIN;
   private int initialBlockDamage;
   private int durabilityRemainingOnBlock = -1;

   public PlayerInteractionManager(World var1) {
      this.world = var1;
   }

   public void setGameType(GameType var1) {
      this.gameType = var1;
      var1.configurePlayerCapabilities(this.player.capabilities);
      this.player.sendPlayerAbilities();
      this.player.mcServer.getPlayerList().sendPacketToAllPlayers(new SPacketPlayerListItem(SPacketPlayerListItem.Action.UPDATE_GAME_MODE, new EntityPlayerMP[]{this.player}));
      this.world.updateAllPlayersSleepingFlag();
   }

   public GameType getGameType() {
      return this.gameType;
   }

   public boolean survivalOrAdventure() {
      return this.gameType.isSurvivalOrAdventure();
   }

   public boolean isCreative() {
      return this.gameType.isCreative();
   }

   public void initializeGameType(GameType var1) {
      if (this.gameType == GameType.NOT_SET) {
         this.gameType = var1;
      }

      this.setGameType(this.gameType);
   }

   public void updateBlockRemoving() {
      ++this.curblockDamage;
      if (this.receivedFinishDiggingPacket) {
         int var1 = this.curblockDamage - this.initialBlockDamage;
         IBlockState var2 = this.world.getBlockState(this.delayedDestroyPos);
         Block var3 = var2.getBlock();
         if (var3.isAir(var2, this.world, this.delayedDestroyPos)) {
            this.receivedFinishDiggingPacket = false;
         } else {
            float var4 = var2.getPlayerRelativeBlockHardness(this.player, this.player.world, this.delayedDestroyPos) * (float)(var1 + 1);
            int var5 = (int)(var4 * 10.0F);
            if (var5 != this.durabilityRemainingOnBlock) {
               this.world.sendBlockBreakProgress(this.player.getEntityId(), this.delayedDestroyPos, var5);
               this.durabilityRemainingOnBlock = var5;
            }

            if (var4 >= 1.0F) {
               this.receivedFinishDiggingPacket = false;
               this.tryHarvestBlock(this.delayedDestroyPos);
            }
         }
      } else if (this.isDestroyingBlock) {
         IBlockState var6 = this.world.getBlockState(this.destroyPos);
         Block var7 = var6.getBlock();
         if (var7.isAir(var6, this.world, this.destroyPos)) {
            this.world.sendBlockBreakProgress(this.player.getEntityId(), this.destroyPos, -1);
            this.durabilityRemainingOnBlock = -1;
            this.isDestroyingBlock = false;
         } else {
            int var8 = this.curblockDamage - this.initialDamage;
            float var9 = var6.getPlayerRelativeBlockHardness(this.player, this.player.world, this.destroyPos) * (float)(var8 + 1);
            int var10 = (int)(var9 * 10.0F);
            if (var10 != this.durabilityRemainingOnBlock) {
               this.world.sendBlockBreakProgress(this.player.getEntityId(), this.destroyPos, var10);
               this.durabilityRemainingOnBlock = var10;
            }
         }
      }

   }

   public void onBlockClicked(BlockPos var1, EnumFacing var2) {
      LeftClickBlock var3 = ForgeHooks.onLeftClickBlock(this.player, var1, var2, ForgeHooks.rayTraceEyeHitVec(this.player, this.getBlockReachDistance() + 1.0D));
      if (var3.isCanceled()) {
         this.player.connection.sendPacket(new SPacketBlockChange(this.world, var1));
         this.world.notifyBlockUpdate(var1, this.world.getBlockState(var1), this.world.getBlockState(var1), 3);
      } else {
         if (this.isCreative()) {
            if (!this.world.extinguishFire((EntityPlayer)null, var1, var2)) {
               this.tryHarvestBlock(var1);
            }
         } else {
            IBlockState var4 = this.world.getBlockState(var1);
            Block var5 = var4.getBlock();
            if (this.gameType.isAdventure()) {
               if (this.gameType == GameType.SPECTATOR) {
                  return;
               }

               if (!this.player.isAllowEdit()) {
                  ItemStack var6 = this.player.getHeldItemMainhand();
                  if (var6 == null) {
                     return;
                  }

                  if (!var6.canDestroy(var5)) {
                     return;
                  }
               }
            }

            this.initialDamage = this.curblockDamage;
            float var8 = 1.0F;
            if (!var4.getBlock().isAir(var4, this.world, var1)) {
               if (var3.getUseBlock() != Result.DENY) {
                  var5.onBlockClicked(this.world, var1, this.player);
                  this.world.extinguishFire((EntityPlayer)null, var1, var2);
               } else {
                  this.player.connection.sendPacket(new SPacketBlockChange(this.world, var1));
                  this.world.notifyBlockUpdate(var1, this.world.getBlockState(var1), this.world.getBlockState(var1), 3);
               }

               var8 = var4.getPlayerRelativeBlockHardness(this.player, this.player.world, var1);
            }

            if (var3.getUseItem() == Result.DENY) {
               if (var8 >= 1.0F) {
                  this.player.connection.sendPacket(new SPacketBlockChange(this.world, var1));
                  this.world.notifyBlockUpdate(var1, this.world.getBlockState(var1), this.world.getBlockState(var1), 3);
               }

               return;
            }

            if (!var4.getBlock().isAir(var4, this.world, var1) && var8 >= 1.0F) {
               this.tryHarvestBlock(var1);
            } else {
               this.isDestroyingBlock = true;
               this.destroyPos = var1;
               int var7 = (int)(var8 * 10.0F);
               this.world.sendBlockBreakProgress(this.player.getEntityId(), var1, var7);
               this.durabilityRemainingOnBlock = var7;
            }
         }

      }
   }

   public void blockRemoving(BlockPos var1) {
      if (var1.equals(this.destroyPos)) {
         int var2 = this.curblockDamage - this.initialDamage;
         IBlockState var3 = this.world.getBlockState(var1);
         if (!var3.getBlock().isAir(var3, this.world, var1)) {
            float var4 = var3.getPlayerRelativeBlockHardness(this.player, this.player.world, var1) * (float)(var2 + 1);
            if (var4 >= 0.7F) {
               this.isDestroyingBlock = false;
               this.world.sendBlockBreakProgress(this.player.getEntityId(), var1, -1);
               this.tryHarvestBlock(var1);
            } else if (!this.receivedFinishDiggingPacket) {
               this.isDestroyingBlock = false;
               this.receivedFinishDiggingPacket = true;
               this.delayedDestroyPos = var1;
               this.initialBlockDamage = this.initialDamage;
            }
         }
      }

   }

   public void cancelDestroyingBlock() {
      this.isDestroyingBlock = false;
      this.world.sendBlockBreakProgress(this.player.getEntityId(), this.destroyPos, -1);
   }

   private boolean removeBlock(BlockPos var1) {
      return this.removeBlock(var1, false);
   }

   private boolean removeBlock(BlockPos var1, boolean var2) {
      IBlockState var3 = this.world.getBlockState(var1);
      boolean var4 = var3.getBlock().removedByPlayer(var3, this.world, var1, this.player, var2);
      if (var4) {
         var3.getBlock().onBlockDestroyedByPlayer(this.world, var1, var3);
      }

      return var4;
   }

   public boolean tryHarvestBlock(BlockPos var1) {
      int var2 = ForgeHooks.onBlockBreakEvent(this.world, this.gameType, this.player, var1);
      if (var2 == -1) {
         return false;
      } else {
         IBlockState var3 = this.world.getBlockState(var1);
         TileEntity var4 = this.world.getTileEntity(var1);
         Block var5 = var3.getBlock();
         if ((var5 instanceof BlockCommandBlock || var5 instanceof BlockStructure) && !this.player.canUseCommandBlock()) {
            this.world.notifyBlockUpdate(var1, var3, var3, 3);
            return false;
         } else {
            ItemStack var6 = this.player.getHeldItemMainhand();
            if (var6 != null && var6.getItem().onBlockStartBreak(var6, var1, this.player)) {
               return false;
            } else {
               this.world.playEvent(this.player, 2001, var1, Block.getStateId(var3));
               boolean var7 = false;
               if (this.isCreative()) {
                  var7 = this.removeBlock(var1);
                  this.player.connection.sendPacket(new SPacketBlockChange(this.world, var1));
               } else {
                  ItemStack var8 = this.player.getHeldItemMainhand();
                  ItemStack var9 = var8 == null ? null : var8.copy();
                  boolean var10 = var3.getBlock().canHarvestBlock(this.world, var1, this.player);
                  if (var8 != null) {
                     var8.onBlockDestroyed(this.world, var3, var1, this.player);
                     if (var8.stackSize <= 0) {
                        ForgeEventFactory.onPlayerDestroyItem(this.player, var8, EnumHand.MAIN_HAND);
                        this.player.setHeldItem(EnumHand.MAIN_HAND, (ItemStack)null);
                     }
                  }

                  var7 = this.removeBlock(var1, var10);
                  if (var7 && var10) {
                     var3.getBlock().harvestBlock(this.world, this.player, var1, var3, var4, var9);
                  }
               }

               if (!this.isCreative() && var7 && var2 > 0) {
                  var3.getBlock().dropXpOnBlockBreak(this.world, var1, var2);
               }

               return var7;
            }
         }
      }
   }

   public EnumActionResult processRightClick(EntityPlayer var1, World var2, ItemStack var3, EnumHand var4) {
      if (this.gameType == GameType.SPECTATOR) {
         return EnumActionResult.PASS;
      } else if (var1.getCooldownTracker().hasCooldown(var3.getItem())) {
         return EnumActionResult.PASS;
      } else if (ForgeHooks.onItemRightClick(var1, var4, var3)) {
         return EnumActionResult.PASS;
      } else {
         int var5 = var3.stackSize;
         int var6 = var3.getMetadata();
         ActionResult var7 = var3.useItemRightClick(var2, var1, var4);
         ItemStack var8 = (ItemStack)var7.getResult();
         if (var8 == var3 && var8.stackSize == var5 && var8.getMaxItemUseDuration() <= 0 && var8.getMetadata() == var6) {
            return var7.getType();
         } else {
            var1.setHeldItem(var4, var8);
            if (this.isCreative()) {
               var8.stackSize = var5;
               if (var8.isItemStackDamageable()) {
                  var8.setItemDamage(var6);
               }
            }

            if (var8.stackSize == 0) {
               var1.setHeldItem(var4, (ItemStack)null);
               ForgeEventFactory.onPlayerDestroyItem(var1, var8, var4);
            }

            if (!var1.isHandActive()) {
               ((EntityPlayerMP)var1).sendContainerToPlayer(var1.inventoryContainer);
            }

            return var7.getType();
         }
      }
   }

   public EnumActionResult processRightClickBlock(EntityPlayer var1, World var2, @Nullable ItemStack var3, EnumHand var4, BlockPos var5, EnumFacing var6, float var7, float var8, float var9) {
      if (this.gameType == GameType.SPECTATOR) {
         TileEntity var18 = var2.getTileEntity(var5);
         if (var18 instanceof ILockableContainer) {
            Block var19 = var2.getBlockState(var5).getBlock();
            ILockableContainer var20 = (ILockableContainer)var18;
            if (var20 instanceof TileEntityChest && var19 instanceof BlockChest) {
               var20 = ((BlockChest)var19).getLockableContainer(var2, var5);
            }

            if (var20 != null) {
               var1.displayGUIChest(var20);
               return EnumActionResult.SUCCESS;
            }
         } else if (var18 instanceof IInventory) {
            var1.displayGUIChest((IInventory)var18);
            return EnumActionResult.SUCCESS;
         }

         return EnumActionResult.PASS;
      } else {
         RightClickBlock var10 = ForgeHooks.onRightClickBlock(var1, var4, var3, var5, var6, ForgeHooks.rayTraceEyeHitVec(var1, this.getBlockReachDistance() + 1.0D));
         if (var10.isCanceled()) {
            return EnumActionResult.PASS;
         } else {
            Item var11 = var3 == null ? null : var3.getItem();
            EnumActionResult var12 = var11 == null ? EnumActionResult.PASS : var11.onItemUseFirst(var3, var1, var2, var5, var6, var7, var8, var9, var4);
            if (var12 != EnumActionResult.PASS) {
               return var12;
            } else {
               boolean var13 = true;

               for(ItemStack var17 : new ItemStack[]{var1.getHeldItemMainhand(), var1.getHeldItemOffhand()}) {
                  var13 = var13 && (var17 == null || var17.getItem().doesSneakBypassUse(var17, var2, var5, var1));
               }

               EnumActionResult var21 = EnumActionResult.PASS;
               if (!var1.isSneaking() || var13 || var10.getUseBlock() == Result.ALLOW) {
                  IBlockState var22 = var2.getBlockState(var5);
                  if (var10.getUseBlock() != Result.DENY && var22.getBlock().onBlockActivated(var2, var5, var22, var1, var4, var3, var6, var7, var8, var9)) {
                     var21 = EnumActionResult.SUCCESS;
                  }
               }

               if (var3 == null) {
                  return EnumActionResult.PASS;
               } else if (var1.getCooldownTracker().hasCooldown(var3.getItem())) {
                  return EnumActionResult.PASS;
               } else {
                  if (var3.getItem() instanceof ItemBlock && !var1.canUseCommandBlock()) {
                     Block var23 = ((ItemBlock)var3.getItem()).getBlock();
                     if (var23 instanceof BlockCommandBlock || var23 instanceof BlockStructure) {
                        return EnumActionResult.FAIL;
                     }
                  }

                  if (this.isCreative()) {
                     int var24 = var3.getMetadata();
                     int var25 = var3.stackSize;
                     if ((var21 == EnumActionResult.SUCCESS || var10.getUseItem() == Result.DENY) && (var21 != EnumActionResult.SUCCESS || var10.getUseItem() != Result.ALLOW)) {
                        return var21;
                     } else {
                        EnumActionResult var26 = var3.onItemUse(var1, var2, var5, var4, var6, var7, var8, var9);
                        var3.setItemDamage(var24);
                        var3.stackSize = var25;
                        return var26;
                     }
                  } else if ((var21 == EnumActionResult.SUCCESS || var10.getUseItem() == Result.DENY) && (var21 != EnumActionResult.SUCCESS || var10.getUseItem() != Result.ALLOW)) {
                     return var21;
                  } else {
                     return var3.onItemUse(var1, var2, var5, var4, var6, var7, var8, var9);
                  }
               }
            }
         }
      }
   }

   public void setWorld(WorldServer var1) {
      this.world = var1;
   }

   public double getBlockReachDistance() {
      return this.blockReachDistance;
   }

   public void setBlockReachDistance(double var1) {
      this.blockReachDistance = var1;
   }
}
