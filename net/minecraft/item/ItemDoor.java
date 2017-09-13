package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemDoor extends Item {
   private final Block block;

   public ItemDoor(Block var1) {
      this.block = var1;
      this.setCreativeTab(CreativeTabs.REDSTONE);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      if (var6 != EnumFacing.UP) {
         return EnumActionResult.FAIL;
      } else {
         IBlockState var10 = var3.getBlockState(var4);
         Block var11 = var10.getBlock();
         if (!var11.isReplaceable(var3, var4)) {
            var4 = var4.offset(var6);
         }

         if (var2.canPlayerEdit(var4, var6, var1) && this.block.canPlaceBlockAt(var3, var4)) {
            EnumFacing var12 = EnumFacing.fromAngle((double)var2.rotationYaw);
            int var13 = var12.getFrontOffsetX();
            int var14 = var12.getFrontOffsetZ();
            boolean var15 = var13 < 0 && var9 < 0.5F || var13 > 0 && var9 > 0.5F || var14 < 0 && var7 > 0.5F || var14 > 0 && var7 < 0.5F;
            placeDoor(var3, var4, var12, this.block, var15);
            SoundType var16 = var3.getBlockState(var4).getBlock().getSoundType(var3.getBlockState(var4), var3, var4, var2);
            var3.playSound(var2, var4, var16.getPlaceSound(), SoundCategory.BLOCKS, (var16.getVolume() + 1.0F) / 2.0F, var16.getPitch() * 0.8F);
            --var1.stackSize;
            return EnumActionResult.SUCCESS;
         } else {
            return EnumActionResult.FAIL;
         }
      }
   }

   public static void placeDoor(World var0, BlockPos var1, EnumFacing var2, Block var3, boolean var4) {
      BlockPos var5 = var1.offset(var2.rotateY());
      BlockPos var6 = var1.offset(var2.rotateYCCW());
      int var7 = (var0.getBlockState(var6).isNormalCube() ? 1 : 0) + (var0.getBlockState(var6.up()).isNormalCube() ? 1 : 0);
      int var8 = (var0.getBlockState(var5).isNormalCube() ? 1 : 0) + (var0.getBlockState(var5.up()).isNormalCube() ? 1 : 0);
      boolean var9 = var0.getBlockState(var6).getBlock() == var3 || var0.getBlockState(var6.up()).getBlock() == var3;
      boolean var10 = var0.getBlockState(var5).getBlock() == var3 || var0.getBlockState(var5.up()).getBlock() == var3;
      if ((!var9 || var10) && var8 <= var7) {
         if (var10 && !var9 || var8 < var7) {
            var4 = false;
         }
      } else {
         var4 = true;
      }

      BlockPos var11 = var1.up();
      boolean var12 = var0.isBlockPowered(var1) || var0.isBlockPowered(var11);
      IBlockState var13 = var3.getDefaultState().withProperty(BlockDoor.FACING, var2).withProperty(BlockDoor.HINGE, var4 ? BlockDoor.EnumHingePosition.RIGHT : BlockDoor.EnumHingePosition.LEFT).withProperty(BlockDoor.POWERED, Boolean.valueOf(var12)).withProperty(BlockDoor.OPEN, Boolean.valueOf(var12));
      var0.setBlockState(var1, var13.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.LOWER), 2);
      var0.setBlockState(var11, var13.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER), 2);
      var0.notifyNeighborsOfStateChange(var1, var3);
      var0.notifyNeighborsOfStateChange(var11, var3);
   }
}
