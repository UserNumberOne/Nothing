package net.minecraft.client.renderer;

import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VboRenderList extends ChunkRenderContainer {
   public void renderChunkLayer(BlockRenderLayer var1) {
      if (this.initialized) {
         for(RenderChunk var3 : this.renderChunks) {
            net.minecraft.client.renderer.vertex.VertexBuffer var4 = var3.getVertexBufferByLayer(var1.ordinal());
            GlStateManager.pushMatrix();
            this.preRenderChunk(var3);
            var3.multModelviewMatrix();
            var4.bindBuffer();
            this.setupArrayPointers();
            var4.drawArrays(7);
            GlStateManager.popMatrix();
         }

         OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
         GlStateManager.resetColor();
         this.renderChunks.clear();
      }

   }

   private void setupArrayPointers() {
      GlStateManager.glVertexPointer(3, 5126, 28, 0);
      GlStateManager.glColorPointer(4, 5121, 28, 12);
      GlStateManager.glTexCoordPointer(2, 5126, 28, 16);
      OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
      GlStateManager.glTexCoordPointer(2, 5122, 28, 24);
      OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
   }
}
