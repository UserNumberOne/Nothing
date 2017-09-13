package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;

public class BlockSeaLantern extends Block {
   public BlockSeaLantern(Material var1) {
      super(materialIn);
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
   }

   public int quantityDropped(Random var1) {
      return 2 + random.nextInt(2);
   }

   public int quantityDroppedWithBonus(int var1, Random var2) {
      return MathHelper.clamp(this.quantityDropped(random) + random.nextInt(fortune + 1), 1, 5);
   }

   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Items.PRISMARINE_CRYSTALS;
   }

   public MapColor getMapColor(IBlockState var1) {
      return MapColor.QUARTZ;
   }

   protected boolean canSilkHarvest() {
      return true;
   }
}
