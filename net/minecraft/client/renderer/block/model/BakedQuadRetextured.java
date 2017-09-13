package net.minecraft.client.renderer.block.model;

import java.util.Arrays;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BakedQuadRetextured extends BakedQuad {
   private final TextureAtlasSprite texture;

   public BakedQuadRetextured(BakedQuad var1, TextureAtlasSprite var2) {
      super(Arrays.copyOf(var1.getVertexData(), var1.getVertexData().length), var1.tintIndex, FaceBakery.getFacingFromVertexData(var1.getVertexData()), var1.getSprite(), var1.applyDiffuseLighting, var1.format);
      this.texture = var2;
      this.remapQuad();
   }

   private void remapQuad() {
      for(int var1 = 0; var1 < 4; ++var1) {
         int var2 = this.format.getIntegerSize() * var1;
         int var3 = this.format.getUvOffsetById(0) / 4;
         this.vertexData[var2 + var3] = Float.floatToRawIntBits(this.texture.getInterpolatedU((double)this.sprite.getUnInterpolatedU(Float.intBitsToFloat(this.vertexData[var2 + var3]))));
         this.vertexData[var2 + var3 + 1] = Float.floatToRawIntBits(this.texture.getInterpolatedV((double)this.sprite.getUnInterpolatedV(Float.intBitsToFloat(this.vertexData[var2 + var3 + 1]))));
      }

   }
}
