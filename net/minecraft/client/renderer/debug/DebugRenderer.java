package net.minecraft.client.renderer.debug;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DebugRenderer {
   public final DebugRenderer.IDebugRenderer debugRendererPathfinding;
   public final DebugRenderer.IDebugRenderer debugRendererWater;
   public final DebugRenderer.IDebugRenderer debugRendererChunkBorder;
   public final DebugRenderer.IDebugRenderer debugRendererHeightMap;
   private boolean chunkBordersEnabled;
   private boolean pathfindingEnabled;
   private boolean waterEnabled;
   private boolean heightmapEnabled;

   public DebugRenderer(Minecraft var1) {
      this.debugRendererPathfinding = new DebugRendererPathfinding(var1);
      this.debugRendererWater = new DebugRendererWater(var1);
      this.debugRendererChunkBorder = new DebugRendererChunkBorder(var1);
      this.debugRendererHeightMap = new DebugRendererHeightMap(var1);
   }

   public boolean shouldRender() {
      return this.chunkBordersEnabled || this.pathfindingEnabled || this.waterEnabled;
   }

   public boolean toggleDebugScreen() {
      this.chunkBordersEnabled = !this.chunkBordersEnabled;
      return this.chunkBordersEnabled;
   }

   public void renderDebug(float var1, long var2) {
      if (this.pathfindingEnabled) {
         this.debugRendererPathfinding.render(var1, var2);
      }

      if (this.chunkBordersEnabled && !Minecraft.getMinecraft().isReducedDebug()) {
         this.debugRendererChunkBorder.render(var1, var2);
      }

      if (this.waterEnabled) {
         this.debugRendererWater.render(var1, var2);
      }

      if (this.heightmapEnabled) {
         this.debugRendererHeightMap.render(var1, var2);
      }

   }

   public static void renderDebugText(String var0, double var1, double var3, double var5, float var7, int var8) {
      Minecraft var9 = Minecraft.getMinecraft();
      if (var9.player != null && var9.getRenderManager() != null && var9.getRenderManager().options != null) {
         FontRenderer var10 = var9.fontRendererObj;
         EntityPlayerSP var11 = var9.player;
         double var12 = var11.lastTickPosX + (var11.posX - var11.lastTickPosX) * (double)var7;
         double var14 = var11.lastTickPosY + (var11.posY - var11.lastTickPosY) * (double)var7;
         double var16 = var11.lastTickPosZ + (var11.posZ - var11.lastTickPosZ) * (double)var7;
         GlStateManager.pushMatrix();
         GlStateManager.translate((float)(var1 - var12), (float)(var3 - var14) + 0.07F, (float)(var5 - var16));
         GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
         GlStateManager.scale(0.02F, -0.02F, 0.02F);
         RenderManager var18 = var9.getRenderManager();
         GlStateManager.rotate(-var18.playerViewY, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate((float)(var18.options.thirdPersonView == 2 ? 1 : -1) * var18.playerViewX, 1.0F, 0.0F, 0.0F);
         GlStateManager.disableLighting();
         GlStateManager.enableTexture2D();
         GlStateManager.enableDepth();
         GlStateManager.depthMask(true);
         GlStateManager.scale(-1.0F, 1.0F, 1.0F);
         var10.drawString(var0, -var10.getStringWidth(var0) / 2, 0, var8);
         GlStateManager.enableLighting();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.popMatrix();
      }

   }

   @SideOnly(Side.CLIENT)
   public interface IDebugRenderer {
      void render(float var1, long var2);
   }
}
