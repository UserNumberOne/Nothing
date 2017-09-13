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

   public PlayerInteractionManager(World var1) {
      this.world = var1;
   }

   public void setGameType(GameType var1) {
      this.gameType = var1;
      var1.configurePlayerCapabilities(this.player.capabilities);
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

   public void initializeGameType(GameType var1) {
      if (this.gameType == GameType.NOT_SET) {
         this.gameType = var1;
      }

      this.setGameType(this.gameType);
   }

   public void updateBlockRemoving() {
      this.curblockDamage = MinecraftServer.currentTick;
      if (this.receivedFinishDiggingPacket) {
         int var1 = this.curblockDamage - this.initialBlockDamage;
         IBlockState var2 = this.world.getBlockState(this.delayedDestroyPos);
         var2.getBlock();
         if (var2.getMaterial() == Material.AIR) {
            this.receivedFinishDiggingPacket = false;
         } else {
            float var3 = var2.getPlayerRelativeBlockHardness(this.player, this.player.world, this.delayedDestroyPos) * (float)(var1 + 1);
            int var4 = (int)(var3 * 10.0F);
            if (var4 != this.durabilityRemainingOnBlock) {
               this.world.sendBlockBreakProgress(this.player.getEntityId(), this.delayedDestroyPos, var4);
               this.durabilityRemainingOnBlock = var4;
            }

            if (var3 >= 1.0F) {
               this.receivedFinishDiggingPacket = false;
               this.tryHarvestBlock(this.delayedDestroyPos);
            }
         }
      } else if (this.isDestroyingBlock) {
         IBlockState var5 = this.world.getBlockState(this.destroyPos);
         var5.getBlock();
         if (var5.getMaterial() == Material.AIR) {
            this.world.sendBlockBreakProgress(this.player.getEntityId(), this.destroyPos, -1);
            this.durabilityRemainingOnBlock = -1;
            this.isDestroyingBlock = false;
         } else {
            int var6 = this.curblockDamage - this.initialDamage;
            float var7 = var5.getPlayerRelativeBlockHardness(this.player, this.player.world, this.delayedDestroyPos) * (float)(var6 + 1);
            int var8 = (int)(var7 * 10.0F);
            if (var8 != this.durabilityRemainingOnBlock) {
               this.world.sendBlockBreakProgress(this.player.getEntityId(), this.destroyPos, var8);
               this.durabilityRemainingOnBlock = var8;
            }
         }
      }

   }

   public void onBlockClicked(BlockPos var1, EnumFacing var2) {
      PlayerInteractEvent var3 = CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_BLOCK, var1, var2, this.player.inventory.getCurrentItem(), EnumHand.MAIN_HAND);
      if (var3.isCancelled()) {
         this.player.connection.sendPacket(new SPacketBlockChange(this.world, var1));
         TileEntity var9 = this.world.getTileEntity(var1);
         if (var9 != null) {
            this.player.connection.sendPacket(var9.getUpdatePacket());
         }

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
            float var10 = 1.0F;
            if (var3.useInteractedBlock() == Result.DENY) {
               IBlockState var7 = this.world.getBlockState(var1);
               if (var5 == Blocks.OAK_DOOR) {
                  boolean var8 = var7.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER;
                  this.player.connection.sendPacket(new SPacketBlockChange(this.world, var1));
                  this.player.connection.sendPacket(new SPacketBlockChange(this.world, var8 ? var1.up() : var1.down()));
               } else if (var5 == Blocks.TRAPDOOR) {
                  this.player.connection.sendPacket(new SPacketBlockChange(this.world, var1));
               }
            } else if (var4.getMaterial() != Material.AIR) {
               var5.onBlockClicked(this.world, var1, this.player);
               var10 = var4.getPlayerRelativeBlockHardness(this.player, this.player.world, var1);
               this.world.extinguishFire((EntityPlayer)null, var1, var2);
            }

            if (var3.useItemInHand() == Result.DENY) {
               if (var10 > 1.0F) {
                  this.player.connection.sendPacket(new SPacketBlockChange(this.world, var1));
               }

               return;
            }

            BlockDamageEvent var11 = CraftEventFactory.callBlockDamageEvent(this.player, var1.getX(), var1.getY(), var1.getZ(), this.player.inventory.getCurrentItem(), var10 >= 1.0F);
            if (var11.isCancelled()) {
               this.player.connection.sendPacket(new SPacketBlockChange(this.world, var1));
               return;
            }

            if (var11.getInstaBreak()) {
               var10 = 2.0F;
            }

            if (var4.getMaterial() != Material.AIR && var10 >= 1.0F) {
               this.tryHarvestBlock(var1);
            } else {
               this.isDestroyingBlock = true;
               this.destroyPos = var1;
               int var12 = (int)(var10 * 10.0F);
               this.world.sendBlockBreakProgress(this.player.getEntityId(), var1, var12);
               this.durabilityRemainingOnBlock = var12;
            }
         }

      }
   }

   public void blockRemoving(BlockPos var1) {
      if (var1.equals(this.destroyPos)) {
         this.curblockDamage = MinecraftServer.currentTick;
         int var2 = this.curblockDamage - this.initialDamage;
         IBlockState var3 = this.world.getBlockState(var1);
         if (var3.getMaterial() != Material.AIR) {
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
      } else {
         this.player.connection.sendPacket(new SPacketBlockChange(this.world, var1));
      }

   }

   public void cancelDestroyingBlock() {
      this.isDestroyingBlock = false;
      this.world.sendBlockBreakProgress(this.player.getEntityId(), this.destroyPos, -1);
   }

   private boolean removeBlock(BlockPos var1) {
      IBlockState var2 = this.world.getBlockState(var1);
      var2.getBlock().onBlockHarvested(this.world, var1, var2, this.player);
      boolean var3 = this.world.setBlockToAir(var1);
      if (var3) {
         var2.getBlock().onBlockDestroyedByPlayer(this.world, var1, var2);
      }

      return var3;
   }

   public boolean tryHarvestBlock(BlockPos var1) {
      BlockBreakEvent var2 = null;
      if (this.player instanceof EntityPlayerMP) {
         org.bukkit.block.Block var3 = this.world.getWorld().getBlockAt(var1.getX(), var1.getY(), var1.getZ());
         boolean var4 = this.gameType.isCreative() && this.player.getHeldItemMainhand() != null && this.player.getHeldItemMainhand().getItem() instanceof ItemSword;
         if (this.world.getTileEntity(var1) == null && !var4) {
            SPacketBlockChange var5 = new SPacketBlockChange(this.world, var1);
            var5.blockState = Blocks.AIR.getDefaultState();
            this.player.connection.sendPacket(var5);
         }

         var2 = new BlockBreakEvent(var3, this.player.getBukkitEntity());
         var2.setCancelled(var4);
         IBlockState var12 = this.world.getBlockState(var1);
         Block var6 = var12.getBlock();
         ItemStack var7 = this.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
         if (var6 != null && !var2.isCancelled() && !this.isCreative() && this.player.canHarvestBlock(var6.getDefaultState()) && (!var6.canSilkHarvest() || EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, var7) <= 0)) {
            int var8 = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, var7);
            var2.setExpToDrop(var6.getExpDrop(this.world, var12, var8));
         }

         this.world.getServer().getPluginManager().callEvent(var2);
         if (var2.isCancelled()) {
            if (var4) {
               return false;
            }

            this.player.connection.sendPacket(new SPacketBlockChange(this.world, var1));
            if (var6 instanceof BlockDoor) {
               boolean var18 = var12.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER;
               this.player.connection.sendPacket(new SPacketBlockChange(this.world, var18 ? var1.up() : var1.down()));
            }

            TileEntity var19 = this.world.getTileEntity(var1);
            if (var19 != null) {
               this.player.connection.sendPacket(var19.getUpdatePacket());
            }

            return false;
         }
      }

      IBlockState var10 = this.world.getBlockState(var1);
      if (var10.getBlock() == Blocks.AIR) {
         return false;
      } else {
         TileEntity var11 = this.world.getTileEntity(var1);
         Block var13 = var10.getBlock();
         if (var10.getBlock() == Blocks.SKULL && !this.isCreative()) {
            var10.getBlock().dropBlockAsItemWithChance(this.world, var1, var10, 1.0F, 0);
            return this.removeBlock(var1);
         } else if ((var13 instanceof BlockCommandBlock || var13 instanceof BlockStructure) && !this.player.canUseCommandBlock()) {
            this.world.notifyBlockUpdate(var1, var10, var10, 3);
            return false;
         } else {
            if (this.gameType.isAdventure()) {
               if (this.gameType == GameType.SPECTATOR) {
                  return false;
               }

               if (!this.player.isAllowEdit()) {
                  ItemStack var14 = this.player.getHeldItemMainhand();
                  if (var14 == null) {
                     return false;
                  }

                  if (!var14.canDestroy(var13)) {
                     return false;
                  }
               }
            }

            this.world.playEvent(this.player, 2001, var1, Block.getStateId(var10));
            boolean var15 = this.removeBlock(var1);
            if (this.isCreative()) {
               this.player.connection.sendPacket(new SPacketBlockChange(this.world, var1));
            } else {
               ItemStack var16 = this.player.getHeldItemMainhand();
               ItemStack var17 = var16 == null ? null : var16.copy();
               boolean var9 = this.player.canHarvestBlock(var10);
               if (var16 != null) {
                  var16.onBlockDestroyed(this.world, var10, var1, this.player);
                  if (var16.stackSize == 0) {
                     this.player.setHeldItem(EnumHand.MAIN_HAND, (ItemStack)null);
                  }
               }

               if (var15 && var9) {
                  var10.getBlock().harvestBlock(this.world, this.player, var1, var10, var11, var17);
               }
            }

            if (var15 && var2 != null) {
               var10.getBlock().dropXpOnBlockBreak(this.world, var1, var2.getExpToDrop());
            }

            return var15;
         }
      }
   }

   public EnumActionResult processRightClick(EntityPlayer var1, World var2, ItemStack var3, EnumHand var4) {
      if (this.gameType == GameType.SPECTATOR) {
         return EnumActionResult.PASS;
      } else if (var1.getCooldownTracker().hasCooldown(var3.getItem())) {
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
            }

            if (!var1.isHandActive()) {
               ((EntityPlayerMP)var1).sendContainerToPlayer(var1.inventoryContainer);
            }

            return var7.getType();
         }
      }
   }

   public EnumActionResult processRightClickBlock(EntityPlayer var1, World var2, @Nullable ItemStack var3, EnumHand var4, BlockPos var5, EnumFacing var6, float var7, float var8, float var9) {
      IBlockState var10 = var2.getBlockState(var5);
      EnumActionResult var11 = EnumActionResult.FAIL;
      if (var10.getBlock() != Blocks.AIR) {
         boolean var12 = false;
         if (this.gameType == GameType.SPECTATOR) {
            TileEntity var13 = var2.getTileEntity(var5);
            var12 = !(var13 instanceof ILockableContainer) && !(var13 instanceof IInventory);
         }

         if (!var1.getBukkitEntity().isOp() && var3 != null && Block.getBlockFromItem(var3.getItem()) instanceof BlockCommandBlock) {
            var12 = true;
         }

         PlayerInteractEvent var17 = CraftEventFactory.callPlayerInteractEvent(var1, Action.RIGHT_CLICK_BLOCK, var5, var6, var3, var12, var4);
         this.firedInteract = true;
         this.interactResult = var17.useItemInHand() == Result.DENY;
         if (var17.useInteractedBlock() == Result.DENY) {
            if (var10.getBlock() instanceof BlockDoor) {
               boolean var18 = var10.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER;
               ((EntityPlayerMP)var1).connection.sendPacket(new SPacketBlockChange(var2, var18 ? var5.up() : var5.down()));
            } else if (var10.getBlock() instanceof BlockCake) {
               ((EntityPlayerMP)var1).getBukkitEntity().sendHealthUpdate();
            }

            var11 = var17.useItemInHand() != Result.ALLOW ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
         } else {
            if (this.gameType == GameType.SPECTATOR) {
               TileEntity var14 = var2.getTileEntity(var5);
               if (var14 instanceof ILockableContainer) {
                  Block var15 = var2.getBlockState(var5).getBlock();
                  ILockableContainer var16 = (ILockableContainer)var14;
                  if (var16 instanceof TileEntityChest && var15 instanceof BlockChest) {
                     var16 = ((BlockChest)var15).getLockableContainer(var2, var5);
                  }

                  if (var16 != null) {
                     var1.displayGUIChest(var16);
                     return EnumActionResult.SUCCESS;
                  }
               } else if (var14 instanceof IInventory) {
                  var1.displayGUIChest((IInventory)var14);
                  return EnumActionResult.SUCCESS;
               }

               return EnumActionResult.PASS;
            }

            if (!var1.isSneaking() || var1.getHeldItemMainhand() == null && var1.getHeldItemOffhand() == null) {
               var11 = var10.getBlock().onBlockActivated(var2, var5, var10, var1, var4, var3, var6, var7, var8, var9) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
            }
         }

         if (var3 != null && var11 != EnumActionResult.SUCCESS && !this.interactResult) {
            int var19 = var3.getMetadata();
            int var20 = var3.stackSize;
            var11 = var3.onItemUse(var1, var2, var5, var4, var6, var7, var8, var9);
            if (this.isCreative()) {
               var3.setItemDamage(var19);
               var3.stackSize = var20;
            }
         }
      }

      return var11;
   }

   public void setWorld(WorldServer var1) {
      this.world = var1;
   }
}
