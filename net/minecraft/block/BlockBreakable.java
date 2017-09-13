package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

public class BlockBreakable extends Block {
   private final boolean ignoreSimilarity;

   protected BlockBreakable(Material var1, boolean var2) {
      this(var1, var2, var1.getMaterialMapColor());
   }

   protected BlockBreakable(Material var1, boolean var2, MapColor var3) {
      super(var1, var3);
      this.ignoreSimilarity = var2;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }
}
