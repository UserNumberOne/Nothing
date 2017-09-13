package net.minecraft.item;

import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.util.Vector;

public class ItemMinecart extends Item {
   private static final IBehaviorDispenseItem MINECART_DISPENSER_BEHAVIOR = new BehaviorDefaultDispenseItem() {
      private final BehaviorDefaultDispenseItem behaviourDefaultDispenseItem = new BehaviorDefaultDispenseItem();

      public ItemStack dispenseStack(IBlockSource isourceblock, ItemStack itemstack) {
         EnumFacing enumdirection = (EnumFacing)isourceblock.getBlockState().getValue(BlockDispenser.FACING);
         World world = isourceblock.getWorld();
         double d0 = isourceblock.getX() + (double)enumdirection.getFrontOffsetX() * 1.125D;
         double d1 = Math.floor(isourceblock.getY()) + (double)enumdirection.getFrontOffsetY();
         double d2 = isourceblock.getZ() + (double)enumdirection.getFrontOffsetZ() * 1.125D;
         BlockPos blockposition = isourceblock.getBlockPos().offset(enumdirection);
         IBlockState iblockdata = world.getBlockState(blockposition);
         BlockRailBase.EnumRailDirection blockminecarttrackabstract_enumtrackposition = iblockdata.getBlock() instanceof BlockRailBase ? (BlockRailBase.EnumRailDirection)iblockdata.getValue(((BlockRailBase)iblockdata.getBlock()).getShapeProperty()) : BlockRailBase.EnumRailDirection.NORTH_SOUTH;
         double d3;
         if (BlockRailBase.isRailBlock(iblockdata)) {
            if (blockminecarttrackabstract_enumtrackposition.isAscending()) {
               d3 = 0.6D;
            } else {
               d3 = 0.1D;
            }
         } else {
            if (iblockdata.getMaterial() != Material.AIR || !BlockRailBase.isRailBlock(world.getBlockState(blockposition.down()))) {
               return this.behaviourDefaultDispenseItem.dispense(isourceblock, itemstack);
            }

            IBlockState iblockdata1 = world.getBlockState(blockposition.down());
            BlockRailBase.EnumRailDirection blockminecarttrackabstract_enumtrackposition1 = iblockdata1.getBlock() instanceof BlockRailBase ? (BlockRailBase.EnumRailDirection)iblockdata1.getValue(((BlockRailBase)iblockdata1.getBlock()).getShapeProperty()) : BlockRailBase.EnumRailDirection.NORTH_SOUTH;
            if (enumdirection != EnumFacing.DOWN && blockminecarttrackabstract_enumtrackposition1.isAscending()) {
               d3 = -0.4D;
            } else {
               d3 = -0.9D;
            }
         }

         ItemStack itemstack1 = itemstack.splitStack(1);
         Block block2 = world.getWorld().getBlockAt(isourceblock.getBlockPos().getX(), isourceblock.getBlockPos().getY(), isourceblock.getBlockPos().getZ());
         CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);
         BlockDispenseEvent event = new BlockDispenseEvent(block2, craftItem.clone(), new Vector(d0, d1 + d3, d2));
         if (!BlockDispenser.eventFired) {
            world.getServer().getPluginManager().callEvent(event);
         }

         if (event.isCancelled()) {
            ++itemstack.stackSize;
            return itemstack;
         } else {
            if (!event.getItem().equals(craftItem)) {
               ++itemstack.stackSize;
               ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
               IBehaviorDispenseItem idispensebehavior = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(eventStack.getItem());
               if (idispensebehavior != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && idispensebehavior != this) {
                  idispensebehavior.dispense(isourceblock, eventStack);
                  return itemstack;
               }
            }

            itemstack1 = CraftItemStack.asNMSCopy(event.getItem());
            EntityMinecart entityminecartabstract = EntityMinecart.create(world, event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ(), ((ItemMinecart)itemstack1.getItem()).minecartType);
            if (itemstack.hasDisplayName()) {
               entityminecartabstract.setCustomNameTag(itemstack.getDisplayName());
            }

            world.spawnEntity(entityminecartabstract);
            return itemstack;
         }
      }

      protected void playDispenseSound(IBlockSource isourceblock) {
         isourceblock.getWorld().playEvent(1000, isourceblock.getBlockPos(), 0);
      }
   };
   private final EntityMinecart.Type minecartType;

   public ItemMinecart(EntityMinecart.Type entityminecartabstract_enumminecarttype) {
      this.maxStackSize = 1;
      this.minecartType = entityminecartabstract_enumminecarttype;
      this.setCreativeTab(CreativeTabs.TRANSPORTATION);
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, MINECART_DISPENSER_BEHAVIOR);
   }

   public EnumActionResult onItemUse(ItemStack itemstack, EntityPlayer entityhuman, World world, BlockPos blockposition, EnumHand enumhand, EnumFacing enumdirection, float f, float f1, float f2) {
      IBlockState iblockdata = world.getBlockState(blockposition);
      if (!BlockRailBase.isRailBlock(iblockdata)) {
         return EnumActionResult.FAIL;
      } else {
         if (!world.isRemote) {
            BlockRailBase.EnumRailDirection blockminecarttrackabstract_enumtrackposition = iblockdata.getBlock() instanceof BlockRailBase ? (BlockRailBase.EnumRailDirection)iblockdata.getValue(((BlockRailBase)iblockdata.getBlock()).getShapeProperty()) : BlockRailBase.EnumRailDirection.NORTH_SOUTH;
            double d0 = 0.0D;
            if (blockminecarttrackabstract_enumtrackposition.isAscending()) {
               d0 = 0.5D;
            }

            EntityMinecart entityminecartabstract = EntityMinecart.create(world, (double)blockposition.getX() + 0.5D, (double)blockposition.getY() + 0.0625D + d0, (double)blockposition.getZ() + 0.5D, this.minecartType);
            if (itemstack.hasDisplayName()) {
               entityminecartabstract.setCustomNameTag(itemstack.getDisplayName());
            }

            world.spawnEntity(entityminecartabstract);
         }

         --itemstack.stackSize;
         return EnumActionResult.SUCCESS;
      }
   }
}
