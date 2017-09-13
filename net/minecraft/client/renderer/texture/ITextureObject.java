package net.minecraft.client.renderer.texture;

import java.io.IOException;
import net.minecraft.client.resources.IResourceManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface ITextureObject {
   void setBlurMipmap(boolean var1, boolean var2);

   void restoreLastBlurMipmap();

   void loadTexture(IResourceManager var1) throws IOException;

   int getGlTextureId();
}
