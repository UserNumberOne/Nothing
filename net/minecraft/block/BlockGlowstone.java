package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;

public class BlockGlowstone extends Block {
   public BlockGlowstone(Material var1) {
      super(materialIn);
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
   }

   public int quantityDroppedWithBonus(int var1, Random var2) {
      return MathHelper.clamp(this.quantityDropped(random) + random.nextInt(fortune + 1), 1, 4);
   }

   public int quantityDropped(Random var1) {
      return 2 + random.nextInt(3);
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Items.GLOWSTONE_DUST;
   }

   public MapColor getMapColor(IBlockState var1) {
      return MapColor.SAND;
   }
}
