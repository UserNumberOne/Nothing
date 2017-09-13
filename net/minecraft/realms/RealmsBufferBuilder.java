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
      this.b = var1;
   }

   public RealmsBufferBuilder from(VertexBuffer var1) {
      this.b = var1;
      return this;
   }

   public void sortQuads(float var1, float var2, float var3) {
      this.b.sortVertexData(var1, var2, var3);
   }

   public void fixupQuadColor(int var1) {
      this.b.putColor4(var1);
   }

   public ByteBuffer getBuffer() {
      return this.b.getByteBuffer();
   }

   public void postNormal(float var1, float var2, float var3) {
      this.b.putNormal(var1, var2, var3);
   }

   public int getDrawMode() {
      return this.b.getDrawMode();
   }

   public void offset(double var1, double var3, double var5) {
      this.b.setTranslation(var1, var3, var5);
   }

   public void restoreState(VertexBuffer.State var1) {
      this.b.setVertexState(var1);
   }

   public void endVertex() {
      this.b.endVertex();
   }

   public RealmsBufferBuilder normal(float var1, float var2, float var3) {
      return this.from(this.b.normal(var1, var2, var3));
   }

   public void end() {
      this.b.finishDrawing();
   }

   public void begin(int var1, VertexFormat var2) {
      this.b.begin(var1, var2);
   }

   public RealmsBufferBuilder color(int var1, int var2, int var3, int var4) {
      return this.from(this.b.color(var1, var2, var3, var4));
   }

   public void faceTex2(int var1, int var2, int var3, int var4) {
      this.b.putBrightness4(var1, var2, var3, var4);
   }

   public void postProcessFacePosition(double var1, double var3, double var5) {
      this.b.putPosition(var1, var3, var5);
   }

   public void fixupVertexColor(float var1, float var2, float var3, int var4) {
      this.b.putColorRGB_F(var1, var2, var3, var4);
   }

   public RealmsBufferBuilder color(float var1, float var2, float var3, float var4) {
      return this.from(this.b.color(var1, var2, var3, var4));
   }

   public RealmsVertexFormat getVertexFormat() {
      return new RealmsVertexFormat(this.b.getVertexFormat());
   }

   public void faceTint(float var1, float var2, float var3, int var4) {
      this.b.putColorMultiplier(var1, var2, var3, var4);
   }

   public RealmsBufferBuilder tex2(int var1, int var2) {
      return this.from(this.b.lightmap(var1, var2));
   }

   public void putBulkData(int[] var1) {
      this.b.addVertexData(var1);
   }

   public RealmsBufferBuilder tex(double var1, double var3) {
      return this.from(this.b.tex(var1, var3));
   }

   public int getVertexCount() {
      return this.b.getVertexCount();
   }

   public void clear() {
      this.b.reset();
   }

   public RealmsBufferBuilder vertex(double var1, double var3, double var5) {
      return this.from(this.b.pos(var1, var3, var5));
   }

   public void fixupQuadColor(float var1, float var2, float var3) {
      this.b.putColorRGB_F4(var1, var2, var3);
   }

   public void noColor() {
      this.b.noColor();
   }
}
