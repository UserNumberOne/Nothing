package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelSlime;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderSlime;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerSlimeGel implements LayerRenderer {
   private final RenderSlime slimeRenderer;
   private final ModelBase slimeModel = new ModelSlime(0);

   public LayerSlimeGel(RenderSlime var1) {
      this.slimeRenderer = slimeRendererIn;
   }

   public void doRenderLayer(EntitySlime var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if (!entitylivingbaseIn.isInvisible()) {
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.enableNormalize();
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
         this.slimeModel.setModelAttributes(this.slimeRenderer.getMainModel());
         this.slimeModel.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
         GlStateManager.disableBlend();
         GlStateManager.disableNormalize();
      }

   }

   public boolean shouldCombineTextures() {
      return true;
   }
}
