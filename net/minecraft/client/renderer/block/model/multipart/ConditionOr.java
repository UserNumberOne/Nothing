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
public class ConditionOr implements ICondition {
   final Iterable conditions;

   public ConditionOr(Iterable var1) {
      this.conditions = var1;
   }

   public Predicate getPredicate(final BlockStateContainer var1) {
      return Predicates.or(Iterables.transform(this.conditions, new Function() {
         @Nullable
         public Predicate apply(@Nullable ICondition var1x) {
            return var1x == null ? null : var1x.getPredicate(var1);
         }
      }));
   }
}
