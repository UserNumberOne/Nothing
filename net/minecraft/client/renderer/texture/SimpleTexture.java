package net.minecraft.client.renderer.texture;

import java.awt.image.BufferedImage;
import java.io.IOException;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class SimpleTexture extends AbstractTexture {
   private static final Logger LOG = LogManager.getLogger();
   protected final ResourceLocation textureLocation;

   public SimpleTexture(ResourceLocation var1) {
      this.textureLocation = var1;
   }

   public void loadTexture(IResourceManager var1) throws IOException {
      this.deleteGlTexture();
      IResource var2 = null;

      try {
         var2 = var1.getResource(this.textureLocation);
         BufferedImage var3 = TextureUtil.readBufferedImage(var2.getInputStream());
         boolean var4 = false;
         boolean var5 = false;
         if (var2.hasMetadata()) {
            try {
               TextureMetadataSection var6 = (TextureMetadataSection)var2.getMetadata("texture");
               if (var6 != null) {
                  var4 = var6.getTextureBlur();
                  var5 = var6.getTextureClamp();
               }
            } catch (RuntimeException var10) {
               LOG.warn("Failed reading metadata of: {}", new Object[]{this.textureLocation, var10});
            }
         }

         TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), var3, var4, var5);
      } finally {
         IOUtils.closeQuietly(var2);
      }

   }
}
