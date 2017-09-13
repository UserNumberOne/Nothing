package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.List;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VariantList {
   private final List variantList;

   public VariantList(List var1) {
      this.variantList = variantListIn;
   }

   public List getVariantList() {
      return this.variantList;
   }

   public boolean equals(Object var1) {
      if (this == p_equals_1_) {
         return true;
      } else if (p_equals_1_ instanceof VariantList) {
         VariantList variantlist = (VariantList)p_equals_1_;
         return this.variantList.equals(variantlist.variantList);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.variantList.hashCode();
   }

   @SideOnly(Side.CLIENT)
   public static class Deserializer implements JsonDeserializer {
      public VariantList deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         List list = Lists.newArrayList();
         if (p_deserialize_1_.isJsonArray()) {
            JsonArray jsonarray = p_deserialize_1_.getAsJsonArray();
            if (jsonarray.size() == 0) {
               throw new JsonParseException("Empty variant array");
            }

            for(JsonElement jsonelement : jsonarray) {
               list.add((Variant)p_deserialize_3_.deserialize(jsonelement, Variant.class));
            }
         } else {
            list.add((Variant)p_deserialize_3_.deserialize(p_deserialize_1_, Variant.class));
         }

         return new VariantList(list);
      }
   }
}
