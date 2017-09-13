package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;

public class ItemLilyPad extends ItemColored {
   public ItemLilyPad(Block var1) {
      super(block, false);
   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      RayTraceResult raytraceresult = this.rayTrace(worldIn, playerIn, true);
      if (raytraceresult == null) {
         return new ActionResult(EnumActionResult.PASS, itemStackIn);
      } else {
         if (raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos blockpos = raytraceresult.getBlockPos();
            if (!worldIn.isBlockModifiable(playerIn, blockpos) || !playerIn.canPlayerEdit(blockpos.offset(raytraceresult.sideHit), raytraceresult.sideHit, itemStackIn)) {
               return new ActionResult(EnumActionResult.FAIL, itemStackIn);
            }

            BlockPos blockpos1 = blockpos.up();
            IBlockState iblockstate = worldIn.getBlockState(blockpos);
            if (iblockstate.getMaterial() == Material.WATER && ((Integer)iblockstate.getValue(BlockLiquid.LEVEL)).intValue() == 0 && worldIn.isAirBlock(blockpos1)) {
               BlockSnapshot blocksnapshot = BlockSnapshot.getBlockSnapshot(worldIn, blockpos1);
               worldIn.setBlockState(blockpos1, Blocks.WATERLILY.getDefaultState());
               if (ForgeEventFactory.onPlayerBlockPlace(playerIn, blocksnapshot, EnumFacing.UP, hand).isCanceled()) {
                  blocksnapshot.restore(true, false);
                  return new ActionResult(EnumActionResult.FAIL, itemStackIn);
               }

               worldIn.setBlockState(blockpos1, Blocks.WATERLILY.getDefaultState(), 11);
               if (!playerIn.capabilities.isCreativeMode) {
                  --itemStackIn.stackSize;
               }

               playerIn.addStat(StatList.getObjectUseStats(this));
               worldIn.playSound(playerIn, blockpos, SoundEvents.BLOCK_WATERLILY_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
               return new ActionResult(EnumActionResult.SUCCESS, itemStackIn);
            }
         }

         return new ActionResult(EnumActionResult.FAIL, itemStackIn);
      }
   }
}
