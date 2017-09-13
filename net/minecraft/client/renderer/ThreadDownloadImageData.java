package net.minecraft.client.renderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class ThreadDownloadImageData extends SimpleTexture {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final AtomicInteger TEXTURE_DOWNLOADER_THREAD_ID = new AtomicInteger(0);
   @Nullable
   private final File cacheFile;
   private final String imageUrl;
   @Nullable
   private final IImageBuffer imageBuffer;
   @Nullable
   private BufferedImage bufferedImage;
   @Nullable
   private Thread imageThread;
   private boolean textureUploaded;

   public ThreadDownloadImageData(@Nullable File var1, String var2, ResourceLocation var3, @Nullable IImageBuffer var4) {
      super(textureResourceLocation);
      this.cacheFile = cacheFileIn;
      this.imageUrl = imageUrlIn;
      this.imageBuffer = imageBufferIn;
   }

   private void checkTextureUploaded() {
      if (!this.textureUploaded && this.bufferedImage != null) {
         if (this.textureLocation != null) {
            this.deleteGlTexture();
         }

         TextureUtil.uploadTextureImage(super.getGlTextureId(), this.bufferedImage);
         this.textureUploaded = true;
      }

   }

   public int getGlTextureId() {
      this.checkTextureUploaded();
      return super.getGlTextureId();
   }

   public void setBufferedImage(BufferedImage var1) {
      this.bufferedImage = bufferedImageIn;
      if (this.imageBuffer != null) {
         this.imageBuffer.skinAvailable();
      }

   }

   public void loadTexture(IResourceManager var1) throws IOException {
      if (this.bufferedImage == null && this.textureLocation != null) {
         super.loadTexture(resourceManager);
      }

      if (this.imageThread == null) {
         if (this.cacheFile != null && this.cacheFile.isFile()) {
            LOGGER.debug("Loading http texture from local cache ({})", new Object[]{this.cacheFile});

            try {
               this.bufferedImage = ImageIO.read(this.cacheFile);
               if (this.imageBuffer != null) {
                  this.setBufferedImage(this.imageBuffer.parseUserSkin(this.bufferedImage));
               }
            } catch (IOException var3) {
               LOGGER.error("Couldn't load skin {}", new Object[]{this.cacheFile, var3});
               this.loadTextureFromServer();
            }
         } else {
            this.loadTextureFromServer();
         }
      }

   }

   protected void loadTextureFromServer() {
      this.imageThread = new Thread("Texture Downloader #" + TEXTURE_DOWNLOADER_THREAD_ID.incrementAndGet()) {
         public void run() {
            HttpURLConnection httpurlconnection = null;
            ThreadDownloadImageData.LOGGER.debug("Downloading http texture from {} to {}", new Object[]{ThreadDownloadImageData.this.imageUrl, ThreadDownloadImageData.this.cacheFile});

            try {
               httpurlconnection = (HttpURLConnection)(new URL(ThreadDownloadImageData.this.imageUrl)).openConnection(Minecraft.getMinecraft().getProxy());
               httpurlconnection.setDoInput(true);
               httpurlconnection.setDoOutput(false);
               httpurlconnection.connect();
               if (httpurlconnection.getResponseCode() / 100 != 2) {
                  return;
               }

               BufferedImage bufferedimage;
               if (ThreadDownloadImageData.this.cacheFile != null) {
                  FileUtils.copyInputStreamToFile(httpurlconnection.getInputStream(), ThreadDownloadImageData.this.cacheFile);
                  bufferedimage = ImageIO.read(ThreadDownloadImageData.this.cacheFile);
               } else {
                  bufferedimage = TextureUtil.readBufferedImage(httpurlconnection.getInputStream());
               }

               if (ThreadDownloadImageData.this.imageBuffer != null) {
                  bufferedimage = ThreadDownloadImageData.this.imageBuffer.parseUserSkin(bufferedimage);
               }

               ThreadDownloadImageData.this.setBufferedImage(bufferedimage);
            } catch (Exception var6) {
               ThreadDownloadImageData.LOGGER.error("Couldn't download http texture", var6);
               return;
            } finally {
               if (httpurlconnection != null) {
                  httpurlconnection.disconnect();
               }

            }

         }
      };
      this.imageThread.setDaemon(true);
      this.imageThread.start();
   }
}
