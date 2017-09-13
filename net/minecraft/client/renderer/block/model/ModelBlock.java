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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
      return (ModelBlock)JsonUtils.gsonDeserialize(SERIALIZER, readerIn, ModelBlock.class, false);
   }

   public static ModelBlock deserialize(String var0) {
      return deserialize(new StringReader(jsonString));
   }

   public ModelBlock(@Nullable ResourceLocation var1, List var2, Map var3, boolean var4, boolean var5, ItemCameraTransforms var6, List var7) {
      this.elements = elementsIn;
      this.ambientOcclusion = ambientOcclusionIn;
      this.gui3d = gui3dIn;
      this.textures = texturesIn;
      this.parentLocation = parentLocationIn;
      this.cameraTransforms = cameraTransformsIn;
      this.overrides = overridesIn;
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
         this.parent = (ModelBlock)p_178299_1_.get(this.parentLocation);
      }

   }

   public Collection getOverrideLocations() {
      Set set = Sets.newHashSet();

      for(ItemOverride itemoverride : this.overrides) {
         set.add(itemoverride.getLocation());
      }

      return set;
   }

   public List getOverrides() {
      return this.overrides;
   }

   public ItemOverrideList createOverrides() {
      return this.overrides.isEmpty() ? ItemOverrideList.NONE : new ItemOverrideList(this.overrides);
   }

   public boolean isTexturePresent(String var1) {
      return !"missingno".equals(this.resolveTextureName(textureName));
   }

   public String resolveTextureName(String var1) {
      if (!this.startsWithHash(textureName)) {
         textureName = '#' + textureName;
      }

      return this.resolveTextureName(textureName, new ModelBlock.Bookkeep(this));
   }

   private String resolveTextureName(String var1, ModelBlock.Bookkeep var2) {
      if (this.startsWithHash(textureName)) {
         if (this == p_178302_2_.modelExt) {
            LOGGER.warn("Unable to resolve texture due to upward reference: {} in {}", new Object[]{textureName, this.name});
            return "missingno";
         } else {
            String s = (String)this.textures.get(textureName.substring(1));
            if (s == null && this.hasParent()) {
               s = this.parent.resolveTextureName(textureName, p_178302_2_);
            }

            p_178302_2_.modelExt = this;
            if (s != null && this.startsWithHash(s)) {
               s = p_178302_2_.model.resolveTextureName(s, p_178302_2_);
            }

            return s != null && !this.startsWithHash(s) ? s : "missingno";
         }
      } else {
         return textureName;
      }
   }

   private boolean startsWithHash(String var1) {
      return hash.charAt(0) == '#';
   }

   @Nullable
   public ResourceLocation getParentLocation() {
      return this.parentLocation;
   }

   public ModelBlock getRootModel() {
      return this.hasParent() ? this.parent.getRootModel() : this;
   }

   public ItemCameraTransforms getAllTransforms() {
      ItemTransformVec3f itemtransformvec3f = this.getTransform(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND);
      ItemTransformVec3f itemtransformvec3f1 = this.getTransform(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND);
      ItemTransformVec3f itemtransformvec3f2 = this.getTransform(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND);
      ItemTransformVec3f itemtransformvec3f3 = this.getTransform(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND);
      ItemTransformVec3f itemtransformvec3f4 = this.getTransform(ItemCameraTransforms.TransformType.HEAD);
      ItemTransformVec3f itemtransformvec3f5 = this.getTransform(ItemCameraTransforms.TransformType.GUI);
      ItemTransformVec3f itemtransformvec3f6 = this.getTransform(ItemCameraTransforms.TransformType.GROUND);
      ItemTransformVec3f itemtransformvec3f7 = this.getTransform(ItemCameraTransforms.TransformType.FIXED);
      return new ItemCameraTransforms(itemtransformvec3f, itemtransformvec3f1, itemtransformvec3f2, itemtransformvec3f3, itemtransformvec3f4, itemtransformvec3f5, itemtransformvec3f6, itemtransformvec3f7);
   }

   private ItemTransformVec3f getTransform(ItemCameraTransforms.TransformType var1) {
      return this.parent != null && !this.cameraTransforms.hasCustomTransform(type) ? this.parent.getTransform(type) : this.cameraTransforms.getTransform(type);
   }

   public static void checkModelHierarchy(Map var0) {
      for(ModelBlock modelblock : p_178312_0_.values()) {
         try {
            ModelBlock modelblock1 = modelblock.parent;

            for(ModelBlock modelblock2 = modelblock1.parent; modelblock1 != modelblock2; modelblock2 = modelblock2.parent.parent) {
               modelblock1 = modelblock1.parent;
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
         this.model = modelIn;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class Deserializer implements JsonDeserializer {
      public ModelBlock deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
         List list = this.getModelElements(p_deserialize_3_, jsonobject);
         String s = this.getParent(jsonobject);
         Map map = this.getTextures(jsonobject);
         boolean flag = this.getAmbientOcclusionEnabled(jsonobject);
         ItemCameraTransforms itemcameratransforms = ItemCameraTransforms.DEFAULT;
         if (jsonobject.has("display")) {
            JsonObject jsonobject1 = JsonUtils.getJsonObject(jsonobject, "display");
            itemcameratransforms = (ItemCameraTransforms)p_deserialize_3_.deserialize(jsonobject1, ItemCameraTransforms.class);
         }

         List list1 = this.getItemOverrides(p_deserialize_3_, jsonobject);
         ResourceLocation resourcelocation = s.isEmpty() ? null : new ResourceLocation(s);
         return new ModelBlock(resourcelocation, list, map, flag, true, itemcameratransforms, list1);
      }

      protected List getItemOverrides(JsonDeserializationContext var1, JsonObject var2) {
         List list = Lists.newArrayList();
         if (object.has("overrides")) {
            for(JsonElement jsonelement : JsonUtils.getJsonArray(object, "overrides")) {
               list.add((ItemOverride)deserializationContext.deserialize(jsonelement, ItemOverride.class));
            }
         }

         return list;
      }

      private Map getTextures(JsonObject var1) {
         Map map = Maps.newHashMap();
         if (object.has("textures")) {
            JsonObject jsonobject = object.getAsJsonObject("textures");

            for(Entry entry : jsonobject.entrySet()) {
               map.put(entry.getKey(), ((JsonElement)entry.getValue()).getAsString());
            }
         }

         return map;
      }

      private String getParent(JsonObject var1) {
         return JsonUtils.getString(object, "parent", "");
      }

      protected boolean getAmbientOcclusionEnabled(JsonObject var1) {
         return JsonUtils.getBoolean(object, "ambientocclusion", true);
      }

      protected List getModelElements(JsonDeserializationContext var1, JsonObject var2) {
         List list = Lists.newArrayList();
         if (object.has("elements")) {
            for(JsonElement jsonelement : JsonUtils.getJsonArray(object, "elements")) {
               list.add((BlockPart)deserializationContext.deserialize(jsonelement, BlockPart.class));
            }
         }

         return list;
      }
   }

   @SideOnly(Side.CLIENT)
   public static class LoopException extends RuntimeException {
   }
}
