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
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_10_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.block.BlockPlaceEvent;

public class ItemLilyPad extends ItemColored {
   public ItemLilyPad(Block block) {
      super(block, false);
   }

   public ActionResult onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityhuman, EnumHand enumhand) {
      RayTraceResult movingobjectposition = this.rayTrace(world, entityhuman, true);
      if (movingobjectposition == null) {
         return new ActionResult(EnumActionResult.PASS, itemstack);
      } else {
         if (movingobjectposition.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos blockposition = movingobjectposition.getBlockPos();
            if (!world.isBlockModifiable(entityhuman, blockposition) || !entityhuman.canPlayerEdit(blockposition.offset(movingobjectposition.sideHit), movingobjectposition.sideHit, itemstack)) {
               return new ActionResult(EnumActionResult.FAIL, itemstack);
            }

            BlockPos blockposition1 = blockposition.up();
            IBlockState iblockdata = world.getBlockState(blockposition);
            if (iblockdata.getMaterial() == Material.WATER && ((Integer)iblockdata.getValue(BlockLiquid.LEVEL)).intValue() == 0 && world.isAirBlock(blockposition1)) {
               BlockState blockstate = CraftBlockState.getBlockState(world, blockposition1.getX(), blockposition1.getY(), blockposition1.getZ());
               world.setBlockState(blockposition1, Blocks.WATERLILY.getDefaultState(), 11);
               BlockPlaceEvent placeEvent = CraftEventFactory.callBlockPlaceEvent(world, entityhuman, enumhand, blockstate, blockposition.getX(), blockposition.getY(), blockposition.getZ());
               if (placeEvent == null || !placeEvent.isCancelled() && placeEvent.canBuild()) {
                  if (!entityhuman.capabilities.isCreativeMode) {
                     --itemstack.stackSize;
                  }

                  entityhuman.addStat(StatList.getObjectUseStats(this));
                  world.playSound(entityhuman, blockposition, SoundEvents.BLOCK_WATERLILY_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                  return new ActionResult(EnumActionResult.SUCCESS, itemstack);
               }

               blockstate.update(true, false);
               return new ActionResult(EnumActionResult.PASS, itemstack);
            }
         }

         return new ActionResult(EnumActionResult.FAIL, itemstack);
      }
   }
}
