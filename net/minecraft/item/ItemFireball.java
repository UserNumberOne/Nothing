package net.minecraft.item;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;

public class ItemFireball extends Item {
   public ItemFireball() {
      this.setCreativeTab(CreativeTabs.MISC);
   }

   public EnumActionResult onItemUse(ItemStack itemstack, EntityPlayer entityhuman, World world, BlockPos blockposition, EnumHand enumhand, EnumFacing enumdirection, float f, float f1, float f2) {
      if (world.isRemote) {
         return EnumActionResult.SUCCESS;
      } else {
         blockposition = blockposition.offset(enumdirection);
         if (!entityhuman.canPlayerEdit(blockposition, enumdirection, itemstack)) {
            return EnumActionResult.FAIL;
         } else {
            if (world.getBlockState(blockposition).getMaterial() == Material.AIR) {
               if (CraftEventFactory.callBlockIgniteEvent(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), IgniteCause.FIREBALL, entityhuman).isCancelled()) {
                  if (!entityhuman.capabilities.isCreativeMode) {
                     --itemstack.stackSize;
                  }

                  return EnumActionResult.PASS;
               }

               world.playSound((EntityPlayer)null, blockposition, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, (itemRand.nextFloat() - itemRand.nextFloat()) * 0.2F + 1.0F);
               world.setBlockState(blockposition, Blocks.FIRE.getDefaultState());
            }

            if (!entityhuman.capabilities.isCreativeMode) {
               --itemstack.stackSize;
            }

            return EnumActionResult.SUCCESS;
         }
      }
   }
}
