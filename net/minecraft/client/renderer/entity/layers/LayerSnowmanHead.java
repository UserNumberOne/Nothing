package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderSnowMan;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerSnowmanHead implements LayerRenderer {
   private final RenderSnowMan snowManRenderer;

   public LayerSnowmanHead(RenderSnowMan var1) {
      this.snowManRenderer = snowManRendererIn;
   }

   public void doRenderLayer(EntitySnowman var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if (!entitylivingbaseIn.isInvisible() && !entitylivingbaseIn.isPumpkinEquipped()) {
         GlStateManager.pushMatrix();
         this.snowManRenderer.getMainModel().head.postRender(0.0625F);
         float f = 0.625F;
         GlStateManager.translate(0.0F, -0.34375F, 0.0F);
         GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.scale(0.625F, -0.625F, -0.625F);
         Minecraft.getMinecraft().getItemRenderer().renderItem(entitylivingbaseIn, new ItemStack(Blocks.PUMPKIN, 1), ItemCameraTransforms.TransformType.HEAD);
         GlStateManager.popMatrix();
      }

   }

   public boolean shouldCombineTextures() {
      return true;
   }
}
