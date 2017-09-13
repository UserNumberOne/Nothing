package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.hanging.HangingPlaceEvent;

public class ItemLead extends Item {
   public ItemLead() {
      this.setCreativeTab(CreativeTabs.TOOLS);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      Block var10 = var3.getBlockState(var4).getBlock();
      if (!(var10 instanceof BlockFence)) {
         return EnumActionResult.PASS;
      } else {
         if (!var3.isRemote) {
            attachToFence(var2, var3, var4);
         }

         return EnumActionResult.SUCCESS;
      }
   }

   public static boolean attachToFence(EntityPlayer var0, World var1, BlockPos var2) {
      EntityLeashKnot var3 = EntityLeashKnot.getKnotForPosition(var1, var2);
      boolean var4 = false;
      int var5 = var2.getX();
      int var6 = var2.getY();
      int var7 = var2.getZ();

      for(EntityLiving var10 : var1.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB((double)var5 - 7.0D, (double)var6 - 7.0D, (double)var7 - 7.0D, (double)var5 + 7.0D, (double)var6 + 7.0D, (double)var7 + 7.0D))) {
         if (var10.getLeashed() && var10.getLeashedToEntity() == var0) {
            if (var3 == null) {
               var3 = EntityLeashKnot.createKnot(var1, var2);
               HangingPlaceEvent var11 = new HangingPlaceEvent((Hanging)var3.getBukkitEntity(), var0 != null ? (Player)var0.getBukkitEntity() : null, var1.getWorld().getBlockAt(var5, var6, var7), BlockFace.SELF);
               var1.getServer().getPluginManager().callEvent(var11);
               if (var11.isCancelled()) {
                  var3.setDead();
                  return false;
               }
            }

            if (!CraftEventFactory.callPlayerLeashEntityEvent(var10, var3, var0).isCancelled()) {
               var10.setLeashedToEntity(var3, true);
               var4 = true;
            }
         }
      }

      return var4;
   }
}
