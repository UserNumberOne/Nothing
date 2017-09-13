package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.client.renderer.block.model.VariantList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Multipart {
   private final List selectors;
   private BlockStateContainer stateContainer;

   public Multipart(List var1) {
      this.selectors = selectorsIn;
   }

   public List getSelectors() {
      return this.selectors;
   }

   public Set getVariants() {
      Set set = Sets.newHashSet();

      for(Selector selector : this.selectors) {
         set.add(selector.getVariantList());
      }

      return set;
   }

   public void setStateContainer(BlockStateContainer var1) {
      this.stateContainer = stateContainerIn;
   }

   public BlockStateContainer getStateContainer() {
      return this.stateContainer;
   }

   public boolean equals(Object var1) {
      if (this == p_equals_1_) {
         return true;
      } else {
         if (p_equals_1_ instanceof Multipart) {
            Multipart multipart = (Multipart)p_equals_1_;
            if (this.selectors.equals(multipart.selectors)) {
               if (this.stateContainer == null) {
                  return multipart.stateContainer == null;
               }

               return this.stateContainer.equals(multipart.stateContainer);
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
         return new Multipart(this.getSelectors(p_deserialize_3_, p_deserialize_1_.getAsJsonArray()));
      }

      private List getSelectors(JsonDeserializationContext var1, JsonArray var2) {
         List list = Lists.newArrayList();

         for(JsonElement jsonelement : elements) {
            list.add((Selector)context.deserialize(jsonelement, Selector.class));
         }

         return list;
      }
   }
}
