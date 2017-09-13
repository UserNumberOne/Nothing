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
   public ItemStack dispenseStack(IBlockSource var1, ItemStack var2) {
      World var3 = var1.getWorld();
      IPosition var4 = BlockDispenser.getDispensePosition(var1);
      EnumFacing var5 = (EnumFacing)var1.getBlockState().getValue(BlockDispenser.FACING);
      IProjectile var6 = this.getProjectileEntity(var3, var4, var2);
      ItemStack var7 = var2.splitStack(1);
      Block var8 = var3.getWorld().getBlockAt(var1.getBlockPos().getX(), var1.getBlockPos().getY(), var1.getBlockPos().getZ());
      CraftItemStack var9 = CraftItemStack.asCraftMirror(var7);
      BlockDispenseEvent var10 = new BlockDispenseEvent(var8, var9.clone(), new Vector((double)var5.getFrontOffsetX(), (double)((float)var5.getFrontOffsetY() + 0.1F), (double)var5.getFrontOffsetZ()));
      if (!BlockDispenser.eventFired) {
         var3.getServer().getPluginManager().callEvent(var10);
      }

      if (var10.isCancelled()) {
         ++var2.stackSize;
         return var2;
      } else {
         if (!var10.getItem().equals(var9)) {
            ++var2.stackSize;
            ItemStack var11 = CraftItemStack.asNMSCopy(var10.getItem());
            IBehaviorDispenseItem var12 = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(var11.getItem());
            if (var12 != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && var12 != this) {
               var12.dispense(var1, var11);
               return var2;
            }
         }

         var6.setThrowableHeading(var10.getVelocity().getX(), var10.getVelocity().getY(), var10.getVelocity().getZ(), this.getProjectileVelocity(), this.getProjectileInaccuracy());
         ((Entity)var6).projectileSource = new CraftBlockProjectileSource((TileEntityDispenser)var1.getBlockTileEntity());
         var3.spawnEntity((Entity)var6);
         return var2;
      }
   }

   protected void playDispenseSound(IBlockSource var1) {
      var1.getWorld().playEvent(1002, var1.getBlockPos(), 0);
   }

   protected abstract IProjectile getProjectileEntity(World var1, IPosition var2, ItemStack var3);

   protected float getProjectileInaccuracy() {
      return 6.0F;
   }

   protected float getProjectileVelocity() {
      return 1.1F;
   }
}
