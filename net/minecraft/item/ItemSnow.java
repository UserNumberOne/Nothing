package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemSnow extends ItemBlock {
   public ItemSnow(Block var1) {
      super(block);
      this.setMaxDamage(0);
      this.setHasSubtypes(true);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      if (stack.stackSize != 0 && playerIn.canPlayerEdit(pos, facing, stack)) {
         IBlockState iblockstate = worldIn.getBlockState(pos);
         Block block = iblockstate.getBlock();
         BlockPos blockpos = pos;
         if ((facing != EnumFacing.UP || block != this.block) && !block.isReplaceable(worldIn, pos)) {
            blockpos = pos.offset(facing);
            iblockstate = worldIn.getBlockState(blockpos);
            block = iblockstate.getBlock();
         }

         if (block == this.block) {
            int i = ((Integer)iblockstate.getValue(BlockSnow.LAYERS)).intValue();
            if (i <= 7) {
               IBlockState iblockstate1 = iblockstate.withProperty(BlockSnow.LAYERS, Integer.valueOf(i + 1));
               AxisAlignedBB axisalignedbb = iblockstate1.getCollisionBoundingBox(worldIn, blockpos);
               if (axisalignedbb != Block.NULL_AABB && worldIn.checkNoEntityCollision(axisalignedbb.offset(blockpos)) && worldIn.setBlockState(blockpos, iblockstate1, 10)) {
                  SoundType soundtype = this.block.getSoundType(iblockstate1, worldIn, blockpos, playerIn);
                  worldIn.playSound(playerIn, blockpos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                  --stack.stackSize;
                  return EnumActionResult.SUCCESS;
               }
            }
         }

         return super.onItemUse(stack, playerIn, worldIn, blockpos, hand, facing, hitX, hitY, hitZ);
      } else {
         return EnumActionResult.FAIL;
      }
   }

   public int getMetadata(int var1) {
      return damage;
   }

   public boolean canPlaceBlockOnSide(World var1, BlockPos var2, EnumFacing var3, EntityPlayer var4, ItemStack var5) {
      IBlockState state = world.getBlockState(pos);
      return state.getBlock() == Blocks.SNOW_LAYER && ((Integer)state.getValue(BlockSnow.LAYERS)).intValue() <= 7 ? true : super.canPlaceBlockOnSide(world, pos, side, player, stack);
   }
}
