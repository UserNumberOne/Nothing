package net.minecraft.block.state.pattern;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

public class BlockStateMatcher implements Predicate {
   public static final Predicate ANY = new Predicate() {
      public boolean apply(@Nullable IBlockState var1) {
         return true;
      }
   };
   private final BlockStateContainer blockstate;
   private final Map propertyPredicates = Maps.newHashMap();

   private BlockStateMatcher(BlockStateContainer var1) {
      this.blockstate = blockStateIn;
   }

   public static BlockStateMatcher forBlock(Block var0) {
      return new BlockStateMatcher(blockIn.getBlockState());
   }

   public boolean apply(@Nullable IBlockState var1) {
      if (p_apply_1_ != null && p_apply_1_.getBlock().equals(this.blockstate.getBlock())) {
         for(Entry entry : this.propertyPredicates.entrySet()) {
            if (!this.matches(p_apply_1_, (IProperty)entry.getKey(), (Predicate)entry.getValue())) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   protected boolean matches(IBlockState var1, IProperty var2, Predicate var3) {
      return predicate.apply(blockState.getValue(property));
   }

   public BlockStateMatcher where(IProperty var1, Predicate var2) {
      if (!this.blockstate.getProperties().contains(property)) {
         throw new IllegalArgumentException(this.blockstate + " cannot support property " + property);
      } else {
         this.propertyPredicates.put(property, is);
         return this;
      }
   }
}
