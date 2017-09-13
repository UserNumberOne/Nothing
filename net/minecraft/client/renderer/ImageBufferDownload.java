package net.minecraft.client.renderer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ImageBufferDownload implements IImageBuffer {
   private int[] imageData;
   private int imageWidth;
   private int imageHeight;

   public BufferedImage parseUserSkin(BufferedImage var1) {
      if (var1 == null) {
         return null;
      } else {
         this.imageWidth = 64;
         this.imageHeight = 64;
         BufferedImage var2 = new BufferedImage(this.imageWidth, this.imageHeight, 2);
         Graphics var3 = var2.getGraphics();
         var3.drawImage(var1, 0, 0, (ImageObserver)null);
         boolean var4 = var1.getHeight() == 32;
         if (var4) {
            var3.setColor(new Color(0, 0, 0, 0));
            var3.fillRect(0, 32, 64, 32);
            var3.drawImage(var2, 24, 48, 20, 52, 4, 16, 8, 20, (ImageObserver)null);
            var3.drawImage(var2, 28, 48, 24, 52, 8, 16, 12, 20, (ImageObserver)null);
            var3.drawImage(var2, 20, 52, 16, 64, 8, 20, 12, 32, (ImageObserver)null);
            var3.drawImage(var2, 24, 52, 20, 64, 4, 20, 8, 32, (ImageObserver)null);
            var3.drawImage(var2, 28, 52, 24, 64, 0, 20, 4, 32, (ImageObserver)null);
            var3.drawImage(var2, 32, 52, 28, 64, 12, 20, 16, 32, (ImageObserver)null);
            var3.drawImage(var2, 40, 48, 36, 52, 44, 16, 48, 20, (ImageObserver)null);
            var3.drawImage(var2, 44, 48, 40, 52, 48, 16, 52, 20, (ImageObserver)null);
            var3.drawImage(var2, 36, 52, 32, 64, 48, 20, 52, 32, (ImageObserver)null);
            var3.drawImage(var2, 40, 52, 36, 64, 44, 20, 48, 32, (ImageObserver)null);
            var3.drawImage(var2, 44, 52, 40, 64, 40, 20, 44, 32, (ImageObserver)null);
            var3.drawImage(var2, 48, 52, 44, 64, 52, 20, 56, 32, (ImageObserver)null);
         }

         var3.dispose();
         this.imageData = ((DataBufferInt)var2.getRaster().getDataBuffer()).getData();
         this.setAreaOpaque(0, 0, 32, 16);
         if (var4) {
            this.doTransparencyHack(32, 0, 64, 32);
         }

         this.setAreaOpaque(0, 16, 64, 32);
         this.setAreaOpaque(16, 48, 48, 64);
         return var2;
      }
   }

   public void skinAvailable() {
   }

   private void doTransparencyHack(int var1, int var2, int var3, int var4) {
      for(int var5 = var1; var5 < var3; ++var5) {
         for(int var6 = var2; var6 < var4; ++var6) {
            int var7 = this.imageData[var5 + var6 * this.imageWidth];
            if ((var7 >> 24 & 255) < 128) {
               return;
            }
         }
      }

      for(int var8 = var1; var8 < var3; ++var8) {
         for(int var9 = var2; var9 < var4; ++var9) {
            this.imageData[var8 + var9 * this.imageWidth] &= 16777215;
         }
      }

   }

   private void setAreaOpaque(int var1, int var2, int var3, int var4) {
      for(int var5 = var1; var5 < var3; ++var5) {
         for(int var6 = var2; var6 < var4; ++var6) {
            this.imageData[var5 + var6 * this.imageWidth] |= -16777216;
         }
      }

   }
}
