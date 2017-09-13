package net.minecraft.client.renderer.block.model;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MultipartBakedModel implements IBakedModel {
   private final Map selectors;
   protected final boolean ambientOcclusion;
   protected final boolean gui3D;
   protected final TextureAtlasSprite particleTexture;
   protected final ItemCameraTransforms cameraTransforms;
   protected final ItemOverrideList overrides;

   public MultipartBakedModel(Map var1) {
      this.selectors = var1;
      IBakedModel var2 = (IBakedModel)var1.values().iterator().next();
      this.ambientOcclusion = var2.isAmbientOcclusion();
      this.gui3D = var2.isGui3d();
      this.particleTexture = var2.getParticleTexture();
      this.cameraTransforms = var2.getItemCameraTransforms();
      this.overrides = var2.getOverrides();
   }

   public List getQuads(@Nullable IBlockState var1, @Nullable EnumFacing var2, long var3) {
      ArrayList var5 = Lists.newArrayList();
      if (var1 != null) {
         for(Entry var7 : this.selectors.entrySet()) {
            if (((Predicate)var7.getKey()).apply(var1)) {
               var5.addAll(((IBakedModel)var7.getValue()).getQuads(var1, var2, var3++));
            }
         }
      }

      return var5;
   }

   public boolean isAmbientOcclusion() {
      return this.ambientOcclusion;
   }

   public boolean isGui3d() {
      return this.gui3D;
   }

   public boolean isBuiltInRenderer() {
      return false;
   }

   public TextureAtlasSprite getParticleTexture() {
      return this.particleTexture;
   }

   public ItemCameraTransforms getItemCameraTransforms() {
      return this.cameraTransforms;
   }

   public ItemOverrideList getOverrides() {
      return this.overrides;
   }

   @SideOnly(Side.CLIENT)
   public static class Builder {
      private final Map builderSelectors = Maps.newLinkedHashMap();

      public void putModel(Predicate var1, IBakedModel var2) {
         this.builderSelectors.put(var1, var2);
      }

      public IBakedModel makeMultipartModel() {
         return new MultipartBakedModel(this.builderSelectors);
      }
   }
}
