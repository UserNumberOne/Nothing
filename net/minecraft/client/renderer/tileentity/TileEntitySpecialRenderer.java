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
      ITextComponent itextcomponent = te.getDisplayName();
      if (itextcomponent != null && this.rendererDispatcher.cameraHitResult != null && te.getPos().equals(this.rendererDispatcher.cameraHitResult.getBlockPos())) {
         this.setLightmapDisabled(true);
         this.drawNameplate(te, itextcomponent.getFormattedText(), x, y, z, 12);
         this.setLightmapDisabled(false);
      }

   }

   protected void setLightmapDisabled(boolean var1) {
      GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
      if (disabled) {
         GlStateManager.disableTexture2D();
      } else {
         GlStateManager.enableTexture2D();
      }

      GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
   }

   protected void bindTexture(ResourceLocation var1) {
      TextureManager texturemanager = this.rendererDispatcher.renderEngine;
      if (texturemanager != null) {
         texturemanager.bindTexture(location);
      }

   }

   protected World getWorld() {
      return this.rendererDispatcher.world;
   }

   public void setRendererDispatcher(TileEntityRendererDispatcher var1) {
      this.rendererDispatcher = rendererDispatcherIn;
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
      Entity entity = this.rendererDispatcher.entity;
      double d0 = te.getDistanceSq(entity.posX, entity.posY, entity.posZ);
      if (d0 <= (double)(maxDistance * maxDistance)) {
         float f = this.rendererDispatcher.entityYaw;
         float f1 = this.rendererDispatcher.entityPitch;
         boolean flag = false;
         EntityRenderer.drawNameplate(this.getFontRenderer(), str, (float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F, 0, f, f1, false, false);
      }

   }
}
