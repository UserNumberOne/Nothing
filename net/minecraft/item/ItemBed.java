package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ItemBed extends Item {
   public ItemBed() {
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      if (var3.isRemote) {
         return EnumActionResult.SUCCESS;
      } else if (var6 != EnumFacing.UP) {
         return EnumActionResult.FAIL;
      } else {
         IBlockState var10 = var3.getBlockState(var4);
         Block var11 = var10.getBlock();
         boolean var12 = var11.isReplaceable(var3, var4);
         if (!var12) {
            var4 = var4.up();
         }

         int var13 = MathHelper.floor((double)(var2.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
         EnumFacing var14 = EnumFacing.getHorizontal(var13);
         BlockPos var15 = var4.offset(var14);
         if (var2.canPlayerEdit(var4, var6, var1) && var2.canPlayerEdit(var15, var6, var1)) {
            boolean var16 = var3.getBlockState(var15).getBlock().isReplaceable(var3, var15);
            boolean var17 = var12 || var3.isAirBlock(var4);
            boolean var18 = var16 || var3.isAirBlock(var15);
            if (var17 && var18 && var3.getBlockState(var4.down()).isFullyOpaque() && var3.getBlockState(var15.down()).isFullyOpaque()) {
               IBlockState var19 = Blocks.BED.getDefaultState().withProperty(BlockBed.OCCUPIED, Boolean.valueOf(false)).withProperty(BlockBed.FACING, var14).withProperty(BlockBed.PART, BlockBed.EnumPartType.FOOT);
               if (var3.setBlockState(var4, var19, 11)) {
                  IBlockState var20 = var19.withProperty(BlockBed.PART, BlockBed.EnumPartType.HEAD);
                  var3.setBlockState(var15, var20, 11);
               }

               SoundType var21 = var19.getBlock().getSoundType();
               var3.playSound((EntityPlayer)null, var4, var21.getPlaceSound(), SoundCategory.BLOCKS, (var21.getVolume() + 1.0F) / 2.0F, var21.getPitch() * 0.8F);
               --var1.stackSize;
               return EnumActionResult.SUCCESS;
            } else {
               return EnumActionResult.FAIL;
            }
         } else {
            return EnumActionResult.FAIL;
         }
      }
   }
}
