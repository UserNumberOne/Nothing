package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class LayeredTexture extends AbstractTexture {
   private static final Logger LOGGER = LogManager.getLogger();
   public final List layeredTextureNames;

   public LayeredTexture(String... var1) {
      this.layeredTextureNames = Lists.newArrayList(textureNames);
   }

   public void loadTexture(IResourceManager var1) throws IOException {
      this.deleteGlTexture();
      BufferedImage bufferedimage = null;

      for(String s : this.layeredTextureNames) {
         IResource iresource = null;

         try {
            if (s != null) {
               iresource = resourceManager.getResource(new ResourceLocation(s));
               BufferedImage bufferedimage1 = TextureUtil.readBufferedImage(iresource.getInputStream());
               if (bufferedimage == null) {
                  bufferedimage = new BufferedImage(bufferedimage1.getWidth(), bufferedimage1.getHeight(), 2);
               }

               bufferedimage.getGraphics().drawImage(bufferedimage1, 0, 0, (ImageObserver)null);
            }
            continue;
         } catch (IOException var10) {
            LOGGER.error("Couldn't load layered image", var10);
         } finally {
            IOUtils.closeQuietly(iresource);
         }

         return;
      }

      TextureUtil.uploadTextureImage(this.getGlTextureId(), bufferedimage);
   }
}
