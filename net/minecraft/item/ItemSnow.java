package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemSnow extends ItemBlock {
   public ItemSnow(Block var1) {
      super(var1);
      this.setMaxDamage(0);
      this.setHasSubtypes(true);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      if (var1.stackSize != 0 && var2.canPlayerEdit(var4, var6, var1)) {
         IBlockState var10 = var3.getBlockState(var4);
         Block var11 = var10.getBlock();
         BlockPos var12 = var4;
         if ((var6 != EnumFacing.UP || var11 != this.block) && !var11.isReplaceable(var3, var4)) {
            var12 = var4.offset(var6);
            var10 = var3.getBlockState(var12);
            var11 = var10.getBlock();
         }

         if (var11 == this.block) {
            int var13 = ((Integer)var10.getValue(BlockSnow.LAYERS)).intValue();
            if (var13 <= 7) {
               IBlockState var14 = var10.withProperty(BlockSnow.LAYERS, Integer.valueOf(var13 + 1));
               AxisAlignedBB var15 = var14.getCollisionBoundingBox(var3, var12);
               if (var15 != Block.NULL_AABB && var3.checkNoEntityCollision(var15.offset(var12)) && var3.setBlockState(var12, var14, 10)) {
                  SoundType var16 = this.block.getSoundType();
                  var3.playSound(var2, var12, var16.getPlaceSound(), SoundCategory.BLOCKS, (var16.getVolume() + 1.0F) / 2.0F, var16.getPitch() * 0.8F);
                  --var1.stackSize;
                  return EnumActionResult.SUCCESS;
               }
            }
         }

         return super.onItemUse(var1, var2, var3, var12, var5, var6, var7, var8, var9);
      } else {
         return EnumActionResult.FAIL;
      }
   }

   public int getMetadata(int var1) {
      return var1;
   }
}
