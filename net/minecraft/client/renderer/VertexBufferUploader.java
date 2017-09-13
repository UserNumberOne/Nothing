package net.minecraft.client.renderer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VertexBufferUploader extends WorldVertexBufferUploader {
   private net.minecraft.client.renderer.vertex.VertexBuffer vertexBuffer;

   public void draw(VertexBuffer var1) {
      var1.reset();
      this.vertexBuffer.bufferData(var1.getByteBuffer());
   }

   public void setVertexBuffer(net.minecraft.client.renderer.vertex.VertexBuffer var1) {
      this.vertexBuffer = var1;
   }
}
