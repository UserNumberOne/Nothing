package net.minecraft.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.IntBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ScreenshotEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;

@SideOnly(Side.CLIENT)
public class ScreenShotHelper {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
   private static IntBuffer pixelBuffer;
   private static int[] pixelValues;

   public static ITextComponent saveScreenshot(File var0, int var1, int var2, Framebuffer var3) {
      return saveScreenshot(var0, (String)null, var1, var2, var3);
   }

   public static ITextComponent saveScreenshot(File var0, String var1, int var2, int var3, Framebuffer var4) {
      try {
         File var5 = new File(var0, "screenshots");
         var5.mkdir();
         BufferedImage var6 = createScreenshot(var2, var3, var4);
         File var7;
         if (var1 == null) {
            var7 = getTimestampedPNGFileForDirectory(var5);
         } else {
            var7 = new File(var5, var1);
         }

         var7 = var7.getCanonicalFile();
         ScreenshotEvent var8 = ForgeHooksClient.onScreenshot(var6, var7);
         if (var8.isCanceled()) {
            return var8.getCancelMessage();
         } else {
            var7 = var8.getScreenshotFile();
            ImageIO.write(var6, "png", var7);
            TextComponentString var9 = new TextComponentString(var7.getName());
            var9.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, var7.getAbsolutePath()));
            var9.getStyle().setUnderlined(Boolean.valueOf(true));
            return (ITextComponent)(var8.getResultMessage() != null ? var8.getResultMessage() : new TextComponentTranslation("screenshot.success", new Object[]{var9}));
         }
      } catch (Exception var10) {
         LOGGER.warn("Couldn't save screenshot", var10);
         return new TextComponentTranslation("screenshot.failure", new Object[]{var10.getMessage()});
      }
   }

   public static BufferedImage createScreenshot(int var0, int var1, Framebuffer var2) {
      if (OpenGlHelper.isFramebufferEnabled()) {
         var0 = var2.framebufferTextureWidth;
         var1 = var2.framebufferTextureHeight;
      }

      int var3 = var0 * var1;
      if (pixelBuffer == null || pixelBuffer.capacity() < var3) {
         pixelBuffer = BufferUtils.createIntBuffer(var3);
         pixelValues = new int[var3];
      }

      GlStateManager.glPixelStorei(3333, 1);
      GlStateManager.glPixelStorei(3317, 1);
      pixelBuffer.clear();
      if (OpenGlHelper.isFramebufferEnabled()) {
         GlStateManager.bindTexture(var2.framebufferTexture);
         GlStateManager.glGetTexImage(3553, 0, 32993, 33639, pixelBuffer);
      } else {
         GlStateManager.glReadPixels(0, 0, var0, var1, 32993, 33639, pixelBuffer);
      }

      pixelBuffer.get(pixelValues);
      TextureUtil.processPixelValues(pixelValues, var0, var1);
      BufferedImage var4;
      if (OpenGlHelper.isFramebufferEnabled()) {
         var4 = new BufferedImage(var2.framebufferWidth, var2.framebufferHeight, 1);
         int var5 = var2.framebufferTextureHeight - var2.framebufferHeight;

         for(int var6 = var5; var6 < var2.framebufferTextureHeight; ++var6) {
            for(int var7 = 0; var7 < var2.framebufferWidth; ++var7) {
               var4.setRGB(var7, var6 - var5, pixelValues[var6 * var2.framebufferTextureWidth + var7]);
            }
         }
      } else {
         var4 = new BufferedImage(var0, var1, 1);
         var4.setRGB(0, 0, var0, var1, pixelValues, 0, var0);
      }

      return var4;
   }

   private static File getTimestampedPNGFileForDirectory(File var0) {
      String var1 = DATE_FORMAT.format(new Date()).toString();
      int var2 = 1;

      while(true) {
         File var3 = new File(var0, var1 + (var2 == 1 ? "" : "_" + var2) + ".png");
         if (!var3.exists()) {
            return var3;
         }

         ++var2;
      }
   }
}
