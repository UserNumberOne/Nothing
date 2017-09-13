package net.minecraft.client.renderer.tileentity;

import java.util.List;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileEntityBeaconRenderer extends TileEntitySpecialRenderer {
   public static final ResourceLocation TEXTURE_BEACON_BEAM = new ResourceLocation("textures/entity/beacon_beam.png");

   public void renderTileEntityAt(TileEntityBeacon var1, double var2, double var4, double var6, float var8, int var9) {
      this.renderBeacon(var2, var4, var6, (double)var8, (double)var1.shouldBeamRender(), var1.getBeamSegments(), (double)var1.getWorld().getTotalWorldTime());
   }

   public void renderBeacon(double var1, double var3, double var5, double var7, double var9, List var11, double var12) {
      GlStateManager.alphaFunc(516, 0.1F);
      this.bindTexture(TEXTURE_BEACON_BEAM);
      if (var9 > 0.0D) {
         GlStateManager.disableFog();
         int var14 = 0;

         for(int var15 = 0; var15 < var11.size(); ++var15) {
            TileEntityBeacon.BeamSegment var16 = (TileEntityBeacon.BeamSegment)var11.get(var15);
            renderBeamSegment(var1, var3, var5, var7, var9, var12, var14, var16.getHeight(), var16.getColors());
            var14 += var16.getHeight();
         }

         GlStateManager.enableFog();
      }

   }

   public static void renderBeamSegment(double var0, double var2, double var4, double var6, double var8, double var10, int var12, int var13, float[] var14) {
      renderBeamSegment(var0, var2, var4, var6, var8, var10, var12, var13, var14, 0.2D, 0.25D);
   }

   public static void renderBeamSegment(double var0, double var2, double var4, double var6, double var8, double var10, int var12, int var13, float[] var14, double var15, double var17) {
      int var19 = var12 + var13;
      GlStateManager.glTexParameteri(3553, 10242, 10497);
      GlStateManager.glTexParameteri(3553, 10243, 10497);
      GlStateManager.disableLighting();
      GlStateManager.disableCull();
      GlStateManager.disableBlend();
      GlStateManager.depthMask(true);
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      Tessellator var20 = Tessellator.getInstance();
      VertexBuffer var21 = var20.getBuffer();
      double var22 = var10 + var6;
      double var24 = var13 < 0 ? var22 : -var22;
      double var26 = MathHelper.frac(var24 * 0.2D - (double)MathHelper.floor(var24 * 0.1D));
      float var28 = var14[0];
      float var29 = var14[1];
      float var30 = var14[2];
      double var31 = var22 * 0.025D * -1.5D;
      double var33 = 0.5D + Math.cos(var31 + 2.356194490192345D) * var15;
      double var35 = 0.5D + Math.sin(var31 + 2.356194490192345D) * var15;
      double var37 = 0.5D + Math.cos(var31 + 0.7853981633974483D) * var15;
      double var39 = 0.5D + Math.sin(var31 + 0.7853981633974483D) * var15;
      double var41 = 0.5D + Math.cos(var31 + 3.9269908169872414D) * var15;
      double var43 = 0.5D + Math.sin(var31 + 3.9269908169872414D) * var15;
      double var45 = 0.5D + Math.cos(var31 + 5.497787143782138D) * var15;
      double var47 = 0.5D + Math.sin(var31 + 5.497787143782138D) * var15;
      double var49 = 0.0D;
      double var51 = 1.0D;
      double var53 = -1.0D + var26;
      double var55 = (double)var13 * var8 * (0.5D / var15) + var53;
      var21.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      var21.pos(var0 + var33, var2 + (double)var19, var4 + var35).tex(1.0D, var55).color(var28, var29, var30, 1.0F).endVertex();
      var21.pos(var0 + var33, var2 + (double)var12, var4 + var35).tex(1.0D, var53).color(var28, var29, var30, 1.0F).endVertex();
      var21.pos(var0 + var37, var2 + (double)var12, var4 + var39).tex(0.0D, var53).color(var28, var29, var30, 1.0F).endVertex();
      var21.pos(var0 + var37, var2 + (double)var19, var4 + var39).tex(0.0D, var55).color(var28, var29, var30, 1.0F).endVertex();
      var21.pos(var0 + var45, var2 + (double)var19, var4 + var47).tex(1.0D, var55).color(var28, var29, var30, 1.0F).endVertex();
      var21.pos(var0 + var45, var2 + (double)var12, var4 + var47).tex(1.0D, var53).color(var28, var29, var30, 1.0F).endVertex();
      var21.pos(var0 + var41, var2 + (double)var12, var4 + var43).tex(0.0D, var53).color(var28, var29, var30, 1.0F).endVertex();
      var21.pos(var0 + var41, var2 + (double)var19, var4 + var43).tex(0.0D, var55).color(var28, var29, var30, 1.0F).endVertex();
      var21.pos(var0 + var37, var2 + (double)var19, var4 + var39).tex(1.0D, var55).color(var28, var29, var30, 1.0F).endVertex();
      var21.pos(var0 + var37, var2 + (double)var12, var4 + var39).tex(1.0D, var53).color(var28, var29, var30, 1.0F).endVertex();
      var21.pos(var0 + var45, var2 + (double)var12, var4 + var47).tex(0.0D, var53).color(var28, var29, var30, 1.0F).endVertex();
      var21.pos(var0 + var45, var2 + (double)var19, var4 + var47).tex(0.0D, var55).color(var28, var29, var30, 1.0F).endVertex();
      var21.pos(var0 + var41, var2 + (double)var19, var4 + var43).tex(1.0D, var55).color(var28, var29, var30, 1.0F).endVertex();
      var21.pos(var0 + var41, var2 + (double)var12, var4 + var43).tex(1.0D, var53).color(var28, var29, var30, 1.0F).endVertex();
      var21.pos(var0 + var33, var2 + (double)var12, var4 + var35).tex(0.0D, var53).color(var28, var29, var30, 1.0F).endVertex();
      var21.pos(var0 + var33, var2 + (double)var19, var4 + var35).tex(0.0D, var55).color(var28, var29, var30, 1.0F).endVertex();
      var20.draw();
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      GlStateManager.depthMask(false);
      var31 = 0.5D - var17;
      var33 = 0.5D - var17;
      var35 = 0.5D + var17;
      var37 = 0.5D - var17;
      var39 = 0.5D - var17;
      var41 = 0.5D + var17;
      var43 = 0.5D + var17;
      var45 = 0.5D + var17;
      var47 = 0.0D;
      var49 = 1.0D;
      var51 = -1.0D + var26;
      var53 = (double)var13 * var8 + var51;
      var21.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      var21.pos(var0 + var31, var2 + (double)var19, var4 + var33).tex(1.0D, var53).color(var28, var29, var30, 0.125F).endVertex();
      var21.pos(var0 + var31, var2 + (double)var12, var4 + var33).tex(1.0D, var51).color(var28, var29, var30, 0.125F).endVertex();
      var21.pos(var0 + var35, var2 + (double)var12, var4 + var37).tex(0.0D, var51).color(var28, var29, var30, 0.125F).endVertex();
      var21.pos(var0 + var35, var2 + (double)var19, var4 + var37).tex(0.0D, var53).color(var28, var29, var30, 0.125F).endVertex();
      var21.pos(var0 + var43, var2 + (double)var19, var4 + var45).tex(1.0D, var53).color(var28, var29, var30, 0.125F).endVertex();
      var21.pos(var0 + var43, var2 + (double)var12, var4 + var45).tex(1.0D, var51).color(var28, var29, var30, 0.125F).endVertex();
      var21.pos(var0 + var39, var2 + (double)var12, var4 + var41).tex(0.0D, var51).color(var28, var29, var30, 0.125F).endVertex();
      var21.pos(var0 + var39, var2 + (double)var19, var4 + var41).tex(0.0D, var53).color(var28, var29, var30, 0.125F).endVertex();
      var21.pos(var0 + var35, var2 + (double)var19, var4 + var37).tex(1.0D, var53).color(var28, var29, var30, 0.125F).endVertex();
      var21.pos(var0 + var35, var2 + (double)var12, var4 + var37).tex(1.0D, var51).color(var28, var29, var30, 0.125F).endVertex();
      var21.pos(var0 + var43, var2 + (double)var12, var4 + var45).tex(0.0D, var51).color(var28, var29, var30, 0.125F).endVertex();
      var21.pos(var0 + var43, var2 + (double)var19, var4 + var45).tex(0.0D, var53).color(var28, var29, var30, 0.125F).endVertex();
      var21.pos(var0 + var39, var2 + (double)var19, var4 + var41).tex(1.0D, var53).color(var28, var29, var30, 0.125F).endVertex();
      var21.pos(var0 + var39, var2 + (double)var12, var4 + var41).tex(1.0D, var51).color(var28, var29, var30, 0.125F).endVertex();
      var21.pos(var0 + var31, var2 + (double)var12, var4 + var33).tex(0.0D, var51).color(var28, var29, var30, 0.125F).endVertex();
      var21.pos(var0 + var31, var2 + (double)var19, var4 + var33).tex(0.0D, var53).color(var28, var29, var30, 0.125F).endVertex();
      var20.draw();
      GlStateManager.enableLighting();
      GlStateManager.enableTexture2D();
      GlStateManager.depthMask(true);
   }

   public boolean isGlobalRenderer(TileEntityBeacon var1) {
      return true;
   }
}
