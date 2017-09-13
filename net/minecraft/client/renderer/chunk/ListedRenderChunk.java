package net.minecraft.client.renderer.chunk;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ListedRenderChunk extends RenderChunk {
   private final int baseDisplayList = GLAllocation.generateDisplayLists(BlockRenderLayer.values().length);

   public ListedRenderChunk(World var1, RenderGlobal var2, int var3) {
      super(var1, var2, var3);
   }

   public int getDisplayList(BlockRenderLayer var1, CompiledChunk var2) {
      return !var2.isLayerEmpty(var1) ? this.baseDisplayList + var1.ordinal() : -1;
   }

   public void deleteGlResources() {
      super.deleteGlResources();
      GLAllocation.deleteDisplayLists(this.baseDisplayList, BlockRenderLayer.values().length);
   }
}
