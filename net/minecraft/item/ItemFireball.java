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

public class ItemFireball extends Item {
   public ItemFireball() {
      this.setCreativeTab(CreativeTabs.MISC);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      if (worldIn.isRemote) {
         return EnumActionResult.SUCCESS;
      } else {
         pos = pos.offset(facing);
         if (!playerIn.canPlayerEdit(pos, facing, stack)) {
            return EnumActionResult.FAIL;
         } else {
            if (worldIn.getBlockState(pos).getMaterial() == Material.AIR) {
               worldIn.playSound((EntityPlayer)null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, (itemRand.nextFloat() - itemRand.nextFloat()) * 0.2F + 1.0F);
               worldIn.setBlockState(pos, Blocks.FIRE.getDefaultState());
            }

            if (!playerIn.capabilities.isCreativeMode) {
               --stack.stackSize;
            }

            return EnumActionResult.SUCCESS;
         }
      }
   }
}
