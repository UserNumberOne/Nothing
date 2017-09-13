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
      this.modelLocation = var1;
      this.rotation = var2;
      this.uvLock = var3;
      this.weight = var4;
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
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof Variant)) {
         return false;
      } else {
         Variant var2 = (Variant)var1;
         return this.modelLocation.equals(var2.modelLocation) && this.rotation == var2.rotation && this.uvLock == var2.uvLock && this.weight == var2.weight;
      }
   }

   public int hashCode() {
      int var1 = this.modelLocation.hashCode();
      var1 = 31 * var1 + this.rotation.hashCode();
      var1 = 31 * var1 + this.uvLock.hashCode();
      var1 = 31 * var1 + this.weight;
      return var1;
   }

   public IModel process(IModel var1) {
      return ModelProcessingHelper.uvlock(var1, this.isUvLock());
   }

   @SideOnly(Side.CLIENT)
   public static class Deserializer implements JsonDeserializer {
      public Variant deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         JsonObject var4 = var1.getAsJsonObject();
         String var5 = this.getStringModel(var4);
         ModelRotation var6 = this.parseModelRotation(var4);
         boolean var7 = this.parseUvLock(var4);
         int var8 = this.parseWeight(var4);
         return new Variant(this.getResourceLocationBlock(var5), var6, var7, var8);
      }

      private ResourceLocation getResourceLocationBlock(String var1) {
         ResourceLocation var2 = new ResourceLocation(var1);
         var2 = new ResourceLocation(var2.getResourceDomain(), "block/" + var2.getResourcePath());
         return var2;
      }

      private boolean parseUvLock(JsonObject var1) {
         return JsonUtils.getBoolean(var1, "uvlock", false);
      }

      protected ModelRotation parseModelRotation(JsonObject var1) {
         int var2 = JsonUtils.getInt(var1, "x", 0);
         int var3 = JsonUtils.getInt(var1, "y", 0);
         ModelRotation var4 = ModelRotation.getModelRotation(var2, var3);
         if (var4 == null) {
            throw new JsonParseException("Invalid BlockModelRotation x: " + var2 + ", y: " + var3);
         } else {
            return var4;
         }
      }

      protected String getStringModel(JsonObject var1) {
         return JsonUtils.getString(var1, "model");
      }

      protected int parseWeight(JsonObject var1) {
         int var2 = JsonUtils.getInt(var1, "weight", 1);
         if (var2 < 1) {
            throw new JsonParseException("Invalid weight " + var2 + " found, expected integer >= 1");
         } else {
            return var2;
         }
      }
   }
}
