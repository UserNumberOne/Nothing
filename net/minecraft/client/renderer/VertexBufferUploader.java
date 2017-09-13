package net.minecraft.client.renderer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VertexBufferUploader extends WorldVertexBufferUploader {
   private net.minecraft.client.renderer.vertex.VertexBuffer vertexBuffer;

   public void draw(VertexBuffer var1) {
      vertexBufferIn.reset();
      this.vertexBuffer.bufferData(vertexBufferIn.getByteBuffer());
   }

   public void setVertexBuffer(net.minecraft.client.renderer.vertex.VertexBuffer var1) {
      this.vertexBuffer = vertexBufferIn;
   }
}
