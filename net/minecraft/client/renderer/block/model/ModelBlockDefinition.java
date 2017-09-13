package net.minecraft.client.renderer.block.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.multipart.Multipart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.client.model.BlockStateLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelBlockDefinition {
   @VisibleForTesting
   static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(ModelBlockDefinition.class, new ModelBlockDefinition.Deserializer()).registerTypeAdapter(Variant.class, new Variant.Deserializer()).registerTypeAdapter(VariantList.class, new VariantList.Deserializer()).registerTypeAdapter(Multipart.class, new Multipart.Deserializer()).registerTypeAdapter(Selector.class, new Selector.Deserializer()).create();
   private final Map mapVariants = Maps.newHashMap();
   private Multipart multipart;

   public static ModelBlockDefinition parseFromReader(Reader var0) {
      return BlockStateLoader.load(var0, GSON);
   }

   public ModelBlockDefinition(Map var1, Multipart var2) {
      this.multipart = var2;
      this.mapVariants.putAll(var1);
   }

   public ModelBlockDefinition(List var1) {
      ModelBlockDefinition var2 = null;

      for(ModelBlockDefinition var4 : var1) {
         if (var4.hasMultipartData()) {
            this.mapVariants.clear();
            var2 = var4;
         }

         this.mapVariants.putAll(var4.mapVariants);
      }

      if (var2 != null) {
         this.multipart = var2.multipart;
      }

   }

   public boolean hasVariant(String var1) {
      return this.mapVariants.get(var1) != null;
   }

   public VariantList getVariant(String var1) {
      VariantList var2 = (VariantList)this.mapVariants.get(var1);
      if (var2 == null) {
         throw new ModelBlockDefinition.MissingVariantException();
      } else {
         return var2;
      }
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else {
         if (var1 instanceof ModelBlockDefinition) {
            ModelBlockDefinition var2 = (ModelBlockDefinition)var1;
            if (this.mapVariants.equals(var2.mapVariants)) {
               return this.hasMultipartData() ? this.multipart.equals(var2.multipart) : !var2.hasMultipartData();
            }
         }

         return false;
      }
   }

   public int hashCode() {
      return 31 * this.mapVariants.hashCode() + (this.hasMultipartData() ? this.multipart.hashCode() : 0);
   }

   public Set getMultipartVariants() {
      HashSet var1 = Sets.newHashSet(this.mapVariants.values());
      if (this.hasMultipartData()) {
         var1.addAll(this.multipart.getVariants());
      }

      return var1;
   }

   public boolean hasMultipartData() {
      return this.multipart != null;
   }

   public Multipart getMultipartData() {
      return this.multipart;
   }

   @SideOnly(Side.CLIENT)
   public static class Deserializer implements JsonDeserializer {
      public ModelBlockDefinition deserialize(JsonElement var1, Type var2, JsonDeserializationContext var3) throws JsonParseException {
         JsonObject var4 = var1.getAsJsonObject();
         Map var5 = this.parseMapVariants(var3, var4);
         Multipart var6 = this.parseMultipart(var3, var4);
         if (var5.isEmpty() && (var6 == null || var6.getVariants().isEmpty())) {
            throw new JsonParseException("Neither 'variants' nor 'multipart' found");
         } else {
            return new ModelBlockDefinition(var5, var6);
         }
      }

      protected Map parseMapVariants(JsonDeserializationContext var1, JsonObject var2) {
         HashMap var3 = Maps.newHashMap();
         if (var2.has("variants")) {
            JsonObject var4 = JsonUtils.getJsonObject(var2, "variants");

            for(Entry var6 : var4.entrySet()) {
               var3.put(var6.getKey(), (VariantList)var1.deserialize((JsonElement)var6.getValue(), VariantList.class));
            }
         }

         return var3;
      }

      @Nullable
      protected Multipart parseMultipart(JsonDeserializationContext var1, JsonObject var2) {
         if (!var2.has("multipart")) {
            return null;
         } else {
            JsonArray var3 = JsonUtils.getJsonArray(var2, "multipart");
            return (Multipart)var1.deserialize(var3, Multipart.class);
         }
      }
   }

   @SideOnly(Side.CLIENT)
   public class MissingVariantException extends RuntimeException {
   }
}
