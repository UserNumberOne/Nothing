package net.minecraft.client.renderer.texture;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.SplashProgress;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class TextureUtil {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final IntBuffer DATA_BUFFER = GLAllocation.createDirectIntBuffer(4194304);
   public static final DynamicTexture MISSING_TEXTURE = new DynamicTexture(16, 16);
   public static final int[] MISSING_TEXTURE_DATA = MISSING_TEXTURE.getTextureData();
   private static final float[] COLOR_GAMMAS;
   private static final int[] MIPMAP_BUFFER;

   private static float getColorGamma(int var0) {
      return COLOR_GAMMAS[var0 & 255];
   }

   public static int glGenTextures() {
      return GlStateManager.generateTexture();
   }

   public static void deleteTexture(int var0) {
      GlStateManager.deleteTexture(var0);
   }

   public static int uploadTextureImage(int var0, BufferedImage var1) {
      return uploadTextureImageAllocate(var0, var1, false, false);
   }

   public static void uploadTexture(int var0, int[] var1, int var2, int var3) {
      bindTexture(var0);
      uploadTextureSub(0, var1, var2, var3, 0, 0, false, false, false);
   }

   public static int[][] generateMipmapData(int var0, int var1, int[][] var2) {
      int[][] var3 = new int[var0 + 1][];
      var3[0] = var2[0];
      if (var0 > 0) {
         boolean var4 = false;

         for(int var5 = 0; var5 < var2.length; ++var5) {
            if (var2[0][var5] >> 24 == 0) {
               var4 = true;
               break;
            }
         }

         for(int var14 = 1; var14 <= var0; ++var14) {
            if (var2[var14] != null) {
               var3[var14] = var2[var14];
            } else {
               int[] var6 = var3[var14 - 1];
               int[] var7 = new int[var6.length >> 2];
               int var8 = var1 >> var14;
               if (var8 > 0) {
                  int var9 = var7.length / var8;
                  int var10 = var8 << 1;

                  for(int var11 = 0; var11 < var8; ++var11) {
                     for(int var12 = 0; var12 < var9; ++var12) {
                        int var13 = 2 * (var11 + var12 * var10);
                        var7[var11 + var12 * var8] = blendColors(var6[var13 + 0], var6[var13 + 1], var6[var13 + 0 + var10], var6[var13 + 1 + var10], var4);
                     }
                  }
               }

               var3[var14] = var7;
            }
         }
      }

      return var3;
   }

   private static int blendColors(int var0, int var1, int var2, int var3, boolean var4) {
      if (var4) {
         MIPMAP_BUFFER[0] = var0;
         MIPMAP_BUFFER[1] = var1;
         MIPMAP_BUFFER[2] = var2;
         MIPMAP_BUFFER[3] = var3;
         float var13 = 0.0F;
         float var15 = 0.0F;
         float var17 = 0.0F;
         float var19 = 0.0F;

         for(int var9 = 0; var9 < 4; ++var9) {
            if (MIPMAP_BUFFER[var9] >> 24 != 0) {
               var13 += getColorGamma(MIPMAP_BUFFER[var9] >> 24);
               var15 += getColorGamma(MIPMAP_BUFFER[var9] >> 16);
               var17 += getColorGamma(MIPMAP_BUFFER[var9] >> 8);
               var19 += getColorGamma(MIPMAP_BUFFER[var9] >> 0);
            }
         }

         var13 = var13 / 4.0F;
         var15 = var15 / 4.0F;
         var17 = var17 / 4.0F;
         var19 = var19 / 4.0F;
         int var21 = (int)(Math.pow((double)var13, 0.45454545454545453D) * 255.0D);
         int var10 = (int)(Math.pow((double)var15, 0.45454545454545453D) * 255.0D);
         int var11 = (int)(Math.pow((double)var17, 0.45454545454545453D) * 255.0D);
         int var12 = (int)(Math.pow((double)var19, 0.45454545454545453D) * 255.0D);
         if (var21 < 96) {
            var21 = 0;
         }

         return var21 << 24 | var10 << 16 | var11 << 8 | var12;
      } else {
         int var5 = blendColorComponent(var0, var1, var2, var3, 24);
         int var6 = blendColorComponent(var0, var1, var2, var3, 16);
         int var7 = blendColorComponent(var0, var1, var2, var3, 8);
         int var8 = blendColorComponent(var0, var1, var2, var3, 0);
         return var5 << 24 | var6 << 16 | var7 << 8 | var8;
      }
   }

   private static int blendColorComponent(int var0, int var1, int var2, int var3, int var4) {
      float var5 = getColorGamma(var0 >> var4);
      float var6 = getColorGamma(var1 >> var4);
      float var7 = getColorGamma(var2 >> var4);
      float var8 = getColorGamma(var3 >> var4);
      float var9 = (float)((double)((float)Math.pow((double)(var5 + var6 + var7 + var8) * 0.25D, 0.45454545454545453D)));
      return (int)((double)var9 * 255.0D);
   }

   public static void uploadTextureMipmap(int[][] var0, int var1, int var2, int var3, int var4, boolean var5, boolean var6) {
      for(int var7 = 0; var7 < var0.length; ++var7) {
         int[] var8 = var0[var7];
         if (var1 >> var7 <= 0 || var2 >> var7 <= 0) {
            break;
         }

         uploadTextureSub(var7, var8, var1 >> var7, var2 >> var7, var3 >> var7, var4 >> var7, var5, var6, var0.length > 1);
      }

   }

   private static void uploadTextureSub(int var0, int[] var1, int var2, int var3, int var4, int var5, boolean var6, boolean var7, boolean var8) {
      int var9 = 4194304 / var2;
      setTextureBlurMipmap(var6, var8);
      setTextureClamped(var7);

      int var10;
      for(int var11 = 0; var11 < var2 * var3; var11 += var2 * var10) {
         int var12 = var11 / var2;
         var10 = Math.min(var9, var3 - var12);
         int var13 = var2 * var10;
         copyToBufferPos(var1, var11, var13);
         GlStateManager.glTexSubImage2D(3553, var0, var4, var5 + var12, var2, var10, 32993, 33639, DATA_BUFFER);
      }

   }

   public static int uploadTextureImageAllocate(int var0, BufferedImage var1, boolean var2, boolean var3) {
      allocateTexture(var0, var1.getWidth(), var1.getHeight());
      return uploadTextureImageSub(var0, var1, 0, 0, var2, var3);
   }

   public static void allocateTexture(int var0, int var1, int var2) {
      allocateTextureImpl(var0, 0, var1, var2);
   }

   public static void allocateTextureImpl(int var0, int var1, int var2, int var3) {
      synchronized(SplashProgress.class) {
         deleteTexture(var0);
         bindTexture(var0);
      }

      if (var1 >= 0) {
         GlStateManager.glTexParameteri(3553, 33085, var1);
         GlStateManager.glTexParameteri(3553, 33082, 0);
         GlStateManager.glTexParameteri(3553, 33083, var1);
         GlStateManager.glTexParameterf(3553, 34049, 0.0F);
      }

      for(int var7 = 0; var7 <= var1; ++var7) {
         GlStateManager.glTexImage2D(3553, var7, 6408, var2 >> var7, var3 >> var7, 0, 32993, 33639, (IntBuffer)null);
      }

   }

   public static int uploadTextureImageSub(int var0, BufferedImage var1, int var2, int var3, boolean var4, boolean var5) {
      bindTexture(var0);
      uploadTextureImageSubImpl(var1, var2, var3, var4, var5);
      return var0;
   }

   private static void uploadTextureImageSubImpl(BufferedImage var0, int var1, int var2, boolean var3, boolean var4) {
      int var5 = var0.getWidth();
      int var6 = var0.getHeight();
      int var7 = 4194304 / var5;
      int[] var8 = new int[var7 * var5];
      setTextureBlurred(var3);
      setTextureClamped(var4);

      for(int var9 = 0; var9 < var5 * var6; var9 += var5 * var7) {
         int var10 = var9 / var5;
         int var11 = Math.min(var7, var6 - var10);
         int var12 = var5 * var11;
         var0.getRGB(0, var10, var5, var11, var8, 0, var5);
         copyToBuffer(var8, var12);
         GlStateManager.glTexSubImage2D(3553, 0, var1, var2 + var10, var5, var11, 32993, 33639, DATA_BUFFER);
      }

   }

   private static void setTextureClamped(boolean var0) {
      if (var0) {
         GlStateManager.glTexParameteri(3553, 10242, 10496);
         GlStateManager.glTexParameteri(3553, 10243, 10496);
      } else {
         GlStateManager.glTexParameteri(3553, 10242, 10497);
         GlStateManager.glTexParameteri(3553, 10243, 10497);
      }

   }

   private static void setTextureBlurred(boolean var0) {
      setTextureBlurMipmap(var0, false);
   }

   private static void setTextureBlurMipmap(boolean var0, boolean var1) {
      if (var0) {
         GlStateManager.glTexParameteri(3553, 10241, var1 ? 9987 : 9729);
         GlStateManager.glTexParameteri(3553, 10240, 9729);
      } else {
         GlStateManager.glTexParameteri(3553, 10241, var1 ? 9986 : 9728);
         GlStateManager.glTexParameteri(3553, 10240, 9728);
      }

   }

   private static void copyToBuffer(int[] var0, int var1) {
      copyToBufferPos(var0, 0, var1);
   }

   private static void copyToBufferPos(int[] var0, int var1, int var2) {
      int[] var3 = var0;
      if (Minecraft.getMinecraft().gameSettings.anaglyph) {
         var3 = updateAnaglyph(var0);
      }

      DATA_BUFFER.clear();
      DATA_BUFFER.put(var3, var1, var2);
      DATA_BUFFER.position(0).limit(var2);
   }

   static void bindTexture(int var0) {
      GlStateManager.bindTexture(var0);
   }

   public static int[] readImageData(IResourceManager var0, ResourceLocation var1) throws IOException {
      IResource var2 = null;

      int[] var3;
      try {
         var2 = var0.getResource(var1);
         BufferedImage var4 = readBufferedImage(var2.getInputStream());
         int var5 = var4.getWidth();
         int var6 = var4.getHeight();
         int[] var7 = new int[var5 * var6];
         var4.getRGB(0, 0, var5, var6, var7, 0, var5);
         var3 = var7;
      } finally {
         IOUtils.closeQuietly(var2);
      }

      return var3;
   }

   public static BufferedImage readBufferedImage(InputStream var0) throws IOException {
      BufferedImage var1;
      try {
         var1 = ImageIO.read(var0);
      } finally {
         IOUtils.closeQuietly(var0);
      }

      return var1;
   }

   public static int[] updateAnaglyph(int[] var0) {
      int[] var1 = new int[var0.length];

      for(int var2 = 0; var2 < var0.length; ++var2) {
         var1[var2] = anaglyphColor(var0[var2]);
      }

      return var1;
   }

   public static int anaglyphColor(int var0) {
      int var1 = var0 >> 24 & 255;
      int var2 = var0 >> 16 & 255;
      int var3 = var0 >> 8 & 255;
      int var4 = var0 & 255;
      int var5 = (var2 * 30 + var3 * 59 + var4 * 11) / 100;
      int var6 = (var2 * 30 + var3 * 70) / 100;
      int var7 = (var2 * 30 + var4 * 70) / 100;
      return var1 << 24 | var5 << 16 | var6 << 8 | var7;
   }

   public static void processPixelValues(int[] var0, int var1, int var2) {
      int[] var3 = new int[var1];
      int var4 = var2 / 2;

      for(int var5 = 0; var5 < var4; ++var5) {
         System.arraycopy(var0, var5 * var1, var3, 0, var1);
         System.arraycopy(var0, (var2 - 1 - var5) * var1, var0, var5 * var1, var1);
         System.arraycopy(var3, 0, var0, (var2 - 1 - var5) * var1, var1);
      }

   }

   static {
      int var0 = -16777216;
      int var1 = -524040;
      int[] var2 = new int[]{-524040, -524040, -524040, -524040, -524040, -524040, -524040, -524040};
      int[] var3 = new int[]{-16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216};
      int var4 = var2.length;

      for(int var5 = 0; var5 < 16; ++var5) {
         System.arraycopy(var5 < var4 ? var2 : var3, 0, MISSING_TEXTURE_DATA, 16 * var5, var4);
         System.arraycopy(var5 < var4 ? var3 : var2, 0, MISSING_TEXTURE_DATA, 16 * var5 + var4, var4);
      }

      MISSING_TEXTURE.updateDynamicTexture();
      COLOR_GAMMAS = new float[256];

      for(int var6 = 0; var6 < COLOR_GAMMAS.length; ++var6) {
         COLOR_GAMMAS[var6] = (float)Math.pow((double)((float)var6 / 255.0F), 2.2D);
      }

      MIPMAP_BUFFER = new int[4];
   }
}
