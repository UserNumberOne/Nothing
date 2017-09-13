package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;

public class BlockEmptyDrops extends Block {
   public BlockEmptyDrops(Material var1) {
      super(materialIn);
   }

   public int quantityDropped(Random var1) {
      return 0;
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return null;
   }
}
