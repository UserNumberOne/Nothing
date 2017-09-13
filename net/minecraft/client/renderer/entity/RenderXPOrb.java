package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderXPOrb extends Render {
   private static final ResourceLocation EXPERIENCE_ORB_TEXTURES = new ResourceLocation("textures/entity/experience_orb.png");

   public RenderXPOrb(RenderManager var1) {
      super(var1);
      this.shadowSize = 0.15F;
      this.shadowOpaque = 0.75F;
   }

   public void doRender(EntityXPOrb var1, double var2, double var4, double var6, float var8, float var9) {
      if (!this.renderOutlines) {
         GlStateManager.pushMatrix();
         GlStateManager.translate((float)var2, (float)var4, (float)var6);
         this.bindEntityTexture(var1);
         RenderHelper.enableStandardItemLighting();
         int var10 = var1.getTextureByXP();
         float var11 = (float)(var10 % 4 * 16 + 0) / 64.0F;
         float var12 = (float)(var10 % 4 * 16 + 16) / 64.0F;
         float var13 = (float)(var10 / 4 * 16 + 0) / 64.0F;
         float var14 = (float)(var10 / 4 * 16 + 16) / 64.0F;
         float var15 = 1.0F;
         float var16 = 0.5F;
         float var17 = 0.25F;
         int var18 = var1.getBrightnessForRender(var9);
         int var19 = var18 % 65536;
         int var20 = var18 / 65536;
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)var19, (float)var20);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         float var21 = 255.0F;
         float var22 = ((float)var1.xpColor + var9) / 2.0F;
         var20 = (int)((MathHelper.sin(var22 + 0.0F) + 1.0F) * 0.5F * 255.0F);
         boolean var23 = true;
         int var24 = (int)((MathHelper.sin(var22 + 4.1887903F) + 1.0F) * 0.1F * 255.0F);
         GlStateManager.translate(0.0F, 0.1F, 0.0F);
         GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate((float)(this.renderManager.options.thirdPersonView == 2 ? -1 : 1) * -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
         float var25 = 0.3F;
         GlStateManager.scale(0.3F, 0.3F, 0.3F);
         Tessellator var26 = Tessellator.getInstance();
         VertexBuffer var27 = var26.getBuffer();
         var27.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
         var27.pos(-0.5D, -0.25D, 0.0D).tex((double)var11, (double)var14).color(var20, 255, var24, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
         var27.pos(0.5D, -0.25D, 0.0D).tex((double)var12, (double)var14).color(var20, 255, var24, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
         var27.pos(0.5D, 0.75D, 0.0D).tex((double)var12, (double)var13).color(var20, 255, var24, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
         var27.pos(-0.5D, 0.75D, 0.0D).tex((double)var11, (double)var13).color(var20, 255, var24, 128).normal(0.0F, 1.0F, 0.0F).endVertex();
         var26.draw();
         GlStateManager.disableBlend();
         GlStateManager.disableRescaleNormal();
         GlStateManager.popMatrix();
         super.doRender(var1, var2, var4, var6, var8, var9);
      }

   }

   protected ResourceLocation getEntityTexture(EntityXPOrb var1) {
      return EXPERIENCE_ORB_TEXTURES;
   }
}
