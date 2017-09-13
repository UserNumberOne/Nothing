package net.minecraft.client.renderer.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderTntMinecart extends RenderMinecart {
   public RenderTntMinecart(RenderManager var1) {
      super(var1);
   }

   protected void renderCartContents(EntityMinecartTNT var1, float var2, IBlockState var3) {
      int var4 = var1.getFuseTicks();
      if (var4 > -1 && (float)var4 - var2 + 1.0F < 10.0F) {
         float var5 = 1.0F - ((float)var4 - var2 + 1.0F) / 10.0F;
         var5 = MathHelper.clamp(var5, 0.0F, 1.0F);
         var5 = var5 * var5;
         var5 = var5 * var5;
         float var6 = 1.0F + var5 * 0.3F;
         GlStateManager.scale(var6, var6, var6);
      }

      super.renderCartContents(var1, var2, var3);
      if (var4 > -1 && var4 / 5 % 2 == 0) {
         BlockRendererDispatcher var10 = Minecraft.getMinecraft().getBlockRendererDispatcher();
         GlStateManager.disableTexture2D();
         GlStateManager.disableLighting();
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_ALPHA);
         GlStateManager.color(1.0F, 1.0F, 1.0F, (1.0F - ((float)var4 - var2 + 1.0F) / 100.0F) * 0.8F);
         GlStateManager.pushMatrix();
         var10.renderBlockBrightness(Blocks.TNT.getDefaultState(), 1.0F);
         GlStateManager.popMatrix();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.disableBlend();
         GlStateManager.enableLighting();
         GlStateManager.enableTexture2D();
      }

   }
}
