package net.minecraft.client.renderer.tileentity;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class TileEntitySpecialRenderer {
   protected static final ResourceLocation[] DESTROY_STAGES = new ResourceLocation[]{new ResourceLocation("textures/blocks/destroy_stage_0.png"), new ResourceLocation("textures/blocks/destroy_stage_1.png"), new ResourceLocation("textures/blocks/destroy_stage_2.png"), new ResourceLocation("textures/blocks/destroy_stage_3.png"), new ResourceLocation("textures/blocks/destroy_stage_4.png"), new ResourceLocation("textures/blocks/destroy_stage_5.png"), new ResourceLocation("textures/blocks/destroy_stage_6.png"), new ResourceLocation("textures/blocks/destroy_stage_7.png"), new ResourceLocation("textures/blocks/destroy_stage_8.png"), new ResourceLocation("textures/blocks/destroy_stage_9.png")};
   protected TileEntityRendererDispatcher rendererDispatcher;

   public void renderTileEntityAt(TileEntity var1, double var2, double var4, double var6, float var8, int var9) {
      ITextComponent var10 = var1.getDisplayName();
      if (var10 != null && this.rendererDispatcher.cameraHitResult != null && var1.getPos().equals(this.rendererDispatcher.cameraHitResult.getBlockPos())) {
         this.setLightmapDisabled(true);
         this.drawNameplate(var1, var10.getFormattedText(), var2, var4, var6, 12);
         this.setLightmapDisabled(false);
      }

   }

   protected void setLightmapDisabled(boolean var1) {
      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      if (var1) {
         GlStateManager.disableTexture2D();
      } else {
         GlStateManager.enableTexture2D();
      }

      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
   }

   protected void bindTexture(ResourceLocation var1) {
      TextureManager var2 = this.rendererDispatcher.renderEngine;
      if (var2 != null) {
         var2.bindTexture(var1);
      }

   }

   protected World getWorld() {
      return this.rendererDispatcher.world;
   }

   public void setRendererDispatcher(TileEntityRendererDispatcher var1) {
      this.rendererDispatcher = var1;
   }

   public FontRenderer getFontRenderer() {
      return this.rendererDispatcher.getFontRenderer();
   }

   public boolean isGlobalRenderer(TileEntity var1) {
      return false;
   }

   public void renderTileEntityFast(TileEntity var1, double var2, double var4, double var6, float var8, int var9, VertexBuffer var10) {
   }

   protected void drawNameplate(TileEntity var1, String var2, double var3, double var5, double var7, int var9) {
      Entity var10 = this.rendererDispatcher.entity;
      double var11 = var1.getDistanceSq(var10.posX, var10.posY, var10.posZ);
      if (var11 <= (double)(var9 * var9)) {
         float var13 = this.rendererDispatcher.entityYaw;
         float var14 = this.rendererDispatcher.entityPitch;
         boolean var15 = false;
         EntityRenderer.drawNameplate(this.getFontRenderer(), var2, (float)var3 + 0.5F, (float)var5 + 1.5F, (float)var7 + 0.5F, 0, var13, var14, false, false);
      }

   }
}
