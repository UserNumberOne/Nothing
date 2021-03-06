package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Multipart {
   private final List selectors;
   private BlockStateContainer stateContainer;

   public Multipart(List var1) {
      this.selectors = var1;
   }

   public List getSelectors() {
      return this.selectors;
   }

   public Set getVariants() {
      HashSet var1 = Sets.newHashSet();

      for(Selector var3 : this.selectors) {
         var1.add(var3.getVariantList());
      }

      return var1;
   }

   public void setStateContainer(BlockStateContainer var1) {
      this.stateContainer = var1;
   }

   public BlockStateContainer getStateContainer() {
      return this.stateContainer;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else {
         if (var1 instanceof Multipart) {
            Multipart var2 = (Multipart)var1;
            if (this.selectors.equals(var2.selectors)) {
               if (this.stateContainer == null) {
                  return var2.stateContainer == null;
               }

               return this.stateContainer.equals(var2.stateContainer);
            }
         }

         return false;
      }
   }

   public int hashCode() {
      return 31 * this.selectors.hashCode() + (this.stateContainer == null ? 0 : this.stateContainer.hashCode());
   }

   @SideOnly(Side.CLIENT)
   public static class Deserializer implements JsonDeserializer {
      public Multipart deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         return new Multipart(this.getSelectors(var3, var1.getAsJsonArray()));
      }

      private List getSelectors(JsonDeserializationContext var1, JsonArray var2) {
         ArrayList var3 = Lists.newArrayList();

         for(JsonElement var5 : var2) {
            var3.add((Selector)var1.deserialize(var5, Selector.class));
         }

         return var3;
      }
   }
}
