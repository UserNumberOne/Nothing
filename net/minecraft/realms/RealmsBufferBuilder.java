package net.minecraft.realms;

import java.nio.ByteBuffer;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RealmsBufferBuilder {
   private VertexBuffer b;

   public RealmsBufferBuilder(VertexBuffer var1) {
      this.b = p_i46442_1_;
   }

   public RealmsBufferBuilder from(VertexBuffer var1) {
      this.b = p_from_1_;
      return this;
   }

   public void sortQuads(float var1, float var2, float var3) {
      this.b.sortVertexData(p_sortQuads_1_, p_sortQuads_2_, p_sortQuads_3_);
   }

   public void fixupQuadColor(int var1) {
      this.b.putColor4(p_fixupQuadColor_1_);
   }

   public ByteBuffer getBuffer() {
      return this.b.getByteBuffer();
   }

   public void postNormal(float var1, float var2, float var3) {
      this.b.putNormal(p_postNormal_1_, p_postNormal_2_, p_postNormal_3_);
   }

   public int getDrawMode() {
      return this.b.getDrawMode();
   }

   public void offset(double var1, double var3, double var5) {
      this.b.setTranslation(p_offset_1_, p_offset_3_, p_offset_5_);
   }

   public void restoreState(VertexBuffer.State var1) {
      this.b.setVertexState(p_restoreState_1_);
   }

   public void endVertex() {
      this.b.endVertex();
   }

   public RealmsBufferBuilder normal(float var1, float var2, float var3) {
      return this.from(this.b.normal(p_normal_1_, p_normal_2_, p_normal_3_));
   }

   public void end() {
      this.b.finishDrawing();
   }

   public void begin(int var1, VertexFormat var2) {
      this.b.begin(p_begin_1_, p_begin_2_);
   }

   public RealmsBufferBuilder color(int var1, int var2, int var3, int var4) {
      return this.from(this.b.color(p_color_1_, p_color_2_, p_color_3_, p_color_4_));
   }

   public void faceTex2(int var1, int var2, int var3, int var4) {
      this.b.putBrightness4(p_faceTex2_1_, p_faceTex2_2_, p_faceTex2_3_, p_faceTex2_4_);
   }

   public void postProcessFacePosition(double var1, double var3, double var5) {
      this.b.putPosition(p_postProcessFacePosition_1_, p_postProcessFacePosition_3_, p_postProcessFacePosition_5_);
   }

   public void fixupVertexColor(float var1, float var2, float var3, int var4) {
      this.b.putColorRGB_F(p_fixupVertexColor_1_, p_fixupVertexColor_2_, p_fixupVertexColor_3_, p_fixupVertexColor_4_);
   }

   public RealmsBufferBuilder color(float var1, float var2, float var3, float var4) {
      return this.from(this.b.color(p_color_1_, p_color_2_, p_color_3_, p_color_4_));
   }

   public RealmsVertexFormat getVertexFormat() {
      return new RealmsVertexFormat(this.b.getVertexFormat());
   }

   public void faceTint(float var1, float var2, float var3, int var4) {
      this.b.putColorMultiplier(p_faceTint_1_, p_faceTint_2_, p_faceTint_3_, p_faceTint_4_);
   }

   public RealmsBufferBuilder tex2(int var1, int var2) {
      return this.from(this.b.lightmap(p_tex2_1_, p_tex2_2_));
   }

   public void putBulkData(int[] var1) {
      this.b.addVertexData(p_putBulkData_1_);
   }

   public RealmsBufferBuilder tex(double var1, double var3) {
      return this.from(this.b.tex(p_tex_1_, p_tex_3_));
   }

   public int getVertexCount() {
      return this.b.getVertexCount();
   }

   public void clear() {
      this.b.reset();
   }

   public RealmsBufferBuilder vertex(double var1, double var3, double var5) {
      return this.from(this.b.pos(p_vertex_1_, p_vertex_3_, p_vertex_5_));
   }

   public void fixupQuadColor(float var1, float var2, float var3) {
      this.b.putColorRGB_F4(p_fixupQuadColor_1_, p_fixupQuadColor_2_, p_fixupQuadColor_3_);
   }

   public void noColor() {
      this.b.noColor();
   }
}
