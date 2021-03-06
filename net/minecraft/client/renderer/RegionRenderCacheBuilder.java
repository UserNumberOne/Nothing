package net.minecraft.client.renderer;

import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RegionRenderCacheBuilder {
   private final VertexBuffer[] worldRenderers = new VertexBuffer[BlockRenderLayer.values().length];

   public RegionRenderCacheBuilder() {
      this.worldRenderers[BlockRenderLayer.SOLID.ordinal()] = new VertexBuffer(2097152);
      this.worldRenderers[BlockRenderLayer.CUTOUT.ordinal()] = new VertexBuffer(131072);
      this.worldRenderers[BlockRenderLayer.CUTOUT_MIPPED.ordinal()] = new VertexBuffer(131072);
      this.worldRenderers[BlockRenderLayer.TRANSLUCENT.ordinal()] = new VertexBuffer(262144);
   }

   public VertexBuffer getWorldRendererByLayer(BlockRenderLayer var1) {
      return this.worldRenderers[var1.ordinal()];
   }

   public VertexBuffer getWorldRendererByLayerId(int var1) {
      return this.worldRenderers[var1];
   }
}
