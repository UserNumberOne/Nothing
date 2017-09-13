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
      this.key = var1;
      this.value = var2;
   }

   public Predicate getPredicate(BlockStateContainer var1) {
      final IProperty var2 = var1.getProperty(this.key);
      if (var2 == null) {
         throw new RuntimeException(this.toString() + ": Definition: " + var1 + " has no property: " + this.key);
      } else {
         String var3 = this.value;
         boolean var4 = !var3.isEmpty() && var3.charAt(0) == '!';
         if (var4) {
            var3 = var3.substring(1);
         }

         List var5 = SPLITTER.splitToList(var3);
         if (var5.isEmpty()) {
            throw new RuntimeException(this.toString() + ": has an empty value: " + this.value);
         } else {
            Predicate var6;
            if (var5.size() == 1) {
               var6 = this.makePredicate(var2, var3);
            } else {
               var6 = Predicates.or(Iterables.transform(var5, new Function() {
                  @Nullable
                  public Predicate apply(@Nullable String var1) {
                     return ConditionPropertyValue.this.makePredicate(var2, var1);
                  }
               }));
            }

            return var4 ? Predicates.not(var6) : var6;
         }
      }
   }

   private Predicate makePredicate(final IProperty var1, String var2) {
      final Optional var3 = var1.parseValue(var2);
      if (!var3.isPresent()) {
         throw new RuntimeException(this.toString() + ": has an unknown value: " + this.value);
      } else {
         return new Predicate() {
            public boolean apply(@Nullable IBlockState var1x) {
               return var1x != null && var1x.getValue(var1).equals(var3.get());
            }
         };
      }
   }

   public String toString() {
      return Objects.toStringHelper(this).add("key", this.key).add("value", this.value).toString();
   }
}
