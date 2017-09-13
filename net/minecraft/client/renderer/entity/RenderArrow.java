package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class RenderArrow extends Render {
   public RenderArrow(RenderManager var1) {
      super(var1);
   }

   public void doRender(EntityArrow var1, double var2, double var4, double var6, float var8, float var9) {
      this.bindEntityTexture(var1);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.pushMatrix();
      GlStateManager.disableLighting();
      GlStateManager.translate((float)var2, (float)var4, (float)var6);
      GlStateManager.rotate(var1.prevRotationYaw + (var1.rotationYaw - var1.prevRotationYaw) * var9 - 90.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(var1.prevRotationPitch + (var1.rotationPitch - var1.prevRotationPitch) * var9, 0.0F, 0.0F, 1.0F);
      Tessellator var10 = Tessellator.getInstance();
      VertexBuffer var11 = var10.getBuffer();
      boolean var12 = false;
      float var13 = 0.0F;
      float var14 = 0.5F;
      float var15 = 0.0F;
      float var16 = 0.15625F;
      float var17 = 0.0F;
      float var18 = 0.15625F;
      float var19 = 0.15625F;
      float var20 = 0.3125F;
      float var21 = 0.05625F;
      GlStateManager.enableRescaleNormal();
      float var22 = (float)var1.arrowShake - var9;
      if (var22 > 0.0F) {
         float var23 = -MathHelper.sin(var22 * 3.0F) * var22;
         GlStateManager.rotate(var23, 0.0F, 0.0F, 1.0F);
      }

      GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.scale(0.05625F, 0.05625F, 0.05625F);
      GlStateManager.translate(-4.0F, 0.0F, 0.0F);
      if (this.renderOutlines) {
         GlStateManager.enableColorMaterial();
         GlStateManager.enableOutlineMode(this.getTeamColor(var1));
      }

      GlStateManager.glNormal3f(0.05625F, 0.0F, 0.0F);
      var11.begin(7, DefaultVertexFormats.POSITION_TEX);
      var11.pos(-7.0D, -2.0D, -2.0D).tex(0.0D, 0.15625D).endVertex();
      var11.pos(-7.0D, -2.0D, 2.0D).tex(0.15625D, 0.15625D).endVertex();
      var11.pos(-7.0D, 2.0D, 2.0D).tex(0.15625D, 0.3125D).endVertex();
      var11.pos(-7.0D, 2.0D, -2.0D).tex(0.0D, 0.3125D).endVertex();
      var10.draw();
      GlStateManager.glNormal3f(-0.05625F, 0.0F, 0.0F);
      var11.begin(7, DefaultVertexFormats.POSITION_TEX);
      var11.pos(-7.0D, 2.0D, -2.0D).tex(0.0D, 0.15625D).endVertex();
      var11.pos(-7.0D, 2.0D, 2.0D).tex(0.15625D, 0.15625D).endVertex();
      var11.pos(-7.0D, -2.0D, 2.0D).tex(0.15625D, 0.3125D).endVertex();
      var11.pos(-7.0D, -2.0D, -2.0D).tex(0.0D, 0.3125D).endVertex();
      var10.draw();

      for(int var24 = 0; var24 < 4; ++var24) {
         GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.glNormal3f(0.0F, 0.0F, 0.05625F);
         var11.begin(7, DefaultVertexFormats.POSITION_TEX);
         var11.pos(-8.0D, -2.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
         var11.pos(8.0D, -2.0D, 0.0D).tex(0.5D, 0.0D).endVertex();
         var11.pos(8.0D, 2.0D, 0.0D).tex(0.5D, 0.15625D).endVertex();
         var11.pos(-8.0D, 2.0D, 0.0D).tex(0.0D, 0.15625D).endVertex();
         var10.draw();
      }

      if (this.renderOutlines) {
         GlStateManager.disableOutlineMode();
         GlStateManager.disableColorMaterial();
      }

      GlStateManager.disableRescaleNormal();
      GlStateManager.enableLighting();
      GlStateManager.popMatrix();
      super.doRender(var1, var2, var4, var6, var8, var9);
   }
}
