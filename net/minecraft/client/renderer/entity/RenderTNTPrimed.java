package net.minecraft.client.renderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTNTPrimed extends Render {
   public RenderTNTPrimed(RenderManager var1) {
      super(var1);
      this.shadowSize = 0.5F;
   }

   public void doRender(EntityTNTPrimed var1, double var2, double var4, double var6, float var8, float var9) {
      BlockRendererDispatcher var10 = Minecraft.getMinecraft().getBlockRendererDispatcher();
      GlStateManager.pushMatrix();
      GlStateManager.translate((float)var2, (float)var4 + 0.5F, (float)var6);
      if ((float)var1.getFuse() - var9 + 1.0F < 10.0F) {
         float var11 = 1.0F - ((float)var1.getFuse() - var9 + 1.0F) / 10.0F;
         var11 = MathHelper.clamp(var11, 0.0F, 1.0F);
         var11 = var11 * var11;
         var11 = var11 * var11;
         float var12 = 1.0F + var11 * 0.3F;
         GlStateManager.scale(var12, var12, var12);
      }

      float var16 = (1.0F - ((float)var1.getFuse() - var9 + 1.0F) / 100.0F) * 0.8F;
      this.bindEntityTexture(var1);
      GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.translate(-0.5F, -0.5F, 0.5F);
      var10.renderBlockBrightness(Blocks.TNT.getDefaultState(), var1.getBrightness(var9));
      GlStateManager.translate(0.0F, 0.0F, 1.0F);
      if (this.renderOutlines) {
         GlStateManager.enableColorMaterial();
         GlStateManager.enableOutlineMode(this.getTeamColor(var1));
         var10.renderBlockBrightness(Blocks.TNT.getDefaultState(), 1.0F);
         GlStateManager.disableOutlineMode();
         GlStateManager.disableColorMaterial();
      } else if (var1.getFuse() / 5 % 2 == 0) {
         GlStateManager.disableTexture2D();
         GlStateManager.disableLighting();
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_ALPHA);
         GlStateManager.color(1.0F, 1.0F, 1.0F, var16);
         GlStateManager.doPolygonOffset(-3.0F, -3.0F);
         GlStateManager.enablePolygonOffset();
         var10.renderBlockBrightness(Blocks.TNT.getDefaultState(), 1.0F);
         GlStateManager.doPolygonOffset(0.0F, 0.0F);
         GlStateManager.disablePolygonOffset();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.disableBlend();
         GlStateManager.enableLighting();
         GlStateManager.enableTexture2D();
      }

      GlStateManager.popMatrix();
      super.doRender(var1, var2, var4, var6, var8, var9);
   }

   protected ResourceLocation getEntityTexture(EntityTNTPrimed var1) {
      return TextureMap.LOCATION_BLOCKS_TEXTURE;
   }
}
