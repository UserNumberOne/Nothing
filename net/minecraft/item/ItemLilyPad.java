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
import org.bukkit.craftbukkit.v1_10_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.block.BlockPlaceEvent;

public class ItemLilyPad extends ItemColored {
   public ItemLilyPad(Block var1) {
      super(var1, false);
   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      RayTraceResult var5 = this.rayTrace(var2, var3, true);
      if (var5 == null) {
         return new ActionResult(EnumActionResult.PASS, var1);
      } else {
         if (var5.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos var6 = var5.getBlockPos();
            if (!var2.isBlockModifiable(var3, var6) || !var3.canPlayerEdit(var6.offset(var5.sideHit), var5.sideHit, var1)) {
               return new ActionResult(EnumActionResult.FAIL, var1);
            }

            BlockPos var7 = var6.up();
            IBlockState var8 = var2.getBlockState(var6);
            if (var8.getMaterial() == Material.WATER && ((Integer)var8.getValue(BlockLiquid.LEVEL)).intValue() == 0 && var2.isAirBlock(var7)) {
               CraftBlockState var9 = CraftBlockState.getBlockState(var2, var7.getX(), var7.getY(), var7.getZ());
               var2.setBlockState(var7, Blocks.WATERLILY.getDefaultState(), 11);
               BlockPlaceEvent var10 = CraftEventFactory.callBlockPlaceEvent(var2, var3, var4, var9, var6.getX(), var6.getY(), var6.getZ());
               if (var10 == null || !var10.isCancelled() && var10.canBuild()) {
                  if (!var3.capabilities.isCreativeMode) {
                     --var1.stackSize;
                  }

                  var3.addStat(StatList.getObjectUseStats(this));
                  var2.playSound(var3, var6, SoundEvents.BLOCK_WATERLILY_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                  return new ActionResult(EnumActionResult.SUCCESS, var1);
               }

               var9.update(true, false);
               return new ActionResult(EnumActionResult.PASS, var1);
            }
         }

         return new ActionResult(EnumActionResult.FAIL, var1);
      }
   }
}
