package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ISmartVariant;
import net.minecraftforge.client.model.ModelProcessingHelper;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Variant implements ISmartVariant {
   private final ResourceLocation modelLocation;
   private final ModelRotation rotation;
   private final boolean uvLock;
   private final int weight;

   public Variant(ResourceLocation var1, ModelRotation var2, boolean var3, int var4) {
      this.modelLocation = modelLocationIn;
      this.rotation = rotationIn;
      this.uvLock = uvLockIn;
      this.weight = weightIn;
   }

   public ResourceLocation getModelLocation() {
      return this.modelLocation;
   }

   /** @deprecated */
   @Deprecated
   public ModelRotation getRotation() {
      return this.rotation;
   }

   public IModelState getState() {
      return this.rotation;
   }

   public boolean isUvLock() {
      return this.uvLock;
   }

   public int getWeight() {
      return this.weight;
   }

   public String toString() {
      return "Variant{modelLocation=" + this.modelLocation + ", rotation=" + this.rotation + ", uvLock=" + this.uvLock + ", weight=" + this.weight + '}';
   }

   public boolean equals(Object var1) {
      if (this == p_equals_1_) {
         return true;
      } else if (!(p_equals_1_ instanceof Variant)) {
         return false;
      } else {
         Variant variant = (Variant)p_equals_1_;
         return this.modelLocation.equals(variant.modelLocation) && this.rotation == variant.rotation && this.uvLock == variant.uvLock && this.weight == variant.weight;
      }
   }

   public int hashCode() {
      int i = this.modelLocation.hashCode();
      i = 31 * i + this.rotation.hashCode();
      i = 31 * i + this.uvLock.hashCode();
      i = 31 * i + this.weight;
      return i;
   }

   public IModel process(IModel var1) {
      return ModelProcessingHelper.uvlock(base, this.isUvLock());
   }

   @SideOnly(Side.CLIENT)
   public static class Deserializer implements JsonDeserializer {
      public Variant deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
         String s = this.getStringModel(jsonobject);
         ModelRotation modelrotation = this.parseModelRotation(jsonobject);
         boolean flag = this.parseUvLock(jsonobject);
         int i = this.parseWeight(jsonobject);
         return new Variant(this.getResourceLocationBlock(s), modelrotation, flag, i);
      }

      private ResourceLocation getResourceLocationBlock(String var1) {
         ResourceLocation resourcelocation = new ResourceLocation(p_188041_1_);
         resourcelocation = new ResourceLocation(resourcelocation.getResourceDomain(), "block/" + resourcelocation.getResourcePath());
         return resourcelocation;
      }

      private boolean parseUvLock(JsonObject var1) {
         return JsonUtils.getBoolean(json, "uvlock", false);
      }

      protected ModelRotation parseModelRotation(JsonObject var1) {
         int i = JsonUtils.getInt(json, "x", 0);
         int j = JsonUtils.getInt(json, "y", 0);
         ModelRotation modelrotation = ModelRotation.getModelRotation(i, j);
         if (modelrotation == null) {
            throw new JsonParseException("Invalid BlockModelRotation x: " + i + ", y: " + j);
         } else {
            return modelrotation;
         }
      }

      protected String getStringModel(JsonObject var1) {
         return JsonUtils.getString(json, "model");
      }

      protected int parseWeight(JsonObject var1) {
         int i = JsonUtils.getInt(json, "weight", 1);
         if (i < 1) {
            throw new JsonParseException("Invalid weight " + i + " found, expected integer >= 1");
         } else {
            return i;
         }
      }
   }
}
