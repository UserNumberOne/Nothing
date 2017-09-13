package net.minecraft.client.renderer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Tessellator {
   private final VertexBuffer worldRenderer;
   private final WorldVertexBufferUploader vboUploader = new WorldVertexBufferUploader();
   private static final Tessellator INSTANCE = new Tessellator(2097152);

   public static Tessellator getInstance() {
      return INSTANCE;
   }

   public Tessellator(int bufferSize) {
      this.worldRenderer = new VertexBuffer(bufferSize);
   }

   public void draw() {
      this.worldRenderer.finishDrawing();
      this.vboUploader.draw(this.worldRenderer);
   }

   public VertexBuffer getBuffer() {
      return this.worldRenderer;
   }
}
