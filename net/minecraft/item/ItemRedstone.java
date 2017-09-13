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
      boolean var10 = var3.getBlockState(var4).getBlock().isReplaceable(var3, var4);
      BlockPos var11 = var10 ? var4 : var4.offset(var6);
      if (var2.canPlayerEdit(var11, var6, var1) && var3.canBlockBePlaced(var3.getBlockState(var11).getBlock(), var11, false, var6, (Entity)null, var1) && Blocks.REDSTONE_WIRE.canPlaceBlockAt(var3, var11)) {
         --var1.stackSize;
         var3.setBlockState(var11, Blocks.REDSTONE_WIRE.getDefaultState());
         return EnumActionResult.SUCCESS;
      } else {
         return EnumActionResult.FAIL;
      }
   }
}
