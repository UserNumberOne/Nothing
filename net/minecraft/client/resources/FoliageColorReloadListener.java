package net.minecraft.client.resources;

import java.io.IOException;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ColorizerFoliage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FoliageColorReloadListener implements IResourceManagerReloadListener {
   private static final ResourceLocation LOC_FOLIAGE_PNG = new ResourceLocation("textures/colormap/foliage.png");

   public void onResourceManagerReload(IResourceManager var1) {
      try {
         ColorizerFoliage.setFoliageBiomeColorizer(TextureUtil.readImageData(var1, LOC_FOLIAGE_PNG));
      } catch (IOException var3) {
         ;
      }

   }
}
