package net.minecraft.client;

import com.google.common.base.Throwables;
import java.io.IOException;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MinecraftError;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LoadingScreenRenderer implements IProgressUpdate {
   private String message = "";
   private final Minecraft mc;
   private String currentlyDisplayedText = "";
   private long systemTime = Minecraft.getSystemTime();
   private boolean loadingSuccess;
   private final ScaledResolution scaledResolution;
   private final Framebuffer framebuffer;

   public LoadingScreenRenderer(Minecraft var1) {
      this.mc = var1;
      this.scaledResolution = new ScaledResolution(var1);
      this.framebuffer = new Framebuffer(var1.displayWidth, var1.displayHeight, false);
      this.framebuffer.setFramebufferFilter(9728);
   }

   public void resetProgressAndMessage(String var1) {
      this.loadingSuccess = false;
      this.displayString(var1);
   }

   public void displaySavingString(String var1) {
      this.loadingSuccess = true;
      this.displayString(var1);
   }

   private void displayString(String var1) {
      this.currentlyDisplayedText = var1;
      if (!this.mc.running) {
         if (!this.loadingSuccess) {
            throw new MinecraftError();
         }
      } else {
         GlStateManager.clear(256);
         GlStateManager.matrixMode(5889);
         GlStateManager.loadIdentity();
         if (OpenGlHelper.isFramebufferEnabled()) {
            int var2 = this.scaledResolution.getScaleFactor();
            GlStateManager.ortho(0.0D, (double)(this.scaledResolution.getScaledWidth() * var2), (double)(this.scaledResolution.getScaledHeight() * var2), 0.0D, 100.0D, 300.0D);
         } else {
            ScaledResolution var3 = new ScaledResolution(this.mc);
            GlStateManager.ortho(0.0D, var3.getScaledWidth_double(), var3.getScaledHeight_double(), 0.0D, 100.0D, 300.0D);
         }

         GlStateManager.matrixMode(5888);
         GlStateManager.loadIdentity();
         GlStateManager.translate(0.0F, 0.0F, -200.0F);
      }

   }

   public void displayLoadingString(String var1) {
      if (!this.mc.running) {
         if (!this.loadingSuccess) {
            throw new MinecraftError();
         }
      } else {
         this.systemTime = 0L;
         this.message = var1;
         this.setLoadingProgress(-1);
         this.systemTime = 0L;
      }

   }

   public void setLoadingProgress(int var1) {
      if (!this.mc.running) {
         if (!this.loadingSuccess) {
            throw new MinecraftError();
         }
      } else {
         long var2 = Minecraft.getSystemTime();
         if (var2 - this.systemTime >= 100L) {
            this.systemTime = var2;
            ScaledResolution var4 = new ScaledResolution(this.mc);
            int var5 = var4.getScaleFactor();
            int var6 = var4.getScaledWidth();
            int var7 = var4.getScaledHeight();
            if (OpenGlHelper.isFramebufferEnabled()) {
               this.framebuffer.framebufferClear();
            } else {
               GlStateManager.clear(256);
            }

            this.framebuffer.bindFramebuffer(false);
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0D, var4.getScaledWidth_double(), var4.getScaledHeight_double(), 0.0D, 100.0D, 300.0D);
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.0F, 0.0F, -200.0F);
            if (!OpenGlHelper.isFramebufferEnabled()) {
               GlStateManager.clear(16640);
            }

            try {
               if (!FMLClientHandler.instance().handleLoadingScreen(var4)) {
                  Tessellator var8 = Tessellator.getInstance();
                  VertexBuffer var9 = var8.getBuffer();
                  this.mc.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
                  float var10 = 32.0F;
                  var9.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                  var9.pos(0.0D, (double)var7, 0.0D).tex(0.0D, (double)((float)var7 / 32.0F)).color(64, 64, 64, 255).endVertex();
                  var9.pos((double)var6, (double)var7, 0.0D).tex((double)((float)var6 / 32.0F), (double)((float)var7 / 32.0F)).color(64, 64, 64, 255).endVertex();
                  var9.pos((double)var6, 0.0D, 0.0D).tex((double)((float)var6 / 32.0F), 0.0D).color(64, 64, 64, 255).endVertex();
                  var9.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.0D).color(64, 64, 64, 255).endVertex();
                  var8.draw();
                  if (var1 >= 0) {
                     boolean var11 = true;
                     boolean var12 = true;
                     int var13 = var6 / 2 - 50;
                     int var14 = var7 / 2 + 16;
                     GlStateManager.disableTexture2D();
                     var9.begin(7, DefaultVertexFormats.POSITION_COLOR);
                     var9.pos((double)var13, (double)var14, 0.0D).color(128, 128, 128, 255).endVertex();
                     var9.pos((double)var13, (double)(var14 + 2), 0.0D).color(128, 128, 128, 255).endVertex();
                     var9.pos((double)(var13 + 100), (double)(var14 + 2), 0.0D).color(128, 128, 128, 255).endVertex();
                     var9.pos((double)(var13 + 100), (double)var14, 0.0D).color(128, 128, 128, 255).endVertex();
                     var9.pos((double)var13, (double)var14, 0.0D).color(128, 255, 128, 255).endVertex();
                     var9.pos((double)var13, (double)(var14 + 2), 0.0D).color(128, 255, 128, 255).endVertex();
                     var9.pos((double)(var13 + var1), (double)(var14 + 2), 0.0D).color(128, 255, 128, 255).endVertex();
                     var9.pos((double)(var13 + var1), (double)var14, 0.0D).color(128, 255, 128, 255).endVertex();
                     var8.draw();
                     GlStateManager.enableTexture2D();
                  }

                  GlStateManager.enableBlend();
                  GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                  this.mc.fontRendererObj.drawStringWithShadow(this.currentlyDisplayedText, (float)((var6 - this.mc.fontRendererObj.getStringWidth(this.currentlyDisplayedText)) / 2), (float)(var7 / 2 - 4 - 16), 16777215);
                  this.mc.fontRendererObj.drawStringWithShadow(this.message, (float)((var6 - this.mc.fontRendererObj.getStringWidth(this.message)) / 2), (float)(var7 / 2 - 4 + 8), 16777215);
               }
            } catch (IOException var16) {
               Throwables.propagate(var16);
            }

            this.framebuffer.unbindFramebuffer();
            if (OpenGlHelper.isFramebufferEnabled()) {
               this.framebuffer.framebufferRender(var6 * var5, var7 * var5);
            }

            this.mc.updateDisplay();

            try {
               Thread.yield();
            } catch (Exception var15) {
               ;
            }
         }
      }

   }

   public void setDoneWorking() {
   }
}
