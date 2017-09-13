package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

public class ItemSeeds extends Item implements IPlantable {
   private final Block crops;
   private final Block soilBlockID;

   public ItemSeeds(Block crops, Block soil) {
      this.crops = crops;
      this.soilBlockID = soil;
      this.setCreativeTab(CreativeTabs.MATERIALS);
   }

   public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
      IBlockState state = worldIn.getBlockState(pos);
      if (facing == EnumFacing.UP && playerIn.canPlayerEdit(pos.offset(facing), facing, stack) && state.getBlock().canSustainPlant(state, worldIn, pos, EnumFacing.UP, this) && worldIn.isAirBlock(pos.up())) {
         worldIn.setBlockState(pos.up(), this.crops.getDefaultState());
         --stack.stackSize;
         return EnumActionResult.SUCCESS;
      } else {
         return EnumActionResult.FAIL;
      }
   }

   public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
      return this.crops == Blocks.NETHER_WART ? EnumPlantType.Nether : EnumPlantType.Crop;
   }

   public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
      return this.crops.getDefaultState();
   }
}
