package net.minecraft.server.management;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCake;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.src.MinecraftServer;
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
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractionManager {
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
   public boolean interactResult = false;
   public boolean firedInteract = false;

   public PlayerInteractionManager(World world) {
      this.world = world;
   }

   public void setGameType(GameType enumgamemode) {
      this.gameType = enumgamemode;
      enumgamemode.configurePlayerCapabilities(this.player.capabilities);
      this.player.sendPlayerAbilities();
      this.player.mcServer.getPlayerList().sendAll(new SPacketPlayerListItem(SPacketPlayerListItem.Action.UPDATE_GAME_MODE, new EntityPlayerMP[]{this.player}), this.player);
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

   public void initializeGameType(GameType enumgamemode) {
      if (this.gameType == GameType.NOT_SET) {
         this.gameType = enumgamemode;
      }

      this.setGameType(this.gameType);
   }

   public void updateBlockRemoving() {
      this.curblockDamage = MinecraftServer.currentTick;
      if (this.receivedFinishDiggingPacket) {
         int j = this.curblockDamage - this.initialBlockDamage;
         IBlockState iblockdata = this.world.getBlockState(this.delayedDestroyPos);
         iblockdata.getBlock();
         if (iblockdata.getMaterial() == Material.AIR) {
            this.receivedFinishDiggingPacket = false;
         } else {
            float f = iblockdata.getPlayerRelativeBlockHardness(this.player, this.player.world, this.delayedDestroyPos) * (float)(j + 1);
            int i = (int)(f * 10.0F);
            if (i != this.durabilityRemainingOnBlock) {
               this.world.sendBlockBreakProgress(this.player.getEntityId(), this.delayedDestroyPos, i);
               this.durabilityRemainingOnBlock = i;
            }

            if (f >= 1.0F) {
               this.receivedFinishDiggingPacket = false;
               this.tryHarvestBlock(this.delayedDestroyPos);
            }
         }
      } else if (this.isDestroyingBlock) {
         IBlockState iblockdata1 = this.world.getBlockState(this.destroyPos);
         iblockdata1.getBlock();
         if (iblockdata1.getMaterial() == Material.AIR) {
            this.world.sendBlockBreakProgress(this.player.getEntityId(), this.destroyPos, -1);
            this.durabilityRemainingOnBlock = -1;
            this.isDestroyingBlock = false;
         } else {
            int k = this.curblockDamage - this.initialDamage;
            float f = iblockdata1.getPlayerRelativeBlockHardness(this.player, this.player.world, this.delayedDestroyPos) * (float)(k + 1);
            int i = (int)(f * 10.0F);
            if (i != this.durabilityRemainingOnBlock) {
               this.world.sendBlockBreakProgress(this.player.getEntityId(), this.destroyPos, i);
               this.durabilityRemainingOnBlock = i;
            }
         }
      }

   }

   public void onBlockClicked(BlockPos blockposition, EnumFacing enumdirection) {
      PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_BLOCK, blockposition, enumdirection, this.player.inventory.getCurrentItem(), EnumHand.MAIN_HAND);
      if (event.isCancelled()) {
         this.player.connection.sendPacket(new SPacketBlockChange(this.world, blockposition));
         TileEntity tileentity = this.world.getTileEntity(blockposition);
         if (tileentity != null) {
            this.player.connection.sendPacket(tileentity.getUpdatePacket());
         }

      } else {
         if (this.isCreative()) {
            if (!this.world.extinguishFire((EntityPlayer)null, blockposition, enumdirection)) {
               this.tryHarvestBlock(blockposition);
            }
         } else {
            IBlockState iblockdata = this.world.getBlockState(blockposition);
            Block block = iblockdata.getBlock();
            if (this.gameType.isAdventure()) {
               if (this.gameType == GameType.SPECTATOR) {
                  return;
               }

               if (!this.player.isAllowEdit()) {
                  ItemStack itemstack = this.player.getHeldItemMainhand();
                  if (itemstack == null) {
                     return;
                  }

                  if (!itemstack.canDestroy(block)) {
                     return;
                  }
               }
            }

            this.initialDamage = this.curblockDamage;
            float f = 1.0F;
            if (event.useInteractedBlock() == Result.DENY) {
               IBlockState data = this.world.getBlockState(blockposition);
               if (block == Blocks.OAK_DOOR) {
                  boolean bottom = data.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER;
                  this.player.connection.sendPacket(new SPacketBlockChange(this.world, blockposition));
                  this.player.connection.sendPacket(new SPacketBlockChange(this.world, bottom ? blockposition.up() : blockposition.down()));
               } else if (block == Blocks.TRAPDOOR) {
                  this.player.connection.sendPacket(new SPacketBlockChange(this.world, blockposition));
               }
            } else if (iblockdata.getMaterial() != Material.AIR) {
               block.onBlockClicked(this.world, blockposition, this.player);
               f = iblockdata.getPlayerRelativeBlockHardness(this.player, this.player.world, blockposition);
               this.world.extinguishFire((EntityPlayer)null, blockposition, enumdirection);
            }

            if (event.useItemInHand() == Result.DENY) {
               if (f > 1.0F) {
                  this.player.connection.sendPacket(new SPacketBlockChange(this.world, blockposition));
               }

               return;
            }

            BlockDamageEvent blockEvent = CraftEventFactory.callBlockDamageEvent(this.player, blockposition.getX(), blockposition.getY(), blockposition.getZ(), this.player.inventory.getCurrentItem(), f >= 1.0F);
            if (blockEvent.isCancelled()) {
               this.player.connection.sendPacket(new SPacketBlockChange(this.world, blockposition));
               return;
            }

            if (blockEvent.getInstaBreak()) {
               f = 2.0F;
            }

            if (iblockdata.getMaterial() != Material.AIR && f >= 1.0F) {
               this.tryHarvestBlock(blockposition);
            } else {
               this.isDestroyingBlock = true;
               this.destroyPos = blockposition;
               int i = (int)(f * 10.0F);
               this.world.sendBlockBreakProgress(this.player.getEntityId(), blockposition, i);
               this.durabilityRemainingOnBlock = i;
            }
         }

      }
   }

   public void blockRemoving(BlockPos blockposition) {
      if (blockposition.equals(this.destroyPos)) {
         this.curblockDamage = MinecraftServer.currentTick;
         int i = this.curblockDamage - this.initialDamage;
         IBlockState iblockdata = this.world.getBlockState(blockposition);
         if (iblockdata.getMaterial() != Material.AIR) {
            float f = iblockdata.getPlayerRelativeBlockHardness(this.player, this.player.world, blockposition) * (float)(i + 1);
            if (f >= 0.7F) {
               this.isDestroyingBlock = false;
               this.world.sendBlockBreakProgress(this.player.getEntityId(), blockposition, -1);
               this.tryHarvestBlock(blockposition);
            } else if (!this.receivedFinishDiggingPacket) {
               this.isDestroyingBlock = false;
               this.receivedFinishDiggingPacket = true;
               this.delayedDestroyPos = blockposition;
               this.initialBlockDamage = this.initialDamage;
            }
         }
      } else {
         this.player.connection.sendPacket(new SPacketBlockChange(this.world, blockposition));
      }

   }

   public void cancelDestroyingBlock() {
      this.isDestroyingBlock = false;
      this.world.sendBlockBreakProgress(this.player.getEntityId(), this.destroyPos, -1);
   }

   private boolean removeBlock(BlockPos blockposition) {
      IBlockState iblockdata = this.world.getBlockState(blockposition);
      iblockdata.getBlock().onBlockHarvested(this.world, blockposition, iblockdata, this.player);
      boolean flag = this.world.setBlockToAir(blockposition);
      if (flag) {
         iblockdata.getBlock().onBlockDestroyedByPlayer(this.world, blockposition, iblockdata);
      }

      return flag;
   }

   public boolean tryHarvestBlock(BlockPos blockposition) {
      BlockBreakEvent event = null;
      if (this.player instanceof EntityPlayerMP) {
         org.bukkit.block.Block block = this.world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
         boolean isSwordNoBreak = this.gameType.isCreative() && this.player.getHeldItemMainhand() != null && this.player.getHeldItemMainhand().getItem() instanceof ItemSword;
         if (this.world.getTileEntity(blockposition) == null && !isSwordNoBreak) {
            SPacketBlockChange packet = new SPacketBlockChange(this.world, blockposition);
            packet.blockState = Blocks.AIR.getDefaultState();
            this.player.connection.sendPacket(packet);
         }

         event = new BlockBreakEvent(block, this.player.getBukkitEntity());
         event.setCancelled(isSwordNoBreak);
         IBlockState nmsData = this.world.getBlockState(blockposition);
         Block nmsBlock = nmsData.getBlock();
         ItemStack itemstack = this.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
         if (nmsBlock != null && !event.isCancelled() && !this.isCreative() && this.player.canHarvestBlock(nmsBlock.getDefaultState()) && (!nmsBlock.canSilkHarvest() || EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, itemstack) <= 0)) {
            int bonusLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, itemstack);
            event.setExpToDrop(nmsBlock.getExpDrop(this.world, nmsData, bonusLevel));
         }

         this.world.getServer().getPluginManager().callEvent(event);
         if (event.isCancelled()) {
            if (isSwordNoBreak) {
               return false;
            }

            this.player.connection.sendPacket(new SPacketBlockChange(this.world, blockposition));
            if (nmsBlock instanceof BlockDoor) {
               boolean bottom = nmsData.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER;
               this.player.connection.sendPacket(new SPacketBlockChange(this.world, bottom ? blockposition.up() : blockposition.down()));
            }

            TileEntity tileentity = this.world.getTileEntity(blockposition);
            if (tileentity != null) {
               this.player.connection.sendPacket(tileentity.getUpdatePacket());
            }

            return false;
         }
      }

      IBlockState iblockdata = this.world.getBlockState(blockposition);
      if (iblockdata.getBlock() == Blocks.AIR) {
         return false;
      } else {
         TileEntity tileentity = this.world.getTileEntity(blockposition);
         Block block = iblockdata.getBlock();
         if (iblockdata.getBlock() == Blocks.SKULL && !this.isCreative()) {
            iblockdata.getBlock().dropBlockAsItemWithChance(this.world, blockposition, iblockdata, 1.0F, 0);
            return this.removeBlock(blockposition);
         } else if ((block instanceof BlockCommandBlock || block instanceof BlockStructure) && !this.player.canUseCommandBlock()) {
            this.world.notifyBlockUpdate(blockposition, iblockdata, iblockdata, 3);
            return false;
         } else {
            if (this.gameType.isAdventure()) {
               if (this.gameType == GameType.SPECTATOR) {
                  return false;
               }

               if (!this.player.isAllowEdit()) {
                  ItemStack itemstack = this.player.getHeldItemMainhand();
                  if (itemstack == null) {
                     return false;
                  }

                  if (!itemstack.canDestroy(block)) {
                     return false;
                  }
               }
            }

            this.world.playEvent(this.player, 2001, blockposition, Block.getStateId(iblockdata));
            boolean flag = this.removeBlock(blockposition);
            if (this.isCreative()) {
               this.player.connection.sendPacket(new SPacketBlockChange(this.world, blockposition));
            } else {
               ItemStack itemstack1 = this.player.getHeldItemMainhand();
               ItemStack itemstack2 = itemstack1 == null ? null : itemstack1.copy();
               boolean flag1 = this.player.canHarvestBlock(iblockdata);
               if (itemstack1 != null) {
                  itemstack1.onBlockDestroyed(this.world, iblockdata, blockposition, this.player);
                  if (itemstack1.stackSize == 0) {
                     this.player.setHeldItem(EnumHand.MAIN_HAND, (ItemStack)null);
                  }
               }

               if (flag && flag1) {
                  iblockdata.getBlock().harvestBlock(this.world, this.player, blockposition, iblockdata, tileentity, itemstack2);
               }
            }

            if (flag && event != null) {
               iblockdata.getBlock().dropXpOnBlockBreak(this.world, blockposition, event.getExpToDrop());
            }

            return flag;
         }
      }
   }

   public EnumActionResult processRightClick(EntityPlayer entityhuman, World world, ItemStack itemstack, EnumHand enumhand) {
      if (this.gameType == GameType.SPECTATOR) {
         return EnumActionResult.PASS;
      } else if (entityhuman.getCooldownTracker().hasCooldown(itemstack.getItem())) {
         return EnumActionResult.PASS;
      } else {
         int i = itemstack.stackSize;
         int j = itemstack.getMetadata();
         ActionResult interactionresultwrapper = itemstack.useItemRightClick(world, entityhuman, enumhand);
         ItemStack itemstack1 = (ItemStack)interactionresultwrapper.getResult();
         if (itemstack1 == itemstack && itemstack1.stackSize == i && itemstack1.getMaxItemUseDuration() <= 0 && itemstack1.getMetadata() == j) {
            return interactionresultwrapper.getType();
         } else {
            entityhuman.setHeldItem(enumhand, itemstack1);
            if (this.isCreative()) {
               itemstack1.stackSize = i;
               if (itemstack1.isItemStackDamageable()) {
                  itemstack1.setItemDamage(j);
               }
            }

            if (itemstack1.stackSize == 0) {
               entityhuman.setHeldItem(enumhand, (ItemStack)null);
            }

            if (!entityhuman.isHandActive()) {
               ((EntityPlayerMP)entityhuman).sendContainerToPlayer(entityhuman.inventoryContainer);
            }

            return interactionresultwrapper.getType();
         }
      }
   }

   public EnumActionResult processRightClickBlock(EntityPlayer entityhuman, World world, @Nullable ItemStack itemstack, EnumHand enumhand, BlockPos blockposition, EnumFacing enumdirection, float f, float f1, float f2) {
      IBlockState blockdata = world.getBlockState(blockposition);
      EnumActionResult result = EnumActionResult.FAIL;
      if (blockdata.getBlock() != Blocks.AIR) {
         boolean cancelledBlock = false;
         if (this.gameType == GameType.SPECTATOR) {
            TileEntity tileentity = world.getTileEntity(blockposition);
            cancelledBlock = !(tileentity instanceof ILockableContainer) && !(tileentity instanceof IInventory);
         }

         if (!entityhuman.getBukkitEntity().isOp() && itemstack != null && Block.getBlockFromItem(itemstack.getItem()) instanceof BlockCommandBlock) {
            cancelledBlock = true;
         }

         PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(entityhuman, Action.RIGHT_CLICK_BLOCK, blockposition, enumdirection, itemstack, cancelledBlock, enumhand);
         this.firedInteract = true;
         this.interactResult = event.useItemInHand() == Result.DENY;
         if (event.useInteractedBlock() == Result.DENY) {
            if (blockdata.getBlock() instanceof BlockDoor) {
               boolean bottom = blockdata.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER;
               ((EntityPlayerMP)entityhuman).connection.sendPacket(new SPacketBlockChange(world, bottom ? blockposition.up() : blockposition.down()));
            } else if (blockdata.getBlock() instanceof BlockCake) {
               ((EntityPlayerMP)entityhuman).getBukkitEntity().sendHealthUpdate();
            }

            result = event.useItemInHand() != Result.ALLOW ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
         } else {
            if (this.gameType == GameType.SPECTATOR) {
               TileEntity tileentity = world.getTileEntity(blockposition);
               if (tileentity instanceof ILockableContainer) {
                  Block block = world.getBlockState(blockposition).getBlock();
                  ILockableContainer itileinventory = (ILockableContainer)tileentity;
                  if (itileinventory instanceof TileEntityChest && block instanceof BlockChest) {
                     itileinventory = ((BlockChest)block).getLockableContainer(world, blockposition);
                  }

                  if (itileinventory != null) {
                     entityhuman.displayGUIChest(itileinventory);
                     return EnumActionResult.SUCCESS;
                  }
               } else if (tileentity instanceof IInventory) {
                  entityhuman.displayGUIChest((IInventory)tileentity);
                  return EnumActionResult.SUCCESS;
               }

               return EnumActionResult.PASS;
            }

            if (!entityhuman.isSneaking() || entityhuman.getHeldItemMainhand() == null && entityhuman.getHeldItemOffhand() == null) {
               result = blockdata.getBlock().onBlockActivated(world, blockposition, blockdata, entityhuman, enumhand, itemstack, enumdirection, f, f1, f2) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
            }
         }

         if (itemstack != null && result != EnumActionResult.SUCCESS && !this.interactResult) {
            int j1 = itemstack.getMetadata();
            int k1 = itemstack.stackSize;
            result = itemstack.onItemUse(entityhuman, world, blockposition, enumhand, enumdirection, f, f1, f2);
            if (this.isCreative()) {
               itemstack.setItemDamage(j1);
               itemstack.stackSize = k1;
            }
         }
      }

      return result;
   }

   public void setWorld(WorldServer worldserver) {
      this.world = worldserver;
   }
}
