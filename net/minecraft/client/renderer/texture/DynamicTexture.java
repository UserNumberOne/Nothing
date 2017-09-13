package net.minecraft.client.renderer.texture;

import java.awt.image.BufferedImage;
import java.io.IOException;
import net.minecraft.client.resources.IResourceManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DynamicTexture extends AbstractTexture {
   private final int[] dynamicTextureData;
   private final int width;
   private final int height;

   public DynamicTexture(BufferedImage var1) {
      this(bufferedImage.getWidth(), bufferedImage.getHeight());
      bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), this.dynamicTextureData, 0, bufferedImage.getWidth());
      this.updateDynamicTexture();
   }

   public DynamicTexture(int var1, int var2) {
      this.width = textureWidth;
      this.height = textureHeight;
      this.dynamicTextureData = new int[textureWidth * textureHeight];
      TextureUtil.allocateTexture(this.getGlTextureId(), textureWidth, textureHeight);
   }

   public void loadTexture(IResourceManager var1) throws IOException {
   }

   public void updateDynamicTexture() {
      TextureUtil.uploadTexture(this.getGlTextureId(), this.dynamicTextureData, this.width, this.height);
   }

   public int[] getTextureData() {
      return this.dynamicTextureData;
   }
}
