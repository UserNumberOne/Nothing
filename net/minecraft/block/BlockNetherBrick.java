package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;

public class BlockNetherBrick extends Block {
   public BlockNetherBrick() {
      super(Material.ROCK);
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
   }

   public MapColor getMapColor(IBlockState var1) {
      return MapColor.NETHERRACK;
   }
}
