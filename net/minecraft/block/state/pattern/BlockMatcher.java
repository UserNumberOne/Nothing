package net.minecraft.block.state.pattern;

import com.google.common.base.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

public class BlockMatcher implements Predicate {
   private final Block block;

   private BlockMatcher(Block var1) {
      this.block = var1;
   }

   public static BlockMatcher forBlock(Block var0) {
      return new BlockMatcher(var0);
   }

   public boolean apply(@Nullable IBlockState var1) {
      return var1 != null && var1.getBlock() == this.block;
   }
}
