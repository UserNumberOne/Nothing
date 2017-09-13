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
      this.textureLocation = var1;
      this.listTextures = var2;
      this.listDyeColors = var3;
   }

   public void loadTexture(IResourceManager var1) throws IOException {
      this.deleteGlTexture();
      IResource var2 = null;

      BufferedImage var3;
      label237: {
         try {
            var2 = var1.getResource(this.textureLocation);
            BufferedImage var4 = TextureUtil.readBufferedImage(var2.getInputStream());
            int var5 = var4.getType();
            if (var5 == 0) {
               var5 = 6;
            }

            var3 = new BufferedImage(var4.getWidth(), var4.getHeight(), var5);
            Graphics var6 = var3.getGraphics();
            var6.drawImage(var4, 0, 0, (ImageObserver)null);
            int var7 = 0;

            while(true) {
               if (var7 >= 17 || var7 >= this.listTextures.size() || var7 >= this.listDyeColors.size()) {
                  break label237;
               }

               IResource var8 = null;

               try {
                  String var9 = (String)this.listTextures.get(var7);
                  MapColor var10 = ((EnumDyeColor)this.listDyeColors.get(var7)).getMapColor();
                  if (var9 != null) {
                     var8 = var1.getResource(new ResourceLocation(var9));
                     BufferedImage var11 = TextureUtil.readBufferedImage(var8.getInputStream());
                     if (var11.getWidth() == var3.getWidth() && var11.getHeight() == var3.getHeight() && var11.getType() == 6) {
                        for(int var12 = 0; var12 < var11.getHeight(); ++var12) {
                           for(int var13 = 0; var13 < var11.getWidth(); ++var13) {
                              int var14 = var11.getRGB(var13, var12);
                              if ((var14 & -16777216) != 0) {
                                 int var15 = (var14 & 16711680) << 8 & -16777216;
                                 int var16 = var4.getRGB(var13, var12);
                                 int var17 = MathHelper.multiplyColor(var16, var10.colorValue) & 16777215;
                                 var11.setRGB(var13, var12, var15 | var17);
                              }
                           }
                        }

                        var3.getGraphics().drawImage(var11, 0, 0, (ImageObserver)null);
                     }
                  }
               } finally {
                  IOUtils.closeQuietly(var8);
               }

               ++var7;
            }
         } catch (IOException var27) {
            LOG.error("Couldn't load layered image", var27);
         } finally {
            IOUtils.closeQuietly(var2);
         }

         return;
      }

      TextureUtil.uploadTextureImage(this.getGlTextureId(), var3);
   }
}
