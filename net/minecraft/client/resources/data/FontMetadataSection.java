package net.minecraft.client.resources.data;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FontMetadataSection implements IMetadataSection {
   private final float[] charWidths;
   private final float[] charLefts;
   private final float[] charSpacings;

   public FontMetadataSection(float[] var1, float[] var2, float[] var3) {
      this.charWidths = var1;
      this.charLefts = var2;
      this.charSpacings = var3;
   }
}
