package net.minecraft.client.renderer;

import net.minecraft.client.renderer.chunk.ListedRenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderList extends ChunkRenderContainer {
   public void renderChunkLayer(BlockRenderLayer var1) {
      if (this.initialized) {
         for(RenderChunk var3 : this.renderChunks) {
            ListedRenderChunk var4 = (ListedRenderChunk)var3;
            GlStateManager.pushMatrix();
            this.preRenderChunk(var3);
            GlStateManager.callList(var4.getDisplayList(var1, var4.getCompiledChunk()));
            GlStateManager.popMatrix();
         }

         GlStateManager.resetColor();
         this.renderChunks.clear();
      }

   }
}
