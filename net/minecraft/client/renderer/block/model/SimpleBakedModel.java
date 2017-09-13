package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SimpleBakedModel implements IBakedModel {
   protected final List generalQuads;
   protected final Map faceQuads;
   protected final boolean ambientOcclusion;
   protected final boolean gui3d;
   protected final TextureAtlasSprite texture;
   protected final ItemCameraTransforms cameraTransforms;
   protected final ItemOverrideList itemOverrideList;

   public SimpleBakedModel(List var1, Map var2, boolean var3, boolean var4, TextureAtlasSprite var5, ItemCameraTransforms var6, ItemOverrideList var7) {
      this.generalQuads = generalQuadsIn;
      this.faceQuads = faceQuadsIn;
      this.ambientOcclusion = ambientOcclusionIn;
      this.gui3d = gui3dIn;
      this.texture = textureIn;
      this.cameraTransforms = cameraTransformsIn;
      this.itemOverrideList = itemOverrideListIn;
   }

   public List getQuads(@Nullable IBlockState var1, @Nullable EnumFacing var2, long var3) {
      return side == null ? this.generalQuads : (List)this.faceQuads.get(side);
   }

   public boolean isAmbientOcclusion() {
      return this.ambientOcclusion;
   }

   public boolean isGui3d() {
      return this.gui3d;
   }

   public boolean isBuiltInRenderer() {
      return false;
   }

   public TextureAtlasSprite getParticleTexture() {
      return this.texture;
   }

   public ItemCameraTransforms getItemCameraTransforms() {
      return this.cameraTransforms;
   }

   public ItemOverrideList getOverrides() {
      return this.itemOverrideList;
   }

   @SideOnly(Side.CLIENT)
   public static class Builder {
      private final List builderGeneralQuads;
      private final Map builderFaceQuads;
      private final ItemOverrideList builderItemOverrideList;
      private final boolean builderAmbientOcclusion;
      private TextureAtlasSprite builderTexture;
      private final boolean builderGui3d;
      private final ItemCameraTransforms builderCameraTransforms;

      public Builder(ModelBlock var1, ItemOverrideList var2) {
         this(model.isAmbientOcclusion(), model.isGui3d(), model.getAllTransforms(), overrides);
      }

      public Builder(IBlockState var1, IBakedModel var2, TextureAtlasSprite var3, BlockPos var4) {
         this(model.isAmbientOcclusion(), model.isGui3d(), model.getItemCameraTransforms(), model.getOverrides());
         this.builderTexture = model.getParticleTexture();
         long i = MathHelper.getPositionRandom(pos);

         for(EnumFacing enumfacing : EnumFacing.values()) {
            this.addFaceQuads(state, model, texture, enumfacing, i);
         }

         this.addGeneralQuads(state, model, texture, i);
      }

      private Builder(boolean var1, boolean var2, ItemCameraTransforms var3, ItemOverrideList var4) {
         this.builderGeneralQuads = Lists.newArrayList();
         this.builderFaceQuads = Maps.newEnumMap(EnumFacing.class);

         for(EnumFacing enumfacing : EnumFacing.values()) {
            this.builderFaceQuads.put(enumfacing, Lists.newArrayList());
         }

         this.builderItemOverrideList = overrides;
         this.builderAmbientOcclusion = ambientOcclusion;
         this.builderGui3d = gui3d;
         this.builderCameraTransforms = transforms;
      }

      private void addFaceQuads(IBlockState var1, IBakedModel var2, TextureAtlasSprite var3, EnumFacing var4, long var5) {
         for(BakedQuad bakedquad : p_188644_2_.getQuads(p_188644_1_, p_188644_4_, p_188644_5_)) {
            this.addFaceQuad(p_188644_4_, new BakedQuadRetextured(bakedquad, p_188644_3_));
         }

      }

      private void addGeneralQuads(IBlockState var1, IBakedModel var2, TextureAtlasSprite var3, long var4) {
         for(BakedQuad bakedquad : p_188645_2_.getQuads(p_188645_1_, (EnumFacing)null, p_188645_4_)) {
            this.addGeneralQuad(new BakedQuadRetextured(bakedquad, p_188645_3_));
         }

      }

      public SimpleBakedModel.Builder addFaceQuad(EnumFacing var1, BakedQuad var2) {
         ((List)this.builderFaceQuads.get(facing)).add(quad);
         return this;
      }

      public SimpleBakedModel.Builder addGeneralQuad(BakedQuad var1) {
         this.builderGeneralQuads.add(quad);
         return this;
      }

      public SimpleBakedModel.Builder setTexture(TextureAtlasSprite var1) {
         this.builderTexture = texture;
         return this;
      }

      public IBakedModel makeBakedModel() {
         if (this.builderTexture == null) {
            throw new RuntimeException("Missing particle!");
         } else {
            return new SimpleBakedModel(this.builderGeneralQuads, this.builderFaceQuads, this.builderAmbientOcclusion, this.builderGui3d, this.builderTexture, this.builderCameraTransforms, this.builderItemOverrideList);
         }
      }
   }
}
