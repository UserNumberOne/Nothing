package net.minecraft.item;

import javax.annotation.Nullable;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_10_R1.block.CraftBlock;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.hanging.HangingPlaceEvent;

public class ItemHangingEntity extends Item {
   private final Class hangingEntityClass;

   public ItemHangingEntity(Class var1) {
      this.hangingEntityClass = var1;
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      BlockPos var10 = var4.offset(var6);
      if (var6 != EnumFacing.DOWN && var6 != EnumFacing.UP && var2.canPlayerEdit(var10, var6, var1)) {
         EntityHanging var11 = this.createEntity(var3, var10, var6);
         if (var11 != null && var11.onValidSurface()) {
            if (!var3.isRemote) {
               Player var12 = var2 == null ? null : (Player)var2.getBukkitEntity();
               Block var13 = var3.getWorld().getBlockAt(var4.getX(), var4.getY(), var4.getZ());
               BlockFace var14 = CraftBlock.notchToBlockFace(var6);
               HangingPlaceEvent var15 = new HangingPlaceEvent((Hanging)var11.getBukkitEntity(), var12, var13, var14);
               var3.getServer().getPluginManager().callEvent(var15);
               if (var15.isCancelled()) {
                  return EnumActionResult.FAIL;
               }

               var11.playPlaceSound();
               var3.spawnEntity(var11);
            }

            --var1.stackSize;
         }

         return EnumActionResult.SUCCESS;
      } else {
         return EnumActionResult.FAIL;
      }
   }

   @Nullable
   private EntityHanging createEntity(World var1, BlockPos var2, EnumFacing var3) {
      return (EntityHanging)(this.hangingEntityClass == EntityPainting.class ? new EntityPainting(var1, var2, var3) : (this.hangingEntityClass == EntityItemFrame.class ? new EntityItemFrame(var1, var2, var3) : null));
   }
}
