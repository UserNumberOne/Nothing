package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelQuadruped;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderMooshroom;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerMooshroomMushroom implements LayerRenderer {
   private final RenderMooshroom mooshroomRenderer;

   public LayerMooshroomMushroom(RenderMooshroom var1) {
      this.mooshroomRenderer = mooshroomRendererIn;
   }

   public void doRenderLayer(EntityMooshroom var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if (!entitylivingbaseIn.isChild() && !entitylivingbaseIn.isInvisible()) {
         BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
         this.mooshroomRenderer.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
         GlStateManager.enableCull();
         GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
         GlStateManager.pushMatrix();
         GlStateManager.scale(1.0F, -1.0F, 1.0F);
         GlStateManager.translate(0.2F, 0.35F, 0.5F);
         GlStateManager.rotate(42.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.pushMatrix();
         GlStateManager.translate(-0.5F, -0.5F, 0.5F);
         blockrendererdispatcher.renderBlockBrightness(Blocks.RED_MUSHROOM.getDefaultState(), 1.0F);
         GlStateManager.popMatrix();
         GlStateManager.pushMatrix();
         GlStateManager.translate(0.1F, 0.0F, -0.6F);
         GlStateManager.rotate(42.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.translate(-0.5F, -0.5F, 0.5F);
         blockrendererdispatcher.renderBlockBrightness(Blocks.RED_MUSHROOM.getDefaultState(), 1.0F);
         GlStateManager.popMatrix();
         GlStateManager.popMatrix();
         GlStateManager.pushMatrix();
         ((ModelQuadruped)this.mooshroomRenderer.getMainModel()).head.postRender(0.0625F);
         GlStateManager.scale(1.0F, -1.0F, 1.0F);
         GlStateManager.translate(0.0F, 0.7F, -0.2F);
         GlStateManager.rotate(12.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.translate(-0.5F, -0.5F, 0.5F);
         blockrendererdispatcher.renderBlockBrightness(Blocks.RED_MUSHROOM.getDefaultState(), 1.0F);
         GlStateManager.popMatrix();
         GlStateManager.cullFace(GlStateManager.CullFace.BACK);
         GlStateManager.disableCull();
      }

   }

   public boolean shouldCombineTextures() {
      return true;
   }
}
