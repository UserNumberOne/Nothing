package net.minecraft.client.resources.data;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TextureMetadataSection implements IMetadataSection {
   private final boolean textureBlur;
   private final boolean textureClamp;

   public TextureMetadataSection(boolean var1, boolean var2) {
      this.textureBlur = var1;
      this.textureClamp = var2;
   }

   public boolean getTextureBlur() {
      return this.textureBlur;
   }

   public boolean getTextureClamp() {
      return this.textureClamp;
   }
}
