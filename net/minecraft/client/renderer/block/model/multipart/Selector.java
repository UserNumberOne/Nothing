package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.client.renderer.block.model.VariantList;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Selector {
   private final ICondition condition;
   private final VariantList variantList;

   public Selector(ICondition var1, VariantList var2) {
      if (var1 == null) {
         throw new IllegalArgumentException("Missing condition for selector");
      } else if (var2 == null) {
         throw new IllegalArgumentException("Missing variant for selector");
      } else {
         this.condition = var1;
         this.variantList = var2;
      }
   }

   public VariantList getVariantList() {
      return this.variantList;
   }

   public Predicate getPredicate(BlockStateContainer var1) {
      return this.condition.getPredicate(var1);
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else {
         if (var1 instanceof Selector) {
            Selector var2 = (Selector)var1;
            if (this.condition.equals(var2.condition)) {
               return this.variantList.equals(var2.variantList);
            }
         }

         return false;
      }
   }

   public int hashCode() {
      return 31 * this.condition.hashCode() + this.variantList.hashCode();
   }

   @SideOnly(Side.CLIENT)
   public static class Deserializer implements JsonDeserializer {
      private static final Function FUNCTION_OR_AND = new Function() {
         @Nullable
         public ICondition apply(@Nullable JsonElement var1) {
            return var1 == null ? null : Selector.Deserializer.getOrAndCondition(var1.getAsJsonObject());
         }
      };
      private static final Function FUNCTION_PROPERTY_VALUE = new Function() {
         @Nullable
         public ICondition apply(@Nullable Entry var1) {
            return var1 == null ? null : Selector.Deserializer.makePropertyValue(var1);
         }
      };

      public Selector deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         JsonObject var4 = var1.getAsJsonObject();
         return new Selector(this.getWhenCondition(var4), (VariantList)var3.deserialize(var4.get("apply"), VariantList.class));
      }

      private ICondition getWhenCondition(JsonObject var1) {
         return var1.has("when") ? getOrAndCondition(JsonUtils.getJsonObject(var1, "when")) : ICondition.TRUE;
      }

      @VisibleForTesting
      static ICondition getOrAndCondition(JsonObject var0) {
         Set var1 = var0.entrySet();
         if (var1.isEmpty()) {
            throw new JsonParseException("No elements found in selector");
         } else {
            return (ICondition)(var1.size() == 1 ? (var0.has("OR") ? new ConditionOr(Iterables.transform(JsonUtils.getJsonArray(var0, "OR"), FUNCTION_OR_AND)) : (var0.has("AND") ? new ConditionAnd(Iterables.transform(JsonUtils.getJsonArray(var0, "AND"), FUNCTION_OR_AND)) : makePropertyValue((Entry)var1.iterator().next()))) : new ConditionAnd(Iterables.transform(var1, FUNCTION_PROPERTY_VALUE)));
         }
      }

      private static ConditionPropertyValue makePropertyValue(Entry var0) {
         return new ConditionPropertyValue((String)var0.getKey(), ((JsonElement)var0.getValue()).getAsString());
      }
   }
}
