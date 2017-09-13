package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemSeedFood extends ItemFood {
   private final Block crops;
   private final Block soilId;

   public ItemSeedFood(int var1, float var2, Block var3, Block var4) {
      super(var1, var2, false);
      this.crops = var3;
      this.soilId = var4;
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      if (var6 == EnumFacing.UP && var2.canPlayerEdit(var4.offset(var6), var6, var1) && var3.getBlockState(var4).getBlock() == this.soilId && var3.isAirBlock(var4.up())) {
         var3.setBlockState(var4.up(), this.crops.getDefaultState(), 11);
         --var1.stackSize;
         return EnumActionResult.SUCCESS;
      } else {
         return EnumActionResult.FAIL;
      }
   }
}
