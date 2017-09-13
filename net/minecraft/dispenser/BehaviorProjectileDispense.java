package net.minecraft.dispenser;

import net.minecraft.block.BlockDispenser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_10_R1.projectiles.CraftBlockProjectileSource;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.util.Vector;

public abstract class BehaviorProjectileDispense extends BehaviorDefaultDispenseItem {
   public ItemStack dispenseStack(IBlockSource isourceblock, ItemStack itemstack) {
      World world = isourceblock.getWorld();
      IPosition iposition = BlockDispenser.getDispensePosition(isourceblock);
      EnumFacing enumdirection = (EnumFacing)isourceblock.getBlockState().getValue(BlockDispenser.FACING);
      IProjectile iprojectile = this.getProjectileEntity(world, iposition, itemstack);
      ItemStack itemstack1 = itemstack.splitStack(1);
      Block block = world.getWorld().getBlockAt(isourceblock.getBlockPos().getX(), isourceblock.getBlockPos().getY(), isourceblock.getBlockPos().getZ());
      CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);
      BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new Vector((double)enumdirection.getFrontOffsetX(), (double)((float)enumdirection.getFrontOffsetY() + 0.1F), (double)enumdirection.getFrontOffsetZ()));
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

         iprojectile.setThrowableHeading(event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ(), this.getProjectileVelocity(), this.getProjectileInaccuracy());
         ((Entity)iprojectile).projectileSource = new CraftBlockProjectileSource((TileEntityDispenser)isourceblock.getBlockTileEntity());
         world.spawnEntity((Entity)iprojectile);
         return itemstack;
      }
   }

   protected void playDispenseSound(IBlockSource isourceblock) {
      isourceblock.getWorld().playEvent(1002, isourceblock.getBlockPos(), 0);
   }

   protected abstract IProjectile getProjectileEntity(World var1, IPosition var2, ItemStack var3);

   protected float getProjectileInaccuracy() {
      return 6.0F;
   }

   protected float getProjectileVelocity() {
      return 1.1F;
   }
}
