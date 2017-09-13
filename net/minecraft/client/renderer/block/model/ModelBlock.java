package net.minecraft.client.renderer.block.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class ModelBlock {
   private static final Logger LOGGER = LogManager.getLogger();
   @VisibleForTesting
   static final Gson SERIALIZER = (new GsonBuilder()).registerTypeAdapter(ModelBlock.class, new ModelBlock.Deserializer()).registerTypeAdapter(BlockPart.class, new BlockPart.Deserializer()).registerTypeAdapter(BlockPartFace.class, new BlockPartFace.Deserializer()).registerTypeAdapter(BlockFaceUV.class, new BlockFaceUV.Deserializer()).registerTypeAdapter(ItemTransformVec3f.class, new ItemTransformVec3f.Deserializer()).registerTypeAdapter(ItemCameraTransforms.class, new ItemCameraTransforms.Deserializer()).registerTypeAdapter(ItemOverride.class, new ItemOverride.Deserializer()).create();
   private final List elements;
   private final boolean gui3d;
   public final boolean ambientOcclusion;
   private final ItemCameraTransforms cameraTransforms;
   private final List overrides;
   public String name = "";
   @VisibleForTesting
   public final Map textures;
   @VisibleForTesting
   public ModelBlock parent;
   @VisibleForTesting
   protected ResourceLocation parentLocation;

   public static ModelBlock deserialize(Reader var0) {
      return (ModelBlock)JsonUtils.gsonDeserialize(SERIALIZER, var0, ModelBlock.class, false);
   }

   public static ModelBlock deserialize(String var0) {
      return deserialize(new StringReader(var0));
   }

   public ModelBlock(@Nullable ResourceLocation var1, List var2, Map var3, boolean var4, boolean var5, ItemCameraTransforms var6, List var7) {
      this.elements = var2;
      this.ambientOcclusion = var4;
      this.gui3d = var5;
      this.textures = var3;
      this.parentLocation = var1;
      this.cameraTransforms = var6;
      this.overrides = var7;
   }

   public List getElements() {
      return this.elements.isEmpty() && this.hasParent() ? this.parent.getElements() : this.elements;
   }

   private boolean hasParent() {
      return this.parent != null;
   }

   public boolean isAmbientOcclusion() {
      return this.hasParent() ? this.parent.isAmbientOcclusion() : this.ambientOcclusion;
   }

   public boolean isGui3d() {
      return this.gui3d;
   }

   public boolean isResolved() {
      return this.parentLocation == null || this.parent != null && this.parent.isResolved();
   }

   public void getParentFromMap(Map var1) {
      if (this.parentLocation != null) {
         this.parent = (ModelBlock)var1.get(this.parentLocation);
      }

   }

   public Collection getOverrideLocations() {
      HashSet var1 = Sets.newHashSet();

      for(ItemOverride var3 : this.overrides) {
         var1.add(var3.getLocation());
      }

      return var1;
   }

   public List getOverrides() {
      return this.overrides;
   }

   public ItemOverrideList createOverrides() {
      return this.overrides.isEmpty() ? ItemOverrideList.NONE : new ItemOverrideList(this.overrides);
   }

   public boolean isTexturePresent(String var1) {
      return !"missingno".equals(this.resolveTextureName(var1));
   }

   public String resolveTextureName(String var1) {
      if (!this.startsWithHash(var1)) {
         var1 = '#' + var1;
      }

      return this.resolveTextureName(var1, new ModelBlock.Bookkeep(this));
   }

   private String resolveTextureName(String var1, ModelBlock.Bookkeep var2) {
      if (this.startsWithHash(var1)) {
         if (this == var2.modelExt) {
            LOGGER.warn("Unable to resolve texture due to upward reference: {} in {}", new Object[]{var1, this.name});
            return "missingno";
         } else {
            String var3 = (String)this.textures.get(var1.substring(1));
            if (var3 == null && this.hasParent()) {
               var3 = this.parent.resolveTextureName(var1, var2);
            }

            var2.modelExt = this;
            if (var3 != null && this.startsWithHash(var3)) {
               var3 = var2.model.resolveTextureName(var3, var2);
            }

            return var3 != null && !this.startsWithHash(var3) ? var3 : "missingno";
         }
      } else {
         return var1;
      }
   }

   private boolean startsWithHash(String var1) {
      return var1.charAt(0) == '#';
   }

   @Nullable
   public ResourceLocation getParentLocation() {
      return this.parentLocation;
   }

   public ModelBlock getRootModel() {
      return this.hasParent() ? this.parent.getRootModel() : this;
   }

   public ItemCameraTransforms getAllTransforms() {
      ItemTransformVec3f var1 = this.getTransform(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND);
      ItemTransformVec3f var2 = this.getTransform(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND);
      ItemTransformVec3f var3 = this.getTransform(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND);
      ItemTransformVec3f var4 = this.getTransform(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND);
      ItemTransformVec3f var5 = this.getTransform(ItemCameraTransforms.TransformType.HEAD);
      ItemTransformVec3f var6 = this.getTransform(ItemCameraTransforms.TransformType.GUI);
      ItemTransformVec3f var7 = this.getTransform(ItemCameraTransforms.TransformType.GROUND);
      ItemTransformVec3f var8 = this.getTransform(ItemCameraTransforms.TransformType.FIXED);
      return new ItemCameraTransforms(var1, var2, var3, var4, var5, var6, var7, var8);
   }

   private ItemTransformVec3f getTransform(ItemCameraTransforms.TransformType var1) {
      return this.parent != null && !this.cameraTransforms.hasCustomTransform(var1) ? this.parent.getTransform(var1) : this.cameraTransforms.getTransform(var1);
   }

   public static void checkModelHierarchy(Map var0) {
      for(ModelBlock var2 : var0.values()) {
         try {
            ModelBlock var3 = var2.parent;

            for(ModelBlock var4 = var3.parent; var3 != var4; var4 = var4.parent.parent) {
               var3 = var3.parent;
            }

            throw new ModelBlock.LoopException();
         } catch (NullPointerException var5) {
            ;
         }
      }

   }

   @SideOnly(Side.CLIENT)
   static final class Bookkeep {
      public final ModelBlock model;
      public ModelBlock modelExt;

      private Bookkeep(ModelBlock var1) {
         this.model = var1;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class Deserializer implements JsonDeserializer {
      public ModelBlock deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         JsonObject var4 = var1.getAsJsonObject();
         List var5 = this.getModelElements(var3, var4);
         String var6 = this.getParent(var4);
         Map var7 = this.getTextures(var4);
         boolean var8 = this.getAmbientOcclusionEnabled(var4);
         ItemCameraTransforms var9 = ItemCameraTransforms.DEFAULT;
         if (var4.has("display")) {
            JsonObject var10 = JsonUtils.getJsonObject(var4, "display");
            var9 = (ItemCameraTransforms)var3.deserialize(var10, ItemCameraTransforms.class);
         }

         List var12 = this.getItemOverrides(var3, var4);
         ResourceLocation var11 = var6.isEmpty() ? null : new ResourceLocation(var6);
         return new ModelBlock(var11, var5, var7, var8, true, var9, var12);
      }

      protected List getItemOverrides(JsonDeserializationContext var1, JsonObject var2) {
         ArrayList var3 = Lists.newArrayList();
         if (var2.has("overrides")) {
            for(JsonElement var5 : JsonUtils.getJsonArray(var2, "overrides")) {
               var3.add((ItemOverride)var1.deserialize(var5, ItemOverride.class));
            }
         }

         return var3;
      }

      private Map getTextures(JsonObject var1) {
         HashMap var2 = Maps.newHashMap();
         if (var1.has("textures")) {
            JsonObject var3 = var1.getAsJsonObject("textures");

            for(Entry var5 : var3.entrySet()) {
               var2.put(var5.getKey(), ((JsonElement)var5.getValue()).getAsString());
            }
         }

         return var2;
      }

      private String getParent(JsonObject var1) {
         return JsonUtils.getString(var1, "parent", "");
      }

      protected boolean getAmbientOcclusionEnabled(JsonObject var1) {
         return JsonUtils.getBoolean(var1, "ambientocclusion", true);
      }

      protected List getModelElements(JsonDeserializationContext var1, JsonObject var2) {
         ArrayList var3 = Lists.newArrayList();
         if (var2.has("elements")) {
            for(JsonElement var5 : JsonUtils.getJsonArray(var2, "elements")) {
               var3.add((BlockPart)var1.deserialize(var5, BlockPart.class));
            }
         }

         return var3;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class LoopException extends RuntimeException {
   }
}
