package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;

public class BlockGlass extends BlockBreakable {
   public BlockGlass(Material var1, boolean var2) {
      super(var1, var2);
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
   }

   public int quantityDropped(Random var1) {
      return 0;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   protected boolean canSilkHarvest() {
      return true;
   }
}
