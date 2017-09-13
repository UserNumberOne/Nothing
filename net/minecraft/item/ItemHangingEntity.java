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

   public ItemHangingEntity(Class oclass) {
      this.hangingEntityClass = oclass;
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public EnumActionResult onItemUse(ItemStack itemstack, EntityPlayer entityhuman, World world, BlockPos blockposition, EnumHand enumhand, EnumFacing enumdirection, float f, float f1, float f2) {
      BlockPos blockposition1 = blockposition.offset(enumdirection);
      if (enumdirection != EnumFacing.DOWN && enumdirection != EnumFacing.UP && entityhuman.canPlayerEdit(blockposition1, enumdirection, itemstack)) {
         EntityHanging entityhanging = this.createEntity(world, blockposition1, enumdirection);
         if (entityhanging != null && entityhanging.onValidSurface()) {
            if (!world.isRemote) {
               Player who = entityhuman == null ? null : (Player)entityhuman.getBukkitEntity();
               Block blockClicked = world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ());
               BlockFace blockFace = CraftBlock.notchToBlockFace(enumdirection);
               HangingPlaceEvent event = new HangingPlaceEvent((Hanging)entityhanging.getBukkitEntity(), who, blockClicked, blockFace);
               world.getServer().getPluginManager().callEvent(event);
               if (event.isCancelled()) {
                  return EnumActionResult.FAIL;
               }

               entityhanging.playPlaceSound();
               world.spawnEntity(entityhanging);
            }

            --itemstack.stackSize;
         }

         return EnumActionResult.SUCCESS;
      } else {
         return EnumActionResult.FAIL;
      }
   }

   @Nullable
   private EntityHanging createEntity(World world, BlockPos blockposition, EnumFacing enumdirection) {
      return (EntityHanging)(this.hangingEntityClass == EntityPainting.class ? new EntityPainting(world, blockposition, enumdirection) : (this.hangingEntityClass == EntityItemFrame.class ? new EntityItemFrame(world, blockposition, enumdirection) : null));
   }
}
