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
      this.layeredTextureNames = Lists.newArrayList(var1);
   }

   public void loadTexture(IResourceManager var1) throws IOException {
      this.deleteGlTexture();
      BufferedImage var2 = null;

      for(String var4 : this.layeredTextureNames) {
         IResource var5 = null;

         try {
            if (var4 != null) {
               var5 = var1.getResource(new ResourceLocation(var4));
               BufferedImage var6 = TextureUtil.readBufferedImage(var5.getInputStream());
               if (var2 == null) {
                  var2 = new BufferedImage(var6.getWidth(), var6.getHeight(), 2);
               }

               var2.getGraphics().drawImage(var6, 0, 0, (ImageObserver)null);
            }
            continue;
         } catch (IOException var10) {
            LOGGER.error("Couldn't load layered image", var10);
         } finally {
            IOUtils.closeQuietly(var5);
         }

         return;
      }

      TextureUtil.uploadTextureImage(this.getGlTextureId(), var2);
   }
}
