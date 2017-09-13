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

      public ItemStack dispenseStack(IBlockSource var1, ItemStack var2) {
         EnumFacing var3 = (EnumFacing)var1.getBlockState().getValue(BlockDispenser.FACING);
         World var4 = var1.getWorld();
         double var5 = var1.getX() + (double)var3.getFrontOffsetX() * 1.125D;
         double var7 = Math.floor(var1.getY()) + (double)var3.getFrontOffsetY();
         double var9 = var1.getZ() + (double)var3.getFrontOffsetZ() * 1.125D;
         BlockPos var11 = var1.getBlockPos().offset(var3);
         IBlockState var12 = var4.getBlockState(var11);
         BlockRailBase.EnumRailDirection var13 = var12.getBlock() instanceof BlockRailBase ? (BlockRailBase.EnumRailDirection)var12.getValue(((BlockRailBase)var12.getBlock()).getShapeProperty()) : BlockRailBase.EnumRailDirection.NORTH_SOUTH;
         double var14;
         if (BlockRailBase.isRailBlock(var12)) {
            if (var13.isAscending()) {
               var14 = 0.6D;
            } else {
               var14 = 0.1D;
            }
         } else {
            if (var12.getMaterial() != Material.AIR || !BlockRailBase.isRailBlock(var4.getBlockState(var11.down()))) {
               return this.behaviourDefaultDispenseItem.dispense(var1, var2);
            }

            IBlockState var16 = var4.getBlockState(var11.down());
            BlockRailBase.EnumRailDirection var17 = var16.getBlock() instanceof BlockRailBase ? (BlockRailBase.EnumRailDirection)var16.getValue(((BlockRailBase)var16.getBlock()).getShapeProperty()) : BlockRailBase.EnumRailDirection.NORTH_SOUTH;
            if (var3 != EnumFacing.DOWN && var17.isAscending()) {
               var14 = -0.4D;
            } else {
               var14 = -0.9D;
            }
         }

         ItemStack var22 = var2.splitStack(1);
         Block var24 = var4.getWorld().getBlockAt(var1.getBlockPos().getX(), var1.getBlockPos().getY(), var1.getBlockPos().getZ());
         CraftItemStack var18 = CraftItemStack.asCraftMirror(var22);
         BlockDispenseEvent var19 = new BlockDispenseEvent(var24, var18.clone(), new Vector(var5, var7 + var14, var9));
         if (!BlockDispenser.eventFired) {
            var4.getServer().getPluginManager().callEvent(var19);
         }

         if (var19.isCancelled()) {
            ++var2.stackSize;
            return var2;
         } else {
            if (!var19.getItem().equals(var18)) {
               ++var2.stackSize;
               ItemStack var20 = CraftItemStack.asNMSCopy(var19.getItem());
               IBehaviorDispenseItem var21 = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(var20.getItem());
               if (var21 != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && var21 != this) {
                  var21.dispense(var1, var20);
                  return var2;
               }
            }

            var22 = CraftItemStack.asNMSCopy(var19.getItem());
            EntityMinecart var25 = EntityMinecart.create(var4, var19.getVelocity().getX(), var19.getVelocity().getY(), var19.getVelocity().getZ(), ((ItemMinecart)var22.getItem()).minecartType);
            if (var2.hasDisplayName()) {
               var25.setCustomNameTag(var2.getDisplayName());
            }

            var4.spawnEntity(var25);
            return var2;
         }
      }

      protected void playDispenseSound(IBlockSource var1) {
         var1.getWorld().playEvent(1000, var1.getBlockPos(), 0);
      }
   };
   private final EntityMinecart.Type minecartType;

   public ItemMinecart(EntityMinecart.Type var1) {
      this.maxStackSize = 1;
      this.minecartType = var1;
      this.setCreativeTab(CreativeTabs.TRANSPORTATION);
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, MINECART_DISPENSER_BEHAVIOR);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      IBlockState var10 = var3.getBlockState(var4);
      if (!BlockRailBase.isRailBlock(var10)) {
         return EnumActionResult.FAIL;
      } else {
         if (!var3.isRemote) {
            BlockRailBase.EnumRailDirection var11 = var10.getBlock() instanceof BlockRailBase ? (BlockRailBase.EnumRailDirection)var10.getValue(((BlockRailBase)var10.getBlock()).getShapeProperty()) : BlockRailBase.EnumRailDirection.NORTH_SOUTH;
            double var12 = 0.0D;
            if (var11.isAscending()) {
               var12 = 0.5D;
            }

            EntityMinecart var14 = EntityMinecart.create(var3, (double)var4.getX() + 0.5D, (double)var4.getY() + 0.0625D + var12, (double)var4.getZ() + 0.5D, this.minecartType);
            if (var1.hasDisplayName()) {
               var14.setCustomNameTag(var1.getDisplayName());
            }

            var3.spawnEntity(var14);
         }

         --var1.stackSize;
         return EnumActionResult.SUCCESS;
      }
   }
}
