package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ConditionPropertyValue implements ICondition {
   private static final Splitter SPLITTER = Splitter.on('|').omitEmptyStrings();
   private final String key;
   private final String value;

   public ConditionPropertyValue(String var1, String var2) {
      this.key = keyIn;
      this.value = valueIn;
   }

   public Predicate getPredicate(BlockStateContainer var1) {
      final IProperty iproperty = blockState.getProperty(this.key);
      if (iproperty == null) {
         throw new RuntimeException(this.toString() + ": Definition: " + blockState + " has no property: " + this.key);
      } else {
         String s = this.value;
         boolean flag = !s.isEmpty() && s.charAt(0) == '!';
         if (flag) {
            s = s.substring(1);
         }

         List list = SPLITTER.splitToList(s);
         if (list.isEmpty()) {
            throw new RuntimeException(this.toString() + ": has an empty value: " + this.value);
         } else {
            Predicate predicate;
            if (list.size() == 1) {
               predicate = this.makePredicate(iproperty, s);
            } else {
               predicate = Predicates.or(Iterables.transform(list, new Function() {
                  @Nullable
                  public Predicate apply(@Nullable String var1) {
                     return ConditionPropertyValue.this.makePredicate(iproperty, p_apply_1_);
                  }
               }));
            }

            return flag ? Predicates.not(predicate) : predicate;
         }
      }
   }

   private Predicate makePredicate(final IProperty var1, String var2) {
      final Optional optional = property.parseValue(valueIn);
      if (!optional.isPresent()) {
         throw new RuntimeException(this.toString() + ": has an unknown value: " + this.value);
      } else {
         return new Predicate() {
            public boolean apply(@Nullable IBlockState var1x) {
               return p_apply_1_ != null && p_apply_1_.getValue(property).equals(optional.get());
            }
         };
      }
   }

   public String toString() {
      return Objects.toStringHelper(this).add("key", this.key).add("value", this.value).toString();
   }
}
