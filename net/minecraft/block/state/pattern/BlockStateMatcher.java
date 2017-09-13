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
      public boolean apply(@Nullable IBlockState p_apply_1_) {
         return true;
      }
   };
   private final BlockStateContainer blockstate;
   private final Map propertyPredicates = Maps.newHashMap();

   private BlockStateMatcher(BlockStateContainer blockStateIn) {
      this.blockstate = blockStateIn;
   }

   public static BlockStateMatcher forBlock(Block blockIn) {
      return new BlockStateMatcher(blockIn.getBlockState());
   }

   public boolean apply(@Nullable IBlockState p_apply_1_) {
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

   protected boolean matches(IBlockState blockState, IProperty property, Predicate predicate) {
      return predicate.apply(blockState.getValue(property));
   }

   public BlockStateMatcher where(IProperty property, Predicate is) {
      if (!this.blockstate.getProperties().contains(property)) {
         throw new IllegalArgumentException(this.blockstate + " cannot support property " + property);
      } else {
         this.propertyPredicates.put(property, is);
         return this;
      }
   }
}
