package net.minecraft.block.state.pattern;

import com.google.common.base.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

public class BlockMaterialMatcher implements Predicate {
   private final Material material;

   private BlockMaterialMatcher(Material var1) {
      this.material = var1;
   }

   public static BlockMaterialMatcher forMaterial(Material var0) {
      return new BlockMaterialMatcher(var0);
   }

   public boolean apply(@Nullable IBlockState var1) {
      return var1 != null && var1.getMaterial() == this.material;
   }
}
