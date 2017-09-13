package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.ModelSkeleton;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.SkeletonType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerSkeletonType implements LayerRenderer {
   private static final ResourceLocation STRAY_CLOTHES_TEXTURES = new ResourceLocation("textures/entity/skeleton/stray_overlay.png");
   private final RenderLivingBase renderer;
   private ModelSkeleton layerModel;

   public LayerSkeletonType(RenderLivingBase var1) {
      this.renderer = var1;
      this.layerModel = new ModelSkeleton(0.25F, true);
   }

   public void doRenderLayer(EntitySkeleton var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if (var1.getSkeletonType() == SkeletonType.STRAY) {
         this.layerModel.setModelAttributes(this.renderer.getMainModel());
         this.layerModel.setLivingAnimations(var1, var2, var3, var4);
         this.renderer.bindTexture(STRAY_CLOTHES_TEXTURES);
         this.layerModel.render(var1, var2, var3, var5, var6, var7, var8);
      }

   }

   public boolean shouldCombineTextures() {
      return true;
   }
}
