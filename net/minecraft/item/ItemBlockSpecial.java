package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBlockSpecial extends Item {
   private final Block block;

   public ItemBlockSpecial(Block var1) {
      this.block = var1;
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      IBlockState var10 = var3.getBlockState(var4);
      Block var11 = var10.getBlock();
      if (var11 == Blocks.SNOW_LAYER && ((Integer)var10.getValue(BlockSnow.LAYERS)).intValue() < 1) {
         var6 = EnumFacing.UP;
      } else if (!var11.isReplaceable(var3, var4)) {
         var4 = var4.offset(var6);
      }

      if (var2.canPlayerEdit(var4, var6, var1) && var1.stackSize != 0 && var3.canBlockBePlaced(this.block, var4, false, var6, (Entity)null, var1)) {
         IBlockState var12 = this.block.getStateForPlacement(var3, var4, var6, var7, var8, var9, 0, var2, var1);
         if (!var3.setBlockState(var4, var12, 11)) {
            return EnumActionResult.FAIL;
         } else {
            var12 = var3.getBlockState(var4);
            if (var12.getBlock() == this.block) {
               ItemBlock.setTileEntityNBT(var3, var2, var4, var1);
               var12.getBlock().onBlockPlacedBy(var3, var4, var12, var2, var1);
            }

            SoundType var13 = var12.getBlock().getSoundType(var12, var3, var4, var2);
            var3.playSound(var2, var4, var13.getPlaceSound(), SoundCategory.BLOCKS, (var13.getVolume() + 1.0F) / 2.0F, var13.getPitch() * 0.8F);
            --var1.stackSize;
            return EnumActionResult.SUCCESS;
         }
      } else {
         return EnumActionResult.FAIL;
      }
   }

   public Block getBlock() {
      return this.block;
   }
}
