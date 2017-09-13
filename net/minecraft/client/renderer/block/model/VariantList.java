package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VariantList {
   private final List variantList;

   public VariantList(List var1) {
      this.variantList = var1;
   }

   public List getVariantList() {
      return this.variantList;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (var1 instanceof VariantList) {
         VariantList var2 = (VariantList)var1;
         return this.variantList.equals(var2.variantList);
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
         ArrayList var4 = Lists.newArrayList();
         if (var1.isJsonArray()) {
            JsonArray var5 = var1.getAsJsonArray();
            if (var5.size() == 0) {
               throw new JsonParseException("Empty variant array");
            }

            for(JsonElement var7 : var5) {
               var4.add((Variant)var3.deserialize(var7, Variant.class));
            }
         } else {
            var4.add((Variant)var3.deserialize(var1, Variant.class));
         }

         return new VariantList(var4);
      }
   }
}
