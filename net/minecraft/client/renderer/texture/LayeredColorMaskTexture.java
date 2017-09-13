package net.minecraft.client.renderer.texture;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.util.List;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class LayeredColorMaskTexture extends AbstractTexture {
   private static final Logger LOG = LogManager.getLogger();
   private final ResourceLocation textureLocation;
   private final List listTextures;
   private final List listDyeColors;

   public LayeredColorMaskTexture(ResourceLocation var1, List var2, List var3) {
      this.textureLocation = textureLocationIn;
      this.listTextures = p_i46101_2_;
      this.listDyeColors = p_i46101_3_;
   }

   public void loadTexture(IResourceManager var1) throws IOException {
      this.deleteGlTexture();
      IResource iresource = null;

      BufferedImage bufferedimage;
      label237: {
         try {
            iresource = resourceManager.getResource(this.textureLocation);
            BufferedImage bufferedimage1 = TextureUtil.readBufferedImage(iresource.getInputStream());
            int i = bufferedimage1.getType();
            if (i == 0) {
               i = 6;
            }

            bufferedimage = new BufferedImage(bufferedimage1.getWidth(), bufferedimage1.getHeight(), i);
            Graphics graphics = bufferedimage.getGraphics();
            graphics.drawImage(bufferedimage1, 0, 0, (ImageObserver)null);
            int j = 0;

            while(true) {
               if (j >= 17 || j >= this.listTextures.size() || j >= this.listDyeColors.size()) {
                  break label237;
               }

               IResource iresource1 = null;

               try {
                  String s = (String)this.listTextures.get(j);
                  MapColor mapcolor = ((EnumDyeColor)this.listDyeColors.get(j)).getMapColor();
                  if (s != null) {
                     iresource1 = resourceManager.getResource(new ResourceLocation(s));
                     BufferedImage bufferedimage2 = TextureUtil.readBufferedImage(iresource1.getInputStream());
                     if (bufferedimage2.getWidth() == bufferedimage.getWidth() && bufferedimage2.getHeight() == bufferedimage.getHeight() && bufferedimage2.getType() == 6) {
                        for(int k = 0; k < bufferedimage2.getHeight(); ++k) {
                           for(int l = 0; l < bufferedimage2.getWidth(); ++l) {
                              int i1 = bufferedimage2.getRGB(l, k);
                              if ((i1 & -16777216) != 0) {
                                 int j1 = (i1 & 16711680) << 8 & -16777216;
                                 int k1 = bufferedimage1.getRGB(l, k);
                                 int l1 = MathHelper.multiplyColor(k1, mapcolor.colorValue) & 16777215;
                                 bufferedimage2.setRGB(l, k, j1 | l1);
                              }
                           }
                        }

                        bufferedimage.getGraphics().drawImage(bufferedimage2, 0, 0, (ImageObserver)null);
                     }
                  }
               } finally {
                  IOUtils.closeQuietly(iresource1);
               }

               ++j;
            }
         } catch (IOException var27) {
            LOG.error("Couldn't load layered image", var27);
         } finally {
            IOUtils.closeQuietly(iresource);
         }

         return;
      }

      TextureUtil.uploadTextureImage(this.getGlTextureId(), bufferedimage);
   }
}
