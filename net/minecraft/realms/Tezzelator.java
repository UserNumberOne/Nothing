package net.minecraft.realms;

import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Tezzelator {
   public static Tessellator t = Tessellator.getInstance();
   public static final Tezzelator instance = new Tezzelator();

   public void end() {
      t.draw();
   }

   public Tezzelator vertex(double var1, double var3, double var5) {
      t.getBuffer().pos(p_vertex_1_, p_vertex_3_, p_vertex_5_);
      return this;
   }

   public void color(float var1, float var2, float var3, float var4) {
      t.getBuffer().color(p_color_1_, p_color_2_, p_color_3_, p_color_4_);
   }

   public void tex2(short var1, short var2) {
      t.getBuffer().lightmap(p_tex2_1_, p_tex2_2_);
   }

   public void normal(float var1, float var2, float var3) {
      t.getBuffer().normal(p_normal_1_, p_normal_2_, p_normal_3_);
   }

   public void begin(int var1, RealmsVertexFormat var2) {
      t.getBuffer().begin(p_begin_1_, p_begin_2_.getVertexFormat());
   }

   public void endVertex() {
      t.getBuffer().endVertex();
   }

   public void offset(double var1, double var3, double var5) {
      t.getBuffer().setTranslation(p_offset_1_, p_offset_3_, p_offset_5_);
   }

   public RealmsBufferBuilder color(int var1, int var2, int var3, int var4) {
      return new RealmsBufferBuilder(t.getBuffer().color(p_color_1_, p_color_2_, p_color_3_, p_color_4_));
   }

   public Tezzelator tex(double var1, double var3) {
      t.getBuffer().tex(p_tex_1_, p_tex_3_);
      return this;
   }
}
