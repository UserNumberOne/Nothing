package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemSlab extends ItemBlock {
   private final BlockSlab singleSlab;
   private final BlockSlab doubleSlab;

   public ItemSlab(Block var1, BlockSlab var2, BlockSlab var3) {
      super(block);
      this.singleSlab = singleSlab;
      this.doubleSlab = doubleSlab;
      this.setMaxDamage(0);
      this.setHasSubtypes(true);
   }

   public int getMetadata(int var1) {
      return damage;
   }

   public String getUnlocalizedName(ItemStack var1) {
      return this.singleSlab.getUnlocalizedName(stack.getMetadata());
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      if (stack.stackSize != 0 && playerIn.canPlayerEdit(pos.offset(facing), facing, stack)) {
         Comparable comparable = this.singleSlab.getTypeForItem(stack);
         IBlockState iblockstate = worldIn.getBlockState(pos);
         if (iblockstate.getBlock() == this.singleSlab) {
            IProperty iproperty = this.singleSlab.getVariantProperty();
            Comparable comparable1 = iblockstate.getValue(iproperty);
            BlockSlab.EnumBlockHalf blockslab$enumblockhalf = (BlockSlab.EnumBlockHalf)iblockstate.getValue(BlockSlab.HALF);
            if ((facing == EnumFacing.UP && blockslab$enumblockhalf == BlockSlab.EnumBlockHalf.BOTTOM || facing == EnumFacing.DOWN && blockslab$enumblockhalf == BlockSlab.EnumBlockHalf.TOP) && comparable1 == comparable) {
               IBlockState iblockstate1 = this.makeState(iproperty, comparable1);
               AxisAlignedBB axisalignedbb = iblockstate1.getCollisionBoundingBox(worldIn, pos);
               if (axisalignedbb != Block.NULL_AABB && worldIn.checkNoEntityCollision(axisalignedbb.offset(pos)) && worldIn.setBlockState(pos, iblockstate1, 11)) {
                  SoundType soundtype = this.doubleSlab.getSoundType(iblockstate1, worldIn, pos, playerIn);
                  worldIn.playSound(playerIn, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                  --stack.stackSize;
               }

               return EnumActionResult.SUCCESS;
            }
         }

         return this.tryPlace(playerIn, stack, worldIn, pos.offset(facing), comparable) ? EnumActionResult.SUCCESS : super.onItemUse(stack, playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
      } else {
         return EnumActionResult.FAIL;
      }
   }

   @SideOnly(Side.CLIENT)
   public boolean canPlaceBlockOnSide(World var1, BlockPos var2, EnumFacing var3, EntityPlayer var4, ItemStack var5) {
      BlockPos blockpos = pos;
      IProperty iproperty = this.singleSlab.getVariantProperty();
      Comparable comparable = this.singleSlab.getTypeForItem(stack);
      IBlockState iblockstate = worldIn.getBlockState(pos);
      if (iblockstate.getBlock() == this.singleSlab) {
         boolean flag = iblockstate.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP;
         if ((side == EnumFacing.UP && !flag || side == EnumFacing.DOWN && flag) && comparable == iblockstate.getValue(iproperty)) {
            return true;
         }
      }

      pos = pos.offset(side);
      IBlockState iblockstate1 = worldIn.getBlockState(pos);
      return iblockstate1.getBlock() == this.singleSlab && comparable == iblockstate1.getValue(iproperty) ? true : super.canPlaceBlockOnSide(worldIn, blockpos, side, player, stack);
   }

   private boolean tryPlace(EntityPlayer var1, ItemStack var2, World var3, BlockPos var4, Object var5) {
      IBlockState iblockstate = worldIn.getBlockState(pos);
      if (iblockstate.getBlock() == this.singleSlab) {
         Comparable comparable = iblockstate.getValue(this.singleSlab.getVariantProperty());
         if (comparable == itemSlabType) {
            IBlockState iblockstate1 = this.makeState(this.singleSlab.getVariantProperty(), comparable);
            AxisAlignedBB axisalignedbb = iblockstate1.getCollisionBoundingBox(worldIn, pos);
            if (axisalignedbb != Block.NULL_AABB && worldIn.checkNoEntityCollision(axisalignedbb.offset(pos)) && worldIn.setBlockState(pos, iblockstate1, 11)) {
               SoundType soundtype = this.doubleSlab.getSoundType(iblockstate1, worldIn, pos, player);
               worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
               --stack.stackSize;
            }

            return true;
         }
      }

      return false;
   }

   protected IBlockState makeState(IProperty var1, Comparable var2) {
      return this.doubleSlab.getDefaultState().withProperty(p_185055_1_, p_185055_2_);
   }
}
