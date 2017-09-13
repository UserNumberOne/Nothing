package net.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemRedstone extends Item {
   public ItemRedstone() {
      this.setCreativeTab(CreativeTabs.REDSTONE);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      boolean flag = worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos);
      BlockPos blockpos = flag ? pos : pos.offset(facing);
      if (playerIn.canPlayerEdit(blockpos, facing, stack) && worldIn.canBlockBePlaced(worldIn.getBlockState(blockpos).getBlock(), blockpos, false, facing, (Entity)null, stack) && Blocks.REDSTONE_WIRE.canPlaceBlockAt(worldIn, blockpos)) {
         --stack.stackSize;
         worldIn.setBlockState(blockpos, Blocks.REDSTONE_WIRE.getDefaultState());
         return EnumActionResult.SUCCESS;
      } else {
         return EnumActionResult.FAIL;
      }
   }
}
