package net.minecraft.client.renderer.block.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.IVertexProducer;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BakedQuad implements IVertexProducer {
   protected final int[] vertexData;
   protected final int tintIndex;
   protected final EnumFacing face;
   protected final TextureAtlasSprite sprite;
   protected final VertexFormat format;
   protected final boolean applyDiffuseLighting;

   /** @deprecated */
   @Deprecated
   public BakedQuad(int[] var1, int var2, EnumFacing var3, TextureAtlasSprite var4) {
      this(var1, var2, var3, var4, true, DefaultVertexFormats.ITEM);
   }

   public BakedQuad(int[] var1, int var2, EnumFacing var3, TextureAtlasSprite var4, boolean var5, VertexFormat var6) {
      this.format = var6;
      this.applyDiffuseLighting = var5;
      this.vertexData = var1;
      this.tintIndex = var2;
      this.face = var3;
      this.sprite = var4;
   }

   public TextureAtlasSprite getSprite() {
      return this.sprite;
   }

   public int[] getVertexData() {
      return this.vertexData;
   }

   public boolean hasTintIndex() {
      return this.tintIndex != -1;
   }

   public int getTintIndex() {
      return this.tintIndex;
   }

   public EnumFacing getFace() {
      return this.face;
   }

   public void pipe(IVertexConsumer var1) {
      LightUtil.putBakedQuad(var1, this);
   }

   public VertexFormat getFormat() {
      return this.format;
   }

   public boolean shouldApplyDiffuseLighting() {
      return this.applyDiffuseLighting;
   }
}
