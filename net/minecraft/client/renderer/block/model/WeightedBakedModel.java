package net.minecraft.client.renderer.block.model;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.WeightedRandom;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class WeightedBakedModel implements IBakedModel {
   private final int totalWeight;
   private final List models;
   private final IBakedModel baseModel;

   public WeightedBakedModel(List var1) {
      this.models = var1;
      this.totalWeight = WeightedRandom.getTotalWeight(var1);
      this.baseModel = ((WeightedBakedModel.WeightedModel)var1.get(0)).model;
   }

   private IBakedModel getRandomModel(long var1) {
      return ((WeightedBakedModel.WeightedModel)WeightedRandom.getRandomItem(this.models, Math.abs((int)var1 >> 16) % this.totalWeight)).model;
   }

   public List getQuads(@Nullable IBlockState var1, @Nullable EnumFacing var2, long var3) {
      return this.getRandomModel(var3).getQuads(var1, var2, var3);
   }

   public boolean isAmbientOcclusion() {
      return this.baseModel.isAmbientOcclusion();
   }

   public boolean isGui3d() {
      return this.baseModel.isGui3d();
   }

   public boolean isBuiltInRenderer() {
      return this.baseModel.isBuiltInRenderer();
   }

   public TextureAtlasSprite getParticleTexture() {
      return this.baseModel.getParticleTexture();
   }

   public ItemCameraTransforms getItemCameraTransforms() {
      return this.baseModel.getItemCameraTransforms();
   }

   public ItemOverrideList getOverrides() {
      return this.baseModel.getOverrides();
   }

   @SideOnly(Side.CLIENT)
   public static class Builder {
      private final List listItems = Lists.newArrayList();

      public WeightedBakedModel.Builder add(IBakedModel var1, int var2) {
         this.listItems.add(new WeightedBakedModel.WeightedModel(var1, var2));
         return this;
      }

      public WeightedBakedModel build() {
         Collections.sort(this.listItems);
         return new WeightedBakedModel(this.listItems);
      }

      public IBakedModel first() {
         return ((WeightedBakedModel.WeightedModel)this.listItems.get(0)).model;
      }
   }

   @SideOnly(Side.CLIENT)
   static class WeightedModel extends WeightedRandom.Item implements Comparable {
      protected final IBakedModel model;

      public WeightedModel(IBakedModel var1, int var2) {
         super(var2);
         this.model = var1;
      }

      public int compareTo(WeightedBakedModel.WeightedModel var1) {
         return ComparisonChain.start().compare(var1.itemWeight, this.itemWeight).result();
      }

      public String toString() {
         return "MyWeighedRandomItem{weight=" + this.itemWeight + ", model=" + this.model + '}';
      }
   }
}
