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

public class ItemFlintAndSteel extends Item {
   public ItemFlintAndSteel() {
      this.maxStackSize = 1;
      this.setMaxDamage(64);
      this.setCreativeTab(CreativeTabs.TOOLS);
   }

   public EnumActionResult onItemUse(ItemStack itemstack, EntityPlayer entityhuman, World world, BlockPos blockposition, EnumHand enumhand, EnumFacing enumdirection, float f, float f1, float f2) {
      blockposition = blockposition.offset(enumdirection);
      if (!entityhuman.canPlayerEdit(blockposition, enumdirection, itemstack)) {
         return EnumActionResult.FAIL;
      } else {
         if (world.getBlockState(blockposition).getMaterial() == Material.AIR) {
            if (CraftEventFactory.callBlockIgniteEvent(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), IgniteCause.FLINT_AND_STEEL, entityhuman).isCancelled()) {
               itemstack.damageItem(1, entityhuman);
               return EnumActionResult.PASS;
            }

            world.playSound(entityhuman, blockposition, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, itemRand.nextFloat() * 0.4F + 0.8F);
            world.setBlockState(blockposition, Blocks.FIRE.getDefaultState(), 11);
         }

         itemstack.damageItem(1, entityhuman);
         return EnumActionResult.SUCCESS;
      }
   }
}
