package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import javax.annotation.Nullable;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ConditionAnd implements ICondition {
   private final Iterable conditions;

   public ConditionAnd(Iterable var1) {
      this.conditions = conditionsIn;
   }

   public Predicate getPredicate(final BlockStateContainer var1) {
      return Predicates.and(Iterables.transform(this.conditions, new Function() {
         @Nullable
         public Predicate apply(@Nullable ICondition var1x) {
            return p_apply_1_ == null ? null : p_apply_1_.getPredicate(blockState);
         }
      }));
   }
}
