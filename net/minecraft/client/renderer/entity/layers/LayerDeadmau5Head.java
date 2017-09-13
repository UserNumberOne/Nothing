package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerDeadmau5Head implements LayerRenderer {
   private final RenderPlayer playerRenderer;

   public LayerDeadmau5Head(RenderPlayer var1) {
      this.playerRenderer = playerRendererIn;
   }

   public void doRenderLayer(AbstractClientPlayer var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if ("deadmau5".equals(entitylivingbaseIn.getName()) && entitylivingbaseIn.hasSkin() && !entitylivingbaseIn.isInvisible()) {
         this.playerRenderer.bindTexture(entitylivingbaseIn.getLocationSkin());

         for(int i = 0; i < 2; ++i) {
            float f = entitylivingbaseIn.prevRotationYaw + (entitylivingbaseIn.rotationYaw - entitylivingbaseIn.prevRotationYaw) * partialTicks - (entitylivingbaseIn.prevRenderYawOffset + (entitylivingbaseIn.renderYawOffset - entitylivingbaseIn.prevRenderYawOffset) * partialTicks);
            float f1 = entitylivingbaseIn.prevRotationPitch + (entitylivingbaseIn.rotationPitch - entitylivingbaseIn.prevRotationPitch) * partialTicks;
            GlStateManager.pushMatrix();
            GlStateManager.rotate(f, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(f1, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.375F * (float)(i * 2 - 1), 0.0F, 0.0F);
            GlStateManager.translate(0.0F, -0.375F, 0.0F);
            GlStateManager.rotate(-f1, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-f, 0.0F, 1.0F, 0.0F);
            float f2 = 1.3333334F;
            GlStateManager.scale(1.3333334F, 1.3333334F, 1.3333334F);
            this.playerRenderer.getMainModel().renderDeadmau5Head(0.0625F);
            GlStateManager.popMatrix();
         }
      }

   }

   public boolean shouldCombineTextures() {
      return true;
   }
}
