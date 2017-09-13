package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

public class BlockGravel extends BlockFalling {
   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      if (var3 > 3) {
         var3 = 3;
      }

      return var2.nextInt(10 - var3 * 3) == 0 ? Items.FLINT : Item.getItemFromBlock(this);
   }

   public MapColor getMapColor(IBlockState var1) {
      return MapColor.STONE;
   }
}
