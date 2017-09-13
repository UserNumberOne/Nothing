package net.minecraft.item;

import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.end.DragonFightManager;

public class ItemEndCrystal extends Item {
   public ItemEndCrystal() {
      this.setUnlocalizedName("end_crystal");
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      IBlockState var10 = var3.getBlockState(var4);
      if (var10.getBlock() != Blocks.OBSIDIAN && var10.getBlock() != Blocks.BEDROCK) {
         return EnumActionResult.FAIL;
      } else {
         BlockPos var11 = var4.up();
         if (!var2.canPlayerEdit(var11, var6, var1)) {
            return EnumActionResult.FAIL;
         } else {
            BlockPos var12 = var11.up();
            boolean var13 = !var3.isAirBlock(var11) && !var3.getBlockState(var11).getBlock().isReplaceable(var3, var11);
            var13 = var13 | (!var3.isAirBlock(var12) && !var3.getBlockState(var12).getBlock().isReplaceable(var3, var12));
            if (var13) {
               return EnumActionResult.FAIL;
            } else {
               double var14 = (double)var11.getX();
               double var16 = (double)var11.getY();
               double var18 = (double)var11.getZ();
               List var20 = var3.getEntitiesWithinAABBExcludingEntity((Entity)null, new AxisAlignedBB(var14, var16, var18, var14 + 1.0D, var16 + 2.0D, var18 + 1.0D));
               if (!var20.isEmpty()) {
                  return EnumActionResult.FAIL;
               } else {
                  if (!var3.isRemote) {
                     EntityEnderCrystal var21 = new EntityEnderCrystal(var3, (double)((float)var4.getX() + 0.5F), (double)(var4.getY() + 1), (double)((float)var4.getZ() + 0.5F));
                     var21.setShowBottom(false);
                     var3.spawnEntity(var21);
                     if (var3.provider instanceof WorldProviderEnd) {
                        DragonFightManager var22 = ((WorldProviderEnd)var3.provider).getDragonFightManager();
                        var22.respawnDragon();
                     }
                  }

                  --var1.stackSize;
                  return EnumActionResult.SUCCESS;
               }
            }
         }
      }
   }
}
