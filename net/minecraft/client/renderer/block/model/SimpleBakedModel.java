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
      this.generalQuads = var1;
      this.faceQuads = var2;
      this.ambientOcclusion = var3;
      this.gui3d = var4;
      this.texture = var5;
      this.cameraTransforms = var6;
      this.itemOverrideList = var7;
   }

   public List getQuads(@Nullable IBlockState var1, @Nullable EnumFacing var2, long var3) {
      return var2 == null ? this.generalQuads : (List)this.faceQuads.get(var2);
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
         this(var1.isAmbientOcclusion(), var1.isGui3d(), var1.getAllTransforms(), var2);
      }

      public Builder(IBlockState var1, IBakedModel var2, TextureAtlasSprite var3, BlockPos var4) {
         this(var2.isAmbientOcclusion(), var2.isGui3d(), var2.getItemCameraTransforms(), var2.getOverrides());
         this.builderTexture = var2.getParticleTexture();
         long var5 = MathHelper.getPositionRandom(var4);

         for(EnumFacing var10 : EnumFacing.values()) {
            this.addFaceQuads(var1, var2, var3, var10, var5);
         }

         this.addGeneralQuads(var1, var2, var3, var5);
      }

      private Builder(boolean var1, boolean var2, ItemCameraTransforms var3, ItemOverrideList var4) {
         this.builderGeneralQuads = Lists.newArrayList();
         this.builderFaceQuads = Maps.newEnumMap(EnumFacing.class);

         for(EnumFacing var8 : EnumFacing.values()) {
            this.builderFaceQuads.put(var8, Lists.newArrayList());
         }

         this.builderItemOverrideList = var4;
         this.builderAmbientOcclusion = var1;
         this.builderGui3d = var2;
         this.builderCameraTransforms = var3;
      }

      private void addFaceQuads(IBlockState var1, IBakedModel var2, TextureAtlasSprite var3, EnumFacing var4, long var5) {
         for(BakedQuad var8 : var2.getQuads(var1, var4, var5)) {
            this.addFaceQuad(var4, new BakedQuadRetextured(var8, var3));
         }

      }

      private void addGeneralQuads(IBlockState var1, IBakedModel var2, TextureAtlasSprite var3, long var4) {
         for(BakedQuad var7 : var2.getQuads(var1, (EnumFacing)null, var4)) {
            this.addGeneralQuad(new BakedQuadRetextured(var7, var3));
         }

      }

      public SimpleBakedModel.Builder addFaceQuad(EnumFacing var1, BakedQuad var2) {
         ((List)this.builderFaceQuads.get(var1)).add(var2);
         return this;
      }

      public SimpleBakedModel.Builder addGeneralQuad(BakedQuad var1) {
         this.builderGeneralQuads.add(var1);
         return this;
      }

      public SimpleBakedModel.Builder setTexture(TextureAtlasSprite var1) {
         this.builderTexture = var1;
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
