package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

public class ItemSeedFood extends ItemFood implements IPlantable {
   private final Block crops;
   private final Block soilId;

   public ItemSeedFood(int var1, float var2, Block var3, Block var4) {
      super(healAmount, saturation, false);
      this.crops = crops;
      this.soilId = soil;
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      IBlockState state = worldIn.getBlockState(pos);
      if (facing == EnumFacing.UP && playerIn.canPlayerEdit(pos.offset(facing), facing, stack) && state.getBlock().canSustainPlant(state, worldIn, pos, EnumFacing.UP, this) && worldIn.isAirBlock(pos.up())) {
         worldIn.setBlockState(pos.up(), this.crops.getDefaultState(), 11);
         --stack.stackSize;
         return EnumActionResult.SUCCESS;
      } else {
         return EnumActionResult.FAIL;
      }
   }

   public EnumPlantType getPlantType(IBlockAccess var1, BlockPos var2) {
      return EnumPlantType.Crop;
   }

   public IBlockState getPlant(IBlockAccess var1, BlockPos var2) {
      return this.crops.getDefaultState();
   }
}
