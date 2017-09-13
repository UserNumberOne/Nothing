package net.minecraft.client.renderer.debug;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DebugRendererChunkBorder implements DebugRenderer.IDebugRenderer {
   private final Minecraft minecraft;

   public DebugRendererChunkBorder(Minecraft var1) {
      this.minecraft = var1;
   }

   public void render(float var1, long var2) {
      EntityPlayerSP var4 = this.minecraft.player;
      Tessellator var5 = Tessellator.getInstance();
      VertexBuffer var6 = var5.getBuffer();
      double var7 = var4.lastTickPosX + (var4.posX - var4.lastTickPosX) * (double)var1;
      double var9 = var4.lastTickPosY + (var4.posY - var4.lastTickPosY) * (double)var1;
      double var11 = var4.lastTickPosZ + (var4.posZ - var4.lastTickPosZ) * (double)var1;
      double var13 = 0.0D - var9;
      double var15 = 256.0D - var9;
      GlStateManager.disableTexture2D();
      GlStateManager.disableBlend();
      double var17 = (double)(var4.chunkCoordX << 4) - var7;
      double var19 = (double)(var4.chunkCoordZ << 4) - var11;
      GlStateManager.glLineWidth(1.0F);
      var6.begin(3, DefaultVertexFormats.POSITION_COLOR);

      for(int var21 = -16; var21 <= 32; var21 += 16) {
         for(int var22 = -16; var22 <= 32; var22 += 16) {
            var6.pos(var17 + (double)var21, var13, var19 + (double)var22).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
            var6.pos(var17 + (double)var21, var13, var19 + (double)var22).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
            var6.pos(var17 + (double)var21, var15, var19 + (double)var22).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
            var6.pos(var17 + (double)var21, var15, var19 + (double)var22).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
         }
      }

      for(int var24 = 2; var24 < 16; var24 += 2) {
         var6.pos(var17 + (double)var24, var13, var19).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         var6.pos(var17 + (double)var24, var13, var19).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var6.pos(var17 + (double)var24, var15, var19).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var6.pos(var17 + (double)var24, var15, var19).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         var6.pos(var17 + (double)var24, var13, var19 + 16.0D).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         var6.pos(var17 + (double)var24, var13, var19 + 16.0D).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var6.pos(var17 + (double)var24, var15, var19 + 16.0D).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var6.pos(var17 + (double)var24, var15, var19 + 16.0D).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
      }

      for(int var25 = 2; var25 < 16; var25 += 2) {
         var6.pos(var17, var13, var19 + (double)var25).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         var6.pos(var17, var13, var19 + (double)var25).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var6.pos(var17, var15, var19 + (double)var25).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var6.pos(var17, var15, var19 + (double)var25).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         var6.pos(var17 + 16.0D, var13, var19 + (double)var25).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         var6.pos(var17 + 16.0D, var13, var19 + (double)var25).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var6.pos(var17 + 16.0D, var15, var19 + (double)var25).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var6.pos(var17 + 16.0D, var15, var19 + (double)var25).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
      }

      for(int var26 = 0; var26 <= 256; var26 += 2) {
         double var29 = (double)var26 - var9;
         var6.pos(var17, var29, var19).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
         var6.pos(var17, var29, var19).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var6.pos(var17, var29, var19 + 16.0D).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var6.pos(var17 + 16.0D, var29, var19 + 16.0D).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var6.pos(var17 + 16.0D, var29, var19).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var6.pos(var17, var29, var19).color(1.0F, 1.0F, 0.0F, 1.0F).endVertex();
         var6.pos(var17, var29, var19).color(1.0F, 1.0F, 0.0F, 0.0F).endVertex();
      }

      var5.draw();
      GlStateManager.glLineWidth(2.0F);
      var6.begin(3, DefaultVertexFormats.POSITION_COLOR);

      for(int var27 = 0; var27 <= 16; var27 += 16) {
         for(int var30 = 0; var30 <= 16; var30 += 16) {
            var6.pos(var17 + (double)var27, var13, var19 + (double)var30).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
            var6.pos(var17 + (double)var27, var13, var19 + (double)var30).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var6.pos(var17 + (double)var27, var15, var19 + (double)var30).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
            var6.pos(var17 + (double)var27, var15, var19 + (double)var30).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
         }
      }

      for(int var28 = 0; var28 <= 256; var28 += 16) {
         double var31 = (double)var28 - var9;
         var6.pos(var17, var31, var19).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
         var6.pos(var17, var31, var19).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
         var6.pos(var17, var31, var19 + 16.0D).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
         var6.pos(var17 + 16.0D, var31, var19 + 16.0D).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
         var6.pos(var17 + 16.0D, var31, var19).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
         var6.pos(var17, var31, var19).color(0.25F, 0.25F, 1.0F, 1.0F).endVertex();
         var6.pos(var17, var31, var19).color(0.25F, 0.25F, 1.0F, 0.0F).endVertex();
      }

      var5.draw();
      GlStateManager.glLineWidth(1.0F);
      GlStateManager.enableBlend();
      GlStateManager.enableTexture2D();
   }
}
