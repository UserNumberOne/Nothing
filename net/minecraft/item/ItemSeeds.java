package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemSeeds extends Item {
   private final Block crops;
   private final Block soilBlockID;

   public ItemSeeds(Block var1, Block var2) {
      this.crops = var1;
      this.soilBlockID = var2;
      this.setCreativeTab(CreativeTabs.MATERIALS);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      if (var6 == EnumFacing.UP && var2.canPlayerEdit(var4.offset(var6), var6, var1) && var3.getBlockState(var4).getBlock() == this.soilBlockID && var3.isAirBlock(var4.up())) {
         var3.setBlockState(var4.up(), this.crops.getDefaultState());
         --var1.stackSize;
         return EnumActionResult.SUCCESS;
      } else {
         return EnumActionResult.FAIL;
      }
   }
}
