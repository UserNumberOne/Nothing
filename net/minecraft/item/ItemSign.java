package net.minecraft.item;

import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ItemSign extends Item {
   public ItemSign() {
      this.maxStackSize = 16;
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      IBlockState var10 = var3.getBlockState(var4);
      boolean var11 = var10.getBlock().isReplaceable(var3, var4);
      if (var6 != EnumFacing.DOWN && (var10.getMaterial().isSolid() || var11) && (!var11 || var6 == EnumFacing.UP)) {
         var4 = var4.offset(var6);
         if (var2.canPlayerEdit(var4, var6, var1) && Blocks.STANDING_SIGN.canPlaceBlockAt(var3, var4)) {
            if (var3.isRemote) {
               return EnumActionResult.SUCCESS;
            } else {
               var4 = var11 ? var4.down() : var4;
               if (var6 == EnumFacing.UP) {
                  int var12 = MathHelper.floor((double)((var2.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
                  var3.setBlockState(var4, Blocks.STANDING_SIGN.getDefaultState().withProperty(BlockStandingSign.ROTATION, Integer.valueOf(var12)), 11);
               } else {
                  var3.setBlockState(var4, Blocks.WALL_SIGN.getDefaultState().withProperty(BlockWallSign.FACING, var6), 11);
               }

               --var1.stackSize;
               TileEntity var15 = var3.getTileEntity(var4);
               if (var15 instanceof TileEntitySign && !ItemBlock.setTileEntityNBT(var3, var2, var4, var1)) {
                  var2.openEditSign((TileEntitySign)var15);
               }

               return EnumActionResult.SUCCESS;
            }
         } else {
            return EnumActionResult.FAIL;
         }
      } else {
         return EnumActionResult.FAIL;
      }
   }
}
