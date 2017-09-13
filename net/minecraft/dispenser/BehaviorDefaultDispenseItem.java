package net.minecraft.dispenser;

import net.minecraft.block.BlockDispenser;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.util.Vector;

public class BehaviorDefaultDispenseItem implements IBehaviorDispenseItem {
   public final ItemStack dispense(IBlockSource var1, ItemStack var2) {
      ItemStack var3 = this.dispenseStack(var1, var2);
      this.playDispenseSound(var1);
      this.spawnDispenseParticles(var1, (EnumFacing)var1.getBlockState().getValue(BlockDispenser.FACING));
      return var3;
   }

   protected ItemStack dispenseStack(IBlockSource var1, ItemStack var2) {
      EnumFacing var3 = (EnumFacing)var1.getBlockState().getValue(BlockDispenser.FACING);
      BlockDispenser.getDispensePosition(var1);
      ItemStack var4 = var2.splitStack(1);
      if (!a(var1.getWorld(), var4, 6, var3, var1)) {
         ++var2.stackSize;
      }

      return var2;
   }

   public static boolean a(World var0, ItemStack var1, int var2, EnumFacing var3, IBlockSource var4) {
      IPosition var5 = BlockDispenser.getDispensePosition(var4);
      double var6 = var5.getX();
      double var8 = var5.getY();
      double var10 = var5.getZ();
      if (var3.getAxis() == EnumFacing.Axis.Y) {
         var8 = var8 - 0.125D;
      } else {
         var8 = var8 - 0.15625D;
      }

      EntityItem var12 = new EntityItem(var0, var6, var8, var10, var1);
      double var13 = var0.rand.nextDouble() * 0.1D + 0.2D;
      var12.motionX = (double)var3.getFrontOffsetX() * var13;
      var12.motionY = 0.20000000298023224D;
      var12.motionZ = (double)var3.getFrontOffsetZ() * var13;
      var12.motionX += var0.rand.nextGaussian() * 0.007499999832361937D * (double)var2;
      var12.motionY += var0.rand.nextGaussian() * 0.007499999832361937D * (double)var2;
      var12.motionZ += var0.rand.nextGaussian() * 0.007499999832361937D * (double)var2;
      Block var15 = var0.getWorld().getBlockAt(var4.getBlockPos().getX(), var4.getBlockPos().getY(), var4.getBlockPos().getZ());
      CraftItemStack var16 = CraftItemStack.asCraftMirror(var1);
      BlockDispenseEvent var17 = new BlockDispenseEvent(var15, var16.clone(), new Vector(var12.motionX, var12.motionY, var12.motionZ));
      if (!BlockDispenser.eventFired) {
         var0.getServer().getPluginManager().callEvent(var17);
      }

      if (var17.isCancelled()) {
         return false;
      } else {
         var12.setEntityItemStack(CraftItemStack.asNMSCopy(var17.getItem()));
         var12.motionX = var17.getVelocity().getX();
         var12.motionY = var17.getVelocity().getY();
         var12.motionZ = var17.getVelocity().getZ();
         if (var17.getItem().getType().equals(var16.getType())) {
            var0.spawnEntity(var12);
            return true;
         } else {
            ItemStack var18 = CraftItemStack.asNMSCopy(var17.getItem());
            IBehaviorDispenseItem var19 = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(var18.getItem());
            if (var19 != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && var19.getClass() != BehaviorDefaultDispenseItem.class) {
               var19.dispense(var4, var18);
            } else {
               var0.spawnEntity(var12);
            }

            return false;
         }
      }
   }

   protected void playDispenseSound(IBlockSource var1) {
      var1.getWorld().playEvent(1000, var1.getBlockPos(), 0);
   }

   protected void spawnDispenseParticles(IBlockSource var1, EnumFacing var2) {
      var1.getWorld().playEvent(2000, var1.getBlockPos(), this.getWorldEventDataFrom(var2));
   }

   private int getWorldEventDataFrom(EnumFacing var1) {
      return var1.getFrontOffsetX() + 1 + (var1.getFrontOffsetZ() + 1) * 3;
   }
}
