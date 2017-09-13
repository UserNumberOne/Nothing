package net.minecraft.client.renderer.entity.layers;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderEnderman;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerHeldBlock implements LayerRenderer {
   private final RenderEnderman endermanRenderer;

   public LayerHeldBlock(RenderEnderman var1) {
      this.endermanRenderer = var1;
   }

   public void doRenderLayer(EntityEnderman var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      IBlockState var9 = var1.getHeldBlockState();
      if (var9 != null) {
         BlockRendererDispatcher var10 = Minecraft.getMinecraft().getBlockRendererDispatcher();
         GlStateManager.enableRescaleNormal();
         GlStateManager.pushMatrix();
         GlStateManager.translate(0.0F, 0.6875F, -0.75F);
         GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.translate(0.25F, 0.1875F, 0.25F);
         float var11 = 0.5F;
         GlStateManager.scale(-0.5F, -0.5F, 0.5F);
         int var12 = var1.getBrightnessForRender(var4);
         int var13 = var12 % 65536;
         int var14 = var12 / 65536;
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)var13, (float)var14);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         this.endermanRenderer.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
         var10.renderBlockBrightness(var9, 1.0F);
         GlStateManager.popMatrix();
         GlStateManager.disableRescaleNormal();
      }

   }

   public boolean shouldCombineTextures() {
      return false;
   }
}
