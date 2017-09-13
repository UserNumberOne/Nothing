package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderSpider;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerSpiderEyes implements LayerRenderer {
   private static final ResourceLocation SPIDER_EYES = new ResourceLocation("textures/entity/spider_eyes.png");
   private final RenderSpider spiderRenderer;

   public LayerSpiderEyes(RenderSpider var1) {
      this.spiderRenderer = var1;
   }

   public void doRenderLayer(EntitySpider var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      this.spiderRenderer.bindTexture(SPIDER_EYES);
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
      if (var1.isInvisible()) {
         GlStateManager.depthMask(false);
      } else {
         GlStateManager.depthMask(true);
      }

      int var9 = 61680;
      int var10 = var9 % 65536;
      int var11 = var9 / 65536;
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)var10, (float)var11);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.spiderRenderer.getMainModel().render(var1, var2, var3, var5, var6, var7, var8);
      var9 = var1.getBrightnessForRender(var4);
      var10 = var9 % 65536;
      var11 = var9 / 65536;
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)var10, (float)var11);
      this.spiderRenderer.setLightmap(var1, var4);
      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
   }

   public boolean shouldCombineTextures() {
      return false;
   }
}
