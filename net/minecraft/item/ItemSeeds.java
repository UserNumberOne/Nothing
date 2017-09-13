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

   public ItemSeeds(Block var1, Block var2) {
      this.crops = var1;
      this.soilBlockID = var2;
      this.setCreativeTab(CreativeTabs.MATERIALS);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      IBlockState var10 = var3.getBlockState(var4);
      if (var6 == EnumFacing.UP && var2.canPlayerEdit(var4.offset(var6), var6, var1) && var10.getBlock().canSustainPlant(var10, var3, var4, EnumFacing.UP, this) && var3.isAirBlock(var4.up())) {
         var3.setBlockState(var4.up(), this.crops.getDefaultState());
         --var1.stackSize;
         return EnumActionResult.SUCCESS;
      } else {
         return EnumActionResult.FAIL;
      }
   }

   public EnumPlantType getPlantType(IBlockAccess var1, BlockPos var2) {
      return this.crops == Blocks.NETHER_WART ? EnumPlantType.Nether : EnumPlantType.Crop;
   }

   public IBlockState getPlant(IBlockAccess var1, BlockPos var2) {
      return this.crops.getDefaultState();
   }
}
