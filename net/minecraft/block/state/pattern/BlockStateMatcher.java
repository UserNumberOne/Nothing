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

      // $FF: synthetic method
      public boolean apply(Object var1) {
         return this.apply((IBlockState)var1);
      }
   };
   private final BlockStateContainer blockstate;
   private final Map propertyPredicates = Maps.newHashMap();

   private BlockStateMatcher(BlockStateContainer var1) {
      this.blockstate = var1;
   }

   public static BlockStateMatcher forBlock(Block var0) {
      return new BlockStateMatcher(var0.getBlockState());
   }

   public boolean apply(@Nullable IBlockState var1) {
      if (var1 != null && var1.getBlock().equals(this.blockstate.getBlock())) {
         for(Entry var3 : this.propertyPredicates.entrySet()) {
            if (!this.matches(var1, (IProperty)var3.getKey(), (Predicate)var3.getValue())) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   protected boolean matches(IBlockState var1, IProperty var2, Predicate var3) {
      return var3.apply(var1.getValue(var2));
   }

   public BlockStateMatcher where(IProperty var1, Predicate var2) {
      if (!this.blockstate.getProperties().contains(var1)) {
         throw new IllegalArgumentException(this.blockstate + " cannot support property " + var1);
      } else {
         this.propertyPredicates.put(var1, var2);
         return this;
      }
   }

   // $FF: synthetic method
   public boolean apply(Object var1) {
      return this.apply((IBlockState)var1);
   }
}
